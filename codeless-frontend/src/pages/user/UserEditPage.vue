<template>
  <a-form
    v-model="formState"
    v-bind="layout"
    name="nest-messages"
    @submit="handleSubmit"
  >
    <a-form-item name="userName" label="用户名" :rules="[{ validator: usernameValidator }]">
      <a-input v-model:value="formState.username" />
    </a-form-item>
    <a-form-item name="userAccount" label="账号">
      <span>{{loginUserStore.loginUser?.userAccount}}</span>
    </a-form-item>
    <a-form-item name="userIntro" label="用户简介">
      <a-textarea v-model:value="formState.userIntro" />
    </a-form-item>
    <a-form-item name="avatarUrl" label="用户头像">
      <a-image :src="loginUserStore.loginUser?.avatarUrl" :height="64"/>
    </a-form-item>
    <a-form-item name="gender" label="性别">
      <a-select v-model:value="formState.gender" allow-clear :first-active-value="formState.gender">
        <a-select-option :key="0">未知</a-select-option>
        <a-select-option :key="1">男</a-select-option>
        <a-select-option :key="2">女</a-select-option>
      </a-select>
    </a-form-item>
    <a-form-item name="userRole" label="用户角色">
      <span>{{loginUserStore.loginUser?.userRole === 0 ? '用户' : '管理员'}}</span>
    </a-form-item>
    <a-form-item :wrapper-col="{ ...layout.wrapperCol, offset: 18 }">
      <a-button type="primary" html-type="submit">提交信息</a-button>
    </a-form-item>
  </a-form>
</template>
<script lang="ts" setup>
import { reactive } from 'vue';
import { useLoginUserStore } from '@/stores/loginUser.ts'
import { updateUser } from '@/api/user.ts'
import { message } from 'ant-design-vue'
import { usernameValidator } from '@/validator.ts'
import { useRouter } from 'vue-router'
const layout = {
  labelCol: { span: 4 },
  wrapperCol: { span: 16 },
};
const router = useRouter();

const loginUserStore = useLoginUserStore();

const formState = reactive<API.LoginUserVo>({
  ...loginUserStore.loginUser
});

const handleSubmit = async () => {
  const res = await updateUser(formState);
  if (res.data.code === 200) {
    message.success('修改信息成功!');
    router.replace('/user/home');
  } else {
    message.error('操作失败, ' + res.data.message);
  }
};
</script>
