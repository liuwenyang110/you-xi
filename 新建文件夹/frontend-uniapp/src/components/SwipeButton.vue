<template>
  <view class="swipe-btn-wrap" id="swipe-container" :style="{ background: isSuccess ? successColor : bg }">
    <view class="swipe-bg-text" :class="{'fade-out': isSuccess}">{{ text }}</view>
    
    <view class="swipe-thumb" 
          @touchstart="onTouchStart" 
          @touchmove.stop.prevent="onTouchMove" 
          @touchend="onTouchEnd"
          :style="{ transform: `translateX(${currentX}px)`, transition: isDragging ? 'none' : 'transform 0.3s ease-out' }">
          <text class="thumb-icon">{{ isSuccess ? '✓' : '>>' }}</text>
    </view>

    <!-- Overlay when success -->
    <view class="success-overlay scene-enter" v-if="isSuccess">
       <text class="success-text">{{ successText }}</text>
    </view>
  </view>
</template>

<script>
export default {
  name: 'SwipeButton',
  props: {
    text: { type: String, default: '右滑确认联系' },
    successText: { type: String, default: '已获取联系方式' },
    bg: { type: String, default: '#374151' },
    successColor: { type: String, default: '#2D7A4F' }
  },
  data() {
    return {
      isDragging: false,
      startX: 0,
      currentX: 0,
      maxX: 0,
      isSuccess: false
    }
  },
  mounted() {
    this.calculateMaxX()
  },
  methods: {
    calculateMaxX() {
      // Create a selector query to find component width
      const query = uni.createSelectorQuery().in(this)
      query.select('#swipe-container').boundingClientRect(data => {
        if (data) {
           // Thumb width is ~120rpx, assume ratio. 
           // We'll calculate roughly ContainerWidth - ThumbWidth
           // In uniapp, it's safer to get the px width of the container.
           const containerW = data.width
           const thumbW = containerW * 0.2 // roughly 20%
           this.maxX = containerW - thumbW - 8 // 8px padding
        } else {
           this.maxX = 250 // fallback px
        }
      }).exec()
    },
    onTouchStart(e) {
      if (this.isSuccess) return
      this.isDragging = true
      this.startX = e.touches[0].clientX - this.currentX
    },
    onTouchMove(e) {
      if (!this.isDragging || this.isSuccess) return
      let x = e.touches[0].clientX - this.startX
      if (x < 0) x = 0
      if (x > this.maxX) x = this.maxX
      this.currentX = x
    },
    onTouchEnd() {
      if (!this.isDragging || this.isSuccess) return
      this.isDragging = false
      
      // If swiped more than 80% to the right, trigger success
      if (this.currentX > this.maxX * 0.8) {
         this.currentX = this.maxX
         this.onSuccess()
      } else {
         // Snap back
         this.currentX = 0
         // Provide haptic feedback for failed attempt
         uni.vibrateShort()
      }
    },
    onSuccess() {
      this.isSuccess = true
      uni.vibrateLong()
      setTimeout(() => {
         this.$emit('success')
         // Optionally reset after emitting
         // this.reset()
      }, 500)
    },
    reset() {
       this.isSuccess = false;
       this.currentX = 0;
    }
  }
}
</script>

<style scoped>
.swipe-btn-wrap {
  width: 100%;
  height: 120rpx;
  border-radius: 999rpx;
  position: relative;
  display: flex;
  align-items: center;
  padding: 8rpx;
  box-sizing: border-box;
  overflow: hidden;
  transition: background 0.3s ease;
  box-shadow: inset 0 4rpx 10rpx rgba(0,0,0,0.2);
}

.swipe-bg-text {
  position: absolute;
  width: 100%;
  text-align: center;
  font-size: 34rpx;
  font-weight: 800;
  color: rgba(255, 255, 255, 0.7);
  letter-spacing: 4rpx;
  pointer-events: none;
  transition: opacity 0.3s;
}
.swipe-bg-text.fade-out { opacity: 0; }

.swipe-thumb {
  width: 20%;
  min-width: 104rpx;
  height: 104rpx;
  border-radius: 999rpx;
  background: white;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 4rpx 12rpx rgba(0,0,0,0.2);
  position: relative;
  z-index: 2;
  transition: transform 0.3s ease-out; /* overwritten by inline style when dragging */
}

.thumb-icon {
  font-size: 36rpx;
  font-weight: 800;
  color: var(--primary-strong);
}

.success-overlay {
  position: absolute;
  top: 0; left: 0; right: 0; bottom: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 3;
}
.success-text {
  color: white;
  font-size: 36rpx;
  font-weight: 800;
  text-shadow: 0 4rpx 8rpx rgba(0,0,0,0.2);
}
</style>
