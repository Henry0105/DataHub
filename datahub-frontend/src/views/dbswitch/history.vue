<template>
  <div class="history-container">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="page-title">
        <el-icon :size="20" color="#0052d9"><Clock /></el-icon>
        <span>历史任务</span>
        <span class="page-subtitle">History Tasks</span>
      </div>
    </div>

    <!-- 统计卡片 -->
    <div class="stats-grid">
      <div class="stat-card" v-for="stat in statistics" :key="stat.key">
        <div class="stat-icon" :style="{ background: stat.bgColor }">
          <el-icon :size="24" :color="stat.color">
            <component :is="stat.icon" />
          </el-icon>
        </div>
        <div class="stat-content">
          <div class="stat-value">{{ stat.value }}</div>
          <div class="stat-label">{{ stat.label }}</div>
        </div>
      </div>
    </div>

    <!-- 搜索和筛选区域 -->
    <div class="search-section">
      <el-card shadow="never">
        <div class="search-form">
          <el-row :gutter="16">
            <el-col :span="6">
              <el-input
                v-model="searchForm.taskName"
                placeholder="请输入任务名称"
                clearable
                :prefix-icon="Search"
              />
            </el-col>
            <el-col :span="6">
              <el-select
                v-model="searchForm.status"
                placeholder="任务状态"
                clearable
                style="width: 100%"
              >
                <el-option label="全部状态" value="" />
                <el-option label="运行中" value="running" />
                <el-option label="已完成" value="completed" />
                <el-option label="失败" value="failed" />
                <el-option label="暂停" value="paused" />
              </el-select>
            </el-col>
            <el-col :span="6">
              <el-select
                v-model="searchForm.syncType"
                placeholder="同步类型"
                clearable
                style="width: 100%"
              >
                <el-option label="全部类型" value="" />
                <el-option label="全量同步" value="full" />
                <el-option label="增量同步" value="incremental" />
                <el-option label="结构同步" value="structure" />
              </el-select>
            </el-col>
            <el-col :span="6">
              <el-date-picker
                v-model="searchForm.dateRange"
                type="daterange"
                range-separator="至"
                start-placeholder="开始日期"
                end-placeholder="结束日期"
                style="width: 100%"
              />
            </el-col>
          </el-row>
          <el-row :gutter="16" style="margin-top: 16px;">
            <el-col :span="6">
              <el-select
                v-model="searchForm.dataSource"
                placeholder="数据源"
                clearable
                style="width: 100%"
              >
                <el-option label="全部数据源" value="" />
                <el-option
                  v-for="ds in dataSourceOptions"
                  :key="ds.value"
                  :label="ds.label"
                  :value="ds.value"
                />
              </el-select>
            </el-col>
            <el-col :span="6">
              <el-select
                v-model="searchForm.operator"
                placeholder="操作人"
                clearable
                style="width: 100%"
              >
                <el-option label="全部操作人" value="" />
                <el-option
                  v-for="op in operatorOptions"
                  :key="op.value"
                  :label="op.label"
                  :value="op.value"
                />
              </el-select>
            </el-col>
            <el-col :span="12">
              <div class="search-buttons">
                <el-button type="primary" @click="handleSearch">
                  <el-icon><Search /></el-icon>
                  搜索
                </el-button>
                <el-button @click="handleReset">
                  <el-icon><Refresh /></el-icon>
                  重置
                </el-button>
              </div>
            </el-col>
          </el-row>
        </div>
      </el-card>
    </div>

    <!-- 图表展示区域 -->
    <div class="chart-section">
      <el-row :gutter="16">
        <el-col :span="12">
          <el-card title="任务状态分布" shadow="never">
            <div class="chart-container">
              <div ref="statusChartRef" style="height: 320px;"></div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="12">
          <el-card title="同步类型分布" shadow="never">
            <div class="chart-container">
              <div ref="typeChartRef" style="height: 320px;"></div>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </div>

    <!-- 数据表格 -->
    <div class="table-section">
      <el-card shadow="never">
        <template #header>
          <div class="card-header">
            <span>任务列表</span>
            <div class="header-actions">
              <el-button type="primary" @click="handleExport">
                <el-icon><Download /></el-icon>
                导出
              </el-button>
            </div>
          </div>
        </template>
        
        <el-table
          :data="tableData"
          style="width: 100%"
          v-loading="loading"
          @selection-change="handleSelectionChange"
        >
          <el-table-column type="selection" width="55" />
          <el-table-column prop="id" label="任务ID" width="80" />
          <el-table-column prop="name" label="任务名称" min-width="200">
            <template #default="{ row }">
              <div class="task-name-cell">
                <el-icon :size="16" color="#0052d9"><Operation /></el-icon>
                <span class="task-name">{{ row.name }}</span>
              </div>
            </template>
          </el-table-column>
          <el-table-column prop="sourceName" label="源数据库" min-width="180">
            <template #default="{ row }">
              <div class="db-info">
                <el-tag size="small" :type="getDbTagType(row.sourceType)">
                  {{ row.sourceType }}
                </el-tag>
                <span class="db-name">{{ row.sourceName }}</span>
              </div>
            </template>
          </el-table-column>
          <el-table-column prop="targetName" label="目标数据库" min-width="180">
            <template #default="{ row }">
              <div class="db-info">
                <el-tag size="small" :type="getDbTagType(row.targetType)">
                  {{ row.targetType }}
                </el-tag>
                <span class="db-name">{{ row.targetName }}</span>
              </div>
            </template>
          </el-table-column>
          <el-table-column prop="syncType" label="同步类型" width="100">
            <template #default="{ row }">
              <el-tag size="small" :type="getSyncTypeTag(row.syncType)">
                {{ row.syncType }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="status" label="状态" width="100">
            <template #default="{ row }">
              <span class="status-badge" :class="`status-${getStatusClass(row.status)}`">
                <span class="status-dot"></span>
                {{ getStatusText(row.status) }}
              </span>
            </template>
          </el-table-column>
          <el-table-column prop="progress" label="进度" width="120">
            <template #default="{ row }">
              <el-progress
                :percentage="row.progress || 0"
                :status="getProgressStatus(row.status)"
                :stroke-width="6"
              />
            </template>
          </el-table-column>
          <el-table-column prop="createBy" label="操作人" width="120" />
          <el-table-column prop="createTime" label="创建时间" width="180" />
          <el-table-column label="操作" width="200" fixed="right">
            <template #default="{ row }">
              <div class="action-buttons">
                <el-button type="primary" size="small" link @click="handleView(row)">
                  <el-icon><View /></el-icon>
                  查看
                </el-button>
                <el-button type="warning" size="small" link @click="handleEdit(row)">
                  <el-icon><Edit /></el-icon>
                  编辑
                </el-button>
                <el-button 
                  class="btn-text-danger"
                  link
                  size="small"
                  @click="handleDelete(row)"
                  :disabled="row.status === 'running'"
                >
                  <el-icon><Delete /></el-icon>
                  删除
                </el-button>
              </div>
            </template>
          </el-table-column>
        </el-table>
        
        <div class="pagination-container">
          <el-pagination
            v-model:current-page="pagination.current"
            v-model:page-size="pagination.pageSize"
            :total="pagination.total"
            :page-sizes="[10, 20, 50, 100]"
            layout="total, sizes, prev, pager, next, jumper"
            @size-change="handleSizeChange"
            @current-change="handleCurrentChange"
          />
        </div>
      </el-card>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import * as echarts from 'echarts'
import { syncApi } from '@/api/sync'
import {
  Search,
  Refresh,
  View,
  Edit,
  Delete,
  Download,
  Clock,
  Operation,
} from '@element-plus/icons-vue'

interface SearchForm {
  taskName: string
  status: string
  syncType: string
  dateRange: any
  dataSource: string
  operator: string
}

interface TaskItem {
  id: number
  name: string
  sourceName: string
  sourceType: string
  targetName: string
  targetType: string
  syncType: string
  status: string
  progress: number
  createBy: string
  createTime: string
}

interface Pagination {
  current: number
  pageSize: number
  total: number
}

const router = useRouter()
const loading = ref(false)
const statusChartRef = ref<HTMLElement>()
const typeChartRef = ref<HTMLElement>()
let statusChart: echarts.ECharts | null = null
let typeChart: echarts.ECharts | null = null

const searchForm = reactive<SearchForm>({
  taskName: '',
  status: '',
  syncType: '',
  dateRange: null,
  dataSource: '',
  operator: '',
})

const pagination = reactive<Pagination>({
  current: 1,
  pageSize: 20,
  total: 0,
})

const tableData = ref<TaskItem[]>([])
const selectedRows = ref<TaskItem[]>([])

const fixText = (s: string) => {
  try {
    if (!s) return s
    return decodeURIComponent(escape(s))
  } catch {
    return s
  }
}

// 模拟数据
const statistics = ref([
  { key: 'total', label: '总任务数', value: 0, icon: 'Document', color: '#165DFF', bgColor: '#E8F3FF' },
  { key: 'running', label: '运行中', value: 0, icon: 'VideoPlay', color: '#00B96B', bgColor: '#E6FFFB' },
  { key: 'completed', label: '已完成', value: 0, icon: 'SuccessFilled', color: '#52C41A', bgColor: '#E6FFF9' },
  { key: 'failed', label: '失败', value: 0, icon: 'CircleCloseFilled', color: '#F53F3F', bgColor: '#FFF1F0' },
])

const dataSourceOptions = [
  { value: 'mysql', label: 'MySQL' },
  { value: 'postgresql', label: 'PostgreSQL' },
  { value: 'oracle', label: 'Oracle' },
  { value: 'sqlserver', label: 'SQL Server' },
]

const operatorOptions = [
  { value: 'admin', label: '管理员' },
  { value: 'user1', label: '张三' },
  { value: 'user2', label: '李四' },
  { value: 'user3', label: '王五' },
]

// 获取数据库标签类型
const getDbTagType = (type: string) => {
  const typeMap: Record<string, string> = {
    MySQL: 'primary',
    PostgreSQL: 'success',
    Oracle: 'warning',
    'SQL Server': 'info',
  }
  return typeMap[type] || 'info'
}

// 获取同步类型标签
const getSyncTypeTag = (type: string) => {
  const typeMap: Record<string, string> = {
    全量同步: 'primary',
    增量同步: 'success',
    结构同步: 'warning',
  }
  return typeMap[type] || 'info'
}

// 获取状态样式类名
const getStatusClass = (status: string) => {
  const statusMap: Record<string, string> = {
    running: 'running',
    completed: 'completed',
    failed: 'failed',
    paused: 'paused',
  }
  return statusMap[status] || 'default'
}

// 获取状态文本
const getStatusText = (status: string) => {
  const textMap: Record<string, string> = {
    running: '运行中',
    completed: '已完成',
    failed: '失败',
    paused: '暂停',
  }
  return textMap[status] || status
}

// 获取进度状态
const getProgressStatus = (status: string) => {
  const statusMap: Record<string, string> = {
    running: '',
    completed: 'success',
    failed: 'exception',
  }
  return statusMap[status] || ''
}

// 初始化图表
const initCharts = () => {
  statusChart = echarts.init(statusChartRef.value!)
  typeChart = echarts.init(typeChartRef.value!)
}

const updateCharts = () => {
  const statusCounts: Record<string, number> = { 运行中: 0, 已完成: 0, 失败: 0, 暂停: 0 }
  tableData.value.forEach((t) => {
    const k = getStatusText(t.status)
    if (statusCounts[k] !== undefined) statusCounts[k]++
  })
  const typeCounts: Record<string, number> = { 全量同步: 0, 增量同步: 0, 结构同步: 0 }
  tableData.value.forEach((t) => {
    if (typeCounts[t.syncType] !== undefined) typeCounts[t.syncType]++
  })

  const total = tableData.value.length
  const statusOption = {
    color: ['#2E6EFF', '#4E5FE1', '#F53F3F', '#A0AEC0'],
    tooltip: { trigger: 'item', formatter: '{b}: {d}%' },
    legend: { bottom: 0, icon: 'circle' },
    graphic: {
      type: 'text',
      left: 'center',
      top: '40%',
      style: {
        text: `任务总数\n${total}`,
        textAlign: 'center',
        fill: '#1f2937',
        fontSize: 18,
        fontWeight: 600,
        lineHeight: 26,
      },
    },
    series: [
      {
        name: '任务状态',
        type: 'pie',
        radius: ['58%', '78%'],
        center: ['50%', '45%'],
        label: { show: false },
        labelLine: { show: true, length: 18, length2: 8, lineStyle: { color: '#94a3b8' } },
        itemStyle: { borderColor: '#fff', borderWidth: 2 },
        emphasis: { scale: true, scaleSize: 8 },
        data: [
          { value: statusCounts['运行中'], name: '运行中' },
          { value: statusCounts['已完成'], name: '已完成' },
          { value: statusCounts['失败'], name: '失败' },
          { value: statusCounts['暂停'], name: '暂停' },
        ],
      },
    ],
  }
  const typeOption = {
    tooltip: { trigger: 'axis', axisPointer: { type: 'line' } },
    grid: { left: 24, right: 24, bottom: 24, top: 24, containLabel: true },
    xAxis: {
      type: 'category',
      data: ['全量同步', '增量同步', '结构同步'],
      axisTick: { alignWithLabel: true },
      axisLine: { lineStyle: { color: '#E5E7EB' } },
      axisLabel: { color: '#6B7280' },
    },
    yAxis: {
      type: 'value',
      splitLine: { lineStyle: { color: '#F1F5F9' } },
      axisLine: { show: false },
      axisLabel: { color: '#6B7280' },
    },
    series: [
      {
        name: '任务数量',
        type: 'bar',
        barMaxWidth: 36,
        data: [typeCounts['全量同步'], typeCounts['增量同步'], typeCounts['结构同步']],
        itemStyle: {
          borderRadius: [8, 8, 0, 0],
          color: '#2E6EFF',
        },
      },
    ],
  }
  statusChart && statusChart.setOption(statusOption)
  typeChart && typeChart.setOption(typeOption)
}

// 加载表格数据
const loadTableData = async () => {
  loading.value = true
  try {
    const res = await syncApi.page({ current: pagination.current, size: pagination.pageSize })
    const records = res.records || []
    tableData.value = records.map((r: any) => ({
      id: r.id,
      name: fixText(r.taskName || ''),
      sourceName: r.sourceTable,
      sourceType: 'MySQL',
      targetName: r.targetTable,
      targetType: 'PostgreSQL',
      syncType: r.syncMode === 'incremental' ? '增量同步' : '全量同步',
      status: r.status || 'completed',
      progress: r.progress || 0,
      createBy: '系统',
      createTime: r.createTime,
    }))
    pagination.total = res.total || records.length
    const running = tableData.value.filter((t) => t.status === 'running').length
    const completed = tableData.value.filter((t) => t.status === 'completed' || t.status === 'success').length
    const failed = tableData.value.filter((t) => t.status === 'failed').length
    statistics.value = [
      { key: 'total', label: '总任务数', value: tableData.value.length, icon: 'Document', color: '#2E6EFF', bgColor: '#E8F3FF' },
      { key: 'running', label: '运行中', value: running, icon: 'VideoPlay', color: '#1B73E0', bgColor: '#E8F3FF' },
      { key: 'completed', label: '已完成', value: completed, icon: 'SuccessFilled', color: '#2E6EFF', bgColor: '#E8F3FF' },
      { key: 'failed', label: '失败', value: failed, icon: 'CircleCloseFilled', color: '#6AA7FF', bgColor: '#E8F3FF' },
    ]
    updateCharts()
  } catch (e) {
    loading.value = false
  } finally {
    loading.value = false
  }
}

// 搜索
const handleSearch = () => {
  pagination.current = 1
  loadTableData()
}

// 重置
const handleReset = () => {
  Object.assign(searchForm, {
    taskName: '',
    status: '',
    syncType: '',
    dateRange: null,
    dataSource: '',
    operator: '',
  })
  handleSearch()
}

// 表格选择变化
const handleSelectionChange = (selection: TaskItem[]) => {
  selectedRows.value = selection
}

// 分页大小变化
const handleSizeChange = (size: number) => {
  pagination.pageSize = size
  loadTableData()
}

// 当前页变化
const handleCurrentChange = (page: number) => {
  pagination.current = page
  loadTableData()
}

// 查看详情
const handleView = (row: TaskItem) => {
  router.push(`/dataintegration/sync/${row.id}`)
}

// 编辑
const handleEdit = async (row: TaskItem) => {
  // 跳转到数据同步页面并打开编辑对话框
  router.push({
    path: '/dataintegration/sync',
    query: { edit: row.id }
  })
}

// 删除
const handleDelete = (row: TaskItem) => {
  ElMessageBox.confirm('确定要删除这个任务吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning',
  }).then(async () => {
    try {
      await syncApi.delete(row.id)
      ElMessage.success('删除成功')
      loadTableData()
    } catch (e: any) {
      ElMessage.error('删除失败')
    }
  })
}

