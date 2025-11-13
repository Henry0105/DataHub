<template>
  <div class="page-container-tdesign">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="page-title">
        <el-icon :size="20" color="#0052d9"><Coin /></el-icon>
        <span>数据中枢</span>
        <span class="page-subtitle">DataCap</span>
      </div>
      <el-button type="primary" @click="handleNewQuery">
        <el-icon><Plus /></el-icon>
        新建查询
      </el-button>
    </div>

    <!-- 功能说明 -->
    <div class="info-banner">
      <div class="info-item">
        <el-icon color="#0052d9"><Check /></el-icon>
        <span>统一SQL接口访问40+数据源</span>
      </div>
      <div class="info-item">
        <el-icon color="#0052d9"><Check /></el-icon>
        <span>支持数据转换和集成</span>
      </div>
      <div class="info-item">
        <el-icon color="#0052d9"><Check /></el-icon>
        <span>数据可视化分析</span>
      </div>
      <div class="info-item">
        <el-icon color="#0052d9"><Check /></el-icon>
        <span>查询历史管理</span>
      </div>
    </div>

    <!-- 统计卡片 -->
    <div class="stats-container">
      <div class="stat-card">
        <div class="stat-icon" style="background: #e6f0ff;">
          <el-icon :size="24" color="#0052d9"><Connection /></el-icon>
        </div>
        <div class="stat-content">
          <div class="stat-value">{{ stats.datasources }}</div>
          <div class="stat-label">数据源连接</div>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon" style="background: #e6fffb;">
          <el-icon :size="24" color="#00b96b"><Document /></el-icon>
        </div>
        <div class="stat-content">
          <div class="stat-value">{{ stats.queries }}</div>
          <div class="stat-label">查询任务</div>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon" style="background: #fff7e6;">
          <el-icon :size="24" color="#ff9800"><FolderOpened /></el-icon>
        </div>
        <div class="stat-content">
          <div class="stat-value">{{ stats.datasets }}</div>
          <div class="stat-label">数据集</div>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon" style="background: #f0f0ff;">
          <el-icon :size="24" color="#722ed1"><PieChart /></el-icon>
        </div>
        <div class="stat-content">
          <div class="stat-value">{{ stats.reports }}</div>
          <div class="stat-label">可视化报表</div>
        </div>
      </div>
    </div>

    <!-- 标签页 -->
    <div class="tabs-container">
      <el-tabs v-model="activeTab">
        <!-- SQL查询 -->
        <el-tab-pane label="SQL查询" name="query">
          <div class="query-panel">
            <div class="query-editor">
              <div class="editor-header">
                <el-select v-model="selectedDatasource" placeholder="选择数据源" style="width: 250px">
                  <el-option
                    v-for="ds in datasources"
                    :key="ds.id"
                    :label="ds.name"
                    :value="ds.id"
                  >
                    <span>
                      <el-tag size="small" :type="getDbTagType(ds.type)">{{ ds.type }}</el-tag>
                      {{ ds.name }}
                    </span>
                  </el-option>
                </el-select>
                <div class="editor-actions">
                  <el-button type="primary" :icon="CaretRight" @click="handleExecuteQuery">
                    执行查询
                  </el-button>
                  <el-button :icon="DocumentCopy" @click="handleSaveQuery">保存</el-button>
                  <el-button :icon="Delete" @click="handleClearQuery">清空</el-button>
                </div>
              </div>
              <el-input
                v-model="sqlQuery"
                type="textarea"
                :rows="10"
                placeholder="请输入SQL查询语句..."
                class="sql-textarea"
              />
            </div>
            
            <!-- 查询结果 -->
            <div class="query-result" v-if="queryResult.show">
              <div class="result-header">
                <span class="result-title">
                  <el-icon><SuccessFilled /></el-icon>
                  查询结果 ({{ queryResult.rows }} 行，耗时 {{ queryResult.duration }}ms)
                </span>
                <el-button size="small" :icon="Download" @click="handleExport">导出</el-button>
              </div>
              <el-table :data="queryResult.data" border stripe max-height="400">
                <el-table-column
                  v-for="col in queryResult.columns"
                  :key="col"
                  :prop="col"
                  :label="col"
                  min-width="150"
                  show-overflow-tooltip
                />
              </el-table>
            </div>
          </div>
        </el-tab-pane>

        <!-- 查询历史 -->
        <el-tab-pane label="查询历史" name="history">
          <div class="history-panel">
            <div class="search-bar">
              <el-input
                v-model="historySearch"
                placeholder="搜索SQL语句或数据源"
                :prefix-icon="Search"
                clearable
                style="width: 300px"
              />
              <el-button :icon="Refresh" @click="handleRefreshHistory">刷新</el-button>
            </div>
            
            <el-table :data="filteredHistory" border stripe v-loading="historyLoading">
              <el-table-column type="index" label="#" width="60" />
              <el-table-column prop="sql" label="SQL语句" min-width="300" show-overflow-tooltip>
                <template #default="{ row }">
                  <div class="sql-cell">
                    <code>{{ row.sql }}</code>
                  </div>
                </template>
              </el-table-column>
              <el-table-column prop="datasource" label="数据源" width="180">
                <template #default="{ row }">
                  <el-tag size="small" :type="getDbTagType(row.dbType)">{{ row.dbType }}</el-tag>
                  {{ row.datasource }}
                </template>
              </el-table-column>
              <el-table-column prop="status" label="状态" width="100">
                <template #default="{ row }">
                  <el-tag :type="row.status === '成功' ? 'success' : 'danger'" size="small">
                    {{ row.status }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column prop="duration" label="耗时" width="100">
                <template #default="{ row }">
                  {{ row.duration }}ms
                </template>
              </el-table-column>
              <el-table-column prop="rows" label="行数" width="100" />
              <el-table-column prop="time" label="执行时间" width="160" />
              <el-table-column label="操作" width="180" fixed="right">
                <template #default="{ row }">
                  <el-button type="primary" size="small" link @click="handleRerun(row)">
                    <el-icon><Refresh /></el-icon>
                    重新执行
                  </el-button>
                  <el-button type="primary" size="small" link @click="handleCopySQL(row)">
                    <el-icon><DocumentCopy /></el-icon>
                    复制
                  </el-button>
                  <el-button type="danger" size="small" link @click="handleDeleteHistory(row)">
                    <el-icon><Delete /></el-icon>
                    删除
                  </el-button>
        </template>
              </el-table-column>
        </el-table>

            <div class="pagination-container">
              <el-pagination
                v-model:current-page="historyPage"
                v-model:page-size="historyPageSize"
                :page-sizes="[10, 20, 50, 100]"
                :total="historyTotal"
                layout="total, sizes, prev, pager, next, jumper"
              />
            </div>
          </div>
        </el-tab-pane>

        <!-- 数据集管理 -->
        <el-tab-pane label="数据集管理" name="datasets">
          <div class="datasets-panel">
            <div class="search-bar">
              <el-input
                v-model="datasetSearch"
                placeholder="搜索数据集名称"
                :prefix-icon="Search"
                clearable
                style="width: 300px"
              />
              <el-button type="primary" @click="handleCreateDataset">
                <el-icon><Plus /></el-icon>
                创建数据集
              </el-button>
            </div>

            <div class="datasets-grid">
              <div
                v-for="dataset in filteredDatasets"
                :key="dataset.id"
                class="dataset-card"
                @click="handleViewDataset(dataset)"
              >
                <div class="dataset-icon">
                  <el-icon :size="32" color="#0052d9"><FolderOpened /></el-icon>
                </div>
                <div class="dataset-info">
                  <div class="dataset-name">{{ dataset.name }}</div>
                  <div class="dataset-desc">{{ dataset.description }}</div>
                  <div class="dataset-meta">
                    <span><el-icon><Connection /></el-icon> {{ dataset.datasource }}</span>
                    <span><el-icon><Document /></el-icon> {{ dataset.rows }} 行</span>
                    <span><el-icon><Clock /></el-icon> {{ dataset.updateTime }}</span>
                  </div>
                </div>
                <div class="dataset-actions" @click.stop>
                  <el-button type="primary" size="small" link>
                    <el-icon><View /></el-icon>
                  </el-button>
                  <el-button type="primary" size="small" link>
                    <el-icon><Edit /></el-icon>
                  </el-button>
                  <el-button type="danger" size="small" link>
                    <el-icon><Delete /></el-icon>
                  </el-button>
                </div>
              </div>
            </div>
          </div>
        </el-tab-pane>

        <!-- 可视化报表 -->
        <el-tab-pane label="可视化报表" name="reports">
          <div class="reports-panel">
            <div class="search-bar">
              <el-input
                v-model="reportSearch"
                placeholder="搜索报表名称"
                :prefix-icon="Search"
                clearable
                style="width: 300px"
              />
              <el-button type="primary" @click="handleCreateReport">
                <el-icon><Plus /></el-icon>
                创建报表
              </el-button>
            </div>

            <div class="reports-grid">
              <div
                v-for="report in filteredReports"
                :key="report.id"
                class="report-card"
                @click="handleViewReport(report)"
              >
                <div class="report-preview">
                  <el-icon :size="48" color="#722ed1"><PieChart /></el-icon>
                </div>
                <div class="report-info">
                  <div class="report-name">{{ report.name }}</div>
                  <div class="report-type">
                    <el-tag size="small">{{ report.type }}</el-tag>
                  </div>
                  <div class="report-meta">
                    <span><el-icon><User /></el-icon> {{ report.creator }}</span>
                    <span><el-icon><Clock /></el-icon> {{ report.createTime }}</span>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </el-tab-pane>
      </el-tabs>
    </div>

    <!-- 保存查询对话框 -->
    <el-dialog v-model="saveDialogVisible" title="保存查询" width="500px">
      <el-form :model="saveForm" label-width="100px">
        <el-form-item label="查询名称">
          <el-input v-model="saveForm.name" placeholder="请输入查询名称" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input
            v-model="saveForm.description"
            type="textarea"
            :rows="3"
            placeholder="请输入查询描述"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="saveDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleConfirmSave">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Search,
  Refresh,
  Plus,
  CaretRight,
  DocumentCopy,
  Delete,
  Download,
  Clock,
  User,
} from '@element-plus/icons-vue'

