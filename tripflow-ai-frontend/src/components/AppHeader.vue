<template>
  <nav ref="navbar" class="navbar navbar-fms fixed-top">
    <div class="container-fluid d-flex justify-content-between align-items-center px-3">
      <router-link to="/" class="navbar-brand d-flex align-items-center text-decoration-none">
        <img src="@/assets/img/logo-bg-rm.png" alt="Find My Seoul" class="logo-img" />
      </router-link>

      <div class="d-flex align-items-center gap-3">

        <transition name="login-fade">
          <div v-if="isLoggedIn" class="login-info d-flex align-items-center gap-2">
            <span class="user-info">{{ userName || 'User' }}</span>

            <button @click="onLogoutClick" class="logout-btn" title="Logout">
              로그아웃
            </button>
          </div>
        </transition>

        <a v-if="!isLoggedIn" href="http://localhost:8080/oauth2/authorization/google"
          class="login-button" title="Login with Google OAuth">
          <i class="bi bi-box-arrow-in-right"></i>
          <span>로그인</span>
        </a>

        <img v-else :src="userProfileImage || defaultProfileImg" alt="Profile" class="profile-img-logged-in" />

        <button class="btn text-white fs-4 border-0 p-2" type="button" data-bs-toggle="offcanvas"
          data-bs-target="#sidebar" aria-controls="sidebar">
          <i class="bi bi-list"></i>
        </button>
      </div>
    </div>
  </nav>
</template>

<script setup>
import { ref, onMounted, onBeforeUnmount } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/store/authStore'
import { storeToRefs } from 'pinia'

// ✅ 기본 이미지 import (템플릿에서 쓰기 위해)
import defaultProfileImg from '@/assets/img/profile-logo.png'

const router = useRouter()
const navbar = ref(null)

const authStore = useAuthStore()

// isLoggedIn, userName, userProfileImage가 실시간으로 바뀝니다.
const { isLoggedIn, userName, userProfileImage } = storeToRefs(authStore)

const onLogoutClick = () => {
  // Store의 로그아웃 액션 실행 (데이터 비우기)
  authStore.logout();

  console.log('✅ 로그아웃 완료')

  // 메인으로 이동
  router.push('/logout');
  // 또는 window.location.href = '/' (새로고침이 필요하다면 이것 사용)
}

const handleScroll = () => {
  if (window.scrollY > 20) navbar.value?.classList.add('scrolled')
  else navbar.value?.classList.remove('scrolled')
}

onMounted(() => window.addEventListener('scroll', handleScroll))
onBeforeUnmount(() => window.removeEventListener('scroll', handleScroll))
</script>

