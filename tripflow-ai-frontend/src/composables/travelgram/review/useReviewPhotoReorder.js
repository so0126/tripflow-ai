import { computed, onMounted, ref } from 'vue'

export function useReviewPhotoReorder({ reviewStore, api, router }) {
  const photos = ref([...reviewStore.photos])
  const mainPhotoId = ref(reviewStore.mainPhotoId)
  const isLoading = ref(false)
  const hasError = ref(false)

  const syncMainPhoto = () => {
    if (photos.value.length > 0) {
      mainPhotoId.value = photos.value[0].id
    }
  }

  onMounted(() => {
    if (photos.value.length > 0) {
      mainPhotoId.value = photos.value[0].id
    }
  })

  const selectMain = (id) => {
    const idx = photos.value.findIndex((photo) => photo.id === id)
    if (idx <= 0) return

    const selected = photos.value.splice(idx, 1)[0]
    photos.value.unshift(selected)
    syncMainPhoto()
  }

  const moveUp = (idx) => {
    if (idx === 0) return
    ;[photos.value[idx - 1], photos.value[idx]] = [photos.value[idx], photos.value[idx - 1]]
    syncMainPhoto()
  }

  const moveDown = (idx) => {
    if (idx === 0 || idx >= photos.value.length - 1) return
    ;[photos.value[idx + 1], photos.value[idx]] = [photos.value[idx], photos.value[idx + 1]]
    syncMainPhoto()
  }

  const canProceed = computed(() => photos.value.length > 0 && !!mainPhotoId.value && !isLoading.value)

  const goNext = async () => {
    if (!canProceed.value) return

    isLoading.value = true
    hasError.value = false

    try {
      reviewStore.setPhotos(photos.value)
      reviewStore.setMainPhoto(mainPhotoId.value)

      await api.updatePhotoOrder({
        reviewPostId: reviewStore.reviewPostId,
        photos: photos.value.map((photo, index) => ({
          photoId: photo.id,
          orderIndex: index,
        })),
      })

      await api.analyzePhotoMood(reviewStore.reviewPostId)

      reviewStore.nextStep()
      router.push({
        name: 'CaptionSelect',
        params: { planId: reviewStore.planId },
      })
    } catch (error) {
      console.error('사진 순서 저장 또는 분석 실패', error)
      hasError.value = true
    } finally {
      isLoading.value = false
    }
  }

  const goBack = () => router.push({ name: 'CreateTravelReview' })

  return {
    photos,
    mainPhotoId,
    isLoading,
    hasError,
    selectMain,
    moveUp,
    moveDown,
    canProceed,
    goNext,
    goBack,
  }
}
