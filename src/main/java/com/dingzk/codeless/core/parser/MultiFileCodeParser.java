package com.dingzk.codeless.core.parser;

import com.dingzk.codeless.ai.model.MultiFileCodeResult;
import org.apache.commons.lang3.StringUtils;

import java.util.regex.Pattern;

/**
 * 多文件代码解析器
 *
 * @author ding
 * @date 2025/12/6 21:54
 */
public class MultiFileCodeParser implements CodeParser<MultiFileCodeResult> {
    /**
     * html解析 pattern
     */
    private static final Pattern HTML_CODE_PATTERN = Pattern.compile("```html\\s*\\n([\\s\\S]*?)```", Pattern.CASE_INSENSITIVE);
    /**
     * css解析 pattern
     */
    private static final Pattern CSS_CODE_PATTERN = Pattern.compile("```css\\s*\\n([\\s\\S]*?)```", Pattern.CASE_INSENSITIVE);
    /**
     * js解析 pattern
     */
    private static final Pattern JS_CODE_PATTERN = Pattern.compile("```(?:js|javascript)\\s*\\n([\\s\\S]*?)```", Pattern.CASE_INSENSITIVE);
    @Override
    public MultiFileCodeResult parse(String content) {
        MultiFileCodeResult result = new MultiFileCodeResult();
        // 提取 HTML 代码
        String htmlCode = extractCodeByPattern(content, HTML_CODE_PATTERN);
        if (StringUtils.isNotBlank(htmlCode)) {
            result.setHtmlCode(htmlCode.trim());
        }
        // 提取 CSS 代码
        String cssCode = extractCodeByPattern(content, CSS_CODE_PATTERN);
        if (StringUtils.isNotBlank(cssCode)) {
            result.setCssCode(cssCode.trim());
        }
        // 提取 JS 代码
        String jsCode = extractCodeByPattern(content, JS_CODE_PATTERN);
        if (StringUtils.isNotBlank(jsCode)) {
            result.setJsCode(jsCode.trim());
        }

        // 当 ai 生成结果不是按照指定格式导致解析异常时，直接将 ai 返回内容作为 html 代码
        if (StringUtils.isAllBlank(result.getHtmlCode(), result.getCssCode(), result.getJsCode())) {
            result.setHtmlCode(content);
        }
        return result;
    }
}
