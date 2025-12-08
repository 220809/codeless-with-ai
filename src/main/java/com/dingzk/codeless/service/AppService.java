package com.dingzk.codeless.service;

import com.dingzk.codeless.model.dto.app.AppAddRequest;
import com.dingzk.codeless.model.dto.app.AppAdminUpdateRequest;
import com.dingzk.codeless.model.dto.app.AppSearchRequest;
import com.dingzk.codeless.model.dto.app.AppUpdateRequest;
import com.dingzk.codeless.model.entity.App;
import com.dingzk.codeless.model.entity.User;
import com.dingzk.codeless.model.vo.AppVo;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.service.IService;
import reactor.core.publisher.Flux;

/**
 * 应用服务层
 *
 * @author ding
 * @date 2025/12/7 14:45
 */
public interface AppService extends IService<App> {

    /**
     * 部署应用
     * @param appId 应用id
     * @param loginUser 登录用户
     * @return 应用访问url
     */
    String deployApp(Long appId, User loginUser);

    /**
     * 对话生成代码
     * @param appId 应用id
     * @param userMessage 用户消息
     * @param loginUser 登录用户
     * @return 生成代码结果
     */
    Flux<String> genCodeFromChat(Long appId, String userMessage, User loginUser);

    /**
     * 创建应用
     * @param appAddRequest 应用创建请求
     * @param loginUser 登录用户
     * @return 应用id
     */
    long addApp(AppAddRequest appAddRequest, User loginUser);

    /**
     * 更新应用
     * @param appUpdateRequest 应用更新请求
     * @param loginUser 登录用户
     * @return 更新结果
     */
    boolean updateApp(AppUpdateRequest appUpdateRequest, User loginUser);

    /**
     * 删除应用
     * @param id 应用id
     * @param loginUser 登录用户
     * @return 删除结果
     */
    boolean deleteApp(Long id, User loginUser);

    /**
     * 根据 id 查看应用详情
     * @param id 应用id
     * @param loginUser 登录用户
     * @return 应用详情
     */
    AppVo getAppById(Long id, User loginUser);

    /**
     * 分页查询应用列表
     * @param appSearchRequest 应用搜索请求
     * @param loginUser 登录用户
     * @return 应用分页列表
     */
    Page<AppVo> pageListApps(AppSearchRequest appSearchRequest, User loginUser);

    // region 管理员功能

    /**
     * 管理员删除应用
     * @param id 应用id
     * @return 删除结果
     */
    boolean adminDeleteApp(Long id);

    /**
     * 管理员更新应用
     * @param appAdminUpdateRequest 应用更新请求
     * @return 更新结果
     */
    boolean adminUpdateApp(AppAdminUpdateRequest appAdminUpdateRequest);

    /**
     * 管理员分页查询应用列表
     * @param appSearchRequest 应用搜索请求
     * @return 应用分页列表
     */
    Page<AppVo> adminPageListApps(AppSearchRequest appSearchRequest);

    /**
     * 管理员根据 id 查看应用详情
     * @param id 应用id
     * @return 应用详情
     */
    AppVo adminGetAppById(Long id);

    // endregion
}
