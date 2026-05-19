<template>

  <div class="card p-4 mt-3">
    <h5 class="mb-3">존재하는 여정</h5>

    <div class="plans-accordion">
      <div v-for="(day, dIdx) in plansDays" :key="day.id"
        :class="['day-block card mb-3', { expanded: expandedDay === dIdx }]">
        <div class="day-header d-flex align-items-center p-3" :class="{ 'day-header-active': expandedDay === dIdx }"
          @click="toggleDay(dIdx)" role="button" tabindex="0">
          <div>
            <div class="day-label">Day {{ dIdx + 1 }}</div>
            <div class="fw-medium day-title">{{ day.title }}</div>
            <div class="small text-muted day-date">{{ day.date }}</div>
          </div>

          <div class="ms-auto text-end">
            <div class="small text-muted">Daily Cost</div>
            <div class="fw-semibold text-accent">${{ day.cost }}</div>
            <div class="chev mt-1">{{ expandedDay === dIdx ? '˄' : '˅' }}</div>
          </div>
        </div>

        <div v-show="expandedDay === dIdx" class="day-body p-2">
          <ul class="list-unstyled m-0">
            <!-- Top insertion row: 이제 add 모드일 때만 보임 (replace 모드일 때는 숨김)
                   top-slot에 항목이 있으면 item-added 클래스를 붙여 시각 효과를 동일하게 만듦 -->
            <li v-if="mode === 'add'"
              :class="['day-item day-item-top d-flex align-items-center p-3', { 'item-added': !!day._topItem }]">
              <template v-if="!day._topItem">
                <div class="flex-fill">
                  <div class="fw-medium text-muted">Add at beginning of the day</div>
                </div>

                <!-- reuse same add button behavior, pass index -1 => insert at top (now stored as day._topItem) -->
                <div v-if="mode === 'add'" class="action-wrap">
                  <button class="btn btn-add" @click.stop="addToItinerary(dIdx, -1)" :disabled="alreadyAddedAnywhere()"
                    :title="alreadyAddedAnywhere() ? '이미 추가된 장소입니다' : '여정의 첫 번째로 추가해보세요.'">＋</button>
                </div>
              </template>

              <template v-else>
                <div class="flex-fill">
                  <div class="fw-medium">{{ day._topItem.title }}</div>
                  <div class="small text-muted">{{ day._topItem.time }}</div>
                </div>

                <div class="me-2 d-flex align-items-center">
                  <div class="added-badge me-2">Added</div>
                  <button class="btn btn-remove" @click.stop="removeTopAdded(dIdx)" aria-label="삭제">✕</button>
                </div>
              </template>
            </li>

            <li v-for="(it, iIdx) in day.items" :key="it.id" class="day-item d-flex align-items-center p-3" :class="{
              'item-selected': it._replaced && !it._appliedReplace,
              'item-added': it._added,
              'item-replaced-success': it._replaced && it._appliedReplace
            }">
              <div class="flex-fill" @click="onItemClick(dIdx, iIdx)" role="button" tabindex="0">
                <div class="fw-medium">
                  <span :class="{ 'replaced-title': it._replaced }">{{ it.title }}</span>
                </div>
                <div class="small text-muted">{{ it.time }}</div>
              </div>

              <!-- replaced badge + undo -->
              <div v-if="it._replaced" class="me-2 d-flex align-items-center">
                <div class="replaced-badge me-2">{{ it._appliedReplace ? '대체됨' : '대체 예정' }}</div>
                <button class="btn btn-undo" @click.stop="undoReplace(dIdx, iIdx)">↺</button>
              </div>

              <!-- added badge + remove (for added items) -->
              <div v-else-if="it._added" class="me-2 d-flex align-items-center">
                <div class="added-badge me-2">Added</div>
                <button class="btn btn-remove" @click.stop="removeAdded(dIdx, iIdx)" aria-label="삭제">✕</button>
              </div>

              <!-- mode = add : show + button per item -->
              <div v-else-if="mode === 'add'" class="action-wrap">
                <button class="btn btn-add" @click.stop="addToItinerary(dIdx, iIdx)" :disabled="alreadyAddedAnywhere()"
                  :title="alreadyAddedAnywhere() ? '이미 추가된 장소입니다' : '이 곳을 추가하세요.'">＋</button>
              </div>

              <!-- mode = replace : show check button to replace THIS item -->
              <div v-else-if="mode === 'replace'" class="action-wrap">
                <button class="btn btn-replace" :class="{ active: it._replaced }" @click.stop="replaceThis(dIdx, iIdx)"
                  :title="it._replaced ? '이미 대체됨 (되돌리기 가능)' : '이 장소로 대체하세요.'">
                  ✓
                </button>
              </div>
            </li>
          </ul>
        </div>
      </div>
    </div>

    <div v-if="toast.visible" class="action-toast">{{ toast.message }}</div>

    <div class="d-flex mt-3">

      <NavigationButtons
    back-text="뒤로가기"
    next-text="완료하기"
    :is-next-disabled="selectedIndex === null"
    @back="goBack"
    @next="done"
      />
    </div>
  </div>
  <!-- </div> -->
