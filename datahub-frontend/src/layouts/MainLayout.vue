<template>
  <el-container class="main-layout arco-style">
    <!-- Arco 风格顶部导航栏 - 占据整个屏幕宽度 -->
    <el-header class="arco-header">
      <!-- Logo 区域 -->
      <div class="arco-logo">
        <span class="logo-d">D</span>
        <span class="logo-rest">ATAHUB</span>
      </div>
      
      <!-- 右侧工具栏 -->
      <div class="arco-header-right">
        <!-- 搜索框 -->
        <div class="arco-search-box">
          <el-input
            v-model="searchText"
            :placeholder="$t('navbar.search.placeholder')"
            :prefix-icon="Search"
            clearable
            size="small"
            @keyup.enter="handleSearch"
          />
        </div>
        
        <!-- 工具栏图标 -->
        <div class="arco-toolbar-icons">
          <!-- 语言切换 -->
          <el-dropdown @command="handleSelectLanguage">
            <el-tooltip :content="$t('settings.language')">
              <el-icon :size="18">
                <ChatDotRound />
              </el-icon>
            </el-tooltip>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item 
                    v-for="item in LOCALE_OPTIONS" 
                    :key="item.value"
                    :command="item.value"
                    :disabled="currentLocale === item.value"
                  >
                    {{ item.label }}
                    <el-icon v-if="currentLocale === item.value" style="margin-left: 8px">
                      <Check />
                    </el-icon>
                  </el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          
          <!-- 通知 -->
          <el-tooltip :content="$t('navbar.notification')">
            <el-badge :value="3" class="toolbar-badge">
              <el-icon :size="18">
                <Bell />
              </el-icon>
            </el-badge>
          </el-tooltip>
          
          <!-- 主题切换 -->
          <el-tooltip content="暗色模式（开发中）">
            <el-icon @click="toggleTheme" :size="18">
              <Moon />
            </el-icon>
          </el-tooltip>
          
          <!-- 设置 -->
          <el-tooltip :content="$t('navbar.settings')">
            <el-icon :size="18">
              <Setting />
            </el-icon>
          </el-tooltip>
        </div>
        
        <!-- 用户信息 -->
        <el-dropdown>
          <div class="arco-user-info">
            <el-avatar :size="28" src="https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png" />
            <span class="username">管理员</span>
          </div>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item>{{ $t('navbar.userCenter') }}</el-dropdown-item>
              <el-dropdown-item>{{ $t('navbar.settings') }}</el-dropdown-item>
              <el-dropdown-item divided @click="handleLogout">{{ $t('navbar.logout') }}</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </div>
    </el-header>

    <!-- 导航栏下面的区域：左侧菜单 + 主内容 -->
    <el-container class="main-container">
      <!-- 左侧菜单栏 -->
      <el-aside :width="isCollapse ? '64px' : '200px'" class="sidebar">
        <el-menu
          :default-active="activeMenu"
          :collapse="isCollapse"
          :unique-opened="true"
          router
          class="sidebar-menu"
        >
          <template v-for="route in menuItems" :key="route.fullPath">
            <!-- 折叠状态：只显示一级菜单 -->
            <el-menu-item v-if="isCollapse" :index="route.fullPath">
              <el-icon>
                <component :is="route.meta?.icon || 'Menu'" />
              </el-icon>
              <template #title>{{ $t(route.meta?.title as string) }}</template>
            </el-menu-item>
            
            <!-- 展开状态：显示带子菜单的完整菜单 -->
            <template v-else>
              <el-sub-menu
                v-if="route.children.length"
                :index="route.fullPath"
              >
                <template #title>
                  <el-icon>
                    <component :is="route.meta?.icon || 'Menu'" />
                  </el-icon>
                  <span>{{ $t(route.meta?.title as string) }}</span>
                </template>
                <el-menu-item
                  v-for="child in route.children"
                  :key="child.fullPath"
                  :index="child.fullPath"
                >
                  <span>{{ $t(child.meta?.title as string) }}</span>
                </el-menu-item>
              </el-sub-menu>

              <el-menu-item v-else :index="route.fullPath">
                <el-icon>
                  <component :is="route.meta?.icon || 'Menu'" />
                </el-icon>
                <template #title>{{ $t(route.meta?.title as string) }}</template>
              </el-menu-item>
            </template>
          </template>
        </el-menu>
        
        <!-- 折叠按钮 -->
        <div class="collapse-btn" @click="toggleCollapse">
          <el-icon :size="16">
            <Fold v-if="!isCollapse" />
            <Expand v-else />
          </el-icon>
        </div>
      </el-aside>

      <!-- 主内容区域 -->
      <el-main class="arco-main-content">
        <div class="arco-breadcrumb-bar">
          <div class="crumbs">
            <el-icon :size="16" class="crumb-icon">
              <component :is="breadcrumbs[0]?.icon || 'Connection'" />
            </el-icon>
            <span v-for="(b, i) in breadcrumbs" :key="i" class="crumb">
              <span class="crumb-text">{{ b.label }}</span>
              <span v-if="i < breadcrumbs.length - 1" class="crumb-sep">/</span>
            </span>
          </div>
        </div>
        <router-view v-slot="{ Component }">
          <transition name="fade" mode="out-in">
            <component :is="Component" />
          </transition>
        </router-view>
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import type { RouteMeta } from 'vue-router'
import { ElMessage } from 'element-plus'
import { 
  Search,
  Fold,
  Expand,
  ChatDotRound,
  Bell,
  Moon,
  Setting,
  Check
} from '@element-plus/icons-vue'
import useLocale from '@/hooks/useLocale'
import { LOCALE_OPTIONS } from '@/locale'
import { useI18n } from 'vue-i18n'

