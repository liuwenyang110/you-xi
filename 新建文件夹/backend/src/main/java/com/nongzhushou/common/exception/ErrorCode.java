package com.nongzhushou.common.exception;

public enum ErrorCode {
    SUCCESS(0, "成功"),
    INVALID_REQUEST(4000, "请求参数无效"),
    UNAUTHORIZED(4001, "未登录或登录已过期"),
    FORBIDDEN(4003, "无权访问"),
    NOT_FOUND(4004, "资源不存在"),
    INVALID_STATE(4009, "状态不正确"),
    BIZ_ERROR(4010, "业务处理异常"),
    RATE_LIMITED(4029, "操作过于频繁，请稍后再试"),
    SYSTEM_ERROR(5000, "系统内部错误");

    private final int code;
    private final String defaultMessage;

    ErrorCode(int code, String defaultMessage) {
        this.code = code;
        this.defaultMessage = defaultMessage;
    }

    public int getCode() {
        return code;
    }

    public String getDefaultMessage() {
        return defaultMessage;
    }
}
