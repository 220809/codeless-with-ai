<template>
  <a-card class="app-card" :hoverable="true">
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
            @click.stop="handleViewDeploy"
          >
            查看作品
          </a-button>
          <a-button
            class="cover-btn view-chat-btn"
            @click.stop="handleViewChat"
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
        <div v-if="showAuthor" class="app-info">
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
        <div v-else class="app-time">
          {{ formatTime(app.createTime) }}
        </div>
      </template>
    </a-card-meta>
  </a-card>
</template>

<script setup lang="ts">
import { FileImageOutlined } from '@ant-design/icons-vue'
import { formatRelativeTime } from '@/utils/date.ts'
import { useRouter } from 'vue-router'

const props = defineProps<{
  app: API.AppVo
  showAuthor?: boolean
  formatTime?: (time?: string) => string
}>()

const emits = defineEmits<{
  viewDeploy: [app: API.AppVo]
  viewChat: [app: API.AppVo]
}>()

const router = useRouter()

const formatTime = props.formatTime || formatRelativeTime

const handleViewDeploy = () => {
  if (props.app.deployKey) {
    emits('viewDeploy', props.app)
  }
}

const handleViewChat = () => {
  emits('viewChat', props.app)
}
</script>

<style scoped>
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
</style>

