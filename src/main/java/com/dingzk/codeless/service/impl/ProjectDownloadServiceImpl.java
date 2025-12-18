package com.dingzk.codeless.service.impl;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.core.util.ZipUtil;
import com.dingzk.codeless.exception.BusinessException;
import com.dingzk.codeless.exception.ErrorCode;
import com.dingzk.codeless.exception.ThrowUtils;
import com.dingzk.codeless.service.ProjectDownloadService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Set;

/**
 * 项目下载服务实现
 *
 * @author ding
 * @date 2025/12/18 14:18
 */
@Service
@Slf4j
public class ProjectDownloadServiceImpl implements ProjectDownloadService {

    /**
     * 忽略的文件夹
     */
    private static final Set<String> IGNORE_DIRS =  Set.of(
            "node_modules",
            ".git",
            "dist",
            "build",
            ".DS_Store",
            ".env",
            "target",
            ".mvn",
            ".idea",
            ".vscode"
    );

    /**
     * 忽略的文件
     */
    private static final Set<String> IGNORE_FILE_SUFFIXES = Set.of(
            "log",
            "tmp",
            "cache"
    );

    @Override
    public void downloadProjectAsZip(String projectPath, String fileName, HttpServletResponse response) {
        // 基础校验
        ThrowUtils.throwIf(StrUtil.isBlank(projectPath), ErrorCode.BAD_PARAM_ERROR, "项目路径为空");
        ThrowUtils.throwIf(StrUtil.isBlank(fileName), ErrorCode.BAD_PARAM_ERROR, "文件名为空");
        File projectDir = new File(projectPath);
        ThrowUtils.throwIf(!projectDir.exists(), ErrorCode.BAD_PARAM_ERROR, "项目路径不存在");
        ThrowUtils.throwIf(!projectDir.isDirectory(), ErrorCode.BAD_PARAM_ERROR, "项目路径不是一个文件夹");
        // 设置 HTTP 响应头
        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType("application/zip");
        response.addHeader("Content-Disposition",
                String.format("attachment; filename=\"%s.zip\"", fileName));
        // 压缩项目
        try {
            log.info("开始压缩项目: {}", projectPath);
            ZipUtil.zip(response.getOutputStream(), StandardCharsets.UTF_8, false,
                    file -> !isIgnoredFile(projectDir.toPath(), file.toPath()), projectDir);
            log.info("压缩项目成功: {}", fileName);
        } catch (Exception e) {
            log.error("压缩项目失败", e);
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "压缩项目失败");
        }
    }

    private boolean isIgnoredFile(Path rootPath, Path filePath) {
        // 检查文件扩展名
        String suffix = FileUtil.getSuffix(filePath.toFile());
        if (suffix != null && IGNORE_FILE_SUFFIXES.contains(suffix)) {
            return true;
        }
        // 获取文件相对路径
        Path relativePath = rootPath.relativize(filePath);
        // 检查文件是否在忽略的文件夹中
        for (Path path : relativePath) {
            if (IGNORE_DIRS.contains(path.toString())) {
                return true;
            }
        }
        return false;
    }
}
