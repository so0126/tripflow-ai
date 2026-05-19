<!-- src/components/planner/NowActivity.vue -->
<template>
  <div v-if="place" ref="cardRef" class="now-active-card" :class="{ disabled: isLocked }" :style="bgGradient"
    @click="onClickCard">
    <!-- Top row: NOW pill + dot + status + (nav on right) -->
    <div class="now-top">
      <span class="now-pill">NOW</span>

      <span class="dot" :class="{
        done: isDone,
        current: isCurrent,
        locked: isLocked
      }" />

      <span class="status">
        {{
          isDone
            ? "완료됨"
            : isCurrent
              ? "진행 중"
              : "예정됨"
        }}
      </span>

      <span v-if="isLocked" class="lock-badge" title="아직 시작되지 않은 일정입니다">
        🔒
      </span>

      <!-- 네비게이션: 기능 유지 (UI는 최대한 얇게) -->
      <div class="now-nav" @click.stop>
        <button class="now-nav-btn" :disabled="index === 0" @click.stop="$emit('update:index', index - 1)"
          aria-label="previous">
          ‹
        </button>

        <span class="now-nav-count">
          {{ index + 1 }} / {{ total }}
        </span>

        <button class="now-nav-btn" :disabled="index === total - 1" @click.stop="$emit('update:index', index + 1)"
          aria-label="next">
          ›
        </button>
      </div>
    </div>

    <!-- Center title -->
    <div class="now-title">
      {{ place.title }}
    </div>

    <!-- subtitle: time • place -->
    <div class="now-sub">
      <span class="time">
        {{ formatTime(place.startAt) }}
        <span v-if="place.endAt">– {{ formatTime(place.endAt) }}</span>
      </span>

      <!-- ✅ 주소 줄 -->
      <div v-if="place.details?.address" class="now-address">
        {{ place.details.address }}
      </div>
      <!-- <span class="sep">•</span>
      <span class="place truncate">
        {{ place.details?.area || place.details?.address || "Seoul" }}
      </span> -->
    </div>

    <!-- progress -->
    <div class="progress" aria-hidden="true">
      <div class="bar" :style="{ width: progressPercent + '%' }"></div>
    </div>
  </div>
</template>

<script setup>
import { computed, toRefs, ref } from "vue";

/* ===========================
   Props / Emits (그대로)
=========================== */
const props = defineProps({
  place: { type: Object, default: null },
  index: { type: Number, required: true },       // day 내부 index
  total: { type: Number, required: true },
  dayIndex: { type: Number, required: true },

  // 🔥 핵심
  globalIndex: { type: Number, required: true },
  globalActiveIndex: { type: Number, required: true },
});

const isDone = computed(() => props.place.isEnded);

const isCurrent = computed(() => {
  return props.globalIndex === props.globalActiveIndex;
});

const isLocked = computed(() => {
  return !isDone.value && !isCurrent.value;
});

const cardRef = ref(null);

const emit = defineEmits(["update:index", "complete"]);
const { place, index, total } = toRefs(props);

/* ===========================
   Handlers (그대로)
=========================== */
const onClickCard = () => {
  if (isLocked.value) {
    cardRef.value?.classList.add("shake");
    setTimeout(() => {
      cardRef.value?.classList.remove("shake");
    }, 300);
    return;
  }

  emit("complete", place.value);
};
/* ===========================
   Utils (그대로)
=========================== */
const formatTime = (iso) => {
  if (!iso) return "";
  return new Date(iso).toLocaleTimeString("ko-KR", {
    hour: "2-digit",
    minute: "2-digit",
    hour12: false,
  });
};



/* ===========================
   UI용 progress
=========================== */
const progressPercent = computed(() => {
  if (!total.value || total.value === 0) return 0;

  // index는 0부터 시작하니까 +1
  return Math.round(((index.value + 1) / total.value) * 100);
});


/* ===========================
   Time-based background
=========================== */
const hour = new Date().getHours();

const bgGradient = computed(() => {
  // 기본값 (밤)
  let from = "rgba(16, 14, 40, 0.94)";
  let to = "rgba(34, 18, 70, 0.94)";

  if (hour >= 5 && hour < 10) {
    // 🌅 아침: 아주 은은한 블루 퍼플
    from = "rgba(18, 20, 48, 0.94)";
    to = "rgba(36, 28, 76, 0.94)";
  } else if (hour >= 10 && hour < 17) {
    // ☀️ 낮: 보라에 살짝 블루 기
    from = "rgba(20, 26, 56, 0.94)";
    to = "rgba(38, 30, 82, 0.94)";
  } else if (hour >= 17 && hour < 20) {
    // 🌇 저녁: 퍼플 + 살짝 웜톤
    from = "rgba(32, 20, 52, 0.94)";
    to = "rgba(54, 24, 78, 0.94)";
  }

  return {
    background: `linear-gradient(135deg, ${from}, ${to})`,
  };
});
</script>

<style scoped>
/* =================================================
   NOW ACTIVE CARD (스크린샷 스타일: 가로 글래스 카드)
================================================= */
.now-active-card {
  width: calc(100% - 48px);
  margin: 16px 24px 22px;
  border-radius: 22px;
  padding: 18px 20px 14px;
  position: relative;
  overflow: hidden;
  cursor: pointer;

  background:
    linear-gradient(135deg,
      rgba(139, 92, 246, 0.95),
      rgba(168, 85, 247, 0.95),
      rgba(217, 70, 239, 0.95));

  border: 2px solid rgba(255, 255, 255, 0.3);
  backdrop-filter: blur(15px);
  -webkit-backdrop-filter: blur(15px);
}

