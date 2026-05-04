# 苍穹外卖系统 (Sky Take Out)

校园外卖系统，由三个子系统组成：Java 后端、Vue 管理后台、uni-app 微信小程序。

## 项目结构

```
sky-delivery/
├── core/                          # 核心源码
│   ├── backend/                   # Java Spring Boot 后端
│   │   ├── sky-server/            # 主服务模块
│   │   ├── sky-common/            # 公共模块
│   │   ├── sky-pojo/              # 实体/DTO 模块
│   │   ├── scripts/               # 数据库迁移脚本
│   │   └── deploy/                # 部署配置模板
│   ├── nginx/                     # Nginx 反向代理 + Vue 前端构建产物
│   ├── miniapp/                   # uni-app 微信小程序源码
│   └── database/                  # 数据库初始化脚本
├── verification/                  # 测试验证
│   └── e2e-tests/                 # Playwright E2E 测试
├── tooling/                       # 工具脚本
│   ├── .env.example               # 环境变量模板
│   └── open-wechat.js             # 启动微信开发者工具
└── docs/                          # 项目文档
```

## 技术栈

| 层 | 技术 |
|---|---|
| 后端 | Java 17 + Spring Boot 3.4.4 + MyBatis + MySQL 8 + Redis |
| 前端 | Vue 2 + Element UI |
| 小程序 | uni-app (Vue 2) |
| 测试 | Playwright + JUnit 5 + Mockito |

## 快速开始

详见 [SETUP.md](./SETUP.md)。

### 一句话启动（Windows）

```batch
# 1. 配置环境变量（复制 tooling\.env.example → .env，填写实际值）

# 2. 初始化数据库
mysql -u root -p < core\database\init.sql

# 3. 启动所有服务
scripts\start-all.bat
```

## 默认端口

| 服务 | 端口 | URL |
|---|---|---|
| 后端 API | 8080 | http://localhost:8080 |
| 管理后台 | 8081 | http://localhost:8081/#/login |
| Swagger 文档 | 8080 | http://localhost:8080/doc.html |

## 默认账号

| 角色 | 账号 | 密码 |
|---|---|---|
| 管理员 | admin | 123456 |
| 小程序用户 | mock_code（微信 code） | — |

## 关键配置文件

| 文件 | 用途 |
|---|---|
| `core/backend/sky-server/src/main/resources/application-dev.yml` | 开发环境配置 |
| `core/backend/sky-server/src/main/resources/application.yml` | 主配置 |
| `core/nginx/conf/nginx.conf` | Nginx 代理配置 |
| `core/miniapp/utils/env.js` | 小程序 API 地址 |

## 数据库迁移

多商户 schema 迁移脚本位于 `core/backend/scripts/phase1_multi_merchant_schema.sql`。
如果首次运行提示"数据库结构不匹配"，请执行：

```bash
mysql -u root -p sky_take_out < core\backend\scripts\phase1_multi_merchant_schema.sql
```

## 许可证

仅供学习交流使用。
