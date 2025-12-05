package com.dingzk.codeless.controller;

import com.dingzk.codeless.annotation.CheckPermission;
import com.dingzk.codeless.common.BaseResponse;
import com.dingzk.codeless.common.DeleteRequest;
import com.dingzk.codeless.common.RespUtils;
import com.dingzk.codeless.constant.UserConstant;
import com.dingzk.codeless.exception.BusinessException;
import com.dingzk.codeless.exception.ErrorCode;
import com.dingzk.codeless.model.dto.user.*;
import com.dingzk.codeless.model.entity.User;
import com.dingzk.codeless.model.vo.LoginUserVo;
import com.dingzk.codeless.model.vo.AdminUserVo;
import com.dingzk.codeless.service.UserService;
import com.mybatisflex.core.paginate.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 用户控制器
 *
 * @author ding
 * @date 2025/12/4 15:24
 */
@RestController
@RequestMapping("/user")
@Tag(name = "user", description = "用户模块相关接口")
public class UserController {

    @Autowired
    private UserService userService;

    @PostMapping("/register")
    @Operation(summary = "用户注册")
    BaseResponse<Long> userRegister(@RequestBody UserRegisterRequest userRegisterRequest) {
        if (userRegisterRequest == null) {
            throw new BusinessException(ErrorCode.BAD_PARAM_ERROR);
        }
        String userAccount = userRegisterRequest.getUserAccount();
        String password = userRegisterRequest.getPassword();
        String checkedPassword = userRegisterRequest.getCheckedPassword();
        long userId = userService.userRegister(userAccount, password, checkedPassword);
        return RespUtils.success(userId);
    }

    @PostMapping("/login")
    @Operation(summary = "用户登录")
    BaseResponse<LoginUserVo> userLogin(@RequestBody UserLoginRequest userLoginRequest, HttpServletRequest request) {
        if (userLoginRequest == null) {
            throw new BusinessException(ErrorCode.BAD_PARAM_ERROR);
        }
        String userAccount = userLoginRequest.getUserAccount();
        String password = userLoginRequest.getPassword();
        User loginUser = userService.userLogin(userAccount, password, request);
        return RespUtils.success(LoginUserVo.fromUser(loginUser));
    }

    @GetMapping("/current")
    @Operation(summary = "获取当前登录用户信息")
    BaseResponse<LoginUserVo> getCurrentUser(HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        return RespUtils.success(LoginUserVo.fromUser(loginUser));
    }

    @PostMapping("/logout")
    @Operation(summary = "用户登出")
    BaseResponse<Void> userLogout(HttpServletRequest request) {
        userService.userLogout(request);
        return RespUtils.success(null);
    }

    @PostMapping("/add")
    @Operation(summary = "新增用户")
    @CheckPermission(allow = UserConstant.ROLE_ADMIN)
    BaseResponse<Long> addUser(@RequestBody UserAddRequest userAddRequest) {
        if (userAddRequest == null) {
            throw new BusinessException(ErrorCode.BAD_PARAM_ERROR);
        }
        long userId = userService.addUser(userAddRequest);
        return RespUtils.success(userId);
    }

    @PostMapping("/update")
    @Operation(summary = "更新用户")
    BaseResponse<Boolean> updateUser(@RequestBody UserUpdateRequest userUpdateRequest, HttpServletRequest request) {
        if (userUpdateRequest == null) {
            throw new BusinessException(ErrorCode.BAD_PARAM_ERROR);
        }
        boolean result = userService.updateUser(userUpdateRequest, request);
        return RespUtils.success(result);
    }

    @PostMapping("/delete")
    @Operation(summary = "删除用户")
    @CheckPermission(allow = UserConstant.ROLE_ADMIN)
    BaseResponse<Boolean> deleteUser(@RequestBody DeleteRequest deleteRequest) {
        if (deleteRequest == null) {
            throw new BusinessException(ErrorCode.BAD_PARAM_ERROR);
        }
        boolean result = userService.deleteUser(deleteRequest.getId());
        return RespUtils.success(result);
    }

    @PostMapping("/list/page")
    @Operation(summary = "分页查询用户")
    @CheckPermission(allow = UserConstant.ROLE_ADMIN)
    BaseResponse<Page<AdminUserVo>> pageListUser(@RequestBody UserSearchRequest userSearchRequest, HttpServletRequest request) {
        if (userSearchRequest == null) {
            throw new BusinessException(ErrorCode.BAD_PARAM_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        if (loginUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }

        Page<User> userPage = userService.pageListUser(userSearchRequest);
        Page<AdminUserVo> userVoPage = new Page<>(userPage.getPageNumber(), userPage.getPageSize(), userPage.getTotalRow());
        userVoPage.setRecords(userPage.getRecords().stream().map(AdminUserVo::fromUser).toList());
        return RespUtils.success(userVoPage);
    }
}
