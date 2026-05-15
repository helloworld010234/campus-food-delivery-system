# Campus Ordering Source-Evidence Thesis Rewrite Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Rewrite the thesis body so Chapters 3-6 explain how the campus ordering system solves real problems using concrete mechanisms from the source code.

**Architecture:** Keep the existing DOCX structure, figures, screenshots, captions, and school formatting. Add a small deterministic Python patcher that locates paragraphs by heading/caption text, replaces only prose paragraphs, and leaves embedded images/tables intact. Validate by scanning risky phrases and rendering PNG pages with the artifact-tool renderer.

**Tech Stack:** Python `python-docx`, Word OOXML, existing bundled Codex document renderer, Spring Boot source evidence from `backend/`, MySQL schema evidence from `backend/scripts/phase1_multi_merchant_schema.sql`.

---

### Task 1: Capture Source-Evidence Rewrite Targets

**Files:**
- Create: `D:\sky-delivery\core\paper_work\core_chapters_upgrade\notes\source_evidence_rewrite_targets.md`
- Read: `D:\sky-delivery\core\backend\sky-server\src\main\java\com\sky\interceptor\JwtTokenAdminInterceptor.java`
- Read: `D:\sky-delivery\core\backend\sky-server\src\main\java\com\sky\interceptor\JwtTokenUserInterceptor.java`
- Read: `D:\sky-delivery\core\backend\sky-common\src\main\java\com\sky\context\BaseContext.java`
- Read: `D:\sky-delivery\core\backend\sky-server\src\main\java\com\sky\security\MerchantScopeGuard.java`
- Read: `D:\sky-delivery\core\backend\sky-server\src\main\java\com\sky\service\impl\ShoppingCartServiceImpl.java`
- Read: `D:\sky-delivery\core\backend\sky-server\src\main\java\com\sky\service\impl\OrderServiceImpl.java`
- Read: `D:\sky-delivery\core\backend\sky-server\src\main\java\com\sky\websocket\WebSocketServer.java`
- Read: `D:\sky-delivery\core\backend\sky-server\src\main\java\com\sky\service\impl\ReportServiceImpl.java`
- Read: `D:\sky-delivery\core\backend\scripts\phase1_multi_merchant_schema.sql`

- [ ] **Step 1: Write the evidence target note**

Create `D:\sky-delivery\core\paper_work\core_chapters_upgrade\notes\source_evidence_rewrite_targets.md` with this exact content:

```markdown
# 源码证据驱动正文重写目标

## 身份识别
- 管理端拦截器解析 JWT 后写入员工编号、商家编号和账号类型。
- 学生端拦截器解析 JWT 后写入学生编号。
- BaseContext 使用 ThreadLocal 保存当前请求上下文，并在请求结束后清理。
- 正文表达重点：服务端通过身份上下文区分学生、商家和平台，避免每个业务方法重复传递身份参数。

## 商家范围隔离
- MerchantScopeGuard 统一处理平台账号、商家账号、商家员工和学生私有接口的商家范围。
- 平台账号可以跨商家查询和维护；商家账号只能访问绑定商家数据。
- 正文表达重点：商家范围不是前端隐藏按钮，而是服务端统一校验。

## 购物车设计
- ShoppingCartServiceImpl 使用 userId + merchantId 作为购物车范围。
- 添加商品时校验菜品或套餐是否属于当前商家。
- 相同商品与规格组合累加数量；清空购物车只清理当前商家。
- 正文表达重点：购物车是订单前的业务缓冲区，用来防止跨商家混合结算。

## 订单提交
- OrderServiceImpl 使用 Redis setIfAbsent 建立 10 秒短时提交锁。
- 订单提交校验地址、商家启用状态、营业状态、购物车内容和商家归属。
- @Transactional 保证订单主表、订单明细和购物车清理在同一事务内完成。
- 正文表达重点：订单提交不是简单插入记录，而是一组一致性操作。

## 支付和状态流转
- 支付成功后由服务端将订单更新为待商家处理，并记录支付状态和时间。
- 接单、拒单、出餐、完成等动作均校验当前订单状态。
- 正文表达重点：订单状态是系统中的业务顺序表，防止订单跳步。

## WebSocket 提醒
- WebSocketServer 按员工编号维护连接。
- 新订单和催单事件按商家员工定向发送，同时通知平台账号。
- 正文表达重点：消息提醒提高实时性，但订单真实状态仍以服务端查询结果为准。

## 运营数据
- ReportServiceImpl 按日期聚合营业额、订单量、有效订单、用户数和热销商品。
- 报表查询复用商家范围校验，平台看全局，商家看本店。
- 正文表达重点：运营数据来自订单和明细聚合，不是静态展示。

## 数据库结构
- campus 和 merchant 表保存校区与商家基础信息。
- category、dish、setmeal、shopping_cart、orders 补充 merchant_id。
- orders 补充 campus_id、merchant_name、goods_amount、delivery_fee、item_count。
- 订单索引围绕 user_id/order_time、merchant_id/status/order_time、merchant_id/pay_status/order_time 设计。
- 正文表达重点：数据库结构直接服务多商家隔离、订单查询和统计分析。
```

