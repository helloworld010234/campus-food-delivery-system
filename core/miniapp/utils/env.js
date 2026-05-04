// Environment configuration for miniapp API base URL.
const ENV_CONFIG = {
  development: "http://8.136.34.168",
  production: "http://8.136.34.168",
};

const env =
  (typeof process !== "undefined" && process.env && process.env.NODE_ENV) ||
  "development";

export const baseUrl = ENV_CONFIG[env] || ENV_CONFIG.development;
