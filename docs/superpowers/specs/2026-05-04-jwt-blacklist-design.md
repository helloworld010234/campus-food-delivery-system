# JWT Token Blacklist Implementation Design

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add database-backed token blacklist to invalidate JWT tokens on logout and password change, with scheduled cleanup of expired records.

**Architecture:** Extend existing single-token JWT architecture (2-hour TTL) with a `token_blacklist` table. Interceptors check the blacklist before parsing JWT. Logout endpoints add the current token to the blacklist. A scheduled task cleans records expired for more than 24 hours.

**Tech Stack:** Spring Boot 3.4.4, Java 17, MyBatis, MySQL, JJWT 0.12.6, JUnit 5, Mockito, AssertJ, Spring Scheduling

---

## Section 1: Problem Statement and Scope

### Current State
- `JwtUtil.createJWT` / `JwtUtil.parseJWT` only — no lifecycle management
- `JwtTokenAdminInterceptor` / `JwtTokenUserInterceptor` parse and validate tokens
- `EmployeeController.logout()` is a no-op (`return Result.success()`)
- No user logout endpoint exists
- Token TTL is fixed at 2 hours (`7200000ms`)

### Problems
1. **No logout invalidation** — after logout, the token remains valid until natural expiration
2. **No password-change invalidation** — changing a password does not invalidate existing sessions
3. **No account-disable invalidation** — disabling an employee account does not invalidate their active tokens

### Out of Scope
- Refresh token mechanism (retain single access token)
- Changing TTL from 2 hours
- Multi-device global logout (subject-level ban)
- Redis-based blacklist (use database per user preference)

### In Scope
- Database table `token_blacklist` with SHA-256 hashed tokens
- MyBatis mapper + entity + service layer
- Interceptor integration with blacklist check
- Logout endpoints that add tokens to blacklist
- Scheduled cleanup task
- Full unit test coverage (target >= 85%)

---

## Section 2: Component Design

### Files Created

| File | Path | Responsibility |
|------|------|----------------|
| `TokenBlacklist` entity | `sky-pojo/src/main/java/com/sky/entity/TokenBlacklist.java` | Map `token_blacklist` table columns |
| `TokenBlacklistMapper` | `sky-server/src/main/java/com/sky/mapper/TokenBlacklistMapper.java` | MyBatis CRUD operations |
| `TokenBlacklistMapper.xml` | `sky-server/src/main/resources/mapper/TokenBlacklistMapper.xml` | SQL mapping |
| `TokenBlacklistService` | `sky-server/src/main/java/com/sky/service/TokenBlacklistService.java` | Interface: add, check, cleanup |
| `TokenBlacklistServiceImpl` | `sky-server/src/main/java/com/sky/service/impl/TokenBlacklistServiceImpl.java` | Implementation |
| `TokenBlacklistCleanupTask` | `sky-server/src/main/java/com/sky/task/TokenBlacklistCleanupTask.java` | `@Scheduled` cleanup cron job |
| `TokenBlacklistServiceTest` | `sky-server/src/test/java/com/sky/service/TokenBlacklistServiceTest.java` | Unit tests |
| `TokenBlacklistCleanupTaskTest` | `sky-server/src/test/java/com/sky/task/TokenBlacklistCleanupTaskTest.java` | Unit tests |
| `JwtTokenAdminInterceptorTest` | `sky-server/src/test/java/com/sky/interceptor/JwtTokenAdminInterceptorTest.java` | Unit tests |
| `EmployeeControllerTest` updates | `sky-server/src/test/java/com/sky/controller/admin/EmployeeControllerTest.java` | Additional tests |

### Files Modified

