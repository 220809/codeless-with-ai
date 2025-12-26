package com.dingzk.codeless.utils;

import cn.hutool.json.JSONUtil;
import org.springframework.util.DigestUtils;

/**
 * CacheKeyUtils
 *
 * @author ding
 * @date 2025/12/26 16:22
 */
public class CacheKeyUtils {
    /**
     * 生成缓存key
     * @param data data
     * @return cacheKey
     */
    public static String generateCacheKey(Object data) {
        if (data == null) {
            DigestUtils.md5DigestAsHex("null".getBytes());
        }
        String jsonStr = JSONUtil.toJsonStr(data);
        return DigestUtils.md5DigestAsHex(jsonStr.getBytes());
    }
}
