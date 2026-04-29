<template>
  <view class="page">
    <view class="card hero-card glass-panel scene-enter">
      <view class="brand-row">
        <view class="brand-pill">农助手 · 找机进度</view>
        <ux-capsule
          :label="statusText"
          :tone="statusTone === 'pending' ? 'warn' : (statusTone === 'active' ? 'active' : 'closed')"
          icon-kind="match"
          :strong="true"
        />
      </view>

      <view class="hero-row">
        <view class="hero-copy">
          <view class="section-caption">需求汇报进度</view>
          <view class="hero-title">找机结果</view>
          <view class="hero-subtitle">正在按照距离和机型匹配附近农机主，也可返回首页自己浏览直接联系。</view>
          <view class="metric-strip">
            <view class="metric-chip">需求 #{{ demandId || '-' }}</view>
            <view class="metric-chip">第 {{ status.matchAttemptCount || 0 }} 轮</view>
            <view class="metric-chip">{{ lastUpdated || '--:--:--' }}</view>
          </view>
        </view>
      </view>

      <view v-if="loading" class="state-card">
        <view class="state-title">正在同步匹配状态</view>
        <view class="desc">请稍候，系统正在拉取当前轮次最新结果。</view>
      </view>
      <view v-else-if="errorText" class="state-card warning-state">
        <view class="state-title">匹配状态获取失败</view>
        <view class="desc">{{ errorText }}</view>
      </view>
      <view v-else class="decision-card">
        <view class="decision-title">{{ statusHeadline }}</view>
        <view class="decision-desc">{{ statusHint }}</view>
        <view class="decision-step">
          <view class="decision-step-title">下一步</view>
          <view class="decision-step-desc">{{ nextActionText }}</view>
        </view>
        <ux-hint title="隐私提示" text="确认联系前将持续使用隐私号。" tone="warn" icon-kind="hint" />
        <view class="decision-actions">
          <view class="decision-action-pill" @click="onMainAction">{{ mainActionText }}</view>
          <view class="decision-action-pill" @click="onSecondaryAction">{{ secondaryActionText }}</view>
          <view class="decision-action-pill" v-if="canCancel" @click="cancelDemand">取消需求</view>
        </view>
      </view>

      <view class="summary-grid">
        <view class="summary-box primary">
          <view class="summary-label">当前状态</view>
          <view class="summary-value">{{ statusText }}</view>
        </view>
        <view class="summary-box">
          <view class="summary-label">匹配轮次</view>
          <view class="summary-value">第 {{ status.matchAttemptCount || 0 }} 轮</view>
        </view>
        <view class="summary-box weak">
          <view class="summary-label">最近刷新</view>
          <view class="summary-value">{{ lastUpdated || '--:--:--' }}</view>
        </view>
      </view>
    </view>

    <view class="card scene-enter delay-2">
      <view class="section-title">找机进度</view>
      <view class="progress-list">
        <view class="progress-item">
          <view class="progress-dot" :class="{ active: flowStepDone('FILTER') }"></view>
          <view class="progress-body">
            <view class="progress-title">帮您寻找机手</view>
            <view class="desc">按您的位置、作物和亩数，找附近的机手。</view>
          </view>
        </view>
        <view class="progress-item">
          <view class="progress-dot" :class="{ active: flowStepDone('DISPATCH') }"></view>
          <view class="progress-body">
            <view class="progress-title">通知师傅接单</view>
            <view class="desc">先通知近的师傅，没人接再扩大范围。</view>
          </view>
        </view>
        <view class="progress-item">
          <view class="progress-dot" :class="{ active: flowStepDone('CONTACT') }"></view>
          <view class="progress-body">
            <view class="progress-title">拿到电话，谈好来地</view>
            <view class="desc">师傅接单后，您确认一下就能拿到电话。</view>
          </view>
        </view>
      </view>
    </view>

    <view class="action-dock scene-enter delay-3">
      <view class="action-panel">
        <view class="action-copy">
          <view class="action-title">{{ actionTitle }}</view>
          <view class="action-subtitle">{{ actionSubtitle }}</view>
        </view>
        <view class="action-buttons">
          <view class="primary-btn action-btn" :class="{ 'is-disabled': actionBusy }" @click="onMainAction">{{ mainActionText }}</view>
          <view class="secondary-btn action-btn secondary" :class="{ 'is-disabled': actionBusy }" @click="onSecondaryAction">{{ secondaryActionText }}</view>
        </view>
      </view>
    </view>
  </view>
