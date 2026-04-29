// V3 需求 API
import request from './request'

// === 个人需求 ===

// 发布个人需求
export const publishDemand = (data) => request.post('/api/v3/demands', data)

// 我的需求列表
export const myDemands = () => request.get('/api/v3/demands/my')

// 更新需求状态（CONTACTED/COMPLETED/CANCELLED）
export const updateDemandStatus = (id, status) =>
  request.put(`/api/v3/demands/${id}/status`, null, { params: { status } })

// 片区需求列表（农机手查看）
export const getZoneDemands = (zoneId, page = 1, size = 20) =>
  request.get(`/api/v3/zones/${zoneId}/demands`, { params: { page, size } })

// === 合集需求 ===

// 发起合集需求
export const createGroup = (data) => request.post('/api/v3/demand-groups', data)

// 片区合集需求列表
export const getZoneGroups = (zoneId) => request.get(`/api/v3/zones/${zoneId}/demand-groups`)

// 合集需求详情
export const getGroupDetail = (id) => request.get(`/api/v3/demand-groups/${id}`)

// 申请加入合集
export const applyJoinGroup = (id, data) => request.post(`/api/v3/demand-groups/${id}/join`, data)

// 审批加入申请
export const approveJoin = (memberId, approved) =>
  request.put(`/api/v3/demand-groups/members/${memberId}/approve`, null, { params: { approved } })
