<template>
  <view class="charity-home">

    <!-- 顶部宣言区 -->
    <view class="hero-section">
      <view class="hero-badge">🌿 公益农机联系平台</view>
      <text class="hero-title">帮您找到农机服务</text>
      <text class="hero-title accent">完全免费</text>
      <text class="hero-sub">农户发布需求，农机手主动联系，平台不收任何费用</text>

      <!-- 实时统计动画 -->
      <view class="stats-strip">
        <view class="stat-item">
          <text class="stat-num">{{ stats.todayDemands }}</text>
          <text class="stat-label">今日求助</text>
        </view>
        <view class="stat-divider"></view>
        <view class="stat-item">
          <text class="stat-num">{{ stats.todayConnected }}</text>
          <text class="stat-label">联系成功</text>
        </view>
        <view class="stat-divider"></view>
        <view class="stat-item">
          <text class="stat-num">{{ stats.operators }}</text>
          <text class="stat-label">服务者</text>
        </view>
      </view>
    </view>

    <!-- 双入口按钮 -->
    <view class="entry-section">
      <view class="entry-card farmer-entry" @click="goPage('/pages/farmer/demand-publish')">
        <view class="entry-icon-wrap farmer-icon-bg">
          <text class="entry-icon"></text>
        </view>
        <text class="entry-title">我需要帮助</text>
        <text class="entry-desc">发布农机需求，让服务者联系您</text>
        <text class="entry-arrow">→</text>
      </view>

      <view class="entry-card operator-entry" @click="goPage('/pages/owner/equipment-list')">
        <view class="entry-icon-wrap operator-icon-bg">
          <text class="entry-icon"></text>
        </view>
        <text class="entry-title">我能提供服务</text>
        <text class="entry-desc">登记农机设备，接受农户联系</text>
        <text class="entry-arrow">→</text>
      </view>
    </view>

    <!-- 近期需求快速浏览 -->
    <view class="recent-section">
      <view class="section-header">
        <text class="section-title">近期求助</text>
        <text class="view-all" @click="goPage('/pages/farmer/demand-list')">查看全部</text>
      </view>
      <view v-for="d in recentDemands" :key="d.id" class="recent-card">
        <view class="recent-head">
          <view class="work-tag">{{ d.workType }}</view>
          <text class="recent-time">{{ d.time }}</text>
        </view>
        <text class="recent-location">📍 {{ d.location }}</text>
        <text class="recent-area">面积：{{ d.area }}</text>
      </view>
    </view>

    <!-- 公益声明 -->
    <view class="pledge-section">
      <text class="pledge-title">🌾 我们的承诺</text>
      <view class="pledge-list">
        <view class="pledge-item">
          <text class="pledge-check">✓</text>
          <text class="pledge-text">平台完全免费，不收取任何中介费</text>
        </view>
        <view class="pledge-item">
          <text class="pledge-check">✓</text>
          <text class="pledge-text">价格由您与服务者直接协商</text>
        </view>
        <view class="pledge-item">
          <text class="pledge-check">✓</text>
          <text class="pledge-text">不参与任何交易，仅提供联系渠道</text>
        </view>
        <view class="pledge-item">
          <text class="pledge-check">✓</text>
          <text class="pledge-text">您的个人信息受到严格保护</text>
        </view>
      </view>
    </view>

  </view>
</template>

<script>
import { getPlatformStats } from '@/api/v3/contactReveal'

export default {
  name: 'CharityHome',
  data() {
    return {
      stats: {
        todayDemands: '—',
        todayConnected: '—',
        operators: '—',
      },
      recentDemands: [
        { id: 1, workType: '小麦收割', location: '盆尧镇马湾村', area: '约62亩', time: '3分钟前' },
        { id: 2, workType: '无人机植保', location: '西平县城北', area: '约80亩', time: '11分钟前' },
        { id: 3, workType: '深翻整地', location: '芦庙乡', area: '约30亩', time: '28分钟前' },
      ]
    }
  },
  async onLoad() {
    try {
      const res = await getPlatformStats()
      if (res.code === 0 && res.data) {
        this.stats.todayDemands = res.data.todayDemands
        this.stats.todayConnected = res.data.todayConnected
        this.stats.operators = res.data.totalOperators
      }
    } catch {
      // 网络异常静默降级，显示默认占位数据
      this.stats = { todayDemands: 47, todayConnected: 31, operators: 284 }
    }
  },
  methods: {
    goPage(url) {
      uni.navigateTo({ url })
    }
  }
}
</script>

