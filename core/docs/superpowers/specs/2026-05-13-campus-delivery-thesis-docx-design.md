# 《校园外卖设计与实现》成品 Word 交付设计

日期：2026-05-13

## 1. 目标

基于当前工作区内已经完成的 thesis workspace、源码证据索引、样稿分析结果和官方模板/现有初稿 `C:\Users\g'y'c\Desktop\初稿2_no_images.docx`，产出一份可交付的本科毕业论文主文档 `.docx`，以及一份附件 `.docx`。

本次交付采用“官方模板承载 + 证据优先重写”策略：

- 官方模板负责最终文档容器、章节承载和基础格式继承。
- 现有初稿只作为可复用素材来源，不直接视为事实来源。
- 正文事实以当前代码仓、SQL、接口、测试、截图和 workflow 证据为准。

## 2. 已确认约束

- 论文题目：《校园外卖设计与实现》
- 论文类型：本科毕业论文
- 模板身份：`C:\Users\g'y'c\Desktop\初稿2_no_images.docx` 既是学校/学院官方模板，也是现有初稿
- 交付范围：主论文 `.docx` + 附件 `.docx`
- 总字数要求：没有明确要求
- 任务书 / 开题报告 / 导师明确要求：没有
- 成稿策略：以源码和证据为准重写成更稳妥的定稿
- 质量边界：不使用渲染校验功能

## 3. 可用输入材料

### 3.1 模板与 workflow 产物

- `C:\Users\g'y'c\Desktop\初稿2_no_images.docx`
- [sample-docx-analysis.json](D:\sky-delivery\core\paper-context\workflow\sample-docx-analysis.json)
- [user-dashboard.md](D:\sky-delivery\core\paper-context\workflow\user-dashboard.md)
- [material-inventory.md](D:\sky-delivery\core\paper-context\workflow\material-inventory.md)
- [sample-template-analysis.md](D:\sky-delivery\core\paper-context\workflow\sample-template-analysis.md)
- [standard-profile.yaml](D:\sky-delivery\core\thesis-ai-standard\templates\standard-profile.yaml)
- [thesis-ai-spec.yaml](D:\sky-delivery\core\thesis-ai-standard\templates\thesis-ai-spec.yaml)
- [figure-registry.yaml](D:\sky-delivery\core\thesis-ai-standard\templates\figure-registry.yaml)

### 3.2 源码与项目证据

- 项目根目录：`D:\sky-delivery\core`
- 后端：Spring Boot 多模块 Maven 工程
- 小程序端：`miniapp/`
- 数据库脚本：`database/init.sql`、`backend/scripts/phase1_multi_merchant_schema.sql`
- 证据索引：
  - [tech-stack.md](D:\sky-delivery\core\paper-context\evidence\tech-stack.md)
  - [api-list.md](D:\sky-delivery\core\paper-context\evidence\api-list.md)
  - [database-schema.md](D:\sky-delivery\core\paper-context\evidence\database-schema.md)
  - [test-results.md](D:\sky-delivery\core\paper-context\evidence\test-results.md)
  - [data-dictionary.md](D:\sky-delivery\core\paper-context\evidence\data-dictionary.md)
  - [screenshot-index.md](D:\sky-delivery\core\paper-context\evidence\screenshot-index.md)

### 3.3 现有测试与截图证据

- surefire XML：16 份
- 汇总测试结果：80 tests，0 failures，0 errors，0 skipped
- JaCoCo 行覆盖率：约 21.77%
- 截图素材：
  - `miniapp/design/index.png`
  - `miniapp/design/dish.png`
  - `miniapp/design/detail.png`
  - `miniapp/design/action.gif`

## 4. 设计方案比较

### 方案 A：模板保留 + 证据重写

优点：

- 最大化继承官方模板外观
- 旧稿中可用结构可保留
- 正文事实可重建到较稳的状态

缺点：

- 内容替换量大
- 需要严格控制旧稿事实污染

### 方案 B：现有初稿深修

优点：

- 改动最少
- 速度较快

缺点：

- 容易继承旧稿问题
- 质量上限受旧稿约束

### 方案 C：从零生成新稿再回填模板

优点：

- 正文逻辑最干净
- 最适合完全重建

缺点：

- 与现有模板和初稿复用最少
- 回填成本和返工风险更高

### 推荐方案

采用方案 A：模板保留 + 证据重写。

理由：当前最重要的是在保住学校模板承载能力的前提下，把正文事实链尽可能拉回到源码和证据上。这是质量和交付风险最平衡的路线。

## 5. 正文重组策略

正文采用六章结构，以现有样稿目录节奏为骨架，但内容以重写为主。

### 第 1 章 绪论

- 保留章节角色，不直接照抄旧稿
- 重写背景、意义、研究内容和论文结构
- 国内外研究现状只做保守表述
- 若缺少新增文献池，则优先保留旧稿中看起来可用的文献框架，后续再人工复核

