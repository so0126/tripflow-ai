<template>
  <component :is="to ? 'router-link' : 'button'" :to="to" :class="buttonClass" :type="to ? null : type"
    @click="handleClick">
    <slot />
  </component>
</template>

<!-- 컴포넌트 초기화 또는 이벤트 처리 -->
<script setup>
import { computed } from 'vue'
import { useRouter } from 'vue-router'

const props = defineProps({
  to: String,
  variant: { type: String, default: 'primary' },
  size: { type: String, default: 'md' },
  type: { type: String, default: 'button' },
  emitClick: { type: Boolean, default: true } // 클릭 emit 여부
})

const emit = defineEmits(['click'])

//현재는 안 쓰지만, 필요 시 router 기반 이동을 위해 선언
const router = useRouter()

const buttonClass = computed(() => [
  'btn',
  `btn-${props.variant}`,
  `btn-${props.size}`
])

function handleClick(e) {
  if (props.emitClick && !props.to) {
    emit('click', e)
  }
}
</script>


<style scoped></style>