# Sky Delivery Comprehensive Upgrade — Verification Matrix

Date: 2026-05-05
Owner: Agent 6 (Verification & Evidence)
Phase: Parallel-phase preparation. Broad test execution deferred until coordinator integration signal.

> Source PRD: `tasks/prd-sky-delivery-comprehensive-upgrade.md`
> Gate design: `docs/superpowers/plans/2026-05-05-comprehensive-upgrade-gates.md`
> Required gates board: `scripts/ralph/required-gates.md`
> Feynman inputs: `docs/reviews/2026-05-05-feynman-report-upgrade-inputs.md`
> Final report template: `docs/reviews/2026-05-05-comprehensive-upgrade-final-report-template.md`

This matrix separates Sky Delivery application verification from Feynman tool/harness
verification. A Feynman REPL slash-command SKIP is documented as a tool-environment
limitation, not as an application defect.

---

## 1. Verdict Vocabulary

| Verdict | Meaning in this matrix | Action required |
|---|---|---|
| PASS | Command/check executed and produced expected outcome with evidence path. | Record evidence path. |
| PARTIAL | Command produced an artifact but exited non-zero or did not complete fully. | Review artifact before claiming pass. |
| TIMEOUT | Hit the run-time budget. Treated as execution outcome, not automatic semantic failure. | Inspect any artifact produced; assess whether to extend budget or split. |
| SKIP | Precondition or interaction model not satisfied (e.g. REPL-only, missing CLI, scope out). | Document concrete reason. Never count as pass. |
| PENDING | Not yet executed. The default state for broad tests during the parallel phase. | Coordinator schedules execution after integration. |
| BLOCKED | Cannot run because a dependency or environment is missing. | Record owner of unblock. |
| FAIL | Command ran and produced a wrong result with reproducible evidence. | Open follow-up; do not mark gate pass. |

Project-specific rules:

- TIMEOUT in the Feynman harness is not a Sky Delivery defect.
- SKIP for REPL-only Feynman commands is a tool limitation, not an app gap.
- PARTIAL artifacts must be reviewed manually; an artifact alone is not pass.
- PENDING items must be transitioned by the coordinator before the final report is rendered.

---

## 2. Backend Test Inventory (Maven, JUnit + Mockito + Spring)

Inventoried via `find core/backend -path '*/src/test/java/*.java'` against the
`D:\sky-delivery` working tree (main checkout, where the new untracked tests live).

### 2.1 Per-Test Classification

