<template>
  <view class="detail-page">
    <view v-if="loading" class="loading-state">
      <text>加载中...</text>
    </view>
    
    <view v-else-if="!post" class="empty-state">
      <text>帖子不存在或已删除</text>
    </view>

    <scroll-view v-else class="content-scroll" scroll-y>
      <!-- 文章主体 -->
      <view class="post-main">
        <view class="post-header">
          <view class="user-info">
            <view class="avatar">{{ post.authorName ? post.authorName[0] : '农' }}</view>
            <view class="info-text">
              <text class="username">{{ post.authorName || '匿名用户' }}</text>
              <text class="time">{{ formatTime(post.createdAt) }}</text>
            </view>
          </view>
          <view class="type-tag" :class="post.postType.toLowerCase()">
            {{ getTypeLabel(post.postType) }}
          </view>
        </view>

        <text class="post-title">{{ post.title }}</text>
        <rich-text class="post-content" :nodes="post.content"></rich-text>

        <!-- 地点信息卡片 -->
        <view class="location-card" v-if="post.locationName">
          <text class="loc-icon">📍</text>
          <view class="loc-info">
            <text class="loc-name">{{ post.locationName }}</text>
            <text class="loc-sub">位置真实有效 · 请放心查看</text>
          </view>
        </view>

        <!-- 公益提示 -->
        <view class="charity-alert">
          <text class="alert-icon">🛡️</text>
          <text class="alert-text">
            农助手友情提醒：本信息由用户发布，平台已对真实性进行基础审核。
            私下沟通时请注意安全，涉及资金往来请多加小心。
          </text>
        </view>
      </view>

      <!-- 底部撑开 -->
      <view class="footer-spacer"></view>
    </scroll-view>

    <!-- 底部操作栏 -->
    <view class="action-bar" v-if="post">
      <view class="secondary-actions">
        <view class="action-item">
          <text class="action-icon">💬</text>
          <text class="action-label">咨询</text>
        </view>
      </view>

      <!-- 核心动作：如果是急单，显示认领；如果是租赁，显示联系 -->
      <button 
        v-if="post.postType === 'DEMAND_URGENT'" 
        class="primary-btn claim-btn"
        :loading="claiming"
        @click="showClaimModal"
      >
        我要认领帮扶
      </button>
      <button 
        v-else-if="post.postType === 'RENTAL'" 
        class="primary-btn rental-btn"
        @click="contactOwner"
      >
        联系机主
      </button>
      <button 
        v-else 
        class="primary-btn normal-btn"
        @click="replyPost"
      >
        参与讨论
      </button>
    </view>

    <!-- 认领确认弹窗 -->
    <uni-popup ref="claimPopup" type="bottom">
      <view class="claim-panel">
        <text class="panel-title">确认认领该需求？</text>
        <text class="panel-desc">
          认领后您即成为该需求的志愿者，请尽快出发前往目的地。
          平台将记录您的公益行径，感谢您的热心！
        </text>
        
        <!-- 设备选择（模拟，实际应从设备列表拉取） -->
        <view class="equipment-select">
          <text class="select-label">选择出动设备：</text>
          <view class="eq-chip active">默认主用机器</view>
        </view>

        <view class="panel-footer">
          <button class="cancel-btn" @click="$refs.claimPopup.close()">取消</button>
          <button class="confirm-btn" :loading="claiming" @click="confirmClaim">确认认领</button>
        </view>
      </view>
    </uni-popup>
  </view>
</template>

<script>
import { getCommunityPostDetail } from '@/api/v3/community'
import { claimDemand } from '@/api/v3/volunteer'

export default {
  data() {
    return {
      post: null,
      loading: true,
      claiming: false,
      userId: 1001, // 模拟当前登录用户
    }
  },
  onLoad(options) {
    if (options.id) {
      this.loadDetail(options.id)
    }
  },
  methods: {
    async loadDetail(id) {
      this.loading = true
      try {
        const res = await getCommunityPostDetail(id)
        if (res.code === 0) {
          this.post = res.data
        }
      } catch {
        uni.showToast({ title: '加载失败', icon: 'none' })
      } finally {
        this.loading = false
      }
    },

    getTypeLabel(type) {
      const map = {
        'DEMAND_URGENT': '求助急单',
        'RENTAL': '农机租赁',
        'CHAT': '农技讨论'
      }
      return map[type] || '其他'
    },

    formatTime(time) {
      if (!time) return ''
      return new Date(time).toLocaleDateString()
    },

    showClaimModal() {
      this.$refs.claimPopup.open()
    },

    async confirmClaim() {
      if (this.claiming) return
      this.claiming = true
      try {
        const res = await claimDemand({
          demandId: this.post.id, // 这里逻辑上应该是帖子关联的需求ID，暂用帖子ID
          volunteerId: this.userId,
          equipmentId: 1 // 模拟
        })
        if (res.code === 0) {
          uni.showToast({ title: '认领成功！感谢您的爱心', icon: 'success' })
          this.$refs.claimPopup.close()
          // 跳转到我的认领页面
          setTimeout(() => {
            uni.navigateTo({ url: '/pages/volunteer/my-claims' })
          }, 1500)
        } else {
          uni.showToast({ title: res.message || '认领失败', icon: 'none' })
        }
      } catch {
        uni.showToast({ title: '网络异常', icon: 'none' })
      } finally {
        this.claiming = false
      }
    },

    contactOwner() {
      uni.showModal({
        title: '温馨提示',
        content: '联系方式：138****8888 (公益平台模拟)',
        showCancel: false
      })
    },

    replyPost() {
      uni.showToast({ title: '回复功能建设中', icon: 'none' })
    }
  }
}
</script>

