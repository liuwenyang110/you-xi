import { defineStore } from 'pinia'
import {
  createCollabSession,
  fetchCollabMessages,
  fetchCollabSessions,
  sendCollabMessage,
  updateCollabStatus
} from '../api/collab'

const QUICK_REPLIES = [
  '我这边马上出发',
  '请把具体位置再发我一次',
  '我预计半小时内到',
  '已经到村口了',
  '作业完成后请确认一下'
]

function sortSessions(sessions: any[]) {
  return [...sessions].sort((left: any, right: any) => {
    const leftTime = new Date(left?.lastMessageAt || 0).getTime()
    const rightTime = new Date(right?.lastMessageAt || 0).getTime()
    if (rightTime !== leftTime) {
      return rightTime - leftTime
    }
    return Number(right?.id || 0) - Number(left?.id || 0)
  })
}

export const useCollabStore = defineStore('collab', {
  state: () => ({
    sessionList: [] as any[],
    activeSession: null as any,
    messagePages: {} as Record<string, any[]>,
    unreadCounters: {} as Record<string, number>,
    syncStatus: 'idle',
    quickReplies: QUICK_REPLIES,
    pollerId: null as ReturnType<typeof setInterval> | null
  }),
  getters: {
    activeMessages(state) {
      if (!state.activeSession?.id) return []
      return state.messagePages[String(state.activeSession.id)] || []
    }
  },
  actions: {
    patchSession(session: any) {
      const key = String(session.id)
      const existing = this.sessionList.find((item: any) => String(item.id) === key)
      const nextSession = {
        ...(existing || {}),
        ...session
      }
      const remaining = this.sessionList.filter((item: any) => String(item.id) !== key)
      this.sessionList = sortSessions([nextSession, ...remaining])
      this.unreadCounters[key] = Number(nextSession.unreadCount || 0)
      if (this.activeSession && String(this.activeSession.id) === key) {
        this.activeSession = nextSession
      }
      return nextSession
    },
    async loadSessions() {
      this.syncStatus = 'syncing'
      try {
        const sessions = await fetchCollabSessions()
        this.sessionList = sortSessions(Array.isArray(sessions) ? sessions : [])
        this.unreadCounters = this.sessionList.reduce((acc: Record<string, number>, item: any) => {
          acc[String(item.id)] = Number(item.unreadCount || 0)
          return acc
        }, {})
        if (this.activeSession?.id) {
          const latest = this.sessionList.find((item: any) => String(item.id) === String(this.activeSession.id))
          if (latest) {
            this.activeSession = latest
          }
        }
        this.syncStatus = 'ready'
      } catch (error) {
        this.syncStatus = 'error'
        throw error
      }
    },
    async openSession(session: any) {
      this.activeSession = this.patchSession({ ...session, unreadCount: 0 })
      await this.loadMessages(session.id)
      this.unreadCounters[String(session.id)] = 0
      this.startPolling(session.id)
    },
    async createSession(payload: any) {
      const session = await createCollabSession(payload)
      this.patchSession(session)
      await this.loadSessions()
      await this.loadMessages(session.id)
      return session
    },
    async loadMessages(sessionId: number | string) {
      this.syncStatus = 'syncing'
      try {
        const messages = await fetchCollabMessages(sessionId)
        this.messagePages[String(sessionId)] = Array.isArray(messages) ? messages : []
        this.syncStatus = 'ready'
      } catch (error) {
        this.syncStatus = 'error'
        throw error
      }
    },
    async postMessage(sessionId: number | string, payload: any) {
      const message = await sendCollabMessage(sessionId, payload)
      const key = String(sessionId)
      this.messagePages[key] = [...(this.messagePages[key] || []), message]
      this.patchSession({
        id: sessionId,
        lastMessagePreview: message.content || '[新消息]',
        lastMessageAt: message.createdAt,
        unreadCount: 0
      })
      return message
    },
    async changeStatus(sessionId: number | string, status: string) {
      const session = await updateCollabStatus(sessionId, status)
      this.patchSession(session)
      await this.loadSessions()
      await this.loadMessages(sessionId)
      return session
    },
    startPolling(sessionId: number | string) {
      this.stopPolling()
      this.pollerId = setInterval(async () => {
        try {
          await this.loadMessages(sessionId)
          await this.loadSessions()
        } catch (error) {
          this.syncStatus = 'error'
        }
      }, 5000)
    },
    stopPolling() {
      if (this.pollerId) {
        clearInterval(this.pollerId)
        this.pollerId = null
      }
    }
  }
})
