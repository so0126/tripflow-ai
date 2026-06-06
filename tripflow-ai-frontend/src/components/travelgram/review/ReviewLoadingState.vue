<template>
  <div :class="wrapperClasses">
    <slot>
      <div class="default-state loading-state">
        <div class="spinner-border mb-3" :class="spinnerClass"></div>
        <h5 v-if="title" class="state-title">{{ title }}</h5>
        <p v-if="description" class="state-description">{{ description }}</p>
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
  title: {
    type: String,
    default: '',
  },
  description: {
    type: String,
    default: '',
  },
  spinnerClass: {
    type: String,
    default: 'text-primary',
  },
})

const wrapperClasses = computed(() => [
  'review-loading-state',
  `review-loading-state--${props.mode}`,
])
</script>

<style scoped>
.review-loading-state {
  display: flex;
  align-items: center;
  justify-content: center;
}

.review-loading-state--inline {
  min-height: 220px;
  padding: 3rem 0;
}

.review-loading-state--overlay {
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

.state-title {
  margin-bottom: 0.5rem;
  font-weight: 700;
}

.state-description {
  margin-bottom: 0;
  color: #6c757d;
}
</style>
