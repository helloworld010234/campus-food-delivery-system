# 品牌本地化实施计划 — 苍穹外卖 → 杏林食速

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 将系统中所有面向用户/管理员的"苍穹外卖"品牌痕迹替换为"杏林食速"，仅替换可见文案与配置，不改动业务逻辑代码、Java 包名、数据库名或模块名。

**Architecture:** 分4个阶段执行，每阶段只修改明确列出的文件，独立 commit，阶段1（后端）必须通过 Maven 编译验证才能进入后续阶段。所有修改均为字符串替换，无逻辑变更。

**Tech Stack:** Spring Boot (Java), Vue.js/Element UI (merchant-admin), uni-app (miniapp), Maven, Git

---

## 文件变更总览

| 阶段 | 文件 | 操作 | 说明 |
|------|------|------|------|
| 1 | `core/backend/sky-server/src/main/resources/application-dev.yml:18` | 编辑 | `shop-name: 苍穹外卖` → `杏林食速` |
| 1 | `core/backend/sky-common/src/main/java/com/sky/properties/StorefrontProperties.java:22` | 编辑 | 默认店铺名 |
| 1 | `core/backend/sky-server/src/main/java/com/sky/config/WebMvcConfiguration.java:95,97,116,118` | 编辑 | Swagger 标题/描述（4处） |
| 2 | `core/miniapp/pages/index/file.vue:74` | 编辑 | `name: "苍穹"` → `"杏林食速"` |
| 2 | `core/miniapp/README.md:1,19,20` | 编辑 | 文档标题 |
| 2 | `core/miniapp/小程序开发流程.md:1` | 编辑 | 文档标题 |
| 3 | `core/nginx/html/sky/merchant-admin/联调指南.md` | 编辑 | 文档中的品牌名 |
| 4 | `README.md`, `SETUP.md`, `.env.example`, `startup-report.md` | 编辑 | 项目级文档 |
| 4 | `docs/superpowers/plans/2026-05-04-jwt-blacklist.md:1246` | 编辑 | 计划文档配置示例 |
| 4 | `docs/superpowers/agent-briefs/2026-05-05-agent-05-devops-engineering.md` | 编辑 | Agent 简报 |
| 4 | `docs/deployment/database-migrations.md` | 编辑 | 部署文档 |
| 4 | `docs/deployment/troubleshooting.md` | 编辑 | 故障排查文档 |
| 4 | `docs/thesis-upgrade/staged-content.md` | 编辑 | 论文升级文档 |

---

### Task 1: 后端配置 — application-dev.yml

**Files:**
- Modify: `core/backend/sky-server/src/main/resources/application-dev.yml:18`

- [ ] **Step 1: 修改 shop-name**

```
将第18行：
    shop-name: 苍穹外卖
替换为：
    shop-name: 杏林食速
```

- [ ] **Step 2: 用 git diff 确认只改了这一行**

Run: `git diff core/backend/sky-server/src/main/resources/application-dev.yml`
Expected: 仅显示 `shop-name` 一行的变更，无其他改动

- [ ] **Step 3: Stage 并 Commit**

```bash
git add core/backend/sky-server/src/main/resources/application-dev.yml
git commit -m "chore(brand): stage1a - update shop-name in application-dev.yml"
```

---

### Task 2: 后端配置 — StorefrontProperties.java

**Files:**
- Modify: `core/backend/sky-common/src/main/java/com/sky/properties/StorefrontProperties.java:22`

- [ ] **Step 1: 修改默认店铺名**

```java
将第22行：
    private String shopName = "苍穹外卖";
替换为：
    private String shopName = "杏林食速";
```

- [ ] **Step 2: 用 git diff 确认只改了这一行**

Run: `git diff core/backend/sky-common/src/main/java/com/sky/properties/StorefrontProperties.java`
Expected: 仅显示 shopName 默认值一行的变更

- [ ] **Step 3: Stage 并 Commit**

```bash
git add core/backend/sky-common/src/main/java/com/sky/properties/StorefrontProperties.java
git commit -m "chore(brand): stage1b - update default shopName in StorefrontProperties"
```

---

### Task 3: 后端配置 — WebMvcConfiguration.java（Swagger 文档）

**Files:**
- Modify: `core/backend/sky-server/src/main/java/com/sky/config/WebMvcConfiguration.java:95,97,116,118`

- [ ] **Step 1: 修改管理端 Swagger 标题和描述**

```java
将第95-97行：
                .title("苍穹外卖项目接口文档")
                .version("2.0")
                .description("苍穹外卖项目接口文档")
替换为：
                .title("杏林食速项目接口文档")
                .version("2.0")
                .description("杏林食速项目接口文档")
```

- [ ] **Step 2: 修改用户端 Swagger 标题和描述**

