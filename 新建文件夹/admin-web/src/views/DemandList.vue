<template>
  <div class="page-shell">
    <section class="page-header">
      <div>
        <h1>需求管理</h1>
        <p>聚焦待匹配、匹配中和异常需求，便于运营快速跟进。</p>
      </div>
      <el-button type="primary" @click="handleCreate">新增需求</el-button>
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
            <p>支持按状态、作物和关键词快速定位需求。</p>
          </div>
          <el-tag type="info">共 {{ filteredRows.length }} 条</el-tag>
        </div>
      </template>

      <div class="filter-grid">
        <el-input v-model="searchKeyword" placeholder="搜索需求编号、地点、农户" clearable />
        <el-select v-model="searchStatus" placeholder="需求状态" clearable>
          <el-option
            v-for="item in demandStatusOptions"
            :key="item.value"
            :label="item.label"
            :value="item.value"
          />
        </el-select>
        <el-select v-model="searchCropCode" placeholder="作物类型" clearable>
          <el-option
            v-for="item in cropOptions"
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
        <el-tag effect="plain" type="warning">待匹配优先处理</el-tag>
        <el-tag effect="plain" type="primary">匹配中需关注时效</el-tag>
        <el-tag effect="plain" type="info">演示数据为前端本地数据</el-tag>
      </div>
    </el-card>

    <el-card shadow="never" class="panel-card">
      <template #header>
        <div class="panel-head">
          <div>
            <h2>需求列表</h2>
            <p>展示地点、作业信息、匹配状态与创建时间，提升运营判读效率。</p>
          </div>
        </div>
      </template>

      <el-empty
        v-if="pagedRows.length === 0"
        description="当前筛选条件下暂无需求数据，请调整筛选条件后重试。"
      />
      <el-table v-else :data="pagedRows" border stripe table-layout="auto">
        <el-table-column label="需求信息" min-width="220">
          <template #default="{ row }">
            <div class="primary-cell">
              <strong>#{{ row.id }}</strong>
              <span>{{ row.farmerName }}</span>
              <span class="subtle">{{ row.phone }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="作业地点" min-width="220">
          <template #default="{ row }">
            <div class="primary-cell">
              <span>{{ row.villageName }}</span>
              <span class="subtle">{{ row.townshipName }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="作业内容" min-width="220">
          <template #default="{ row }">
            <div class="primary-cell">
              <span
                >{{ getCropLabel(row.cropCode) }} / {{ getScheduleLabel(row.scheduleType) }}</span
              >
              <span class="subtle">面积 {{ row.areaMu }} 亩，期望 {{ row.expectedDate }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="120" align="center">
          <template #default="{ row }">
            <el-tag :type="getTagType('demandStatus', row.status)">
              {{ getStatusLabel('demandStatus', row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="备注说明" min-width="200">
          <template #default="{ row }">
            <span>{{ row.remark || '暂无补充说明' }}</span>
          </template>
        </el-table-column>
        <el-table-column label="创建时间" width="170">
          <template #default="{ row }">
            {{ row.createdAt }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button size="small" @click="handleView(row)">查看</el-button>
            <el-button
              v-if="canCancelDemand(row.status)"
              size="small"
              type="danger"
              plain
              @click="handleCancel(row)"
            >
              取消
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

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="640px">
      <el-form :model="form" label-width="100px">
        <el-form-item label="作业地点" required>
          <el-input v-model="form.villageName" placeholder="请输入作业地点" />
        </el-form-item>
        <el-form-item label="作物类型" required>
          <el-select v-model="form.cropCode" placeholder="请选择作物类型">
            <el-option
              v-for="item in cropOptions"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="面积(亩)" required>
          <el-input-number v-model="form.areaMu" :min="1" :max="10000" />
        </el-form-item>
        <el-form-item label="作业类型" required>
          <el-select v-model="form.scheduleType" placeholder="请选择作业类型">
            <el-option
              v-for="item in scheduleOptions"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleSubmit">提交</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { fetchDemands } from '../api/admin'
import { getOptions, getStatusLabel, getTagType } from '../utils/status'

interface DemandRow {
  id: number
  villageName: string
  townshipName: string
  farmerName: string
  phone: string
  cropCode: string
  areaMu: number
  scheduleType: string
  expectedDate: string
  status: string
  remark: string
  createdAt: string
}

interface DemandForm {
  villageName: string
  cropCode: string
  areaMu: number
  scheduleType: string
}

const cropOptions = [
  { label: '水稻', value: 'RICE' },
  { label: '小麦', value: 'WHEAT' },
  { label: '玉米', value: 'CORN' },
  { label: '油菜', value: 'RAPESEED' },
  { label: '蔬菜', value: 'VEGETABLE' }
]

const scheduleOptions = [
  { label: '整地', value: 'LAND_PREPARATION' },
  { label: '播种', value: 'SOWING' },
  { label: '施肥', value: 'FERTILIZATION' },
  { label: '植保', value: 'PLANT_PROTECTION' },
  { label: '收割', value: 'HARVESTING' }
]

const demandStatusOptions = getOptions('demandStatus')

const rows = ref<DemandRow[]>([
  {
    id: 1001,
    villageName: '东河村 3 组',
    townshipName: '常熟市梅李镇',
    farmerName: '张建国',
    phone: '13800138001',
    cropCode: 'RICE',
    areaMu: 50,
    scheduleType: 'LAND_PREPARATION',
    expectedDate: '2026-04-05',
    status: 'PUBLISHED',
    remark: '临近插秧窗口，优先安排 100 马力以上拖拉机。',
    createdAt: '2026-03-28 10:30:00'
  },
  {
    id: 1002,
    villageName: '虹桥片区 2 号田',
    townshipName: '昆山市巴城镇',
    farmerName: '王德福',
    phone: '13800138002',
    cropCode: 'WHEAT',
    areaMu: 30,
    scheduleType: 'SOWING',
    expectedDate: '2026-04-03',
    status: 'MATCHING',
    remark: '需播种机和随车人员一起进场。',
    createdAt: '2026-03-27 14:20:00'
  },
  {
    id: 1003,
    villageName: '苏州示范点 A 区',
    townshipName: '吴江区震泽镇',
    farmerName: '刘阿明',
    phone: '13800138003',
    cropCode: 'CORN',
    areaMu: 80,
    scheduleType: 'HARVESTING',
    expectedDate: '2026-03-26',
    status: 'COMPLETED',
    remark: '已完成验收，等待归档。',
    createdAt: '2026-03-25 09:15:00'
  },
  {
    id: 1004,
    villageName: '北联村西侧地块',
    townshipName: '太仓市沙溪镇',
    farmerName: '赵明海',
    phone: '13800138004',
    cropCode: 'RAPESEED',
    areaMu: 26,
    scheduleType: 'PLANT_PROTECTION',
    expectedDate: '2026-04-06',
    status: 'CANCELLED',
    remark: '农户调整计划，需求已撤销。',
    createdAt: '2026-03-24 16:00:00'
  }
])

const searchKeyword = ref('')
const searchStatus = ref('')
const searchCropCode = ref('')
const currentPage = ref(1)
const pageSize = ref(10)
const dialogVisible = ref(false)
const dialogTitle = ref('新增需求')

const form = reactive<DemandForm>({
  villageName: '',
  cropCode: '',
  areaMu: 1,
  scheduleType: ''
})

const cropLabelMap = cropOptions.reduce<Record<string, string>>(
  (map, item) => ({
    ...map,
    [item.value]: item.label
  }),
  {}
)

const scheduleLabelMap = scheduleOptions.reduce<Record<string, string>>(
  (map, item) => ({
    ...map,
    [item.value]: item.label
  }),
  {}
)

const filteredRows = computed(() => {
  const keyword = searchKeyword.value.trim().toLowerCase()

  return rows.value.filter(row => {
    const matchesKeyword =
      !keyword ||
      [String(row.id), row.villageName, row.townshipName, row.farmerName, row.phone].some(value =>
        value.toLowerCase().includes(keyword)
      )

    const matchesStatus = !searchStatus.value || row.status === searchStatus.value
    const matchesCrop = !searchCropCode.value || row.cropCode === searchCropCode.value

    return matchesKeyword && matchesStatus && matchesCrop
  })
})

const pagedRows = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value
  return filteredRows.value.slice(start, start + pageSize.value)
})

const summaryCards = computed(() => {
  const publishedCount = filteredRows.value.filter(item => item.status === 'PUBLISHED').length
  const matchingCount = filteredRows.value.filter(item => item.status === 'MATCHING').length
  const totalArea = filteredRows.value.reduce((sum, item) => sum + item.areaMu, 0)
  const todayFocus = filteredRows.value.filter(item =>
    ['PUBLISHED', 'MATCHING'].includes(item.status)
  ).length

  return [
    { label: '需求总量', value: filteredRows.value.length, desc: '当前筛选结果内需求数' },
    { label: '待匹配', value: publishedCount, desc: '需尽快进入匹配流程' },
    { label: '匹配中', value: matchingCount, desc: '需跟进匹配时效与回访' },
    { label: '作业面积', value: `${totalArea} 亩`, desc: '便于评估运力需求' },
    { label: '今日关注', value: todayFocus, desc: '建议优先处理待匹配与匹配中需求' }
  ]
})

function getCropLabel(value: string): string {
  return cropLabelMap[value] || value
}

function getScheduleLabel(value: string): string {
  return scheduleLabelMap[value] || value
}

function canCancelDemand(status: string): boolean {
  return status === 'PUBLISHED' || status === 'MATCHING'
}

function handleSearch(): void {
  currentPage.value = 1
  ElMessage.success('已应用筛选条件')
}

function handleReset(): void {
  searchKeyword.value = ''
  searchStatus.value = ''
  searchCropCode.value = ''
  currentPage.value = 1
  ElMessage.success('已重置筛选条件')
}

function handleCreate(): void {
  dialogTitle.value = '新增需求'
  form.villageName = ''
  form.cropCode = ''
  form.areaMu = 1
  form.scheduleType = ''
  dialogVisible.value = true
}

function handleView(row: DemandRow): void {
  ElMessageBox.alert(
    `需求 #${row.id}\n农户：${row.farmerName}\n地点：${row.townshipName} · ${row.villageName}\n状态：${getStatusLabel('demandStatus', row.status)}`,
    '需求详情',
    {
      confirmButtonText: '确定'
    }
  )
}

function handleCancel(row: DemandRow): void {
  ElMessageBox.confirm(`确定要取消需求 #${row.id} 吗？`, '取消需求', {
    type: 'warning'
  })
    .then(() => {
      rows.value = rows.value.map(item =>
        item.id === row.id ? { ...item, status: 'CANCELLED' } : item
      )
      ElMessage.success('需求已取消')
    })
    .catch(() => undefined)
}

function handleSubmit(): void {
  dialogVisible.value = false
  ElMessage.success('已保存，当前为前端演示提交')
}
onMounted(async () => {
  try {
    const data = await fetchDemands()
    if (Array.isArray(data) && data.length > 0) {
      rows.value = data.map(item => ({
        id: item.id,
        villageName: item.villageName || '待补充地点',
        townshipName: item.baseRegionCode || '待补充区域',
        farmerName: item.farmerId ? `农户 #${item.farmerId}` : '农户待补充',
        phone: '待补充',
        cropCode: item.cropCode || 'RICE',
        areaMu: Number(item.areaMu || 0),
        scheduleType: item.scheduleType || 'LAND_PREPARATION',
        expectedDate: item.expectedDate || '',
        status: item.status || 'PUBLISHED',
        remark: item.remark || item.statusDescription || '',
        createdAt: item.createdAt || item.publishedAt || ''
      }))
    }
  } catch (error) {
    console.warn('加载真实需求列表失败，继续使用演示数据', error)
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

.summary-card,
.panel-card {
  border-radius: 12px;
}

.summary-card {
  padding: 16px 18px;
  background: #fff;
  border: 1px solid #e5e6eb;
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
}

.filter-grid {
  display: grid;
  grid-template-columns: minmax(220px, 2fr) repeat(2, minmax(160px, 1fr)) auto;
  gap: 12px;
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
  .filter-grid {
    grid-template-columns: 1fr;
  }

  .page-header,
  .panel-head {
    flex-direction: column;
    align-items: flex-start;
  }
}
</style>
