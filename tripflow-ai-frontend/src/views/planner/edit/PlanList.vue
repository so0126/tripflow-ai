<!-- src/views/planner/PlanList.vue -->
<template>
  <section class="plan-root card rounded-0 h-100 d-flex flex-column">

    <!-- Header -->
    <div class="p-4 pb-3 border-bottom d-flex align-items-center justify-content-between">
      <div class="d-flex gap-3 align-items-center">
        <div class="rounded-3 bg-secondary-subtle d-flex align-items-center justify-content-center"
          style="width: 46px; height: 46px">
          📅
        </div>

        <div>
          <h5 class="mb-1 title" v-html="highlightedTitle"></h5>
          <p class="text-muted small mb-0 sub">
            {{ plan?.startDate }} - {{ plan?.endDate }} • Seoul, Korea
          </p>
        </div>
      </div>
      <!-- Edit Button -->
      <button v-if="currentDayPlaces.length > 0" class="btn btn-outline-secondary edit-btn-large"
        @click="toggleEditMode">
        {{ editMode ? "편집 완료" : "편집" }}
      </button>
    </div>

    <!-- <transition name="fade">
      <div v-if="justSynced" class="sync-banner">
        ✔ 채팅에서 수정한 일정이 반영되었습니다
      </div>
    </transition> -->

    <NowActivity :place="nowPlace" :index="displayIndex" :total="currentDayPlaces.length" :dayIndex="selectedDayIndex"
      :globalIndex="displayGlobalIndex" :globalActiveIndex="globalActiveIndex" @update:index="browseIndex = $event"
      @complete="openActivityComplete" />

    <!-- 🔥 Body Component -->
    <div class="plan-body-scroll flex-grow-1 overflow-y-auto">

      <!-- ⭐ 전환 효과 핵심 -->
      <transition name="fade-slide" mode="out-in">

        <!-- 로딩 중 -->
        <div v-if="isLoadingPlan" key="loading" class="d-flex justify-content-center align-items-center"
          style="min-height: 300px;">
          <div class="text-center">
            <div class="spinner-border mb-3" role="status" style="width: 3rem; height: 3rem; color: #2d4a8f;">
              <span class="visually-hidden">불러오는 중...</span>
            </div>
            <p class="text-muted">일정을 불러오는 중입니다</p>
          </div>
        </div>

        <!-- 로딩 완료 -->
        <div v-else key="content">
          <div :class="{ 'server-updated': justSynced }">
            <PlanDayTimeline :days="days" :currentDayPlaces="currentDayPlaces" :selectedDayIndex="selectedDayIndex"
              :editMode="editMode" :typeColor="typeColor" :typeLabel="typeLabel" :formatTime="formatTime"
              :categoryMap="categoryMap" @open-modal="openModal" @delete-place="onDeletePlace"
              @update:selectedDayIndex="selectedDayIndex = $event" />
          </div>
          <NavigationButtons back-text="이전" @back="onBack" @next="onNext" class="mb-4">
            <template #next-content>
              {{ travelStore.isTraveling ? '여행 종료' : '여행 일정 요약페이지로 이동' }}
            </template>
          </NavigationButtons>
        </div>

      </transition>
    </div>

    <!-- Modals -->
    <ActivityDetailsModal :open="modalOpen" :data="modalData" @close="modalOpen = false" />

    <ReplaceModal :open="replaceModalOpen" :target="replaceTarget" :alternatives="replaceAlternatives"
      @close="replaceModalOpen = false" @apply-replacement="applyReplacement" @delete-anyway="deleteAnyway" />

    <ActivityCompleteModal :open="activityModalOpen" :title="activityModalData.title" :status="activityModalData.status"
      :comment="comment" :spendInput="spendInput" :quickStats="activityQuickStats" @close="activityModalOpen = false"
      @confirm="completeActivity" @update:spend-input="spendInput = $event" @update:comment="comment = $event" />
  </section>
</template>



<script setup>
import { ref, computed, onMounted, watch, nextTick } from "vue";

import { useRouter } from "vue-router";
import NavigationButtons from "@/components/common/button/NavigationButtons.vue"
import PageHeader from "@/components/common/header/PageHeader.vue";
import plannerApi from "@/api/plannerApi";


import { useAuthStore } from "@/store/authStore";
import { useTravelStore } from "@/store/travelStore";
import { useChatStore } from "@/store/chatStore";

