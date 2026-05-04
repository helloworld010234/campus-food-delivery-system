package com.sky.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "sky.multi-merchant")
@Data
public class MultiMerchantProperties {

    /**
     * 是否要求数据库必须已经完成多商户 schema 迁移。
     * false: 允许单店兼容模式启动
     * true: 未完成迁移时直接启动失败
     */
    private Boolean required = Boolean.FALSE;
}
