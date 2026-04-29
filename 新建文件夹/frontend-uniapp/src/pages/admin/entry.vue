<template>
  <view class="page admin-page">
    <!-- 顶部身份卡 -->
    <view class="card card-glass scene-enter">
      <view class="admin-header">
        <view class="admin-avatar">🛡️</view>
        <view class="admin-info">
          <view class="admin-name">管理员控制台</view>
          <view class="admin-id">ID: {{ uiStore.userId || '10003' }} · {{ uiStore.mode === 'ELDER' ? '老年模式' : '正常模式' }}</view>
        </view>
      </view>
    </view>

    <!-- 影响力数据看板 -->
    <view class="card scene-enter delay-1">
      <view class="section-head">
        <view>
          <view class="section-title">📊 社会影响力看板</view>
          <view class="section-subtitle">平台公益数据实时统计</view>
        </view>
        <view class="section-link" @click="refreshStats">刷新</view>
      </view>

      <view class="stats-mega-grid">
        <view class="mega-stat mega-stat-primary">
          <view class="mega-stat-value">{{ stats.totalFarmers }}</view>
          <view class="mega-stat-label">累计服务农户</view>
          <view class="mega-stat-trend">↑ {{ stats.newFarmersToday }} 今日新增</view>
        </view>
        <view class="mega-stat mega-stat-gold">
          <view class="mega-stat-value">{{ stats.totalOrders }}</view>
          <view class="mega-stat-label">完成对接</view>
          <view class="mega-stat-trend">↑ {{ stats.newOrdersToday }} 今日</view>
        </view>
      </view>

      <view class="stats-detail-grid">
        <view class="detail-stat">
          <view class="detail-stat-value">{{ stats.totalAreaMu }}</view>
          <view class="detail-stat-label">累计作业亩数</view>
        </view>
        <view class="detail-stat">
          <view class="detail-stat-value">{{ stats.avgResponseMinutes }}min</view>
          <view class="detail-stat-label">平均响应时间</view>
        </view>
        <view class="detail-stat">
          <view class="detail-stat-value">{{ stats.disputeRate }}%</view>
          <view class="detail-stat-label">争议率</view>
        </view>
        <view class="detail-stat">
          <view class="detail-stat-value">{{ stats.totalVillages }}</view>
          <view class="detail-stat-label">覆盖村庄</view>
        </view>
      </view>

      <!-- 信号质量分布 -->
      <view class="quality-section">
        <view class="quality-title">GPS 信号质量分布</view>
        <view class="quality-bars">
          <view class="quality-bar">
            <view class="quality-bar-label">良好</view>
            <view class="quality-bar-track">
              <view class="quality-bar-fill quality-good" :style="{ width: stats.signalGoodPct + '%' }"></view>
            </view>
            <view class="quality-bar-pct">{{ stats.signalGoodPct }}%</view>
          </view>
          <view class="quality-bar">
            <view class="quality-bar-label">一般</view>
            <view class="quality-bar-track">
              <view class="quality-bar-fill quality-fair" :style="{ width: stats.signalFairPct + '%' }"></view>
            </view>
            <view class="quality-bar-pct">{{ stats.signalFairPct }}%</view>
          </view>
          <view class="quality-bar">
            <view class="quality-bar-label">较差</view>
            <view class="quality-bar-track">
              <view class="quality-bar-fill quality-poor" :style="{ width: stats.signalPoorPct + '%' }"></view>
            </view>
            <view class="quality-bar-pct">{{ stats.signalPoorPct }}%</view>
          </view>
        </view>
      </view>
    </view>

    <!-- 轨迹争议审核 -->
    <view class="card scene-enter delay-2">
      <view class="section-head">
        <view>
          <view class="section-title">⚠️ 轨迹争议审核</view>
          <view class="section-subtitle">面积报告有争议的订单</view>
        </view>
        <view class="dispute-badge" v-if="disputes.length > 0">{{ disputes.length }} 条</view>
      </view>

      <view v-if="disputes.length === 0" class="empty-block">
        <view class="empty-title">暂无争议工单 ✅</view>
        <view class="empty-desc">所有面积报告目前均已确认或未被质疑。</view>
      </view>

      <view v-else class="dispute-list">
        <view
          v-for="item in disputes"
          :key="item.id"
          class="dispute-item"
          @click="openDispute(item)"
        >
          <view class="dispute-main">
            <view class="dispute-title-row">
              <view class="dispute-name">订单 #{{ item.orderId }}</view>
              <view class="status-tag status-warn">待审核</view>
            </view>
            <view class="dispute-meta">
              农户预估 {{ item.farmerEstimate }} 亩 · GPS 计算 {{ item.gpsArea }} 亩 · 偏差 {{ item.deviation }}%
            </view>
            <view class="dispute-desc">{{ item.reason || '农户对面积计算结果有异议' }}</view>
          </view>
          <view class="dispute-actions">
            <view class="dispute-action-btn action-pass" @click.stop="resolveDispute(item.id, 'PASS')">✓ 采信</view>
            <view class="dispute-action-btn action-reject" @click.stop="resolveDispute(item.id, 'REJECT')">✗ 驳回</view>
          </view>
        </view>
      </view>
    </view>

    <!-- 快捷操作 -->
    <view class="card scene-enter delay-3">
      <view class="section-title">🔧 快捷操作</view>
      <view class="quick-grid">
        <view class="quick-item" @click="openAdminWeb">
          <view class="quick-icon">💻</view>
          <view class="quick-label">Web管理台</view>
        </view>
        <view class="quick-item" @click="goLogin">
          <view class="quick-icon">🔄</view>
          <view class="quick-label">切换账号</view>
        </view>
        <view class="quick-item" @click="exportData">
          <view class="quick-icon">📤</view>
          <view class="quick-label">导出数据</view>
        </view>
        <view class="quick-item" @click="viewLogs">
          <view class="quick-icon"></view>
          <view class="quick-label">操作日志</view>
        </view>
      </view>
    </view>
  </view>
