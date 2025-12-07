package com.dingzk.codeless.service;

import com.dingzk.codeless.exception.BusinessException;
import com.dingzk.codeless.mapper.AppMapper;
import com.dingzk.codeless.model.dto.app.AppAddRequest;
import com.dingzk.codeless.model.dto.app.AppAdminUpdateRequest;
import com.dingzk.codeless.model.dto.app.AppSearchRequest;
import com.dingzk.codeless.model.dto.app.AppUpdateRequest;
import com.dingzk.codeless.model.entity.App;
import com.dingzk.codeless.model.entity.User;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.Arrays;

/**
 * AppServiceTest
 *
 * @author ding
 * @date 2025/12/7 14:45
 */
@SpringBootTest
class AppServiceTest {

    @Resource
    private AppService appService;

    @MockitoBean
    private AppMapper appMapper;

    private static final String TEST_INITIAL_PROMPT = "这是一个测试应用，用于生成一个简单的计算器页面";
    private static final String TEST_APP_NAME = "测试应用";

    // 测试应用ID，需要在数据库中存在且属于测试用户
    private static final Long TEST_APP_ID = 353022803707863040L;
    // 测试用户ID
    private static final Long TEST_USER_ID = 353022803707863041L;
    
    private static final App TEST_APP = App.builder().id(TEST_APP_ID).name(TEST_APP_NAME).userId(TEST_USER_ID).build();

    @Test
    void test_addAppFailed_whenLoginUserIsNull() {
        final String errorMessage = "用户不存在";
        AppAddRequest appAddRequest = new AppAddRequest();
        appAddRequest.setInitialPrompt(TEST_INITIAL_PROMPT);

        BusinessException exception = Assertions.assertThrows(BusinessException.class, () ->
                appService.addApp(appAddRequest, null));
        Assertions.assertEquals(errorMessage, exception.getMessage());
    }

    @Test
    void test_addAppFailed_whenInitialPromptIsBlank() {
        final String errorMessage = "提示词不能未空且长度不超过10000";
        AppAddRequest appAddRequest = new AppAddRequest();
        User testUser = User.builder().id(TEST_USER_ID).build();

        // 测试空字符串
        appAddRequest.setInitialPrompt("");
        BusinessException exception1 = Assertions.assertThrows(BusinessException.class, () ->
                appService.addApp(appAddRequest, testUser));
        Assertions.assertEquals(errorMessage, exception1.getMessage());

        // 测试null
        appAddRequest.setInitialPrompt(null);
        BusinessException exception2 = Assertions.assertThrows(BusinessException.class, () ->
                appService.addApp(appAddRequest, testUser));
        Assertions.assertEquals(errorMessage, exception2.getMessage());
    }

    @Test
    void test_addAppFailed_whenInitialPromptTooLong() {
        final String errorMessage = "提示词不能未空且长度不超过10000";
        AppAddRequest appAddRequest = new AppAddRequest();
        User testUser = User.builder().id(TEST_USER_ID).build();

        // 创建超过10000个字符的字符串
        char[] charArray = new char[10001];
        Arrays.fill(charArray, 'a');
        String longPrompt = new String(charArray);
        appAddRequest.setInitialPrompt(longPrompt);

        BusinessException exception = Assertions.assertThrows(BusinessException.class, () ->
                appService.addApp(appAddRequest, testUser));
        Assertions.assertEquals(errorMessage, exception.getMessage());
    }

    @Test
    void test_updateAppFailed_whenIdIsNull() {
        final String errorMessage = "应用ID不合法";
        AppUpdateRequest appUpdateRequest = new AppUpdateRequest();
        User testUser = User.builder().id(TEST_USER_ID).build();

        // 测试null
        appUpdateRequest.setId(null);
        BusinessException exception1 = Assertions.assertThrows(BusinessException.class, () ->
                appService.updateApp(appUpdateRequest, testUser));
        Assertions.assertEquals(errorMessage, exception1.getMessage());

        // 测试<=0
        appUpdateRequest.setId(0L);
        BusinessException exception2 = Assertions.assertThrows(BusinessException.class, () ->
                appService.updateApp(appUpdateRequest, testUser));
        Assertions.assertEquals(errorMessage, exception2.getMessage());

        appUpdateRequest.setId(-1L);
        BusinessException exception3 = Assertions.assertThrows(BusinessException.class, () ->
                appService.updateApp(appUpdateRequest, testUser));
        Assertions.assertEquals(errorMessage, exception3.getMessage());
    }

