<template>
  <view class="operator-detail-page">
    <view class="page-header">
      <view class="avatar-wrap">
        <view class="avatar-circle">
          <text class="avatar-icon"></text>
        </view>
        <view class="verified-badge" v-if="operator.verified">
          <text>✓ 已认证</text>
        </view>
      </view>
      <text class="op-name">{{ operator.realName || ('农机手' + operator.id) }}</text>
      <text class="op-zone">{{ zoneName }}</text>
    </view>

    <!-- 联系方式 -->
    <view class="contact-card">
      <view class="contact-header">
        <text class="contact-title">📞 直接联系</text>
        <text class="contact-tip">公益平台 · 联系完全免费</text>
      </view>
      <view class="phone-display" v-if="operator.virtualPhone">
        <text class="phone-number">{{ operator.virtualPhone }}</text>
        <button class="call-btn" @click="callPhone(operator.virtualPhone)">
          📞 一键拨打
        </button>
      </view>
      <view class="phone-empty" v-else>
        <text class="phone-empty-text">该服务者暂未设置联系方式，可通过片区群联系</text>
      </view>
    </view>

    <!-- 农机列表 -->
    <view class="machinery-section">
      <text class="section-title">拥有农机</text>

      <view v-if="machineList.length === 0" class="empty-machine">
        <text>暂未登记农机信息</text>
      </view>

      <view
        v-for="m in machineList"
        :key="m.id"
        class="machine-card"
      >
        <view class="machine-card-head">
          <view class="machine-type-tag">{{ getMachineTypeName(m.machineTypeId) }}</view>
          <view class="cross-tag" v-if="m.isCrossRegion">🗺 跨区可接</view>
        </view>

        <view class="machine-info-block">
          <text class="machine-brand-model" v-if="m.brand || m.modelNo">
            {{ m.brand }} {{ m.modelNo }}
          </text>

          <!-- 适用作物 -->
          <view class="crops-wrap" v-if="m.suitableCrops">
            <text class="detail-label">可收</text>
            <view class="crop-chips">
              <text
                v-for="cropCode in m.suitableCrops.split(',')"
                :key="cropCode"
                class="crop-chip"
              >{{ getCropName(cropCode.trim()) }}</text>
            </view>
          </view>

          <!-- 跨区说明 -->
          <view class="cross-desc" v-if="m.isCrossRegion && m.crossRangeDesc">
            <text class="detail-label">跨区</text>
            <text class="cross-desc-text">{{ m.crossRangeDesc }}</text>
          </view>

          <!-- 可用时间 -->
          <view class="avail-row" v-if="m.availDesc">
            <text class="detail-label">时间</text>
            <text class="avail-text">{{ m.availDesc }}</text>
          </view>

          <!-- 其他说明 -->
          <view class="desc-row" v-if="m.description">
            <text class="detail-label">备注</text>
            <text class="desc-text">{{ m.description }}</text>
          </view>
        </view>
      </view>
    </view>

    <!-- 公益声明 -->
    <view class="bottom-tip">
      <text class="btip-icon">🌿</text>
      <text class="btip-text">本平台完全免费提供信息对接服务，价格和时间请您与服务者直接商定，平台不收取任何费用</text>
    </view>
  </view>
</template>

<script>
import { getZoneMachinery } from '@/api/v3/machinery'
import { getZoneOperators } from '@/api/v3/user'
import { useV3ZoneStore } from '@/stores/v3/zoneStore'
import { useV3UserStore } from '@/stores/v3/userStore'

export default {
  name: 'OperatorDetail',
  data() {
    return {
      operatorId: null,
      operator: {},
      machineList: [],
    }
  },
  computed: {
    zoneName() { return useV3ZoneStore().zoneName },
  },
  async onLoad(options) {
    this.operatorId = Number(options.operatorId)
    const store = useV3ZoneStore()
    if (!store.dictsLoaded) await store.loadDicts()
    await this.loadData()
  },
  methods: {
    async loadData() {
      const zoneId = useV3UserStore().zoneId
      try {
        // 获取片区所有机手（取当前机手信息）
        const opRes = await getZoneOperators(zoneId)
        if (opRes.code === 0) {
          this.operator = opRes.data.find(o => o.id === this.operatorId) || {}
        }
        // 获取片区农机列表（过滤当前机手）
        const machineRes = await getZoneMachinery(zoneId)
        if (machineRes.code === 0) {
          this.machineList = machineRes.data.filter(m => m.operatorId === this.operatorId)
        }
      } catch (e) {
        uni.showToast({ title: '加载失败', icon: 'none' })
      }
    },
    getMachineTypeName(id) {
      return useV3ZoneStore().machineTypeName(id)
    },
    getCropName(code) {
      const crop = useV3ZoneStore().dicts.crops.find(c => c.code === code)
      return crop?.name || code
    },
    callPhone(phone) {
      uni.makePhoneCall({
        phoneNumber: phone,
        fail: () => uni.showToast({ title: '拨号失败，请手动拨打', icon: 'none' })
      })
    }
  }
}
</script>

