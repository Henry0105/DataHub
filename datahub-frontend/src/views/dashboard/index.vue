<template>
  <div class="workplace-container" ref="containerRef">
    <div class="workplace-content" :style="scaleStyle">
    <div class="workplace-wrapper">
      <!-- 左侧主内容区域 -->
      <div class="workplace-left">
        <!-- 欢迎卡片 + 数据统计 + 图表 -->
        <el-card class="overview-card">
          <h2 class="welcome-title">欢迎回来，用户</h2>
          <el-divider />
          
          <!-- 统计指标 -->
          <div class="stats-row">
            <div class="stat-item">
              <div class="stat-icon">
                <el-icon :size="24" color="#165dff"><Document /></el-icon>
              </div>
              <div>
                <div class="stat-title">线上总数据</div>
                <div class="stat-count">{{ stats.datasourceCount * 373.5 | 0 }}<span class="stat-unit">w+</span></div>
              </div>
            </div>
            <el-divider direction="vertical" class="stat-divider" />
            
            <div class="stat-item">
              <div class="stat-icon">
                <el-icon :size="24" color="#722ed1"><Edit /></el-icon>
              </div>
              <div>
                <div class="stat-title">投稿中的内容</div>
                <div class="stat-count">{{ stats.syncTaskCount + 368 }}<span class="stat-unit">篇</span></div>
              </div>
            </div>
            <el-divider direction="vertical" class="stat-divider" />
            
            <div class="stat-item">
              <div class="stat-icon">
                <el-icon :size="24" color="#f77234"><ChatDotRound /></el-icon>
              </div>
              <div>
                <div class="stat-title">日新增评论</div>
                <div class="stat-count">{{ stats.queryCount + 8874 }}<span class="stat-unit">条</span></div>
              </div>
            </div>
            <el-divider direction="vertical" class="stat-divider" />
            
            <div class="stat-item">
              <div class="stat-icon">
                <el-icon :size="24" color="#00b42a"><TrendCharts /></el-icon>
              </div>
              <div>
                <div class="stat-title">转化率增幅</div>
                <div class="stat-count">2.8%<el-icon color="#00b42a" style="margin-left: 8px;"><CaretTop /></el-icon></div>
              </div>
            </div>
          </div>
          
          <el-divider />
          
          <!-- 图表 -->
          <div class="chart-header">
            <div class="chart-title">内容数据<span class="chart-subtitle">(近1年)</span></div>
            <el-link type="primary" :underline="false">查看更多</el-link>
          </div>
          <div ref="chartRef" style="width: 100%; height: 300px;"></div>
        </el-card>
        
        <!-- 下方两个并排卡片 -->
        <el-row :gutter="16" style="margin-top: 16px;" class="equal-cards-row">
          <!-- 热门内容 -->
          <el-col :span="12" class="equal-card">
            <el-card>
              <template #header>
                <div class="card-header">
                  <span class="card-title">热门内容</span>
                  <el-link type="primary" :underline="false">查看更多</el-link>
                </div>
              </template>
              <el-radio-group v-model="contentType" size="small" style="margin-bottom: 12px;">
                <el-radio-button label="text">文本</el-radio-button>
                <el-radio-button label="image">图片</el-radio-button>
                <el-radio-button label="video">视频</el-radio-button>
              </el-radio-group>
              <div class="popular-table">
                <el-table :data="popularContent" style="width: 100%;" size="small">
                  <el-table-column prop="rank" label="排名" width="60" />
                  <el-table-column prop="title" label="内容标题" show-overflow-tooltip />
                  <el-table-column prop="pv" label="浏览量" width="100" />
                  <el-table-column label="较周同比" width="110">
                    <template #default="{ row }">
                      <span :style="{ color: row.increase > 0 ? '#00b42a' : '#f53f3f' }">
                        {{ (row.increase * 100).toFixed(2) }}%
                        <el-icon><component :is="row.increase > 0 ? 'CaretTop' : 'CaretBottom'" /></el-icon>
                      </span>
                    </template>
                  </el-table-column>
                </el-table>
              </div>
              <div class="popular-footer">
                <el-pagination
                  small
                  background
                  layout="prev, pager, next"
                  :total="20"
                  :page-size="5"
                  :current-page="currentPage"
                  @current-change="handlePageChange"
                />
              </div>
            </el-card>
          </el-col>
          
          <!-- 内容占比 -->
          <el-col :span="12" class="equal-card">
            <el-card>
              <template #header>
                <span class="card-title">内容占比</span>
              </template>
              <div class="pie-container">
                <div ref="pieChartRef" class="pie-ref"></div>
              </div>
            </el-card>
          </el-col>
        </el-row>
      </div>
      
      <!-- 右侧侧边栏 -->
      <div class="workplace-right">
        <!-- 快捷入口 -->
        <el-card class="sidebar-equal">
          <template #header>
            <div class="card-header">
              <span class="card-title">快捷入口</span>
              <el-popover
                placement="bottom-end"
                :width="300"
                trigger="click"
              >
                <template #reference>
                  <el-link type="primary" :underline="false">查看更多</el-link>
                </template>
                <div class="more-shortcuts-grid">
                  <div 
                    v-for="item in moreShortcuts" 
                    :key="item.key"
                    class="shortcut-item"
                    @click="goToModule(item.path)"
                  >
                    <div class="shortcut-icon">
                      <el-icon :size="18"><component :is="item.icon" /></el-icon>
                    </div>
                    <div class="shortcut-title">{{ item.title }}</div>
                  </div>
                </div>
              </el-popover>
            </div>
          </template>
          <div class="shortcuts-grid">
            <div 
              v-for="item in shortcuts" 
              :key="item.key"
              class="shortcut-item"
              @click="goToModule(item.path)"
            >
              <div class="shortcut-icon">
                <el-icon :size="18"><component :is="item.icon" /></el-icon>
              </div>
              <div class="shortcut-title">{{ item.title }}</div>
            </div>
          </div>
          <el-divider />
          <div class="recent-title">最近使用</div>
          <div class="shortcuts-grid recent-grid">
            <div 
              v-for="item in recentShortcuts" 
              :key="item.key"
              class="shortcut-item"
              @click="goToModule(item.path)"
            >
              <div class="shortcut-icon">
                <el-icon :size="18"><component :is="item.icon" /></el-icon>
              </div>
              <div class="shortcut-title">{{ item.title }}</div>
            </div>
          </div>
        </el-card>
        
        <!-- 最近访问 -->
        <!-- 公告 -->
        <el-card style="margin-top: 16px;" class="announcement-card">
          <template #header>
            <div class="card-header">
              <span class="card-title">公告</span>
              <el-link type="primary" :underline="false">查看更多</el-link>
            </div>
          </template>
          <div class="announcement-list">
            <div v-for="(item, index) in announcements" :key="index" class="announcement-item">
              <el-tag :type="item.tagType as any" size="small">{{ item.tag }}</el-tag>
              <span class="announcement-content">{{ item.content }}</span>
            </div>
          </div>
        </el-card>
        
        <!-- 文档中心 -->
        <el-card style="margin-top: 16px;">
          <template #header>
            <span class="card-title">文档中心</span>
          </template>
          <div class="docs-list">
            <div v-for="(item, index) in docs" :key="index" class="doc-item">
              <el-icon><Document /></el-icon>
              <span>{{ item }}</span>
            </div>
          </div>
        </el-card>
      </div>
    </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { dashboardApi, type DashboardStats } from '@/api'
