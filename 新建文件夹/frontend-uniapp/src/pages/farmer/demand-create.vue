<template>
  <view class="page farmer-page">
    <!-- 背景装饰 -->
    <view class="bg-shape shape-1"></view>
    <view class="bg-shape shape-2"></view>

    <!-- 顶栏标题 -->
    <view class="welcome-bar flex justify-between items-end scene-enter">
      <view class="welcome-left">
        <view class="welcome-title text-2xl font-extrabold">发布作业需求</view>
        <view class="welcome-sub text-muted text-sm mt-1">信息越全，越容易吸引附近的机手主动联系您</view>
      </view>
    </view>

    <!-- 核心 Hero 区：发布准备度与信誉分析 -->
    <view class="card card-hero scene-enter delay-1">
      <view class="hero-row flex gap-3 items-center">
        <view class="hero-copy flex-1">
          <view class="section-caption text-xs font-bold text-primary tracking-widest uppercase"
            >北斗大数据·发布准备度</view
          >
          <view class="hero-title text-xl font-bold mt-1">信息完善度 {{ completionPercent }}%</view>
          <view class="hero-subtitle text-sm text-muted mt-2 leading-relaxed">
            信息填得好，机手联系早。完善的信息能让机手更准确评估作业难度。
          </view>
        </view>
        <view class="hero-visual-box flex-shrink-0">
          <view class="hero-orbit"></view>
          <view class="hero-core flex items-center justify-center font-bold text-white text-xs"
            >{{ completionPercent }}%</view
          >
        </view>
      </view>

      <view class="progress-container mt-4">
        <view class="progress-bar-bg">
          <view class="progress-fill" :style="{ width: `${completionPercent}%` }"></view>
        </view>
      </view>

      <view class="hero-badges flex flex-wrap gap-2 mt-4">
        <view class="status-tag status-active">北斗找机</view>
        <view class="status-tag status-active">直接联系</view>
        <view class="status-tag status-active" v-if="completionPercent === 100"
          >高优先权已激活</view
        >
      </view>
    </view>


    <!-- 作业信息卡片 -->
    <view class="card scene-enter delay-2">
      <view class="section-header mb-4">
        <view class="section-title font-bold text-lg">作业核心信息</view>
        <view class="text-xs text-muted mt-1">描述您的作业类型与规模</view>
      </view>

      <view class="form-grid grid grid-cols-2 gap-3 mb-4">
        <view class="form-item">
          <view class="form-label text-xs mb-2">服务大类</view>
          <picker :range="categories" range-key="label" @change="onCategoryChange">
            <view class="input-modern flex items-center justify-between">
              <text>{{ selectedCategory.label }}</text>
              <text class="arrow-down text-xs">▼</text>
            </view>
          </picker>
        </view>
        <view class="form-item">
          <view class="form-label text-xs mb-2">服务子类</view>
          <picker :range="subcategories" range-key="label" @change="onSubChange">
            <view class="input-modern flex items-center justify-between">
              <text>{{ selectedSub.label }}</text>
              <text class="arrow-down text-xs">▼</text>
            </view>
          </picker>
        </view>
      </view>

      <view class="form-grid grid grid-cols-2 gap-3 mb-4">
        <view class="form-item">
          <view class="form-label text-xs mb-2">具体作物</view>
          <input v-model="form.cropCode" class="input-modern" placeholder="如：冬小麦/晚稻/玉米" />
        </view>
        <view class="form-item">
          <view class="form-label text-xs mb-2">作业面积 (亩)</view>
          <input v-model="form.areaMu" class="input-modern" type="digit" placeholder="如：12.5" />
        </view>
      </view>

      <view class="form-item">
        <view class="form-label text-xs mb-2">补充说明 (选填)</view>
        <textarea
          v-model="form.voiceText"
          class="input-modern h-24 pt-2"
          placeholder="地块特点、道路情况等..."
        />
      </view>
    </view>

    <!-- 位置卡片 -->
    <view class="card scene-enter delay-3">
      <view class="section-header mb-4">
        <view class="section-title font-bold text-lg">作业位置</view>
        <view class="text-xs text-muted mt-1">精准定位可大幅缩短匹配路径</view>
      </view>

      <view class="form-item mb-3">
        <view class="form-label text-xs mb-2">所在村名或地点</view>
        <view class="flex gap-2">
          <input
            v-model="form.villageName"
            class="input-modern flex-1"
            placeholder="输入地点名称"
            @input="onPlaceInput"
          />
          <button class="btn btn-secondary px-4 py-0 text-sm" @click="searchPlace">建议</button>
        </view>
      </view>

      <view class="preset-row flex flex-wrap gap-2 mb-4">
        <view
          v-for="(item, index) in locationPresets"
          :key="item.label"
          class="preset-chip text-xs px-3 py-1 rounded-full border border-light"
          :class="{ 'preset-active bg-primary border-primary text-white': locationIndex === index }"
          @click="applyLocationPreset(index)"
        >
          {{ item.label }}
        </view>
      </view>

      <view
        v-if="placeSuggestions.length > 0"
        class="suggestion-panel card card-glass mb-4 p-3 overflow-hidden"
      >
        <view class="text-xs font-bold text-primary mb-2">选择建议地点</view>
        <view
          v-for="item in placeSuggestions"
          :key="`${item.name}-${item.lng}-${item.lat}`"
          class="suggestion-item py-2 border-b border-light last:border-b-0"
          @click="applySuggestion(item)"
        >
          <view class="suggestion-name text-sm font-bold">{{ item.name }}</view>
          <view class="suggestion-address text-xs text-muted mt-1">{{ item.address }}</view>
        </view>
      </view>

      <view class="coord-display tactical-panel mb-2">
        <view class="tp-header">
          <view class="tp-status">
            <view class="tp-dot" :class="{ active: form.lat }"></view>
            <text class="tp-txt">北斗三号高精度定位{{ form.lat ? '已锁定' : '待激活' }}</text>
          </view>
          <text class="tp-ver">VER 3.0</text>
        </view>
        <view class="tp-body">
          <view class="tp-coord">
            <text class="tp-label">LON</text>
            <text class="tp-val">{{ form.lng ? form.lng.toFixed(6) : '---.------' }}</text>
          </view>
          <view class="tp-coord">
            <text class="tp-label">LAT</text>
            <text class="tp-val">{{ form.lat ? form.lat.toFixed(6) : '---.------' }}</text>
          </view>
        </view>
        <view class="tp-footer" v-if="form.lat">
          <text class="tp-hint">经由北斗基准站差分纠偏，误差 &lt; 0.5m</text>
        </view>
      </view>

      <!-- 北斗扫频动效 (覆盖层) -->
      <view class="scanning-overlay" v-if="isScanning">
        <view class="scan-grid"></view>
        <view class="scan-line"></view>
        <view class="scan-content">
          <view class="scan-radar">
            <view class="radar-pulse"></view>
            <view class="radar-cross"></view>
          </view>
          <text class="scan-text">正在确认作业模型...</text>
          <text class="scan-sub">建立位置坐标与作物类型的映射</text>
        </view>
      </view>
    </view>

    <!-- 特殊地况与真实障碍排雷区 -->
    <view class="card scene-enter delay-3">
      <view class="section-header mb-4">
        <view class="section-title font-bold text-lg text-red-600 flex items-center">
          <text class="mr-1">⚠️</text> 恶劣地况与雷区预警
        </view>
        <view class="text-xs text-muted mt-1">如隐瞒坑害机手会降低您的信誉分</view>
      </view>

      <view class="preset-row flex flex-wrap gap-2 mb-4">
        <view
          v-for="tag in availableTags"
          :key="tag.value"
          class="hazard-chip text-xs px-3 py-1 rounded-md border border-light"
          :class="{ 'hazard-active': form.plotTags.includes(tag.value) }"
          @click="togglePlotTag(tag.value)"
        >
          {{ tag.label }}
        </view>
      </view>

      <view
        class="upload-area bg-page-bg rounded-xl p-4 flex flex-col items-center justify-center border-dashed border-2 border-gray-200 mt-2"
        @click="uploadImage"
      >
        <text class="text-2xl mb-1">📸</text>
        <text class="text-xs text-muted font-bold text-center"
          >传几张现场的图<br />(让机手心里有底，避免白跑)</text
        >
        <view class="flex gap-2 mt-2" v-if="form.images.length > 0">
          <view
            v-for="(img, i) in form.images"
            :key="i"
            class="w-10 h-10 bg-gray-300 rounded overflow-hidden"
          >
            <image :src="img" class="w-full h-full" mode="aspectFill"></image>
          </view>
        </view>
      </view>
    </view>

    <!-- 时间与操作 -->
    <view class="card scene-enter delay-4">
      <view class="section-header mb-4">
        <view class="section-title font-bold text-lg">作业时间</view>
        <view class="text-xs text-muted mt-1">您期望农机主何时到达</view>
      </view>
      <picker :range="scheduleTypes" range-key="label" @change="onScheduleChange">
        <view class="input-modern flex justify-between items-center">
          <text>{{ currentScheduleLabel }}</text>
          <text class="arrow-down text-xs">▼</text>
        </view>
      </picker>
    </view>

    <!-- 吸底操作栏 -->
    <view class="fixed-bottom p-4 scene-enter delay-4">
      <view class="action-card card card-glass flex justify-between items-center p-4">
        <view class="flex flex-col">
          <text class="text-sm font-bold text-primary">{{
            isReadyToSubmit ? '配置已就绪' : '补充信息'
          }}</text>
          <text class="text-xs text-muted mt-1">{{ readinessHint }}</text>
        </view>
        <button
          class="btn btn-primary px-8"
          :class="{ 'opacity-50': !isReadyToSubmit || submitting }"
          @click="submitDemand"
        >
          {{ submitting ? '提交中' : '发布需求' }}
        </button>
      </view>


    </view>
  </view>