import ActivityDetailsModal from "@/components/planner/ActivityDetailsModal.vue";
import ReplaceModal from "@/components/planner/ReplaceModal.vue";
import NowActivity from "@/components/planner/NowActivity.vue";
import PlanDayTimeline from "@/components/planner/PlanDayTimeline.vue";
import ActivityCompleteModal from "@/components/planner/ActivityCompleteModal.vue";

/* ---------- 기본 상태들 ---------- */
const modalOpen = ref(false);
const modalData = ref(null);

const isSyncingFromServer = ref(false);
const justSynced = ref(false);

const plan = ref(null);
const days = ref([]);
const selectedDayIndex = ref(0);
const editMode = ref(false);

const router = useRouter();
const authStore = useAuthStore();
const travelStore = useTravelStore();
const chatStore = useChatStore();

// fallback 이미지들
const fallbacks = [
  "/images/01.png",
  "/images/02.png",
  "/images/03.png",
  "/images/04.png",
  "/images/05.png",
  "/images/06.png",
];

/* ---------- ReplaceModal 상태 ---------- */
const replaceModalOpen = ref(false);
const replaceTarget = ref(null);
const replaceAlternatives = ref([]);

/* ---------- ACTIVITY COMPLETE MODAL 상태 ---------- */
const activityModalOpen = ref(false);
const activityModalData = ref({
  status: 'PENDING',
  memo: '',
  actualCost: null,
});
const activePlace = ref(null);
const spendInput = ref(null);
const comment = ref("");

/* ---------- NOW CARD 상태 ---------- */
const allPlaces = computed(() => {
  return days.value.flatMap(d => d.places);
});

const globalActiveIndex = computed(() => {
  return allPlaces.value.findIndex(p => p.isEnded === false);
});

const displayGlobalIndex = computed(() => {
  if (displayIndex.value == null) return null;
  return getGlobalIndex(selectedDayIndex.value, displayIndex.value);
});

// 사용자가 좌/우로 탐색 중일 때 쓰는 인덱스
const browseIndex = ref(null); // null = NOW 모드

// 현재 Day의 places
const currentDayPlaces = computed(() => {
  return days.value?.[selectedDayIndex.value]?.places ?? [];
});

const isCurrentDayEnded = computed(() => {
  const places = currentDayPlaces.value;
  if (!places.length) return true;

  return places.every(p => p.isEnded === true);
});

// 메인 NOW 인덱스 (isEnded === false 중 마지막)

// 실제로 화면에 보여줄 index
const displayIndex = computed(() => {
  // 사용자가 좌/우 탐색 중이면 그걸 유지
  if (browseIndex.value !== null) return browseIndex.value;

  // day 기준 규칙 적용
  return getNowIndexByDay(selectedDayIndex.value);
});


// 실제 NOW 카드에 표시할 place
const nowPlace = computed(() => {
  if (!travelStore.$state.isTraveling) return null;
  return currentDayPlaces.value[displayIndex.value] ?? null;
});

/* ---------- category 기본 이미지 ---------- */
const categoryDefaultImageMap = {
  FOOD: new URL("@/assets/img/travel-places/food.png", import.meta.url).href,
  SPOT: new URL("@/assets/img/travel-places/spot.png", import.meta.url).href,
  SHOPPING: new URL("@/assets/img/travel-places/shopping.png", import.meta.url).href,
  CAFE: new URL("@/assets/img/travel-places/cafe.png", import.meta.url).href,
  HOTEL: new URL("@/assets/img/hotel-image/0001.jpg", import.meta.url).href,
  EVENT: new URL("@/assets/img/travel-places/event.png", import.meta.url).href,
  ETC: new URL("@/assets/img/travel-places/etc.png", import.meta.url).href,
};

const getDefaultGalleryByType = (type = "ETC") => {
  return [categoryDefaultImageMap[type] ?? categoryDefaultImageMap.ETC];
};

/* ---------- util ---------- */
const formatTime = (isoString) => {
  if (!isoString) return "";
  const date = new Date(isoString);
  return date.toLocaleTimeString("ko-KR", {
    hour: "2-digit",
    minute: "2-digit",
    hour12: false,
  });
};

