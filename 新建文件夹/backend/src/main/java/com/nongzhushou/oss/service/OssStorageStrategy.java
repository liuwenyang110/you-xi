package com.nongzhushou.oss.service;

import java.io.InputStream;
import java.util.Map;

/**
 * OSS 对象存储策略接口
 *
 * 当前实现：LocalFileStorageService（本地文件系统，开发阶段）
 * 生产切换：阿里云 OSS / 腾讯云 COS / MinIO
 *
 * 只需新建一个 @Service 实现此接口并加 @Primary 即可替换
 */
public interface OssStorageStrategy {

    /**
     * 上传文件
     * @param inputStream 文件流
     * @param originalFilename 原始文件名
     * @param contentType MIME 类型
     * @param fileSize 文件大小（字节）
     * @return { "url": 可访问URL, "key": OSS对象Key }
     */
    Map<String, String> upload(InputStream inputStream, String originalFilename, String contentType, long fileSize);

    /**
     * 删除文件
     * @param key OSS 对象 Key
     */
    void delete(String key);

    /**
     * 批量删除（茶馆关闭时用）
     * @param keys Key 列表
     */
    void batchDelete(java.util.List<String> keys);
}
