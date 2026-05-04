package com.sky.dto;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalTime;

@Data
public class MerchantDTO implements Serializable {

    private Long id;

    private Long campusId;

    private String merchantCode;

    private String name;

    private String logo;

    private String coverImage;

    private String description;

    private String announcement;

    private String contactName;

    private String contactPhone;

    private String addressDetail;

    private Integer sort;

    private Integer status;

    private Integer businessStatus;

    private LocalTime businessBeginTime;

    private LocalTime businessEndTime;
}
