<template>
  <view class="home-page">
    <!-- 1. 背景地图（展示定位感，非核心功能） -->
    <map
      id="mainMap"
      class="full-map"
      :latitude="location.lat"
      :longitude="location.lng"
      :scale="14"
      :show-location="true"
    >
      <cover-view class="h5-map-fallback" v-if="isH5">
        <cover-view class="map-overlay-tint"></cover-view>
      </cover-view>
    </map>

    <!-- 2. 深色遮罩，让底部内容可读 -->
    <view class="map-dimmer"></view>

    <!-- 3. 顶部导航栏 -->
    <view class="top-nav-bar safe-area-top">
      <view class="nav-left">
        <view class="user-avatar" @click="openProfile"></view>
        <view class="mode-switch-wrap">
          <mode-switch tight />
        </view>
      </view>
      <view class="nav-right">
        <view class="proxy-button" @click="goProxyOrder">
          <text class="proxy-icon"></text>
          <text>代找</text>
        </view>
      </view>
    </view>

    <!-- 4. 主面板（底部抽屉样式） -->
    <view class="bottom-panel">

      <!-- 定位栏 -->
      <view class="location-bar" @click="handleEditLocation">
        <view class="dot green-dot"></view>
        <text class="location-text">{{ currentAddress }}</text>
        <text class="location-arrow">></text>
      </view>

      <!-- 农业类型 Tab -->
      <scroll-view class="category-tabs" scroll-x :show-scrollbar="false">
        <view
          class="tab-item"
          v-for="cat in categories"
          :key="cat.id"
          :class="{ active: selectedCategory === cat.id }"
          @click="selectCategory(cat.id)"
        >
          <text class="tab-emoji">{{ cat.emoji }}</text>
          <text class="tab-name">{{ cat.name }}</text>
        </view>
      </scroll-view>

      <!-- 作物 + 面积快选栏 -->
      <view class="quick-options">
        <view class="option-btn" @click="showCropSelector">
          <text class="opt-label">作物</text>
          <text class="opt-val">{{ selectedCropName }}</text>
        </view>
        <view class="option-btn">
          <text class="opt-label">亩数</text>
          <input class="opt-input" type="number" placeholder="输入亩数" v-model="areaInput" />
        </view>
      </view>

      <!-- 附近农机主列表 -->
      <view class="section-header">
        <text class="section-title">附近机手 · 直接联系</text>
        <text class="section-hint">{{ nearbyOwners.length }} 位可联系</text>
      </view>

      <scroll-view class="owner-list" scroll-y>
        <!-- 农机主卡片 -->
        <view
          v-for="owner in nearbyOwners"
          :key="owner.id"
          class="owner-card"
          :class="{ selected: selectedOwner && selectedOwner.id === owner.id }"
          @click="selectOwner(owner)"
        >
          <view class="owner-left">
            <view class="owner-avatar">{{ owner.avatar }}</view>
            <view class="owner-badge-wrap">
              <view class="rep-badge" :class="'rep-' + owner.repCode">{{ owner.repLabel }}</view>
            </view>
          </view>
          <view class="owner-main">
            <view class="owner-name-row">
              <text class="owner-name">{{ owner.name }}</text>
              <text class="owner-distance">{{ owner.distance }}</text>
            </view>
            <view class="owner-machine">{{ owner.machine }}</view>
            <view class="owner-meta-row">
              <text class="owner-services">服务 {{ owner.totalServices }} 次</text>
              <text class="owner-rate">好评 {{ owner.goodRate }}%</text>
            </view>
          </view>
          <view class="owner-action">
            <view class="contact-btn" @click.stop="initiateContact(owner)">
              📞 联系
            </view>
          </view>
        </view>

        <!-- 空状态 -->
        <view class="no-owners" v-if="nearbyOwners.length === 0">
          <text class="no-owners-icon"></text>
          <text class="no-owners-text">附近暂时没有空闲机手</text>
          <text class="no-owners-sub">点下面的按鈕，发个需求等师傅来接</text>
        </view>

        <view class="list-bottom-pad"></view>
      </scroll-view>

      <!-- 发布需求兜底按钮 -->
      <view class="post-demand-bar">
        <view class="post-demand-btn" @click="goPostDemand">
          点这里发需求，等师傅来接单
        </view>
      </view>

    </view>

    <!-- 5. 农机主名片弹窗 -->
    <view class="contact-popup animate-fade-in" v-if="showContactPanel" @click="showContactPanel = false">
      <view class="contact-card-wrap scene-enter" @click.stop>
        <operator-card 
          :operator="selectedOwnerCardData" 
          @contact="confirmContact" 
        />
        <view class="close-hint text-center text-xs text-muted mt-4">点击空白处关闭</view>
      </view>
    </view>

    <!-- 6. 隐私同意弹窗 -->
    <privacy-consent-dialog
      ref="privacyDialog"
      :show-phone-consent="true"
      @agreed="onPrivacyAgreed"
      @declined="onPrivacyDeclined"
    />

    <!-- 7. 导航栏 -->
    <custom-tab-bar current="home" />
    <publish-action-sheet />
  </view>
