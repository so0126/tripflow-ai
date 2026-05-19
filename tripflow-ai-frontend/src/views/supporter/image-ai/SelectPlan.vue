<template>
  
        <!-- Header -->
    <div class="page-header">
      <div class="d-flex gap-3 align-items-center">
        <button class="btn btn-link p-0 back-button" @click="$router.back()">
          <i class="bi bi-arrow-left-short fs-1"></i>
        </button>

        <div class="icon-box">📷</div>

        <div>
          <h5 class="mb-1 title">이미지 기반 여행 AI</h5>
          <p class="sub">당신의 사진으로 여행 장소를 찾아보아요!</p>
        </div>
      </div>
    </div>

<div class="page-body">

  <BaseSection icon="bi-list-check" title="AI 추천을 어떻게 사용하고 싶으신가요?" subtitle="완료율">
    

    <div class="selected-place card p-3 mb-3 d-flex align-items-center">
  <img v-if="item?.imageUrl" :src="item.imageUrl" class="thumb me-4" />

  <div class="place-text text-center flex-fill">
    <div class="fw-medium fs-5">{{ item?.placeName || 'Unknown Place' }}</div>
    <div class="small text-muted mt-1">{{ item?.location || '' }}</div>
    <div class="small text-muted mt-2">{{ item?.description || '' }}</div>
  </div>
</div>

    <ul class="list-unstyled">
      <li class="option p-3 mb-2 rounded d-flex align-items-center" :class="{ selected: selectedOption === 'add' }"
        @click="selectOption('add')" @keyup.enter.space.prevent="selectOption('add')" role="button" tabindex="0"
        :aria-pressed="selectedOption === 'add'">
        <div class="icon add me-3">＋</div>
        <div class="flex-fill">
          <div class="fw-medium">여정에 추가하기</div>
          <div class="small text-muted">당신의 여정에 이 장소를 추가해보세요.</div>
        </div>

        <div v-if="selectedOption === 'add'" class="select-check" aria-hidden="true">✓</div>
      </li>

      <li class="option p-3 mb-2 rounded d-flex align-items-center" :class="{ selected: selectedOption === 'replace' }"
        @click="selectOption('replace')" @keyup.enter.space.prevent="selectOption('replace')" role="button" tabindex="0"
        :aria-pressed="selectedOption === 'replace'">
        <div class="icon replace me-3">↺</div>
        <div class="flex-fill">
          <div class="fw-medium">장소 대체</div>
          <div class="small text-muted">장소를 대체해보세요.</div>
        </div>

        <div v-if="selectedOption === 'replace'" class="select-check" aria-hidden="true">✓</div>
      </li>
      
      <li class="option p-3 mb-2 rounded d-flex align-items-center" :class="{ selected: selectedOption === 'save' }"
        @click="selectOption('save')" @keyup.enter.space.prevent="selectOption('save')" role="button" tabindex="0"
        :aria-pressed="selectedOption === 'save'">
        <div class="icon save me-3">💾</div>
        <div class="flex-fill">
          <div class="fw-medium">저장하기</div>
          <div class="small text-muted">일정 추가 없이 저장하고 히스토리 내역에서 확인할 수 있어요.</div>
        </div>
        
        <div v-if="selectedOption === 'save'" class="select-check" aria-hidden="true">✓</div>
      </li>

      <!-- Not Interested: now selectable; Confirm required to proceed -->
      <li class="option p-3 mb-2 rounded d-flex align-items-center" :class="{ selected: selectedOption === 'not_interested' }"
        @click="selectOption('not_interested')" @keyup.enter.space.prevent="selectOption('not_interested')" role="button" tabindex="0"
        :aria-pressed="selectedOption === 'not_interested'">
        <div class="icon no me-3">✕</div>
        <div class="flex-fill">
          <div class="fw-medium">관심 없음</div>
          <div class="small text-muted">아무것도 하지 않고 검색으로 돌아갑니다.</div>
        </div>

        <div v-if="selectedOption === 'not_interested'" class="select-check" aria-hidden="true">✓</div>
      </li>
    </ul>
  </BaseSection>


      <div class="d-flex gap-3 mt-5">
       <NavigationButtons
        back-text="뒤로가기"
        next-text="확인"
        :is-next-disabled="!selectedOption || isSaving"
        @back="onStepBack"
        @next="confirm"
      >
    </NavigationButtons>
    </div>
    
  </div>

  </template>

