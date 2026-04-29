/**
 * 通知中心 + 需求生命周期 API
 */
import request from './request'

// ============ 通知中心 ============

/** 获取我的通知列表 */
export function getNotifications(page = 1, size = 20) {
  return request.get('/api/v3/notification/list', { params: { page, size } })
}

/** 一键全部已读 */
export function markAllRead() {
  return request.post('/api/v3/notification/read-all')
}

/** 单条标记已读 */
export function markRead(id) {
  return request.post(`/api/v3/notification/read/${id}`)
}

/** 操作确认（活儿干完了等） */
export function confirmAction(id) {
  return request.post(`/api/v3/notification/action/${id}`)
}

// ============ 需求生命周期确认 ============

/** 确认活儿干完了 */
export function confirmDemandComplete(demandId) {
  return request.post(`/api/v3/lifecycle/demand/${demandId}/complete`)
}

/** 确认活儿还没完 */
export function confirmDemandOngoing(demandId) {
  return request.post(`/api/v3/lifecycle/demand/${demandId}/ongoing`)
}

// ============ 镇域雷达 ============

/** 获取镇域雷达气泡数据 */
export function getRadarData(townshipCode) {
  return request.get('/api/v3/lifecycle/radar', { params: { townshipCode } })
}
