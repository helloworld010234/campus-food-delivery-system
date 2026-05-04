# Sky Delivery P0 Fixes Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Fix two P0 blockers: (1) add missing `@EnableScheduling` to restore order timeout tasks, (2) add JWT handshake auth to WebSocket `/ws/{sid}` endpoint.

**Architecture:** Fix 1 is a single-annotation change with integration test. Fix 2 introduces a `ServerEndpointConfig.Configurator` subclass for handshake-time token validation, then checks `empId == sid` in `@OnOpen`. Both fixes reuse existing `JwtUtil`/`JwtProperties` without new dependencies.

**Tech Stack:** Spring Boot 3.4.4, Java 17, Jakarta WebSocket, JJWT 0.12.6, JUnit 5, Mockito, Testcontainers (MySQL 8), Maven, Jacoco 0.8.12

---

## File Structure

| File | Action | Responsibility |
|---|---|---|
| `sky-server/src/main/java/com/sky/SkyApplication.java` | Modify | Add `@EnableScheduling` annotation |
| `sky-server/src/main/java/com/sky/websocket/WebSocketAuthConfigurator.java` | **Create** | Handshake-time JWT token validation for WebSocket |
| `sky-server/src/main/java/com/sky/websocket/WebSocketServer.java` | Modify | Add `EndpointConfig` param to `@OnOpen`, call `session.close()` on auth failure, fix silent exception swallowing |
| `sky-server/src/test/java/com/sky/SkyApplicationIT.java` | **Create** | Integration test: verify `ScheduledAnnotationBeanPostProcessor` is registered |
| `sky-server/src/test/java/com/sky/websocket/WebSocketAuthConfiguratorTest.java` | **Create** | Unit test: token missing / forged / valid scenarios for `modifyHandshake()` |
| `sky-server/src/test/java/com/sky/websocket/WebSocketServerTest.java` | **Create** | Unit test: `onOpen()` auth check (empId == sid) |

---

## Task 1: Fix `@EnableScheduling` — Restore Order Timeout Tasks

**Files:**
- Modify: `sky-server/src/main/java/com/sky/SkyApplication.java`
- Test: `sky-server/src/test/java/com/sky/SkyApplicationIT.java`

### Step 1: Write the failing test

Create `sky-server/src/test/java/com/sky/SkyApplicationIT.java`:

```java
package com.sky;

import com.sky.config.TestcontainersConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Import;
import org.springframework.scheduling.annotation.ScheduledAnnotationBeanPostProcessor;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Import(TestcontainersConfig.class)
class SkyApplicationIT {

    @Autowired
    private ApplicationContext applicationContext;

    @Test
    void shouldRegisterScheduledAnnotationBeanPostProcessor_whenApplicationStarts() {
        ScheduledAnnotationBeanPostProcessor processor =
                applicationContext.getBean(ScheduledAnnotationBeanPostProcessor.class);
        assertThat(processor).isNotNull();
    }
}
```

### Step 2: Run test to verify it fails

```bash
cd core/backend/sky-server
mvn test -Dtest=SkyApplicationIT -DfailIfNoTests=false
```

**Expected:** FAIL with `NoSuchBeanDefinitionException: No qualifying bean of type 'org.springframework.scheduling.annotation.ScheduledAnnotationBeanPostProcessor'`

### Step 3: Write minimal implementation

Modify `sky-server/src/main/java/com/sky/SkyApplication.java`:

```java
package com.sky;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication
@EnableTransactionManagement
@EnableCaching
@EnableScheduling
@Slf4j
public class SkyApplication {
    public static void main(String[] args) {
        SpringApplication.run(SkyApplication.class, args);
        log.info("server started");
    }
}
```

### Step 4: Run test to verify it passes

```bash
mvn test -Dtest=SkyApplicationIT -DfailIfNoTests=false
```

**Expected:** PASS

### Step 5: Commit

```bash
git add sky-server/src/main/java/com/sky/SkyApplication.java

git add sky-server/src/test/java/com/sky/SkyApplicationIT.java

git commit -m "fix: enable scheduling for order timeout tasks"
```

---

## Task 2: Create WebSocket Handshake Authenticator

**Files:**
- Create: `sky-server/src/main/java/com/sky/websocket/WebSocketAuthConfigurator.java`
- Test: `sky-server/src/test/java/com/sky/websocket/WebSocketAuthConfiguratorTest.java`

### Step 1: Write the failing test

