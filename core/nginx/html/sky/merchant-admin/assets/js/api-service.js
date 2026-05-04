// API service for the merchant admin console.
// This file wraps the phase-1 multi-merchant backend endpoints.

const DEFAULT_BACKEND_ORIGIN = 'http://localhost:8080';

function getWindowLocation() {
    if (typeof window === 'undefined' || !window.location) {
        return null;
    }
    return window.location;
}

function getFrontendOrigin() {
    const location = getWindowLocation();
    if (!location || !/^https?:$/i.test(location.protocol)) {
        return DEFAULT_BACKEND_ORIGIN;
    }
    return location.origin || DEFAULT_BACKEND_ORIGIN;
}

function shouldUseAdminProxy() {
    const location = getWindowLocation();
    if (!location || !/^https?:$/i.test(location.protocol)) {
        return false;
    }
    return location.port !== '8080';
}

function normalizePath(path) {
    if (!path) return '/';
    return path.startsWith('/') ? path : `/${path}`;
}

function rewriteAdminPath(path) {
    const normalizedPath = normalizePath(path);
    if (shouldUseAdminProxy() && normalizedPath.startsWith('/admin/')) {
        return `/api/${normalizedPath.slice('/admin/'.length)}`;
    }
    return normalizedPath;
}

function resolveRequestUrl(path) {
    return `${getFrontendOrigin()}${rewriteAdminPath(path)}`;
}

function resolveAssetUrl(path) {
    if (!path) return path;
    if (/^(https?:)?\/\//i.test(path) || path.startsWith('data:')) return path;
    if (path.startsWith('./') || path.startsWith('../')) return path;
    return `${getFrontendOrigin()}${rewriteAdminPath(path)}`;
}

function parseFileName(contentDisposition, fallbackFileName) {
    if (!contentDisposition) return fallbackFileName;

    const utf8Match = contentDisposition.match(/filename\*=UTF-8''([^;]+)/i);
    if (utf8Match?.[1]) {
        return decodeURIComponent(utf8Match[1]);
    }

    const basicMatch = contentDisposition.match(/filename="?([^";]+)"?/i);
    if (basicMatch?.[1]) {
        return basicMatch[1];
    }

    return fallbackFileName;
}

const API_CONFIG = {
    baseURL: getFrontendOrigin(),
    timeout: 10000,
    tokenKey: 'token',
    useAdminProxy: shouldUseAdminProxy(),
    resolveRequestUrl,
    resolveAssetUrl
};

const TokenManager = {
    get() {
        return localStorage.getItem(API_CONFIG.tokenKey);
    },
    set(token) {
        localStorage.setItem(API_CONFIG.tokenKey, token);
    },
    remove() {
        localStorage.removeItem(API_CONFIG.tokenKey);
    },
    exists() {
        return Boolean(this.get());
    }
};

class HttpClient {
    constructor(config) {
        this.baseURL = config.baseURL;
        this.timeout = config.timeout;
    }

    buildHeaders(extraHeaders = {}) {
        const headers = {
            'Content-Type': 'application/json',
            ...extraHeaders
        };

        const token = TokenManager.get();
        if (token) {
            headers.token = token;
        }

        return headers;
    }

    toQueryString(params = {}) {
        return Object.entries(params)
            .filter(([, value]) => value !== undefined && value !== null && value !== '')
            .map(([key, value]) => `${encodeURIComponent(key)}=${encodeURIComponent(value)}`)
            .join('&');
    }

    async request(path, options = {}) {
        const queryString = this.toQueryString(options.params);
        const url = `${API_CONFIG.resolveRequestUrl(path)}${queryString ? `?${queryString}` : ''}`;

        const controller = new AbortController();
        const timeoutId = setTimeout(() => controller.abort(), this.timeout);

        try {
            const response = await fetch(url, {
                method: options.method || 'GET',
                headers: this.buildHeaders(options.headers),
                body: options.data !== undefined ? JSON.stringify(options.data) : undefined,
                signal: controller.signal
            });

            const result = await response.json();
            if (result.code === 1) {
                return result.data;
            }

            throw new Error(result.msg || '请求失败');
        } catch (error) {
            if (error.name === 'AbortError') {
                throw new Error('请求超时，请检查后端服务是否可用');
            }
            throw error;
        } finally {
            clearTimeout(timeoutId);
        }
    }

