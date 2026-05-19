<template>
    <div class="plan-card-container">
        <!-- 일정 카드 표시 -->
        <div v-if="placeInfo" class="card border-0 shadow-sm rounded-3 overflow-hidden plan-card">
            <div class="card-header bg-primary text-white">
                <div class="d-flex justify-content-between align-items-center">
                    <div>
                        <h6 class="mb-1">{{ placeInfo.dayIndex ? `Day ${placeInfo.dayIndex}` : '일정' }}</h6>
                        <h5 class="mb-0">{{ placeInfo.placeName }}</h5>
                    </div>
                    <span class="badge bg-light text-primary">
                        {{ placeInfo.category || '장소' }}
                    </span>
                </div>
            </div>

            <!-- 이미지 -->
            <div class="card-img-top plan-image">
                <img 
                    :src="placeInfo.imageUrl || getDefaultImage(placeInfo.placeName)" 
                    :alt="placeInfo.placeName" 
                    class="w-100 h-100"
                    @error="onImageError"
                />
            </div>

            <!-- 정보 -->
            <div class="card-body">
                <!-- 시간 -->
                <div v-if="placeInfo.visitTime" class="d-flex align-items-center mb-3">
                    <i class="bi bi-clock me-2 text-primary"></i>
                    <span>{{ formatTime(placeInfo.visitTime) }}</span>
                </div>

                <!-- 주소 -->
                <div v-if="placeInfo.address" class="d-flex align-items-start mb-3">
                    <i class="bi bi-geo-alt me-2 text-primary"></i>
                    <span class="text-muted small">{{ placeInfo.address }}</span>
                </div>

                <!-- 카테고리 -->
                <div v-if="placeInfo.category" class="d-flex align-items-center mb-3">
                    <i class="bi bi-tag me-2 text-primary"></i>
                    <span class="badge bg-light text-dark">{{ placeInfo.category }}</span>
                </div>

                <!-- 확인 버튼들 -->
                <div v-if="showConfirmation" class="mt-4 d-flex gap-2">
                    <button
                      class="btn btn-primary btn-sm flex-grow-1"
                      @click="emitConfirm"
                    >
                        <i class="bi bi-check-circle me-1"></i> 맞습니다
                    </button>
                    <button
                      class="btn btn-outline-secondary btn-sm flex-grow-1"
                      @click="emitCancel"
                    >
                        <i class="bi bi-x-circle me-1"></i> 아닙니다
                    </button>
                </div>
            </div>
        </div>
    </div>
</template>

<script setup>
import { ref } from 'vue';

const props = defineProps({
    placeInfo: {
        type: Object,
        default: null
    },
    showConfirmation: {
        type: Boolean,
        default: false
    },
    message: {
        type: String,
        default: '이 일정을 삭제하시나요?'
    },
    actionType: {
        type: String,
        default: 'DELETE'
    }
});

const emit = defineEmits(['confirm', 'cancel']);
const isConfirming = ref(false);

const formatTime = (timeStr) => {
    if (!timeStr) return '시간 미정';
    try {
        // ISO 형식의 시간 문자열 포맷
        const date = new Date(timeStr);
        if (isNaN(date)) return timeStr;
        const hours = date.getHours().toString().padStart(2, '0');
        const minutes = date.getMinutes().toString().padStart(2, '0');
        return `${hours}:${minutes}`;
    } catch {
        return timeStr;
    }
};

const emitConfirm = async () => {
    // NOTE: ChatSidebar의 onPlanCardConfirm에서 API를 호출하므로
    // 여기서는 단순히 이벤트만 emit
    console.log('📤 [PlanCardDisplay] confirm 이벤트 emit');
    emit('confirm');
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
    console.warn('❌ 이미지 로딩 실패:', event.target.src);
    if (props.placeInfo?.placeName) {
        event.target.src = getDefaultImage(props.placeInfo.placeName);
    }
};
</script>

<style scoped>
.plan-card-container {
    margin: 10px 0;
    animation: slideIn 0.3s ease-out;
}

@keyframes slideIn {
    from {
        opacity: 0;
        transform: translateY(10px);
    }
    to {
        opacity: 1;
        transform: translateY(0);
    }
}

.plan-card {
    border: 1px solid #e0e0e0;
    transition: transform 0.2s, box-shadow 0.2s;
}

.plan-card:hover {
    transform: translateY(-2px);
    box-shadow: 0 8px 16px rgba(0, 0, 0, 0.15);
}

.card-header {
    padding: 1rem;
    border-bottom: 3px solid rgba(255, 255, 255, 0.2);
}

.plan-image {
    height: 200px;
    overflow: hidden;
    background-color: #f5f5f5;
}

.plan-image img {
    object-fit: cover;
}

.card-body {
    padding: 1rem;
}

.btn-sm {
    font-size: 0.875rem;
    padding: 0.4rem 0.8rem;
}

i {
    font-size: 0.95rem;
}
</style>
