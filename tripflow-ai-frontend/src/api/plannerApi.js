import api from './axios'

// 활성화된 여행 계획 불러오기
async function getActivePlan(userId) {
  return await api.get(`/plans/${userId}/active/detail`)
}

// 쉴 장소(근처 카페) 불러오기
async function getRestPlaces(lat, lng) {
  return await api.get(`/search-rest-place`, {
    params: { lat, lng }
  })
}

// 방문할 장소 수정
async function updatePlanPlace(placeId, newPlace) {
  return await api.put(`/plans/places/${placeId}`, newPlace)
}

// 방문할 장소 삭제
async function deletePlanPlace(placeId) {
  return await api.delete(`/plans/places/${placeId}`)
}

async function getActivePlanIdAndDayIndex(userId) {
  const res = await api.get(`/plans/${userId}/active/plan/info`, userId)
  return res.data;
}

const getBlogList = async (keyword) => {
  const res = await api.get('api/search/blog', {
    params: {
      keyword: keyword
    }
  })
  return res.data
}

// 여행 완료 저장
async function saveEndTravel(planId) {
  return await api.post(`/plans/${planId}/complete`, planId)
}

// 활동 완료 저장
async function saveCurrentActivity(activityData) {
  return await api.post('/api/travel/current-activity', activityData)
}

// 활동 완료 조회
async function getCurrentActivity(planPlaceId) {
  return await api.get(`/api/travel/current-activity/${planPlaceId}`, planPlaceId)
}

export default {
  getActivePlan,
  getBlogList,
  getActivePlanIdAndDayIndex,
  getRestPlaces,
  updatePlanPlace,
  deletePlanPlace,
  saveEndTravel,
  saveCurrentActivity,
  getCurrentActivity
};