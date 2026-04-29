import { request } from './request'

export const ownerApi = {
  pendingList() {
    return request('/dispatches/pending')
  },
  listMatchTasks() {
    return request('/match-tasks')
  },
  dispatchDetail(id) {
    return request(`/dispatches/${id}`)
  },
  accept(id) {
    return request(`/dispatches/${id}/accept`, { method: 'POST' })
  },
  reject(id) {
    return request(`/dispatches/${id}/reject`, { method: 'POST' })
  },
  listEquipment() {
    return request('/equipment')
  },
  updateEquipment(id, data) {
    return request(`/equipment/${id}`, { method: 'PUT', data })
  },
  listServiceItems() {
    return request('/service-items')
  }
}
