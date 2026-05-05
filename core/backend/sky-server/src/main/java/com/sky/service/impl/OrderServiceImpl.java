package com.sky.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.AccountTypeConstant;
import com.sky.context.BaseContext;
import com.sky.dto.OrdersCancelDTO;
import com.sky.dto.OrdersConfirmDTO;
import com.sky.dto.OrdersPageQueryDTO;
import com.sky.dto.OrdersPaymentDTO;
import com.sky.dto.OrdersRejectionDTO;
import com.sky.dto.OrdersSubmitDTO;
import com.sky.entity.AddressBook;
import com.sky.entity.Campus;
import com.sky.entity.Employee;
import com.sky.entity.Merchant;
import com.sky.entity.OrderDetail;
import com.sky.entity.Orders;
import com.sky.entity.ShoppingCart;
import com.sky.entity.User;
import com.sky.exception.AddressBookBusinessException;
import com.sky.exception.BaseException;
import com.sky.exception.OrderBusinessException;
import com.sky.exception.ShoppingCartBusinessException;
import com.sky.mapper.AddressBookMapper;
import com.sky.mapper.EmployeeMapper;
import com.sky.mapper.OrderDetailMapper;
import com.sky.mapper.OrdersMapper;
import com.sky.mapper.ShoppingCartMapper;
import com.sky.mapper.UserMapper;
import com.sky.properties.StorefrontProperties;
import com.sky.result.PageResult;
import com.sky.security.MerchantScopeGuard;
import com.sky.service.CampusService;
import com.sky.service.MerchantService;
import com.sky.service.OrderService;
import com.sky.support.MultiMerchantSchemaSupport;
import com.sky.utils.StorefrontImageResolver;
import com.sky.utils.WeChatPayUtil;
import com.sky.vo.MerchantVO;
import com.sky.vo.OrderPaymentVO;
import com.sky.vo.OrderStatisticsVO;
import com.sky.vo.OrderSubmitVO;
import com.sky.vo.OrderVO;
import com.sky.websocket.WebSocketServer;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Order lifecycle service. The user-private chain (cart submit, payment, history,
 * detail, cancel, repetition, reminder) MUST be bound to {@code userId + merchantId}
 * when the multi-merchant schema is ready. Cross-merchant data is rejected through
 * {@link MerchantScopeGuard} rather than ad-hoc checks scattered in this class.
 *
 * <p>Legacy single-merchant fallback is preserved when
 * {@link MultiMerchantSchemaSupport} reports the columns are missing: only then we
 * fall back to "first enabled merchant" / null-merchant mappers.</p>
 */
@Service
public class OrderServiceImpl implements OrderService {

    private static final BigDecimal MOCK_PAY_AMOUNT = new BigDecimal("0.01");

    private static final String MSG_ORDER_NOT_FOUND = "order not found";
    private static final String MSG_ORDER_PAID = "order already paid";
    private static final String MSG_ORDER_STATUS_INVALID = "order status invalid";
    private static final String MSG_ADDRESS_EMPTY = "user address is empty, cannot submit order";
    private static final String MSG_CART_EMPTY = "shopping cart is empty, cannot submit order";
    private static final String MSG_ORDER_FORBIDDEN_USER = "no permission to access this order";
    private static final String MSG_USER_ENDPOINT_ONLY = "this endpoint is for end users only";
    private static final String MSG_ADMIN_ENDPOINT_ONLY = "this endpoint is for admin only";
    private static final String MSG_CAMPUS_CLOSED = "current campus is not open";
    private static final String MSG_MERCHANT_DISABLED = "current merchant is unavailable";
    private static final String MSG_MERCHANT_NOT_OPEN = "current merchant is closed";

    @Autowired
    private OrdersMapper ordersMapper;

    @Autowired
    private AddressBookMapper addressBookMapper;

    @Autowired
    private ShoppingCartMapper shoppingCartMapper;

    @Autowired
    private OrderDetailMapper orderDetailMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private WeChatPayUtil weChatPayUtil;

    @Autowired
    private WebSocketServer webSocketServer;

    @Autowired
    private StorefrontProperties storefrontProperties;

    @Autowired
    private StorefrontImageResolver storefrontImageResolver;

