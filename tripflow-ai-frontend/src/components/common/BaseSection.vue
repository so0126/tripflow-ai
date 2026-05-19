<!-- 컴포넌트의 UI -->
<template>
  <div class="upload-section mb-4" :style="{ backgroundColor: bgColor }">
    <div class="upload-header d-flex justify-content-between align-items-center mb-3">
      
      <div class="d-flex align-items-center gap-3">
        
        <div class="header-icon flex-shrink-0">
          <slot name="icon">
            <i v-if="icon" :class="icon + ' text-primary fs-4'"></i>
          </slot>
        </div>

        <div class="header-text-group">
          <h5 class="upload-title mb-0">
            {{ title }}
          </h5>
          
          <div class="upload-subtitle mt-1">
            <template v-if="$slots.subtitle">
              <slot name="subtitle"></slot>
            </template>
            <span v-else>{{ subtitle }}</span>
          </div>
        </div>

      </div>

      <div v-if="$slots.actions" class="upload-header-actions ms-3">
        <slot name="actions"></slot>
      </div>
    </div>

    <div class="upload-content">
      <slot></slot>
    </div>
  </div>
</template>

<!-- 컴포넌트의 초기화 또는 이벤트 처리 -->
<script setup>
defineProps({
    title: String,
    subtitle: String,
    icon: String,
    bgColor: { type: String, default: '#f9fafc' }
})
</script>

<!-- 컴포넌트 스타일 정의 -->
<style scoped>
.upload-section {
  background: linear-gradient(135deg, #ffffff 0%, #fafbfc 100%);
  border-radius: 18px;
  padding: 1.75rem;
  border: 1px solid rgba(26, 42, 86, 0.08);
  margin-bottom: 0;
  box-shadow: 
    0 4px 16px rgba(26, 42, 86, 0.06),
    0 1px 4px rgba(26, 42, 86, 0.04);
  transition: all 0.3s ease;
  position: relative;
  overflow: hidden;
}

.upload-section::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 3px;
  background: linear-gradient(90deg, #2d4a8f 0%, #4a6bb5 100%);
  border-radius: 18px 18px 0 0;
}

.upload-section:hover {
  transform: translateY(-2px);
  box-shadow: 
    0 8px 24px rgba(26, 42, 86, 0.1),
    0 2px 8px rgba(26, 42, 86, 0.06);
  border-color: rgba(74, 107, 181, 0.2);
}

.upload-header {
  /* 전체 헤더 정렬 */
  display: flex;
  justify-content: space-between;
  align-items: center; /* 수직 중앙 정렬 */
}

.upload-title {
  font-weight: 700;
  font-size: 1.15rem;
  color: #2d4a8f;
  line-height: 1.2; /* 행간 조절 */
  letter-spacing: -0.01em;
}

.upload-subtitle {
  color: #6c757d;
  font-size: 0.95rem;
  line-height: 1.4;
}

/* 아이콘이 텍스트보다 커도 텍스트가 찌그러지지 않게 */
.header-text-group {
  display: flex;
  flex-direction: column;
  justify-content: center;
}

.upload-header-actions {
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.upload-content {
  margin-top: 1rem;
}
</style>