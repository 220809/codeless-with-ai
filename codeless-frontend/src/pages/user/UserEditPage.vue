<template>
  <div class="user-edit-wrapper">
    <a-form :model="formState" v-bind="layout" name="nest-messages" @finish="handleSubmit" class="user-edit-form">
    <a-form-item name="username" label="用户名" :rules="[{ validator: usernameValidator }]">
      <a-input v-model:value="formState.username" />
    </a-form-item>
    <a-form-item name="userAccount" label="账号" :rules="[{validator: accountValidator}]">
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
    <a-form-item :wrapper-col="{ ...layout.wrapperCol, offset: 4 }">
      <a-space>
        <a-button type="primary" html-type="submit">提交信息</a-button>
        <a-button @click="handleCancel">取消</a-button>
      </a-space>
    </a-form-item>
    </a-form>
  </div>
</template>
<script lang="ts" setup>
import { reactive } from 'vue'
import { updateUser } from '@/api/user.ts'
import { message } from 'ant-design-vue'
import { accountValidator, usernameValidator } from '@/validator.ts'
import { useLoginUserStore } from '@/stores/loginUser.ts'

const layout = {
  labelCol: { span: 4 },
  wrapperCol: { span: 16 },
}

const props = defineProps<{
  editUser?: API.LoginUserVo
}>();

const emits = defineEmits(['closeModal']);

const loginUserStore = useLoginUserStore()

const formState = reactive<API.LoginUserVo>({
  ...props.editUser,
})

const handleSubmit = async (values: any) => {
  console.log(1)
  const res = await updateUser({
    id: formState.id,
    ...values,
  })
  if (res.data.code === 200) {
    message.success('修改信息成功!')
    emits('closeModal');
  } else {
    message.error('操作失败, ' + res.data.message)
  }
}

const handleCancel = () => {
  emits('closeModal');
}
</script>
<style scoped>
.user-edit-wrapper {
  padding: 8px 0;
}

.user-edit-form {
  max-width: 100%;
}

:deep(.ant-form-item-label > label) {
  font-weight: 500;
  color: #595959;
}

:deep(.ant-form-item) {
  margin-bottom: 24px;
}

:deep(.ant-input),
:deep(.ant-select-selector),
:deep(.ant-input-number-input) {
  border-radius: 4px;
}

:deep(.ant-btn) {
  border-radius: 4px;
  height: 36px;
  padding: 0 20px;
  font-weight: 500;
}

:deep(.ant-image) {
  border-radius: 4px;
  border: 1px solid #f0f0f0;
}
</style>
