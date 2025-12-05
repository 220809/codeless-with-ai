package com.dingzk.codeless.service;

import com.dingzk.codeless.model.dto.user.UserAddRequest;
import com.dingzk.codeless.model.dto.user.UserSearchRequest;
import com.dingzk.codeless.model.dto.user.UserUpdateRequest;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.service.IService;
import com.dingzk.codeless.model.entity.User;
import jakarta.servlet.http.HttpServletRequest;

/**
 * 用户服务层
 *
 * @author ding
 * @date 2025/12/4 15:24
 */
public interface UserService extends IService<User> {
    /**
     * 用户注册
     * @param userAccount 用户账号
     * @param password 密码
     * @param checkedPassword 确认密码
     * @return 用户id
     */
    long userRegister(String userAccount, String password, String checkedPassword);

    /**
     * 用户登录
     * @param userAccount 用户账号
     * @param password 密码
     * @param request request
     * @return 用户
     */
    User userLogin(String userAccount, String password, HttpServletRequest request);

    /**
     * 获取当前登录用户
     * @param request request
     * @return 当前登录用户
     */
    User getLoginUser(HttpServletRequest request);

    /**
     * 用户登出
     * @param request request
     */
    void userLogout(HttpServletRequest request);

    // region 管理员功能
    /**
     * 管理员接口: 添加用户
     *
     * @return 用户 id
     */
    long addUser(UserAddRequest userAddRequest);

    /**
     * 管理员接口: 删除用户
     * @param id 用户id
     * @return 删除结果
     */
    boolean deleteUser(Long id);

    // endregion

    /**
     * 分页查询用户
     * @param userSearchRequest 用户查询请求
     * @return 用户分页列表
     */
    Page<User> pageListUser(UserSearchRequest userSearchRequest);

    /**
     * 管理员接口: 更新用户
     * @param userUpdateRequest 用户更新请求
     * @param request request
     * @return 更新结果
     */
    boolean updateUser(UserUpdateRequest userUpdateRequest, HttpServletRequest request);
}
