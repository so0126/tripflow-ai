import { computed, onMounted, ref } from 'vue'

export function useInstagramPreview({ reviewStore, authStore, router }) {
  onMounted(() => {
    if (!authStore.isLoggedIn) {
      authStore.initializeAuth()
    }
  })

  const userInfo = computed(() => {
    const name = authStore.userName || 'Traveler'
    return {
      handle: name.toLowerCase().replace(/\s+/g, '.'),
      profileImage: authStore.userProfileImage,
      location: '대한민국, 서울',
    }
  })

  const likes = ref(1234)
  const currentIndex = ref(0)

  const canProceed = computed(() => reviewStore.photos && reviewStore.photos.length > 0)

  const currentPhoto = computed(() => {
    if (!reviewStore.photos?.length) return null
    return reviewStore.photos[currentIndex.value]
  })

  const prevPhoto = () => {
    if (currentIndex.value > 0) currentIndex.value--
  }

  const nextPhoto = () => {
    if (currentIndex.value < reviewStore.photos.length - 1) {
      currentIndex.value++
    }
  }

  const handleImageError = (event) => {
    console.error('Image load failed:', event.target.src)
  }

  const copyToClipboard = () => {
    const caption = reviewStore.caption || ''
    const tags = reviewStore.selectedHashtags.map((tag) => `#${tag.name}`).join(' ')
    const text = `${caption}\n\n${tags}`.trim()

    navigator.clipboard.writeText(text).then(() => {
      alert('📋 Copied!')
    })
  }

  const goBack = () => router.push({ name: 'EditPage' })

  const publish = () => {
    alert('✅ 게시물이 준비되었습니다!')
    router.push({ name: 'CompleteReview' })
  }

  return {
    userInfo,
    likes,
    currentIndex,
    canProceed,
    currentPhoto,
    prevPhoto,
    nextPhoto,
    handleImageError,
    copyToClipboard,
    goBack,
    publish,
  }
}
