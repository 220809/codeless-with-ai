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
          <div v-if="streaming" class="message-item ai-message">
            <div class="message-content">
              <div class="message-text" v-html="formatMessage(currentAiMessage)"></div>
              <span class="typing-indicator">▋</span>
            </div>
          </div>
        </div>

        <!-- 用户消息输入框 -->
        <div class="input-area">
          <a-textarea
            v-model:value="userInput"
            :placeholder="'输入你的消息...'"
            :auto-size="{ minRows: 2, maxRows: 6 }"
            :disabled="streaming || !appData.id"
            @keydown.enter.ctrl="handleSendMessage"
            @keydown.enter.exact.prevent="handleSendMessage"
          />
          <div class="input-actions">
            <a-button
              type="primary"
              :loading="streaming"
              :disabled="!userInput.trim() || !appData.id"
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
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted, nextTick, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { message } from 'ant-design-vue'
import { getAppById, genCodeFromChat, deployApp } from '@/api/app.ts'

const route = useRoute()
const router = useRouter()

// 应用数据
const appData = ref<API.AppVo>({})
const loading = ref(false)

// 消息列表
interface Message {
  type: 'user' | 'ai'
  content: string
}

const messages = ref<Message[]>([])
const userInput = ref('')
const streaming = ref(false)
const currentAiMessage = ref('')
let eventSource: EventSource | null = null

// 预览相关
const previewReady = ref(false)
const previewUrl = ref('')
const canDeploy = computed(() => previewReady.value && appData.value.genFileType)

// 消息容器引用（用于自动滚动）
const messagesContainerRef = ref<HTMLElement>()

// 获取应用信息
const fetchAppData = async () => {
  const appId = route.query.id as string
  if (!appId) {
    message.error('缺少应用ID')
    router.back()
    return
  }

  loading.value = true
  try {
    const res = await getAppById({ id: appId as any })
    if ((res.data.code === 200 || res.data.code === 0) && res.data.data) {
      appData.value = res.data.data

      // 如果有初始提示词，自动发送
      if (appData.value.initialPrompt) {
        await sendMessage(appData.value.initialPrompt)
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

// 发送消息并处理SSE流
const sendMessage = async (content: string) => {
  if (!appData.value.id) return

  // 添加用户消息
  messages.value.push({
    type: 'user',
    content: content,
  })

  // 滚动到底部
  await scrollToBottom()

  // 开始流式接收
  streaming.value = true
  currentAiMessage.value = ''

  // 关闭之前的连接
  if (eventSource) {
    eventSource.close()
    eventSource = null
  }

  try {
    // 构建SSE URL
    const baseURL = 'http://localhost:8888/api'
    const url = `${baseURL}/app/chat/codegen?appId=${appData.value.id}&userMessage=${encodeURIComponent(content)}`

    // 创建EventSource
    eventSource = new EventSource(url)

    let streamEnded = false

    const finishStream = () => {
      if (streamEnded) return
      streamEnded = true

      if (eventSource) {
        eventSource.close()
        eventSource = null
      }

      streaming.value = false

      // 保存AI消息
      if (currentAiMessage.value.trim()) {
        messages.value.push({
          type: 'ai',
          content: currentAiMessage.value,
        })
        currentAiMessage.value = ''

        // 生成完成后，显示预览
        showPreview()
      }
    }

    eventSource.onmessage = (event) => {
      if (event.data) {
        // 检查是否是结束标记
        if (event.data === '[DONE]' || event.data.trim() === '') {
          finishStream()
          return
        }

        currentAiMessage.value += event.data
        // 实时滚动到底部
        scrollToBottom()
      }
    }

    eventSource.onerror = (error) => {
      console.error('SSE error:', error)
      // EventSource在错误时会自动重连，我们需要检查readyState
      if (eventSource?.readyState === EventSource.CLOSED) {
        finishStream()
      }
    }

    // 设置超时（60秒后自动关闭）
    setTimeout(() => {
      if (!streamEnded && eventSource) {
        finishStream()
        message.warning('生成超时，已保存当前内容')
      }
    }, 60000)
  } catch (error: any) {
    streaming.value = false
    message.error('发送消息失败: ' + (error.message || '网络错误'))
  }
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
      const deployUrl = res.data.data
      message.success(`部署成功！访问地址: ${deployUrl}`)
      // 可以打开新窗口显示部署的网站
      window.open(deployUrl, '_blank')
    } else {
      message.error('部署失败: ' + (res.data.message || '未知错误'))
    }
  } catch (error: any) {
    message.error('部署失败: ' + (error.message || '网络错误'))
  } finally {
    deploying.value = false
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
  if (eventSource) {
    eventSource.close()
    eventSource = null
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
}

/* 左侧对话区域 */
.chat-area {
  flex: 1;
  display: flex;
  flex-direction: column;
  background: #fff;
  border-right: 1px solid #e8e8e8;
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
  0%, 50% {
    opacity: 1;
  }
  51%, 100% {
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
  background: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
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
