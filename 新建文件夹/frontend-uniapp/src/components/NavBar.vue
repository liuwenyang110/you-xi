<template>
  <view class="navbar" :style="{ paddingTop: statusBarH + 'px' }" :class="{ 'navbar--transparent': transparent }">
    <view class="navbar__inner">
      <view class="navbar__left" @click="goBack" v-if="showBack">
        <text class="navbar__back">‹</text>
      </view>
      <view class="navbar__left" v-else></view>
      <text class="navbar__title">{{ title }}</text>
      <view class="navbar__right">
        <slot name="right"></slot>
      </view>
    </view>
  </view>
  <!-- 占位，防止内容被遮挡 -->
  <view :style="{ height: (statusBarH + 44) + 'px' }" v-if="!transparent"></view>
</template>

<script>
export default {
  name: 'NavBar',
  props: {
    title: { type: String, default: '' },
    showBack: { type: Boolean, default: true },
    transparent: { type: Boolean, default: false },
  },
  data() {
    return { statusBarH: 0 }
  },
  created() {
    const sys = uni.getSystemInfoSync()
    this.statusBarH = sys.statusBarHeight || 0
  },
  methods: {
    goBack() {
      const pages = getCurrentPages()
      if (pages.length > 1) {
        uni.navigateBack()
      } else {
        uni.reLaunch({ url: '/pages/farmer/zone-home' })
      }
    }
  }
}
</script>

<style scoped>
.navbar {
  position: fixed;
  top: 0; left: 0; right: 0;
  z-index: 100;
  background: var(--surface-deep);
  border-bottom: 1px solid var(--border-light);
}
.navbar--transparent {
  background: transparent;
  border-bottom: none;
}
.navbar__inner {
  height: 44px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 16px;
}
.navbar__left {
  width: 60px;
  display: flex;
  align-items: center;
}
.navbar__back {
  font-size: 28px;
  font-weight: 300;
  color: var(--text-primary);
  line-height: 1;
  padding: 4px 8px;
}
.navbar--transparent .navbar__back {
  color: #fff;
}
.navbar__title {
  font-size: 17px;
  font-weight: 600;
  color: var(--text-primary);
  text-align: center;
  flex: 1;
}
.navbar--transparent .navbar__title {
  color: #fff;
}
.navbar__right {
  width: 60px;
  display: flex;
  align-items: center;
  justify-content: flex-end;
}
</style>