import * as echarts from 'echarts'

const router = useRouter()
const chartRef = ref<HTMLElement>()
const pieChartRef = ref<HTMLElement>()
const containerRef = ref<HTMLElement>()
const contentType = ref('text')
const currentPage = ref(1)

// 缩放比例
const scale = ref(1)
const scaleStyle = computed(() => ({
  transform: `scale(${scale.value})`,
  transformOrigin: 'top left',
}))

// 计算缩放比例
const calculateScale = () => {
  if (!containerRef.value) return
  const containerWidth = containerRef.value.clientWidth
  const designWidth = 1400 // 设计稿宽度
  scale.value = containerWidth / designWidth
}

// 统计数据
const stats = ref<DashboardStats>({
  datasourceCount: 0,
  syncTaskCount: 0,
  queryCount: 0,
  datasetCount: 0,
  runningTaskCount: 0,
  successTaskCount: 0,
})

// 加载统计数据
const loadStats = async () => {
  try {
    stats.value = await dashboardApi.getStats()
  } catch (error) {
    console.error('加载统计数据失败:', error)
  }
}

const goToModule = (path: string) => {
  router.push(path)
}

const handlePageChange = (page: number) => {
  currentPage.value = page
}

// 快捷入口
const shortcuts = [
  { key: '1', title: '数据源', icon: 'DataLine', path: '/datasource' },
  { key: '2', title: '数据同步', icon: 'Connection', path: '/dataintegration/sync' },
  { key: '3', title: 'DataCap', icon: 'Coin', path: '/datacap' },
  { key: '4', title: 'DataSophon', icon: 'Platform', path: '/datasophon' },
  { key: '5', title: 'StreamPark', icon: 'VideoPlay', path: '/streampark' },
  { key: '6', title: '数据对比', icon: 'Operation', path: '/compare' },
]