</template>

<script>
import ModeSwitch from '../../components/ModeSwitch.vue'
import CustomTabBar from '../../components/CustomTabBar.vue'
import PublishActionSheet from '../../components/PublishActionSheet.vue'
import PrivacyConsentDialog from '../../components/PrivacyConsentDialog.vue'
import OperatorCard from '../../components/OperatorCard.vue'
import { uiStore } from '../../store/ui'
import { ensurePageAccess } from '../../utils/pageAuth'
import { CROPS, SERVICE_CATEGORIES } from '../../constants/taxonomy'
import { farmerApi } from '../../api/farmer'


export default {
  components: { ModeSwitch, CustomTabBar, PublishActionSheet, PrivacyConsentDialog, OperatorCard },
  data() {
    return {
      uiStore,
      isH5: false,
      location: { lat: 33.3765, lng: 114.0167 }, // 默认河南驻马店
      currentAddress: '正在获取定位...',
      categories: SERVICE_CATEGORIES,
      selectedCategory: SERVICE_CATEGORIES[0].id,
      selectedCropId: 'WHEAT_WINTER',
      areaInput: '',
      nearbyOwners: [],
      selectedOwner: null,
      showContactPanel: false,
      contacting: false,
      privacyConsented: false
    }
  },
  computed: {
    selectedCropName() {
      const crop = CROPS.find(c => c.id === this.selectedCropId)
      return crop ? crop.name : '未知'
    },
    selectedOwnerCardData() {
      if (!this.selectedOwner) return {}
      return {
        ...this.selectedOwner,
        nickname: this.selectedOwner.name,
        avatarUrl: '',
        locationName: this.selectedOwner.locationName,
        serviceCount: this.selectedOwner.serviceCount,
        rating: this.selectedOwner.rating,
        years: this.selectedOwner.years,
        machineName: this.selectedOwner.machineName,
        distance: this.selectedOwner.distance.replace(' km', '')
      }
    }
  },
  onLoad() {
    // #ifdef H5
    this.isH5 = true
    // #endif
  },
  onShow() {
    uni.setNavigationBarTitle({ title: '农助手 · 找农机' })
    ensurePageAccess('FARMER')
    this.getLocation()
    this.loadNearbyOwners()
    // 首次进入时检查隐私授权
    this.$nextTick(() => {
      if (this.$refs.privacyDialog && !this.privacyConsented) {
        this.$refs.privacyDialog.checkAndShow()
      }
    })
  },
  methods: {
    selectCategory(id) {
      this.selectedCategory = id
      this.loadNearbyOwners()
    },
    loadNearbyOwners() {
      // 待后端 ownerApi.listNearby() 接口就绪后替换
      this.nearbyOwners = []
    },
    getLocation() {
      uni.getLocation({
        type: 'gcj02',
        success: (res) => {
          this.location = { lat: res.latitude, lng: res.longitude }
          this.currentAddress = `经度 ${res.longitude.toFixed(4)}, 纬度 ${res.latitude.toFixed(4)}`
        },
        fail: () => {
          this.currentAddress = '定位失败，请授权后重试'
        }
      })
    },
    handleEditLocation() {
      uni.showToast({ title: '地图选点建设中...', icon: 'none' })
    },
    showCropSelector() {
      const cropNames = CROPS.slice(0, 8).map(c => c.name)
      uni.showActionSheet({
        itemList: cropNames,
        success: (res) => { this.selectedCropId = CROPS[res.tapIndex].id }
      })
    },
    selectOwner(owner) {
      this.selectedOwner = owner
      this.showContactPanel = true
    },
    async initiateContact(owner) {
      this.selectedOwner = owner
      this.showContactPanel = true
    },
    async confirmContact() {
      if (!this.selectedOwner || this.contacting) return
      this.contacting = true
      try {
        await farmerApi.initiateContact(
          this.selectedOwner.id,
          this.selectedOwner.serviceItemId,
          'PHONE',
          'BROWSE'
        )
        this.showContactPanel = false
        uni.showToast({ title: '联系已建立！', icon: 'success' })
        setTimeout(() => {
          uni.navigateTo({ url: '/pages/orders/index' })
        }, 1000)
      } catch (e) {
        // 网络未就绪时显示电话号码（降级方案）
        uni.showModal({
          title: '联系方式',
          content: `${this.selectedOwner.name}：${this.selectedOwner.maskedPhone}\n\n网络不稳定，请直接拨打电话。`,
          showCancel: false,
          confirmText: '知道了'
        })
      } finally {
        this.contacting = false
      }
    },
    onPrivacyAgreed({ alreadyConsented } = {}) {
      this.privacyConsented = true
      if (!alreadyConsented) {
        uni.showToast({ title: '感谢您的信任', icon: 'none' })
      }
    },
    onPrivacyDeclined() {
      this.privacyConsented = false
    },
    goPostDemand() {
      uni.navigateTo({ url: '/pages/farmer/demand-create' })
    },
    goProxyOrder() {
      uni.navigateTo({ url: '/pages/farmer/proxy-order' })
    },
    openProfile() {
      uni.navigateTo({ url: '/pages/profile/index' })
    }
  }
}
</script>

