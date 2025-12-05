package com.dingzk.codeless.service;

import com.dingzk.codeless.common.DeleteRequest;
import com.dingzk.codeless.exception.BusinessException;
import com.dingzk.codeless.model.dto.user.UserAddRequest;
import com.dingzk.codeless.model.dto.user.UserSearchRequest;
import com.dingzk.codeless.model.dto.user.UserUpdateRequest;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Arrays;

/**
 * UserServiceTest
 *
 * @author ding
 * @date 2025/12/4 18:25
 */
@SpringBootTest
class UserServiceTest {

    @Resource
    private UserService userService;

    private static final String TEST_USER_ACCOUNT = "test1";
    private static final String TEST_PASSWORD = "12345678";

    private static final Long TEST_UPDATED_USER_ID = 353022803707863040L;

    @Test
    void test_userRegisterFailed_whenParamsAnyBlank() {
        final String errorMessage = "账号、密码、确认密码不能为空";

        BusinessException exception1 = Assertions.assertThrows(BusinessException.class, () ->
                userService.userRegister("", "", ""));
        Assertions.assertEquals(errorMessage, exception1.getMessage());

        BusinessException exception2 = Assertions.assertThrows(BusinessException.class, () ->
                userService.userRegister(TEST_USER_ACCOUNT, "", ""));
        Assertions.assertEquals(errorMessage, exception2.getMessage());

        BusinessException exception3 = Assertions.assertThrows(BusinessException.class, () ->
                userService.userRegister("", TEST_PASSWORD, ""));
        Assertions.assertEquals(errorMessage, exception3.getMessage());

        BusinessException exception4 = Assertions.assertThrows(BusinessException.class, () ->
                userService.userRegister("", "", TEST_PASSWORD));
        Assertions.assertEquals(errorMessage, exception4.getMessage());

        BusinessException exception5 = Assertions.assertThrows(BusinessException.class, () ->
                userService.userRegister("", TEST_PASSWORD, TEST_PASSWORD));
        Assertions.assertEquals(errorMessage, exception5.getMessage());

        BusinessException exception6 = Assertions.assertThrows(BusinessException.class, () ->
                userService.userRegister(TEST_USER_ACCOUNT, "", TEST_PASSWORD));
        Assertions.assertEquals(errorMessage, exception6.getMessage());

        BusinessException exception7 = Assertions.assertThrows(BusinessException.class, () ->
                userService.userRegister(TEST_USER_ACCOUNT, TEST_PASSWORD, ""));
        Assertions.assertEquals(errorMessage, exception7.getMessage());

    }

    @Test
    void test_userRegisterFailed_whenParamsIllegal() {
        BusinessException exception1 = Assertions.assertThrows(BusinessException.class, () ->
                userService.userRegister("123", TEST_PASSWORD, TEST_PASSWORD));
        Assertions.assertEquals("账号长度不合法", exception1.getMessage());

        BusinessException exception2 = Assertions.assertThrows(BusinessException.class, () ->
                userService.userRegister(TEST_USER_ACCOUNT, "123", "123"));
        Assertions.assertEquals("密码长度不合法", exception2.getMessage());

        BusinessException exception3 = Assertions.assertThrows(BusinessException.class, () ->
                userService.userRegister(TEST_USER_ACCOUNT, TEST_PASSWORD, "123"));
        Assertions.assertEquals("密码和确认密码不一致", exception3.getMessage());

        BusinessException exception4 = Assertions.assertThrows(BusinessException.class, () ->
                userService.userRegister(TEST_USER_ACCOUNT, TEST_PASSWORD, TEST_PASSWORD));
        Assertions.assertEquals("账号已被注册", exception4.getMessage());

        BusinessException exception5 = Assertions.assertThrows(BusinessException.class, () ->
                userService.userRegister("Hello 123你好", TEST_PASSWORD, TEST_PASSWORD));
        Assertions.assertEquals("账号格式不合法", exception5.getMessage());

        BusinessException exception6 = Assertions.assertThrows(BusinessException.class, () ->
                userService.userRegister(TEST_USER_ACCOUNT, "123你好hello", TEST_PASSWORD));
        Assertions.assertEquals("密码格式不合法", exception6.getMessage());
    }

