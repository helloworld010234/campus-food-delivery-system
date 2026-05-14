# Campus Delivery Figure Upgrade Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Build a complete, thesis-ready figure and screenshot upgrade for the campus delivery paper, using mini-program screenshots extracted from the user-provided original DOCX, real Web admin screenshots, redrawn backend/database diagrams, and a final DOCX with image QA.

**Architecture:** Use a project-local artifact pipeline under `D:/sky-delivery/core/paper-output/figure-upgrade` so the new round does not contaminate older outputs. The pipeline is split into extraction, figure registry, diagram rendering, screenshot processing, optional Web recapture, DOCX integration, and static/render QA, with every generated image traceable to either source code, database evidence, an original DOCX image, or a real browser screenshot.

**Tech Stack:** Python 3, `python-docx`, Pillow, DOCX zip media extraction, Playwright or Chrome DevTools for optional Web recapture, SVG/PNG generation, PowerShell commands, Git.

---

## Scope And Source Rules

The implementation must follow the approved design spec at `D:/sky-delivery/core/docs/superpowers/specs/2026-05-14-campus-delivery-figure-upgrade-design.md`.

Use these fixed source paths:

- User original thesis DOCX for mini-program screenshots: `C:/Users/g'y'c/Desktop/校园外卖设计与实现-学校规范格式终稿_已替换.docx`
- Current breakthrough manuscript source: `D:/sky-delivery/core/paper-output/breakthrough/source/校园外卖设计与实现-突破重构正文.md`
- Current breakthrough DOCX as latest content baseline: `D:/sky-delivery/core/paper-output/breakthrough/校园外卖设计与实现-突破重构版.docx`
- Existing Web screenshots: `D:/sky-delivery/core/paper-output/screenshots/web-admin`
- Existing processed screenshot evidence: `D:/sky-delivery/core/paper-output/screenshots/processed`
- Optional Web startup script: `C:/Users/g'y'c/Desktop/start-campus-delivery.cmd`

Do not use project mini-program design mockups as mini-program thesis screenshots. The only acceptable mini-program screenshot source is the user original DOCX.

Do not introduce the project training name, old demo database name, or old demo branding in figure text, captions, generated filenames, reports, or final thesis text.

## File Structure

Create this new implementation area:

```text
D:/sky-delivery/core/paper-output/figure-upgrade/
  tools/
    __init__.py
    paths.py
    extract_original_docx_images.py
    collect_figure_evidence.py
    render_academic_diagrams.py
    process_screenshots.py
    capture_web_admin.py
    build_figure_upgrade_docx.py
    qa_figure_upgrade_docx.py
  source/
    校园外卖设计与实现-图表增强正文.md
  figures/
    sources/
    svg/
    png/
    docx-png/
  screenshots/
    extracted/
    contact-sheets/
    web-original/
    processed/
  reports/
    figure-registry.json
    image-source-map.json
    extraction-report.json
    evidence-report.json
    screenshot-report.json
    docx-build-qa.json
    docx-static-qa.json
  rendered-pages/
  校园外卖设计与实现-图表增强版.docx
  delivery-report.md
```

Each tool must be deterministic: running the same command twice should overwrite generated files in `figure-upgrade` and produce the same registry/report structure.

### Task 1: Create Pipeline Scaffold And Shared Paths

**Files:**
- Create: `D:/sky-delivery/core/paper-output/figure-upgrade/tools/__init__.py`
- Create: `D:/sky-delivery/core/paper-output/figure-upgrade/tools/paths.py`
- Create directories listed in the File Structure section.

- [ ] **Step 1: Create directories**

Run:

```powershell
New-Item -ItemType Directory -Force `
  "D:/sky-delivery/core/paper-output/figure-upgrade/tools", `
  "D:/sky-delivery/core/paper-output/figure-upgrade/source", `
  "D:/sky-delivery/core/paper-output/figure-upgrade/figures/sources", `
  "D:/sky-delivery/core/paper-output/figure-upgrade/figures/svg", `
  "D:/sky-delivery/core/paper-output/figure-upgrade/figures/png", `
  "D:/sky-delivery/core/paper-output/figure-upgrade/figures/docx-png", `
  "D:/sky-delivery/core/paper-output/figure-upgrade/screenshots/extracted", `
  "D:/sky-delivery/core/paper-output/figure-upgrade/screenshots/contact-sheets", `
  "D:/sky-delivery/core/paper-output/figure-upgrade/screenshots/web-original", `
  "D:/sky-delivery/core/paper-output/figure-upgrade/screenshots/processed", `
  "D:/sky-delivery/core/paper-output/figure-upgrade/reports", `
  "D:/sky-delivery/core/paper-output/figure-upgrade/rendered-pages"
```

Expected: all directories exist under `D:/sky-delivery/core/paper-output/figure-upgrade`.

- [ ] **Step 2: Create package marker**

Write `D:/sky-delivery/core/paper-output/figure-upgrade/tools/__init__.py`:

```python
"""Figure upgrade tooling for the campus delivery thesis."""
```

- [ ] **Step 3: Create shared paths**

Write `D:/sky-delivery/core/paper-output/figure-upgrade/tools/paths.py`:

```python
from __future__ import annotations

from pathlib import Path

ROOT = Path("D:/sky-delivery/core")
WORK_DIR = ROOT / "paper-output" / "figure-upgrade"
TOOLS_DIR = WORK_DIR / "tools"
SOURCE_DIR = WORK_DIR / "source"
FIGURE_SOURCE_DIR = WORK_DIR / "figures" / "sources"
FIGURE_SVG_DIR = WORK_DIR / "figures" / "svg"
FIGURE_PNG_DIR = WORK_DIR / "figures" / "png"
DOCX_PNG_DIR = WORK_DIR / "figures" / "docx-png"
SCREENSHOT_EXTRACTED_DIR = WORK_DIR / "screenshots" / "extracted"
CONTACT_SHEET_DIR = WORK_DIR / "screenshots" / "contact-sheets"
WEB_ORIGINAL_DIR = WORK_DIR / "screenshots" / "web-original"
SCREENSHOT_PROCESSED_DIR = WORK_DIR / "screenshots" / "processed"
REPORT_DIR = WORK_DIR / "reports"
RENDERED_PAGES_DIR = WORK_DIR / "rendered-pages"

USER_ORIGINAL_DOCX = Path(r"C:/Users/g'y'c/Desktop/校园外卖设计与实现-学校规范格式终稿_已替换.docx")
BREAKTHROUGH_SOURCE_MD = ROOT / "paper-output" / "breakthrough" / "source" / "校园外卖设计与实现-突破重构正文.md"
BREAKTHROUGH_DOCX = ROOT / "paper-output" / "breakthrough" / "校园外卖设计与实现-突破重构版.docx"
OUTPUT_SOURCE_MD = SOURCE_DIR / "校园外卖设计与实现-图表增强正文.md"
OUTPUT_DOCX = WORK_DIR / "校园外卖设计与实现-图表增强版.docx"

EXISTING_WEB_SCREENSHOT_DIR = ROOT / "paper-output" / "screenshots" / "web-admin"
EXISTING_PROCESSED_SCREENSHOT_DIR = ROOT / "paper-output" / "screenshots" / "processed"
START_SCRIPT = Path(r"C:/Users/g'y'c/Desktop/start-campus-delivery.cmd")

FIGURE_REGISTRY = REPORT_DIR / "figure-registry.json"
IMAGE_SOURCE_MAP = REPORT_DIR / "image-source-map.json"
EXTRACTION_REPORT = REPORT_DIR / "extraction-report.json"
EVIDENCE_REPORT = REPORT_DIR / "evidence-report.json"
SCREENSHOT_REPORT = REPORT_DIR / "screenshot-report.json"
DOCX_BUILD_QA = REPORT_DIR / "docx-build-qa.json"
DOCX_STATIC_QA = REPORT_DIR / "docx-static-qa.json"

FORBIDDEN_TERMS = [
    "".join(["sky", "_take", "_out"]),
    "\u82cd\u7a79\u5916\u5356",
]


def ensure_dirs() -> None:
    for path in [
        WORK_DIR,
        TOOLS_DIR,
        SOURCE_DIR,
        FIGURE_SOURCE_DIR,
        FIGURE_SVG_DIR,
        FIGURE_PNG_DIR,
        DOCX_PNG_DIR,
        SCREENSHOT_EXTRACTED_DIR,
        CONTACT_SHEET_DIR,
        WEB_ORIGINAL_DIR,
        SCREENSHOT_PROCESSED_DIR,
        REPORT_DIR,
        RENDERED_PAGES_DIR,
    ]:
        path.mkdir(parents=True, exist_ok=True)
```

