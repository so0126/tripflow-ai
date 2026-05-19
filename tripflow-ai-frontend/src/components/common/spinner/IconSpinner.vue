<template>
  <div class="icon-spinner">
    <div class="icon-row">
      <div
        v-for="step in animatedSteps"
        :key="step.key"
        class="icon-item"
        :style="{ animationDelay: step.delay }"
      >
        <i :class="['bi', step.icon]"></i>
      </div>
    </div>

    <p class="spinner-text">
      {{ text }}
    </p>
  </div>
</template>


<script setup>
import { computed } from 'vue'

/**
 * Props 정의
 */
const props = defineProps({
  /**
   * 여정 아이콘 목록
   * ex) [{ key: 1, icon: 'bi-camera' }]
   */
  steps: {
    type: Array,
    required: true
  },

  /**
   * 로딩 문구
   */
  text: {
    type: String,
    default: 'AI가 여행 이야기를 만들고 있어요…'
  },

  /**
   * 아이콘 애니메이션 간격 (초)
   */
  delayGap: {
    type: Number,
    default: 0.25
  }
})

/**
 * 아이콘별 animation-delay 계산
 */
const animatedSteps = computed(() =>
  props.steps.map((step, index) => ({
    ...step,
    delay: `${index * props.delayGap}s`
  }))
)
</script>

<style scoped>
.icon-spinner {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 3.5rem 0;
  text-align: center;
}

.icon-row {
  display: flex;
  gap: 0.75rem;
  margin-bottom: 1.2rem;
}

.icon-item {
  width: 42px;
  height: 42px;
  border-radius: 50%;
  background: #fff3e6;

  display: flex;
  align-items: center;
  justify-content: center;

  color: #ff8c00;

  opacity: 0;
  transform: translateY(6px) scale(0.9);
  animation: journeyPulse 1.6s ease-in-out infinite;
}

@keyframes journeyPulse {
  0% {
    opacity: 0;
    transform: translateY(6px) scale(0.9);
  }
  30% {
    opacity: 1;
    transform: translateY(0) scale(1);
  }
  60% {
    opacity: 0.4;
  }
  100% {
    opacity: 0;
    transform: translateY(-6px) scale(0.9);
  }
}

.spinner-text {
  color: #6b7280;
}


</style>