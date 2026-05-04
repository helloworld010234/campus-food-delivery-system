package com.sky.utils;

import org.junit.jupiter.api.Test;

import java.util.Date;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class JwtUtilTest {

    private static final String SECRET_KEY = "testsecretkeytestsecretkeytestsecretkey";
    private static final long TTL = 7200000L;

    @Test
    void getExpirationDate_shouldReturnCorrectDate() {
        String token = JwtUtil.createJWT(SECRET_KEY, TTL, Map.of("key", "value"));

        Date expirationDate = JwtUtil.getExpirationDate(SECRET_KEY, token);

        assertThat(expirationDate).isNotNull();
        assertThat(expirationDate.getTime()).isGreaterThan(System.currentTimeMillis());
    }
}
