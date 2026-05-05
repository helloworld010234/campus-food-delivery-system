package com.sky.service.impl;

import com.sky.constant.AccountTypeConstant;
import com.sky.context.BaseContext;
import com.sky.entity.Merchant;
import com.sky.exception.BaseException;
import com.sky.mapper.MerchantMapper;
import com.sky.properties.StorefrontProperties;
import com.sky.service.CampusService;
import com.sky.support.MultiMerchantSchemaSupport;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MerchantServiceImplScopeTest {

    @Mock
    private MerchantMapper merchantMapper;

    @Mock
    private CampusService campusService;

    @Mock
    private StorefrontProperties storefrontProperties;

    @Mock
    private MultiMerchantSchemaSupport schemaSupport;

    @InjectMocks
    private MerchantServiceImpl merchantService;

    @BeforeEach
    void setUp() {
        BaseContext.clear();
    }

    @AfterEach
    void tearDown() {
        BaseContext.clear();
    }

    @Test
    void getById_shouldRejectCrossMerchantRead_forMerchantAccount() {
        when(schemaSupport.supportsMerchantTable()).thenReturn(true);
        when(merchantMapper.getById(2L)).thenReturn(Merchant.builder().id(2L).campusId(1L).build());
        BaseContext.setCurrentAccountType(AccountTypeConstant.MERCHANT_ADMIN);
        BaseContext.setCurrentMerchantId(1L);

        assertThatThrownBy(() -> merchantService.getById(2L))
                .isInstanceOf(BaseException.class);
    }

    @Test
    void getById_shouldAllowCrossMerchantRead_forPlatformAccount() {
        when(schemaSupport.supportsMerchantTable()).thenReturn(true);
        when(merchantMapper.getById(2L)).thenReturn(Merchant.builder().id(2L).campusId(1L).build());
        BaseContext.setCurrentAccountType(AccountTypeConstant.PLATFORM_ADMIN);

        Merchant merchant = merchantService.getById(2L);

        assertThat(merchant.getId()).isEqualTo(2L);
    }
}
