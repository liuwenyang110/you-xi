<template>
  <view class="chat-page">
    <view v-if="session" class="chat-header safe-area-top">
      <view class="chat-title-row">
        <view>
          <text class="chat-title">{{ session.title }}</text>
          <text class="chat-subtitle">{{ session.partnerName }}</text>
        </view>
        <view class="chat-status">{{ session.statusLabel }}</view>
      </view>
      <view class="chat-banner">
        <text class="chat-banner-text">{{ session.disclaimer }}</text>
      </view>
    </view>

    <scroll-view
      v-if="session"
      class="chat-scroll"
      scroll-y
      :scroll-into-view="anchorId"
      @scrolltoupper="refreshSession"
    >
      <view class="status-card">
        <text class="status-card-title">{{ session.statusLabel }}</text>
        <text class="status-card-desc">{{ session.statusDescription }}</text>
        <text class="status-card-sync">{{ syncText }}</text>
      </view>

      <view
        v-for="message in messages"
        :key="message.id"
        class="message-row"
        :class="{
          'message-self': message.sender === 'self',
          'message-system': message.sender === 'system'
        }"
      >
        <view v-if="message.sender !== 'system'" class="message-avatar">
          <text>{{ message.sender === 'self' ? '我' : session.avatarText }}</text>
        </view>

        <view class="message-body">
          <text v-if="message.sender === 'system'" class="message-system-text">{{ message.content }}</text>
          <view v-else class="message-bubble" :class="{ 'bubble-self': message.sender === 'self' }">
            <image
              v-if="message.type === 'IMAGE' && message.imageUrl"
              class="message-image"
              :src="message.imageUrl"
              mode="aspectFill"
              @click="previewImage(message.imageUrl)"
            />
            <text v-else class="message-text">{{ message.content }}</text>
          </view>
          <text v-if="message.sender !== 'system'" class="message-time">{{ formatRelative(message.createdAt) }}</text>
        </view>
      </view>

      <view :id="anchorId" class="scroll-anchor"></view>
    </scroll-view>

    <view v-if="session" class="action-panel">
      <view class="action-row">
        <view
          v-for="action in statusActions"
          :key="action.key"
          class="action-pill"
          :class="{ warning: action.warning }"
          @click="handleStatusAction(action.key)"
        >
          <text>{{ action.label }}</text>
        </view>
      </view>

      <scroll-view class="quick-row" scroll-x>
        <view
          v-for="reply in collabStore.quickReplies"
          :key="reply"
          class="quick-pill"
          @click="sendQuickReply(reply)"
        >
          <text>{{ reply }}</text>
        </view>
      </scroll-view>

      <view v-if="showFeedback" class="feedback-card">
        <text class="feedback-title">完成反馈</text>
        <view class="feedback-stars">
          <view
            v-for="star in [1, 2, 3, 4, 5]"
            :key="star"
            class="star-item"
            :class="{ active: rating >= star }"
            @click="rating = star"
          >
            ★
          </view>
        </view>
        <textarea
          v-model="feedbackText"
          class="feedback-input"
          maxlength="120"
          placeholder="可以补充到场情况、服务质量和后续建议。"
        />
        <view class="feedback-submit" @click="submitFeedback">提交反馈</view>
      </view>

      <view class="composer">
        <view class="composer-image" @click="pickImage">图</view>
        <input
          v-model="draft"
          class="composer-input"
          maxlength="120"
          placeholder="发送公益协作消息"
          confirm-type="send"
          @confirm="sendMessage"
        />
        <view class="composer-send" @click="sendMessage">发送</view>
      </view>
    </view>

    <view v-else class="chat-empty">
      <text class="chat-empty-title">正在打开会话...</text>
    </view>
  </view>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue'
import { onHide, onLoad, onShow, onUnload } from '@dcloudio/uni-app'
import { useCollabStore } from '@/stores/collab'
import { formatRelativeTime } from '@/utils/collab'
import type { CollabSessionStatus } from '@/types/collab'

