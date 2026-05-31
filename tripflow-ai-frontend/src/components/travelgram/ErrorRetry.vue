<template>
  <div class="error-state">
    <i :class="['bi', icon, 'error-icon']"></i>
    <p class="error-text">
      <!-- 문구는 화면마다 다르므로 slot으로 받음 (없으면 기본 문구) -->
      <slot>문제가 발생했어요. 잠시 후 다시 시도해 주세요.</slot>
    </p>
    <button class="btn btn-primary" @click="$emit('retry')">
      <i class="bi bi-arrow-clockwise me-1"></i>{{ retryText }}
    </button>
  </div>
</template>

<script setup>
defineProps({
  icon: { type: String, default: 'bi-exclamation-triangle' },
  retryText: { type: String, default: '다시 시도' }
})
defineEmits(['retry'])
</script>

<style scoped>
.error-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  text-align: center;
  gap: 1rem;
  padding: 3rem 0;
  min-height: 220px;
}

.error-icon {
  font-size: 2.5rem;
  color: #e07a3f;
}

.error-text {
  color: #6c757d;
  margin: 0;
  line-height: 1.6;
}
</style>
