import api from './axios'

//지도 영역 내 화장실 조회
async function getToiletsInBounds(bounds) {
  console.log('🔵 API 호출: /supporter/toilets/in-bounds', bounds)
  const res = await api.get('/supporter/toilets/in-bounds', {
    params: bounds
  });
  console.log('✅ API 응답:', res.data)
  // res.data가 {success, data, error} 형태이면 data 추출
  return res.data.data || res.data;
}

//사용자 위치의 가까운 화장실 조회
async function getNearestToilets(userLat, userLng, limit) {
  console.log('🔵 API 호출: /supporter/toilets/nearest', { userLat, userLng, limit })
  const res = await api.get('/supporter/toilets/nearest', {
    params: {
      userLat: userLat,
      userLng: userLng,
      limit: limit
    }
  });
  console.log('✅ API 응답:', res.data)
  // res.data가 {success, data, error} 형태이면 data 추출
  return res.data.data || res.data;
}

export default {
  getToiletsInBounds,
  getNearestToilets
};