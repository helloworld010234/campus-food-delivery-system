package com.sky.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class EmployeeDTO implements Serializable {

    private Long id;

    private Long merchantId;

    private Integer accountType;

    private String username;

    private String name;

    private String phone;

    private String sex;

    private String idNumber;

    private Integer status;
}
