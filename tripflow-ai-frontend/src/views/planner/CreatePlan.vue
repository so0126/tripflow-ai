<template>
  <div class="landing-page">
    
    <!-- 플로팅 로그인/메뉴 영역 -->
    <div class="landing-login-area">
      <a v-if="!isLoggedIn" 
         href="http://localhost:8080/oauth2/authorization/google"
         class="landing-login-btn">
        <i class="bi bi-box-arrow-in-right"></i>
        <span>로그인</span>
      </a>
      
      <div v-else class="landing-user-info">
        <span class="landing-username">{{ userName }}</span>
        <img :src="userProfileImage || defaultProfileImg" alt="Profile" class="landing-profile-img" />
        <button @click="handleLogout" class="landing-logout-btn" title="로그아웃">
          <i class="bi bi-box-arrow-right"></i>
        </button>
      </div>
      
      <!-- 메뉴 버튼 -->
      <button 
        class="landing-menu-btn" 
        title="메뉴" 
        type="button" 
        data-bs-toggle="offcanvas" 
        data-bs-target="#sidebar" 
        aria-controls="sidebar">
        <i class="bi bi-list"></i>
      </button>
    </div>
    
    <!-- 메뉴 사이드바 -->
    <Menubar />
    
    <!-- 히어로 섹션 -->
    <section class="hero-section">
      <div class="hero-overlay"></div>
      <div class="hero-content">
        <h1 class="hero-title">
          <span class="highlight">AI가 만드는</span>
          <br>나만의 서울 여행
        </h1>
        <p class="hero-subtitle">
          당신의 여행 스타일에 맞춘 완벽한 일정을 AI가 자동으로 생성해드립니다
        </p>
        
        <!-- AI 프롬프트 입력 -->
        <div class="prompt-hero-card">
          <div class="prompt-input-wrapper">
            <textarea 
              class="prompt-input" 
              placeholder="어떤 여행을 꿈꾸시나요? (예: KPOP 성지순례 3일 코스, 먹방 투어 2일)" 
              v-model="promptInput"
              rows="3" 
              :disabled="isLoading" 
            />
            <button 
              class="btn-generate" 
              @click="generateItinerary" 
              :disabled="!promptInput.trim()"
              :class="{ loading: isLoading }">
              <template v-if="isLoading">
                <span class="spinner-border spinner-border-sm"></span>
                AI가 일정을 생성하는 중...
              </template>
              <template v-else>
                <i class="bi bi-magic"></i>
                AI 여행 일정 만들기
              </template>
            </button>
          </div>
        </div>

        <!-- 퀵 스타트 버튼 -->
        <div class="quick-examples">
          <span class="quick-label">빠른 시작:</span>
          <button class="quick-btn" @click="promptInput = 'KPOP 팬을 위한 3일 서울 여행'">
            <i class="bi bi-music-note-beamed"></i> KPOP 투어
          </button>
          <button class="quick-btn" @click="promptInput = '서울 먹방 2일 코스'">
            <i class="bi bi-cup-hot"></i> 맛집 투어
          </button>
          <button class="quick-btn" @click="promptInput = '인스타 감성 사진 명소 1일'">
            <i class="bi bi-camera"></i> 사진 명소
          </button>
        </div>
      </div>
    </section>

    <!-- 특징 섹션 -->
    <section class="features-section">
      <div class="container">
        <h2 class="section-title">왜 Find My Seoul인가요?</h2>
        <div class="features-grid">
          <div class="feature-card">
            <div class="feature-icon">
              <i class="bi bi-robot"></i>
            </div>
            <h3>AI 맞춤 플래닝</h3>
            <p>최신 AI 기술로 당신의 취향과 예산에 딱 맞는 여행 일정을 자동 생성합니다</p>
          </div>
          <div class="feature-card">
            <div class="feature-icon">
              <i class="bi bi-geo-alt-fill"></i>
            </div>
            <h3>실시간 정보</h3>
            <p>서울의 최신 명소, 맛집, 축제 정보를 실시간으로 반영합니다</p>
          </div>
          <div class="feature-card">
            <div class="feature-icon">
              <i class="bi bi-chat-dots"></i>
            </div>
            <h3>24시간 AI 어시스턴트</h3>
            <p>여행 중 궁금한 점이 있다면 AI 채팅으로 언제든 물어보세요</p>
          </div>
          <div class="feature-card">
            <div class="feature-icon">
              <i class="bi bi-calendar-check"></i>
            </div>
            <h3>편리한 일정 관리</h3>
            <p>생성된 일정을 자유롭게 수정하고 관리할 수 있습니다</p>
          </div>
        </div>
      </div>
    </section>

    <!-- 추천 카드 섹션 -->
    <section class="recommendations-section">
      <div class="container">
        <h2 class="section-title">서울 여행 추천</h2>
        <p class="section-subtitle">서울의 다양한 매력을 경험해보세요</p>
        
        <div class="recommendations-grid">
          <div class="recommendation-item" v-for="item in cards" :key="item.key" @click="openModal(item.key)">
            <div class="recommendation-image">
              <img :src="item.img" :alt="item.label" />
              <div class="recommendation-overlay">
                <i :class="item.icon" class="recommendation-icon"></i>
              </div>
            </div>
            <div class="recommendation-content">
              <h3>{{ item.label }}</h3>
              <p>{{ item.description }}</p>
            </div>
          </div>
        </div>
      </div>
    </section>

    <!-- CTA 섹션 -->
    <section class="cta-section">
      <div class="cta-content">
        <h2>지금 바로 시작하세요!</h2>
        <p>AI가 당신만을 위한 특별한 서울 여행을 만들어드립니다</p>
        <button class="btn-cta" @click="scrollToTop">
          <i class="bi bi-arrow-up-circle"></i>
          여행 계획 시작하기
        </button>
      </div>
    </section>

    <BlogListModal :isOpen="isModalOpen" :isLoading="isLoading" :items="blogItems" :keyword="currentKeyword"
      @close="isModalOpen = false" />
  </div>
