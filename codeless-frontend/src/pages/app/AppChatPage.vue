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
        <a-tooltip title="下载项目" placement="bottom">
          <a-button
            :loading="downloading"
            :disabled="streaming && !previewReady"
            @click="handleDownload"
            style="margin-right: 12px"
          >
            <template #icon>
              <DownloadOutlined />
            </template>
          </a-button>
        </a-tooltip>
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
          <!-- 加载更多按钮 -->
          <div v-if="hasMoreHistory" class="load-more-container">
            <a-button
              type="link"
              :loading="loadingHistory"
              @click="loadMoreHistory"
            >
              加载更多
            </a-button>
          </div>
          <div
            v-for="(msg, index) in messages"
            :key="msg.id || index"
            :class="['message-item', msg.type === 'user' ? 'user-message' : 'ai-message']"
          >
            <a-avatar
              v-if="msg.type === 'user'"
              :src="loginUserStore.loginUser.avatarUrl"
              :size="32"
              class="message-avatar"
            >
              {{ loginUserStore.loginUser.username?.[0] || 'U' }}
            </a-avatar>
            <div v-if="msg.type === 'ai'" class="ai-avatar">
              <img src="@/assets/AI_icon.svg" alt="AI" class="ai-icon" />
            </div>
            <div class="message-content">
              <div class="message-text" v-html="formatMessage(msg.content)"></div>
            </div>
          </div>
          <div class="message-item ai-message" v-if="streaming">
            <div class="message-content" style="height: 38px; margin-left: 44px; line-height: 38px">
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
          <div v-if="streaming" class="preview-placeholder">
            <a-spin :spinning="streaming">
              <template #tip>
                {{ 'AI 正在生成网站, 请稍等...' }}
              </template>
            </a-spin>
          </div>
          <div v-else-if="!previewReady" class="preview-placeholder">
            <div class="preview-tip">
              <p>尝试与AI对话生成应用</p>
            </div>
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
    <app-detail-modal
      :app-data="appData"
      :visible="appDetailModalVisible"
      @update:visible="appDetailModalVisible = $event"
      @update:app="handleAppUpdate"
      @deleted="handleAppDeleted"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted, nextTick, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { message } from 'ant-design-vue'
import { DownloadOutlined } from '@ant-design/icons-vue'
import { getAppById, deployApp } from '@/api/app.ts'
import { pageListMyChatHistory } from '@/api/chatHistory.ts'
import request from '@/request.ts'
import { useLoginUserStore } from '@/stores/loginUser.ts'
import AppDetailModal from '@/components/AppDetailModal.vue'
import MarkdownIt from 'markdown-it'
import hljs from 'highlight.js'
import 'highlight.js/styles/github.css'
import { CodeGenTypeEnum } from '@/utils/constants.ts'

const route = useRoute()
const router = useRouter()

// 应用数据
const appData = ref<API.AppVo>({})
const loading = ref(false)

// 消息列表
interface Message {
  id?: string | number
  type: 'user' | 'ai'
  content: string,
  loading?: boolean,
}

const messages = ref<Message[]>([])
const userInput = ref('')
const streaming = ref(false)

// 对话历史相关
const loadingHistory = ref(false)
const lastRecentCreateTime = ref<string | undefined>(undefined)
const hasMoreHistory = ref(false)

// 预览相关
const previewReady = ref(false)
const previewUrl = ref('')
const canDeploy = computed(() => previewReady.value && appData.value.genFileType)

const loginUserStore = useLoginUserStore()

// 初始化 Markdown 解析器
const md = new MarkdownIt({
  html: true,
  linkify: true,
  typographer: true,
  highlight: function (str: any, lang: any) {
    if (lang && hljs.getLanguage(lang)) {
      try {
        return '<pre class="hljs"><code>' +
               hljs.highlight(str, { language: lang, ignoreIllegals: true }).value +
               '</code></pre>';
      } catch (__) {}
    }
    return '<pre class="hljs"><code>' + md.utils.escapeHtml(str) + '</code></pre>';
  }
})

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

