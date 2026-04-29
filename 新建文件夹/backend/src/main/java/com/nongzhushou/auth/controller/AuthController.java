package com.nongzhushou.auth.controller;

import com.nongzhushou.auth.dto.RealnameSubmitRequest;
import com.nongzhushou.auth.dto.RoleSwitchRequest;
import com.nongzhushou.auth.dto.SmsLoginRequest;
import com.nongzhushou.auth.dto.SmsSendRequest;
import com.nongzhushou.auth.dto.UiModeSwitchRequest;
import com.nongzhushou.auth.dto.WechatLoginRequest;
import com.nongzhushou.auth.service.AuthService;
import com.nongzhushou.common.api.Result;
import jakarta.validation.Valid;
import java.util.Map;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Validated
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/sms/send")
    public Result<Map<String, Object>> sendSms(@Valid @RequestBody SmsSendRequest request) {
        return Result.ok(authService.sendSmsCode(request.getPhone()));
    }

    @PostMapping("/login/sms")
    public Result<Map<String, Object>> loginBySms(@Valid @RequestBody SmsLoginRequest request) {
        return Result.ok(authService.loginBySms(request));
    }

    @PostMapping("/login/wechat")
    public Result<Map<String, Object>> loginByWechat(@Valid @RequestBody WechatLoginRequest request) {
        return Result.ok(authService.loginByWechat(request));
    }

    @PostMapping("/realname/submit")
    public Result<Map<String, Object>> submitRealname(@Valid @RequestBody RealnameSubmitRequest request) {
        return Result.ok(authService.submitRealname(request));
    }

    @PostMapping("/role/switch")
    public Result<Map<String, Object>> switchRole(@Valid @RequestBody RoleSwitchRequest request) {
        return Result.ok(authService.switchRole(request));
    }

    @PostMapping("/ui-mode/switch")
    public Result<Map<String, Object>> switchUiMode(@Valid @RequestBody UiModeSwitchRequest request) {
        return Result.ok(authService.switchUiMode(request));
    }

    @GetMapping("/me")
    public Result<Map<String, Object>> me() {
        return Result.ok(authService.currentUser());
    }
}