- [ ] **Step 4: Verify scaffold imports**

Run:

```powershell
python -c "import sys; sys.path.insert(0, r'D:/sky-delivery/core/paper-output/figure-upgrade/tools'); import paths; paths.ensure_dirs(); print(paths.WORK_DIR)"
```

Expected output includes:

```text
D:\sky-delivery\core\paper-output\figure-upgrade
```

- [ ] **Step 5: Commit scaffold**

Run:

```powershell
git add -- paper-output/figure-upgrade/tools/__init__.py paper-output/figure-upgrade/tools/paths.py
git commit -m "chore: scaffold figure upgrade pipeline"
```

### Task 2: Extract Original DOCX Images And Build Source Map

**Files:**
- Create: `D:/sky-delivery/core/paper-output/figure-upgrade/tools/extract_original_docx_images.py`
- Output: `D:/sky-delivery/core/paper-output/figure-upgrade/screenshots/extracted/*`
- Output: `D:/sky-delivery/core/paper-output/figure-upgrade/screenshots/contact-sheets/original-docx-media.png`
- Output: `D:/sky-delivery/core/paper-output/figure-upgrade/reports/extraction-report.json`
- Output: `D:/sky-delivery/core/paper-output/figure-upgrade/reports/image-source-map.json`

- [ ] **Step 1: Write extraction tool**

Create `extract_original_docx_images.py` with this structure:

```python
from __future__ import annotations

import json
import shutil
import sys
import zipfile
from dataclasses import asdict, dataclass
from io import BytesIO
from pathlib import Path

from PIL import Image, ImageDraw, ImageFont

sys.path.insert(0, str(Path(__file__).resolve().parent))
from paths import (  # noqa: E402
    CONTACT_SHEET_DIR,
    EXTRACTION_REPORT,
    IMAGE_SOURCE_MAP,
    SCREENSHOT_EXTRACTED_DIR,
    USER_ORIGINAL_DOCX,
    ensure_dirs,
)


@dataclass
class ExtractedImage:
    docx_member: str
    filename: str
    bytes: int
    width: int
    height: int
    role: str
    reason: str


def classify(width: int, height: int, name: str) -> tuple[str, str]:
    ratio = height / max(width, 1)
    if ratio >= 1.35 and width >= 600:
        return "miniapp_candidate", "vertical phone-like screenshot extracted from original DOCX"
    if width >= 1000 and height >= 550:
        return "wide_system_candidate", "wide screenshot or composite extracted from original DOCX"
    return "supporting_media", "non-primary media extracted for traceability"


def extract_images() -> list[ExtractedImage]:
    ensure_dirs()
    if not USER_ORIGINAL_DOCX.exists():
        raise FileNotFoundError(USER_ORIGINAL_DOCX)
    for old in SCREENSHOT_EXTRACTED_DIR.glob("*"):
        if old.is_file():
            old.unlink()

    extracted: list[ExtractedImage] = []
    with zipfile.ZipFile(USER_ORIGINAL_DOCX) as archive:
        members = sorted(m for m in archive.namelist() if m.startswith("word/media/"))
        for member in members:
            raw = archive.read(member)
            with Image.open(BytesIO(raw)) as image:
                width, height = image.size
                suffix = Path(member).suffix.lower() or ".png"
                filename = f"original-{Path(member).stem}{suffix}"
                target = SCREENSHOT_EXTRACTED_DIR / filename
                target.write_bytes(raw)
                role, reason = classify(width, height, filename)
                extracted.append(
                    ExtractedImage(
                        docx_member=member,
                        filename=filename,
                        bytes=len(raw),
                        width=width,
                        height=height,
                        role=role,
                        reason=reason,
                    )
                )
    return extracted


def make_contact_sheet(images: list[ExtractedImage]) -> Path:
    thumbs: list[tuple[ExtractedImage, Image.Image]] = []
    thumb_w = 180
    thumb_h = 220
    for item in images:
        with Image.open(SCREENSHOT_EXTRACTED_DIR / item.filename) as source:
            im = source.convert("RGB")
            im.thumbnail((thumb_w, thumb_h))
            canvas = Image.new("RGB", (thumb_w, thumb_h), "white")
            x = (thumb_w - im.width) // 2
            y = (thumb_h - im.height) // 2
            canvas.paste(im, (x, y))
            thumbs.append((item, canvas))

    cols = 4
    label_h = 56
    rows = (len(thumbs) + cols - 1) // cols
    sheet = Image.new("RGB", (cols * thumb_w, rows * (thumb_h + label_h)), "white")
    draw = ImageDraw.Draw(sheet)
    font = ImageFont.load_default()
    for idx, (item, thumb) in enumerate(thumbs):
        col = idx % cols
        row = idx // cols
        x = col * thumb_w
        y = row * (thumb_h + label_h)
        sheet.paste(thumb, (x, y))
        label = f"{item.filename}\n{item.width}x{item.height}\n{item.role}"
        draw.multiline_text((x + 6, y + thumb_h + 4), label, fill="black", font=font, spacing=2)

    out = CONTACT_SHEET_DIR / "original-docx-media.png"
    sheet.save(out, dpi=(300, 300))
    return out


def main() -> None:
    images = extract_images()
    contact_sheet = make_contact_sheet(images)
    report = {
        "source_docx": str(USER_ORIGINAL_DOCX),
        "total_images": len(images),
        "miniapp_candidates": [asdict(i) for i in images if i.role == "miniapp_candidate"],
        "wide_system_candidates": [asdict(i) for i in images if i.role == "wide_system_candidate"],
        "all_images": [asdict(i) for i in images],
        "contact_sheet": str(contact_sheet),
    }
    EXTRACTION_REPORT.write_text(json.dumps(report, ensure_ascii=False, indent=2), encoding="utf-8")
    IMAGE_SOURCE_MAP.write_text(json.dumps(report, ensure_ascii=False, indent=2), encoding="utf-8")
    print(json.dumps({"total_images": len(images), "contact_sheet": str(contact_sheet)}, ensure_ascii=False, indent=2))
    if len(images) < 10:
        raise SystemExit("Original DOCX image extraction produced too few images")
    if len(report["miniapp_candidates"]) < 4:
        raise SystemExit("Original DOCX did not expose enough mini-program-like screenshots")


if __name__ == "__main__":
    main()
```

- [ ] **Step 2: Run extraction**

Run:

```powershell
python D:/sky-delivery/core/paper-output/figure-upgrade/tools/extract_original_docx_images.py
```

Expected:

```text
"total_images": 18
```

The exact count should remain 18 for the current provided DOCX. If the count changes because the user replaces the DOCX, continue only if there are at least 10 images and at least 4 mini-program candidates.

- [ ] **Step 3: Verify mini-program source rule**

Run:

```powershell
python -c "import json; p=r'D:/sky-delivery/core/paper-output/figure-upgrade/reports/image-source-map.json'; data=json.load(open(p,encoding='utf-8')); print(len(data['miniapp_candidates'])); print(data['source_docx'])"
```

Expected output:

```text
8
C:\Users\g'y'c\Desktop\校园外卖设计与实现-学校规范格式终稿_已替换.docx
```

The first number may be greater than or equal to 4 if the source DOCX changes. It must not be zero.

- [ ] **Step 4: Commit extraction tool and reports**

Run:

```powershell
git add -- paper-output/figure-upgrade/tools/extract_original_docx_images.py paper-output/figure-upgrade/screenshots/extracted paper-output/figure-upgrade/screenshots/contact-sheets paper-output/figure-upgrade/reports/extraction-report.json paper-output/figure-upgrade/reports/image-source-map.json
git commit -m "feat: extract original thesis screenshots"
```

### Task 3: Collect Source Evidence And Create Figure Registry

**Files:**
- Create: `D:/sky-delivery/core/paper-output/figure-upgrade/tools/collect_figure_evidence.py`
- Output: `D:/sky-delivery/core/paper-output/figure-upgrade/reports/evidence-report.json`
- Output: `D:/sky-delivery/core/paper-output/figure-upgrade/reports/figure-registry.json`

- [ ] **Step 1: Write evidence collector**

Create `collect_figure_evidence.py`:

```python
from __future__ import annotations

import json
import sys
from pathlib import Path

sys.path.insert(0, str(Path(__file__).resolve().parent))
from paths import EVIDENCE_REPORT, FIGURE_REGISTRY, ROOT, ensure_dirs  # noqa: E402


BACKEND_PATHS = [
    "backend/sky-server/src/main/java/com/sky/controller/user",
    "backend/sky-server/src/main/java/com/sky/controller/admin",
    "backend/sky-server/src/main/java/com/sky/service/impl",
    "backend/sky-server/src/main/java/com/sky/mapper",
    "backend/sky-pojo/src/main/java/com/sky/entity",
    "backend/sky-pojo/src/main/java/com/sky/dto",
    "backend/sky-pojo/src/main/java/com/sky/vo",
]

REGISTRY = [
    {"key": "fig3-1-system-architecture", "caption": "图 3-1 系统总体架构图", "chapter": "第3章", "kind": "diagram", "width_cm": 13.0, "source": "source_code"},
    {"key": "fig3-2-function-modules", "caption": "图 3-2 系统功能模块结构图", "chapter": "第3章", "kind": "diagram", "width_cm": 13.0, "source": "source_code"},
    {"key": "fig3-3-business-loop", "caption": "图 3-3 核心业务闭环图", "chapter": "第3章", "kind": "diagram", "width_cm": 12.6, "source": "source_code"},
    {"key": "fig4-1-database-er-main", "caption": "图 4-1 数据库总体ER图", "chapter": "第4章", "kind": "er", "width_cm": 13.2, "source": "database_schema"},
    {"key": "fig4-2-order-domain-er", "caption": "图 4-2 订单领域实体关系图", "chapter": "第4章", "kind": "er", "width_cm": 12.6, "source": "database_schema"},
    {"key": "fig4-3-order-state", "caption": "图 4-3 订单状态流转图", "chapter": "第4章", "kind": "state", "width_cm": 12.4, "source": "source_code"},
    {"key": "fig5-1-login-flow", "caption": "图 5-1 用户登录认证流程图", "chapter": "第5章", "kind": "flow", "width_cm": 10.8, "source": "source_code"},
    {"key": "fig5-2-browse-spec-flow", "caption": "图 5-2 商品浏览与规格选择流程图", "chapter": "第5章", "kind": "flow", "width_cm": 10.8, "source": "source_code"},
    {"key": "fig5-3-cart-flow", "caption": "图 5-3 添加购物车流程图", "chapter": "第5章", "kind": "flow", "width_cm": 10.8, "source": "source_code"},
    {"key": "fig5-4-order-submit-flow", "caption": "图 5-4 订单提交流程图", "chapter": "第5章", "kind": "flow", "width_cm": 10.8, "source": "source_code"},
    {"key": "fig5-5-payment-callback-flow", "caption": "图 5-5 支付与回调处理流程图", "chapter": "第5章", "kind": "flow", "width_cm": 10.8, "source": "source_code"},
    {"key": "fig5-6-merchant-accept-flow", "caption": "图 5-6 商家接单与出餐流程图", "chapter": "第5章", "kind": "flow", "width_cm": 10.8, "source": "source_code"},
    {"key": "fig5-7-product-maintenance-flow", "caption": "图 5-7 管理端商品维护流程图", "chapter": "第5章", "kind": "flow", "width_cm": 10.8, "source": "source_code"},
    {"key": "fig5-8-web-dashboard", "caption": "图 5-8 Web管理端首页效果图", "chapter": "第5章", "kind": "screenshot", "width_cm": 13.2, "source": "real_web"},
    {"key": "fig5-9-web-orders", "caption": "图 5-9 Web订单管理效果图", "chapter": "第5章", "kind": "screenshot", "width_cm": 13.2, "source": "real_web"},
    {"key": "fig5-10-web-products", "caption": "图 5-10 Web商品管理效果图", "chapter": "第5章", "kind": "screenshot", "width_cm": 13.2, "source": "real_web"},
    {"key": "fig6-1-miniapp-home", "caption": "图 6-1 小程序端首页效果图", "chapter": "第6章", "kind": "screenshot", "width_cm": 12.2, "source": "original_docx"},
    {"key": "fig6-2-miniapp-order", "caption": "图 6-2 小程序端点餐与下单效果图", "chapter": "第6章", "kind": "screenshot", "width_cm": 12.2, "source": "original_docx"},
    {"key": "fig6-3-web-statistics", "caption": "图 6-3 管理端统计分析效果图", "chapter": "第6章", "kind": "screenshot", "width_cm": 13.2, "source": "real_web"},
    {"key": "appendix-a-full-er", "caption": "附录图 A-1 完整数据库ER图", "chapter": "附录A", "kind": "er", "width_cm": 15.0, "source": "database_schema"},
    {"key": "appendix-a-table-fields", "caption": "附录图 A-2 核心数据表字段结构图", "chapter": "附录A", "kind": "diagram", "width_cm": 15.0, "source": "database_schema"},
    {"key": "appendix-b-layer-calls", "caption": "附录图 B-1 后端分层调用关系图", "chapter": "附录B", "kind": "diagram", "width_cm": 15.0, "source": "source_code"},
    {"key": "appendix-b-api-map", "caption": "附录图 B-2 接口模块映射图", "chapter": "附录B", "kind": "diagram", "width_cm": 15.0, "source": "source_code"},
    {"key": "appendix-c-web-gallery", "caption": "附录图 C-1 Web管理端截图组", "chapter": "附录C", "kind": "screenshot", "width_cm": 15.0, "source": "real_web"},
    {"key": "appendix-c-miniapp-gallery", "caption": "附录图 C-2 小程序端截图组", "chapter": "附录C", "kind": "screenshot", "width_cm": 15.0, "source": "original_docx"},
]


def count_java_files() -> dict[str, int]:
    result: dict[str, int] = {}
    for rel in BACKEND_PATHS:
        path = ROOT / rel
        result[rel] = len(list(path.rglob("*.java"))) if path.exists() else 0
    return result


def main() -> None:
    ensure_dirs()
    evidence = {
        "backend_file_counts": count_java_files(),
        "registry_count": len(REGISTRY),
        "main_text_figures": [item for item in REGISTRY if not item["chapter"].startswith("附录")],
        "appendix_figures": [item for item in REGISTRY if item["chapter"].startswith("附录")],
    }
    FIGURE_REGISTRY.write_text(json.dumps(REGISTRY, ensure_ascii=False, indent=2), encoding="utf-8")
    EVIDENCE_REPORT.write_text(json.dumps(evidence, ensure_ascii=False, indent=2), encoding="utf-8")
    print(json.dumps({"registry_count": len(REGISTRY), "backend_paths": len(evidence["backend_file_counts"])}, ensure_ascii=False, indent=2))
    if len(REGISTRY) < 24:
        raise SystemExit("Figure registry is too small for the complete enhancement route")


if __name__ == "__main__":
    main()
```

- [ ] **Step 2: Run evidence collector**

Run:

```powershell
python D:/sky-delivery/core/paper-output/figure-upgrade/tools/collect_figure_evidence.py
```

Expected:

```text
"registry_count": 25
```

- [ ] **Step 3: Verify registry categories**

Run:

