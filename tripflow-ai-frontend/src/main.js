import { createApp } from 'vue'
import { createPinia } from 'pinia'
import App from './App.vue'
import router from './router'
import 'bootstrap/dist/css/bootstrap.min.css'
import 'bootstrap-icons/font/bootstrap-icons.css'
import 'bootstrap'
import '@/assets/styles/global.scss'
import imgFallback from "./directives/imgFallback";


createApp(App)
  .use(createPinia())
  .use(router)
  .directive("img-fallback", imgFallback)
  .mount('#app')