    @Test
    void test_updateAppFailed_whenLoginUserIsNull() {
        final String errorMessage = "用户不存在";
        AppUpdateRequest appUpdateRequest = new AppUpdateRequest();
        appUpdateRequest.setId(TEST_APP_ID);

        BusinessException exception = Assertions.assertThrows(BusinessException.class, () ->
                appService.updateApp(appUpdateRequest, null));
        Assertions.assertEquals(errorMessage, exception.getMessage());
    }

    @Test
    void test_updateAppFailed_whenAppNotExist() {
        final String errorMessage = "应用不存在";
        AppUpdateRequest appUpdateRequest = new AppUpdateRequest();
        appUpdateRequest.setId(TEST_APP_ID + 1000); // 不存在的应用ID
        appUpdateRequest.setName(TEST_APP_NAME);
        User testUser = User.builder().id(TEST_USER_ID).build();

        BusinessException exception = Assertions.assertThrows(BusinessException.class, () ->
                appService.updateApp(appUpdateRequest, testUser));
        Assertions.assertEquals(errorMessage, exception.getMessage());
    }

    @Test
    void test_updateAppFailed_whenNoPermission() {
        final String errorMessage = "无权限";
        AppUpdateRequest appUpdateRequest = new AppUpdateRequest();
        appUpdateRequest.setId(TEST_APP_ID);
        appUpdateRequest.setName(TEST_APP_NAME);
        User otherUser = User.builder().id(TEST_USER_ID + 1000).build(); // 其他用户

        // mock 数据
        Mockito.when(appMapper.selectOneById(Mockito.any())).thenReturn(TEST_APP);

        BusinessException exception = Assertions.assertThrows(BusinessException.class, () ->
                appService.updateApp(appUpdateRequest, otherUser));
        Assertions.assertEquals(errorMessage, exception.getMessage());
    }

    @Test
    void test_updateAppFailed_whenNameIllegal() {
        final String errorMessage = "应用名称不合法";
        AppUpdateRequest appUpdateRequest = new AppUpdateRequest();
        appUpdateRequest.setId(TEST_APP_ID);
        User testUser = User.builder().id(TEST_USER_ID).build();

        // mock 数据
        Mockito.when(appMapper.selectOneById(Mockito.any())).thenReturn(TEST_APP);

        // 测试空字符串
        appUpdateRequest.setName("");
        BusinessException exception1 = Assertions.assertThrows(BusinessException.class, () ->
                appService.updateApp(appUpdateRequest, testUser));
        Assertions.assertEquals(errorMessage, exception1.getMessage());

        // 测试null
        appUpdateRequest.setName(null);
        BusinessException exception2 = Assertions.assertThrows(BusinessException.class, () ->
                appService.updateApp(appUpdateRequest, testUser));
        Assertions.assertEquals(errorMessage, exception2.getMessage());

        // 测试长度过短
        appUpdateRequest.setName("a");
        BusinessException exception3 = Assertions.assertThrows(BusinessException.class, () ->
                appService.updateApp(appUpdateRequest, testUser));
        Assertions.assertEquals(errorMessage, exception3.getMessage());

        // 测试长度过长
        char[] charArray = new char[51];
        Arrays.fill(charArray, 'a');
        String longName = new String(charArray);
        appUpdateRequest.setName(longName);
        BusinessException exception4 = Assertions.assertThrows(BusinessException.class, () ->
                appService.updateApp(appUpdateRequest, testUser));
        Assertions.assertEquals(errorMessage, exception4.getMessage());
    }

