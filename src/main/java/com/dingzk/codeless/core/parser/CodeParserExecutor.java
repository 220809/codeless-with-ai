package com.dingzk.codeless.core.parser;

import com.dingzk.codeless.ai.model.MultiFileCodeResult;
import com.dingzk.codeless.ai.model.SingleHtmlCodeResult;
import com.dingzk.codeless.exception.BusinessException;
import com.dingzk.codeless.exception.ErrorCode;
import com.dingzk.codeless.model.enums.GenFileTypeEnum;

/**
 * 代码解析器执行器
 *
 * @author ding
 * @date 2025/12/6 21:58
 */
public class CodeParserExecutor {

    /**
     * 单文件HTML代码解析器
     */
    private static final CodeParser<SingleHtmlCodeResult> SINGLE_HTML_CODE_PARSER = new SingleHtmlCodeParser();
    /**
     * 多文件代码解析器
     */
    private static final CodeParser<MultiFileCodeResult> MULTI_FILE_CODE_PARSER = new MultiFileCodeParser();

    /**
     * 根据生成文件类型执行解析
     * @param content 解析内容
     * @param genFileType 生成文件类型
     * @return 解析结果 CodeResult
     */
    public static Object execute(String content, GenFileTypeEnum genFileType) {
        return switch (genFileType) {
            case SINGLE_HTML -> SINGLE_HTML_CODE_PARSER.parse(content);
            case MULTI_FILE -> MULTI_FILE_CODE_PARSER.parse(content);
            default -> throw new BusinessException(ErrorCode.SYSTEM_ERROR, "代码保存类型错误");
        };
    }
}
