import { defineStore } from 'pinia'
import { ref, computed } from 'vue'

const MODE_KEY = 'nong_ui_mode'
const ROLE_KEY = 'nong_role'
const TOKEN_KEY = 'nong_token'
const USER_ID_KEY = 'nong_user_id'
const PHONE_KEY = 'nong_phone'

// 存储读写函数（兼容H5和UniApp）
function readStorage(key: string): string {
  if (typeof window !== 'undefined' && window.localStorage) {
    return window.localStorage.getItem(key) || ''
  }
  return uni.getStorageSync(key) || ''
}

function writeStorage(key: string, value: string | null | undefined) {
  const normalized = value == null ? '' : String(value)
  if (typeof window !== 'undefined' && window.localStorage) {
    if (normalized) {
      window.localStorage.setItem(key, normalized)
    } else {
      window.localStorage.removeItem(key)
    }
    return
  }
  if (normalized) {
    uni.setStorageSync(key, normalized)
  } else {
    uni.removeStorageSync(key)
  }
}

// UI Store 定义
export const useUiStore = defineStore('ui', () => {
  // 状态
  const mode = ref<'NORMAL' | 'ELDER'>('NORMAL')
  const role = ref<'FARMER' | 'OWNER' | 'ADMIN'>('FARMER')
  const token = ref('')
  const userId = ref('')
  const phone = ref('')

  // 计算属性
  const isElderMode = computed(() => mode.value === 'ELDER')
  const isLoggedIn = computed(() => !!token.value)
  const userInfo = computed(() => ({
    userId: userId.value,
    phone: phone.value,
    role: role.value
  }))

  // 方法
  function applyModeClass() {
    // H5 uses body/html classes so elder mode is immediately visible.
    if (typeof document === 'undefined') {
      return
    }
    const isElder = mode.value === 'ELDER'
    document.documentElement.classList.toggle('elder-mode', isElder)
    document.body.classList.toggle('elder-mode', isElder)
  }

  function hydrate() {
    try {
      mode.value = (readStorage(MODE_KEY) as 'NORMAL' | 'ELDER') || 'NORMAL'
      role.value = (readStorage(ROLE_KEY) as 'FARMER' | 'OWNER' | 'ADMIN') || 'FARMER'
      token.value = readStorage(TOKEN_KEY) || ''
      userId.value = String(readStorage(USER_ID_KEY) || '')
      phone.value = readStorage(PHONE_KEY) || ''
    } catch (error) {
      mode.value = 'NORMAL'
      role.value = 'FARMER'
      token.value = ''
      userId.value = ''
      phone.value = ''
    }
    applyModeClass()
  }

  function setMode(newMode: 'NORMAL' | 'ELDER') {
    mode.value = newMode || 'NORMAL'
    writeStorage(MODE_KEY, mode.value)
    applyModeClass()
  }

  function setRole(newRole: 'FARMER' | 'OWNER' | 'ADMIN') {
    role.value = newRole || 'FARMER'
    writeStorage(ROLE_KEY, role.value)
  }

  function setToken(newToken: string) {
    token.value = newToken || ''
    writeStorage(TOKEN_KEY, token.value)
  }

  function setUserId(newUserId: string | number) {
    userId.value = String(newUserId || '')
    writeStorage(USER_ID_KEY, userId.value)
  }

  function setPhone(newPhone: string) {
    phone.value = newPhone || ''
    writeStorage(PHONE_KEY, phone.value)
  }

  function setSession(payload: {
    token?: string
    userId?: string | number
    phone?: string
    role?: 'FARMER' | 'OWNER' | 'ADMIN'
    uiMode?: 'NORMAL' | 'ELDER'
  } = {}) {
    setToken(payload.token || '')
    setUserId(payload.userId || '')
    setPhone(payload.phone || '')
    setRole(payload.role || 'FARMER')
    setMode(payload.uiMode || 'NORMAL')
  }

  function clearSession() {
    setToken('')
    setUserId('')
    setPhone('')
    setRole('FARMER')
    setMode('NORMAL')
  }

  function toggleMode() {
    setMode(mode.value === 'ELDER' ? 'NORMAL' : 'ELDER')
  }

  // 初始化
  hydrate()

  return {
    // 状态
    mode,
    role,
    token,
    userId,
    phone,
    // 计算属性
    isElderMode,
    isLoggedIn,
    userInfo,
    // 方法
    applyModeClass,
    hydrate,
    setMode,
    setRole,
    setToken,
    setUserId,
    setPhone,
    setSession,
    clearSession,
    toggleMode
  }
})

// 为了兼容旧代码，导出一个默认实例（在组件外使用需注意）
let _uiStoreInstance: ReturnType<typeof useUiStore> | null = null
export function getUiStore() {
  if (!_uiStoreInstance) {
    _uiStoreInstance = useUiStore()
  }
  return _uiStoreInstance
}