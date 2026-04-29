<template>
  <view class="page report-page">
    <view class="card report-hero scene-enter">
      <view class="report-icon"></view>
      <view class="report-hero-title">作业面积报告</view>
      <view class="report-hero-sub">以下数据由北斗三号高精度轨迹自动计算，仅供双方参考</view>
    </view>

    <view v-if="loading" class="card scene-enter delay-1">
      <view class="loading-box">正在加载报告数据…</view>
    </view>

    <view v-else-if="!report" class="card scene-enter delay-1">
      <view class="empty-block">
        <view class="empty-title">暂无面积报告</view>
        <view class="empty-desc">作业完成后，系统将自动生成北斗卫星轨迹计算的面积报告。</view>
        <button class="btn btn-primary" style="margin-top: 32rpx;" @click="handleGenerate" :disabled="generating">
          {{ generating ? '正在生成…' : '常规估算面积' }}
        </button>

        <!-- V2 核心实用: 物理绕圈测田 -->
        <button class="btn btn-secondary" style="margin-top: 16rpx; border-color: #10B981; color: #10B981" @click="startBeidouMeasure">
          启动北斗绕地实测 (防扯皮)
        </button>
      </view>
    </view>

    <!-- 挂载北斗测算仪组件 (V2 新功能) -->
    <view v-else-if="showBeidouMeasurer" class="scene-enter">
      <beidou-land-measurer @complete="onMeasureComplete" />
    </view>

    <template v-else>
      <!-- 核心数据卡片 -->
      <view class="area-grid scene-enter delay-1">
        <view class="card area-card area-card-main">
          <view class="area-value">{{ report.correctedAreaMu }}</view>
          <view class="area-unit">亩（纠偏后）</view>
        </view>
        <view class="card area-card">
          <view class="area-value-sm">{{ report.rawAreaMu }}</view>
          <view class="area-unit">亩（原始）</view>
        </view>
      </view>

      <!-- 详细数据 -->
      <view class="card scene-enter delay-2">
        <view class="section-title">作业详情</view>
        <view class="detail-list">
          <view class="detail-row">
            <view class="detail-label">总行驶距离</view>
            <view class="detail-value">{{ formatDistance(report.totalDistanceM) }}</view>
          </view>
          <view class="detail-row">
            <view class="detail-label">作业幅宽</view>
            <view class="detail-value">{{ report.workWidthM }} 米</view>
          </view>
          <view class="detail-row">
            <view class="detail-label">作业时长</view>
            <view class="detail-value">{{ formatDuration(report.workDurationMinutes) }}</view>
          </view>
          <view class="detail-row">
            <view class="detail-label">有效轨迹点</view>
            <view class="detail-value">{{ report.trackPointCount }} 个</view>
          </view>
          <view class="detail-row">
            <view class="detail-label">信号质量</view>
            <view class="detail-value">
              <view class="signal-badge" :class="`signal-${report.signalQuality}`">
                {{ signalLabel(report.signalQuality) }}
              </view>
            </view>
          </view>
          <view class="detail-row">
            <view class="detail-label">生成时间</view>
            <view class="detail-value">{{ report.generatedAt || '-' }}</view>
          </view>
        </view>
      </view>

      <!-- 免责声明 -->
      <view class="card disclaimer-card scene-enter delay-3">
        <view class="disclaimer-icon">⚠️</view>
        <view class="disclaimer-text">
          本报告数据由北斗导航系统轨迹自动计算生成，受卫星信号、地形遮挡等因素影响，可能存在一定误差。具体作业费用请双方根据实际情况自行协商确定。农助手为中国公益平台，不参与费用结算。
        </view>
      </view>
    </template>
  </view>
</template>

<script>
import { trackApi } from '../../api/track'
import { uiStore } from '../../store/ui'

