<template>
  <!-- ================= Header ================= -->
  <div class="p-4 pb-3 border-bottom d-flex align-items-center justify-content-between">
    <div class="d-flex gap-3 align-items-center">
      <button class="btn btn-link p-0 back-button" @click="$router.back()" title="뒤로 가기">
        <i class="bi bi-arrow-left-short fs-1"></i>
      </button>

      <div
        class="rounded-3 bg-secondary-subtle d-flex align-items-center justify-content-center"
        style="width: 46px; height: 46px"
      >
        💖
      </div>

      <div>
        <h5 class="mb-1 title">트래블그램</h5>
        <p class="text-muted small mb-0 sub">
          당신의 여행 추억을 기록하고 공유하세요
        </p>
      </div>
    </div>
  </div>

  <!-- ================= Page Layout ================= -->
  <div class="travelgram-page">
    <div class="page-inner">

      <!-- ================= Photo Order Card ================= -->
      <section class="photo-order-section">
        <div class="photo-order-card">

          <TipBox
            name="대표 사진 안내"
            description="대표 사진은 삭제할 수 없습니다.
                         사진 순서만 변경할 수 있습니다."
          />

          <transition-group
            name="photo-move"
            tag="div"
            class="photo-list"
          >
            <div
              v-for="(photo, index) in photos"
              :key="photo.id"
              class="photo-item"
              :class="{ active: photo.id === mainPhotoId }"
              @click="selectMain(photo.id)"
            >
              <div class="photo-thumb">
                <img :src="photo.url" />
              </div>

              <div class="photo-info flex-grow-1">
                <h6 class="photo-name">
                  {{ photo.name?.replace(/\.[^/.]+$/, '') }}
                </h6>
                <p>{{ reviewStore.planTitle }}</p>
              </div>

              <div class="photo-actions d-flex align-items-center" @click.stop>
                <button
                  class="btn btn-sm btn-outline-secondary me-1"
                  @click="moveUp(index)"
                >
                  <i class="bi bi-arrow-up"></i>
                </button>
                <button
                  class="btn btn-sm btn-outline-secondary"
                  @click="moveDown(index)"
                >
                  <i class="bi bi-arrow-down"></i>
                </button>
              </div>
            </div>
          </transition-group>

        </div>
      </section>

      <!-- ================= Navigation ================= -->
      <NavigationButtons
        backText="뒤로가기"
        :isNextDisabled="!canProceed"
        @back="goBack"
        @next="goNext"
      >
        <template #next-content>
          <span v-if="isLoading">
            <span class="spinner-border spinner-border-sm me-2"></span>
            AI가 열심히 분석 중 입니다...
          </span>
          <span v-else>다음으로</span>
        </template>
      </NavigationButtons>
    </div>

    <!-- ================= Loading Overlay ================= -->
    <div v-if="isLoading" class="loading-overlay">
      <div class="loading-content">
        <div class="spinner-border text-primary mb-3"></div>
        <h5>사진 요약을 모아서 분석 중 입니다.</h5>
        <p>여행의 분위기를 감지하고 있습니다...</p>
      </div>
    </div>

    <!-- ================= Error Overlay ================= -->
    <div v-if="hasError" class="error-overlay">
      <div class="error-overlay-card">
        <ErrorRetry @retry="goNext">
          분석에 실패했어요.<br />잠시 후 다시 시도해 주세요.
        </ErrorRetry>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useReviewStore } from '@/store/reviewStore'
import api from '@/api/travelgramApi'

import TipBox from '@/components/common/TipBox.vue'
import NavigationButtons from '@/components/common/button/NavigationButtons.vue'
import ErrorRetry from '@/components/travelgram/ErrorRetry.vue'

const router = useRouter()
const reviewStore = useReviewStore()

/* ================= State ================= */
const photos = ref([...reviewStore.photos])
const mainPhotoId = ref(reviewStore.mainPhotoId)
const isLoading = ref(false)
const hasError = ref(false)

