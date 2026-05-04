package com.sky.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MerchantVO implements Serializable {

    private Long id;

    private Long campusId;

    private String campusName;

    private Integer campusStatus;

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

    private BigDecimal deliveryFee;

    private Integer estimatedDeliveryMinutes;

    private String servicePhone;
}
