<template>
  <div class="data-lineage">
    <div class="page-header">
      <h2>数据血缘图</h2>
      <p>展示表之间的依赖关系</p>
    </div>

    <div class="content-wrapper">
      <el-card class="lineage-card">
        <template #header>
          <div class="card-header-content">
            <span>血缘关系图</span>
            <div>
              <el-select v-model="selectedTableName" placeholder="选择数据表" style="width: 250px; margin-right: 12px">
                <el-option
                  v-for="table in tables"
                  :key="table.name"
                  :label="table.name"
                  :value="table.name"
                />
              </el-select>
              <el-button @click="refreshLineage">
                <el-icon><Refresh /></el-icon>
                刷新
              </el-button>
            </div>
          </div>
        </template>

        <div class="lineage-view">
          <div v-if="!selectedTableName" class="empty-hint">
            <el-icon :size="48"><Share /></el-icon>
            <p>请选择一个数据表查看血缘关系</p>
          </div>
          <div v-else class="lineage-display">
            <div class="lineage-level">
              <h4>上游依赖</h4>
              <div class="table-nodes">
                <el-tag v-for="table in currentLineage.upstream" :key="table" type="primary" size="large">
                  {{ table }}
                </el-tag>
                <span v-if="currentLineage.upstream.length === 0" class="no-data">无</span>
              </div>
            </div>
            
            <div class="current-table">
              <el-tag type="success" size="large" effect="dark">
                {{ selectedTableName }}
              </el-tag>
            </div>

            <div class="lineage-level">
              <h4>下游影响</h4>
              <div class="table-nodes">
                <el-tag v-for="table in currentLineage.downstream" :key="table" type="warning" size="large">
                  {{ table }}
                </el-tag>
                <span v-if="currentLineage.downstream.length === 0" class="no-data">无</span>
              </div>
            </div>
          </div>
        </div>
      </el-card>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { Refresh, Share } from '@element-plus/icons-vue'

const selectedTableName = ref('')

const tables = ref([
  { name: 'ods_user_info' },
  { name: 'dwd_user_info' },
  { name: 'dws_user_summary' },
  { name: 'ads_user_report' },
])

const lineageData: Record<string, any> = {
  'ods_user_info': {
    upstream: [],
    downstream: ['dwd_user_info'],
  },
  'dwd_user_info': {
    upstream: ['ods_user_info'],
    downstream: ['dws_user_summary'],
  },
  'dws_user_summary': {
    upstream: ['dwd_user_info'],
    downstream: ['ads_user_report'],
  },
  'ads_user_report': {
    upstream: ['dws_user_summary'],
    downstream: [],
  },
}

const currentLineage = computed(() => {
  if (!selectedTableName.value) {
    return { upstream: [], downstream: [] }
  }
  return lineageData[selectedTableName.value] || { upstream: [], downstream: [] }
})

const refreshLineage = () => {
  ElMessage.success('血缘关系已刷新')
}
</script>

<style scoped lang="scss">
.data-lineage {
  height: 100%;
  display: flex;
  flex-direction: column;
  background: #f5f7fa;
}

.page-header {
  padding: 24px;
  background: #fff;
  border-bottom: 1px solid #e5e6eb;

  h2 {
    margin: 0 0 8px 0;
    font-size: 20px;
    font-weight: 600;
  }

  p {
    margin: 0;
    font-size: 14px;
    color: #86909c;
  }
}

.content-wrapper {
  flex: 1;
  padding: 16px;
  overflow: auto;
}

.lineage-card {
  height: 100%;
}

.card-header-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.lineage-view {
  min-height: 500px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.empty-hint {
  text-align: center;
  color: #909399;

  p {
    margin-top: 16px;
  }
}

.lineage-display {
  width: 100%;
  display: flex;
  flex-direction: column;
  gap: 40px;
  align-items: center;
}

.lineage-level {
  text-align: center;
  width: 100%;

  h4 {
    margin: 0 0 16px 0;
    color: #606266;
  }
}

.table-nodes {
  display: flex;
  gap: 12px;
  justify-content: center;
  flex-wrap: wrap;
}

.current-table {
  padding: 20px 0;
}

.no-data {
  color: #c0c4cc;
}
</style>
