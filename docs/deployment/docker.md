# Docker / Compose Proposal

> **Status as of 2026-05-05:** PROPOSED. The Dockerfile and
> `docker-compose.yml` under `core/backend/deploy/` build cleanly in
> design but have **not been validated end-to-end against this repo by
> Agent 5**. Treat the artifacts as a starting point. Any
> "implemented + verified" claim must come from a follow-up gate run.

This document describes the container layout, image build expectations,
runtime contract, secrets handling, and known gaps for the proposed
Docker workflow.

## 1. Service inventory

| Service | Image | Purpose |
|---|---|---|
| `mysql` | `mysql:8.0` | Single-instance database. First-boot init scripts seed `init.sql` then `phase1_multi_merchant_schema.sql`. |
| `redis` | `redis:7-alpine` | Cache + session store. Optionally enables `requirepass`. |
| `sky-server` | built from `core/backend/deploy/Dockerfile` | Spring Boot backend, port 8080. |

The Vue admin (Nginx static) and the uni-app miniapp are **out of scope**
for this Compose file. Agent 5 owns backend deployment only.

## 2. Build expectations

The Dockerfile (`core/backend/deploy/Dockerfile`) is a multi-stage build:

1. Stage `builder` uses `maven:3.9-eclipse-temurin-17-alpine`,
   resolves dependencies offline, then runs
   `mvn -B -pl sky-server -am clean package -DskipTests`.
2. Stage runtime uses `eclipse-temurin:17-jre-alpine`,
   adds non-root user `sky`, copies `app.jar`, and exposes port `8080`.
3. `HEALTHCHECK` calls `GET /internal/health` (provided by
   `HealthController.java`). Spring Boot Actuator is **not** on the
   classpath — do not change the path to `/actuator/health`.

Build context is the **`core/backend` directory**, not the repo root,
so the Dockerfile only sees the backend Maven multi-module tree.
`docker-compose.yml` sets `context: ../..` and explicitly names the
Dockerfile so the same layout works when invoked from the repo root.

## 3. Compose layout

```text
core/backend/deploy/docker-compose.yml
├── mysql      (volume: mysql_data,  init scripts mounted RO from repo)
├── redis      (volume: redis_data,  appendonly enabled)
└── sky-server (build context: core/backend)
```

Networks: a single bridge network `sky-net` connects all three
services. `sky-server` resolves DB and Redis by service name.

## 4. Volume strategy

The Compose file uses **named** volumes for `mysql_data`, `redis_data`,
and `sky_logs`. Docker Desktop on Windows places these under its data
root (typically the WSL2 VHDX, **not** `C:\Users\...`).

If you want to force volumes onto `D:\`, replace named volumes with bind
mounts under `D:\sky-delivery\runtime\`:

```yaml
volumes:
  - D:/sky-delivery/runtime/mysql:/var/lib/mysql
  - D:/sky-delivery/runtime/redis:/data
  - D:/sky-delivery/runtime/logs:/var/log/sky
```

`runtime/` is not currently in the repo and is git-ignored implicitly
(no `.gitkeep`). Create it before `docker compose up -d`.

## 5. Secrets handling

- All secrets come from the host `.env` file at the repo root.
- Compose is invoked with `--env-file .env` so the `${VAR}` syntax in
  `docker-compose.yml` resolves at startup.
- **Nothing is baked into the image.** Re-using the image on another
  host only requires a fresh `.env`.
- WeChat Pay PEMs (`apiclient_key.pem`, `apiclient_cert.pem`, etc.)
  are not part of the image. The commented-out volume mount in
  `docker-compose.yml` shows the recommended path:
  `D:\sky-delivery\secrets\wechat:/etc/sky/wechat:ro`.
  Update `WECHAT_PRIVATE_KEY_FILE_PATH` to `/etc/sky/wechat/apiclient_key.pem`
  (Linux path inside the container) when enabling.

## 6. Database initialization in containers

`mysql:8.0` runs files in `/docker-entrypoint-initdb.d/` in lexical order
**only on first boot of an empty data volume**. `docker-compose.yml`
mounts:

| Inside container | From repo |
|---|---|
| `/docker-entrypoint-initdb.d/01-init.sql` | `core/database/init.sql` |
| `/docker-entrypoint-initdb.d/02-multi-merchant.sql` | `core/backend/scripts/phase1_multi_merchant_schema.sql` |

Re-applying these on a populated volume requires
`docker compose down -v` (destroys local DB data) and a fresh `up`.

## 7. Health checks

| Service | Probe | Notes |
|---|---|---|
| `mysql` | `mysqladmin ping -h localhost -u root -p$DB_PASSWORD` | Recovers in ~30s; depends on init scripts. |
| `redis` | `redis-cli -a $REDIS_PASSWORD ping` (falls back to `redis-cli ping` when no password) | Conservative `start_period: 10s`. |
| `sky-server` | `wget -qO- http://localhost:8080/internal/health` | `start_period: 60s` because Spring Boot needs time to warm up. |