| # | Test Class | Module | Layer | Owner Agent | Default Verdict | Evidence Path / Reason |
|---:|---|---|---|---|---|---|
| 1 | `com.sky.utils.JwtUtilTest` | sky-common | unit | Agent 1 | PENDING (last run PASS in surefire) | `core/backend/sky-common/target/surefire-reports/com.sky.utils.JwtUtilTest.txt` (deleted in working tree, will regenerate) |
| 2 | `com.sky.service.TokenBlacklistServiceTest` | sky-server | unit | Agent 1 | PENDING (modified file in working tree, surefire was PASS) | `core/backend/sky-server/target/surefire-reports/com.sky.service.TokenBlacklistServiceTest.txt` |
| 3 | `com.sky.security.MerchantScopeGuardTest` | sky-server | unit (new) | Agent 1 | PENDING (untracked) | `target/surefire-reports/com.sky.security.MerchantScopeGuardTest.txt` (will regenerate) |
| 4 | `com.sky.interceptor.JwtTokenAdminInterceptorTest` | sky-server | unit | Agent 1 | PENDING (modified) | `target/surefire-reports/com.sky.interceptor.JwtTokenAdminInterceptorTest.txt` |
| 5 | `com.sky.interceptor.JwtTokenUserInterceptorTest` | sky-server | unit | Agent 1 | PENDING (modified) | `target/surefire-reports/com.sky.interceptor.JwtTokenUserInterceptorTest.txt` |
| 6 | `com.sky.task.TokenBlacklistCleanupTaskTest` | sky-server | unit | Agent 1 | PENDING (was PASS) | `target/surefire-reports/com.sky.task.TokenBlacklistCleanupTaskTest.txt` |
| 7 | `com.sky.service.impl.MerchantServiceImplScopeTest` | sky-server | unit (new) | Agent 1 / 3 | PENDING (untracked) | regenerate via Maven |
| 8 | `com.sky.service.impl.DishServiceImplTest` | sky-server | unit | Agent 3 | PENDING (was PASS) | `target/surefire-reports/com.sky.service.impl.DishServiceImplTest.txt` |
| 9 | `com.sky.service.impl.OrderServiceImplTest` | sky-server | unit | Agent 2 | PENDING (modified — Agent 2 may extend) | `target/surefire-reports/com.sky.service.impl.OrderServiceImplTest.txt` |
| 10 | `com.sky.service.impl.UserServiceImplTest` | sky-server | unit | Agent 2 / 4 | PENDING (was PASS) | `target/surefire-reports/com.sky.service.impl.UserServiceImplTest.txt` |
| 11 | `com.sky.service.impl.ReportServiceImplScopeTest` | sky-server | unit (new) | Agent 3 | PENDING (untracked) | regenerate via Maven |
| 12 | `com.sky.service.impl.WorkspaceServiceImplScopeTest` | sky-server | unit (new) | Agent 3 | PENDING (untracked) | regenerate via Maven |
| 13 | `com.sky.controller.admin.DishControllerTest` | sky-server | WebMvc | Agent 3 | PENDING (was PASS) | `target/surefire-reports/com.sky.controller.admin.DishControllerTest.txt` |
| 14 | `com.sky.controller.admin.EmployeeControllerTest` | sky-server | WebMvc | Agent 1 / 3 | PENDING (was PASS) | `target/surefire-reports/com.sky.controller.admin.EmployeeControllerTest.txt` |
| 15 | `com.sky.controller.admin.SetmealControllerTest` | sky-server | WebMvc | Agent 3 | PENDING (was PASS) | `target/surefire-reports/com.sky.controller.admin.SetmealControllerTest.txt` |
| 16 | `com.sky.controller.user.DishControllerTest` | sky-server | WebMvc | Agent 4 | PENDING (was PASS) | `target/surefire-reports/com.sky.controller.user.DishControllerTest.txt` |
| 17 | `com.sky.controller.user.OrderControllerTest` | sky-server | WebMvc | Agent 2 | PENDING (was PASS) | `target/surefire-reports/com.sky.controller.user.OrderControllerTest.txt` |
| 18 | `com.sky.controller.user.UserControllerTest` | sky-server | WebMvc | Agent 4 | PENDING (was PASS) | `target/surefire-reports/com.sky.controller.user.UserControllerTest.txt` |
| 19 | `com.sky.websocket.WebSocketAuthConfiguratorTest` | sky-server | unit | Agent 1 | PENDING (was PASS, surefire .txt deleted in working tree) | `target/surefire-reports/com.sky.websocket.WebSocketAuthConfiguratorTest.txt` |
| 20 | `com.sky.websocket.WebSocketServerTest` | sky-server | unit | Agent 1 | PENDING (was PASS) | `target/surefire-reports/com.sky.websocket.WebSocketServerTest.txt` |
| 21 | `com.sky.SkyApplicationIT` | sky-server | integration | Coordinator | PENDING (depends on Docker/Testcontainers; may BLOCKED) | `target/failsafe-reports/com.sky.SkyApplicationIT.txt` |
| 22 | `com.sky.TestcontainersConfig` | sky-server | test config | Coordinator | not a test class | wiring helper |
| 23 | `com.sky.test.OssValidationTest` | sky-server | env validation | Agent 5 | PENDING (env-dependent; may SKIP if OSS env missing) | `target/surefire-reports/com.sky.test.OssValidationTest.txt` |

Total Java test classes: **22 executable** (1 common + 21 server) plus 1 test-config helper (`TestcontainersConfig`). The previous matrix listed 19; the 3 new untracked tests
(`MerchantServiceImplScopeTest`, `ReportServiceImplScopeTest`,
`WorkspaceServiceImplScopeTest`) bring the total to 22.

New tests added during this upgrade (untracked per `git status --short` in the main checkout):

