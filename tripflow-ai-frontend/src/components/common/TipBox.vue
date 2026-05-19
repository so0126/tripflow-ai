<!-- 컴포넌트의 UI -->
<template>
    <div class="tip-box bg-white p-3 rounded-3 shadow-sm">
        <div class="d-flex gap-2 mb-2">
            <i class="bi bi-lightbulb text-warning fs-5"></i>
            <h5 class="fw-bold mb-0">{{ props.name }}</h5>
        </div>
        <p class="text-muted mb-0">
            {{ props.description }}
        </p>
    </div>
</template>

<!-- 컴포넌트의 초기화 또는 이벤트 처리 -->
<script setup>
const props = defineProps({
  name: String,
  description: String,
})
</script>

<!-- 컴포넌트 스타일 정의 -->
<style scoped>
.tip-box {
  border: 1px solid rgba(0, 0, 0, 0.05);
}
</style>