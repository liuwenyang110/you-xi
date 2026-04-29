<template>
  <view class="page">
    <view class="card hero-card glass-panel scene-enter">
      <view class="section-caption">设备控制台</view>
      <view class="hero-title">我的农机</view>
      <view class="hero-subtitle">先看全局，再做编辑。系统会优先提示影响派单命中率的设备问题，减少逐条翻找。</view>
      <view class="hero-badges">
        <view class="tag">定位覆盖</view>
        <view class="tag">接单状态</view>
        <view class="tag">优先处理</view>
      </view>
    </view>

    <view class="card scene-enter delay-1" v-if="loading">
      <view class="state-title">正在加载设备列表</view>
      <view class="desc">请稍候，系统正在同步您的设备信息。</view>
    </view>
    <view class="card scene-enter delay-1" v-else-if="errorText">
      <view class="state-title">设备列表加载失败</view>
      <view class="warning">{{ errorText }}</view>
    </view>
    <view class="card scene-enter delay-1" v-else-if="equipmentList.length === 0">
      <view class="state-title">当前暂无设备记录</view>
      <view class="desc">补充设备后，平台才能进行更精准的匹配派单。</view>
    </view>

    <template v-else>
      <view class="card scene-enter delay-2 fleet-overview">
        <view class="overview-head">
          <view>
            <view class="section-title">设备概览</view>
            <view class="desc">优先把定位缺失和暂停接单的设备处理好，再进入细节编辑。</view>
          </view>
          <view class="tag light-tag">驾驶舱视图</view>
        </view>
        <view class="overview-grid">
          <view class="overview-tile">
            <view class="overview-value">{{ equipmentList.length }}</view>
            <view class="overview-label">设备总数</view>
          </view>
          <view class="overview-tile">
            <view class="overview-value">{{ withLocationCount }}</view>
            <view class="overview-label">已定位</view>
          </view>
          <view class="overview-tile">
            <view class="overview-value">{{ acceptingCount }}</view>
            <view class="overview-label">可接单</view>
          </view>
          <view class="overview-tile warning-tile">
            <view class="overview-value">{{ missingLocationCount }}</view>
            <view class="overview-label">待补定位</view>
          </view>
        </view>
      </view>

      <view class="card scene-enter delay-2 decision-queue-card" v-if="attentionQueue.length">
        <view class="row section-row">
          <view class="section-title">优先处理清单</view>
          <view class="tag light-tag">决策前置</view>
        </view>
        <view
          v-for="item in attentionQueue"
          :key="`queue-${item.id}`"
          class="queue-item"
          @click="openAttentionItem(item)"
        >
          <view class="queue-main">
            <view class="queue-name">{{ item.equipmentName || ('设备 #' + item.id) }}</view>
            <view class="queue-meta">{{ attentionLabel(item) }} · {{ hasCoords(item) ? '已定位' : '待补定位' }}</view>
          </view>
          <view class="queue-cta">去处理</view>
        </view>
      </view>

      <view class="card scene-enter delay-2 filter-card">
        <view class="filter-head">
          <view class="section-title">设备筛选</view>
          <view class="desc">颜色强调只用于优先级，不平均用力。高优先设备会更突出。</view>
        </view>
        <view class="filter-row">
          <view class="filter-chip" :class="{ active: highPriorityOnly }" @click="toggleHighPriority">
            {{ highPriorityOnly ? '仅看高优先：开' : '仅看高优先：关' }}
          </view>
        </view>
        <view class="filter-row">
          <view
            v-for="option in locationFilterOptions"
            :key="option.value"
            class="filter-chip"
            :class="{ active: locationFilter === option.value }"
            @click="setLocationFilter(option.value)"
          >
            {{ option.label }}
          </view>
        </view>
        <view class="filter-row">
          <view
            v-for="option in orderFilterOptions"
            :key="option.value"
            class="filter-chip"
            :class="{ active: orderFilter === option.value }"
            @click="setOrderFilter(option.value)"
          >
            {{ option.label }}
          </view>
        </view>
        <view class="coverage-block">
          <view class="coverage-row">
            <view class="coverage-title">定位覆盖率</view>
            <view class="coverage-value">{{ withLocationCount }}/{{ equipmentList.length }}</view>
          </view>
          <view class="coverage-track">
            <view class="coverage-fill" :style="{ width: `${locationCoveragePercent}%` }"></view>
          </view>
        </view>
      </view>

      <view class="card scene-enter delay-3 control-deck">
        <view class="row section-row">
          <view class="section-title">控制台动作区</view>
          <view class="tag light-tag">先概览后编辑</view>
        </view>
        <view class="console-grid">
          <view class="console-tile">
            <view class="console-label">高优先待处理</view>
            <view class="console-value">{{ attentionCount }}</view>
          </view>
          <view class="console-tile">
            <view class="console-label">定位覆盖率</view>
            <view class="console-value">{{ locationCoveragePercent }}%</view>
          </view>
        </view>
        <view class="console-actions">
          <view class="secondary-btn slim-btn" @click="focusAttentionQueue">定位到优先设备</view>
          <view class="secondary-btn slim-btn" @click="focusMissingLocations">仅看待定位</view>
          <view class="secondary-btn slim-btn" @click="focusPausedOrders">仅看暂停接单</view>
          <view class="secondary-btn slim-btn" @click="clearFilters">重置筛选</view>
        </view>
        <view class="desc" v-if="topAttentionEquipment">
          当前建议先处理：{{ topAttentionEquipment.equipmentName || ('设备 #' + topAttentionEquipment.id) }}
        </view>
      </view>

      <view v-if="filteredEquipmentList.length === 0" class="card scene-enter delay-3">
        <view class="state-title">当前筛选下暂无设备</view>
        <view class="desc">请切换筛选条件，或先补充定位信息。</view>
      </view>

      <view v-else class="device-stack">
        <view
          v-for="(item, index) in filteredEquipmentList"
          :key="item.id"
          class="card device-card lift-card scene-enter"
          :class="[index < 3 ? `delay-${Math.min(index + 1, 4)}` : 'delay-4', attentionCardClass(item)]"
        >
          <view class="device-head">
            <view>
              <view class="device-name">{{ item.equipmentName || ('设备 #' + item.id) }}</view>
              <view class="device-meta">型号 {{ item.brandModel || '未填写' }}</view>
              <view class="device-meta low-noise">设备状态 {{ equipmentStatusLabel(item.currentStatus) }} · 审核 {{ approveStatusLabel(item.approveStatus) }}</view>
            </view>
            <view class="status-group">
              <view class="tag" :class="locationTagClass(item)">{{ hasCoords(item) ? '已定位' : '待定位' }}</view>
              <view class="tag" :class="orderTagClass(item)">{{ item.isAcceptingOrders !== false ? '可接单' : '暂停接单' }}</view>
              <view class="tag" :class="attentionToneClass(item)">{{ attentionLabel(item) }}</view>
            </view>
          </view>

          <view class="summary-grid">
            <view class="summary-box">
              <view class="summary-label">服务半径</view>
              <view class="summary-value">{{ item.serviceRadiusKm || 0 }} km</view>
            </view>
            <view class="summary-box">
              <view class="summary-label">当前位置</view>
              <view class="summary-value small">{{ coordText(item.currentLat, item.currentLng) }}</view>
            </view>
          </view>

          <view class="row panel-toolbar">
            <view class="desc panel-hint">
              {{ expandedEquipmentId === item.id ? '控制台已展开，可编辑坐标并保存。' : '默认收起编辑区，保持概览清晰。' }}
            </view>
            <view class="secondary-btn mini-btn" @click="toggleExpand(item.id)">
              {{ expandedEquipmentId === item.id ? '收起控制台' : '展开控制台' }}
            </view>
          </view>

          <view v-if="expandedEquipmentId === item.id" class="editor-block">
            <view class="field-label">搜索地点</view>
            <input
              v-model="item.locationKeywordDraft"
              class="input"
              placeholder="例如：东河村 / 杭州东站 / 上海虹桥站"
              @input="clearItemSuggestions(item)"
            />

            <view class="search-row">
              <view class="secondary-btn slim-btn" @click="searchLocation(item)">{{ item.searching ? '正在搜索...' : '搜索地点建议' }}</view>
              <view class="secondary-btn slim-btn" @click="locateItem(item)">{{ item.locating ? '正在定位...' : '直接定位' }}</view>
            </view>

            <view class="preset-grid">
              <view
                v-for="preset in locationPresets"
                :key="`${item.id}-${preset.label}`"
                class="preset-chip"
                @click="applyPreset(item, preset)"
              >
                {{ preset.label }}
              </view>
            </view>

            <view v-if="item.suggestions.length > 0" class="suggestion-list">
              <view class="suggestion-title">地点建议</view>
              <view
                v-for="suggestion in item.suggestions"
                :key="`${item.id}-${suggestion.name}-${suggestion.lng}`"
                class="suggestion-item"
                @click="applySuggestion(item, suggestion)"
              >
                <view class="suggestion-main">
                  <view class="suggestion-name">{{ suggestion.name }}</view>
                  <view class="suggestion-address">{{ suggestion.address }}</view>
                </view>
                <view class="suggestion-meta">
                  <view class="tag light-tag">{{ suggestion.tag || '地点' }}</view>
                  <view class="suggestion-source">{{ suggestion.source || 'map' }}</view>
                </view>
              </view>
            </view>

            <view class="coord-panel">
              <view class="coord-top">
                <view class="coord-title">设备坐标</view>
                <view class="tag light-tag">{{ hasCoords(item) ? '可参与精准匹配' : '等待定位' }}</view>
              </view>
              <view class="coord-grid">
                <input v-model="item.currentLatDraft" class="input small-input" placeholder="纬度" />
                <input v-model="item.currentLngDraft" class="input small-input" placeholder="经度" />
              </view>
              <view class="desc">当前：{{ coordText(item.currentLat, item.currentLng) }}</view>
              <view class="desc" v-if="item.geoTip">{{ item.geoTip }}</view>
            </view>

            <view class="primary-btn" @click="saveLocation(item)">{{ item.saving ? '正在保存位置...' : '保存当前位置' }}</view>
          </view>
        </view>
      </view>
    </template>
  </view>
