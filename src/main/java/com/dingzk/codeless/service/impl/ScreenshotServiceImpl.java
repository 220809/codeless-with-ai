package com.dingzk.codeless.service.impl;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import com.dingzk.codeless.exception.ErrorCode;
import com.dingzk.codeless.exception.ThrowUtils;
import com.dingzk.codeless.manager.CosManager;
import com.dingzk.codeless.service.ScreenshotService;
import com.dingzk.codeless.utils.ScreenshotUtil;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.stereotype.Service;

import java.io.File;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Service
@Slf4j
public class ScreenshotServiceImpl implements ScreenshotService {

    @Resource
    private CosManager cosManager;

    @Override
    public String doScreenshotAndUpload(String url) {

        // 检查参数
        ThrowUtils.throwIf(StrUtil.isBlank(url), ErrorCode.BAD_PARAM_ERROR, "url不能为空");
        // 截图，获取压缩后的截图
        String screenshotSavePath = ScreenshotUtil.doScreenshotAndSave(url);
        ThrowUtils.throwIf(StrUtil.isBlank(screenshotSavePath), ErrorCode.OPERATION_ERROR, "截图失败, 截图文件路径为空");
        File screenshotFile = new File(screenshotSavePath);
        try {
            // 上传到 COS
            // 检查截图文件
            ThrowUtils.throwIf(!screenshotFile.exists(), ErrorCode.OPERATION_ERROR, "截图文件不存在");

            String cosUrl = cosManager.uploadFile(generateStorageKey(), screenshotFile);
            ThrowUtils.throwIf(StrUtil.isBlank(cosUrl), ErrorCode.OPERATION_ERROR, "截图上传到 COS 失败");
            return cosUrl;
        } finally {
            // 删除本地截图文件
            if (screenshotFile.exists()) {
                File parentFile = screenshotFile.getParentFile();
                FileUtil.del(parentFile);
                log.info("本地截图文件已成功删除");
            }
        }
    }

    private String generateStorageKey() {
        String fileName = RandomStringUtils.insecure().nextAlphanumeric(8) + "_compressed.jpg";
        String dateStr = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
        return String.format("screenshots/%s/%s", dateStr, fileName);
    }
}
