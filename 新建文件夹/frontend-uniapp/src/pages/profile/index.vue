<template>
  <view class="profile-page">
    <!-- 顶部用户卡片 -->
    <view class="user-card scene-enter">
      <!-- 动态背景装饰 -->
      <view class="card-bg-deco deco-1"></view>
      <view class="card-bg-deco deco-2"></view>
      
      <view class="user-avatar-wrap">
        <view class="user-avatar">
          <text class="avatar-text">{{ isFarmer ? '农' : '机' }}</text>
        </view>
      </view>
      <view class="user-info">
        <text class="user-name">{{ profile.realName || '农助手用户' }}</text>
        <view class="role-tag" :class="isFarmer ? 'tag-farmer' : 'tag-operator'">
          <text class="tag-dot"></text>
          {{ isFarmer ? '农户，您好' : '师傅，开工大吉' }}
        </view>
      </view>
    </view>

    <!-- 片区信息 -->
    <view class="section-card card-glass scene-enter delay-1">
      <view class="section-row" hover-class="row-hover" @click="gotoZoneSelect">
        <view class="row-left">
            <text class="row-icon-text">•</text>
          <view class="row-content">
            <text class="row-title">所在片区</text>
            <text class="row-sub">{{ zoneName || '未加入片区' }}</text>
          </view>
        </view>
        <text class="row-arrow">›</text>
      </view>
    </view>

    <!-- 快捷功能 -->
    <view class="section-card card-glass scene-enter delay-2">
      <view class="section-title-bar">常用功能</view>

      <view class="section-row" hover-class="row-hover" @click="navTo('/pages/common/notifications')">
        <view class="row-left">
            <text class="row-icon-text">•</text>
          <text class="row-title">消息中心</text>
        </view>
        <view class="row-right-wrap">
          <view class="badge badge-error" v-if="hasUnread">新消息</view>
          <text class="row-arrow">›</text>
        </view>
      </view>

      <view class="section-row" hover-class="row-hover" @click="navTo('/pages/common/teahouse')">
        <view class="row-left">
            <text class="row-icon-text">•</text>
          <text class="row-title">村交流大厅</text>
        </view>
        <text class="row-arrow">›</text>
      </view>

      <view class="section-row" v-if="isFarmer" hover-class="row-hover" @click="navTo('/pages/farmer/demand-list')">
        <view class="row-left">
            <text class="row-icon-text">•</text>
          <text class="row-title">我的需求</text>
        </view>
        <text class="row-arrow">›</text>
      </view>

      <view class="section-row" v-if="isFarmer" hover-class="row-hover" @click="navTo('/pages/farmer/operator-list')">
        <view class="row-left">
            <text class="row-icon-text">•</text>
          <text class="row-title">找农机手</text>
        </view>
        <text class="row-arrow">›</text>
      </view>

      <view class="section-row" v-if="isOperator" hover-class="row-hover" @click="navTo('/pages/operator/machinery-manage')">
        <view class="row-left">
            <text class="row-icon-text">•</text>
          <text class="row-title">我的农机</text>
        </view>
        <text class="row-arrow">›</text>
      </view>

      <view class="section-row" v-if="isOperator" hover-class="row-hover" @click="navTo('/pages/operator/demand-browse')">
        <view class="row-left">
            <text class="row-icon-text">•</text>
          <text class="row-title">浏览片区农活</text>
        </view>
        <text class="row-arrow">›</text>
      </view>
    </view>

    <!-- 显示设置 -->
    <view class="section-card card-glass scene-enter delay-3">
      <view class="section-title-bar">显示设置</view>

      <view class="section-row" hover-class="row-hover" @click="toggleElderMode">
           <view class="row-left">
            <text class="row-icon-text">•</text>
          <view class="row-content">
            <text class="row-title">长辈版（大字模式）</text>
            <text class="row-sub">开启后放大所有文字和按钮</text>
          </view>
        </view>
        <view class="toggle-switch" :class="{ 'toggle-on': elderMode }">
          <view class="toggle-thumb"></view>
        </view>
      </view>
    </view>

    <!-- 平台信息 -->
    <view class="section-card card-glass scene-enter delay-4">
      <view class="section-title-bar">关于平台</view>

      <view class="platform-info-block">
        <view class="platform-logo">
          <text class="platform-logo-name">农助手 V3</text>
          <view class="public-welfare-tag">
            <text>公益项目</text>
          </view>
        </view>
        <text class="platform-desc">
          农助手是面向农村的农事信息对接公告栏，帮助农户与农机手便捷联系，让农活不误时。平台只做信息撮合，不参与价格谈判和交易纠纷。本项目为公益项目，全功能免费开放。
        </text>
        <view class="platform-rule-list">
          <text class="rule-item">✓ 需求信息仅本镇域可见</text>
          <text class="rule-item">✓ 联系方式仅本片区可见</text>
          <text class="rule-item">✓ 15天不活跃自动清理</text>
          <text class="rule-item">✗ 平台不担保、不派单</text>
          <text class="rule-item">✗ 请勿发布虚假信息</text>
        </view>
      </view>
    </view>

    <!-- 退出/注销 -->
    <view class="logout-btn-wrap scene-enter delay-5">
      <button class="logout-btn" hover-class="logout-btn-hover" @click="logout">退出登录</button>
    </view>
  </view>
