package com.sky.support;

import com.sky.properties.MultiMerchantProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class MultiMerchantStartupGuard implements ApplicationRunner {

    @Autowired
    private MultiMerchantSchemaSupport schemaSupport;

    @Autowired
    private MultiMerchantProperties multiMerchantProperties;

    @Override
    public void run(ApplicationArguments args) {
        if (schemaSupport.isCoreSchemaReady()) {
            log.info("Multi-merchant schema is ready: {}", schemaSupport.getReadinessSummary());
            return;
        }

        String message = "Multi-merchant schema is not fully ready: " + schemaSupport.getReadinessSummary();
        if (Boolean.TRUE.equals(multiMerchantProperties.getRequired())) {
            throw new IllegalStateException(message + ". Startup aborted because sky.multi-merchant.required=true");
        }

        log.warn("{}; falling back to single-store compatibility mode", message);
    }
}
