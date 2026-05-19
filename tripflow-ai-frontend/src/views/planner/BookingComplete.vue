<template>
  <div class="planner-page">    
  <div class="booking-complete container py-5">
    <div class="text-center mb-4">
      <div class="check-circle mx-auto mb-3 d-inline-flex align-items-center justify-content-center">
        <i class="bi bi-check-lg text-white fs-2"></i>
      </div>
      <h2 class="fw-bold">완료되었습니다!</h2>
      <p class="text-muted">서울 여행 준비가 모두 끝났어요</p>
    </div>

    <div class="row justify-content-center">
      <div class="col-md-10">
        <div class="card mb-4 bg-light border-0 shadow-sm p-3">
          <h6 class="mb-2">다음 단계</h6>
          <ul class="mb-0 small">
            <li>일정이 플래너에 저장되었습니다</li>
            <li>언제든지 일정을 수정할 수 있어요</li>
            <li>여행 중에는 서포터에서 유용한 지도 기반 도움을 받을 수 있어요</li>
          </ul>
        </div>

          <div class="d-grid mb-2">
  <FilledButton
    @click="goToPlanner"
  >
    전체 일정으로 돌아가기
  </FilledButton>
</div>

        <p class="text-muted small mt-3">
          <i class="bi bi-pin-angle me-1"></i>
          팁: 챗봇에게 다양한 요청을 해보세요. 여행지를 추천받거나 수정할 수도 있습니다!
        </p>
      </div>
    </div>
  </div>
  </div>

</template>

<script setup>
import { useTravelStore } from '@/store/travelStore'
import router from '@/router'
import FilledButton from '@/components/common/button/FilledButton.vue'
const travelStore = useTravelStore()


function startTravel() {
  travelStore.startTravel()
}
const goToPlanner = () => {
  travelStore.startTravel();
  router.push("/planner/edit");
};
</script>

<style scoped>
.planner-page {
  background-color: #ffffff;
  min-height: 100vh;
  padding-bottom: 6rem;
  padding: 2rem 1.25rem 6rem; /* 👈 상단 padding 2rem으로 통일 */
}
.booking-complete {
  max-width: 980px;
  margin: 0 auto;
}

.check-circle {
  width: 72px;
  height: 72px;
  border-radius: 50%;
  background: #28a745;
  box-shadow: 0 6px 18px rgba(40, 167, 69, 0.12);
}

.btn-primary {
  background-color: var(--bs-primary, #1B3B6F);
  border-color: var(--bs-primary, #1B3B6F);
  color: white;
}

.btn-primary:hover {
  opacity: 0.95;
}
</style>
