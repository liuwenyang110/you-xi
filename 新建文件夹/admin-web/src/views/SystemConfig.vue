<template>
  <div class="config-page">
    <h2 class="page-title">⚙️ 系统配置</h2>

    <!-- 系统状态卡片 -->
    <div class="status-grid">
      <div
        v-for="item in systemStatus"
        :key="item.name"
        class="status-card"
        :class="'status-' + item.health"
      >
        <div class="sc-header">
          <span class="sc-icon">{{ item.icon }}</span>
          <span class="sc-name">{{ item.name }}</span>
          <span class="sc-badge" :class="'badge-' + item.health">{{ item.healthLabel }}</span>
        </div>
        <div class="sc-detail">{{ item.detail }}</div>
      </div>
    </div>

    <!-- 功能开关 -->
    <div class="config-section">
      <h3 class="section-title">功能开关</h3>
      <div class="switch-grid">
        <div v-for="sw in switches" :key="sw.key" class="switch-row">
          <div class="sw-info">
            <span class="sw-label">{{ sw.label }}</span>
            <span class="sw-desc">{{ sw.desc }}</span>
          </div>
          <el-switch v-model="sw.value" :active-text="sw.onText" :inactive-text="sw.offText" />
        </div>
      </div>
    </div>

    <!-- 环境变量 -->
    <div class="config-section">
      <h3 class="section-title">环境配置</h3>
      <el-table :data="envVars" stripe style="width: 100%" header-cell-class-name="dark-header">
        <el-table-column prop="key" label="配置项" width="280" />
        <el-table-column prop="value" label="当前值" />
        <el-table-column prop="source" label="来源" width="120" />
      </el-table>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'

const systemStatus = ref([
  {
    icon: '🗄️',
    name: 'MySQL 数据库',
    health: 'up',
    healthLabel: '运行中',
    detail: 'mysql:3306 / nongzhushou'
  },
  {
    icon: '⚡',
    name: 'Redis 缓存',
    health: 'up',
    healthLabel: '运行中',
    detail: 'redis:6379 / db0'
  },
  {
    icon: '🐰',
    name: 'RabbitMQ 消息队列',
    health: 'up',
    healthLabel: '运行中',
    detail: 'rabbitmq:5672 / 3 queues active'
  },
  {
    icon: '🔍',
    name: 'Zipkin 链路追踪',
    health: 'up',
    healthLabel: '运行中',
    detail: 'zipkin:9411 / 采样率 100%'
  },
  {
    icon: '📈',
    name: 'Prometheus 监控',
    health: 'up',
    healthLabel: '抓取中',
    detail: '每 15s 采集一次 / 运行 2h'
  },
  {
    icon: '📊',
    name: 'Grafana 仪表盘',
    health: 'up',
    healthLabel: '运行中',
    detail: 'grafana:3000 / 1 仪表盘'
  }
])

const switches = ref([
  {
    key: 'gaode',
    label: '高德地图 API',
    desc: '关闭后启用 H5 模拟地图',
    value: true,
    onText: '在线',
    offText: '模拟'
  },
  {
    key: 'sms',
    label: '短信发送',
    desc: '关闭后验证码打印到控制台',
    value: false,
    onText: '真实',
    offText: 'Mock'
  },
  {
    key: 'mq',
    label: 'MQ 异步匹配',
    desc: '关闭后回退为同步匹配',
    value: true,
    onText: '异步',
    offText: '同步'
  },
  {
    key: 'trace',
    label: '全链路追踪',
    desc: '关闭后停止上报 Zipkin',
    value: true,
    onText: '开启',
    offText: '关闭'
  }
])

const envVars = ref([
  { key: 'SPRING_PROFILES_ACTIVE', value: 'dev', source: '环境变量' },
  { key: 'MYSQL_HOST', value: '127.0.0.1', source: '默认值' },
  { key: 'REDIS_HOST', value: '127.0.0.1', source: '默认值' },
  { key: 'RABBITMQ_HOST', value: 'localhost', source: '默认值' },
  { key: 'JWT_SECRET', value: '******', source: '环境变量' },
  { key: 'GAODE_MAP_API_KEY', value: '(未配置)', source: '默认值' },
  { key: 'TRACING_SAMPLE_RATE', value: '1.0', source: '默认值' }
])
</script>

<style scoped>
.config-page {
  padding: 24px;
  min-height: 100vh;
}
.page-title {
  font-size: 20px;
  font-weight: 800;
  color: #e8f4fd;
  margin-bottom: 24px;
}

.status-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 16px;
  margin-bottom: 32px;
}
.status-card {
  background: rgba(0, 212, 255, 0.04);
  border: 1px solid rgba(0, 212, 255, 0.12);
  border-radius: 12px;
  padding: 16px;
  transition: all 0.2s;
}
.status-card:hover {
  border-color: rgba(0, 212, 255, 0.3);
}
.status-up {
  border-left: 3px solid #00ff94;
}
.status-down {
  border-left: 3px solid #ff4d4f;
}

.sc-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 8px;
}
.sc-icon {
  font-size: 20px;
}
.sc-name {
  font-size: 14px;
  font-weight: 700;
  color: #e8f4fd;
  flex: 1;
}
.sc-badge {
  font-size: 11px;
  font-weight: 700;
  padding: 2px 8px;
  border-radius: 100px;
}
.badge-up {
  background: rgba(0, 255, 148, 0.15);
  color: #00ff94;
}
.badge-down {
  background: rgba(255, 77, 79, 0.15);
  color: #ff4d4f;
}
.sc-detail {
  font-size: 12px;
  color: #5b8db8;
}

.config-section {
  margin-bottom: 32px;
}
.section-title {
  font-size: 15px;
  font-weight: 700;
  color: #8db4cf;
  margin-bottom: 16px;
  padding-left: 10px;
  border-left: 3px solid #00d4ff;
}

.switch-grid {
  display: flex;
  flex-direction: column;
  gap: 12px;
}
.switch-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: rgba(255, 255, 255, 0.03);
  border-radius: 8px;
  padding: 12px 16px;
}
.sw-info {
  display: flex;
  flex-direction: column;
}
.sw-label {
  font-size: 14px;
  font-weight: 600;
  color: #e8f4fd;
}
.sw-desc {
  font-size: 12px;
  color: #5b8db8;
  margin-top: 2px;
}

:deep(.dark-header) {
  background: #0a1628 !important;
  color: #8db4cf !important;
}
:deep(.el-table) {
  background: transparent !important;
  --el-table-bg-color: transparent;
  --el-table-tr-bg-color: transparent;
  --el-table-header-bg-color: #0a1628;
}
:deep(.el-table td),
:deep(.el-table th) {
  border-color: rgba(0, 212, 255, 0.08) !important;
}
:deep(.el-table__row) {
  color: #cbd5e1;
}
:deep(.el-table__row:hover > td) {
  background: rgba(0, 212, 255, 0.06) !important;
}
</style>
