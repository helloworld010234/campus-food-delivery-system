package com.sky.security;

import com.sky.constant.AccountTypeConstant;
import com.sky.context.BaseContext;
import com.sky.exception.BaseException;
import com.sky.support.MultiMerchantSchemaSupport;
import com.sky.utils.MerchantScopeUtils;
import org.springframework.stereotype.Component;

/**
 * Centralised guard that expresses every merchant-scope boundary used by the
 * service layer. Other agents must reuse these methods rather than scattering
 * account-type checks across services.
 *
 * <h2>Permission matrix</h2>
 * <pre>
 *   Account type      | Merchant context | Cross-merchant read | Cross-merchant write | Notes
 *   ------------------+------------------+---------------------+----------------------+-----------------------------------------------
 *   PLATFORM_ADMIN    | optional         | yes                 | yes (explicit id)    | global visibility, scope by request param only
 *   MERCHANT_ADMIN    | required         | denied              | denied               | resolveAdminQueryMerchantId throws on mismatch
 *   MERCHANT_STAFF    | required         | denied              | denied               | same rules as MERCHANT_ADMIN at this layer
 *   STUDENT_USER      | implicit         | n/a                 | n/a                  | user-side JWT, interceptor populates currentId
 *                     |                  |                     |                      | only; this guard is bypassed for public browse
 *                     |                  |                     |                      | and applied per call for private user endpoints
 *
 *   Endpoint category | Auth boundary
 *   ------------------+--------------------------------------------------------------
 *   public browse     | shop / category list / dish list / setmeal list - no guard
 *                     | (intentionally allowed by product rules)
 *   private user      | cart / submit order / pay / history / detail / cancel /
 *                       reorder / reminder - require user JWT, plus
 *                       requireExplicitMerchantId() when isCoreSchemaReady() is true
 *   admin chain       | employee / merchant / catalog / order / report / workspace -
 *                       require admin JWT and the matching guard helper below
 *
 *   Failure mode      | Behaviour
 *   ------------------+--------------------------------------------------------------
 *   missing JWT       | interceptor returns 401, BaseContext.clear() runs in finally
 *   blacklisted JWT   | interceptor returns 401 before BaseContext is populated
 *   missing merchant  | guard throws BaseException for merchant accounts
 *   wrong merchant id | guard throws BaseException, never silently overwrites
 *   schema not ready  | requireExplicitMerchantId returns null (single-merchant fallback)
 * </pre>
 *
 * <h2>Compatibility mode</h2>
 * When {@link MultiMerchantSchemaSupport#isCoreSchemaReady()} returns false the
 * project runs in single-merchant fallback. The guard intentionally does not
 * throw on a missing merchant id in that mode so the legacy behaviour keeps
 * working.
 *
 * <h2>Test isolation</h2>
 * Tests using this guard or {@link BaseContext} must call
 * {@link BaseContext#clear()} in both {@code @BeforeEach} and {@code @AfterEach}
 * to avoid ThreadLocal leakage between tests.
 */
@Component
public class MerchantScopeGuard {

    private final MultiMerchantSchemaSupport schemaSupport;

    public MerchantScopeGuard(MultiMerchantSchemaSupport schemaSupport) {
        this.schemaSupport = schemaSupport;
    }

    /**
     * Resolve the merchant id required for a private write/read on a
     * multi-merchant private endpoint. When the core schema is not ready the
     * caller may still operate with a null id (single-merchant fallback).
     */
    public Long requireExplicitMerchantId(Long merchantId, Long shopId, String operation) {
        Long resolved = merchantId != null ? merchantId : shopId;
        if (schemaSupport.isCoreSchemaReady() && resolved == null) {
            throw new BaseException(operation + " requires merchantId or shopId");
        }
        return resolved;
    }

    /**
     * Resolve the merchant id for an admin LIST/QUERY. Platform accounts may
     * pass any id (including null = global). Merchant accounts must either pass
     * their own id or null; passing another merchant's id is denied.
     */
    public Long resolveAdminQueryMerchantId(Long requestedMerchantId) {
        return MerchantScopeUtils.resolveQueryMerchantId(requestedMerchantId);
    }

    /**
     * Resolve the merchant id used for an admin write that targets a single
     * merchant. Platform accounts pass through. Merchant accounts must provide
     * their own id (or null, which is filled in from the bound context).
     */
    public Long resolveMerchantWriteId(Long requestedMerchantId, Long shopId, String operation) {
        Long resolved = requestedMerchantId != null ? requestedMerchantId : shopId;
        if (!MerchantScopeUtils.isMerchantAccount()) {
            return resolved;
        }

        Long currentMerchantId = requireCurrentMerchant(operation);
        if (resolved == null) {
            return currentMerchantId;
        }
        if (!currentMerchantId.equals(resolved)) {
            throw new BaseException("No permission to access other merchant " + operation);
        }
        return currentMerchantId;
    }

    /**
     * Assert the current account may access an existing record owned by the
     * given merchant. Platform accounts pass through. Merchant accounts must
     * own the resource.
     */
    public void assertMerchantAccountCanAccess(Long ownerMerchantId, String resourceName) {
        if (!MerchantScopeUtils.isMerchantAccount()) {
            return;
        }

        Long currentMerchantId = requireCurrentMerchant(resourceName);
        if (ownerMerchantId == null || !currentMerchantId.equals(ownerMerchantId)) {
            throw new BaseException("No permission to access other merchant " + resourceName);
        }
    }

    /**
     * Assert two domain records belong to the same merchant. Used when joining
     * cart items with dishes/setmeals or matching order rows.
     */
    public void assertSameMerchant(Long expectedMerchantId, Long actualMerchantId, String resourceName) {
        if (expectedMerchantId == null || actualMerchantId == null || !expectedMerchantId.equals(actualMerchantId)) {
            throw new BaseException(resourceName + " merchant mismatch");
        }
    }

    /**
     * Assert that a merchant context exists for the current thread. Useful for
     * private user endpoints that must not run without a resolved merchant in
     * multi-merchant mode while remaining harmless in fallback mode.
     */
    public Long requireMerchantContext(String operation) {
        if (!schemaSupport.isCoreSchemaReady()) {
            return BaseContext.getCurrentMerchantId();
        }
        return requireCurrentMerchant(operation);
    }

    /**
     * Assert the current account is a merchant admin (not staff).
     * Used for operations that require full merchant management privileges.
     */
    public void assertMerchantAdmin(String operation) {
        if (MerchantScopeUtils.isMerchantAccount()
                && !AccountTypeConstant.MERCHANT_ADMIN.equals(BaseContext.getCurrentAccountType())) {
            throw new BaseException("该操作需要商家管理员权限: " + operation);
        }
    }

    private Long requireCurrentMerchant(String operation) {
        Long currentMerchantId = BaseContext.getCurrentMerchantId();
        if (currentMerchantId == null) {
            throw new BaseException(operation + " requires current merchant context");
        }
        return currentMerchantId;
    }
}