// 统计数据
const stats = ref({
  datasources: 42,
  queries: 156,
  datasets: 89,
  reports: 34,
})

// 标签页
const activeTab = ref('query')

// 数据源列表
const datasources = ref([
  { id: 1, name: 'MySQL主库', type: 'MySQL' },
  { id: 2, name: 'Oracle数据仓库', type: 'Oracle' },
  { id: 3, name: 'PostgreSQL分析库', type: 'PostgreSQL' },
  { id: 4, name: '达梦数据库', type: 'DM' },
  { id: 5, name: '人大金仓数据库', type: 'KingBase' },
])

// SQL查询
const selectedDatasource = ref(1)
const sqlQuery = ref('SELECT * FROM users WHERE status = 1 LIMIT 100;')
const queryResult = ref<{
  show: boolean
  data: any[]
  columns: string[]
  rows: number
  duration: number
}>({
  show: false,
  data: [],
  columns: [],
  rows: 0,
  duration: 0,
})

// 查询历史
const historySearch = ref('')
const historyLoading = ref(false)
const historyPage = ref(1)
const historyPageSize = ref(10)
const queryHistory = ref([
  {
    id: 1,
    sql: 'SELECT * FROM users WHERE status = 1',
    datasource: 'MySQL主库',
    dbType: 'MySQL',
    status: '成功',
    duration: 125,
    rows: 1250,
    time: '2025-01-15 10:30:25',
  },
  {
    id: 2,
    sql: 'SELECT COUNT(*) FROM orders WHERE create_time > "2025-01-01"',
    datasource: 'PostgreSQL分析库',
    dbType: 'PostgreSQL',
    status: '成功',
    duration: 89,
    rows: 1,
    time: '2025-01-15 10:28:15',
  },
  {
    id: 3,
    sql: 'SELECT * FROM products WHERE price > 100',
    datasource: 'Oracle数据仓库',
    dbType: 'Oracle',
    status: '成功',
    duration: 256,
    rows: 3420,
    time: '2025-01-15 10:25:10',
  },
  {
    id: 4,
    sql: 'SELECT user_id, SUM(amount) as total FROM orders GROUP BY user_id',
    datasource: 'MySQL主库',
    dbType: 'MySQL',
    status: '失败',
    duration: 0,
    rows: 0,
    time: '2025-01-15 10:20:05',
  },
  {
    id: 5,
    sql: 'SELECT * FROM customers WHERE region = "华东" ORDER BY create_time DESC',
    datasource: '达梦数据库',
    dbType: 'DM',
    status: '成功',
    duration: 178,
    rows: 856,
    time: '2025-01-15 10:15:30',
  },
])

