<template>
  <a-layout-header class="global-header">
    <div class="header-content">
      <!-- 左侧：Logo 和标题 -->
      <div class="header-left">
        <img alt="Logo" class="logo" src="@/assets/logo.png" />
        <span class="site-title">CodeLess</span>
        <!-- 菜单项 -->
        <a-menu
          v-model:selectedKeys="selectedKeys"
          mode="horizontal"
          :items="menuItems"
          class="header-menu"
          @click="handleMenuClick"
        />
      </div>

      <!-- 右侧：用户信息或登录按钮 -->
      <div class="header-right">
        <template v-if="isLoggedIn">
          <a-dropdown>
            <a-space class="user-info">
              <a-avatar :size="32" :src="userAvatar" />
              <span class="user-name">{{ userName }}</span>
            </a-space>
            <template #overlay>
              <a-menu>
                <a-menu-item key="profile">个人中心</a-menu-item>
                <a-menu-item key="settings">设置</a-menu-item>
                <a-menu-divider />
                <a-menu-item key="logout" @click="handleLogout">退出登录</a-menu-item>
              </a-menu>
            </template>
          </a-dropdown>
        </template>
        <a-button v-else type="primary" @click="handleLogin">登录</a-button>
      </div>
    </div>
  </a-layout-header>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import type { MenuProps } from 'ant-design-vue'

// 菜单配置
const menuItems = ref<MenuProps['items']>([
  {
    key: '/',
    label: '首页',
  },
  {
    key: '/about',
    label: '关于',
  },
])

// 路由
const router = useRouter()
const route = useRoute()

// 当前选中的菜单项
const selectedKeys = computed(() => [route.path])

// 用户登录状态（这里使用 ref，实际应该从 store 或 API 获取）
const isLoggedIn = ref(false)
const userName = ref('用户名')
const userAvatar = ref('')

// 菜单点击处理
const handleMenuClick: MenuProps['onClick'] = (e) => {
  router.push(e.key as string)
}

// 登录处理
const handleLogin = () => {
  // TODO: 实现登录逻辑
  console.log('登录')
}

// 退出登录处理
const handleLogout = () => {
  isLoggedIn.value = false
  // TODO: 实现退出登录逻辑
  console.log('退出登录')
}
</script>

<style scoped>
.global-header {
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
  -webkit-backdrop-filter: blur(10px);
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.08), 0 1px 2px rgba(0, 0, 0, 0.06);
  padding: 0;
  height: 64px;
  line-height: 64px;
  position: sticky;
  top: 0;
  z-index: 1000;
  border-bottom: 1px solid rgba(0, 0, 0, 0.05);
}

.header-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
  max-width: 1600px;
  margin: 0 auto;
  padding: 0 24px;
  height: 100%;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 16px;
  flex: 1;
}

.logo {
  width: 45px;
  height: 45px;
  object-fit: contain;
  transition: transform 0.3s ease;
}

.logo:hover {
  transform: scale(1.05);
}

.site-title {
  font-size: 24px;
  font-weight: 600;
  color: #1e293b;
  white-space: nowrap;
  letter-spacing: -0.5px;
}

.header-menu {
  flex: 1;
  font-weight: 400;
  border-bottom: none;
  margin-left: 24px;
  background: transparent;
}

.header-menu :deep(.ant-menu-item) {
  color: #64748b;
  font-size: 15px;
  padding: 0 16px;
  margin: 0 4px;
  border-radius: 6px;
  transition: all 0.2s ease;
}

.header-menu :deep(.ant-menu-item:hover) {
  color: #1890ff;
  background: rgba(24, 144, 255, 0.08);
}

.header-menu :deep(.ant-menu-item-selected) {
  color: #1890ff;
  background: rgba(24, 144, 255, 0.1);
  font-weight: 500;
}

.header-menu :deep(.ant-menu-item-selected::after) {
  display: none;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 12px;
}

.user-info {
  cursor: pointer;
  padding: 4px 12px;
  border-radius: 8px;
  transition: all 0.2s ease;
}

.user-info:hover {
  background-color: rgba(0, 0, 0, 0.04);
}

.user-name {
  font-size: 14px;
  color: #334155;
  font-weight: 500;
}

.header-right :deep(.ant-btn-primary) {
  height: 36px;
  padding: 0 20px;
  border-radius: 8px;
  font-weight: 500;
  box-shadow: 0 2px 4px rgba(24, 144, 255, 0.2);
  transition: all 0.2s ease;
}

.header-right :deep(.ant-btn-primary:hover) {
  transform: translateY(-1px);
  box-shadow: 0 4px 8px rgba(24, 144, 255, 0.3);
}

/* 响应式设计 */
@media (max-width: 768px) {
  .header-content {
    padding: 0 16px;
  }

  .site-title {
    font-size: 18px;
  }

  .header-menu {
    margin-left: 12px;
  }

  .header-menu :deep(.ant-menu-item) {
    padding: 0 12px;
    font-size: 14px;
  }

  .user-name {
    display: none;
  }
}
</style>

