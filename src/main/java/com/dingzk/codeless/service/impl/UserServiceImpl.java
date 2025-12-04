package com.dingzk.codeless.service.impl;

import com.dingzk.codeless.common.SearchRequest;
import com.dingzk.codeless.constant.UserConstant;
import com.dingzk.codeless.exception.ErrorCode;
import com.dingzk.codeless.exception.ThrowUtils;
import com.dingzk.codeless.mapper.UserMapper;
import com.dingzk.codeless.model.dto.user.UserAddRequest;
import com.dingzk.codeless.model.dto.user.UserSearchRequest;
import com.dingzk.codeless.model.dto.user.UserUpdateRequest;
import com.dingzk.codeless.model.entity.User;
import com.dingzk.codeless.model.enums.UserGenderEnum;
import com.dingzk.codeless.model.enums.UserRoleEnum;
import com.dingzk.codeless.service.UserService;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

/**
 * 用户服务实现类
 *
 * @author ding
 * @date 2025/12/4 15:24
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User>  implements UserService {
    /**
     * 账号最大长度
     */
    private static final int USER_ACCOUNT_MAX_LENGTH = 11;
    /**
     * 账号最小长度
     */
    private static final int USER_ACCOUNT_MIN_LENGTH = 4;
    /**
     * 账号正则表达式
     */
    private static final String USER_ACCOUNT_REGEX = "^[a-zA-Z0-9]{4,11}$";
    /**
     * 密码最大长度
     */
    private static final int PASSWORD_MAX_LENGTH = 16;
    /**
     * 密码最小长度
     */
    private static final int PASSWORD_MIN_LENGTH = 8;
    /**
     * 密码正则校验
     */
    private static final String PASSWORD_REGEX = "^[A-Za-z\\d!\"#$%&'()*+,\\-./:;<=>?@\\[\\]^_{|}~\\\\]{8,16}$";
    /**
     * 用户名最大长度
     */
    private static final int USERNAME_MAX_LENGTH = 16;
    /**
     * 用户名最小长度
     */
    private static final int USERNAME_MIN_LENGTH = 3;
    /**
     * 用户名正则表达式
     */
    private static final String USERNAME_REGEX = "^[\\S]{3,16}$";


    @Override
    public long userRegister(String userAccount, String password, String checkedPassword) {
        // 1. 参数校验
        // 账号、密码、确认密码全不能为空
        ThrowUtils.throwIf(StringUtils.isAnyBlank(userAccount, password, checkedPassword),
                ErrorCode.BAD_PARAM_ERROR, "账号、密码、确认密码不能为空");
        // 账号密码校验
        validateUserAccountAndPassword(userAccount, password);

        // 密码和确认密码要一致
        ThrowUtils.throwIf(!password.equals(checkedPassword),
                ErrorCode.BAD_PARAM_ERROR, "密码和确认密码不一致");

        // 2. 账号是否已存在
        synchronized (userAccount.intern()) {
            QueryWrapper queryWrapper = QueryWrapper.create()
                    .eq("user_account", userAccount);
            ThrowUtils.throwIf(mapper.selectCountByQuery(queryWrapper) > 0,
                    ErrorCode.OPERATION_ERROR, "账号已被注册");
        }

        // 3. 密码加密
        String encryptPassword = encryptPassword(password);

        // 4. 注册用户
        User user = User.builder()
                .userAccount(userAccount)
                .username(userAccount)
                .password(encryptPassword).build();
        this.save(user);
        return user.getId();
    }

    @Override
    public User userLogin(String userAccount, String password, HttpServletRequest request) {
        // 1. 参数校验
        // 账号或密码不能为空
        ThrowUtils.throwIf(StringUtils.isAnyBlank(userAccount, password), ErrorCode.BAD_PARAM_ERROR, "账号或密码不能为空");
        // 账号密码校验
        validateUserAccountAndPassword(userAccount, password);

        // 2. 查询数据库尝试登录
        QueryWrapper queryWrapper = QueryWrapper.create()
                .eq("user_account", userAccount)
                .eq("password", encryptPassword(password));
        User user = mapper.selectOneByQuery(queryWrapper);
        ThrowUtils.throwIf(user == null, ErrorCode.BAD_PARAM_ERROR, "账号或密码错误");

        // 3. 记录登录态
        if (request != null) {
            request.getSession().setAttribute(UserConstant.USER_LOGIN_STATE_KEY, user.getId());
        }
        return user;
    }

    private void validateUserAccountAndPassword(String userAccount, String password) {
        // 账号长度合法
        ThrowUtils.throwIf(
                userAccount.length() < USER_ACCOUNT_MIN_LENGTH || userAccount.length() > USER_ACCOUNT_MAX_LENGTH,
                ErrorCode.BAD_PARAM_ERROR, "账号长度不合法");
        // 账号正则校验
        ThrowUtils.throwIf(!userAccount.matches(USER_ACCOUNT_REGEX), ErrorCode.BAD_PARAM_ERROR, "账号格式不合法");
        // 密码长度合法
        ThrowUtils.throwIf(password.length() < PASSWORD_MIN_LENGTH || password.length() > PASSWORD_MAX_LENGTH,
                ErrorCode.BAD_PARAM_ERROR, "密码长度不合法");
        // 密码正则校验
        ThrowUtils.throwIf(!password.matches(PASSWORD_REGEX), ErrorCode.BAD_PARAM_ERROR, "密码格式不合法");
    }

    private String encryptPassword(String password) {
        final String salt = "dingzi";
        return DigestUtils.md5DigestAsHex((salt + password).getBytes());
    }

    @Override
    public User getLoginUser(HttpServletRequest request) {
        // 1. 获取 session
        HttpSession session = request.getSession();
        // 2. 获取 session 中的用户
        Object userObj = session.getAttribute(UserConstant.USER_LOGIN_STATE_KEY);
        ThrowUtils.throwIf(userObj == null, ErrorCode.NOT_LOGIN_ERROR);
        Long userId = (Long) userObj;
        // 3. 查询数据库
        User loginUser = mapper.selectOneById(userId);
        ThrowUtils.throwIf(loginUser == null, ErrorCode.NOT_FOUND_ERROR, "用户不存在");
        return loginUser;
    }

    @Override
    public void userLogout(HttpServletRequest request) {
        // 1. 获取 session
        HttpSession session = request.getSession();
        // 2. 获取 session 中的用户
        Object userObj = session.getAttribute(UserConstant.USER_LOGIN_STATE_KEY);
        ThrowUtils.throwIf(userObj == null, ErrorCode.NOT_LOGIN_ERROR);
        session.removeAttribute(UserConstant.USER_LOGIN_STATE_KEY);
    }

    // region 管理员功能
    @Override
    public long addUser(UserAddRequest userAddRequest) {
        // 1. 参数校验
        User userToSave = new User();
        BeanUtils.copyProperties(userAddRequest, userToSave);
        validateUserParams(userToSave, true);

        if (userToSave.getUsername() == null) {
            userToSave.setUsername(userToSave.getUserAccount());
        }
        // 2. 保存用户
        // 密码入库前加密
        userToSave.setPassword(encryptPassword(userToSave.getPassword()));
        boolean result = this.save(userToSave);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR, "添加用户失败");
        return userToSave.getId();
    }

    @Override
    public boolean updateUser(UserUpdateRequest userUpdateRequest) {
        // 1. 参数校验
        User userToSave = new User();
        BeanUtils.copyProperties(userUpdateRequest, userToSave);
        validateUserParams(userToSave, false);

        // 2. 更新用户
        // 密码入库前加密
        userToSave.setPassword(encryptPassword(userToSave.getPassword()));
        boolean result = this.updateById(userToSave);
        ThrowUtils.throwIf(!result, ErrorCode.NOT_FOUND_ERROR, "用户不存在");
        return true;
    }

    private void validateUserParams(User user, boolean isAdd) {
        // 1. 参数校验
        Long id = user.getId();
        ThrowUtils.throwIf(!isAdd && id == null, ErrorCode.BAD_PARAM_ERROR, "用户ID不能为空");

        String userAccount = user.getUserAccount();
        if (isAdd || userAccount != null) {
            ThrowUtils.throwIf(StringUtils.isBlank(userAccount)
                            || (userAccount.length() < USER_ACCOUNT_MIN_LENGTH
                                    || userAccount.length() > USER_ACCOUNT_MAX_LENGTH
                                    || !userAccount.matches(USER_ACCOUNT_REGEX)),
                    ErrorCode.BAD_PARAM_ERROR, "账号不合法"
            );
        }

        String password = user.getPassword();
        if (isAdd || password != null) {
            ThrowUtils.throwIf(StringUtils.isBlank(password)
                            || (password.length() < PASSWORD_MIN_LENGTH
                                    || password.length() > PASSWORD_MAX_LENGTH
                                    || !password.matches(PASSWORD_REGEX)),
                    ErrorCode.BAD_PARAM_ERROR, "密码不合法"
            );
        }

        String username = user.getUsername();
        ThrowUtils.throwIf(username != null && StringUtils.isBlank(username),
                ErrorCode.BAD_PARAM_ERROR, "用户名不合法"
        );
        ThrowUtils.throwIf(StringUtils.isNotBlank(username)
                        && (username.length() < USERNAME_MIN_LENGTH
                            || username.length() > USERNAME_MAX_LENGTH
                            || !username.matches(USERNAME_REGEX)),
                ErrorCode.BAD_PARAM_ERROR, "用户名不合法"
        );

        String userIntro = user.getUserIntro();
        ThrowUtils.throwIf(StringUtils.isNotBlank(userIntro) && userIntro.length() > 100,
                ErrorCode.BAD_PARAM_ERROR, "用户简介过长"
        );

        Integer gender = user.getGender();
        ThrowUtils.throwIf(UserGenderEnum.fromValue(gender) == null,
                ErrorCode.BAD_PARAM_ERROR, "性别不合法"
        );

        Integer userRole = user.getUserRole();
        ThrowUtils.throwIf(UserRoleEnum.fromValue(userRole) == null,
                ErrorCode.BAD_PARAM_ERROR, "用户角色不合法"
        );
    }

    @Override
    public boolean deleteUser(Long id) {
        ThrowUtils.throwIf(id == null, ErrorCode.BAD_PARAM_ERROR, "用户ID不能为空");
        boolean result = this.removeById(id);
        ThrowUtils.throwIf(!result, ErrorCode.NOT_FOUND_ERROR, "用户不存在");
        return true;
    }

    // endregion

    @Override
    public Page<User> pageListUser(UserSearchRequest userSearchRequest) {
        // 1. 部分参数校验
        Long id = userSearchRequest.getId();
        ThrowUtils.throwIf(id != null && id < 0, ErrorCode.BAD_PARAM_ERROR, "用户ID不合法");

        Integer gender = userSearchRequest.getGender();
        ThrowUtils.throwIf(gender != null && UserGenderEnum.fromValue(gender) == null,
                ErrorCode.BAD_PARAM_ERROR, "性别不合法"
        );

        Integer userRole = userSearchRequest.getUserRole();
        ThrowUtils.throwIf(userRole != null && UserRoleEnum.fromValue(userRole) == null,
                ErrorCode.BAD_PARAM_ERROR, "用户角色不合法"
        );
        Integer pageNum = userSearchRequest.getPageNum();
        ThrowUtils.throwIf(pageNum == null || pageNum <= 0, ErrorCode.BAD_PARAM_ERROR, "页码不合法");
        Integer pageSize = userSearchRequest.getPageSize();
        ThrowUtils.throwIf(pageSize == null || pageSize <= 0, ErrorCode.BAD_PARAM_ERROR, "每页条数不合法");

        String sortOrder = userSearchRequest.getSortOrder();
        ThrowUtils.throwIf(SearchRequest.SortOrder.fromString(sortOrder) == null,
                ErrorCode.BAD_PARAM_ERROR, "排序方式不合法");

        // 2. 构造查询条件
        // 3. 执行查询
        return this.page(Page.of(pageNum, pageSize), mapper.buildQueryWrapper(userSearchRequest));
    }
}