const historyTotal = computed(() => {
  let data = [...queryHistory.value]
  if (historySearch.value) {
    data = data.filter(
      (item) =>
        item.sql.toLowerCase().includes(historySearch.value.toLowerCase()) ||
        item.datasource.toLowerCase().includes(historySearch.value.toLowerCase())
    )
  }
  return data.length
})

const filteredHistory = computed(() => {
  let data = [...queryHistory.value]
  if (historySearch.value) {
    data = data.filter(
      (item) =>
        item.sql.toLowerCase().includes(historySearch.value.toLowerCase()) ||
        item.datasource.toLowerCase().includes(historySearch.value.toLowerCase())
    )
  }
  const start = (historyPage.value - 1) * historyPageSize.value
  const end = start + historyPageSize.value
  return data.slice(start, end)
})

// 数据集
const datasetSearch = ref('')
const datasets = ref([
  {
    id: 1,
    name: '用户行为数据集',
    description: '包含用户浏览、点击、购买等行为数据',
    datasource: 'MySQL主库',
    rows: 125000,
    updateTime: '2025-01-15 10:30',
  },
  {
    id: 2,
    name: '订单统计数据集',
    description: '订单金额、数量、地区等统计数据',
    datasource: 'PostgreSQL分析库',
    rows: 45000,
    updateTime: '2025-01-15 09:20',
  },
  {
    id: 3,
    name: '产品销售数据集',
    description: '产品销量、库存、价格等数据',
    datasource: 'Oracle数据仓库',
    rows: 8900,
    updateTime: '2025-01-14 16:45',
  },
])