/* ✅ Duration 계산 (모달 QuickStats용) */
const getDurationText = (start, end) => {
  if (!start || !end) return "-";
  const diffMin = Math.round((new Date(end) - new Date(start)) / 60000);
  const h = Math.floor(diffMin / 60);
  const m = diffMin % 60;
  if (h && m) return `${h}h ${m}m`;
  if (h) return `${h}h`;
  return `${m}m`;
};

const isLoadingPlan = ref(true);

/* 날짜 계산 함수 */
const getTripDuration = computed(() => {
  if (!plan.value?.startDate || !plan.value?.endDate) {
    // return { nights: 3, days: 4, text: "3박 4일" };
    return { nights: 0, days: 0, text: "" };
  }

  const start = new Date(plan.value.startDate);
  const end = new Date(plan.value.endDate);

  // 날짜 차이 계산 (일 단위)
  const diffTime = Math.abs(end - start);
  const diffDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24));

  const days = diffDays + 1; // 당일 포함
  const nights = diffDays;   // 밤 수는 일수 - 1

  return {
    nights,
    days,
    text: `${nights}박 ${days}일`
  };
});

/* ✅ 모달에 넣을 QuickStats (장소마다 다르게) */
const activityQuickStats = computed(() => {
  if (!activePlace.value) return {};

  const started = activePlace.value.startAt ? formatTime(activePlace.value.startAt) : "-";
  const duration = getDurationText(activePlace.value.startAt, activePlace.value.endAt);
  const status = activePlace.value.isEnded ? "Completed" : "Pending";

  return { started, duration, status };
});

/* ---------- 모달 열기 로직 ---------- */
const openActivityComplete = async (place) => {
  activePlace.value = place;

  // 프론트 정보로 먼저 세팅
  activityModalData.value = {
    title: place.title,
    status: place.status ?? 'PENDING',
    memo: '',
    actualCost: null,
  };

  // 백엔드에서 활동 정보 조회 (금액, 메모 등만)
  try {
    const res = await plannerApi.getCurrentActivity(place.id || place.placeId);
    if (res?.data) {
      activityModalData.value.memo = res.data.memo || '';
      activityModalData.value.actualCost = res.data.actualCost ?? null;
    }
  } catch (e) {
    // 404 등 에러 시 기본값 유지
  }

  // 입력값도 동기화
  comment.value = activityModalData.value.memo;
  spendInput.value = activityModalData.value.actualCost;

  activityModalOpen.value = true;
};

watch(
  () => chatStore.livePlanFromChat?.updatedAt,
  async (newVal, oldVal) => {
    if (!newVal || newVal === oldVal) return;
    if (!chatStore.livePlanFromChat) return;

    console.log("🔥 [PlanList] 채팅 → 서버 응답 반영:", newVal);

    try {
      isSyncingFromServer.value = true;
      await applyAiPlan(chatStore.livePlanFromChat.data);

      justSynced.value = true;
      setTimeout(() => {
        justSynced.value = false;
      }, 1200);
    } finally {
      isSyncingFromServer.value = false;
    }
  }
);

const getGlobalIndex = (dayIndex, placeIndex) => {
  let offset = 0;
  for (let i = 0; i < dayIndex; i++) {
    offset += days.value[i].places.length;
  }
  return offset + placeIndex;
};


/* ✅ 활동 완료 (confirm) */
const completeActivity = async () => {
  if (!activePlace.value) return;

  try {
    // 현재 시간을 endedAt으로 설정
    const endedAt = new Date().toISOString();

    // API 요청 데이터 구성 (백엔드 DTO에 맞춤: camelCase)
    const activityData = {
      planPlaceId: activePlace.value.id || activePlace.value.placeId,
      actualCost: spendInput.value,
      memo: comment.value,
      endedAt: endedAt
    };

    // API 호출
    const response = await plannerApi.saveCurrentActivity(activityData);

    // UI 상태 업데이트
    activePlace.value.isEnded = true;
    activePlace.value.actualCost = spendInput.value;
    activePlace.value.memo = comment.value;

    // 먼저 Vue 반영을 기다린다
    await nextTick();

    // 그 다음에 판단
    if (isCurrentDayEnded.value) {
      const nextDayIndex = selectedDayIndex.value + 1;
      if (nextDayIndex < days.value.length) {
        selectedDayIndex.value = nextDayIndex;
      }
    }

    // NOW 모드로 복귀
    browseIndex.value = null;

    // 모달 닫기
    activityModalOpen.value = false;


    spendInput.value = null;
    comment.value = "";

  } catch (error) {
    console.error('❌ 활동 완료 저장 실패:', error);
    alert('활동 완료 처리에 실패했습니다.\n다시 시도해주세요.');
  }
};

