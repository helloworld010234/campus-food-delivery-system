# 管理后台表单交互体验优化 - 实现计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 将商品/套餐/员工的新增和编辑从 `prompt()` 连环弹窗改造为集中式表单弹窗，新增图片上传和套餐菜品可视化选择，优化搜索筛选局部更新。

**Architecture:** 原生 JS SPA，无框架。使用工厂函数 `createFormDialog()` 统一创建所有表单弹窗，共享遮罩、动画、校验、Loading 行为。DishComboSelector 为独立控件，内部管理复选和份数状态。

**Tech Stack:** 原生 JavaScript + Tailwind CSS CDN + 已有 Toast/Loading/escapeHtml 工具

---

## 文件清单

| 文件 | 路径 | 修改内容 |
|------|------|---------|
| `app.js` | `core/nginx/html/sky/merchant-admin/assets/js/app.js` | 新增 FormDialog/DishComboSelector/ImageUpload，重写所有 prompt() 操作 |
| `index.html` | `core/nginx/html/sky/merchant-admin/index.html` | 新增表单弹窗 CSS 样式 |

---

### Task 1: 表单弹窗 CSS 样式

**Files:**
- Modify: `core/nginx/html/sky/merchant-admin/index.html`

- [ ] **Step 1: 在 index.html <style> 标签内添加 Dialog 和表单样式**

在 `index.html` 的 `<style>` 标签内（在 `.toast-container` 样式之后，或任意位置）插入以下内容：

```css
.dialog-overlay {
    position: fixed;
    inset: 0;
    background: rgba(0, 0, 0, 0.45);
    z-index: 100;
    display: flex;
    align-items: center;
    justify-content: center;
    animation: fadeIn 0.2s ease;
}

.dialog-panel {
    background: #fff;
    border-radius: 14px;
    width: 100%;
    max-width: 500px;
    margin: 20px;
    padding: 24px;
    box-shadow: 0 20px 48px rgba(0,0,0,0.15);
    animation: dialogSlideIn 0.25s cubic-bezier(0.16, 1, 0.3, 1);
    max-height: 90vh;
    overflow-y: auto;
}

.dialog-panel.dialog-lg {
    max-width: 640px;
}

@keyframes dialogSlideIn {
    from { opacity: 0; transform: translateY(-16px) scale(0.97); }
    to   { opacity: 1; transform: translateY(0) scale(1); }
}

.form-row { margin-bottom: 16px; }

.form-label {
    display: block;
    font-size: 13px;
    font-weight: 500;
    color: var(--text-primary);
    margin-bottom: 6px;
}

.form-label .required {
    color: #cb4f4f;
    margin-left: 2px;
}

.form-input, .form-select, .form-textarea {
    width: 100%;
    padding: 10px 14px;
    border: 1px solid #d6e5dc;
    border-radius: 10px;
    font-size: 14px;
    color: var(--text-primary);
    background: #fff;
    transition: border-color 0.15s, box-shadow 0.15s;
}

.form-input:focus, .form-select:focus, .form-textarea:focus {
    outline: none;
    border-color: #2a7d58;
    box-shadow: 0 0 0 3px rgba(42,125,88,0.12);
}

.form-input.is-invalid, .form-select.is-invalid, .form-textarea.is-invalid {
    border-color: #cb4f4f;
    box-shadow: 0 0 0 3px rgba(203,79,79,0.12);
}

.form-error-text {
    font-size: 12px;
    color: #cb4f4f;
    margin-top: 4px;
}

.form-textarea {
    resize: vertical;
    min-height: 80px;
}

.radio-group {
    display: flex;
    gap: 20px;
}

.radio-item {
    display: flex;
    align-items: center;
    gap: 6px;
    cursor: pointer;
    font-size: 14px;
}

.radio-item input[type="radio"] {
    accent-color: #2a7d58;
    width: 16px;
    height: 16px;
    cursor: pointer;
}

.dialog-actions {
    display: flex;
    justify-content: flex-end;
    gap: 12px;
    margin-top: 24px;
    padding-top: 16px;
    border-top: 1px solid #f3f4f6;
}

.btn-cancel {
    padding: 10px 20px;
    border: 1px solid #d6e5dc;
    border-radius: 10px;
    font-size: 14px;
    color: var(--text-secondary);
    background: #fff;
    cursor: pointer;
    transition: all 0.15s;
}

.btn-cancel:hover { background: #f9fafb; }

.btn-primary-submit {
    padding: 10px 20px;
    border: none;
    border-radius: 10px;
    font-size: 14px;
    color: #fff;
    background: #2a7d58;
    cursor: pointer;
    transition: opacity 0.15s;
}

.btn-primary-submit:hover { opacity: 0.9; }

.btn-primary-submit:disabled {
    opacity: 0.6;
    cursor: not-allowed;
}

.image-upload-preview {
    width: 80px;
    height: 80px;
    border-radius: 10px;
    object-fit: cover;
    border: 1px solid #d6e5dc;
    background: #f9fafb;
}

.image-upload-row {
    display: flex;
    align-items: center;
    gap: 12px;
}

.image-upload-btn {
    padding: 8px 14px;
    border: 1px dashed #d6e5dc;
    border-radius: 10px;
    font-size: 13px;
    color: var(--text-secondary);
    background: #fff;
    cursor: pointer;
    transition: all 0.15s;
}

.image-upload-btn:hover {
    border-color: #2a7d58;
    color: #2a7d58;
}

.dish-selector-list {
    max-height: 240px;
    overflow-y: auto;
    border: 1px solid #d6e5dc;
    border-radius: 10px;
    padding: 8px;
    margin-top: 8px;
}

.dish-selector-item {
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 8px 10px;
    border-radius: 8px;
    transition: background 0.1s;
}

.dish-selector-item:hover { background: #f9fafb; }

.dish-selector-item label {
    display: flex;
    align-items: center;
    gap: 8px;
    flex: 1;
    cursor: pointer;
    font-size: 14px;
}

.dish-selector-item input[type="checkbox"] {
    accent-color: #2a7d58;
    width: 16px;
    height: 16px;
    cursor: pointer;
}

.dish-qty-input {
    width: 56px;
    padding: 4px 8px;
    border: 1px solid #d6e5dc;
    border-radius: 6px;
    font-size: 13px;
    text-align: center;
}

.dish-qty-input:disabled {
    background: #f3f4f6;
    opacity: 0.5;
}

.dish-selector-summary {
    font-size: 12px;
    color: var(--text-secondary);
    margin-top: 8px;
    padding: 6px 10px;
    background: #f1f7f2;
    border-radius: 8px;
}
```

