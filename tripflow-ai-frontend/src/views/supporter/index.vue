<template>
  <div class="supporter-page">
    <!-- Header -->
    <div class="supporter-header border-bottom bg-white">
      <div class="container">
        <div class="d-flex gap-3 align-items-center" style="padding: 1.5rem 0;">
          <div class="rounded-3 bg-secondary-subtle d-flex align-items-center justify-content-center"
            style="width: 46px; height: 46px">
            💬
          </div>

          <div>
            <h5 class="mb-1 title">서포터</h5>
            <p class="text-muted small mb-0 sub">
              실시간으로 당신의 여행을 도와드립니다
            </p>
          </div>
        </div>
      </div>
    </div>

    <div class="container">
    <div class="map-content-wrapper d-flex gap-3" style="margin-top: 1rem; margin-bottom: 2rem;">
      <!-- 왼쪽: 지도 영역 -->
      <div class="map-wrapper-full flex-grow-1">
        <div class="map-top-row d-flex align-items-start justify-content-between">
          <nav class="browser-tabs" role="tablist" aria-label="Map tabs">
            <button role="tab" :class="['tab-btn', { active: currentTab === 'image' }]" @click="currentTab = 'image'">
              이미지 기반 여행 AI
            </button>
            <button role="tab" :class="['tab-btn', { active: currentTab === 'restroom' }]" @click="currentTab = 'restroom'">
              공중 화장실
            </button>
          </nav>

          <div class="map-file-label small text-muted" role="button" title="Files">
            <i class="bi bi-folder2-open-fill"></i>
          </div>
        </div>

        <div class="card map-container shadow-sm border-0 p-0 position-relative">
          <NaverMap
            v-if="currentTab === 'image'"
            :markers="historyMarkers"
            :initialCenter="{ lat: 37.45, lng: 127.05 }"
            :initialZoom="11"
            :fitBoundsMode="true"
          />
          <NaverMap
            v-if="currentTab === 'restroom'"
            ref="restroomMapRef"
            :markers="toiletMarkers"
            :initialCenter="mapCenter"
            :initialZoom="16"
            :fitBoundsMode="false"
            @bounds-changed="onBoundsChanged"
          />
        </div>
      </div>

      <!-- 오른쪽: 사이드바 콘텐츠 -->
      <div class="sidebar-content-wrapper flex-shrink-0">
        <div v-show="currentTab === 'image'" class="sidebar-section">
          <BaseSection title="이미지 기반 여행 AI" subtitle="사진을 올리면 관련된 장소를 추천해드립니다.">
            <template #icon>
              <div class="ai-badge"><i class="bi bi-camera-fill"></i></div>
            </template>

            <div class="image-ui-row d-flex flex-column gap-3">
              <!-- 안내 박스 -->
              <div class="how-works">
                <div class="how-works-header mb-2">
                  <i class="bi bi-lightbulb text-warning me-2"></i>
                  <strong>어떻게 동작하나요?</strong>
                </div>
                <ol class="small text-muted mb-0 ps-3 how-works-list">
                  <li>여행 중 궁금한 점을 사진으로 올려보세요.</li>
                  <li>AI가 이미지를 분석합니다.</li>
                  <li>사진과 관련된 장소 추천을 받아보세요.</li>
                </ol>
              </div>

              <!-- 업로드 버튼 -->
              <label
                class="upload-control d-block btn-interactive"
                @dragover.prevent
                @drop.prevent="onDrop"
                for="imageInput"
                @click.prevent="goToImageAINew"
                title="Open Image AI"
              >
                <div class="upload-gradient-sm d-flex align-items-center justify-content-center w-100">
                  <div class="text-center w-100">
                    <template v-if="imagePreview">
                      <img :src="imagePreview" alt="preview" class="preview-img rounded" />
                    </template>
                    <template v-else>
                      <i class="bi bi-camera-fill" style="font-size: 1.75rem;"></i>
                      <div class="mt-2 label-text fw-semibold">업로드</div>
                    </template>
                  </div>
                </div>
              </label>

              <!-- 히스토리 버튼 -->
              <label class="upload-control d-block btn-interactive" @click.prevent="goToImageAIHistory" title="History">
                <div class="upload-gradient-sm history-control d-flex align-items-center justify-content-center w-100">
                  <div class="text-center w-100">
                    <i class="bi bi-clock-history" style="font-size: 1.75rem;"></i>
                    <div class="mt-2 label-text fw-semibold">히스토리</div>
                  </div>
                </div>
              </label>
            </div>

            <input id="imageInput" type="file" accept="image/*" class="d-none" @change="onFileChange" />
          </BaseSection>
        </div>

        <div v-show="currentTab === 'restroom'" class="sidebar-section">
          <BaseSection title="근처 공중 화장실" subtitle="근처에 있는 공중 화장실을 찾아보세요.">
            <template #icon>
              <div class="ai-badge"><i class="bi bi-person-standing"></i></div>
            </template>

            <div v-if="isLoadingRestrooms" class="text-center py-4">
              <div class="spinner-border text-primary" role="status">
                <span class="visually-hidden">로딩 중...</span>
              </div>
              <p class="mt-2 text-muted small">주변 화장실 검색 중...</p>
            </div>

            <div v-else class="list-group">
              <a
                v-for="(r, i) in filteredRestrooms"
                :key="i"
                href="#"
                class="list-group-item list-group-item-action mb-2 d-flex align-items-center rounded border-0 shadow-sm"
                @click.prevent="focusOnRestroom(r)"
              >
                <div class="me-3 icon-box d-flex align-items-center justify-content-center">
                  <i class="bi bi-person-standing text-primary fs-4"></i>
                </div>
                <div class="flex-fill">
                  <div class="fw-medium small">{{ r.name || '공중화장실' }}</div>
                  <div class="small text-muted" style="font-size: 0.75rem;">
                    <i class="bi bi-geo-alt me-1"></i> {{ r.address || r.roadAddress || '주소 정보 없음' }}
                  </div>
                </div>
                <div class="ms-3 text-muted"><i class="bi bi-chevron-right"></i></div>
              </a>

              <div v-if="filteredRestrooms.length === 0" class="text-center py-4 text-muted">
                주변에 등록된 화장실이 없습니다
              </div>
            </div>
          </BaseSection>
        </div>
      </div>
    </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed, watch } from 'vue'
