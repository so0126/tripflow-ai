<template>
  <section>
    <h5 class="mb-3"><i class="bi bi-people-fill me-2"></i>근처 공중 화장실</h5>

    <!-- NaverMap 컴포넌트 사용 -->
    <NaverMap
      ref="naverMapRef"
      :markers="toiletMarkers"
      :initialCenter="mapCenter"
      :initialZoom="16"
      :fitBoundsMode="false"
      @bounds-changed="onBoundsChanged"
    />

    <div class="list-group mt-3">
      <div v-if="isLoadingRestrooms" class="text-center py-4">
        <div class="spinner-border text-primary" role="status">
          <span class="visually-hidden">로딩 중...</span>
        </div>
        <p class="mt-2 text-muted">주변 화장실 검색 중...</p>
      </div>
      
      <template v-else>
        <a v-for="(r, i) in filteredRestrooms" :key="i" href="#"
           class="list-group-item list-group-item-action mb-2 d-flex align-items-center rounded border-0 shadow-sm"
           @click.prevent="focusOnRestroom(r)">
          <div class="me-3 icon-box d-flex align-items-center justify-content-center">
            <i class="bi bi-people-fill text-primary fs-4"></i>
          </div>
          <div class="flex-fill">
            <div class="fw-medium">{{ r.toiletName || '공중화장실' }}</div>
            <div class="small text-muted">
              <i class="bi bi-geo-alt me-1"></i> {{ r.address || r.roadAddress || '주소 정보 없음' }}
            </div>
          </div>
          <div class="ms-3 text-muted"><i class="bi bi-chevron-right"></i></div>
        </a>
        
        <div v-if="filteredRestrooms.length === 0" class="text-center py-4 text-muted">
          주변에 등록된 화장실이 없습니다.
        </div>
      </template>
    </div>
  </section>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import NaverMap from '@/components/supporter/NaverMap.vue'
import ToiletApi from '@/api/ToiletApi'

const naverMapRef = ref(null)
const mapCenter = ref({ lat: 37.5665, lng: 126.9780 }) // 서울 중심
const userLocation = ref(null)

const toiletMarkers = ref([])
const nearestRestrooms = ref([])
const isLoadingRestrooms = ref(false)

// null 필터링된 화장실 목록 (computed)
const filteredRestrooms = computed(() => {
  return nearestRestrooms.value.filter(r => r != null)
})

// 사용자 위치 가져오기
const getUserLocation = () => {
  return new Promise((resolve, reject) => {
    if (navigator.geolocation) {
      navigator.geolocation.getCurrentPosition(
        (position) => {
          const location = {
            lat: position.coords.latitude,
            lng: position.coords.longitude
          }
          resolve(location)
        },
        (error) => {
          console.error('Geolocation 오류:', error)
          reject(error)
        }
      )
    } else {
      reject(new Error('Geolocation not supported'))
    }
  })
}

// 가까운 화장실 3개 가져오기
const loadNearestRestrooms = async () => {
  try {
    isLoadingRestrooms.value = true
    
    // 사용자 위치 가져오기
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
    
    console.log('🚻 가까운 화장실 검색 - 위치:', location)
    
    // 백엔드 API 호출
    const toilets = await ToiletApi.getNearestToilets(location.lat, location.lng, 3)
    
    console.log('✅ 가까운 화장실:', toilets)
    // null 체크 및 필터링
    nearestRestrooms.value = Array.isArray(toilets) ? toilets.filter(t => t != null) : []
    
  } catch (error) {
    console.error('❌ 가까운 화장실 로드 실패:', error)
    nearestRestrooms.value = []
  } finally {
    isLoadingRestrooms.value = false
  }
}

// 지도 범위 내 화장실 가져오기
const loadToiletsInBounds = async (bounds) => {
  try {
    if (!bounds) return
    
    console.log('🗺️ 지도 범위 내 화장실 검색:', bounds)
    
    // 백엔드 API 호출
    const toilets = await ToiletApi.getToiletsInBounds(bounds)
    
    console.log('✅ 범위 내 화장실 개수:', toilets.length)
    
    // 마커 데이터로 변환 (null 체크)
    toiletMarkers.value = Array.isArray(toilets) 
      ? toilets
          .filter(toilet => toilet != null && toilet.latitude && toilet.longitude)
          .map(toilet => ({
            lat: toilet.latitude,
            lng: toilet.longitude,
            title: toilet.toiletName || '공중화장실',
            info: `
              <div style="padding: 10px; min-width: 200px;">
                <strong>${toilet.toiletName || '공중화장실'}</strong>
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

// 지도 범위 변경 이벤트 핸들러
const onBoundsChanged = (bounds) => {
  console.log('🟡 onBoundsChanged 호출됨:', bounds)
  loadToiletsInBounds(bounds)
}

// 특정 화장실에 포커스
const focusOnRestroom = (restroom) => {
  if (naverMapRef.value && naverMapRef.value.map) {
    const map = naverMapRef.value.map
    const location = new window.naver.maps.LatLng(restroom.latitude, restroom.longitude)
    map.setCenter(location)
    map.setZoom(18) // 신규 맵 타일: 18 레벨로 확대
  }
}

// 컴포넌트 마운트 시 초기 로드
onMounted(async () => {
  console.log('🟢 RestroomsMap onMounted 시작')
  await loadNearestRestrooms()
  console.log('🟢 RestroomsMap onMounted 완료')
})
</script>

<style scoped>
.icon-box {
  width: 48px;
  height: 48px;
  background: #f8f9fa;
  border-radius: 8px;
}

.list-group-item:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 8px rgba(0,0,0,0.1) !important;
}
</style>