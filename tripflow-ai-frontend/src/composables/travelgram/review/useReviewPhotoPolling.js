import { computed, onBeforeUnmount, ref } from 'vue'

export function useReviewPhotoPolling({ reviewStore, api, uploadedImages }) {
  const pollingInterval = ref(null)

  const totalCount = computed(() => uploadedImages.value.length)
  const settledCount = computed(
    () => uploadedImages.value.filter((image) => image.status === 'SUCCESS' || image.status === 'FAILED').length,
  )
  const successCount = computed(() => uploadedImages.value.filter((image) => image.status === 'SUCCESS').length)
  const failedCount = computed(() => uploadedImages.value.filter((image) => image.status === 'FAILED').length)
  const allSettled = computed(() => totalCount.value > 0 && settledCount.value === totalCount.value)
  const isAnalyzing = computed(() => totalCount.value > 0 && !allSettled.value)
  const canProceed = computed(() => totalCount.value > 0 && successCount.value === totalCount.value)

  const stopPolling = () => {
    if (pollingInterval.value) {
      clearInterval(pollingInterval.value)
      pollingInterval.value = null
    }
  }

  const checkAnalysisStatus = async () => {
    const res = await api.getReviewPhotos(reviewStore.reviewPostId)
    const serverPhotos = res.data.data || []

    uploadedImages.value.forEach((image) => {
      const match = serverPhotos.find((serverPhoto) => String(serverPhoto.id) === String(image.id))
      if (match) {
        image.status = match.status
        image.summary = match.summary
      }
    })

    if (allSettled.value) stopPolling()
  }

  const startPolling = () => {
    if (pollingInterval.value) return
    pollingInterval.value = setInterval(checkAnalysisStatus, 3000)
  }

  const handleReanalyze = async (photoId) => {
    const image = uploadedImages.value.find((item) => String(item.id) === String(photoId))
    if (image) image.status = 'PENDING'

    try {
      await api.reanalyzePhoto(photoId)
      startPolling()
    } catch (error) {
      console.error('재분석 요청 실패:', error)
      if (image) image.status = 'FAILED'
    }
  }

  onBeforeUnmount(stopPolling)

  return {
    totalCount,
    settledCount,
    successCount,
    failedCount,
    allSettled,
    isAnalyzing,
    canProceed,
    startPolling,
    stopPolling,
    checkAnalysisStatus,
    handleReanalyze,
  }
}
