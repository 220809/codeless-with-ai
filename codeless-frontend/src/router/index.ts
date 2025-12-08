import { createRouter, createWebHistory } from 'vue-router'
import HomePage from '@/pages/HomePage.vue'
import AdminUserPage from '@/pages/admin/AdminUserPage.vue'
import AdminAppPage from '@/pages/admin/AdminAppPage.vue'
import AppEditPage from '@/pages/admin/AppEditPage.vue'
import AppChatPage from '@/pages/app/AppChatPage.vue'
import UserLoginPage from '@/pages/user/UserLoginPage.vue'
import UserRegisterPage from '@/pages/user/UserRegisterPage.vue'
import UserHomePage from '@/pages/user/UserHomePage.vue'
import UserEditPage from '@/pages/user/UserEditPage.vue'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      name: 'home',
      component: HomePage,
    },
    {
      path: '/about',
      name: 'about',
      // route level code-splitting
      // this generates a separate chunk (About.[hash].js) for this route
      // which is lazy-loaded when the route is visited.
      component: () => import('@/pages/HomePage.vue'),
    },
    {
      path: '/admin/user',
      name: 'admin_user',
      component: AdminUserPage,
    },
    {
      path: '/admin/app',
      name: 'admin_app',
      component: AdminAppPage,
    },
    {
      path: '/admin/app/edit',
      name: 'admin_app_edit',
      component: AppEditPage,
    },
    {
      path: '/app/edit',
      name: 'app_edit',
      component: AppEditPage,
    },
    {
      path: '/app/chat',
      name: 'app_chat',
      component: AppChatPage,
    },
    {
      path: '/user/login',
      name: 'login',
      component: UserLoginPage,
    },
    {
      path: '/user/register',
      name: 'register',
      component: UserRegisterPage,
    },
    {
      path: '/user/home',
      name: 'user_home',
      component: UserHomePage,
    },
    {
      path: '/user/edit',
      name: 'user_edit',
      component: UserHomePage,
    },
  ],
})

export default router
