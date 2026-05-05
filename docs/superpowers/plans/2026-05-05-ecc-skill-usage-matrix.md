# ECC Skill Usage Matrix For Next Iteration Hardening

日期：2026-05-05

## 1. Purpose

This matrix tells the future Claude Code execution which ECC/Spring/Java skills or norms to use for each next-iteration workstream.

If the exact skill is unavailable in the future runtime, the agent must state that and apply the closest local standard manually.

## 2. Global Requirement

Every worker must include this section in its delivery summary:

```markdown
## Skills / Norms Used

- Skill or norm:
- Where it affected decisions:
- Verification performed because of it:
```

## 3. Workstream Matrix

| Agent | Workstream | Required Skills / Norms | Apply To |
|---|---|---|---|
| Agent A | Deployment Hardening | `ecc-java-build-resolver`, `springboot-verification`, deployment patterns, docker patterns | Maven build, Dockerfile, Compose, health checks, startup scripts |
| Agent B | Miniapp Anonymous Browse & Cleanup | API contract discipline, public/private auth boundary, frontend request-wrapper review, Playwright smoke norms | `request.js`, miniapp pages, public browse routes, 401 behavior |
| Agent C | Quality Guardrails | `ecc-java-reviewer`, `java-coding-standards`, `springboot-tdd`, `springboot-verification`, `springboot-security` | Java cleanup, tests, guard usage checklist, mock payment protection |
| Agent D | Migration & Database Safety | `database-migrations`, MyBatis SQL review norms, `springboot-patterns`, `springboot-verification` | migration SQL, mapper assumptions, database docs |
| Agent E | CI/E2E Evidence | `springboot-verification`, `ecc-java-build-resolver`, Playwright norms, Feynman evidence classification | Maven full baseline, Testcontainers, Playwright, verification matrix |

## 4. Skill Application Details

### ecc-java-reviewer

Use when reviewing Java, Spring Boot, Maven, security, transaction, controller, service, mapper, or Java test changes.

Minimum application:

- Inspect `git diff -- '*.java'`.
- Check security, layering, transactions, concurrency, exception handling, and tests.
- Findings must lead delivery summary if issues exist.

### ecc-java-build-resolver

Use when Maven, Java compilation, dependency, annotation processor, or JVM toolchain failures occur.

Minimum application:

- Run the smallest relevant failing command.
- Parse the first real error.
- Apply the smallest targeted fix.
- Re-run the original failing command.

### springboot-tdd

Use for Java bug fixes and guardrails.

Minimum application:

- Write or identify failing test first when behavior changes.
- Implement minimal code.
- Refactor only after green.
- Prefer JUnit 5, Mockito, AssertJ, MockMvc where appropriate.

### springboot-verification

Use before declaring any Spring Boot work ready.

Minimum application:

- Build or focused compile when feasible.
- Run focused tests.
- Classify blocked integration checks precisely.
- Review diff before final claim.

### java-coding-standards

Use for Java cleanup.

Minimum application:

- Preserve naming clarity.
- Keep exceptions meaningful.
- Avoid broad refactors.
- Avoid exposing implementation details.

### springboot-security

Use for auth/public/private boundary work.

Minimum application:

- Preserve private route protection.
- Avoid secret leakage.
- Keep authorization checks server-side.
- Public browse changes must not weaken cart/order/payment routes.

### database-migrations

Use for migration script review.

Minimum application:

- Classify rerun safety.
- Identify destructive operations.
- Document rollback or recovery.
- Check compatibility with existing seeded data.

### Playwright / E2E Norms

Use for browser/API smoke design.

Minimum application:

- Start with smallest smoke.
- Separate service-unavailable from test failure.
- Record URLs, commands, and artifacts.

## 5. Final Report Requirement

The final Ralph-style report must include a table:

| Agent | Skills / Norms Used | Verification Impact |
|---|---|---|

No agent may omit this table row.