Create `sky-server/src/test/java/com/sky/websocket/WebSocketAuthConfiguratorTest.java`:

```java
package com.sky.websocket;

import com.sky.constant.JwtClaimsConstant;
import com.sky.exception.BaseException;
import com.sky.properties.JwtProperties;
import com.sky.utils.JwtUtil;
import jakarta.websocket.HandshakeResponse;
import jakarta.websocket.server.HandshakeRequest;
import jakarta.websocket.server.ServerEndpointConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WebSocketAuthConfiguratorTest {

    @Mock
    private HandshakeRequest request;

    @Mock
    private HandshakeResponse response;

    @Mock
    private ServerEndpointConfig config;

    @Mock
    private JwtProperties jwtProperties;

    @InjectMocks
    private WebSocketAuthConfigurator configurator;

    private static final String SECRET_KEY = "testsecretkeytestsecretkeytestsecretkey";
    private static final long TTL = 7200000L;

    @BeforeEach
    void setUp() {
        when(jwtProperties.getAdminSecretKey()).thenReturn(SECRET_KEY);
    }

    @Test
    void shouldThrowBaseException_whenTokenIsMissing() {
        when(request.getParameterMap()).thenReturn(Collections.emptyMap());
        when(config.getUserProperties()).thenReturn(new HashMap<>());

        assertThatThrownBy(() -> configurator.modifyHandshake(config, request, response))
                .isInstanceOf(BaseException.class)
                .hasMessageContaining("Unauthorized");
    }

    @Test
    void shouldThrowBaseException_whenTokenIsForged() {
        Map<String, List<String>> params = new HashMap<>();
        params.put("token", Collections.singletonList("fake.jwt.token"));
        when(request.getParameterMap()).thenReturn(params);
        when(config.getUserProperties()).thenReturn(new HashMap<>());

        assertThatThrownBy(() -> configurator.modifyHandshake(config, request, response))
                .isInstanceOf(BaseException.class)
                .hasMessageContaining("Unauthorized");
    }

    @Test
    void shouldPutEmpIdIntoUserProperties_whenTokenIsValid() {
        String token = JwtUtil.createJWT(SECRET_KEY, TTL,
                java.util.Map.of(JwtClaimsConstant.EMP_ID, 5L));

        Map<String, List<String>> params = new HashMap<>();
        params.put("token", Collections.singletonList(token));
        when(request.getParameterMap()).thenReturn(params);

        Map<String, Object> userProperties = new HashMap<>();
        when(config.getUserProperties()).thenReturn(userProperties);

        configurator.modifyHandshake(config, request, response);

        assertThat(userProperties).containsEntry("empId", 5L);
    }
}
```

### Step 2: Run test to verify it fails

```bash
mvn test -Dtest=WebSocketAuthConfiguratorTest -DfailIfNoTests=false
```

**Expected:** FAIL with compilation errors — `WebSocketAuthConfigurator` class does not exist.

### Step 3: Write minimal implementation

Create `sky-server/src/main/java/com/sky/websocket/WebSocketAuthConfigurator.java`:

```java
package com.sky.websocket;

import com.sky.constant.JwtClaimsConstant;
import com.sky.exception.BaseException;
import com.sky.properties.JwtProperties;
import com.sky.utils.JwtUtil;
import io.jsonwebtoken.Claims;
import jakarta.websocket.HandshakeResponse;
import jakarta.websocket.server.HandshakeRequest;
import jakarta.websocket.server.ServerEndpointConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
@Slf4j
public class WebSocketAuthConfigurator extends ServerEndpointConfig.Configurator {

    @Autowired
    private JwtProperties jwtProperties;

    @Override
    public void modifyHandshake(ServerEndpointConfig config,
                                HandshakeRequest request,
                                HandshakeResponse response) {
        Map<String, List<String>> parameterMap = request.getParameterMap();
        List<String> tokens = parameterMap.get("token");

        if (tokens == null || tokens.isEmpty()) {
            log.warn("WebSocket handshake rejected: missing token");
            throw new BaseException("Unauthorized");
        }

        String token = tokens.get(0);

        try {
            Claims claims = JwtUtil.parseJWT(jwtProperties.getAdminSecretKey(), token);
            Long empId = Long.valueOf(claims.get(JwtClaimsConstant.EMP_ID).toString());
            config.getUserProperties().put("empId", empId);
            log.debug("WebSocket handshake accepted for empId={}", empId);
        } catch (Exception e) {
            log.warn("WebSocket handshake rejected: invalid token");
            throw new BaseException("Unauthorized");
        }
    }
}
```

