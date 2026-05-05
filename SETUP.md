# 苍穹外卖系统 — 开发环境搭建指南 (Windows)

> 目标：从零开始，在 Windows 10/11 上搭建可用于开发与联调的全栈环境。
> 配套文档：[`docs/deployment/index.md`](./docs/deployment/index.md) 部署索引、
> [`docs/deployment/database-migrations.md`](./docs/deployment/database-migrations.md) 数据库迁移顺序、
> [`docs/deployment/troubleshooting.md`](./docs/deployment/troubleshooting.md) 故障排查、
> [`docs/deployment/docker.md`](./docs/deployment/docker.md) Docker 设计（提案）。

## 0. 前置依赖速查

| 软件 | 最低版本 | 安装来源 | 说明 |
|---|---|---|---|
| Java JDK | 17 | https://adoptium.net | 设置 `JAVA_HOME`，加入 `PATH` |
| Maven | 3.9 | https://maven.apache.org | 校验：`mvn -v` 应显示 Java 17 |
| MySQL | 8.0 | https://dev.mysql.com | 默认字符集 utf8mb4，建议 `lower_case_table_names=1` |
| Redis | 6 | https://github.com/tporadowski/redis 或 WSL2 | 校验：`redis-cli ping` |
| Node.js | 18 | https://nodejs.org | 仅 E2E 测试和 uni-app 构建需要 |
| Nginx | 1.24 | 已包含在 `core/nginx/` | 无需单独安装 |
| 微信开发者工具 | 最新 | https://developers.weixin.qq.com/miniprogram/dev/devtools/download.html | 仅小程序联调需要 |

> **C 盘避坑**：Maven 缓存默认在 `~\.m2`。如果你的 C 盘空间紧张，请在 `~/.m2/settings.xml` 中显式设置 `<localRepository>D:\m2-repo</localRepository>`，或为本仓库执行时设置 `-Dmaven.repo.local=D:\m2-repo`。本仓库的脚本不会主动写入 C 盘临时目录。

## 1. 配置环境变量

```batch
copy tooling\.env.example .env
notepad .env
```

最小必填项：

```
DB_HOST=localhost
DB_PORT=3306
DB_DATABASE=sky_take_out
DB_USERNAME=root
DB_PASSWORD=<你的MySQL密码>

REDIS_HOST=localhost
REDIS_PORT=6379
REDIS_PASSWORD=               # 本地无密码留空
REDIS_DATABASE=0

JWT_ADMIN_SECRET_KEY=<至少 64 字节随机字符串>
JWT_USER_SECRET_KEY=<至少 64 字节随机字符串>
```

> **JJWT 0.12.x 的 HS512 强制要求密钥 UTF-8 字节长度 ≥ 64 字节**。短于 64 字节会在启动时抛 `WeakKeyException`。
> 生成示例：
>
> ```batch
> node -e "console.log(require('crypto').randomBytes(48).toString('hex'))"
> ```
>
> 或者在 PowerShell：
>
> ```powershell
> [Convert]::ToBase64String([byte[]]([System.Security.Cryptography.RandomNumberGenerator]::Create().GetBytes(64)))
> ```

完整变量清单见 [`.env.example`](./.env.example)。所有值均为占位符，请勿提交真实凭据。

## 2. 数据库初始化与迁移（顺序敏感）

### 2.1 创建库

```batch
mysql -u root -p -e "CREATE DATABASE IF NOT EXISTS sky_take_out CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"
```

### 2.2 导入基础 schema 与种子数据

```batch
mysql -u root -p sky_take_out < core\database\init.sql
```

`init.sql` 创建基础业务表（employee、category、dish、setmeal、order、address_book 等）并插入演示数据。

### 2.3 多商户迁移（**几乎总是需要**）

当前 Java 代码已经依赖 `merchant_id`、`campus_id` 等列。如果只导入基础 schema，启动时多个 Mapper 会报 “Unknown column”。请执行：

```batch
mysql -u root -p sky_take_out < core\backend\scripts\phase1_multi_merchant_schema.sql
```

### 2.4 历史菜品图片迁移（可选）

```powershell
pwsh core\backend\scripts\migrate-legacy-dish-images.ps1
```

### 2.5 mock 用户默认地址（可选，便于下单链路 E2E）

```sql
INSERT INTO sky_take_out.address_book
(user_id, consignee, sex, phone, province_name, city_name, district_name, detail, label, is_default)
VALUES
(1, '测试用户', '男', '13800138000', '北京市', '北京市', '朝阳区', '酒仙桥北路14号', '公司', 1);
```

## 3. 启动后端服务

### 3.1 直接 Maven 跑

```batch
cd core\backend\sky-server
mvn spring-boot:run -DskipTests
```

等待 `Started SkyApplication`，浏览器打开 http://localhost:8080/doc.html 应能看到 Swagger。
健康检查：http://localhost:8080/internal/health 返回 JSON `{"status":"UP",...}`。

