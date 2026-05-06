# Thesis Latest Diagrams Replacement Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Replace the old images in `初稿1-答辩佐证版-图文增强版.docx` with corrected latest thesis diagrams, using gpt-image-2 selectively to fix source-accuracy and overlap problems without excessive image-generation cost.

**Architecture:** First build a source fact checklist from the current `D:\sky-delivery` code and compare it against the 10 latest diagrams in `D:\architecture-diagrams\sky-delivery-thesis`. Only diagrams with factual errors, missing information, or unreadable overlap are regenerated or edited with gpt-image-2; acceptable diagrams are reused. The DOCX replacement is then performed as a local document operation: remove old figure blocks, insert only the selected 10 latest diagrams, and run non-rendering OOXML/package QA.

**Tech Stack:** gpt-image-2 via the image generation workflow for selected bitmap diagram fixes, Microsoft Word DOCX/OOXML inspection, `python-docx` or equivalent local DOCX editing for insertion only, PowerShell for file checks. Final page render validation is explicitly skipped.

---

## File Structure

Read-only inputs:

- `D:\sky-delivery\docs\superpowers\specs\2026-05-06-thesis-latest-diagrams-replacement-design.md`
- `D:\architecture-diagrams\sky-delivery-thesis\chart-info.md`
- `D:\architecture-diagrams\sky-delivery-thesis\fig-2-1-tech-selection.png`
- `D:\architecture-diagrams\sky-delivery-thesis\fig-4-1-system-architecture.png`
- `D:\architecture-diagrams\sky-delivery-thesis\fig-4-2-deployment-structure.png`
- `D:\architecture-diagrams\sky-delivery-thesis\fig-4-3-core-er-model.png`
- `D:\architecture-diagrams\sky-delivery-thesis\fig-4-4-role-permission-matrix.png`
- `D:\architecture-diagrams\sky-delivery-thesis\fig-5-1-login-auth-flow.png`
- `D:\architecture-diagrams\sky-delivery-thesis\fig-5-2-merchant-switch-flow.png`
- `D:\architecture-diagrams\sky-delivery-thesis\fig-5-3-shopping-cart-scope-flow.png`
- `D:\architecture-diagrams\sky-delivery-thesis\fig-5-4-order-submit-flow.png`
- `D:\architecture-diagrams\sky-delivery-thesis\fig-5-5-order-notification-flow.png`
- `C:\Users\g'y'c\Desktop\毕业论文\初稿1-答辩佐证版-图文增强版.docx`

Generated or modified outputs:

- `D:\architecture-diagrams\sky-delivery-thesis\corrected\fig-2-1-tech-selection.png`
- `D:\architecture-diagrams\sky-delivery-thesis\corrected\fig-4-1-system-architecture.png`
- `D:\architecture-diagrams\sky-delivery-thesis\corrected\fig-4-2-deployment-structure.png`
- `D:\architecture-diagrams\sky-delivery-thesis\corrected\fig-4-3-core-er-model.png`
- `D:\architecture-diagrams\sky-delivery-thesis\corrected\fig-4-4-role-permission-matrix.png`
- `D:\architecture-diagrams\sky-delivery-thesis\corrected\fig-5-1-login-auth-flow.png`
- `D:\architecture-diagrams\sky-delivery-thesis\corrected\fig-5-2-merchant-switch-flow.png`
- `D:\architecture-diagrams\sky-delivery-thesis\corrected\fig-5-3-shopping-cart-scope-flow.png`
- `D:\architecture-diagrams\sky-delivery-thesis\corrected\fig-5-4-order-submit-flow.png`
- `D:\architecture-diagrams\sky-delivery-thesis\corrected\fig-5-5-order-notification-flow.png`
- `D:\sky-delivery\docs\thesis-visual-evidence\qa\latest-gptimage2-diagram-triage.md`
- `D:\sky-delivery\docs\thesis-visual-evidence\qa\latest-gptimage2-prompts.md`
- `D:\sky-delivery\docs\thesis-visual-evidence\qa\latest-docx-replacement-report.json`
- `D:\sky-delivery\docs\thesis-visual-evidence\qa\latest-diagram-replacement-qa.json`
- `C:\Users\g'y'c\Desktop\毕业论文\初稿1-答辩佐证版-图文增强版.before-latest-diagrams-20260506.docx`
- `C:\Users\g'y'c\Desktop\毕业论文\初稿1-答辩佐证版-图文增强版.docx`

