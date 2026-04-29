/**
 * 间距设计令牌 — TypeScript 类型定义
 *
 * 与 utilities.css / layout.css 的间距值严格同步。
 * 基于 8rpx 递增体系（UniApp 中 2rpx ≈ 1px @ 750 设计稿）。
 *
 * 使用场景：组件动态 style 绑定、老年模式缩放计算等。
 */

/** 间距级别映射（单位 rpx） */
export const SPACING = {
  0: 0,
  1: 8,
  2: 16,
  3: 24,
  4: 32,
  5: 40,
  6: 48,
  7: 56,
  8: 64,
  9: 72,
  10: 80,
  12: 96,
  16: 128,
} as const

export type SpacingLevel = keyof typeof SPACING

/** 返回 rpx 字符串，可直接用于 :style 绑定 */
export function rpx(level: SpacingLevel): string {
  return `${SPACING[level]}rpx`
}

/** 页面级常用间距 — 与 layout.css 同步 */
export const PAGE = {
  /** 页面水平内边距 */
  paddingX: '32rpx',
  /** 安全区底部留白 */
  safeBottom: '34rpx',
  /** 区块间距 */
  sectionGap: '24rpx',
  /** 卡片内边距 (card.css --padding) */
  cardPadding: '32rpx',
  /** 卡片底部外边距 */
  cardMarginBottom: '24rpx',
} as const

/** 老年模式缩放系数 — 匹配 PRD v2.1 */
export const ELDER_SCALE = 1.2

/** 获取老年模式放大后的 rpx 值 */
export function elderRpx(level: SpacingLevel): string {
  const base = SPACING[level]
  return `${Math.round(base * ELDER_SCALE)}rpx`
}

export default SPACING