/* 카드 내부 ‘도트’ 분위기 (사진처럼 은은하게) */
.now-active-card::after {
  content: "";
  position: absolute;
  inset: 0;
  pointer-events: none;
  opacity: .35;

  background-image:
    radial-gradient(rgba(255, 255, 255, .22) 1px, transparent 1px);
  background-size: 18px 18px;
  background-position: center;
  mask-image: radial-gradient(closest-side, rgba(0, 0, 0, .9), transparent 68%);
  -webkit-mask-image: radial-gradient(closest-side, rgba(0, 0, 0, .9), transparent 68%);
  animation: sparkle-stars 3s ease-in-out infinite;
}

/* 화려한 빛 오버레이 */
.now-active-card::before {
  content: "";
  position: absolute;
  inset: 0;
  pointer-events: none;
  background:
    radial-gradient(circle at 30% 40%, rgba(255, 255, 255, 0.3), transparent 50%),
    radial-gradient(circle at 70% 60%, rgba(255, 182, 255, 0.25), transparent 50%),
    radial-gradient(circle at 50% 50%, rgba(192, 132, 252, 0.2), transparent 70%);
  animation: glow-pulse 4s ease-in-out infinite;
}

/* =========================
   TOP ROW
========================= */
.now-top {
  display: flex;
  align-items: center;
  gap: 10px;
  position: relative;
  z-index: 2;
}

.now-pill {
  font-size: 12px;
  letter-spacing: .12em;
  padding: 6px 10px;
  border-radius: 999px;
  background: rgba(255, 255, 255, .14);
  border: 1px solid rgba(255, 255, 255, .22);
  color: rgba(255, 255, 255, .92);
}

.dot {
  width: 8px;
  height: 8px;
  border-radius: 999px;
  background: rgba(150, 255, 210, .95);
  box-shadow: 0 0 0 6px rgba(150, 255, 210, .14);
}

.dot.done {
  background: rgba(170, 255, 120, .95);
  box-shadow: 0 0 0 6px rgba(170, 255, 120, .14);
}

.dot.current {
  background: rgba(150, 255, 210, .95);
  box-shadow: 0 0 0 6px rgba(150, 255, 210, .14);
}

.dot.locked {
  background: rgba(200, 200, 200, .9);
  box-shadow: none;
}

.status {
  font-size: 13px;
  color: rgba(255, 255, 255, .84);
}

/* nav: 최대한 얇게, 우측 정렬 */
.now-nav {
  margin-left: auto;
  display: flex;
  align-items: center;
  gap: 8px;
}

.now-nav-btn {
  width: 26px;
  height: 26px;
  border-radius: 999px;
  border: 1px solid rgba(255, 255, 255, .22);
  background: rgba(255, 255, 255, .08);
  color: rgba(255, 255, 255, .92);
  font-size: 18px;
  font-weight: 700;
  cursor: pointer;
  line-height: 1;
}

.now-nav-btn:disabled {
  opacity: 0.35;
  cursor: default;
}

.now-nav-count {
  font-size: 12px;
  color: rgba(255, 255, 255, .62);
}

/* =========================
   TITLE / SUB (센터 정렬)
========================= */
.now-title {
  margin: 14px 0 8px;
  text-align: center;
  font-size: 26px;
  line-height: 1.15;
  font-weight: 850;
  color: rgba(255, 255, 255, .96);
  text-shadow: 0 10px 26px rgba(0, 0, 0, .28);
  position: relative;
  z-index: 2;
}

.now-sub {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 10px;
  font-size: 14px;
  color: rgba(255, 255, 255, .82);
  margin-bottom: 12px;
  position: relative;
  z-index: 2;
}

.sep {
  opacity: .6;
}

.truncate {
  max-width: 44ch;
  overflow: hidden;
  white-space: nowrap;
  text-overflow: ellipsis;
}

/* =========================
   PROGRESS (바닥에 길게)
========================= */
.progress {
  height: 6px;
  border-radius: 999px;
  background: rgba(255, 255, 255, .14);
  overflow: hidden;
  position: relative;
  z-index: 2;
}

.bar {
  height: 100%;
  border-radius: 999px;
  background: rgba(255, 255, 255, .82);
  box-shadow: 0 10px 26px rgba(255, 255, 255, .16);
  transition: width .35s ease;
}

/* 별 반짝임 애니메이션 */
@keyframes sparkle-stars {

  0%,
  100% {
    opacity: 0.5;
  }

  50% {
    opacity: 1;
  }
}

/* 빛 반짝임 애니메이션 */
@keyframes glow-pulse {
  0% {
    opacity: 0.6;
    transform: scale(1);
  }

  50% {
    opacity: 1;
    transform: scale(1.05);
  }

  100% {
    opacity: 0.6;
    transform: scale(1);
  }
}

/* 미래 일정 비활성화 UX */
.now-active-card.disabled {
  opacity: 0.85;
  /*  거의 유지 */
  filter: grayscale(0.15);
  /*  최소만 */
  transform: none;
}

.lock-badge {
  margin-left: 6px;
  font-size: 14px;
  opacity: 0.9;
  animation: lock-pulse 1.8s ease-in-out infinite;
}

@keyframes lock-pulse {
  0% {
    transform: scale(1);
    opacity: 0.9;
  }

  50% {
    transform: scale(1.08);
    opacity: 1;
  }

  100% {
    transform: scale(1);
    opacity: 0.9;
  }
}

@keyframes lock-shake {
  0% {
    transform: translateX(0);
  }

  25% {
    transform: translateX(-3px);
  }

  50% {
    transform: translateX(3px);
  }

  75% {
    transform: translateX(-3px);
  }

  100% {
    transform: translateX(0);
  }
}

.now-active-card.shake {
  animation: lock-shake 0.3s ease;
}
</style>
