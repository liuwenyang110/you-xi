<template>
  <view class="sessions-page">
    <view class="sessions-header safe-area-top">
      <text class="sessions-title">公益协作会话</text>
      <text class="sessions-desc">像微信一样查看最近沟通，但只用于公益对接，不做交易撮合。</text>
      <view class="sessions-toolbar">
        <view class="toolbar-pill">
          <text>{{ syncText }}</text>
        </view>
        <view class="toolbar-link" @click="goCommunity">回到广场</view>
      </view>
    </view>

    <view class="filter-row">
      <view
        v-for="filter in filters"
        :key="filter.key"
        class="filter-chip"
        :class="{ active: currentFilter === filter.key }"
        @click="currentFilter = filter.key"
      >
        <text>{{ filter.label }}</text>
      </view>
    </view>

    <scroll-view class="session-scroll" scroll-y refresher-enabled :refresher-triggered="refreshing" @refresherrefresh="refreshSessions">
      <view class="summary-card">
        <view class="summary-item">
          <text class="summary-value">{{ sessionCount }}</text>
          <text class="summary-label">会话数</text>
        </view>
        <view class="summary-item">
          <text class="summary-value">{{ unreadTotal }}</text>
          <text class="summary-label">未读</text>
        </view>
        <view class="summary-item">
          <text class="summary-value">{{ activeCount }}</text>
          <text class="summary-label">进行中</text>
        </view>
      </view>

      <view
        v-for="session in filteredSessions"
        :key="session.id"
        class="session-card"
        @click="openSession(session.id)"
      >
        <view class="session-avatar">
          <text>{{ session.avatarText }}</text>
        </view>

        <view class="session-body">
          <view class="session-top">
            <text class="session-title">{{ session.title }}</text>
            <text class="session-time">{{ formatRelative(session.updatedAt) }}</text>
          </view>
          <view class="session-mid">
            <text class="session-partner">{{ session.partnerName }}</text>
            <text class="session-status" :class="statusClass(session.status)">{{ session.statusLabel }}</text>
          </view>
          <text class="session-preview">{{ session.lastMessagePreview }}</text>
          <text class="session-disclaimer">{{ session.disclaimer }}</text>
        </view>

        <view v-if="session.unreadCount" class="session-unread">
          <text>{{ session.unreadCount > 99 ? '99+' : session.unreadCount }}</text>
        </view>
      </view>

      <view v-if="!filteredSessions.length && !refreshing" class="empty-state">
        <text class="empty-title">还没有协作会话</text>
        <text class="empty-desc">去公益广场点开一条求助或找机动态，就能开始像微信一样的协作沟通。</text>
        <view class="empty-btn" @click="goCommunity">去公益广场</view>
      </view>
    </scroll-view>
  </view>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue'
import { onLoad, onPullDownRefresh, onShow } from '@dcloudio/uni-app'
import { useCollabStore } from '@/stores/collab'
import { formatRelativeTime } from '@/utils/collab'
import type { CollabSessionStatus } from '@/types/collab'

const collabStore = useCollabStore()
const refreshing = ref(false)
const currentFilter = ref<'all' | 'active' | 'completed' | 'closed'>('all')

const filters = [
  { key: 'all', label: '全部' },
  { key: 'active', label: '进行中' },
  { key: 'completed', label: '已完成' },
  { key: 'closed', label: '已关闭' }
] as const

const filteredSessions = computed(() => {
  if (currentFilter.value === 'active') {
    return collabStore.sessionList.filter((item) => !['COMPLETED', 'CLOSED'].includes(item.status))
  }
  if (currentFilter.value === 'completed') {
    return collabStore.sessionList.filter((item) => item.status === 'COMPLETED')
  }
  if (currentFilter.value === 'closed') {
    return collabStore.sessionList.filter((item) => item.status === 'CLOSED')
  }
  return collabStore.sessionList
})

const sessionCount = computed(() => collabStore.sessionList.length)
const unreadTotal = computed(() =>
  collabStore.sessionList.reduce((sum, item) => sum + (item.unreadCount || 0), 0)
)
const activeCount = computed(() =>
  collabStore.sessionList.filter((item) => !['COMPLETED', 'CLOSED'].includes(item.status)).length
)
const syncText = computed(() => {
  if (collabStore.syncStatus.sessions === 'offline') {
    return '本地会话缓存'
  }
  if (collabStore.syncStatus.sessions === 'syncing') {
    return '正在同步'
  }
  return 'REST 轮询同步'
})

function formatRelative(value: string) {
  return formatRelativeTime(value)
}

function statusClass(status: CollabSessionStatus) {
  return {
    'status-active': ['PUBLISHED', 'CLAIMED', 'COMMUNICATING'].includes(status),
    'status-info': ['ARRIVING', 'IN_SERVICE'].includes(status),
    'status-done': status === 'COMPLETED',
    'status-muted': status === 'CLOSED'
  }
}

async function refreshSessions() {
  refreshing.value = true
  await collabStore.loadSessions()
  refreshing.value = false
}

function openSession(sessionId: string) {
  collabStore.setActiveSession(sessionId)
  uni.navigateTo({ url: `/pages/common/collab-chat?sessionId=${encodeURIComponent(sessionId)}` })
}

function goCommunity() {
  uni.navigateTo({ url: '/pages/community/index' })
}

onLoad(async () => {
  await refreshSessions()
})

