<template>
  <div class="governance-page">
    <section class="governance-hero">
      <div class="governance-hero__content">
        <span class="governance-hero__eyebrow">公益运营总览</span>
        <h1>农助公益联络中枢</h1>
        <p>
          这一页把求助发布、协作响应、举报治理和服务闭环汇总到一个运营视角里。
          平台只做公益信息撮合与治理留痕，不参与交易定价和资金流转。
        </p>
      </div>

      <div class="governance-hero__meta">
        <article class="governance-status-card">
          <span class="governance-status-card__label">数据模式</span>
          <strong class="governance-status-card__value">{{ modeLabel }}</strong>
          <span class="governance-source-note">最近刷新：{{ state.updatedAt || '待加载' }}</span>
        </article>
        <article class="governance-status-card">
          <span class="governance-status-card__label">治理提示</span>
          <strong class="governance-status-card__value"
            >{{ state.riskSignals.openReports || 0 }} 条待处理举报</strong
          >
          <span class="governance-source-note"
            >活跃求助 {{ state.riskSignals.activeDemands || 0 }} 条</span
          >
        </article>
      </div>
    </section>

    <section class="governance-toolbar">
      <div class="governance-toolbar__tags">
        <el-tag
          :type="state.mode === 'live' ? 'success' : state.mode === 'mixed' ? 'warning' : 'info'"
        >
          {{ modeLabel }}
        </el-tag>
        <el-tag effect="plain">仅用于公益协作治理</el-tag>
        <el-tag effect="plain" type="success">旧接口兼容聚合</el-tag>
      </div>

      <div class="governance-toolbar__actions">
        <el-button :loading="loading" type="primary" @click="loadData">刷新总览</el-button>
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
      <article v-for="card in state.overviewCards" :key="card.key" class="governance-summary-card">
        <span class="governance-summary-card__label">{{ card.label }}</span>
        <strong class="governance-summary-card__value">{{ card.value }}</strong>
        <span class="governance-summary-card__hint">{{ card.hint }}</span>
      </article>
    </section>

    <section class="governance-panel-grid">
      <article class="governance-panel">
        <div class="governance-panel__head">
          <div>
            <h2>公益覆盖热区</h2>
            <p>按当前公开求助村点聚合，帮助运营快速识别需要集中服务响应的区域。</p>
          </div>
          <el-tag type="success">共 {{ state.coverageRows.length }} 个村点</el-tag>
        </div>
        <div class="governance-panel__body">
          <el-empty
            v-if="!state.coverageRows.length"
            description="暂无可展示的公开求助数据"
            class="governance-empty"
          />
          <el-table v-else :data="state.coverageRows" stripe table-layout="auto">
            <el-table-column prop="villageName" label="村点" min-width="180" />
            <el-table-column prop="status" label="状态" width="120" />
            <el-table-column prop="areaMu" label="面积(亩)" width="100" />
            <el-table-column prop="createdAt" label="发布时间" min-width="160" />
          </el-table>
        </div>
      </article>

      <article class="governance-panel">
        <div class="governance-panel__head">
          <div>
            <h2>治理风向</h2>
            <p>核心治理信号和公益原则提醒，帮助值守人员判断今天先处理什么。</p>
          </div>
        </div>
        <div class="governance-panel__body">
          <div class="governance-kv-grid">
            <article class="governance-kv-card">
              <span class="governance-kv-card__label">未结举报</span>
              <strong class="governance-kv-card__value">{{
                state.riskSignals.openReports || 0
              }}</strong>
            </article>
            <article class="governance-kv-card">
              <span class="governance-kv-card__label">活跃求助</span>
              <strong class="governance-kv-card__value">{{
                state.riskSignals.activeDemands || 0
              }}</strong>
            </article>
            <article class="governance-kv-card">
              <span class="governance-kv-card__label">志愿参与</span>
              <strong class="governance-kv-card__value">{{
                state.riskSignals.volunteerCount || 0
              }}</strong>
            </article>
          </div>

          <div class="governance-list" style="margin-top: 16px">
            <article
              v-for="alert in state.governanceAlerts"
              :key="alert.title"
              class="governance-list-card"
            >
              <div class="governance-list-card__title">
                <span>{{ alert.title }}</span>
                <el-tag :type="tagTypeMap[alert.level] || 'info'" size="small">{{
                  alert.level
                }}</el-tag>
              </div>
              <div class="governance-list-card__desc">{{ alert.description }}</div>
            </article>
          </div>
        </div>
      </article>
    </section>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { fetchGovernanceDashboard } from '../api/governance'

const loading = ref(false)

const state = reactive({
  overviewCards: [],
  coverageRows: [],
  governanceAlerts: [],
  riskSignals: {
    openReports: 0,
    activeDemands: 0,
    volunteerCount: 0
  },
  warnings: [],
  source: {},
  mode: 'fallback',
  updatedAt: ''
})

const tagTypeMap = {
  info: 'info',
  success: 'success',
  warning: 'warning'
}

const modeLabel = computed(() => {
  if (state.mode === 'live') {
    return '实时接口'
  }

  if (state.mode === 'mixed') {
    return '接口 + 回退混合'
  }

  return '安全示例数据'
})

async function loadData() {
  loading.value = true

  try {
    const payload = await fetchGovernanceDashboard()
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
