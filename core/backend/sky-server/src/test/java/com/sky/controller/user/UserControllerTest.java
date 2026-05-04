package com.sky.controller.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.sky.dto.UserLoginDTO;
import com.sky.entity.Campus;
import com.sky.entity.Merchant;
import com.sky.entity.User;
import com.sky.properties.JwtProperties;
import com.sky.service.CampusService;
import com.sky.service.MerchantService;
import com.sky.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private JwtProperties jwtProperties;

    @MockitoBean
    private CampusService campusService;

    @MockitoBean
    private MerchantService merchantService;

    @Test
    void login_shouldReturnUserLoginVOWithToken_whenCredentialsAreValid() throws Exception {
        // Arrange
        UserLoginDTO dto = new UserLoginDTO();
        dto.setCode("wx_auth_code_123");

        User user = User.builder()
                .id(1L)
                .openid("openid_abc")
                .name("Test User")
                .build();

        Campus campus = Campus.builder()
                .id(10L)
                .code("CAMPUS_01")
                .name("Main Campus")
                .servicePhone("1234567890")
                .status(1)
                .estimatedDeliveryMinutes(30)
                .deliveryFee(new BigDecimal("5.00"))
                .build();

        Merchant merchant = Merchant.builder()
                .id(100L)
                .name("Test Shop")
                .addressDetail("123 Main St")
                .contactPhone("0987654321")
                .status(1)
                .businessStatus(1)
                .logo("logo.png")
                .coverImage("cover.jpg")
                .announcement("Welcome")
                .description("Best food in town")
                .build();

        when(userService.wxLogin(any(UserLoginDTO.class))).thenReturn(user);
        when(jwtProperties.getUserSecretKey()).thenReturn("userSecretKeyForJwtTokenGeneration12345");
        when(jwtProperties.getUserTtl()).thenReturn(7200000L);
        when(campusService.getDefaultCampus()).thenReturn(campus);
        when(merchantService.getFirstEnabledMerchant(campus.getId())).thenReturn(merchant);

        // Act & Assert
        mockMvc.perform(post("/user/user/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.openid").value("openid_abc"))
                .andExpect(jsonPath("$.data.token").isNotEmpty())
                .andExpect(jsonPath("$.data.campusId").value(10))
                .andExpect(jsonPath("$.data.campusName").value("Main Campus"))
                .andExpect(jsonPath("$.data.merchantId").value(100))
                .andExpect(jsonPath("$.data.shopName").value("Test Shop"));
    }

    @Test
    void login_shouldReturnUserLoginVOWithNullMerchant_whenNoMerchantExists() throws Exception {
        // Arrange
        UserLoginDTO dto = new UserLoginDTO();
        dto.setCode("wx_auth_code_456");

        User user = User.builder()
                .id(2L)
                .openid("openid_def")
                .name("Another User")
                .build();

        Campus campus = Campus.builder()
                .id(20L)
                .code("CAMPUS_02")
                .name("Secondary Campus")
                .servicePhone("1112223333")
                .status(1)
                .estimatedDeliveryMinutes(45)
                .deliveryFee(new BigDecimal("3.50"))
                .build();

        when(userService.wxLogin(any(UserLoginDTO.class))).thenReturn(user);
        when(jwtProperties.getUserSecretKey()).thenReturn("userSecretKeyForJwtTokenGeneration12345");
        when(jwtProperties.getUserTtl()).thenReturn(7200000L);
        when(campusService.getDefaultCampus()).thenReturn(campus);
        when(merchantService.getFirstEnabledMerchant(campus.getId())).thenReturn(null);

        // Act & Assert
        MvcResult result = mockMvc.perform(post("/user/user/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1))
                .andExpect(jsonPath("$.data.id").value(2))
                .andExpect(jsonPath("$.data.merchantId").isEmpty())
                .andExpect(jsonPath("$.data.shopName").isEmpty())
                .andReturn();

        String responseBody = result.getResponse().getContentAsString();
        assertNotNull(responseBody);
        assert responseBody.contains("token");
    }

    @Test
    void login_shouldReturnErrorResult_whenServiceThrowsBaseException() throws Exception {
        // Arrange
        UserLoginDTO dto = new UserLoginDTO();
        dto.setCode("invalid_code");

        when(userService.wxLogin(any(UserLoginDTO.class)))
                .thenThrow(new com.sky.exception.BaseException("WeChat login failed"));

        // Act & Assert
        mockMvc.perform(post("/user/user/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.msg").value("WeChat login failed"));
    }

    @Test
    void login_shouldAcceptRequestWithObjectNodeBody() throws Exception {
        // Arrange
        ObjectNode jsonNode = objectMapper.createObjectNode();
        jsonNode.put("code", "wx_auth_code_json");

        User user = User.builder()
                .id(3L)
                .openid("openid_json")
                .build();

        Campus campus = Campus.builder()
                .id(30L)
                .code("CAMPUS_03")
                .name("JSON Campus")
                .servicePhone("4445556666")
                .status(1)
                .estimatedDeliveryMinutes(20)
                .deliveryFee(new BigDecimal("2.00"))
                .build();

        when(userService.wxLogin(any(UserLoginDTO.class))).thenReturn(user);
        when(jwtProperties.getUserSecretKey()).thenReturn("userSecretKeyForJwtTokenGeneration12345");
        when(jwtProperties.getUserTtl()).thenReturn(7200000L);
        when(campusService.getDefaultCampus()).thenReturn(campus);
        when(merchantService.getFirstEnabledMerchant(campus.getId())).thenReturn(null);

        // Act & Assert
        mockMvc.perform(post("/user/user/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(jsonNode)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1))
                .andExpect(jsonPath("$.data.id").value(3));
    }
}
