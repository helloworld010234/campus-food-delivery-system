package com.sky.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class MerchantPageQueryDTO implements Serializable {

    private int page;

    private int pageSize;

    private Long campusId;

    private Integer status;

    private Integer businessStatus;

    private String name;
}
