<template>
  <a-form :model="formState" v-bind="layout" name="nest-messages" @finish="handleSubmit">
    <a-form-item name="username" label="用户名" :rules="[{ validator: usernameValidator }]">
      <a-input v-model:value="formState.username" />
    </a-form-item>
    <a-form-item name="userAccount" label="账号">
      <a-input
        v-model:value="formState.userAccount"
        v-if="loginUserStore.loginUser.userRole === 1"
      />
      <span v-else>{{ editUser.userAccount }}</span>
    </a-form-item>
    <a-form-item name="userIntro" label="用户简介">
      <a-textarea v-model:value="formState.userIntro" />
    </a-form-item>
    <a-form-item name="avatarUrl" label="用户头像">
      <a-image :src="formState?.avatarUrl" :height="64" />
    </a-form-item>
    <a-form-item name="gender" label="性别">
      <a-select
        v-model:value="formState.gender"
        allow-clear
        :first-active-value="formState?.gender"
      >
        <a-select-option :key="0">未知</a-select-option>
        <a-select-option :key="1">男</a-select-option>
        <a-select-option :key="2">女</a-select-option>
      </a-select>
    </a-form-item>
    <a-form-item name="userRole" label="用户角色">
      <a-select
        v-model:value="formState.userRole"
        allow-clear
        :first-active-value="formState?.userRole"
        v-if="loginUserStore.loginUser.userRole === 1"
      >
        <a-select-option :key="0">用户</a-select-option>
        <a-select-option :key="1">管理员</a-select-option>
      </a-select>
      <span v-else>{{ formState?.userRole === 0 ? '用户' : '管理员' }}</span>
    </a-form-item>
    <a-form-item :wrapper-col="{ ...layout.wrapperCol, offset: 18 }">
      <a-button type="primary" html-type="submit">提交信息</a-button>
    </a-form-item>
  </a-form>
</template>
<script lang="ts" setup>
import { reactive } from 'vue'
import { updateUser } from '@/api/user.ts'
import { message } from 'ant-design-vue'
import { usernameValidator } from '@/validator.ts'
import { useRouter } from 'vue-router'
import { useLoginUserStore } from '@/stores/loginUser.ts'

const layout = {
  labelCol: { span: 4 },
  wrapperCol: { span: 16 },
}

const props = defineProps<{
  editUser: API.LoginUserVo
}>()
const router = useRouter()

const loginUserStore = useLoginUserStore()

const formState = reactive<API.LoginUserVo>({
  ...props.editUser,
})

const handleSubmit = async (values: any) => {
  const res = await updateUser({
    id: formState.id,
    ...values,
  })
  if (res.data.code === 200) {
    message.success('修改信息成功!')
    if (window.location.pathname === '/user/edit') {
      router.replace('/user/home')
    }
  } else {
    message.error('操作失败, ' + res.data.message)
  }
}
</script>
