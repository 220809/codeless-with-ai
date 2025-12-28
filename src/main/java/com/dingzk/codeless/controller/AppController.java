package com.dingzk.codeless.controller;

import cn.hutool.json.JSONUtil;
import com.dingzk.codeless.annotation.CheckPermission;
import com.dingzk.codeless.common.BaseResponse;
import com.dingzk.codeless.common.DeleteRequest;
import com.dingzk.codeless.common.RespUtils;
import com.dingzk.codeless.constant.AppConstant;
import com.dingzk.codeless.constant.UserConstant;
import com.dingzk.codeless.exception.BusinessException;
import com.dingzk.codeless.exception.ErrorCode;
import com.dingzk.codeless.exception.ThrowUtils;
import com.dingzk.codeless.model.dto.app.*;
import com.dingzk.codeless.model.entity.App;
import com.dingzk.codeless.model.entity.User;
import com.dingzk.codeless.model.vo.AppVo;
import com.dingzk.codeless.ratelimit.annotation.RateLimit;
import com.dingzk.codeless.ratelimit.enums.RateLimitType;
import com.dingzk.codeless.service.AppService;
import com.dingzk.codeless.service.ProjectDownloadService;
import com.dingzk.codeless.service.UserService;
import com.mybatisflex.core.paginate.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.lang3.StringUtils;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.File;
import java.util.Map;

/**
 * 应用控制器
 *
 * @author ding
 * @date 2025/12/7 14:45
 */
@RestController
@RequestMapping("/app")
@Tag(name = "app", description = "应用模块相关接口")
public class AppController {

    @Resource
    private AppService appService;

    @Resource
    private UserService userService;

    @Resource
    private ProjectDownloadService projectDownloadService;

    @GetMapping("/download/{id}")
    @Operation(summary = "下载应用")
    public BaseResponse<Void> downloadApp(@PathVariable Long id, HttpServletRequest request, HttpServletResponse response) {
        ThrowUtils.throwIf(id == null || id <= 0, ErrorCode.BAD_PARAM_ERROR, "非法的appId");
        User loginUser = userService.getLoginUser(request);
        // 检查用户权限
        App app = appService.checkAppPermission(id, loginUser, true);
        // 获取下载项目目录
        String genFileType = app.getGenFileType();
        String projectRootPath = AppConstant.CODE_FILE_SAVE_BASE_PATH + File.separator + genFileType + "_" + id;
        File projectRoot = new File(projectRootPath);
        ThrowUtils.throwIf(!projectRoot.exists(), ErrorCode.NOT_FOUND_ERROR, "项目不存在");
        String projectFileName = "project_" + id;
        projectDownloadService.downloadProjectAsZip(projectRootPath, projectFileName, response);

        return RespUtils.success(null);
    }

    @PostMapping("/deploy")
    @Operation(summary = "部署应用")
    public BaseResponse<String> deployApp(@RequestBody AppDeployRequest appDeployRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(appDeployRequest == null, ErrorCode.BAD_PARAM_ERROR, "部署请求不能为空");
        User loginUser = userService.getLoginUser(request);
        Long appId = appDeployRequest.getAppId();
        ThrowUtils.throwIf(appId == null || appId <= 0, ErrorCode.BAD_PARAM_ERROR, "非法的appId");
        return RespUtils.success(appService.deployApp(appId, loginUser));
    }

