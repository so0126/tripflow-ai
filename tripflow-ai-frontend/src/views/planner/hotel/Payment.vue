<template>
  <div class="planner-page">
    <!-- Header -->
    <div class="hotel-header border-bottom bg-white">
      <div class="container">
        <div class="d-flex gap-3 align-items-center" style="padding: 1.5rem 0;">
          <div class="rounded-3 bg-secondary-subtle d-flex align-items-center justify-content-center"
            style="width: 46px; height: 46px">
            💳
          </div>

          <div>
            <h5 class="mb-1 title">결제 및 예약</h5>
            <p class="text-muted small mb-0 sub">
              예약 정보를 확인하고 결제를 완료하세요
            </p>
          </div>
        </div>
      </div>
    </div>

  <div class="hotel-payment container py-4">

    <!-- 선택한 호텔 요약 -->
    <BaseSection icon="bi-building" title="예약 정보 요약">
      <div class="card-body">
        <div class="row mb-3">
          <div class="col-md-4">
            <img :src="selectedHotel?.image" class="img-fluid rounded" :alt="selectedHotel?.name"
              style="object-fit: cover; height: 200px; width: 100%;" />
          </div>
          <div class="col-md-8">
            <h5 class="card-title hotel-name">{{ selectedHotel?.name }}</h5>
            <p class="card-text text-muted hotel-location">
              <i class="bi bi-geo-alt"></i>
              {{ selectedHotel?.location }}
            </p>
            <p class="card-text text-muted hotel-room-type">
              <i class="bi bi-door-closed"></i>
              {{ selectedHotel?.roomType }}
            </p>
            <div class="mb-3 hotel-dates">
              <div><i class="bi bi-calendar-check"></i> {{ selectedHotel?.checkInDate }} ~ {{ selectedHotel?.checkOutDate }}</div>
              <div><i class="bi bi-moon"></i> {{ extractNumber(selectedHotel?.nights) }}박 | <i class="bi bi-people"></i> {{ selectedHotel?.guests }}</div>
            </div>
            <div class="mb-3 amenities">
              <span class="badge bg-light text-secondary me-2" v-if="selectedHotel?.freeWifi">
                <i class="bi bi-wifi me-1"></i> 무료 와이파이
              </span>
              <span class="badge bg-light text-secondary me-2" v-if="selectedHotel?.breakfast">
                <i class="bi bi-cup-hot me-1"></i> 조식 포함
              </span>
              <span class="badge bg-light text-secondary me-2" v-if="selectedHotel?.pool">
                <i class="bi bi-water me-1"></i> 수영장
              </span>
              <span class="badge bg-light text-secondary" v-if="selectedHotel?.spa">
                <i class="bi bi-heart-pulse me-1"></i> 스파
              </span>
            </div>
            <div class="rating">
              <i class="bi bi-star-fill text-warning"></i>
              <span class="ms-1 rating-score">{{ selectedHotel?.rating }}</span>
              <span class="text-muted rating-reviews">({{ selectedHotel?.reviews }}개의 리뷰)</span>
            </div>
          </div>
        </div>
      </div>
    </BaseSection>

    <!-- 가격 정보 -->
    <BaseSection icon="bi-receipt" title="요금 상세">
      <div class="card-body">
        <div class="d-flex justify-content-between mb-2 price-row">
          <span class="price-label">{{ extractNumber(selectedHotel?.nights) }}박 × ₩{{ formatPrice(selectedHotel?.price) }}/박</span>
          <span class="fw-bold price-value">₩{{ formatPrice(roomPrice) }}</span>
        </div>
        <div class="d-flex justify-content-between mb-2 price-row">
          <span class="price-label">세금 및 수수료 (15%)</span>
          <span class="fw-bold price-value">₩{{ formatPrice(taxFee) }}</span>
        </div>
        <hr />
        <div class="d-flex justify-content-between total-row">
          <span class="fw-bold total-label">총 결제 금액</span>
          <span class="fw-bold text-primary total-amount">₩{{ formatPrice(finalTotal) }}</span>
        </div>
      </div>
    </BaseSection>

    <!-- 결제수단 선택 -->
    <BaseSection icon="bi-credit-card-2-front" title="결제 방법 선택">
      <div class="row g-3">
        <div class="col-6">
          <div class="payment-item" :class="{ active: paymentMethod === 'creditCard' }"
            @click="paymentMethod = 'creditCard'">
            <i class="bi bi-credit-card payment-icon"></i>
            <span class="payment-text">신용카드</span>
          </div>
        </div>
        <div class="col-6">
          <div class="payment-item" :class="{ active: paymentMethod === 'debitCard' }"
            @click="paymentMethod = 'debitCard'">
            <i class="bi bi-credit-card payment-icon"></i>
            <span class="payment-text">체크카드</span>
          </div>
        </div>
        <div class="col-6">
          <div class="payment-item" :class="{ active: paymentMethod === 'bankTransfer' }"
            @click="paymentMethod = 'bankTransfer'">
            <i class="bi bi-bank payment-icon"></i>
            <span class="payment-text">계좌이체</span>
          </div>
        </div>
        <div class="col-6">
          <div class="payment-item" :class="{ active: paymentMethod === 'paypal' }"
            @click="paymentMethod = 'paypal'">
            <i class="bi bi-cash-coin payment-icon"></i>
            <span class="payment-text">페이팔</span>
          </div>
        </div>
      </div>
    </BaseSection>

    <!-- 카드 정보 입력 -->
    <BaseSection
      v-if="paymentMethod === 'creditCard' || paymentMethod === 'debitCard'"
      icon="bi-credit-card-2-back"
      title="카드 정보 입력"
    >
      <div class="mb-3">
        <label for="cardName" class="form-label card-label">카드 소유자 이름</label>
        <input type="text" class="form-control rounded-pill card-input" id="cardName" v-model="cardDetails.name" placeholder="이름 입력" />
      </div>

      <div class="mb-3">
        <label for="cardNumber" class="form-label card-label">카드 번호</label>
        <input type="text" class="form-control rounded-pill card-input" id="cardNumber" v-model="cardDetails.number"
          placeholder="1234 5678 9012 3456" maxlength="19" />
      </div>

      <div class="row">
        <div class="col-md-6 mb-3">
          <label for="expiry" class="form-label card-label">유효기간</label>
          <input type="text" class="form-control rounded-pill card-input" id="expiry" v-model="cardDetails.expiry"
            placeholder="MM/YY" maxlength="5" />
        </div>
        <div class="col-md-6">
          <label for="cvv" class="form-label card-label">CVV 번호</label>
          <input type="text" class="form-control rounded-pill card-input" id="cvv" v-model="cardDetails.cvv"
            placeholder="123" maxlength="3" />
        </div>
      </div>
    </BaseSection>

    <!-- 약관 동의 -->
    <div class="card mb-4">
      <div class="card-body">
        <div class="form-check">
          <input class="form-check-input" type="checkbox" id="agreement" v-model="agreeToTerms" />
          <label class="form-check-label agreement-label" for="agreement">
            예약 조건 및 취소 정책에 동의합니다.
          </label>
        </div>
      </div>
    </div>

    <NavigationButtons
  back-text="뒤로가기"
  :is-next-disabled="!agreeToTerms || isProcessing"
  @back="goBack"
  @next="processPayment"
