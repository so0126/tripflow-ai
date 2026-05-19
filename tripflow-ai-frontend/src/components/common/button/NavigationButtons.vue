<template>
  <div class="navigation-buttons">
    <OutlineButton
      class="flex-item"
      :size="size"
      :main-color="themeColor"
      :hover-color="activeColor"
      @click="$emit('back')"
    >
      {{ backText }}
    </OutlineButton>

    <FilledButton
      class="flex-item"
      :size="size"
      :disabled="isNextDisabled"
      :bg-color="themeColor"
      :hover-color="activeColor"
      @click="$emit('next')"
    >
      <slot name="next-content">
        {{ nextText }}
      </slot>
    </FilledButton>
  </div>
</template>

<script setup>
import OutlineButton from './OutlineButton.vue';
import FilledButton from './FilledButton.vue';

defineProps({
  backText: { type: String, default: '뒤로가기' },
  nextText: { type: String, default: '다음으로' },
  isNextDisabled: { type: Boolean, default: false },

  /* 사이즈 */
  size: {
    type: String,
    default: 'lg', // sm | md | lg
    validator: v => ['sm', 'md', 'lg'].includes(v)
  },

  /* 테마 */
  themeColor: { type: String, default: '#2d4a8f' },
  activeColor: { type: String, default: '#4a6bb5' }
});

defineEmits(['back', 'next']);
</script>

<style scoped>
.navigation-buttons {
  display: flex;
  gap: 0.75rem;
  margin-top: 2rem;
  width: 100%;
}

.flex-item {
  flex: 1;
}
</style>
