# 管理后台表单交互体验优化设计文档

## 项目背景

苍穹外卖管理后台是一个基于原生 JavaScript + Tailwind CSS CDN 的单页应用（SPA）。当前系统的商品、套餐、员工的新增/编辑操作全部使用浏览器原生 `prompt()` 逐条弹窗输入，每次操作需要连续确认 5-6 次弹窗，且分类/角色/性别等枚举字段需要用户手动输入数字 ID，体验极差。本设计将把这些操作统一改造为集中式表单弹窗。

## 设计目标

1. **消除所有 `prompt()` 连环弹窗** — 将商品/套餐/员工的新增和编辑改为单个表单弹窗
2. **枚举字段使用下拉选择** — 分类、角色、性别改为 `<select>` 或单选按钮
3. **套餐菜品组合可视化** — 用可勾选列表替代 `dishId*份数` 文本格式
4. **图片支持本地上传** — 复用 `CommonAPI.upload()`，支持选择文件+预览
5. **前端实时校验** — 必填项、格式、数值范围在提交前即时反馈
6. **搜索筛选局部更新** — 商家列表搜索不再触发整页 `renderApp()`
7. **统一 Dialog 组件** — 一致的遮罩、动画、关闭行为、Loading 状态

## 技术约束

- 保持原生 JavaScript，不引入 Vue/React/Angular
- 复用现有的 Toast 组件、withSubmitting/setSubmitting、escapeHtml
- 复用后端 API，无需后端改动
- Tailwind CSS CDN 已加载，可直接使用类名
- 图片上传复用 `CommonAPI.upload()` 接口

## 组件设计

### 1. FormDialog 工厂函数

```javascript
function createFormDialog({
    title,           // 弹窗标题
    width = 'md',    // 宽度: sm(400px) | md(500px) | lg(640px) | xl(720px)
    content,         // 表单 HTML 字符串
    onSubmit,        // 提交回调 (formData) => Promise<void>
    validate,        // 校验回调 (formData) => string | null
    submitText = '确认',
    cancelText = '取消'
})
```

行为：
- 点击遮罩层关闭
- ESC 键关闭
- 提交按钮自动进入 Loading 状态（复用 setSubmitting）
- 校验失败时聚焦到第一个错误字段
- 提交成功后自动关闭弹窗
- 关闭时从 DOM 中移除（不保留隐藏节点）

### 2. ImageUpload 控件

```javascript
function createImageUploadInput({
    id,
    initialUrl = '',
    placeholder = '选择图片文件'
})
```

行为：
- 显示为文件选择按钮 + 图片预览区域
- 选择文件后自动调用 `API.Common.upload()`
- 上传中显示 "上传中..." 文字
- 上传成功显示缩略图（80x80px，圆角）
- 上传失败显示 Toast 错误，保留原值
- 支持点击预览图重新选择
- 预览区下方显示图片 URL（可折叠）

### 3. DishComboSelector 控件（套餐菜品选择器）

```javascript
function createDishComboSelector({
    id,
    availableDishes,  // 当前商家的菜品列表
    initialItems = [] // 初始已选 [{dishId, name, quantity}]
})
```

行为：
- 顶部搜索框，实时过滤菜品列表
- 列表显示：复选框 + 菜品名称 + 份数输入框（数字，最小1）
- 勾选后份数输入框可用，默认1
- 取消勾选后份数输入框禁用
- 底部显示 "已选 N 项：xxx ×1, xxx ×2"
- 返回数据结构：`[{dishId, quantity}]`

## 表单详细设计

### 商品表单（新增/编辑共用）

字段清单：

| 字段 | 类型 | 必填 | 前端校验 |
|------|------|------|---------|
| name | text | 是 | 非空，长度 1-32 |
| categoryId | select | 是 | 非空，值在 options 中 |
| price | number | 是 | > 0，最多2位小数 |
| image | ImageUpload | 否 | URL 格式（上传后自动填充） |
| description | textarea | 否 | 长度 0-200 |
| status | hidden | — | 默认 1（启用） |

编辑模式差异：
- 标题从 "新增商品" 变为 "编辑商品"
- 所有字段预填充当前值
- 提交调用 `API.Dish.updateDish()` 而非 `addDish()`

### 套餐表单（新增/编辑共用）

字段清单：

| 字段 | 类型 | 必填 | 前端校验 |
|------|------|------|---------|
| name | text | 是 | 非空，长度 1-32 |
| categoryId | select | 是 | 非空，值在 options 中 |
| price | number | 是 | > 0，最多2位小数 |
| image | ImageUpload | 否 | URL 格式 |
| description | textarea | 否 | 长度 0-200 |
| setmealDishes | DishComboSelector | 是 | 至少选1个，每项份数 ≥ 1 |
| status | hidden | — | 默认 1 |

编辑模式差异：
- 标题从 "新增套餐" 变为 "编辑套餐"
- 所有字段预填充当前值
- 提交调用 `API.Setmeal.updateSetmeal()` 而非 `addSetmeal()`

### 员工表单（新增/编辑共用）

字段清单：

| 字段 | 类型 | 必填 | 前端校验 |
|------|------|------|---------|
| name | text | 是 | 非空，长度 1-20 |
| username | text | 是 | 非空，4-20字符，字母数字下划线 |
| phone | tel | 是 | 中国大陆手机号正则 |
| idNumber | text | 是 | 18位身份证号正则（简化版） |
| sex | radio | 是 | 1=男，2=女 |
| accountType | radio | 是 | 2=商家管理员，3=商家员工 |
| status | hidden | — | 默认 1 |

