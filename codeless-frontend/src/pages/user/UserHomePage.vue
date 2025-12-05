<template>
  <div id="userHomePage">
     <a-layout class="userPageLayout">
       <a-layout-sider>
         <div class="logo" />
         <a-menu v-model:selectedKeys="selectedKeys" theme="dark" mode="inline">
           <a-menu-item key="1" @click="() => router.push('/user/home')">
             <user-outlined />
             <span class="nav-text">主页</span>
           </a-menu-item>
           <a-menu-item key="2" @click="() => router.push('/user/edit')">
             <FormOutlined />
             <span class="nav-text">编辑信息</span>
           </a-menu-item>
         </a-menu>
       </a-layout-sider>
       <a-layout-content :style="{ margin: '24px 16px 0', overflow: 'initial' }">
         <template v-if="route.path === '/user/home'">
           <div :style="{ padding: '24px', background: '#fff', width: '100%'}">
             <a-card style="width: 100%"
                     title="用户信息"
             >
               <a-card-meta
                 :title="loginUserStore.loginUser.username"
                 :description="loginUserStore.loginUser.userIntro"
               >
                 <template #avatar>
                   <a-avatar :src="loginUserStore.loginUser.avatarUrl" size="large"/>
                 </template>
               </a-card-meta>
               <template #extra>
                 <a-button href="/user/edit">修改信息</a-button>
               </template>
             </a-card>
           </div>
         </template>
         <template v-if="route.path === '/user/edit'">
           <UserEditPage />
         </template>
       </a-layout-content>
     </a-layout>
  </div>
</template>
<script lang="ts" setup>
import { ref } from 'vue';
import {
  UserOutlined,
  FormOutlined,
} from '@ant-design/icons-vue';
import { useLoginUserStore } from '@/stores/loginUser.ts'
import { useRoute, useRouter } from 'vue-router'
import UserEditPage from '@/pages/user/UserEditPage.vue'

const route =  useRoute();
const router = useRouter();
const selectedKeys = ref<string[]>(['1']);

const loginUserStore = useLoginUserStore();
</script>
<style>
#components-layout-demo-fixed-sider .logo {
  height: 32px;
  background: rgba(255, 255, 255, 0.2);
  margin: 16px;
}
.site-layout .site-layout-background {
  background: #fff;
}

[data-theme='dark'] .site-layout .site-layout-background {
  background: #141414;
}
#userHomePage {
  width: 960px;
}
.userPageLayout {
  min-height: 750px
}
</style>