    @Autowired
    private CampusService campusService;

    @Autowired
    private MerchantService merchantService;

    @Autowired
    private EmployeeMapper employeeMapper;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MultiMerchantSchemaSupport schemaSupport;

    @Autowired
    private MerchantScopeGuard merchantScopeGuard;

    @Override
    @Transactional
    public OrderSubmitVO submitOrder(OrdersSubmitDTO ordersSubmitDTO) {
        Long merchantId = resolvePrivateMerchantId(
                ordersSubmitDTO.getMerchantId(), ordersSubmitDTO.getShopId(), "order submit");

        AddressBook addressBook = addressBookMapper.getById(ordersSubmitDTO.getAddressBookId());
        if (addressBook == null) {
            throw new AddressBookBusinessException(MSG_ADDRESS_EMPTY);
        }

        Merchant merchant = merchantService.getById(merchantId);
        Campus campus = merchant == null ? null : campusService.getById(merchant.getCampusId());
        validateMerchantOrderable(campus, merchant);

        Long userId = BaseContext.getCurrentId();
        Long cartScopeId = schemaSupport.supportsShoppingCartScope() ? merchant.getId() : null;
        ShoppingCart condition = ShoppingCart.builder()
                .userId(userId)
                .merchantId(cartScopeId)
                .build();
        List<ShoppingCart> cartList = shoppingCartMapper.list(condition);
        if (CollectionUtils.isEmpty(cartList)) {
            throw new ShoppingCartBusinessException(MSG_CART_EMPTY);
        }

        // Defensive cross-merchant check on cart rows. Even though the mapper is
        // scoped, we make sure no row leaked from a different merchant before we
        // copy it into the order details.
        if (schemaSupport.supportsShoppingCartScope()) {
            for (ShoppingCart cart : cartList) {
                if (cart.getMerchantId() != null) {
                    merchantScopeGuard.assertSameMerchant(merchant.getId(), cart.getMerchantId(),
                            "shopping cart item");
                }
            }
        }

        BigDecimal goodsAmount = BigDecimal.ZERO;
        int itemCount = 0;
        for (ShoppingCart cart : cartList) {
            goodsAmount = goodsAmount.add(cart.getAmount().multiply(BigDecimal.valueOf(cart.getNumber())));
            itemCount += cart.getNumber();
        }

        BigDecimal deliveryFee = campus.getDeliveryFee() == null ? BigDecimal.ZERO : campus.getDeliveryFee();
        BigDecimal packAmount = schemaSupport.supportsOrdersScope() ? BigDecimal.ZERO : BigDecimal.valueOf(itemCount);
        BigDecimal totalAmount = goodsAmount.add(deliveryFee).add(packAmount);

        User user = userMapper.getById(userId);

        Orders orders = new Orders();
        BeanUtils.copyProperties(ordersSubmitDTO, orders);
        orders.setOrderTime(LocalDateTime.now());
        orders.setPhone(addressBook.getPhone());
        orders.setUserId(userId);
        orders.setUserName(user == null ? null : user.getName());
        orders.setConsignee(addressBook.getConsignee());
        orders.setStatus(Orders.PENDING_PAYMENT);
        orders.setPayStatus(Orders.UN_PAID);
        orders.setDeliveryStatus(1);
        orders.setTablewareStatus(1);
        orders.setNumber(String.valueOf(System.currentTimeMillis()));
        orders.setAddress(buildFullAddress(addressBook));
        orders.setPackAmount(packAmount);
        orders.setAmount(totalAmount);
        if (schemaSupport.supportsOrdersScope()) {
            orders.setCampusId(campus.getId());
            orders.setMerchantId(merchant.getId());
            orders.setMerchantName(merchant.getName());
            orders.setGoodsAmount(goodsAmount);
            orders.setDeliveryFee(deliveryFee);
            orders.setItemCount(itemCount);
            ordersMapper.insert(orders);
        } else {
            ordersMapper.insertLegacy(orders);
        }

        List<OrderDetail> orderDetailList = new ArrayList<>();
        for (ShoppingCart cart : cartList) {
            OrderDetail orderDetail = new OrderDetail();
            BeanUtils.copyProperties(cart, orderDetail);
            orderDetail.setOrderId(orders.getId());
            orderDetailList.add(orderDetail);
        }
        orderDetailMapper.insertBatch(orderDetailList);

        // Cart cleanup is part of the same transaction as order creation; we only
        // delete rows that match the resolved merchant scope so users with parallel
        // carts on other merchants are not affected.
        if (schemaSupport.supportsShoppingCartScope()) {
            shoppingCartMapper.deleteByUserIdAndMerchantId(userId, merchant.getId());
        } else {
            shoppingCartMapper.deleteByUserId(userId);
        }

        return OrderSubmitVO.builder()
                .id(orders.getId())
                .orderTime(orders.getOrderTime())
                .orderAmount(orders.getAmount())
                .orderNumber(orders.getNumber())
                .build();
    }

