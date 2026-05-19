<!-- src/components/planner/ActivityDetailsModal.vue -->
<template>

  <teleport to="body">
    <div v-if="open" class="modal-backdrop" @click="$emit('close')">
      <div class="modal-card" @click.stop>

        <!-- X 버튼 -->
        <button class="close-btn" @click="$emit('close')">✕</button>

        <!--  상단 메인 이미지 -->
        <div class="main-image-wrapper">
          <img :src="localGallery[0]" alt="thumbnail" class="main-image" v-img-fallback="fallbacks" />
        </div>

        <!-- 제목 -->
        <h3 class="activity-title">
          {{ data?.title || "Untitled Activity" }}
        </h3>

        <!--  위치 정보 info-block -->
        <div class="info-block mb-3">

          <!-- Location Section -->
          <div>
            <div class="info-header">
              <span class="info-header-icon">📌</span>
              <span class="info-header-label">장소</span>
            </div>

            <div class="info-indent">
              <div class="info-main">
                {{ data?.area || "Seoul" }}
              </div>

              <div class="info-sub">
                {{ data?.address || "No address provided" }}
              </div>
            </div>
          </div>

          <!-- 구분선 -->
          <div class="info-divider"></div>

          <!-- Description Section -->
          <div>
            <div class="info-header mt-2">
              <span class="info-header-icon">📌</span>
              <span class="info-header-label">소개</span>
            </div>

            <div class="info-indent">
              <p class="info-desc">
                {{ data?.desc || "No description provided yet." }}
              </p>
            </div>
          </div>

        </div>

        <!-- 지도 -->
        <h4 class="map-title">📍 Location on Map</h4>
        <div class="map-wrapper">
          <iframe :src="mapSrc(data)" loading="lazy" referrerpolicy="no-referrer-when-downgrade"></iframe>
        </div>
      </div>
    </div>
  </teleport>
</template>

<script setup>
import { computed } from "vue";
import defaultImg1 from "@/assets/planner/activity-default-1.jpg";

const emits = defineEmits(["close"]);

const props = defineProps({
  open: { type: Boolean, default: false },
  data: { type: Object, default: null },
});

const fallbacks = [
  "/images/01.png",
  "/images/02.png",
  "/images/03.png",
  "/images/04.png",
  "/images/05.png",
  "/images/06.png",
];

/* 첫 번째 이미지만 사용 */
const localGallery = computed(() => {
  const d = props.data;

  //  data가 완전히 null이면 기본 이미지
  if (!d) return [defaultImg1];

  //  gallery 자체가 없으면 기본 이미지
  if (!d.gallery) return [defaultImg1];

  //  gallery 배열이 비어있지 않으면 사용
  if (d.gallery.length > 0) return d.gallery;

  //  모든 조건에서 기본 이미지
  return [defaultImg1];
});

const mapSrc = (data) => {
  const q = encodeURIComponent(data?.address || data?.title || "Seoul");
  return `https://www.google.com/maps?q=${q}&output=embed`;
};
</script>

<style scoped>
/* 전체 화면 흐림 overlay */
.modal-backdrop {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.45);
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 20px;
  z-index: 2000;
}

/* 메인 카드 */
.modal-card {
  width: min(480px, 95vw);
  background: #fff;
  border-radius: 14px;
  overflow: hidden;
  position: relative;
  box-shadow: 0 10px 35px rgba(0, 0, 0, 0.25);
  animation: fadeIn 0.2s ease-out;
  display: flex;
  flex-direction: column;
}

/* X 버튼 */
.close-btn {
  position: absolute;
  top: 14px;
  right: 14px;
  border: none;
  background: #ffffffd0;
  width: 34px;
  height: 34px;
  border-radius: 50%;
  font-size: 16px;
  cursor: pointer;
}

/* 🎨 상단 이미지 */
.main-image-wrapper {
  width: 100%;
  max-height: 260px;
  overflow: hidden;
}

.main-image {
  width: 100%;
  height: auto;
  object-fit: cover;
}

/* 제목 */
.activity-title {
  font-size: 20px;
  font-weight: 700;
  padding: 18px 20px 10px;
  color: #1e293b;
}

/* info-block */
.info-block {
  background: #f5f5f5;
  margin: 0 20px;
  border-radius: 14px;
  padding: 14px 18px 10px;
}

/* 제목 줄: 아이콘 + 텍스트 */
.info-header {
  display: flex;
  align-items: center;
  gap: 6px;
  margin-bottom: 6px;
}

.info-header-icon {
  font-size: 14px;
  line-height: 1;
}

.info-header-label {
  font-size: 13px;
  font-weight: 600;
  color: #4b5563;
}

/* 들여쓰기 영역 */
.info-indent {
  margin-left: 25px;
}

/* 지역 / 주소 / 설명 정렬 */
.info-main {
  font-size: 14px;
  font-weight: 600;
  color: #111827;
  margin-bottom: 2px;
}

.info-sub {
  font-size: 12px;
  color: #6b7280;
  margin-bottom: 6px;
}

.info-desc {
  font-size: 12px;
  color: #6b7280;
  margin-bottom: 8px;
  line-height: 1.5;
}

/* hr 느낌의 얇은 구분선 */
.info-divider {
  height: 1px;
  background: #e5e7eb;
  margin: 8px 0;
}

/* 지도 */
.map-title {
  font-size: 15px;
  font-weight: 700;
  padding: 10px 20px 6px;
  color: #1e293b;
}

.map-wrapper {
  width: 100%;
  height: 220px;
  overflow: hidden;
  border-radius: 0 0 14px 14px;
  background: #e5e7eb;
}

.map-wrapper iframe {
  width: 100%;
  height: 100%;
  border: none;
}

/* Animation */
@keyframes fadeIn {
  from {
    opacity: 0;
    transform: scale(0.97);
  }

  to {
    opacity: 1;
    transform: scale(1);
  }
}
</style>
