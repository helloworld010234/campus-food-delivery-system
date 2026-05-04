# Sky Delivery P0 修复设计文档

**日期**: 2026-05-04  
**范围**: P0 阻断项代码修复（`@EnableScheduling` + WebSocket 握手认证）  
**基线 Commit**: `cde7038`  
**分支**: `claude/pedantic-fermat-fdd59c`

---

## Section 1: 工作流架构与全技能映射

### 阶段 0 — 研究确认（已完成）

| 输入 | 动作 | Skill / Agent / Rule |
|---|---|---|
| `feynman-test` 审计报告 | 代码-论文一致性审计 | `feynman-audit` skill |
| 源码定位 | 确认 `SkyApplication.java`、`OrderTask.java`、`WebSocketServer.java` | `code-explorer` agent |
| 消费者识别 | 确认 admin Vue 2 web 是唯一内部 `/ws/{sid}` 消费者 | `code-explorer` agent |

### 阶段 1 — 设计决策与 ADR

| 决策项 | Skill / Agent / Rule |
|---|---|
| 记录"为何选择 HandshakeInterceptor + query token" | `architecture-decision-records` skill |
| 架构评审 | `architect` agent / `code-architect` agent |
| 安全设计评审 | `springboot-security` skill + `security-reviewer` agent |

### 阶段 2 — TDD 实现

| 修复项 | 测试先行 Skill | 实现辅助 Skill | Agent |
|---|---|---|---|
| Fix 1: `@EnableScheduling` | `springboot-tdd` / `test-driven-development` / `tdd-workflow` | `springboot-patterns` | `tdd-guide` agent |
| Fix 2: WebSocket `HandshakeInterceptor` | `springboot-tdd` / `test-driven-development` / `tdd-workflow` | `springboot-security` + `springboot-patterns` | `tdd-guide` agent |

### 阶段 3 — 代码审查

| 审查维度 | Agent | Rule |
|---|---|---|
| Java / Spring Boot 规范 | `java-reviewer` agent | `rules/java/coding-style.md`、`rules/java/patterns.md` |
| 通用代码质量 | `code-reviewer` agent | `rules/common/code-review.md` |
| 安全漏洞扫描 | `security-reviewer` agent | `rules/common/security.md`、`rules/java/security.md` |
| 类型设计 | `type-design-analyzer` agent | — |
| 注释质量 | `comment-analyzer` agent | — |

### 阶段 4 — 验证与 E2E

| 验证项 | Skill / Agent |
|---|---|
| 单元测试 + 集成测试覆盖率 >=80% | `springboot-verification` skill + `verification-before-completion` skill |
| 定时任务调度验证 | `verification-loop` skill |
| 端到端：WebSocket 握手拒绝/通过 | `e2e-testing` skill + `e2e-runner` agent |
| 性能/无回归 | `performance-optimizer` agent |

### 阶段 5 — 提交与分支收尾

| 动作 | Skill / Agent / Rule |
|---|---|
| Conventional commit | `git-workflow` skill |
| PR 创建与描述 | `github-ops` skill |
| 分支收尾检查 | `finishing-a-development-branch` skill |
| 文档同步更新 | `doc-updater` agent |

### 终端交接

设计确认后，调用 `writing-plans` skill 生成详细实现计划。

---

## Section 2: 组件拆分与接口设计

### Fix 1 — `@EnableScheduling` 恢复定时任务

**组件变更**

| 文件 | 变更 | 说明 |
|---|---|---|
| `SkyApplication.java` | `+ @EnableScheduling` + `import` | 一行注解修复，遵循 `rules/common/coding-style.md` 的 KISS 原则 |

**Skill / Agent / Rule 映射**

| 阶段 | 映射 |
|---|---|
| TDD | `springboot-tdd` / `test-driven-development` skill：先写"缺少 `@EnableScheduling` 时 `OrderTask` 不被注册"的集成测试（RED），再添加注解（GREEN） |
| 代码审查 | `java-reviewer` + `code-reviewer` agent |
| 验证 | `springboot-verification` skill：启动 Spring 上下文，断言 `ScheduledAnnotationBeanPostProcessor` 已注册且 `OrderTask` 的 2 个 `@Scheduled` 方法在 `TaskScheduler` 中可见 |

