<template>
  <view class="role-select-page">
    <!-- 顶部装饰 -->
    <view class="hero-section">
      <text class="hero-title">🌿 农助手</text>
      <text class="hero-sub">免费公益平台 · 请选择您的身份</text>
    </view>

    <!-- 角色卡片 -->
    <view class="roles-wrap">
      <view
        class="role-card"
        :class="{ selected: selected === 'FARMER' }"
        @click="select('FARMER')"
      >
        <view class="card-icon">🌾</view>
        <view class="card-info">
          <text class="card-title">我需要帮助</text>
          <text class="card-desc">发布农事需求，等待服务者主动联系您</text>
        </view>
        <view class="card-check" v-if="selected === 'FARMER'">✓</view>
      </view>

      <view
        class="role-card"
        :class="{ selected: selected === 'OPERATOR' }"
        @click="select('OPERATOR')"
      >
        <view class="card-icon">🚜</view>
        <view class="card-info">
          <text class="card-title">我能提供服务</text>
          <text class="card-desc">登记农机设备，公益帮助本片区农户</text>
        </view>
        <view class="card-check" v-if="selected === 'OPERATOR'">✓</view>
      </view>
    </view>

    <!-- 公益平台说明 -->
    <view class="charity-box">
      <text class="charity-text">🌿 本平台完全免费，不收取任何费用。
服务價格由双方直接协商，平台不介入交易。</text>
    </view>

    <!-- 温馨提示 -->
    <view class="tips-box">
      <text class="tips-text">注意：身份选定后不可更改，请谨慎选择</text>
    </view>

    <!-- 确认按钮 -->
    <view class="btn-wrap">
      <button
        class="confirm-btn"
        :disabled="!selected || loading"
        :loading="loading"
        @click="confirm"
      >确认身份，进入农助手</button>
    </view>
  </view>
</template>

<script>
import { selectRole } from '@/api/v3/user'
import { useV3UserStore } from '@/stores/v3/userStore'

export default {
  name: 'RoleSelect',
  data() {
    return {
      selected: '',
      loading: false,
    }
  },
  methods: {
    select(role) {
      this.selected = role
    },
    async confirm() {
      if (!this.selected) return
      this.loading = true
      try {
        const res = await selectRole({ role: this.selected })
        if (res.code === 0) {
          const store = useV3UserStore()
          store.setProfile(res.data)
          uni.redirectTo({ url: '/pages/common/zone-select' })
        } else {
          uni.showToast({ title: res.message || '操作失败', icon: 'none' })
        }
      } catch (e) {
        uni.showToast({ title: '网络异常，请重试', icon: 'none' })
      } finally {
        this.loading = false
      }
    }
  }
}
</script>

<style>
.role-select-page {
  min-height: 100vh;
  background: linear-gradient(160deg, #F0F7EC 0%, #E8F4E2 100%);
  padding: 0 32rpx 60rpx;
}
.hero-section {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 100rpx 0 60rpx;
}
.hero-icon { font-size: 100rpx; margin-bottom: 16rpx; }
.hero-title { font-size: 52rpx; font-weight: 700; color: #2D7A4F; letter-spacing: 4rpx; }
.hero-sub { font-size: 30rpx; color: #5BA88A; margin-top: 12rpx; }
.roles-wrap { display: flex; flex-direction: column; gap: 24rpx; margin-bottom: 32rpx; }
.role-card {
  display: flex;
  align-items: center;
  background: #fff;
  border-radius: 24rpx;
  padding: 36rpx 32rpx;
  border: 4rpx solid transparent;
  box-shadow: 0 4rpx 20rpx rgba(0,0,0,0.06);
  position: relative;
}
.role-card.selected { border-color: #2D7A4F; background: #EAF5EE; box-shadow: 0 6rpx 32rpx rgba(45,122,79,0.18); }
.card-icon { font-size: 72rpx; margin-right: 28rpx; flex-shrink: 0; }
.card-info { flex: 1; }
.card-title { display: block; font-size: 36rpx; font-weight: 700; color: #1A3A28; margin-bottom: 8rpx; }
.card-desc { display: block; font-size: 26rpx; color: #6B8F7A; line-height: 1.5; }
.card-check { font-size: 40rpx; color: #2D7A4F; font-weight: 700; }
.tips-box {
  display: flex;
  align-items: center;
  background: rgba(255,193,7,0.1);
  border-radius: 16rpx;
  padding: 18rpx 24rpx;
  margin-bottom: 32rpx;
}
.tips-icon { font-size: 32rpx; margin-right: 12rpx; }
.tips-text { font-size: 26rpx; color: #8B6914; }
/* 公益堪明 */
.charity-box {
  background: rgba(45,122,79,0.06);
  border-left: 6rpx solid #5BA88A;
  border-radius: 16rpx;
  padding: 24rpx 28rpx;
  margin-bottom: 20rpx;
}
.charity-text { font-size: 26rpx; color: #2D5A3D; line-height: 1.8; }

.btn-wrap { padding: 0 8rpx 40rpx; }
.confirm-btn {
  width: 100%;
  height: 96rpx;
  background: linear-gradient(135deg, #5BA88A, #2D7A4F);
  color: #fff;
  border-radius: 48rpx;
  font-size: 34rpx;
  font-weight: 700;
  border: none;
  letter-spacing: 2rpx;
  box-shadow: 0 8rpx 32rpx rgba(45,122,79,0.3);
}
.confirm-btn[disabled] { opacity: 0.5; box-shadow: none; }
</style>
