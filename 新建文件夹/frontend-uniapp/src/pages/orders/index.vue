<template>
  <view class="orders-page">
    <!-- 顶栏 -->
    <view class="orders-header safe-area-top scene-enter">
      <view class="header-left">
        <text class="header-title">我的联系</text>
        <text class="header-sub">联系记录 · 服务反馈</text>
      </view>
    </view>

    <!-- Tab 状态筛选栏 -->
    <view class="tab-bar scene-enter delay-1">
      <view
        class="tab-item"
        v-for="tab in tabs"
        :key="tab.key"
        :class="{ active: currentTab === tab.key }"
        @click="currentTab = tab.key"
      >
        <text class="tab-label">{{ tab.label }}</text>
        <view class="tab-count" v-if="tab.count > 0">{{ tab.count }}</view>
      </view>
    </view>

    <!-- 订单列表 (有单时显示) -->
    <scroll-view class="order-list" scroll-y v-if="filteredOrders.length > 0">
      <view
        v-for="(order, index) in filteredOrders"
        :key="order.id"
        class="order-card card-glass scene-enter"
        :style="{ animationDelay: (0.1 + index * 0.05) + 's' }"
      >
        <!-- 订单头 -->
        <view class="oc-header">
          <view class="oc-type-tag" :class="'tag-' + order.type">
            <text class="tag-dot"></text>
            {{ order.typeLabel }}
          </view>
          <view class="oc-status" :class="'st-' + order.status">{{ order.statusLabel }}</view>
        </view>

        <!-- 联系内容 -->
        <view class="oc-body">
          <view class="oc-main">
            <text class="oc-title">{{ order.title }}</text>
            <text class="oc-detail">{{ order.detail }}</text>
            <view class="oc-time-row">
              <text class="oc-icon">🕒</text>
              <text class="oc-time">{{ order.time }}</text>
            </view>
          </view>
          <view class="oc-rating-box" v-if="order.rating">
            <text class="oc-rating-label">评价</text>
            <text class="oc-rating-stars">{{ '⭐'.repeat(order.rating) }}</text>
          </view>
        </view>

        <!-- 联系底部按钮 -->
        <view class="oc-footer" v-if="order.status !== 'cancelled'">
           <view class="oc-btn oc-btn-ghost" hover-class="btn-hover" v-if="order.status === 'done'">查看详情</view>
           <view class="oc-btn oc-btn-ghost" hover-class="btn-hover" v-if="order.status === 'done' && !order.rating">评价反馈</view>
           <view class="oc-btn oc-btn-primary" hover-class="btn-press" v-if="order.status === 'progress'">查看联系方式</view>
           <view class="oc-btn oc-btn-warn" hover-class="btn-hover-warn" v-if="order.status === 'pending'">取消联系</view>
           <view class="oc-btn oc-btn-primary" hover-class="btn-press" v-if="order.status === 'pending'">拨打电话</view>
        </view>
      </view>

      <view class="tabbar-placeholder"></view>
    </scroll-view>

    <!-- 空状态 (无单时) -->
    <view class="empty-state scene-enter delay-2" v-else>
      <view class="empty-illustration">
        <view class="empty-circle c1"></view>
        <view class="empty-circle c2"></view>
        <view class="empty-icon"></view>
      </view>
      <text class="empty-title">{{ emptyTitle }}</text>
      <text class="empty-desc">{{ emptyDesc }}</text>
      <view class="empty-btn" hover-class="btn-press" @click="goHome">去发个需求试试</view>
    </view>

    <custom-tab-bar current="orders" />
    <publish-action-sheet />
  </view>
</template>

<script>
import CustomTabBar from '../../components/CustomTabBar.vue'
import PublishActionSheet from '../../components/PublishActionSheet.vue'

