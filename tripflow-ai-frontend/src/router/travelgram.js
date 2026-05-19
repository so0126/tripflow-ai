// src/router/travelgram.js
import Travelgram from '@/views/travelgram/index.vue'

// 리뷰 작성 단계
import CreateTravelReview from '@/views/travelgram/review/CreateTravelReview.vue'
import PhotoOrder from '@/views/travelgram/review/PhotoOrder.vue'
import CaptionSelect from '@/views/travelgram/review/CaptionSelect.vue'
import HashtagSelect from '@/views/travelgram/review/HashtagSelect.vue'
import EditPage from '@/views/travelgram/review/EditPage.vue'
import InstagramPreview from '@/views/travelgram/review/InstagramPreview.vue'
import CompleteReview from '@/views/travelgram/review/CompleteReview.vue'

const routes = [
  {
    path: '/travelgram',
    name: 'Travelgram',
    component: Travelgram,
  },
  {
    path: '/travelgram/:planId/:planTitle?',
    name: 'CreateTravelReview',
    component: CreateTravelReview,
    props: true,
  },
  {
    path: '/travelgram/:planId/order',
    name: 'PhotoOrder',
    component: PhotoOrder,
    props: true,
  },
  {
    path: '/travelgram/:planId/caption',
    name: 'CaptionSelect',
    component: CaptionSelect,
    props: true,
  },
  {
    path: '/travelgram/:planId/hashtags',
    name: 'HashtagSelect',
    component: HashtagSelect,
    props: true,
  },
  {
    path: '/travelgram/:planId/edit',
    name: 'EditPage',
    component: EditPage,
    props: true,
  },
  {
    path: '/travelgram/:planId/preview',
    name: 'InstagramPreview',
    component: InstagramPreview,
    props: true,
  },
  {
    path: '/travelgram/:planId/complete',
    name: 'CompleteReview',
    component: CompleteReview,
    props: true,
  },
]
export default routes;