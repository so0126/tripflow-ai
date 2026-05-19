<template>
  <div class="auth-wrapper">
    <div class="auth-card">
      <div class="spinner"></div>

      <div class="icon">✨</div>

      <h2>로그인 중입니다</h2>
      <p>계정을 안전하게 확인하고 있어요</p>
    </div>
  </div>
</template>

<script setup>
import { onMounted } from 'vue'
import { useRoute } from 'vue-router'
import router from '@/router'
import { useAuthStore } from '@/store/authStore'

const route = useRoute()
const authStore = useAuthStore()

onMounted(async () => {
  try {
    const encodedUserInfo = route.query.userInfo
    if (!encodedUserInfo) {
      await new Promise(r => setTimeout(r, 800))
      window.location.href = '/'
      return
    }

    const binary = atob(encodedUserInfo)
    const bytes = Uint8Array.from(binary, c => c.charCodeAt(0))
    const json = new TextDecoder('utf-8').decode(bytes)
    const userInfo = JSON.parse(json)

    // 🔥 기존 방식 유지
    authStore.setOAuthUser({
      id: userInfo.userId,
      email: userInfo.email,
      name: userInfo.name,
      picture: userInfo.picture,
      provider: userInfo.oauthProvider
    })

    // UX 딜레이
    await new Promise(r => setTimeout(r, 700))

    authStore.initializeAuth();
    // 강제 새로고침
    // window.location.href = '/'
    router.replace('/');

  } catch (e) {
    console.error(e)
    await new Promise(r => setTimeout(r, 1200))
    window.location.href = '/'
  }
})

</script>

<style scoped>
.auth-wrapper {
  height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #fffaf3;
}

.auth-card {
  background: #ffffff;
  padding: 2.5rem 3rem;
  border-radius: 1.25rem;
  text-align: center;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.08);
  animation: fadeUp 0.4s ease;
}

.icon {
  font-size: 2rem;
  margin-bottom: 0.5rem;
}

h2 {
  font-size: 1.2rem;
  font-weight: 600;
  color: #1b3b6f;
  margin-bottom: 0.25rem;
}

p {
  font-size: 0.9rem;
  color: #777;
}

/* Spinner */
.spinner {
  width: 36px;
  height: 36px;
  margin: 0 auto 1rem;
  border: 3px solid rgba(255, 145, 77, 0.25);
  border-top-color: #ff914d;
  border-radius: 50%;
  animation: spin 1s linear infinite;
}

/* Animations */
@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}

@keyframes fadeUp {
  from {
    opacity: 0;
    transform: translateY(8px);
  }

  to {
    opacity: 1;
    transform: translateY(0);
  }
}
</style>
