// Merchant admin console for the campus multi-merchant backend.

const STORAGE_KEYS = {
    sessionUser: 'merchant_admin_session_user'
};

const ACCOUNT_TYPES = {
    PLATFORM_ADMIN: 1,
    MERCHANT_ADMIN: 2,
    MERCHANT_STAFF: 3
};

const VIEW_SYNC_TTL = 15 * 1000;

const VIEW_TITLES = {
    dashboard: '商家工作台',
    products: '商品管理',
    orders: '订单管理',
    setmeals: '套餐管理',
    statistics: '数据中心',
    employees: '员工管理',
    merchantProfile: '商家信息'
};

const ORDER_STATUS_TO_UI = {
    1: 'pending',
    2: 'pending',
    3: 'preparing',
    4: 'delivering',
    5: 'completed',
    6: 'cancelled'
};

let currentUser = restoreCurrentUser();
let currentView = currentUser && API.Token.exists() ? 'dashboard' : 'login';
let currentOrderStatus = '';
let selectedOrderId = null;
let renderVersion = 0;
let lastSyncErrorAt = 0;
let categoryNameById = new Map();
let dishCategoryOptions = [];
let setmealCategoryOptions = [];
let merchantProfile = null;
let campusServiceStatus = 1;

const viewSyncedAt = {
    context: 0,
    dashboard: 0,
    products: 0,
    orders: 0,
    setmeals: 0,
    statistics: 0,
    employees: 0,
    merchantProfile: 0
};

const icons = {
    dashboard: `<svg class="icon" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M3 12l2-2 7-7 7 7 2 2M5 10v10a1 1 0 001 1h3m6 0h3a1 1 0 001-1V10M9 21v-6a1 1 0 011-1h4a1 1 0 011 1v6"></path></svg>`,
    products: `<svg class="icon" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M20 7l-8-4-8 4m16 0l-8 4m8-4v10l-8 4m0-10L4 7m8 4v10"></path></svg>`,
    orders: `<svg class="icon" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 5H7a2 2 0 00-2 2v12a2 2 0 002 2h10a2 2 0 002-2V7a2 2 0 00-2-2h-2M9 5a2 2 0 002 2h2a2 2 0 002-2M9 5a2 2 0 012-2h2a2 2 0 012 2"></path></svg>`,
    setmeals: `<svg class="icon" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M7 8h10M7 12h10M7 16h6M5 4h14a2 2 0 012 2v12a2 2 0 01-2 2H5a2 2 0 01-2-2V6a2 2 0 012-2z"></path></svg>`,
    statistics: `<svg class="icon" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M11 19V9m4 10V5m4 14v-8M7 19v-4M3 19h18"></path></svg>`,
    employees: `<svg class="icon" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M17 20h5v-1a4 4 0 00-5.356-3.772M17 20H7m10 0v-1c0-1.657-1.343-3-3-3H10c-1.657 0-3 1.343-3 3v1m0 0H2v-1a4 4 0 015.356-3.772M14 7a4 4 0 11-8 0 4 4 0 018 0zm6 3a3 3 0 11-6 0 3 3 0 016 0z"></path></svg>`,
    merchant: `<svg class="icon" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M3 9l1.5-4.5A2 2 0 016.4 3h11.2a2 2 0 011.9 1.5L21 9m-18 0h18m-1 0v10a2 2 0 01-2 2h-3V13H9v8H6a2 2 0 01-2-2V9"></path></svg>`,
    bell: `<svg class="icon" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 17h5l-1.405-1.405A2.032 2.032 0 0118 14.158V11a6.002 6.002 0 00-4-5.659V5a2 2 0 10-4 0v.341C7.67 6.165 6 8.388 6 11v3.159c0 .538-.214 1.055-.595 1.436L4 17h5m6 0v1a3 3 0 11-6 0v-1m6 0H9"></path></svg>`,
    plus: `<svg class="icon" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 4v16m8-8H4"></path></svg>`,
    edit: `<svg class="icon" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M11 5H6a2 2 0 00-2 2v11a2 2 0 002 2h11a2 2 0 002-2v-5m-1-9l2 2m0 0L12 15l-4 1 1-4 8-8z"></path></svg>`,
    delete: `<svg class="icon" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 7L5 7m2 0l1 12a2 2 0 002 2h4a2 2 0 002-2l1-12m-8 0V5a1 1 0 011-1h4a1 1 0 011 1v2"></path></svg>`,
    eye: `<svg class="icon" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 12a3 3 0 11-6 0 3 3 0 016 0z"></path><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M2.458 12C3.732 7.943 7.523 5 12 5c4.478 0 8.268 2.943 9.542 7-1.274 4.057-5.064 7-9.542 7-4.477 0-8.268-2.943-9.542-7z"></path></svg>`,
    clock: `<svg class="icon" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z"></path></svg>`,
    search: `<svg class="icon" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z"></path></svg>`,
    download: `<svg class="icon" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 16v1a3 3 0 003 3h10a3 3 0 003-3v-1m-4-5l-4 4m0 0l-4-4m4 4V4"></path></svg>`,
    arrowRight: `<svg class="icon" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 5l7 7-7 7"></path></svg>`
};

function restoreCurrentUser() {
    try {
        const raw = localStorage.getItem(STORAGE_KEYS.sessionUser);
        return raw ? JSON.parse(raw) : null;
    } catch (error) {
        console.warn('Failed to restore session user:', error);
        return null;
    }
}

function persistCurrentUser() {
    if (!currentUser) return;
    localStorage.setItem(STORAGE_KEYS.sessionUser, JSON.stringify(currentUser));
}

function clearCurrentUser() {
    currentUser = null;
    localStorage.removeItem(STORAGE_KEYS.sessionUser);
}

function resetSyncState() {
    Object.keys(viewSyncedAt).forEach((key) => {
        viewSyncedAt[key] = 0;
    });
}

function shouldSync(view, force = false) {
    if (force) return true;
    return Date.now() - (viewSyncedAt[view] || 0) > VIEW_SYNC_TTL;
}

function markSynced(view) {
    viewSyncedAt[view] = Date.now();
}

function toNumber(value, fallback = 0) {
    const next = Number(value);
    return Number.isFinite(next) ? next : fallback;
}