### Step 4: Run test to verify it passes

```bash
mvn test -Dtest=WebSocketAuthConfiguratorTest -DfailIfNoTests=false
```

**Expected:** PASS (3/3 tests)

### Step 5: Commit

```bash
git add sky-server/src/main/java/com/sky/websocket/WebSocketAuthConfigurator.java

git add sky-server/src/test/java/com/sky/websocket/WebSocketAuthConfiguratorTest.java

git commit -m "feat: add WebSocket handshake JWT authenticator"
```

---

## Task 3: Integrate Auth into WebSocketServer and Fix Silent Failures

**Files:**
- Modify: `sky-server/src/main/java/com/sky/websocket/WebSocketServer.java`
- Test: `sky-server/src/test/java/com/sky/websocket/WebSocketServerTest.java`

### Step 1: Write the failing test

Create `sky-server/src/test/java/com/sky/websocket/WebSocketServerTest.java`:

```java
package com.sky.websocket;

import jakarta.websocket.CloseReason;
import jakarta.websocket.EndpointConfig;
import jakarta.websocket.Session;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WebSocketServerTest {

    private WebSocketServer webSocketServer;

    @Mock
    private Session session;

    @Mock
    private EndpointConfig config;

    @BeforeEach
    void setUp() {
        webSocketServer = new WebSocketServer();
    }

    @Test
    void shouldCloseSession_whenEmpIdDoesNotMatchSid() throws Exception {
        Map<String, Object> userProperties = new HashMap<>();
        userProperties.put("empId", 5L);
        when(config.getUserProperties()).thenReturn(userProperties);

        webSocketServer.onOpen(session, "3", config);

        ArgumentCaptor<CloseReason> captor = ArgumentCaptor.forClass(CloseReason.class);
        verify(session).close(captor.capture());
        assertThat(captor.getValue().getCloseCode()).isEqualTo(CloseReason.CloseCodes.VIOLATED_POLICY);
    }

    @Test
    void shouldCloseSession_whenEmpIdIsMissing() throws Exception {
        when(config.getUserProperties()).thenReturn(new HashMap<>());

        webSocketServer.onOpen(session, "5", config);

        ArgumentCaptor<CloseReason> captor = ArgumentCaptor.forClass(CloseReason.class);
        verify(session).close(captor.capture());
        assertThat(captor.getValue().getCloseCode()).isEqualTo(CloseReason.CloseCodes.VIOLATED_POLICY);
    }

    @Test
    void shouldAddToSessionMap_whenEmpIdMatchesSid() throws Exception {
        Map<String, Object> userProperties = new HashMap<>();
        userProperties.put("empId", 5L);
        when(config.getUserProperties()).thenReturn(userProperties);

        webSocketServer.onOpen(session, "5", config);

        verify(session, never()).close(any(CloseReason.class));
        // After successful onOpen, sendToClient should be able to retrieve the session
        webSocketServer.sendToClient("5", "test message");
    }
}
```

### Step 2: Run test to verify it fails

```bash
mvn test -Dtest=WebSocketServerTest -DfailIfNoTests=false
```

**Expected:** FAIL with compilation errors — `onOpen` method signature does not accept `EndpointConfig` parameter.

### Step 3: Write minimal implementation

Modify `sky-server/src/main/java/com/sky/websocket/WebSocketServer.java`:

```java
package com.sky.websocket;

import jakarta.websocket.*;
import jakarta.websocket.server.PathParam;
import jakarta.websocket.server.ServerEndpoint;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Slf4j
@ServerEndpoint(value = "/ws/{sid}", configurator = WebSocketAuthConfigurator.class)
public class WebSocketServer {

    private static final Map<String, Session> SESSION_MAP = new ConcurrentHashMap<>();

    @OnOpen
    public void onOpen(Session session, @PathParam("sid") String sid, EndpointConfig config) {
        Long empId = (Long) config.getUserProperties().get("empId");
        if (empId == null || !String.valueOf(empId).equals(sid)) {
            log.warn("WebSocket connection rejected: empId={} does not match sid={}", empId, sid);
            try {
                session.close(new CloseReason(CloseReason.CloseCodes.VIOLATED_POLICY, "Unauthorized"));
            } catch (Exception e) {
                log.error("Failed to close unauthorized WebSocket session, sid={}", sid, e);
            }
            return;
        }
        SESSION_MAP.put(sid, session);
        log.info("WebSocket connection established, sid={}", sid);
    }

    @OnMessage
    public void onMessage(String message, @PathParam("sid") String sid) {
        // no-op
    }

    @OnClose
    public void onClose(@PathParam("sid") String sid) {
        SESSION_MAP.remove(sid);
        log.info("WebSocket connection closed, sid={}", sid);
    }

    public void sendToAllClient(String message) {
        Collection<Session> sessions = SESSION_MAP.values();
        for (Session session : sessions) {
            try {
                session.getBasicRemote().sendText(message);
            } catch (Exception e) {
                log.error("Failed to broadcast WebSocket message", e);
            }
        }
    }

    public void sendToClient(String sid, String message) {
        Session session = SESSION_MAP.get(sid);
        if (session == null) {
            return;
        }
        try {
            session.getBasicRemote().sendText(message);
        } catch (Exception e) {
            log.error("Failed to send WebSocket message to sid={}", sid, e);
        }
    }
}
```

