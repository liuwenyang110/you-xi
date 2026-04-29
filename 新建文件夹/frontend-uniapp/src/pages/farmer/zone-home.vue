<template>
  <view class="zone-home">
    <!-- 导航栏 -->
    <view class="nav-bar" :style="{ paddingTop: statusBarHeight + 'px' }">
      <view class="nav-content">
        <view class="zone-badge" @click="gotoZoneSelect">
          <text class="zone-badge-name">{{ zoneName }}</text>
          <text class="zone-badge-arrow">›</text>
        </view>
        <view class="nav-actions">
          <view class="nav-bell" @click="gotoNotifications">
            <text class="bell-icon">消息</text>
            <view class="bell-dot" v-if="hasUnread"></view>
          </view>
        </view>
      </view>
    </view>

    <!-- 农机资源概览 -->
    <view class="machinery-card">
      <view class="card-header">
        <text class="card-title">本片区农机</text>
        <text class="card-more" @click="gotoOperatorList">详情 ›</text>
      </view>
      <view class="stats-row" v-if="machineryStats.length">
        <view
          v-for="stat in machineryStats"
          :key="stat.categoryName"
          class="stat-item"
        >
          <text class="stat-count">{{ stat.cnt }}</text>
          <text class="stat-label">{{ stat.categoryName }}</text>
        </view>
      </view>
      <view class="stats-empty" v-else>
        <text class="stats-empty-text">暂无农机手入驻</text>
      </view>
      <view class="operator-summary">
        <text class="summary-text">{{ operatorCount }} 名机手 · {{ machineryCount }} 台农机</text>
      </view>
    </view>

    <!-- 快捷操作 -->
    <view class="action-shortcuts">
      <view class="shortcut-item" @click="gotoDemandPublish">
        <view class="shortcut-icon bg-green">发</view>
        <text class="shortcut-text">发布需求</text>
      </view>
      <view class="shortcut-item" @click="gotoOperatorList">
        <view class="shortcut-icon bg-blue">找</view>
        <text class="shortcut-text">找农机</text>
      </view>
      <view class="shortcut-item" @click="gotoDemandList">
        <view class="shortcut-icon bg-amber">单</view>
        <text class="shortcut-text">我的需求</text>
      </view>
      <view class="shortcut-item" @click="gotoTeahouse">
        <view class="shortcut-icon bg-tea">聊</view>
        <text class="shortcut-text">交流大厅</text>
      </view>
    </view>

    <!-- 需求列表 -->
    <view class="demands-section">
      <view class="section-header">
        <text class="section-title">片区需求</text>
        <text class="section-more" @click="gotoDemandList">全部 ›</text>
      </view>

      <view v-if="demands.length === 0 && !loadingDemands" class="no-demands">
        <text class="no-demands-text">暂无需求</text>
        <text class="no-demands-sub">发布第一条地块需求</text>
        <button class="publish-btn" @click="gotoDemandPublish">发布需求</button>
      </view>

      <view
        v-for="item in demands"
        :key="item.id"
        class="demand-card"
      >
        <view class="demand-header">
          <view class="demand-work-badge">
            <text class="demand-work-name">{{ getWorkTypeName(item.workTypeId) }}</text>
          </view>
          <text class="demand-date">{{ formatDate(item.expectDateStart) }}</text>
        </view>
        <view class="demand-info">
          <view class="demand-info-row" v-if="item.areaDesc">
            <text class="info-label">面积</text>
            <text class="info-value">{{ item.areaDesc }}</text>
          </view>
          <view class="demand-info-row" v-if="item.locationDesc">
            <text class="info-label">位置</text>
            <text class="info-value">{{ item.locationDesc }}</text>
          </view>
          <view class="demand-info-row" v-if="item.plotNotes">
            <text class="info-label">备注</text>
            <text class="info-value notes">{{ item.plotNotes }}</text>
          </view>
        </view>
        <view class="demand-footer">
          <text class="demand-time">{{ formatRelativeTime(item.publishedAt) }}</text>
          <text class="demand-status" :class="statusClass(item.status)">{{ statusText(item.status) }}</text>
        </view>
      </view>

      <view class="load-more" v-if="hasMore" @click="loadMore">
        <text>加载更多</text>
      </view>
    </view>
  </view>
</template>

<script>
import { useV3UserStore } from '@/stores/v3/userStore'
import { useV3ZoneStore } from '@/stores/v3/zoneStore'
import { getZoneDemands } from '@/api/v3/demand'