    @Override
    public OrderPaymentVO payment(OrdersPaymentDTO ordersPaymentDTO) throws Exception {
        Long userId = BaseContext.getCurrentId();
        Orders ordersDB = ordersMapper.getByNumberAndUserId(ordersPaymentDTO.getOrderNumber(), userId);
        if (ordersDB == null) {
            throw new OrderBusinessException(MSG_ORDER_NOT_FOUND);
        }
        if (Orders.PAID.equals(ordersDB.getPayStatus())) {
            throw new OrderBusinessException(MSG_ORDER_PAID);
        }

        if (Boolean.TRUE.equals(storefrontProperties.getMockPayment())) {
            paySuccess(ordersPaymentDTO.getOrderNumber());
            return OrderPaymentVO.builder().mockPay(Boolean.TRUE).build();
        }

        User user = userMapper.getById(userId);
        JsonNode jsonObject = weChatPayUtil.pay(
                ordersPaymentDTO.getOrderNumber(),
                MOCK_PAY_AMOUNT,
                (ordersDB.getMerchantName() == null ? "order" : ordersDB.getMerchantName() + " order"),
                user.getOpenid()
        );

        if ("ORDERPAID".equals(jsonObject.get("code").asText())) {
            throw new OrderBusinessException(MSG_ORDER_PAID);
        }

        OrderPaymentVO vo = objectMapper.treeToValue(jsonObject, OrderPaymentVO.class);
        vo.setPackageStr(jsonObject.get("package").asText());
        vo.setMockPay(Boolean.FALSE);
        return vo;
    }

    @Override
    public void paySuccess(String outTradeNo) {
        Orders ordersDB = ordersMapper.getByNumber(outTradeNo);
        if (ordersDB == null) {
            return;
        }

        if (Orders.PAID.equals(ordersDB.getPayStatus()) && Orders.TO_BE_CONFIRMED.equals(ordersDB.getStatus())) {
            return;
        }

        if (!Orders.PENDING_PAYMENT.equals(ordersDB.getStatus()) && !Orders.UN_PAID.equals(ordersDB.getPayStatus())) {
            return;
        }

        ordersMapper.update(Orders.builder()
                .id(ordersDB.getId())
                .status(Orders.TO_BE_CONFIRMED)
                .payStatus(Orders.PAID)
                .checkoutTime(LocalDateTime.now())
                .build());

        sendOrderEventToMerchant(ordersDB.getMerchantId(), 1, ordersDB.getId(), "order: " + outTradeNo);
    }

    @Override
    public PageResult pageQueryUser(int page, int pageSize, Integer status, Integer payStatus) {
        PageHelper.startPage(page, pageSize);
        OrdersPageQueryDTO queryDTO = new OrdersPageQueryDTO();
        queryDTO.setUserId(BaseContext.getCurrentId());
        queryDTO.setStatus(status);
        queryDTO.setPayStatus(payStatus);
        queryDTO.setMerchantId(null);

        Page<Orders> orderPage = ordersMapper.pageQuery(queryDTO);
        List<OrderVO> list = new ArrayList<>();
        if (orderPage != null && orderPage.getTotal() > 0) {
            for (Orders orders : orderPage) {
                list.add(buildOrderVO(orders));
            }
        }
        return new PageResult(orderPage == null ? 0 : orderPage.getTotal(), list);
    }

