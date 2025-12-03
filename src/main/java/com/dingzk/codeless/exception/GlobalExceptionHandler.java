package com.dingzk.codeless.exception;

import com.dingzk.codeless.common.BaseResponse;
import com.dingzk.codeless.common.RespUtils;
import io.swagger.v3.oas.annotations.Hidden;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全局异常处理
 *
 * @author dingzk
 * @date 2025/12/3 20:00
 */
@RestControllerAdvice
@Slf4j
@Hidden
public class GlobalExceptionHandler {

    /**
     * 处理业务异常
     * @param e 异常
     * @return 响应
     */
    @ExceptionHandler(exception = {BusinessException.class})
    public BaseResponse<?> handleBusinessException(BusinessException e) {
        log.error("BusinessException: {}", e.getMessage());
        return RespUtils.error(e.getCode(), e.getMessage());
    }

    /**
     * 处理运行时异常
     * @param e 异常
     * @return 响应
     */
    @ExceptionHandler(exception = {RuntimeException.class})
    public BaseResponse<?> handleRuntimeException(RuntimeException e) {
        log.error("RuntimeException: {}", e.getMessage());
        return RespUtils.error(ErrorCode.SYSTEM_ERROR);
    }
}