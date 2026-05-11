# 杏林食速系统启动报告

生成时间: 2026-04-30 18:55

---

## 一、本机资源检查

| 资源 | 要求版本 | 实际状态 | 结果 |
|------|----------|----------|------|
| JDK | 17+ | Java 17.0.15 (Microsoft OpenJDK) | 通过 |
| Maven | 3.9+ | Apache Maven 3.9.10 | 通过 |
| MySQL | 8.0+ | MySQL 8.0.41 (服务运行中) | 通过 |
| Node.js | - | v20.17.0 | 通过 |
| Redis | 6+ | 未安装 | **缺失** |
| Nginx | 1.24+ | 未安装 | **缺失** |

### 资源详细说明

**已通过的资源：**
- Java: Microsoft OpenJDK 17.0.15，Maven 使用此版本
- Maven: 3.9.10，构建正常
- MySQL: 8.0.41 社区版，服务状态 RUNNING
  - root 密码: `root`
  - 数据库 `sky_take_out` 已存在且包含数据
  - employee 表: 14 条记录
  - dish 表: 23 条记录
- Node.js: v20.17.0，用于运行前端静态服务器

**缺失的资源（未下载）：**
- Redis: 未在本机找到 redis-cli 或 redis-server
- Nginx: 未在本机找到 nginx.exe

---

## 二、项目编译状态

### Maven 构建结果: 全部成功

```
sky-take-out ....................................... SUCCESS [  0.133 s]
sky-common ......................................... SUCCESS [  4.100 s]
sky-pojo ........................................... SUCCESS [  3.440 s]
sky-server ......................................... SUCCESS [  4.136 s]
```

**编译警告:**
1. `SetmealVO.java` 和 `DishVO.java` 中 `@Builder` 注解忽略了初始化表达式，建议添加 `@Builder.Default`
2. `OrderVO.java` 缺少 `@EqualsAndHashCode(callSuper=false)` 注解
3. `WeChatPayUtil.java` 使用了已弃用的 API
4. 测试代码中使用了已弃用的 `@MockBean`，建议改用 `@MockitoBean`

---

## 三、服务启动状态

### 1. 后端服务 (Spring Boot)

| 项目 | 状态 |
|------|------|
| 启动状态 | 运行中 |
| 进程 PID | 20092 |
| 监听端口 | 8080 |
| 激活配置 | dev |
| 数据库连接 | 正常 (HikariPool 已启动) |
| 多商户检测 | 通过 (campus, merchant, employeeScope 等全部就绪) |
| Redis 连接 | 未配置/未连接 |

**后端 API 测试结果:**

| API 端点 | 方法 | 状态 | 说明 |
|----------|------|------|------|
| `/admin/employee/login` | POST | 通过 | 返回 JWT token，认证正常 |
| `/user/shop/status` | GET | 通过 | 返回店铺状态 code=1 |
| `/admin/dish/page` | GET | 通过 | 返回菜品分页数据 (23条) |

### 2. 前端管理后台

| 项目 | 状态 |
|------|------|
| 启动状态 | 运行中 |
| 服务方式 | Node.js http-server (替代 Nginx) |
| 监听端口 | 8081 |
| 根目录 | `core/nginx/html/sky` |
| HTTP 状态 | 200 OK |

### 3. 微信小程序端

| 项目 | 状态 |
|------|------|
| 启动状态 | **未启动** (需手动启动) |
| 原因 | 需要微信开发者工具或 HBuilderX (GUI 应用) |
| 配置状态 | 正常，API 基础地址指向 `http://127.0.0.1:8080` |

---

## 四、访问地址汇总

| 服务 | URL | 说明 |
|------|-----|------|
| 后端 API | http://localhost:8080 | RESTful API 服务 |
| 管理后台 | http://localhost:8081 | 前端管理界面 |
| API 文档 | http://localhost:8080/doc.html | Knife4j Swagger 文档 |
| 管理后台登录 | http://localhost:8081/#/login | 默认账号: admin/123456 |

---

## 五、存在的问题与风险

### 1. Redis 缺失 (中等影响)

**问题描述:** 本机未安装 Redis，但项目配置了 Redis 连接 (`application-dev.yml`)。

