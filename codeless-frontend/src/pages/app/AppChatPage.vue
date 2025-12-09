<template>
  <div id="appChatPage">
    <!-- 顶部栏 -->
    <div class="top-bar">
      <div class="top-bar-left">
        <h2 class="app-name">{{ appData.name || '未命名应用' }}</h2>
      </div>
      <div class="top-bar-right">
        <a-button @click="showAppDetailModal">
          应用详情
        </a-button>
        <a-button
          type="primary"
          :loading="deploying"
          :disabled="!canDeploy"
          @click="handleDeploy"
        >
          部署
        </a-button>
      </div>
    </div>

    <!-- 核心内容区域 -->
    <div class="content-area">
      <!-- 左侧对话区域 -->
      <div class="chat-area">
        <!-- 消息区域 -->
        <div class="messages-container" ref="messagesContainerRef">
          <div
            v-for="(msg, index) in messages"
            :key="index"
            :class="['message-item', msg.type === 'user' ? 'user-message' : 'ai-message']"
          >
            <div class="message-content">
              <div class="message-text" v-html="formatMessage(msg.content)"></div>
            </div>
          </div>
          <div class="message-item ai-message" v-if="streaming">
            <div class="message-content">
              {{  'AI 正在卖命生成中'  }}
              <a-spin :spinning="streaming" />
            </div>
          </div>
        </div>

        <!-- 用户消息输入框 -->
        <div class="input-area">
          <a-tooltip :title="!canEdit ? '无法在别人的作品下对话哦~' : ''" placement="top">
            <a-textarea
              v-model:value="userInput"
              :placeholder="canEdit ? '输入你的消息...' : '无法在别人的作品下对话哦~'"
              :auto-size="{ minRows: 2, maxRows: 6 }"
              :disabled="streaming || !appData.id || !canEdit"
              @keydown.enter.ctrl="handleSendMessage"
              @keydown.enter.exact.prevent="handleSendMessage"
            />
          </a-tooltip>
          <div class="input-actions">
            <a-button
              type="primary"
              :loading="streaming"
              :disabled="!userInput.trim() || !appData.id || !canEdit"
              @click="handleSendMessage"
            >
              发送
            </a-button>
          </div>
        </div>
      </div>

      <!-- 右侧网页展示区域 -->
      <div class="preview-area">
        <div v-if="!previewReady" class="preview-placeholder">
          <a-spin :spinning="!previewReady">
            <template #tip>
              {{ 'AI 生成完成后方可预览' }}
            </template>
          </a-spin>
        </div>
        <iframe
          v-else
          :src="previewUrl"
          class="preview-iframe"
          frameborder="0"
        />
      </div>
    </div>

    <!-- 部署模态框 -->
    <a-modal
      v-model:open="deployModalVisible"
      title="应用部署成功"
      :footer="null"
      width="600px"
    >
      <div class="deploy-modal-content">
        <div class="deploy-url-section">
          <div class="deploy-url-label">部署地址：</div>
          <div class="deploy-url-container">
            <a-input
              :value="deployUrl"
              readonly
              class="deploy-url-input"
            />
            <a-button
              type="primary"
              @click="handleCopyUrl"
              class="copy-btn"
            >
              复制
            </a-button>
          </div>
        </div>
        <div class="deploy-actions">
          <a-button
            type="primary"
            size="large"
            block
            @click="handleVisitUrl"
          >
            前往访问
          </a-button>
        </div>
      </div>
    </a-modal>

    <!-- 应用详情模态框 -->
    <a-modal
      v-model:open="appDetailModalVisible"
      title="应用详情"
      :footer="null"
      width="500px"
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
            <a-button danger @click="handleDeleteApp">
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
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted, nextTick, computed, reactive, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { message, Modal } from 'ant-design-vue'
import { getAppById, genCodeFromChat, deployApp, deleteApp, adminDeleteApp, updateApp, adminUpdateApp } from '@/api/app.ts'
import request from '@/request.ts'
import { useLoginUserStore } from '@/stores/loginUser.ts'
import dayjs from 'dayjs'