- `core/backend/sky-server/src/test/java/com/sky/security/MerchantScopeGuardTest.java`
- `core/backend/sky-server/src/test/java/com/sky/service/impl/MerchantServiceImplScopeTest.java`
- `core/backend/sky-server/src/test/java/com/sky/service/impl/ReportServiceImplScopeTest.java`
- `core/backend/sky-server/src/test/java/com/sky/service/impl/WorkspaceServiceImplScopeTest.java`

Modified tests (per `git status --short` in the main checkout):

- `JwtTokenAdminInterceptorTest`, `JwtTokenUserInterceptorTest`, `TokenBlacklistServiceTest`, `OrderServiceImplTest`.

### 2.2 Mapper Integration Coverage

No dedicated mapper-level test classes (`*MapperTest`) exist in the inventoried tree.
Mapper paths are exercised indirectly via service tests against
`@MockBean`/`Mockito` doubles, and by `SkyApplicationIT` against Testcontainers.
If Agents 1–3 add direct mapper integration tests, append them here with their owner.

### 2.3 Worktree Note

This Agent 6 session ran inside `D:\sky-delivery\.claude\worktrees\eager-lewin-54e9d5`
on branch `claude/jwt-blacklist`, which is a clean checkout of the JWT-blacklist work
already merged. The new untracked scope tests above live only in the main checkout at
`D:\sky-delivery`. The matrix path itself, however, is also untracked in the main
checkout — its existence and updates are owned by Agent 6 per the brief.

---

## 3. End-to-End Test Inventory (Playwright)

Located under `verification/e2e-tests/`.

- Config: `verification/e2e-tests/playwright.config.ts`
  - `baseURL: http://localhost:8081`
  - `actionTimeout: 10000`, `navigationTimeout: 30000`
  - Single project: `chromium`
- `npx playwright test --list` enumeration (via static read; not executed):

### 3.1 Per-Spec Classification

| # | Spec File | Test Cases | Default Verdict | Reason / Evidence |
|---:|---|---|---|---|
| 1 | `verification/e2e-tests/tests/e2e/api/health.spec.ts` | `backend user API responds`, `backend admin API responds`, `nginx serves frontend index` | PENDING | Smallest smoke. Requires admin backend on `:8081` and nginx serving `/`. Run after Agent 5 reports services up. |
| 2 | `verification/e2e-tests/tests/e2e/auth/login.spec.ts` | `login page loads`, `admin can login with valid credentials`, `invalid credentials show error` | PENDING | Hits `/api/employee/login`. Depends on JWT/blacklist work (Agent 1). |
| 3 | `verification/e2e-tests/tests/e2e/features/dashboard.spec.ts` | `dashboard loads after login`, `can navigate to shop table page` | PENDING | Post-login SPA routing. Depends on admin SPA build under `core/nginx/html/sky/`. |

Total: **3 spec files / 8 test cases**.

### 3.2 Coverage Gaps (Owner-Tagged Recommendations)

| Gap | Suggested owner | Suggested artifact |
|---|---|---|
| Multi-merchant scope smoke (admin login as merchant A cannot see merchant B data) | Agent 6 (with Agent 1 contract) | new `tests/e2e/security/scope-isolation.spec.ts` |
| Cart submit / order submit / reorder / cancel happy path | Agent 6 (with Agent 2 contract) | new `tests/e2e/order/order-flow.spec.ts` |
| Public dish/setmeal/category/shop browse without token (regression) | Agent 6 (with Agent 4 contract) | new `tests/e2e/public/public-browse.spec.ts` |
| Admin reporting page scope check (turnover/order/user with merchant scope) | Agent 6 (with Agent 3 contract) | new `tests/e2e/admin/report-scope.spec.ts` |
| Miniapp contract surface | Agent 4 | manual contract audit + curl scripts; Playwright likely SKIP for wx runtime |

Agent 6 may author new specs above. Agent 6 must not edit specs authored by other agents.

---

## 4. Verification Matrix (Project-Level)