## Cost Control Rules for gpt-image-2

- Do not regenerate all 10 diagrams by default.
- First inspect each current PNG for factual accuracy and visible overlap.
- Reuse a current PNG if it already passes content checks and is readable.
- Use gpt-image-2 only for diagrams marked `Needs generation`.
- Limit each diagram to one generation attempt plus one targeted retry if text is close but one or two items are wrong.
- If a generated result repeatedly misspells required technical text, stop and ask before spending more attempts.
- Keep prompts short, exact, and source-backed. Do not ask for decorative illustration.

## Source Truth Checklist

Every final diagram must match these facts:

- User token header: `authentication`
- Admin token header: `token`
- User login endpoint: `POST /user/user/login`
- Admin login endpoint: `POST /admin/employee/login`
- JWT claims: `userId`, `empId`, `merchantId`, `accountType`
- Account types: `STUDENT_USER=0`, `PLATFORM_ADMIN=1`, `MERCHANT_ADMIN=2`, `MERCHANT_STAFF=3`
- `User` fields: `id`, `openid`, `name`, `phone`, `sex`, `idNumber`, `avatar`, `createTime`; no `password`
- `Orders` statuses: `PENDING_PAYMENT(1)`, `TO_BE_CONFIRMED(2)`, `CONFIRMED(3)`, `DELIVERY_IN_PROGRESS(4)`, `COMPLETED(5)`, `CANCELLED(6)`
- Payment statuses: `UN_PAID(0)`, `PAID(1)`, `REFUND(2)`
- `ShoppingCart` fields: `id`, `userId`, `merchantId`, `dishId`, `setmealId`, `name`, `dishFlavor`, `number`, `amount`, `image`, `createTime`
- `OrderDetail` fields: `id`, `orderId`, `dishId`, `setmealId`, `name`, `dishFlavor`, `number`, `amount`, `image`
- WebSocket endpoint: `/ws/{sid}`
- WebSocket auth: URL parameter `?token=JWT_TOKEN`
- WebSocket sessions: `ConcurrentHashMap<String, Session> SESSION_MAP`
- WebSocket methods: `sendToAllClient()`, `sendToClient(sid, message)`
- Merchant scope frontend: `withMerchantScope(params, merchantId)` injects `merchantId` and `shopId`
- Merchant scope backend: `MerchantScopeUtils.resolveQueryMerchantId()`
- Order transaction: `submitOrder()` has `@Transactional`
- Order submit lock: `order:submit:lock:{userId}`, 10 seconds
- Mock payment success: `paySuccess()` sets `status = TO_BE_CONFIRMED(2)` and `payStatus = PAID(1)`
- User whitelist: `/user/user/login`, `/user/shop/**`, `/user/dish/list`, `/user/category/list`, `/user/setmeal/list`, `/user/common/download`
- Admin whitelist: `/admin/employee/login`, `/admin/common/download`

### Task 1: Build the Diagram Triage Sheet

**Files:**
- Read: `D:\architecture-diagrams\sky-delivery-thesis\*.png`
- Read: source files named in `D:\architecture-diagrams\sky-delivery-thesis\chart-info.md`
- Create: `D:\sky-delivery\docs\thesis-visual-evidence\qa\latest-gptimage2-diagram-triage.md`

- [ ] **Step 1: Create the triage document skeleton**

Create `D:\sky-delivery\docs\thesis-visual-evidence\qa\latest-gptimage2-diagram-triage.md`:

