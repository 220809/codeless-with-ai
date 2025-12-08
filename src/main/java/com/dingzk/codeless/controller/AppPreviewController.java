package com.dingzk.codeless.controller;

import com.dingzk.codeless.constant.AppConstant;
import com.dingzk.codeless.exception.ErrorCode;
import com.dingzk.codeless.exception.ThrowUtils;
import com.dingzk.codeless.model.entity.App;
import com.dingzk.codeless.model.entity.User;
import com.dingzk.codeless.service.AppService;
import com.dingzk.codeless.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.HandlerMapping;

import java.io.File;

/**
 * 应用预览控制器
 *
 * @author ding
 * @date 2025/12/8 15:27
 */
@RestController
@RequestMapping("/app/preview")
@Tag(name = "app_preview", description = "应用预览控制器")
public class AppPreviewController {

    // 应用生成根目录（用于浏览）
    private static final String PREVIEW_ROOT_DIR = AppConstant.CODE_FILE_SAVE_BASE_PATH;

    @Autowired
    private AppService appService;

    @Autowired
    private UserService userService;

    /**
     * 提供静态资源访问，支持目录重定向
     * 访问格式：http://localhost:8888/api/app/preview/{appId}[/{fileName}]
     */
    @GetMapping("/{appId}/**")
    @Operation(summary = "应用预览")
    public ResponseEntity<Resource> appPreview(@PathVariable("appId") Long appId, HttpServletRequest request) {
        // 校验 app 权限
        App app = checkAppPermission(appId, userService.getLoginUser(request));

        try {
            // 获取资源路径
            String resourcePath = (String) request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);
            resourcePath = resourcePath.substring(("/app/preview/" + appId).length());
            // 如果是目录访问（不带斜杠），重定向到带斜杠的URL
            if (resourcePath.isEmpty()) {
                HttpHeaders headers = new HttpHeaders();
                headers.add("Location", request.getRequestURI() + "/");
                return new ResponseEntity<>(headers, HttpStatus.MOVED_PERMANENTLY);
            }
            // 默认返回 index.html
            if (resourcePath.equals("/")) {
                resourcePath = "/index.html";
            }
            // 构建文件路径
            String filePath = StringUtils.join(PREVIEW_ROOT_DIR, File.separator,
                    StringUtils.join(app.getGenFileType(), "_", app.getId()), resourcePath);
            File file = new File(filePath);
            // 检查文件是否存在
            if (!file.exists()) {
                return ResponseEntity.notFound().build();
            }
            // 返回文件资源
            Resource resource = new FileSystemResource(file);
            return ResponseEntity.ok()
                    .header("Content-Type", getContentTypeWithCharset(filePath))
                    .body(resource);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * 获取文件类型
     * @param filePath 文件路径
     * @return 文件类型
     */
    private String getContentTypeWithCharset(String filePath) {
        if (filePath.endsWith(".html")) return "text/html; charset=UTF-8";
        if (filePath.endsWith(".css")) return "text/css; charset=UTF-8";
        if (filePath.endsWith(".js")) return "application/javascript; charset=UTF-8";
        if (filePath.endsWith(".png")) return "image/png";
        if (filePath.endsWith(".jpg")) return "image/jpeg";
        return "application/octet-stream";
    }

    /**
     * 校验应用权限
     * @param appId 应用ID
     * @param loginUser 当前登录用户
     * @return 应用信息
     */
    private App checkAppPermission(Long appId, User loginUser) {
        ThrowUtils.throwIf(loginUser == null, ErrorCode.NOT_FOUND_ERROR, "用户不存在");
        ThrowUtils.throwIf(appId == null || appId <= 0, ErrorCode.BAD_PARAM_ERROR, "应用ID不合法");

        // 1. 校验应用是否存在
        App oldApp = appService.getById(appId);
        ThrowUtils.throwIf(oldApp == null, ErrorCode.NOT_FOUND_ERROR, "应用不存在");

        // 2. 校验权限（只能更新自己的应用）
        ThrowUtils.throwIf(!loginUser.getId().equals(oldApp.getUserId()), ErrorCode.NO_AUTH_ERROR);
        return oldApp;
    }
}