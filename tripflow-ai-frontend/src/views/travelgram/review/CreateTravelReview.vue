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
      
      <!-- =========================
           HERO / 여행 요약 카드
           ========================== -->
        <div class="plan-hero" v-if="currentplanInfo">
          <!-- 상단 정보 영역 -->
          <div class="hero-main">
            <div class="hero-text">
              <h3 class="plan-hero-title">{{ planTitle }}</h3>
              <p class="plan-hero-sub">
                사진으로 여행을 다시 정리해요.
                업로드하면 AI가 감성적인 후기를 만들어줘요.
              </p>

              <div class="chip-row">
                <span class="chip">📍 {{ currentplanInfo.location }}</span>
                <span class="chip">📅 {{ currentplanInfo.date }}</span>
                <span class="chip" v-if="currentplanInfo.rawCost > 0">
                  💸 {{ currentplanInfo.cost }}
                </span>
                <span class="chip" v-else>💸 Budget 미입력</span>
              </div>
            </div>

            <!-- 대표사진 -->
            <div class="hero-image">
              <transition name="fade">
                <img
                  v-if="hasPhotos"
                  :src="uploadedImages[0]?.previewUrl || uploadedImages[0]?.url"
                  class="hero-cover-img"
                />
                <div v-else class="hero-cover-placeholder">
                  대표 사진
                </div>
              </transition>
            </div>
          </div>

          <!-- CTA는 분리 -->
          <button class="primary-cta" @click="scrollToUploader">
            📸 여행 사진 업로드하고 AI 후기 만들기
          </button>
        </div>


        <!-- =========================
        일정 (접힘/펼침)
        ========================== -->
        <div class="itinerary-section" v-if="currentplanInfo && currentplanInfo.itinerary">
          <button class="itinerary-toggle" @click="isItineraryOpen = !isItineraryOpen">
            <i class="bi bi-calendar-event"></i>
            지난 여행 일정 (AI 참고용)
            <span class="ms-auto">{{ isItineraryOpen ? '▲' : '▼' }}</span>
          </button>

          <div v-show="isItineraryOpen" class="timeline-wrapper">
            <PlanDayTimeline 
              :days="currentplanInfo.itinerary" 
              :current-day-places="currentDayPlaces" 
              v-model:selectedDayIndex="selectedDayIndex"
              :edit-mode="false" 
              :type-color="getTypeColor"
              :type-label="getTypeLabel" 
              :format-time="formatTime" 
              :category-map="categoryMap"
              @open-modal="handleOpenModal" 
            />
          </div>
        </div>

        <!-- =========================
             사진 업로드 섹션
             ========================== -->
             <div class="uploader-anchor"></div>
             <div class="upload-section">
               <PhotoUploader v-model="uploadedImages" :is-ready="isReady" :photo-group-id="reviewStore.photoGroupId"
               :max-count="10" @upload-started="startPolling" />
              </div>
              
              <!-- AI 분석 상태 -->
              <div v-if="isAnalyzing" class="alert alert-info mt-3 d-flex align-items-center">
                <div class="spinner-border spinner-border-sm me-2"></div>
                <div>
                  <strong>AI가 사진을 분석하고 있어요...</strong>
                  <span class="small ms-1">잠시만 기다려주세요.</span>
          </div>
        </div>

        <!-- 하단 네비게이션 -->
        <NavigationButtons backText="뒤로가기" :isNextDisabled="!canProceed" @back="goBack" @next="goNext">
          <template #next-content>
            <span v-if="isAnalyzing">분석 중...</span>
            <span v-else>다음으로</span>
          </template>
        </NavigationButtons>
      </div>
</div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useReviewStore } from '@/store/reviewStore'
import api from '@/api/travelgramApi'

import PageHeader from '@/components/common/header/PageHeader.vue'
import StepHeader from '@/components/common/header/StepHeader.vue'
import NavigationButtons from '@/components/common/button/NavigationButtons.vue'
import PhotoUploader from '@/components/travelgram/PhotoUploader.vue'
import PlanDayTimeline from '@/components/planner/PlanDayTimeline.vue'
import { JOURNEY_SUBTITLES } from '@/constants/journeySubtitles'

const router = useRouter()
const route = useRoute()
const reviewStore = useReviewStore()

const planId = route.params.planId
const planTitle = route.params.planTitle || '나의 여행'

const uploadedImages = ref([])
const isAnalyzing = ref(false)
const pollingInterval = ref(null)
const isReady = ref(false)
const currentplanInfo = ref(null)
const isItineraryOpen = ref(false)
// [추가] 선택된 Day 인덱스
const selectedDayIndex = ref(0)

