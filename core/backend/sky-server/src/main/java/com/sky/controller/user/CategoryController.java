package com.sky.controller.user;

import com.sky.entity.Category;
import com.sky.result.Result;
import com.sky.service.CategoryService;
import com.sky.service.MerchantService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController("userCategoryController")
@RequestMapping("/user/category")
public class CategoryController {

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private MerchantService merchantService;

    @GetMapping("/list")
    public Result<List<Category>> list(Integer type, Long merchantId) {
        return Result.success(categoryService.list(type, resolveMerchantId(merchantId)));
    }

    private Long resolveMerchantId(Long merchantId) {
        if (merchantId != null) {
            return merchantId;
        }
        return merchantService.getFirstEnabledMerchant(null).getId();
    }
}