</template>

<script>
import { uiStore } from '../../store/ui'
import { ensurePageAccess } from '../../utils/pageAuth'

export default {
  data() {
    return {
      uiStore,
      adminUrl: 'http://127.0.0.1:5174/',
      stats: {
        totalFarmers: 0,
        newFarmersToday: 0,
        totalOrders: 0,
        newOrdersToday: 0,
        totalAreaMu: 0,
        avgResponseMinutes: 0,
        disputeRate: 0,
        totalVillages: 0,
        signalGoodPct: 0,
        signalFairPct: 0,
        signalPoorPct: 0
      },
      disputes: []
    }
  },
  onShow() {
    uni.setNavigationBarTitle({ title: '管理员控制台' })
    ensurePageAccess('ADMIN')
  },
  methods: {
    refreshStats() {
      uni.showToast({ title: '数据已刷新', icon: 'success', duration: 1000 })
    },
    openDispute(item) {
      uni.navigateTo({ url: `/pages/common/area-report?orderId=${item.orderId}` })
    },
    resolveDispute(id, action) {
      const label = action === 'PASS' ? '已采信 GPS 面积' : '已驳回，建议双方协商'
      this.disputes = this.disputes.filter(d => d.id !== id)
      uni.showToast({ title: label, icon: 'success', duration: 1500 })
    },
    openAdminWeb() {
      if (typeof window !== 'undefined' && window.open) {
        window.open(this.adminUrl, '_blank')
        return
      }
      uni.showToast({ title: '请在浏览器中打开管理台', icon: 'none' })
    },
    goLogin() {
      uni.reLaunch({ url: '/pages/common/login' })
    },
    exportData() {
      uni.showToast({ title: '导出功能开发中…', icon: 'none' })
    },
    viewLogs() {
      uni.showToast({ title: '日志功能开发中…', icon: 'none' })
    }
  }
}
</script>

<style>
.admin-page {
  min-height: 100vh;
  padding: 28rpx 24rpx 56rpx;
  background: linear-gradient(170deg, #F0F4FD 0%, #F7F8FA 50%, #EEF7F1 100%);
}

/* 顶部身份卡 */
.admin-header {
  display: flex;
  align-items: center;
  gap: 20rpx;
}

.admin-avatar {
  width: 88rpx;
  height: 88rpx;
  border-radius: 22rpx;
  background: linear-gradient(135deg, #3B82F6, #1D4ED8);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 44rpx;
  box-shadow: 0 12rpx 28rpx rgba(59, 130, 246, 0.25);
}

.admin-name {
  font-size: 36rpx;
  font-weight: 800;
  color: var(--text-primary);
}

.admin-id {
  margin-top: 6rpx;
  font-size: 24rpx;
  color: var(--text-muted);
}

/* 大数据卡片 */
.stats-mega-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 14rpx;
  margin-bottom: 18rpx;
}

.mega-stat {
  padding: 28rpx 20rpx;
  border-radius: var(--radius-xl);
  text-align: center;
  position: relative;
  overflow: hidden;
}

.mega-stat::before {
  content: '';
  position: absolute;
  top: -30rpx; right: -30rpx;
  width: 100rpx; height: 100rpx;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.15);
}

.mega-stat-primary {
  background: var(--gradient-primary);
  color: #fff;
}

.mega-stat-gold {
  background: linear-gradient(135deg, #F59E0B, #D97706);
  color: #fff;
}

.mega-stat-value {
  font-size: 52rpx;
  font-weight: 800;
  line-height: 1.2;
  font-variant-numeric: tabular-nums;
}

.mega-stat-label {
  font-size: 24rpx;
  margin-top: 6rpx;
  opacity: 0.9;
}

.mega-stat-trend {
  font-size: 22rpx;
  margin-top: 10rpx;
  opacity: 0.75;
}

/* 详细统计格子 */
.stats-detail-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 10rpx;
}

.detail-stat {
  padding: 18rpx 8rpx;
  text-align: center;
  border-radius: var(--radius-lg);
  background: var(--page-bg);
}