### 3.2 打成 JAR 后跑

```batch
mvn -f core\backend\pom.xml -pl sky-server -am package -DskipTests
java -jar core\backend\sky-server\target\sky-server.jar
```

## 4. 启动前端 (Nginx)

```batch
cd core\nginx
start-nginx.bat
```

访问：http://localhost:8081/#/login

## 5. 启动微信小程序

### 5.1 安装依赖

```batch
cd core\miniapp
npm install
```

### 5.2 编译为微信小程序

使用 HBuilderX 打开 `core\miniapp` 目录 → **运行 → 运行到小程序模拟器 → 微信开发者工具**。
或命令行：

```batch
cd core\miniapp
npx @dcloudio/uni-mp-weixin
```

### 5.3 微信开发者工具导入

导入 `core\miniapp\unpackage\dist\dev\mp-weixin`，AppID 填测试号 `wxf16ad96ba7ac7238`（仅本地预览有效）。

### 5.4 真机预览改 API 地址

编辑 `core\miniapp\utils\env.js`，把 `development` 改为电脑局域网 IP，并保证手机和电脑同 WiFi、本机防火墙放通 8080。

## 6. Windows 一键启动 / 停止

确保 `.env` 已经填好后：

```batch
scripts\start-all.bat
```

脚本动作（顺序）：

1. 加载 `.env`（按行解析，不再用 `for /f /backquote` 执行 `.env`）
2. 校验 `JWT_*_SECRET_KEY` 长度 ≥ 64 字节并给出警告
3. 检查 8080/8081 端口占用
4. 启动后端（`mvn spring-boot:run`），随后用 PowerShell 探测 `/internal/health`
5. 启动 Nginx
6. 在浏览器中打开后台

停止：

```batch
scripts\stop-all.bat
```

> 停止脚本会先 `nginx -s quit`、再按端口 8080 上的 PID 精确终止后端 JVM，避免误杀其他 java 进程。

## 7. 运行 E2E 测试（Agent 6 维护）

```batch
cd verification\e2e-tests
npm install
npx playwright install chromium
npx playwright test
```

## 8. 常见问题

### Q1 `Access denied for user 'root'@'localhost'`
检查 `.env` 中的 `DB_USERNAME` / `DB_PASSWORD`，或在命令行临时覆盖：
```batch
set DB_USERNAME=root
set DB_PASSWORD=<你的密码>
mvn spring-boot:run -DskipTests
```

### Q2 启动报 `NOAUTH Authentication required` 或 Redis 连不上
- 确认本地 Redis 是否启用了 `requirepass`。
- Spring Boot 3.x 用的是 `spring.data.redis.password`（已在 application 中正确配置），但 `.env` 必须填同一个密码。
- 若 Redis 没配密码，`.env` 里 `REDIS_PASSWORD=` 留空。

### Q3 登录返回 `系统繁忙` 或日志出现 `WeakKeyException`
JWT 密钥不到 64 字节。重新生成两个独立的 ≥ 64 字节随机串后重启。

### Q4 启动报 `Unknown column 'merchant_id'` 或 `column campus not found`
没跑多商户迁移。执行：
```batch
mysql -u root -p sky_take_out < core\backend\scripts\phase1_multi_merchant_schema.sql
```

### Q5 端口 8080 / 8081 / 3306 / 6379 被占用
```batch
netstat -ano | findstr ":8080 .*LISTENING"
taskkill /F /PID <pid>
```
若是其他服务（IIS、Skype、本地 MySQL 实例等），考虑在 `application.yml`、`nginx.conf` 改端口后再重试。

### Q6 小程序真机预览访问不到后端
1. 把 `env.js` 的 `127.0.0.1` 改为电脑的局域网 IP（不是 WSL IP）。
2. 关闭 Windows 防火墙或单独放通 8080。
3. 确保手机和电脑在同一 WiFi。

### Q7 Nginx 启动失败
查看 `core\nginx\logs\error.log`，常见原因：8081 占用、`html/sky` 静态资源缺失。

## 9. 服务端口速查

| 服务 | 端口 | 配置位置 |
|---|---|---|
| 后端 API / Swagger | 8080 | `application.yml` |
| 健康检查 `/internal/health` | 8080 | `HealthController.java` |
| 管理后台 (Nginx) | 8081 | `core/nginx/conf/nginx.conf` |
| MySQL | 3306 | `.env` |
| Redis | 6379 | `.env` |

## 10. 进一步阅读

- [`docs/deployment/index.md`](./docs/deployment/index.md) — 部署文档总入口
- [`docs/deployment/database-migrations.md`](./docs/deployment/database-migrations.md) — 数据库迁移顺序与回滚思路
- [`docs/deployment/troubleshooting.md`](./docs/deployment/troubleshooting.md) — 故障排查全集
- [`docs/deployment/docker.md`](./docs/deployment/docker.md) — Docker / Compose 提案