import { useRouter } from 'vue-router'
import BaseSection from '@/components/common/BaseSection.vue'
import NaverMap from '@/components/supporter/NaverMap.vue'
import ToiletApi from '@/api/ToiletApi'
import imageSearchApi from '@/api/imageSearchApi'
import { useAuthStore } from '@/store/authStore'
import { useTravelStore } from '@/store/travelStore'
import { getChecklistBundleByPlanAndDay } from '@/api/checklistApi'

const router = useRouter()
const authStore = useAuthStore()
const travelStore = useTravelStore()

/** ✅ 네이버 maps 준비될 때까지 기다림 (지도 렌더를 막지는 않음) */
const waitForNaverMaps = (timeoutMs = 8000, intervalMs = 80) => {
  return new Promise((resolve) => {
    const start = Date.now()
    const timer = setInterval(() => {
      if (window.naver?.maps) {
        clearInterval(timer)
        resolve(true)
        return
      }
      if (Date.now() - start > timeoutMs) {
        clearInterval(timer)
        resolve(false)
      }
    }, intervalMs)
  })
}

const ensureNaverReady = async () => {
  const ok = await waitForNaverMaps()
  if (!ok) console.warn('⚠️ window.naver.maps가 아직 없습니다. (스크립트 로드 지연/실패 가능)')
  return ok
}

/** ✅ 체크리스트 */
const checklistBundle = ref(null)
const isLoadingChecklist = ref(false)
const checklistError = ref(null)

const loadChecklistAgent = async () => {
  try {
    checklistError.value = null
    isLoadingChecklist.value = true

    // ✅ [수정] try-catch로 안전하게 가져오기
    let storedPlanId = null;
    try {
      storedPlanId = localStorage.getItem('planId');
    } catch (e) {
      console.warn('LocalStorage access denied (index.vue):', e);
    }

    const planId = travelStore.planId ?? storedPlanId;
    // ✅ 테스트용 dayIndex 강제
    const dayIndex = 1

    if (!planId) {
      console.warn('⚠️ planId가 없어서 체크리스트 호출 스킵', { planId, dayIndex })
      checklistBundle.value = null
      return
    }

    const data = await getChecklistBundleByPlanAndDay(planId, dayIndex)
    checklistBundle.value = data
    console.log('✅ 체크리스트 에이전트 응답:', data)
  } catch (e) {
    checklistError.value = e
    console.error('❌ 체크리스트 에이전트 호출 실패:', e)
  } finally {
    isLoadingChecklist.value = false
  }
}