const collabStore = useCollabStore()
const draft = ref('')
const feedbackText = ref('')
const rating = ref(5)
const currentSessionId = ref('')
const anchorId = ref('chat-anchor')

let pollTimer: ReturnType<typeof setInterval> | null = null

const session = computed(() => collabStore.activeSession)
const messages = computed(() =>
  currentSessionId.value ? collabStore.sessionMessages(currentSessionId.value) : []
)
const showFeedback = computed(() => session.value?.status === 'COMPLETED')
const syncText = computed(() => {
  if (!currentSessionId.value) {
    return ''
  }
  const state = collabStore.syncStatus.messages[currentSessionId.value] || collabStore.syncStatus.sessions
  if (state === 'offline') {
    return '当前消息采用本地持久化'
  }
  if (state === 'syncing') {
    return '正在同步会话状态'
  }
  return 'REST 轮询已开启'
})

const statusActions = computed(() => {
  const currentStatus = session.value?.status
  if (!currentStatus) {
    return []
  }

  if (currentStatus === 'PUBLISHED' || currentStatus === 'CLAIMED') {
    return [
      { key: 'COMMUNICATING', label: '确认沟通' },
      { key: 'CLOSED', label: '关闭协作', warning: true }
    ]
  }
  if (currentStatus === 'COMMUNICATING') {
    return [
      { key: 'ARRIVING', label: '标记到场中' },
      { key: 'CLOSED', label: '关闭协作', warning: true }
    ]
  }
  if (currentStatus === 'ARRIVING') {
    return [
      { key: 'IN_SERVICE', label: '标记服务中' },
      { key: 'CLOSED', label: '关闭协作', warning: true }
    ]
  }
  if (currentStatus === 'IN_SERVICE') {
    return [
      { key: 'COMPLETED', label: '标记已完成' },
      { key: 'CLOSED', label: '关闭协作', warning: true }
    ]
  }
  if (currentStatus === 'COMPLETED') {
    return [{ key: 'COMPLETED', label: '等待反馈' }]
  }
  return [{ key: 'CLOSED', label: '会话已关闭', warning: true }]
})

function formatRelative(value: string) {
  return formatRelativeTime(value)
}

async function refreshSession() {
  if (!currentSessionId.value) {
    return
  }
  collabStore.syncStatus.messages[currentSessionId.value] = 'syncing'
  await collabStore.loadSessions({ silent: true })
  collabStore.setActiveSession(currentSessionId.value)
  collabStore.syncStatus.messages[currentSessionId.value] = collabStore.syncStatus.sessions
}

async function sendMessage() {
  if (!currentSessionId.value) {
    return
  }
  const text = draft.value.trim()
  if (!text) {
    uni.showToast({ title: '先输入一点内容', icon: 'none' })
    return
  }

  await collabStore.sendTextMessage(currentSessionId.value, text, 'TEXT')
  draft.value = ''
  anchorId.value = `chat-anchor-${Date.now()}`
}

async function sendQuickReply(reply: string) {
  if (!currentSessionId.value) {
    return
  }
  await collabStore.sendTextMessage(currentSessionId.value, reply, 'QUICK_REPLY')
  anchorId.value = `chat-anchor-${Date.now()}`
}

async function pickImage() {
  if (!currentSessionId.value) {
    return
  }

  uni.chooseImage({
    count: 1,
    sizeType: ['compressed'],
    success: async (res) => {
      const file = res.tempFilePaths?.[0]
      if (!file) {
        return
      }
      await collabStore.sendImageMessage(currentSessionId.value, file)
      anchorId.value = `chat-anchor-${Date.now()}`
    }
  })
}

function previewImage(url: string) {
  uni.previewImage({ urls: [url], current: url })
}

async function handleStatusAction(nextStatus: string) {
  if (!currentSessionId.value || !session.value) {
    return
  }
  if (session.value.status === 'COMPLETED' && nextStatus === 'COMPLETED') {
    return
  }

  await collabStore.updateSessionStatus(currentSessionId.value, nextStatus as CollabSessionStatus)
}