</template>

<script setup>
import { ref, reactive, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import PageHeader from '@/components/common/header/PageHeader.vue'
import NavigationButtons from '@/components/common/button/NavigationButtons.vue';

const route = useRoute()
const router = useRouter()

const goBack = () => {
  router.push({ name: 'CreateNewSearch' });
};
// read mode and item from route.state first, then fallback to query
const mode = route?.state?.mode || route?.query?.mode || 'add'

const itemFromState = route?.state?.item
const item = itemFromState || {
  id: route?.query?.itemId || 'new-place',
  name: route?.query?.itemName || 'Unknown Place',
  thumb: '/sample/placeholder1.jpg',
  time: 'TBD'
}

// sample structured plans: days with items (replace with real data)
const plansDays = ref([
  {
    id: 1,
    title: 'Arrival & Gangnam Exploration',
    date: 'Nov 6, 2025',
    cost: 80,
    items: [
      { id: 'a1', title: 'Hotel Check-in (Gangnam)', time: '5:00 PM' },
      { id: 'a2', title: 'COEX Mall & Starfield Library', time: '6:30 PM' },
      { id: 'a3', title: 'Bonguensa Temple', time: '8:30 PM' }
    ]
  },
  {
    id: 2,
    title: 'Palace & Hanbok Experience',
    date: 'Nov 7, 2025',
    cost: 62,
    items: [
      { id: 'b1', title: 'Gyeongbokgung Palace', time: '10:00 AM' },
      { id: 'b2', title: 'Bukchon Hanok Village', time: '1:00 PM' }
    ]
  },
  {
    id: 3,
    title: 'N Seoul Tower & Hongdae',
    date: 'Nov 8, 2025',
    cost: 137,
    items: [
      { id: 'c1', title: 'N Seoul Tower', time: '11:00 AM' },
      { id: 'c2', title: 'Hongdae Walk', time: '7:00 PM' }
    ]
  }
])

const expandedDay = ref(0)
const toast = reactive({ visible: false, message: '' })
let toastTimer = null

const toggleDay = (idx) => {
  expandedDay.value = expandedDay.value === idx ? null : idx
}

const showToast = (msg) => {
  toast.message = msg
  toast.visible = true
  clearTimeout(toastTimer)
  toastTimer = setTimeout(() => { toast.visible = false; toast.message = '' }, 1200)
}

/* --- ADD logic --- */
/* alreadyAddedAnywhere now checks both normal items and per-day top item */
const alreadyAddedAnywhere = () => {
  const srcId = item.id
  return plansDays.value.some(day => {
    if (day._topItem) {
      const t = day._topItem
      if (t._sourceId && srcId && t._sourceId === srcId) return true
      if (!t._sourceId && t.title === item.name) return true
    }
    return day.items.some(it =>
      it._added && ((it._sourceId && srcId && it._sourceId === srcId) || (!it._sourceId && it.title === item.name))
    )
  })
}

/* addToItinerary now supports iIdx === -1 (store as day._topItem) so top-add row is replaced */
const addToItinerary = (dIdx, iIdx) => {
  if (alreadyAddedAnywhere()) {
    showToast(`"${item.name}" 은(는) 이미 추가되어 있습니다`)
    return
  }
  const newId = `${item.id || 'CreateNewSearch'}-${Date.now()}-${Math.floor(Math.random() * 1000)}`
  const newItem = {
    id: newId,
    title: item.name || 'New Place',
    time: item.time || 'TBD',
    _added: true,
    _sourceId: item.id || null
  }

  if (iIdx === -1) {
    // place into the top-slot for the day (replaces the "Add at beginning" row visually)
    plansDays.value[dIdx]._topItem = newItem
  } else {
    // insert after given index in the regular items list
    plansDays.value[dIdx].items.splice(iIdx + 1, 0, newItem)
  }

  expandedDay.value = dIdx
  showToast(`"${newItem.title}" 이(가) 추가되었습니다`)
}

/* remove top-added item */
const removeTopAdded = (dIdx) => {
  const day = plansDays.value[dIdx]
  if (!day || !day._topItem) return
  const title = day._topItem.title
  delete day._topItem
  showToast(`"${title}" 이(가) 삭제되었습니다`)
}

/* helper: find existing applied or pending replace (returns {d,i} or null) */
const findExistingReplace = () => {
  for (let d = 0; d < plansDays.value.length; d++) {
    const day = plansDays.value[d]
    for (let i = 0; i < day.items.length; i++) {
      if (day.items[i]._replaced) return { d, i }
    }
  }
  return null
}

/* --- REPLACE logic (single selection only) --- */
const replaceThis = (dIdx, iIdx) => {
  const target = plansDays.value[dIdx].items[iIdx]

  // prevent replacing added items
  if (target._added) {
    showToast('추가된 항목은 교체할 수 없습니다')
    return
  }

  if (alreadyAddedAnywhere() && !(target._replaced && target._sourceId === item.id)) {
    showToast(`"${item.name}" 은(는) 이미 일정에 포함되어 있습니다`)
    return
  }

  // If currently replaced (same item) -> undo
  if (target._replaced) {
    undoReplace(dIdx, iIdx)
    return
  }

  // enforce single replace: undo any other replaced item first
  const prev = findExistingReplace()
  if (prev && (prev.d !== dIdx || prev.i !== iIdx)) {
    undoReplace(prev.d, prev.i)
  }

  // perform replacement in-place: keep original for undo
  target._original = { id: target.id, title: target.title, time: target.time }
  target._replaced = true
  target._appliedReplace = true
  target._sourceId = item.id || null
  target.title = item.name || target.title
  target.time = item.time || target.time

  expandedDay.value = dIdx
  showToast(`"${target.title}" 으로 대체되었습니다`)
}

/* undo replace */
const undoReplace = (dIdx, iIdx) => {
  const target = plansDays.value[dIdx].items[iIdx]
  if (!target || !target._replaced) return
  const orig = target._original || {}
  target.title = orig.title || target.title
  target.time = orig.time || target.time
  delete target._original
  delete target._replaced
  delete target._appliedReplace
  delete target._sourceId
  showToast('교체가 취소되었습니다')
}

/* remove added (only items with _added in the items array) */
const removeAdded = (dIdx, iIdx) => {
  const it = plansDays.value[dIdx].items[iIdx]
  if (!it || !it._added) return
  plansDays.value[dIdx].items.splice(iIdx, 1)
  showToast(`"${it.title}" 이(가) 삭제되었습니다`)
}

const hasChanges = computed(() => {
  return plansDays.value.some(day =>
    Boolean(day._topItem) ||
    day.items.some(it => it._added || it._replaced)
  )
})

const onItemClick = (dIdx, iIdx) => {
  if (mode === 'replace') replaceThis(dIdx, iIdx)
  // in add mode item click does nothing (use +)
}

const done = () => {
  const changed = hasChanges.value
  if (!changed) {
    router.back()
    return
  }
  // pass modified plans (could save via API)
  router.push({ name: 'Complete', state: { plans: plansDays.value } })
}
</script>

<style scoped>
.supporter-page {
  background-color: #fffaf3;
  min-height: 100vh;
  padding: 2rem 1.25rem; /* App.vue 사이드바도 padding-top: 2rem 필요 */
}

.card {
  border-radius: 12px;
  position: relative;
}

/* day block */
.day-block {
  border: 1px solid #f3e8ff;
  border-radius: 12px;
  overflow: hidden;
  background: #fff;
}

.day-block.expanded {
  box-shadow: 0 12px 30px rgba(27, 59, 111, 0.06);
}

/* day header */
.day-header {
  background: #fff;
  cursor: pointer;
  transition: background .18s, color .18s;
}

.day-header .day-label {
  color: #ff8c00;
  font-weight: 700;
  font-size: 13px;
}

.day-header .day-title {
  color: #1b3b6f;
  font-size: 16px;
}

.day-header .day-date {
  color: #6b7280;
}

/* active/expanded header style (클릭/확장 시 전체 색상 변경) */
.day-block.expanded .day-header {
  background: #ff8c00;
  color: #fff;
}

.day-block.expanded .day-header .day-label,
.day-block.expanded .day-header .day-title,
.day-block.expanded .day-header .day-date {
  color: rgba(255, 255, 255, 0.95);
}

.day-block.expanded .text-accent {
  color: #FFF;
}

/* change chevron color on expanded */
.day-block.expanded .chev {
  color: rgba(255, 255, 255, 0.95);
}

/* day body */
.day-body {
  background: #fff9ff;
  transition: background .18s;
}

/* when expanded, adjust inner item visuals */
.day-block.expanded .day-item {
  background: #fffaf0;
}

.day-block.expanded .day-item .fw-medium {
  color: #1b3b6f;
  font-weight: 700;
}

.day-block.expanded .day-item .small {
  color: #6b7280;
}

/* day item */
.day-item {
  border-top: 1px solid #f5f5f7;
  background: #fff;
  transition: background .12s, transform .08s, box-shadow .12s;
  align-items: center;
  gap: 12px;
}

.day-item:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 20px rgba(27, 59, 111, 0.04);
}

