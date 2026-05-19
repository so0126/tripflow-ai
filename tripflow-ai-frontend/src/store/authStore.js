import { defineStore } from 'pinia'
import { ref, computed } from 'vue'

export const useAuthStore = defineStore('auth', () => {
  const user = ref(null)
  const isAuthenticated = ref(false)
  const isInitialized = ref(false)

  // getters
  const isLoggedIn = computed(() => isAuthenticated.value)
  const userName = computed(() => user.value?.name)
  const userEmail = computed(() => user.value?.email)
  const userProfileImage = computed(() => user.value?.picture)
  const userId = computed(() => user.value?.id)

  // OAuth 로그인 직후 호출
  const setOAuthUser = (oauthData) => {
    user.value = oauthData
    isAuthenticated.value = true
    isInitialized.value = true
    // try-catch로 감싸서 스토리지 접근 에러 방지
    try {
      localStorage.setItem('user', JSON.stringify(user.value)) //
    } catch (e) {
      console.warn('LocalStorage access denied (setOAuthUser):', e)
    }
  }

  // 로그아웃
  const logout = () => {
    user.value = null
    isAuthenticated.value = false
    isInitialized.value = true
   // try-catch로 감싸서 스토리지 접근 에러 방지
    try {
      localStorage.removeItem('jwtToken') //
      localStorage.removeItem('accessToken')
      localStorage.removeItem('user')
    } catch (e) {
      console.warn('LocalStorage access denied (logout):', e)
    }
  }

  // 새로고침 / 재접속 복구용
  const initializeAuth = () => {
    if (isInitialized.value) return

    try {
      const userStr = localStorage.getItem('user')
      if (userStr) {
        user.value = JSON.parse(userStr)
        isAuthenticated.value = true
      }
    } catch (e) {
      console.error('❌ Auth init failed', e)
      isAuthenticated.value = false
    } finally {
      isInitialized.value = true
    }
  }

  return {
    user,
    isAuthenticated,
    isLoggedIn,
    userName,
    userEmail,
    userProfileImage,
    userId,
    setOAuthUser,
    logout,
    initializeAuth
  }
})
