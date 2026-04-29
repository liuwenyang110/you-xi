<template>
  <view class="claims-page">
    <!-- 顶部统计 -->
    <view class="hero-stats">
      <view class="hero-bg"></view>
      <view class="stats-content">
        <text class="stats-title">我的志愿帮扶</text>
        <view class="stats-row">
          <view class="stat-item">
            <text class="stat-val">{{ stats.running }}</text>
            <text class="stat-label">进行中</text>
          </view>
          <view class="stat-item">
            <text class="stat-val">{{ stats.finished }}</text>
            <text class="stat-label">已完成</text>
          </view>
          <view class="stat-item">
            <text class="stat-val">{{ stats.rating }}</text>
            <text class="stat-label">平均评分</text>
          </view>
        </view>
      </view>
    </view>

    <!-- 列表区 -->
    <scroll-view class="list-scroll" scroll-y>
      <view class="list-container">
        <view v-if="claims.length === 0" class="empty-state">
          <icon type="info" size="64" color="#EAF5EE" />
          <text class="empty-text">暂无认领中的需求</text>
          <button class="go-btn" @click="goSquare">去社区广场看看</button>
        </view>

        <view v-for="item in claims" :key="item.id" class="claim-card">
          <view class="card-header">
            <text class="status-tag" :class="item.status.toLowerCase()">
              {{ getStatusLabel(item.status) }}
            </text>
            <text class="time">{{ formatTime(item.claimedAt) }}</text>
          </view>

          <view class="card-body">
            <text class="demand-title">需求：{{ item.demandTitle || '正在帮扶的需求' }}</text>
            <view class="loc-info">
              <text class="loc-icon">📍</text>
              <text class="loc-text">{{ item.locationName || '目的地已在详情中确认' }}</text>
            </view>
          </view>

          <!-- 操作按钮：根据状态显示 -->
          <view class="card-footer" v-if="['CLAIMED', 'EN_ROUTE', 'WORKING'].includes(item.status)">
            <button 
              v-if="item.status === 'CLAIMED'" 
              class="action-btn start-btn" 
              @click="updateStatus(item.id, 'EN_ROUTE')"
            >
              我现在出发
            </button>
            <button 
              v-if="item.status === 'EN_ROUTE'" 
              class="action-btn work-btn" 
              @click="updateStatus(item.id, 'WORKING')"
            >
              我已到达，开始作业
            </button>
            <button 
              v-if="item.status === 'WORKING'" 
              class="action-btn finish-btn" 
              @click="updateStatus(item.id, 'FINISHED')"
            >
              作业完成
            </button>
            <button class="cancel-btn" @click="updateStatus(item.id, 'CANCELLED')">取消认领</button>
          </view>
          
          <!-- 已完成显示反馈 -->
          <view class="card-footer feedback-view" v-if="item.status === 'FINISHED' && item.farmerRating">
            <text class="rating-stars">农户评分：{{ '★'.repeat(item.farmerRating) }}</text>
            <text class="feedback-text" v-if="item.farmerFeedback">"{{ item.farmerFeedback }}"</text>
          </view>
        </view>
      </view>
    </scroll-view>
  </view>
</template>

<script>
import { getVolunteerHistory, updateClaimStatus } from '@/api/v3/volunteer'

export default {
  data() {
    return {
      userId: 1001, // 模拟
      claims: [],
      stats: {
        running: 0,
        finished: 0,
        rating: '5.0'
      }
    }
  },
  onShow() {
    this.loadHistory()
  },
  methods: {
    async loadHistory() {
      try {
        const res = await getVolunteerHistory(this.userId)
        if (res.code === 0) {
          const list = res.data || []
          this.claims = list
          this.stats.running = list.filter(i => ['CLAIMED', 'EN_ROUTE', 'WORKING'].includes(i.status)).length
          this.stats.finished = list.filter(i => i.status === 'FINISHED').length
          
          const rated = list.filter(i => i.farmerRating)
          if (rated.length > 0) {
            const avg = rated.reduce((acc, cur) => acc + cur.farmerRating, 0) / rated.length
            this.stats.rating = avg.toFixed(1)
          }
        }
      } catch {
        // 使用模拟数据
        this.mockData()
      }
    },

    mockData() {
      this.claims = [
        { id: 1, status: 'CLAIMED', claimedAt: new Date(), demandTitle: '马湾村小麦急收', locationName: '盆尧镇马湾村东' },
        { id: 2, status: 'FINISHED', claimedAt: new Date(), demandTitle: '无人机打药', locationName: '西平县城郊', farmerRating: 5, farmerFeedback: '来得很快，干活利索' }
      ]
      this.stats.running = 1
      this.stats.finished = 1
    },

    getStatusLabel(status) {
      const map = {
        'CLAIMED': '待出发',
        'EN_ROUTE': '赶往途中',
        'WORKING': '作业中',
        'FINISHED': '帮扶礼成',
        'CANCELLED': '已取消'
      }
      return map[status] || status
    },

    formatTime(time) {
      if (!time) return ''
      return new Date(time).toLocaleDateString()
    },

    async updateStatus(id, newStatus) {
      const titles = {
        'EN_ROUTE': '准备出发',
        'WORKING': '确认开始作业',
        'FINISHED': '确认作业已完成',
        'CANCELLED': '取消认领'
      }
      
      uni.showModal({
        title: '状态变更',
        content: `确定要标记为 [${this.getStatusLabel(newStatus)}] 吗？`,
        success: async (res) => {
          if (res.confirm) {
            try {
              const r = await updateClaimStatus(id, newStatus)
              if (r.code === 0) {
                uni.showToast({ title: '更新成功', icon: 'success' })
                this.loadHistory()
              }
            } catch {
              uni.showToast({ title: '网络失败', icon: 'none' })
            }
          }
        }
      })
    },

    goSquare() {
      uni.navigateTo({ url: '/pages/community/index' })
    }
  }
}
</script>

