<template>
  <view class="dashboard-page">
    <view class="page-header">
      <text class="page-title">📊 数据看板</text>
      <text class="page-sub">农助手平台运营总览</text>
    </view>

    <!-- 核心指标卡 -->
    <view class="kpi-grid">
      <view class="kpi-card">
        <text class="kpi-icon">🗺</text>
        <text class="kpi-num">{{ stats.zoneCount || 0 }}</text>
        <text class="kpi-label">片区总数</text>
      </view>
      <view class="kpi-card">
        <text class="kpi-icon"></text>
        <text class="kpi-num">{{ stats.farmerCount || 0 }}</text>
        <text class="kpi-label">农户数</text>
      </view>
      <view class="kpi-card">
        <text class="kpi-icon"></text>
        <text class="kpi-num">{{ stats.operatorCount || 0 }}</text>
        <text class="kpi-label">农机手</text>
      </view>
      <view class="kpi-card">
        <text class="kpi-icon">⚙️</text>
        <text class="kpi-num">{{ stats.machineryCount || 0 }}</text>
        <text class="kpi-label">登记农机</text>
      </view>
    </view>

    <!-- 需求状态分布 -->
    <view class="section-card">
      <text class="section-title">需求分布</text>
      <view class="progress-list">
        <view class="progress-item" v-for="(item, i) in demandStats" :key="i">
          <view class="progress-row">
            <text class="progress-label">{{ item.label }}</text>
            <text class="progress-count">{{ item.count }}</text>
          </view>
          <view class="progress-bar-bg">
            <view
              class="progress-bar-fill"
              :style="{ width: totalDemands ? (item.count / totalDemands * 100) + '%' : '0%', background: item.color }"
            ></view>
          </view>
        </view>
      </view>
    </view>

    <!-- 农机大类分布 -->
    <view class="section-card">
      <text class="section-title">🔧 农机大类构成</text>
      <view class="machine-chips">
        <view
          v-for="mc in machineryStats"
          :key="mc.categoryId"
          class="machine-chip"
        >
          <text class="mc-icon">{{ mc.icon || '🚜' }}</text>
          <text class="mc-name">{{ mc.categoryName }}</text>
          <text class="mc-cnt">{{ mc.cnt }}台</text>
        </view>
      </view>
    </view>

    <!-- 活跃片区 Top5 -->
    <view class="section-card">
      <text class="section-title">🏆 活跃片区 Top 5</text>
      <view class="top-list">
        <view
          class="top-item"
          v-for="(zone, idx) in topZones" :key="zone.id"
        >
          <view class="top-rank" :class="idx < 3 ? 'rank-top' : ''">{{ idx + 1 }}</view>
          <view class="top-info">
            <text class="top-name">{{ zone.name }}</text>
            <text class="top-meta">农机手 {{ zone.operatorCount }} · 农机 {{ zone.machineryCount }}</text>
          </view>
          <view class="top-score">{{ zone.operatorCount + zone.machineryCount }}</view>
        </view>
      </view>
    </view>

    <!-- 时间轴：最近动态 -->
    <view class="section-card last">
      <text class="section-title">⏱ 最近平台动态</text>
      <view class="timeline">
        <view class="timeline-item" v-for="(event, i) in recentEvents" :key="i">
          <view class="timeline-dot" :class="'event-' + event.type"></view>
          <view class="timeline-content">
            <text class="event-text">{{ event.text }}</text>
            <text class="event-time">{{ event.time }}</text>
          </view>
        </view>
      </view>
    </view>
  </view>
</template>

<script>
import request from '@/api/v3/request'

export default {
  name: 'DataDashboard',
  data() {
    return {
      loading: false,
      stats: {},
      machineryStats: [],
      topZones: [],
      recentEvents: [],
      demandStats: [
        { label: '🟡 待联系', key: 'pendingCount', count: 0, color: '#FFB300' },
        { label: '🔵 已联系', key: 'contactedCount', count: 0, color: '#42A5F5' },
        { label: '🟢 已完成', key: 'doneCount', count: 0, color: '#4CAF50' },
        { label: '⚫ 已取消', key: 'cancelledCount', count: 0, color: '#bbb' },
      ],
    }
  },
  computed: {
    totalDemands() { return this.demandStats.reduce((s, d) => s + d.count, 0) }
  },
  async onLoad() {
    await this.loadDashboard()
  },
  async onPullDownRefresh() {
    await this.loadDashboard()
    uni.stopPullDownRefresh()
  },
  methods: {
    async loadDashboard() {
      this.loading = true
      try {
        const res = await request.get('/api/v3/admin/dashboard')
        if (res.code === 0) {
          const d = res.data
          this.stats = d.overview || {}
          this.machineryStats = d.machineryStats || []
          this.topZones = (d.zones || [])
            .sort((a, b) => (b.operatorCount + b.machineryCount) - (a.operatorCount + a.machineryCount))
            .slice(0, 5)
          this.demandStats.forEach(ds => {
            ds.count = d.demandStats?.[ds.key] || 0
          })
          this.recentEvents = d.recentEvents || []
        } else {
          // API 返回非 200，显示空状态
          this.stats = {}
        }
      } catch (e) {
        this.stats = {}
      } finally {
        this.loading = false
      }
    }
  }
}
</script>

