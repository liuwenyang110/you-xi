<template>
  <view class="demand-publish">
    <view class="page-header">
      <text class="page-title">{{ isGroup ? '发起合集需求' : '发布需求' }}</text>
      <text class="page-sub">{{ isGroup ? '邀请邻居一起联合，量大机手更愿来' : '告诉机手你的地块情况' }}</text>
    </view>

    <!-- 模式切换（个人 / 合集） -->
    <view class="mode-switch">
      <view
        class="mode-btn"
        :class="{ active: !isGroup }"
        @click="isGroup = false"
      >个人发布</view>
      <view
        class="mode-btn"
        :class="{ active: isGroup }"
        @click="isGroup = true"
      >合集联合 🤝</view>
    </view>

    <view class="form-wrap">
      <!-- 合集标题（合集模式） -->
      <view class="form-item" v-if="isGroup">
        <text class="form-label">合集名称</text>
        <input
          class="form-input"
          v-model="form.title"
          placeholder="如：南头地块统一收割"
          maxlength="30"
        />
      </view>

      <!-- 作业类型 -->
      <view class="form-item">
        <text class="form-label">需要做什么 <text class="required">*</text></text>
        <view class="work-type-grid">
          <view
            v-for="wt in workTypes"
            :key="wt.id"
            class="wt-chip"
            :class="{ selected: form.workTypeId === wt.id }"
            @click="form.workTypeId = wt.id"
          >{{ wt.name }}</view>
        </view>
      </view>

      <!-- 作物品种（可选） -->
      <view class="form-item">
        <text class="form-label">作物品种（可选）</text>
        <picker
          mode="selector"
          :range="cropOptions"
          range-key="label"
          :value="selectedCropIndex"
          @change="onCropChange"
        >
          <view class="picker-field">
            <text class="picker-text">{{ selectedCropName || '请选择（可不填）' }}</text>
            <text class="picker-arrow">›</text>
          </view>
        </picker>
      </view>

      <!-- 面积描述 -->
      <view class="form-item">
        <text class="form-label">地块面积</text>
        <input
          class="form-input"
          v-model="form.areaDesc"
          placeholder="如：约8亩，或8-12亩"
          maxlength="20"
        />
      </view>

      <!-- 期望时间 -->
      <view class="form-item">
        <text class="form-label">期望作业时间</text>
        <view class="date-row">
          <picker mode="date" :value="form.expectDateStart" @change="e => form.expectDateStart = e.detail.value">
            <view class="date-input">
              <text>{{ form.expectDateStart || '开始日期' }}</text>
            </view>
          </picker>
          <text class="date-sep">至</text>
          <picker mode="date" :value="form.expectDateEnd" @change="e => form.expectDateEnd = e.detail.value">
            <view class="date-input">
              <text>{{ form.expectDateEnd || '结束日期' }}</text>
            </view>
          </picker>
        </view>
      </view>

      <!-- 地块位置描述 -->
      <view class="form-item">
        <text class="form-label">地块在哪里</text>
        <textarea
          class="form-textarea"
          v-model="form.locationDesc"
          placeholder="如：村东头灌溉渠南边，王家旁边那块"
          maxlength="100"
          auto-height
        />
      </view>

      <!-- 地块注意事项 -->
      <view class="form-item">
        <text class="form-label">特别注意事项</text>
        <textarea
          class="form-textarea warn-textarea"
          v-model="form.plotNotes"
          placeholder="如：有水渠需注意、地头有大树、地软不好走等，如实告知"
          maxlength="100"
          auto-height
        />
      </view>
    </view>

    <!-- 提交 -->
    <view class="submit-wrap">
      <button
        class="submit-btn"
        :disabled="!canSubmit || loading"
        :loading="loading"
        @click="submit"
      >{{ isGroup ? '发起合集招募' : '发布到片区公告栏' }}</button>
    </view>
  </view>
</template>

<script>
import { publishDemand, createGroup } from '@/api/v3/demand'
import { useV3ZoneStore } from '@/stores/v3/zoneStore'
import { useV3UserStore } from '@/stores/v3/userStore'

export default {
  name: 'DemandPublish',
  data() {
    return {
      isGroup: false,
      loading: false,
      form: {
        title: '',
        workTypeId: null,
        cropId: null,
        areaDesc: '',
        expectDateStart: '',
        expectDateEnd: '',
        locationDesc: '',
        plotNotes: '',
      },
      selectedCropIndex: 0,
      selectedCropName: '',
    }
  },
  computed: {
    workTypes() {
      return useV3ZoneStore().dicts.workTypes || []
    },
    crops() {
      return useV3ZoneStore().dicts.crops || []
    },
    cropOptions() {
      return [{ label: '不限 / 其他', code: null }, ...this.crops.map(c => ({ label: `${c.name}${c.alias ? ' / ' + c.alias.split(',')[0] : ''}`, code: c.code, id: c.id }))]
    },
    canSubmit() {
      return !!this.form.workTypeId
    }
  },
  async onLoad(query) {
    if (query.mode === 'group') this.isGroup = true
    const store = useV3ZoneStore()
    if (!store.dictsLoaded) await store.loadDicts()
  },
  methods: {
    onCropChange(e) {
      this.selectedCropIndex = e.detail.value
      const opt = this.cropOptions[e.detail.value]
      this.selectedCropName = opt?.label || ''
      this.form.cropId = opt?.id || null
    },
    async submit() {
      if (!this.canSubmit) return
      this.loading = true
      try {
        const payload = { ...this.form }
        if (!payload.expectDateStart) delete payload.expectDateStart
        if (!payload.expectDateEnd) delete payload.expectDateEnd
        if (!payload.cropId) delete payload.cropId

        const api = this.isGroup ? createGroup : publishDemand
        const res = await api(payload)
        if (res.code === 0) {
          uni.showToast({ title: this.isGroup ? '合集需求已发起！' : '需求已发布到片区！', icon: 'success' })
          setTimeout(() => uni.navigateBack(), 1000)
        } else {
          uni.showToast({ title: res.message || '发布失败', icon: 'none' })
        }
      } catch (e) {
        uni.showToast({ title: '网络异常', icon: 'none' })
      } finally {
        this.loading = false
      }
    }
  }
}
</script>

