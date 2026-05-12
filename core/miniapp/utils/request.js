import store from "../store";
import { baseUrl } from "./env";
import { clearSessionState, getStoredToken } from "./session.js";

let authRedirecting = false;

const redirectToLogin = (title) => {
  if (authRedirecting || typeof uni === "undefined") {
    return;
  }

  authRedirecting = true;
  uni.showToast({
    title,
    icon: "none",
  });
  setTimeout(() => {
    uni.redirectTo({
      url: "/pages/index/index",
      complete: () => {
        authRedirecting = false;
      },
    });
  }, 120);
};

const getAuthToken = () => {
  const storeInfo = store.state;
  const cachedToken = getStoredToken();
  const token = storeInfo.token || cachedToken;

  if (!storeInfo.token && token) {
    store.commit("setToken", token);
  }

  return token;
};

const buildHeaders = (token) => ({
  Accept: "application/json",
  "Content-Type": "application/json",
  authentication: token,
});

const OSS_HOST_PATTERN = /\.oss-[a-z0-9-]+\.aliyuncs\.com$/i;

const toDownloadProxyUrl = (value) => {
  if (typeof value !== "string") {
    return value;
  }

  const rawUrl = value.trim();
  if (!rawUrl || rawUrl.includes("/user/common/download?name=")) {
    return value;
  }

  const matched = rawUrl.match(/^https?:\/\/([^/?#]+)\/([^?#]+)(?:\?[^#]*)?$/i);
  if (!matched) {
    return value;
  }

  const host = matched[1];
  if (!OSS_HOST_PATTERN.test(host)) {
    return value;
  }

  const objectName = decodeURIComponent(matched[2] || "").replace(/^\/+/, "");
  if (!objectName) {
    return value;
  }

  return `${baseUrl}/user/common/download?name=${encodeURIComponent(objectName)}`;
};

const normalizeResponsePayload = (payload, visited = new WeakSet()) => {
  if (typeof payload === "string") {
    return toDownloadProxyUrl(payload);
  }

  if (Array.isArray(payload)) {
    return payload.map((item) => normalizeResponsePayload(item, visited));
  }

  if (!payload || typeof payload !== "object") {
    return payload;
  }

  if (visited.has(payload)) {
    return payload;
  }
  visited.add(payload);

  const normalized = {};
  Object.keys(payload).forEach((key) => {
    normalized[key] = normalizeResponsePayload(payload[key], visited);
  });
  return normalized;
};

const handleResponse = (res, resolve, reject, url) => {
  const { data, statusCode } = res;
  const normalizedData = normalizeResponsePayload(data);

  if (normalizedData.code == 200 || normalizedData.code === 1) {
    resolve(normalizedData);
    return;
  }

  if (statusCode === 401 || normalizedData.code === 401) {
    store.commit("setToken", "");
    clearSessionState();
    redirectToLogin("登录状态已失效");
  }

  reject({
    ...normalizedData,
    statusCode,
  });
};

const handleNetworkError = (err, url, reject) => {
  let message = err.errMsg || err.data || "网络请求失败";

  if (message.includes("timeout") || message.includes("超时")) {
    message = "请求超时，请检查网络后重试";
  } else if (message.includes("fail") || message.includes("无法连接")) {
    message = "网络连接失败，请检查网络设置";
  }

  uni.showToast({
    title: message,
    icon: "none",
  });

  reject({
    data: {
      msg: message,
      url,
    },
    err,
  });
};

// Public browse endpoints that do NOT require authentication.
// These match the backend JwtTokenUserInterceptor exclusions in
// WebMvcConfiguration.java (lines 53-59).
const ANONYMOUS_ALLOWLIST = [
  "/user/shop/status",
  "/user/shop/list",
  "/user/shop/getMerchantInfo",
  "/user/category/list",
  "/user/dish/list",
  "/user/setmeal/list",
  "/user/common/download",
];

const isAnonymousAllowed = (url) => {
  if (!url || typeof url !== "string") {
    return false;
  }
  return ANONYMOUS_ALLOWLIST.some((allowed) => url.startsWith(allowed));
};

// 参数: url 请求地址 params 请求参数 data 请求体 method 请求方式
export function request({ url = "", params = {}, data = null, method = "GET" }) {
  const requestData = data !== null ? data : params;
  const token = getAuthToken();
  const isLoginRequest = url === "/user/user/login";
  const allowAnonymous = isAnonymousAllowed(url);

  if (!isLoginRequest && !token && !allowAnonymous) {
    redirectToLogin("请先登录");
    return Promise.reject({
      data: {
        msg: "未登录，请先返回首页完成登录",
        url,
        statusCode: 401,
      },
    });
  }

  store.commit("setLodding", false);

  return new Promise((resolve, reject) => {
    uni.request({
      url: baseUrl + url,
      data: requestData,
      header: buildHeaders(token),
      method,
      timeout: 12000,
      success: (res) => handleResponse(res, resolve, reject, url),
      fail: (err) => handleNetworkError(err, url, reject),
    });
  });
}