</template>

<script>
import UxCapsule from '../../components/UxCapsule.vue'
import UxHint from '../../components/UxHint.vue'
import { farmerApi } from '../../api/farmer'
import { ensurePageAccess } from '../../utils/pageAuth'
import { relaunchTo } from '../../utils/pageNav'
import {
  getDemandStatusDescription,
  getDemandStatusLabel,
  getDemandStatusTone,
  normalizeDemandStatus
} from '../../constants/status'

export default {
  components: { UxCapsule, UxHint },
  data() {
    return {
      demandId: null,
      loading: false,
      errorText: '',
      status: {},
      pollTimer: null,
      lastUpdated: '',
      actionBusy: false
    }
  },
  computed: {
    normalizedStatus() {
      return normalizeDemandStatus(this.status.status || 'MATCHING')
    },
    statusTone() {
      return getDemandStatusTone(this.normalizedStatus)
    },
    statusText() {
      return getDemandStatusLabel(this.normalizedStatus)
    },
    statusHeadline() {
      const raw = this.normalizedStatus
      if (raw === 'MATCHING') return '正在帮您寻找附近机手，请稍等'
      if (raw === 'MATCH_FAILED') return '这次没找到合适机手'
      if (raw === 'WAIT_FARMER_CONTACT_CONFIRM') return '师傅接单了，快去确认联系！'
      if (raw === 'NEGOTIATING') return '和师傅商量好时间和价格吧'
      if (raw === 'IN_SERVICE' || raw === 'SERVING') return '师傅正在地里干活'
      if (raw === 'COMPLETED') return '这次服务圆满完成！'
      if (raw === 'CANCELLED') return '需求已取消'
      return '状态已更新'
    },
    statusHint() {
      const raw = this.normalizedStatus
      if (raw === 'MATCHING') return '我们帮您找附近的机手，您可以放心等候，有消息会通知您。'
      if (raw === 'MATCH_FAILED') return '这次没找到，可以重新发一次需求或扩大范围再试试。'
      if (raw === 'WAIT_FARMER_CONTACT_CONFIRM') return '有师傅要来了，点下面按钮确认联系，拿到师傅电话。'
      if (raw === 'NEGOTIATING') return '已拿到师傅电话，打电话商量好下地时间和价格。'
      if (raw === 'IN_SERVICE' || raw === 'SERVING') return '机手已下地作业，干完活来这里确认一下就行。'
      if (raw === 'COMPLETED') return getDemandStatusDescription(raw)
      if (raw === 'CANCELLED') return getDemandStatusDescription(raw)
      return getDemandStatusDescription(raw)
    },
    nextActionText() {
      if (this.normalizedStatus === 'MATCHING') return '坐等通知，有师傅接单会告诉您。'
      if (this.normalizedStatus === 'MATCH_FAILED') return '点"重新找机手"再等等，或者取消后重发。'
      if (this.normalizedStatus === 'WAIT_FARMER_CONTACT_CONFIRM') return '点下面大按钮，立刻拿到师傅电话。'
      if (['NEGOTIATING', 'IN_SERVICE', 'SERVING', 'COMPLETED'].includes(this.normalizedStatus) && this.status.currentOrderId) return '去看订单进度。'
      if (this.normalizedStatus === 'CANCELLED') return '回首页重新叫机手。'
      return '刷新一下看看状态。'
    },
    canCancel() {
      return ['PUBLISHED', 'MATCHING', 'MATCH_FAILED'].includes(this.normalizedStatus)
    },
    actionTitle() {
      if (this.loading) return '正在查询状态'
      if (this.errorText) return '查询失败，请重试'
      if (this.normalizedStatus === 'WAIT_FARMER_CONTACT_CONFIRM') return '师傅等您确认'
      if (this.normalizedStatus === 'MATCH_FAILED') return '这次没找到机手'
      if (['NEGOTIATING', 'IN_SERVICE', 'SERVING'].includes(this.normalizedStatus)) return '已联系上师傅'
      return '正在帮您找机手'
    },
    actionSubtitle() {
      if (this.loading) return '稍等一下...'
      if (this.errorText) return '点按钮重试，或者回首页'
      if (this.normalizedStatus === 'WAIT_FARMER_CONTACT_CONFIRM') return '确认后马上给您师傅电话'
      if (this.normalizedStatus === 'MATCH_FAILED') return '可以重新发需求再等等'
      if (['NEGOTIATING', 'IN_SERVICE', 'SERVING'].includes(this.normalizedStatus)) return '打电话商量好后去看订单'
      if (this.normalizedStatus === 'CANCELLED') return '需求已结束，可以重新发'
      return '有消息我们会通知您'
    },
    mainActionText() {
      if (this.actionBusy) return '处理中...'
      if (this.loading || this.errorText) return '刷新一下'
      if (this.normalizedStatus === 'WAIT_FARMER_CONTACT_CONFIRM') return '去确认联系师傅'
      if (['NEGOTIATING', 'IN_SERVICE', 'SERVING', 'COMPLETED'].includes(this.normalizedStatus) && this.status.currentOrderId) return '查看订单进度'
      if (this.normalizedStatus === 'MATCH_FAILED' && this.canCancel) return '取消后重新找'
      if (this.normalizedStatus === 'CANCELLED') return '回首页'
      return '刷新一下'
    },
    secondaryActionText() {
      if (this.actionBusy) return '请稍候'
      if (this.loading || this.errorText) return '回首页'
      if (['MATCHING', 'PUBLISHED'].includes(this.normalizedStatus)) return '回首页'
      if (this.normalizedStatus === 'WAIT_FARMER_CONTACT_CONFIRM') return '再刷新一下'
      return '回首页'
    }
  },
  onLoad(query) {
    this.demandId = query.demandId
  },
  onShow() {
    uni.setNavigationBarTitle({ title: '找机结果' })
    if (!ensurePageAccess('FARMER')) return
    this.refreshStatus()
    this.startPolling()
  },
  onHide() {
    this.stopPolling()
  },
  onUnload() {
    this.stopPolling()
  },
  methods: {
    startPolling() {
      this.stopPolling()
      this.pollTimer = setInterval(() => {
        this.refreshStatus(false)
      }, 3000)
    },
    stopPolling() {
      if (this.pollTimer) {
        clearInterval(this.pollTimer)
        this.pollTimer = null
      }
    },
    async refreshStatus(showError = true) {
      if (!this.demandId) return
      this.loading = true
      if (showError) this.errorText = ''
      try {
        this.status = await farmerApi.getMatchStatus(this.demandId)
        this.lastUpdated = new Date().toLocaleTimeString()
        if (this.status.currentOrderId) {
          this.stopPolling()
          uni.redirectTo({ url: `/pages/farmer/order-detail?orderId=${this.status.currentOrderId}` })
          return
        }
        if (this.status.currentContactSessionId) {
          this.stopPolling()
          uni.redirectTo({ url: `/pages/farmer/contact-confirm?contactId=${this.status.currentContactSessionId}&demandId=${this.demandId}` })
        }
      } catch (error) {
        if (showError) this.errorText = error?.message || '获取匹配状态失败'
      } finally {
        this.loading = false
      }
    },
    async cancelDemand() {
      if (!this.demandId || this.actionBusy) return
      this.actionBusy = true
      try {
        await farmerApi.cancelDemand(this.demandId)
        uni.showToast({ title: '需求已取消', icon: 'success' })
        setTimeout(() => {
          relaunchTo('/pages/farmer/home')
        }, 500)
      } catch (error) {
        uni.showToast({ title: error?.message || '取消失败', icon: 'none' })
      } finally {
        this.actionBusy = false
      }
    },
    onMainAction() {
      if (this.actionBusy) return
      if (this.loading || this.errorText) {
        this.refreshStatus()
        return
      }
      if (this.normalizedStatus === 'WAIT_FARMER_CONTACT_CONFIRM' && this.status.currentContactSessionId) {
        uni.navigateTo({ url: `/pages/farmer/contact-confirm?contactId=${this.status.currentContactSessionId}&demandId=${this.demandId}` })
        return
      }
      if (['NEGOTIATING', 'IN_SERVICE', 'COMPLETED'].includes(this.normalizedStatus) && this.status.currentOrderId) {
        uni.navigateTo({ url: `/pages/farmer/order-detail?orderId=${this.status.currentOrderId}` })
        return
      }
      if (this.normalizedStatus === 'MATCH_FAILED' && this.canCancel) {
        this.cancelDemand()
        return
      }
      if (this.normalizedStatus === 'CANCELLED') {
        this.backHome()
        return
      }
      this.refreshStatus()
    },
    onSecondaryAction() {
      if (this.actionBusy) return
      if (this.normalizedStatus === 'WAIT_FARMER_CONTACT_CONFIRM') {
        this.refreshStatus()
        return
      }
      this.backHome()
    },
    flowStepDone(step) {
      const current = this.normalizedStatus
      if (step === 'FILTER') return ['MATCHING', 'WAIT_FARMER_CONTACT_CONFIRM', 'NEGOTIATING', 'IN_SERVICE', 'COMPLETED', 'MATCH_FAILED', 'CANCELLED'].includes(current)
      if (step === 'DISPATCH') return ['WAIT_FARMER_CONTACT_CONFIRM', 'NEGOTIATING', 'IN_SERVICE', 'COMPLETED', 'MATCH_FAILED', 'CANCELLED'].includes(current)
      if (step === 'CONTACT') return ['WAIT_FARMER_CONTACT_CONFIRM', 'NEGOTIATING', 'IN_SERVICE', 'COMPLETED'].includes(current)
      return false
    },
    backHome() {
      relaunchTo('/pages/farmer/home')
    }
  }
}
</script>