export default {
  name: 'FarmerZoneHome',
  data() {
    return {
      statusBarHeight: 0,
      demands: [],
      page: 1,
      hasMore: false,
      total: 0,
      loadingDemands: false,
      hasUnread: false,
    }
  },
  computed: {
    zoneName() {
      return useV3ZoneStore().zoneName || '未选择片区'
    },
    operatorCount() {
      return useV3ZoneStore().operatorCount
    },
    machineryCount() {
      return useV3ZoneStore().machineryCount
    },
    machineryStats() {
      return useV3ZoneStore().machineryStats || []
    },
    zoneId() {
      return useV3UserStore().zoneId
    },

  },
  async onLoad() {
    const sysInfo = uni.getSystemInfoSync()
    this.statusBarHeight = sysInfo.statusBarHeight || 0
    await this.init()
  },
  async onPullDownRefresh() {
    this.page = 1
    this.demands = []
    await this.loadDemands()
    uni.stopPullDownRefresh()
  },
  methods: {
    async init() {
      const zoneStore = useV3ZoneStore()
      const userStore = useV3UserStore()
      if (!userStore.zoneId) {
        uni.redirectTo({ url: '/pages/common/zone-select' })
        return
      }
      await Promise.all([
        zoneStore.loadZone(userStore.zoneId),
        zoneStore.loadDicts(),
        this.loadDemands(),
        this.checkUnread(),
      ])
    },
    async loadDemands() {
      if (this.loadingDemands) return
      this.loadingDemands = true
      try {
        const zoneId = useV3UserStore().zoneId
        const res = await getZoneDemands(zoneId, this.page, 10)
        if (res.code === 0) {
          const list = res.data.list || []
          this.demands = this.page === 1 ? list : [...this.demands, ...list]
          this.total = res.data.total
          this.hasMore = this.demands.length < this.total
        }
      } catch (e) {
        console.error('加载需求失败', e)
      } finally {
        this.loadingDemands = false
      }
    },
    async loadMore() {
      this.page++
      await this.loadDemands()
    },
    getWorkTypeName(workTypeId) {
      const store = useV3ZoneStore()
      const wt = store.dicts.workTypes.find(w => w.id === workTypeId)
      return wt?.name || '农事作业'
    },
    formatDate(dateStr) {
      if (!dateStr) return '时间待定'
      const d = new Date(dateStr)
      return `${d.getMonth()+1}月${d.getDate()}日`
    },
    formatRelativeTime(dateStr) {
      if (!dateStr) return ''
      const diff = Date.now() - new Date(dateStr).getTime()
      const mins = Math.floor(diff / 60000)
      if (mins < 1) return '刚刚'
      if (mins < 60) return `${mins}分钟前`
      const hours = Math.floor(mins / 60)
      if (hours < 24) return `${hours}小时前`
      const days = Math.floor(hours / 24)
      return `${days}天前`
    },
    statusText(s) {
      return { PUBLISHED: '待联系', CONTACTED: '已联系', COMPLETED: '已完成', CANCELLED: '已取消' }[s] || s
    },
    statusClass(s) {
      return { PUBLISHED: 'status-published', CONTACTED: 'status-contacted', COMPLETED: 'status-done', CANCELLED: 'status-cancelled' }[s] || ''
    },
    gotoDemandPublish() { uni.navigateTo({ url: '/pages/farmer/demand-publish' }) },
    gotoDemandList() { uni.navigateTo({ url: '/pages/farmer/demand-list' }) },
    gotoOperatorList() { uni.navigateTo({ url: '/pages/farmer/operator-list' }) },
    gotoZoneSelect() { uni.navigateTo({ url: '/pages/common/zone-select' }) },

    gotoTeahouse() { uni.navigateTo({ url: '/pages/common/teahouse' }) },
    gotoNotifications() { uni.navigateTo({ url: '/pages/common/notifications' }) },
    async checkUnread() {
      try {
        const { getNotifications } = await import('@/api/v3/notification')
        const res = await getNotifications(1, 1)
        if (res.code === 0) { this.hasUnread = (res.data.unreadCount || 0) > 0 }
      } catch (e) { /* 静默 */ }
    },
  }
}
</script>

<style scoped>
.zone-home { background: var(--page-bg); min-height: 100vh; min-height: 100dvh; padding-bottom: 20rpx; }

/* 导航栏 */
.nav-bar { background: var(--gradient-primary); padding-bottom: 28rpx; }
.nav-content {
  padding: 14rpx 32rpx 0;
  display: flex;
  align-items: center;
  justify-content: space-between;
}
.zone-badge {
  display: inline-flex;
  align-items: center;
  background: rgba(255,255,255,0.15);
  backdrop-filter: blur(8px);
  border-radius: var(--radius-full);
  padding: 10rpx 24rpx;
  transition: background var(--duration-200);
}
.zone-badge-name { font-size: 28rpx; color: #fff; font-weight: 600; max-width: 280rpx; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.zone-badge-arrow { font-size: 30rpx; color: rgba(255,255,255,0.7); margin-left: 8rpx; }

/* 通知 */
.nav-actions { display: flex; align-items: center; }
.nav-bell {
  position: relative; padding: 8rpx 16rpx;
  background: rgba(255,255,255,0.12);
  border-radius: var(--radius-full);
}
.bell-icon { font-size: 24rpx; color: rgba(255,255,255,0.9); font-weight: 500; }
.bell-dot {
  position: absolute; top: 4rpx; right: 8rpx;
  width: 12rpx; height: 12rpx; border-radius: 50%;
  background: #ef4444; border: 2rpx solid rgba(255,255,255,0.8);
}

/* 农机概览 */
.machinery-card {
  background: var(--surface-deep);
  margin: -12rpx 24rpx 0;
  border-radius: var(--radius-2xl);
  padding: 28rpx;
  box-shadow: var(--shadow-md);
  position: relative;
  z-index: 2;
}
.card-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20rpx; }
.card-title { font-size: 30rpx; font-weight: 700; color: var(--text-primary); }
.card-more { font-size: 24rpx; color: var(--primary-color); font-weight: 500; }

