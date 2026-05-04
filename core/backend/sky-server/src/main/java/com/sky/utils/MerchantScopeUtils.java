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
                throw new BaseException("鏃犳潈璁块棶鍏朵粬鍟嗘埛鏁版嵁");
            }
            return currentMerchantId;
        }
        return requestedMerchantId;
    }

    public static Long resolveRequiredMerchantId(Long requestedMerchantId) {
        Long merchantId = resolveQueryMerchantId(requestedMerchantId);
        if (merchantId == null) {
            throw new BaseException("褰撳墠鎿嶄綔蹇呴』鎸囧畾鍟嗘埛");
        }
        return merchantId;
    }

    public static void assertAccessible(Long merchantId) {
        if (!isMerchantAccount()) {
            return;
        }
        Long currentMerchantId = getRequiredCurrentMerchantId();
        if (merchantId != null && !currentMerchantId.equals(merchantId)) {
            throw new BaseException("鏃犳潈璁块棶鍏朵粬鍟嗘埛鏁版嵁");
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