/** Map-related state */
const currentTab = ref('image')

// 히스토리 마커 (Image 탭)
const historyMarkers = ref([])
const isLoadingHistory = ref(false)

// Restroom 탭
const restroomMapRef = ref(null)
const mapCenter = ref({ lat: 37.5665, lng: 126.9780 })
const userLocation = ref(null)
const toiletMarkers = ref([])
const nearestRestrooms = ref([])
const isLoadingRestrooms = ref(false)

const filteredRestrooms = computed(() => nearestRestrooms.value.filter(r => r != null))

const getUserLocation = () => {
  return new Promise((resolve, reject) => {
    if (!navigator.geolocation) return reject(new Error('Geolocation not supported'))
    navigator.geolocation.getCurrentPosition(
      (position) => resolve({ lat: position.coords.latitude, lng: position.coords.longitude }),
      (error) => reject(error)
    )
  })
}

/** ✅ Image History 로드 (마커 데이터는 naver 준비 후에만 세팅) */
const loadImageHistory = async () => {
  try {
    isLoadingHistory.value = true

    const userId = authStore.userId
    if (!userId) {
      console.warn('❌ 사용자 ID가 없습니다.')
      historyMarkers.value = []
      return
    }

    console.log('🖼️ 이미지 히스토리 로드 중... userId:', userId)
    const response = await imageSearchApi.getSessionsByUserId(userId)

    const sessions = Array.isArray(response) ? response : (response.data || [])
    const markersMap = new Map()

    sessions.forEach(session => {
      const candidates = session.candidates || []
      const selectedCandidate = candidates.find(c => c.isSelected === true)

      if (selectedCandidate?.place?.lat && selectedCandidate?.place?.lng) {
        const place = selectedCandidate.place
        const key = `${place.lat},${place.lng}`
        if (!markersMap.has(key)) {
          markersMap.set(key, {
            lat: place.lat,
            lng: place.lng,
            title: place.name || '추천 장소',
            info: `
              <div style="padding: 10px; min-width: 200px;">
                <strong>${place.name || '추천 장소'}</strong>
                <div style="margin-top: 8px; font-size: 12px; color: #666;">
                  ${place.address || '주소 정보 없음'}
                </div>
                <div style="margin-top: 6px; font-size: 11px; color: #999;">
                  순위: ${selectedCandidate.rank || '-'}
                </div>
              </div>
            `
          })
        }
      }
    })

    historyMarkers.value = Array.from(markersMap.values())
    console.log('🎯 히스토리 마커 개수:', historyMarkers.value.length)
  } catch (error) {
    console.error('❌ 히스토리 로드 실패:', error)
    historyMarkers.value = []
  } finally {
    isLoadingHistory.value = false
  }
}

// 가까운 화장실 3개
const loadNearestRestrooms = async () => {
  try {
    isLoadingRestrooms.value = true

    let location = userLocation.value
    if (!location) {
      try {
        location = await getUserLocation()
        userLocation.value = location
        mapCenter.value = location
      } catch (error) {
        console.warn('사용자 위치를 가져올 수 없습니다. 기본 위치 사용')
        location = mapCenter.value
      }
    }

    const toilets = await ToiletApi.getNearestToilets(location.lat, location.lng, 3)
    nearestRestrooms.value = Array.isArray(toilets) ? toilets.filter(t => t != null) : []
  } catch (error) {
    console.error('❌ 가까운 화장실 로드 실패:', error)
    nearestRestrooms.value = []
  } finally {
    isLoadingRestrooms.value = false
  }
}

// 지도 범위 내 화장실
const loadToiletsInBounds = async (bounds) => {
  try {
    if (!bounds) return

    const toilets = await ToiletApi.getToiletsInBounds(bounds)

    toiletMarkers.value = Array.isArray(toilets)
      ? toilets
          .filter(toilet => toilet != null && (toilet.latitude || toilet.lat) && (toilet.longitude || toilet.lng))
          .map(toilet => ({
            lat: toilet.latitude || toilet.lat,
            lng: toilet.longitude || toilet.lng,
            title: toilet.toiletName || toilet.name || '공중화장실',
            info: `
              <div style="padding: 10px; min-width: 200px;">
                <strong>${toilet.toiletName || toilet.name || '공중화장실'}</strong>
                <div style="margin-top: 8px; font-size: 12px; color: #666;">
                  ${toilet.address || toilet.roadAddress || '주소 정보 없음'}
                </div>
              </div>
            `
          }))
      : []
  } catch (error) {
    console.error('❌ 범위 내 화장실 로드 실패:', error)
    toiletMarkers.value = []
  }
}