    @Test
    void test_deleteAppFailed_whenIdIsNull() {
        final String errorMessage = "应用ID不合法";
        User testUser = User.builder().id(TEST_USER_ID).build();

        // 测试null
        BusinessException exception1 = Assertions.assertThrows(BusinessException.class, () ->
                appService.deleteApp(null, testUser));
        Assertions.assertEquals(errorMessage, exception1.getMessage());

        // 测试<=0
        BusinessException exception2 = Assertions.assertThrows(BusinessException.class, () ->
                appService.deleteApp(0L, testUser));
        Assertions.assertEquals(errorMessage, exception2.getMessage());

        BusinessException exception3 = Assertions.assertThrows(BusinessException.class, () ->
                appService.deleteApp(-1L, testUser));
        Assertions.assertEquals(errorMessage, exception3.getMessage());
    }

    @Test
    void test_deleteAppFailed_whenLoginUserIsNull() {
        final String errorMessage = "用户不存在";

        BusinessException exception = Assertions.assertThrows(BusinessException.class, () ->
                appService.deleteApp(TEST_APP_ID, null));
        Assertions.assertEquals(errorMessage, exception.getMessage());
    }

    @Test
    void test_deleteAppFailed_whenAppNotExist() {
        final String errorMessage = "应用不存在";
        User testUser = User.builder().id(TEST_USER_ID).build();

        BusinessException exception = Assertions.assertThrows(BusinessException.class, () ->
                appService.deleteApp(TEST_APP_ID + 1000, testUser)); // 不存在的应用ID
        Assertions.assertEquals(errorMessage, exception.getMessage());
    }

    @Test
    void test_deleteAppFailed_whenNoPermission() {
        final String errorMessage = "无权限";
        User otherUser = User.builder().id(TEST_USER_ID + 1000).build(); // 其他用户

        // mock 数据
        Mockito.when(appMapper.selectOneById(Mockito.any())).thenReturn(TEST_APP);

        BusinessException exception = Assertions.assertThrows(BusinessException.class, () ->
                appService.deleteApp(TEST_APP_ID, otherUser));
        Assertions.assertEquals(errorMessage, exception.getMessage());
    }

    @Test
    void test_getAppByIdFailed_whenIdIsNull() {
        final String errorMessage = "应用ID不合法";
        User testUser = User.builder().id(TEST_USER_ID).build();

        // 测试null
        BusinessException exception1 = Assertions.assertThrows(BusinessException.class, () ->
                appService.getAppById(null, testUser));
        Assertions.assertEquals(errorMessage, exception1.getMessage());

        // 测试<=0
        BusinessException exception2 = Assertions.assertThrows(BusinessException.class, () ->
                appService.getAppById(0L, testUser));
        Assertions.assertEquals(errorMessage, exception2.getMessage());

        BusinessException exception3 = Assertions.assertThrows(BusinessException.class, () ->
                appService.getAppById(-1L, testUser));
        Assertions.assertEquals(errorMessage, exception3.getMessage());
    }

    @Test
    void test_getAppByIdFailed_whenLoginUserIsNull() {
        final String errorMessage = "用户不存在";

        BusinessException exception = Assertions.assertThrows(BusinessException.class, () ->
                appService.getAppById(TEST_APP_ID, null));
        Assertions.assertEquals(errorMessage, exception.getMessage());
    }

    @Test
    void test_getAppByIdFailed_whenAppNotExist() {
        final String errorMessage = "应用不存在";
        User testUser = User.builder().id(TEST_USER_ID).build();

        BusinessException exception = Assertions.assertThrows(BusinessException.class, () ->
                appService.getAppById(TEST_APP_ID + 1000, testUser)); // 不存在的应用ID
        Assertions.assertEquals(errorMessage, exception.getMessage());
    }

    @Test
    void test_pageListAppsFailed_whenLoginUserIsNull() {
        final String errorMessage = "未登录";
        AppSearchRequest appSearchRequest = new AppSearchRequest();

        BusinessException exception = Assertions.assertThrows(BusinessException.class, () ->
                appService.pageListApps(appSearchRequest, null));
        Assertions.assertEquals(errorMessage, exception.getMessage());
    }

