package com.sky.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sky.constant.MessageConstant;
import com.sky.dto.UserLoginDTO;
import com.sky.entity.User;
import com.sky.exception.LoginFailedException;
import com.sky.mapper.UserMapper;
import com.sky.properties.StorefrontProperties;
import com.sky.properties.WeChatProperties;
import com.sky.service.UserService;
import com.sky.utils.HttpClientUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Mark
 * @date 2024/2/15
 */

@Slf4j
@Service
public class UserServiceImpl implements UserService {
    public static final String WX_LOGIN = "https://api.weixin.qq.com/sns/jscode2session";

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private WeChatProperties weChatProperties;

    @Autowired
    private StorefrontProperties storefrontProperties;

    @Autowired
    private ObjectMapper objectMapper;

    @Override
    public User wxLogin(UserLoginDTO userLoginDTO) {
        String openId;
        if (Boolean.TRUE.equals(storefrontProperties.getMockUserLogin())) {
            openId = buildMockOpenId(userLoginDTO.getCode());
        } else {
            openId = getOpenId(userLoginDTO.getCode());
        }

        if (openId == null){
            throw new LoginFailedException(MessageConstant.LOGIN_FAILED);
        }

        User user = userMapper.getByOpenId(openId);

        if (user == null){
            // 新用户
            user = User.builder().openid(openId)
                    .createTime(LocalDateTime.now()).build();
            userMapper.insert(user);
        }
        return user;
    }

    /**
     * 获取微信用户openid
     * @param code
     * @return
     */
    private String getOpenId(String code){
        if (code == null || code.trim().isEmpty()) {
            return null;
        }

        Map<String, String> map = new HashMap<>();
        map.put("appid", weChatProperties.getAppid());
        map.put("secret", weChatProperties.getSecret());
        map.put("js_code", code);
        map.put("grant_type", "authorization_code");

        String json = HttpClientUtil.doGet(WX_LOGIN, map);

        try {
            JsonNode jsonObject = objectMapper.readTree(json);
            String openid = jsonObject.get("openid").asText();
            return openid;
        } catch (Exception e) {
            log.error("获取openid失败", e);
            return null;
        }
    }

    private String buildMockOpenId(String code) {
        if (code == null || code.trim().isEmpty()) {
            return "mock-openid-default";
        }
        return "mock-openid-" + code.trim();
    }
}
