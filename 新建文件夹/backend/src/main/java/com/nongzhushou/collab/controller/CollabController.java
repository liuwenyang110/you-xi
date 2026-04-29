package com.nongzhushou.collab.controller;

import com.nongzhushou.collab.service.CollabService;
import com.nongzhushou.common.api.Result;
import jakarta.validation.constraints.Positive;
import java.util.List;
import java.util.Map;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/api/v1/collab")
public class CollabController {

    private final CollabService collabService;

    public CollabController(CollabService collabService) {
        this.collabService = collabService;
    }

    @GetMapping("/sessions")
    public Result<List<Map<String, Object>>> listSessions() {
        return Result.ok(collabService.listSessions());
    }

    @PostMapping("/sessions")
    public Result<Map<String, Object>> createSession(@RequestBody Map<String, Object> body) {
        Long ownerId = longValue(body.get("ownerId"));
        Long sourcePostId = longValue(body.get("sourcePostId"));
        Long demandId = longValue(body.get("demandId"));
        String discoverySource = stringValue(body.get("discoverySource"));
        String subject = stringValue(body.get("subject"));
        String initialMessage = stringValue(body.get("initialMessage"));
        return Result.ok(collabService.createSession(ownerId, sourcePostId, demandId, discoverySource, subject, initialMessage));
    }

    @GetMapping("/sessions/{id}/messages")
    public Result<List<Map<String, Object>>> listMessages(@PathVariable("id") @Positive Long id) {
        return Result.ok(collabService.listMessages(id));
    }

    @PostMapping("/sessions/{id}/messages")
    public Result<Map<String, Object>> sendMessage(@PathVariable("id") @Positive Long id, @RequestBody Map<String, Object> body) {
        return Result.ok(collabService.sendMessage(id, stringValue(body.get("messageType")), stringValue(body.get("content")), stringValue(body.get("mediaUrl"))));
    }

    @PostMapping("/sessions/{id}/status")
    public Result<Map<String, Object>> updateStatus(@PathVariable("id") @Positive Long id, @RequestBody Map<String, Object> body) {
        return Result.ok(collabService.updateSessionStatus(id, stringValue(body.get("status"))));
    }

    @GetMapping("/dashboard/summary")
    public Result<Map<String, Object>> dashboardSummary() {
        return Result.ok(collabService.dashboardSummary());
    }

    private Long longValue(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Number number) {
            return number.longValue();
        }
        return Long.valueOf(String.valueOf(value));
    }

    private String stringValue(Object value) {
        return value == null ? null : String.valueOf(value);
    }
}
