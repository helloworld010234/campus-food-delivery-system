package com.sky.context;

public final class BaseContext {

    private static final ThreadLocal<Long> CURRENT_ID = new ThreadLocal<>();
    private static final ThreadLocal<Long> CURRENT_MERCHANT_ID = new ThreadLocal<>();
    private static final ThreadLocal<Integer> CURRENT_ACCOUNT_TYPE = new ThreadLocal<>();

    private BaseContext() {
    }

    public static void setCurrentId(Long id) {
        CURRENT_ID.set(id);
    }

    public static Long getCurrentId() {
        return CURRENT_ID.get();
    }

    public static void removeCurrentId() {
        CURRENT_ID.remove();
    }

    public static void setCurrentMerchantId(Long merchantId) {
        CURRENT_MERCHANT_ID.set(merchantId);
    }

    public static Long getCurrentMerchantId() {
        return CURRENT_MERCHANT_ID.get();
    }

    public static void removeCurrentMerchantId() {
        CURRENT_MERCHANT_ID.remove();
    }

    public static void setCurrentAccountType(Integer accountType) {
        CURRENT_ACCOUNT_TYPE.set(accountType);
    }

    public static Integer getCurrentAccountType() {
        return CURRENT_ACCOUNT_TYPE.get();
    }

    public static void removeCurrentAccountType() {
        CURRENT_ACCOUNT_TYPE.remove();
    }

    public static void clear() {
        removeCurrentId();
        removeCurrentMerchantId();
        removeCurrentAccountType();
    }
}
