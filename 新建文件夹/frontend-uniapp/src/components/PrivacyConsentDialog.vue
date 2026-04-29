<template>
  <view class="privacy-mask" v-if="visible" @click.stop>
    <view class="privacy-sheet scene-enter">
      <!-- 头部图标 -->
      <view class="privacy-icon-wrap">
        <view class="privacy-icon-bg">
          <text class="privacy-icon">🔒</text>
        </view>
      </view>

      <!-- 标题 -->
      <view class="privacy-title">农助手隐私保护</view>
      <view class="privacy-version">《隐私政策》版本 {{ policyVersion }}</view>

      <!-- 正文 -->
      <scroll-view class="privacy-body" scroll-y>
        <view class="privacy-section">
          <view class="privacy-section-title">我们收集哪些信息</view>
          <view class="privacy-section-content">
            为帮您找到合适的农机服务，我们会收集：
            手机号、位置信息（作业地点）、农机服务需求。
          </view>
        </view>
        <view class="privacy-section">
          <view class="privacy-section-title">如何使用您的信息</view>
          <view class="privacy-section-content">
            上述信息仅用于：匹配附近农机主、展示联系方式、提升服务质量。
            我们<text class="privacy-emphasis">不会</text>将您的信息出售给第三方。
          </view>
        </view>
        <view class="privacy-section">
          <view class="privacy-section-title">您的权利</view>
          <view class="privacy-section-content">
            您可随时在「个人中心 → 隐私设置」中查询、更正、删除您的个人信息，或撤回授权。
          </view>
        </view>
        <view class="privacy-tip">
          ⚠️ 根据《个人信息保护法》，使用本平台前需要您的明确同意。
        </view>
      </scroll-view>

      <!-- 手机号联系选项 -->
      <view class="phone-consent-row" v-if="showPhoneConsent">
        <view class="phone-consent-check" @click="phoneDisplayConsented = !phoneDisplayConsented">
          <view class="check-box" :class="{ checked: phoneDisplayConsented }">
            <text v-if="phoneDisplayConsented" class="check-mark">✓</text>
          </view>
          <text class="phone-consent-label">同意展示脱敏手机号（如 138****0001）给对方查看</text>
        </view>
      </view>

      <!-- 按钮区域 -->
      <view class="privacy-actions">
        <view class="privacy-btn-agree" @click="onAgree">
          同意并继续使用
        </view>
        <view class="privacy-btn-decline" @click="onDecline">
          不同意，退出
        </view>
      </view>
    </view>
  </view>
</template>

<script>
import { farmerApi } from '../api/farmer'

const CURRENT_POLICY_VERSION = '1.0'

export default {
  name: 'PrivacyConsentDialog',
  props: {
    /** 是否显示手机号展示授权选项 */
    showPhoneConsent: {
      type: Boolean,
      default: false
    }
  },
  emits: ['agreed', 'declined'],
  data() {
    return {
      visible: false,
      policyVersion: CURRENT_POLICY_VERSION,
      phoneDisplayConsented: true,
      checked: false
    }
  },
  methods: {
    /**
     * 检查并按需弹窗。
     * 已同意则不弹出，直接回调 agreed。
     */
    async checkAndShow() {
      try {
        const res = await farmerApi.checkPrivacyConsent('PRIVACY_POLICY')
        if (res && res.consented) {
          // 已同意过，静默放行
          this.$emit('agreed', { alreadyConsented: true })
          return
        }
      } catch (_) {
        // 检查失败时降级展示弹窗
      }
      this.show()
    },
    show() {
      this.visible = true
    },
    hide() {
      this.visible = false
    },
    async onAgree() {
      try {
        // 记录隐私政策同意
        await farmerApi.recordPrivacyConsent('PRIVACY_POLICY', CURRENT_POLICY_VERSION)
        // 如果需要手机号展示授权
        if (this.showPhoneConsent && this.phoneDisplayConsented) {
          await farmerApi.recordPrivacyConsent('PHONE_DISPLAY', CURRENT_POLICY_VERSION)
        }
      } catch (e) {
        // 容错：记录失败不阻断用户体验
        console.warn('[PrivacyConsent] 同意记录失败，降级处理', e)
      }
      this.hide()
      this.$emit('agreed', {
        phoneDisplayConsented: this.phoneDisplayConsented,
        alreadyConsented: false
      })
    },
    onDecline() {
      this.hide()
      this.$emit('declined')
      uni.showModal({
        title: '温馨提示',
        content: '不同意隐私政策将无法使用平台服务。如有顾虑欢迎联系客服了解详情。',
        showCancel: false,
        confirmText: '知道了'
      })
    }
  }
}
</script>