<style>
body, page { overflow: hidden; }
</style>

<style scoped>
.home-page {
  position: relative;
  width: 100vw;
  height: 100vh;
  height: 100dvh; /* iOS Safari safe viewport height */
  overflow: hidden;
}

/* 地图底图 */
.full-map {
  width: 100%; height: 100%;
  position: absolute; top: 0; left: 0; z-index: 1;
}
.h5-map-fallback {
  position: absolute; top: 0; left: 0; right: 0; bottom: 0;
}
.map-overlay-tint {
  position: absolute; top: 0; left: 0; right: 0; bottom: 0;
  background: rgba(10, 25, 15, 0.7);
}

/* 地图遮罩（让面板内容清晰） */
.map-dimmer {
  position: absolute; top: 0; left: 0; right: 0; bottom: 0;
  background: linear-gradient(to bottom, transparent 20%, rgba(8, 20, 10, 0.55) 60%, rgba(8, 20, 10, 0.92) 100%);
  z-index: 2;
  pointer-events: none;
}

/* ====== 顶部导航 ====== */
.top-nav-bar {
  position: absolute; top: var(--window-top, 0); left: 0; right: 0;
  z-index: 10; display: flex; justify-content: space-between;
  padding: 24rpx 32rpx; pointer-events: none;
}
.nav-left, .nav-right {
  display: flex; gap: 16rpx; pointer-events: auto; align-items: center;
}
.user-avatar {
  width: 80rpx; height: 80rpx; border-radius: 50%;
  background: rgba(255,255,255,0.85); backdrop-filter: blur(12px);
  display: flex; align-items: center; justify-content: center; font-size: 40rpx;
}
.mode-switch-wrap { transform: scale(0.85); transform-origin: left center; }
.proxy-button {
  background: rgba(255,255,255,0.15); backdrop-filter: blur(12px);
  border: 1px solid rgba(255,255,255,0.25); border-radius: 999rpx;
  padding: 0 24rpx; height: 72rpx;
  display: flex; align-items: center; gap: 8rpx;
  font-size: 26rpx; font-weight: 700; color: #fff;
}
.proxy-icon { font-size: 32rpx; }
.subtle-link { font-size: 24rpx; color: rgba(255,255,255,0.7); padding: 0 12rpx; }

/* ====== 底部主面板 ====== */
.bottom-panel {
  position: absolute; bottom: 0; left: 0; right: 0;
  z-index: 100;
  height: 72vh;
  background: rgba(10, 25, 12, 0.88);
  backdrop-filter: blur(var(--blur-lg)) saturate(150%);
  -webkit-backdrop-filter: blur(var(--blur-lg)) saturate(150%);
  border-top-left-radius: var(--radius-3xl);
  border-top-right-radius: var(--radius-3xl);
  border-top: 1px solid var(--border-light);
  padding: 32rpx 28rpx 0;
  display: flex; flex-direction: column;
}

/* 定位栏 */
.location-bar {
  display: flex; align-items: center; gap: 16rpx;
  padding: 18rpx 24rpx;
  background: rgba(255,255,255,0.07);
  border-radius: var(--radius-lg); margin-bottom: 20rpx;
  border: 1px solid rgba(255,255,255,0.08);
}
.green-dot {
  width: 16rpx; height: 16rpx; border-radius: 50%;
  background: #4ade80; flex-shrink: 0;
  box-shadow: 0 0 8rpx rgba(74,222,128,0.6);
}
.location-text {
  flex: 1; font-size: 28rpx; font-weight: 700; color: #fff;
  white-space: nowrap; overflow: hidden; text-overflow: ellipsis;
}
.location-arrow { color: rgba(255,255,255,0.3); font-size: 24rpx; }

