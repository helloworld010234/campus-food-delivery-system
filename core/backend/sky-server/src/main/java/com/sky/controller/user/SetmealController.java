package com.sky.controller.user;

import com.sky.constant.StatusConstant;
import com.sky.entity.Setmeal;
import com.sky.result.Result;
import com.sky.service.MerchantService;
import com.sky.service.SetmealService;
import com.sky.utils.StorefrontImageResolver;
import com.sky.vo.DishItemVO;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;
import java.util.List;

@RestController("userSetmealController")
@RequestMapping("/user/setmeal")
@RequiredArgsConstructor
public class SetmealController {

    private static final Duration SETMEAL_CACHE_TTL = Duration.ofHours(6);

    private final SetmealService setmealService;

    private final MerchantService merchantService;

    private final RedisTemplate<String, Object> redisTemplate;

    private final StorefrontImageResolver storefrontImageResolver;

    @GetMapping("/list")
    public Result<List<Setmeal>> list(Long categoryId, Long merchantId) {
        Long resolvedMerchantId = resolveMerchantId(merchantId);
        String cacheKey = "setmeal_" + resolvedMerchantId + "_" + categoryId;

        List<Setmeal> cachedList = (List<Setmeal>) redisTemplate.opsForValue().get(cacheKey);
        if (cachedList != null && !cachedList.isEmpty()) {
            refreshSetmealImages(cachedList);
            redisTemplate.opsForValue().set(cacheKey, cachedList, SETMEAL_CACHE_TTL);
            return Result.success(cachedList);
        }

        Setmeal setmeal = Setmeal.builder()
                .merchantId(resolvedMerchantId)
                .categoryId(categoryId)
                .status(StatusConstant.ENABLE)
                .build();
        List<Setmeal> list = setmealService.list(setmeal);
        redisTemplate.opsForValue().set(cacheKey, list, SETMEAL_CACHE_TTL);
        return Result.success(list);
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

    private void refreshSetmealImages(List<Setmeal> list) {
        if (list == null || list.isEmpty()) {
            return;
        }
        list.forEach(item -> item.setImage(storefrontImageResolver.resolve(item.getImage())));
    }
}
