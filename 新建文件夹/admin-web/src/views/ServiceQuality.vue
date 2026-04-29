<template>
  <div class="governance-page">
    <section class="governance-hero">
      <div class="governance-hero__content">
        <span class="governance-hero__eyebrow">服务质量</span>
        <h1>投诉与闭环质量台</h1>
        <p>
          这一页聚焦“谁被反复投诉、哪些订单已经完成但仍有未结问题、投诉负载是否异常”。
          现阶段基于订单、举报和用户账号旧接口做质量聚合。
        </p>
      </div>

      <div class="governance-hero__meta">
        <article class="governance-status-card">
          <span class="governance-status-card__label">未结投诉</span>
          <strong class="governance-status-card__value">{{ summaryValue(2) }}</strong>
          <span class="governance-source-note">按举报状态估算</span>
        </article>
        <article class="governance-status-card">
          <span class="governance-status-card__label">投诉负载率</span>
          <strong class="governance-status-card__value">{{ summaryValue(3) }}</strong>
          <span class="governance-source-note">最近刷新：{{ state.updatedAt || '待加载' }}</span>
        </article>
      </div>
    </section>

    <section class="governance-toolbar">
      <div class="governance-toolbar__tags">
        <el-tag effect="plain">服务反馈优先保护农户权益</el-tag>
        <el-tag :type="state.mode === 'live' ? 'success' : 'warning'">{{ modeLabel }}</el-tag>
      </div>
      <div class="governance-toolbar__actions">
        <el-button :loading="loading" type="primary" @click="loadData">刷新服务质量</el-button>
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

    <section class="governance-panel-grid">
      <article class="governance-panel">
        <div class="governance-panel__head">
          <div>
            <h2>投诉聚合榜</h2>
            <p>合并相同被投诉对象的记录，方便快速识别反复被投诉的主体。</p>
          </div>
          <el-tag type="danger">{{ highPriorityCount }} 个高优先对象</el-tag>
        </div>
        <div class="governance-panel__body">
          <el-table :data="state.complaintRows" stripe table-layout="auto">
            <el-table-column prop="reportedUserId" label="用户ID" width="100" />
            <el-table-column prop="role" label="角色" width="110" />
            <el-table-column prop="phone" label="联系方式" min-width="150" />
            <el-table-column prop="openCount" label="未结投诉" width="100" />
            <el-table-column prop="totalCount" label="累计投诉" width="100" />
            <el-table-column label="风险等级" width="120">
              <template #default="{ row }">
                <el-tag :type="priorityTagType(row.priority)">{{
                  priorityLabel(row.priority)
                }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="投诉类型" min-width="220">
              <template #default="{ row }">
                {{ row.reportTypes.join(' / ') }}
              </template>
            </el-table-column>
          </el-table>
        </div>
      </article>

      <article class="governance-panel">
        <div class="governance-panel__head">
          <div>
            <h3>质量提示</h3>
            <p>给运营一个直接可执行的关注顺序。</p>
          </div>
        </div>
        <div class="governance-panel__body">
          <div class="governance-list">
            <article class="governance-list-card">
              <div class="governance-list-card__title">
                <span>优先回访对象</span>
                <span>{{ highPriorityCount }} 个</span>
              </div>
              <div class="governance-list-card__desc">
                对未结投诉大于等于 2 条的对象优先做人工电话核验和历史服务复盘。
              </div>
            </article>
            <article class="governance-list-card">
              <div class="governance-list-card__title">
                <span>已完成服务</span>
                <span>{{ summaryValue(0) }}</span>
              </div>
              <div class="governance-list-card__desc">
                已完成订单用于对照投诉负载率，避免只看投诉数量误判整体服务健康度。
              </div>
            </article>
            <article class="governance-list-card">
              <div class="governance-list-card__title">
                <span>治理原则</span>
                <span>公益优先</span>
              </div>
              <div class="governance-list-card__desc">
                先保护农户权益，再判断是否需要限制主体继续参与公益协作撮合。
              </div>
            </article>
          </div>
        </div>
      </article>
    </section>

    <article class="governance-panel" style="margin-top: 16px">
      <div class="governance-panel__head">
        <div>
          <h2>订单质量样本</h2>
          <p>兼容旧订单接口，供运营抽样复查订单状态与投诉处理压力。</p>
        </div>
      </div>
      <div class="governance-panel__body">
        <el-table :data="state.qualityRows" stripe table-layout="auto">
          <el-table-column prop="id" label="订单ID" width="100" />
          <el-table-column prop="demandId" label="Demand" width="100" />
          <el-table-column prop="ownerId" label="机手/服务者" width="120" />
          <el-table-column prop="farmerId" label="农户" width="100" />
          <el-table-column prop="status" label="状态" min-width="140" />
        </el-table>
      </div>
    </article>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { fetchServiceQuality } from '../api/governance'

const loading = ref(false)

const state = reactive({
  summaryCards: [],
  complaintRows: [],
  qualityRows: [],
  warnings: [],
  mode: 'fallback',
  updatedAt: ''
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

const highPriorityCount = computed(
  () => state.complaintRows.filter(item => item.priority === 'high').length
)

function summaryValue(index) {
  return state.summaryCards[index]?.value || '0'
}

function priorityLabel(priority) {
  return priority === 'high' ? '高优先' : priority === 'medium' ? '中优先' : '低优先'
}

function priorityTagType(priority) {
  return priority === 'high' ? 'danger' : priority === 'medium' ? 'warning' : 'success'
}

async function loadData() {
  loading.value = true

  try {
    const payload = await fetchServiceQuality()
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