/* ---------- AI 일정 → 화면에 적용하는 함수 ---------- */
const applyAiPlan = async (payload) => {
  console.log("✅ [PlanList] applyAiPlan 호출됨", payload);

  if (!payload) {
    console.log("⚠️ [PlanList] payload가 없음");
    return;
  }

  // if (!payload.days || !Array.isArray(payload.days)) {
  //   console.log("⚠️ [PlanList] payload.days가 없거나 배열이 아님");
  //   return;
  // }

  // plan.value = {
  //   id: payload.planId,
  //   startDate: payload.startDate,
  //   endDate: payload.endDate,
  //   title: payload.title ?? "AI 추천 여행 일정",
  // };

  // days.value = (payload.days || []).map((d) => ({
  //   day: {
  //     id: d.dayIndex,
  //     dayIndex: d.dayIndex,
  //     planDate: d.date,
  //     title: `Day ${d.dayIndex}`,
  //   },
  //   places: (d.schedules || []).map((s) => {
  //     const type = s.normalizedCategory ?? "ETC";

  //     const gallery =
  //       s.firstImage2
  //         ? [s.firstImage2]
  //         : s.firstImage
  //           ? [s.firstImage]
  //           : getDefaultGalleryByType(type);

  //     return {
  //       title: s.title,
  //       startAt: s.startAt,
  //       endAt: s.endAt,
  //       placeName: s.placeName,
  //       address: s.address,
  //       // ✅ status 기본값 (없으면 Pending으로)
  //       status: s.status ?? "PENDING",
  //       details: {
  //         type,
  //         gallery,
  //         desc: `${s.title} 방문을 추천합니다`,
  //         address: s.address,
  //         area: "Seoul",
  //         firstImage: s.firstImage,
  //         firstImage2: s.firstImage2,
  //       },
  //     };
  //   }),
  // }));

  await renderPlan();

  travelStore.setPlanInfo(payload.planId, travelStore.dayIndex, travelStore.planDate);
  // ✅ selectedDayIndex 리셋 제거 - 현재 보고있는 day 유지
  console.log("[PlanList] days 갱신:", days.value);
};

/* ---------- watch ---------- */
watch(selectedDayIndex, () => {
  browseIndex.value = null; // Day 바뀌면 NOW 기준으로 복귀
});



/* ---------- 모달 ---------- */
const openModal = (place) => {
  if (editMode.value) return;

  const type = place?.details?.type ?? "ETC";

  const gallery =
    place?.details?.firstImage
      ? [place.details.firstImage]
      : (place?.details?.gallery?.length ? place.details.gallery : getDefaultGalleryByType(type));

  modalData.value = {
    title: place.title,
    address: place.details?.address,
    area: place.details?.area ?? "Seoul",
    gallery,
    desc: place.details?.desc,
  };

  modalOpen.value = true;
};

