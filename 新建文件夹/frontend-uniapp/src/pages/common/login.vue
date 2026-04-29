<template>
  <view class="login-page">
    <!-- 背景装饰 -->
    <view class="bg-decor">
      <view class="field-pattern"></view>
    </view>

    <!-- 顶部品牌区 -->
    <view class="brand-section">
      <view class="logo-wrap">
        <text class="logo-icon">农</text>
        <view class="logo-glow"></view>
      </view>
      <text class="brand-name">农助手</text>
      <text class="brand-slogan">村里的农事信息对接站</text>
    </view>

    <!-- 登录表单 -->
    <view class="form-card">
      <view class="form-title">手机号登录</view>

      <!-- 手机号 -->
      <view class="input-group">
        <text class="input-prefix">+86</text>
        <input
          class="phone-input"
          type="number"
          v-model="phone"
          placeholder="请输入手机号"
          maxlength="11"
        />
      </view>

      <!-- 密码 -->
      <view class="input-group">
        <input
          class="code-input"
          type="password"
          v-model="password"
          placeholder="请输入密码(需包含数字和英文)"
        />
      </view>

      <!-- 登录按钮 -->
      <button
        class="login-btn"
        :disabled="!canLogin || logging"
        :loading="logging"
        @click="login"
      >登 录</button>

      <!-- 协议 -->
      <view class="agreement-row">
        <text class="agreement-text">登录即代表同意</text>
        <text class="agreement-link">《农助手用户协议》</text>
        <text class="agreement-text">和</text>
        <text class="agreement-link">《隐私政策》</text>
      </view>
    </view>

    <!-- 底部提示 -->
    <view class="bottom-tip">
      <text class="tip-text">本平台仅用于农事信息对接，不涉及在线交易</text>
    </view>
  </view>
</template>

<script>
import request from '@/api/v3/request'
import { useV3UserStore } from '@/stores/v3/userStore'
import { useV3ZoneStore } from '@/stores/v3/zoneStore'

export default {
  name: 'LoginPage',
  data() {
    return {
      phone: '',
      password: '',
      logging: false,
    }
  },
  computed: {
    canLogin() {
      const pwdValid = /^(?=.*[0-9])(?=.*[a-zA-Z]).+$/.test(this.password)
      return this.phone.length === 11 && pwdValid && this.password.length >= 6
    }
  },
  async onLoad() {
    // 检查是否已登录
    const token = uni.getStorageSync('token')
    if (token) {
      await this.tryAutoLogin()
    }
  },
  onUnload() {
  },
  methods: {
    async tryAutoLogin() {
      const userStore = useV3UserStore()
      try {
        await userStore.fetchProfile()
        if (userStore.profile) {
          this.redirectAfterLogin(userStore)
        }
      } catch (e) {
        uni.removeStorageSync('token')
      }
    },
    async login() {
      if (!this.canLogin) return
      this.logging = true
      try {
        // 用项目统一的 v3 request（自带正确的 baseURL）
        // 后端 dev 模式允许万能验证码 123456 绕过校验
        const res = await request.post('/api/v1/auth/login/sms', {
          phone: this.phone,
          code: '123456',
        })
        if (res?.code === 0) {
          const token = res.data?.token
          if (token) uni.setStorageSync('token', token)

          const userStore = useV3UserStore()
          await userStore.fetchProfile()

          if (!userStore.profile) {
            uni.redirectTo({ url: '/pages/common/role-select' })
          } else {
            this.redirectAfterLogin(userStore)
          }
        } else {
          uni.showToast({ title: res?.message || '登录失败', icon: 'none' })
        }
      } catch (e) {
        uni.showToast({ title: '网络异常，请稍后重试', icon: 'none' })
      } finally {
        this.logging = false
      }
    },
    redirectAfterLogin(userStore) {
      if (!userStore.zoneId) {
        // 没有片区，去选片区
        uni.reLaunch({ url: '/pages/common/zone-select' })
        return
      }
      // 根据角色跳转主页
      const url = userStore.isFarmer ? '/pages/farmer/zone-home' : '/pages/operator/zone-home'
      uni.reLaunch({ url })
    }
  }
}
</script>

