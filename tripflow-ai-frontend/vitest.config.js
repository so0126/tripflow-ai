import { fileURLToPath, URL } from 'node:url'
import { defineConfig } from 'vitest/config'
import vue from '@vitejs/plugin-vue'

// Vue CLI(webpack)와 별개로 동작하는 테스트 전용 설정.
// SFC 컴파일은 @vitejs/plugin-vue가, '@/...' 별칭은 아래 alias가 담당한다.
export default defineConfig({
  plugins: [vue()],
  resolve: {
    alias: {
      '@': fileURLToPath(new URL('./src', import.meta.url)),
    },
  },
  test: {
    environment: 'jsdom',          // 컴포넌트 마운트에 필요한 DOM 제공
    include: ['src/**/*.{test,spec}.{js,mjs,ts}'],
  },
})
