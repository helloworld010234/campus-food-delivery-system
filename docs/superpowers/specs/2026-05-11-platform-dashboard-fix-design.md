# 平台管理端显示问题修复设计

## 问题概述

修复平台管理员工作台（platformDashboard）的两个显示问题：
1. **商家状态图不显示**
2. **入驻商家数不正确**

## 根因分析

### 问题 1：商家状态图不显示

- `renderPlatformDashboard()` 在 HTML 中渲染了图表容器 `<div id="merchantStatusChart" style="height: 300px;"></div>`（`app.js:1045`）
- `charts.js` 中**没有**对应的 `createMerchantStatusChart` 初始化函数
- `app.js` 的 `renderApp()` 在视图切换后的 `setTimeout` 回调中（约 `2789` 行），只处理了 `dashboard`、`statistics`、`platformMerchants` 三种视图的图表/过滤初始化，**缺少 `platformDashboard` 分支**
- 因此该 `<div>` 永远只是空容器，Chart.js 图表从未被创建

### 问题 2：入驻商家数不正确

- `syncPlatformDashboardData()`（`app.js:559`）使用两次独立 API 调用：
  - 第一次：`API.Platform.getMerchantPage({ page: 1, pageSize: 1 })` 仅用于获取 `merchantPage.total`
  - 第二次：`fetchPagedRecords(...)` 获取所有商家记录填充列表
- 两次调用之间存在不一致风险（如第一次失败、返回异常 `total`、或网络问题导致 `totalMerchants` 与实际 `allMerchants.length` 不符）
- `platformStatistics.totalMerchants` 被设为 `merchantPage.total`，但下方列表实际渲染的是 `platformMerchants.length`
- 最小修复：移除单独的 `merchantPage` 调用，直接使用 `allMerchants.length` 作为总数，确保统计数字与实际列表严格一致

## 修复方案

### 修改 1：`charts.js` — 新增商家状态分布图表

新增 `createMerchantStatusChart(containerId, data)` 函数：
- 图表类型：doughnut（环形图），与现有 `createCategoryChart` 风格保持一致
- 数据：正常商家数 vs 已禁用商家数
- 颜色：使用现有 `CHART_THEME` 和 `CATEGORY_COLORS`
- 目标容器：`merchantStatusChart`

### 修改 2：`app.js` — 修复 HTML 容器与图表初始化

1. 在 `renderPlatformDashboard()` 中，将 `<div id="merchantStatusChart">` 改为与现有图表一致的 `<div style="height: 300px;"><canvas id="merchantStatusChart"></canvas></div>`
2. 在 `renderApp()` 的 `setTimeout` 回调中，添加 `currentView === 'platformDashboard'` 分支，调用 `createMerchantStatusChart('merchantStatusChart', platformStatistics)`

### 修改 3：`app.js` — 修复入驻商家数统计逻辑

在 `syncPlatformDashboardData()` 中：
- 删除单独的 `merchantPage` 调用和 `totalMerchants` 变量
- `fetchPagedRecords` 获取 `allMerchants` 后，直接使用 `allMerchants.length` 作为总数
- 保持 `activeCount` 和 `businessActiveCount` 从 `platformMerchants` 过滤计算的逻辑不变

## 影响范围

- **文件**：`core/nginx/html/sky/merchant-admin/assets/js/charts.js`、`core/nginx/html/sky/merchant-admin/assets/js/app.js`
- **无后端变更**
- **无数据库变更**
- **无 API 接口变更**

## 验证方式

1. 登录平台管理端（admin / 123456）
2. 导航至「平台工作台」
3. 验证：
   - 「商家状态分布」区域出现环形图（正常 vs 已禁用）
   - 「入驻商家」卡片数字与下方「商家列表」实际条数一致
   - 切换商家状态后刷新页面，图表和数字同步更新
