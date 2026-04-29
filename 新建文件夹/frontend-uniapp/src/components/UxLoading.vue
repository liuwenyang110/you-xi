<template>
  <view class="ux-loading" :class="{ 'fullscreen': fullscreen }">
    <view class="loading-container">
      <!-- 加载动画 -->
      <view class="loading-spinner" :style="{ width: size, height: size }">
        <view 
          v-for="n in 12" 
          :key="n" 
          class="spinner-blade"
          :style="{
            transform: `rotate(${n * 30}deg)`,
            animationDelay: `${(n - 1) * 0.1}s`
          }"
        ></view>
      </view>
      
      <!-- 加载文本 -->
      <view class="loading-text" v-if="text">{{ text }}</view>
      
      <!-- 自定义插槽 -->
      <slot></slot>
    </view>
    
    <!-- 遮罩层 -->
    <view class="loading-mask" v-if="mask" @click="onMaskClick"></view>
  </view>
</template>

<script>
export default {
  name: 'UxLoading',
  props: {
    // 是否显示
    show: {
      type: Boolean,
      default: true
    },
    // 加载文本
    text: {
      type: String,
      default: ''
    },
    // 加载图标大小
    size: {
      type: String,
      default: '40px'
    },
    // 是否显示遮罩
    mask: {
      type: Boolean,
      default: true
    },
    // 是否全屏
    fullscreen: {
      type: Boolean,
      default: false
    },
    // 点击遮罩是否关闭
    maskClosable: {
      type: Boolean,
      default: false
    }
  },
  methods: {
    onMaskClick() {
      if (this.maskClosable) {
        this.$emit('mask-click')
      }
    }
  }
}
</script>

<style scoped>
.ux-loading {
  position: relative;
  z-index: 9999;
}

.fullscreen {
  position: fixed;
  top: 0;
  left: 0;
  width: 100vw;
  height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
}

.loading-container {
  z-index: 10001;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 20px;
  background: rgba(255, 255, 255, 0.95);
  border-radius: 12px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.loading-spinner {
  position: relative;
  margin-bottom: 12px;
}

.spinner-blade {
  position: absolute;
  left: 44%;
  top: 0;
  width: 12%;
  height: 100%;
  background: var(--primary-color);
  border-radius: 2px;
  opacity: 0;
  animation: spinner-fade 1.2s linear infinite;
}

@keyframes spinner-fade {
  0% {
    opacity: 0;
  }
  100% {
    opacity: 1;
  }
}

.loading-text {
  font-size: 14px;
  color: var(--text-muted);
  margin-top: 8px;
}

.loading-mask {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: rgba(0, 0, 0, 0.3);
  z-index: 10000;
}
</style>