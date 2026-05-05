# Sky Take Out — Deployment Documentation Index

This directory holds deployment-related documentation owned by Agent 5
(Deployment & Engineering) of the Sky Delivery comprehensive upgrade.

## Topics

| File | Purpose |
|---|---|
| [`database-migrations.md`](./database-migrations.md) | Database initialization order, multi-merchant migration, rerun safety |
| [`troubleshooting.md`](./troubleshooting.md) | Common startup, env, Redis, MySQL, JWT, port-conflict, schema-mismatch issues |
| [`docker.md`](./docker.md) | Docker / Compose proposal (not yet executed against this repo) |

## Related artifacts

- Repository root: [`README.md`](../../README.md), [`SETUP.md`](../../SETUP.md)
- Env templates: [`.env.example`](../../.env.example), [`tooling/.env.example`](../../tooling/.env.example)
- Startup scripts: [`scripts/start-all.bat`](../../scripts/start-all.bat), [`scripts/stop-all.bat`](../../scripts/stop-all.bat)
- Server templates: [`core/backend/deploy/`](../../core/backend/deploy/) (systemd, nginx, application-prod, Docker proposal)

## Status of Docker materials

Status as of 2026-05-05:

- [`Dockerfile`](../../core/backend/deploy/Dockerfile): **proposed**, not validated by Agent 5.
- [`docker-compose.yml`](../../core/backend/deploy/docker-compose.yml): **proposed**, not validated.
- Health checks reference the existing endpoint
  `GET /internal/health` (see `core/backend/sky-server/src/main/java/com/sky/controller/HealthController.java`).
  No Spring Boot Actuator dependency is in scope right now.

## C-drive avoidance

All deployment artifacts created by Agent 5 live under `D:\sky-delivery`.
Maven users with C-drive pressure should redirect their local repo:

```
mvn -Dmaven.repo.local=D:\m2-repo ...
```

or set `<localRepository>D:\m2-repo</localRepository>` inside
`%USERPROFILE%\.m2\settings.xml`.

The startup batch files do not write to `%TEMP%`, `%LOCALAPPDATA%`, or
any other C-drive directory; logs go to `core\nginx\logs\` and the
spawned backend `cmd` window.
