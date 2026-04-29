const FALLBACK_RAW_DATA = {
  dashboard: {
    demandCount: 28,
    matchSuccessCount: 19,
    completedOrderCount: 14,
    reportCount: 4
  },
  collabSummary: {
    collabSessionCount: 0,
    collabMessageCount: 0,
    collabEventCount: 0,
    highRiskSessionCount: 0
  },
  collabSessions: [],
  matchTasks: [
    {
      id: 4101,
      demandId: 1201,
      ownerId: 5201,
      serviceName: '跨区收割支援',
      status: 'PENDING_RESPONSE',
      matchReason: '跨镇优先调度'
    },
    {
      id: 4102,
      demandId: 1202,
      ownerId: 5202,
      serviceName: '无人机植保',
      status: 'CONTACT_OPENED',
      matchReason: '已建立联系'
    },
    {
      id: 4103,
      demandId: 1203,
      ownerId: 5203,
      serviceName: '志愿抢收',
      status: 'WAIT_FARMER_CONFIRM',
      matchReason: '待农户确认'
    }
  ],
  demands: [
    { id: 1201, villageName: '青山镇东河村', status: 'MATCHING', cropCode: 'WHEAT', areaMu: 42, createdAt: '2026-04-21 08:30:00' },
    { id: 1202, villageName: '白沙镇南坝村', status: 'COMPLETED', cropCode: 'CORN', areaMu: 63, createdAt: '2026-04-21 09:10:00' },
    { id: 1203, villageName: '柳溪镇北沟村', status: 'PUBLISHED', cropCode: 'RICE', areaMu: 25, createdAt: '2026-04-21 10:05:00' },
    { id: 1204, villageName: '柳溪镇红星村', status: 'PUBLISHED', cropCode: 'RICE', areaMu: 18, createdAt: '2026-04-21 11:12:00' }
  ],
  orders: [
    { id: 6101, demandId: 1201, ownerId: 5201, farmerId: 3201, status: 'WAIT_NEGOTIATION', createdAt: '2026-04-21 08:42:00' },
    { id: 6102, demandId: 1202, ownerId: 5202, farmerId: 3202, status: 'COMPLETED', createdAt: '2026-04-21 09:30:00' },
    { id: 6103, demandId: 1203, ownerId: 5203, farmerId: 3203, status: 'SERVING', createdAt: '2026-04-21 10:15:00' }
  ],
  users: [
    { id: 5201, currentRole: 'OWNER', phone: '13800005201' },
    { id: 5202, currentRole: 'OWNER', phone: '13800005202' },
    { id: 5203, currentRole: 'VOLUNTEER', phone: '13800005203' },
    { id: 3201, currentRole: 'FARMER', phone: '13800003201' }
  ],
  reports: [
    { id: 7101, reportedUserId: 5201, reportType: 'ABUSE', status: 'OPEN' },
    { id: 7102, reportedUserId: 5201, reportType: 'PRICE', status: 'OPEN' },
    { id: 7103, reportedUserId: 5203, reportType: 'ATTITUDE', status: 'RESOLVED' },
    { id: 7104, reportedUserId: 3201, reportType: 'SPAM', status: 'OPEN' }
  ],
  audits: [
    { id: 8101, action: 'retry_match', content: '对需求 1201 发起人工重试', createdAt: '2026-04-21T09:20:00' },
    { id: 8102, action: 'content_hide', content: '下架需求 1204，原因：重复发布', createdAt: '2026-04-21T10:48:00' },
    { id: 8103, action: 'service_review', content: '人工回访订单 6103', createdAt: '2026-04-21T11:10:00' }
  ]
}

function numberValue(value) {
  const numeric = Number(value)
  return Number.isFinite(numeric) ? numeric : 0
}

function stringValue(value, fallback = '-') {
  return typeof value === 'string' && value.trim() ? value.trim() : fallback
}

function formatPercent(numerator, denominator) {
  if (!denominator) {
    return '0.0%'
  }

  return `${((numerator / denominator) * 100).toFixed(1)}%`
}

function formatDateTime(value) {
  if (!value) {
    return '待补充'
  }

  return String(value).replace('T', ' ').slice(0, 19)
}

function normalizeRole(user) {
  return stringValue(user?.currentRole || user?.primaryRole, 'UNKNOWN')
}

function normalizeReportStatus(report) {
  return stringValue(report?.status, 'OPEN').toUpperCase()
}