async function submitFeedback() {
  if (!currentSessionId.value) {
    return
  }
  await collabStore.submitFeedback(currentSessionId.value, rating.value, feedbackText.value.trim())
  uni.showToast({ title: '反馈已提交', icon: 'success' })
}

function startPolling() {
  stopPolling()
  pollTimer = setInterval(() => {
    refreshSession()
  }, 12000)
}

function stopPolling() {
  if (pollTimer) {
    clearInterval(pollTimer)
    pollTimer = null
  }
}

onLoad(async (query) => {
  currentSessionId.value = decodeURIComponent(String(query?.sessionId || ''))
  if (!currentSessionId.value) {
    return
  }
  collabStore.setActiveSession(currentSessionId.value)
  await refreshSession()
  startPolling()
})

onShow(() => {
  if (currentSessionId.value) {
    collabStore.setActiveSession(currentSessionId.value)
    startPolling()
  }
})

onHide(() => {
  stopPolling()
})

onUnload(() => {
  stopPolling()
})
</script>

<style scoped>
.chat-page {
  min-height: 100vh;
  background:
    radial-gradient(circle at top, rgba(168, 214, 184, 0.3), transparent 26%),
    linear-gradient(180deg, #edf5ef 0%, #f8fbf9 100%);
  display: flex;
  flex-direction: column;
}

.chat-header {
  padding: 26rpx 24rpx 20rpx;
  background: rgba(255, 255, 255, 0.9);
  backdrop-filter: blur(16px);
}

.chat-title-row {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 18rpx;
}

.chat-title {
  display: block;
  font-size: 38rpx;
  font-weight: 900;
  color: #163626;
}

.chat-subtitle {
  display: block;
  margin-top: 8rpx;
  font-size: 24rpx;
  color: #617466;
}

.chat-status {
  padding: 14rpx 18rpx;
  border-radius: 999rpx;
  background: rgba(36, 87, 61, 0.08);
  color: #24573d;
  font-size: 22rpx;
  font-weight: 800;
}

.chat-banner {
  margin-top: 18rpx;
  padding: 18rpx 20rpx;
  border-radius: 18rpx;
  background: rgba(36, 87, 61, 0.08);
}

.chat-banner-text {
  font-size: 23rpx;
  line-height: 1.6;
  color: #486556;
}

.chat-scroll {
  flex: 1;
  padding: 20rpx 24rpx;
  box-sizing: border-box;
}

.status-card {
  padding: 24rpx;
  border-radius: 24rpx;
  background: rgba(255, 255, 255, 0.86);
  margin-bottom: 18rpx;
  box-shadow: 0 10rpx 26rpx rgba(28, 60, 42, 0.05);
}

.status-card-title {
  display: block;
  font-size: 30rpx;
  font-weight: 900;
  color: #173727;
}

.status-card-desc {
  display: block;
  margin-top: 10rpx;
  font-size: 24rpx;
  line-height: 1.65;
  color: #5c7262;
}

.status-card-sync {
  display: block;
  margin-top: 12rpx;
  font-size: 22rpx;
  color: #7b8d7d;
}

.message-row {
  display: flex;
  gap: 14rpx;
  margin-bottom: 18rpx;
}

.message-self {
  justify-content: flex-end;
}

.message-system {
  justify-content: center;
}

.message-avatar {
  width: 68rpx;
  height: 68rpx;
  border-radius: 22rpx;
  background: linear-gradient(135deg, #d3e8d8, #7db78e);
  color: #173c29;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24rpx;
  font-weight: 900;
  flex-shrink: 0;
}

.message-body {
  max-width: 72%;
}

.message-system-text {
  display: inline-block;
  padding: 14rpx 18rpx;
  border-radius: 999rpx;
  background: rgba(25, 63, 42, 0.08);
  color: #647969;
  font-size: 22rpx;
  line-height: 1.5;
  text-align: center;
}

.message-bubble {
  padding: 20rpx 22rpx;
  border-radius: 24rpx 24rpx 24rpx 8rpx;
  background: rgba(255, 255, 255, 0.92);
  color: #1e3527;
  box-shadow: 0 10rpx 22rpx rgba(31, 63, 44, 0.05);
}

.bubble-self {
  background: linear-gradient(135deg, #2c6a48, #3b885c);
  color: #fff;
  border-radius: 24rpx 24rpx 8rpx 24rpx;
}

.message-text {
  font-size: 28rpx;
  line-height: 1.7;
}

.message-image {
  width: 280rpx;
  height: 280rpx;
  border-radius: 20rpx;
  background: #eef2ee;
}

.message-time {
  display: block;
  margin-top: 8rpx;
  font-size: 20rpx;
  color: #839485;
}

.action-panel {
  padding: 18rpx 20rpx calc(env(safe-area-inset-bottom, 0px) + 18rpx);
  background: rgba(255, 255, 255, 0.92);
  backdrop-filter: blur(18px);
  border-top: 1px solid rgba(35, 85, 60, 0.08);
}

.action-row {
  display: flex;
  gap: 12rpx;
  margin-bottom: 14rpx;
}

.action-pill {
  flex: 1;
  text-align: center;
  padding: 16rpx 0;
  border-radius: 999rpx;
  background: rgba(36, 87, 61, 0.08);
  color: #24573d;
  font-size: 24rpx;
  font-weight: 800;
}

.action-pill.warning {
  background: rgba(217, 92, 77, 0.12);
  color: #b34939;
}

.quick-row {
  white-space: nowrap;
  margin-bottom: 14rpx;
}

.quick-pill {
  display: inline-flex;
  align-items: center;
  margin-right: 12rpx;
  padding: 14rpx 18rpx;
  border-radius: 999rpx;
  background: #eef3ef;
  color: #446254;
  font-size: 23rpx;
}

.feedback-card {
  margin-bottom: 14rpx;
  padding: 22rpx;
  border-radius: 24rpx;
  background: rgba(249, 244, 225, 0.92);
}

.feedback-title {
  display: block;
  font-size: 28rpx;
  font-weight: 900;
  color: #684d10;
}

.feedback-stars {
  display: flex;
  gap: 10rpx;
  margin-top: 14rpx;
}

.star-item {
  font-size: 40rpx;
  color: rgba(155, 110, 13, 0.28);
}

.star-item.active {
  color: #c98819;
}

.feedback-input {
  width: 100%;
  min-height: 120rpx;
  margin-top: 16rpx;
  padding: 18rpx;
  border-radius: 18rpx;
  background: rgba(255, 255, 255, 0.84);
  font-size: 26rpx;
  box-sizing: border-box;
}

.feedback-submit {
  margin-top: 16rpx;
  text-align: center;
  padding: 18rpx 0;
  border-radius: 999rpx;
  background: linear-gradient(135deg, #8f6a19, #c28d22);
  color: #fff;
  font-size: 26rpx;
  font-weight: 800;
}

.composer {
  display: flex;
  align-items: center;
  gap: 12rpx;
}

.composer-image,
.composer-send {
  flex-shrink: 0;
  width: 76rpx;
  height: 76rpx;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 26rpx;
  font-weight: 900;
}

.composer-image {
  background: #eef3ef;
  color: #3d5f4d;
}

.composer-input {
  flex: 1;
  height: 76rpx;
  padding: 0 24rpx;
  border-radius: 999rpx;
  background: #f3f6f4;
  font-size: 26rpx;
}

.composer-send {
  width: 92rpx;
  background: linear-gradient(135deg, #24573d, #3e885d);
  color: #fff;
}

.scroll-anchor {
  height: 2rpx;
}

.chat-empty {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
}

.chat-empty-title {
  font-size: 30rpx;
  color: #6a7d6d;
}
</style>
