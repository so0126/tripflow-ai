<template>
  <div class="journey-header">
    <!-- 상단: 뒤로가기 + 타이틀 -->
    <div class="journey-top">
      <div class="left">
        <i
          class="bi bi-arrow-left-short back-icon"
          role="button"
          @click="$emit('back')"
        ></i>

        <div class="title-block">
          <div class="journey-title">
            {{ title }}
          </div>
          <div class="journey-subtitle">
            {{ currentStepDescription }}
          </div>
        </div>
      </div>

      <div class="step-indicator">
        Step <span class="step-current">{{ currentStep }}</span
        >/<span>{{ totalSteps }}</span>
      </div>
    </div>

    <!-- 여정 스텝 -->
    <div class="journey-steps">
      <div
        v-for="stepItem in steps"
        :key="stepItem.key"
        class="journey-step"
        :class="{
          done: stepItem.key < currentStep,
          active: stepItem.key === currentStep
        }"
      >
        <div class="step-icon">
          <i :class="['bi', stepItem.icon]"></i>
        </div>
        <div class="step-label">{{ stepItem.label }}</div>
      </div>
    </div>

    <!-- 정확한 진행률 -->
    <div class="progress-wrapper">
      <div class="progress">
        <div
          class="progress-bar"
          :style="{ width: progressPercent + '%' }"
        ></div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  title: {
    type: String,
    default: '여행 후기 작성'
  },
  currentStep: {
    type: Number,
    required: true
  },
  steps: {
    type: Array,
    required: true
  }
})

const totalSteps = computed(() => props.steps.length)

const currentStepDescription = computed(() => {
  const current = props.steps.find(s => s.key === props.currentStep)
  return current
    ? `지금은 “${current.label}” 단계예요`
    : ''
})

const progressPercent = computed(() => {
  return Math.round((props.currentStep / totalSteps.value) * 100)
})
</script>

<style scoped>
/* =========================
   컴포넌트 타이포 스케일 캡슐화
   (전역 typography 영향 ❌)
========================= */

.journey-header {
  padding: 0 0.5rem;
  font-size: 1em; /* body 기준 유지 */
  margin-bottom: 1.75rem; /* 🔥 다음 카드와 거리 확보 */
}

/* =========================
   상단 영역
========================= */

.journey-top {
  display: flex;
  justify-content: space-between;
  align-items: flex-end;
  margin-bottom: 1.25em;
}

.left {
  display: flex;
  align-items: center;
  gap: 0.6em;
}

.back-icon {
  font-size: 2.2em;          /* 🔥 아이콘 크게 */
  color: #ff8c00;
  margin-left: -0.4em;
  transition: transform 0.2s ease;
}
.back-icon:hover {
  transform: translateX(-4px);
}

/* 제목 블록 */
.title-block {
  display: flex;
  flex-direction: column;
  gap: 0.25em;
}

.journey-title {
  font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, "Helvetica Neue", Arial, "Noto Sans KR", sans-serif;
  font-size: 1.35em;         /* 🔥 제목 체급 상승 */
  font-weight: 700;
  line-height: 1.2;
  color: #1b3b6f;
}

.journey-subtitle {
  font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, "Helvetica Neue", Arial, "Noto Sans KR", sans-serif;
  font-size: 1.05em;         /* 🔥 부제 가독성 확보 */
  color: #6b7280;
}

/* Step 숫자 */
.step-indicator {
  font-size: 0.9em;
  color: #9ca3af;
}
.step-current {
  color: #ff8c00;
  font-weight: 700;
}

/* =========================
   여정 스텝
========================= */

.journey-steps {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;

  margin-top: 1.2em;      /* 🔥 위 여백 */
  margin-bottom: 1.6em;   /* 🔥 아래 여백 */

  padding: 0.6em 0.4em;   /* 🔥 좌우 숨 */
}

.journey-step {
  flex: 1;
  text-align: center;
  font-size: 0.8em;
  color: #c4c4c4;
  position: relative;

  padding: 0.2em 0; /* 🔥 세로 숨 */
}


.step-icon {
  width: 2.2em;
  height: 2.2em;
  margin: 0 auto 0.25em;
  border-radius: 50%;
  background: #f1f3f5;
  display: flex;
  align-items: center;
  justify-content: center;
}

.step-icon i {
  font-size: 1.1em;
}

/* 라벨 */
.step-label {
  white-space: nowrap;
}

/* 상태 */
.journey-step.done {
  color: #ff8c00;
}
.journey-step.done .step-icon {
  background: #ffedd5;
}

.journey-step.active {
  color: #1b3b6f;
  font-weight: 700;
}
.journey-step.active .step-icon {
  background: #ffe0b2;
}

/* 연결선 */
.journey-step:not(:last-child)::after {
  content: '';
  position: absolute;
  top: 1.1em;
  right: -50%;
  width: 100%;
  height: 2px;
  background: #e5e7eb;
  z-index: -1;
}

.journey-step.done:not(:last-child)::after {
  background: linear-gradient(90deg, #ff8c00, #ffd39c);
}

/* =========================
   Progress Bar
========================= */

.progress-wrapper {
  margin-top: 1.2em; /* 🔥 여정과 거리 */
}

.progress {
  height: 1.5em;
  background: #f1f3f5;
  border-radius: 10px;
  overflow: hidden;
}

.progress-bar {
  height: 100%;
  background: linear-gradient(
    90deg,
    #ff8c00,
    #ffb347
  ); /* 🔥 그라데이션 */

  border-radius: 999px;
  transition: width 0.6s cubic-bezier(0.25, 1, 0.5, 1);
}
</style>