/* ---------- ReplaceModal: 삭제 버튼 클릭 시 ---------- */
const onDeletePlace = async (idx, place) => {
  console.log('🔍 삭제하려는 장소:', place);

  // 위치 정보 추출 (여러 경로 시도)
  let lat = place.lat ||
    place.latitude ||
    place.details?.lat ||
    place.details?.latitude ||
    place.mapY;

  let lng = place.lng ||
    place.longitude ||
    place.details?.lng ||
    place.details?.longitude ||
    place.mapX;

  // 숫자로 변환 (문자열일 수 있으므로)
  lat = parseFloat(lat);
  lng = parseFloat(lng);

  console.log('📍 추출된 위치 (변환 후):', { lat, lng, type: `${typeof lat}, ${typeof lng}` });

  let alternatives = [];

  // API 호출해서 근처 카페 가져오기
  if (lat && lng && !isNaN(lat) && !isNaN(lng)) {
    try {
      console.log('🔄 API 호출 시작:', {
        url: '/search-rest-place',
        params: { lat, lng }
      });

      const response = await plannerApi.getRestPlaces(lat, lng);

      console.log('✅ API 응답 전체:', response);
      console.log('✅ API 응답 데이터:', response.data);

      // 응답 데이터를 alternatives 형식으로 변환
      const cafes = response.data.data || response.data || [];

      console.log('📦 변환할 카페 목록:', cafes);

      if (Array.isArray(cafes) && cafes.length > 0) {
        // ⭐ 상위 3개만 선택
        const topThreeCafes = cafes.slice(0, 3);
        console.log(`🎯 상위 3개 선택 (전체 ${cafes.length}개 중):`, topThreeCafes);

        alternatives = topThreeCafes.map(cafe => {
          console.log('🔄 카페 변환 중:', cafe);
          return {
            id: place.id,
            title: cafe.title || cafe.name || cafe.placeName || '카페',
            image: cafe.firstImage2 || cafe.firstImage || cafe.image,
            thumbnail: cafe.firstImage2 || cafe.firstImage || cafe.thumbnail,
            desc: cafe.overview || cafe.desc || `${cafe.title || '카페'}을(를) 추천합니다`,
            shortDesc: cafe.category || '카페',
            address: cafe.address || cafe.addr1,
            mapX: cafe.mapX || cafe.lng || cafe.longitude,
            mapY: cafe.mapY || cafe.lat || cafe.latitude,
            startAt: place.startAt,  // 기존 장소의 시간 유지
            endAt: place.endAt,
            status: "PENDING",
            details: {
              type: 'CAFE',
              gallery: cafe.firstImage2 ? [cafe.firstImage2] : cafe.firstImage ? [cafe.firstImage] : [],
              desc: cafe.overview || cafe.desc || `${cafe.title || '카페'}을(를) 추천합니다`,
              address: cafe.address || cafe.addr1,
              area: 'Seoul'
            }
          };
        });

        console.log('✅ 변환된 alternatives (상위 3개):', alternatives);
      } else {
        console.warn('⚠️ API 응답이 비어있거나 배열이 아닙니다');
        alternatives = currentDayPlaces.value.filter((p, i) => i !== idx).slice(0, 3);
      }
    } catch (error) {
      console.error('❌ API 호출 실패:', error);
      console.error('❌ 에러 응답:', error.response?.data);
      console.error('❌ 에러 상태:', error.response?.status);

      // API 실패 시 기존 로직 사용 (같은 날의 다른 장소들 중 3개)
      alternatives = currentDayPlaces.value.filter((p, i) => i !== idx).slice(0, 3);
      console.warn('⚠️ API 실패로 기존 장소 목록 사용:', alternatives.length, '개');
    }
  } else {
    console.warn('⚠️ 위치 정보가 유효하지 않아 기존 장소로 대체합니다:', { lat, lng });
    // 위치 정보가 없으면 기존 로직 사용 (3개만)
    alternatives = currentDayPlaces.value.filter((p, i) => i !== idx).slice(0, 3);
  }

  replaceTarget.value = place;
  replaceAlternatives.value = alternatives;
  replaceModalOpen.value = true;
};

const applyReplacement = (alt) => {
  const idx = currentDayPlaces.value.findIndex((p) => p === replaceTarget.value);
  console.log("replaceTarget", replaceTarget.value);
  if (idx !== -1) {
    days.value[selectedDayIndex.value].places.splice(idx, 1, alt);
    console.log("----", replaceTarget.value.id, alt);
    plannerApi.updatePlanPlace(replaceTarget.value.id, alt);
  }
  replaceModalOpen.value = false;
  console.log("####", replaceTarget.value.id);
};

const deleteAnyway = () => {
  const idx = currentDayPlaces.value.findIndex((p) => p === replaceTarget.value);
  if (idx !== -1) {
    days.value[selectedDayIndex.value].places.splice(idx, 1);
    plannerApi.deletePlanPlace(replaceTarget.value.id);
  }
  replaceModalOpen.value = false;
};

/* ---------- 기타 ---------- */
const toggleEditMode = () => (editMode.value = !editMode.value);

const highlightedTitle = computed(() => {
  const duration = getTripDuration.value.text;
  return plan.value?.title ?? `서울, ${duration} <span class="highlight">추천일정</span>입니다`;
});

const categoryMap = {
  FOOD: "음식점",
  SPOT: "관광지",
  SHOPPING: "쇼핑",
  CAFE: "카페",
  HOTEL: "숙소",
  EVENT: "이벤트",
  ETC: "공원",
};

