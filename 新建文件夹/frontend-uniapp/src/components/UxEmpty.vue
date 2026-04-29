<template>
  <view class="ux-empty" :class="`tone-${tone}`">
    <ux-glyph class="ux-empty-icon" :kind="iconKind" :size="64" />
    <view class="ux-empty-title">{{ title }}</view>
    <view class="ux-empty-desc">{{ desc }}</view>
    <slot />
  </view>
</template>

<script>
import UxGlyph from './UxGlyph.vue'

export default {
  components: { UxGlyph },
  props: {
    title: {
      type: String,
      default: '这里还没有内容'
    },
    desc: {
      type: String,
      default: '点下面的按鈕可以开始'
    },
    tone: {
      type: String,
      default: 'neutral'
    },
    iconKind: {
      type: String,
      default: 'empty'
    }
  }
}
</script>

<style scoped>
.ux-empty {
  text-align: center;
  padding: 26rpx 22rpx;
  border-radius: 24rpx;
  border: 1rpx dashed var(--border-regular);
  background: var(--surface-frosted);
}

.ux-empty-icon {
  margin: 0 auto 12rpx;
}

.ux-empty-title {
  font-size: var(--text-base);
  font-weight: var(--font-bold);
  color: var(--text-primary);
  margin-bottom: 8rpx;
}

.ux-empty-desc {
  font-size: var(--text-sm);
  line-height: 1.64;
  color: var(--text-muted);
}

.tone-warn {
  background: var(--color-warning-bg);
  border-color: rgba(171, 128, 31, 0.24);
}

.tone-muted {
  background: var(--secondary-color);
  border-color: var(--border-regular);
}
</style>