    @Test
    void test_userLoginFailed_whenParamsAnyBlank() {
        final String errorMessage = "账号或密码不能为空";

        BusinessException exception1 = Assertions.assertThrows(BusinessException.class, () ->
                userService.userLogin("", "", null));
        Assertions.assertEquals(errorMessage, exception1.getMessage());

        BusinessException exception2 = Assertions.assertThrows(BusinessException.class, () ->
                userService.userLogin(TEST_USER_ACCOUNT, "", null));
        Assertions.assertEquals(errorMessage, exception2.getMessage());

        BusinessException exception3 = Assertions.assertThrows(BusinessException.class, () ->
                userService.userLogin("", TEST_PASSWORD, null));
        Assertions.assertEquals(errorMessage, exception3.getMessage());
    }

    @Test
    void test_userLoginFailed_whenParamsIllegal() {
        BusinessException exception1 = Assertions.assertThrows(BusinessException.class, () ->
                userService.userLogin("123", TEST_PASSWORD, null));
        Assertions.assertEquals("账号长度不合法", exception1.getMessage());

        BusinessException exception2 = Assertions.assertThrows(BusinessException.class, () ->
                userService.userLogin(TEST_USER_ACCOUNT, "123", null));
        Assertions.assertEquals("密码长度不合法", exception2.getMessage());

        BusinessException exception3 = Assertions.assertThrows(BusinessException.class, () ->
                userService.userLogin(TEST_USER_ACCOUNT, "djakjdjkas", null));
        Assertions.assertEquals("账号或密码错误", exception3.getMessage());

        BusinessException exception4 = Assertions.assertThrows(BusinessException.class, () ->
                userService.userLogin("Hello 123", TEST_PASSWORD, null));
        Assertions.assertEquals("账号格式不合法", exception4.getMessage());

        BusinessException exception5 = Assertions.assertThrows(BusinessException.class, () ->
                userService.userLogin(TEST_USER_ACCOUNT, "123 hello", null));
        Assertions.assertEquals("密码格式不合法", exception5.getMessage());
    }

    @Test
    void test_addUserFailed_whenAccountIllegal() {
        final String errorMessage = "账号不合法";
        UserAddRequest userAddRequest = new UserAddRequest();
        String userAccount = "";
        userAddRequest.setUserAccount(userAccount);
        BusinessException exception1 = Assertions.assertThrows(BusinessException.class, () ->
                userService.addUser(userAddRequest));
        Assertions.assertEquals(errorMessage, exception1.getMessage());

        userAccount = "123";
        userAddRequest.setUserAccount(userAccount);
        BusinessException exception2 = Assertions.assertThrows(BusinessException.class, () ->
                userService.addUser(userAddRequest));
        Assertions.assertEquals(errorMessage, exception2.getMessage());

        userAccount = "123456789012";
        userAddRequest.setUserAccount(userAccount);
        BusinessException exception3 = Assertions.assertThrows(BusinessException.class, () ->
                userService.addUser(userAddRequest));
        Assertions.assertEquals(errorMessage, exception3.getMessage());

        userAccount = "     ";
        userAddRequest.setUserAccount(userAccount);
        BusinessException exception4 = Assertions.assertThrows(BusinessException.class, () ->
                userService.addUser(userAddRequest));
        Assertions.assertEquals(errorMessage, exception4.getMessage());
    }

    @Test
    void test_addUserFailed_whenPasswordIllegal() {
        final String errorMessage = "密码不合法";
        UserAddRequest userAddRequest = new UserAddRequest();
        String password = "";
        userAddRequest.setUserAccount(TEST_USER_ACCOUNT);
        userAddRequest.setPassword(password);
        BusinessException exception1 = Assertions.assertThrows(BusinessException.class, () ->
                userService.addUser(userAddRequest));
        Assertions.assertEquals(errorMessage, exception1.getMessage());

        password = "1234567";
        userAddRequest.setPassword(password);
        BusinessException exception2 = Assertions.assertThrows(BusinessException.class, () ->
                userService.addUser(userAddRequest));
        Assertions.assertEquals(errorMessage, exception2.getMessage());

        password = "12345678901234567";
        userAddRequest.setPassword(password);
        BusinessException exception3 = Assertions.assertThrows(BusinessException.class, () ->
                userService.addUser(userAddRequest));
        Assertions.assertEquals(errorMessage, exception3.getMessage());

        password = "         ";
        userAddRequest.setPassword(password);
        BusinessException exception4 = Assertions.assertThrows(BusinessException.class, () ->
                userService.addUser(userAddRequest));
        Assertions.assertEquals(errorMessage, exception4.getMessage());
    }

