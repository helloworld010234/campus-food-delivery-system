# Agent 02 Brief: 订单与交易链路

日期：2026-05-05

## 1. Mission

Agent 2 负责购物车、下单、支付边界、订单状态、复购、催单和订单明细一致性。目标是在多商户场景下保证私有用户链路明确绑定商户，订单数据不跨用户、不跨商户、不破坏事务一致性。

## 2. Background

订单链路是校园外卖系统的核心闭环。当前项目存在多商户兼容逻辑、模拟支付路径、WebSocket 订单通知和用户历史订单等功能。升级重点不是重写交易系统，而是把商户上下文、用户归属和事务边界变得明确可测。

## 3. Input Materials

- `docs/superpowers/specs/2026-05-05-sky-delivery-comprehensive-upgrade-design.md`
- `docs/superpowers/agent-briefs/2026-05-05-agent-01-backend-security.md`
- `docs/reviews/2026-05-05-feynman-report-upgrade-inputs.md`
- `docs/superpowers/specs/2026-05-05-multi-merchant-isolation-design.md`
- `D:\feynman-test-2026-05-05\outputs\source-comparison\comparison.md`

## 4. Primary File Ownership

Primary write scope:

- `core/backend/sky-server/src/main/java/com/sky/service/impl/ShoppingCartServiceImpl.java`
- `core/backend/sky-server/src/main/java/com/sky/service/impl/OrderServiceImpl.java`
- cart/order-focused tests under `core/backend/sky-server/src/test/java/com/sky/service/impl/`
- order/cart mapper tests if needed

Coordinate before editing:

- `core/backend/sky-server/src/main/java/com/sky/security/`
- admin report/workspace services
- miniapp request utilities
- shared mapper methods used by Agent 3

## 5. Forbidden Scope

Agent 2 must not:

- implement real production WeChat Pay unless separately approved.
- remove mock payment behavior that the current project relies on.
- rewrite the order state machine wholesale.
- change frontend request contracts without coordinating with Agent 4.
- modify security guard semantics directly unless assigned by coordinator.
- edit generated `target/` or miniapp `unpackage/` artifacts.

## 6. Execution Tasks

1. Audit shopping cart operations:
   - add item.
   - subtract item.
   - list cart.
   - clean cart.
   - merchant id/shop id compatibility.
2. Enforce private cart scope:
   - current user id.
   - explicit merchant id in multi-merchant mode.
   - item belongs to selected merchant.
   - clean only current merchant cart.
3. Audit order submission:
   - cart items loaded by user and merchant.
   - dish/setmeal ownership.
   - address ownership.
   - order total and detail creation.
   - cart cleanup after submit.
4. Audit order operations:
   - payment.
   - history list.
   - detail.
   - cancel.
   - repetition/reorder.
   - reminder.
5. Review transaction boundaries:
   - order submit should create order and details atomically.
   - cart cleanup should not affect other merchants.
   - payment status update should not create inconsistent state.
6. Add focused tests for mixed-merchant prevention, user ownership, transaction behavior, and missing merchant context.

## 7. Execution-Stage Notes

- Start by reading Agent 1's current guard semantics if implementation has begun. Do not duplicate guard logic locally.
- If Agent 1 has not finished, design tests around expected guard behavior and mark the dependency clearly.
- Keep legacy single-merchant fallback behavior intact when schema support says merchant columns are unavailable.
- Do not silently default to the first merchant for private writes in multi-merchant-ready mode unless the product rule explicitly allows it.
- Make cross-merchant failure deterministic. Avoid behavior where mapper ordering decides which merchant gets used.
- Be careful with `shopId` compatibility: use it as an alias only where legacy clients require it, not as a separate source of truth.
- Ensure shopping cart cleanup after order submit uses the same merchant scope used to load the cart.
- Reorder should write items back into the original order's merchant cart, not the currently browsed merchant unless they match.
- Avoid changing public catalog browse behavior while hardening private order flows.
- If payment logic uses mock amount or mock status transitions, document that boundary; do not present it as production payment.
- If tests need database state, prefer mock/unit tests first and leave integration fixtures to Agent 6 unless necessary.
- Do not touch admin reporting aggregations unless needed for order-owned mapper behavior; coordinate with Agent 3.
- Do not write temporary outputs to C drive.

## 8. Suggested Verification

Focused commands:

```powershell
mvn -f core/backend/pom.xml test -pl sky-server "-Dtest=*Order*Test,*ShoppingCart*Test"
```

Additional checks:

- Manual API check for user adding cart item with missing merchant id in multi-merchant mode.
- Manual/API check that clearing merchant A cart leaves merchant B cart intact.
- Manual/API check that merchant A product cannot be submitted under merchant B.

## 9. Acceptance Criteria

- Private cart operations are bound to `userId + merchantId`.
- Order submit validates same-merchant cart, dish, setmeal, order, and detail data.
- User cannot access or mutate another user's order.
- Reorder and reminder preserve user and merchant ownership.
- Transactional behavior is tested or documented with a focused verification plan.

## 10. Delivery Summary Format

```markdown
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

