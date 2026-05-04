# Multi-Merchant Isolation Ralph Loop Plan

## Goal
Harden multi-merchant tenant isolation across core business modules while preserving public cross-merchant browsing and enforcing strict ownership for private user and merchant-admin flows.

## Current Mode
Ralph Loop: story-by-story ECC-style execution with test evidence per story and gate evidence before any completion claim.

## Stories
- [x] US-001 Ralph control plane for isolation hardening
- [ ] US-002 Merchant scope guard abstraction and baseline tests
- [ ] US-003 Admin domain isolation for employee and catalog management
- [ ] US-004 Private user-chain isolation for cart and order flows
- [ ] US-005 Merchant-scoped reporting and workspace statistics
- [ ] US-006 Public browsing regression protection
- [ ] US-007 ECC verification and Ralph gate evidence

## Required Gates
- [x] G-001 Ralph PRD conversion initialized.
- [x] G-002 Previous Ralph run archived before overwrite.
- [ ] G-003 Merchant-scope guard enforced across core modules.
- [ ] G-004 Private user chain explicit merchant binding + same-merchant checks.
- [ ] G-005 Merchant-scoped reporting/workspace behavior verified.
- [ ] G-006 Public cross-merchant browsing regression verified.
- [ ] G-007 ECC verification suite passes (`mvn test` + Playwright E2E).
- [ ] G-008 Stories and memory files fully synchronized with evidence.

## Decisions
- Public browsing remains cross-merchant by product rule.
- Private user flows require explicit merchant binding in multi-merchant mode.
- Merchant admin accounts cannot access or mutate other merchants' resources.
- Platform admin global visibility is preserved where endpoint semantics already allow it.

## Active Iteration
US-001 complete. Ready to start US-002 implementation with tests-first execution.

## Risks
| Risk | Impact | Mitigation |
|------|--------|------------|
| Isolation logic remains scattered in service methods | Medium | Introduce a shared merchant-scope guard and migrate high-risk paths first. |
| Hardening private flows accidentally blocks public browse | High | Add dedicated regression tests for public shop/category/dish/setmeal browsing. |
| Reporting aggregation leaks global numbers to merchant admin | High | Add merchant-scoped filters and explicit tests for report/workspace endpoints. |
