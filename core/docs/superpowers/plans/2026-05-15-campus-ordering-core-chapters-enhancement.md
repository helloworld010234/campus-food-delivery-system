# Campus Ordering Thesis Core Chapters Enhancement Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Upgrade the thesis body so Chapters 2-6 clearly explain a campus ordering system: students order meals, merchants process orders and view operating data, and the platform centrally manages merchants.

**Architecture:** Treat the DOCX as the presentation layer and edit it through a controlled extraction, drafting, replacement, and visual QA pipeline. Work in `D:\sky-delivery\core\paper_work\core_chapters_upgrade`, preserve the original Word formatting, title hierarchy, figures, captions, and clean screenshots, and never generate PDF output. The thesis body may be substantially rewritten where the current prose is empty, repetitive, or inconsistent with the campus ordering system line. Use the source code only as evidence for the article logic, not as text pasted into the thesis.

**Tech Stack:** DOCX/OOXML, bundled Python `C:\Users\g'y'c\.cache\codex-runtimes\codex-primary-runtime\dependencies\python\python.exe`, `python-docx 1.2.0`, PowerShell, Documents artifact-tool renderer, Spring Boot/Java source inspection, Vue/JS source inspection, SQL schema inspection.

---

## Source And Output Files

**Read-only source document:**
- `C:\Users\g'y'c\Desktop\毕业论文1_完整版 - 副本.docx`

**Design source:**
- `D:\sky-delivery\core\docs\superpowers\specs\2026-05-15-campus-delivery-core-chapters-enhancement-design.md`

**Create under workspace:**
- `D:\sky-delivery\core\paper_work\core_chapters_upgrade\source\毕业论文1_完整版 - 副本.source.docx` - local working copy of the input DOCX.
- `D:\sky-delivery\core\paper_work\core_chapters_upgrade\notes\docx_outline.md` - extracted chapter outline and paragraph index inventory.
- `D:\sky-delivery\core\paper_work\core_chapters_upgrade\notes\docx_paragraphs.json` - paragraph index, style, and text dump used for precise replacement.
- `D:\sky-delivery\core\paper_work\core_chapters_upgrade\notes\source_evidence_digest.md` - chapter-by-chapter evidence summary from backend, database, web, and miniapp source.
- `D:\sky-delivery\core\paper_work\core_chapters_upgrade\drafts\chapter_02.md` through `chapter_06.md` - rewritten chapter body drafts.
- `D:\sky-delivery\core\paper_work\core_chapters_upgrade\drafts\front_back_sync.md` - light synchronization notes for abstract, introduction, and conclusion.
- `D:\sky-delivery\core\paper_work\core_chapters_upgrade\notes\replacements.json` - paragraph replacement ranges and final text.
- `D:\sky-delivery\core\paper_work\core_chapters_upgrade\scripts\extract_docx_outline.py` - deterministic DOCX paragraph extractor.
- `D:\sky-delivery\core\paper_work\core_chapters_upgrade\scripts\apply_docx_replacements.py` - controlled paragraph-range replacement script.
- `D:\sky-delivery\core\paper_work\core_chapters_upgrade\scripts\verify_docx_text.py` - thesis text verification script.
- `D:\sky-delivery\core\paper_work\core_chapters_upgrade\outputs\毕业论文1_完整版_正文增强版.docx` - final enhanced DOCX.
- `D:\sky-delivery\core\paper_work\core_chapters_upgrade\renders\final` - PNG page renders for visual QA.

**Modify in repository:**
- `docs/superpowers/plans/2026-05-15-campus-ordering-core-chapters-enhancement.md` - this execution plan only.

---

## Body Rewrite Rules

- The body text may receive major modifications in Chapters 2-6 when doing so improves completeness, clarity, and thesis quality.
- Allowed changes include rewriting weak paragraphs, merging repetitive paragraphs, reordering explanation inside the same chapter, and replacing old-topic paragraphs with campus ordering system analysis.
- Protected items include school formatting, heading levels, figure/table captions, existing clean screenshots, generated diagrams, references, appendices, and the overall DOCX structure.
- Every major rewrite must still explain a concrete design reason: student ordering efficiency, merchant order processing and operating data, platform centralized merchant management, order status reliability, or merchant data isolation.
- New prose must be readable to non-specialist teachers and must avoid source-code dumping.

---

## Task 1: Prepare Workspace And Preserve Input

**Files:**
- Create: `D:\sky-delivery\core\paper_work\core_chapters_upgrade\source\毕业论文1_完整版 - 副本.source.docx`
- Create: `D:\sky-delivery\core\paper_work\core_chapters_upgrade\notes`
- Create: `D:\sky-delivery\core\paper_work\core_chapters_upgrade\drafts`
- Create: `D:\sky-delivery\core\paper_work\core_chapters_upgrade\scripts`
- Create: `D:\sky-delivery\core\paper_work\core_chapters_upgrade\outputs`
- Create: `D:\sky-delivery\core\paper_work\core_chapters_upgrade\renders\final`

- [ ] **Step 1: Confirm the source DOCX exists**

Run:

```powershell
Test-Path -LiteralPath "C:\Users\g'y'c\Desktop\毕业论文1_完整版 - 副本.docx"
```

Expected: `True`

- [ ] **Step 2: Create the workspace directories**

Run each command separately:

```powershell
New-Item -ItemType Directory -Force -Path "D:\sky-delivery\core\paper_work\core_chapters_upgrade\source"
```

