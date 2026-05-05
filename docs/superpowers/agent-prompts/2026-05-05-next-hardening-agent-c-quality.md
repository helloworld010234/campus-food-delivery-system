# Agent C Prompt: Quality Guardrails

```markdown
You are Agent C for Sky Delivery Next Iteration Hardening.

Workstream: Quality Guardrails.

## Goal

Install guardrails that prevent future regressions: no bypassing `MerchantScopeGuard`, no accidental removal of mock WeChat Pay test path, and no needless propagation of mojibake exception text.

## Required Reading

- `docs/reviews/2026-05-05-next-iteration-handoff.md`
- `docs/reviews/2026-05-05-comprehensive-upgrade-final-report.md`
- `docs/superpowers/plans/2026-05-05-ecc-skill-usage-matrix.md`
- `core/backend/sky-server/src/main/java/com/sky/security/MerchantScopeGuard.java`
- `core/backend/sky-server/src/main/java/com/sky/utils/MerchantScopeUtils.java`
- `core/backend/sky-server/src/main/java/com/sky/service/impl/OrderServiceImpl.java`

## Required Skills / Norms

Use aggressively where applicable:

- `ecc-java-reviewer` for Java/Spring/security/service/test review.
- `java-coding-standards` for cleanup, exception text, naming, and minimal refactor discipline.
- `springboot-tdd` for behavior changes and regression tests.
- `springboot-verification` for focused Maven verification and diff review.
- `springboot-security` for authorization guardrail review.

If a skill is unavailable, state that and apply the closest documented norm manually.

## Owned Files

You may modify:

- `core/backend/sky-server/src/main/java/com/sky/utils/MerchantScopeUtils.java`
- `core/backend/sky-server/src/main/java/com/sky/service/impl/OrderServiceImpl.java` only for comments/tests protecting mock pay, not broad logic changes
- focused Java tests related to guardrails
- docs/checklists under `docs/`
- PR or review checklist docs if present or created

## Forbidden Without Main Thread Approval

- Rewriting `MerchantScopeGuard` public API
- changing real WeChat Pay behavior
- changing order state machine
- mapper SQL
- `scripts/ralph/*`
- generated `target/`

## Tasks

1. Inspect `MerchantScopeUtils` mojibake and clean text if safe.
2. Ensure tests assert exception types/behavior, not fragile localized strings.
3. Add or update guardrail docs/checklist requiring new service code to use `MerchantScopeGuard`.
4. Protect mock WeChat Pay branch with comment and/or focused test if not already sufficient.
5. Run focused tests for impacted Java files.
6. Perform ECC Java review-style diff review.

## Verification

Focused commands should be smallest safe Maven tests, for example:

- `mvn -f core/backend/pom.xml test -pl sky-server "-Dtest=*Scope*Test,*Order*Test"`

If build/test fails, use `ecc-java-build-resolver` workflow: smallest failing command, first real error, smallest fix, rerun.

## Delivery Summary

Return:

```markdown
## Agent C Delivery Summary

- Scope handled:
- Files changed:
- Mojibake status:
- Guardrail docs/checklist:
- Mock pay protection:
- Tests run:
- Results:
- ECC review findings:
- Skills / Norms Used:
  - Skill or norm:
  - Where it affected decisions:
  - Verification performed because of it:
- Acceptance criteria satisfied:
- Remaining risks:
- Coordination requests:
```
```