- [ ] **Step 2: 验证 CSS 语法**

浏览器打开 http://localhost:3458，确认控制台没有 CSS 语法错误。

---

### Task 2: FormDialog 工厂函数

**Files:**
- Modify: `core/nginx/html/sky/merchant-admin/assets/js/app.js`

在 `app.js` 的 `buildCategoryHint` 函数之前（约第 1720 行附近），插入以下代码：

```javascript
function createFormDialog({ title, width = 'md', content, onSubmit, validate, submitText = '确认', cancelText = '取消' }) {
    const dialog = document.createElement('div');
    dialog.className = 'dialog-overlay';
    const widthClass = width === 'lg' ? 'dialog-lg' : '';

    dialog.innerHTML = `
        <div class="dialog-panel ${widthClass}" role="dialog" aria-modal="true">
            <h3 style="font-size: 18px; font-weight: 600; margin-bottom: 20px; color: var(--text-primary);">${escapeHtml(title)}</h3>
            <form id="dialogForm" autocomplete="off">${content}</form>
            <div class="dialog-actions">
                <button type="button" class="btn-cancel" id="dialogCancel">${escapeHtml(cancelText)}</button>
                <button type="submit" class="btn-primary-submit" id="dialogSubmit" form="dialogForm">${escapeHtml(submitText)}</button>
            </div>
        </div>
    `;

    document.body.appendChild(dialog);

    const form = dialog.querySelector('#dialogForm');
    const submitBtn = dialog.querySelector('#dialogSubmit');

    function closeDialog() {
        dialog.style.animation = 'fadeIn 0.15s ease reverse';
        dialog.querySelector('.dialog-panel').style.animation = 'dialogSlideIn 0.2s ease reverse';
        setTimeout(() => dialog.remove(), 200);
    }

    function clearErrors() {
        form.querySelectorAll('.form-error-text').forEach((el) => el.remove());
        form.querySelectorAll('.is-invalid').forEach((el) => el.classList.remove('is-invalid'));
    }

    function showFieldError(fieldId, message) {
        const field = form.querySelector(`[name="${fieldId}"], #${fieldId}`);
        if (!field) return;
        field.classList.add('is-invalid');
        const errorEl = document.createElement('div');
        errorEl.className = 'form-error-text';
        errorEl.textContent = message;
        if (field.parentElement.classList.contains('radio-group') || field.parentElement.classList.contains('image-upload-row')) {
            field.parentElement.after(errorEl);
        } else {
            field.after(errorEl);
        }
        if (field.focus) field.focus();
    }

    dialog.addEventListener('click', (e) => {
        if (e.target === dialog) closeDialog();
    });

    dialog.querySelector('#dialogCancel').addEventListener('click', closeDialog);

    dialog.addEventListener('keydown', (e) => {
        if (e.key === 'Escape') closeDialog();
    });

    form.addEventListener('submit', async (e) => {
        e.preventDefault();
        if (submitBtn.disabled) return;
        clearErrors();

        const formData = new FormData(form);
        const data = Object.fromEntries(formData.entries());

        if (validate) {
            const error = validate(data, form);
            if (error) {
                const [fieldId, message] = error;
                showFieldError(fieldId, message);
                return;
            }
        }

        try {
            submitBtn.disabled = true;
            submitBtn.textContent = '提交中...';
            await onSubmit(data, form);
            closeDialog();
        } catch (err) {
            submitBtn.disabled = false;
            submitBtn.textContent = submitText;
            throw err;
        }
    });

    // Auto-focus first input
    const firstInput = form.querySelector('input:not([type="hidden"]), select, textarea');
    if (firstInput) firstInput.focus();

    return { dialog, closeDialog, clearErrors, showFieldError };
}

