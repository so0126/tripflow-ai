<template>
        <!-- Header -->
    <div class="p-4 pb-3 border-bottom d-flex align-items-center justify-content-between">
      <div class="d-flex gap-3 align-items-center">
        <button class="btn btn-link p-0 back-button" @click="$router.back()" title="뒤로 가기">
          <i class="bi bi-arrow-left-short fs-1"></i>
        </button>
        
        <div class="rounded-3 bg-secondary-subtle d-flex align-items-center justify-content-center"
          style="width: 46px; height: 46px">
          👤
        </div>

        <div>
          <h5 class="mb-1 title">마이페이지</h5>
          <p class="text-muted small mb-0 sub">
            당신의 정보를 기록해보세요.
          </p>
        </div>
      </div>

    </div>
    <div class="travelgram-page">
      <div class="page-inner">


      
      <div class="text-center mb-5">
        <div class="position-relative d-inline-block mb-3">
        <img v-if="profileData.profileImage" :src="profileData.profileImage" alt="Profile"
        class="rounded-circle shadow-sm border border-4 border-white"
          style="width: 140px; height: 140px; object-fit: cover;" />
        <div v-else
          class="rounded-circle shadow-sm border border-4 border-white bg-light d-flex align-items-center justify-content-center"
          style="width: 140px; height: 140px;">
          <i class="bi bi-person text-secondary" style="font-size: 4rem;"></i>
        </div>

        <button @click="triggerImageInput"
          class="btn btn-primary rounded-circle position-absolute bottom-0 end-0 shadow-sm border border-2 border-white"
          style="width: 42px; height: 42px; display: flex; align-items: center; justify-content: center;">
          <i class="bi bi-camera-fill text-white"></i>
        </button>
      </div>

      <div v-if="profileData.profileImage">
        <button class="btn btn-link text-danger text-decoration-none fw-bold" @click="removeProfileImage">
          <i class="bi bi-trash me-1"></i> 이미지 삭제
        </button>
      </div>

      <input ref="profileImageInput" type="file" accept="image/*" @change="handleImageUpload" hidden />
    </div>

    <BaseSection icon="bi-person-gear" title="Basic Info" subtitle="기본 정보 수정">
      <div class="row g-4">
        <div class="col-md-6">
          <label class="custom-label">Name</label>
          <input type="text" class="form-control custom-input" v-model="profileData.name" placeholder="이름을 입력하세요" />
        </div>
        <div class="col-md-6">
          <label class="custom-label">Korean Name</label>
          <input type="text" class="form-control custom-input" v-model="profileData.koreanName" placeholder="한글 이름" />
        </div>
        <div class="col-12">
          <label class="custom-label">Email</label>
          <input type="email" class="form-control custom-input bg-light" v-model="profileData.email" disabled />
          <p class="text-muted ms-2 mt-1 fs-6">
            <i class="bi bi-info-circle me-1"></i> 이메일은 변경할 수 없습니다.
          </p>
        </div>
      </div>
    </BaseSection>
    
    <BaseSection icon="bi-airplane" title="Travel Style" subtitle="나의 여행 스타일 설정">
      <div class="row g-4 mb-4">
        <div class="col-md-6">
          <label class="custom-label">Nationality</label>
          <input type="text" class="form-control custom-input" v-model="profileData.nationality" placeholder="국적" />
        </div>
        <div class="col-md-6">
          <label class="custom-label">Preferred Currency</label>
          <select class="form-select custom-input" v-model="profileData.preferredCurrency">
            <option value="USD">USD ($)</option>
            <option value="KRW">KRW (₩)</option>
            <option value="EUR">EUR (€)</option>
            <option value="JPY">JPY (¥)</option>
          </select>
        </div>
      </div>
      
      <label class="custom-label mb-3">Interests</label>
      <div class="d-flex flex-wrap gap-2">
        <div v-for="interest in availableInterests" :key="interest.id" class="interest-chip"
        :class="{ 'active': profileData.interests.includes(interest.id) }" @click="toggleInterest(interest.id)">
          <i :class="interest.icon"></i>
          <span class="ms-2">{{ interest.name }}</span>
        </div>
      </div>
    </BaseSection>

    <BaseSection icon="bi-heart-pulse" title="Medical Info" subtitle="건강 및 특이사항">
      <div class="row g-4">
        <div class="col-12">
          <label class="custom-label">Allergies</label>
          <textarea class="form-control custom-input" rows="2" v-model="profileData.medicalInfo.allergies"
            placeholder="알레르기 정보 (예: 땅콩, 갑각류)"></textarea>
        </div>
        <div class="col-12">
          <label class="custom-label">Dietary Restrictions</label>
          <textarea class="form-control custom-input" rows="2" v-model="profileData.medicalInfo.dietaryRestrictions"
          placeholder="식단 제한 (예: 채식, 글루텐 프리)"></textarea>
        </div>
      </div>
    </BaseSection>
    
    <BaseSection icon="bi-share" title="Social Connect" subtitle="SNS 계정 연동">
      <div class="d-flex justify-content-between align-items-center p-3 border rounded-4 bg-white shadow-sm">
        <div class="d-flex align-items-center">
          <i class="bi bi-instagram fs-2 me-3" style="color: #E1306C;"></i>
          <div>
            <h5 class="m-0">Instagram</h5>
            <small class="text-muted">여행 사진 공유하기</small>
          </div>
        </div>
        <div class="form-check form-switch">
          <input class="form-check-input" type="checkbox" role="switch" v-model="profileData.instagramConnected"
            style="width: 3rem; height: 1.5rem;">
        </div>
      </div>
    </BaseSection>
    <NavigationButtons
     back-text="취소"
     :is-next-disabled="isLoading"
     @back="goBack"
     @next="saveProfile"
   >
     <template #next-content>
       <i class="bi bi-check-lg me-2" v-if="!isLoading"></i>
       <span v-if="isLoading" class="spinner-border spinner-border-sm me-2"></span>
       {{ isLoading ? '저장 중...' : '저장하기' }}
     </template>
   </NavigationButtons>
    
  </div>


    </div>
    
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import BaseSection from '@/components/common/BaseSection.vue'
import PageHeader from "@/components/common/header/PageHeader.vue";
// import BackButtonPageHeader from '@/components/common/BackButtonPageHeader.vue'
import NavigationButtons from '@/components/common/button/NavigationButtons.vue';


