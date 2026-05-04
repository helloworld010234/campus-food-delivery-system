package com.sky.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class ShoppingCartDTO implements Serializable {

    private Long merchantId;

    /**
     * Legacy alias kept for backward compatibility with older clients.
     */
    private Long shopId;

    private Long dishId;

    private Long setmealId;

    private String dishFlavor;
}
