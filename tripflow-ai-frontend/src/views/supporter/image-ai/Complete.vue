<template>


  <div class="complete-card card p-5 mt-4 text-center">
    <div class="check-circle mb-4">✓</div>
    <h3 class="mb-2">이미지 검색 완료!</h3>
    <p class="text-muted mb-4">당신의 여정에 장소가 추가됩니다!</p>

    <div class="d-flex flex-column align-items-center gap-3">
             <NavigationButtons
            back-text="다른 사진 검색하기"
            next-text="서포터 메인으로 돌아가기"
            @back="goSearchAnother"
            @next="goBackToSupporter"
          >
          </NavigationButtons>
  </div>
  </div>
  <!-- </div> -->
</template>

<script setup>
import PageHeader from '@/components/common/header/PageHeader.vue'
import { useRoute, useRouter } from 'vue-router'
import NavigationButtons from '@/components/common/button/NavigationButtons.vue';

const router = useRouter();
const route = useRoute()
// optional: receive state plans if needed
const plans = route?.state?.plans || null

// 왼쪽(Back) 버튼 클릭 시 이동
const goBackToSupporter = () => {
  router.push({ name: 'SupporterMain' });
};

// 오른쪽(Next) 버튼 클릭 시 이동
const goSearchAnother = () => {
  router.push({ name: 'CreateNewSearch' });
};
</script>

<style scoped>
.supporter-page {
  background-color: #fffaf3;
  min-height: 100vh;
  padding: 2rem 1.25rem; /* App.vue 사이드바도 padding-top: 2rem 필요 */
}

.complete-wrap {
  padding-bottom: 40px;
}

.complete-card {
  max-width: 720px;
  margin: 32px auto;
  border-radius: 12px;
  background: #fff9ff;
  box-shadow: 0 8px 30px rgba(27, 59, 111, 0.06);
}

.check-circle {
  width: 96px;
  height: 96px;
  border-radius: 50%;
  background: #1b3b6f;
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 44px;
  margin: 0 auto;
  box-shadow: 0 8px 24px rgba(27, 59, 111, 0.12);
}

/* buttons */
.btn-wide {
  width: 320px;
  border-radius: 12px;
  padding: 12px 18px;
}

.btn-primary {
  background: #1b3b6f;
  border-color: #1b3b6f;
  color: #fff;
}

.btn-outline-primary {
  background: #fff;
  color: #1b3b6f;
  border: 1px solid #e6eef9;
}
</style>