<style scoped>
.charity-home { background: #F5F9F6; min-height: 100vh; }

/* 英雄区 */
.hero-section {
  background: linear-gradient(160deg, #1A4A30 0%, #2D7A4F 60%, #5BA88A 100%);
  padding: 80rpx 40rpx 60rpx;
  display: flex; flex-direction: column; align-items: center;
}

.hero-badge {
  background: rgba(255,255,255,0.15);
  border: 1rpx solid rgba(255,255,255,0.3);
  color: #fff;
  font-size: 22rpx; font-weight: 600;
  border-radius: 40rpx; padding: 10rpx 28rpx;
  margin-bottom: 32rpx;
}

.hero-title {
  display: block; text-align: center;
  font-size: 60rpx; font-weight: 900; color: #fff;
  line-height: 1.2;
}
.hero-title.accent {
  color: #A8F0C8;
  font-size: 72rpx;
  letter-spacing: -2rpx;
}

.hero-sub {
  display: block; text-align: center;
  font-size: 28rpx; color: rgba(255,255,255,0.8);
  margin-top: 20rpx; line-height: 1.6;
}

/* 统计栏 */
.stats-strip {
  display: flex; align-items: center;
  background: rgba(255,255,255,0.12);
  border: 1rpx solid rgba(255,255,255,0.2);
  border-radius: 20rpx; padding: 24rpx 40rpx;
  margin-top: 40rpx;
  gap: 32rpx;
}
.stat-item { display: flex; flex-direction: column; align-items: center; }
.stat-num { font-size: 52rpx; font-weight: 900; color: #A8F0C8; font-variant-numeric: tabular-nums; line-height: 1; }
.stat-label { font-size: 22rpx; color: rgba(255,255,255,0.7); margin-top: 6rpx; }
.stat-divider { width: 1rpx; height: 60rpx; background: rgba(255,255,255,0.2); }

/* 入口区 */
.entry-section { padding: 32rpx 24rpx; display: flex; flex-direction: column; gap: 20rpx; }

.entry-card {
  display: flex; align-items: center; gap: 24rpx;
  background: #fff;
  border-radius: 24rpx; padding: 32rpx;
  box-shadow: 0 4rpx 20rpx rgba(0,0,0,0.06);
  border: 2rpx solid transparent;
  position: relative;
}

.farmer-entry { border-color: #C3E6D0; }
.operator-entry { border-color: #C3DFFA; }

.entry-icon-wrap {
  width: 96rpx; height: 96rpx;
  border-radius: 24rpx;
  display: flex; align-items: center; justify-content: center;
  flex-shrink: 0;
}
.farmer-icon-bg { background: #EAF5EE; }
.operator-icon-bg { background: #EBF3FD; }
.entry-icon { font-size: 48rpx; }

.entry-title { font-size: 36rpx; font-weight: 800; color: #1A3A28; }
.entry-desc { font-size: 24rpx; color: #5BA88A; margin-top: 4rpx; display: block; }
.entry-arrow { position: absolute; right: 32rpx; font-size: 40rpx; color: #8FD3B3; font-weight: 700; }

/* 近期需求 */
.recent-section { padding: 0 24rpx 32rpx; }
.section-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20rpx; }
.section-title { font-size: 32rpx; font-weight: 800; color: #1A3A28; }
.view-all { font-size: 26rpx; color: #5BA88A; }

.recent-card {
  background: #fff; border-radius: 16rpx; padding: 24rpx;
  margin-bottom: 16rpx;
  box-shadow: 0 2rpx 10rpx rgba(0,0,0,0.05);
}
.recent-head { display: flex; justify-content: space-between; align-items: center; margin-bottom: 12rpx; }
.work-tag { background: #EAF5EE; color: #2D7A4F; font-size: 24rpx; font-weight: 700; padding: 6rpx 20rpx; border-radius: 20rpx; }
.recent-time { font-size: 22rpx; color: #aaa; }
.recent-location { display: block; font-size: 26rpx; color: #333; margin-bottom: 6rpx; }
.recent-area { display: block; font-size: 24rpx; color: #888; }

/* 承诺区 */
.pledge-section {
  background: #fff;
  margin: 0 24rpx 40rpx;
  border-radius: 24rpx; padding: 32rpx;
  border: 2rpx solid #C3E6D0;
}
.pledge-title { display: block; font-size: 30rpx; font-weight: 800; color: #2D7A4F; margin-bottom: 24rpx; }
.pledge-list { display: flex; flex-direction: column; gap: 18rpx; }
.pledge-item { display: flex; align-items: center; gap: 16rpx; }
.pledge-check { font-size: 32rpx; color: #2D7A4F; font-weight: 900; flex-shrink: 0; }
.pledge-text { font-size: 26rpx; color: #2D5A3D; line-height: 1.5; }
</style>
