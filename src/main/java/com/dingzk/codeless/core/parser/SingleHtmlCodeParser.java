package com.dingzk.codeless.core.parser;

import com.dingzk.codeless.ai.model.SingleHtmlCodeResult;
import org.apache.commons.lang3.StringUtils;

import java.util.regex.Pattern;

/**
 * 单个html代码解析器
 *
 * @author ding
 * @date 2025/12/6 21:45
 */
public class SingleHtmlCodeParser implements CodeParser<SingleHtmlCodeResult> {

    /**
     * html解析 pattern
     */
    private static final Pattern HTML_CODE_PATTERN = Pattern.compile("```html\\s*\\n([\\s\\S]*?)```", Pattern.CASE_INSENSITIVE);
    @Override
    public SingleHtmlCodeResult parse(String content) {
        SingleHtmlCodeResult result = new SingleHtmlCodeResult();
        // 提取html代码
        String htmlCode = extractCodeByPattern(content, HTML_CODE_PATTERN);
        if (StringUtils.isNotBlank(htmlCode.trim())) {
            result.setHtmlCode(htmlCode.trim());
        } else {
            // 如果没有找到代码块，将整个内容作为html
            result.setHtmlCode(content.trim());
        }
        return result;
    }
}
