<template>
  <view class="community-page">
    <!-- 顶部状态栏占位 -->
    <view class="status-bar-placeholder"></view>

    <!-- 顶部导航与搜索 -->
    <view class="nav-header">
      <view class="search-bar">
        <icon type="search" size="16" color="#A0C4B5" />
        <input class="search-input" placeholder="查找急单、农机或是农业问题..." disabled @click="onSearch" />
      </view>
    </view>

    <!-- 顶部分类 Tab -->
    <view class="type-tabs">
      <view
        v-for="tab in tabs"
        :key="tab.value"
        class="tab-item"
        :class="{ active: currentTab === tab.value }"
        @click="switchTab(tab.value)"
      >
        <text class="tab-label">{{ tab.label }}</text>
        <view class="tab-line" v-if="currentTab === tab.value"></view>
      </view>
    </view>

    <!-- 列表展示区 -->
    <scroll-view
      class="post-list"
      scroll-y
      @scrolltolower="onReachBottom"
      refresher-enabled
      :refresher-triggered="refreshing"
      @refresherrefresh="onRefresh"
    >
      <view class="list-content">
        <!-- 帖子项 -->
        <view
          v-for="post in posts"
          :key="post.id"
          class="post-card"
          @click="goDetail(post.id)"
        >
          <!-- 头部：类型标签 + 状态 -->
          <view class="post-head">
            <view class="type-badge" :class="post.postType.toLowerCase()">
              {{ getTypeLabel(post.postType) }}
            </view>
            <text class="post-time">{{ formatTime(post.createdAt) }}</text>
          </view>

          <!-- 中部：主要内容 -->
          <view class="post-body">
            <text class="post-title">{{ post.title }}</text>
            <text class="post-desc">{{ truncateContent(post.content) }}</text>
          </view>

          <!-- 尾部：地理位置 + 交互统计 -->
          <view class="post-foot">
            <view class="location-wrap">
              <text class="location-icon">📍</text>
              <text class="location-text">{{ post.locationName || '未知位置' }}</text>
            </view>
            <view class="stats-wrap">
              <text class="stat-item">👁 {{ post.viewCount || 0 }}</text>
              <text class="stat-item">💬 {{ post.replyCount || 0 }}</text>
            </view>
          </view>

          <!-- 特殊展示：急单标志 -->
          <view v-if="post.isUrgent" class="urgent-ribbon">
            <text class="urgent-text">加急</text>
          </view>
        </view>

        <!-- 加载状态 -->
        <view class="load-more">
          <text v-if="loading" class="loading-text">耕耘加载中...</text>
          <text v-else-if="noMore" class="loading-text">木有了，常回来看看</text>
        </view>
      </view>
    </scroll-view>

    <!-- 悬浮发布按钮 -->
    <view class="fab-btn" @click="goPublish">
      <text class="fab-icon">+</text>
      <text class="fab-text">发布</text>
    </view>
  </view>
</template>

<script>
import { getCommunityPosts } from '@/api/v3/community'

export default {
  data() {
    return {
      currentTab: 'DEMAND_URGENT',
      tabs: [
        { label: '找机急单', value: 'DEMAND_URGENT' },
        { label: '农机租赁', value: 'RENTAL' },
        { label: '农技互动', value: 'CHAT' },
      ],
      posts: [],
      page: 1,
      loading: false,
      refreshing: false,
      noMore: false,
    }
  },
  onLoad() {
    this.loadData(true)
  },
  methods: {
    async loadData(reset = false) {
      if (this.loading) return
      if (reset) {
        this.page = 1
        this.noMore = false
      }
      if (this.noMore) return

      this.loading = true
      try {
        const res = await getCommunityPosts({
          type: this.currentTab,
          page: this.page,
          size: 10
        })
        if (res.code === 0) {
          const list = res.data || []
          if (reset) this.posts = list
          else this.posts = [...this.posts, ...list]
          
          if (list.length < 10) this.noMore = true
          else this.page++
        }
      } catch (e) {
        uni.showToast({ title: '网络异常', icon: 'none' })
      } finally {
        this.loading = false
        this.refreshing = false
      }
    },

    switchTab(val) {
      if (this.currentTab === val) return
      this.currentTab = val
      this.loadData(true)
    },

    onRefresh() {
      this.refreshing = true
      this.loadData(true)
    },

    onReachBottom() {
      this.loadData()
    },

    getTypeLabel(type) {
      const map = {
        'DEMAND_URGENT': '求助',
        'RENTAL': '租赁',
        'CHAT': '讨论'
      }
      return map[type] || '其他'
    },

    truncateContent(text) {
      if (!text) return ''
      return text.length > 50 ? text.substring(0, 50) + '...' : text
    },

    formatTime(time) {
      if (!time) return ''
      // 简单模拟时间格式化，实际建议用 dayjs
      const date = new Date(time)
      const now = new Date()
      const diff = (now - date) / 1000
      if (diff < 60) return '刚刚'
      if (diff < 3600) return Math.floor(diff/60) + '分钟前'
      if (diff < 86400) return Math.floor(diff/3600) + '小时前'
      return date.getMonth() + 1 + '-' + date.getDate()
    },

    goDetail(id) {
      uni.navigateTo({ url: `/pages/community/post-detail?id=${id}` })
    },

    goPublish() {
      uni.navigateTo({ url: `/pages/community/post-publish?type=${this.currentTab}` })
    },

    onSearch() {
      uni.showToast({ title: '搜索功能建设中', icon: 'none' })
    }
  }
}
</script>