| File | Path | Change |
|------|------|--------|
| `JwtUtil` | `sky-common/.../JwtUtil.java` | Add `getExpirationDate(secretKey, token)` helper |
| `JwtTokenAdminInterceptor` | `sky-server/.../JwtTokenAdminInterceptor.java` | Constructor inject `TokenBlacklistService`, add blacklist check before parseJWT |
| `JwtTokenUserInterceptor` | `sky-server/.../JwtTokenUserInterceptor.java` | Same as admin interceptor |
| `EmployeeController` | `sky-server/.../EmployeeController.java` | Constructor injection; `logout()` extracts token and calls blacklist service |
| `UserController` | `sky-server/.../UserController.java` | Add `logout()` endpoint with same pattern |
| `application-dev.yml` | `sky-server/src/main/resources/application-dev.yml` | Add `sky.jwt.blacklist-cleanup-cron` property |
| `init.sql` / migration | `core/database/` | Add `token_blacklist` CREATE TABLE |

### Table Schema

```sql
CREATE TABLE token_blacklist (
    id           BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
    token_hash   VARCHAR(64) NOT NULL COMMENT 'token SHA-256 哈希',
    token_type   VARCHAR(16) NOT NULL COMMENT 'ADMIN | USER',
    subject_id   BIGINT COMMENT 'emp_id 或 user_id',
    expires_at   DATETIME NOT NULL COMMENT 'token 原过期时间',
    created_at   DATETIME DEFAULT CURRENT_TIMESTAMP,
    reason       VARCHAR(32) COMMENT 'LOGOUT | PASSWORD_CHANGE | ACCOUNT_DISABLED',
    UNIQUE KEY uk_token_hash (token_hash),
    KEY idx_expires_at (expires_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Token 黑名单';
```

### Service Interface

```java
public interface TokenBlacklistService {
    void addToBlacklist(String token, TokenType type, String reason);
    boolean isBlacklisted(String token);
    int cleanupExpired(LocalDateTime before);
}
```

### Constructor Injection Pattern (MANDATORY)

Per `java/patterns.md` — all new components use constructor injection:

```java
@Service
public class TokenBlacklistServiceImpl implements TokenBlacklistService {
    private final TokenBlacklistMapper tokenBlacklistMapper;

    public TokenBlacklistServiceImpl(TokenBlacklistMapper tokenBlacklistMapper) {
        this.tokenBlacklistMapper = tokenBlacklistMapper;
    }
}
```

---

## Section 3: Data Flow

### Flow 1: Login (UNCHANGED)
```
Controller.login()
  → Service authenticate
  → JwtUtil.createJWT(secretKey, ttl, claims)
  → return Result.success(LoginVO)
```

### Flow 2: Request Authentication (MODIFIED)
```
HTTP Request
  → Interceptor.preHandle()
    1. Extract token from header
    2. TokenBlacklistService.isBlacklisted(token)
       → SHA-256(token)
       → Mapper.selectByTokenHash(hash)
       → if present: response.setStatus(401); return false
       → if Mapper throws: log.error + return false (graceful degradation)
    3. parseJWT (existing)
    4. BaseContext.setCurrentId() (existing)
    5. return true
```

### Flow 3: Logout (NEW)
```
POST /admin/employee/logout 或 POST /user/user/logout
  → Controller extracts token from header
  → TokenBlacklistService.addToBlacklist(token, type, "LOGOUT")
       → parseJWT to get expires_at (safe parse, swallow exceptions)
       → compute SHA-256 hash
       → Mapper.insert(record)
  → BaseContext.clear()
  → return Result.success()
```

**Security note:** Logout always returns `Result.success()` regardless of token validity — prevents token enumeration attacks.

### Flow 4: Scheduled Cleanup (NEW)
```
@Scheduled(cron = "${sky.jwt.blacklist-cleanup-cron:0 0 3 * * ?}")
TokenBlacklistCleanupTask.run()
  → TokenBlacklistService.cleanupExpired(LocalDateTime.now().minusDays(1))
    → Mapper.deleteByExpiresAtBefore(threshold)
```

---

## Section 4: Test Strategy

### TDD Workflow (MANDATORY)

Per `common/testing.md` and `java/testing.md`:
1. Write failing test (RED)
2. Run test — must fail
3. Write minimal implementation (GREEN)
4. Run test — must pass
5. Refactor
6. Verify coverage >= 80%

**tdd-guide agent must be triggered before each component's implementation.**

### Test Cases

