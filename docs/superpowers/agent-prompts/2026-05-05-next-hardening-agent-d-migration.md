# Agent D Prompt: Migration & Database Safety

```markdown
You are Agent D for Sky Delivery Next Iteration Hardening.

Workstream: Migration & Database Safety.

## Goal

Classify multi-merchant migration rerun safety, check MyBatis mapper assumptions against migration-added columns, and update database migration documentation with evidence.

## Required Reading

- `docs/reviews/2026-05-05-next-iteration-handoff.md`
- `docs/reviews/2026-05-05-comprehensive-upgrade-final-report.md`
- `docs/superpowers/plans/2026-05-05-ecc-skill-usage-matrix.md`
- `core/backend/scripts/phase1_multi_merchant_schema.sql`
- mapper interfaces and XML under `core/backend/sky-server/src/main/`
- database docs if present under `docs/`

## Required Skills / Norms

Use aggressively where applicable:

- `database-migrations` for idempotency, rollback, rerun safety, and zero-downtime cautions.
- MyBatis SQL review norms for mapper assumptions and dynamic filters.
- `springboot-patterns` for service/mapper responsibility boundaries.
- `springboot-verification` for focused tests or compile checks.
- `ecc-java-reviewer` if Java mapper/service code is reviewed or changed.

If a skill is unavailable, state that and apply the closest documented norm manually.

## Owned Files

You may modify:

- database migration docs under `docs/`
- `core/backend/scripts/phase1_multi_merchant_schema.sql` only if a minimal safe fix is needed and rollback/recovery is documented
- mapper docs/evidence under `docs/reviews/`
- focused mapper or migration tests if introduced

## Forbidden Without Main Thread Approval

- destructive schema changes
- production data deletion
- broad service rewrites
- `scripts/ralph/*`
- generated `target/`
- real database credentials

## Tasks

1. Inspect `phase1_multi_merchant_schema.sql` for idempotency.
2. Classify each migration operation: safe rerun, unsafe rerun, conditional, destructive, seed-only.
3. Check whether mapper queries assume columns added by the migration.
4. Check whether service fallback behavior matches migration-ready vs legacy schema.
5. Update docs to state actual rerun safety and recovery steps.
6. If changing SQL, include rollback/recovery notes and focused verification.

## Verification

Use static SQL review first. If a database is available, run non-destructive validation only. If database tooling is missing, mark live validation `blocked` with exact reason.

Do not invent DB verification evidence.

## Delivery Summary

Return:

```markdown
## Agent D Delivery Summary

- Scope handled:
- Files changed:
- Idempotency classification:
- Mapper assumptions:
- Live DB validation: pass / blocked / not run
- Commands or checks run:
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

