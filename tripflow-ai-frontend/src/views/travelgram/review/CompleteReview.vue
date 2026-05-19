<template>
  
  <div class="complete-page">
    <div class="complete-container">
      <!-- 🎉 아이콘 -->
      <div class="success-icon">
        🎉
      </div>

      <!-- 제목 -->
      <h2 class="complete-title">
        후기가 인스타그램에<br />
        성공적으로 업로드되었어요
      </h2>

      <!-- 설명 (가독성 개선) -->
      <div class="message">
        <p>당신의 여행 이야기가 세상과 연결되었습니다.</p>
        <p>이 순간이 누군가의 여행에</p>
        <p>작은 영감이 되기를 바랍니다.</p>
      </div>

      <!-- 인스타 CTA -->
      <div class="share-box">
        <p class="share-text">
          인스타그램에서 직접 확인해보세요
        </p>

        <!-- ✅ 버튼 형태 -->
        <button class="btn-instagram" @click="openInstagram">
          <i class="bi bi-instagram me-2"></i>
          인스타그램에서 보기
        </button>
      </div>

      <!-- 하단 네비 -->
      <NavigationButtons
        backText="다시 만들기"
        nextText="메인 홈으로"
        @back="goToTravelgram"
        @next="goHome"
      />
    </div>
  </div>
</template>


<script setup>
import { useRouter } from 'vue-router'
import PageHeader from '@/components/common/header/PageHeader.vue'
import NavigationButtons from '@/components/common/button/NavigationButtons.vue'

const router = useRouter()
const props = defineProps({
  planId: {
    type: [String, Number],
    required: true
  }
})
const goToTravelgram = () => router.push({ name: 'Travelgram' })
const goHome = () => router.push('/')

const openInstagram = () => {
  window.open('https://www.instagram.com/', '_blank')
}
</script>


<style scoped>
.complete-page {
  background-color: #ffffff;
  min-height: 100vh;
  padding: 3rem 1.25rem 6rem;
  display: flex;
  justify-content: center;
}

.complete-container {
  background: white;
  border-radius: 2rem;
  padding: 3.5rem 2rem 3rem;
  max-width: 420px;
  width: 100%;
  text-align: center;
  box-shadow: 0 16px 40px rgba(0,0,0,0.08);
  animation: fadeUp 0.8s ease both;
}

/* 🎉 */
.success-icon {
  font-size: 5rem;
  margin-bottom: 1.5rem;
  animation: popBounce 1.2s ease;
}

/* 제목 */
.complete-title {
  color: #1b3b6f;
  font-weight: 700;
  line-height: 1.4;
  margin-bottom: 1.5rem;
}

/* 설명 */
.message {
  color: #555;
  line-height: 1.8;
  margin-bottom: 2.5rem;
}

.message p {
  margin: 0.25rem 0;
}

/* 공유 박스 */
.share-box {
  background: #f8f9fb;
  border-radius: 1.25rem;
  padding: 1.6rem 1.25rem;
  margin-bottom: 2.5rem;
}

.share-text {
  color: #1b3b6f;
  font-weight: 600;
  margin-bottom: 1rem;
}

/* 🔥 인스타 버튼 (공식 감성) */
.btn-instagram {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  border: none;
  color: white;
  font-weight: 700;
  padding: 0.9rem 1.9rem;
  border-radius: 999px;
  cursor: pointer;
  background: linear-gradient(
    45deg,
    #f09433,
    #e6683c,
    #dc2743,
    #cc2366,
    #bc1888
  );
  box-shadow: 0 10px 24px rgba(220, 39, 67, 0.35);
  transition: all 0.25s ease;
}

.btn-instagram:hover {
  transform: translateY(-3px);
  box-shadow: 0 14px 30px rgba(220, 39, 67, 0.45);
}

/* 애니메이션 */
@keyframes popBounce {
  0% { transform: scale(0.4); opacity: 0; }
  60% { transform: scale(1.15); }
  100% { transform: scale(1); opacity: 1; }
}

@keyframes fadeUp {
  from { opacity: 0; transform: translateY(20px); }
  to { opacity: 1; transform: translateY(0); }
}
</style>