function deriveVillageCoverage(demands) {
  return new Set(demands.map((item) => stringValue(item?.villageName, `需求 ${item?.id ?? '未知'}`))).size
}

function deriveVolunteerCount(users) {
  return users.filter((item) => normalizeRole(item) === 'VOLUNTEER').length
}

function buildOverviewCards(raw) {
  const demandCount = numberValue(raw.dashboard?.demandCount || raw.demands.length)
  const responseCount = numberValue(raw.dashboard?.matchSuccessCount || raw.matchTasks.length || raw.collabSessions.length)
  const completedCount = numberValue(raw.dashboard?.completedOrderCount || raw.orders.filter((item) => item.status === 'COMPLETED').length)
  const volunteerCount = deriveVolunteerCount(raw.users)
  const coverageCount = deriveVillageCoverage(raw.demands)

  return [
    { key: 'help', label: '公益求助总量', value: String(demandCount), hint: '来自现有需求与公开内容入口', tone: 'green' },
    { key: 'response', label: '已响应协作链路', value: String(responseCount), hint: '兼容旧匹配与新协作会话', tone: 'blue' },
    { key: 'completion', label: '完成闭环率', value: formatPercent(completedCount, responseCount), hint: '已完成协作 / 已响应协作', tone: 'emerald' },
    { key: 'volunteer', label: '志愿参与人数', value: String(volunteerCount), hint: '当前识别为志愿角色的参与者', tone: 'orange' },
    { key: 'coverage', label: '覆盖村点', value: String(coverageCount), hint: '按已发布需求的村点去重', tone: 'slate' }
  ]
}

export function buildDashboardSummary(input = {}) {
  const raw = {
    ...createFallbackGovernanceSeed(),
    ...input
  }

  const openReports = raw.reports.filter((item) => normalizeReportStatus(item) !== 'RESOLVED').length
  const publishedCount = raw.demands.filter((item) => ['PUBLISHED', 'MATCHING'].includes(stringValue(item.status))).length
  const completedCount = numberValue(raw.dashboard?.completedOrderCount || raw.orders.filter((item) => item.status === 'COMPLETED').length)

  return {
    overviewCards: buildOverviewCards(raw),
    riskSignals: {
      openReports,
      activeDemands: publishedCount,
      completedOrders: completedCount,
      volunteerCount: deriveVolunteerCount(raw.users)
    },
    coverageRows: raw.demands.map((item) => ({
      id: item.id,
      villageName: stringValue(item.villageName, `需求 ${item.id}`),
      status: stringValue(item.status, 'UNKNOWN'),
      areaMu: numberValue(item.areaMu),
      createdAt: formatDateTime(item.createdAt || item.publishedAt)
    })),
    governanceAlerts: [
      {
        title: '平台坚持公益撮合',
        description: '仅提供信息联络和治理留痕，不参与交易定价与资金流转。',
        level: 'info'
      },
      {
        title: openReports > 0 ? '存在待处理举报' : '当前暂无待处理举报峰值',
        description: openReports > 0 ? `目前仍有 ${openReports} 条举报待跟进。` : '举报态势平稳，可重点巡检高频发帖和异常协作链路。',
        level: openReports > 0 ? 'warning' : 'success'
      }
    ]
  }
}

function buildLegacyConversationRows(raw) {
  return raw.matchTasks.map((task) => {
    const demand = raw.demands.find((item) => item.id === task.demandId) || {}
    const order = raw.orders.find((item) => item.demandId === task.demandId) || {}
    const relatedReports = raw.reports.filter((item) => {
      const reportedUserId = numberValue(item.reportedUserId)
      return reportedUserId && [numberValue(task.ownerId), numberValue(order.ownerId), numberValue(order.farmerId)].includes(reportedUserId)
    })
    const relatedAudits = raw.audits.filter((item) => String(item.content || '').includes(String(task.demandId)))
    const openReports = relatedReports.filter((item) => normalizeReportStatus(item) !== 'RESOLVED')
    const riskReasons = []

    if (openReports.length > 0) {
      riskReasons.push(`举报待处理 ${openReports.length} 条`)
    }

    if (['PENDING_RESPONSE', 'WAIT_FARMER_CONFIRM'].includes(stringValue(task.status))) {
      riskReasons.push('会话仍处于待响应状态')
    }

    if (stringValue(order.status) === 'WAIT_NEGOTIATION') {
      riskReasons.push('已进入协商但尚未闭环')
    }

    if (relatedAudits.some((item) => String(item.action || '').includes('retry'))) {
      riskReasons.push('出现人工重试记录')
    }

    let riskLevel = 'low'
    if (openReports.length > 0 || relatedAudits.some((item) => String(item.action || '').includes('retry'))) {
      riskLevel = 'high'
    } else if (riskReasons.length > 0) {
      riskLevel = 'medium'
    }

    return {
      taskId: task.id,
      demandId: task.demandId,
      orderId: order.id || null,
      villageName: stringValue(demand.villageName, '待补充村点'),
      serviceName: stringValue(task.serviceName, '公益协作服务'),
      taskStatus: stringValue(task.status, 'UNKNOWN'),
      orderStatus: stringValue(order.status, '未建单'),
      ownerId: numberValue(task.ownerId || order.ownerId),
      farmerId: numberValue(order.farmerId),
      reportCount: openReports.length,
      riskLevel,
      riskReasons,
      latestAuditAt: formatDateTime(relatedAudits[0]?.createdAt),
      latestAuditText: stringValue(relatedAudits[0]?.content, '暂无人工处置记录'),
      source: 'legacy'
    }
  })
}

