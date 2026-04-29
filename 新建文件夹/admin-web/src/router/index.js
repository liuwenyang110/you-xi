import { createRouter, createWebHistory } from 'vue-router'
import AdminLayout from '../layout/AdminLayout.vue'

export const routes = [
  {
    path: '/',
    component: AdminLayout,
    children: [
      {
        path: '',
        name: 'dashboard',
        component: () => import('../views/Dashboard.vue'),
        meta: { title: '公益总览' }
      },
      {
        path: 'governance/conversations',
        name: 'conversation-governance',
        component: () => import('../views/ConversationGovernance.vue'),
        meta: { title: '会话治理' }
      },
      {
        path: 'governance/content',
        name: 'content-governance',
        component: () => import('../views/ContentGovernance.vue'),
        meta: { title: '内容治理' }
      },
      {
        path: 'governance/service-quality',
        name: 'service-quality',
        component: () => import('../views/ServiceQuality.vue'),
        meta: { title: '服务质量' }
      },
      {
        path: 'demands',
        name: 'demands',
        component: () => import('../views/DemandList.vue'),
        meta: { title: '需求管理' }
      },
      {
        path: 'orders',
        name: 'orders',
        component: () => import('../views/OrderList.vue'),
        meta: { title: '订单管理' }
      },
      {
        path: 'users',
        name: 'users',
        component: () => import('../views/UserList.vue'),
        meta: { title: '用户管理' }
      },
      {
        path: 'equipment',
        name: 'equipment',
        component: () => import('../views/EquipmentList.vue'),
        meta: { title: '设备管理' }
      },
      {
        path: 'system/config',
        name: 'system-config',
        component: () => import('../views/SystemConfig.vue'),
        meta: { title: '系统配置' }
      },
      {
        path: 'system/audit',
        name: 'audit-log',
        component: () => import('../views/AuditLog.vue'),
        meta: { title: '操作日志' }
      },
      {
        path: 'system/report',
        name: 'data-report',
        component: () => import('../views/DataReport.vue'),
        meta: { title: '数据报表' }
      }
    ]
  }
]

export default createRouter({
  history: createWebHistory(),
  routes
})
