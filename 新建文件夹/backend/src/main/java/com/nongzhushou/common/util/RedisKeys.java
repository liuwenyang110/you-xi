package com.nongzhushou.common.util;

public final class RedisKeys {

    private RedisKeys() {
    }

    public static String matchDemandLock(Long demandId) {
        return "match:demand:lock:" + demandId;
    }

    public static String userUiMode(Long userId) {
        return "user:ui:mode:" + userId;
    }

    public static String smsCode(String phone) {
        return "auth:sms:" + phone;
    }
}
