package com.sky.controller.user;

import com.sky.entity.Campus;
import com.sky.service.CampusService;
import com.sky.service.MerchantService;
import com.sky.vo.MerchantVO;
import com.sky.result.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController("userShopController")
@RequestMapping("/user/shop")
public class ShopController {

    @Autowired
    private CampusService campusService;

    @Autowired
    private MerchantService merchantService;

    @GetMapping("/status")
    public Result<Integer> getStatus(Long merchantId, Long shopId) {
        Long resolvedMerchantId = resolveMerchantId(merchantId, shopId);
        Campus campus = campusService.getDefaultCampus();
        if (resolvedMerchantId == null) {
            return Result.success(campus.getStatus());
        }
        MerchantVO merchantVO = merchantService.getMerchantInfo(resolvedMerchantId);
        int available = Integer.valueOf(1).equals(campus.getStatus())
                && Integer.valueOf(1).equals(merchantVO.getStatus())
                && Integer.valueOf(1).equals(merchantVO.getBusinessStatus()) ? 1 : 0;
        return Result.success(available);
    }

    @GetMapping("/list")
    public Result<List<MerchantVO>> list(Long campusId) {
        return Result.success(merchantService.listForUser(campusId));
    }

    @GetMapping("/getMerchantInfo")
    public Result<Map<String, Object>> getMerchantInfo(Long merchantId, Long shopId) {
        MerchantVO merchantVO = merchantService.getMerchantInfo(resolveMerchantId(merchantId, shopId));
        Map<String, Object> data = new HashMap<>();
        data.put("merchantId", merchantVO.getId());
        data.put("shopId", merchantVO.getId());
        data.put("shopName", merchantVO.getName());
        data.put("shopAddress", merchantVO.getAddressDetail());
        data.put("phone", merchantVO.getContactPhone());
        data.put("deliveryFee", merchantVO.getDeliveryFee());
        data.put("estimatedDeliveryMinutes", merchantVO.getEstimatedDeliveryMinutes());
        data.put("status", merchantVO.getStatus());
        data.put("businessStatus", merchantVO.getBusinessStatus());
        data.put("campusStatus", merchantVO.getCampusStatus());
        data.put("logo", merchantVO.getLogo());
        data.put("coverImage", merchantVO.getCoverImage());
        data.put("description", merchantVO.getDescription());
        data.put("announcement", merchantVO.getAnnouncement());
        data.put("campusName", merchantVO.getCampusName());
        return Result.success(data);
    }

    private Long resolveMerchantId(Long merchantId, Long shopId) {
        return merchantId != null ? merchantId : shopId;
    }
}
