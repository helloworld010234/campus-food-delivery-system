package com.sky.controller.admin;

import com.sky.constant.JwtClaimsConstant;
import com.sky.dto.EmployeeDTO;
import com.sky.dto.EmployeeLoginDTO;
import com.sky.dto.EmployeePageQueryDTO;
import com.sky.entity.Employee;
import com.sky.properties.JwtProperties;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.EmployeeService;
import com.sky.service.TokenBlacklistService;
import com.sky.utils.JwtUtil;
import com.sky.vo.EmployeeLoginVO;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
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
@RequestMapping("/admin/employee")
public class EmployeeController {

    private final EmployeeService employeeService;
    private final JwtProperties jwtProperties;
    private final TokenBlacklistService tokenBlacklistService;

    public EmployeeController(EmployeeService employeeService, JwtProperties jwtProperties, TokenBlacklistService tokenBlacklistService) {
        this.employeeService = employeeService;
        this.jwtProperties = jwtProperties;
        this.tokenBlacklistService = tokenBlacklistService;
    }

    @PostMapping("/login")
    public Result<EmployeeLoginVO> login(@RequestBody EmployeeLoginDTO employeeLoginDTO) {
        Employee employee = employeeService.login(employeeLoginDTO);
        Map<String, Object> claims = new HashMap<>();
        claims.put(JwtClaimsConstant.EMP_ID, employee.getId());
        claims.put(JwtClaimsConstant.MERCHANT_ID, employee.getMerchantId());
        claims.put(JwtClaimsConstant.ACCOUNT_TYPE, employee.getAccountType());
        String token = JwtUtil.createJWT(jwtProperties.getAdminSecretKey(), jwtProperties.getAdminTtl(), claims);

        EmployeeLoginVO loginVO = EmployeeLoginVO.builder()
                .id(employee.getId())
                .merchantId(employee.getMerchantId())
                .accountType(employee.getAccountType())
                .userName(employee.getUsername())
                .name(employee.getName())
                .token(token)
                .build();
        return Result.success(loginVO);
    }

    @PostMapping("/logout")
    public Result<String> logout(HttpServletRequest request) {
        String token = request.getHeader(jwtProperties.getAdminTokenName());
        if (token != null && !token.isBlank()) {
            LocalDateTime expiresAt = extractExpiration(token, jwtProperties.getAdminSecretKey());
            tokenBlacklistService.addToBlacklist(token, "ADMIN", "LOGOUT", expiresAt);
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

    @PostMapping
    public Result<String> save(@RequestBody EmployeeDTO employeeDTO) {
        employeeService.save(employeeDTO);
        return Result.success();
    }

    @GetMapping("/page")
    public Result<PageResult> page(EmployeePageQueryDTO employeePageQueryDTO) {
        return Result.success(employeeService.pageQuery(employeePageQueryDTO));
    }

    @PostMapping("/status/{status}")
    public Result<String> startOrStop(@PathVariable Integer status, Long id) {
        employeeService.startOrStop(status, id);
        return Result.success();
    }

    @GetMapping("/{id}")
    public Result<Employee> getById(@PathVariable Long id) {
        return Result.success(employeeService.getById(id));
    }

    @PutMapping
    public Result<String> updateEmployeeData(@RequestBody EmployeeDTO employeeDTO) {
        employeeService.updateEmployeeData(employeeDTO);
        return Result.success();
    }
}
