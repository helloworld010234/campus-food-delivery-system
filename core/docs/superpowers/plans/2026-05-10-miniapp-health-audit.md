# miniapp 综合健康度体检报告生成 - 实施计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 按 spec `core/docs/superpowers/specs/2026-05-10-miniapp-health-audit-design.md` 产出单文档 yaml-dense 健康度报告至 `core/docs/superpowers/audits/2026-05-10-miniapp-health.md`，过程为静态分析 + 跨端对照，**不实跑 miniapp**，**不改代码**。

**Architecture:** 6 章按依赖顺序填充：§0 元信息 → §2 API 契约 → §3 功能页面 → §4 代码质量 → §5 横向对照 → 回填 §1 速览。每章用 ripgrep 静态采集 + gstack 能力（review/investigate）做证据校验。最终自检文档大小、ID 唯一性、孤儿发现。

**Tech Stack:** ripgrep, wc, find, gstack/review, gstack/investigate, manual code reading

---

## 文件结构

| 文件 | 操作 | 说明 |
|---|---|---|
| `core/docs/superpowers/audits/2026-05-10-miniapp-health.md` | 创建 | 最终交付物，单文档 yaml-dense |
| `core/docs/superpowers/audits/_workspace/api-list.yaml` | 临时 | miniapp api.js 全部 exports + path/method |
| `core/docs/superpowers/audits/_workspace/api-imports.yaml` | 临时 | 各页面 import 关系反查表 |
| `core/docs/superpowers/audits/_workspace/be-endpoints.yaml` | 临时 | 后端 user Controller 端点清单 |
| `core/docs/superpowers/audits/_workspace/ma-api.yaml` | 临时 | merchant-admin API 调用清单 |
| `core/docs/superpowers/audits/_workspace/findings.yaml` | 临时 | 全部 finding 累积器 |

工作区 `_workspace/` 用于积累中间数据，最终内容合并入 audit md。任务完成后保留以备复查（不入 git，已通过 .gitignore 全局忽略 `_workspace/`）。

---

## Task 1: 工作目录与元数据采集

**Files:**
- Create: `core/docs/superpowers/audits/2026-05-10-miniapp-health.md`
- Create: `core/docs/superpowers/audits/_workspace/`

- [ ] **Step 1.1: 创建目录结构**

Run:
```
mkdir -p core/docs/superpowers/audits/_workspace
ls core/docs/superpowers/audits/
```

Expected: 输出含 `_workspace/`。

- [ ] **Step 1.2: 采集 git 元信息**

Run:
```
git -C D:/sky-delivery rev-parse --short HEAD
git -C D:/sky-delivery rev-parse --abbrev-ref HEAD
date -u +"%Y-%m-%dT%H:%M:%SZ"
```

记录三项数值用于 Step 1.3 的 frontmatter。

- [ ] **Step 1.3: 创建 audit md 骨架**

Write to `core/docs/superpowers/audits/2026-05-10-miniapp-health.md` (起始内容):

文件内容（具体写法参考 spec §4，章节占位符等待后续 Task 填充）：

    # miniapp 综合健康度体检报告

    （在此放置 yaml 代码块，包含 generated_at、miniapp_commit、miniapp_branch、spec 路径）

    ## 1. 健康度速览
    （Task 6 回填）

    ## 2. API 契约层
    （Task 2 填充）

    ## 3. 功能完整性
    （Task 3 填充）

    ## 4. 代码质量
    （Task 4 填充）

    ## 5. 横向对照 (miniapp ↔ merchant-admin)
    （Task 5 填充）

- [ ] **Step 1.4: 大小检查点**

Run:
```
wc -l core/docs/superpowers/audits/2026-05-10-miniapp-health.md
```

Expected: ≤ 30 行（仅骨架）。

---

## Task 2: §2 API 契约层数据采集

**Files:**
- Modify: `core/docs/superpowers/audits/2026-05-10-miniapp-health.md` (§2 节)
- Workspace: `_workspace/api-list.yaml`, `_workspace/api-imports.yaml`, `_workspace/be-endpoints.yaml`, `_workspace/findings.yaml`

- [ ] **Step 2.1: 提取 miniapp api.js 全部 exports**

