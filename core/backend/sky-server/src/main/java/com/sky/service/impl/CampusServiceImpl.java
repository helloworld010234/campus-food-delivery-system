package com.sky.service.impl;

import com.sky.entity.Campus;
import com.sky.mapper.CampusMapper;
import com.sky.properties.StorefrontProperties;
import com.sky.service.CampusService;
import com.sky.support.MultiMerchantSchemaSupport;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class CampusServiceImpl implements CampusService {

    private static final String DEFAULT_CAMPUS_STATUS_KEY = "campus:status:default";

    @Autowired
    private CampusMapper campusMapper;

    @Autowired
    private StorefrontProperties storefrontProperties;

    @Autowired
    private MultiMerchantSchemaSupport schemaSupport;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Override
    public Campus getDefaultCampus() {
        if (!schemaSupport.supportsCampusTable()) {
            return buildFallbackCampus();
        }
        Campus campus = campusMapper.getDefaultCampus();
        return campus == null ? buildFallbackCampus() : campus;
    }

    @Override
    public Campus getById(Long id) {
        if (id == null) {
            return getDefaultCampus();
        }
        if (!schemaSupport.supportsCampusTable()) {
            return buildFallbackCampus();
        }
        Campus campus = campusMapper.getById(id);
        return campus == null ? buildFallbackCampus() : campus;
    }

    @Override
    public Integer getCampusStatus() {
        Campus campus = getDefaultCampus();
        return campus == null || campus.getStatus() == null ? 1 : campus.getStatus();
    }

    @Override
    public void updateStatus(Integer status) {
        Campus campus = getDefaultCampus();
        if (!schemaSupport.supportsCampusTable()) {
            redisTemplate.opsForValue().set(DEFAULT_CAMPUS_STATUS_KEY, status);
            return;
        }
        campusMapper.update(Campus.builder()
                .id(campus.getId())
                .status(status)
                .build());
    }

    private Campus buildFallbackCampus() {
        return Campus.builder()
                .id(1L)
                .code("campus-001")
                .name("Campus Delivery")
                .address(storefrontProperties.getShopAddress())
                .servicePhone(storefrontProperties.getPhone())
                .status(resolveFallbackStatus())
                .deliveryFee(storefrontProperties.getDeliveryFee())
                .estimatedDeliveryMinutes(storefrontProperties.getEstimatedDeliveryMinutes())
                .build();
    }

    private Integer resolveFallbackStatus() {
        Object status = redisTemplate.opsForValue().get(DEFAULT_CAMPUS_STATUS_KEY);
        if (status == null) {
            return 1;
        }
        if (status instanceof Number) {
            return ((Number) status).intValue();
        }
        return Integer.valueOf(status.toString());
    }
}