// 加载对话历史
const loadChatHistory = async (createTime?: string) => {
  if (!appId.value) return

  loadingHistory.value = true
  try {
    // 后端将Long类型id转换为了string，前端不应该转为Number，避免精度丢失
    // 但API类型定义是number，所以使用as any绕过类型检查
    const appIdForApi = typeof appId.value === 'string'
      ? (appId.value as any) // 保持string类型，避免精度丢失
      : (appId.value as any)

    const res = await pageListMyChatHistory({
      appId: appIdForApi,
      pageSize: 10,
      lastRecentCreateTime: createTime,
    })

    if ((res.data.code === 200 || res.data.code === 0) && res.data.data) {
      const historyData = res.data.data
      const records = historyData.records || []

      // 后端返回的是降序，需要反转成升序
      const reversedRecords = [...records].reverse()

      // 转换为消息格式
      const historyMessages: Message[] = []
      for (const record of reversedRecords) {
        if (record.messageContent) {
          // 根据 messageType 判断是用户消息还是AI消息
          // 通常 'user' 或 'USER' 表示用户消息，'ai' 或 'AI' 表示AI消息
          const isUserMessage = record.messageType?.toLowerCase() === 'user' ||
                                record.messageType === '0' ||
                                !record.messageType // 如果没有 messageType，默认是用户消息

          historyMessages.push({
            id: record.id ? String(record.id) : undefined,
            type: isUserMessage ? 'user' : 'ai',
            content: record.messageContent,
          })
        }
      }

      if (createTime) {
        // 加载更多：插入到列表前面
        messages.value = [...historyMessages, ...messages.value]
      } else {
        // 首次加载：替换整个列表
        messages.value = historyMessages
      }

      // 更新游标：使用最后一条记录的创建时间
      if (records.length > 0) {
        const lastRecord = records[records.length - 1]
        lastRecentCreateTime.value = lastRecord.createTime
        // 如果返回的记录数等于 pageSize，说明可能还有更多
        hasMoreHistory.value = records.length >= 10
      } else {
        lastRecentCreateTime.value = undefined
        hasMoreHistory.value = false
      }

      // 如果有历史消息，尝试显示预览（但不触发生成）
      if (messages.value.length > 0 && !createTime) {
        // 只在首次加载时检查预览，加载更多时不检查
        await nextTick()
        checkAndShowPreview()
      }
    } else {
      console.error('加载对话历史失败:', res.data.message || '未知错误')
    }
  } catch (error: any) {
    console.error('加载对话历史失败:', error)
    message.error('加载对话历史失败: ' + (error.message || '网络错误'))
  } finally {
    loadingHistory.value = false
  }
}

// 加载更多历史消息
const loadMoreHistory = async () => {
  if (!lastRecentCreateTime.value || loadingHistory.value) return
  await loadChatHistory(lastRecentCreateTime.value)
  await nextTick()
  // 保持滚动位置
  if (messagesContainerRef.value) {
    const oldScrollHeight = messagesContainerRef.value.scrollHeight
    const oldScrollTop = messagesContainerRef.value.scrollTop
    const scrollDiff = messagesContainerRef.value.scrollHeight - oldScrollHeight
    messagesContainerRef.value.scrollTop = oldScrollTop + scrollDiff
  }
}

// 检查并显示预览
const checkAndShowPreview = async () => {
  if (!appData.value.genFileType || !appData.value.id) {
    previewReady.value = false
    return
  }

  // 尝试加载预览
  const codeGenType = appData.value.genFileType
  const appIdStr = String(appData.value.id) // 确保是字符串类型
  let testUrl = `http://localhost:8888/api/app/preview/${codeGenType}_${appIdStr}/`

  // 检查预览是否可用
  try {
    const isVueProject = appData.value.genFileType === CodeGenTypeEnum.VUE_PROJECT;
    if (isVueProject) {
      testUrl += 'dist/index.html';
    }
    const response = await fetch(testUrl, { method: 'GET' })
    if (response.ok) {
      previewUrl.value = testUrl
      previewReady.value = true
    } else {
      previewReady.value = false
    }
  } catch (error) {
    previewReady.value = false
  }
}