<style scoped>
.navbar-fms {
  background-color: rgba(255, 255, 255, 0.98);
  backdrop-filter: blur(12px);
  -webkit-backdrop-filter: blur(12px);
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
  padding: 0 2rem;
  height: 72px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  box-shadow: none;
  font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, "Helvetica Neue", Arial, "Noto Sans KR", sans-serif;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
  border-bottom: 1px solid rgba(26, 42, 86, 0.08);
  z-index: 1000;

  .navbar-brand {
    display: flex;
    align-items: center;
    justify-content: center;
    height: 100%;
    transition: all 0.3s ease;

    img {
      height: 52px;
      object-fit: contain;
      filter: none;
      transition: all 0.4s ease;
    }

    &:hover img {
      transform: scale(1.05);
    }
  }

  .btn i {
    color: #2d4a8f;
    font-size: 1.8rem;
    transition: all 0.3s ease;
  }

  /* 🎨 스크롤 시 남색 그라데이션 배경 */
  &.scrolled {
    background: linear-gradient(135deg, #1a2a56 0%, #2d4a8f 100%);
    box-shadow: 0 4px 25px rgba(26, 42, 86, 0.3);
    border-bottom: none;
    height: 68px;

    .navbar-brand img {
      filter: brightness(0) invert(1);
      height: 48px;
    }

    .btn i {
      color: #ffffff;
    }

    .user-info {
      color: rgba(255, 255, 255, 0.95);
      text-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
    }

    .logout-btn {
      background-color: rgba(255, 255, 255, 0.95);
      color: #1a2a56;
      border-color: transparent;
      font-weight: 600;
    }

    .logout-btn:hover {
      background-color: white;
      transform: translateY(-1px);
      box-shadow: 0 2px 8px rgba(255, 255, 255, 0.3);
    }

    .login-button {
      background: rgba(255, 255, 255, 0.95);
      color: #1a2a56;
      box-shadow: 0 2px 12px rgba(255, 255, 255, 0.3);
      border: 2px solid transparent;
    }

    .login-button:hover {
      background: white;
      color: #2d4a8f;
      box-shadow: 0 4px 15px rgba(255, 255, 255, 0.4);
      transform: translateY(-2px);
    }

    .profile-img-logged-in {
      border-color: rgba(255, 255, 255, 0.9);
      box-shadow: 0 2px 8px rgba(0, 0, 0, 0.15);
    }
  }
}

.logo-img {
  height: 52px;
  object-fit: contain;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.logo-img:hover {
  transform: translateY(-1px);
  filter: brightness(1.05);
}

.profile-img {
  width: 44px;
  height: 44px;
  border-radius: 50%;
  object-fit: cover;
  cursor: pointer;
  border: 2px solid rgba(26, 42, 86, 0.2);
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
}

.profile-img:hover {
  transform: scale(1.08) translateY(-1px);
  border-color: #2d4a8f;
  box-shadow: 0 4px 12px rgba(45, 74, 143, 0.35);
}

/* 로그인 버튼 스타일 - 남색 테마 */
.login-button {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 12px 28px;
  background: linear-gradient(135deg, #1a2a56 0%, #2d4a8f 100%);
  color: white;
  text-decoration: none;
  border-radius: 50px;
  font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, "Helvetica Neue", Arial, sans-serif;
  font-weight: 600;
  font-size: 1rem;
  letter-spacing: 0.3px;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  box-shadow: 0 2px 12px rgba(26, 42, 86, 0.35);
  border: none;
  cursor: pointer;
}

.login-button i {
  font-size: 1.1rem;
  transition: transform 0.3s ease;
}

.login-button:hover {
  background: linear-gradient(135deg, #2d4a8f 0%, #4a6bb5 100%);
  box-shadow: 0 4px 20px rgba(26, 42, 86, 0.5);
  transform: translateY(-2px);
  color: white;
}

.login-button:hover i {
  transform: translateX(2px);
}

.login-button:active {
  transform: translateY(0);
  box-shadow: 0 2px 8px rgba(26, 42, 86, 0.4);
}

.profile-btn {
  background: none !important;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 0 !important;
  border-radius: 50%;
}

.profile-btn:hover {
  background-color: rgba(255, 255, 255, 0.2) !important;
}

.btn {
  background: none !important;
  display: flex;
  align-items: center;
  justify-content: center;
  height: 44px;
  width: 44px;
  border-radius: 12px;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.btn i {
  font-size: 24px;
  color: #2c3e50;
  transition: color 0.3s ease;
}

.btn:hover {
  background-color: rgba(26, 42, 86, 0.05) !important;
  transform: translateY(-1px);
}

.btn:hover i {
  color: #2d4a8f;
}

.btn:active {
  transform: translateY(0);
}

.nav-item {
  font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, "Helvetica Neue", Arial, sans-serif;
  background: linear-gradient(135deg, #1a2a56 0%, #2d4a8f 100%);
  color: #fff !important;
  text-decoration: none;
  font-weight: 600;
  font-size: 1rem;
  letter-spacing: 0.3px;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  border-radius: 12px;
  padding: 12px 20px;

  &:hover {
    background: linear-gradient(135deg, #2d4a8f 0%, #4a6bb5 100%);
    transform: translateY(-2px);
    box-shadow: 0 4px 12px rgba(26, 42, 86, 0.35);
  }

  &:active {
    transform: translateY(0);
  }
}

.nav-item.active {
  background: #ffffff;
  color: #1a2a56 !important;
  box-shadow: 0 0 0 2px #2d4a8f;
  font-weight: 700;
}

.nav-item.active:hover {
  background: #f8f9fa;
  transform: translateY(0);
}

.offcanvas-header h5 {
  font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, "Helvetica Neue", Arial, sans-serif;
  color: #2c3e50;
  font-weight: 700;
  font-size: 1.25rem;
  letter-spacing: 0.3px;
}

/* 로그인 정보 */
.login-info {
  padding: 0 12px;
  display: flex;
  align-items: center;
  gap: 14px;
}

.user-info {
  font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, "Helvetica Neue", Arial, sans-serif;
  font-weight: 600;
  font-size: 1.1rem;
  white-space: nowrap;
  color: #2c3e50;
  letter-spacing: 0.2px;
}

/* 로그아웃 버튼 - 남색 테마 */
.logout-btn {
  font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, "Helvetica Neue", Arial, sans-serif;
  font-size: 0.95rem;
  font-weight: 600;
  padding: 8px 20px;
  border-radius: 50px;
  border: 2px solid #2d4a8f;
  color: #2d4a8f;
  background-color: transparent;
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  display: inline-flex;
  align-items: center;
  height: auto;
  width: auto;
  letter-spacing: 0.3px;
}

.logout-btn:hover {
  background: linear-gradient(135deg, #1a2a56 0%, #2d4a8f 100%);
  color: white;
  border-color: transparent;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(26, 42, 86, 0.35);
}

.logout-btn:active {
  transform: translateY(0);
  box-shadow: 0 2px 6px rgba(26, 42, 86, 0.25);
}

.profile-img-logged-in {
  width: 44px;
  height: 44px;
  border-radius: 50%;
  object-fit: cover;
  border: 2px solid rgba(26, 42, 86, 0.2);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  cursor: pointer;
}

.profile-img-logged-in:hover {
  border-color: #2d4a8f;
  box-shadow: 0 4px 12px rgba(45, 74, 143, 0.35);
  transform: scale(1.08) translateY(-1px);
}

/* 로그인 영역 등장 애니메이션 */
.login-fade-enter-active,
.login-fade-leave-active {
  transition: all 0.25s ease;
}

.login-fade-enter-from {
  opacity: 0;
  transform: translateY(-6px);
}

.login-fade-enter-to {
  opacity: 1;
  transform: translateY(0);
}

.login-fade-leave-from {
  opacity: 1;
  transform: translateY(0);
}

.login-fade-leave-to {
  opacity: 0;
  transform: translateY(-6px);
}
</style>