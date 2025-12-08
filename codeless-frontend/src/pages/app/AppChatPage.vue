<template>
  <div id="appChatPage">
    <!-- 顶部栏 -->
    <div class="top-bar">
      <div class="top-bar-left">
        <h2 class="app-name">{{ appData.name || '未命名应用' }}</h2>
      </div>
      <div class="top-bar-right">
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
          <div class="message-item ai-message">
            <div class="message-content">
              <div class="message-text" v-html="formatMessage(currentAiMessage)"></div>
              <span class="typing-indicator">▋</span>
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
          <a-empty description="等待生成完成..." />
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
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted, nextTick, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { message } from 'ant-design-vue'
import { getAppById, genCodeFromChat, deployApp } from '@/api/app.ts'
import request from '@/request.ts'
import { useLoginUserStore } from '@/stores/loginUser.ts'

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