- [ ] **Step 2: Confirm the note exists**

Run:

```powershell
Get-Item -LiteralPath "D:\sky-delivery\core\paper_work\core_chapters_upgrade\notes\source_evidence_rewrite_targets.md"
```

Expected: one file entry with non-zero `Length`.

### Task 2: Create the Rewrite Patch Script

**Files:**
- Create: `D:\sky-delivery\core\paper_work\core_chapters_upgrade\scripts\apply_source_evidence_rewrite.py`
- Modify in-place through script: `D:\sky-delivery\core\paper_work\core_chapters_upgrade\outputs\毕业论文1_完整版_正文增强版.docx`
- Backup through script: `D:\sky-delivery\core\paper_work\core_chapters_upgrade\outputs\毕业论文1_完整版_正文增强版.before_source_evidence_rewrite.docx`

- [ ] **Step 1: Create the patch script**

Create `D:\sky-delivery\core\paper_work\core_chapters_upgrade\scripts\apply_source_evidence_rewrite.py` with this code:

```python
from __future__ import annotations

import argparse
import shutil
from pathlib import Path

from docx import Document


REPLACEMENTS = {
    "需求分析不能只罗列菜单名称，而要说明每类角色在什么场景下需要系统提供什么能力。本文将需求划分为学生端、商家端和平台端三类，并从功能需求与非功能需求两个层面展开。功能需求回答系统要做什么，非功能需求回答系统要怎样做才可靠。这种分析方式更适合校园点餐系统，因为系统中既有学生的高频移动端操作，也有商家的后台经营管理，还有平台的基础数据维护。":
    "校园点餐系统的需求不只来自页面菜单，更来自多角色协同中的业务约束。学生端关注能否快速完成商家选择、商品加入购物车和订单提交；商家端关注订单能否按状态处理，并能否看到本店经营数据；平台端关注商家资料、营业状态和账号边界是否可统一维护。为了让这些需求在系统中可执行，服务端需要在身份识别、商家范围、购物车归属和订单状态等位置设置明确规则，而不是只依靠前端页面展示。",

    "在分析过程中，本文坚持三个原则。第一，需求必须能够回到真实场景，例如学生是否能快速完成点餐、商家是否能及时处理订单。第二，需求必须体现角色边界，例如商家不能管理其他商家的订单。第三，需求必须能够在系统中验证，例如订单状态是否按规则变化，平台修改商家状态后学生端展示是否受到影响。":
    "需求分析围绕三个可验证问题展开。第一，学生在多个商家之间切换时，购物车和订单是否仍能保持明确商家归属。第二，商家处理订单时，是否只能看到和操作本商家范围内的数据。第三，平台调整商家启用状态或营业状态后，学生端下单和商家端经营是否能同步受到约束。后续设计均围绕这些问题展开，使需求能够落到服务端校验、数据库字段和运行效果中。",

    "学生端的目标是完成一条连续的点餐路径。学生登录后进入小程序，先查看当前校区可用商家，再进入目标商家的商品列表。商品列表需要按分类展示，支持菜品、套餐和规格选择。学生选好商品后加入购物车，可以调整数量或清空购物车。结算时，系统需要展示地址、商品明细、金额和备注等信息，学生确认后提交订单。":
    "学生端的核心目标是把一次点餐过程稳定地转化为一笔订单。学生登录后先获得自己的身份信息，随后查看当前校区可用商家，并进入目标商家的商品列表。学生选择菜品、套餐和规格后加入购物车，服务端会按照当前学生和当前商家保存购物车记录。这样即使学生在多个商家之间切换，结算时也能保证本次订单只来自一个商家，避免不同商家的商品被混合提交。",

    "商家端的目标是完成本商家范围内的经营管理。商家需要维护分类、菜品和套餐等基础信息，并根据营业情况进行上下架或修改。学生提交订单后，商家需要在后台查看新订单，并根据订单状态进行接单、拒单、处理和完成等操作。系统应当限制不符合状态条件的操作，避免商家误处理订单。":
    "商家端的核心目标是处理本商家范围内的商品、订单和经营数据。商家维护分类、菜品和套餐时，服务端会保留商家归属，避免商品落入其他商家的数据范围。学生支付成功后，订单进入待商家处理状态，商家才能进行接单或拒单；接单后才能进入出餐处理；完成出餐后才能结束订单。这样的状态限制可以减少误操作，也使商家端看到的订单进度与服务端数据保持一致。",

    "平台管理端的目标是统一管理商家和系统基础数据。平台人员需要维护商家名称、编码、联系方式、地址、展示顺序、启用状态和营业状态。启用状态决定商家是否作为系统内有效对象，营业状态影响学生端是否能够正常下单。平台端还需要查看商家概览和基础统计，以便判断系统整体运行情况。":
    "平台管理端负责维护系统运行边界。平台人员可以维护商家名称、编码、联系方式、地址、展示顺序、启用状态和营业状态。启用状态决定商家是否作为系统内有效对象，营业状态决定学生端是否允许向该商家提交订单。平台端与商家端共用管理入口，但服务端会根据账号类型区分平台账号和商家账号：平台账号可以跨商家维护基础资料，商家账号只能处理绑定商家的业务数据。",

    "系统总体设计的目标，是支撑学生点餐、商家经营和平台统一管理商家三类任务。学生端需要轻量、快速、适合手机操作；商家端需要集中处理商品、订单和运营数据；平台端需要维护商家资料和运行状态。后端服务负责把这些前端操作转化为可靠的业务规则，数据库负责保存长期业务数据。":
    "系统总体设计围绕身份、范围、状态和数据四条主线展开。身份用于判断当前请求来自学生、商家还是平台；范围用于限制商家只能访问本店数据；状态用于控制订单从待支付到待处理、处理中和已完成的业务顺序；数据用于保存商品、购物车、订单和统计结果。前端负责提供操作入口，后端负责执行这些规则，数据库负责保存规则执行后的结果。",

    "系统采用前后端分离结构。小程序端面向学生，主要承担商家浏览、商品选择、购物车和订单查看等操作。Web 管理端面向商家和平台人员，适合展示表格、筛选条件、统计图和批量操作。后端服务对外提供统一接口，对内按控制层、业务层和数据访问层组织。数据库保存用户、地址、商家、商品、购物车、订单和订单明细等数据。":
    "系统采用前后端分离结构。小程序端面向学生，承担商家浏览、商品选择、购物车和订单查看等操作；Web 管理端面向商家和平台人员，承担商品维护、订单处理、商家管理和统计分析。后端服务对外提供统一接口，对内按控制层、业务层和数据访问层组织。登录后，拦截器解析 JWT，并把当前学生编号、员工编号、商家编号和账号类型写入线程上下文，业务层随后根据上下文进行权限和商家范围判断。",

    "从整体架构看，学生端和管理端虽然使用场景不同，但都通过后端服务访问同一套核心数据。图4-1将这种分层关系表达出来：前端负责交互，后端负责规则，数据层负责保存结果，这样才能在商家管理、订单统计和平台治理之间保持一致。":
    "从整体架构看，学生端和管理端虽然使用场景不同，但都通过后端服务访问同一套核心数据。图4-1将这种分层关系表达出来：前端负责交互，后端负责身份识别、商家范围、订单状态和异常处理，数据层负责保存用户、商家、商品、购物车、订单和统计所需的业务数据。这样可以避免不同端各自维护规则造成结果不一致。",

    "接口协同的关键在于前端动作和后端规则之间要保持一致。学生端接口主要服务于登录、商家浏览、购物车、订单提交和订单查询；管理端接口主要服务于商家资料、商品维护、订单处理和统计分析。前端页面负责把操作入口展示给用户，后端服务负责判断操作是否允许。":
    "接口协同的关键在于前端动作和后端规则之间要保持一致。学生端接口主要服务于登录、商家浏览、购物车、订单提交和订单查询；管理端接口主要服务于商家资料、商品维护、订单处理和统计分析。前端页面只提供操作入口，真正的身份判断、商家范围校验、状态流转和重复提交控制都在服务端完成，因此即使前端参数异常，服务端仍能拦截不符合规则的请求。",

    "权限边界主要体现在商家范围上。学生只能访问自己的地址、购物车和订单；商家账号只能访问本商家的商品、订单和经营数据；平台账号可以维护跨商家的基础数据。系统把这些规则放在服务端处理，而不是只依靠前端隐藏按钮，是为了防止越权访问和数据混乱。":
    "权限边界主要体现在商家范围上。学生只能访问自己的地址、购物车和订单；商家账号只能访问本商家的商品、订单和经营数据；平台账号可以维护跨商家的基础数据。服务端通过统一的商家范围校验组件处理这些规则，平台账号查询时可以保留全局范围，商家账号查询时会被限制到绑定商家，写入和修改操作也必须与当前商家一致。",

    "数据库设计服务于校园点餐业务主线。系统中的核心实体可以分为用户与地址、商家与商品、购物车与订单、平台与商家管理四类。用户和地址用于支撑学生下单，商家、分类、菜品和套餐用于支撑商品浏览，购物车和订单用于承接交易过程，平台管理相关数据用于维护商家状态和运行边界。":
    "数据库设计服务于校园点餐业务主线。系统中的核心实体可以分为用户与地址、校区与商家、商家与商品、购物车与订单四类。用户和地址用于确定下单人和取餐联系信息；校区和商家用于限定服务范围；分类、菜品和套餐通过商家编号归属到具体商家；购物车和订单继续保存商家编号，使商品选择、结算和订单处理能够沿同一条商家范围推进。",

    "数据库设计遵循三项原则。第一，业务关系要清楚。一个学生可以有多个地址，一个商家可以有多个分类和商品，一笔订单可以包含多条订单明细。第二，商家归属要明确。分类、菜品、套餐、购物车和订单都需要能够关联到具体商家。第三，状态字段要与业务流程一致。订单状态、支付状态、商家启用状态和营业状态应当服务于真实操作，而不是只作为展示字段。":
    "数据库设计遵循三项原则。第一，业务关系要清楚：一个学生可以有多个地址，一个商家可以有多个分类和商品，一笔订单可以包含多条订单明细。第二，商家归属要明确：分类、菜品、套餐、购物车和订单都保存商家编号，保证查询、结算和统计时能够按商家隔离。第三，字段和索引要服务真实查询：订单表围绕学生历史订单、商家按状态处理订单、商家按支付状态和时间统计等场景建立查询条件。",

    "核心数据表围绕学生、商家、商品和订单展开。用户表保存学生身份信息，地址表保存学生常用地址；商家表保存商家名称、编码、联系方式、启用状态、营业状态和展示排序；分类表、菜品表和套餐表保存商家范围内的商品结构；购物车表保存学生临时选择的商品；订单表保存订单整体信息，订单明细表保存订单中的具体商品。":
    "核心数据表围绕学生、商家、商品和订单展开。用户表保存学生身份信息，地址表保存学生常用地址；商家表保存商家名称、编码、联系方式、启用状态、营业状态和展示排序；分类表、菜品表和套餐表通过商家编号保存本店商品结构；购物车表保存学生、商家、商品、规格、数量和金额；订单表保存校区、商家、订单金额、支付状态和业务状态，订单明细表保存下单时的商品快照。",

    "购物车是正式订单生成前的临时容器。系统在购物车中保存学生、商家、商品或套餐、规格、数量和金额等信息。学生添加商品时，服务端会先识别当前学生和当前商家，再校验商品是否属于该商家。如果购物车中已经存在相同商品和规格组合，则累加数量；如果不存在，则新增记录。":
    "购物车是正式订单生成前的业务缓冲区。系统在购物车中保存学生、商家、商品或套餐、规格、数量和金额等信息。学生添加商品时，服务端先从身份上下文取得当前学生，再根据请求中的商家信息确定购物车范围，并校验菜品或套餐是否属于当前商家。如果购物车中已经存在相同商品和规格组合，则累加数量；如果不存在，则新增记录。该设计既减少重复行，也避免跨商家商品进入同一次结算。",

    "订单生成是把购物车数据转为正式交易数据的过程。学生提交订单时，服务端需要校验地址是否存在、商家是否可用、购物车是否为空、金额是否合理。校验通过后，系统写入订单主表和订单明细表，并清理当前商家范围内已提交的购物车数据。由于提交订单属于高敏感操作，系统还需要通过短时锁或类似机制降低重复点击造成重复订单的风险。":
    "订单生成是把购物车数据转为正式业务数据的过程。学生提交订单时，服务端先使用短时提交锁限制重复点击，再校验地址是否存在、商家是否启用、商家是否营业、购物车是否为空以及购物车记录是否属于当前商家。校验通过后，系统在同一事务中写入订单主表和订单明细表，并清理当前商家范围内已提交的购物车数据。这样可以避免只生成主订单、明细缺失或购物车被错误清空等不一致情况。",

    "支付结果处理的重点，是把支付结果转换为系统内部的订单状态变化。学生发起支付后，服务端根据订单编号查询订单，确认订单存在且属于当前用户，再处理支付结果。支付成功后，系统将订单从待支付改为待商家处理，并更新支付状态和支付时间。这个过程不能只依赖前端页面显示，因为前端页面可能刷新、关闭或被异常请求影响。":
    "支付结果处理的重点，是把支付结果转换为系统内部的订单状态变化。学生发起支付后，服务端根据订单编号和当前学生查询订单，确认订单存在且未支付，再处理支付结果。支付成功后，系统将订单从待支付改为待商家处理，并更新支付状态和支付时间。服务端在更新前还会判断订单当前状态，避免重复回调或异常请求造成订单状态被反复修改。",

    "消息通知用于提高商家处理效率。当订单支付成功或学生催单时，服务端可以向对应商家端发送提醒，使商家及时看到待处理订单。通知本身不是订单数据的唯一依据，商家端仍以服务端查询结果为准。这样既能提升实时性，也能保持数据一致。":
    "消息通知用于提高商家处理效率。当订单支付成功或学生催单时，服务端构造包含事件类型、订单编号和商家编号的消息，并通过 WebSocket 发送给对应商家的管理端账号。平台账号也可以接收相关提醒，用于观察系统运行情况。通知只负责提醒，订单真实状态仍以服务端查询结果为准，这样既能提升实时性，也能保持数据一致。",

    "安全设计主要包括身份认证、商家范围校验、输入校验和敏感信息保护。学生端和管理端登录后，后续请求都需要携带身份信息。学生只能访问自己的地址、购物车和订单；商家只能访问本商家的商品、订单和经营数据；平台账号负责跨商家的基础资料维护。":
    "安全设计主要包括身份认证、商家范围校验、输入校验和敏感信息保护。学生端和管理端登录后，后续请求都需要携带令牌。服务端解析令牌后，将当前用户、商家和账号类型保存到请求上下文中；业务层再根据上下文限制数据范围。学生只能访问自己的地址、购物车和订单；商家只能访问本商家的商品、订单和经营数据；平台账号负责跨商家的基础资料维护。",

    "异常处理的目标是让错误结果明确、原因可追踪。地址为空、购物车为空、商家停业、订单状态不合法、重复提交等情况都应由服务端统一处理，并向前端返回清晰提示。统一异常处理可以减少控制层重复代码，也能让论文中的可靠性设计落到真实实现上。":
    "异常处理的目标是让错误结果明确、原因可追踪。地址为空、购物车为空、商家停业、订单状态不合法、重复提交、商家范围不匹配等情况都由服务端统一判断，并向前端返回明确提示。控制层不直接拼接复杂判断，而是把核心规则放在业务层和范围校验组件中，减少重复代码，也避免不同接口对同一规则处理不一致。",

    "后端代码遵循控制层、业务层和数据访问层的分工。控制层负责接收请求和返回结果，业务层负责购物车归属判断、订单金额计算、订单状态校验、支付结果处理和商家范围控制，数据访问层负责执行数据库查询和更新。这样的结构能够把论文中的总体设计落到工程实现上。":
    "后端代码遵循控制层、业务层和数据访问层的分工。控制层负责接收请求和返回结果；业务层负责购物车归属判断、订单金额计算、订单状态校验、支付结果处理、商家范围控制和消息提醒；数据访问层负责执行数据库查询和更新。身份上下文、商家范围校验和事务控制集中在服务端完成，使前端页面变化不会破坏核心业务规则。",

    "登录功能用于建立后续操作所需的身份上下文。学生端登录后，系统能够识别当前学生，从而确定地址、购物车和订单归属。管理端登录时，系统不仅校验账号和密码，还要识别账号类型和商家归属。平台账号和商家账号虽然都进入 Web 管理端，但可操作的数据范围不同。":
    "登录功能用于建立后续操作所需的身份上下文。学生端登录后，令牌中保存学生编号，后续地址、购物车和订单操作都以该编号限定数据范围。管理端登录时，令牌中除员工编号外，还保存账号类型和商家编号。请求进入服务端后，拦截器解析令牌并写入线程上下文，业务层再依据上下文区分平台账号和商家账号的数据权限。",

    "商品浏览阶段不是单纯展示菜单。学生选择商家和商品时，系统需要同时关注商家是否营业、商品是否可售、规格是否选择完整等条件。图6-2把页面选择和业务校验放在同一条流程中，体现了前端反馈与后端判断之间的配合。":
    "商品浏览阶段不是单纯展示菜单。学生选择商家和商品时，系统需要同时关注商家是否启用、是否营业、商品是否可售以及规格是否选择完整。前端负责展示分类、菜品和套餐，服务端根据商家编号查询对应商品数据，并在加入购物车和提交订单时再次校验商家与商品归属。图6-2把页面选择和业务校验放在同一条流程中，体现了前端反馈与后端判断之间的配合。",

    "添加购物车是学生端高频操作。学生选择商品或套餐后，前端将商品标识、规格、数量和当前商家信息提交给后端。服务端先识别当前学生和商家，再校验商品是否属于该商家。若购物车中已经存在相同商品和规格组合，系统只更新数量；若不存在，则新增购物车记录。":
    "添加购物车是学生端高频操作。学生选择商品或套餐后，前端将商品标识、规格、数量和当前商家信息提交给后端。服务端先识别当前学生和商家，再校验商品是否属于该商家。若购物车中已经存在相同商品和规格组合，系统只更新数量；若不存在，则新增购物车记录。购物车查询和清空同样按学生与商家共同限定范围，避免影响学生在其他商家的未提交商品。",

    "订单提交流程把购物车数据转换为正式订单。学生确认地址、商品明细、金额和备注后提交订单。服务端首先控制重复提交，再校验地址、商家状态、购物车内容和金额，随后生成订单主表与订单明细，并清理已提交的购物车。这个过程集中在服务端完成，是为了保证订单数据完整可靠。":
    "订单提交流程把购物车数据转换为正式订单。学生确认地址、商品明细、金额和备注后提交订单。服务端首先通过 Redis 短时锁控制重复提交，再校验地址、商家启用状态、营业状态、购物车内容和商家归属。校验通过后，订单主表、订单明细和购物车清理在同一事务内完成。这个过程集中在服务端完成，是为了保证订单数据完整可靠。",

    "支付结果处理位于订单提交之后。系统根据订单编号处理支付结果，支付成功后修改订单状态和支付状态，并让商家端能够看到新的待处理订单。支付结果不能只由学生端页面决定，因为页面状态不等同于服务端订单状态。":
    "支付结果处理位于订单提交之后。系统根据订单编号处理支付结果，支付成功后修改订单状态和支付状态，并记录支付时间。随后服务端向对应商家端发送新订单提醒，使商家及时看到待处理订单。支付结果不能只由学生端页面决定，因为页面状态不等同于服务端订单状态，最终状态必须以服务端更新结果为准。",

    "商家订单处理模块承接学生端订单。商家登录后只能查看本商家范围内的订单，并按照订单状态进行接单、拒单、处理和完成等操作。服务端在每个操作中校验当前状态和商家归属，防止商家处理不属于自己的订单，也防止订单状态被跳过。":
    "商家订单处理模块承接学生端订单。商家登录后只能查看本商家范围内的订单，并按照订单状态进行接单、拒单、出餐和完成等操作。服务端在每个操作中先校验商家归属，再校验当前订单状态。例如，只有待商家处理的订单才能接单或拒单，只有已接单订单才能进入出餐处理，只有处理中订单才能完成。该机制防止商家处理不属于自己的订单，也防止订单状态被跳过。",

    "平台管理模块主要负责商家基础数据和运行状态。平台人员可以维护商家资料、启用状态、营业状态、展示顺序和联系方式。商家信息既影响学生端商家列表，也影响商家端数据范围，还会成为订单归属判断的重要依据。":
    "平台管理模块主要负责商家基础数据和运行状态。平台人员可以维护商家资料、启用状态、营业状态、展示顺序和联系方式。商家信息既影响学生端商家列表，也影响商家端数据范围，还会成为购物车和订单归属判断的重要依据。平台禁用商家后，商家不再作为有效对象参与下单；商家打烊后，学生端仍可看到商家信息，但订单提交会被服务端拦截。",

    "统计分析页面展示了订单趋势、时段分布和热销商品等运营数据，说明系统不仅支持交易处理，也能为商家经营提供基础数据反馈。":
    "统计分析页面展示订单趋势、时段分布和热销商品等运营数据。这些数据由订单表和订单明细表按日期、订单状态和商家范围聚合得到。平台账号可以查看整体经营情况，商家账号只能查看本商家数据。通过营业额、有效订单数、完成率、客单价和热销商品等指标，系统能够为商家经营提供基础数据反馈。"
}


RISKY_TERMS = [
    "答辩老师",
    "非专业老师",
    "写作主线",
    "与只按功能菜单展开的论文相比",
    "论文能够回答",
    "被解释为",
    "不是简单页面拼接",
]

FORBIDDEN_TERMS = [
    "sky_take_out",
    "苍穹外卖",
    "校园外卖",
    "外卖",
    "配送",
]


def replace_paragraph_text(paragraph, text: str) -> None:
    for run in paragraph.runs:
        run.text = ""
    if paragraph.runs:
        paragraph.runs[0].text = text
    else:
        paragraph.add_run(text)


def apply_replacements(path: Path) -> int:
    doc = Document(str(path))
    changed = 0
    missing = []
    by_text = {paragraph.text.strip(): paragraph for paragraph in doc.paragraphs if paragraph.text.strip()}
    for old, new in REPLACEMENTS.items():
        paragraph = by_text.get(old)
        if paragraph is None:
            missing.append(old[:40])
            continue
        replace_paragraph_text(paragraph, new)
        changed += 1
    if missing:
        print("missing replacements:")
        for item in missing:
            print("-", item)
    doc.save(str(path))
    return changed


def scan_text(path: Path) -> list[str]:
    doc = Document(str(path))
    problems = []
    terms = RISKY_TERMS + FORBIDDEN_TERMS
    for index, paragraph in enumerate(doc.paragraphs):
        text = paragraph.text.strip()
        for term in terms:
            if term in text:
                problems.append(f"{index}: {term}: {text}")
    return problems


def main() -> None:
    parser = argparse.ArgumentParser()
    parser.add_argument("--docx", required=True)
    args = parser.parse_args()
    path = Path(args.docx)
    backup = path.with_name(path.stem + ".before_source_evidence_rewrite.docx")
    if not backup.exists():
        shutil.copy2(path, backup)
    changed = apply_replacements(path)
    problems = scan_text(path)
    print(f"changed={changed}")
    print(f"backup={backup}")
    if problems:
        print("scan problems:")
        for problem in problems:
            print(problem)
        raise SystemExit(2)
    print("scan passed")


if __name__ == "__main__":
    main()
```