```java
将第116-118行：
                .title("苍穹外卖项目接口文档")
                .version("2.0")
                .description("苍穹外卖项目接口文档")
替换为：
                .title("杏林食速项目接口文档")
                .version("2.0")
                .description("杏林食速项目接口文档")
```

- [ ] **Step 3: 用 git diff 确认只改了这4处**

Run: `git diff core/backend/sky-server/src/main/java/com/sky/config/WebMvcConfiguration.java`
Expected: 仅显示4处 `"苍穹外卖项目接口文档"` → `"杏林食速项目接口文档"` 的变更

- [ ] **Step 4: Stage 并 Commit**

```bash
git add core/backend/sky-server/src/main/java/com/sky/config/WebMvcConfiguration.java
git commit -m "chore(brand): stage1c - update swagger api docs title and description"
```

---

### Task 4: 阶段1编译验证

**Files:**
- 无新增/修改文件

- [ ] **Step 1: 执行 Maven 编译**

Run: `cd D:/sky-delivery/core/backend && mvn clean compile -pl sky-server -am`
Expected: BUILD SUCCESS，控制台无编译错误

- [ ] **Step 2: 若编译失败，立即回滚阶段1的全部3个commit**

```bash
cd D:/sky-delivery
git revert HEAD~2..HEAD --no-edit
```
然后停止后续所有阶段，报告编译错误。

- [ ] **Step 3: 编译通过后，确认 Swagger 文档标题已更新**

1. 确保后端服务已启动（若未启动，执行 `java -jar core/backend/sky-server/target/sky-server-1.0-SNAPSHOT.jar`）
2. 浏览器访问 `http://localhost:8080/doc.html`
3. 确认页面顶部显示"杏林食速项目接口文档"（而非"苍穹外卖项目接口文档"）

---

### Task 5: 小程序端 — file.vue

**Files:**
- Modify: `core/miniapp/pages/index/file.vue:74`

- [ ] **Step 1: 修改品牌名**

```javascript
将第74行：
						name: "苍穹",
替换为：
						name: "杏林食速",
```

- [ ] **Step 2: 用 git diff 确认只改了这一行**

Run: `git diff core/miniapp/pages/index/file.vue`
Expected: 仅显示 name 值的变更

- [ ] **Step 3: Stage 并 Commit**

```bash
git add core/miniapp/pages/index/file.vue
git commit -m "chore(brand): stage2a - update brand name in miniapp file.vue"
```

---

### Task 6: 小程序端 — README.md 和 小程序开发流程.md

**Files:**
- Modify: `core/miniapp/README.md:1,19,20`
- Modify: `core/miniapp/小程序开发流程.md:1`

- [ ] **Step 1: 修改 README.md**

```markdown
将第1行：
<!-- ## 苍穹外卖 - 小程序  即（苍穹外卖）
替换为：
<!-- ## 杏林食速 - 小程序  即（杏林食速）

将第19行：
#### 苍穹外卖小程序流程说明
替换为：
#### 杏林食速小程序流程说明

将第20行：
#### 2022-8-24  把苍穹外卖改成苍穹外卖、换logo
替换为：
#### 2022-8-24  把苍穹外卖改成杏林食速、换logo
```

- [ ] **Step 2: 修改 小程序开发流程.md**

```markdown
将第1行：
#### 苍穹外卖小程序流程说明
替换为：
#### 杏林食速小程序流程说明
```

- [ ] **Step 3: 用 git diff 确认改动范围**

Run: `git diff --stat core/miniapp/`
Expected: 仅显示 README.md 和 小程序开发流程.md 的变更

- [ ] **Step 4: Stage 并 Commit**

```bash
git add core/miniapp/README.md core/miniapp/小程序开发流程.md
git commit -m "chore(brand): stage2b - update miniapp docs"
```

---

### Task 7: 前端管理端文档 — 联调指南.md

**Files:**
- Modify: `core/nginx/html/sky/merchant-admin/联调指南.md`

- [ ] **Step 1: 全局替换文档中的品牌名**

Run: `sed -i 's/苍穹外卖/杏林食速/g' D:/sky-delivery/core/nginx/html/sky/merchant-admin/联调指南.md`
（Windows 下可用 PowerShell: `(Get-Content D:/sky-delivery/core/nginx/html/sky/merchant-admin/联调指南.md) -replace '苍穹外卖', '杏林食速' | Set-Content D:/sky-delivery/core/nginx/html/sky/merchant-admin/联调指南.md`）

- [ ] **Step 2: 用 git diff 确认改动**

Run: `git diff core/nginx/html/sky/merchant-admin/联调指南.md`
Expected: 所有"苍穹外卖"均替换为"杏林食速"，无其他意外变更

- [ ] **Step 3: Stage 并 Commit**