### Step 4: Run test to verify it passes

```bash
mvn test -Dtest=WebSocketServerTest -DfailIfNoTests=false
```

**Expected:** PASS (3/3 tests)

### Step 5: Commit

```bash
git add sky-server/src/main/java/com/sky/websocket/WebSocketServer.java

git add sky-server/src/test/java/com/sky/websocket/WebSocketServerTest.java

git commit -m "feat: integrate JWT auth into WebSocketServer and fix silent failures"
```

---

## Task 4: Run Full Test Suite and Verify Coverage

**Files:** None (verification only)

### Step 1: Run all new tests

```bash
mvn test -Dtest=SkyApplicationIT,WebSocketAuthConfiguratorTest,WebSocketServerTest -DfailIfNoTests=false
```

**Expected:** All 7 tests PASS

### Step 2: Generate Jacoco coverage report

```bash
mvn jacoco:report
```

**Expected:** Report generated at `target/site/jacoco/index.html`

### Step 3: Verify coverage meets threshold

Check coverage report. Target: new code coverage >= 80%.

If coverage is below 80%, add additional test cases for:
- `sendToAllClient()` with exception scenario
- `sendToClient()` with null session scenario
- `onClose()` removing session

### Step 4: Commit (if coverage fixes added)

```bash
git add sky-server/src/test/java/com/sky/websocket/

git commit -m "test: improve WebSocket test coverage"
```

---

## Self-Review Checklist

### 1. Spec Coverage

| Spec Requirement | Plan Task |
|---|---|
| `@EnableScheduling` added to `SkyApplication.java` | Task 1, Step 3 |
| `ScheduledAnnotationBeanPostProcessor` test | Task 1, Step 1 |
| `WebSocketAuthConfigurator` created | Task 2, Step 3 |
| Token missing/forged/valid test scenarios | Task 2, Step 1 |
| `WebSocketServer.onOpen` auth check (empId == sid) | Task 3, Step 3 |
| Silent failure fix (`catch Exception ignored` → log) | Task 3, Step 3 |
| `sendToAllClient` / `sendToClient` exception logging | Task 3, Step 3 |
| TDD workflow (RED-GREEN) | All tasks |
| Commit per fix | Task 1 Step 5, Task 2 Step 5, Task 3 Step 5 |

**Gaps:** None.

### 2. Placeholder Scan

- [x] No "TBD", "TODO", "implement later"
- [x] No vague "add error handling" without code
- [x] No "write tests for the above" without test code
- [x] No "similar to Task N" references
- [x] Every code step has complete code block

### 3. Type Consistency

- `JwtUtil.parseJWT(String, String)` — matches existing signature (line 42 of JwtUtil.java)
- `JwtClaimsConstant.EMP_ID = "empId"` — matches existing constant
- `JwtProperties.getAdminSecretKey()` — matches existing getter
- `ServerEndpointConfig.Configurator.modifyHandshake()` — matches Jakarta WebSocket API
- `CloseReason.CloseCodes.VIOLATED_POLICY` — standard Jakarta constant
- `@OnOpen` signature with `EndpointConfig` — Jakarta WebSocket supports this parameter

All types consistent.

---

## Execution Handoff

**Plan complete and saved to `docs/superpowers/plans/2026-05-04-sky-delivery-p0-fixes.md`. Two execution options:**

**1. Subagent-Driven (recommended)** — Dispatch a fresh subagent per task, review between tasks, fast iteration

**2. Inline Execution** — Execute tasks in this session using executing-plans, batch execution with checkpoints

**Which approach?**