const onBoundsChanged = (bounds) => {
  console.log('🟡 onBoundsChanged 호출됨:', bounds)
  loadToiletsInBounds(bounds)
}

// 특정 화장실 포커스
const focusOnRestroom = async (restroom) => {
  const ok = await ensureNaverReady()
  if (!ok) return

  if (restroomMapRef.value && restroomMapRef.value.map) {
    const map = restroomMapRef.value.map
    const lat = restroom.latitude || restroom.lat
    const lng = restroom.longitude || restroom.lng
    const location = new window.naver.maps.LatLng(lat, lng)
    map.setCenter(location)
    map.setZoom(18)
  }
}

// 탭 전환 시 데이터 로드
watch(currentTab, async (newTab) => {
  if (newTab === 'restroom') {
    console.log('🟢 Restroom 탭 활성화 - 데이터 로드 시작')

    if (nearestRestrooms.value.length === 0) {
      await loadNearestRestrooms()
    }

    setTimeout(() => {
      if (restroomMapRef.value && restroomMapRef.value.map) {
        const map = restroomMapRef.value.map
        const bounds = map.getBounds()
        if (bounds) {
          const ne = bounds.getNE()
          const sw = bounds.getSW()
          onBoundsChanged({
            northEastLat: ne.lat(),
            northEastLng: ne.lng(),
            southWestLat: sw.lat(),
            southWestLng: sw.lng()
          })
        }
      }
    }, 1000)
  } else if (newTab === 'image') {
    console.log('🟢 Image 탭 활성화 - 히스토리 로드')

    if (historyMarkers.value.length === 0) {
      // ✅ naver 준비된 뒤에만 마커 데이터 세팅
      const ok = await ensureNaverReady()
      if (ok) await loadImageHistory()
    }
  }
})

// ✅ 마운트 시: 체크리스트 먼저 → naver 준비 → 히스토리(마커) 로드
onMounted(async () => {
  console.log('🟢 Supporter 페이지 마운트')

  // planId/dayIndex 초기화
  if (!travelStore.planId || travelStore.dayIndex === null) {
    const userId = authStore.userId
    if (userId) {
      await travelStore.initializeTravelInfo(userId)
    }
  }

  // 1) ✅ 체크리스트 먼저
  await loadChecklistAgent()

  // 2) ✅ naver 준비된 뒤에만 마커(히스토리) 로드
  const ok = await ensureNaverReady()
  if (ok) {
    await loadImageHistory()
  } else {
    historyMarkers.value = []
  }
})

/** image upload handling */
const imagePreview = ref(null)
const triggerFile = () => document.getElementById('imageInput')?.click()

const onFileChange = (e) => {
  const f = e.target.files && e.target.files[0]
  if (!f) return
  const reader = new FileReader()
  reader.onload = (ev) => (imagePreview.value = ev.target.result)
  reader.readAsDataURL(f)
}

const onDrop = (e) => {
  const f = e.dataTransfer.files && e.dataTransfer.files[0]
  if (!f) return
  const reader = new FileReader()
  reader.onload = (ev) => (imagePreview.value = ev.target.result)
  reader.readAsDataURL(f)
}

// 라우팅
const goToImageAI = () => {
  router.push({ name: 'History' }).catch(() => {})
}

const goToImageAINew = () => {
  router
    .push({ name: 'CreateNewSearch' })
    .then(() => window.scrollTo({ top: 0, left: 0, behavior: 'smooth' }))
    .catch(() => {})
}

const goToImageAIHistory = () => {
  if (!authStore.userId) {
    alert('로그인이 필요한 서비스입니다.\n히스토리를 확인하려면 로그인해주세요.')
    window.location.href = 'http://localhost:8080/oauth2/authorization/google'
    return
  }

  router
    .push({ name: 'History' })
    .then(() => window.scrollTo({ top: 0, left: 0, behavior: 'smooth' }))
    .catch(() => {})
}
</script>