Run:
```
grep -nE "^(export |module\\.exports|const ) " D:/sky-delivery/core/miniapp/pages/api/api.js | head -100
```

Expected: ~30-50 个函数定义。手动解析每个 export 的：
- 函数名
- 行号
- HTTP method（看函数体的 request method 字段）
- path 字符串

写入 `_workspace/api-list.yaml`，格式：

    api_list:
      - fn: getList
        line: 80
        path: /dish/list
        method: GET
      - fn: submitOrderSubmit
        line: 145
        path: /user/order/submit
        method: POST
      ...

- [ ] **Step 2.2: 反查 exports 在哪些页面被 import**

Run:
```
grep -rEn "from ['\"][\\.\\@/]+/?pages/api/api['\"]" D:/sky-delivery/core/miniapp/pages/ --include="*.vue" --include="*.js"
```

对每个匹配行解析 `{fn1, fn2, fn3}` 列表，写入 `_workspace/api-imports.yaml`：

    imports:
      pages/index/index.js:
        - getMerchantInfo
        - getCategoryList
        - getDishList
      pages/order/index.js:
        - submitOrderSubmit
        - getAddressBookDefault
      ...
    dead_exports:
      - openTable
      - getTableState
      - ...

`dead_exports` = `api_list` 全集减去所有 imports 出现过的函数名集合。

- [ ] **Step 2.3: 列出后端 user Controller 端点**

Run:
```
grep -rEn "@(GetMapping|PostMapping|PutMapping|DeleteMapping|RequestMapping)" D:/sky-delivery/core/backend/sky-server/src/main/java/com/sky/controller/user/
```

并配合每个 Controller 类的 `@RequestMapping` 前缀：

```
grep -rEn "@RequestMapping" D:/sky-delivery/core/backend/sky-server/src/main/java/com/sky/controller/user/ -B 2
```

整合得到全路径，写入 `_workspace/be-endpoints.yaml`：

    backend_user_endpoints:
      - controller: DishController
        method: list
        path: /user/dish/list
        http_method: GET
        auth: anonymous
      - controller: OrderController
        method: submit
        path: /user/order/submit
        http_method: POST
        auth: user_jwt
      ...

- [ ] **Step 2.4: 用 gstack/investigate 深入对比**

Run:
```
Skill(skill="gstack/investigate")
```

Prompt to gstack:
> 输入：core/docs/superpowers/audits/_workspace/api-list.yaml 和 _workspace/be-endpoints.yaml。
> 输出：列出 miniapp 调用 path 与 backend 实际暴露 path 不匹配的所有项，每项给出 mp_path、be_path、method 差异、必填字段差异。
> 同时列出 miniapp 调用了但 backend 已删除的端点（dead_endpoint）。

将结果合并到 `_workspace/findings.yaml`（API 部分）。

- [ ] **Step 2.5: 分配 P0/P1/P2 ID**

按 spec §3 严重度规则归类：
- P0: api_path_mismatch / api_method_mismatch / missing_required / dead_endpoint
- P1: dead_code (api.js 中定义但无 import) / field_mismatch / extra_param
- P2: response_field_diff (仅命名差异)

ID 编号顺序：P0 从 P0-001 起，P1 从 P1-010 起（预留 P1-001~009 给代码质量），P2 从 P2-020 起。

每条 finding 完整字段（按 spec §5 schema）：

    api_findings:
      block_A_shared_with_admin: []
      block_B_miniapp_only:
        - id: P0-001
          t: api_path_mismatch
          sev: P0
          be:
            controller: DishController#list
            path: /user/dish/list
            method: GET
            req_fields: [pageNum, pageSize, categoryId]
            resp_fields: [...]
            auth: anonymous
          mp:
            fn: getList
            file: pages/api/api.js:80
            path: /dish/list
            method: GET
            params: [pageNum, pageSize, categoryId]
            called_by: [pages/index/index.js:42]
          diff: mp_path_wrong
          evi: pages/api/api.js:80
          imp: dish_browse_break
          fix: pages/api/api.js:80:change_path_to_/user/dish/list
      block_C_admin_only_listed: []
      block_D_dead_in_miniapp:
        - id: P1-010
          t: dead_code
          sev: P1
          evi: pages/api/api.js:45-58
          imp: 13_unused_legacy_apis
          fix: pages/api/api.js:remove_table_funcs

