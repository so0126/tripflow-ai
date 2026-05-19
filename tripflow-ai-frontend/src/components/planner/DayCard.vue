<template>
    <div class="card border-0 shadow-sm rounded-4 overflow-hidden">
        <div class="card-body d-flex justify-content-between align-items-center"
            :class="isOpen ? 'bg-primary text-white' : 'bg-white'" role="button" @click="$emit('toggleDay', day.id)">
            <div>
                <div class="small fw-semibold" :class="!isOpen ? 'text-primary' : ''">
                    Day {{ day.day.dayIndex }}
                </div>
                <h6 class="mb-0 title">{{ day.day.title }}</h6>
                <div class="small" :class="!isOpen ? 'text-muted' : 'text-white-50'">
                    {{ day.day.planDate }}
                </div>
            </div>
            <div class="text-end">
                <div class="small" :class="isOpen ? 'text-white-50' : ''">
                    Daily Cost
                </div>
                <div class="fw-bold title">$ {{ day.dailyCost }}</div>
                <div class="small">
                    <span class="chevron" :class="{ 'rotate-180': isOpen }">⌃</span>
                </div>
            </div>
        </div>

        <transition name="collapse">
            <div v-if="isOpen" class="list-group list-group-flush">
                <ActivityList :day="day" :dayIndex="dayIndex" :activityRowClass="activityRowClass"
                    :getIconForType="getIconForType" :hasCost="hasCost" :formatCost="formatCost"
                    @openDetails="(d, a) => $emit('openDetails', d, a)"
                    @toggleComplete="(d, a) => $emit('toggleComplete', d, a)"
                    @openReplace="(d, a) => $emit('openReplace', d, a)" />
            </div>
        </transition>
    </div>
</template>

<script setup>
import { computed } from "vue";
import ActivityList from "./ActivityList.vue";

const props = defineProps({
    day: Object,
    dayIndex: Number,
    openDayId: Number,
    activityRowClass: Function,
    getIconForType: Function,
    hasCost: Function,
    formatCost: Function,
});

defineEmits(["toggleDay", "openDetails", "toggleComplete", "openReplace"]);

const isOpen = computed(() => props.openDayId === props.day.day.id);
</script>

<style scoped>


.collapse-enter-active,
.collapse-leave-active {
    transition: all 0.2s ease;
}

.collapse-enter-from,
.collapse-leave-to {
    max-height: 0;
    opacity: 0;
}

.chevron {
    transition: transform 0.2s;
    display: inline-block;
}

.rotate-180 {
    transform: rotate(180deg);
}
</style>
