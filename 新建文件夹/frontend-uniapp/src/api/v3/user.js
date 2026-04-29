// V3 用户 API
import request from './request'

// 选择角色（一次性）
export const selectRole = (data) => request.post('/api/v3/user/role', data)

// 加入/切换片区
export const joinZone = (data) => request.post('/api/v3/user/zone', data)

// 获取当前 V3 用户档案
export const getV3Profile = () => request.get('/api/v3/user/profile')

// 获取本片区农机手列表（仅本片区农户可调用）
export const getZoneOperators = (zoneId) => request.get(`/api/v3/zones/${zoneId}/operators`)