- [ ] **Step 2.6: 写入 audit md §2**

Edit `core/docs/superpowers/audits/2026-05-10-miniapp-health.md`，将 `## 2. API 契约层` 节占位符替换为 Step 2.5 收集的 4 个 block 的 yaml。

- [ ] **Step 2.7: §2 大小自检**

Run:
```
wc -l core/docs/superpowers/audits/2026-05-10-miniapp-health.md
```

Expected: ≤ 350 行（§2 单节预算 ~300 行 + 骨架 30 行）。如超出，归并 dead_code 为名称列表（不展开每条 evi/imp/fix）。

- [ ] **Step 2.8: Commit §2 数据**

```
git add core/docs/superpowers/audits/2026-05-10-miniapp-health.md
git commit -m "audit: populate miniapp section 2 api contract findings"
```

---

## Task 3: §3 功能完整性采集

**Files:**
- Modify: `core/docs/superpowers/audits/2026-05-10-miniapp-health.md` (§3 节)

- [ ] **Step 3.1: 对每个核心页面静态检查 5 项**

按 spec §6 page_enum 列出 8 个页面：

```
P-index    pages/index/index.vue
P-details  pages/details/index.vue
P-order    pages/order/index.vue
P-pay      pages/pay/index.vue
P-success  pages/success/index.vue
P-history  pages/historyOrder/historyOrder.vue
P-user     pages/userCenter/index.vue
P-address  pages/addOrEditAddress/addOrEditAddress.vue
```

对每个页面运行：
```
FILE=D:/sky-delivery/core/miniapp/pages/<path>
echo "=== $FILE ==="
test -f "$FILE" && grep -cE "uni\\.showToast|catch\\(|\\.fail" "$FILE"
test -f "$FILE" && grep -cE "loading|isLoading" "$FILE"
test -f "$FILE" && grep -cE "v-if=.*length\\s*===\\s*0|empty-state" "$FILE"
test -f "$FILE" && grep -cE "onLoad|onShow" "$FILE"
```

按以下规则填 5 项 check：

```
api: ok | fail | partial
  - ok: 该页面用到的 API 在 §2 中均无 P0
  - fail: 至少 1 个 API 是 P0
  - partial: 1+ API 是 P1 但无 P0
err: has | miss
  - has: showToast/catch/.fail 计数 > 0
  - miss: 计数 = 0
empty: ok | miss | n_a
  - ok: 列表型页面有 v-if length===0 占位
  - miss: 列表型页面无占位
  - n_a: 表单/详情类页面不需要
load: has | miss | n_a
  - has: 有 loading/isLoading 状态
  - miss: 异步操作但无 loading
  - n_a: 静态页面
route: ok | fail
  - ok: onLoad 参数与跳转处一致
  - fail: 期望参数缺失或拼写差异
```

- [ ] **Step 3.2: 计算每页 verdict**

按 spec §6 verdict_rule：
- ok: 所有 5 项 (api/err/empty/load/route) 都是 ok/has 状态
- partial: 至少 1 项 fail/miss 但 api 不是 fail
- broken: api=fail (P0 阻塞)，或者 5 项中 ≥2 项 fail

- [ ] **Step 3.3: 写入 audit md §3**

替换 `## 3. 功能完整性` 占位符为 yaml 块：

    pages:
      - id: P-index
        file: pages/index/index.vue
        verdict: partial
        apis_used: [getMerchantInfo, getCategoryList, getDishList, userLogin]
        checks:
          api: partial
          err: has
          empty: ok
          load: miss
          route: ok
        issues: [P1-005, P1-013]
      - id: P-order
        ...

- [ ] **Step 3.4: §3 大小自检**

Expected: 累计 ≤ 500 行。

- [ ] **Step 3.5: Commit §3**

```
git add core/docs/superpowers/audits/2026-05-10-miniapp-health.md
git commit -m "audit: populate miniapp section 3 functional completeness"
```

---

## Task 4: §4 代码质量采集

**Files:**
- Modify: `core/docs/superpowers/audits/2026-05-10-miniapp-health.md` (§4 节)