```markdown
# Latest Diagram gpt-image-2 Triage

Date: 2026-05-06

Scope: Inspect the 10 latest diagrams and decide which ones need gpt-image-2 regeneration. Reuse diagrams that are factually correct and readable.

| Diagram | Content Status | Readability Status | Decision | Required Fixes |
|---|---|---|---|---|
| fig-2-1-tech-selection.png | Pending | Pending | Pending | Pending |
| fig-4-1-system-architecture.png | Pending | Pending | Pending | Pending |
| fig-4-2-deployment-structure.png | Pending | Pending | Pending | Pending |
| fig-4-3-core-er-model.png | Pending | Pending | Pending | Pending |
| fig-4-4-role-permission-matrix.png | Pending | Pending | Pending | Pending |
| fig-5-1-login-auth-flow.png | Pending | Pending | Pending | Pending |
| fig-5-2-merchant-switch-flow.png | Pending | Pending | Pending | Pending |
| fig-5-3-shopping-cart-scope-flow.png | Pending | Pending | Pending | Pending |
| fig-5-4-order-submit-flow.png | Pending | Pending | Pending | Pending |
| fig-5-5-order-notification-flow.png | Pending | Pending | Pending | Pending |
```

- [ ] **Step 2: Inspect the current PNG dimensions and file health**

Run:

```powershell
Add-Type -AssemblyName System.Drawing
Get-ChildItem "D:\architecture-diagrams\sky-delivery-thesis" -Filter "fig-*.png" | ForEach-Object {
  $img=[System.Drawing.Image]::FromFile($_.FullName)
  [PSCustomObject]@{Name=$_.Name; Width=$img.Width; Height=$img.Height; Bytes=$_.Length}
  $img.Dispose()
} | Format-Table -AutoSize
```

Expected: 10 PNG files exist, have nonzero byte sizes, and are large enough for thesis insertion.

- [ ] **Step 3: Open and visually inspect each PNG**

For each diagram, check:

- Does any text overlap or get covered?
- Is any required fact from the Source Truth Checklist absent?
- Does any text contradict source facts?
- Is any dense label too small to read after insertion into a Word page?

Update `latest-gptimage2-diagram-triage.md` with one of:

- `Reuse`: source-correct and readable.
- `Needs generation`: source error, missing fact, or serious overlap.
- `Needs one retry only`: a gpt-image-2 result is close but has one small text error.

- [ ] **Step 4: Commit Task 1**

Run:

```powershell
git add -- "docs/thesis-visual-evidence/qa/latest-gptimage2-diagram-triage.md"
git commit -m "docs: triage latest thesis diagrams for gpt-image-2"
```

### Task 2: Generate Only the Diagrams Marked as Needed

**Files:**
- Read: `D:\sky-delivery\docs\thesis-visual-evidence\qa\latest-gptimage2-diagram-triage.md`
- Create: `D:\sky-delivery\docs\thesis-visual-evidence\qa\latest-gptimage2-prompts.md`
- Create/Copy: `D:\architecture-diagrams\sky-delivery-thesis\corrected\*.png`

- [ ] **Step 1: Create the corrected output directory**

Run:

```powershell
New-Item -ItemType Directory -Force "D:\architecture-diagrams\sky-delivery-thesis\corrected"
```

Expected: directory exists.

- [ ] **Step 2: Copy reusable diagrams into corrected directory**

For each `Reuse` diagram from the triage sheet, run:

```powershell
Copy-Item "D:\architecture-diagrams\sky-delivery-thesis\<diagram-name>.png" "D:\architecture-diagrams\sky-delivery-thesis\corrected\<diagram-name>.png" -Force
```

Expected: corrected directory has a same-named PNG for every reused diagram.

- [ ] **Step 3: Write one exact prompt per diagram that needs gpt-image-2**

Create `D:\sky-delivery\docs\thesis-visual-evidence\qa\latest-gptimage2-prompts.md`.

Use this template for each `Needs generation` diagram:

