<template>
  <view class="operator-workbench">
    <!-- 自定义导航 -->
    <view class="nav-bar" :style="{ paddingTop: statusBarHeight + 'px' }">
      <view class="nav-content">
        <view class="zone-tag">
          <text class="zone-tag-text">{{ zoneName }}</text>
        </view>
        <text class="nav-title">农机手工作台</text>
        <view class="nav-actions">
          <view class="nav-bell" @click="gotoNotifications">
            <text class="bell-icon"></text>
            <view class="bell-dot" v-if="hasUnread"></view>
          </view>
        </view>
      </view>
    </view>

    <!-- 视图切换 Tab -->
    <view class="view-tabs">
      <view class="tab-item" :class="{ active: viewMode === 'radar' }" @click="viewMode = 'radar'">
        <text class="tab-icon">📡</text>
        <text class="tab-text">镇域雷达</text>
      </view>
      <view class="tab-item" :class="{ active: viewMode === 'list' }" @click="viewMode = 'list'">
        <text class="tab-icon"></text>
        <text class="tab-text">需求列表</text>
      </view>
      <view class="tab-item" :class="{ active: viewMode === 'machine' }" @click="viewMode = 'machine'">
        <text class="tab-icon">🔧</text>
        <text class="tab-text">我的农机</text>
      </view>
    </view>

    <!-- ========== 雷达视图 ========== -->
    <view v-if="viewMode === 'radar'" class="radar-view">
      <view class="radar-header">
        <text class="radar-title">🌍 镇域需求雷达</text>
        <text class="radar-desc">气泡越大 = 该村需求越旺</text>
      </view>

      <view v-if="radarLoading" class="radar-loading">
        <text>扫描中...</text>
      </view>

      <view v-else-if="radarBubbles.length === 0" class="radar-empty">
        <text class="radar-empty-icon">📡</text>
        <text class="radar-empty-text">本镇周边暂无活跃需求</text>
        <text class="radar-empty-sub">等待农户发布需求后这里会亮起来</text>
      </view>

      <view v-else class="bubble-grid">
        <view
          v-for="bubble in radarBubbles"
          :key="bubble.zoneId"
          class="bubble-card"
          :class="{ 'bubble-hot': bubble.demandCount >= 5, 'bubble-current': bubble.zoneId == zoneId }"
          @click="handleBubbleClick(bubble)"
        >
          <view class="bubble-circle" :style="{ width: getBubbleSize(bubble) + 'rpx', height: getBubbleSize(bubble) + 'rpx' }">
            <text class="bubble-count">{{ bubble.demandCount }}</text>
          </view>
          <text class="bubble-name">{{ bubble.zoneName }}</text>
          <text class="bubble-label">{{ bubble.demandCount }}条需求</text>
          <view class="bubble-current-tag" v-if="bubble.zoneId == zoneId">
            <text>📍当前</text>
          </view>
        </view>
      </view>

      <!-- 顺路推荐卡片 -->
      <view class="route-suggest" v-if="routeSuggestions.length > 0">
        <view class="route-header">
          <text class="route-title">🛤️ 顺路活儿推荐</text>
          <text class="route-desc">这些村子离您很近，顺路干完不白跑</text>
        </view>
        <view
          v-for="s in routeSuggestions"
          :key="s.zoneId"
          class="route-card"
          @click="handleBubbleClick(s)"
        >
          <view class="route-left">
            <text class="route-village">{{ s.zoneName }}</text>
            <text class="route-count">{{ s.demandCount }}条待接</text>
          </view>
          <view class="route-btn">
            <text>去看看 ›</text>
          </view>
        </view>
      </view>

      <!-- 茶馆快捷入口 -->
      <view class="teahouse-entry" @click="gotoTeahouse">
        <text class="teahouse-icon"></text>
        <view class="teahouse-info">
          <text class="teahouse-name">进入交流大厅</text>
          <text class="teahouse-desc">跟农户聊细节、问价格、对接散活</text>
        </view>
        <text class="teahouse-arrow">›</text>
      </view>
    </view>

    <!-- ========== 列表视图（原有逻辑） ========== -->
    <view v-if="viewMode === 'list'" class="list-view">
      <view class="demands-section">
        <view class="section-header">
          <text class="section-title">片区待接农活</text>
          <text class="section-count">共 {{ total }} 条</text>
        </view>

        <scroll-view class="filter-scroll" scroll-x>
          <view class="filter-chips">
            <view
              class="filter-chip" :class="{ active: filterWorkTypeId === null }"
              @click="filterWorkTypeId = null; loadDemands()"
            >全部</view>
            <view
              v-for="wt in workTypes" :key="wt.id"
              class="filter-chip" :class="{ active: filterWorkTypeId === wt.id }"
              @click="filterWorkTypeId = wt.id; loadDemands()"
            >{{ wt.name }}</view>
          </view>
        </scroll-view>

        <view v-if="demands.length === 0 && !loadingDemands" class="empty-tip">
          <text>本片区暂无未接农活需求</text>
        </view>

        <view v-for="item in demands" :key="item.id" class="demand-card">
          <view class="demand-tag-row">
            <view class="demand-work-tag">{{ getWorkTypeName(item.workTypeId) }}</view>
            <text class="demand-time">{{ formatRelativeTime(item.publishedAt) }}</text>
          </view>
          <view class="demand-info-row" v-if="item.areaDesc">
            <text class="info-label">面积</text>
            <text class="info-val">{{ item.areaDesc }}</text>
          </view>
          <view class="demand-info-row" v-if="item.locationDesc">
            <text class="info-label">位置</text>
            <text class="info-val">{{ item.locationDesc }}</text>
          </view>
          <view class="demand-info-row" v-if="item.expectDateStart">
            <text class="info-label">时间</text>
            <text class="info-val">{{ item.expectDateStart }}</text>
          </view>
          <view class="demand-notes" v-if="item.plotNotes">
            <text class="notes-icon">⚠️</text>
            <text class="notes-text">{{ item.plotNotes }}</text>
          </view>
          <view class="demand-footer">
            <text class="contact-tip">联系农户：请直接拨打虚拟号或通过平台联系</text>
          </view>
        </view>

        <view class="load-more" v-if="hasMore" @click="loadMore">加载更多</view>
      </view>
    </view>

    <!-- ========== 我的农机视图 ========== -->
    <view v-if="viewMode === 'machine'" class="machine-view">
      <view class="my-machine-card">
        <view class="card-header">
          <text class="card-title">🔧 我的农机</text>
          <text class="card-action" @click="gotoMachineryManage">管理 ›</text>
        </view>
        <view v-if="myMachineryList.length">
          <view v-for="m in myMachineryList" :key="m.id" class="machine-item">
            <text class="machine-icon"></text>
            <view class="machine-info">
              <text class="machine-name">{{ getMachineTypeName(m.machineTypeId) }}</text>
              <text class="machine-brand">{{ m.brand }} {{ m.modelNo }}</text>
            </view>
            <view class="machine-badge" v-if="m.isCrossRegion">
              <text>跨区可接</text>
            </view>
          </view>
        </view>
        <view class="no-machine" v-else>
          <text class="no-machine-text">还没有登记农机</text>
          <button class="add-machine-btn" @click="gotoMachineryManage">登记我的农机</button>
        </view>
      </view>
    </view>
  </view>
