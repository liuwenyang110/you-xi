<template>
  <div class="governance-page">
    <section class="governance-hero">
      <div class="governance-hero__content">
        <span class="governance-hero__eyebrow">内容治理</span>
        <h1>公开内容巡检台</h1>
        <p>
          现阶段先把需求公开内容当作内容治理入口，结合举报和审计日志做兼容巡检。 后续后端接入
          community/collab 新接口后，这一页可以直接切真实内容源。
        </p>
      </div>

      <div class="governance-hero__meta">
        <article class="governance-status-card">
          <span class="governance-status-card__label">待审风险内容</span>
          <strong class="governance-status-card__value">{{ summaryValue(1) }}</strong>
          <span class="governance-source-note">按举报与治理信号估算</span>
        </article>
        <article class="governance-status-card">
          <span class="governance-status-card__label">当前模式</span>
          <strong class="governance-status-card__value">{{ modeLabel }}</strong>
          <span class="governance-source-note">最近刷新：{{ state.updatedAt || '待加载' }}</span>
        </article>
      </div>
    </section>

    <section class="governance-toolbar">
      <div class="governance-toolbar__tags">
        <el-tag effect="plain">公开求助优先公益表达</el-tag>
        <el-tag effect="plain" type="warning">兼容旧需求数据源</el-tag>
      </div>
      <div class="governance-toolbar__actions">
        <el-button :loading="loading" type="primary" @click="loadData">刷新内容治理</el-button>
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

    <section class="governance-panel-grid--three">
      <article class="governance-panel">
        <div class="governance-panel__head">
          <div>
            <h2>公开内容列表</h2>
            <p>支持按村点、作物和状态筛选，快速定位需要巡检的内容。</p>
          </div>
          <el-tag type="info">{{ filteredRows.length }} 条</el-tag>
        </div>
        <div class="governance-panel__body">
          <div class="governance-filter">
            <el-input v-model="keyword" placeholder="搜索村点、内容编号、作物" clearable />
            <el-select v-model="statusFilter" placeholder="内容状态" clearable>
              <el-option label="已发布" value="PUBLISHED" />
              <el-option label="匹配中" value="MATCHING" />
              <el-option label="已完成" value="COMPLETED" />
            </el-select>
            <el-select v-model="reportFilter" placeholder="举报信号" clearable>
              <el-option label="有举报" value="reported" />
              <el-option label="无举报" value="clean" />
            </el-select>
            <el-button @click="resetFilters">重置</el-button>
          </div>

          <el-empty
            v-if="!filteredRows.length"
            description="当前筛选条件下没有内容记录"
            class="governance-empty"
          />
          <el-table v-else :data="filteredRows" stripe table-layout="auto">
            <el-table-column prop="id" label="内容ID" width="90" />
            <el-table-column prop="villageName" label="村点" min-width="180" />
            <el-table-column prop="cropCode" label="作物" width="100" />
            <el-table-column prop="status" label="状态" width="120" />
            <el-table-column prop="reportCount" label="举报数" width="90" />
            <el-table-column prop="auditHint" label="治理备注" min-width="220" />
          </el-table>
        </div>
      </article>

      <article class="governance-panel">
        <div class="governance-panel__head">
          <div>
            <h3>举报池</h3>
            <p>优先处理未结举报，避免公益内容被商业化或恶意刷屏。</p>
          </div>
        </div>
        <div class="governance-panel__body">
          <div class="governance-list">
            <article v-for="item in state.reportRows" :key="item.id" class="governance-list-card">
              <div class="governance-list-card__title">
                <span>举报 #{{ item.id }}</span>
                <el-tag :type="item.status === 'RESOLVED' ? 'success' : 'danger'" size="small">
                  {{ item.status }}
                </el-tag>
              </div>
              <div class="governance-list-card__desc">
                被举报用户：{{ item.reportedUserId }} · 类型：{{ item.reportType }}
              </div>
            </article>
          </div>
        </div>
      </article>

      <article class="governance-panel">
        <div class="governance-panel__head">
          <div>
            <h3>治理动作</h3>
            <p>保留人工下架、巡检和复核痕迹，便于追溯决策。</p>
          </div>
        </div>
        <div class="governance-panel__body">
          <div class="governance-list">
            <article
              v-for="item in state.moderationRows"
              :key="item.id"
              class="governance-list-card"
            >
              <div class="governance-list-card__title">
                <span>{{ item.action }}</span>
                <span>{{ item.createdAt }}</span>
              </div>
              <div class="governance-list-card__desc">{{ item.content }}</div>
            </article>
          </div>
        </div>
      </article>
    </section>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { fetchContentGovernance } from '../api/governance'

const loading = ref(false)
const keyword = ref('')
const statusFilter = ref('')
const reportFilter = ref('')

const state = reactive({
  summaryCards: [],
  contentRows: [],
  reportRows: [],
  moderationRows: [],
  warnings: [],
  mode: 'fallback',
  updatedAt: ''
})

const filteredRows = computed(() => {
  const normalizedKeyword = keyword.value.trim().toLowerCase()

  return state.contentRows.filter(row => {
    const matchesKeyword =
      !normalizedKeyword ||
      [String(row.id), row.villageName, row.cropCode].some(value =>
        String(value).toLowerCase().includes(normalizedKeyword)
      )

    const matchesStatus = !statusFilter.value || row.status === statusFilter.value
    const matchesReport =
      !reportFilter.value ||
      (reportFilter.value === 'reported' && row.reportCount > 0) ||
      (reportFilter.value === 'clean' && row.reportCount === 0)

    return matchesKeyword && matchesStatus && matchesReport
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

function resetFilters() {
  keyword.value = ''
  statusFilter.value = ''
  reportFilter.value = ''
}

async function loadData() {
  loading.value = true

  try {
    const payload = await fetchContentGovernance()
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
