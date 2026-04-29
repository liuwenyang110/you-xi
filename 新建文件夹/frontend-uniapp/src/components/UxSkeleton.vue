<template>
  <view class="ux-skeleton">
    <!-- 骨架屏容器 -->
    <view class="skeleton-container" :class="{ 'animate-pulse': animate }">
      <!-- 自定义插槽，允许自定义骨架结构 -->
      <slot>
        <!-- 默认骨架：矩形 -->
        <view 
          v-for="i in count" 
          :key="i" 
          class="skeleton-item" 
          :style="{
            width: width,
            height: height,
            borderRadius: radius,
            marginBottom: gap
          }"
        ></view>
      </slot>
    </view>
  </view>
</template>

<script>
export default {
  name: 'UxSkeleton',
  props: {
    // 骨架数量
    count: {
      type: Number,
      default: 1
    },
    // 骨架宽度
    width: {
      type: String,
      default: '100%'
    },
    // 骨架高度
    height: {
      type: String,
      default: '20px'
    },
    // 圆角
    radius: {
      type: String,
      default: '4px'
    },
    // 间距
    gap: {
      type: String,
      default: '12px'
    },
    // 是否启用脉冲动画
    animate: {
      type: Boolean,
      default: true
    }
  }
}
</script>

<style scoped>
.ux-skeleton {
  width: 100%;
}

.skeleton-container {
  width: 100%;
}

.skeleton-item {
  background: linear-gradient(90deg, var(--secondary-color) 25%, var(--secondary-strong) 50%, var(--secondary-color) 75%);
  background-size: 200% 100%;
}

.animate-pulse .skeleton-item {
  animation: pulse 1.5s ease-in-out infinite;
}

@keyframes pulse {
  0% {
    background-position: 200% 0;
  }
  100% {
    background-position: -200% 0;
  }
}
</style>