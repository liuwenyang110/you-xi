# 农助手设计系统使用指南

## 概述

农助手采用模块化、可扩展的设计系统，遵循公益绿色主题，支持适老模式。

## 设计系统架构

```
styles/
├── design-tokens/        # 设计令牌（核心变量）
│   ├── colors.css       # 颜色变量
│   ├── typography.css   # 排版变量
│   ├── effects.css      # 效果变量（阴影、圆角、动画）
│   ├── spacing.css      # 间距变量（新增）
│   └── index.css        # 设计令牌入口
├── core/               # 核心样式
│   ├── reset.css       # 重置样式
│   ├── animations.css  # 动画
│   └── utilities.css   # 工具类
├── components/         # 组件样式
│   ├── button.css
│   ├── card.css
│   ├── form.css
│   └── layout.css
└── index.css          # 全局样式入口
```

## 设计令牌使用说明

### 颜色系统

使用 CSS 变量而非硬编码颜色值：

```css
/* ✅ 好的做法 */
color: var(--primary-color);
background: var(--primary-light);
border-color: var(--border-regular);

/* ❌ 避免这样 */
color: #2D7A4F;
background: #EAF5EE;
```

常用颜色变量：
- `--primary-color` - 公益绿主色
- `--text-primary` - 主要文字
- `--text-muted` - 次要文字
- `--page-bg` - 页面背景
- `--color-success/warning/error/info` - 状态色

### 间距系统

使用间距变量统一页面布局：

```css
/* ✅ 使用变量 */
padding: var(--space-6);
margin-bottom: var(--space-4);
gap: var(--card-gap);

/* 可用间距变量 */
--space-1 (4rpx), --space-2 (8rpx), --space-3 (12rpx), --space-4 (16rpx)
--space-6 (24rpx), --space-8 (32rpx), --space-10 (40rpx), --space-12 (48rpx)

/* 快捷别名 */
--space-xs, --space-sm, --space-md, --space-lg, --space-xl
```

### 圆角系统

```css
/* ✅ 使用变量 */
border-radius: var(--radius-lg);
border-radius: var(--radius-2xl);
border-radius: var(--radius-full);
```

### 阴影系统

```css
/* ✅ 使用变量 */
box-shadow: var(--shadow-base);
box-shadow: var(--shadow-md);
box-shadow: var(--shadow-colored); /* 绿色主题阴影 */
```

### 动画系统

```css
transition: all var(--duration-200) var(--ease-in-out);
animation: fade-in var(--duration-300) var(--ease-out);
```

## 组件类使用

### 卡片组件

```html
<!-- 标准卡片 -->
<view class="card">
  卡片内容
</view>

<!-- 玻璃拟态卡片 -->
<view class="card card-glass">
  玻璃卡片
</view>

<!-- 状态卡片 -->
<view class="card card-status card-status-success">
  成功状态
</view>
```

### 按钮组件

```html
<!-- 主按钮 -->
<button class="btn btn-primary">主要操作</button>

<!-- 次要按钮 -->
<button class="btn btn-secondary">次要操作</button>
```

### 工具类

```html
<!-- Flex 布局 -->
<view class="flex items-center justify-between">
  <!-- 内容 -->
</view>

<!-- 间距 -->
<view class="mt-4 mb-4">
  <!-- 内容 -->
</view>

<!-- 文字 -->
<view class="text-primary font-bold">
  主要文字
</view>
```

## 适老模式支持

设计系统自动支持适老模式，只需在 page 上添加 `.elder-mode` 类：

```html
<!-- 页面容器 -->
<page class="elder-mode">
  <!-- 内容 -->
</page>
```

适老模式特性：
- 字体自动放大
- 间距自动增加
- 颜色对比度增强
- 动画速度变慢（减少视觉疲劳）
- 禁用玻璃特效（使用实色背景）

## 页面样式最佳实践

### 1. 使用 scoped 样式

```vue
<style scoped>
/* 页面特定样式 */
.page-container {
  padding: var(--page-padding);
}
</style>
```

### 2. 优先使用设计系统变量

```css
/* ✅ */
.my-component {
  background: var(--surface-deep);
  border-radius: var(--radius-xl);
  padding: var(--card-padding);
  box-shadow: var(--shadow-base);
}

/* ❌ */
.my-component {
  background: white;
  border-radius: 20rpx;
  padding: 24rpx;
  box-shadow: 0 2rpx 8rpx rgba(0,0,0,0.04);
}
```

### 3. 为适老模式考虑

```css
/* 确保在适老模式下也工作良好 */
.my-button {
  min-height: 80rpx;
  font-size: var(--font-base);
}

/* 适老模式会自动增大这些值 */
```

## 颜色主题一致性

整个项目保持公益绿色主题：
- 主色：`#2D7A4F`
- 浅色：`#EAF5EE`
- 背景：`#F5F9F6`

避免引入其他主题色，保持品牌一致性。

## 导入规范

所有样式已在 App.vue 中全局导入，无需在每个页面单独导入：

```css
/* App.vue 已处理 */
@import './styles/index.css';
```

## 维护建议

1. **新增变量**：在对应的设计令牌文件中添加
2. **新增组件样式**：在 components/ 目录下创建新文件
3. **修改主题**：只需修改 design-tokens/colors.css
4. **适配新平台**：在 styles/ 目录下添加平台特定样式

## 参考资源

- 项目 README.md
- PROJECT_OVERVIEW.md
- 组件源码示例