>
  <template #next-content>
    <span v-if="!isProcessing">
      ₩{{ formatPrice(finalTotal) }} 결제하기
    </span>

    <span v-else class="d-flex align-items-center justify-content-center">
      <span
        class="spinner-border spinner-border-sm me-2"
        role="status"
        aria-hidden="true"
      ></span>
      결제 처리 중...
    </span>
  </template>
</NavigationButtons>
  </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { useRouter, useRoute } from 'vue-router';
import { useTravelStore } from '@/store/travelStore';
import { useAuthStore } from '@/store/authStore';
import NavigationButtons from '@/components/common/button/NavigationButtons.vue'
import BaseSection from '@/components/common/BaseSection.vue';
import hotelApi from '@/api/hotelApi';

const route = useRoute();
const router = useRouter();
const travelStore = useTravelStore();
const authStore = useAuthStore();

// ✅ 모든 ref는 초기값을 명확하게
const selectedHotel = ref(null);
const roomPrice = ref(0);
const taxFee = ref(0);
const finalTotal = ref(0);
const paymentMethod = ref('creditCard');
const cardDetails = ref({
  name: '',
  number: '',
  expiry: '',
  cvv: ''
});
const agreeToTerms = ref(false);
const isProcessing = ref(false);

// ✅ 숫자만 추출하는 함수
const extractNumber = (value) => {
  if (!value) return 0;
  const num = Number(String(value).replace(/[^0-9]/g, ''));
  return num || 0;
};