| Category | Command / Check | Expected Scope | Verdict | Evidence Path | Notes |
|---|---|---|---|---|---|
| Backend (full) | `mvn -f core/backend/pom.xml test` | All modules: sky-common, sky-pojo, sky-server | PENDING | `core/backend/sky-server/target/surefire-reports/`, `core/backend/sky-server/target/site/jacoco/index.html` | Run after coordinator integration signal. JaCoCo site already present (stale; will be regenerated). |
| Backend focused | `mvn -f core/backend/pom.xml -pl sky-server -Dtest=MerchantServiceImplScopeTest test` | New scope guard | PENDING | `target/surefire-reports/com.sky.service.impl.MerchantServiceImplScopeTest.txt` | Already executed once during dev (untracked surefire report present). |
| Backend focused | `mvn -f core/backend/pom.xml -pl sky-server -Dtest=MerchantScopeGuardTest test` | Security scope guard | PENDING | `target/surefire-reports/com.sky.security.MerchantScopeGuardTest.txt` | Already executed once during dev (untracked surefire report present). |
| Backend focused | `mvn -f core/backend/pom.xml -pl sky-server -Dtest=JwtTokenAdminInterceptorTest,JwtTokenUserInterceptorTest,TokenBlacklistServiceTest,TokenBlacklistCleanupTaskTest test` | JWT + blacklist (Agent 1) | PENDING | `target/surefire-reports/` | Surefire reports already exist from prior runs (modified state in git). |
| Backend focused | `mvn -f core/backend/pom.xml -pl sky-server -Dtest=OrderServiceImplTest,OrderControllerTest test` | Order flow (Agent 2) | PENDING | `target/surefire-reports/` | Add cart/reorder/cancel tests if Agent 2 introduces them. |
| Backend focused | `mvn -f core/backend/pom.xml -pl sky-server -Dtest=*WorkspaceService*,*ReportService* test` | Workspace + report (Agent 3) | PENDING | `target/surefire-reports/` | Test class names depend on Agent 3 additions. |
| Backend focused | `mvn -f core/backend/pom.xml -pl sky-server -Dtest=WebSocketAuthConfiguratorTest,WebSocketServerTest test` | WebSocket auth | PENDING | `target/surefire-reports/` | Tests already exist on disk; surefire output deleted in working tree (git shows D). |
| Backend integration | `mvn -f core/backend/pom.xml -pl sky-server -Dtest=SkyApplicationIT verify` | Spring context | PENDING | `target/surefire-reports/` | Requires Testcontainers (Docker). Likely BLOCKED if Docker daemon absent; mark accordingly. |
| Coverage | open `core/backend/sky-server/target/site/jacoco/index.html` | JaCoCo report | PENDING | `core/backend/sky-server/target/site/jacoco/index.html` | Generated by Maven test phase; verify ≥ 80% on changed classes. |
| E2E | `npx playwright test` (cwd `verification/e2e-tests`) | Health, login, dashboard | PENDING | `verification/e2e-tests/playwright-report/index.html` | Requires admin backend on `localhost:8081` and frontend reachable. Coordinate with Agent 5. |
| E2E focused | `npx playwright test tests/e2e/api/health.spec.ts` | Health endpoints | PENDING | playwright-report | Smallest smoke; usable as preflight. |
| Manual API | `curl` admin login + protected endpoint with merchant scope header | Permission matrix (Agent 1) | PENDING | record JSON in `docs/reviews/` | Coordinate with Agent 1. |
| Manual API | `curl` user public dish list without token | Public browse regression | PENDING | record JSON in `docs/reviews/` | Coordinate with Agent 4. |
| Manual API | `curl` anonymous shop status / category list / dish list / setmeal list | Public browse regression (anonymous shop/category/dish/setmeal listing must remain accessible) | PENDING | record JSON in `docs/reviews/` | Cross-cuts Agents 1, 2, 4. Required by spec section 9 risk: "业务升级破坏公开浏览". |
| Manual API | order submit / reorder / cancel happy path | Order flow (Agent 2) | PENDING | record in `docs/reviews/` | Coordinate with Agent 2. |
| Manual API | admin report turnover / order with merchant scope | Report scope (Agent 3) | PENDING | record in `docs/reviews/` | Coordinate with Agent 3. |
| Deployment | follow `docs/.../setup` and `docs/.../deploy` to first-time start | Reproducibility (Agent 5) | PENDING | doc paths from Agent 5 | Coordinate with Agent 5. |
| Static | `git status --short` | Workspace state | OBSERVED | inline | Captured at Agent 6 start; preserved (no revert). |

