package com.sky.controller.user;

import com.sky.dto.OrdersPaymentDTO;
import com.sky.dto.OrdersSubmitDTO;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.OrderService;
import com.sky.vo.OrderPaymentVO;
import com.sky.vo.OrderSubmitVO;
import com.sky.vo.OrderVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

@RestController("userOrder")
@RequestMapping("/user/order")
public class OrderController {

    @Autowired
    private OrderService orderService;

    @PostMapping("/submit")
    public Result<OrderSubmitVO> submit(@RequestBody OrdersSubmitDTO ordersSubmitDTO) {
        return Result.success(orderService.submitOrder(ordersSubmitDTO));
    }

    @PutMapping("/payment")
    public Result<OrderPaymentVO> payment(@RequestBody OrdersPaymentDTO ordersPaymentDTO) throws Exception {
        return Result.success(orderService.payment(ordersPaymentDTO));
    }

    @GetMapping("/historyOrders")
    public Result<PageResult> page(int page, int pageSize, Integer status) {
        return Result.success(orderService.pageQueryUser(page, pageSize, status, null));
    }

    @GetMapping("/queryOrdersCheckStatus")
    public Result<PageResult> queryOrdersCheckStatus(int page, int pageSize, Integer payStatus) {
        return Result.success(orderService.pageQueryUser(page, pageSize, null, payStatus));
    }

    @GetMapping("/getEstimatedDeliveryTime")
    public Result<LocalDateTime> getEstimatedDeliveryTime(Long merchantId, Long shopId, String customerAddress) {
        return Result.success(orderService.getEstimatedDeliveryTime(merchantId != null ? merchantId : shopId, customerAddress));
    }

    @GetMapping("/orderDetail/{id}")
    public Result<OrderVO> details(@PathVariable Long id) {
        return Result.success(orderService.detail(id));
    }

    @PutMapping("/cancel/{id}")
    public Result<String> cancel(@PathVariable Long id) throws Exception {
        orderService.cancelOrderById(id);
        return Result.success();
    }

    @PostMapping("/repetition/{id}")
    public Result<String> repetition(@PathVariable Long id) {
        orderService.repetition(id);
        return Result.success();
    }

    @GetMapping("/reminder/{id}")
    public Result<String> reminder(@PathVariable Long id) {
        orderService.reminder(id);
        return Result.success();
    }
}
