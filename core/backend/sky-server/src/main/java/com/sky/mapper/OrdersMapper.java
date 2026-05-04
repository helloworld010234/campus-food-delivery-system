package com.sky.mapper;

import com.github.pagehelper.Page;
import com.sky.dto.GoodsSalesDTO;
import com.sky.dto.OrdersPageQueryDTO;
import com.sky.entity.Orders;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Mapper
public interface OrdersMapper {

    void insert(Orders orders);

    void insertLegacy(Orders orders);

    @Select("select * from orders where number = #{orderNumber}")
    Orders getByNumber(String orderNumber);

    void update(Orders orders);

    Page<Orders> pageQuery(OrdersPageQueryDTO ordersPageQueryDTO);

    @Select("select * from orders where id = #{id}")
    Orders getById(Long id);

    @Select("select count(id) from orders where status = #{status} and (#{merchantId} is null or merchant_id = #{merchantId})")
    Integer countStatus(@Param("status") Integer status, @Param("merchantId") Long merchantId);

    @Select("select count(id) from orders where status = #{status}")
    Integer countStatusLegacy(@Param("status") Integer status);

    @Select("select * from orders where status = #{status} and order_time < #{time}")
    List<Orders> getByStatusAndOrderTimeLT(@Param("status") Integer status, @Param("time") LocalDateTime time);

    @Select("select * from orders where number = #{number} and user_id = #{userId}")
    Orders getByNumberAndUserId(@Param("number") String number, @Param("userId") Long userId);

    Double sumByMap(Map<String, Object> map);

    Integer countByMap(Map<String, Object> map);

    List<GoodsSalesDTO> getSalesTop10(@Param("begin") LocalDateTime begin, @Param("end") LocalDateTime end, @Param("merchantId") Long merchantId);
}
