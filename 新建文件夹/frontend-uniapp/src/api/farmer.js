import { request } from './request'

export const farmerApi = {
  // === 需求管理 ===
  createDemand(data) {
    return request('/demands', { method: 'POST', data })
  },
  listDemands() {
    return request('/demands')
  },
  getDemand(id) {
    return request(`/demands/${id}`)
  },
  getMatchStatus(id) {
    return request(`/demands/${id}/match-status`)
  },
  cancelDemand(id) {
    return request(`/demands/${id}/cancel`, { method: 'POST' })
  },

  // === 联系对接（V10 核心） ===
  listContacts() {
    return request('/contact-sessions')
  },
  confirmContact(id) {
    return request(`/contact-sessions/${id}/confirm`, { method: 'POST' })
  },
  rejectContact(id) {
    return request(`/contact-sessions/${id}/reject`, { method: 'POST' })
  },
  /** V10 新增：农户主动发起联系（不经过匹配系统） */
  initiateContact(ownerId, serviceItemId, contactType = 'PHONE', discoverySource = 'BROWSE') {
    return request('/contact-sessions/initiate', {
      method: 'POST',
      data: { ownerId, serviceItemId, contactType, discoverySource }
    })
  },
  /** V10 新增：提交服务反馈与评价 */
  submitFeedback(id, feedback, rating) {
    return request(`/contact-sessions/${id}/feedback`, {
      method: 'POST',
      data: { feedback, rating }
    })
  },
  /** V10 新增：标记服务已完成 */
  markServiceCompleted(id) {
    return request(`/contact-sessions/${id}/complete`, { method: 'POST' })
  },

  // === 订单/联系记录（保持兼容） ===
  listOrders() {
    return request('/orders')
  },
  getOrder(id) {
    return request(`/orders/${id}`)
  },
  confirmFinish(id) {
    return request(`/orders/${id}/confirm-finish`, {
      method: 'POST',
      data: { actorRole: 'farmer' }
    })
  },

  // === V10 新增：隐私合规 ===
  /** 记录用户隐私同意 */
  recordPrivacyConsent(consentType, consentVersion = '1.0') {
    return request(`/privacy/consent?consentType=${consentType}&consentVersion=${consentVersion}`, {
      method: 'POST'
    })
  },
  /** 检查用户是否已同意隐私政策 */
  checkPrivacyConsent(consentType) {
    return request(`/privacy/consent/check?consentType=${consentType}`)
  }
}
