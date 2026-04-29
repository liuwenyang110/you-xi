package com.nongzhushou.contact.controller;

import com.nongzhushou.common.api.Result;
import com.nongzhushou.contact.service.ContactService;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import java.util.List;
import java.util.Map;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.access.prepost.PreAuthorize;

/**
 * 联系对接控制器（V10 升级为平台核心）
 *
 * 核心流程：
 * 农户浏览设备列表 → 发起联系 → 获取联系方式 → 线下沟通 → 服务完成 → 评价反馈
 */
@PreAuthorize("hasRole('FARMER')")
@RestController
@Validated
@RequestMapping("/api/v1/contact-sessions")
public class ContactController {

    private final ContactService contactService;

    public ContactController(ContactService contactService) {
        this.contactService = contactService;
    }

    /** 获取当前用户的联系记录列表 */
    @GetMapping
    public Result<List<Map<String, Object>>> list() {
        return Result.ok(contactService.list());
    }

    /** 确认联系会话（兼容匹配模式和直接联系模式） */
    @PostMapping("/{id}/confirm")
    public Result<Map<String, Object>> confirm(@PathVariable @Positive(message = "id must be greater than 0") Long id) {
        return Result.ok(contactService.confirm(id));
    }

    /** 拒绝/关闭联系会话 */
    @PostMapping("/{id}/reject")
    public Result<Map<String, Object>> reject(@PathVariable @Positive(message = "id must be greater than 0") Long id) {
        return Result.ok(contactService.reject(id));
    }

    // === V10 新增：联系平台核心接口 ===

    /**
     * 发起联系（农户主动联系农机主）
     * 不经过匹配系统，直接建立联系会话
     */
    @PostMapping("/initiate")
    public Result<Map<String, Object>> initiateContact(
            @RequestParam @Positive Long ownerId,
            @RequestParam(required = false) Long serviceItemId,
            @RequestParam(required = false, defaultValue = "PHONE") String contactType,
            @RequestParam(required = false, defaultValue = "BROWSE") String discoverySource
    ) {
        return Result.ok(contactService.initiateContact(ownerId, serviceItemId, contactType, discoverySource));
    }

    /**
     * 提交服务反馈与评价
     * 农户在服务完成后对农机主进行评价
     */
    @PostMapping("/{id}/feedback")
    public Result<Map<String, Object>> submitFeedback(
            @PathVariable @Positive Long id,
            @RequestParam(required = false) String feedback,
            @RequestParam(required = false) @Min(1) @Max(5) Integer rating
    ) {
        return Result.ok(contactService.submitFeedback(id, feedback, rating));
    }

    /**
     * 标记服务已完成
     */
    @PostMapping("/{id}/complete")
    public Result<Map<String, Object>> markServiceCompleted(
            @PathVariable @Positive Long id
    ) {
        return Result.ok(contactService.markServiceCompleted(id));
    }
}
