<template>
  <div class="page-shell">
    <section class="page-header">
      <div>
        <h1>订单管理</h1>
        <p>围绕履约进度、金额和异常状态，提升订单跟踪效率。</p>
      </div>
    </section>

    <section class="summary-grid">
      <article v-for="item in summaryCards" :key="item.label" class="summary-card">
        <span class="summary-label">{{ item.label }}</span>
        <strong class="summary-value">{{ item.value }}</strong>
        <span class="summary-desc">{{ item.desc }}</span>
      </article>
    </section>

    <el-card shadow="never" class="panel-card">
      <template #header>
        <div class="panel-head">
          <div>
            <h2>筛选条件</h2>
            <p>支持按订单状态、时间和关键词筛选履约单据。</p>
          </div>
          <el-tag type="info">共 {{ filteredRows.length }} 条</el-tag>
        </div>
      </template>

      <div class="filter-grid filter-grid-order">
        <el-input
          v-model="searchKeyword"
          placeholder="搜索订单号、需求号、农户、农机主"
          clearable
        />
        <el-select v-model="searchStatus" placeholder="订单状态" clearable>
          <el-option
            v-for="item in orderStatusOptions"
            :key="item.value"
            :label="item.label"
            :value="item.value"
          />
        </el-select>
        <el-date-picker
          v-model="dateRange"
          type="daterange"
          range-separator="至"
          start-placeholder="开始日期"
          end-placeholder="结束日期"
          value-format="YYYY-MM-DD"
        />
        <div class="filter-actions">
          <el-button type="primary" @click="handleSearch">应用筛选</el-button>
          <el-button @click="handleReset">重置</el-button>
        </div>
      </div>

      <div class="filter-tips">
        <el-tag effect="plain" type="warning">待协商与待确认优先关注</el-tag>
        <el-tag effect="plain" type="primary">服务中订单关注履约进度</el-tag>
        <el-tag effect="plain" type="info">金额与时效均基于前端演示数据</el-tag>
      </div>
    </el-card>

    <el-card shadow="never" class="panel-card">
      <template #header>
        <div class="panel-head">
          <div>
            <h2>订单列表</h2>
            <p>突出履约角色、服务内容、金额与当前处理动作。</p>
          </div>
        </div>
      </template>

      <el-empty
        v-if="pagedRows.length === 0"
        description="暂无符合条件的订单，请调整筛选项后重试。"
      />
      <el-table v-else :data="pagedRows" border stripe table-layout="auto">
        <el-table-column label="订单信息" min-width="180">
          <template #default="{ row }">
            <div class="primary-cell">
              <strong>#{{ row.id }}</strong>
              <span class="subtle">关联需求 #{{ row.demandId }}</span>
              <span class="subtle">创建于 {{ row.createdAt }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="履约双方" min-width="220">
          <template #default="{ row }">
            <div class="primary-cell">
              <span>农户：{{ row.farmerName }}</span>
              <span class="subtle">农机主：{{ row.ownerName }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="服务内容" min-width="200">
          <template #default="{ row }">
            <div class="primary-cell">
              <span>{{ row.serviceType }}</span>
              <span class="subtle">{{ row.location }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="金额/面积" width="140" align="right">
          <template #default="{ row }">
            <div class="primary-cell align-right">
              <strong>¥{{ formatAmount(row.totalAmount) }}</strong>
              <span class="subtle">{{ row.areaMu }} 亩</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="130" align="center">
          <template #default="{ row }">
            <el-tag :type="getTagType('orderStatus', row.status)">
              {{ getStatusLabel('orderStatus', row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="运营备注" min-width="220">
          <template #default="{ row }">
            {{ row.remark || '暂无备注' }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button size="small" @click="handleView(row)">查看</el-button>
            <el-button
              v-if="row.status === 'FINISHED_PENDING_CONFIRM'"
              size="small"
              type="success"
              plain
              @click="handleConfirm(row)"
            >
              确认完工
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :page-sizes="[10, 20, 50, 100]"
          :total="filteredRows.length"
          layout="total, sizes, prev, pager, next, jumper"
        />
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { fetchOrders } from '../api/admin'
import { getOptions, getStatusLabel, getTagType } from '../utils/status'

interface OrderRow {
  id: number
  demandId: number
  farmerName: string
  ownerName: string
  serviceType: string
  totalAmount: number
  status: string
  areaMu: number
  location: string
  remark: string
  createdAt: string
}

const orderStatusOptions = getOptions('orderStatus')

const rows = ref<OrderRow[]>([
  {
    id: 2001,
    demandId: 1001,
    farmerName: '张建国',
    ownerName: '李卫东',
    serviceType: '整地服务',
    totalAmount: 2500,
    status: 'SERVING',
    areaMu: 50,
    location: '常熟市梅李镇东河村 3 组',
    remark: '设备已进场，预计今日完成第一轮整地。',
    createdAt: '2026-03-28 11:30:00'
  },
  {
    id: 2002,
    demandId: 1002,
    farmerName: '王德福',
    ownerName: '赵国强',
    serviceType: '播种服务',
    totalAmount: 1800,
    status: 'WAIT_NEGOTIATION',
    areaMu: 30,
    location: '昆山市巴城镇虹桥片区 2 号田',
    remark: '双方待确认单价与进场时间。',
    createdAt: '2026-03-27 15:45:00'
  },
  {
    id: 2003,
    demandId: 1003,
    farmerName: '刘阿明',
    ownerName: '陈建华',
    serviceType: '收割服务',
    totalAmount: 3200,
    status: 'COMPLETED',
    areaMu: 80,
    location: '吴江区震泽镇示范点 A 区',
    remark: '已验收，待财务核对结算单。',
    createdAt: '2026-03-25 10:20:00'
  },
  {
    id: 2004,
    demandId: 1004,
    farmerName: '赵明海',
    ownerName: '周师傅',
    serviceType: '植保服务',
    totalAmount: 960,
    status: 'FINISHED_PENDING_CONFIRM',
    areaMu: 26,
    location: '太仓市沙溪镇北联村西侧地块',
    remark: '现场已作业完成，待农户确认。',
    createdAt: '2026-03-24 17:10:00'
  }
])

const searchKeyword = ref('')
const searchStatus = ref('')
const dateRange = ref<string[]>([])
const currentPage = ref(1)
const pageSize = ref(10)

const filteredRows = computed(() => {
  const keyword = searchKeyword.value.trim().toLowerCase()
  const [startDate, endDate] = dateRange.value || []

  return rows.value.filter(row => {
    const matchesKeyword =
      !keyword ||
      [
        String(row.id),
        String(row.demandId),
        row.farmerName,
        row.ownerName,
        row.serviceType,
        row.location
      ].some(value => value.toLowerCase().includes(keyword))

    const matchesStatus = !searchStatus.value || row.status === searchStatus.value
    const rowDate = row.createdAt.slice(0, 10)
    const matchesDate = !startDate || !endDate || (rowDate >= startDate && rowDate <= endDate)

    return matchesKeyword && matchesStatus && matchesDate
  })
})

const pagedRows = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value
  return filteredRows.value.slice(start, start + pageSize.value)
})

const summaryCards = computed(() => {
  const servingCount = filteredRows.value.filter(item => item.status === 'SERVING').length
  const pendingCount = filteredRows.value.filter(item =>
    ['WAIT_NEGOTIATION', 'FINISHED_PENDING_CONFIRM'].includes(item.status)
  ).length
  const completedAmount = filteredRows.value
    .filter(item => item.status === 'COMPLETED')
    .reduce((sum, item) => sum + item.totalAmount, 0)
  const totalAmount = filteredRows.value.reduce((sum, item) => sum + item.totalAmount, 0)

  return [
    { label: '订单总量', value: filteredRows.value.length, desc: '当前筛选结果内订单数' },
    { label: '服务中', value: servingCount, desc: '需跟踪履约进度与现场反馈' },
    { label: '待处理', value: pendingCount, desc: '优先协商或确认完工' },
    { label: '累计金额', value: `¥${formatAmount(totalAmount)}`, desc: '当前订单总金额' },
    { label: '已完工金额', value: `¥${formatAmount(completedAmount)}`, desc: '已完成订单金额汇总' }
  ]
})

function formatAmount(value: number): string {
  return value.toLocaleString('zh-CN')
}

function handleSearch(): void {
  currentPage.value = 1
  ElMessage.success('已应用筛选条件')
}

function handleReset(): void {
  searchKeyword.value = ''
  searchStatus.value = ''
  dateRange.value = []
  currentPage.value = 1
  ElMessage.success('已重置筛选条件')
}

function handleView(row: OrderRow): void {
  ElMessageBox.alert(
    `订单 #${row.id}\n状态：${getStatusLabel('orderStatus', row.status)}\n农户：${row.farmerName}\n农机主：${row.ownerName}\n备注：${row.remark}`,
    '订单详情',
    {
      confirmButtonText: '确定'
    }
  )
}

function handleConfirm(row: OrderRow): void {
  ElMessageBox.confirm(`确认订单 #${row.id} 已完工吗？`, '完工确认', {
    type: 'warning'
  })
    .then(() => {
      rows.value = rows.value.map(item =>
        item.id === row.id ? { ...item, status: 'COMPLETED' } : item
      )
      ElMessage.success('订单已确认完工')
    })
    .catch(() => undefined)
}
onMounted(async () => {
  try {
    const data = await fetchOrders()
    if (Array.isArray(data) && data.length > 0) {
      rows.value = data.map(item => ({
        id: item.id,
        demandId: item.demandId,
        farmerName: item.farmerId ? `农户 #${item.farmerId}` : '农户待补充',
        ownerName: item.ownerId ? `农机主 #${item.ownerId}` : '农机主待补充',
        serviceType: item.serviceItemId ? `服务项 #${item.serviceItemId}` : '待补充服务',
        totalAmount: 0,
        status: item.status || 'WAIT_NEGOTIATION',
        areaMu: 0,
        location: '待补充位置',
        remark: item.statusDescription || '来自真实接口',
        createdAt: item.createdAt || item.contactConfirmedAt || ''
      }))
    }
  } catch (error) {
    console.warn('加载真实订单列表失败，继续使用演示数据', error)
  }
})
</script>

<style scoped>
.page-shell {
  padding: 24px;
  background: #f5f7fa;
  min-height: 100vh;
}

.page-header,
.panel-head,
.filter-actions,
.filter-tips,
.pagination {
  display: flex;
  align-items: center;
}

.page-header,
.panel-head {
  justify-content: space-between;
  gap: 16px;
}

.page-header {
  margin-bottom: 16px;
}

.page-header h1,
.panel-head h2 {
  margin: 0;
  color: #1f2329;
}

.page-header p,
.panel-head p {
  margin: 6px 0 0;
  color: #667085;
  font-size: 14px;
}

.summary-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(180px, 1fr));
  gap: 16px;
  margin-bottom: 16px;
}

.summary-card {
  padding: 16px 18px;
  background: #fff;
  border: 1px solid #e5e6eb;
  border-radius: 12px;
}

.summary-label,
.summary-desc,
.subtle {
  color: #667085;
}

.summary-label,
.summary-desc {
  display: block;
}

.summary-value {
  display: block;
  margin: 8px 0;
  font-size: 28px;
  color: #1d2129;
}

.panel-card {
  margin-bottom: 16px;
  border-radius: 12px;
}

.filter-grid {
  display: grid;
  gap: 12px;
}

.filter-grid-order {
  grid-template-columns: minmax(220px, 2fr) minmax(180px, 1fr) minmax(260px, 1.5fr) auto;
}

.filter-actions {
  gap: 12px;
}

.filter-tips {
  flex-wrap: wrap;
  gap: 8px;
  margin-top: 12px;
}

.primary-cell {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.align-right {
  align-items: flex-end;
}

.primary-cell strong {
  color: #1d2129;
}

.subtle {
  font-size: 12px;
}

.pagination {
  justify-content: flex-end;
  margin-top: 16px;
}

@media (max-width: 1080px) {
  .filter-grid-order {
    grid-template-columns: 1fr;
  }

  .page-header,
  .panel-head {
    flex-direction: column;
    align-items: flex-start;
  }
}
</style>