function escapeHtml(value) {
    return String(value ?? '')
        .replace(/&/g, '&amp;')
        .replace(/</g, '&lt;')
        .replace(/>/g, '&gt;')
        .replace(/"/g, '&quot;')
        .replace(/'/g, '&#39;');
}

function formatCurrency(amount) {
    return `¥${toNumber(amount).toFixed(2)}`;
}

function formatDateTime(value) {
    if (!value) return '暂无时间';
    const date = new Date(value);
    if (Number.isNaN(date.getTime())) return String(value);
    return date.toLocaleString('zh-CN', {
        year: 'numeric',
        month: '2-digit',
        day: '2-digit',
        hour: '2-digit',
        minute: '2-digit'
    });
}

function formatRelativeTime(value) {
    if (!value) return '刚刚';
    const date = new Date(value);
    if (Number.isNaN(date.getTime())) return String(value);
    const diffMinutes = Math.floor((Date.now() - date.getTime()) / 60000);
    if (diffMinutes < 1) return '刚刚';
    if (diffMinutes < 60) return `${diffMinutes} 分钟前`;
    const diffHours = Math.floor(diffMinutes / 60);
    if (diffHours < 24) return `${diffHours} 小时前`;
    return formatDateTime(value);
}

function formatBusinessHours(begin, end) {
    if (!begin && !end) return '平台未配置';
    return `${begin || '--:--'} - ${end || '--:--'}`;
}

function humanDayLabel(dateText) {
    if (!dateText) return '';
    const parts = String(dateText).split('-');
    if (parts.length >= 3) {
        return `${parts[1]}/${parts[2]}`;
    }
    return dateText;
}

function maskPhone(phone) {
    const value = String(phone || '');
    if (value.length < 7) return value || '--';
    return `${value.slice(0, 3)}****${value.slice(-4)}`;
}

function normalizeImageUrl(url) {
    if (!url) return './download/product_fried_rice.jpg';
    if (/^https?:\/\//i.test(url)) return url;
    if (url.startsWith('/')) return API.config.resolveAssetUrl(url);
    return url;
}

function mapOrderStatusToUI(status) {
    return ORDER_STATUS_TO_UI[toNumber(status, 2)] || 'pending';
}

function getOrderStatusMeta(status) {
    const map = {
        pending: { text: '待接单', className: 'status-pending' },
        preparing: { text: '备餐中', className: 'status-online' },
        delivering: { text: '配送中', className: 'status-online' },
        completed: { text: '已完成', className: 'status-offline' },
        cancelled: { text: '已取消', className: 'status-offline' }
    };
    return map[status] || { text: status, className: 'status-offline' };
}

function getCampusStatusMeta(status) {
    return toNumber(status, 1) === 1
        ? { text: '校园配送已开启', className: 'status-online' }
        : { text: '校园配送已暂停', className: 'status-offline' };
}

function getBusinessStatusMeta(status) {
    return toNumber(status, 1) === 1
        ? { text: '商家营业中', className: 'status-online' }
        : { text: '商家已打烊', className: 'status-pending' };
}

function getPlatformStatusMeta(status) {
    return toNumber(status, 1) === 1
        ? { text: '平台已启用', className: 'status-online' }
        : { text: '平台已停用', className: 'status-offline' };
}

function getAccountTypeLabel(accountType) {
    if (toNumber(accountType) === ACCOUNT_TYPES.MERCHANT_ADMIN) return '商家管理员';
    if (toNumber(accountType) === ACCOUNT_TYPES.MERCHANT_STAFF) return '商家员工';
    return '平台管理员';
}

function isMerchantAdmin() {
    return toNumber(currentUser?.accountType) === ACCOUNT_TYPES.MERCHANT_ADMIN;
}

function canManageEmployees() {
    return isMerchantAdmin();
}

function getMerchantDisplayName() {
    if (merchantProfile?.name) return merchantProfile.name;
    if (currentUser?.merchantId) return `商户 #${currentUser.merchantId}`;
    return '当前商家';
}

function getCampusDisplayName() {
    return merchantProfile?.campusName || '杏林校园';
}

function getMerchantInitial() {
    return escapeHtml(getMerchantDisplayName().slice(0, 1) || '店');
}

function parseCSVStrings(text) {
    if (!text) return [];
    return String(text)
        .split(',')
        .map((item) => item.trim())
        .filter(Boolean);
}

function parseCSVNumbers(text, minLength = 0) {
    const values = parseCSVStrings(text).map((item) => toNumber(item, 0));
    if (minLength > values.length) {
        return values.concat(Array(minLength - values.length).fill(0));
    }
    return values;
}

async function fetchPagedRecords(fetchPage, extraParams = {}, pageSize = 20, maxPages = 20) {
    const first = await fetchPage({ page: 1, pageSize, ...extraParams });
    const records = [...(first?.records || [])];
    const total = toNumber(first?.total, 0);
    const pageCount = Math.min(Math.ceil(total / pageSize), maxPages);

    for (let page = 2; page <= pageCount; page += 1) {
        const data = await fetchPage({ page, pageSize, ...extraParams });
        records.push(...(data?.records || []));
    }

    return records;
}

async function syncMerchantContext(force = false) {
    if (!currentUser || !currentUser.merchantId || !API.Token.exists()) return;
    if (!shouldSync('context', force) && merchantProfile) return;

    const [merchant, campusStatus] = await Promise.all([
        API.Merchant.getById(currentUser.merchantId),
        API.Shop.getStatus().catch(() => merchantProfile?.campusStatus ?? 1)
    ]);

    merchantProfile = merchant;
    campusServiceStatus = toNumber(campusStatus, merchant?.campusStatus ?? 1);
    markSynced('context');
    markSynced('merchantProfile');
}

async function syncCategoriesData(force = false) {
    if (!shouldSync('products', force) && dishCategoryOptions.length > 0 && setmealCategoryOptions.length > 0) {
        return;
    }

    const [dishCategories, mealCategories] = await Promise.all([
        API.Category.getCategoryByType(1).catch(() => []),
        API.Category.getCategoryByType(2).catch(() => [])
    ]);

    dishCategoryOptions = Array.isArray(dishCategories) ? dishCategories : [];
    setmealCategoryOptions = Array.isArray(mealCategories) ? mealCategories : [];
    categoryNameById = new Map([...dishCategoryOptions, ...setmealCategoryOptions].map((item) => [toNumber(item.id), item.name]));
    categories.splice(0, categories.length, ...dishCategoryOptions.map((item) => ({
        id: item.id,
        name: item.name,
        icon: '•'
    })));
}

async function syncProductsData(force = false) {
    if (!shouldSync('products', force)) return;
    await syncCategoriesData(force);

    const records = await fetchPagedRecords((params) => API.Dish.getDishList(params), {}, 40, 20);
    const mapped = (records || []).map((item) => ({
        id: item.id,
        name: item.name || '未命名商品',
        category: item.categoryName || categoryNameById.get(toNumber(item.categoryId)) || '未分类',
        categoryId: toNumber(item.categoryId),
        price: toNumber(item.price),
        image: normalizeImageUrl(item.image),
        description: item.description || '暂无商品描述',
        status: toNumber(item.status, 0),
        sales: 0,
        stock: '--'
    }));

    products.splice(0, products.length, ...mapped);
    markSynced('products');
}

function parseOrderDishes(orderDishesText) {
    if (!orderDishesText) return [];
    return String(orderDishesText)
        .split(';')
        .map((item) => item.trim())
        .filter(Boolean)
        .map((item, index) => {
            const [name, quantity] = item.split('*');
            return {
                id: `fallback-${index}`,
                name: (name || '').trim() || '菜品',
                quantity: toNumber(quantity, 1),
                price: 0,
                image: './download/product_fried_rice.jpg'
            };
        });
}

function resolveGoodsAmount(totalAmount, goodsAmount, deliveryFee, packAmount) {
    if (goodsAmount > 0) return goodsAmount;
    return Math.max(totalAmount - deliveryFee - packAmount, 0);
}

function buildOrderModel(order, detail) {
    const detailItems = detail?.orderDetailList || [];
    const items = detailItems.length > 0
        ? detailItems.map((item, index) => ({
            id: item.id || `${order.id}-${index}`,
            name: item.name || '菜品',
            quantity: toNumber(item.number, 1),
            price: toNumber(item.amount),
            image: normalizeImageUrl(item.image)
        }))
        : parseOrderDishes(order.orderDishes);

    const totalAmount = toNumber(detail?.amount ?? order.amount, 0);
    const deliveryFee = toNumber(detail?.deliveryFee ?? order.deliveryFee, 0);
    const packAmount = toNumber(detail?.packAmount ?? order.packAmount, 0);
    const itemCount = toNumber(detail?.itemCount ?? order.itemCount, items.reduce((sum, item) => sum + toNumber(item.quantity, 0), 0));
    const goodsAmount = resolveGoodsAmount(
        totalAmount,
        toNumber(detail?.goodsAmount ?? order.goodsAmount, 0),
        deliveryFee,
        packAmount
    );

    return {
        id: String(order.number || order.id),
        orderId: order.id,
        orderNumber: String(order.number || order.id),
        customer: order.consignee || order.userName || detail?.consignee || '匿名用户',
        rawPhone: detail?.phone || order.phone || '',
        phone: maskPhone(detail?.phone || order.phone || ''),
        address: detail?.address || order.address || '暂无地址信息',
        items,
        totalAmount,
        goodsAmount,
        deliveryFee,
        packAmount,
        itemCount,
        status: mapOrderStatusToUI(detail?.status ?? order.status),
        rawStatus: toNumber(detail?.status ?? order.status, 0),
        orderTime: detail?.orderTime || order.orderTime || '',
        checkoutTime: detail?.checkoutTime || order.checkoutTime || '',
        note: detail?.remark || order.remark || '',
        merchantName: detail?.merchantName || order.merchantName || detail?.shopName || getMerchantDisplayName(),
        merchantId: toNumber(detail?.merchantId ?? order.merchantId, currentUser?.merchantId || 0),
        campusId: toNumber(detail?.campusId ?? order.campusId, merchantProfile?.campusId || 0),
        payStatus: toNumber(detail?.payStatus ?? order.payStatus, 0),
        payMethod: toNumber(detail?.payMethod ?? order.payMethod, 0),
        cancelReason: detail?.cancelReason || '',
        rejectionReason: detail?.rejectionReason || '',
        courierTelephone: detail?.courierTelephone || '',
        servicePhone: detail?.shopTelephone || merchantProfile?.servicePhone || '',
        detail
    };
}

async function syncOrdersData(force = false) {
    if (!shouldSync('orders', force)) return;

    const records = await fetchPagedRecords((params) => API.Order.getOrderList(params), {}, 20, 15);
    const details = await Promise.all((records || []).slice(0, 20).map(async (item) => {
        try {
            return [item.id, await API.Order.getOrderDetail(item.id)];
        } catch (error) {
            return [item.id, null];
        }
    }));
    const detailMap = new Map(details);
    const mapped = (records || []).map((item) => buildOrderModel(item, detailMap.get(item.id)));

    orders.splice(0, orders.length, ...mapped);
    markSynced('orders');
}

async function hydrateOrderDetail(orderId) {
    const target = orders.find((item) => item.orderId === orderId);
    if (!target) return null;

    try {
        const detail = await API.Order.getOrderDetail(orderId);
        const merged = buildOrderModel({ ...target, id: orderId, number: target.orderNumber }, detail);
        Object.assign(target, merged);
        return target;
    } catch (error) {
        return target;
    }
}

async function syncSetMealsData(force = false) {
    if (!shouldSync('setmeals', force)) return;
    await syncCategoriesData(force);

    const records = await fetchPagedRecords((params) => API.Setmeal.getSetmealList(params), {}, 20, 10);
    const details = await Promise.all((records || []).slice(0, 20).map(async (item) => {
        try {
            return [item.id, await API.Setmeal.getSetmealById(item.id)];
        } catch (error) {
            return [item.id, null];
        }
    }));
    const detailMap = new Map(details);

    const mapped = (records || []).map((item) => {
        const detail = detailMap.get(item.id);
        const dishes = (detail?.setmealDishes || []).map((dish, index) => ({
            id: dish.id || `${item.id}-${index}`,
            dishId: dish.dishId,
            name: dish.name || '菜品',
            quantity: toNumber(dish.copies, 1)
        }));

        return {
            id: item.id,
            name: item.name || '未命名套餐',
            categoryId: toNumber(item.categoryId, 0),
            categoryName: item.categoryName || categoryNameById.get(toNumber(item.categoryId)) || '套餐分类',
            price: toNumber(item.price),
            originalPrice: toNumber(item.price),
            image: normalizeImageUrl(item.image),
            description: item.description || '暂无套餐描述',
            status: toNumber(item.status, 0),
            products: dishes,
            sales: 0
        };
    });

    setMeals.splice(0, setMeals.length, ...mapped);
    markSynced('setmeals');
}

async function syncEmployeesData(force = false) {
    if (!shouldSync('employees', force)) return;

    const records = await fetchPagedRecords((params) => API.Employee.getEmployeeList(params), {}, 20, 10);
    const mapped = (records || []).map((item) => ({
        id: item.id,
        username: item.username || '--',
        name: item.name || item.username || '员工',
        phone: maskPhone(item.phone),
        rawPhone: item.phone || '',
        accountType: toNumber(item.accountType, ACCOUNT_TYPES.MERCHANT_STAFF),
        role: getAccountTypeLabel(item.accountType),
        sex: item.sex || '1',
        idNumber: item.idNumber || '',
        status: toNumber(item.status, 0),
        createTime: String(item.createTime || '').split(' ')[0] || '--'
    }));

    employees.splice(0, employees.length, ...mapped);
    markSynced('employees');
}

function rebuildCategoryStatisticsFromProducts() {
    if (!products.length) return;
    const counter = new Map();

    products.forEach((item) => {
        const key = item.category || '未分类';
        counter.set(key, (counter.get(key) || 0) + 1);
    });

    const labels = [...counter.keys()];
    const total = [...counter.values()].reduce((sum, value) => sum + value, 0) || 1;
    const values = [...counter.values()].map((value) => Number(((value / total) * 100).toFixed(1)));
    const colors = ['#ff7a21', '#ffb347', '#11a75c', '#3b82f6', '#8b5cf6', '#14b8a6', '#0ea5e9', '#f43f5e'];

    statistics.categoryData.labels = labels;
    statistics.categoryData.values = values;
    statistics.categoryData.colors = labels.map((_, index) => colors[index % colors.length]);
}

async function syncDashboardAndStatisticsData(force = false) {
    if (!shouldSync('dashboard', force) && !shouldSync('statistics', force)) return;

    const end = new Date();
    const begin = new Date(end.getTime() - 6 * 24 * 60 * 60 * 1000);
    const beginText = begin.toISOString().slice(0, 10);
    const endText = end.toISOString().slice(0, 10);

    const [
        businessData,
        orderOverview,
        dishOverview,
        setmealOverview,
        orderStats,
        turnoverReport,
        orderReport,
        top10Report
    ] = await Promise.all([
        API.Workspace.getTodayData(),
        API.Workspace.getOrderOverview(),
        API.Workspace.getDishOverview(),
        API.Workspace.getSetmealOverview().catch(() => ({ sold: 0, discontinued: 0 })),
        API.Order.getOrderStatistics(),
        API.Report.getTurnoverStatistics(beginText, endText),
        API.Report.getOrderStatistics(beginText, endText),
        API.Report.getTop10(beginText, endText)
    ]);

    statistics.today.orders = toNumber(orderOverview?.allOrders, orders.length);
    statistics.today.revenue = toNumber(businessData?.turnover, 0);
    statistics.today.pendingOrders = toNumber(orderStats?.toBeConfirmed ?? orderOverview?.waitingOrders, 0);
    statistics.today.totalProducts = toNumber(dishOverview?.sold, 0) + toNumber(dishOverview?.discontinued, 0);
    statistics.today.totalSetmeals = toNumber(setmealOverview?.sold, 0) + toNumber(setmealOverview?.discontinued, 0);
    statistics.today.newUsers = toNumber(businessData?.newUsers, 0);

    statistics.trend.ordersChange = `完成率 ${(toNumber(businessData?.orderCompletionRate, 0)).toFixed(1)}%`;
    statistics.trend.revenueChange = `客单价 ${formatCurrency(businessData?.unitPrice || 0)}`;
    statistics.trend.pendingChange = `${statistics.today.pendingOrders} 单待接单`;
    statistics.trend.productsChange = `在售 ${toNumber(dishOverview?.sold, 0)} 个商品`;

    const reportDates = parseCSVStrings(turnoverReport?.dateList);
    const labels = reportDates.length > 0 ? reportDates.map(humanDayLabel) : statistics.weekData.labels;
    const revenueList = parseCSVNumbers(turnoverReport?.turnoverList, labels.length);
    const orderList = parseCSVNumbers(orderReport?.orderCountList, labels.length);

    statistics.weekData.labels = labels;
    statistics.weekData.revenue = revenueList;
    statistics.weekData.orders = orderList;

    const topNames = parseCSVStrings(top10Report?.nameList);
    const topNumbers = parseCSVNumbers(top10Report?.numberList, topNames.length);
    if (topNames.length > 0) {
        const maxSales = Math.max(...topNumbers, 1);
        statistics.topProducts = topNames.map((name, index) => ({
            name,
            sales: topNumbers[index] || 0,
            percentage: Math.round(((topNumbers[index] || 0) / maxSales) * 100)
        }));
    } else {
        statistics.topProducts = [];
    }

    rebuildCategoryStatisticsFromProducts();
    markSynced('dashboard');
    markSynced('statistics');
}

async function syncDataForView(view, force = false) {
    if (view !== 'login') {
        await syncMerchantContext(force);
    }

    switch (view) {
        case 'dashboard':
            await syncProductsData(force);
            await Promise.all([syncOrdersData(force), syncDashboardAndStatisticsData(force)]);
            return;
        case 'products':
            await Promise.all([syncCategoriesData(force), syncProductsData(force)]);
            return;
        case 'orders':
            await syncOrdersData(force);
            return;
        case 'setmeals':
            await Promise.all([syncCategoriesData(force), syncProductsData(force), syncSetMealsData(force)]);
            return;
        case 'statistics':
            await syncProductsData(force);
            await syncDashboardAndStatisticsData(force);
            return;
        case 'employees':
            await syncEmployeesData(force);
            return;
        case 'merchantProfile':
            await syncMerchantContext(force);
            return;
        default:
            return;
    }
}

function isAuthError(error) {
    const message = String(error?.message || '').toLowerCase();
    return message.includes('login') || message.includes('token') || message.includes('未登录') || message.includes('notlogin');
}

function notifySyncError(error) {
    console.error('数据同步失败:', error);
    if (isAuthError(error)) {
        API.Token.remove();
        clearCurrentUser();
        resetSyncState();
        currentView = 'login';
        renderApp();
        return;
    }

    if (Date.now() - lastSyncErrorAt > 5000) {
        lastSyncErrorAt = Date.now();
        alert(`数据同步失败，将展示上次加载结果：${error.message || error}`);
    }
}

function renderBadge(text, className) {
    return `<span class="status-badge ${className}">${escapeHtml(text)}</span>`;
}

function renderStatusBanner() {
    if (!merchantProfile) return '';

    const campusMeta = getCampusStatusMeta(campusServiceStatus);
    const businessMeta = getBusinessStatusMeta(merchantProfile.businessStatus);
    const platformMeta = getPlatformStatusMeta(merchantProfile.status);

    let title = '当前商家经营正常，可继续接单';
    let description = `${escapeHtml(getCampusDisplayName())} 使用统一配送规则，配送费 ${formatCurrency(merchantProfile.deliveryFee)}，预计 ${escapeHtml(merchantProfile.estimatedDeliveryMinutes || '--')} 分钟送达。`;
    let panelStyle = 'background: linear-gradient(135deg, rgba(17, 167, 92, 0.12), rgba(255,255,255,0.92)); border-color: rgba(17, 167, 92, 0.24);';

    if (toNumber(merchantProfile.status, 1) !== 1) {
        title = '当前商家已被平台停用';
        description = '平台已关闭该商家的经营入口，商家侧仅保留只读查看能力。';
        panelStyle = 'background: linear-gradient(135deg, rgba(229, 72, 77, 0.14), rgba(255,255,255,0.94)); border-color: rgba(229, 72, 77, 0.24);';
    } else if (toNumber(campusServiceStatus, 1) !== 1) {
        title = '校园配送服务已暂停';
        description = '当前是校园统一服务停服，不是单个商家问题，商家无法自行恢复。';
        panelStyle = 'background: linear-gradient(135deg, rgba(245, 158, 11, 0.16), rgba(255,255,255,0.94)); border-color: rgba(245, 158, 11, 0.26);';
    } else if (toNumber(merchantProfile.businessStatus, 1) !== 1) {
        title = '当前商家处于打烊状态';
        description = '用户端会看到该商家暂停营业。商家管理员可以在商家信息页恢复营业。';
        panelStyle = 'background: linear-gradient(135deg, rgba(245, 158, 11, 0.16), rgba(255,255,255,0.94)); border-color: rgba(245, 158, 11, 0.26);';
    }

    return `<div class="mb-6 rounded-2xl p-5 border" style="${panelStyle}">
        <div class="flex flex-col gap-4 lg:flex-row lg:items-start lg:justify-between">
            <div>
                <p class="text-sm font-semibold mb-2" style="color: var(--text-primary);">当前商家：${escapeHtml(getMerchantDisplayName())}</p>
                <h3 class="text-xl font-semibold mb-2" style="color: var(--text-primary);">${escapeHtml(title)}</h3>
                <p class="text-sm leading-6" style="color: var(--text-secondary);">${escapeHtml(description)}</p>
            </div>
            <div class="flex flex-wrap gap-2">${renderBadge(campusMeta.text, campusMeta.className)}${renderBadge(businessMeta.text, businessMeta.className)}${renderBadge(platformMeta.text, platformMeta.className)}</div>
        </div>
    </div>`;
}

function renderPageHeader(title, description, actions = '') {
    return `<div class="flex flex-col gap-4 mb-6 lg:flex-row lg:items-end lg:justify-between">
        <div>
            <h2 class="text-2xl font-semibold mb-2" style="color: var(--text-primary);">${escapeHtml(title)}</h2>
            <p class="text-sm" style="color: var(--text-secondary);">${escapeHtml(description)}</p>
        </div>
        ${actions ? `<div class="flex flex-wrap gap-3">${actions}</div>` : ''}
    </div>`;
}

function renderEmptyState(title, description) {
    return `<div class="bg-white rounded-lg p-12 text-center">
        <div class="mx-auto mb-4 w-14 h-14 rounded-full flex items-center justify-center" style="background: rgba(255,122,33,0.12); color: var(--primary-orange);">${icons.merchant}</div>
        <h3 class="text-lg font-semibold mb-2" style="color: var(--text-primary);">${escapeHtml(title)}</h3>
        <p class="text-sm" style="color: var(--text-secondary);">${escapeHtml(description)}</p>
    </div>`;
}

function renderSidebar() {
    const items = [
        { id: 'dashboard', label: '工作台', icon: 'dashboard' },
        { id: 'products', label: '商品管理', icon: 'products' },
        { id: 'orders', label: '订单管理', icon: 'orders' },
        { id: 'setmeals', label: '套餐管理', icon: 'setmeals' },
        { id: 'statistics', label: '数据中心', icon: 'statistics' },
        { id: 'merchantProfile', label: '商家信息', icon: 'merchant' }
    ];

    if (canManageEmployees()) {
        items.splice(5, 0, { id: 'employees', label: '员工管理', icon: 'employees' });
    }

    return `<div class="sidebar">
        <div class="p-6 border-b border-gray-200">
            <h1 class="text-xl font-bold" style="color: #ffd5b6;">校园外卖商家端</h1>
            <p class="text-sm text-gray-500 mt-1">${escapeHtml(getMerchantDisplayName())}</p>
            <p class="text-xs mt-3" style="color: rgba(222,231,245,0.58);">已绑定当前商户 · ${escapeHtml(getAccountTypeLabel(currentUser?.accountType))}</p>
        </div>
        <nav class="py-4">
            ${items.map((item) => `<div class="sidebar-item ${currentView === item.id ? 'active' : ''}" onclick="navigateTo('${item.id}')">${icons[item.icon]}<span>${item.label}</span></div>`).join('')}
        </nav>
        <div class="px-5 pb-6 pt-2 text-xs leading-6" style="color: rgba(220,230,246,0.62);">
            当前商家所有商品、订单、员工数据都由后端按 <code>merchantId</code> 自动隔离。
        </div>
    </div>`;
}

function renderTopBar() {
    const pendingCount = orders.filter((item) => item.status === 'pending').length;
    const campusMeta = getCampusStatusMeta(campusServiceStatus);
    const businessMeta = getBusinessStatusMeta(merchantProfile?.businessStatus ?? 1);

    return `<div class="top-bar">
        <div>
            <h2 class="text-xl font-semibold" style="color: var(--text-primary);">${escapeHtml(VIEW_TITLES[currentView] || VIEW_TITLES.dashboard)}</h2>
            <p class="text-sm mt-1" style="color: var(--text-secondary);">${escapeHtml(getMerchantDisplayName())} · ${escapeHtml(getCampusDisplayName())}</p>
        </div>
        <div class="flex flex-wrap items-center gap-3 justify-end">
            <div class="px-3 py-2 rounded-xl border text-sm" style="background: rgba(255,255,255,0.72); border-color: rgba(196,208,226,0.64);">
                <span style="color: var(--text-secondary);">当前角色</span>
                <span class="ml-2 font-semibold" style="color: var(--text-primary);">${escapeHtml(getAccountTypeLabel(currentUser?.accountType))}</span>
            </div>
            <div class="px-3 py-2 rounded-xl border text-sm" style="background: rgba(255,255,255,0.72); border-color: rgba(196,208,226,0.64);">${renderBadge(campusMeta.text, campusMeta.className)}</div>
            <div class="px-3 py-2 rounded-xl border text-sm" style="background: rgba(255,255,255,0.72); border-color: rgba(196,208,226,0.64);">${renderBadge(businessMeta.text, businessMeta.className)}</div>
            <button class="relative px-3 py-2 rounded-xl border flex items-center gap-2" style="background: rgba(255,255,255,0.72); border-color: rgba(196,208,226,0.64);" onclick="openPendingOrders()">
                ${icons.bell}
                <span class="text-sm font-medium" style="color: var(--text-primary);">待接单</span>
                ${pendingCount > 0 ? `<span class="absolute -top-1 -right-1 bg-red-500 text-white text-xs rounded-full min-w-[20px] h-5 px-1 flex items-center justify-center">${pendingCount}</span>` : ''}
            </button>
            <button class="flex items-center gap-3 px-3 py-2 rounded-xl border" style="background: rgba(255,255,255,0.8); border-color: rgba(196,208,226,0.64);" onclick="logout()">
                <div class="w-9 h-9 rounded-full flex items-center justify-center text-white font-semibold" style="background: linear-gradient(135deg, var(--primary-orange), var(--secondary-orange));">${escapeHtml((currentUser?.name || '商').slice(0, 1))}</div>
                <div class="text-left">
                    <p class="text-sm font-semibold" style="color: var(--text-primary);">${escapeHtml(currentUser?.name || '商家账号')}</p>
                    <p class="text-xs" style="color: var(--text-secondary);">退出登录</p>
                </div>
            </button>
        </div>
    </div>`;
}

function renderMetricCard(title, value, subtitle, iconHtml, accentStyle) {
    return `<div class="bg-white rounded-lg p-6 card-hover">
        <div class="flex items-center justify-between mb-4">
            <div class="w-12 h-12 rounded-lg flex items-center justify-center text-white" style="${accentStyle}">${iconHtml}</div>
            <span class="text-sm font-medium" style="color: var(--text-secondary);">${escapeHtml(subtitle)}</span>
        </div>
        <p class="text-sm mb-1" style="color: var(--text-secondary);">${escapeHtml(title)}</p>
        <p class="text-3xl font-bold" style="color: var(--text-primary);">${escapeHtml(value)}</p>
    </div>`;
}

function renderDashboard() {
    const pendingOrders = orders.filter((item) => item.status === 'pending');
    const latestOrders = orders.slice(0, 5);

    return `<div class="content-area fade-in">
        ${renderStatusBanner()}
        ${renderPageHeader('商家工作台', '围绕当前商户的订单、商品与经营数据统一运营。')}
        <div class="grid grid-cols-1 md:grid-cols-2 xl:grid-cols-4 gap-6 mb-6">
            ${renderMetricCard('今日订单', String(statistics.today.orders || 0), statistics.trend.ordersChange || '今日概览', icons.orders, 'background: linear-gradient(135deg, #ff7a21, #ffb347);')}
            ${renderMetricCard('今日营业额', formatCurrency(statistics.today.revenue || 0), statistics.trend.revenueChange || '营收变化', `<svg class="icon-lg" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 8c-1.657 0-3 .895-3 2s1.343 2 3 2 3 .895 3 2-1.343 2-3 2m0-8c1.11 0 2.08.402 2.599 1M12 8V7m0 1v8m0 0v1m0-1c-1.11 0-2.08-.402-2.599-1M21 12a9 9 0 11-18 0 9 9 0 0118 0z"></path></svg>`, 'background: linear-gradient(135deg, #11a75c, #48c78e);')}
            ${renderMetricCard('待接单', String(statistics.today.pendingOrders || 0), statistics.trend.pendingChange || '待处理提醒', icons.clock, 'background: linear-gradient(135deg, #f59e0b, #fbbf24);')}
            ${renderMetricCard('在售商品数', String(statistics.today.totalProducts || 0), statistics.trend.productsChange || '菜单规模', icons.products, 'background: linear-gradient(135deg, #3b82f6, #60a5fa);')}
        </div>
        <div class="grid grid-cols-1 xl:grid-cols-3 gap-6 mb-6">
            <div class="xl:col-span-2 bg-white rounded-lg p-6 card-hover">
                <div class="flex items-center justify-between mb-4">
                    <h3 class="text-lg font-semibold" style="color: var(--text-primary);">近 7 日经营趋势</h3>
                    <button class="text-sm font-medium" style="color: var(--primary-orange);" onclick="navigateTo('statistics')">查看数据中心</button>
                </div>
                <div style="height: 300px;"><canvas id="revenueChart"></canvas></div>
            </div>
            <div class="bg-white rounded-lg p-6 card-hover">
                <div class="flex items-center justify-between mb-4">
                    <h3 class="text-lg font-semibold" style="color: var(--text-primary);">商家状态摘要</h3>
                    <button class="text-sm font-medium" style="color: var(--primary-orange);" onclick="navigateTo('merchantProfile')">查看详情</button>
                </div>
                <div class="space-y-4">
                    <div class="p-4 rounded-xl border" style="border-color: rgba(196,208,226,0.64);">
                        <p class="text-sm mb-1" style="color: var(--text-secondary);">商家名称</p>
                        <p class="text-lg font-semibold" style="color: var(--text-primary);">${escapeHtml(getMerchantDisplayName())}</p>
                        <p class="text-xs mt-2" style="color: var(--text-secondary);">商家编码：${escapeHtml(merchantProfile?.merchantCode || '--')}</p>
                    </div>
                    <div class="grid grid-cols-2 gap-3">
                        <div class="p-4 rounded-xl border" style="border-color: rgba(196,208,226,0.64);">
                            <p class="text-sm" style="color: var(--text-secondary);">营业时段</p>
                            <p class="font-semibold mt-1" style="color: var(--text-primary);">${escapeHtml(formatBusinessHours(merchantProfile?.businessBeginTime, merchantProfile?.businessEndTime))}</p>
                        </div>
                        <div class="p-4 rounded-xl border" style="border-color: rgba(196,208,226,0.64);">
                            <p class="text-sm" style="color: var(--text-secondary);">统一配送费</p>
                            <p class="font-semibold mt-1" style="color: var(--text-primary);">${formatCurrency(merchantProfile?.deliveryFee || 0)}</p>
                        </div>
                        <div class="p-4 rounded-xl border" style="border-color: rgba(196,208,226,0.64);">
                            <p class="text-sm" style="color: var(--text-secondary);">预计送达</p>
                            <p class="font-semibold mt-1" style="color: var(--text-primary);">${escapeHtml(merchantProfile?.estimatedDeliveryMinutes || '--')} 分钟</p>
                        </div>
                        <div class="p-4 rounded-xl border" style="border-color: rgba(196,208,226,0.64);">
                            <p class="text-sm" style="color: var(--text-secondary);">今日新增用户</p>
                            <p class="font-semibold mt-1" style="color: var(--text-primary);">${escapeHtml(statistics.today.newUsers || 0)}</p>
                        </div>
                    </div>
                    <div class="flex flex-wrap gap-2">
                        ${renderBadge(getCampusStatusMeta(campusServiceStatus).text, getCampusStatusMeta(campusServiceStatus).className)}
                        ${renderBadge(getBusinessStatusMeta(merchantProfile?.businessStatus).text, getBusinessStatusMeta(merchantProfile?.businessStatus).className)}
                        ${renderBadge(getPlatformStatusMeta(merchantProfile?.status).text, getPlatformStatusMeta(merchantProfile?.status).className)}
                    </div>
                </div>
            </div>
        </div>
        <div class="grid grid-cols-1 xl:grid-cols-3 gap-6">
            <div class="xl:col-span-2 bg-white rounded-lg p-6 card-hover">
                <div class="flex items-center justify-between mb-4">
                    <h3 class="text-lg font-semibold" style="color: var(--text-primary);">最新订单</h3>
                    <button class="text-sm font-medium" style="color: var(--primary-orange);" onclick="navigateTo('orders')">前往订单管理</button>
                </div>
                <div class="space-y-3">
                    ${latestOrders.length === 0 ? renderEmptyState('暂无订单数据', '当前商家还没有产生订单。') : latestOrders.map((order) => {
                        const statusMeta = getOrderStatusMeta(order.status);
                        return `<button class="w-full text-left p-4 rounded-xl border transition hover:bg-gray-50" style="border-color: rgba(196,208,226,0.64);" onclick="openOrderDetail(${order.orderId})">
                            <div class="flex flex-col gap-3 lg:flex-row lg:items-center lg:justify-between">
                                <div>
                                    <p class="font-semibold mb-1" style="color: var(--text-primary);">订单号 ${escapeHtml(order.orderNumber)}</p>
                                    <p class="text-sm" style="color: var(--text-secondary);">${escapeHtml(order.customer)} · ${escapeHtml(formatRelativeTime(order.orderTime))}</p>
                                </div>
                                <div class="flex items-center gap-3">
                                    <span class="font-semibold" style="color: var(--primary-orange);">${formatCurrency(order.totalAmount)}</span>
                                    ${renderBadge(statusMeta.text, statusMeta.className)}
                                </div>
                            </div>
                        </button>`;
                    }).join('')}
                </div>
            </div>
            <div class="bg-white rounded-lg p-6 card-hover">
                <div class="flex items-center justify-between mb-4">
                    <h3 class="text-lg font-semibold" style="color: var(--text-primary);">待处理提醒</h3>
                    <button class="text-sm font-medium" style="color: var(--primary-orange);" onclick="openPendingOrders()">立即处理</button>
                </div>
                ${pendingOrders.length === 0 ? renderEmptyState('当前无待接单', '新订单来了会在这里高亮提醒。') : `<div class="space-y-3">${pendingOrders.slice(0, 4).map((order) => `<div class="p-4 rounded-xl border" style="border-color: rgba(245,158,11,0.22); background: rgba(245,158,11,0.06);">
                    <div class="flex items-center justify-between gap-3 mb-2">
                        <p class="font-semibold" style="color: var(--text-primary);">${escapeHtml(order.customer)}</p>
                        <span class="text-sm font-semibold" style="color: var(--warning);">${formatCurrency(order.totalAmount)}</span>
                    </div>
                    <p class="text-sm mb-2" style="color: var(--text-secondary);">${escapeHtml(order.address)}</p>
                    <button class="text-sm font-medium" style="color: var(--primary-orange);" onclick="openOrderDetail(${order.orderId})">查看订单详情</button>
                </div>`).join('')}</div>`}
            </div>
        </div>
    </div>`;
}

function renderProducts() {
    const actions = `<button onclick="openAddProductDialog()" class="btn-primary px-5 py-2 rounded-lg flex items-center gap-2">${icons.plus}<span>新增商品</span></button>`;
    const grid = products.length > 0 ? renderProductCards(products) : renderEmptyState('当前没有商品', '请先为当前商家创建菜品。');

    return `<div class="content-area fade-in">
        ${renderStatusBanner()}
        ${renderPageHeader('商品管理', '商品默认归属于当前商家，无需前端再选择 merchantId。', actions)}
        <div class="bg-white rounded-lg p-4 mb-6 flex flex-col gap-4 xl:flex-row xl:items-center xl:justify-between">
            <div class="flex flex-col gap-4 xl:flex-row xl:items-center xl:flex-1">
                <div class="relative flex-1 max-w-xl">
                    <input type="text" id="productSearch" placeholder="搜索商品名称或描述" class="w-full pl-10 pr-4 py-3 border border-gray-300 rounded-lg focus:outline-none" oninput="filterProducts()" />
                    <div class="absolute left-3 top-1/2 -translate-y-1/2 text-gray-400">${icons.search}</div>
                </div>
                <select id="categoryFilter" class="px-4 py-3 border border-gray-300 rounded-lg" onchange="filterProducts()">
                    <option value="">全部分类</option>
                    ${dishCategoryOptions.map((item) => `<option value="${item.id}">${escapeHtml(item.name)}</option>`).join('')}
                </select>
                <select id="statusFilter" class="px-4 py-3 border border-gray-300 rounded-lg" onchange="filterProducts()">
                    <option value="">全部状态</option>
                    <option value="1">在售</option>
                    <option value="0">停售</option>
                </select>
            </div>
            <div class="text-sm" style="color: var(--text-secondary);">当前商家：${escapeHtml(getMerchantDisplayName())}</div>
        </div>
        <div id="productsGrid" class="grid grid-cols-1 md:grid-cols-2 xl:grid-cols-3 2xl:grid-cols-4 gap-6">${grid}</div>
    </div>`;
}

function renderProductCards(list) {
    if (!list.length) {
        return renderEmptyState('没有匹配的商品', '试试调整搜索条件或新增商品。');
    }

    return list.map((product) => `<div class="product-card">
        <div class="relative overflow-hidden">
            <img src="${escapeHtml(product.image)}" alt="${escapeHtml(product.name)}" onerror="this.src='./download/product_fried_rice.jpg'" />
            ${product.status === 0 ? `<div class="absolute inset-0 bg-black bg-opacity-45 flex items-center justify-center"><span class="text-white font-bold text-lg">已停售</span></div>` : ''}
        </div>
        <div class="p-4">
            <div class="flex items-start justify-between gap-3 mb-2">
                <div>
                    <h3 class="font-semibold text-lg" style="color: var(--text-primary);">${escapeHtml(product.name)}</h3>
                    <p class="text-sm" style="color: var(--text-secondary);">${escapeHtml(product.category)}</p>
                </div>
                ${renderBadge(product.status === 1 ? '在售' : '停售', product.status === 1 ? 'status-online' : 'status-offline')}
            </div>
            <p class="text-sm min-h-[44px] mb-4" style="color: var(--text-secondary);">${escapeHtml(product.description)}</p>
            <div class="flex items-center justify-between mb-4">
                <span class="text-2xl font-bold" style="color: var(--primary-orange);">${formatCurrency(product.price)}</span>
                <span class="text-sm" style="color: var(--text-secondary);">归属商家：${escapeHtml(getMerchantDisplayName())}</span>
            </div>
            <div class="flex items-center justify-between">
                <label class="switch">
                    <input type="checkbox" ${product.status === 1 ? 'checked' : ''} onchange="toggleProductStatus(${product.id})">
                    <span class="slider"></span>
                </label>
                <div class="flex items-center gap-2">
                    <button class="p-2 hover:bg-gray-100 rounded" style="color: var(--primary-orange);" onclick="editProduct(${product.id})">${icons.edit}</button>
                    <button class="p-2 hover:bg-gray-100 rounded" style="color: var(--accent-red);" onclick="deleteProduct(${product.id})">${icons.delete}</button>
                </div>
            </div>
        </div>
    </div>`).join('');
}

function renderOrders() {
    const tabs = [
        { key: '', label: '全部订单', count: orders.length },
        { key: 'pending', label: '待接单', count: orders.filter((item) => item.status === 'pending').length },
        { key: 'preparing', label: '备餐中', count: orders.filter((item) => item.status === 'preparing').length },
        { key: 'delivering', label: '配送中', count: orders.filter((item) => item.status === 'delivering').length },
        { key: 'completed', label: '已完成', count: orders.filter((item) => item.status === 'completed').length }
    ];

    return `<div class="content-area fade-in">
        ${renderStatusBanner()}
        ${renderPageHeader('订单管理', '订单列表已自动按当前商家隔离，可直接处理待接单和履约状态。')}
        <div class="bg-white rounded-lg p-4 mb-6">
            <div class="flex flex-wrap items-center gap-4 border-b border-gray-200">
                ${tabs.map((tab) => `<button onclick="filterOrdersByStatus('${tab.key}')" class="pb-4 px-2 font-medium transition ${currentOrderStatus === tab.key ? 'border-b-2' : ''}" style="${currentOrderStatus === tab.key ? 'border-color: var(--primary-orange); color: var(--primary-orange);' : 'color: var(--text-secondary);'}">${tab.label}<span class="ml-2 px-2 py-1 text-xs rounded-full" style="background: ${currentOrderStatus === tab.key ? 'var(--primary-orange)' : 'rgba(145,164,196,0.18)'}; color: ${currentOrderStatus === tab.key ? '#fff' : 'inherit'};">${tab.count}</span></button>`).join('')}
            </div>
        </div>
        <div id="ordersList" class="space-y-4">${renderOrderCards(currentOrderStatus)}</div>
    </div>`;
}

function renderOrderActionButtons(order, compact = false) {
    const classes = compact ? 'px-3 py-2 rounded-lg text-sm' : 'px-4 py-2 rounded-lg';
    const buttons = [`<button onclick="openOrderDetail(${order.orderId})" class="${classes}" style="background: rgba(59,130,246,0.12); color: #2563eb;">查看详情</button>`];

    if (order.status === 'pending') {
        buttons.unshift(`<button onclick="acceptOrder(${order.orderId})" class="${classes} btn-primary">接单</button>`);
    } else if (order.status === 'preparing') {
        buttons.unshift(`<button onclick="updateOrderStatus(${order.orderId}, 'delivering')" class="${classes} btn-primary">开始配送</button>`);
    } else if (order.status === 'delivering') {
        buttons.unshift(`<button onclick="updateOrderStatus(${order.orderId}, 'completed')" class="${classes} btn-primary">完成订单</button>`);
    }

    return buttons.join('');
}

function renderOrderCards(status) {
    const list = status ? orders.filter((item) => item.status === status) : orders;
    if (!list.length) {
        return renderEmptyState('暂无匹配订单', '切换筛选状态后再试，或等待新订单进入。');
    }

    return list.map((order) => {
        const statusMeta = getOrderStatusMeta(order.status);
        return `<div class="order-card ${order.status}">
            <div class="flex flex-col gap-4 lg:flex-row lg:items-start lg:justify-between mb-4">
                <div>
                    <p class="font-semibold text-lg mb-1" style="color: var(--text-primary);">订单号 ${escapeHtml(order.orderNumber)}</p>
                    <p class="text-sm" style="color: var(--text-secondary);">${escapeHtml(order.customer)} · ${escapeHtml(order.phone)} · ${escapeHtml(formatDateTime(order.orderTime))}</p>
                    <p class="text-sm mt-1" style="color: var(--text-secondary);">${escapeHtml(order.address)}</p>
                    ${order.note ? `<p class="text-sm mt-2" style="color: var(--warning);">备注：${escapeHtml(order.note)}</p>` : ''}
                </div>
                <div class="flex items-center gap-3">${renderBadge(statusMeta.text, statusMeta.className)}<span class="font-semibold" style="color: var(--primary-orange);">${formatCurrency(order.totalAmount)}</span></div>
            </div>
            <div class="border-t border-b border-gray-100 py-4 mb-4">
                <div class="grid grid-cols-1 md:grid-cols-3 gap-3 mb-4">
                    <div class="p-3 rounded-xl" style="background: rgba(255,122,33,0.08);">
                        <p class="text-xs mb-1" style="color: var(--text-secondary);">商品金额</p>
                        <p class="font-semibold" style="color: var(--text-primary);">${formatCurrency(order.goodsAmount)}</p>
                    </div>
                    <div class="p-3 rounded-xl" style="background: rgba(59,130,246,0.08);">
                        <p class="text-xs mb-1" style="color: var(--text-secondary);">配送费 / 打包费</p>
                        <p class="font-semibold" style="color: var(--text-primary);">${formatCurrency(order.deliveryFee)} / ${formatCurrency(order.packAmount)}</p>
                    </div>
                    <div class="p-3 rounded-xl" style="background: rgba(17,167,92,0.08);">
                        <p class="text-xs mb-1" style="color: var(--text-secondary);">商品件数</p>
                        <p class="font-semibold" style="color: var(--text-primary);">${escapeHtml(order.itemCount)}</p>
                    </div>
                </div>
                <div class="space-y-2">
                    ${(order.items || []).slice(0, 3).map((item) => `<div class="flex items-center gap-3">
                        <img src="${escapeHtml(item.image || './download/product_fried_rice.jpg')}" alt="${escapeHtml(item.name)}" class="w-12 h-12 rounded object-cover" onerror="this.src='./download/product_fried_rice.jpg'" />
                        <div class="flex-1">
                            <p class="font-medium" style="color: var(--text-primary);">${escapeHtml(item.name)}</p>
                            <p class="text-sm" style="color: var(--text-secondary);">x${escapeHtml(item.quantity)}</p>
                        </div>
                        <p class="font-medium" style="color: var(--primary-orange);">${formatCurrency(item.price)}</p>
                    </div>`).join('')}
                    ${order.items.length > 3 ? `<p class="text-sm" style="color: var(--text-secondary);">还有 ${order.items.length - 3} 个商品，点击“查看详情”可查看完整明细。</p>` : ''}
                </div>
            </div>
            <div class="flex flex-col gap-3 lg:flex-row lg:items-center lg:justify-between">
                <p class="text-sm" style="color: var(--text-secondary);">商家：${escapeHtml(order.merchantName || getMerchantDisplayName())}</p>
                <div class="flex flex-wrap items-center gap-2">${renderOrderActionButtons(order)}</div>
            </div>
        </div>`;
    }).join('');
}

function renderSetMeals() {
    const actions = `<button onclick="openAddSetmealDialog()" class="btn-primary px-5 py-2 rounded-lg flex items-center gap-2">${icons.plus}<span>新增套餐</span></button>`;
    return `<div class="content-area fade-in">
        ${renderStatusBanner()}
        ${renderPageHeader('套餐管理', '套餐与菜品都自动挂载到当前商家，不再共享全局菜单。', actions)}
        <div class="grid grid-cols-1 md:grid-cols-2 xl:grid-cols-3 gap-6">
            ${setMeals.length === 0 ? renderEmptyState('当前没有套餐', '可根据热销组合快速创建套餐商品。') : setMeals.map((meal) => `<div class="product-card">
                <div class="relative overflow-hidden">
                    <img src="${escapeHtml(meal.image)}" alt="${escapeHtml(meal.name)}" onerror="this.src='./download/product_fried_rice.jpg'" />
                </div>
                <div class="p-4">
                    <div class="flex items-start justify-between gap-3 mb-2">
                        <div>
                            <h3 class="font-semibold text-lg" style="color: var(--text-primary);">${escapeHtml(meal.name)}</h3>
                            <p class="text-sm" style="color: var(--text-secondary);">${escapeHtml(meal.categoryName)}</p>
                        </div>
                        ${renderBadge(meal.status === 1 ? '在售' : '停售', meal.status === 1 ? 'status-online' : 'status-offline')}
                    </div>
                    <p class="text-sm min-h-[44px] mb-3" style="color: var(--text-secondary);">${escapeHtml(meal.description)}</p>
                    <div class="flex items-center justify-between mb-3">
                        <span class="text-2xl font-bold" style="color: var(--primary-orange);">${formatCurrency(meal.price)}</span>
                        <span class="text-sm" style="color: var(--text-secondary);">含 ${(meal.products || []).length} 个菜品</span>
                    </div>
                    <div class="text-sm mb-4" style="color: var(--text-secondary);">${(meal.products || []).map((item) => `${escapeHtml(item.name)} x${escapeHtml(item.quantity)}`).join(' · ') || '暂无配置菜品'}</div>
                    <div class="flex items-center justify-between">
                        <label class="switch">
                            <input type="checkbox" ${meal.status === 1 ? 'checked' : ''} onchange="toggleSetmealStatus(${meal.id})">
                            <span class="slider"></span>
                        </label>
                        <div class="flex items-center gap-2">
                            <button class="p-2 hover:bg-gray-100 rounded" style="color: var(--primary-orange);" onclick="editSetmeal(${meal.id})">${icons.edit}</button>
                            <button class="p-2 hover:bg-gray-100 rounded" style="color: var(--accent-red);" onclick="deleteSetmeal(${meal.id})">${icons.delete}</button>
                        </div>
                    </div>
                </div>
            </div>`).join('')}
        </div>
    </div>`;
}
function renderStatistics() {
    return `<div class="content-area fade-in">
        ${renderStatusBanner()}
        ${renderPageHeader('数据中心', '统计口径已收敛为当前商家视角，便于商家管理端直接看本店经营。', `<button class="px-4 py-2 border border-gray-300 rounded-lg hover:bg-gray-50 flex items-center gap-2" onclick="API.Report.exportBusinessData()">${icons.download}<span>导出报表</span></button>`)}
        <div class="grid grid-cols-1 lg:grid-cols-2 gap-6 mb-6">
            <div class="bg-white rounded-lg p-6">
                <div class="flex items-center justify-between mb-4">
                    <h3 class="text-lg font-semibold" style="color: var(--text-primary);">订单与营业额趋势</h3>
                    <span class="text-sm" style="color: var(--text-secondary);">近 7 日</span>
                </div>
                <div style="height: 300px;"><canvas id="dualAxisChart"></canvas></div>
            </div>
            <div class="bg-white rounded-lg p-6">
                <h3 class="text-lg font-semibold mb-4" style="color: var(--text-primary);">时段订单热度</h3>
                <div style="height: 300px;"><canvas id="hourlyChart"></canvas></div>
            </div>
        </div>
        <div class="grid grid-cols-1 xl:grid-cols-3 gap-6 mb-6">
            <div class="bg-white rounded-lg p-6">
                <h3 class="text-lg font-semibold mb-4" style="color: var(--text-primary);">经营摘要</h3>
                <div class="space-y-3">
                    <div class="flex items-center justify-between"><span class="text-sm" style="color: var(--text-secondary);">今日营业额</span><span class="font-semibold" style="color: var(--text-primary);">${formatCurrency(statistics.today.revenue || 0)}</span></div>
                    <div class="flex items-center justify-between"><span class="text-sm" style="color: var(--text-secondary);">今日订单数</span><span class="font-semibold" style="color: var(--text-primary);">${escapeHtml(statistics.today.orders || 0)}</span></div>
                    <div class="flex items-center justify-between"><span class="text-sm" style="color: var(--text-secondary);">新增用户数</span><span class="font-semibold" style="color: var(--text-primary);">${escapeHtml(statistics.today.newUsers || 0)}</span></div>
                    <div class="flex items-center justify-between"><span class="text-sm" style="color: var(--text-secondary);">商品 / 套餐总数</span><span class="font-semibold" style="color: var(--text-primary);">${escapeHtml(statistics.today.totalProducts || 0)} / ${escapeHtml(statistics.today.totalSetmeals || 0)}</span></div>
                </div>
            </div>
            <div class="xl:col-span-2 bg-white rounded-lg p-6">
                <h3 class="text-lg font-semibold mb-4" style="color: var(--text-primary);">热销商品 Top 10</h3>
                ${statistics.topProducts.length === 0 ? renderEmptyState('暂无热销数据', '订单沉淀后会展示当前商家的热销商品排行。') : `<div class="space-y-3">${statistics.topProducts.map((item, index) => `<div class="flex items-center gap-4">
                    <div class="w-8 h-8 rounded-full flex items-center justify-center font-bold ${index < 3 ? 'text-white' : 'bg-gray-100 text-gray-600'}" style="${index < 3 ? 'background: linear-gradient(135deg, var(--primary-orange), var(--secondary-orange))' : ''}">${index + 1}</div>
                    <div class="flex-1">
                        <div class="flex items-center justify-between mb-1"><span class="font-medium" style="color: var(--text-primary);">${escapeHtml(item.name)}</span><span class="text-sm" style="color: var(--text-secondary);">${escapeHtml(item.sales)} 份</span></div>
                        <div class="w-full bg-gray-100 rounded-full h-2"><div class="h-2 rounded-full" style="width: ${item.percentage}%; background: linear-gradient(90deg, var(--primary-orange), var(--secondary-orange))"></div></div>
                    </div>
                </div>`).join('')}</div>`}
            </div>
        </div>
        <div class="bg-white rounded-lg p-6">
            <h3 class="text-lg font-semibold mb-4" style="color: var(--text-primary);">分类结构</h3>
            <div class="grid grid-cols-1 md:grid-cols-2 xl:grid-cols-4 gap-4">
                ${statistics.categoryData.labels.map((label, index) => `<div class="p-4 rounded-lg border border-gray-200">
                    <div class="flex items-center justify-between mb-2">
                        <span class="font-medium" style="color: var(--text-primary);">${escapeHtml(label)}</span>
                        <span class="w-3 h-3 rounded-full" style="background: ${statistics.categoryData.colors[index]}"></span>
                    </div>
                    <p class="text-2xl font-bold mb-1" style="color: var(--text-primary);">${escapeHtml(statistics.categoryData.values[index])}%</p>
                    <p class="text-sm" style="color: var(--text-secondary);">菜单占比</p>
                </div>`).join('')}
            </div>
        </div>
    </div>`;
}

function renderEmployees() {
    if (!canManageEmployees()) {
        return `<div class="content-area fade-in">${renderStatusBanner()}${renderPageHeader('员工管理', '当前账号为商家员工，只保留只读运营视角。')}${renderEmptyState('当前账号无员工管理权限', '如需新增或维护账号，请使用商家管理员账号登录。')}</div>`;
    }

    const actions = `<button onclick="openAddEmployeeDialog()" class="btn-primary px-5 py-2 rounded-lg flex items-center gap-2">${icons.plus}<span>新增员工</span></button>`;
    return `<div class="content-area fade-in">
        ${renderStatusBanner()}
        ${renderPageHeader('员工管理', '同一商家下的账号由后端自动绑定 merchantId，新员工默认密码为 123456。', actions)}
        <div class="bg-white rounded-lg overflow-hidden">
            <table class="w-full">
                <thead class="bg-gray-50">
                    <tr>
                        <th class="px-6 py-3 text-left text-sm font-semibold text-gray-700">姓名</th>
                        <th class="px-6 py-3 text-left text-sm font-semibold text-gray-700">账号</th>
                        <th class="px-6 py-3 text-left text-sm font-semibold text-gray-700">手机号</th>
                        <th class="px-6 py-3 text-left text-sm font-semibold text-gray-700">角色</th>
                        <th class="px-6 py-3 text-left text-sm font-semibold text-gray-700">状态</th>
                        <th class="px-6 py-3 text-left text-sm font-semibold text-gray-700">创建日期</th>
                        <th class="px-6 py-3 text-left text-sm font-semibold text-gray-700">操作</th>
                    </tr>
                </thead>
                <tbody class="divide-y divide-gray-200">
                    ${employees.length === 0 ? `<tr><td colspan="7" class="px-6 py-10 text-center text-sm" style="color: var(--text-secondary);">当前商家还没有员工账号。</td></tr>` : employees.map((employee) => `<tr class="hover:bg-gray-50">
                        <td class="px-6 py-4">
                            <div class="flex items-center gap-3">
                                <div class="w-10 h-10 rounded-full flex items-center justify-center text-white font-bold" style="background: linear-gradient(135deg, var(--primary-orange), var(--secondary-orange));">${escapeHtml(employee.name.slice(0, 1))}</div>
                                <span class="font-medium" style="color: var(--text-primary);">${escapeHtml(employee.name)}</span>
                            </div>
                        </td>
                        <td class="px-6 py-4 text-gray-600">${escapeHtml(employee.username)}</td>
                        <td class="px-6 py-4 text-gray-600">${escapeHtml(employee.phone)}</td>
                        <td class="px-6 py-4">${renderBadge(employee.role, employee.accountType === ACCOUNT_TYPES.MERCHANT_ADMIN ? 'status-online' : 'status-pending')}</td>
                        <td class="px-6 py-4">${renderBadge(employee.status === 1 ? '启用' : '停用', employee.status === 1 ? 'status-online' : 'status-offline')}</td>
                        <td class="px-6 py-4 text-gray-600">${escapeHtml(employee.createTime)}</td>
                        <td class="px-6 py-4">
                            <div class="flex items-center gap-2">
                                <button class="p-2 hover:bg-gray-100 rounded" style="color: var(--primary-orange);" onclick="editEmployee(${employee.id})">${icons.edit}</button>
                                <button class="p-2 hover:bg-gray-100 rounded" style="color: ${employee.status === 1 ? 'var(--warning)' : 'var(--accent-green)'};" onclick="toggleEmployeeStatus(${employee.id})">${employee.status === 1 ? icons.clock : icons.plus}</button>
                            </div>
                        </td>
                    </tr>`).join('')}
                </tbody>
            </table>
        </div>
    </div>`;
}

function renderMerchantProfile() {
    if (!merchantProfile) {
        return `<div class="content-area fade-in">${renderEmptyState('未加载到商家信息', '请刷新页面后重试。')}</div>`;
    }

    const campusMeta = getCampusStatusMeta(campusServiceStatus);
    const businessMeta = getBusinessStatusMeta(merchantProfile.businessStatus);
    const platformMeta = getPlatformStatusMeta(merchantProfile.status);
    const cover = merchantProfile.coverImage || merchantProfile.logo;

    const actionButton = isMerchantAdmin()
        ? `<button onclick="toggleBusinessStatus()" class="btn-primary px-5 py-2 rounded-lg">${toNumber(merchantProfile.businessStatus, 1) === 1 ? '一键打烊' : '恢复营业'}</button>`
        : `<div class="text-sm px-4 py-2 rounded-lg border" style="background: rgba(255,255,255,0.7); border-color: rgba(196,208,226,0.64); color: var(--text-secondary);">当前账号只读，无法修改营业状态</div>`;

    return `<div class="content-area fade-in">
        ${renderStatusBanner()}
        ${renderPageHeader('商家信息', '当前页用于展示商家身份、营业状态和校园统一规则。基础资料由平台统一维护。', actionButton)}
        <div class="bg-white rounded-lg overflow-hidden mb-6">
            <div class="p-8" style="background: linear-gradient(135deg, rgba(255,122,33,0.16), rgba(59,130,246,0.12));">
                <div class="flex flex-col gap-6 xl:flex-row xl:items-center">
                    <div class="w-28 h-28 rounded-3xl overflow-hidden flex items-center justify-center text-white text-3xl font-bold" style="background: linear-gradient(135deg, var(--primary-orange), var(--secondary-orange));">
                        ${cover ? `<img src="${escapeHtml(normalizeImageUrl(cover))}" alt="${escapeHtml(merchantProfile.name)}" class="w-full h-full object-cover" />` : getMerchantInitial()}
                    </div>
                    <div class="flex-1">
                        <p class="text-sm mb-2" style="color: var(--text-secondary);">${escapeHtml(getCampusDisplayName())} · 商家编码 ${escapeHtml(merchantProfile.merchantCode || '--')}</p>
                        <h2 class="text-3xl font-bold mb-3" style="color: var(--text-primary);">${escapeHtml(merchantProfile.name || getMerchantDisplayName())}</h2>
                        <div class="flex flex-wrap gap-2 mb-4">
                            ${renderBadge(campusMeta.text, campusMeta.className)}
                            ${renderBadge(businessMeta.text, businessMeta.className)}
                            ${renderBadge(platformMeta.text, platformMeta.className)}
                        </div>
                        <p class="text-sm leading-6" style="color: var(--text-secondary);">${escapeHtml(merchantProfile.description || '平台尚未填写商家简介。')}</p>
                    </div>
                </div>
            </div>
        </div>
        <div class="grid grid-cols-1 xl:grid-cols-2 gap-6">
            <div class="bg-white rounded-lg p-6">
                <h3 class="text-lg font-semibold mb-4" style="color: var(--text-primary);">商家基础资料</h3>
                <div class="space-y-4">
                    <div><p class="text-sm mb-1" style="color: var(--text-secondary);">商家名称</p><p class="font-semibold" style="color: var(--text-primary);">${escapeHtml(merchantProfile.name || '--')}</p></div>
                    <div><p class="text-sm mb-1" style="color: var(--text-secondary);">商家公告</p><p class="font-semibold" style="color: var(--text-primary);">${escapeHtml(merchantProfile.announcement || '暂无公告')}</p></div>
                    <div><p class="text-sm mb-1" style="color: var(--text-secondary);">详细地址</p><p class="font-semibold" style="color: var(--text-primary);">${escapeHtml(merchantProfile.addressDetail || '暂无地址')}</p></div>
                    <div><p class="text-sm mb-1" style="color: var(--text-secondary);">排序值</p><p class="font-semibold" style="color: var(--text-primary);">${escapeHtml(merchantProfile.sort || 0)}</p></div>
                </div>
            </div>
            <div class="bg-white rounded-lg p-6">
                <h3 class="text-lg font-semibold mb-4" style="color: var(--text-primary);">联系与营业信息</h3>
                <div class="space-y-4">
                    <div><p class="text-sm mb-1" style="color: var(--text-secondary);">联系人</p><p class="font-semibold" style="color: var(--text-primary);">${escapeHtml(merchantProfile.contactName || '平台未配置')}</p></div>
                    <div><p class="text-sm mb-1" style="color: var(--text-secondary);">联系电话</p><p class="font-semibold" style="color: var(--text-primary);">${escapeHtml(merchantProfile.contactPhone || merchantProfile.servicePhone || '--')}</p></div>
                    <div><p class="text-sm mb-1" style="color: var(--text-secondary);">营业时段</p><p class="font-semibold" style="color: var(--text-primary);">${escapeHtml(formatBusinessHours(merchantProfile.businessBeginTime, merchantProfile.businessEndTime))}</p></div>
                    <div><p class="text-sm mb-1" style="color: var(--text-secondary);">当前账号</p><p class="font-semibold" style="color: var(--text-primary);">${escapeHtml(currentUser?.name || '--')} · ${escapeHtml(getAccountTypeLabel(currentUser?.accountType))}</p></div>
                </div>
            </div>
            <div class="bg-white rounded-lg p-6">
                <h3 class="text-lg font-semibold mb-4" style="color: var(--text-primary);">校园统一规则</h3>
                <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
                    <div class="p-4 rounded-xl border" style="border-color: rgba(196,208,226,0.64);">
                        <p class="text-sm mb-1" style="color: var(--text-secondary);">校园服务状态</p>
                        <div class="mt-2">${renderBadge(campusMeta.text, campusMeta.className)}</div>
                    </div>
                    <div class="p-4 rounded-xl border" style="border-color: rgba(196,208,226,0.64);">
                        <p class="text-sm mb-1" style="color: var(--text-secondary);">统一配送费</p>
                        <p class="font-semibold mt-2" style="color: var(--text-primary);">${formatCurrency(merchantProfile.deliveryFee || 0)}</p>
                    </div>
                    <div class="p-4 rounded-xl border" style="border-color: rgba(196,208,226,0.64);">
                        <p class="text-sm mb-1" style="color: var(--text-secondary);">预计送达</p>
                        <p class="font-semibold mt-2" style="color: var(--text-primary);">${escapeHtml(merchantProfile.estimatedDeliveryMinutes || '--')} 分钟</p>
                    </div>
                    <div class="p-4 rounded-xl border" style="border-color: rgba(196,208,226,0.64);">
                        <p class="text-sm mb-1" style="color: var(--text-secondary);">校园服务热线</p>
                        <p class="font-semibold mt-2" style="color: var(--text-primary);">${escapeHtml(merchantProfile.servicePhone || '--')}</p>
                    </div>
                </div>
            </div>
            <div class="bg-white rounded-lg p-6">
                <h3 class="text-lg font-semibold mb-4" style="color: var(--text-primary);">营业控制</h3>
                <div class="space-y-4">
                    <div class="flex items-center justify-between p-4 rounded-xl border" style="border-color: rgba(196,208,226,0.64);">
                        <div>
                            <p class="font-semibold" style="color: var(--text-primary);">商家营业状态</p>
                            <p class="text-sm mt-1" style="color: var(--text-secondary);">该开关控制当前商家在用户端是否可下单。</p>
                        </div>
                        ${renderBadge(businessMeta.text, businessMeta.className)}
                    </div>
                    <div class="flex items-center justify-between p-4 rounded-xl border" style="border-color: rgba(196,208,226,0.64);">
                        <div>
                            <p class="font-semibold" style="color: var(--text-primary);">平台状态</p>
                            <p class="text-sm mt-1" style="color: var(--text-secondary);">平台停用时，商家侧仅保留查看能力。</p>
                        </div>
                        ${renderBadge(platformMeta.text, platformMeta.className)}
                    </div>
                    <p class="text-sm leading-6" style="color: var(--text-secondary);">一期最小平台版里，商家可直接切换营业状态；名称、Logo、公告等基础资料仍由平台统一维护，避免商家端越权修改。</p>
                </div>
            </div>
        </div>
    </div>`;
}

function renderOrderDetailDrawer() {
    const order = orders.find((item) => item.orderId === selectedOrderId);
    if (!order) return '';

    const statusMeta = getOrderStatusMeta(order.status);
    const paymentText = order.payStatus === 1 ? '已支付' : '未支付';

    return `<div class="fixed inset-0 z-[1000]">
        <div class="absolute inset-0" style="background: rgba(12,23,44,0.54);" onclick="closeOrderDetail()"></div>
        <div class="drawer" onclick="event.stopPropagation()">
            <div class="p-6 border-b border-gray-200 flex items-start justify-between gap-4">
                <div>
                    <p class="text-sm mb-2" style="color: var(--text-secondary);">订单详情</p>
                    <h3 class="text-2xl font-semibold" style="color: var(--text-primary);">订单号 ${escapeHtml(order.orderNumber)}</h3>
                    <div class="flex flex-wrap gap-2 mt-3">
                        ${renderBadge(statusMeta.text, statusMeta.className)}
                        ${renderBadge(paymentText, order.payStatus === 1 ? 'status-online' : 'status-pending')}
                    </div>
                </div>
                <button class="px-4 py-2 rounded-lg border" style="border-color: rgba(196,208,226,0.64);" onclick="closeOrderDetail()">关闭</button>
            </div>
            <div class="p-6 space-y-6">
                <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
                    <div class="p-4 rounded-xl border" style="border-color: rgba(196,208,226,0.64);">
                        <p class="text-sm mb-2" style="color: var(--text-secondary);">下单用户</p>
                        <p class="font-semibold" style="color: var(--text-primary);">${escapeHtml(order.customer)}</p>
                        <p class="text-sm mt-2" style="color: var(--text-secondary);">${escapeHtml(order.rawPhone || order.phone)}</p>
                    </div>
                    <div class="p-4 rounded-xl border" style="border-color: rgba(196,208,226,0.64);">
                        <p class="text-sm mb-2" style="color: var(--text-secondary);">商家 / 校园</p>
                        <p class="font-semibold" style="color: var(--text-primary);">${escapeHtml(order.merchantName || getMerchantDisplayName())}</p>
                        <p class="text-sm mt-2" style="color: var(--text-secondary);">${escapeHtml(getCampusDisplayName())}</p>
                    </div>
                </div>
                <div class="p-4 rounded-xl border" style="border-color: rgba(196,208,226,0.64);">
                    <p class="text-sm mb-2" style="color: var(--text-secondary);">收货地址</p>
                    <p class="font-semibold" style="color: var(--text-primary);">${escapeHtml(order.address)}</p>
                    <p class="text-sm mt-2" style="color: var(--text-secondary);">下单时间：${escapeHtml(formatDateTime(order.orderTime))}</p>
                    ${order.checkoutTime ? `<p class="text-sm mt-1" style="color: var(--text-secondary);">支付时间：${escapeHtml(formatDateTime(order.checkoutTime))}</p>` : ''}
                </div>
                <div class="p-4 rounded-xl border" style="border-color: rgba(196,208,226,0.64);">
                    <div class="flex items-center justify-between mb-4">
                        <h4 class="text-lg font-semibold" style="color: var(--text-primary);">商品明细</h4>
                        <span class="text-sm" style="color: var(--text-secondary);">${escapeHtml(order.itemCount)} 件商品</span>
                    </div>
                    <div class="space-y-3">
                        ${(order.items || []).map((item) => `<div class="flex items-center gap-3">
                            <img src="${escapeHtml(item.image || './download/product_fried_rice.jpg')}" alt="${escapeHtml(item.name)}" class="w-14 h-14 rounded object-cover" onerror="this.src='./download/product_fried_rice.jpg'" />
                            <div class="flex-1">
                                <p class="font-medium" style="color: var(--text-primary);">${escapeHtml(item.name)}</p>
                                <p class="text-sm mt-1" style="color: var(--text-secondary);">数量 x${escapeHtml(item.quantity)}</p>
                            </div>
                            <div class="text-right">
                                <p class="font-semibold" style="color: var(--primary-orange);">${formatCurrency(item.price)}</p>
                            </div>
                        </div>`).join('')}
                    </div>
                </div>
                <div class="grid grid-cols-1 md:grid-cols-3 gap-4">
                    <div class="p-4 rounded-xl border" style="border-color: rgba(196,208,226,0.64);">
                        <p class="text-sm mb-1" style="color: var(--text-secondary);">商品金额</p>
                        <p class="font-semibold" style="color: var(--text-primary);">${formatCurrency(order.goodsAmount)}</p>
                    </div>
                    <div class="p-4 rounded-xl border" style="border-color: rgba(196,208,226,0.64);">
                        <p class="text-sm mb-1" style="color: var(--text-secondary);">配送费 / 打包费</p>
                        <p class="font-semibold" style="color: var(--text-primary);">${formatCurrency(order.deliveryFee)} / ${formatCurrency(order.packAmount)}</p>
                    </div>
                    <div class="p-4 rounded-xl border" style="border-color: rgba(196,208,226,0.64);">
                        <p class="text-sm mb-1" style="color: var(--text-secondary);">订单总额</p>
                        <p class="font-semibold" style="color: var(--text-primary);">${formatCurrency(order.totalAmount)}</p>
                    </div>
                </div>
                ${order.note || order.cancelReason || order.rejectionReason ? `<div class="p-4 rounded-xl border" style="border-color: rgba(196,208,226,0.64);">
                    <h4 class="text-lg font-semibold mb-3" style="color: var(--text-primary);">备注与异常说明</h4>
                    ${order.note ? `<p class="text-sm mb-2" style="color: var(--text-secondary);">用户备注：${escapeHtml(order.note)}</p>` : ''}
                    ${order.cancelReason ? `<p class="text-sm mb-2" style="color: var(--text-secondary);">取消原因：${escapeHtml(order.cancelReason)}</p>` : ''}
                    ${order.rejectionReason ? `<p class="text-sm" style="color: var(--text-secondary);">拒单原因：${escapeHtml(order.rejectionReason)}</p>` : ''}
                </div>` : ''}
                <div class="flex flex-wrap items-center gap-2">${renderOrderActionButtons(order, true)}</div>
            </div>
        </div>
    </div>`;
}

function renderLoginPage() {
    return `<div class="login-container">
        <div class="login-left">
            <div class="login-content flex flex-col items-center justify-center h-full text-white px-12">
                <h1 class="font-bold mb-6">杏林校园外卖</h1>
                <p class="text-2xl mb-4">多商户商家运营后台</p>
                <p class="text-lg opacity-90">登录后进入当前商家的订单、菜单与营业工作台</p>
                <p class="text-sm mt-10 px-4 py-2 rounded-full border border-white/25 bg-white/10 tracking-wider">MULTI-MERCHANT CAMPUS DELIVERY</p>
            </div>
        </div>
        <div class="login-right">
            <div class="w-full max-w-md">
                <div class="text-center mb-8">
                    <h2 class="text-3xl font-bold mb-2" style="color: var(--text-primary);">商家后台登录</h2>
                    <p class="text-gray-500">当前版本仅支持商家管理员和商家员工登录</p>
                </div>
                <form id="loginForm" class="space-y-6">
                    <div>
                        <label class="block text-sm font-medium mb-2" style="color: var(--text-primary);">用户名</label>
                        <input type="text" id="username" class="w-full px-4 py-3 border border-gray-300 rounded-lg focus:outline-none" placeholder="请输入商家账号，例如 m2admin_phase1" />
                    </div>
                    <div>
                        <label class="block text-sm font-medium mb-2" style="color: var(--text-primary);">密码</label>
                        <input type="password" id="password" class="w-full px-4 py-3 border border-gray-300 rounded-lg focus:outline-none" placeholder="请输入登录密码" />
                    </div>
                    <div class="flex items-center justify-between">
                        <label class="flex items-center">
                            <input type="checkbox" class="mr-2" checked />
                            <span class="text-sm text-gray-600">记住本次登录</span>
                        </label>
                        <span class="text-sm" style="color: var(--primary-orange);">平台管理员请使用平台端</span>
                    </div>
                    <button type="submit" class="w-full btn-primary py-3 rounded-lg text-white font-semibold text-lg">登录商家后台</button>
                </form>
            </div>
        </div>
    </div>`;
}

function getSelectedOrder() {
    return orders.find((item) => item.orderId === selectedOrderId) || null;
}

function updateDocumentTitle() {
    const title = VIEW_TITLES[currentView] || VIEW_TITLES.dashboard;
    document.title = `${title} - ${getMerchantDisplayName()}`;
}

function buildCategoryHint(list) {
    return list.map((item) => `${item.id}:${item.name}`).join('；');
}

function buildDishComboHint() {
    const sample = products.slice(0, 12).map((item) => `${item.id}:${item.name}`).join('；');
    return sample || '请先创建菜品';
}

function formatSetmealComboInput(items) {
    return (items || []).map((item) => `${item.dishId || item.id}*${item.quantity || item.copies || 1}`).join(',');
}

function parseSetmealComboInput(text) {
    return String(text || '')
        .split(',')
        .map((item) => item.trim())
        .filter(Boolean)
        .map((item) => {
            const [dishIdText, copiesText] = item.split('*').map((part) => part.trim());
            const dishId = Number(dishIdText);
            const copies = Number(copiesText || '1');
            const dish = products.find((product) => product.id === dishId);
            if (!Number.isFinite(dishId) || !dish) return null;
            return {
                dishId,
                name: dish.name,
                copies: Number.isFinite(copies) && copies > 0 ? copies : 1
            };
        })
        .filter(Boolean);
}

function navigateTo(view) {
    currentView = view;
    selectedOrderId = null;
    renderApp();
}

function openPendingOrders() {
    currentOrderStatus = 'pending';
    currentView = 'orders';
    renderApp();
}

async function handleLogin(event) {
    event.preventDefault();
    const username = document.getElementById('username').value.trim();
    const password = document.getElementById('password').value;
    const submitButton = event.target.querySelector('button[type="submit"]');
    const originalText = submitButton.innerHTML;

    if (!username || !password) {
        alert('请输入用户名和密码');
        return;
    }

    submitButton.disabled = true;
    submitButton.innerHTML = '登录中...';

    try {
        const result = await API.Employee.login(username, password);
        if (toNumber(result.accountType) === ACCOUNT_TYPES.PLATFORM_ADMIN || !result.merchantId) {
            alert('该账号属于平台端，请使用平台管理端登录。');
            return;
        }

        API.Token.set(result.token);
        currentUser = {
            id: result.id,
            userName: result.userName,
            name: result.name || result.userName,
            merchantId: result.merchantId,
            accountType: result.accountType
        };
        persistCurrentUser();
        resetSyncState();
        currentOrderStatus = '';
        selectedOrderId = null;
        currentView = 'dashboard';
        await syncDataForView('dashboard', true);
        renderApp();
    } catch (error) {
        alert(`登录失败：${error.message || error}`);
    } finally {
        submitButton.disabled = false;
        submitButton.innerHTML = originalText;
    }
}

async function logout() {
    if (!confirm('确认退出当前商家后台吗？')) return;

    try {
        await API.Employee.logout();
    } catch (error) {
        console.warn('Logout request failed:', error);
    }

    API.Token.remove();
    clearCurrentUser();
    merchantProfile = null;
    campusServiceStatus = 1;
    resetSyncState();
    currentOrderStatus = '';
    selectedOrderId = null;
    currentView = 'login';
    renderApp();
}

function filterProducts() {
    const searchText = (document.getElementById('productSearch')?.value || '').trim().toLowerCase();
    const categoryValue = document.getElementById('categoryFilter')?.value || '';
    const statusValue = document.getElementById('statusFilter')?.value || '';

    const filtered = products.filter((product) => {
        const matchSearch = (product.name || '').toLowerCase().includes(searchText) || (product.description || '').toLowerCase().includes(searchText);
        const matchCategory = !categoryValue || String(product.categoryId) === String(categoryValue);
        const matchStatus = !statusValue || String(product.status) === String(statusValue);
        return matchSearch && matchCategory && matchStatus;
    });

    const container = document.getElementById('productsGrid');
    if (container) {
        container.innerHTML = renderProductCards(filtered);
    }
}

function filterOrdersByStatus(status) {
    currentOrderStatus = status;
    renderApp();
}

async function openOrderDetail(orderId) {
    selectedOrderId = orderId;
    await hydrateOrderDetail(orderId);
    renderApp();
}

function closeOrderDetail() {
    selectedOrderId = null;
    renderApp();
}

async function openAddProductDialog() {
    await syncCategoriesData(true);
    if (!dishCategoryOptions.length) {
        alert('请先在后端创建菜品分类');
        return;
    }

    const name = prompt('请输入商品名称');
    if (!name || !name.trim()) return;

    const categoryIdInput = prompt(`请输入分类 ID：${buildCategoryHint(dishCategoryOptions)}`, String(dishCategoryOptions[0].id));
    if (!categoryIdInput) return;
    const categoryId = Number(categoryIdInput);
    if (!Number.isFinite(categoryId)) {
        alert('分类 ID 不合法');
        return;
    }

    const priceInput = prompt('请输入商品价格（元）', '18');
    if (!priceInput) return;
    const price = Number(priceInput);
    if (!Number.isFinite(price) || price <= 0) {
        alert('商品价格必须大于 0');
        return;
    }

    const description = prompt('请输入商品描述（可选）', '') || '';
    const image = prompt('请输入商品图片 URL（可选）', '') || '';

    try {
        await API.Dish.addDish({
            name: name.trim(),
            categoryId,
            price: Number(price.toFixed(2)),
            description: description.trim(),
            image: image.trim(),
            status: 1,
            flavors: []
        });
        await Promise.all([syncProductsData(true), syncDashboardAndStatisticsData(true)]);
        renderApp();
        alert('新增商品成功');
    } catch (error) {
        alert(`新增商品失败：${error.message || error}`);
    }
}

async function editProduct(id) {
    await syncCategoriesData(false);

    try {
        const detail = await API.Dish.getDishById(id);
        const name = prompt('请输入商品名称', detail.name || '');
        if (!name || !name.trim()) return;

        const categoryIdInput = prompt(`请输入分类 ID：${buildCategoryHint(dishCategoryOptions)}`, String(detail.categoryId || dishCategoryOptions[0]?.id || ''));
        if (!categoryIdInput) return;
        const categoryId = Number(categoryIdInput);
        if (!Number.isFinite(categoryId)) {
            alert('分类 ID 不合法');
            return;
        }

        const priceInput = prompt('请输入商品价格（元）', String(detail.price || ''));
        if (!priceInput) return;
        const price = Number(priceInput);
        if (!Number.isFinite(price) || price <= 0) {
            alert('商品价格必须大于 0');
            return;
        }

        const description = prompt('请输入商品描述', detail.description || '') || '';
        const image = prompt('请输入商品图片 URL', detail.image || '') || '';

        await API.Dish.updateDish({
            id,
            name: name.trim(),
            categoryId,
            price: Number(price.toFixed(2)),
            description: description.trim(),
            image: image.trim(),
            status: detail.status,
            flavors: detail.flavors || []
        });
        await Promise.all([syncProductsData(true), syncDashboardAndStatisticsData(true)]);
        renderApp();
        alert('商品更新成功');
    } catch (error) {
        alert(`更新商品失败：${error.message || error}`);
    }
}

async function toggleProductStatus(id) {
    const target = products.find((item) => item.id === id);
    if (!target) return;

    try {
        await API.Dish.updateStatus(target.status === 1 ? 0 : 1, id);
        await Promise.all([syncProductsData(true), syncDashboardAndStatisticsData(true)]);
        renderApp();
    } catch (error) {
        alert(`切换商品状态失败：${error.message || error}`);
    }
}

async function deleteProduct(id) {
    if (!confirm('确认删除这个商品吗？')) return;

    try {
        await API.Dish.deleteDish([id]);
        await Promise.all([syncProductsData(true), syncDashboardAndStatisticsData(true)]);
        renderApp();
    } catch (error) {
        alert(`删除商品失败：${error.message || error}`);
    }
}

async function openAddSetmealDialog() {
    await Promise.all([syncCategoriesData(true), syncProductsData(true)]);

    if (!setmealCategoryOptions.length) {
        alert('请先在后端创建套餐分类');
        return;
    }
    if (!products.length) {
        alert('请先为当前商家创建菜品，再配置套餐');
        return;
    }

    const name = prompt('请输入套餐名称');
    if (!name || !name.trim()) return;

    const categoryIdInput = prompt(`请输入套餐分类 ID：${buildCategoryHint(setmealCategoryOptions)}`, String(setmealCategoryOptions[0].id));
    if (!categoryIdInput) return;
    const categoryId = Number(categoryIdInput);
    if (!Number.isFinite(categoryId)) {
        alert('套餐分类 ID 不合法');
        return;
    }

    const priceInput = prompt('请输入套餐价格（元）', '25');
    if (!priceInput) return;
    const price = Number(priceInput);
    if (!Number.isFinite(price) || price <= 0) {
        alert('套餐价格必须大于 0');
        return;
    }

    const comboInput = prompt(`请输入套餐菜品组合，格式为 dishId*份数，多个用英文逗号分隔。\n示例：1*1,2*2\n可选菜品：${buildDishComboHint()}`);
    if (!comboInput) return;
    const setmealDishes = parseSetmealComboInput(comboInput);
    if (!setmealDishes.length) {
        alert('套餐菜品组合不能为空，且菜品 ID 必须属于当前商家');
        return;
    }

    const description = prompt('请输入套餐描述（可选）', '') || '';
    const image = prompt('请输入套餐图片 URL（可选）', '') || '';

    try {
        await API.Setmeal.addSetmeal({
            name: name.trim(),
            categoryId,
            price: Number(price.toFixed(2)),
            description: description.trim(),
            image: image.trim(),
            status: 1,
            setmealDishes
        });
        await Promise.all([syncSetMealsData(true), syncDashboardAndStatisticsData(true)]);
        renderApp();
        alert('新增套餐成功');
    } catch (error) {
        alert(`新增套餐失败：${error.message || error}`);
    }
}

async function editSetmeal(id) {
    await Promise.all([syncCategoriesData(false), syncProductsData(false)]);

    try {
        const detail = await API.Setmeal.getSetmealById(id);
        const name = prompt('请输入套餐名称', detail.name || '');
        if (!name || !name.trim()) return;

        const categoryIdInput = prompt(`请输入套餐分类 ID：${buildCategoryHint(setmealCategoryOptions)}`, String(detail.categoryId || setmealCategoryOptions[0]?.id || ''));
        if (!categoryIdInput) return;
        const categoryId = Number(categoryIdInput);
        if (!Number.isFinite(categoryId)) {
            alert('套餐分类 ID 不合法');
            return;
        }

        const priceInput = prompt('请输入套餐价格（元）', String(detail.price || ''));
        if (!priceInput) return;
        const price = Number(priceInput);
        if (!Number.isFinite(price) || price <= 0) {
            alert('套餐价格必须大于 0');
            return;
        }

        const comboInput = prompt(
            `请输入套餐菜品组合，格式为 dishId*份数，多个用英文逗号分隔。\n示例：1*1,2*2\n可选菜品：${buildDishComboHint()}`,
            formatSetmealComboInput(detail.setmealDishes || [])
        );
        if (!comboInput) return;

        const setmealDishes = parseSetmealComboInput(comboInput);
        if (!setmealDishes.length) {
            alert('套餐菜品组合不能为空');
            return;
        }

        const description = prompt('请输入套餐描述', detail.description || '') || '';
        const image = prompt('请输入套餐图片 URL', detail.image || '') || '';

        await API.Setmeal.updateSetmeal({
            id,
            name: name.trim(),
            categoryId,
            price: Number(price.toFixed(2)),
            description: description.trim(),
            image: image.trim(),
            status: detail.status,
            setmealDishes
        });
        await Promise.all([syncSetMealsData(true), syncDashboardAndStatisticsData(true)]);
        renderApp();
        alert('套餐更新成功');
    } catch (error) {
        alert(`更新套餐失败：${error.message || error}`);
    }
}

async function toggleSetmealStatus(id) {
    const target = setMeals.find((item) => item.id === id);
    if (!target) return;

    try {
        await API.Setmeal.updateStatus(target.status === 1 ? 0 : 1, id);
        await Promise.all([syncSetMealsData(true), syncDashboardAndStatisticsData(true)]);
        renderApp();
    } catch (error) {
        alert(`切换套餐状态失败：${error.message || error}`);
    }
}

async function deleteSetmeal(id) {
    if (!confirm('确认删除这个套餐吗？')) return;

    try {
        await API.Setmeal.deleteSetmeal([id]);
        await Promise.all([syncSetMealsData(true), syncDashboardAndStatisticsData(true)]);
        renderApp();
    } catch (error) {
        alert(`删除套餐失败：${error.message || error}`);
    }
}

async function openAddEmployeeDialog() {
    if (!canManageEmployees()) {
        alert('当前账号没有员工管理权限');
        return;
    }

    const name = prompt('请输入员工姓名');
    if (!name || !name.trim()) return;

    const username = prompt('请输入登录用户名');
    if (!username || !username.trim()) return;

    const phone = prompt('请输入手机号');
    if (!phone || !phone.trim()) return;

    const accountTypeInput = prompt('请输入账号角色：2=商家管理员，3=商家员工', '3');
    if (!accountTypeInput) return;
    const accountType = Number(accountTypeInput);
    if (![ACCOUNT_TYPES.MERCHANT_ADMIN, ACCOUNT_TYPES.MERCHANT_STAFF].includes(accountType)) {
        alert('角色只能是 2 或 3');
        return;
    }

    const sex = prompt('请输入性别：1=男，2=女', '1') || '1';
    const idNumber = prompt('请输入身份证号');
    if (!idNumber || !idNumber.trim()) {
        alert('身份证号不能为空');
        return;
    }

    try {
        await API.Employee.addEmployee({
            name: name.trim(),
            username: username.trim(),
            phone: phone.trim(),
            sex: sex.trim(),
            idNumber: idNumber.trim(),
            status: 1,
            accountType
        });
        await syncEmployeesData(true);
        renderApp();
        alert('新增员工成功，默认密码为 123456');
    } catch (error) {
        alert(`新增员工失败：${error.message || error}`);
    }
}

async function editEmployee(id) {
    if (!canManageEmployees()) {
        alert('当前账号没有员工管理权限');
        return;
    }

    try {
        const detail = await API.Employee.getEmployeeById(id);
        const name = prompt('请输入员工姓名', detail.name || '');
        if (!name || !name.trim()) return;

        const username = prompt('请输入登录用户名', detail.username || '');
        if (!username || !username.trim()) return;

        const phone = prompt('请输入手机号', detail.phone || '');
        if (!phone || !phone.trim()) return;

        const accountTypeInput = prompt('请输入账号角色：2=商家管理员，3=商家员工', String(detail.accountType || ACCOUNT_TYPES.MERCHANT_STAFF));
        if (!accountTypeInput) return;
        const accountType = Number(accountTypeInput);
        if (![ACCOUNT_TYPES.MERCHANT_ADMIN, ACCOUNT_TYPES.MERCHANT_STAFF].includes(accountType)) {
            alert('角色只能是 2 或 3');
            return;
        }

        const sex = prompt('请输入性别：1=男，2=女', detail.sex || '1') || '1';
        const idNumber = prompt('请输入身份证号', detail.idNumber || '');
        if (!idNumber || !idNumber.trim()) {
            alert('身份证号不能为空');
            return;
        }

        await API.Employee.updateEmployee({
            id,
            name: name.trim(),
            username: username.trim(),
            phone: phone.trim(),
            sex: sex.trim(),
            idNumber: idNumber.trim(),
            status: detail.status,
            accountType
        });
        await syncEmployeesData(true);
        renderApp();
        alert('员工信息已更新');
    } catch (error) {
        alert(`更新员工失败：${error.message || error}`);
    }
}

async function toggleEmployeeStatus(id) {
    if (!canManageEmployees()) return;
    const target = employees.find((item) => item.id === id);
    if (!target) return;

    try {
        await API.Employee.updateStatus(target.status === 1 ? 0 : 1, id);
        await syncEmployeesData(true);
        renderApp();
    } catch (error) {
        alert(`切换员工状态失败：${error.message || error}`);
    }
}

async function toggleBusinessStatus() {
    if (!isMerchantAdmin()) {
        alert('当前账号不能切换商家营业状态');
        return;
    }
    if (!merchantProfile?.id) {
        alert('当前商家信息未加载完成');
        return;
    }

    const nextStatus = toNumber(merchantProfile.businessStatus, 1) === 1 ? 0 : 1;
    const confirmText = nextStatus === 1 ? '确认恢复营业吗？' : '确认将当前商家设为打烊吗？';
    if (!confirm(confirmText)) return;

    try {
        await API.Merchant.updateBusinessStatus(nextStatus, merchantProfile.id);
        await syncMerchantContext(true);
        renderApp();
    } catch (error) {
        alert(`切换营业状态失败：${error.message || error}`);
    }
}

async function acceptOrder(id) {
    try {
        await API.Order.confirm(id);
        await Promise.all([syncOrdersData(true), syncDashboardAndStatisticsData(true)]);
        if (selectedOrderId === id) {
            await hydrateOrderDetail(id);
        }
        renderApp();
    } catch (error) {
        alert(`接单失败：${error.message || error}`);
    }
}

async function updateOrderStatus(id, nextStatus) {
    try {
        if (nextStatus === 'delivering') {
            await API.Order.delivery(id);
        } else if (nextStatus === 'completed') {
            await API.Order.complete(id);
        }
        await Promise.all([syncOrdersData(true), syncDashboardAndStatisticsData(true)]);
        if (selectedOrderId === id) {
            await hydrateOrderDetail(id);
        }
        renderApp();
    } catch (error) {
        alert(`更新订单状态失败：${error.message || error}`);
    }
}

async function renderApp() {
    const app = document.getElementById('app');
    if (!app) return;

    const version = ++renderVersion;
    if (currentView !== 'login' && (!API.Token.exists() || !currentUser)) {
        currentView = 'login';
    }

    if (currentView === 'login') {
        document.title = '商家后台登录';
        app.innerHTML = renderLoginPage();
        document.getElementById('loginForm')?.addEventListener('submit', handleLogin);
        return;
    }

    try {
        await syncDataForView(currentView, false);
    } catch (error) {
        notifySyncError(error);
    }

    if (version !== renderVersion) return;

    let content = '';
    switch (currentView) {
        case 'dashboard':
            content = renderDashboard();
            break;
        case 'products':
            content = renderProducts();
            break;
        case 'orders':
            content = renderOrders();
            break;
        case 'setmeals':
            content = renderSetMeals();
            break;
        case 'statistics':
            content = renderStatistics();
            break;
        case 'employees':
            content = renderEmployees();
            break;
        case 'merchantProfile':
            content = renderMerchantProfile();
            break;
        default:
            content = renderDashboard();
            break;
    }

    app.innerHTML = `${renderSidebar()}<div class="main-layout">${renderTopBar()}${content}</div>${selectedOrderId ? renderOrderDetailDrawer() : ''}`;
    updateDocumentTitle();

    setTimeout(() => {
        if (currentView === 'dashboard') {
            createRevenueChart('revenueChart', statistics.weekData);
            createCategoryChart('categoryChart', statistics.categoryData);
        } else if (currentView === 'statistics') {
            createDualAxisChart('dualAxisChart', statistics.weekData);
            createHourlyChart('hourlyChart', statistics.hourlyOrders);
        }
    }, 80);
}

document.addEventListener('DOMContentLoaded', () => {
    renderApp();
});
