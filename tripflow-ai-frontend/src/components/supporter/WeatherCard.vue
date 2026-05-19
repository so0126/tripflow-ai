<template>
  <div v-if="weather" class="wc-root" :style="bgStyle">
    <div class="wc-top">
      <div class="wc-icon">
        <i :class="weather.iconClass"></i>
      </div>
      <div class="wc-right">
        <div class="wc-temp">{{ weather.temperature }}<span class="wc-deg">°C</span></div>
        <div class="wc-desc">{{ weather.desc }}</div>
        <div class="wc-loc">대한민국, 서울</div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, computed } from 'vue'

const props = defineProps({
  latitude: { type: Number, default: 37.5665 },
  longitude: { type: Number, default: 126.9780 }
})

const weather = ref(null)

const weatherCodeToText = (code) => {
  const map = {
    0: '맑음',
    1: '대체로 맑음',
    2: '구름 많음',
    3: '흐림',
    45: '안개',
    48: '착빙성 안개', // 안개가 얼어붙는 현상
    51: '가벼운 이슬비',
    53: '중간 이슬비',
    55: '짙은 이슬비',
    56: '가벼운 어는 이슬비',
    57: '짙은 어는 이슬비',
    61: '약한 비',
    63: '중간 비',
    65: '강한 비',
    66: '가벼운 어는 비',
    67: '강한 어는 비',
    71: '약한 눈',
    73: '중간 눈',
    75: '강한 눈',
    80: '소나기',
    81: '다소 강한 소나기',
    82: '폭우성 소나기',
    95: '뇌우',
    96: '뇌우 (약한 우박)',
    99: '뇌우 (강한 우박)'
  }
  return map[code] || '알 수 없음'
}

async function fetchOpenMeteo() {
  try {
    const lat = props.latitude
    const lon = props.longitude
    const url = `https://api.open-meteo.com/v1/forecast?latitude=${lat}&longitude=${lon}&current_weather=true&hourly=relativehumidity_2m,cloudcover&daily=precipitation_sum&timezone=Asia%2FSeoul`
    const res = await fetch(url)
    const data = await res.json()
    if (data && data.current_weather) {
      const cw = data.current_weather
      let humidity = '--'
      let clouds = '--'
      let precip = '--'
      try {
        const nowISO = new Date().toISOString().slice(0,13) + ':00'
        const idx = data.hourly.time.indexOf(nowISO)
        if (idx >= 0) {
          humidity = Math.round(data.hourly.relativehumidity_2m[idx])
          clouds = Math.round(data.hourly.cloudcover[idx])
        }
      } catch (e) {
        console.warn('Failed to parse hourly weather arrays', e)
      }
      try {
        if (data.daily && data.daily.precipitation_sum && data.daily.precipitation_sum.length > 0) {
          precip = Math.round(data.daily.precipitation_sum[0] * 10) / 10
        }
      } catch (e) {
        console.warn('Failed to parse daily precipitation', e)
      }

      const w = {
        temperature: Math.round(cw.temperature),
        windspeed: Math.round(cw.windspeed * 10) / 10,
        code: cw.weathercode,
        desc: weatherCodeToText(cw.weathercode),
        humidity: humidity,
        clouds: clouds,
        precip: precip,
        time: new Date().toLocaleTimeString()
      }

      const code = w.code
      if ([0,1].includes(code)) w.iconClass = 'bi bi-brightness-high'
      else if ([2,3].includes(code)) w.iconClass = 'bi bi-cloud-sun'
      else if (code >= 80 && code < 90) w.iconClass = 'bi bi-cloud-rain'
      else if (code >= 95) w.iconClass = 'bi bi-cloud-lightning'
      else w.iconClass = 'bi bi-cloud'

      weather.value = w
    }
  } catch (err) {
    console.warn('Weather fetch failed', err)
  }
}

onMounted(() => {
  fetchOpenMeteo()
})

// 하얀 배경으로 고정
const bgStyle = computed(() => ({ 
  background: '#ffffff'
}))
</script>

<style scoped>
.wc-root { 
  width: 100%; 
  box-sizing: border-box;
  border-radius: 18px; 
  overflow: hidden; 
  color: #2d4a8f; 
  margin-bottom: 0;
  height: 100px; 
  display: flex;
  align-items: center;
  position: relative;
  transition: all 0.3s ease;
  border: 1px solid #e2e8f0;
}

.wc-root:hover {
  transform: translateY(-2px);
  border-color: #cbd5e1;
}

.wc-root::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 3px;
  background: #2d4a8f;
  border-radius: 18px 18px 0 0;
}

.wc-top { 
  display: flex; 
  gap: 12px; 
  align-items: center; 
  padding: 0 24px;       /* 좌우 패딩만 줌 */
  width: 100%;           /* 가로 꽉 채움 */
  justify-content: space-between;
}

.wc-icon { 
  display: flex; 
  align-items: center; 
  justify-content: center; 
}

/* 아이콘 - 남색으로 변경, 100px 높이에 맞게 축소 */
.wc-icon i { 
  font-size: 3rem;
  color: #2d4a8f; 
  line-height: 1;
}

.wc-right { 
  text-align: right; 
}

.wc-temp { 
  font-size: 1.75rem;
  font-weight: 700; 
  line-height: 1;
  color: #1a2a56;
}

.wc-deg { 
  font-size: 1rem; 
  margin-left: 4px; 
  vertical-align: top;
  margin-top: 4px;
  display: inline-block;
  color: #2d4a8f;
}

.wc-desc { 
  font-size: 0.9rem; 
  font-weight: 600; 
  margin-top: 4px; 
  color: #4a6bb5;
}

.wc-loc { 
  font-size: 0.8rem; 
  opacity: 0.9; 
  margin-top: 4px; 
  color: #6b7280;
}

/* 모바일 대응 */
@media (max-width:767px){ 
  .wc-root { height: auto; padding: 1.5rem 0; }
  .wc-icon i { font-size: 3.5rem; } 
  .wc-temp { font-size: 2.5rem; } 
}
</style>
