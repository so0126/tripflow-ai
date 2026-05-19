import api from './axios'

// 주소를 위도/경도로 변환 (백엔드 Geocoding API 호출)
async function addressToCoordinates(address) {
    try {
        const response = await api.get('/api/geo/forward', {
            params: { address }
        })
        
        const data = response.data
        console.log('Geocoding API 응답:', data)
        
        // 백엔드 응답 형식에 맞게 파싱
        if (data && data.lat && data.lng) {
            return {
                lat: data.lat,
                lng: data.lng
            }
        }
        
        throw new Error('좌표를 찾을 수 없습니다')
        
    } catch (error) {
        console.error('주소 변환 오류:', error)
        // 기본값 반환 (서울 중심)
        return { lat: 37.5665, lng: 126.9780 }
    }
}

//이미지 Agent 생성 및 후보자 생성
async function recommendPlacesByImage(placeType, image, address) {
    const formData = new FormData()
    formData.append('placeType', placeType)
    formData.append('image', image) // File 객체
    formData.append('address', address)
    
    // baseURL 확인 및 로깅
    const apiUrl = `${api.defaults.baseURL || 'http://localhost:8080'}/supporter/image-search/candidates`
    console.log('최종 요청 URL:', apiUrl)
    console.log('API baseURL:', api.defaults.baseURL)
    console.log('전송 데이터 - placeType:', placeType, 'address:', address)
    
    const res = await api.post('/supporter/image-search/candidates', formData, {
        headers: {
            'Content-Type': 'multipart/form-data'
        }
    })
    return res.data
}

//후보자 저장 (모든 후보지 저장)
async function savePlaceCandidates(userId, dataToSave) {
    // dataToSave 형식:
    // {
    //   selectedPlace: { placeName, location, imageUrl, ... },
    //   candidates: [{ placeName, location, imageUrl, ... }, ...], // 모든 후보지
    //   uploadedImage: base64String,
    //   selectedType: 'landscape|food|activities',
    //   action: 'save|add|replace'
    // }
    
    const allCandidates = dataToSave.candidates || []
    const selectedPlace = dataToSave.selectedPlace
    const selectedType = dataToSave.selectedType || 'landscape'
    const actionType = dataToSave.action || 'SAVE'
    
    console.log('선택된 장소:', selectedPlace)
    console.log('모든 후보지:', allCandidates)
    
    // 모든 후보지를 PlaceCandidateRequest 형식으로 변환 (위경도는 0으로 설정)
    const candidatesToSave = await Promise.all(allCandidates.map(async(candidate, index) => {
        // actionType을 백엔드 enum 형식에 맞게 변환
        let backendActionType = 'SAVE_ONLY'
        if (actionType === 'add') {
            backendActionType = 'ADD_PLAN'
        } else if (actionType === 'replace') {
            backendActionType = 'REPLACED_PLAN'
        }
        
        return {
            name: candidate?.placeName || '',
            address: candidate?.location || candidate?.address || '',
            lat: 0.0,  // 임시값
            lng: 0.0,  // 임시값
            placeType: selectedType,
            visualFeatures: candidate?.description || '',
            imageUrl: candidate?.imageUrl || '',
            imageStatus: 'READY', // PENDING, READY, FAILED
            isSelected: selectedPlace && candidate?.placeName === selectedPlace.placeName, // 선택된 것만 true
            rank: index + 1, // 1, 2, 3
            actionType: backendActionType // SAVE_ONLY, ADD_PLAN, REPLACED_PLAN
        }
    }));
    
    console.log('저장할 모든 후보자 데이터 (위경도 포함):', candidatesToSave)
    console.log('userId:', userId)
    
    const res = await api.post(`/supporter/image-search/candidates/save?userId=${userId}`, candidatesToSave, {
        headers: {
            'Content-Type': 'application/json'
        }
    })
    console.log('✅ 저장 응답:', res.data)
    return res.data
}

// 사용자의 모든 세션 조회 (히스토리)
async function getSessionsByUserId(userId) {
    const res = await api.get('/supporter/image-search/candidates/sessions', {
        params: { userId }
    })
    return res.data
}

// ActionType 변경
async function updateActionType(candidateId, actionType) {
    // actionType 변환: add -> ADD_PLAN, replace -> REPLACED_PLAN
    let backendActionType = 'SAVE_ONLY'
    if (actionType === 'add') {
        backendActionType = 'ADD_PLAN'
    } else if (actionType === 'replace') {
        backendActionType = 'REPLACED_PLAN'
    }
    
    const res = await api.put(`/supporter/image-search/candidates/${candidateId}/action-type`, null, {
        params: { actionType: backendActionType }
    })
    return res.data
}

// 후보지 삭제
async function deleteCandidate(candidateId) {
    const res = await api.delete(`/supporter/image-search/candidates/${candidateId}`)
    return res.data
}

export default {
    recommendPlacesByImage,
    savePlaceCandidates,
    getSessionsByUserId,
    updateActionType,
    deleteCandidate
}