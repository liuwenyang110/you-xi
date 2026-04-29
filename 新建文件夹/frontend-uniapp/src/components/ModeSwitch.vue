<template>
  <view class="mode-shell card glass-panel scene-enter lift-card soft-panel">
    <view class="mode-copy">
      <view class="section-caption">界面模式</view>
      <view class="mode-title">显示模式</view>
      <view class="desc">当前为 {{ uiStore.mode === 'ELDER' ? '老年模式' : '正常模式' }}，可随时切换更适合的阅读与操作体验。</view>
    </view>
    <view class="mode-actions">
      <view class="mode-pill" :class="{ active: uiStore.mode === 'NORMAL' }">标准</view>
      <view class="mode-pill" :class="{ active: uiStore.mode === 'ELDER' }">老年</view>
      <view class="secondary-btn mode-btn" @click="toggle">{{ uiStore.mode === 'ELDER' ? '切回正常模式' : '切换老年模式' }}</view>
    </view>
  </view>
</template>

<script>
import { uiStore } from '../store/ui'
import { authApi } from '../api/auth'

export default {
  data() {
    return {
      uiStore
    }
  },
  methods: {
    async toggle() {
      uiStore.toggleMode()
      if (uiStore.token) {
        try {
          await authApi.switchUiMode({ uiMode: uiStore.mode })
        } catch (error) {
        }
      }
      this.$forceUpdate()
    }
  }
}
</script>

<style scoped>
.mode-shell {
  display: flex;
  justify-content: space-between;
  gap: 20rpx;
  align-items: center;
}
.mode-copy {
  flex: 1;
}
.mode-title {
  font-size: 34rpx;
  font-weight: 700;
  color: var(--text-primary);
  margin-bottom: 8rpx;
}
.mode-actions {
  display: flex;
  flex-wrap: wrap;
  gap: 12rpx;
  justify-content: flex-end;
  align-items: center;
}
.mode-pill {
  padding: 10rpx 18rpx;
  border-radius: 999rpx;
  background: rgba(255, 255, 255, 0.5);
  color: var(--text-muted);
  font-size: 24rpx;
  font-weight: 700;
}
.mode-pill.active {
  background: rgba(16, 185, 129, 0.12);
  color: var(--primary-strong);
}
.mode-btn {
  margin-top: 0;
  width: 240rpx;
}
</style>
