import api from './axios'

/**
 * userId로 활성 여행 계획을 조회하고 LLM이 추천하는 호텔을 반환한다.
 * @param {Long} userId - 사용자 ID
 * @param {String} userPreferences - 사용자 선호도 (선택사항)
 * @returns {Promise} 호텔 추천 정보
 */
async function recommendHotel(userId, userPreferences = null) {
  const params = {
    userId: userId
  }
  
  if (userPreferences) {
    params.preferences = userPreferences
  }
  
  return api.get('/api/hotel/recommend', { params })
}


// ✅ 사용자 ID로 호텔 정보를 조회한다.
async function getHotelByUserId(userId) {
  console.log('📤 API 호출: GET /api/hotel-ff')
  console.log('userId:', userId)
  
  return api.get(`/api/hotel-ff/${userId}`)
}

/**
 * 호텔 예약을 생성 또는 수정한다. (Upsert)
 * @param {Long} userId - 사용자 ID
 * @param {Object} hotelData - 호텔 예약 데이터
 * @returns {Promise} 저장된 예약 정보
 */
async function createHotelBooking(userId, hotelData) {
  return api.put(`/api/hotel-ff/insert/${userId}`, hotelData)
}

/**
 * 호텔 예약을 삭제한다.
 * @param {Long} id - 예약 ID
 * @returns {Promise} 삭제 결과
 */
async function deleteHotelBooking(id) {
  return api.delete(`/api/hotel-ff/id/${id}`)
}

export default {
  recommendHotel,
  getHotelByUserId,
  createHotelBooking,
  deleteHotelBooking
}

