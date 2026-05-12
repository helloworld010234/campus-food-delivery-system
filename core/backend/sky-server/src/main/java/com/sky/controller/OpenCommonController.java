package com.sky.controller;

import com.sky.utils.AliOssUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletResponse;

@RestController
@Api(tags = "开放资源接口")
@Slf4j
@RequiredArgsConstructor
public class OpenCommonController {

    private final AliOssUtil aliOssUtil;

    @GetMapping({"/common/download", "/api/common/download", "/user/common/download"})
    @ApiOperation("公开文件下载")
    public void download(String name, HttpServletResponse response) {
        if (!StringUtils.hasText(name)) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        try {
            response.setContentType(getContentType(name));
            response.setHeader("Cache-Control", "public,max-age=86400");
            aliOssUtil.downloadStream(name, response.getOutputStream());
        } catch (Exception ex) {
            log.error("开放资源下载失败, name={}", name, ex);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    private String getContentType(String name) {
        String lower = name.toLowerCase();
        if (lower.endsWith(".png")) {
            return "image/png";
        }
        if (lower.endsWith(".jpg") || lower.endsWith(".jpeg")) {
            return "image/jpeg";
        }
        if (lower.endsWith(".gif")) {
            return "image/gif";
        }
        if (lower.endsWith(".webp")) {
            return "image/webp";
        }
        return "application/octet-stream";
    }
}
