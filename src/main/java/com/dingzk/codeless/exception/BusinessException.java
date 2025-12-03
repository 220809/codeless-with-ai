package com.dingzk.codeless.exception;

import lombok.Getter;

import java.io.Serial;

/**
 * 业务异常类
 *
 * @author ding
 * @date 2025/12/3 20:04
 */
@Getter
public class BusinessException extends RuntimeException{
    @Serial
    private static final long serialVersionUID = 5956768683135396526L;

    /**
     * 错误代码
     */
    private final int code;


    public BusinessException(int code, String message) {
        super(message);
        this.code = code;
    }
    public BusinessException(ErrorCode errorCode) {
        this(errorCode.getCode(), errorCode.getMessage());
    }

    public BusinessException(ErrorCode errorCode, String message) {
        this(errorCode.getCode(), message);
    }
}