// 更多快捷入口
const moreShortcuts = [
  { key: '7', title: '数据标准', icon: 'Document', path: '/standard' },
  { key: '8', title: '同步历史', icon: 'Clock', path: '/dataintegration/history' },
  { key: '9', title: 'API共享', icon: 'Share', path: '/sqlrest' },
  { key: '10', title: 'BI报表', icon: 'PieChart', path: '/datart' },
  { key: '11', title: '数据质量', icon: 'CircleCheck', path: '/datavines' },
  { key: '12', title: '指标体系', icon: 'TrendCharts', path: '/supersonic' },
]

const recentShortcuts = [
  { key: '1', title: '数据源', icon: 'DataLine', path: '/datasource' },
  { key: '2', title: '数据同步', icon: 'Connection', path: '/dataintegration/sync' },
  { key: '3', title: 'DataCap', icon: 'Coin', path: '/datacap' },
]

// 热门内容
const popularContent = ref([
  { rank: 1, title: '数据宝守护“核”——一线对径工作者', pv: '496.8k', increase: 0.7455 },
  { rank: 2, title: '数据宝守护“核”——一线对径工作者', pv: '493.6k', increase: -1.7863 },
  { rank: 3, title: '“双12”遗泪商家庆祝“电商平台的大促”落幕', pv: '490.4k', increase: 1.3274 },
  { rank: 4, title: '数据宝守护“核”——一线对径工作者', pv: '487.2k', increase: 0.4825 },
  { rank: 5, title: '行业报告：年度内容质量分析', pv: '465.3k', increase: 0.2134 },
])

// 最近访问
const recentVisits = ref([
  { name: '数据源管理', icon: 'Document', time: '5分钟前', color: '#165dff', iconBg: 'rgba(22, 93, 255, 0.1)' },
  { name: '数据库同步', icon: 'DataLine', time: '10分钟前', color: '#722ed1', iconBg: 'rgba(114, 46, 209, 0.1)' },
  { name: '高级管理', icon: 'Management', time: '1小时前', color: '#00b42a', iconBg: 'rgba(0, 180, 42, 0.1)' },
])

// 公告
const announcements = ref([
  { tag: '活动', content: '内容管理机器人活动已开始', tagType: 'danger' },
  { tag: '通知', content: '新版本已上线，请及时测试...', tagType: 'primary' },
  { tag: '通告', content: '1月系统升级计划通告', tagType: 'warning' },
  { tag: '通知', content: '新网站已经通过测试，请查看...', tagType: 'primary' },
  { tag: '活动', content: '数据分析工具优化活动进行中', tagType: 'danger' },
  { tag: '通告', content: '服务器维护将于本周末进行', tagType: 'warning' },
])

