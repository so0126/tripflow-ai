<template>
  <section class="new-search-page card rounded-0 h-100 d-flex flex-column">
    <!-- Header -->
    <header class="page-header">
      <div class="d-flex gap-3 align-items-center">
        <button
          class="btn btn-link p-0 back-button"
          @click="$router.back()"
          title="뒤로 가기"
        >
          <i class="bi bi-arrow-left-short fs-1"></i>
        </button>

        <div class="icon-box">📷</div>

        <div>
          <h5 class="mb-1 title">이미지 기반 여행 AI</h5>
          <p class="sub mb-0">
            사진을 올리면 관련된 장소를 추천해드립니다.
          </p>
        </div>
      </div>
    </header>

    <!-- Body -->
    <main class="page-body">
      <!-- How it works -->
      <div class="how-works-box mb-4">
        <div class="how-works-header">
          <i class="bi bi-lightbulb text-warning me-2"></i>
          <strong>어떻게 동작하나요?</strong>
        </div>

        <ol class="how-works-list mb-0">
          <li>여행 중 궁금한 점을 사진으로 올려보세요.</li>
          <li>AI가 이미지를 분석합니다.</li>
          <li>사진과 관련된 장소 추천을 받아보세요.</li>
        </ol>
      </div>

      <!-- Upload Area -->
      <section class="upload-section mt-4">
        <div
          v-if="!imagePreview"
          class="upload-box"
          @click="triggerFileInput"
          @dragover.prevent
          @drop.prevent="onDrop"
        >
          <i class="bi bi-cloud-arrow-up fs-2 text-secondary mb-2"></i>
          <p class="text-secondary mb-0">클릭해서 사진을 업로드하세요.</p>
          <small class="text-muted">
            사진 크기는 10MB까지 가능하며, JPG, PNG만 올려주세요.
          </small>
        </div>

        <!-- Preview -->
        <div v-else class="preview-full" @click="triggerFileInput">
          <img :src="imagePreview" alt="preview" />
          <div class="preview-overlay">
            <i class="bi bi-arrow-repeat fs-3"></i>
            <p class="mb-0 mt-2">클릭하여 다른 사진 선택</p>
          </div>
        </div>
      </section>

      <!-- Hidden Input -->
      <input
        id="imageInput"
        type="file"
        accept="image/*"
        ref="fileInput"
        class="d-none"
        @change="onFileChange"
      />
    </main>

    <!-- Footer -->
    <footer class="page-footer">
      <NavigationButtons
        back-text="취소"
        :is-next-disabled="!imagePreview"
        @back="handleCancel"
        @next="goToType"
      >
        <template #next-content>
          <i class="bi bi-arrow-right-circle me-2"></i>
          사진 유형 선택
        </template>
      </NavigationButtons>
    </footer>
  </section>
</template>

<script setup>
import { ref } from "vue";
import { useRoute, useRouter } from "vue-router";
import NavigationButtons from "@/components/common/button/NavigationButtons.vue";

const route = useRoute();
const router = useRouter();

const imagePreview = ref(null);
const fileInput = ref(null);

const triggerFileInput = () => fileInput.value?.click();

const handleCancel = () => {
  imagePreview.value = null;
  if (fileInput.value) fileInput.value.value = "";
};

const onFileChange = (e) => {
  const f = e.target.files && e.target.files[0];
  if (!f) return;

  const reader = new FileReader();
  reader.onload = (ev) => {
    imagePreview.value = ev.target.result;
  };
  reader.readAsDataURL(f);
};

const onDrop = (e) => {
  const f = e.dataTransfer.files && e.dataTransfer.files[0];
  if (!f) return;

  const reader = new FileReader();
  reader.onload = (ev) => {
    imagePreview.value = ev.target.result;
  };
  reader.readAsDataURL(f);
};

const goToType = () => {
  if (!imagePreview.value) return;
  router.push({
    name: "ImageType",
    query: { preview: imagePreview.value, from: "CreateNewSearch" },
  });
};
</script>

<style scoped>
/* ==========================
   Page Layout (Header/Body/Footer)
   ========================== */
.new-search-page {
  background: #ffffff;
  overflow: hidden; /* body만 스크롤 */
  border-radius: 0;
}

/* Header */
.page-header {
  height: 96px;
  padding: 1.4rem 2rem 1.1rem;
  border-bottom: 1px solid #e2e8f0;
  display: flex;
  align-items: center;
  background: #ffffff;
}