```powershell
python -c "import json; p=r'D:/sky-delivery/core/paper-output/figure-upgrade/reports/figure-registry.json'; data=json.load(open(p,encoding='utf-8')); print(sum(1 for x in data if x['kind']=='flow')); print(sum(1 for x in data if x['source']=='original_docx'))"
```

Expected:

```text
7
2
```

The second number counts main-text mini-program figures only. Appendix mini-program gallery is also marked `original_docx`.

- [ ] **Step 4: Commit registry and evidence**

Run:

```powershell
git add -- paper-output/figure-upgrade/tools/collect_figure_evidence.py paper-output/figure-upgrade/reports/evidence-report.json paper-output/figure-upgrade/reports/figure-registry.json
git commit -m "feat: register thesis figure upgrade assets"
```

### Task 4: Render Professional Backend, Database, And Flow Diagrams

**Files:**
- Create: `D:/sky-delivery/core/paper-output/figure-upgrade/tools/render_academic_diagrams.py`
- Output: `D:/sky-delivery/core/paper-output/figure-upgrade/figures/sources/*.json`
- Output: `D:/sky-delivery/core/paper-output/figure-upgrade/figures/svg/*.svg`
- Output: `D:/sky-delivery/core/paper-output/figure-upgrade/figures/png/*.png`
- Output: `D:/sky-delivery/core/paper-output/figure-upgrade/figures/docx-png/*.png`

- [ ] **Step 1: Write renderer data model and style constants**

Create the top of `render_academic_diagrams.py`:

```python
from __future__ import annotations

import html
import json
import math
import shutil
import sys
import textwrap
from dataclasses import dataclass
from pathlib import Path

from PIL import Image, ImageDraw, ImageFont

sys.path.insert(0, str(Path(__file__).resolve().parent))
from paths import (  # noqa: E402
    DOCX_PNG_DIR,
    FIGURE_PNG_DIR,
    FIGURE_REGISTRY,
    FIGURE_SOURCE_DIR,
    FIGURE_SVG_DIR,
    FORBIDDEN_TERMS,
    ensure_dirs,
)

BLUE = "#1F4E79"
BLACK = "#222222"
GRAY = "#666666"
LIGHT_GRAY = "#F5F7FA"
MID_GRAY = "#D9DEE7"
WHITE = "#FFFFFF"
FONT_FAMILY = "Microsoft YaHei, SimSun, Arial"
FONT_CANDIDATES = [
    Path("C:/Windows/Fonts/msyh.ttc"),
    Path("C:/Windows/Fonts/simsun.ttc"),
    Path("C:/Windows/Fonts/simhei.ttf"),
]


@dataclass
class Node:
    key: str
    label: str
    kind: str = "rect"
    group: str = ""


@dataclass
class Edge:
    source: str
    target: str
    label: str = ""


@dataclass
class Diagram:
    key: str
    title: str
    layout: str
    nodes: list[Node]
    edges: list[Edge]
```

- [ ] **Step 2: Add shared SVG and PNG rendering helpers**

Add helper functions:

```python
def font_path() -> str | None:
    for item in FONT_CANDIDATES:
        if item.exists():
            return str(item)
    return None


def load_font(size: int) -> ImageFont.FreeTypeFont | ImageFont.ImageFont:
    path = font_path()
    if path:
        return ImageFont.truetype(path, size=size)
    return ImageFont.load_default()


def wrap_label(text: str, width: int = 12) -> list[str]:
    result: list[str] = []
    for part in text.split("\n"):
        result.extend(textwrap.wrap(part, width=width, break_long_words=False) or [part])
    return result


def assert_clean_text(text: str) -> None:
    hits = [term for term in FORBIDDEN_TERMS if term in text]
    if hits:
        raise ValueError(f"Forbidden figure text found: {hits}")


def svg_text(x: float, y: float, text: str, size: int = 18, color: str = BLACK, weight: str = "400") -> str:
    assert_clean_text(text)
    lines = wrap_label(text, 13)
    start = y - (len(lines) - 1) * size * 0.6
    out = []
    for idx, line in enumerate(lines):
        out.append(
            f'<text x="{x:.1f}" y="{start + idx * size * 1.25:.1f}" text-anchor="middle" '
            f'font-family="{FONT_FAMILY}" font-size="{size}" font-weight="{weight}" fill="{color}">{html.escape(line)}</text>'
        )
    return "\n".join(out)


def draw_png_card(key: str, title: str, blocks: list[str], width: int, height: int) -> Image.Image:
    image = Image.new("RGB", (width, height), "white")
    draw = ImageDraw.Draw(image)
    title_font = load_font(34)
    body_font = load_font(24)
    draw.rectangle((8, 8, width - 8, height - 8), outline=(31, 78, 121), width=3)
    draw.text((32, 26), title, font=title_font, fill=(34, 34, 34))
    y = 92
    for block in blocks:
        draw.rounded_rectangle((42, y, width - 42, y + 72), radius=8, outline=(31, 78, 121), width=2, fill=(247, 249, 252))
        draw.text((64, y + 20), block, font=body_font, fill=(34, 34, 34))
        y += 92
        if y > height - 110:
            break
    return image


def save_svg_png(key: str, title: str, svg: str, width: int, height: int, png_blocks: list[str]) -> None:
    assert_clean_text(svg)
    FIGURE_SOURCE_DIR.joinpath(f"{key}.json").write_text(json.dumps({"key": key, "title": title, "blocks": png_blocks}, ensure_ascii=False, indent=2), encoding="utf-8")
    svg_path = FIGURE_SVG_DIR / f"{key}.svg"
    svg_path.write_text(svg, encoding="utf-8")
    image = draw_png_card(key, title, png_blocks, width, height)
    png_path = FIGURE_PNG_DIR / f"{key}.png"
    image.save(png_path, dpi=(300, 300))
    shutil.copyfile(png_path, DOCX_PNG_DIR / f"{key}.png")
```

- [ ] **Step 3: Define complete flow specs**

Add flow diagram definitions. Each flow must have normal and abnormal endings:

```python
FLOW_SPECS = {
    "fig5-1-login-flow": [
        ("开始：提交登录请求", "start"),
        ("校验账号与登录凭证", "rect"),
        ("凭证有效？", "diamond"),
        ("返回登录失败提示", "end_bad"),
        ("查询用户或管理员信息", "rect"),
        ("账号存在且状态正常？", "diamond"),
        ("返回账号异常提示", "end_bad"),
        ("生成访问令牌并记录登录态", "rect"),
        ("令牌生成成功？", "diamond"),
        ("返回系统异常提示", "end_bad"),
        ("返回登录成功结果", "end_good"),
    ],
    "fig5-2-browse-spec-flow": [
        ("开始：进入店铺页面", "start"),
        ("查询店铺营业状态", "rect"),
        ("店铺营业？", "diamond"),
        ("提示店铺休息并停止选购", "end_bad"),
        ("加载分类与商品列表", "rect"),
        ("商品已上架？", "diamond"),
        ("隐藏或提示商品不可售", "end_bad"),
        ("用户选择商品规格", "rect"),
        ("规格选择完整？", "diamond"),
        ("提示补全规格", "end_bad"),
        ("展示价格并允许加入购物车", "end_good"),
    ],
    "fig5-3-cart-flow": [
        ("开始：点击加入购物车", "start"),
        ("校验登录态与商品参数", "rect"),
        ("商品可售？", "diamond"),
        ("返回不可加入提示", "end_bad"),
        ("查询购物车已有记录", "rect"),
        ("已有相同商品规格？", "diamond"),
        ("数量加一并更新金额", "rect"),
        ("新增购物车记录", "rect"),
        ("数据库保存成功？", "diamond"),
        ("返回保存失败提示", "end_bad"),
        ("返回最新购物车数量", "end_good"),
    ],
    "fig5-4-order-submit-flow": [
        ("开始：提交订单", "start"),
        ("校验登录态、地址和购物车", "rect"),
        ("地址有效且购物车非空？", "diamond"),
        ("返回地址或购物车异常", "end_bad"),
        ("查询店铺与商品状态", "rect"),
        ("均可下单？", "diamond"),
        ("返回商品不可售提示", "end_bad"),
        ("计算金额并校验前端金额", "rect"),
        ("金额一致？", "diamond"),
        ("拒绝提交并记录异常", "end_bad"),
        ("创建订单和订单明细", "rect"),
        ("事务提交成功？", "diamond"),
        ("回滚并返回失败", "end_bad"),
        ("清空购物车并返回订单号", "end_good"),
    ],
    "fig5-5-payment-callback-flow": [
        ("开始：接收支付回调", "start"),
        ("校验签名和通知参数", "rect"),
        ("签名有效？", "diamond"),
        ("返回失败并记录日志", "end_bad"),
        ("查询本地订单", "rect"),
        ("订单存在？", "diamond"),
        ("返回订单不存在", "end_bad"),
        ("校验金额与商户订单号", "rect"),
        ("金额一致？", "diamond"),
        ("标记支付异常", "end_bad"),
        ("判断是否已处理", "rect"),
        ("首次成功回调？", "diamond"),
        ("直接返回成功应答", "end_good"),
        ("更新订单为已支付并通知商家", "rect"),
        ("更新成功？", "diamond"),
        ("返回重试应答", "end_bad"),
        ("返回成功应答", "end_good"),
    ],
    "fig5-6-merchant-accept-flow": [
        ("开始：商家处理新订单", "start"),
        ("查询订单当前状态", "rect"),
        ("订单可处理？", "diamond"),
        ("提示状态已变化", "end_bad"),
        ("商家选择接单或拒单", "rect"),
        ("接单？", "diamond"),
        ("填写拒单原因并取消订单", "rect"),
        ("更新为已接单并进入备餐", "rect"),
        ("状态更新成功？", "diamond"),
        ("返回处理失败提示", "end_bad"),
        ("通知用户订单处理结果", "rect"),
        ("通知成功？", "diamond"),
        ("记录通知失败并保留订单状态", "end_bad"),
        ("流程结束", "end_good"),
    ],
    "fig5-7-product-maintenance-flow": [
        ("开始：进入商品维护页面", "start"),
        ("选择新增、修改或上下架", "rect"),
        ("表单字段完整？", "diamond"),
        ("提示补全商品信息", "end_bad"),
        ("校验分类与商家权限", "rect"),
        ("分类有效且有权限？", "diamond"),
        ("返回权限或分类异常", "end_bad"),
        ("上传或复用商品图片", "rect"),
        ("图片处理成功？", "diamond"),
        ("提示图片上传失败", "end_bad"),
        ("保存商品与规格数据", "rect"),
        ("保存成功？", "diamond"),
        ("返回保存失败提示", "end_bad"),
        ("刷新列表并返回成功", "end_good"),
    ],
}
```

- [ ] **Step 4: Render flowcharts with visible branches**

Implement a vertical flow renderer that draws decision diamonds and side abnormal branches. Each `diamond` node must have a downward positive branch and a right-side negative branch that returns to the next relevant ending or step. Labels must include “是” and “否”.

Run:

```powershell
python D:/sky-delivery/core/paper-output/figure-upgrade/tools/render_academic_diagrams.py --flows
```

Expected:

```text
rendered_flows=7
```

Open at least two outputs from `D:/sky-delivery/core/paper-output/figure-upgrade/figures/png` and verify by sight that they contain readable nodes, branch labels, and side abnormal paths.

- [ ] **Step 5: Render architecture, module, business loop, ER, and appendix diagrams**

Add non-flow diagram specs for these keys and render them:

```text
fig3-1-system-architecture
fig3-2-function-modules
fig3-3-business-loop
fig4-1-database-er-main
fig4-2-order-domain-er
fig4-3-order-state
appendix-a-full-er
appendix-a-table-fields
appendix-b-layer-calls
appendix-b-api-map
```

Minimum content:

- `fig3-1-system-architecture`: student mini-program, Web management end, backend service, authentication interceptor, business services, persistence layer, MySQL, Redis, object storage, payment callback, WebSocket notification.
- `fig3-2-function-modules`: student ordering module, merchant order module, platform management module, common services.
- `fig3-3-business-loop`: browse, cart, order, payment, merchant acceptance, delivery, completion, statistics feedback.
- `fig4-1-database-er-main`: user, employee, merchant, category, dish, setmeal, shopping_cart, address_book, orders, order_detail, campus.
- `fig4-2-order-domain-er`: user, address_book, shopping_cart, orders, order_detail, merchant, dish, setmeal.
- `fig4-3-order-state`: pending payment, paid, accepted, delivery, completed, cancelled, refund/abnormal.
- Appendix diagrams contain more fields and relationship labels than the main-text diagrams.

Run:

```powershell
python D:/sky-delivery/core/paper-output/figure-upgrade/tools/render_academic_diagrams.py --diagrams
```

Expected:

```text
rendered_diagrams=10
```

- [ ] **Step 6: Verify diagram count and forbidden text**

Run:

```powershell
python -c "import sys; from pathlib import Path; sys.path.insert(0, r'D:/sky-delivery/core/paper-output/figure-upgrade/tools'); from paths import FORBIDDEN_TERMS; root=Path(r'D:/sky-delivery/core/paper-output/figure-upgrade/figures/png'); files=list(root.glob('*.png')); text='\\n'.join(p.read_text(encoding='utf-8', errors='ignore') for p in Path(r'D:/sky-delivery/core/paper-output/figure-upgrade/figures/svg').glob('*.svg')); print(len(files)); print([term for term in FORBIDDEN_TERMS if term in text])"
```

Expected:

```text
17
[]
```

- [ ] **Step 7: Commit rendered diagram tool and generated diagrams**

Run:

```powershell
git add -- paper-output/figure-upgrade/tools/render_academic_diagrams.py paper-output/figure-upgrade/figures
git commit -m "feat: render enhanced thesis diagrams"
```

### Task 5: Process Original Mini-Program Screenshots And Existing Web Screenshots

**Files:**
- Create: `D:/sky-delivery/core/paper-output/figure-upgrade/tools/process_screenshots.py`
- Output: `D:/sky-delivery/core/paper-output/figure-upgrade/screenshots/processed/*.png`
- Output: `D:/sky-delivery/core/paper-output/figure-upgrade/reports/screenshot-report.json`

- [ ] **Step 1: Write screenshot processor**

Create `process_screenshots.py`:

