package com.dingzk.codeless.exception;

/**
 * 异常抛出工具类
 *
 * @author ding
 * @date 2025/12/3 20:04
 */
public class ThrowUtils {
    /**
     * 条件抛出异常
     * @param condition 抛异常调条件
     * @param errorCode 错误码
     */
    public static void throwIf(boolean condition, ErrorCode errorCode) {
        throwIf(condition, errorCode, errorCode.getMessage());
    }

    /**
     * 条件抛出异常
     * @param condition 抛异常调条件
     * @param errorCode 错误码
     * @param message 错误信息
     */
    public static void throwIf(boolean condition, ErrorCode errorCode, String message) {
        if (condition) {
            throw new BusinessException(errorCode, message);
        }
    }
}
