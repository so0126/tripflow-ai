<template>
  <header class="travelgram-header p-4 pb-3 border-bottom d-flex align-items-center justify-content-between">
    <div class="d-flex gap-3 align-items-center">
      <button
        v-if="showBack"
        class="btn btn-link p-0 back-button"
        type="button"
        title="뒤로 가기"
        @click="goBack"
      >
        <i class="bi bi-arrow-left-short fs-1"></i>
      </button>

      <div class="travelgram-icon rounded-3 bg-secondary-subtle d-flex align-items-center justify-content-center">
        {{ icon }}
      </div>

      <div>
        <h5 class="mb-1 title">{{ title }}</h5>
        <p class="text-muted small mb-0 sub">
          {{ subtitle }}
        </p>
      </div>
    </div>
  </header>
</template>

<script setup>
import { useRouter } from 'vue-router'

defineProps({
  showBack: {
    type: Boolean,
    default: true
  },
  title: {
    type: String,
    default: '트래블그램'
  },
  subtitle: {
    type: String,
    default: '당신의 여행 추억을 기록하고 공유하세요'
  },
  icon: {
    type: String,
    default: '💖'
  }
})

const router = useRouter()

const goBack = () => {
  router.back()
}

</script>

<style scoped>
.travelgram-header {
  background: #fff;
}

.travelgram-icon {
  width: 46px;
  height: 46px;
  flex: 0 0 46px;
}

.title {
  font-weight: 600;
  color: #1b3b6f;
}

.sub {
  line-height: 1.2;
}

.back-button {
  color: #1b3b6f;
  text-decoration: none;
}
</style>
