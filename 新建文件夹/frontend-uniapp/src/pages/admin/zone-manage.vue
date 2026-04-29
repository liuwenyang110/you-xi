<template>
  <view class="zone-admin-page">
    <view class="page-header">
      <text class="page-title">🗺 服务区域管理</text>
      <text class="page-sub">管理公益对接片区，覆盖更多村镇</text>
    </view>

    <!-- 搜索与过滤 -->
    <view class="search-bar">
      <view class="search-input-wrap">
        <text class="search-icon">🔍</text>
        <input
          class="search-input"
          v-model="keyword"
          placeholder="搜索片区名称..."
          @input="onSearch"
        />
      </view>
      <picker mode="selector" :range="typeOptions" range-key="label"
              :value="typeIndex" @change="onTypeChange">
        <view class="filter-btn">{{ typeOptions[typeIndex].label }} ›</view>
      </picker>
    </view>

    <!-- 统计概览 -->
    <view class="stats-row">
      <view class="stat-card">
        <text class="stat-num">{{ totalZones }}</text>
        <text class="stat-label">片区总数</text>
      </view>
      <view class="stat-card">
        <text class="stat-num">{{ activeZones }}</text>
        <text class="stat-label">已激活</text>
      </view>
      <view class="stat-card">
        <text class="stat-num">{{ totalOperators }}</text>
        <text class="stat-label">服务者</text>
      </view>
      <view class="stat-card">
        <text class="stat-num">{{ totalMachinery }}</text>
        <text class="stat-label">农机台数</text>
      </view>
    </view>

    <!-- 片区列表 -->
    <view class="zone-list">
      <view
        v-for="zone in filteredZones"
        :key="zone.id"
        class="zone-card"
        @click="showDetail(zone)"
      >
        <view class="zone-card-header">
          <view class="zone-name-row">
            <text class="zone-name">{{ zone.name }}</text>
            <view class="zone-type-tag" :class="'type-' + zone.zoneType">
              {{ typeLabel(zone.zoneType) }}
            </view>
          </view>
          <view class="zone-status-dot" :class="zone.status === 'ACTIVE' ? 'dot-active' : 'dot-inactive'"></view>
        </view>

        <text class="zone-location-desc" v-if="zone.locationDesc">{{ zone.locationDesc }}</text>

        <view class="zone-stats-row">
          <view class="zone-micro-stat">
            <text class="mstat-icon"></text>
            <text class="mstat-val">{{ zone.operatorCount || 0 }}</text>
            <text class="mstat-label">服务者</text>
          </view>
          <view class="zone-micro-stat">
            <text class="mstat-icon"></text>
            <text class="mstat-val">{{ zone.machineryCount || 0 }}</text>
            <text class="mstat-label">农机</text>
          </view>
          <view class="zone-micro-stat">
            <text class="mstat-label">所属：{{ zone.townshipCode }}</text>
          </view>
        </view>
      </view>
    </view>

    <!-- 空列表 -->
    <view class="empty-state" v-if="filteredZones.length === 0 && !loading">
      <text class="empty-icon">🗺</text>
      <text class="empty-text">暂无匹配的片区</text>
    </view>

    <!-- 添加片区 浮动按钮 -->
    <view class="fab" @click="showAddForm = true">
      <text class="fab-icon">＋</text>
    </view>

    <!-- 片区详情弹出层 -->
    <view class="modal-mask" v-if="detailZone" @click.self="detailZone = null">
      <view class="modal-card">
        <view class="modal-header">
          <text class="modal-title">{{ detailZone.name }}</text>
          <text class="modal-close" @click="detailZone = null">✕</text>
        </view>
        <view class="modal-body">
          <view class="detail-row">
            <text class="detail-key">类型</text>
            <text class="detail-val">{{ typeLabel(detailZone.zoneType) }}</text>
          </view>
          <view class="detail-row">
            <text class="detail-key">状态</text>
            <text class="detail-val" :class="detailZone.status === 'ACTIVE' ? 'status-ok' : 'status-off'">
              {{ detailZone.status === 'ACTIVE' ? '正常运行' : '已停用' }}
            </text>
          </view>
          <view class="detail-row">
            <text class="detail-key">农机手</text>
            <text class="detail-val">{{ detailZone.operatorCount || 0 }} 人</text>
          </view>
          <view class="detail-row">
            <text class="detail-key">农机数量</text>
            <text class="detail-val">{{ detailZone.machineryCount || 0 }} 台</text>
          </view>
          <view class="detail-row" v-if="detailZone.locationDesc">
            <text class="detail-key">位置描述</text>
            <text class="detail-val">{{ detailZone.locationDesc }}</text>
          </view>
        </view>
        <view class="modal-actions">
          <button class="modal-btn secondary" @click="toggleStatus(detailZone)">
            {{ detailZone.status === 'ACTIVE' ? '停用片区' : '启用片区' }}
          </button>
        </view>
      </view>
    </view>

    <!-- 新增片区表单 -->
    <view class="modal-mask" v-if="showAddForm" @click.self="showAddForm = false">
      <view class="modal-card">
        <view class="modal-header">
          <text class="modal-title">➕ 新增片区</text>
          <text class="modal-close" @click="showAddForm = false">✕</text>
        </view>
        <view class="modal-body">
          <view class="form-item">
            <text class="form-label">片区名称 *</text>
            <input class="form-input" v-model="addForm.name" placeholder="如：盆尧镇南关行政村" />
          </view>
          <view class="form-item">
            <text class="form-label">片区类型</text>
            <picker mode="selector" :range="['行政村', '社区', '种植片区']"
                    :value="addTypeIndex" @change="e => addTypeIndex = e.detail.value">
              <view class="picker-field">
                <text>{{ ['行政村', '社区', '种植片区'][addTypeIndex] }}</text>
                <text>›</text>
              </view>
            </picker>
          </view>
          <view class="form-item">
            <text class="form-label">乡镇区划码</text>
            <input class="form-input" v-model="addForm.townshipCode" placeholder="如：411722100000" maxlength="12" />
          </view>
          <view class="form-item">
            <text class="form-label">位置描述（可选）</text>
            <input class="form-input" v-model="addForm.locationDesc" placeholder="如：盆尧镇南侧，沿河村落群" />
          </view>
        </view>
        <view class="modal-actions">
          <button class="modal-btn primary" :loading="adding" @click="addZone">确认新增</button>
        </view>
      </view>
    </view>
  </view>
