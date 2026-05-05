package com.sky.security;

import com.sky.constant.AccountTypeConstant;
import com.sky.context.BaseContext;
import com.sky.exception.BaseException;
import com.sky.support.MultiMerchantSchemaSupport;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MerchantScopeGuardTest {

    @Mock
    private MultiMerchantSchemaSupport schemaSupport;

    private MerchantScopeGuard guard;

    @BeforeEach
    void setUp() {
        guard = new MerchantScopeGuard(schemaSupport);
        BaseContext.clear();
    }

    @AfterEach
    void tearDown() {
        BaseContext.clear();
    }

    // ---------------------------------------------------------------------
    // resolveAdminQueryMerchantId
    // ---------------------------------------------------------------------

    @Test
    void resolveAdminQueryMerchantId_shouldReturnRequested_forPlatformAccount() {
        BaseContext.setCurrentAccountType(AccountTypeConstant.PLATFORM_ADMIN);

        Long resolved = guard.resolveAdminQueryMerchantId(12L);

        assertThat(resolved).isEqualTo(12L);
    }

    @Test
    void resolveAdminQueryMerchantId_shouldReturnNullPassthrough_forPlatformAccount() {
        BaseContext.setCurrentAccountType(AccountTypeConstant.PLATFORM_ADMIN);

        Long resolved = guard.resolveAdminQueryMerchantId(null);

        assertThat(resolved).isNull();
    }

    @Test
    void resolveAdminQueryMerchantId_shouldThrow_whenMerchantAccountRequestsAnotherMerchant() {
        BaseContext.setCurrentAccountType(AccountTypeConstant.MERCHANT_ADMIN);
        BaseContext.setCurrentMerchantId(8L);

        assertThatThrownBy(() -> guard.resolveAdminQueryMerchantId(9L))
                .isInstanceOf(BaseException.class);
    }

    @Test
    void resolveAdminQueryMerchantId_shouldRejectMerchantStaffCrossMerchantQuery() {
        BaseContext.setCurrentAccountType(AccountTypeConstant.MERCHANT_STAFF);
        BaseContext.setCurrentMerchantId(8L);

        assertThatThrownBy(() -> guard.resolveAdminQueryMerchantId(99L))
                .isInstanceOf(BaseException.class);
    }

    @Test
    void resolveAdminQueryMerchantId_shouldFillCurrent_whenMerchantAccountPassesNull() {
        BaseContext.setCurrentAccountType(AccountTypeConstant.MERCHANT_ADMIN);
        BaseContext.setCurrentMerchantId(8L);

        Long resolved = guard.resolveAdminQueryMerchantId(null);

        assertThat(resolved).isEqualTo(8L);
    }

    // ---------------------------------------------------------------------
    // resolveMerchantWriteId
    // ---------------------------------------------------------------------

    @Test
    void resolveMerchantWriteId_shouldPassThrough_forPlatformAccount() {
        BaseContext.setCurrentAccountType(AccountTypeConstant.PLATFORM_ADMIN);

        Long resolved = guard.resolveMerchantWriteId(7L, null, "saveDish");

        assertThat(resolved).isEqualTo(7L);
    }

    @Test
    void resolveMerchantWriteId_shouldThrow_whenMerchantAccountTargetsAnother() {
        BaseContext.setCurrentAccountType(AccountTypeConstant.MERCHANT_ADMIN);
        BaseContext.setCurrentMerchantId(3L);

        assertThatThrownBy(() -> guard.resolveMerchantWriteId(4L, null, "saveDish"))
                .isInstanceOf(BaseException.class);
    }

    @Test
    void resolveMerchantWriteId_shouldFillCurrent_whenMerchantAccountPassesNull() {
        BaseContext.setCurrentAccountType(AccountTypeConstant.MERCHANT_STAFF);
        BaseContext.setCurrentMerchantId(3L);

        Long resolved = guard.resolveMerchantWriteId(null, null, "saveDish");

        assertThat(resolved).isEqualTo(3L);
    }

    @Test
    void resolveMerchantWriteId_shouldThrow_whenMerchantAccountHasNoContext() {
        BaseContext.setCurrentAccountType(AccountTypeConstant.MERCHANT_ADMIN);

        assertThatThrownBy(() -> guard.resolveMerchantWriteId(null, null, "saveDish"))
                .isInstanceOf(BaseException.class);
    }

    // ---------------------------------------------------------------------
    // requireExplicitMerchantId
    // ---------------------------------------------------------------------

    @Test
    void requireExplicitMerchantId_shouldThrow_whenCoreSchemaReadyAndMissingMerchant() {
        when(schemaSupport.isCoreSchemaReady()).thenReturn(true);

        assertThatThrownBy(() -> guard.requireExplicitMerchantId(null, null, "addShoppingCart"))
                .isInstanceOf(BaseException.class);
    }

    @Test
    void requireExplicitMerchantId_shouldReturnResolvedValue_whenMerchantProvided() {
        when(schemaSupport.isCoreSchemaReady()).thenReturn(true);

        Long resolved = guard.requireExplicitMerchantId(3L, null, "submitOrder");

        assertThat(resolved).isEqualTo(3L);
    }

    @Test
    void requireExplicitMerchantId_shouldFallBackToShopId_whenMerchantNull() {
        when(schemaSupport.isCoreSchemaReady()).thenReturn(true);

        Long resolved = guard.requireExplicitMerchantId(null, 9L, "submitOrder");

        assertThat(resolved).isEqualTo(9L);
    }

    @Test
    void requireExplicitMerchantId_shouldAllowNull_whenSchemaNotReady() {
        when(schemaSupport.isCoreSchemaReady()).thenReturn(false);

        Long resolved = guard.requireExplicitMerchantId(null, null, "addShoppingCart");

        assertThat(resolved).isNull();
    }

    // ---------------------------------------------------------------------
    // assertMerchantAccountCanAccess
    // ---------------------------------------------------------------------

    @Test
    void assertMerchantAccountCanAccess_shouldThrow_whenOwnerIsDifferent() {
        BaseContext.setCurrentAccountType(AccountTypeConstant.MERCHANT_STAFF);
        BaseContext.setCurrentMerchantId(11L);

        assertThatThrownBy(() -> guard.assertMerchantAccountCanAccess(7L, "order"))
                .isInstanceOf(BaseException.class);
    }

    @Test
    void assertMerchantAccountCanAccess_shouldThrow_whenOwnerIsNullForMerchantAccount() {
        BaseContext.setCurrentAccountType(AccountTypeConstant.MERCHANT_ADMIN);
        BaseContext.setCurrentMerchantId(11L);

        assertThatThrownBy(() -> guard.assertMerchantAccountCanAccess(null, "order"))
                .isInstanceOf(BaseException.class);
    }

    @Test
    void assertMerchantAccountCanAccess_shouldAllow_whenOwnerMatchesMerchantAccount() {
        BaseContext.setCurrentAccountType(AccountTypeConstant.MERCHANT_STAFF);
        BaseContext.setCurrentMerchantId(11L);

        guard.assertMerchantAccountCanAccess(11L, "order");
    }

    @Test
    void assertMerchantAccountCanAccess_shouldAllow_forPlatformAccount() {
        BaseContext.setCurrentAccountType(AccountTypeConstant.PLATFORM_ADMIN);

        guard.assertMerchantAccountCanAccess(99L, "order");
    }

    @Test
    void assertMerchantAccountCanAccess_shouldAllow_whenAccountTypeMissing() {
        // public-browse style entry: no account type populated. Guard should
        // not throw because the rule applies to merchant accounts only.
        guard.assertMerchantAccountCanAccess(99L, "publicShop");
    }

    // ---------------------------------------------------------------------
    // assertSameMerchant
    // ---------------------------------------------------------------------

    @Test
    void assertSameMerchant_shouldAllowMatchingIds() {
        guard.assertSameMerchant(5L, 5L, "cartItem");
    }

    @Test
    void assertSameMerchant_shouldThrowOnMismatch() {
        assertThatThrownBy(() -> guard.assertSameMerchant(5L, 6L, "cartItem"))
                .isInstanceOf(BaseException.class);
    }

    @Test
    void assertSameMerchant_shouldThrowOnNull() {
        assertThatThrownBy(() -> guard.assertSameMerchant(null, 6L, "cartItem"))
                .isInstanceOf(BaseException.class);
        assertThatThrownBy(() -> guard.assertSameMerchant(5L, null, "cartItem"))
                .isInstanceOf(BaseException.class);
    }

    // ---------------------------------------------------------------------
    // requireMerchantContext
    // ---------------------------------------------------------------------

    @Test
    void requireMerchantContext_shouldReturnContext_whenSchemaReady() {
        when(schemaSupport.isCoreSchemaReady()).thenReturn(true);
        BaseContext.setCurrentMerchantId(4L);

        Long resolved = guard.requireMerchantContext("submitOrder");

        assertThat(resolved).isEqualTo(4L);
    }

    @Test
    void requireMerchantContext_shouldThrow_whenSchemaReadyButMissing() {
        when(schemaSupport.isCoreSchemaReady()).thenReturn(true);

        assertThatThrownBy(() -> guard.requireMerchantContext("submitOrder"))
                .isInstanceOf(BaseException.class);
    }

    @Test
    void requireMerchantContext_shouldReturnNull_whenSchemaNotReady() {
        when(schemaSupport.isCoreSchemaReady()).thenReturn(false);

        Long resolved = guard.requireMerchantContext("submitOrder");

        assertThat(resolved).isNull();
    }

    // ---------------------------------------------------------------------
    // public-browse / missing-account-type boundary
    // ---------------------------------------------------------------------

    @Test
    void resolveAdminQueryMerchantId_shouldPassThrough_whenAccountTypeMissing() {
        // No account type populated (e.g. infrastructure code path before
        // interceptor runs). Guard must not pretend the caller is a merchant
        // and must not throw for null id.
        Long resolved = guard.resolveAdminQueryMerchantId(null);

        assertThat(resolved).isNull();
    }

    @Test
    void resolveAdminQueryMerchantId_shouldPassThrough_whenAccountTypeMissingAndIdProvided() {
        Long resolved = guard.resolveAdminQueryMerchantId(42L);

        assertThat(resolved).isEqualTo(42L);
    }

    @Test
    void resolveMerchantWriteId_shouldPassThrough_whenAccountTypeMissing() {
        // Same rationale as the query case - non-merchant callers retain the
        // requested id without triggering the cross-merchant denial branch.
        Long resolved = guard.resolveMerchantWriteId(15L, null, "saveDish");

        assertThat(resolved).isEqualTo(15L);
    }

    @Test
    void requireMerchantContext_shouldReturnNull_whenSchemaNotReadyAndNoContext() {
        // Compatibility mode: legacy single-merchant behaviour must keep
        // working even with no current merchant id populated.
        when(schemaSupport.isCoreSchemaReady()).thenReturn(false);

        Long resolved = guard.requireMerchantContext("publicBrowse");

        assertThat(resolved).isNull();
    }

    @Test
    void requireExplicitMerchantId_shouldPassMerchantId_whenBothProvided() {
        // merchantId wins over shopId when both supplied.
        when(schemaSupport.isCoreSchemaReady()).thenReturn(true);

        Long resolved = guard.requireExplicitMerchantId(3L, 7L, "submitOrder");

        assertThat(resolved).isEqualTo(3L);
    }
}
