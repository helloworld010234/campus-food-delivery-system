package com.sky.controller.admin;

import com.sky.result.Result;
import com.sky.service.CampusService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController("adminShopController")
@RequestMapping("/admin/shop")
@RequiredArgsConstructor
public class ShopController {

    private final CampusService campusService;

    @GetMapping("/status")
    public Result<Integer> getStatus() {
        return Result.success(campusService.getCampusStatus());
    }

    @PutMapping("/{status}")
    public Result<String> setStatus(@PathVariable Integer status) {
        campusService.updateStatus(status);
        return Result.success();
    }
}
