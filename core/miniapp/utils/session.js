const SESSION_STORAGE_KEY = "sky-miniapp-session";
const DEFAULT_BASE_USER_INFO = {
  avatarUrl: "/static/imgDefault.png",
  nickName: "微信用户",
  gender: 0,
};

const canUseStorage = () => {
  return (
    typeof uni !== "undefined" &&
    uni !== null &&
    typeof uni.getStorageSync === "function" &&
    typeof uni.setStorageSync === "function"
  );
};

export const createDefaultSessionState = () => ({
  storeInfo: {},
  shopInfo: {},
  merchantList: [],
  currentMerchantId: "",
  baseUserInfo: {
    ...DEFAULT_BASE_USER_INFO,
  },
  addressBackUrl: "",
  shopPhone: "",
  token: "",
  deliveryFee: 0,
  gender: 0,
});

export const createDefaultBaseUserInfo = () => ({
  ...DEFAULT_BASE_USER_INFO,
});

export const normalizeBaseUserInfo = (userInfo = {}) => ({
  avatarUrl:
    userInfo && typeof userInfo.avatarUrl === "string" && userInfo.avatarUrl
      ? userInfo.avatarUrl
      : DEFAULT_BASE_USER_INFO.avatarUrl,
  nickName:
    userInfo && typeof userInfo.nickName === "string" && userInfo.nickName
      ? userInfo.nickName
      : DEFAULT_BASE_USER_INFO.nickName,
  gender: Number(
    userInfo && typeof userInfo.gender !== "undefined"
      ? userInfo.gender
      : DEFAULT_BASE_USER_INFO.gender
  ),
});

export const loadSessionState = () => {
  if (!canUseStorage()) {
    return {};
  }

  try {
    const cached = uni.getStorageSync(SESSION_STORAGE_KEY);
    if (!cached || typeof cached !== "object") {
      return {};
    }
    return {
      ...cached,
      baseUserInfo: normalizeBaseUserInfo(cached.baseUserInfo),
      merchantList: Array.isArray(cached.merchantList) ? cached.merchantList : [],
      currentMerchantId:
        typeof cached.currentMerchantId === "string"
          ? cached.currentMerchantId
          : "",
      token: typeof cached.token === "string" ? cached.token : "",
      deliveryFee: Number(cached.deliveryFee || 0),
      gender: Number(cached.gender || 0),
    };
  } catch (error) {
    return {};
  }
};

export const persistSessionState = (state = {}) => {
  if (!canUseStorage()) {
    return;
  }

  try {
    uni.setStorageSync(SESSION_STORAGE_KEY, {
      storeInfo: state.storeInfo || {},
      shopInfo: state.shopInfo || {},
      merchantList: Array.isArray(state.merchantList) ? state.merchantList : [],
      currentMerchantId: state.currentMerchantId || "",
      baseUserInfo: normalizeBaseUserInfo(state.baseUserInfo),
      addressBackUrl: state.addressBackUrl || "",
      shopPhone: state.shopPhone || "",
      token: state.token || "",
      deliveryFee: Number(state.deliveryFee || 0),
      gender: Number(state.gender || 0),
    });
  } catch (error) {}
};

export const clearSessionState = () => {
  if (!canUseStorage() || typeof uni.removeStorageSync !== "function") {
    return;
  }

  try {
    uni.removeStorageSync(SESSION_STORAGE_KEY);
  } catch (error) {}
};

export const getStoredToken = () => {
  const session = loadSessionState();
  return typeof session.token === "string" ? session.token : "";
};
