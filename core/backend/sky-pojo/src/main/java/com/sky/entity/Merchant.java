package com.sky.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Merchant implements Serializable {

    private static final long serialVersionUID = 1L;

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

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    private Long createUser;

    private Long updateUser;
}
