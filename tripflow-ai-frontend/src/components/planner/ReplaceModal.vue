<template>
  <teleport to="body">
    <div v-if="open" class="modal-backdrop" @click="$emit('close')">
      <div class="modal-card" @click.stop>
        <!-- Header -->
        <div class="modal-header">
          <h3 class="modal-title">
            정말 <strong>{{ target?.title || "이 장소" }}</strong>를 삭제하시겠습니까?
          </h3>
          <button class="icon-close" @click="$emit('close')">✕</button>
        </div>

        <!-- Description -->
        <h5 class="modal-subdesc">
          <strong>저희가 유사한 장소를 추천해드릴게요</strong>
        </h5>

        <!-- Alternatives -->
        <div class="alt-list">
          <div
            v-for="(alt, i) in alternatives"
            :key="'alt-' + i"
            class="alt-card"
            @click="$emit('preview-alt', alt)"
          >
            <div class="alt-row">
              <!-- Thumbnail -->
              <div class="alt-thumb">
                <img
                  v-if="alt.image || alt.thumbnail"
                  :src="alt.image || alt.thumbnail"
                  alt="place thumbnail"
                />
                <img
                  v-else
                  :src="assignedImages[i]"
                  alt="cafe thumbnail"
                />
              </div>

              <!-- Body -->
              <div class="alt-body">
                <div class="alt-title">{{ alt.title }}</div>

                <!-- 👇 장소 한줄 설명 -->
                <div class="alt-desc">
                  {{ alt.shortDesc || alt.desc || "Recommended nearby place" }}
                </div>
              </div>

              <!-- CTA -->
              <button
                class="replace-cta"
                @click.stop="handleReplacement(alt, i)"
              >
                장소 변경
              </button>
            </div>
          </div>
        </div>

        <!-- Footer -->
        <div class="modal-footer">
          <button class="btn-cancel" @click="emit('close')">취소</button>
          <button class="btn-delete" @click="emit('delete-anyway')">
            그래도 삭제하기
          </button>
        </div>
      </div>
    </div>
  </teleport>
</template>


<script setup>
import { computed } from 'vue';

const props = defineProps({
  open: { type: Boolean, default: false },
  target: { type: Object, default: null },
  alternatives: { type: Array, default: () => [] },
});

const emit = defineEmits(["close", "preview-alt", "apply-replacement", "delete-anyway"]);

// 카페 더미 이미지 URL 배열 (Unsplash - 무료 사용 가능)
const dummyCafeImages = [
  'https://images.unsplash.com/photo-1554118811-1e0d58224f24?w=400',  // 모던 카페
  'https://images.unsplash.com/photo-1501339847302-ac426a4a7cbb?w=400',  // 카페 인테리어
  'https://images.unsplash.com/photo-1559925393-8be0ec4767c8?w=400',  // 커피숍
  'https://images.unsplash.com/photo-1453614512568-c4024d13c247?w=400',  // 카페 테이블
  'https://images.unsplash.com/photo-1511920170033-f8396924c348?w=400',  // 카페 외관
  'https://images.unsplash.com/photo-1442512595331-e89e73853f31?w=400',  // 카페 내부
  'https://images.unsplash.com/photo-1495474472287-4d71bcdd2085?w=400',  // 커피와 노트북
  'https://images.unsplash.com/photo-1521017432531-fbd92d768814?w=400',  // 카페 분위기
  'https://images.unsplash.com/photo-1509042239860-f550ce710b93?w=400',  // 커피 클로즈업
  'https://images.unsplash.com/photo-1445116572660-236099ec97a0?w=400',  // 카페 음료
];

// alternatives 개수만큼 중복 없이 이미지 할당
const assignedImages = computed(() => {
  // 이미지 배열을 섞음 (Fisher-Yates shuffle)
  const shuffled = [...dummyCafeImages].sort(() => Math.random() - 0.5);
  
  // alternatives 개수만큼만 반환
  return shuffled.slice(0, props.alternatives.length);
});

