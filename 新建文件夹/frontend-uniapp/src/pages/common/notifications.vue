<template>
  <view class="noti-page">
    <view class="noti-header">
      <text class="noti-title">消息中心</text>
      <view class="noti-read-all" v-if="unreadCount > 0" @click="handleReadAll">
        <text class="read-all-text">全部已读</text>
      </view>
    </view>

    <!-- 未读角标 -->
    <view class="unread-badge" v-if="unreadCount > 0">
      <text class="badge-text">{{ unreadCount }} 条未读消息</text>
    </view>

    <!-- 通知列表 -->
    <view class="noti-list">
      <view v-if="notifications.length === 0 && !loading" class="noti-empty">
        <text class="empty-icon">📭</text>
        <text class="empty-text">暂无消息通知</text>
      </view>

      <view
        v-for="item in notifications"
        :key="item.id"
        class="noti-card"
        :class="{ 'noti-unread': !item.isRead }"
        @click="handleNotificationClick(item)"
      >
        <view class="noti-card-left">
          <text class="noti-type-icon">{{ getTypeIcon(item.notiType) }}</text>
          <view class="noti-dot" v-if="!item.isRead"></view>
        </view>
        <view class="noti-card-body">
          <text class="noti-card-title">{{ item.title }}</text>
          <text class="noti-card-content">{{ item.content }}</text>
          <text class="noti-card-time">{{ formatTime(item.createdAt) }}</text>
        </view>
        <view class="noti-card-action" v-if="item.needAction && !item.actionDone">
          <view
            v-if="item.actionType === 'CONFIRM_COMPLETE'"
            class="action-btn complete-btn"
            @click.stop="handleConfirmComplete(item)"
          >
            <text>干完啦</text>
          </view>
          <view
            v-if="item.actionType === 'CONFIRM_COMPLETE'"
            class="action-btn ongoing-btn"
            @click.stop="handleConfirmOngoing(item)"
          >
            <text>还没完</text>
          </view>
        </view>
        <view class="noti-card-action" v-if="item.needAction && item.actionDone">
          <text class="action-done-text">✓ 已处理</text>
        </view>
      </view>
    </view>
  </view>
</template>

<script>
import { getNotifications, markAllRead, markRead, confirmAction } from '@/api/v3/notification'
import { confirmDemandComplete, confirmDemandOngoing } from '@/api/v3/notification'

export default {
  name: 'NotificationCenter',
  data() {
    return {
      notifications: [],
      unreadCount: 0,
      page: 1,
      loading: false,
    }
  },
  async onLoad() {
    await this.loadNotifications()
  },
  async onPullDownRefresh() {
    this.page = 1
    await this.loadNotifications()
    uni.stopPullDownRefresh()
  },
  methods: {
    async loadNotifications() {
      this.loading = true
      try {
        const res = await getNotifications(this.page, 30)
        if (res.code === 0) {
          this.notifications = res.data.list || []
          this.unreadCount = res.data.unreadCount || 0
        }
      } finally {
        this.loading = false
      }
    },
    async handleReadAll() {
      await markAllRead()
      this.unreadCount = 0
      this.notifications.forEach(n => { n.isRead = 1 })
      uni.showToast({ title: '已全部标记已读', icon: 'success' })
    },
    async handleNotificationClick(item) {
      if (!item.isRead) {
        await markRead(item.id)
        item.isRead = 1
        this.unreadCount = Math.max(0, this.unreadCount - 1)
      }
    },
    async handleConfirmComplete(item) {
      if (!item.refId) return
      const res = await confirmDemandComplete(item.refId)
      if (res.code === 0) {
        await confirmAction(item.id)
        item.actionDone = 1
        uni.showToast({ title: '已确认完成！', icon: 'success' })
      }
    },
    async handleConfirmOngoing(item) {
      if (!item.refId) return
      const res = await confirmDemandOngoing(item.refId)
      if (res.code === 0) {
        await confirmAction(item.id)
        item.actionDone = 1
        uni.showToast({ title: '收到，继续帮您挂着', icon: 'none' })
      }
    },
    getTypeIcon(type) {
      return { DEMAND_ASK: '', DEMAND_WARN: '', TEAHOUSE_CLOSING: '', SYSTEM: '' }[type] || ''
    },
    formatTime(dt) {
      if (!dt) return ''
      const d = new Date(dt)
      const diff = Date.now() - d.getTime()
      if (diff < 60000) return '刚刚'
      if (diff < 3600000) return Math.floor(diff / 60000) + '分钟前'
      if (diff < 86400000) return Math.floor(diff / 3600000) + '小时前'
      const days = Math.floor(diff / 86400000)
      return days + '天前'
    },
  },
}
</script>

<style>
.noti-page { background: #F0F4ED; min-height: 100vh; padding-bottom: 40rpx; }

.noti-header {
  display: flex; justify-content: space-between; align-items: center;
  padding: 32rpx 32rpx 16rpx; background: #fff;
}
.noti-title { font-size: 36rpx; font-weight: 700; color: #1E3A12; }
.read-all-text { font-size: 28rpx; color: #4CAF50; font-weight: 600; }

.unread-badge {
  margin: 0 24rpx; padding: 14rpx 24rpx;
  background: linear-gradient(135deg, #E8F5E9, #C8E6C9); border-radius: 12rpx;
}
.badge-text { font-size: 26rpx; color: #2E7D32; font-weight: 600; }

.noti-list { padding: 16rpx 24rpx; }

.noti-empty { display: flex; flex-direction: column; align-items: center; padding: 80rpx 0; }
.empty-icon { font-size: 80rpx; margin-bottom: 16rpx; }
.empty-text { font-size: 30rpx; color: #999; }

.noti-card {
  display: flex; align-items: flex-start; gap: 16rpx;
  background: #fff; border-radius: 16rpx; padding: 24rpx;
  margin-bottom: 16rpx; box-shadow: 0 2rpx 10rpx rgba(0,0,0,0.04);
  transition: all 0.2s;
}
.noti-card.noti-unread { border-left: 6rpx solid #4CAF50; }

.noti-card-left { position: relative; flex-shrink: 0; }
.noti-type-icon { font-size: 44rpx; }
.noti-dot {
  position: absolute; top: -4rpx; right: -4rpx;
  width: 16rpx; height: 16rpx; border-radius: 50%;
  background: #F44336;
}

.noti-card-body { flex: 1; min-width: 0; }
.noti-card-title { display: block; font-size: 30rpx; font-weight: 700; color: #1E3A12; margin-bottom: 8rpx; }
.noti-card-content {
  display: block; font-size: 26rpx; color: #666; line-height: 1.6;
  overflow: hidden; display: -webkit-box; -webkit-line-clamp: 3; -webkit-box-orient: vertical;
}
.noti-card-time { display: block; font-size: 22rpx; color: #bbb; margin-top: 8rpx; }

.noti-card-action { display: flex; flex-direction: column; gap: 12rpx; flex-shrink: 0; align-self: center; }
.action-btn {
  padding: 12rpx 24rpx; border-radius: 20rpx; font-size: 24rpx; font-weight: 600; text-align: center;
}
.complete-btn { background: linear-gradient(135deg, #4CAF50, #2E7D32); color: #fff; }
.ongoing-btn { background: #F5F5F5; color: #666; }
.action-done-text { font-size: 24rpx; color: #4CAF50; }
</style>
