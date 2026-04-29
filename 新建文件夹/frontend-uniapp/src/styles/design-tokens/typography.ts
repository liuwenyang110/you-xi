/**
 * 排版设计令牌 — TypeScript 类型定义
 *
 * 严格镜像 typography.css 的变量值。
 * 提供类型安全的字号/字重/行高引用。
 */

/** 字体家族 — 与 typography.css --font-sans / --font-mono 同步 */
export const FONT_FAMILY = {
  sans: '"Inter", "Outfit", -apple-system, BlinkMacSystemFont, "SF Pro Text", "PingFang SC", "Helvetica Neue", Arial, sans-serif',
  mono: 'ui-monospace, SFMono-Regular, Menlo, Monaco, Consolas, "Liberation Mono", "Courier New", monospace',
} as const

/** 字号（rpx 数值），与 typography.css --text-* 同步 */
export const FONT_SIZE = {
  xs: 22,
  sm: 24,
  base: 28,
  lg: 32,
  xl: 36,
  '2xl': 42,
  '3xl': 50,
} as const

/** 老年模式字号覆盖 — typography.css .elder-mode */
export const ELDER_FONT_SIZE = {
  xs: 26,
  sm: 28,
  base: 32,
  lg: 36,
  xl: 42,
  '2xl': 48,
  '3xl': 56,
} as const

export type FontSizeLevel = keyof typeof FONT_SIZE

/** 字重 — 与 typography.css --font-* 同步 */
export const FONT_WEIGHT = {
  normal: 400,
  medium: 500,
  semibold: 600,
  bold: 700,
  extrabold: 800,
} as const

/** 老年模式字重覆盖 */
export const ELDER_FONT_WEIGHT = {
  medium: 600, // 比普通模式加一档
} as const

export type FontWeightName = keyof typeof FONT_WEIGHT

/** 行高 — 与 typography.css --leading-* 同步 */
export const LINE_HEIGHT = {
  none: 1,
  tight: 1.25,
  snug: 1.375,
  normal: 1.5,
  relaxed: 1.625,
  loose: 2,
} as const

export type LineHeightName = keyof typeof LINE_HEIGHT

/** 返回 rpx 字符串 */
export function fontSize(level: FontSizeLevel): string {
  return `${FONT_SIZE[level]}rpx`
}

/** 老年模式字号 rpx 字符串 */
export function elderFontSize(level: FontSizeLevel): string {
  return `${ELDER_FONT_SIZE[level]}rpx`
}

/** CSS 变量引用 */
export function fontSizeVar(level: FontSizeLevel): string {
  const varMap: Record<FontSizeLevel, string> = {
    xs: 'var(--text-xs)',
    sm: 'var(--text-sm)',
    base: 'var(--text-base)',
    lg: 'var(--text-lg)',
    xl: 'var(--text-xl)',
    '2xl': 'var(--text-2xl)',
    '3xl': 'var(--text-3xl)',
  }
  return varMap[level]
}

export default { FONT_FAMILY, FONT_SIZE, FONT_WEIGHT, LINE_HEIGHT }
