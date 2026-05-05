# Troubleshooting

Authoritative collection of runtime and startup failures for the Sky Take
Out backend, Vue admin (via Nginx), and uni-app miniapp on Windows. Each
entry lists symptom, root cause, fix, and how to verify.

## Quick lookup

| Symptom | Section |
|---|---|
| `WeakKeyException` on startup | [JWT key too short](#jwt-key-too-short) |
| `NOAUTH Authentication required` | [Redis password mismatch](#redis-password-mismatch) |
| `Access denied for user 'root'@'localhost'` | [MySQL credentials](#mysql-credentials) |
| `Unknown column 'merchant_id'` / `Table 'sky_take_out.campus' doesn't exist` | [Schema mismatch](#schema-mismatch-multi-merchant-not-applied) |
| Backend dies immediately, port already in use | [Port conflicts](#port-conflicts) |
| `start-all.bat` exits with `JAVA_HOME` errors | [Java / Maven path](#java--maven-path) |
| Nginx fails on port 8081 | [Nginx startup failure](#nginx-startup-failure) |
| Miniapp cannot reach API on phone | [Miniapp real-device connectivity](#miniapp-real-device-connectivity) |
| Health probe in `start-all.bat` never reports UP | [Health probe stuck](#health-probe-stuck) |
| Backend log: `Public Key must not be null` | [WeChat Pay placeholders](#wechat-pay-placeholders) |

---

## JWT key too short

**Symptom**

```
io.jsonwebtoken.security.WeakKeyException:
  The signing key's size is N bits which is not secure enough for the HS512 algorithm.
```

**Cause**

`JJWT 0.12.x` enforces `>= 64` bytes (UTF-8) for HS512. The defaults
inside `scripts/start-all.bat` are dev placeholders that satisfy the
length requirement, but a `.env` overriding them with anything shorter
will fail.

**Fix**

Regenerate two distinct random secrets and place them in `.env`:

```batch
node -e "console.log(require('crypto').randomBytes(48).toString('hex'))"
```

```powershell
[Convert]::ToBase64String([System.Security.Cryptography.RandomNumberGenerator]::GetBytes(64))
```

Both `JWT_ADMIN_SECRET_KEY` and `JWT_USER_SECRET_KEY` must be `>= 64`
bytes. They must be different from each other.

**Verify**

`scripts\start-all.bat` prints `[WARN] JWT_..._SECRET_KEY is only N
chars` when the key is too short. The backend will refuse to start
otherwise.

---

## Redis password mismatch

**Symptom**

```
NOAUTH Authentication required
io.lettuce.core.RedisCommandExecutionException: WRONGPASS invalid username-password pair or user is disabled.
```

**Cause**

Local Redis was started with `requirepass` but `.env` either omits
`REDIS_PASSWORD` or sets it to the wrong value. Spring Boot 3.x reads
`spring.data.redis.password`; the `.env` value is bound to that key via
`application.yml`.

**Fix**

1. Confirm whether your Redis has a password:

   ```batch
   redis-cli -p 6379 INFO server
   redis-cli -p 6379 -a <password> PING
   ```

2. Edit `.env`:
   - **No password set in Redis** → `REDIS_PASSWORD=`
   - **Password set in Redis** → `REDIS_PASSWORD=<exact match>`

3. Restart the backend (`scripts\stop-all.bat` then `start-all.bat`).

---

## MySQL credentials

**Symptom**

```
java.sql.SQLException: Access denied for user 'root'@'localhost' (using password: YES)
HikariPool-1 - Failed to validate connection
```

**Cause**

`DB_USERNAME` / `DB_PASSWORD` in `.env` does not match the local MySQL
account. On Windows-installed MySQL 8 the default root password is set
during installation and is **not** `root`.

**Fix**

```batch
mysql -u root -p
```

Confirm the password works at the CLI before placing it in `.env`. If
the database itself does not exist yet, see
[`database-migrations.md`](./database-migrations.md).

If you intentionally use a non-root account, grant minimum privileges:

```sql
CREATE USER 'sky'@'localhost' IDENTIFIED BY '<password>';
GRANT ALL PRIVILEGES ON sky_take_out.* TO 'sky'@'localhost';
FLUSH PRIVILEGES;
```

Then set `DB_USERNAME=sky` in `.env`.

---

## Schema mismatch (multi-merchant not applied)

**Symptom**

Any of:

- `java.sql.SQLSyntaxErrorException: Unknown column 'merchant_id' in 'field list'`
- `Unknown column 'campus_id' in 'where clause'`
- `Table 'sky_take_out.campus' doesn't exist`
- `Table 'sky_take_out.merchant' doesn't exist`
- HTTP 500 on `/admin/category/list` or `/user/shop/status` after a
  successful login.

**Cause**

`core/database/init.sql` only ships the base schema. Current Java code
relies on the multi-merchant migration which lives in a separate file.

**Fix**

```batch
mysql -u root -p sky_take_out < core\backend\scripts\phase1_multi_merchant_schema.sql
```

If the migration was partially applied (e.g. interrupted), the safe
recovery is:

```sql
DROP DATABASE sky_take_out;
CREATE DATABASE sky_take_out CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

then re-run `init.sql` followed by the multi-merchant SQL from a clean
state. See [`database-migrations.md`](./database-migrations.md).

---

## Port conflicts

**Symptom**

- `start-all.bat` prints `[WARN] Port 8080 is already in use`.
- Backend logs `Web server failed to start. Port 8080 was already in use.`
- Nginx exits silently (check `core\nginx\logs\error.log`).

**Cause**

Another process holds 8080 (backend), 8081 (Nginx admin), 3306 (MySQL),
or 6379 (Redis). Common offenders on Windows: a previous run that did
not stop cleanly, IIS, Skype, another local MySQL/Redis instance, WSL
forwarding.

**Fix**

```batch
:: identify
netstat -ano | findstr /R /C:":8080 .*LISTENING"
netstat -ano | findstr /R /C:":8081 .*LISTENING"

:: kill (replace <pid>)
taskkill /F /PID <pid>

:: or use the project's stop script
scripts\stop-all.bat
```

If 8080 is owned by an unrelated service you cannot stop:

1. Edit `core/backend/sky-server/src/main/resources/application.yml`
   `server.port:` to a free port (e.g. 8090).
2. Update `core/nginx/conf/nginx.conf` upstream block to point at the
   new port.
3. Update `verification/e2e-tests/` env if you use it.

---

## Java / Maven path

**Symptom**

- `'mvn' is not recognized as an internal or external command`
- `JAVA_HOME is not set`
- `mvn -v` reports a different Java version than 17.

**Fix**

1. Install Temurin 17 from <https://adoptium.net>.
2. Set environment variables (System → Advanced → Environment
   Variables):
   - `JAVA_HOME = C:\Program Files\Eclipse Adoptium\jdk-17.x.x` (or D
     drive if you installed there)
   - Add `%JAVA_HOME%\bin` to `Path`.
   - Add Maven `bin` to `Path`.
3. Restart the terminal so the new env is picked up.
4. Verify:
   ```batch
   java -version
   mvn -v
   ```

---

## Nginx startup failure

**Symptom**

- `core\nginx\start-nginx.bat` shows nothing happened.
- `core\nginx\logs\error.log` contains
  `bind() to 0.0.0.0:8081 failed (10048: ...)`.

**Cause**

Port 8081 is already bound, or static assets under
`core\nginx\html\sky` are missing.

**Fix**

```batch
netstat -ano | findstr /R /C:":8081 .*LISTENING"
taskkill /F /PID <pid>
```

If `html\sky` is empty, restore from the repository (it is checked in).
Do **not** rebuild Vue admin yourself — the bundled artifacts are the
source of truth for this repo.

---

## Miniapp real-device connectivity

**Symptom**

Miniapp on a real phone cannot reach `http://localhost:8080`.

**Fix**

1. Edit `core/miniapp/utils/env.js` and replace `localhost` with the
   workstation's LAN IPv4 address (not the WSL adapter).
2. Open Windows Defender Firewall → allow inbound TCP 8080 for
   `Private` networks.
3. Confirm phone and workstation share the same Wi-Fi.
4. Verify from the phone browser: `http://<lan-ip>:8080/internal/health`
   should return JSON.

---

## Health probe stuck

**Symptom**

`scripts\start-all.bat` prints
`Backend did not answer /internal/health yet; continuing anyway.`

**Cause**

First-time Maven downloads (or a slow disk) push backend startup past
the 30-second probe budget. The script intentionally continues so the
admin window opens, but the backend may still be compiling.

**Fix**

- Watch the spawned `Sky Backend` cmd window for `Started SkyApplication`.
- Manually probe:

  ```powershell
  Invoke-WebRequest http://localhost:8080/internal/health -UseBasicParsing
  ```

- If startup repeatedly takes too long, prime the Maven cache once:

  ```batch
  mvn -f core\backend\pom.xml -pl sky-server -am dependency:go-offline -DskipTests
  ```

---

## WeChat Pay placeholders

**Symptom**

Backend logs `Public Key must not be null` or
`java.io.FileNotFoundException` for `apiclient_key.pem` on startup.

**Cause**

`MOCK_PAYMENT=false` in `.env` forces the WeChat Pay client to load
real merchant credentials, but the placeholder paths in `.env.example`
were never replaced.

**Fix**

Set `MOCK_PAYMENT=true` (default in `.env.example`) for local
development. Only fill the `WECHAT_*` block on a machine that actually
has the merchant credentials, and store PEM files outside the repo
(e.g. `D:\sky-delivery\secrets\` — already excluded from git).

---

## When to escalate

- Schema-related errors that survive a clean re-import → ask Agents 1/2/3
  to check the migration script.
- Nginx config drift breaks E2E → coordinate with Agent 6 before
  changing `core/nginx/conf/nginx.conf`.
- Real production credentials were committed by mistake → rotate
  immediately, then notify the coordinator.
