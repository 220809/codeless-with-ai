<template>
  <a-modal
    v-model:open="visible"
    title="应用详情"
    :footer="null"
    width="500px"
    @update:open="handleVisibleChange"
  >
    <div class="app-detail-content">
      <!-- 应用基础信息 -->
      <div class="app-info-section">
        <div class="info-item">
          <span class="info-label">创建者：</span>
          <div class="creator-info">
            <a-avatar
              v-if="appData.user?.avatarUrl"
              :src="appData.user.avatarUrl"
              :size="32"
            />
            <a-avatar v-else :size="32">{{ appData.user?.username?.[0] || 'U' }}</a-avatar>
            <span class="creator-name">{{ appData.user?.username || '未知用户' }}</span>
          </div>
        </div>
        <div class="info-item">
          <span class="info-label">创建时间：</span>
          <span class="info-value">{{ formatCreateTime(appData.createTime) }}</span>
        </div>
      </div>

      <!-- 操作栏（仅本人或管理员可见） -->
      <div v-if="canEditOrAdmin" class="action-section">
        <a-divider />
        <div class="action-buttons">
          <a-button type="primary" @click="toggleEditForm">
            {{ editFormVisible ? '收起' : '修改' }}
          </a-button>
          <a-button danger @click="handleDelete" :loading="deleting">
            删除
          </a-button>
        </div>
      </div>

      <!-- 编辑表单区域 -->
      <div v-if="canEditOrAdmin && editFormVisible" class="edit-form-section">
        <a-divider />
        <a-form :model="editFormState" layout="vertical" @finish="handleSubmitEdit">
          <a-form-item
            name="name"
            label="应用名称"
            :rules="[{ required: true, message: '请输入应用名称' }]"
          >
            <a-input v-model:value="editFormState.name" placeholder="请输入应用名称" />
          </a-form-item>
          <a-form-item
            v-if="isAdmin"
            name="cover"
            label="应用封面"
          >
            <a-input v-model:value="editFormState.cover" placeholder="请输入封面图片URL" />
            <div v-if="editFormState.cover" style="margin-top: 8px">
              <a-image :src="editFormState.cover" :height="120" />
            </div>
          </a-form-item>
          <a-form-item
            v-if="isAdmin"
            name="priority"
            label="优先级"
          >
            <a-input-number
              v-model:value="editFormState.priority"
              :min="0"
              :max="100"
              style="width: 100%"
              placeholder="优先级（0-100，99为精选）"
            />
            <div style="margin-top: 4px; color: #999; font-size: 12px">
              提示：优先级设置为99时，应用将显示在精选列表中
            </div>
          </a-form-item>
          <a-form-item>
            <a-space>
              <a-button type="primary" html-type="submit" :loading="submitting">
                提交修改
              </a-button>
              <a-button @click="handleCancelEdit">
                取消
              </a-button>
            </a-space>
          </a-form-item>
        </a-form>
      </div>
    </div>
  </a-modal>
</template>

<script setup lang="ts">
import { ref, computed, reactive, watch } from 'vue'
import { message, Modal } from 'ant-design-vue'
import { updateApp, adminUpdateApp, deleteApp, adminDeleteApp } from '@/api/app.ts'
import { formatDateTime } from '@/utils/date.ts'
import { isAdmin as checkIsAdmin, canEditApp } from '@/utils/helpers.ts'
import { useLoginUserStore } from '@/stores/loginUser.ts'
import { useRouter } from 'vue-router'

const props = defineProps<{
  appData: API.AppVo
  visible: boolean
}>()

const emits = defineEmits<{
  'update:visible': [visible: boolean]
  'update:app': [app: API.AppVo]
  'deleted': []
}>()

const router = useRouter()
const loginUserStore = useLoginUserStore()

const visible = computed({
  get: () => props.visible,
  set: (val) => emits('update:visible', val)
})