<style scoped>
.detail-page {
  background: #fff;
  height: 100vh;
  display: flex;
  flex-direction: column;
}

.content-scroll {
  flex: 1;
}

.post-main {
  padding: 40rpx 32rpx;
}

.post-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 40rpx;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 20rpx;
}

.avatar {
  width: 80rpx;
  height: 80rpx;
  background: #EAF5EE;
  color: #2D7A4F;
  border-radius: 40rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 700;
  font-size: 32rpx;
}

.username {
  display: block;
  font-size: 30rpx;
  font-weight: 700;
  color: #1A3A28;
}

.time {
  font-size: 24rpx;
  color: #A0C4B5;
}

.type-tag {
  font-size: 24rpx;
  padding: 8rpx 24rpx;
  border-radius: 30rpx;
  font-weight: 700;
}
.type-tag.demand_urgent { background: #fee2e2; color: #ef4444; }
.type-tag.rental { background: #dcfce7; color: #2D7A4F; }
.type-tag.chat { background: #fef9c3; color: #ca8a04; }

.post-title {
  display: block;
  font-size: 44rpx;
  font-weight: 900;
  color: #1A3A28;
  line-height: 1.4;
  margin-bottom: 32rpx;
}

.post-content {
  font-size: 32rpx;
  color: #333;
  line-height: 1.8;
  margin-bottom: 48rpx;
}

/* 地点卡片 */
.location-card {
  background: #F5F9F6;
  border-radius: 20rpx;
  padding: 24rpx 32rpx;
  display: flex;
  align-items: center;
  gap: 20rpx;
  margin-bottom: 40rpx;
  border: 1rpx solid #EAF5EE;
}
.loc-icon { font-size: 40rpx; }
.loc-name { display: block; font-size: 30rpx; color: #1A3A28; font-weight: 700; }
.loc-sub { font-size: 22rpx; color: #6B8F7A; }

/* 公益警示 */
.charity-alert {
  background: rgba(45,122,79,0.05);
  border-radius: 16rpx;
  padding: 24rpx;
  display: flex;
  gap: 16rpx;
}
.alert-icon { font-size: 32rpx; }
.alert-text { font-size: 24rpx; color: #5BA88A; line-height: 1.6; }

.footer-spacer { height: 160rpx; }

/* 底部操作栏 */
.action-bar {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  background: #fff;
  padding: 20rpx 32rpx 48rpx;
  display: flex;
  align-items: center;
  gap: 24rpx;
  box-shadow: 0 -4rpx 20rpx rgba(0,0,0,0.05);
  border-top: 1rpx solid #F5F9F6;
}

.secondary-actions {
  display: flex;
  gap: 32rpx;
}
.action-item {
  display: flex;
  flex-direction: column;
  align-items: center;
}
.action-icon { font-size: 40rpx; color: #2D7A4F; }
.action-label { font-size: 22rpx; color: #2D7A4F; font-weight: 600; }

.primary-btn {
  flex: 1;
  height: 96rpx;
  border-radius: 48rpx;
  font-size: 32rpx;
  font-weight: 800;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  border: none;
}
.claim-btn { background: #ef4444; box-shadow: 0 4rpx 16rpx rgba(239,68,68,0.3); }
.rental-btn { background: #2D7A4F; box-shadow: 0 4rpx 16rpx rgba(45,122,79,0.3); }
.normal-btn { background: #5BA88A; }

/* 认领面板 */
.claim-panel {
  background: #fff;
  border-radius: 40rpx 40rpx 0 0;
  padding: 48rpx 32rpx 64rpx;
}
.panel-title {
  display: block;
  font-size: 38rpx;
  font-weight: 900;
  color: #1A3A28;
  margin-bottom: 24rpx;
  text-align: center;
}
.panel-desc {
  display: block;
  font-size: 28rpx;
  color: #5BA88A;
  line-height: 1.6;
  margin-bottom: 40rpx;
  text-align: center;
}
.equipment-select {
  background: #F5F9F6;
  border-radius: 20rpx;
  padding: 32rpx;
  margin-bottom: 48rpx;
}
.select-label {
  display: block;
  font-size: 28rpx;
  color: #1A3A28;
  margin-bottom: 20rpx;
  font-weight: 700;
}
.eq-chip {
  display: inline-block;
  padding: 12rpx 32rpx;
  background: #fff;
  border: 2rpx solid #C3E6D0;
  border-radius: 40rpx;
  color: #2D7A4F;
  font-size: 28rpx;
  font-weight: 600;
}
.eq-chip.active {
  background: #2D7A4F;
  color: #fff;
  border-color: #2D7A4F;
}

.panel-footer {
  display: flex;
  gap: 24rpx;
}
.cancel-btn {
  flex: 1;
  background: #F5F9F6;
  color: #6B8F7A;
  border: none;
  font-weight: 700;
}
.confirm-btn {
  flex: 2;
  background: #2D7A4F;
  color: #fff;
  border: none;
  font-weight: 800;
}
</style>