const router = useRouter()
const route = useRoute()
const { t } = useI18n()

const { currentLocale, changeLocale } = useLocale()

const isCollapse = ref(false)
const searchText = ref('')

const normalizePath = (path: string) => (path.startsWith('/') ? path : `/${path}`)
const joinPaths = (parent: string, child?: string) => {
  const base = normalizePath(parent).replace(/\/$/, '')
  if (!child) return base
  return child.startsWith('/') ? child : `${base}/${child}`
}

const layoutRoutes = computed(() => {
  const routes = router.getRoutes()
  const mainRoute = routes.find(r => r.path === '/' && r.children && r.children.length > 0)
  return (mainRoute?.children || []).filter(child => !child.meta?.hideInMenu)
})

interface MenuChild {
  meta?: RouteMeta
  fullPath: string
}

interface MenuItem {
  meta?: RouteMeta
  fullPath: string
  children: MenuChild[]
}

const menuItems = computed<MenuItem[]>(() =>
  layoutRoutes.value.map(routeRecord => {
    const fullPath = normalizePath(routeRecord.path)
    const children =
      (routeRecord.children || [])
        .filter(child => !child.meta?.hideInMenu)
        .map(child => ({
          meta: child.meta,
          fullPath: joinPaths(fullPath, typeof child.path === 'string' ? child.path : undefined),
        })) || []

    return {
      meta: routeRecord.meta,
      fullPath,
      children,
    }
  }),
)

const activeMenu = computed(() => route.path)

// 当前路由信息
const currentRoute = computed(() => route)

// 判断路由是否激活
const isRouteActive = (path: string) => {
  return route.path === path || route.path.startsWith(path + '/')
}

// 切换侧边栏折叠状态
const toggleCollapse = () => {
  isCollapse.value = !isCollapse.value
}

// 搜索功能
const handleSearch = () => {
  if (searchText.value) {
    ElMessage.info(`搜索: ${searchText.value}`)
  }
}

// 选择语言
const handleSelectLanguage = (locale: string) => {
  changeLocale(locale)
}

// 切换主题
const toggleTheme = () => {
  ElMessage.info('暗色模式功能开发中，敬请期待...')
  // appStore.toggleTheme()
  // ElMessage.success(t('settings.themeChanged'))
}

// 退出登录
const handleLogout = () => {
  ElMessage.success(t('navbar.logoutSuccess'))
  localStorage.removeItem('token')
  router.push('/login')
}

const breadcrumbs = computed(() => {
  const matched = route.matched.filter(r => r.path !== '/')
  return matched.map(r => ({
    label: r.meta?.title ? t(r.meta.title as string) : r.name || r.path,
    icon: r.meta?.icon,
  }))
})
</script>