<style scoped>
.supporter-page {
  background-color: #ffffff;
  min-height: 100vh;
  padding-bottom: 6rem;
}

.supporter-header {
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.05);
}

.supporter-header .title {
  font-size: 1.25rem;
  font-weight: 600;
  color: #1e293b;
}

.supporter-header .sub {
  color: #64748b;
}

.supporter-header .bg-secondary-subtle {
  background-color: #f1f5f9 !important;
}

.map-container {
  min-height: 380px;
  overflow: visible;
  border-radius: 12px;
}

.map-gradient {
  height: 100%;
  background: #f3fffb;
  border-radius: 12px;
  position: relative;
  overflow: hidden;
}

.map-top-bar {
  z-index: 250;
  pointer-events: auto;
}

.map-top-bar .tab-btn {
  pointer-events: auto;
}

.browser-tabs {
  display: inline-flex;
  gap: 6px;
  align-items: center;
  background: #f8fafc;
  padding: 4px;
  border-radius: 12px;
  border: 1px solid #e2e8f0;
}

.tab-btn {
  background: transparent;
  border: none;
  padding: 10px 20px;
  font-size: 0.9rem;
  font-weight: 500;
  border-radius: 8px;
  cursor: pointer;
  color: #64748b;
  transition: all 0.2s ease;
  white-space: nowrap;
}

.tab-btn:hover {
  background: #f1f5f9;
  color: #475569;
}

.tab-btn.active {
  background: #2d4a8f;
  color: #ffffff;
  font-weight: 600;
  box-shadow: 0 2px 8px rgba(45, 74, 143, 0.2);
}

.tab-btn:focus {
  outline: 2px solid rgba(45, 74, 143, 0.2);
  outline-offset: 2px;
}

.map-marker {
  position: absolute;
  transform: translate(-50%, -50%);
  font-size: 20px;
  z-index: 5;
  background: rgba(255, 255, 255, 0.9);
  padding: 6px;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}

.marker-image {
  color: #fff;
  background: rgba(58, 87, 151, 0.95);
}

.marker-restroom {
  color: #0d6efd;
  background: rgba(255, 255, 255, 0.9);
}

.gps-center {
  position: absolute;
  left: 50%;
  top: 50%;
  transform: translate(-50%, -50%);
  text-align: center;
  z-index: 1;
}

.detail-area {
  min-height: 360px;
  border-radius: 12px;
}

.ai-badge {
  width: 44px;
  height: 44px;
  border-radius: 10px;
  background: #2d4a8f;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
}

.upload-gradient {
  height: 120px;
  border-radius: 12px;
  background: linear-gradient(135deg, #2d4a8f 0%, #1a2a56 100%);
  border: 1px solid rgba(255, 255, 255, 0.1);
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  transition: all 0.3s ease;
  cursor: pointer;
}

.upload-gradient:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(45, 74, 143, 0.3);
}

.image-ui-row .history-column .upload-gradient {
  background: linear-gradient(135deg, #64748b 0%, #475569 100%);
  border: 1px solid rgba(255, 255, 255, 0.1);
}

.image-ui-row .history-column .upload-gradient:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(100, 116, 139, 0.3);
}

.image-ui-row .history-column .upload-gradient .text-white-50,
.image-ui-row .history-column .upload-gradient i,
.image-ui-row .history-column .label-text {
  color: #ffffff !important;
}

.preview-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  border-radius: 10px;
}

.checklist-wrapper {
  cursor: pointer;
  user-select: none;
}

.checklist-wrapper :deep(.upload-header) {
  cursor: pointer;
  user-select: none;
}

.checklist-wrapper :deep(.upload-header):hover {
  opacity: 0.9;
}

.map-top-row {
  position: relative;
  z-index: 80;
  margin-bottom: 3px;
}

.map-file-label {
  z-index: 85;
  background: rgba(255, 255, 255, 0.9);
  padding: 6px 10px;
  border-radius: 8px;
}

.map-container {
  position: relative;
  z-index: 10;
  margin-top: 0;
}

@media (max-width: 991px) {
  .col-md-4,
  .col-md-8 {
    flex: 0 0 100%;
    max-width: 100%;
  }

  .map-gradient {
    height: 280px;
  }
}