- [ ] **Step 2: Run the patch script**

Run:

```powershell
& "C:\Users\g'y'c\.cache\codex-runtimes\codex-primary-runtime\dependencies\python\python.exe" "D:\sky-delivery\core\paper_work\core_chapters_upgrade\scripts\apply_source_evidence_rewrite.py" --docx "D:\sky-delivery\core\paper_work\core_chapters_upgrade\outputs\毕业论文1_完整版_正文增强版.docx"
```

Expected: `changed=27` and `scan passed`. If Word locks the file, close Word or use Word automation to save the same replacements into the open document.

### Task 3: Verify Text Quality and Risk Terms

**Files:**
- Read: `D:\sky-delivery\core\paper_work\core_chapters_upgrade\outputs\毕业论文1_完整版_正文增强版.docx`

- [ ] **Step 1: Print key rewritten paragraphs**

Run:

```powershell
@'
import sys
from docx import Document
doc=Document(sys.argv[1])
keywords=["ThreadLocal","Redis","事务","WebSocket","商家范围","购物车","订单状态","统计分析页面"]
for i,p in enumerate(doc.paragraphs):
    text=p.text.strip()
    if text and any(k in text for k in keywords):
        print(f"{i}: {text}")
'@ | & "C:\Users\g'y'c\.cache\codex-runtimes\codex-primary-runtime\dependencies\python\python.exe" - "D:\sky-delivery\core\paper_work\core_chapters_upgrade\outputs\毕业论文1_完整版_正文增强版.docx"
```

