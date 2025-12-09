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
          :placeholder="'使用 CodeLess 创建一个高效的小工具,帮我计算......'"
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
            <app-card
              v-for="app in myAppsData"
              :key="app.id"
              :app="app"
              :show-author="false"
              @view-deploy="handleViewDeploy"
              @view-chat="handleViewChat"
            />
          </div>
          <div v-if="myAppsTotal > 0" class="pagination-container">
            <a-pagination
              v-model:current="myAppsPageNum"
              v-model:page-size="myAppsPageSize"
              :total="myAppsTotal"
              :show-total="(total: number) => `共${total}条结果`"
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
            <app-card
              v-for="app in featuredAppsData"
              :key="app.id"
              :app="app"
              :show-author="true"
              @view-deploy="handleViewDeploy"
              @view-chat="handleViewChat"
            />
          </div>
          <div v-if="featuredAppsTotal > 0" class="pagination-container">
            <a-pagination
              v-model:current="featuredAppsPageNum"
              v-model:page-size="featuredAppsPageSize"
              :total="featuredAppsTotal"
              :show-total="(total: number) => `共${total}条结果`"
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
import { ref, onMounted } from 'vue'
import { message } from 'ant-design-vue'
import {
  UploadOutlined,
  ArrowUpOutlined,
} from '@ant-design/icons-vue'
import { addApp, pageListMyApps, pageListFeaturedApps } from '@/api/app.ts'
import { useRouter } from 'vue-router'
import { useLoginUserStore } from '@/stores/loginUser.ts'
import AppCard from '@/components/AppCard.vue'

// 提示词输入
const promptInput = ref('')
const creating = ref(false)

// 我的应用列表
const myAppsData = ref<API.AppVo[]>([])
const myAppsLoading = ref(false)
const myAppsPageNum = ref(1)
const myAppsPageSize = ref(8)
const myAppsTotal = ref(0)

// 精选应用列表
const featuredAppsData = ref<API.AppVo[]>([])
const featuredAppsLoading = ref(false)
const featuredAppsPageNum = ref(1)
const featuredAppsPageSize = ref(8)
const featuredAppsTotal = ref(0)

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
      message.success('应用创建成功！')
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
      sortField: 'create_time',
      sortOrder: 'desc',
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
      sortField: 'create_time',
      sortOrder: 'desc',
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

// 精选应用分页变化
const handleFeaturedAppsPageChange = (page: number, pageSize: number) => {
  featuredAppsPageNum.value = page
  featuredAppsPageSize.value = pageSize
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
  max-width: 1600px;
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
  grid-template-columns: repeat(4, 1fr);
  gap: 24px;
  margin-bottom: 24px;
}


.pagination-container {
  display: flex;
  justify-content: center;
  margin-top: 24px;
}

/* 响应式设计 */
@media (max-width: 1200px) {
  .apps-grid {
    grid-template-columns: repeat(3, 1fr);
  }
}

@media (max-width: 900px) {
  .apps-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}

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
