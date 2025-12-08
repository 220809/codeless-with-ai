<template>
  <div class="home-page">
    <!-- 标题区域 -->
    <div class="header-section">
      <div class="title-container">
        <h1 class="main-title">
          <span>一句话</span>
          <span class="cat-icon">🐱</span>
          <span>呈所想</span>
        </h1>
        <p class="subtitle">与 AI 对话轻松创建应用和网站</p>
      </div>
    </div>

    <!-- 提示词输入框区域 -->
    <div class="input-section">
      <div class="input-container">
        <a-textarea
          v-model:value="promptInput"
          :placeholder="'使用 NoCode 创建一个高效的小工具,帮我计算......'"
          :auto-size="{ minRows: 4, maxRows: 6 }"
          class="prompt-input"
          @keydown.enter.ctrl="handleSend"
        />
        <div class="input-actions">
              <div class="input-left-actions">
                <a-button type="text" class="action-btn">
                  <template #icon>
                    <UploadOutlined />
                  </template>
                  上传
                </a-button>
              </div>
              <a-button
                type="primary"
                shape="circle"
                :loading="creating"
                class="send-btn"
                @click="handleSend"
              >
                <template #icon>
                  <ArrowUpOutlined />
                </template>
              </a-button>
            </div>
      </div>
    </div>

    <div id="app-list">
      <!-- 我的应用列表 -->
      <div class="apps-section">
        <div class="section-header">
          <h2 class="section-title">我的作品</h2>
        </div>
        <a-spin :spinning="myAppsLoading">
          <div v-if="myAppsData.length === 0 && !myAppsLoading" class="empty-state">
            <a-empty description="暂无应用" />
          </div>
          <div v-else class="apps-grid">
            <a-card
              v-for="app in myAppsData"
              :key="app.id"
              class="app-card"
              :hoverable="true"
            >
              <template #cover>
                <div class="app-cover">
                  <img
                    v-if="app.cover"
                    :src="app.cover"
                    :alt="app.name"
                    class="cover-image"
                  />
                  <div v-else class="cover-placeholder">
                    <FileImageOutlined class="placeholder-icon" />
                  </div>
                  <div class="app-cover-buttons">
                    <a-button
                      v-if="app.deployKey"
                      class="cover-btn view-works-btn"
                      @click.stop="handleViewDeploy(app)"
                    >
                      查看作品
                    </a-button>
                    <a-button
                      class="cover-btn view-chat-btn"
                      @click.stop="handleViewChat(app)"
                    >
                      查看对话
                    </a-button>
                  </div>
                </div>
              </template>
              <a-card-meta>
                <template #title>
                  <div class="app-title">{{ app.name || '未命名应用' }}</div>
                </template>
                <template #description>
                  <div class="app-time">
                    {{ formatTime(app.createTime) }}
                  </div>
                </template>
              </a-card-meta>
            </a-card>
          </div>
          <div v-if="myAppsTotal > 0" class="pagination-container">
            <a-pagination
              v-model:current="myAppsPageNum"
              v-model:page-size="myAppsPageSize"
              :total="myAppsTotal"
              :page-size-options="['10', '20']"
              show-size-changer
              show-total
              @change="handleMyAppsPageChange"
              @show-size-change="handleMyAppsPageChange"
            />
          </div>
        </a-spin>
      </div>

      <!-- 精选应用列表 -->
      <div class="apps-section">
        <div class="section-header">
          <h2 class="section-title">精选案例</h2>
        </div>
        <a-spin :spinning="featuredAppsLoading">
          <div v-if="featuredAppsData.length === 0 && !featuredAppsLoading" class="empty-state">
            <a-empty description="暂无应用" />
          </div>
          <div v-else class="apps-grid">
            <a-card
              v-for="app in featuredAppsData"
              :key="app.id"
              class="app-card"
              :hoverable="true"
            >
              <template #cover>
                <div class="app-cover">
                  <img
                    v-if="app.cover"
                    :src="app.cover"
                    :alt="app.name"
                    class="cover-image"
                  />
                  <div v-else class="cover-placeholder">
                    <FileImageOutlined class="placeholder-icon" />
                  </div>
                  <div class="app-cover-buttons">
                    <a-button
                      v-if="app.deployKey"
                      class="cover-btn view-works-btn"
                      @click.stop="handleViewDeploy(app)"
                    >
                      查看作品
                    </a-button>
                    <a-button
                      class="cover-btn view-chat-btn"
                      @click.stop="handleViewChat(app)"
                    >
                      查看对话
                    </a-button>
                  </div>
                </div>
              </template>
              <a-card-meta>
                <template #title>
                  <div class="app-title">{{ app.name || '未命名应用' }}</div>
                </template>
                <template #description>
                  <div class="app-info">
                    <a-avatar
                      v-if="app.user?.avatarUrl"
                      :src="app.user.avatarUrl"
                      :size="20"
                      class="user-avatar"
                    />
                    <span class="app-author">
                    {{ app.user?.username || 'NoCode 官方' }}
                  </span>
                    <span class="app-time">
                    {{ formatTime(app.createTime) }}
                  </span>
                  </div>
                </template>
              </a-card-meta>
            </a-card>
          </div>
          <div v-if="featuredAppsTotal > 0" class="pagination-container">
            <a-pagination
              v-model:current="featuredAppsPageNum"
              v-model:page-size="featuredAppsPageSize"
              :total="featuredAppsTotal"
              :page-size-options="['10', '20']"
              show-size-changer
              show-total
              @change="handleFeaturedAppsPageChange"
              @show-size-change="handleFeaturedAppsPageChange"
            />
          </div>
        </a-spin>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, reactive } from 'vue'
