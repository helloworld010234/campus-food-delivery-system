# Agent 05 Brief: 部署、环境与工程化

日期：2026-05-05

## 1. Mission

Agent 5 负责部署、环境和工程化。目标是让项目可以被新开发者或部署者按文档复现启动，并为后续 Docker/Compose、健康检查、迁移顺序和环境变量治理打基础。

## 2. Background

项目包含 Spring Boot 后端、MySQL、Redis、Nginx 静态后台和 uni-app 小程序。Feynman 输出中已有 replication guide 和 Docker analysis，可作为部署文档升级输入。全局 AGENTS 指令要求避免在 C 盘生成临时文件、产物、缓存和中间数据。

## 3. Input Materials

- `README.md`
- `SETUP.md`
- `.env.example`
- `tooling/.env.example`
- `scripts/start-all.bat`
- `scripts/stop-all.bat`
- `D:\feynman-test-2026-05-05\outputs\replication\guide.md`
- `D:\feynman-test-2026-05-05\outputs\docker\analysis.md`
- `docs/reviews/2026-05-05-feynman-report-upgrade-inputs.md`

## 4. Primary File Ownership

Primary write scope:

- `README.md`
- `SETUP.md`
- `.env.example`
- `tooling/.env.example`
- `scripts/start-all.bat`
- `scripts/stop-all.bat`
- `core/backend/deploy/`
- deployment docs created under `docs/`

Coordinate before editing:

- real `.env`
- database migration scripts
- application YAML files
- Nginx proxy config that affects admin or miniapp tests
- E2E config owned by Agent 6

## 5. Forbidden Scope

Agent 5 must not:

- commit real credentials or user secrets.
- overwrite `.env` with example values.
- change production database schema without a migration plan.
- introduce Docker files that require C drive build cache or C drive temp output.
- break current Windows one-click scripts without replacement.
- edit business logic to satisfy deployment docs.

## 6. Execution Tasks

1. Audit setup docs for:
   - Java version.
   - Maven version.
   - MySQL version.
   - Redis version.
   - Node version.
   - Nginx ports.
   - default accounts.
2. Audit environment templates:
   - DB variables.
   - Redis variables.
   - JWT secrets.
   - OSS variables.
   - WeChat variables.
   - API base URL.
3. Audit database initialization:
   - base schema.
   - multi-merchant migration.
   - seed data.
   - rerun safety.
4. Audit startup scripts:
   - root `.env` loading.
   - backend startup.
   - Nginx startup.
   - stop behavior.
   - logs and PIDs.
5. Design or implement Docker/Compose materials:
   - MySQL service.
   - Redis service.
   - sky-server service.
   - health checks.
   - volumes.
   - secret handling.
6. Update common troubleshooting:
   - JWT key too short.
   - Redis password mismatch.
   - MySQL access denied.
   - port conflicts.
   - schema mismatch.

## 7. Execution-Stage Notes

- Start from existing docs. Do not replace them with a generic deployment guide.
- Keep Windows workflows first-class; this repository is being used on Windows PowerShell.
- Do not use C drive for helper scripts, caches, generated Docker context, screenshots, or conversion outputs.
- If package installs are needed during future execution, configure cache under `D:\sky-delivery` or another non-C path.
- Avoid editing the real `.env`; use `.env.example` and `tooling/.env.example`.
- Secrets must be placeholders only. Do not echo or record real local secret values in docs.
- If adding Docker design, make clear whether it is implemented and verified or only proposed.
- If health check references `/health`, verify whether that endpoint exists. If not, propose adding one or use an existing endpoint in design.
- Be careful with database names. Existing docs and examples may refer to `sky_take_out` or other names; normalize or explain differences.
- Multi-merchant migration should be documented as a second step after base schema, not silently assumed.
- Do not edit Java application config in ways that break local non-Docker startup unless coordinated.
- Coordinate with Agent 6 before changing Playwright base URLs or startup assumptions.

## 8. Suggested Verification

Manual checks:

- Fresh setup dry run from README/SETUP steps.
- Confirm required env vars are documented.
- Confirm startup script assumptions match current repo.
- Confirm Docker design references existing paths.

Potential commands:

```powershell
mvn -f core/backend/pom.xml -pl sky-server -am package -DskipTests
```

```powershell
scripts\start-all.bat
```

Only run startup commands during execution phase, not during design-only work.

## 9. Acceptance Criteria

- Setup docs list dependencies, versions, ports, and accounts.
- Environment templates are clear and secret-free.
- Database init and migration order are documented.
- Docker/Compose plan or implementation is explicit.
- C-drive avoidance is documented.
- Troubleshooting section reflects current project issues.

## 10. Delivery Summary Format

```markdown
## Delivery Summary

- Scope handled:
- Files changed:
- Verification performed:
- Result:
- Environment assumptions:
- Acceptance criteria satisfied:
- Remaining risks:
- Coordination requests:
```