const router = useRouter()
const isLoading = ref(false)
const profileImageInput = ref(null)

const triggerImageInput = () => profileImageInput.value?.click()

// 데이터 로직은 기존과 동일
const profileData = reactive({
  name: '',
  koreanName: '',
  email: '',
  profileImage: '',
  nationality: '',
  preferredCurrency: 'USD',
  interests: [],
  medicalInfo: { allergies: '', dietaryRestrictions: '' },
  instagramConnected: false
})

const availableInterests = [
  { id: 'culture', name: 'Culture', icon: 'bi bi-building' },
  { id: 'food', name: 'Food', icon: 'bi bi-fork-knife' },
  { id: 'shopping', name: 'Shopping', icon: 'bi bi-bag' },
  { id: 'nature', name: 'Nature', icon: 'bi bi-tree' },
  { id: 'nightlife', name: 'Nightlife', icon: 'bi bi-moon-stars' },
  { id: 'art', name: 'Art & Museums', icon: 'bi bi-palette' },
  { id: 'adventure', name: 'Adventure', icon: 'bi bi-mountain' },
  { id: 'relaxation', name: 'Relaxation', icon: 'bi bi-flower1' }
]

const handleImageUpload = (event) => {
  const file = event.target.files[0]
  if (file) {
    if (file.size > 5 * 1024 * 1024) {
      alert('이미지는 5MB 이하여야 합니다.')
      return
    }
    const reader = new FileReader()
    reader.onload = (e) => { profileData.profileImage = e.target.result }
    reader.readAsDataURL(file)
  }
}

const removeProfileImage = () => { profileData.profileImage = '' }

const toggleInterest = (interestId) => {
  const index = profileData.interests.indexOf(interestId)
  if (index > -1) profileData.interests.splice(index, 1)
  else profileData.interests.push(interestId)
}

const saveProfile = async () => {
  isLoading.value = true
  try {
    await new Promise(resolve => setTimeout(resolve, 1000)) // API Simulate
    localStorage.setItem('userProfile', JSON.stringify(profileData))
    alert('프로필이 저장되었습니다!')
    goBack()
  } catch (error) {
    alert('저장에 실패했습니다.')
  } finally {
    isLoading.value = false
  }
}

const goBack = () => router.push({name: 'MyProfile'});


onMounted(() => {
  try {
    const savedProfile = localStorage.getItem('userProfile')
    if (savedProfile) Object.assign(profileData, JSON.parse(savedProfile))
    // 기본값 폴백 (예시)
    if (!profileData.name) profileData.name = 'John Doe'
    if (!profileData.email) profileData.email = 'john.doe@gmail.com'
  } catch (e) { console.error(e) }
})
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

/* 🖋️ 입력 폼 스타일 (Journal Style) */
.custom-label {
  font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, "Helvetica Neue", Arial, "Noto Sans KR", sans-serif;
  /* 표준 폰트 */
  font-size: 1.1rem;
  color: #ff8c00;
  /* Primary Color */
  margin-bottom: 0.5rem;
  display: block;
}

.custom-input {
  font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, "Helvetica Neue", Arial, "Noto Sans KR", sans-serif;
  /* 표준 폰트 */
  font-size: 1.25rem;
  /* 글씨 시원하게 키움 */
  border-radius: 1rem;
  /* 둥글게 */
  border: 1px solid #dee2e6;
  padding: 0.7rem 1.2rem;
  background-color: #fff;
  transition: all 0.2s ease;
}

.custom-input:focus {
  border-color: #ff8c00;
  box-shadow: 0 0 0 4px rgba(255, 140, 0, 0.1);
  background-color: #fffaf0;
  /* 포커스 시 아주 연한 오렌지 배경 */
}

.custom-input:disabled {
  background-color: #f8f9fa;
  color: #adb5bd;
}

/* 🏷️ 관심사 칩 스타일 (Pill Shape) */
.interest-chip {
  padding: 0.6rem 1.2rem;
  border-radius: 50px;
  border: 1px solid #dee2e6;
  color: #6c757d;
  font-size: 1rem;
  cursor: pointer;
  transition: all 0.2s;
  display: flex;
  align-items: center;
}

.interest-chip:hover {
  background-color: #f8f9fa;
  transform: translateY(-2px);
}

.interest-chip.active {
  background-color: #ff8c00;
  border-color: #ff8c00;
  color: white;
  font-weight: normal;
  /* 손글씨체는 normal이 예쁨 */
}
</style>