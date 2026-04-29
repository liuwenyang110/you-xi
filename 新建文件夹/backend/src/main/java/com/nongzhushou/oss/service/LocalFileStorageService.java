package com.nongzhushou.oss.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.file.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * 本地文件系统存储实现（开发阶段 fallback）
 *
 * 文件存储路径:  {uploadDir}/teahouse/{yyyy/MM/dd}/{uuid}.{ext}
 * 对外 URL:      /uploads/teahouse/{yyyy/MM/dd}/{uuid}.{ext}
 *
 * ⚠️ 生产环境务必替换为云 OSS 实现！
 *    创建新的 @Service @Primary 实现 OssStorageStrategy 接口即可
 */
@Service
public class LocalFileStorageService implements OssStorageStrategy {

    private static final Logger log = LoggerFactory.getLogger(LocalFileStorageService.class);

    @Value("${app.upload.dir:./uploads}")
    private String uploadDir;

    @Value("${app.upload.base-url:}")
    private String baseUrl;  // 外部访问基础 URL，为空时用相对路径

    private static final Set<String> ALLOWED_TYPES = Set.of(
        "image/jpeg", "image/png", "image/gif", "image/webp",
        "audio/mp3", "audio/mpeg", "audio/wav", "audio/amr", "audio/ogg"
    );

    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB

    @Override
    public Map<String, String> upload(InputStream inputStream, String originalFilename, String contentType, long fileSize) {
        // 安全校验
        if (!ALLOWED_TYPES.contains(contentType)) {
            throw new RuntimeException("不支持的文件类型：" + contentType);
        }
        if (fileSize > MAX_FILE_SIZE) {
            throw new RuntimeException("文件太大了，最多10MB");
        }

        // 生成存储路径
        String datePath = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
        String ext = getExtension(originalFilename, contentType);
        String fileName = UUID.randomUUID().toString().replace("-", "") + "." + ext;
        String key = "teahouse/" + datePath + "/" + fileName;

        Path filePath = Paths.get(uploadDir, key);
        try {
            Files.createDirectories(filePath.getParent());
            Files.copy(inputStream, filePath, StandardCopyOption.REPLACE_EXISTING);
            log.info("文件已保存: {} ({}bytes)", key, fileSize);
        } catch (IOException e) {
            log.error("文件保存失败", e);
            throw new RuntimeException("文件保存失败: " + e.getMessage());
        }

        // 生成 URL
        String url;
        if (baseUrl != null && !baseUrl.isEmpty()) {
            url = baseUrl + "/uploads/" + key;
        } else {
            url = "/uploads/" + key;
        }

        Map<String, String> result = new LinkedHashMap<>();
        result.put("url", url);
        result.put("key", key);
        return result;
    }

    @Override
    public void delete(String key) {
        if (key == null || key.isEmpty()) return;
        Path filePath = Paths.get(uploadDir, key);
        try {
            Files.deleteIfExists(filePath);
            log.info("文件已删除: {}", key);
        } catch (IOException e) {
            log.warn("文件删除失败: {}", key, e);
        }
    }

    @Override
    public void batchDelete(List<String> keys) {
        if (keys == null || keys.isEmpty()) return;
        int count = 0;
        for (String key : keys) {
            try {
                Path filePath = Paths.get(uploadDir, key);
                if (Files.deleteIfExists(filePath)) count++;
            } catch (IOException e) {
                log.warn("批量删除失败: {}", key, e);
            }
        }
        log.info("批量删除完成: {}/{} 个文件", count, keys.size());
    }

    private String getExtension(String filename, String contentType) {
        if (filename != null && filename.contains(".")) {
            return filename.substring(filename.lastIndexOf('.') + 1).toLowerCase();
        }
        // 根据 MIME 推断
        return switch (contentType) {
            case "image/jpeg" -> "jpg";
            case "image/png" -> "png";
            case "image/gif" -> "gif";
            case "image/webp" -> "webp";
            case "audio/mp3", "audio/mpeg" -> "mp3";
            case "audio/wav" -> "wav";
            case "audio/amr" -> "amr";
            case "audio/ogg" -> "ogg";
            default -> "bin";
        };
    }
}