export default {
  components: { CustomTabBar, PublishActionSheet },
  data() {
    return {
      currentTab: 'all',
      tabs: [
        { key: 'all', label: '全部', count: 3 },
        { key: 'pending', label: '已联系', count: 1 },
        { key: 'progress', label: '服务中', count: 1 },
        { key: 'done', label: '已完成', count: 1 },
        { key: 'cancelled', label: '已关闭', count: 0 },
      ],
      // 仿真联系记录（代表一个真实使用过平台的农户的联系历史）
      orders: [
        {
          id: 1, type: 'harvest', typeLabel: '小麦收割',
          title: '联合收割机 · 小麦收割',
          detail: '河南驻马店西平县盆尧镇 · 15亩 · 张师傅',
          time: '2026-06-12 下午3:20',
          rating: 5,
          status: 'done', statusLabel: '已完成',
        },
        {
          id: 2, type: 'planting', typeLabel: '夏玉米播种',
          title: '玉米精量播种机 · 抢茬播种',
          detail: '河南驻马店西平县盆尧镇马湾村 · 15亩 · 李师傅',
          time: '2026-06-14 上午8:00',
          rating: null,
          status: 'progress', statusLabel: '服务中',
        },
        {
          id: 3, type: 'spray', typeLabel: '植保飞防',
          title: '无人机喷洒 · 除草剂',
          detail: '河南驻马店西平县盆尧镇刘店村 · 8亩 · 王师傅',
          time: '2026-06-15 预约',
          rating: null,
          status: 'pending', statusLabel: '已联系',
        },
      ],
    }
  },
  computed: {
    filteredOrders() {
      if (this.currentTab === 'all') return this.orders
      return this.orders.filter(o => o.status === this.currentTab)
    },
    emptyTitle() {
      const map = { pending: '没有联系记录', progress: '没有服务中的', done: '还没完成过', cancelled: '没有关闭记录', all: '暂无联系记录' }
      return map[this.currentTab] || '暂无联系记录'
    },
    emptyDesc() {
      return this.currentTab === 'all' ? '去首页找找附近的农机主吧～' : '换个标签看看吧'
    },
  },
  onShow() {
    uni.setNavigationBarTitle({ title: '我的联系' })
  },
  methods: {
    goHome() {
      uni.switchTab({ url: '/pages/farmer/home' })
    },
  },
}
</script>

<style scoped>
.orders-page {
  min-height: 100vh;
  background: var(--page-bg);
  display: flex;
  flex-direction: column;
}

/* 顶栏 */
.orders-header {
  background: var(--surface-frosted);
  backdrop-filter: blur(var(--blur-lg));
  -webkit-backdrop-filter: blur(var(--blur-lg));
  border-bottom: 1px solid var(--border-light);
  padding: 40rpx 32rpx 32rpx;
}
.header-title {
  font-size: 44rpx;
  font-weight: 900;
  color: var(--text-primary);
  letter-spacing: 2rpx;
}
.header-sub {
  font-size: 24rpx;
  color: var(--text-muted);
  margin-top: 8rpx;
  display: block;
}

/* Tab 筛选栏 */
.tab-bar {
  display: flex;
  background: var(--surface-frosted);
  backdrop-filter: blur(var(--blur-lg));
  -webkit-backdrop-filter: blur(var(--blur-lg));
  border-bottom: 1px solid var(--border-light);
  padding: 6rpx 16rpx;
  position: sticky;
  top: 0;
  z-index: 10;
}
.tab-item {
  flex: 1;
  text-align: center;
  padding: 24rpx 8rpx;
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8rpx;
  transition: all var(--duration-200);
}
.tab-label {
  font-size: 26rpx;
  font-weight: 600;
  color: var(--text-muted);
}
.tab-item.active .tab-label {
  color: var(--primary-color);
  font-weight: 800;
}
.tab-item.active::after {
  content: '';
  position: absolute;
  bottom: 0; left: 25%; right: 25%;
  height: 6rpx;
  border-radius: 999rpx;
  background: var(--primary-color);
}
.tab-count {
  min-width: 32rpx; height: 32rpx;
  border-radius: 100rpx;
  background: var(--color-error);
  color: white;
  font-size: 20rpx;
  font-weight: 800;
  display: flex; align-items: center; justify-content: center;
  padding: 0 8rpx;
}

/* 订单列表 */
.order-list {
  flex: 1;
  padding: 24rpx 24rpx 0;
}

/* 单个订单卡片 */
.order-card {
  margin-bottom: 24rpx;
  border-radius: var(--radius-2xl);
  padding: 32rpx;
}

/* .card-glass 复用全局 card.css 定义，不再本地覆盖 */

.oc-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24rpx;
}

