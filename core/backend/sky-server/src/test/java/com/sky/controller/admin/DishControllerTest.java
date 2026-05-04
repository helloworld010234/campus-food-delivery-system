package com.sky.controller.admin;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.DishFlavor;
import com.sky.properties.JwtProperties;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.DishService;
import com.sky.vo.DishVO;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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
    private RedisTemplate redisTemplate;

    @MockBean
    private JwtProperties jwtProperties;

    @Test
    void save_shouldReturnSuccess_andCleanCache() throws Exception {
        DishDTO dishDTO = new DishDTO();
        dishDTO.setName("Test Dish");
        dishDTO.setPrice(new BigDecimal("19.99"));
        dishDTO.setCategoryId(1L);
        dishDTO.setFlavors(List.of(
                DishFlavor.builder().name("Spiciness").value("[\"Mild\", \"Hot\"]").build()
        ));

        doNothing().when(dishService).saveWithFlavor(any(DishDTO.class));

        ValueOperations<String, Object> valueOps = org.mockito.Mockito.mock(ValueOperations.class);
        when(redisTemplate.opsForValue()).thenReturn(valueOps);
        when(redisTemplate.keys(anyString())).thenReturn(Set.of("dish_1"));
        when(redisTemplate.delete(any(Set.class))).thenReturn(1L);

        mockMvc.perform(post("/admin/dish")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dishDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1));

        verify(dishService, times(1)).saveWithFlavor(any(DishDTO.class));
        verify(redisTemplate, times(1)).keys("dish_*");
        verify(redisTemplate, times(1)).delete(any(Set.class));
    }

    @Test
    void page_shouldReturnPageResult() throws Exception {
        DishPageQueryDTO queryDTO = new DishPageQueryDTO();
        queryDTO.setPage(1);
        queryDTO.setPageSize(10);
        queryDTO.setName("Test");

        DishVO dishVO = DishVO.builder()
                .id(1L)
                .name("Test Dish")
                .price(new BigDecimal("19.99"))
                .build();
        PageResult pageResult = new PageResult(1L, List.of(dishVO));

        when(dishService.page(any(DishPageQueryDTO.class))).thenReturn(pageResult);

        mockMvc.perform(get("/admin/dish/page")
                        .param("page", "1")
                        .param("pageSize", "10")
                        .param("name", "Test"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1))
                .andExpect(jsonPath("$.data.total").value(1))
                .andExpect(jsonPath("$.data.records[0].name").value("Test Dish"));

        verify(dishService, times(1)).page(any(DishPageQueryDTO.class));
    }

    @Test
    void getById_shouldReturnDishVO_whenDishExists() throws Exception {
        DishVO dishVO = DishVO.builder()
                .id(1L)
                .name("Test Dish")
                .price(new BigDecimal("29.99"))
                .flavors(List.of(DishFlavor.builder().name("Sweetness").value("[\"Low\", \"High\"]").build()))
                .build();

        when(dishService.getByIdWithFlavor(eq(1L))).thenReturn(dishVO);

        mockMvc.perform(get("/admin/dish/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.name").value("Test Dish"))
                .andExpect(jsonPath("$.data.price").value(29.99))
                .andExpect(jsonPath("$.data.flavors[0].name").value("Sweetness"));

        verify(dishService, times(1)).getByIdWithFlavor(eq(1L));
    }

    @Test
    void save_shouldReturnSuccess_withEmptyFlavors() throws Exception {
        DishDTO dishDTO = new DishDTO();
        dishDTO.setName("Simple Dish");
        dishDTO.setPrice(new BigDecimal("9.99"));
        dishDTO.setCategoryId(2L);
        dishDTO.setFlavors(Collections.emptyList());

        doNothing().when(dishService).saveWithFlavor(any(DishDTO.class));

        ValueOperations<String, Object> valueOps = org.mockito.Mockito.mock(ValueOperations.class);
        when(redisTemplate.opsForValue()).thenReturn(valueOps);
        when(redisTemplate.keys(anyString())).thenReturn(Set.of("dish_2"));
        when(redisTemplate.delete(any(Set.class))).thenReturn(1L);

        mockMvc.perform(post("/admin/dish")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dishDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1))
                .andExpect(jsonPath("$.data").doesNotExist());

        verify(dishService, times(1)).saveWithFlavor(any(DishDTO.class));
    }
}