</template>


<script setup>
import { ref } from 'vue'
import { useRoute } from 'vue-router'
import router from '@/router'
import PageHeader from '@/components/common/header/PageHeader.vue'
import TipBox from '@/components/common/TipBox.vue'
import BlogListModal from '@/components/planner/BlogListModal.vue'
import RecommendationCard from '@/components/planner/RecommendationCard.vue'
import Menubar from '@/components/Menubar.vue'
import plannerApi from '@/api/plannerApi'
import chatApi from '@/api/chatApi'
import { useAuthStore } from '@/store/authStore'
import { storeToRefs } from 'pinia'

// 이미지
import accommodationImg from '@/assets/img/planner-recommendation/accommodation.png'
import restaurantImg from '@/assets/img/planner-recommendation/restaurant.png'
import attractionImg from '@/assets/img/planner-recommendation/attraction.png'
import photoSpotImg from '@/assets/img/planner-recommendation/photospot.png'
import festivalImg from '@/assets/img/planner-recommendation/festival.png'
import experienceImg from '@/assets/img/planner-recommendation/experience.png'
import defaultProfileImg from '@/assets/img/profile-logo.png'

const route = useRoute()
const authStore = useAuthStore()

// 인증 상태
const { isLoggedIn, userName, userProfileImage } = storeToRefs(authStore)

const promptInput = ref('')
const isLoading = ref(false)

// 로그아웃 처리
const handleLogout = () => {
  authStore.logout()
  router.push('/logout')
}

// 모달
const isModalOpen = ref(false)
const blogItems = ref([])
const currentKeyword = ref('')