const route = useRoute()
const router = useRouter()

// 应用数据
const appData = ref<API.AppVo>({})
const loading = ref(false)

// 消息列表
interface Message {
  type: 'user' | 'ai'
  content: string,
  loading?: boolean,
}

const messages = ref<Message[]>([])
const userInput = ref('')
const streaming = ref(false)
const currentAiMessage = ref('')
let abortController: AbortController | null = null

// 预览相关
const previewReady = ref(false)
const previewUrl = ref('')
const canDeploy = computed(() => previewReady.value && appData.value.genFileType)

const loginUserStore = useLoginUserStore()

// 权限检查：是否是自己的作品
const canEdit = computed(() => {
  if (!appData.value.userId || !loginUserStore.loginUser.id) {
    return false
  }
  return appData.value.userId === loginUserStore.loginUser.id
})

// 权限检查：是否是本人或管理员
const canEditOrAdmin = computed(() => {
  const isAdmin = loginUserStore.loginUser.userRole === 1
  return canEdit.value || isAdmin
})

// 是否是管理员
const isAdmin = computed(() => loginUserStore.loginUser.userRole === 1)

// 编辑表单相关
const editFormVisible = ref(false)
const submitting = ref(false)
const editFormState = reactive<{
  name?: string
  cover?: string
  priority?: number
}>({
  name: '',
  cover: '',
  priority: 0,
})

const appId = ref<any>();
// 消息容器引用（用于自动滚动）
const messagesContainerRef = ref<HTMLElement>()