onShow(async () => {
  await collabStore.loadSessions({ silent: true })
})

onPullDownRefresh(async () => {
  await refreshSessions()
  uni.stopPullDownRefresh()
})
</script>

<style scoped>
.sessions-page {
  min-height: 100vh;
  background:
    radial-gradient(circle at top left, rgba(176, 230, 196, 0.34), transparent 32%),
    linear-gradient(180deg, #eef6f0 0%, #f7faf8 100%);
}

.sessions-header {
  padding: 28rpx 28rpx 24rpx;
  background: linear-gradient(180deg, rgba(255, 255, 255, 0.82), rgba(255, 255, 255, 0.58));
  backdrop-filter: blur(18px);
}

.sessions-title {
  display: block;
  font-size: 44rpx;
  font-weight: 900;
  color: #173526;
}

.sessions-desc {
  display: block;
  margin-top: 10rpx;
  font-size: 25rpx;
  line-height: 1.7;
  color: #617566;
}

.sessions-toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 16rpx;
  margin-top: 20rpx;
}

.toolbar-pill,
.toolbar-link {
  padding: 14rpx 20rpx;
  border-radius: 999rpx;
  font-size: 24rpx;
}

.toolbar-pill {
  background: rgba(36, 87, 61, 0.08);
  color: #24573d;
  font-weight: 700;
}

.toolbar-link {
  color: #24573d;
  background: #fff;
}

.filter-row {
  display: flex;
  gap: 14rpx;
  padding: 18rpx 24rpx 0;
}

.filter-chip {
  flex: 1;
  padding: 16rpx 0;
  border-radius: 999rpx;
  text-align: center;
  font-size: 24rpx;
  color: #68806e;
  background: rgba(255, 255, 255, 0.8);
}

.filter-chip.active {
  background: #24573d;
  color: #fff;
  font-weight: 800;
}

.session-scroll {
  height: calc(100vh - 300rpx);
  padding: 18rpx 24rpx 36rpx;
  box-sizing: border-box;
}

.summary-card {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 16rpx;
  padding: 24rpx;
  border-radius: 24rpx;
  background: rgba(255, 255, 255, 0.92);
  box-shadow: 0 14rpx 28rpx rgba(26, 58, 40, 0.06);
  margin-bottom: 18rpx;
}

.summary-item {
  text-align: center;
}

.summary-value {
  display: block;
  font-size: 40rpx;
  font-weight: 900;
  color: #183828;
}

.summary-label {
  display: block;
  margin-top: 6rpx;
  font-size: 22rpx;
  color: #718372;
}

.session-card {
  display: flex;
  gap: 18rpx;
  position: relative;
  padding: 24rpx;
  margin-bottom: 16rpx;
  border-radius: 24rpx;
  background: rgba(255, 255, 255, 0.92);
  box-shadow: 0 14rpx 28rpx rgba(26, 58, 40, 0.05);
}

.session-avatar {
  width: 88rpx;
  height: 88rpx;
  border-radius: 28rpx;
  background: linear-gradient(135deg, #d4e9d9, #7db790);
  color: #1a432e;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 34rpx;
  font-weight: 900;
  flex-shrink: 0;
}

.session-body {
  flex: 1;
  min-width: 0;
}

.session-top,
.session-mid {
  display: flex;
  justify-content: space-between;
  gap: 16rpx;
  align-items: center;
}

.session-title {
  flex: 1;
  font-size: 30rpx;
  font-weight: 800;
  color: #183828;
}

.session-time {
  font-size: 22rpx;
  color: #819282;
}

.session-mid {
  margin-top: 8rpx;
}

.session-partner {
  font-size: 24rpx;
  color: #4d6353;
}

.session-status {
  padding: 8rpx 16rpx;
  border-radius: 999rpx;
  font-size: 22rpx;
  font-weight: 700;
}

.status-active {
  background: rgba(46, 116, 80, 0.1);
  color: #2e7450;
}

.status-info {
  background: rgba(51, 110, 178, 0.1);
  color: #336eb2;
}

.status-done {
  background: rgba(218, 166, 54, 0.12);
  color: #9b6e0d;
}

.status-muted {
  background: rgba(105, 122, 111, 0.1);
  color: #6a7c70;
}

.session-preview {
  display: block;
  margin-top: 12rpx;
  font-size: 25rpx;
  line-height: 1.6;
  color: #415747;
}

.session-disclaimer {
  display: block;
  margin-top: 12rpx;
  font-size: 21rpx;
  color: #7f9080;
  line-height: 1.5;
}

.session-unread {
  position: absolute;
  right: 18rpx;
  top: 18rpx;
  min-width: 38rpx;
  height: 38rpx;
  padding: 0 10rpx;
  border-radius: 999rpx;
  background: #ec624d;
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20rpx;
  font-weight: 800;
}

.empty-state {
  padding: 140rpx 56rpx;
  text-align: center;
}

.empty-title {
  display: block;
  font-size: 36rpx;
  font-weight: 900;
  color: #183828;
}

.empty-desc {
  display: block;
  margin-top: 16rpx;
  font-size: 26rpx;
  line-height: 1.75;
  color: #738673;
}

.empty-btn {
  display: inline-block;
  margin-top: 28rpx;
  padding: 20rpx 40rpx;
  border-radius: 999rpx;
  background: linear-gradient(135deg, #24573d, #3b875b);
  color: #fff;
  font-size: 28rpx;
  font-weight: 800;
}
</style>
