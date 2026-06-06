<template>
  <TravelgramHeader :back-route="{ name: 'CaptionSelect' }" />

    
<div class="travelgram-page">
      <div class="page-inner">

    <section class="hashtag-section">
      <!-- 가이드 -->
      <h5 class="guide-title">
        <i class="bi bi-hash me-2"></i>
        해시태그 관리
      </h5>
      <p class="guide-subtitle">
        AI가 추천한 태그를 선택하거나, 나만의 태그를 직접 추가해보세요.
      </p>

      <!-- 🟠 내가 선택한 태그 -->
      <p v-if="selectedCount > 0" class="section-label">
        내가 선택한 태그
      </p>

      <div v-if="selectedCount > 0" class="tag-cloud-box selected-box">
        <div class="tag-list">
          <ReviewTagPill
            v-for="tagName in Array.from(selectedSet)"
            :key="'selected-' + tagName"
            variant="selected"
            :selected="true"
            :label="tagName"
            @click="toggleTag(tagName)"
          />
        </div>
      </div>

      <!-- ➕ 커스텀 태그 입력 -->
      <div class="input-area">
        <div class="input-wrapper">
          <span class="prefix">#</span>
          <input
            type="text"
            v-model="newTagInput"
            placeholder="직접 태그 추가 (예: seoul, travel)"
            @keyup.enter="addCustomTag"
          />
          <button
            class="btn-add"
            @click="addCustomTag"
            :disabled="!newTagInput.trim()"
          >
            <i class="bi bi-plus-lg"></i>
          </button>
        </div>
      </div>

      <!-- 🔵 AI 추천 태그 (❗ AI 전용 ❗) -->
      <p class="section-label">AI가 추천한 태그</p>

      <div class="tag-cloud-box">
        <div class="tag-list">
          <ReviewTagPill
            v-for="(tag, index) in aiTags"
            :key="'ai-' + index"
            :variant="isSelected(tag.name) ? 'active' : 'default'"
            :selected="isSelected(tag.name)"
            :label="tag.name"
            @click="toggleTag(tag.name)"
          />
        </div>

        <p class="count-text">
          <span class="text-orange">{{ selectedCount }}</span> tags selected
        </p>
      </div>
    </section>

    <NavigationButtons
      backText="뒤로가기"
      nextText="다음으로"
      :isNextDisabled="selectedCount === 0"
      @back="goBack"
      @next="goNext"
    />
    </div>
</div>
</template>

<script setup>
import TravelgramHeader from '@/components/travelgram/TravelgramHeader.vue'
import { useRouter } from 'vue-router'
import { useReviewStore } from '@/store/reviewStore'
import api from '@/api/travelgramApi'

import NavigationButtons from '@/components/common/button/NavigationButtons.vue'
import { useReviewHashtagEditor } from '@/composables/travelgram/review/useReviewHashtagEditor'
import ReviewTagPill from '@/components/travelgram/review/ReviewTagPill.vue'

const router = useRouter()
const reviewStore = useReviewStore()
const {
  aiTags,
  selectedSet,
  newTagInput,
  selectedCount,
  isSelected,
  toggleTag,
  addCustomTag,
  goBack,
  goNext,
} = useReviewHashtagEditor({ reviewStore, api, router })
</script>

<style scoped>
/* ================= Page Background ================= */
.travelgram-page {
  min-height: 100vh;
  display: flex;
  justify-content: center;
}

/* ================= Content Width ================= */
.page-inner {
  width: 100%;
  max-width: 1200px;
  padding: 50px 16px 32px;
}
.hashtag-section {
  margin-top: 1.5rem;
}

.guide-title {
  color: #1b3b6f;
  font-weight: 700;
  margin-bottom: 0.5rem;
}

.guide-title i {
  color: #ff6b6b; /* 🔥 포인트 컬러 */
  font-size: 1.1em;
}
.guide-subtitle {
  color: #868e96;
  margin-bottom: 2rem;
  line-height: 1.5;
}

.section-label {
  font-weight: 700;
  color: #1b3b6f;
  margin: 2.5rem 0 0.75rem;
}

.tag-cloud-box {
  background: white;
  border-radius: 1.5rem;
  padding: 1.75rem;
  box-shadow: 0 6px 24px rgba(27, 59, 111, 0.06);
}

.selected-box {
  background: #fff7ef;
  border: 1px solid #ffd8b5;
  margin-bottom: 2.5rem;
}

.tag-list {
  display: flex;
  flex-wrap: wrap;
  gap: 0.6rem;
}

.tag-pill {
  background: #f1f3f5;
  color: #868e96;
  border-radius: 999px;
  padding: 0.5rem 1rem;
  border: none;
  cursor: pointer;
  display: flex;
  align-items: center;
  transition: all 0.2s ease;
}

.tag-pill.active {
  background-color: #1b3b6f;
  color: white;
  box-shadow: 0 4px 12px rgba(27, 59, 111, 0.3);
}

.tag-pill.selected {
  background-color: #ff8c00;
  color: white;
  box-shadow: 0 4px 12px rgba(255, 140, 0, 0.3);
}

.pop {
  animation: pop 0.25s ease;
}

@keyframes pop {
  0% { transform: scale(0.9); opacity: 0.7; }
  100% { transform: scale(1); opacity: 1; }
}

.input-area {
  margin: 2.5rem 0 2rem;
}

.input-wrapper {
  display: flex;
  align-items: center;
  background: white;
  border: 2px solid #e9ecef;
  border-radius: 1.2rem;
  padding: 0.6rem 1.1rem;
}

.prefix {
  font-weight: 600;
  color: #adb5bd;
  margin-right: 0.5rem;
}

.input-wrapper input {
  flex: 1;
  border: none;
  outline: none;
}

.btn-add {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  border: none;
  background: #f1f3f5;
  cursor: pointer;
}

.btn-add:hover:not(:disabled) {
  background: #1b3b6f;
  color: white;
}

.count-text {
  text-align: right;
  color: #adb5bd;
  margin-top: 1rem;
}

.text-orange {
  color: #ff8c00;
  font-weight: 700;
}
</style>
