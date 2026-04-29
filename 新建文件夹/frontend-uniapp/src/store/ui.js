// 适配器文件：向后兼容旧的uiStore，内部使用Pinia store
// 注意：建议新代码直接导入 useUiStore from '@/stores/ui'

import { getUiStore } from '../stores/ui'

// 延迟获取 Store，避免在 Pinia 实例创建前调用
const getStore = () => getUiStore()

// 创建兼容旧API的对象
export const uiStore = {
  get mode() {
    return getStore().mode
  },
  set mode(value) {
    getStore().setMode(value)
  },
  get role() {
    return getStore().role
  },
  set role(value) {
    getStore().setRole(value)
  },
  get token() {
    return getStore().token
  },
  set token(value) {
    getStore().setToken(value)
  },
  get userId() {
    return getStore().userId
  },
  set userId(value) {
    getStore().setUserId(value)
  },
  get phone() {
    return getStore().phone
  },
  set phone(value) {
    getStore().setPhone(value)
  },
  applyModeClass() {
    getStore().applyModeClass()
  },
  hydrate() {
    getStore().hydrate()
  },
  setMode(mode) {
    getStore().setMode(mode)
  },
  setRole(role) {
    getStore().setRole(role)
  },
  setToken(token) {
    getStore().setToken(token)
  },
  setUserId(userId) {
    getStore().setUserId(userId)
  },
  setPhone(phone) {
    getStore().setPhone(phone)
  },
  setSession(payload = {}) {
    getStore().setSession(payload)
  },
  clearSession() {
    getStore().clearSession()
  },
  toggleMode() {
    getStore().toggleMode()
  }
}