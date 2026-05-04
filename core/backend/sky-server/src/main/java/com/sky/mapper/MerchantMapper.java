package com.sky.mapper;

import com.github.pagehelper.Page;
import com.sky.anotation.AutoFill;
import com.sky.dto.MerchantPageQueryDTO;
import com.sky.entity.Merchant;
import com.sky.enumeration.OperationType;
import com.sky.vo.MerchantVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface MerchantMapper {

    @AutoFill(OperationType.INSERT)
    void insert(Merchant merchant);

    @AutoFill(OperationType.UPDATE)
    void update(Merchant merchant);

    Page<Merchant> pageQuery(MerchantPageQueryDTO queryDTO);

    @Select("select * from merchant where id = #{id}")
    Merchant getById(Long id);

    @Select("select * from merchant where campus_id = #{campusId} and status = 1 order by sort asc, id asc limit 1")
    Merchant getFirstEnabledByCampusId(Long campusId);

    List<MerchantVO> listForUser(Long campusId);

    MerchantVO getMerchantInfo(Long merchantId);
}
