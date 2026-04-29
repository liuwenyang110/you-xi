<template>
  <view class="page">
    <view class="card hero-card glass-panel scene-enter">
      <view class="brand-row">
        <view class="brand-pill">农助手 · 联系详情</view>
        <view class="hero-badge" :class="`tone-${statusTone}`">{{ orderStatusText }}</view>
      </view>

      <template v-if="loading">
        <view class="state-card">
          <view class="state-title">正在加载联系记录</view>
          <view class="desc">请稍候，系统正在同步最新进度。</view>
        </view>
      </template>
      <template v-else-if="errorText">
        <view class="state-card warning-state">
          <view class="state-title">联系记录加载失败</view>
          <view class="desc">{{ errorText }}</view>
        </view>
      </template>
      <template v-else-if="order.id">
        <view class="hero-row">
          <view class="hero-copy">
            <view class="section-caption">联系服务进度</view>
            <view class="hero-title">联系 #{{ order.id }}</view>
            <view class="hero-subtitle">{{ displayServiceName }} · 联系时间 {{ lastUpdated }}</view>
            <view class="metric-strip">
              <view class="metric-chip">服务进度 {{ orderProgressPercent }}%</view>
              <view class="metric-chip">{{ lastUpdated || '--:--:--' }}</view>
            </view>
          </view>
        </view>

        <view class="decision-card">
          <view class="decision-title">{{ orderHeadline }}</view>
          <view class="decision-desc">{{ orderHint }}</view>
          <view class="order-progress">
            <view class="order-progress-head">
              <view class="order-progress-title">履约进度</view>
              <view class="order-progress-value">{{ orderProgressPercent }}%</view>
            </view>
            <view class="order-progress-track">
              <view class="order-progress-fill" :style="{ width: `${orderProgressPercent}%` }"></view>
            </view>
          </view>
          <view class="decision-tip">平台仅记录联系与服务节点，不保存通话内容，也不参与收款结算。</view>
          <view class="decision-actions">
            <view class="decision-action-pill" @click="onMainAction">{{ mainActionText }}</view>
            <view class="decision-action-pill" @click="onSecondaryAction">{{ secondaryActionText }}</view>
            <view class="decision-action-pill" v-if="canConfirmFinish" @click="confirmFinish">立即确认</view>
          </view>
        </view>

        <view class="summary-grid">
          <view class="summary-box primary">
            <view class="summary-label">当前状态</view>
            <view class="summary-value">{{ orderStatusText }}</view>
          </view>
          <view class="summary-box">
            <view class="summary-label">协作服务</view>
            <view class="summary-value">{{ displayServiceName }}</view>
          </view>
          <view class="summary-box weak">
            <view class="summary-label">关联需求</view>
            <view class="summary-value">#{{ order.demandId || '-' }}</view>
          </view>
        </view>
      </template>
      <template v-else>
        <view class="state-card">
          <view class="state-title">当前没有联系记录</view>
          <view class="desc">你可以返回首页查找附近农机主，或登录等待农机主主动联系。</view>
        </view>
      </template>
    </view>

    <view v-if="order.id && !errorText" class="card scene-enter delay-2">
      <view class="section-title">联系服务流程</view>
      <view class="step-list">
        <view class="step-item done">
          <view class="step-dot"></view>
          <view class="step-content">
            <view class="step-title">已建立联系</view>
            <view class="step-desc">双方已获得联系方式，可线下沟通。</view>
          </view>
        </view>
        <view class="step-item" :class="{ done: ['SERVING', 'FINISHED_PENDING_CONFIRM', 'COMPLETED'].includes(normalizedOrderStatus) }">
          <view class="step-dot"></view>
          <view class="step-content">
            <view class="step-title">服务进行中</view>
            <view class="step-desc">线下确认时间、价格与现场安排。</view>
          </view>
        </view>
        <view class="step-item" :class="{ done: ['FINISHED_PENDING_CONFIRM', 'COMPLETED'].includes(normalizedOrderStatus) }">
          <view class="step-dot"></view>
          <view class="step-content">
            <view class="step-title">服务完成</view>
            <view class="step-desc">作业结束，确认完工后即可评价。</view>
          </view>
        </view>
        <view class="step-item" :class="{ done: normalizedOrderStatus === 'COMPLETED' }">
          <view class="step-dot"></view>
          <view class="step-content">
            <view class="step-title">服务评价</view>
            <view class="step-desc">给这次服务打个分，让更多农户知道。</view>
          </view>
        </view>
      </view>
    </view>

    <!-- 评价区域 — 服务完成后显示 -->
    <view v-if="order.id && normalizedOrderStatus === 'COMPLETED' && !feedbackSubmitted" class="card scene-enter delay-3">
      <view class="section-title">服务评价</view>
      <view class="rating-row">
        <text
          v-for="star in 5" :key="star"
          class="rating-star" :class="{ active: ratingValue >= star }"
          @click="ratingValue = star"
        >⭐</text>
        <text class="rating-label">{{ ratingValue ? ratingValue + ' 星' : '请打分' }}</text>
      </view>
      <textarea
        class="feedback-input"
        placeholder="写写这次师傅怎么样？（可不填）"
        v-model="feedbackText"
        maxlength="200"
      />
      <view class="feedback-btn" :class="{ busy: feedbackBusy }" @click="submitFeedback">
        {{ feedbackBusy ? '提交中...' : '提交评价' }}
      </view>
    </view>
    <view v-if="feedbackSubmitted" class="card scene-enter">
      <view class="feedback-done">
        <text class="feedback-done-icon">❤️</text>
        <text class="feedback-done-text">谢谢您的评价，让更多农户知道这位加了！</text>
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
import { farmerApi } from '../../api/farmer'
import { ensurePageAccess } from '../../utils/pageAuth'
import { relaunchTo } from '../../utils/pageNav'

