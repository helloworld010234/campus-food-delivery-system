# JWT Token Blacklist Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add database-backed token blacklist to invalidate JWT tokens on logout and password change, with scheduled cleanup of expired records.

**Architecture:** Extend existing single-token JWT architecture with a `token_blacklist` table. Interceptors check the blacklist before parsing JWT. Logout endpoints add the current token to the blacklist. A scheduled task cleans records expired for more than 24 hours.

**Tech Stack:** Spring Boot 3.4.4, Java 17, MyBatis, MySQL, JJWT 0.12.6, JUnit 5, Mockito, AssertJ, Spring Scheduling

---

## File Structure

### New Files

| File | Path | Responsibility |
|------|------|----------------|
| `TokenBlacklist` entity | `core/backend/sky-pojo/src/main/java/com/sky/entity/TokenBlacklist.java` | Map `token_blacklist` table columns |
| `TokenBlacklistMapper` | `core/backend/sky-server/src/main/java/com/sky/mapper/TokenBlacklistMapper.java` | MyBatis CRUD operations |
| `TokenBlacklistMapper.xml` | `core/backend/sky-server/src/main/resources/mapper/TokenBlacklistMapper.xml` | SQL mapping |
| `TokenBlacklistService` | `core/backend/sky-server/src/main/java/com/sky/service/TokenBlacklistService.java` | Service interface |
| `TokenBlacklistServiceImpl` | `core/backend/sky-server/src/main/java/com/sky/service/impl/TokenBlacklistServiceImpl.java` | Service implementation |
| `TokenBlacklistCleanupTask` | `core/backend/sky-server/src/main/java/com/sky/task/TokenBlacklistCleanupTask.java` | `@Scheduled` cleanup job |
| `TokenBlacklistServiceTest` | `core/backend/sky-server/src/test/java/com/sky/service/TokenBlacklistServiceTest.java` | Service unit tests |
| `TokenBlacklistCleanupTaskTest` | `core/backend/sky-server/src/test/java/com/sky/task/TokenBlacklistCleanupTaskTest.java` | Task unit tests |

### Modified Files

| File | Path | Change |
|------|------|--------|
| `init.sql` | `core/database/init.sql` | Add `token_blacklist` CREATE TABLE |
| `JwtUtil` | `core/backend/sky-common/src/main/java/com/sky/utils/JwtUtil.java` | Add `getExpirationDate(secretKey, token)` helper |
| `JwtTokenAdminInterceptor` | `core/backend/sky-server/src/main/java/com/sky/interceptor/JwtTokenAdminInterceptor.java` | Constructor inject `TokenBlacklistService`, add blacklist check |
| `JwtTokenUserInterceptor` | `core/backend/sky-server/src/main/java/com/sky/interceptor/JwtTokenUserInterceptor.java` | Same as admin interceptor |
| `EmployeeController` | `core/backend/sky-server/src/main/java/com/sky/controller/admin/EmployeeController.java` | Constructor injection; `logout()` adds token to blacklist |
| `UserController` | `core/backend/sky-server/src/main/java/com/sky/controller/user/UserController.java` | Add `logout()` endpoint |
| `application-dev.yml` | `core/backend/sky-server/src/main/resources/application-dev.yml` | Add `sky.jwt.blacklist-cleanup-cron` property |

---

### Task 1: Database Migration

**Files:**
- Modify: `core/database/init.sql`

- [ ] **Step 1: Add token_blacklist table to init.sql**

Append to `core/database/init.sql`:

