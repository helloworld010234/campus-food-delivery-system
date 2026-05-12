package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.AccountTypeConstant;
import com.sky.constant.PasswordConstant;
import com.sky.constant.StatusConstant;
import com.sky.context.BaseContext;
import com.sky.dto.EmployeeDTO;
import com.sky.dto.EmployeeLoginDTO;
import com.sky.dto.EmployeePageQueryDTO;
import com.sky.entity.Employee;
import com.sky.exception.AccountLockedException;
import com.sky.exception.AccountNotFoundException;
import com.sky.exception.BaseException;
import com.sky.exception.PasswordErrorException;
import com.sky.mapper.EmployeeMapper;
import com.sky.result.PageResult;
import com.sky.service.EmployeeService;
import com.sky.support.MultiMerchantSchemaSupport;
import com.sky.utils.MerchantScopeUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EmployeeServiceImpl implements EmployeeService {

    private final EmployeeMapper employeeMapper;

    private final MultiMerchantSchemaSupport schemaSupport;

    @Override
    public Employee login(EmployeeLoginDTO employeeLoginDTO) {
        Employee employee = employeeMapper.getByUsername(employeeLoginDTO.getUsername());
        if (employee == null) {
            throw new AccountNotFoundException("璐﹀彿涓嶅瓨鍦?");
        }

        String password = DigestUtils.md5DigestAsHex(employeeLoginDTO.getPassword().getBytes());
        if (!password.equals(employee.getPassword())) {
            throw new PasswordErrorException("瀵嗙爜閿欒");
        }

        if (StatusConstant.DISABLE.equals(employee.getStatus())) {
            throw new AccountLockedException("璐﹀彿琚攣瀹?");
        }
        normalizeAccountScope(employee);
        return employee;
    }

    @Override
    public void save(EmployeeDTO employeeDTO) {
        // MERCHANT_STAFF cannot manage employees
        if (schemaSupport.supportsEmployeeScope()
                && AccountTypeConstant.MERCHANT_STAFF.equals(BaseContext.getCurrentAccountType())) {
            throw new BaseException("员工账号无权管理员工");
        }

        Employee employee = new Employee();
        BeanUtils.copyProperties(employeeDTO, employee);

        if (!schemaSupport.supportsEmployeeScope()) {
            employee.setMerchantId(null);
            employee.setAccountType(AccountTypeConstant.PLATFORM_ADMIN);
        } else if (MerchantScopeUtils.isMerchantAccount()) {
            employee.setMerchantId(BaseContext.getCurrentMerchantId());
            if (employee.getAccountType() == null || AccountTypeConstant.PLATFORM_ADMIN.equals(employee.getAccountType())) {
                employee.setAccountType(AccountTypeConstant.MERCHANT_STAFF);
            }
        } else {
            if (employee.getAccountType() == null) {
                employee.setAccountType(AccountTypeConstant.PLATFORM_ADMIN);
            }
            if (!AccountTypeConstant.PLATFORM_ADMIN.equals(employee.getAccountType()) && employee.getMerchantId() == null) {
                throw new BaseException("鍟嗘埛璐﹀彿蹇呴』缁戝畾鍟嗘埛");
            }
        }

        if (employee.getStatus() == null) {
            employee.setStatus(StatusConstant.ENABLE);
        }
        employee.setPassword(DigestUtils.md5DigestAsHex(PasswordConstant.DEFAULT_PASSWORD.getBytes()));
        if (schemaSupport.supportsEmployeeScope()) {
            employeeMapper.insert(employee);
        } else {
            employeeMapper.insertLegacy(employee);
        }
    }

    @Override
    public PageResult pageQuery(EmployeePageQueryDTO pageQueryDTO) {
        if (!schemaSupport.supportsEmployeeScope()) {
            pageQueryDTO.setMerchantId(null);
            pageQueryDTO.setAccountType(null);
        } else if (MerchantScopeUtils.isMerchantAccount()) {
            pageQueryDTO.setMerchantId(BaseContext.getCurrentMerchantId());
        }
        PageHelper.startPage(pageQueryDTO.getPage(), pageQueryDTO.getPageSize());
        Page<Employee> page = employeeMapper.pageQuery(pageQueryDTO);
        List<Employee> result = page.getResult();
        if (result != null) {
            result.forEach(this::normalizeAccountScope);
        }
        return new PageResult(page.getTotal(), result);
    }

    @Override
    public void startOrStop(Integer status, Long id) {
        Employee employee = getAccessibleEmployee(id);
        employeeMapper.update(Employee.builder()
                .id(employee.getId())
                .status(status)
                .build());
    }

    @Override
    public Employee getById(Long id) {
        Employee employee = getAccessibleEmployee(id);
        employee.setPassword("******");
        return employee;
    }

    @Override
    public void updateEmployeeData(EmployeeDTO employeeDTO) {
        // MERCHANT_STAFF cannot manage employees
        if (schemaSupport.supportsEmployeeScope()
                && AccountTypeConstant.MERCHANT_STAFF.equals(BaseContext.getCurrentAccountType())) {
            throw new BaseException("员工账号无权管理员工");
        }

        Employee existing = getAccessibleEmployee(employeeDTO.getId());
        Employee toUpdate = new Employee();
        BeanUtils.copyProperties(employeeDTO, toUpdate);
        toUpdate.setId(existing.getId());

        if (!schemaSupport.supportsEmployeeScope()) {
            toUpdate.setMerchantId(null);
            toUpdate.setAccountType(null);
        } else if (MerchantScopeUtils.isMerchantAccount()) {
            toUpdate.setMerchantId(BaseContext.getCurrentMerchantId());
            if (toUpdate.getAccountType() == null || AccountTypeConstant.PLATFORM_ADMIN.equals(toUpdate.getAccountType())) {
                toUpdate.setAccountType(existing.getAccountType());
            }
        } else if (!AccountTypeConstant.PLATFORM_ADMIN.equals(existing.getAccountType()) && toUpdate.getMerchantId() == null) {
            toUpdate.setMerchantId(existing.getMerchantId());
        }

        employeeMapper.update(toUpdate);
    }

    private Employee getAccessibleEmployee(Long id) {
        Employee employee = employeeMapper.getById(id);
        if (employee == null) {
            throw new BaseException("鍛樺伐涓嶅瓨鍦?");
        }
        normalizeAccountScope(employee);
        MerchantScopeUtils.assertAccessible(employee.getMerchantId());
        return employee;
    }

    private void normalizeAccountScope(Employee employee) {
        if (employee == null) {
            return;
        }
        if (!schemaSupport.supportsEmployeeScope()) {
            employee.setMerchantId(null);
            employee.setAccountType(AccountTypeConstant.PLATFORM_ADMIN);
            return;
        }
        if (employee.getAccountType() == null) {
            employee.setAccountType(employee.getMerchantId() == null
                    ? AccountTypeConstant.PLATFORM_ADMIN
                    : AccountTypeConstant.MERCHANT_ADMIN);
        }
        if (AccountTypeConstant.PLATFORM_ADMIN.equals(employee.getAccountType())) {
            employee.setMerchantId(null);
            return;
        }
        if (employee.getMerchantId() == null) {
            throw new BaseException("鍟嗘埛璐﹀彿蹇呴』缁戝畾鍟嗘埛");
        }
    }
}