// 대체 버튼 클릭 핸들러
const handleReplacement = (alt, index) => {
  console.log('🔄 대체 전 데이터:', alt);
  
  // 이미지가 없으면 화면에 보였던 더미 이미지 추가
  const altWithImage = {
    ...alt,
    image: alt.image || alt.thumbnail || assignedImages.value[index],
    thumbnail: alt.thumbnail || alt.image || assignedImages.value[index],
    details: {
      ...alt.details,
      gallery: alt.details?.gallery?.length 
        ? alt.details.gallery 
        : [assignedImages.value[index]]
    }
  };
  
  console.log('✅ 대체 후 데이터 (이미지 포함):', altWithImage);
  
  emit('apply-replacement', altWithImage);
};

const hasCost = (cost) => {
  return cost === 0 || (typeof cost === "number" && !Number.isNaN(cost));
};

const formatCost = (cost) => {
  if (cost === 0) return "Free";
  if (typeof cost === "number") return `$${cost}`;
  return "";
};
</script>

<style scoped>
/* ===============================
   Overlay
=============================== */
.modal-backdrop {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.35);
  display: flex;
  align-items: flex-start;
  justify-content: center;
  padding: 7vh 16px;
  z-index: 10000;
}

/* ===============================
   Modal Card
=============================== */
.modal-card {
  width: min(560px, 94vw);
  background: #ffffff;
  border-radius: 20px;
  padding: 24px 22px 20px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.22);
  animation: pop 0.18s ease;
}

@keyframes pop {
  from {
    transform: translateY(-8px);
    opacity: 0.9;
  }
  to {
    transform: translateY(0);
    opacity: 1;
  }
}

/* ===============================
   Header
=============================== */
.modal-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 14px;
}

.modal-title {
  font-size: 20px;
  font-weight: 800;
  margin: 0;
  color: #0f172a;
}

.modal-title strong {
  color: #ef4444;
}

.icon-close {
  border: none;
  background: transparent;
  font-size: 22px;
  line-height: 1;
  cursor: pointer;
  color: #94a3b8;
  padding: 6px;
}

.icon-close:hover {
  color: #64748b;
}

/* ===============================
   Copy Text
=============================== */
.modal-desc {
  margin: 0 0 12px;
  color: #334155;
  font-size: 15px;
  line-height: 1.6;
}

.modal-subdesc {
  margin: 0 0 18px;
  color: #64748b;
  font-size: 14.5px;
}

/* ===============================
   Alternatives List
=============================== */
.alt-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.alt-card {
  border: 1px solid #e5e7eb;
  border-radius: 16px;
  padding: 16px;
  background: #ffffff;
  cursor: pointer;
}

.alt-card:hover {
  /* background: #fafafa; */
}

/* ===============================
   Alternative Row
=============================== */
.alt-row {
  display: flex;
  gap: 16px;
  align-items: center;
}

/* ===============================
   Thumbnail (Image)
=============================== */
.alt-thumb {
  width: 64px;
  height: 64px;
  border-radius: 14px;
  overflow: hidden;
  background: #f1f5f9;
  flex-shrink: 0;
  display: flex;
  align-items: center;
  justify-content: center;
}

.alt-thumb img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.alt-thumb-fallback {
  font-size: 22px;
  color: #64748b;
}

/* ===============================
   Body Text
=============================== */
.alt-body {
  flex: 1;
  min-width: 0;
}

.alt-title {
  font-size: 16px;
  font-weight: 800;
  color: #0f172a;
  margin-bottom: 6px;
}

.alt-desc {
  font-size: 14px;
  color: #64748b;
  line-height: 1.55;
}

/* ===============================
   Replace CTA
=============================== */
.replace-cta {
  border: none;
  border-radius: 12px;
  padding: 10px 16px;
  font-weight: 700;
  font-size: 13px;
  cursor: pointer;
  background: #eef6ff;
  color: #2563eb;
  white-space: nowrap;
  flex-shrink: 0;
}

.replace-cta:hover {
  background: #e6f0ff;
}

/* ===============================
   Footer Buttons
=============================== */
.modal-footer {
  display: flex;
  gap: 16px;
  margin-top: 22px;
}

.btn-cancel,
.btn-delete {
  flex: 1;
  border: none;
  border-radius: 14px;
  padding: 16px;
  font-weight: 800;
  cursor: pointer;
  font-size: 15px;
}

.btn-cancel {
  background: #f3f4f6;
  color: #334155;
}

.btn-cancel:hover {
  background: #eceef2;
}

.btn-delete {
  background: #ffecec;
  color: #ef4444;
}

.btn-delete:hover {
  background: #ffe2e2;
}

</style>