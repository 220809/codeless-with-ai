package com.dingzk.codeless.service.impl;

import com.dingzk.codeless.ai.AiGenFileTypeRoutingService;
import com.dingzk.codeless.common.SearchRequest;
import com.dingzk.codeless.constant.AppConstant;
import com.dingzk.codeless.core.AiGenCodeFacade;
import com.dingzk.codeless.core.builder.VueProjectBuilder;
import com.dingzk.codeless.core.handler.MessageHandlerExecutor;
import com.dingzk.codeless.exception.BusinessException;
import com.dingzk.codeless.exception.ErrorCode;
import com.dingzk.codeless.exception.ThrowUtils;
import com.dingzk.codeless.mapper.AppMapper;
import com.dingzk.codeless.model.dto.app.AppAddRequest;
import com.dingzk.codeless.model.dto.app.AppAdminUpdateRequest;
import com.dingzk.codeless.model.dto.app.AppSearchRequest;
import com.dingzk.codeless.model.dto.app.AppUpdateRequest;
import com.dingzk.codeless.model.entity.App;
import com.dingzk.codeless.model.entity.User;
import com.dingzk.codeless.model.enums.ChatMessageTypeEnum;
import com.dingzk.codeless.model.enums.GenFileTypeEnum;
import com.dingzk.codeless.model.vo.AppVo;
import com.dingzk.codeless.model.vo.LoginUserVo;
import com.dingzk.codeless.service.AppService;
import com.dingzk.codeless.service.ChatHistoryService;
import com.dingzk.codeless.service.ScreenshotService;
import com.dingzk.codeless.service.UserService;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.io.File;
import java.io.IOException;
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
@Slf4j
public class AppServiceImpl extends ServiceImpl<AppMapper, App> implements AppService {

    @Resource
    private UserService userService;

    @Resource
    private AiGenCodeFacade aiGenCodeFacade;

    @Resource
    private ChatHistoryService chatHistoryService;

    @Resource
    private MessageHandlerExecutor messageHandlerExecutor;

    @Resource
    private VueProjectBuilder vueProjectBuilder;

    @Resource
    private ScreenshotService screenshotService;

    @Resource
    private AiGenFileTypeRoutingService aiGenFileTypeRoutingService;

    @Value("${app.deploy-host:http://localhost}")
    private String deployHost;

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

    private static final int USER_MESSAGE_MAX_LENGTH = 1000;

