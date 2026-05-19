<template>
  <button
    class="btn-outline"
    :class="`btn-${size}`"
  >
    <slot />
  </button>
</template>

<script setup>
defineProps({
  /* 색상 */
  mainColor: { type: String, default: '#1b3b6f' },
  hoverColor: { type: String, default: '#ff8c00' },
  hoverTextColor: { type: String, default: '#ffffff' },

  /* 사이즈 */
  size: {
    type: String,
    default: 'lg', // sm | md | lg
    validator: v => ['sm', 'md', 'lg'].includes(v)
  }
});
</script>

<style scoped>
.btn-outline {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  border-radius: 1rem;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
  width: 100%;
  background-color: #fff;

  color: v-bind(mainColor);
  border: 2px solid v-bind(mainColor);
}
.btn-outline:hover {
  background-color: color-mix(
    in srgb,
    v-bind(mainColor) 8%,
    white
  );
}

.btn-outline:active {
  background-color: color-mix(
    in srgb,
    v-bind(mainColor) 15%,
    white
  );
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