```python
from __future__ import annotations

import json
import shutil
import sys
from pathlib import Path

from PIL import Image, ImageDraw, ImageFont, ImageOps

sys.path.insert(0, str(Path(__file__).resolve().parent))
from paths import (  # noqa: E402
    DOCX_PNG_DIR,
    EXISTING_WEB_SCREENSHOT_DIR,
    IMAGE_SOURCE_MAP,
    SCREENSHOT_EXTRACTED_DIR,
    SCREENSHOT_PROCESSED_DIR,
    SCREENSHOT_REPORT,
    WEB_ORIGINAL_DIR,
    ensure_dirs,
)

BLUE = (31, 78, 121)
GRAY = (220, 224, 230)
BLACK = (34, 34, 34)
WHITE = (255, 255, 255)


def font(size: int) -> ImageFont.FreeTypeFont | ImageFont.ImageFont:
    for path in [Path("C:/Windows/Fonts/msyh.ttc"), Path("C:/Windows/Fonts/simsun.ttc")]:
        if path.exists():
            return ImageFont.truetype(str(path), size=size)
    return ImageFont.load_default()


def fit_image(path: Path, box: tuple[int, int], border: bool = True) -> Image.Image:
    with Image.open(path) as source:
        im = ImageOps.exif_transpose(source).convert("RGB")
    im.thumbnail(box, Image.LANCZOS)
    canvas = Image.new("RGB", box, WHITE)
    x = (box[0] - im.width) // 2
    y = (box[1] - im.height) // 2
    canvas.paste(im, (x, y))
    if border:
        draw = ImageDraw.Draw(canvas)
        draw.rectangle((0, 0, box[0] - 1, box[1] - 1), outline=GRAY, width=2)
    return canvas


def label_panel(im: Image.Image, label: str) -> Image.Image:
    label_h = 52
    out = Image.new("RGB", (im.width, im.height + label_h), WHITE)
    out.paste(im, (0, 0))
    draw = ImageDraw.Draw(out)
    draw.rectangle((0, im.height, im.width, im.height + label_h), fill=(247, 249, 252))
    draw.text((18, im.height + 12), label, font=font(24), fill=BLUE)
    return out


def compose_horizontal(paths: list[Path], labels: list[str], out_name: str, panel_size: tuple[int, int]) -> dict:
    panels = [label_panel(fit_image(path, panel_size), label) for path, label in zip(paths, labels)]
    gap = 28
    width = sum(p.width for p in panels) + gap * (len(panels) - 1)
    height = max(p.height for p in panels)
    out = Image.new("RGB", (width, height), WHITE)
    x = 0
    for panel in panels:
        out.paste(panel, (x, 0))
        x += panel.width + gap
    target = SCREENSHOT_PROCESSED_DIR / out_name
    out.save(target, dpi=(300, 300))
    shutil.copyfile(target, DOCX_PNG_DIR / out_name)
    return {"output": str(target), "inputs": [str(p) for p in paths], "width": out.width, "height": out.height}


def select_miniapp_images() -> list[Path]:
    data = json.loads(IMAGE_SOURCE_MAP.read_text(encoding="utf-8"))
    candidates = data["miniapp_candidates"]
    ordered = sorted(candidates, key=lambda item: (item["width"] * item["height"]), reverse=True)
    selected = [SCREENSHOT_EXTRACTED_DIR / item["filename"] for item in ordered[:6]]
    if len(selected) < 4:
        raise SystemExit("Not enough original DOCX mini-program screenshots for composition")
    return selected


def copy_web_inputs() -> dict[str, Path]:
    ensure_dirs()
    mapping = {
        "dashboard": "01-dashboard.png",
        "products": "02-products.png",
        "orders": "03-orders.png",
        "statistics": "04-statistics.png",
    }
    result: dict[str, Path] = {}
    for key, filename in mapping.items():
        src = EXISTING_WEB_SCREENSHOT_DIR / filename
        if not src.exists():
            continue
        dst = WEB_ORIGINAL_DIR / filename
        shutil.copyfile(src, dst)
        result[key] = dst
    return result


def annotate_web(path: Path, out_name: str, title: str) -> dict:
    with Image.open(path) as source:
        im = ImageOps.exif_transpose(source).convert("RGB")
    max_w = 1800
    if im.width > max_w:
        new_h = int(im.height * max_w / im.width)
        im = im.resize((max_w, new_h), Image.LANCZOS)
    top = 64
    out = Image.new("RGB", (im.width, im.height + top), WHITE)
    draw = ImageDraw.Draw(out)
    draw.rectangle((0, 0, out.width, top), fill=(247, 249, 252))
    draw.text((24, 17), title, font=font(28), fill=BLUE)
    out.paste(im, (0, top))
    draw.rectangle((0, top, out.width - 1, out.height - 1), outline=GRAY, width=2)
    target = SCREENSHOT_PROCESSED_DIR / out_name
    out.save(target, dpi=(300, 300))
    shutil.copyfile(target, DOCX_PNG_DIR / out_name)
    return {"output": str(target), "input": str(path), "width": out.width, "height": out.height}


def main() -> None:
    ensure_dirs()
    for old in SCREENSHOT_PROCESSED_DIR.glob("*.png"):
        old.unlink()
    mini = select_miniapp_images()
    report = {"miniapp_source": "original_docx", "miniapp": [], "web": []}
    report["miniapp"].append(compose_horizontal(mini[:3], ["a 首页", "b 商品浏览", "c 商品详情"], "fig6-1-miniapp-home.png", (360, 660)))
    report["miniapp"].append(compose_horizontal(mini[3:6], ["a 购物车", "b 确认订单", "c 支付结果"], "fig6-2-miniapp-order.png", (360, 660)))

    web = copy_web_inputs()
    web_titles = {
        "dashboard": "Web管理端首页",
        "orders": "Web订单管理",
        "products": "Web商品管理",
        "statistics": "管理端统计分析",
    }
    web_outputs = {
        "dashboard": "fig5-8-web-dashboard.png",
        "orders": "fig5-9-web-orders.png",
        "products": "fig5-10-web-products.png",
        "statistics": "fig6-3-web-statistics.png",
    }
    for key, path in web.items():
        report["web"].append(annotate_web(path, web_outputs[key], web_titles[key]))
    if len(web) < 4:
        report["needs_web_recapture"] = True
    else:
        report["needs_web_recapture"] = False

    SCREENSHOT_REPORT.write_text(json.dumps(report, ensure_ascii=False, indent=2), encoding="utf-8")
    print(json.dumps({"miniapp_groups": len(report["miniapp"]), "web_images": len(report["web"]), "needs_web_recapture": report["needs_web_recapture"]}, ensure_ascii=False, indent=2))


if __name__ == "__main__":
    main()
```

- [ ] **Step 2: Run screenshot processing**

Run:

```powershell
python D:/sky-delivery/core/paper-output/figure-upgrade/tools/process_screenshots.py
```

Expected:

```text
"miniapp_groups": 2
"web_images": 4
```

If `needs_web_recapture` is `true`, run Task 6 before DOCX integration.

- [ ] **Step 3: Manually inspect processed screenshots**

Open these files:

```text
D:/sky-delivery/core/paper-output/figure-upgrade/screenshots/processed/fig6-1-miniapp-home.png
D:/sky-delivery/core/paper-output/figure-upgrade/screenshots/processed/fig6-2-miniapp-order.png
D:/sky-delivery/core/paper-output/figure-upgrade/screenshots/processed/fig5-8-web-dashboard.png
D:/sky-delivery/core/paper-output/figure-upgrade/screenshots/processed/fig5-9-web-orders.png
```

Acceptance:

- Mini-program screenshots are vertical phone screenshots extracted from the original DOCX.
- Web screenshots show real admin pages.
- Captions and panel labels do not cover important UI content.
- Images are not stretched.

- [ ] **Step 4: Commit screenshot processing**

Run:

```powershell
git add -- paper-output/figure-upgrade/tools/process_screenshots.py paper-output/figure-upgrade/screenshots paper-output/figure-upgrade/reports/screenshot-report.json paper-output/figure-upgrade/figures/docx-png
git commit -m "feat: process thesis screenshots for figure upgrade"
```

### Task 6: Recapture Missing Web Admin Screenshots Only If Needed

**Files:**
- Create: `D:/sky-delivery/core/paper-output/figure-upgrade/tools/capture_web_admin.py`
- Output: `D:/sky-delivery/core/paper-output/figure-upgrade/screenshots/web-original/*.png`

- [ ] **Step 1: Check whether recapture is needed**

Run:

```powershell
python -c "import json; p=r'D:/sky-delivery/core/paper-output/figure-upgrade/reports/screenshot-report.json'; data=json.load(open(p,encoding='utf-8')); print(data.get('needs_web_recapture'))"
```

Expected for the current workspace:

```text
False
```

If the output is `False`, skip to Task 7. If the output is `True`, continue this task.

- [ ] **Step 2: Start the system**

Run:

```powershell
Start-Process -FilePath "C:/Users/g'y'c/Desktop/start-campus-delivery.cmd" -WindowStyle Hidden
```

Expected: backend serves `http://localhost:8080` and admin frontend serves `http://localhost:8081`.

- [ ] **Step 3: Write capture tool**

Create `capture_web_admin.py` using Playwright:

```python
from __future__ import annotations

import sys
from pathlib import Path

sys.path.insert(0, str(Path(__file__).resolve().parent))
from paths import WEB_ORIGINAL_DIR, ensure_dirs  # noqa: E402


PAGES = [
    ("01-dashboard.png", "http://localhost:8081/#/dashboard"),
    ("02-products.png", "http://localhost:8081/#/dish"),
    ("03-orders.png", "http://localhost:8081/#/order"),
    ("04-statistics.png", "http://localhost:8081/#/statistics"),
]


def main() -> None:
    from playwright.sync_api import sync_playwright

    ensure_dirs()
    with sync_playwright() as pw:
        browser = pw.chromium.launch(headless=True)
        page = browser.new_page(viewport={"width": 1440, "height": 960}, device_scale_factor=1)
        page.goto("http://localhost:8081", wait_until="networkidle")
        page.fill("input[type='text']", "admin")
        page.fill("input[type='password']", "123456")
        page.click("button")
        page.wait_for_timeout(1500)
        for filename, url in PAGES:
            page.goto(url, wait_until="networkidle")
            page.wait_for_timeout(1000)
            page.screenshot(path=str(WEB_ORIGINAL_DIR / filename), full_page=False)
        browser.close()


if __name__ == "__main__":
    main()
```

- [ ] **Step 4: Run capture**

Run:

```powershell
python D:/sky-delivery/core/paper-output/figure-upgrade/tools/capture_web_admin.py
```

Expected: four PNG files exist in `D:/sky-delivery/core/paper-output/figure-upgrade/screenshots/web-original`.

- [ ] **Step 5: Re-run screenshot processing**

Run:

```powershell
python D:/sky-delivery/core/paper-output/figure-upgrade/tools/process_screenshots.py
```

Expected:

```text
"web_images": 4
"needs_web_recapture": false
```

- [ ] **Step 6: Commit recapture tool and screenshots**

Run:

```powershell
git add -- paper-output/figure-upgrade/tools/capture_web_admin.py paper-output/figure-upgrade/screenshots/web-original paper-output/figure-upgrade/screenshots/processed paper-output/figure-upgrade/reports/screenshot-report.json
git commit -m "feat: capture missing web admin screenshots"
```

### Task 7: Build Enhanced Manuscript Source And Final DOCX

**Files:**
- Create: `D:/sky-delivery/core/paper-output/figure-upgrade/tools/build_figure_upgrade_docx.py`
- Create: `D:/sky-delivery/core/paper-output/figure-upgrade/source/校园外卖设计与实现-图表增强正文.md`
- Output: `D:/sky-delivery/core/paper-output/figure-upgrade/校园外卖设计与实现-图表增强版.docx`
- Output: `D:/sky-delivery/core/paper-output/figure-upgrade/reports/docx-build-qa.json`

- [ ] **Step 1: Copy breakthrough manuscript into figure-upgrade source**

Run:

```powershell
Copy-Item -LiteralPath "D:/sky-delivery/core/paper-output/breakthrough/source/校园外卖设计与实现-突破重构正文.md" -Destination "D:/sky-delivery/core/paper-output/figure-upgrade/source/校园外卖设计与实现-图表增强正文.md" -Force
```

Expected: the new source markdown exists under `paper-output/figure-upgrade/source`.

- [ ] **Step 2: Replace old figure markers with enhanced markers**

Modify the new source markdown only. Use these markers for main-text images:

```text
[[FIG:fig3-1-system-architecture|图 3-1 系统总体架构图|13.0]]
[[FIG:fig3-2-function-modules|图 3-2 系统功能模块结构图|13.0]]
[[FIG:fig3-3-business-loop|图 3-3 核心业务闭环图|12.6]]
[[FIG:fig4-1-database-er-main|图 4-1 数据库总体ER图|13.2]]
[[FIG:fig4-2-order-domain-er|图 4-2 订单领域实体关系图|12.6]]
[[FIG:fig4-3-order-state|图 4-3 订单状态流转图|12.4]]
[[FIG:fig5-1-login-flow|图 5-1 用户登录认证流程图|10.8]]
[[FIG:fig5-2-browse-spec-flow|图 5-2 商品浏览与规格选择流程图|10.8]]
[[FIG:fig5-3-cart-flow|图 5-3 添加购物车流程图|10.8]]
[[FIG:fig5-4-order-submit-flow|图 5-4 订单提交流程图|10.8]]
[[FIG:fig5-5-payment-callback-flow|图 5-5 支付与回调处理流程图|10.8]]
[[FIG:fig5-6-merchant-accept-flow|图 5-6 商家接单与出餐流程图|10.8]]
[[FIG:fig5-7-product-maintenance-flow|图 5-7 管理端商品维护流程图|10.8]]
[[FIG:fig5-8-web-dashboard|图 5-8 Web管理端首页效果图|13.2]]
[[FIG:fig5-9-web-orders|图 5-9 Web订单管理效果图|13.2]]
[[FIG:fig5-10-web-products|图 5-10 Web商品管理效果图|13.2]]
[[FIG:fig6-1-miniapp-home|图 6-1 小程序端首页效果图|12.2]]
[[FIG:fig6-2-miniapp-order|图 6-2 小程序端点餐与下单效果图|12.2]]
[[FIG:fig6-3-web-statistics|图 6-3 管理端统计分析效果图|13.2]]
```

Append this appendix section before final acknowledgements or after references, matching the current thesis structure:

```text
# 附录A 数据库设计补充图
[[FIG:appendix-a-full-er|附录图 A-1 完整数据库ER图|15.0]]
[[FIG:appendix-a-table-fields|附录图 A-2 核心数据表字段结构图|15.0]]

# 附录B 后端接口与调用关系补充图
[[FIG:appendix-b-layer-calls|附录图 B-1 后端分层调用关系图|15.0]]
[[FIG:appendix-b-api-map|附录图 B-2 接口模块映射图|15.0]]

# 附录C 系统界面截图补充图
[[FIG:appendix-c-web-gallery|附录图 C-1 Web管理端截图组|15.0]]
[[FIG:appendix-c-miniapp-gallery|附录图 C-2 小程序端截图组|15.0]]
```

- [ ] **Step 3: Write DOCX builder**

Create `build_figure_upgrade_docx.py` by copying the previous `build_breakthrough_docx.py` structure, then change constants:

```python
from __future__ import annotations

import json
import re
import shutil
import zipfile
from dataclasses import dataclass
from pathlib import Path
from tempfile import TemporaryDirectory

from docx import Document
from docx.enum.text import WD_ALIGN_PARAGRAPH, WD_LINE_SPACING
from docx.oxml import OxmlElement
from docx.oxml.ns import qn
from docx.shared import Cm, Pt, RGBColor
from PIL import Image

ROOT = Path("D:/sky-delivery/core")
WORK_DIR = ROOT / "paper-output" / "figure-upgrade"
SOURCE_MD = WORK_DIR / "source" / "校园外卖设计与实现-图表增强正文.md"
INPUT_DOCX = Path(r"C:/Users/g'y'c/Desktop/校园外卖设计与实现-学校规范格式终稿_已替换.docx")
OUT_DOCX = WORK_DIR / "校园外卖设计与实现-图表增强版.docx"
IMAGE_MAP = WORK_DIR / "reports" / "docx-image-map.json"
FIGURE_DIR = WORK_DIR / "figures" / "docx-png"
QA_JSON = WORK_DIR / "reports" / "docx-build-qa.json"
```

Keep the proven formatting helpers from `build_breakthrough_docx.py`: run fonts, paragraph spacing, figure insertion, table insertion, TOC field handling, heading styles, and image width calculation. Change only the source/output paths and figure lookup directory.

- [ ] **Step 4: Add figure lookup fallback**

Inside the figure insertion logic, resolve image paths in this order:

```python
def resolve_figure_path(fig_key: str) -> Path:
    png = FIGURE_DIR / f"{fig_key}.png"
    if png.exists():
        return png
    direct = WORK_DIR / "screenshots" / "processed" / f"{fig_key}.png"
    if direct.exists():
        return direct
    raise FileNotFoundError(f"Missing figure asset for {fig_key}")
```

- [ ] **Step 5: Build final DOCX**

Run:

```powershell
python D:/sky-delivery/core/paper-output/figure-upgrade/tools/build_figure_upgrade_docx.py
```

Expected:

```text
D:\sky-delivery\core\paper-output\figure-upgrade\校园外卖设计与实现-图表增强版.docx
```