```powershell
New-Item -ItemType Directory -Force -Path "D:\sky-delivery\core\paper_work\core_chapters_upgrade\notes"
```

```powershell
New-Item -ItemType Directory -Force -Path "D:\sky-delivery\core\paper_work\core_chapters_upgrade\drafts"
```

```powershell
New-Item -ItemType Directory -Force -Path "D:\sky-delivery\core\paper_work\core_chapters_upgrade\scripts"
```

```powershell
New-Item -ItemType Directory -Force -Path "D:\sky-delivery\core\paper_work\core_chapters_upgrade\outputs"
```

```powershell
New-Item -ItemType Directory -Force -Path "D:\sky-delivery\core\paper_work\core_chapters_upgrade\renders\final"
```

Expected: each directory exists under `D:\sky-delivery\core\paper_work\core_chapters_upgrade`.

- [ ] **Step 3: Copy the input document into the workspace**

Run:

```powershell
Copy-Item -LiteralPath "C:\Users\g'y'c\Desktop\毕业论文1_完整版 - 副本.docx" -Destination "D:\sky-delivery\core\paper_work\core_chapters_upgrade\source\毕业论文1_完整版 - 副本.source.docx" -Force
```

Expected: the copied file exists and has non-zero length.

- [ ] **Step 4: Confirm the bundled document runtime**

Run:

```powershell
& "C:\Users\g'y'c\.cache\codex-runtimes\codex-primary-runtime\dependencies\python\python.exe" -c "import docx, lxml; print(docx.__version__)"
```

Expected: `1.2.0`

- [ ] **Step 5: Commit the plan before document work**

Run:

```powershell
git add -- docs/superpowers/plans/2026-05-15-campus-ordering-core-chapters-enhancement.md
```

```powershell
git commit -m "docs: plan campus ordering thesis body enhancement" -- docs/superpowers/plans/2026-05-15-campus-ordering-core-chapters-enhancement.md
```

Expected: a commit containing only this plan file.

---

## Task 2: Extract DOCX Outline And Paragraph Inventory

**Files:**
- Create: `D:\sky-delivery\core\paper_work\core_chapters_upgrade\scripts\extract_docx_outline.py`
- Create: `D:\sky-delivery\core\paper_work\core_chapters_upgrade\notes\docx_outline.md`
- Create: `D:\sky-delivery\core\paper_work\core_chapters_upgrade\notes\docx_paragraphs.json`

- [ ] **Step 1: Create the extractor script**

Create `D:\sky-delivery\core\paper_work\core_chapters_upgrade\scripts\extract_docx_outline.py` with this content:

```python
from __future__ import annotations

import argparse
import json
import re
import zipfile
from pathlib import Path
import xml.etree.ElementTree as ET

NS = {"w": "http://schemas.openxmlformats.org/wordprocessingml/2006/main"}
W = NS["w"]


def q(name: str) -> str:
    return f"{{{W}}}{name}"


def paragraph_text(paragraph: ET.Element) -> str:
    return "".join(node.text or "" for node in paragraph.iter(q("t"))).strip()


def paragraph_style(paragraph: ET.Element) -> str:
    p_pr = paragraph.find(q("pPr"))
    if p_pr is None:
        return ""
    p_style = p_pr.find(q("pStyle"))
    if p_style is None:
        return ""
    return p_style.attrib.get(q("val"), "")


def extract(docx_path: Path) -> list[dict[str, object]]:
    with zipfile.ZipFile(docx_path) as package:
        xml = package.read("word/document.xml")
    root = ET.fromstring(xml)
    body = root.find(q("body"))
    if body is None:
        raise RuntimeError("word/document.xml has no body element")

    rows: list[dict[str, object]] = []
    for index, paragraph in enumerate(body.findall(q("p"))):
        text = paragraph_text(paragraph)
        style = paragraph_style(paragraph)
        if text:
            rows.append({"index": index, "style": style, "text": text})
    return rows


def build_outline(rows: list[dict[str, object]]) -> str:
    chapter_re = re.compile(r"^第[一二三四五六七八九十]+章")
    section_re = re.compile(r"^[0-9]+(\.[0-9]+){1,3}\\s*")
    lines = ["# DOCX 段落与章节索引", ""]
    for row in rows:
        text = str(row["text"])
        style = str(row["style"])
        index = row["index"]
        if chapter_re.match(text) or section_re.match(text) or "摘要" == text or "结论" in text:
            lines.append(f"- `{index}` `{style}` {text}")
    lines.append("")
    lines.append("## 第 2-6 章替换时的使用方法")
    lines.append("")
    lines.append("1. 用上方索引定位每章标题段落。")
    lines.append("2. 在 `docx_paragraphs.json` 中读取该章标题之后、下一章标题之前的正文段落。")
    lines.append("3. 只替换正文段落，保留图题、表题、图片和章节标题段落。")
    return "\n".join(lines) + "\n"


def main() -> None:
    parser = argparse.ArgumentParser()
    parser.add_argument("--docx", required=True)
    parser.add_argument("--outdir", required=True)
    args = parser.parse_args()

    docx_path = Path(args.docx)
    outdir = Path(args.outdir)
    outdir.mkdir(parents=True, exist_ok=True)

    rows = extract(docx_path)
    (outdir / "docx_paragraphs.json").write_text(
        json.dumps(rows, ensure_ascii=False, indent=2),
        encoding="utf-8",
    )
    (outdir / "docx_outline.md").write_text(build_outline(rows), encoding="utf-8")
    print(f"paragraphs={len(rows)}")


if __name__ == "__main__":
    main()
```

