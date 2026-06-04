<template>
  <TravelgramHeader :back-route="{ name: 'PhotoOrder' }" />
    <div class="travelgram-page">
      <div class="page-inner">

    <section class="caption-section">
      <header class="caption-header">
        <h5 class="section-title">
          <i class="bi bi-stars text-primary me-2"></i>
          AI가 생성한 후기
        </h5>
        <p class="section-subtitle">
          마음에 드는 이야기 스타일을 하나 골라주세요.
        </p>
      </header>

      <!-- 로딩 -->
      <IconSpinner
        v-if="isLoading"
        :steps="journeySteps"
        text="AI가 여행 이야기를 한 장면씩 엮고 있어요…"
      />

      <!-- 에러 -->
      <ErrorRetry v-else-if="hasError" @retry="loadStyles">
        AI 후기 생성에 실패했어요.<br />잠시 후 다시 시도해 주세요.
      </ErrorRetry>

      <!-- 결과 -->
      <div v-else class="caption-list">
        <div
          v-for="(item, index) in reviewStore.generatedOptions"
          :key="index"
          class="caption-card"
          :class="{ active: selectedIndex === index }"
          @click="selectStyle(index)"
        >
          <div class="caption-card-header">
            <span
              class="caption-label"
              :class="getLabelClass(item.style.toneCode)"
            >
              {{ item.style.name }}
            </span>

            <i
              v-if="selectedIndex === index"
              class="bi bi-check-circle-fill checkmark"
            ></i>
          </div>

          <p class="caption-text">
            {{ item.style.caption }}
          </p>

          <div class="hashtag-preview">
            <small class="text-muted">
              {{ item.hashtags.map(h => '#' + h.name).slice(0, 3).join(' ') }}
            </small>
          </div>
        </div>
      </div>
    </section>

    <NavigationButtons
      backText="뒤로가기"
      :isNextDisabled="!canProceed"
      @back="goBack"
      @next="goNext"
    >
      <template #next-content>
        <span v-if="isAnalyzing">분석 중...</span>
        <span v-else>다음으로</span>
      </template>
    </NavigationButtons>
          </div>
    </div>
</template>


<script setup>
import TravelgramHeader from '@/components/travelgram/TravelgramHeader.vue'
  import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useReviewStore } from '@/store/reviewStore'
import api from '@/api/travelgramApi'
import StepHeader from '@/components/common/header/StepHeader.vue'
import PageHeader from '@/components/common/header/PageHeader.vue'
import NavigationButtons from '@/components/common/button/NavigationButtons.vue'
import { JOURNEY_SUBTITLES, journeySteps } from '@/constants/journeySubtitles'
import IconSpinner from '@/components/common/spinner/IconSpinner.vue'
import ErrorRetry from '@/components/travelgram/ErrorRetry.vue'

const router = useRouter()
const reviewStore = useReviewStore()

const isLoading = ref(false)
const hasError = ref(false)
const isAnalyzing = ref(false)
const selectedIndex = ref(null)

const canProceed = computed(() => selectedIndex.value !== null)
const stepSubtitle = computed(() => JOURNEY_SUBTITLES[3])

// AI 스타일 생성 호출 — 재시도 버튼에서도 다시 부를 수 있도록 함수로 분리
const loadStyles = async () => {
  isLoading.value = true
  hasError.value = false
  try {
    const res = await api.generateAiStyles(
      reviewStore.planId,
      reviewStore.reviewPostId
    )
    reviewStore.setGeneratedOptions(res.data.data)
  } catch (e) {
    // 동기 호출이라 서버 실패(5xx)/네트워크 오류가 여기로 전파됨
    console.error('AI 스타일 생성 실패', e)
    hasError.value = true
  } finally {
    isLoading.value = false
  }
}

onMounted(() => {
  if (reviewStore.generatedOptions.length > 0) return
  loadStyles()
})

const selectStyle = (index) => {
  selectedIndex.value = index
}

const getLabelClass = (code) => {
  switch (code) {
    case 'EMOTIONAL': return 'poetic'
    case 'INFORMATIVE': return 'inspirational'
    case 'WITTY': return 'fun'
    default: return 'casual'
  }
}

const goBack = () => router.push({ name: 'PhotoOrder' })
const props = defineProps({
  planId: {
    type: [String, Number],
    required: true
  }
})
const goNext = async () => {
  if (selectedIndex.value === null) return

  isAnalyzing.value = true
  try {
    const selected = reviewStore.generatedOptions[selectedIndex.value]
    reviewStore.selectStyleOption(selected)
    await api.selectStyle(reviewStore.reviewPostId, reviewStore.reviewStyleId)
    reviewStore.nextStep()
    router.push({ name: 'HashtagSelect' })
  } finally {
    isAnalyzing.value = false
  }
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
.review-caption-select {
  background-color: #fffaf3;
  min-height: 100vh;
  padding: 2.5rem 1.5rem 6rem;
}

/* 섹션 */
.caption-section {
  margin-top: 2rem;
}

/* 헤더 */
.caption-header {
  margin-bottom: 1.75rem;
}

.section-title {
  color: #1b3b6f;
  font-weight: 700;
  margin-bottom: 0.4rem;
}

.section-subtitle {
  color: #6c757d;
}

/* 카드 리스트 */
.caption-list {
  display: flex;
  flex-direction: column;
  gap: 1.4rem; /* 🔥 카드 간 숨 */
}

/* 카드 */
.caption-card {
  background: #fff;
  border-radius: 1.25rem;
  padding: 1.4rem 1.5rem;
  border: 2px solid transparent;
  box-shadow: 0 6px 18px rgba(0, 0, 0, 0.05);
  cursor: pointer;
  transition: all 0.25s ease;
}

.caption-card:hover {
  background-color: #fff7ed;
}

.caption-card.active {
  border-color: #ff8c00;
  background-color: #fffaf3;
}

/* 카드 헤더 */
.caption-card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 0.75rem;
}

/* 라벨 */
.caption-label {
  font-weight: 600;
  padding: 0.25rem 0.7rem;
  border-radius: 999px;
}

/* 라벨 색상 */
.poetic {
  background: #efe4ff;
  color: #6a1b9a;
}
.inspirational {
  background: #e6f4ea;
  color: #2e7d32;
}
.fun {
  background: #ffe4e9;
  color: #c2185b;
}
.casual {
  background: #fff3cd;
  color: #d9822b;
}

/* 텍스트 */
.caption-text {
  line-height: 1.6;
  color: #333;
}

/* 해시태그 */
.hashtag-preview {
  margin-top: 0.8rem;
  color: #999;
}

/* 체크 */
.checkmark {
  color: #ff8c00;
}
.loading-container {
  display: flex;
  flex-direction: column;
  align-items: center;      /* ⭕ 가로 중앙 */
  justify-content: center;  /* ⭕ 세로 중앙(내용 기준) */

  text-align: center;
  padding: 3rem 0;          /* 🔥 위아래 숨 */
  color: #666;
  min-height: 220px; /* 🔥 로딩 중 레이아웃 점프 방지 */
}
</style>
