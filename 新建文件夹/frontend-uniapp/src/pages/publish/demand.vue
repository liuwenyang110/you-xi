<template>
  <view class="demand-publish-page">
    <!-- 顶部公益声明栏 -->
    <view class="charity-header">
      <text class="charity-title">🌾 发布求助需求</text>
      <text class="charity-sub">信息免费发布 · 服务者主动联系您</text>
    </view>

    <!-- 作业类型 -->
    <view class="form-section">
      <text class="section-label">作业类型 *</text>
      <scroll-view class="type-scroll" scroll-x>
        <view
          v-for="t in workTypes"
          :key="t.value"
          class="type-chip"
          :class="{ active: form.workType === t.value }"
          @click="form.workType = t.value"
        >
          {{ t.label }}
        </view>
      </scroll-view>
    </view>

    <!-- 地块位置 -->
    <view class="form-section">
      <text class="section-label">📍 地块位置 *</text>
      <view class="input-wrap" @click="pickLocation">
        <text class="input-text" :class="{ placeholder: !form.location }">
          {{ form.location || '点击获取当前位置或手动填写...' }}
        </text>
        <text class="input-icon">›</text>
      </view>
      <input
        class="detail-input"
        v-model="form.locationDetail"
        placeholder="可补充详细位置，如：东沟边、村头水塘北侧"
      />
    </view>

    <!-- 作业面积 -->
    <view class="form-section">
      <text class="section-label">📐 作业面积</text>
      <view class="area-row">
        <input
          class="area-input"
          type="digit"
          v-model="form.areaMu"
          placeholder="估计亩数"
        />
        <text class="area-unit">亩</text>
        <text class="area-hint">（不确定可不填）</text>
      </view>
    </view>

    <!-- 期望时间 -->
    <view class="form-section">
      <text class="section-label">📅 期望作业时间</text>
      <view class="time-slots">
        <view
          v-for="s in timeSlots"
          :key="s.value"
          class="slot-btn"
          :class="{ active: form.timeSlot === s.value }"
          @click="form.timeSlot = s.value"
        >{{ s.label }}</view>
      </view>
    </view>

    <!-- 补充说明 -->
    <view class="form-section">
      <text class="section-label">📝 补充说明（可选）</text>
      <textarea
        class="remark-input"
        v-model="form.remark"
        placeholder="如：地势偏低、需要收割机带脱粒..."
        maxlength="200"
      />
    </view>

    <!-- 公益声明 -->
    <view class="charity-notice">
      <text class="notice-text">
        🌿 本平台完全免费，您的需求将公开展示于片区，
        有意向的服务者会主动与您联系，请留意电话。
      </text>
    </view>

    <!-- 提交按钮 -->
    <view class="submit-wrap">
      <button
        class="submit-btn"
        :loading="submitting"
        :disabled="!canSubmit"
        @click="submitDemand"
      >发布求助需求</button>
    </view>
  </view>
</template>

<script>
import { publishDemand } from '@/api/v3/demand'

export default {
  name: 'DemandPublish',
  data() {
    return {
      submitting: false,
      form: {
        workType: '',
        location: '',
        locationDetail: '',
        areaMu: '',
        timeSlot: '',
        remark: '',
      },
      workTypes: [
        { label: '🌾 小麦收割', value: 'WHEAT_HARVEST' },
        { label: '🌽 玉米收割', value: 'CORN_HARVEST' },
        { label: '✈️ 无人机植保', value: 'DRONE_SPRAY' },
        { label: '🚜 深翻整地', value: 'TILLAGE' },
        { label: '🌱 播种', value: 'SEEDING' },
        { label: '📦 秸秆打捆', value: 'STRAW_BALING' },
      ],
      timeSlots: [
        { label: '今天', value: 'TODAY' },
        { label: '明天', value: 'TOMORROW' },
        { label: '两天内', value: 'TWO_DAYS' },
        { label: '本周内', value: 'THIS_WEEK' },
        { label: '不限时间', value: 'FLEXIBLE' },
      ],
    }
  },
  computed: {
    canSubmit() {
      return !this.submitting && !!this.form.workType && !!this.form.location
    }
  },
  methods: {
    async pickLocation() {
      try {
        const res = await new Promise((resolve, reject) => {
          uni.getLocation({
            type: 'gcj02',
            success: resolve,
            fail: reject,
          })
        })
        this.form.location = `${res.latitude.toFixed(4)}, ${res.longitude.toFixed(4)}`
        uni.showToast({ title: '已获取当前位置', icon: 'success' })
      } catch {
        // 用户拒绝定位，改为手动输入
        this.$nextTick(() => {
          uni.showModal({
            title: '请手动填写位置',
            content: '如：西平县盆尧镇马湾村东地',
            editable: true,
            success: (r) => {
              if (r.confirm && r.content) this.form.location = r.content
            }
          })
        })
      }
    },

    async submitDemand() {
      if (!this.canSubmit) return
      this.submitting = true
      try {
        const res = await publishDemand({
          workType: this.form.workType,
          locationName: this.form.location + (this.form.locationDetail ? ' ' + this.form.locationDetail : ''),
          areaMu: this.form.areaMu ? Number(this.form.areaMu) : null,
          timeSlot: this.form.timeSlot,
          remark: this.form.remark,
        })
        if (res.code === 0) {
          uni.showToast({ title: '需求已发布！等待服务者联系', icon: 'success' })
          setTimeout(() => uni.navigateBack(), 1500)
        } else {
          uni.showToast({ title: res.message || '发布失败', icon: 'none' })
        }
      } catch {
        uni.showToast({ title: '网络异常，请重试', icon: 'none' })
      } finally {
        this.submitting = false
      }
    }
  }
}
</script>

