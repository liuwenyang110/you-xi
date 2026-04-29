<template>
  <view class="page">
    <view class="card hero-card glass-panel scene-enter">
      <view class="brand-row">
        <view class="brand-pill">农助手 · 农户端</view>
        <view class="hero-badge" :class="`tone-${statusTone}`">{{ contactStatusText }}</view>
      </view>

      <view class="hero-top">
        <view>
          <view class="section-caption">建立联系</view>
          <view class="hero-title">联系机手</view>
          <view class="hero-subtitle">确认联系后，您可以直接拨打机手电话或发送微信。</view>
        </view>
      </view>

      <view class="stats-grid" v-if="contact.id">
        <view class="stat-box primary">
          <view class="stat-label">联系单</view>
          <view class="stat-value">#{{ contact.id }}</view>
        </view>
        <view class="stat-box">
          <view class="stat-label">需求编号</view>
          <view class="stat-value">#{{ contact.demandId || demandId || '-' }}</view>
        </view>
        <view class="stat-box weak">
          <view class="stat-label">隐私号</view>
          <view class="stat-value small">{{ contact.maskedPhone || '170****0002' }}</view>
        </view>
      </view>

      <view class="focus-note" v-if="lastUpdated">最近刷新 {{ lastUpdated }}</view>

      <view v-if="loading" class="card-state">
        <view class="state-title">正在同步联系状态</view>
        <view class="desc">请稍候，系统正在拉取最新状态。</view>
      </view>
      <view v-else-if="errorText" class="card-state warning-state">
        <view class="state-title">联系信息加载失败</view>
        <view class="desc">{{ errorText }}</view>
      </view>
      <view v-else-if="!contact.id" class="card-state">
        <view class="state-title">暂无待确认联系单</view>
        <view class="desc">你可以返回匹配页查看最新结果，或回到首页发起新需求。</view>
      </view>
      <view v-else class="decision-card">
        <view class="decision-title">{{ contactHeadline }}</view>
        <view class="decision-desc">{{ contactHint }}</view>
        <view v-if="contact.confirmedAt" class="decision-meta">确认时间 {{ contact.confirmedAt }}</view>
        <view class="decision-tip">{{ getDemandStatusDescription('WAIT_FARMER_CONTACT_CONFIRM') }}</view>
      </view>
    </view>

    <view class="card scene-enter delay-2">
      <view class="section-title">后续指引</view>
      <view class="progress-list">
        <view class="progress-item">
          <view class="progress-dot active"></view>
          <view class="progress-body">
            <view class="progress-title">电话/微信沟通</view>
            <view class="desc">直接商定作业时间、价格和具体下地位置。</view>
          </view>
        </view>
        <view class="progress-item">
          <view class="progress-dot" :class="{ active: isConfirmed }"></view>
          <view class="progress-body">
            <view class="progress-title">进入协作</view>
            <view class="desc">确认意向后，系统将为您记录联系历史，方便追溯。</view>
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
import { farmerApi } from '../../api/farmer'
import { ensurePageAccess } from '../../utils/pageAuth'
import { relaunchTo } from '../../utils/pageNav'
import { getDemandStatusDescription } from '../../constants/status'