    @Override
    public OrderVO detail(Long id) {
        Orders orders = getAccessibleOrder(id);
        return buildOrderVO(orders);
    }

    @Override
    public void cancelOrderById(Long id) throws Exception {
        Orders orders = getAccessibleUserOrder(id);
        if (orders.getStatus() > Orders.TO_BE_CONFIRMED) {
            throw new OrderBusinessException(MSG_ORDER_STATUS_INVALID);
        }

        Orders toUpdate = new Orders();
        toUpdate.setId(orders.getId());
        if (refundIfNecessary(orders)) {
            toUpdate.setPayStatus(Orders.REFUND);
        }
        toUpdate.setStatus(Orders.CANCELLED);
        toUpdate.setCancelReason("user cancelled");
        toUpdate.setCancelTime(LocalDateTime.now());
        ordersMapper.update(toUpdate);
    }

    @Override
    public void repetition(Long id) {
        Orders orders = getAccessibleUserOrder(id);
        Long userId = BaseContext.getCurrentId();

        // Reorder must write into the original order's merchant cart. We never
        // silently fall back to "first enabled merchant" in multi-merchant mode -
        // an order written without a merchant in that mode is a data integrity
        // bug, not something to paper over here.
        Long targetMerchantId = null;
        if (schemaSupport.supportsShoppingCartScope()) {
            if (orders.getMerchantId() == null) {
                throw new BaseException("original order has no merchant context, cannot repeat");
            }
            targetMerchantId = orders.getMerchantId();
        }
        final Long resolvedMerchantId = targetMerchantId;

        List<OrderDetail> orderDetailList = orderDetailMapper.getByOrderId(id);
        List<ShoppingCart> shoppingCartList = orderDetailList.stream().map(item -> {
            ShoppingCart shoppingCart = new ShoppingCart();
            BeanUtils.copyProperties(item, shoppingCart, "id");
            shoppingCart.setMerchantId(resolvedMerchantId);
            shoppingCart.setUserId(userId);
            shoppingCart.setCreateTime(LocalDateTime.now());
            return shoppingCart;
        }).collect(Collectors.toList());
        if (schemaSupport.supportsShoppingCartScope()) {
            shoppingCartMapper.insertBatch(shoppingCartList);
        } else {
            shoppingCartMapper.insertBatchLegacy(shoppingCartList);
        }
    }

    @Override
    public PageResult conditionSearch(OrdersPageQueryDTO ordersPageQueryDTO) {
        if (schemaSupport.supportsOrdersScope()) {
            ordersPageQueryDTO.setMerchantId(merchantScopeGuard.resolveAdminQueryMerchantId(
                    ordersPageQueryDTO.getMerchantId()));
        } else {
            ordersPageQueryDTO.setMerchantId(null);
        }
        PageHelper.startPage(ordersPageQueryDTO.getPage(), ordersPageQueryDTO.getPageSize());
        Page<Orders> page = ordersMapper.pageQuery(ordersPageQueryDTO);
        List<OrderVO> orderVOList = getOrderVOList(page);
        return new PageResult(page.getTotal(), orderVOList);
    }

    @Override
    public OrderStatisticsVO statistics() {
        Long merchantId = schemaSupport.supportsOrdersScope()
                ? merchantScopeGuard.resolveAdminQueryMerchantId(null)
                : null;
        Integer toBeConfirmed = schemaSupport.supportsOrdersScope()
                ? ordersMapper.countStatus(Orders.TO_BE_CONFIRMED, merchantId)
                : ordersMapper.countStatusLegacy(Orders.TO_BE_CONFIRMED);
        Integer confirmed = schemaSupport.supportsOrdersScope()
                ? ordersMapper.countStatus(Orders.CONFIRMED, merchantId)
                : ordersMapper.countStatusLegacy(Orders.CONFIRMED);
        Integer deliveryInProgress = schemaSupport.supportsOrdersScope()
                ? ordersMapper.countStatus(Orders.DELIVERY_IN_PROGRESS, merchantId)
                : ordersMapper.countStatusLegacy(Orders.DELIVERY_IN_PROGRESS);

        OrderStatisticsVO orderStatisticsVO = new OrderStatisticsVO();
        orderStatisticsVO.setToBeConfirmed(toBeConfirmed);
        orderStatisticsVO.setConfirmed(confirmed);
        orderStatisticsVO.setDeliveryInProgress(deliveryInProgress);
        return orderStatisticsVO;
    }