// 获取应用信息
const fetchAppData = async () => {
  const id = route.query.id as string
  if (!id) {
    message.error('缺少应用ID')
    router.back()
    return
  }
  appId.value = id;

  // 移除URL中的view参数（如果存在）
  if (route.query.view) {
    router.replace({
      path: route.path,
      query: { id: route.query.id }
    })
  }

  loading.value = true
  try {
    const res = await getAppById({ id: appId.value as any })
    if (res.data.code === 200 && res.data.data) {
      appData.value = res.data.data

      // 先加载对话历史
      await loadChatHistory()

      // 如果是自己的app，并且没有对话历史，才自动发送initialPrompt
      if (canEdit.value && messages.value.length === 0 && appData.value.initialPrompt) {
        await sendInitialMessage(appData.value.initialPrompt)
      } else if (messages.value.length > 0) {
        // 如果有历史消息，不触发AI生成，只检查预览
        // 确保streaming状态为false，防止重复生成
        streaming.value = false
        await checkAndShowPreview()
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

// 显示预览（在AI生成完成后调用）
const showPreview = async () => {
  await checkAndShowPreview()
}

// 部署应用
const deploying = ref(false)
const deployModalVisible = ref(false)
const deployUrl = ref('')

// 下载应用
const downloading = ref(false)

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

// 下载应用
const handleDownload = async () => {
  if (!appData.value.id) {
    message.error('应用ID不存在')
    return
  }

  downloading.value = true
  try {
    // 使用 axios 直接请求二进制数据
    const response = await request({
      url: `/app/download/${appData.value.id}`,
      method: 'GET',
      responseType: 'blob',
    })

    // 从响应头获取文件名（axios 可能将响应头键名转换为小写）
    const contentDisposition = response.headers['content-disposition'] || response.headers['Content-Disposition']
    let fileName = `${'project_' + (appData.value.id ?? 'unknown')}.zip`
    if (contentDisposition) {
      // 匹配 filename="xxx" 或 filename=xxx 格式
      const fileNameMatch = contentDisposition.match(/filename[^;=\n]*=((['"]).*?\2|[^;\n]*)/i)
      if (fileNameMatch && fileNameMatch[1]) {
        fileName = fileNameMatch[1].replace(/['"]/g, '')
        // 处理可能的编码问题（如 filename*=UTF-8''xxx 格式）
        if (fileName.startsWith("UTF-8''") || fileName.startsWith("utf-8''")) {
          fileName = decodeURIComponent(fileName.replace(/^UTF-8''/i, ''))
        } else {
          try {
            fileName = decodeURIComponent(fileName)
          } catch (e) {
            // 如果解码失败，使用原始值
          }
        }
      }
    }

    // 创建 blob URL 并触发下载
    const blob = new Blob([response.data], { type: 'application/zip' })
    const url = window.URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.download = fileName
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
    window.URL.revokeObjectURL(url)
  } catch (error: any) {
    console.error('下载失败:', error)
    message.error('下载失败: ' + (error.message || '网络错误'))
  } finally {
    downloading.value = false
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
}

// 处理应用更新
const handleAppUpdate = (updatedApp: API.AppVo) => {
  appData.value = { ...appData.value, ...updatedApp }
}

// 处理应用删除
const handleAppDeleted = () => {
  router.push('/')
}

// 格式化消息（支持 Markdown 和代码高亮）
const formatMessage = (text: string) => {
  if (!text) return ''
  return md.render(text)
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
  // 清理工作（如果需要）
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
  align-items: flex-start;
  gap: 12px;
  max-width: 80%;
  min-width: 0;
}

.user-message {
  align-self: flex-end;
  flex-direction: row-reverse;
}

.ai-message {
  align-self: flex-start;
}

.message-avatar {
  flex-shrink: 0;
}

.ai-avatar {
  width: 32px;
  height: 32px;
  flex-shrink: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f0f0f0;
  border-radius: 50%;
  padding: 4px;
}

.ai-icon {
  width: 24px;
  height: 24px;
}

.message-content {
  padding: 0 8px;
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

/* Markdown 样式 */
.message-text :deep(h1),
.message-text :deep(h2),
.message-text :deep(h3),
.message-text :deep(h4),
.message-text :deep(h5),
.message-text :deep(h6) {
  margin: 16px 0 8px 0;
  font-weight: 600;
  line-height: 1.4;
}

.message-text :deep(h1) { font-size: 1.5em; }
.message-text :deep(h2) { font-size: 1.3em; }
.message-text :deep(h3) { font-size: 1.1em; }

.message-text :deep(p) {
  margin: 8px 0;
}

.message-text :deep(ul),
.message-text :deep(ol) {
  margin: 8px 0;
  padding-left: 24px;
}

.message-text :deep(li) {
  margin: 4px 0;
}

.message-text :deep(blockquote) {
  margin: 8px 0;
  padding: 8px 16px;
  border-left: 4px solid #d1d5db;
  background: rgba(0, 0, 0, 0.05);
  color: #6b7280;
}

.message-text :deep(a) {
  color: #3b82f6;
  text-decoration: none;
}

.message-text :deep(a:hover) {
  text-decoration: underline;
}

.message-text :deep(table) {
  border-collapse: collapse;
  margin: 12px 0;
  width: 100%;
}

.message-text :deep(th),
.message-text :deep(td) {
  border: 1px solid #e5e7eb;
  padding: 8px 12px;
  text-align: left;
}

.message-text :deep(th) {
  background: #f9fafb;
  font-weight: 600;
}

.message-text :deep(hr) {
  border: none;
  border-top: 1px solid #e5e7eb;
  margin: 16px 0;
}

/* 代码块样式 */
.message-text :deep(pre.hljs) {
  background: #ffffff;
  padding: 16px;
  border-radius: 8px;
  overflow-x: auto;
  margin: 12px 0;
  font-size: 14px;
  line-height: 1.5;
  border: 1px solid #d1d5db;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
}

.message-text :deep(pre.hljs code) {
  background: transparent;
  padding: 0;
  border-radius: 0;
  font-family: 'Fira Code', 'Consolas', 'Monaco', 'Courier New', monospace;
  color: #1f2937;
}

.ai-message .message-text :deep(pre.hljs) {
  background: #ffffff;
  border-color: #d1d5db;
}

.ai-message .message-text :deep(pre.hljs code) {
  color: #1f2937;
}

.message-text :deep(code:not(pre code)) {
  background: #f3f4f6;
  padding: 2px 6px;
  border-radius: 4px;
  font-family: 'Fira Code', 'Consolas', 'Monaco', 'Courier New', monospace;
  font-size: 0.9em;
  color: #dc2626;
  border: 1px solid #e5e7eb;
}

.ai-message .message-text :deep(code:not(pre code)) {
  background: #f3f4f6;
  color: #dc2626;
  border: 1px solid #e5e7eb;
}

.user-message .message-text :deep(code:not(pre code)) {
  background: rgba(255, 255, 255, 0.25);
  color: #ffd700;
  border: 1px solid rgba(255, 255, 255, 0.3);
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

.preview-tip {
  text-align: center;
  color: #8c8c8c;
}

.preview-tip p {
  font-size: 16px;
  margin: 0;
}

.load-more-container {
  text-align: center;
  padding: 16px 0;
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
