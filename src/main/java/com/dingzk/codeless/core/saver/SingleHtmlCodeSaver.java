package com.dingzk.codeless.core.saver;

import com.dingzk.codeless.ai.model.SingleHtmlCodeResult;
import com.dingzk.codeless.exception.BusinessException;
import com.dingzk.codeless.exception.ErrorCode;
import com.dingzk.codeless.model.enums.GenFileTypeEnum;
import org.apache.commons.lang3.StringUtils;

/**
 * 单html代码保存器
 *
 * @author ding
 * @date 2025/12/6 22:15
 */
public class SingleHtmlCodeSaver extends AbstractCodeSaver<SingleHtmlCodeResult> {
    @Override
    protected void saveFiles(String fileBaseDir, SingleHtmlCodeResult codeResult) {
        saveCodeFile(fileBaseDir, "index.html", codeResult.getHtmlCode());
    }

    @Override
    protected void validateBeforeSave(SingleHtmlCodeResult codeResult) {
        super.validateBeforeSave(codeResult);
        if (StringUtils.isBlank(codeResult.getHtmlCode())) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "保存代码为空");
        }
    }

    @Override
    protected GenFileTypeEnum genFileTypeEnum() {
        return GenFileTypeEnum.SINGLE_HTML;
    }
}
