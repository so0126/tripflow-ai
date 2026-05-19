import axios from './axios'

// userId로 모든 체크리스트 항목 조회
export const getChecklistByUserId = async (userId) => {
  try {
    const response = await axios.get(`/supporter/checklist-items/user/${userId}`)
    console.log('✅ API Response:', response.data)
    // API 응답: { success, status, data: [...], error }
    // 실제 데이터는 response.data.data 안에 있음
    return response.data.data || response.data
  } catch (error) {
    console.error('❌ Failed to load checklist:', error)
    console.error('Error response:', error.response?.data)
    throw error
  }
}

// planId와 dayIndex로 체크리스트 조회 (필요시)
export const getChecklistByPlanAndDay = async (planId, dayIndex) => {
  try {
    const response = await axios.get(`/supporter/checklist-items/plan/${planId}/day/${dayIndex}`)
    console.log('✅ Checklist loaded:', response.data)
    return response.data.data || response.data
  } catch (error) {
    console.error('❌ Failed to load checklist:', error)
    console.error('Error response:', error.response?.data)
    throw error
  }
}

// 체크리스트 항목 생성
export const createChecklistItem = async (item) => {
  try {
    const response = await axios.post('/supporter/checklist-items', item)
    console.log('✅ Checklist item created:', response.data)
    return response.data.data || response.data
  } catch (error) {
    console.error('❌ Failed to create checklist item:', error)
    console.error('Error response:', error.response?.data)
    throw error
  }
}

// 체크리스트 항목 수정
export const updateChecklistItem = async (id, item) => {
  try {
    const response = await axios.put(`/supporter/checklist-items/${id}`, item)
    console.log('✅ Checklist item updated:', response.data)
    return response.data.data || response.data
  } catch (error) {
    console.error('❌ Failed to update checklist item:', error)
    console.error('Error response:', error.response?.data)
    throw error
  }
}

// 체크리스트 항목 삭제
export const deleteChecklistItem = async (id) => {
  try {
    const response = await axios.delete(`/supporter/checklist-items/${id}`)
    console.log('✅ Checklist item deleted')
    return response.data.data || response.data
  } catch (error) {
    console.error('❌ Failed to delete checklist item:', error)
    console.error('Error response:', error.response?.data)
    throw error
  }
}

// 체크리스트 항목 체크 상태 토글
export const toggleChecklistItem = async (id, isChecked) => {
  try {
    const response = await axios.put(`/supporter/checklist-items/${id}/toggle`, { isChecked })
    console.log('✅ Checklist item toggled:', response.data)
    return response.data.data || response.data
  } catch (error) {
    console.error('❌ Failed to toggle checklist item:', error)
    console.error('Error response:', error.response?.data)
    throw error
  }
}

// planId + dayIndex로 체크리스트 에이전트 호출
// GET /supporter/checklists/checklist/{planId}/{dayIndex}
export const getChecklistBundleByPlanAndDay = async (planId, dayIndex) => {
  try {
    const response = await axios.get(`/supporter/checklists/checklist/${planId}/${dayIndex}`)
    console.log('✅ Checklist bundle loaded:', response.data)
    return response.data.data || response.data
  } catch (error) {
    console.error('❌ Failed to load checklist bundle:', error)
    console.error('Error response:', error.response?.data)
    throw error
  }
}