<style>
.demand-publish-page {
  min-height: 100vh;
  background: #F5F9F6;
  padding-bottom: 100rpx;
}

/* ── 顶部公益声明 ── */
.charity-header {
  background: linear-gradient(135deg, #2D7A4F, #5BA88A);
  padding: 80rpx 40rpx 48rpx;
}
.charity-title {
  display: block;
  font-size: 42rpx;
  font-weight: 700;
  color: #fff;
}
.charity-sub {
  display: block;
  font-size: 26rpx;
  color: rgba(255,255,255,0.85);
  margin-top: 8rpx;
}

/* ── 表单区 ── */
.form-section {
  background: #fff;
  margin: 16rpx 24rpx 0;
  border-radius: 20rpx;
  padding: 28rpx 32rpx;
  box-shadow: 0 2rpx 12rpx rgba(45,122,79,0.06);
}
.section-label {
  display: block;
  font-size: 28rpx;
  font-weight: 700;
  color: #1A3A28;
  margin-bottom: 20rpx;
}

/* 作业类型横向滚动 */
.type-scroll { white-space: nowrap; }
.type-chip {
  display: inline-flex;
  align-items: center;
  padding: 12rpx 28rpx;
  background: #EAF5EE;
  color: #2D7A4F;
  border-radius: 40rpx;
  font-size: 28rpx;
  font-weight: 600;
  margin-right: 16rpx;
  border: 2rpx solid #C3E6D0;
  white-space: nowrap;
}
.type-chip.active {
  background: #2D7A4F;
  color: #fff;
  border-color: #2D7A4F;
}

/* 位置输入 */
.input-wrap {
  display: flex;
  align-items: center;
  background: #F0F7F3;
  border-radius: 14rpx;
  padding: 20rpx 24rpx;
  margin-bottom: 16rpx;
}
.input-text { flex: 1; font-size: 28rpx; color: #1A3A28; }
.input-text.placeholder { color: #A0C4B5; }
.input-icon { font-size: 36rpx; color: #6B8F7A; }
.detail-input {
  width: 100%;
  background: #F0F7F3;
  border-radius: 14rpx;
  padding: 18rpx 24rpx;
  font-size: 28rpx;
  color: #1A3A28;
  box-sizing: border-box;
}

/* 面积行 */
.area-row { display: flex; align-items: center; gap: 16rpx; }
.area-input {
  width: 160rpx;
  background: #F0F7F3;
  border-radius: 14rpx;
  padding: 18rpx 20rpx;
  font-size: 30rpx;
  text-align: center;
}
.area-unit { font-size: 30rpx; color: #1A3A28; font-weight: 600; }
.area-hint { font-size: 24rpx; color: #A0C4B5; }

/* 时间槽 */
.time-slots { display: flex; flex-wrap: wrap; gap: 16rpx; }
.slot-btn {
  padding: 14rpx 28rpx;
  background: #EAF5EE;
  color: #2D7A4F;
  border-radius: 40rpx;
  font-size: 28rpx;
  font-weight: 600;
  border: 2rpx solid #C3E6D0;
}
.slot-btn.active {
  background: #2D7A4F;
  color: #fff;
  border-color: #2D7A4F;
}

/* 备注 */
.remark-input {
  width: 100%;
  min-height: 120rpx;
  background: #F0F7F3;
  border-radius: 14rpx;
  padding: 18rpx 24rpx;
  font-size: 28rpx;
  color: #1A3A28;
  box-sizing: border-box;
}

/* ── 公益声明 ── */
.charity-notice {
  margin: 16rpx 24rpx 0;
  background: rgba(45,122,79,0.06);
  border-radius: 16rpx;
  padding: 24rpx 28rpx;
  border-left: 6rpx solid #5BA88A;
}
.notice-text {
  font-size: 26rpx;
  color: #2D5A3D;
  line-height: 1.8;
}

/* ── 提交按钮 ── */
.submit-wrap {
  padding: 32rpx 24rpx 0;
}
.submit-btn {
  width: 100%;
  height: 96rpx;
  background: linear-gradient(135deg, #5BA88A, #2D7A4F);
  color: #fff;
  border-radius: 48rpx;
  font-size: 34rpx;
  font-weight: 700;
  border: none;
  box-shadow: 0 8rpx 32rpx rgba(45,122,79,0.3);
}
.submit-btn[disabled] {
  opacity: 0.5;
  box-shadow: none;
}
</style>
