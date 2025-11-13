<template>
  <div class="page-container-tdesign">
    <div class="page-header">
      <div class="page-title">
        <el-icon :size="20" color="#0052d9"><Operation /></el-icon>
        <span>数据比对</span>
        <span class="page-subtitle">Data Compare</span>
      </div>
      <el-button type="primary" @click="handleCreate">
        <el-icon><Plus /></el-icon>
        创建比对任务
      </el-button>
    </div>

    <div class="info-banner">
      <div class="info-item">
        <el-icon color="#0052d9"><Check /></el-icon>
        <span>多源数据一致性校验</span>
      </div>
      <div class="info-item">
        <el-icon color="#0052d9"><Check /></el-icon>
        <span>数据质量监控</span>
      </div>
      <div class="info-item">
        <el-icon color="#0052d9"><Check /></el-icon>
        <span>差异分析与报告</span>
      </div>
    </div>

    <div class="stats-container">
      <div class="stat-card">
        <div class="stat-icon" style="background: #e6f0ff;">
          <el-icon :size="24" color="#0052d9"><Document /></el-icon>
        </div>
        <div class="stat-content">
          <div class="stat-value">38</div>
          <div class="stat-label">比对任务</div>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon" style="background: #e6fff9;">
          <el-icon :size="24" color="#52c41a"><SuccessFilled /></el-icon>
        </div>
        <div class="stat-content">
          <div class="stat-value">32</div>
          <div class="stat-label">一致</div>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon" style="background: #fff1f0;">
          <el-icon :size="24" color="#ff4d4f"><WarningFilled /></el-icon>
        </div>
        <div class="stat-content">
          <div class="stat-value">6</div>
          <div class="stat-label">存在差异</div>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon" style="background: #f0f5ff;">
          <el-icon :size="24" color="#597ef7"><TrendCharts /></el-icon>
        </div>
        <div class="stat-content">
          <div class="stat-value">99.2%</div>
          <div class="stat-label">一致性率</div>
        </div>
      </div>
    </div>

    <div class="table-card">
      <el-table :data="tableData" style="width: 100%">
        <el-table-column prop="name" label="任务名称" min-width="200" />
        <el-table-column prop="sourceDb" label="源数据库" width="150">
          <template #default="{ row }">
            <el-tag size="small">{{ row.sourceDb }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="比对方向" width="80" align="center">
          <template>
            <el-icon :size="20" color="#0052d9"><Right /></el-icon>
          </template>
        </el-table-column>
        <el-table-column prop="targetDb" label="目标数据库" width="150">
          <template #default="{ row }">
            <el-tag size="small" type="success">{{ row.targetDb }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="tables" label="表数量" width="100" />
        <el-table-column prop="records" label="记录数" width="120" />
        <el-table-column prop="consistency" label="一致性" width="100">
          <template #default="{ row }">
            <el-tag :type="parseFloat(row.consistency) >= 99 ? 'success' : 'warning'" size="small">
              {{ row.consistency }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)" size="small">
              {{ row.status }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="updateTime" label="更新时间" width="160" />
        <el-table-column label="操作" width="220">
          <template #default="{ row }">
            <el-button type="primary" size="small" link @click="handleRun(row)">
              <el-icon><VideoPlay /></el-icon>
              执行
            </el-button>
            <el-button type="primary" size="small" link @click="handleViewReport(row)">
              <el-icon><Document /></el-icon>
              报告
            </el-button>
            <el-button type="primary" size="small" link @click="handleView(row)">
              <el-icon><View /></el-icon>
              详情
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
import { ref } from 'vue'
import { ElMessage } from 'element-plus'

const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(38)

const tableData = ref([
  {
    id: 1,
    name: '用户表数据比对',
    sourceDb: 'MySQL-生产',
    targetDb: 'MySQL-备份',
    tables: 5,
    records: '1,250,000',
    consistency: '100%',
    status: '已完成',
    updateTime: '2025-01-15 14:30:00',
  },
  {
    id: 2,
    name: '订单表一致性校验',
    sourceDb: 'Oracle',
    targetDb: 'PostgreSQL',
    tables: 8,
    records: '850,000',
    consistency: '99.8%',
    status: '已完成',
    updateTime: '2025-01-15 14:25:00',
  },
  {
    id: 3,
    name: '商品库存比对',
    sourceDb: 'MySQL',
    targetDb: 'ClickHouse',
    tables: 3,
    records: '450,000',
    consistency: '98.5%',
    status: '存在差异',
    updateTime: '2025-01-15 14:20:00',
  },
])

const getStatusType = (status: string) => {
  const map: Record<string, any> = {
    '运行中': 'primary',
    '已完成': 'success',
    '存在差异': 'warning',
    '失败': 'danger',
  }
  return map[status] || 'info'
}

const handleCreate = () => {
  ElMessage.info('创建比对任务功能开发中...')
}

const handleRun = (row: any) => {
  ElMessage.success(`执行比对任务: ${row.name}`)
}

const handleViewReport = (row: any) => {
  ElMessage.info(`查看比对报告: ${row.name}`)
}

const handleView = (row: any) => {
  ElMessage.info(`查看任务详情: ${row.name}`)
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