</template>

<script>
import { getZones } from '@/api/v3/zone'

export default {
  name: 'ZoneAdmin',
  data() {
    return {
      loading: false,
      zones: [],
      keyword: '',
      typeIndex: 0,
      typeOptions: [
        { label: '全部类型', value: null },
        { label: '行政村', value: 'VILLAGE' },
        { label: '社区', value: 'COMMUNITY' },
        { label: '种植片区', value: 'FARM' },
      ],
      detailZone: null,
      showAddForm: false,
      adding: false,
      addTypeIndex: 0,
      addForm: {
        name: '',
        townshipCode: '',
        locationDesc: '',
      },
    }
  },
  computed: {
    filteredZones() {
      let list = this.zones
      if (this.keyword) {
        list = list.filter(z => z.name.includes(this.keyword))
      }
      const typeVal = this.typeOptions[this.typeIndex].value
      if (typeVal) {
        list = list.filter(z => z.zoneType === typeVal)
      }
      return list
    },
    totalZones() { return this.zones.length },
    activeZones() { return this.zones.filter(z => z.status === 'ACTIVE').length },
    totalOperators() { return this.zones.reduce((s, z) => s + (z.operatorCount || 0), 0) },
    totalMachinery() { return this.zones.reduce((s, z) => s + (z.machineryCount || 0), 0) },
  },
  async onLoad() {
    await this.loadZones()
  },
  methods: {
    async loadZones() {
      // 示例：加载河南驻马店西平县所有片区
      this.loading = true
      try {
        const res = await getZones({ countyCode: '411722000000' })
        if (res.code === 0) this.zones = res.data || []
      } catch (e) {
        uni.showToast({ title: '加载失败', icon: 'none' })
      } finally {
        this.loading = false
      }
    },
    onSearch() { /* computed 自动过滤 */ },
    onTypeChange(e) { this.typeIndex = e.detail.value },
    typeLabel(type) {
      return { VILLAGE: '行政村', COMMUNITY: '社区', FARM: '种植片区' }[type] || type
    },
    showDetail(zone) { this.detailZone = zone },
    toggleStatus(zone) {
      // TODO: 调用管理员专用接口切换状态
      const next = zone.status === 'ACTIVE' ? 'INACTIVE' : 'ACTIVE'
      uni.showModal({
        title: '确认操作',
        content: `确定${next === 'INACTIVE' ? '停用' : '启用'}「${zone.name}」片区吗？`,
        success: (r) => {
          if (r.confirm) {
            zone.status = next
            this.detailZone = null
            uni.showToast({ title: '操作成功', icon: 'success' })
          }
        }
      })
    },
    async addZone() {
      if (!this.addForm.name.trim()) {
        uni.showToast({ title: '请填写片区名称', icon: 'none' })
        return
      }
      const typeMap = ['VILLAGE', 'COMMUNITY', 'FARM']
      const payload = {
        name: this.addForm.name,
        zoneType: typeMap[this.addTypeIndex],
        townshipCode: this.addForm.townshipCode,
        locationDesc: this.addForm.locationDesc,
      }
      this.adding = true
      try {
        // TODO: 调用管理员创建片区接口
        await new Promise(resolve => setTimeout(resolve, 800))
        uni.showToast({ title: '片区创建成功', icon: 'success' })
        this.showAddForm = false
        this.addForm = { name: '', townshipCode: '', locationDesc: '' }
        await this.loadZones()
      } catch (e) {
        uni.showToast({ title: '创建失败', icon: 'none' })
      } finally {
        this.adding = false
      }
    }
  }
}
</script>