const typeColor = (type) => {
  switch (type) {
    case "FOOD":
      return "color-red";
    case "SHOPPING":
      return "color-blue";
    case "CAFE":
      return "color-green";
    case "HOTEL":
      return "color-gray";
    case "SPOT":
      return "color-purple";
    case "EVENT":
      return "color-purple";
    case "ETC":
      return "color-purple";
    default:
      return "color-purple";
  }
};

const typeLabel = (type) => {
  switch (type) {
    case "FOOD":
      return "식사 장소 추천";
    case "SHOPPING":
      return "쇼핑 추천";
    case "CAFE":
      return "카페 추천";
    case "HOTEL":
      return "숙소 이동";
    case "SPOT":
      return "관광지 추천";
    case "EVENT":
      return "이벤트 방문";
    case "ETC":
      return "공원 산책";
    default:
      return null;
  }
};

/* ---------- 서버 플랜 불러오기 ---------- */
const normalizePlaces = (places = []) =>
  places.map((p) => {
    const type = p.normalizedCategory ?? "ETC";

    const gallery =
      p.firstImage2
        ? [p.firstImage2]
        : p.firstImage
          ? [p.firstImage]
          : getDefaultGalleryByType(type);

    return {
      ...p,
      // ✅ status가 없을 수 있어서 기본값 보강
      // status: p.status ?? "PENDING",
      isEnded: !!p.isEnded,
      details: {
        type,
        gallery,
        desc: p.desc ?? `${p.title} 방문 추천`,
        address: p.address,
        area: p.area ?? "Seoul",
      },
    };
  });

const renderPlan = async () => {
  const res = await plannerApi.getActivePlan(authStore.userId);
  const raw = res?.data?.data || {};

  console.log("📥 [PlanList] 서버에서 불러온 계획 데이터:", raw);

  plan.value = raw.plan || null;

  days.value = (raw.days || []).map((d) => ({
    day: d.day,
    places: normalizePlaces(d.places),
  }));

  if (raw?.plan?.id) {
    travelStore.setPlanInfo(raw.plan.id, travelStore.dayIndex, travelStore.planDate);
  }
};

// nowIndex 자동 계산 함수
// function selectNowActivity() {
//   const places = currentDayPlaces.value;
//   if (!places.length) {
//     nowIndex.value = 0;
//     return;
//   }
//   const now = new Date();
//   // 진행 중이거나, 아직 시작 전인 첫 번째 place를 찾음
//   let idx = places.findIndex(p => {
//     const start = p.startAt ? new Date(p.startAt) : null;
//     const end = p.endAt ? new Date(p.endAt) : null;
//     if (start && end) {
//       return now >= start && now <= end;
//     }
//     if (start && now <= start) {
//       return true;
//     }
//     return false;
//   });
//   if (idx === -1) idx = 0;
//   nowIndex.value = idx;
// }

// Day가 바뀔 때마다 nowActivity도 자동 선택
// watch(selectedDayIndex, () => {
//   selectNowActivity();
// });

/* ---------- onMounted ---------- */
onMounted(async () => {
  isLoadingPlan.value = true;
  authStore.initializeAuth();

  const MIN_LOADING_TIME = 600; // ms (400~800 추천)
  const startTime = Date.now();

  try {
    const userId = authStore.userId;

    if (chatStore.livePlanFromChat) {
      applyAiPlan(chatStore.livePlanFromChat.data);
      setTimeout(() => {
        selectToday();
        // selectNowActivity();
      }, 0);
      return;
    }

    if (userId != null) {
      await renderPlan();
      setTimeout(() => {
        selectToday();
        moveToFirstUnfinishedDayFromToday();
      }, 0);
    }
  } finally {
    const elapsed = Date.now() - startTime;
    const remaining = MIN_LOADING_TIME - elapsed;

    if (remaining > 0) {
      setTimeout(() => {
        isLoadingPlan.value = false;
      }, remaining);
    } else {
      isLoadingPlan.value = false;
    }
  }
});


