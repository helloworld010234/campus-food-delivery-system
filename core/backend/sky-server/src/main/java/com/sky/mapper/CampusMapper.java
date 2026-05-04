package com.sky.mapper;

import com.sky.anotation.AutoFill;
import com.sky.entity.Campus;
import com.sky.enumeration.OperationType;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface CampusMapper {

    @Select("select * from campus order by id limit 1")
    Campus getDefaultCampus();

    @Select("select * from campus where id = #{id}")
    Campus getById(Long id);

    @AutoFill(OperationType.INSERT)
    void insert(Campus campus);

    @AutoFill(OperationType.UPDATE)
    void update(Campus campus);
}
