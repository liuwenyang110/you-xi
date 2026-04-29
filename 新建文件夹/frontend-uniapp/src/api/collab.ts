import { request } from './request'

export interface CollabSessionPayload {
  ownerId: number
  sourcePostId?: number | null
  demandId?: number | null
  discoverySource?: string
  subject?: string
  initialMessage?: string
}

export interface CollabMessagePayload {
  messageType?: string
  content?: string
  mediaUrl?: string | null
}

export function fetchCollabSessions() {
  return request('/collab/sessions')
}

export function createCollabSession(payload: CollabSessionPayload) {
  return request('/collab/sessions', {
    method: 'POST',
    data: payload
  })
}

export function fetchCollabMessages(sessionId: number | string) {
  return request(`/collab/sessions/${sessionId}/messages`)
}

export function sendCollabMessage(sessionId: number | string, payload: CollabMessagePayload) {
  return request(`/collab/sessions/${sessionId}/messages`, {
    method: 'POST',
    data: payload
  })
}

export function updateCollabStatus(sessionId: number | string, status: string) {
  return request(`/collab/sessions/${sessionId}/status`, {
    method: 'POST',
    data: { status }
  })
}

export function fetchCollabDashboard() {
  return request('/collab/dashboard/summary')
}
