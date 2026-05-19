<template>
  <ActivityCompleteModal
    :open="true"
    title="테스트 장소"
    :gallery="selected.gallery"
    :spendInput="0"
    comment="테스트 코멘트"
    :quickStats="{ started: '10:00', duration: '30m', status: 'On schedule' }"
    @close="() => {}"
    @confirm="() => {}"
  />
</template>
<script setup>
import ActivityCompleteModal from "@/components/planner/ActivityCompleteModal.vue";
</script>