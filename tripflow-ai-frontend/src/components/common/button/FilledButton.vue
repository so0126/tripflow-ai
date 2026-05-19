<template>
  <button
    class="btn-filled"
    :class="`btn-${size}`"
    :disabled="disabled"
  >
    <slot />
  </button>
</template>

<script setup>
defineProps({
  disabled: { type: Boolean, default: false },

  /* 색상 */
  bgColor: { type: String, default: '#1b3b6f' },
  textColor: { type: String, default: '#ffffff' },
  hoverColor: { type: String, default: '#ff8c00' },

  /* 사이즈 */
  size: {
    type: String,
    default: 'lg',
    validator: v => ['sm', 'md', 'lg'].includes(v)
  }
});
</script>

<style scoped>
.btn-filled {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  border-radius: 1rem;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
  width: 100%;
  border: none;

  background-color: v-bind(bgColor);
  color: v-bind(textColor);
}

.btn-filled:hover:not(:disabled) {
  background-color: color-mix(
    in srgb,
    v-bind(bgColor) 90%,
    black
  );
}

.btn-filled:active:not(:disabled) {
  background-color: color-mix(
    in srgb,
    v-bind(bgColor) 80%,
    black
  );
}

.btn-filled:disabled {
  background-color: #b0bfd8;
  color: #ffffff;
  cursor: not-allowed;
  opacity: 1;
}

/* ===== Size Variants ===== */
.btn-sm {
  height: 32px;
  font-size: 1rem;
  padding: 0 0.75rem;
}

.btn-md {
  height: 40px;
  font-size: 1.25rem;
  padding: 0 1rem;
}

.btn-lg {
  height: 48px;
  font-size: 1.5rem;
  padding: 0 1.25rem;
}
</style>
