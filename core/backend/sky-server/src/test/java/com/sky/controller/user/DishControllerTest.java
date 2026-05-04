package com.sky.controller.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sky.entity.Merchant;
import com.sky.properties.JwtProperties;
import com.sky.result.Result;
import com.sky.service.DishService;
import com.sky.service.MerchantService;
import com.sky.vo.DishVO;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(DishController.class)
class DishControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private DishService dishService;

    @MockBean
    private RedisTemplate<String, Object> redisTemplate;

    @MockBean
    private MerchantService merchantService;

    @MockBean
    private JwtProperties jwtProperties;

    @Test
    void list_shouldReturnDishesFromCache_whenCacheHit() throws Exception {
        Long categoryId = 1L;
        Long merchantId = 10L;
        String cacheKey = "dish_" + merchantId + "_" + categoryId;

        DishVO cachedDish = DishVO.builder()
                .id(1L)
                .name("Cached Dish")
                .price(new BigDecimal("15.99"))
                .build();
        List<DishVO> cachedList = List.of(cachedDish);

        ValueOperations<String, Object> valueOps = org.mockito.Mockito.mock(ValueOperations.class);
        when(redisTemplate.opsForValue()).thenReturn(valueOps);
        when(valueOps.get(eq(cacheKey))).thenReturn(cachedList);

        mockMvc.perform(get("/user/dish/list")
                        .param("categoryId", categoryId.toString())
                        .param("merchantId", merchantId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1))
                .andExpect(jsonPath("$.data[0].name").value("Cached Dish"))
                .andExpect(jsonPath("$.data[0].price").value(15.99));

        verify(dishService, never()).listWithFlavor(any());
        verify(redisTemplate.opsForValue(), never()).set(any(), any());
    }

    @Test
    void list_shouldQueryServiceAndCache_whenCacheMiss() throws Exception {
        Long categoryId = 2L;
        Long merchantId = 20L;
        String cacheKey = "dish_" + merchantId + "_" + categoryId;

        DishVO dishVO = DishVO.builder()
                .id(2L)
                .name("Fresh Dish")
                .price(new BigDecimal("25.50"))
                .build();
        List<DishVO> serviceList = List.of(dishVO);

        ValueOperations<String, Object> valueOps = org.mockito.Mockito.mock(ValueOperations.class);
        when(redisTemplate.opsForValue()).thenReturn(valueOps);
        when(valueOps.get(eq(cacheKey))).thenReturn(null);
        when(dishService.listWithFlavor(any())).thenReturn(serviceList);

        mockMvc.perform(get("/user/dish/list")
                        .param("categoryId", categoryId.toString())
                        .param("merchantId", merchantId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1))
                .andExpect(jsonPath("$.data[0].name").value("Fresh Dish"))
                .andExpect(jsonPath("$.data[0].price").value(25.50));

        verify(dishService, times(1)).listWithFlavor(any());
        verify(valueOps, times(1)).set(eq(cacheKey), eq(serviceList));
    }

    @Test
    void list_shouldResolveMerchantId_whenMerchantIdIsNull() throws Exception {
        Long categoryId = 3L;
        Long resolvedMerchantId = 5L;
        String cacheKey = "dish_" + resolvedMerchantId + "_" + categoryId;

        Merchant merchant = Merchant.builder()
                .id(resolvedMerchantId)
                .name("Default Merchant")
                .build();

        DishVO dishVO = DishVO.builder()
                .id(3L)
                .name("Default Merchant Dish")
                .price(new BigDecimal("12.00"))
                .build();
        List<DishVO> serviceList = List.of(dishVO);

        ValueOperations<String, Object> valueOps = org.mockito.Mockito.mock(ValueOperations.class);
        when(redisTemplate.opsForValue()).thenReturn(valueOps);
        when(valueOps.get(eq(cacheKey))).thenReturn(null);
        when(merchantService.getFirstEnabledMerchant(isNull())).thenReturn(merchant);
        when(dishService.listWithFlavor(any())).thenReturn(serviceList);

        mockMvc.perform(get("/user/dish/list")
                        .param("categoryId", categoryId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1))
                .andExpect(jsonPath("$.data[0].name").value("Default Merchant Dish"));

        verify(merchantService, times(1)).getFirstEnabledMerchant(isNull());
        verify(dishService, times(1)).listWithFlavor(any());
        verify(valueOps, times(1)).set(eq(cacheKey), eq(serviceList));
    }

    @Test
    void list_shouldReturnEmptyList_whenNoDishesFound() throws Exception {
        Long categoryId = 99L;
        Long merchantId = 99L;
        String cacheKey = "dish_" + merchantId + "_" + categoryId;

        ValueOperations<String, Object> valueOps = org.mockito.Mockito.mock(ValueOperations.class);
        when(redisTemplate.opsForValue()).thenReturn(valueOps);
        when(valueOps.get(eq(cacheKey))).thenReturn(null);
        when(dishService.listWithFlavor(any())).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/user/dish/list")
                        .param("categoryId", categoryId.toString())
                        .param("merchantId", merchantId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data").isEmpty());

        verify(dishService, times(1)).listWithFlavor(any());
        verify(valueOps, times(1)).set(eq(cacheKey), eq(Collections.emptyList()));
    }
}
