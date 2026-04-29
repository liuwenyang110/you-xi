const STATUS_DICTIONARIES = {
  demandStatus: {
    PUBLISHED: { label: '待匹配', tagType: 'warning', level: 'warning' },
    MATCHING: { label: '匹配中', tagType: 'primary', level: 'primary' },
    WAIT_FARMER_CONTACT_CONFIRM: { label: '待确认联系', tagType: 'warning', level: 'warning' },
    WAITING_FARMER_CONFIRM: { label: '待农户确认', tagType: 'warning', level: 'warning' },
    NEGOTIATING: { label: '协商中', tagType: 'primary', level: 'primary' },
    CONTACTING: { label: '联系中', tagType: 'primary', level: 'primary' },
    IN_SERVICE: { label: '服务中', tagType: 'success', level: 'success' },
    SERVING: { label: '服务中', tagType: 'success', level: 'success' },
    FINISHED_PENDING_CONFIRM: { label: '待完工确认', tagType: 'warning', level: 'warning' },
    MATCH_FAILED: { label: '匹配失败', tagType: 'danger', level: 'danger' },
    COMPLETED: { label: '已完成', tagType: 'success', level: 'success' },
    CANCELLED: { label: '已取消', tagType: 'info', level: 'info' },
    CLOSED: { label: '已关闭', tagType: 'info', level: 'info' }
  },
  orderStatus: {
    WAIT_NEGOTIATION: { label: '待协商', tagType: 'warning', level: 'warning' },
    PENDING_CONTACT: { label: '待联系', tagType: 'info', level: 'info' },
    SERVING: { label: '服务中', tagType: 'primary', level: 'primary' },
    FINISHED_PENDING_CONFIRM: { label: '待完工确认', tagType: 'warning', level: 'warning' },
    COMPLETED: { label: '已完成', tagType: 'success', level: 'success' },
    FAILED: { label: '已失败', tagType: 'danger', level: 'danger' },
    CANCELLED: { label: '已取消', tagType: 'info', level: 'info' }
  },
  userStatus: {
    ACTIVE: { label: '正常', tagType: 'success', level: 'success' },
    DISABLED: { label: '禁用', tagType: 'danger', level: 'danger' },
    PENDING: { label: '待审核', tagType: 'warning', level: 'warning' }
  },
  userRole: {
    FARMER: { label: '农户', tagType: 'success', level: 'success' },
    OWNER: { label: '农机主', tagType: 'primary', level: 'primary' },
    ADMIN: { label: '管理员', tagType: 'warning', level: 'warning' },
    OPERATOR: { label: '运营', tagType: 'info', level: 'info' }
  },
  equipmentStatus: {
    IDLE: { label: '空闲', tagType: 'success', level: 'success' },
    BUSY: { label: '作业中', tagType: 'warning', level: 'warning' },
    FAULT: { label: '故障中', tagType: 'danger', level: 'danger' },
    PAUSED: { label: '已暂停', tagType: 'info', level: 'info' }
  },
  approvalStatus: {
    PENDING: { label: '待审核', tagType: 'warning', level: 'warning' },
    PASSED: { label: '已通过', tagType: 'success', level: 'success' },
    REJECTED: { label: '未通过', tagType: 'danger', level: 'danger' }
  },
  reportStatus: {
    PENDING: { label: '待处理', tagType: 'warning', level: 'warning' },
    PROCESSING: { label: '处理中', tagType: 'primary', level: 'primary' },
    RESOLVED: { label: '已处理', tagType: 'success', level: 'success' },
    REJECTED: { label: '驳回', tagType: 'info', level: 'info' }
  },
  matchTaskStatus: {
    PENDING_RESPONSE: { label: '待机主响应', tagType: 'warning', level: 'warning' },
    OWNER_ACCEPTED: { label: '机主已接单', tagType: 'success', level: 'success' },
    WAIT_FARMER_CONFIRM: { label: '待农户确认', tagType: 'warning', level: 'warning' },
    WAITING_FARMER_CONFIRM: { label: '待农户确认', tagType: 'warning', level: 'warning' },
    CONTACT_OPENED: { label: '已建立联系', tagType: 'success', level: 'success' },
    OWNER_REJECTED: { label: '机主已拒绝', tagType: 'info', level: 'info' },
    FARMER_REJECTED: { label: '农户已拒绝', tagType: 'info', level: 'info' },
    OWNER_TIMEOUT: { label: '机主超时', tagType: 'danger', level: 'danger' },
    FARMER_TIMEOUT: { label: '农户超时', tagType: 'danger', level: 'danger' },
    DEMAND_CANCELLED: { label: '需求已取消', tagType: 'info', level: 'info' },
    EXPIRED: { label: '已过期', tagType: 'info', level: 'info' }
  }
}

const FALLBACK_META = {
  label: '未知状态',
  tagType: 'info',
  level: 'info'
}

export function getStatusMeta(dictionaryName, code) {
  if (!code) {
    return FALLBACK_META
  }

  const dictionary = STATUS_DICTIONARIES[dictionaryName] || {}
  return (
    dictionary[code] || {
      label: String(code),
      tagType: 'info',
      level: 'info'
    }
  )
}

export function getStatusLabel(dictionaryName, code) {
  return getStatusMeta(dictionaryName, code).label
}

export function getTagType(dictionaryName, code) {
  return getStatusMeta(dictionaryName, code).tagType
}

export function getOptions(dictionaryName) {
  const dictionary = STATUS_DICTIONARIES[dictionaryName] || {}

  return Object.entries(dictionary).map(([value, meta]) => ({
    value,
    label: meta.label
  }))
}

export function getCountSummary(list, field) {
  return list.reduce((summary, item) => {
    const key = item?.[field] || 'UNKNOWN'
    return {
      ...summary,
      [key]: (summary[key] || 0) + 1
    }
  }, {})
}
