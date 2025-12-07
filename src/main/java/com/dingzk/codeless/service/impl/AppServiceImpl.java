package com.dingzk.codeless.service.impl;

import com.dingzk.codeless.common.SearchRequest;
import com.dingzk.codeless.exception.ErrorCode;
import com.dingzk.codeless.exception.ThrowUtils;
import com.dingzk.codeless.mapper.AppMapper;
import com.dingzk.codeless.model.dto.app.AppAddRequest;
import com.dingzk.codeless.model.dto.app.AppAdminUpdateRequest;
import com.dingzk.codeless.model.dto.app.AppSearchRequest;
import com.dingzk.codeless.model.dto.app.AppUpdateRequest;
import com.dingzk.codeless.model.entity.App;
import com.dingzk.codeless.model.entity.User;
import com.dingzk.codeless.model.enums.GenFileTypeEnum;
import com.dingzk.codeless.model.vo.AppVo;
import com.dingzk.codeless.model.vo.LoginUserVo;
import com.dingzk.codeless.service.AppService;
import com.dingzk.codeless.service.UserService;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 应用服务实现类
 *
 * @author ding
 * @date 2025/12/7 14:45
 */
@Service
public class AppServiceImpl extends ServiceImpl<AppMapper, App> implements AppService {

    @Resource
    private UserService userService;

    /**
     * 应用名称最大长度
     */
    private static final int APP_NAME_MAX_LENGTH = 50;
    /**
     * 应用名称最小长度
     */
    private static final int APP_NAME_MIN_LENGTH = 2;
    /**
     * 应用封面最大长度
     */
    private static final int APP_COVER_MAX_LENGTH = 500;
    /**
     * 初始prompt最大长度
     */
    private static final int INITIAL_PROMPT_MAX_LENGTH = 10000;

    @Override
    public long addApp(AppAddRequest appAddRequest, User loginUser) {
        // 1. 参数校验
        ThrowUtils.throwIf(loginUser == null, ErrorCode.NOT_FOUND_ERROR, "用户不存在");
        String initialPrompt = appAddRequest.getInitialPrompt();
        ThrowUtils.throwIf(StringUtils.isEmpty(initialPrompt) || initialPrompt.length() > INITIAL_PROMPT_MAX_LENGTH,
                ErrorCode.BAD_PARAM_ERROR, String.format("提示词不能未空且长度不超过%d", INITIAL_PROMPT_MAX_LENGTH));

        // 2. 构造应用实体
        App app = new App();
        app.setInitialPrompt(initialPrompt);
        app.setUserId(loginUser.getId());
        // 初始以提示词前16个字符作为应用名称
        app.setName(StringUtils.substring(initialPrompt, 0, 15));
        // 暂定为多文件类型
        app.setGenFileType(GenFileTypeEnum.MULTI_FILE.getName());

        // 3. 保存应用
        boolean result = this.save(app);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR, "创建应用失败");