// 文档
const docs = ref([
  'DataHub 快速开始',
  '数据源接入指南',
  'Vue 组件开发文档',
  '数据同步最佳实践',
])

// 初始化图表
const initChart = () => {
  if (!chartRef.value) return
  
  const chart = echarts.init(chartRef.value)
  
  const option = {
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      top: '10%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      boundaryGap: false,
      data: ['2025-1', '2025-2', '2025-3', '2025-4', '2025-5', '2025-6', '2025-7', '2025-8', '2025-9', '2025-10', '2025-11', '2025-12'],
      axisLine: {
        lineStyle: {
          color: '#e5e6eb'
        }
      },
      axisLabel: {
        color: '#86909c'
      }
    },
    yAxis: {
      type: 'value',
      axisLine: {
        show: false
      },
      axisTick: {
        show: false
      },
      splitLine: {
        lineStyle: {
          color: '#f2f3f5'
        }
      },
      axisLabel: {
        color: '#86909c',
        formatter: (value: number) => {
          return value >= 1000 ? (value / 1000) + 'k' : value
        }
      }
    },
    series: [
      {
        data: [65000, 55000, 48000, 58000, 62000, 68000, 72000, 70000, 62000, 58000, 64000, 70000],
        type: 'line',
        smooth: true,
        symbol: 'none',
        lineStyle: {
          width: 3,
          color: '#165dff'
        },
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: 'rgba(22, 93, 255, 0.3)' },
            { offset: 1, color: 'rgba(22, 93, 255, 0.05)' }
          ])
        }
      },
      {
        data: [45000, 48000, 52000, 55000, 51000, 48000, 44000, 50000, 56000, 62000, 68000, 65000],
        type: 'line',
        smooth: true,
        symbol: 'none',
        lineStyle: {
          width: 3,
          color: '#722ed1'
        },
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: 'rgba(114, 46, 209, 0.2)' },
            { offset: 1, color: 'rgba(114, 46, 209, 0.03)' }
          ])
        }
      }
    ],
    tooltip: {
      trigger: 'axis',
      backgroundColor: 'rgba(255, 255, 255, 0.95)',
      borderColor: '#e5e6eb',
      borderWidth: 1,
      textStyle: {
        color: '#1d2129'
      }
    }
  }
  
  chart.setOption(option)
  
  // 响应式
  window.addEventListener('resize', () => {
    chart.resize()
  })
}

// 初始化饼图
const initPieChart = () => {
  if (!pieChartRef.value) return
  
  const chart = echarts.init(pieChartRef.value)
  
  const option = {
    tooltip: {
      trigger: 'item',
      formatter: '{b}: {c} ({d%})'
    },
    legend: {
      bottom: '5%',
      left: 'center',
      orient: 'horizontal'
    },
    color: ['#249EFF', '#313CA9', '#21CCFF'],
    series: [
      {
        name: '内容类型',
        type: 'pie',
        radius: ['50%', '70%'],
        center: ['50%', '45%'],
        avoidLabelOverlap: true,
        itemStyle: {
          borderRadius: 4,
          borderColor: '#fff',
          borderWidth: 2
        },
        label: {
          show: true,
          position: 'outside',
          formatter: '{d}%',
          fontSize: 12,
          color: '#86909C'
        },
        emphasis: {
          label: {
            show: true,
            fontSize: 14,
            fontWeight: 'bold'
          }
        },
        labelLine: {
          show: true,
          length: 10,
          length2: 8
        },
        data: [
          { value: 548, name: '视频' },
          { value: 234, name: '图片' },
          { value: 335, name: '文本' }
        ]
      }
    ]
  }
  
  chart.setOption(option)
  
  // 响应式
  setTimeout(() => {
    chart.resize()
  }, 100)
  
  window.addEventListener('resize', () => {
    chart.resize()
  })
}

