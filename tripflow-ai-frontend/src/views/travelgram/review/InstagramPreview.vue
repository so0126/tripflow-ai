<template>
        <!-- Header -->
    <div class="p-4 pb-3 border-bottom d-flex align-items-center justify-content-between">
      <div class="d-flex gap-3 align-items-center">
        <button class="btn btn-link p-0 back-button" @click="$router.back()" title="뒤로 가기">
          <i class="bi bi-arrow-left-short fs-1"></i>
        </button>
        
        <div class="rounded-3 bg-secondary-subtle d-flex align-items-center justify-content-center"
          style="width: 46px; height: 46px">
          💖
        </div>

        <div>
          <h5 class="mb-1 title">트래블그램</h5>
          <p class="text-muted small mb-0 sub">
            당신의 여행 추억을 기록하고 공유하세요
          </p>
        </div>
      </div>
    </div>
<div class="travelgram-page">
      <div class="page-inner">
    <section class="preview-section">
      <h6 class="section-title">
        <i class="bi bi-instagram me-2 instagram-gradient-icon"></i>
        인스타그램 미리보기
      </h6>
      <p class="section-subtitle">
        실제 인스타그램에 업로드되었을 때의 모습을 확인해보세요.
      </p>

      <div class="insta-card" v-if="currentPhoto">
        <!-- 상단 프로필 -->
        <div class="insta-header">
          <img
            :src="userInfo.profileImage || defaultProfileImg"
            class="profile-img-circle"
            alt="Profile"
          />
          <div class="profile-info">
            <strong>{{ userInfo.handle }}</strong>
            <p>{{ userInfo.location }}</p>
          </div>
        </div>

        <!-- 사진 -->
        <div class="photo-carousel">
          <img
            :src="currentPhoto.url"
            class="preview-photo"
            :alt="currentPhoto.name"
            @error="handleImageError"
          />

          <div class="photo-index">
            {{ currentIndex + 1 }}/{{ reviewStore.photos.length }}
          </div>

          <button
            v-if="reviewStore.photos.length > 1"
            class="nav-btn nav-prev"
            @click="prevPhoto"
            :disabled="currentIndex === 0"
          >
            <i class="bi bi-chevron-left"></i>
          </button>

          <button
            v-if="reviewStore.photos.length > 1"
            class="nav-btn nav-next"
            @click="nextPhoto"
            :disabled="currentIndex === reviewStore.photos.length - 1"
          >
            <i class="bi bi-chevron-right"></i>
          </button>
        </div>

        <!-- 액션 -->
        <div class="insta-actions">
          <i class="bi bi-heart"></i>
          <i class="bi bi-chat"></i>
          <i class="bi bi-send"></i>
        </div>

        <p class="likes-count">{{ likes.toLocaleString() }} likes</p>

        <!-- 캡션 -->
        <div class="insta-caption">
          <strong>{{ userInfo.handle }}</strong>
          <span>{{ reviewStore.caption || '추가된 내용이 없습니다.' }}</span>
        </div>

        <!-- 해시태그 -->
        <div
          class="insta-hashtags"
          v-if="reviewStore.selectedHashtags.length"
        >
          <span
            v-for="tag in reviewStore.selectedHashtags"
            :key="tag.id"
          >
            #{{ tag.name }}
          </span>
        </div>

        <p class="time-posted">2 hours ago</p>
      </div>

      <p v-else class="empty-state">
        사진 정보를 불러올 수 없습니다.
        <br />
        처음 단계부터 다시 시도해주세요.
      </p>

      <!-- 복사 -->
      <div class="copy-section">
        <button class="btn-copy" @click="copyToClipboard">
          <i class="bi bi-clipboard me-2"></i>
          캡션 & 해시태그 복사
        </button>
      </div>
    </section>

    <NavigationButtons
      backText="뒤로가기"
      :isNextDisabled="!canProceed"
      @back="goBack"
      nextText="발행하기"
      @next="publish"
    />
        </div>
    </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useReviewStore } from '@/store/reviewStore'
import { useAuthStore } from '@/store/authStore'

import StepHeader from '@/components/common/header/StepHeader.vue'
import PageHeader from '@/components/common/header/PageHeader.vue'
import NavigationButtons from '@/components/common/button/NavigationButtons.vue'
import defaultProfileImg from '@/assets/img/profile-logo.png'
import { JOURNEY_SUBTITLES } from '@/constants/journeySubtitles'

const reviewStore = useReviewStore()
const authStore = useAuthStore()
const router = useRouter()

const stepSubtitle = computed(() => JOURNEY_SUBTITLES[6])

onMounted(() => {
  if (!authStore.isLoggedIn) {
    authStore.initializeAuth()
  }
})

const userInfo = computed(() => {
  const name = authStore.userName || 'Traveler'
  return {
    handle: name.toLowerCase().replace(/\s+/g, '.'),
    profileImage: authStore.userProfileImage,
    location: '대한민국, 서울'
  }
})

const likes = ref(1234)
const currentIndex = ref(0)

const canProceed = computed(() => {
  return reviewStore.photos && reviewStore.photos.length > 0
})

const currentPhoto = computed(() => {
  if (!reviewStore.photos?.length) return null
  return reviewStore.photos[currentIndex.value]
})

const prevPhoto = () => {
  if (currentIndex.value > 0) currentIndex.value--
}
const nextPhoto = () => {
  if (currentIndex.value < reviewStore.photos.length - 1) {
    currentIndex.value++
  }
}

