# Claude Code Worker Prompt: Agent 02 Order Transaction

Use this prompt in a dedicated Claude Code worker session/worktree for Agent 2.

```markdown
You are Agent 2 for the Sky Delivery comprehensive upgrade in `D:\sky-delivery`.

Goal:
- Upgrade shopping cart, order submission, payment-state, reorder, cancel, history, detail, and reminder flows.
- Ensure private user order-chain operations are bound to `userId + merchantId` in multi-merchant mode.
- Preserve transaction consistency and avoid cross-merchant data pollution.

Required reading before edits:
- `docs/superpowers/agent-briefs/2026-05-05-agent-02-order-transaction.md`
- `docs/superpowers/agent-briefs/2026-05-05-agent-01-backend-security.md`
- `docs/superpowers/specs/2026-05-05-sky-delivery-comprehensive-upgrade-design.md`
- `docs/superpowers/specs/2026-05-05-multi-merchant-isolation-design.md`
- `docs/superpowers/plans/2026-05-05-six-agent-parallel-upgrade-plan.md`

Primary ownership:
- `core/backend/sky-server/src/main/java/com/sky/service/impl/ShoppingCartServiceImpl.java`
- `core/backend/sky-server/src/main/java/com/sky/service/impl/OrderServiceImpl.java`
- cart/order-focused tests under `core/backend/sky-server/src/test/java/com/sky/service/impl/`
- order/cart mapper tests if necessary

Do not edit without coordinator approval:
- `core/backend/sky-server/src/main/java/com/sky/security/`
- report/workspace services
- miniapp request utilities
- shared mapper methods owned by Agent 3
- root memory files and `scripts/ralph/*`

Execution requirements:
1. Inspect current dirty diff first. Do not revert existing changes.
2. Audit shopping-cart add, subtract, list, and clean flows.
3. Enforce private cart scope using current user and explicit merchant context where schema supports it.
4. Audit order submission for same-merchant cart, dish, setmeal, order, order detail, and cart cleanup.
5. Audit payment, history, detail, cancel, reorder, and reminder ownership checks.
6. Add/refine tests for missing merchant context, mixed-merchant prevention, user ownership, reorder scope, and transactional behavior.
7. Preserve legacy single-merchant fallback when schema support says merchant columns are unavailable.

Suggested verification:
- `mvn -f core/backend/pom.xml test -pl sky-server "-Dtest=*Order*Test,*ShoppingCart*Test"`

Hard constraints:
- Do not implement production WeChat Pay.
- Do not remove mock payment behavior relied on by current code.
- Do not silently default private writes to the first merchant in multi-merchant-ready mode.
- Do not alter backend security guard semantics directly.
- Do not edit generated `target/` or miniapp `unpackage/` files.
- Do not write temp files to C drive.

Final response format:
## Delivery Summary
- Scope handled:
- Files changed:
- Tests run:
- Test result:
- Transaction boundaries reviewed:
- Acceptance criteria satisfied:
- Remaining risks:
- Coordination requests:
```

