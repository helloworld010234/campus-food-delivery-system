package com.sky.controller.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sky.dto.OrdersSubmitDTO;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.properties.JwtProperties;
import com.sky.service.OrderService;
import com.sky.vo.OrderSubmitVO;
import com.sky.vo.OrderVO;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(OrderController.class)
class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private OrderService orderService;

    @MockitoBean
    private JwtProperties jwtProperties;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void submitOrder_shouldReturnOrderSubmitVo_whenRequestIsValid() throws Exception {
        OrdersSubmitDTO dto = new OrdersSubmitDTO();
        dto.setAddressBookId(1L);
        dto.setAmount(new BigDecimal("38.50"));
        dto.setPayMethod(1);

        OrderSubmitVO vo = OrderSubmitVO.builder()
                .id(100L)
                .orderNumber("ORDER_20250430_001")
                .orderAmount(new BigDecimal("38.50"))
                .orderTime(LocalDateTime.now())
                .build();

        when(orderService.submitOrder(any(OrdersSubmitDTO.class))).thenReturn(vo);

        mockMvc.perform(post("/user/order/submit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(Result.success().getCode()))
                .andExpect(jsonPath("$.data.id").value(100))
                .andExpect(jsonPath("$.data.orderNumber").value("ORDER_20250430_001"))
                .andExpect(jsonPath("$.data.orderAmount").value(38.50));
    }

    @Test
    void historyOrders_shouldReturnPageResult_whenParametersAreValid() throws Exception {
        PageResult pageResult = new PageResult(2L, Collections.emptyList());

        when(orderService.pageQueryUser(1, 10, 2, null)).thenReturn(pageResult);

        mockMvc.perform(get("/user/order/historyOrders")
                        .param("page", "1")
                        .param("pageSize", "10")
                        .param("status", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(Result.success().getCode()))
                .andExpect(jsonPath("$.data.total").value(2));
    }

    @Test
    void orderDetail_shouldReturnOrderVo_whenOrderExists() throws Exception {
        OrderVO orderVO = new OrderVO();
        orderVO.setId(100L);
        orderVO.setOrderDishes("Test Dish");

        when(orderService.detail(100L)).thenReturn(orderVO);

        mockMvc.perform(get("/user/order/orderDetail/{id}", 100))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(Result.success().getCode()))
                .andExpect(jsonPath("$.data.id").value(100))
                .andExpect(jsonPath("$.data.orderDishes").value("Test Dish"));
    }

    @Test
    void historyOrders_shouldUseDefaultBinding_whenStatusIsNull() throws Exception {
        PageResult pageResult = new PageResult(5L, Collections.emptyList());

        when(orderService.pageQueryUser(eq(1), eq(5), eq((Integer) null), eq((Integer) null))).thenReturn(pageResult);

        mockMvc.perform(get("/user/order/historyOrders")
                        .param("page", "1")
                        .param("pageSize", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(Result.success().getCode()))
                .andExpect(jsonPath("$.data.total").value(5));
    }
}