### Fix 2 — WebSocket `/ws/{sid}` 握手认证

**技术约束**

`WebSocketServer` 使用 Jakarta WebSocket (`@ServerEndpoint`)，采用标准机制 `ServerEndpointConfig.Configurator.modifyHandshake()` 做握手阶段 token 预检，`@OnOpen` 做 `empId == sid` 最终校验。认证方案为 query string 传 `token`，复用 admin JWT。

**组件拆分**

| 角色 | 文件 | 新增/修改 | 职责 |
|---|---|---|---|
| 握手拦截器 | `WebSocketAuthConfigurator.java` | **新增** | `extends ServerEndpointConfig.Configurator`，重写 `modifyHandshake()`：从 `HandshakeRequest.getParameterMap()` 取 `token`，调用 `JwtUtil.parseJWT()` 做签名/过期验证，将 `empId` 写入 `ServerEndpointConfig.getUserProperties()` |
| 端点入口 | `WebSocketServer.java` | 修改 | `@ServerEndpoint(value="/ws/{sid}", configurator=WebSocketAuthConfigurator.class)`；`onOpen` 中从 `EndpointConfig` 读取 `empId`，断言 `String.valueOf(empId).equals(sid)`，失败立即 `session.close(CloseReason.VIOLATED_POLICY)` |
| JWT 工具 | `JwtUtil.java` | 不修改 | 复用现有 `parseJWT(String secretKey, String token)` 静态方法 |

**接口契约**

```java
public class WebSocketAuthConfigurator extends ServerEndpointConfig.Configurator {
    @Autowired private JwtProperties jwtProperties;

    @Override
    public void modifyHandshake(ServerEndpointConfig config,
                                HandshakeRequest request,
                                HandshakeResponse response) {
        // 1. 从 request.getParameterMap() 获取 token
        // 2. JwtUtil.parseJWT(jwtProperties.getAdminSecretKey(), token)
        // 3. 提取 JwtClaimsConstant.EMP_ID -> Long empId
        // 4. config.getUserProperties().put("empId", empId)
        // 5. 失败则抛出 BaseException（容器返回 401/403）
    }
}
```

```java
@OnOpen
public void onOpen(Session session,
                   @PathParam("sid") String sid,
                   EndpointConfig config) {
    Long empId = (Long) config.getUserProperties().get("empId");
    if (empId == null || !String.valueOf(empId).equals(sid)) {
        session.close(new CloseReason(VIOLATED_POLICY, "Unauthorized"));
        return;
    }
    SESSION_MAP.put(sid, session);
}
```

**数据流**

```
[Admin Vue 2 Web] --wss://host/ws/{sid}?token=<adminJwt>--> [Nginx]
                                                              |
                                                              v
                                                    [Tomcat WebSocket Container]
                                                              |
                                                    +---------+---------+
                                                    v                   v
                                          modifyHandshake()         @OnOpen()
                                          (token 签名/过期验证)       (empId == sid 校验)
                                                    |                   |
                                                    v                   v
                                          有效: empId 写入           匹配: 加入 SESSION_MAP
                                          userProperties             不匹配: session.close()
```

**Skill / Agent / Rule 映射**

| 阶段 | 映射 |
|---|---|
| 安全设计 | `springboot-security` skill + `security-reviewer` agent |
| 架构评审 | `architect` agent |
| TDD | `springboot-tdd` / `test-driven-development` skill |
| 代码质量 | `java-reviewer` agent |
| 安全审查 | `security-reviewer` agent |
| 类型设计 | `type-design-analyzer` agent |
| 验证 | `springboot-verification` skill + `e2e-testing` skill |

---

## Section 3: 数据流与错误处理

### Fix 1 — `@EnableScheduling` 错误处理

| 场景 | 当前行为 | 修复后行为 |
|---|---|---|
| 数据库连接失败 | `ordersMapper` 抛异常，TaskScheduler 记录 ERROR | 同左（无需额外改动） |
| 无匹配订单 | `orders == null` 或 `size() == 0`，空跑 | 同左，建议改为 `CollectionUtils.isEmpty()` |
| 更新单条失败 | 循环中断，剩余订单未处理 | **逐条 try-catch**，单条失败不阻断批次 |

**Skill / Agent / Rule 映射**