// 카드 정의
const cards = [
  { key: 'accommodation', label: '감성 숙소', icon: 'bi-house-heart', img: accommodationImg, description: '서울의 특별한 숙소를 찾아보세요' },
  { key: 'restaurants', label: '맛집 내돈내산', icon: 'bi-cup-hot', img: restaurantImg, description: '현지인이 추천하는 진짜 맛집' },
  { key: 'attractions', label: '가볼만한 곳', icon: 'bi-compass', img: attractionImg, description: '서울의 핫플레이스를 탐험하세요' },
  { key: 'photospots', label: '사진 명소', icon: 'bi-camera', img: photoSpotImg, description: '인스타그램 감성 사진 스팟' },
  { key: 'festivals', label: '축제', icon: 'bi-music-note', img: festivalImg, description: '서울에서 열리는 다양한 축제' },
  { key: 'experiences', label: '이색 체험', icon: 'bi-calendar-event', img: experienceImg, description: '특별한 추억을 만드는 체험' }
]

// 스크롤 함수
const scrollToTop = () => {
  window.scrollTo({ top: 0, behavior: 'smooth' })
}

// 블로그 모달
const openModal = async (category) => {
  currentKeyword.value = `서울 ${category}`
  isModalOpen.value = true
  isLoading.value = true

  try {
    const res = await plannerApi.getBlogList(currentKeyword.value)
    blogItems.value = res.data || []
  } finally {
    isLoading.value = false
  }
}

// AI 일정 생성
const generateItinerary = async () => {
  if (!promptInput.value.trim() || isLoading.value) return

  isLoading.value = true

  try {
    await chatApi.chat({
      message: promptInput.value,
      userId: authStore.userId,
      currentUrl: route.path
    })

    // 페이지 이동 전에 로딩 유지
    setTimeout(() => {
      router.push('/planner/edit');
    }, 600)

  } catch (e) {
    console.error(e)
    isLoading.value = false;
  }
}

</script>

<style scoped>
.landing-page {
  background-color: #ffffff;
  min-height: 100vh;
  position: relative;
  font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, "Helvetica Neue", Arial, "Noto Sans KR", sans-serif;
  color: #2c3e50;
  line-height: 1.7;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
}

/* ========== 플로팅 로그인 영역 ========== */
.landing-login-area {
  position: fixed;
  top: 2rem;
  right: 2rem;
  z-index: 1000;
  display: flex;
  align-items: center;
  gap: 0.75rem;
}

.landing-login-btn {
  display: inline-flex;
  align-items: center;
  gap: 0.5rem;
  padding: 0.75rem 1.5rem;
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
  color: #ff8c00;
  text-decoration: none;
  border-radius: 50px;
  font-weight: 700;
  font-size: 1rem;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.15);
  transition: all 0.3s ease;
  border: 2px solid rgba(255, 140, 0, 0.2);
}

.landing-login-btn i {
  font-size: 1.2rem;
}

.landing-login-btn:hover {
  background: white;
  transform: translateY(-2px);
  box-shadow: 0 6px 25px rgba(255, 140, 0, 0.3);
  color: #ff6b00;
}

.landing-user-info {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  padding: 0.5rem 1rem;
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
  border-radius: 50px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.15);
}

.landing-username {
  font-weight: 700;
  font-size: 1rem;
  color: #ff8c00;
}

.landing-profile-img {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  object-fit: cover;
  border: 2px solid #ff8c00;
}

.landing-logout-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 36px;
  height: 36px;
  background: rgba(255, 140, 0, 0.1);
  border: 2px solid rgba(255, 140, 0, 0.3);
  border-radius: 50%;
  color: #ff8c00;
  cursor: pointer;
  transition: all 0.3s ease;
  padding: 0;
}

.landing-logout-btn:hover {
  background: #ff8c00;
  color: white;
  transform: scale(1.1);
}

.landing-logout-btn i {
  font-size: 1.1rem;
}

