package com.nongzhushou.teahouse.controller;

import com.nongzhushou.common.exception.BizException;
import com.nongzhushou.common.exception.ErrorCode;
import com.nongzhushou.teahouse.entity.TeahouseMessageEntity;
import com.nongzhushou.teahouse.service.TeahouseService;
import com.nongzhushou.v3user.entity.V3UserEntity;
import com.nongzhushou.v3user.service.V3UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 村级快闪茶馆 API
 */
@RestController
@RequestMapping("/api/v3/teahouse")
public class TeahouseController {

    @Autowired private TeahouseService teahouseService;
    @Autowired private V3UserService v3UserService;

    /** 获取茶馆状态 */
    @GetMapping("/status")
    public ResponseEntity<?> getStatus(@RequestParam Long zoneId) {
        Map<String, Object> result = teahouseService.getTeahouseStatus(zoneId);
        return ok(result);
    }

    /** 获取茶馆消息列表 */
    @GetMapping("/messages")
    public ResponseEntity<?> getMessages(@RequestParam Long zoneId,
                                          @RequestParam(defaultValue = "1") int page,
                                          @RequestParam(defaultValue = "20") int size) {
        Map<String, Object> result = teahouseService.getMessages(zoneId, page, size);
        return ok(result);
    }

    /** 发送消息（文字） */
    @PostMapping("/send")
    public ResponseEntity<?> sendMessage(@RequestBody Map<String, Object> body) {
        V3UserEntity user = v3UserService.getCurrentV3User();
        Long zoneId = toLong(body.get("zoneId"));
        String msgType = str(body.get("msgType"), "TEXT");
        String content = str(body.get("content"), "");
        String mediaKey = str(body.get("mediaKey"), null);

        if (zoneId == null) throw new BizException(ErrorCode.BIZ_ERROR, "缺少片区ID");
        if ("TEXT".equals(msgType) && (content == null || content.trim().isEmpty())) {
            throw new BizException(ErrorCode.BIZ_ERROR, "消息内容不能为空");
        }
        TeahouseMessageEntity msg = teahouseService.sendMessage(zoneId, user.getId(), msgType, content, mediaKey);
        return ok(msg);
    }

    /** 农户申请延期 */
    @PostMapping("/extend")
    public ResponseEntity<?> extend(@RequestBody Map<String, Object> body) {
        V3UserEntity user = v3UserService.getCurrentV3User();
        Long zoneId = toLong(body.get("zoneId"));
        Integer days = toInt(body.get("days"));
        if (zoneId == null || days == null) throw new BizException(ErrorCode.BIZ_ERROR, "参数不完整");
        teahouseService.extendTeahouse(zoneId, user.getId(), days);
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("message", "延期成功，茶馆将继续开放 " + days + " 天");
        return ok(result);
    }

    // ---- 工具 ----
    private ResponseEntity<?> ok(Object data) {
        Map<String, Object> resp = new LinkedHashMap<>();
        resp.put("code", 200);
        resp.put("data", data);
        return ResponseEntity.ok(resp);
    }
    private String str(Object o, String def) { return o == null ? def : o.toString(); }
    private Long toLong(Object o) {
        if (o == null) return null;
        try { return Long.parseLong(o.toString()); } catch (Exception e) { return null; }
    }
    private Integer toInt(Object o) {
        if (o == null) return null;
        try { return Integer.parseInt(o.toString()); } catch (Exception e) { return null; }
    }
}
