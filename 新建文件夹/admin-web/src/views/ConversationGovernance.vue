<template>
  <div class="governance-page">
    <section class="governance-hero">
      <div class="governance-hero__content">
        <span class="governance-hero__eyebrow">会话治理</span>
        <h1>公益协作链路风控台</h1>
        <p>
          当前视图同时纳入旧匹配链路和新协作会话，帮助运营优先识别长时间未响应、消息堆积、重复重试和举报风险。
        </p>
      </div>

      <div class="governance-hero__meta">
        <article class="governance-status-card">
          <span class="governance-status-card__label">高风险会话</span>
          <strong class="governance-status-card__value">{{ summaryValue(2) }}</strong>
          <span class="governance-source-note">建议优先人工介入</span>
        </article>
        <article class="governance-status-card">
          <span class="governance-status-card__label">跟进模式</span>
          <strong class="governance-status-card__value">{{ modeLabel }}</strong>
          <span class="governance-source-note">最近刷新：{{ state.updatedAt || '待加载' }}</span>
        </article>
      </div>
    </section>

    <section class="governance-toolbar">
      <div class="governance-toolbar__tags">
        <el-tag effect="plain">公益协作，不做泛社交 IM</el-tag>
        <el-tag :type="state.mode === 'live' ? 'success' : 'warning'">{{ modeLabel }}</el-tag>
      </div>
      <div class="governance-toolbar__actions">
        <el-button :loading="loading" type="primary" @click="loadData">刷新会话治理</el-button>
      </div>
    </section>

    <section v-if="state.warnings.length" class="governance-inline-alerts">
      <el-alert
        v-for="warning in state.warnings"
        :key="warning"
        :title="warning"
        type="warning"
        :closable="false"
        show-icon
      />
    </section>

    <section class="governance-summary-grid">
      <article v-for="card in state.summaryCards" :key="card.label" class="governance-summary-card">
        <span class="governance-summary-card__label">{{ card.label }}</span>
        <strong class="governance-summary-card__value">{{ card.value }}</strong>
        <span class="governance-summary-card__hint">{{ card.hint }}</span>
      </article>
    </section>

    <article class="governance-panel">
      <div class="governance-panel__head">
        <div>
          <h2>重点协作链路</h2>
          <p>按风险等级、消息堆积、举报与重试信号排序，帮助运营快速分派处置。</p>
        </div>
        <el-tag type="info">筛选后 {{ filteredRows.length }} 条</el-tag>
      </div>
      <div class="governance-panel__body">
        <div class="governance-filter">
          <el-input v-model="keyword" placeholder="搜索 demandId、村点、服务类型" clearable />
          <el-select v-model="riskFilter" placeholder="风险等级" clearable>
            <el-option label="高风险" value="high" />
            <el-option label="中风险" value="medium" />
            <el-option label="低风险" value="low" />
          </el-select>
          <el-select v-model="statusFilter" placeholder="协作状态" clearable>
            <el-option label="待响应" value="PENDING_RESPONSE" />
            <el-option label="待农户确认" value="WAIT_FARMER_CONFIRM" />
            <el-option label="已联系" value="CONTACTED" />
            <el-option label="已认领" value="CLAIMED" />
            <el-option label="协作中" value="IN_PROGRESS" />
            <el-option label="已完成" value="COMPLETED" />
            <el-option label="已关闭" value="CLOSED" />
          </el-select>
          <el-button @click="resetFilters">重置</el-button>
        </div>

        <el-empty
          v-if="!filteredRows.length"
          description="当前筛选条件下没有需要重点关注的协作链路"
          class="governance-empty"
        />
        <el-table v-else :data="filteredRows" stripe table-layout="auto">
          <el-table-column prop="demandId" label="Demand" width="100" />
          <el-table-column label="来源" width="120">
            <template #default="{ row }">
              <el-tag :type="row.source === 'collab' ? 'success' : 'info'">
                {{ row.source === 'collab' ? '新协作域' : '旧兼容链路' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="villageName" label="村点" min-width="180" />
          <el-table-column prop="serviceName" label="协作类型" min-width="160" />
          <el-table-column label="风险等级" width="120">
            <template #default="{ row }">
              <el-tag :type="riskTagType(row.riskLevel)">
                {{ riskLabel(row.riskLevel) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="风险原因" min-width="240">
            <template #default="{ row }">
              <span>{{ row.riskReasons.length ? row.riskReasons.join(' / ') : '暂无异常' }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="taskStatus" label="协作状态" width="140" />
          <el-table-column prop="orderStatus" label="承接状态" width="140" />
          <el-table-column prop="latestAuditText" label="最近动态" min-width="220" />
        </el-table>
      </div>
    </article>

    <section class="governance-panel-grid" style="margin-top: 16px">
      <article class="governance-panel">
        <div class="governance-panel__head">
          <div>
            <h3>运营处理建议</h3>
            <p>按当前会话状态和风险来源给出最小化处理顺序，先保证公益协作不中断。</p>
          </div>
        </div>
        <div class="governance-panel__body">
          <div class="governance-list">
            <article
              v-for="row in filteredRows.slice(0, 4)"
              :key="row.taskId"
              class="governance-list-card"
            >
              <div class="governance-list-card__title">
                <span>需求 #{{ row.demandId }} / {{ row.serviceName }}</span>
                <span :class="riskClass(row.riskLevel)">{{ riskLabel(row.riskLevel) }}</span>
              </div>
              <div class="governance-list-card__meta">
                <span>{{ row.source === 'collab' ? '新协作会话' : '旧链路兼容' }}</span>
                <span>状态：{{ row.taskStatus }}</span>
              </div>
              <div class="governance-list-card__desc">
                {{
                  row.riskReasons.length ? row.riskReasons.join('；') : '当前暂无异常，可继续观察。'
                }}
              </div>
              <div class="governance-list-card__meta">
                <span>村点：{{ row.villageName }}</span>
                <span>最近记录：{{ row.latestAuditAt }}</span>
              </div>
            </article>
          </div>
        </div>
      </article>

      <article class="governance-panel">
        <div class="governance-panel__head">
          <div>
            <h3>审计留痕</h3>
            <p>兼容现有管理员审计日志，帮助复盘为什么某条协作链路进入治理视野。</p>
          </div>
        </div>
        <div class="governance-panel__body">
          <div class="governance-list">
            <article
              v-for="audit in state.auditRows.slice(0, 6)"
              :key="audit.id"
              class="governance-list-card"
            >
              <div class="governance-list-card__title">
                <span>{{ audit.action }}</span>
                <span>{{ audit.createdAt }}</span>
              </div>
              <div class="governance-list-card__desc">{{ audit.content }}</div>
            </article>
          </div>
        </div>
      </article>
    </section>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { fetchConversationGovernance } from '../api/governance'

const loading = ref(false)
const keyword = ref('')
const riskFilter = ref('')
const statusFilter = ref('')

const state = reactive({
  summaryCards: [],
  priorityRows: [],
  auditRows: [],
  warnings: [],
  mode: 'fallback',
  updatedAt: ''
})

const filteredRows = computed(() => {
  const normalizedKeyword = keyword.value.trim().toLowerCase()

  return state.priorityRows.filter(row => {
    const matchesKeyword =
      !normalizedKeyword ||
      [String(row.demandId), row.villageName, row.serviceName].some(value =>
        String(value).toLowerCase().includes(normalizedKeyword)
      )

    const matchesRisk = !riskFilter.value || row.riskLevel === riskFilter.value
    const matchesStatus = !statusFilter.value || row.taskStatus === statusFilter.value

    return matchesKeyword && matchesRisk && matchesStatus
  })
})

const modeLabel = computed(() => {
  if (state.mode === 'live') {
    return '实时接口'
  }

  if (state.mode === 'mixed') {
    return '混合模式'
  }

  return '安全示例数据'
})

function summaryValue(index) {
  return state.summaryCards[index]?.value || '0'
}

function riskLabel(level) {
  return level === 'high' ? '高风险' : level === 'medium' ? '中风险' : '低风险'
}

function riskTagType(level) {
  return level === 'high' ? 'danger' : level === 'medium' ? 'warning' : 'success'
}

function riskClass(level) {
  return level === 'high'
    ? 'governance-risk-high'
    : level === 'medium'
      ? 'governance-risk-medium'
      : 'governance-risk-low'
}

function resetFilters() {
  keyword.value = ''
  riskFilter.value = ''
  statusFilter.value = ''
}

async function loadData() {
  loading.value = true

  try {
    const payload = await fetchConversationGovernance()
    Object.assign(state, payload)
  } finally {
    loading.value = false
  }
}

onMounted(loadData)
</script>

<style scoped>
@import './governance-shared.css';
</style>
