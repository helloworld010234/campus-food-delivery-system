package com.sky;

import org.junit.jupiter.api.Test;
import org.springframework.scheduling.annotation.EnableScheduling;

import static org.assertj.core.api.Assertions.assertThat;

class SkyApplicationIT {

    @Test
    void shouldHaveEnableSchedulingAnnotation() {
        assertThat(SkyApplication.class.isAnnotationPresent(EnableScheduling.class)).isTrue();
    }
}