</template>

<script>
import { farmerApi } from '../../api/farmer'
import { mapApi } from '../../api/map'
import { ensurePageAccess } from '../../utils/pageAuth'
import { SERVICE_CATEGORIES, SERVICE_ITEMS, getServiceItemsByCategory } from '../../constants/taxonomy'

// 将设计文档的新分类体系转换为 picker 格式
const PICKER_CATEGORIES = SERVICE_CATEGORIES.map(c => ({
  id: c.id, label: `${c.emoji} ${c.name}`
}))

// 按大类build子分类映射表
const PICKER_CATEGORY_MAP = {}
SERVICE_CATEGORIES.forEach(c => {
  PICKER_CATEGORY_MAP[c.id] = getServiceItemsByCategory(c.id).map(item => ({
    id: item.id, label: item.name, code: item.code, desc: item.desc
  }))
})

export default {
  onShow() {
    uni.setNavigationBarTitle({ title: '' })
    ensurePageAccess('FARMER')
  },
  data() {
    return {
      submitting: false,
      locating: false,
      searching: false,
      geoTip: '',
      placeSuggestions: [],

      // Dynamic mapping for categories -> subcategories（从设计文档生成）
      categoryMap: PICKER_CATEGORY_MAP,
      categories: PICKER_CATEGORIES,
      subcategories: PICKER_CATEGORY_MAP[PICKER_CATEGORIES[0].id] || [],

      hazardDictionaries: {
        // key = SERVICE_CATEGORIES.id
        1: [ // 耕地整地
          { label: '🪨 致命: 有巨大暗石', value: '巨型暗石极易打碎刀片' },
          { label: '⚠️ 极限陡坡梯田', value: '坡度畸高容易侧翻发生事故' },
          { label: '🚫 土壤极度板结', value: '常年未耕或粘土巨难打碎' },
          { label: '🔺 畸形散块田', value: '形状奇怪倒头难度极大' }
        ],
        2: [ // 播种插秧
          { label: '⚠️ 泥泞易陷车', value: '有大面积积水泥泞' },
          { label: '🚫 限宽大车难进', value: '村口限宽桥梁大车难进' },
          { label: '🔺 地块未整好', value: '前茬秸秆残留量大' }
        ],
        3: [ // 施肥打药
          { label: '⚡ 致命: 空中有高压线', value: '高压线网危险作业' },
          { label: '🐝 致命: 邻近养蜂场', value: '紧邻蜂场严重纠纷隐患' },
          { label: '🚰 现场无水源', value: '无法就近取水配药' },
          { label: '💨 风力环境多变', value: '峡谷风大易偏洒失效' }
        ],
        4: [ // 灌溉排水
          { label: '⚠️ 无电源', value: '现场无电源需柴油泵' },
          { label: '💧 深泥坑无法着力', value: '设备难以安置' }
        ],
        5: [ // 收割收获
          { label: '⚠️ 泥泞易陷车', value: '有大面积积水泥泞' },
          { label: '⚠️ 严重倒伏', value: '作物严重倾斜倒伏' },
          { label: '🚫 限宽大车难进', value: '村口限宽桥梁大车难进' },
          { label: '🔺 畸形散块田', value: '形状奇怪倒头难度极大' }
        ],
        7: [ // 果园作业
          { label: '⚠️ 极限陡坡', value: '坡度畸高容易侧翻' },
          { label: '🚫 行距过窄', value: '树行间距不足机械通行' },
          { label: '🐝 邻近养蜂场', value: '紧邻蜂场打药纠纷隐患' }
        ],
      },

      scheduleTypes: [
        { label: '今天', value: 'today' },
        { label: '明天', value: 'tomorrow' },
        { label: '三天内', value: 'three_days' },
        { label: '加急(1小时内)', value: 'urgent' }
      ],
      locationPresets: [],
      categoryIndex: 0,
      subIndex: 0,
      scheduleIndex: 0,
      locationIndex: 0,
      isScanning: false,
      selectedCategory: PICKER_CATEGORIES[0],
      selectedSub: (PICKER_CATEGORY_MAP[PICKER_CATEGORIES[0].id] || [])[0] || {},
      form: {
        serviceCategoryId: PICKER_CATEGORIES[0].id,
        serviceSubcategoryId: ((PICKER_CATEGORY_MAP[PICKER_CATEGORIES[0].id] || [])[0] || {}).id,
        cropCode: '冬小麦',
        areaMu: '12.5',
        villageName: '东河村',
        scheduleType: 'today',
        lat: 30.27415,
        lng: 120.15515,
        voiceText: '',
        remark: '',
        plotTags: [],
        images: []
      }
    }
  },
  computed: {
    availableTags() {
      return this.hazardDictionaries[this.form.serviceCategoryId] || this.hazardDictionaries[1] || []
    },
    currentScheduleLabel() {
      return this.scheduleTypes[this.scheduleIndex].label
    },
    locationReadyText() {
      return this.form.lat && this.form.lng ? '位置已确定' : '等待定位'
    },
    completionPercent() {
      const area = Number(this.form.areaMu)
      const checks = [
        !!this.form.serviceCategoryId,
        !!this.form.serviceSubcategoryId,
        !!String(this.form.cropCode || '').trim(),
        Number.isFinite(area) && area > 0,
        !!String(this.form.villageName || '').trim(),
        Number.isFinite(Number(this.form.lat)),
        Number.isFinite(Number(this.form.lng)),
        !!this.form.scheduleType
      ]
      const completed = checks.filter(Boolean).length
      return Math.round((completed / checks.length) * 100)
    },
    isReadyToSubmit() {
      return this.completionPercent === 100
    },
    readinessHint() {
      if (this.submitting) return '正在进入找机列表'
      if (this.isReadyToSubmit) return '发布后可查看匹配结果'
      return `还差 ${100 - this.completionPercent}% 信息`
    }
  },
  methods: {
    normalizeGeoKeyword(value) {
      const raw = String(value || '')
        .trim()
        .toLowerCase()
      const aliasMap = { 东河村: 'donghe village', 虹桥: 'hongqiao' }
      return aliasMap[value] || raw
    },
    validateForm() {
      if (!this.form.serviceCategoryId) return '请选择服务类别'
      if (!this.form.cropCode) return '请输入作物'
      const area = Number(this.form.areaMu)
      if (!area || area <= 0) return '面积格式错误'
      if (!this.form.lat) return '请完成地点定位'
      return ''
    },
    onPlaceInput() {
      this.placeSuggestions = []
      this.geoTip = ''
    },
    onCategoryChange(event) {
      this.categoryIndex = Number(event.detail.value)
      this.selectedCategory = this.categories[this.categoryIndex]
      this.form.serviceCategoryId = this.selectedCategory.id

      this.form.plotTags = [] // Clear incompatible hazards when switching heavily diverging modules

      // Cascade update subcategories
      this.subcategories = this.categoryMap[this.selectedCategory.id] || []
      this.subIndex = 0
      this.selectedSub = this.subcategories[0] || {}
      this.form.serviceSubcategoryId = this.selectedSub.id
    },
    onSubChange(event) {
      this.subIndex = Number(event.detail.value)
      this.selectedSub = this.subcategories[this.subIndex]
      this.form.serviceSubcategoryId = this.selectedSub.id
    },
    onScheduleChange(event) {
      this.scheduleIndex = Number(event.detail.value)
      this.form.scheduleType = this.scheduleTypes[this.scheduleIndex].value
    },
    applyLocationPreset(index) {
      this.locationIndex = index
      const item = this.locationPresets[index]
      this.form.villageName = item.villageName
      this.form.lat = item.lat
      this.form.lng = item.lng
      this.placeSuggestions = []
    },
    applySuggestion(item) {
      this.isScanning = true
      setTimeout(() => {
        this.form.villageName = item.name || this.form.villageName
        this.form.lat = Number(item.lat)
        this.form.lng = Number(item.lng)
        this.placeSuggestions = []
        this.isScanning = false
        uni.showToast({ title: '北斗定位锁定成功', icon: 'success' })
      }, 1500)
    },
    async searchPlace() {
      if (this.searching) return
      if (!this.form.villageName) return
      this.searching = true
      try {
        const result = await mapApi.search(this.normalizeGeoKeyword(this.form.villageName))
        this.placeSuggestions = result?.items || []
      } finally {
        this.searching = false
      }
    },
    togglePlotTag(value) {
      const idx = this.form.plotTags.indexOf(value)
      if (idx > -1) {
        this.form.plotTags.splice(idx, 1)
      } else {
        this.form.plotTags.push(value)
      }
    },
    uploadImage() {
      uni.chooseImage({
        count: 3,
        success: res => {
          this.form.images = [...this.form.images, ...res.tempFilePaths]
        }
      })
    },
    async submitDemand() {
      if (this.submitting || !this.isReadyToSubmit) return
      const validationMessage = this.validateForm()
      if (validationMessage) return uni.showToast({ title: validationMessage, icon: 'none' })
      this.submitting = true
      try {
        const demandId = await farmerApi.createDemand({
          ...this.form,
          cropCode: String(this.form.cropCode || '').trim(),
          areaMu: Number(this.form.areaMu),
          lat: Number(this.form.lat),
          lng: Number(this.form.lng),
          requirementJson: {
            terrainTags: ['中原大平原', '散户零碎田'],
            plotTags: this.form.plotTags,
            images: this.form.images
          }
        })
        uni.navigateTo({ url: `/pages/farmer/matching?demandId=${demandId}` })
      } catch (error) {
        uni.showToast({ title: '发布失败' || error?.message, icon: 'none' })
      } finally {
        this.submitting = false
      }
    }
  }
}
</script>

