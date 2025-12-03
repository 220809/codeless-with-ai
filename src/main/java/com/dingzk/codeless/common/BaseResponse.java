package com.dingzk.codeless.common;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 通用响应类
 *
 * @author ding
 * @date 2025/12/3 19:54
 */
@Data
@AllArgsConstructor
public class BaseResponse<T> implements Serializable {
    @Serial
    private static final long serialVersionUID = 3111358233482166974L;

    /**
     * 响应代码
     */
    private int code;
    /**
     * 响应数据
     */
    private T data;
    /**
     * 响应消息
     */
    private String message;
}