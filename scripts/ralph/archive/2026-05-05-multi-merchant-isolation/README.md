# Archive: 2026-05-05 Multi-Merchant Isolation Ralph

This folder snapshots the multi-merchant isolation Ralph loop before it was absorbed
into the broader **Sky Delivery Comprehensive Upgrade** loop on 2026-05-05.

## Why archived

The multi-merchant专项 was one of six workstreams in the comprehensive upgrade design
(`docs/superpowers/specs/2026-05-05-sky-delivery-comprehensive-upgrade-design.md`).
Per `docs/superpowers/plans/2026-05-05-comprehensive-upgrade-ralph-blueprint.md`,
the previous Ralph artifacts must be archived (not discarded) and absorbed under
**Agent 1 — Backend Security and Permission Boundaries**.

## What was archived

- `prd.json` — multi-merchant isolation PRD with US-001..US-007 (US-001 + US-002 already
  marked as `passes=true`, evidence: `MerchantScopeGuard` + tests + reuse in
  `ReportServiceImpl` and `WorkspaceServiceImpl`).
- `progress.txt` — Ralph progress log up to US-002 completion.
- `required-gates.md` — gate tracker (G-001 + G-002 pass; G-003..G-008 pending).
- `task_plan.md` — story checklist for the multi-merchant专项.
- `findings.md` — evidence sources, high-risk areas, existing strengths.
- `progress.md` — operational log of the multi-merchant专项 setup.

## Absorption notes

- The work in `core/backend/sky-server/src/main/java/com/sky/security/MerchantScopeGuard.java`
  and `core/backend/sky-server/src/test/java/com/sky/security/MerchantScopeGuardTest.java`
  remains in the working tree.
- The earlier guard reuse in `ReportServiceImpl` and `WorkspaceServiceImpl` remains in the
  working tree; Agent 3 must preserve and extend it rather than reverting it.
- The unit test `MerchantServiceImplScopeTest.java` likewise remains and is preserved.
- These artifacts feed Agent 1 (security/scope) and Agent 3 (admin reporting) directly.

## Successor

- `tasks/prd-sky-delivery-comprehensive-upgrade.md`
- `scripts/ralph/prd.json` (regenerated for branch
  `ralph/sky-delivery-comprehensive-upgrade`).
