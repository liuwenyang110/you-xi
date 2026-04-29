<template>
  <div class="page-shell">
    <section class="page-header">
      <div>
        <h1>设备管理</h1>
        <p>围绕设备状态、审核进度和地域分布，增强后台设备管理可读性。</p>
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
            <p>支持按设备状态、审核状态和关键词快速定位设备。</p>
          </div>
          <el-tag type="info">共 {{ filteredRows.length }} 台</el-tag>
        </div>
      </template>

      <div class="filter-grid filter-grid-equipment">
        <el-input
          v-model="searchKeyword"
          placeholder="搜索设备名称、型号、所属农机主、地区"
          clearable
        />
        <el-select v-model="searchStatus" placeholder="设备状态" clearable>
          <el-option
            v-for="item in equipmentStatusOptions"
            :key="item.value"
            :label="item.label"
            :value="item.value"
          />
        </el-select>
        <el-select v-model="searchApproveStatus" placeholder="审核状态" clearable>
          <el-option
            v-for="item in approveStatusOptions"
            :key="item.value"
            :label="item.label"
            :value="item.value"
          />
        </el-select>
        <div class="filter-actions">
          <el-button type="primary" @click="handleSearch">应用筛选</el-button>
          <el-button @click="handleReset">重置</el-button>
        </div>
      </div>

      <div class="filter-tips">
        <el-tag effect="plain" type="warning">待审核设备建议优先处理</el-tag>
        <el-tag effect="plain" type="primary">作业中设备重点关注位置与可调度性</el-tag>
        <el-tag effect="plain" type="info">当前数据为前端演示数据</el-tag>
      </div>
    </el-card>

    <el-card shadow="never" class="panel-card">
      <template #header>
        <div class="panel-head">
          <div>
            <h2>设备列表</h2>
            <p>突出设备型号、所属人、状态与审核情况。</p>
          </div>
        </div>
      </template>

      <el-empty
        v-if="pagedRows.length === 0"
        description="暂无符合条件的设备，请调整筛选后重试。"
      />
      <el-table v-else :data="pagedRows" border stripe table-layout="auto">
        <el-table-column label="设备信息" min-width="220">
          <template #default="{ row }">
            <div class="primary-cell">
              <strong>#{{ row.id }} {{ row.equipmentName }}</strong>
              <span>{{ row.brandModel }}</span>
              <span class="subtle">车牌/编号：{{ row.deviceCode }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="所属农机主" min-width="180">
          <template #default="{ row }">
            <div class="primary-cell">
              <span>{{ row.ownerName }}</span>
              <span class="subtle">{{ row.ownerPhone }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="设备状态" width="120" align="center">
          <template #default="{ row }">
            <el-tag :type="getTagType('equipmentStatus', row.currentStatus)">
              {{ getStatusLabel('equipmentStatus', row.currentStatus) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="审核状态" width="120" align="center">
          <template #default="{ row }">
            <el-tag :type="getTagType('approvalStatus', row.approveStatus)">
              {{ getStatusLabel('approvalStatus', row.approveStatus) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="位置/能力" min-width="220">
          <template #default="{ row }">
            <div class="primary-cell">
              <span>{{ row.location }}</span>
              <span class="subtle">{{ row.capacityText }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="添加时间" width="170">
          <template #default="{ row }">
            {{ row.createdAt }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button size="small" @click="handleView(row)">查看</el-button>
            <el-button
              v-if="row.approveStatus === 'PENDING'"
              size="small"
              type="primary"
              plain
              @click="handleApprove(row)"
            >
              审核
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

    <el-dialog v-model="approveVisible" title="设备审核" width="460px">
      <el-form :model="approveForm" label-width="90px">
        <el-form-item label="设备名称">
          <span>{{ approveForm.equipmentName }}</span>
        </el-form-item>
        <el-form-item label="审核结果" required>
          <el-radio-group v-model="approveForm.result">
            <el-radio value="PASSED">通过</el-radio>
            <el-radio value="REJECTED">不通过</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="审核意见">
          <el-input
            v-model="approveForm.comment"
            type="textarea"
            :rows="3"
            placeholder="请输入审核意见（选填）"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="approveVisible = false">取消</el-button>
          <el-button type="primary" @click="handleApproveSubmit">提交</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { fetchEquipment } from '../api/admin'
import { getOptions, getStatusLabel, getTagType } from '../utils/status'

interface EquipmentRow {
  id: number
  equipmentName: string
  brandModel: string
  deviceCode: string
  ownerName: string
  ownerPhone: string
  currentStatus: string
  approveStatus: string
  location: string
  capacityText: string
  createdAt: string
}

interface ApproveForm {
  equipmentId: number
  equipmentName: string
  result: string
  comment: string
}

const equipmentStatusOptions = getOptions('equipmentStatus')
const approveStatusOptions = getOptions('approvalStatus')

const rows = ref<EquipmentRow[]>([
  {
    id: 4001,
    equipmentName: '大型拖拉机',
    brandModel: '东方红-1004',
    deviceCode: '苏E-T1004',
    ownerName: '李卫东',
    ownerPhone: '13800138002',
    currentStatus: 'IDLE',
    approveStatus: 'PASSED',
    location: '江苏省苏州市常熟市',
    capacityText: '100 马力，适合整地与旋耕',
    createdAt: '2026-03-25 14:30:00'
  },
  {
    id: 4002,
    equipmentName: '联合收割机',
    brandModel: '雷沃谷神-888',
    deviceCode: '浙A-H888',
    ownerName: '王国强',
    ownerPhone: '13800138005',
    currentStatus: 'BUSY',
    approveStatus: 'PASSED',
    location: '浙江省杭州市临平区',
    capacityText: '每小时 15 亩，跨区作业中',
    createdAt: '2026-03-24 10:15:00'
  },
  {
    id: 4003,
    equipmentName: '播种机',
    brandModel: '农哈哈-6 行',
    deviceCode: '沪B-S006',
    ownerName: '赵国强',
    ownerPhone: '13800138006',
    currentStatus: 'FAULT',
    approveStatus: 'PENDING',
    location: '上海市青浦区',
    capacityText: '适配小麦与玉米播种',
    createdAt: '2026-03-23 16:45:00'
  },
  {
    id: 4004,
    equipmentName: '植保无人机',
    brandModel: '极飞 P100 Pro',
    deviceCode: '苏C-U100',
    ownerName: '周师傅',
    ownerPhone: '13800138004',
    currentStatus: 'PAUSED',
    approveStatus: 'REJECTED',
    location: '江苏省太仓市沙溪镇',
    capacityText: '电池异常，待维修并补充材料',
    createdAt: '2026-03-22 09:50:00'
  }
])

const searchKeyword = ref('')
const searchStatus = ref('')
const searchApproveStatus = ref('')
const currentPage = ref(1)
const pageSize = ref(10)
const approveVisible = ref(false)

const approveForm = reactive<ApproveForm>({
  equipmentId: 0,
  equipmentName: '',
  result: 'PASSED',
  comment: ''
})

const filteredRows = computed(() => {
  const keyword = searchKeyword.value.trim().toLowerCase()

  return rows.value.filter(row => {
    const matchesKeyword =
      !keyword ||
      [
        String(row.id),
        row.equipmentName,
        row.brandModel,
        row.ownerName,
        row.location,
        row.deviceCode
      ].some(value => value.toLowerCase().includes(keyword))

    const matchesStatus = !searchStatus.value || row.currentStatus === searchStatus.value
    const matchesApproveStatus =
      !searchApproveStatus.value || row.approveStatus === searchApproveStatus.value

    return matchesKeyword && matchesStatus && matchesApproveStatus
  })
})

const pagedRows = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value
  return filteredRows.value.slice(start, start + pageSize.value)
})

const summaryCards = computed(() => {
  const idleCount = filteredRows.value.filter(item => item.currentStatus === 'IDLE').length
  const busyCount = filteredRows.value.filter(item => item.currentStatus === 'BUSY').length
  const pendingCount = filteredRows.value.filter(item => item.approveStatus === 'PENDING').length
  const approvedCount = filteredRows.value.filter(item => item.approveStatus === 'PASSED').length

  return [
    { label: '设备总量', value: filteredRows.value.length, desc: '当前筛选结果内设备数' },
    { label: '空闲可调度', value: idleCount, desc: '可优先分配到新需求' },
    { label: '作业中', value: busyCount, desc: '需关注进度与位置' },
    { label: '待审核', value: pendingCount, desc: '建议尽快完成资料审核' },
    { label: '审核通过', value: approvedCount, desc: '已可进入正常调度' }
  ]
})

function handleSearch(): void {
  currentPage.value = 1
  ElMessage.success('已应用筛选条件')
}

function handleReset(): void {
  searchKeyword.value = ''
  searchStatus.value = ''
  searchApproveStatus.value = ''
  currentPage.value = 1
  ElMessage.success('已重置筛选条件')
}

function handleView(row: EquipmentRow): void {
  ElMessageBox.alert(
    `设备 #${row.id}\n名称：${row.equipmentName}\n状态：${getStatusLabel('equipmentStatus', row.currentStatus)}\n审核：${getStatusLabel('approvalStatus', row.approveStatus)}\n能力：${row.capacityText}`,
    '设备详情',
    {
      confirmButtonText: '确定'
    }
  )
}

function handleApprove(row: EquipmentRow): void {
  approveForm.equipmentId = row.id
  approveForm.equipmentName = row.equipmentName
  approveForm.result = 'PASSED'
  approveForm.comment = ''
  approveVisible.value = true
}

function handleApproveSubmit(): void {
  rows.value = rows.value.map(item =>
    item.id === approveForm.equipmentId
      ? {
          ...item,
          approveStatus: approveForm.result
        }
      : item
  )
  approveVisible.value = false
  ElMessage.success('设备审核结果已更新')
}
onMounted(async () => {
  try {
    const data = await fetchEquipment()
    if (Array.isArray(data) && data.length > 0) {
      rows.value = data.map(item => ({
        id: item.id,
        equipmentName: item.equipmentName || '待命名设备',
        brandModel: item.brandModel || '待补充型号',
        deviceCode: item.machineTypeId ? `机型 #${item.machineTypeId}` : '待补充编号',
        ownerName: item.ownerId ? `农机主 #${item.ownerId}` : '待补充农机主',
        ownerPhone: '待补充',
        currentStatus: item.currentStatus || 'IDLE',
        approveStatus: item.approveStatus || 'PENDING',
        location: item.baseRegionCode || '待补充地区',
        capacityText: item.serviceRadiusKm
          ? `服务半径 ${item.serviceRadiusKm} km`
          : '待补充服务能力',
        createdAt: item.createdAt || ''
      }))
    }
  } catch (error) {
    console.warn('加载真实设备列表失败，继续使用演示数据', error)
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

.filter-grid-equipment {
  grid-template-columns: minmax(220px, 2fr) repeat(2, minmax(160px, 1fr)) auto;
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

@media (max-width: 960px) {
  .filter-grid-equipment {
    grid-template-columns: 1fr;
  }

  .page-header,
  .panel-head {
    flex-direction: column;
    align-items: flex-start;
  }
}
</style>
