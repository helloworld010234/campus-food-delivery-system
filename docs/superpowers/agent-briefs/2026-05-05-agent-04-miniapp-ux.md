# Agent 04 Brief: 小程序与用户体验

日期：2026-05-05

## 1. Mission

Agent 4 负责 uni-app 小程序端用户体验和接口契约。目标是让商户选择、菜单浏览、购物车、地址、下单、支付、订单详情和历史订单在用户侧表现一致，并与后端商户上下文规则对齐。

## 2. Background

Feynman source comparison显示小程序使用 uni-app/Vue 2，`request.js` 注入 `authentication` header，`merchant.js` 负责 `merchantId` 和 `shopId` 兼容。用户端既有公开浏览，也有需要登录和商户上下文的私有链路。

## 3. Input Materials

- `docs/superpowers/specs/2026-05-05-sky-delivery-comprehensive-upgrade-design.md`
- `docs/superpowers/agent-briefs/2026-05-05-agent-01-backend-security.md`
- `docs/superpowers/agent-briefs/2026-05-05-agent-02-order-transaction.md`
- `D:\feynman-test-2026-05-05\outputs\source-comparison\comparison.md`
- `miniapp-preview.png`
- `miniapp-preview.json`

## 4. Primary File Ownership

Primary write scope:

- `core/miniapp/pages/`
- `core/miniapp/utils/request.js`
- `core/miniapp/utils/merchant.js`
- `core/miniapp/utils/session.js`
- `core/miniapp/utils/webscoket.js`
- miniapp-facing API contract docs if created

Coordinate before editing:

- backend controllers and services
- E2E tests owned by Agent 6
- static admin frontend
- generated `core/miniapp/unpackage/` output

## 5. Forbidden Scope

Agent 4 must not:

- rewrite the miniapp to a different framework.
- edit backend security or order logic directly.
- make visual redesign the main task unless explicitly approved.
- commit generated miniapp build output unless the coordinator asks for it.
- remove compatibility support for `shopId` without backend coordination.
- introduce new package installs into C drive.

## 6. Execution Tasks

1. Audit miniapp merchant context flow:
   - merchant list.
   - selected merchant state.
   - `merchantId` and `shopId` injection.
   - page navigation carrying merchant context.
2. Audit public browsing:
   - shop list/status.
   - category list.
   - dish list.
   - setmeal list.
3. Audit private user flows:
   - add cart.
   - list cart.
   - clean cart.
   - submit order.
   - payment.
   - order detail.
   - history orders.
   - reorder.
4. Review login/session behavior:
   - token storage.
   - `authentication` header.
   - 401 handling.
   - redirect behavior.
5. Review user-facing errors:
   - missing merchant.
   - merchant closed.
   - product unavailable.
   - cart empty.
   - order submit failure.
6. Clarify WebSocket or notification boundary:
   - current file exists.
   - whether user-side push is active.
   - whether this is future work or current feature.

## 7. Execution-Stage Notes

- Start by mapping the current request utility flow before changing pages. Many bugs will be solved centrally in `merchant.js` or `request.js`.
- Preserve `merchantId` as the preferred field and `shopId` as compatibility alias unless the backend contract changes.
- Public browsing should not require login unless product rules say so.
- Private operations should not proceed without selected merchant context in multi-merchant mode. The UI should fail early with a clear message where possible.
- Avoid changing backend endpoint paths from the client side without coordinating with Agent 1 or Agent 2.
- Do not assume generated files under `unpackage/` are source of truth. Prefer source pages and utils.
- Be careful with typo-preserving files such as `webscoket.js`; renaming may break imports. If renaming is desired, coordinate and search all references.
- Keep UI changes modest unless a separate visual redesign is approved. This upgrade is primarily contract and reliability hardening.
- Use existing uni-app patterns and existing project utilities.
- If browser or simulator verification is not available, document manual WeChat developer tool steps.
- Coordinate with Agent 6 for E2E/API coverage rather than duplicating test scaffolding.
- Do not write temporary outputs to C drive.

## 8. Suggested Verification

Manual checks:

- Select merchant, open menu, add dish to cart, submit order.
- Switch merchant and verify cart scope does not bleed.
- Access public browse without token where supported.
- Expired/missing token redirects or reports error consistently for private flow.

Potential automated checks:

```powershell
npx playwright test
```

Use only if Agent 6 or coordinator has prepared a miniapp-compatible test path.

## 9. Acceptance Criteria

- Miniapp private requests consistently carry merchant context.
- Public browsing remains accessible under intended rules.
- Login/session failure behavior is reviewed.
- User-facing errors are mapped.
- WebSocket/notification boundary is documented.

## 10. Delivery Summary Format

```markdown
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