<script setup>
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useImageSearchStore } from '@/store/imageSearchStore'
import imageSearchApi from '@/api/imageSearchApi'
import PageHeader from '@/components/common/header/PageHeader.vue'
import StepHeader from '@/components/common/header/StepHeader.vue'
import BaseSection from '@/components/common/BaseSection.vue'
import { useAuthStore } from '@/store/authStore'
import NavigationButtons from '@/components/common/button/NavigationButtons.vue';
import { useChatStore } from '@/store/chatStore'

const router = useRouter()
const imageSearchStore = useImageSearchStore()
const authStore = useAuthStore()
const chatStore = useChatStore()

const onStepBack = () => {
  router.push({ name: 'AiRecommend' }).catch(() => { })
}

// 스토어에서 선택된 장소 가져오기 (computed로 변경하여 반응형으로 만들기)
const item = computed(() => {
  const selected = imageSearchStore.getSelectedPlace
  return selected || {
    id: 0,
    placeName: 'Unknown Place',
    description: '',
    location: '',
    imageUrl: ''
  }
})

// UI selection state
const selectedOption = ref(null)
const isSaving = ref(false)

const selectOption = (k) => {
  selectedOption.value = selectedOption.value === k ? null : k
}

// DB에 저장 (save, add, replace 선택 시) - 모든 후보지 저장
const saveToDatabase = async (action) => {
  try {
    isSaving.value = true
    
    const userId = authStore.userId;
    if (!userId) {
      throw new Error('유저가 로그인하지 않았습니다.')
    }
    
    // 모든 후보지 저장
    const allCandidates = imageSearchStore.getCandidates
    const selectedPlace = imageSearchStore.getSelectedPlace
    
    const dataToSave = {
      selectedPlace: selectedPlace,
      candidates: allCandidates, // 모든 후보지 3개
      uploadedImage: imageSearchStore.getUploadedImage,
      selectedType: imageSearchStore.getSelectedType,
      action: action // save, add, replace
    }
    
    console.log('DB에 저장할 데이터 (모든 후보지):', dataToSave)
    
    // imageSearchApi.savePlaceCandidates() 호출 - 모든 후보지 저장
    const response = await imageSearchApi.savePlaceCandidates(userId, dataToSave)
    
    console.log('저장 성공 - sessionId:', response)
    return true
    
  } catch (error) {
    console.error('DB 저장 오류:', error)
    alert('저장에 실패했습니다.')
    return false
  } finally {
    isSaving.value = false
  }
}

// 한글 종성(받침) 확인 함수
const hasJongseong = (str) => {
  if (!str || str.length === 0) return false
  
  const lastChar = str[str.length - 1]
  const code = lastChar.charCodeAt(0)
  
  // 한글 유니코드 범위: 0xAC00 ~ 0xD7A3
  if (code < 0xAC00 || code > 0xD7A3) return false
  
  // 종성 계산: (code - 0xAC00) % 28
  // 0이면 받침 없음, 1~27이면 받침 있음
  return (code - 0xAC00) % 28 !== 0
}

