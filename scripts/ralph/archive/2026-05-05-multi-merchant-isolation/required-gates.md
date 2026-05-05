# Ralph Required Gates

## Gate Status

| Gate | Status | Evidence |
|------|--------|----------|
| G-001 Ralph PRD conversion initialized | pass | `tasks/prd-multi-merchant-isolation.md` and `scripts/ralph/prd.json` regenerated for branch `ralph/multi-merchant-isolation-hardening`. |
| G-002 Previous Ralph run archived before overwrite | pass | `scripts/ralph/archive/2026-05-05-thesis-upgrade/prd.json` and `progress.txt` created. |
| G-003 Merchant-scope guard is enforced across core modules | pending | Pending implementation/test evidence from US-002 and US-003. |
| G-004 Private user chain requires explicit merchant and same-merchant consistency | pending | Pending implementation/test evidence from US-004. |
| G-005 Merchant-scoped reporting/workspace metrics are verified | pending | Pending implementation/test evidence from US-005. |
| G-006 Public cross-merchant browsing regression is preserved | pending | Pending implementation/test evidence from US-006. |
| G-007 ECC verification suite passes | pending | Pending `mvn -f core/backend/pom.xml test` and `npx playwright test` evidence from US-007. |
| G-008 Ralph stories and memory files are fully synchronized | pending | Pending all `userStories[].passes=true` plus final updates to `task_plan.md`, `findings.md`, `progress.md`, and `scripts/ralph/progress.txt`. |

## Required Final Check Procedure

Before any final readiness statement:

1. Read `scripts/ralph/prd.json` and verify every `userStories[].passes` value is `true`.
2. Read this file and verify every gate has passing evidence.
3. Read `task_plan.md`, `findings.md`, and `progress.md` and confirm they reflect the latest iteration.
4. Run `git status --short` and confirm no unintended source-code regressions.
5. If any gate is not passing, record blockers in `scripts/ralph/progress.txt` and continue the next Ralph iteration.
