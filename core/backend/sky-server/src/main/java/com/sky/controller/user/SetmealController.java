package com.sky.controller.user;

import com.sky.constant.StatusConstant;
import com.sky.entity.Setmeal;
import com.sky.result.Result;
import com.sky.service.MerchantService;
import com.sky.service.SetmealService;
import com.sky.vo.DishItemVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController("userSetmealController")
@RequestMapping("/user/setmeal")
public class SetmealController {

    @Autowired
    private SetmealService setmealService;

    @Autowired
    private MerchantService merchantService;

    @GetMapping("/list")
    @Cacheable(cacheNames = "setmealCache", key = "#merchantId + '_' + #categoryId")
    public Result<List<Setmeal>> list(Long categoryId, Long merchantId) {
        Long resolvedMerchantId = resolveMerchantId(merchantId);
        Setmeal setmeal = Setmeal.builder()
                .merchantId(resolvedMerchantId)
                .categoryId(categoryId)
                .status(StatusConstant.ENABLE)
                .build();
        return Result.success(setmealService.list(setmeal));
    }

    @GetMapping("/dish/{id}")
    public Result<List<DishItemVO>> dishList(@PathVariable("id") Long id) {
        return Result.success(setmealService.getDishItemById(id));
    }

    private Long resolveMerchantId(Long merchantId) {
        if (merchantId != null) {
            return merchantId;
        }
        return merchantService.getFirstEnabledMerchant(null).getId();
    }
}
