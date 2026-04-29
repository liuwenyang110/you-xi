import { request } from './request'

export function fetchDashboard() {
  return request('/admin/dashboard')
}

export function fetchMatchTasks() {
  return request('/admin/match-tasks')
}

export function fetchDemands() {
  return request('/admin/demands')
}

export function fetchOrders() {
  return request('/admin/orders')
}

export function fetchUsers() {
  return request('/admin/users')
}

export function fetchEquipment() {
  return request('/admin/equipment')
}

export function fetchReports() {
  return request('/admin/reports')
}

export function fetchAudits() {
  return request('/admin/audits')
}

export function fetchCollabSummary() {
  return request('/admin/collab/summary')
}

export function fetchCollabSessions() {
  return request('/admin/collab/sessions')
}

export function fetchMapConfig() {
  return request('/debug/map/config')
}

export function fetchUnifiedEligible(demandId) {
  return request(`/debug/unified-match/${demandId}/eligible`)
}
