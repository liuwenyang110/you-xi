<template>
  <view class="page">
    <view class="card hero-card glass-panel scene-enter">
      <view class="section-caption">服务能力中台</view>
      <view class="hero-title">我的服务项</view>
      <view class="hero-subtitle">先看优先级，再逐条优化。平台会优先派发给“已启用 + 可接单 + 条件完整”的服务项。</view>
      <view class="hero-badges">
        <view class="tag">能力标签</view>
        <view class="tag">接单状态</view>
        <view class="tag">优先级评分</view>
      </view>
    </view>

    <view class="card scene-enter delay-1" v-if="loading">
      <view class="state-title">正在加载服务项</view>
      <view class="desc">请稍候，系统正在同步服务能力信息。</view>
    </view>
    <view class="card scene-enter delay-1" v-else-if="errorText">
      <view class="state-title">服务项加载失败</view>
      <view class="warning">{{ errorText }}</view>
    </view>
    <view class="card scene-enter delay-1" v-else-if="serviceItems.length === 0">
      <view class="state-title">当前暂无服务项记录</view>
      <view class="desc">补齐服务能力后，平台才能进行更准确的智能匹配。</view>
    </view>

    <template v-else>
      <view class="card scene-enter delay-2 service-overview">
        <view class="overview-head">
          <view>
            <view class="section-title">能力概览</view>
            <view class="desc">色彩强调服务优先级，不平均用力，帮助你快速决定先处理哪一项。</view>
          </view>
          <view class="tag light-tag">优先运营</view>
        </view>
        <view class="overview-grid">
          <view class="overview-tile">
            <view class="overview-value">{{ serviceItems.length }}</view>
            <view class="overview-label">服务总数</view>
          </view>
          <view class="overview-tile">
            <view class="overview-value">{{ activeCount }}</view>
            <view class="overview-label">启用中</view>
          </view>
          <view class="overview-tile">
            <view class="overview-value">{{ acceptingCount }}</view>
            <view class="overview-label">可接单</view>
          </view>
          <view class="overview-tile warning-tile">
            <view class="overview-value">{{ needsActionCount }}</view>
            <view class="overview-label">待优化</view>
          </view>
        </view>
      </view>

      <view class="card scene-enter delay-2 ops-deck">
        <view class="row section-row">
          <view class="section-title">运营控制台</view>
          <view class="tag light-tag">决策前置</view>
        </view>
        <input
          v-model.trim="keyword"
          class="input"
          placeholder="搜索服务名、作物标签、地形标签"
        />
        <view class="sort-row">
          <view
            v-for="option in sortOptions"
            :key="option.value"
            class="sort-chip"
            :class="{ active: sortMode === option.value }"
            @click="setSortMode(option.value)"
          >
            {{ option.label }}
          </view>
        </view>
        <view class="desc" v-if="topPriorityItem">
          建议优先关注：{{ topPriorityItem.serviceName || ('服务 #' + topPriorityItem.id) }}（{{ priorityLabel(topPriorityItem) }}）
        </view>
      </view>

      <view class="card scene-enter delay-2 filter-card">
        <view class="section-title">服务筛选</view>
        <view class="filter-row">
          <view
            v-for="option in statusFilterOptions"
            :key="option.value"
            class="filter-chip"
            :class="{ active: statusFilter === option.value }"
            @click="setStatusFilter(option.value)"
          >
            {{ option.label }}
          </view>
        </view>
        <view class="filter-row">
          <view
            v-for="option in acceptFilterOptions"
            :key="option.value"
            class="filter-chip"
            :class="{ active: acceptFilter === option.value }"
            @click="setAcceptFilter(option.value)"
          >
            {{ option.label }}
          </view>
        </view>
      </view>

      <view v-if="filteredServiceItems.length === 0" class="card scene-enter delay-3">
        <view class="state-title">当前筛选下暂无服务项</view>
        <view class="desc">请调整筛选条件，或清空关键词后重试。</view>
      </view>

      <view v-else class="service-stack">
        <view
          v-for="(item, index) in filteredServiceItems"
          :key="item.id"
          class="card service-card lift-card scene-enter"
          :class="[index < 3 ? `delay-${Math.min(index + 1, 4)}` : 'delay-4', serviceCardClass(item)]"
        >
          <view class="service-head">
            <view>
              <view class="service-name">{{ item.serviceName || ('服务 #' + item.id) }}</view>
              <view class="service-meta">状态：{{ serviceStatusLabel(item.status) }}</view>
            </view>
            <view class="status-group">
              <view class="tag" :class="serviceToneClass(item.status)">{{ serviceStatusLabel(item.status) }}</view>
              <view class="tag" :class="acceptToneClass(item.isAcceptingOrders)">{{ item.isAcceptingOrders ? '当前接单中' : '暂停接单' }}</view>
              <view class="tag" :class="priorityToneClass(item)">{{ priorityLabel(item) }}</view>
            </view>
          </view>

          <view class="priority-bar">
            <view class="priority-fill" :style="{ width: `${priorityScore(item)}%` }"></view>
          </view>

          <view class="summary-grid">
            <view class="summary-box">
              <view class="summary-label">作物标签</view>
              <view class="summary-value small">{{ item.cropTags || '未填写' }}</view>
            </view>
            <view class="summary-box">
              <view class="summary-label">面积范围</view>
              <view class="summary-value small">{{ item.minAreaMu || 0 }} - {{ item.maxAreaMu || 0 }} 亩</view>
            </view>
          </view>

          <view class="detail-panel">
            <view class="detail-row">
              <view class="detail-label">地形标签</view>
              <view class="detail-value">{{ item.terrainTags || '未填写' }}</view>
            </view>
            <view class="detail-row">
              <view class="detail-label">地块标签</view>
              <view class="detail-value">{{ item.plotTags || '未填写' }}</view>
            </view>
            <view class="detail-row">
              <view class="detail-label">服务半径</view>
              <view class="detail-value">{{ item.serviceRadiusKm || 0 }} 公里</view>
            </view>
            <view class="detail-row">
              <view class="detail-label">优先级得分</view>
              <view class="detail-value">{{ priorityScore(item) }}/100</view>
            </view>
          </view>
        </view>
      </view>
    </template>
  </view>