The build QA report must show at least 25 mapped figures.

- [ ] **Step 6: Commit DOCX builder and generated DOCX**

Run:

```powershell
git add -- paper-output/figure-upgrade/tools/build_figure_upgrade_docx.py paper-output/figure-upgrade/source paper-output/figure-upgrade/校园外卖设计与实现-图表增强版.docx paper-output/figure-upgrade/reports/docx-build-qa.json paper-output/figure-upgrade/reports/docx-image-map.json
git commit -m "feat: build figure-enhanced thesis docx"
```

### Task 8: Add Static QA And Render Verification

**Files:**
- Create: `D:/sky-delivery/core/paper-output/figure-upgrade/tools/qa_figure_upgrade_docx.py`
- Output: `D:/sky-delivery/core/paper-output/figure-upgrade/reports/docx-static-qa.json`
- Output: `D:/sky-delivery/core/paper-output/figure-upgrade/delivery-report.md`

- [ ] **Step 1: Write static QA tool**

Create `qa_figure_upgrade_docx.py`:

```python
from __future__ import annotations

import json
import re
import sys
import zipfile
from pathlib import Path

from docx import Document

sys.path.insert(0, str(Path(__file__).resolve().parent))
from paths import (  # noqa: E402
    DOCX_STATIC_QA,
    FIGURE_REGISTRY,
    FORBIDDEN_TERMS,
    IMAGE_SOURCE_MAP,
    OUTPUT_DOCX,
    OUTPUT_SOURCE_MD,
    SCREENSHOT_REPORT,
    WORK_DIR,
)


def main() -> None:
    doc = Document(OUTPUT_DOCX)
    paragraphs = [p.text.strip() for p in doc.paragraphs if p.text.strip()]
    text = "\n".join(paragraphs)
    source = OUTPUT_SOURCE_MD.read_text(encoding="utf-8")
    registry = json.loads(FIGURE_REGISTRY.read_text(encoding="utf-8"))
    image_source = json.loads(IMAGE_SOURCE_MAP.read_text(encoding="utf-8"))
    screenshot_report = json.loads(SCREENSHOT_REPORT.read_text(encoding="utf-8"))
    with zipfile.ZipFile(OUTPUT_DOCX) as archive:
        xml = archive.read("word/document.xml").decode("utf-8", errors="ignore")

    forbidden_hits = [term for term in FORBIDDEN_TERMS if term in text or term in source or term in xml]
    captions = [item["caption"] for item in registry]
    missing_captions = [caption for caption in captions if caption not in text]
    figure_markers = re.findall(r"\[\[FIG:([^|]+)\|", source)
    docx_image_count = len(doc.inline_shapes)
    original_docx_source_ok = "original_docx" in json.dumps(registry, ensure_ascii=False) and len(image_source.get("miniapp_candidates", [])) >= 4
    web_source_ok = len(screenshot_report.get("web", [])) >= 4
    consecutive_captions = []
    caption_set = set(captions)
    for idx, paragraph in enumerate(paragraphs[:-1]):
        if paragraph in caption_set and paragraphs[idx + 1] in caption_set:
            consecutive_captions.append([paragraph, paragraphs[idx + 1]])

    report = {
        "docx": str(OUTPUT_DOCX),
        "paragraphs": len(doc.paragraphs),
        "inline_shapes": docx_image_count,
        "registry_figures": len(registry),
        "source_figure_markers": len(figure_markers),
        "missing_captions": missing_captions,
        "forbidden_hits": forbidden_hits,
        "original_docx_source_ok": original_docx_source_ok,
        "web_source_ok": web_source_ok,
        "consecutive_captions": consecutive_captions,
        "checks_passed": not forbidden_hits and not missing_captions and original_docx_source_ok and web_source_ok and docx_image_count >= 25,
    }
    DOCX_STATIC_QA.write_text(json.dumps(report, ensure_ascii=False, indent=2), encoding="utf-8")
    delivery = WORK_DIR / "delivery-report.md"
    delivery.write_text(
        "\n".join(
            [
                "# 图表增强版交付报告",
                "",
                f"- 最终文档：`{OUTPUT_DOCX}`",
                f"- 图片数量：{docx_image_count}",
                f"- 图表登记数量：{len(registry)}",
                f"- 小程序截图来源：用户提供的原论文 DOCX",
                f"- Web 截图数量：{len(screenshot_report.get('web', []))}",
                f"- 静态检查：{'通过' if report['checks_passed'] else '未通过'}",
            ]
        ),
        encoding="utf-8",
    )
    print(json.dumps(report, ensure_ascii=False, indent=2))
    if not report["checks_passed"]:
        raise SystemExit(1)


if __name__ == "__main__":
    main()
```

- [ ] **Step 2: Run static QA**

Run:

```powershell
python D:/sky-delivery/core/paper-output/figure-upgrade/tools/qa_figure_upgrade_docx.py
```

Expected:

```text
"checks_passed": true
```

- [ ] **Step 3: Render DOCX pages if the local document renderer is available**

Use the existing Documents plugin workflow or previous project render tooling. Render to:

```text
D:/sky-delivery/core/paper-output/figure-upgrade/rendered-pages
```

Acceptance:

- Sample rendered pages show images inside margins.
- Flowcharts are readable after Word scaling.
- No page consists only of one small floating image unless it is an appendix large figure.
- Captions stay near their images.

- [ ] **Step 4: Commit QA and delivery report**

Run:

```powershell
git add -- paper-output/figure-upgrade/tools/qa_figure_upgrade_docx.py paper-output/figure-upgrade/reports/docx-static-qa.json paper-output/figure-upgrade/delivery-report.md paper-output/figure-upgrade/rendered-pages
git commit -m "test: verify figure-enhanced thesis docx"
```

### Task 9: Final Review And Packaging

**Files:**
- Modify only if QA requires it: generated files under `D:/sky-delivery/core/paper-output/figure-upgrade`

- [ ] **Step 1: Review git diff scope**

Run:

```powershell
git status --short
```

Acceptance:

- New work is confined to `paper-output/figure-upgrade` plus any committed plan/spec files.
- Existing unrelated build artifacts remain unstaged.

- [ ] **Step 2: Review final artifact list**

Run:

```powershell
Get-ChildItem -LiteralPath "D:/sky-delivery/core/paper-output/figure-upgrade" -Force | Select-Object Name,Length,LastWriteTime
```

Expected visible files include:

```text
校园外卖设计与实现-图表增强版.docx
delivery-report.md
figures
screenshots
reports
```

- [ ] **Step 3: Open the final DOCX manually**

Open:

```text
D:/sky-delivery/core/paper-output/figure-upgrade/校园外卖设计与实现-图表增强版.docx
```

Acceptance:

- Main text has richer diagrams and screenshots.
- Mini-program screenshots visibly come from the original thesis media, not project design mockups.
- Flowcharts include complete branches and end states.
- Appendix contains full-size supporting figures.
- Formatting follows the original school format.

- [ ] **Step 4: Final commit if manual review caused adjustments**

If manual review required additional image sizing or caption edits, commit only those files:

```powershell
git add -- paper-output/figure-upgrade
git commit -m "fix: polish figure-enhanced thesis layout"
```

## Self-Review Checklist

Spec coverage:

- Original DOCX mini-program screenshot source: Task 2 and Task 5.
- Existing Web screenshots first, recapture only if needed: Task 5 and Task 6.
- Backend and database diagrams redrawn: Task 4.
- Complete branch logic in every flowchart: Task 4 flow specs and side-branch rendering acceptance.
- Main-text plus appendix completeness: Task 3 registry and Task 7 source integration.
- Image quality and layout checks: Task 5, Task 8, Task 9.
- Forbidden term checks: Task 4 and Task 8.

Placeholder scan:

- The plan contains no open implementation markers.
- Every conditional branch has an explicit action.
- Every command includes an expected result.

Execution choice after this plan:

- Use subagent-driven execution when the user wants faster parallel work with review checkpoints.
- Use inline execution when the user wants a single continuous implementation session in the current thread.