@media (min-width: 0) {
  .image-ui-row {
    align-items: stretch;
    margin-top: 8px;
    min-height: 140px;
    height: 100%;
    display: flex;
  }

  .image-ui-row .how-works {
    flex: 2 2 0;
    min-width: 0;
    display: flex;
    flex-direction: column;
    justify-content: flex-start;
    margin-top: 0;
    gap: 8px;
    padding: 1.25rem;
    background: #f8fafc;
    border-radius: 12px;
    border: 1px solid #e2e8f0;
    margin-right: 12px;
  }

  .how-works-header {
    color: #2d4a8f;
    font-size: 1rem;
    display: flex;
    align-items: center;
  }

  .how-works-list {
    line-height: 1.8;
    color: #64748b;
  }

  .how-works-list li {
    margin-bottom: 0.5rem;
  }

  .how-works-list li:last-child {
    margin-bottom: 0;
  }

  .image-ui-row .upload-column,
  .image-ui-row .history-column {
    flex: 1 1 0;
    min-width: 0;
    display: flex;
    align-items: stretch;
    justify-content: center;
  }

  .image-ui-row .col {
    min-width: 0;
    display: flex;
    flex-direction: column;
  }

  .image-ui-row .upload-column .upload-control,
  .image-ui-row .history-column .upload-control {
    flex: 1 1 auto;
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    width: 100%;
    height: 100%;
    padding: 0;
  }

  .image-ui-row .upload-column .upload-gradient,
  .image-ui-row .history-column .upload-gradient {
    width: 100%;
    height: 100%;
    border-radius: 12px;
    display: flex;
    align-items: center;
    justify-content: center;
    min-height: 140px;
  }
}

/* ✅ 좌우 레이아웃 - 지도 + 사이드바 */
.map-content-wrapper {
  display: flex;
  gap: 1.5rem;
  align-items: flex-start;
  margin-top: 1rem;
}

.map-wrapper-full {
  flex: 1;
  min-width: 0;
}

.sidebar-content-wrapper {
  margin-top: 52px;
  width: 440px;
  max-height: calc(100vh - 250px);
  overflow-y: auto;
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.sidebar-section {
  display: flex;
  flex-direction: column;
}

/* ✅ 버튼 상호작용 */
.btn-interactive {
  cursor: pointer;
  transition: all 0.2s ease;
}

.btn-interactive:active {
  opacity: 0.9;
}

/* ✅ 사이드바용 업로드 그래디언트 */
.upload-gradient-sm {
  height: 100px;
  border-radius: 12px;
  background: linear-gradient(135deg, #2d4a8f 0%, #1a2a56 100%);
  border: 2px solid rgba(255, 255, 255, 0.15);
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  transition: all 0.3s ease;
  cursor: pointer;
  position: relative;
  overflow: hidden;
}

.upload-gradient-sm::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(255, 255, 255, 0.1);
  opacity: 0;
  transition: opacity 0.3s ease;
}

.upload-gradient-sm:hover {
  transform: translateY(-3px);
  box-shadow: 0 6px 16px rgba(45, 74, 143, 0.4);
  border-color: rgba(255, 255, 255, 0.25);
}

.upload-gradient-sm:hover::before {
  opacity: 1;
}

.upload-gradient-sm.history-control {
  background: linear-gradient(135deg, #64748b 0%, #475569 100%);
}

.upload-gradient-sm.history-control:hover {
  box-shadow: 0 6px 16px rgba(100, 116, 139, 0.4);
}

.upload-gradient-sm .text-white-50,
.upload-gradient-sm i,
.upload-gradient-sm .label-text,
.upload-gradient-sm .label-subtext {
  color: #ffffff !important;
}

.upload-gradient-sm i {
  display: block;
  margin-bottom: 0.25rem;
}

.label-text {
  font-size: 1rem;
  font-weight: 600;
  letter-spacing: 0.5px;
}

.label-subtext {
  font-size: 0.8rem;
  color: rgba(255, 255, 255, 0.85) !important;
  margin-top: 0.25rem;
}

/* ✅ 사이드바 반응형 */
@media (max-width: 1200px) {
  .map-content-wrapper {
    gap: 1rem;
  }

  .sidebar-content-wrapper {
    width: 380px;
  }
}

@media (max-width: 991px) {
  .map-content-wrapper {
    flex-direction: column;
    gap: 1.5rem;
  }

  .sidebar-content-wrapper {
    width: 100%;
    max-height: none;
  }

  .image-ui-row {
    flex-direction: column !important;
  }

  .map-container {
    min-height: 320px;
  }
}
</style>
