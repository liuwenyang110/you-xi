<template>
  <view class="ux-glyph" :class="`kind-${kind}`" :style="{ width: `${size}rpx`, height: `${size}rpx` }">
    <view class="ux-glyph-ring"></view>
    <text class="ux-glyph-text">{{ glyphText }}</text>
  </view>
</template>

<script>
const GLYPH_TEXT_MAP = {
  farmer: '农',
  owner: '机',
  admin: '管',
  match: '配',
  order: '单',
  service: '服',
  equipment: '设',
  location: '位',
  pending: '等',
  success: '成',
  warn: '注',
  closed: '停',
  empty: '空',
  hint: '提示'
}

export default {
  props: {
    kind: {
      type: String,
      default: 'hint'
    },
    size: {
      type: Number,
      default: 56
    }
  },
  computed: {
    glyphText() {
      return GLYPH_TEXT_MAP[this.kind] || '标'
    }
  }
}
</script>

<style scoped>
.ux-glyph {
  position: relative;
  border-radius: 20rpx;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
  background: linear-gradient(145deg, #f9f4e8, #edf4e6);
  box-shadow: 0 10rpx 20rpx rgba(69, 61, 33, 0.12);
}

.ux-glyph-ring {
  position: absolute;
  inset: 4rpx;
  border-radius: 16rpx;
  border: 1rpx solid rgba(255, 255, 255, 0.8);
}

.ux-glyph-text {
  position: relative;
  font-size: 22rpx;
  color: var(--text-primary);
  font-weight: 800;
  letter-spacing: 0.8rpx;
}

.kind-farmer,
.kind-service {
  background: linear-gradient(145deg, rgba(246, 247, 233, 0.98), rgba(225, 243, 215, 0.95));
}

.kind-owner,
.kind-equipment {
  background: linear-gradient(145deg, rgba(237, 246, 235, 0.98), rgba(222, 239, 241, 0.95));
}

.kind-match,
.kind-location {
  background: linear-gradient(145deg, rgba(247, 241, 227, 0.98), rgba(231, 244, 220, 0.95));
}

.kind-order,
.kind-success {
  background: linear-gradient(145deg, rgba(236, 248, 232, 0.98), rgba(218, 239, 219, 0.95));
}

.kind-pending,
.kind-warn {
  background: linear-gradient(145deg, rgba(255, 243, 213, 0.98), rgba(250, 232, 193, 0.96));
}

.kind-closed,
.kind-empty,
.kind-admin {
  background: linear-gradient(145deg, rgba(240, 239, 235, 0.98), rgba(228, 228, 220, 0.95));
}
</style>
