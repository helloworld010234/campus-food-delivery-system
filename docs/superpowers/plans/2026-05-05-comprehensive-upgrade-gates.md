# Comprehensive Upgrade Gate Design

日期：2026-05-05

## 1. Purpose

This document defines the required gates for the Sky Delivery comprehensive upgrade.

It is a Markdown design document. During execution, these gates can be converted into `scripts/ralph/required-gates.md`.

## 2. Gate Table

| Gate | Status During Design | Evidence Required During Execution |
|---|---|---|
| G-001 Old Ralph archived and comprehensive PRD ready | designed | Archive path exists; `tasks/prd-sky-delivery-comprehensive-upgrade.md` is source PRD |
| G-002 Six agent briefs complete | designed | All 6 `docs/superpowers/agent-briefs/*.md` files exist with ownership, scope, tests, and acceptance criteria |
| G-003 Backend security and scope verified | designed | Agent 1 summary; security/scope tests; permission matrix |
| G-004 Order and transaction consistency verified | designed | Agent 2 summary; cart/order/payment/reorder tests or API checks |
| G-005 Admin report/workspace scope verified | designed | Agent 3 summary; platform vs merchant statistic evidence |
| G-006 Miniapp contract and UX flow verified | designed | Agent 4 summary; request contract review; user-flow verification |
| G-007 Deployment and environment reproducibility verified | designed | Agent 5 summary; setup/deploy docs; health-check or startup validation |
| G-008 Verification matrix and Feynman absorption complete | designed | Agent 6 summary; PASS/PARTIAL/TIMEOUT/SKIP matrix; test command evidence |
| G-009 Integrated final report complete | designed | Final report lists all workstreams, tests, risks, and follow-ups |

## 3. Gate Details

### G-001 Old Ralph Archived And Comprehensive PRD Ready

Pass evidence:

- Existing multi-merchant Ralph files archived.
- New source PRD exists.
- New Ralph branch name and story mapping are recorded.

Fail conditions:

- Old `scripts/ralph/prd.json` is overwritten without archive.
- New PRD does not mention all 6 agent workstreams.

### G-002 Six Agent Briefs Complete

Pass evidence:

- All 6 brief files exist.
- Each brief includes goal, background, input materials, ownership, forbidden scope, execution details, tests, and delivery summary format.

Fail conditions:

- Any agent lacks file ownership.
- Shared files are assigned to multiple agents without coordination rule.

### G-003 Backend Security And Scope Verified

Pass evidence:

- Permission matrix covers platform admin, merchant admin, merchant staff, and user.
- Cross-merchant private access tests pass or have documented blockers.
- Public browse regression is explicitly checked.

### G-004 Order And Transaction Consistency Verified

Pass evidence:

- Cart operations use `userId + merchantId` in multi-merchant mode.
- Order submit, reorder, cancel, detail, history, and reminder paths preserve ownership.
- Transactional test or manual proof covers order detail creation and cart cleanup.

### G-005 Admin Report And Workspace Scope Verified

Pass evidence:

- Report and workspace services use platform/merchant-scoped semantics.
- Merchant account cannot see global metrics.
- Export and screen statistics use consistent scope.

### G-006 Miniapp Contract And UX Flow Verified

Pass evidence:

- Private miniapp requests carry `merchantId` or `shopId`.
- Public browse requests remain usable.
- Login/session failures and business errors have reviewed behavior.

### G-007 Deployment And Environment Reproducibility Verified

Pass evidence:

- Setup docs list required dependencies and versions.
- Environment templates contain placeholders, not real secrets.
- Database initialization and migration order are documented.
- Startup and health verification steps are documented or tested.

### G-008 Verification Matrix And Feynman Absorption Complete

Pass evidence:

- `final-test-harness-report.md`, `matrix.csv`, and `regression-vs-2026-05-04.md` are summarized.
- PASS/PARTIAL/TIMEOUT/SKIP are interpreted.
- Required Maven, Playwright, API, manual, and Feynman-derived checks are listed.

### G-009 Integrated Final Report Complete

Pass evidence:

- Final report includes all 6 agent summaries.
- Tests and residual risks are listed.
- Follow-up work is prioritized.

## 4. Final Readiness Rule

The project should not be called complete until every gate is either `pass` with evidence or explicitly marked as blocked with a concrete reason and follow-up owner.