export default {
  data() {
    return {
      orderId: null,
      order: {},
      loading: false,
      confirming: false,
      errorText: '',
      pollTimer: null,
      lastUpdated: '',
      actionBusy: false,
      // 评价相关
      ratingValue: 0,
      feedbackText: '',
      feedbackBusy: false,
      feedbackSubmitted: false
    }
  },
  computed: {
    normalizedOrderStatus() {
      const raw = this.order.status || '-'
      if (raw === 'PENDING_CONTACT') return 'WAIT_NEGOTIATION'
      return raw
    },
    statusTone() {
      if (this.normalizedOrderStatus === 'COMPLETED') return 'active'
      if (this.normalizedOrderStatus === 'FINISHED_PENDING_CONFIRM') return 'pending'
      if (this.normalizedOrderStatus === '-') return 'closed'
      return 'pending'
    },
    orderStatusText() {
      const map = {
        WAIT_NEGOTIATION: '和师傅商量中',
        PENDING_CONTACT: '等候师傅到来',
        SERVING: '师傅正在干活',
        FINISHED_PENDING_CONFIRM: '活干完了，确认一下',
        COMPLETED: '服务完成'
      }
      return map[this.normalizedOrderStatus] || this.normalizedOrderStatus
    },
    displayServiceName() {
      const raw = String(this.order.serviceName || '').trim()
      if (raw) return raw
      return '农机协作订单'
    },
    canConfirmFinish() {
      return !!this.order.id && ['WAIT_NEGOTIATION', 'SERVING', 'FINISHED_PENDING_CONFIRM'].includes(this.normalizedOrderStatus)
    },
    confirmButtonText() {
      if (this.confirming || this.actionBusy) return '提交中...'
      if (this.normalizedOrderStatus === 'WAIT_NEGOTIATION') return '标记协商完成'
      if (this.normalizedOrderStatus === 'SERVING') return '提交完工确认'
      return '确认完工'
    },
    orderProgressPercent() {
      const map = {
        WAIT_NEGOTIATION: 25,
        SERVING: 65,
        FINISHED_PENDING_CONFIRM: 85,
        COMPLETED: 100
      }
      return map[this.normalizedOrderStatus] || 15
    },
    orderHeadline() {
      if (this.normalizedOrderStatus === 'WAIT_NEGOTIATION') return '已拿到师傅电话，打电话商量好时间'
      if (this.normalizedOrderStatus === 'SERVING') return '师傅正在地里干活'
      if (this.normalizedOrderStatus === 'FINISHED_PENDING_CONFIRM') return '师傅说活干完了，确认一下'
      if (this.normalizedOrderStatus === 'COMPLETED') return '这次服务圆满完成！'
      return '订单已更新'
    },
    orderHint() {
      const raw = this.normalizedOrderStatus
      if (raw === 'WAIT_NEGOTIATION') return '已拿到师傅电话，打电话商量好价格和下地时间。'
      if (raw === 'SERVING') return '师傅正在地里干，完工后进来确认一下。'
      if (raw === 'FINISHED_PENDING_CONFIRM') return '师傅说活干完了，您确认完工后，平台会把这次服务标记完成。'
      if (raw === 'COMPLETED') return '订单已全部完成，感谢使用农助手！'
      return '可以继续刷新查看最新进展。'
    },
    actionTitle() {
      if (this.loading) return '正在同步订单状态'
      if (this.errorText) return '订单同步异常'
      if (!this.order.id) return '暂无订单可操作'
      if (this.canConfirmFinish) return '等待你推进下一步'
      if (this.normalizedOrderStatus === 'COMPLETED') return '订单已完成'
      return '继续跟进订单'
    },
    actionSubtitle() {
      if (this.loading) return '正在同步最新进度'
      if (this.errorText) return '可重试刷新，或返回首页'
      if (!this.order.id) return '回到首页继续查看需求进展'
      if (this.canConfirmFinish) return '按当前阶段提交确认，推动状态流转'
      if (this.normalizedOrderStatus === 'COMPLETED') return '可返回首页继续发起新需求'
      return '建议持续关注订单进展'
    },
    mainActionText() {
      if (this.loading) return '刷新订单'
      if (this.errorText || !this.order.id) return '刷新订单'
      if (this.canConfirmFinish) return this.confirmButtonText
      return '刷新订单'
    },
    secondaryActionText() {
      if (this.loading) return '返回首页'
      if (this.errorText || !this.order.id) return '返回首页'
      if (this.normalizedOrderStatus === 'COMPLETED') return '返回首页'
      return '返回首页'
    }
  },
  onLoad(query) {
    this.orderId = query.orderId
  },
  onShow() {
    uni.setNavigationBarTitle({ title: '联系详情' })
    if (!ensurePageAccess('FARMER')) return
    this.loadOrder()
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
        this.loadOrder(false)
      }, 4000)
    },
    stopPolling() {
      if (this.pollTimer) {
        clearInterval(this.pollTimer)
        this.pollTimer = null
      }
    },
    async loadOrder(showError = true) {
      this.loading = true
      if (showError) this.errorText = ''
      try {
        if (this.orderId) {
          this.order = await farmerApi.getOrder(this.orderId)
          this.lastUpdated = new Date().toLocaleTimeString()
          if (this.order.status === 'COMPLETED') this.stopPolling()
          return
        }
        const orders = await farmerApi.listOrders()
        this.order = orders[0] || {}
        if (this.order.id) this.orderId = this.order.id
        this.lastUpdated = new Date().toLocaleTimeString()
        if (this.order.status === 'COMPLETED') this.stopPolling()
      } catch (error) {
        if (showError) {
          this.order = {}
          this.errorText = error?.message || '加载订单失败'
        }
      } finally {
        this.loading = false
      }
    },
    async confirmFinish() {
      if (!this.order.id || this.confirming || this.actionBusy) return
      const confirm = await new Promise((resolve) => {
        uni.showModal({
          title: '确认提交',
          content: '提交后会推动订单进入下一状态，是否继续？',
          success: (res) => resolve(!!res.confirm),
          fail: () => resolve(false)
        })
      })
      if (!confirm) return
      this.confirming = true
      this.actionBusy = true
      try {
        const res = await farmerApi.confirmFinish(this.order.id)
        this.order = res.order || this.order
        await this.loadOrder()
        uni.showToast({ title: '已提交完工确认', icon: 'success' })
      } catch (error) {
        uni.showToast({ title: error?.message || '操作失败', icon: 'none' })
      } finally {
        this.confirming = false
        this.actionBusy = false
      }
    },
    onMainAction() {
      if (this.actionBusy) return
      if (this.canConfirmFinish) {
        this.confirmFinish()
        return
      }
      this.loadOrder()
    },
    onSecondaryAction() {
      if (this.actionBusy) return
      this.backHome()
    },
    async submitFeedback() {
      if (this.feedbackBusy) return
      if (!this.ratingValue) {
        uni.showToast({ title: '请先打个分吧！', icon: 'none' })
        return
      }
      this.feedbackBusy = true
      try {
        await farmerApi.submitFeedback(this.order.id, this.feedbackText, this.ratingValue)
        this.feedbackSubmitted = true
        this.stopPolling()
        uni.showToast({ title: '评价提交成功 ❤️', icon: 'success' })
      } catch (e) {
        uni.showToast({ title: e?.message || '提交失败', icon: 'none' })
      } finally {
        this.feedbackBusy = false
      }
    },
    backHome() {
      relaunchTo('/pages/farmer/home')
    }
  }
}
</script>