<style scoped>
.operator-detail-page { background: var(--page-bg); min-height: 100vh; min-height: 100dvh; padding-bottom: 40rpx; }

.page-header {
  background: var(--gradient-primary);
  padding: 80rpx 40rpx 48rpx;
  display: flex;
  flex-direction: column;
  align-items: center;
}
.avatar-wrap { position: relative; margin-bottom: 20rpx; }
.avatar-circle {
  width: 140rpx;
  height: 140rpx;
  background: rgba(255,255,255,0.2);
  border: 4rpx solid rgba(255,255,255,0.5);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
}
.avatar-icon { font-size: 80rpx; }
.verified-badge {
  position: absolute;
  bottom: 0;
  right: -8rpx;
  background: var(--primary-color);
  border-radius: var(--radius-xl);
  padding: 4rpx 12rpx;
  font-size: 20rpx;
  color: #fff;
}
.op-name { font-size: 44rpx; font-weight: var(--font-bold); color: #fff; margin-bottom: 10rpx; }
.op-zone { font-size: var(--text-sm); color: rgba(255,255,255,0.8); }

.contact-card {
  background: var(--surface-deep);
  margin: 20rpx 24rpx 0;
  border-radius: var(--radius-xl);
  padding: 28rpx;
  box-shadow: var(--shadow-base);
}
.contact-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20rpx; }
.contact-title { font-size: var(--text-base); font-weight: var(--font-bold); color: var(--text-primary); }
.contact-tip { font-size: var(--text-xs); color: var(--text-soft); }
.phone-display { display: flex; align-items: center; justify-content: space-between; }
.phone-number { font-size: 44rpx; font-weight: var(--font-bold); color: var(--text-primary); letter-spacing: 4rpx; }
.call-btn {
  background: var(--gradient-primary);
  color: #fff;
  border: none;
  border-radius: var(--radius-full);
  font-size: var(--text-base);
  padding: 16rpx 32rpx;
  box-shadow: var(--shadow-colored);
}
.phone-empty-text { font-size: var(--text-base); color: var(--text-soft); line-height: 1.6; }

.machinery-section { margin: 20rpx 24rpx 0; }
.section-title { display: block; font-size: var(--text-lg); font-weight: var(--font-bold); color: var(--text-primary); margin-bottom: 16rpx; }

.empty-machine {
  background: var(--surface-deep);
  border-radius: var(--radius-lg);
  padding: 40rpx;
  text-align: center;
  font-size: var(--text-base);
  color: var(--text-soft);
}

.machine-card {
  background: var(--surface-deep);
  border-radius: var(--radius-lg);
  padding: 28rpx;
  margin-bottom: 16rpx;
  box-shadow: var(--shadow-sm);
  border-left: 6rpx solid var(--primary-color);
}
.machine-card-head {
  display: flex;
  align-items: center;
  gap: 16rpx;
  margin-bottom: 16rpx;
}
.machine-type-tag {
  background: var(--color-success-bg);
  border-radius: var(--radius-full);
  padding: 8rpx 22rpx;
  font-size: var(--text-base);
  color: var(--primary-strong);
  font-weight: var(--font-bold);
}
.cross-tag {
  background: var(--color-info-bg);
  border-radius: var(--radius-full);
  padding: 6rpx 18rpx;
  font-size: var(--text-xs);
  color: var(--color-info);
}
.machine-brand-model { display: block; font-size: var(--text-base); color: var(--text-regular); font-weight: var(--font-semibold); margin-bottom: 12rpx; }
.detail-label { font-size: var(--text-xs); color: var(--text-soft); margin-right: 10rpx; }
.crops-wrap { display: flex; align-items: center; margin-bottom: 10rpx; flex-wrap: wrap; gap: 10rpx; }
.crop-chips { display: flex; flex-wrap: wrap; gap: 8rpx; }
.crop-chip {
  background: var(--color-warning-bg);
  border-radius: var(--radius-xl);
  padding: 4rpx 16rpx;
  font-size: var(--text-xs);
  color: var(--color-warning);
}
.cross-desc { display: flex; align-items: flex-start; margin-bottom: 10rpx; }
.cross-desc-text { font-size: var(--text-sm); color: var(--color-info); flex: 1; line-height: 1.5; }
.avail-row { display: flex; align-items: center; margin-bottom: 8rpx; }
.avail-text { font-size: var(--text-sm); color: var(--text-muted); }
.desc-row { display: flex; align-items: flex-start; }
.desc-text { font-size: var(--text-sm); color: var(--text-muted); flex: 1; line-height: 1.5; }

.bottom-tip {
  display: flex;
  align-items: flex-start;
  background: var(--color-info-bg);
  margin: 20rpx 24rpx 0;
  border-radius: var(--radius-lg);
  padding: 20rpx 24rpx;
}
.btip-icon { font-size: var(--text-base); margin-right: 10rpx; flex-shrink: 0; }
.btip-text { font-size: var(--text-sm); color: var(--color-info); line-height: 1.6; }
</style>
