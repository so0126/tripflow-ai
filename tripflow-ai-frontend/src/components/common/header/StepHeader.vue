<template>
  <div class="sub-header mb-4">
    <div class="d-flex justify-content-between align-items-end mb-3">
      <div class="d-flex align-items-center gap-3">
        <i 
          class="bi bi-arrow-left-short back-icon fs-2" 
          @click="$emit('back')"
          role="button"
        ></i>
        
        <div class="d-flex flex-column">
          <h5 class="header-title m-0 fw-bold">{{ title }}</h5>
          <p v-if="subtitle" class="header-subtitle text-muted m-0 mt-1">
            {{ subtitle }}
          </p>
        </div>
      </div>
      
      <span class="step-indicator text-muted">
        Step <span class="text-orange">{{ step }}</span>
      </span>
    </div>

    <div v-if="progressPercent > 0" class="progress-container">
      <div class="progress">
        <div
          class="progress-bar"
          role="progressbar"
          :style="{ width: progressPercent + '%' }"
          aria-valuemin="0" 
          aria-valuemax="100"
        ></div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  title: { type: String, default: 'Create Travel Review' },
  subtitle: { type: String, default: '' },
  step: { type: [String, Number], default: '1/6' },
})

const progressPercent = computed(() => {
  const [current, total] = String(props.step).split('/').map(Number)
  if (!current || !total) return 0
  return Math.min(100, Math.round((current / total) * 100))
})
</script>

<style scoped>
.sub-header {
  padding: 0 0.5rem; /* PageHeader 내부 콘텐츠와 라인 맞춤 */
}

.back-icon {
  color: #ff8c00;
  transition: transform 0.2s ease, color 0.2s ease;
  margin-left: -0.5rem; /* 아이콘의 여백 보정하여 텍스트 라인 정렬 */
}

.back-icon:hover {
  transform: translateX(-4px);
  color: #e07b00;
}

.header-title {
  color: #1B3B6F;
  letter-spacing: -0.5px;
}

.header-subtitle {
  font-size: 1.15rem;
}

.step-indicator {
  font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, "Helvetica Neue", Arial, "Noto Sans KR", sans-serif;
  font-size: 0.9rem;
  letter-spacing: 0.5px;
}

.text-orange {
  color: #ff8c00;
  font-weight: 600;
}

/* 프로그레스 바 스타일 고도화 */
.progress {
  height: 10px; /* 너무 두껍지 않게 조정 (세련됨 유지) */
  background-color: #f1f3f5;
  border-radius: 10px;
  overflow: hidden;
}

.progress-bar {
  background-color: #ff8c00;
  border-radius: 10px;
  transition: width 0.6s cubic-bezier(0.25, 1, 0.5, 1);
}
</style>