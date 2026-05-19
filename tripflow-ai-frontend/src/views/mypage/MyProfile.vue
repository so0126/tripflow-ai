<template>
  <!-- Header -->
    <div class="p-4 pb-3 border-bottom d-flex align-items-center justify-content-between">
      <div class="d-flex gap-3 align-items-center">
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
    <div class="position-relative d-inline-block">
      <img v-if="profileImage" :src="profileImage" alt="Profile"
      class="rounded-circle shadow-sm border border-4 border-white"
          style="width: 140px; height: 140px; object-fit: cover;" />
        <div v-else
        class="rounded-circle shadow-sm border border-4 border-white bg-light d-flex align-items-center justify-content-center"
          style="width: 140px; height: 140px;">
          <i class="bi bi-person text-secondary" style="font-size: 4rem;"></i>
        </div>

        <button @click="goToEditProfile"
        class="btn btn-sm btn-light position-absolute bottom-0 end-0 rounded-circle shadow-sm border"
          style="width: 40px; height: 40px;">
          <i class="bi bi-pencil-fill text-primary"></i>
        </button>
      </div>

      <div class="mt-3">
        <h2 class="mb-1">{{ profileData.name }}</h2>
        <p class="text-muted fs-5 m-0">{{ profileData.email }}</p>
      </div>
    </div>

    <BaseSection icon="bi-person-vcard" title="Basic Info" subtitle="기본 정보">
      <div class="row g-4">
        <div class="col-6">
          <h4 class="text-secondary mb-1">Korean Name</h4>
          <p class="fs-5 border-bottom pb-2">{{ profileData.koreanName || '-' }}</p>
        </div>
        <div class="col-6">
          <h4 class="text-secondary mb-1">Nationality</h4>
          <p class="fs-5 border-bottom pb-2">{{ profileData.nationality || '-' }}</p>
        </div>
      </div>
    </BaseSection>

    <BaseSection icon="bi-airplane" title="Travel Style" subtitle="나의 여행 스타일">
      <div class="mb-4">
        <h4 class="text-secondary mb-1">Currency</h4>
        <p class="fs-5">{{ getCurrencyLabel(profileData.preferredCurrency) }}</p>
      </div>

      <h4 class="text-secondary mb-3">Interests</h4>
      <div class="d-flex flex-wrap gap-2">
        <div v-for="interest in availableInterests" :key="interest.id" class="interest-chip"
          :class="{ 'active': profileData.interests.includes(interest.id) }">
          <i :class="interest.icon"></i>
          <span class="ms-2">{{ interest.name }}</span>
        </div>
      </div>
    </BaseSection>

    <BaseSection icon="bi-building-check" title="Reservations" subtitle="호텔 예약 내역">
      <div v-if="profileData.reservations.length > 0" class="d-flex flex-column gap-3">
        <div v-for="(reservation, index) in profileData.reservations" :key="index" class="custom-card p-3">
          <div class="d-flex justify-content-between align-items-start mb-2">
            <div>
              <h4 class="m-0 text-primary">{{ reservation.hotelName }}</h4>
              <p class="text-muted m-0 fs-6">{{ reservation.location }}</p>
            </div>
            <span class="badge rounded-pill fw-normal"
              :class="reservation.status === 'Confirmed' ? 'bg-success' : 'bg-warning text-dark'">
              {{ reservation.status }}
            </span>
          </div>

          <div class="d-flex gap-4 mt-2 text-secondary fs-6">
            <div>
              <i class="bi bi-calendar-check me-1"></i> Check-in : {{ reservation.checkIn }}
            </div>
            <div>
              <i class="bi bi-moon-stars me-1"></i> {{ reservation.nights }} Nights
            </div>
          </div>
        </div>
      </div>
      <p v-else class="text-muted text-center py-3">예약된 내역이 없습니다.</p>
    </BaseSection>

    <BaseSection icon="bi-wallet2" title="Payment" subtitle="결제 정보">
      <h4 class="text-secondary mb-3">Payment Methods</h4>
      <div v-if="profileData.paymentMethods.length > 0" class="d-flex flex-column gap-2 mb-4">
        <div v-for="(card, index) in profileData.paymentMethods" :key="index"
          class="custom-card p-3 d-flex align-items-center justify-content-between">
          <div class="d-flex align-items-center gap-3">
            <i class="bi bi-credit-card-2-front fs-2 text-primary"></i>
            <div>
              <h5 class="m-0">{{ card.type }} <span class="fs-6 text-muted">**** {{ card.lastFour }}</span></h5>
              <p class="m-0 text-muted fs-6">Expires {{ card.expiry }}</p>
            </div>
          </div>
          <span v-if="card.isDefault" class="badge bg-primary rounded-pill fw-normal">Default</span>
        </div>
      </div>
      <p v-else class="text-muted mb-4">등록된 카드가 없습니다.</p>
    </BaseSection>

    <BaseSection icon="bi-heart-pulse" title="Health" subtitle="건강 정보">
      
      <h4 class="text-secondary mb-2">Medicine info</h4>
      <div class="p-3 bg-light rounded-3">
        <div class="mb-2">
          <span class="fw-bold me-2" style="color: #ff8c00;">Allergies:</span>
          <span>{{ profileData.medicalInfo.allergies || 'None' }}</span>
        </div>
        <div>
          <span class="fw-bold me-2" style="color: #ff8c00;">Dietary:</span>
          <span>{{ profileData.medicalInfo.dietaryRestrictions || 'None' }}</span>
        </div>
      </div>
      

    </BaseSection>
    <FilledButton
      size="lg"
      :disabled="isLoading"
      @click="goToEditProfile"
    >
      프로필 편집
    </FilledButton>
  </div>