- [ ] **Step 4.1: 用 gstack/review 做代码质量扫描**

Run:
```
Skill(skill="gstack/review")
```

Prompt to gstack:
> 范围：core/miniapp/（排除 components/uni-icons/、common/、static/、node_modules/）
> 关注：dead_code, oversize_file (>800 lines), hardcode (id/url/coord), css_typo, confused_naming, console_log_left, magic_number, missing_error_handling, mutation_pattern
> 输出格式：每条 finding 含 t/evi/imp/fix（按 spec §7 type 枚举归类）

将 gstack 输出写入 `_workspace/findings.yaml` (quality 部分)。

- [ ] **Step 4.2: 手动 grep 补充扫描**

补充 gstack 可能遗漏的项（dead_code 复用 Task 2.2 结果）：

oversize_file:
```
find D:/sky-delivery/core/miniapp -type f \\( -name "*.vue" -o -name "*.js" \\) ! -path "*/node_modules/*" ! -path "*/uni-icons/*" -exec wc -l {} \\; | sort -rn | head -10
```
Expected: 列出最大 10 个文件，标记 lines > 800 的为 finding。

hardcode 模式：
```
grep -rEn "tableId\\s*[:=]\\s*[\"'][0-9]+[\"']" D:/sky-delivery/core/miniapp/pages/
grep -rEn "\\b1[12][0-9]\\.[0-9]+\\s*,\\s*[34][0-9]\\.[0-9]+\\b" D:/sky-delivery/core/miniapp/pages/
grep -rEn "https?://" D:/sky-delivery/core/miniapp/pages/ | grep -vE "node_modules|sourceMappingURL"
```

css_typo:
```
grep -rEn ":\\s*[0-9]+rp[^x]" D:/sky-delivery/core/miniapp/pages/ --include="*.vue"
grep -rEn ":\\s*[0-9]+p[ \\t]" D:/sky-delivery/core/miniapp/pages/ --include="*.vue"
```

console_log_left:
```
grep -rcE "console\\.log" D:/sky-delivery/core/miniapp/pages/ --include="*.vue" --include="*.js"
```
合并所有计数，作为 1 条 P2 finding。

- [ ] **Step 4.3: 合并去重并分配 ID**

合并 gstack/review 输出与手动 grep 结果，按文件位置去重。
ID 编号：P1-001~009 给代码质量（与 Task 2.5 的 P1-010+ 区分），P2-001~019 给代码质量。

每条 finding 字段（按 spec §7 schema）：

    code_quality_findings:
      - id: P1-001
        t: oversize_file
        sev: P1
        evi: pages/index/index.js:1151
        imp: hard_to_maintain
        fix: pages/index/index.js:split_to_components
      - id: P1-002
        t: hardcode
        sev: P1
        evi: pages/index/index.js:tableId="1282346960773238786"
        imp: cross_table_data_leak
        fix: pages/index/index.js:read_from_scene_or_query
      - id: P2-001
        t: css_typo
        sev: P2
        evi: pages/success/index.vue:92
        imp: line_height_invalid
        fix: pages/success/index.vue:92:44rp_to_44rpx
      ...

- [ ] **Step 4.4: 写入 audit md §4**

替换 `## 4. 代码质量` 占位符。

- [ ] **Step 4.5: §4 大小自检**

Expected: 累计 ≤ 650 行。

- [ ] **Step 4.6: Commit §4**

```
git add core/docs/superpowers/audits/2026-05-10-miniapp-health.md
git commit -m "audit: populate miniapp section 4 code quality"
```

---

## Task 5: §5 横向对照（miniapp ↔ merchant-admin）

**Files:**
- Modify: `core/docs/superpowers/audits/2026-05-10-miniapp-health.md` (§5 节)
- Workspace: `_workspace/ma-api.yaml`

- [ ] **Step 5.1: 列出 merchant-admin API 调用**

Run:
```
find D:/sky-delivery/core/web/merchant-admin/src/api -type f \\( -name "*.js" -o -name "*.ts" \\)
grep -rEn "request\\.(get|post|put|delete)|axios\\.(get|post|put|delete)" D:/sky-delivery/core/web/merchant-admin/src/api/
```

