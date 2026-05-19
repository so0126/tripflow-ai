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


// 사용자 ID로 최근 예약 요약 조회
async function getHotelByUserId(userId) {
  return api.get(`/api/hotel-bookings/user/${userId}/summary`)
}

// 호텔 예약 생성
async function createHotelBooking(bookingData) {
  return api.post('/api/hotel-bookings', bookingData)
}

// 호텔 예약 삭제
async function deleteHotelBooking(id) {
  return api.delete(`/api/hotel-bookings/${id}`)
}

export default {
  recommendHotel,
  getHotelByUserId,
  createHotelBooking,
  deleteHotelBooking
}