</template>

<script>
import { getZoneDemands } from '@/api/v3/demand'
import { myMachinery } from '@/api/v3/machinery'
import { getRadarData } from '@/api/v3/notification'
import { useV3UserStore } from '@/stores/v3/userStore'
import { useV3ZoneStore } from '@/stores/v3/zoneStore'

export default {
  name: 'OperatorZoneHome',
  data() {
    return {
      statusBarHeight: 0,
      viewMode: 'radar',  // radar / list / machine
      myMachineryList: [],
      demands: [],
      page: 1,
      hasMore: false,
      total: 0,
      loadingDemands: false,
      filterWorkTypeId: null,
      radarBubbles: [],
      radarLoading: false,
      routeSuggestions: [],
      hasUnread: false,
    }
  },
  computed: {
    zoneName() { return useV3ZoneStore().zoneName },
    zoneId() { return useV3UserStore().zoneId },
    townshipCode() { return useV3ZoneStore().townshipCode },
    workTypes() { return useV3ZoneStore().dicts.workTypes || [] },
  },
  async onLoad() {
    const sysInfo = uni.getSystemInfoSync()
    this.statusBarHeight = sysInfo.statusBarHeight || 0
    const userStore = useV3UserStore()
    if (!userStore.zoneId) {
      uni.redirectTo({ url: '/pages/common/zone-select' })
      return
    }
    const zoneStore = useV3ZoneStore()
    await Promise.all([
      zoneStore.loadZone(userStore.zoneId),
      zoneStore.loadDicts(),
      this.loadMyMachinery(),
      this.loadDemands(),
      this.checkUnread(),
    ])
    // 加载完 zone 后再加载雷达（需要 townshipCode）
    await this.loadRadar()
  },
  async onPullDownRefresh() {
    this.page = 1
    this.demands = []
    await Promise.all([this.loadMyMachinery(), this.loadDemands(), this.loadRadar(), this.checkUnread()])
    uni.stopPullDownRefresh()
  },
  methods: {
    async loadMyMachinery() {
      try {
        const res = await myMachinery()
        if (res.code === 0) this.myMachineryList = res.data
      } catch (e) {}
    },
    async loadDemands() {
      if (!this.zoneId) return
      this.loadingDemands = true
      try {
        const res = await getZoneDemands(this.zoneId, this.page, 10)
        if (res.code === 0) {
          const list = res.data.list || []
          const filtered = this.filterWorkTypeId
            ? list.filter(d => d.workTypeId === this.filterWorkTypeId)
            : list
          this.demands = this.page === 1 ? filtered : [...this.demands, ...filtered]
          this.total = res.data.total
          this.hasMore = this.demands.length < this.total
        }
      } catch (e) {} finally {
        this.loadingDemands = false
      }
    },
    async loadRadar() {
      const tc = this.townshipCode || useV3ZoneStore().townshipCode
      if (!tc) return
      this.radarLoading = true
      try {
        const res = await getRadarData(tc)
        if (res.code === 0) {
          this.radarBubbles = res.data.villages || []
          // 顺路推荐 = 非当前村的气泡，按需求数降序取前3
          this.routeSuggestions = this.radarBubbles
            .filter(b => b.zoneId != this.zoneId && b.demandCount > 0)
            .sort((a, b) => b.demandCount - a.demandCount)
            .slice(0, 3)
        }
      } catch (e) {
        console.error('雷达数据加载失败', e)
      } finally {
        this.radarLoading = false
      }
    },
    async loadMore() {
      this.page++
      await this.loadDemands()
    },
    async checkUnread() {
      try {
        const { getNotifications } = await import('@/api/v3/notification')
        const res = await getNotifications(1, 1)
        if (res.code === 0) { this.hasUnread = (res.data.unreadCount || 0) > 0 }
      } catch (e) {}
    },
    getBubbleSize(bubble) {
      const base = 120
      const scale = Math.min(bubble.demandCount * 20, 100)
      return base + scale
    },
    handleBubbleClick(bubble) {
      // 点击气泡切换到列表视图并跳转到该村的需求浏览
      uni.navigateTo({ url: `/pages/operator/demand-browse?zoneId=${bubble.zoneId}&zoneName=${bubble.zoneName}` })
    },
    getMachineTypeName(id) {
      return useV3ZoneStore().machineTypeName(id)
    },
    getWorkTypeName(id) {
      const wt = useV3ZoneStore().dicts.workTypes.find(w => w.id === id)
      return wt?.name || '农事作业'
    },
    formatRelativeTime(dateStr) {
      if (!dateStr) return ''
      const diff = Date.now() - new Date(dateStr).getTime()
      const mins = Math.floor(diff / 60000)
      if (mins < 1) return '刚刚'
      if (mins < 60) return `${mins}分钟前`
      const hours = Math.floor(mins / 60)
      if (hours < 24) return `${hours}小时前`
      return `${Math.floor(hours / 24)}天前`
    },
    gotoMachineryManage() { uni.navigateTo({ url: '/pages/operator/machinery-manage' }) },
    gotoTeahouse() { uni.navigateTo({ url: '/pages/common/teahouse' }) },
    gotoNotifications() { uni.navigateTo({ url: '/pages/common/notifications' }) },
  }
}
</script>