```bash
git add core/nginx/html/sky/merchant-admin/联调指南.md
git commit -m "chore(brand): stage3 - update merchant-admin integration guide"
```

---

### Task 8: 项目级文档批量替换

**Files:**
- Modify: `README.md`
- Modify: `SETUP.md`
- Modify: `.env.example`
- Modify: `startup-report.md`
- Modify: `docs/superpowers/plans/2026-05-04-jwt-blacklist.md`
- Modify: `docs/superpowers/agent-briefs/2026-05-05-agent-05-devops-engineering.md`
- Modify: `docs/deployment/database-migrations.md`
- Modify: `docs/deployment/troubleshooting.md`
- Modify: `docs/thesis-upgrade/staged-content.md`

- [ ] **Step 1: 批量替换所有项目级文档中的品牌名**

Windows PowerShell 命令：
```powershell
$files = @(
    "D:/sky-delivery/README.md",
    "D:/sky-delivery/SETUP.md",
    "D:/sky-delivery/.env.example",
    "D:/sky-delivery/startup-report.md",
    "D:/sky-delivery/docs/superpowers/plans/2026-05-04-jwt-blacklist.md",
    "D:/sky-delivery/docs/superpowers/agent-briefs/2026-05-05-agent-05-devops-engineering.md",
    "D:/sky-delivery/docs/deployment/database-migrations.md",
    "D:/sky-delivery/docs/deployment/troubleshooting.md",
    "D:/sky-delivery/docs/thesis-upgrade/staged-content.md"
)
foreach ($f in $files) {
    if (Test-Path $f) {
        (Get-Content $f) -replace '苍穹外卖', '杏林食速' | Set-Content $f
    }
}
```

- [ ] **Step 2: 用 git diff --stat 确认改动范围**

Run: `git diff --stat`
Expected: 仅显示上述9个文档文件的变更，无业务代码文件出现在变更列表中

- [ ] **Step 3: 若发现意外文件被修改，立即还原并重新执行**

```bash
git checkout -- <unexpected-file>
```

- [ ] **Step 4: Stage 并 Commit**

```bash
git add README.md SETUP.md .env.example startup-report.md
git add docs/superpowers/plans/2026-05-04-jwt-blacklist.md
git add docs/superpowers/agent-briefs/2026-05-05-agent-05-devops-engineering.md
git add docs/deployment/database-migrations.md
git add docs/deployment/troubleshooting.md
git add docs/thesis-upgrade/staged-content.md
git commit -m "chore(brand): stage4 - update project-wide docs"
```

---

### Task 9: 全局残留扫描与最终验证

**Files:**
- 无新增/修改文件

- [ ] **Step 1: 扫描核心源文件中是否还有"苍穹外卖"残留**

Run:
```bash
cd D:/sky-delivery
grep -r "苍穹外卖" \
    --include="*.java" --include="*.yml" --include="*.yaml" \
    --include="*.js" --include="*.vue" --include="*.html" \
    --include="*.md" --include="*.txt" --include="*.properties" \
    core/ scripts/ docs/ .env.example README.md SETUP.md startup-report.md \
    2>/dev/null | grep -v "target/" | grep -v "worktrees/" | grep -v "node_modules/"
```

Windows PowerShell:
```powershell
$exclude = @('target', 'worktrees', 'node_modules', '.git')
Get-ChildItem -Path D:/sky-delivery/core, D:/sky-delivery/scripts, D:/sky-delivery/docs, D:/sky-delivery/.env.example, D:/sky-delivery/README.md, D:/sky-delivery/SETUP.md, D:/sky-delivery/startup-report.md -Recurse -File -Include *.java, *.yml, *.yaml, *.js, *.vue, *.html, *.md, *.txt, *.properties | Where-Object { $exclude -notcontains $_.Directory.Name -and $_.FullName -notmatch 'target|worktrees|node_modules' } | Select-String -Pattern "苍穹外卖"
```

Expected: 无任何匹配结果（或仅出现在 target/、worktrees/、node_modules/ 等排除目录中）

- [ ] **Step 2: 若发现残留，定位并修复**

Run: `git diff` 查看具体文件，然后手动替换残留处，再执行 `git add` 和 `git commit -m "chore(brand): fix residual brand references"`

- [ ] **Step 3: 验证后端编译仍通过**

Run: `cd D:/sky-delivery/core/backend && mvn clean compile -pl sky-server -am`
Expected: BUILD SUCCESS

- [ ] **Step 4: 最终确认清单**

- [ ] Swagger 页面标题显示"杏林食速项目接口文档"
- [ ] 前端管理端登录后顶部品牌名显示"杏林食速"
- [ ] 小程序首页品牌名显示"杏林食速"
- [ ] 全局残留扫描核心源文件无"苍穹外卖"
- [ ] 数据库连接正常，业务功能无异常