<style>
.farmer-page {
  min-height: 100vh;
  position: relative;
  overflow-x: hidden;
  padding: 32rpx 28rpx 220rpx;
}

/* 战术坐标面板 */
.tactical-panel {
  background: #111827;
  border-radius: 12rpx;
  padding: 20rpx;
  border: 1px solid #1f2937;
  box-shadow: inset 0 0 40rpx rgba(0, 0, 0, 0.5);
  margin-bottom: 12rpx;
}
.tp-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16rpx;
}
.tp-status {
  display: flex;
  align-items: center;
  gap: 8rpx;
}
.tp-dot {
  width: 10rpx;
  height: 10rpx;
  background: #6b7280;
  border-radius: 50%;
}
.tp-dot.active {
  background: var(--primary-color);
  box-shadow: 0 0 10rpx var(--primary-color);
  animation: blink 1s infinite;
}
@keyframes blink {
  0%,
  100% {
    opacity: 1;
  }
  50% {
    opacity: 0.3;
  }
}
.tp-txt {
  font-size: 18rpx;
  color: #9ca3af;
  font-weight: 800;
  text-transform: uppercase;
  letter-spacing: 1rpx;
}
.tp-ver {
  font-size: 16rpx;
  color: #4b5563;
  font-family: monospace;
}
.tp-body {
  display: flex;
  gap: 40rpx;
}
.tp-coord {
  display: flex;
  flex-direction: column;
  gap: 4rpx;
}
.tp-label {
  font-size: 16rpx;
  color: #6b7280;
  font-family: monospace;
}
.tp-val {
  font-size: 28rpx;
  color: #10b981;
  font-weight: 900;
  font-family: 'DIN Alternate', monospace;
}
.tp-footer {
  margin-top: 12rpx;
  border-top: 1px dashed #374151;
  padding-top: 8rpx;
}
.tp-hint {
  font-size: 16rpx;
  color: #6b7280;
}