import { message } from 'ant-design-vue'
import {
  UploadOutlined,
  ArrowUpOutlined,
  FileImageOutlined,
} from '@ant-design/icons-vue'
import { addApp, pageListMyApps, pageListFeaturedApps } from '@/api/app.ts'
import { useRouter } from 'vue-router'
import { useLoginUserStore } from '@/stores/loginUser.ts'
import dayjs from 'dayjs'

// 提示词输入
const promptInput = ref('')
const creating = ref(false)

// 我的应用列表
const myAppsData = ref<API.AppVo[]>([])
const myAppsLoading = ref(false)
const myAppsPageNum = ref(1)
const myAppsPageSize = ref(20)
const myAppsTotal = ref(0)
const myAppsSearchName = ref('')

// 精选应用列表
const featuredAppsData = ref<API.AppVo[]>([])
const featuredAppsLoading = ref(false)
const featuredAppsPageNum = ref(1)
const featuredAppsPageSize = ref(20)
const featuredAppsTotal = ref(0)
const featuredAppsSearchName = ref('')

// 创建应用
const handleSend = async () => {
  if (!promptInput.value.trim()) {
    message.warning('请输入提示词')
    return
  }

  creating.value = true
  try {
    const res = await addApp({
      initialPrompt: promptInput.value.trim(),
    })
    if ((res.data.code === 200 || res.data.code === 0) && res.data.data) {
      const appId = res.data.data
      message.success(`应用创建成功！应用ID: ${appId}`)
      promptInput.value = ''
      // 刷新我的应用列表
      await fetchMyApps()
      // 跳转到应用生成对话页面
      router.push(`/app/chat?id=${appId}`)
    } else {
      message.error('创建失败: ' + (res.data.message || '未知错误'))
    }
  } catch (error: any) {
    message.error('创建失败: ' + (error.message || '网络错误'))
  } finally {
    creating.value = false
  }
}

// 获取我的应用列表
const fetchMyApps = async () => {
  myAppsLoading.value = true
  try {
    const res = await pageListMyApps({
      pageNum: myAppsPageNum.value,
      pageSize: myAppsPageSize.value,
      name: myAppsSearchName.value || undefined,
    })
    if ((res.data.code === 200 || res.data.code === 0) && res.data.data) {
      myAppsData.value = res.data.data.records || []
      myAppsTotal.value = res.data.data.totalRow || 0
    } else {
      message.error('获取应用列表失败: ' + (res.data.message || '未知错误'))
    }
  } catch (error: any) {
    message.error('获取应用列表失败: ' + (error.message || '网络错误'))
  } finally {
    myAppsLoading.value = false
  }
}