// 导出
const handleExport = () => {
  ElMessage.success('导出功能开发中...')
}

onMounted(() => {
  initCharts()
  loadTableData()
})
</script>

<style scoped>
.history-container {
  padding: 20px;
  background: #f5f7fa;
  min-height: calc(100vh - 120px);
}

.page-header {
  margin-bottom: 20px;
}

.page-title {
  display: flex;
  align-items: center;
  gap: 12px;
  font-size: 24px;
  font-weight: 600;
  color: #303133;
}

.page-subtitle {
  font-size: 14px;
  color: #909399;
  font-weight: normal;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 16px;
  margin-bottom: 20px;
}

.stat-card {
  background: #fff;
  border-radius: 8px;
  padding: 20px;
  display: flex;
  align-items: center;
  gap: 16px;
  box-shadow: 0 6px 20px rgba(22, 93, 255, 0.06);
  transition: transform 0.2s ease;
}

.stat-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 20px 0 rgba(0, 0, 0, 0.08);
}

.stat-icon {
  width: 48px;
  height: 48px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.stat-value {
  font-size: 24px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 4px;
}

.stat-label {
  font-size: 14px;
  color: #909399;
}

.search-section {
  margin-bottom: 20px;
}

.search-form {
  padding: 20px;
}

.search-buttons {
  display: flex;
  gap: 12px;
  justify-content: flex-end;
}

.chart-section {
  margin-bottom: 20px;
}

.chart-container {
  padding: 20px;
}

.table-section {
  background: #fff;
  border-radius: 8px;
  overflow: hidden;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-actions {
  display: flex;
  gap: 12px;
}

.task-name-cell {
  display: flex;
  align-items: center;
  gap: 8px;
}

.task-name {
  font-weight: 500;
  color: #303133;
}

.db-info {
  display: flex;
  align-items: center;
  gap: 8px;
}

.db-name {
  color: #606266;
  font-size: 14px;
}

.status-badge {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 4px 12px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 500;
}

.status-running {
  background: #e6fffb;
  color: #00b96b;
}

.status-completed {
  background: #e6fff9;
  color: #52c41a;
}

.status-failed {
  background: #fff1f0;
  color: #ff4d4f;
}

.status-paused {
  background: #fff7e8;
  color: #ff7d00;
}

.status-dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: currentColor;
}

.action-buttons {
  display: flex;
  gap: 8px;
}

.btn-text-danger {
  background: transparent !important;
  color: #ff4d4f !important;
  border: none !important;
  padding: 0 8px !important;
  height: auto !important;
  box-shadow: none !important;
}

.btn-text-danger:hover {
  color: #ff7875 !important;
  background: transparent !important;
}

.btn-text-danger.is-disabled,
.btn-text-danger.is-disabled:hover {
  color: #ffb3b3 !important;
  cursor: not-allowed;
}

:deep(.btn-text-danger .el-icon) {
  color: inherit !important;
  margin-right: 4px;
}

.pagination-container {
  display: flex;
  justify-content: flex-end;
  padding: 20px 0 0 0;
}

:deep(.el-card__header) {
  padding: 16px 20px;
  border-bottom: 1px solid #ebeef5;
}

:deep(.el-card__body) {
  padding: 0;
}
</style>
