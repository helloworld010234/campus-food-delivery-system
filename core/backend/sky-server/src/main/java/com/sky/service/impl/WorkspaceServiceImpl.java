package com.sky.service.impl;

import com.sky.constant.StatusConstant;
import com.sky.entity.Orders;
import com.sky.mapper.DishMapper;
import com.sky.mapper.OrdersMapper;
import com.sky.mapper.SetmealMapper;
import com.sky.mapper.UserMapper;
import com.sky.security.MerchantScopeGuard;
import com.sky.service.WorkspaceService;
import com.sky.vo.BusinessDataVO;
import com.sky.vo.DishOverViewVO;
import com.sky.vo.OrderOverViewVO;
import com.sky.vo.SetmealOverViewVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Workspace dashboard service.
 *
 * <h2>Platform vs merchant semantics</h2>
 * Every workspace metric ("today" business data, order overview, dish
 * overview, setmeal overview) routes its merchant scope through
 * {@link MerchantScopeGuard#resolveAdminQueryMerchantId(Long)}:
 * <ul>
 *   <li>Platform admin: {@code null} merchant id is allowed and means
 *       "aggregate across all merchants" - the platform-level dashboard.</li>
 *   <li>Merchant admin / merchant staff: the guard substitutes the bound
 *       merchant id, so the workspace numbers always reflect a single
 *       merchant. Merchant accounts cannot pass a different id and cannot
 *       see global metrics.</li>
 * </ul>
 *
 * The same resolution rule is used by the report and report-export paths,
 * so on-screen workspace, on-screen report, and Excel export always share
 * an identical merchant scope.
 */
@Service
public class WorkspaceServiceImpl implements WorkspaceService {

    @Autowired
    private OrdersMapper orderMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private DishMapper dishMapper;

    @Autowired
    private SetmealMapper setmealMapper;

    @Autowired
    private MerchantScopeGuard merchantScopeGuard;

    @Override
    public BusinessDataVO getBusinessData(LocalDateTime begin, LocalDateTime end) {
        Map<String, Object> map = new HashMap<>();
        map.put("begin", begin);
        map.put("end", end);
        map.put("merchantId", merchantScopeGuard.resolveAdminQueryMerchantId(null));

        Integer totalOrderCount = orderMapper.countByMap(map);

        map.put("status", Orders.COMPLETED);
        Double turnover = orderMapper.sumByMap(map);
        turnover = turnover == null ? 0.0 : turnover;
        Integer validOrderCount = orderMapper.countByMap(map);

        Double unitPrice = 0.0;
        Double orderCompletionRate = 0.0;
        if (totalOrderCount != null && totalOrderCount != 0 && validOrderCount != null && validOrderCount != 0) {
            orderCompletionRate = validOrderCount.doubleValue() / totalOrderCount;
            unitPrice = turnover / validOrderCount;
        }

        Integer newUsers = userMapper.getUserByMap(map);

        return BusinessDataVO.builder()
                .turnover(turnover)
                .validOrderCount(validOrderCount == null ? 0 : validOrderCount)
                .orderCompletionRate(orderCompletionRate)
                .unitPrice(unitPrice)
                .newUsers(newUsers == null ? 0 : newUsers)
                .build();
    }

    @Override
    public OrderOverViewVO getOrderOverView() {
        Map<String, Object> map = new HashMap<>();
        map.put("begin", LocalDateTime.now().with(LocalTime.MIN));
        map.put("merchantId", merchantScopeGuard.resolveAdminQueryMerchantId(null));

        map.put("status", Orders.TO_BE_CONFIRMED);
        Integer waitingOrders = orderMapper.countByMap(map);

        map.put("status", Orders.CONFIRMED);
        Integer deliveredOrders = orderMapper.countByMap(map);

        map.put("status", Orders.COMPLETED);
        Integer completedOrders = orderMapper.countByMap(map);

        map.put("status", Orders.CANCELLED);
        Integer cancelledOrders = orderMapper.countByMap(map);

        map.put("status", null);
        Integer allOrders = orderMapper.countByMap(map);

        return OrderOverViewVO.builder()
                .waitingOrders(waitingOrders == null ? 0 : waitingOrders)
                .deliveredOrders(deliveredOrders == null ? 0 : deliveredOrders)
                .completedOrders(completedOrders == null ? 0 : completedOrders)
                .cancelledOrders(cancelledOrders == null ? 0 : cancelledOrders)
                .allOrders(allOrders == null ? 0 : allOrders)
                .build();
    }

    @Override
    public DishOverViewVO getDishOverView() {
        Map<String, Object> map = new HashMap<>();
        map.put("merchantId", merchantScopeGuard.resolveAdminQueryMerchantId(null));
        map.put("status", StatusConstant.ENABLE);
        Integer sold = dishMapper.countByMap(map);

        map.put("status", StatusConstant.DISABLE);
        Integer discontinued = dishMapper.countByMap(map);

        return DishOverViewVO.builder()
                .sold(sold == null ? 0 : sold)
                .discontinued(discontinued == null ? 0 : discontinued)
                .build();
    }

    @Override
    public SetmealOverViewVO getSetmealOverView() {
        Map<String, Object> map = new HashMap<>();
        map.put("merchantId", merchantScopeGuard.resolveAdminQueryMerchantId(null));
        map.put("status", StatusConstant.ENABLE);
        Integer sold = setmealMapper.countByMap(map);

        map.put("status", StatusConstant.DISABLE);
        Integer discontinued = setmealMapper.countByMap(map);

        return SetmealOverViewVO.builder()
                .sold(sold == null ? 0 : sold)
                .discontinued(discontinued == null ? 0 : discontinued)
                .build();
    }
}