### 第 2 章 相关技术与需求基础

- 结合 `tech-stack.md`、`api-list.md` 和项目结构重写
- 重点写 uni-app、小程序端、Spring Boot、MyBatis、JWT、WebSocket、OSS、支付边界
- 同时加入角色边界、功能需求和非功能需求

### 第 3 章 系统架构与数据模型设计

- 作为重点重写章节
- 事实绑定到：
  - `database/init.sql`
  - `phase1_multi_merchant_schema.sql`
  - 实体类、Mapper、Controller
  - `database-schema.md`
  - `data-dictionary.md`
- 核心内容：
  - 总体架构
  - 前后端分离
  - 模块划分
  - 多商户隔离
  - 数据模型与接口边界

### 第 4 章 关键业务流程实现

- 作为核心重写章节
- 事实绑定到真实 controller / service / test / screenshot
- 重点流程：
  - 登录认证
  - 商户切换
  - 菜品浏览
  - 购物车隔离
  - 订单提交
  - 支付边界
  - 订单通知
  - 运营统计
- 不写无法在代码或测试中回指的业务细节

### 第 5 章 系统验证与质量分析

- 严格使用现有测试与覆盖率证据
- 事实绑定到：
  - surefire XML
  - JaCoCo CSV
  - 测试源码
- 允许写：
  - 功能测试
  - 关键流程验证
  - 质量分析
  - 局限性说明
- 不允许写：
  - 无证据的性能压测
  - 用户满意度调查
  - 生产环境运营数据

### 第 6 章 总结与展望

- 基于前文真实完成内容收束
- 不新增正文中未出现的新成果
- 展望只写合理的改进方向

## 6. 附件文档设计

附件 `.docx` 作为主论文的证据补充，不承担正文主叙事。

预计包含：

- 核心接口与页面映射表
- 测试证据摘要
- 数据库与核心实体说明
- 图表来源与截图来源说明
- 可放入附件的流程性补充材料

附件内容仍然必须来自真实路径和已登记证据，不补写未验证材料。

## 7. 产出流程

### 第一步：补全结构化规格

- 根据已确认信息更新 `standard-profile.yaml`
- 根据题目与论文类型更新 `thesis-ai-spec.yaml`
- 根据章节重点与已有素材更新 `figure-registry.yaml`
- 更新 workflow 状态文件

### 第二步：生成正文中间稿

- 先形成主论文 Markdown 中间稿
- 先做章节级事实校对，再做行文润色
- 保证每个核心段落都能找到源码、SQL、接口、测试或截图依据

### 第三步：构建附件中间稿

- 以表格和短说明为主
- 优先放入证据性强、正文不宜过长展开的材料

### 第四步：回填官方模板

- 以 `初稿2_no_images.docx` 为承载体
- 将主论文内容回填或替换到模板中
- 另生成附件 `.docx`
- 输出到 `paper-output/`

## 8. 质量门禁

本次只执行非渲染质量门禁。

### 执行项

- 检查模板文件、规格文件、工作流文件是否一致
- 检查章节结构是否与确认设计一致
- 检查正文主张是否能回指到 `paper-context/evidence/` 或真实源码路径
- 检查图表和附件条目是否有来源说明
- 检查主论文与附件文件是否成功生成

### 不执行项

- 不执行页面级渲染校验
- 不执行视觉排版人工验页
- 不声称已经完成视觉层面的最终版式验收

## 9. 风险与处理策略

### 风险 1：旧稿中存在与当前源码不一致的叙述

处理：

- 一律以源码和证据索引为准
- 旧稿只复用语言骨架，不复用未经核实的事实

### 风险 2：缺少完整文献池

处理：

- 先保守复用旧稿中已有文献框架
- 在交付说明中明确文献闭环尚未完成完全核验

### 风险 3：没有渲染校验

处理：

- 明确交付边界
- 将本次结果定义为“结构与证据链完成的成品稿”，不宣称经过视觉验页

### 风险 4：没有任务书、开题和导师要求

处理：

- 按系统设计类本科毕业论文保守建模
- 避免写“创新点过度拔高”或“导师定制要求”内容

## 10. 完成标准

当以下条件满足时，本任务视为完成：

- 在 `paper-output/` 下生成主论文 `.docx`
- 在 `paper-output/` 下生成附件 `.docx`
- 同时保留可回溯的 Markdown/证据/workflow 记录
- 正文重点章节已经按源码证据完成重写
- 第 3、4、5 章的主张能回指到已登记证据
- 附件内容与主论文不冲突
- 交付说明中清楚标注本次未执行渲染校验

## 11. 本次不做的事情

- 不承诺学校最终格式已视觉验收通过
- 不补写不存在的性能数据、问卷数据或生产数据
- 不在没有用户新增材料的前提下扩写不存在的任务书/导师要求
- 不把旧稿中未核实的内容直接保留为最终事实
