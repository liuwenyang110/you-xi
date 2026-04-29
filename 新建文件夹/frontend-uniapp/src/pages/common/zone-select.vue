<template>
  <view class="zone-select-page">
    <NavBar title="选择所在片区" />
    <view class="page-header">
      <text class="page-sub">按您的实际位置逐级选择</text>
    </view>

    <!-- 省市县乡 级联选择器 -->
    <view class="cascade-wrap">
      <!-- 省 -->
      <view class="level-block">
        <text class="level-label">省份</text>
        <picker mode="selector" :range="provinceList" range-key="name"
                :value="selectedProvince" @change="onProvinceChange">
          <view class="picker-display">
            <text class="picker-value">{{ provinceName || '请选择省份' }}</text>
            <text class="picker-arrow">›</text>
          </view>
        </picker>
      </view>

      <!-- 市 -->
      <view class="level-block" v-if="cityList.length">
        <text class="level-label">城市</text>
        <picker mode="selector" :range="cityList" range-key="name"
                :value="selectedCity" @change="onCityChange">
          <view class="picker-display">
            <text class="picker-value">{{ cityName || '请选择城市' }}</text>
            <text class="picker-arrow">›</text>
          </view>
        </picker>
      </view>

      <!-- 县 -->
      <view class="level-block" v-if="countyList.length">
        <text class="level-label">区/县</text>
        <picker mode="selector" :range="countyList" range-key="name"
                :value="selectedCounty" @change="onCountyChange">
          <view class="picker-display">
            <text class="picker-value">{{ countyName || '请选择区/县' }}</text>
            <text class="picker-arrow">›</text>
          </view>
        </picker>
      </view>

      <!-- 乡镇 -->
      <view class="level-block" v-if="townshipList.length">
        <text class="level-label">乡镇</text>
        <picker mode="selector" :range="townshipList" range-key="name"
                :value="selectedTownship" @change="onTownshipChange">
          <view class="picker-display">
            <text class="picker-value">{{ townshipName || '请选择乡镇' }}</text>
            <text class="picker-arrow">›</text>
          </view>
        </picker>
      </view>
    </view>

    <!-- 片区列表 -->
    <view class="zone-list-section" v-if="zoneList.length">
      <text class="section-title">选择所在片区</text>
      <view
        v-for="zone in zoneList"
        :key="zone.id"
        class="zone-item"
        :class="{ selected: selectedZoneId === zone.id }"
        @click="selectZone(zone)"
      >
        <view class="zone-info">
          <text class="zone-name">{{ zone.name }}</text>
          <text class="zone-type-tag">{{ zoneTypeText(zone.zoneType) }}</text>
        </view>
        <view class="zone-stats">
          <text class="zone-stat">{{ zone.machineryCount }}台农机</text>
          <text class="zone-stat">{{ zone.operatorCount }}名机手</text>
        </view>
        <text class="zone-check" v-if="selectedZoneId === zone.id">✓</text>
      </view>
    </view>

    <!-- 暂无片区 -->
    <view class="empty-tip" v-if="townshipSelected && zoneList.length === 0 && !loading">
      <text class="empty-text">该乡镇暂未设置片区</text>
      <text class="empty-sub">可联系管理员申请开通</text>
    </view>

    <!-- 确认按钮 -->
    <view class="btn-wrap" v-if="selectedZoneId">
      <button class="confirm-btn" :loading="loading" @click="confirm">
        加入「{{ selectedZoneName }}」
      </button>
    </view>
  </view>
</template>

<script>
import { getProvinces, getChildren, getZones } from '@/api/v3/zone'
import { joinZone } from '@/api/v3/user'
import { useV3UserStore } from '@/stores/v3/userStore'
import { useV3ZoneStore } from '@/stores/v3/zoneStore'
import NavBar from '@/components/NavBar.vue'

