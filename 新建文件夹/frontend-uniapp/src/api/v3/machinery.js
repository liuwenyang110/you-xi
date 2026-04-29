// V3 农机 API
import request from './request'

// 农机手登记农机
export const registerMachinery = (data) => request.post('/api/v3/machinery', data)

// 更新农机信息
export const updateMachinery = (id, data) => request.put(`/api/v3/machinery/${id}`, data)

// 下架农机
export const deactivateMachinery = (id) => request.delete(`/api/v3/machinery/${id}`)

// 我的农机列表
export const myMachinery = () => request.get('/api/v3/machinery/my')

// 片区农机列表（本片区农户可见）
export const getZoneMachinery = (zoneId) => request.get(`/api/v3/zones/${zoneId}/machinery`)
