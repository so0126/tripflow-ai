import { createRouter, createWebHistory } from 'vue-router'

// 1. 필요한 페이지 컴포넌트 import (메인 페이지용 하나만 남김)
import CreatePlan from '@/views/planner/CreatePlan.vue'
import OAuthCallback from '@/views/auth/OAuthCallback.vue'

// 2. 하위 모듈 라우트 import
import travelgram from './travelgram'
import planner from './planner'
import supporter from './supporter'
import mypage from './mypage'
import plannerApi from '@/api/plannerApi'
import { useAuthStore } from '@/store/authStore'
import LogoutCallback from '@/views/auth/LogoutCallback.vue'

// import { useTravelStore } from '@/store/travelStore' // 필요 시 주석 해제

// const authStore = useAuthStore();

const routes = [
  // ✅ 1. 메인 경로 명확화
  // (supporter.js에 있는 '/' 리다이렉트와 충돌나지 않게 여기서 확실히 정의하거나, supporter.js에서 '/'를 지워야 함)
  {
    path: '/',
    name: 'Home',
    component: CreatePlan,
    meta: {
      forbidIfHasPlan: true
    }
  },

  // ✅ 2. OAuth 콜백 등 전역적으로 필요한 특수 라우트
  { path: '/oauth/callback', component: OAuthCallback },
  { path: '/logout', name: 'Logout', component: LogoutCallback },

  // ✅ 3. 하위 모듈 확장 (여기서 모든 세부 경로를 담당)
  // 중복 선언했던 /travelgram, /supporter, /planner/hotel 등은 다 지움
  ...planner,
  ...supporter,
  ...travelgram,
  ...mypage,
]

const router = createRouter({
  history: createWebHistory(),
  routes,
  scrollBehavior() {
    return { top: 0 };
  }
});

router.beforeEach(async (to, from, next) => {
  const authStore = useAuthStore();
  
  // 마이페이지 접근 시 로그인 체크
  if (to.meta.requiresAuth && !authStore.isLoggedIn) {
    alert('로그인이 필요한 서비스입니다.')
    next('/')
    return
  }
  
  if (to.path === '/') {
    if (authStore.userId != null) {
      const res = await plannerApi.getActivePlan(authStore.userId)
      const plan = res?.data?.data?.plan

      if (plan && plan.id) {
        // 활성 일정 존재 → 홈 접근 차단
        next('/planner/edit')
        return
      }
    }
  }

  next()
})

export default router