    @Override
    public void confirm(OrdersConfirmDTO confirmDTO) {
        Orders orders = getAccessibleAdminOrder(confirmDTO.getId());
        ordersMapper.update(Orders.builder()
                .id(orders.getId())
                .status(Orders.CONFIRMED)
                .build());
    }

    @Override
    public void rejection(OrdersRejectionDTO ordersRejectionDTO) throws Exception {
        Orders ordersDB = getAccessibleAdminOrder(ordersRejectionDTO.getId());
        if (!Orders.TO_BE_CONFIRMED.equals(ordersDB.getStatus())) {
            throw new OrderBusinessException(MSG_ORDER_STATUS_INVALID);
        }

        Orders orders = new Orders();
        orders.setId(ordersDB.getId());
        if (refundIfNecessary(ordersDB)) {
            orders.setPayStatus(Orders.REFUND);
        }
        orders.setStatus(Orders.CANCELLED);
        orders.setRejectionReason(ordersRejectionDTO.getRejectionReason());
        orders.setCancelTime(LocalDateTime.now());
        ordersMapper.update(orders);
    }

    @Override
    public void cancel(OrdersCancelDTO ordersCancelDTO) throws Exception {
        Orders ordersDB = getAccessibleAdminOrder(ordersCancelDTO.getId());
        Orders orders = new Orders();
        orders.setId(ordersDB.getId());
        if (refundIfNecessary(ordersDB)) {
            orders.setPayStatus(Orders.REFUND);
        }
        orders.setStatus(Orders.CANCELLED);
        orders.setCancelReason(ordersCancelDTO.getCancelReason());
        orders.setCancelTime(LocalDateTime.now());
        ordersMapper.update(orders);
    }

    @Override
    public void delivery(Long id) {
        Orders ordersDB = getAccessibleAdminOrder(id);
        if (!Orders.CONFIRMED.equals(ordersDB.getStatus())) {
            throw new OrderBusinessException(MSG_ORDER_STATUS_INVALID);
        }

        ordersMapper.update(Orders.builder()
                .id(ordersDB.getId())
                .status(Orders.DELIVERY_IN_PROGRESS)
                .build());
    }

    @Override
    public void complete(Long id) {
        Orders ordersDB = getAccessibleAdminOrder(id);
        if (!Orders.DELIVERY_IN_PROGRESS.equals(ordersDB.getStatus())) {
            throw new OrderBusinessException(MSG_ORDER_STATUS_INVALID);
        }

        ordersMapper.update(Orders.builder()
                .id(ordersDB.getId())
                .deliveryTime(LocalDateTime.now())
                .status(Orders.COMPLETED)
                .build());
    }

    @Override
    public void reminder(Long id) {
        Orders ordersDB = getAccessibleUserOrder(id);
        sendOrderEventToMerchant(ordersDB.getMerchantId(), 2, id, "order: " + ordersDB.getNumber());
    }

    @Override
    public LocalDateTime getEstimatedDeliveryTime(Long merchantId, String customerAddress) {
        Long resolvedMerchantId = resolvePrivateMerchantId(merchantId, null, "estimated delivery time");
        Merchant merchant = merchantService.getById(resolvedMerchantId);
        Campus campus = merchant == null ? null : campusService.getById(merchant.getCampusId());
        validateMerchantOrderable(campus, merchant);
        Integer minutes = campus.getEstimatedDeliveryMinutes();
        if (minutes == null || minutes <= 0) {
            minutes = 30;
        }
        return LocalDateTime.now().plusMinutes(minutes);
    }

    private Orders getAccessibleOrder(Long id) {
        Orders orders = ordersMapper.getById(id);
        if (orders == null) {
            throw new OrderBusinessException(MSG_ORDER_NOT_FOUND);
        }

        if (BaseContext.getCurrentAccountType() != null) {
            // Admin path: platform accounts pass through, merchant accounts must
            // own the order. Same semantics as MerchantScopeUtils.assertAccessible
            // but routed through the centralised guard so behaviour stays in sync
            // with Agent 1's permission matrix.
            merchantScopeGuard.assertMerchantAccountCanAccess(orders.getMerchantId(), "order");
        } else {
            Long currentUserId = BaseContext.getCurrentId();
            if (currentUserId == null || !currentUserId.equals(orders.getUserId())) {
                throw new BaseException(MSG_ORDER_FORBIDDEN_USER);
            }
        }
        return orders;
    }

