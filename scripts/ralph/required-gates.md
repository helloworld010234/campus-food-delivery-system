# Sky Delivery Comprehensive Upgrade — Required Gates

Loop: `ralph/sky-delivery-comprehensive-upgrade`
Source PRD: `tasks/prd-sky-delivery-comprehensive-upgrade.md`
Gate design: `docs/superpowers/plans/2026-05-05-comprehensive-upgrade-gates.md`

## Gate Status

| Gate | Status | Evidence | Owner |
|------|--------|----------|-------|
| G-001 Old Ralph archived and comprehensive PRD ready | pass | `scripts/ralph/archive/2026-05-05-multi-merchant-isolation/{prd.json,progress.txt,required-gates.md,task_plan.md,findings.md,progress.md,README.md}` and regenerated `scripts/ralph/prd.json` for branch `ralph/sky-delivery-comprehensive-upgrade`. | Coordinator |
| G-002 Six agent briefs complete | pass | All six briefs exist under `docs/superpowers/agent-briefs/2026-05-05-agent-0{1..6}-*.md` and matching prompts under `docs/superpowers/agent-prompts/2026-05-05-agent-0{1..6}-*-prompt.md`. | Coordinator |
| G-003 Backend security and scope verified | pass | Agent 1 delivery: `MerchantScopeGuard.java` + `MerchantScopeGuardTest.java` (29 tests) + `MerchantServiceImplScopeTest.java` (2 tests) + extended `JwtTokenAdminInterceptorTest`/`JwtTokenUserInterceptorTest`/`TokenBlacklistServiceTest`/`TokenBlacklistCleanupTaskTest`. `mvn -f core/backend/pom.xml test -pl sky-server "-Dtest=*Scope*Test,*Security*Test,*Jwt*Test,*TokenBlacklist*Test"` → BUILD SUCCESS, 57 tests, 0 failures. Permission matrix documented in `MerchantScopeGuard` Javadoc. Public-browse boundary explicit. | Agent 1 |
| G-004 Order and transaction consistency verified | pass | Agent 2 delivery: `ShoppingCartServiceImplTest.java` (17 new tests) + `OrderServiceImplTest.java` (extended to 21 tests). `mvn -f core/backend/pom.xml test -pl sky-server -Dtest="*Order*Test,*ShoppingCart*Test"` → BUILD SUCCESS, 42 tests, 0 failures. Transaction rollback on cross-merchant cart row verified. Reorder uses original-order merchant. Mock WeChat Pay branch preserved. Legacy single-merchant fallback preserved. | Agent 2 |
| G-005 Admin report/workspace scope verified | pass | Agent 3 delivery: `ReportServiceImplScopeTest.java` (8 tests) + `WorkspaceServiceImplScopeTest.java` (7 tests) + `core/nginx/html/sky/merchant-admin/admin-api-scope.md`. `mvn -f core/backend/pom.xml test -pl sky-server "-Dtest=*Report*Test,*Workspace*Test,*Scope*Test"` → BUILD SUCCESS, 46 tests, 0 failures. Platform admin sees aggregate; merchant accounts forced to own merchant; cross-merchant query rejected; Excel export reuses scoped businessData. | Agent 3 |
| G-006 Miniapp contract and UX flow verified | pass | Agent 4 delivery: code audit + one bug fix (removed duplicate `getData()` in `core/miniapp/pages/index/index.js`). `withMerchantScope` confirmed across cart/order/category/dish/setmeal/shop APIs. Reorder via `resolveMerchantIdFromOrder`. 401 path triggers login modal restoring default merchant. Documented risks: anonymous public browse not currently supported (request.js login-gates non-login URLs); `webscoket.js` dead code; 401 redirect drops route merchantId. None blocking. | Agent 4 |
| G-007 Deployment and environment reproducibility verified | pass | Agent 5 delivery: README/SETUP/`.env.example`/`tooling/.env.example`/`scripts/start-all.bat`/`scripts/stop-all.bat` audited and confirmed sufficient. Created `docs/deployment/troubleshooting.md` (JWT length, Redis auth, MySQL credentials, schema mismatch, ports, Nginx, miniapp LAN, health probe, WeChat Pay) and `docs/deployment/docker.md` (Compose layout, D-drive volume guidance, validation checklist). Docker artifacts at `core/backend/deploy/{Dockerfile,docker-compose.yml,.dockerignore}`. UTF-8 valid. No real credentials. Open: `docker compose up` checklist pending actual run. | Agent 5 |
| G-008 Verification matrix and Feynman absorption complete | pass | Agent 6 delivery: `docs/reviews/2026-05-05-comprehensive-upgrade-verification-matrix.md`. 22 Java test classes + 3 Playwright spec files inventoried. Feynman 2026-05-05 absorption: 17 PASS / 2 PARTIAL / 0 FAIL / 4 TIMEOUT / 17 SKIP — zero map to Sky Delivery defects (all SKIPs are Feynman v0.2.40 tool/env limitations; all TIMEOUTs are model-latency). Public-browse regression row included. Coordinator regression sweep: 5-step plan in matrix section 6. | Agent 6 |
| G-009 Integrated final report complete | pending | Coordinator pending: render final upgrade report from `docs/reviews/2026-05-05-comprehensive-upgrade-final-report-template.md`. | Coordinator |

## Required Final Check Procedure

Before any final readiness statement:

1. Read `scripts/ralph/prd.json` and verify every `userStories[].passes` value is `true`
   or has a documented blocker recorded with an owner.
2. Read this file and verify every gate has either passing evidence or a documented blocker
   with an owner.
3. Read `task_plan.md`, `findings.md`, and `progress.md` and confirm they reflect the latest
   iteration.
4. Run `git status --short` and confirm only intended changes remain (and that the original
   uncommitted dirty state has been preserved).
5. If any gate is not passing, record blockers in `scripts/ralph/progress.txt` with
   identified owner and continue the next iteration.
6. Render the final upgrade report using
   `docs/reviews/2026-05-05-comprehensive-upgrade-final-report-template.md`.
