package com.nongzhushou.common.exception;

import com.nongzhushou.common.api.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(BizException.class)
    public Result<Object> handleBizException(BizException e) {
        log.warn("Business exception captured: {} - {}", e.getErrorCode().getCode(), e.getMessage());
        return Result.fail(e.getErrorCode(), e.getMessage());
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Result<Object> handleException(Exception e) {
        log.error("Unhandled system exception", e);
        return Result.fail(ErrorCode.SYSTEM_ERROR, "系统开小差了，请稍后再试");
    }
}
