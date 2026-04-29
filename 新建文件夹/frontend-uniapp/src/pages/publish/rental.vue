<template>
  <view class="publish-page">
    <view class="nav-header safe-area-top">
      <view class="back-btn" @click="goBack">←</view>
      <view class="nav-title">农机出租发布</view>
      <view class="nav-right"></view>
    </view>

    <view class="warning-bar">
       <text>💡 提示：发布出租前需确保机况良好，不得发布虚假信息。</text>
    </view>

    <view class="form-container">
      <!-- 基础机型信息 -->
      <view class="form-card">
        <view class="section-badge">机器信息</view>
        <view class="form-item border-bottom">
           <text class="label">机械大类</text>
           <picker mode="selector" :range="categories" range-key="name" @change="onCategoryChange">
              <view class="picker-value" :class="{'placeholder': !selectedCat}">
                 {{ selectedCat ? selectedCat.name : '请选择 (如：耕整地)' }}
              </view>
           </picker>
        </view>
        
        <view class="form-item border-bottom" v-if="selectedCat">
           <text class="label">具体类型</text>
           <picker mode="selector" :range="machines" range-key="name" @change="onMachineChange">
              <view class="picker-value" :class="{'placeholder': !selectedMach}">
                 {{ selectedMach ? selectedMach.name : '请选择' }}
              </view>
           </picker>
        </view>

        <view class="form-item">
           <text class="label">品牌与型号</text>
           <input type="text" class="text-input" placeholder="例如：东方红 120马力" v-model="brandModel" />
        </view>
      </view>

      <!-- 计费规则 -->
      <view class="form-card">
        <view class="section-badge">💰 出租计费</view>
        <view class="price-modes">
           <view class="mode-btn" :class="{'active': priceMode === 'DAY'}" @click="priceMode = 'DAY'">按天出租</view>
           <view class="mode-btn" :class="{'active': priceMode === 'MU'}" @click="priceMode = 'MU'">按亩收费</view>
           <view class="mode-btn" :class="{'active': priceMode === 'DISCUSS'}" @click="priceMode = 'DISCUSS'">价格面议</view>
        </view>
        
        <view class="form-item" v-if="priceMode !== 'DISCUSS'">
           <text class="label">期望价格</text>
           <view class="price-input-wrap">
              <text class="currency">¥</text>
              <input type="number" class="price-input" placeholder="0" v-model="priceValue" />
              <text class="unit">/ {{ priceMode === 'DAY' ? '天' : '亩' }}</text>
           </view>
        </view>
      </view>

      <!-- 照片上传 -->
      <view class="form-card">
        <view class="section-badge">📷 机器实拍图</view>
        <view class="photo-grid">
           <!-- 已选图片 -->
           <view class="photo-item" v-for="(img, idx) in images" :key="idx">
              <image :src="img" mode="aspectFill" class="photo" />
              <view class="delete-btn" @click="removeImage(idx)">×</view>
           </view>
           <!-- 上传按钮 -->
           <view class="upload-btn" @click="chooseImage" v-if="images.length < 3">
              <view class="upload-plus">+</view>
              <view class="upload-text">{{ images.length }}/3</view>
           </view>
        </view>
      </view>

      <view class="action-wrap">
         <button class="primary-btn" :class="{'disabled': !isFormValid }" @click="submitRental">发布出租信息</button>
      </view>
    </view>
  </view>
</template>

<script>
import { MACH_CATEGORIES, getMachinesByCategory } from '../../constants/taxonomy'

export default {
  data() {
    return {
      categories: MACH_CATEGORIES,
      selectedCat: null,
      machines: [],
      selectedMach: null,
      brandModel: '',
      
      priceMode: 'DAY', // DAY, MU, DISCUSS
      priceValue: '',
      
      images: []
    }
  },
  computed: {
    isFormValid() {
      if (!this.selectedMach || !this.brandModel) return false
      if (this.priceMode !== 'DISCUSS' && !this.priceValue) return false
      return true
    }
  },
  methods: {
    goBack() { uni.navigateBack() },
    onCategoryChange(e) {
       this.selectedCat = this.categories[e.detail.value]
       this.machines = getMachinesByCategory(this.selectedCat.id)
       this.selectedMach = null // reset
    },
    onMachineChange(e) {
       this.selectedMach = this.machines[e.detail.value]
    },
    chooseImage() {
       uni.chooseImage({
          count: 3 - this.images.length,
          success: (res) => {
             this.images.push(...res.tempFilePaths)
          }
       })
    },
    removeImage(idx) {
       this.images.splice(idx, 1)
    },
    submitRental() {
       if (!this.isFormValid) return
       uni.showLoading({ title: '正在提交...' })
       setTimeout(() => {
          uni.hideLoading()
          uni.showToast({ title: '已成功发布出租信息', icon: 'success' })
          setTimeout(() => { uni.navigateBack() }, 1500)
       }, 1000)
    }
  }
}
</script>