// [추가] 선택된 Day에 해당하는 장소 목록 계산
const currentDayPlaces = computed(() => {
  if (!currentplanInfo.value?.itinerary) return []
  return currentplanInfo.value.itinerary[selectedDayIndex.value]?.places || []
})

const stepSubtitle = computed(() => JOURNEY_SUBTITLES[1])
/* ---------- 일정 헬퍼 ---------- */
const categoryMap = {
  FOOD: "음식점",
  SPOT: "관광지",
  SHOPPING: "쇼핑",
  CAFE: "카페",
  HOTEL: "숙소",
  EVENT: "이벤트",
  ETC: "기타",
  ATTRACTION: "관광지",
  RESTAURANT: "음식점",
  ACCOMMODATION: "숙소"
}

const getTypeColor = (type) => {
  const t = type?.toUpperCase()
  if (["FOOD", "RESTAURANT"].includes(t)) return "color-red"
  if (t === "SHOPPING") return "color-blue"
  if (t === "CAFE") return "color-green"
  if (["HOTEL", "ACCOMMODATION"].includes(t)) return "color-gray"
  return "color-purple"
}

const getTypeLabel = (type) => {
  const t = type?.toUpperCase()
  if (["FOOD", "RESTAURANT"].includes(t)) return "식사"
  if (t === "SHOPPING") return "쇼핑"
  if (t === "CAFE") return "카페"
  if (["HOTEL", "ACCOMMODATION"].includes(t)) return "숙소"
  if (["SPOT", "ATTRACTION"].includes(t)) return "관광"
  return null
}

const formatTime = (iso) => iso ? iso.substring(11, 16) : ''
const handleOpenModal = () => { }

/* ---------- 데이터 ---------- */
const fetchPlanDetail = async () => {
  const res = await api.getPlanDetail(planId)
  const data = res.data

  const rawBudget = Number(data.plan.budget || 0)

  currentplanInfo.value = {
    location: data.days?.[0]?.places?.[0]?.address?.split(' ').slice(0, 2).join(' ') || 'Seoul',
    date: `${data.plan.startDate} ~ ${data.plan.endDate}`,
    rawCost: rawBudget,
    cost: rawBudget.toLocaleString(),
    itinerary: data.days.map(d => ({
      dayNumber: d.day.dayIndex,
      title: d.day.title,
      date: d.day.planDate,
      places: d.places.map(p => ({
        title: p.placeName,
        startAt: p.startAt,
        details: {
          type: p.placeType || 'ETC',
          desc: p.description,
          gallery: p.firstImage2 ? [p.firstImage2] : [],
          address: p.address
        }
      }))
    }))
  }
}

/* ---------- 라이프사이클 ---------- */
onMounted(async () => {
  reviewStore.setplanInfo(planId, planTitle)
  await fetchPlanDetail()
  const res = await api.createReview(planId)
  reviewStore.setReviewInfo(res.data.reviewPostId, res.data.photoGroupId, res.data.hashtagGroupId)
  isReady.value = true
})

const checkAnalysisStatus = async () => {
  const res = await api.getReviewPhotos(reviewStore.photoGroupId)
  const serverPhotos = res.data.data || []

  uploadedImages.value.forEach(img => {
    const match = serverPhotos.find(s => String(s.id) === String(img.id))
    if (match?.summary) {
      img.isAnalyzed = true
      img.summary = match.summary
    }
  })

  isAnalyzing.value = uploadedImages.value.some(i => !i.isAnalyzed)
}

const startPolling = () => {
  if (pollingInterval.value) return
  pollingInterval.value = setInterval(checkAnalysisStatus, 3000)
}

onUnmounted(() => pollingInterval.value && clearInterval(pollingInterval.value))

const canProceed = computed(() =>
  uploadedImages.value.length > 0 && !isAnalyzing.value
)

const scrollToUploader = () => {
  document.querySelector('.uploader-anchor')?.scrollIntoView({ behavior: 'smooth' })
}

const goNext = () => {
  reviewStore.setPhotos(uploadedImages.value)
  reviewStore.nextStep()
  router.push({ name: 'PhotoOrder', params: { planId } })
}
const props = defineProps({
  planId: {
    type: [String, Number],
    required: true
  }
})

const goBack = () => router.push({ name: 'Travelgram' })

const hasPhotos = computed(() => uploadedImages.value.length > 0)