// 获取应用信息
const fetchAppData = async () => {
  const id = route.query.id as string
  if (!id) {
    message.error('缺少应用ID')
    router.back()
    return
  }
  appId.value = id;

  loading.value = true
  try {
    const res = await getAppById({ id: appId.value as any })
    if (res.data.code === 200 && res.data.data) {
      appData.value = res.data.data

      if (messages.value.length >= 2) {
        showPreview();
      }

      // 检查是否有view参数，如果有则不自动发送消息
      // 目前使用 viewParam 防止查看对话时发送ai消息
      const viewParam = route.query.view
      if (!viewParam && appData.value.initialPrompt && messages.value.length === 0) {
        await sendInitialMessage(appData.value.initialPrompt)
      }
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

// 发送消息
const handleSendMessage = async () => {
  if (!userInput.value.trim() || !appData.value.id) return
  await sendMessage(userInput.value.trim())
  userInput.value = ''
}

const sendInitialMessage = async (prompt: string) => {
  // 添加用户消息
  messages.value.push({
    type: 'user',
    content: prompt,
  })

  // 添加AI消息占位符
  const aiMessageIndex = messages.value.length
  messages.value.push({
    type: 'ai',
    content: '',
    loading: true,
  })

  await nextTick()
  scrollToBottom()

  // 开始生成
  streaming.value = true
  await generateCode(prompt, aiMessageIndex)
}

// 发送消息并处理SSE流
const sendMessage = async (content: string) => {
  if (!content.trim() || streaming.value) return;

  // 添加用户消息
  messages.value.push({
    type: 'user',
    content: content,
  })

  // 添加AI消息占位符
  const aiMessageIndex = messages.value.length
  messages.value.push({
    type: 'ai',
    content: '',
    loading: true,
  })

  // 滚动到底部
  await scrollToBottom()

  // 开始流式接收
  streaming.value = true
  await generateCode(content, aiMessageIndex);
}

// 生成代码 - 使用 EventSource 处理流式响应
const generateCode = async (userMessage: string, aiMessageIndex: number) => {
  let eventSource: EventSource | null = null
  let streamCompleted = false

  try {
    // 获取 axios 配置的 baseURL
    const baseURL = request.defaults.baseURL;

    // 构建URL参数
    const params = new URLSearchParams({
      appId: appId.value || '',
      userMessage,
    })

    const url = `${baseURL}/app/chat/codegen?${params}`

    // 创建 EventSource 连接
    eventSource = new EventSource(url, {
      withCredentials: true,
    })

    let fullContent = ''

    // 处理接收到的消息
    eventSource.onmessage = function (event) {
      if (streamCompleted) return

      try {
        // 解析JSON包装的数据
        const parsed = JSON.parse(event.data)
        const content = parsed.r

        // 拼接内容
        if (content !== undefined && content !== null) {
          fullContent += content
          messages.value[aiMessageIndex].content = fullContent
          messages.value[aiMessageIndex].loading = false
          scrollToBottom()
        }
      } catch (error) {
        console.error('解析消息失败:', error)
        handleError(error, aiMessageIndex)
      }
    }

    // 处理done事件
    eventSource.addEventListener('done', function () {
      if (streamCompleted) return

      streamCompleted = true
      streaming.value = false
      eventSource?.close()

      // 延迟更新预览，确保后端已完成处理
      setTimeout(async () => {
        await fetchAppData()
        showPreview()
      }, 1000)
    })

    // 处理business-error事件（后端限流等错误）
    eventSource.addEventListener('business-error', function (event: MessageEvent) {
      if (streamCompleted) return

      try {
        const errorData = JSON.parse(event.data)
        console.error('SSE业务错误事件:', errorData)

        // 显示具体的错误信息
        const errorMessage = errorData.message || '生成过程中出现错误'
        messages.value[aiMessageIndex].content = `❌ ${errorMessage}`
        messages.value[aiMessageIndex].loading = false
        message.error(errorMessage)

        streamCompleted = true
        streaming.value = false
        eventSource?.close()
      } catch (parseError) {
        console.error('解析错误事件失败:', parseError, '原始数据:', event.data)
        handleError(new Error('服务器返回错误'), aiMessageIndex)
      }
    })

    // 处理错误
    eventSource.onerror = function () {
      if (streamCompleted || !streaming.value) return
      // 检查是否是正常的连接关闭
      if (eventSource?.readyState === EventSource.CONNECTING) {
        streamCompleted = true
        streaming.value = false
        eventSource?.close()

        setTimeout(async () => {
          await fetchAppData()
          showPreview()
        }, 1000)
      } else {
        handleError(new Error('SSE连接错误'), aiMessageIndex)
      }
    }
  } catch (error) {
    console.error('创建 EventSource 失败：', error)
    handleError(error, aiMessageIndex)
  }
}

// 错误处理函数
const handleError = (error: unknown, aiMessageIndex: number) => {
  console.error('生成代码失败：', error)
  messages.value[aiMessageIndex].content = '抱歉，生成过程中出现了错误，请重试。'
  messages.value[aiMessageIndex].loading = false
  message.error('生成失败，请重试')
  streaming.value = false
}

// 显示预览
const showPreview = () => {
  if (!appData.value.genFileType || !appData.value.id) return

  // 构建预览URL：http://localhost:8888/api/app/preview/{codeGenType}_{appId}/
  const codeGenType = appData.value.genFileType
  const appId = appData.value.id
  previewUrl.value = `http://localhost:8888/api/app/preview/${codeGenType}_${appId}/`
  previewReady.value = true
}

// 部署应用
const deploying = ref(false)
const deployModalVisible = ref(false)
const deployUrl = ref('')

// 应用详情模态框
const appDetailModalVisible = ref(false)
const deleting = ref(false)

const handleDeploy = async () => {
  if (!appData.value.id) {
    message.error('应用ID不存在')
    return
  }

  deploying.value = true
  try {
    const res = await deployApp({
      appId: appData.value.id as any,
    })
    if ((res.data.code === 200 || res.data.code === 0) && res.data.data) {
      deployUrl.value = res.data.data
      deployModalVisible.value = true
    } else {
      message.error('部署失败: ' + (res.data.message || '未知错误'))
    }
  } catch (error: any) {
    message.error('部署失败: ' + (error.message || '网络错误'))
  } finally {
    deploying.value = false
  }
}

// 复制URL
const handleCopyUrl = async () => {
  try {
    await navigator.clipboard.writeText(deployUrl.value)
    message.success('复制成功')
  } catch (error) {
    // 降级方案：使用传统方法
    const textArea = document.createElement('textarea')
    textArea.value = deployUrl.value
    textArea.style.position = 'fixed'
    textArea.style.opacity = '0'
    document.body.appendChild(textArea)
    textArea.select()
    try {
      document.execCommand('copy')
      message.success('复制成功')
    } catch (err) {
      message.error('复制失败')
    }
    document.body.removeChild(textArea)
  }
}

// 前往访问
const handleVisitUrl = () => {
  if (deployUrl.value) {
    window.open(deployUrl.value, '_blank')
  }
}

// 显示应用详情模态框
const showAppDetailModal = () => {
  appDetailModalVisible.value = true
  // 初始化编辑表单数据
  editFormState.name = appData.value.name || ''
  editFormState.cover = appData.value.cover || ''
  editFormState.priority = appData.value.priority ?? 0
  editFormVisible.value = false
}

// 监听模态框关闭，重置编辑状态
watch(appDetailModalVisible, (visible) => {
  if (!visible) {
    editFormVisible.value = false
  }
})

// 格式化创建时间
const formatCreateTime = (time?: string) => {
  if (!time) return '-'
  return dayjs(time).format('YYYY-MM-DD HH:mm:ss')
}

// 切换编辑表单显示/隐藏
const toggleEditForm = () => {
  editFormVisible.value = !editFormVisible.value
  if (editFormVisible.value) {
    // 展开时重置表单数据为当前应用数据
    editFormState.name = appData.value.name || ''
    editFormState.cover = appData.value.cover || ''
    editFormState.priority = appData.value.priority ?? 0
  }
}

// 提交编辑
const handleSubmitEdit = async () => {
  if (!appData.value.id) {
    message.error('应用ID不存在')
    return
  }

  submitting.value = true
  try {
    let res
    if (isAdmin.value) {
      // 管理员使用adminUpdateApp接口
      res = await adminUpdateApp({
        id: appData.value.id as any,
        name: editFormState.name,
        cover: editFormState.cover,
        priority: editFormState.priority,
      })
    } else {
      // 普通用户使用updateApp接口，只能修改名称
      res = await updateApp({
        id: appData.value.id as any,
        name: editFormState.name,
      })
    }

    if ((res.data.code === 200 || res.data.code === 0) && res.data.data) {
      message.success('修改信息成功!')
      // 刷新应用数据
      await fetchAppData()
      // 收起编辑表单
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

// 取消编辑
const handleCancelEdit = () => {
  // 重置表单数据为当前应用数据
  editFormState.name = appData.value.name || ''
  editFormState.cover = appData.value.cover || ''
  editFormState.priority = appData.value.priority ?? 0
  editFormVisible.value = false
}

// 删除应用
const handleDeleteApp = () => {
  if (!appData.value.id) {
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
        const isAdmin = loginUserStore.loginUser.userRole === 1
        if (isAdmin) {
          res = await adminDeleteApp({ id: appData.value.id! as any })
        } else {
          res = await deleteApp({ id: appData.value.id! as any })
        }

        if ((res.data.code === 200 || res.data.code === 0) && res.data.data) {
          message.success('删除成功!')
          appDetailModalVisible.value = false
          // 删除成功后返回首页
          router.push('/')
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

// 格式化消息（支持代码块）
const formatMessage = (text: string) => {
  if (!text) return ''
  // 转义HTML
  let formatted = text
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')

  // 简单的代码块处理
  formatted = formatted.replace(/```([\s\S]*?)```/g, '<pre><code>$1</code></pre>')
  formatted = formatted.replace(/`([^`]+)`/g, '<code>$1</code>')

  // 换行处理
  formatted = formatted.replace(/\n/g, '<br>')

  return formatted
}

// 滚动到底部
const scrollToBottom = async () => {
  await nextTick()
  if (messagesContainerRef.value) {
    messagesContainerRef.value.scrollTop = messagesContainerRef.value.scrollHeight
  }
}

// 清理
onUnmounted(() => {
  if (abortController) {
    abortController.abort()
    abortController = null
  }
})

onMounted(() => {
  fetchAppData()
})
</script>

<style scoped>
#appChatPage {
  height: calc(100vh - 64px);
  display: flex;
  flex-direction: column;
  background: #f5f5f5;
  width: 1600px;
}

/* 顶部栏 */
.top-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 24px;
  background: #fff;
  border-bottom: 1px solid #e8e8e8;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}

.top-bar-left {
  flex: 1;
}

.app-name {
  margin: 0;
  font-size: 20px;
  font-weight: 600;
  color: #1e293b;
}

.top-bar-right {
  display: flex;
  gap: 12px;
}

/* 核心内容区域 */
.content-area {
  flex: 1;
  display: flex;
  overflow: hidden;
  min-width: 0;
}

/* 左侧对话区域 */
.chat-area {
  flex: 1;
  min-width: 0;
  max-width: 50%;
  display: flex;
  flex-direction: column;
  background: #fff;
  border-right: 1px solid #e8e8e8;
  overflow: hidden;
}

.messages-container {
  flex: 1;
  overflow-y: auto;
  padding: 24px;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.message-item {
  display: flex;
  max-width: 80%;
  min-width: 0;
}

.user-message {
  align-self: flex-end;
}

.ai-message {
  align-self: flex-start;
}

.message-content {
  padding: 12px 16px;
  border-radius: 12px;
  word-wrap: break-word;
  word-break: break-word;
  overflow-wrap: break-word;
  max-width: 100%;
  min-width: 0;
}

.user-message .message-content {
  background: #1890ff;
  color: #fff;
}

.ai-message .message-content {
  background: #f0f0f0;
  color: #333;
}

.message-text {
  line-height: 1.6;
  word-wrap: break-word;
  word-break: break-word;
  overflow-wrap: break-word;
  max-width: 100%;
  min-width: 0;
}

.message-text :deep(pre) {
  background: rgba(0, 0, 0, 0.1);
  padding: 8px;
  border-radius: 4px;
  overflow-x: auto;
  margin: 8px 0;
}

.message-text :deep(code) {
  background: rgba(0, 0, 0, 0.1);
  padding: 2px 6px;
  border-radius: 3px;
  font-family: 'Courier New', monospace;
  font-size: 0.9em;
}

.typing-indicator {
  display: inline-block;
  animation: blink 1s infinite;
  color: #1890ff;
}

@keyframes blink {
  0%,
  50% {
    opacity: 1;
  }
  51%,
  100% {
    opacity: 0;
  }
}

/* 输入区域 */
.input-area {
  padding: 16px 24px;
  border-top: 1px solid #e8e8e8;
  background: #fafafa;
}

.input-actions {
  display: flex;
  justify-content: flex-end;
  margin-top: 12px;
}

/* 右侧预览区域 */
.preview-area {
  flex: 1;
  min-width: 0;
  max-width: 50%;
  background: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
}

.preview-placeholder {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
}

.preview-iframe {
  width: 100%;
  height: 100%;
  border: none;
}

/* 部署模态框样式 */
.deploy-modal-content {
  padding: 8px 0;
}

.deploy-url-section {
  margin-bottom: 24px;
}

.deploy-url-label {
  margin-bottom: 8px;
  font-weight: 500;
  color: #333;
}

.deploy-url-container {
  display: flex;
  gap: 8px;
  align-items: center;
}

.deploy-url-input {
  flex: 1;
}

.copy-btn {
  flex-shrink: 0;
}

.deploy-actions {
  margin-top: 24px;
}

/* 应用详情模态框样式 */
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

/* 响应式设计 */
@media (max-width: 768px) {
  .content-area {
    flex-direction: column;
  }

  .chat-area {
    flex: 0 0 50%;
    border-right: none;
    border-bottom: 1px solid #e8e8e8;
  }

  .preview-area {
    flex: 0 0 50%;
  }

  .message-item {
    max-width: 90%;
  }
}
</style>
