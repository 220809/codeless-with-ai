package com.dingzk.codeless.ai.tools;

import cn.hutool.core.io.FileUtil;
import cn.hutool.json.JSONObject;
import com.dingzk.codeless.constant.AppConstant;
import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import dev.langchain4j.agent.tool.ToolMemoryId;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

/**
 * FileModifyTool - 修改文件内容工具（支持替换指定内容）
 *
 * @author ding
 * @date 2025/12/13
 */
@Slf4j
@Component
public class FileModifyTool extends BaseTool {
    
    /**
     * 修改文件内容，用新内容替换旧内容
     *
     * @param relativePath 文件的相对路径
     * @param oldContent 要替换的旧内容
     * @param newContent 替换后的新内容
     * @param appId 应用ID，用于确定项目目录
     * @return 修改结果信息
     */
    @Tool("modify file content by replacing specified old content with new content at the specified relative path")
    public String modifyFile(@P("the relative path of the file to modify") String relativePath,
                            @P("the old content to be replaced") String oldContent,
                            @P("the new content to replace the old content") String newContent,
                            @ToolMemoryId long appId) {
        try {
            // 安全路径处理
            String safeRelativePath = FileToolUtil.resolveSafePath(relativePath);
            if (safeRelativePath == null) {
                return String.format("文件修改失败: 路径不安全或超出允许范围 - %s", relativePath);
            }
            
            // 构建项目根目录路径
            String projectDirName = "vue_project_" + appId;
            Path projectRoot = Paths.get(AppConstant.CODE_FILE_SAVE_BASE_PATH, projectDirName);
            
            // 解析最终路径
            Path path = projectRoot.resolve(safeRelativePath).normalize();
            
            // 验证解析后的路径是否在项目根目录内（防止路径遍历攻击）
            if (!path.startsWith(projectRoot)) {
                return String.format("文件修改失败: 路径不安全或超出允许范围 - %s", relativePath);
            }
            
            // 检查文件是否存在
            if (!Files.exists(path)) {
                return String.format("文件修改失败: 文件不存在 - %s", relativePath);
            }
            
            // 检查是否为文件（不是目录）
            if (!Files.isRegularFile(path)) {
                return String.format("文件修改失败: 路径指向的不是文件 - %s", relativePath);
            }
            
            // 读取文件内容
            byte[] bytes = Files.readAllBytes(path);
            String fileContent = new String(bytes, StandardCharsets.UTF_8);
            
            // 检查旧内容是否存在
            if (!fileContent.contains(oldContent)) {
                return String.format("文件修改失败: 文件中未找到要替换的内容 - %s", relativePath);
            }
            
            // 执行替换操作
            String modifiedContent = fileContent.replace(oldContent, newContent);
            
            // 检查是否有实际变化（防止替换失败但返回成功）
            if (fileContent.equals(modifiedContent)) {
                return String.format("文件修改失败: 替换后内容未发生变化 - %s", relativePath);
            }
            
            // 写回文件
            Files.writeString(path, modifiedContent,
                    StandardOpenOption.TRUNCATE_EXISTING,
                    StandardOpenOption.WRITE);
            
            log.info("成功修改文件: {}", path.toAbsolutePath());
            return "文件修改成功: " + relativePath;
        } catch (IOException e) {
            String errorMessage = String.format("文件修改失败: %s, 错误: %s", relativePath, e.getMessage());
            log.error(errorMessage, e);
            return errorMessage;
        }
    }

    @Override
    public String getName() {
        return "modifyFile";
    }

    @Override
    public String getDisplayName() {
        return "修改文件";
    }

    @Override
    public String executionResult(JSONObject args) {
        String relativePath = args.getStr("relativePath");
        String fileSuffix = FileUtil.getSuffix(relativePath);
        String oldContent = args.getStr("oldContent");
        String newContent = args.getStr("newContent");
        String executionMessage = String.format(
                """
                [执行结果] %s: %s
                ```%s
                %s
                ```
                修改为:
                ```%s
                %s
                ```
                """
                , getDisplayName(), relativePath, fileSuffix, oldContent, fileSuffix, newContent);
        return String.format("\n\n%s\n\n", executionMessage);
    }
}