// 页面加载时初始化
onMounted(() => {
  loadStats()
  initChart()
  initPieChart()
  calculateScale()
  window.addEventListener('resize', calculateScale)
})

onUnmounted(() => {
  window.removeEventListener('resize', calculateScale)
})
</script>

<style scoped lang="scss">
// 完全复制 Arco Design Pro 的样式
.workplace-container {
  width: 100%;
  height: 100%;
  overflow: auto;
  background: var(--el-bg-color-page);
}

.workplace-content {
  width: 1400px;
  padding: 0 8px 8px 8px;
  transition: transform 0.3s ease;
}

.workplace-wrapper {
  display: flex;
  width: 100%;
  gap: 16px;
}

.workplace-left {
  flex: 1;
  min-width: 0;
}

.workplace-right {
  width: 280px;
  flex-shrink: 0;
}

// Overview Card Styles - 完全复制
.overview-card {
  :deep(.el-card__body) {
    padding-top: 12px;
  }
  .welcome-title {
    font-size: 20px;
    font-weight: 500;
    margin: 8px 0 12px;
  }
  
  // 统计指标行
  .stats-row {
    display: flex;
    
    .stat-item {
      display: flex;
      align-items: center;
      padding-left: 20px;
      flex: 1;
      
      .stat-icon {
        display: flex;
        align-items: center;
        justify-content: center;
        width: 54px;
        height: 54px;
        background-color: var(--el-fill-color-light);
        border-radius: 50%;
        margin-right: 12px;
      }
      
      .stat-title {
        font-size: 12px;
        color: var(--el-text-color-regular);
      }
      
      .stat-count {
        font-size: 22px;
        font-weight: 600;
        color: var(--el-text-color-primary);
        
        .stat-unit {
          font-size: 12px;
          font-weight: 400;
          color: var(--el-text-color-secondary);
          margin-left: 8px;
        }
      }
    }
    
    .stat-divider {
      height: 60px;
    }
  }
  
  // 图表头部
  .chart-header {
    display: flex;
    justify-content: space-between;
    margin-bottom: 16px;
    
    .chart-title {
      font-size: 16px;
      font-weight: 500;
      margin: 0;
      
      .chart-subtitle {
        font-size: 12px;
        font-weight: 400;
        margin-left: 4px;
        color: var(--el-text-color-secondary);
      }
    }
  }
}

// Card Header
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  
  .card-title {
    font-size: 16px;
    font-weight: 600;
  }
}

// Shortcuts Grid - 完全复制
.shortcuts-grid {
  display: grid;
  grid-template-columns: 33.33% 33.33% 33.33%;
  grid-row-gap: 8px;
  
  .shortcut-item {
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    padding: 12px 8px;
    box-sizing: border-box;
    cursor: pointer;
    min-height: 80px;
    
    &:hover {
      .shortcut-icon {
        background-color: var(--el-color-primary-light-9);
        
        :deep(.el-icon) {
          color: var(--el-color-primary);
        }
      }
      
      .shortcut-title {
        color: var(--el-color-primary);
      }
    }
    
    .shortcut-icon {
      display: flex;
      justify-content: center;
      align-items: center;
      width: 32px;
      height: 32px;
      border-radius: 6px;
      background-color: var(--el-fill-color-light);
      margin-bottom: 8px;
      
      :deep(.el-icon) {
        font-size: 18px;
      }
    }
    
    .shortcut-title {
      font-size: 12px;
      line-height: 20px;
      color: var(--el-text-color-primary);
      text-align: center;
      word-break: keep-all;
      white-space: nowrap;
    }
  }
}

.recent-title {
  font-weight: 500;
  font-size: 16px;
  line-height: 24px;
  color: var(--el-text-color-primary);
  margin-bottom: 12px;
}

