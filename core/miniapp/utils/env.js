// Environment configuration for miniapp API base URL.
const ENV_CONFIG = {
  development: "http://10.6.242.59:8081",
  production: "http://8.136.34.168",
};

const env =
  (typeof process !== "undefined" && process.env && process.env.NODE_ENV) ||
  "development";

const resolveOverrideBaseUrl = () => {
  if (typeof process === "undefined" || !process.env) {
    return "";
  }

  return (
    process.env.VUE_APP_API_BASE_URL ||
    process.env.UNI_APP_API_BASE_URL ||
    process.env.API_BASE_URL ||
    ""
  );
};

const normalizeBaseUrl = (url) => String(url || "").replace(/\/+$/, "");

const resolveRuntimeBaseUrl = () => {
  if (typeof uni === "undefined" || typeof uni.getStorageSync !== "function") {
    return "";
  }

  return uni.getStorageSync("SKY_API_BASE_URL") || "";
};

const runtimeBaseUrl = normalizeBaseUrl(resolveRuntimeBaseUrl());
const overrideBaseUrl = normalizeBaseUrl(resolveOverrideBaseUrl());
export const baseUrl =
  runtimeBaseUrl ||
  overrideBaseUrl ||
  normalizeBaseUrl(ENV_CONFIG[env] || ENV_CONFIG.development);