.landing-menu-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 48px;
  height: 48px;
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
  border: 2px solid rgba(255, 140, 0, 0.2);
  border-radius: 12px;
  color: #ff8c00;
  cursor: pointer;
  transition: all 0.3s ease;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.15);
  padding: 0;
}

.landing-menu-btn:hover {
  background: white;
  transform: translateY(-2px);
  box-shadow: 0 6px 25px rgba(255, 140, 0, 0.3);
  color: #ff6b00;
}

.landing-menu-btn i {
  font-size: 1.75rem;
}

/* ========== 히어로 섹션 ========== */
.hero-section {
  position: relative;
  min-height: 90vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #ff8c00 0%, #ff6b00 50%, #ff4500 100%);
  overflow: hidden;
  padding: 2rem 1.5rem;
}

.hero-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: 
    radial-gradient(circle at 20% 50%, rgba(255, 255, 255, 0.1) 0%, transparent 50%),
    radial-gradient(circle at 80% 80%, rgba(255, 255, 255, 0.1) 0%, transparent 50%);
  animation: float 20s ease-in-out infinite;
}

@keyframes float {
  0%, 100% { transform: translateY(0) scale(1); }
  50% { transform: translateY(-20px) scale(1.05); }
}

.hero-content {
  position: relative;
  z-index: 2;
  text-align: center;
  width: 100%;
  max-width: none;
  margin: 0 auto;
  padding: 0 4rem;
}

.hero-title {
  font-size: 3.5rem;
  font-weight: 700;
  color: white;
  margin-bottom: 1.5rem;
  line-height: 1.3;
  text-shadow: 0 2px 15px rgba(0, 0, 0, 0.15);
  animation: fadeInUp 0.8s ease-out;
  max-width: 1200px;
  margin-left: auto;
  margin-right: auto;
  letter-spacing: -0.02em;
}

