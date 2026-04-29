<template>
  <div class="report-page">
    <h2 class="page-title">📊 数据报表</h2>

    <!-- KPI 卡片 -->
    <div class="kpi-row">
      <div v-for="kpi in kpis" :key="kpi.label" class="kpi-card">
        <div class="kpi-icon">{{ kpi.icon }}</div>
        <div class="kpi-body">
          <div class="kpi-value">{{ kpi.value }}</div>
          <div class="kpi-label">{{ kpi.label }}</div>
          <div class="kpi-trend" :class="kpi.up ? 'trend-up' : 'trend-down'">
            {{ kpi.up ? '▲' : '▼' }} {{ kpi.trend }}
          </div>
        </div>
      </div>
    </div>

    <!-- 图表区域 -->
    <div class="chart-grid">
      <!-- 匹配成功率趋势 -->
      <div class="chart-card">
        <div class="chart-title">匹配成功率趋势（近 7 天）</div>
        <div class="chart-placeholder" ref="matchChartRef">
          <div class="mock-chart">
            <div v-for="(val, i) in matchTrendData" :key="i" class="bar-col">
              <div class="bar" :style="{ height: val + '%' }"></div>
              <span class="bar-label">{{ matchLabels[i] }}</span>
            </div>
          </div>
        </div>
      </div>

      <!-- 订单量统计 -->
      <div class="chart-card">
        <div class="chart-title">日订单量统计（近 7 天）</div>
        <div class="chart-placeholder">
          <div class="mock-chart">
            <div v-for="(val, i) in orderTrendData" :key="i" class="bar-col">
              <div class="bar bar-blue" :style="{ height: val / 5 + '%' }"></div>
              <span class="bar-label">{{ matchLabels[i] }}</span>
            </div>
          </div>
        </div>
      </div>

      <!-- 用户活跃分布 -->
      <div class="chart-card">
        <div class="chart-title">用户角色分布</div>
        <div class="donut-chart">
          <div class="donut-ring">
            <svg viewBox="0 0 100 100" class="donut-svg">
              <circle
                cx="50"
                cy="50"
                r="40"
                fill="none"
                stroke="#00D4FF"
                stroke-width="12"
                stroke-dasharray="163 88"
                stroke-dashoffset="-25"
              />
              <circle
                cx="50"
                cy="50"
                r="40"
                fill="none"
                stroke="#00FF94"
                stroke-width="12"
                stroke-dasharray="63 188"
                stroke-dashoffset="-188"
              />
              <circle
                cx="50"
                cy="50"
                r="40"
                fill="none"
                stroke="#FFB800"
                stroke-width="12"
                stroke-dasharray="25 226"
                stroke-dashoffset="-0"
              />
            </svg>
            <div class="donut-center">
              <span class="donut-total">1,247</span>
              <span class="donut-label">总用户</span>
            </div>
          </div>
          <div class="donut-legend">
            <div class="legend-item">
              <span class="dot" style="background: #00d4ff"></span> 农户 — 812 (65%)
            </div>
            <div class="legend-item">
              <span class="dot" style="background: #00ff94"></span> 机主 — 335 (27%)
            </div>
            <div class="legend-item">
              <span class="dot" style="background: #ffb800"></span> 管理员 — 100 (8%)
            </div>
          </div>
        </div>
      </div>

      <!-- MQ 消费统计 -->
      <div class="chart-card">
        <div class="chart-title">MQ 消息处理统计（今日）</div>
        <div class="mq-stats">
          <div v-for="q in mqStats" :key="q.queue" class="mq-row">
            <span class="mq-queue">{{ q.queue }}</span>
            <div class="mq-bar-track">
              <div
                class="mq-bar-fill"
                :style="{ width: (q.processed / q.total) * 100 + '%' }"
              ></div>
            </div>
            <span class="mq-nums">{{ q.processed }}/{{ q.total }}</span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'

const kpis = ref([
  { icon: '🎯', label: '匹配成功率', value: '87.3%', trend: '+2.1%', up: true },
  { icon: '📦', label: '今日订单量', value: '342', trend: '+18', up: true },
  { icon: '👥', label: '活跃用户', value: '1,247', trend: '+56', up: true },
  { icon: '⏱️', label: '平均响应时间', value: '2.3s', trend: '-0.4s', up: true }
])

