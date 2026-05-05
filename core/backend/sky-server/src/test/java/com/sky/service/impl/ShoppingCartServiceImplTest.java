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
import com.sky.support.MultiMerchantSchemaSupport;
import com.sky.utils.StorefrontImageResolver;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Focused unit tests for {@link ShoppingCartServiceImpl}.
 *
 * <p>Covers the multi-merchant isolation contract for the user private cart chain:
 * <ul>
 *   <li>missing merchant id rejection in multi-merchant mode</li>
 *   <li>cross-merchant item rejection (mixed-merchant prevention)</li>
 *   <li>cart cleanup scoped to current user + merchant only</li>
 *   <li>legacy single-merchant fallback when schema lacks columns</li>
 * </ul>
 */
@ExtendWith(MockitoExtension.class)
class ShoppingCartServiceImplTest {

    private static final Long USER_ID = 100L;
    private static final Long MERCHANT_A = 10L;
    private static final Long MERCHANT_B = 20L;

    @Mock
    private ShoppingCartMapper shoppingCartMapper;

    @Mock
    private DishMapper dishMapper;

    @Mock
    private SetmealMapper setmealMapper;

    @Mock
    private StorefrontImageResolver storefrontImageResolver;

    @Mock
    private MultiMerchantSchemaSupport schemaSupport;

    @Mock
    private MerchantScopeGuard merchantScopeGuard;

    @InjectMocks
    private ShoppingCartServiceImpl shoppingCartService;

    @BeforeEach
    void setUp() {
        BaseContext.clear();
        BaseContext.setCurrentId(USER_ID);
    }

    @AfterEach
    void tearDown() {
        BaseContext.clear();
    }

    // -------------------------------------------------------------------------
    // addShoppingCart tests
    // -------------------------------------------------------------------------

    @Test
    void addShoppingCart_whenSchemaReadyAndMissingMerchantId_shouldThrowBaseException() {
        // Arrange
        ShoppingCartDTO dto = new ShoppingCartDTO();
        dto.setDishId(1L);

        when(schemaSupport.supportsShoppingCartScope()).thenReturn(true);
        when(merchantScopeGuard.requireExplicitMerchantId(eq(null), eq(null), any()))
                .thenThrow(new BaseException("shopping cart add requires merchantId or shopId"));

        // Act & Assert
        assertThrows(BaseException.class, () -> shoppingCartService.addShoppingCart(dto));
        verify(shoppingCartMapper, never()).insert(any());
        verify(shoppingCartMapper, never()).insertLegacy(any());
    }

    @Test
    void addShoppingCart_whenDishBelongsToDifferentMerchant_shouldThrowAndNotInsert() {
        // Arrange: cart targets merchant A but dish lives in merchant B
        ShoppingCartDTO dto = new ShoppingCartDTO();
        dto.setMerchantId(MERCHANT_A);
        dto.setDishId(7L);

        Dish dish = Dish.builder()
                .id(7L)
                .name("Cross merchant dish")
                .image("dish.png")
                .price(new BigDecimal("10.00"))
                .merchantId(MERCHANT_B)
                .build();

        when(schemaSupport.supportsShoppingCartScope()).thenReturn(true);
        when(schemaSupport.supportsDishScope()).thenReturn(true);
        when(merchantScopeGuard.requireExplicitMerchantId(eq(MERCHANT_A), eq(null), any()))
                .thenReturn(MERCHANT_A);
        when(shoppingCartMapper.list(any(ShoppingCart.class))).thenReturn(Collections.emptyList());
        when(dishMapper.getById(7L)).thenReturn(dish);
        doThrow(new BaseException("dish merchant mismatch"))
                .when(merchantScopeGuard).assertSameMerchant(eq(MERCHANT_A), eq(MERCHANT_B), eq("dish"));

        // Act & Assert
        assertThrows(BaseException.class, () -> shoppingCartService.addShoppingCart(dto));
        verify(shoppingCartMapper, never()).insert(any());
        verify(shoppingCartMapper, never()).insertLegacy(any());
    }