Until the coordinator signals integration, every PENDING row stays PENDING. Agent 6 must
not mark any of them PASS without artifact review.

---

## 5. Feynman 2026-05-05 Absorption Matrix

Sources (immutable, do not edit):

- `D:\feynman-test-2026-05-05\reports\final-test-harness-report.md`
- `D:\feynman-test-2026-05-05\reports\matrix.csv`
- `D:\feynman-test-2026-05-05\reports\regression-vs-2026-05-04.md`

### 5.1 Verdict Counts

| Verdict | Count | Project interpretation |
|---|---:|---|
| PASS | 17 | Tool/harness path validated. Subset is project-relevant (docker, replication, source_comparison, literature_review, deep_research, watch). |
| PARTIAL | 2 | Artifact present, exit non-zero. Review needed before using as project evidence. |
| FAIL | 0 | None. |
| TIMEOUT | 4 | peer_review, paper_code_audit, autoresearch, jobs. Inspect artifacts before drawing conclusions. |
| ERROR | 0 | None. |
| SKIP | 17 | Mostly Layer 2 / Layer 3 REPL-only slash commands and 4 fixed SKIPs. Tool limitation, not app defect. |
| TOTAL | 40 | |

### 5.2 Layer Interpretation

| Layer | Cases | Verdicts | Project meaning |
|---|---|---|---|
| L1-CLI | 8 | 8 PASS | Stable preflight evidence. Useful only as tool readiness signal. |
| L2-EXT | 8 | 8 SKIP (REPL-only) | Tool limitation. Not a Sky Delivery failure. Documented as such. |
| L3-LIVE | 5 | 5 SKIP (REPL-only) | Same as above. |
| L4A-SKILL | 10 | 6 PASS, 2 PARTIAL, 2 TIMEOUT | Project-relevant skills (docker, source_comparison) PASS. peer_review/paper_code_audit TIMEOUT — artifacts to be reviewed if cited. |
| L4B-INTER | 3 | 2 PASS, 1 TIMEOUT | deep_research and watch usable as roadmap background. autoresearch TIMEOUT artifact must be reviewed. |
| L4CDE-MISC | 6 | 1 PASS, 4 SKIP, 1 TIMEOUT | replication PASS — directly usable as Agent 5 setup-doc input. |

### 5.3 Regression vs 2026-05-04

- improved (4): deep_research, watch, autoresearch, replication.
- same (10): paper_writing, source_comparison, literature_review, session_search, docker, session_log, alpha_research, modal_compute, runpod_compute, contributing.
- regressed (5): peer_review, eli5, paper_code_audit, preview, jobs.

Regressions affect tool/skill confidence. They do not block Sky Delivery business
upgrades unless the upgrade depends directly on those skills' outputs. None of the
five regressed skills is a load-bearing input for any current agent line.

### 5.4 Mapping Feynman Outputs to Project Use

| Skill | Verdict | Project use |
|---|---|---|
| docker | PASS | Optional input to Agent 5 deployment / Compose review. |
| replication | PASS | Useful as reference for Agent 5 setup reproduction guide. |
| source_comparison | PASS | Optional cross-check for Agents 1–5 if a thesis-vs-code drift question arises. |
| deep_research / watch | PASS | Roadmap background only. Not project evidence. |
| literature_review | PASS | Roadmap background only. |
| autoresearch (TIMEOUT) | review artifact | Roadmap background only. Do not cite without review. |
| peer_review (TIMEOUT) | review artifact | Optional commentary; not project evidence. |
| paper_code_audit (TIMEOUT) | review artifact | Optional commentary; not project evidence. |
| eli5 (PARTIAL) | review artifact | Not project evidence. |
| preview (PARTIAL) | review artifact | Not project evidence. |
| jobs (TIMEOUT) | review artifact | Not project evidence. |

### 5.5 Data Integrity Note

Feynman report records:

- thesis.docx SHA256 unchanged: `e7639acdf4904f97f84a0cb45dfb0484b47fa8aa7be9e3583023c9c3687a538c`.
- No unmasked secret tokens detected in scanned outputs.

Recorded here so the project's verification evidence chain can cite it without
re-reading the immutable source report.

### 5.6 Tool-Limitation vs Project-Defect Decision Table