```markdown
## <diagram file name>

Use case: infographic-diagram
Asset type: graduation thesis architecture/process diagram
Primary request: Create a clean academic Chinese diagram matching the title and exact source facts below.
Style/medium: flat professional infographic, white background, light academic palette, crisp text, no decorative illustration.
Composition/framing: 16:9 or near 4:3 landscape, generous spacing, no overlapping labels, large readable Chinese text.
Text requirements: Use the exact technical strings below verbatim.
Source facts:
- <fact 1>
- <fact 2>
- <fact 3>
Constraints:
- Do not invent endpoints, fields, status values, roles, or production capabilities.
- Do not use any old screenshot evidence.
- Keep all text readable at Word-page width.
- No watermark.
Avoid:
- Overlapping text
- Tiny text
- Decorative icons that hide labels
- English-only labels where Chinese thesis wording is expected
```

For `fig-5-5-order-notification-flow.png`, include these exact strings:

```text
/ws/{sid}
?token=JWT_TOKEN
ConcurrentHashMap<String, Session> SESSION_MAP
sendToAllClient()
sendToClient(sid, message)
```

For `fig-4-3-core-er-model.png`, include:

```text
User: id, openid, name, phone, sex, idNumber, avatar, createTime
ShoppingCart: id, userId, merchantId, dishId, setmealId, name, dishFlavor, number, amount, image, createTime
OrderDetail: id, orderId, dishId, setmealId, name, dishFlavor, number, amount, image
Do not include password in User.
```

- [ ] **Step 4: Use gpt-image-2 selectively**

For each diagram marked `Needs generation`, use gpt-image-2 through the image generation workflow with the corresponding prompt from `latest-gptimage2-prompts.md`.

Expected for each generated diagram:

- The image is saved into `D:\architecture-diagrams\sky-delivery-thesis\corrected\<diagram-name>.png`.
- The diagram contains the required exact technical strings.
- The diagram has no text overlap or missing key facts.

- [ ] **Step 5: Retry only if one targeted fix is needed**

If a generated image has one or two specific text mistakes, issue one targeted gpt-image-2 retry with this structure:

```text
Revise the previous diagram only to fix these exact text issues:
- Replace "<wrong text>" with "<correct text>"
- Add missing label "<missing exact text>"
Keep the same diagram layout, title, colors, and all other labels unchanged.
No new facts, no decorative additions, no watermark.
```

Stop after one retry per diagram unless the user explicitly approves more attempts.

- [ ] **Step 6: Ensure all 10 corrected PNGs exist**

Run:

```powershell
Get-ChildItem "D:\architecture-diagrams\sky-delivery-thesis\corrected" -Filter "fig-*.png" |
  Select-Object Name,Length,LastWriteTime | Format-Table -AutoSize
```

Expected: exactly 10 PNG files exist.

- [ ] **Step 7: Commit Task 2 report**

Run:

```powershell
git add -- "docs/thesis-visual-evidence/qa/latest-gptimage2-prompts.md" "docs/thesis-visual-evidence/qa/latest-gptimage2-diagram-triage.md"
git commit -m "docs: record gpt-image-2 thesis diagram prompts"
```

Note: `D:\architecture-diagrams` may be outside the Git repository. If so, do not force it into this repo; record the corrected diagram paths in the final response.

### Task 3: Replace Old Images in the Enhanced DOCX

**Files:**
- Read: `C:\Users\g'y'c\Desktop\毕业论文\初稿1-答辩佐证版-图文增强版.docx`
- Read: `D:\architecture-diagrams\sky-delivery-thesis\corrected\*.png`
- Create: `C:\Users\g'y'c\Desktop\毕业论文\初稿1-答辩佐证版-图文增强版.before-latest-diagrams-20260506.docx`
- Modify: `C:\Users\g'y'c\Desktop\毕业论文\初稿1-答辩佐证版-图文增强版.docx`
- Create: `D:\sky-delivery\docs\thesis-visual-evidence\qa\latest-docx-replacement-report.json`

- [ ] **Step 1: Back up the target DOCX**

