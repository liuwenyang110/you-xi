<template>
  <view class="demand-list-page">
    <view class="page-header">
      <text class="page-title">我的需求</text>
      <text class="page-sub">您发布的所有需求记录</text>
    </view>

    <!-- 状态过滤 tab -->
    <view class="status-tabs">
      <view
        v-for="tab in tabs"
        :key="tab.key"
        class="tab-item"
        :class="{ active: activeTab === tab.key }"
        @click="switchTab(tab.key)"
      >{{ tab.label }}</view>
    </view>

    <!-- 发布按钮 -->
    <view class="publish-fab" @click="goPublish">
      <text class="fab-icon">＋</text>
      <text class="fab-text">发布新需求</text>
    </view>

    <!-- 需求列表 -->
    <scroll-view class="demand-scroll" scroll-y>
      <view v-if="filteredList.length === 0 && !loading" class="empty-state">
        <text class="empty-icon"></text>
        <text class="empty-title">{{ activeTab === 'ALL' ? '还没有发布过需求' : '暂无该状态的需求' }}</text>
        <button class="empty-btn" @click="goPublish">立即发布需求</button>
      </view>

      <view
        v-for="item in filteredList"
        :key="item.id"
        class="demand-card"
      >
        <!-- 卡片头 -->
        <view class="card-head">
          <view class="work-badge">
            <text class="work-badge-icon"></text>
            <text class="work-badge-name">{{ getWorkTypeName(item.workTypeId) }}</text>
          </view>
          <view class="status-pill" :class="statusClass(item.status)">
            {{ statusText(item.status) }}
          </view>
        </view>

        <!-- 信息行 -->
        <view class="card-body">
          <view class="info-row" v-if="item.areaDesc">
            <text class="info-key">面积</text>
            <text class="info-val">{{ item.areaDesc }}</text>
          </view>
          <view class="info-row" v-if="item.locationDesc">
            <text class="info-key">位置</text>
            <text class="info-val">{{ item.locationDesc }}</text>
          </view>
          <view class="info-row" v-if="item.expectDateStart">
            <text class="info-key">时间</text>
            <text class="info-val">{{ item.expectDateStart }} ~ {{ item.expectDateEnd || '待定' }}</text>
          </view>
          <view class="info-row warn" v-if="item.plotNotes">
            <text class="info-key">备注</text>
            <text class="info-val warn-text">{{ item.plotNotes }}</text>
          </view>
        </view>

        <!-- 操作按钮：公益语言 -->
        <view class="card-actions" v-if="item.status === 'PUBLISHED'">
          <view class="action-btn contacted" @click="changeStatus(item.id, 'CONTACTED')">
            ✓ 已联系到服务者
          </view>
          <view class="action-btn cancel" @click="changeStatus(item.id, 'CANCELLED')">
            取消
          </view>
        </view>
        <view class="card-actions" v-else-if="item.status === 'CONTACTED'">
          <view class="action-btn done" @click="changeStatus(item.id, 'COMPLETED')">
            🎉 作业已完成
          </view>
        </view>

        <!-- 底部时间 -->
        <view class="card-foot">
          <text class="foot-time">{{ formatRelativeTime(item.publishedAt) }}发布</text>
        </view>
      </view>
    </scroll-view>

    <!-- 公益声明浮动条 -->
    <view class="charity-notice">
      <text class="notice-icon">🌿</text>
      <text class="notice-text">本平台完全免费，不收取任何费用，价格由您与服务者直接商定</text>
    </view>
  </view>
</template>

<script>
import { myDemands, updateDemandStatus } from '@/api/v3/demand'
import { useV3ZoneStore } from '@/stores/v3/zoneStore'

