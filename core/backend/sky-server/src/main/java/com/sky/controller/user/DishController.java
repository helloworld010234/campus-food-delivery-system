package com.sky.controller.user;

import com.sky.constant.StatusConstant;
import com.sky.entity.Dish;
import com.sky.result.Result;
import com.sky.service.DishService;
import com.sky.service.MerchantService;
import com.sky.vo.DishVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController("userDishController")
@RequestMapping("/user/dish")
public class DishController {

    @Autowired
    private DishService dishService;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private MerchantService merchantService;

    @GetMapping("/list")
    public Result<List<DishVO>> list(Long categoryId, Long merchantId) {
        Long resolvedMerchantId = resolveMerchantId(merchantId);
        String cacheKey = "dish_" + resolvedMerchantId + "_" + categoryId;
        List<DishVO> list = (List<DishVO>) redisTemplate.opsForValue().get(cacheKey);
        if (list != null && !list.isEmpty()) {
            return Result.success(list);
        }

        Dish dish = Dish.builder()
                .merchantId(resolvedMerchantId)
                .categoryId(categoryId)
                .status(StatusConstant.ENABLE)
                .build();
        list = dishService.listWithFlavor(dish);
        redisTemplate.opsForValue().set(cacheKey, list);
        return Result.success(list);
    }

    private Long resolveMerchantId(Long merchantId) {
        if (merchantId != null) {
            return merchantId;
        }
        return merchantService.getFirstEnabledMerchant(null).getId();
    }
}