/* 北斗扫频遮罩 */
.scanning-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(6, 78, 59, 0.95);
  z-index: 9999;
  display: flex;
  align-items: center;
  justify-content: center;
  backdrop-filter: blur(8rpx);
}
.scan-grid {
  position: absolute;
  width: 200%;
  height: 200%;
  background-image:
    linear-gradient(rgba(16, 185, 129, 0.1) 1px, transparent 1px),
    linear-gradient(90deg, rgba(16, 185, 129, 0.1) 1px, transparent 1px);
  background-size: 40rpx 40rpx;
  animation: move-grid 10s linear infinite;
}
@keyframes move-grid {
  0% {
    transform: translate(-25%, -25%);
  }
  100% {
    transform: translate(0, 0);
  }
}

.scan-line {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 100rpx;
  background: linear-gradient(180deg, rgba(16, 185, 129, 0.2) 0%, transparent 100%);
  border-top: 2rpx solid #10b981;
  animation: scan-vertical 2.5s ease-in-out infinite;
}
@keyframes scan-vertical {
  0% {
    top: -10%;
  }
  100% {
    top: 110%;
  }
}

.scan-content {
  position: relative;
  z-index: 10;
  display: flex;
  flex-direction: column;
  align-items: center;
}
.scan-radar {
  width: 160rpx;
  height: 160rpx;
  position: relative;
  margin-bottom: 40rpx;
}
.radar-pulse {
  width: 160rpx;
  height: 160rpx;
  border: 2rpx solid #10b981;
  border-radius: 50%;
  animation: radar-pulse 2s linear infinite;
}
@keyframes radar-pulse {
  0% {
    transform: scale(0.5);
    opacity: 1;
  }
  100% {
    transform: scale(1.5);
    opacity: 0;
  }
}
.radar-cross {
  position: absolute;
  top: 50%;
  left: 50%;
  width: 40rpx;
  height: 40rpx;
  transform: translate(-50%, -50%);
}
.radar-cross::before,
.radar-cross::after {
  content: '';
  position: absolute;
  background: #10b981;
}
.radar-cross::before {
  width: 40rpx;
  height: 2rpx;
  top: 20rpx;
  left: 0;
}
.radar-cross::after {
  height: 40rpx;
  width: 2rpx;
  left: 20rpx;
  top: 0;
}

