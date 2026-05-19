import EditProfile from '@/views/mypage/EditProfile.vue';
import MyProfile from '@/views/mypage/MyProfile.vue'


const routes=[
    {
    path: '/mypage',
    name: 'MyProfile',
    component: MyProfile,
    meta: { requiresAuth: true }
  },
  {
    path: '/mypage/edit',
    name: 'EditProfile',
    component: EditProfile,
    meta: { requiresAuth: true }
  },
];

export default routes;