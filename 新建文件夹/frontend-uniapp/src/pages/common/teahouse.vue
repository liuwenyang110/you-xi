<template>
  <view class="teahouse">
    <!-- 顶部状态栏 -->
    <view class="th-header">
      <view class="th-header-left">
        <text class="th-back" @click="goBack">‹</text>
        <view class="th-header-info">
          <text class="th-title">{{ zoneName }} 交流大厅</text>
          <text class="th-subtitle" :class="'status-' + teahouseStatus">{{ statusLabel }}</text>
        </view>
      </view>
      <view class="th-header-right" @click="showTeahouseInfo = true">
        <text class="th-info-btn">i</text>
      </view>
    </view>

    <!-- 预警/倒计时横幅 -->
    <view class="warn-banner" v-if="teahouseStatus === 'CLOSING_WARN'">
      <text class="warn-icon">⚠️</text>
      <view class="warn-content">
        <text class="warn-title">大厅即将打烊</text>
        <view class="countdown-bar" v-if="daysUntilClose > 0">
          <view class="countdown-fill" :style="{ width: (daysUntilClose / 8 * 100) + '%' }"></view>
        </view>
        <text class="warn-desc">距离强制关闭还剩 <text class="countdown-num">{{ daysUntilClose }}</text> 天，发布新需求或申请延期可重置。</text>
      </view>
      <view class="warn-action" @click="showExtendModal = true">
        <text class="warn-btn">申请延期</text>
      </view>
    </view>

    <view class="warn-banner extended" v-if="teahouseStatus === 'EXTENDED'">
      <text class="warn-icon">🕐</text>
      <view class="warn-content">
        <text class="warn-title">延期中</text>
        <view class="countdown-bar" v-if="daysUntilClose > 0">
          <view class="countdown-fill extend-fill" :style="{ width: (daysUntilClose / 15 * 100) + '%' }"></view>
        </view>
        <text class="warn-desc">还剩 <text class="countdown-num">{{ daysUntilClose }}</text> 天，15天无新需求仍将关闭。</text>
      </view>
    </view>

    <view class="closed-banner" v-if="teahouseStatus === 'CLOSED' || teahouseStatus === 'NOT_CREATED'">
      <text class="closed-icon"></text>
      <text class="closed-text" v-if="teahouseStatus === 'CLOSED'">大厅已关闭，等待新需求发布后重新开张</text>
      <text class="closed-text" v-else>本村暂无交流大厅，发布首条需求即可开张</text>
    </view>

    <!-- 消息列表 -->
    <scroll-view
      class="msg-list"
      scroll-y
      :scroll-top="scrollTop"
      v-if="teahouseStatus === 'OPEN' || teahouseStatus === 'CLOSING_WARN' || teahouseStatus === 'EXTENDED'"
    >
      <view class="msg-load-more" v-if="hasMore" @click="loadMore">
        <text>加载更早的消息</text>
      </view>

      <view
        v-for="msg in messages"
        :key="msg.id"
        class="msg-item"
        :class="{ 'msg-system': msg.msgType === 'SYSTEM', 'msg-mine': msg.senderId === myUserId }"
      >
        <!-- 系统公告 -->
        <view v-if="msg.msgType === 'SYSTEM'" class="msg-system-wrap">
          <text class="msg-system-text">{{ msg.content }}</text>
        </view>

        <!-- 普通消息 -->
        <view v-else class="msg-bubble-wrap" :class="{ 'bubble-right': msg.senderId === myUserId }">
          <view class="msg-avatar">
            <text class="avatar-text">{{ msg.senderId === myUserId ? '我' : '他' }}</text>
          </view>
          <view class="msg-bubble" :class="{ 'bubble-mine': msg.senderId === myUserId }">
            <!-- 文字 -->
            <text v-if="msg.msgType === 'TEXT'" class="msg-text">{{ msg.content }}</text>
            <!-- 图片 -->
            <image v-if="msg.msgType === 'IMAGE'" :src="msg.content" mode="widthFix" class="msg-image" @click="previewImage(msg.content)"></image>
            <!-- 语音 -->
            <view v-if="msg.msgType === 'VOICE'" class="msg-voice" @click="playVoice(msg.content)">
              <text class="voice-icon">🔊</text>
              <text class="voice-label">语音消息</text>
            </view>
            <text class="msg-time">{{ formatTime(msg.createdAt) }}</text>
          </view>
        </view>
      </view>

      <view class="msg-bottom-space"></view>
    </scroll-view>

    <!-- 输入区 -->
    <view
      class="input-bar"
      v-if="teahouseStatus === 'OPEN' || teahouseStatus === 'CLOSING_WARN' || teahouseStatus === 'EXTENDED'"
    >
      <view class="input-media-btn" @click="chooseImage">
        <text class="media-icon">📷</text>
      </view>
      <input
        class="msg-input"
        v-model="inputText"
        placeholder="说点什么..."
        confirm-type="send"
        @confirm="sendTextMessage"
      />
      <view class="send-btn" :class="{ 'send-active': inputText.trim() }" @click="sendTextMessage">
        <text class="send-icon">📤</text>
      </view>
    </view>

    <!-- 延期弹窗 -->
    <view class="modal-mask" v-if="showExtendModal" @click="showExtendModal = false">
      <view class="modal-box" @click.stop="">
        <text class="modal-title">申请延期</text>
        <text class="modal-desc">请输入您希望大厅多保留几天（1-7天）</text>
        <input class="modal-input" type="number" v-model="extendInput" placeholder="如：3" />
        <view class="modal-actions">
          <view class="modal-btn cancel" @click="showExtendModal = false">
            <text>取消</text>
          </view>
          <view class="modal-btn confirm" @click="submitExtend">
            <text>确认延期</text>
          </view>
        </view>
      </view>
    </view>

    <!-- 茶馆信息弹窗 -->
    <view class="modal-mask" v-if="showTeahouseInfo" @click="showTeahouseInfo = false">
      <view class="modal-box info-box" @click.stop="">
        <text class="modal-title">关于交流大厅</text>
        <view class="info-item">
          <text class="info-label">规则说明</text>
          <text class="info-text">• 首条需求发布时自动开张</text>
          <text class="info-text">• 连续7天无新需求将发出预警</text>
          <text class="info-text">• 可申请延期1-7天</text>
          <text class="info-text">• 连续15天无新需求将强制关闭</text>
          <text class="info-text">• 关闭后所有聊天记录会被清除</text>
        </view>
        <view class="info-item" v-if="lastDemandAt">
          <text class="info-label">最近一条需求</text>
          <text class="info-text">{{ formatTime(lastDemandAt) }}</text>
        </view>
        <view class="modal-btn confirm" style="margin-top: 24rpx;" @click="showTeahouseInfo = false">
          <text>知道了</text>
        </view>
      </view>
    </view>
  </view>
