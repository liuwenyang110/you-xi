/**
 * 动画设计令牌 — TypeScript 类型定义
 *
 * 严格镜像 effects.css 中的动效变量。
 * 缓动函数 + 时长体系，用于 JS 动画或组件 :style 绑定。
 */

/** 缓动函数 — 与 effects.css :root 1:1 同步 */
export const EASING = {
  inOut: 'cubic-bezier(0.4, 0, 0.2, 1)',
  out: 'cubic-bezier(0, 0, 0.2, 1)',
  in: 'cubic-bezier(0.4, 0, 1, 1)',
  spring: 'cubic-bezier(0.175, 0.885, 0.32, 1.275)',
  bounce: 'cubic-bezier(0.68, -0.55, 0.265, 1.55)',
} as const

/** 时长（毫秒数字） — 与 effects.css --duration-* 同步 */
export const DURATION = {
  100: 100,
  200: 200,
  300: 300,
  500: 500,
  700: 700,
  1000: 1000,
} as const

/** 老年模式时长覆盖 — 与 effects.css .elder-mode 同步 */
export const ELDER_DURATION = {
  200: 300,
  300: 500,
  500: 800,
} as const

export type DurationLevel = keyof typeof DURATION
export type EasingName = keyof typeof EASING

/** 返回 CSS transition 简写，方便 :style 绑定 */
export function transition(
  property: string = 'all',
  duration: DurationLevel = 200,
  easing: EasingName = 'inOut'
): string {
  return `${property} ${DURATION[duration]}ms ${EASING[easing]}`
}

/** CSS 变量引用形式 */
export function durationVar(level: DurationLevel): string {
  return `var(--duration-${level})`
}

export function easingVar(name: EasingName): string {
  const map: Record<EasingName, string> = {
    inOut: 'var(--ease-in-out)',
    out: 'var(--ease-out)',
    in: 'var(--ease-in)',
    spring: 'var(--spring)',
    bounce: 'var(--bounce)',
  }
  return map[name]
}

export default { EASING, DURATION, ELDER_DURATION }