Expected: ~25-40 个 API 函数。每个解析：
- 函数名 / 文件:行
- HTTP method
- path
- 必填参数

写入 `_workspace/ma-api.yaml`：

    ma_apis:
      - fn: getDishPage
        file: src/api/dish.js:42
        path: /admin/dish/page
        method: GET
        auth: admin_jwt
        params: [page, pageSize, categoryId, name, status]
      - fn: addDish
        file: src/api/dish.js:67
        path: /admin/dish
        method: POST
        ...

- [ ] **Step 5.2: 列出后端 admin Controller 端点**

Run:
```
grep -rEn "@(GetMapping|PostMapping|PutMapping|DeleteMapping)" D:/sky-delivery/core/backend/sky-server/src/main/java/com/sky/controller/admin/
grep -rEn "@RequestMapping" D:/sky-delivery/core/backend/sky-server/src/main/java/com/sky/controller/admin/ -B 2
```

写入 `_workspace/be-endpoints.yaml` 的 `backend_admin_endpoints` 节。

- [ ] **Step 5.3: 用 gstack/investigate 配对**

Run:
```
Skill(skill="gstack/investigate")
```

Prompt to gstack:
> 输入：
>   _workspace/api-list.yaml (mp 调用)
>   _workspace/ma-api.yaml (ma 调用)
>   _workspace/be-endpoints.yaml (be user + admin 端点)
> 任务：按业务操作 (op) 配对，输出 12-18 对：
>   - parallel: 同 op 不同 path（如 user dish list vs admin dish list）
>   - shared: 同一 path 双方都调
>   - only_mp: 仅 miniapp 调用
>   - only_ma: 仅 admin 调用 + miniapp 应该有等价物但缺失
> 对每对输出：op / pair_type / mp 字段集 / ma 字段集 / be 字段集 / diff 列表
> diff 关注：path / method / 必填字段 / auth / 默认值 / 错误处理

- [ ] **Step 5.4: 标记 X-NNN ID 并补 NEW-NNN 缺口**

X-NNN 从 X-001 起，按 op 字母顺序排序。

如某 only_ma 对应的 op 表明 miniapp 应该有对应能力（如 OSS 上传）但缺失，分配 NEW-001、NEW-002 标记为 capability_gap：

    xref_pairs:
      - id: X-001
        op: dish_list_browse
        pair_type: parallel
        mp:
          fn: getList
          path: /dish/list
          method: GET
          file: pages/api/api.js:80
          params: [pageNum, pageSize, categoryId]
          auth: anonymous_allowed
        ma:
          fn: getDishPage
          path: /admin/dish/page
          method: GET
          file: src/api/dish.js:42
          params: [page, pageSize, categoryId, name, status]
          auth: admin_jwt
        be:
          mp_target: DishController#list@/user/dish/list
          ma_target: DishController#page@/admin/dish/page
          same_handler: false
        diff:
          - mp_path_wrong: P0-001
          - mp_no_status_filter: P1-014
          - mp_no_name_search: P1-015
        refs: [P0-001, P1-014, P1-015]

      - id: X-007
        op: oss_upload
        pair_type: only_ma
        mp:
          fn: -
          path: -
          note: not_used
        ma:
          fn: uploadFile
          path: /admin/common/upload
          method: POST
          file: src/api/common.js:8
          auth: admin_jwt
        be:
          mp_target: -
          ma_target: CommonController#upload@/admin/common/upload
          same_handler: false
        diff:
          - no_user_endpoint: NEW-001
        refs: [NEW-001]

- [ ] **Step 5.5: 写入 audit md §5**

替换 `## 5. 横向对照` 占位符。

- [ ] **Step 5.6: §5 大小自检**

Expected: 累计 ≤ 750 行。如超出，将 only_mp 类型的 pair 简化为只记 op + ref（不展开 mp/ma 字段）。

- [ ] **Step 5.7: Commit §5**

```
git add core/docs/superpowers/audits/2026-05-10-miniapp-health.md
git commit -m "audit: populate miniapp section 5 cross-ref with merchant-admin"
```

---

## Task 6: §1 健康度速览汇总

**Files:**
- Modify: `core/docs/superpowers/audits/2026-05-10-miniapp-health.md` (§1 节)

