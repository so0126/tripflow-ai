import { computed, onMounted, ref } from 'vue'

export function useReviewHashtagEditor({ reviewStore, api, router }) {
  const aiTags = ref([])
  const selectedSet = ref(new Set())
  const newTagInput = ref('')

  const selectedCount = computed(() => selectedSet.value.size)

  onMounted(() => {
    aiTags.value = reviewStore.aiHashtags || []

    const selected = reviewStore.selectedHashtags || []
    selectedSet.value.clear()
    selected.forEach((tag) => selectedSet.value.add(tag.name))
  })

  const isSelected = (name) => selectedSet.value.has(name)

  const toggleTag = (name) => {
    if (selectedSet.value.has(name)) {
      selectedSet.value.delete(name)
    } else {
      selectedSet.value.add(name)
    }
  }

  const addCustomTag = () => {
    const value = newTagInput.value.trim()
    if (!value) return

    const clean = value.replace(/^#/, '')
    selectedSet.value.add(clean)
    newTagInput.value = ''
  }

  const goBack = () => router.push({ name: 'CaptionSelect' })

  const goNext = async () => {
    try {
      const finalTags = Array.from(selectedSet.value).map((name) => ({ name }))

      reviewStore.selectedHashtags = finalTags

      await api.createHashtags({
        hashtagGroupId: reviewStore.hashtagGroupId,
        names: finalTags.map((tag) => tag.name),
      })

      reviewStore.nextStep()
      router.push({ name: 'EditPage' })
    } catch (error) {
      console.error(error)
      alert('해시태그 저장에 실패했습니다.')
    }
  }

  return {
    aiTags,
    selectedSet,
    newTagInput,
    selectedCount,
    isSelected,
    toggleTag,
    addCustomTag,
    goBack,
    goNext,
  }
}
