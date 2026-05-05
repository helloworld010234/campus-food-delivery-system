package com.sky.service.impl;

import com.sky.constant.AccountTypeConstant;
import com.sky.context.BaseContext;
import com.sky.exception.BaseException;
import com.sky.mapper.OrdersMapper;
import com.sky.mapper.UserMapper;
import com.sky.security.MerchantScopeGuard;
import com.sky.service.WorkspaceService;
import com.sky.support.MultiMerchantSchemaSupport;
import com.sky.vo.OrderReportVO;
import com.sky.vo.SalesTop10ReportVO;
import com.sky.vo.TurnoverReportVO;
import com.sky.vo.UserReportVO;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Verifies platform vs merchant report scope semantics for {@link ReportServiceImpl}.
 *
 * <ul>
 *   <li>Platform admin -> map.merchantId == null (global aggregate).</li>
 *   <li>Merchant admin -> map.merchantId == bound id (single merchant view).</li>
 *   <li>Merchant cannot pretend to be another merchant in this layer because the
 *       guard intercepts cross-merchant requests; covered by the dedicated
 *       guard test as well.</li>
 *   <li>User statistics also threads merchantId so merchant accounts cannot see
 *       global registration counts.</li>
 * </ul>
 */
@ExtendWith(MockitoExtension.class)
class ReportServiceImplScopeTest {

    @Mock
    private OrdersMapper ordersMapper;

    @Mock
    private UserMapper userMapper;

    @Mock
    private WorkspaceService workspaceService;

    @Mock
    private MultiMerchantSchemaSupport schemaSupport;

    private MerchantScopeGuard merchantScopeGuard;

    @InjectMocks
    private ReportServiceImpl reportService;

    @BeforeEach
    void setUp() {
        BaseContext.clear();
        merchantScopeGuard = new MerchantScopeGuard(schemaSupport);
        // wire the real guard (constructed with the schema support mock) into
        // the service under test so that we exercise the production path.
        try {
            java.lang.reflect.Field guardField = ReportServiceImpl.class.getDeclaredField("merchantScopeGuard");
            guardField.setAccessible(true);
            guardField.set(reportService, merchantScopeGuard);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException(e);
        }
    }

    @AfterEach
    void tearDown() {
        BaseContext.clear();
    }

    @Test
    void getTurnoverStatistics_shouldUseGlobalScope_forPlatformAdmin() {
        BaseContext.setCurrentAccountType(AccountTypeConstant.PLATFORM_ADMIN);
        when(ordersMapper.sumByMap(anyMap())).thenReturn(100.0);

        TurnoverReportVO vo = reportService.getTurnoverStatistics(LocalDate.now().minusDays(2), LocalDate.now());

        assertThat(vo).isNotNull();
        ArgumentCaptor<Map<String, Object>> captor = ArgumentCaptor.forClass(Map.class);
        verify(ordersMapper, atLeastOnce()).sumByMap(captor.capture());
        // platform admin -> null merchantId -> global aggregate at the mapper.
        captor.getAllValues().forEach(map -> assertThat(map.get("merchantId")).isNull());
    }

    @Test
    void getTurnoverStatistics_shouldScopeToBoundMerchant_forMerchantAdmin() {
        BaseContext.setCurrentAccountType(AccountTypeConstant.MERCHANT_ADMIN);
        BaseContext.setCurrentMerchantId(7L);
        when(ordersMapper.sumByMap(anyMap())).thenReturn(50.0);

        reportService.getTurnoverStatistics(LocalDate.now().minusDays(1), LocalDate.now());

        ArgumentCaptor<Map<String, Object>> captor = ArgumentCaptor.forClass(Map.class);
        verify(ordersMapper, atLeastOnce()).sumByMap(captor.capture());
        captor.getAllValues().forEach(map -> assertThat(map.get("merchantId")).isEqualTo(7L));
    }

