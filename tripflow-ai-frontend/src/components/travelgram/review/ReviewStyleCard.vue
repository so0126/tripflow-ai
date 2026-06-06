<template>
  <div
    class="caption-card"
    :class="{ active: selected }"
    @click="$emit('select')"
  >
    <div class="caption-card-header">
      <span class="caption-label" :class="labelClass">
        {{ item.style.name }}
      </span>

      <i v-if="selected" class="bi bi-check-circle-fill checkmark"></i>
    </div>

    <p class="caption-text">
      {{ item.style.caption }}
    </p>

    <div class="hashtag-preview">
      <small class="text-muted">
        {{ item.hashtags.map((h) => '#' + h.name).slice(0, 3).join(' ') }}
      </small>
    </div>
  </div>
</template>

<script setup>
defineProps({
  item: {
    type: Object,
    required: true,
  },
  selected: {
    type: Boolean,
    default: false,
  },
  labelClass: {
    type: String,
    default: 'casual',
  },
})

defineEmits(['select'])
</script>

<style scoped>
.caption-card {
  background: #fff;
  border-radius: 1.25rem;
  padding: 1.4rem 1.5rem;
  border: 2px solid transparent;
  box-shadow: 0 6px 18px rgba(0, 0, 0, 0.05);
  cursor: pointer;
  transition: all 0.25s ease;
}

.caption-card:hover {
  background-color: #fff7ed;
}

.caption-card.active {
  border-color: #ff8c00;
  background-color: #fffaf3;
}

.caption-card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 0.75rem;
}

.caption-label {
  font-weight: 600;
  padding: 0.25rem 0.7rem;
  border-radius: 999px;
}

.poetic {
  background: #efe4ff;
  color: #6a1b9a;
}

.inspirational {
  background: #e6f4ea;
  color: #2e7d32;
}

.fun {
  background: #ffe4e9;
  color: #c2185b;
}

.casual {
  background: #fff3cd;
  color: #d9822b;
}

.caption-text {
  line-height: 1.6;
  color: #333;
}

.hashtag-preview {
  margin-top: 0.8rem;
  color: #999;
}

.checkmark {
  color: #ff8c00;
}
</style>
