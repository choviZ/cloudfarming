import { defineStore } from 'pinia'
import { ref } from 'vue'
import { getUser } from '@cloudfarming/core'
import type { UserRespDTO } from '@cloudfarming/core'

const useUserStore = defineStore('useUserStore', () => {

  // 用户信息
  const loginUser = ref<UserRespDTO>()

  function setUser(user: UserRespDTO) {
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

  return { loginUser, setUser, fetchUser, clearUser }
})

export default useUserStore
