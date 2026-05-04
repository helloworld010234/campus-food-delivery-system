package com.sky.handler;

import com.sky.exception.BaseException;
import com.sky.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.sql.SQLIntegrityConstraintViolationException;

/**
 * 全局异常处理器，处理项目中抛出的业务异常
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(BaseException.class)
    public Result exceptionHandler(BaseException ex) {
        log.error("业务异常: {}", ex.getMessage(), ex);
        return Result.error(ex.getMessage());
    }

    @ExceptionHandler(SQLIntegrityConstraintViolationException.class)
    public Result sqlIntegrityConstraintViolationException(SQLIntegrityConstraintViolationException ex) {
        String message = ex.getMessage();
        if (message != null && message.contains("Duplicate entry")) {
            String[] s = message.split(" ");
            if (s.length > 2) {
                String username = s[2];
                String msg = username + "已存在";
                return Result.error(msg);
            }
        }
        return Result.error("该记录已存在，请勿重复添加");
    }

    @ExceptionHandler(DataAccessException.class)
    public Result dataAccessExceptionHandler(DataAccessException ex) {
        log.error("数据库访问异常", ex);
        return Result.error("当前数据库结构与代码版本不匹配，请先完成多商户数据库迁移或切换到单店兼容模式");
    }

    @ExceptionHandler(Exception.class)
    public Result handleException(Exception ex) {
        log.error("系统异常: {}", ex.getMessage(), ex);
        return Result.error("系统繁忙，请稍后再试");
    }
}
