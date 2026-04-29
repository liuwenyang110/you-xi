<template>
  <div class="page-shell">
    <section class="page-header">
      <div>
        <h1>用户管理</h1>
        <p>统一展示账号状态、角色结构与近期注册情况，便于运营处置。</p>
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
            <p>按角色、账号状态和关键词快速定位用户。</p>
          </div>
          <el-tag type="info">共 {{ filteredRows.length }} 人</el-tag>
        </div>
      </template>

      <div class="filter-grid filter-grid-user">
        <el-input v-model="searchKeyword" placeholder="搜索用户ID、手机号、姓名、地区" clearable />
        <el-select v-model="searchRole" placeholder="用户角色" clearable>
          <el-option
            v-for="item in roleOptions"
            :key="item.value"
            :label="item.label"
            :value="item.value"
          />
        </el-select>
        <el-select v-model="searchStatus" placeholder="账号状态" clearable>
          <el-option
            v-for="item in userStatusOptions"
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
        <el-tag effect="plain" type="danger">受限账号需谨慎复核</el-tag>
        <el-tag effect="plain" type="primary">农机主优先关注实名、设备与履约能力</el-tag>
        <el-tag effect="plain" type="warning">待审核账号建议尽快补齐资料</el-tag>
        <el-tag effect="plain" type="info">当前为前端演示数据，无后端持久化</el-tag>
      </div>
    </el-card>

    <el-card shadow="never" class="panel-card">
      <template #header>
        <div class="panel-head">
          <div>
            <h2>用户列表</h2>
            <p>强化基本档案、角色状态与认证情况展示。</p>
          </div>
        </div>
      </template>

      <el-empty
        v-if="pagedRows.length === 0"
        description="暂无符合条件的用户，请调整筛选条件后重试。"
      />
      <el-table v-else :data="pagedRows" border stripe table-layout="auto">
        <el-table-column label="账号信息" min-width="220">
          <template #default="{ row }">
            <div class="primary-cell">
              <strong>#{{ row.id }} {{ row.name }}</strong>
              <span>{{ row.phone }}</span>
              <span class="subtle">{{ row.region }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="角色" width="120" align="center">
          <template #default="{ row }">
            <el-tag :type="getTagType('userRole', row.role)">
              {{ getStatusLabel('userRole', row.role) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="账号状态" width="120" align="center">
          <template #default="{ row }">
            <el-tag :type="getTagType('userStatus', row.status)">
              {{ getStatusLabel('userStatus', row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="认证与履约" min-width="220">
          <template #default="{ row }">
            <div class="primary-cell">
              <span>{{ row.verifyText }}</span>
              <span class="subtle">{{ row.statsText }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="最近活跃" width="170">
          <template #default="{ row }">
            {{ row.lastActiveAt }}
          </template>
        </el-table-column>
        <el-table-column label="注册时间" width="170">
          <template #default="{ row }">
            {{ row.createdAt }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button size="small" @click="handleView(row)">查看</el-button>
            <el-button
              size="small"
              :type="row.status === 'ACTIVE' ? 'danger' : 'success'"
              plain
              @click="handleToggleStatus(row)"
            >
              {{ row.status === 'ACTIVE' ? '禁用' : '启用' }}
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

    <el-dialog v-model="detailVisible" title="用户详情" width="560px">
      <el-descriptions :column="1" border>
        <el-descriptions-item label="用户ID">{{ currentUser.id }}</el-descriptions-item>
        <el-descriptions-item label="手机号">{{ currentUser.phone }}</el-descriptions-item>
        <el-descriptions-item label="姓名">{{ currentUser.name }}</el-descriptions-item>
        <el-descriptions-item label="角色">
          {{ getStatusLabel('userRole', currentUser.role) }}
        </el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="getTagType('userStatus', currentUser.status)">
            {{ getStatusLabel('userStatus', currentUser.status) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="地区">{{ currentUser.region }}</el-descriptions-item>
        <el-descriptions-item label="认证信息">{{ currentUser.verifyText }}</el-descriptions-item>
        <el-descriptions-item label="运营备注">{{ currentUser.statsText }}</el-descriptions-item>
        <el-descriptions-item label="注册时间">{{ currentUser.createdAt }}</el-descriptions-item>
      </el-descriptions>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="detailVisible = false">关闭</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { fetchUsers } from '../api/admin'
import { getOptions, getStatusLabel, getTagType } from '../utils/status'

interface UserRow {
  id: number
  phone: string
  name: string
  role: string
  status: string
  region: string
  verifyText: string
  statsText: string
  lastActiveAt: string
  createdAt: string
}

const roleOptions = getOptions('userRole')
const userStatusOptions = getOptions('userStatus')

const rows = ref<UserRow[]>([
  {
    id: 3001,
    phone: '13800138001',
    name: '张建国',
    role: 'FARMER',
    status: 'ACTIVE',
    region: '江苏省常熟市梅李镇',
    verifyText: '实名已完成，需求发布 12 次',
    statsText: '近 30 天下单 3 次，最近一次评价为 5 星',
    lastActiveAt: '2026-04-02 19:20:00',
    createdAt: '2026-03-20 09:15:00'
  },
  {
    id: 3002,
    phone: '13800138002',
    name: '李卫东',
    role: 'OWNER',
    status: 'ACTIVE',
    region: '江苏省苏州市吴江区',
    verifyText: '实名与设备认证已完成',
    statsText: '名下 3 台设备，近 30 天完成 5 单',
    lastActiveAt: '2026-04-03 08:10:00',
    createdAt: '2026-03-19 14:30:00'
  },
  {
    id: 3003,
    phone: '13800138003',
    name: '王晓燕',
    role: 'ADMIN',
    status: 'ACTIVE',
    region: '平台运营中心',
    verifyText: '后台管理账号',
    statsText: '负责订单与设备审核',
    lastActiveAt: '2026-04-03 09:00:00',
    createdAt: '2026-03-18 10:20:00'
  },
  {
    id: 3004,
    phone: '13800138004',
    name: '周师傅',
    role: 'OWNER',
    status: 'DISABLED',
    region: '江苏省太仓市沙溪镇',
    verifyText: '实名认证完成，设备资料待补充',
    statsText: '近 7 天有 1 次投诉，账号已临时禁用',
    lastActiveAt: '2026-03-29 12:00:00',
    createdAt: '2026-03-16 13:05:00'
  },
  {
    id: 3005,
    phone: '13800138005',
    name: '陈阿姨',
    role: 'FARMER',
    status: 'PENDING',
    region: '江苏省苏州市昆山市巴城镇',
    verifyText: '实名资料待审核',
    statsText: '刚注册，等待补齐身份信息与地块信息',
    lastActiveAt: '2026-04-03 07:45:00',
    createdAt: '2026-04-03 07:20:00'
  }
])

const searchKeyword = ref('')
const searchRole = ref('')
const searchStatus = ref('')
const currentPage = ref(1)
const pageSize = ref(10)
const detailVisible = ref(false)

const currentUser = reactive<UserRow>({
  id: 0,
  phone: '',
  name: '',
  role: '',
  status: '',
  region: '',
  verifyText: '',
  statsText: '',
  lastActiveAt: '',
  createdAt: ''
})

const filteredRows = computed(() => {
  const keyword = searchKeyword.value.trim().toLowerCase()

  return rows.value.filter(row => {
    const matchesKeyword =
      !keyword ||
      [String(row.id), row.phone, row.name, row.region].some(value =>
        value.toLowerCase().includes(keyword)
      )

    const matchesRole = !searchRole.value || row.role === searchRole.value
    const matchesStatus = !searchStatus.value || row.status === searchStatus.value

    return matchesKeyword && matchesRole && matchesStatus
  })
})

const pagedRows = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value
  return filteredRows.value.slice(start, start + pageSize.value)
})

const summaryCards = computed(() => {
  const ownerCount = filteredRows.value.filter(item => item.role === 'OWNER').length
  const farmerCount = filteredRows.value.filter(item => item.role === 'FARMER').length
  const disabledCount = filteredRows.value.filter(item => item.status === 'DISABLED').length
  const pendingCount = filteredRows.value.filter(item => item.status === 'PENDING').length
  const activeCount = filteredRows.value.filter(item => item.status === 'ACTIVE').length

  return [
    { label: '用户总量', value: filteredRows.value.length, desc: '当前筛选结果内账号数' },
    { label: '农户账号', value: farmerCount, desc: '需求发布与服务采购主体' },
    { label: '农机主账号', value: ownerCount, desc: '供给侧核心履约主体' },
    { label: '正常账号', value: activeCount, desc: '可正常登录与操作' },
    { label: '待审核账号', value: pendingCount, desc: '建议尽快完成资料审核' },
    { label: '受限账号', value: disabledCount, desc: '需运营复核后再启用' }
  ]
})

function handleSearch(): void {
  currentPage.value = 1
  ElMessage.success('已应用筛选条件')
}

function handleReset(): void {
  searchKeyword.value = ''
  searchRole.value = ''
  searchStatus.value = ''
  currentPage.value = 1
  ElMessage.success('已重置筛选条件')
}

function handleView(row: UserRow): void {
  Object.assign(currentUser, row)
  detailVisible.value = true
}

function handleToggleStatus(row: UserRow): void {
  const nextStatus = row.status === 'ACTIVE' ? 'DISABLED' : 'ACTIVE'
  const actionText = nextStatus === 'ACTIVE' ? '启用' : '禁用'

  ElMessageBox.confirm(`确定要${actionText}用户 ${row.name} 吗？`, '账号状态调整', {
    type: 'warning'
  })
    .then(() => {
      rows.value = rows.value.map(item =>
        item.id === row.id ? { ...item, status: nextStatus } : item
      )
      if (currentUser.id === row.id) {
        currentUser.status = nextStatus
      }
      ElMessage.success(`已${actionText}用户账号`)
    })
    .catch(() => undefined)
}
onMounted(async () => {
  try {
    const data = await fetchUsers()
    if (Array.isArray(data) && data.length > 0) {
      rows.value = data.map(item => ({
        id: item.id,
        phone: item.phone || '待补充',
        name: item.id ? `用户 #${item.id}` : '待命名用户',
        role: item.currentRole || item.primaryRole || 'FARMER',
        status: item.status || 'PENDING',
        region: '待补充地区',
        verifyText: item.status === 'PENDING' ? '待审核账号' : '账号信息已建立',
        statsText: item.currentRole ? `当前角色：${item.currentRole}` : '待补充运营信息',
        lastActiveAt: item.updatedAt || item.createdAt || '',
        createdAt: item.createdAt || ''
      }))
    }
  } catch (error) {
    console.warn('加载真实用户列表失败，继续使用演示数据', error)
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

.filter-grid-user {
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
  .filter-grid-user {
    grid-template-columns: 1fr;
  }

  .page-header,
  .panel-head {
    flex-direction: column;
    align-items: flex-start;
  }
}
</style>
