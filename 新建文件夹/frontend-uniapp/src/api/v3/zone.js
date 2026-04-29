// V3 区划 & 片区 & 字典 API
import request from './request'

// 获取省级列表
export const getProvinces = () => request.get('/api/v3/regions/provinces')

// 获取下级区划（传 parentCode 获取市/县/乡）
export const getChildren = (parentCode) => request.get('/api/v3/regions', { params: { parentCode } })

// 获取片区列表
export const getZones = (params) => request.get('/api/v3/zones', { params })

// 片区详情（含农机数量统计）
export const getZoneDetail = (zoneId) => request.get(`/api/v3/zones/${zoneId}`)

// 片区农机分类统计（公开）
export const getZoneMachineryStats = (zoneId) => request.get(`/api/v3/zones/${zoneId}/machinery/stats`)

// 一次性获取所有字典（农机类型/作物品种/作业类型）
export const getAllDicts = () => request.get('/api/v3/dicts')

// 获取农机大类
export const getMachineCategories = () => request.get('/api/v3/dicts/machine-categories')

// 获取农机小类（可选按大类过滤）
export const getMachineTypes = (categoryId) => request.get('/api/v3/dicts/machine-types', { params: { categoryId } })

// 获取作物品种
export const getCrops = () => request.get('/api/v3/dicts/crops')

// 获取作业类型
export const getWorkTypes = (categoryId) => request.get('/api/v3/dicts/work-types', { params: { categoryId } })
