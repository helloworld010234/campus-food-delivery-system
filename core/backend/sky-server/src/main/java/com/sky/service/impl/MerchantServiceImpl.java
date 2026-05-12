package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.dto.MerchantDTO;
import com.sky.dto.MerchantPageQueryDTO;
import com.sky.entity.Campus;
import com.sky.entity.Merchant;
import com.sky.exception.BaseException;
import com.sky.mapper.MerchantMapper;
import com.sky.properties.StorefrontProperties;
import com.sky.result.PageResult;
import com.sky.service.CampusService;
import com.sky.service.MerchantService;
import com.sky.support.MultiMerchantSchemaSupport;
import com.sky.utils.MerchantScopeUtils;
import com.sky.vo.MerchantVO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MerchantServiceImpl implements MerchantService {

    private final MerchantMapper merchantMapper;

    private final CampusService campusService;

    private final StorefrontProperties storefrontProperties;

    private final MultiMerchantSchemaSupport schemaSupport;

    @Override
    public void save(MerchantDTO merchantDTO) {
        ensureMerchantSchemaReadyForWrite();
        requirePlatformAccount();

        Merchant merchant = new Merchant();
        BeanUtils.copyProperties(merchantDTO, merchant);
        if (merchant.getCampusId() == null) {
            merchant.setCampusId(campusService.getDefaultCampus().getId());
        }
        if (merchant.getMerchantCode() == null || merchant.getMerchantCode().trim().isEmpty()) {
            merchant.setMerchantCode("merchant-" + System.currentTimeMillis());
        }
        if (merchant.getSort() == null) {
            merchant.setSort(0);
        }
        if (merchant.getStatus() == null) {
            merchant.setStatus(1);
        }
        if (merchant.getBusinessStatus() == null) {
            merchant.setBusinessStatus(1);
        }
        merchantMapper.insert(merchant);
    }

    @Override
    public void update(MerchantDTO merchantDTO) {
        ensureMerchantSchemaReadyForWrite();
        requirePlatformAccount();
        Merchant merchant = merchantMapper.getById(merchantDTO.getId());
        if (merchant == null) {
            throw new BaseException("商户不存在");
        }

        Merchant toUpdate = new Merchant();
        BeanUtils.copyProperties(merchantDTO, toUpdate);
        toUpdate.setId(merchant.getId());
        merchantMapper.update(toUpdate);
    }

    @Override
    public PageResult pageQuery(MerchantPageQueryDTO queryDTO) {
        if (!schemaSupport.supportsMerchantTable()) {
            return new PageResult(1, Collections.singletonList(buildFallbackMerchant(campusService.getById(queryDTO.getCampusId()))));
        }
        PageHelper.startPage(queryDTO.getPage(), queryDTO.getPageSize());
        Page<Merchant> page = merchantMapper.pageQuery(queryDTO);
        return new PageResult(page.getTotal(), page.getResult());
    }

    @Override
    public Merchant getById(Long id) {
        if (id == null || !schemaSupport.supportsMerchantTable()) {
            return buildFallbackMerchant(campusService.getDefaultCampus());
        }
        Merchant merchant = merchantMapper.getById(id);
        if (merchant == null) {
            return buildFallbackMerchant(campusService.getDefaultCampus());
        }
        // Enforce merchant-scope read: a merchant account must not read another
        // merchant's record. Platform accounts pass through.
        MerchantScopeUtils.assertAccessible(merchant.getId());
        return merchant;
    }

    @Override
    public void updateStatus(Integer status, Long id) {
        ensureMerchantSchemaReadyForWrite();
        requirePlatformAccount();
        Merchant merchant = getById(id);
        merchantMapper.update(Merchant.builder()
                .id(merchant.getId())
                .status(status)
                .build());
    }

    @Override
    public void updateBusinessStatus(Integer businessStatus, Long id) {
        ensureMerchantSchemaReadyForWrite();
        Merchant merchant = getById(id);
        MerchantScopeUtils.assertAccessible(merchant.getId());
        merchantMapper.update(Merchant.builder()
                .id(merchant.getId())
                .businessStatus(businessStatus)
                .build());
    }

    @Override
    public List<MerchantVO> listForUser(Long campusId) {
        if (!schemaSupport.supportsMerchantTable()) {
            Campus campus = campusService.getById(campusId);
            return Collections.singletonList(buildFallbackMerchantVO(campus));
        }
        List<MerchantVO> list = merchantMapper.listForUser(campusId);
        if (list == null || list.isEmpty()) {
            Campus campus = campusService.getById(campusId);
            return Collections.singletonList(buildFallbackMerchantVO(campus));
        }
        return list;
    }

    @Override
    public MerchantVO getMerchantInfo(Long merchantId) {
        if (!schemaSupport.supportsMerchantTable()) {
            return buildFallbackMerchantVO(campusService.getDefaultCampus());
        }
        MerchantVO merchantVO = merchantMapper.getMerchantInfo(merchantId);
        if (merchantVO != null) {
            return merchantVO;
        }
        return buildFallbackMerchantVO(campusService.getDefaultCampus());
    }

    @Override
    public Merchant getFirstEnabledMerchant(Long campusId) {
        if (!schemaSupport.supportsMerchantTable()) {
            return buildFallbackMerchant(campusService.getById(campusId));
        }
        Long actualCampusId = campusId == null ? campusService.getDefaultCampus().getId() : campusId;
        Merchant merchant = merchantMapper.getFirstEnabledByCampusId(actualCampusId);
        return merchant == null ? buildFallbackMerchant(campusService.getById(actualCampusId)) : merchant;
    }

    private void requirePlatformAccount() {
        if (MerchantScopeUtils.isMerchantAccount()) {
            throw new BaseException("商户账号无权执行该操作");
        }
    }

    private void ensureMerchantSchemaReadyForWrite() {
        if (!schemaSupport.supportsMerchantTable()) {
            throw new BaseException("褰撳墠鐜浠嶅浜庡崟搴楀吋瀹规ā寮忥紝璇峰厛瀹屾垚澶氬晢鎴锋暟鎹簱杩佺Щ");
        }
    }

    private Merchant buildFallbackMerchant(Campus campus) {
        Long campusId = campus == null ? 1L : campus.getId();
        return Merchant.builder()
                .id(schemaSupport.getDefaultMerchantId())
                .campusId(campusId)
                .merchantCode("merchant-001")
                .name(storefrontProperties.getShopName())
                .contactPhone(storefrontProperties.getPhone())
                .addressDetail(storefrontProperties.getShopAddress())
                .description("Default merchant")
                .announcement("Default merchant")
                .sort(0)
                .status(1)
                .businessStatus(1)
                .build();
    }

    private MerchantVO buildFallbackMerchantVO(Campus campus) {
        Merchant merchant = buildFallbackMerchant(campus);
        return MerchantVO.builder()
                .id(merchant.getId())
                .campusId(merchant.getCampusId())
                .campusName(campus == null ? "Campus Delivery" : campus.getName())
                .campusStatus(campus == null ? 1 : campus.getStatus())
                .merchantCode(merchant.getMerchantCode())
                .name(merchant.getName())
                .description(merchant.getDescription())
                .announcement(merchant.getAnnouncement())
                .contactPhone(merchant.getContactPhone())
                .addressDetail(merchant.getAddressDetail())
                .sort(merchant.getSort())
                .status(merchant.getStatus())
                .businessStatus(merchant.getBusinessStatus())
                .deliveryFee(campus == null ? storefrontProperties.getDeliveryFee() : campus.getDeliveryFee())
                .estimatedDeliveryMinutes(campus == null ? storefrontProperties.getEstimatedDeliveryMinutes() : campus.getEstimatedDeliveryMinutes())
                .servicePhone(campus == null ? storefrontProperties.getPhone() : campus.getServicePhone())
                .build();
    }
}