- [ ] **Step 6.1: 统计 totals**

Run:
```
DOC=core/docs/superpowers/audits/2026-05-10-miniapp-health.md
grep -c "sev: P0" "$DOC"
grep -c "sev: P1" "$DOC"
grep -c "sev: P2" "$DOC"
grep -cE "verdict: ok" "$DOC"
grep -cE "verdict: partial" "$DOC"
grep -cE "verdict: broken" "$DOC"
grep -cE "^  - id: X-" "$DOC"
grep -cE "^  - id: NEW-" "$DOC"
```

记录数值。

- [ ] **Step 6.2: 计算 dim_scores**

按 spec §4 dim_score_rule：

```
api_compliance:
  - 检查 §2 中 api_findings 数：
    - 任意 P0 → fail
    - 0 P0 但有 P1 → warn
    - 0 P0 0 P1 → pass

functional_completeness:
  - 检查 §3 中 verdict 分布：
    - 任意 broken → fail
    - 任意 partial → warn
    - 全部 ok → pass

code_quality:
  - 检查 §4 中 code_quality_findings 数：
    - 任意 P0（理论上不应出现，因为代码质量类不阻塞）→ fail
    - 任意 P1 → warn
    - 仅 P2 → pass
```

- [ ] **Step 6.3: 抽取 top3_risks**

排序规则：
1. sev DESC (P0 > P1 > P2)
2. ID 数字 ASC

取前 3，每条生成 ≤80 字符的 one_liner。

- [ ] **Step 6.4: 写入 audit md §1**

替换 `## 1. 健康度速览` 占位符为 yaml 块（按 spec §4 schema）：

    overall_verdict: <一句话整体结论>
    dim_scores:
      api_compliance: warn | fail | pass
      functional_completeness: warn | fail | pass
      code_quality: warn | fail | pass
    totals:
      api_total: <Step 6.1 数值>
      api_ok: ...
      api_warn: ...
      api_fail: ...
      api_dead: ...
      findings_p0: ...
      findings_p1: ...
      findings_p2: ...
      pages_checked: 8
      pages_ok: ...
      pages_partial: ...
      pages_broken: ...
      xref_pairs: ...
    top3_risks:
      - id: P0-001
        one_liner: <≤80 字符>
      - id: P0-002
        one_liner: ...
      - id: P0-003
        one_liner: ...
    scan_scope:
      files_total: <find 全量>
      files_scanned: <实际扫描>
      file_globs_included: [...]
      file_globs_excluded: [...]

- [ ] **Step 6.5: §1 大小自检**

Expected: 累计 ≤ 800 行。

- [ ] **Step 6.6: Commit §1**

```
git add core/docs/superpowers/audits/2026-05-10-miniapp-health.md
git commit -m "audit: populate miniapp section 1 summary dashboard"
```

---

## Task 7: 自检与最终归档

**Files:**
- Modify: `core/docs/superpowers/audits/2026-05-10-miniapp-health.md`

- [ ] **Step 7.1: 大小预算硬验证**

Run:
```
DOC=core/docs/superpowers/audits/2026-05-10-miniapp-health.md
wc -l "$DOC"
wc -c "$DOC"
```

Expected: 行数 ≤ 800，字节 ≤ 60000（约 15K tokens）。

如超出预算，按以下顺序裁剪：
1. §4 dead_code 项合并为 `dead_apis: [name1, name2, ...]` 单行列表
2. §5 only_mp 类 pair 仅留 `id / op / pair_type / refs`
3. §3 verdict=ok 的页面只留 `id / file / verdict: ok`

- [ ] **Step 7.2: ID 唯一性自检**

Run:
```
DOC=core/docs/superpowers/audits/2026-05-10-miniapp-health.md
grep -oE "id: P[012]-[0-9]+|id: X-[0-9]+|id: NEW-[0-9]+|id: F-[a-z]+-[a-z]+" "$DOC" | sort | uniq -d
```

Expected: 空输出（无重复 ID）。如有重复，重新分配编号。

- [ ] **Step 7.3: 孤儿发现自检**

每个 P0/P1 finding ID 必须在 §1 top3_risks 或 §3 issues 或 §5 refs 中至少出现一次。

