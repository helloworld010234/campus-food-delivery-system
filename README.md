# 杏林食速系统 (Sky Take Out)

校园外卖系统，由三个子系统组成：Java 后端、Vue 管理后台（通过 Nginx 提供静态资源）、uni-app 微信小程序。

## 项目结构

```
sky-delivery/
├── core/                          # 核心源码
│   ├── backend/                   # Java Spring Boot 后端 (multi-module Maven)
│   │   ├── sky-server/            # 主服务模块（可执行 JAR）
│   │   ├── sky-common/            # 公共模块（编译期依赖）
│   │   ├── sky-pojo/              # 实体/DTO 模块（编译期依赖）
│   │   ├── scripts/               # 数据库迁移脚本（多商户、镜像）
│   │   └── deploy/                # 生产/Docker 部署模板（含 application-prod.example.yml）
│   ├── nginx/                     # Nginx 反向代理 + Vue 后台静态产物
│   ├── miniapp/                   # uni-app 微信小程序源码
│   └── database/                  # init.sql 基础表结构 + 种子数据
├── verification/
│   └── e2e-tests/                 # Playwright E2E 测试 (Agent 6 owned)
├── tooling/
│   ├── .env.example               # 环境变量模板（与根 .env.example 同步）
│   └── open-wechat.js             # 启动微信开发者工具
├── scripts/
│   ├── start-all.bat              # Windows 一键启动
│   └── stop-all.bat               # Windows 一键停止
└── docs/
    ├── deployment/                # 部署、Docker、迁移、故障排查
    ├── reviews/                   # 升级评审记录
    └── superpowers/               # 6-agent 综合升级设计
```

## 技术栈与版本要求

| 层 | 组件 | 版本 | 备注 |
|---|---|---|---|
| 后端 | Java JDK | 17+ | `pom.xml` 锁定为 17（Spring Boot 3.4.4 对应） |
| 后端 | Maven | 3.9+ | 多模块构建（sky-common / sky-pojo / sky-server） |
| 后端 | Spring Boot | 3.4.4 | Redis 配置键已迁移到 `spring.data.redis.*` |
| 数据库 | MySQL | 8.0+ | utf8mb4，时区与服务端保持一致 |
| 缓存 | Redis | 6+ | 推荐 Windows 端使用 [tporadowski/redis](https://github.com/tporadowski/redis) 或 WSL |
| 前端 | Vue 2 + Element UI | 内置静态产物 | 静态资源已构建到 `core/nginx/html/sky` |
| 前端代理 | Nginx | 1.24+ | 已随仓库提供 `core/nginx/nginx.exe` |
| 小程序 | uni-app (Vue 2) | — | 通过 HBuilderX 或微信开发者工具运行 |
| Node | Node.js | 18+ | 仅 E2E 测试和小程序构建需要 |
| 测试 | Playwright + JUnit 5 + Mockito | — | 详见 `verification/e2e-tests/` |

## 快速开始

详见 [SETUP.md](./SETUP.md) 与 [docs/deployment/index.md](./docs/deployment/index.md)。

### Windows 一键启动

```batch
:: 1. 配置环境变量（复制模板）
copy tooling\.env.example .env
notepad .env

:: 2. 创建库 + 导入基础结构
mysql -u root -p -e "CREATE DATABASE IF NOT EXISTS sky_take_out CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"
mysql -u root -p sky_take_out < core\database\init.sql

:: 3. （可选）应用多商户迁移，详见数据库迁移章节
mysql -u root -p sky_take_out < core\backend\scripts\phase1_multi_merchant_schema.sql

:: 4. 一键启动
scripts\start-all.bat
```

> 启动脚本会读取 `.env`、检查端口 8080/8081 占用、启动后端与 Nginx，并尝试探测 `/internal/health`。

## 默认端口

| 服务 | 端口 | URL | 配置位置 |
|---|---|---|---|
| 后端 API | 8080 | http://localhost:8080 | `application.yml` |
| 后端健康检查 | 8080 | http://localhost:8080/internal/health | `HealthController.java` |
| Swagger 文档 | 8080 | http://localhost:8080/doc.html | Knife4j 内置 |
| 管理后台 (Nginx) | 8081 | http://localhost:8081/#/login | `core/nginx/conf/nginx.conf` |
| MySQL | 3306 | — | `.env` |
| Redis | 6379 | — | `.env` |

## 默认账号（仅本地/演示）

| 角色 | 账号 | 密码 |
|---|---|---|
| 平台管理员 | admin | 123456 |
| 小程序模拟登录 | mock_code | 当 `MOCK_USER_LOGIN=true` 时生效 |

> 生产部署必须改密。账号源自 `core/database/init.sql` 中的 employee 种子数据。

## 关键配置文件

| 文件 | 用途 |
|---|---|
| `core/backend/sky-server/src/main/resources/application.yml` | Spring Boot 主配置（占位符引用 `.env`） |
| `core/backend/sky-server/src/main/resources/application-dev.yml` | 开发 profile |
| `core/backend/deploy/application-prod.example.yml` | 生产 profile 模板（占位符占位，需自行填充） |
| `core/nginx/conf/nginx.conf` | Nginx 反代 `/api/` 与 `/user/` 到后端 8080 |
| `core/miniapp/utils/env.js` | 小程序运行时 API base URL |
| `.env` (本地，未入库) | 由 `tooling/.env.example` 派生 |

## 数据库初始化与迁移顺序

按以下顺序执行（每一步均幂等，但若先后顺序错误会出现列缺失或索引冲突）：

1. **基础库**：`mysql ... -e "CREATE DATABASE IF NOT EXISTS sky_take_out ..."`
2. **基础表 + 种子数据**：`core/database/init.sql`
3. **多商户迁移（必要时）**：`core/backend/scripts/phase1_multi_merchant_schema.sql`
   会新增 `campus`、`merchant` 表，并为现有业务表添加 `merchant_id`、`campus_id` 列。
4. **历史菜品图片迁移（可选）**：`core/backend/scripts/migrate-legacy-dish-images.ps1`
5. **示例替换脚本（可选）**：`core/backend/scripts/replace_dishes_delete.sql` / `campus_replace_delete.sql`

> 仅在后端启动日志出现 “column ... not found”、“unknown column 'merchant_id'” 等提示时执行第 3 步。
> 详见 [docs/deployment/database-migrations.md](./docs/deployment/database-migrations.md)。

## 部署与 Docker（提案）

仓库提供 Linux/systemd 模板：

- `core/backend/deploy/application-prod.example.yml`
- `core/backend/deploy/sky-backend.service.example`
- `core/backend/deploy/sky-port-filter.service.example`
- `core/backend/deploy/nginx-ip-http.conf`

容器化方案（**proposed，未在本仓库执行验证**）：

- `core/backend/deploy/Dockerfile`（多阶段构建，sky-server）
- `core/backend/deploy/docker-compose.yml`（mysql + redis + sky-server）
- `docs/deployment/docker.md`

未启用 Spring Boot Actuator，容器健康检查使用项目自带 `/internal/health`。

## 许可证

仅供学习交流使用。
