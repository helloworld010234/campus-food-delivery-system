package com.sky.service;

import com.sky.dto.MerchantDTO;
import com.sky.dto.MerchantPageQueryDTO;
import com.sky.entity.Merchant;
import com.sky.result.PageResult;
import com.sky.vo.MerchantVO;

import java.util.List;

public interface MerchantService {

    void save(MerchantDTO merchantDTO);

    void update(MerchantDTO merchantDTO);

    PageResult pageQuery(MerchantPageQueryDTO queryDTO);

    Merchant getById(Long id);

    void updateStatus(Integer status, Long id);

    void updateBusinessStatus(Integer businessStatus, Long id);

    List<MerchantVO> listForUser(Long campusId);

    MerchantVO getMerchantInfo(Long merchantId);

    Merchant getFirstEnabledMerchant(Long campusId);
}
