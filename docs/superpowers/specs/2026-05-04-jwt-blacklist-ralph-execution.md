# Ralph Execution — JWT Token Blacklist

## 前置检查（Pre-Flight Gate）

启动执行前，确认以下全部条件满足：

- [ ] Spec 文件存在且可读：`docs/superpowers/specs/2026-05-04-jwt-blacklist-design.md`
- [ ] Plan 文件存在且可读：`docs/superpowers/plans/2026-05-04-jwt-blacklist.md`
- [ ] 基线 commit `271558f`（P0 fixes merged）在 git 历史中可验证
- [ ] 分支 `master` 为当前工作分支
- [ ] 已读取完整 spec 内容（5 个 sections + plan 11 个 tasks）
- [ ] 数据库 `db_new` 可连接或 `init.sql` 可执行

---

## prd.json（Execution Stories）

```json
{
  "project": "JWT Token Blacklist Implementation",
  "branchName": "claude/jwt-blacklist-execution",
  "description": "Ralph-style execution for JWT token blacklist: database table, MyBatis layer, service, interceptor integration, logout endpoints, scheduled cleanup",
  "userStories": [
    {
      "id": "US-001",
      "title": "Section 1-2 基础设施 — 数据库表与实体层",
      "description": "验证 token_blacklist 表结构正确，TokenBlacklist entity 与 MyBatis mapper 可编译通过",
      "acceptanceCriteria": [
        "Task 1: init.sql 追加 token_blacklist CREATE TABLE，含 uk_token_hash 唯一索引和 idx_expires_at 普通索引",
        "Task 2: TokenBlacklist.java 位于 sky-pojo，含 7 个字段（id, tokenHash, tokenType, subjectId, expiresAt, createdAt, reason），使用 @Data @Builder",
        "Task 3: TokenBlacklistMapper.java 含 insert / selectByTokenHash / deleteByExpiresAtBefore 三个方法",
        "Task 3: TokenBlacklistMapper.xml 正确映射 deleteByExpiresAtBefore",
        "Typecheck passes: mvn compile 无报错"
      ],
      "priority": 1,
      "passes": false,
      "notes": ""
    },
    {
      "id": "US-002",
      "title": "Section 2 Service 层 — TokenBlacklistService TDD",
      "description": "验证 Service 接口与实现通过 TDD 流程，覆盖黑名单加入、查询、降级、清理",
      "acceptanceCriteria": [
        "Task 4 Step 1: 先写测试 isBlacklisted_shouldReturnTrue_whenTokenInBlacklist，运行必须 FAIL",
        "Task 4 Step 3: TokenBlacklistServiceImpl 构造函数注入 TokenBlacklistMapper",
        "Task 4 Step 3: addToBlacklist 使用 DigestUtils.md5DigestAsHex 计算 hash（注意：设计文档写 SHA-256 但代码示例用 md5，需统一）",
        "Task 4 Step 3: isBlacklisted 异常时 log.error + return false（降级）",
        "Task 4 Step 5: 5 个测试全部 PASS：inBlacklist, notInBlacklist, mapperException, addToBlacklist, cleanupExpired",
        "Coverage >= 85% for TokenBlacklistServiceImpl"
      ],
      "priority": 2,
      "passes": false,
      "notes": ""
    },
    {
      "id": "US-003",
      "title": "Section 3 工具扩展 — JwtUtil.getExpirationDate",
      "description": "验证 JwtUtil 新增方法可正确提取 token 过期时间",
      "acceptanceCriteria": [
        "Task 5 Step 1: JwtUtilTest 先写失败测试 getExpirationDate_shouldReturnCorrectDate",
        "Task 5 Step 3: JwtUtil.getExpirationDate 使用 Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload().getExpiration()",
        "Task 5 Step 4: sky-common 模块测试 PASS",
        "无现有 JwtUtil 测试被破坏"
      ],
      "priority": 3,
      "passes": false,
      "notes": ""
    },
    {
      "id": "US-004",
      "title": "Section 3 拦截器集成 — Admin/User 黑名单检查",
      "description": "验证两个 JWT 拦截器在构造函数注入 TokenBlacklistService，并在 parseJWT 前检查黑名单",
      "acceptanceCriteria": [
        "Task 6: JwtTokenAdminInterceptor 构造函数注入 JwtProperties + TokenBlacklistService",
        "Task 6: preHandle 中 tokenBlacklistService.isBlacklisted(token) 在 parseJWT 之前调用",
        "Task 6: 黑名单命中时 response.setStatus(401) + return false",
        "Task 6: 3 个测试 PASS：blacklistedToken, validToken, nonHandlerMethod",
        "Task 7: JwtTokenUserInterceptor 同样改造，2 个测试 PASS",
        "两个拦截器改造后不影响现有认证流程"
      ],
      "priority": 4,
      "passes": false,
      "notes": ""
    },
    {
      "id": "US-005",
      "title": "Section 3 登出端点 — Employee/User logout",
      "description": "验证登出接口将当前 token 加入黑名单",
      "acceptanceCriteria": [
        "Task 8: EmployeeController 改用构造函数注入（3 个依赖）",
        "Task 8: logout() 从 HttpServletRequest header 提取 token，调用 tokenBlacklistService.addToBlacklist(token, 'ADMIN', 'LOGOUT')",
        "Task 8: token 为 null 或 blank 时不调用 service（防 NPE）",
        "Task 8: UserController 新增 logout() 端点，同样模式",
        "Task 8: EmployeeControllerTest 2 个测试 PASS：addToBlacklist, missingToken",
        "登出无论 token 是否有效都返回 Result.success()（防枚举攻击）"
      ],
      "priority": 5,
      "passes": false,
      "notes": ""
    },
    {
      "id": "US-006",
      "title": "Section 3 定时清理 — TokenBlacklistCleanupTask",
      "description": "验证定时任务每天凌晨 3 点清理过期 24 小时以上的黑名单记录",
      "acceptanceCriteria": [
        "Task 9: TokenBlacklistCleanupTask 构造函数注入 TokenBlacklistService",
        "Task 9: @Scheduled(cron = '${sky.jwt.blacklist-cleanup-cron:0 0 3 * * ?}')",
        "Task 9: run() 调用 cleanupExpired(LocalDateTime.now().minusDays(1))",
        "Task 9: 测试 PASS：run_shouldCallCleanupExpired",
        "Task 10: application-dev.yml 追加 sky.jwt.blacklist-cleanup-cron 配置"
      ],
      "priority": 6,
      "passes": false,
      "notes": ""
    },
    {
      "id": "US-007",
      "title": "Section 4-5 测试覆盖与审查 — 全量验证",
      "description": "验证所有测试通过、覆盖率达标、安全审查完成",
      "acceptanceCriteria": [
        "mvn test -pl sky-server 全部 PASS",
        "Jacoco 报告 TokenBlacklistServiceImpl >= 85%",
        "security-reviewer agent 审查 5 个认证相关文件无 CRITICAL 问题",
        "code-reviewer agent 审查所有新建/修改文件无 HIGH 问题",
        "commit 共 10 个（Task 1-10 各一个），conventional commits 格式正确"
      ],
      "priority": 7,
      "passes": false,
      "notes": ""
    },
    {
      "id": "US-008",
      "title": "写入执行报告并归档",
      "description": "汇总所有 story 的发现，生成结构化执行报告",
      "acceptanceCriteria": [
        "执行报告写入 docs/reviews/2026-05-04-jwt-blacklist-execution-report.md",
        "Report 包含：每个 US 的 PASS/FAIL 状态、具体发现（含行号引用）、BLOCKER/WARN/INFO",
        "progress.txt 更新，所有 US 标记为 DONE"
      ],
      "priority": 8,
      "passes": false,
      "notes": ""
    }
  ]
}
```

