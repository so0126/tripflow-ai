<template>
  <div class="profile-card shadow-sm">
    <div class="profile-avatar mx-auto mb-2">
      <img :src="displayImage" :alt="displayName" class="avatar-img" />
    </div>

    <h4 class="profile-name mb-1">{{ displayName }}</h4>
    <p class="profile-bio mb-3">{{ bio }}</p>

    <div class="profile-stats">
      <div class="stat-item">
        <span class="stat-number">{{ totalplans }}</span>
        <span class="stat-label">총 여행</span>
      </div>
      <div class="stat-item">
        <span class="stat-number">{{ travelDays }}</span>
        <span class="stat-label">여행 일수</span>
      </div>
      <div class="stat-item">
        <span class="stat-number">{{ completed }}</span>
        <span class="stat-label">완료된 여행</span>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue';
import { useAuthStore } from '@/store/authStore'; // 1. Store 임포트
import defaultProfileImg from '@/assets/img/profile-logo.png';

// 2. Store 초기화
const authStore = useAuthStore();

// 3. Props 정의 (name, image는 Store에서 가져오므로 제거, 나머지는 유지)
defineProps({
  bio: { type: String, required: true },
  // 통계 데이터는 상황에 따라 API로 가져오거나 Props로 받을 수 있음
  totalplans: { type: Number, default: 0 },
  travelDays: { type: Number, default: 0 },
  completed: { type: Number, default: 0 },
});

// 4. Computed로 Store 데이터 연결 (반응성 유지)
// 이름 로직: Store에 이름이 있으면 사용, 없으면 'User'
const displayName = computed(() => {
  return authStore.user?.name || '사용자'; 
});

// 이미지 로직: Store에 이미지가 있으면 사용, 없으면 기본 이미지
const displayImage = computed(() => {
  return authStore.userProfileImage || defaultProfileImg;
});
</script>

<style scoped>
/* ===============================
   👤 Profile Summary
   =============================== */
.profile-card {
  background: #ffffff;
  padding: 1.75rem 1.5rem;
  border-radius: 20px;
  width: 100%;
  max-width: 380px;
  margin: 0 auto;
  text-align: center;
  transition: all 0.25s ease;
  border: 1px solid rgba(0, 0, 0, 0.06);
  margin-top: 2rem; /* 🔥 필요하면 1.5rem~3rem 사이 조절 */
}

.profile-card:hover {
  transform: translateY(-3px);
  box-shadow: 0 6px 18px rgba(0, 0, 0, 0.08);
}

.profile-avatar .avatar-img {
  width: 80px;
  height: 80px;
  border-radius: 50%;
  object-fit: cover;
  margin: 0 auto;
  border: 3px solid #e9ecef;
}

.profile-name {
  font-weight: 700;
  color: #2b2b2b;
}

.profile-bio {
  color: #666;
}

/* --- 통계 영역 수정 부분 --- */
.profile-stats {
  margin-top: 1.5rem;
  display: flex;
  justify-content: center; /* 전체 영역 가운데 정렬 */
  gap: 1.5rem; /* 아이템 사이 간격 (margin 대신 gap 사용 추천) */
}

.stat-item {
  /* 내부 요소(숫자, 글자)를 수직 중앙 정렬 */
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-width: 70px; /* 각 항목의 최소 너비를 잡아주면 더 안정적임 */
}

.stat-number {
  font-size: 1.5rem; /* h5 사이즈 대체 */
  font-weight: 700;
  color: #1b3b6f;
  margin-bottom: 0.25rem;
  line-height: 1.2;
}

.stat-label {
  font-size: 1.25rem;
  color: #888;
  white-space: nowrap; /* 글자가 줄바꿈되지 않도록 */
}
</style>