const filteredDatasets = computed(() => {
  if (!datasetSearch.value) return datasets.value
  return datasets.value.filter((item) =>
    item.name.toLowerCase().includes(datasetSearch.value.toLowerCase())
  )
})

// 报表
const reportSearch = ref('')
const reports = ref([
  {
    id: 1,
    name: '销售趋势分析',
    type: '折线图',
    creator: '张三',
    createTime: '2025-01-15',
  },
  {
    id: 2,
    name: '用户地区分布',
    type: '饼图',
    creator: '李四',
    createTime: '2025-01-14',
  },
  {
    id: 3,
    name: '产品销量排行',
    type: '柱状图',
    creator: '王五',
    createTime: '2025-01-13',
  },
  {
    id: 4,
    name: '订单金额统计',
    type: '面积图',
    creator: '赵六',
    createTime: '2025-01-12',
  },
])

const filteredReports = computed(() => {
  if (!reportSearch.value) return reports.value
  return reports.value.filter((item) =>
    item.name.toLowerCase().includes(reportSearch.value.toLowerCase())
  )
})

// 保存查询对话框
const saveDialogVisible = ref(false)
const saveForm = ref({
  name: '',
  description: '',
})

// 获取数据库标签类型
const getDbTagType = (type: string) => {
  const map: Record<string, any> = {
    MySQL: 'primary',
    Oracle: 'danger',
    PostgreSQL: 'success',
    DM: 'info',
    KingBase: 'warning',
  }
  return map[type] || ''
}