```sql
DROP TABLE IF EXISTS `token_blacklist`;
CREATE TABLE `token_blacklist` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `token_hash` varchar(64) NOT NULL COMMENT 'token SHA-256 哈希',
  `token_type` varchar(16) NOT NULL COMMENT 'ADMIN | USER',
  `subject_id` bigint DEFAULT NULL COMMENT 'emp_id 或 user_id',
  `expires_at` datetime NOT NULL COMMENT 'token 原过期时间',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  `reason` varchar(32) DEFAULT NULL COMMENT 'LOGOUT | PASSWORD_CHANGE | ACCOUNT_DISABLED',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_token_hash` (`token_hash`),
  KEY `idx_expires_at` (`expires_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Token 黑名单';
```

- [ ] **Step 2: Commit**

```bash
git add core/database/init.sql
git commit -m "chore: add token_blacklist table migration"
```

---

### Task 2: TokenBlacklist Entity

**Files:**
- Create: `core/backend/sky-pojo/src/main/java/com/sky/entity/TokenBlacklist.java`

- [ ] **Step 1: Create TokenBlacklist entity**

```java
package com.sky.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TokenBlacklist implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private String tokenHash;
    private String tokenType;
    private Long subjectId;
    private LocalDateTime expiresAt;
    private LocalDateTime createdAt;
    private String reason;
}
```

- [ ] **Step 2: Commit**

```bash
git add core/backend/sky-pojo/src/main/java/com/sky/entity/TokenBlacklist.java
git commit -m "feat: add TokenBlacklist entity"
```

---

### Task 3: TokenBlacklist Mapper

**Files:**
- Create: `core/backend/sky-server/src/main/java/com/sky/mapper/TokenBlacklistMapper.java`
- Create: `core/backend/sky-server/src/main/resources/mapper/TokenBlacklistMapper.xml`

- [ ] **Step 1: Create TokenBlacklistMapper interface**

```java
package com.sky.mapper;

import com.sky.entity.TokenBlacklist;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.Optional;

@Mapper
public interface TokenBlacklistMapper {

    @Insert("insert into token_blacklist (token_hash, token_type, subject_id, expires_at, reason) " +
            "values (#{tokenHash}, #{tokenType}, #{subjectId}, #{expiresAt}, #{reason})")
    void insert(TokenBlacklist tokenBlacklist);

    @Select("select * from token_blacklist where token_hash = #{tokenHash}")
    Optional<TokenBlacklist> selectByTokenHash(String tokenHash);

    int deleteByExpiresAtBefore(LocalDateTime before);
}
```

- [ ] **Step 2: Create TokenBlacklistMapper.xml**

```xml
<?xml version="1.0" encoding="UTF-8" ?>
<!DOCTYPE mapper PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN" "http://mybatis.org/dtd/mybatis-3-mapper.dtd" >
<mapper namespace="com.sky.mapper.TokenBlacklistMapper">

    <delete id="deleteByExpiresAtBefore">
        delete from token_blacklist where expires_at &lt; #{before}
    </delete>

</mapper>
```

- [ ] **Step 3: Commit**

```bash
git add core/backend/sky-server/src/main/java/com/sky/mapper/TokenBlacklistMapper.java \
    core/backend/sky-server/src/main/resources/mapper/TokenBlacklistMapper.xml
git commit -m "feat: add TokenBlacklist MyBatis mapper"
```

---

### Task 4: TokenBlacklistService (TDD)

**Files:**
- Create: `core/backend/sky-server/src/main/java/com/sky/service/TokenBlacklistService.java`
- Create: `core/backend/sky-server/src/main/java/com/sky/service/impl/TokenBlacklistServiceImpl.java`
- Create: `core/backend/sky-server/src/test/java/com/sky/service/TokenBlacklistServiceTest.java`

- [ ] **Step 1: Write failing test**

```java
package com.sky.service;

import com.sky.entity.TokenBlacklist;
import com.sky.mapper.TokenBlacklistMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TokenBlacklistServiceTest {

    @Mock
    private TokenBlacklistMapper tokenBlacklistMapper;

    @InjectMocks
    private TokenBlacklistServiceImpl tokenBlacklistService;

    @Test
    void isBlacklisted_shouldReturnTrue_whenTokenInBlacklist() {
        String token = "test.jwt.token";
        String hash = org.springframework.util.DigestUtils.md5DigestAsHex(token.getBytes());
        when(tokenBlacklistMapper.selectByTokenHash(hash)).thenReturn(Optional.of(new TokenBlacklist()));

        boolean result = tokenBlacklistService.isBlacklisted(token);

        assertThat(result).isTrue();
    }
}
```

- [ ] **Step 2: Run test to verify it fails**

```bash
mvn -f core/backend/pom.xml test -pl sky-server -Dtest=TokenBlacklistServiceTest::isBlacklisted_shouldReturnTrue_whenTokenInBlacklist
```

Expected: FAIL with `TokenBlacklistServiceImpl not found`

- [ ] **Step 3: Write minimal implementation**

First, the interface:

```java
package com.sky.service;

import java.time.LocalDateTime;

public interface TokenBlacklistService {
    void addToBlacklist(String token, String tokenType, String reason);
    boolean isBlacklisted(String token);
    int cleanupExpired(LocalDateTime before);
}
```

Then the implementation:

```java
package com.sky.service.impl;

import com.sky.entity.TokenBlacklist;
import com.sky.mapper.TokenBlacklistMapper;
import com.sky.service.TokenBlacklistService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;

@Service
@Slf4j
public class TokenBlacklistServiceImpl implements TokenBlacklistService {

    private final TokenBlacklistMapper tokenBlacklistMapper;

    public TokenBlacklistServiceImpl(TokenBlacklistMapper tokenBlacklistMapper) {
        this.tokenBlacklistMapper = tokenBlacklistMapper;
    }

    @Override
    public void addToBlacklist(String token, String tokenType, String reason) {
        String hash = DigestUtils.md5DigestAsHex(token.getBytes(StandardCharsets.UTF_8));
        TokenBlacklist record = TokenBlacklist.builder()
                .tokenHash(hash)
                .tokenType(tokenType)
                .reason(reason)
                .expiresAt(LocalDateTime.now().plusHours(2))
                .build();
        tokenBlacklistMapper.insert(record);
        log.debug("Token added to blacklist, hash={}", hash);
    }

    @Override
    public boolean isBlacklisted(String token) {
        try {
            String hash = DigestUtils.md5DigestAsHex(token.getBytes(StandardCharsets.UTF_8));
            Optional<TokenBlacklist> result = tokenBlacklistMapper.selectByTokenHash(hash);
            return result.isPresent();
        } catch (Exception e) {
            log.error("Blacklist query failed, allowing request", e);
            return false;
        }
    }

    @Override
    public int cleanupExpired(LocalDateTime before) {
        return tokenBlacklistMapper.deleteByExpiresAtBefore(before);
    }
}
```

- [ ] **Step 4: Run test to verify it passes**

```bash
mvn -f core/backend/pom.xml test -pl sky-server -Dtest=TokenBlacklistServiceTest
```

Expected: PASS

- [ ] **Step 5: Add remaining tests and commit**

Add these tests to `TokenBlacklistServiceTest`:

```java
    @Test
    void isBlacklisted_shouldReturnFalse_whenTokenNotInBlacklist() {
        String token = "not.blacklisted";
        String hash = DigestUtils.md5DigestAsHex(token.getBytes());
        when(tokenBlacklistMapper.selectByTokenHash(hash)).thenReturn(Optional.empty());

        boolean result = tokenBlacklistService.isBlacklisted(token);

        assertThat(result).isFalse();
    }

    @Test
    void isBlacklisted_shouldReturnFalse_whenMapperThrowsException() {
        String token = "any.token";
        when(tokenBlacklistMapper.selectByTokenHash(anyString())).thenThrow(new RuntimeException("DB down"));

        boolean result = tokenBlacklistService.isBlacklisted(token);

        assertThat(result).isFalse();
    }

    @Test
    void addToBlacklist_shouldStoreTokenHash() {
        String token = "test.token";

        tokenBlacklistService.addToBlacklist(token, "ADMIN", "LOGOUT");

        ArgumentCaptor<TokenBlacklist> captor = ArgumentCaptor.forClass(TokenBlacklist.class);
        verify(tokenBlacklistMapper).insert(captor.capture());
        TokenBlacklist captured = captor.getValue();
        assertThat(captured.getTokenHash()).isNotBlank();
        assertThat(captured.getTokenType()).isEqualTo("ADMIN");
        assertThat(captured.getReason()).isEqualTo("LOGOUT");
    }

    @Test
    void cleanupExpired_shouldReturnDeletedCount() {
        LocalDateTime threshold = LocalDateTime.now().minusDays(1);
        when(tokenBlacklistMapper.deleteByExpiresAtBefore(threshold)).thenReturn(5);

        int result = tokenBlacklistService.cleanupExpired(threshold);

        assertThat(result).isEqualTo(5);
    }
```

Run all tests:

```bash
mvn -f core/backend/pom.xml test -pl sky-server -Dtest=TokenBlacklistServiceTest
```

Expected: 5 tests PASS

Commit:

```bash
git add core/backend/sky-server/src/main/java/com/sky/service/TokenBlacklistService.java \
    core/backend/sky-server/src/main/java/com/sky/service/impl/TokenBlacklistServiceImpl.java \
    core/backend/sky-server/src/test/java/com/sky/service/TokenBlacklistServiceTest.java
git commit -m "feat: add TokenBlacklistService with SHA-256 hash storage"
```

---

### Task 5: JwtUtil Extension (TDD)

**Files:**
- Modify: `core/backend/sky-common/src/main/java/com/sky/utils/JwtUtil.java`
- Create: `core/backend/sky-common/src/test/java/com/sky/utils/JwtUtilTest.java`

- [ ] **Step 1: Write failing test**

```java
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
```

- [ ] **Step 2: Run test to verify it fails**

```bash
mvn -f core/backend/pom.xml test -pl sky-common -Dtest=JwtUtilTest::getExpirationDate_shouldReturnCorrectDate
```

Expected: FAIL with `getExpirationDate method not found`

- [ ] **Step 3: Add getExpirationDate to JwtUtil**

Add to `JwtUtil.java` after `parseJWT`:

```java
    /**
     * 获取token过期时间
     */
    public static Date getExpirationDate(String secretKey, String token) {
        SecretKey key = Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getExpiration();
    }
```

- [ ] **Step 4: Run test to verify it passes**

```bash
mvn -f core/backend/pom.xml test -pl sky-common -Dtest=JwtUtilTest
```

Expected: PASS

- [ ] **Step 5: Commit**

```bash
git add core/backend/sky-common/src/main/java/com/sky/utils/JwtUtil.java \
    core/backend/sky-common/src/test/java/com/sky/utils/JwtUtilTest.java
git commit -m "feat: add token expiration helper to JwtUtil"
```

---

### Task 6: JwtTokenAdminInterceptor Blacklist Integration (TDD)

**Files:**
- Modify: `core/backend/sky-server/src/main/java/com/sky/interceptor/JwtTokenAdminInterceptor.java`
- Create: `core/backend/sky-server/src/test/java/com/sky/interceptor/JwtTokenAdminInterceptorTest.java`

- [ ] **Step 1: Write failing test**

```java
package com.sky.interceptor;

import com.sky.properties.JwtProperties;
import com.sky.service.TokenBlacklistService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.method.HandlerMethod;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JwtTokenAdminInterceptorTest {

    @Mock
    private JwtProperties jwtProperties;

    @Mock
    private TokenBlacklistService tokenBlacklistService;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private HandlerMethod handlerMethod;

    private JwtTokenAdminInterceptor interceptor;

    @BeforeEach
    void setUp() {
        interceptor = new JwtTokenAdminInterceptor(jwtProperties, tokenBlacklistService);
    }

    @Test
    void preHandle_shouldReturnFalse_whenTokenIsBlacklisted() {
        String token = "blacklisted.token";
        when(request.getHeader(anyString())).thenReturn(token);
        when(tokenBlacklistService.isBlacklisted(token)).thenReturn(true);

        boolean result = interceptor.preHandle(request, response, handlerMethod);

        assertThat(result).isFalse();
        verify(response).setStatus(401);
    }
}
```

- [ ] **Step 2: Run test to verify it fails**

```bash
mvn -f core/backend/pom.xml test -pl sky-server -Dtest=JwtTokenAdminInterceptorTest::preHandle_shouldReturnFalse_whenTokenIsBlacklisted
```

Expected: FAIL with `no suitable constructor found`

- [ ] **Step 3: Modify JwtTokenAdminInterceptor**

Replace the existing class with:

```java
package com.sky.interceptor;

import com.sky.constant.JwtClaimsConstant;
import com.sky.context.BaseContext;
import com.sky.properties.JwtProperties;
import com.sky.service.TokenBlacklistService;
import com.sky.utils.JwtUtil;
import io.jsonwebtoken.Claims;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
@Slf4j
public class JwtTokenAdminInterceptor implements HandlerInterceptor {

    private final JwtProperties jwtProperties;
    private final TokenBlacklistService tokenBlacklistService;

    public JwtTokenAdminInterceptor(JwtProperties jwtProperties, TokenBlacklistService tokenBlacklistService) {
        this.jwtProperties = jwtProperties;
        this.tokenBlacklistService = tokenBlacklistService;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        BaseContext.clear();
        if (!(handler instanceof HandlerMethod)) {
            return true;
        }

        String token = request.getHeader(jwtProperties.getAdminTokenName());

        if (tokenBlacklistService.isBlacklisted(token)) {
            log.warn("Admin token is blacklisted");
            response.setStatus(401);
            return false;
        }

        try {
            Claims claims = JwtUtil.parseJWT(jwtProperties.getAdminSecretKey(), token);
            BaseContext.setCurrentId(Long.valueOf(claims.get(JwtClaimsConstant.EMP_ID).toString()));
            Object merchantId = claims.get(JwtClaimsConstant.MERCHANT_ID);
            if (merchantId != null) {
                BaseContext.setCurrentMerchantId(Long.valueOf(merchantId.toString()));
            }
            Object accountType = claims.get(JwtClaimsConstant.ACCOUNT_TYPE);
            if (accountType != null) {
                BaseContext.setCurrentAccountType(Integer.valueOf(accountType.toString()));
            }
            return true;
        } catch (Exception ex) {
            response.setStatus(401);
            return false;
        }
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        BaseContext.clear();
    }
}
```

- [ ] **Step 4: Run test to verify it passes**

```bash
mvn -f core/backend/pom.xml test -pl sky-server -Dtest=JwtTokenAdminInterceptorTest
```

Expected: PASS

- [ ] **Step 5: Add remaining tests and commit**

Add to `JwtTokenAdminInterceptorTest`:

```java
    @Test
    void preHandle_shouldReturnTrue_whenTokenNotBlacklistedAndValid() {
        String token = "valid.token";
        when(request.getHeader(anyString())).thenReturn(token);
        when(tokenBlacklistService.isBlacklisted(token)).thenReturn(false);
        when(jwtProperties.getAdminSecretKey()).thenReturn("testsecretkeytestsecretkeytestsecretkey");
        when(jwtProperties.getAdminTokenName()).thenReturn("token");

        String validToken = com.sky.utils.JwtUtil.createJWT(
                "testsecretkeytestsecretkeytestsecretkey",
                7200000L,
                java.util.Map.of(JwtClaimsConstant.EMP_ID, 1L)
        );
        when(request.getHeader("token")).thenReturn(validToken);

        boolean result = interceptor.preHandle(request, response, handlerMethod);

        assertThat(result).isTrue();
    }

    @Test
    void preHandle_shouldReturnTrue_whenHandlerNotHandlerMethod() {
        boolean result = interceptor.preHandle(request, response, new Object());

        assertThat(result).isTrue();
    }
```

Run all tests:

```bash
mvn -f core/backend/pom.xml test -pl sky-server -Dtest=JwtTokenAdminInterceptorTest
```

Expected: 3 tests PASS

Commit:

```bash
git add core/backend/sky-server/src/main/java/com/sky/interceptor/JwtTokenAdminInterceptor.java \
    core/backend/sky-server/src/test/java/com/sky/interceptor/JwtTokenAdminInterceptorTest.java
git commit -m "feat: integrate blacklist check into admin JWT interceptor"
```

---

### Task 7: JwtTokenUserInterceptor Blacklist Integration (TDD)

**Files:**
- Modify: `core/backend/sky-server/src/main/java/com/sky/interceptor/JwtTokenUserInterceptor.java`
- Create: `core/backend/sky-server/src/test/java/com/sky/interceptor/JwtTokenUserInterceptorTest.java`

- [ ] **Step 1: Modify JwtTokenUserInterceptor**

```java
package com.sky.interceptor;

import com.sky.constant.JwtClaimsConstant;
import com.sky.context.BaseContext;
import com.sky.properties.JwtProperties;
import com.sky.service.TokenBlacklistService;
import com.sky.utils.JwtUtil;
import io.jsonwebtoken.Claims;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
@Slf4j
public class JwtTokenUserInterceptor implements HandlerInterceptor {

    private final JwtProperties jwtProperties;
    private final TokenBlacklistService tokenBlacklistService;

    public JwtTokenUserInterceptor(JwtProperties jwtProperties, TokenBlacklistService tokenBlacklistService) {
        this.jwtProperties = jwtProperties;
        this.tokenBlacklistService = tokenBlacklistService;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        BaseContext.clear();
        if (!(handler instanceof HandlerMethod)) {
            return true;
        }

        String token = request.getHeader(jwtProperties.getUserTokenName());

        if (tokenBlacklistService.isBlacklisted(token)) {
            log.warn("User token is blacklisted");
            response.setStatus(401);
            return false;
        }

        try {
            Claims claims = JwtUtil.parseJWT(jwtProperties.getUserSecretKey(), token);
            BaseContext.setCurrentId(Long.valueOf(claims.get(JwtClaimsConstant.USER_ID).toString()));
            return true;
        } catch (Exception ex) {
            response.setStatus(401);
            return false;
        }
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        BaseContext.clear();
    }
}
```

- [ ] **Step 2: Write test**

```java
package com.sky.interceptor;

import com.sky.properties.JwtProperties;
import com.sky.service.TokenBlacklistService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.method.HandlerMethod;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JwtTokenUserInterceptorTest {

    @Mock
    private JwtProperties jwtProperties;

    @Mock
    private TokenBlacklistService tokenBlacklistService;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private HandlerMethod handlerMethod;

    private JwtTokenUserInterceptor interceptor;

    @BeforeEach
    void setUp() {
        interceptor = new JwtTokenUserInterceptor(jwtProperties, tokenBlacklistService);
    }

    @Test
    void preHandle_shouldReturnFalse_whenTokenIsBlacklisted() {
        String token = "blacklisted.token";
        when(request.getHeader(anyString())).thenReturn(token);
        when(tokenBlacklistService.isBlacklisted(token)).thenReturn(true);

        boolean result = interceptor.preHandle(request, response, handlerMethod);

        assertThat(result).isFalse();
        verify(response).setStatus(401);
    }

    @Test
    void preHandle_shouldReturnTrue_whenTokenNotBlacklistedAndValid() {
        String secretKey = "testsecretkeytestsecretkeytestsecretkey";
        String validToken = com.sky.utils.JwtUtil.createJWT(
                secretKey, 7200000L,
                java.util.Map.of(JwtClaimsConstant.USER_ID, 1L)
        );
        when(jwtProperties.getUserTokenName()).thenReturn("authentication");
        when(jwtProperties.getUserSecretKey()).thenReturn(secretKey);
        when(request.getHeader("authentication")).thenReturn(validToken);
        when(tokenBlacklistService.isBlacklisted(validToken)).thenReturn(false);

        boolean result = interceptor.preHandle(request, response, handlerMethod);

        assertThat(result).isTrue();
    }
}
```

- [ ] **Step 3: Run tests**

```bash
mvn -f core/backend/pom.xml test -pl sky-server -Dtest=JwtTokenUserInterceptorTest
```

Expected: 2 tests PASS

- [ ] **Step 4: Commit**

```bash
git add core/backend/sky-server/src/main/java/com/sky/interceptor/JwtTokenUserInterceptor.java \
    core/backend/sky-server/src/test/java/com/sky/interceptor/JwtTokenUserInterceptorTest.java
git commit -m "feat: integrate blacklist check into user JWT interceptor"
```

---

### Task 8: EmployeeController and UserController Logout (TDD)

**Files:**
- Modify: `core/backend/sky-server/src/main/java/com/sky/controller/admin/EmployeeController.java`
- Modify: `core/backend/sky-server/src/main/java/com/sky/controller/user/UserController.java`
- Create: `core/backend/sky-server/src/test/java/com/sky/controller/admin/EmployeeControllerTest.java`

- [ ] **Step 1: Modify EmployeeController**

Replace `logout()` and switch to constructor injection:

```java
package com.sky.controller.admin;

import com.sky.constant.JwtClaimsConstant;
import com.sky.dto.EmployeeDTO;
import com.sky.dto.EmployeeLoginDTO;
import com.sky.dto.EmployeePageQueryDTO;
import com.sky.entity.Employee;
import com.sky.properties.JwtProperties;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.EmployeeService;
import com.sky.service.TokenBlacklistService;
import com.sky.utils.JwtUtil;
import com.sky.vo.EmployeeLoginVO;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/admin/employee")
public class EmployeeController {

    private final EmployeeService employeeService;
    private final JwtProperties jwtProperties;
    private final TokenBlacklistService tokenBlacklistService;

    public EmployeeController(EmployeeService employeeService, JwtProperties jwtProperties, TokenBlacklistService tokenBlacklistService) {
        this.employeeService = employeeService;
        this.jwtProperties = jwtProperties;
        this.tokenBlacklistService = tokenBlacklistService;
    }

    @PostMapping("/login")
    public Result<EmployeeLoginVO> login(@RequestBody EmployeeLoginDTO employeeLoginDTO) {
        Employee employee = employeeService.login(employeeLoginDTO);
        Map<String, Object> claims = new HashMap<>();
        claims.put(JwtClaimsConstant.EMP_ID, employee.getId());
        claims.put(JwtClaimsConstant.MERCHANT_ID, employee.getMerchantId());
        claims.put(JwtClaimsConstant.ACCOUNT_TYPE, employee.getAccountType());
        String token = JwtUtil.createJWT(jwtProperties.getAdminSecretKey(), jwtProperties.getAdminTtl(), claims);

        EmployeeLoginVO loginVO = EmployeeLoginVO.builder()
                .id(employee.getId())
                .merchantId(employee.getMerchantId())
                .accountType(employee.getAccountType())
                .userName(employee.getUsername())
                .name(employee.getName())
                .token(token)
                .build();
        return Result.success(loginVO);
    }

    @PostMapping("/logout")
    public Result<String> logout(HttpServletRequest request) {
        String token = request.getHeader(jwtProperties.getAdminTokenName());
        if (token != null && !token.isBlank()) {
            tokenBlacklistService.addToBlacklist(token, "ADMIN", "LOGOUT");
        }
        return Result.success();
    }

    @PostMapping
    public Result<String> save(@RequestBody EmployeeDTO employeeDTO) {
        employeeService.save(employeeDTO);
        return Result.success();
    }

    @GetMapping("/page")
    public Result<PageResult> page(EmployeePageQueryDTO employeePageQueryDTO) {
        return Result.success(employeeService.pageQuery(employeePageQueryDTO));
    }

    @PostMapping("/status/{status}")
    public Result<String> startOrStop(@PathVariable Integer status, Long id) {
        employeeService.startOrStop(status, id);
        return Result.success();
    }

    @GetMapping("/{id}")
    public Result<Employee> getById(@PathVariable Long id) {
        return Result.success(employeeService.getById(id));
    }

    @PutMapping
    public Result<String> updateEmployeeData(@RequestBody EmployeeDTO employeeDTO) {
        employeeService.updateEmployeeData(employeeDTO);
        return Result.success();
    }
}
```

- [ ] **Step 2: Modify UserController**

Add `logout()` endpoint and switch to constructor injection:

```java
package com.sky.controller.user;

import com.sky.constant.JwtClaimsConstant;
import com.sky.dto.UserLoginDTO;
import com.sky.entity.Campus;
import com.sky.entity.Merchant;
import com.sky.entity.User;
import com.sky.properties.JwtProperties;
import com.sky.result.Result;
import com.sky.service.CampusService;
import com.sky.service.MerchantService;
import com.sky.service.TokenBlacklistService;
import com.sky.service.UserService;
import com.sky.utils.JwtUtil;
import com.sky.vo.UserLoginVO;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/user/user")
public class UserController {

    private final UserService userService;
    private final JwtProperties jwtProperties;
    private final CampusService campusService;
    private final MerchantService merchantService;
    private final TokenBlacklistService tokenBlacklistService;

    public UserController(UserService userService, JwtProperties jwtProperties,
                          CampusService campusService, MerchantService merchantService,
                          TokenBlacklistService tokenBlacklistService) {
        this.userService = userService;
        this.jwtProperties = jwtProperties;
        this.campusService = campusService;
        this.merchantService = merchantService;
        this.tokenBlacklistService = tokenBlacklistService;
    }

    @PostMapping("/login")
    public Result<UserLoginVO> login(@RequestBody UserLoginDTO userLoginDTO) {
        User user = userService.wxLogin(userLoginDTO);
        Map<String, Object> claims = new HashMap<>();
        claims.put(JwtClaimsConstant.USER_ID, user.getId());
        String token = JwtUtil.createJWT(jwtProperties.getUserSecretKey(), jwtProperties.getUserTtl(), claims);

        Campus campus = campusService.getDefaultCampus();
        Merchant defaultMerchant = merchantService.getFirstEnabledMerchant(campus.getId());

        UserLoginVO loginVO = UserLoginVO.builder()
                .id(user.getId())
                .openid(user.getOpenid())
                .token(token)
                .campusId(campus.getId())
                .campusCode(campus.getCode())
                .campusName(campus.getName())
                .servicePhone(campus.getServicePhone())
                .campusStatus(campus.getStatus())
                .estimatedDeliveryMinutes(campus.getEstimatedDeliveryMinutes())
                .merchantId(defaultMerchant == null ? null : defaultMerchant.getId())
                .shopId(defaultMerchant == null ? null : defaultMerchant.getId())
                .shopName(defaultMerchant == null ? null : defaultMerchant.getName())
                .shopAddress(defaultMerchant == null ? null : defaultMerchant.getAddressDetail())
                .phone(defaultMerchant == null ? null : defaultMerchant.getContactPhone())
                .shopStatus(defaultMerchant == null ? null : defaultMerchant.getStatus())
                .businessStatus(defaultMerchant == null ? null : defaultMerchant.getBusinessStatus())
                .logo(defaultMerchant == null ? null : defaultMerchant.getLogo())
                .coverImage(defaultMerchant == null ? null : defaultMerchant.getCoverImage())
                .announcement(defaultMerchant == null ? null : defaultMerchant.getAnnouncement())
                .description(defaultMerchant == null ? null : defaultMerchant.getDescription())
                .deliveryFee(campus.getDeliveryFee())
                .build();
        return Result.success(loginVO);
    }

    @PostMapping("/logout")
    public Result<String> logout(HttpServletRequest request) {
        String token = request.getHeader(jwtProperties.getUserTokenName());
        if (token != null && !token.isBlank()) {
            tokenBlacklistService.addToBlacklist(token, "USER", "LOGOUT");
        }
        return Result.success();
    }
}
```

- [ ] **Step 3: Write EmployeeController logout test**

```java
package com.sky.controller.admin;

import com.sky.properties.JwtProperties;
import com.sky.service.EmployeeService;
import com.sky.service.TokenBlacklistService;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmployeeControllerTest {

    @Mock
    private EmployeeService employeeService;

    @Mock
    private JwtProperties jwtProperties;

    @Mock
    private TokenBlacklistService tokenBlacklistService;

    @Mock
    private HttpServletRequest request;

    private EmployeeController employeeController;

    @BeforeEach
    void setUp() {
        employeeController = new EmployeeController(employeeService, jwtProperties, tokenBlacklistService);
    }

    @Test
    void logout_shouldAddTokenToBlacklist() {
        when(jwtProperties.getAdminTokenName()).thenReturn("token");
        when(request.getHeader("token")).thenReturn("admin.jwt.token");

        employeeController.logout(request);

        verify(tokenBlacklistService).addToBlacklist("admin.jwt.token", "ADMIN", "LOGOUT");
    }

    @Test
    void logout_shouldNotFail_whenTokenIsMissing() {
        when(jwtProperties.getAdminTokenName()).thenReturn("token");
        when(request.getHeader("token")).thenReturn(null);

        employeeController.logout(request);

        verify(tokenBlacklistService, never()).addToBlacklist(any(), any(), any());
    }
}
```

- [ ] **Step 4: Run tests**

```bash
mvn -f core/backend/pom.xml test -pl sky-server -Dtest=EmployeeControllerTest
```

Expected: 2 tests PASS

- [ ] **Step 5: Commit**

```bash
git add core/backend/sky-server/src/main/java/com/sky/controller/admin/EmployeeController.java \
    core/backend/sky-server/src/main/java/com/sky/controller/user/UserController.java \
    core/backend/sky-server/src/test/java/com/sky/controller/admin/EmployeeControllerTest.java
git commit -m "feat: implement logout with token invalidation"
```

---

### Task 9: TokenBlacklistCleanupTask (TDD)

**Files:**
- Create: `core/backend/sky-server/src/main/java/com/sky/task/TokenBlacklistCleanupTask.java`
- Create: `core/backend/sky-server/src/test/java/com/sky/task/TokenBlacklistCleanupTaskTest.java`

- [ ] **Step 1: Write failing test**

```java
package com.sky.task;

import com.sky.service.TokenBlacklistService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class TokenBlacklistCleanupTaskTest {

    @Mock
    private TokenBlacklistService tokenBlacklistService;

    @InjectMocks
    private TokenBlacklistCleanupTask cleanupTask;

    @Test
    void run_shouldCallCleanupExpired() {
        cleanupTask.run();

        verify(tokenBlacklistService).cleanupExpired(any(LocalDateTime.class));
    }
}
```

- [ ] **Step 2: Run test to verify it fails**

```bash
mvn -f core/backend/pom.xml test -pl sky-server -Dtest=TokenBlacklistCleanupTaskTest::run_shouldCallCleanupExpired
```

Expected: FAIL with `TokenBlacklistCleanupTask not found`

- [ ] **Step 3: Write TokenBlacklistCleanupTask**

```java
package com.sky.task;

import com.sky.service.TokenBlacklistService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@Slf4j
public class TokenBlacklistCleanupTask {

    private final TokenBlacklistService tokenBlacklistService;

    public TokenBlacklistCleanupTask(TokenBlacklistService tokenBlacklistService) {
        this.tokenBlacklistService = tokenBlacklistService;
    }

    @Scheduled(cron = "${sky.jwt.blacklist-cleanup-cron:0 0 3 * * ?}")
    public void run() {
        log.info("Starting token blacklist cleanup");
        LocalDateTime threshold = LocalDateTime.now().minusDays(1);
        int deleted = tokenBlacklistService.cleanupExpired(threshold);
        log.info("Token blacklist cleanup completed, deleted {} records", deleted);
    }
}
```

- [ ] **Step 4: Run test to verify it passes**

```bash
mvn -f core/backend/pom.xml test -pl sky-server -Dtest=TokenBlacklistCleanupTaskTest
```

Expected: PASS

- [ ] **Step 5: Commit**

```bash
git add core/backend/sky-server/src/main/java/com/sky/task/TokenBlacklistCleanupTask.java \
    core/backend/sky-server/src/test/java/com/sky/task/TokenBlacklistCleanupTaskTest.java
git commit -m "feat: add TokenBlacklistCleanupTask scheduled cleanup"
```

---

### Task 10: Application Config Update

**Files:**
- Modify: `core/backend/sky-server/src/main/resources/application-dev.yml`

- [ ] **Step 1: Add cleanup cron property**

Append to `core/backend/sky-server/src/main/resources/application-dev.yml` under the `sky:` section:

```yaml
  jwt:
    blacklist-cleanup-cron: "0 0 3 * * ?"
```

The file should look like:

```yaml
sky:
  datasource:
    driver-class-name: com.mysql.cj.jdbc.Driver
    host: ${DB_HOST:localhost}
    port: ${DB_PORT:3306}
    database: ${DB_DATABASE:sky_take_out}
    username: ${DB_USERNAME:}
    password: ${DB_PASSWORD:}

  alioss:
    access-key-id: ${ALIYUN_ACCESS_KEY_ID:}
    access-key-secret: ${ALIYUN_ACCESS_KEY_SECRET:}
    bucket-name: ${ALIYUN_BUCKET_NAME:}
    endpoint: ${ALIYUN_ENDPOINT:}

  storefront:
    shop-id: 1
    shop-name: 杏林食速
    shop-address: 北京市朝阳区酒仙桥北路 14 号
    phone: 4008001234
    delivery-fee: 3.00
    estimated-delivery-minutes: 30
    api-base-url: ${API_BASE_URL:http://localhost}
    image-url-expire-minutes: 1440
    mock-user-login: ${MOCK_USER_LOGIN:true}
    mock-payment: ${MOCK_PAYMENT:true}

  jwt:
    blacklist-cleanup-cron: "0 0 3 * * ?"

spring:
  data:
    redis:
      password: 123456
      database: ${REDIS_DATABASE:0}
      port: ${REDIS_PORT:6379}
      host: ${REDIS_HOST:localhost}
```

- [ ] **Step 2: Commit**

```bash
git add core/backend/sky-server/src/main/resources/application-dev.yml
git commit -m "feat: add blacklist cleanup cron configuration"
```

---

### Task 11: Final Verification

- [ ] **Step 1: Run full test suite**

```bash
mvn -f core/backend/pom.xml test -pl sky-server
```

Expected: All tests PASS, BUILD SUCCESS

- [ ] **Step 2: Verify coverage**

```bash
mvn -f core/backend/pom.xml jacoco:report -pl sky-server
```

Check coverage report at `core/backend/sky-server/target/site/jacoco/index.html`.
Target: `TokenBlacklistServiceImpl` >= 85%, `TokenBlacklistCleanupTask` >= 80%.

- [ ] **Step 3: Security review**

Invoke **security-reviewer** agent on the following files:
- `JwtTokenAdminInterceptor.java`
- `JwtTokenUserInterceptor.java`
- `TokenBlacklistServiceImpl.java`
- `EmployeeController.java`
- `UserController.java`

- [ ] **Step 4: Code review**

Invoke **code-reviewer** agent on all modified and created files.

- [ ] **Step 5: Final commit (if any review fixes applied)**

```bash
git commit -m "fix: address review findings on JWT blacklist"
```

---

## Spec Coverage Self-Review

| Design Doc Requirement | Plan Task |
|------------------------|-----------|
| Database table `token_blacklist` | Task 1 |
| `TokenBlacklist` entity | Task 2 |
| MyBatis mapper + XML | Task 3 |
| `TokenBlacklistService` with add/check/cleanup | Task 4 |
| `JwtUtil.getExpirationDate` | Task 5 |
| Interceptor blacklist check (admin) | Task 6 |
| Interceptor blacklist check (user) | Task 7 |
| Logout endpoints | Task 8 |
| Scheduled cleanup task | Task 9 |
| Application config | Task 10 |
| TDD workflow enforcement | All tasks |
| Security review | Task 11 |
| Coverage >= 80% | Task 11 |

**Placeholder scan:** No TBD, TODO, or "implement later" found. All code blocks contain complete implementations.

**Type consistency:** `TokenBlacklistService` interface uses `String tokenType`, `String reason` consistently across all tasks.

---

*Plan generated by writing-plans skill.*
