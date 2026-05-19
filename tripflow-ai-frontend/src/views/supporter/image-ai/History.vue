<template>




  <section class="history-root card rounded-0 h-100 d-flex flex-column">
<!-- Header -->
    <div class="page-header">
      <div class="d-flex gap-3 align-items-center">
        <button class="btn btn-link p-0 back-button" @click="$router.back()">
          <i class="bi bi-arrow-left-short fs-1"></i>
        </button>

        <div class="icon-box">🕒</div>

        <div>
          <h5 class="mb-1 title">이미지 기반 여행 AI</h5>
          <p class="sub">당신의 사진으로 여행 장소를 찾아보아요!</p>
        </div>
      </div>
    </div>
  <!-- Body Content -->
  <div class="history-body-scroll flex-grow-1 overflow-y-auto p-4">
    <!-- Section Header -->
    <div class="section-header d-flex align-items-center justify-content-between mb-4">
      <div class="d-flex align-items-center gap-3">
        <div class="icon-badge">
          <i class="bi bi-clock-history"></i>
        </div>
        <h6 class="section-title mb-0">AI가 추천한 히스토리</h6>
      </div>
      <router-link class="btn btn-sm btn-primary" :to="{ name: 'CreateNewSearch' }">
        + 새로운 검색
      </router-link>
    </div>

    <div v-if="isLoading" class="text-center py-5">
      <div class="spinner-border text-primary" role="status">
        <span class="visually-hidden">로딩 중...</span>
      </div>
    </div>
    
    <div v-else class="history-list">
      <div v-for="(h, i) in history" :key="i" class="history-item card p-3 mb-3">
        <div class="d-flex">
          <img v-if="h.thumb" :src="h.thumb" class="thumb me-3"  v-img-fallback="fallbacks" />
          <div v-else class="thumb me-3 bg-secondary d-flex align-items-center justify-content-center text-white">
            <i class="bi bi-image"></i>
          </div>
          <div class="flex-fill">
            <div class="d-flex justify-content-between">
              <div>
                <div class="small text-muted">{{ h.date }}</div>
                <div class="history-item-title fw-medium mt-1">{{ h.title }}</div>
                <div class="small text-muted mt-1">{{ h.note }}</div>
              </div>
              <div>
                <span v-if="h.status" class="badge status-badge">{{ h.status }}</span>
              </div>
            </div>

            <div class="mt-3">
              <div class="small text-muted mb-1">AI 추천 ({{ h.recommendations.length }})</div>
              <div class="d-flex gap-2">
                <template v-for="(r, idx) in h.recommendations" :key="idx">
                  <img v-if="r.thumb" :src="r.thumb" class="rec-thumb" :title="r.name"  v-img-fallback="fallbacks" />
                  <div v-else class="rec-thumb bg-secondary d-flex align-items-center justify-content-center text-white" :title="r.name">
                    <i class="bi bi-image"></i>
                  </div>
                </template>
              </div>
            </div>
          </div>
        </div>

        <!-- Hover Overlay -->
        <div class="hover-overlay">
          <div class="overlay-actions">
            <button class="action-btn detail-btn" @click="openModal(h)" title="세부사항을 확인해보세요.">
              <i class="bi bi-eye"></i> 세부 사항
            </button>
            <!-- Change Status 버튼: Saved only 상태일 때만 표시 -->
            <button v-if="h.status === 'Saved only'" class="action-btn change-btn" @click="openChangeStatusModal(h)" title="상태 변경">
              <i class="bi bi-arrow-repeat"></i> 상태 변경
            </button>
          </div>
        </div>
      </div>

      <div v-if="history.length === 0" class="text-center text-muted py-5">
        AI 히스토리가 존재하지 않습니다. "새로운 검색" 버튼을 클릭하거나 서포터 홈에서 사진 업로드를 해보세요.
      </div>
    </div>
  </div>

  <!-- ActivityDetailsModal -->
  <ActivityDetailsModal 
    v-if="selectedHistory" 
    :open="true"
    :data="selectedHistory"
    @close="selectedHistory = null"
  />

  <!-- Change Status Modal -->
  <teleport to="body">
    <div v-if="changeStatusItem" class="modal-backdrop" @click="changeStatusItem = null">
      <div class="modal-card" @click.stop>
        <!-- Header -->
        <div class="d-flex justify-content-between align-items-center mb-3">
          <h5 class="mb-0">상태 변경</h5>
          <button
            class="btn btn-sm btn-light rounded-circle"
            @click="changeStatusItem = null"
          >
            ✕
          </button>
        </div>

        <!-- Item Info -->
        <div class="selected-place card p-3 mb-3 d-flex align-items-center">
          <img v-if="changeStatusItem.thumb" :src="changeStatusItem.thumb" class="thumb me-3"  v-img-fallback="fallbacks" />
          <div v-else class="thumb me-3 bg-secondary d-flex align-items-center justify-content-center text-white">
            <i class="bi bi-image"></i>
          </div>
          <div>
            <div class="fw-medium">{{ changeStatusItem.title }}</div>
            <div class="small text-muted">{{ changeStatusItem.note }}</div>
          </div>
        </div>

        <!-- Options -->
        <ul class="list-unstyled">
          <li class="option p-3 mb-2 rounded d-flex align-items-center" 
            :class="{ selected: changeStatusSelection === 'add' }"
            @click="changeStatusSelection = 'add'" role="button" tabindex="0">
            <div class="icon add me-3">＋</div>
            <div class="flex-fill">
              <div class="fw-medium">장소 추가</div>
              <div class="small text-muted">당신의 여행 일정에 이 장소를 추가해보세요.</div>
            </div>
            <div v-if="changeStatusSelection === 'add'" class="select-check" aria-hidden="true">✓</div>
          </li>

          <li class="option p-3 mb-2 rounded d-flex align-items-center" 
            :class="{ selected: changeStatusSelection === 'replace' }"
            @click="changeStatusSelection = 'replace'" role="button" tabindex="0">
            <div class="icon replace me-3">↺</div>
            <div class="flex-fill">
              <div class="fw-medium">장소 대체</div>
              <div class="small text-muted">일정에 존재하는 장소를 대체해보세요.</div>
            </div>
            <div v-if="changeStatusSelection === 'replace'" class="select-check" aria-hidden="true">✓</div>
          </li>
        </ul>

        <!-- Buttons -->
    <div class="d-flex gap-3 mt-5">

      <NavigationButtons
      back-text="취소"
      next-text="확인"
      :is-next-disabled="!changeStatusSelection"
      @back="changeStatusItem = null"
      @next="confirmChangeStatus"
      />
  </div>
  </div>
    </div>
  </teleport>
  </section>

