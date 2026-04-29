import { createSSRApp } from 'vue'
import { createPinia, setActivePinia } from 'pinia'
import App from './App.vue'
import { uiStore } from './store/ui'

export function createApp() {
  const app = createSSRApp(App)
  
  // 初始化Pinia
  const pinia = createPinia()
  setActivePinia(pinia)
  
  // 安装Pinia
  app.use(pinia)
  
  // 初始化旧版uiStore
  uiStore.hydrate()
  
  return {
    app,
    pinia
  }
}