// 获取精选应用列表
const fetchFeaturedApps = async () => {
  featuredAppsLoading.value = true
  try {
    const res = await pageListFeaturedApps({
      pageNum: featuredAppsPageNum.value,
      pageSize: featuredAppsPageSize.value,
      name: featuredAppsSearchName.value || undefined,
    })
    if ((res.data.code === 200 || res.data.code === 0) && res.data.data) {
      featuredAppsData.value = res.data.data.records || []
      featuredAppsTotal.value = res.data.data.totalRow || 0
    } else {
      message.error('获取精选应用列表失败: ' + (res.data.message || '未知错误'))
    }
  } catch (error: any) {
    message.error('获取精选应用列表失败: ' + (error.message || '网络错误'))
  } finally {
    featuredAppsLoading.value = false
  }
}

// 我的应用分页变化
const handleMyAppsPageChange = (page: number, pageSize: number) => {
  myAppsPageNum.value = page
  myAppsPageSize.value = pageSize
  fetchMyApps()
}

// 我的应用搜索
const handleMyAppsSearch = () => {
  myAppsPageNum.value = 1
  fetchMyApps()
}

// 精选应用分页变化
const handleFeaturedAppsPageChange = (page: number, pageSize: number) => {
  featuredAppsPageNum.value = page
  featuredAppsPageSize.value = pageSize
  fetchFeaturedApps()
}

// 精选应用搜索
const handleFeaturedAppsSearch = () => {
  featuredAppsPageNum.value = 1
  fetchFeaturedApps()
}

const router = useRouter()
const loginUserStore = useLoginUserStore()

// 查看对话
const handleViewChat = (app: API.AppVo) => {
  if (!app.id) return
  // 跳转到应用生成对话页面
  // 保持id为原始类型，避免精度丢失
  const appId = app.id
  router.push(`/app/chat?id=${appId}${app.deployKey ? '&view=1' : ''}`);
}

// 查看作品
const handleViewDeploy = (app: API.AppVo) => {
  if (!app.deployKey) return
  // 打开部署地址：localhost/{deployKey}
  const deployUrl = `http://localhost/${app.deployKey}`
  window.open(deployUrl, '_blank')
}

// 格式化时间
const formatTime = (time?: string) => {
  if (!time) return ''
  const now = dayjs()
  const createTime = dayjs(time)
  const diffHours = now.diff(createTime, 'hour')
  const diffDays = now.diff(createTime, 'day')
  const diffWeeks = now.diff(createTime, 'week')

  if (diffHours < 1) {
    return '刚刚创建'
  } else if (diffHours < 24) {
    return `创建于${diffHours}小时前`
  } else if (diffDays < 7) {
    return `创建于${diffDays}天前`
  } else if (diffWeeks < 4) {
    return `创建于${diffWeeks}周前`
  } else {
    return createTime.format('YYYY-MM-DD')
  }
}

// 初始化
onMounted(() => {
  fetchMyApps()
  fetchFeaturedApps()
})
</script>

<style scoped>
.home-page {
  min-height: calc(100vh - 64px - 50px);
  background: transparent;
  padding: 40px 20px;
  max-width: 1600px;
  margin: 0 auto;
}

/* 标题区域 */
.header-section {
  text-align: center;
  margin-bottom: 60px;
}

.title-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 16px;
}

.main-title {
  font-size: 48px;
  font-weight: 700;
  color: #1e293b;
  letter-spacing: -1px;
  margin: 0;
  display: flex;
  align-items: center;
  gap: 12px;
}

.cat-icon {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 40px;
  height: 40px;
  background: #10b981;
  border-radius: 50%;
  font-size: 24px;
}

.subtitle {
  font-size: 18px;
  color: #64748b;
  margin: 0;
  font-weight: 400;
}

/* 输入框区域 */
.input-section {
  margin-bottom: 80px;
  display: flex;
  justify-content: center;
}

.input-container {
  width: 100%;
  max-width: 900px;
  position: relative;
}

.prompt-input {
  width: 100%;
  border-radius: 16px;
  padding: 20px;
  font-size: 16px;
  border: 1px solid #e2e8f0;
  box-shadow: 0 4px 6px rgba(0, 0, 0, 0.05);
  transition: all 0.3s ease;
}

