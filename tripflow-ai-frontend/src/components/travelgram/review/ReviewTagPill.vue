<template>
  <button
    type="button"
    class="tag-pill"
    :class="pillClasses"
    @click="$emit('click')"
  >
    <span class="hash">#</span>{{ label }}
    <i v-if="selected" class="bi bi-check-lg ms-1 small-icon"></i>
  </button>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  label: {
    type: String,
    required: true,
  },
  selected: {
    type: Boolean,
    default: false,
  },
  variant: {
    type: String,
    default: 'default',
    validator: (value) => ['default', 'selected', 'active'].includes(value),
  },
})

defineEmits(['click'])

const pillClasses = computed(() => ({
  active: props.variant === 'active',
  selected: props.variant === 'selected',
  pop: props.selected,
}))
</script>

<style scoped>
.tag-pill {
  background: #f1f3f5;
  color: #868e96;
  border-radius: 999px;
  padding: 0.5rem 1rem;
  border: none;
  cursor: pointer;
  display: flex;
  align-items: center;
  transition: all 0.2s ease;
}

.tag-pill.active {
  background-color: #1b3b6f;
  color: white;
  box-shadow: 0 4px 12px rgba(27, 59, 111, 0.3);
}

.tag-pill.selected {
  background-color: #ff8c00;
  color: white;
  box-shadow: 0 4px 12px rgba(255, 140, 0, 0.3);
}

.tag-pill.pop {
  animation: pop 0.25s ease;
}

.hash {
  margin-right: 0.15rem;
}

.small-icon {
  font-size: 0.8rem;
}

@keyframes pop {
  0% {
    transform: scale(0.9);
    opacity: 0.7;
  }
  100% {
    transform: scale(1);
    opacity: 1;
  }
}
</style>