<style>
.dashboard-page { background: #F0F4ED; min-height: 100vh; padding-bottom: 60rpx; }
.page-header { background: linear-gradient(135deg, #1A3D10, #2D5A1B); padding: 80rpx 40rpx 40rpx; }
.page-title { display: block; font-size: 42rpx; font-weight: 700; color: #fff; }
.page-sub { display: block; font-size: 26rpx; color: rgba(255,255,255,0.8); margin-top: 6rpx; }

.kpi-grid { display: flex; flex-wrap: wrap; padding: 16rpx 16rpx 0; gap: 12rpx; }
.kpi-card { flex: 1 1 calc(50% - 6rpx); background: #fff; border-radius: 20rpx; padding: 28rpx 20rpx; text-align: center; box-shadow: 0 3rpx 14rpx rgba(0,0,0,0.07); }
.kpi-icon { display: block; font-size: 52rpx; margin-bottom: 12rpx; }
.kpi-num { display: block; font-size: 56rpx; font-weight: 800; color: #2D5A1B; line-height: 1; }
.kpi-label { display: block; font-size: 26rpx; color: #888; margin-top: 8rpx; }

.section-card { background: #fff; border-radius: 20rpx; margin: 16rpx 16rpx 0; padding: 28rpx; box-shadow: 0 3rpx 14rpx rgba(0,0,0,0.07); }
.section-card.last { margin-bottom: 0; }
.section-title { display: block; font-size: 32rpx; font-weight: 700; color: #1E3A12; margin-bottom: 24rpx; }

.progress-item { margin-bottom: 20rpx; }
.progress-row { display: flex; justify-content: space-between; margin-bottom: 8rpx; }
.progress-label { font-size: 26rpx; color: #555; }
.progress-count { font-size: 26rpx; font-weight: 700; color: #1E3A12; }
.progress-bar-bg { height: 16rpx; background: #F0F0F0; border-radius: 8rpx; overflow: hidden; }
.progress-bar-fill { height: 100%; border-radius: 8rpx; transition: width .4s ease; }

.machine-chips { display: flex; flex-wrap: wrap; gap: 16rpx; }
.machine-chip { background: #F5F8F2; border-radius: 16rpx; padding: 16rpx 20rpx; display: flex; align-items: center; gap: 10rpx; }
.mc-icon { font-size: 32rpx; }
.mc-name { font-size: 26rpx; color: #2D5A1B; font-weight: 600; }
.mc-cnt { font-size: 26rpx; color: #4CAF50; font-weight: 700; }

.top-item { display: flex; align-items: center; padding: 16rpx 0; border-bottom: 1rpx solid #F5F5F5; }
.top-item:last-child { border-bottom: none; }
.top-rank { width: 48rpx; height: 48rpx; border-radius: 50%; background: #EEE; display: flex; align-items: center; justify-content: center; font-size: 24rpx; font-weight: 700; color: #888; flex-shrink: 0; }
.top-rank.rank-top { background: linear-gradient(135deg, #FFB300, #FF8F00); color: #fff; }
.top-info { flex: 1; padding: 0 16rpx; }
.top-name { display: block; font-size: 28rpx; font-weight: 700; color: #1E3A12; }
.top-meta { display: block; font-size: 22rpx; color: #999; margin-top: 4rpx; }
.top-score { font-size: 32rpx; font-weight: 800; color: #4CAF50; }

.timeline-item { display: flex; align-items: flex-start; gap: 16rpx; padding: 14rpx 0; }
.timeline-dot { width: 20rpx; height: 20rpx; border-radius: 50%; flex-shrink: 0; margin-top: 6rpx; }
.event-demand { background: #FFB300; }
.event-join { background: #4CAF50; }
.event-done { background: #42A5F5; }
.event-machinery { background: #AB47BC; }
.event-text { display: block; font-size: 28rpx; color: #333; }
.event-time { display: block; font-size: 22rpx; color: #bbb; margin-top: 4rpx; }
</style>
