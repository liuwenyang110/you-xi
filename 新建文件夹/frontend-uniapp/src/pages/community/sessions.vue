<template>
  <view class="sessions-page">
    <view class="header safe-area-top">
      <text class="title">协作会话</text>
      <text class="subtitle">像微信一样沟通，但只用于公益协作</text>
    </view>

    <view class="banner">平台只协助沟通，不参与交易、定价或收款。</view>

    <scroll-view class="list" scroll-y>
      <view v-if="syncStatus === 'syncing'" class="state-card">正在同步会话…</view>
      <view v-else-if="sessionList.length === 0" class="state-card">还没有协作会话，去公益广场发起一个吧。</view>

      <view v-for="item in sessionList" :key="item.id" class="session-card" @click="openSession(item)">
        <view class="avatar">{{ avatarText(item.counterpartName) }}</view>
        <view class="body">
          <view class="row">
            <text class="name">{{ item.counterpartName || '协作伙伴' }}</text>
            <text class="time">{{ formatTime(item.lastMessageAt) }}</text>
          </view>
          <view class="subject-row">
            <text class="subject">{{ item.subject }}</text>
            <text class="status-pill" :class="statusClass(item.status)">{{ statusLabel(item.status) }}</text>
          </view>
          <view class="row">
            <text class="preview">{{ item.lastMessagePreview || item.publicNotice }}</text>
            <text v-if="item.unreadCount" class="unread">{{ item.unreadCount }}</text>
          </view>
        </view>
      </view>
      <view class="bottom-space"></view>
    </scroll-view>
  </view>
</template>

<script setup lang="ts">
import { computed, onMounted } from 'vue'
import { useCollabStore } from '../../stores/collab'

const collabStore = useCollabStore()
const sessionList = computed(() => collabStore.sessionList)
const syncStatus = computed(() => collabStore.syncStatus)

onMounted(async () => {
  await collabStore.loadSessions()
})

function avatarText(name: string) {
  return (name || '协').slice(-1)
}

function formatTime(value: string) {
  if (!value) return ''
  return value.replace('T', ' ').slice(5, 16)
}

function statusLabel(status: string) {
  const labels: Record<string, string> = {
    CONTACTED: '已联系',
    CLAIMED: '已认领',
    IN_PROGRESS: '协作中',
    COMPLETED: '已完成',
    CLOSED: '已关闭'
  }
  return labels[status] || '协作中'
}

function statusClass(status: string) {
  if (status === 'COMPLETED') return 'done'
  if (status === 'CLOSED') return 'closed'
  if (status === 'CLAIMED') return 'claimed'
  return 'active'
}

async function openSession(session: any) {
  await collabStore.openSession(session)
  uni.navigateTo({ url: `/pages/community/chat?id=${session.id}` })
}
</script>

<style scoped>
.sessions-page {
  min-height: 100vh;
  background: #f5f7f6;
}
.header {
  padding: 32rpx 24rpx 18rpx;
}
.title {
  display: block;
  font-size: 38rpx;
  font-weight: 700;
  color: #20362c;
}
.subtitle {
  display: block;
  margin-top: 8rpx;
  font-size: 24rpx;
  color: #6d7f74;
}
.banner,
.state-card,
.session-card {
  margin: 0 24rpx 20rpx;
  background: #fff;
  border-radius: 24rpx;
}
.banner,
.state-card {
  padding: 24rpx;
  color: #4d6456;
}
.list {
  height: calc(100vh - 180rpx);
}
.session-card {
  display: flex;
  gap: 20rpx;
  padding: 24rpx;
}
.avatar {
  width: 88rpx;
  height: 88rpx;
  border-radius: 28rpx;
  background: linear-gradient(135deg, #1f8f5f, #57b98a);
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 34rpx;
  font-weight: 700;
}
.body {
  flex: 1;
  min-width: 0;
}
.row {
  display: flex;
  justify-content: space-between;
  gap: 12rpx;
  align-items: center;
}
.name,
.subject,
.preview,
.time {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.name {
  font-size: 28rpx;
  font-weight: 700;
  color: #20362c;
}
.subject {
  display: block;
  margin-top: 10rpx;
  font-size: 24rpx;
  color: #466050;
}
.subject-row {
  display: flex;
  align-items: center;
  gap: 12rpx;
  margin-top: 10rpx;
}
.status-pill {
  flex-shrink: 0;
  padding: 6rpx 14rpx;
  border-radius: 999rpx;
  font-size: 20rpx;
  color: #1f8f5f;
  background: rgba(31, 143, 95, 0.1);
}
.status-pill.claimed {
  color: #8a5b12;
  background: rgba(217, 119, 6, 0.12);
}
.status-pill.done {
  color: #17603d;
  background: rgba(22, 163, 74, 0.12);
}
.status-pill.closed {
  color: #6b7280;
  background: rgba(107, 114, 128, 0.12);
}
.preview {
  flex: 1;
  margin-top: 10rpx;
  font-size: 22rpx;
  color: #7a887f;
}
.time {
  font-size: 20rpx;
  color: #90a097;
}
.unread {
  min-width: 36rpx;
  height: 36rpx;
  padding: 0 10rpx;
  border-radius: 999rpx;
  background: #e5484d;
  color: #fff;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  font-size: 20rpx;
}
.bottom-space {
  height: 80rpx;
}
</style>
