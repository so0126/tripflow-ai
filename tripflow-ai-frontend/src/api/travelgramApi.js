import api from './axios'

// 여행 목록 조회
const getAllPlanByUserId = (userId) => api.get(`/plans/user/${userId}`)

// 여행 후기 생성용 상세 일정 조회
const getPlanDetail = (planId) => api.get(`/plans/${planId}/detail`)

// 여행 계획 제목 조회
const getPlanTitle = (planId) => api.get(`/ai/review/${planId}/title`)

// 리뷰 포스트(draft) 생성
const createReview = (planId) =>
  api.post('/reviews/create', null, {
    params: { planId },
  })

// 리뷰 사진 업로드
const uploadReviewPhotos = (formData) => api.post('/reviews/photos/upload', formData)

// 리뷰 사진 순서 업데이트
const updatePhotoOrder = (payload) => api.put('/reviews/photo/order', payload)

// 사진 분석 요청
const analyzePhotoMood = (reviewPostId) =>
  api.post('/reviews/photo/analyze', null, {
    params: { reviewPostId },
  })

// 리뷰 사진 조회
const getReviewPhotos = (reviewPostId) =>
  api.get('/reviews/photos', {
    params: { reviewPostId },
  })

// AI 후기 스타일 생성
const generateAiStyles = (planId, reviewPostId) =>
  api.post('/ai/review/generate-styles', null, {
    params: { planId, reviewPostId },
  })

// 후기 스타일 선택
const selectStyle = (reviewPostId, reviewStyleId) =>
  api.post('/reviews/style/select', null, {
    params: {
      reviewPostId,
      reviewStyleId,
    },
  })

// 후기 캡션 저장
const updateCaption = (reviewPostId, caption) =>
  api.post('/reviews/caption/update', null, {
    params: {
      reviewPostId,
      caption,
    },
  })

// 해시태그 저장
const createHashtags = (payload) => api.post('/reviews/hashtags/create', payload)

// 사진 재분석
const reanalyzePhoto = (photoId) => api.post(`/reviews/photo/${photoId}/reanalyze`)

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
  reanalyzePhoto,
}