function createImageUploadRow({ name, initialUrl = '', label = '图片' }) {
    const id = `img_${name}_${Date.now()}`;
    return {
        html: `
            <div class="form-row">
                <label class="form-label">${escapeHtml(label)}</label>
                <div class="image-upload-row">
                    ${initialUrl ? `<img src="${escapeHtml(initialUrl)}" class="image-upload-preview" id="${id}_preview" />` : `<div class="image-upload-preview" id="${id}_preview" style="display:flex;align-items:center;justify-content:center;color:#999;font-size:12px;">无图片</div>`}
                    <div style="flex:1;">
                        <input type="file" id="${id}_file" accept="image/*" style="display:none;" />
                        <button type="button" class="image-upload-btn" onclick="document.getElementById('${id}_file').click()">选择文件</button>
                        <input type="hidden" name="${name}" id="${id}_url" value="${escapeHtml(initialUrl)}" />
                        <div id="${id}_status" style="font-size:12px;color:var(--text-secondary);margin-top:4px;">支持 jpg/png，最大 2MB</div>
                    </div>
                </div>
            </div>
        `,
        init() {
            const fileInput = document.getElementById(`${id}_file`);
            const urlInput = document.getElementById(`${id}_url`);
            const preview = document.getElementById(`${id}_preview`);
            const status = document.getElementById(`${id}_status`);

            fileInput.addEventListener('change', async () => {
                const file = fileInput.files[0];
                if (!file) return;
                status.textContent = '上传中...';
                try {
                    const url = await API.Common.upload(file);
                    urlInput.value = url;
                    if (preview.tagName === 'IMG') {
                        preview.src = url;
                    } else {
                        preview.outerHTML = `<img src="${escapeHtml(url)}" class="image-upload-preview" id="${id}_preview" />`;
                    }
                    status.textContent = '上传成功';
                    status.style.color = '#2a7d58';
                } catch (err) {
                    status.textContent = '上传失败：' + (err.message || '未知错误');
                    status.style.color = '#cb4f4f';
                    fileInput.value = '';
                }
            });
        },
        getUrl() {
            return document.getElementById(`${id}_url`)?.value || '';
        }
    };
}

