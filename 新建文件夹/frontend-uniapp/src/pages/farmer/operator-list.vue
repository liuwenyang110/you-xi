<template>
  <view class="operator-list-page">
    <view class="page-header">
      <text class="page-title">本片区农机手</text>
      <text class="page-sub">{{ zoneName }} · 共 {{ operatorCount }} 名机手</text>
    </view>

    <!-- 农机分类统计 -->
    <view class="stats-chips" v-if="machineryStats.length">
      <view v-for="s in machineryStats" :key="s.categoryName" class="stat-chip">
        <text>{{ s.icon }} {{ s.categoryName }} {{ s.cnt }}台</text>
      </view>
    </view>

    <!-- 提示：仅本片区可见 -->
    <view class="privacy-tip">
      <text class="tip-icon"></text>
      <text class="tip-text">联系方式仅本片区农户可见，请文明使用</text>
    </view>

    <!-- 农机手列表 -->
    <view v-if="operators.length">
      <view
        v-for="op in operators"
        :key="op.id"
        class="operator-card"
        @click="gotoDetail(op)"
      >
        <view class="op-avatar">
          <text class="op-avatar-icon"></text>
        </view>
        <view class="op-info">
          <view class="op-name-row">
            <text class="op-name">{{ op.realName || '农机手' + op.id }}</text>
            <view class="op-verified" v-if="op.verified">
              <text>✓ 已认证</text>
            </view>
          </view>
          <!-- 这里展示机械信息（需要从另外接口获取或在operator对象中携带） -->
          <text class="op-machinery-hint">点击查看农机详情与联系方式</text>
        </view>
        <text class="op-arrow">›</text>
      </view>
    </view>

    <!-- 空状态 -->
    <view class="empty-state" v-if="operators.length === 0 && !loading">
      <text class="empty-icon"></text>
      <text class="empty-title">本片区暂时没有农机手</text>
      <text class="empty-tip">可向亲友推广，邀请农机手入驻本片区</text>
    </view>
  </view>
</template>

<script>
import { getZoneOperators } from '@/api/v3/user'
import { useV3UserStore } from '@/stores/v3/userStore'
import { useV3ZoneStore } from '@/stores/v3/zoneStore'

export default {
  name: 'OperatorList',
  data() {
    return {
      operators: [],
      loading: false,
    }
  },
  computed: {
    zoneName() { return useV3ZoneStore().zoneName },
    operatorCount() { return useV3ZoneStore().operatorCount },
    machineryStats() { return useV3ZoneStore().machineryStats || [] },
    zoneId() { return useV3UserStore().zoneId },
  },
  async onLoad() {
    await this.loadOperators()
  },
  async onPullDownRefresh() {
    await this.loadOperators()
    uni.stopPullDownRefresh()
  },
  methods: {
    async loadOperators() {
      if (!this.zoneId) return
      this.loading = true
      try {
        const res = await getZoneOperators(this.zoneId)
        if (res.code === 0) this.operators = res.data
      } catch (e) {
        uni.showToast({ title: '加载失败', icon: 'none' })
      } finally {
        this.loading = false
      }
    },
    gotoDetail(op) {
      uni.navigateTo({ url: `/pages/farmer/operator-detail?operatorId=${op.id}` })
    }
  }
}
</script>

<style scoped>
.operator-list-page { background: var(--page-bg); min-height: 100vh; min-height: 100dvh; }
.page-header {
  background: var(--gradient-primary);
  padding: 80rpx 40rpx 40rpx;
}
.page-title { display: block; font-size: 40rpx; font-weight: var(--font-bold); color: #fff; }
.page-sub { display: block; font-size: var(--text-base); color: rgba(255,255,255,0.8); margin-top: 8rpx; }

.stats-chips {
  display: flex;
  flex-wrap: wrap;
  gap: 16rpx;
  padding: 20rpx 24rpx;
}
.stat-chip {
  background: var(--surface-deep);
  border-radius: var(--radius-full);
  padding: 12rpx 28rpx;
  font-size: var(--text-sm);
  color: var(--primary-strong);
  box-shadow: var(--shadow-sm);
}

.privacy-tip {
  display: flex;
  align-items: center;
  background: var(--color-info-bg);
  margin: 0 24rpx 16rpx;
  border-radius: var(--radius-lg);
  padding: 16rpx 20rpx;
}
.tip-icon { font-size: var(--text-base); margin-right: 10rpx; }
.tip-text { font-size: var(--text-xs); color: var(--color-info); }

.operator-card {
  display: flex;
  align-items: center;
  background: var(--surface-deep);
  margin: 0 24rpx 16rpx;
  border-radius: var(--radius-lg);
  padding: 28rpx 24rpx;
  box-shadow: var(--shadow-sm);
}
.op-avatar {
  width: 88rpx;
  height: 88rpx;
  background: linear-gradient(135deg, rgba(16,185,129,0.1), rgba(16,185,129,0.2));
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 20rpx;
  flex-shrink: 0;
}
.op-avatar-icon { font-size: 48rpx; }
.op-info { flex: 1; }
.op-name-row { display: flex; align-items: center; margin-bottom: 8rpx; }
.op-name { font-size: var(--text-lg); font-weight: var(--font-bold); color: var(--text-primary); margin-right: 12rpx; }
.op-verified {
  background: var(--color-success-bg);
  border-radius: var(--radius-xl);
  padding: 2rpx 12rpx;
  font-size: var(--text-xs);
  color: var(--primary-strong);
}
.op-machinery-hint { font-size: var(--text-sm); color: var(--text-soft); }
.op-arrow { font-size: 44rpx; color: var(--text-soft); }

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 100rpx 40rpx;
}
.empty-icon { font-size: 100rpx; margin-bottom: 24rpx; }
.empty-title { font-size: var(--text-lg); color: var(--text-muted); margin-bottom: 12rpx; }
.empty-tip { font-size: var(--text-sm); color: var(--text-soft); text-align: center; line-height: 1.6; }
</style>
