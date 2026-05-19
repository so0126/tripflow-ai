import axios from 'axios'

const instance = axios.create({
  baseURL: process.env.VUE_APP_API_BASE_URL,
})

// 요청 인터셉터 수정
instance.interceptors.request.use(
  (config) => {
    try {
      // try-catch로 감싸서 스토리지 접근 에러 방지
      const token = localStorage.getItem('jwtToken')
      if (token) {
        config.headers.Authorization = `Bearer ${token}`
      }
    } catch (error) {
      console.warn('LocalStorage access denied via axios interceptor:', error);
    }
    return config
  },
  (error) => Promise.reject(error)
)

// 응답 인터셉터 수정
instance.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      try {
        localStorage.removeItem('jwtToken')
      } catch (e) {
        console.warn('LocalStorage remove failed:', e);
      }
      window.location.href = '/'
    }
    return Promise.reject(error)
  }
)

export default instance


