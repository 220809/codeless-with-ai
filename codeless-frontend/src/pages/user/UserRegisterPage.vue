<template>
  <div id="userRegisterPage">
    <h2 class="title">CodeLess - 用户注册</h2>
    <span class="slogan">无需代码，使用对话生成应用</span>
    <a-form :model="formState" autocomplete="off" @finish="handleSubmit">
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
      <a-form-item
        name="checkedPassword"
        :rules="[
          { required: true, message: '请再次输入密码' },
          { validator: validatePasswordMatch },
        ]"
      >
        <a-input-password
          v-model:value="formState.checkedPassword"
          placeholder="请再次输入密码"
          size="large"
        >
          <template #prefix><LockOutlined style="color: rgba(0, 0, 0, 0.25)" /></template>
        </a-input-password>
      </a-form-item>
      <span class="regTip">已有账号? <a href="/user/login">去登录</a></span>
      <a-form-item>
        <a-button
          type="primary"
          size="large"
          block
          html-type="submit"
          :disabled="
            formState.userAccount === '' ||
            formState.password === '' ||
            formState.checkedPassword === ''
          "
        >
          注册
        </a-button>
      </a-form-item>
    </a-form>
  </div>
</template>
<script lang="ts" setup>
import { reactive } from 'vue'
import { UserOutlined, LockOutlined } from '@ant-design/icons-vue'
import type { FormProps, Rule } from 'ant-design-vue'
import { message } from 'ant-design-vue'
import { userRegister } from '@/api/user.ts'
import { useRouter } from 'vue-router'
import { accountValidator, passwordValidator } from '@/validator.ts'

const formState = reactive<API.UserRegisterRequest>({
  userAccount: '',
  password: '',
  checkedPassword: '',
})
const router = useRouter();

const validatePasswordMatch = async (_rule: Rule, value: string) => {
  if (!value) {
    return Promise.reject('请再次输入密码')
  }
  if (value !== formState.password) {
    return Promise.reject('两次输入的密码不一致')
  }
  return Promise.resolve()
}

const handleSubmit: FormProps['onFinish'] = async (values) => {
  const res = await userRegister(values)
  // 注册成功
  if (res.data.code === 200) {
    message.success('注册成功，请登录')
    router.replace({ path: '/user/login' })
  } else {
    message.error('注册失败, ' + res.data.message)
  }
}
</script>

<style>
#userRegisterPage {
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
  margin-bottom: 16px;
}
.regTip {
  display: block;
  text-align: right;
  margin-bottom: 16px;
  color: #aaaaaa;
}
</style>