    @Test
    void test_pageListAppsFailed_whenInvalidPageParams() {
        final String errorMessage = "页码不合法";
        AppSearchRequest appSearchRequest = new AppSearchRequest();
        User testUser = User.builder().id(TEST_USER_ID).build();

        // 测试pageNum为null
        appSearchRequest.setPageNum(null);
        BusinessException exception1 = Assertions.assertThrows(BusinessException.class, () ->
                appService.pageListApps(appSearchRequest, testUser));
        Assertions.assertEquals(errorMessage, exception1.getMessage());

        // 测试pageNum<=0
        appSearchRequest.setPageNum(0);
        BusinessException exception2 = Assertions.assertThrows(BusinessException.class, () ->
                appService.pageListApps(appSearchRequest, testUser));
        Assertions.assertEquals(errorMessage, exception2.getMessage());

        appSearchRequest.setPageNum(-1);
        BusinessException exception3 = Assertions.assertThrows(BusinessException.class, () ->
                appService.pageListApps(appSearchRequest, testUser));
        Assertions.assertEquals(errorMessage, exception3.getMessage());
    }

    @Test
    void test_pageListAppsFailed_whenInvalidPageSize() {
        final String errorMessage = "每页条数不合法";
        AppSearchRequest appSearchRequest = new AppSearchRequest();
        appSearchRequest.setPageNum(1);
        User testUser = User.builder().id(TEST_USER_ID).build();

        // 测试pageSize为null
        appSearchRequest.setPageSize(null);
        BusinessException exception1 = Assertions.assertThrows(BusinessException.class, () ->
                appService.pageListApps(appSearchRequest, testUser));
        Assertions.assertEquals(errorMessage, exception1.getMessage());

        // 测试pageSize<=0
        appSearchRequest.setPageSize(0);
        BusinessException exception2 = Assertions.assertThrows(BusinessException.class, () ->
                appService.pageListApps(appSearchRequest, testUser));
        Assertions.assertEquals(errorMessage, exception2.getMessage());

        appSearchRequest.setPageSize(-1);
        BusinessException exception3 = Assertions.assertThrows(BusinessException.class, () ->
                appService.pageListApps(appSearchRequest, testUser));
        Assertions.assertEquals(errorMessage, exception3.getMessage());
    }

    @Test
    void test_pageListAppsFailed_whenInvalidSortOrder() {
        final String errorMessage = "排序方式不合法";
        AppSearchRequest appSearchRequest = new AppSearchRequest();
        appSearchRequest.setPageNum(1);
        appSearchRequest.setPageSize(10);
        User testUser = User.builder().id(TEST_USER_ID).build();

        appSearchRequest.setSortOrder("invalid");
        BusinessException exception = Assertions.assertThrows(BusinessException.class, () ->
                appService.pageListApps(appSearchRequest, testUser));
        Assertions.assertEquals(errorMessage, exception.getMessage());
    }

    @Test
    void test_adminUpdateAppFailed_whenIdIsNull() {
        final String errorMessage = "应用ID不合法";
        AppAdminUpdateRequest appAdminUpdateRequest = new AppAdminUpdateRequest();

        // 测试null
        appAdminUpdateRequest.setId(null);
        BusinessException exception1 = Assertions.assertThrows(BusinessException.class, () ->
                appService.adminUpdateApp(appAdminUpdateRequest));
        Assertions.assertEquals(errorMessage, exception1.getMessage());

        // 测试<=0
        appAdminUpdateRequest.setId(0L);
        BusinessException exception2 = Assertions.assertThrows(BusinessException.class, () ->
                appService.adminUpdateApp(appAdminUpdateRequest));
        Assertions.assertEquals(errorMessage, exception2.getMessage());

        appAdminUpdateRequest.setId(-1L);
        BusinessException exception3 = Assertions.assertThrows(BusinessException.class, () ->
                appService.adminUpdateApp(appAdminUpdateRequest));
        Assertions.assertEquals(errorMessage, exception3.getMessage());
    }

