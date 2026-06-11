import { computed, onBeforeUnmount, ref } from 'vue'

export function useReviewPhotoPolling({ reviewStore, api, uploadedImages }) {
  const POLLING_STATUS = {
    IDLE: 'idle',
    POLLING: 'polling',
    ERROR: 'error',
    TIMEOUT: 'timeout',
  }
  const MAX_POLLING_ATTEMPTS = 20 // 1분
  const POLLING_ERROR_MESSAGE = '상태 확인에 실패했어요. 새로고침 해주세요.'
  const POLLING_TIMEOUT_MESSAGE = '분석이 지연돼요. 잠시 후 재시도해주세요.'

  const pollingStatus = ref(POLLING_STATUS.IDLE)
  const pollingFailCount = ref(0)
  const pollingAttemptCount = ref(0)
  const pollingInterval = ref(null)

  const totalCount = computed(() => uploadedImages.value.length)
  const settledCount = computed(
    () => uploadedImages.value.filter((image) => image.status === 'SUCCESS' || image.status === 'FAILED').length,
  )
  const successCount = computed(() => uploadedImages.value.filter((image) => image.status === 'SUCCESS').length)
  const failedCount = computed(() => uploadedImages.value.filter((image) => image.status === 'FAILED').length)
  const allSettled = computed(() => totalCount.value > 0 && settledCount.value === totalCount.value)
  const isAnalyzing = computed(() => pollingStatus.value === POLLING_STATUS.POLLING)
  const canProceed = computed(() => totalCount.value > 0 && successCount.value === totalCount.value)

  const resetPollingState = () => {
    pollingStatus.value = POLLING_STATUS.IDLE
    pollingFailCount.value = 0
    pollingAttemptCount.value = 0
  }

  const clearPollingTimer = () => {
    if (!pollingInterval.value) return

    clearInterval(pollingInterval.value)
    pollingInterval.value = null
  }

  const stopPolling = (nextStatus = POLLING_STATUS.IDLE) => {
    clearPollingTimer()
    pollingStatus.value = nextStatus
  }

  const checkAnalysisStatus = async () => {
    pollingAttemptCount.value += 1

    try {
      const res = await api.getReviewPhotos(reviewStore.reviewPostId)
      const serverPhotos = res.data.data || []

      uploadedImages.value.forEach((image) => {
        const match = serverPhotos.find((serverPhoto) => String(serverPhoto.id) === String(image.id))
        if (match) {
          image.status = match.status
          image.summary = match.summary
        }
      })

      pollingFailCount.value = 0

      if (allSettled.value) stopPolling()
      if (pollingAttemptCount.value >= MAX_POLLING_ATTEMPTS && pollingStatus.value === POLLING_STATUS.POLLING) {
        stopPolling(POLLING_STATUS.TIMEOUT)
      }
    } catch (error) {
      console.error('사진 분석 상태 조회 실패:', error)
      pollingFailCount.value += 1

      if (pollingFailCount.value >= 3) {
        stopPolling(POLLING_STATUS.ERROR)
        return
      }

      if (pollingAttemptCount.value >= MAX_POLLING_ATTEMPTS) {
        stopPolling(POLLING_STATUS.TIMEOUT)
      }
    }
  }

  const startPolling = () => {
    if (pollingInterval.value) return // 중복 시작 방지 가드, null이 아니면 폴링 중이라는 의미

    resetPollingState()
    pollingStatus.value = POLLING_STATUS.POLLING
    pollingInterval.value = setInterval(checkAnalysisStatus, 3000)
  }

  const handleReanalyze = async (photoId) => {
    const image = uploadedImages.value.find((item) => String(item.id) === String(photoId))
    if (image) image.status = 'PENDING'

    try {
      await api.reanalyzePhoto(photoId)
      stopPolling()
      startPolling()
    } catch (error) {
      console.error('재분석 요청 실패:', error)
      if (image) image.status = 'FAILED'
    }
  }

  onBeforeUnmount(stopPolling)

  return {
    pollingStatus,
    pollingFailCount,
    pollingAttemptCount,
    pollingErrorMessage: POLLING_ERROR_MESSAGE,
    pollingTimeoutMessage: POLLING_TIMEOUT_MESSAGE,
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
