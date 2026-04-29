<template>
  <view class="amap-container">
    <!-- 地图容器 -->
    <view :id="mapId" class="amap-box"></view>

    <!-- 地图顶部工具栏 -->
    <view class="map-toolbar" v-if="showToolbar">
      <view class="toolbar-btn" @click="locateMe">
        <text class="toolbar-icon">📍</text>
        <text class="toolbar-label">定位</text>
      </view>
      <view class="toolbar-btn" @click="zoomIn">
        <text class="toolbar-icon">＋</text>
      </view>
      <view class="toolbar-btn" @click="zoomOut">
        <text class="toolbar-icon">－</text>
      </view>
    </view>

    <!-- 地图状态指示 -->
    <view class="map-status" v-if="loading">
      <view class="status-pulse"></view>
      <text class="status-text">地图加载中...</text>
    </view>

    <!-- 信息气泡 -->
    <view class="info-bubble" v-if="selectedMarker" @click="closeBubble">
      <view class="bubble-content">
        <view class="bubble-header">
          <text class="bubble-title">{{ selectedMarker.title }}</text>
          <text class="bubble-close">✕</text>
        </view>
        <text class="bubble-desc" v-if="selectedMarker.desc">{{ selectedMarker.desc }}</text>
        <view class="bubble-meta" v-if="selectedMarker.distance">
          <text class="meta-icon">🚗</text>
          <text class="meta-text">距您约 {{ selectedMarker.distance }}</text>
        </view>
      </view>
    </view>
  </view>
</template>

<script>
export default {
  name: 'AMapView',
  props: {
    // 地图中心经度
    lng: { type: Number, default: 113.65 },
    // 地图中心纬度
    lat: { type: Number, default: 34.76 },
    // 缩放级别
    zoom: { type: Number, default: 13 },
    // 标记点列表 [{ lng, lat, title, desc, icon, distance }]
    markers: { type: Array, default: () => [] },
    // 是否显示工具栏
    showToolbar: { type: Boolean, default: true },
    // 地图唯一ID
    mapId: { type: String, default: 'amap-main' },
    // 地图高度
    height: { type: String, default: '500rpx' },
  },
  data() {
    return {
      map: null,
      loading: true,
      selectedMarker: null,
      mapMarkers: [],
      geolocation: null,
    }
  },
  watch: {
    markers: {
      handler(newVal) {
        this.renderMarkers(newVal)
      },
      deep: true,
    },
    lng() { this.updateCenter() },
    lat() { this.updateCenter() },
  },
  mounted() {
    // #ifdef H5
    this.$nextTick(() => {
      setTimeout(() => this.initMap(), 300)
    })
    // #endif
  },
  beforeUnmount() {
    if (this.map) {
      this.map.destroy()
      this.map = null
    }
  },
  methods: {
    initMap() {
      if (typeof AMap === 'undefined') {
        console.warn('[AMapView] AMap SDK 未加载')
        this.loading = false
        return
      }

      try {
        this.map = new AMap.Map(this.mapId, {
          zoom: this.zoom,
          center: [this.lng, this.lat],
          mapStyle: 'amap://styles/fresh',
          resizeEnable: true,
          viewMode: '2D',
        })

        this.map.on('complete', () => {
          this.loading = false
          this.$emit('map-ready', this.map)
        })

        this.map.on('click', (e) => {
          this.selectedMarker = null
          this.$emit('map-click', { lng: e.lnglat.getLng(), lat: e.lnglat.getLat() })
        })

        // 渲染初始标记
        if (this.markers.length) {
          this.renderMarkers(this.markers)
        }
      } catch (err) {
        console.error('[AMapView] 初始化失败', err)
        this.loading = false
      }
    },

    renderMarkers(list) {
      if (!this.map) return

      // 清除旧标记
      this.mapMarkers.forEach(m => this.map.remove(m))
      this.mapMarkers = []

      list.forEach((item, idx) => {
        const marker = new AMap.Marker({
          position: [item.lng, item.lat],
          title: item.title || '',
          offset: new AMap.Pixel(-13, -30),
          content: this.buildMarkerHtml(item, idx),
        })

        marker.on('click', () => {
          this.selectedMarker = item
          this.$emit('marker-click', item)
        })

        this.mapMarkers.push(marker)
        this.map.add(marker)
      })

      // 自适应视野
      if (list.length > 1) {
        this.map.setFitView(this.mapMarkers, false, [60, 60, 60, 60])
      }
    },

    buildMarkerHtml(item, idx) {
      const colors = ['#10B981', '#3B82F6', '#F59E0B', '#EF4444', '#8B5CF6']
      const color = colors[idx % colors.length]
      return `<div style="
        display:flex;align-items:center;justify-content:center;
        width:28px;height:28px;border-radius:50% 50% 50% 0;
        background:${color};color:#fff;font-size:13px;font-weight:700;
        transform:rotate(-45deg);box-shadow:0 2px 8px ${color}66;
        border:2px solid #fff;
      "><span style="transform:rotate(45deg)">${idx + 1}</span></div>`
    },

    updateCenter() {
      if (this.map) {
        this.map.setCenter([this.lng, this.lat])
      }
    },

    locateMe() {
      if (!this.map) return
      AMap.plugin('AMap.Geolocation', () => {
        this.geolocation = new AMap.Geolocation({
          enableHighAccuracy: true,
          timeout: 10000,
          buttonPosition: 'RB',
        })
        this.geolocation.getCurrentPosition((status, result) => {
          if (status === 'complete') {
            const pos = result.position
            this.map.setCenter([pos.lng, pos.lat])
            this.map.setZoom(15)
            this.$emit('located', { lng: pos.lng, lat: pos.lat, address: result.formattedAddress })
          } else {
            uni.showToast({ title: '定位失败，请检查权限', icon: 'none' })
          }
        })
      })
    },

    zoomIn() {
      if (this.map) this.map.zoomIn()
    },

    zoomOut() {
      if (this.map) this.map.zoomOut()
    },

    closeBubble() {
      this.selectedMarker = null
    },

    // 暴露给父组件的方法
    getMap() {
      return this.map
    },

    flyTo(lng, lat, zoom = 15) {
      if (this.map) {
        this.map.setZoomAndCenter(zoom, [lng, lat], true)
      }
    },
  },
}
</script>