This table is the canonical answer to "is this Feynman row a Sky Delivery problem?"
The coordinator and any reader of the final report should consult it before quoting
a Feynman verdict against the project.

| Feynman row pattern | Verdict | Tool limitation? | Project defect? | Rationale |
|---|---|---|---|---|
| L1-CLI cases (1–8) | PASS | n/a | no | Tool readiness only. Counts as preflight, not feature evidence. |
| L2-EXT cases (9–16) | SKIP | **yes** | **no** | Slash commands are REPL-only in v0.2.40; `--prompt` and `chat <text>` send slash text as prompt; stdin pipe times out at 30s. |
| L3-LIVE cases (17–21) | SKIP | **yes** | **no** | Same REPL-only constraint as L2-EXT. |
| L4A docker / replication / source_comparison / literature_review / paper_writing / session_search / session_log | PASS | n/a | no | Skill output usable as background, but Sky Delivery business correctness is not asserted by these. |
| L4A peer_review / paper_code_audit | TIMEOUT | **yes (rate-limit / model latency)** | **no** | Artifacts exist; need human review before any project citation. |
| L4A eli5 | PARTIAL | **yes** | **no** | Artifact incomplete; not project evidence. |
| L4A preview | PARTIAL | **yes** | **no** | Slow rendering; not project evidence. |
| L4B deep_research / watch | PASS | n/a | no | Roadmap background only. |
| L4B autoresearch | TIMEOUT | **yes** | **no** | Artifact partial; roadmap background only. |
| L4CDE alpha_research / modal_compute / runpod_compute / contributing | SKIP | **yes (env / spec)** | **no** | Missing CLI or out-of-scope by spec. |
| L4CDE jobs | TIMEOUT | **yes** | **no** | Background skill; not project evidence. |
| L4CDE replication | PASS | n/a | no | Optional input to Agent 5 setup-doc reproduction guide. |

**Bottom line for the final report**: zero Feynman rows in the 2026-05-05 run map to
a Sky Delivery application defect. Every non-PASS row is either a tool/env limitation
or a non-load-bearing background skill.

---

## 6. Minimum Recommended Coordinator Regression Sweep

Use this as the smallest command set that closes the verification loop. The longer
list in Section 7 is the full reference.

```powershell
# Step 1 — fast unit / WebMvc / interceptor / scope guard suite (no Docker required)
mvn -f core/backend/pom.xml -pl sky-server -DfailIfNoTests=false `
  -Dtest=JwtTokenAdminInterceptorTest,JwtTokenUserInterceptorTest,`
TokenBlacklistServiceTest,TokenBlacklistCleanupTaskTest,`
WebSocketAuthConfiguratorTest,WebSocketServerTest,`
MerchantScopeGuardTest,MerchantServiceImplScopeTest,`
ReportServiceImplScopeTest,WorkspaceServiceImplScopeTest,`
DishServiceImplTest,OrderServiceImplTest,UserServiceImplTest,`
DishController*,EmployeeControllerTest,SetmealControllerTest,`
OrderControllerTest,UserControllerTest test
```

```powershell
# Step 2 — full module test (catches anything Step 1 missed; expect ~22 test classes)
mvn -f core/backend/pom.xml test
```

```powershell
# Step 3 — Spring context integration (BLOCKED if Docker unavailable; mark explicitly)
mvn -f core/backend/pom.xml -pl sky-server -Dtest=SkyApplicationIT verify
```

```powershell
# Step 4 — Playwright smoke (services must be on :8081). Health spec first.
cd verification/e2e-tests
npx playwright test tests/e2e/api/health.spec.ts
npx playwright test  # full E2E if Step 4a green
```

```powershell
# Step 5 — anonymous public-browse curl smoke (records public-browse regression)
curl -i http://localhost:8080/user/shop/status
curl -i http://localhost:8080/user/category/list?type=1
curl -i http://localhost:8080/user/dish/list?categoryId=<id>
curl -i http://localhost:8080/user/setmeal/list?categoryId=<id>
```

Acceptance for the sweep:

