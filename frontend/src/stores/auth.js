import { defineStore } from 'pinia'
import { ref } from 'vue'
import router from '../router'

export const useAuthStore = defineStore('auth', () => {
  const user = ref(null)
  const token = ref('')

  function setUser(u) { user.value = u }
  function setToken(t) { token.value = t }

  function logout() {
    user.value = null
    token.value = ''
    router.push('/login')
  }

  return { user, token, setUser, setToken, logout }
})
