<template>
  <div class="admin-layout">
    <aside class="sidebar" :class="{ collapsed: isCollapsed }">
      <div class="sidebar-header">
        <div class="sidebar-brand-mark">NZ</div>
        <div v-show="!isCollapsed" class="sidebar-brand-copy">
          <strong>农助公益后台</strong>
          <span>Governance Console</span>
        </div>
      </div>

      <el-menu
        :default-active="currentRoute"
        :collapse="isCollapsed"
        router
        class="sidebar-menu"
        background-color="#0f1f1a"
        text-color="#a7c3b5"
        active-text-color="#f4fff6"
      >
        <div v-show="!isCollapsed" class="menu-group-label">公益治理</div>
        <el-menu-item index="/">
          <template #title>公益总览</template>
        </el-menu-item>
        <el-menu-item index="/governance/conversations">
          <template #title>会话治理</template>
        </el-menu-item>
        <el-menu-item index="/governance/content">
          <template #title>内容治理</template>
        </el-menu-item>
        <el-menu-item index="/governance/service-quality">
          <template #title>服务质量</template>
        </el-menu-item>

        <div v-show="!isCollapsed" class="menu-group-label">运营数据</div>
        <el-menu-item index="/demands">
          <template #title>需求管理</template>
        </el-menu-item>
        <el-menu-item index="/orders">
          <template #title>订单管理</template>
        </el-menu-item>
        <el-menu-item index="/users">
          <template #title>用户管理</template>
        </el-menu-item>
        <el-menu-item index="/equipment">
          <template #title>设备管理</template>
        </el-menu-item>

        <div v-show="!isCollapsed" class="menu-group-label">系统巡检</div>
        <el-sub-menu index="system">
          <template #title>
            <span>系统管理</span>
          </template>
          <el-menu-item index="/system/config">系统配置</el-menu-item>
          <el-menu-item index="/system/audit">操作日志</el-menu-item>
          <el-menu-item index="/system/report">数据报表</el-menu-item>
        </el-sub-menu>
      </el-menu>
    </aside>

    <div class="main-area">
      <header class="topbar">
        <div class="topbar-left">
          <el-button text @click="isCollapsed = !isCollapsed">
            {{ isCollapsed ? '展开' : '收起' }}
          </el-button>
          <div class="topbar-title">
            <strong>{{ currentMeta.title || '公益总览' }}</strong>
            <span>纯公益撮合 · 信息联络与治理留痕</span>
          </div>
        </div>

        <div class="topbar-right">
          <el-tag type="success">公益优先</el-tag>
          <el-tag type="info" effect="plain">管理端</el-tag>
        </div>
      </header>

      <main class="page-content">
        <router-view />
      </main>
    </div>
  </div>
</template>

<script setup>
import { computed, ref } from 'vue'
import { useRoute } from 'vue-router'

const route = useRoute()
const isCollapsed = ref(false)

const currentRoute = computed(() => route.path)
const currentMeta = computed(() => route.meta || {})
</script>

<style scoped>
.admin-layout {
  display: flex;
  min-height: 100vh;
  background: #eaf1eb;
}

.sidebar {
  width: 248px;
  background:
    radial-gradient(circle at top, rgba(93, 170, 105, 0.18), transparent 24%),
    linear-gradient(180deg, #0f1f1a 0%, #102720 100%);
  color: #f4fff6;
  border-right: 1px solid rgba(255, 255, 255, 0.06);
  transition: width 0.28s ease;
  flex-shrink: 0;
}

.sidebar.collapsed {
  width: 72px;
}

.sidebar-header {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 18px 16px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.08);
}

.sidebar-brand-mark {
  width: 38px;
  height: 38px;
  border-radius: 14px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  font-weight: 800;
  color: #103221;
  background: linear-gradient(135deg, #d6f3d9 0%, #82d98a 100%);
}

.sidebar-brand-copy {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.sidebar-brand-copy strong {
  font-size: 15px;
}

.sidebar-brand-copy span {
  font-size: 11px;
  color: #9ebaaa;
}

.sidebar-menu {
  border-right: none;
  padding: 12px 8px 18px;
}

.menu-group-label {
  padding: 14px 12px 8px;
  font-size: 11px;
  color: #6f8f7e;
  text-transform: uppercase;
  letter-spacing: 0.08em;
}

:deep(.el-menu-item),
:deep(.el-sub-menu__title) {
  margin: 4px 0;
  border-radius: 12px;
}

:deep(.el-menu-item:hover),
:deep(.el-sub-menu__title:hover) {
  background: rgba(130, 217, 138, 0.1) !important;
}

:deep(.el-menu-item.is-active) {
  background: linear-gradient(
    90deg,
    rgba(130, 217, 138, 0.2),
    rgba(130, 217, 138, 0.08)
  ) !important;
}

.main-area {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
}

.topbar {
  display: flex;
  justify-content: space-between;
  gap: 16px;
  align-items: center;
  padding: 14px 20px;
  background: rgba(255, 255, 255, 0.82);
  backdrop-filter: blur(14px);
  border-bottom: 1px solid rgba(24, 48, 40, 0.08);
  position: sticky;
  top: 0;
  z-index: 10;
}

.topbar-left,
.topbar-right {
  display: flex;
  align-items: center;
  gap: 12px;
}

.topbar-title {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.topbar-title strong {
  color: #173126;
}

.topbar-title span {
  font-size: 12px;
  color: #698173;
}

.page-content {
  flex: 1;
  min-height: 0;
  overflow: auto;
}

@media (max-width: 960px) {
  .sidebar {
    width: 88px;
  }

  .sidebar-brand-copy,
  .menu-group-label {
    display: none;
  }

  .topbar {
    padding: 12px 16px;
    align-items: flex-start;
    flex-direction: column;
  }
}
</style>