Expected: output includes rewritten paragraphs discussing JWT/ThreadLocal, Redis short lock, transaction, WebSocket, merchant scope, cart scope, order status, and reporting.

- [ ] **Step 2: Run risk-term scan**

Run:

```powershell
@'
import sys
from docx import Document
terms=["答辩老师","非专业老师","写作主线","与只按功能菜单展开的论文相比","论文能够回答","被解释为","不是简单页面拼接","sky_take_out","苍穹外卖","校园外卖","外卖","配送"]
doc=Document(sys.argv[1])
found=False
for i,p in enumerate(doc.paragraphs):
    text=p.text.strip()
    hits=[term for term in terms if term in text]
    if hits:
        found=True
        print(f"{i}: {hits}: {text}")
print("FOUND" if found else "scan passed")
'@ | & "C:\Users\g'y'c\.cache\codex-runtimes\codex-primary-runtime\dependencies\python\python.exe" - "D:\sky-delivery\core\paper_work\core_chapters_upgrade\outputs\毕业论文1_完整版_正文增强版.docx"
```

Expected: `scan passed`.

### Task 4: Render and Inspect DOCX Pages

**Files:**
- Read/render: `D:\sky-delivery\core\paper_work\core_chapters_upgrade\outputs\毕业论文1_完整版_正文增强版.docx`
- Create: `D:\sky-delivery\core\paper_work\core_chapters_upgrade\renders\source_evidence_rewrite_check\page-*.png`

