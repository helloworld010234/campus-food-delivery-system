# Six-Agent Parallel Upgrade Plan

日期：2026-05-05

## 1. Purpose

This plan defines how six agents can work concurrently on the Sky Delivery comprehensive upgrade without overwriting each other or losing evidence.

The plan is design-only. It does not start agents, edit production code, or rebuild Ralph execution files.

## 2. Parallel Workstreams

| Agent | Workstream | Primary Ownership |
|---|---|---|
| Agent 1 | Backend security and permission boundaries | Auth, interceptors, scope guard, security tests |
| Agent 2 | Order and transaction flows | Cart/order/payment/reorder/reminder services |
| Agent 3 | Admin reporting and workspace | Admin controllers, report/workspace services, admin static app |
| Agent 4 | Miniapp UX and API contract | Miniapp pages and utilities |
| Agent 5 | Deployment and engineering | README, SETUP, env templates, scripts, deploy files |
| Agent 6 | Verification and evidence | Test matrix, E2E, reviews, gates, evidence docs |

## 3. Shared Files

The coordinator owns these files:

- `task_plan.md`
- `findings.md`
- `progress.md`
- `scripts/ralph/prd.json`
- `scripts/ralph/progress.txt`
- `scripts/ralph/required-gates.md`
- final upgrade report

Agents may propose updates to shared files in their summaries, but they should not directly edit them during parallel implementation unless explicitly assigned.

## 4. File Ownership

### Agent 1

Primary:

- `core/backend/sky-common/src/main/java/com/sky/context/`
- `core/backend/sky-common/src/main/java/com/sky/constant/`
- `core/backend/sky-server/src/main/java/com/sky/interceptor/`
- `core/backend/sky-server/src/main/java/com/sky/security/`
- `core/backend/sky-server/src/test/java/com/sky/security/`

Coordinate before touching:

- order/report/workspace services
- mapper XML or mapper interfaces shared with other agents

### Agent 2

Primary:

- `core/backend/sky-server/src/main/java/com/sky/service/impl/ShoppingCartServiceImpl.java`
- `core/backend/sky-server/src/main/java/com/sky/service/impl/OrderServiceImpl.java`
- order/cart-related mapper tests

Coordinate before touching:

- security guard semantics
- miniapp request contract files

### Agent 3

Primary:

- `core/backend/sky-server/src/main/java/com/sky/controller/admin/`
- `core/backend/sky-server/src/main/java/com/sky/service/impl/ReportServiceImpl.java`
- `core/backend/sky-server/src/main/java/com/sky/service/impl/WorkspaceServiceImpl.java`
- `core/nginx/html/sky/merchant-admin/`

Coordinate before touching:

- auth and scope guard code
- order service internals

### Agent 4

Primary:

- `core/miniapp/pages/`
- `core/miniapp/utils/request.js`
- `core/miniapp/utils/merchant.js`
- `core/miniapp/utils/session.js`
- `core/miniapp/utils/webscoket.js`

Coordinate before touching:

- backend endpoint contracts
- generated miniapp build output under `unpackage/`

### Agent 5

Primary:

- `README.md`
- `SETUP.md`
- `.env.example`
- `tooling/.env.example`
- `scripts/start-all.bat`
- `scripts/stop-all.bat`
- `core/backend/deploy/`

Coordinate before touching:

- real `.env`
- database migration scripts used by backend tests

### Agent 6

Primary:

- `verification/e2e-tests/`
- `docs/reviews/`
- `docs/superpowers/`
- verification evidence docs

Coordinate before touching:

- production code files owned by implementation agents

## 5. Integration Order

Final integration should happen in this order:

1. Agent 1 security and scope guard semantics.
2. Agent 2 order and cart logic that depends on security semantics.
3. Agent 3 admin reports and workspace metrics.
4. Agent 4 miniapp request and UX alignment.
5. Agent 5 deployment and reproducibility docs/scripts.
6. Agent 6 verification matrix and final evidence.

## 6. Conflict Rules

- If two agents need the same file, the coordinator decides ownership before edits continue.
- Agents must not revert changes they did not author.
- Agents must report all shared-file needs in their delivery summary.
- Generated build artifacts should not be edited unless the agent owns that generated surface.
- Current unrelated worktree changes must be preserved.

## 7. Agent Delivery Summary

Each agent must end with:

```markdown
## Delivery Summary

- Scope handled:
- Files changed:
- Tests run:
- Test result:
- Acceptance criteria satisfied:
- Remaining risks:
- Coordination requests:
```

## 8. Execution-Stage Notes Requirement

Each agent brief must include an `Execution-Stage Notes` section. This section records the practical cautions that matter during implementation, including:

- how to handle existing dirty worktree changes.
- which shared files must not be edited directly.
- which dependencies require coordination with other agents.
- which generated files or build outputs should be avoided.
- how to preserve compatibility behavior.
- how to classify test failures, skips, and missing environment dependencies.
- where C-drive writes must be avoided.

The coordinator should review this section before dispatching each agent. If an agent's task changes, update the corresponding brief before implementation starts.

## 9. Verification Expectations

Agents run focused tests for their area. The coordinator or Agent 6 runs the broad gate suite after integration:

- `mvn -f core/backend/pom.xml test`
- relevant focused Maven tests
- `npx playwright test` under `verification/e2e-tests`
- manual API checks where automation is unavailable

## 10. Worktree Recommendation

For true concurrent implementation, use isolated git worktrees or branches per agent. If a single branch is used, enforce file ownership strictly and integrate one agent at a time.