</template>

<script>
import { getTeahouseMessages, sendTeahouseMessage, extendTeahouse, getTeahouseStatus } from '@/api/v3/teahouse'
import { useV3UserStore } from '@/stores/v3/userStore'
import { useV3ZoneStore } from '@/stores/v3/zoneStore'

export default {
  name: 'Teahouse',
  data() {
    return {
      teahouseStatus: 'NOT_CREATED',
      messages: [],
      page: 1,
      total: 0,
      hasMore: false,
      inputText: '',
      scrollTop: 99999,
      lastDemandAt: null,
      warnSentAt: null,
      extendedAt: null,
      extendDays: 0,
      showExtendModal: false,
      showTeahouseInfo: false,
      extendInput: '',
      loading: false,
      uploading: false,
    }
  },
  computed: {
    zoneName() { return useV3ZoneStore().zoneName || '本村' },
    zoneId() { return useV3UserStore().zoneId },
    myUserId() { return useV3UserStore().v3UserId },
    statusLabel() {
      const m = {
        OPEN: '• 营业中',
        CLOSING_WARN: '• 即将打烊',
        EXTENDED: '• 延期开放',
        CLOSED: '• 已关闭',
        NOT_CREATED: '• 未开张',
      }
      return m[this.teahouseStatus] || this.teahouseStatus
    },
    /** 精确计算距离强制关闭还剩多少天 */
    daysUntilClose() {
      if (!this.lastDemandAt) return 0
      const lastDemand = new Date(this.lastDemandAt).getTime()
      const deadline = lastDemand + 15 * 86400000 // 15天后强制关闭
      const remain = Math.ceil((deadline - Date.now()) / 86400000)
      return Math.max(0, remain)
    },
  },
  async onLoad() {
    await this.loadStatus()
    if (this.teahouseStatus !== 'NOT_CREATED' && this.teahouseStatus !== 'CLOSED') {
      await this.loadMessages()
    }
  },
  methods: {
    goBack() {
      const pages = getCurrentPages()
      if (pages.length > 1) uni.navigateBack()
      else uni.reLaunch({ url: '/pages/farmer/zone-home' })
    },
    async loadStatus() {
      const res = await getTeahouseStatus(this.zoneId)
      if (res.code === 0) {
        this.teahouseStatus = res.data.status || 'NOT_CREATED'
        this.lastDemandAt = res.data.lastDemandAt
        this.warnSentAt = res.data.warnSentAt
        this.extendedAt = res.data.extendedAt
        this.extendDays = res.data.extendDays || 0
      }
    },
    async loadMessages() {
      if (this.loading) return
      this.loading = true
      try {
        const res = await getTeahouseMessages(this.zoneId, this.page, 30)
        if (res.code === 0) {
          const list = (res.data.list || []).reverse()
          this.messages = this.page === 1 ? list : [...list, ...this.messages]
          this.total = res.data.total
          this.hasMore = this.messages.length < this.total
          this.teahouseStatus = res.data.teahouseStatus || this.teahouseStatus
          if (this.page === 1) {
            this.$nextTick(() => { this.scrollTop = 99999 })
          }
        }
      } finally {
        this.loading = false
      }
    },
    async loadMore() {
      this.page++
      await this.loadMessages()
    },
    async sendTextMessage() {
      const text = this.inputText.trim()
      if (!text) return
      this.inputText = ''
      const res = await sendTeahouseMessage(this.zoneId, text)
      if (res.code === 0) {
        this.messages.push(res.data)
        this.$nextTick(() => { this.scrollTop = 99999 })
      } else {
        uni.showToast({ title: res.message || '发送失败', icon: 'none' })
      }
    },
    async submitExtend() {
      const days = parseInt(this.extendInput)
      if (!days || days < 1 || days > 7) {
        uni.showToast({ title: '请输入1-7天', icon: 'none' })
        return
      }
      const res = await extendTeahouse(this.zoneId, days)
      if (res.code === 0) {
        uni.showToast({ title: '延期成功！', icon: 'success' })
        this.showExtendModal = false
        this.extendInput = ''
        await this.loadStatus()
        await this.loadMessages()
      } else {
        uni.showToast({ title: res.message || '延期失败', icon: 'none' })
      }
    },
    previewImage(url) {
      uni.previewImage({ urls: [url], current: url })
    },
    playVoice(url) {
      const audio = uni.createInnerAudioContext()
      audio.src = url
      audio.play()
    },
    /** 选择/拍摄图片并上传 */
    chooseImage() {
      uni.chooseImage({
        count: 1,
        sizeType: ['compressed'],
        sourceType: ['album', 'camera'],
        success: async (res) => {
          const tempPath = res.tempFilePaths[0]
          this.uploading = true
          uni.showLoading({ title: '上传中...' })
          try {
            // 上传到 OSS（或本地 fallback）
            const uploadRes = await this.uploadFile(tempPath)
            if (uploadRes.url) {
              // 发送图片消息
              const sendRes = await sendTeahouseMessage(this.zoneId, uploadRes.url, 'IMAGE', uploadRes.key)
              if (sendRes.code === 0) {
                this.messages.push(sendRes.data)
                this.$nextTick(() => { this.scrollTop = 99999 })
              }
            }
          } catch (e) {
            uni.showToast({ title: '上传失败', icon: 'none' })
          } finally {
            this.uploading = false
            uni.hideLoading()
          }
        }
      })
    },
    /** 文件上传（优先OSS，回退到后端中转） */
    async uploadFile(filePath) {
      return new Promise((resolve, reject) => {
        uni.uploadFile({
          url: this.getUploadUrl(),
          filePath,
          name: 'file',
          header: {
            'Authorization': `Bearer ${uni.getStorageSync('token') || ''}`
          },
          success: (res) => {
            try {
              const data = JSON.parse(res.data)
              if (data.code === 0) {
                resolve({ url: data.data.url, key: data.data.key || '' })
              } else {
                reject(new Error(data.message || '上传失败'))
              }
            } catch (e) {
              reject(e)
            }
          },
          fail: reject
        })
      })
    },
    getUploadUrl() {
      const base = (() => {
        try {
          const envBase = import.meta.env?.VITE_API_BASE || ''
          return envBase.replace(/\/api\/v1\/?$/, '') || 'http://127.0.0.1:8080'
        } catch { return 'http://127.0.0.1:8080' }
      })()
      return base + '/api/v3/upload/image'
    },
    formatTime(dt) {
      if (!dt) return ''
      const d = new Date(dt)
      const now = new Date()
      const diff = now - d
      if (diff < 60000) return '刚刚'
      if (diff < 3600000) return Math.floor(diff / 60000) + '分钟前'
      if (diff < 86400000) return Math.floor(diff / 3600000) + '小时前'
      return `${d.getMonth()+1}/${d.getDate()} ${String(d.getHours()).padStart(2,'0')}:${String(d.getMinutes()).padStart(2,'0')}`
    },
  },
}
</script>