<style>
.demand-publish { background: #F5F8F2; min-height: 100vh; padding-bottom: 120rpx; }
.page-header {
  background: linear-gradient(135deg, #3E6B2E, #5A9A40);
  padding: 80rpx 40rpx 40rpx;
}
.page-title { display: block; font-size: 40rpx; font-weight: 700; color: #fff; margin-bottom: 8rpx; }
.page-sub { display: block; font-size: 26rpx; color: rgba(255,255,255,0.8); }

.mode-switch {
  display: flex;
  background: #fff;
  margin: 20rpx 24rpx;
  border-radius: 16rpx;
  overflow: hidden;
  box-shadow: 0 2rpx 10rpx rgba(0,0,0,0.06);
}
.mode-btn {
  flex: 1;
  text-align: center;
  padding: 24rpx;
  font-size: 28rpx;
  color: #888;
  border-radius: 16rpx;
  transition: all 0.2s;
}
.mode-btn.active {
  background: linear-gradient(135deg, #4CAF50, #2E7D32);
  color: #fff;
  font-weight: 700;
}

.form-wrap {
  background: #fff;
  margin: 0 24rpx;
  border-radius: 20rpx;
  padding: 8rpx 0;
  box-shadow: 0 4rpx 16rpx rgba(0,0,0,0.05);
}
.form-item { padding: 28rpx 32rpx; border-bottom: 1rpx solid #F5F5F5; }
.form-item:last-child { border-bottom: none; }
.form-label { display: block; font-size: 30rpx; font-weight: 600; color: #333; margin-bottom: 20rpx; }
.required { color: #F44336; }

.form-input {
  display: block;
  width: 100%;
  border: 1rpx solid #E8F0E4;
  border-radius: 12rpx;
  padding: 20rpx 24rpx;
  font-size: 30rpx;
  background: #FAFCF9;
  box-sizing: border-box;
}
.form-textarea {
  display: block;
  width: 100%;
  border: 1rpx solid #E8F0E4;
  border-radius: 12rpx;
  padding: 20rpx 24rpx;
  font-size: 28rpx;
  background: #FAFCF9;
  min-height: 100rpx;
  box-sizing: border-box;
  line-height: 1.6;
}
.warn-textarea { border-color: #FFCCBC; background: #FFF8F6; }

.work-type-grid { display: flex; flex-wrap: wrap; gap: 16rpx; }
.wt-chip {
  padding: 14rpx 28rpx;
  border: 2rpx solid #DDD;
  border-radius: 40rpx;
  font-size: 26rpx;
  color: #666;
  background: #F8F8F8;
}
.wt-chip.selected {
  border-color: #4CAF50;
  background: #E8F5E9;
  color: #2E7D32;
  font-weight: 700;
}

.picker-field {
  display: flex;
  align-items: center;
  justify-content: space-between;
  border: 1rpx solid #E8F0E4;
  border-radius: 12rpx;
  padding: 20rpx 24rpx;
  background: #FAFCF9;
}
.picker-text { font-size: 30rpx; color: #333; }
.picker-arrow { font-size: 32rpx; color: #999; }

.date-row { display: flex; align-items: center; gap: 16rpx; }
.date-input {
  flex: 1;
  border: 1rpx solid #E8F0E4;
  border-radius: 12rpx;
  padding: 18rpx 20rpx;
  background: #FAFCF9;
  font-size: 28rpx;
  color: #333;
  text-align: center;
}
.date-sep { font-size: 28rpx; color: #888; }

.submit-wrap {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  padding: 20rpx 32rpx 48rpx;
  background: rgba(245,248,242,0.96);
  backdrop-filter: blur(10px);
}
.submit-btn {
  width: 100%;
  height: 96rpx;
  background: linear-gradient(135deg, #4CAF50, #2E7D32);
  color: #fff;
  border: none;
  border-radius: 48rpx;
  font-size: 32rpx;
  font-weight: 700;
  box-shadow: 0 8rpx 28rpx rgba(46,125,50,0.35);
}
.submit-btn[disabled] { opacity: 0.4; box-shadow: none; }
</style>
