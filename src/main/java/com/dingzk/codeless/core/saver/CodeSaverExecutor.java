package com.dingzk.codeless.core.saver;

import com.dingzk.codeless.ai.model.MultiFileCodeResult;
import com.dingzk.codeless.ai.model.SingleHtmlCodeResult;
import com.dingzk.codeless.exception.BusinessException;
import com.dingzk.codeless.exception.ErrorCode;
import com.dingzk.codeless.model.enums.GenFileTypeEnum;

import java.io.File;

/**
 * 代码保存执行器
 *
 * @author ding
 * @date 2025/12/6 22:17
 */
public class CodeSaverExecutor {
    private static final SingleHtmlCodeSaver SINGLE_HTML_CODE_SAVER = new SingleHtmlCodeSaver();
    private static final MultiFileCodeSaver MULTI_FILE_CODE_SAVER = new MultiFileCodeSaver();
    public static File execute(Object codeResult, GenFileTypeEnum genFileType, Long appId) {
        return switch (genFileType) {
            case SINGLE_HTML -> SINGLE_HTML_CODE_SAVER.save((SingleHtmlCodeResult) codeResult, appId);
            case MULTI_FILE -> MULTI_FILE_CODE_SAVER.save((MultiFileCodeResult) codeResult, appId);
            default -> throw new BusinessException(ErrorCode.SYSTEM_ERROR, "代码保存类型错误");
        };
    }
}
