export const normalizeMerchantId = (value) => {
  if (value === undefined || value === null || value === "") {
    return "";
  }

  return String(value);
};

export const withMerchantScope = (params = {}, merchantId) => {
  const sourceMerchantId =
    merchantId !== undefined && merchantId !== null && merchantId !== ""
      ? merchantId
      : params.merchantId !== undefined &&
        params.merchantId !== null &&
        params.merchantId !== ""
      ? params.merchantId
      : params.shopId;

  const normalizedMerchantId = normalizeMerchantId(sourceMerchantId);
  if (!normalizedMerchantId) {
    return {
      ...params,
    };
  }

  return {
    ...params,
    merchantId: normalizedMerchantId,
    shopId: normalizedMerchantId,
  };
};

export const resolveMerchantIdFromState = (
  currentMerchantId,
  storeInfo = {},
  shopInfo = {}
) => {
  return normalizeMerchantId(
    currentMerchantId ||
      storeInfo.merchantId ||
      storeInfo.shopId ||
      shopInfo.merchantId ||
      shopInfo.shopId
  );
};

export const resolveMerchantIdFromOrder = (order = {}) => {
  return normalizeMerchantId(order.merchantId || order.shopId);
};
