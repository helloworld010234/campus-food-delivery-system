# Sky Delivery — Next Iteration Handoff Report

日期：2026-05-05
作用：本文件是 **2026-05-05 综合升级迭代** 的执行结果报告，目的是把当前状态、已验证基线、遗留事项、风险与决策点一次性交接给下一次迭代的 coordinator。

> 本文件是面向"下一次"的；本次迭代的"完结报告"在
> [comprehensive-upgrade-final-report.md](docs/reviews/2026-05-05-comprehensive-upgrade-final-report.md)。
> 两份文件配合使用：本次完结报告关注"做了什么、有什么证据"；本文件关注"下次接到这份代码后，怎么继续"。

---

## 1. Purpose & Audience

读者：下一轮 Ralph loop 的 coordinator（人或 AI）。

目标：让下一轮 coordinator 在阅读本文件 ≤ 10 分钟内做到——

- 知道当前哪些能力是 **已验证**、可以直接构建上层的（不需要重复验证）。
- 知道哪些事项是 **被 deliberately 推迟** 的（不要当成新缺陷重做）。
- 知道哪些是 **需要决策** 的开放问题（不要自己拍板）。
- 知道环境、命令、文件位置的 **一手坐标**，不用从空白起步。
- 拿到一份 **预飞清单**，避免在新迭代第一步就回踩老坑。

---

## 2. What This Iteration Delivered (1-min Recap)

| 维度 | 结果 |
|---|---|
| 迭代分支 | `ralph/sky-delivery-comprehensive-upgrade` |
| 总裁决 | **GREEN** |
| Story 通过率 | US-001..US-008 全部 `passes: true` |
| Gate 通过率 | G-001..G-009 全部 `pass` |
| Coordinator 回归测试 | `mvn test` BUILD SUCCESS, **114 tests, 0 failures** |
| Agent 数 | 1 coordinator + 6 parallel workers |
| 关键交付物 | [final report](docs/reviews/2026-05-05-comprehensive-upgrade-final-report.md), [verification matrix](docs/reviews/2026-05-05-comprehensive-upgrade-verification-matrix.md), 两份 deployment doc, `MerchantScopeGuard` 抽象 + 完整测试套件 |
| Ready to merge | **YES** |
| 阻塞项 | 无 |

---

## 3. Baseline Capabilities (Don't Regress)

下列能力在本轮已经过 **代码审计 + 单元测试** 验证；下一轮不要在不知情的情况下回退它们：

### 3.1 Backend Security

- `core/backend/sky-server/src/main/java/com/sky/security/MerchantScopeGuard.java`
  - 中央权限守卫；5 个公共方法构成 service 层调用 API：
    - `requireExplicitMerchantId(Long)` — 公共浏览/匿名场景下要求显式商户 id。
    - `resolveAdminQueryMerchantId(Long)` — admin 报表/工作台读路径的商户 id 解析。
    - `resolveMerchantWriteId(Long)` — admin 写路径的商户 id 解析。
    - `assertMerchantAccountCanAccess(Long)` — 商户账号访问检查。
    - `assertSameMerchant(Long, Long)` — 跨实体一致性检查。
  - Javadoc 中包含完整的 PLATFORM_ADMIN / MERCHANT_ADMIN / MERCHANT_STAFF / STUDENT_USER 权限矩阵。**新代码请直接调用本守卫，不要重新实现权限判断分支。**
- JWT 拦截器 admin/user 各 8 个测试通过。
- Token Blacklist：SHA-256 hex 哈希、TTL 清理任务、`isBlacklisted` 异常 fail-closed。

### 3.2 Order & Cart Transaction

- `OrderServiceImpl` / `ShoppingCartServiceImpl` 已正确接入 `MerchantScopeGuard`；Mock 微信支付分支保留可用。
- 单测 42 条覆盖：cart add/sub/list/clean、order submit/payment/history/detail/cancel/reorder/reminder、跨商户事务回滚。

### 3.3 Admin Reporting & Workspace

- `ReportServiceImpl` / `WorkspaceServiceImpl` 全部度量都通过 `merchantScopeGuard.resolveAdminQueryMerchantId(null)` 解析当前商户。
- Excel 导出复用同一 scoped `businessData`。
- admin API 范围 contract 文档：[admin-api-scope.md](core/nginx/html/sky/merchant-admin/admin-api-scope.md)。
- 单测 46 条覆盖：所有 turnover/user/order/top10/export 端点 + workspace 4 个端点。