/* ================= Init ================= */
onMounted(() => {
  if (photos.value.length > 0) {
    mainPhotoId.value = photos.value[0].id
  }
})

const syncMainPhoto = () => {
  if (photos.value.length > 0) {
    mainPhotoId.value = photos.value[0].id
  }
}

/* ================= Actions ================= */
const selectMain = (id) => {
  const idx = photos.value.findIndex(p => p.id === id)
  if (idx <= 0) return

  const selected = photos.value.splice(idx, 1)[0]
  photos.value.unshift(selected)
  syncMainPhoto()
}

const moveUp = (idx) => {
  if (idx === 0) return
  ;[photos.value[idx - 1], photos.value[idx]] =
    [photos.value[idx], photos.value[idx - 1]]
  syncMainPhoto()
}

const moveDown = (idx) => {
  if (idx === 0 || idx >= photos.value.length - 1) return
  ;[photos.value[idx + 1], photos.value[idx]] =
    [photos.value[idx], photos.value[idx + 1]]
  syncMainPhoto()
}

/* ================= Navigation ================= */
const canProceed = computed(() =>
  photos.value.length > 0 && !!mainPhotoId.value && !isLoading.value
)

const goNext = async () => {
  if (!canProceed.value) return
  isLoading.value = true
  hasError.value = false

  try {
    reviewStore.setPhotos(photos.value)
    reviewStore.setMainPhoto(mainPhotoId.value)

    await api.updatePhotoOrder({
      photoGroupId: reviewStore.photoGroupId,
      photos: photos.value.map((p, i) => ({
        photoId: p.id,
        orderIndex: i
      }))
    })

    await api.analyzePhotoMood(reviewStore.photoGroupId)

    reviewStore.nextStep()
    router.push({
      name: 'CaptionSelect',
      params: { planId: reviewStore.planId }
    })
  } catch (e) {
    // 순서 저장/분석 실패(5xx·네트워크) — 사진 순서 화면은 유지하고 재시도 가능하게
    console.error('사진 순서 저장 또는 분석 실패', e)
    hasError.value = true
  } finally {
    isLoading.value = false
  }
}

const goBack = () => router.push({ name: 'CreateTravelReview' })
</script>

<style scoped>
/* ================= Page Background ================= */
.travelgram-page {
  min-height: 100vh;
  display: flex;
  justify-content: center;
}

/* ================= Content Width ================= */
.page-inner {
  width: 100%;
  max-width: 1200px;
  padding: 50px 16px 32px;
}

/* ================= Card ================= */
.photo-order-card {
  background: #fff;
  border-radius: 20px;
  padding: 2.25rem 2rem;
  margin-bottom: 24px;
  box-shadow: 0 8px 24px rgba(27, 59, 111, 0.08);
}

/* ================= Photo List ================= */
.photo-list {
  margin-top: 1rem;
}

.photo-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px;
  border-radius: 12px;
  border: 1px solid #e6e6e6;
  cursor: pointer;
  margin-bottom: 12px;
  transition: all .25s ease;
}

.photo-item:hover {
  background: #f0f4f9;
}

.photo-item.active {
  border: 2px solid #1B3B6F;
  background: #e8eef7;
}

.photo-thumb img {
  width: 64px;
  height: 64px;
  border-radius: 10px;
  object-fit: cover;
}

/* ================= Animation ================= */
.photo-move-move {
  transition: transform 0.25s ease;
}

/* ================= Loading ================= */
.loading-overlay {
  position: fixed;
  inset: 0;
  background: rgba(255, 255, 255, 0.85);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 9999;
  backdrop-filter: blur(5px);
}

.loading-content {
  text-align: center;
  color: #1b3b6f;
}

/* ================= Error ================= */
.error-overlay {
  position: fixed;
  inset: 0;
  background: rgba(255, 255, 255, 0.85);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 9999;
  backdrop-filter: blur(5px);
}

.error-overlay-card {
  background: #fff;
  border-radius: 16px;
  padding: 0.5rem 2.5rem;
  box-shadow: 0 8px 24px rgba(27, 59, 111, 0.12);
}
</style>
