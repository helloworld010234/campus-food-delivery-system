package com.sky.utils;

import com.aliyun.oss.ClientConfiguration;
import com.aliyun.oss.ClientException;
import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.OSSException;
import com.aliyun.oss.model.OSSObject;
import com.sky.exception.OssAuthException;
import com.sky.exception.OssBucketException;
import com.sky.exception.OssNetworkException;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Set;

@Slf4j
public class AliOssUtil {

    private static final Set<String> AUTH_ERROR_CODES = Set.of(
            "InvalidAccessKeyId",
            "SignatureDoesNotMatch",
            "AccessDenied"
    );

    private static final String NO_SUCH_BUCKET = "NoSuchBucket";

    private final String endpoint;
    private final String accessKeyId;
    private final String accessKeySecret;
    private final String bucketName;
    private final int presignedUrlExpireMinutes;

    private final OSS ossClient;

    public AliOssUtil(String endpoint, String accessKeyId, String accessKeySecret,
                      String bucketName, int connectionTimeoutMs, int readTimeoutMs,
                      int maxConnections, int presignedUrlExpireMinutes) {
        this.endpoint = endpoint;
        this.accessKeyId = accessKeyId;
        this.accessKeySecret = accessKeySecret;
        this.bucketName = bucketName;
        this.presignedUrlExpireMinutes = presignedUrlExpireMinutes;

        ClientConfiguration clientConfig = new ClientConfiguration();
        clientConfig.setConnectionTimeout(connectionTimeoutMs);
        clientConfig.setSocketTimeout(readTimeoutMs);
        clientConfig.setMaxConnections(maxConnections);
        clientConfig.setMaxErrorRetry(3);

        this.ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret, clientConfig);
        log.info("OSSClient initialized for bucket={}, endpoint={}", bucketName, endpoint);
    }

    @PreDestroy
    public void shutdown() {
        if (ossClient != null) {
            ossClient.shutdown();
            log.info("OSSClient shutdown");
        }
    }

    /**
     * 上传文件并返回签名访问URL
     */
    public String upload(byte[] bytes, String objectName) {
        try {
            ossClient.putObject(bucketName, objectName, new ByteArrayInputStream(bytes));
        } catch (OSSException oe) {
            log.error("OSS upload failed: code={}, message={}, requestId={}, hostId={}",
                    oe.getErrorCode(), oe.getErrorMessage(), oe.getRequestId(), oe.getHostId());
            throw mapOssException("文件上传失败", oe);
        } catch (ClientException ce) {
            log.error("OSS client error during upload: {}", ce.getMessage(), ce);
            throw new OssNetworkException("文件上传失败", ce);
        }

        String signedUrl = generatePresignedUrl(objectName, presignedUrlExpireMinutes);
        log.info("文件上传成功: objectName={}, signedUrl={}", objectName, signedUrl);
        return signedUrl;
    }

    /**
     * 生成预签名URL
     */
    public String generatePresignedUrl(String objectNameOrUrl, long expireMinutes) {
        String objectName = extractObjectName(objectNameOrUrl);
        if (objectName == null || objectName.trim().isEmpty()) {
            return null;
        }

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
        }
    }

    /**
     * 流式下载到 OutputStream（避免OOM）
     */
    public void downloadStream(String objectNameOrUrl, OutputStream outputStream) {
        String objectName = extractObjectName(objectNameOrUrl);
        if (objectName == null || objectName.trim().isEmpty()) {
            throw new OssNetworkException("对象名不能为空");
        }

        OSSObject ossObject = null;
        try {
            ossObject = ossClient.getObject(bucketName, objectName);
            if (ossObject == null || ossObject.getObjectContent() == null) {
                throw new OssBucketException("对象不存在: " + objectName);
            }
            try (InputStream inputStream = ossObject.getObjectContent()) {
                byte[] buffer = new byte[8192];
                int len;
                while ((len = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, len);
                }
                outputStream.flush();
            }
        } catch (OSSException oe) {
            log.error("OSS download failed: code={}, objectName={}", oe.getErrorCode(), objectName);
            throw mapOssException("文件下载失败", oe);
        } catch (ClientException ce) {
            log.error("OSS client error during download: {}", ce.getMessage(), ce);
            throw new OssNetworkException("文件下载失败", ce);
        } catch (IOException ioe) {
            log.error("IO error during OSS download: objectName={}", objectName, ioe);
            throw new OssNetworkException("文件下载失败", ioe);
        } finally {
            if (ossObject != null) {
                try {
                    ossObject.close();
                } catch (IOException e) {
                    log.warn("Failed to close OSSObject", e);
                }
            }
        }
    }

    /**
     * 下载对象内容到字节数组（小文件场景）
     */
    public byte[] download(String objectNameOrUrl) {
        String objectName = extractObjectName(objectNameOrUrl);
        if (objectName == null || objectName.trim().isEmpty()) {
            return null;
        }

        OSSObject ossObject = null;
        try {
            ossObject = ossClient.getObject(bucketName, objectName);
            if (ossObject == null || ossObject.getObjectContent() == null) {
                return null;
            }
            try (InputStream inputStream = ossObject.getObjectContent()) {
                return readBytes(inputStream);
            }
        } catch (OSSException | ClientException | IOException ex) {
            log.error("OSS download failed, objectName={}", objectName, ex);
            return null;
        } finally {
            if (ossObject != null) {
                try {
                    ossObject.close();
                } catch (IOException e) {
                    log.warn("Failed to close OSSObject", e);
                }
            }
        }
    }

    /**
     * 从完整 URL 中解析当前 bucket 的对象名
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

    /**
     * 校验 bucket 是否可访问
     */
    public boolean validateConnection() {
        try {
            return ossClient.doesBucketExist(bucketName);
        } catch (OSSException | ClientException ex) {
            log.error("OSS connection validation failed", ex);
            return false;
        }
    }

    private RuntimeException mapOssException(String prefix, OSSException oe) {
        String errorCode = oe.getErrorCode();
        String message = prefix + ": " + oe.getErrorMessage();
        if (AUTH_ERROR_CODES.contains(errorCode)) {
            return new OssAuthException(message, oe);
        }
        if (NO_SUCH_BUCKET.equals(errorCode)) {
            return new OssBucketException(message, oe);
        }
        return new OssNetworkException(message, oe);
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