</template>

<script setup>
import { ref, onMounted } from 'vue'
import ActivityDetailsModal from '@/components/planner/ActivityDetailsModal.vue'
import imageSearchApi from '@/api/imageSearchApi'
import { useAuthStore } from '@/store/authStore'
import NavigationButtons from '@/components/common/button/NavigationButtons.vue';
import { useRouter } from 'vue-router';
import { useChatStore } from '@/store/chatStore';

const fallbacks = [
  "/images/01.png",
  "/images/02.png",
  "/images/03.png",
  "/images/04.png",
  "/images/05.png",
  "/images/06.png",
];

const router = useRouter()
const authStore = useAuthStore()
const chatStore = useChatStore()

// 모달 상태
const selectedHistory = ref(null)
const changeStatusItem = ref(null)
const changeStatusSelection = ref(null)

// 히스토리 데이터
const history = ref([])
const isLoading = ref(false)

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

// ActionType 한글 변환
const getStatusText = (actionType) => {
  const statusMap = {
    'SAVE_ONLY': 'Saved only',
    'ADD_PLAN': 'Added',
    'REPLACED_PLAN': 'Replaced'
  }
  return statusMap[actionType] || actionType
}

// 히스토리 데이터 로드
const loadHistory = async () => {
  try {
    isLoading.value = true
    
    const userId = authStore.userId;
    console.log('📋 히스토리 로드 - userId:', userId)
    
    // API 호출
    const response = await imageSearchApi.getSessionsByUserId(userId)
    console.log('📋 받은 세션 데이터:', response)
    console.log('📋 데이터 타입:', typeof response, Array.isArray(response))
    
    // 응답이 래핑되어 있는 경우 data 추출
    const sessions = response.data ? response.data : (Array.isArray(response) ? response : [])
    
    console.log('📋 추출된 세션 배열:', sessions, '개수:', sessions.length)
    
    if (sessions.length === 0) {
      console.log('📋 세션 데이터가 없습니다')
      history.value = []
      return
    }
    
    // 데이터 변환
    history.value = sessions.map(session => {
      // 선택된 후보지 찾기 (isSelected === true)
      const selectedCandidate = session.candidates.find(c => c.isSelected)
      const selectedPlace = selectedCandidate?.place
      
      // 이미지 URL 우선순위: internalThumbnailUrl > internalOriginalUrl > externalImageUrl
      const getImageUrl = (place) => {
        return place?.internalThumbnailUrl || place?.internalOriginalUrl || place?.externalImageUrl || ''
      }
      
      // 선택되지 않은 후보지들 (나머지 추천 목록)
      const otherCandidates = session.candidates.filter(c => !c.isSelected)
      
      return {
        sessionId: session.sessionId,
        date: new Date(session.createdAt).toLocaleDateString('ko-KR', {
          year: 'numeric',
          month: '2-digit',
          day: '2-digit'
        }).replace(/\. /g, '.').replace('.', ''),
        title: selectedPlace?.name || 'Unknown Place',
        note: selectedPlace?.address || '',
        status: getStatusText(session.actionType),
        thumb: getImageUrl(selectedPlace),
        area: selectedPlace?.address || '',
        address: selectedPlace?.address || '',
        hours: '',
        cost: 0,
        desc: selectedPlace?.description || '',
        imageQuery: selectedPlace?.name || '',
        candidates: session.candidates,
        selectedCandidate: selectedCandidate,
        // ActivityDetailsModal용 gallery 배열 (internalOriginalUrl 우선)
        gallery: [
          selectedPlace?.internalOriginalUrl || selectedPlace?.internalThumbnailUrl || selectedPlace?.externalImageUrl || ''
        ].filter(url => url),
        recommendations: otherCandidates.map(c => ({
          candidateId: c.candidateId,
          thumb: getImageUrl(c.place),
          name: c.place?.name || '',
          isSelected: c.isSelected
        }))
      }
    })
    
    console.log('✅ 변환된 히스토리:', history.value)
    
    // 각 히스토리의 recommendations 확인
    history.value.forEach((h, idx) => {
      console.log(`세션 ${idx + 1} - 대표: ${h.title}, 추천 개수: ${h.recommendations.length}`)
      h.recommendations.forEach((r, ridx) => {
        console.log(`  추천 ${ridx + 1}: ${r.name}, 이미지: ${r.thumb ? '있음' : '없음'}`)
      })
    })
    
  } catch (error) {
    console.error('❌ 히스토리 로드 실패:', error)
  } finally {
    isLoading.value = false
  }
}

