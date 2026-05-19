<template>
  <div class="offcanvas offcanvas-end" id="sidebar" tabindex="-1">
    <div class="offcanvas-header border-bottom">
      <h5 class="offcanvas-title fw-semibold">메뉴</h5>
      <button type="button" class="btn-close text-reset" data-bs-dismiss="offcanvas" aria-label="Close"></button>
    </div>

    <div class="offcanvas-body d-flex flex-column gap-2">
      <router-link
        v-for="item in menuItems"
        :key="item.name"
        :to="item.route"
        class="d-flex align-items-center gap-3 px-3 py-3 rounded nav-item text-decoration-none"
        :class="{ active: activeMenu === item.name }"
        @click="onSelect(item.name)"
        data-bs-dismiss="offcanvas"
      >
        <i :class="[item.icon, 'fs-3']"></i> 
        
        <h5 class="m-0 fw-bold">{{ item.label }}</h5>
      </router-link>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useAuthStore } from '@/store/authStore'
import * as bootstrap from 'bootstrap'
import 'bootstrap/dist/css/bootstrap.min.css'
import 'bootstrap-icons/font/bootstrap-icons.css'

const router = useRouter()
const route = useRoute()
const authStore = useAuthStore()

const allMenuItems = [
  { name: 'home', label: '홈', icon: 'bi bi-house-fill', route: '/' },
  { name: 'planner', label: '플래너', icon: 'bi bi-map-fill', route: '/planner/edit' },
  { name: 'supporter', label: '서포터', icon: 'bi bi-people', route: '/supporter' },
  { name: 'travelgram', label: '트래벌그램', icon: 'bi bi bi-instagram', route: '/travelgram' },
  { name: 'mypage', label: '마이페이지', icon: 'bi bi-person-circle', route: '/mypage', requiresAuth: true },
]

// 로그인 상태에 따라 메뉴 항목 필터링
const menuItems = computed(() => {
  if (!authStore.isLoggedIn) {
    return allMenuItems.filter(item => !item.requiresAuth)
  }
  return allMenuItems
})

// 현재 라우트에 따라 활성 메뉴 결정
const activeMenu = computed(() => {
  // 가장 긴 경로부터 확인 (더 구체적인 경로를 먼저 매칭)
  const sortedItems = [...menuItems.value].sort((a, b) => b.route.length - a.route.length)
  const currentItem = sortedItems.find(item => {
    if (item.route === '/') {
      return route.path === '/' // 홈은 정확히 일치해야 함
    }
    return route.path.startsWith(item.route)
  })
  return currentItem ? currentItem.name : null
})

let offcanvasInstance = null

function dedupeBackdrops() {
  const backdrops = Array.from(document.querySelectorAll('.offcanvas-backdrop'))
  // Bootstrap이 내부적으로 backdrop을 재활용하기 때문에 마지막 것만 남기고 제거
  if (backdrops.length > 1) {
    backdrops.slice(0, -1).forEach(el => el.remove())
  }
}

function cleanupBody() {
  document.body.classList.remove('modal-open')
  document.body.style.removeProperty('overflow')
  document.body.style.removeProperty('padding-right')
}

onMounted(() => {
  const sidebarEl = document.getElementById('sidebar')

  // 혹시 남아있는 인스턴스가 있다면 정리
  const existing = bootstrap.Offcanvas.getInstance(sidebarEl)
  if (existing) existing.dispose()

  // 새 인스턴스 한 번만 생성 (backdrop 없이)
  offcanvasInstance = new bootstrap.Offcanvas(sidebarEl, {
    backdrop: false,
    keyboard: true,
    scroll: false
  })

  // 열릴 때 중복 백드롭 제거
  sidebarEl.addEventListener('shown.bs.offcanvas', () => {
    setTimeout(dedupeBackdrops, 50)
  })

  // 닫힌 뒤 잔여 백드롭/스크롤 상태 정리
  sidebarEl.addEventListener('hidden.bs.offcanvas', () => {
    setTimeout(() => {
      dedupeBackdrops()
      cleanupBody()
    }, 150)
  })

  // 배경 클릭 시 닫기 (data-bs-toggle 미사용)
  document.addEventListener('click', (e) => {
    const backdrop = document.querySelector('.offcanvas-backdrop')
    if (backdrop && e.target === backdrop && sidebarEl.classList.contains('show')) {
      closeSidebar()
    }
  })
})

function openSidebar() {
  dedupeBackdrops() // 혹시 남은 잔여 백드롭 제거
  offcanvasInstance?.show()
}

function closeSidebar() {
  offcanvasInstance?.hide()
}

function onSelect(name) {
  const target = menuItems.value.find(item => item.name === name)
  
  // 마이페이지 접근 시 로그인 체크
  if (target?.requiresAuth && !authStore.isLoggedIn) {
    alert('로그인이 필요한 서비스입니다.')
    closeSidebar()
    return
  }
  
  if (target) {
    router.push(target.route)
  }
  closeSidebar()
}
</script>

<style scoped>
/* Offcanvas 배경 - 흰색 */
.offcanvas {
  background-color: #fff;
  box-shadow: -4px 0 12px rgba(0, 0, 0, 0.1);
}

/* Offcanvas Header */
.offcanvas-header {
  border-bottom-color: #e5e7eb !important;
}

.offcanvas-header h5 {
  color: #1b3b6f;
}

.offcanvas-header .btn-close {
  opacity: 0.6;
}

/* 기본 메뉴 아이템 - 흰색 배경에 남색 텍스트 */
.nav-item {
  color: #1b3b6f !important;
  text-decoration: none;
  transition: all 0.2s ease;
  background-color: transparent;
}

.nav-item i {
  color: #1b3b6f !important;
}

.nav-item h5 {
  color: #1b3b6f !important;
}

.nav-item:hover {
  background-color: #f3f4f6;
  color: #1b3b6f !important;
}

.nav-item:hover i,
.nav-item:hover h5 {
  color: #1b3b6f !important;
}

/* 활성화된 메뉴 - 남색 배경에 흰색 텍스트 */
.nav-item.active {
  background-color: #1b3b6f;
  color: #fff !important;
  box-shadow: 0 2px 8px rgba(27, 59, 111, 0.3);
}

.nav-item.active i {
  color: #fff !important;
}

.nav-item.active h5 {
  color: #fff !important;
}

/* backdrop 완전히 숨김 */
.offcanvas-backdrop {
  display: none !important;
}
</style>
