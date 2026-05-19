<template>
  <div id="app">
    <div class="layout-container" :class="{ 
      'is-planner-page': route.path.includes('planner'),
      'is-landing-page': isLandingPage,
      'is-supporter-page': route.path.includes('supporter')
    }">
      
      <AppHeader v-if="!isLandingPage" @toggle-sidebar="isSidebarOpen = true" />

      <SideBar :isOpen="isSidebarOpen" @close="isSidebarOpen = false" />

      <div class="app-body-row">
        
        <aside v-if="showRightSidebar" class="sidebar-area right">
          <WeatherCard />
          <Checklist />
        </aside>

        <aside v-if="showLeftSidebar" class="sidebar-area left chat-floating">
          <ChatSidebar />
        </aside>

        <div class="main-content">
          <main class="page-container">
            <RouterView />
          </main>
        </div>

      </div>

      <AppFooter v-if="!isLandingPage" />

    </div>
  </div>
</template>

<script setup>
import { ref, watch, computed, onMounted } from "vue";
import { useRoute } from "vue-router";
import AppHeader from "./components/AppHeader.vue";
import AppFooter from "./components/AppFooter.vue";
import SideBar from "./components/Menubar.vue";
import ChatSidebar from "./components/ChatSidebar.vue";
import Checklist from "./components/supporter/Checklist.vue";
import WeatherCard from "./components/supporter/WeatherCard.vue";
import { useAuthStore } from "@/store/authStore"
import "bootstrap/dist/css/bootstrap.min.css";
import "bootstrap/dist/js/bootstrap.bundle.min.js";
import { useTravelStore } from "./store/travelStore";

const route = useRoute();
const authStore = useAuthStore();
const travelStore = useTravelStore();
const isSidebarOpen = ref(false);
watch(isSidebarOpen, (v) => {
  document.body.style.overflow = v ? "hidden" : "";
});

// 랜딩페이지 여부 확인
const isLandingPage = computed(() => route.path === '/');

// 사이드바 표시 조건
const showLeftSidebar = computed(() => route.path.includes('planner') && !isLandingPage.value);
const showRightSidebar = computed(() => route.path.includes('supporter'));

// OAuth 로직 (그대로 유지)
onMounted(() => {
  authStore.initializeAuth();

  if (authStore.userId != null) {
    travelStore.initializeTravelInfo(authStore.userId);
  }

  // 📌 Footer 감지: 채팅이 footer를 침범하지 않도록 관리
  // if (route.path.includes('planner')) {
  //   setupChatFooterDetection();
  // }
});

// Footer 감지 함수
const setupChatFooterDetection = () => {
  const chat = document.querySelector('.chat-floating');
  const footer = document.querySelector('footer');
  const layoutContainer = document.querySelector('.layout-container');

  if (!chat || !footer || !layoutContainer) return;

  const updateChatPosition = () => {
    const footerRect = footer.getBoundingClientRect();
    const chatHeight = chat.offsetHeight || 400;
    
    // footer가 화면에 보이기 시작했는지 확인
    if (footerRect.top < window.innerHeight) {
      // footer 위에 채팅을 배치 (fixed)
      chat.classList.add('chat-above-footer');
      
      // layout-container의 왼쪽 위치 계산
      const containerRect = layoutContainer.getBoundingClientRect();
      chat.style.left = `${containerRect.left}px`;
      
      const topPosition = footerRect.top - chatHeight - 16;
      chat.style.top = `${Math.max(64, topPosition)}px`;
    } else {
      // footer가 안 보이면: sticky로 복구
      chat.classList.remove('chat-above-footer');
      chat.style.top = '64px';
      chat.style.left = '0';
    }
  };

  // 초기 설정
  updateChatPosition();

  // scroll 이벤트로 지속적으로 업데이트
  window.addEventListener('scroll', updateChatPosition);
  
  // cleanup
  return () => window.removeEventListener('scroll', updateChatPosition);
};
</script>

<style scoped>
/* App.vue 내에서 직접 import 하거나 
  vite.config.js에서 전역으로 설정
*/
</style>

<style>
/* Landing 페이지: 헤더/푸터 없이 전체 화면 사용 */
.is-landing-page {
  margin: 0 !important;
  padding: 0 !important;
  border-radius: 0 !important;
  box-shadow: none !important;
  max-width: 100% !important;
  min-height: 100vh !important;
  background: transparent !important;
}

.is-landing-page .app-body-row {
  overflow: visible !important;
}

.is-landing-page .main-content {
  padding: 0 !important;
  overflow: visible !important;
}

.is-landing-page .page-container {
  padding: 0 !important;
  max-width: 100% !important;
  margin: 0 !important;
}

/* Planner 페이지: 스크롤 컨테이너 설정 및 여백 제거 */
.is-planner-page {
  overflow: visible !important;
  max-width: 100% !important;
  margin: 0 !important;
  border-radius: 0 !important;
  padding-top: 0 !important;
}

.is-planner-page .app-body-row {
  overflow: visible !important;
  align-items: flex-start !important;
  gap: 0 !important;
  margin-top: 72px;
}

.is-planner-page .main-content {
  overflow: visible !important;
  padding: 0 !important;
}

.is-planner-page .page-container {
  padding: 0 !important;
}

.is-planner-page .sidebar-area {
  overflow-y: visible !important;
}

.is-planner-page .sidebar-area.left {
  padding: 0 !important;
  border-right: none !important;
}

/* 채팅: sticky로 스크롤 따라오기 */
.is-planner-page .sidebar-area.left.chat-floating {
  position: sticky !important;
  top: 64px !important;
  height: calc(100vh - 64px) !important;
  max-height: calc(100vh - 64px) !important;
  overflow-y: auto !important;
  overflow-x: hidden !important;
  align-self: flex-start !important;
  z-index: 50 !important;
}

/* Supporter 페이지: 전체 너비 사용, 여백 제거 */
.is-supporter-page {
  max-width: 100% !important;
  margin: 0 !important;
  border-radius: 0 !important;
  padding: 0 !important;
}

.is-supporter-page .app-body-row {
  width: 100% !important;
  max-width: 100% !important;
  padding: 0 !important;
  margin: 0 !important;
  margin-top: 72px !important;
  align-items: flex-start !important;
}

.is-supporter-page .main-content {
  width: 100% !important;
  max-width: 100% !important;
  padding: 0 !important;
  margin: 0 !important;
}

.is-supporter-page .page-container {
  width: 100% !important;
  max-width: 100% !important;
  padding: 0 !important;
  margin: 0 !important;
}

.is-supporter-page .sidebar-area.right {
  margin-top: 0 !important;
  padding-top: 1rem !important;
  order: -1 !important;
}
</style>