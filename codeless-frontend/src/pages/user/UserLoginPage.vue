<template>
  <div id="userLoginPage">
    <h2 class="title">CodeLess - 用户登录</h2>
    <span class="slogan">无需代码，使用对话生成应用</span>
    <a-form
      :model="formState"
      autocomplete="off"
      @finish="handleSubmit"
    >
      <a-form-item
        name="userAccount"
        :rules="[
          {validator: accountValidator,}
          ]"
      >
        <a-input v-model:value="formState.userAccount" placeholder="请输入账号" size="large">
          <template #prefix><UserOutlined style="color: rgba(0, 0, 0, 0.25)" /></template>
        </a-input>
      </a-form-item>
      <a-form-item
        name="password"
        :rules="[
          {validator: passwordValidator,},
          ]"
      >
        <a-input-password v-model:value="formState.password" placeholder="请输入密码" size="large">
          <template #prefix><LockOutlined style="color: rgba(0, 0, 0, 0.25)" /></template>
        </a-input-password>
      </a-form-item>
      <span class="regTip">没有账号? <a href="/user/register">去注册</a></span>
      <a-form-item>
        <a-button
          type="primary"
          size="large"
          block
          html-type="submit"
          :disabled="formState.userAccount === '' || formState.password === ''"
        >
          登录
        </a-button>
      </a-form-item>
    </a-form>
  </div>
</template>
<script lang="ts" setup>
import { reactive } from 'vue';
import { UserOutlined, LockOutlined } from '@ant-design/icons-vue';
import type { FormProps } from 'ant-design-vue';
import { message } from 'ant-design-vue';
import { userLogin } from '@/api/user.ts'
import { useLoginUserStore } from '@/stores/loginUser.ts'
import { useRouter } from 'vue-router'
import { accountValidator, passwordValidator } from '@/validator.ts'


const formState = reactive<API.UserLoginRequest>({
  userAccount: '',
  password: '',
});
const router = useRouter();
const loginUserStore = useLoginUserStore();
const handleSubmit: FormProps['onFinish'] = async values => {
  const res = await userLogin(values);
  // 登录成功
  if (res.data.code === 200 && res.data.data) {
    await loginUserStore.fetchLoginUser();
    message.success('登录成功');
    router.replace({ path: '/' });
  } else {
    message.error('登录失败, ' + res.data.message);
  }
};
</script>

<style>
#userLoginPage {
  width: 320px;
}
.title {
  text-align: center;
  height: 48px;
  line-height: 48px;
  font-weight: 600;
}
.slogan {
  display: block;
  text-align: center;
  color: #aaaaaa;
  margin-bottom:  16px;
}
.regTip {
  display: block;
  text-align: right;
  margin-bottom: 16px;
  color: #aaaaaa;
}
</style>