/* 分类 Tab */
.category-tabs {
  width: 100%; white-space: nowrap;
  margin-bottom: 16rpx;
  border-bottom: 1px solid rgba(255,255,255,0.08);
}
.tab-item {
  display: inline-flex; align-items: center; gap: 8rpx;
  padding: 14rpx 28rpx; color: rgba(255,255,255,0.4);
  position: relative; transition: color 0.2s;
}
.tab-item.active { color: #fff; font-weight: 800; }
.tab-item.active::after {
  content: ''; position: absolute; bottom: 0; left: 28rpx; right: 28rpx;
  height: 5rpx; border-radius: 999rpx;
  background: linear-gradient(90deg, #4ade80, #22c55e);
}
.tab-emoji { font-size: 30rpx; }
.tab-name { font-size: 26rpx; }

/* 快选栏 */
.quick-options {
  display: flex; gap: 14rpx; margin-bottom: 18rpx;
}
.option-btn {
  flex: 1; display: flex; align-items: center; justify-content: space-between;
  padding: 18rpx 20rpx;
  background: rgba(255,255,255,0.06);
  border-radius: var(--radius-lg);
  border: 1px solid rgba(255,255,255,0.08);
}
.opt-label { font-size: 22rpx; color: rgba(255,255,255,0.4); }
.opt-val { font-size: 26rpx; font-weight: 700; color: #fff; }
.opt-input { text-align: right; font-size: 26rpx; font-weight: 700; color: #fff; width: 100rpx; background: transparent; }

/* 列表标题行 */
.section-header {
  display: flex; justify-content: space-between; align-items: center;
  margin-bottom: 14rpx;
}
.section-title { font-size: 28rpx; font-weight: 800; color: #fff; }
.section-hint { font-size: 22rpx; color: rgba(255,255,255,0.4); }

/* 农机主列表 */
.owner-list { flex: 1; }

.owner-card {
  display: flex; align-items: center; gap: 16rpx;
  padding: 22rpx 18rpx;
  background: rgba(255,255,255,0.06);
  border-radius: var(--radius-xl);
  border: 1px solid rgba(255,255,255,0.07);
  margin-bottom: 14rpx;
  transition: transform var(--duration-200) var(--ease-in-out), background var(--duration-200);
}
.owner-card.selected {
  background: rgba(74,222,128,0.1);
  border-color: rgba(74,222,128,0.3);
}
.owner-card:active { transform: scale(0.98); }

.owner-left {
  position: relative; flex-shrink: 0;
}
.owner-avatar {
  width: 88rpx; height: 88rpx; border-radius: 50%;
  background: rgba(255,255,255,0.1);
  display: flex; align-items: center; justify-content: center;
  font-size: 46rpx;
}
.owner-badge-wrap {
  position: absolute; bottom: -8rpx; left: 50%; transform: translateX(-50%);
  white-space: nowrap;
}

/* 信誉徽章 */
.rep-badge {
  font-size: 18rpx; font-weight: 800;
  padding: 4rpx 12rpx; border-radius: 100rpx;
  white-space: nowrap;
}
.rep-NEWBIE { background: rgba(148,163,184,0.15); color: #94a3b8; }
.rep-RELIABLE { background: rgba(59,130,246,0.15); color: #60a5fa; }
.rep-VETERAN { background: rgba(16,185,129,0.15); color: #34d399; }
.rep-GOLD { background: rgba(245,158,11,0.15); color: #fbbf24; }
.rep-MODEL { background: rgba(239,68,68,0.15); color: #f87171; }

.owner-main { flex: 1; min-width: 0; }
.owner-name-row { display: flex; justify-content: space-between; align-items: center; margin-bottom: 6rpx; }
.owner-name { font-size: 30rpx; font-weight: 800; color: #fff; }
.owner-distance { font-size: 22rpx; color: rgba(255,255,255,0.4); }
.owner-machine { font-size: 24rpx; color: rgba(255,255,255,0.6); margin-bottom: 6rpx; }
.owner-meta-row { display: flex; gap: 16rpx; }
.owner-services, .owner-rate { font-size: 21rpx; color: rgba(255,255,255,0.35); }

.owner-action { flex-shrink: 0; }
.contact-btn {
  background: var(--gradient-primary);
  color: white; font-size: var(--text-sm); font-weight: var(--font-extrabold);
  padding: 16rpx 24rpx; border-radius: var(--radius-full);
  box-shadow: var(--shadow-colored);
  white-space: nowrap;
}
.contact-btn:active { transform: scale(0.94); }

/* 空状态 */
.no-owners {
  display: flex; flex-direction: column; align-items: center;
  padding: 60rpx 0; gap: 12rpx;
}
.no-owners-icon { font-size: 64rpx; }
.no-owners-text { font-size: 30rpx; font-weight: 700; color: rgba(255,255,255,0.6); }
.no-owners-sub { font-size: 24rpx; color: rgba(255,255,255,0.3); }

.list-bottom-pad { height: 180rpx; }

/* 发布需求兜底栏 */
.post-demand-bar {
  padding: 16rpx 0 calc(16rpx + env(safe-area-inset-bottom));
}
.post-demand-btn {
  background: rgba(255,255,255,0.07);
  border: 1px dashed rgba(255,255,255,0.2);
  border-radius: var(--radius-xl);
  padding: 22rpx;
  font-size: var(--text-sm); color: rgba(255,255,255,0.55);
  text-align: center;
  font-weight: 600;
}
.post-demand-btn:active { background: rgba(255,255,255,0.12); }

/* ====== 联系确认弹窗 ====== */
.contact-popup {
  position: fixed; top: 0; left: 0; right: 0; bottom: 0;
  z-index: 500;
  background: rgba(0,0,0,0.7);
  backdrop-filter: blur(10px);
  display: flex; align-items: center; justify-content: center;
}
.contact-card-wrap {
  width: 90%;
  max-width: 650rpx;
}
.cp-header {
  display: flex; justify-content: space-between; align-items: center;
  margin-bottom: 28rpx;
}
.cp-title { font-size: 34rpx; font-weight: 900; color: #fff; }
.cp-close { font-size: 36rpx; color: rgba(255,255,255,0.4); padding: 0 8rpx; }
.cp-owner-row {
  display: flex; align-items: center; gap: 20rpx; margin-bottom: 28rpx;
  padding: 20rpx; background: rgba(255,255,255,0.06);
  border-radius: var(--radius-xl);
}
.cp-avatar {
  width: 80rpx; height: 80rpx; border-radius: 50%;
  background: rgba(255,255,255,0.1);
  display: flex; align-items: center; justify-content: center; font-size: 44rpx;
}
.cp-info { flex: 1; }
.cp-name { font-size: 32rpx; font-weight: 800; color: #fff; display: block; }
.cp-machine { font-size: 24rpx; color: rgba(255,255,255,0.5); display: block; margin-top: 4rpx; }
.cp-phone-box {
  display: flex; justify-content: space-between; align-items: center;
  padding: 24rpx; background: rgba(255,255,255,0.06);
  border-radius: 18rpx; margin-bottom: 20rpx;
}
.cp-phone-label { font-size: 24rpx; color: rgba(255,255,255,0.5); }
.cp-phone { font-size: 32rpx; font-weight: 800; color: #4ade80; letter-spacing: 2rpx; }
.cp-tip {
  padding: 16rpx 18rpx;
  background: rgba(245,158,11,0.08); border-radius: 14rpx;
  font-size: 22rpx; color: #fbbf24; line-height: 1.6;
  margin-bottom: 28rpx;
}
.cp-actions { display: flex; flex-direction: column; gap: 16rpx; }
.cp-btn-primary {
  height: 100rpx; background: linear-gradient(135deg, #16a34a, #22c55e);
  color: white; font-size: 34rpx; font-weight: 800; border-radius: 999rpx;
  display: flex; align-items: center; justify-content: center;
  box-shadow: 0 12rpx 28rpx rgba(34,197,94,0.35); letter-spacing: 2rpx;
}
.cp-btn-primary:active { transform: scale(0.97); }
.cp-btn-secondary {
  height: 76rpx; background: transparent; color: rgba(255,255,255,0.4);
  font-size: 28rpx; display: flex; align-items: center; justify-content: center;
}

/* ====== 老年友好模式 ====== */
html.elder-mode .section-title { font-size: 34rpx; }
html.elder-mode .owner-name { font-size: 36rpx; }
html.elder-mode .contact-btn { font-size: 30rpx; padding: 20rpx 30rpx; }
html.elder-mode .cp-phone { font-size: 40rpx; }
html.elder-mode .cp-btn-primary { height: 120rpx; font-size: 40rpx; }
html.elder-mode .location-text { font-size: 34rpx; }
</style>
