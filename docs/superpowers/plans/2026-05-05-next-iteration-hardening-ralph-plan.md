# Sky Delivery Next Iteration Hardening Ralph Plan

日期：2026-05-05

## 1. Plan Status

This is an execution-ready Markdown plan for a future Claude Code run. It should be converted into Ralph execution state by that future run, not by this session.

## 2. Required Preflight For Future Run

The future executor must:

1. Read `docs/reviews/2026-05-05-next-iteration-handoff.md`.
2. Read `docs/reviews/2026-05-05-comprehensive-upgrade-final-report.md`.
3. Read `docs/reviews/2026-05-05-comprehensive-upgrade-verification-matrix.md`.
4. Inspect `git status --short`.
5. Preserve all existing dirty worktree changes.
6. Archive current `scripts/ralph/{prd.json,progress.txt,required-gates.md}` and root memory files before overwriting.
7. Write a protection note that says existing uncommitted changes were observed and must not be reset, cleaned, deleted, or overwritten.
8. Stop immediately if archive/protection cannot be completed safely.
9. Start a new Ralph loop named `ralph/sky-delivery-next-iteration-hardening`.

No worker agent may be dispatched until steps 1 through 8 are complete.

## 3. Ralph Stories

### US-001 Ralph Hardening Control Plane

Owner: main Claude thread after parallel dispatch setup.

Scope:

- Archive previous Ralph state.
- Initialize new PRD/gates/memory files.
- Record baseline from previous handoff.
- Protect previous verified capabilities.
- Block all worker dispatch until archive-and-protect preflight completes.

Acceptance:

- Archive exists.
- New loop name is recorded.
- Baseline capabilities are listed in `findings.md`.
- Existing dirty worktree state is documented.
- A preflight protection note exists before any worker delivery summary.

### US-002 Deployment Hardening

Owner: Agent A.

Scope:

- Docker Compose verification.
- Dockerfile/Compose health-check review.
- startup script review.
- env template and secret safety review.
- deployment docs update.

Acceptance:

- Docker status is `verified`, `blocked`, or `not official path` with evidence.
- Health check matches an actual endpoint or doc explains required future endpoint.
- No secrets committed.
- No C-drive temp/cache path introduced.

### US-003 Miniapp Anonymous Browse And Cleanup

Owner: Agent B.

Scope:

- Anonymous public browse decision.
- `request.js` route allowlist or conservative behavior.
- `webscoket.js` dead-code resolution.
- 401 `merchantId` preservation behavior.
- backend public/private contract compatibility check.

Acceptance:

- Public/private boundary is explicit.
- Private cart/order flows remain login-gated.
- Anonymous browse outcome is implemented or blocked by product decision.
- dead code is removed, quarantined, or documented with reason.

### US-004 Quality Guardrails

Owner: Agent C.

Scope:

- `MerchantScopeUtils` mojibake cleanup.
- guard-consumer checklist.
- mock payment preservation comment/test.
- PR/merge gate docs.
- ECC Java review pass over changed Java areas.

Acceptance:

- Java text cleanup does not change behavior.
- New or updated tests protect mock payment behavior.
- New guardrail docs tell future contributors when to use `MerchantScopeGuard`.
- Focused Maven tests pass or blockers are recorded.

### US-005 Migration And Database Safety

Owner: Agent D.

Scope:

- inspect `phase1_multi_merchant_schema.sql`.
- classify idempotency.
- verify mapper assumptions for migration-added columns.
- update database migration docs.
- document rerun and rollback/recovery behavior.

Acceptance:

- Idempotency is classified with evidence.
- Rerun safety is documented.
- Mapper assumptions are listed.
- No unsafe schema change is made without rollback notes.

### US-006 CI/E2E Evidence

Owner: Agent E.

Scope:

- `SkyApplicationIT` feasibility.
- Playwright smoke and full E2E prerequisites.
- Maven full-module baseline classification.
- verification matrix update.
- Feynman evidence classification preservation.

Acceptance:

- Each check is `pass`, `blocked`, or `fail` with reason.
- No SKIP/TIMEOUT is misclassified as project failure.
- The verification matrix has next-iteration rows and evidence paths.

### US-007 Parallel Result Integration And Ralph Final Report

Owner: main Claude thread after all agents return.

Scope:

- Collect five delivery summaries.
- Update story status.
- Update gate status.
- Render final Ralph-style report.
- State merge readiness.

Acceptance:

- Every story is pass/fail/blocked.
- Every gate is pass/fail/blocked.
- Final report includes changed files, tests, blockers, and follow-ups.
- No pending state remains.

## 4. Parallel Dispatch Rule

The future executor must dispatch Agents A through E in one single Claude Code message or tool round, but only after archive-and-protect preflight passes. Do not run Agent A, wait, then run Agent B. The goal is true concurrent execution after the baseline is protected.

## 5. Coordinator-Lite Rule

The main thread should only:

- read baseline.
- dispatch all agents.
- collect summaries.
- update Ralph state.
- render final report.

The main thread should not pre-implement worker tasks unless a worker returns `blocked` and the unblock action belongs to the main thread.

## 6. Final Report Rule

The final report must use:

- `docs/reviews/2026-05-05-next-hardening-final-report-template.md`

The report must not claim success without evidence.
