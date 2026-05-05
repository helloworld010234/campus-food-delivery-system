package com.sky.service.impl;

import com.sky.constant.AccountTypeConstant;
import com.sky.context.BaseContext;
import com.sky.mapper.DishMapper;
import com.sky.mapper.OrdersMapper;
import com.sky.mapper.SetmealMapper;
import com.sky.mapper.UserMapper;
import com.sky.security.MerchantScopeGuard;
import com.sky.support.MultiMerchantSchemaSupport;
import com.sky.vo.BusinessDataVO;
import com.sky.vo.DishOverViewVO;
import com.sky.vo.OrderOverViewVO;
import com.sky.vo.SetmealOverViewVO;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Verifies platform vs merchant workspace scope semantics for
 * {@link WorkspaceServiceImpl}.
 *
 * <ul>
 *   <li>Platform admin -> map.merchantId == null (global "today" aggregate).</li>
 *   <li>Merchant admin / staff -> map.merchantId == bound id (single-merchant
 *       overview).</li>
 *   <li>The same scope rule applies to business data, order overview, dish
 *       overview, and setmeal overview - keeping the workspace tiles in sync
 *       with the report screens and Excel export.</li>
 * </ul>
 */
@ExtendWith(MockitoExtension.class)
class WorkspaceServiceImplScopeTest {

    @Mock
    private OrdersMapper orderMapper;

    @Mock
    private UserMapper userMapper;

    @Mock
    private DishMapper dishMapper;

    @Mock
    private SetmealMapper setmealMapper;

    @Mock
    private MultiMerchantSchemaSupport schemaSupport;

    private MerchantScopeGuard merchantScopeGuard;

    @InjectMocks
    private WorkspaceServiceImpl workspaceService;

    @BeforeEach
    void setUp() {
        BaseContext.clear();
        merchantScopeGuard = new MerchantScopeGuard(schemaSupport);
        try {
            java.lang.reflect.Field guardField = WorkspaceServiceImpl.class.getDeclaredField("merchantScopeGuard");
            guardField.setAccessible(true);
            guardField.set(workspaceService, merchantScopeGuard);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException(e);
        }
    }

    @AfterEach
    void tearDown() {
        BaseContext.clear();
    }

    @Test
    void getBusinessData_shouldUseGlobalScope_forPlatformAdmin() {
        BaseContext.setCurrentAccountType(AccountTypeConstant.PLATFORM_ADMIN);
        when(orderMapper.countByMap(anyMap())).thenReturn(10);
        when(orderMapper.sumByMap(anyMap())).thenReturn(123.0);
        when(userMapper.getUserByMap(anyMap())).thenReturn(4);

        BusinessDataVO vo = workspaceService.getBusinessData(LocalDateTime.now().minusDays(1), LocalDateTime.now());

        assertThat(vo).isNotNull();
        ArgumentCaptor<Map<String, Object>> captor = ArgumentCaptor.forClass(Map.class);
        verify(orderMapper, atLeastOnce()).countByMap(captor.capture());
        captor.getAllValues().forEach(map -> assertThat(map.get("merchantId")).isNull());
    }

    @Test
    void getBusinessData_shouldScopeToBoundMerchant_forMerchantAdmin() {
        BaseContext.setCurrentAccountType(AccountTypeConstant.MERCHANT_ADMIN);
        BaseContext.setCurrentMerchantId(5L);
        when(orderMapper.countByMap(anyMap())).thenReturn(7);
        when(orderMapper.sumByMap(anyMap())).thenReturn(50.0);
        when(userMapper.getUserByMap(anyMap())).thenReturn(2);

        workspaceService.getBusinessData(LocalDateTime.now().minusDays(1), LocalDateTime.now());

        ArgumentCaptor<Map<String, Object>> captor = ArgumentCaptor.forClass(Map.class);
        verify(orderMapper, atLeastOnce()).countByMap(captor.capture());
        captor.getAllValues().forEach(map -> assertThat(map.get("merchantId")).isEqualTo(5L));
        ArgumentCaptor<Map<String, Object>> userCaptor = ArgumentCaptor.forClass(Map.class);
        verify(userMapper, atLeastOnce()).getUserByMap(userCaptor.capture());
        userCaptor.getAllValues().forEach(map -> assertThat(map.get("merchantId")).isEqualTo(5L));
    }

