<template>
  <view class="page">
    <view class="card hero-card glass-panel scene-enter">
      <view class="hero-top">
        <view>
          <view class="section-caption">联系请求详情</view>
          <view class="hero-title">{{ formatServiceName(detail.serviceName, detail.id) }}</view>
          <view class="hero-subtitle">以下信息为决策参考，确认后区瞌会展示联系方式。</view>
        </view>
        <ux-capsule :label="ownerStatusLabel(detail.status)" :tone="statusTone(detail.status)" icon-kind="pending" :strong="true" />
      </view>

      <view v-if="detail.id" class="decision-deck">
        <view class="decision-item">
          <view class="decision-label">决策建议</view>
          <view class="decision-value">{{ decisionHeadline }}</view>
        </view>
        <view class="decision-item">
          <view class="decision-label">服务地点</view>
          <view class="decision-value small">{{ formatVillageName(detail.villageName, detail.id) }}</view>
        </view>
        <view class="decision-item">
          <view class="decision-label">优先圈层</view>
          <view class="decision-value small">{{ distanceLayerLabel(detail.distanceLayer) }}</view>
        </view>
        <view class="decision-item">
          <view class="decision-label">预计距离</view>
          <view class="decision-value small">{{ detail.distanceKm ? `${detail.distanceKm} km` : '待计算' }}</view>
        </view>
      </view>

      <view class="dispatch-overview" v-if="detail.id">
        <view class="dispatch-chip">
          <view class="dispatch-chip-label">派单编号</view>
          <view class="dispatch-chip-value">#{{ detail.id }}</view>
        </view>
        <view class="dispatch-chip">
          <view class="dispatch-chip-label">需求编号</view>
          <view class="dispatch-chip-value">{{ detail.demandId ? '#' + detail.demandId : '-' }}</view>
        </view>
        <view class="dispatch-chip">
          <view class="dispatch-chip-label">当前状态</view>
          <view class="dispatch-chip-value small">{{ ownerStatusLabel(detail.status) }}</view>
        </view>
        <view class="dispatch-chip">
          <view class="dispatch-chip-label">最近刷新</view>
          <view class="dispatch-chip-value small">{{ lastUpdated || '--:--:--' }}</view>
        </view>
      </view>

      <view v-if="loading" class="card-state">
        <view class="state-title">正在加载派单详情</view>
        <view class="desc">请稍候，系统正在同步最新状态。</view>
      </view>
      <view v-else-if="errorText" class="card-state warning-state">
        <view class="state-title">派单详情加载失败</view>
        <view class="desc">{{ errorText }}</view>
      </view>
      <view v-else-if="!detail.id" class="card-state">
        <view class="state-title">当前没有可查看派单</view>
        <view class="desc">返回农机主首页查看待响应任务。</view>
      </view>
      <view v-else class="status-card">
        <view class="status-title">{{ detailHeadline }}</view>
        <view class="desc">{{ statusHint }}</view>
        <view class="status-tags">
          <ux-capsule :label="ownerStatusLabel(detail.status)" :tone="statusTone(detail.status)" icon-kind="pending" />
          <view class="tag light-tag" v-if="detail.distanceLayer">圈层 {{ distanceLayerLabel(detail.distanceLayer) }}</view>
          <view class="tag light-tag" v-if="detail.distanceKm">距离 {{ detail.distanceKm }} km</view>
          <view class="tag light-tag" v-if="detail.distanceSource">来源 {{ detail.distanceSource }}</view>
        </view>
        <view class="decision-note">{{ statusActionHint }}</view>
        <view class="tip-strip" v-if="resultText">{{ resultText }}</view>
        <view class="decision-actions">
          <view class="decision-action-pill primary-action" v-if="detail.status === 'PENDING_RESPONSE'" @click="accept">确认接单</view>
          <view class="decision-action-pill reject-action" v-if="detail.status === 'PENDING_RESPONSE'" @click="reject">拒绝此单</view>
          <view class="decision-action-pill" @click="loadDetail">刷新状态</view>
          <view class="decision-action-pill" @click="backHome">返回首页</view>
        </view>
      </view>
    </view>

    <view class="card scene-enter delay-2">
      <view class="section-title">处理步骤</view>
      <view class="progress-list">
        <view class="progress-item">
          <view class="progress-dot active"></view>
          <view class="progress-body">
            <view class="progress-title">看联系请求信息</view>
            <view class="desc">确认服务地点和联系方的需求再决定是否接单。</view>
          </view>
        </view>
        <view class="progress-item">
          <view class="progress-dot" :class="{ active: detail.status !== 'PENDING_RESPONSE' }"></view>
          <view class="progress-body">
            <view class="progress-title">确认接单</view>
            <view class="desc">接单后双方将建立联系，展示脚敏手机号给农户查看。</view>
          </view>
        </view>
        <view class="progress-item">
          <view class="progress-dot" :class="{ active: detail.status === 'CONTACT_OPENED' }"></view>
          <view class="progress-body">
            <view class="progress-title">联系已建立，线下推进</view>
            <view class="desc">联系建立后可直接与农户电话沟通，确认时间和价格。</view>
          </view>
        </view>
      </view>
    </view>
  </view>
