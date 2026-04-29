package com.nongzhushou.oss.controller;

import com.nongzhushou.oss.service.OssStorageStrategy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 文件上传 API
 * 前端通过 uni.uploadFile 调用
 */
@RestController
@RequestMapping("/api/v3/upload")
public class UploadController {

    @Autowired
    private OssStorageStrategy ossStorage;

    /**
     * 上传图片
     * @param file MultipartFile
     * @return { code: 200, data: { url, key } }
     */
    @PostMapping("/image")
    public ResponseEntity<?> uploadImage(@RequestParam("file") MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return error("请选择文件");
        }

        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            return error("仅支持图片文件");
        }

        try {
            Map<String, String> result = ossStorage.upload(
                file.getInputStream(),
                file.getOriginalFilename(),
                contentType,
                file.getSize()
            );
            return ok(result);
        } catch (Exception e) {
            return error(e.getMessage());
        }
    }

    /**
     * 上传语音
     */
    @PostMapping("/voice")
    public ResponseEntity<?> uploadVoice(@RequestParam("file") MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return error("请选择文件");
        }

        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("audio/")) {
            return error("仅支持音频文件");
        }

        try {
            Map<String, String> result = ossStorage.upload(
                file.getInputStream(),
                file.getOriginalFilename(),
                contentType,
                file.getSize()
            );
            return ok(result);
        } catch (Exception e) {
            return error(e.getMessage());
        }
    }

    private ResponseEntity<?> ok(Object data) {
        Map<String, Object> resp = new LinkedHashMap<>();
        resp.put("code", 200);
        resp.put("data", data);
        return ResponseEntity.ok(resp);
    }

    private ResponseEntity<?> error(String msg) {
        Map<String, Object> resp = new LinkedHashMap<>();
        resp.put("code", 400);
        resp.put("message", msg);
        return ResponseEntity.badRequest().body(resp);
    }
}