- [ ] **Step 1: Render DOCX to PNG only**

Run:

```powershell
& "C:\Users\g'y'c\.cache\codex-runtimes\codex-primary-runtime\dependencies\python\python.exe" "C:\Users\g'y'c\.codex\plugins\cache\openai-primary-runtime\documents\26.423.10653\skills\documents\render_docx.py" "D:\sky-delivery\core\paper_work\core_chapters_upgrade\outputs\毕业论文1_完整版_正文增强版.docx" --output_dir "D:\sky-delivery\core\paper_work\core_chapters_upgrade\renders\source_evidence_rewrite_check" --renderer artifact-tool
```

Expected: PNG pages are rendered. Do not export PDF.

- [ ] **Step 2: Inspect key pages**

Open these PNGs with `view_image`:

```text
D:\sky-delivery\core\paper_work\core_chapters_upgrade\renders\source_evidence_rewrite_check\page-12.png
D:\sky-delivery\core\paper_work\core_chapters_upgrade\renders\source_evidence_rewrite_check\page-14.png
D:\sky-delivery\core\paper_work\core_chapters_upgrade\renders\source_evidence_rewrite_check\page-19.png
D:\sky-delivery\core\paper_work\core_chapters_upgrade\renders\source_evidence_rewrite_check\page-24.png
D:\sky-delivery\core\paper_work\core_chapters_upgrade\renders\source_evidence_rewrite_check\page-34.png
```

