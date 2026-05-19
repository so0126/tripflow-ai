<template>
  <div class="sub-header mb-4">
    <div class="d-flex justify-content-between align-items-center">
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
      
      <slot name="right-action">
        <h6 v-if="step" class="step-indicator text-muted m-0">
           Step {{ step }}
        </h6>
      </slot>
    </div>
    
    </div>
</template>

<script setup>
defineProps({
  title: { type: String, default: 'Detail View' },
  subtitle: { type: String, default: '' },
  step: { type: [String, Number], default: '' }, // step이 있으면 표시, 없으면 숨김
})
</script>

<style scoped>
/* StepHeader와 동일한 스타일 공유 */
.sub-header {
  padding: 0 0.5rem;
}

.back-icon {
  color: #ff8c00;
  transition: transform 0.2s ease, color 0.2s ease;
  margin-left: -0.5rem; 
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
  opacity: 0.6;
}
</style>