const handleImageError = (e) => {
  console.error('Image load failed:', e.target.src)
}

const copyToClipboard = () => {
  const caption = reviewStore.caption || ''
  const tags = reviewStore.selectedHashtags
    .map(tag => `#${tag.name}`)
    .join(' ')
  const text = `${caption}\n\n${tags}`.trim()

  navigator.clipboard.writeText(text).then(() => {
    alert('📋 Copied!')
  })
}
const props = defineProps({
  planId: {
    type: [String, Number],
    required: true
  }
})
const goBack = () => router.push({ name: 'EditPage' })
const publish = () => {
  alert('✅ 게시물이 준비되었습니다!')
  router.push({ name: 'CompleteReview' })
}


</script>

<style scoped>
/* ================= Page Background ================= */
.travelgram-page {
  min-height: 100vh;
  display: flex;
  justify-content: center;
}

/* ================= Content Width ================= */
.page-inner {
  width: 100%;
  max-width: 1200px;
  padding: 50px 16px 32px;
}

/* 섹션 */
.section-title {
  color: #1b3b6f;
  font-weight: 600;
  padding: 0 1.25rem;
  margin-bottom: 0.5rem;
  margin-top: 1.5rem;
}

.section-subtitle {
  color: #6c757d;
  padding: 0 1.25rem;
  margin-bottom: 1.25rem;
}

/* 카드 */
.insta-card {
  background: white;
  border-radius: 1.25rem;
  border: 1px solid #eee;
  overflow: hidden;
  box-shadow: 0 6px 20px rgba(0, 0, 0, 0.06);
  margin: 0 0.75rem 2rem;
}

/* 프로필 */
.insta-header {
  display: flex;
  align-items: center;
  padding: 0.75rem 1rem;
  border-bottom: 1px solid #f1f1f1;
}

.profile-img-circle {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  object-fit: cover;
  border: 1px solid #eee;
  margin-right: 0.75rem;
}

.profile-info p {
  color: #777;
  margin: 0;
}

/* 사진 */
.photo-carousel {
  position: relative;
  width: 420px;
  height: 420px;
  margin: 0 auto;          /* 카드 중앙 정렬 */
  background: #f5f5f5;
  overflow: hidden;
  display: flex;
  align-items: center;
  justify-content: center;
}

.preview-photo {
  width: 100%;
  height: 100%;
  object-fit: cover;     /* 인스타 느낌 핵심 */
}

.photo-index {
  position: absolute;
  top: 0.75rem;
  right: 0.75rem;
  background: rgba(0, 0, 0, 0.6);
  color: white;
  padding: 0.25rem 0.6rem;
  border-radius: 1rem;
}


/* 액션 */
.insta-actions {
  display: flex;
  gap: 1rem;
  padding: 0.75rem 1rem 0;
  color: #333;
}

.likes-count {
  font-weight: 600;
  padding: 0.25rem 1rem;
}

/* 캡션 */
.insta-caption {
  padding: 0.5rem 1rem 0;
  line-height: 1.5;
}

.insta-caption strong {
  margin-right: 0.5rem;
}

/* 해시태그 */
.insta-hashtags {
  padding: 0.5rem 1rem;
  color: #1b3b6f;
  display: flex;
  flex-wrap: wrap;
  gap: 0.25rem;
}

/* 시간 */
.time-posted {
  color: #888;
  padding: 0 1rem 1rem;
}

/* 복사 */
.copy-section {
  text-align: center;
  margin-bottom: 1.5rem;
}

.btn-copy {
  background: white;
  border: 2px solid #1b3b6f;
  color: #1b3b6f;
  font-weight: 600;
  padding: 0.75rem 1.5rem;
  border-radius: 1rem;
  cursor: pointer;
  transition: all 0.2s ease;
}

.btn-copy:hover {
  background: #1b3b6f;
  color: white;
}

/* 인스타그램 아이콘 그라데이션 */
.instagram-gradient-icon {
  background: linear-gradient(
    45deg,
    #feda75,
    #fa7e1e,
    #d62976,
    #962fbf,
    #4f5bd5
  );
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  display: inline-block;
}

.empty-state {
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;

  min-height: 260px;              /* 카드 높이 기준 */
  text-align: center;

  color: #1b3b6f;
  font-weight: 600;
  line-height: 1.6;

  padding: 2rem 1.5rem;
}

.nav-btn {
  position: absolute;
  top: 50%;
  transform: translateY(-50%);

  width: 44px;
  height: 44px;

  border-radius: 50%;
  border: none;

  background: rgba(27, 59, 111, 0.85); /* 메인 남색 */
  color: #ffffff;

  display: flex;
  align-items: center;
  justify-content: center;

  cursor: pointer;
  transition: all 0.25s ease;

  box-shadow: 0 6px 16px rgba(27, 59, 111, 0.35);
  backdrop-filter: blur(4px);
}

.nav-prev {
  left: 0.75rem;
}

.nav-next {
  right: 0.75rem;
}
.nav-btn:hover {
  background: linear-gradient(135deg, #1b3b6f, #274b8f);
  transform: translateY(-50%) scale(1.05);
  box-shadow: 0 10px 24px rgba(27, 59, 111, 0.45);
}
.nav-btn:disabled {
  background: rgba(27, 59, 111, 0.35);
  box-shadow: none;
  cursor: not-allowed;
  transform: translateY(-50%);
}
.nav-btn i {
  font-size: 1.25rem;
}

</style>