    @Test
    void getOrderStatistics_shouldThreadMerchantIdIntoCountByMap_forMerchantAccount() {
        BaseContext.setCurrentAccountType(AccountTypeConstant.MERCHANT_STAFF);
        BaseContext.setCurrentMerchantId(3L);
        when(ordersMapper.countByMap(anyMap())).thenReturn(5);

        OrderReportVO vo = reportService.getOrderStatistics(LocalDate.now().minusDays(1), LocalDate.now());

        assertThat(vo).isNotNull();
        ArgumentCaptor<Map<String, Object>> captor = ArgumentCaptor.forClass(Map.class);
        verify(ordersMapper, atLeastOnce()).countByMap(captor.capture());
        captor.getAllValues().forEach(map -> assertThat(map.get("merchantId")).isEqualTo(3L));
    }

    @Test
    void getOrderStatistics_shouldUseGlobalScope_forPlatformAdmin() {
        BaseContext.setCurrentAccountType(AccountTypeConstant.PLATFORM_ADMIN);
        when(ordersMapper.countByMap(anyMap())).thenReturn(2);

        reportService.getOrderStatistics(LocalDate.now().minusDays(1), LocalDate.now());

        ArgumentCaptor<Map<String, Object>> captor = ArgumentCaptor.forClass(Map.class);
        verify(ordersMapper, atLeastOnce()).countByMap(captor.capture());
        captor.getAllValues().forEach(map -> assertThat(map.get("merchantId")).isNull());
    }

    @Test
    void getSalesTop10_shouldPassNullMerchantId_forPlatformAdmin() {
        BaseContext.setCurrentAccountType(AccountTypeConstant.PLATFORM_ADMIN);
        when(ordersMapper.getSalesTop10(any(), any(), any())).thenReturn(Collections.emptyList());

        SalesTop10ReportVO vo = reportService.getSalesTop10(LocalDate.now().minusDays(1), LocalDate.now());

        assertThat(vo).isNotNull();
        verify(ordersMapper).getSalesTop10(any(LocalDateTime.class), any(LocalDateTime.class), eq(null));
    }

    @Test
    void getSalesTop10_shouldPassBoundMerchantId_forMerchantAccount() {
        BaseContext.setCurrentAccountType(AccountTypeConstant.MERCHANT_ADMIN);
        BaseContext.setCurrentMerchantId(11L);
        when(ordersMapper.getSalesTop10(any(), any(), eq(11L))).thenReturn(Collections.emptyList());

        reportService.getSalesTop10(LocalDate.now().minusDays(1), LocalDate.now());

        verify(ordersMapper).getSalesTop10(any(LocalDateTime.class), any(LocalDateTime.class), eq(11L));
    }

    @Test
    void getUserStatistics_shouldThreadMerchantId_forMerchantAdmin() {
        BaseContext.setCurrentAccountType(AccountTypeConstant.MERCHANT_ADMIN);
        BaseContext.setCurrentMerchantId(4L);
        when(userMapper.getUserByMap(anyMap())).thenReturn(2);

        UserReportVO vo = reportService.getUserSratistics(LocalDate.now().minusDays(1), LocalDate.now());

        assertThat(vo).isNotNull();
        ArgumentCaptor<Map<String, Object>> captor = ArgumentCaptor.forClass(Map.class);
        verify(userMapper, atLeastOnce()).getUserByMap(captor.capture());
        captor.getAllValues().forEach(map -> assertThat(map.get("merchantId")).isEqualTo(4L));
    }

    @Test
    void getUserStatistics_shouldUseGlobalScope_forPlatformAdmin() {
        BaseContext.setCurrentAccountType(AccountTypeConstant.PLATFORM_ADMIN);
        when(userMapper.getUserByMap(anyMap())).thenReturn(2);

        reportService.getUserSratistics(LocalDate.now().minusDays(1), LocalDate.now());

        ArgumentCaptor<Map<String, Object>> captor = ArgumentCaptor.forClass(Map.class);
        verify(userMapper, atLeastOnce()).getUserByMap(captor.capture());
        // global aggregate is signalled by null merchantId.
        captor.getAllValues().forEach(map -> assertThat(map.get("merchantId")).isNull());
    }
}
