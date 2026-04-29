/**
 * 阴影设计令牌 — TypeScript 类型定义
 *
 * 严格镜像 effects.css 中的 --shadow-* 变量。
 * 用于组件需要在 JS 层面引用阴影值的场景。
 */

/** 阴影令牌 — 与 effects.css :root 保持 1:1 同步 */
export const SHADOWS = {
  none: '0 0 #0000',
  sm: '0 1px 2px 0 rgba(0, 0, 0, 0.05)',
  base: '0 4rpx 12rpx rgba(0, 0, 0, 0.02), 0 2rpx 4rpx rgba(0, 0, 0, 0.01)',
  md: '0 10rpx 20rpx -5rpx rgba(0, 0, 0, 0.04), 0 8rpx 10rpx -6rpx rgba(0, 0, 0, 0.01)',
  lg: '0 20rpx 25rpx -5rpx rgba(0, 0, 0, 0.03), 0 8rpx 10rpx -6rpx rgba(0, 0, 0, 0.01)',
  xl: '0 25rpx 50rpx -12rpx rgba(0, 0, 0, 0.06)',
  colored: '0 16rpx 32rpx rgba(16, 185, 129, 0.24)',
  warm: '0 12rpx 28rpx rgba(245, 158, 11, 0.16)',
  inner: 'inset 0 2rpx 4rpx rgba(0, 0, 0, 0.04)',
} as const

/** 老年模式阴影 — effects.css .elder-mode 覆盖值 */
export const ELDER_SHADOWS = {
  base: '0 8rpx 24rpx rgba(0, 0, 0, 0.08)',
  md: '0 10rpx 30rpx rgba(0, 0, 0, 0.06)',
  colored: '0 10rpx 24rpx rgba(22, 32, 18, 0.16)',
} as const

export type ShadowLevel = keyof typeof SHADOWS

/** 圆角令牌 — 与 effects.css :root 同步 */
export const RADIUS = {
  sm: '8rpx',
  md: '12rpx',
  lg: '16rpx',
  xl: '20rpx',
  '2xl': '24rpx',
  '3xl': '32rpx',
  full: '9999px',
} as const

/** 模糊令牌 — 与 effects.css :root 同步 */
export const BLUR = {
  sm: '8px',
  md: '16px',
  lg: '24px',
  xl: '40px',
} as const

/** CSS 变量引用 — 在模板 :style 中更推荐用这个 */
export function shadowVar(level: ShadowLevel): string {
  return `var(--shadow-${level})`
}

export default SHADOWS