.highlight {
  background: linear-gradient(120deg, #ffffff 0%, #fff8e1 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  font-weight: 800;
}

.hero-subtitle {
  font-size: 1.35rem;
  color: rgba(255, 255, 255, 0.92);
  margin-bottom: 3rem;
  font-weight: 400;
  animation: fadeInUp 0.8s ease-out 0.2s backwards;
  letter-spacing: -0.01em;
  max-width: 1200px;
  margin-left: auto;
  margin-right: auto;
}

@keyframes fadeInUp {
  from {
    opacity: 0;
    transform: translateY(30px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

/* 프롬프트 카드 */
.prompt-hero-card {
  background: white;
  border-radius: 1.5rem;
  padding: 2rem;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.3);
  animation: fadeInUp 0.8s ease-out 0.4s backwards;
  margin-bottom: 2rem;
  max-width: 1000px;
  width: 100%;
  height: 300px;
  margin-left: auto;
  margin-right: auto;
  display: flex;
  align-items: center;
}

.prompt-input-wrapper {
  position: relative;
  width: 100%;
  height: 100%;
  display: flex;
  flex-direction: column;
}

.prompt-input {
  width: 100%;
  height: 100%;
  padding: 1.5rem;
  padding-bottom: 5rem;
  border: 2px solid #f0f0f0;
  border-radius: 1rem;
  /* background-color: #fafafa; */
  font-size: 1.1rem;
  transition: all 0.3s ease;
  resize: none;
  font-family: inherit;
}

.prompt-input:disabled {
  background-color: #f3f3f3;
  color: #999;
  cursor: not-allowed;
}

.prompt-input:focus {
  outline: none;
  border-color: #ff8c00;
  background: #fff;
  box-shadow: 0 0 0 4px rgba(255, 140, 0, 0.1);
}

.btn-generate {
  position: absolute;
  right: 1.5rem;
  bottom: 1.5rem;
  padding: 0.9rem 2rem;
  border-radius: 50px;
  background: linear-gradient(135deg, #ff8c00 0%, #ff6b00 100%);
  color: #fff;
  font-weight: 700;
  font-size: 1.05rem;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 0.5rem;
  border: none;
  cursor: pointer;
  box-shadow: 0 8px 20px rgba(255, 140, 0, 0.4);
  transition: all 0.3s ease;
}

.btn-generate:hover:not(:disabled) {
  transform: translateY(-3px);
  box-shadow: 0 12px 28px rgba(255, 140, 0, 0.5);
}

.btn-generate.loading {
  cursor: wait;
}

.btn-generate:disabled {
  background: #e0e0e0;
  color: #aaa;
  box-shadow: none;
  cursor: not-allowed;
}

.spinner-border {
  width: 1rem;
  height: 1rem;
  border-width: 2px;
  color: #fff;
}

/* 퀵 스타트 버튼 */
.quick-examples {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 1rem;
  flex-wrap: wrap;
  animation: fadeInUp 0.8s ease-out 0.6s backwards;
}

.quick-label {
  color: rgba(255, 255, 255, 0.95);
  font-weight: 500;
  font-size: 0.95rem;
  letter-spacing: 0.01em;
}

.quick-btn {
  padding: 0.65rem 1.3rem;
  background: rgba(255, 255, 255, 0.18);
  backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.35);
  border-radius: 25px;
  color: white;
  font-weight: 500;
  font-size: 0.92rem;
  cursor: pointer;
  transition: all 0.3s ease;
  display: inline-flex;
  align-items: center;
  gap: 0.5rem;
  letter-spacing: 0.01em;
}

.quick-btn:hover {
  background: rgba(255, 255, 255, 0.3);
  transform: translateY(-2px);
}

/* ========== 특징 섹션 ========== */
.features-section {
  padding: 6rem 2rem;
  /* background: #fafafa; */
}

.container {
  max-width: 1200px;
  margin: 0 auto;
}

.section-title {
  text-align: center;
  font-size: 2.5rem;
  font-weight: 700;
  color: #1a1a1a;
  margin-bottom: 1rem;
  letter-spacing: -0.02em;
}

.section-subtitle {
  text-align: center;
  font-size: 1.2rem;
  color: #5a5a5a;
  margin-bottom: 4rem;
  font-weight: 400;
  letter-spacing: -0.01em;
}

.features-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 2rem;
}

.feature-card {
  background: white;
  padding: 2.5rem 2rem;
  border-radius: 1.5rem;
  text-align: center;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
  transition: all 0.3s ease;
}

.feature-card:hover {
  transform: translateY(-10px);
  box-shadow: 0 12px 40px rgba(255, 140, 0, 0.2);
}

.feature-icon {
  width: 80px;
  height: 80px;
  margin: 0 auto 1.5rem;
  background: linear-gradient(135deg, #ff8c00, #ff6b00);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 2.5rem;
  color: white;
}

.feature-card h3 {
  font-size: 1.4rem;
  font-weight: 600;
  color: #2c3e50;
  margin-bottom: 1rem;
  letter-spacing: -0.01em;
}

.feature-card p {
  color: #5a6c7d;
  line-height: 1.7;
  font-size: 1rem;
  font-weight: 400;
}

/* ========== 추천 섹션 ========== */
.recommendations-section {
  padding: 6rem 2rem;
  background: white;
}

.recommendations-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
  gap: 2rem;
}

.recommendation-item {
  cursor: pointer;
  border-radius: 1.5rem;
  overflow: hidden;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.1);
  transition: all 0.3s ease;
  background: white;
}

.recommendation-item:hover {
  transform: translateY(-8px);
  box-shadow: 0 12px 40px rgba(0, 0, 0, 0.15);
}

.recommendation-image {
  position: relative;
  height: 250px;
  overflow: hidden;
}

.recommendation-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.3s ease;
}

.recommendation-item:hover .recommendation-image img {
  transform: scale(1.1);
}

