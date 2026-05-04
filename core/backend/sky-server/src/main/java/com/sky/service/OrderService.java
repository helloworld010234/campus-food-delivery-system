package com.sky.service;

import com.sky.dto.OrdersCancelDTO;
import com.sky.dto.OrdersConfirmDTO;
import com.sky.dto.OrdersPageQueryDTO;
import com.sky.dto.OrdersPaymentDTO;
import com.sky.dto.OrdersRejectionDTO;
import com.sky.dto.OrdersSubmitDTO;
import com.sky.result.PageResult;
import com.sky.vo.OrderPaymentVO;
import com.sky.vo.OrderStatisticsVO;
import com.sky.vo.OrderSubmitVO;
import com.sky.vo.OrderVO;

import java.time.LocalDateTime;

public interface OrderService {

    OrderSubmitVO submitOrder(OrdersSubmitDTO orders);

    OrderPaymentVO payment(OrdersPaymentDTO ordersPaymentDTO) throws Exception;

    void paySuccess(String outTradeNo);

    PageResult pageQueryUser(int page, int pageSize, Integer status, Integer payStatus);

    OrderVO detail(Long id);

    void cancelOrderById(Long id) throws Exception;

    void repetition(Long id);

    PageResult conditionSearch(OrdersPageQueryDTO ordersPageQueryDTO);

    OrderStatisticsVO statistics();

    void confirm(OrdersConfirmDTO confirmDTO);

    void rejection(OrdersRejectionDTO ordersRejectionDTO) throws Exception;

    void cancel(OrdersCancelDTO ordersCancelDTO) throws Exception;

    void delivery(Long id);

    void complete(Long id);

    void reminder(Long id);

    LocalDateTime getEstimatedDeliveryTime(Long merchantId, String customerAddress);
}