.oc-type-tag {
  font-size: 22rpx;
  font-weight: 700;
  padding: 8rpx 20rpx;
  border-radius: var(--radius-full);
  display: flex; align-items: center; gap: 10rpx;
}
.tag-dot { width: 12rpx; height: 12rpx; border-radius: 50%; opacity: 0.8; }
.tag-harvest { background: var(--color-warning-bg); color: var(--color-warning); }
.tag-harvest .tag-dot { background: var(--color-warning); }
.tag-planting { background: var(--color-success-bg); color: var(--color-success); }
.tag-planting .tag-dot { background: var(--color-success); }
.tag-spray { background: var(--color-info-bg); color: var(--color-info); }
.tag-spray .tag-dot { background: var(--color-info); }

.oc-status {
  font-size: 24rpx;
  font-weight: 800;
}
.st-done { color: var(--text-soft); }
.st-progress { color: var(--color-info); }
.st-pending { color: var(--color-warning); }
.st-cancelled { color: var(--text-muted); }

.oc-body {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 20rpx;
}

.oc-main {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 12rpx;
}
.oc-title { font-size: 32rpx; font-weight: 800; color: var(--text-primary); }
.oc-detail { font-size: 26rpx; color: var(--text-regular); line-height: 1.5; }
.oc-time-row { display: flex; align-items: center; gap: 8rpx; }
.oc-icon { font-size: 24rpx; opacity: 0.6; }
.oc-time { font-size: 24rpx; color: var(--text-muted); font-weight: 500; }

.oc-rating-box {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  background: rgba(245, 158, 11, 0.05);
  padding: 12rpx 20rpx;
  border-radius: var(--radius-md);
  border: 1px solid rgba(245, 158, 11, 0.1);
}
.oc-rating-label { font-size: 20rpx; color: var(--color-warning); font-weight: 700; margin-bottom: 4rpx; }
.oc-rating-stars { font-size: 24rpx; filter: drop-shadow(0 2rpx 4rpx rgba(245, 158, 11, 0.3)); }

/* 卡片底部按钮 */
.oc-footer {
  display: flex;
  justify-content: flex-end;
  gap: 16rpx;
  margin-top: 28rpx;
  padding-top: 24rpx;
  border-top: 1px dashed var(--border-light);
}
.oc-btn {
  font-size: 24rpx;
  font-weight: 700;
  padding: 16rpx 36rpx;
  border-radius: var(--radius-full);
  transition: all var(--duration-200);
}
.btn-press { transform: scale(0.95); opacity: 0.9; }
.btn-hover { background: var(--secondary-strong) !important; transform: scale(0.98); }
.btn-hover-warn { background: rgba(239, 68, 68, 0.15) !important; transform: scale(0.98); }

.oc-btn-ghost {
  background: var(--secondary-color);
  color: var(--text-regular);
}
.oc-btn-primary {
  background: var(--gradient-primary);
  color: white;
  box-shadow: var(--shadow-colored);
}
.oc-btn-warn {
  background: rgba(239, 68, 68, 0.08);
  color: var(--color-error);
  border: 1px solid rgba(239, 68, 68, 0.1);
}

/* 空状态 */
.empty-state {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 80rpx 60rpx;
}
.empty-illustration {
  position: relative;
  width: 240rpx; height: 240rpx;
  margin-bottom: 48rpx;
  display: flex; align-items: center; justify-content: center;
}
.empty-circle {
  position: absolute;
  border-radius: 50%;
  border: 4rpx dashed var(--border-regular);
}
.c1 { width: 240rpx; height: 240rpx; animation: spin 16s linear infinite; }
.c2 { width: 160rpx; height: 160rpx; animation: spin 10s linear infinite reverse; border-color: rgba(16, 185, 129, 0.2); }
@keyframes spin { 0% { transform: rotate(0deg); } 100% { transform: rotate(360deg); } }

.empty-icon { font-size: 80rpx; z-index: 1; filter: drop-shadow(0 8rpx 16rpx rgba(0,0,0,0.1)); }
.empty-title {
  font-size: 36rpx; font-weight: 800; color: var(--text-primary);
  margin-bottom: 16rpx;
}
.empty-desc {
  font-size: 26rpx; color: var(--text-muted); margin-bottom: 48rpx;
}
.empty-btn {
  background: var(--gradient-primary);
  color: white;
  font-size: 30rpx; font-weight: 800;
  padding: 24rpx 64rpx;
  border-radius: var(--radius-full);
  box-shadow: var(--shadow-colored);
}

.tabbar-placeholder { height: 200rpx; }

/* 动画复用全局 card.css 中的 .scene-enter / .delay-* 定义 */
</style>
