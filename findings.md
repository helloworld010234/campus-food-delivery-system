# Middleware Deployment Findings

## Project Structure
- Java Spring Boot backend under `core/backend`, with `sky-server` as the runnable service.
- Vue admin static assets are bundled under `core/nginx/html/sky`.
- Nginx configs exist in `core/nginx/conf` and Linux deployment templates exist under `core/backend/deploy`.

## Middleware Dependencies
- Required local middleware for runtime:
  - MySQL 8 compatible database, default database `sky_take_out`.
  - Redis 6+ compatible service on `6379`; project dev config currently hard-codes Redis password `123456`.
  - Nginx reverse proxy/static serving. Linux deployment template proxies public port `80` to backend `127.0.0.1:8081`.
- External services configured but not installed locally:
  - Aliyun OSS for image upload, configured through environment/application properties.
  - WeChat login/payment APIs, configured through environment/application properties.
- Build/runtime tools are not middleware but may be needed for deployment: Java 17+ for the Spring Boot app, Maven if building on server, Node only for frontend builds/tests.

## Remote Server State
- Connected to `8.136.34.168` as root.
- OS: CentOS Linux 7, package manager `yum`.
- MySQL is installed as MySQL 5.7.44, `mysqld` is active/enabled, listening on `3306`.
- Redis is installed under `/www/server/redis`, requires password `123456`, and responds to authenticated `PING`.
- Nginx is installed as `/www/server/nginx` version 1.28.1; config test passes and public HTTP responds on port `80`.
- Production app config at `/etc/sky/application-prod.yml` targets MySQL database `db_new` with user `sky`. The app credentials work.
- `db_new` currently has 13 tables, including migrated multi-merchant tables/columns (`campus`, `merchant`, `employee.merchant_id`).
- Service state repaired: `mysqld`, `redis`, `nginx`, and `sky-port-filter` are all active/enabled.
- Public firewall no longer exposes `3306` or `6379`; external TCP test shows port `80` open and `3306/6379` timeout.
- Note: project docs say MySQL 8+, but the existing server runs MySQL 5.7.44. Current schema and verification queries work on 5.7; upgrading MySQL in-place would be a separate, higher-risk database migration.

## App Startup
- Backend `sky-backend` was switched to `/www/server/java/jdk-17.0.8/bin/java` and starts successfully on `127.0.0.1:8081`.
- Nginx serves the frontend on `http://8.136.34.168/` and proxies API requests to the backend.
- Public API check `http://8.136.34.168/user/shop/status` returns HTTP 200 with `{"code":1,"data":1}`.
- WeChat miniapp source API base URL is now `http://8.136.34.168`.
- HBuilderX imported `D:\sky-delivery\core\miniapp` and built WeChat output under `D:\sky-delivery\core\miniapp\unpackage\dist\dev\mp-weixin`.
- WeChat DevTools must open the compiled `mp-weixin` directory, not the uni-app source directory, because the source directory has no `app.json`.
- `project.config.json` was copied into the compiled `mp-weixin` directory so WeChat DevTools has `appid: wxf16ad96ba7ac7238`.
- WeChat DevTools preview succeeded and generated `D:\sky-delivery\miniapp-preview.png`.