function buildCollabConversationRows(raw) {
  return (raw.collabSessions || []).map((session) => {
    const unreadFarmerCount = numberValue(session.unreadFarmerCount)
    const unreadOwnerCount = numberValue(session.unreadOwnerCount)
    const riskReasons = []

    if (unreadFarmerCount >= 5 || unreadOwnerCount >= 5) {
      riskReasons.push('长时间未读消息堆积')
    }

    if (['CONTACTED', 'CLAIMED'].includes(stringValue(session.status, 'UNKNOWN'))) {
      riskReasons.push('协作仍停留在初始阶段')
    }

    return {
      taskId: `collab-${session.id}`,
      demandId: numberValue(session.demandId || session.sourcePostId || session.id),
      orderId: null,
      villageName: stringValue(session.summary || session.subject, '公益协作会话'),
      serviceName: stringValue(session.subject, '公益协作服务'),
      taskStatus: stringValue(session.status, 'UNKNOWN'),
      orderStatus: '协作会话',
      ownerId: 0,
      farmerId: 0,
      reportCount: 0,
      riskLevel: unreadFarmerCount >= 5 || unreadOwnerCount >= 5 ? 'high' : riskReasons.length > 0 ? 'medium' : 'low',
      riskReasons,
      latestAuditAt: formatDateTime(session.lastMessageAt),
      latestAuditText: stringValue(session.lastMessagePreview, '暂无最新消息'),
      source: 'collab'
    }
  })
}

function buildConversationRows(raw) {
  return buildLegacyConversationRows(raw).concat(buildCollabConversationRows(raw))
}

export function buildConversationGovernance(input = {}) {
  const raw = {
    ...createFallbackGovernanceSeed(),
    ...input
  }

  const priorityRows = buildConversationRows(raw).sort((left, right) => {
    const riskWeight = { high: 3, medium: 2, low: 1 }
    return riskWeight[right.riskLevel] - riskWeight[left.riskLevel]
  })

  const highRiskCount = priorityRows.filter((item) => item.riskLevel === 'high').length
  const followUpCount = priorityRows.filter((item) => ['high', 'medium'].includes(item.riskLevel)).length
  const closedLoopCount = priorityRows.filter((item) => ['COMPLETED', 'CLOSED'].includes(stringValue(item.taskStatus))).length
  const collabCount = numberValue(raw.collabSummary?.collabSessionCount || raw.collabSessions?.length)

  return {
    summaryCards: [
      { label: '协作链路总数', value: String(priorityRows.length), hint: '兼容旧匹配任务与新协作会话' },
      { label: '新协作会话', value: String(collabCount), hint: '来自公益协作中枢的新域数据' },
      { label: '高风险会话', value: String(highRiskCount), hint: '未读堆积、重试、待响应优先介入' },
      { label: '待人工跟进', value: String(followUpCount), hint: '当前建议运营介入的协作链路' },
      { label: '已闭环链路', value: String(closedLoopCount), hint: '状态已完成或已关闭的协作链路' }
    ],
    priorityRows,
    auditRows: raw.audits.map((item) => ({
      id: item.id,
      action: stringValue(item.action, 'audit'),
      content: stringValue(item.content),
      createdAt: formatDateTime(item.createdAt)
    }))
  }
}

