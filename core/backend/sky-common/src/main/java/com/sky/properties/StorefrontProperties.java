package com.sky.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
@ConfigurationProperties(prefix = "sky.storefront")
@Data
public class StorefrontProperties {

    /**
     * 门店标识，主要用于兼容前端已有字段。
     */
    private Long shopId = 1L;

    /**
     * 门店名称。
     */
    private String shopName = "杏林食速";

    /**
     * 门店地址。
     */
    private String shopAddress = "";

    /**
     * 联系电话。
     */
    private String phone = "";

    /**
     * 配送费。
     */
    private BigDecimal deliveryFee = BigDecimal.ZERO;

    /**
     * 预计送达分钟数。
     */
    private Integer estimatedDeliveryMinutes = 30;

    /**
     * 后端对外访问地址，用于拼接小程序可直接访问的图片地址。
     */
    private String apiBaseUrl = "http://127.0.0.1:8080";

    private Integer imageUrlExpireMinutes = 1440;

    /**
     * 本地联调时是否开启微信登录 mock。
     */
    private Boolean mockUserLogin = Boolean.FALSE;

    /**
     * 本地联调时是否开启支付 mock。
     */
    private Boolean mockPayment = Boolean.FALSE;
}
