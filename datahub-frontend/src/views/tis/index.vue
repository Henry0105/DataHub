<template>
  <div class="page-container-tdesign">
    <div class="page-header">
      <div class="page-title">
        <el-icon :size="20" color="#0052d9"><Share /></el-icon>
        <span>数据集成平台</span>
        <span class="page-subtitle">TIS</span>
      </div>
      <el-button type="primary" @click="handleCreate">
        <el-icon><Plus /></el-icon>
        创建任务
      </el-button>
    </div>

    <div class="info-banner">
      <div class="info-item">
        <el-icon color="#0052d9"><Check /></el-icon>
        <span>实时批量数据同步</span>
      </div>
      <div class="info-item">
        <el-icon color="#0052d9"><Check /></el-icon>
        <span>支持Kafka到Doris</span>
      </div>
      <div class="info-item">
        <el-icon color="#0052d9"><Check /></el-icon>
        <span>可视化任务配置</span>
      </div>
    </div>

    <div class="stats-container">
      <div class="stat-card">
        <div class="stat-icon" style="background: #e6f0ff;">
          <el-icon :size="24" color="#0052d9"><Document /></el-icon>
        </div>
        <div class="stat-content">
          <div class="stat-value">28</div>
          <div class="stat-label">总任务数</div>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon" style="background: #e6fffb;">
          <el-icon :size="24" color="#00b96b"><VideoPlay /></el-icon>
        </div>
        <div class="stat-content">
          <div class="stat-value">12</div>
          <div class="stat-label">运行中</div>
        </div>
      </div>
    </div>

    <div class="table-card">
      <el-table :data="tableData" style="width: 100%">
        <el-table-column prop="name" label="任务名称" min-width="200" />
        <el-table-column prop="source" label="数据源" width="150" />
        <el-table-column prop="target" label="目标" width="150" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === '运行中' ? 'success' : 'info'" size="small">
              {{ row.status }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="updateTime" label="更新时间" width="160" />
        <el-table-column label="操作" width="200">
          <template #default="{ row }">
            <el-button type="primary" size="small" link>
              <el-icon><VideoPlay /></el-icon>
              启动
            </el-button>
            <el-button type="primary" size="small" link>
              <el-icon><View /></el-icon>
              详情
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { ElMessage } from 'element-plus'

const tableData = ref([
  { id: 1, name: 'Kafka到Doris实时同步', source: 'Kafka', target: 'Doris', status: '运行中', updateTime: '2025-01-15 14:30:00' },
  { id: 2, name: 'MySQL到Hive批量', source: 'MySQL', target: 'Hive', status: '已停止', updateTime: '2025-01-15 14:25:00' },
])

const handleCreate = () => {
  ElMessage.info('创建任务功能开发中...')
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
</style>
