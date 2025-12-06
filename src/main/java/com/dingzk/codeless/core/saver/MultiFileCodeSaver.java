package com.dingzk.codeless.core.saver;

import com.dingzk.codeless.ai.model.MultiFileCodeResult;
import com.dingzk.codeless.exception.BusinessException;
import com.dingzk.codeless.exception.ErrorCode;
import com.dingzk.codeless.model.enums.GenFileTypeEnum;
import org.apache.commons.lang3.StringUtils;

public class MultiFileCodeSaver extends AbstractCodeSaver<MultiFileCodeResult>{
    @Override
    protected void saveFiles(String fileBaseDir, MultiFileCodeResult codeResult) {
        saveCodeFile(fileBaseDir, "index.html", codeResult.getHtmlCode());
        saveCodeFile(fileBaseDir, "style.css", codeResult.getCssCode());
        saveCodeFile(fileBaseDir, "script.js", codeResult.getJsCode());
    }

    @Override
    protected void validateBeforeSave(MultiFileCodeResult codeResult) {
        super.validateBeforeSave(codeResult);
        if (StringUtils.isAllBlank(codeResult.getHtmlCode(), codeResult.getCssCode(), codeResult.getJsCode())) {
            throw new BusinessException(ErrorCode.BAD_PARAM_ERROR, "保存代码为空");
        }
    }

    @Override
    protected GenFileTypeEnum genFileTypeEnum() {
        return GenFileTypeEnum.MULTI_FILE;
    }
}
