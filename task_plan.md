# Sky Delivery Comprehensive Upgrade — Task Plan

Loop: `ralph/sky-delivery-comprehensive-upgrade`
Source PRD: `tasks/prd-sky-delivery-comprehensive-upgrade.md`
Design: `docs/superpowers/specs/2026-05-05-sky-delivery-comprehensive-upgrade-design.md`

## Goal

Upgrade `D:\sky-delivery` across six fronts — backend security, order/transaction
flows, admin reporting/workspace, miniapp UX/API contract, deployment engineering,
and verification evidence — coordinated by one main coordinator with six parallel
worker agents.

## Current Mode

Single coordinator window dispatches six worker agents in parallel via the Agent
tool with strict file ownership. Multi-merchant isolation专项 is absorbed under
Agent 1 (Backend Security and Permission Boundaries).

## Stories

- [x] US-001 Comprehensive upgrade control plane and old Ralph migration (Coordinator)
- [x] US-002 Backend security and permission boundary upgrade (Agent 1) — MerchantScopeGuard + 57 tests pass
- [x] US-003 Order and transaction flow upgrade (Agent 2) — 42 cart/order tests pass
- [x] US-004 Admin reporting and workspace upgrade (Agent 3) — 46 report/workspace/scope tests pass
- [x] US-005 Miniapp UX and API contract upgrade (Agent 4) — one bug fix, private-flow scope confirmed
- [x] US-006 Deployment, environment, and engineering upgrade (Agent 5) — troubleshooting + docker docs added
- [x] US-007 Verification system and quality evidence upgrade (Agent 6) — verification matrix delivered
- [ ] US-008 Six-line integration, regression verification, and final upgrade report (Coordinator) — final report pending

## Required Gates

- [x] G-001 Old Ralph archived and comprehensive PRD ready
- [x] G-002 Six agent briefs complete
- [x] G-003 Backend security and scope verified — Agent 1 evidence
- [x] G-004 Order and transaction consistency verified — Agent 2 evidence
- [x] G-005 Admin report/workspace scope verified — Agent 3 evidence
- [x] G-006 Miniapp contract and UX flow verified — Agent 4 evidence
- [x] G-007 Deployment and environment reproducibility verified — Agent 5 evidence
- [x] G-008 Verification matrix and Feynman absorption complete — Agent 6 evidence
- [ ] G-009 Integrated final report complete — coordinator pending

## File Ownership Summary

| Agent | Primary Ownership |
|-------|-------------------|
| Agent 1 | `core/backend/sky-common/.../context/`, `.../constant/`, `core/backend/sky-server/.../interceptor/`, `.../security/`, security tests |
| Agent 2 | `OrderServiceImpl.java`, `ShoppingCartServiceImpl.java`, cart/order tests |
| Agent 3 | `controller/admin/`, `ReportServiceImpl.java`, `WorkspaceServiceImpl.java`, `core/nginx/html/sky/merchant-admin/` |
| Agent 4 | `core/miniapp/pages/`, `core/miniapp/utils/{request.js,merchant.js,session.js,webscoket.js}` |
| Agent 5 | `README.md`, `SETUP.md`, `.env.example`, `tooling/.env.example`, `scripts/start-all.bat`, `scripts/stop-all.bat`, `core/backend/deploy/` |
| Agent 6 | `verification/e2e-tests/`, `docs/reviews/`, `docs/superpowers/`, evidence docs |

## Coordinator-Only Files

- `task_plan.md`
- `findings.md`
- `progress.md`
- `scripts/ralph/prd.json`
- `scripts/ralph/progress.txt`
- `scripts/ralph/required-gates.md`
- `docs/reviews/2026-05-05-comprehensive-upgrade-final-report.md` (rendered later)

## Decisions

- Public browsing remains cross-merchant by product rule.
- Private user flows require explicit merchant binding in multi-merchant mode.
- Merchant admin accounts cannot access or mutate other merchants' resources.
- Platform admin global visibility is preserved where endpoint semantics already allow it.
- Existing `MerchantScopeGuard`, scope-guard reuse in `ReportServiceImpl` /
  `WorkspaceServiceImpl`, and `MerchantServiceImplScopeTest` are baseline work that must
  be preserved and extended (not reverted).

## Active Iteration

US-001 done. All six worker agents (1-6) dispatched in parallel from a single coordinator
message and all six returned with Delivery Summaries. Coordinator integrated outputs:
US-002..US-007 transitioned to passes=true; G-003..G-008 transitioned to pass with evidence
paths. Pending: coordinator regression sweep per matrix section 6, then render final report
to close US-008 / G-009.

## Risks

| Risk | Impact | Mitigation |
|------|--------|------------|
| Six agents touching shared services | Merge conflicts and reverted work | Strict file ownership; coordinator-only shared files; integrate sequentially |
| Existing dirty work overwritten | Loss of MerchantScopeGuard / report scope work | Archive before overwrite; agent prompts forbid revert; preserve original dirty state |
| Public browsing accidentally blocked | Miniapp browsing regression | Agent 1, Agent 2, Agent 4, Agent 6 each include public-browse regression as acceptance criterion |
| Feynman tool limitations misread as defects | False negatives in evidence | Agent 6 classifies SKIP/TIMEOUT/PARTIAL with project-specific interpretation |
| Real secrets leaked in env templates | Credential exposure | Agent 5 limited to placeholder envs; coordinator forbids real `.env` edits |
