package com.dingzk.codeless.controller;

import com.dingzk.codeless.annotation.CheckPermission;
import com.dingzk.codeless.common.BaseResponse;
import com.dingzk.codeless.common.DeleteRequest;
import com.dingzk.codeless.common.RespUtils;
import com.dingzk.codeless.constant.AppConstant;
import com.dingzk.codeless.constant.UserConstant;
import com.dingzk.codeless.exception.BusinessException;
import com.dingzk.codeless.exception.ErrorCode;
import com.dingzk.codeless.model.dto.app.AppAddRequest;
import com.dingzk.codeless.model.dto.app.AppAdminUpdateRequest;
import com.dingzk.codeless.model.dto.app.AppSearchRequest;
import com.dingzk.codeless.model.dto.app.AppUpdateRequest;
import com.dingzk.codeless.model.entity.User;
import com.dingzk.codeless.model.vo.AppVo;
import com.dingzk.codeless.service.AppService;
import com.dingzk.codeless.service.UserService;
import com.mybatisflex.core.paginate.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

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
        // 设置为查询自己的应用
        appSearchRequest.setUserId(loginUser.getId());
        Page<AppVo> appVoPage = appService.pageListApps(appSearchRequest, loginUser);
        return RespUtils.success(appVoPage);
    }

    @PostMapping("/list/page/featured")
    @Operation(summary = "分页查询精选的应用列表")
    public BaseResponse<Page<AppVo>> pageListFeaturedApps(@RequestBody AppSearchRequest appSearchRequest, HttpServletRequest request) {
        if (appSearchRequest == null) {
            throw new BusinessException(ErrorCode.BAD_PARAM_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        appSearchRequest.setPriority(AppConstant.FEATURED_APP_PRIORITY);
        Page<AppVo> appVoPage = appService.pageListApps(appSearchRequest, loginUser);
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