.icon-box {
  width: 42px;
  height: 42px;
  border-radius: 10px;
  background: #f8fafc;
  border: 1px solid #e2e8f0;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 1.25rem;
}

/* Title/Sub */
.title {
  font-size: 1.15rem;
  font-weight: 800;
  color: #1e293b;
  letter-spacing: -0.02em;
  margin: 0;
}

.sub {
  color: #64748b;
  font-size: 0.82rem;
  font-weight: 500;
}

/* Back button */
.back-button {
  color: #2d4a8f;
  text-decoration: none;
  transition: all 0.25s ease;
  margin-left: -0.5rem;
}

.back-button:hover {
  color: #1a2a56;
  transform: translateX(-4px);
}

.back-button:focus {
  outline: none;
  box-shadow: none;
}

/* Body (scroll area) */
.page-body {
  flex: 1;
  overflow-y: auto;
  padding: 2.25rem 2rem 2.75rem; /* 🔥 헤더/푸터와 “숨통” */
  background: #ffffff;

  scrollbar-width: thin;
  scrollbar-color: #cbd5e1 #ffffff;
}

.page-body::-webkit-scrollbar {
  width: 6px;
}
.page-body::-webkit-scrollbar-track {
  background: #ffffff;
  border-radius: 3px;
}
.page-body::-webkit-scrollbar-thumb {
  background: #cbd5e1;
  border-radius: 3px;
}
.page-body::-webkit-scrollbar-thumb:hover {
  background: #94a3b8;
}

/* How it works */
.how-works-box {
  background: linear-gradient(135deg, #ffffff 0%, #f8fafc 100%);
  border-radius: 14px;
  border: 2px solid #e2e8f0;
  padding: 1.5rem;
  box-shadow: 0 2px 8px rgba(45, 74, 143, 0.05);
}

.how-works-header {
  color: #2d4a8f;
  font-size: 1.05rem;
  font-weight: 800;
  display: flex;
  align-items: center;
  margin-bottom: 0.75rem;
}

.how-works-list {
  padding-left: 1.1rem;
  line-height: 1.9;
  color: #475569;
  font-size: 0.92rem;
}
.how-works-list li {
  margin-bottom: 0.55rem;
  font-weight: 500;
}
.how-works-list li:last-child {
  margin-bottom: 0;
}

/* Upload section */
.upload-section {
  background-color: #f9fafc;
  border-radius: 1rem;
  padding: 1.5rem;
  border: 1px solid #eef2f7;

  
}

.upload-box {
  border: 2px dashed #d0d5dd;
  border-radius: 0.85rem;
  height: 400px;
  padding: 2rem;
  text-align: center;
  background-color: #fff;
  cursor: pointer;
  transition: background-color 0.25s ease, border-color 0.25s ease;

  /* 🔥 여기 추가 */
  display: flex;
  flex-direction: column;
  align-items: center;     /* 가로 중앙 */
  justify-content: center; /* 세로 중앙 */
  text-align: center;
}

.upload-box:hover {
  background-color: #fef8f2;
  border-color: #2d4a8f;
}

.preview-full {
  position: relative;
  width: 100%;
  height: 380px;       /* 🔥 기본 고정 */
  max-height: 380px;
  border-radius: 0.85rem;
  overflow: hidden;
  cursor: pointer;
  border: 2px solid #e2e8f0;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
}

.preview-full img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  object-position: center;
  display: block;
}

.preview-overlay {
  position: absolute;
  inset: 0;
  background: rgba(45, 74, 143, 0.9);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  opacity: 0;
  transition: opacity 0.25s ease;
  color: #fff;
}
.preview-full:hover .preview-overlay {
  opacity: 1;
}
.preview-overlay p {
  font-size: 0.95rem;
  font-weight: 700;
}

/* Footer */
.page-footer {
  border-top: 1px solid #e2e8f0;
  background: #ffffff;
  padding: 1.25rem 2rem 1.75rem; /* 🔥 바닥에 딱 붙는 느낌 제거 */
}

/* Disabled button */
button:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

/* Small text */
.text-muted {
  color: #64748b !important;
}

/* Responsive */
@media (max-width: 768px) {
  .page-header {
    padding: 1.2rem 1.25rem 1rem;
  }
  .page-body {
    padding: 1.6rem 1.25rem 2.2rem;
  }
  .page-footer {
    padding: 1.1rem 1.25rem 1.4rem;
  }
  .preview-full {
    height: 320px;
    max-height: 320px;
  }
}
</style>