---

## Ralph Loop 执行协议

### Step 1: 读取状态

必执行：
1. Read `docs/superpowers/plans/2026-05-04-jwt-blacklist.md` -> 确认 11 个 tasks
2. Read `docs/superpowers/specs/2026-05-04-jwt-blacklist-design.md` -> 确认设计约束
3. `git log --oneline -5` -> 确认基线 commit `271558f`
4. `mvn -f core/backend/pom.xml test -pl sky-server -q` -> 确认当前测试基线

### Step 2: 选择下一个 Story

按 priority 顺序选择第一个 `passes: false` 的 story。

### Step 3: 执行 Acceptance Criteria（逐条验证）

逐条 criterion 的验证模板：

```markdown
- [criterion 描述]
  - 动作: [Read/Grep/编译/测试]
  - 证据: [具体发现，含行号或引用]
  - 状态: PASS / FAIL
```

### Step 4: 更新状态

- `prd.json`: `passes = true`, `notes` = 完成摘要
- `progress.txt`: 追加 `YYYY-MM-DD HH:MM | US-00X | DONE | [摘要]`

### Step 5: 循环继续

**自动进入下一个 story，不要停顿。**

---

## 阻塞点声明格式

```markdown
BLOCKER: [US-00X: criterion 描述]
SEVERITY: BLOCKER | WARN | INFO
EVIDENCE: [spec 中的具体引用或命令输出]
IMPACT: [阻塞了哪个后续 story 或实现阶段]
RECOMMENDATION: [修复建议]
```

---

## 退出协议

**全部满足才允许声明执行完成：**

1. 所有 8 个 story 的 `passes === true`
2. 每个 story 的 `notes` 都有具体证据（含代码行号引用）
3. 执行报告已写入 `docs/reviews/2026-05-04-jwt-blacklist-execution-report.md`
4. `progress.txt` 已记录完整执行历史
5. 无未解决的 BLOCKER

**退出输出：**

```markdown
RALPH EXECUTION COMPLETE
==========================
Spec: docs/superpowers/specs/2026-05-04-jwt-blacklist-design.md
Plan: docs/superpowers/plans/2026-05-04-jwt-blacklist.md
Branch: claude/jwt-blacklist-execution
Stories: 8/8 DONE

Execution Report: docs/reviews/2026-05-04-jwt-blacklist-execution-report.md

Summary:
- BLOCKER: [X]
- WARN: [X]
- INFO: [X]

Per-Story:
[US-001] 数据库表与实体层: [PASS/FAIL]
[US-002] Service 层 TDD: [PASS/FAIL]
[US-003] JwtUtil 扩展: [PASS/FAIL]
[US-004] 拦截器集成: [PASS/FAIL]
[US-005] 登出端点: [PASS/FAIL]
[US-006] 定时清理: [PASS/FAIL]
[US-007] 测试覆盖与审查: [PASS/FAIL]

All acceptance criteria verified with fresh evidence.
Ready for finishing-a-development-branch skill invocation.
```

---

## 核心禁令

- 禁止在未逐条验证 criterion 的情况下标记 story 为 done
- 禁止用"看起来一致"替代实际编译/测试验证
- 禁止跳过 BLOCKER 继续执行后续 story
- 禁止在未读取 plan 文件的情况下开始执行
- 禁止在未更新 progress.txt 的情况下结束执行

**证据先于断言。**