</template>

<script>
import { ownerApi } from '../../api/owner'
import { mapApi } from '../../api/map'
import { ensurePageAccess } from '../../utils/pageAuth'
import { getEquipmentStatusLabel, getApproveStatusLabel } from '../../constants/status'
import { formatCoord } from '../../utils/formatters'

export default {
  data() {
    return {
      loading: false,
      errorText: '',
      equipmentList: [],
      expandedEquipmentId: null,
      locationFilter: 'ALL',
      orderFilter: 'ALL',
      highPriorityOnly: false,
      locationFilterOptions: [
        { value: 'ALL', label: '全部定位' },
        { value: 'LOCATED', label: '仅看已定位' },
        { value: 'MISSING', label: '仅看待定位' }
      ],
      orderFilterOptions: [
        { value: 'ALL', label: '全部接单状态' },
        { value: 'ACCEPTING', label: '仅看可接单' },
        { value: 'PAUSED', label: '仅看暂停接单' }
      ],
      locationPresets: []
    }
  },
  computed: {
    withLocationCount() {
      return this.equipmentList.filter((item) => this.hasCoords(item)).length
    },
    missingLocationCount() {
      return this.equipmentList.length - this.withLocationCount
    },
    acceptingCount() {
      return this.equipmentList.filter((item) => item.isAcceptingOrders !== false).length
    },
    locationCoveragePercent() {
      if (!this.equipmentList.length) return 0
      return Math.round((this.withLocationCount / this.equipmentList.length) * 100)
    },
    filteredEquipmentList() {
      return this.equipmentList.filter((item) => {
        const located = this.hasCoords(item)
        const accepting = item.isAcceptingOrders !== false
        const matchLocation = this.locationFilter === 'ALL'
          || (this.locationFilter === 'LOCATED' && located)
          || (this.locationFilter === 'MISSING' && !located)
        const matchOrder = this.orderFilter === 'ALL'
          || (this.orderFilter === 'ACCEPTING' && accepting)
          || (this.orderFilter === 'PAUSED' && !accepting)
        const matchPriority = !this.highPriorityOnly || this.attentionScore(item) >= 60
        return matchLocation && matchOrder && matchPriority
      })
    },
    attentionCount() {
      return this.equipmentList.filter((item) => this.attentionScore(item) >= 60).length
    },
    topAttentionEquipment() {
      const ranked = [...this.equipmentList].sort((a, b) => this.attentionScore(b) - this.attentionScore(a))
      return ranked[0] || null
    },
    attentionQueue() {
      return [...this.equipmentList]
        .sort((a, b) => this.attentionScore(b) - this.attentionScore(a))
        .filter((item) => this.attentionScore(item) >= 60)
        .slice(0, 3)
    }
  },
  onShow() {
    uni.setNavigationBarTitle({ title: '我的农机' })
    if (!ensurePageAccess('OWNER')) return
    this.loadEquipment()
  },
  onPullDownRefresh() {
    if (!ensurePageAccess('OWNER')) {
      uni.stopPullDownRefresh()
      return
    }
    this.loadEquipment().finally(() => uni.stopPullDownRefresh())
  },
  methods: {
    hasCoords(item) {
      return item.currentLat != null && item.currentLng != null
    },
    setLocationFilter(value) {
      this.locationFilter = value
    },
    setOrderFilter(value) {
      this.orderFilter = value
    },
    clearFilters() {
      this.locationFilter = 'ALL'
      this.orderFilter = 'ALL'
      this.highPriorityOnly = false
    },
    toggleHighPriority() {
      this.highPriorityOnly = !this.highPriorityOnly
    },
    openAttentionItem(item) {
      this.clearFilters()
      this.highPriorityOnly = true
      this.expandedEquipmentId = item.id
      uni.showToast({ title: `已聚焦 ${item.equipmentName || ('设备 #' + item.id)}`, icon: 'none' })
    },
    focusMissingLocations() {
      this.locationFilter = 'MISSING'
      this.orderFilter = 'ALL'
      const target = this.filteredEquipmentList[0]
      this.expandedEquipmentId = target ? target.id : null
    },
    focusPausedOrders() {
      this.locationFilter = 'ALL'
      this.orderFilter = 'PAUSED'
      const target = this.filteredEquipmentList[0]
      this.expandedEquipmentId = target ? target.id : null
    },
    focusAttentionQueue() {
      this.clearFilters()
      if (!this.topAttentionEquipment) return
      this.expandedEquipmentId = this.topAttentionEquipment.id
      uni.showToast({ title: '已定位到优先设备', icon: 'none' })
    },
    toggleExpand(id) {
      this.expandedEquipmentId = this.expandedEquipmentId === id ? null : id
    },
    attentionScore(item) {
      let score = 0
      if (!this.hasCoords(item)) score += 50
      if (item.isAcceptingOrders === false) score += 30
      if (item.approveStatus === 'PENDING') score += 15
      if (item.currentStatus === 'FAULT') score += 10
      return score
    },
    attentionLabel(item) {
      const score = this.attentionScore(item)
      if (score >= 80) return '高优先'
      if (score >= 60) return '需处理'
      if (score >= 30) return '可优化'
      return '状态良好'
    },
    attentionToneClass(item) {
      const score = this.attentionScore(item)
      if (score >= 80) return 'tone-critical'
      if (score >= 60) return 'tone-warn'
      if (score >= 30) return 'tone-info'
      return 'tone-success'
    },
    attentionCardClass(item) {
      const score = this.attentionScore(item)
      if (score >= 80) return 'card-critical'
      if (score >= 60) return 'card-warn'
      return 'card-normal'
    },
    locationTagClass(item) {
      return this.hasCoords(item) ? 'tone-success' : 'tone-warn'
    },
    orderTagClass(item) {
      return item.isAcceptingOrders !== false ? 'tone-info' : 'tone-muted'
    },
    normalizeGeoKeyword(value) {
      const raw = String(value || '').trim().toLowerCase()
      return raw
    },
    equipmentStatusLabel(status) {
      return getEquipmentStatusLabel(status)
    },
    approveStatusLabel(status) {
      return getApproveStatusLabel(status)
    },
    coordText(lat, lng) {
      return formatCoord(lat, lng)
    },
    buildItemState(item) {
      return {
        ...item,
        currentLatDraft: item.currentLat == null ? '' : String(item.currentLat),
        currentLngDraft: item.currentLng == null ? '' : String(item.currentLng),
        locationKeywordDraft: '',
        geoTip: '',
        locating: false,
        searching: false,
        saving: false,
        suggestions: []
      }
    },
    clearItemSuggestions(item) {
      item.suggestions = []
      item.geoTip = ''
    },
    applyPreset(item, preset) {
      item.locationKeywordDraft = preset.keyword
      item.currentLatDraft = String(preset.lat)
      item.currentLngDraft = String(preset.lng)
      item.geoTip = `已切换到 ${preset.label} 预设位置`
      item.suggestions = []
    },
    applySuggestion(item, suggestion) {
      item.locationKeywordDraft = suggestion.name
      item.currentLatDraft = String(suggestion.lat)
      item.currentLngDraft = String(suggestion.lng)
      item.geoTip = `已选择“${suggestion.name}”，来源：${suggestion.source || 'map'}`
      item.suggestions = []
      uni.showToast({ title: '地点已选中', icon: 'success' })
    },
    async searchLocation(item) {
      if (item.searching) return
      if (!item.locationKeywordDraft) {
        uni.showToast({ title: '请先输入地点名', icon: 'none' })
        return
      }
      item.searching = true
      try {
        const result = await mapApi.search(this.normalizeGeoKeyword(item.locationKeywordDraft))
        item.suggestions = result && result.items ? result.items : []
        if (item.suggestions.length === 0) {
          uni.showToast({ title: '没有找到地点建议', icon: 'none' })
        }
      } catch (error) {
        uni.showToast({ title: (error && error.message) || '搜索地点失败', icon: 'none' })
      } finally {
        item.searching = false
      }
    },
    async locateItem(item) {
      if (item.locating) return
      if (!item.locationKeywordDraft) {
        uni.showToast({ title: '请先输入地点名', icon: 'none' })
        return
      }
      item.locating = true
      try {
        const result = await mapApi.geocode(this.normalizeGeoKeyword(item.locationKeywordDraft))
        if (!result || !result.matched) {
          uni.showToast({ title: (result && result.message) || '未匹配到坐标', icon: 'none' })
          return
        }
        item.currentLatDraft = String(result.lat)
        item.currentLngDraft = String(result.lng)
        item.geoTip = `已按“${result.formattedAddress || item.locationKeywordDraft}”完成定位，来源：${result.source || 'map'}`
        uni.showToast({ title: '定位成功', icon: 'success' })
      } catch (error) {
        uni.showToast({ title: (error && error.message) || '定位失败', icon: 'none' })
      } finally {
        item.locating = false
      }
    },
    async saveLocation(item) {
      if (item.saving) return
      const currentLat = Number(item.currentLatDraft)
      const currentLng = Number(item.currentLngDraft)
      if (!Number.isFinite(currentLat) || !Number.isFinite(currentLng)) {
        uni.showToast({ title: '请输入有效坐标', icon: 'none' })
        return
      }
      item.saving = true
      try {
        const updated = await ownerApi.updateEquipment(item.id, {
          machineTypeId: item.machineTypeId,
          equipmentName: item.equipmentName,
          brandModel: item.brandModel,
          quantity: item.quantity,
          baseRegionCode: item.baseRegionCode,
          serviceRadiusKm: item.serviceRadiusKm,
          currentLat,
          currentLng
        })
        item.currentLat = updated.currentLat
        item.currentLng = updated.currentLng
        item.currentLatDraft = updated.currentLat == null ? '' : String(updated.currentLat)
        item.currentLngDraft = updated.currentLng == null ? '' : String(updated.currentLng)
        uni.showToast({ title: '位置已更新', icon: 'success' })
      } catch (error) {
        uni.showToast({ title: (error && error.message) || '位置更新失败', icon: 'none' })
      } finally {
        item.saving = false
      }
    },
    async loadEquipment() {
      this.loading = true
      this.errorText = ''
      try {
        this.equipmentList = (await ownerApi.listEquipment()).map((item) => this.buildItemState(item))
        const target = [...this.equipmentList].sort((a, b) => this.attentionScore(b) - this.attentionScore(a))[0]
        this.expandedEquipmentId = target ? target.id : null
      } catch (error) {
        this.equipmentList = []
        this.expandedEquipmentId = null
        this.errorText = (error && error.message) || '加载设备失败'
      } finally {
        this.loading = false
      }
    }
  }
}
</script>

