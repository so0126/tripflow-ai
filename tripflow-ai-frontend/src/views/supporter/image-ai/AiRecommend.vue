<template>
  <section class="image-result-page card rounded-0 h-100 d-flex flex-column">
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
          <p class="sub mb-0">당신의 사진으로 여행 장소를 찾아보아요!</p>
        </div>
      </div>
    </header>

    <!-- Body -->
    <main class="page-body">
      <BaseSection
        icon="bi bi-images"
        title="AI 분석 완료"
        :subtitle="`유사한 장소 ${results.length}개`"
      >
        <!-- Loading -->
         <div v-if="isLoading" class="loading-area">
             <div class="spinner-border text-primary" role="status"></div>
             <p class="mt-3 text-muted">AI가 분석 중입니다...</p>
            </div>

        <!-- Results -->
        <template v-else>
          <h5 class="mb-3">추천된 목적지</h5>

          <div class="results-list">
            <div
              v-for="(r, i) in results"
              :key="i"
              class="result-card card mb-3 p-3"
              :class="{ selected: selectedIndex === i }"
              @click="select(i)"
              role="button"
              tabindex="0"
            >
              <div class="d-flex align-items-center">
                <div class="thumb-wrap me-3 position-relative">
                  <div class="thumb-bg">
                    <img
                      :src="r.imageUrl || '/sample/placeholder.jpg'"
                      class="thumb"
                      alt="thumb"
                      v-img-fallback="fallbacks"
                    />
                  </div>
                  <div class="match-badge">
                    {{ Math.round((r.confidence || 0) * 100) }}%
                  </div>
                </div>

                <div class="flex-fill">
                  <div class="fw-semibold">{{ r.placeName }}</div>
                  <div class="small text-muted mt-1">
                    {{ r.description || r.association }}
                  </div>

                  <div class="small text-muted mt-2">
                    <i class="bi bi-geo-alt me-1"></i>{{ r.address }}
                    <span class="ms-3 text-purple">
                      신뢰도 {{ Math.round((r.confidence || 0) * 100) }}%
                    </span>
                  </div>
                </div>
              </div>

              <div v-if="selectedIndex === i" class="select-check">✓</div>
            </div>
          </div>
        </template>
      </BaseSection>
    </main>

    <!-- Footer -->
    <footer class="page-footer">
      <NavigationButtons
        back-text="뒤로가기"
        next-text="적용하기"
        :is-next-disabled="selectedIndex === null"
        @back="goBack"
        @next="addPlan"
      />
    </footer>
  </section>
</template>


<script setup>
import { ref, onMounted } from "vue";
import { useRouter, useRoute } from "vue-router";
import { useSupporterStore } from "@/store/supporterStore";
import { useImageSearchStore } from "@/store/imageSearchStore";
import imageSearchApi from "@/api/imageSearchApi";
import BaseSection from "@/components/common/BaseSection.vue";
import NavigationButtons from "@/components/common/button/NavigationButtons.vue";

const router = useRouter();
const route = useRoute();
const supporterStore = useSupporterStore();
const imageSearchStore = useImageSearchStore();

const results = ref([]);
const selectedIndex = ref(null);
const isLoading = ref(true);

const fallbacks = [
  "/images/01.png",
  "/images/02.png",
  "/images/03.png",
];

const goBack = () => {
  router.push({ name: "CreateNewSearch" });
};

onMounted(async () => {
  try {
    const preview = route.query.preview;
    const type = route.query.type;
    const address = supporterStore.getUserAddress || "Seoul, South Korea";

    const file = base64ToFile(preview, "image.jpg");
    const res = await imageSearchApi.recommendPlacesByImage(type, file, address);

    results.value = res.data || [];
    imageSearchStore.setSearchResult(preview, type, results.value);
  } catch {
    results.value = [];
  } finally {
    isLoading.value = false;
  }
});

const select = (i) => {
  selectedIndex.value = selectedIndex.value === i ? null : i;
  imageSearchStore.setSelectedPlace(
    selectedIndex.value !== null ? results.value[i] : null
  );
};

const addPlan = () => {
  if (selectedIndex.value === null) return;
  router.push({ name: "SelectPlan" });
};

function base64ToFile(base64, name) {
  const arr = base64.split(",");
  const mime = arr[0].match(/:(.*?);/)[1];
  const bstr = atob(arr[1]);
  const u8arr = new Uint8Array(bstr.length);
  for (let i = 0; i < bstr.length; i++) u8arr[i] = bstr.charCodeAt(i);
  return new File([u8arr], name, { type: mime });
}
</script>

<style scoped>
.image-result-page {
  background: #fff;
  overflow: hidden;
}

.loading-area {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-height: 420px;
}

/* Header */
.page-header {
  height: 96px;
  padding: 1.4rem 2rem;
  border-bottom: 1px solid #e2e8f0;
  display: flex;
  align-items: center;
}

/* Body */
.page-body {
  flex: 1;
  display: flex;              /* 🔥 내부 정렬 제어 */
  flex-direction: column;
  overflow-y: auto;
  padding: 2.2rem 2rem 3rem;
}

/* Footer */
.page-footer {
  border-top: 1px solid #e2e8f0;
  padding: 1.25rem 2rem 1.75rem;
  background: #fff;
}

/* Result cards */
.result-card {
  border-radius: 12px;
  border: 1px solid #f3e8ff;
  cursor: pointer;
  transition: all 0.2s ease;
  position: relative;
}

.result-card.selected {
  border-color: #1b3b6f;
  background: #f3f7ff;
  transform: translateY(-3px);
}

.select-check {
  position: absolute;
  right: 16px;
  top: 50%;
  transform: translateY(-50%);
  background: #1b3b6f;
  color: #fff;
  width: 34px;
  height: 34px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 700;
}

.thumb-wrap {
  width: 84px;
}

.thumb-bg {
  width: 84px;
  height: 84px;
  border-radius: 10px;
  overflow: hidden;
}

.thumb {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.match-badge {
  position: absolute;
  top: 6px;
  left: 6px;
  background: #1b3b6f;
  color: #fff;
  font-size: 12px;
  padding: 4px 8px;
  border-radius: 999px;
}

.text-purple {
  color: #1b3b6f;
  font-weight: 600;
}
</style>
