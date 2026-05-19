<template>
  <div class="photo-uploader">
    <h5 class="upload-title mb-1">
      <i class="bi bi-image me-1 text-secondary"></i> 사진 업로드
    </h5>
    <p class="upload-subtitle">
      {{ maxCount }}개까지 사진을 올릴 수 있습니다. ({{ images.length }}/{{ maxCount }})
    </p>

    <section class="upload-section">
      <div v-if="isReady" class="upload-box" @click="triggerFileInput">
        <i class="bi bi-cloud-arrow-up fs-2 text-secondary mb-2"></i>
        <p class="text-secondary mb-0">클릭해서 사진을 업로드하세요.</p>
        <small class="text-muted">사진 크기는 각 10MB까지 가능하며, JPG, PNG만 올려주세요.</small>
        <input 
          type="file" 
          multiple 
          accept="image/*" 
          ref="fileInput" 
          @change="handleFileUpload" 
          hidden 
        />
      </div>

      <div v-if="images.length" class="preview-grid mt-3">
        <div v-for="(img, idx) in images" :key="img.id || idx" class="preview-item">
          <img :src="img.url" :alt="img.name" :class="{ 'opacity-50': img.uploading }" />
          
          <div v-if="img.uploading" class="upload-spinner">
            <div class="spinner-border spinner-border-sm text-primary" role="status"></div>
          </div>
        </div>
      </div>
    </section>
  </div>
</template>

<script setup>
import { ref, toRef } from 'vue'
import { v4 as uuidv4 } from 'uuid'
import api from '@/api/travelgramApi'

// Props 정의
const props = defineProps({
  modelValue: { type: Array, default: () => [] }, // 부모와 v-model로 연결될 이미지 배열
  isReady: { type: Boolean, default: false },     // 업로드 준비 상태
  photoGroupId: { type: [String, Number], required: false }, // 업로드할 그룹 ID
  maxCount: { type: Number, default: 10 }         // 최대 업로드 개수
})



// Emits 정의
const emit = defineEmits(['update:modelValue', 'upload-started'])

// Props 반응형 연결
const images = toRef(props, 'modelValue')
const fileInput = ref(null)

const triggerFileInput = () => fileInput.value?.click()

// 📤 파일 업로드 로직 (기존 로직 이동)
const handleFileUpload = async (event) => {
  const files = Array.from(event.target.files)

  if (images.value.length + files.length > props.maxCount) {
    alert(`최대 ${props.maxCount}장까지만 업로드할 수 있습니다.`)
    return
  }

  const baseOrderIndex = images.value.length

  // 1. 미리보기 생성
  const newPreviewImages = files.map((file, index) => ({
    id: uuidv4(),
    name: file.name,
    url: URL.createObjectURL(file),
    file: file,
    uploading: true,
    orderIndex: baseOrderIndex + index,
  }))

  // 부모에게 업데이트된 목록 전달 (v-model)
  const updatedList = [...images.value, ...newPreviewImages]
  emit('update:modelValue', updatedList)

  // 2. 서버 업로드
  try {
    const uploadedList = await uploadPhotos(files, props.photoGroupId, baseOrderIndex)
    const finalUploadedList = uploadedList.data || []

    // 3. URL 교체 및 상태 업데이트
    const newList = updatedList.map(img => {
      const serverImg = finalUploadedList.find(u => u.orderIndex === img.orderIndex)
      if (serverImg) {
        URL.revokeObjectURL(img.url) // 메모리 해제
        return {
          ...img,
          url: serverImg.fileUrl,
          id: serverImg.id,
          uploading: false,
          file: null
        }
      }
      return img
    })

    // 최종 목록 업데이트 및 폴링 시작 요청
    emit('update:modelValue', newList)
    emit('upload-started') // 🚀 부모에게 "업로드 끝났으니 AI 분석 확인해" 라고 신호 보냄

  } catch (error) {
    console.error('File upload failed:', error)
    alert('사진 업로드에 실패했습니다.')
    // 실패 시 롤백
    emit('update:modelValue', images.value.filter(img => img.orderIndex < baseOrderIndex))
  } finally {
    if (fileInput.value) fileInput.value.value = ''
  }
}

// API 호출 함수
const uploadPhotos = async (files, photoGroupId, startOrderIndex) => {
  const formData = new FormData()
  formData.append("photoGroupId", photoGroupId)
  formData.append("startOrderIndex", startOrderIndex)
  
  const fileArray = Array.isArray(files) ? files : [files]
  fileArray.forEach((file) => formData.append("files", file))

  return api.uploadReviewPhotos(formData)
}
</script>

<style scoped>
/* 기존 스타일 그대로 가져오기 */
.upload-section {
  background-color: #f9fafc;
  border-radius: 1rem;
  padding: 1.5rem;
  border: 1px solid #eee;
}
.upload-title {
  font-weight: 600;
  color: #1b3b6f;
}
.upload-subtitle {
  color: #6c757d;
  margin-bottom: 1rem;
}
.upload-box {
  border: 2px dashed #d0d5dd;
  border-radius: 0.75rem;
  padding: 2rem;
  text-align: center;
  background-color: #fff;
  cursor: pointer;
  transition: background-color 0.3s ease;
}
.upload-box:hover {
  background-color: #fef8f2;
}
.preview-grid {
  display: flex;
  flex-wrap: wrap;
  gap: 0.75rem;
}
.preview-item {
  position: relative;
  width: 80px;
  height: 80px;
  border-radius: 0.75rem;
  overflow: hidden;
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.1);
}
.upload-spinner {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  z-index: 10;
}
.opacity-50 {
  opacity: 0.5;
}
.preview-item img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}
</style>