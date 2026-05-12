package com.sky.utils;

import com.sky.constant.AccountTypeConstant;
import com.sky.context.BaseContext;
import com.sky.exception.BaseException;

public final class MerchantScopeUtils {

    private MerchantScopeUtils() {
    }

    public static Long resolveQueryMerchantId(Long requestedMerchantId) {
        if (isMerchantAccount()) {
            Long currentMerchantId = getRequiredCurrentMerchantId();
            if (requestedMerchantId != null && !currentMerchantId.equals(requestedMerchantId)) {
                throw new BaseException("无权访问其他商户数据");
            }
            return currentMerchantId;
        }
        return requestedMerchantId;
    }

    public static Long resolveRequiredMerchantId(Long requestedMerchantId) {
        Long merchantId = resolveQueryMerchantId(requestedMerchantId);
        if (merchantId == null) {
            throw new BaseException("当前操作必须指定商户");
        }
        return merchantId;
    }

    public static void assertAccessible(Long merchantId) {
        if (!isMerchantAccount()) {
            return;
        }
        Long currentMerchantId = getRequiredCurrentMerchantId();
        if (merchantId != null && !currentMerchantId.equals(merchantId)) {
            throw new BaseException("无权访问其他商户数据");
        }
    }

    public static boolean isMerchantAccount() {
        Integer accountType = BaseContext.getCurrentAccountType();
        return AccountTypeConstant.MERCHANT_ADMIN.equals(accountType)
                || AccountTypeConstant.MERCHANT_STAFF.equals(accountType);
    }

    public static boolean isPlatformAccount() {
        return AccountTypeConstant.PLATFORM_ADMIN.equals(BaseContext.getCurrentAccountType());
    }

    private static Long getRequiredCurrentMerchantId() {
        Long currentMerchantId = BaseContext.getCurrentMerchantId();
        if (currentMerchantId == null) {
            throw new BaseException("商户账号未绑定商户");
        }
        return currentMerchantId;
    }
}