    private Orders getAccessibleUserOrder(Long id) {
        Orders orders = getAccessibleOrder(id);
        if (BaseContext.getCurrentAccountType() != null) {
            throw new BaseException(MSG_USER_ENDPOINT_ONLY);
        }
        return orders;
    }

    private Orders getAccessibleAdminOrder(Long id) {
        Orders orders = getAccessibleOrder(id);
        if (BaseContext.getCurrentAccountType() == null) {
            throw new BaseException(MSG_ADMIN_ENDPOINT_ONLY);
        }
        return orders;
    }

    private List<OrderVO> getOrderVOList(Page<Orders> page) {
        List<OrderVO> list = new ArrayList<>();
        List<Orders> ordersList = page.getResult();
        if (!CollectionUtils.isEmpty(ordersList)) {
            for (Orders orders : ordersList) {
                OrderVO orderVO = new OrderVO();
                BeanUtils.copyProperties(orders, orderVO);
                orderVO.setOrderDishes(getOrderDishesStr(orders));
                list.add(orderVO);
            }
        }
        return list;
    }

    private String getOrderDishesStr(Orders orders) {
        List<OrderDetail> orderDetailList = orderDetailMapper.getByOrderId(orders.getId());
        List<String> orderDishList = orderDetailList.stream()
                .map(item -> item.getName() + "*" + item.getNumber() + ";")
                .collect(Collectors.toList());
        return String.join("", orderDishList);
    }

    private OrderVO buildOrderVO(Orders orders) {
        OrderVO orderVO = new OrderVO();
        BeanUtils.copyProperties(orders, orderVO);

        List<OrderDetail> orderDetailList = orderDetailMapper.getByOrderId(orders.getId());
        resolveOrderDetailImages(orderDetailList);
        orderVO.setOrderDetailList(orderDetailList);

        MerchantVO merchantInfo = merchantService.getMerchantInfo(orders.getMerchantId());
        orderVO.setShopName(orders.getMerchantName() == null ? merchantInfo.getName() : orders.getMerchantName());
        orderVO.setDeliveryFee(orders.getDeliveryFee() == null ? merchantInfo.getDeliveryFee() : orders.getDeliveryFee());
        orderVO.setShopTelephone(merchantInfo.getContactPhone());
        orderVO.setCourierTelephone(merchantInfo.getServicePhone());

        if (orders.getAddressBookId() != null) {
            AddressBook addressBook = addressBookMapper.getById(orders.getAddressBookId());
            if (addressBook != null) {
                orderVO.setSex(addressBook.getSex());
                if (isBlank(orderVO.getAddress())) {
                    orderVO.setAddress(buildFullAddress(addressBook));
                }
            }
        }
        return orderVO;
    }

    private void resolveOrderDetailImages(List<OrderDetail> orderDetailList) {
        if (orderDetailList == null) {
            return;
        }
        orderDetailList.forEach(item -> item.setImage(storefrontImageResolver.resolve(item.getImage())));
    }

    /**
     * Mock-payment-aware refund. When the project runs in mock-payment mode we
     * do not call WeChat Pay; we just signal "refund accepted" so the caller can
     * flip the order pay status. Real WeChat Pay integration is intentionally
     * stubbed out and not part of this upgrade.
     */
    private boolean refundIfNecessary(Orders orders) throws Exception {
        if (orders == null || !Orders.PAID.equals(orders.getPayStatus())) {
            return false;
        }

        if (Boolean.TRUE.equals(storefrontProperties.getMockPayment())) {
            return true;
        }

        String refund = weChatPayUtil.refund(
                orders.getNumber(),
                orders.getNumber(),
                MOCK_PAY_AMOUNT,
                MOCK_PAY_AMOUNT
        );
        return refund != null;
    }