<style scoped>
.amap-container {
  position: relative;
  width: 100%;
  border-radius: 20rpx;
  overflow: hidden;
  box-shadow: 0 8rpx 32rpx rgba(0, 0, 0, 0.08);
}

.amap-box {
  width: 100%;
  height: v-bind(height);
}

/* 工具栏 */
.map-toolbar {
  position: absolute;
  right: 20rpx;
  top: 20rpx;
  display: flex;
  flex-direction: column;
  gap: 8rpx;
  z-index: 10;
}

.toolbar-btn {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  width: 72rpx;
  height: 72rpx;
  background: rgba(255, 255, 255, 0.92);
  border-radius: 16rpx;
  box-shadow: 0 4rpx 16rpx rgba(0, 0, 0, 0.1);
  backdrop-filter: blur(10px);
  transition: all 0.2s;
}

.toolbar-btn:active {
  transform: scale(0.9);
  background: rgba(255, 255, 255, 1);
}

.toolbar-icon {
  font-size: 28rpx;
  line-height: 1;
}

.toolbar-label {
  font-size: 16rpx;
  color: var(--text-muted, #6B7280);
  margin-top: 2rpx;
}

/* 加载状态 */
.map-status {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  display: flex;
  align-items: center;
  gap: 12rpx;
  background: rgba(255, 255, 255, 0.9);
  padding: 16rpx 32rpx;
  border-radius: 40rpx;
  box-shadow: 0 4rpx 16rpx rgba(0, 0, 0, 0.1);
  backdrop-filter: blur(10px);
  z-index: 20;
}

.status-pulse {
  width: 16rpx;
  height: 16rpx;
  border-radius: 50%;
  background: var(--primary-color, #10B981);
  animation: mapPulse 1.5s ease-in-out infinite;
}

@keyframes mapPulse {
  0%, 100% { transform: scale(1); opacity: 1; }
  50% { transform: scale(1.8); opacity: 0.4; }
}

.status-text {
  font-size: 24rpx;
  color: var(--text-regular, #374151);
  font-weight: 500;
}

/* 信息气泡 */
.info-bubble {
  position: absolute;
  bottom: 20rpx;
  left: 20rpx;
  right: 20rpx;
  z-index: 15;
  animation: bubbleIn 0.3s ease-out;
}

@keyframes bubbleIn {
  from { transform: translateY(20rpx); opacity: 0; }
  to { transform: translateY(0); opacity: 1; }
}

.bubble-content {
  background: rgba(255, 255, 255, 0.95);
  border-radius: 20rpx;
  padding: 24rpx 28rpx;
  box-shadow: 0 8rpx 32rpx rgba(0, 0, 0, 0.12);
  backdrop-filter: blur(20px);
  border: 1rpx solid rgba(0, 0, 0, 0.04);
}

.bubble-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8rpx;
}

.bubble-title {
  font-size: 30rpx;
  font-weight: 700;
  color: var(--text-primary, #111827);
}

.bubble-close {
  font-size: 28rpx;
  color: var(--text-soft, #9CA3AF);
  padding: 8rpx;
}

.bubble-desc {
  font-size: 26rpx;
  color: var(--text-muted, #6B7280);
  line-height: 1.5;
  margin-bottom: 8rpx;
}

.bubble-meta {
  display: flex;
  align-items: center;
  gap: 8rpx;
}

.meta-icon {
  font-size: 22rpx;
}

.meta-text {
  font-size: 24rpx;
  color: var(--primary-color, #10B981);
  font-weight: 600;
}
</style>
