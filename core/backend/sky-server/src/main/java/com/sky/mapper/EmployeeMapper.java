package com.sky.mapper;

import com.github.pagehelper.Page;
import com.sky.anotation.AutoFill;
import com.sky.dto.EmployeePageQueryDTO;
import com.sky.entity.Employee;
import com.sky.enumeration.OperationType;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface EmployeeMapper {

    @Select("select * from employee where username = #{username}")
    Employee getByUsername(String username);

    @Insert("insert into employee (merchant_id, account_type, name, username, password, phone, sex, id_number, status, create_time, update_time, create_user, update_user) " +
            "values (#{merchantId}, #{accountType}, #{name}, #{username}, #{password}, #{phone}, #{sex}, #{idNumber}, #{status}, #{createTime}, #{updateTime}, #{createUser}, #{updateUser})")
    @AutoFill(OperationType.INSERT)
    void insert(Employee employee);

    @Insert("insert into employee (name, username, password, phone, sex, id_number, status, create_time, update_time, create_user, update_user) " +
            "values (#{name}, #{username}, #{password}, #{phone}, #{sex}, #{idNumber}, #{status}, #{createTime}, #{updateTime}, #{createUser}, #{updateUser})")
    @AutoFill(OperationType.INSERT)
    void insertLegacy(Employee employee);

    Page<Employee> pageQuery(EmployeePageQueryDTO pageQueryDTO);

    @AutoFill(OperationType.UPDATE)
    void update(Employee employee);

    @Select("select * from employee where id = #{id}")
    Employee getById(Long id);

    @Select("select * from employee where merchant_id = #{merchantId} and status = 1")
    List<Employee> listByMerchantId(Long merchantId);

    @Select("select * from employee where account_type = #{accountType} and status = 1")
    List<Employee> listByAccountType(Integer accountType);
}