export default {
  data() {
    return {
      orderId: null,
      report: null,
      loading: false,
      generating: false,
      showBeidouMeasurer: false
    }
  },
  components: {
    // 注册引入我们刚才写的地头实用的组件
    'beidou-land-measurer': () => import('../../components/GpsLandMeasurer.vue')
  },
  onLoad(query) {
    this.orderId = query.orderId || null
    if (this.orderId) {
      this.loadReport()
    }
  },
  methods: {
    async loadReport() {
      this.loading = true
      try {
        const res = await trackApi.getReport(this.orderId)
        this.report = res
      } catch (e) {
        this.report = null
      } finally {
        this.loading = false
      }
    },
    async handleGenerate() {
      if (!this.orderId) return
      this.generating = true
      try {
        const res = await trackApi.generateReport({
          orderId: Number(this.orderId),
          ownerId: uiStore.userId,
          farmerId: null
        })
        this.report = res
        uni.showToast({ title: '报告已生成', icon: 'success' })
      } catch (e) {
        uni.showToast({ title: e?.message || '生成失败', icon: 'none' })
      } finally {
        this.generating = false
      }
    },
    startBeidouMeasure() {
      // 切换视图为硬核物理打卡组件
      this.showBeidouMeasurer = true
    },
    onMeasureComplete(result) {
      this.showBeidouMeasurer = false
      // 将测算结果挂载
      this.report = {
         correctedAreaMu: result.mu,
         rawAreaMu: result.mu,
         totalDistanceM: result.distance || 0,
         trackPointCount: result.points,
         signalQuality: 'GOOD',
         generatedAt: new Date().toLocaleString()
      }
    },
    formatDistance(meters) {
      if (!meters) return '0 米'
      const m = Number(meters)
      if (m >= 1000) return `${(m / 1000).toFixed(2)} 公里`
      return `${m.toFixed(0)} 米`
    },
    formatDuration(minutes) {
      if (!minutes) return '0 分钟'
      const m = Number(minutes)
      if (m >= 60) return `${Math.floor(m / 60)} 小时 ${m % 60} 分钟`
      return `${m} 分钟`
    },
    signalLabel(quality) {
      const map = { GOOD: '良好 ●●●●', FAIR: '一般 ●●●○', POOR: '较差 ●●○○', UNKNOWN: '未知' }
      return map[quality] || '未知'
    }
  }
}
</script>

<style>
.report-page {
  min-height: 100vh;
  padding: 28rpx 24rpx 56rpx;
}

.report-hero {
  text-align: center;
  padding: 48rpx 32rpx;
  background: linear-gradient(135deg, rgba(16, 185, 129, 0.08), rgba(245, 158, 11, 0.06));
}

.report-icon {
  font-size: 64rpx;
  margin-bottom: 16rpx;
}

.report-hero-title {
  font-size: 40rpx;
  font-weight: 800;
  color: var(--text-primary);
}

.report-hero-sub {
  margin-top: 12rpx;
  font-size: 26rpx;
  color: var(--text-muted);
  line-height: 1.6;
}

.area-grid {
  display: grid;
  grid-template-columns: 1.2fr 1fr;
  gap: 14rpx;
  margin-top: 18rpx;
}

.area-card {
  padding: 32rpx 20rpx;
  text-align: center;
}

.area-card-main {
  background: var(--gradient-primary);
}

.area-card-main .area-value {
  color: #FFFFFF;
  font-size: 56rpx;
  font-weight: 800;
  line-height: 1.2;
}

.area-card-main .area-unit {
  color: rgba(255, 255, 255, 0.85);
  font-size: 24rpx;
  margin-top: 8rpx;
}

.area-value-sm {
  font-size: 40rpx;
  font-weight: 800;
  color: var(--text-primary);
  line-height: 1.2;
}

.area-unit {
  font-size: 22rpx;
  color: var(--text-muted);
  margin-top: 8rpx;
}

.section-title {
  font-size: 32rpx;
  font-weight: 800;
  color: var(--text-primary);
  margin-bottom: 20rpx;
}

.detail-list {
  display: flex;
  flex-direction: column;
}

.detail-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 18rpx 0;
  border-bottom: 1px solid var(--border-light);
}

.detail-row:last-child {
  border-bottom: none;
}

.detail-label {
  font-size: 28rpx;
  color: var(--text-muted);
}

.detail-value {
  font-size: 28rpx;
  font-weight: 700;
  color: var(--text-primary);
}

.signal-badge {
  padding: 4rpx 16rpx;
  border-radius: 999rpx;
  font-size: 22rpx;
  font-weight: 700;
}

.signal-GOOD { background: var(--color-success-bg); color: #065F46; }
.signal-FAIR { background: var(--color-warning-bg); color: #92400E; }
.signal-POOR { background: var(--color-error-bg); color: #991B1B; }
.signal-UNKNOWN { background: var(--page-bg); color: var(--text-muted); }

.disclaimer-card {
  display: flex;
  gap: 16rpx;
  align-items: flex-start;
  margin-top: 18rpx;
  background: var(--color-warning-bg);
  border: 1px solid rgba(245, 158, 11, 0.15);
}

.disclaimer-icon {
  font-size: 36rpx;
  flex-shrink: 0;
}

.disclaimer-text {
  font-size: 24rpx;
  line-height: 1.7;
  color: #92400E;
}

.loading-box, .empty-block { padding: 28rpx 0; }
.empty-title { font-size: 30rpx; font-weight: 700; color: var(--text-primary); margin-bottom: 10rpx; }
.empty-desc { font-size: 24rpx; color: var(--text-muted); line-height: 1.7; }
</style>
