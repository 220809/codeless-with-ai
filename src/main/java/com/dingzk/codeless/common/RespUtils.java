package com.dingzk.codeless.common;

import com.dingzk.codeless.exception.ErrorCode;

/**
 * 通用响应工具类
 *
 * @author ding
 * @date 2025/12/3 20:03
 */
public final class RespUtils {

    /**
     * 成功响应
     * @param data 响应数据
     * @return BaseResponse
     * @param <T> T
     */
    public static <T> BaseResponse<T> success(T data) {
        return new BaseResponse<>(200, data, "ok");
    }

    /**
     * 失败响应
     * @param code 错误代码
     * @param message 错误信息
     * @return BaseResponse
     */
    public static BaseResponse<?> error(int code, String message) {
        return new BaseResponse<>(code, null, message);
    }

    /**
     * 失败响应
     * @param errorCode 错误码
     * @return BaseResponse
     */
    public static BaseResponse<?> error(ErrorCode errorCode) {
        return new BaseResponse<>(errorCode.getCode(), null, errorCode.getMessage());
    }

    /**
     * 失败响应
     * @param errorCode 错误码
     * @param message 错误信息
     * @return BaseResponse
     */
    public static BaseResponse<?> error(ErrorCode errorCode, String message) {
        return new BaseResponse<>(errorCode.getCode(), null, message);
    }
}