package com.sky.constant;

/**
 * Numeric account-type codes shared by JWT claims and BaseContext.
 *
 * <p>Admin chain accounts (platform / merchant) are distinguished by these
 * values inside the same JWT structure. Student users are served through a
 * separate user-side JWT (see {@code JwtTokenUserInterceptor}) and never set
 * an admin {@code accountType}; the {@link #STUDENT_USER} marker is provided
 * for documentation and tests that want to express "user-side caller" without
 * inventing an ad-hoc constant.</p>
 */
public final class AccountTypeConstant {

    private AccountTypeConstant() {
    }

    /** Platform-level admin: global visibility across merchants. */
    public static final Integer PLATFORM_ADMIN = 1;

    /** Merchant admin: bound to a single merchant via {@code merchantId} claim. */
    public static final Integer MERCHANT_ADMIN = 2;

    /** Merchant staff: bound to a single merchant, behaves like merchant admin at scope layer. */
    public static final Integer MERCHANT_STAFF = 3;

    /**
     * Documentation marker for user-side (student) callers. Not stored in the
     * admin JWT or in {@code BaseContext.currentAccountType}; tests use it to
     * make permission-matrix expectations explicit.
     */
    public static final Integer STUDENT_USER = 0;
}
