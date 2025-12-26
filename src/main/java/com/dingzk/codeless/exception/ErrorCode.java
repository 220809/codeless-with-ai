package com.dingzk.codeless.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 错误码枚举类
 *
 * @author ding
 * @date 2025/12/3 20:04
 */
@AllArgsConstructor
@Getter
public enum ErrorCode {
    BAD_PARAM_ERROR(40000, "参数有误"),
    NOT_LOGIN_ERROR(40100, "未登录"),
    NO_AUTH_ERROR(40101, "无权限"),
    FORBIDDEN_ERROR(40300, "禁止访问"),
    NOT_FOUND_ERROR(40400, "资源不存在"),
    TOO_MANY_REQUEST_ERROR(42900, "请求过于频繁"),
    SYSTEM_ERROR(50000, "系统错误"),
    OPERATION_ERROR(50001, "操作失败");

    /**
     * 错误代码
     */
    private final int code;
    /**
     * 错误信息
     */
    private final String message;
}