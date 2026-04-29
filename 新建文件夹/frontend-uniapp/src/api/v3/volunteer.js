import request from './request'

/** 认领帮扶需求 */
export function claimDemand(data) {
  return request({
    url: '/volunteer/claim',
    method: 'POST',
    data,
  })
}

/** 更新认领状态 (EN_ROUTE / WORKING / FINISHED / CANCELLED) */
export function updateClaimStatus(id, status) {
  return request({
    url: `/volunteer/claim/${id}/status`,
    method: 'PUT',
    params: { status },
  })
}

/** 提交农户反馈 */
export function submitFeedback(id, feedback, rating) {
  return request({
    url: `/volunteer/claim/${id}/feedback`,
    method: 'POST',
    params: { feedback, rating },
  })
}

/** 获取志愿者历史记录 */
export function getVolunteerHistory(volunteerId) {
  return request({
    url: '/volunteer/history',
    method: 'GET',
    params: { volunteerId },
  })
}

/** 根据需求ID获取认领状态 */
export function getClaimByDemand(demandId) {
  return request({
    url: `/volunteer/claim/by-demand/${demandId}`,
    method: 'GET',
  })
}