Run:

```powershell
Copy-Item -LiteralPath "C:\Users\g'y'c\Desktop\毕业论文\初稿1-答辩佐证版-图文增强版.docx" `
  -Destination "C:\Users\g'y'c\Desktop\毕业论文\初稿1-答辩佐证版-图文增强版.before-latest-diagrams-20260506.docx" `
  -Force
```

Expected: backup exists and is not empty.

- [ ] **Step 2: Remove old figure blocks**

Open the target DOCX and remove old inserted image paragraphs, old transition sentences, and these old captions:

```text
图4-1 系统总体框架图（依据源码与配置绘制）
图4-2 系统部署与证据采集结构图
图4-3 核心数据模型ER图（依据数据库脚本与实体类绘制）
图5-1 登录认证与接口边界流程图
图5-2 商户切换与作用域传递流程图
图5-3 小程序端界面实现证据（来源于项目既有界面图）
图5-4 购物车商户隔离流程图
图5-5 订单提交与支付边界流程图
图5-6 订单通知与WebSocket推送流程图
图5-7 Web管理端界面运行证据
图6-1 系统验证结果证据
```

Keep original thesis text and tables, including:

```text
表3.1 主要角色与需求边界
表4.1 核心实体与作用域字段
表5.1 关键接口与页面映射
表6.1 主要功能验证项
```

- [ ] **Step 3: Insert the 10 corrected diagrams**

Insert the corrected images at these locations:

| Insert before heading | Image | Caption |
|---|---|---|
| `第三章 系统需求建模` | `fig-2-1-tech-selection.png` | `表2-1 技术选型表` |
| `4.2 前后端分离设计` | `fig-4-1-system-architecture.png` | `图4-1 系统总体框架图` |
| `4.3 后端模块设计` | `fig-4-2-deployment-structure.png` | `图4-2 系统部署与证据采集结构图` |
| `4.5 接口边界与安全设计` | `fig-4-3-core-er-model.png` | `图4-3 核心数据模型ER图` |
| `4.5 接口边界与安全设计` | `fig-4-4-role-permission-matrix.png` | `图4-4 角色权限矩阵` |
| `5.2 商户切换流程实现` | `fig-5-1-login-auth-flow.png` | `图5-1 登录认证与接口边界流程图` |
| `5.3 菜品浏览流程实现` | `fig-5-2-merchant-switch-flow.png` | `图5-2 商户切换与作用域传递流程图` |
| `5.5 订单提交流程实现` | `fig-5-3-shopping-cart-scope-flow.png` | `图5-3 购物车商户隔离流程图` |
| `5.7 订单通知流程实现` | `fig-5-4-order-submit-flow.png` | `图5-4 订单提交与支付边界流程图` |
| `5.8 历史订单与再来一单流程实现` | `fig-5-5-order-notification-flow.png` | `图5-5 订单通知与WebSocket推送流程图` |

Use centered images and centered captions. Keep page template, headings, body style, header, footer, and existing tables.

- [ ] **Step 4: Write the replacement report**

Create `D:\sky-delivery\docs\thesis-visual-evidence\qa\latest-docx-replacement-report.json` with:

```json
{
  "target": "C:\\Users\\g'y'c\\Desktop\\毕业论文\\初稿1-答辩佐证版-图文增强版.docx",
  "backup": "C:\\Users\\g'y'c\\Desktop\\毕业论文\\初稿1-答辩佐证版-图文增强版.before-latest-diagrams-20260506.docx",
  "diagram_source_dir": "D:\\architecture-diagrams\\sky-delivery-thesis\\corrected",
  "inserted_captions": [
    "表2-1 技术选型表",
    "图4-1 系统总体框架图",
    "图4-2 系统部署与证据采集结构图",
    "图4-3 核心数据模型ER图",
    "图4-4 角色权限矩阵",
    "图5-1 登录认证与接口边界流程图",
    "图5-2 商户切换与作用域传递流程图",
    "图5-3 购物车商户隔离流程图",
    "图5-4 订单提交与支付边界流程图",
    "图5-5 订单通知与WebSocket推送流程图"
  ],
  "removed_old_captions": [
    "图5-3 小程序端界面实现证据（来源于项目既有界面图）",
    "图5-7 Web管理端界面运行证据",
    "图6-1 系统验证结果证据"
  ],
  "render_validation": "skipped by explicit user instruction"
}
```