    @Test
    void test_addUserFailed_whenUsernameIllegal() {
        final String errorMessage = "用户名不合法";
        UserAddRequest userAddRequest = new UserAddRequest();
        String username = "12";
        userAddRequest.setUserAccount(TEST_USER_ACCOUNT);
        userAddRequest.setPassword(TEST_PASSWORD);
        userAddRequest.setUsername(username);
        BusinessException exception1 = Assertions.assertThrows(BusinessException.class, () ->
                userService.addUser(userAddRequest));
        Assertions.assertEquals(errorMessage, exception1.getMessage());

        username = "12345678901234567";
        userAddRequest.setUsername(username);
        BusinessException exception2 = Assertions.assertThrows(BusinessException.class, () ->
                userService.addUser(userAddRequest));
        Assertions.assertEquals(errorMessage, exception2.getMessage());

        username = "         ";
        userAddRequest.setUsername(username);
        BusinessException exception3 = Assertions.assertThrows(BusinessException.class, () ->
                userService.addUser(userAddRequest));
        Assertions.assertEquals(errorMessage, exception3.getMessage());
    }

    @Test
    void test_addUserFailed_whenUserIntroIllegal() {
        final String errorMessage = "用户简介过长";
        UserAddRequest userAddRequest = new UserAddRequest();
        userAddRequest.setUserAccount(TEST_USER_ACCOUNT);
        userAddRequest.setPassword(TEST_PASSWORD);
        char[] charArray = new char[101];
        Arrays.fill(charArray, 'a');
        String userIntro = new String(charArray);
        userAddRequest.setUserIntro(userIntro);
        BusinessException exception1 = Assertions.assertThrows(BusinessException.class, () ->
                userService.addUser(userAddRequest));
        Assertions.assertEquals(errorMessage, exception1.getMessage());
    }

    @Test
    void test_addUserFailed_whenGenderIllegal() {
        final String errorMessage = "性别不合法";
        UserAddRequest userAddRequest = new UserAddRequest();
        userAddRequest.setUserAccount(TEST_USER_ACCOUNT);
        userAddRequest.setPassword(TEST_PASSWORD);

        int gender = -1;
        userAddRequest.setGender(gender);
        BusinessException exception1 = Assertions.assertThrows(BusinessException.class, () ->
                userService.addUser(userAddRequest));
        Assertions.assertEquals(errorMessage, exception1.getMessage());
    }

    @Test
    void test_addUserFailed_whenUserRoleIllegal() {
        final String errorMessage = "用户角色不合法";
        UserAddRequest userAddRequest = new UserAddRequest();
        userAddRequest.setUserAccount(TEST_USER_ACCOUNT);
        userAddRequest.setPassword(TEST_PASSWORD);

        int role = -1;
        userAddRequest.setUserRole(role);
        userAddRequest.setGender(0);
        BusinessException exception1 = Assertions.assertThrows(BusinessException.class, () ->
                userService.addUser(userAddRequest));
        Assertions.assertEquals(errorMessage, exception1.getMessage());
    }

