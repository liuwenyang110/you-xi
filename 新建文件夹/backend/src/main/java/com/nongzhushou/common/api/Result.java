package com.nongzhushou.common.api;

import com.nongzhushou.common.exception.ErrorCode;

public class Result<T> {

    private Integer code;
    private String message;
    private T data;

    public static <T> Result<T> ok(T data) {
        Result<T> result = new Result<>();
        result.code = ErrorCode.SUCCESS.getCode();
        result.message = ErrorCode.SUCCESS.getDefaultMessage();
        result.data = data;
        return result;
    }

    public static <T> Result<T> fail(String message) {
        return fail(ErrorCode.BIZ_ERROR, message);
    }

    public static <T> Result<T> fail(ErrorCode errorCode) {
        return fail(errorCode, errorCode.getDefaultMessage());
    }

    public static <T> Result<T> fail(ErrorCode errorCode, String message) {
        Result<T> result = new Result<>();
        result.code = errorCode.getCode();
        result.message = message == null || message.isBlank() ? errorCode.getDefaultMessage() : message;
        return result;
    }

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }
}
