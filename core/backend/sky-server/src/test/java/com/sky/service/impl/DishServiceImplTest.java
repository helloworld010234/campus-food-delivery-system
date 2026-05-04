package com.sky.service.impl;

import com.sky.constant.MessageConstant;
import com.sky.constant.StatusConstant;
import com.sky.dto.DishDTO;
import com.sky.entity.Dish;
import com.sky.entity.DishFlavor;
import com.sky.entity.SetmealDish;
import com.sky.exception.BaseException;
import com.sky.mapper.CategoryMapper;
import com.sky.mapper.DishFlavorMapper;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetmealDishMapper;
import com.sky.support.MultiMerchantSchemaSupport;
import com.sky.utils.MerchantScopeUtils;
import com.sky.utils.StorefrontImageResolver;
import com.sky.vo.DishVO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DishServiceImplTest {

    @Mock
    private DishMapper dishMapper;

    @Mock
    private DishFlavorMapper dishFlavorMapper;

    @Mock
    private SetmealDishMapper setmealDishMapper;

    @Mock
    private CategoryMapper categoryMapper;

    @Mock
    private StorefrontImageResolver storefrontImageResolver;

    @Mock
    private MultiMerchantSchemaSupport schemaSupport;

    @InjectMocks
    private DishServiceImpl dishService;

    @Test
    void saveWithFlavor_whenLegacySchema_shouldInsertDishAndFlavors() {
        // Arrange
        when(schemaSupport.supportsDishScope()).thenReturn(false);
        when(schemaSupport.supportsCategoryScope()).thenReturn(false);

        DishFlavor flavor1 = DishFlavor.builder()
                .name("Spicy")
                .value("Mild,Hot")
                .build();
        DishFlavor flavor2 = DishFlavor.builder()
                .name("Size")
                .value("Small,Large")
                .build();

        DishDTO dishDTO = new DishDTO();
        dishDTO.setName("Kung Pao Chicken");
        dishDTO.setCategoryId(1L);
        dishDTO.setPrice(new BigDecimal("38.00"));
        dishDTO.setImage("chicken.jpg");
        dishDTO.setFlavors(Arrays.asList(flavor1, flavor2));

        // Act
        dishService.saveWithFlavor(dishDTO);

        // Assert
        verify(dishMapper, times(1)).insertLegacy(any(Dish.class));
        verify(dishMapper, times(1)).insertBatch(anyList());
        verify(dishMapper, never()).insert(any(Dish.class));

        // Verify flavors were updated with the dish id
        // Since dish id is generated inside insertLegacy, we just verify insertBatch was called
        assertNotNull(dishDTO.getFlavors());
        assertEquals(2, dishDTO.getFlavors().size());
    }

    @Test
    void getByIdWithFlavor_whenDishExists_shouldReturnDishVOWithFlavors() {
        // Arrange
        Long dishId = 1L;

        Dish dish = Dish.builder()
                .id(dishId)
                .name("Kung Pao Chicken")
                .categoryId(1L)
                .price(new BigDecimal("38.00"))
                .image("chicken.jpg")
                .status(StatusConstant.ENABLE)
                .build();

        DishFlavor flavor1 = DishFlavor.builder()
                .id(1L)
                .dishId(dishId)
                .name("Spicy")
                .value("Mild,Hot")
                .build();
        DishFlavor flavor2 = DishFlavor.builder()
                .id(2L)
                .dishId(dishId)
                .name("Size")
                .value("Small,Large")
                .build();
        List<DishFlavor> flavors = Arrays.asList(flavor1, flavor2);

        when(dishMapper.getById(dishId)).thenReturn(dish);
        when(dishFlavorMapper.getByDishId(dishId)).thenReturn(flavors);
        when(storefrontImageResolver.resolve(dish.getImage())).thenReturn("resolved_chicken.jpg");

        try (MockedStatic<MerchantScopeUtils> mockedUtils = mockStatic(MerchantScopeUtils.class)) {
            mockedUtils.when(() -> MerchantScopeUtils.assertAccessible(any())).thenAnswer(invocation -> null);

            // Act
            DishVO result = dishService.getByIdWithFlavor(dishId);

            // Assert
            assertNotNull(result);
            assertEquals(dishId, result.getId());
            assertEquals("Kung Pao Chicken", result.getName());
            assertEquals(new BigDecimal("38.00"), result.getPrice());
            assertNotNull(result.getFlavors());
            assertEquals(2, result.getFlavors().size());
            assertEquals("resolved_chicken.jpg", result.getImage());

            verify(dishMapper, times(1)).getById(dishId);
            verify(dishFlavorMapper, times(1)).getByDishId(dishId);
            verify(storefrontImageResolver, times(1)).resolve("chicken.jpg");
        }
    }

    @Test
    void deleteBatch_whenAssociatedWithSetmeal_shouldThrowBaseException() {
        // Arrange
        List<Long> ids = Arrays.asList(1L, 2L);

        Dish dish1 = Dish.builder()
                .id(1L)
                .name("Dish 1")
                .status(StatusConstant.DISABLE)
                .build();
        Dish dish2 = Dish.builder()
                .id(2L)
                .name("Dish 2")
                .status(StatusConstant.DISABLE)
                .build();

        SetmealDish setmealDish = SetmealDish.builder()
                .id(1L)
                .setmealId(10L)
                .dishId(1L)
                .build();
        List<SetmealDish> setmealDishes = Collections.singletonList(setmealDish);

        when(dishMapper.getById(1L)).thenReturn(dish1);
        when(dishMapper.getById(2L)).thenReturn(dish2);
        when(setmealDishMapper.getSetmealsByDishIds(ids)).thenReturn(setmealDishes);

        try (MockedStatic<MerchantScopeUtils> mockedUtils = mockStatic(MerchantScopeUtils.class)) {
            mockedUtils.when(() -> MerchantScopeUtils.assertAccessible(any())).thenAnswer(invocation -> null);

            // Act & Assert
            BaseException exception = assertThrows(BaseException.class, () -> dishService.deleteBatch(ids));
            assertNotNull(exception.getMessage());

            verify(dishMapper, never()).deleteBatch(anyList());
            verify(dishFlavorMapper, never()).deleteBatch(anyList());
        }
    }

    @Test
    void deleteBatch_whenNotAssociatedWithSetmeal_shouldDeleteDishAndFlavors() {
        // Arrange
        List<Long> ids = Arrays.asList(1L, 2L);

        Dish dish1 = Dish.builder()
                .id(1L)
                .name("Dish 1")
                .status(StatusConstant.DISABLE)
                .build();
        Dish dish2 = Dish.builder()
                .id(2L)
                .name("Dish 2")
                .status(StatusConstant.DISABLE)
                .build();

        when(dishMapper.getById(1L)).thenReturn(dish1);
        when(dishMapper.getById(2L)).thenReturn(dish2);
        when(setmealDishMapper.getSetmealsByDishIds(ids)).thenReturn(Collections.emptyList());

        try (MockedStatic<MerchantScopeUtils> mockedUtils = mockStatic(MerchantScopeUtils.class)) {
            mockedUtils.when(() -> MerchantScopeUtils.assertAccessible(any())).thenAnswer(invocation -> null);

            // Act
            dishService.deleteBatch(ids);

            // Assert
            verify(dishMapper, times(1)).deleteBatch(ids);
            verify(dishFlavorMapper, times(1)).deleteBatch(ids);
        }
    }

    @Test
    void deleteBatch_whenDishIsEnabled_shouldThrowBaseException() {
        // Arrange
        List<Long> ids = Collections.singletonList(1L);

        Dish dish = Dish.builder()
                .id(1L)
                .name("Dish 1")
                .status(StatusConstant.ENABLE)
                .build();

        when(dishMapper.getById(1L)).thenReturn(dish);

        try (MockedStatic<MerchantScopeUtils> mockedUtils = mockStatic(MerchantScopeUtils.class)) {
            mockedUtils.when(() -> MerchantScopeUtils.assertAccessible(any())).thenAnswer(invocation -> null);

            // Act & Assert
            BaseException exception = assertThrows(BaseException.class, () -> dishService.deleteBatch(ids));
            assertNotNull(exception.getMessage());

            verify(setmealDishMapper, never()).getSetmealsByDishIds(anyList());
            verify(dishMapper, never()).deleteBatch(anyList());
            verify(dishFlavorMapper, never()).deleteBatch(anyList());
        }
    }

    @Test
    void getByIdWithFlavor_whenDishNotFound_shouldThrowBaseException() {
        // Arrange
        Long dishId = 999L;
        when(dishMapper.getById(dishId)).thenReturn(null);

        // Act & Assert
        BaseException exception = assertThrows(BaseException.class, () -> dishService.getByIdWithFlavor(dishId));
        assertNotNull(exception.getMessage());

        verify(dishFlavorMapper, never()).getByDishId(anyLong());
        verify(storefrontImageResolver, never()).resolve(anyString());
    }

    @Test
    void saveWithFlavor_whenNoFlavors_shouldInsertDishOnly() {
        // Arrange
        when(schemaSupport.supportsDishScope()).thenReturn(false);
        when(schemaSupport.supportsCategoryScope()).thenReturn(false);

        DishDTO dishDTO = new DishDTO();
        dishDTO.setName("Plain Rice");
        dishDTO.setCategoryId(1L);
        dishDTO.setPrice(new BigDecimal("5.00"));
        dishDTO.setFlavors(Collections.emptyList());

        // Act
        dishService.saveWithFlavor(dishDTO);

        // Assert
        verify(dishMapper, times(1)).insertLegacy(any(Dish.class));
        verify(dishMapper, never()).insertBatch(anyList());
    }

    @Test
    void deleteBatch_whenSingleIdAndNotAssociated_shouldDeleteSuccessfully() {
        // Arrange
        List<Long> ids = Collections.singletonList(1L);

        Dish dish = Dish.builder()
                .id(1L)
                .name("Dish 1")
                .status(StatusConstant.DISABLE)
                .build();

        when(dishMapper.getById(1L)).thenReturn(dish);
        when(setmealDishMapper.getSetmealsByDishIds(ids)).thenReturn(Collections.emptyList());

        try (MockedStatic<MerchantScopeUtils> mockedUtils = mockStatic(MerchantScopeUtils.class)) {
            mockedUtils.when(() -> MerchantScopeUtils.assertAccessible(any())).thenAnswer(invocation -> null);

            // Act
            dishService.deleteBatch(ids);

            // Assert
            verify(dishMapper, times(1)).deleteBatch(ids);
            verify(dishFlavorMapper, times(1)).deleteBatch(ids);
        }
    }
}
