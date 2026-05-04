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

const handleResponse = (res, resolve, reject, url) => {
  const { data, statusCode } = res;

  if (data.code == 200 || data.code === 1) {
    resolve(data);
    return;
  }

  if (statusCode === 401 || data.code === 401) {
    store.commit("setToken", "");
    clearSessionState();
    redirectToLogin("登录状态已失效");
  }

  reject({
    ...data,
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

// 参数: url 请求地址 params 请求参数 data 请求体 method 请求方式
export function request({ url = "", params = {}, data = null, method = "GET" }) {
  const requestData = data !== null ? data : params;
  const token = getAuthToken();
  const isLoginRequest = url === "/user/user/login";

  if (!isLoginRequest && !token) {
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