// ✅ 가격 포맷팅 함수
const formatPrice = (price) => {
  if (!price || price === 0 || isNaN(price)) {
    return '0';
  }
  return Number(price).toLocaleString();
};

// ✅ 날짜를 YYYY-MM-DD 형식으로 변환
const formatDateToYYYYMMDD = (dateString) => {
  if (!dateString) return null;
  return dateString.split('T')[0];
};

// ✅ 가격 계산 함수
const calculatePrices = () => {
  if (!selectedHotel.value) return;

  const price = Number(selectedHotel.value.price) || 0;
  const nights = extractNumber(selectedHotel.value.nights);

  roomPrice.value = price * nights;
  taxFee.value = Math.ceil(roomPrice.value * 0.15);
  finalTotal.value = roomPrice.value + taxFee.value;

  console.log('💰 가격 계산:', {
    price,
    nights,
    roomPrice: roomPrice.value,
    taxFee: taxFee.value,
    finalTotal: finalTotal.value
  });
};

// ✅ 마운트 시 호텔 정보 받기
onMounted(() => {
  console.log('🔍 Payment 페이지 로드');
  console.log('route.query:', route.query);

  if (route.query.hotel) {
    try {
      selectedHotel.value = JSON.parse(route.query.hotel);
      console.log('✅ 호텔 정보 로드 성공:', selectedHotel.value);
      console.log('price:', selectedHotel.value.price);
      console.log('nights:', selectedHotel.value.nights);
      calculatePrices();
    } catch (error) {
      console.error('❌ 호텔 정보 파싱 실패:', error);
      alert('호텔 정보를 불러올 수 없습니다.');
      router.push({ name: 'hotel' });
    }
  } else {
    console.warn('❌ 호텔 정보 없음');
    alert('호텔을 선택해주세요.');
    router.push({ name: 'hotel' });
  }
});

const goBack = () => {
  router.push({ name: 'hotel' });
};

const validateCardDetails = () => {
  if (paymentMethod.value === 'creditCard' || paymentMethod.value === 'debitCard') {
    return (
      cardDetails.value.name &&
      cardDetails.value.number &&
      cardDetails.value.expiry &&
      cardDetails.value.cvv
    );
  }
  return true;
};

// ✅ 결제 프로세스 시작
const processPayment = async () => {
  if (!agreeToTerms.value) {
    alert('예약 조건에 동의해주세요.');
    return;
  }

  if (!validateCardDetails()) {
    alert('카드 정보를 모두 입력해주세요.');
    return;
  }

  if (!route.query.bookingData) {
    alert('예약 정보를 찾을 수 없습니다. 다시 호텔을 선택해주세요.');
    router.push({ name: 'hotel' });
    return;
  }

  isProcessing.value = true;

  try {
    const userId = authStore.userId;
    const bookingData = JSON.parse(route.query.bookingData);

    bookingData.userId = userId;
    bookingData.status = 'CONFIRMED';
    bookingData.paymentStatus = 'PAID';
    bookingData.bookedAt = new Date().toISOString();
    delete bookingData.providerBookingMeta;

    const response = await hotelApi.createHotelBooking(bookingData);
    console.log('✅ 예약 완료:', response.data);

    isProcessing.value = false;
    travelStore.increaseStep();

    alert('예약이 완료되었습니다!');
    router.push({ name: 'bookingComplete' });

  } catch (error) {
    console.error('❌ 예약 실패:', error.response?.data ?? error.message);
    isProcessing.value = false;
    alert('예약 처리 중 오류가 발생했습니다.');
  }
};
</script>