/* top-add row: 조금 더 연하게 표시해 자연스럽게 보이도록 함 */
.day-item-top {
  background: transparent;
  border-top: none;
}

.day-item-top .fw-medium {
  color: #6b7280;
  font-weight: 600;
}

/* when top slot is filled, make sure green-left/green-bg matches .item-added */
.day-item-top.item-added {
  border-top: 1px solid #f5f5f7;
  border-left: 4px solid #10b981;
  background: #f1fff6;
  box-shadow: 0 6px 18px rgba(16, 185, 129, 0.04);
}

/* visual states */
.item-added {
  border-left: 4px solid #10b981;
  background: #f1fff6;
  box-shadow: 0 6px 18px rgba(16, 185, 129, 0.04);
}

.item-selected {
  border-color: #1b3b6f;
  background: #f3f7ff;
  box-shadow: 0 8px 20px rgba(27, 59, 111, 0.06);
}

/* replaced (applied) uses same green visual as added */
.item-replaced-success {
  border-left: 4px solid #10b981;
  background: #f1fff6;
  box-shadow: 0 6px 18px rgba(16, 185, 129, 0.04);
  transition: transform .12s, background .18s;
  transform: translateY(-2px);
}

/* replaced badge + undo */
.replaced-badge {
  background: #f59e0b;
  color: #fff;
  padding: 6px 10px;
  border-radius: 999px;
  font-weight: 700;
}

