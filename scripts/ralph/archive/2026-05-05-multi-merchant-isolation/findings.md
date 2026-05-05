# Multi-Merchant Isolation Findings

## Active Evidence Sources

- Approved design spec: `D:\sky-delivery\docs\superpowers\specs\2026-05-05-multi-merchant-isolation-design.md`
- Source PRD for conversion: `D:\sky-delivery\tasks\prd-multi-merchant-isolation.md`
- Converted Ralph plan: `D:\sky-delivery\scripts\ralph\prd.json`
- Existing backend scope components:
  - `core/backend/sky-common/src/main/java/com/sky/context/BaseContext.java`
  - `core/backend/sky-server/src/main/java/com/sky/utils/MerchantScopeUtils.java`
  - `core/backend/sky-server/src/main/java/com/sky/support/MultiMerchantSchemaSupport.java`
  - `core/backend/sky-server/src/main/java/com/sky/interceptor/JwtTokenAdminInterceptor.java`
  - `core/backend/sky-server/src/main/java/com/sky/interceptor/JwtTokenUserInterceptor.java`

## High-Risk Areas Identified

| Area | Risk |
|------|------|
| Shopping cart private flows | Missing/implicit merchant context can cause cross-merchant or overbroad cart operations. |
| Order submit/repetition chain | Merchant consistency across cart, dish, setmeal, and order ownership must be explicit. |
| Reporting user statistics | Merchant account can potentially view global data if merchant filters are absent. |
| Admin scoped writes | Some mapper operations remain id-based and rely on service-layer ownership checks. |
| Public browse regression | Isolation hardening may accidentally block cross-merchant public browse endpoints. |

## Existing Strengths to Reuse

- Service-layer ownership checks already exist in several modules (`getAccessible*` patterns).
- JWT admin interceptor sets `merchantId` and `accountType` into `BaseContext`.
- Multi-merchant schema capability flags are already available for compatibility handling.

## Current Ralph State

- US-001 complete: control plane reset and conversion done.
- US-002..US-007 pending implementation and evidence.
- Previous thesis run archived under `scripts/ralph/archive/2026-05-05-thesis-upgrade/`.