<style scoped>
.publish-page { min-height: 100vh; background: var(--page-bg); padding-bottom: 60rpx;}

.nav-header { padding: 24rpx 32rpx; display: flex; align-items: center; justify-content: space-between; background: var(--surface-deep); }
.back-btn { font-size: 40rpx; padding: 10rpx; }
.nav-title { font-size: 34rpx; font-weight: 800; }
.nav-right { width: 60rpx; }

.warning-bar { background: var(--color-warning-bg); padding: 20rpx 32rpx; font-size: 24rpx; color: var(--color-warning-text, #D97706); }

.form-container { padding: 24rpx; }
.form-card { background: var(--surface-deep); border-radius: var(--radius-xl); padding: 32rpx; margin-bottom: 24rpx; position: relative;}

.section-badge { display: inline-block; background: var(--primary-light); color: var(--primary-strong); padding: 6rpx 20rpx; border-radius: var(--radius-full); font-size: 24rpx; font-weight: 700; margin-bottom: 32rpx; }

.form-item { display: flex; align-items: center; justify-content: space-between; padding: 24rpx 0; }
.border-bottom { border-bottom: 1px solid var(--border-light); }
.label { font-size: 30rpx; color: var(--text-primary); font-weight: 700; width: 200rpx; }
.text-input { flex: 1; text-align: right; font-size: 30rpx; color: var(--text-primary); }
.picker-value { flex: 1; text-align: right; font-size: 30rpx; color: var(--text-primary); }
.placeholder { color: var(--text-soft); }

.price-modes { display: flex; gap: 16rpx; margin-bottom: 24rpx; }
.mode-btn { flex: 1; text-align: center; padding: 16rpx; background: var(--page-bg); border-radius: 12rpx; font-size: 26rpx; color: var(--text-regular); border: 2rpx solid transparent; transition: all 0.2s;}
.mode-btn.active { background: rgba(16, 185, 129, 0.1); color: var(--primary-color); border-color: var(--primary-color); font-weight: 700; }

.price-input-wrap { display: flex; align-items: baseline; justify-content: flex-end; flex: 1; }
.currency { font-size: 28rpx; font-weight: 800; color: var(--accent-gold); margin-right: 8rpx; }
.price-input { width: 140rpx; font-size: 44rpx; font-weight: 800; color: var(--accent-gold); text-align: right; }
.unit { font-size: 26rpx; color: var(--text-muted); margin-left: 8rpx; }

.photo-grid { display: flex; flex-wrap: wrap; gap: 20rpx; }
.photo-item { width: 180rpx; height: 180rpx; position: relative; border-radius: 12rpx; overflow: hidden; }
.photo { width: 100%; height: 100%; object-fit: cover; }
.delete-btn { position: absolute; top: 8rpx; right: 8rpx; width: 40rpx; height: 40rpx; background: rgba(0,0,0,0.5); color: white; border-radius: 50%; display: flex; align-items: center; justify-content: center; font-size: 24rpx; }

.upload-btn { width: 180rpx; height: 180rpx; background: var(--secondary-color); border: 2rpx dashed var(--secondary-strong); border-radius: var(--radius-md); display: flex; flex-direction: column; align-items: center; justify-content: center; }
.upload-btn:active { background: var(--secondary-strong); }
.upload-plus { font-size: 60rpx; color: var(--text-soft); line-height: 1; margin-bottom: 8rpx; font-weight: 300; }
.upload-text { font-size: 22rpx; color: var(--text-soft); }

.action-wrap { margin-top: 60rpx; }
.primary-btn { background: var(--primary-strong); color: white; border-radius: var(--radius-full); font-size: 34rpx; font-weight: 800; height: 96rpx; display: flex; align-items: center; justify-content: center; box-shadow: var(--shadow-colored); }
.primary-btn.disabled { opacity: 0.5; box-shadow: none; }
</style>