    @Test
    void addShoppingCart_whenSetmealBelongsToDifferentMerchant_shouldThrowAndNotInsert() {
        // Arrange
        ShoppingCartDTO dto = new ShoppingCartDTO();
        dto.setMerchantId(MERCHANT_A);
        dto.setSetmealId(8L);

        Setmeal setmeal = Setmeal.builder()
                .id(8L)
                .name("Cross merchant set")
                .image("set.png")
                .price(new BigDecimal("20.00"))
                .merchantId(MERCHANT_B)
                .build();

        when(schemaSupport.supportsShoppingCartScope()).thenReturn(true);
        when(schemaSupport.supportsSetmealScope()).thenReturn(true);
        when(merchantScopeGuard.requireExplicitMerchantId(eq(MERCHANT_A), eq(null), any()))
                .thenReturn(MERCHANT_A);
        when(shoppingCartMapper.list(any(ShoppingCart.class))).thenReturn(Collections.emptyList());
        when(setmealMapper.getById(8L)).thenReturn(setmeal);
        doThrow(new BaseException("setmeal merchant mismatch"))
                .when(merchantScopeGuard).assertSameMerchant(eq(MERCHANT_A), eq(MERCHANT_B), eq("setmeal"));

        // Act & Assert
        assertThrows(BaseException.class, () -> shoppingCartService.addShoppingCart(dto));
        verify(shoppingCartMapper, never()).insert(any());
    }

    @Test
    void addShoppingCart_withNoDishOrSetmealId_shouldThrow() {
        // Arrange: schema ready, merchant present, but no item id
        ShoppingCartDTO dto = new ShoppingCartDTO();
        dto.setMerchantId(MERCHANT_A);

        when(schemaSupport.supportsShoppingCartScope()).thenReturn(true);
        when(merchantScopeGuard.requireExplicitMerchantId(eq(MERCHANT_A), eq(null), any()))
                .thenReturn(MERCHANT_A);
        when(shoppingCartMapper.list(any(ShoppingCart.class))).thenReturn(Collections.emptyList());

        // Act & Assert
        assertThrows(BaseException.class, () -> shoppingCartService.addShoppingCart(dto));
        verify(shoppingCartMapper, never()).insert(any());
    }

    @Test
    void addShoppingCart_withDishInSameMerchant_shouldInsertWithUserAndMerchantBinding() {
        // Arrange
        ShoppingCartDTO dto = new ShoppingCartDTO();
        dto.setMerchantId(MERCHANT_A);
        dto.setDishId(11L);

        Dish dish = Dish.builder()
                .id(11L)
                .name("Same merchant dish")
                .image("d.png")
                .price(new BigDecimal("12.50"))
                .merchantId(MERCHANT_A)
                .build();

        when(schemaSupport.supportsShoppingCartScope()).thenReturn(true);
        when(schemaSupport.supportsDishScope()).thenReturn(true);
        when(merchantScopeGuard.requireExplicitMerchantId(eq(MERCHANT_A), eq(null), any()))
                .thenReturn(MERCHANT_A);
        when(shoppingCartMapper.list(any(ShoppingCart.class))).thenReturn(Collections.emptyList());
        when(dishMapper.getById(11L)).thenReturn(dish);

        // Act
        shoppingCartService.addShoppingCart(dto);

        // Assert
        verify(shoppingCartMapper).insert(argThat(cart ->
                MERCHANT_A.equals(cart.getMerchantId())
                        && USER_ID.equals(cart.getUserId())
                        && Integer.valueOf(1).equals(cart.getNumber())
                        && cart.getCreateTime() != null
                        && "Same merchant dish".equals(cart.getName())));
        verify(shoppingCartMapper, never()).insertLegacy(any());
    }

    @Test
    void addShoppingCart_whenLegacyFallback_shouldUseLegacyInsertWithoutMerchantBinding() {
        // Arrange: schema not multi-merchant ready
        ShoppingCartDTO dto = new ShoppingCartDTO();
        dto.setDishId(15L);

        Dish dish = Dish.builder()
                .id(15L)
                .name("Legacy dish")
                .image("legacy.png")
                .price(new BigDecimal("9.00"))
                .build();

        when(schemaSupport.supportsShoppingCartScope()).thenReturn(false);
        when(schemaSupport.supportsDishScope()).thenReturn(false);
        when(shoppingCartMapper.list(any(ShoppingCart.class))).thenReturn(Collections.emptyList());
        when(dishMapper.getById(15L)).thenReturn(dish);

        // Act
        shoppingCartService.addShoppingCart(dto);

        // Assert: legacy insert path used, merchant id is null (fallback semantics).
        verify(shoppingCartMapper).insertLegacy(argThat(cart ->
                cart.getMerchantId() == null && USER_ID.equals(cart.getUserId())));
        verify(shoppingCartMapper, never()).insert(any());
    }