</template>

<script>
import { ownerApi } from '../../api/owner'
import { ensurePageAccess } from '../../utils/pageAuth'

export default {
  data() {
    return {
      loading: false,
      errorText: '',
      serviceItems: [],
      keyword: '',
      sortMode: 'PRIORITY_DESC',
      statusFilter: 'ALL',
      acceptFilter: 'ALL',
      sortOptions: [
        { value: 'PRIORITY_DESC', label: '按优先级' },
        { value: 'RADIUS_DESC', label: '按半径' },
        { value: 'NAME_ASC', label: '按名称' }
      ],
      statusFilterOptions: [
        { value: 'ALL', label: '全部状态' },
        { value: 'ACTIVE', label: '仅看启用中' },
        { value: 'DISABLED', label: '仅看已停用' }
      ],
      acceptFilterOptions: [
        { value: 'ALL', label: '全部接单状态' },
        { value: 'ACCEPTING', label: '仅看接单中' },
        { value: 'PAUSED', label: '仅看暂停接单' }
      ]
    }
  },
  computed: {
    activeCount() {
      return this.serviceItems.filter((item) => item.status === 'ACTIVE').length
    },
    acceptingCount() {
      return this.serviceItems.filter((item) => item.isAcceptingOrders).length
    },
    needsActionCount() {
      return this.serviceItems.filter((item) => this.priorityScore(item) < 70).length
    },
    topPriorityItem() {
      const ranked = [...this.serviceItems].sort((a, b) => this.priorityScore(b) - this.priorityScore(a))
      return ranked[0] || null
    },
    rankedServiceItems() {
      const list = this.serviceItems.filter((item) => {
        const matchStatus = this.statusFilter === 'ALL' || item.status === this.statusFilter
        const matchAccept = this.acceptFilter === 'ALL'
          || (this.acceptFilter === 'ACCEPTING' && item.isAcceptingOrders)
          || (this.acceptFilter === 'PAUSED' && !item.isAcceptingOrders)
        const text = `${item.serviceName || ''} ${item.cropTags || ''} ${item.terrainTags || ''} ${item.plotTags || ''}`.toLowerCase()
        const matchKeyword = !this.keyword || text.includes(this.keyword.toLowerCase())
        return matchStatus && matchAccept && matchKeyword
      })
      if (this.sortMode === 'RADIUS_DESC') {
        return list.sort((a, b) => Number(b.serviceRadiusKm || 0) - Number(a.serviceRadiusKm || 0))
      }
      if (this.sortMode === 'NAME_ASC') {
        return list.sort((a, b) => String(a.serviceName || '').localeCompare(String(b.serviceName || '')))
      }
      return list.sort((a, b) => this.priorityScore(b) - this.priorityScore(a))
    },
    filteredServiceItems() {
      return this.rankedServiceItems
    }
  },
  onShow() {
    uni.setNavigationBarTitle({ title: '我的服务项' })
    if (!ensurePageAccess('OWNER')) return
    this.loadServiceItems()
  },
  onPullDownRefresh() {
    if (!ensurePageAccess('OWNER')) {
      uni.stopPullDownRefresh()
      return
    }
    this.loadServiceItems().finally(() => uni.stopPullDownRefresh())
  },
  methods: {
    setStatusFilter(value) {
      this.statusFilter = value
    },
    setAcceptFilter(value) {
      this.acceptFilter = value
    },
    setSortMode(value) {
      this.sortMode = value
    },
    serviceStatusLabel(status) {
      const map = {
        ACTIVE: '启用中',
        DISABLED: '已停用'
      }
      return map[status] || status || '未知状态'
    },
    serviceToneClass(status) {
      return status === 'ACTIVE' ? 'tone-success' : 'tone-muted'
    },
    acceptToneClass(flag) {
      return flag ? 'tone-info' : 'tone-warn'
    },
    priorityScore(item) {
      let score = 0
      if (item.status === 'ACTIVE') score += 35
      if (item.isAcceptingOrders) score += 30
      if (Number(item.serviceRadiusKm || 0) >= 10) score += 15
      if (item.cropTags) score += 10
      if (item.terrainTags || item.plotTags) score += 10
      return Math.min(score, 100)
    },
    priorityLabel(item) {
      const score = this.priorityScore(item)
      if (score >= 85) return '高优先'
      if (score >= 70) return '主力'
      if (score >= 50) return '可优化'
      return '待处理'
    },
    priorityToneClass(item) {
      const score = this.priorityScore(item)
      if (score >= 85) return 'tone-success'
      if (score >= 70) return 'tone-info'
      if (score >= 50) return 'tone-warn'
      return 'tone-critical'
    },
    serviceCardClass(item) {
      const score = this.priorityScore(item)
      if (score >= 85) return 'card-high'
      if (score >= 70) return 'card-medium'
      return 'card-low'
    },
    async loadServiceItems() {
      this.loading = true
      this.errorText = ''
      try {
        this.serviceItems = await ownerApi.listServiceItems()
      } catch (error) {
        this.serviceItems = []
        this.errorText = (error && error.message) || '加载服务项失败'
      } finally {
        this.loading = false
      }
    }
  }
}
</script>

