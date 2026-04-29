package com.nongzhushou.auth.service;

import com.nongzhushou.auth.dto.RealnameSubmitRequest;
import com.nongzhushou.auth.dto.RoleSwitchRequest;
import com.nongzhushou.auth.dto.SmsLoginRequest;
import com.nongzhushou.auth.dto.UiModeSwitchRequest;
import com.nongzhushou.auth.dto.WechatLoginRequest;
import java.util.Map;
import org.springframework.stereotype.Service;

public interface AuthService {

    Map<String, Object> sendSmsCode(String phone);

    Map<String, Object> loginBySms(SmsLoginRequest request);

    Map<String, Object> loginByWechat(WechatLoginRequest request);

    Map<String, Object> submitRealname(RealnameSubmitRequest request);

    Map<String, Object> switchRole(RoleSwitchRequest request);

    Map<String, Object> switchUiMode(UiModeSwitchRequest request);

    Map<String, Object> currentUser();
}
