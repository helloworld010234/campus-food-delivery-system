package com.sky.service.impl;

import com.sky.context.BaseContext;
import com.sky.dto.ShoppingCartDTO;
import com.sky.entity.Dish;
import com.sky.entity.Setmeal;
import com.sky.entity.ShoppingCart;
import com.sky.exception.BaseException;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetmealMapper;
import com.sky.mapper.ShoppingCartMapper;
import com.sky.security.MerchantScopeGuard;
import com.sky.service.ShoppingCartService;
import com.sky.support.MultiMerchantSchemaSupport;
import com.sky.utils.StorefrontImageResolver;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Shopping cart operations are private to the current user. When the multi-merchant
 * schema is ready, every read/write must be scoped to {@code userId + merchantId}.
 * In legacy single-merchant fallback mode (column not present) we keep the previous
 * behaviour by ignoring the merchant column entirely.
 *
 * <p>Merchant resolution and cross-merchant validation are delegated to
 * {@link MerchantScopeGuard}; this service must not invent its own scope rules.</p>
 */
@Service
@RequiredArgsConstructor
public class ShoppingCartServiceImpl implements ShoppingCartService {

    private static final String OP_ADD = "shopping cart add";
    private static final String OP_SUB = "shopping cart subtract";
    private static final String OP_LIST = "shopping cart list";
    private static final String OP_CLEAN = "shopping cart clean";

    private final ShoppingCartMapper shoppingCartMapper;

    private final DishMapper dishMapper;

    private final SetmealMapper setmealMapper;

    private final StorefrontImageResolver storefrontImageResolver;

    private final MultiMerchantSchemaSupport schemaSupport;

    private final MerchantScopeGuard merchantScopeGuard;

    @Override
    public void addShoppingCart(ShoppingCartDTO shoppingCartDTO) {
        Long logicalMerchantId = resolvePrivateMerchantId(
                shoppingCartDTO.getMerchantId(), shoppingCartDTO.getShopId(), OP_ADD);
        Long persistedMerchantId = schemaSupport.supportsShoppingCartScope() ? logicalMerchantId : null;

        ShoppingCart shoppingCart = new ShoppingCart();
        BeanUtils.copyProperties(shoppingCartDTO, shoppingCart);
        shoppingCart.setMerchantId(persistedMerchantId);
        shoppingCart.setUserId(BaseContext.getCurrentId());

        // Lookup any existing line for the same user+merchant+(dish|setmeal+flavor).
        List<ShoppingCart> shoppingCartList = shoppingCartMapper.list(shoppingCart);
        if (shoppingCartList != null && shoppingCartList.size() == 1) {
            ShoppingCart existing = shoppingCartList.get(0);
            existing.setNumber(existing.getNumber() + 1);
            shoppingCartMapper.updateNumberById(existing);
            return;
        }

        if (shoppingCartDTO.getDishId() != null) {
            Dish dish = dishMapper.getById(shoppingCartDTO.getDishId());
            if (dish == null) {
                throw new BaseException("dish not found");
            }
            assertItemBelongsToMerchant(dish.getMerchantId(), logicalMerchantId,
                    schemaSupport.supportsDishScope(), "dish");
            shoppingCart.setName(dish.getName());
            shoppingCart.setImage(dish.getImage());
            shoppingCart.setAmount(dish.getPrice());
        } else if (shoppingCartDTO.getSetmealId() != null) {
            Setmeal setmeal = setmealMapper.getById(shoppingCartDTO.getSetmealId());
            if (setmeal == null) {
                throw new BaseException("setmeal not found");
            }
            assertItemBelongsToMerchant(setmeal.getMerchantId(), logicalMerchantId,
                    schemaSupport.supportsSetmealScope(), "setmeal");
            shoppingCart.setName(setmeal.getName());
            shoppingCart.setImage(setmeal.getImage());
            shoppingCart.setAmount(setmeal.getPrice());
        } else {
            throw new BaseException("shopping cart item cannot be empty");
        }

        shoppingCart.setNumber(1);
        shoppingCart.setCreateTime(LocalDateTime.now());
        if (schemaSupport.supportsShoppingCartScope()) {
            shoppingCartMapper.insert(shoppingCart);
        } else {
            shoppingCartMapper.insertLegacy(shoppingCart);
        }
    }

