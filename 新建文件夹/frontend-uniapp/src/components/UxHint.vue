<template>
  <view class="ux-hint" :class="`tone-${tone}`">
    <ux-glyph class="ux-hint-icon" :kind="iconKind" :size="46" />
    <view class="ux-hint-copy">
      <view class="ux-hint-title">{{ title }}</view>
      <view class="ux-hint-text">{{ text }}</view>
    </view>
  </view>
</template>

<script>
import UxGlyph from './UxGlyph.vue'

export default {
  components: { UxGlyph },
  props: {
    title: {
      type: String,
      default: ''
    },
    text: {
      type: String,
      default: ''
    },
    tone: {
      type: String,
      default: 'neutral'
    },
    iconKind: {
      type: String,
      default: 'hint'
    }
  }
}
</script>

<style scoped>
.ux-hint {
  display: flex;
  align-items: flex-start;
  gap: 12rpx;
  padding: 16rpx;
  border-radius: 22rpx;
  border: 1rpx solid rgba(101, 88, 58, 0.12);
  background: rgba(255, 255, 255, 0.68);
}

.ux-hint-icon {
  margin-top: 2rpx;
  flex-shrink: 0;
}

.ux-hint-copy {
  flex: 1;
}

.ux-hint-title {
  font-size: 24rpx;
  font-weight: 700;
  color: var(--text-primary);
  margin-bottom: 8rpx;
}

.ux-hint-text {
  font-size: 22rpx;
  line-height: 1.62;
  color: var(--text-muted);
}

.tone-warn {
  background: rgba(255, 243, 213, 0.9);
  border-color: rgba(174, 129, 26, 0.2);
}

.tone-info {
  background: rgba(229, 242, 255, 0.92);
  border-color: rgba(86, 123, 168, 0.18);
}

.tone-success {
  background: rgba(231, 248, 232, 0.92);
  border-color: rgba(76, 136, 86, 0.18);
}

.tone-muted {
  background: rgba(238, 238, 234, 0.92);
  border-color: rgba(112, 118, 105, 0.16);
}
</style>