<style>
.page { padding-bottom: calc(252rpx + env(safe-area-inset-bottom)); }
.brand-row { display: flex; justify-content: space-between; align-items: center; gap: 12rpx; margin-bottom: 14rpx; }
.brand-pill { padding: 10rpx 18rpx; border-radius: 999rpx; background: rgba(47, 107, 61, 0.1); color: #2f6b3d; font-size: 22rpx; font-weight: 700; }
.hero-badge { padding: 12rpx 20rpx; border-radius: 999rpx; font-weight: 700; }
.hero-badge.tone-pending { background: rgba(211, 149, 22, 0.14); color: #8a6208; }
.hero-badge.tone-active { background: rgba(47, 107, 61, 0.12); color: #2f6b3d; }
.hero-badge.tone-closed { background: rgba(128, 94, 76, 0.14); color: #6f5141; }
.hero-row { display: flex; gap: 20rpx; align-items: center; }
.hero-copy { flex: 1; }
.state-card { background: rgba(255,255,255,0.54); border-radius: 24rpx; padding: 22rpx; margin-top: 8rpx; }
.warning-state { background: rgba(255, 245, 237, 0.94); }
.state-title { font-size: 32rpx; font-weight: 700; color: #244a2f; margin-bottom: 12rpx; }
.decision-card { padding: 22rpx; border-radius: 24rpx; background: linear-gradient(165deg, rgba(255, 255, 255, 0.9), rgba(242, 248, 238, 0.92)); border: 1rpx solid rgba(82, 100, 67, 0.12); box-shadow: 0 10rpx 24rpx rgba(62, 83, 49, 0.08); margin-top: 8rpx; }
.decision-title { font-size: 32rpx; font-weight: 700; color: #244a2f; margin-bottom: 10rpx; line-height: 1.34; }
.decision-desc { color: #55685a; line-height: 1.7; }
.decision-step { margin-top: 14rpx; padding: 14rpx 16rpx; border-radius: 16rpx; background: rgba(255, 255, 255, 0.74); border: 1rpx solid rgba(93, 112, 76, 0.12); }
.decision-step-title { font-size: 24rpx; font-weight: 700; color: #2a5235; margin-bottom: 8rpx; }
.decision-step-desc { font-size: 24rpx; color: #5c6e5d; line-height: 1.6; }
.decision-tip { margin-top: 14rpx; padding: 14rpx 16rpx; border-radius: 16rpx; background: rgba(255, 243, 209, 0.86); color: #7a5f16; font-size: 24rpx; }
.decision-actions { display: flex; flex-wrap: wrap; gap: 12rpx; margin-top: 14rpx; }
.decision-action-pill { padding: 12rpx 16rpx; border-radius: 999rpx; background: rgba(255,255,255,0.85); color: #2a5536; border: 1rpx solid rgba(86, 104, 74, 0.14); font-size: 22rpx; }
.summary-grid { display: grid; grid-template-columns: repeat(3, 1fr); gap: 14rpx; margin-top: 18rpx; }
.summary-box { background: rgba(255,255,255,0.56); border-radius: 22rpx; padding: 18rpx; border: 1rpx solid rgba(101, 84, 41, 0.06); }
.summary-box.primary { background: linear-gradient(165deg, rgba(70, 131, 84, 0.16), rgba(255, 255, 255, 0.74)); }
.summary-box.weak { opacity: 0.86; }
.summary-label { font-size: 22rpx; color: #73806d; margin-bottom: 8rpx; }
.summary-value { font-size: 24rpx; line-height: 1.5; font-weight: 700; color: #274633; }
.progress-list { margin-top: 18rpx; }
.progress-item { display: flex; gap: 18rpx; padding: 18rpx 0; }
.progress-item + .progress-item { border-top: 1rpx solid rgba(92, 108, 82, 0.10); }
.progress-dot { width: 22rpx; height: 22rpx; border-radius: 999rpx; margin-top: 10rpx; background: rgba(120, 133, 112, 0.28); flex-shrink: 0; }
.progress-dot.active { background: linear-gradient(135deg, #2f6b3d, #467e50); box-shadow: 0 0 0 8rpx rgba(47, 107, 61, 0.12); }
.progress-title { font-size: 28rpx; font-weight: 700; color: #294633; margin-bottom: 10rpx; }
.action-dock { position: fixed; left: 24rpx; right: 24rpx; bottom: calc(14rpx + env(safe-area-inset-bottom)); padding-top: 12rpx; z-index: 40; }
.action-panel { border-radius: 30rpx; padding: 22rpx; background: rgba(255, 252, 245, 0.92); border: 1rpx solid rgba(90, 76, 43, 0.08); box-shadow: 0 24rpx 46rpx rgba(59, 44, 18, 0.14); backdrop-filter: blur(14px); }
.action-copy { margin-bottom: 16rpx; }
.action-title { font-size: 30rpx; font-weight: 700; color: #254c31; margin-bottom: 8rpx; }
.action-subtitle { font-size: 24rpx; color: #67745f; line-height: 1.6; }
.action-buttons { display: flex; gap: 14rpx; }
.action-btn { margin-top: 0; flex: 1; text-align: center; }
.action-btn.secondary { background: rgba(255, 255, 255, 0.82); border: 1rpx solid rgba(83, 105, 72, 0.14); }
.action-btn.is-disabled { opacity: 0.65; filter: saturate(0.75); }
</style>