    @Test
    void addShoppingCart_whenItemAlreadyInCart_shouldOnlyIncrementNumber() {
        // Arrange
        ShoppingCartDTO dto = new ShoppingCartDTO();
        dto.setMerchantId(MERCHANT_A);
        dto.setDishId(11L);

        ShoppingCart existing = ShoppingCart.builder()
                .id(99L)
                .userId(USER_ID)
                .merchantId(MERCHANT_A)
                .dishId(11L)
                .number(2)
                .build();

        when(schemaSupport.supportsShoppingCartScope()).thenReturn(true);
        when(merchantScopeGuard.requireExplicitMerchantId(eq(MERCHANT_A), eq(null), any()))
                .thenReturn(MERCHANT_A);
        when(shoppingCartMapper.list(any(ShoppingCart.class)))
                .thenReturn(Collections.singletonList(existing));

        // Act
        shoppingCartService.addShoppingCart(dto);

        // Assert
        verify(shoppingCartMapper).updateNumberById(argThat(cart ->
                cart.getId().equals(99L) && Integer.valueOf(3).equals(cart.getNumber())));
        verify(shoppingCartMapper, never()).insert(any());
        verify(shoppingCartMapper, never()).insertLegacy(any());
    }

    // -------------------------------------------------------------------------
    // subShoppingCart tests
    // -------------------------------------------------------------------------

    @Test
    void subShoppingCart_whenSchemaReadyAndMissingMerchantId_shouldThrow() {
        // Arrange
        ShoppingCartDTO dto = new ShoppingCartDTO();
        dto.setDishId(1L);

        when(schemaSupport.supportsShoppingCartScope()).thenReturn(true);
        when(merchantScopeGuard.requireExplicitMerchantId(eq(null), eq(null), any()))
                .thenThrow(new BaseException("shopping cart subtract requires merchantId or shopId"));

        // Act & Assert
        assertThrows(BaseException.class, () -> shoppingCartService.subShoppingCart(dto));
        verify(shoppingCartMapper, never()).deleteById(anyLong());
        verify(shoppingCartMapper, never()).updateNumberById(any());
    }

    @Test
    void subShoppingCart_whenItemCountMoreThanOne_shouldDecrement() {
        // Arrange
        ShoppingCartDTO dto = new ShoppingCartDTO();
        dto.setMerchantId(MERCHANT_A);
        dto.setDishId(11L);

        ShoppingCart existing = ShoppingCart.builder()
                .id(99L)
                .userId(USER_ID)
                .merchantId(MERCHANT_A)
                .dishId(11L)
                .number(3)
                .build();

        when(schemaSupport.supportsShoppingCartScope()).thenReturn(true);
        when(merchantScopeGuard.requireExplicitMerchantId(eq(MERCHANT_A), eq(null), any()))
                .thenReturn(MERCHANT_A);
        when(shoppingCartMapper.list(any(ShoppingCart.class)))
                .thenReturn(Collections.singletonList(existing));

        // Act
        shoppingCartService.subShoppingCart(dto);

        // Assert
        verify(shoppingCartMapper).updateNumberById(argThat(cart ->
                cart.getId().equals(99L) && Integer.valueOf(2).equals(cart.getNumber())));
        verify(shoppingCartMapper, never()).deleteById(anyLong());
    }

    @Test
    void subShoppingCart_whenItemCountIsOne_shouldDeleteRow() {
        // Arrange
        ShoppingCartDTO dto = new ShoppingCartDTO();
        dto.setMerchantId(MERCHANT_A);
        dto.setDishId(11L);

        ShoppingCart existing = ShoppingCart.builder()
                .id(99L)
                .userId(USER_ID)
                .merchantId(MERCHANT_A)
                .dishId(11L)
                .number(1)
                .build();

        when(schemaSupport.supportsShoppingCartScope()).thenReturn(true);
        when(merchantScopeGuard.requireExplicitMerchantId(eq(MERCHANT_A), eq(null), any()))
                .thenReturn(MERCHANT_A);
        when(shoppingCartMapper.list(any(ShoppingCart.class)))
                .thenReturn(Collections.singletonList(existing));

        // Act
        shoppingCartService.subShoppingCart(dto);

        // Assert
        verify(shoppingCartMapper).deleteById(99L);
        verify(shoppingCartMapper, never()).updateNumberById(any());
    }

    @Test
    void subShoppingCart_whenNoMatchingRow_shouldBeNoOp() {
        // Arrange
        ShoppingCartDTO dto = new ShoppingCartDTO();
        dto.setMerchantId(MERCHANT_A);
        dto.setDishId(11L);

        when(schemaSupport.supportsShoppingCartScope()).thenReturn(true);
        when(merchantScopeGuard.requireExplicitMerchantId(eq(MERCHANT_A), eq(null), any()))
                .thenReturn(MERCHANT_A);
        when(shoppingCartMapper.list(any(ShoppingCart.class)))
                .thenReturn(Collections.emptyList());

        // Act
        shoppingCartService.subShoppingCart(dto);

        // Assert: silent no-op preserves existing behaviour for empty cart sub.
        verify(shoppingCartMapper, never()).deleteById(anyLong());
        verify(shoppingCartMapper, never()).updateNumberById(any());
    }

    // -------------------------------------------------------------------------
    // showShoppingCart tests
    // -------------------------------------------------------------------------