.recommendation-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: linear-gradient(to bottom, transparent 0%, rgba(0, 0, 0, 0.6) 100%);
  display: flex;
  align-items: flex-end;
  justify-content: center;
  padding: 2rem;
}

.recommendation-icon {
  font-size: 3rem;
  color: white;
}

.recommendation-content {
  padding: 1.5rem;
}

.recommendation-content h3 {
  font-size: 1.3rem;
  font-weight: 600;
  color: #2c3e50;
  margin-bottom: 0.5rem;
  letter-spacing: -0.01em;
}

.recommendation-content p {
  color: #5a6c7d;
  font-size: 0.95rem;
  line-height: 1.6;
  font-weight: 400;
}

/* ========== CTA 섹션 ========== */
.cta-section {
  padding: 6rem 2rem;
  background: linear-gradient(135deg, #ff8c00 0%, #ff6b00 100%);
  text-align: center;
}

.cta-content h2 {
  font-size: 2.5rem;
  font-weight: 700;
  color: white;
  margin-bottom: 1rem;
  letter-spacing: -0.02em;
}

.cta-content p {
  font-size: 1.2rem;
  color: rgba(255, 255, 255, 0.92);
  margin-bottom: 2rem;
  font-weight: 400;
  letter-spacing: -0.01em;
}

.btn-cta {
  padding: 1.2rem 3rem;
  background: white;
  color: #ff8c00;
  border: none;
  border-radius: 50px;
  font-size: 1.15rem;
  font-weight: 600;
  cursor: pointer;
  box-shadow: 0 8px 25px rgba(0, 0, 0, 0.2);
  letter-spacing: -0.01em;
  transition: all 0.3s ease;
  display: inline-flex;
  align-items: center;
  gap: 0.5rem;
}

.btn-cta:hover {
  transform: translateY(-5px);
  box-shadow: 0 12px 35px rgba(0, 0, 0, 0.3);
}

/* 반응형 */
@media (max-width: 768px) {
  .landing-login-area {
    top: 1rem;
    right: 1rem;
    flex-direction: row;
    gap: 0.5rem;
  }
  
  .landing-login-btn {
    padding: 0.6rem 1rem;
    font-size: 0.9rem;
  }
  
  .landing-user-info {
    padding: 0.4rem 0.8rem;
    gap: 0.5rem;
  }
  
  .landing-username {
    font-size: 0.85rem;
  }
  
  .landing-profile-img {
    width: 32px;
    height: 32px;
  }
  
  .landing-logout-btn {
    width: 32px;
    height: 32px;
  }
  
  .landing-logout-btn i {
    font-size: 1rem;
  }
  
  .landing-menu-btn {
    width: 40px;
    height: 40px;
  }
  
  .landing-menu-btn i {
    font-size: 1.5rem;
  }
  
  .hero-content {
    padding: 0 1rem;
  }
  
  .prompt-hero-card {
    padding: 1.5rem;
    height: auto;
    min-height: 280px;
  }
  
  .prompt-input {
    padding: 1.2rem;
    padding-bottom: 4.5rem;
    font-size: 1rem;
  }
  
  .btn-generate {
    right: 1rem;
    bottom: 1rem;
    padding: 0.75rem 1.5rem;
    font-size: 0.95rem;
  }
  
  .landing-login-btn {
    padding: 0.6rem 1.2rem;
    font-size: 0.9rem;
  }
  
  .landing-username {
    font-size: 0.9rem;
  }
  
  .landing-profile-img {
    width: 35px;
    height: 35px;
  }
  
  .hero-title {
    font-size: 2.5rem;
  }
  
  .hero-subtitle {
    font-size: 1.1rem;
  }
  
  .section-title {
    font-size: 2rem;
  }
  
  .btn-generate {
    position: static;
    width: 100%;
    margin-top: 1rem;
    justify-content: center;
  }
  
  .prompt-input {
    padding-bottom: 1.5rem;
  }
}
</style>