**影响范围:**
- 菜品缓存功能无法使用（可能频繁查询数据库）
- 购物车缓存功能可能异常
- WebSocket 相关功能可能受影响
- 分布式锁功能不可用

**建议:** 安装 Redis 并启动服务，或在开发环境中配置内存缓存替代方案。

### 2. Nginx 缺失 (低影响)

**问题描述:** 本机未安装 Nginx，已使用 Node.js http-server 替代。

**影响范围:**
- 无反向代理功能（管理后台直接访问后端 API）
- 无静态资源缓存优化
- 无 Gzip 压缩
- 生产环境必须使用 Nginx

**建议:** 当前开发环境使用 http-server 已足够，生产环境需安装 Nginx。

### 3. 测试代码弃用警告 (低影响)

**问题描述:** 测试代码使用了 Spring Boot 2.x 的 `@MockBean`，在 Spring Boot 3.x 中已弃用。

**建议:** 将 `@MockBean` 替换为 `@MockitoBean`。

### 4. 数据库名不一致 (低影响)

**问题描述:** 初始化脚本 `init.sql` 使用数据库名 `db_new`，而项目配置使用 `sky_take_out`。

**说明:** 实际数据库 `sky_take_out` 已存在且数据完整，不影响运行。

---

## 六、启动命令记录

### 后端启动
```bash
cd D:/sky-delivery/core/backend/sky-server
set DB_HOST=localhost
set DB_PORT=3306
set DB_DATABASE=sky_take_out
set DB_USERNAME=root
set DB_PASSWORD=root
set JWT_ADMIN_SECRET_KEY=change_me_to_a_random_string_at_least_32_chars_long
set JWT_USER_SECRET_KEY=change_me_to_a_random_string_at_least_32_chars_long
set SPRING_PROFILES_ACTIVE=dev
mvn spring-boot:run -DskipTests
```

### 前端启动 (替代 Nginx)
```bash
npx http-server "D:/sky-delivery/core/nginx/html/sky" -p 8081 --cors
```

### 微信小程序启动
需要手动使用微信开发者工具打开 `D:/sky-delivery/core/miniapp` 目录。

---

## 七、微信小程序端状态补充

### 启动尝试结果: 无法自动启动

**原因:** 本机未安装微信开发者工具或 HBuilderX，这两个是 uni-app 小程序项目必需的 GUI 开发工具，无法通过命令行自动启动。

**小程序配置检查:**
- AppID: `wxf16ad96ba7ac7238` (微信小程序测试号)
- API 基础地址: `http://127.0.0.1:8080` (指向本机后端)
- 开发环境: `development`
- Mock 登录: 已启用 (`MOCK_USER_LOGIN=true`)
- Mock 支付: 已启用 (`MOCK_PAYMENT=true`)

**手动启动步骤:**

方式一 (推荐): 使用微信开发者工具
1. 安装微信开发者工具 (https://developers.weixin.qq.com/miniprogram/dev/devtools/download.html)
2. 打开微信开发者工具
3. 选择"导入项目"
4. 项目目录选择: `D:\sky-delivery\core\miniapp`
5. AppID 填写: `wxf16ad96ba7ac7238`
6. 点击"确定"导入

方式二: 使用 HBuilderX
1. 安装 HBuilderX (https://www.dcloud.io/hbuilderx.html)
2. 打开 HBuilderX
3. 文件 -> 打开目录 -> 选择 `D:\sky-delivery\core\miniapp`
4. 点击工具栏"运行" -> "运行到小程序模拟器" -> "微信开发者工具"

---

## 八、总结

| 子系统 | 状态 | 说明 |
|--------|------|------|
| 后端 API | 正常运行 | 端口 8080，数据库连接正常 |
| 管理后台 | 正常运行 | 端口 8081，使用 http-server |
| API 文档 | 正常运行 | Knife4j Swagger 文档可访问 |
| 微信小程序 | 需手动启动 | 缺少微信开发者工具/HBuilderX |
| Redis | 缺失 | 建议安装以启用缓存 |
| Nginx | 缺失 | 开发环境已用 http-server 替代 |

**整体评估:** 项目核心功能（后端 API + 管理后台 + API 文档）已成功启动并运行，API 测试全部通过。主要缺失 Redis 缓存服务和微信开发工具，不影响基本功能但 Redis 会影响性能和缓存相关功能。
