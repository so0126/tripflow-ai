<template>
  <section class="image-ai-type-page">

    <!-- Header -->
    <div class="page-header">
      <div class="d-flex gap-3 align-items-center">
        <button class="btn btn-link p-0 back-button" @click="$router.back()">
          <i class="bi bi-arrow-left-short fs-1"></i>
        </button>

        <div class="icon-box">📷</div>

        <div>
          <h5 class="mb-1 title">이미지 기반 여행 AI</h5>
          <p class="sub">당신의 사진으로 여행 장소를 찾아보아요!</p>
        </div>
      </div>
    </div>

    <!-- Body -->
    <div class="page-body">
      <BaseSection
        icon="bi-camera"
        title="이 사진에서 알고 싶은 정보는 무엇인가요?"
      >
        <div class="preview-wrap mb-4" v-if="preview">
          <img :src="preview" alt="uploaded preview" class="preview-img" />
        </div>

        <ul class="list-unstyled">
          <!-- Landscape -->
          <li
            class="option-item p-3 mb-3 rounded d-flex align-items-center"
            :class="{ selected: selectedType === 'landscape' }"
            @click="setType('landscape')"
          >
            <div class="option-icon gradient-1 me-3">
              <i class="bi bi-image-fill"></i>
            </div>
            <div class="flex-fill">
              <div class="fw-medium">풍경 / 장소 </div>
              <div class="small text-muted">공원, 절, 전망대, 거리</div>
            </div>
            <div class="check-mark" v-if="selectedType === 'landscape'">✓</div>
          </li>

          <!-- Food -->
          <li
            class="option-item p-3 mb-3 rounded d-flex align-items-center"
            :class="{ selected: selectedType === 'food' }"
            @click="setType('food')"
          >
            <div class="option-icon gradient-2 me-3">
              <i class="bi bi-cup-straw"></i>
            </div>
            <div class="flex-fill">
              <div class="fw-medium">음식 / 레스토랑</div>
              <div class="small text-muted">삼겹살, 길거리 음식, 카페</div>
            </div>
            <div class="check-mark" v-if="selectedType === 'food'">✓</div>
          </li>

          <!-- Activities -->
          <li
            class="option-item p-3 mb-2 rounded d-flex align-items-center"
            :class="{ selected: selectedType === 'activities' }"
            @click="setType('activities')"
          >
            <div class="option-icon gradient-3 me-3">
              <i class="bi bi-person-fill"></i>
            </div>
            <div class="flex-fill">
              <div class="fw-medium">활동 / 경험</div>
              <div class="small text-muted">사진 명소, 문화 체험</div>
            </div>
            <div class="check-mark" v-if="selectedType === 'activities'">✓</div>
          </li>
        </ul>
      </BaseSection>
    </div>

    <!-- Footer -->
    <div class="page-footer">
      <NavigationButtons
        back-text="뒤로 가기"
        next-text="다음으로 가기"
        :is-next-disabled="!selectedType"
        @back="goBack"
        @next="goNext"
      />
    </div>
  </section>
</template>


<script setup>
import { ref } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import StepHeader from '@/components/common/header/StepHeader.vue'
import BaseSection from '@/components/common/BaseSection.vue'
import PageHeader from '@/components/common/header/PageHeader.vue'
import NavigationButtons from '@/components/common/button/NavigationButtons.vue';


const goBack = async () => {
  router.push({
    name: 'AiRecommend',
    query: { preview: preview.value, type: selectedType.value }
  })
}
const router = useRouter()
const route = useRoute()

// New.vue에서 query parameter로 전달받은 이미지 데이터
const preview = ref(route.query?.preview || null)

// 선택 상태
const selectedType = ref(null)

// 선택 처리 (시각적 표시)
const setType = (type) => {
  selectedType.value = selectedType.value === type ? null : type
}

// Next 클릭 시 결과 페이지로 이동
const goNext = async () => {
  if (!selectedType.value) return
  
  // AiRecommend 페이지로 이동 (type 정보 전달)
  router.push({
    name: 'AiRecommend',
    query: { preview: preview.value, type: selectedType.value }
  })
}
</script>

<style scoped>
.image-ai-type-page {
  display: flex;
  flex-direction: column;
  min-height: 100vh;
  background: #ffffff;
}

/* Header */
.page-header {
  padding: 1.75rem 2rem 1.25rem;
  border-bottom: 1px solid #e2e8f0;
}

.icon-box {
  width: 42px;
  height: 42px;
  border-radius: 10px;
  background: #f1f5f9;
  display: flex;
  align-items: center;
  justify-content: center;
}

.title {
  font-weight: 700;
}

.sub {
  font-size: 0.85rem;
  color: #64748b;
}

/* Body */
.page-body {
  flex: 1;
  padding: 2.5rem 2rem 3rem; /* 🔥 헤더랑 떨어지는 핵심 */
}

/* Preview */
.preview-wrap {
  background: #f5f7fa;
  padding: 14px;
  border-radius: 14px;
}

.preview-img {
  width: 100%;
  height: 280px;
  object-fit: cover;
  border-radius: 12px;
}

/* Footer */
.page-footer {
  padding: 1.5rem 2rem 2rem; /* 🔥 버튼 바닥에서 띄우기 */
  border-top: 1px solid #e2e8f0;
  background: #ffffff;
}

/* Option items */
.option-item {
  background: #fff;
  border: 1px solid #e5e7eb;
  cursor: pointer;
  transition: all 0.2s ease;
}

.option-item.selected {
  border-color: #1b3b6f;
  background: #f3f7ff;
  box-shadow: 0 6px 18px rgba(27, 59, 111, 0.08);
}

.option-icon {
  width: 56px;
  height: 56px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  font-size: 20px;
}

.check-mark {
  font-weight: 700;
  color: #1b3b6f;
}

/* Gradients */
.gradient-1 { background: linear-gradient(135deg, #6dd3ff, #7be6b8); }
.gradient-2 { background: linear-gradient(135deg, #ffb86b, #ff9ad1); }
.gradient-3 { background: linear-gradient(135deg, #a78bff, #ff9ad1); }
</style>