    @Test
    void test_adminUpdateAppFailed_whenAppNotExist() {
        final String errorMessage = "应用不存在";
        AppAdminUpdateRequest appAdminUpdateRequest = new AppAdminUpdateRequest();
        appAdminUpdateRequest.setId(TEST_APP_ID + 1000); // 不存在的应用ID

        BusinessException exception = Assertions.assertThrows(BusinessException.class, () ->
                appService.adminUpdateApp(appAdminUpdateRequest));
        Assertions.assertEquals(errorMessage, exception.getMessage());
    }

    @Test
    void test_adminUpdateAppFailed_whenNameTooShort() {
        final String errorMessage = "应用名称长度应在2~50之间";
        AppAdminUpdateRequest appAdminUpdateRequest = new AppAdminUpdateRequest();
        appAdminUpdateRequest.setId(TEST_APP_ID);
        appAdminUpdateRequest.setName("a"); // 长度为1

        // mock 数据
        Mockito.when(appMapper.selectOneById(Mockito.any())).thenReturn(TEST_APP);

        BusinessException exception = Assertions.assertThrows(BusinessException.class, () ->
                appService.adminUpdateApp(appAdminUpdateRequest));
        Assertions.assertEquals(errorMessage, exception.getMessage());
    }

    @Test
    void test_adminUpdateAppFailed_whenNameTooLong() {
        final String errorMessage = "应用名称长度应在2~50之间";
        AppAdminUpdateRequest appAdminUpdateRequest = new AppAdminUpdateRequest();
        appAdminUpdateRequest.setId(TEST_APP_ID);

        // mock 数据
        Mockito.when(appMapper.selectOneById(Mockito.any())).thenReturn(TEST_APP);

        // 创建51个字符的名称
        char[] charArray = new char[51];
        Arrays.fill(charArray, 'a');
        String longName = new String(charArray);
        appAdminUpdateRequest.setName(longName);

        BusinessException exception = Assertions.assertThrows(BusinessException.class, () ->
                appService.adminUpdateApp(appAdminUpdateRequest));
        Assertions.assertEquals(errorMessage, exception.getMessage());
    }

    @Test
    void test_adminUpdateAppFailed_whenCoverTooLong() {
        final String errorMessage = "应用封面地址过长";
        AppAdminUpdateRequest appAdminUpdateRequest = new AppAdminUpdateRequest();
        appAdminUpdateRequest.setId(TEST_APP_ID);

        // mock 数据
        Mockito.when(appMapper.selectOneById(Mockito.any())).thenReturn(TEST_APP);

        // 创建501个字符的封面地址
        char[] charArray = new char[501];
        Arrays.fill(charArray, 'a');
        String longCover = new String(charArray);
        appAdminUpdateRequest.setCover(longCover);

        BusinessException exception = Assertions.assertThrows(BusinessException.class, () ->
                appService.adminUpdateApp(appAdminUpdateRequest));
        Assertions.assertEquals(errorMessage, exception.getMessage());
    }

    @Test
    void test_adminDeleteAppFailed_whenIdIsNull() {
        final String errorMessage = "应用ID不合法";

        // 测试null
        BusinessException exception1 = Assertions.assertThrows(BusinessException.class, () ->
                appService.adminDeleteApp(null));
        Assertions.assertEquals(errorMessage, exception1.getMessage());

        // 测试<=0
        BusinessException exception2 = Assertions.assertThrows(BusinessException.class, () ->
                appService.adminDeleteApp(0L));
        Assertions.assertEquals(errorMessage, exception2.getMessage());

        BusinessException exception3 = Assertions.assertThrows(BusinessException.class, () ->
                appService.adminDeleteApp(-1L));
        Assertions.assertEquals(errorMessage, exception3.getMessage());
    }

