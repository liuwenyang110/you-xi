<template>
  <view class="page proxy-page">
    <view class="card proxy-hero scene-enter">
      <view class="proxy-emoji"></view>
      <view class="proxy-hero-title">帮家人下单</view>
      <view class="proxy-hero-sub">爸妈不会用手机？您可以远程帮他们找农机</view>
    </view>

    <view class="card scene-enter delay-1">
      <view class="section-title">家人信息</view>

      <view class="form-group">
        <text class="form-label">家人称呼</text>
        <view class="form-input-box">
          <input class="form-input" v-model="familyName" placeholder="如：爸爸、妈妈、二叔" maxlength="20" />
        </view>
      </view>

      <view class="form-group">
        <text class="form-label">家人手机号（选填）</text>
        <view class="form-input-box">
          <input class="form-input" type="number" v-model="familyPhone" placeholder="机手可能需要联系家人" maxlength="11" />
        </view>
      </view>

      <view class="form-group">
        <text class="form-label">地块位置描述</text>
        <view class="form-input-box">
          <input class="form-input" v-model="location" placeholder="如：李庄村东头那块地" maxlength="100" />
        </view>
      </view>
    </view>

    <view class="card scene-enter delay-2">
      <view class="section-title">作业需求</view>

      <view class="form-group">
        <text class="form-label">作物类型</text>
        <view class="crop-tags">
          <view
            v-for="crop in cropOptions"
            :key="crop.code"
            class="crop-tag"
            :class="{ 'crop-tag-active': selectedCrop === crop.code }"
            @click="selectedCrop = crop.code"
          >
            {{ crop.emoji }} {{ crop.name }}
          </view>
        </view>
      </view>

      <view class="form-group">
        <text class="form-label">预估面积（亩）</text>
        <view class="form-input-box">
          <input class="form-input" type="digit" v-model="areaMu" placeholder="大概多少亩？" />
        </view>
      </view>

      <view class="form-group">
        <text class="form-label">备注（给机手的话）</text>
        <view class="form-input-box">
          <textarea class="form-textarea" v-model="remark" placeholder="比如：地头有棵大柳树，从那边进去" maxlength="200" />
        </view>
      </view>
    </view>

    <view class="scene-enter delay-3" style="padding: 0 4rpx;">
      <button class="btn btn-primary proxy-submit-btn" @click="handleSubmit" :disabled="submitting">
        {{ submitting ? '正在提交…' : '帮家人发布需求' }}
      </button>
      <view class="proxy-note">提交后，系统会自动为您家人匹配附近的农机</view>
    </view>
  </view>
</template>

<script>
import { farmerApi } from '../../api/farmer'
import { uiStore } from '../../store/ui'

export default {
  data() {
    return {
      familyName: '',
      familyPhone: '',
      location: '',
      selectedCrop: 'WHEAT_WINTER',
      areaMu: '',
      remark: '',
      submitting: false,
      cropOptions: [
        { code: 'WHEAT_WINTER', name: '冬小麦', emoji: '' },
        { code: 'CORN_YELLOW', name: '大田玉米', emoji: '' },
        { code: 'RICE_MID_LATE', name: '中晚稻', emoji: '🌿' },
        { code: 'PEANUT', name: '花生', emoji: '' }
      ]
    }
  },
  methods: {
    async handleSubmit() {
      if (!this.familyName.trim()) {
        uni.showToast({ title: '请填写家人称呼', icon: 'none' })
        return
      }
      if (!this.location.trim()) {
        uni.showToast({ title: '请填写地块位置', icon: 'none' })
        return
      }
      if (!this.areaMu || Number(this.areaMu) <= 0) {
        uni.showToast({ title: '请填写预估面积', icon: 'none' })
        return
      }

      this.submitting = true
      try {
        await farmerApi.createDemand({
          serviceCategoryId: 3,
          serviceSubcategoryId: 301,
          cropCode: this.selectedCrop,
          areaMu: Number(this.areaMu),
          villageName: this.location,
          scheduleType: 'three_days',
          remark: `[代下单] 帮${this.familyName}下单${this.familyPhone ? '，联系电话：' + this.familyPhone : ''}。${this.remark}`
        })
        uni.showToast({ title: '已成功帮家人发布！', icon: 'success', duration: 2000 })
        setTimeout(() => {
          uni.navigateBack()
        }, 1500)
      } catch (e) {
        uni.showToast({ title: e?.message || '提交失败', icon: 'none' })
      } finally {
        this.submitting = false
      }
    }
  }
}
</script>

<style>
.proxy-page {
  min-height: 100vh;
  padding: 28rpx 24rpx 56rpx;
}

.proxy-hero {
  text-align: center;
  padding: 48rpx 32rpx;
  background: linear-gradient(135deg, rgba(245, 158, 11, 0.06), rgba(16, 185, 129, 0.04));
}

.proxy-emoji { font-size: 64rpx; margin-bottom: 16rpx; }
.proxy-hero-title { font-size: 40rpx; font-weight: 800; color: var(--text-primary); }
.proxy-hero-sub { margin-top: 12rpx; font-size: 26rpx; color: var(--text-muted); line-height: 1.6; }

.section-title {
  font-size: 32rpx;
  font-weight: 800;
  color: var(--text-primary);
  margin-bottom: 20rpx;
}

.crop-tags { display: flex; flex-wrap: wrap; gap: 14rpx; }
.crop-tag {
  padding: 16rpx 24rpx;
  border-radius: 999rpx;
  font-size: 28rpx;
  font-weight: 600;
  background: var(--secondary-color);
  color: var(--text-regular);
  border: 2px solid transparent;
  transition: all 0.2s;
}
.crop-tag-active {
  background: rgba(16, 185, 129, 0.1);
  color: var(--primary-strong);
  border-color: var(--primary-color);
}

.form-textarea {
  width: 100%;
  min-height: 140rpx;
  padding: 20rpx;
  font-size: 30rpx;
  border: none;
  background: transparent;
  line-height: 1.6;
}

.proxy-submit-btn {
  width: 100%;
  margin-top: 28rpx;
  min-height: 120rpx;
  font-size: 36rpx;
  font-weight: 700;
}

.proxy-note {
  margin-top: 16rpx;
  text-align: center;
  font-size: 24rpx;
  color: var(--text-soft);
}
</style>
