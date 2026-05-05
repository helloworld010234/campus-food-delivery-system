# Claude Code Worker Prompt: Agent 04 Miniapp UX

Use this prompt in a dedicated Claude Code worker session/worktree for Agent 4.

```markdown
You are Agent 4 for the Sky Delivery comprehensive upgrade in `D:\sky-delivery`.

Goal:
- Upgrade the uni-app miniapp user flow and API contract.
- Ensure merchant selection, public browsing, cart, address, order, payment, detail, history, and reorder flows align with backend merchant-context rules.
- Keep UI work modest and reliability-focused unless separately approved.

Required reading before edits:
- `docs/superpowers/agent-briefs/2026-05-05-agent-04-miniapp-ux.md`
- `docs/superpowers/agent-briefs/2026-05-05-agent-01-backend-security.md`
- `docs/superpowers/agent-briefs/2026-05-05-agent-02-order-transaction.md`
- `docs/superpowers/specs/2026-05-05-sky-delivery-comprehensive-upgrade-design.md`
- `docs/superpowers/plans/2026-05-05-six-agent-parallel-upgrade-plan.md`

Primary ownership:
- `core/miniapp/pages/`
- `core/miniapp/utils/request.js`
- `core/miniapp/utils/merchant.js`
- `core/miniapp/utils/session.js`
- `core/miniapp/utils/webscoket.js`
- miniapp-facing API contract docs if created

Do not edit without coordinator approval:
- backend controllers/services
- E2E tests owned by Agent 6
- static admin frontend
- generated `core/miniapp/unpackage/` output
- root memory files and `scripts/ralph/*`

Execution requirements:
1. Inspect current dirty diff first. Do not revert existing changes.
2. Map current merchant-context flow through merchant list, selected merchant state, request utilities, and page navigation.
3. Preserve `merchantId` as preferred field and `shopId` as compatibility alias unless backend contract changes.
4. Audit public browse requests and ensure intended unauthenticated browse still works.
5. Audit private user flows: add/list/clean cart, submit order, pay, detail, history, reorder.
6. Review token storage, `authentication` header, 401 handling, and redirect behavior.
7. Review user-facing errors for missing merchant, closed merchant, unavailable product, empty cart, and order submit failure.
8. Document whether user-side WebSocket/notification is active, partial, or future work.

Suggested verification:
- Manual flow: select merchant -> open menu -> add item -> cart -> submit order.
- Manual flow: switch merchant -> verify cart scope does not bleed.
- Manual flow: public browse without token where supported.
- If a browser/E2E path exists: `npx playwright test` after coordinator/Agent 6 prepares services.

Hard constraints:
- Do not rewrite miniapp to another framework.
- Do not edit backend logic directly.
- Do not commit generated `unpackage/` files unless coordinator asks.
- Be careful with typo-preserving `webscoket.js`; renaming may break imports.
- Do not install packages or create caches on C drive.

Final response format:
## Delivery Summary
- Scope handled:
- Files changed:
- Manual flows checked:
- Automated tests run:
- Test result:
- Request contract notes:
- Acceptance criteria satisfied:
- Remaining risks:
- Coordination requests:
```

