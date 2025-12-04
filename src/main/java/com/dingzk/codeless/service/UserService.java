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

    boolean updateUser(UserUpdateRequest userUpdateRequest);

    boolean deleteUser(Long id);

    // endregion

    Page<User> pageListUser(UserSearchRequest userSearchRequest);
}
