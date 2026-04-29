<template>
  <view class="chat-page">
    <view class="chat-header safe-area-top">
      <text class="chat-title">{{ activeSession?.subject || '公益协作会话' }}</text>
      <text class="chat-subtitle">{{ activeSession?.publicNotice }}</text>
    </view>

    <scroll-view class="message-list" scroll-y :scroll-top="scrollTop">
      <view v-for="item in activeMessages" :key="item.id" class="message-item" :class="{ mine: !item.fromCounterpart && !item.systemFlag, system: item.systemFlag }">
        <view v-if="item.systemFlag" class="system-bubble">{{ item.content }}</view>
        <view v-else class="bubble">{{ item.content || '[媒体消息]' }}</view>
      </view>
    </scroll-view>

    <view class="quick-replies">
      <text v-for="reply in quickReplies" :key="reply" class="quick-reply" @click="useQuickReply(reply)">{{ reply }}</text>
    </view>

    <view class="toolbar">
      <input v-model="draft" class="input" placeholder="发送协作消息…" confirm-type="send" @confirm="submitMessage" />
      <view class="action status" @click="markDone">完成</view>
      <view class="action send" @click="submitMessage">发送</view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { computed, onMounted, onUnmounted, ref } from 'vue'
import { useCollabStore } from '../../stores/collab'

const collabStore = useCollabStore()
const draft = ref('')
const scrollTop = ref(99999)
const activeSession = computed(() => collabStore.activeSession)
const activeMessages = computed(() => collabStore.activeMessages)
const quickReplies = computed(() => collabStore.quickReplies)

onMounted(async () => {
  const pages = getCurrentPages()
  const page = pages[pages.length - 1] as any
  const id = page?.options?.id
  if (id) {
    const target = collabStore.sessionList.find((item: any) => String(item.id) === String(id))
    if (target) {
      await collabStore.openSession(target)
    } else {
      await collabStore.loadSessions()
      const reloaded = collabStore.sessionList.find((item: any) => String(item.id) === String(id))
      if (reloaded) {
        await collabStore.openSession(reloaded)
      }
    }
    scrollTop.value = 99999
  }
})

onUnmounted(() => {
  collabStore.stopPolling()
})

async function submitMessage() {
  if (!activeSession.value?.id || !draft.value.trim()) return
  await collabStore.postMessage(activeSession.value.id, {
    messageType: 'TEXT',
    content: draft.value.trim()
  })
  draft.value = ''
  scrollTop.value = 99999
}

function useQuickReply(text: string) {
  draft.value = text
}

async function markDone() {
  if (!activeSession.value?.id) return
  await collabStore.changeStatus(activeSession.value.id, 'COMPLETED')
  uni.showToast({ title: '已标记完成', icon: 'none' })
}
</script>

<style scoped>
.chat-page {
  min-height: 100vh;
  background: #efeae2;
  display: flex;
  flex-direction: column;
}
.chat-header {
  padding: 28rpx 24rpx 18rpx;
  background: rgba(255, 255, 255, 0.88);
  backdrop-filter: blur(18px);
}
.chat-title {
  display: block;
  font-size: 32rpx;
  font-weight: 700;
  color: #21352c;
}
.chat-subtitle {
  display: block;
  margin-top: 8rpx;
  font-size: 22rpx;
  color: #738579;
}
.message-list {
  flex: 1;
  padding: 24rpx;
}
.message-item {
  display: flex;
  margin-bottom: 18rpx;
}
.message-item.mine {
  justify-content: flex-end;
}
.message-item.system {
  justify-content: center;
}
.bubble,
.system-bubble {
  max-width: 72%;
  border-radius: 22rpx;
  padding: 18rpx 20rpx;
  font-size: 26rpx;
  line-height: 1.6;
}
.bubble {
  background: #fff;
  color: #20362d;
}
.message-item.mine .bubble {
  background: #95ec69;
}
.system-bubble {
  background: rgba(32, 54, 45, 0.08);
  color: #607368;
  font-size: 22rpx;
}
.quick-replies {
  display: flex;
  gap: 12rpx;
  overflow-x: auto;
  padding: 12rpx 20rpx;
  background: rgba(255, 255, 255, 0.78);
}
.quick-reply {
  white-space: nowrap;
  padding: 10rpx 18rpx;
  border-radius: 999rpx;
  background: #edf3ef;
  color: #345546;
  font-size: 22rpx;
}
.toolbar {
  display: flex;
  align-items: center;
  gap: 12rpx;
  padding: 16rpx 20rpx calc(16rpx + env(safe-area-inset-bottom));
  background: #f7f7f7;
}
.input {
  flex: 1;
  min-height: 76rpx;
  background: #fff;
  border-radius: 18rpx;
  padding: 0 20rpx;
}
.action {
  padding: 16rpx 22rpx;
  border-radius: 18rpx;
  color: #fff;
  font-size: 24rpx;
}
.action.status {
  background: #607d6f;
}
.action.send {
  background: #1f8f5f;
}
</style>
