<template>
    <div class="planner-page">
    <div class="plan-summary container py-5">

        <!-- 상단 인사 영역 -->
        <div class="text-center mb-4">
            <div class="hero-icon mx-auto mb-3 d-inline-flex align-items-center justify-content-center">
                ✈️
            </div>
            <h2 class="fw-bold mb-1">여행 일정이 준비되었어요</h2>
            <p class="text-muted mb-0">다음 단계로 이동하기 전에 여행 일정을 한눈에 확인해보세요</p>
        </div>

        <div class="row justify-content-center">
            <div class="col-lg-10">

                <!-- 여행 요약 카드 -->
                <div class="card mb-4 shadow-sm border-0">
                    <div class="card-header bg-primary text-white border-0 rounded-top">
                        <h5 class="mb-0 d-flex align-items-center gap-2">
                            <span class="header-title">🧳 여행 개요</span>
                            <small class="badge bg-light text-primary-emphasis ms-auto">
                                {{ duration }}일
                            </small>
                        </h5>
                    </div>

                    <div class="card-body">

                        <!-- 제목 & 날짜 -->
                        <div class="d-flex flex-wrap align-items-center justify-content-between mb-3">
                            <div>
                                <h5 class="mb-1">{{ planTitle }}</h5>
                                <p class="text-muted small mb-0">{{ dateRangeText }} • 서울</p>
                            </div>

                            <div class="summary-metrics d-flex gap-3 mt-3 mt-sm-0">
                                <div class="metric-pill">
                                    <span class="label">일정 </span>
                                    <span class="value">{{ days.length }}</span>
                                </div>
                                <div class="metric-pill">
                                    <span class="label">장소 </span>
                                    <span class="value">{{ totalPlaces }}</span>
                                </div>
                                <div class="metric-pill" v-if="totalBudget">
                                    <span class="label">예산</span>
                                    <span class="value">{{ formattedBudget }}</span>
                                </div>
                            </div>
                        </div>
                        <hr />

                        <!-- 날짜별 요약 -->
                        <div class="day-list">
                            <div v-for="(day, idx) in days" :key="day.day || idx" class="day-row">
                                <div class="day-badge">Day {{ idx + 1 }}</div>

                                <div class="flex-grow-1">
                                    <div class="day-tagline text-muted small mb-1">
                                        {{ dayTagline(day, idx) }}
                                    </div>

                                    <div class="day-places">
                                        <span v-for="(place, pIdx) in (day.places || []).slice(0, 3)" :key="pIdx"
                                            class="badge rounded-pill place-badge">
                                            {{ place.title }}
                                        </span>

                                        <span v-if="(day.places || []).length > 3"
                                            class="badge rounded-pill more-badge">
                                            +{{ (day.places || []).length - 3 }} 개
                                        </span>

                                        <span v-if="!(day.places && day.places.length)"
                                            class="text-muted small fst-italic">
                                            추가된 일정이 없어요
                                        </span>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>

                <!-- ★ 위치 교체: Next Step이 아래, Before You Go가 위가 아니라,
             너가 요청한 대로 '컴포넌트 자체 위치'를 서로 바꾸어 배치함 -->

                <!-- NEXT STEP & BEFORE YOU GO (좌우 교체됨) -->
                <div class="row g-4 mb-4 align-items-stretch">

                    <!-- BEFORE YOU GO -->
                    <div class="col-lg-5 d-flex">
                        <div class="card shadow-sm border-0 flex-fill d-flex flex-column card-step">

                            <div>
                                <h4 class="card-step-title">일정을 수정하고 싶나요?</h4>
                                <p class="card-step-text">
                                    일정 순서나 장소를 수정하고 싶다면, 
                                    아래 버튼을 눌러주세요.
                                </p>
                            </div>

                            <div class="card-step-footer">
                                <button class="btn btn-outline-secondary w-100" @click="goBackToEdit">
                                    일정 수정하기
                                </button>
                            </div>

                        </div>
                    </div>

                    <!-- NEXT STEP -->
                    <div class="col-lg-7 d-flex">
                        <div class="card shadow-sm border-0 flex-fill d-flex flex-column card-step">

                            <div>
                                <h4 class="card-step-title">다음 단계: 숙소 선택</h4>
                                <p class="card-step-text">
                                    AI가 여행 동선과 일정 밀도를 분석해서<br />
                                    가장 효율적인 숙소 후보를 추천해드릴게요.
                                </p>
                            </div>

                            <div class="card-step-footer">
                                <button class="btn btn-primary w-100" @click="goToHotel">
                                    숙소 선택하러 가기 →
                                </button>
                            </div>
                        </div>
                    </div>
                </div>
            </div>

            <!-- 안내 문구 -->
            <p class="text-muted small mt-3 d-flex align-items-center">
                <i class="bi bi-info-circle me-2"></i>
                이 요약 화면은 Travelgram에서도 나중에 다시 확인할 수 있어요.
            </p>

        </div>
    </div>
    </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import plannerApi from '@/api/plannerApi'
import { useAuthStore } from '@/store/authStore'

const router = useRouter()
const authStore = useAuthStore()

const plan = ref(null)
const days = ref([])

// 초기 데이터 로딩
const loadPlan = async () => {
    await authStore.initializeAuth?.()
    const res = await plannerApi.getActivePlan(authStore.userId)
    const raw = res?.data?.data || {}
    plan.value = raw.plan || null
    days.value = raw.days || []
}

onMounted(() => {
    loadPlan()
})

