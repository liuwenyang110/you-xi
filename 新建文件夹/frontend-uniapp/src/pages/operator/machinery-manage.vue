<template>
  <view class="machinery-manage">
    <view class="page-header">
      <text class="page-title">🔧 我的农机</text>
      <text class="page-sub">登记您的农机，让农户找到您</text>
    </view>

    <!-- 已登记列表 -->
    <view v-if="myList.length" class="my-list">
      <view class="list-section-title">已登记 {{ myList.length }} 台</view>
      <view
        v-for="m in myList"
        :key="m.id"
        class="machine-card"
      >
        <view class="machine-card-header">
          <view class="machine-type-badge">
            <text>{{ getMachineTypeName(m.machineTypeId) }}</text>
          </view>
          <view class="machine-actions">
            <text class="action-btn danger" @click="deactivate(m.id)">下架</text>
          </view>
        </view>
        <view class="machine-detail">
          <text class="machine-brand-model">{{ m.brand }} {{ m.modelNo }}</text>
          <view class="machine-tags" v-if="m.isCrossRegion">
            <view class="tag-cross"><text>🗺 可跨区作业</text></view>
          </view>
          <text class="machine-desc" v-if="m.crossRangeDesc">{{ m.crossRangeDesc }}</text>
          <text class="machine-avail" v-if="m.availDesc">可用时间：{{ m.availDesc }}</text>
        </view>
      </view>
    </view>

    <!-- 登记新农机 -->
    <view class="add-section">
      <view class="add-section-title">➕ 登记新农机</view>
      <view class="form-wrap">
        <!-- 农机大类 -->
        <view class="form-item">
          <text class="form-label">农机类型 <text class="required">*</text></text>
          <picker mode="selector" :range="categories" range-key="name"
                  :value="selectedCatIndex" @change="onCatChange">
            <view class="picker-field">
              <text class="picker-text">{{ selectedCatName || '请选择农机大类' }}</text>
              <text class="picker-arrow">›</text>
            </view>
          </picker>
        </view>

        <!-- 农机小类 -->
        <view class="form-item" v-if="typeOptions.length">
          <text class="form-label">具体机型 <text class="required">*</text></text>
          <picker mode="selector" :range="typeOptions" range-key="name"
                  :value="selectedTypeIndex" @change="onTypeChange">
            <view class="picker-field">
              <text class="picker-text">{{ selectedTypeName || '请选择机型' }}</text>
              <text class="picker-arrow">›</text>
            </view>
          </picker>
          <!-- 机型说明 -->
          <view class="type-tip" v-if="selectedTypeDesc">
            <text class="type-tip-icon">ℹ️</text>
            <text class="type-tip-text">{{ selectedTypeDesc }}</text>
          </view>
        </view>

        <!-- 品牌 -->
        <view class="form-item">
          <text class="form-label">品牌（如知道可填）</text>
          <view class="brand-chips">
            <view
              v-for="b in commonBrands"
              :key="b"
              class="brand-chip"
              :class="{ selected: form.brand === b }"
              @click="form.brand = b"
            >{{ b }}</view>
          </view>
          <input class="form-input" v-model="form.brand" placeholder="或手动输入品牌名" />
        </view>

        <!-- 型号 -->
        <view class="form-item">
          <text class="form-label">型号（选填）</text>
          <input class="form-input" v-model="form.modelNo" placeholder="如 GE80、锐龙4LZ-6.0" />
        </view>

        <!-- 是否跨区 -->
        <view class="form-item">
          <text class="form-label">是否提供跨区作业</text>
          <view class="radio-row">
            <view
              class="radio-item"
              :class="{ selected: form.isCrossRegion === 0 }"
              @click="form.isCrossRegion = 0"
            >仅本地作业</view>
            <view
              class="radio-item"
              :class="{ selected: form.isCrossRegion === 1 }"
              @click="form.isCrossRegion = 1"
            >可跨区/跨省 🗺</view>
          </view>
        </view>

        <!-- 跨区说明（跨区时显示） -->
        <view class="form-item" v-if="form.isCrossRegion === 1">
          <text class="form-label">跨区作业说明</text>
          <textarea
            class="form-textarea"
            v-model="form.crossRangeDesc"
            placeholder="如：可跨省，5月在河南、6月在山东沿线接活"
            maxlength="100"
            auto-height
          />
        </view>

        <!-- 可作业时间 -->
        <view class="form-item">
          <text class="form-label">可接活时间说明</text>
          <input class="form-input" v-model="form.availDesc" placeholder="如：5-6月收麦，9-10月收玉米" />
        </view>

        <!-- 其他说明 -->
        <view class="form-item">
          <text class="form-label">其他说明</text>
          <textarea
            class="form-textarea"
            v-model="form.description"
            placeholder="如：带秸秆打捆、夜间也可作业等"
            maxlength="100"
            auto-height
          />
        </view>

        <view class="submit-wrap">
          <button
            class="submit-btn"
            :disabled="!canSubmit || submitting"
            :loading="submitting"
            @click="submit"
          >登记这台农机</button>
        </view>
      </view>
    </view>
  </view>
