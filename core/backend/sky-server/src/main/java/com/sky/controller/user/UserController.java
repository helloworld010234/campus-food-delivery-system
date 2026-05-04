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
import com.sky.service.TokenBlacklistService;
import com.sky.service.UserService;
import com.sky.utils.JwtUtil;
import com.sky.vo.UserLoginVO;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/user/user")
public class UserController {

    private final UserService userService;
    private final JwtProperties jwtProperties;
    private final CampusService campusService;
    private final MerchantService merchantService;
    private final TokenBlacklistService tokenBlacklistService;

    public UserController(UserService userService, JwtProperties jwtProperties, CampusService campusService, MerchantService merchantService, TokenBlacklistService tokenBlacklistService) {
        this.userService = userService;
        this.jwtProperties = jwtProperties;
        this.campusService = campusService;
        this.merchantService = merchantService;
        this.tokenBlacklistService = tokenBlacklistService;
    }

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

    @PostMapping("/logout")
    public Result<String> logout(HttpServletRequest request) {
        String token = request.getHeader(jwtProperties.getUserTokenName());
        if (token != null && !token.isBlank()) {
            LocalDateTime expiresAt = extractExpiration(token, jwtProperties.getUserSecretKey());
            tokenBlacklistService.addToBlacklist(token, "USER", "LOGOUT", expiresAt);
        }
        return Result.success();
    }

    private LocalDateTime extractExpiration(String token, String secretKey) {
        try {
            Date exp = JwtUtil.getExpirationDate(secretKey, token);
            return Instant.ofEpochMilli(exp.getTime()).atZone(ZoneId.systemDefault()).toLocalDateTime();
        } catch (Exception e) {
            return LocalDateTime.now().plusHours(2);
        }
    }
}
