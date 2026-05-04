package com.sky.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
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
import com.sky.service.CampusService;
import com.sky.service.MerchantService;
import com.sky.support.MultiMerchantSchemaSupport;
import com.sky.utils.MerchantScopeUtils;
import com.sky.utils.StorefrontImageResolver;
import com.sky.utils.WeChatPayUtil;
import com.sky.vo.OrderSubmitVO;
import com.sky.websocket.WebSocketServer;
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

    @InjectMocks
    private OrderServiceImpl orderService;

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
}
