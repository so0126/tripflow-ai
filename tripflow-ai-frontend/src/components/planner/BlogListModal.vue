<template>
  <div v-if="isOpen" class="modal-overlay" @click="close">
    <div class="modal-content" @click.stop>
      <div class="modal-header">
        <h5 class="title">
          🌱 <span class="highlight">{{ keyword }}</span> 검색 결과
        </h5>
        <button class="close-btn" @click="close">
          <i class="bi bi-x-lg"></i>
        </button>
      </div>

      <div class="modal-body">
        <div v-if="isLoading" class="loading-state">
          <div class="spinner-border text-primary" role="status"></div>
          <p>블로그 리뷰를 찾아보고 있어요...</p>
        </div>

        <div v-else-if="items.length === 0" class="empty-state">
          검색 결과가 없습니다.
        </div>

        <ul v-else class="blog-list">
          <li v-for="(item, index) in items" :key="index" class="blog-item" @click="openLink(item.link)">
            <div class="item-title" v-html="item.title"></div>
            <div class="item-desc" v-html="item.description"></div>
            <div class="item-date">{{ formatDate(item.postdate) }}</div>
          </li>
        </ul>
      </div>
    </div>
  </div>
</template>

<script setup>
const props = defineProps({
  isOpen: Boolean,
  isLoading: Boolean,
  items: Array,
  keyword: String
});

const emit = defineEmits(['close']);

const close = () => {
  emit('close');
};

const openLink = (url) => {
  window.open(url, '_blank');
};

// 날짜 포맷 (20231225 -> 2023.12.25)
const formatDate = (dateStr) => {
  if (!dateStr) return '';
  return `${dateStr.substring(0, 4)}.${dateStr.substring(4, 6)}.${dateStr.substring(6, 8)}`;
};
</script>

<style scoped>
.modal-overlay {
  position: fixed; top: 0; left: 0; width: 100%; height: 100%;
  background: rgba(0, 0, 0, 0.5); z-index: 2000;
  display: flex; justify-content: center; align-items: center;
}

.modal-content {
  background: white; width: 90%; max-width: 600px; max-height: 80vh;
  border-radius: 16px; display: flex; flex-direction: column;
  box-shadow: 0 10px 25px rgba(0,0,0,0.2);
  overflow: hidden;
}

.modal-header {
  padding: 16px 20px; border-bottom: 1px solid #eee;
  display: flex; justify-content: space-between; align-items: center;
  background-color: #f8f9fa;
}

.title { margin: 0;  font-weight: bold; }
.highlight { color: #ff8c00; }

.close-btn {
  background: none; border: none;  cursor: pointer; color: #666;
}

.modal-body { flex: 1; overflow-y: auto; padding: 0; }

.loading-state, .empty-state {
  padding: 40px; text-align: center; color: #888;
}

.blog-list { list-style: none; padding: 0; margin: 0; }

.blog-item {
  padding: 16px 20px; border-bottom: 1px solid #f1f1f1; cursor: pointer;
  transition: background 0.2s;
}
.blog-item:hover { background-color: #f9f9f9; }

.item-title { font-weight: bold;  margin-bottom: 6px; color: #333; }
.item-desc {  color: #666; line-height: 1.4; display: -webkit-box; -webkit-line-clamp: 2; -webkit-box-orient: vertical; overflow: hidden; }
.item-date { color: #aaa; margin-top: 8px; text-align: right; }

/* 네이버 API 검색어 하이라이트 스타일 */
:deep(b) { color: #e56d00; font-weight: 800; }
</style>