// computed
const planTitle = computed(
    () =>
        plan.value?.title ||
        'Your Seoul itinerary'
)

const duration = computed(() => {
    if (plan.value?.durationDays) return plan.value.durationDays
    if (plan.value?.startDate && plan.value?.endDate) {
        const s = new Date(plan.value.startDate)
        const e = new Date(plan.value.endDate)
        if (!isNaN(s) && !isNaN(e)) {
            const diff = (e - s) / (1000 * 60 * 60 * 24) + 1
            return diff > 0 ? diff : days.value.length || 0
        }
    }
    return days.value.length || 0
})

const dateRangeText = computed(() => {
    if (plan.value?.startDate && plan.value?.endDate) {
        return `${plan.value.startDate} ~ ${plan.value.endDate}`
    }
    return 'Dates not set'
})

const totalPlaces = computed(() =>
    (days.value || []).reduce(
        (sum, d) => sum + ((d.places && d.places.length) || 0),
        0
    )
)

// 예: plan에 budget / currency가 있다면 사용, 없으면 null
const totalBudget = computed(() => plan.value?.budget || null)
const currency = computed(() => plan.value?.currency || 'KRW')

const formattedBudget = computed(() => {
    if (!totalBudget.value) return ''
    if (currency.value === 'KRW') {
        return `₩${Math.round(totalBudget.value).toLocaleString()}`
    }
    return `${currency.value} ${Number(totalBudget.value).toLocaleString()}`
})

const dayTagline = (day, idx) => {
    const count = (day.places && day.places.length) || 0
    if (count === 0) return `No fixed plan yet for Day ${idx + 1}`
    if (count === 1) return `1 main spot planned`
    if (count <= 3) return `${count} spots planned`
    return `${count} spots planned · packed day`
}

// navigation
const goToHotel = () => {
    router.push('/planner/hotel')
}

const goBackToEdit = () => {
    router.push('/planner/edit')
}
</script>

<style scoped>
.planner-page {
  background-color: #ffffff;
  min-height: 100vh;
  padding-bottom: 6rem;
  padding: 2rem 1.25rem 6rem;
}
.plan-summary {
    max-width: 980px;
    margin: 0 auto;

    .hero-icon {
        width: 72px;
        height: 72px;
        border-radius: 24px;
        background: #e3f2fd;
        font-size: 2rem;
        box-shadow: 0 2px 8px rgba(45, 74, 143, 0.1);
    }

    .card-header.bg-primary {
        background: #2d4a8f !important;
        border-top-left-radius: .75rem;
        border-top-right-radius: .75rem;
        color: white;
    }

    .header-title {
        color: white !important;
    }

    .btn-primary {
        background-color: #2d4a8f !important;
        border-color: #2d4a8f !important;
        color: white;
    }

    .btn-primary:hover {
        background-color: #1a2a56 !important;
        border-color: #1a2a56 !important;
    }

    .btn-outline-secondary {
        border-radius: .6rem;
        color: #64748b;
        border-color: #cbd5e1;
    }

    .btn-outline-secondary:hover {
        background-color: #f8fafc;
        border-color: #94a3b8;
        color: #475569;
    }

    .summary-metrics .metric-pill {
        background: #e2e8f0;
        border-radius: 999px;
        padding: 8px 16px;
        display: flex;
        align-items: center;
        gap: 4px;

        .label {
            font-size: .85rem;
            color: #64748b;
            font-weight: 600;
        }

        .value {
            font-size: 1.15rem;
            font-weight: 700;
            color: #64748b;
        }
    }

    .day-row {
        display: flex;
        gap: 12px;
        padding: 10px 0;
        border-bottom: 1px solid #e2e8f0;
    }

    /* Day Badge 텍스트 완전 중앙 정렬 */
    .day-badge {
        padding: 6px 10px;
        border-radius: 999px;
        background: #e3f2fd;
        color: #2d4a8f;
        font-weight: 600;
        font-size: .85rem;

        /* 추가 */
        display: flex;
        align-items: center;
        justify-content: center;
    }

    .place-badge {
        background: #fff1db;
        color: #b16200;
        font-size: .9rem;
        font-weight: 500;
        margin-right: 6px;
        margin-bottom: 6px;
    }

    .more-badge {
        background: #e2e8f0;
        color: #64748b;
        font-size: .9rem;
        margin-bottom: 6px;
    }

    .day-places {
        display: flex;
        flex-wrap: wrap;
        gap: 6px;
    }

    /* 두 카드(NEXT STEP, BEFORE YOU GO)의 Typography 통일 */
    .card-step-title {
        font-weight: 600;
        margin-bottom: .75rem;
        font-size: 1.15rem;
        color: #1e293b;
    }

    .card-step-text {
        line-height: 1.6;
        color: #64748b;
        margin-bottom: 1rem;
        font-size: .95rem;
    }

    /* 카드 전체 간격을 통일 */
    .card-step {
        padding: 28px;
        border-radius: 12px;
    }

    /* 버튼을 카드 하단 동일 위치에 */
    .card-step-footer {
        margin-top: auto;
        padding-top: 14px;
        display: flex;
        justify-content: center;
    }
}

/* 전역 텍스트 가독성 개선 */
:deep(.text-muted) {
    color: #64748b !important;
}

:deep(h2) {
    color: #1e293b;
}

:deep(h5) {
    color: #1e293b;
}

:deep(.badge.bg-light) {
    background-color: white !important;
    color: #2d4a8f !important;
}
</style>
