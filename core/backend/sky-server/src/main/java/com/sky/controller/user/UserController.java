package com.sky.controller.user;

import com.sky.constant.JwtClaimsConstant;
import com.sky.dto.UserLoginDTO;
import com.sky.entity.Campus;
import com.sky.entity.Merchant;
import com.sky.entity.User;
import com.sky.properties.JwtProperties;
import com.sky.result.Result;
import com.sky.service.CampusService;
import com.sky.service.MerchantService;
import com.sky.service.UserService;
import com.sky.utils.JwtUtil;
import com.sky.vo.UserLoginVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/user/user")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private JwtProperties jwtProperties;

    @Autowired
    private CampusService campusService;

    @Autowired
    private MerchantService merchantService;

    @PostMapping("/login")
    public Result<UserLoginVO> login(@RequestBody UserLoginDTO userLoginDTO) {
        User user = userService.wxLogin(userLoginDTO);
        Map<String, Object> claims = new HashMap<>();
        claims.put(JwtClaimsConstant.USER_ID, user.getId());
        String token = JwtUtil.createJWT(jwtProperties.getUserSecretKey(), jwtProperties.getUserTtl(), claims);

        Campus campus = campusService.getDefaultCampus();
        Merchant defaultMerchant = merchantService.getFirstEnabledMerchant(campus.getId());

        UserLoginVO loginVO = UserLoginVO.builder()
                .id(user.getId())
                .openid(user.getOpenid())
                .token(token)
                .campusId(campus.getId())
                .campusCode(campus.getCode())
                .campusName(campus.getName())
                .servicePhone(campus.getServicePhone())
                .campusStatus(campus.getStatus())
                .estimatedDeliveryMinutes(campus.getEstimatedDeliveryMinutes())
                .merchantId(defaultMerchant == null ? null : defaultMerchant.getId())
                .shopId(defaultMerchant == null ? null : defaultMerchant.getId())
                .shopName(defaultMerchant == null ? null : defaultMerchant.getName())
                .shopAddress(defaultMerchant == null ? null : defaultMerchant.getAddressDetail())
                .phone(defaultMerchant == null ? null : defaultMerchant.getContactPhone())
                .shopStatus(defaultMerchant == null ? null : defaultMerchant.getStatus())
                .businessStatus(defaultMerchant == null ? null : defaultMerchant.getBusinessStatus())
                .logo(defaultMerchant == null ? null : defaultMerchant.getLogo())
                .coverImage(defaultMerchant == null ? null : defaultMerchant.getCoverImage())
                .announcement(defaultMerchant == null ? null : defaultMerchant.getAnnouncement())
                .description(defaultMerchant == null ? null : defaultMerchant.getDescription())
                .deliveryFee(campus.getDeliveryFee())
                .build();
        return Result.success(loginVO);
    }
}