<style scoped>
.claims-page {
  background: #F5F9F6;
  height: 100vh;
  display: flex;
  flex-direction: column;
}

.hero-stats {
  position: relative;
  height: 380rpx;
  background: linear-gradient(135deg, #1A4A30, #2D7A4F);
  padding: 80rpx 40rpx;
  color: #fff;
}

.stats-title {
  display: block;
  font-size: 42rpx;
  font-weight: 800;
  margin-bottom: 60rpx;
}

.stats-row {
  display: flex;
  justify-content: space-around;
  background: rgba(255,255,255,0.1);
  border-radius: 24rpx;
  padding: 32rpx;
  backdrop-filter: blur(10px);
}

.stat-item {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.stat-val {
  font-size: 44rpx;
  font-weight: 900;
  color: #A8F0C8;
}

.stat-label {
  font-size: 22rpx;
  color: rgba(255,255,255,0.7);
  margin-top: 8rpx;
}

/* 列表区 */
.list-scroll {
  flex: 1;
  margin-top: -40rpx;
}

.list-container {
  padding: 0 32rpx 60rpx;
}

.claim-card {
  background: #fff;
  border-radius: 24rpx;
  padding: 32rpx;
  margin-bottom: 24rpx;
  box-shadow: 0 4rpx 20rpx rgba(45,122,79,0.06);
  border: 1rpx solid #EAF5EE;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24rpx;
}

.status-tag {
  font-size: 24rpx;
  font-weight: 700;
  padding: 6rpx 20rpx;
  border-radius: 20rpx;
}
.status-tag.claimed { background: #EAF5EE; color: #2D7A4F; }
.status-tag.en_route { background: #EBF3FD; color: #2D8CFF; }
.status-tag.working { background: #FFF7E6; color: #FA8C16; }
.status-tag.finished { background: #F6FFED; color: #52C41A; }
.status-tag.cancelled { background: #F5F5F5; color: #999; }

.time {
  font-size: 24rpx;
  color: #A0C4B5;
}

.card-body {
  margin-bottom: 32rpx;
}

.demand-title {
  display: block;
  font-size: 34rpx;
  font-weight: 700;
  color: #1A3A28;
  margin-bottom: 12rpx;
}

.loc-info {
  display: flex;
  align-items: center;
  gap: 8rpx;
}
.loc-icon { font-size: 24rpx; }
.loc-text { font-size: 26rpx; color: #6B8F7A; }

/* 底部操作 */
.card-footer {
  display: flex;
  gap: 16rpx;
  border-top: 2rpx solid #F5F9F6;
  padding-top: 32rpx;
}

.action-btn {
  flex: 2;
  height: 80rpx;
  border-radius: 40rpx;
  font-size: 28rpx;
  font-weight: 700;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  border: none;
}
.start-btn { background: #5BA88A; }
.work-btn { background: #FA8C16; }
.finish-btn { background: #2D7A4F; }

.cancel-btn {
  flex: 1;
  height: 80rpx;
  border-radius: 40rpx;
  font-size: 26rpx;
  background: #F5F5F5;
  color: #999;
  border: none;
}

/* 反馈视图 */
.feedback-view {
  flex-direction: column;
  align-items: flex-start;
  gap: 8rpx;
}
.rating-stars {
  font-size: 28rpx;
  color: #FAAD14;
  font-weight: 700;
}
.feedback-text {
  font-size: 26rpx;
  color: #5BA88A;
  font-style: italic;
}

.empty-state {
  padding: 120rpx 0;
  display: flex;
  flex-direction: column;
  align-items: center;
}
.empty-text {
  font-size: 28rpx;
  color: #A0C4B5;
  margin: 32rpx 0 48rpx;
}
.go-btn {
  background: #2D7A4F;
  color: #fff;
  border-radius: 40rpx;
  padding: 0 60rpx;
  font-size: 28rpx;
}
</style>
