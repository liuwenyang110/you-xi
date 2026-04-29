import type {
  BackendCommunityPost,
  BackendContactSession,
  CollabMessage,
  CollabSessionStatus
} from '@/types/collab'

export const COLLAB_DISCLAIMER = '公益协作仅用于信息对接，平台不参与交易，请注意保护隐私。'

export const COLLAB_QUICK_REPLIES = [
  '收到，我先看一下地况。',
  '我在路上了，到了给您发消息。',
  '平台只负责公益对接，费用和时间请双方自行确认。',
  '辛苦了，麻烦到地头后再联系我。'
]

export const COMMUNITY_TABS = [
  { key: 'nearby', label: '附近' },
  { key: 'machinery', label: '找机' },
  { key: 'mutualAid', label: '互助圈' },
  { key: 'notice', label: '信息墙' }
] as const

export function clipText(value: string, maxLength = 88): string {
  const text = normalizeText(value)
  if (text.length <= maxLength) {
    return text
  }
  return `${text.slice(0, maxLength)}...`
}

export function normalizeText(value: string | number | null | undefined): string {
  return String(value ?? '').replace(/\s+/g, ' ').trim()
}

export function toSessionKey(id: string | number): string {
  return String(id)
}

export function formatRelativeTime(value?: string | null): string {
  if (!value) {
    return '刚刚'
  }

  const target = new Date(value).getTime()
  if (!target) {
    return '刚刚'
  }

  const diff = Date.now() - target
  const minute = 60 * 1000
  const hour = 60 * minute
  const day = 24 * hour

  if (diff < minute) {
    return '刚刚'
  }
  if (diff < hour) {
    return `${Math.max(1, Math.floor(diff / minute))}分钟前`
  }
  if (diff < day) {
    return `${Math.max(1, Math.floor(diff / hour))}小时前`
  }
  if (diff < day * 7) {
    return `${Math.max(1, Math.floor(diff / day))}天前`
  }

  const date = new Date(target)
  const month = `${date.getMonth() + 1}`.padStart(2, '0')
  const dayText = `${date.getDate()}`.padStart(2, '0')
  return `${month}-${dayText}`
}

export function sortByUpdatedAtDesc<T extends { updatedAt?: string | null }>(items: T[]): T[] {
  return [...items].sort((left, right) => {
    const leftTime = left.updatedAt ? new Date(left.updatedAt).getTime() : 0
    const rightTime = right.updatedAt ? new Date(right.updatedAt).getTime() : 0
    return rightTime - leftTime
  })
}

export function safeJsonArray(value?: string | null): string[] {
  if (!value) {
    return []
  }

  try {
    const parsed = JSON.parse(value)
    if (Array.isArray(parsed)) {
      return parsed.filter((item) => typeof item === 'string')
    }
  } catch (error) {
    return []
  }

  return []
}

export function getPostAvatarText(post: BackendCommunityPost): string {
  const type = normalizeText(post.postType).toUpperCase()
  if (type === 'RENTAL') {
    return '机'
  }
  if (type === 'CHAT') {
    return '助'
  }
  return '农'
}

export function getPostTagLabel(post: BackendCommunityPost): string {
  const type = normalizeText(post.postType).toUpperCase()
  if (type === 'RENTAL') {
    return '找机服务'
  }
  if (type === 'CHAT') {
    return '互助圈'
  }
  if (Number(post.isUrgent || 0) > 0) {
    return '紧急协作'
  }
  return '公益求助'
}

export function getPostStatusLabel(post: BackendCommunityPost): string {
  if (normalizeText(post.status).toUpperCase() === 'CLOSED') {
    return '已关闭'
  }
  if (Number(post.isUrgent || 0) > 0) {
    return '抢收优先'
  }
  return '等待响应'
}

export function matchFeedTab(post: BackendCommunityPost, tab: string): boolean {
  const type = normalizeText(post.postType).toUpperCase()
  if (tab === 'machinery') {
    return type === 'RENTAL' || type === 'DEMAND_URGENT'
  }
  if (tab === 'mutualAid') {
    return type === 'CHAT'
  }
  if (tab === 'notice') {
    return type !== 'RENTAL'
  }
  return true
}

export function getSessionStatusMeta(status: CollabSessionStatus) {
  const map: Record<CollabSessionStatus, { label: string; description: string; tone: string }> = {
    PUBLISHED: {
      label: '已发布',
      description: '需求已经发出，等待公益协作者响应。',
      tone: 'pending'
    },
    CLAIMED: {
      label: '已认领',
      description: '已有协作者接单，正在准备进一步沟通。',
      tone: 'active'
    },
    COMMUNICATING: {
      label: '沟通中',
      description: '双方正在确认时间、位置与地况。',
      tone: 'active'
    },
    ARRIVING: {
      label: '到场中',
      description: '协作者正在赶往现场，请保持通讯畅通。',
      tone: 'info'
    },
    IN_SERVICE: {
      label: '服务中',
      description: '现场服务进行中，完成后可提交反馈。',
      tone: 'success'
    },
    COMPLETED: {
      label: '已完成',
      description: '本次公益协作已完成，欢迎留下反馈。',
      tone: 'done'
    },
    CLOSED: {
      label: '已关闭',
      description: '会话已结束，后续不会再发送新消息。',
      tone: 'muted'
    }
  }

  return map[status]
}

export function mapBackendSessionStatus(session: BackendContactSession): CollabSessionStatus {
  const status = normalizeText(session.status).toUpperCase()
  const serviceCompleted = Boolean(Number(session.serviceCompleted || 0))
  const hasRating = Number(session.farmerRating || 0) > 0

  if (serviceCompleted || hasRating || status === 'FEEDBACK_GIVEN') {
    return 'COMPLETED'
  }
  if (status === 'SERVING' || status === 'CONTACT_ACTIVE' || status === 'CONFIRMED') {
    return status === 'SERVING' ? 'IN_SERVICE' : 'COMMUNICATING'
  }
  if (status === 'CONTACTED' || status === 'WAIT_FARMER_CONFIRM' || status === 'WAITING_FARMER_CONFIRM') {
    return 'CLAIMED'
  }
  if (status === 'CLOSED' || status === 'EXPIRED') {
    return 'CLOSED'
  }
  return 'PUBLISHED'
}

export function buildSeedMessages(sessionId: string, status: CollabSessionStatus, preview: string): CollabMessage[] {
  const now = new Date().toISOString()
  return [
    {
      id: `${sessionId}-seed-0`,
      sessionId,
      type: 'SYSTEM',
      sender: 'system',
      content: COLLAB_DISCLAIMER,
      createdAt: now
    },
    {
      id: `${sessionId}-seed-1`,
      sessionId,
      type: 'SYSTEM',
      sender: 'system',
      content: `${getSessionStatusMeta(status).label}：${preview || getSessionStatusMeta(status).description}`,
      createdAt: now
    }
  ]
}
