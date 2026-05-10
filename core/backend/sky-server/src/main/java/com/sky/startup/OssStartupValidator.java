package com.sky.startup;

import com.sky.properties.AliOssProperties;
import com.sky.utils.AliOssUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class OssStartupValidator implements CommandLineRunner {

    private final AliOssUtil aliOssUtil;
    private final AliOssProperties aliOssProperties;

    @Override
    public void run(String... args) {
        if (!aliOssProperties.isStartupValidationEnabled()) {
            log.info("OSS startup validation is disabled");
            return;
        }

        log.info("Validating OSS connectivity...");
        boolean valid = aliOssUtil.validateConnection();
        if (!valid) {
            String message = String.format(
                    "OSS连接校验失败: endpoint=%s, bucket=%s. 请检查sky.alioss配置项（access-key-id, access-key-secret, endpoint, bucket-name）",
                    aliOssProperties.getEndpoint(),
                    aliOssProperties.getBucketName()
            );
            log.error(message);
            throw new IllegalStateException(message);
        }
        log.info("OSS connectivity validated successfully: bucket={}", aliOssProperties.getBucketName());
    }
}