| 维度 | 映射 |
|---|---|
| 静默失败检测 | `silent-failure-hunter` agent |
| 批次容错 | `springboot-patterns` skill |
| 日志规范 | `rules/common/coding-style.md` + `rules/java/coding-style.md` |

### Fix 2 — WebSocket 认证错误处理

| 场景 | 处理行为 | 映射 |
|---|---|---|
| token 缺失 | `modifyHandshake()` 抛 `BaseException("Unauthorized")` | `security-reviewer` agent |
| token 伪造/签名错误 | `JwtUtil.parseJWT()` 抛异常，握手失败 | `security-reviewer` + `springboot-security` |
| token 过期 | JJWT 抛 `ExpiredJwtException`，握手失败 | `springboot-security` skill |
| empId != sid | `onOpen()` 中 `session.close(VIOLATED_POLICY)` | `security-reviewer` agent |
| 连接建立后发送失败 | 当前 `catch (Exception ignored)` — **静默吞异常** | `silent-failure-hunter` agent |

**静默失败修复**（P0 内处理）：

```java
} catch (Exception e) {
    log.error("WebSocket消息发送失败, sid={}", sid, e);
}
```

**Skill / Agent / Rule 映射**

| 维度 | 映射 |
|---|---|
| 安全错误处理 | `security-reviewer` agent |
| 静默失败清理 | `silent-failure-hunter` agent |
| 异常传播边界 | `springboot-patterns` skill |
| 日志脱敏 | `rules/java/security.md` + `rules/common/security.md` |

---

## Section 4: 测试策略

### Fix 1 — `@EnableScheduling` 测试

| 层级 | 测试类 | 目标 | Skill / Agent |
|---|---|---|---|
| 集成测试 | `SkyApplicationIT.java` | 启动 Spring 上下文，断言 `ScheduledAnnotationBeanPostProcessor` Bean 存在 | `springboot-tdd` + `tdd-guide` |
| 集成测试 | `OrderTaskIT.java` | 验证 `OrderTask` 的 2 个 `@Scheduled` 方法已被注册 | `springboot-tdd` |
| 单元测试 | `OrderTaskTest.java` | mock `OrdersMapper`，验证 `processTimeoutOrder()` 行为 | `test-driven-development` |

**TDD 顺序**：RED（断言 Bean 不存在）-> GREEN（添加注解）-> IMPROVE（覆盖率 >=80%）

### Fix 2 — WebSocket 握手认证测试

| 层级 | 测试类 | 目标 | Skill / Agent |
|---|---|---|---|
| 单元测试 | `WebSocketAuthConfiguratorTest.java` | mock HandshakeRequest/Response/Config，验证 token 边界 | `springboot-tdd` + `tdd-guide` |
| 集成测试 | `WebSocketServerIT.java` | `@SpringBootTest` + 容器，验证完整握手 | `springboot-tdd` |
| E2E 测试 | `WebSocketAuthE2E.java` | 真实 WebSocket 客户端连接 | `e2e-testing` + `e2e-runner` |

**测试场景矩阵**

| 场景 | 输入 | 预期 |
|---|---|---|
| token 缺失 | `?token=` 或不含 | 握手失败 401 |
| token 伪造 | `?token=fake.jwt.here` | 握手失败 401 |
| token 过期 | 过期 JWT | 握手失败 401 |
| empId != sid | empId=5, sid="3" | `session.close()` |
| 合法匹配 | empId=5, sid="5" | 加入 SESSION_MAP，可收发 |

**Skill / Agent / Rule 映射**

| 维度 | 映射 |
|---|---|
| TDD 强制 | `tdd-guide` agent |
| 安全测试 | `springboot-security` skill |
| E2E 验证 | `e2e-testing` + `e2e-runner` |
| 覆盖率 | `springboot-verification` + `verification-before-completion` |
| 测试分析 | `pr-test-analyzer` agent |

---

## Section 5: 提交策略与技能交接

### Commit 拆分

```
commit 1: fix: enable scheduling for order timeout tasks
          + 仅修改 SkyApplication.java

commit 2: feat: add JWT handshake auth to WebSocket endpoint
          + 新增 WebSocketAuthConfigurator.java
          + 修改 WebSocketServer.java
```

### 代码审查触发点

