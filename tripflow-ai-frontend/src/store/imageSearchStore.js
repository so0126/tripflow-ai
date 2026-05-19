import { defineStore } from 'pinia'

export const useImageSearchStore = defineStore('imageSearch', {
  state: () => ({
    // 후보 장소 목록 (AiRecommend 페이지에서 생성)
    candidates: [],
    // 선택된 장소
    selectedPlace: null,
    // 선택된 사진 타입
    selectedType: null,
    // 업로드된 이미지 (Base64)
    uploadedImage: null
  }),
  
  getters: {
    getCandidates() {
      return this.candidates
    },
    getSelectedPlace() {
      return this.selectedPlace
    },
    getSelectedType() {
      return this.selectedType
    },
    getUploadedImage() {
      return this.uploadedImage
    }
  },
  
  actions: {
    // 후보 장소 목록 저장 (AiRecommend에서 호출)
    setCandidates(candidates) {
      this.candidates = candidates || []
    },
    
    // 선택된 장소 저장 (AiRecommend의 addPlan에서 호출)
    setSelectedPlace(place) {
      this.selectedPlace = place
    },
    
    // 선택된 사진 타입 저장
    setSelectedType(type) {
      this.selectedType = type
    },
    
    // 업로드된 이미지 저장
    setUploadedImage(image) {
      this.uploadedImage = image
    },
    
    // 전체 검색 결과 저장 (이미지, 타입, 후보 장소)
    setSearchResult(image, type, candidates) {
      this.uploadedImage = image
      this.selectedType = type
      this.candidates = candidates || []
      this.selectedPlace = null
    },
    
    // 데이터 초기화
    clearSearchResult() {
      this.candidates = []
      this.selectedPlace = null
      this.selectedType = null
      this.uploadedImage = null
    }
  }
})
