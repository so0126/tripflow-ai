import api from './axios'

// 🔹 [수정] 백엔드 Path Variable 형식(/plans/user/{userId})에 맞게 URL 수정
const getAllPlanByUserId = async (userId) => {
  const res = await api.get(`/plans/user/${userId}`)
  return res.data
}
// 🔹 [추가] 상세 일정 조회 API 함수
const getPlanDetail = async (planId) => {
  const res = await api.get(`/plans/${planId}/detail`)
  return res.data
}

const getPlanTitle= async(planId) => {
    return api.get(`/ai/review/${planId}/title`);
}
// 1) 리뷰 포스트 + 리뷰 사진 그룹 생성 (review_posts, review_photo_groups auto increment 생성)
const createReview = async(planId)=>{
  const res = await api.post('/reviews/create', null, {
    params: {planId: planId}
})
  return res.data
} 

/* 2) 리뷰 사진 업로드 */
const uploadReviewPhotos = async (formData) => {
  return api.post('/reviews/photos/upload', formData);
}
// 3) 리뷰 사진 순서 업데이트
const updatePhotoOrder = async (payload) => {
  return api.put('/reviews/photo/order', payload)
}

// 🔥 [추가] 사진 분석 요청 API
const analyzePhotoMood = async(photoGroupId) =>{
    // Post 요청, 파라미터로 photoGroupId 전달
    return api.post('/reviews/photo/analyze', null, {
  params: { photoGroupId: photoGroupId }
})
}
// [추가] 조회용 API
const getReviewPhotos = async(photoGroupId) =>{
    return api.get('/reviews/photos', {
      params: { photoGroupId }
    });
}

const generateAiStyles = async (planId, reviewPostId) => {
  // 백엔드 컨트롤러에 맞게 파라미터 전달
  return api.post('/ai/review/generate-styles', null, {
    params: { planId, reviewPostId }
  });
}
const selectStyle = async (reviewPostId, reviewStyleId) => {
  // 🔥 [수정됨] 
  // 두 번째 인자(Body)는 null로 비우고, 
  // 세 번째 인자(Config)의 params에 데이터를 넣어야 백엔드가 인식합니다.
  return api.post('/reviews/style/select', null, {
    params: {
      reviewPostId: reviewPostId,
      reviewStyleId: reviewStyleId
    }
  });
}
const updateCaption = async(reviewPostId, caption)=>{
  return api.post('/reviews/caption/update',null,{
    params:{
      reviewPostId: reviewPostId,
      caption: caption
    }
  });
}
const createHashtags = async(payload)=>{
  return api.post('/reviews/hashtags/create',payload)
}
const reanalyzePhoto = async(photoId) => {
  return api.post(`/reviews/photo/${photoId}/reanalyze`)
}
export default {
    getAllPlanByUserId,
    getPlanDetail,
    getPlanTitle,
    createReview,
    uploadReviewPhotos,
    updatePhotoOrder,
    analyzePhotoMood,
    getReviewPhotos,
    generateAiStyles,
    selectStyle,
    updateCaption,
    createHashtags,
    reanalyzePhoto
};