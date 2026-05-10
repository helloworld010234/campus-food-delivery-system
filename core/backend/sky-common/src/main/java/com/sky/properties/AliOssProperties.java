package com.sky.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "sky.alioss")
@Data
public class AliOssProperties {

    private String endpoint;
    private String accessKeyId;
    private String accessKeySecret;
    private String bucketName;

    /** Connection timeout in milliseconds */
    private int connectionTimeoutMs = 10000;

    /** Socket read timeout in milliseconds */
    private int readTimeoutMs = 30000;

    /** Max HTTP connections */
    private int maxConnections = 200;

    /** Presigned URL expiration in minutes (default 7 days) */
    private int presignedUrlExpireMinutes = 10080;

    /** Whether to validate OSS connectivity on startup */
    private boolean startupValidationEnabled = true;

}
