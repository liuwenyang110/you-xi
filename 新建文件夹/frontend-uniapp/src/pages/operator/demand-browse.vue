<template>
  <view class="demand-browse">
    <view class="page-header">
      <text class="page-title">片区待接农活</text>
      <text class="page-sub">{{ zoneName }} · 待联系 {{ total }} 条</text>
    </view>

    <!-- 作业类型快筛 - 适老化大按钮 -->
    <scroll-view class="filter-scroll" scroll-x>
      <view class="filter-chips">
        <view
          class="filter-chip-large"
          :class="{ active: filterWorkTypeId === null }"
          @click="setFilter(null)"
        >全部农活</view>
        <view
          v-for="wt in workTypes"
          :key="wt.id"
          class="filter-chip-large"
          :class="{ active: filterWorkTypeId === wt.id }"
          @click="setFilter(wt.id)"
        >{{ wt.name }}</view>
      </view>
    </scroll-view>

    <!-- 列表 -->
    <view v-if="demands.length === 0 && !loading" class="empty-state">
      <text class="empty-icon"></text>
      <text class="empty-text">{{ filterWorkTypeId ? '该类型暂无待接农活' : '本片区暂无待接农活' }}</text>
    </view>

    <view
      v-for="item in demands"
      :key="item.id"
      class="demand-card"
    >
      <view class="card-top">
        <view class="work-tag">{{ getWorkTypeName(item.workTypeId) }}</view>
        <view class="time-tag" v-if="item.expectDateStart">
          📅 {{ formatDate(item.expectDateStart) }}
        </view>
      </view>

      <view class="card-info">
        <view class="info-row" v-if="item.areaDesc">
          <text class="info-k">面积</text>
          <text class="info-v">{{ item.areaDesc }}</text>
        </view>
        <view class="info-row" v-if="item.locationDesc">
          <text class="info-k">位置</text>
          <text class="info-v">{{ item.locationDesc }}</text>
        </view>
        <view class="info-row" v-if="item.cropId">
          <text class="info-k">作物</text>
          <text class="info-v">{{ getCropName(item.cropId) }}</text>
        </view>
      </view>

      <!-- 地块注意事项 醒目展示 -->
      <view class="plot-notes" v-if="item.plotNotes">
        <text class="notes-head">⚠️ 注意事项</text>
        <text class="notes-body">{{ item.plotNotes }}</text>
      </view>

      <view class="card-foot">
        <text class="pub-time">{{ formatRelativeTime(item.publishedAt) }}发布</text>
      </view>
    </view>

    <view class="load-more" v-if="hasMore" @click="loadMore">
      <text>加载更多</text>
    </view>
  </view>
</template>

<script>
import { getZoneDemands } from '@/api/v3/demand'
import { useV3UserStore } from '@/stores/v3/userStore'
import { useV3ZoneStore } from '@/stores/v3/zoneStore'

export default {
  name: 'DemandBrowse',
  data() {
    return {
      demands: [],
      page: 1,
      hasMore: false,
      total: 0,
      loading: false,
      filterWorkTypeId: null,
    }
  },
  computed: {
    zoneName() { return useV3ZoneStore().zoneName },
    workTypes() { return useV3ZoneStore().dicts.workTypes || [] },
    zoneId() { return useV3UserStore().zoneId },
  },
  async onLoad() {
    const store = useV3ZoneStore()
    if (!store.dictsLoaded) await store.loadDicts()
    await this.loadDemands()
  },
  async onPullDownRefresh() {
    this.page = 1
    this.demands = []
    await this.loadDemands()
    uni.stopPullDownRefresh()
  },
  methods: {
    async setFilter(workTypeId) {
      this.filterWorkTypeId = workTypeId
      this.page = 1
      this.demands = []
      await this.loadDemands()
    },
    async loadDemands() {
      if (!this.zoneId || this.loading) return
      this.loading = true
      try {
        const res = await getZoneDemands(this.zoneId, this.page, 15)
        if (res.code === 0) {
          let list = res.data.list || []
          if (this.filterWorkTypeId) {
            list = list.filter(d => d.workTypeId === this.filterWorkTypeId)
          }
          this.demands = this.page === 1 ? list : [...this.demands, ...list]
          this.total = res.data.total
          this.hasMore = (this.page * 15) < res.data.total
        }
      } catch (e) {
        uni.showToast({ title: '加载失败', icon: 'none' })
      } finally {
        this.loading = false
      }
    },
    async loadMore() {
      this.page++
      await this.loadDemands()
    },
    getWorkTypeName(id) {
      const wt = this.workTypes.find(w => w.id === id)
      return wt?.name || '农事作业'
    },
    getCropName(id) {
      const crops = useV3ZoneStore().dicts.crops || []
      const c = crops.find(c => c.id === id)
      return c?.name || ''
    },
    formatDate(dateStr) {
      if (!dateStr) return ''
      const d = new Date(dateStr)
      return `${d.getMonth() + 1}月${d.getDate()}日`
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
    }
  }
}
</script>