function createDishComboSelector({ name, dishes, initialItems = [] }) {
    const id = `combo_${name}_${Date.now()}`;
    const initialMap = new Map();
    initialItems.forEach((item) => {
        initialMap.set(String(item.dishId || item.id), item.quantity || item.copies || 1);
    });

    const dishRows = dishes.map((d) => {
        const isChecked = initialMap.has(String(d.id));
        const qty = initialMap.get(String(d.id)) || 1;
        return `
            <div class="dish-selector-item">
                <label>
                    <input type="checkbox" data-dish-id="${d.id}" ${isChecked ? 'checked' : ''} />
                    <span>${escapeHtml(d.name)}</span>
                </label>
                <input type="number" class="dish-qty-input" data-qty-for="${d.id}" value="${qty}" min="1" ${isChecked ? '' : 'disabled'} />
            </div>
        `;
    }).join('');

    return {
        html: `
            <div class="form-row">
                <label class="form-label">菜品组合 <span class="required">*</span></label>
                <input type="text" id="${id}_search" class="form-input" placeholder="搜索菜品..." style="margin-bottom:8px;" />
                <div class="dish-selector-list" id="${id}_list">${dishRows}</div>
                <div class="dish-selector-summary" id="${id}_summary">已选 0 项</div>
                <input type="hidden" name="${name}" id="${id}_value" />
            </div>
        `,
        init() {
            const searchInput = document.getElementById(`${id}_search`);
            const list = document.getElementById(`${id}_list`);
            const summary = document.getElementById(`${id}_summary`);
            const valueInput = document.getElementById(`${id}_value`);

            function updateSummary() {
                const checked = list.querySelectorAll('input[type="checkbox"]:checked');
                const items = [];
                checked.forEach((cb) => {
                    const dishId = cb.dataset.dishId;
                    const qty = list.querySelector(`[data-qty-for="${dishId}"]`).value;
                    const name = cb.nextElementSibling.textContent;
                    items.push(`${escapeHtml(name)} ×${qty}`);
                });
                summary.textContent = items.length ? `已选 ${items.length} 项：${items.join('，')}` : '已选 0 项';

                const data = Array.from(checked).map((cb) => ({
                    dishId: Number(cb.dataset.dishId),
                    quantity: Number(list.querySelector(`[data-qty-for="${cb.dataset.dishId}"]`).value) || 1
                }));
                valueInput.value = JSON.stringify(data);
            }

            list.addEventListener('change', (e) => {
                if (e.target.type === 'checkbox') {
                    const qtyInput = list.querySelector(`[data-qty-for="${e.target.dataset.dishId}"]`);
                    qtyInput.disabled = !e.target.checked;
                    if (e.target.checked && qtyInput.value < 1) qtyInput.value = 1;
                    updateSummary();
                }
            });

            list.addEventListener('input', (e) => {
                if (e.target.classList.contains('dish-qty-input')) {
                    updateSummary();
                }
            });

            searchInput.addEventListener('input', () => {
                const term = searchInput.value.trim().toLowerCase();
                list.querySelectorAll('.dish-selector-item').forEach((row) => {
                    const name = row.querySelector('span').textContent.toLowerCase();
                    row.style.display = name.includes(term) ? '' : 'none';
                });
            });

            updateSummary();
        },
        getValue() {
            const raw = document.getElementById(`${id}_value`)?.value || '[]';
            try { return JSON.parse(raw); } catch { return []; }
        },
        validate() {
            const val = this.getValue();
            if (!val || val.length === 0) return '请至少选择一个菜品';
            return null;
        }
    };
}
```

- [ ] **Step 2: 验证语法**

运行 `node --check core/nginx/html/sky/merchant-admin/assets/js/app.js` 确认无语法错误。

---

### Task 3: 商品表单替换 prompt()

**Files:**
- Modify: `core/nginx/html/sky/merchant-admin/assets/js/app.js`

- [ ] **Step 1: 重写 openAddProductDialog**

将 `openAddProductDialog()` 从 `async function` 改为普通函数（内部异步事件处理），使用 `createFormDialog`：

```javascript
function openAddProductDialog() {
    if (dishCategoryOptions.length === 0) {
        Toast.show('请先在后端创建菜品分类', 'error');
        return;
    }

    const categoryOptions = dishCategoryOptions.map((c) =>
        `<option value="${c.id}">${escapeHtml(c.name)}</option>`
    ).join('');

    const imageUpload = createImageUploadRow({ name: 'image', label: '商品图片' });

    const { dialog, closeDialog } = createFormDialog({
        title: '新增商品',
        content: `
            <div class="form-row">
                <label class="form-label">商品名称 <span class="required">*</span></label>
                <input type="text" name="name" class="form-input" placeholder="请输入商品名称" />
            </div>
            <div class="form-row">
                <label class="form-label">商品分类 <span class="required">*</span></label>
                <select name="categoryId" class="form-select">
                    <option value="">请选择分类</option>
                    ${categoryOptions}
                </select>
            </div>
            <div class="form-row">
                <label class="form-label">商品价格（元） <span class="required">*</span></label>
                <input type="number" name="price" class="form-input" placeholder="请输入价格" step="0.01" min="0.01" />
            </div>
            ${imageUpload.html}
            <div class="form-row">
                <label class="form-label">商品描述</label>
                <textarea name="description" class="form-textarea" placeholder="请输入商品描述（可选）"></textarea>
            </div>
            <input type="hidden" name="status" value="1" />
        `,
        validate(data) {
            if (!data.name?.trim()) return ['name', '商品名称不能为空'];
            if (!data.categoryId) return ['categoryId', '请选择商品分类'];
            const price = Number(data.price);
            if (!price || price <= 0) return ['price', '商品价格必须大于 0'];
            return null;
        },
        async onSubmit(data) {
            const payload = {
                name: data.name.trim(),
                categoryId: Number(data.categoryId),
                price: Number(Number(data.price).toFixed(2)),
                description: (data.description || '').trim(),
                image: imageUpload.getUrl(),
                status: 1,
                flavors: []
            };
            await withSubmitting(async () => {
                await API.Dish.addDish(payload);
                await Promise.all([syncProductsData(true), syncDashboardAndStatisticsData(true)]);
                renderApp();
                Toast.show('新增商品成功', 'success');
            }, '新增商品失败');
        }
    });

    imageUpload.init();
}
```

- [ ] **Step 2: 重写 editProduct**

将 `editProduct(id)` 使用 `createFormDialog`：

```javascript
async function editProduct(id) {
    if (isSubmitting) return;
    const detail = products.find((p) => p.id === id);
    if (!detail) return;

    if (dishCategoryOptions.length === 0) {
        Toast.show('分类 ID 不合法', 'error');
        return;
    }

    const categoryOptions = dishCategoryOptions.map((c) =>
        `<option value="${c.id}" ${String(c.id) === String(detail.categoryId) ? 'selected' : ''}>${escapeHtml(c.name)}</option>`
    ).join('');

    const imageUpload = createImageUploadRow({ name: 'image', initialUrl: detail.image || '', label: '商品图片' });

    createFormDialog({
        title: '编辑商品',
        content: `
            <input type="hidden" name="id" value="${detail.id}" />
            <div class="form-row">
                <label class="form-label">商品名称 <span class="required">*</span></label>
                <input type="text" name="name" class="form-input" value="${escapeHtml(detail.name || '')}" />
            </div>
            <div class="form-row">
                <label class="form-label">商品分类 <span class="required">*</span></label>
                <select name="categoryId" class="form-select">
                    <option value="">请选择分类</option>
                    ${categoryOptions}
                </select>
            </div>
            <div class="form-row">
                <label class="form-label">商品价格（元） <span class="required">*</span></label>
                <input type="number" name="price" class="form-input" value="${detail.price || ''}" step="0.01" min="0.01" />
            </div>
            ${imageUpload.html}
            <div class="form-row">
                <label class="form-label">商品描述</label>
                <textarea name="description" class="form-textarea">${escapeHtml(detail.description || '')}</textarea>
            </div>
            <input type="hidden" name="status" value="${detail.status || 1}" />
        `,
        validate(data) {
            if (!data.name?.trim()) return ['name', '商品名称不能为空'];
            if (!data.categoryId) return ['categoryId', '请选择商品分类'];
            const price = Number(data.price);
            if (!price || price <= 0) return ['price', '商品价格必须大于 0'];
            return null;
        },
        async onSubmit(data) {
            const payload = {
                id: Number(data.id),
                name: data.name.trim(),
                categoryId: Number(data.categoryId),
                price: Number(Number(data.price).toFixed(2)),
                description: (data.description || '').trim(),
                image: imageUpload.getUrl(),
                status: Number(data.status),
                flavors: detail.flavors || []
            };
            await withSubmitting(async () => {
                await API.Dish.updateDish(payload);
                await Promise.all([syncProductsData(true), syncDashboardAndStatisticsData(true)]);
                renderApp();
                Toast.show('商品更新成功', 'success');
            }, '更新商品失败');
        }
    });

    imageUpload.init();
}
```

- [ ] **Step 3: 验证语法并测试**

运行 `node --check` 确认无语法错误。

---

### Task 4: 员工表单替换 prompt()

**Files:**
- Modify: `core/nginx/html/sky/merchant-admin/assets/js/app.js`

- [ ] **Step 1: 重写 openAddEmployeeDialog**

```javascript
function openAddEmployeeDialog() {
    if (!canManageEmployees()) {
        Toast.show('当前账号没有员工管理权限', 'error');
        return;
    }

    createFormDialog({
        title: '新增员工',
        content: `
            <div class="form-row">
                <label class="form-label">员工姓名 <span class="required">*</span></label>
                <input type="text" name="name" class="form-input" placeholder="请输入员工姓名" />
            </div>
            <div class="form-row">
                <label class="form-label">登录用户名 <span class="required">*</span></label>
                <input type="text" name="username" class="form-input" placeholder="请输入登录用户名" />
            </div>
            <div class="form-row">
                <label class="form-label">手机号码 <span class="required">*</span></label>
                <input type="tel" name="phone" class="form-input" placeholder="请输入手机号" />
            </div>
            <div class="form-row">
                <label class="form-label">身份证号 <span class="required">*</span></label>
                <input type="text" name="idNumber" class="form-input" placeholder="请输入身份证号" />
            </div>
            <div class="form-row">
                <label class="form-label">员工性别 <span class="required">*</span></label>
                <div class="radio-group">
                    <label class="radio-item"><input type="radio" name="sex" value="1" checked /> 男</label>
                    <label class="radio-item"><input type="radio" name="sex" value="2" /> 女</label>
                </div>
            </div>
            <div class="form-row">
                <label class="form-label">账号角色 <span class="required">*</span></label>
                <div class="radio-group">
                    <label class="radio-item"><input type="radio" name="accountType" value="${ACCOUNT_TYPES.MERCHANT_ADMIN}" checked /> 商家管理员</label>
                    <label class="radio-item"><input type="radio" name="accountType" value="${ACCOUNT_TYPES.MERCHANT_STAFF}" /> 商家员工</label>
                </div>
            </div>
            <input type="hidden" name="status" value="1" />
        `,
        validate(data) {
            if (!data.name?.trim()) return ['name', '员工姓名不能为空'];
            if (!data.username?.trim()) return ['username', '登录用户名不能为空'];
            if (!data.phone?.trim()) return ['phone', '手机号码不能为空'];
            if (!data.idNumber?.trim()) return ['idNumber', '身份证号不能为空'];
            return null;
        },
        async onSubmit(data) {
            const payload = {
                name: data.name.trim(),
                username: data.username.trim(),
                phone: data.phone.trim(),
                sex: data.sex,
                idNumber: data.idNumber.trim(),
                status: 1,
                accountType: Number(data.accountType)
            };
            await withSubmitting(async () => {
                await API.Employee.addEmployee(payload);
                await syncEmployeesData(true);
                renderApp();
                Toast.show('新增员工成功，默认密码为 123456', 'success');
            }, '新增员工失败');
        }
    });
}
```

- [ ] **Step 2: 重写 editEmployee**

```javascript
async function editEmployee(id) {
    if (!canManageEmployees()) {
        Toast.show('当前账号没有员工管理权限', 'error');
        return;
    }

    const detail = employees.find((e) => e.id === id);
    if (!detail) return;

    createFormDialog({
        title: '编辑员工',
        content: `
            <input type="hidden" name="id" value="${detail.id}" />
            <div class="form-row">
                <label class="form-label">员工姓名 <span class="required">*</span></label>
                <input type="text" name="name" class="form-input" value="${escapeHtml(detail.name || '')}" />
            </div>
            <div class="form-row">
                <label class="form-label">登录用户名</label>
                <input type="text" class="form-input" value="${escapeHtml(detail.username || '')}" disabled style="background:#f3f4f6;" />
            </div>
            <div class="form-row">
                <label class="form-label">手机号码 <span class="required">*</span></label>
                <input type="tel" name="phone" class="form-input" value="${escapeHtml(detail.phone || '')}" />
            </div>
            <div class="form-row">
                <label class="form-label">身份证号 <span class="required">*</span></label>
                <input type="text" name="idNumber" class="form-input" value="${escapeHtml(detail.idNumber || '')}" />
            </div>
            <div class="form-row">
                <label class="form-label">员工性别 <span class="required">*</span></label>
                <div class="radio-group">
                    <label class="radio-item"><input type="radio" name="sex" value="1" ${String(detail.sex) === '1' ? 'checked' : ''} /> 男</label>
                    <label class="radio-item"><input type="radio" name="sex" value="2" ${String(detail.sex) === '2' ? 'checked' : ''} /> 女</label>
                </div>
            </div>
            <div class="form-row">
                <label class="form-label">账号角色 <span class="required">*</span></label>
                <div class="radio-group">
                    <label class="radio-item"><input type="radio" name="accountType" value="${ACCOUNT_TYPES.MERCHANT_ADMIN}" ${detail.accountType === ACCOUNT_TYPES.MERCHANT_ADMIN ? 'checked' : ''} /> 商家管理员</label>
                    <label class="radio-item"><input type="radio" name="accountType" value="${ACCOUNT_TYPES.MERCHANT_STAFF}" ${detail.accountType === ACCOUNT_TYPES.MERCHANT_STAFF ? 'checked' : ''} /> 商家员工</label>
                </div>
            </div>
        `,
        validate(data) {
            if (!data.name?.trim()) return ['name', '员工姓名不能为空'];
            if (!data.phone?.trim()) return ['phone', '手机号码不能为空'];
            if (!data.idNumber?.trim()) return ['idNumber', '身份证号不能为空'];
            return null;
        },
        async onSubmit(data) {
            const payload = {
                id: Number(data.id),
                name: data.name.trim(),
                username: detail.username,
                phone: data.phone.trim(),
                sex: data.sex,
                idNumber: data.idNumber.trim(),
                accountType: Number(data.accountType)
            };
            await withSubmitting(async () => {
                await API.Employee.updateEmployee(payload);
                await syncEmployeesData(true);
                renderApp();
                Toast.show('员工信息已更新', 'success');
            }, '更新员工失败');
        }
    });
}
```

- [ ] **Step 3: 验证语法**

---

### Task 5: 套餐表单替换 prompt()

**Files:**
- Modify: `core/nginx/html/sky/merchant-admin/assets/js/app.js`

- [ ] **Step 1: 重写 openAddSetmealDialog**

```javascript
function openAddSetmealDialog() {
    if (setmealCategoryOptions.length === 0) {
        Toast.show('请先在后端创建套餐分类', 'error');
        return;
    }
    if (!products || products.length === 0) {
        Toast.show('请先为当前商家创建菜品，再配置套餐', 'error');
        return;
    }

    const categoryOptions = setmealCategoryOptions.map((c) =>
        `<option value="${c.id}">${escapeHtml(c.name)}</option>`
    ).join('');

    const imageUpload = createImageUploadRow({ name: 'image', label: '套餐图片' });
    const comboSelector = createDishComboSelector({ name: 'combo', dishes: products, initialItems: [] });

    createFormDialog({
        title: '新增套餐',
        width: 'lg',
        content: `
            <div class="form-row">
                <label class="form-label">套餐名称 <span class="required">*</span></label>
                <input type="text" name="name" class="form-input" placeholder="请输入套餐名称" />
            </div>
            <div class="form-row">
                <label class="form-label">套餐分类 <span class="required">*</span></label>
                <select name="categoryId" class="form-select">
                    <option value="">请选择分类</option>
                    ${categoryOptions}
                </select>
            </div>
            <div class="form-row">
                <label class="form-label">套餐价格（元） <span class="required">*</span></label>
                <input type="number" name="price" class="form-input" placeholder="请输入价格" step="0.01" min="0.01" />
            </div>
            ${imageUpload.html}
            ${comboSelector.html}
            <div class="form-row">
                <label class="form-label">套餐描述</label>
                <textarea name="description" class="form-textarea" placeholder="请输入套餐描述（可选）"></textarea>
            </div>
            <input type="hidden" name="status" value="1" />
        `,
        validate(data, form) {
            if (!data.name?.trim()) return ['name', '套餐名称不能为空'];
            if (!data.categoryId) return ['categoryId', '请选择套餐分类'];
            const price = Number(data.price);
            if (!price || price <= 0) return ['price', '套餐价格必须大于 0'];
            const comboError = comboSelector.validate();
            if (comboError) return ['combo', comboError];
            return null;
        },
        async onSubmit(data) {
            const setmealDishes = comboSelector.getValue().map((item) => ({
                dishId: item.dishId,
                copies: item.quantity
            }));
            const payload = {
                name: data.name.trim(),
                categoryId: Number(data.categoryId),
                price: Number(Number(data.price).toFixed(2)),
                description: (data.description || '').trim(),
                image: imageUpload.getUrl(),
                status: 1,
                setmealDishes
            };
            await withSubmitting(async () => {
                await API.Setmeal.addSetmeal(payload);
                await Promise.all([syncSetMealsData(true), syncDashboardAndStatisticsData(true)]);
                renderApp();
                Toast.show('新增套餐成功', 'success');
            }, '新增套餐失败');
        }
    });

    imageUpload.init();
    comboSelector.init();
}
```

- [ ] **Step 2: 重写 editSetmeal**

```javascript
async function editSetmeal(id) {
    if (isSubmitting) return;
    const detail = setmeals.find((s) => s.id === id);
    if (!detail) return;

    if (setmealCategoryOptions.length === 0) {
        Toast.show('套餐分类 ID 不合法', 'error');
        return;
    }

    const categoryOptions = setmealCategoryOptions.map((c) =>
        `<option value="${c.id}" ${String(c.id) === String(detail.categoryId) ? 'selected' : ''}>${escapeHtml(c.name)}</option>`
    ).join('');

    const imageUpload = createImageUploadRow({ name: 'image', initialUrl: detail.image || '', label: '套餐图片' });

    const initialCombo = (detail.setmealDishes || []).map((d) => ({
        dishId: d.dishId,
        quantity: d.copies || 1
    }));
    const comboSelector = createDishComboSelector({ name: 'combo', dishes: products, initialItems: initialCombo });

    createFormDialog({
        title: '编辑套餐',
        width: 'lg',
        content: `
            <input type="hidden" name="id" value="${detail.id}" />
            <div class="form-row">
                <label class="form-label">套餐名称 <span class="required">*</span></label>
                <input type="text" name="name" class="form-input" value="${escapeHtml(detail.name || '')}" />
            </div>
            <div class="form-row">
                <label class="form-label">套餐分类 <span class="required">*</span></label>
                <select name="categoryId" class="form-select">
                    <option value="">请选择分类</option>
                    ${categoryOptions}
                </select>
            </div>
            <div class="form-row">
                <label class="form-label">套餐价格（元） <span class="required">*</span></label>
                <input type="number" name="price" class="form-input" value="${detail.price || ''}" step="0.01" min="0.01" />
            </div>
            ${imageUpload.html}
            ${comboSelector.html}
            <div class="form-row">
                <label class="form-label">套餐描述</label>
                <textarea name="description" class="form-textarea">${escapeHtml(detail.description || '')}</textarea>
            </div>
            <input type="hidden" name="status" value="${detail.status || 1}" />
        `,
        validate(data) {
            if (!data.name?.trim()) return ['name', '套餐名称不能为空'];
            if (!data.categoryId) return ['categoryId', '请选择套餐分类'];
            const price = Number(data.price);
            if (!price || price <= 0) return ['price', '套餐价格必须大于 0'];
            const comboError = comboSelector.validate();
            if (comboError) return ['combo', comboError];
            return null;
        },
        async onSubmit(data) {
            const setmealDishes = comboSelector.getValue().map((item) => ({
                dishId: item.dishId,
                copies: item.quantity
            }));
            const payload = {
                id: Number(data.id),
                name: data.name.trim(),
                categoryId: Number(data.categoryId),
                price: Number(Number(data.price).toFixed(2)),
                description: (data.description || '').trim(),
                image: imageUpload.getUrl(),
                status: Number(data.status),
                setmealDishes
            };
            await withSubmitting(async () => {
                await API.Setmeal.updateSetmeal(payload);
                await Promise.all([syncSetMealsData(true), syncDashboardAndStatisticsData(true)]);
                renderApp();
                Toast.show('套餐更新成功', 'success');
            }, '更新套餐失败');
        }
    });

    imageUpload.init();
    comboSelector.init();
}
```

- [ ] **Step 3: 验证语法**

---

### Task 6: 搜索筛选局部更新

**Files:**
- Modify: `core/nginx/html/sky/merchant-admin/assets/js/app.js`

- [ ] **Step 1: 修改 renderPlatformMerchants 和 renderPlatformMerchantTable**

将 `renderPlatformMerchants()` 中的表格区域添加一个 tbody ID，以便局部更新：

在 `renderPlatformMerchantTable()` 返回值的 `<tbody>` 标签上添加 `id="merchantTableBody"`：

找到 `renderPlatformMerchantTable()` 函数（约第 1118 行），将其返回的 HTML 中的 `<tbody>` 改为 `<tbody id="merchantTableBody">`。

- [ ] **Step 2: 重写 filterPlatformMerchants**

```javascript
function filterPlatformMerchants() {
    const tbody = document.getElementById('merchantTableBody');
    if (!tbody) return;

    const searchText = (document.getElementById('merchantSearch')?.value || '').trim().toLowerCase();
    const statusValue = document.getElementById('merchantStatusFilter')?.value || '';
    const businessValue = document.getElementById('merchantBusinessFilter')?.value || '';

    const filtered = platformMerchants.filter((m) => {
        const matchSearch = !searchText || (m.name || '').toLowerCase().includes(searchText);
        const matchStatus = !statusValue || String(m.status) === statusValue;
        const matchBusiness = !businessValue || String(m.businessStatus) === businessValue;
        return matchSearch && matchStatus && matchBusiness;
    });

    if (filtered.length === 0) {
        tbody.innerHTML = `<tr><td colspan="8" class="py-8 text-center" style="color:var(--text-secondary)">没有找到商家</td></tr>`;
        return;
    }

    tbody.innerHTML = filtered.map((m) => `
        <tr style="border-bottom: 1px solid #f3f4f6;">
            <td class="py-3 px-2">${m.id}</td>
            <td class="py-3 px-2 font-medium">${escapeHtml(m.name)}</td>
            <td class="py-3 px-2">${escapeHtml(m.contactPerson || '')}</td>
            <td class="py-3 px-2">${escapeHtml(m.contactPhone || '')}</td>
            <td class="py-3 px-2">${renderBadge(m.status === 1 ? '正常' : '已禁用', m.status === 1 ? 'status-online' : 'status-offline')}</td>
            <td class="py-3 px-2">${renderBadge(m.businessStatus === 1 ? '营业中' : '已打烊', m.businessStatus === 1 ? 'status-online' : 'status-pending')}</td>
            <td class="py-3 px-2" style="color: var(--text-secondary);">${m.createTime || ''}</td>
            <td class="py-3 px-2">
                <div class="flex gap-2">
                    <button class="text-xs px-2 py-1 rounded border" style="border-color: var(--primary-green); color: var(--primary-green);" onclick="toggleMerchantStatus(${m.id}, ${m.status})" data-submit-btn>${m.status === 1 ? '禁用' : '启用'}</button>
                    <button class="text-xs px-2 py-1 rounded border" style="border-color: var(--primary-green); color: var(--primary-green);" onclick="toggleMerchantBusinessStatus(${m.id}, ${m.businessStatus})" data-submit-btn>${m.businessStatus === 1 ? '打烊' : '营业'}</button>
                </div>
            </td>
        </tr>
    `).join('');
}
```

- [ ] **Step 3: 修改 renderPlatformMerchantTable 返回 thead + tbody 结构**

同时修改 `renderPlatformMerchantTable()` 不再读取 DOM 筛选值（`filterPlatformMerchants` 已负责筛选和渲染 tbody）：

将 `renderPlatformMerchantTable()` 改为只返回表头和一个空 tbody，由 `filterPlatformMerchants()` 填充行数据。

```javascript
function renderPlatformMerchantTable() {
    return `<div class="overflow-x-auto"><table class="w-full text-sm"><thead><tr style="border-bottom: 1px solid var(--border-color);"><th class="text-left py-3 px-2" style="color: var(--text-secondary);">ID</th><th class="text-left py-3 px-2" style="color: var(--text-secondary);">商家名称</th><th class="text-left py-3 px-2" style="color: var(--text-secondary);">联系人</th><th class="text-left py-3 px-2" style="color: var(--text-secondary);">联系电话</th><th class="text-left py-3 px-2" style="color: var(--text-secondary);">状态</th><th class="text-left py-3 px-2" style="color: var(--text-secondary);">营业状态</th><th class="text-left py-3 px-2" style="color: var(--text-secondary);">创建时间</th><th class="text-left py-3 px-2" style="color: var(--text-secondary);">操作</th></tr></thead><tbody id="merchantTableBody"><tr><td colspan="8" class="py-8 text-center" style="color:var(--text-secondary)">加载中...</td></tbody></table></div>`;
}
```

并在 `renderPlatformMerchants()` 渲染完成后调用一次 `filterPlatformMerchants()` 来填充数据。在 `renderPlatformMerchants()` 的 return 值末尾添加：

```javascript
// 在 renderPlatformMerchants return 的模板字符串之后、闭合 </div> 之前
// 实际上需要在 renderApp 渲染完成后调用 filterPlatformMerchants
```

更简洁的方式：在 `renderApp()` 中，当 `currentView === 'platformMerchants'` 时，在 render 后调用 `filterPlatformMerchants()`。

找到 `renderApp()` 函数末尾（约第 2370 行附近），在 `currentView === 'platformMerchants'` 的分支中，添加：

```javascript
if (currentView === 'platformMerchants') {
    filterPlatformMerchants();
}
```

---

### Task 7: 清理旧代码和验证

**Files:**
- Modify: `core/nginx/html/sky/merchant-admin/assets/js/app.js`

- [ ] **Step 1: 删除不再需要的辅助函数**

确认以下函数已无任何引用后删除：
- `buildCategoryHint`（如确认无引用）
- `buildDishComboHint`（如确认无引用）
- `formatSetmealComboInput`（如确认无引用）
- `parseSetmealComboInput`（如确认无引用）

**注意：** 先搜索确认这些函数没有其他调用方再删除。

- [ ] **Step 2: 运行 node --check 验证整个文件**

```bash
node --check core/nginx/html/sky/merchant-admin/assets/js/app.js
```

- [ ] **Step 3: 提交代码**

```bash
git add core/nginx/html/sky/merchant-admin/assets/js/app.js
git add core/nginx/html/sky/merchant-admin/index.html
git commit -m "feat: replace prompt() chains with form dialogs, add image upload and dish combo selector"
```

---

### Task 8: gstack 浏览器验证

- [ ] **Step 1: 启动预览**

确认后端在 8080 运行，然后启动前端预览：

```bash
preview_start merchant-admin-local
```

- [ ] **Step 2: 验证商品表单**

1. 登录 merchant1 / 123456
2. 进入"商品管理"
3. 点击"新增商品"
4. 确认弹窗显示完整表单（名称、分类下拉、价格、图片上传、描述）
5. 不填必填项直接提交 → 确认显示红色错误提示
6. 填写正确内容，选择图片上传 → 确认预览显示
7. 提交 → 确认商品列表出现新商品

- [ ] **Step 3: 验证套餐表单**

1. 进入"套餐管理"
2. 点击"新增套餐"
3. 确认弹窗包含菜品选择器（可搜索、可勾选、可输入份数）
4. 勾选 2 个菜品，输入份数 → 确认底部摘要正确显示
5. 提交 → 确认套餐列表出现新套餐

- [ ] **Step 4: 验证员工表单**

1. 进入"员工管理"
2. 点击"新增员工"
3. 确认弹窗包含单选按钮（性别、角色）
4. 提交 → 确认员工列表出现新员工

- [ ] **Step 5: 验证搜索局部更新**

1. 切换到 admin 登录（平台端）
2. 进入"商家管理"
3. 在搜索框输入文字 → 确认页面不闪烁，只有表格内容变化

- [ ] **Step 6: 验证编辑功能**

1. 商品列表点击"编辑"
2. 确认表单预填充了当前值
3. 修改价格后提交 → 确认价格已更新

---

## 回滚方案

如需回滚，执行以下命令恢复 prompt() 版本：

```bash
git checkout c899df4 -- core/nginx/html/sky/merchant-admin/assets/js/app.js
git checkout c899df4 -- core/nginx/html/sky/merchant-admin/index.html
```

## 风险与注意事项

1. **DishComboSelector 的 `dishes` 参数** 必须来自当前商家的 `products` 数组（已通过 merchantId 隔离）。
2. **图片上传** 依赖 `API.Common.upload()`，该接口使用 `Content-Type: multipart/form-data`，不需要 JSON 序列化。
3. **套餐编辑时的 `setmealDishes`** 字段名要和后端 DTO 匹配（后端字段名通常是 `setmealDishes`，每个元素有 `dishId` 和 `copies`）。
4. **表单验证** 中返回 `[fieldId, message]` 的格式，`fieldId` 要匹配表单元素的 `name` 或 `id` 属性。
5. **radio 按钮** 使用 `FormData` 获取时，只会获取选中的那个值。
