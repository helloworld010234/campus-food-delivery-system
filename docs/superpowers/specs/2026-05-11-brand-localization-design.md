# 品牌本地化设计 — 苍穹外卖 → 杏林食速

## 目标

将系统中所有面向用户/管理员的"苍穹外卖"品牌痕迹替换为"杏林食速"，使系统符合南通大学杏林学院校园外卖的场景定位。

**核心原则**：仅替换可见文案与配置，不改动任何业务逻辑代码、Java 包名、数据库名、模块名或目录结构，确保零业务风险。

---

## 替换范围

### 明确替换（运行时可见 + 文档）

| 阶段 | 文件路径 | 替换内容 | 说明 |
|------|---------|---------|------|
| 1 | `core/backend/sky-server/src/main/resources/application-dev.yml` | `shop-name: 苍穹外卖` → `杏林食速` | 后端店铺名配置，驱动前端动态展示 |
| 1 | `core/backend/sky-common/src/main/java/com/sky/properties/StorefrontProperties.java` | `private String shopName = "苍穹外卖"` → `"杏林食速"` | 属性类默认值 |
| 1 | `core/backend/sky-server/src/main/java/com/sky/config/WebMvcConfiguration.java` | `"苍穹外卖项目接口文档"` → `"杏林食速项目接口文档"`（4处） | Swagger/Knife4j 文档标题与描述 |
| 2 | `core/miniapp/pages/index/file.vue` | `name: "苍穹"` → `"杏林食速"` | 小程序端品牌展示 |
| 2 | `core/miniapp/README.md` | 标题中的"苍穹外卖" → "杏林食速" | 小程序文档 |
| 2 | `core/miniapp/小程序开发流程.md` | 标题中的"苍穹外卖" → "杏林食速" | 小程序文档 |
| 3 | `core/nginx/html/sky/merchant-admin/联调指南.md` | "苍穹外卖" → "杏林食速" | 前端联调文档 |
| 4 | `README.md` | "苍穹外卖" → "杏林食速" | 项目主文档 |
| 4 | `SETUP.md` | "苍穹外卖" → "杏林食速" | 开发环境文档 |
| 4 | `.env.example` | "苍穹外卖" → "杏林食速" | 环境变量模板 |
| 4 | `startup-report.md` | "苍穹外卖" → "杏林食速" | 启动报告 |
| 4 | `docs/superpowers/plans/2026-05-04-jwt-blacklist.md` | `shop-name: 苍穹外卖` → `杏林食速` | 计划文档配置示例 |
| 4 | `docs/superpowers/agent-briefs/2026-05-05-agent-05-devops-engineering.md` | "苍穹外卖" → "杏林食速" | Agent 简报 |
| 4 | `docs/deployment/database-migrations.md` | "苍穹外卖" → "杏林食速" | 部署文档 |
| 4 | `docs/deployment/troubleshooting.md` | "苍穹外卖" → "杏林食速" | 故障排查文档 |
| 4 | `docs/thesis-upgrade/staged-content.md` | "苍穹外卖" → "杏林食速" | 论文升级文档 |

### 明确不替换（用户已确认）

| 类型 | 示例 | 原因 |
|------|------|------|
| Java 包名 | `com.sky.*` | 涉及数千个 import 语句，重构风险极高 |
| Maven 模块名 | `sky-server`, `sky-common`, `sky-pojo` | 影响构建配置、CI 脚本 |
| 数据库名 | `sky_take_out` | 需重建数据库或迁移数据，风险高 |
| 数据库连接配置 | `application-dev.yml` 中的 `database: ${DB_DATABASE:sky_take_out}` | 保留环境变量兼容 |
| nginx 目录路径 | `core/nginx/html/sky/` | 纯部署路径，对外不可见 |
| 业务逻辑代码 | Controller/Service/Mapper/Entity | 零业务代码改动原则 |

### 无需替换（已符合场景）

- `core/nginx/html/sky/merchant-admin/index.html` 的 `<title>校园外卖管理后台</title>` — 已经是通用场景描述
- 前端管理端动态品牌展示 — 由后端 `shop-name` 配置驱动，改阶段1即可生效

---

## 分阶段执行策略

每阶段只修改明确列出的文件，独立 commit，编译验证后再进入下一阶段。

### 阶段1：后端运行时配置（最高优先级，影响业务）