function moveToFirstUnfinishedDayFromToday() {
  // today 기준 day index
  let startIdx = selectedDayIndex.value;

  // 혹시 today가 여행 범위 밖이면 0
  if (startIdx < 0) startIdx = 0;

  for (let i = startIdx; i < days.value.length; i++) {
    const hasUnfinished = days.value[i].places.some(
      p => p.isEnded === false
    );

    if (hasUnfinished) {
      selectedDayIndex.value = i;
      return;
    }
  }
}
// 오늘 날짜에 맞는 Day 자동 선택 함수
function selectToday() {
  if (!plan.value?.startDate || !days.value.length) return;

  const today = new Date();
  const todayYMD = today.toISOString().slice(0, 10); // 'YYYY-MM-DD'

  // plan.startDate ~ plan.endDate 범위 내에 오늘이 있는지 체크
  const startYMD = plan.value.startDate.slice(0, 10);
  const endYMD = plan.value.endDate.slice(0, 10);

  if (todayYMD < startYMD || todayYMD > endYMD) {
    // 오늘이 여행 기간이 아니면 첫째날로
    selectedDayIndex.value = 0;
    return;
  }

  // days에서 오늘 날짜와 일치하는 인덱스 찾기
  const foundIdx = days.value.findIndex(d => {
    if (!d.day?.planDate) return false;
    return d.day.planDate.slice(0, 10) === todayYMD;
  });

  if (foundIdx !== -1) {
    selectedDayIndex.value = foundIdx;
  } else {
    // 혹시 없으면 첫째날로
    selectedDayIndex.value = 0;
  }
}


function getNowIndexByDay(dayIndex) {
  const places = days.value?.[dayIndex]?.places ?? [];
  if (!places.length) return null;

  // 1. 진행 중이 있으면 → false 중 가장 앞
  const firstUnfinished = places.findIndex(p => p.isEnded === false);
  if (firstUnfinished !== -1) return firstUnfinished;

  // 2. 전부 완료면 → 마지막
  return places.length - 1;
}




/* ---------- navigation ---------- */
const onNext = () => {
  if (travelStore.$state.isTraveling) endplan();
  else goToSummary();
};

const onBack = () => router.back();

const goToSummary = () => router.push("/planner/summary");
const endplan = async () => {
  const planId = travelStore.planId;

  if (!planId) {
    console.warn('❌ planId가 없습니다');
    return;
  }

  try {
    // 여행 종료 API 호출
    await plannerApi.saveEndTravel(planId);
    console.log('✅ 여행 종료 완료');

    // 스토어 상태 초기화
    travelStore.endTravel();
    travelStore.clearPlanInfo();

    // 메인 페이지로 리다이렉트
    router.push('/');
  } catch (error) {
    console.error('❌ 여행 종료 실패:', error);
    alert('여행 종료 처리에 실패했습니다.\n다시 시도해주세요.');
  }
};

watch(globalActiveIndex, (newGlobalIndex) => {
  if (newGlobalIndex === -1) return;

  let offset = 0;

  for (let dayIdx = 0; dayIdx < days.value.length; dayIdx++) {
    const dayPlaces = days.value[dayIdx].places;

    if (newGlobalIndex < offset + dayPlaces.length) {
      // 🔥 현재 선택된 day와 다르면 이동
      if (selectedDayIndex.value !== dayIdx) {
        selectedDayIndex.value = dayIdx;
      }
      return;
    }

    offset += dayPlaces.length;
  }
});

</script>

<style scoped>
/* ========================================
   � PlanList - 랜딩페이지 스타일 매칭
   ======================================== */
.plan-root {
  height: 100%;
  display: flex;
  flex-direction: column;
  font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, "Helvetica Neue", Arial, "Noto Sans KR", sans-serif;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
  /* background: #ffffff; */
  /* border: 1px solid #f1f5f9 !important; */
  /* box-shadow: 0 1px 3px rgba(0, 0, 0, 0.06) !important; */
  /* border-radius: 16px; */
  overflow: hidden;
}

/* 헤더 영역 스타일 개선 */
.plan-root>div:first-child {
  background: #ffffff;
  color: #2d4a8f;
  border-bottom: 1px solid #e2e8f0 !important;
  height: 100px;
  display: flex;
  align-items: center;
  padding: 1.25rem 2rem !important;
  position: relative;
}

.plan-root h5.title {
  font-size: 1.15rem;
  font-weight: 700;
  color: #1e293b !important;
  letter-spacing: -0.02em;
  margin-bottom: 0.25rem;
}

.plan-root .sub {
  color: #64748b !important;
  font-size: 0.8rem;
  font-weight: 500;
}