    @Test
    void getOrderOverView_shouldScopeToBoundMerchant_forMerchantStaff() {
        BaseContext.setCurrentAccountType(AccountTypeConstant.MERCHANT_STAFF);
        BaseContext.setCurrentMerchantId(2L);
        when(orderMapper.countByMap(anyMap())).thenReturn(1);

        OrderOverViewVO vo = workspaceService.getOrderOverView();

        assertThat(vo).isNotNull();
        ArgumentCaptor<Map<String, Object>> captor = ArgumentCaptor.forClass(Map.class);
        verify(orderMapper, atLeastOnce()).countByMap(captor.capture());
        captor.getAllValues().forEach(map -> assertThat(map.get("merchantId")).isEqualTo(2L));
    }

    @Test
    void getOrderOverView_shouldUseGlobalScope_forPlatformAdmin() {
        BaseContext.setCurrentAccountType(AccountTypeConstant.PLATFORM_ADMIN);
        when(orderMapper.countByMap(anyMap())).thenReturn(1);

        workspaceService.getOrderOverView();

        ArgumentCaptor<Map<String, Object>> captor = ArgumentCaptor.forClass(Map.class);
        verify(orderMapper, atLeastOnce()).countByMap(captor.capture());
        captor.getAllValues().forEach(map -> assertThat(map.get("merchantId")).isNull());
    }

    @Test
    void getDishOverView_shouldScopeToBoundMerchant_forMerchantAdmin() {
        BaseContext.setCurrentAccountType(AccountTypeConstant.MERCHANT_ADMIN);
        BaseContext.setCurrentMerchantId(9L);
        when(dishMapper.countByMap(anyMap())).thenReturn(3);

        DishOverViewVO vo = workspaceService.getDishOverView();

        assertThat(vo).isNotNull();
        ArgumentCaptor<Map<String, Object>> captor = ArgumentCaptor.forClass(Map.class);
        verify(dishMapper, atLeastOnce()).countByMap(captor.capture());
        captor.getAllValues().forEach(map -> assertThat(map.get("merchantId")).isEqualTo(9L));
    }

    @Test
    void getSetmealOverView_shouldScopeToBoundMerchant_forMerchantAdmin() {
        BaseContext.setCurrentAccountType(AccountTypeConstant.MERCHANT_ADMIN);
        BaseContext.setCurrentMerchantId(12L);
        when(setmealMapper.countByMap(anyMap())).thenReturn(4);

        SetmealOverViewVO vo = workspaceService.getSetmealOverView();

        assertThat(vo).isNotNull();
        ArgumentCaptor<Map<String, Object>> captor = ArgumentCaptor.forClass(Map.class);
        verify(setmealMapper, atLeastOnce()).countByMap(captor.capture());
        captor.getAllValues().forEach(map -> assertThat(map.get("merchantId")).isEqualTo(12L));
    }

    @Test
    void getSetmealOverView_shouldUseGlobalScope_forPlatformAdmin() {
        BaseContext.setCurrentAccountType(AccountTypeConstant.PLATFORM_ADMIN);
        when(setmealMapper.countByMap(anyMap())).thenReturn(4);

        workspaceService.getSetmealOverView();

        ArgumentCaptor<Map<String, Object>> captor = ArgumentCaptor.forClass(Map.class);
        verify(setmealMapper, atLeastOnce()).countByMap(captor.capture());
        captor.getAllValues().forEach(map -> assertThat(map.get("merchantId")).isNull());
    }
}
