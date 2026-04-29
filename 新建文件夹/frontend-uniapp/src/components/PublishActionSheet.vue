<template>
  <view class="publish-action-wrap" v-if="visible">
    <!-- 背景遮罩 -->
    <view class="overlay scene-enter" :class="{ 'fade-out': isHiding }" @click="close"></view>
    
    <!-- 动作面板 -->
    <view class="action-sheet" :class="{ 'slide-down': isHiding, 'scene-enter': !isHiding }">
      <view class="sheet-title">🌿 我要发布...</view>

      <view class="action-cards">
        
        <!-- 求助需求 -->
        <view class="action-card demand-card" @click="goPublishDemand" hover-class="card-hover">
          <view class="card-icon-wrap">
            <text class="card-icon">🌾</text>
            <view class="icon-bg-blur demand-blur"></view>
          </view>
          <view class="card-text">
            <view class="card-title">发布求助需求</view>
            <view class="card-desc">免费发布，等待服务者主动联系您</view>
          </view>
          <view class="card-arrow">›</view>
        </view>

        <!-- 登记服务（有农机可帮助别人） -->
        <view class="action-card rent-card" @click="goPublishRental" hover-class="card-hover">
          <view class="card-icon-wrap">
            <text class="card-icon">🤝</text>
            <view class="icon-bg-blur rent-blur"></view>
          </view>
          <view class="card-text">
             <view class="card-title">登记服务能力</view>
             <view class="card-desc">家里有农机？告诉邻里，助人助己</view>
          </view>
          <view class="card-arrow">›</view>
        </view>

      </view>

      <!-- 底部关闭按钮（仿 iOS） -->
      <view class="close-btn-wrap" @click="close">
         <view class="close-btn">
            <text class="close-icon">×</text>
         </view>
      </view>
    </view>
  </view>
</template>

<script>
export default {
  name: 'PublishActionSheet',
  data() {
    return {
      visible: false,
      isHiding: false
    }
  },
  created() {
    uni.$on('open-publish-sheet', this.show)
  },
  unmounted() {
    uni.$off('open-publish-sheet', this.show)
  },
  methods: {
    show() {
      this.isHiding = false
      this.visible = true
    },
    close() {
      this.isHiding = true
      setTimeout(() => {
        this.visible = false
        this.isHiding = false
      }, 300) // Match CSS transition duration
    },
    goPublishDemand() {
      this.close()
      uni.navigateTo({ url: '/pages/publish/demand' })
    },
    goPublishRental() {
      this.close()
      uni.navigateTo({ url: '/pages/publish/rental' })
    }
  }
}
</script>

<style scoped>
.publish-action-wrap {
  position: fixed;
  top: 0; left: 0; right: 0; bottom: 0;
  z-index: 9999;
  display: flex;
  flex-direction: column;
  justify-content: flex-end;
}

/* ================== OVERLAY ================== */
.overlay {
  position: absolute;
  top: 0; left: 0; right: 0; bottom: 0;
  background: rgba(0, 0, 0, 0.45);
  backdrop-filter: blur(4px);
}

.overlay.scene-enter { animation: fade-in 0.3s ease-out both; }
.overlay.fade-out { animation: fade-out 0.3s ease-out both; }

@keyframes fade-in { from { opacity: 0; } to { opacity: 1; } }
@keyframes fade-out { from { opacity: 1; } to { opacity: 0; } }

/* ================== SHEET ================== */
.action-sheet {
  position: relative;
  background: var(--page-bg);
  border-top-left-radius: 40rpx;
  border-top-right-radius: 40rpx;
  padding: 40rpx 32rpx;
  padding-bottom: env(safe-area-inset-bottom, 40rpx);
  box-shadow: 0 -20rpx 40rpx rgba(0,0,0,0.1);
}

.action-sheet.scene-enter { animation: slide-up 0.3s cubic-bezier(0.175, 0.885, 0.32, 1.275) both; }
.action-sheet.slide-down { animation: slide-down 0.3s cubic-bezier(0.4, 0, 1, 1) both; }

@keyframes slide-up {
  from { transform: translateY(100%); }
  to { transform: translateY(0); }
}

@keyframes slide-down {
  from { transform: translateY(0); }
  to { transform: translateY(100%); }
}

.sheet-title {
  text-align: center;
  font-size: 32rpx;
  font-weight: 800;
  color: var(--text-primary);
  margin-bottom: 40rpx;
}

/* ================== ACTION CARDS ================== */
.action-cards {
  display: flex;
  flex-direction: column;
  gap: 24rpx;
}

.action-card {
  display: flex;
  align-items: center;
  background: white;
  padding: 32rpx;
  border-radius: 28rpx;
  box-shadow: 0 4rpx 12rpx rgba(0,0,0,0.03);
  transition: transform 0.1s;
}

.card-hover {
  transform: scale(0.98);
  background: #f9fafb;
}

.card-icon-wrap {
  position: relative;
  width: 96rpx;
  height: 96rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 24rpx;
}

.card-icon {
  font-size: 56rpx;
  position: relative;
  z-index: 2;
}

.icon-bg-blur {
  position: absolute;
  top: 10rpx; left: 10rpx; right: -10rpx; bottom: -10rpx;
  border-radius: 50%;
  filter: blur(12px);
  z-index: 1;
}

.demand-blur { background: rgba(45, 122, 79, 0.3); }
.rent-blur   { background: rgba(91, 168, 138, 0.3); }

.card-text {
  flex: 1;
}

.card-title {
  font-size: 32rpx;
  font-weight: 800;
  color: var(--text-primary);
  margin-bottom: 6rpx;
}

.card-desc {
  font-size: 22rpx;
  color: var(--text-muted);
}

.card-arrow {
  font-size: 32rpx;
  color: var(--text-soft);
  font-weight: 300;
}

/* ================== CLOSE BUTTON ================== */
.close-btn-wrap {
  margin-top: 40rpx;
  display: flex;
  justify-content: center;
}

.close-btn {
  width: 80rpx;
  height: 80rpx;
  background: white;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 4rpx 12rpx rgba(0,0,0,0.06);
}

.close-btn:active {
  background: #f3f4f6;
  transform: scale(0.9);
}

.close-icon {
  font-size: 48rpx;
  color: var(--text-regular);
  margin-top: -4rpx; /* Visual center tweak */
}
</style>
