/**
 * 公益联系信息记录 API
 * 对应后端：ContactRevealController (/api/v3/contact-reveal)
 *
 * 平台使命：免费提供联系渠道，不参与任何商业交易
 */
import request from './request'

/**
 * 记录联系行为（农户点击"一键拨打"时调用）
 * @param {number} operatorId 服务者 ID
 * @param {string} source 联系来源（OPERATOR_DETAIL | ZONE_HOME | DEMAND_LIST）
 */
export function logContactReveal(operatorId, source = 'OPERATOR_DETAIL') {
  return request.post('/api/v3/contact-reveal/log', null, {
    params: { operatorId, source }
  })
}

/**
 * 获取今日联系次数（防骚扰友好提示）
 */
export function getTodayRevealCount() {
  return request.get('/api/v3/contact-reveal/today-count')
}

/**
 * 获取平台公益统计数据（首页实时展示用）
 * 返回：今日求助数、今日联系成功数、注册服务者数、覆盖村镇数
 */
export function getPlatformStats() {
  return request.get('/api/v3/contact-reveal/stats/platform')
}

/**
 * 获取片区公益对接统计（Admin 看板数据源）
 * @param {number} zoneId 片区 ID
 */
export function getZoneRevealStats(zoneId) {
  return request.get(`/api/v3/contact-reveal/stats/zone/${zoneId}`)
}
