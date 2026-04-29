<template>
  <view class="operator-card glass-panel" :class="{ 'is-share': isShare }">
    <!-- 装饰背景 -->
    <view class="card-bg">
      <view class="blob blob-1"></view>
      <view class="blob blob-2"></view>
    </view>

    <!-- 顶部状态栏 -->
    <view class="card-header flex justify-between items-center">
      <view class="badge-reputation font-bold">
        <text class="icon">✨</text>
        金牌老把式
      </view>
      <view class="status-online flex items-center">
        <view class="dot-live"></view>
        <text class="text-xs ml-1">在线接收预约</text>
      </view>
    </view>

    <!-- 机手核心信息 -->
    <view class="operator-profile mt-6 flex items-center gap-4">
      <view class="avatar-wrapper">
        <image :src="operator.avatarUrl || '/static/default-avatar.png'" class="avatar-img" mode="aspectFill" />
        <view class="cert-check">✔️</view>
      </view>
      <view class="profile-info">
        <view class="name-row flex items-center gap-2">
          <text class="name text-xl font-black">{{ operator.nickname || '资深机手' }}</text>
          <view class="tag-level">L5 专家级</view>
        </view>
        <view class="location-row flex items-center text-xs text-muted mt-1">
          <text class="icon mr-1">📍</text>
          {{ operator.locationName || '常驻：城关镇' }} · 距离约 {{ operator.distance || '2.5' }}km
        </view>
      </view>
    </view>

    <!-- 核心机械装备 -->
    <view class="machinery-box mt-6">
      <view class="section-label">主力装备</view>
      <view class="machine-detail flex items-center gap-3">
        <image :src="operator.machineImg || '/static/default-machine.png'" class="machine-img" mode="aspectFit" />
        <view class="machine-specs">
          <view class="machine-name font-bold">{{ operator.machineName || '雷沃欧豹 M1204-D' }}</view>
          <view class="machine-tags flex flex-wrap gap-1 mt-1">
            <text class="spec-tag">120马力</text>
            <text class="spec-tag">四驱</text>
            <text class="spec-tag">带空调</text>
          </view>
        </view>
      </view>
    </view>

    <!-- 服务轨迹与评价 -->
    <view class="stats-row mt-6 grid grid-cols-3 gap-2">
      <view class="stat-item">
        <view class="stat-num">{{ operator.serviceCount || 428 }}</view>
        <view class="stat-label">作业亩数</view>
      </view>
      <view class="stat-item">
        <view class="stat-num">{{ operator.rating || '4.9' }}</view>
        <view class="stat-label">口碑评分</view>
      </view>
      <view class="stat-item">
        <view class="stat-num">{{ operator.years || '8' }}</view>
        <view class="stat-label">从业年限</view>
      </view>
    </view>

    <!-- 底部操作与分享信息 -->
    <view class="card-footer mt-8 flex items-center justify-between">
      <view class="qr-info flex items-center gap-2">
        <view class="qr-box flex items-center justify-center">
          <text class="text-[10px] text-center">QR<br />CODE</text>
        </view>
        <view>
          <view class="text-xs font-bold">微信扫码</view>
          <view class="text-[10px] text-muted">查看机手实时动态</view>
        </view>
      </view>
      <button v-if="!isShare" class="btn-contact-main" @click="$emit('contact')">
        立即联系
      </button>
      <text v-else class="text-[10px] text-muted italic">农助手 · 让农机找地更简单</text>
    </view>
  </view>
</template>

<script>
export default {
  props: {
    operator: {
      type: Object,
      default: () => ({})
    },
    isShare: {
      type: Boolean,
      default: false
    }
  }
}
</script>

<style scoped>
.operator-card {
  position: relative;
  padding: 40rpx;
  border-radius: 40rpx;
  overflow: hidden;
  color: #fff;
  background: rgba(255, 255, 255, 0.05);
  border: 1px solid rgba(255, 255, 255, 0.1);
  box-shadow: 0 30rpx 60rpx rgba(0, 0, 0, 0.3);
  backdrop-filter: blur(20px);
}

