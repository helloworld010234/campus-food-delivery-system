# Agent 03 Brief: 运营后台与报表工作台

日期：2026-05-05

## 1. Mission

Agent 3 负责后台管理接口、运营报表和工作台统计。目标是让平台视角和商户视角的接口语义清楚，统计口径一致，商户账号不会看到全局数据。

## 2. Background

当前项目包含后台管理端、报表接口和工作台指标。已有多商户隔离设计指出报表用户统计、工作台概览等路径可能存在过宽统计风险。Agent 3 负责把这些口径收拢，并与管理端静态前端调用保持一致。

## 3. Input Materials

- `docs/superpowers/specs/2026-05-05-sky-delivery-comprehensive-upgrade-design.md`
- `docs/superpowers/agent-briefs/2026-05-05-agent-01-backend-security.md`
- `docs/reviews/2026-05-05-feynman-report-upgrade-inputs.md`
- `D:\feynman-test-2026-05-05\outputs\source-comparison\comparison.md`
- `docs/superpowers/specs/2026-05-05-multi-merchant-isolation-design.md`

## 4. Primary File Ownership

Primary write scope:

- `core/backend/sky-server/src/main/java/com/sky/controller/admin/`
- `core/backend/sky-server/src/main/java/com/sky/service/impl/ReportServiceImpl.java`
- `core/backend/sky-server/src/main/java/com/sky/service/impl/WorkspaceServiceImpl.java`
- report/workspace/admin-focused tests
- `core/nginx/html/sky/merchant-admin/`

Coordinate before editing:

- `core/backend/sky-server/src/main/java/com/sky/security/`
- order service internals
- miniapp user pages
- shared mapper methods used by Agent 2

## 5. Forbidden Scope

Agent 3 must not:

- change account type semantics directly.
- remove platform admin global visibility unless product rule changes.
- make merchant reports global by default.
- edit miniapp files.
- rewrite the static admin app into a new framework.
- commit real credentials or export files.

## 6. Execution Tasks

1. Audit admin controller behavior for:
   - employee.
   - merchant.
   - category.
   - dish.
   - setmeal.
   - order.
   - report.
   - workspace.
2. Define platform vs merchant query semantics:
   - platform account can query global or selected merchant where endpoint supports it.
   - merchant account resolves to current merchant.
   - merchant account explicitly requesting another merchant is rejected.
3. Review report metrics:
   - turnover statistics.
   - user statistics.
   - order statistics.
   - sales Top10.
   - report export.
4. Review workspace metrics:
   - business data.
   - order overview.
   - dish overview.
   - setmeal overview.
5. Review static admin frontend calls:
   - token header.
   - merchant filters.
   - report date ranges.
   - dashboard API paths.
6. Add focused tests or API checks for platform and merchant perspectives.

## 7. Execution-Stage Notes

- Read current dirty diff first. `ReportServiceImpl` and `WorkspaceServiceImpl` may already have guard wiring from the prior multi-merchant work.
- Do not undo Agent 1 guard usage. If the guard does not expose a needed method, request coordination instead of adding local account checks everywhere.
- Be precise with "global" language. Platform global view is allowed; merchant global view is not.
- Report user statistics are especially sensitive: user counts can become global unless merchant filtering is explicitly modeled.
- Export endpoints must use the same query scope as on-screen reports.
- Workstation/dashboard "today" metrics should use the same merchant resolution rule as reports.
- If a mapper currently accepts a `Map`, document required keys and avoid adding ambiguous key names.
- Avoid making admin frontend UI changes that depend on backend behavior not yet implemented.
- Static admin files under `core/nginx/html/sky/merchant-admin/` are project assets, not generated Vue source; edit conservatively.
- Coordinate with Agent 2 before changing order status query semantics.
- Coordinate with Agent 5 before changing Nginx routes or deployment paths.
- Do not write export artifacts or temp files to C drive.

## 8. Suggested Verification

Focused commands:

```powershell
mvn -f core/backend/pom.xml test -pl sky-server "-Dtest=*Report*Test,*Workspace*Test,*Admin*Test,*Merchant*Scope*Test"
```

Manual/API checks:

- Platform admin report without merchant filter returns global data when intended.
- Platform admin report with merchant filter returns selected merchant data.
- Merchant admin report ignores or rejects other merchant id.
- Workspace overview for merchant admin is merchant-scoped.

## 9. Acceptance Criteria

- Platform and merchant report semantics are documented.
- Merchant accounts cannot view global report/workspace metrics.
- Export and screen report scopes are consistent.
- Admin static frontend request assumptions are documented or fixed.
- Focused report/workspace verification exists.

## 10. Delivery Summary Format

```markdown
## Delivery Summary

- Scope handled:
- Files changed:
- Tests run:
- Test result:
- Platform vs merchant semantics:
- Acceptance criteria satisfied:
- Remaining risks:
- Coordination requests:
```