**涉及文件：**
1. `core/backend/sky-server/src/main/resources/application-dev.yml`
2. `core/backend/sky-common/src/main/java/com/sky/properties/StorefrontProperties.java`
3. `core/backend/sky-server/src/main/java/com/sky/config/WebMvcConfiguration.java`

**验证方式：**
- `mvn clean compile -pl sky-server -am` 编译通过
- 启动后端服务后访问 `http://localhost:8080/doc.html`
- 确认 Swagger 页面标题显示为"杏林食速项目接口文档"
- 确认前端管理端登录后顶部品牌名显示为"杏林食速"

**Commit 消息：** `chore(brand): stage1 - backend shop name and swagger docs`

### 阶段2：小程序端

**涉及文件：**
1. `core/miniapp/pages/index/file.vue`
2. `core/miniapp/README.md`
3. `core/miniapp/小程序开发流程.md`

**验证方式：**
- HBuilderX / uni-app 编译检查无报错
- 预览小程序首页，确认品牌名显示为"杏林食速"

**Commit 消息：** `chore(brand): stage2 - miniapp brand name`

### 阶段3：前端管理端文档

**涉及文件：**
1. `core/nginx/html/sky/merchant-admin/联调指南.md`

**验证方式：**
- 纯 Markdown 文档变更，无需编译

**Commit 消息：** `chore(brand): stage3 - merchant-admin docs`

### 阶段4：项目级文档批量替换

**涉及文件：**
- `README.md`
- `SETUP.md`
- `.env.example`
- `startup-report.md`
- `docs/superpowers/plans/2026-05-04-jwt-blacklist.md`
- `docs/superpowers/agent-briefs/2026-05-05-agent-05-devops-engineering.md`
- `docs/deployment/database-migrations.md`
- `docs/deployment/troubleshooting.md`
- `docs/thesis-upgrade/staged-content.md`

**验证方式：**
- 使用 `grep -r "苍穹外卖" --include="*.md" --include="*.yml" --include="*.txt" D:/sky-delivery` 确认核心源文件中无残留
- 排除 target/、worktrees/、node_modules/ 等目录

**Commit 消息：** `chore(brand): stage4 - project-wide docs`

---

## 特别安全措施

1. **阶段隔离**：每阶段修改前，用 `git diff --stat` 确认改动范围不超过预期文件列表；若发现意外文件被修改，立即中止
2. **编译门禁**：阶段1（后端）修改后必须 `mvn clean compile` 通过才能 commit；编译失败立即 `git checkout --` 还原
3. **独立 Commit**：每阶段一个独立 commit，message 标注阶段号，便于 `git revert HEAD` 精准回滚单阶段
4. **业务代码零触碰**：严格限定在配置、属性、文档层面，不改任何 Controller/Service/Mapper/Entity 中的代码逻辑
5. **数据库安全**：`sky_take_out` 数据库名不动，不涉及任何数据库迁移操作
6. **Swagger 现场验证**：阶段1后必须人工/浏览器确认 Swagger 页面标题已更新，作为阶段1通过的硬标准
7. **残留扫描**：全部阶段完成后，执行全局 grep 扫描，确认核心源文件（排除 build 产物）中无"苍穹外卖"残留

---

## 回滚预案

| 场景 | 操作 | 命令 |
|------|------|------|
| 阶段1编译失败 | 放弃阶段1修改，不进入后续阶段 | `git checkout -- <modified-files>` |
| 阶段1已commit但Swagger异常 | 回滚阶段1 commit | `git revert HEAD --no-edit` |
| 阶段N已commit但发现问题 | 回滚阶段N commit | `git revert HEAD --no-edit` |
| 多阶段需要整体回滚 | 回滚最近N个commit | `git revert HEAD~N..HEAD --no-edit` |
| Swagger缓存导致标题未更新 | 清理target重新编译 | `mvn clean compile` |

所有修改均不涉及数据库 schema 变更或数据迁移，无需数据回滚。

---

## 验证清单（全部阶段完成后）

- [ ] 后端编译通过：`mvn clean compile -pl sky-server -am`
- [ ] Swagger 文档标题显示"杏林食速项目接口文档"
- [ ] 前端管理端登录后顶部品牌名显示"杏林食速"
- [ ] 小程序首页品牌名显示"杏林食速"
- [ ] 全局 grep 扫描核心源文件无"苍穹外卖"残留（排除 target/、worktrees/、node_modules/）
- [ ] 数据库连接正常，业务功能无异常