export default {
  name: 'ZoneSelect',
  components: { NavBar },
  data() {
    return {
      loading: false,
      // 区划数据
      provinceList: [], selectedProvince: 0,
      cityList: [],    selectedCity: 0,
      countyList: [],  selectedCounty: 0,
      townshipList: [], selectedTownship: 0,
      // 片区
      zoneList: [],
      selectedZoneId: null,
      selectedZoneName: '',
      townshipSelected: false,
      // 当前选中的区划对象
      currentTownship: null,
    }
  },
  computed: {
    provinceName() {
      return this.provinceList[this.selectedProvince]?.name || ''
    },
    cityName() {
      return this.cityList[this.selectedCity]?.name || ''
    },
    countyName() {
      return this.countyList[this.selectedCounty]?.name || ''
    },
    townshipName() {
      return this.townshipList[this.selectedTownship]?.name || ''
    }
  },
  async onLoad() {
    await this.loadProvinces()
  },
  methods: {
    async loadProvinces() {
      const res = await getProvinces()
      if (res.code === 0) this.provinceList = res.data
    },
    async onProvinceChange(e) {
      this.selectedProvince = e.detail.value
      this.cityList = []
      this.countyList = []
      this.townshipList = []
      this.zoneList = []
      this.selectedZoneId = null
      this.townshipSelected = false
      const province = this.provinceList[this.selectedProvince]
      if (province) {
        const res = await getChildren(province.code)
        if (res.code === 0) this.cityList = res.data
        this.selectedCity = 0
      }
    },
    async onCityChange(e) {
      this.selectedCity = e.detail.value
      this.countyList = []
      this.townshipList = []
      this.zoneList = []
      this.selectedZoneId = null
      const city = this.cityList[this.selectedCity]
      if (city) {
        const res = await getChildren(city.code)
        if (res.code === 0) this.countyList = res.data
        this.selectedCounty = 0
      }
    },
    async onCountyChange(e) {
      this.selectedCounty = e.detail.value
      this.townshipList = []
      this.zoneList = []
      this.selectedZoneId = null
      const county = this.countyList[this.selectedCounty]
      if (county) {
        const res = await getChildren(county.code)
        if (res.code === 0) this.townshipList = res.data
        this.selectedTownship = 0
      }
    },
    async onTownshipChange(e) {
      this.selectedTownship = e.detail.value
      this.zoneList = []
      this.selectedZoneId = null
      this.townshipSelected = true
      const township = this.townshipList[this.selectedTownship]
      if (township) {
        this.currentTownship = township
        const res = await getZones({ townshipCode: township.code })
        if (res.code === 0) this.zoneList = res.data
      }
    },
    selectZone(zone) {
      this.selectedZoneId = zone.id
      this.selectedZoneName = zone.name
    },
    zoneTypeText(type) {
      const map = { VILLAGE: '行政村', COMMUNITY: '社区', FARM: '种植片区' }
      return map[type] || type
    },
    async confirm() {
      if (!this.selectedZoneId) return
      this.loading = true
      try {
        const res = await joinZone({ zoneId: this.selectedZoneId })
        if (res.code === 0) {
          const userStore = useV3UserStore()
          const zoneStore = useV3ZoneStore()
          userStore.updateZone(this.selectedZoneId)
          await zoneStore.loadZone(this.selectedZoneId)

          const role = userStore.role
          const target = role === 'FARMER' ? '/pages/farmer/zone-home' : '/pages/operator/zone-home'
          uni.showToast({ title: '已加入片区', icon: 'success' })
          setTimeout(() => {
            uni.reLaunch({ url: target })
          }, 800)
        } else {
          uni.showToast({ title: res.message || '加入失败', icon: 'none' })
        }
      } catch (e) {
        uni.showToast({ title: '网络异常', icon: 'none' })
      } finally {
        this.loading = false
      }
    }
  }
}
</script>

<style>
.zone-select-page {
  min-height: 100vh;
  background: #F5F8F2;
  padding: 0 0 120rpx;
}
.page-header {
  background: linear-gradient(135deg, #2D5A1B, #4A8A2E);
  padding: 100rpx 40rpx 48rpx;
}
.page-title { display: block; font-size: 44rpx; font-weight: 700; color: #fff; }
.page-sub { display: block; font-size: 28rpx; color: rgba(255,255,255,0.75); margin-top: 8rpx; }

.cascade-wrap {
  background: #fff;
  margin: 24rpx 24rpx 0;
  border-radius: 20rpx;
  overflow: hidden;
  box-shadow: 0 4rpx 16rpx rgba(0,0,0,0.06);
}
.level-block {
  display: flex;
  align-items: center;
  padding: 28rpx 32rpx;
  border-bottom: 1rpx solid #F0F0F0;
}
.level-label { font-size: 28rpx; color: #666; width: 200rpx; flex-shrink: 0; }
.picker-display {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: space-between;
}
.picker-value { font-size: 30rpx; color: #1E3A12; }
.picker-arrow { font-size: 36rpx; color: #999; }

.zone-list-section { margin: 24rpx; }
.section-title { display: block; font-size: 30rpx; font-weight: 700; color: #2D5A1B; margin-bottom: 16rpx; }
.zone-item {
  background: #fff;
  border-radius: 16rpx;
  padding: 28rpx 28rpx;
  margin-bottom: 16rpx;
  border: 3rpx solid transparent;
  box-shadow: 0 2rpx 12rpx rgba(0,0,0,0.05);
  position: relative;
}
.zone-item.selected {
  border-color: #4CAF50;
  background: #F2FAF2;
}
.zone-info { display: flex; align-items: center; margin-bottom: 12rpx; }
.zone-name { font-size: 34rpx; font-weight: 700; color: #1E3A12; margin-right: 16rpx; }
.zone-type-tag {
  font-size: 22rpx;
  background: #EFF6EC;
  color: #4A7A35;
  padding: 4rpx 14rpx;
  border-radius: 20rpx;
}
.zone-stats { display: flex; gap: 24rpx; }
.zone-stat { font-size: 26rpx; color: #7A966B; }
.zone-check {
  position: absolute;
  right: 28rpx;
  top: 50%;
  transform: translateY(-50%);
  font-size: 40rpx;
  color: #4CAF50;
  font-weight: 700;
}

.empty-tip {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 80rpx 40rpx;
}
.empty-icon { font-size: 80rpx; margin-bottom: 20rpx; }
.empty-text { font-size: 32rpx; color: #666; margin-bottom: 10rpx; }
.empty-sub { font-size: 26rpx; color: #999; }

.btn-wrap {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  padding: 20rpx 32rpx 48rpx;
  background: rgba(245,248,242,0.95);
  backdrop-filter: blur(10px);
}
.confirm-btn {
  width: 100%;
  height: 96rpx;
  background: linear-gradient(135deg, #4CAF50, #2E7D32);
  color: #fff;
  border-radius: 48rpx;
  font-size: 34rpx;
  font-weight: 700;
  border: none;
  box-shadow: 0 8rpx 28rpx rgba(46,125,50,0.35);
}
</style>
