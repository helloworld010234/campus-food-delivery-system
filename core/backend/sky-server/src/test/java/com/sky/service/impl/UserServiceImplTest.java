package com.sky.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sky.dto.UserLoginDTO;
import com.sky.entity.User;
import com.sky.exception.LoginFailedException;
import com.sky.mapper.UserMapper;
import com.sky.properties.StorefrontProperties;
import com.sky.properties.WeChatProperties;
import com.sky.utils.HttpClientUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserMapper userMapper;

    @Mock
    private WeChatProperties weChatProperties;

    @Mock
    private StorefrontProperties storefrontProperties;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private UserServiceImpl userServiceImpl;

    @Test
    void wxLogin_mockLogin_newUser_shouldInsertAndReturnUser() throws Exception {
        // Arrange
        UserLoginDTO dto = new UserLoginDTO();
        dto.setCode("abc123");

        when(storefrontProperties.getMockUserLogin()).thenReturn(Boolean.TRUE);
        when(userMapper.getByOpenId("mock-openid-abc123")).thenReturn(null);
        doNothing().when(userMapper).insert(any(User.class));

        // Act
        User result = userServiceImpl.wxLogin(dto);

        // Assert
        assertNotNull(result);
        assertEquals("mock-openid-abc123", result.getOpenid());
        assertNotNull(result.getCreateTime());
        verify(userMapper, times(1)).getByOpenId("mock-openid-abc123");
        verify(userMapper, times(1)).insert(any(User.class));
    }

    @Test
    void wxLogin_mockLogin_existingUser_shouldNotInsertAndReturnUser() throws Exception {
        // Arrange
        UserLoginDTO dto = new UserLoginDTO();
        dto.setCode("abc123");

        User existingUser = User.builder()
                .id(1L)
                .openid("mock-openid-abc123")
                .name("Existing User")
                .build();

        when(storefrontProperties.getMockUserLogin()).thenReturn(Boolean.TRUE);
        when(userMapper.getByOpenId("mock-openid-abc123")).thenReturn(existingUser);

        // Act
        User result = userServiceImpl.wxLogin(dto);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("mock-openid-abc123", result.getOpenid());
        assertEquals("Existing User", result.getName());
        verify(userMapper, times(1)).getByOpenId("mock-openid-abc123");
        verify(userMapper, never()).insert(any(User.class));
    }

    @Test
    void wxLogin_realWeChatLogin_existingUser_shouldReturnUser() throws Exception {
        // Arrange
        UserLoginDTO dto = new UserLoginDTO();
        dto.setCode("wx_code_123");

        User existingUser = User.builder()
                .id(2L)
                .openid("real_openid_xyz")
                .name("WeChat User")
                .build();

        when(storefrontProperties.getMockUserLogin()).thenReturn(Boolean.FALSE);
        when(weChatProperties.getAppid()).thenReturn("test_appid");
        when(weChatProperties.getSecret()).thenReturn("test_secret");

        JsonNode jsonNode = mock(JsonNode.class);
        when(jsonNode.get("openid")).thenReturn(jsonNode);
        when(jsonNode.asText()).thenReturn("real_openid_xyz");

        try (MockedStatic<HttpClientUtil> mockedStatic = mockStatic(HttpClientUtil.class)) {
            mockedStatic.when(() -> HttpClientUtil.doGet(anyString(), anyMap()))
                    .thenReturn("{\"openid\":\"real_openid_xyz\"}");

            when(objectMapper.readTree("{\"openid\":\"real_openid_xyz\"}")).thenReturn(jsonNode);
            when(userMapper.getByOpenId("real_openid_xyz")).thenReturn(existingUser);

            // Act
            User result = userServiceImpl.wxLogin(dto);

            // Assert
            assertNotNull(result);
            assertEquals(2L, result.getId());
            assertEquals("real_openid_xyz", result.getOpenid());
            assertEquals("WeChat User", result.getName());
            verify(userMapper, times(1)).getByOpenId("real_openid_xyz");
            verify(userMapper, never()).insert(any(User.class));
        }
    }

    @Test
    void wxLogin_realWeChatLogin_noOpenId_shouldThrowLoginFailedException() throws Exception {
        // Arrange
        UserLoginDTO dto = new UserLoginDTO();
        dto.setCode("wx_code_invalid");

        when(storefrontProperties.getMockUserLogin()).thenReturn(Boolean.FALSE);
        when(weChatProperties.getAppid()).thenReturn("test_appid");
        when(weChatProperties.getSecret()).thenReturn("test_secret");

        JsonNode jsonNode = mock(JsonNode.class);
        when(jsonNode.get("openid")).thenReturn(null);

        try (MockedStatic<HttpClientUtil> mockedStatic = mockStatic(HttpClientUtil.class)) {
            mockedStatic.when(() -> HttpClientUtil.doGet(anyString(), anyMap()))
                    .thenReturn("{\"errcode\":40029,\"errmsg\":\"invalid code\"}");

            when(objectMapper.readTree("{\"errcode\":40029,\"errmsg\":\"invalid code\"}")).thenReturn(jsonNode);

            // Act & Assert
            LoginFailedException exception = assertThrows(LoginFailedException.class, () -> {
                userServiceImpl.wxLogin(dto);
            });
            assertEquals("登录失败", exception.getMessage());
            verify(userMapper, never()).getByOpenId(anyString());
            verify(userMapper, never()).insert(any(User.class));
        }
    }

    @Test
    void wxLogin_realWeChatLogin_nullOpenId_shouldThrowLoginFailedException() throws Exception {
        // Arrange
        UserLoginDTO dto = new UserLoginDTO();
        dto.setCode("wx_code_null");

        when(storefrontProperties.getMockUserLogin()).thenReturn(Boolean.FALSE);
        when(weChatProperties.getAppid()).thenReturn("test_appid");
        when(weChatProperties.getSecret()).thenReturn("test_secret");

        JsonNode jsonNode = mock(JsonNode.class);
        when(jsonNode.get("openid")).thenReturn(null);

        try (MockedStatic<HttpClientUtil> mockedStatic = mockStatic(HttpClientUtil.class)) {
            mockedStatic.when(() -> HttpClientUtil.doGet(anyString(), anyMap()))
                    .thenReturn("{\"openid\":null}");

            when(objectMapper.readTree("{\"openid\":null}")).thenReturn(jsonNode);

            // Act & Assert
            LoginFailedException exception = assertThrows(LoginFailedException.class, () -> {
                userServiceImpl.wxLogin(dto);
            });
            assertEquals("登录失败", exception.getMessage());
            verify(userMapper, never()).getByOpenId(anyString());
            verify(userMapper, never()).insert(any(User.class));
        }
    }

    @Test
    void wxLogin_mockLogin_nullCode_shouldUseDefaultMockOpenId() throws Exception {
        // Arrange
        UserLoginDTO dto = new UserLoginDTO();
        dto.setCode(null);

        when(storefrontProperties.getMockUserLogin()).thenReturn(Boolean.TRUE);
        when(userMapper.getByOpenId("mock-openid-default")).thenReturn(null);
        doNothing().when(userMapper).insert(any(User.class));

        // Act
        User result = userServiceImpl.wxLogin(dto);

        // Assert
        assertNotNull(result);
        assertEquals("mock-openid-default", result.getOpenid());
        verify(userMapper, times(1)).getByOpenId("mock-openid-default");
        verify(userMapper, times(1)).insert(any(User.class));
    }

    @Test
    void wxLogin_mockLogin_emptyCode_shouldUseDefaultMockOpenId() throws Exception {
        // Arrange
        UserLoginDTO dto = new UserLoginDTO();
        dto.setCode("   ");

        when(storefrontProperties.getMockUserLogin()).thenReturn(Boolean.TRUE);
        when(userMapper.getByOpenId("mock-openid-default")).thenReturn(null);
        doNothing().when(userMapper).insert(any(User.class));

        // Act
        User result = userServiceImpl.wxLogin(dto);

        // Assert
        assertNotNull(result);
        assertEquals("mock-openid-default", result.getOpenid());
        verify(userMapper, times(1)).getByOpenId("mock-openid-default");
        verify(userMapper, times(1)).insert(any(User.class));
    }

    @Test
    void wxLogin_realWeChatLogin_newUser_shouldInsertAndReturnUser() throws Exception {
        // Arrange
        UserLoginDTO dto = new UserLoginDTO();
        dto.setCode("wx_code_new");

        when(storefrontProperties.getMockUserLogin()).thenReturn(Boolean.FALSE);
        when(weChatProperties.getAppid()).thenReturn("test_appid");
        when(weChatProperties.getSecret()).thenReturn("test_secret");

        JsonNode jsonNode = mock(JsonNode.class);
        when(jsonNode.get("openid")).thenReturn(jsonNode);
        when(jsonNode.asText()).thenReturn("new_openid_123");

        try (MockedStatic<HttpClientUtil> mockedStatic = mockStatic(HttpClientUtil.class)) {
            mockedStatic.when(() -> HttpClientUtil.doGet(anyString(), anyMap()))
                    .thenReturn("{\"openid\":\"new_openid_123\"}");

            when(objectMapper.readTree("{\"openid\":\"new_openid_123\"}")).thenReturn(jsonNode);
            when(userMapper.getByOpenId("new_openid_123")).thenReturn(null);
            doNothing().when(userMapper).insert(any(User.class));

            // Act
            User result = userServiceImpl.wxLogin(dto);

            // Assert
            assertNotNull(result);
            assertEquals("new_openid_123", result.getOpenid());
            assertNotNull(result.getCreateTime());
            verify(userMapper, times(1)).getByOpenId("new_openid_123");
            verify(userMapper, times(1)).insert(any(User.class));
        }
    }

    @Test
    void wxLogin_realWeChatLogin_emptyCode_shouldReturnNullOpenIdAndThrowException() throws Exception {
        // Arrange
        UserLoginDTO dto = new UserLoginDTO();
        dto.setCode("");

        when(storefrontProperties.getMockUserLogin()).thenReturn(Boolean.FALSE);

        // Act & Assert
        LoginFailedException exception = assertThrows(LoginFailedException.class, () -> {
            userServiceImpl.wxLogin(dto);
        });
        assertEquals("登录失败", exception.getMessage());
        verify(userMapper, never()).getByOpenId(anyString());
        verify(userMapper, never()).insert(any(User.class));
    }
}