| 触发条件 | 触发的 Agent |
|---|---|
| 代码刚写完/修改后 | `code-reviewer` agent |
| 安全敏感代码变更 | `security-reviewer` agent |
| 架构变更 | `architect` agent |
| 提交到共享分支前 | `java-reviewer` agent |

### 审查执行顺序

```
1. git diff 查看变更范围
2. 先跑 security checklist -> security-reviewer agent
3. 再跑 code quality checklist -> java-reviewer + code-reviewer agent
4. 运行相关测试 -> springboot-verification skill
5. 验证覆盖率 >= 80% -> verification-before-completion skill
```

### 回滚锚点

- 基线 commit: `cde7038`
- 异常回滚: `git reset --hard cde7038`

### 技能交接图

```
brainstorming (当前) -> writing-plans -> executing-plans
                                      -> security-reviewer + java-reviewer + code-reviewer
                                      -> springboot-verification + e2e-testing + verification-before-completion
                                      -> git-workflow + github-ops
                                      -> finishing-a-development-branch
```

---

## 关键技能与规则索引

| 类别 | 名称 | 路径 |
|---|---|---|
| Skill | `writing-plans` | `~/.claude/skills/writing-plans` |
| Skill | `executing-plans` | `~/.claude/skills/executing-plans` |
| Skill | `springboot-tdd` | `~/.claude/skills/springboot-tdd` |
| Skill | `springboot-verification` | `~/.claude/skills/springboot-verification` |
| Skill | `springboot-security` | `~/.claude/skills/springboot-security` |
| Skill | `springboot-patterns` | `~/.claude/skills/springboot-patterns` |
| Skill | `git-workflow` | `~/.claude/skills/git-workflow` |
| Skill | `github-ops` | `~/.claude/skills/github-ops` |
| Skill | `finishing-a-development-branch` | `~/.claude/skills/finishing-a-development-branch` |
| Skill | `e2e-testing` | `~/.claude/skills/e2e-testing` |
| Skill | `feynman-audit` | `~/.claude/skills/feynman-audit` |
| Skill | `architecture-decision-records` | `~/.claude/skills/architecture-decision-records` |
| Skill | `verification-before-completion` | `~/.claude/skills/verification-before-completion` |
| Skill | `verification-loop` | `~/.claude/skills/verification-loop` |
| Agent | `tdd-guide` | `~/.claude/agents/tdd-guide.md` |
| Agent | `security-reviewer` | `~/.claude/agents/security-reviewer.md` |
| Agent | `java-reviewer` | `~/.claude/agents/java-reviewer.md` |
| Agent | `code-reviewer` | `~/.claude/agents/code-reviewer.md` |
| Agent | `architect` | `~/.claude/agents/architect.md` |
| Agent | `silent-failure-hunter` | `~/.claude/agents/silent-failure-hunter.md` |
| Agent | `type-design-analyzer` | `~/.claude/agents/type-design-analyzer.md` |
| Agent | `pr-test-analyzer` | `~/.claude/agents/pr-test-analyzer.md` |
| Agent | `code-explorer` | `~/.claude/agents/code-explorer.md` |
| Agent | `code-architect` | `~/.claude/agents/code-architect.md` |
| Agent | `comment-analyzer` | `~/.claude/agents/comment-analyzer.md` |
| Agent | `performance-optimizer` | `~/.claude/agents/performance-optimizer.md` |
| Agent | `e2e-runner` | `~/.claude/agents/e2e-runner.md` |
| Agent | `doc-updater` | `~/.claude/agents/doc-updater.md` |
| Rule | `git-workflow` | `rules/common/git-workflow.md` |
| Rule | `development-workflow` | `rules/common/development-workflow.md` |
| Rule | `security` | `rules/common/security.md` |
| Rule | `code-review` | `rules/common/code-review.md` |
| Rule | `testing` | `rules/common/testing.md` |
| Rule | `agents` | `rules/common/agents.md` |
| Rule | `coding-style` | `rules/common/coding-style.md` |
| Rule | `coding-style` | `rules/java/coding-style.md` |
| Rule | `security` | `rules/java/security.md` |
| Rule | `testing` | `rules/java/testing.md` |
| Rule | `patterns` | `rules/java/patterns.md` |
