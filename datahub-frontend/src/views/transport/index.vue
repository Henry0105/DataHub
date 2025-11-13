<template>
  <div class="page-container-tdesign">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="page-title">
        <el-icon :size="20" color="#0052d9"><Connection /></el-icon>
        <span>数据集成管理</span>
        <span class="page-subtitle">Transport (DataX)</span>
      </div>
      <el-button type="primary" @click="handleCreateTask">
        <el-icon><Plus /></el-icon>
        创建任务
      </el-button>
    </div>

    <!-- 功能说明 -->
    <div class="info-banner">
      <div class="info-item">
        <el-icon color="#0052d9"><Check /></el-icon>
        <span>支持50+种数据源接入</span>
      </div>
      <div class="info-item">
        <el-icon color="#0052d9"><Check /></el-icon>
        <span>实时与批量集成能力</span>
      </div>
      <div class="info-item">
        <el-icon color="#0052d9"><Check /></el-icon>
        <span>智能数据处理</span>
      </div>
      <div class="info-item">
        <el-icon color="#0052d9"><Check /></el-icon>
        <span>高可靠与高可用</span>
      </div>
    </div>

    <!-- 统计卡片 -->
    <div class="stats-container">
      <div class="stat-card">
        <div class="stat-icon" style="background: #e6f0ff;">
          <el-icon :size="24" color="#0052d9"><Document /></el-icon>
        </div>
        <div class="stat-content">
          <div class="stat-value">{{ stats.total }}</div>
          <div class="stat-label">总任务数</div>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon" style="background: #e6fffb;">
          <el-icon :size="24" color="#00b96b"><VideoPlay /></el-icon>
        </div>
        <div class="stat-content">
          <div class="stat-value">{{ stats.running }}</div>
          <div class="stat-label">运行中</div>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon" style="background: #e6fff9;">
          <el-icon :size="24" color="#52c41a"><SuccessFilled /></el-icon>
        </div>
        <div class="stat-content">
          <div class="stat-value">{{ stats.success }}</div>
          <div class="stat-label">成功</div>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon" style="background: #fff1f0;">
          <el-icon :size="24" color="#ff4d4f"><CircleCloseFilled /></el-icon>
        </div>
        <div class="stat-content">
          <div class="stat-value">{{ stats.failed }}</div>
          <div class="stat-label">失败</div>
        </div>
      </div>
    </div>

    <!-- 搜索和筛选 -->
    <div class="search-bar">
      <el-input
        v-model="searchText"
        placeholder="搜索任务名称"
        :prefix-icon="Search"
        clearable
        style="width: 300px"
      />
      <div class="filter-group">
        <el-select v-model="filterStatus" placeholder="任务状态" clearable style="width: 150px">
          <el-option label="运行中" value="运行中" />
          <el-option label="成功" value="成功" />
          <el-option label="失败" value="失败" />
        </el-select>
        <el-button @click="handleRefresh" :icon="Refresh">刷新</el-button>
      </div>
    </div>

    <!-- 数据表格 -->
    <div class="table-card">
      <el-table :data="filteredData" style="width: 100%" v-loading="loading">
        <el-table-column prop="name" label="任务名称" min-width="200" />
        <el-table-column prop="reader" label="数据源" width="150">
          <template #default="{ row }">
            <el-tag size="small">{{ row.reader }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="同步方向" width="80" align="center">
          <template>
            <el-icon :size="20" color="#0052d9"><Right /></el-icon>
          </template>
        </el-table-column>
        <el-table-column prop="writer" label="目标" width="150">
          <template #default="{ row }">
            <el-tag size="small" type="success">{{ row.writer }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="records" label="记录数" width="120" />
        <el-table-column prop="speed" label="速度" width="120" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)" size="small">
              {{ row.status }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="updateTime" label="更新时间" width="160" />
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" size="small" link @click="handleRun(row)">
              <el-icon><VideoPlay /></el-icon>
              执行
            </el-button>
            <el-button type="primary" size="small" link @click="handleView(row)">
              <el-icon><View /></el-icon>
              详情
            </el-button>
            <el-button type="danger" size="small" link @click="handleDelete(row)">
              <el-icon><Delete /></el-icon>
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-container">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :page-sizes="[10, 20, 50, 100]"
          :total="total"
          layout="total, sizes, prev, pager, next, jumper"
        />
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Refresh } from '@element-plus/icons-vue'

const stats = ref({
  total: 45,
  running: 8,
  success: 32,
  failed: 5,
})

const searchText = ref('')
const filterStatus = ref('')
const loading = ref(false)
const currentPage = ref(1)
const pageSize = ref(10)

const tableData = ref([
  {
    id: 1,
    name: 'MySQL到Hive全量同步',
    reader: 'MySQL',
    writer: 'Hive',
    records: '1,250,000',
    speed: '50K/s',
    status: '成功',
    updateTime: '2025-01-15 14:30:00',
  },
  {
    id: 2,
    name: 'Oracle到ClickHouse增量',
    reader: 'Oracle',
    writer: 'ClickHouse',
    records: '850,000',
    speed: '35K/s',
    status: '运行中',
    updateTime: '2025-01-15 14:25:00',
  },
  {
    id: 3,
    name: 'MongoDB到MySQL',
    reader: 'MongoDB',
    writer: 'MySQL',
    records: '0',
    speed: '0',
    status: '失败',
    updateTime: '2025-01-15 14:20:00',
  },
])

const total = computed(() => {
  let data = [...tableData.value]
  if (searchText.value) {
    data = data.filter(item =>
      item.name.toLowerCase().includes(searchText.value.toLowerCase())
    )
  }
  if (filterStatus.value) {
    data = data.filter(item => item.status === filterStatus.value)
  }
  return data.length
})

const filteredData = computed(() => {
  let data = [...tableData.value]
  if (searchText.value) {
    data = data.filter(item =>
      item.name.toLowerCase().includes(searchText.value.toLowerCase())
    )
  }
  if (filterStatus.value) {
    data = data.filter(item => item.status === filterStatus.value)
  }
  const start = (currentPage.value - 1) * pageSize.value
  const end = start + pageSize.value
  return data.slice(start, end)
})

const getStatusType = (status: string) => {
  const map: Record<string, any> = {
    '运行中': 'primary',
    '成功': 'success',
    '失败': 'danger',
  }
  return map[status] || 'info'
}

const handleRefresh = () => {
  loading.value = true
  setTimeout(() => {
    loading.value = false
    ElMessage.success('刷新成功')
  }, 500)
}

const handleCreateTask = () => {
  ElMessage.info('创建任务功能开发中...')
}

const handleRun = (row: any) => {
  ElMessage.success(`执行任务: ${row.name}`)
}

const handleView = (row: any) => {
  ElMessage.info(`查看任务详情: ${row.name}`)
}

const handleDelete = (row: any) => {
  ElMessageBox.confirm(`确定要删除任务"${row.name}"吗？`, '警告', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'error',
  }).then(() => {
    ElMessage.success('删除成功')
  })
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

.search-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
  padding: 16px 24px;
  background: #fff;
  border-radius: 6px;
  box-shadow: 0 1px 2px 0 rgba(0, 0, 0, 0.03);

  .filter-group {
    display: flex;
    gap: 12px;
  }
}

.table-card {
  background: #fff;
  border-radius: 6px;
  padding: 24px;
  box-shadow: 0 1px 2px 0 rgba(0, 0, 0, 0.03);
}

.pagination-container {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}
</style>