export function buildContentGovernance(input = {}) {
  const raw = {
    ...createFallbackGovernanceSeed(),
    ...input
  }

  const contentRows = raw.demands.map((item) => {
    const relatedReports = raw.reports.filter((report) => String(report.reportedUserId || '').includes(String(item.farmerId || '')))
    const auditHits = raw.audits.filter((audit) => String(audit.content || '').includes(String(item.id)))

    return {
      id: item.id,
      villageName: stringValue(item.villageName, `需求 ${item.id}`),
      cropCode: stringValue(item.cropCode, 'UNKNOWN'),
      status: stringValue(item.status, 'UNKNOWN'),
      areaMu: numberValue(item.areaMu),
      reportCount: relatedReports.length,
      auditHint: stringValue(auditHits[0]?.content, '暂无治理动作'),
      createdAt: formatDateTime(item.createdAt || item.publishedAt)
    }
  })

  const openReports = raw.reports.filter((item) => normalizeReportStatus(item) !== 'RESOLVED').length
  const hiddenContent = raw.audits.filter((item) => String(item.action || '').includes('content')).length

  return {
    summaryCards: [
      { label: '公开内容总量', value: String(contentRows.length), hint: '当前以需求公开内容兼容展示' },
      { label: '待审风险内容', value: String(openReports), hint: '依据举报与治理信号估算' },
      { label: '人工下架记录', value: String(hiddenContent), hint: '来自管理员审计日志' },
      { label: '进行中求助', value: String(raw.demands.filter((item) => ['PUBLISHED', 'MATCHING'].includes(stringValue(item.status))).length), hint: '建议重点巡检的公开内容' }
    ],
    contentRows,
    reportRows: raw.reports.map((item) => ({
      id: item.id,
      reportedUserId: numberValue(item.reportedUserId),
      reportType: stringValue(item.reportType, 'UNKNOWN'),
      status: normalizeReportStatus(item)
    })),
    moderationRows: raw.audits.map((item) => ({
      id: item.id,
      action: stringValue(item.action, 'audit'),
      content: stringValue(item.content),
      createdAt: formatDateTime(item.createdAt)
    }))
  }
}

export function buildServiceQuality(input = {}) {
  const raw = {
    ...createFallbackGovernanceSeed(),
    ...input
  }

  const reportGroups = raw.reports.reduce((accumulator, item) => {
    const userId = numberValue(item.reportedUserId)

    if (!userId) {
      return accumulator
    }

    if (!accumulator.has(userId)) {
      accumulator.set(userId, [])
    }

    accumulator.get(userId).push(item)
    return accumulator
  }, new Map())

  const complaintRows = Array.from(reportGroups.entries())
    .map(([reportedUserId, reports]) => {
      const user = raw.users.find((item) => numberValue(item.id) === reportedUserId) || {}
      const openCount = reports.filter((item) => normalizeReportStatus(item) !== 'RESOLVED').length

      return {
        reportedUserId,
        role: normalizeRole(user),
        phone: stringValue(user.phone, '待补充'),
        totalCount: reports.length,
        openCount,
        reportTypes: reports.map((item) => stringValue(item.reportType, 'UNKNOWN')),
        priority: openCount >= 2 ? 'high' : openCount === 1 ? 'medium' : 'low'
      }
    })
    .sort((left, right) => right.openCount - left.openCount || right.totalCount - left.totalCount)

  const completedOrderCount = numberValue(raw.dashboard?.completedOrderCount || raw.orders.filter((item) => stringValue(item.status) === 'COMPLETED').length)
  const reportCount = numberValue(raw.dashboard?.reportCount || raw.reports.length)
  const openReportCount = raw.reports.filter((item) => normalizeReportStatus(item) !== 'RESOLVED').length

  return {
    summaryCards: [
      { label: '已完成服务', value: String(completedOrderCount), hint: '当前兼容已完成订单总数' },
      { label: '累计投诉记录', value: String(reportCount), hint: '举报/投诉数据总量' },
      { label: '未结投诉', value: String(openReportCount), hint: '仍需跟进闭环的投诉' },
      { label: '投诉负载率', value: formatPercent(openReportCount, completedOrderCount), hint: '未结投诉 / 已完成服务' }
    ],
    complaintRows,
    qualityRows: raw.orders.map((item) => ({
      id: item.id,
      demandId: item.demandId,
      ownerId: numberValue(item.ownerId),
      farmerId: numberValue(item.farmerId),
      status: stringValue(item.status, 'UNKNOWN')
    }))
  }
}

export function createFallbackGovernanceSeed() {
  return JSON.parse(JSON.stringify(FALLBACK_RAW_DATA))
}