- Step 1 must be GREEN. Any FAIL in Step 1 is a real project defect.
- Step 2 GREEN confirms no other test class regressed.
- Step 3 may be BLOCKED if Docker is absent — record reason, do not mark FAIL.
- Step 4 may be BLOCKED if services are not running — record reason, do not mark FAIL.
- Step 5 must return 200/code=1 for each call. Non-200 / code≠1 is a real defect
  (public-browse regression risk in spec section 9).

---

## 7. Quality Gate Mapping

Cross-references `docs/superpowers/plans/2026-05-05-comprehensive-upgrade-gates.md`
and `scripts/ralph/required-gates.md`.

| Gate | Owner | Required evidence categories | Where to look |
|---|---|---|---|
| G-001 Old Ralph archived & PRD ready | Coordinator | Archive directory + new prd.json | `scripts/ralph/archive/2026-05-05-multi-merchant-isolation/`, `scripts/ralph/prd.json` |
| G-002 Six agent briefs complete | Coordinator | 6 brief files + 6 prompt files | `docs/superpowers/agent-briefs/2026-05-05-agent-0{1..6}-*.md`, `docs/superpowers/agent-prompts/2026-05-05-agent-0{1..6}-*-prompt.md` |
| G-003 Backend security & scope | Agent 1 | Permission matrix, scope/security tests, public-browse regression check | Agent 1 delivery summary, `MerchantScopeGuardTest`, `MerchantServiceImplScopeTest`, JWT/interceptor tests, JaCoCo report |
| G-004 Order/transaction consistency | Agent 2 | Cart/order/payment/reorder/cancel/history/detail/reminder evidence | Agent 2 delivery summary, OrderServiceImplTest + new tests, manual API records |
| G-005 Admin report/workspace scope | Agent 3 | Platform vs merchant statistic evidence; export/screen scope | Agent 3 delivery summary, ReportService/WorkspaceService tests + manual checks |
| G-006 Miniapp contract & UX flow | Agent 4 | Request contract review; user-flow verification | Agent 4 delivery summary, miniapp request audit, optional Playwright/manual evidence |
| G-007 Deployment & reproducibility | Agent 5 | Setup/deploy docs; health-check / startup validation | Agent 5 delivery summary, `docs/.../setup` + `docs/.../deploy`, startup logs |
| G-008 Verification matrix & Feynman absorption | Agent 6 | This document + Feynman summary + test command list | `docs/reviews/2026-05-05-comprehensive-upgrade-verification-matrix.md` (this file) |
| G-009 Integrated final report | Coordinator | Filled `2026-05-05-comprehensive-upgrade-final-report-template.md` referencing G-001..G-008 evidence | `docs/reviews/<final-report-filename>.md` (rendered post-integration) |

Pass criterion: every gate is `pass` with evidence, or explicitly `blocked` with a
concrete reason and follow-up owner. PENDING is not a final state.

---

## 8. Suggested Broad-Test Commands for Coordinator (Run After Integration)

Do not run these during the parallel phase. Coordinator runs them in this order
once Agents 1–5 have integrated.

```powershell
# 1. Full backend test (root pom)
mvn -f core/backend/pom.xml test
```

```powershell
# 2. Backend integration test (requires Docker for Testcontainers)
mvn -f core/backend/pom.xml -pl sky-server -Dtest=SkyApplicationIT verify
```

```powershell
# 3. Coverage report (already produced by step 1 if jacoco profile is active)
# Open file:
# core/backend/sky-server/target/site/jacoco/index.html
```

```powershell
# 4. Playwright E2E (services must be running on localhost:8081)
cd verification/e2e-tests
npx playwright install --with-deps chromium  # only if not yet installed
npx playwright test
```

Focused commands collected from agent briefs (placeholders — fill from Agent 1–5
delivery summaries):

| Agent | Suggested focused command(s) | Source |
|---|---|---|
| Agent 1 | `mvn -pl sky-server -Dtest=MerchantScopeGuardTest,MerchantServiceImplScopeTest,JwtTokenAdminInterceptorTest,JwtTokenUserInterceptorTest,TokenBlacklistServiceTest,TokenBlacklistCleanupTaskTest test` | Inferred from new test files. To confirm in Agent 1 summary. |
| Agent 2 | `mvn -pl sky-server -Dtest=OrderServiceImplTest,OrderControllerTest test` (+ any new cart/order tests) | To confirm in Agent 2 summary. |
| Agent 3 | `mvn -pl sky-server -Dtest=*ReportService*,*WorkspaceService* test` (+ any new admin scope tests) | To confirm in Agent 3 summary. |
| Agent 4 | manual contract audit + targeted Playwright if added | To confirm in Agent 4 summary. |
| Agent 5 | startup smoke + health-check curl | To confirm in Agent 5 summary. |
| Agent 6 | this matrix + final report | This file. |