`/internal/health` is the **only** project-provided endpoint as of
2026-05-05. It returns `{"status":"UP", "service":"sky-backend",
"timestamp":"..."}` synchronously.

## 8. Usage

```batch
cd D:\sky-delivery
copy tooling\.env.example .env
notepad .env

docker compose -f core\backend\deploy\docker-compose.yml --env-file .env build
docker compose -f core\backend\deploy\docker-compose.yml --env-file .env up -d
docker compose -f core\backend\deploy\docker-compose.yml ps
docker compose -f core\backend\deploy\docker-compose.yml logs -f sky-server
```

Stop and clean:

```batch
docker compose -f core\backend\deploy\docker-compose.yml down

:: also remove DB / Redis / log volumes
docker compose -f core\backend\deploy\docker-compose.yml down -v
```

## 9. Validation checklist (not executed yet)

When Agent 5 (or a future gate) validates the Docker artifacts, all of
the following should pass:

- [ ] `docker compose build` completes without warnings.
- [ ] `docker compose up -d` brings all three services to `healthy`.
- [ ] `curl http://localhost:8080/internal/health` returns
  `{"status":"UP", ...}`.
- [ ] Admin login (`admin` / `123456`) succeeds when the Vue admin
  points at `http://localhost:8080`.
- [ ] Database schema includes `merchant`, `campus`, and
  `merchant_user` tables.
- [ ] Redis responds with `PONG` from inside the network:
  `docker compose exec redis redis-cli -a $REDIS_PASSWORD ping`.
- [ ] Logs are persisted: `docker compose down` followed by `up -d`
  preserves DB state.

## 10. Known gaps and follow-up

- **No production profile is enabled.** `application-prod.example.yml`
  in `core/backend/deploy/` is the template; rename/fill before
  switching `SPRING_PROFILES_ACTIVE` to `prod`.
- **No Vue admin / Nginx container yet.** If the Vue admin needs to be
  containerized, a separate stage in this Compose file should serve
  `core/nginx/html/sky` over Nginx and proxy `/api/`, `/user/`,
  `/ws/` to the `sky-server` service. Out of scope as of 2026-05-05.
- **Log shipping is manual.** `sky_logs` is mounted but no central log
  driver is configured. Use `docker compose logs -f` for now.
- **No CI build** is wired up to publish images to a registry.
- **Spring Boot Actuator is intentionally absent.** If a richer health
  surface is needed later, add `spring-boot-starter-actuator`, expose
  `management.endpoints.web.exposure.include=health`, and switch the
  Dockerfile / Compose probes to `/actuator/health`.

## 11. Related files

- [`core/backend/deploy/Dockerfile`](../../core/backend/deploy/Dockerfile)
- [`core/backend/deploy/docker-compose.yml`](../../core/backend/deploy/docker-compose.yml)
- [`core/backend/deploy/.dockerignore`](../../core/backend/deploy/.dockerignore)
- [`core/backend/deploy/application-prod.example.yml`](../../core/backend/deploy/application-prod.example.yml)
- [`docs/deployment/database-migrations.md`](./database-migrations.md)
- [`docs/deployment/troubleshooting.md`](./troubleshooting.md)