Run:
```
DOC=core/docs/superpowers/audits/2026-05-10-miniapp-health.md
# 定义集
grep -oE "^[[:space:]]+id: P[012]-[0-9]+" "$DOC" | awk '{print $2}' | sort -u > /tmp/defined_ids.txt
# 引用集（任何位置出现的 ID 字面量）
grep -oE "P[012]-[0-9]+" "$DOC" | sort -u > /tmp/all_ids.txt
# 仅在定义集出现但未在引用集出现的 ID（孤儿）
comm -23 /tmp/defined_ids.txt /tmp/all_ids.txt
```

Expected: 空（每个 P0/P1 至少被引用一次）。如有孤儿，回到 §1/§3/§5 补充引用。

- [ ] **Step 7.4: yaml 块语法验证**

Run:
```
DOC=core/docs/superpowers/audits/2026-05-10-miniapp-health.md
# 提取所有 yaml 块到临时文件并 yaml 解析
awk '/^```yaml$/,/^```$/' "$DOC" | grep -v "^```" > /tmp/all-yaml.yaml
python -c "import yaml,sys; list(yaml.safe_load_all(open('/tmp/all-yaml.yaml'))); print('OK')"
```

Expected: `OK`。如失败，定位 syntax error 行并修正。

- [ ] **Step 7.5: gstack/review 终审**

Run:
```
Skill(skill="gstack/review")
```

Prompt to gstack:
> 范围：core/docs/superpowers/audits/2026-05-10-miniapp-health.md
> 关注：
> - yaml 语法正确性（无 indent error）
> - ID 引用一致性（refs 中每个 ID 在文档其他位置必有 id 定义）
> - 字段完整性（每个 finding 含 spec 要求的全部字段）
> - 上下文友好度（无散文堆砌、无表格、字段名简短稳定）
> - 与 spec §4-§8 schema 一致性

修复任何发现的问题。

- [ ] **Step 7.6: Final commit**

```
git add core/docs/superpowers/audits/2026-05-10-miniapp-health.md
git commit -m "audit: finalize miniapp comprehensive health audit report"
```

- [ ] **Step 7.7: 工作区保留确认**

```
ls -la core/docs/superpowers/audits/_workspace/
```

Expected: yaml 文件存在，便于后续复查 / 重跑某节。

---

## Self-Review Checklist

| Spec 要求 | 对应 Task / Step |
|---|---|
| §0 元信息 yaml frontmatter | Task 1.2 + 1.3 |
| §1 速览 / dim_scores / top3_risks | Task 6.1-6.4 |
| §2 API 契约层 / 4 个 block | Task 2.1-2.6 |
| §3 功能完整性 / 8 页面 / 5 项 check | Task 3.1-3.3 |
| §4 代码质量 / type 枚举 | Task 4.1-4.4 |
| §5 横向对照 / 4 种 pair_type / NEW-NNN | Task 5.1-5.5 |
| ID 全局唯一 | Task 7.2 |
| 孤儿发现检查 | Task 7.3 |
| 大小预算 ≤ 800 行 | Task 7.1 + 各节子检 (2.7/3.4/4.5/5.6/6.5) |
| yaml 语法正确 | Task 7.4 |
| gstack 能力使用 | Task 2.4 (investigate) / Task 4.1 (review) / Task 5.3 (investigate) / Task 7.5 (review) |
| 不实跑 / 不改代码 | 全程 read-only + audit md write，无 miniapp 启动、无 source 改动 |

**Placeholder scan:** 无 TBD/TODO/FIXME。所有步骤含具体命令和 yaml 示例。

**Type consistency:** 全文 yaml 字段名与 spec §4-§8 schema 一致（id/t/sev/be/mp/ma/diff/evi/imp/fix/refs/checks/verdict/pair_type/op）。

**Scope check:** 单文档静态审计 + 跨端对照，不含修复 plan / 测试 / live 实跑。

**gstack 应用点:**
- Task 2.4 → `gstack/investigate` 对比 mp ↔ be 端点
- Task 4.1 → `gstack/review` 扫描代码质量
- Task 5.3 → `gstack/investigate` 配对 mp ↔ ma 业务操作
- Task 7.5 → `gstack/review` 终审 audit md
