const routes = [
    {
        path: '/supporter',
        name: 'Supporter',
        component: () => import('@/views/supporter/index.vue')
    },
    {
        path: '/supporter/image-ai/history',
        name: 'History',
        component: () => import('@/views/supporter/image-ai/History.vue')
    },
    {
        path: '/supporter/image-ai/new-search',
        name: 'CreateNewSearch',
        component: () => import('@/views/supporter/image-ai/CreateNewSearch.vue')
    },
    {
        path: '/supporter/image-ai/img-type',
        name: 'ImageType',
        component: () => import('@/views/supporter/image-ai/ImageType.vue')
    },
    {
        path: '/supporter/image-ai/recommend',
        name: 'AiRecommend',
        component: () => import('@/views/supporter/image-ai/AiRecommend.vue')
    },
    {
        path: '/supporter/image-ai/choice-plan',
        name: 'ChoicePlan',
        component: () => import('@/views/supporter/image-ai/ChoicePlan.vue')
    },
    {
        path: '/supporter/image-ai/select-plan',
        name: 'SelectPlan',
        component: () => import('@/views/supporter/image-ai/SelectPlan.vue')
    },
    {
        path: '/supporter/image-ai/complete-plan',
        name: 'Complete',
        component: () => import('@/views/supporter/image-ai/Complete.vue')
    },
    {
        path: '/supporter/restrooms',
        name: 'RestroomsMap',
        component: () => import('@/views/supporter/RestroomsMap.vue')
    },
    {
        path: '/supporter/checklist',
        name: 'Checklist',
        component: () => import('@/components/supporter/Checklist.vue')
    }
];

export default routes;