<style scoped>
.page { padding-bottom: calc(252rpx + env(safe-area-inset-bottom)); }
.brand-row { display: flex; justify-content: space-between; align-items: center; gap: 12rpx; margin-bottom: 14rpx; }
.brand-pill { padding: 10rpx 18rpx; border-radius: var(--radius-full); background: rgba(16,185,129,0.1); color: var(--primary-strong); font-size: var(--text-xs); font-weight: var(--font-bold); }
.hero-badge { padding: 12rpx 20rpx; border-radius: var(--radius-full); font-weight: var(--font-bold); }
.hero-badge.tone-pending { background: rgba(211, 149, 22, 0.14); color: var(--color-warning-text, #8a6208); }
.hero-badge.tone-active { background: rgba(16, 185, 129, 0.12); color: var(--primary-strong); }
.hero-badge.tone-closed { background: rgba(128, 94, 76, 0.14); color: var(--text-muted); }
.hero-row { display: flex; gap: 20rpx; align-items: center; }
.hero-copy { flex: 1; }
.state-card { background: rgba(255,255,255,0.54); border-radius: var(--radius-2xl); padding: 22rpx; margin-top: 8rpx; }
.warning-state { background: rgba(255, 245, 237, 0.94); }
.state-title { font-size: var(--text-lg); font-weight: var(--font-bold); color: var(--text-primary); margin-bottom: 12rpx; }
.decision-card { padding: 22rpx; border-radius: var(--radius-2xl); background: linear-gradient(165deg, rgba(255, 255, 255, 0.9), rgba(242, 248, 238, 0.92)); border: 1rpx solid var(--border-light); box-shadow: var(--shadow-base); margin-top: 8rpx; }
.decision-title { font-size: var(--text-lg); font-weight: var(--font-bold); color: var(--text-primary); margin-bottom: 10rpx; line-height: 1.34; }
.decision-desc { color: var(--text-muted); line-height: 1.7; }
.decision-tip { margin-top: 14rpx; padding: 14rpx 16rpx; border-radius: var(--radius-lg); background: var(--color-warning-bg); color: var(--color-warning-text, #7a5f16); font-size: var(--text-xs); }
.decision-actions { display: flex; flex-wrap: wrap; gap: 12rpx; margin-top: 14rpx; }
.decision-action-pill { padding: 12rpx 16rpx; border-radius: var(--radius-full); background: rgba(255,255,255,0.84); color: var(--primary-strong); border: 1rpx solid var(--border-light); font-size: var(--text-xs); }
.order-progress { margin-top: 14rpx; padding: 14rpx; border-radius: var(--radius-xl); background: rgba(255, 255, 255, 0.66); border: 1rpx solid var(--border-light); }
.order-progress-head { display: flex; justify-content: space-between; align-items: center; margin-bottom: 10rpx; }
.order-progress-title { font-size: var(--text-xs); color: var(--text-muted); }
.order-progress-value { font-size: var(--text-xs); font-weight: var(--font-bold); color: var(--primary-strong); }
.order-progress-track { height: 10rpx; border-radius: var(--radius-full); overflow: hidden; background: rgba(103, 123, 94, 0.2); }
.order-progress-fill { height: 100%; border-radius: inherit; background: var(--gradient-primary); transition: width .3s ease; }
.summary-grid { display: grid; grid-template-columns: repeat(3, 1fr); gap: 14rpx; margin-top: 18rpx; }
.summary-box { background: rgba(255,255,255,0.56); border-radius: var(--radius-2xl); padding: 18rpx; border: 1rpx solid var(--border-light); }
.summary-box.primary { background: linear-gradient(165deg, rgba(16, 185, 129, 0.12), rgba(255, 255, 255, 0.74)); }
.summary-box.weak { opacity: 0.86; }
.summary-label { font-size: var(--text-xs); color: var(--text-muted); margin-bottom: 8rpx; }
.summary-value { font-size: var(--text-xs); line-height: 1.5; font-weight: var(--font-bold); color: var(--text-primary); }
.step-list { margin-top: 18rpx; }
.step-item { display: flex; gap: 18rpx; padding: 18rpx 0; opacity: 0.58; }
.step-item.done { opacity: 1; }
.step-dot { width: 22rpx; height: 22rpx; margin-top: 8rpx; border-radius: 50%; background: rgba(93, 120, 86, 0.25); flex-shrink: 0; }
.step-item.done .step-dot { background: var(--primary-strong); box-shadow: 0 0 0 8rpx rgba(16, 185, 129, 0.10); }
.step-content { flex: 1; }
.step-title { font-size: var(--text-base); font-weight: var(--font-bold); color: var(--text-primary); margin-bottom: 8rpx; }
.step-desc { color: var(--text-muted); line-height: 1.7; font-size: var(--text-xs); }
.action-dock { position: fixed; left: 24rpx; right: 24rpx; bottom: calc(14rpx + env(safe-area-inset-bottom)); padding-top: 12rpx; z-index: 40; }
.action-panel { border-radius: var(--radius-2xl); padding: 22rpx; background: rgba(255, 252, 245, 0.92); border: 1rpx solid var(--border-light); box-shadow: 0 24rpx 46rpx rgba(59, 44, 18, 0.14); backdrop-filter: blur(14px); }
.action-copy { margin-bottom: 16rpx; }
.action-title { font-size: var(--text-base); font-weight: var(--font-bold); color: var(--text-primary); margin-bottom: 8rpx; }
.action-subtitle { font-size: var(--text-xs); color: var(--text-muted); line-height: 1.6; }
.action-buttons { display: flex; gap: 14rpx; }
.action-btn { margin-top: 0; flex: 1; text-align: center; }
.action-btn.secondary { background: rgba(255, 255, 255, 0.82); border: 1rpx solid var(--border-light); }
.action-btn.is-disabled { opacity: 0.65; filter: saturate(0.75); }

/* 评价区域 */
.rating-row { display: flex; align-items: center; gap: 12rpx; margin: 20rpx 0; }
.rating-star { font-size: 48rpx; opacity: 0.3; transition: opacity 0.15s; }
.rating-star.active { opacity: 1; }
.rating-label { font-size: var(--text-base); color: var(--text-muted); font-weight: var(--font-bold); margin-left: 8rpx; }
.feedback-input {
  width: 100%; min-height: 140rpx;
  background: rgba(255,255,255,0.7); border-radius: var(--radius-xl);
  padding: 20rpx; font-size: var(--text-base); color: var(--text-primary);
  border: 1rpx solid var(--border-light);
  margin-bottom: 20rpx; box-sizing: border-box;
  line-height: 1.6;
}
.feedback-btn {
  height: 90rpx; border-radius: var(--radius-full);
  background: var(--gradient-primary);
  color: white; font-size: var(--text-base); font-weight: 800;
  display: flex; align-items: center; justify-content: center;
  box-shadow: var(--shadow-colored);
}
.feedback-btn.busy { opacity: 0.65; }
.feedback-done {
  display: flex; flex-direction: column; align-items: center;
  padding: 40rpx 0; gap: 16rpx;
}
.feedback-done-icon { font-size: 72rpx; }
.feedback-done-text { font-size: var(--text-base); color: var(--primary-strong); font-weight: var(--font-bold); text-align: center; line-height: 1.6; }
</style>
