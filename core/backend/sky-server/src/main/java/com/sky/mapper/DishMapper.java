package com.sky.mapper;

import com.github.pagehelper.Page;
import com.sky.anotation.AutoFill;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.entity.DishFlavor;
import com.sky.enumeration.OperationType;
import com.sky.vo.DishVO;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

@Mapper
public interface DishMapper {

    @Select("select count(id) from dish where category_id = #{categoryId}")
    Integer countByCategoryId(Long categoryId);

    @AutoFill(OperationType.INSERT)
    void insert(Dish dish);

    @Insert("insert into dish (status, name, category_id, price, image, description, create_time, update_time, create_user, update_user) " +
            "values (#{status}, #{name}, #{categoryId}, #{price}, #{image}, #{description}, #{createTime}, #{updateTime}, #{createUser}, #{updateUser})")
    @AutoFill(OperationType.INSERT)
    void insertLegacy(Dish dish);

    void insertBatch(List<DishFlavor> flavors);

    Page<DishVO> pageQuery(DishPageQueryDTO queryDTO);

    @Select("select * from dish where id = #{id}")
    Dish getById(Long id);

    void deleteBatch(List<Long> dishIds);

    @AutoFill(OperationType.UPDATE)
    void update(Dish dish);

    List<Dish> list(Dish dish);

    @Select("select a.* from dish a left join setmeal_dish b on a.id = b.dish_id where b.setmeal_id = #{setmealId}")
    List<Dish> getBySetmealId(Long setmealId);

    Integer countByMap(Map<String, Object> map);
}
