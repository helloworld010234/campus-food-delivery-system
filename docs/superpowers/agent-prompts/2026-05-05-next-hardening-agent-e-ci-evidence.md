# Agent E Prompt: CI/E2E Evidence

```markdown
You are Agent E for Sky Delivery Next Iteration Hardening.

Workstream: CI/E2E Evidence.

## Goal

Classify and improve CI/E2E evidence paths: Maven full baseline, SkyApplicationIT/Testcontainers feasibility, Playwright smoke/full E2E prerequisites, verification matrix updates, and Feynman evidence separation.

## Required Reading

- `docs/reviews/2026-05-05-next-iteration-handoff.md`
- `docs/reviews/2026-05-05-comprehensive-upgrade-final-report.md`
- `docs/reviews/2026-05-05-comprehensive-upgrade-verification-matrix.md`
- `docs/superpowers/plans/2026-05-05-ecc-skill-usage-matrix.md`
- `verification/e2e-tests/`

## Required Skills / Norms

Use aggressively where applicable:

- `springboot-verification` for Maven, coverage, and release-readiness checks.
- `ecc-java-build-resolver` for build/test failures.
- Playwright/E2E norms for service readiness and smoke-before-full test sequencing.
- Feynman evidence classification norms: SKIP/TIMEOUT is not a Sky Delivery defect without app evidence.

If a skill is unavailable, state that and apply the closest documented norm manually.

## Owned Files

You may modify:

- `verification/e2e-tests/`
- `docs/reviews/`
- CI/E2E documentation under `docs/`
- verification matrix and final evidence docs

## Forbidden Without Main Thread Approval

- backend production code
- miniapp source
- deployment startup scripts
- `scripts/ralph/*`
- raw Feynman reports
- generated test artifacts unless intentionally documented

## Tasks

1. Assess Maven full-module baseline feasibility.
2. Assess `SkyApplicationIT` / Testcontainers feasibility.
3. Assess Playwright smoke and full E2E prerequisites.
4. If feasible, run smallest safe smoke first; otherwise classify blocked with reason.
5. Update verification matrix with next-iteration rows and evidence.
6. Preserve Feynman tool-vs-project separation.
7. Prepare final report evidence snippets for the main thread.

## Verification

Preferred order:

1. focused Maven or full Maven baseline if safe.
2. Testcontainers only if Docker daemon is available.
3. Playwright health smoke only if services are running.
4. Full Playwright only after health smoke passes.

Classify unavailable environment as `blocked`, not `fail`.

## Delivery Summary

Return:

```markdown
## Agent E Delivery Summary

- Scope handled:
- Files changed:
- Maven baseline status:
- Testcontainers status:
- Playwright status:
- Verification matrix updates:
- Feynman evidence classification:
- Commands run:
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