.btn-undo {
  width: 36px;
  height: 36px;
  border-radius: 8px;
  border: 1px solid #e6e6ea;
  background: #fff;
  color: #1b3b6f;
  cursor: pointer;
  margin-left: 6px;
}

.btn-undo:hover {
  background: #f3f7ff;
}

/* added badge + remove */
.added-badge {
  background: #10b981;
  color: #fff;
  padding: 6px 10px;
  border-radius: 999px;
  font-weight: 700;
  font-size: 13px;
}

.btn-remove {
  width: 36px;
  height: 36px;
  border-radius: 8px;
  border: 1px solid #f0e6ea;
  background: #fff;
  color: #ef4444;
  font-weight: 700;
  cursor: pointer;
}

.btn-remove:hover {
  background: #fff7f7;
}

/* action buttons */
.action-wrap {
  width: 64px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.btn-add {
  width: 36px;
  height: 36px;
  border-radius: 8px;
  border: none;
  background: #fff;
  color: #1b3b6f;
  font-weight: 700;
  font-size: 18px;
  box-shadow: 0 4px 10px rgba(27, 59, 111, 0.06);
  cursor: pointer;
}

.btn-add:disabled {
  opacity: 0.45;
  cursor: not-allowed;
  color: #94a3b8;
}

.btn-replace {
  width: 36px;
  height: 36px;
  border-radius: 8px;
  border: 1px solid #e6e6ea;
  background: #fff;
  color: #6b7280;
  font-weight: 700;
  font-size: 14px;
  cursor: pointer;
}

.btn-replace.active {
  background: #1b3b6f;
  color: #fff;
  border-color: #1b3b6f;
  box-shadow: 0 6px 18px rgba(27, 59, 111, 0.12);
}

/* toast */
.action-toast {
  position: absolute;
  right: 16px;
  top: 12px;
  background: rgba(16, 185, 129, 0.12);
  color: #065f46;
  padding: 8px 12px;
  border-radius: 999px;
  font-weight: 600;
  box-shadow: 0 6px 18px rgba(6, 95, 70, 0.06);
  z-index: 10;
}

/* utilities */
.text-accent {
  color: #1b3b6f;
  font-weight: 700;
}

button:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.card {
  border-radius: 12px;
}
</style>