### 3.4 Miniapp Private Flow

- 私域流程（cart / order / reorder / historyOrder）已经通过 `withMerchantScope` 和 `resolveMerchantIdFromOrder` 绑定商户。
- `core/miniapp/pages/index/index.js` 重复 `getData()` 已修复。

### 3.5 Deployment Surface

- README / SETUP / `.env.example` / `tooling/.env.example` / `start-all.bat` / `stop-all.bat` 经过审计判定可用。
- Docker artifacts 在 `core/backend/deploy/`（**Compose up 验证未做**，见 § 4 P1）。
- 部署文档：[troubleshooting.md](docs/deployment/troubleshooting.md)（338 行）、[docker.md](docs/deployment/docker.md)（177 行）。

### 3.6 Verification Evidence

- 单一来源真相：[verification matrix](docs/reviews/2026-05-05-comprehensive-upgrade-verification-matrix.md)。
- Feynman 2026-05-05 已吸收：17 PASS / 2 PARTIAL / 0 FAIL / 4 TIMEOUT / 17 SKIP — **零项目缺陷**（所有 SKIP/TIMEOUT 都是 Feynman 工具/环境限制）。

---

## 4. Deferred Backlog (Pick-up Order)

| 优先 | 项 | Owner Lineage | 估时 | 备注 |
|---|---|---|---|---|
| P1 | `docker compose up` 验证 | Agent 5 | 0.5d | 完成后即可把 Docker 状态从 "proposed" 升级到 "verified"；执行清单见 [docker.md § 9](docs/deployment/docker.md) |
| P1 | 匿名公开浏览策略决策 | Product + Agent 1/4 | 0.5d 决策 + 0.5d 实现 | 当前 `core/miniapp/utils/request.js` 第 94–103 行对所有非 `/user/user/login` 请求强制重定向到登录；产品需决定是否开放匿名浏览 |
| P2 | `MerchantScopeUtils.java` 异常消息 GBK→UTF-8 mojibake 清理 | Agent 1 lineage | 0.25d | 仅影响中文异常文本展示；功能不受影响（测试断言异常类型而非消息字符串） |
| P2 | 删除 `core/miniapp/utils/webscoket.js`（dead code）+ 捆绑的 stomp.js | Agent 4 lineage | 0.25d | URL 仍指向训练源 `wss://socket-canzg.itheima.net/ws`；当前无 import |
| P2 | 401 重定向时持久化 `routeMerchantId` | Agent 4 lineage | 0.5d | 解决深链 `/pages/index/index?merchantId=X` 重新登录后回到默认商户的问题 |
| P2 | `SkyApplicationIT` + Playwright E2E 接入 CI | Agent 6 + DevOps | 1–2d | 需要 Docker daemon + 服务编排；Testcontainers 已在 pom 内 |
| P3 | 确认 `phase1_multi_merchant_schema.sql` 幂等性 | Agent 1/2/3 + Agent 5 | 0.25d | 确认后可放宽 [database-migrations.md](docs/database-migrations.md) 的"重跑须谨慎"提示 |

> 选取下一轮 scope 时，**优先把同一 lineage 的 P 项打包**，避免 6 条线再次发散。
> 例：Agent 4 lineage 的两个 P2（webscoket.js 清理 + 401 routeMerchantId 持久化）可以并到一个 miniapp 清理迭代。

---

## 5. Open Decisions (Need Owner Input Before Coding)

| # | 问题 | 影响面 | 建议提问对象 |
|---|---|---|---|
| D-1 | 是否支持匿名公开浏览？ | 决定要不要在 `request.js` 引入 `allowAnonymous` 标志；决定后端 `/user/shop/*`、`/user/category/list`、`/user/dish/list`、`/user/setmeal/list` 是否需要标记为公开端点 | 产品 + Agent 1（后端策略） |
| D-2 | `core/miniapp/utils/webscoket.js` 是真要做还是删？ | 当前 inert；如果团队考虑做用户侧实时通知（订单状态推送等），保留并修；否则删 | 产品 |
| D-3 | `phase1_multi_merchant_schema.sql` 是否承诺幂等性？ | 决定 ops 文档措辞和 CI 中是否允许重跑 | DBA / Agent 1 |
| D-4 | 是否允许在 admin 之外向 STUDENT_USER 公开商户列表选择 UI？ | 影响匿名浏览实现路径 | 产品 |
| D-5 | Docker compose 部署是否进入官方部署路径？ | 决定 P1 docker 验证的优先级与归属（QA vs DevOps） | DevOps |

