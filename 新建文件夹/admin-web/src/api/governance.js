import {
  fetchAudits,
  fetchCollabSessions as fetchLegacyCollabSessions,
  fetchCollabSummary as fetchLegacyCollabSummary,
  fetchDashboard,
  fetchDemands,
  fetchMatchTasks,
  fetchOrders,
  fetchReports,
  fetchUsers
} from './admin'
import {
  buildContentGovernance,
  buildConversationGovernance,
  buildDashboardSummary,
  buildServiceQuality,
  createFallbackGovernanceSeed
} from './governance-models.mjs'

async function settle(name, fetcher) {
  try {
    const data = await fetcher()
    return { name, ok: true, data }
  } catch (error) {
    return { name, ok: false, error }
  }
}

function toWarning(result) {
  return `${result.name} 接口不可用，已切换为安全示例数据。`
}

function mergeResults(results) {
  const fallback = createFallbackGovernanceSeed()
  const warnings = []
  const source = {}
  const merged = { ...fallback }

  results.forEach(result => {
    if (result.ok) {
      merged[result.name] = result.data
      source[result.name] = 'live'
      return
    }

    warnings.push(toWarning(result))
    source[result.name] = 'fallback'
  })

  return {
    raw: merged,
    warnings,
    source,
    mode: warnings.length === 0 ? 'live' : warnings.length === results.length ? 'fallback' : 'mixed'
  }
}

async function loadRawGovernanceData() {
  const results = await Promise.all([
    settle('dashboard', fetchDashboard),
    settle('collabSummary', fetchLegacyCollabSummary),
    settle('collabSessions', fetchLegacyCollabSessions),
    settle('matchTasks', fetchMatchTasks),
    settle('demands', fetchDemands),
    settle('orders', fetchOrders),
    settle('users', fetchUsers),
    settle('reports', fetchReports),
    settle('audits', fetchAudits)
  ])

  return mergeResults(results)
}

function withMeta(payload, transformed) {
  return {
    ...transformed,
    warnings: payload.warnings,
    source: payload.source,
    mode: payload.mode,
    updatedAt: new Date().toLocaleString('zh-CN', { hour12: false })
  }
}

export async function fetchGovernanceDashboard() {
  const payload = await loadRawGovernanceData()
  return withMeta(payload, buildDashboardSummary(payload.raw))
}

export async function fetchConversationGovernance() {
  const payload = await loadRawGovernanceData()
  return withMeta(payload, buildConversationGovernance(payload.raw))
}

export async function fetchContentGovernance() {
  const payload = await loadRawGovernanceData()
  return withMeta(payload, buildContentGovernance(payload.raw))
}

export async function fetchServiceQuality() {
  const payload = await loadRawGovernanceData()
  return withMeta(payload, buildServiceQuality(payload.raw))
}
