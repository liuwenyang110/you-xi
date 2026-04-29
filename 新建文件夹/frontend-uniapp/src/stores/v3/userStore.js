import { defineStore } from 'pinia'
import { getV3Profile } from '@/api/v3/user'

export const useV3UserStore = defineStore('v3user', {
  state: () => ({
    profile: null,     // V3 用户档案
    loaded: false,
    elderMode: false,  // 适老版大字模式
  }),

  getters: {
    isLoggedIn: (state) => !!state.profile,
    role: (state) => state.profile?.role,
    roleName: (state) => state.profile?.role === 'FARMER' ? '农户' : (state.profile?.role === 'OPERATOR' ? '农机手' : null),
    isFarmer: (state) => state.profile?.role === 'FARMER',
    isOperator: (state) => state.profile?.role === 'OPERATOR',
    hasRole: (state) => !!state.profile?.role,
    hasZone: (state) => !!state.profile?.zoneId,
    zoneId: (state) => state.profile?.zoneId,
    userId: (state) => state.profile?.id,
    v3UserId: (state) => state.profile?.id,
    isElderMode: (state) => state.elderMode,
  },

  actions: {
    async fetchProfile() {
      try {
        const res = await getV3Profile()
        if (res.code === 0) {
          this.profile = res.data
          this.loaded = true
        }
      } catch (e) {
        this.profile = null
      }
    },

    setProfile(profile) {
      this.profile = profile
      this.loaded = true
    },

    updateZone(zoneId) {
      if (this.profile) {
        this.profile = { ...this.profile, zoneId }
      }
    },

    clear() {
      this.profile = null
      this.loaded = false
    },

    toggleElderMode() {
      this.elderMode = !this.elderMode
    },

    setElderMode(val) {
      this.elderMode = !!val
    }
  },

  // 持久化（uni-app 用 uni.setStorageSync）
  persist: {
    enabled: true,
    strategies: [
      {
        key: 'v3user',
        storage: {
          getItem: (key) => uni.getStorageSync(key),
          setItem: (key, value) => uni.setStorageSync(key, value),
          removeItem: (key) => uni.removeStorageSync(key),
          },
        paths: ['profile', 'loaded', 'elderMode']
      }
    ]
  }
})
