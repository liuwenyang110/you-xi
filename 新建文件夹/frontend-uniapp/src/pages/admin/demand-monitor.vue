<template>
  <view class="demand-monitor">
    <view class="page-header">
      <text class="page-title">公益对接监控</text>
      <text class="page-sub">实时掘握片区农事求助与服务者对接点情况</text>
    </view>

    <!-- 状态统计栏 -->
    <view class="stats-strip">
      <view class="stat-pill" :class="{ active: statusFilter === null }" @click="setFilter(null)">
        <text class="pill-num">{{ totalCount }}</text>
        <text class="pill-label">全部</text>
      </view>
      <view class="stat-pill pending" :class="{ active: statusFilter === 'PENDING' }" @click="setFilter('PENDING')">
        <text class="pill-num">{{ counts.PENDING || 0 }}</text>
        <text class="pill-label">待联系</text>
      </view>
      <view class="stat-pill contacted" :class="{ active: statusFilter === 'CONTACTED' }" @click="setFilter('CONTACTED')">
        <text class="pill-num">{{ counts.CONTACTED || 0 }}</text>
        <text class="pill-label">已联系</text>
      </view>
      <view class="stat-pill done" :class="{ active: statusFilter === 'DONE' }" @click="setFilter('DONE')">
        <text class="pill-num">{{ counts.DONE || 0 }}</text>
        <text class="pill-label">已完成</text>
      </view>
    </view>

    <!-- 需求列表 -->
    <view class="demand-list">
      <view
        v-for="item in filteredDemands"
        :key="item.id"
        class="demand-row"
      >
        <view class="demand-row-left">
          <view class="d-status-dot" :class="'dot-' + item.status"></view>
          <view class="d-info">
            <text class="d-type">{{ item.workTypeName || '农事作业' }}</text>
            <text class="d-meta">
              片区#{{ item.zoneId }} ·
              {{ item.isGroup ? '合集' : '个人' }} ·
              {{ formatRelTime(item.publishedAt) }}
            </text>
          </view>
        </view>
        <view class="d-status-tag" :class="'tag-' + item.status">
          {{ statusName(item.status) }}
        </view>
      </view>

      <view class="empty-state" v-if="filteredDemands.length === 0 && !loading">
        <text class="empty-icon"></text>
        <text class="empty-text">暂无符合条件的需求</text>
      </view>
    </view>

    <!-- 加载更多 -->
    <view class="load-more" v-if="hasMore" @click="loadMore">加载更多</view>
  </view>
</template>

<script>
import request from '@/api/v3/request'

export default {
  name: 'DemandMonitor',
  data() {
    return {
      demands: [],
      loading: false,
      page: 1,
      pageSize: 20,
      hasMore: false,
      statusFilter: null,
      counts: {},
    }
  },
  computed: {
    filteredDemands() {
      if (!this.statusFilter) return this.demands
      return this.demands.filter(d => d.status === this.statusFilter)
    },
    totalCount() { return this.demands.length }
  },
  async onLoad() {
    await this.loadDemands()
  },
  async onPullDownRefresh() {
    this.page = 1
    this.demands = []
    await this.loadDemands()
    uni.stopPullDownRefresh()
  },
  methods: {
    async loadDemands() {
      this.loading = true
      try {
        const res = await request.get('/api/v3/admin/demands', {
          params: { page: this.page, size: this.pageSize }
        })
        if (res.code === 0) {
          const list = res.data?.list || []
          this.demands = this.page === 1 ? list : [...this.demands, ...list]
          this.hasMore = (this.page * this.pageSize) < (res.data?.total || 0)
          // 计算各状态计数
          this.counts = {}
          this.demands.forEach(d => {
            this.counts[d.status] = (this.counts[d.status] || 0) + 1
          })
        }
      } finally {
        this.loading = false
      }
    },
    async loadMore() {
      this.page++
      await this.loadDemands()
    },
    setFilter(status) { this.statusFilter = status },
    statusName(s) {
      return { PENDING: '待联系', CONTACTED: '已联系', DONE: '已完成', CANCELLED: '已取消' }[s] || s
    },
    formatRelTime(dt) {
      if (!dt) return ''
      const diff = Date.now() - new Date(dt).getTime()
      const mins = Math.floor(diff / 60000)
      if (mins < 60) return `${mins}分钟前`
      const hours = Math.floor(mins / 60)
      if (hours < 24) return `${hours}小时前`
      return `${Math.floor(hours / 24)}天前`
    }
  }
}
</script>

<style>
.demand-monitor { background: #F0F7F3; min-height: 100vh; padding-bottom: 60rpx; }
.page-header { background: linear-gradient(135deg, #2D7A4F, #5BA88A); padding: 80rpx 40rpx 40rpx; }
.page-title { display: block; font-size: 42rpx; font-weight: 700; color: #fff; }
.page-sub { display: block; font-size: 26rpx; color: rgba(255,255,255,0.8); margin-top: 6rpx; }

.stats-strip { display: flex; padding: 16rpx 24rpx; gap: 12rpx; background: #fff; box-shadow: 0 2rpx 10rpx rgba(0,0,0,0.06); }
.stat-pill { flex: 1; text-align: center; border-radius: 14rpx; padding: 16rpx 8rpx; background: #F0F7F3; }
.stat-pill.active { transform: none; }
.stat-pill.pending.active { background: #FFF8E1; }
.stat-pill.contacted.active { background: #E3F2FD; }
.stat-pill.done.active { background: #EAF5EE; }
.pill-num { display: block; font-size: 36rpx; font-weight: 700; color: #2D7A4F; }
.pill-label { display: block; font-size: 22rpx; color: #888; margin-top: 4rpx; }

.demand-list { padding: 16rpx 24rpx 0; }
.demand-row { background: #fff; border-radius: 14rpx; padding: 20rpx 24rpx; margin-bottom: 14rpx; display: flex; align-items: center; justify-content: space-between; box-shadow: 0 2rpx 8rpx rgba(0,0,0,0.05); }
.demand-row-left { display: flex; align-items: center; gap: 16rpx; }
.d-status-dot { width: 16rpx; height: 16rpx; border-radius: 50%; flex-shrink: 0; }
.dot-PENDING { background: #FFB300; }
.dot-CONTACTED { background: #42A5F5; }
.dot-DONE { background: #4CAF50; }
.dot-CANCELLED { background: #bbb; }
.d-type { display: block; font-size: 28rpx; font-weight: 700; color: #1E3A12; }
.d-meta { display: block; font-size: 22rpx; color: #999; margin-top: 4rpx; }
.d-status-tag { font-size: 24rpx; border-radius: 20rpx; padding: 6rpx 16rpx; font-weight: 600; }
.tag-PENDING { background: #FFF8E1; color: #F57F17; }
.tag-CONTACTED { background: #E3F2FD; color: #1565C0; }
.tag-DONE { background: #E8F5E9; color: #2E7D32; }
.tag-CANCELLED { background: #FAFAFA; color: #999; }

.empty-state { display: flex; flex-direction: column; align-items: center; padding: 80rpx; }
.empty-icon { font-size: 80rpx; margin-bottom: 20rpx; }
.empty-text { font-size: 30rpx; color: #999; }
.load-more { text-align: center; padding: 32rpx; font-size: 28rpx; color: #4CAF50; font-weight: 600; }
</style>