<style scoped>
.community-page {
  background: #F5F9F6;
  height: 100vh;
  display: flex;
  flex-direction: column;
}

.nav-header {
  padding: 20rpx 32rpx;
  background: linear-gradient(to bottom, #2D7A4F, #2D7A4F); /* 保持与整体绿色调一致 */
}

.search-bar {
  display: flex;
  align-items: center;
  background: #fff;
  border-radius: 40rpx;
  padding: 16rpx 32rpx;
  gap: 16rpx;
}

.search-input {
  flex: 1;
  font-size: 28rpx;
  color: #1A3A28;
}

/* 分类 Tab */
.type-tabs {
  display: flex;
  background: #fff;
  padding: 0 20rpx;
  border-bottom: 1rpx solid #EAF5EE;
}

.tab-item {
  flex: 1;
  padding: 24rpx 0;
  display: flex;
  flex-direction: column;
  align-items: center;
  position: relative;
}

.tab-label {
  font-size: 30rpx;
  color: #6B8F7A;
  font-weight: 700;
}

.tab-item.active .tab-label {
  color: #2D7A4F;
  font-size: 32rpx;
}

.tab-line {
  position: absolute;
  bottom: 0;
  width: 40rpx;
  height: 6rpx;
  background: #2D7A4F;
  border-radius: 3rpx;
}

/* 帖子列表 */
.post-list {
  flex: 1;
  overflow: hidden;
}

.list-content {
  padding: 24rpx;
}

.post-card {
  background: #fff;
  border-radius: 20rpx;
  padding: 32rpx;
  margin-bottom: 24rpx;
  box-shadow: 0 4rpx 20rpx rgba(45,122,79,0.06);
  position: relative;
  overflow: hidden;
  border: 1rpx solid #EAF5EE;
}

.post-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20rpx;
}

.type-badge {
  font-size: 22rpx;
  font-weight: 700;
  padding: 6rpx 20rpx;
  border-radius: 24rpx;
}

.type-badge.demand_urgent { background: #fee2e2; color: #ef4444; }
.type-badge.rental { background: #dcfce7; color: #2D7A4F; }
.type-badge.chat { background: #fef9c3; color: #ca8a04; }

.post-time {
  font-size: 24rpx;
  color: #A0C4B5;
}

.post-body {
  margin-bottom: 24rpx;
}

.post-title {
  display: block;
  font-size: 36rpx;
  font-weight: 800;
  color: #1A3A28;
  margin-bottom: 12rpx;
  line-height: 1.4;
}

.post-desc {
  display: block;
  font-size: 28rpx;
  color: #5BA88A;
  line-height: 1.6;
}

.post-foot {
  display: flex;
  justify-content: space-between;
  align-items: center;
  border-top: 2rpx solid #F5F9F6;
  padding-top: 20rpx;
}

.location-wrap {
  display: flex;
  align-items: center;
  gap: 8rpx;
}

.location-icon { font-size: 26rpx; }
.location-text { font-size: 26rpx; color: #6B8F7A; }

.stats-wrap {
  display: flex;
  gap: 24rpx;
}

.stat-item {
  font-size: 24rpx;
  color: #A0C4B5;
}

/* 急单角标 */
.urgent-ribbon {
  position: absolute;
  top: 0;
  right: 0;
  background: #ef4444;
  padding: 0 40rpx;
  transform: rotate(45deg) translate(30%, -50%);
}
.urgent-text {
  font-size: 18rpx;
  color: #fff;
  font-weight: 700;
}

.load-more {
  padding: 40rpx 0;
  text-align: center;
}
.loading-text {
  font-size: 26rpx;
  color: #A0C4B5;
}

/* 悬浮发布按钮 */
.fab-btn {
  position: fixed;
  right: 40rpx;
  bottom: 60rpx;
  width: 120rpx;
  height: 120rpx;
  background: linear-gradient(135deg, #5BA88A, #2D7A4F);
  border-radius: 60rpx;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  box-shadow: 0 8rpx 32rpx rgba(45,122,79,0.3);
  z-index: 100;
}

.fab-icon {
  color: #fff;
  font-size: 48rpx;
  font-weight: 400;
  line-height: 1;
}

.fab-text {
  color: #fff;
  font-size: 22rpx;
  font-weight: 700;
}
</style>