.detail-stat-value {
  font-size: 32rpx;
  font-weight: 800;
  color: var(--text-primary);
  font-variant-numeric: tabular-nums;
}

.detail-stat-label {
  margin-top: 6rpx;
  font-size: 20rpx;
  color: var(--text-soft);
}

/* GPS 信号质量 */
.quality-section {
  margin-top: 24rpx;
  padding-top: 20rpx;
  border-top: 1px solid var(--border-light);
}

.quality-title {
  font-size: 26rpx;
  font-weight: 700;
  color: var(--text-regular);
  margin-bottom: 14rpx;
}

.quality-bars {
  display: flex;
  flex-direction: column;
  gap: 12rpx;
}

.quality-bar {
  display: flex;
  align-items: center;
  gap: 12rpx;
}

.quality-bar-label {
  width: 60rpx;
  font-size: 22rpx;
  color: var(--text-muted);
  flex-shrink: 0;
}

.quality-bar-track {
  flex: 1;
  height: 16rpx;
  background: var(--page-bg);
  border-radius: 999rpx;
  overflow: hidden;
}

.quality-bar-fill {
  height: 100%;
  border-radius: inherit;
  transition: width 1s var(--ease-out);
}

.quality-good { background: linear-gradient(90deg, #10B981, #34D399); }
.quality-fair { background: linear-gradient(90deg, #F59E0B, #FBBF24); }
.quality-poor { background: linear-gradient(90deg, #EF4444, #F87171); }

.quality-bar-pct {
  width: 60rpx;
  font-size: 22rpx;
  font-weight: 700;
  color: var(--text-primary);
  text-align: right;
  flex-shrink: 0;
}

/* Section common */
.section-head {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 16rpx;
  margin-bottom: 20rpx;
}

.section-title {
  font-size: 32rpx;
  font-weight: 800;
  color: var(--text-primary);
}

.section-subtitle {
  margin-top: 8rpx;
  font-size: 22rpx;
  color: var(--text-soft);
}

.section-link {
  font-size: 24rpx;
  font-weight: 700;
  color: var(--primary-color);
}

/* 争议审核 */
.dispute-badge {
  padding: 6rpx 18rpx;
  border-radius: 999rpx;
  background: var(--color-warning-bg);
  color: #92400E;
  font-size: 22rpx;
  font-weight: 700;
}

.dispute-list {
  display: flex;
  flex-direction: column;
  gap: 18rpx;
}

.dispute-item {
  padding: 24rpx;
  border-radius: var(--radius-xl);
  background: rgba(254, 243, 199, 0.2);
  border: 1px solid rgba(245, 158, 11, 0.1);
}

.dispute-title-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12rpx;
}

.dispute-name {
  font-size: 30rpx;
  font-weight: 700;
  color: var(--text-primary);
}

.dispute-meta {
  margin-top: 10rpx;
  font-size: 24rpx;
  color: var(--text-muted);
  line-height: 1.6;
}

.dispute-desc {
  margin-top: 8rpx;
  font-size: 24rpx;
  color: var(--text-soft);
  line-height: 1.6;
}

.dispute-actions {
  display: flex;
  gap: 14rpx;
  margin-top: 16rpx;
}

.dispute-action-btn {
  flex: 1;
  padding: 16rpx 0;
  text-align: center;
  border-radius: var(--radius-lg);
  font-size: 26rpx;
  font-weight: 700;
  transition: all var(--duration-200) var(--ease-out);
}

.dispute-action-btn:active {
  transform: scale(0.96);
}

.action-pass {
  background: rgba(16, 185, 129, 0.1);
  color: var(--primary-strong);
  border: 1px solid rgba(16, 185, 129, 0.2);
}

.action-reject {
  background: rgba(239, 68, 68, 0.08);
  color: #DC2626;
  border: 1px solid rgba(239, 68, 68, 0.15);
}

/* 快捷操作 */
.quick-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 14rpx;
  margin-top: 8rpx;
}

.quick-item {
  padding: 24rpx 8rpx;
  text-align: center;
  border-radius: var(--radius-xl);
  background: var(--page-bg);
  transition: all var(--duration-200) var(--ease-out);
}

.quick-item:active {
  transform: scale(0.95);
  background: var(--secondary-strong);
}

.quick-icon {
  font-size: 40rpx;
  margin-bottom: 8rpx;
}

.quick-label {
  font-size: 22rpx;
  font-weight: 600;
  color: var(--text-regular);
}

/* Status tags */
.status-tag {
  padding: 6rpx 18rpx;
  border-radius: 999rpx;
  font-size: 22rpx;
  font-weight: 700;
  white-space: nowrap;
}

.status-warn {
  background: var(--color-warning-bg);
  color: #92400E;
}

/* Empty state */
.empty-block { padding: 28rpx 0; }
.empty-title { font-size: 30rpx; font-weight: 700; color: var(--text-primary); margin-bottom: 10rpx; }
.empty-desc { font-size: 24rpx; color: var(--text-muted); line-height: 1.7; }
</style>
