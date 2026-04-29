/**
 * 村级快闪茶馆 API
 */
import request from './request'

/** 获取茶馆状态 */
export function getTeahouseStatus(zoneId) {
  return request.get('/api/v3/teahouse/status', { params: { zoneId } })
}

/** 获取茶馆消息列表（分页） */
export function getTeahouseMessages(zoneId, page = 1, size = 20) {
  return request.get('/api/v3/teahouse/messages', { params: { zoneId, page, size } })
}

/** 发送文字消息 */
export function sendTeahouseMessage(zoneId, content, msgType = 'TEXT', mediaKey = null) {
  return request.post('/api/v3/teahouse/send', { zoneId, content, msgType, mediaKey })
}

/** 申请延期 */
export function extendTeahouse(zoneId, days) {
  return request.post('/api/v3/teahouse/extend', { zoneId, days })
}
