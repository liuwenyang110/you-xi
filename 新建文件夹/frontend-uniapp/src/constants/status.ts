type StatusTone = 'pending' | 'active' | 'success' | 'closed' | 'neutral' | 'info' | 'warn'

type StatusMeta = {
  label: string
  description: string
  tone: StatusTone
}

function createStatusHelpers<T extends Record<string, StatusMeta>>(dict: T) {
  const getMeta = (status?: string | null): StatusMeta => {
    const key = String(status || '').trim().toUpperCase()
    return dict[key as keyof T] || {
      label: key || '未知状态',
      description: '状态已更新，请以下一步提示为准。',
      tone: 'neutral'
    }
  }

  return {
    getMeta,
    getLabel: (status?: string | null) => getMeta(status).label,
    getDescription: (status?: string | null) => getMeta(status).description,
    getTone: (status?: string | null) => getMeta(status).tone
  }
}

export const EQUIPMENT_STATUS_DICT = {
  IDLE: { label: '空闲中', description: '设备可接单，可正常参与匹配。', tone: 'success' },
  BUSY: { label: '作业中', description: '设备正在服务，暂不参与新任务。', tone: 'active' },
  FAULT: { label: '故障中', description: '设备存在故障，暂不建议接单。', tone: 'closed' },
  PAUSED: { label: '已暂停', description: '设备已暂停接单，请检查排班和状态。', tone: 'neutral' }
} as const

export type EquipmentStatus = keyof typeof EQUIPMENT_STATUS_DICT

export const APPROVE_STATUS_DICT = {
  PENDING: { label: '待审核', description: '资料已提交，请等待平台审核。', tone: 'pending' },
  PASSED: { label: '已通过', description: '资料审核通过，可正常使用。', tone: 'success' },
  REJECTED: { label: '未通过', description: '资料未通过审核，请按提示补充。', tone: 'closed' }
} as const

export type ApproveStatus = keyof typeof APPROVE_STATUS_DICT

export const DEMAND_STATUS_DICT = {
  // 【寻机阶段】
  PUBLISHED:                    { label: '正在找机手', description: '需求已发出去，稍等一会儿就有机手接单。', tone: 'info' },
  MATCHING:                     { label: '正在找机手', description: '我们正在帮您找附近的机手，稍等片刻。', tone: 'pending' },
  // 【路上阶段】
  WAIT_FARMER_CONTACT_CONFIRM:  { label: '师傅接单了！', description: '有师傅要来了，点这里确认联系，获取电话。', tone: 'active' },
  WAITING_FARMER_CONFIRM:       { label: '师傅接单了！', description: '有师傅要来了，点这里确认联系，获取电话。', tone: 'active' },
  NEGOTIATING:                  { label: '和师傅商量中', description: '已拿到师傅电话，打电话商量好时间和价格。', tone: 'active' },
  // 【作业阶段】
  IN_SERVICE:                   { label: '师傅正在干活', description: '机手已下地作业，活干完了来这里确认。', tone: 'active' },
  SERVING:                      { label: '师傅正在干活', description: '机手已下地作业，活干完了来这里确认。', tone: 'active' },
  // 【完成阶段】
  FINISHED_PENDING_CONFIRM:     { label: '活干完了，确认一下', description: '师傅说完活了，点这里确认，平台会把这次服务记为完成。', tone: 'warn' },
  // 结束状态
  MATCH_FAILED:                 { label: '没找到机手', description: '这次没找到，可以重新发一次需求再等等。', tone: 'closed' },
  COMPLETED:                    { label: '服务完成', description: '这次服务圆满完成！可以再发需求叫机手。', tone: 'success' },
  CANCELLED:                    { label: '已取消', description: '需求已取消，可以重新发布。', tone: 'closed' }
} as const

export type DemandStatus = keyof typeof DEMAND_STATUS_DICT

export const ORDER_STATUS_DICT = {
  // 【路上阶段】
  WAIT_NEGOTIATION:         { label: '和师傅商量中', description: '已拿到师傅电话，打电话商量好时间和价钱。', tone: 'pending' },
  PENDING_CONTACT:          { label: '等候师傅到来', description: '订单已建立，师傅正往您这边赶。', tone: 'pending' },
  // 【作业阶段】
  SERVING:                  { label: '师傅正在干活', description: '机手正在地里作业，完工后记得来确认。', tone: 'active' },
  // 【完成阶段】
  FINISHED_PENDING_CONFIRM: { label: '活干完了，确认一下', description: '师傅说完活了，请您确认一下，平台会把这次服务记为完成。', tone: 'warn' },
  COMPLETED:                { label: '服务完成', description: '这次服务已全部完成，感谢使用农助手！', tone: 'success' },
  // 异常状态
  FAILED:                   { label: '订单出问题了', description: '这次没完成，如有疑问请联系平台客服。', tone: 'closed' },
  CANCELLED:                { label: '已取消', description: '订单已取消。', tone: 'closed' }
} as const

