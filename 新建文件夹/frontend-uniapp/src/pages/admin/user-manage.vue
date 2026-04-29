<template>
  <view class="user-admin-page">
    <view class="page-header">
      <text class="page-title">👥 社区用户管理</text>
      <text class="page-sub">求助者 {{ farmerCount }} · 服务者 {{ operatorCount }}</text>
    </view>

    <!-- 角色切换 Tab -->
    <view class="role-tabs">
      <view
        class="role-tab"
        :class="{ active: activeTab === 'FARMER' }"
        @click="switchTab('FARMER')"
      >求助者</view>
      <view
        class="role-tab"
        :class="{ active: activeTab === 'OPERATOR' }"
        @click="switchTab('OPERATOR')"
      >服务者</view>
    </view>

    <!-- 搜索栏 -->
    <view class="search-wrap">
      <input
        class="search-input"
        v-model="keyword"
        :placeholder="`搜索${activeTab === 'FARMER' ? '求助者' : '服务者'}姓名...`"
      />
    </view>

    <!-- 用户列表 -->
    <view class="user-list">
      <view
        v-for="user in filteredUsers"
        :key="user.id"
        class="user-card"
        @click="openDetail(user)"
      >
        <view class="user-avatar">{{ user.realName ? user.realName[0] : '？' }}</view>
        <view class="user-info">
          <text class="user-name">{{ user.realName || '未设置姓名' }}</text>
          <text class="user-meta">
            {{ user.zoneId ? `所在片区 #${user.zoneId}` : '未入驻片区' }}
            {{ user.zoneJoinedAt ? ` · 已入驻` : '' }}
          </text>
        </view>
        <view class="user-status" :class="'status-' + user.status">
          {{ { NORMAL: '正常', FROZEN: '冻结', BANNED: '封禁' }[user.status] || user.status }}
        </view>
      </view>

      <view class="empty-state" v-if="filteredUsers.length === 0 && !loading">
        <text class="empty-icon">{{ activeTab === 'FARMER' ? '🌾' : '🚜' }}</text>
        <text class="empty-text">暂无{{ activeTab === 'FARMER' ? '求助者' : '服务者' }}数据</text>
      </view>
    </view>

    <!-- 用户详情弹层 -->
    <view class="modal-mask" v-if="detailUser" @click.self="detailUser = null">
      <view class="modal-card">
        <view class="modal-header">
          <view class="detail-avatar large">{{ detailUser.realName ? detailUser.realName[0] : '？' }}</view>
          <view>
            <text class="modal-title">{{ detailUser.realName || '未设置姓名' }}</text>
            <text class="modal-sub">{{ detailUser.role === 'FARMER' ? '求助者' : '服务者（农机手）' }}</text>
          </view>
          <text class="modal-close" @click="detailUser = null">✕</text>
        </view>
        <view class="modal-body">
          <view class="detail-row">
            <text class="detail-key">状态</text>
            <text class="detail-val" :class="'status-' + detailUser.status">
              {{ { NORMAL: '正常', FROZEN: '冻结', BANNED: '封禁' }[detailUser.status] }}
            </text>
          </view>
          <view class="detail-row">
            <text class="detail-key">所在片区</text>
            <text class="detail-val">{{ detailUser.zoneId ? `片区 #${detailUser.zoneId}` : '未入驻' }}</text>
          </view>
          <view class="detail-row" v-if="detailUser.homeLocation">
            <text class="detail-key">住址</text>
            <text class="detail-val">{{ detailUser.homeLocation }}</text>
          </view>
          <view class="detail-row">
            <text class="detail-key">注册日期</text>
            <text class="detail-val">{{ formatDate(detailUser.createdAt) }}</text>
          </view>
        </view>
        <view class="modal-actions" v-if="detailUser.status === 'NORMAL'">
          <button class="modal-btn danger" @click="changeStatus(detailUser, 'FROZEN')">冻结账号</button>
        </view>
        <view class="modal-actions" v-else>
          <button class="modal-btn primary" @click="changeStatus(detailUser, 'NORMAL')">恢复正常</button>
        </view>
      </view>
    </view>
  </view>
</template>

<script>
import request from '@/api/v3/request'

export default {
  name: 'UserAdmin',
  data() {
    return {
      loading: false,
      activeTab: 'FARMER',
      farmers: [],
      operators: [],
      keyword: '',
      detailUser: null,
    }
  },
  computed: {
    farmerCount() { return this.farmers.length },
    operatorCount() { return this.operators.length },
    filteredUsers() {
      const list = this.activeTab === 'FARMER' ? this.farmers : this.operators
      if (!this.keyword) return list
      return list.filter(u => (u.realName || '').includes(this.keyword))
    }
  },
  async onLoad() {
    await this.loadUsers()
  },
  async onPullDownRefresh() {
    await this.loadUsers()
    uni.stopPullDownRefresh()
  },
  methods: {
    async loadUsers() {
      this.loading = true
      try {
        // 管理员查询接口（后期对接真实分页接口）
        const [fr, or] = await Promise.all([
          request.get('/api/v3/admin/users', { params: { role: 'FARMER', size: 100 } }),
          request.get('/api/v3/admin/users', { params: { role: 'OPERATOR', size: 100 } }),
        ])
        this.farmers = (fr.code === 0 ? fr.data?.list : []) || []
        this.operators = (or.code === 0 ? or.data?.list : []) || []
      } finally {
        this.loading = false
      }
    },
    switchTab(role) {
      this.activeTab = role
      this.keyword = ''
    },
    openDetail(user) { this.detailUser = { ...user } },
    changeStatus(user, status) {
      uni.showModal({
        title: '确认操作',
        content: `确定将「${user.realName}」状态改为 ${status === 'FROZEN' ? '冻结' : '正常'} 吗？`,
        success: async (r) => {
          if (!r.confirm) return
          const res = await request.put(`/api/v3/admin/users/${user.id}/status`, { status })
          if (res.code === 0) {
            uni.showToast({ title: '操作成功', icon: 'success' })
            this.detailUser.status = status
            await this.loadUsers()
          }
        }
      })
    },
    formatDate(dt) {
      if (!dt) return '—'
      const d = new Date(dt)
      return `${d.getFullYear()}-${String(d.getMonth()+1).padStart(2,'0')}-${String(d.getDate()).padStart(2,'0')}`
    }
  }
}
</script>

