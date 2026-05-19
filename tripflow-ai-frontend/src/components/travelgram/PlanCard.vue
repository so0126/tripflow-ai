<template>
  <div class="plan-card" @click="handleClick">

    <!-- 본문 -->
    <div class="plan-info">
      <div class="d-flex align-items-center mb-1">
        <span class="badge me-2">✔ {{ planStatus }}</span>
        <h5 class="plan-title mb-0" :class="{ 'no-title': !hasTitle }">{{ displayTitle }}</h5>
      </div>
      <p class="plan-meta mb-0">
        <i class="bi bi-calendar-event me-1"></i>{{ date }}<br />
        <i class="bi bi-currency-dollar me-1"></i>{{ budget }}
      </p>
    </div>

    <!-- 오른쪽 화살표 -->
    <i class="bi bi-chevron-right icon-chevron"></i>
  </div>
</template>

<script setup>
import { computed } from "vue";

const props = defineProps({
  planId: [String, Number],
  planTitle: { type: String, default: '' }, // required 해제 또는 default 설정
  date: String,
  budget: String,
  image: String,
  planStatus: { type: String, default: 'Done' }
})

// 2. 이벤트를 정의합니다.
const emit = defineEmits(['cardClick'])
// ✅ 1. 실제 제목이 있는지 확인하는 computed 속성 추가
const hasTitle = computed(() => {
  return props.planTitle && props.planTitle.trim().length > 0;
})

// 2. 화면에 표시할 텍스트 결정
const displayTitle = computed(() => {
  return hasTitle.value
    ? props.planTitle
    : '카드를 클릭하면 AI가 당신의 여정에 맞는 제목을 만들어드립니다.';
})

// 3. 클릭 시 부모에게 id와 title을 전달합니다.
const handleClick = () => {
  emit('cardClick', props.planId, props.planTitle)
}
</script>

<style scoped>
.plan-card {
  text-align: left; /* ✅ 부모의 중앙 정렬 상속 제거 */
  border: 1px solid #eee;
  background-color: #d9f0ff;
  border-radius: 1rem;
  display: flex;
  align-items: center;
  justify-content: flex-start; /* ✅ 내부 아이템 왼쪽 정렬 */
  padding: 1rem;
  margin-bottom: 1rem;
  transition: all 0.3s ease;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.03);
  cursor: pointer; /* ✅ 클릭 가능 커서 */
  transition: transform 0.25s ease, box-shadow 0.25s ease;

  &:hover {
    transform: translateY(-3px);
    border-color: #ffb347;
    box-shadow: 0 4px 12px rgba(255, 140, 0, 0.15);
  }


  img {
    width: 70px;
    height: 70px;
    object-fit: cover;
    border-radius: 0.75rem;
    margin-right: 1rem;
  }

/* 텍스트 정보 영역 */
  .plan-info {
    flex: 1;
    overflow: hidden; /* 긴 텍스트 처리용 */

    .plan-title {
      font-weight: 600;
      color: #1B3B6F;
      margin-bottom: 0.25rem;
      word-break: keep-all; /* 단어 단위로 줄바꿈 */
      line-height: 1.4;     /* 줄간격 확보 */
      /* white-space: nowrap; */
      /* overflow: hidden; */
      /* text-overflow: ellipsis; */
      /* ✅ [수정됨] .no-title 클래스가 붙었을 때 스타일 오버라이딩 */
      &.no-title {
        color: #999;       /* 회색 */
        font-style: italic; /* 기울임 */
        font-weight: 400;   /* 얇게 */
      }
    

    .plan-meta {
      font-size: 0.85rem;
      color: #666;
      line-height: 1.4;
    }
    }

  .badge {
    background-color: #3ac569;
    font-size: 1.15rem;
    padding: 0.25rem 0.5rem;
    border-radius: 0.5rem;
    color: #fff;
    font-weight: 500;
  }

  .icon-chevron {
    font-size: 1.25rem;
    color: #bbb;
  }
}
}
</style>