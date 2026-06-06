<template>
  <div :class="wrapperClasses">
    <slot>
      <div class="default-state error-state">
        <i :class="['bi', icon, 'error-icon']"></i>
        <p v-if="title" class="state-title">{{ title }}</p>
        <p v-if="description" class="state-description">{{ description }}</p>
        <button v-if="retryText" type="button" class="btn btn-primary" @click="$emit('retry')">
          <i class="bi bi-arrow-clockwise me-1"></i>{{ retryText }}
        </button>
      </div>
    </slot>
  </div>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  mode: {
    type: String,
    default: 'inline',
    validator: (value) => ['inline', 'overlay'].includes(value),
  },
  icon: {
    type: String,
    default: 'bi-exclamation-triangle',
  },
  title: {
    type: String,
    default: '',
  },
  description: {
    type: String,
    default: '',
  },
  retryText: {
    type: String,
    default: '다시 시도',
  },
})

defineEmits(['retry'])

const wrapperClasses = computed(() => [
  'review-error-state',
  `review-error-state--${props.mode}`,
])
</script>

<style scoped>
.review-error-state {
  display: flex;
  align-items: center;
  justify-content: center;
}

.review-error-state--inline {
  min-height: 220px;
  padding: 3rem 0;
}

.review-error-state--overlay {
  position: fixed;
  inset: 0;
  z-index: 9999;
  backdrop-filter: blur(5px);
  background: rgba(255, 255, 255, 0.85);
}

.default-state {
  text-align: center;
  color: #1b3b6f;
}

.error-state {
  gap: 1rem;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 3rem 0;
  min-height: 220px;
}

.error-icon {
  font-size: 2.5rem;
  color: #e07a3f;
}

.state-title {
  margin-bottom: 0.5rem;
  font-weight: 700;
}

.state-description {
  margin-bottom: 0;
  color: #6c757d;
}
</style>
