package com.nongzhushou.notification.controller;

import com.nongzhushou.notification.service.InAppNotificationService;
import com.nongzhushou.v3user.entity.V3UserEntity;
import com.nongzhushou.v3user.service.V3UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 通知中心 API
 */
@RestController
@RequestMapping("/api/v3/notification")
public class NotificationController {

    @Autowired private InAppNotificationService notificationService;
    @Autowired private V3UserService v3UserService;

    /** 获取我的通知列表 */
    @GetMapping("/list")
    public ResponseEntity<?> list(@RequestParam(defaultValue = "1") int page,
                                   @RequestParam(defaultValue = "20") int size) {
        V3UserEntity user = v3UserService.getCurrentV3User();
        Map<String, Object> result = notificationService.getUserNotifications(user.getId(), page, size);
        return ok(result);
    }

    /** 一键全部已读 */
    @PostMapping("/read-all")
    public ResponseEntity<?> readAll() {
        V3UserEntity user = v3UserService.getCurrentV3User();
        notificationService.markAllRead(user.getId());
        return ok("已全部标记为已读");
    }

    /** 单条标记已读 */
    @PostMapping("/read/{id}")
    public ResponseEntity<?> read(@PathVariable Long id) {
        notificationService.markRead(id);
        return ok("已读");
    }

    /** 操作确认（如"活儿干完了"） */
    @PostMapping("/action/{id}")
    public ResponseEntity<?> action(@PathVariable Long id) {
        notificationService.markActionDone(id);
        return ok("操作已确认");
    }

    private ResponseEntity<?> ok(Object data) {
        Map<String, Object> resp = new LinkedHashMap<>();
        resp.put("code", 200);
        resp.put("data", data);
        return ResponseEntity.ok(resp);
    }
}
