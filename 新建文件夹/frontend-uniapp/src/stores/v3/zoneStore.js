import { defineStore } from 'pinia'
import { getZoneDetail, getZoneMachineryStats, getAllDicts } from '@/api/v3/zone'

export const useV3ZoneStore = defineStore('v3zone', {
  state: () => ({
    currentZone: null,      // 当前片区详情
    machineryStats: [],     // 片区农机分类统计
    dicts: {
      machineCategories: [],
      machineTypes: [],
      crops: [],
      workTypes: [],
    },
    dictsLoaded: false,
  }),

  getters: {
    zoneName: (state) => state.currentZone?.name || '',
    zoneId: (state) => state.currentZone?.id,
    operatorCount: (state) => state.currentZone?.operatorCount || 0,
    machineryCount: (state) => state.currentZone?.machineryCount || 0,
    townshipCode: (state) => state.currentZone?.townshipCode || '',

    // 获取作业类型名称
    workTypeName: (state) => (code) => {
      const wt = state.dicts.workTypes.find(w => w.code === code)
      return wt?.name || code
    },

    // 获取作物品种名称
    cropName: (state) => (code) => {
      const crop = state.dicts.crops.find(c => c.code === code)
      return crop?.name || code
    },

    // 获取农机类型名称
    machineTypeName: (state) => (id) => {
      const mt = state.dicts.machineTypes.find(m => m.id === id)
      return mt?.name || ''
    },
  },

  actions: {
    async loadZone(zoneId) {
      try {
        const [zoneRes, statsRes] = await Promise.all([
          getZoneDetail(zoneId),
          getZoneMachineryStats(zoneId),
        ])
        if (zoneRes.code === 0) this.currentZone = zoneRes.data
        if (statsRes.code === 0) this.machineryStats = statsRes.data
      } catch (e) {
        console.error('加载片区失败', e)
      }
    },

    async loadDicts() {
      if (this.dictsLoaded) return
      try {
        const res = await getAllDicts()
        if (res.code === 0) {
          this.dicts = res.data
          this.dictsLoaded = true
        }
      } catch (e) {
        console.error('加载字典失败', e)
      }
    },

    clearZone() {
      this.currentZone = null
      this.machineryStats = []
    }
  },

  persist: {
    enabled: true,
    strategies: [
      {
        key: 'v3zone',
        storage: {
          getItem: (key) => uni.getStorageSync(key),
          setItem: (key, value) => uni.setStorageSync(key, value),
          removeItem: (key) => uni.removeStorageSync(key),
        },
        paths: ['dicts', 'dictsLoaded'] // 只持久化字典，片区信息每次重新拉
      }
    ]
  }
})