    @Override
    public String deployApp(Long appId, User loginUser) {
        // 1. 权限校验: 用户、app权限校验
        App app = checkAppPermission(appId, loginUser, true);

        // 2. 检查 deploy_key，没有则新生成
        String deployKey = app.getDeployKey();
        if (StringUtils.isBlank(deployKey)) {
            deployKey = RandomStringUtils.secure().nextAlphanumeric(6);
        }

        // 3. 拷贝应用文件到部署目录
        // 检查源目录
        String sourceDirPath = AppConstant.CODE_FILE_SAVE_BASE_PATH;
        String genFileType = app.getGenFileType();
        String appBaseDirName = StringUtils.join(genFileType, "_", app.getId());
        String appBaseDirPath = StringUtils.join(sourceDirPath, File.separator, appBaseDirName);
        File appBaseDir = new File(appBaseDirPath);
        ThrowUtils.throwIf(!appBaseDir.exists() || !appBaseDir.isDirectory(),
                ErrorCode.SYSTEM_ERROR, "应用文件不存在");

        //  Vue 项目特殊处理：执行构建
        GenFileTypeEnum genFileTypeEnum = GenFileTypeEnum.getByName(genFileType);
        if (genFileTypeEnum == GenFileTypeEnum.VUE_PROJECT) {
            // Vue 项目需要构建
            boolean buildSuccess = vueProjectBuilder.buildProject(appBaseDirPath);
            ThrowUtils.throwIf(!buildSuccess, ErrorCode.SYSTEM_ERROR, "Vue 项目构建失败，请检查代码和依赖");
            // 检查 dist 目录是否存在
            File distDir = new File(appBaseDirPath, "dist");
            ThrowUtils.throwIf(!distDir.exists(), ErrorCode.SYSTEM_ERROR, "Vue 项目构建完成但未生成 dist 目录");
            // 将 dist 目录作为部署源
            appBaseDir = distDir;
            log.info("Vue 项目构建成功，部署 dist 目录至: {}", distDir.getAbsolutePath());
        }

        // 检查应用部署目录（目标目录）
        String deployBaseDirPath = AppConstant.CODE_FILE_DEPLOY_BASE_PATH;
        String appDeployDirPath = StringUtils.join(deployBaseDirPath, File.separator, deployKey);
        File appDeployDir = new File(appDeployDirPath);
        // 拷贝文件
        try {
            FileUtils.copyDirectory(appBaseDir, appDeployDir);
        } catch (IOException e) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "拷贝应用文件失败: " + e.getMessage());
        }

        // 4. 更新 app 部署信息
        App updateApp = new App();
        updateApp.setId(appId);
        updateApp.setDeployKey(deployKey);
        updateApp.setDeployedTime(LocalDateTime.now());
        boolean updateResult = this.updateById(updateApp);
        ThrowUtils.throwIf(!updateResult, ErrorCode.OPERATION_ERROR, "更新应用部署信息失败");

        // 返回应用访问url
        String deployedUrl = String.format("%s/%s", deployHost, deployKey);
        // 异步生成封面
        doAppScreenshotAsync(deployedUrl, appId);
        return deployedUrl;
    }

    private void doAppScreenshotAsync(String url, long appId) {
        Thread.startVirtualThread(() -> {
            String coverUrl = screenshotService.doScreenshotAndUpload(url);
            // 更新 app cover 字段
            App app = new App();
            app.setId(appId);
            app.setCover(coverUrl);
            boolean updateResult = this.updateById(app);
            ThrowUtils.throwIf(!updateResult, ErrorCode.OPERATION_ERROR, "更新应用封面失败");
        });
    }

    @Override
    public Flux<String> genCodeFromChat(Long appId, String userMessage, User loginUser) {
        // 1. 参数校验
        // 校验 app 是否存在、用户是否存在、用户是否有app权限
        App app = checkAppPermission(appId, loginUser, true);
        // 校验 message 是否有内容
        ThrowUtils.throwIf(StringUtils.isBlank(userMessage), ErrorCode.BAD_PARAM_ERROR, "用户提示词不能为空");
        // 校验 message 长度
        ThrowUtils.throwIf(userMessage.length() > USER_MESSAGE_MAX_LENGTH, ErrorCode.BAD_PARAM_ERROR, "用户提示词过长");
        // 校验文件生成类型
        GenFileTypeEnum genFileType = GenFileTypeEnum.getByName(app.getGenFileType());
        ThrowUtils.throwIf(genFileType == null, ErrorCode.SYSTEM_ERROR, "存在非法的文件生成类型");

        // 保存用户消息
        chatHistoryService.addChatHistory(appId, userMessage, ChatMessageTypeEnum.USER.getType(), loginUser);

        // ai 返回完整消息后保存ai消息
        Flux<String> aiMessageFlux = aiGenCodeFacade.streamingGenerateAndSaveCodeFile(userMessage, genFileType, app.getId());
        // 根据生成项目类型，分别处理并保存消息到数据库
        return messageHandlerExecutor.execute(aiMessageFlux, chatHistoryService, appId, loginUser, genFileType);
    }

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

        // ai 智能选择项目生成类型
        GenFileTypeEnum genFileType = aiGenFileTypeRoutingService.routeGenFileType(initialPrompt);
        app.setGenFileType(genFileType.getName());

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

    @Override
    public App checkAppPermission(Long appId, User loginUser, boolean owner) {
        ThrowUtils.throwIf(loginUser == null, ErrorCode.NOT_FOUND_ERROR, "用户不存在");
        ThrowUtils.throwIf(appId == null || appId <= 0, ErrorCode.BAD_PARAM_ERROR, "应用ID不合法");

        // 1. 校验应用是否存在
        App oldApp = this.getById(appId);
        ThrowUtils.throwIf(oldApp == null, ErrorCode.NOT_FOUND_ERROR, "应用不存在");

        // 2. 校验权限（只能更新自己的应用）
        ThrowUtils.throwIf(owner && !loginUser.getId().equals(oldApp.getUserId()), ErrorCode.NO_AUTH_ERROR);
        return oldApp;
    }

    @Override
    public boolean deleteApp(Long id, User loginUser) {
        checkAppPermission(id, loginUser, true);

        // 3. 级联删除聊天历史
        try {
            chatHistoryService.deleteChatHistoryByAppId(id);
        } catch (Exception e) {
            // 删除对话消息异常不要影响应用删除，这里做日志记录
            log.error("删除应用关联的历史消息失败: {}", e.getMessage());
        }

        // 4. 删除应用
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
    public Page<AppVo> pageListMyApps(AppSearchRequest appSearchRequest, User loginUser) {
        ThrowUtils.throwIf(loginUser == null, ErrorCode.NOT_LOGIN_ERROR);
        // 设置为查询自己的应用
        appSearchRequest.setUserId(loginUser.getId());
        return pageListApps(appSearchRequest);
    }

    @Override
    public Page<AppVo> pageListFeaturedApps(AppSearchRequest appSearchRequest) {
        appSearchRequest.setPriority(AppConstant.FEATURED_APP_PRIORITY);
        return pageListApps(appSearchRequest);
    }

    private Page<AppVo> pageListApps(AppSearchRequest appSearchRequest) {
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