<style scoped>
.teahouse { display: flex; flex-direction: column; height: 100vh; height: 100dvh; background: var(--page-bg); }

/* 顶部状态栏 */
.th-header {
  display: flex; justify-content: space-between; align-items: center;
  padding: 24rpx 32rpx; background: var(--gradient-primary);
}
.th-header-left { display: flex; align-items: center; gap: 16rpx; }
.th-back { font-size: 48rpx; font-weight: 300; color: #fff; padding: 0 8rpx; line-height: 1; }
.th-header-info { display: flex; flex-direction: column; }
.th-title { font-size: var(--text-lg); font-weight: var(--font-bold); color: #fff; }
.th-subtitle { font-size: var(--text-xs); margin-top: 4rpx; }
.status-OPEN { color: rgba(165,214,167,1); }
.status-CLOSING_WARN { color: rgba(255,224,130,1); }
.status-EXTENDED { color: rgba(255,224,130,1); }
.status-CLOSED { color: rgba(239,154,154,1); }
.status-NOT_CREATED { color: rgba(255,255,255,0.6); }
.th-info-btn {
  font-size: 28rpx; font-weight: 600; color: #fff;
  width: 40rpx; height: 40rpx; border: 2rpx solid rgba(255,255,255,0.6);
  border-radius: 50%; display: flex; align-items: center; justify-content: center;
  font-style: italic;
}

/* 预警横幅 */
.warn-banner {
  display: flex; align-items: center; gap: 16rpx;
  background: var(--color-warning-bg); padding: 20rpx 28rpx;
  border-left: 6rpx solid var(--color-warning);
}
.warn-banner.extended { border-left-color: var(--color-info); background: var(--color-info-bg); }
.warn-icon { font-size: 40rpx; flex-shrink: 0; }
.warn-content { flex: 1; }
.warn-title { display: block; font-size: var(--text-base); font-weight: var(--font-bold); color: var(--color-warning-text, #7B5800); }
.warn-desc { display: block; font-size: var(--text-xs); color: var(--color-warning-text, #996B00); line-height: 1.5; }
.warn-action { flex-shrink: 0; }
.warn-btn {
  font-size: var(--text-sm); color: #fff; background: var(--color-warning);
  padding: 10rpx 24rpx; border-radius: var(--radius-full); font-weight: var(--font-semibold);
}

.closed-banner {
  display: flex; flex-direction: column; align-items: center; justify-content: center;
  padding: 80rpx 40rpx; flex: 1;
}
.closed-icon { font-size: 100rpx; margin-bottom: 24rpx; }
.closed-text { font-size: var(--text-base); color: var(--text-soft); text-align: center; line-height: 1.6; }

/* 消息列表 */
.msg-list { flex: 1; padding: 16rpx 24rpx; overflow-y: auto; }
.msg-load-more { text-align: center; padding: 16rpx; font-size: var(--text-sm); color: var(--text-soft); }
.msg-bottom-space { height: 20rpx; }

/* 系统消息 */
.msg-system-wrap { text-align: center; padding: 12rpx 0; }
.msg-system-text {
  display: inline; font-size: var(--text-xs); color: var(--color-warning-text, #8B6914);
  background: var(--color-warning-bg); padding: 8rpx 20rpx; border-radius: var(--radius-xl);
  line-height: 1.6;
}

/* 聊天气泡 */
.msg-bubble-wrap { display: flex; align-items: flex-start; gap: 12rpx; margin-bottom: 20rpx; }
.msg-bubble-wrap.bubble-right { flex-direction: row-reverse; }

.msg-avatar {
  width: 72rpx; height: 72rpx; border-radius: 50%;
  background: var(--color-success-bg); display: flex; align-items: center; justify-content: center;
  flex-shrink: 0;
}
.avatar-emoji { font-size: 36rpx; }

.msg-bubble {
  max-width: 65%; background: var(--surface-deep); border-radius: var(--radius-xl);
  padding: 18rpx 24rpx; box-shadow: var(--shadow-sm);
  position: relative;
}
.msg-bubble.bubble-mine { background: rgba(16, 185, 129, 0.12); }

.msg-text { font-size: var(--text-base); color: var(--text-regular); line-height: 1.6; word-break: break-all; }
.msg-image { width: 100%; max-width: 400rpx; border-radius: var(--radius-md); }
.msg-voice {
  display: flex; align-items: center; gap: 8rpx; padding: 8rpx 0;
}
.voice-icon { font-size: 36rpx; }
.voice-label { font-size: var(--text-sm); color: var(--primary-color); }
.msg-time { display: block; font-size: var(--text-xs); color: var(--text-soft); margin-top: 8rpx; text-align: right; }

/* 输入栏 */
.input-bar {
  display: flex; align-items: center; gap: 16rpx;
  padding: 16rpx 24rpx; background: var(--surface-deep);
  border-top: 1rpx solid var(--border-light);
  padding-bottom: calc(16rpx + env(safe-area-inset-bottom));
}
.msg-input {
  flex: 1; height: 72rpx; background: var(--page-bg); border-radius: var(--radius-full);
  padding: 0 24rpx; font-size: var(--text-base); color: var(--text-regular);
}
.send-btn {
  width: 72rpx; height: 72rpx; border-radius: 50%;
  background: var(--secondary-strong); display: flex; align-items: center; justify-content: center;
  transition: background var(--duration-200);
}
.send-btn.send-active { background: var(--primary-color); }
.send-icon { font-size: 32rpx; }

/* 图片按钮 */
.input-media-btn {
  width: 72rpx; height: 72rpx; border-radius: 50%;
  background: var(--page-bg); display: flex; align-items: center; justify-content: center;
  flex-shrink: 0;
}
.media-icon { font-size: 32rpx; }

/* 倒计时进度条 */
.countdown-bar {
  width: 100%; height: 10rpx; background: rgba(0,0,0,0.1);
  border-radius: 5rpx; margin: 8rpx 0; overflow: hidden;
}
.countdown-fill {
  height: 100%; border-radius: 5rpx;
  background: linear-gradient(90deg, var(--color-error), var(--color-warning));
  transition: width 0.5s ease;
}
.countdown-fill.extend-fill {
  background: linear-gradient(90deg, var(--color-info), hsl(200, 80%, 40%));
}
.countdown-num {
  font-weight: 800; font-size: var(--text-lg); color: var(--color-error);
}

/* 弹窗 */
.modal-mask {
  position: fixed; top: 0; left: 0; right: 0; bottom: 0;
  background: rgba(0,0,0,0.5); display: flex; align-items: center; justify-content: center;
  z-index: 999;
}
.modal-box {
  width: 600rpx; background: var(--surface-deep); border-radius: var(--radius-2xl); padding: 40rpx;
}
.modal-title { display: block; font-size: 34rpx; font-weight: var(--font-bold); color: var(--text-primary); margin-bottom: 16rpx; text-align: center; }
.modal-desc { display: block; font-size: var(--text-base); color: var(--text-muted); margin-bottom: 24rpx; text-align: center; }
.modal-input {
  width: 100%; height: 80rpx; background: var(--page-bg); border-radius: var(--radius-lg);
  padding: 0 24rpx; font-size: var(--text-lg); text-align: center; margin-bottom: 24rpx;
}
.modal-actions { display: flex; gap: 20rpx; }
.modal-btn {
  flex: 1; height: 80rpx; border-radius: var(--radius-full);
  display: flex; align-items: center; justify-content: center; font-size: var(--text-base); font-weight: var(--font-semibold);
}
.modal-btn.cancel { background: var(--secondary-color); color: var(--text-muted); }
.modal-btn.confirm { background: var(--gradient-primary); color: #fff; }

.info-box { max-height: 80vh; overflow-y: auto; }
.info-item { margin-bottom: 20rpx; }
.info-label { display: block; font-size: var(--text-base); font-weight: var(--font-bold); color: var(--text-primary); margin-bottom: 8rpx; }
.info-text { display: block; font-size: var(--text-sm); color: var(--text-muted); line-height: 1.8; }
</style>
