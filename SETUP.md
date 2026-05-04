# 苍穹外卖系统 — 开发环境搭建指南

> 目标：从零开始，在 Windows 上搭建完整的开发/联调环境。

## 前置依赖

| 软件 | 版本 | 下载/说明 |
|---|---|---|
| Java JDK | 17+ | https://adoptium.net |
| Maven | 3.9+ | https://maven.apache.org |
| MySQL | 8.0+ | https://dev.mysql.com |
| Redis | 6+ | https://github.com/microsoftarchive/redis (Windows) 或 WSL |
| Node.js | 18+ | https://nodejs.org |
| Nginx | 1.24+ | 已包含在 `core/nginx/` |
| 微信开发者工具 | 最新版 | https://developers.weixin.qq.com/miniprogram/dev/devtools/download.html |

## 1. 配置环境变量

复制模板文件，填入实际值：

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
DB_PASSWORD=你的MySQL密码

REDIS_HOST=localhost
REDIS_PORT=6379
REDIS_PASSWORD=你的Redis密码（如无密码留空）
REDIS_DATABASE=0

JWT_ADMIN_SECRET_KEY=至少32字符的随机字符串
JWT_USER_SECRET_KEY=至少32字符的随机字符串
```

**重要**：JJWT 0.12.x 要求密钥长度 >= 256 位（32 字节），请使用强随机字符串。

## 2. 初始化数据库

### 2.1 创建数据库

```bash
mysql -u root -p -e "CREATE DATABASE IF NOT EXISTS sky_take_out CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"
```

### 2.2 导入基础数据

```bash
mysql -u root -p sky_take_out < core\database\init.sql
```

### 2.3 多商户 Schema 迁移（如需要）

如果后续启动后端时提示"数据库结构与代码版本不匹配"，执行：

```bash
mysql -u root -p sky_take_out < core\backend\scripts\phase1_multi_merchant_schema.sql
```

### 2.4 插入 mock 用户默认地址（可选，用于测试下单）

```sql
INSERT INTO sky_take_out.address_book
(user_id, consignee, sex, phone, province_name, city_name, district_name, detail, label, is_default)
VALUES
(1, '测试用户', '男', '13800138000', '北京市', '北京市', '朝阳区', '酒仙桥北路14号', '公司', 1);
```

## 3. 启动后端服务

```batch
cd core\backend\sky-server
mvn spring-boot:run -DskipTests
```

等待日志出现 `Started SkyApplication`，即表示启动成功。

验证：浏览器访问 http://localhost:8080/doc.html 应能看到 Swagger 接口文档。

## 4. 启动前端（Nginx）

Nginx 已包含在项目中，无需额外安装。

```batch
cd core\nginx
start-nginx.bat
```

验证：浏览器访问 http://localhost:8081/#/login 应能看到管理后台登录页。

## 5. 启动微信小程序

### 5.1 安装依赖

```batch
cd core\miniapp
npm install
```

### 5.2 编译为微信小程序

使用 HBuilderX 打开 `core\miniapp` 目录，点击菜单 **运行 → 运行到小程序模拟器 → 微信开发者工具**。

或命令行：

```batch
cd core\miniapp
npx @dcloudio/uni-mp-weixin
```

### 5.3 使用微信开发者工具打开

微信开发者工具 → 导入项目 → 选择：

```
core\miniapp\unpackage\dist\dev\mp-weixin
```

AppID 填写：`wxf16ad96ba7ac7238`（测试号即可）

### 5.4 修改小程序 API 地址（真机预览时）

编辑 `core\miniapp\utils\env.js`，将 `development` 改为你电脑的局域网 IP：

```javascript
const ENV_CONFIG = {
  development: "http://192.168.x.x:8080",  // 替换为你的局域网 IP
  production: "http://127.0.0.1:8080",
};
```

确保手机和电脑在同一 WiFi 下。

## 6. 一键启动脚本（Windows）

如果已正确配置 `.env` 文件，可直接运行：

```batch
scripts\start-all.bat
```

该脚本会依次启动：
1. Java 后端（端口 8080）
2. Nginx 前端（端口 8081）
3. 打开 Swagger 文档页面

## 7. 运行 E2E 测试

```batch
cd verification\e2e-tests
npm install
npx playwright install chromium
npx playwright test
```

## 常见问题

### Q1: 后端启动报 "Access denied for user"？

检查 `.env` 文件中的 `DB_USERNAME` 和 `DB_PASSWORD` 是否正确，或直接在命令行设置：

```batch
set DB_USERNAME=root
set DB_PASSWORD=你的密码
mvn spring-boot:run -DskipTests
```

### Q2: 后端启动报 "Unable to connect to Redis / NOAUTH"？

检查 Redis 密码配置。Spring Boot 3.x 使用 `spring.data.redis.password` 而非 `spring.redis.password`。

### Q3: 登录接口返回 "系统繁忙"？

检查后端日志，通常是 JWT 密钥长度不足。确保 `JWT_ADMIN_SECRET_KEY` 和 `JWT_USER_SECRET_KEY` 均 >= 32 字符。

### Q4: 小程序真机预览无法连接后端？

1. 将 `env.js` 中的 `127.0.0.1` 改为电脑局域网 IP
2. 关闭 Windows 防火墙或开放 8080 端口
3. 确保手机和电脑在同一 WiFi

### Q5: Nginx 启动失败？

检查端口 8081 是否被占用：

```batch
netstat -ano | findstr 8081
```

如被占用，修改 `core\nginx\conf\nginx.conf` 中的 `listen` 端口。

## 服务端口速查

| 服务 | 端口 | 配置位置 |
|---|---|---|
| 后端 API | 8080 | `application.yml` |
| 管理后台 | 8081 | `nginx/conf/nginx.conf` |
| MySQL | 3306 | `.env` |
| Redis | 6379 | `.env` |
| Swagger | 8080/doc.html | 内置 |
