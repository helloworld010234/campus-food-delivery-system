package com.sky.controller.admin;

import com.sky.constant.MessageConstant;
import com.sky.result.Result;
import com.sky.utils.AliOssUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/admin/common")
@Api(tags = "通用接口")
@Slf4j
public class CommonController {

    private static final List<String> ALLOWED_EXTENSIONS = Arrays.asList("jpg", "jpeg", "png", "gif", "webp");

    @Autowired
    private AliOssUtil aliOssUtil;

    /**
     * 文件上传
     */
    @PostMapping("/upload")
    @ApiOperation("文件上传")
    public Result<String> upload(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return Result.error("上传文件不能为空");
        }

        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || !originalFilename.contains(".")) {
            return Result.error("文件名格式不正确");
        }

        String suffix = originalFilename.substring(originalFilename.lastIndexOf(".") + 1).toLowerCase();
        if (!ALLOWED_EXTENSIONS.contains(suffix)) {
            return Result.error("不支持的文件类型，仅允许: " + ALLOWED_EXTENSIONS);
        }

        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            return Result.error("只允许上传图片文件");
        }

        String objectName = UUID.randomUUID().toString() + "." + suffix;

        try {
            aliOssUtil.upload(file.getBytes(), objectName);
            String imageUrl = "/api/common/download?name=" + URLEncoder.encode(objectName, StandardCharsets.UTF_8.name());
            return Result.success(imageUrl);
        } catch (IOException ex) {
            log.error("文件上传失败", ex);
        }
        return Result.error(MessageConstant.UPLOAD_FAILED);
    }

    /**
     * 文件下载（从 OSS 读取）
     * @param name OSS 对象名，或当前 bucket 的完整 URL
     */
    @GetMapping("/download")
    @ApiOperation("文件下载")
    public void download(String name, HttpServletResponse response) {
        if (!StringUtils.hasText(name)) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        try {
            byte[] bytes = aliOssUtil.download(name);
            if (bytes == null || bytes.length == 0) {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                return;
            }

            response.setContentType(getContentType(name));
            response.setHeader("Cache-Control", "public,max-age=86400");
            response.getOutputStream().write(bytes);
            response.getOutputStream().flush();
        } catch (IOException ex) {
            log.error("文件下载失败, name={}", name, ex);
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