<style scoped lang="scss">
// Arco 风格的主布局
.main-layout.arco-style {
  height: 100vh;
  display: flex;
  flex-direction: column;
  
  // Arco 风格顶部导航栏 - 占据整个屏幕宽度
  .arco-header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    background: #fff;
    border-bottom: 1px solid #e5e6eb;
    padding: 0 20px;
    height: 60px;
    box-shadow: 0 2px 5px 0 rgba(0, 0, 0, 0.05);
    position: relative;
    z-index: 100;
    
    // Logo 区域
    .arco-logo {
      display: flex;
      align-items: center;
      font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', 'Helvetica Neue', Arial, sans-serif;
      font-weight: 800;
      font-size: 22px;
      letter-spacing: 0.5px;
      cursor: pointer;
      
      .logo-d {
        color: #165dff;
        margin-right: 1px;
      }
      
      .logo-rest {
        color: #1d2129;
      }
    }
    
    // 右侧工具栏
    .arco-header-right {
      display: flex;
      align-items: center;
      gap: 12px;
      
      // 搜索框
      .arco-search-box {
        width: 220px;
        
        :deep(.el-input) {
          .el-input__wrapper {
            background: #f2f3f5;
            box-shadow: none;
            border-radius: 4px;
            height: 32px;
            
            &:hover {
              background: #e5e6eb;
            }
            
            &.is-focus {
              background: #fff;
              box-shadow: 0 0 0 1px #165dff;
            }
          }
          
          .el-input__inner {
            color: #1d2129;
            font-size: 13px;
            
            &::placeholder {
              color: #86909c;
            }
          }
        }
      }
      
      // 工具栏图标
      .arco-toolbar-icons {
        display: flex;
        align-items: center;
        gap: 4px;
        
        :deep(.el-icon) {
          display: inline-flex;
          align-items: center;
          justify-content: center;
          width: 32px;
          height: 32px;
          font-size: 18px;
          color: #4e5969;
          cursor: pointer;
          border-radius: 4px;
          transition: all 0.2s;
        }

        :deep(.el-icon:hover) {
          color: #165dff;
          background: #f2f3f5;
        }

        :deep(.el-icon svg) {
          width: 18px;
          height: 18px;
        }
        
        .toolbar-badge {
          :deep(.el-badge__content) {
            background-color: #f53f3f;
            border: none;
            font-size: 12px;
          }
        }
      }
      
      // 用户信息
      .arco-user-info {
        display: flex;
        align-items: center;
        cursor: pointer;
        padding: 4px 12px;
        border-radius: 4px;
        transition: all 0.2s;
        margin-left: 8px;
        
        &:hover {
          background: #f2f3f5;
        }
        
        .username {
          margin-left: 8px;
          font-size: 14px;
          color: #1d2129;
          font-weight: 400;
        }
      }
    }
  }
  
  // 导航栏下面的容器
  .main-container {
    flex: 1;
    overflow: hidden;
    
    // 左侧菜单栏
    .sidebar {
      background: #fff;
      border-right: 1px solid #e5e6eb;
      transition: width 0.3s;
      overflow: hidden;
      display: flex;
      flex-direction: column;
      position: relative;
      
      .sidebar-menu {
        flex: 1;
        border-right: none;
        background: #fff;
        padding: 8px;
        overflow-y: auto;
        
        :deep(.el-menu-item) {
          color: #4e5969;
          margin-bottom: 4px;
          border-radius: 4px;
          font-size: 14px;
          height: 40px;
          line-height: 40px;
          transition: all 0.2s;
          
          &:hover {
            color: #165dff;
            background: #f2f3f5;
          }
          
          &.is-active {
            color: #165dff;
            background: #e8f3ff;
            font-weight: 500;
          }
          
          .el-icon {
            color: inherit;
            margin-right: 8px;
            font-size: 18px;
          }
        }

        :deep(.el-sub-menu__title) {
          color: #4e5969;
          margin-bottom: 4px;
          border-radius: 4px;
          font-size: 14px;
          height: 40px;
          line-height: 40px;
          transition: all 0.2s;

          &:hover {
            color: #165dff;
            background: #f2f3f5;
          }

          .el-icon {
            color: inherit;
            margin-right: 8px;
            font-size: 18px;
          }
        }

        :deep(.el-sub-menu.is-opened > .el-sub-menu__title) {
          color: #165dff;
          background: #e8f3ff;
          font-weight: 500;
        }

        :deep(.el-sub-menu .el-menu-item) {
          padding-left: 48px !important;
          min-width: auto;
        }
        
        // 折叠状态下的样式
        &.el-menu--collapse {
          padding: 8px 4px;
          
          :deep(.el-menu-item) {
            padding: 0 !important;
            display: flex;
            align-items: center;
            justify-content: center;
            height: 48px;
            
            .el-icon {
              margin-right: 0;
              font-size: 24px;
            }
          }
        }
      }
      
      // 折叠按钮
      .collapse-btn {
        display: flex;
        align-items: center;
        justify-content: center;
        height: 40px;
        border-top: 1px solid #e5e6eb;
        cursor: pointer;
        color: #4e5969;
        transition: all 0.2s;
        
        &:hover {
          color: #165dff;
          background: #f2f3f5;
        }
      }
    }
    
    // 主内容区域
    .arco-main-content {
      background: #f2f3f5;
      padding: 0 16px 16px 16px;
      overflow-y: auto;
      flex: 1;

      .arco-breadcrumb-bar {
        background: transparent;
        border: none;
        margin: 8px 0;
        padding: 0 16px;
        display: flex;
        align-items: center;
        min-height: 30px;
      }

      .crumbs {
        display: inline-flex;
        align-items: center;
        gap: 6px;
        color: #4e5969;
        font-size: 16px;
      }

      .crumb-icon {
        color: #4e5969;
      }

      .crumb-text {
        color: #1d2129;
        font-size: 16px;
        font-weight: 500;
      }

      .crumb-sep {
        color: #86909c;
        margin: 0 4px;
      }
    }
  }
}

// 路由切换动画
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.2s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}
</style>