<style scoped>
.planner-page {
  background-color: #ffffff;
  min-height: 100vh;
  padding-bottom: 6rem;
}
.payment-item {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 0.75rem 1rem;
  border: 1px solid #e2e8f0;
  border-radius: 0.75rem;
  background: #fff;
  cursor: pointer;
  transition: all 0.3s ease;
  text-align: center;
}

.payment-item:hover {
  border-color: #2d4a8f;
  background-color: #f0f4ff;
}

.payment-item.active {
  border-color: #2d4a8f;
  background-color: #2d4a8f;
  color: white;
}

.payment-item i {
  font-size: 1.2rem;
}

.card {
  border: 1px solid #e2e8f0;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.05);
}

.rating {
  display: flex;
  align-items: center;
  font-size: 0.95rem;
}

.form-control {
  border: 1px solid #e2e8f0;
}

.form-control:focus {
  border-color: #2d4a8f;
  box-shadow: 0 0 0 0.2rem rgba(45, 74, 143, 0.25);
}

.btn-primary {
  background-color: #2d4a8f;
  border-color: #2d4a8f;
  color: white !important;
}

.btn-primary:hover:not(:disabled) {
  background-color: #1a2a56;
  border-color: #1a2a56;
  color: white !important;
}

.btn-primary:disabled {
  background-color: #e2e8f0;
  border-color: #e2e8f0;
  color: #94a3b8;
  cursor: not-allowed;
}

.btn-outline-secondary {
  color: #64748b !important;
  border-color: #e2e8f0;
}

.btn-outline-secondary:hover {
  background-color: #f1f5f9;
  border-color: #cbd5e1;
  color: #475569 !important;
}

.hotel-header {
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.05);
}

.hotel-header .title {
  font-size: 1.25rem;
  font-weight: 600;
  color: #1e293b;
}

.hotel-header .sub {
  color: #64748b;
}

.hotel-header .bg-secondary-subtle {
  background-color: #f1f5f9 !important;
}

/* 호텔 정보 타이포그래피 */
.hotel-name {
  font-size: 1.25rem;
  font-weight: 600;
  color: #1e293b;
  margin-bottom: 0.5rem;
  line-height: 1.3;
}

.hotel-location {
  font-size: 0.95rem;
  margin-bottom: 0.5rem;
  line-height: 1.5;
}

.hotel-room-type {
  font-size: 0.9rem;
  margin-bottom: 0.75rem;
  line-height: 1.5;
}

.hotel-dates {
  font-size: 0.9rem;
  color: #64748b;
  line-height: 1.6;
}

.amenities .badge {
  font-size: 0.85rem;
  padding: 0.35rem 0.65rem;
  font-weight: 500;
}

.rating {
  font-size: 1rem;
  line-height: 1.5;
}

.rating-score {
  font-weight: 600;
  color: #1e293b;
}

.rating-reviews {
  font-size: 0.9rem;
  margin-left: 0.25rem;
}

/* 가격 정보 타이포그래피 */
.price-row {
  font-size: 0.95rem;
  line-height: 1.6;
}

.price-label {
  color: #475569;
}

.price-value {
  color: #1e293b;
  font-size: 1rem;
}

.total-row {
  margin-top: 0.5rem;
}

.total-label {
  font-size: 1.1rem;
  color: #1e293b;
}

.total-amount {
  font-size: 1.35rem;
  color: #2d4a8f;
}

/* 결제 수단 타이포그래피 */
.payment-icon {
  font-size: 1.25rem;
  margin-right: 0.5rem;
}

.payment-text {
  font-size: 0.95rem;
  font-weight: 500;
}

/* 폼 라벨 및 입력 */
.card-label {
  font-size: 0.95rem;
  font-weight: 500;
  color: #475569;
  margin-bottom: 0.5rem;
}

.card-input {
  font-size: 0.95rem;
  padding: 0.625rem 1rem;
}

.card-input::placeholder {
  color: #94a3b8;
  font-size: 0.9rem;
}

/* 약관 동의 */
.agreement-label {
  font-size: 0.95rem;
  color: #475569;
  line-height: 1.5;
}

/* 버튼 텍스트 */
.btn-lg {
  font-size: 1rem;
  font-weight: 500;
  padding: 0.75rem 2rem;
}
</style>