// Confirm: 선택에 따라 처리
const confirm = async () => {
  if (!selectedOption.value) return

  // 로그인 체크 (save, add, replace는 로그인 필요)
  if (selectedOption.value !== 'not_interested' && !authStore.userId) {
    alert('로그인이 필요한 서비스입니다.\n로그인 후 이용해주세요.')
    // 백엔드 OAuth 로그인으로 리다이렉트
    window.location.href = 'http://localhost:8080/oauth2/authorization/google'
    return
  }

  // Not Interested -> 저장하지 않고 돌아가기
  if (selectedOption.value === 'not_interested') {
    router.push({ name:'CreateNewSearch' }).catch(() => { })
    return
  }

  // save, add, replace -> DB에 저장
  const saved = await saveToDatabase(selectedOption.value)
  
  if (!saved) {
    return // 저장 실패 시 진행하지 않음
  }

  // save only -> 히스토리 페이지로 이동
  if (selectedOption.value === 'save') {
    router.push({ name: 'History' }).catch(() => { })
    return
  }

  // add 또는 replace -> chatStore에 메시지 설정 후 PlanList로 이동
  if (selectedOption.value === 'add' || selectedOption.value === 'replace') {
    const placeName = item.value?.placeName || '장소'
    
    let chatMessage = ''
    
    if (selectedOption.value === 'add') {
      // "장소명을/를 일정에 추가해줘"
      const josa = hasJongseong(placeName) ? '을' : '를'
      chatMessage = `${placeName}${josa} 일정에 추가해줘`
    } else if (selectedOption.value === 'replace') {
      // "일정에 있는 장소 한 곳을 장소명으로/로 변경하고 싶어"
      const josa = hasJongseong(placeName) ? '으로' : '로'
      chatMessage = `일정에 있는 장소 한 곳을 ${placeName}${josa} 변경하고 싶어`
    }
    
    console.log('📤 [SelectPlan] chatStore에 메시지 설정:', chatMessage)
    
    // PlanList로 먼저 이동
    router.push({ name: 'planedit' }).then(() => {
      // 페이지 이동 완료 후 메시지 설정 (ChatSidebar가 마운트된 후)
      setTimeout(() => {
        console.log('📤 [SelectPlan] 페이지 이동 완료, 메시지 전송:', chatMessage)
        chatStore.sendMessage(chatMessage)
      }, 300) // ChatSidebar 마운트 대기
    }).catch(() => {
      console.error('❌ planedit 라우트 이동 실패')
    })
  }
}
</script>

<style scoped>
.place-text {
  text-align: center;
}
/* Header */
.page-header {
  padding: 1.75rem 2rem 1.25rem;
  border-bottom: 1px solid #e2e8f0;
  
}
.page-body {
  flex: 1;
  padding: 2.5rem 2rem 3rem; /* 🔥 헤더랑 떨어지는 핵심 */
}
.icon-box {
  width: 42px;
  height: 42px;
  border-radius: 10px;
  background: #f1f5f9;
  display: flex;
  align-items: center;
  justify-content: center;
}

.title {
  font-weight: 700;
}

.sub {
  font-size: 0.85rem;
  color: #64748b;
}

.history-card {
  background: #FFD9A6;
  border-radius: 12px;
}

.selected-place {
  border-radius: 10px;
  background: #fff;
  border: 1px solid #f3e8ff;
}

.thumb {
  width: 320px;
  height: 320px;
  object-fit: cover;
  border-radius: 10px;
}

.option {
  background: #fff;
  border: 1px solid #f3e8ff;
  cursor: pointer;
  transition: box-shadow .12s, transform .08s;
  position: relative;
}

.option.selected {
  border-color: #1b3b6f;
  background: #f3f7ff;
  transform: translateY(-2px);
  box-shadow: 0 8px 20px rgba(27, 59, 111, 0.06);
}

.option .icon {
  width: 48px;
  height: 48px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f7f7ff;
  color: #6b46ff;
  font-weight: 700;
  font-size: 18px;
}

.option:hover {
  box-shadow: 0 8px 20px rgba(167, 139, 255, 0.06);
  transform: translateY(-2px);
}

/* select check (right side) - reuse Recommend style */
.select-check {
  position: absolute;
  right: 16px;
  top: 50%;
  transform: translateY(-50%);
  background: #1b3b6f;
  color: #fff;
  width: 36px;
  height: 36px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 700;
  box-shadow: 0 6px 18px rgba(27, 59, 111, 0.12);
  border: 2px solid rgba(255, 255, 255, 0.6);
}

/* small tweak for Not Interested icon color */
.option .icon.no {
  background: #fff5f2;
  color: #d03b1f;
}

button:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.card {
  border-radius: 12px;
}
</style>