// 컴포넌트 마운트 시 데이터 로드
onMounted(() => {
  loadHistory()
})

// 상세 보기 모달 열기
const openModal = (item) => {
  selectedHistory.value = item
}

// Change Status 모달 열기
const openChangeStatusModal = (item) => {
  changeStatusItem.value = item
  changeStatusSelection.value = null
}

// Change Status 확인
const confirmChangeStatus = async () => {
  if (!changeStatusSelection.value || !changeStatusItem.value) return

  const item = changeStatusItem.value
  const mode = changeStatusSelection.value

  try {
    // 선택된 후보지의 candidateId 가져오기 (대표 장소)
    if (!item.selectedCandidate) {
      console.error('선택된 후보지가 없습니다')
      alert('선택된 장소를 찾을 수 없습니다.')
      return
    }
    
    const candidateId = item.selectedCandidate.candidateId
    
    // ActionType 업데이트
    console.log('🔄 ActionType 업데이트:', candidateId, mode)
    await imageSearchApi.updateActionType(candidateId, mode)
    
    console.log('✅ ActionType 업데이트 성공')
    
    // 모달 닫기
    changeStatusItem.value = null
    changeStatusSelection.value = null
    
    // 장소명 가져오기
    const placeName = item.title || '장소'
    
    // 채팅 메시지 생성
    let chatMessage = ''
    
    if (mode === 'add') {
      const josa = hasJongseong(placeName) ? '을' : '를'
      chatMessage = `${placeName}${josa} 일정에 추가해줘`
    } else if (mode === 'replace') {
      const josa = hasJongseong(placeName) ? '으로' : '로'
      chatMessage = `일정에 있는 장소 한 곳을 ${placeName}${josa} 변경하고 싶어`
    }
    
    console.log('📤 [History] 채팅 메시지 생성:', chatMessage)
    
    // PlanList로 먼저 이동
    router.push({ name: 'planedit' }).then(() => {
      // 페이지 이동 완료 후 메시지 설정 (ChatSidebar가 마운트된 후)
      setTimeout(() => {
        console.log('📤 [History] 페이지 이동 완료, 메시지 전송:', chatMessage)
        chatStore.sendMessage(chatMessage)
      }, 300) // ChatSidebar 마운트 대기
    }).catch(() => {
      console.error('❌ planedit 라우트 이동 실패')
    })
    
  } catch (error) {
    console.error('❌ ActionType 업데이트 실패:', error)
    alert('상태 변경에 실패했습니다.')
  }
}
</script>