---

## 9. Hard Constraints Recorded

- Raw Feynman reports under `D:\feynman-test-2026-05-05\` are immutable and were not
  edited. Summaries were written into this project doc instead.
- TIMEOUT verdicts are not auto-classified as failures. Each Feynman TIMEOUT was
  paired with its artifact path so the artifact can be reviewed before any citation.
- SKIP verdicts for Layer 2/Layer 3 REPL-only slash commands are documented as tool
  limitations of Feynman v0.2.40 (`--prompt` and `chat <text>` do not invoke slash
  commands; stdin pipe times out at 30s). They are not Sky Delivery failures.
- Production code was not modified by Agent 6.
- No broad Maven or Playwright runs were executed during the parallel phase. Broad
  runs are scheduled for after coordinator integration.
- No artifact was written to the C drive by Agent 6.

---

## 10. Agent Delivery Summary Placeholders

The coordinator will splice these into
`docs/reviews/2026-05-05-comprehensive-upgrade-final-report-template.md` once each
agent reports back. Agent 6 owns these placeholders.

### Agent 1 — Backend Security & Scope

- Scope handled: _pending_
- Files changed: _pending_
- Tests run: _pending_
- Result: _pending_
- Risks: _pending_
- Coordination notes: _pending_

### Agent 2 — Order & Transaction Flows

- Scope handled: _pending_
- Files changed: _pending_
- Tests run: _pending_
- Result: _pending_
- Risks: _pending_
- Coordination notes: _pending_

### Agent 3 — Admin Reporting & Workspace

- Scope handled: _pending_
- Files changed: _pending_
- Tests run: _pending_
- Result: _pending_
- Risks: _pending_
- Coordination notes: _pending_

### Agent 4 — Miniapp Contract & UX

- Scope handled: _pending_
- Files changed: _pending_
- Tests run: _pending_
- Result: _pending_
- Risks: _pending_
- Coordination notes: _pending_

### Agent 5 — Deployment & Engineering

- Scope handled: _pending_
- Files changed: _pending_
- Tests run: _pending_
- Result: _pending_
- Risks: _pending_
- Coordination notes: _pending_

### Agent 6 — Verification & Evidence (self)

- Scope handled: Verification matrix, per-Java-test classification, per-Playwright-spec classification, public-browse regression row, Feynman tool-vs-project decision table, minimum coordinator regression sweep, gate mapping, broad-test command catalog, agent delivery placeholders, coverage gaps tagged with agent owners.
- Files changed: `docs/reviews/2026-05-05-comprehensive-upgrade-verification-matrix.md` (this file).
- Tests run: None (parallel phase). Inventory only. `mvn` and `npx playwright` not invoked by Agent 6 — recommended to coordinator per section 6.A.
- Result: Matrix and placeholders ready for coordinator integration. Default verdict for every project test row is PENDING per the brief; default Feynman verdict per row is preserved from the immutable source report.
- Risks: PENDING rows must transition before final report; SkyApplicationIT may be BLOCKED without Docker; Playwright requires running services; OssValidationTest may SKIP without OSS env.
- Coordination notes: Need focused commands and delivery summaries from Agents 1–5 to convert PENDING placeholders. Recommend coordinator runs Section 6.A sweep as the first integration check.

---

## 11. Open Questions for Coordinator

1. Are SkyApplicationIT (Testcontainers) and Playwright services in scope for the
   final broad-run, or should they be marked BLOCKED with documented reasons?
2. Will Agents 1–3 add mapper-level integration tests, or is service-level coverage
   plus `SkyApplicationIT` deemed sufficient?
3. Should the final report copy live as `docs/reviews/2026-05-05-comprehensive-upgrade-final-report.md`
   (separate from the template), or replace the template? Current assumption: a
   sibling file, leaving the template untouched.