</template>

<script>
import { useV3UserStore } from '@/stores/v3/userStore'
import { useV3ZoneStore } from '@/stores/v3/zoneStore'

export default {
  name: 'ProfileIndex',
  data() {
    return {
      hasUnread: false,
    }
  },
  computed: {
    profile() { return useV3UserStore().profile || {} },
    isFarmer() { return useV3UserStore().isFarmer },
    isOperator() { return useV3UserStore().isOperator },
    zoneName() { return useV3ZoneStore().zoneName },
    elderMode() { return useV3UserStore().elderMode },
  },
  async onLoad() {
    const userStore = useV3UserStore()
    if (!userStore.profile) {
      await userStore.fetchProfile()
    }
    if (userStore.zoneId) {
      const zoneStore = useV3ZoneStore()
      if (!zoneStore.currentZone) {
        await zoneStore.loadZone(userStore.zoneId)
      }
    }
    this.checkUnread()
    // 应用适老版
    this.applyElderMode()
  },
  methods: {
    navTo(url) { uni.navigateTo({ url }) },
    gotoZoneSelect() { uni.navigateTo({ url: '/pages/common/zone-select' }) },
    toggleElderMode() {
      const userStore = useV3UserStore()
      userStore.toggleElderMode()
      this.applyElderMode()
      uni.showToast({
        title: userStore.elderMode ? '已切换为长辈版' : '已切换为标准版',
        icon: 'none'
      })
    },
    applyElderMode() {
      const isElder = useV3UserStore().elderMode
      uni.$emit('elderModeChange', isElder)
    },
    async checkUnread() {
      try {
        const { getNotifications } = await import('@/api/v3/notification')
        const res = await getNotifications(1, 1)
        if (res.code === 0) { this.hasUnread = (res.data.unreadCount || 0) > 0 }
      } catch (e) {}
    },
    logout() {
      uni.showModal({
        title: '退出确认',
        content: '确定退出登录吗？',
        success: (res) => {
          if (res.confirm) {
            useV3UserStore().clear()
            useV3ZoneStore().clearZone()
            uni.removeStorageSync('token')
            uni.removeStorageSync('v3user')
            uni.reLaunch({ url: '/pages/common/login' })
          }
        }
      })
    }
  }
}
</script>

<style scoped>
.profile-page {
  background: var(--page-bg);
  min-height: 100vh;
  padding: 24rpx 24rpx 80rpx;
  position: relative;
}

/* ========== Top User Card ========== */
.user-card {
  position: relative;
  display: flex;
  align-items: center;
  background: var(--gradient-primary);
  padding: 64rpx 40rpx 56rpx;
  border-radius: var(--radius-2xl);
  overflow: hidden;
  box-shadow: var(--shadow-colored);
  margin-bottom: 24rpx;
}

.card-bg-deco {
  position: absolute;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.15);
  filter: blur(40px);
}
.deco-1 { width: 300rpx; height: 300rpx; top: -100rpx; right: -50rpx; }
.deco-2 { width: 200rpx; height: 200rpx; bottom: -80rpx; left: -20rpx; }

.user-avatar-wrap {
  position: relative;
  margin-right: 32rpx;
  z-index: 2;
}



.user-avatar {
  position: relative;
  width: 128rpx; height: 128rpx;
  background: rgba(255, 255, 255, 0.25);
  border: 4rpx solid rgba(255, 255, 255, 0.6);
  border-radius: 50%;
  display: flex; align-items: center; justify-content: center;
  z-index: 2;
  box-shadow: var(--shadow-base);
}