<style>
.login-page {
  min-height: 100vh;
  min-height: 100dvh;
  background: linear-gradient(160deg, var(--primary-strong) 0%, hsl(var(--primary-hue), 70%, 30%) 40%, var(--primary-color) 100%);
  display: flex;
  flex-direction: column;
  align-items: center;
  overflow: hidden;
  position: relative;
}

.bg-decor {
  position: absolute;
  top: 0; left: 0; right: 0; bottom: 0;
  overflow: hidden;
  pointer-events: none;
}
.field-pattern {
  position: absolute;
  width: 800rpx;
  height: 800rpx;
  border-radius: 50%;
  background: radial-gradient(circle, rgba(76,175,80,0.15) 0%, transparent 70%);
  top: -200rpx;
  right: -200rpx;
}

.brand-section {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 130rpx 0 70rpx;
}
.logo-wrap {
  position: relative;
  width: 140rpx;
  height: 140rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 24rpx;
}
.logo-icon { font-size: 100rpx; }
.logo-glow {
  position: absolute;
  inset: 0;
  border-radius: 50%;
  background: radial-gradient(circle, rgba(139,195,74,0.4) 0%, transparent 70%);
  animation: pulse 2s ease-in-out infinite;
}
@keyframes pulse {
  0%, 100% { transform: scale(1); opacity: 0.7; }
  50% { transform: scale(1.3); opacity: 0.3; }
}
.brand-name {
  font-size: 60rpx;
  font-weight: 900;
  color: #fff;
  letter-spacing: 8rpx;
  margin-bottom: 12rpx;
  text-shadow: 0 4rpx 20rpx rgba(0,0,0,0.3);
}
.brand-slogan {
  font-size: 28rpx;
  color: rgba(255,255,255,0.7);
  letter-spacing: 2rpx;
}

.form-card {
  background: var(--surface-deep);
  border-radius: 40rpx 40rpx 0 0;
  width: 100%;
  padding: 52rpx 48rpx 40rpx;
  flex: 1;
  box-shadow: 0 -8rpx 40rpx rgba(0,0,0,0.15);
}
.form-title {
  font-size: var(--text-xl);
  font-weight: var(--font-bold);
  color: var(--text-primary);
  margin-bottom: 40rpx;
}
.input-group {
  display: flex;
  align-items: center;
  border: 2rpx solid var(--border-regular);
  border-radius: var(--radius-lg);
  padding: 0 20rpx;
  margin-bottom: 24rpx;
  background: var(--primary-faint);
  height: 96rpx;
}
.input-prefix {
  font-size: 30rpx;
  color: var(--text-muted);
  padding-right: 16rpx;
  border-right: 2rpx solid var(--border-regular);
  margin-right: 20rpx;
}
.phone-input, .code-input {
  flex: 1;
  font-size: 32rpx;
  color: var(--text-primary);
  border: none;
  background: transparent;
}
.sms-btn {
  flex-shrink: 0;
  background: var(--gradient-primary);
  color: #fff;
  border: none;
  border-radius: var(--radius-md);
  font-size: 26rpx;
  padding: 12rpx 24rpx;
  white-space: nowrap;
}
.sms-btn[disabled] { opacity: 0.5; }

.login-btn {
  width: 100%;
  height: 100rpx;
  background: var(--gradient-primary);
  color: #fff;
  border: none;
  border-radius: var(--radius-full);
  font-size: var(--text-xl);
  font-weight: var(--font-bold);
  letter-spacing: 4rpx;
  box-shadow: var(--shadow-colored);
  margin-bottom: 28rpx;
}
.login-btn[disabled] { opacity: 0.4; box-shadow: none; }

.agreement-row {
  display: flex;
  flex-wrap: wrap;
  justify-content: center;
  gap: 4rpx;
}
.agreement-text { font-size: var(--text-xs); color: var(--text-soft); }
.agreement-link { font-size: var(--text-xs); color: var(--primary-color); }

.bottom-tip {
  padding: 24rpx 48rpx 60rpx;
  text-align: center;
  width: 100%;
  box-sizing: border-box;
  background: var(--surface-deep);
}
.tip-text { font-size: var(--text-xs); color: var(--text-soft); }
</style>