package com.dingzk.codeless.core.builder;

import cn.hutool.core.util.RuntimeUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.concurrent.TimeUnit;

/**
 * VueProjectBuilder
 *
 * @author ding
 * @date 2025/12/14 18:18
 */
@Slf4j
@Component
public class VueProjectBuilder {

    /**
     * 构建 Vue 项目
     * @param projectDir 项目目录
     * @return 是否成功
     */
    public boolean buildProject(String projectDir) {
        File workingDir = new File(projectDir);
        // 校验项目目录
        if (!workingDir.exists() || !workingDir.isDirectory()) {
            log.error("项目目录不存在: {}", projectDir);
            return false;
        }
        // 校验 npm install 所需依赖文件: package.json
        File packageJsonFile = new File(workingDir, "package.json");
        if (!packageJsonFile.exists()) {
            log.error("项目缺少 package.json 文件: {}", projectDir);
            return false;
        }
        if (!runNpmInstallCommand(workingDir)) {
            log.error("执行 npm install 失败: {}", projectDir);
            return false;
        }
        if (!runNpmRunBuildCommand(workingDir)) {
            log.error("执行 npm run build 失败: {}", projectDir);
            return false;
        }
        // run build 是否执行成功: 校验 dist 目录
        File distDir = new File(workingDir, "dist");
        if (!distDir.exists() || !distDir.isDirectory()) {
            log.error("项目构建成功, 但项目缺少 dist 目录: {}", projectDir);
            return false;
        }
        log.info("项目构建成功, dist 目录: {}", distDir.getAbsolutePath());
        return true;
    }

    /**
     * buildProject 耗时操作，提供异步执行版本
     * @param projectDir 项目目录
     */
    public void buildProjectAsync(String projectDir) {
        Thread.ofVirtual()
                .name("async_build_project")
                .start(() -> {
                    try {
                        buildProject(projectDir);
                    } catch (Exception e) {
                        log.error("异步构建项目失败: {}", projectDir, e);
                    }
                });
    }

    /**
     * 执行 npm install 命令
     * @param workingDir 工作目录
     * @return 是否成功
     */
    private boolean runNpmInstallCommand(File workingDir) {
        log.info("运行 npm install 命令...");
        return executeCommand(workingDir, String.format("%s install", exactNpmCommand()), 300); // 设置超时时间为 5 分钟
    }

    /**
     * 执行 npm run build 命令
     * @param workingDir 工作目录
     * @return 是否成功
     */
    private boolean runNpmRunBuildCommand(File workingDir) {
        log.info("运行 npm run build 命令...");
        return executeCommand(workingDir, String.format("%s run build", exactNpmCommand()), 180); // 设置超时时间为 3 分钟
    }

    private String exactNpmCommand() {
        boolean isWindows = System.getProperty("os.name").toLowerCase().contains("windows");
        return isWindows ? "npm.cmd" : "npm";
    }

    /**
     * 运行指令
     * @param workingDir 工作目录
     * @param command 指令
     * @param timeoutSeconds 超时时间
     * @return 是否成功
     */
    private boolean executeCommand(File workingDir, String command, int timeoutSeconds) {
        try {
            log.info("在目录 {} 中执行命令: {}", workingDir.getAbsolutePath(), command);
            Process process = RuntimeUtil.exec(
                    null,
                    workingDir,
                    command.split("\\s+") // 命令分割为数组
            );
            // 等待进程完成，设置超时
            boolean finished = process.waitFor(timeoutSeconds, TimeUnit.SECONDS);
            if (!finished) {
                log.error("执行超时，强制终止进程");
                process.destroyForcibly();
                return false;
            }
            int exitCode = process.exitValue();
            if (exitCode == 0) {
                log.info("命令执行成功: {}", command);
                return true;
            } else {
                log.error("命令执行失败，exit code: {}", exitCode);
                return false;
            }
        } catch (Exception e) {
            log.error("执行命令失败: {}, 错误信息: {}", command, e.getMessage());
            return false;
        }
    }
}