- [ ] **Step 2: Run the extractor**

Run:

```powershell
& "C:\Users\g'y'c\.cache\codex-runtimes\codex-primary-runtime\dependencies\python\python.exe" "D:\sky-delivery\core\paper_work\core_chapters_upgrade\scripts\extract_docx_outline.py" --docx "D:\sky-delivery\core\paper_work\core_chapters_upgrade\source\毕业论文1_完整版 - 副本.source.docx" --outdir "D:\sky-delivery\core\paper_work\core_chapters_upgrade\notes"
```

Expected: console prints `paragraphs=<number greater than 100>`.

- [ ] **Step 3: Verify Chapter 2-6 headings are visible**

Run:

```powershell
Select-String -LiteralPath "D:\sky-delivery\core\paper_work\core_chapters_upgrade\notes\docx_outline.md" -Pattern "第二章|第三章|第四章|第五章|第六章"
```

Expected: at least five matches, one for each core chapter.

---

## Task 3: Build Source Evidence Digest

**Files:**
- Create: `D:\sky-delivery\core\paper_work\core_chapters_upgrade\notes\source_evidence_digest.md`
- Read: `D:\sky-delivery\core\backend\sky-server\src\main\java\com\sky\service\impl\ShoppingCartServiceImpl.java`
- Read: `D:\sky-delivery\core\backend\sky-server\src\main\java\com\sky\service\impl\OrderServiceImpl.java`
- Read: `D:\sky-delivery\core\backend\sky-server\src\main\java\com\sky\security\MerchantScopeGuard.java`
- Read: `D:\sky-delivery\core\backend\sky-server\src\main\java\com\sky\controller\admin\ReportController.java`
- Read: `D:\sky-delivery\core\backend\sky-server\src\main\java\com\sky\controller\admin\WorkSpaceController.java`
- Read: `D:\sky-delivery\core\backend\sky-server\src\main\java\com\sky\controller\admin\MerchantController.java`
- Read: `D:\sky-delivery\core\backend\scripts\phase1_multi_merchant_schema.sql`
- Read: `D:\sky-delivery\core\database\init.sql`
- Read: `D:\sky-delivery\core\miniapp\pages\merchant\index.vue`
- Read: `D:\sky-delivery\core\miniapp\pages\order\index.vue`

- [ ] **Step 1: Locate evidence files**

Run:

```powershell
Get-ChildItem -Path "D:\sky-delivery\core\backend","D:\sky-delivery\core\miniapp","D:\sky-delivery\core\nginx","D:\sky-delivery\core\database" -Recurse -File -Include *.java,*.js,*.sql,*.vue -ErrorAction SilentlyContinue | Where-Object { $_.FullName -match "ShoppingCart|Order|Merchant|Report|WorkSpace|merchant|order|init\.sql|multi_merchant" } | Select-Object -First 120 -ExpandProperty FullName
```

Expected: output includes the files listed in this task.

- [ ] **Step 2: Search for operating-data evidence**

Run:

```powershell
Get-ChildItem -Path "D:\sky-delivery\core\backend","D:\sky-delivery\core\miniapp","D:\sky-delivery\core\nginx","D:\sky-delivery\core\database" -Recurse -File -Include *.java,*.js,*.sql,*.vue -ErrorAction SilentlyContinue | Select-String -Pattern "BusinessData|turnover|OrderReport|SalesTop10|WorkSpace|营业|订单统计|merchantId|MerchantScope|shoppingCart" -CaseSensitive:$false | Select-Object -First 160 Path,LineNumber,Line
```

Expected: output includes report/workspace VO or controller evidence and merchant-scope evidence.

- [ ] **Step 3: Write the digest**

Create `D:\sky-delivery\core\paper_work\core_chapters_upgrade\notes\source_evidence_digest.md` with these sections and concrete source-based statements:

```markdown
# 源码证据摘要

## 学生点餐链路
- 小程序端存在商户选择页面、商品/套餐浏览页面、购物车页面、订单提交页面和历史订单页面。论文中应表述为“学生先选择商户，再浏览该商户的菜品并提交订单”，不写具体接口路径。
- 后端用户侧控制器覆盖分类、菜品、套餐、购物车和订单等模块。论文中应解释为“学生端操作由后端统一校验，避免前端直接改动订单或购物车数据”。

## 购物车归属
- 购物车实体、DTO 和服务实现中存在用户、商品、套餐、数量、金额等信息，并结合商户范围进行处理。论文中应表述为“购物车既属于学生，也需要和当前商户保持一致，避免跨商户混合结算”。

## 订单状态
- 订单提交、支付、接单、拒单、取消、完成等操作分散在用户端和管理端订单控制器、服务实现和 DTO 中。论文中应表述为“订单状态是系统的业务顺序表，每一步操作都必须满足当前状态条件”。

## 商家运营数据
- 管理端存在工作台、报表、营业额、订单统计、销量排行等 VO 或控制器。论文中应突出“商家不仅处理订单，还能查看营业额趋势、订单量和商品销售情况”。

## 平台统一管理商家
- 商户实体、DTO、Mapper、控制器、服务和多商户迁移脚本共同说明系统支持商家资料维护、启用状态和营业状态管理。论文中应表述为“平台统一维护商家基础资料和可用状态，商家再在自身范围内经营商品和订单”。

## 数据库关系
- 数据库脚本中包含用户、地址、商户、分类、商品、购物车、订单和订单明细等表。论文中应围绕“一名学生可有多个地址、一个商户可有多个商品、一个订单可有多条明细”讲清关系。

## 正文禁用表达
- 不使用旧项目名。
- 不把论文主线写成配送平台。
- 不在正文里堆叠类名、方法名和接口路径。
```

