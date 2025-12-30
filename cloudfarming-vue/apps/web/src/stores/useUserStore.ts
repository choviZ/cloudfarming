import { ref, computed } from 'vue'
import { defineStore } from 'pinia'
import type { UserRespDTO } from '@/types/userType.ts'
import { getUser } from '@/api/user.ts'

export const useUserStore = defineStore('useUserStore', () => {

  // 用户信息
  const loginUser = ref<UserRespDTO>()

  function setUser(user: UserRespDTO){
    loginUser.value = user
  }

  function clearUser() {
    loginUser.value = undefined
  }

  // 获取用户信息
  async function fetchUser() {
    const res = await getUser()
    if (res.code === '0' && res.data) {
      setUser(res.data)
    } else {
      clearUser()
    }
  }

  return { loginUser,setUser,fetchUser,clearUser }
})
