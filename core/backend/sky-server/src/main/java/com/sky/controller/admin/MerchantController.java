package com.sky.controller.admin;

import com.sky.dto.MerchantDTO;
import com.sky.dto.MerchantPageQueryDTO;
import com.sky.entity.Merchant;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.MerchantService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/merchant")
@RequiredArgsConstructor
public class MerchantController {

    private final MerchantService merchantService;

    @PostMapping
    public Result<String> save(@RequestBody MerchantDTO merchantDTO) {
        merchantService.save(merchantDTO);
        return Result.success();
    }

    @PutMapping
    public Result<String> update(@RequestBody MerchantDTO merchantDTO) {
        merchantService.update(merchantDTO);
        return Result.success();
    }

    @GetMapping("/page")
    public Result<PageResult> page(MerchantPageQueryDTO queryDTO) {
        return Result.success(merchantService.pageQuery(queryDTO));
    }

    @GetMapping("/{id}")
    public Result<Merchant> getById(@PathVariable Long id) {
        return Result.success(merchantService.getById(id));
    }

    @PostMapping("/status/{status}")
    public Result<String> updateStatus(@PathVariable Integer status, Long id) {
        merchantService.updateStatus(status, id);
        return Result.success();
    }

    @PostMapping("/business-status/{status}")
    public Result<String> updateBusinessStatus(@PathVariable Integer status, Long id) {
        merchantService.updateBusinessStatus(status, id);
        return Result.success();
    }
}
