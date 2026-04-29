/**
 * 设计令牌统一索引
 *
 * 纯 ESM 导出，在需要时按模块 import。
 * 每个模块与对应的 CSS 文件严格同步。
 *
 * CSS 层面：colors.css → effects.css → typography.css （已有）
 * TS  层面：colors.ts → shadows.ts → animations.ts → typography.ts → spacing.ts
 */

// ── 颜色 ──────────────────────────────────
export { COLOR_TOKENS, ELDER_MODE_COLOR_TOKENS, ColorUtils, getCssColorVariable } from './colors'
export type { ColorTokens, ElderModeColorTokens } from './colors'

// ── 间距 ──────────────────────────────────
export { SPACING, PAGE, ELDER_SCALE, rpx, elderRpx } from './spacing'
export type { SpacingLevel } from './spacing'

// ── 阴影 / 圆角 / 模糊 ───────────────────
export { SHADOWS, ELDER_SHADOWS, RADIUS, BLUR, shadowVar } from './shadows'
export type { ShadowLevel } from './shadows'

// ── 动画 / 缓动 / 时长 ───────────────────
export { EASING, DURATION, ELDER_DURATION, transition, durationVar, easingVar } from './animations'
export type { DurationLevel, EasingName } from './animations'

// ── 排版 ──────────────────────────────────
export {
  FONT_FAMILY, FONT_SIZE, ELDER_FONT_SIZE,
  FONT_WEIGHT, ELDER_FONT_WEIGHT, LINE_HEIGHT,
  fontSize, elderFontSize, fontSizeVar,
} from './typography'
export type { FontSizeLevel, FontWeightName, LineHeightName } from './typography'