const matchLabels = ['03/29', '03/30', '03/31', '04/01', '04/02', '04/03', '04/04']
const matchTrendData = ref([78, 82, 85, 81, 88, 86, 87])
const orderTrendData = ref([280, 310, 295, 335, 320, 350, 342])

const mqStats = ref([
  { queue: 'match.dispatch', processed: 342, total: 345 },
  { queue: 'order.status', processed: 298, total: 298 },
  { queue: 'timeout.result', processed: 47, total: 47 },
  { queue: 'dlx (死信)', processed: 3, total: 3 }
])
</script>

<style scoped>
.report-page {
  padding: 24px;
  min-height: 100vh;
}
.page-title {
  font-size: 20px;
  font-weight: 800;
  color: #e8f4fd;
  margin-bottom: 24px;
}

/* KPI */
.kpi-row {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
  margin-bottom: 24px;
}
.kpi-card {
  background: rgba(0, 212, 255, 0.04);
  border: 1px solid rgba(0, 212, 255, 0.12);
  border-radius: 12px;
  padding: 16px;
  display: flex;
  align-items: center;
  gap: 14px;
}
.kpi-icon {
  font-size: 28px;
}
.kpi-value {
  font-size: 24px;
  font-weight: 900;
  color: #e8f4fd;
}
.kpi-label {
  font-size: 12px;
  color: #5b8db8;
  margin-top: 2px;
}
.kpi-trend {
  font-size: 11px;
  margin-top: 4px;
  font-weight: 600;
}
.trend-up {
  color: #00ff94;
}
.trend-down {
  color: #ff4d4f;
}

/* Charts */
.chart-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 16px;
}
.chart-card {
  background: rgba(0, 212, 255, 0.04);
  border: 1px solid rgba(0, 212, 255, 0.12);
  border-radius: 12px;
  padding: 20px;
}
.chart-title {
  font-size: 14px;
  font-weight: 700;
  color: #8db4cf;
  margin-bottom: 16px;
  padding-left: 10px;
  border-left: 3px solid #00d4ff;
}

/* Mock bar chart */
.mock-chart {
  display: flex;
  align-items: flex-end;
  gap: 12px;
  height: 160px;
  padding: 0 8px;
}
.bar-col {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  height: 100%;
  justify-content: flex-end;
}
.bar {
  width: 100%;
  border-radius: 4px 4px 0 0;
  background: linear-gradient(180deg, #00ff94, rgba(0, 255, 148, 0.3));
  transition: height 0.6s ease;
  min-height: 4px;
}
.bar-blue {
  background: linear-gradient(180deg, #00d4ff, rgba(0, 212, 255, 0.3)) !important;
}
.bar-label {
  font-size: 10px;
  color: #5b8db8;
}

/* Donut */
.donut-chart {
  display: flex;
  align-items: center;
  gap: 24px;
}
.donut-ring {
  position: relative;
  width: 140px;
  height: 140px;
}
.donut-svg {
  width: 100%;
  height: 100%;
  transform: rotate(-90deg);
}
.donut-center {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  text-align: center;
  display: flex;
  flex-direction: column;
}
.donut-total {
  font-size: 22px;
  font-weight: 900;
  color: #e8f4fd;
}
.donut-label {
  font-size: 11px;
  color: #5b8db8;
}
.donut-legend {
  display: flex;
  flex-direction: column;
  gap: 10px;
}
.legend-item {
  font-size: 13px;
  color: #cbd5e1;
  display: flex;
  align-items: center;
  gap: 8px;
}
.dot {
  width: 10px;
  height: 10px;
  border-radius: 50%;
  flex-shrink: 0;
}

/* MQ Stats */
.mq-stats {
  display: flex;
  flex-direction: column;
  gap: 14px;
}
.mq-row {
  display: flex;
  align-items: center;
  gap: 12px;
}
.mq-queue {
  font-size: 13px;
  color: #8db4cf;
  width: 140px;
  font-weight: 600;
}
.mq-bar-track {
  flex: 1;
  height: 10px;
  background: rgba(255, 255, 255, 0.06);
  border-radius: 5px;
  overflow: hidden;
}
.mq-bar-fill {
  height: 100%;
  background: linear-gradient(90deg, #00d4ff, #00ff94);
  border-radius: 5px;
  transition: width 0.6s;
}
.mq-nums {
  font-size: 12px;
  color: #5b8db8;
  width: 60px;
  text-align: right;
}
</style>