.is-share {
  width: 600rpx;
}

.card-bg {
  position: absolute;
  top: 0; left: 0; right: 0; bottom: 0;
  z-index: -1;
  overflow: hidden;
}

.blob {
  position: absolute;
  border-radius: 50%;
  filter: blur(60rpx);
}

.blob-1 {
  width: 300rpx; height: 300rpx;
  background: rgba(16, 185, 129, 0.3);
  top: -100rpx; right: -100rpx;
}

.blob-2 {
  width: 250rpx; height: 250rpx;
  background: rgba(59, 130, 246, 0.2);
  bottom: -50rpx; left: -50rpx;
}

.badge-reputation {
  display: flex;
  align-items: center;
  gap: 8rpx;
  background: linear-gradient(90deg, #f59e0b, #d97706);
  padding: 8rpx 20rpx;
  border-radius: 999rpx;
  font-size: 20rpx;
  color: #fff;
  box-shadow: 0 4rpx 15rpx rgba(245, 158, 11, 0.4);
}

.dot-live {
  width: 12rpx; height: 12rpx;
  background: #10b981;
  border-radius: 50%;
  box-shadow: 0 0 10rpx #10b981;
  animation: pulse 2s infinite;
}

@keyframes pulse {
  0% { transform: scale(1); opacity: 1; }
  50% { transform: scale(1.5); opacity: 0.5; }
  100% { transform: scale(1); opacity: 1; }
}

.avatar-wrapper {
  position: relative;
}

.avatar-img {
  width: 110rpx; height: 110rpx;
  border-radius: 50%;
  border: 4rpx solid rgba(255, 255, 255, 0.2);
}

.cert-check {
  position: absolute;
  right: -4rpx; bottom: -4rpx;
  width: 36rpx; height: 36rpx;
  background: #3b82f6;
  border-radius: 50%;
  border: 2rpx solid #fff;
  font-size: 18rpx;
  display: flex; align-items: center; justify-content: center;
}

.tag-level {
  font-size: 18rpx;
  padding: 4rpx 12rpx;
  background: rgba(255, 255, 255, 0.1);
  border-radius: 4rpx;
  color: rgba(255, 255, 255, 0.6);
}

.section-label {
  font-size: 20rpx;
  color: rgba(255, 255, 255, 0.4);
  margin-bottom: 20rpx;
  text-transform: uppercase;
  letter-spacing: 2rpx;
}

.machine-detail {
  background: rgba(0, 0, 0, 0.2);
  padding: 20rpx;
  border-radius: 20rpx;
  border: 1px solid rgba(255, 255, 255, 0.05);
}

.machine-img {
  width: 100rpx; height: 100rpx;
}

.spec-tag {
  font-size: 18rpx;
  padding: 4rpx 12rpx;
  background: rgba(16, 185, 129, 0.1);
  color: #10b981;
  border-radius: 4rpx;
}

.stat-item {
  text-align: center;
  padding: 16rpx;
  background: rgba(255, 255, 255, 0.03);
  border-radius: 16rpx;
}

.stat-num {
  font-size: 32rpx;
  font-weight: 800;
  color: #fff;
}

.stat-label {
  font-size: 18rpx;
  color: rgba(255, 255, 255, 0.4);
  margin-top: 4rpx;
}

.qr-box {
  width: 80rpx; height: 80rpx;
  background: #fff;
  border-radius: 8rpx;
  color: #000;
}

.btn-contact-main {
  background: #fff;
  color: #000;
  font-weight: 800;
  border-radius: 20rpx;
  padding: 20rpx 50rpx;
  font-size: 28rpx;
  border: none;
  box-shadow: 0 10rpx 30rpx rgba(255, 255, 255, 0.2);
}

.btn-contact-main:active {
  transform: translateY(2rpx);
  box-shadow: 0 5rpx 15rpx rgba(255, 255, 255, 0.1);
}
</style>