// Recent List
.recent-list {
  .recent-item {
    display: flex;
    align-items: center;
    padding: 8px 0;
    gap: 12px;
    cursor: pointer;
    
    .recent-icon {
      width: 40px;
      height: 40px;
      border-radius: 8px;
      display: flex;
      align-items: center;
      justify-content: center;
      flex-shrink: 0;
    }
    
    .recent-info {
      flex: 1;
      
      .recent-name {
        font-size: 14px;
        color: var(--el-text-color-primary);
        margin-bottom: 4px;
      }
      
      .recent-time {
        font-size: 12px;
        color: var(--el-text-color-secondary);
      }
    }
  }
}

// Announcement List - 完全复制
.announcement-list {
  .announcement-item {
    display: flex;
    align-items: center;
    width: 100%;
    margin-bottom: 8px;
    
    .announcement-content {
      flex: 1;
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
      margin-left: 8px;
      color: var(--el-text-color-secondary);
      text-decoration: none;
      font-size: 13px;
      cursor: pointer;
    }
  }
}

.announcement-card {
  display: flex;
  flex-direction: column;
}

.announcement-card :deep(.el-card__body) {
  flex: 1;
  overflow: auto;
  padding: 12px 20px;
}

// Docs List
.docs-list {
  .doc-item {
    display: flex;
    align-items: center;
    gap: 8px;
    padding: 8px 0;
    font-size: 14px;
    color: var(--el-color-primary);
    cursor: pointer;
    text-decoration: none;
    
    &:hover {
      text-decoration: underline;
    }
  }
}

// 表格增长率符号
:deep(.el-table) {
  .symbol {
    font-size: 10px;
    margin-left: 4px;
    vertical-align: 0;
  }
}

.equal-cards-row {
  display: flex;
  align-items: stretch;
  gap: 16px;
  
  .el-col {
    display: flex;
    flex-direction: column;
    flex: 1;
    min-width: 0;
  }
}

.equal-card :deep(.el-card) {
  display: flex;
  flex-direction: column;
  height: 100%;
}

.equal-card :deep(.el-card__header) {
  padding: 16px 20px;
  border-bottom: 1px solid var(--el-card-border-color);
}

.equal-card :deep(.el-card__body) {
  display: flex;
  flex-direction: column;
  flex: 1;
  padding: 20px;
}

.popular-table {
  flex: 1;
  overflow: auto;
}

.popular-footer {
  margin-top: auto;
  display: flex;
  justify-content: flex-end;
}

.sidebar-equal :deep(.el-card__body) {
  padding-bottom: 16px;
}

.recent-grid {
  padding-bottom: 12px;
}

.pie-container {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 300px;
}

.pie-ref {
  width: 100%;
  height: 300px;
}

.sidebar-equal {
  display: flex;
  flex-direction: column;
}

.sidebar-equal :deep(.el-card__body) {
  display: flex;
  flex-direction: column;
  overflow-y: auto;
}

// 更多快捷入口弹窗
.more-shortcuts-grid {
  display: grid;
  grid-template-columns: 50% 50%;
  grid-row-gap: 8px;
  
  .shortcut-item {
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    padding: 12px 8px;
    box-sizing: border-box;
    cursor: pointer;
    min-height: 80px;
    border-radius: 4px;
    transition: all 0.3s;
    
    &:hover {
      background-color: var(--el-fill-color-light);
      
      .shortcut-icon {
        background-color: var(--el-color-primary-light-9);
        
        :deep(.el-icon) {
          color: var(--el-color-primary);
        }
      }
      
      .shortcut-title {
        color: var(--el-color-primary);
      }
    }
    
    .shortcut-icon {
      display: flex;
      justify-content: center;
      align-items: center;
      width: 32px;
      height: 32px;
      border-radius: 6px;
      background-color: var(--el-fill-color-light);
      margin-bottom: 8px;
      transition: all 0.3s;
      
      :deep(.el-icon) {
        font-size: 18px;
      }
    }
    
    .shortcut-title {
      font-size: 12px;
      line-height: 20px;
      color: var(--el-text-color-primary);
      text-align: center;
      word-break: keep-all;
      white-space: nowrap;
      transition: all 0.3s;
    }
  }
}
</style>