</template>

<script>
import UxCapsule from '../../components/UxCapsule.vue'
import { ownerApi } from '../../api/owner'
import { ensurePageAccess } from '../../utils/pageAuth'
import { relaunchTo, safeBack } from '../../utils/pageNav'

export default {
  components: { UxCapsule },
  data() {
    return {
      attemptId: null,
      from: '',
      loading: false,
      errorText: '',
      detail: {},
      resultText: '',
      lastUpdated: ''
    }
  },
  computed: {
    detailHeadline() {
      if (!this.detail.id) return '等待新的派单'
      if (this.detail.status === 'PENDING_RESPONSE') return '请尽快决定是否接单'
      if (this.detail.status === 'WAIT_FARMER_CONFIRM' || this.detail.status === 'WAITING_FARMER_CONFIRM') return '已接单，等待农户确认联系'
      if (this.detail.status === 'CONTACT_OPENED') return '联系已建立，可推进后续服务'
      if (this.detail.status === 'FARMER_REJECTED') return '农户本轮选择暂不联系'
      if (this.detail.status === 'OWNER_TIMEOUT') return '当前派单已超时关闭'
      if (this.detail.status === 'OWNER_REJECTED') return '当前派单已拒绝'
      if (this.detail.status === 'FARMER_TIMEOUT') return '农户确认联系已超时'
      if (this.detail.status === 'DEMAND_CANCELLED') return '需求已取消'
      return '派单状态已更新'
    },
    decisionHeadline() {
      const raw = this.detail.status
      if (raw === 'PENDING_RESPONSE') return '建议立即处理，避免超时流转'
      if (raw === 'WAIT_FARMER_CONFIRM' || raw === 'WAITING_FARMER_CONFIRM') return '已接单，等待农户确认'
      if (raw === 'CONTACT_OPENED') return '联系已建立，可推进履约'
      if (raw === 'OWNER_REJECTED' || raw === 'OWNER_TIMEOUT') return '本轮已关闭，返回首页处理下一单'
      if (raw === 'FARMER_REJECTED' || raw === 'FARMER_TIMEOUT') return '农户侧未完成确认，系统将继续流转'
      if (raw === 'DEMAND_CANCELLED') return '需求已取消，本轮结束'
      return '请刷新状态'
    },
    statusHint() {
      const raw = this.detail.status
      if (raw === 'PENDING_RESPONSE') return '若长时间未处理，系统会自动关闭当前派单并继续尝试派发。'
      if (raw === 'WAIT_FARMER_CONFIRM' || raw === 'WAITING_FARMER_CONFIRM') return '您已完成接单，当前正在等待农户确认是否建立正式联系。'
      if (raw === 'FARMER_REJECTED') return '农户本轮选择暂不联系，系统已结束当前派单。'
      if (raw === 'OWNER_TIMEOUT') return '该派单因超时未处理而自动关闭。'
      if (raw === 'OWNER_REJECTED') return '您已主动拒绝本次派单。'
      if (raw === 'FARMER_TIMEOUT') return '农户长时间未确认联系，系统已自动关闭本轮联系。'
      if (raw === 'CONTACT_OPENED') return '双方已建立联系，后续可在订单侧继续推进服务协商。'
      if (raw === 'DEMAND_CANCELLED') return '农户已取消当前需求，本轮派单结束。'
      return '请关注最新派单状态。'
    },
    statusActionHint() {
      const raw = this.detail.status
      if (raw === 'PENDING_RESPONSE') return '建议在 5 分钟内完成接单决策，避免派单超时被系统自动流转。'
      if (raw === 'WAIT_FARMER_CONFIRM' || raw === 'WAITING_FARMER_CONFIRM') return '您已完成接单，下一步只需等待农户确认联系。'
      if (raw === 'CONTACT_OPENED') return '联系已建立，可转去订单侧继续推进协商与执行。'
      if (raw === 'OWNER_REJECTED' || raw === 'OWNER_TIMEOUT') return '本次派单已结束，建议回首页处理下一条任务。'
      if (raw === 'FARMER_REJECTED' || raw === 'FARMER_TIMEOUT') return '农户侧未建立联系，系统通常会继续回流派发。'
      if (raw === 'DEMAND_CANCELLED') return '需求已取消，不再需要操作。'
      return '可点击“刷新派单”查看最新状态。'
    }
  },
  onLoad(query) {
    this.attemptId = query.attemptId
    this.from = query.from || ''
  },
  onShow() {
    uni.setNavigationBarTitle({ title: '联系请求详情' })
    if (!ensurePageAccess('OWNER')) return
    this.loadDetail()
  },
  onPullDownRefresh() {
    if (!ensurePageAccess('OWNER')) {
      uni.stopPullDownRefresh()
      return
    }
    this.loadDetail().finally(() => uni.stopPullDownRefresh())
  },
  methods: {
    ownerStatusLabel(status) {
      const map = {
        PENDING_RESPONSE: '待响应',
        WAIT_FARMER_CONFIRM: '待农户确认',
        WAITING_FARMER_CONFIRM: '待农户确认',
        CONTACT_OPENED: '已建立联系',
        OWNER_TIMEOUT: '已超时',
        OWNER_REJECTED: '已拒绝',
        FARMER_REJECTED: '农户暂不联系',
        FARMER_TIMEOUT: '农户确认超时',
        DEMAND_CANCELLED: '需求已取消'
      }
      return map[status] || status || '未知状态'
    },
    statusToneClass(status) {
      if (status === 'PENDING_RESPONSE') return 'tone-warn'
      if (status === 'WAIT_FARMER_CONFIRM' || status === 'WAITING_FARMER_CONFIRM') return 'tone-info'
      if (status === 'CONTACT_OPENED') return 'tone-success'
      if (status === 'OWNER_REJECTED' || status === 'OWNER_TIMEOUT' || status === 'FARMER_TIMEOUT' || status === 'DEMAND_CANCELLED') return 'tone-muted'
      return ''
    },
    statusTone(status) {
      if (status === 'PENDING_RESPONSE') return 'warn'
      if (status === 'WAIT_FARMER_CONFIRM' || status === 'WAITING_FARMER_CONFIRM') return 'info'
      if (status === 'CONTACT_OPENED') return 'success'
      if (status === 'OWNER_REJECTED' || status === 'OWNER_TIMEOUT' || status === 'FARMER_TIMEOUT' || status === 'DEMAND_CANCELLED') return 'closed'
      return 'neutral'
    },
    distanceLayerLabel(value) {
      if (!value) return '未分层'
      if (value === 'L1' || value === '近距离优先') return '近距离优先'
      if (value === 'L2' || value === '扩展候选') return '扩展候选'
      if (value === 'L3' || value === '兜底候选') return '兜底候选'
      return value
    },
    formatVillageName(value, fallbackId) {
      const raw = String(value || '').trim()
      if (!raw || raw === '???') return fallbackId ? `地点待完善 · 派单 #${fallbackId}` : '待补全'
      const lower = raw.toLowerCase()
      const aliasMap = {
        'donghe village': '东河村',
        'hangzhou east station': '杭州东站'
      }
      return aliasMap[lower] || raw
    },
    formatServiceName(value, fallbackId) {
      const raw = String(value || '').trim()
      if (!raw) return fallbackId ? `派单 #${fallbackId}` : '当前派单'
      const lower = raw.toLowerCase()
      const aliasMap = {
        'rice harvest service': '水稻收割服务',
        'general tractor service': '通用农机作业服务'
      }
      return aliasMap[lower] || raw
    },
    async loadDetail() {
      if (!this.attemptId) return
      this.loading = true
      this.errorText = ''
      try {
        this.detail = await ownerApi.dispatchDetail(this.attemptId)
        this.lastUpdated = new Date().toLocaleTimeString()
      } catch (error) {
        this.detail = {}
        this.errorText = (error && error.message) || '加载派单详情失败'
      } finally {
        this.loading = false
      }
    },
    async accept() {
      try {
        const res = await ownerApi.accept(this.attemptId)
        this.resultText = res && res.contactSession
          ? `已创建联系单 #${res.contactSession.id}，当前等待农户确认联系。`
          : '接单成功。'
        await this.loadDetail()
        uni.showToast({ title: '接单成功', icon: 'success' })
      } catch (error) {
        uni.showToast({ title: (error && error.message) || '操作失败', icon: 'none' })
      }
    },
    async reject() {
      try {
        const res = await ownerApi.reject(this.attemptId)
        this.resultText = res && res.nextMatchAttempt
          ? `已拒绝当前派单，系统已流转到下一位候选（尝试单 #${res.nextMatchAttempt.id}）。`
          : '已拒绝当前派单，当前没有下一位可派发候选。'
        await this.loadDetail()
        uni.showToast({ title: '已拒绝', icon: 'success' })
      } catch (error) {
        uni.showToast({ title: (error && error.message) || '操作失败', icon: 'none' })
      }
    },
    backHome() {
      if (this.from === 'home') {
        safeBack('/pages/operator/zone-home')
        return
      }
      relaunchTo('/pages/operator/zone-home')
    }
  }
}
</script>

