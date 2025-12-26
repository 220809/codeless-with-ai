package com.dingzk.codeless.ratelimit.enums;

/**
 * 限流类型枚举类
 *
 * @author ding
 * @date 2025/12/26 19:54
 */
public enum RateLimitType {
    
    /**
     * 接口级别限流
     */
    API,
    
    /**
     * 用户级别限流
     */
    USER,
    
    /**
     * IP级别限流
     */
    IP
}