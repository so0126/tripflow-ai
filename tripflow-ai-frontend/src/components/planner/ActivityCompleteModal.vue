<!-- src/components/planner/PlannerActivityCompleteModal.vue -->
<template>
  <teleport to="body">
    <div v-if="open" class="modal-backdrop" @click="$emit('close')">
      <div class="modal-card" @click.stop>
        <!-- Header -->
        <div class="modal-header">
          <h3 class="mb-0">
            <strong class="title-highlight">{{ title }}</strong> 방문은 어떠셨나요?
          </h3>
          <button class="btn btn-sm btn-light rounded-circle close-btn" @click="$emit('close')">
            ✕
          </button>
        </div>

        <!-- 비용 입력 -->
        <label class="label">실제 사용 금액 (선택)</label>
        <div class="input mb-2">
          <span class="text-muted">$</span>
          <input
            type="number"
            min="0"
            step="1"
            :value="spendInput"
            placeholder="금액 입력"
            @input="onInput"
          />
          <span class="text-muted">USD</span>
        </div>

        <!-- 빠른 입력 -->
        <div class="quick-amounts mb-3">
          <button v-for="v in [5, 10, 20, 30]" :key="v" type="button" class="chip-btn"
            @click="$emit('update:spend-input', v)">
            ${{ v }}
          </button>
          <button type="button" class="chip-btn ghost" @click="$emit('update:spend-input', null)">
            초기화
          </button>
        </div>

        <!-- 코멘트 -->
        <label class="label">한 줄 메모 (선택)</label>
        <textarea
          class="comment-box mb-3"
          rows="2"
          :value="comment"
          placeholder="기억에 남는 점이나 간단한 메모를 남겨보세요"
          @input="$emit('update:comment', $event.target.value)"
        ></textarea>

        <!-- Quick Stats -->
        <div class="stats-card mb-3">
          <div class="fw-semibold mb-2">[ 활동 요약 ]</div>
          <div class="row small">
            <div class="col-6 mb-1">
              <span class="text-muted">시작 시간</span>
              <div>{{ quickStats?.started }}</div>
            </div>
            <div class="col-6 mb-1">
              <span class="text-muted">소요 시간</span>
              <div>{{ quickStats?.duration }}</div>
            </div>
            <div class="col-12 mt-2">
              <span class="text-muted">상태</span>
              <div class="fw-semibold" :class="quickStats?.status === 'Completed'
                ? 'text-success'
                : 'text-warning'">
                {{ quickStats?.status === 'Completed' ? '완료됨' : '진행 중' }}
              </div>
            </div>
          </div>
        </div>

        <!-- Actions -->
        <div class="d-flex justify-content-between">
          <button class="btn flex-fill me-2" @click="$emit('close')">
            취소
          </button>
          <button class="btn primary flex-fill" @click="$emit('confirm')">
            {{ status === 'DONE' ? '수정하기' : '활동 완료 처리' }}
          </button>
        </div>
      </div>
    </div>
  </teleport>
</template>

<script setup>
defineProps({
  open: Boolean,
  title: String,
  status: String,
  comment: String,
  spendInput: Number,
  quickStats: Object,
});
const emit = defineEmits(["close", "confirm", "update:spend-input", "update:comment"]);
const onInput = (e) => {
  const val = e.target.value === "" ? null : Number(e.target.value);
  emit("update:spend-input", val);
};
</script>

<style scoped lang="scss">
/* Backdrop */
.modal-backdrop {
  position: fixed;
  inset: 0;
  background: rgba(17, 24, 39, 0.45);
  display: flex;
  align-items: flex-start;
  justify-content: center;
  padding: 6vh 14px;
  z-index: 10000;
}

/* Card */
.modal-card {
  width: min(520px, 92vw);
  background: #ffffff;
  border-radius: 18px;
  padding: 22px 20px 20px;
  box-shadow: 0 14px 42px rgba(0, 0, 0, 0.25);
  animation: pop 0.18s ease;
}

@keyframes pop {
  from {
    transform: translateY(-6px);
    opacity: 0.9;
  }

  to {
    transform: translateY(0);
    opacity: 1;
  }
}

/* Header */
.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 1rem;
}

.modal-header h3 {
  font-size: 1.5rem;
  font-weight: 600;
  line-height: 1.4;
  padding-right: 10px;
  color: #1f2937;
  /* 검은색 - "방문은 어떠셨나요?" */
}

.title-highlight {
  color: #3730a3;
  /* 남색 - 장소명 (경복궁) */
  font-weight: 800;
}

/* 🔧 본문 텍스트 크기 통일 */
.label,
.input,
.input input,
.comment-box,
.quick-amounts button,
p {
  font-size: 1.3rem;
  /* typography.scss의 p 기준 */
}

/* Inputs */
.input {
  display: flex;
  align-items: center;
  border: 1px solid #e5e7eb;
  border-radius: 12px;
  padding: 6px 10px;
  gap: 6px;

  input {
    border: 0;
    outline: 0;
    width: 100%;
    background: transparent;
  }
}

/* 🔧 Chips: 작게 */
.quick-amounts {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
}

.chip-btn {
  border-radius: 999px;
  border: 1px solid #c7d2fe;
  padding: 2px 10px;
  /* 🔽 높이 줄임 */
  font-size: 1rem;
  /* 🔽 small 급 */
  background: #eef2ff;
  color: #3730a3;
  cursor: pointer;

  &.ghost {
    background: #ffffff;
    color: #6b7280;
  }
}

/* Comment */
.comment-box {
  width: 100%;
  border-radius: 12px;
  border: 1px solid #e5e7eb;
  padding: 8px 10px;
  resize: vertical;
}

/* Stats */
.stats-card {
  border-radius: 14px;
  background: linear-gradient(135deg, #eef2ff, #f8faff);
  padding: 12px 14px;
}

/* Buttons */
.btn.primary {
  background: #1f2937;
  color: #ffffff;
  border-color: #1f2937;
}
</style>