    private String buildFullAddress(AddressBook addressBook) {
        if (addressBook == null) {
            return null;
        }

        StringBuilder builder = new StringBuilder();
        appendIfHasText(builder, addressBook.getProvinceName());
        appendIfHasText(builder, addressBook.getCityName());
        appendIfHasText(builder, addressBook.getDistrictName());
        appendIfHasText(builder, addressBook.getDetail());
        return builder.toString();
    }

    private void appendIfHasText(StringBuilder builder, String value) {
        if (!isBlank(value)) {
            builder.append(value.trim());
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private void validateMerchantOrderable(Campus campus, Merchant merchant) {
        if (campus == null || !Integer.valueOf(1).equals(campus.getStatus())) {
            throw new BaseException(MSG_CAMPUS_CLOSED);
        }
        if (merchant == null || !Integer.valueOf(1).equals(merchant.getStatus())) {
            throw new BaseException(MSG_MERCHANT_DISABLED);
        }
        if (!Integer.valueOf(1).equals(merchant.getBusinessStatus())) {
            throw new BaseException(MSG_MERCHANT_NOT_OPEN);
        }
    }

    private void sendOrderEventToMerchant(Long merchantId, Integer type, Long orderId, String content) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("type", type);
        payload.put("event", type != null && type == 1 ? "NEW_ORDER" : "ORDER_REMINDER");
        payload.put("orderId", orderId);
        payload.put("merchantId", merchantId);
        payload.put("content", content);
        String message;
        try {
            message = objectMapper.writeValueAsString(payload);
        } catch (Exception e) {
            throw new RuntimeException("Failed to serialize order event", e);
        }

        if (!schemaSupport.supportsEmployeeScope() || merchantId == null) {
            webSocketServer.sendToAllClient(message);
            return;
        }

        List<Employee> employees = employeeMapper.listByMerchantId(merchantId);
        if (!CollectionUtils.isEmpty(employees)) {
            for (Employee employee : employees) {
                webSocketServer.sendToClient(String.valueOf(employee.getId()), message);
            }
        }
        sendOrderEventToPlatformAdmins(message);
    }

    private void sendOrderEventToPlatformAdmins(String message) {
        List<Employee> platformAdmins = employeeMapper.listByAccountType(AccountTypeConstant.PLATFORM_ADMIN);
        if (CollectionUtils.isEmpty(platformAdmins)) {
            return;
        }
        for (Employee employee : platformAdmins) {
            webSocketServer.sendToClient(String.valueOf(employee.getId()), message);
        }
    }

    /**
     * Resolve the merchant id for a private user-side write/read that crosses the
     * merchant boundary (submit, repeat, estimated time). When the multi-merchant
     * schema is ready we delegate to {@link MerchantScopeGuard#requireExplicitMerchantId}
     * which throws if neither merchant nor shop id is supplied. We never fall
     * back to "first enabled merchant" for private writes in that mode.
     *
     * <p>In legacy single-merchant fallback we keep the old behaviour: pick the
     * first enabled merchant or the configured default merchant id.</p>
     */
    private Long resolvePrivateMerchantId(Long merchantId, Long shopId, String operation) {
        if (schemaSupport.isCoreSchemaReady()) {
            return merchantScopeGuard.requireExplicitMerchantId(merchantId, shopId, operation);
        }
        Long resolved = merchantId != null ? merchantId : shopId;
        if (resolved != null) {
            return resolved;
        }
        Merchant merchant = merchantService.getFirstEnabledMerchant(null);
        return merchant == null ? schemaSupport.getDefaultMerchantId() : merchant.getId();
    }

    /**
     * Backwards-compat helper used by paths that still need a "best effort"
     * merchant resolution. Callers must avoid this for private writes when the
     * schema is multi-merchant ready - use {@link #resolvePrivateMerchantId}
     * instead. Kept for internal helper uses only.
     */
    @SuppressWarnings("unused")
    private Long resolveLegacyMerchantId(Long merchantId) {
        if (merchantId != null) {
            return merchantId;
        }
        Merchant merchant = merchantService.getFirstEnabledMerchant(null);
        return merchant == null ? schemaSupport.getDefaultMerchantId() : merchant.getId();
    }
}
