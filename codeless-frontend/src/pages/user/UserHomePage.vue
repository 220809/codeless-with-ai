<template>
  <div id="userHomePage">
     <a-layout class="userPageLayout">
       <a-layout-sider class="user-sider" width="240">
         <div class="logo">
           {{'个人中心'}}
         </div>
         <a-menu
           v-model:selectedKeys="selectedKeys"
           theme="light"
           mode="inline"
           class="user-menu"
         >
           <a-menu-item key="/user/home" @click="() => router.push('/user/home')">
             <template #icon><UserOutlined /></template>
             <span class="nav-text">首页</span>
           </a-menu-item>
           <a-menu-item key="/user/edit" @click="() => router.push('/user/edit')">
             <template #icon><FormOutlined /></template>
             <span class="nav-text">编辑信息</span>
           </a-menu-item>
         </a-menu>
       </a-layout-sider>
       <a-layout-content class="user-content">
         <div class="content-wrapper">
           <template v-if="route.path === '/user/home'">
             <a-card class="info-card" :bordered="false">
               <template #title>
                 <span class="card-title">个人信息</span>
               </template>
               <div class="user-info">
                 <div class="info-header">
                   <a-avatar :src="loginUserStore.loginUser.avatarUrl" :size="120" class="user-avatar" />
                   <div class="info-basic">
                     <h2 class="username">{{ loginUserStore.loginUser.username || '未设置用户名' }}</h2>
                     <p class="user-intro">{{ loginUserStore.loginUser.userIntro || '这个人很懒，什么都没有留下~' }}</p>
                   </div>
                 </div>
                 <a-divider />
                 <div class="info-details">
                   <div class="info-item">
                     <span class="info-label">账号：</span>
                     <span class="info-value">{{ loginUserStore.loginUser.userAccount || '-' }}</span>
                   </div>
                   <div class="info-item">
                     <span class="info-label">性别：</span>
                     <span class="info-value">{{ genderText }}</span>
                   </div>
                   <div class="info-item">
                     <span class="info-label">角色：</span>
                     <span class="info-value">{{ USER_ROLE_TEXT[loginUserStore.loginUser.userRole ?? 0] || '用户' }}</span>
                   </div>
                 </div>
                 <div class="info-actions">
                   <a-button type="primary" @click="() => router.push('/user/edit')">
                     <template #icon><FormOutlined /></template>
                     编辑信息
                   </a-button>
                 </div>
               </div>
             </a-card>
           </template>
           <template v-if="route.path === '/user/edit'">
             <a-card class="edit-card" :bordered="false">
               <template #title>
                 <span class="card-title">编辑信息</span>
               </template>
               <UserEditPage :edit-user="loginUserStore.loginUser" @close-modal="handleEditClose" />
             </a-card>
           </template>
         </div>
       </a-layout-content>
     </a-layout>
  </div>
</template>
<script lang="ts" setup>
import { computed } from 'vue'
import {
  UserOutlined,
  FormOutlined,
} from '@ant-design/icons-vue';
import { useLoginUserStore } from '@/stores/loginUser.ts'
import { useRoute, useRouter } from 'vue-router'
import { USER_ROLE_TEXT, GENDER_TEXT } from '@/utils/constants.ts'
import UserEditPage from '@/pages/user/UserEditPage.vue'

const route =  useRoute();
const router = useRouter();

const selectedKeys = computed(() => ([route.path]));

const loginUserStore = useLoginUserStore();

const genderText = computed(() => {
  return GENDER_TEXT[loginUserStore.loginUser.gender ?? 0] || '未知'
});

const handleEditClose = () => {
  // 在个人中心页面，关闭编辑时刷新用户信息并返回首页
  loginUserStore.fetchLoginUser();
  router.push('/user/home');
};
</script>
<style scoped>
#userHomePage {
  min-height: calc(100vh - 64px);
  background: #f0f2f5;
}

.userPageLayout {
  min-height: calc(100vh - 64px);
  background: #f0f2f5;
}

.user-sider {
  background: #fff;
  box-shadow: 2px 0 8px rgba(0, 0, 0, 0.06);
}

.logo {
  padding: 24px;
  text-align: center;
  border-bottom: 1px solid #f0f0f0;
  color: #aaaaaa;
}

.user-menu {
  padding: 8px;
}

.user-menu :deep(.ant-menu-item) {
  border-radius: 6px;
  height: 48px;
  line-height: 48px;
}

.user-menu :deep(.ant-menu-item-selected) {
  background: #e6f7ff;
  color: #1890ff;
}

.user-menu :deep(.ant-menu-item-selected::after) {
  display: none;
}

.user-menu :deep(.ant-menu-item:hover) {
  background: #f5f5f5;
}

.nav-text {
  font-size: 14px;
  font-weight: 500;
}

.user-content {
  padding: 24px;
  width: 720px !important;
  background: #f0f2f5;
}

.content-wrapper {
  max-width: 900px;
  margin: 0 auto;
}

.info-card,
.edit-card {
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  border-radius: 8px;
}

.card-title {
  font-size: 18px;
  font-weight: 600;
  color: #262626;
}

.user-info {
  padding: 8px 0;
}

.info-header {
  display: flex;
  align-items: flex-start;
  gap: 24px;
  margin-bottom: 24px;
}

.user-avatar {
  flex-shrink: 0;
  border: 3px solid #f0f0f0;
}

.info-basic {
  flex: 1;
}

.username {
  margin: 0 0 12px 0;
  font-size: 24px;
  font-weight: 600;
  color: #262626;
}

.user-intro {
  margin: 0;
  font-size: 14px;
  color: #8c8c8c;
  line-height: 1.6;
}

.info-details {
  display: flex;
  flex-direction: column;
  gap: 16px;
  margin: 24px 0;
}

.info-item {
  display: flex;
  align-items: center;
  font-size: 14px;
}

.info-label {
  width: 80px;
  color: #8c8c8c;
  font-weight: 500;
}

.info-value {
  color: #262626;
  flex: 1;
}

.info-actions {
  margin-top: 24px;
  padding-top: 24px;
  border-top: 1px solid #f0f0f0;
}

:deep(.ant-divider) {
  margin: 24px 0;
}

:deep(.ant-card-head) {
  border-bottom: 1px solid #f0f0f0;
  padding: 16px 24px;
}

:deep(.ant-card-body) {
  padding: 24px;
}
</style>