// 新建查询
const handleNewQuery = () => {
  sqlQuery.value = ''
  queryResult.value.show = false
  activeTab.value = 'query'
  ElMessage.success('已创建新查询')
}

// 执行查询
const handleExecuteQuery = () => {
  if (!sqlQuery.value.trim()) {
    ElMessage.warning('请输入SQL语句')
    return
  }
  
  ElMessage.info('正在执行查询...')
  
  setTimeout(() => {
    queryResult.value = {
      show: true,
      data: [
        { id: 1, name: '张三', age: 28, email: 'zhangsan@example.com', status: '正常' },
        { id: 2, name: '李四', age: 32, email: 'lisi@example.com', status: '正常' },
        { id: 3, name: '王五', age: 25, email: 'wangwu@example.com', status: '正常' },
      ],
      columns: ['id', 'name', 'age', 'email', 'status'],
      rows: 3,
      duration: 125,
    }
    ElMessage.success('查询执行成功')
  }, 500)
}

// 保存查询
const handleSaveQuery = () => {
  if (!sqlQuery.value.trim()) {
    ElMessage.warning('请输入SQL语句')
    return
  }
  saveDialogVisible.value = true
}

// 确认保存
const handleConfirmSave = () => {
  if (!saveForm.value.name) {
    ElMessage.warning('请输入查询名称')
    return
  }
  ElMessage.success('查询已保存')
  saveDialogVisible.value = false
  saveForm.value = { name: '', description: '' }
}

// 清空查询
const handleClearQuery = () => {
  sqlQuery.value = ''
  queryResult.value.show = false
}

// 导出结果
const handleExport = () => {
  ElMessage.success('导出功能开发中...')
}

// 刷新历史
const handleRefreshHistory = () => {
  historyLoading.value = true
  setTimeout(() => {
    historyLoading.value = false
    ElMessage.success('刷新成功')
  }, 500)
}

// 重新执行
const handleRerun = (row: any) => {
  sqlQuery.value = row.sql
  selectedDatasource.value = datasources.value.find((ds) => ds.name === row.datasource)?.id || 1
  activeTab.value = 'query'
  ElMessage.success('已加载SQL语句')
}

// 复制SQL
const handleCopySQL = (row: any) => {
  navigator.clipboard.writeText(row.sql)
  ElMessage.success('SQL已复制到剪贴板')
}

// 删除历史
const handleDeleteHistory = (row: any) => {
  ElMessageBox.confirm('确定要删除这条查询历史吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning',
  }).then(() => {
    const index = queryHistory.value.findIndex((item) => item.id === row.id)
    if (index > -1) {
      queryHistory.value.splice(index, 1)
      ElMessage.success('删除成功')
    }
  })
}

// 创建数据集
const handleCreateDataset = () => {
  ElMessage.info('创建数据集功能开发中...')
}

// 查看数据集
const handleViewDataset = (dataset: any) => {
  ElMessage.info(`查看数据集: ${dataset.name}`)
}

// 创建报表
const handleCreateReport = () => {
  ElMessage.info('创建报表功能开发中...')
}

// 查看报表
const handleViewReport = (report: any) => {
  ElMessage.info(`查看报表: ${report.name}`)
}
</script>

<style scoped lang="scss">
.page-container-tdesign {
  padding: 0;
  background: transparent;
  min-height: 100%;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
  padding: 20px 24px;
  background: #fff;
  border-radius: 6px;
  box-shadow: 0 1px 2px 0 rgba(0, 0, 0, 0.03);

  .page-title {
    display: flex;
    align-items: center;
    gap: 8px;
    font-size: 18px;
    font-weight: 600;
    color: #1f2937;

    .page-subtitle {
      font-size: 14px;
      font-weight: 400;
      color: #9ca3af;
    }
  }
}

