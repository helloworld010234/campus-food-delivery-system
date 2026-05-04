package com.sky.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class OrdersSubmitDTO implements Serializable {

    private Long merchantId;

    /**
     * Legacy alias kept for backward compatibility with older clients.
     */
    private Long shopId;

    private Long addressBookId;

    private Integer payMethod;

    private String remark;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime estimatedDeliveryTime;

    private Integer deliveryStatus;

    private Integer tablewareNumber;

    private Integer tablewareStatus;

    private BigDecimal packAmount;

    private BigDecimal amount;
}