<style scoped>
/* 遮罩 */
.privacy-mask {
  position: fixed;
  top: 0; left: 0; right: 0; bottom: 0;
  background: rgba(0, 0, 0, 0.6);
  z-index: 9999;
  display: flex;
  align-items: flex-end;
  justify-content: center;
}

/* 弹出面板 */
.privacy-sheet {
  width: 100%;
  max-height: 88vh;
  background: #FFFFFF;
  border-top-left-radius: 40rpx;
  border-top-right-radius: 40rpx;
  padding: 0 40rpx 40rpx;
  display: flex;
  flex-direction: column;
  box-shadow: 0 -20rpx 60rpx rgba(0,0,0,0.1);
}

/* 顶部图标 */
.privacy-icon-wrap {
  display: flex;
  justify-content: center;
  margin: 40rpx 0 24rpx;
}
.privacy-icon-bg {
  width: 100rpx; height: 100rpx;
  border-radius: 50%;
  background: linear-gradient(135deg, #e8f5e9, #c8e6c9);
  display: flex; align-items: center; justify-content: center;
}
.privacy-icon { font-size: 52rpx; }

/* 文字 */
.privacy-title {
  font-size: 36rpx;
  font-weight: 900;
  color: #1a2e1a;
  text-align: center;
  margin-bottom: 6rpx;
}
.privacy-version {
  font-size: 22rpx;
  color: #78909c;
  text-align: center;
  margin-bottom: 28rpx;
}

/* 正文滚动区 */
.privacy-body {
  flex: 1;
  max-height: 40vh;
  background: #f7faf7;
  border-radius: 20rpx;
  padding: 28rpx;
  margin-bottom: 24rpx;
}

.privacy-section { margin-bottom: 24rpx; }
.privacy-section-title {
  font-size: 26rpx;
  font-weight: 800;
  color: #2e7d32;
  margin-bottom: 10rpx;
}
.privacy-section-content {
  font-size: 24rpx;
  color: #546e57;
  line-height: 1.7;
}
.privacy-emphasis {
  color: #c62828;
  font-weight: 800;
}
.privacy-tip {
  margin-top: 16rpx;
  padding: 16rpx;
  background: rgba(255, 243, 224, 0.9);
  border-radius: 14rpx;
  font-size: 22rpx;
  color: #7b5800;
  line-height: 1.6;
}

/* 手机号同意行 */
.phone-consent-row {
  margin-bottom: 20rpx;
}
.phone-consent-check {
  display: flex;
  align-items: flex-start;
  gap: 16rpx;
}
.check-box {
  width: 40rpx; height: 40rpx;
  border-radius: 10rpx;
  border: 3rpx solid #bdbdbd;
  flex-shrink: 0;
  display: flex; align-items: center; justify-content: center;
  transition: all 0.2s;
}
.check-box.checked {
  background: #2e7d32;
  border-color: #2e7d32;
}
.check-mark { font-size: 24rpx; color: white; font-weight: 900; }
.phone-consent-label {
  font-size: 24rpx;
  color: #546e57;
  line-height: 1.6;
  flex: 1;
}

/* 按钮区 */
.privacy-actions {
  display: flex;
  flex-direction: column;
  gap: 18rpx;
}
.privacy-btn-agree {
  height: 100rpx;
  background: linear-gradient(135deg, #2e7d32, #43a047);
  color: white;
  font-size: 34rpx;
  font-weight: 800;
  border-radius: 999rpx;
  display: flex; align-items: center; justify-content: center;
  box-shadow: 0 12rpx 28rpx rgba(46, 125, 50, 0.35);
  letter-spacing: 2rpx;
}
.privacy-btn-agree:active { transform: scale(0.97); }
.privacy-btn-decline {
  height: 80rpx;
  background: transparent;
  color: #9e9e9e;
  font-size: 28rpx;
  display: flex; align-items: center; justify-content: center;
}

/* 老年友好模式 */
:global(html.elder-mode) .privacy-title { font-size: 42rpx; }
:global(html.elder-mode) .privacy-section-content { font-size: 30rpx; }
:global(html.elder-mode) .privacy-btn-agree { height: 120rpx; font-size: 40rpx; }
</style>
