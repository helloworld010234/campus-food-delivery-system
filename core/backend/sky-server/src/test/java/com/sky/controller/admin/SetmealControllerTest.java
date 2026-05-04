package com.sky.controller.admin;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sky.dto.SetmealDTO;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.properties.JwtProperties;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.SetmealService;
import com.sky.service.TokenBlacklistService;
import com.sky.vo.SetmealVO;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(SetmealController.class)
class SetmealControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private SetmealService setmealService;

    @MockitoBean
    private JwtProperties jwtProperties;

    @MockitoBean
    private TokenBlacklistService tokenBlacklistService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void page_shouldReturnPageResult_whenQueryParametersAreProvided() throws Exception {
        PageResult pageResult = new PageResult(10L, Collections.emptyList());

        when(setmealService.pageQuery(any(SetmealPageQueryDTO.class))).thenReturn(pageResult);

        mockMvc.perform(get("/admin/setmeal/page")
                        .param("page", "1")
                        .param("pageSize", "10")
                        .param("name", "Test Setmeal")
                        .param("status", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(Result.success().getCode()))
                .andExpect(jsonPath("$.data.total").value(10));
    }

    @Test
    void save_shouldReturnSuccess_whenRequestBodyIsValid() throws Exception {
        SetmealDTO dto = new SetmealDTO();
        dto.setName("New Setmeal");
        dto.setPrice(new BigDecimal("99.99"));
        dto.setCategoryId(1L);
        dto.setStatus(1);
        dto.setDescription("Delicious setmeal");
        dto.setImage("image_url");

        doNothing().when(setmealService).saveWithDish(any(SetmealDTO.class));

        mockMvc.perform(post("/admin/setmeal")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(Result.success().getCode()));
    }

    @Test
    void getById_shouldReturnSetmealVo_whenSetmealExists() throws Exception {
        SetmealVO vo = SetmealVO.builder()
                .id(1L)
                .name("Test Setmeal")
                .price(new BigDecimal("66.66"))
                .categoryId(2L)
                .status(1)
                .description("Test description")
                .image("test_image_url")
                .categoryName("Category A")
                .build();

        when(setmealService.getByIdWithDish(1L)).thenReturn(vo);

        mockMvc.perform(get("/admin/setmeal/{id}", 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(Result.success().getCode()))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.name").value("Test Setmeal"))
                .andExpect(jsonPath("$.data.price").value(66.66))
                .andExpect(jsonPath("$.data.categoryName").value("Category A"));
    }

    @Test
    void page_shouldReturnPageResult_whenOnlyPaginationParamsProvided() throws Exception {
        PageResult pageResult = new PageResult(0L, Collections.emptyList());

        when(setmealService.pageQuery(any(SetmealPageQueryDTO.class))).thenReturn(pageResult);

        mockMvc.perform(get("/admin/setmeal/page")
                        .param("page", "1")
                        .param("pageSize", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(Result.success().getCode()))
                .andExpect(jsonPath("$.data.total").value(0));
    }

}