    get(path, params) {
        return this.request(path, { method: 'GET', params });
    }

    post(path, data, params) {
        return this.request(path, { method: 'POST', data, params });
    }

    put(path, data, params) {
        return this.request(path, { method: 'PUT', data, params });
    }

    delete(path, params) {
        return this.request(path, { method: 'DELETE', params });
    }

    async download(path, params = {}, fallbackFileName = 'download.xlsx') {
        const queryString = this.toQueryString(params);
        const url = `${API_CONFIG.resolveRequestUrl(path)}${queryString ? `?${queryString}` : ''}`;
        const controller = new AbortController();
        const timeoutId = setTimeout(() => controller.abort(), this.timeout);

        try {
            const response = await fetch(url, {
                method: 'GET',
                headers: {
                    token: TokenManager.get() || ''
                },
                signal: controller.signal
            });

            if (!response.ok) {
                throw new Error(`下载失败（${response.status}）`);
            }

            const blob = await response.blob();
            const fileName = parseFileName(
                response.headers.get('Content-Disposition'),
                fallbackFileName
            );
            const objectUrl = window.URL.createObjectURL(blob);
            const link = document.createElement('a');
            link.href = objectUrl;
            link.download = fileName;
            document.body.appendChild(link);
            link.click();
            link.remove();
            window.URL.revokeObjectURL(objectUrl);
        } catch (error) {
            if (error.name === 'AbortError') {
                throw new Error('下载超时，请稍后重试');
            }
            throw error;
        } finally {
            clearTimeout(timeoutId);
        }
    }
}

const http = new HttpClient(API_CONFIG);

const EmployeeAPI = {
    login(username, password) {
        return http.post('/admin/employee/login', { username, password });
    },
    logout() {
        return http.post('/admin/employee/logout');
    },
    getEmployeeList(params) {
        return http.get('/admin/employee/page', params);
    },
    addEmployee(data) {
        return http.post('/admin/employee', data);
    },
    updateEmployee(data) {
        return http.put('/admin/employee', data);
    },
    updateStatus(status, id) {
        return http.post(`/admin/employee/status/${status}`, undefined, { id });
    },
    getEmployeeById(id) {
        return http.get(`/admin/employee/${id}`);
    }
};

const DishAPI = {
    getDishList(params) {
        return http.get('/admin/dish/page', params);
    },
    addDish(data) {
        return http.post('/admin/dish', data);
    },
    updateDish(data) {
        return http.put('/admin/dish', data);
    },
    deleteDish(ids) {
        return http.delete('/admin/dish', { ids: ids.join(',') });
    },
    getDishById(id) {
        return http.get(`/admin/dish/${id}`);
    },
    updateStatus(status, id) {
        return http.post(`/admin/dish/status/${status}`, undefined, { id });
    },
    getDishByCategoryId(categoryId) {
        return http.get('/admin/dish/list', { categoryId });
    }
};

const CategoryAPI = {
    getCategoryList(params) {
        return http.get('/admin/category/page', params);
    },
    getCategoryByType(type) {
        return http.get('/admin/category/list', { type });
    },
    addCategory(data) {
        return http.post('/admin/category', data);
    },
    updateCategory(data) {
        return http.put('/admin/category', data);
    },
    deleteCategory(id) {
        return http.delete('/admin/category', { id });
    },
    updateStatus(status, id) {
        return http.post(`/admin/category/status/${status}`, undefined, { id });
    }
};