<style scoped>
.demand-browse { background: var(--page-bg); min-height: 100vh; min-height: 100dvh; padding-bottom: 40rpx; }
.page-header { background: var(--gradient-primary); padding: 80rpx 40rpx 32rpx; }
.page-title { display: block; font-size: 40rpx; font-weight: var(--font-bold); color: #fff; }
.page-sub { display: block; font-size: var(--text-sm); color: rgba(255,255,255,0.8); margin-top: 6rpx; }

.filter-scroll { background: var(--surface-deep); padding: 0 16rpx; box-shadow: var(--shadow-sm); }
.filter-chips { display: flex; gap: 12rpx; padding: 16rpx 0; white-space: nowrap; }
.filter-chip-large {
  display: inline-block;
  padding: 16rpx 32rpx;
  border-radius: var(--radius-lg);
  border: 2rpx solid var(--border-regular);
  font-size: 30rpx;
  color: var(--text-muted);
  background: #fff;
  white-space: nowrap;
  flex-shrink: 0;
  margin-right: 16rpx;
  box-shadow: 0 2rpx 6rpx rgba(0,0,0,0.03);
}
.filter-chip-large.active {
  background: var(--gradient-primary);
  color: #fff;
  border-color: transparent;
  font-weight: 800;
  box-shadow: 0 4rpx 12rpx rgba(45,122,79,0.3);
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 100rpx;
}
.empty-icon { font-size: 100rpx; margin-bottom: 24rpx; }
.empty-text { font-size: var(--text-base); color: var(--text-soft); }

.demand-card {
  background: var(--surface-deep);
  margin: 16rpx 24rpx 0;
  border-radius: var(--radius-xl);
  padding: 24rpx;
  box-shadow: var(--shadow-base);
  border-left: 6rpx solid var(--color-success);
}
.card-top {
  display: flex;
  align-items: center;
  gap: 16rpx;
  margin-bottom: 16rpx;
}
.work-tag {
  background: var(--color-success-bg);
  border-radius: var(--radius-full);
  padding: 8rpx 22rpx;
  font-size: var(--text-base);
  color: var(--primary-strong);
  font-weight: var(--font-bold);
}
.time-tag {
  font-size: var(--text-sm);
  color: var(--color-warning);
  background: var(--color-warning-bg);
  border-radius: var(--radius-full);
  padding: 6rpx 18rpx;
}
.card-info { margin-bottom: 12rpx; }
.info-row { display: flex; margin-bottom: 10rpx; }
.info-k { font-size: var(--text-sm); color: var(--text-soft); width: 80rpx; flex-shrink: 0; }
.info-v { font-size: var(--text-base); color: var(--text-primary); flex: 1; }

.plot-notes {
  background: var(--color-warning-bg);
  border-radius: var(--radius-lg);
  padding: 18rpx 20rpx;
  margin-bottom: 12rpx;
}
.notes-head { display: block; font-size: var(--text-sm); font-weight: var(--font-bold); color: var(--color-error); margin-bottom: 8rpx; }
.notes-body { display: block; font-size: var(--text-sm); color: var(--color-warning-text, #8D4E00); line-height: 1.6; }

.card-foot {
  display: flex;
  justify-content: space-between;
  align-items: center;
  border-top: 1rpx solid var(--border-light);
  padding-top: 14rpx;
}
.pub-time { font-size: var(--text-xs); color: var(--text-soft); }
.status-tip { font-size: var(--text-xs); color: var(--color-info); }
.load-more { text-align: center; padding: 32rpx; font-size: var(--text-base); color: var(--text-soft); }
</style>
