# Agent B Prompt: Miniapp Anonymous Browse & Cleanup

```markdown
You are Agent B for Sky Delivery Next Iteration Hardening.

Workstream: Miniapp Anonymous Browse & Cleanup.

## Goal

Make miniapp public/private behavior intentional: anonymous browse must be either implemented safely or blocked by product decision. Clean up dead miniapp websocket code and handle merchant context across 401 redirects where feasible.

## Required Reading

- `docs/reviews/2026-05-05-next-iteration-handoff.md`
- `docs/reviews/2026-05-05-comprehensive-upgrade-final-report.md`
- `docs/superpowers/plans/2026-05-05-ecc-skill-usage-matrix.md`
- `docs/superpowers/plans/2026-05-05-next-iteration-hardening-gates.md`
- `core/miniapp/utils/request.js`
- `core/miniapp/utils/merchant.js`
- `core/miniapp/utils/session.js`
- `core/miniapp/utils/webscoket.js`

## Required Skills / Norms

Use aggressively where applicable:

- API contract discipline for public/private request boundaries.
- Spring Boot security boundary awareness: public browse changes must not weaken private cart/order/payment routes.
- Playwright/E2E smoke norms for route behavior where possible.
- Existing miniapp request-wrapper conventions.

If a skill is unavailable, state that and apply the closest documented norm manually.

## Owned Files

You may modify:

- `core/miniapp/pages/`
- `core/miniapp/utils/request.js`
- `core/miniapp/utils/merchant.js`
- `core/miniapp/utils/session.js`
- `core/miniapp/utils/webscoket.js`
- miniapp docs under `docs/`
- miniapp evidence docs under `docs/reviews/`

## Forbidden Without Main Thread Approval

- backend Java security/order/report logic
- `scripts/ralph/*`
- `task_plan.md`, `findings.md`, `progress.md`
- generated `core/miniapp/unpackage/`
- package installs or C-drive caches

## Tasks

1. Inspect current login gate in `request.js`.
2. Decide from available docs whether anonymous public browse is approved. If no explicit product decision exists, mark implementation `blocked` and document the needed decision rather than guessing.
3. If anonymous browse is approved in the handoff or new Ralph state, implement a narrow allowlist for public browse routes only.
4. Preserve login-gating for cart/order/payment/user-private routes.
5. Resolve `webscoket.js` status: delete if clearly unused and safe, quarantine/document if uncertain, or retain with explicit reason.
6. Review 401 redirect and merchantId preservation. Implement if low-risk; otherwise document exact follow-up.
7. Record manual verification steps.

## Verification

Use available checks:

- static reference search for `webscoket.js`.
- manual route contract table.
- Playwright/API smoke only if services are running.

## Delivery Summary

Return:

```markdown
## Agent B Delivery Summary

- Scope handled:
- Files changed:
- Anonymous browse decision: implemented / blocked / not-supported
- Private route protection:
- webscoket.js decision:
- 401 merchantId behavior:
- Commands or checks run:
- Results:
- Skills / Norms Used:
  - Skill or norm:
  - Where it affected decisions:
  - Verification performed because of it:
- Acceptance criteria satisfied:
- Remaining risks:
- Coordination requests:
```
```