| # | Component | Test | Type |
|---|-----------|------|------|
| 1 | `TokenBlacklistServiceImpl` | `addToBlacklist_shouldStoreTokenHash_withCorrectFields` | Unit |
| 2 | `TokenBlacklistServiceImpl` | `isBlacklisted_shouldReturnTrue_whenTokenInBlacklist` | Unit |
| 3 | `TokenBlacklistServiceImpl` | `isBlacklisted_shouldReturnFalse_whenTokenNotInBlacklist` | Unit |
| 4 | `TokenBlacklistServiceImpl` | `isBlacklisted_shouldReturnFalse_whenMapperThrowsException` | Unit |
| 5 | `TokenBlacklistServiceImpl` | `cleanupExpired_shouldReturnDeletedCount` | Unit |
| 6 | `JwtTokenAdminInterceptor` | `preHandle_shouldReturnFalse_whenTokenIsBlacklisted` | Unit |
| 7 | `JwtTokenAdminInterceptor` | `preHandle_shouldProceed_whenTokenNotBlacklisted` | Unit |
| 8 | `JwtTokenUserInterceptor` | Same as admin (2 tests) | Unit |
| 9 | `TokenBlacklistCleanupTask` | `run_shouldCallCleanupExpired` | Unit |
| 10 | `EmployeeController.logout` | `logout_shouldAddTokenToBlacklist` | Unit |

### Coverage Targets

| File | Target |
|------|--------|
| `TokenBlacklistServiceImpl` | 85%+ |
| `TokenBlacklistCleanupTask` | 80%+ |
| `JwtTokenAdminInterceptor` (modified lines) | 80%+ |
| `JwtTokenUserInterceptor` (modified lines) | 80%+ |

---

## Section 5: Commit Strategy

### Conventional Commits

| Order | Type | Message | Files |
|-------|------|---------|-------|
| 1 | `chore` | `add token_blacklist table migration` | `core/database/init.sql` or migration |
| 2 | `feat` | `add TokenBlacklist entity and MyBatis mapper` | `TokenBlacklist.java`, `TokenBlacklistMapper.java`, `.xml` |
| 3 | `feat` | `add TokenBlacklistService with SHA-256 hash storage` | `TokenBlacklistService.java`, `TokenBlacklistServiceImpl.java` |
| 4 | `feat` | `add token expiration helper to JwtUtil` | `JwtUtil.java` |
| 5 | `feat` | `integrate blacklist check into JWT interceptors` | `JwtTokenAdminInterceptor.java`, `JwtTokenUserInterceptor.java` |
| 6 | `feat` | `implement logout with token invalidation` | `EmployeeController.java`, `UserController.java` |
| 7 | `feat` | `add TokenBlacklistCleanupTask scheduled cleanup` | `TokenBlacklistCleanupTask.java`, `application-dev.yml` |
| 8 | `test` | `add token blacklist unit and integration tests` | `*Test.java` files |

### Skill/Agent Triggers

| Trigger Point | Agent/Skill | Mandatory? |
|--------------|-------------|-----------|
| After design doc saved | `writing-plans` | Yes |
| Before each component code | `tdd-guide` | Yes |
| After all code written | `code-reviewer` | Yes |
| After auth code complete | `security-reviewer` | **Yes** (`common/security.md` STOP rule) |
| After tests pass | `finishing-a-development-branch` | Yes |

---

## Design Decisions

1. **SHA-256 hash instead of raw token** — prevents storing usable credentials in database
2. **Graceful degradation on blacklist query failure** — `isBlacklisted()` returns `false` (allow) if DB is down, preventing total outage
3. **No subject-level ban** — only the specific token is invalidated; multi-device global logout is out of scope
4. **Retention of expired records for 24h** — supports audit and debugging needs
5. **Constructor injection for all new components** — aligns with `java/patterns.md`

## Risks

| Risk | Mitigation |
|------|-----------|
| DB latency on every request | `token_blacklist` has index on `token_hash`; query is O(1) |
| Table growth without cleanup | Scheduled cleanup + `expires_at` index |
| Blacklist check failure blocks all requests | Graceful degradation: log.error + allow on exception |
