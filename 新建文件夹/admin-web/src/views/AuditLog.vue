<template>
  <div class="audit-page">
    <h2 class="page-title">📝 操作日志</h2>

    <!-- 筛选栏 -->
    <div class="filter-bar">
      <el-select v-model="filterType" placeholder="操作类型" clearable style="width: 160px">
        <el-option label="全部" value="" />
        <el-option label="登录" value="LOGIN" />
        <el-option label="需求发布" value="DEMAND_CREATE" />
        <el-option label="匹配派单" value="MATCH_DISPATCH" />
        <el-option label="订单确认" value="ORDER_CONFIRM" />
        <el-option label="系统配置" value="SYS_CONFIG" />
      </el-select>
      <el-date-picker
        v-model="dateRange"
        type="daterange"
        range-separator="至"
        start-placeholder="开始日期"
        end-placeholder="结束日期"
        style="width: 300px"
      />
      <el-input
        v-model="searchUser"
        placeholder="搜索用户ID或手机号"
        clearable
        style="width: 200px"
      />
      <el-button type="primary" @click="handleSearch">查询</el-button>
    </div>

    <!-- 日志表格 -->
    <el-table :data="auditLogs" stripe style="width: 100%" header-cell-class-name="dark-header">
      <el-table-column prop="time" label="时间" width="180" />
      <el-table-column prop="traceId" label="TraceID" width="200">
        <template #default="{ row }">
          <el-link type="primary" @click="openZipkin(row.traceId)">{{ row.traceId }}</el-link>
        </template>
      </el-table-column>
      <el-table-column prop="userId" label="用户ID" width="100" />
      <el-table-column prop="action" label="操作类型" width="140">
        <template #default="{ row }">
          <el-tag :type="actionTagType(row.action)" size="small">{{ row.actionLabel }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="detail" label="操作详情" />
      <el-table-column prop="ip" label="IP 地址" width="140" />
      <el-table-column prop="result" label="结果" width="80">
        <template #default="{ row }">
          <span :class="row.success ? 'res-ok' : 'res-fail'">{{
            row.success ? '✓ 成功' : '✗ 失败'
          }}</span>
        </template>
      </el-table-column>
    </el-table>

    <!-- 分页 -->
    <div class="pagination-bar">
      <el-pagination
        background
        layout="total, prev, pager, next"
        :total="auditLogs.length"
        :page-size="20"
      />
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'

const filterType = ref('')
const dateRange = ref(null)
const searchUser = ref('')

const auditLogs = ref([
  {
    time: '2026-04-04 14:32:18',
    traceId: 'a1b2c3d4e5f60001',
    userId: '10001',
    action: 'DEMAND_CREATE',
    actionLabel: '需求发布',
    detail: '发布小麦收割需求 #D20260404001，面积 15 亩',
    ip: '192.168.1.101',
    success: true
  },
  {
    time: '2026-04-04 14:32:19',
    traceId: 'a1b2c3d4e5f60002',
    userId: 'SYSTEM',
    action: 'MATCH_DISPATCH',
    actionLabel: '匹配派单',
    detail: 'MQ 消费匹配消息 demandId=1001, 命中机主 userId=10002 (tier=1)',
    ip: '10.0.0.5',
    success: true
  },
  {
    time: '2026-04-04 14:33:05',
    traceId: 'a1b2c3d4e5f60003',
    userId: '10002',
    action: 'ORDER_CONFIRM',
    actionLabel: '接单确认',
    detail: '机主接受匹配 attemptId=501，进入联系协商阶段',
    ip: '192.168.1.205',
    success: true
  },
  {
    time: '2026-04-04 14:28:41',
    traceId: 'a1b2c3d4e5f60004',
    userId: '10003',
    action: 'LOGIN',
    actionLabel: '管理登录',
    detail: '管理员登录管理后台',
    ip: '10.0.0.1',
    success: true
  },
  {
    time: '2026-04-04 14:15:33',
    traceId: 'a1b2c3d4e5f60005',
    userId: '10004',
    action: 'LOGIN',
    actionLabel: '登录尝试',
    detail: '验证码错误，第 2 次尝试',
    ip: '223.104.3.xx',
    success: false
  },
  {
    time: '2026-04-04 13:55:12',
    traceId: 'a1b2c3d4e5f60006',
    userId: 'SYSTEM',
    action: 'SYS_CONFIG',
    actionLabel: '系统配置',
    detail: 'RabbitMQ 连接恢复，重新注册消费者',
    ip: '10.0.0.5',
    success: true
  },
  {
    time: '2026-04-04 13:40:08',
    traceId: 'a1b2c3d4e5f60007',
    userId: '10001',
    action: 'DEMAND_CREATE',
    actionLabel: '需求发布',
    detail: '发布玉米播种需求 #D20260404002，面积 8 亩',
    ip: '192.168.1.101',
    success: true
  }
])

function actionTagType(action) {
  const map = {
    LOGIN: '',
    DEMAND_CREATE: 'success',
    MATCH_DISPATCH: 'warning',
    ORDER_CONFIRM: 'primary',
    SYS_CONFIG: 'info'
  }
  return map[action] || ''
}

function openZipkin(traceId) {
  window.open(`http://localhost:9411/zipkin/traces/${traceId}`, '_blank')
}

function handleSearch() {
  // TODO: 对接后端审计日志 API
}
</script>

<style scoped>
.audit-page {
  padding: 24px;
  min-height: 100vh;
}
.page-title {
  font-size: 20px;
  font-weight: 800;
  color: #e8f4fd;
  margin-bottom: 24px;
}

.filter-bar {
  display: flex;
  gap: 12px;
  margin-bottom: 20px;
  flex-wrap: wrap;
  align-items: center;
}

.pagination-bar {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

.res-ok {
  color: #00ff94;
  font-weight: 600;
  font-size: 12px;
}
.res-fail {
  color: #ff4d4f;
  font-weight: 600;
  font-size: 12px;
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