    /**
     * 更新用户功能测试代码暂不可用 2025-12-05 19:59:10
     */
//    @Test
    void test_updateUserFailed_whenIdIsNull() {
        final String errorMessage = "用户ID不能为空";
        UserUpdateRequest userUpdateRequest = new UserUpdateRequest();
        BusinessException exception1 = Assertions.assertThrows(BusinessException.class, () ->
                userService.updateUser(userUpdateRequest, null));
        Assertions.assertEquals(errorMessage, exception1.getMessage());
    }

//    @Test
    void test_updateUserFailed_whenAccountIllegal() {
        final String errorMessage = "账号不合法";
        UserUpdateRequest userUpdateRequest = new UserUpdateRequest();
        userUpdateRequest.setId(TEST_UPDATED_USER_ID);
        String userAccount = "";
        userUpdateRequest.setUserAccount(userAccount);
        BusinessException exception1 = Assertions.assertThrows(BusinessException.class, () ->
                userService.updateUser(userUpdateRequest, null));
        Assertions.assertEquals(errorMessage, exception1.getMessage());

        userAccount = "123";
        userUpdateRequest.setUserAccount(userAccount);
        BusinessException exception2 = Assertions.assertThrows(BusinessException.class, () ->
                userService.updateUser(userUpdateRequest, null));
        Assertions.assertEquals(errorMessage, exception2.getMessage());

        userAccount = "123456789012";
        userUpdateRequest.setUserAccount(userAccount);
        BusinessException exception3 = Assertions.assertThrows(BusinessException.class, () ->
                userService.updateUser(userUpdateRequest, null));
        Assertions.assertEquals(errorMessage, exception3.getMessage());

        userAccount = "     ";
        userUpdateRequest.setUserAccount(userAccount);
        BusinessException exception4 = Assertions.assertThrows(BusinessException.class, () ->
                userService.updateUser(userUpdateRequest, null));
        Assertions.assertEquals(errorMessage, exception4.getMessage());
    }

//    @Test
    void test_updateUserFailed_whenPasswordIllegal() {
        final String errorMessage = "密码不合法";
        UserUpdateRequest userUpdateRequest = new UserUpdateRequest();
        userUpdateRequest.setId(TEST_UPDATED_USER_ID);
        String password = "";
        userUpdateRequest.setPassword(password);
        BusinessException exception1 = Assertions.assertThrows(BusinessException.class, () ->
                userService.updateUser(userUpdateRequest, null));
        Assertions.assertEquals(errorMessage, exception1.getMessage());

        password = "1234567";
        userUpdateRequest.setPassword(password);
        BusinessException exception2 = Assertions.assertThrows(BusinessException.class, () ->
                userService.updateUser(userUpdateRequest, null));
        Assertions.assertEquals(errorMessage, exception2.getMessage());

        password = "12345678901234567";
        userUpdateRequest.setPassword(password);
        BusinessException exception3 = Assertions.assertThrows(BusinessException.class, () ->
                userService.updateUser(userUpdateRequest, null));
        Assertions.assertEquals(errorMessage, exception3.getMessage());

        password = "         ";
        userUpdateRequest.setPassword(password);
        BusinessException exception4 = Assertions.assertThrows(BusinessException.class, () ->
                userService.updateUser(userUpdateRequest, null));
        Assertions.assertEquals(errorMessage, exception4.getMessage());
    }

//    @Test
    void test_updateUserFailed_whenUsernameIllegal() {
        final String errorMessage = "用户名不合法";
        UserUpdateRequest userUpdateRequest = new UserUpdateRequest();
        userUpdateRequest.setId(TEST_UPDATED_USER_ID);
        String username = "12";
        userUpdateRequest.setUsername(username);
        BusinessException exception1 = Assertions.assertThrows(BusinessException.class, () ->
                userService.updateUser(userUpdateRequest, null));
        Assertions.assertEquals(errorMessage, exception1.getMessage());

        username = "12345678901234567";
        userUpdateRequest.setUsername(username);
        BusinessException exception2 = Assertions.assertThrows(BusinessException.class, () ->
                userService.updateUser(userUpdateRequest, null));
        Assertions.assertEquals(errorMessage, exception2.getMessage());

        username = "         ";
        userUpdateRequest.setUsername(username);
        BusinessException exception3 = Assertions.assertThrows(BusinessException.class, () ->
                userService.updateUser(userUpdateRequest, null));
        Assertions.assertEquals(errorMessage, exception3.getMessage());
    }

//    @Test
    void test_updateUserFailed_whenUserIntroIllegal() {
        final String errorMessage = "用户简介过长";
        UserUpdateRequest userUpdateRequest = new UserUpdateRequest();
        userUpdateRequest.setId(TEST_UPDATED_USER_ID);
        char[] charArray = new char[101];
        Arrays.fill(charArray, 'a');
        String userIntro = new String(charArray);
        userUpdateRequest.setUserIntro(userIntro);
        BusinessException exception1 = Assertions.assertThrows(BusinessException.class, () ->
                userService.updateUser(userUpdateRequest, null));
        Assertions.assertEquals(errorMessage, exception1.getMessage());
    }

//    @Test
    void test_updateUserFailed_whenGenderIllegal() {
        final String errorMessage = "性别不合法";
        UserUpdateRequest userUpdateRequest = new UserUpdateRequest();
        userUpdateRequest.setId(TEST_UPDATED_USER_ID);

        int gender = -1;
        userUpdateRequest.setGender(gender);
        BusinessException exception1 = Assertions.assertThrows(BusinessException.class, () ->
                userService.updateUser(userUpdateRequest, null));
        Assertions.assertEquals(errorMessage, exception1.getMessage());
    }

//    @Test
    void test_updateUserFailed_whenUserRoleIllegal() {
        final String errorMessage = "用户角色不合法";
        UserUpdateRequest userUpdateRequest = new UserUpdateRequest();
        userUpdateRequest.setId(TEST_UPDATED_USER_ID);

        int role = -1;
        userUpdateRequest.setUserRole(role);
        userUpdateRequest.setGender(0);
        BusinessException exception1 = Assertions.assertThrows(BusinessException.class, () ->
                userService.updateUser(userUpdateRequest, null));
        Assertions.assertEquals(errorMessage, exception1.getMessage());
    }

//    @Test
    void test_updateUserFailed_whenUserNotExist() {
        UserUpdateRequest userUpdateRequest = new UserUpdateRequest();
        userUpdateRequest.setUserRole(0);
        userUpdateRequest.setGender(0);
        userUpdateRequest.setId(TEST_UPDATED_USER_ID + 1);
        BusinessException exception1 = Assertions.assertThrows(BusinessException.class, () ->
                userService.updateUser(userUpdateRequest, null));
        Assertions.assertEquals("用户不存在", exception1.getMessage());
    }