<style scoped>
/* Header */
.page-header {
  padding: 1.75rem 2rem 1.25rem;
  border-bottom: 1px solid #e2e8f0;
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


/* ========================================
   History Root - PlanList 스타일 매칭
   ======================================== */
.history-root {
  height: 100%;
  display: flex;
  flex-direction: column;
  font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, "Helvetica Neue", Arial, "Noto Sans KR", sans-serif;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
  background: #ffffff;
  overflow: hidden;
}

/* 헤더 영역 스타일 */
.history-root > div:first-child {
  background: #ffffff;
  color: #2d4a8f;
  border-bottom: 1px solid #e2e8f0 !important;
  height: 100px;
  display: flex;
  align-items: center;
  padding: 1.25rem 2rem !important;
  position: relative;
}

.history-root h5.title {
  font-size: 1.15rem;
  font-weight: 700;
  color: #1e293b !important;
  letter-spacing: -0.02em;
  margin-bottom: 0.25rem;
}

.history-root .sub {
  color: #64748b !important;
  font-size: 0.8rem;
  font-weight: 500;
}

.history-root .rounded-3 {
  background: #f8fafc !important;
  border: 1px solid #e2e8f0;
  font-size: 1.25rem;
  width: 38px !important;
  height: 38px !important;
}

/* 뒤로가기 버튼 */
.back-button {
  color: #2d4a8f;
  text-decoration: none;
  transition: all 0.3s ease;
  margin-left: -0.5rem;
}

.back-button:hover {
  color: #1a2a56;
  transform: translateX(-4px);
}

.back-button:focus {
  outline: none;
  box-shadow: none;
}

/* 본문 스크롤 */
.history-body-scroll {
  flex: 1;
  overflow-y: auto;
  scrollbar-width: thin;
  scrollbar-color: #cbd5e1 #ffffff;
  background: #ffffff;
}

.history-body-scroll::-webkit-scrollbar {
  width: 6px;
}

.history-body-scroll::-webkit-scrollbar-track {
  background: #ffffff;
  border-radius: 3px;
}

.history-body-scroll::-webkit-scrollbar-thumb {
  background: #cbd5e1;
  border-radius: 3px;
  transition: background 0.3s ease;
}

.history-body-scroll::-webkit-scrollbar-thumb:hover {
  background: #94a3b8;
}

/* Section Header 스타일 */
.section-header {
  padding: 0.5rem 0;
}

.icon-badge {
  width: 44px;
  height: 44px;
  border-radius: 12px;
  background: linear-gradient(135deg, #2d4a8f 0%, #1a2a56 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  font-size: 1.2rem;
  box-shadow: 0 4px 12px rgba(45, 74, 143, 0.2);
}

.section-title {
  font-size: 1.1rem;
  font-weight: 700;
  color: #1e293b;
  letter-spacing: -0.02em;
}

/* Primary 버튼 스타일 */
.btn-primary {
  background-color: #2d4a8f !important;
  border-color: #2d4a8f !important;
  color: white !important;
  padding: 0.65rem 1.5rem;
  font-size: 0.95rem;
  font-weight: 600;
  border-radius: 12px;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  letter-spacing: -0.01em;
  text-decoration: none;
}

.btn-primary:hover {
  background-color: #1a2a56 !important;
  border-color: #1a2a56 !important;
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(45, 74, 143, 0.3);
}

.btn-sm.btn-primary {
  padding: 0.5rem 1.2rem;
  font-size: 0.875rem;
  border-radius: 10px;
}

.supporter-page {
  background-color: #fffaf3;
  min-height: 100vh;
  padding: 2rem 1.25rem;
}

.history-card {
  background: #FFD9A6;
  border-radius: 12px;
}

.history-list {
  max-height: 600px;
  overflow-y: auto;
  padding-right: 8px;
  scrollbar-width: thin;
  scrollbar-color: #cbd5e1 transparent;
}

@media (min-height: 900px) {
  .history-list {
    max-height: 720px;
  }
}

@media (max-height: 768px) {
  .history-list {
    max-height: 480px;
  }
}

.history-list::-webkit-scrollbar {
  width: 6px;
}

.history-list::-webkit-scrollbar-track {
  background: transparent;
  border-radius: 3px;
}

.history-list::-webkit-scrollbar-thumb {
  background: #cbd5e1;
  border-radius: 3px;
  transition: background 0.3s ease;
}

.history-list::-webkit-scrollbar-thumb:hover {
  background: #94a3b8;
}

.history-item {
  border-radius: 14px;
  background: linear-gradient(135deg, #ffffff 0%, #f8fafc 100%);
  border: 2px solid #e2e8f0;
  position: relative;
  overflow: hidden;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  box-shadow: 0 2px 8px rgba(45, 74, 143, 0.05);
}

.history-item:hover {
  border-color: #2d4a8f;
  transform: translateY(-2px);
  box-shadow: 0 8px 24px rgba(45, 74, 143, 0.15);
}

.history-item-title {
  font-size: 0.95rem;
  font-weight: 600;
  color: #1e293b;
  line-height: 1.4;
}

.thumb {
  width: 72px;
  height: 72px;
  border-radius: 12px;
  object-fit: cover;
  border: 2px solid #e2e8f0;
  box-shadow: 0 2px 8px rgba(45, 74, 143, 0.1);
}

.rec-thumb {
  width: 64px;
  height: 64px;
  border-radius: 10px;
  object-fit: cover;
  border: 2px solid #e2e8f0;
  transition: all 0.3s ease;
  box-shadow: 0 2px 6px rgba(45, 74, 143, 0.08);
}

.rec-thumb:hover {
  border-color: #2d4a8f;
  transform: scale(1.05);
  box-shadow: 0 4px 12px rgba(45, 74, 143, 0.2);
}

.status-badge {
  background: linear-gradient(135deg, #2d4a8f 0%, #1a2a56 100%);
  color: #fff;
  padding: 6px 12px;
  border-radius: 999px;
  font-size: 12px;
  font-weight: 600;
  box-shadow: 0 2px 8px rgba(45, 74, 143, 0.2);
}

/* Hover Overlay */
.hover-overlay {
  position: absolute;
  inset: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  opacity: 0;
  transition: opacity 0.2s ease;
  pointer-events: none;
  border-radius: 12px;
}

.history-item:hover .hover-overlay {
  opacity: 1;
  pointer-events: auto;
}

.overlay-actions {
  display: flex;
  gap: 12px;
}

.action-btn {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px 20px;
  border: none;
  border-radius: 10px;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  color: #fff;
  letter-spacing: -0.01em;
}

.detail-btn {
  background: linear-gradient(135deg, #2d4a8f 0%, #1a2a56 100%);
  box-shadow: 0 2px 8px rgba(45, 74, 143, 0.3);
}

.detail-btn:hover {
  background: linear-gradient(135deg, #1a2a56 0%, #0f1a3a 100%);
  transform: translateY(-3px);
  box-shadow: 0 6px 16px rgba(45, 74, 143, 0.4);
}

.change-btn {
  background: linear-gradient(135deg, #475569 0%, #334155 100%);
  box-shadow: 0 2px 8px rgba(71, 85, 105, 0.3);
}

.change-btn:hover {
  background: linear-gradient(135deg, #334155 0%, #1e293b 100%);
  transform: translateY(-3px);
  box-shadow: 0 6px 16px rgba(71, 85, 105, 0.4);
}


/* Change Status Modal Styles */
.modal-backdrop {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.35);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 20px;
  z-index: 10000;
}

.modal-card {
  width: min(500px, 90vw);
  background: #fff;
  border-radius: 16px;
  padding: 24px;
  box-shadow: 0 12px 40px rgba(0, 0, 0, 0.22);
  animation: pop 0.18s ease;
}

@keyframes pop {
  from {
    transform: translateY(-6px);
    opacity: 0.9;
  }
  to {
    transform: translateY(0);
    opacity: 1;
  }
}

.selected-place {
  border-radius: 12px;
  background: linear-gradient(135deg, #ffffff 0%, #f8fafc 100%);
  border: 2px solid #e2e8f0;
  justify-content: flex-start;
  box-shadow: 0 2px 8px rgba(45, 74, 143, 0.05);
}

.thumb {
  width: 64px;
  height: 64px;
  object-fit: cover;
  border-radius: 8px;
  flex-shrink: 0;
}

.option {
  background: linear-gradient(135deg, #ffffff 0%, #f8fafc 100%);
  border: 2px solid #e2e8f0;
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  position: relative;
  box-shadow: 0 2px 8px rgba(45, 74, 143, 0.05);
}

.option.selected {
  border-color: #2d4a8f;
  background: linear-gradient(135deg, #f8fafc 0%, #f1f5f9 100%);
  transform: translateY(-2px);
  box-shadow: 0 8px 20px rgba(45, 74, 143, 0.15);
}

.option .icon {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #f1f5f9 0%, #e2e8f0 100%);
  color: #2d4a8f;
  font-weight: 700;
  font-size: 18px;
  flex-shrink: 0;
  border: 2px solid #e2e8f0;
}

.option.selected .icon {
  background: linear-gradient(135deg, #2d4a8f 0%, #1a2a56 100%);
  color: #ffffff;
  border-color: #2d4a8f;
}

.option:hover {
  box-shadow: 0 8px 20px rgba(45, 74, 143, 0.12);
  transform: translateY(-2px);
  border-color: #cbd5e1;
}

.select-check {
  position: absolute;
  right: 16px;
  top: 50%;
  transform: translateY(-50%);
  background: linear-gradient(135deg, #2d4a8f 0%, #1a2a56 100%);
  color: #fff;
  width: 36px;
  height: 36px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 700;
  box-shadow: 0 6px 18px rgba(45, 74, 143, 0.25);
  border: 2px solid rgba(255, 255, 255, 0.3);
}

/* 로딩 스피너 */
.spinner-border {
  width: 3rem;
  height: 3rem;
  border-width: 0.3em;
  color: #2d4a8f;
  border-right-color: transparent;
}

/* 텍스트 스타일 */
.fw-medium {
  color: #1e293b;
  font-weight: 600;
}

.text-muted {
  color: #64748b !important;
}

.small.text-muted {
  font-size: 0.875rem;
}
</style>