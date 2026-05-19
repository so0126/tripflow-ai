<!-- 컴포넌트의 UI -->
<template>
    <div class="form-check form-switch mb-3">
        <input class="form-check-input"
            type="checkbox"
            :id="id"
            v-model="localValue"
            @change="$emit('update:modelValue', modelValue)" />
        <label class="form-check-label" :for="id">
            {{ label }}
        </label>
    </div>
</template>

<!-- 컴포넌트의 초기화 또는 이벤트 처리 -->
<script setup>
import { computed } from 'vue'

const props = defineProps({
  id: String,
  label: String,
  modelValue: Boolean,
})

const emit = defineEmits(['update:modelValue'])

const localValue = computed({
  get: () => props.modelValue,
  set: (val) => emit('update:modelValue', val)
})
</script>

<!-- 컴포넌트 스타일 정의 -->
<style scoped>
.form-check {
    padding-left: 2.5rem;

    .form-check-input {
      margin-left: -2.5rem;
      
      &:checked {
        background-color: #ff8c00;
        border-color: #ff8c00;
      }
    }

    .form-check-label {
      color: #1b3b6f;
      font-weight: 500;
    }
  }
</style>