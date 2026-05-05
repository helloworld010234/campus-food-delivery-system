# Sky Delivery Comprehensive Upgrade — Findings

## Active Evidence Sources

- Comprehensive upgrade design: `docs/superpowers/specs/2026-05-05-sky-delivery-comprehensive-upgrade-design.md`
- Source PRD: `tasks/prd-sky-delivery-comprehensive-upgrade.md`
- Six-agent parallel plan: `docs/superpowers/plans/2026-05-05-six-agent-parallel-upgrade-plan.md`
- Comprehensive upgrade Ralph blueprint: `docs/superpowers/plans/2026-05-05-comprehensive-upgrade-ralph-blueprint.md`
- Comprehensive upgrade gate design: `docs/superpowers/plans/2026-05-05-comprehensive-upgrade-gates.md`
- Multi-merchant isolation design (absorbed under Agent 1): `docs/superpowers/specs/2026-05-05-multi-merchant-isolation-design.md`
- Multi-merchant isolation PRD (absorbed): `tasks/prd-multi-merchant-isolation.md`
- Feynman 2026-05-05 inputs: `docs/reviews/2026-05-05-feynman-report-upgrade-inputs.md`
- Feynman raw reports: `D:\feynman-test-2026-05-05\reports\` (final-test-harness-report.md, matrix.csv, regression-vs-2026-05-04.md)

## Existing Backend Strengths Preserved As Agent 1 Baseline

- `core/backend/sky-common/src/main/java/com/sky/context/BaseContext.java` — id, merchant id, account type per request.
- `core/backend/sky-server/src/main/java/com/sky/utils/MerchantScopeUtils.java` — partial merchant scope resolution.
- `core/backend/sky-server/src/main/java/com/sky/support/MultiMerchantSchemaSupport.java` — schema capability flags for compatibility mode.
- `core/backend/sky-server/src/main/java/com/sky/interceptor/JwtTokenAdminInterceptor.java` and `JwtTokenUserInterceptor.java`.
- Token blacklist support with cleanup task.
- **Uncommitted (preserved): `core/backend/sky-server/src/main/java/com/sky/security/MerchantScopeGuard.java` plus `MerchantScopeGuardTest`** — Agent 1 must extend, not regress.
- **Uncommitted (preserved): scope-guard reuse in `ReportServiceImpl.java` and `WorkspaceServiceImpl.java`** — Agent 3 must extend, not regress.
- **Uncommitted (preserved): `core/backend/sky-server/src/test/java/com/sky/service/impl/MerchantServiceImplScopeTest.java`** — informs Agent 1 / Agent 3 baseline.

## High-Risk Areas By Agent Line

| Area | Agent | Risk |
|------|-------|------|
| JWT, blacklist, scope guard | 1 | Permission rules scattered across services; missing context normalization |
| Shopping cart and order chain | 2 | Implicit merchant context can produce mixed-merchant carts/orders |
| Reporting and workspace | 3 | Merchant accounts may see global metrics if filters absent |
| Miniapp request contract | 4 | Inconsistent `merchantId`/`shopId` parameter; 401 handling gaps |
| Setup, env, deployment | 5 | Real secrets risk; missing health checks; one-click startup fragility |
| Verification matrix | 6 | Feynman tool limitations risk being misread as project defects |

## Public Browse Boundary

- Public browsing must remain cross-merchant. Agents 1, 2, 4, and 6 must each include
  a regression check verifying that anonymous/public browse endpoints continue to work
  (shop, category, dish, setmeal listings).

## Feynman Report Interpretation (Agent 6 owns full matrix)

- PASS / improved → reusable test assets and capability evidence.
- PARTIAL / TIMEOUT → verification-system improvement signal, not Sky Delivery business
  failure unless cross-checked with code/runtime behavior.
- SKIP (REPL-only slash commands) → tool limitation, not project defect.

## Known Compatibility Mode

- When `MultiMerchantSchemaSupport` reports merchant columns are unavailable, single-merchant
  fallback must remain functional. Agents 1, 2, and 3 all rely on this guarantee.

## Coordinator Dispatch Note

Coordinator dispatched six parallel worker agents in a single message via the Agent tool.
File ownership and forbidden scope per `docs/superpowers/plans/2026-05-05-six-agent-parallel-upgrade-plan.md`.
Integration order: Agent 1 → 2 → 3 → 4 → 5 → 6.

## Six Agent Returns and Risks

All six agents returned. Aggregate test evidence:
- Backend security/scope/JWT/blacklist: 57 tests, BUILD SUCCESS (Agent 1).
- Order/cart: 42 tests, BUILD SUCCESS (Agent 2).
- Report/workspace/scope: 46 tests, BUILD SUCCESS (Agent 3).

Risks captured (non-blocking):
- `core/backend/sky-server/src/main/java/com/sky/utils/MerchantScopeUtils.java` retains
  GBK→UTF-8 mojibake on Chinese exception messages. Pre-existing, flagged by Agent 1 / 3.
- `core/miniapp/utils/request.js` login-gates ALL non-login URLs, blocking truly anonymous
  public browse. Agent 4 flagged as a product/policy decision (current miniapp design
  appears to require login first).
- `core/miniapp/utils/webscoket.js` is dead code with a legacy training URL
  (`wss://socket-canzg.itheima.net/ws`). Agent 4 flagged for future removal.
- `docker compose up` against `core/backend/deploy/docker-compose.yml` has not been
  validated; checklist deferred. Agent 5 flagged.
- `SkyApplicationIT` may be BLOCKED without Docker; Playwright requires services on
  `localhost:8081` and admin SPA build. Agent 6 flagged.
