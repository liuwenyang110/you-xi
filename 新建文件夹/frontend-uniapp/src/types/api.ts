// API 请求/响应通用类型
export interface ApiResponse<T = any> {
  code: number
  message: string
  data: T
}

// 需求相关类型
export interface Demand {
  id: number
  villageName: string
  cropCode: string
  areaMu: number
  scheduleType: string
  status: string
  createdAt: string
  updatedAt: string
  // 其他字段根据实际API补充
}

export interface DemandCreateParams {
  villageName: string
  cropCode: string
  areaMu: number
  scheduleType: string
  // 其他字段
}

// 订单相关类型
export interface Order {
  id: number
  demandId: number
  ownerId: number
  status: string
  contactSessionId: number
  createdAt: string
  updatedAt: string
}

// 设备相关类型
export interface Equipment {
  id: number
  equipmentName: string
  brandModel: string
  currentStatus: string
  approveStatus: string
  currentLat: number | null
  currentLng: number | null
  geoTip?: string
}

export interface EquipmentUpdateParams {
  equipmentName?: string
  brandModel?: string
  currentLat?: number
  currentLng?: number
}

// 服务项目类型
export interface ServiceItem {
  id: number
  serviceName: string
  cropTags: string
  terrainTags: string
  plotTags: string
}

// 调度任务类型
export interface DispatchTask {
  id: number
  demandId: number
  equipmentId: number
  status: string
}

// 用户类型
export interface UserProfile {
  id: number
  phone: string
  role: 'FARMER' | 'OWNER' | 'ADMIN'
}