<style scoped>
.operator-workbench { background: var(--page-bg); min-height: 100vh; min-height: 100dvh; }
.nav-bar { background: var(--gradient-primary); }
.nav-content { padding: 12rpx 32rpx 24rpx; display: flex; align-items: center; justify-content: space-between; }
.zone-tag { background: rgba(255,255,255,0.18); border-radius: var(--radius-full); padding: 8rpx 20rpx; }
.zone-tag-text { font-size: var(--text-sm); color: #fff; }
.nav-title { font-size: var(--text-lg); font-weight: var(--font-bold); color: #fff; }
.nav-actions { display: flex; align-items: center; }
.nav-bell { position: relative; padding: 8rpx; }
.bell-icon { font-size: 40rpx; }
.bell-dot {
  position: absolute; top: 4rpx; right: 4rpx;
  width: 16rpx; height: 16rpx; border-radius: 50%;
  background: var(--color-error); border: 2rpx solid #fff;
}

/* 视图切换 Tab */
.view-tabs {
  display: flex; background: var(--surface-deep); padding: 8rpx 24rpx;
  border-bottom: 1rpx solid var(--border-light);
}
.tab-item {
  flex: 1; display: flex; align-items: center; justify-content: center; gap: 8rpx;
  padding: 18rpx 0; border-radius: var(--radius-md); transition: all var(--duration-200);
}
.tab-item.active { background: var(--color-success-bg); }
.tab-icon { font-size: 28rpx; }
.tab-text { font-size: var(--text-sm); color: var(--text-muted); }
.tab-item.active .tab-text { color: var(--primary-strong); font-weight: var(--font-bold); }

/* ========== 雷达视图 ========== */
.radar-view { padding: 20rpx 24rpx; }
.radar-header { margin-bottom: 20rpx; }
.radar-title { display: block; font-size: 34rpx; font-weight: var(--font-bold); color: var(--text-primary); }
.radar-desc { display: block; font-size: var(--text-xs); color: var(--text-soft); margin-top: 4rpx; }

.radar-loading { text-align: center; padding: 60rpx; font-size: var(--text-base); color: var(--text-soft); }
.radar-empty { display: flex; flex-direction: column; align-items: center; padding: 60rpx; }
.radar-empty-icon { font-size: 80rpx; margin-bottom: 16rpx; }
.radar-empty-text { font-size: var(--text-base); color: var(--text-muted); }
.radar-empty-sub { font-size: var(--text-sm); color: var(--text-soft); margin-top: 8rpx; }

/* 气泡网格 */
.bubble-grid {
  display: flex; flex-wrap: wrap; gap: 20rpx; justify-content: center;
  padding: 20rpx 0;
}
.bubble-card {
  display: flex; flex-direction: column; align-items: center;
  background: var(--surface-deep); border-radius: var(--radius-xl); padding: 24rpx 20rpx;
  min-width: 200rpx; box-shadow: var(--shadow-sm);
  position: relative; transition: transform var(--duration-200);
}
.bubble-card:active { transform: scale(0.96); }
.bubble-card.bubble-hot { border: 2rpx solid var(--color-error); }
.bubble-card.bubble-current { border: 2rpx solid var(--color-success); }

.bubble-circle {
  border-radius: 50%; display: flex; align-items: center; justify-content: center;
  background: linear-gradient(135deg, rgba(16,185,129,0.2), rgba(16,185,129,0.6));
  margin-bottom: 12rpx; transition: all var(--duration-300);
}
.bubble-card.bubble-hot .bubble-circle {
  background: linear-gradient(135deg, rgba(239,68,68,0.2), rgba(239,68,68,0.6));
}
.bubble-count { font-size: 40rpx; font-weight: 800; color: #fff; }
.bubble-name { font-size: var(--text-base); font-weight: var(--font-bold); color: var(--text-primary); }
.bubble-label { font-size: var(--text-xs); color: var(--text-soft); margin-top: 4rpx; }
.bubble-current-tag {
  position: absolute; top: 8rpx; right: 8rpx;
  font-size: 20rpx; color: var(--color-success);
}

/* 顺路推荐 */
.route-suggest { margin-top: 28rpx; }
.route-header { margin-bottom: 16rpx; }
.route-title { display: block; font-size: var(--text-base); font-weight: var(--font-bold); color: var(--text-primary); }
.route-desc { display: block; font-size: var(--text-xs); color: var(--text-soft); margin-top: 4rpx; }
.route-card {
  display: flex; justify-content: space-between; align-items: center;
  background: var(--surface-deep); border-radius: var(--radius-lg); padding: 24rpx;
  margin-bottom: 12rpx; box-shadow: var(--shadow-sm);
  border-left: 6rpx solid var(--color-warning);
}
.route-village { display: block; font-size: var(--text-base); font-weight: var(--font-bold); color: var(--text-primary); }
.route-count { display: block; font-size: var(--text-xs); color: var(--color-warning); margin-top: 4rpx; }
.route-btn {
  background: linear-gradient(135deg, var(--color-warning), hsl(30, 100%, 45%));
  color: #fff; padding: 12rpx 28rpx; border-radius: var(--radius-full);
  font-size: var(--text-sm); font-weight: var(--font-semibold);
}

/* 茶馆入口 */
.teahouse-entry {
  display: flex; align-items: center; gap: 16rpx;
  background: var(--secondary-color);
  border-radius: var(--radius-xl); padding: 28rpx; margin-top: 24rpx;
}
.teahouse-icon { font-size: 48rpx; flex-shrink: 0; }
.teahouse-info { flex: 1; }
.teahouse-name { display: block; font-size: var(--text-base); font-weight: var(--font-bold); color: var(--text-primary); }
.teahouse-desc { display: block; font-size: var(--text-xs); color: var(--text-muted); margin-top: 4rpx; }
.teahouse-arrow { font-size: 36rpx; color: var(--text-muted); }

/* ========== 列表视图 ========== */
.list-view { padding: 0; }
.demands-section { padding: 20rpx 24rpx; }
.section-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16rpx; }
.section-title { font-size: var(--text-lg); font-weight: var(--font-bold); color: var(--text-primary); }
.section-count { font-size: var(--text-sm); color: var(--text-soft); }

.filter-scroll { margin-bottom: 16rpx; }
.filter-chips { display: flex; gap: 12rpx; padding-bottom: 4rpx; white-space: nowrap; }
.filter-chip {
  display: inline-block; padding: 10rpx 24rpx; border-radius: var(--radius-full);
  border: 2rpx solid var(--border-regular); font-size: var(--text-sm); color: var(--text-muted); white-space: nowrap;
}
.filter-chip.active { border-color: var(--primary-color); background: var(--color-success-bg); color: var(--primary-strong); font-weight: var(--font-bold); }

.empty-tip { text-align: center; padding: 60rpx; font-size: var(--text-base); color: var(--text-soft); }

.demand-card {
  background: var(--surface-deep); border-radius: var(--radius-lg); padding: 24rpx;
  margin-bottom: 16rpx; box-shadow: var(--shadow-sm);
}
.demand-tag-row { display: flex; justify-content: space-between; align-items: center; margin-bottom: 14rpx; }
.demand-work-tag {
  background: var(--color-success-bg); color: var(--primary-strong); border-radius: var(--radius-full);
  padding: 6rpx 20rpx; font-size: var(--text-sm); font-weight: var(--font-bold);
}
.demand-time { font-size: var(--text-xs); color: var(--text-soft); }
.demand-info-row { display: flex; margin-bottom: 8rpx; }
.info-label { font-size: var(--text-sm); color: var(--text-soft); width: 80rpx; }
.info-val { font-size: var(--text-sm); color: var(--text-regular); flex: 1; }
.demand-notes {
  display: flex; align-items: flex-start;
  background: var(--color-warning-bg); border-radius: var(--radius-md); padding: 14rpx 16rpx; margin: 12rpx 0;
}
.notes-icon { font-size: var(--text-sm); margin-right: 8rpx; flex-shrink: 0; }
.notes-text { font-size: var(--text-sm); color: var(--color-error); line-height: 1.5; }
.demand-footer { border-top: 1rpx solid var(--border-light); padding-top: 12rpx; margin-top: 8rpx; }
.contact-tip { font-size: var(--text-xs); color: var(--color-info); }
.load-more { text-align: center; padding: 24rpx; font-size: var(--text-base); color: var(--text-soft); }

/* ========== 农机视图 ========== */
.machine-view { padding: 20rpx 24rpx; }
.my-machine-card {
  background: var(--surface-deep); border-radius: var(--radius-xl); padding: 28rpx;
  box-shadow: var(--shadow-base);
}
.card-header { display: flex; justify-content: space-between; margin-bottom: 20rpx; }
.card-title { font-size: var(--text-base); font-weight: var(--font-bold); color: var(--text-primary); }
.card-action { font-size: var(--text-base); color: var(--primary-color); }
.machine-item {
  display: flex; align-items: center; padding: 16rpx 0;
  border-bottom: 1rpx solid var(--border-light);
}
.machine-icon { font-size: 40rpx; margin-right: 16rpx; }
.machine-info { flex: 1; }
.machine-name { display: block; font-size: var(--text-base); font-weight: var(--font-semibold); color: var(--text-primary); }
.machine-brand { display: block; font-size: var(--text-xs); color: var(--text-soft); }
.machine-badge {
  background: var(--color-info-bg); border-radius: var(--radius-xl); padding: 6rpx 16rpx;
  font-size: var(--text-xs); color: var(--color-info);
}
.no-machine { display: flex; align-items: center; justify-content: space-between; }
.no-machine-text { font-size: var(--text-base); color: var(--text-soft); }
.add-machine-btn {
  background: var(--gradient-primary);
  color: #fff; border: none; border-radius: var(--radius-full);
  font-size: var(--text-sm); padding: 12rpx 30rpx;
  box-shadow: var(--shadow-colored);
}
</style>