export default {
  data() {
    return {
      demandId: null,
      contactId: null,
      loading: false,
      errorText: '',
      contact: {},
      pollTimer: null,
      lastUpdated: '',
      actionBusy: false
    }
  },
  computed: {
    normalizedStatus() {
      const raw = this.contact.status || 'WAIT_FARMER_CONFIRM'
      if (raw === 'WAITING_FARMER_CONFIRM') return 'WAIT_FARMER_CONFIRM'
      return raw
    },
    contactStatusText() {
      const map = {
        WAIT_FARMER_CONFIRM: '师傅等您确认',
        CONTACT_ACTIVE: '师傅电话拿到了',
        CONFIRMED: '已确认',
        CLOSED: '已关闭',
        EXPIRED: '已过期'
      }
      return map[this.normalizedStatus] || this.normalizedStatus
    },
    statusTone() {
      if (['CONTACT_ACTIVE', 'CONFIRMED'].includes(this.normalizedStatus)) return 'active'
      if (['CLOSED', 'EXPIRED'].includes(this.normalizedStatus)) return 'closed'
      return 'pending'
    },
    isConfirmed() {
      return ['CONTACT_ACTIVE', 'CONFIRMED'].includes(this.normalizedStatus)
    },
    contactHeadline() {
      if (!this.contact.id) return '正在等候师傅'
      if (this.isConfirmed) return '师傅电话拿到了，可以打电话理了'
      if (['CLOSED', 'EXPIRED'].includes(this.normalizedStatus)) return '这个师傅没扮到'
      return '师傅要来了，您确认一下吗？'
    },
    contactHint() {
      if (this.normalizedStatus === 'WAIT_FARMER_CONFIRM') {
        return '点“确认联系师傅”，平台就把师傅电话发给您；不想要，点“换坚师傅”继续找。'
      }
      if (this.isConfirmed) {
        return '拿到师傅电话了！打电话商量好价格和下地时间。'
      }
      if (['CLOSED', 'EXPIRED'].includes(this.normalizedStatus)) {
        return '这个师傅没扮到，系统会继续帮您找。'
      }
      return '稍等一下，系统正在处理。'
    },
    actionTitle() {
      if (this.loading) return '正在查询状态'
      if (this.errorText) return '查询失败了'
      if (this.isConfirmed) return '师傅电话已拿到'
      if (this.normalizedStatus === 'WAIT_FARMER_CONFIRM') return '师傅等您确认'
      if (['CLOSED', 'EXPIRED'].includes(this.normalizedStatus)) return '这个师傅没扮到'
      return '准备下一步'
    },
    actionSubtitle() {
      if (this.loading) return '稍等一下...'
      if (this.errorText) return '点按钮重试，或者回找机页'
      if (this.isConfirmed) return '打电话商量好然后去看订单'
      if (this.normalizedStatus === 'WAIT_FARMER_CONFIRM') return '快点确认，别让师傅久等'
      if (['CLOSED', 'EXPIRED'].includes(this.normalizedStatus)) return '系统会继续帮您找其他师傅'
      return '继续跟进当前需求'
    },
    mainActionText() {
      if (this.actionBusy) return '处理中...'
      if (this.loading || this.errorText || !this.contact.id) return '刷新一下'
      if (this.isConfirmed) return '拨打师傅电话'
      if (this.normalizedStatus === 'WAIT_FARMER_CONFIRM') return '确认联系师傅'
      if (['CLOSED', 'EXPIRED'].includes(this.normalizedStatus)) return '继续找其他师傅'
      return '刷新一下'
    },
    secondaryActionText() {
      if (this.actionBusy) return '请稍候'
      if (this.loading || this.errorText || !this.contact.id) return '回找机页'
      if (this.normalizedStatus === 'WAIT_FARMER_CONFIRM') return '换坚师傅，继续找'
      if (this.isConfirmed) return '回首页'
      if (['CLOSED', 'EXPIRED'].includes(this.normalizedStatus)) return '回首页'
      return '回找机页'
    }
  },
  onLoad(query) {
    this.contactId = query.contactId
    this.demandId = query.demandId
  },
  onShow() {
    uni.setNavigationBarTitle({ title: '确认联系' })
    if (!ensurePageAccess('FARMER')) return
    this.loadContact()
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
        this.loadContact(false)
      }, 3000)
    },
    stopPolling() {
      if (this.pollTimer) {
        clearInterval(this.pollTimer)
        this.pollTimer = null
      }
    },
    async loadContact(showError = true) {
      this.loading = true
      if (showError) this.errorText = ''
      try {
        const contacts = await farmerApi.listContacts()
        this.contact =
          contacts.find((item) => String(item.id) === String(this.contactId))
          || contacts.find((item) => String(item.demandId) === String(this.demandId))
          || {}
        if (this.contact.id && String(this.contact.id) !== String(this.contactId)) {
          this.contactId = this.contact.id
        }
        this.lastUpdated = new Date().toLocaleTimeString()
        const demandStatus = this.demandId ? await farmerApi.getMatchStatus(this.demandId) : null
        if (demandStatus && demandStatus.currentOrderId) {
          this.stopPolling()
          uni.redirectTo({ url: `/pages/farmer/order-detail?orderId=${demandStatus.currentOrderId}` })
        }
      } catch (error) {
        this.contact = {}
        if (showError) this.errorText = error?.message || '加载联系信息失败'
      } finally {
        this.loading = false
      }
    },
    async confirm() {
      if (!this.contactId || this.actionBusy) return
      this.actionBusy = true
      try {
        const res = await farmerApi.confirmContact(this.contactId)
        this.stopPolling()
        if (res?.orderId) {
          uni.navigateTo({ url: `/pages/farmer/order-detail?orderId=${res.orderId}` })
          return
        }
        uni.showToast({ title: '订单生成中，请稍后刷新', icon: 'none' })
        this.loadContact(false)
      } catch (error) {
        uni.showToast({ title: error?.message || '确认失败', icon: 'none' })
      } finally {
        this.actionBusy = false
      }
    },
    async rejectContact() {
      if (!this.contactId || this.actionBusy) return
      this.actionBusy = true
      try {
        await farmerApi.rejectContact(this.contactId)
        this.stopPolling()
        uni.redirectTo({ url: `/pages/farmer/matching?demandId=${this.demandId}` })
      } catch (error) {
        uni.showToast({ title: error?.message || '操作失败', icon: 'none' })
      } finally {
        this.actionBusy = false
      }
    },
    async openOrder() {
      if (!this.demandId || this.actionBusy) return
      this.actionBusy = true
      try {
        const status = await farmerApi.getMatchStatus(this.demandId)
        if (status.currentOrderId) {
          uni.navigateTo({ url: `/pages/farmer/order-detail?orderId=${status.currentOrderId}` })
          return
        }
      } catch (error) {
      } finally {
        this.actionBusy = false
      }
      uni.showToast({ title: '订单尚未生成', icon: 'none' })
    },
    onMainAction() {
      if (this.actionBusy) return
      if (this.loading || this.errorText || !this.contact.id) {
        this.loadContact()
        return
      }
      if (this.isConfirmed) {
        // 模拟拨打电话
        uni.makePhoneCall({
          phoneNumber: this.contact.maskedPhone || '17000000002',
          success: () => {
            console.log('拨号成功')
            this.openOrder() // 拨号后跳转到记录页
          }
        })
        return
      }
      if (this.normalizedStatus === 'WAIT_FARMER_CONFIRM') {
        this.confirm()
        return
      }
      if (['CLOSED', 'EXPIRED'].includes(this.normalizedStatus)) {
        this.backMatch()
        return
      }
      this.loadContact()
    },
    onSecondaryAction() {
      if (this.actionBusy) return
      if (this.loading || this.errorText || !this.contact.id) {
        this.backMatch()
        return
      }
      if (this.normalizedStatus === 'WAIT_FARMER_CONFIRM') {
        this.rejectContact()
        return
      }
      if (this.isConfirmed || ['CLOSED', 'EXPIRED'].includes(this.normalizedStatus)) {
        this.backHome()
        return
      }
      this.backMatch()
    },
    backMatch() {
      if (!this.demandId) {
        relaunchTo('/pages/farmer/home')
        return
      }
      uni.redirectTo({ url: `/pages/farmer/matching?demandId=${this.demandId}` })
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
.hero-top { display: flex; justify-content: space-between; gap: 20rpx; }
.hero-badge { flex-shrink: 0; align-self: flex-start; padding: 12rpx 20rpx; border-radius: 999rpx; font-weight: 700; }
.hero-badge.tone-pending { background: rgba(211, 149, 22, 0.14); color: #8a6208; }
.hero-badge.tone-active { background: rgba(47, 107, 61, 0.12); color: #2f6b3d; }
.hero-badge.tone-closed { background: rgba(128, 94, 76, 0.14); color: #6f5141; }
.stats-grid { display: grid; grid-template-columns: repeat(3, 1fr); gap: 16rpx; margin: 24rpx 0 12rpx; }
.stat-box { background: rgba(255,255,255,0.56); border-radius: 20rpx; padding: 18rpx; border: 1rpx solid rgba(95, 111, 74, 0.07); }
.stat-box.primary { background: linear-gradient(165deg, rgba(70, 131, 84, 0.16), rgba(255, 255, 255, 0.74)); }
.stat-box.weak { opacity: 0.86; }
.stat-label { font-size: 24rpx; color: #6d7a63; margin-bottom: 10rpx; }
.stat-value { font-size: 32rpx; font-weight: 700; color: #254e31; }
.stat-value.small { font-size: 24rpx; line-height: 1.5; }
.focus-note { margin-bottom: 14rpx; color: #5e705f; }
.card-state { background: rgba(255,255,255,0.54); border-radius: 22rpx; padding: 22rpx; margin-top: 8rpx; }
.warning-state { background: rgba(255, 245, 237, 0.94); }
.state-title { font-size: 32rpx; font-weight: 700; color: #244a2f; margin-bottom: 12rpx; }
.decision-card { padding: 22rpx; border-radius: 24rpx; background: linear-gradient(165deg, rgba(255, 255, 255, 0.9), rgba(242, 248, 238, 0.92)); border: 1rpx solid rgba(82, 100, 67, 0.12); box-shadow: 0 10rpx 24rpx rgba(62, 83, 49, 0.08); margin-top: 8rpx; }
.decision-title { font-size: 32rpx; font-weight: 700; color: #244a2f; margin-bottom: 10rpx; line-height: 1.34; }
.decision-desc { color: #55685a; line-height: 1.7; }
.decision-meta { margin-top: 10rpx; color: #6a7a68; font-size: 24rpx; }
.decision-tip { margin-top: 14rpx; padding: 14rpx 16rpx; border-radius: 16rpx; background: rgba(255, 243, 209, 0.86); color: #7a5f16; font-size: 24rpx; }
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
