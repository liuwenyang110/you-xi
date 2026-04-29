import { request } from './request'

export const trackApi = {
  /** 上传轨迹点（机手端） */
  upload(data) {
    return request('/track/upload', { method: 'POST', data })
  },
  /** 生成面积报告 */
  generateReport(data) {
    return request('/track/report/generate', { method: 'POST', data })
  },
  /** 查询面积报告 */
  getReport(orderId) {
    return request(`/track/report/${orderId}`)
  }
}
