package com.sky.controller.user;

import com.sky.constant.StatusConstant;
import com.sky.entity.Dish;
import com.sky.result.Result;
import com.sky.service.DishService;
import com.sky.service.MerchantService;
import com.sky.utils.StorefrontImageResolver;
import com.sky.vo.DishVO;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;
import java.util.List;

@RestController("userDishController")
@RequestMapping("/user/dish")
@RequiredArgsConstructor
public class DishController {

    private static final Duration DISH_CACHE_TTL = Duration.ofHours(6);

    private final DishService dishService;

    private final RedisTemplate<String, Object> redisTemplate;

    private final MerchantService merchantService;

    private final StorefrontImageResolver storefrontImageResolver;

    @GetMapping("/list")
    public Result<List<DishVO>> list(Long categoryId, Long merchantId) {
        Long resolvedMerchantId = resolveMerchantId(merchantId);
        String cacheKey = "dish_" + resolvedMerchantId + "_" + categoryId;
        List<DishVO> list = (List<DishVO>) redisTemplate.opsForValue().get(cacheKey);
        if (list != null && !list.isEmpty()) {
            refreshDishImages(list);
            redisTemplate.opsForValue().set(cacheKey, list, DISH_CACHE_TTL);
            return Result.success(list);
        }

        Dish dish = Dish.builder()
                .merchantId(resolvedMerchantId)
                .categoryId(categoryId)
                .status(StatusConstant.ENABLE)
                .build();
        list = dishService.listWithFlavor(dish);
        redisTemplate.opsForValue().set(cacheKey, list, DISH_CACHE_TTL);
        return Result.success(list);
    }

    private void refreshDishImages(List<DishVO> list) {
        if (list == null || list.isEmpty()) {
            return;
        }
        list.forEach(item -> item.setImage(storefrontImageResolver.resolve(item.getImage())));
    }

    private Long resolveMerchantId(Long merchantId) {
        if (merchantId != null) {
            return merchantId;
        }
        return merchantService.getFirstEnabledMerchant(null).getId();
    }
}