    @Test
    void test_adminDeleteAppFailed_whenAppNotExist() {
        final String errorMessage = "应用不存在";

        BusinessException exception = Assertions.assertThrows(BusinessException.class, () ->
                appService.adminDeleteApp(TEST_APP_ID + 1000)); // 不存在的应用ID
        Assertions.assertEquals(errorMessage, exception.getMessage());
    }

    @Test
    void test_adminGetAppByIdFailed_whenIdIsNull() {
        final String errorMessage = "应用ID不合法";

        // 测试null
        BusinessException exception1 = Assertions.assertThrows(BusinessException.class, () ->
                appService.adminGetAppById(null));
        Assertions.assertEquals(errorMessage, exception1.getMessage());

        // 测试<=0
        BusinessException exception2 = Assertions.assertThrows(BusinessException.class, () ->
                appService.adminGetAppById(0L));
        Assertions.assertEquals(errorMessage, exception2.getMessage());

        BusinessException exception3 = Assertions.assertThrows(BusinessException.class, () ->
                appService.adminGetAppById(-1L));
        Assertions.assertEquals(errorMessage, exception3.getMessage());
    }

    @Test
    void test_adminGetAppByIdFailed_whenAppNotExist() {
        final String errorMessage = "应用不存在";

        BusinessException exception = Assertions.assertThrows(BusinessException.class, () ->
                appService.adminGetAppById(TEST_APP_ID + 1000)); // 不存在的应用ID
        Assertions.assertEquals(errorMessage, exception.getMessage());
    }

    @Test
    void test_adminPageListAppsFailed_whenInvalidPageParams() {
        final String errorMessage = "页码不合法";
        AppSearchRequest appSearchRequest = new AppSearchRequest();

        // 测试pageNum为null
        appSearchRequest.setPageNum(null);
        BusinessException exception1 = Assertions.assertThrows(BusinessException.class, () ->
                appService.adminPageListApps(appSearchRequest));
        Assertions.assertEquals(errorMessage, exception1.getMessage());

        // 测试pageNum<=0
        appSearchRequest.setPageNum(0);
        BusinessException exception2 = Assertions.assertThrows(BusinessException.class, () ->
                appService.adminPageListApps(appSearchRequest));
        Assertions.assertEquals(errorMessage, exception2.getMessage());

        appSearchRequest.setPageNum(-1);
        BusinessException exception3 = Assertions.assertThrows(BusinessException.class, () ->
                appService.adminPageListApps(appSearchRequest));
        Assertions.assertEquals(errorMessage, exception3.getMessage());
    }

    @Test
    void test_adminPageListAppsFailed_whenInvalidPageSize() {
        final String errorMessage = "每页条数不合法";
        AppSearchRequest appSearchRequest = new AppSearchRequest();
        appSearchRequest.setPageNum(1);

        // 测试pageSize为null
        appSearchRequest.setPageSize(null);
        BusinessException exception1 = Assertions.assertThrows(BusinessException.class, () ->
                appService.adminPageListApps(appSearchRequest));
        Assertions.assertEquals(errorMessage, exception1.getMessage());

        // 测试pageSize<=0
        appSearchRequest.setPageSize(0);
        BusinessException exception2 = Assertions.assertThrows(BusinessException.class, () ->
                appService.adminPageListApps(appSearchRequest));
        Assertions.assertEquals(errorMessage, exception2.getMessage());

        appSearchRequest.setPageSize(-1);
        BusinessException exception3 = Assertions.assertThrows(BusinessException.class, () ->
                appService.adminPageListApps(appSearchRequest));
        Assertions.assertEquals(errorMessage, exception3.getMessage());
    }

    @Test
    void test_adminPageListAppsFailed_whenInvalidSortOrder() {
        final String errorMessage = "排序方式不合法";
        AppSearchRequest appSearchRequest = new AppSearchRequest();
        appSearchRequest.setPageNum(1);
        appSearchRequest.setPageSize(10);

        appSearchRequest.setSortOrder("invalid");
        BusinessException exception = Assertions.assertThrows(BusinessException.class, () ->
                appService.adminPageListApps(appSearchRequest));
        Assertions.assertEquals(errorMessage, exception.getMessage());
    }
}
