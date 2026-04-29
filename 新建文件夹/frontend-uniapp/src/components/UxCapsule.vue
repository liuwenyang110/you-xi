<template>
  <view class="ux-capsule" :class="[toneClass, { strong }]">
    <ux-glyph class="ux-capsule-icon" :kind="iconKind" :size="38" />
    <text class="ux-capsule-label">{{ label }}</text>
  </view>
</template>

<script>
import UxGlyph from './UxGlyph.vue'

export default {
  components: { UxGlyph },
  props: {
    label: {
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
    },
    strong: {
      type: Boolean,
      default: false
    }
  },
  computed: {
    toneClass() {
      return `tone-${this.tone}`
    }
  }
}
</script>

<style scoped>
.ux-capsule {
  display: inline-flex;
  align-items: center;
  gap: 8rpx;
  padding: 8rpx 14rpx 8rpx 8rpx;
  border-radius: 999rpx;
  border: 1rpx solid rgba(95, 91, 74, 0.12);
  background: rgba(255, 255, 255, 0.74);
  color: #335339;
}

.ux-capsule-icon {
  flex-shrink: 0;
}

.ux-capsule-label {
  font-size: 22rpx;
  font-weight: 700;
  letter-spacing: 0.3rpx;
}

.ux-capsule.strong {
  padding-right: 18rpx;
  box-shadow: 0 10rpx 22rpx rgba(72, 60, 30, 0.12);
}

.tone-neutral {
  background: rgba(255, 255, 255, 0.78);
  color: #35573b;
}

.tone-pending,
.tone-warn {
  background: rgba(255, 243, 214, 0.9);
  border-color: rgba(173, 129, 30, 0.2);
  color: #845a06;
}

.tone-active,
.tone-info {
  background: rgba(228, 242, 255, 0.9);
  border-color: rgba(73, 113, 163, 0.2);
  color: #2d5a88;
}

.tone-success {
  background: rgba(226, 247, 229, 0.92);
  border-color: rgba(61, 130, 74, 0.2);
  color: #2d6a39;
}

.tone-closed,
.tone-muted {
  background: rgba(236, 237, 233, 0.92);
  border-color: rgba(114, 120, 108, 0.2);
  color: #616a61;
}
</style>