<style>
.zone-admin-page { background: #F0F7F3; min-height: 100vh; padding-bottom: 120rpx; }
.page-header { background: linear-gradient(135deg, #2D7A4F, #5BA88A); padding: 80rpx 40rpx 40rpx; }
.page-title { display: block; font-size: 42rpx; font-weight: 700; color: #fff; }
.page-sub { display: block; font-size: 26rpx; color: rgba(255,255,255,0.8); margin-top: 6rpx; }

.search-bar {
  display: flex;
  align-items: center;
  gap: 20rpx;
  padding: 20rpx 24rpx;
  background: #fff;
  box-shadow: 0 2rpx 8rpx rgba(0,0,0,0.05);
}
.search-input-wrap {
  flex: 1;
  display: flex;
  align-items: center;
  background: #F0F7F3;
  border-radius: 12rpx;
  padding: 14rpx 20rpx;
}
.search-icon { font-size: 28rpx; margin-right: 10rpx; }
.search-input { flex: 1; font-size: 28rpx; color: #333; border: none; background: transparent; }
.filter-btn {
  background: #EAF5EE;
  color: #2D7A4F;
  border-radius: 12rpx;
  padding: 14rpx 20rpx;
  font-size: 26rpx;
  font-weight: 600;
  white-space: nowrap;
}

.stats-row {
  display: flex;
  padding: 16rpx 24rpx;
  gap: 12rpx;
}
.stat-card {
  flex: 1;
  background: #fff;
  border-radius: 16rpx;
  padding: 20rpx 10rpx;
  text-align: center;
  box-shadow: 0 2rpx 10rpx rgba(45,122,79,0.06);
}
.stat-num { display: block; font-size: 40rpx; font-weight: 700; color: #2D7A4F; }
.stat-label { display: block; font-size: 22rpx; color: #6B8F7A; margin-top: 4rpx; }

.zone-list { padding: 0 24rpx; }
.zone-card {
  background: #fff;
  border-radius: 16rpx;
  padding: 24rpx;
  margin-bottom: 16rpx;
  box-shadow: 0 2rpx 12rpx rgba(0,0,0,0.06);
}
.zone-card-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 12rpx;
}
.zone-name-row { display: flex; align-items: center; flex: 1; gap: 12rpx; }
.zone-name { font-size: 32rpx; font-weight: 700; color: #1A3A28; }
.zone-type-tag {
  border-radius: 20rpx;
  padding: 4rpx 16rpx;
  font-size: 22rpx;
}
.type-VILLAGE { background: #EAF5EE; color: #2D7A4F; }
.type-COMMUNITY { background: #E3F2FD; color: #1565C0; }
.type-FARM { background: #FFF8E1; color: #F57F17; }

.zone-status-dot {
  width: 16rpx;
  height: 16rpx;
  border-radius: 50%;
  flex-shrink: 0;
  margin-top: 8rpx;
}
.dot-active { background: #2D7A4F; box-shadow: 0 0 8rpx rgba(45,122,79,0.5); }
.dot-inactive { background: #bbb; }

.zone-location-desc { display: block; font-size: 24rpx; color: #888; margin-bottom: 16rpx; }
.zone-stats-row { display: flex; align-items: center; gap: 20rpx; }
.zone-micro-stat { display: flex; align-items: center; gap: 6rpx; }
.mstat-icon { font-size: 26rpx; }
.mstat-val { font-size: 28rpx; font-weight: 700; color: #2D7A4F; }
.mstat-label { font-size: 22rpx; color: #999; }

.empty-state { display: flex; flex-direction: column; align-items: center; padding: 100rpx; }
.empty-icon { font-size: 80rpx; margin-bottom: 20rpx; }
.empty-text { font-size: 30rpx; color: #999; }

.fab {
  position: fixed;
  right: 40rpx;
  bottom: 80rpx;
  width: 100rpx;
  height: 100rpx;
  background: linear-gradient(135deg, #5BA88A, #2D7A4F);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 8rpx 32rpx rgba(45,122,79,0.4);
}
.fab-icon { font-size: 52rpx; color: #fff; font-weight: 700; }

/* 弹层 */
.modal-mask {
  position: fixed;
  inset: 0;
  background: rgba(0,0,0,0.5);
  display: flex;
  align-items: flex-end;
  z-index: 1000;
}
.modal-card {
  background: #fff;
  border-radius: 32rpx 32rpx 0 0;
  width: 100%;
  padding: 32rpx;
  max-height: 80vh;
  overflow-y: auto;
}
.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 28rpx;
}
.modal-title { font-size: 36rpx; font-weight: 700; color: #1A3A28; }
.modal-close { font-size: 36rpx; color: #999; padding: 10rpx; }
.modal-body { margin-bottom: 24rpx; }
.detail-row { display: flex; padding: 16rpx 0; border-bottom: 1rpx solid #F0F7F3; }
.detail-key { font-size: 28rpx; color: #6B8F7A; width: 160rpx; }
.detail-val { font-size: 28rpx; color: #1A3A28; font-weight: 600; }
.status-ok { color: #2D7A4F; }
.status-off { color: #F44336; }

.form-item { margin-bottom: 24rpx; }
.form-label { display: block; font-size: 28rpx; color: #555; font-weight: 600; margin-bottom: 12rpx; }
.form-input {
  display: block;
  width: 100%;
  border: 2rpx solid #E8F0E4;
  border-radius: 12rpx;
  padding: 18rpx 20rpx;
  font-size: 30rpx;
  background: #FAFCF9;
  box-sizing: border-box;
}
.picker-field {
  display: flex;
  justify-content: space-between;
  border: 2rpx solid #E8F0E4;
  border-radius: 12rpx;
  padding: 18rpx 20rpx;
  font-size: 30rpx;
  color: #333;
  background: #FAFCF9;
}

.modal-actions { padding-top: 8rpx; }
.modal-btn {
  width: 100%;
  height: 88rpx;
  border: none;
  border-radius: 44rpx;
  font-size: 30rpx;
  font-weight: 700;
}
.modal-btn.primary { background: linear-gradient(135deg, #5BA88A, #2D7A4F); color: #fff; }
.modal-btn.secondary { background: #FFF3E0; color: #E65100; }
</style>