    @Test
    void test_deleteUserFailed_whenIdIsNull() {
        DeleteRequest deleteRequest = new DeleteRequest();
        BusinessException exception1 = Assertions.assertThrows(BusinessException.class, () ->
                userService.deleteUser(deleteRequest.getId()));
        Assertions.assertEquals("用户ID不能为空", exception1.getMessage());
    }

    @Test
    void test_deleteUserFailed_whenUserNotExist() {
        DeleteRequest deleteRequest = new DeleteRequest();
        deleteRequest.setId(TEST_UPDATED_USER_ID + 1);
        BusinessException exception1 = Assertions.assertThrows(BusinessException.class, () ->
                userService.deleteUser(deleteRequest.getId()));
        Assertions.assertEquals("用户不存在", exception1.getMessage());
    }

    @Test
    void test_pageListUserFailed_whenInvalidParams() {
        UserSearchRequest userSearchRequest = new UserSearchRequest();
        userSearchRequest.setId(-1L);
        BusinessException exception1 = Assertions.assertThrows(BusinessException.class, () ->
                userService.pageListUser(userSearchRequest));
        Assertions.assertEquals("用户ID不合法", exception1.getMessage());

        userSearchRequest.setId(1L);
        userSearchRequest.setGender(3);
        BusinessException exception2 = Assertions.assertThrows(BusinessException.class, () ->
                userService.pageListUser(userSearchRequest));
        Assertions.assertEquals("性别不合法", exception2.getMessage());

        userSearchRequest.setGender(1);
        userSearchRequest.setUserRole(3);
        BusinessException exception3 = Assertions.assertThrows(BusinessException.class, () ->
                userService.pageListUser(userSearchRequest));
        Assertions.assertEquals("用户角色不合法", exception3.getMessage());

        userSearchRequest.setUserRole(1);
        userSearchRequest.setPageNum(0);
        BusinessException exception4 = Assertions.assertThrows(BusinessException.class, () ->
                userService.pageListUser(userSearchRequest));
        Assertions.assertEquals("页码不合法", exception4.getMessage());

        userSearchRequest.setPageNum(1);
        userSearchRequest.setPageSize(0);
        BusinessException exception5 = Assertions.assertThrows(BusinessException.class, () ->
                userService.pageListUser(userSearchRequest));
        Assertions.assertEquals("每页条数不合法", exception5.getMessage());

        userSearchRequest.setPageSize(10);
        userSearchRequest.setSortOrder("abc");
        BusinessException exception6 = Assertions.assertThrows(BusinessException.class, () ->
                userService.pageListUser(userSearchRequest));
        Assertions.assertEquals("排序方式不合法", exception6.getMessage());
    }
}