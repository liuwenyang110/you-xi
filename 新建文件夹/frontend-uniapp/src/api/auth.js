import { request } from './request'

export const authApi = {
  loginSms(data) {
    return request('/auth/login/sms', { method: 'POST', data })
  },
  switchUiMode(data) {
    return request('/auth/ui-mode/switch', { method: 'POST', data })
  },
  switchRole(data) {
    return request('/auth/role/switch', { method: 'POST', data })
  },
  me() {
    return request('/auth/me')
  },
  async hydrateSession() {
    const me = await request('/auth/me')
    return {
      userId: me.userId,
      phone: me.phone,
      role: me.currentRole || 'FARMER',
      uiMode: me.uiMode || 'NORMAL'
    }
  }
}
