package com.sky.utils;

import com.sky.properties.StorefrontProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.net.URI;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Component
@RequiredArgsConstructor
public class StorefrontImageResolver {

    private final AliOssUtil aliOssUtil;

    private final StorefrontProperties storefrontProperties;

    public String resolve(String originImage) {
        if (!StringUtils.hasText(originImage)) {
            return originImage;
        }

        String image = originImage.trim();
        String objectName = extractObjectName(image);
        if (StringUtils.hasText(objectName)) {
            return resolveObjectUrl(objectName);
        }

        if (image.startsWith("http://") || image.startsWith("https://")) {
            return image;
        }

        if (image.startsWith("/")) {
            return normalizeBaseUrl() + image;
        }

        return image;
    }

    private String extractObjectName(String image) {
        String objectName = extractObjectNameFromDownloadUrl(image);
        if (StringUtils.hasText(objectName)) {
            return objectName;
        }

        return aliOssUtil.extractObjectName(image);
    }

    private String resolveObjectUrl(String objectName) {
        Long expireMinutes = storefrontProperties.getImageUrlExpireMinutes() == null
                ? 1440L
                : storefrontProperties.getImageUrlExpireMinutes().longValue();
        String signedUrl = aliOssUtil.generatePresignedUrl(objectName, expireMinutes);
        if (StringUtils.hasText(signedUrl)) {
            return signedUrl;
        }
        return normalizeBaseUrl() + "/common/download?name=" + urlEncode(objectName);
    }

    private String extractObjectNameFromDownloadUrl(String image) {
        if (!StringUtils.hasText(image)) {
            return null;
        }

        try {
            if (image.startsWith("http://") || image.startsWith("https://")) {
                URI uri = URI.create(image);
                return extractObjectNameFromPathAndQuery(uri.getPath(), uri.getQuery());
            }
        } catch (Exception ex) {
            return null;
        }

        int queryIndex = image.indexOf('?');
        String path = queryIndex >= 0 ? image.substring(0, queryIndex) : image;
        String query = queryIndex >= 0 && queryIndex < image.length() - 1
                ? image.substring(queryIndex + 1)
                : "";
        return extractObjectNameFromPathAndQuery(path, query);
    }

    private String extractObjectNameFromPathAndQuery(String path, String query) {
        if (!StringUtils.hasText(path) || !path.endsWith("/common/download") || !StringUtils.hasText(query)) {
            return null;
        }

        String[] segments = query.split("&");
        for (String segment : segments) {
            if (!StringUtils.hasText(segment)) {
                continue;
            }
            int equalsIndex = segment.indexOf('=');
            String key = equalsIndex >= 0 ? segment.substring(0, equalsIndex) : segment;
            if (!"name".equals(key)) {
                continue;
            }
            String value = equalsIndex >= 0 ? segment.substring(equalsIndex + 1) : "";
            return StringUtils.hasText(value) ? urlDecode(value) : null;
        }
        return null;
    }

    private String normalizeBaseUrl() {
        String baseUrl = storefrontProperties.getApiBaseUrl();
        if (!StringUtils.hasText(baseUrl)) {
            return "http://127.0.0.1:8080";
        }
        if (baseUrl.endsWith("/")) {
            return baseUrl.substring(0, baseUrl.length() - 1);
        }
        return baseUrl;
    }

    private String urlEncode(String value) {
        try {
            return URLEncoder.encode(value, StandardCharsets.UTF_8.name());
        } catch (Exception ex) {
            return value;
        }
    }

    private String urlDecode(String value) {
        try {
            return URLDecoder.decode(value, StandardCharsets.UTF_8.name());
        } catch (Exception ex) {
            return value;
        }
    }
}
