/**
 * 颜色设计令牌 - TypeScript 类型定义
 * 
 * 基于现有 CSS 变量定义的 TypeScript 接口
 * 为农助手平台的现代设计系统提供类型安全支持
 */

export interface ColorTokens {
  /** HSL 基于祖母绿 #10B981 */
  primaryHue: string;
  primarySat: string;
  primaryLit: string;
  
  /** 主色调 */
  primaryColor: string;
  primaryStrong: string;
  primaryLight: string;
  primaryFaint: string;
  
  /** 辅助色 */
  secondaryColor: string;
  secondaryStrong: string;
  
  /** 公益品牌色 */
  accentGold: string;
  accentGoldLight: string;
  accentWarm: string;
  
  /** 语义色彩 / 中性调色板 */
  textPrimary: string;
  textRegular: string;
  textMuted: string;
  textSoft: string;
  
  /** 表面色彩 */
  surfaceDeep: string;
  surfaceTint: string;
  surfaceFrosted: string;
  pageBg: string;
  
  /** 边框色彩 */
  borderLight: string;
  borderRegular: string;
  borderFocus: string;
  
  /** 状态色彩 */
  colorSuccess: string;
  colorWarning: string;
  colorError: string;
  colorInfo: string;
  
  /** 状态背景色彩 (用于磨砂状态卡片) */
  colorSuccessBg: string;
  colorWarningBg: string;
  colorErrorBg: string;
  colorInfoBg: string;
}

export interface ElderModeColorTokens extends Partial<ColorTokens> {
  /** 老年模式特定颜色调整 */
  primaryColor: string;
  primaryStrong: string;
  primaryLight: string;
  primaryFaint: string;
  textPrimary: string;
  textRegular: string;
  textMuted: string;
  textSoft: string;
  pageBg: string;
  surfaceFrosted: string;
  borderLight: string;
  borderRegular: string;
  borderFocus: string;
}

/**
 * 获取当前颜色的 CSS 变量值
 */
export const getCssColorVariable = (token: keyof ColorTokens): string => {
  return `var(--${token.replace(/([A-Z])/g, '-$1').toLowerCase()})`;
};

/**
 * 颜色设计令牌常量定义
 * 与 CSS 变量保持同步
 */
export const COLOR_TOKENS: ColorTokens = {
  primaryHue: '160',
  primarySat: '84%',
  primaryLit: '39%',
  
  primaryColor: 'hsl(var(--primary-hue), var(--primary-sat), var(--primary-lit))',
  primaryStrong: 'hsl(var(--primary-hue), var(--primary-sat), 33%)',
  primaryLight: 'hsl(var(--primary-hue), var(--primary-sat), 96%)',
  primaryFaint: 'hsl(var(--primary-hue), 60%, 97%)',
  
  secondaryColor: '#F3F4F6',
  secondaryStrong: '#E5E7EB',
  
  accentGold: '#F59E0B',
  accentGoldLight: 'rgba(245, 158, 11, 0.08)',
  accentWarm: '#FDB913',
  
  textPrimary: '#111827',
  textRegular: '#374151',
  textMuted: '#6B7280',
  textSoft: '#9CA3AF',
  
  surfaceDeep: '#FFFFFF',
  surfaceTint: 'rgba(255, 255, 255, 1)',
  surfaceFrosted: 'rgba(255, 255, 255, 0.72)',
  pageBg: '#F7F8FA',
  
  borderLight: 'rgba(0, 0, 0, 0.03)',
  borderRegular: 'rgba(0, 0, 0, 0.08)',
  borderFocus: 'rgba(16, 185, 129, 0.3)',
  
  colorSuccess: '#10B981',
  colorWarning: '#F59E0B',
  colorError: '#EF4444',
  colorInfo: '#3B82F6',
  
  colorSuccessBg: 'rgba(209, 250, 229, 0.55)',
  colorWarningBg: 'rgba(254, 243, 199, 0.55)',
  colorErrorBg: 'rgba(254, 226, 226, 0.55)',
  colorInfoBg: 'rgba(219, 234, 254, 0.55)',
};

/**
 * 老年模式颜色调整
 */
export const ELDER_MODE_COLOR_TOKENS: ElderModeColorTokens = {
  primaryColor: '#059669',
  primaryStrong: '#047857',
  primaryLight: '#ECFDF5',
  primaryFaint: '#F0FDF4',
  textPrimary: '#030712',
  textRegular: '#111827',
  textMuted: '#374151',
  textSoft: '#4B5563',
  pageBg: '#F3F4F6',
  surfaceFrosted: 'rgba(255, 255, 255, 0.95)',
  borderLight: 'rgba(0, 0, 0, 0.05)',
  borderRegular: 'rgba(0, 0, 0, 0.12)',
  borderFocus: 'rgba(5, 150, 105, 0.4)',
};

/**
 * 颜色工具函数
 */
export class ColorUtils {
  /**
   * 生成颜色样式对象
   */
  static generateColorStyles(token: keyof ColorTokens, isElderMode: boolean = false): Record<string, string> {
    const prefix = isElderMode ? 'elder-' : '';
    const cssVarName = `--${prefix}${token.replace(/([A-Z])/g, '-$1').toLowerCase()}`;
    
    return {
      [cssVarName]: isElderMode 
        ? ELDER_MODE_COLOR_TOKENS[token as keyof ElderModeColorTokens] || ''
        : COLOR_TOKENS[token]
    };
  }
  
  /**
   * 获取颜色类名
   */
  static getColorClass(token: keyof ColorTokens): string {
    return `color-${token.replace(/([A-Z])/g, '-$1').toLowerCase()}`;
  }
}

export default COLOR_TOKENS;