.stats-row { display: flex; gap: 12rpx; margin-bottom: 16rpx; overflow-x: auto; }
.stat-item {
  display: flex; flex-direction: column; align-items: center;
  min-width: 110rpx; padding: 16rpx;
  background: var(--primary-faint); border-radius: var(--radius-xl);
  flex-shrink: 0;
}
.stat-count { font-size: 36rpx; font-weight: 800; color: var(--primary-strong); }
.stat-label { font-size: 22rpx; color: var(--text-muted); margin-top: 4rpx; }

.stats-empty { padding: 20rpx 0; }
.stats-empty-text { font-size: 28rpx; color: var(--text-soft); }
.operator-summary { border-top: 1rpx solid var(--border-light); padding-top: 14rpx; margin-top: 4rpx; }
.summary-text { font-size: 22rpx; color: var(--text-soft); }

/* 快捷入口 */
.action-shortcuts {
  display: flex;
  padding: 28rpx 32rpx 16rpx;
  justify-content: space-around;
}
.shortcut-item {
  display: flex; flex-direction: column; align-items: center;
  transition: transform var(--duration-200) var(--ease-out);
}
.shortcut-item:active { transform: scale(0.92); }
.shortcut-icon {
  width: 96rpx; height: 96rpx; border-radius: 28rpx;
  display: flex; align-items: center; justify-content: center;
  font-size: 32rpx; font-weight: 700; color: #fff;
  margin-bottom: 12rpx;
  box-shadow: 0 6rpx 16rpx rgba(0,0,0,0.12);
}
.bg-green { background: linear-gradient(145deg, #34d399, #059669); }
.bg-blue { background: linear-gradient(145deg, #60a5fa, #2563eb); }
.bg-amber { background: linear-gradient(145deg, #fbbf24, #d97706); }
.bg-tea { background: linear-gradient(145deg, #a78bfa, #7c3aed); }
.shortcut-text { font-size: 24rpx; color: var(--text-muted); font-weight: 500; }

/* 需求列表 */
.demands-section { padding: 8rpx 24rpx 24rpx; }
.section-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20rpx; }
.section-title { font-size: 32rpx; font-weight: 700; color: var(--text-primary); }
.section-more { font-size: 24rpx; color: var(--primary-color); font-weight: 500; }

.no-demands {
  display: flex; flex-direction: column; align-items: center;
  padding: 64rpx 40rpx;
  background: var(--surface-deep);
  border-radius: var(--radius-2xl);
  box-shadow: var(--shadow-base);
}
.no-demands-text { font-size: 30rpx; color: var(--text-muted); margin-bottom: 8rpx; font-weight: 500; }
.no-demands-sub { font-size: 24rpx; color: var(--text-soft); margin-bottom: 36rpx; }
.publish-btn {
  background: var(--gradient-primary);
  color: #fff; border: none;
  border-radius: var(--radius-full);
  font-size: 28rpx; font-weight: 600;
  padding: 18rpx 56rpx;
  box-shadow: var(--shadow-colored);
  transition: transform var(--duration-200);
}
.publish-btn:active { transform: scale(0.96); }

.demand-card {
  background: var(--surface-deep);
  border-radius: var(--radius-xl);
  padding: 24rpx 28rpx;
  margin-bottom: 16rpx;
  box-shadow: var(--shadow-base);
  transition: box-shadow var(--duration-200);
}
.demand-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16rpx; }
.demand-work-badge {
  background: var(--color-success-bg);
  border-radius: var(--radius-full); padding: 6rpx 20rpx;
}
.demand-work-name { font-size: 24rpx; color: var(--primary-strong); font-weight: 600; }
.demand-date { font-size: 24rpx; color: var(--text-soft); }

.demand-info { margin-bottom: 12rpx; }
.demand-info-row { display: flex; margin-bottom: 8rpx; }
.info-label { font-size: 24rpx; color: var(--text-soft); width: 80rpx; flex-shrink: 0; }
.info-value { font-size: 24rpx; color: var(--text-regular); flex: 1; }
.info-value.notes { color: var(--color-error); }

.demand-footer { display: flex; justify-content: space-between; align-items: center; border-top: 1rpx solid var(--border-light); padding-top: 12rpx; }
.demand-time { font-size: 22rpx; color: var(--text-soft); }
.status-published { font-size: 22rpx; color: var(--color-success); font-weight: 500; }
.status-contacted { font-size: 22rpx; color: var(--color-info); font-weight: 500; }
.status-done { font-size: 22rpx; color: var(--text-soft); }
.status-cancelled { font-size: 22rpx; color: var(--color-error); }

.load-more { text-align: center; padding: 24rpx; font-size: 26rpx; color: var(--text-soft); }
</style>

