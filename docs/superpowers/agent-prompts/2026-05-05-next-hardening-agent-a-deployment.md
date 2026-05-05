# Agent A Prompt: Deployment Hardening

```markdown
You are Agent A for Sky Delivery Next Iteration Hardening.

Workstream: Deployment Hardening.

## Goal

Move deployment materials from "proposed / audited" toward "verified / explicitly blocked with reason". Focus on Docker Compose, Dockerfile, startup scripts, environment templates, health checks, and deployment docs.

## Required Reading

- `docs/reviews/2026-05-05-next-iteration-handoff.md`
- `docs/reviews/2026-05-05-comprehensive-upgrade-final-report.md`
- `docs/deployment/docker.md`
- `docs/deployment/troubleshooting.md`
- `docs/superpowers/plans/2026-05-05-ecc-skill-usage-matrix.md`
- `docs/superpowers/plans/2026-05-05-next-iteration-hardening-gates.md`

## Required Skills / Norms

Use aggressively where applicable:

- `ecc-java-build-resolver` for Maven, Spring Boot build, Docker Java build, dependency, and toolchain failures.
- `springboot-verification` for build/test/coverage/security/diff verification.
- Docker/deployment patterns for Compose, health checks, volumes, secrets, and startup ordering.
- Spring Boot deployment conventions for profiles, health endpoints, and environment variables.

If a skill is unavailable, state that and apply the closest documented norm manually.

## Owned Files

You may modify:

- `README.md`
- `SETUP.md`
- `.env.example`
- `tooling/.env.example`
- `scripts/start-all.bat`
- `scripts/stop-all.bat`
- `core/backend/deploy/`
- `docs/deployment/`
- deployment evidence docs under `docs/reviews/`

## Forbidden Without Main Thread Approval

- Real `.env`
- Java business logic
- database migration SQL
- `scripts/ralph/*`
- `task_plan.md`, `findings.md`, `progress.md`
- generated `target/` artifacts

## Tasks

1. Inspect current Dockerfile, Compose, `.dockerignore`, README, SETUP, env examples, and startup scripts.
2. Check whether Compose references actual built artifact names and health endpoints.
3. If Docker daemon is available, run the smallest safe validation for Compose and record evidence.
4. If Docker daemon is unavailable, mark Docker validation `blocked` with exact reason and do not fake a pass.
5. Validate that no real secrets are introduced.
6. Validate that no C-drive temp/cache/generated outputs are introduced.
7. Update deployment docs or evidence docs only as needed.

## Verification

Prefer smallest safe commands:

- Maven package if needed: `mvn -f core/backend/pom.xml -pl sky-server -am package -DskipTests`
- Docker validation only if daemon is available.
- Health curl only if services are running.

Do not turn missing Docker/services into project failure. Classify as `blocked`.

## Delivery Summary

Return:

```markdown
## Agent A Delivery Summary

- Scope handled:
- Files changed:
- Commands run:
- Results:
- Docker status: verified / blocked / not-official-path
- Health-check status:
- Skills / Norms Used:
  - Skill or norm:
  - Where it affected decisions:
  - Verification performed because of it:
- Acceptance criteria satisfied:
- Remaining risks:
- Coordination requests:
```
```