.scan-text {
  color: #fef08a;
  font-size: 34rpx;
  font-weight: 900;
  margin-bottom: 12rpx;
  text-shadow: 0 0 20rpx rgba(254, 240, 138, 0.5);
}
.scan-sub {
  color: #d1fae5;
  font-size: 22rpx;
  opacity: 0.8;
}

/* ── 品牌质感背景 ── */
.bg-shape {
  position: absolute;
  border-radius: 50%;
  filter: blur(100px);
  z-index: 0;
  opacity: 0.35;
}
.shape-1 {
  width: 450rpx;
  height: 450rpx;
  background: rgba(16, 185, 129, 0.15);
  top: -100rpx;
  left: -100rpx;
  animation: float 12s ease-in-out infinite alternate;
}
.shape-2 {
  width: 400rpx;
  height: 400rpx;
  background: rgba(59, 130, 246, 0.1);
  bottom: 10%;
  right: -150rpx;
  animation: float 16s ease-in-out infinite alternate-reverse;
}

@keyframes float {
  0% {
    transform: translate(0, 0) scale(1);
  }
  100% {
    transform: translate(30rpx, 50rpx) scale(1.1);
  }
}

/* ── UI元素 ── */
.welcome-bar {
  padding: 40rpx 0 32rpx;
  position: relative;
  z-index: 10;
}

/* ── Hero 区动效 ── */
.hero-visual-box {
  width: 120rpx;
  height: 120rpx;
  position: relative;
}
.hero-orbit {
  position: absolute;
  inset: 0;
  border: 2rpx dashed var(--border-regular);
  border-radius: 50%;
  animation: spin 15s linear infinite;
}
.hero-core {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  width: 70rpx;
  height: 70rpx;
  background: var(--primary-color);
  border-radius: 50%;
  box-shadow: var(--shadow-colored);
}
.status-pending {
  background: var(--color-warning-bg);
  color: #92400e;
}
.status-active {
  background: var(--color-success-bg);
  color: #065f46;
}

/* Hazard Tags */
.hazard-chip {
  background: #fdf2f8;
  color: #9d174d;
  font-weight: 700;
  transition: all 0.2s;
  border-radius: 8rpx;
  padding: 12rpx 20rpx;
}
.hazard-active {
  background: var(--color-error);
  color: white;
  border-color: var(--color-error);
  box-shadow: 0 4rpx 12rpx rgba(239, 68, 68, 0.3);
}

@media (prefers-color-scheme: dark) {
  .farmer-page {
    background: #0f172a;
  }
}
</style>