const isAdmin = computed(() => checkIsAdmin(loginUserStore.loginUser.userRole))
const canEditOrAdmin = computed(() => 
  canEditApp(props.appData.userId, loginUserStore.loginUser.id, loginUserStore.loginUser.userRole)
)

const editFormVisible = ref(false)
const submitting = ref(false)
const deleting = ref(false)
const editFormState = reactive<{
  name?: string
  cover?: string
  priority?: number
}>({
  name: '',
  cover: '',
  priority: 0,
})

const formatCreateTime = formatDateTime

const handleVisibleChange = (val: boolean) => {
  if (val) {
    // 打开时初始化编辑表单数据
    editFormState.name = props.appData.name || ''
    editFormState.cover = props.appData.cover || ''
    editFormState.priority = props.appData.priority ?? 0
    editFormVisible.value = false
  }
}

watch(() => props.visible, (val) => {
  if (!val) {
    editFormVisible.value = false
  }
})

const toggleEditForm = () => {
  editFormVisible.value = !editFormVisible.value
  if (editFormVisible.value) {
    // 展开时重置表单数据为当前应用数据
    editFormState.name = props.appData.name || ''
    editFormState.cover = props.appData.cover || ''
    editFormState.priority = props.appData.priority ?? 0
  }
}

const handleSubmitEdit = async () => {
  if (!props.appData.id) {
    message.error('应用ID不存在')
    return
  }

  submitting.value = true
  try {
    let res
    if (isAdmin.value) {
      res = await adminUpdateApp({
        id: props.appData.id as any,
        name: editFormState.name,
        cover: editFormState.cover,
        priority: editFormState.priority,
      })
    } else {
      res = await updateApp({
        id: props.appData.id as any,
        name: editFormState.name,
      })
    }

    if ((res.data.code === 200 || res.data.code === 0) && res.data.data) {
      message.success('修改信息成功!')
      // 通知父组件更新应用数据
      emits('update:app', {
        ...props.appData,
        name: editFormState.name,
        cover: editFormState.cover,
        priority: editFormState.priority,
      })
      editFormVisible.value = false
    } else {
      message.error('操作失败, ' + (res.data.message || '未知错误'))
    }
  } catch (error: any) {
    message.error('操作失败: ' + (error.message || '网络错误'))
  } finally {
    submitting.value = false
  }
}

const handleCancelEdit = () => {
  editFormState.name = props.appData.name || ''
  editFormState.cover = props.appData.cover || ''
  editFormState.priority = props.appData.priority ?? 0
  editFormVisible.value = false
}

const handleDelete = () => {
  if (!props.appData.id) {
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
        if (isAdmin.value) {
          res = await adminDeleteApp({ id: props.appData.id! as any })
        } else {
          res = await deleteApp({ id: props.appData.id! as any })
        }

        if ((res.data.code === 200 || res.data.code === 0) && res.data.data) {
          message.success('删除成功!')
          visible.value = false
          emits('deleted')
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
</script>

<style scoped>
.app-detail-content {
  padding: 8px 0;
}

.app-info-section {
  margin-bottom: 16px;
}

.info-item {
  display: flex;
  align-items: center;
  margin-bottom: 16px;
  font-size: 14px;
}

.info-item:last-child {
  margin-bottom: 0;
}

.info-label {
  width: 80px;
  color: #8c8c8c;
  font-weight: 500;
  flex-shrink: 0;
}

.info-value {
  color: #262626;
  flex: 1;
}

.creator-info {
  display: flex;
  align-items: center;
  gap: 8px;
  flex: 1;
}

.creator-name {
  color: #262626;
  font-weight: 500;
}

.action-section {
  margin-top: 8px;
}

.action-buttons {
  display: flex;
  gap: 12px;
  justify-content: flex-end;
}

.edit-form-section {
  margin-top: 16px;
}

.edit-form-section :deep(.ant-form-item) {
  margin-bottom: 16px;
}

.edit-form-section :deep(.ant-form-item-label > label) {
  font-weight: 500;
  color: #595959;
}
</style>