.info-banner {
  display: flex;
  gap: 24px;
  padding: 16px 24px;
  background: linear-gradient(135deg, #e6f0ff 0%, #f0f7ff 100%);
  border-radius: 6px;
  margin-bottom: 16px;

  .info-item {
    display: flex;
    align-items: center;
    gap: 8px;
    font-size: 14px;
    color: #374151;
  }
}

.stats-container {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
  margin-bottom: 16px;

  .stat-card {
    display: flex;
    align-items: center;
    gap: 16px;
    padding: 20px;
    background: #fff;
    border-radius: 6px;
    box-shadow: 0 1px 2px 0 rgba(0, 0, 0, 0.03);
    transition: all 0.3s;

    &:hover {
      box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.1);
      transform: translateY(-2px);
    }

    .stat-icon {
      display: flex;
      align-items: center;
      justify-content: center;
      width: 48px;
      height: 48px;
      border-radius: 8px;
    }

    .stat-content {
      flex: 1;

      .stat-value {
        font-size: 24px;
        font-weight: 600;
        color: #1f2937;
        line-height: 1.2;
      }

      .stat-label {
        font-size: 14px;
        color: #6b7280;
        margin-top: 4px;
      }
    }
  }
}

.tabs-container {
  background: #fff;
  border-radius: 6px;
  padding: 24px;
  box-shadow: 0 1px 2px 0 rgba(0, 0, 0, 0.03);
}

.query-panel {
  .query-editor {
    margin-bottom: 24px;

    .editor-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 12px;

      .editor-actions {
        display: flex;
        gap: 8px;
      }
    }

    .sql-textarea {
      :deep(.el-textarea__inner) {
        font-family: 'Monaco', 'Menlo', 'Consolas', monospace;
        font-size: 13px;
        line-height: 1.6;
      }
    }
  }

  .query-result {
    .result-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      padding: 12px 16px;
      background: #f0f7ff;
      border-radius: 6px 6px 0 0;
      border: 1px solid #d0e7ff;

      .result-title {
        display: flex;
        align-items: center;
        gap: 8px;
        font-weight: 500;
        color: #0052d9;
      }
    }
  }
}

.history-panel,
.datasets-panel,
.reports-panel {
  .search-bar {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 16px;
  }

  .sql-cell {
    code {
      font-family: 'Monaco', 'Menlo', 'Consolas', monospace;
      font-size: 12px;
      color: #374151;
    }
  }
}

.datasets-grid,
.reports-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 16px;
  margin-top: 16px;
}

.dataset-card,
.report-card {
  padding: 20px;
  background: #fff;
  border: 1px solid #e5e7eb;
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.3s;

  &:hover {
    border-color: #0052d9;
    box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.1);
    transform: translateY(-2px);
  }
}

.dataset-card {
  display: flex;
  gap: 16px;

  .dataset-icon {
    flex-shrink: 0;
  }

  .dataset-info {
    flex: 1;

    .dataset-name {
      font-size: 16px;
      font-weight: 500;
      color: #1f2937;
      margin-bottom: 8px;
    }

    .dataset-desc {
      font-size: 13px;
      color: #6b7280;
      margin-bottom: 12px;
    }

    .dataset-meta {
      display: flex;
      gap: 16px;
      font-size: 12px;
      color: #9ca3af;

      span {
        display: flex;
        align-items: center;
        gap: 4px;
      }
    }
  }

  .dataset-actions {
    display: flex;
    gap: 4px;
    flex-shrink: 0;
  }
}

.report-card {
  .report-preview {
    display: flex;
    align-items: center;
    justify-content: center;
    height: 120px;
    background: #f9fafb;
    border-radius: 4px;
    margin-bottom: 16px;
  }

  .report-info {
    .report-name {
      font-size: 16px;
      font-weight: 500;
      color: #1f2937;
      margin-bottom: 8px;
    }

    .report-type {
      margin-bottom: 12px;
    }

    .report-meta {
      display: flex;
      gap: 16px;
      font-size: 12px;
      color: #9ca3af;

      span {
        display: flex;
        align-items: center;
        gap: 4px;
      }
    }
  }
}

.pagination-container {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}
</style>