- [ ] **Step 4: Verify the digest uses the new thesis line**

Run:

```powershell
Select-String -LiteralPath "D:\sky-delivery\core\paper_work\core_chapters_upgrade\notes\source_evidence_digest.md" -Pattern "校园点餐|学生|商家|运营数据|平台|统一管理"
```

Expected: matches appear in the digest.

---

## Task 4: Draft Chapters 2 And 3

**Files:**
- Create: `D:\sky-delivery\core\paper_work\core_chapters_upgrade\drafts\chapter_02.md`
- Create: `D:\sky-delivery\core\paper_work\core_chapters_upgrade\drafts\chapter_03.md`

- [ ] **Step 1: Draft Chapter 2 around campus ordering scenes**

Create `chapter_02.md` with this writing structure:

```markdown
# 第二章增强稿

## 写作主线
第二章从校园点餐场景出发，解释系统为什么需要学生端、商家端和平台端三类角色。正文应让非专业老师先理解校园用餐的问题，再理解系统设计的必要性。

## 必须覆盖的内容
1. 校园用餐集中在课间、午餐和晚餐时段，线下窗口容易排队。
2. 学生需要快速知道有哪些商户、有哪些菜品、是否可以下单、订单当前处于什么状态。
3. 商家需要按状态处理订单，并通过订单量、营业额和商品销售情况了解经营状况。
4. 平台需要统一维护商家资料、启用状态和营业状态，让学生端看到的信息稳定一致。

## 建议正文段落
校园点餐系统的应用场景主要集中在学生日常用餐过程中。与普通信息管理系统相比，点餐场景具有时间集中、操作频繁和参与角色较多的特点。学生通常希望在较短时间内完成选店、选菜、提交订单和查看状态；商家则需要及时看到待处理订单，并根据订单变化安排出餐；平台侧需要保证商家资料、营业状态和展示状态统一维护，避免学生看到不可用或状态不准确的商家。

从学生角度看，系统需要解决的首要问题是信息获取效率。传统线下点餐往往需要学生到窗口后才能看到菜品和排队情况，而在线点餐可以把商家、分类、菜品、购物车和订单状态集中到小程序端展示。这样学生能够在手机上完成主要操作，减少反复询问和等待。

从商家角度看，系统的价值不只在于接收订单，还在于把经营过程数字化。商家可以根据订单状态区分待接单、已接单和已完成等不同任务，也可以通过营业额、订单量和商品销售情况判断当天经营情况。这样的设计比单纯记录订单更完整，更符合校园多商户经营的实际需求。

从平台角度看，系统需要承担统一管理责任。平台并不直接替商家处理每一笔订单，而是维护商家基础资料、启用状态和营业状态。只有平台侧基础数据稳定，学生端才能看到准确的商家列表，商家端也才能在自己的范围内进行商品维护和订单处理。
```

Expected: Chapter 2 draft does not use old project wording and does not describe the system as a delivery platform.

- [ ] **Step 2: Draft Chapter 3 around role requirements**

Create `chapter_03.md` with this writing structure:

```markdown
# 第三章增强稿

## 写作主线
第三章把功能列表改写为角色行为需求，按“学生要完成什么、商家要完成什么、平台要保证什么”展开。

## 必须覆盖的内容
1. 学生端：登录、选择商户、浏览商品、加入购物车、提交订单、支付、查看订单。
2. 商家端：维护分类/商品/套餐、处理订单、查看营业额趋势、订单量和商品销售。
3. 平台端：维护商家基础资料、启用状态、营业状态和基础统计。
4. 非功能需求：数据隔离、状态可靠、信息及时、演示兼容。

## 建议正文段落
系统需求可以按照学生、商家和平台三类角色进行划分。学生端需求重点在于完成点餐闭环，即从登录进入系统，到选择商户、浏览商品、维护购物车、提交订单，再到支付和查看订单状态。该过程面向移动端使用，因此界面和操作路径应尽量简洁。

商家端需求重点在于经营管理。商家需要维护本商户的分类、商品和套餐等基础信息，也需要处理来自学生端的订单。订单处理不能只靠人工记忆，而应由系统按照订单状态进行限制。与此同时，商家还需要查看营业额、订单量和商品销售情况，以便了解当前经营效果。

平台端需求重点在于统一管理商家。平台需要维护商家基础资料，控制商家是否启用、是否营业，并查看平台范围内的商家概览和基础统计。平台侧管理不等同于商家经营操作，它更像是系统的基础数据管理入口。

非功能需求主要体现在数据隔离、操作可靠和信息及时三个方面。数据隔离要求商家只能看到和处理自己范围内的数据；操作可靠要求订单状态不能随意跳转；信息及时要求支付成功、订单变化和催单等信息能够及时反馈给相关管理端。考虑到毕业设计演示环境，支付流程还应支持可演示的本地处理方式，保证系统能够稳定展示主要流程。
```

