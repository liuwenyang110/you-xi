<template>
  <view class="custom-tab-bar" :class="{ 'elder-mode-tab': isElderMode }">
    <view class="tab-items-container">
      
      <!-- 1. 首页 -->
      <view class="tab-item" :class="{ active: current === 'home' }" @click="switchTab('home', '/pages/farmer/home')">
        <text class="tab-icon">{{ current === 'home' ? '🏠' : '🛖' }}</text>
        <text class="tab-text">找机</text>
      </view>

      <!-- 2. 社区 -->
      <view class="tab-item" :class="{ active: current === 'community' }" @click="switchTab('community', '/pages/community/index')">
        <text class="tab-icon">{{ current === 'community' ? '👥' : '👤' }}</text>
        <text class="tab-text">村社</text>
      </view>

      <!-- 3. 中间大加号 (发布动作) -->
      <view class="tab-item action-center-item" @click="triggerPublish">
        <view class="plus-btn-shadow">
          <view class="plus-btn-core">
             <text class="plus-icon">+</text>
          </view>
        </view>
      </view>

      <!-- 4. 订单/消息 -->
      <view class="tab-item" :class="{ active: current === 'orders' }" @click="switchTab('orders', '/pages/orders/index')">
        <text class="tab-icon">{{ current === 'orders' ? '📋' : '📄' }}</text>
        <text class="tab-text">订单</text>
      </view>

      <!-- 5. 我的 -->
      <view class="tab-item" :class="{ active: current === 'profile' }" @click="switchTab('profile', '/pages/profile/index')">
        <text class="tab-icon">{{ current === 'profile' ? '🧑‍🌾' : '🚜' }}</text>
        <text class="tab-text">我的</text>
      </view>

    </view>
  </view>
</template>

<script>
import { uiStore } from '../store/ui'

export default {
  name: 'CustomTabBar',
  props: {
    current: {
      type: String,
      default: 'home'
    }
  },
  computed: {
    isElderMode() {
      return uiStore.mode === 'ELDER'
    }
  },
  methods: {
    switchTab(tabId, url) {
      if (this.current === tabId) return
      // We use reLaunch or navigateTo to bypass native tabbar restrictions
      // In a real app we'd configure raw pages.json, here we simulate it via normal routing
      uni.reLaunch({ url })
    },
    triggerPublish() {
      // Broadcast the action to open the global publish sheet
      uni.$emit('open-publish-sheet')
    }
  }
}
</script>

<style scoped>
.custom-tab-bar {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  height: 120rpx;
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  box-shadow: 0 -4rpx 30rpx rgba(0, 0, 0, 0.05);
  z-index: 999;
  padding-bottom: env(safe-area-inset-bottom, 20px);
}

.tab-items-container {
  display: flex;
  justify-content: space-around;
  align-items: center;
  height: 100%;
}

.tab-item {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 100%;
  color: var(--text-muted);
  transition: all 0.2s;
}

.tab-item:active {
  opacity: 0.7;
}

.tab-item.active {
  color: var(--primary-color);
}

.tab-icon {
  font-size: 40rpx;
  margin-bottom: 6rpx;
  transition: transform 0.2s;
}

.tab-item.active .tab-icon {
  transform: scale(1.15);
}

.tab-text {
  font-size: 22rpx;
  font-weight: 700;
}

/* ================== CENTER ACTION BUTTON ================== */
.action-center-item {
  position: relative;
  top: -24rpx;
}

.plus-btn-shadow {
  width: 100rpx;
  height: 100rpx;
  border-radius: 50%;
  background: white;
  padding: 8rpx;
  box-shadow: 0 -8rpx 16rpx rgba(0, 0, 0, 0.05); /* lifted shadow on top */
}

.plus-btn-core {
  width: 100%;
  height: 100%;
  background: linear-gradient(135deg, #10B981, #059669);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 8rpx 16rpx rgba(16, 185, 129, 0.3);
  transition: transform 0.2s cubic-bezier(0.68, -0.55, 0.265, 1.55);
}

.plus-btn-core:active {
  transform: scale(0.9);
}

.plus-icon {
  color: white;
  font-size: 56rpx;
  font-weight: 300;
  line-height: 1;
  margin-top: -6rpx;
}

/* ================== ELDER MODE ================== */
.elder-mode-tab {
  height: 140rpx;
  background: #fff;
  border-top: 4rpx solid #c8e6c9;
}
.elder-mode-tab .tab-icon { font-size: 52rpx; }
.elder-mode-tab .tab-text { font-size: 28rpx; }
.elder-mode-tab .plus-btn-shadow { width: 120rpx; height: 120rpx; top: -10rpx; }
.elder-mode-tab .plus-icon { font-size: 68rpx; }
</style>