</template>

<script>
import { registerMachinery, myMachinery, deactivateMachinery } from '@/api/v3/machinery'
import { useV3ZoneStore } from '@/stores/v3/zoneStore'

export default {
  name: 'MachineryManage',
  data() {
    return {
      myList: [],
      submitting: false,
      selectedCatIndex: 0,
      selectedTypeIndex: 0,
      form: {
        machineTypeId: null,
        brand: '',
        modelNo: '',
        isCrossRegion: 0,
        crossRangeDesc: '',
        availDesc: '',
        description: '',
      },
      commonBrands: ['雷沃谷神', '沃得', '久保田', '中联重科', '东方红', '极飞', '大疆'],
    }
  },
  computed: {
    categories() { return useV3ZoneStore().dicts.machineCategories || [] },
    selectedCatId() { return this.categories[this.selectedCatIndex]?.id },
    selectedCatName() { return this.categories[this.selectedCatIndex]?.name || '' },
    typeOptions() {
      if (!this.selectedCatId) return []
      return useV3ZoneStore().dicts.machineTypes.filter(t => t.categoryId === this.selectedCatId)
    },
    selectedTypeName() { return this.typeOptions[this.selectedTypeIndex]?.name || '' },
    selectedTypeDesc() { return this.typeOptions[this.selectedTypeIndex]?.description || '' },
    canSubmit() { return !!this.form.machineTypeId },
  },
  async onLoad() {
    const store = useV3ZoneStore()
    if (!store.dictsLoaded) await store.loadDicts()
    await this.loadMyList()
  },
  methods: {
    onCatChange(e) {
      this.selectedCatIndex = e.detail.value
      this.selectedTypeIndex = 0
      this.form.machineTypeId = null
    },
    onTypeChange(e) {
      this.selectedTypeIndex = e.detail.value
      const t = this.typeOptions[e.detail.value]
      this.form.machineTypeId = t?.id || null
    },
    getMachineTypeName(id) {
      return useV3ZoneStore().machineTypeName(id)
    },
    async loadMyList() {
      try {
        const res = await myMachinery()
        if (res.code === 0) this.myList = res.data
      } catch (e) {}
    },
    async deactivate(id) {
      uni.showModal({
        title: '确认下架',
        content: '下架后农户将无法看到此农机，确定吗？',
        success: async (res) => {
          if (res.confirm) {
            await deactivateMachinery(id)
            uni.showToast({ title: '已下架', icon: 'success' })
            await this.loadMyList()
          }
        }
      })
    },
    async submit() {
      if (!this.canSubmit) return
      this.submitting = true
      try {
        const payload = { ...this.form }
        if (!payload.crossRangeDesc) delete payload.crossRangeDesc
        if (!payload.availDesc) delete payload.availDesc
        if (!payload.description) delete payload.description
        const res = await registerMachinery(payload)
        if (res.code === 0) {
          uni.showToast({ title: '农机已登记！', icon: 'success' })
          // 重置表单
          this.form = { machineTypeId: null, brand: '', modelNo: '', isCrossRegion: 0, crossRangeDesc: '', availDesc: '', description: '' }
          await this.loadMyList()
        } else {
          uni.showToast({ title: res.message || '登记失败', icon: 'none' })
        }
      } catch (e) {
        uni.showToast({ title: '网络异常', icon: 'none' })
      } finally {
        this.submitting = false
      }
    }
  }
}
</script>