    @Test
    void showShoppingCart_whenSchemaReadyAndMissingMerchant_shouldThrow() {
        // Arrange
        when(schemaSupport.supportsShoppingCartScope()).thenReturn(true);
        when(merchantScopeGuard.requireExplicitMerchantId(eq(null), eq(null), any()))
                .thenThrow(new BaseException("shopping cart list requires merchantId or shopId"));

        // Act & Assert
        assertThrows(BaseException.class, () -> shoppingCartService.showShoppingCart(null));
    }

    @Test
    void showShoppingCart_whenSchemaReadyAndMerchantPresent_shouldQueryByUserAndMerchant() {
        // Arrange
        ShoppingCart row = ShoppingCart.builder()
                .id(1L)
                .userId(USER_ID)
                .merchantId(MERCHANT_A)
                .image("img.png")
                .build();
        when(schemaSupport.supportsShoppingCartScope()).thenReturn(true);
        when(merchantScopeGuard.requireExplicitMerchantId(eq(MERCHANT_A), eq(null), any()))
                .thenReturn(MERCHANT_A);
        when(shoppingCartMapper.list(any(ShoppingCart.class)))
                .thenReturn(Collections.singletonList(row));
        when(storefrontImageResolver.resolve("img.png")).thenReturn("https://cdn/img.png");

        // Act
        List<ShoppingCart> result = shoppingCartService.showShoppingCart(MERCHANT_A);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("https://cdn/img.png", result.get(0).getImage());
        verify(shoppingCartMapper).list(argThat(cond ->
                MERCHANT_A.equals(cond.getMerchantId()) && USER_ID.equals(cond.getUserId())));
    }

    @Test
    void showShoppingCart_whenLegacyFallback_shouldNotRequireMerchant() {
        // Arrange
        when(schemaSupport.supportsShoppingCartScope()).thenReturn(false);
        when(shoppingCartMapper.list(any(ShoppingCart.class)))
                .thenReturn(Collections.emptyList());

        // Act
        List<ShoppingCart> result = shoppingCartService.showShoppingCart(null);

        // Assert: no guard invocation, query bound to user only.
        assertNotNull(result);
        verify(merchantScopeGuard, never()).requireExplicitMerchantId(any(), any(), any());
        verify(shoppingCartMapper).list(argThat(cond ->
                cond.getMerchantId() == null && USER_ID.equals(cond.getUserId())));
    }

    // -------------------------------------------------------------------------
    // cleanShoppingCart tests
    // -------------------------------------------------------------------------

    @Test
    void cleanShoppingCart_whenSchemaReadyAndMissingMerchant_shouldThrowAndNotWipeAnything() {
        // Arrange
        when(schemaSupport.supportsShoppingCartScope()).thenReturn(true);
        when(merchantScopeGuard.requireExplicitMerchantId(eq(null), eq(null), any()))
                .thenThrow(new BaseException("shopping cart clean requires merchantId or shopId"));

        // Act & Assert
        assertThrows(BaseException.class, () -> shoppingCartService.cleanShoppingCart(null));

        // Critical: missing merchant must NOT silently wipe across all merchants.
        verify(shoppingCartMapper, never()).deleteByUserId(anyLong());
        verify(shoppingCartMapper, never()).deleteByUserIdAndMerchantId(anyLong(), anyLong());
    }

    @Test
    void cleanShoppingCart_whenSchemaReadyAndMerchantPresent_shouldDeleteOnlyThatMerchantCart() {
        // Arrange
        when(schemaSupport.supportsShoppingCartScope()).thenReturn(true);
        when(merchantScopeGuard.requireExplicitMerchantId(eq(MERCHANT_A), eq(null), any()))
                .thenReturn(MERCHANT_A);

        // Act
        shoppingCartService.cleanShoppingCart(MERCHANT_A);

        // Assert: scoped delete only, leaving other merchant carts intact.
        verify(shoppingCartMapper).deleteByUserIdAndMerchantId(USER_ID, MERCHANT_A);
        verify(shoppingCartMapper, never()).deleteByUserId(anyLong());
    }

    @Test
    void cleanShoppingCart_whenLegacyFallback_shouldDeleteAllForUser() {
        // Arrange
        when(schemaSupport.supportsShoppingCartScope()).thenReturn(false);

        // Act
        shoppingCartService.cleanShoppingCart(null);

        // Assert: legacy mapper path; never silently invokes guard.
        verify(shoppingCartMapper).deleteByUserId(USER_ID);
        verify(shoppingCartMapper, never()).deleteByUserIdAndMerchantId(anyLong(), anyLong());
        verify(merchantScopeGuard, never()).requireExplicitMerchantId(any(), any(), any());
    }
}
