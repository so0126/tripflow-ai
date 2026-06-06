import { computed, onMounted, ref } from 'vue'

export function useReviewStyleSelection({ reviewStore, api, router }) {
  const isLoading = ref(false)
  const hasError = ref(false)
  const isAnalyzing = ref(false)
  const selectedIndex = ref(null)

  const canProceed = computed(() => selectedIndex.value !== null)

  const loadStyles = async () => {
    isLoading.value = true
    hasError.value = false

    try {
      const res = await api.generateAiStyles(reviewStore.planId, reviewStore.reviewPostId)
      reviewStore.setGeneratedOptions(res.data.data)
    } catch (error) {
      console.error('AI 스타일 생성 실패', error)
      hasError.value = true
    } finally {
      isLoading.value = false
    }
  }

  onMounted(() => {
    if (reviewStore.generatedOptions.length > 0) return
    loadStyles()
  })

  const selectStyle = (index) => {
    selectedIndex.value = index
  }

  const getLabelClass = (code) => {
    switch (code) {
      case 'EMOTIONAL':
        return 'poetic'
      case 'INFORMATIVE':
        return 'inspirational'
      case 'WITTY':
        return 'fun'
      default:
        return 'casual'
    }
  }

  const goBack = () => router.push({ name: 'PhotoOrder' })

  const goNext = async () => {
    if (selectedIndex.value === null) return

    isAnalyzing.value = true
    try {
      const selected = reviewStore.generatedOptions[selectedIndex.value]
      reviewStore.selectStyleOption(selected)
      await api.selectStyle(reviewStore.reviewPostId, reviewStore.reviewStyleId)
      reviewStore.nextStep()
      router.push({ name: 'HashtagSelect' })
    } finally {
      isAnalyzing.value = false
    }
  }

  return {
    isLoading,
    hasError,
    isAnalyzing,
    selectedIndex,
    canProceed,
    loadStyles,
    selectStyle,
    getLabelClass,
    goBack,
    goNext,
  }
}