const SetmealAPI = {
    getSetmealList(params) {
        return http.get('/admin/setmeal/page', params);
    },
    addSetmeal(data) {
        return http.post('/admin/setmeal', data);
    },
    updateSetmeal(data) {
        return http.put('/admin/setmeal', data);
    },
    deleteSetmeal(ids) {
        return http.delete('/admin/setmeal', { ids: ids.join(',') });
    },
    getSetmealById(id) {
        return http.get(`/admin/setmeal/${id}`);
    },
    updateStatus(status, id) {
        return http.post(`/admin/setmeal/status/${status}`, undefined, { id });
    }
};

const OrderAPI = {
    getOrderList(params) {
        return http.get('/admin/order/conditionSearch', params);
    },
    getOrderStatistics() {
        return http.get('/admin/order/statistics');
    },
    getOrderDetail(id) {
        return http.get(`/admin/order/details/${id}`);
    },
    confirm(id) {
        return http.put('/admin/order/confirm', { id });
    },
    rejection(data) {
        return http.put('/admin/order/rejection', data);
    },
    cancel(data) {
        return http.put('/admin/order/cancel', data);
    },
    delivery(id) {
        return http.put(`/admin/order/delivery/${id}`);
    },
    complete(id) {
        return http.put(`/admin/order/complete/${id}`);
    }
};

const WorkspaceAPI = {
    getTodayData() {
        return http.get('/admin/workspace/businessData');
    },
    getOrderOverview() {
        return http.get('/admin/workspace/overviewOrders');
    },
    getDishOverview() {
        return http.get('/admin/workspace/overviewDishes');
    },
    getSetmealOverview() {
        return http.get('/admin/workspace/overviewSetmeals');
    }
};

const ReportAPI = {
    getTurnoverStatistics(begin, end) {
        return http.get('/admin/report/turnoverStatistics', { begin, end });
    },
    getUserStatistics(begin, end) {
        return http.get('/admin/report/userStatistics', { begin, end });
    },
    getOrderStatistics(begin, end) {
        return http.get('/admin/report/ordersStatistics', { begin, end });
    },
    getTop10(begin, end) {
        return http.get('/admin/report/top10', { begin, end });
    },
    async exportBusinessData() {
        try {
            await http.download('/admin/report/export', {}, '运营数据报表.xlsx');
        } catch (error) {
            console.error('Failed to export business data:', error);
            if (typeof window !== 'undefined' && typeof window.alert === 'function') {
                window.alert(`导出报表失败：${error.message || error}`);
            }
        }
    }
};

const ShopAPI = {
    getStatus() {
        return http.get('/admin/shop/status');
    },
    setStatus(status) {
        return http.put(`/admin/shop/${status}`);
    }
};

const MerchantAPI = {
    getMerchantPage(params) {
        return http.get('/admin/merchant/page', params);
    },
    getById(id) {
        return http.get(`/admin/merchant/${id}`);
    },
    update(data) {
        return http.put('/admin/merchant', data);
    },
    updateStatus(status, id) {
        return http.post(`/admin/merchant/status/${status}`, undefined, { id });
    },
    updateBusinessStatus(status, id) {
        return http.post(`/admin/merchant/business-status/${status}`, undefined, { id });
    }
};

const CommonAPI = {
    async upload(file) {
        const formData = new FormData();
        formData.append('file', file);

        const response = await fetch(API_CONFIG.resolveRequestUrl('/admin/common/upload'), {
            method: 'POST',
            headers: {
                token: TokenManager.get() || ''
            },
            body: formData
        });

        const result = await response.json();
        if (result.code === 1) {
            return result.data;
        }
        throw new Error(result.msg || '上传失败');
    }
};

const API = {
    Employee: EmployeeAPI,
    Dish: DishAPI,
    Category: CategoryAPI,
    Setmeal: SetmealAPI,
    Order: OrderAPI,
    Workspace: WorkspaceAPI,
    Report: ReportAPI,
    Shop: ShopAPI,
    Merchant: MerchantAPI,
    Common: CommonAPI,
    Token: TokenManager,
    config: API_CONFIG
};

if (typeof window !== 'undefined') {
    window.API = API;
}