    @Override
    public List<ShoppingCart> showShoppingCart(Long merchantId) {
        Long persistedMerchantId = schemaSupport.supportsShoppingCartScope()
                ? resolvePrivateMerchantId(merchantId, null, OP_LIST)
                : null;
        ShoppingCart shoppingCart = ShoppingCart.builder()
                .userId(BaseContext.getCurrentId())
                .merchantId(persistedMerchantId)
                .build();
        List<ShoppingCart> list = shoppingCartMapper.list(shoppingCart);
        if (list != null) {
            list.forEach(item -> item.setImage(storefrontImageResolver.resolve(item.getImage())));
        }
        return list;
    }

    @Override
    public void cleanShoppingCart(Long merchantId) {
        Long userId = BaseContext.getCurrentId();
        if (!schemaSupport.supportsShoppingCartScope()) {
            // Legacy single-merchant table: cart has no merchant_id column at all.
            shoppingCartMapper.deleteByUserId(userId);
            return;
        }

        // Multi-merchant ready: clearing must be scoped to a single merchant. We
        // refuse to silently wipe carts across all merchants.
        Long resolvedMerchantId = resolvePrivateMerchantId(merchantId, null, OP_CLEAN);
        shoppingCartMapper.deleteByUserIdAndMerchantId(userId, resolvedMerchantId);
    }

    @Override
    public void subShoppingCart(ShoppingCartDTO shoppingCartDTO) {
        Long logicalMerchantId = resolvePrivateMerchantId(
                shoppingCartDTO.getMerchantId(), shoppingCartDTO.getShopId(), OP_SUB);
        Long persistedMerchantId = schemaSupport.supportsShoppingCartScope() ? logicalMerchantId : null;

        ShoppingCart shoppingCart = new ShoppingCart();
        BeanUtils.copyProperties(shoppingCartDTO, shoppingCart);
        shoppingCart.setMerchantId(persistedMerchantId);
        shoppingCart.setUserId(BaseContext.getCurrentId());

        List<ShoppingCart> list = shoppingCartMapper.list(shoppingCart);
        if (list == null || list.isEmpty()) {
            return;
        }

        ShoppingCart existing = list.get(0);
        if (existing.getNumber() == 1) {
            shoppingCartMapper.deleteById(existing.getId());
        } else {
            existing.setNumber(existing.getNumber() - 1);
            shoppingCartMapper.updateNumberById(existing);
        }
    }

    /**
     * Resolve the merchant id for a private cart operation. When the cart schema
     * supports merchant scoping the guard requires an explicit merchant id (we
     * never fall back to "first enabled merchant" for private writes). When the
     * schema is in single-merchant fallback the call returns null and the caller
     * uses the legacy mapper paths.
     */
    private Long resolvePrivateMerchantId(Long merchantId, Long shopId, String operation) {
        if (!schemaSupport.supportsShoppingCartScope()) {
            return null;
        }
        return merchantScopeGuard.requireExplicitMerchantId(merchantId, shopId, operation);
    }

    private void assertItemBelongsToMerchant(Long itemMerchantId, Long expectedMerchantId,
                                             boolean schemaSupportsItemScope, String resource) {
        if (!schemaSupportsItemScope || itemMerchantId == null || expectedMerchantId == null) {
            // Legacy data without a merchant column on the item, or single-merchant
            // fallback. Skip the cross-merchant assertion.
            return;
        }
        merchantScopeGuard.assertSameMerchant(expectedMerchantId, itemMerchantId, resource);
    }
}
