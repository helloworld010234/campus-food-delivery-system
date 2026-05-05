package com.sky.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sky.constant.AccountTypeConstant;
import com.sky.context.BaseContext;
import com.sky.dto.OrdersSubmitDTO;
import com.sky.entity.AddressBook;
import com.sky.entity.Campus;
import com.sky.entity.Merchant;
import com.sky.entity.OrderDetail;
import com.sky.entity.Orders;
import com.sky.entity.ShoppingCart;
import com.sky.entity.User;
import com.sky.exception.BaseException;
import com.sky.mapper.AddressBookMapper;
import com.sky.mapper.EmployeeMapper;
import com.sky.mapper.OrderDetailMapper;
import com.sky.mapper.OrdersMapper;
import com.sky.mapper.ShoppingCartMapper;
import com.sky.mapper.UserMapper;
import com.sky.properties.StorefrontProperties;
import com.sky.security.MerchantScopeGuard;
import com.sky.service.CampusService;
import com.sky.service.MerchantService;
import com.sky.support.MultiMerchantSchemaSupport;
import com.sky.utils.MerchantScopeUtils;
import com.sky.utils.StorefrontImageResolver;
import com.sky.utils.WeChatPayUtil;
import com.sky.vo.OrderSubmitVO;
import com.sky.websocket.WebSocketServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderServiceImplTest {

    @Mock
    private OrdersMapper ordersMapper;

    @Mock
    private AddressBookMapper addressBookMapper;

    @Mock
    private ShoppingCartMapper shoppingCartMapper;

    @Mock
    private OrderDetailMapper orderDetailMapper;

    @Mock
    private UserMapper userMapper;

    @Mock
    private WeChatPayUtil weChatPayUtil;

    @Mock
    private WebSocketServer webSocketServer;

    @Mock
    private StorefrontProperties storefrontProperties;

    @Mock
    private StorefrontImageResolver storefrontImageResolver;

    @Mock
    private CampusService campusService;

    @Mock
    private MerchantService merchantService;

    @Mock
    private EmployeeMapper employeeMapper;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private MultiMerchantSchemaSupport schemaSupport;

    @Mock
    private MerchantScopeGuard merchantScopeGuard;

    @InjectMocks
    private OrderServiceImpl orderService;

    @BeforeEach
    void clearContextBefore() {
        BaseContext.clear();
    }

    @AfterEach
    void clearContextAfter() {
        BaseContext.clear();
    }

    // -------------------------------------------------------------------------
    // paySuccess tests
    // -------------------------------------------------------------------------

    @Test
    void paySuccess_whenOrderAlreadyPaidAndToBeConfirmed_shouldBeIdempotentAndNotCallUpdate() {
        // Arrange
        String outTradeNo = "ORDER_12345";
        Orders existingOrder = Orders.builder()
                .id(1L)
                .number(outTradeNo)
                .status(Orders.TO_BE_CONFIRMED)
                .payStatus(Orders.PAID)
                .build();

        when(ordersMapper.getByNumber(outTradeNo)).thenReturn(existingOrder);

        // Act
        orderService.paySuccess(outTradeNo);

        // Assert
        verify(ordersMapper, never()).update(any(Orders.class));
    }

    @Test
    void paySuccess_whenOrderPendingPayment_shouldUpdateStatusToToBeConfirmedAndPayStatusToPaid() {
        // Arrange
        String outTradeNo = "ORDER_67890";
        Orders existingOrder = Orders.builder()
                .id(2L)
                .number(outTradeNo)
                .status(Orders.PENDING_PAYMENT)
                .payStatus(Orders.UN_PAID)
                .merchantId(10L)
                .build();

        when(ordersMapper.getByNumber(outTradeNo)).thenReturn(existingOrder);
        when(schemaSupport.supportsEmployeeScope()).thenReturn(false);

        try (MockedStatic<MerchantScopeUtils> mockedMerchantScopeUtils = mockStatic(MerchantScopeUtils.class)) {
            mockedMerchantScopeUtils.when(() -> MerchantScopeUtils.isMerchantAccount()).thenReturn(false);

            // Act
            orderService.paySuccess(outTradeNo);

            // Assert
            verify(ordersMapper).update(argThat(updatedOrder ->
                    updatedOrder.getId().equals(2L)
                            && Orders.TO_BE_CONFIRMED.equals(updatedOrder.getStatus())
                            && Orders.PAID.equals(updatedOrder.getPayStatus())
                            && updatedOrder.getCheckoutTime() != null
            ));
            verify(webSocketServer).sendToAllClient((String) any());
        }
    }

    @Test
    void paySuccess_whenOrderInCompletedState_shouldReturnWithoutUpdate() {
        // Arrange
        String outTradeNo = "ORDER_COMPLETED";
        Orders existingOrder = Orders.builder()
                .id(3L)
                .number(outTradeNo)
                .status(Orders.COMPLETED)
                .payStatus(Orders.PAID)
                .build();

        when(ordersMapper.getByNumber(outTradeNo)).thenReturn(existingOrder);

        // Act
        orderService.paySuccess(outTradeNo);

        // Assert
        verify(ordersMapper, never()).update(any(Orders.class));
    }

    @Test
    void paySuccess_whenOrderNotFound_shouldReturnWithoutUpdate() {
        // Arrange
        String outTradeNo = "ORDER_NOT_EXIST";
        when(ordersMapper.getByNumber(outTradeNo)).thenReturn(null);

        // Act
        orderService.paySuccess(outTradeNo);

        // Assert
        verify(ordersMapper, never()).update(any(Orders.class));
    }

    // -------------------------------------------------------------------------
    // submitOrder tests
    // -------------------------------------------------------------------------

    @Test
    void submitOrder_withValidData_shouldCreateOrderClearCartAndReturnVo() {
        // Arrange
        Long userId = 100L;
        Long addressBookId = 1L;
        Long merchantId = 10L;
        Long campusId = 5L;

        OrdersSubmitDTO submitDTO = new OrdersSubmitDTO();
        submitDTO.setAddressBookId(addressBookId);
        submitDTO.setMerchantId(merchantId);
        submitDTO.setPayMethod(1);
        submitDTO.setRemark("Please hurry");

        AddressBook addressBook = AddressBook.builder()
                .id(addressBookId)
                .userId(userId)
                .consignee("John Doe")
                .phone("13800138000")
                .provinceName("Province")
                .cityName("City")
                .districtName("District")
                .detail("123 Street")
                .build();

        Merchant merchant = Merchant.builder()
                .id(merchantId)
                .campusId(campusId)
                .name("Test Merchant")
                .status(1)
                .businessStatus(1)
                .build();

        Campus campus = Campus.builder()
                .id(campusId)
                .status(1)
                .deliveryFee(new BigDecimal("3.00"))
                .estimatedDeliveryMinutes(30)
                .build();

        ShoppingCart cartItem1 = ShoppingCart.builder()
                .id(1L)
                .userId(userId)
                .merchantId(merchantId)
                .name("Dish A")
                .number(2)
                .amount(new BigDecimal("15.00"))
                .image("img1.jpg")
                .build();

        ShoppingCart cartItem2 = ShoppingCart.builder()
                .id(2L)
                .userId(userId)
                .merchantId(merchantId)
                .name("Dish B")
                .number(1)
                .amount(new BigDecimal("20.00"))
                .image("img2.jpg")
                .build();

        List<ShoppingCart> cartList = Arrays.asList(cartItem1, cartItem2);

        User user = User.builder()
                .id(userId)
                .name("Test User")
                .build();

        when(addressBookMapper.getById(addressBookId)).thenReturn(addressBook);
        when(merchantService.getById(merchantId)).thenReturn(merchant);
        when(campusService.getById(campusId)).thenReturn(campus);
        when(schemaSupport.supportsShoppingCartScope()).thenReturn(true);
        when(schemaSupport.supportsOrdersScope()).thenReturn(true);
        when(shoppingCartMapper.list(any(ShoppingCart.class))).thenReturn(cartList);
        when(userMapper.getById(userId)).thenReturn(user);
        try (MockedStatic<BaseContext> mockedBaseContext = mockStatic(BaseContext.class)) {
            mockedBaseContext.when(BaseContext::getCurrentId).thenReturn(userId);

            // Act
            OrderSubmitVO result = orderService.submitOrder(submitDTO);

            // Assert
            assertNotNull(result);
            assertNotNull(result.getOrderNumber());
            assertNotNull(result.getOrderTime());

            // Verify order inserted with correct status
            verify(ordersMapper).insert(argThat(order ->
                    Orders.PENDING_PAYMENT.equals(order.getStatus())
                            && Orders.UN_PAID.equals(order.getPayStatus())
                            && userId.equals(order.getUserId())
                            && merchantId.equals(order.getMerchantId())
                            && new BigDecimal("53.00").compareTo(order.getAmount()) == 0
            ));

            // Verify order details inserted
            verify(orderDetailMapper).insertBatch(argThat(details -> {
                if (!(details instanceof List)) {
                    return false;
                }
                List<OrderDetail> detailList = (List<OrderDetail>) details;
                return detailList.size() == 2
                        && "Dish A".equals(detailList.get(0).getName())
                        && "Dish B".equals(detailList.get(1).getName());
            }));

            // Verify cart cleared for user and merchant
            verify(shoppingCartMapper).deleteByUserIdAndMerchantId(userId, merchantId);
        }
    }

    @Test
    void submitOrder_whenAddressBookNotFound_shouldThrowBaseException() {
        // Arrange
        OrdersSubmitDTO submitDTO = new OrdersSubmitDTO();
        submitDTO.setAddressBookId(999L);
        submitDTO.setMerchantId(10L);

        when(addressBookMapper.getById(999L)).thenReturn(null);

        // Act & Assert
        assertThrows(BaseException.class, () -> orderService.submitOrder(submitDTO));
    }

    @Test
    void submitOrder_whenShoppingCartEmpty_shouldThrowBaseException() {
        // Arrange
        Long userId = 100L;
        Long addressBookId = 1L;
        Long merchantId = 10L;
        Long campusId = 5L;

        OrdersSubmitDTO submitDTO = new OrdersSubmitDTO();
        submitDTO.setAddressBookId(addressBookId);
        submitDTO.setMerchantId(merchantId);

        AddressBook addressBook = AddressBook.builder()
                .id(addressBookId)
                .userId(userId)
                .consignee("John Doe")
                .phone("13800138000")
                .provinceName("Province")
                .cityName("City")
                .districtName("District")
                .detail("123 Street")
                .build();

        Merchant merchant = Merchant.builder()
                .id(merchantId)
                .campusId(campusId)
                .name("Test Merchant")
                .status(1)
                .businessStatus(1)
                .build();

        Campus campus = Campus.builder()
                .id(campusId)
                .status(1)
                .deliveryFee(new BigDecimal("3.00"))
                .estimatedDeliveryMinutes(30)
                .build();

        when(addressBookMapper.getById(addressBookId)).thenReturn(addressBook);
        when(merchantService.getById(merchantId)).thenReturn(merchant);
        when(campusService.getById(campusId)).thenReturn(campus);
        when(schemaSupport.supportsShoppingCartScope()).thenReturn(true);
        when(shoppingCartMapper.list(any(ShoppingCart.class))).thenReturn(Collections.emptyList());

        try (MockedStatic<BaseContext> mockedBaseContext = mockStatic(BaseContext.class)) {
            mockedBaseContext.when(BaseContext::getCurrentId).thenReturn(userId);

            // Act & Assert
            assertThrows(BaseException.class, () -> orderService.submitOrder(submitDTO));
        }
    }

    // -------------------------------------------------------------------------
    // submitOrder additional scope tests (Agent 2)
    // -------------------------------------------------------------------------

    @Test
    void submitOrder_whenSchemaReadyAndMissingMerchantId_shouldThrowAndNotInsert() {
        // Arrange: schema is multi-merchant ready, DTO has no merchant id.
        OrdersSubmitDTO submitDTO = new OrdersSubmitDTO();
        submitDTO.setAddressBookId(1L);

        when(schemaSupport.isCoreSchemaReady()).thenReturn(true);
        when(merchantScopeGuard.requireExplicitMerchantId(eq(null), eq(null), anyString()))
                .thenThrow(new BaseException("order submit requires merchantId or shopId"));

        // Act & Assert
        assertThrows(BaseException.class, () -> orderService.submitOrder(submitDTO));
        verify(ordersMapper, never()).insert(any());
        verify(ordersMapper, never()).insertLegacy(any());
        verify(orderDetailMapper, never()).insertBatch(any());
        // Cart cleanup must not run on a failed submit.
        verify(shoppingCartMapper, never()).deleteByUserIdAndMerchantId(anyLong(), anyLong());
        verify(shoppingCartMapper, never()).deleteByUserId(anyLong());
    }

    @Test
    void submitOrder_whenCartItemBelongsToDifferentMerchant_shouldThrowAndRollback() {
        // Arrange: cart targets merchant A, but a stray row points at merchant B.
        Long userId = 100L;
        Long addressBookId = 1L;
        Long merchantA = 10L;
        Long merchantB = 20L;
        Long campusId = 5L;

        OrdersSubmitDTO submitDTO = new OrdersSubmitDTO();
        submitDTO.setAddressBookId(addressBookId);
        submitDTO.setMerchantId(merchantA);

        AddressBook addressBook = AddressBook.builder()
                .id(addressBookId)
                .userId(userId)
                .consignee("John")
                .phone("13800138000")
                .provinceName("P").cityName("C").districtName("D").detail("X")
                .build();
        Merchant merchant = Merchant.builder()
                .id(merchantA).campusId(campusId).name("A").status(1).businessStatus(1).build();
        Campus campus = Campus.builder()
                .id(campusId).status(1).deliveryFee(new BigDecimal("0.00")).estimatedDeliveryMinutes(30).build();

        ShoppingCart goodRow = ShoppingCart.builder()
                .id(1L).userId(userId).merchantId(merchantA)
                .name("ok").number(1).amount(new BigDecimal("5.00")).build();
        ShoppingCart polluted = ShoppingCart.builder()
                .id(2L).userId(userId).merchantId(merchantB)
                .name("pollution").number(1).amount(new BigDecimal("5.00")).build();

        when(schemaSupport.isCoreSchemaReady()).thenReturn(true);
        when(merchantScopeGuard.requireExplicitMerchantId(eq(merchantA), eq(null), anyString()))
                .thenReturn(merchantA);
        when(addressBookMapper.getById(addressBookId)).thenReturn(addressBook);
        when(merchantService.getById(merchantA)).thenReturn(merchant);
        when(campusService.getById(campusId)).thenReturn(campus);
        when(schemaSupport.supportsShoppingCartScope()).thenReturn(true);
        when(shoppingCartMapper.list(any(ShoppingCart.class)))
                .thenReturn(Arrays.asList(goodRow, polluted));
        // Same-merchant assertion explodes only when called with the polluted row.
        // Use lenient stubbing because the loop also calls it for the good row
        // (A=A, which we want to be a no-op).
        org.mockito.Mockito.lenient().doNothing()
                .when(merchantScopeGuard)
                .assertSameMerchant(eq(merchantA), eq(merchantA), eq("shopping cart item"));
        doThrow(new BaseException("shopping cart item merchant mismatch"))
                .when(merchantScopeGuard)
                .assertSameMerchant(eq(merchantA), eq(merchantB), eq("shopping cart item"));

        try (MockedStatic<BaseContext> mockedBaseContext = mockStatic(BaseContext.class)) {
            mockedBaseContext.when(BaseContext::getCurrentId).thenReturn(userId);

            // Act & Assert
            assertThrows(BaseException.class, () -> orderService.submitOrder(submitDTO));

            // Order, details, and cart cleanup must not happen if any cart row
            // belongs to a different merchant. @Transactional rollback ensures
            // partial state is not persisted.
            verify(ordersMapper, never()).insert(any());
            verify(ordersMapper, never()).insertLegacy(any());
            verify(orderDetailMapper, never()).insertBatch(any());
            verify(shoppingCartMapper, never()).deleteByUserIdAndMerchantId(anyLong(), anyLong());
            verify(shoppingCartMapper, never()).deleteByUserId(anyLong());
        }
    }

    // -------------------------------------------------------------------------
    // detail / cancel / repetition / reminder ownership tests
    // -------------------------------------------------------------------------

    @Test
    void detail_whenAccessedByDifferentUser_shouldThrowForbidden() {
        // Arrange: order belongs to user 100, current user is 200, no admin context.
        Long ownerUserId = 100L;
        Orders other = Orders.builder()
                .id(1L)
                .userId(ownerUserId)
                .merchantId(10L)
                .build();
        when(ordersMapper.getById(1L)).thenReturn(other);

        BaseContext.setCurrentId(200L);
        // No account type means user-side access path.

        // Act & Assert
        assertThrows(BaseException.class, () -> orderService.detail(1L));
    }

    @Test
    void detail_whenOrderNotFound_shouldThrow() {
        // Arrange
        when(ordersMapper.getById(99L)).thenReturn(null);
        BaseContext.setCurrentId(200L);

        // Act & Assert
        assertThrows(BaseException.class, () -> orderService.detail(99L));
    }

    @Test
    void cancelOrderById_whenAccessedByDifferentUser_shouldThrowAndNotUpdate() {
        // Arrange
        Orders order = Orders.builder()
                .id(1L)
                .userId(100L)
                .merchantId(10L)
                .status(Orders.PENDING_PAYMENT)
                .payStatus(Orders.UN_PAID)
                .build();
        when(ordersMapper.getById(1L)).thenReturn(order);

        BaseContext.setCurrentId(200L);

        // Act & Assert
        assertThrows(BaseException.class, () -> orderService.cancelOrderById(1L));
        verify(ordersMapper, never()).update(any(Orders.class));
    }

    @Test
    void cancelOrderById_whenAdminCallsUserEndpoint_shouldThrowEndpointMismatch() {
        // Arrange: admin user (account type set) trying to invoke the user endpoint.
        Orders order = Orders.builder()
                .id(1L)
                .userId(100L)
                .merchantId(10L)
                .status(Orders.PENDING_PAYMENT)
                .build();
        when(ordersMapper.getById(1L)).thenReturn(order);

        BaseContext.setCurrentId(99L);
        BaseContext.setCurrentMerchantId(10L);
        BaseContext.setCurrentAccountType(AccountTypeConstant.MERCHANT_ADMIN);

        // Act & Assert
        assertThrows(BaseException.class, () -> orderService.cancelOrderById(1L));
        verify(ordersMapper, never()).update(any(Orders.class));
    }

    @Test
    void repetition_whenSchemaReadyAndOrderHasNoMerchant_shouldThrowAndNotInsertCart() {
        // Arrange: data integrity bug - order missing merchant in multi-merchant mode.
        Orders order = Orders.builder()
                .id(1L)
                .userId(100L)
                .merchantId(null)
                .build();
        when(ordersMapper.getById(1L)).thenReturn(order);
        when(schemaSupport.supportsShoppingCartScope()).thenReturn(true);

        BaseContext.setCurrentId(100L);

        // Act & Assert
        assertThrows(BaseException.class, () -> orderService.repetition(1L));
        verify(shoppingCartMapper, never()).insertBatch(any());
        verify(shoppingCartMapper, never()).insertBatchLegacy(any());
    }

    @Test
    void repetition_whenSchemaReady_shouldWriteIntoOriginalOrderMerchantCart() {
        // Arrange
        Long userId = 100L;
        Long originalMerchant = 10L;

        Orders order = Orders.builder()
                .id(1L)
                .userId(userId)
                .merchantId(originalMerchant)
                .build();
        OrderDetail detail = OrderDetail.builder()
                .id(99L)
                .name("dish")
                .number(2)
                .amount(new BigDecimal("10.00"))
                .dishId(7L)
                .build();
        when(ordersMapper.getById(1L)).thenReturn(order);
        when(schemaSupport.supportsShoppingCartScope()).thenReturn(true);
        when(orderDetailMapper.getByOrderId(1L)).thenReturn(Collections.singletonList(detail));

        BaseContext.setCurrentId(userId);

        // Act
        orderService.repetition(1L);

        // Assert: cart rows must carry the original order's merchant id, not null,
        // and must not silently fall back to "first enabled merchant".
        verify(shoppingCartMapper).insertBatch(argThat(list -> {
            if (list == null || list.size() != 1) {
                return false;
            }
            ShoppingCart cart = list.get(0);
            return originalMerchant.equals(cart.getMerchantId())
                    && userId.equals(cart.getUserId())
                    && cart.getId() == null
                    && Integer.valueOf(2).equals(cart.getNumber());
        }));
        verify(shoppingCartMapper, never()).insertBatchLegacy(any());
    }

    @Test
    void repetition_whenLegacyFallback_shouldUseLegacyBatchInsertWithoutMerchant() {
        // Arrange
        Long userId = 100L;
        Orders order = Orders.builder()
                .id(1L)
                .userId(userId)
                .merchantId(null)
                .build();
        OrderDetail detail = OrderDetail.builder()
                .id(99L)
                .name("dish")
                .number(1)
                .amount(new BigDecimal("5.00"))
                .build();
        when(ordersMapper.getById(1L)).thenReturn(order);
        when(schemaSupport.supportsShoppingCartScope()).thenReturn(false);
        when(orderDetailMapper.getByOrderId(1L)).thenReturn(Collections.singletonList(detail));

        BaseContext.setCurrentId(userId);

        // Act
        orderService.repetition(1L);

        // Assert
        verify(shoppingCartMapper).insertBatchLegacy(argThat(list ->
                list != null && list.size() == 1
                        && list.get(0).getMerchantId() == null
                        && userId.equals(list.get(0).getUserId())));
        verify(shoppingCartMapper, never()).insertBatch(any());
    }

    @Test
    void repetition_whenAccessedByDifferentUser_shouldThrowAndNotInsert() {
        // Arrange
        Orders order = Orders.builder()
                .id(1L)
                .userId(100L)
                .merchantId(10L)
                .build();
        when(ordersMapper.getById(1L)).thenReturn(order);

        BaseContext.setCurrentId(200L);

        // Act & Assert
        assertThrows(BaseException.class, () -> orderService.repetition(1L));
        verify(shoppingCartMapper, never()).insertBatch(any());
        verify(shoppingCartMapper, never()).insertBatchLegacy(any());
    }

    @Test
    void reminder_whenAccessedByDifferentUser_shouldThrowAndNotEmitEvent() {
        // Arrange
        Orders order = Orders.builder()
                .id(1L)
                .userId(100L)
                .merchantId(10L)
                .number("ORDER_X")
                .build();
        when(ordersMapper.getById(1L)).thenReturn(order);

        BaseContext.setCurrentId(200L);

        // Act & Assert
        assertThrows(BaseException.class, () -> orderService.reminder(1L));
        verify(webSocketServer, never()).sendToAllClient(any());
        verify(webSocketServer, never()).sendToClient(any(), any());
    }

    // -------------------------------------------------------------------------
    // payment ownership tests
    // -------------------------------------------------------------------------

    @Test
    void payment_whenOrderNotFoundForUser_shouldThrow() {
        // Arrange: mapper returns null for (orderNumber, userId) pair, indicating
        // either no such order or a different owner.
        BaseContext.setCurrentId(200L);
        when(ordersMapper.getByNumberAndUserId(anyString(), anyLong())).thenReturn(null);

        com.sky.dto.OrdersPaymentDTO dto = new com.sky.dto.OrdersPaymentDTO();
        dto.setOrderNumber("UNKNOWN");

        // Act & Assert
        assertThrows(BaseException.class, () -> orderService.payment(dto));
    }

    @Test
    void payment_whenAlreadyPaid_shouldThrowOrderPaid() throws Exception {
        // Arrange
        Long userId = 200L;
        BaseContext.setCurrentId(userId);
        Orders paid = Orders.builder()
                .id(1L)
                .number("ORDER_PAID")
                .userId(userId)
                .merchantId(10L)
                .payStatus(Orders.PAID)
                .build();
        when(ordersMapper.getByNumberAndUserId("ORDER_PAID", userId)).thenReturn(paid);

        com.sky.dto.OrdersPaymentDTO dto = new com.sky.dto.OrdersPaymentDTO();
        dto.setOrderNumber("ORDER_PAID");

        // Act & Assert
        assertThrows(BaseException.class, () -> orderService.payment(dto));
        // Mock payment path must not be invoked for an already-paid order.
        verify(ordersMapper, never()).update(any(Orders.class));
    }

    // -------------------------------------------------------------------------
    // pageQueryUser ownership scoping
    // -------------------------------------------------------------------------

    @Test
    void pageQueryUser_shouldBindCurrentUserIdAndNullMerchantInQuery() {
        // Arrange: history list must always be scoped by current userId; merchant
        // id is intentionally null because user history aggregates across merchants.
        Long userId = 200L;
        BaseContext.setCurrentId(userId);
        com.github.pagehelper.Page<Orders> empty = new com.github.pagehelper.Page<>();
        when(ordersMapper.pageQuery(any())).thenReturn(empty);

        // Act
        com.sky.result.PageResult result = orderService.pageQueryUser(1, 10, 4, null);

        // Assert
        assertNotNull(result);
        verify(ordersMapper).pageQuery(argThat(query ->
                userId.equals(query.getUserId())
                        && query.getMerchantId() == null
                        && Integer.valueOf(4).equals(query.getStatus())));
    }
}