- [ ] **Step 3: Verify Chapters 2 and 3 drafts**

Run:

```powershell
Select-String -LiteralPath "D:\sky-delivery\core\paper_work\core_chapters_upgrade\drafts\chapter_02.md","D:\sky-delivery\core\paper_work\core_chapters_upgrade\drafts\chapter_03.md" -Pattern "校园外卖|苍穹外卖|sky_take_out|配送|送到|送达"
```

Expected: no matches.

---

## Task 5: Draft Chapters 4 And 5

**Files:**
- Create: `D:\sky-delivery\core\paper_work\core_chapters_upgrade\drafts\chapter_04.md`
- Create: `D:\sky-delivery\core\paper_work\core_chapters_upgrade\drafts\chapter_05.md`

- [ ] **Step 1: Draft Chapter 4 around system architecture**

Create `chapter_04.md` with this writing structure:

```markdown
# 第四章增强稿

## 写作主线
第四章解释系统为什么由小程序端、Web 管理端、后端服务和数据库组成，减少晦涩架构术语，使用“谁负责什么”的表达。

## 建议正文段落
系统总体结构可以理解为四个部分协同工作：小程序端负责学生点餐，Web 管理端负责商家和平台人员操作，后端服务负责统一处理业务规则，数据库负责保存长期数据。这样的划分符合不同角色的使用习惯，也有利于保证订单和商家数据的一致性。

小程序端面向学生，主要承担轻量化操作。学生通常在手机上完成选商户、选菜品、加入购物车和查看订单等动作，因此小程序端需要提供清晰的点餐路径。它不直接修改数据库，而是把学生操作提交给后端服务处理。

Web 管理端面向商家和平台管理人员，更适合展示表格、筛选条件和统计数据。商家可以在管理端维护商品、处理订单并查看运营数据；平台可以在管理端维护商家资料和状态。两类管理角色虽然使用同一类桌面界面，但管理范围不同。

后端服务是系统规则的集中处理位置。学生是否可以下单、商家是否可以处理某个订单、订单状态是否允许进入下一步，都需要由后端统一判断。数据库则保存用户、地址、商家、商品、购物车、订单和订单明细等数据，为系统运行提供持久化基础。
```

- [ ] **Step 2: Draft Chapter 5 around database and process design**

Create `chapter_05.md` with this writing structure:

```markdown
# 第五章增强稿

## 写作主线
第五章把数据库和流程设计讲成业务逻辑，重点解释数据关系、订单状态、支付结果处理和安全异常控制。

## 建议正文段落
数据库设计需要围绕校园点餐中的真实关系展开。学生和地址之间是一对多关系，一名学生可以维护多个常用地址；商家和商品、分类之间也是一对多关系，一个商家可以维护多个分类和多个商品；订单和订单明细之间是一对多关系，一笔订单保存整体信息，多条订单明细保存具体商品内容。通过这些关系，系统能够把学生、商家、商品和订单联系起来。

购物车设计需要特别考虑商家归属。学生在某一商家下选择的商品，不能和另一商家的商品混在同一个结算过程中。因此购物车数据不仅要记录学生和商品，也要记录当前商家范围。这样可以避免跨商家混单，使订单提交时的数据来源更清楚。

订单状态设计体现系统的业务秩序。未支付订单不能直接进入商家处理环节，支付成功后订单进入待接单状态，商家接单后才能继续出餐和完成。拒单、取消等操作也需要记录原因，并根据支付状态进行相应处理。通过状态限制，系统可以减少误操作，保证学生端和商家端看到的订单进度一致。

支付结果处理可以理解为外部支付结果进入系统后的状态更新过程。系统接收到支付成功结果后，会把订单从待支付改为待接单，使商家端能够看到新的待处理订单。论文中不需要展开支付平台的底层细节，只需要说明支付结果如何影响订单状态和后续业务。

安全和异常处理主要用于防止错误操作。登录令牌用于识别当前用户或管理员，商家范围校验用于防止商家访问其他商家的数据，订单重复提交控制用于降低短时间内重复下单的风险，全局异常处理则用于把错误信息以统一方式返回给前端。
```

- [ ] **Step 3: Verify Chapters 4 and 5 drafts**

Run:

```powershell
Select-String -LiteralPath "D:\sky-delivery\core\paper_work\core_chapters_upgrade\drafts\chapter_04.md","D:\sky-delivery\core\paper_work\core_chapters_upgrade\drafts\chapter_05.md" -Pattern "校园外卖|苍穹外卖|sky_take_out|配送|送到|送达"
```

Expected: no matches.

---

## Task 6: Draft Chapter 6 And Light Front/Back Synchronization

**Files:**
- Create: `D:\sky-delivery\core\paper_work\core_chapters_upgrade\drafts\chapter_06.md`
- Create: `D:\sky-delivery\core\paper_work\core_chapters_upgrade\drafts\front_back_sync.md`

- [ ] **Step 1: Draft Chapter 6 around implementation and running result**

Create `chapter_06.md` with this writing structure:

```markdown
# 第六章增强稿

## 写作主线
第六章围绕真实截图和已有图表解释系统运行效果。写法采用“模块解决的问题、系统处理流程、运行效果说明”三段式，避免把源码类名当成正文主体。

## 建议正文段落
学生端实现重点在于形成连续的点餐路径。学生进入系统后，先查看可用商家，再进入目标商家的商品列表。系统根据当前商家加载分类、菜品和套餐信息，使学生看到的内容具有明确来源。学生选择商品后，可以加入购物车并在结算页面确认订单信息。

添加购物车功能体现了数据归属控制。系统在加入商品时会确认商品和当前商家之间的关系，再判断购物车中是否已有相同商品。如果已经存在，则累加数量；如果不存在，则新增购物车记录。这样既能减少重复记录，也能保证购物车内容与当前商家保持一致。

订单提交与支付功能是学生点餐流程的关键。系统在生成订单前需要校验地址、商家状态、购物车内容和金额等信息。订单生成后，订单主表保存整体信息，订单明细保存每个商品的具体内容。支付结果返回后，订单状态会发生变化，商家端即可看到新的待处理订单。

商家端实现重点在于订单处理和运营数据展示。商家登录后只能处理本商户范围内的数据，不能查看其他商户订单。对于订单操作，系统根据当前状态限制接单、拒单、完成等动作，避免跳过流程。对于经营情况，管理端通过营业额、订单量和商品销售等数据帮助商家了解运行效果。

平台端实现重点在于统一管理商家。平台人员可以维护商家资料、启用状态和营业状态，这些状态会影响学生端是否能看到商家以及是否能够下单。平台端不替商家处理具体订单，而是保证商家基础数据统一、准确、可维护。
```

- [ ] **Step 2: Draft abstract/introduction/conclusion synchronization**

Create `front_back_sync.md` with this content:

```markdown
# 摘要、绪论与总结轻微同步稿

## 摘要同步重点
摘要中应把系统名称统一为“校园点餐系统”，概括学生点餐、商家订单处理和平台统一管理商家的完整业务闭环。摘要不增加源码细节，不出现旧项目口径。

## 绪论同步重点
绪论中保留原有研究背景和论文结构，只把研究对象改为校园点餐系统，并补充多商户、订单状态和运营数据三个关键词，使第一章和第 2-6 章保持一致。

## 总结同步重点
总结中强调系统完成了学生端点餐、商家端经营管理和平台端商家管理三个方面的设计与实现。后续展望可以写成“进一步完善推荐、评价、数据分析和移动端体验”，不写源码中不存在的功能已经完成。
```

- [ ] **Step 3: Verify Chapter 6 and sync draft**

Run:

```powershell
Select-String -LiteralPath "D:\sky-delivery\core\paper_work\core_chapters_upgrade\drafts\chapter_06.md","D:\sky-delivery\core\paper_work\core_chapters_upgrade\drafts\front_back_sync.md" -Pattern "校园外卖|苍穹外卖|sky_take_out|配送|送到|送达"
```

Expected: no matches.

---

## Task 7: Build Replacement Map And Apply DOCX Edits

**Files:**
- Create: `D:\sky-delivery\core\paper_work\core_chapters_upgrade\notes\replacements.json`
- Create: `D:\sky-delivery\core\paper_work\core_chapters_upgrade\scripts\apply_docx_replacements.py`
- Create: `D:\sky-delivery\core\paper_work\core_chapters_upgrade\outputs\毕业论文1_完整版_正文增强版.docx`
- Read: `D:\sky-delivery\core\paper_work\core_chapters_upgrade\notes\docx_paragraphs.json`
- Read: `D:\sky-delivery\core\paper_work\core_chapters_upgrade\drafts\chapter_02.md`
- Read: `D:\sky-delivery\core\paper_work\core_chapters_upgrade\drafts\chapter_03.md`
- Read: `D:\sky-delivery\core\paper_work\core_chapters_upgrade\drafts\chapter_04.md`
- Read: `D:\sky-delivery\core\paper_work\core_chapters_upgrade\drafts\chapter_05.md`
- Read: `D:\sky-delivery\core\paper_work\core_chapters_upgrade\drafts\chapter_06.md`

- [ ] **Step 1: Create `replacements.json` from the paragraph inventory**

Use `docx_outline.md` and `docx_paragraphs.json` to locate each chapter body range. Create `D:\sky-delivery\core\paper_work\core_chapters_upgrade\notes\replacements.json` as a JSON array. Each object must contain `chapter`, `start_index`, `end_index`, and `paragraphs`.

Rules for the file:
- `start_index` is the first original body paragraph after the chapter heading and before any retained figure/table caption.
- `end_index` is the last original body paragraph to replace before the next retained heading, figure/table caption, or chapter boundary.
- `paragraphs` contains final thesis paragraphs copied from the approved drafts, without Markdown headings and without list markers.
- Each replacement group must exclude image paragraphs, figure captions, table captions, bibliography, and appendices.
- Because body text is allowed to be substantially rewritten, one replacement group may replace several weak original paragraphs with a smaller number of stronger paragraphs, or expand a sparse original section into several clearer paragraphs, as long as protected items stay untouched.

- [ ] **Step 2: Create the DOCX replacement script**

Create `D:\sky-delivery\core\paper_work\core_chapters_upgrade\scripts\apply_docx_replacements.py` with this content:

```python
from __future__ import annotations

import argparse
import json
from pathlib import Path

from docx import Document
from docx.oxml import OxmlElement
from docx.text.paragraph import Paragraph


def set_paragraph_text(paragraph: Paragraph, text: str) -> None:
    if paragraph.runs:
        paragraph.runs[0].text = text
        for run in paragraph.runs[1:]:
            run.text = ""
    else:
        paragraph.add_run(text)


def insert_paragraph_after(paragraph: Paragraph, text: str, style_name: str | None) -> Paragraph:
    new_p = OxmlElement("w:p")
    paragraph._p.addnext(new_p)
    new_para = Paragraph(new_p, paragraph._parent)
    if style_name:
        new_para.style = style_name
    new_para.add_run(text)
    return new_para


def delete_paragraph(paragraph: Paragraph) -> None:
    element = paragraph._element
    parent = element.getparent()
    parent.remove(element)


def validate_replacement(item: dict[str, object]) -> None:
    required = {"chapter", "start_index", "end_index", "paragraphs"}
    missing = required - set(item)
    if missing:
        raise ValueError(f"replacement missing keys: {sorted(missing)}")
    paragraphs = item["paragraphs"]
    if not isinstance(paragraphs, list) or not paragraphs:
        raise ValueError(f"{item['chapter']} has no replacement paragraphs")
    for paragraph in paragraphs:
        if not isinstance(paragraph, str) or not paragraph.strip():
            raise ValueError(f"{item['chapter']} contains an empty paragraph")


def apply_replacements(input_docx: Path, replacements_path: Path, output_docx: Path) -> None:
    replacements = json.loads(replacements_path.read_text(encoding="utf-8"))
    for item in replacements:
        validate_replacement(item)

    doc = Document(str(input_docx))
    original_paragraphs = list(doc.paragraphs)

    for item in sorted(replacements, key=lambda row: int(row["start_index"]), reverse=True):
        start = int(item["start_index"])
        end = int(item["end_index"])
        new_texts = [str(text).strip() for text in item["paragraphs"]]

        if start < 0 or end >= len(original_paragraphs) or end < start:
            raise IndexError(f"bad paragraph range for {item['chapter']}: {start}-{end}")

        base = original_paragraphs[start]
        style_name = base.style.name if base.style is not None else None

        for idx in range(end, start, -1):
            delete_paragraph(original_paragraphs[idx])

        set_paragraph_text(base, new_texts[0])
        cursor = base
        for text in new_texts[1:]:
            cursor = insert_paragraph_after(cursor, text, style_name)

    output_docx.parent.mkdir(parents=True, exist_ok=True)
    doc.save(str(output_docx))
    print(output_docx)


def main() -> None:
    parser = argparse.ArgumentParser()
    parser.add_argument("--input", required=True)
    parser.add_argument("--replacements", required=True)
    parser.add_argument("--output", required=True)
    args = parser.parse_args()

    apply_replacements(Path(args.input), Path(args.replacements), Path(args.output))


if __name__ == "__main__":
    main()
```

- [ ] **Step 3: Apply replacements**

Run:

```powershell
& "C:\Users\g'y'c\.cache\codex-runtimes\codex-primary-runtime\dependencies\python\python.exe" "D:\sky-delivery\core\paper_work\core_chapters_upgrade\scripts\apply_docx_replacements.py" --input "D:\sky-delivery\core\paper_work\core_chapters_upgrade\source\毕业论文1_完整版 - 副本.source.docx" --replacements "D:\sky-delivery\core\paper_work\core_chapters_upgrade\notes\replacements.json" --output "D:\sky-delivery\core\paper_work\core_chapters_upgrade\outputs\毕业论文1_完整版_正文增强版.docx"
```

Expected: the output path is printed and the DOCX exists.

---

## Task 8: Verify Text Content And Terminology

**Files:**
- Create: `D:\sky-delivery\core\paper_work\core_chapters_upgrade\scripts\verify_docx_text.py`
- Read: `D:\sky-delivery\core\paper_work\core_chapters_upgrade\outputs\毕业论文1_完整版_正文增强版.docx`

- [ ] **Step 1: Create the verification script**

Create `D:\sky-delivery\core\paper_work\core_chapters_upgrade\scripts\verify_docx_text.py` with this content:

```python
from __future__ import annotations

import argparse
import sys
import zipfile
from pathlib import Path
import xml.etree.ElementTree as ET

W = "http://schemas.openxmlformats.org/wordprocessingml/2006/main"


def q(name: str) -> str:
    return f"{{{W}}}{name}"


def docx_text(path: Path) -> str:
    with zipfile.ZipFile(path) as package:
        xml = package.read("word/document.xml")
    root = ET.fromstring(xml)
    return "\n".join(node.text or "" for node in root.iter(q("t")))


def main() -> None:
    parser = argparse.ArgumentParser()
    parser.add_argument("--docx", required=True)
    args = parser.parse_args()

    text = docx_text(Path(args.docx))
    forbidden = ["校园外卖", "苍穹外卖", "sky_take_out", "配送", "送达", "送到"]
    required = ["校园点餐系统", "学生", "商家", "平台", "运营数据", "统一管理", "订单状态"]

    errors: list[str] = []
    for word in forbidden:
        if word in text:
            errors.append(f"forbidden term found: {word}")
    for word in required:
        if word not in text:
            errors.append(f"required term missing: {word}")

    if errors:
        print("\n".join(errors), file=sys.stderr)
        raise SystemExit(1)
    print("text verification passed")


if __name__ == "__main__":
    main()
```

- [ ] **Step 2: Run terminology verification**

Run:

```powershell
& "C:\Users\g'y'c\.cache\codex-runtimes\codex-primary-runtime\dependencies\python\python.exe" "D:\sky-delivery\core\paper_work\core_chapters_upgrade\scripts\verify_docx_text.py" --docx "D:\sky-delivery\core\paper_work\core_chapters_upgrade\outputs\毕业论文1_完整版_正文增强版.docx"
```

Expected: `text verification passed`

- [ ] **Step 3: Manually inspect extracted chapter continuity**

Run:

```powershell
& "C:\Users\g'y'c\.cache\codex-runtimes\codex-primary-runtime\dependencies\python\python.exe" "D:\sky-delivery\core\paper_work\core_chapters_upgrade\scripts\extract_docx_outline.py" --docx "D:\sky-delivery\core\paper_work\core_chapters_upgrade\outputs\毕业论文1_完整版_正文增强版.docx" --outdir "D:\sky-delivery\core\paper_work\core_chapters_upgrade\notes\final_extract"
```

Expected: extraction completes. Inspect `final_extract\docx_outline.md` and confirm Chapter 2-6 headings remain in order.

---

## Task 9: Render PNG Pages And Inspect Layout

**Files:**
- Read: `D:\sky-delivery\core\paper_work\core_chapters_upgrade\outputs\毕业论文1_完整版_正文增强版.docx`
- Create: `D:\sky-delivery\core\paper_work\core_chapters_upgrade\renders\final\page-*.png`

- [ ] **Step 1: Render DOCX to PNG using artifact-tool**

Run:

```powershell
& "C:\Users\g'y'c\.cache\codex-runtimes\codex-primary-runtime\dependencies\python\python.exe" "C:\Users\g'y'c\.codex\plugins\cache\openai-primary-runtime\documents\26.423.10653\skills\documents\render_docx.py" "D:\sky-delivery\core\paper_work\core_chapters_upgrade\outputs\毕业论文1_完整版_正文增强版.docx" --output_dir "D:\sky-delivery\core\paper_work\core_chapters_upgrade\renders\final" --renderer artifact-tool
```

Expected: PNG page images are created in `renders\final`. No PDF file is generated.

- [ ] **Step 2: Inspect important pages visually**

Open the rendered PNGs for:
- the first page containing Chapter 2,
- every page containing figures or screenshots,
- every page containing Chapter 5 diagrams,
- the first page containing Chapter 6,
- the final conclusion page.

Expected:
- no clipped text,
- no overlapping images or paragraphs,
- no one-page blank gaps caused by replacement,
- existing clean screenshots remain clean and unmarked,
- figure/table captions remain near their visual objects.

- [ ] **Step 3: Fix and re-render when layout issues appear**

If a page has visual defects, adjust the affected replacement paragraphs in `notes\replacements.json`, re-run Task 7 Step 3, re-run Task 8 Step 2, and re-run Task 9 Step 1. Repeat until the rendered PNG pages are clean.

---

## Task 10: Final Review And Delivery

**Files:**
- Read: `D:\sky-delivery\core\paper_work\core_chapters_upgrade\outputs\毕业论文1_完整版_正文增强版.docx`
- Read: `D:\sky-delivery\core\paper_work\core_chapters_upgrade\renders\final\page-*.png`

- [ ] **Step 1: Confirm the final output exists**

Run:

```powershell
Get-Item -LiteralPath "D:\sky-delivery\core\paper_work\core_chapters_upgrade\outputs\毕业论文1_完整版_正文增强版.docx" | Select-Object FullName,Length,LastWriteTime
```

Expected: file length is non-zero and timestamp reflects the current run.

- [ ] **Step 2: Check repository changes are limited**

Run:

```powershell
git status --short docs/superpowers/plans/2026-05-15-campus-ordering-core-chapters-enhancement.md paper_work/core_chapters_upgrade
```

Expected: plan and work artifacts are visible; unrelated existing changes are not staged by the implementation.

- [ ] **Step 3: Provide the final DOCX path**

Final user-facing result should point to:

```text
D:\sky-delivery\core\paper_work\core_chapters_upgrade\outputs\毕业论文1_完整版_正文增强版.docx
```

Mention verification:
- terminology verification passed,
- PNG render inspection passed,
- no PDF export was performed.

---

## Self-Review Checklist

**Spec coverage:**
- Student ordering is covered in Tasks 3, 4, 6, and 7.
- Merchant order processing and operating data are covered in Tasks 3, 4, 6, and 7.
- Platform centralized merchant management is covered in Tasks 3, 4, 5, 6, and 7.
- Chapter 2-6 enhancement is covered in Tasks 4, 5, 6, and 7.
- Abstract/introduction/conclusion light synchronization is covered in Task 6.
- Source-code evidence without direct source pasting is covered in Task 3.
- DOCX format preservation and PNG-only visual QA are covered in Tasks 7, 8, and 9.
- Major body rewriting permission is covered in Body Rewrite Rules and Task 7.

**Placeholder scan:**
- This plan avoids undefined implementation placeholders.
- Replacement indices are derived from the extracted paragraph inventory because the exact DOCX paragraph numbers must come from the live input file.

**Terminology guard:**
- Final thesis text must use “校园点餐系统”.
- Final thesis text must not contain旧项目名、`sky_take_out`、`苍穹外卖`.
- Final thesis text must avoid making the system sound like a delivery platform.
