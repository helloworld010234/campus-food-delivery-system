package com.sky.service.impl;

import com.sky.context.BaseContext;
import com.sky.dto.ShoppingCartDTO;
import com.sky.entity.Dish;
import com.sky.entity.Merchant;
import com.sky.entity.Setmeal;
import com.sky.entity.ShoppingCart;
import com.sky.exception.BaseException;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetmealMapper;
import com.sky.mapper.ShoppingCartMapper;
import com.sky.service.MerchantService;
import com.sky.service.ShoppingCartService;
import com.sky.support.MultiMerchantSchemaSupport;
import com.sky.utils.StorefrontImageResolver;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ShoppingCartServiceImpl implements ShoppingCartService {

    @Autowired
    private ShoppingCartMapper shoppingCartMapper;

    @Autowired
    private DishMapper dishMapper;

    @Autowired
    private SetmealMapper setmealMapper;

    @Autowired
    private StorefrontImageResolver storefrontImageResolver;

    @Autowired
    private MerchantService merchantService;

    @Autowired
    private MultiMerchantSchemaSupport schemaSupport;

    @Override
    public void addShoppingCart(ShoppingCartDTO shoppingCartDTO) {
        Long requestedMerchantId = shoppingCartDTO.getMerchantId() != null
                ? shoppingCartDTO.getMerchantId()
                : shoppingCartDTO.getShopId();
        Long logicalMerchantId = resolveRequestedMerchantId(requestedMerchantId);
        Long persistedMerchantId = schemaSupport.supportsShoppingCartScope() ? logicalMerchantId : null;

        ShoppingCart shoppingCart = new ShoppingCart();
        BeanUtils.copyProperties(shoppingCartDTO, shoppingCart);
        shoppingCart.setMerchantId(persistedMerchantId);
        shoppingCart.setUserId(BaseContext.getCurrentId());

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
                throw new BaseException("鑿滃搧涓嶅瓨鍦?");
            }
            if (schemaSupport.supportsDishScope() && dish.getMerchantId() != null && !logicalMerchantId.equals(dish.getMerchantId())) {
                throw new BaseException("鑿滃搧涓嶅睘浜庡綋鍓嶅晢鎴?");
            }
            shoppingCart.setName(dish.getName());
            shoppingCart.setImage(dish.getImage());
            shoppingCart.setAmount(dish.getPrice());
        } else if (shoppingCartDTO.getSetmealId() != null) {
            Setmeal setmeal = setmealMapper.getById(shoppingCartDTO.getSetmealId());
            if (setmeal == null) {
                throw new BaseException("濂楅涓嶅瓨鍦?");
            }
            if (schemaSupport.supportsSetmealScope() && setmeal.getMerchantId() != null && !logicalMerchantId.equals(setmeal.getMerchantId())) {
                throw new BaseException("濂楅涓嶅睘浜庡綋鍓嶅晢鎴?");
            }
            shoppingCart.setName(setmeal.getName());
            shoppingCart.setImage(setmeal.getImage());
            shoppingCart.setAmount(setmeal.getPrice());
        } else {
            throw new BaseException("璐墿杞﹀晢鍝佷笉鑳戒负绌?");
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
        ShoppingCart shoppingCart = ShoppingCart.builder()
                .userId(BaseContext.getCurrentId())
                .merchantId(schemaSupport.supportsShoppingCartScope() ? resolveRequestedMerchantId(merchantId) : null)
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
            shoppingCartMapper.deleteByUserId(userId);
            return;
        }

        Long resolvedMerchantId = resolveRequestedMerchantId(merchantId);
        if (resolvedMerchantId == null) {
            shoppingCartMapper.deleteByUserId(userId);
        } else {
            shoppingCartMapper.deleteByUserIdAndMerchantId(userId, resolvedMerchantId);
        }
    }

    @Override
    public void subShoppingCart(ShoppingCartDTO shoppingCartDTO) {
        Long requestedMerchantId = shoppingCartDTO.getMerchantId() != null
                ? shoppingCartDTO.getMerchantId()
                : shoppingCartDTO.getShopId();
        Long logicalMerchantId = resolveRequestedMerchantId(requestedMerchantId);
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

    private Long resolveRequestedMerchantId(Long merchantId) {
        if (merchantId != null) {
            return merchantId;
        }
        Merchant merchant = merchantService.getFirstEnabledMerchant(null);
        return merchant == null ? schemaSupport.getDefaultMerchantId() : merchant.getId();
    }
}
