package com.dingzk.codeless.service;

import jakarta.servlet.http.HttpServletResponse;

/**
 * 项目下载服务
 *
 * @author ding
 * @date 2025/12/18 14:16
 */
public interface ProjectDownloadService {

    /**
     * 下载项目为zip文件
     * @param projectPath 项目路径
     * @param fileName 下载文件名
     * @param response 响应
     */
    void downloadProjectAsZip(String projectPath, String fileName, HttpServletResponse response);
}