</script>

<style scoped>
/* Page Layout */
.travelgram-page {
  min-height: 100vh;
  background-color: #fff;
  justify-content: center;
  display: flex;
  
}


.page-inner {
  margin-top: 50px;
  width: 100%;
  max-width: 1200px;     /* 🔥 카드형 UI에 가장 안정적인 폭 */
  padding: 0 16px 32px;
}


/* Header Styles */
.travelgram-header {
  position: sticky;
  top: 0;
  z-index: 100;
  margin-bottom: 0;
}

.travelgram-header .title {
  font-weight: 600;
  color: #1B3B6F;
  margin: 0;
}

.travelgram-header .sub {
  font-size: 0.875rem;
  line-height: 1.2;
}

/* Container */
.create-travel-review-page .container {
  padding-top: 2rem;
  padding-bottom: 2rem;
  
}

.itinerary-section {
  margin-top: 28px;
}

.plan-hero {
  background: #fff;
  border-radius: 20px;
  box-shadow: 0 8px 10px rgba(27, 59, 111, 0.08);
  padding: 20px;
  margin-bottom: 20px;
  transition: all 0.35s ease;
}

.hero-cover {
  border-radius: 16px;
  overflow: hidden;
  position: relative;
}

.hero-cover-placeholder {
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(
    135deg,
    rgba(27, 59, 111, 0.15),
    rgba(59, 130, 246, 0.1)
  );
  color: #1B3B6F;
  font-weight: 600;
}

.hero-cover-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

/* 페이드 */
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.25s ease;
}
.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}

/* 업로드 전 */
.hero-empty {
  box-shadow: 0 6px 20px rgba(27, 59, 111, 0.08);
}

/* 업로드 후 */
.hero-filled {
  box-shadow: 0 12px 32px rgba(27, 59, 111, 0.2);
  transform: translateY(-2px);
}

/* CTA 변화 */
.hero-filled .primary-cta {
  background: linear-gradient(135deg, #1B3B6F, #3b82f6);
}

.hero-main {
  display: flex;
  gap: 24px;
  align-items: stretch;
  flex-wrap: wrap;
}

/* 텍스트 영역 */
.hero-text {
  flex: 1 1 320px; /* 최소 폭 */
}

/* 이미지 영역 */
.hero-image {
  flex: 0 0 180px;
  aspect-ratio: 3 / 4; /* 🔥 사진 비율 고정 */
  border-radius: 16px;
  overflow: hidden;
}

/* 이미지 */
.hero-cover-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

/* 모바일 대응 */
@media (max-width: 768px) {
  .hero-image {
    flex: 1 1 100%;
    max-width: 100%;
  }
}

.plan-hero-top {
  display: grid;
  grid-template-columns: 1fr 160px;
  gap: 16px;
}

.plan-hero-title {
  font-weight: 800;
  color: #1B3B6F;
}

.plan-hero-sub {
  color: #6b7280;
  margin: 8px 0 12px;
}

.chip-row {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.chip {
  background: #eff6ff;
  border: 1px solid #bfdbfe;
  padding: 6px 10px;
  border-radius: 999px;
  font-size: .85rem;
  color: #1e40af;
}

.primary-cta {
  width: 100%;
  margin-top: 16px;
  padding: 14px;
  border: none;
  border-radius: 14px;
  background: linear-gradient(135deg, #1B3B6F, #2563eb);
  color: #fff;
  font-weight: 800;
  cursor: pointer;
  transition: all 0.3s ease;
}

.primary-cta:hover {
  background: linear-gradient(135deg, #15305a, #1d4ed8);
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(27, 59, 111, 0.3);
}

.itinerary-toggle {
  width: 100%;
  background: #fff;
  border: 1px solid #e5e7eb;
  border-radius: 14px;
  padding: 12px;
  display: flex;
  gap: 8px;
  margin-bottom: 10px;
  cursor: pointer;
  transition: all 0.2s ease;
  color: #1B3B6F;
  font-weight: 600;
}

.itinerary-toggle:hover {
  background: #f8fafc;
  border-color: #bfdbfe;
}

.upload-section {
  background: #fff;
  border-radius: 20px;
  box-shadow: 0 8px 24px rgba(27, 59, 111, 0.08);
  padding: 18px;
  margin-top: 20px;
}

.uploader-anchor {
  scroll-margin-top: 90px;
}

.alert-info {
  background: #eff6ff;
  border: 1px solid #bfdbfe;
  color: #1e40af;
}
</style>
