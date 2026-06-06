import { computed, ref, watch } from 'vue'

export function useReviewCaptionEditor({ reviewStore, api, route, router }) {
  const photos = computed(() => reviewStore.photos)
  const caption = ref(reviewStore.caption || '')
  const selectedHashtags = computed(() => reviewStore.selectedHashtags || [])
  const currentPhotoIndex = ref(0)
  const isSaving = ref(false)

  const canProceed = computed(() => photos.value && photos.value.length > 0 && !isSaving.value)

  const captionByteLength = computed(() => {
    let total = 0
    for (let index = 0; index < caption.value.length; index++) {
      total += caption.value.charCodeAt(index) > 127 ? 2 : 1
    }
    return total
  })

  watch(caption, (value) => {
    reviewStore.caption = value
  })

  const prevPhoto = () => {
    if (currentPhotoIndex.value > 0) {
      currentPhotoIndex.value--
      scrollToPhoto()
    }
  }

  const nextPhoto = () => {
    if (currentPhotoIndex.value < photos.value.length - 1) {
      currentPhotoIndex.value++
      scrollToPhoto()
    }
  }

  const scrollToPhoto = () => {
    const carousel = document.querySelector('.photo-carousel')
    if (!carousel) return

    const item = carousel.querySelector('.photo-item')
    if (!item) return

    const itemWidth = item.offsetWidth + 16
    carousel.scrollLeft = currentPhotoIndex.value * itemWidth
  }

  const goBack = () => router.push({ name: 'HashtagSelect' })

  const goNext = async () => {
    isSaving.value = true
    try {
      reviewStore.setCaption(caption.value)
      if (reviewStore.reviewPostId) {
        await api.updateCaption(reviewStore.reviewPostId, caption.value)
      }
      router.push({
        name: 'InstagramPreview',
        params: { planId: route.params.planId },
      })
    } catch (error) {
      alert('저장에 실패했습니다.')
    } finally {
      isSaving.value = false
    }
  }

  return {
    photos,
    caption,
    selectedHashtags,
    currentPhotoIndex,
    isSaving,
    canProceed,
    captionByteLength,
    prevPhoto,
    nextPhoto,
    scrollToPhoto,
    goBack,
    goNext,
  }
}