export type OrderStatus = keyof typeof ORDER_STATUS_DICT

export const OWNER_MATCH_STATUS_DICT = {
  PENDING_RESPONSE: { label: '待响应', description: '请尽快决定是否接单，避免错过机会。', tone: 'warn' },
  WAIT_FARMER_CONFIRM: { label: '待农户确认', description: '你已响应，等待农户确认联系。', tone: 'info' },
  WAITING_FARMER_CONFIRM: { label: '待农户确认', description: '你已响应，等待农户确认联系。', tone: 'info' },
  CONTACT_OPENED: { label: '已建立联系', description: '双方已建立联系，可继续推进订单。', tone: 'success' },
  OWNER_TIMEOUT: { label: '已超时', description: '本次响应超时结束。', tone: 'closed' },
  OWNER_REJECTED: { label: '已拒绝', description: '你已拒绝本次派单。', tone: 'closed' },
  FARMER_REJECTED: { label: '农户暂不联系', description: '农户本轮未确认联系，系统会继续处理。', tone: 'closed' },
  FARMER_TIMEOUT: { label: '农户确认超时', description: '农户未在规定时间内确认联系。', tone: 'closed' },
  DEMAND_CANCELLED: { label: '需求已取消', description: '农户已取消需求，本次派单结束。', tone: 'closed' }
} as const

const equipmentHelpers = createStatusHelpers(EQUIPMENT_STATUS_DICT)
const approveHelpers = createStatusHelpers(APPROVE_STATUS_DICT)
const demandHelpers = createStatusHelpers(DEMAND_STATUS_DICT)
const orderHelpers = createStatusHelpers(ORDER_STATUS_DICT)
const ownerMatchHelpers = createStatusHelpers(OWNER_MATCH_STATUS_DICT)

export function getEquipmentStatusLabel(status: EquipmentStatus | string): string {
  return equipmentHelpers.getLabel(status)
}

export function getEquipmentStatusDescription(status: EquipmentStatus | string): string {
  return equipmentHelpers.getDescription(status)
}

export function getEquipmentStatusTone(status: EquipmentStatus | string): StatusTone {
  return equipmentHelpers.getTone(status)
}

export function getApproveStatusLabel(status: ApproveStatus | string): string {
  return approveHelpers.getLabel(status)
}

export function getApproveStatusDescription(status: ApproveStatus | string): string {
  return approveHelpers.getDescription(status)
}

export function getApproveStatusTone(status: ApproveStatus | string): StatusTone {
  return approveHelpers.getTone(status)
}

export function normalizeDemandStatus(status?: string | null): string {
  const key = String(status || '').trim().toUpperCase()
  if (key === 'WAITING_FARMER_CONFIRM') return 'WAIT_FARMER_CONTACT_CONFIRM'
  if (key === 'IN_SERVICE') return 'SERVING'
  return key
}

export function getDemandStatusMeta(status?: DemandStatus | string | null): StatusMeta {
  return demandHelpers.getMeta(normalizeDemandStatus(status))
}

export function getDemandStatusLabel(status: DemandStatus | string): string {
  return getDemandStatusMeta(status).label
}

export function getDemandStatusDescription(status: DemandStatus | string): string {
  return getDemandStatusMeta(status).description
}

export function getDemandStatusTone(status: DemandStatus | string): StatusTone {
  return getDemandStatusMeta(status).tone
}

export function getOrderStatusMeta(status?: OrderStatus | string | null): StatusMeta {
  return orderHelpers.getMeta(status)
}

export function getOrderStatusLabel(status: OrderStatus | string): string {
  return getOrderStatusMeta(status).label
}

export function getOrderStatusDescription(status: OrderStatus | string): string {
  return getOrderStatusMeta(status).description
}

export function getOrderStatusTone(status: OrderStatus | string): StatusTone {
  return getOrderStatusMeta(status).tone
}

export function getOwnerMatchStatusMeta(status?: string | null): StatusMeta {
  return ownerMatchHelpers.getMeta(status)
}

export function getOwnerMatchStatusLabel(status?: string | null): string {
  return getOwnerMatchStatusMeta(status).label
}

export function getOwnerMatchStatusDescription(status?: string | null): string {
  return getOwnerMatchStatusMeta(status).description
}

export function getOwnerMatchStatusTone(status?: string | null): StatusTone {
  return getOwnerMatchStatusMeta(status).tone
}