<style scoped>
.machinery-manage { background: var(--page-bg); min-height: 100vh; min-height: 100dvh; padding-bottom: 40rpx; }
.page-header { background: var(--gradient-primary); padding: 80rpx 40rpx 40rpx; }
.page-title { display: block; font-size: 40rpx; font-weight: var(--font-bold); color: #fff; }
.page-sub { display: block; font-size: var(--text-sm); color: rgba(255,255,255,0.8); margin-top: 8rpx; }

.my-list { padding: 20rpx 24rpx 0; }
.list-section-title { font-size: var(--text-base); color: var(--text-muted); margin-bottom: 12rpx; font-weight: var(--font-semibold); }
.machine-card {
  background: var(--surface-deep);
  border-radius: var(--radius-lg);
  padding: 24rpx;
  margin-bottom: 16rpx;
  box-shadow: var(--shadow-sm);
}
.machine-card-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 12rpx; }
.machine-type-badge {
  background: var(--color-success-bg);
  border-radius: var(--radius-full);
  padding: 6rpx 20rpx;
  font-size: var(--text-sm);
  color: var(--primary-strong);
  font-weight: var(--font-bold);
}
.action-btn { font-size: var(--text-sm); padding: 8rpx 20rpx; border-radius: var(--radius-xl); }
.action-btn.danger { color: var(--color-error); background: rgba(239,68,68,0.08); }
.machine-brand-model { display: block; font-size: var(--text-base); color: var(--text-regular); margin-bottom: 8rpx; }
.machine-tags { margin: 8rpx 0; }
.tag-cross {
  display: inline-block;
  background: var(--color-info-bg);
  border-radius: var(--radius-xl);
  padding: 4rpx 16rpx;
  font-size: var(--text-xs);
  color: var(--color-info);
}
.machine-desc { display: block; font-size: var(--text-sm); color: var(--text-muted); margin-top: 6rpx; }
.machine-avail { display: block; font-size: var(--text-xs); color: var(--text-soft); margin-top: 4rpx; }

.add-section { padding: 20rpx 24rpx; }
.add-section-title { font-size: var(--text-base); font-weight: var(--font-bold); color: var(--text-primary); margin-bottom: 16rpx; }
.form-wrap {
  background: var(--surface-deep);
  border-radius: var(--radius-xl);
  box-shadow: var(--shadow-base);
  overflow: hidden;
}
.form-item { padding: 28rpx 32rpx; border-bottom: 1rpx solid var(--border-light); }
.form-item:last-child { border-bottom: none; }
.form-label { display: block; font-size: var(--text-base); font-weight: var(--font-semibold); color: var(--text-regular); margin-bottom: 16rpx; }
.required { color: var(--color-error); }

.picker-field { display: flex; align-items: center; justify-content: space-between; border: 1rpx solid var(--border-regular); border-radius: var(--radius-md); padding: 20rpx 24rpx; background: var(--primary-faint); }
.picker-text { font-size: var(--text-base); color: var(--text-regular); }
.picker-arrow { font-size: var(--text-lg); color: var(--text-soft); }

.type-tip { display: flex; align-items: flex-start; background: var(--color-success-bg); border-radius: var(--radius-md); padding: 16rpx; margin-top: 12rpx; }
.type-tip-icon { font-size: var(--text-sm); margin-right: 8rpx; flex-shrink: 0; }
.type-tip-text { font-size: var(--text-xs); color: var(--primary-strong); line-height: 1.6; }

.brand-chips { display: flex; flex-wrap: wrap; gap: 12rpx; margin-bottom: 16rpx; }
.brand-chip { padding: 10rpx 22rpx; border: 2rpx solid var(--border-regular); border-radius: var(--radius-full); font-size: var(--text-sm); color: var(--text-muted); background: var(--secondary-color); }
.brand-chip.selected { border-color: var(--primary-color); background: var(--color-success-bg); color: var(--primary-strong); font-weight: var(--font-bold); }

.form-input { display: block; width: 100%; border: 1rpx solid var(--border-regular); border-radius: var(--radius-md); padding: 18rpx 20rpx; font-size: var(--text-base); background: var(--primary-faint); box-sizing: border-box; }
.form-textarea { display: block; width: 100%; border: 1rpx solid var(--border-regular); border-radius: var(--radius-md); padding: 18rpx 20rpx; font-size: var(--text-base); background: var(--primary-faint); min-height: 90rpx; box-sizing: border-box; line-height: 1.6; }

.radio-row { display: flex; gap: 20rpx; }
.radio-item { flex: 1; text-align: center; padding: 18rpx; border: 2rpx solid var(--border-regular); border-radius: var(--radius-lg); font-size: var(--text-base); color: var(--text-muted); }
.radio-item.selected { border-color: var(--primary-color); background: var(--color-success-bg); color: var(--primary-strong); font-weight: var(--font-bold); }

.submit-wrap { padding: 24rpx 32rpx 28rpx; }
.submit-btn { width: 100%; height: 96rpx; background: var(--gradient-primary); color: #fff; border: none; border-radius: var(--radius-full); font-size: var(--text-lg); font-weight: var(--font-bold); box-shadow: var(--shadow-colored); }
.submit-btn[disabled] { opacity: 0.4; box-shadow: none; }
</style>