.avatar-text { font-size: 44rpx; color: #fff; font-weight: 800; }
.user-info { flex: 1; z-index: 2; }
.user-name { 
  display: block; font-size: 44rpx; font-weight: 800; 
  color: #fff; margin-bottom: 12rpx;
  text-shadow: 0 2rpx 8rpx rgba(0,0,0,0.1);
}

.role-tag {
  display: inline-flex; align-items: center; gap: 8rpx;
  border-radius: var(--radius-full); padding: 6rpx 24rpx;
  font-size: 24rpx; font-weight: 700;
  backdrop-filter: blur(var(--blur-sm));
}
.tag-dot { width: 12rpx; height: 12rpx; border-radius: 50%; }
.tag-farmer { background: rgba(255,255,255,0.25); color: #fff; }
.tag-farmer .tag-dot { background: #fff; box-shadow: 0 0 8rpx #fff; }
.tag-operator { background: rgba(245, 158, 11, 0.9); color: #fff; }
.tag-operator .tag-dot { background: #fff; }

/* ========== Section Cards ========== */
.section-card {
  margin-bottom: 24rpx;
  border-radius: var(--radius-2xl);
  overflow: hidden;
}

/* .card-glass 复用全局 card.css 定义，不再本地覆盖 */

.section-title-bar {
  font-size: 24rpx; font-weight: 700; color: var(--text-soft);
  padding: 24rpx 32rpx 8rpx;
  text-transform: uppercase; letter-spacing: 2rpx;
}

.section-row {
  display: flex; align-items: center; justify-content: space-between;
  padding: 28rpx 32rpx;
  border-bottom: 1px solid var(--border-light);
  transition: background var(--duration-200) var(--ease-out);
}
.section-row:last-child { border-bottom: none; }
.row-hover { background: rgba(0, 0, 0, 0.02); }

.row-left { display: flex; align-items: center; flex: 1; gap: 24rpx; }

.row-icon-text { font-size: 32rpx; color: var(--primary-color); margin-right: 8rpx; }

.row-content { flex: 1; display: flex; flex-direction: column; gap: 4rpx; }
.row-title { font-size: 30rpx; font-weight: 700; color: var(--text-primary); }
.row-sub { font-size: 24rpx; color: var(--text-muted); }

.row-arrow { font-size: 40rpx; color: var(--text-soft); font-weight: 300; }

.row-right-wrap { display: flex; align-items: center; gap: 12rpx; }
.badge {
  padding: 4rpx 16rpx; border-radius: var(--radius-full);
  font-size: 22rpx; font-weight: 800;
}
.badge-error { background: rgba(239, 68, 68, 0.1); color: var(--color-error); border: 1px solid rgba(239, 68, 68, 0.2); }

/* Switch Toggle */
.toggle-switch {
  width: 96rpx; height: 56rpx; border-radius: var(--radius-full);
  background: var(--secondary-strong); position: relative;
  transition: all var(--duration-300) var(--spring);
}
.toggle-switch.toggle-on { background: var(--color-success); }
.toggle-thumb {
  width: 48rpx; height: 48rpx; border-radius: 50%;
  background: #fff; position: absolute; top: 4rpx; left: 4rpx;
  transition: all var(--duration-300) var(--spring);
  box-shadow: var(--shadow-sm);
}
.toggle-switch.toggle-on .toggle-thumb { transform: translateX(40rpx); }

/* Platform Info */
.platform-info-block { padding: 8rpx 32rpx 32rpx; }
.platform-logo { display: flex; align-items: center; margin-bottom: 16rpx; gap: 12rpx; }
.platform-logo-icon { font-size: 48rpx; }
.platform-logo-name { font-size: 36rpx; font-weight: 800; color: var(--primary-color); }
.public-welfare-tag {
  background: var(--color-success-bg);
  border-radius: var(--radius-full); padding: 4rpx 16rpx;
  font-size: 22rpx; font-weight: 700; color: var(--color-success);
}
.platform-desc {
  display: block; font-size: 26rpx; color: var(--text-muted);
  line-height: 1.7; margin-bottom: 24rpx;
}
.platform-rule-list { display: flex; flex-direction: column; gap: 12rpx; border-top: 1px dashed var(--border-regular); padding-top: 24rpx; }
.rule-item { font-size: 26rpx; color: var(--text-regular); font-weight: 500; display: flex; align-items: center; gap: 12rpx; }
.rule-icon { font-size: 28rpx; }

/* Logout Btn */
.logout-btn-wrap { padding: 16rpx 0; }
.logout-btn {
  background: rgba(239, 68, 68, 0.05); color: var(--color-error);
  border: 1px solid rgba(239, 68, 68, 0.2);
  border-radius: var(--radius-xl);
  font-size: 32rpx; font-weight: 800;
  height: 96rpx; display: flex; align-items: center; justify-content: center;
  transition: all var(--duration-200) var(--ease-out);
}
.logout-btn-hover {
  background: rgba(239, 68, 68, 0.1); transform: scale(0.98);
}

/* 动画复用全局 card.css 中的 .scene-enter / .delay-* 定义 */
</style>