export default {
  name: 'DemandList',
  data() {
    return {
      loading: false,
      allList: [],
      activeTab: 'ALL',
      tabs: [
        { key: 'ALL',       label: '全部' },
        { key: 'PUBLISHED', label: '待联系' },
        { key: 'CONTACTED', label: '已联系' },
        { key: 'COMPLETED', label: '已完成' },
        { key: 'CANCELLED', label: '已取消' },
      ],
    }
  },
  computed: {
    filteredList() {
      if (this.activeTab === 'ALL') return this.allList
      return this.allList.filter(d => d.status === this.activeTab)
    }
  },
  async onLoad() {
    const store = useV3ZoneStore()
    if (!store.dictsLoaded) await store.loadDicts()
    await this.loadList()
  },
  async onPullDownRefresh() {
    await this.loadList()
    uni.stopPullDownRefresh()
  },
  methods: {
    async loadList() {
      this.loading = true
      try {
        const res = await myDemands()
        if (res.code === 0) this.allList = res.data || []
      } finally {
        this.loading = false
      }
    },
    switchTab(key) { this.activeTab = key },
    getWorkTypeName(id) {
      const wt = useV3ZoneStore().dicts.workTypes.find(w => w.id === id)
      return wt?.name || '农事作业'
    },
    statusText(s) {
      return { PUBLISHED: '等待联系', CONTACTED: '已联系', COMPLETED: '已完成', CANCELLED: '已取消' }[s] || s
    },
    statusClass(s) {
      return { PUBLISHED: 'pill-pub', CONTACTED: 'pill-contacted', COMPLETED: 'pill-done', CANCELLED: 'pill-cancel' }[s] || ''
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
    async changeStatus(id, status) {
      const labels = { CONTACTED: '确认已联系到服务者？', COMPLETED: '确认农事已完成？', CANCELLED: '确认取消此需求？' }
      uni.showModal({
        title: '操作确认',
        content: labels[status] || '确认修改？',
        success: async (r) => {
          if (!r.confirm) return
          const res = await updateDemandStatus(id, status)
          if (res.code === 0) {
            uni.showToast({ title: '已更新', icon: 'success' })
            await this.loadList()
          } else {
            uni.showToast({ title: res.message || '操作失败', icon: 'none' })
          }
        }
      })
    },
    goPublish() { uni.navigateTo({ url: '/pages/farmer/demand-publish' }) }
  }
}
</script>

<style>
.demand-list-page { background: #F5F9F6; min-height: 100vh; padding-bottom: 100rpx; }

/* 公益绿渐变头部 */
.page-header { background: linear-gradient(135deg, #2D7A4F, #5BA88A); padding: 80rpx 40rpx 32rpx; }
.page-title { display: block; font-size: 40rpx; font-weight: 700; color: #fff; }
.page-sub { display: block; font-size: 26rpx; color: rgba(255,255,255,0.8); margin-top: 6rpx; }

.status-tabs {
  display: flex;
  background: #fff;
  overflow-x: auto;
  padding: 0 16rpx;
  box-shadow: 0 2rpx 8rpx rgba(0,0,0,0.05);
}
.tab-item {
  flex-shrink: 0;
  padding: 24rpx 28rpx;
  font-size: 28rpx;
  color: #888;
  border-bottom: 4rpx solid transparent;
  white-space: nowrap;
}
.tab-item.active { color: #2D7A4F; border-bottom-color: #5BA88A; font-weight: 700; }

.publish-fab {
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #5BA88A, #2D7A4F);
  margin: 20rpx 24rpx 0;
  border-radius: 16rpx;
  padding: 20rpx;
  box-shadow: 0 6rpx 20rpx rgba(45,122,79,0.3);
}
.fab-icon { font-size: 36rpx; color: #fff; margin-right: 10rpx; font-weight: 700; }
.fab-text { font-size: 30rpx; color: #fff; font-weight: 700; }

.demand-scroll { height: calc(100vh - 340rpx); }

.empty-state { display: flex; flex-direction: column; align-items: center; padding: 100rpx 40rpx; }
.empty-icon { font-size: 100rpx; margin-bottom: 24rpx; }
.empty-title { font-size: 32rpx; color: #888; margin-bottom: 32rpx; }
.empty-btn { background: linear-gradient(135deg, #5BA88A, #2D7A4F); color: #fff; border: none; border-radius: 40rpx; font-size: 28rpx; padding: 18rpx 48rpx; }

.demand-card {
  background: #fff;
  margin: 16rpx 24rpx 0;
  border-radius: 20rpx;
  padding: 24rpx;
  box-shadow: 0 2rpx 12rpx rgba(0,0,0,0.06);
}
.card-head { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16rpx; }
.work-badge { display: flex; align-items: center; background: #EAF5EE; border-radius: 28rpx; padding: 8rpx 20rpx; }
.work-badge-icon { font-size: 26rpx; margin-right: 6rpx; }
.work-badge-name { font-size: 26rpx; color: #2D7A4F; font-weight: 700; }

.status-pill { border-radius: 24rpx; padding: 6rpx 18rpx; font-size: 24rpx; font-weight: 600; }
.pill-pub { background: #E8F5EE; color: #2D7A4F; }
.pill-contacted { background: #E3F2FD; color: #1565C0; }
.pill-done { background: #F5F5F5; color: #757575; }
.pill-cancel { background: #FFEBEE; color: #C62828; }

.card-body { margin-bottom: 12rpx; }
.info-row { display: flex; margin-bottom: 10rpx; }
.info-key { font-size: 26rpx; color: #999; width: 80rpx; flex-shrink: 0; }
.info-val { font-size: 28rpx; color: #333; flex: 1; }
.info-row.warn .info-val { color: #D84315; }

.card-actions { display: flex; gap: 16rpx; margin: 16rpx 0 8rpx; }
.action-btn { flex: 1; text-align: center; padding: 18rpx; border-radius: 12rpx; font-size: 26rpx; font-weight: 600; }
.action-btn.contacted { background: #EAF5EE; color: #2D7A4F; }
.action-btn.cancel { background: #FFF3F3; color: #C62828; }
.action-btn.done { background: #E3F2FD; color: #1565C0; }

.card-foot { border-top: 1rpx solid #F5F5F5; padding-top: 12rpx; }
.foot-time { font-size: 24rpx; color: #bbb; }
</style>
