<template>
  <div id="appEditPage">
    <a-spin :spinning="loading">
      <a-form v-model="formState" v-bind="layout" name="app-edit-form" @submit="handleSubmit">
        <a-form-item name="name" label="应用名称" :rules="[{ required: true, message: '请输入应用名称' }]">
          <a-input v-model:value="formState.name" :disabled="!canEdit" />
        </a-form-item>
        <a-form-item name="cover" label="应用封面" v-if="isAdmin">
          <a-input v-model:value="formState.cover" placeholder="请输入封面图片URL" />
          <div v-if="formState.cover" style="margin-top: 8px">
            <a-image :src="formState.cover" :height="120" />
          </div>
        </a-form-item>
        <a-form-item name="priority" label="优先级" v-if="isAdmin">
          <a-input-number 
            v-model:value="formState.priority" 
            :min="0" 
            :max="100"
            style="width: 100%"
            placeholder="优先级（0-100，99为精选）"
          />
          <div style="margin-top: 4px; color: #999; font-size: 12px">
            提示：优先级设置为99时，应用将显示在精选列表中
          </div>
        </a-form-item>
        <a-form-item name="id" label="应用ID" v-if="isAdmin">
          <span>{{ formState.id }}</span>
        </a-form-item>
        <a-form-item name="initialPrompt" label="初始提示词" v-if="isAdmin">
          <a-textarea v-model:value="formState.initialPrompt" :rows="4" disabled />
        </a-form-item>
        <a-form-item name="genFileType" label="文件类型" v-if="isAdmin">
          <span>{{ formState.genFileType || '-' }}</span>
        </a-form-item>
        <a-form-item name="user" label="创建者" v-if="isAdmin">
          <a-space>
            <a-avatar v-if="formState.user?.avatarUrl" :src="formState.user.avatarUrl" :size="24" />
            <span>{{ formState.user?.username || '-' }}</span>
          </a-space>
        </a-form-item>
        <a-form-item name="createTime" label="创建时间" v-if="isAdmin">
          <span>{{ formState.createTime ? dayjs(formState.createTime).format('YYYY-MM-DD HH:mm:ss') : '-' }}</span>
        </a-form-item>
        <a-form-item :wrapper-col="{ ...layout.wrapperCol, offset: 4 }">
          <a-space>
            <a-button type="primary" html-type="submit" :loading="submitting">提交信息</a-button>
            <a-button @click="handleCancel">取消</a-button>
            <a-button 
              v-if="canEdit" 
              danger 
              @click="handleDelete" 
              :loading="deleting"
            >
              删除应用
            </a-button>
          </a-space>
        </a-form-item>
      </a-form>
    </a-spin>
  </div>
</template>
<script lang="ts" setup>
import { reactive, ref, onMounted, computed } from 'vue'
import { updateApp, getAppById, adminGetAppById, adminUpdateApp, deleteApp, adminDeleteApp } from '@/api/app.ts'
import { message, Modal } from 'ant-design-vue'
import { useRouter, useRoute } from 'vue-router'
import { useLoginUserStore } from '@/stores/loginUser.ts'
import dayjs from 'dayjs'

const layout = {
  labelCol: { span: 4 },
  wrapperCol: { span: 16 },
}

const router = useRouter()
const route = useRoute()
const loginUserStore = useLoginUserStore()

const loading = ref(false)
const submitting = ref(false)
const deleting = ref(false)
const appData = ref<API.AppVo>({})

const isAdmin = computed(() => loginUserStore.loginUser.userRole === 1)
const canEdit = computed(() => {
  if (isAdmin.value) return true
  // 普通用户只能编辑自己的应用
  return appData.value.userId === loginUserStore.loginUser.id
})

const formState = reactive<{
  id?: string | number
  name?: string
  cover?: string
  priority?: number
  initialPrompt?: string
  genFileType?: string
  user?: API.LoginUserVo
  createTime?: string
}>({
  id: undefined,
  name: '',
  cover: '',
  priority: 0,
  initialPrompt: '',
  genFileType: '',
  user: undefined,
  createTime: '',
})

const fetchAppData = async () => {
  const appId = route.query.id as string
  if (!appId) {
    message.error('缺少应用ID')
    router.back()
    return
  }

  loading.value = true
  try {
    let res
    // 保持id为string类型，避免精度丢失
    const idParam = appId as any
    if (isAdmin.value) {
      res = await adminGetAppById({ id: idParam })
    } else {
      res = await getAppById({ id: idParam })
    }

    if ((res.data.code === 200 || res.data.code === 0) && res.data.data) {
      appData.value = res.data.data
      
      // 检查权限
      if (!isAdmin.value && appData.value.userId !== loginUserStore.loginUser.id) {
        message.error('您没有权限编辑此应用')
        router.back()
        return
      }

      // 填充表单数据，保持id为原始类型（可能是string或number）
      formState.id = appData.value.id as any
      formState.name = appData.value.name || ''
      formState.cover = appData.value.cover || ''
      formState.priority = appData.value.priority ?? 0
      formState.initialPrompt = appData.value.initialPrompt || ''
      formState.genFileType = appData.value.genFileType || ''
      formState.user = appData.value.user
      formState.createTime = appData.value.createTime
    } else {
      message.error('获取应用信息失败: ' + (res.data.message || '未知错误'))
      router.back()
    }
  } catch (error: any) {
    message.error('获取应用信息失败: ' + (error.message || '网络错误'))
    router.back()
  } finally {
    loading.value = false
  }
}

const handleSubmit = async () => {
  if (!formState.id) {
    message.error('应用ID不存在')
    return
  }

  submitting.value = true
  try {
    let res
    if (isAdmin.value) {
      // 管理员使用adminUpdateApp接口，保持id为原始类型
      res = await adminUpdateApp({
        id: formState.id as any,
        name: formState.name,
        cover: formState.cover,
        priority: formState.priority,
      })
    } else {
      // 普通用户使用updateApp接口，只能修改名称，保持id为原始类型
      res = await updateApp({
        id: formState.id as any,
        name: formState.name,
      })
    }

    if ((res.data.code === 200 || res.data.code === 0) && res.data.data) {
      message.success('修改信息成功!')
      router.back()
    } else {
      message.error('操作失败, ' + (res.data.message || '未知错误'))
    }
  } catch (error: any) {
    message.error('操作失败: ' + (error.message || '网络错误'))
  } finally {
    submitting.value = false
  }
}

const handleCancel = () => {
  router.back()
}

const handleDelete = () => {
  if (!formState.id) {
    message.error('应用ID不存在')
    return
  }

  Modal.confirm({
    title: '确认删除',
    content: '确定要删除此应用吗？此操作不可恢复。',
    okText: '确认',
    cancelText: '取消',
    onOk: async () => {
      deleting.value = true
      try {
        let res
        // 保持id为原始类型，避免精度丢失
        if (isAdmin.value) {
          res = await adminDeleteApp({ id: formState.id! as any })
        } else {
          res = await deleteApp({ id: formState.id! as any })
        }

        if ((res.data.code === 200 || res.data.code === 0) && res.data.data) {
          message.success('删除成功!')
          router.back()
        } else {
          message.error('删除失败, ' + (res.data.message || '未知错误'))
        }
      } catch (error: any) {
        message.error('删除失败: ' + (error.message || '网络错误'))
      } finally {
        deleting.value = false
      }
    },
  })
}

onMounted(() => {
  fetchAppData()
})
</script>
<style>
#appEditPage {
  max-width: 800px;
  margin: 24px auto;
  padding: 24px;
  background: #fff;
  border-radius: 8px;
}
</style>
