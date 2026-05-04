package com.sky.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserLoginVO implements Serializable {

    private Long id;

    private String openid;

    private String token;

    private Long campusId;

    private String campusCode;

    private String campusName;

    private String servicePhone;

    private Integer campusStatus;

    private Integer estimatedDeliveryMinutes;

    private Long merchantId;

    private Long shopId;

    private String shopName;

    private String shopAddress;

    private String phone;

    private Integer shopStatus;

    private Integer businessStatus;

    private String logo;

    private String coverImage;

    private String announcement;

    private String description;

    private BigDecimal deliveryFee;
}