    @GetMapping(value = "/chat/codegen", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @Operation(summary = "对话生成代码")
    @RateLimit(limitType = RateLimitType.USER, rate = 5)
    public Flux<ServerSentEvent<String>> genCodeFromChat(@RequestParam Long appId, @RequestParam String userMessage, HttpServletRequest request) {
        ThrowUtils.throwIf(appId == null || appId <= 0, ErrorCode.BAD_PARAM_ERROR, "非法的appId");
        ThrowUtils.throwIf(StringUtils.isBlank(userMessage), ErrorCode.BAD_PARAM_ERROR, "用户提示词不能为空");
        User loginUser = userService.getLoginUser(request);

        Flux<String> streamingResult = appService.genCodeFromChat(appId, userMessage, loginUser);

        return streamingResult
                .map(chunk -> {
                    Map<String, String> wrapper = Map.of("r", chunk);
                    return ServerSentEvent.<String>builder()
                            .data(JSONUtil.toJsonStr(wrapper))
                            .build();
                })
                .concatWith(Mono.just(
                        ServerSentEvent.<String>builder()
                                .event("done")
                                .data("")
                                .build()
                ));
    }

    @PostMapping("/add")
    @Operation(summary = "创建应用")
    public BaseResponse<Long> addApp(@RequestBody AppAddRequest appAddRequest, HttpServletRequest request) {
        if (appAddRequest == null) {
            throw new BusinessException(ErrorCode.BAD_PARAM_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        long appId = appService.addApp(appAddRequest, loginUser);
        return RespUtils.success(appId);
    }

    @PostMapping("/update")
    @Operation(summary = "更新应用")
    public BaseResponse<Boolean> updateApp(@RequestBody AppUpdateRequest appUpdateRequest, HttpServletRequest request) {
        if (appUpdateRequest == null) {
            throw new BusinessException(ErrorCode.BAD_PARAM_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        boolean result = appService.updateApp(appUpdateRequest, loginUser);
        return RespUtils.success(result);
    }

    @PostMapping("/delete")
    @Operation(summary = "删除应用")
    public BaseResponse<Boolean> deleteApp(@RequestBody DeleteRequest deleteRequest, HttpServletRequest request) {
        if (deleteRequest == null) {
            throw new BusinessException(ErrorCode.BAD_PARAM_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        boolean result = appService.deleteApp(deleteRequest.getId(), loginUser);
        return RespUtils.success(result);
    }

    @GetMapping("/get")
    @Operation(summary = "根据 id 查看应用详情")
    public BaseResponse<AppVo> getAppById(Long id, HttpServletRequest request) {
        if (id == null || id <= 0) {
            throw new BusinessException(ErrorCode.BAD_PARAM_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        AppVo appVo = appService.getAppById(id, loginUser);
        return RespUtils.success(appVo);
    }

    @PostMapping("/list/page/my")
    @Operation(summary = "分页查询自己的应用列表")
    public BaseResponse<Page<AppVo>> pageListMyApps(@RequestBody AppSearchRequest appSearchRequest, HttpServletRequest request) {
        if (appSearchRequest == null) {
            throw new BusinessException(ErrorCode.BAD_PARAM_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        Page<AppVo> appVoPage = appService.pageListMyApps(appSearchRequest, loginUser);
        return RespUtils.success(appVoPage);
    }

    @PostMapping("/list/page/featured")
    @Operation(summary = "分页查询精选的应用列表")
    @Cacheable(value = "featured_app_page"  // 缓存名称
            , key = "T(com.dingzk.codeless.utils.CacheKeyUtils).generateCacheKey(#appSearchRequest)" // 缓存key
            , condition = "#appSearchRequest != null && #appSearchRequest.pageNum <= 10"  // 缓存条件
    )
    public BaseResponse<Page<AppVo>> pageListFeaturedApps(@RequestBody AppSearchRequest appSearchRequest) {
        if (appSearchRequest == null) {
            throw new BusinessException(ErrorCode.BAD_PARAM_ERROR);
        }
        Page<AppVo> appVoPage = appService.pageListFeaturedApps(appSearchRequest);
        return RespUtils.success(appVoPage);
    }

    // region 管理员功能

    @PostMapping("/admin/delete")
    @Operation(summary = "管理员删除应用")
    @CheckPermission(allow = UserConstant.ROLE_ADMIN)
    public BaseResponse<Boolean> adminDeleteApp(@RequestBody DeleteRequest deleteRequest) {
        if (deleteRequest == null) {
            throw new BusinessException(ErrorCode.BAD_PARAM_ERROR);
        }
        boolean result = appService.adminDeleteApp(deleteRequest.getId());
        return RespUtils.success(result);
    }

    @PostMapping("/admin/update")
    @Operation(summary = "管理员更新应用")
    @CheckPermission(allow = UserConstant.ROLE_ADMIN)
    public BaseResponse<Boolean> adminUpdateApp(@RequestBody AppAdminUpdateRequest appAdminUpdateRequest) {
        if (appAdminUpdateRequest == null) {
            throw new BusinessException(ErrorCode.BAD_PARAM_ERROR);
        }
        boolean result = appService.adminUpdateApp(appAdminUpdateRequest);
        return RespUtils.success(result);
    }

    @PostMapping("/admin/list/page")
    @Operation(summary = "管理员分页查询应用列表")
    @CheckPermission(allow = UserConstant.ROLE_ADMIN)
    public BaseResponse<Page<AppVo>> adminPageListApps(@RequestBody AppSearchRequest appSearchRequest) {
        if (appSearchRequest == null) {
            throw new BusinessException(ErrorCode.BAD_PARAM_ERROR);
        }
        Page<AppVo> appVoPage = appService.adminPageListApps(appSearchRequest);
        return RespUtils.success(appVoPage);
    }

    @GetMapping("/admin/get")
    @Operation(summary = "管理员根据 id 查看应用详情")
    @CheckPermission(allow = UserConstant.ROLE_ADMIN)
    public BaseResponse<AppVo> adminGetAppById(Long id) {
        if (id == null || id <= 0) {
            throw new BusinessException(ErrorCode.BAD_PARAM_ERROR);
        }
        AppVo appVo = appService.adminGetAppById(id);
        return RespUtils.success(appVo);
    }

    // endregion
}
