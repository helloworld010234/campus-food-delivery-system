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
import com.sky.service.CampusService;
import com.sky.service.MerchantService;
import com.sky.service.OrderService;
import com.sky.support.MultiMerchantSchemaSupport;
import com.sky.utils.MerchantScopeUtils;
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

@Service
public class OrderServiceImpl implements OrderService {

    private static final BigDecimal MOCK_PAY_AMOUNT = new BigDecimal("0.01");

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

    @Override
    @Transactional
    public OrderSubmitVO submitOrder(OrdersSubmitDTO ordersSubmitDTO) {
        Long requestedMerchantId = ordersSubmitDTO.getMerchantId() != null
                ? ordersSubmitDTO.getMerchantId()
                : ordersSubmitDTO.getShopId();
        Long merchantId = resolveRequestedMerchantId(requestedMerchantId);

        AddressBook addressBook = addressBookMapper.getById(ordersSubmitDTO.getAddressBookId());
        if (addressBook == null) {
            throw new AddressBookBusinessException("鐢ㄦ埛鍦板潃涓虹┖锛屼笉鑳戒笅鍗?");
        }

        Merchant merchant = merchantService.getById(merchantId);
        Campus campus = campusService.getById(merchant.getCampusId());
        validateMerchantOrderable(campus, merchant);

        Long userId = BaseContext.getCurrentId();
        ShoppingCart condition = ShoppingCart.builder()
                .userId(userId)
                .merchantId(schemaSupport.supportsShoppingCartScope() ? merchant.getId() : null)
                .build();
        List<ShoppingCart> cartList = shoppingCartMapper.list(condition);
        if (CollectionUtils.isEmpty(cartList)) {
            throw new ShoppingCartBusinessException("璐墿杞︿负绌猴紝涓嶈兘涓嬪崟");
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
            throw new OrderBusinessException("璁㈠崟涓嶅瓨鍦?");
        }
        if (Orders.PAID.equals(ordersDB.getPayStatus())) {
            throw new OrderBusinessException("璇ヨ鍗曞凡鏀粯");
        }

        if (Boolean.TRUE.equals(storefrontProperties.getMockPayment())) {
            paySuccess(ordersPaymentDTO.getOrderNumber());
            return OrderPaymentVO.builder().mockPay(Boolean.TRUE).build();
        }

        User user = userMapper.getById(userId);
        JsonNode jsonObject = weChatPayUtil.pay(
                ordersPaymentDTO.getOrderNumber(),
                MOCK_PAY_AMOUNT,
                (ordersDB.getMerchantName() == null ? "璁㈠崟" : ordersDB.getMerchantName() + "璁㈠崟"),
                user.getOpenid()
        );

        if ("ORDERPAID".equals(jsonObject.get("code").asText())) {
            throw new OrderBusinessException("璇ヨ鍗曞凡鏀粯");
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

        sendOrderEventToMerchant(ordersDB.getMerchantId(), 1, ordersDB.getId(), "璁㈠崟鍙凤細" + outTradeNo);
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
            throw new OrderBusinessException("璁㈠崟鐘舵€侀敊璇?");
        }

        Orders toUpdate = new Orders();
        toUpdate.setId(orders.getId());
        if (refundIfNecessary(orders)) {
            toUpdate.setPayStatus(Orders.REFUND);
        }
        toUpdate.setStatus(Orders.CANCELLED);
        toUpdate.setCancelReason("鐢ㄦ埛鍙栨秷");
        toUpdate.setCancelTime(LocalDateTime.now());
        ordersMapper.update(toUpdate);
    }

    @Override
    public void repetition(Long id) {
        Orders orders = getAccessibleUserOrder(id);
        Long userId = BaseContext.getCurrentId();
        List<OrderDetail> orderDetailList = orderDetailMapper.getByOrderId(id);
        List<ShoppingCart> shoppingCartList = orderDetailList.stream().map(item -> {
            ShoppingCart shoppingCart = new ShoppingCart();
            BeanUtils.copyProperties(item, shoppingCart, "id");
            shoppingCart.setMerchantId(schemaSupport.supportsShoppingCartScope() ? resolveRequestedMerchantId(orders.getMerchantId()) : null);
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
            ordersPageQueryDTO.setMerchantId(MerchantScopeUtils.resolveQueryMerchantId(ordersPageQueryDTO.getMerchantId()));
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
                ? MerchantScopeUtils.resolveQueryMerchantId(null)
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
            throw new OrderBusinessException("璁㈠崟鐘舵€侀敊璇?");
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
            throw new OrderBusinessException("璁㈠崟鐘舵€侀敊璇?");
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
            throw new OrderBusinessException("璁㈠崟鐘舵€侀敊璇?");
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
        sendOrderEventToMerchant(ordersDB.getMerchantId(), 2, id, "璁㈠崟鍙凤細" + ordersDB.getNumber());
    }

    @Override
    public LocalDateTime getEstimatedDeliveryTime(Long merchantId, String customerAddress) {
        Merchant merchant = merchantService.getById(resolveRequestedMerchantId(merchantId));
        Campus campus = campusService.getById(merchant.getCampusId());
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
            throw new OrderBusinessException("璁㈠崟涓嶅瓨鍦?");
        }

        if (BaseContext.getCurrentAccountType() != null) {
            MerchantScopeUtils.assertAccessible(orders.getMerchantId());
        } else if (!BaseContext.getCurrentId().equals(orders.getUserId())) {
            throw new BaseException("鏃犳潈鏌ョ湅璇ヨ鍗?");
        }
        return orders;
    }

    private Orders getAccessibleUserOrder(Long id) {
        Orders orders = getAccessibleOrder(id);
        if (BaseContext.getCurrentAccountType() != null) {
            throw new BaseException("褰撳墠鎺ュ彛浠呴檺鐢ㄦ埛浣跨敤");
        }
        return orders;
    }

    private Orders getAccessibleAdminOrder(Long id) {
        Orders orders = getAccessibleOrder(id);
        if (BaseContext.getCurrentAccountType() == null) {
            throw new BaseException("褰撳墠鎺ュ彛浠呴檺鍚庡彴浣跨敤");
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
            throw new BaseException("褰撳墠鏍″洯鏆傛湭寮€鏀炬湇鍔?");
        }
        if (merchant == null || !Integer.valueOf(1).equals(merchant.getStatus())) {
            throw new BaseException("褰撳墠鍟嗘埛涓嶅彲鐢?");
        }
        if (!Integer.valueOf(1).equals(merchant.getBusinessStatus())) {
            throw new BaseException("褰撳墠鍟嗘埛宸叉墦鐑?");
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

    private Long resolveRequestedMerchantId(Long merchantId) {
        if (merchantId != null) {
            return merchantId;
        }
        Merchant merchant = merchantService.getFirstEnabledMerchant(null);
        return merchant == null ? schemaSupport.getDefaultMerchantId() : merchant.getId();
    }
}