<style>
.hero-top { display: flex; justify-content: space-between; gap: 20rpx; }
.hero-badge { flex-shrink: 0; align-self: flex-start; padding: 12rpx 20rpx; border-radius: 999rpx; background: rgba(47, 107, 61, 0.10); color: #2f6b3d; font-weight: 700; }
.decision-deck { display: grid; grid-template-columns: repeat(2, 1fr); gap: 12rpx; margin: 22rpx 0 14rpx; }
.decision-item { padding: 14rpx; border-radius: 18rpx; background: rgba(255,255,255,0.72); border: 1rpx solid rgba(86, 106, 77, 0.12); }
.decision-label { font-size: 20rpx; color: #73836e; margin-bottom: 8rpx; }
.decision-value { font-size: 26rpx; font-weight: 700; color: #264a33; line-height: 1.35; }
.decision-value.small { font-size: 24rpx; font-weight: 600; }
.dispatch-overview { display: grid; grid-template-columns: repeat(2, 1fr); gap: 14rpx; margin: 0 0 16rpx; }
.dispatch-chip { padding: 16rpx 18rpx; border-radius: 22rpx; background: rgba(255,255,255,0.72); border: 1rpx solid rgba(90, 80, 48, 0.08); box-shadow: 0 10rpx 20rpx rgba(81, 63, 25, 0.04); }
.dispatch-chip-label { font-size: 22rpx; color: #74836f; margin-bottom: 8rpx; }
.dispatch-chip-value { font-size: 30rpx; font-weight: 800; color: #244a2f; line-height: 1.28; }
.dispatch-chip-value.small { font-size: 26rpx; }
.status-card, .card-state { background: rgba(255,255,255,0.54); border-radius: 22rpx; padding: 22rpx; margin-top: 8rpx; }
.warning-state { background: rgba(255, 245, 237, 0.94); }
.state-title, .status-title { font-size: 32rpx; font-weight: 700; color: #244a2f; margin-bottom: 12rpx; }
.status-tags { display: flex; flex-wrap: wrap; gap: 10rpx; margin-top: 14rpx; }
.decision-note { margin-top: 12rpx; color: #5f715f; font-size: 24rpx; line-height: 1.65; }
.tip-strip { margin-top: 16rpx; padding: 16rpx 18rpx; border-radius: 18rpx; background: rgba(235, 247, 230, 0.92); color: #315f39; }
.decision-actions { display: flex; flex-wrap: wrap; gap: 12rpx; margin-top: 14rpx; }
.decision-action-pill { padding: 14rpx 18rpx; border-radius: 999rpx; background: rgba(255,255,255,0.84); color: #2d5939; border: 1rpx solid rgba(86, 104, 74, 0.14); font-size: 22rpx; }
.primary-action { background: linear-gradient(135deg, #2f6b3d, #3f8750); color: #fff; border-color: rgba(52, 104, 63, 0.26); }
.reject-action { background: rgba(255, 240, 226, 0.94); color: #8a4b15; border-color: rgba(176, 117, 63, 0.32); }
.progress-list { margin-top: 18rpx; }
.progress-item { display: flex; gap: 18rpx; padding: 18rpx 0; }
.progress-item + .progress-item { border-top: 1rpx solid rgba(92, 108, 82, 0.10); }
.progress-dot { width: 22rpx; height: 22rpx; border-radius: 999rpx; margin-top: 10rpx; background: rgba(120, 133, 112, 0.28); flex-shrink: 0; }
.progress-dot.active { background: linear-gradient(135deg, #2f6b3d, #467e50); box-shadow: 0 0 0 8rpx rgba(47, 107, 61, 0.12); }
.progress-title { font-size: 28rpx; font-weight: 700; color: #294633; margin-bottom: 10rpx; }
.tone-warn { background: rgba(255, 241, 210, 0.92); color: #8d5d00; }
.tone-info { background: rgba(225, 241, 255, 0.92); color: #235f8b; }
.tone-success { background: rgba(226, 248, 228, 0.92); color: #23653a; }
.tone-muted { background: rgba(236, 237, 233, 0.92); color: #626b61; }
</style>