        return app.getId();
    }

    @Override
    public boolean updateApp(AppUpdateRequest appUpdateRequest, User loginUser) {
        App oldApp = checkAppPermission(appUpdateRequest.getId(), loginUser, true);
        // 3. 校验参数
        String name = appUpdateRequest.getName();
        ThrowUtils.throwIf(StringUtils.isEmpty(name)
                || name.length() < APP_NAME_MIN_LENGTH
                || name.length() > APP_NAME_MAX_LENGTH, ErrorCode.BAD_PARAM_ERROR, "应用名称不合法");

        // 4. 构造更新实体
        App app = new App();
        app.setId(oldApp.getId());
        app.setName(name);
        app.setEditTime(LocalDateTime.now());

        // 5. 更新应用
        boolean result = this.updateById(app);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR, "更新应用失败");

        return true;
    }

    private App checkAppPermission(Long appId, User loginUser, boolean modify) {
        ThrowUtils.throwIf(loginUser == null, ErrorCode.NOT_FOUND_ERROR, "用户不存在");
        ThrowUtils.throwIf(appId == null || appId <= 0, ErrorCode.BAD_PARAM_ERROR, "应用ID不合法");

        // 1. 校验应用是否存在
        App oldApp = this.getById(appId);
        ThrowUtils.throwIf(oldApp == null, ErrorCode.NOT_FOUND_ERROR, "应用不存在");

        // 2. 校验权限（只能更新自己的应用）
        ThrowUtils.throwIf(modify && !loginUser.getId().equals(oldApp.getUserId()), ErrorCode.NO_AUTH_ERROR);
        return oldApp;
    }

    @Override
    public boolean deleteApp(Long id, User loginUser) {
        checkAppPermission(id, loginUser, true);

        // 3. 删除应用
        boolean result = this.removeById(id);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR, "删除应用失败");

        return true;
    }

    @Override
    public AppVo getAppById(Long id, User loginUser) {
        App app = checkAppPermission(id, loginUser, false);

        // 3. 转换为VO
        User user = userService.getById(app.getUserId());
        return AppVo.fromApp(app, LoginUserVo.fromUser(user));
    }

    @Override
    public Page<AppVo> pageListApps(AppSearchRequest appSearchRequest, User loginUser) {
        ThrowUtils.throwIf(loginUser == null, ErrorCode.NOT_LOGIN_ERROR);
        // 1. 参数校验
        validateSearchRequest(appSearchRequest);
        // 2. 设置分页参数
        int pageNum = appSearchRequest.getPageNum();
        int pageSize = Math.min(appSearchRequest.getPageSize(), 20);
        // 普通用户每页最多20条
        appSearchRequest.setPageSize(pageSize);

        // 3. 执行查询
        Page<App> appPage = this.page(Page.of(pageNum, pageSize),
                mapper.buildQueryWrapper(appSearchRequest));
        if (CollectionUtils.isEmpty(appPage.getRecords())) {
            return new Page<>(Collections.emptyList(), pageNum, pageSize, 0);
        }
        Set<Long> userIdSet = appPage.getRecords().stream().map(App::getUserId).collect(Collectors.toSet());
        List<User> userList = userService.listByIds(userIdSet);
        Map<Long, LoginUserVo> idUserMap = userList.stream().collect(Collectors.toMap(User::getId, LoginUserVo::fromUser));

        // 4. 转换为VO
        Page<AppVo> appVoPage = new Page<>(appPage.getPageNumber(), appPage.getPageSize(), appPage.getTotalRow());
        List<AppVo> appVoList = appPage.getRecords().stream().map(app -> AppVo.fromApp(app, idUserMap.get(app.getUserId()))).toList();
        appVoPage.setRecords(appVoList);

        return appVoPage;
    }

    // region 管理员功能

    @Override
    public boolean adminDeleteApp(Long id) {
        ThrowUtils.throwIf(id == null || id <= 0, ErrorCode.BAD_PARAM_ERROR, "应用ID不合法");

        // 1. 校验应用是否存在
        App app = this.getById(id);
        ThrowUtils.throwIf(app == null, ErrorCode.NOT_FOUND_ERROR, "应用不存在");

        // 2. 删除应用
        boolean result = this.removeById(id);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR, "删除应用失败");

        return true;
    }

    @Override
    public boolean adminUpdateApp(AppAdminUpdateRequest appAdminUpdateRequest) {
        Long appId = appAdminUpdateRequest.getId();
        ThrowUtils.throwIf(appId == null || appId <= 0, ErrorCode.BAD_PARAM_ERROR, "应用ID不合法");

        // 1. 校验应用是否存在
        App oldApp = this.getById(appId);
        ThrowUtils.throwIf(oldApp == null, ErrorCode.NOT_FOUND_ERROR, "应用不存在");

        // 2. 参数校验
        // 3. 构造更新实体
        App app = new App();
        app.setId(appId);
        // name 不允许设置未空或空白字符，如设置视为不改 name
        String name = appAdminUpdateRequest.getName();
        ThrowUtils.throwIf(name != null && (name.length() < APP_NAME_MIN_LENGTH || name.length() > APP_NAME_MAX_LENGTH),
                ErrorCode.BAD_PARAM_ERROR,
                String.format("应用名称长度应在%d~%d之间", APP_NAME_MIN_LENGTH, APP_NAME_MAX_LENGTH));
        if (StringUtils.isNotBlank(name)) {
            app.setName(name);
        }
        // priority 不允许设置未负数，如设置视为不改 priority
        if (appAdminUpdateRequest.getPriority() != null && appAdminUpdateRequest.getPriority() >= 0) {
            app.setPriority(appAdminUpdateRequest.getPriority());
        }

        String cover = appAdminUpdateRequest.getCover();
        ThrowUtils.throwIf(cover != null && cover.length() > APP_COVER_MAX_LENGTH,
                ErrorCode.BAD_PARAM_ERROR, "应用封面地址过长");
        app.setCover(cover);
        app.setEditTime(LocalDateTime.now());

        // 4. 更新应用
        boolean result = this.updateById(app);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR, "更新应用失败");

        return true;
    }

    @Override
    public Page<AppVo> adminPageListApps(AppSearchRequest appSearchRequest) {
        // 1. 参数校验
        validateSearchRequest(appSearchRequest);

        // 2. 执行查询
        Integer pageNum = appSearchRequest.getPageNum();
        Integer pageSize = appSearchRequest.getPageSize();

        Page<App> appPage = this.page(Page.of(pageNum, pageSize),
                mapper.buildQueryWrapper(appSearchRequest));
        if (CollectionUtils.isEmpty(appPage.getRecords())) {
            return new Page<>(Collections.emptyList(), pageNum, pageSize, 0);
        }
        Set<Long> userIdSet = appPage.getRecords().stream().map(App::getUserId).collect(Collectors.toSet());
        List<User> userList = userService.listByIds(userIdSet);
        Map<Long, LoginUserVo> idUserMap = userList.stream().collect(Collectors.toMap(User::getId, LoginUserVo::fromUser));

        // 3. 转换为VO
        Page<AppVo> appVoPage = new Page<>(appPage.getPageNumber(), appPage.getPageSize(), appPage.getTotalRow());
        List<AppVo> appVoList = appPage.getRecords().stream().map(app -> AppVo.fromApp(app, idUserMap.get(app.getUserId()))).toList();
        appVoPage.setRecords(appVoList);

        return appVoPage;
    }

    @Override
    public AppVo adminGetAppById(Long id) {
        ThrowUtils.throwIf(id == null || id <= 0, ErrorCode.BAD_PARAM_ERROR, "应用ID不合法");

        // 1. 查询应用
        App app = this.getById(id);
        ThrowUtils.throwIf(app == null, ErrorCode.NOT_FOUND_ERROR, "应用不存在");
        User user = userService.getById(app.getUserId());

        // 2. 转换为VO
        return AppVo.fromApp(app, LoginUserVo.fromUser(user));
    }

    // endregion

    /**
     * 校验搜索请求参数
     * @param appSearchRequest 应用搜索请求
     */
    private void validateSearchRequest(AppSearchRequest appSearchRequest) {
        Integer pageNum = appSearchRequest.getPageNum();
        ThrowUtils.throwIf(pageNum == null || pageNum <= 0, ErrorCode.BAD_PARAM_ERROR, "页码不合法");

        Integer pageSize = appSearchRequest.getPageSize();
        ThrowUtils.throwIf(pageSize == null || pageSize <= 0, ErrorCode.BAD_PARAM_ERROR, "每页条数不合法");

        String sortOrder = appSearchRequest.getSortOrder();
        ThrowUtils.throwIf(SearchRequest.SortOrder.fromString(sortOrder) == null,
                ErrorCode.BAD_PARAM_ERROR, "排序方式不合法");
    }
}