- [ ] **Step 5: Commit Task 3 report**

Run:

```powershell
git add -- "docs/thesis-visual-evidence/qa/latest-docx-replacement-report.json"
git commit -m "docs: record latest thesis docx diagram replacement"
```

### Task 4: Non-Rendering QA

**Files:**
- Read: `C:\Users\g'y'c\Desktop\毕业论文\初稿1-答辩佐证版-图文增强版.docx`
- Read: `C:\Users\g'y'c\Desktop\毕业论文\初稿1-答辩佐证版-图文增强版.before-latest-diagrams-20260506.docx`
- Create: `D:\sky-delivery\docs\thesis-visual-evidence\qa\latest-diagram-replacement-qa.json`

- [ ] **Step 1: Check DOCX package opens as ZIP**

Run:

```powershell
$docx = "C:\Users\g'y'c\Desktop\毕业论文\初稿1-答辩佐证版-图文增强版.docx"
Add-Type -AssemblyName System.IO.Compression.FileSystem
$zip = [System.IO.Compression.ZipFile]::OpenRead($docx)
$entries = $zip.Entries | Select-Object -ExpandProperty FullName
$zip.Dispose()
$entries | Where-Object { $_ -in @("[Content_Types].xml","word/document.xml","word/_rels/document.xml.rels","word/styles.xml") }
```

Expected: all four required entries are printed.

- [ ] **Step 2: Check captions and old-caption removal**

Use Word search or OOXML text extraction to confirm:

Expected present:

```text
表2-1 技术选型表
图4-1 系统总体框架图
图4-2 系统部署与证据采集结构图
图4-3 核心数据模型ER图
图4-4 角色权限矩阵
图5-1 登录认证与接口边界流程图
图5-2 商户切换与作用域传递流程图
图5-3 购物车商户隔离流程图
图5-4 订单提交与支付边界流程图
图5-5 订单通知与WebSocket推送流程图
```

Expected absent:

```text
图5-3 小程序端界面实现证据（来源于项目既有界面图）
图5-7 Web管理端界面运行证据
图6-1 系统验证结果证据
```

- [ ] **Step 3: Check media count and relationships**

Run:

```powershell
$docx = "C:\Users\g'y'c\Desktop\毕业论文\初稿1-答辩佐证版-图文增强版.docx"
Add-Type -AssemblyName System.IO.Compression.FileSystem
$zip = [System.IO.Compression.ZipFile]::OpenRead($docx)
$media = $zip.Entries | Where-Object { $_.FullName -like "word/media/*" }
$rels = $zip.GetEntry("word/_rels/document.xml.rels")
$reader = New-Object System.IO.StreamReader($rels.Open())
$relsText = $reader.ReadToEnd()
$reader.Close()
$zip.Dispose()
[PSCustomObject]@{MediaCount=$media.Count; ImageRelCount=([regex]::Matches($relsText, "/image")).Count}
```

Expected: media count and image relationship count are at least 10.

- [ ] **Step 4: Write the QA report**

Create `D:\sky-delivery\docs\thesis-visual-evidence\qa\latest-diagram-replacement-qa.json`:

```json
{
  "target": "C:\\Users\\g'y'c\\Desktop\\毕业论文\\初稿1-答辩佐证版-图文增强版.docx",
  "backup": "C:\\Users\\g'y'c\\Desktop\\毕业论文\\初稿1-答辩佐证版-图文增强版.before-latest-diagrams-20260506.docx",
  "checks": {
    "backup_exists": "PASS",
    "ooxml_required_parts_present": "PASS",
    "expected_new_captions_present": "PASS",
    "forbidden_old_captions_absent": "PASS",
    "media_count_at_least_10": "PASS",
    "image_relationships_at_least_10": "PASS",
    "render_validation": "SKIPPED_BY_USER_REQUEST"
  }
}
```