<style>
.user-admin-page { background: #F0F7F3; min-height: 100vh; padding-bottom: 60rpx; }
.page-header { background: linear-gradient(135deg, #2D7A4F, #5BA88A); padding: 80rpx 40rpx 40rpx; }
.page-title { display: block; font-size: 42rpx; font-weight: 700; color: #fff; }
.page-sub { display: block; font-size: 26rpx; color: rgba(255,255,255,0.85); margin-top: 6rpx; }

.role-tabs { display: flex; background: #fff; box-shadow: 0 2rpx 8rpx rgba(0,0,0,0.06); }
.role-tab { flex: 1; text-align: center; padding: 24rpx; font-size: 28rpx; color: #888; font-weight: 600; border-bottom: 4rpx solid transparent; transition: all .2s; }
.role-tab.active { color: #2D7A4F; border-bottom-color: #5BA88A; }

.search-wrap { padding: 16rpx 24rpx; background: #fff; border-top: 1rpx solid #F0F7F3; }
.search-input { width: 100%; background: #F0F7F3; border-radius: 12rpx; padding: 14rpx 20rpx; font-size: 28rpx; box-sizing: border-box; }

.user-list { padding: 16rpx 24rpx 0; }
.user-card { background: #fff; border-radius: 16rpx; padding: 24rpx; margin-bottom: 16rpx; display: flex; align-items: center; box-shadow: 0 2rpx 10rpx rgba(45,122,79,0.06); }
.user-avatar { width: 80rpx; height: 80rpx; border-radius: 50%; background: linear-gradient(135deg, #5BA88A, #2D7A4F); display: flex; align-items: center; justify-content: center; font-size: 36rpx; color: #fff; font-weight: 700; flex-shrink: 0; }
.user-info { flex: 1; padding: 0 20rpx; }
.user-name { display: block; font-size: 30rpx; font-weight: 700; color: #1A3A28; }
.user-meta { display: block; font-size: 24rpx; color: #6B8F7A; margin-top: 6rpx; }
.user-status { font-size: 24rpx; border-radius: 20rpx; padding: 6rpx 18rpx; font-weight: 600; }
.status-NORMAL { background: #EAF5EE; color: #2D7A4F; }
.status-FROZEN { background: #E3F2FD; color: #1565C0; }
.status-BANNED { background: #FFEBEE; color: #C62828; }

.empty-state { display: flex; flex-direction: column; align-items: center; padding: 80rpx; }
.empty-icon { font-size: 80rpx; margin-bottom: 20rpx; }
.empty-text { font-size: 30rpx; color: #999; }

/* 弹层 */
.modal-mask { position: fixed; inset: 0; background: rgba(0,0,0,0.5); display: flex; align-items: flex-end; z-index: 1000; }
.modal-card { background: #fff; border-radius: 32rpx 32rpx 0 0; width: 100%; padding: 32rpx; }
.modal-header { display: flex; align-items: center; gap: 24rpx; margin-bottom: 28rpx; position: relative; }
.detail-avatar { width: 80rpx; height: 80rpx; border-radius: 50%; background: linear-gradient(135deg, #5BA88A, #2D7A4F); display: flex; align-items:center; justify-content:center; font-size:36rpx; color:#fff; font-weight:700; flex-shrink:0; }
.detail-avatar.large { width: 96rpx; height: 96rpx; font-size: 44rpx; }
.modal-title { display: block; font-size: 34rpx; font-weight: 700; color: #1A3A28; }
.modal-sub { display: block; font-size: 24rpx; color: #6B8F7A; }
.modal-close { position: absolute; right: 0; top: 0; font-size: 36rpx; color: #bbb; padding: 10rpx; }
.modal-body { margin-bottom: 24rpx; }
.detail-row { display: flex; padding: 16rpx 0; border-bottom: 1rpx solid #F0F7F3; }
.detail-key { font-size: 28rpx; color: #6B8F7A; width: 150rpx; flex-shrink: 0; }
.detail-val { font-size: 28rpx; color: #1A3A28; font-weight: 600; }
.modal-actions { padding-top: 8rpx; }
.modal-btn { width: 100%; height: 88rpx; border: none; border-radius: 44rpx; font-size: 30rpx; font-weight: 700; }
.modal-btn.primary { background: linear-gradient(135deg, #5BA88A, #2D7A4F); color: #fff; }
.modal-btn.danger { background: #FFEBEE; color: #C62828; }
</style>
