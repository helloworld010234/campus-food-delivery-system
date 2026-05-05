# Agent 01 Brief: 后端安全与权限边界

日期：2026-05-05

## 1. Mission

Agent 1 负责 Sky Delivery 综合升级中的后端安全与权限边界。目标是建立稳定的身份认证、账号权限、商户作用域和越权防护基线，并吸收当前多商户隔离 Ralph 专项的已有成果。

## 2. Background

当前项目已经具备这些基础：

- `BaseContext` 保存当前登录 id、商户 id、账号类型。
- `JwtTokenAdminInterceptor` 和 `JwtTokenUserInterceptor` 处理后台与用户端 token。
- `MerchantScopeUtils` 提供部分商户上下文解析。
- `MerchantScopeGuard` 已有未提交草稿，用于统一服务层商户访问判断。
- token blacklist 已在此前工作中加入，用于登出后 token 失效。

现有风险是权限规则散落在各服务方法中，平台账号、商户账号、学生用户、公开浏览之间的边界需要更系统地表达和测试。

## 3. Input Materials

- `docs/superpowers/specs/2026-05-05-sky-delivery-comprehensive-upgrade-design.md`
- `tasks/prd-sky-delivery-comprehensive-upgrade.md`
- `docs/superpowers/plans/2026-05-05-six-agent-parallel-upgrade-plan.md`
- `docs/superpowers/plans/2026-05-05-comprehensive-upgrade-gates.md`
- `tasks/prd-multi-merchant-isolation.md`
- `docs/superpowers/specs/2026-05-05-multi-merchant-isolation-design.md`
- `D:\feynman-test-2026-05-05\reports\matrix.csv`
- `D:\feynman-test-2026-05-05\reports\final-test-harness-report.md`

## 4. Primary File Ownership

Primary write scope:

- `core/backend/sky-common/src/main/java/com/sky/context/`
- `core/backend/sky-common/src/main/java/com/sky/constant/`
- `core/backend/sky-server/src/main/java/com/sky/interceptor/`
- `core/backend/sky-server/src/main/java/com/sky/security/`
- `core/backend/sky-server/src/test/java/com/sky/security/`
- security/scope-focused tests under `core/backend/sky-server/src/test/java/`

Coordinate before editing:

- `OrderServiceImpl.java`
- `ShoppingCartServiceImpl.java`
- `ReportServiceImpl.java`
- `WorkspaceServiceImpl.java`
- mapper interfaces or XML shared with other agents
- root memory files and Ralph files

## 5. Forbidden Scope

Agent 1 must not:

- rewrite the authentication model from JWT to sessions or OAuth.
- introduce a new security framework unless explicitly approved.
- block public browse endpoints that product rules intentionally allow.
- edit miniapp UI files directly.
- overwrite current Ralph files.
- revert unrelated dirty worktree changes.

## 6. Execution Tasks

1. Build a permission matrix for platform admin, merchant admin, merchant staff, and student user.
2. Audit admin and user interceptors for context population and blacklist checks.
3. Review token blacklist service and tests for logout/token-expiration behavior.
4. Normalize merchant-scope guard semantics:
   - resolve admin query merchant id.
   - resolve merchant write id.
   - require explicit merchant id for private multi-merchant operations.
   - assert current merchant can access owned resources.
   - assert two domain records belong to the same merchant.
5. Identify private vs public endpoint categories:
   - public user browse: shop, category, dish, setmeal lists where allowed.
   - private user chain: cart, order, payment, history, detail, cancel, reorder, reminder.
   - admin chain: employee, merchant, catalog, order, reports, workspace.
6. Add focused tests for:
   - merchant account requesting another merchant.
   - platform account retaining global visibility.
   - missing current merchant context.
   - missing explicit merchant id in private multi-merchant mode.
   - public browse remains allowed.

## 7. Execution-Stage Notes

- Start by reading the current dirty diff. Existing `MerchantScopeGuard`, `ReportServiceImpl`, `WorkspaceServiceImpl`, and related tests may already contain user or previous-agent work. Work with it; do not reset it.
- Treat current multi-merchant isolation work as inherited context, not as disposable scratch. If it is incomplete, document the gap and extend it.
- Keep guard methods small and semantically named. Other agents will depend on these names and behaviors.
- Do not scatter new account-type conditionals through every service if a guard method can express the rule once.
- Preserve compatibility mode behavior where `MultiMerchantSchemaSupport` says the schema is not ready.
- Be careful with platform admin semantics: platform should keep global visibility where endpoint semantics already allow it.
- Be careful with merchant staff semantics: staff should normally behave like merchant-scoped accounts.
- Do not change response envelope conventions. Existing controllers generally return `Result<T>`.
- Prefer business exceptions for authorization denials inside service logic; leave authentication failures to interceptors.
- When adding tests, clear `BaseContext` before and after each test to avoid thread-local leakage.
- If a test needs a mapper/service owned by Agent 2 or Agent 3, write a focused mock-based test and report integration needs rather than editing another agent's area.
- Avoid editing generated `target/` files.
- Do not write temporary outputs to C drive.

## 8. Suggested Verification

Focused commands:

```powershell
mvn -f core/backend/pom.xml test -pl sky-server "-Dtest=*Scope*Test,*Security*Test,*Jwt*Test,*TokenBlacklist*Test"
```

Broader command for coordinator or Agent 6:

```powershell
mvn -f core/backend/pom.xml test
```

## 9. Acceptance Criteria

- Permission matrix exists in agent summary or supporting doc.
- Merchant account cross-merchant private access is denied.
- Platform account global or requested-merchant behavior is preserved.
- Token blacklist behavior remains covered.
- Public browse regression is explicitly considered.
- Focused tests pass or blockers are documented.

## 10. Delivery Summary Format

```markdown
## Delivery Summary

- Scope handled:
- Files changed:
- Tests run:
- Test result:
- Permission matrix summary:
- Acceptance criteria satisfied:
- Remaining risks:
- Coordination requests:
```