</div>
</template>

<script setup>
import { reactive, onMounted, computed, ref } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/store/authStore'
// import BackButtonPageHeader from '@/components/common/BackButtonPageHeader.vue'
import BaseSection from '@/components/common/BaseSection.vue'
import FilledButton from '@/components/common/button/FilledButton.vue'

const router = useRouter()
const authStore = useAuthStore()

// ... 데이터 로직은 기존과 동일하게 유지 ...
// (스크립트 부분은 기존 로직이 잘 짜여져 있어서, 
//  불필요한 props나 import만 정리하고 그대로 쓰시면 됩니다.)
const isLoading = ref(false) //
const profileData = reactive({
  name: 'John Doe',
  koreanName: '도우존',
  email: 'john.doe@gmail.com',
  nationality: 'United States',
  preferredCurrency: 'USD',
  interests: ['culture', 'food', 'adventure'],
  reservations: [
    { hotelName: 'The Shilla Seoul', location: 'Jung-gu, Seoul', checkIn: '2024-10-15', checkOut: '2024-10-18', nights: 3, status: 'Confirmed' },
    { hotelName: 'Four Seasons Busan', location: 'Haeundae-gu, Busan', checkIn: '2024-11-01', checkOut: '2024-11-03', nights: 2, status: 'Pending' }
  ],
  paymentMethods: [
    { type: 'Visa', lastFour: '4567', expiry: '12/25', isDefault: true },
    { type: 'Mastercard', lastFour: '8901', expiry: '08/26', isDefault: false }
  ],
  medicalInfo: { allergies: 'Shellfish, Peanuts', dietaryRestrictions: 'Vegetarian, Gluten-free' },
  instagramConnected: true
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

const getCurrencyLabel = (code) => {
  const currencies = { 'USD': 'USD ($)', 'KRW': 'KRW (₩)', 'EUR': 'EUR (€)', 'JPY': 'JPY (¥)' }
  return currencies[code] || code
}

const goBack = () => router.push({name: 'MyProfile'});
const goToEditProfile = () => router.push({name: 'EditProfile'});

const profileImage = computed(() => {
  return authStore.userProfileImage || new URL('@/assets/img/profile-logo.png', import.meta.url).href
})

onMounted(() => {
  authStore.initializeAuth()
  if (authStore.user) {
    profileData.name = authStore.user.name || 'User'
    profileData.email = authStore.user.email || ''
  }
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

/* 취향 태그 (Chips) */
.interest-chip {
  padding: 0.5rem 1rem;
  border-radius: 50px;
  /* 둥글게 */
  border: 1px solid #dee2e6;
  color: #6c757d;
  font-size: 0.95rem;
  /* 본문 폰트보다 약간 작게 */
  display: flex;
  align-items: center;
  transition: all 0.2s;
}

/* 선택된 취향 태그 */
.interest-chip.active {
  background-color: #ff8c00;
  /* Primary Orange */
  border-color: #ff8c00;
  color: white;
  font-weight: normal;
  /* Parkdahyun은 bold보다 normal이 예쁨 */
}

/* 카드 스타일 (예약, 결제수단 등) */
.custom-card {
  background: #ffffff;
  border: 1px solid rgba(0, 0, 0, 0.08);
  border-radius: 1rem;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.02);
  transition: transform 0.2s ease;
}

.custom-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
}

/* 폰트 및 텍스트 헬퍼 (글로벌과 충돌하지 않도록) */
h4 {
  font-size: 1.1rem;
  /* 제목 라벨 크기 조절 */
}
</style>