编辑模式差异：
- 标题从 "新增员工" 变为 "编辑员工"
- 用户名只读（不可修改，这是后端登录名）
- 提交调用 `API.Employee.updateEmployee()` 而非 `addEmployee()`

### 商家表单优化

已有 `openAddMerchantDialog()` 已使用表单弹窗，需要增强：

新增字段：
- logo | ImageUpload | 否 | 商家 Logo
- 前端校验：商家名称必填

### 搜索筛选局部更新

当前问题：`filterPlatformMerchants()` 调用 `renderApp()` 导致整页重绘。

改造方案：
1. `renderPlatformMerchantTable()` 不再读取 DOM（纯函数）
2. 新增 `getPlatformMerchantFilters()` 从 DOM 读取筛选值
3. `filterPlatformMerchants()` 只更新 tbody 的 innerHTML
4. 筛选逻辑提取为纯函数 `applyMerchantFilters(list, filters)`

## CSS 设计

表单样式使用 Tailwind CSS，统一如下：

```css
/* 表单输入框统一样式 */
.form-input {
    @apply w-full px-3 py-2 border rounded-lg text-sm;
    @apply focus:outline-none focus:ring-2 focus:ring-green-500 focus:border-transparent;
    border-color: #d6e5dc;
}

/* 表单标签 */
.form-label {
    @apply block text-sm font-medium mb-1;
    color: var(--text-primary);
}

/* 必填标记 */
.form-required::after {
    content: ' *';
    color: #cb4f4f;
}

/* 错误提示 */
.form-error {
    @apply text-xs mt-1;
    color: #cb4f4f;
}

/* 单选按钮组 */
.radio-group {
    @apply flex gap-4;
}

.radio-item {
    @apply flex items-center gap-2 cursor-pointer;
}

/* Dialog 动画 */
@keyframes dialogFadeIn {
    from { opacity: 0; transform: scale(0.96) translateY(-10px); }
    to { opacity: 1; transform: scale(1) translateY(0); }
}

.dialog-content {
    animation: dialogFadeIn 0.2s ease;
}
```

## 数据流

### 新增商品流程

```
用户点击"新增商品" → openAddProductDialog()
  ↓
创建 FormDialog（内含分类下拉、图片上传、价格输入等）
  ↓
用户填写表单 → 点击"确认"
  ↓
validate() 前端校验 → 失败则聚焦错误字段
  ↓
onSubmit() → withSubmitting(async () => {
    API.Dish.addDish(data)
    syncProductsData(true)
    Toast.show('新增商品成功', 'success')
    renderApp()
})
```

### 编辑商品流程

```
用户点击"编辑" → openEditProductDialog(id)
  ↓
从 products 数组获取 detail
  ↓
创建 FormDialog（预填充所有字段）
  ↓
用户修改 → 点击"确认"
  ↓
validate() 前端校验
  ↓
API.Dish.updateDish(data)
```

## 错误处理

| 场景 | 行为 |
|------|------|
| 前端校验失败 | 聚焦第一个错误字段，显示红色错误文字 |
| 图片上传失败 | Toast.show('图片上传失败：xxx', 'error')，保留原值 |
| API 提交失败 | Toast.show('新增失败：xxx', 'error')，弹窗不关闭 |
| 网络超时 | withSubmitting 已处理，显示"请求超时" |
| 401 未授权 | api-service.js 已处理，自动跳转登录 |

## 边界情况

1. **分类列表为空** — 表单中分类 `<select>` 显示 "暂无分类，请先创建分类"，提交按钮禁用
2. **菜品列表为空** — DishComboSelector 显示 "暂无菜品，请先创建菜品"
3. **图片 URL 超长** — 预览正常显示，URL 输入框只显示前 50 字符 + "..."
4. **编辑时数据已被删除** — `getById` 返回 null 时 Toast 提示并关闭弹窗
5. **快速切换编辑对象** — 关闭旧弹窗再打开新弹窗，不保留上次的编辑状态

## 回滚方案

如需回滚，恢复以下函数的 prompt() 实现即可：
- `openAddProductDialog` / `editProduct`
- `openAddSetmealDialog` / `editSetmeal`
- `openAddEmployeeDialog` / `editEmployee`

原始 prompt() 版本已记录在 git 历史（commit c899df4 之前）。

## 实施顺序

| 顺序 | 模块 | 依赖 |
|------|------|------|
| 1 | FormDialog 工厂 + CSS | 无 |
| 2 | 商品表单（新增+编辑） | FormDialog |
| 3 | 员工表单（新增+编辑） | FormDialog |
| 4 | DishComboSelector 控件 | 无 |
| 5 | 套餐表单（新增+编辑） | FormDialog + DishComboSelector |
| 6 | 商家表单增强（图片上传+校验） | ImageUpload |
| 7 | 搜索筛选局部更新 | 无 |
| 8 | gstack 浏览器验证 | 全部 |

## 测试验证矩阵

| 功能 | 测试步骤 | 预期结果 |
|------|---------|---------|
| 新增商品弹窗 | 点击"新增商品" | 弹出表单弹窗，包含所有字段 |
| 分类下拉选择 | 点击分类下拉 | 显示当前商家所有菜品分类 |
| 图片上传 | 选择本地图片 | 上传成功，显示预览缩略图 |
| 前端校验 | 不填名称直接提交 | 名称字段变红，聚焦到名称输入框 |
| 编辑商品预填充 | 点击"编辑"已有商品 | 所有字段显示当前值 |
| 套餐菜品选择 | 勾选 2 个菜品，输入份数 | 底部显示"已选 2 项" |
| 员工性别单选 | 点击"男"/"女" | 正确选中，提交值为 1/2 |
| 搜索商家 | 输入搜索词 | 表格即时过滤，页面不闪烁 |
