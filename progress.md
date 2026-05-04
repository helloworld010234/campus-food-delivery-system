# Middleware Deployment Progress

## 2026-04-30
- Started continuation of middleware deployment task.
- Checked session catchup output; no previous planning files were present.
- Listed project root contents.
- Confirmed `D:\sky-delivery` is not a Git repository.
- Created persistent planning files in the project root.
- Analyzed README, SETUP, Spring configs, Maven dependencies, and Nginx configs.
- Identified runtime middleware as MySQL, Redis, and Nginx; Aliyun OSS/WeChat are external services.
- Connected to the remote CentOS 7 server and inspected current middleware/service state.
- Diagnosed Redis/Nginx service status mismatch: Redis process is healthy but systemd inactive; Nginx init script returns failure when nginx is already running.
- Patched `/etc/init.d/nginx` on the server so "already running" returns success for systemd.
- Started/enabled Redis and Nginx through systemd; MySQL/Redis/Nginx now report active/enabled.
- Added Spring Boot 3-compatible `spring.data.redis` keys to `/etc/sky/application-prod.yml` while keeping the existing `spring.redis` keys.
- Removed public firewalld exposure for MySQL `3306/tcp` and Redis `6379/tcp`.
- Rewrote `sky-port-filter.service` to enforce closed public data ports on boot.
- Verified Nginx config, Redis auth ping, MySQL app database access, local HTTP response, and external port reachability.
- User requested starting frontend, backend, and WeChat side.
- Changed remote `sky-backend.service` to run with JDK 17 and restarted it successfully.
- Updated miniapp `utils/env.js` to point to `http://8.136.34.168`.
- Imported miniapp into HBuilderX and installed local npm dependency `@dcloudio/uni-ui` with project-local npm cache.
- Fixed miniapp build by changing `pages/address/address.vue` to import `uni-nav-bar` from `@dcloudio/uni-ui` directly.
- Opened WeChat DevTools against compiled `unpackage/dist/dev/mp-weixin` directory.
- Copied `project.config.json` into the compiled miniapp directory to satisfy WeChat DevTools appid lookup.
- Generated a successful WeChat DevTools preview QR image.
- Verified public frontend and backend endpoints return HTTP 200.
