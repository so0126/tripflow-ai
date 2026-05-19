<template>
  <div class="timeline-card-wrapper">
    <!-- Day 헤더 -->
    <div class="day-header mb-2">
      <h6 class="mb-0">📍 Day {{ placeInfo.dayIndex }}</h6>
    </div>

    <!-- 타임라인 항목 -->
    <div class="timeline-item">
      <div class="place-number-circle">{{ placeInfo.dayIndex }}</div>

      <div class="timeline-content">
        <!-- 시간 레이블 -->
        <div class="place-time-badge">
          🕐 {{ formatTime(placeInfo.visitTime) }}
        </div>

        <!-- 카드 -->
        <div class="place-card shadow-sm rounded-3 p-3">
          <!-- 이미지 -->
          <div class="place-image mb-3">
            <img 
              :src="placeInfo.imageUrl || getDefaultImage(placeInfo.placeName)" 
              :alt="placeInfo.placeName" 
              class="w-100 rounded-2" 
              @error="onImageError" 
            />
          </div>

          <!-- 장소명 -->
          <div class="place-title mb-2">{{ placeInfo.placeName }}</div>

          <!-- 방문 시간 -->
          <div class="place-visit-time small mb-2">
            <span class="time-icon">🕐</span> {{ formatTime(placeInfo.visitTime) }}
          </div>

          <!-- 카테고리 -->
          <div v-if="placeInfo.category" class="place-category small text-muted mb-3">
            {{ placeInfo.category }}
          </div>

          <!-- 주소 -->
          <div v-if="placeInfo.address" class="place-address small text-muted mb-3">
            📍 {{ placeInfo.address }}
          </div>

          <!-- 확인 버튼들 -->
          <div v-if="showConfirmation" class="d-flex gap-2 mt-3">
            <button class="btn btn-primary btn-sm flex-grow-1" @click="emitConfirmWithAPI" :disabled="isConfirming">
              <i v-if="isConfirming" class="spinner-border spinner-border-sm me-1"></i>
              <i v-else class="bi bi-check-circle me-1"></i>
              {{ isConfirming ? '처리중...' : '맞습니다' }}
            </button>
            <button class="btn btn-outline-secondary btn-sm flex-grow-1" @click="emitCancel" :disabled="isConfirming">
              <i class="bi bi-x-circle me-1"></i> 아닙니다
            </button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue';
import chatApi from '@/api/chatApi';
import { useAuthStore } from '@/store/authStore';

const authStore = useAuthStore();

const props = defineProps({
  placeInfo: {
    type: Object,
    default: null
  },
  showConfirmation: {
    type: Boolean,
    default: false
  },
  actionType: {
    type: String,
    default: 'DELETE'  // DELETE, MODIFY, ADD
  }
});

const emit = defineEmits(['confirm', 'cancel']);
const isConfirming = ref(false);

const formatTime = (timeStr) => {
  if (!timeStr) return '시간 미정';
  try {
    const date = new Date(timeStr);
    if (isNaN(date.getTime())) return timeStr;
    const hours = String(date.getHours()).padStart(2, '0');
    const minutes = String(date.getMinutes()).padStart(2, '0');
    return `${hours}:${minutes}`;
  } catch (e) {
    return timeStr;
  }
};

const emitConfirmWithAPI = async () => {
  console.log('🎯 [PlanDayTimelineCard] emitConfirmWithAPI 호출됨!');
  isConfirming.value = true;
  try {
    // Backend API 호출: 확인 상태 업데이트
    const payload = {
      actionType: props.actionType,
      placeName: props.placeInfo.placeName,
      userId: authStore.user?.id || 22  // 현재 사용자 ID
    };

    console.log('🎯 [PlanDayTimelineCard] 확인 API 호출:', {
      url: '/api/chat/confirm-action',
      method: 'POST',
      payload
    });

    const result = await chatApi.confirmAction(payload);

    console.log('✅ [PlanDayTimelineCard] 확인 API 응답:', result);

    // 사용자 메시지 전송 (LLM이 Tool을 다시 호출하도록)
    console.log('📤 [PlanDayTimelineCard] confirm 이벤트 emit!');
    emit('confirm');
  } catch (error) {
    console.error('❌ [PlanDayTimelineCard] 확인 API 호출 실패:', error);
    alert('확인 처리 중 오류가 발생했습니다: ' + error.message);
  } finally {
    isConfirming.value = false;
  }
};

const emitCancel = () => {
  emit('cancel');
};

// 🖼️ 기본 이미지 생성 (SVG Data URI)
const getDefaultImage = (placeName) => {
  const displayName = placeName.substring(0, 8);
  return `data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='300' height='200'%3E%3Crect fill='%231B3B6F' width='300' height='200'/%3E%3Ctext x='50%25' y='50%25' text-anchor='middle' dy='.3em' fill='white' font-size='18' font-family='Arial'%3E${encodeURIComponent(displayName)}%3C/text%3E%3C/svg%3E`;
};

// 🖼️ 이미지 로딩 실패 시 기본 이미지로 대체
const onImageError = (event) => {
  console.warn('❌ 이미지 로딩 실패:', props.placeInfo.imageUrl);
  if (props.placeInfo?.placeName) {
    event.target.src = getDefaultImage(props.placeInfo.placeName);
  }
};
</script>

<style scoped>
.timeline-card-wrapper {
  margin: 12px 0;
}

.day-header {
  font-size: 0.95rem;
  font-weight: 600;
  color: #333;
  padding: 0 0 8px 0;
}

.timeline-item {
  display: flex;
  gap: 12px;
  position: relative;
}

.place-number-circle {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 36px;
  height: 36px;
  border-radius: 50%;
  background-color: #1B3B6F;
  color: white;
  font-weight: 700;
  font-size: 0.9rem;
  flex-shrink: 0;
  margin-top: 2px;
}

.timeline-content {
  flex: 1;
}

.place-time-label {
  font-size: 0.85rem;
  color: #666;
  margin-bottom: 6px;
  font-weight: 500;
}

.place-time-badge {
  font-size: 0.95rem;
  font-weight: 600;
  color: #1B3B6F;
  margin-bottom: 8px;
  padding: 4px 8px;
  background-color: #f0f4ff;
  border-radius: 4px;
  display: inline-block;
}

.place-card {
  background-color: #ffffff;
  border: 1px solid #e0e0e0;
}

.place-image {
  width: 100%;
  height: 160px;
  overflow: hidden;
  border-radius: 8px;
}

.place-image img {
  object-fit: cover;
  height: 100%;
}

.place-image-placeholder {
  width: 100%;
  height: 160px;
  background-color: #f5f5f5;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  border: 1px dashed #ddd;
}

.placeholder-content {
  color: #999;
  font-size: 0.95rem;
  text-align: center;
}

.place-title {
  font-size: 1.05rem;
  font-weight: 600;
  color: #333;
}

.place-visit-time {
  color: #666;
  font-size: 0.9rem;
  margin-bottom: 8px;
}

.time-icon {
  margin-right: 4px;
}

.place-category {
  font-size: 0.9rem;
  color: #999;
}

.place-address {
  font-size: 0.9rem;
  color: #666;
  line-height: 1.4;
}
</style>
