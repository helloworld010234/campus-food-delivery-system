package com.sky.utils;

import com.aliyun.oss.ClientException;
import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.OSSException;
import com.aliyun.oss.model.OSSObject;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Data
@AllArgsConstructor
@Slf4j
public class AliOssUtil {

    private String endpoint;
    private String accessKeyId;
    private String accessKeySecret;
    private String bucketName;

    /**
     * 文件上传
     *
     * @param bytes
     * @param objectName
     * @return
     */
    public String upload(byte[] bytes, String objectName) {

        // 创建OSSClient实例。
        OSS ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);

        try {
            // 创建PutObject请求。
            ossClient.putObject(bucketName, objectName, new ByteArrayInputStream(bytes));
        }
        catch (OSSException oe) {
            log.error("OSS upload failed: code={}, message={}, requestId={}, hostId={}",
                    oe.getErrorCode(), oe.getErrorMessage(), oe.getRequestId(), oe.getHostId());
            throw new RuntimeException("文件上传失败: " + oe.getErrorMessage(), oe);
        } catch (ClientException ce) {
            log.error("OSS client error during upload: {}", ce.getMessage(), ce);
            throw new RuntimeException("文件上传失败", ce);
        } finally {
            if (ossClient != null) {
                ossClient.shutdown();
            }
        }

        //文件访问路径规则 https://BucketName.Endpoint/ObjectName
        StringBuilder stringBuilder = new StringBuilder("https://");
        stringBuilder
                .append(bucketName)
                .append(".")
                .append(endpoint)
                .append("/")
                .append(objectName);

        log.info("文件上传到:{}", stringBuilder.toString());

        return stringBuilder.toString();
    }

    public String generatePresignedUrl(String objectNameOrUrl, long expireMinutes) {
        String objectName = extractObjectName(objectNameOrUrl);
        if (objectName == null || objectName.trim().isEmpty()) {
            return null;
        }

        OSS ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);
        try {
            long safeExpireMinutes = expireMinutes > 0 ? expireMinutes : 60L;
            Date expiration = new Date(System.currentTimeMillis() + safeExpireMinutes * 60L * 1000L);
            java.net.URL signedUrl = ossClient.generatePresignedUrl(bucketName, objectName, expiration);
            if (signedUrl == null) {
                return buildHttpsObjectUrl(objectName);
            }
            return ensureHttps(signedUrl.toString());
        } catch (OSSException | ClientException ex) {
            log.error("OSS presigned url generation failed, objectName={}", objectName, ex);
            return buildHttpsObjectUrl(objectName);
        } finally {
            if (ossClient != null) {
                ossClient.shutdown();
            }
        }
    }

    /**
     * 下载对象内容（支持对象名或当前 bucket 的完整 URL）
     *
     * @param objectNameOrUrl OSS 对象名或完整 URL
     * @return 字节数组，失败返回 null
     */
    public byte[] download(String objectNameOrUrl) {
        String objectName = extractObjectName(objectNameOrUrl);
        if (objectName == null || objectName.trim().isEmpty()) {
            return null;
        }

        OSS ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);
        try {
            OSSObject ossObject = ossClient.getObject(bucketName, objectName);
            if (ossObject == null) {
                return null;
            }
            try (InputStream inputStream = ossObject.getObjectContent()) {
                return readBytes(inputStream);
            }
        } catch (OSSException | ClientException | IOException ex) {
            log.error("OSS download failed, objectName={}", objectName, ex);
            return null;
        } finally {
            if (ossClient != null) {
                ossClient.shutdown();
            }
        }
    }

    /**
     * 从完整 URL 中解析当前 bucket 的对象名；若入参本身是对象名则直接返回
     *
     * @param objectNameOrUrl 对象名或完整 URL
     * @return 对象名，无法解析则返回 null
     */
    public String extractObjectName(String objectNameOrUrl) {
        if (objectNameOrUrl == null) {
            return null;
        }

        String value = objectNameOrUrl.trim();
        if (value.isEmpty()) {
            return null;
        }

        if (!value.startsWith("http://") && !value.startsWith("https://")) {
            return value;
        }

        try {
            URI uri = URI.create(value);
            String host = uri.getHost();
            String currentBucketHost = bucketName + "." + endpoint;
            if (host == null || !host.equalsIgnoreCase(currentBucketHost)) {
                return null;
            }
            String path = uri.getPath();
            if (path == null || path.length() <= 1) {
                return null;
            }
            return URLDecoder.decode(path.substring(1), StandardCharsets.UTF_8.name());
        } catch (Exception ex) {
            log.warn("Failed to parse object name from url: {}", objectNameOrUrl, ex);
            return null;
        }
    }

    private byte[] readBytes(InputStream inputStream) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        byte[] buffer = new byte[4096];
        int len;
        while ((len = inputStream.read(buffer)) != -1) {
            outputStream.write(buffer, 0, len);
        }
        return outputStream.toByteArray();
    }

    private String buildHttpsObjectUrl(String objectName) {
        return "https://" + bucketName + "." + endpoint + "/" + objectName;
    }

    private String ensureHttps(String url) {
        if (url == null || url.trim().isEmpty()) {
            return url;
        }
        if (url.startsWith("http://")) {
            return "https://" + url.substring("http://".length());
        }
        return url;
    }
}
