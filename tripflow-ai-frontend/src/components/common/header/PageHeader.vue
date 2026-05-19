<template>
  <header class="page-header">
    <div class="d-flex flex-column">
      <div class="d-flex align-items-center mb-1">
        <i 
          v-if="icon" 
          :class="['bi', icon, 'me-2', 'fs-4']" 
          :style="{ color: iconColor }"
        ></i>
        <h5 class="page-title m-0 fw-bold">{{ title }}</h5>
      </div>
      
      <p v-if="subtitle" class="page-subtitle text-muted m-0 ms-1">
        {{ subtitle }}
      </p>
    </div>
  </header>
</template>

<script setup>
defineProps({
  title: { type: String, required: true },
  subtitle: { type: String, default: "" },
  icon: { type: String, default: "" },
  iconColor: { type: String, default: "#ff8c00" }
});
</script>

<style scoped>
.page-header {
  /* 통일된 웜톤 그라디언트 배경 */
  background: linear-gradient(90deg, #fffaf3 0%, #ffffff 100%);
  padding: 1.5rem 1.5rem;
  border-bottom: 1px solid rgba(27, 59, 111, 0.08);
  margin-bottom: 2rem; /* 하단 콘텐츠와의 여백 */

  /* ✅ 높이를 WeatherCard와 맞춤 (예: 180px) */
  height: 120px;
  border-radius: 16px; /* 둥근 모서리도 맞추고 싶다면 추가 */
  display: flex;       /* 내부 정렬을 위해 */
  flex-direction: column;
}

.page-title {
  color: #1B3B6F; /* 기본 텍스트 컬러 통일 */
  letter-spacing: -0.5px;
}

.page-subtitle {
  font-size: 1.25rem;
  line-height: 1.4;
}
</style>