Expected: no overlap, no broken captions, no obvious page overflow. Text should read as concrete system design rather than generic function description.

### Task 5: Final Delivery Check

**Files:**
- Final: `D:\sky-delivery\core\paper_work\core_chapters_upgrade\outputs\毕业论文1_完整版_正文增强版.docx`
- Backup: `D:\sky-delivery\core\paper_work\core_chapters_upgrade\outputs\毕业论文1_完整版_正文增强版.before_source_evidence_rewrite.docx`

- [ ] **Step 1: Confirm final file timestamp**

Run:

```powershell
Get-Item -LiteralPath "D:\sky-delivery\core\paper_work\core_chapters_upgrade\outputs\毕业论文1_完整版_正文增强版.docx" | Select-Object FullName,Length,LastWriteTime
```

Expected: `LastWriteTime` updates after Task 2.

- [ ] **Step 2: Report concise outcome**

Final response should include:

```text
已完成源码证据驱动正文重写，直接保存到原 DOCX。
重点增强：身份上下文、商家范围隔离、购物车双重归属、Redis 防重复提交、事务订单、订单状态、WebSocket 提醒、报表聚合、数据库索引。
已完成风险表达扫描和 PNG 渲染抽检。
```

## Self-Review

- Spec coverage: every evidence category in the design spec is covered by replacements or validation.
- Placeholder scan: no TBD/TODO/fill-in wording remains.
- Scope check: plan only touches the current thesis DOCX, one evidence note, one patch script, and PNG render output.
- Risk check: the plan explicitly scans risk phrases and old project words after rewriting.
