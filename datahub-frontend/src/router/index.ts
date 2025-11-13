import { createRouter, createWebHistory, RouteRecordRaw } from 'vue-router'
import NProgress from 'nprogress'
import 'nprogress/nprogress.css'

NProgress.configure({ showSpinner: false })

const routes: RouteRecordRaw[] = [
  {
    path: '/',
    redirect: '/dashboard',
  },
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/login/index.vue'),
    meta: { title: '登录', hideInMenu: true },
  },
  {
    path: '/',
    component: () => import('@/layouts/MainLayout.vue'),
    children: [
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('@/views/dashboard/index.vue'),
        meta: { title: '首页', icon: 'HomeFilled' },
      },
      {
        path: 'datacap',
        name: 'DataCap',
        meta: { title: '数据中枢', icon: 'Coin' },
        children: [
          {
            path: 'ops',
            name: 'DataOps',
            component: () => import('@/views/datasophon/index.vue'),
            meta: { title: '数据运维' },
          },
        ],
      },
      {
        path: 'datasource',
        name: 'DataSource',
        component: () => import('@/views/datasource/index.vue'),
        meta: { title: '数据源', icon: 'DataLine' },
      },
      {
        path: 'dataintegration',
        name: 'DataIntegration',
        meta: { title: '数据集成', icon: 'Connection' },
        children: [
          {
            path: 'sync',
            name: 'DataSync',
            component: () => import('@/views/dbswitch/index.vue'),
            meta: { title: '数据同步' },
          },
          {
            path: 'sync/:id',
            name: 'DataSyncView',
            component: () => import('@/views/dbswitch/index.vue'),
            meta: { title: '数据同步', hideInMenu: true },
          },
          {
            path: 'sync/edit/:id',
            name: 'DataSyncEdit',
            component: () => import('@/views/dbswitch/index.vue'),
            meta: { title: '数据同步', hideInMenu: true },
          },
          {
            path: 'history',
            name: 'DataHistory',
            component: () => import('@/views/dbswitch/history.vue'),
            meta: { title: '同步历史' },
          },
        ],
      },


      {
        path: 'streampark',
        name: 'StreamPark',
        component: () => import('@/views/streampark/index.vue'),
        meta: { title: 'StreamPark', icon: 'VideoPlay' },
      },
      {
        path: 'compare',
        name: 'Compare',
        component: () => import('@/views/compare/index.vue'),
        meta: { title: '数据对比', icon: 'Operation' },
      },
      {
        path: 'standard',
        name: 'Standard',
        component: () => import('@/views/standard/index.vue'),
        meta: { title: '数据标准', icon: 'Document' },
      },
      {
        path: 'sqlrest',
        name: 'SqlRest',
        component: () => import('@/views/sqlrest/index.vue'),
        meta: { title: 'API共享平台', icon: 'Share' },
      },
      {
        path: 'datart',
        name: 'Datart',
        component: () => import('@/views/datart/index.vue'),
        meta: { title: 'BI报表管理', icon: 'PieChart' },
      },
      {
        path: 'datavines',
        name: 'DataVines',
        component: () => import('@/views/datavines/index.vue'),
        meta: { title: '数据质量平台', icon: 'CircleCheck' },
      },
      {
        path: 'kylin',
        name: 'Kylin',
        component: () => import('@/views/kylin/index.vue'),
        meta: { title: '数仓建模平台', icon: 'Grid' },
      },
      {
        path: 'supersonic',
        name: 'SuperSonic',
        component: () => import('@/views/supersonic/index.vue'),
        meta: { title: '指标体系平台', icon: 'TrendCharts' },
      },
      {
        path: 'permission',
        name: 'Permission',
        component: () => import('@/views/permission/index.vue'),
        meta: { title: '数据权限管理', icon: 'Lock' },
      },
      {
        path: 'metadata',
        name: 'Metadata',
        component: () => import('@/views/metadata/index.vue'),
        meta: { title: '数据门户管理', icon: 'FolderOpened' },
      },
      {
        path: 'bisheng',
        name: 'Bisheng',
        component: () => import('@/views/bisheng/index.vue'),
        meta: { title: '大模型应用开发', icon: 'Cpu' },
      },
    ],
  },
  {
    path: '/:pathMatch(.*)*',
    name: 'NotFound',
    component: () => import('@/views/error/404.vue'),
    meta: { title: '404', hideInMenu: true },
  },
]

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes,
})

// 路由守卫
router.beforeEach((to, from, next) => {
  NProgress.start()
  
  // 设置页面标题
  document.title = `${to.meta.title || 'AllData'} - 数据中台`
  
  // 简单的登录验证（实际项目中应该验证token）
  const token = localStorage.getItem('token')
  if (to.path !== '/login' && !token) {
    next('/login')
  } else {
    next()
  }
})

router.afterEach(() => {
  NProgress.done()
})

export default router