<style>
.hero-badges { display: flex; flex-wrap: wrap; gap: 12rpx; margin-top: 16rpx; }
.fleet-overview { margin-top: 6rpx; background: linear-gradient(180deg, rgba(255,255,255,0.96), rgba(247,243,232,0.96)); }
.overview-head { display: flex; justify-content: space-between; gap: 18rpx; align-items: flex-start; margin-bottom: 18rpx; }
.overview-grid { display: grid; grid-template-columns: repeat(2, 1fr); gap: 14rpx; }
.overview-tile { padding: 18rpx 16rpx; border-radius: 24rpx; background: rgba(255,255,255,0.68); border: 1rpx solid rgba(90, 80, 48, 0.08); box-shadow: 0 10rpx 20rpx rgba(81, 63, 25, 0.04); }
.overview-value { font-size: 32rpx; font-weight: 800; color: #23482e; line-height: 1.15; }
.overview-label { margin-top: 8rpx; font-size: 22rpx; color: #6f7b68; }
.warning-tile { background: linear-gradient(180deg, rgba(255, 246, 232, 0.92), rgba(255, 239, 214, 0.94)); }
.decision-queue-card { margin-top: 12rpx; background: rgba(255, 255, 255, 0.94); }
.queue-item { display: flex; align-items: center; justify-content: space-between; gap: 16rpx; padding: 16rpx; border-radius: 18rpx; background: rgba(255, 255, 255, 0.72); border: 1rpx solid rgba(90, 80, 48, 0.08); }
.queue-item + .queue-item { margin-top: 10rpx; }
.queue-main { flex: 1; min-width: 0; }
.queue-name { font-size: 27rpx; font-weight: 700; color: #294f33; line-height: 1.3; }
.queue-meta { margin-top: 8rpx; color: #6b7867; font-size: 22rpx; }
.queue-cta { color: #2f6238; font-size: 22rpx; font-weight: 600; }
.filter-card { margin-top: 12rpx; background: rgba(255, 255, 255, 0.94); }
.filter-head { margin-bottom: 14rpx; }
.filter-row { display: flex; flex-wrap: wrap; gap: 12rpx; margin-bottom: 12rpx; }
.filter-chip { padding: 10rpx 20rpx; border-radius: 999rpx; background: rgba(246, 244, 237, 0.95); color: #546650; font-size: 24rpx; }
.filter-chip.active { background: linear-gradient(135deg, #2f6b3d, #3a7b48); color: #fff; box-shadow: 0 8rpx 16rpx rgba(41, 94, 56, 0.22); }
.control-deck { margin-top: 10rpx; background: linear-gradient(180deg, rgba(255,255,255,0.98), rgba(243,247,239,0.95)); border: 1rpx solid rgba(66, 95, 57, 0.14); }
.section-row { margin-bottom: 14rpx; }
.console-grid { display: grid; grid-template-columns: repeat(2, 1fr); gap: 12rpx; margin-bottom: 14rpx; }
.console-tile { border-radius: 20rpx; padding: 16rpx; background: rgba(255, 255, 255, 0.74); border: 1rpx solid rgba(79, 101, 62, 0.12); }
.console-label { font-size: 22rpx; color: #6e7d67; }
.console-value { margin-top: 8rpx; font-size: 34rpx; font-weight: 800; color: #24462f; }
.console-actions { display: flex; flex-wrap: wrap; gap: 12rpx; margin-bottom: 10rpx; }
.coverage-block { margin-top: 8rpx; padding: 16rpx; border-radius: 20rpx; background: rgba(245, 247, 242, 0.95); }
.coverage-row { display: flex; align-items: center; justify-content: space-between; margin-bottom: 10rpx; }
.coverage-title { font-size: 24rpx; color: #5d6f5a; }
.coverage-value { font-size: 26rpx; font-weight: 700; color: #264b31; }
.coverage-track { height: 12rpx; border-radius: 999rpx; background: rgba(70, 95, 61, 0.12); overflow: hidden; }
.coverage-fill { height: 100%; border-radius: inherit; background: linear-gradient(90deg, #2f6b3d, #77a248); }
.device-stack { display: flex; flex-direction: column; gap: 18rpx; }
.device-card { padding-top: 24rpx; position: relative; overflow: hidden; }
.device-card::before { content: ''; position: absolute; left: 0; top: 0; bottom: 0; width: 8rpx; background: linear-gradient(180deg, rgba(47,107,61,0.9), rgba(198,146,47,0.85)); }
.card-critical { border: 1rpx solid rgba(177, 98, 25, 0.30); box-shadow: 0 16rpx 30rpx rgba(160, 90, 22, 0.14); }
.card-warn { border: 1rpx solid rgba(165, 128, 42, 0.22); box-shadow: 0 14rpx 28rpx rgba(145, 113, 36, 0.10); }
.card-normal { border: 1rpx solid rgba(82, 101, 74, 0.12); }
.device-head { display: flex; justify-content: space-between; gap: 18rpx; align-items: flex-start; }
.device-name, .state-title { font-size: 34rpx; font-weight: 700; color: #264530; }
.device-meta { margin-top: 10rpx; color: #677864; }
.device-meta.low-noise { font-size: 22rpx; color: #7a8578; }
.status-group { display: flex; flex-wrap: wrap; gap: 10rpx; justify-content: flex-end; }
.summary-grid { display: grid; grid-template-columns: repeat(2, 1fr); gap: 16rpx; margin: 22rpx 0; }
.summary-box { background: rgba(255,255,255,0.70); border-radius: 22rpx; padding: 18rpx; border: 1rpx solid rgba(90, 80, 48, 0.08); }
.summary-label { font-size: 24rpx; color: #74836f; margin-bottom: 10rpx; }
.summary-value { font-size: 30rpx; font-weight: 700; color: #274633; }
.summary-value.small { font-size: 24rpx; line-height: 1.5; }
.panel-toolbar { margin-top: 4rpx; margin-bottom: 10rpx; align-items: center; gap: 14rpx; }
.panel-hint { flex: 1; margin: 0; }
.mini-btn { width: auto; min-height: 64rpx; display: inline-flex; align-items: center; padding: 0 18rpx; }
.editor-block { margin-top: 10rpx; }
.field-label { margin: 14rpx 0 12rpx; font-size: 26rpx; font-weight: 700; color: #2c4e36; }
.search-row { display: flex; gap: 14rpx; margin-top: 14rpx; }
.slim-btn { flex: 1; justify-content: center; }
.preset-grid { display: flex; flex-wrap: wrap; gap: 12rpx; margin-top: 16rpx; }
.preset-chip { padding: 12rpx 18rpx; border-radius: 999rpx; background: rgba(255,255,255,0.62); color: #32543c; font-weight: 600; }
.suggestion-list { margin-top: 18rpx; padding: 18rpx; border-radius: 24rpx; background: rgba(255,255,255,0.72); border: 1rpx solid rgba(90, 80, 48, 0.08); }
.suggestion-title, .coord-title { font-size: 28rpx; font-weight: 700; color: #274532; margin-bottom: 12rpx; }
.suggestion-item { display: flex; justify-content: space-between; gap: 16rpx; padding: 16rpx 0; }
.suggestion-item + .suggestion-item { border-top: 1rpx solid rgba(84, 103, 75, 0.10); }
.suggestion-name { font-size: 28rpx; font-weight: 700; color: #284633; }
.suggestion-address { margin-top: 8rpx; color: #6d7a69; }
.suggestion-meta { display: flex; flex-direction: column; align-items: flex-end; gap: 10rpx; }
.suggestion-source { color: #71806d; font-size: 24rpx; }
.coord-panel { margin-top: 18rpx; padding: 18rpx; border-radius: 24rpx; background: rgba(255,255,255,0.72); border: 1rpx solid rgba(90, 80, 48, 0.08); }
.coord-top { display: flex; justify-content: space-between; gap: 18rpx; align-items: center; margin-bottom: 12rpx; }
.coord-grid { display: grid; grid-template-columns: repeat(2, 1fr); gap: 14rpx; margin-bottom: 12rpx; }
.small-input { min-height: 86rpx; }
.tone-critical { background: rgba(255, 232, 220, 0.96); color: #9a3f00; }
.tone-warn { background: rgba(255, 241, 210, 0.92); color: #8d5d00; }
.tone-info { background: rgba(225, 241, 255, 0.92); color: #235f8b; }
.tone-success { background: rgba(226, 248, 228, 0.92); color: #23653a; }
.tone-muted { background: rgba(236, 237, 233, 0.92); color: #626b61; }
</style>
