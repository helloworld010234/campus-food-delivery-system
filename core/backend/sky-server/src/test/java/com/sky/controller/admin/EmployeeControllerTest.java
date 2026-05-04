package com.sky.controller.admin;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sky.dto.EmployeeDTO;
import com.sky.dto.EmployeeLoginDTO;
import com.sky.dto.EmployeePageQueryDTO;
import com.sky.entity.Employee;
import com.sky.properties.JwtProperties;
import com.sky.result.PageResult;
import com.sky.service.EmployeeService;
import com.sky.service.TokenBlacklistService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(EmployeeController.class)
class EmployeeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private EmployeeService employeeService;

    @MockitoBean
    private JwtProperties jwtProperties;

    @MockitoBean
    private TokenBlacklistService tokenBlacklistService;

    @Test
    void login_shouldReturnEmployeeLoginVOWithToken_whenCredentialsAreValid() throws Exception {
        // Arrange
        EmployeeLoginDTO dto = new EmployeeLoginDTO();
        dto.setUsername("admin");
        dto.setPassword("password123");

        Employee employee = Employee.builder()
                .id(1L)
                .merchantId(10L)
                .accountType(1)
                .username("admin")
                .name("Administrator")
                .build();

        when(employeeService.login(any(EmployeeLoginDTO.class))).thenReturn(employee);
        when(jwtProperties.getAdminSecretKey()).thenReturn("adminSecretKeyForJwtTokenGeneration12345");
        when(jwtProperties.getAdminTtl()).thenReturn(7200000L);

        // Act & Assert
        mockMvc.perform(post("/admin/employee/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.merchantId").value(10))
                .andExpect(jsonPath("$.data.accountType").value(1))
                .andExpect(jsonPath("$.data.userName").value("admin"))
                .andExpect(jsonPath("$.data.name").value("Administrator"))
                .andExpect(jsonPath("$.data.token").isNotEmpty());
    }

    @Test
    void login_shouldReturnErrorResult_whenServiceThrowsBaseException() throws Exception {
        // Arrange
        EmployeeLoginDTO dto = new EmployeeLoginDTO();
        dto.setUsername("wronguser");
        dto.setPassword("wrongpass");

        when(employeeService.login(any(EmployeeLoginDTO.class)))
                .thenThrow(new com.sky.exception.BaseException("Invalid credentials"));

        // Act & Assert
        mockMvc.perform(post("/admin/employee/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.msg").value("Invalid credentials"));
    }

    @Test
    void save_shouldReturnSuccess_whenEmployeeIsSaved() throws Exception {
        // Arrange
        EmployeeDTO dto = new EmployeeDTO();
        dto.setUsername("newemployee");
        dto.setName("New Employee");
        dto.setPhone("13800138000");
        dto.setSex("1");
        dto.setIdNumber("110101199001011234");
        dto.setStatus(1);

        doNothing().when(employeeService).save(any(EmployeeDTO.class));

        // Act & Assert
        mockMvc.perform(post("/admin/employee")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1));
    }

    @Test
    void save_shouldReturnErrorResult_whenServiceThrowsBaseException() throws Exception {
        // Arrange
        EmployeeDTO dto = new EmployeeDTO();
        dto.setUsername("existinguser");
        dto.setName("Existing User");

        doThrow(new com.sky.exception.BaseException("Username already exists"))
                .when(employeeService).save(any(EmployeeDTO.class));

        // Act & Assert
        mockMvc.perform(post("/admin/employee")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.msg").value("Username already exists"));
    }

    @Test
    void page_shouldReturnPageResult_whenQueryIsValid() throws Exception {
        // Arrange
        PageResult pageResult = new PageResult();
        pageResult.setTotal(2L);
        pageResult.setRecords(Collections.singletonList(
                Employee.builder()
                        .id(1L)
                        .username("admin")
                        .name("Administrator")
                        .build()
        ));

        when(employeeService.pageQuery(any(EmployeePageQueryDTO.class))).thenReturn(pageResult);

        // Act & Assert
        mockMvc.perform(get("/admin/employee/page")
                        .param("page", "1")
                        .param("pageSize", "10")
                        .param("name", "admin"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1))
                .andExpect(jsonPath("$.data.total").value(2))
                .andExpect(jsonPath("$.data.records").isArray())
                .andExpect(jsonPath("$.data.records[0].username").value("admin"));
    }

    @Test
    void page_shouldReturnEmptyPageResult_whenNoRecordsMatch() throws Exception {
        // Arrange
        PageResult emptyPageResult = new PageResult();
        emptyPageResult.setTotal(0L);
        emptyPageResult.setRecords(Collections.emptyList());

        when(employeeService.pageQuery(any(EmployeePageQueryDTO.class))).thenReturn(emptyPageResult);

        // Act & Assert
        mockMvc.perform(get("/admin/employee/page")
                        .param("page", "1")
                        .param("pageSize", "10")
                        .param("name", "nonexistent"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1))
                .andExpect(jsonPath("$.data.total").value(0))
                .andExpect(jsonPath("$.data.records").isArray())
                .andExpect(jsonPath("$.data.records").isEmpty());
    }

    @Test
    void logout_shouldAddTokenToBlacklist_andReturnSuccess() throws Exception {
        when(jwtProperties.getAdminTokenName()).thenReturn("token");
        doNothing().when(tokenBlacklistService).addToBlacklist(eq("valid-admin-token"), eq("ADMIN"), eq("LOGOUT"));

        mockMvc.perform(post("/admin/employee/logout")
                        .header("token", "valid-admin-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1));

        verify(tokenBlacklistService).addToBlacklist("valid-admin-token", "ADMIN", "LOGOUT");
    }

    @Test
    void logout_shouldReturnSuccess_whenNoTokenProvided() throws Exception {
        mockMvc.perform(post("/admin/employee/logout"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1));

        verifyNoInteractions(tokenBlacklistService);
    }
}