> **不要在没有书面决策的情况下，自己替这些问题做选择。** 它们都可能影响下一轮的 file ownership 与 gate 设计。

---

## 6. Known Constraints & Gotchas

下一轮 coordinator 在分发任务、写 prompt、设计 gate 时，请提前知道这些坑：

### 6.1 Tooling / Environment

- **Docker daemon 未在本地始终可用**：`SkyApplicationIT`（Testcontainers）和任何依赖 docker 的验证步骤需要先确认 docker desktop 启动；建议把 docker 状态作为 gate 前置条件。
- **Playwright** 需要服务实际跑在 `localhost:8081`，且 admin SPA 已 build；CI 接入前请把启停脚本封装好。
- **C 盘禁止落盘**：本仓库历来约定所有临时文件、缓存、生成物落到 `D:\sky-delivery\` 下；prompt 中需要再次明确。
- **平台是 Windows 11 + bash**：脚本写 `bash` 语法（`/dev/null` 而不是 `NUL`、正斜杠路径），不要写 `.bat` 才能跑的语法。

### 6.2 Codebase

- `MerchantScopeUtils.java` 的中文异常消息有 mojibake，不要复制其消息文案到新代码做 fail-fast 测试；断言类型不要断言消息字符串。
- `OrderServiceImpl.payment` 真支付分支依赖存储的 `merchantName`；mock 分支必须保留可用，否则本地/CI 测试会断掉。
- `OrderServiceImpl.paySuccess` 故意没做 merchant scope 断言（trade number 系统生成）——不要把它当 bug。
- `core/miniapp/utils/request.js` 第 94–103 行的全局登录拦截在 D-1 决策落地前不要私自改。

### 6.3 Coordinator State

- **6 份 coordinator-only 文件**只能由 coordinator 改：`task_plan.md`、`findings.md`、`progress.md`、`scripts/ralph/prd.json`、`scripts/ralph/progress.txt`、`scripts/ralph/required-gates.md`。worker agents 写入这些文件应当被 prompt 明令禁止。
- **新报告位置**：`docs/reviews/`；**用过的归档**：`scripts/ralph/archive/<日期>-<专项>/`。
- 上一次的 multi-merchant isolation 专项已被吸收为本轮 Agent 1 范围；归档在 `scripts/ralph/archive/2026-05-05-multi-merchant-isolation/`。下一轮如再起新专项，请按相同模式归档当前。

### 6.4 Process

- 所有 6 个 worker agent 的 prompt 要求 **以 Delivery Summary 格式返回**（scope handled / files changed / tests run / result / risks / coordination notes）。复用本格式可省一轮提问。
- 并行派发的关键是 **一个 coordinator 消息内放 6 个 Agent 工具调用**；不要分多轮，否则就退化成串行。

---

## 7. Verified Test / Build Commands

下一轮 coordinator 直接复制可用——这些是本轮 BUILD SUCCESS 的精确命令：

```bash
# 全量 6 agent 涉及的回归（114 tests, 0 failures）
mvn -f core/backend/pom.xml test -pl sky-server \
  "-Dtest=*Scope*Test,*Security*Test,*Jwt*Test,*TokenBlacklist*Test,*Order*Test,*ShoppingCart*Test,*Report*Test,*Workspace*Test"

# Agent 1 focus（57 tests）
mvn -f core/backend/pom.xml test -pl sky-server \
  "-Dtest=*Scope*Test,*Security*Test,*Jwt*Test,*TokenBlacklist*Test"

# Agent 2 focus（42 tests）
mvn -f core/backend/pom.xml test -pl sky-server \
  "-Dtest=*Order*Test,*ShoppingCart*Test"

# Agent 3 focus（46 tests）
mvn -f core/backend/pom.xml test -pl sky-server \
  "-Dtest=*Report*Test,*Workspace*Test,*Scope*Test"

# 全模块（本轮未跑；scope 内通过即可推断模块净）
mvn -f core/backend/pom.xml test
```

阻塞、未跑：

```bash
# 需要 Docker daemon
mvn -pl sky-server -Dtest=SkyApplicationIT verify

