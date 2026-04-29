import test from 'node:test'
import assert from 'node:assert/strict'

import {
  buildDashboardSummary,
  buildConversationGovernance,
  buildServiceQuality
} from './governance-models.mjs'

test('buildDashboardSummary derives public-interest metrics from legacy admin payloads', () => {
  const summary = buildDashboardSummary({
    dashboard: {
      demandCount: 18,
      matchSuccessCount: 12,
      completedOrderCount: 9,
      reportCount: 2
    },
    demands: [
      { id: 1, villageName: '青山镇东河村', status: 'PUBLISHED' },
      { id: 2, villageName: '青山镇西河村', status: 'MATCHING' },
      { id: 3, villageName: '白沙镇南坡村', status: 'COMPLETED' }
    ],
    users: [
      { id: 11, currentRole: 'VOLUNTEER' },
      { id: 12, currentRole: 'OWNER' }
    ],
    reports: [{ id: 1001 }, { id: 1002 }],
    orders: [{ id: 2001, status: 'COMPLETED' }]
  })

  assert.equal(summary.overviewCards[0].value, '18')
  assert.equal(summary.overviewCards[1].value, '12')
  assert.equal(summary.overviewCards[2].value, '75.0%')
  assert.equal(summary.overviewCards[3].value, '1')
  assert.equal(summary.overviewCards[4].value, '3')
  assert.equal(summary.riskSignals.openReports, 2)
})

test('buildConversationGovernance prioritizes reported and stalled collaboration chains', () => {
  const governance = buildConversationGovernance({
    matchTasks: [
      { id: 91, demandId: 501, ownerId: 8001, status: 'PENDING_RESPONSE', serviceName: '跨区收割' },
      { id: 92, demandId: 502, ownerId: 8002, status: 'CONTACT_OPENED', serviceName: '无人机植保' }
    ],
    orders: [
      { id: 301, demandId: 501, ownerId: 8001, farmerId: 9001, status: 'WAIT_NEGOTIATION' },
      { id: 302, demandId: 502, ownerId: 8002, farmerId: 9002, status: 'COMPLETED' }
    ],
    demands: [
      { id: 501, villageName: '青山镇东河村', status: 'MATCHING' },
      { id: 502, villageName: '白沙镇南坡村', status: 'COMPLETED' }
    ],
    reports: [
      { id: 7001, reportedUserId: 8001, reportType: 'ABUSE', status: 'OPEN' }
    ],
    audits: [
      { id: 9001, action: 'retry_match', content: '对需求 501 发起人工重试', createdAt: '2026-04-21T09:20:00' }
    ]
  })

  assert.equal(governance.summaryCards[0].value, '2')
  assert.equal(governance.summaryCards[1].value, '0')
  assert.equal(governance.summaryCards[2].value, '1')
  assert.equal(governance.summaryCards[3].value, '1')
  assert.equal(governance.priorityRows[0].demandId, 501)
  assert.equal(governance.priorityRows[0].riskLevel, 'high')
  assert.match(governance.priorityRows[0].riskReasons.join(','), /举报/)
})

test('buildServiceQuality groups repeated complaints by user and exposes unresolved burden', () => {
  const quality = buildServiceQuality({
    dashboard: {
      completedOrderCount: 6,
      reportCount: 3
    },
    orders: [
      { id: 401, ownerId: 7001, farmerId: 8001, status: 'COMPLETED' },
      { id: 402, ownerId: 7001, farmerId: 8002, status: 'COMPLETED' },
      { id: 403, ownerId: 7002, farmerId: 8003, status: 'SERVING' }
    ],
    reports: [
      { id: 1, reportedUserId: 7001, reportType: 'LATE', status: 'OPEN' },
      { id: 2, reportedUserId: 7001, reportType: 'ATTITUDE', status: 'OPEN' },
      { id: 3, reportedUserId: 7002, reportType: 'NO_SHOW', status: 'RESOLVED' }
    ],
    users: [
      { id: 7001, currentRole: 'OWNER', phone: '13800000001' },
      { id: 7002, currentRole: 'OWNER', phone: '13800000002' }
    ]
  })

  assert.equal(quality.summaryCards[0].value, '6')
  assert.equal(quality.summaryCards[1].value, '3')
  assert.equal(quality.summaryCards[2].value, '2')
  assert.equal(quality.summaryCards[3].value, '33.3%')
  assert.equal(quality.complaintRows[0].reportedUserId, 7001)
  assert.equal(quality.complaintRows[0].openCount, 2)
})