<style>
.hero-badges { display: flex; flex-wrap: wrap; gap: 12rpx; margin-top: 16rpx; }
.service-overview { margin-top: 6rpx; background: linear-gradient(180deg, rgba(255,255,255,0.96), rgba(247,243,232,0.96)); }
.overview-head { display: flex; justify-content: space-between; gap: 18rpx; align-items: flex-start; margin-bottom: 18rpx; }
.overview-grid { display: grid; grid-template-columns: repeat(2, 1fr); gap: 14rpx; }
.overview-tile { padding: 18rpx 16rpx; border-radius: 24rpx; background: rgba(255,255,255,0.68); border: 1rpx solid rgba(90, 80, 48, 0.08); box-shadow: 0 10rpx 20rpx rgba(81, 63, 25, 0.04); }
.overview-value { font-size: 32rpx; font-weight: 800; color: #23482e; line-height: 1.15; }
.overview-label { margin-top: 8rpx; font-size: 22rpx; color: #6f7b68; }
.warning-tile { background: linear-gradient(180deg, rgba(255, 246, 232, 0.92), rgba(255, 239, 214, 0.94)); }
.ops-deck { margin-top: 12rpx; background: linear-gradient(180deg, rgba(255,255,255,0.98), rgba(244,247,240,0.95)); border: 1rpx solid rgba(66, 95, 57, 0.14); }
.section-row { margin-bottom: 12rpx; }
.sort-row { display: flex; flex-wrap: wrap; gap: 12rpx; margin-top: 14rpx; margin-bottom: 12rpx; }
.sort-chip { padding: 10rpx 20rpx; border-radius: 999rpx; background: rgba(246, 244, 237, 0.95); color: #546650; font-size: 24rpx; }
.sort-chip.active { background: linear-gradient(135deg, #2f6b3d, #3a7b48); color: #fff; box-shadow: 0 8rpx 16rpx rgba(41, 94, 56, 0.22); }
.filter-card { margin-top: 12rpx; background: rgba(255, 255, 255, 0.94); }
.filter-row { display: flex; flex-wrap: wrap; gap: 12rpx; margin-top: 12rpx; }
.filter-chip { padding: 10rpx 20rpx; border-radius: 999rpx; background: rgba(246, 244, 237, 0.95); color: #546650; font-size: 24rpx; }
.filter-chip.active { background: linear-gradient(135deg, #2f6b3d, #3a7b48); color: #fff; box-shadow: 0 8rpx 16rpx rgba(41, 94, 56, 0.22); }
.service-stack { display: flex; flex-direction: column; gap: 18rpx; }
.service-card { padding-top: 24rpx; position: relative; overflow: hidden; }
.service-card::before { content: ''; position: absolute; left: 0; top: 0; bottom: 0; width: 8rpx; }
.card-high::before { background: linear-gradient(180deg, rgba(47,107,61,0.95), rgba(85,151,73,0.9)); }
.card-medium::before { background: linear-gradient(180deg, rgba(45,100,137,0.9), rgba(82,144,187,0.9)); }
.card-low::before { background: linear-gradient(180deg, rgba(176,113,30,0.9), rgba(214,150,58,0.9)); }
.card-high { border: 1rpx solid rgba(63, 109, 57, 0.18); box-shadow: 0 14rpx 30rpx rgba(58, 102, 56, 0.12); }
.card-medium { border: 1rpx solid rgba(61, 102, 130, 0.18); box-shadow: 0 14rpx 28rpx rgba(61, 101, 130, 0.10); }
.card-low { border: 1rpx solid rgba(167, 120, 44, 0.2); box-shadow: 0 14rpx 28rpx rgba(152, 110, 41, 0.10); }
.service-head { display: flex; justify-content: space-between; gap: 18rpx; align-items: flex-start; }
.service-name, .state-title { font-size: 34rpx; font-weight: 700; color: #264530; }
.service-meta { margin-top: 10rpx; color: #677864; }
.status-group { display: flex; flex-wrap: wrap; gap: 10rpx; justify-content: flex-end; }
.priority-bar { margin-top: 16rpx; height: 10rpx; border-radius: 999rpx; background: rgba(74, 95, 65, 0.12); overflow: hidden; }
.priority-fill { height: 100%; border-radius: inherit; background: linear-gradient(90deg, #2f6b3d, #77a248); }
.summary-grid { display: grid; grid-template-columns: repeat(2, 1fr); gap: 16rpx; margin: 20rpx 0; }
.summary-box { background: rgba(255,255,255,0.62); border-radius: 22rpx; padding: 18rpx; border: 1rpx solid rgba(90, 80, 48, 0.08); }
.summary-label { font-size: 24rpx; color: #74836f; margin-bottom: 10rpx; }
.summary-value { font-size: 30rpx; font-weight: 700; color: #274633; }
.summary-value.small { font-size: 24rpx; line-height: 1.6; }
.detail-panel { padding: 18rpx; border-radius: 24rpx; background: rgba(255,255,255,0.62); border: 1rpx solid rgba(90, 80, 48, 0.08); }
.detail-row { display: flex; justify-content: space-between; gap: 24rpx; padding: 14rpx 0; }
.detail-row + .detail-row { border-top: 1rpx solid rgba(84, 103, 75, 0.10); }
.detail-label { color: #72806d; }
.detail-value { max-width: 60%; text-align: right; color: #2b4836; font-weight: 600; }
.tone-critical { background: rgba(255, 232, 220, 0.96); color: #9a3f00; }
.tone-warn { background: rgba(255, 241, 210, 0.92); color: #8d5d00; }
.tone-info { background: rgba(225, 241, 255, 0.92); color: #235f8b; }
.tone-success { background: rgba(226, 248, 228, 0.92); color: #23653a; }
.tone-muted { background: rgba(236, 237, 233, 0.92); color: #626b61; }
</style>