.prompt-input:focus {
  border-color: #3b82f6;
  box-shadow: 0 4px 12px rgba(59, 130, 246, 0.15);
}

.input-actions {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 12px;
  padding: 0 8px;
}

.input-left-actions {
  display: flex;
  gap: 8px;
}

.action-btn {
  display: flex;
  align-items: center;
  gap: 4px;
  color: #64748b;
  border-radius: 8px;
  padding: 4px 12px;
  font-size: 14px;
}

.action-btn:hover {
  background-color: #f1f5f9;
  color: #3b82f6;
}

.send-btn {
  width: 48px;
  height: 48px;
  background: #3b82f6;
  border: none;
  box-shadow: 0 4px 8px rgba(59, 130, 246, 0.3);
  transition: all 0.3s ease;
}

.send-btn:hover {
  background: #2563eb;
  transform: translateY(-2px);
  box-shadow: 0 6px 12px rgba(59, 130, 246, 0.4);
}

/* 应用列表区域 */
.apps-section {
  margin-bottom: 60px;
  width: 1600px;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
  flex-wrap: wrap;
  gap: 16px;
}

.section-title {
  font-size: 28px;
  font-weight: 600;
  color: #1e293b;
  margin: 0;
}

.empty-state {
  padding: 60px 0;
  text-align: center;
}

#app-list {
  background: #ffffff;
  border-radius: 12px;
  padding: 36px 24px;
}

.apps-grid {
  width: 100%;
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 24px;
  margin-bottom: 24px;
}

.app-card {
  border-radius: 12px;
  overflow: hidden;
  transition: all 0.3s ease;
  cursor: pointer;
  border: 1px solid #e2e8f0;
}

.app-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 16px rgba(0, 0, 0, 0.1);
}

.app-cover {
  width: 100%;
  height: 200px;
  background: #f8fafc;
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
  position: relative;
}

.cover-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.cover-placeholder {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #f1f5f9 0%, #e2e8f0 100%);
}

.placeholder-icon {
  font-size: 48px;
  color: #cbd5e1;
}

.app-cover-buttons {
  position: absolute;
  bottom: 20px;
  left: 50%;
  transform: translateX(-50%);
  display: flex;
  gap: 12px;
  z-index: 10;
  opacity: 0;
  transition: opacity 0.3s ease;
}

.app-card:hover .app-cover-buttons {
  opacity: 1;
}

.app-card:hover .cover-image,
.app-card:hover .cover-placeholder {
  filter: blur(4px);
  transition: filter 0.3s ease;
}

.cover-btn {
  height: 40px;
  padding: 0 24px;
  font-size: 14px;
  font-weight: 500;
  border-radius: 6px;
  border: none;
  cursor: pointer;
  transition: all 0.3s ease;
}

.view-works-btn {
  background: #4a5568;
  color: #ffffff;
}

.view-works-btn:hover {
  background: #2d3748;
  transform: translateY(-2px);
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.2);
}

.view-chat-btn {
  background: #ffffff;
  color: #1e293b;
  border: 1px solid #e2e8f0;
}

.view-chat-btn:hover {
  background: #f8fafc;
  transform: translateY(-2px);
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.1);
}

.app-title {
  font-size: 16px;
  font-weight: 500;
  color: #1e293b;
  margin-bottom: 8px;
}

.app-time {
  font-size: 12px;
  color: #94a3b8;
}

.app-info {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 12px;
}

.user-avatar {
  flex-shrink: 0;
}

.app-author {
  color: #64748b;
  font-weight: 500;
}

.pagination-container {
  display: flex;
  justify-content: center;
  margin-top: 24px;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .home-page {
    padding: 20px 16px;
  }

  .main-title {
    font-size: 32px;
  }

  .subtitle {
    font-size: 16px;
  }

  .section-title {
    font-size: 24px;
  }

  .apps-grid {
    grid-template-columns: 1fr;
    gap: 16px;
  }

  .section-header {
    flex-direction: column;
    align-items: flex-start;
  }

  .section-header :deep(.ant-input-search) {
    width: 100%;
  }
}
</style>