# 需要服务起在 localhost:8081 + admin SPA build
npx playwright test
```

---

## 8. Key File & Path Inventory

下一轮快速定位用：

| 类别 | 路径 |
|---|---|
| 综合升级 design | [comprehensive-upgrade-design.md](docs/superpowers/specs/2026-05-05-sky-delivery-comprehensive-upgrade-design.md) |
| 综合升级 PRD | [prd-sky-delivery-comprehensive-upgrade.md](tasks/prd-sky-delivery-comprehensive-upgrade.md) |
| 6 agent 并行计划 | [six-agent-parallel-upgrade-plan.md](docs/superpowers/plans/2026-05-05-six-agent-parallel-upgrade-plan.md) |
| 6 agent prompts | `docs/superpowers/agent-prompts/2026-05-05-*.md`（8 份） |
| 6 agent briefs | `docs/superpowers/agent-briefs/2026-05-05-agent-0{1..6}-*.md` |
| 本轮 final report | [comprehensive-upgrade-final-report.md](docs/reviews/2026-05-05-comprehensive-upgrade-final-report.md) |
| 本轮 verification matrix | [verification-matrix.md](docs/reviews/2026-05-05-comprehensive-upgrade-verification-matrix.md) |
| 中央权限守卫 | [MerchantScopeGuard.java](core/backend/sky-server/src/main/java/com/sky/security/MerchantScopeGuard.java) |
| Admin API 范围 contract | [admin-api-scope.md](core/nginx/html/sky/merchant-admin/admin-api-scope.md) |
| Docker 部署 | [docker.md](docs/deployment/docker.md) + `core/backend/deploy/` |
| 故障排查 | [troubleshooting.md](docs/deployment/troubleshooting.md) |
| Ralph 当前 PRD | [scripts/ralph/prd.json](scripts/ralph/prd.json) |
| Ralph 当前 gates | [scripts/ralph/required-gates.md](scripts/ralph/required-gates.md) |
| 上一轮归档 | `scripts/ralph/archive/2026-05-05-multi-merchant-isolation/` |

---

## 9. Risk Register Carry-Over

下列风险本轮 **未升级为缺陷** 但需要在下一轮持续监控：

| ID | 风险 | 等级 | 触发条件 | 应对 |
|---|---|---|---|---|
| R-1 | `MerchantScopeUtils.java` 异常消息 mojibake | 低 | 用户侧/日志看到乱码影响排查 | P2 清理任务（见 § 4） |
| R-2 | 匿名浏览阻断 | 中 | 产品要求公开浏览且发现被强登录拦截 | D-1 决策 + P1 实现（见 § 4-5） |
| R-3 | 401 重定向丢 `merchantId` | 低 | 用户从分享链接进入并触发会话过期 | P2 持久化（见 § 4） |
| R-4 | Docker 部署未实测 | 中 | 任何宣传/外部使用 Docker 部署的场景 | P1 验证（见 § 4） |
| R-5 | E2E 未在 CI 跑 | 中 | 任何"全栈通过 CI 即可发布"的诉求 | P2 接入 CI（见 § 4） |
| R-6 | `phase1_multi_merchant_schema.sql` 重跑 | 低 | 部署文档承诺重跑安全 | P3 幂等性确认（见 § 4） |
| R-7 | `MerchantScopeGuard` 消费者新增遗漏 | 中 | 后续新加 service 不接守卫 | 在新 PR 模板中强制要求"新增 service 是否经过守卫"勾选项 |
| R-8 | Mock 微信支付被误删 | 中 | 误以为是死代码或测试用代码 | 在 `OrderServiceImpl.payment` 加注释护栏 / 测试断言 |

> R-7 / R-8 是 **本轮新引入的、依赖纪律的风险**：新代码不能绕过守卫；老代码的 mock 分支不能被清理掉。下一轮 prompt 必须显式覆盖。

---

## 10. Recommended Next Iteration Themes

下一轮 coordinator 可以从以下三个候选 scope 选一（按价值/工作量排序）。**不建议** 把三个全做，会重蹈本轮多线发散的覆辙——本轮幸亏 6 条线 file ownership 拆得够干净才没翻车。

### 候选 A：Deployment Hardening（推荐）

- 范围：P1 docker compose 验证 + P3 schema 幂等性确认 + P2 E2E 接入 CI。
- Story 估计：US-001..US-005 量级，3–4 个 worker。
- 价值：把"代码已就绪但未跑通部署"这一最大遗留盲区清掉；为外部部署铺路。
- 依赖决策：D-5（Docker 是否进官方部署路径）。

### 候选 B：Miniapp Cleanup & Anonymous Browse

- 范围：D-1 匿名浏览决策落地 + P2 webscoket.js 删除 + P2 401 routeMerchantId 持久化 + P3 后端公开端点标注。
- Story 估计：US-001..US-004 量级，2–3 个 worker（Agent 1 + Agent 4 lineage 为主）。
- 价值：解决产品已知的浏览体验问题；清理掉训练源遗留代码。
- 依赖决策：D-1 / D-2 / D-4。

### 候选 C：Quality Hardening

- 范围：P2 mojibake 清理 + 全模块 mvn test 跑通基线 + 引入 PR 模板 / merge gate（覆盖 R-7 / R-8）+ 把 verification matrix 接入 CI 自动更新。
- Story 估计：US-001..US-003 量级，2 个 worker（Agent 1 + Agent 6 lineage）。
- 价值：把本轮 R-7 / R-8 的纪律风险用工具固化下来；让回归测试自动化。
- 依赖决策：无。

> 选定 scope 后，仍按"1 coordinator + N parallel workers"模式跑；agent 数取决于 file ownership 切片，不要硬凑到 6。

---

## 11. Pre-Flight Checklist for Next Iteration

下一轮 coordinator 在敲下第一行 prompt 之前，请逐项过：

- [ ] 阅读 [本文件](docs/reviews/2026-05-05-next-iteration-handoff.md) 全部 11 节。
- [ ] 阅读 [上轮 final report](docs/reviews/2026-05-05-comprehensive-upgrade-final-report.md)（特别是 § 7 known limitations 和 § 8 follow-up）。
- [ ] 在 `git status --short` 上确认仍存在的 dirty 状态是 **本轮的** 而不是再上一轮残留——避免误删上轮工作。
- [ ] 把当前 `scripts/ralph/{prd.json,progress.txt,required-gates.md}` 归档到 `scripts/ralph/archive/<新日期>-<新专项>/`。
- [ ] 重置 `task_plan.md` / `findings.md` / `progress.md` 到新 scope；旧版本归档同上。
- [ ] 决策开放问题（§ 5 D-1..D-5），把书面结论贴到新 `findings.md`。
- [ ] 写 6 份（或所需份数的）agent prompt，**每份 prompt 显式列出**：
  - 自己的 file ownership 白名单。
  - 不准触碰的 coordinator-only 文件清单。
  - "禁止回退本轮已经验证的能力（§ 3）"显式提示。
  - 风险 R-7 / R-8 的对应纪律要求（守卫消费者、mock 分支保留）。
  - 返回格式（Delivery Summary）。
- [ ] 在一个 coordinator 消息里 **同时派发** 所有 worker（不要分多轮）。
- [ ] 派发后只做四件事：监控、整合、跑回归、写最终报告。
- [ ] 最终报告基于 [模板](docs/reviews/2026-05-05-comprehensive-upgrade-final-report-template.md) 渲染（如已存在）。
- [ ] 全部 gate 都有 evidence-based pass 或 owner-tagged blocker 后，才敲完成。

---

## 12. Out-of-Scope Reminders (Don't Re-Open)

下列事项 **本轮已经判断、归档过**，下一轮如果再起争论，请先回看本节再决定是否复打开：

- "多商户隔离专项"——已被本轮 Agent 1 完整吸收并归档；不要把它当作下一轮的独立专项重起。
- "OrderServiceImpl.paySuccess 没做 scope 断言"——已确认是 trade number 系统生成场景，**不是缺陷**。
- "MerchantScopeUtils 的中文 mojibake 是 bug"——已分级为 P2 文本清理，不阻塞合并；不要当 P0/P1 处理。
- "Mock 微信支付分支应该删"——**绝不能删**；是本地/CI 测试的关键路径。
- "Coordinator 应该自己 commit"——按 user safety rule，commit/push 必须由用户显式批准；coordinator 准备好 staged 状态，等待批准。
- "Feynman 的 SKIP/TIMEOUT 是项目缺陷"——已经分级为工具/环境限制；除非项目代码本身有变化，否则不要重启 Feynman 测试为质量证据。

---

## 13. Sign-off

- 本轮 coordinator：（本文件作者）
- 本轮闭环时间：2026-05-05
- 下一轮入口：以本文件 § 11 预飞清单为起点。
- 紧急联系（如发现本文件与代码现状冲突）：以代码现状为准；按"读取后再决策"的纪律处理，并把发现写入新一轮 `findings.md`。