.plan-root .rounded-3 {
  background: #f8fafc !important;
  border: 1px solid #e2e8f0;
  font-size: 1.25rem;
  width: 38px !important;
  height: 38px !important;
}

/* 일정 본문 스크롤 */
.plan-body-scroll {
  flex: 1;
  overflow-y: auto;
  scrollbar-width: thin;
  scrollbar-color: #cbd5e1 #ffffff;
  background: #ffffff;
}

/* 웹킷 브라우저 스크롤바 */
.plan-body-scroll::-webkit-scrollbar {
  width: 6px;
}

.plan-body-scroll::-webkit-scrollbar-track {
  background: #ffffff;
  border-radius: 3px;
}

.plan-body-scroll::-webkit-scrollbar-thumb {
  background: #cbd5e1;
  border-radius: 3px;
  transition: background 0.3s ease;
}

.plan-body-scroll::-webkit-scrollbar-thumb:hover {
  background: #94a3b8;
}

/* Edit 버튼 스타일 개선 */
.edit-btn-large {
  padding: 0.65rem 1.8rem;
  font-size: 0.95rem;
  font-weight: 600;
  height: auto;
  border-radius: 12px;
  background: #ffffff;
  color: #2d4a8f;
  border: 2px solid #e2e8f0;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  letter-spacing: -0.01em;
}

.edit-btn-large:hover {
  background: #2d4a8f;
  color: white;
  border-color: #2d4a8f;
  transform: translateY(-1px);
}

/* 제목 highlight */
:deep(.highlight) {
  font-weight: 700;
  color: #2d4a8f;
}

/* CTA 영역 스타일 */
.plan-root>.border-top {
  background: #ffffff !important;
  border-top: 1px solid #e2e8f0 !important;
}

/* 편집 버튼 영역 */
.plan-root>div:has(.edit-btn-large) {
  background: #ffffff;
  padding: 1.5rem 2rem 1rem !important;
  border-bottom: 1px solid #f1f5f9;
}

/* 전체적인 색상 테마 - 남색 */
:deep(.text-muted) {
  color: #64748b !important;
}

:deep(.bg-secondary-subtle) {
  background-color: #f1f5f9 !important;
}

/* Bootstrap primary 색상을 남색으로 오버라이드 */
:deep(.btn-primary) {
  background-color: #2d4a8f !important;
  border-color: #2d4a8f !important;
  color: white !important;
}

:deep(.btn-primary:hover) {
  background-color: #1a2a56 !important;
  border-color: #1a2a56 !important;
}

:deep(.btn-outline-primary) {
  color: #2d4a8f !important;
  border-color: #2d4a8f !important;
}

:deep(.btn-outline-primary:hover),
:deep(.btn-outline-primary.active) {
  background-color: #2d4a8f !important;
  border-color: #2d4a8f !important;
  color: white !important;
}

:deep(.text-primary) {
  color: #2d4a8f !important;
}

/* 카드 스타일 통일 */
:deep(.card) {
  border: 1px solid #f1f5f9;
  border-radius: 16px;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  background: white;
}

:deep(.card:hover) {
  border-color: #e2e8f0;
  transform: translateY(-2px);
}

/* 타이포그래피 개선 */
:deep(h5),
:deep(h6) {
  font-weight: 600;
  color: #1e293b;
  letter-spacing: -0.02em;
}

:deep(p) {
  color: #64748b;
  line-height: 1.6;
}

:deep(.small) {
  font-size: 0.875rem;
  color: #94a3b8;
}

/* 로딩 ↔ 콘텐츠 전환 */
.fade-slide-enter-active,
.fade-slide-leave-active {
  transition: opacity 0.35s ease, transform 0.35s ease;
}

/* 콘텐츠 등장 */
.fade-slide-enter-from {
  opacity: 0;
  transform: translateY(10px);
}

/* 이전 화면 퇴장 */
.fade-slide-leave-to {
  opacity: 0;
  transform: translateY(-10px);
}

.sync-banner {
  background: #eef2ff;
  color: #2d4a8f;
  font-size: 0.85rem;
  padding: 6px 12px;
  text-align: center;
  border-bottom: 1px solid #e0e7ff;
}

.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.25s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}

@keyframes serverHighlight {
  0% {
    background-color: #eef2ff;
  }

  100% {
    background-color: transparent;
  }
}

.server-updated {
  animation: serverHighlight 1.2s ease;
}
</style>