- [ ] **Step 5: Commit Task 4 report**

Run:

```powershell
git add -- "docs/thesis-visual-evidence/qa/latest-diagram-replacement-qa.json"
git commit -m "docs: add nonrender qa for latest thesis diagrams"
```

### Task 5: Final Handoff

**Files:**
- Read: `D:\sky-delivery\docs\thesis-visual-evidence\qa\latest-gptimage2-diagram-triage.md`
- Read: `D:\sky-delivery\docs\thesis-visual-evidence\qa\latest-gptimage2-prompts.md`
- Read: `D:\sky-delivery\docs\thesis-visual-evidence\qa\latest-diagram-replacement-qa.json`
- Read: `C:\Users\g'y'c\Desktop\毕业论文\初稿1-答辩佐证版-图文增强版.docx`

- [ ] **Step 1: Confirm target files exist**

Run:

```powershell
Get-Item -LiteralPath "C:\Users\g'y'c\Desktop\毕业论文\初稿1-答辩佐证版-图文增强版.docx" |
  Select-Object FullName,Length,LastWriteTime
Get-Item -LiteralPath "C:\Users\g'y'c\Desktop\毕业论文\初稿1-答辩佐证版-图文增强版.before-latest-diagrams-20260506.docx" |
  Select-Object FullName,Length,LastWriteTime
Get-ChildItem "D:\architecture-diagrams\sky-delivery-thesis\corrected" -Filter "fig-*.png" |
  Select-Object Name,Length,LastWriteTime | Format-Table -AutoSize
```

Expected: final DOCX exists, backup exists, corrected directory contains 10 PNGs.

- [ ] **Step 2: Final response content**

Report these facts:

- Final DOCX path: `C:\Users\g'y'c\Desktop\毕业论文\初稿1-答辩佐证版-图文增强版.docx`
- Backup path: `C:\Users\g'y'c\Desktop\毕业论文\初稿1-答辩佐证版-图文增强版.before-latest-diagrams-20260506.docx`
- Corrected diagram directory: `D:\architecture-diagrams\sky-delivery-thesis\corrected`
- gpt-image-2 was used selectively, only for diagrams marked as needing generation.
- Final page render validation was skipped at the user’s request.

## Self-Review

### Spec Coverage

| Spec requirement | Covered by |
|---|---|
| Use enhanced DOCX as target | Task 3, Task 4, Task 5 |
| Back up target DOCX | Task 3 |
| Use only latest 10 diagrams | Task 2, Task 3, Task 4 |
| Remove old 11 figures and screenshot captions | Task 3, Task 4 |
| Align diagram text to current source | Source Truth Checklist, Task 1, Task 2 |
| Use gpt-image-2 but reduce cost | Cost Control Rules, Task 1, Task 2 |
| Preserve body structure and template parts | Task 3, Task 4 |
| Skip final render validation | Task 4, Task 5 |

### Placeholder Scan

The plan contains no unresolved implementation placeholders. The literal `<diagram-name>` and `<fact 1>` examples appear only inside reusable command/prompt templates that are filled per triage row during execution.

### Type Consistency

The final caption list is consistent across DOCX insertion, QA, and final handoff: `表2-1`, `图4-1` through `图4-4`, and `图5-1` through `图5-5`.

## Execution Choice

Plan complete and saved to `docs/superpowers/plans/2026-05-06-thesis-latest-diagrams-replacement.md`.

Two execution options:

**1. Subagent-Driven (recommended)** - Use separate workers for triage, gpt-image-2 prompt generation, DOCX replacement, and QA.

**2. Inline Execution** - Execute tasks in this session with checkpoints and only call gpt-image-2 for diagrams the triage marks as needing generation.

Which approach?
