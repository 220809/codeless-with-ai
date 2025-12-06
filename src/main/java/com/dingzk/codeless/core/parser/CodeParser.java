package com.dingzk.codeless.core.parser;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 代码解析器
 *
 * @author ding
 * @date 2025/12/6 21:44
 */
public interface CodeParser<T> {
    T parse(String content);

    /**
     * 根据正则模式提取代码
     *
     * @param content 原始内容
     * @param pattern 正则模式
     * @return 提取的代码
     */
    default String extractCodeByPattern(String content, Pattern pattern) {
        Matcher matcher = pattern.matcher(content);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }
}
