import { ref, computed } from 'vue'
import { defineStore } from 'pinia'
import { getCurrentUser } from '@/api/user.ts'

export const useLoginUserStore = defineStore('loginUser', () => {
  const loginUser = ref<API.LoginUserVo>({
    username: '请先登录',
  });

  async function fetchLoginUser() {
    const res = await getCurrentUser();
    if (res.data.code === 200 && res.data.data) {
      loginUser.value = res.data.data;
    }
  }

  function setLoginUser(newUser: API.LoginUserVo) {
    loginUser.value = newUser;
  }

  return { loginUser, fetchLoginUser, setLoginUser }
})
