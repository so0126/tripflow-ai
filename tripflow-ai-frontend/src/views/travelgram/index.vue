<template>
  <TravelgramHeader :show-back="false" />

    <div class="container">
      <ProfileSummary :profileName="displayName" bio="여행 애호가" initials="userInitials" :totalplans="stats.totalPlans"
        :travelDays="stats.travelDays" :completed="stats.completed" />
      <h4 class="my-3">
        <i class="bi bi-compass me-2 text-primary"></i> 완료된 여행 일정
      </h4>

      <div v-if="loading" class="text-center py-5">
        <div class="spinner-border text-primary" role="status">
          <span class="visually-hidden">로딩 중...</span>
        </div>
      </div>

      <div v-else-if="plans.length === 0" class="text-center py-5 text-muted">
        <p>There is not Ended Plan</p>
      </div>

      <div v-else class="plan-list">
        <planCard v-for="plan in plans" :key="plan.id" :planId="plan.id" :planTitle="plan.title"
          :location="'Seoul, Korea'" :date="plan.formattedDate" :budget="plan.formattedBudget" :planStatus="'Done'"
          @click="goToReview(plan.id, plan.title)" />
      </div>
    </div>
</template>

<script setup>
import TravelgramHeader from '@/components/travelgram/TravelgramHeader.vue'
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/store/authStore'
import api from '@/api/travelgramApi'
import { storeToRefs } from 'pinia'
import planCard from '@/components/travelgram/PlanCard.vue';
import ProfileSummary from "@/components/travelgram/ProfileSummary.vue";
import PageHeader from '@/components/common/header/PageHeader.vue';
// import defaultImg from '@/assets/img/profile-logo.png' // 필요시 경로 확인
// import BackButtonPageHeader from '@/components/common/BackButtonPageHeader.vue'

const authStore = useAuthStore()
const router = useRouter()

// 상태 관리
const plans = ref([])
const loading = ref(true)
const stats = ref({ totalPlans: 0, travelDays: 0, completed: 0 })

// 스토어에서 로그인된 유저 정보 가져오기 (vuex 구조에 따라 수정 필요
// 3. Extract using storeToRefs (maintains Reactivity and .value access)
const { userId, userName, userProfileImage } = storeToRefs(authStore)

const displayName = computed(() => userName.value || '여행자')

const computedProfileImage = computed(() => {
  // 1. 스토어에 이미지가 있으면 그것을 사용
  if (userProfileImage.value) {
    return userProfileImage.value
  }
  // 2. 없으면 기본 이미지 (assets 경로 처리)
  return new URL('@/assets/img/profile-logo.png', import.meta.url).href
})

// (선택사항) 이름에서 이니셜 추출 (예: "Jessica Han" -> "JH")
const userInitials = computed(() => {
  const profileName = displayName.value;
  if (!profileName) return '나';
  return profileName.split(' ').map(n => n[0]).join('').toUpperCase().substring(0, 2);
})
// 데이터 가져오기 함수
const fetchPlans = async () => {
  try {
    loading.value = true
    // Safety check: Ensure userId exists before making the API call
    if (!userId.value) {
      console.warn("User ID not found, attempting to load from storage...");
      authStore.initializeAuth(); // Ensure auth is loaded
      if (!userId.value) return; // Stop if still no user
    }

    const targetId = userId.value
    console.log("Fetching plans for user:", targetId)

    // 백엔드 API 호출
    const response = await api.getAllPlanByUserId(targetId)
    const allPlans = response.data

    // 1. 종료된 여행(isEnded === true)만 필터링
    const completedPlans = allPlans.filter(p => p.isEnded === true)

    // 2. 데이터 가공 (UI에 맞게 포맷팅)
    plans.value = completedPlans.map(p => ({
      ...p,
      formattedDate: `${p.startDate} ~ ${p.endDate}`,
      formattedBudget: Number(p.budget).toLocaleString() + ' KRW'
    }))

    // 3. 통계 업데이트 (선택 사항)
    stats.value = {
      totalPlans: allPlans.length,
      completed: completedPlans.length,
      travelDays: calculateTotalDays(completedPlans)
    }

  } catch (error) {
    console.error("여행 계획을 불러오는데 실패했습니다:", error)
  } finally {
    loading.value = false
  }
}

// 여행 일수 합계 계산 도우미 함수
const calculateTotalDays = (plansList) => {
  return plansList.reduce((acc, cur) => {
    const start = new Date(cur.startDate)
    const end = new Date(cur.endDate)
    const diff = (end - start) / (1000 * 60 * 60 * 24) + 1
    return acc + Math.max(1, Math.round(diff)) // 최소 1일 보장 및 반올림
  }, 0)
}

// ✅ 페이지 이동 함수
// PlanCard에서 emit된 id와 title을 인자로 받습니다.
const goToReview = async (id, title) => {
  console.log(`Navigating to review creation for Plan ID: ${id}, Title: ${title}`);

  let targetTitle = title;

  // 1. 제목이 없거나 비어있는 경우 체크
  if (!targetTitle || targetTitle.trim() === '') {
    try {
      // 로딩 인디케이터를 띄우고 싶다면 여기서 loading state 조작 가능

      console.log("제목이 없어 AI 생성을 요청합니다...");
      const response = await api.getPlanTitle(id); // 백엔드 호출

      // 2. 받아온 새 제목 할당
      targetTitle = response.data;
      console.log("새로 생성된 제목:", targetTitle);

      
      // 현재 화면의 리스트에도 새 제목을 즉시 반영하여 UI 업데이트
      const planItem = plans.value.find(p => p.id === id);
      if (planItem) {
        planItem.title = targetTitle;
      }

    } catch (error) {
      console.error("제목 생성 실패:", error);
      targetTitle = "My Trip"; // 실패 시 기본값 설정
    }
  }
    // 3. 제목을 쿼리로 넘기며 페이지 이동
    // (이미 리스트가 업데이트되었으므로 뒤로가기로 돌아와도 제목이 유지됩니다)
    router.push({
      name: 'CreateTravelReview',
      params: { planId: id,
        planTitle: targetTitle
       },
    })
  }
onMounted(() => {
  fetchPlans()
})

const goBack = () => router.push({name: 'plannercreate'});
</script>

<style scoped>
.travelgram-page {
  min-height: 100vh;
  background-color: #ffffff;
}

/* Header Styles (same as supporter) */
.travelgram-header {
  position: sticky;
  top: 0;
  z-index: 100;
  margin-bottom: 0;
}

.travelgram-header .title {
  font-weight: 600;
  color: #1B3B6F;
  margin: 0;
}

.travelgram-header .sub {
  font-size: 0.875rem;
  line-height: 1.2;
}

/* Container */
.container {
  padding-top: 2rem;
  padding-bottom: 2rem;
}

/* ✅ 핵심 수정 부분: 리스트형 배치 */
.plan-list {
  display: flex;
  flex-direction: column; /* 위에서 아래로 쌓이게 설정 */
  gap: 1.5rem;            /* 카드 사이의 간격 */
  margin-bottom: 2rem;
  width: 100%;            /* 리스트 전체 너비 100% */
}
</style>
