<template>
  <div class="table-management">
    <div class="page-header">
      <h2>表管理中心</h2>
      <p>查看和管理所有湖仓表</p>
    </div>

    <div class="content-container">
      <!-- 统计卡片 -->
      <div class="stats-row">
        <div class="stat-card">
          <div class="stat-icon" style="background: #e8f4ff">
            <el-icon :size="24" color="#409eff"><Tickets /></el-icon>
          </div>
          <div class="stat-content">
            <div class="stat-value">{{ tables.length }}</div>
            <div class="stat-label">总表数</div>
          </div>
        </div>
        <div class="stat-card">
          <div class="stat-icon" style="background: #e8f8f2">
            <el-icon :size="24" color="#00b42a"><Upload /></el-icon>
          </div>
          <div class="stat-content">
            <div class="stat-value">{{ activeTableCount }}</div>
            <div class="stat-label">活跃表</div>
          </div>
        </div>
        <div class="stat-card">
          <div class="stat-icon" style="background: #fff7e8">
            <el-icon :size="24" color="#ff7d00"><Coin /></el-icon>
          </div>
          <div class="stat-content">
            <div class="stat-value">{{ totalSize }}</div>
            <div class="stat-label">总存储</div>
          </div>
        </div>
        <div class="stat-card">
          <div class="stat-icon" style="background: #ffece8">
            <el-icon :size="24" color="#f53f3f"><Calendar /></el-icon>
          </div>
          <div class="stat-content">
            <div class="stat-value">{{ todayUpdated }}</div>
            <div class="stat-label">今日更新</div>
          </div>
        </div>
      </div>

      <!-- 表格 -->
      <div class="table-card">
        <div class="card-header">
          <div class="header-left">
            <h3>数据表列表</h3>
          </div>
          <div class="header-right">
            <el-input
              v-model="searchText"
              placeholder="搜索表名"
              style="width: 220px; margin-right: 12px"
              clearable
            >
              <template #prefix>
                <el-icon><Search /></el-icon>
              </template>
            </el-input>
            <el-select
              v-model="layerFilter"
              placeholder="数据分层"
              style="width: 140px; margin-right: 12px"
              clearable
            >
              <el-option label="ODS层" value="ODS" />
              <el-option label="DWD层" value="DWD" />
              <el-option label="DWS层" value="DWS" />
              <el-option label="ADS层" value="ADS" />
            </el-select>
            <el-button type="primary" @click="showCreateDialog = true">
              <el-icon><Plus /></el-icon>
              创建表
            </el-button>
          </div>
        </div>
        <div class="card-body">
          <el-table
            :data="paginatedTables"
            style="width: 100%"
            size="small"
            :max-height="600"
          >
            <el-table-column prop="name" label="表名" min-width="200" fixed="left">
              <template #default="{ row }">
                <div style="display: flex; align-items: center; gap: 8px">
                  <el-icon color="#409eff"><Tickets /></el-icon>
                  <span style="font-weight: 500">{{ row.name }}</span>
                </div>
              </template>
            </el-table-column>
            <el-table-column prop="layer" label="数据分层" min-width="100">
              <template #default="{ row }">
                <el-tag :type="getLayerType(row.layer)" size="small">
                  {{ row.layer }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="domain" label="主题域" min-width="100" />
            <el-table-column prop="description" label="描述" min-width="200" :show-overflow-tooltip="true" />
            <el-table-column prop="fieldCount" label="字段数" min-width="80" />
            <el-table-column prop="partitions" label="分区数" min-width="80" />
            <el-table-column prop="size" label="数据大小" min-width="120" />
            <el-table-column prop="rows" label="记录数" min-width="120">
              <template #default="{ row }">
                {{ formatNumber(row.rows) }}
              </template>
            </el-table-column>
            <el-table-column prop="lastUpdate" label="最后更新" min-width="170" />
            <el-table-column label="操作" width="240" fixed="right" align="left" header-align="left">
              <template #default="{ row }">
                <div style="white-space: nowrap; padding: 0 12px 0 0">
                  <el-button type="primary" size="small" text @click="viewSchema(row)">
                    <el-icon><View /></el-icon>
                    查看结构
                  </el-button>
                  <el-button type="primary" size="small" text @click="viewData(row)" style="margin-left: 8px">
                    <el-icon><Histogram /></el-icon>
                    查看数据
                  </el-button>
                  <el-button type="danger" size="small" text @click="deleteTable(row)" style="margin-left: 8px">
                    <el-icon><Delete /></el-icon>
                    删除
                  </el-button>
                </div>
              </template>
            </el-table-column>
          </el-table>
        </div>
        <div class="card-pagination">
          <el-pagination
            v-model:current-page="currentPage"
            v-model:page-size="pageSize"
            :page-sizes="[10, 20, 30, 50]"
            layout="total, sizes, prev, pager, next, jumper"
            :total="filteredTables.length"
          />
        </div>
      </div>
    </div>

    <!-- 创建表对话框 -->
    <el-dialog v-model="showCreateDialog" title="创建数据表" width="600px">
      <el-form :model="newTable" label-width="100px">
        <el-form-item label="表名">
          <el-input v-model="newTable.name" placeholder="请输入表名" />
        </el-form-item>
        <el-form-item label="数据分层">
          <el-select v-model="newTable.layer" placeholder="选择数据分层">
            <el-option label="ODS - 原始数据层" value="ODS" />
            <el-option label="DWD - 明细数据层" value="DWD" />
            <el-option label="DWS - 汇总数据层" value="DWS" />
            <el-option label="ADS - 应用数据层" value="ADS" />
          </el-select>
        </el-form-item>
        <el-form-item label="主题域">
          <el-input v-model="newTable.domain" placeholder="如:用户域、订单域" />
        </el-form-item>
        <el-form-item label="表描述">
          <el-input v-model="newTable.description" type="textarea" :rows="3" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showCreateDialog = false">取消</el-button>
        <el-button type="primary" @click="createTable">创建</el-button>
      </template>
    </el-dialog>

    <!-- 查看表结构对话框 -->
    <el-dialog v-model="showSchemaDialog" :title="`表结构 - ${selectedTable?.name}`" width="700px">
      <el-table :data="selectedTable?.schema" size="small">
        <el-table-column prop="name" label="字段名" width="150" />
        <el-table-column prop="type" label="数据类型" width="120" />
        <el-table-column prop="nullable" label="可为空" width="80">
          <template #default="{ row }">
            <el-tag :type="row.nullable ? 'info' : 'success'" size="small">
              {{ row.nullable ? '是' : '否' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="comment" label="注释" :show-overflow-tooltip="true" />
      </el-table>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Tickets,
  Upload,
  Coin,
  Calendar,
  Search,
  Plus,
  View,
  Histogram,
  Delete,
} from '@element-plus/icons-vue'

interface TableInfo {
  id: string
  name: string
  layer: string
  domain: string
  description: string
  fieldCount: number
  partitions: number
  size: string
  rows: number
  lastUpdate: string
  schema?: any[]
}

const searchText = ref('')
const layerFilter = ref('')
const currentPage = ref(1)
const pageSize = ref(10)
const showCreateDialog = ref(false)
const showSchemaDialog = ref(false)
const selectedTable = ref<TableInfo | null>(null)

const newTable = ref({
  name: '',
  layer: '',
  domain: '',
  description: '',
})

// 模拟数据
const tables = ref<TableInfo[]>([
  {
    id: '1',
    name: 'ods_user_info',
    layer: 'ODS',
    domain: '用户域',
    description: '用户基础信息表',
    fieldCount: 15,
    partitions: 30,
    size: '2.5 GB',
    rows: 1250000,
    lastUpdate: '2025-01-13 14:30:25',
    schema: [
      { name: 'user_id', type: 'bigint', nullable: false, comment: '用户ID' },
      { name: 'user_name', type: 'string', nullable: false, comment: '用户名' },
      { name: 'age', type: 'int', nullable: true, comment: '年龄' },
      { name: 'gender', type: 'string', nullable: true, comment: '性别' },
    ],
  },
  {
    id: '2',
    name: 'dwd_order_detail',
    layer: 'DWD',
    domain: '订单域',
    description: '订单明细表',
    fieldCount: 20,
    partitions: 60,
    size: '8.2 GB',
    rows: 5800000,
    lastUpdate: '2025-01-13 15:20:10',
  },
  {
    id: '3',
    name: 'dws_user_order_daily',
    layer: 'DWS',
    domain: '订单域',
    description: '用户每日订单汇总表',
    fieldCount: 12,
    partitions: 90,
    size: '1.8 GB',
    rows: 980000,
    lastUpdate: '2025-01-13 16:10:45',
  },
  {
    id: '4',
    name: 'ads_sales_report',
    layer: 'ADS',
    domain: '营销域',
    description: '销售报表',
    fieldCount: 8,
    partitions: 12,
    size: '450 MB',
    rows: 150000,
    lastUpdate: '2025-01-13 17:00:00',
  },
])

const filteredTables = computed(() => {
  return tables.value.filter((table) => {
    const matchSearch = !searchText.value || table.name.toLowerCase().includes(searchText.value.toLowerCase())
    const matchLayer = !layerFilter.value || table.layer === layerFilter.value
    return matchSearch && matchLayer
  })
})

const paginatedTables = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value
  const end = start + pageSize.value
  return filteredTables.value.slice(start, end)
})

const activeTableCount = computed(() => {
  return tables.value.filter((t) => {
    const lastUpdate = new Date(t.lastUpdate)
    const now = new Date()
    const diffDays = (now.getTime() - lastUpdate.getTime()) / (1000 * 60 * 60 * 24)
    return diffDays <= 7
  }).length
})

const totalSize = computed(() => {
  // 简化计算,实际应该累加
  return '12.95 GB'
})

const todayUpdated = computed(() => {
  const today = new Date().toISOString().split('T')[0]
  return tables.value.filter((t) => t.lastUpdate.startsWith(today)).length
})

const getLayerType = (layer: string) => {
  const typeMap: Record<string, any> = {
    ODS: 'info',
    DWD: 'primary',
    DWS: 'success',
    ADS: 'warning',
  }
  return typeMap[layer] || ''
}

const formatNumber = (num: number) => {
  return num.toLocaleString()
}

const createTable = () => {
  if (!newTable.value.name) {
    ElMessage.warning('请输入表名')
    return
  }

  const table: TableInfo = {
    id: Date.now().toString(),
    name: newTable.value.name,
    layer: newTable.value.layer,
    domain: newTable.value.domain,
    description: newTable.value.description,
    fieldCount: 0,
    partitions: 0,
    size: '0 MB',
    rows: 0,
    lastUpdate: new Date().toISOString().replace('T', ' ').substring(0, 19),
  }

  tables.value.unshift(table)
  showCreateDialog.value = false
  newTable.value = { name: '', layer: '', domain: '', description: '' }
  ElMessage.success('表创建成功')
}

const viewSchema = (table: TableInfo) => {
  selectedTable.value = table
  if (!table.schema) {
    ElMessage.warning('该表暂无结构信息')
    return
  }
  showSchemaDialog.value = true
}

const viewData = (table: TableInfo) => {
  ElMessage.info(`查看表 ${table.name} 的数据`)
}

const deleteTable = (table: TableInfo) => {
  ElMessageBox.confirm(`确定要删除表 ${table.name} 吗?`, '删除确认', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning',
  }).then(() => {
    const index = tables.value.findIndex((t) => t.id === table.id)
    if (index > -1) {
      tables.value.splice(index, 1)
      ElMessage.success('删除成功')
    }
  })
}
</script>

<style scoped lang="scss">
.table-management {
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
    color: #1d2129;
  }

  p {
    margin: 0;
    font-size: 14px;
    color: #86909c;
  }
}

.content-container {
  flex: 1;
  padding: 16px;
  overflow: auto;
}

.stats-row {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
  margin-bottom: 16px;
}

.stat-card {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 20px;
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.03);

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
      color: #1d2129;
      margin-bottom: 4px;
    }

    .stat-label {
      font-size: 14px;
      color: #86909c;
    }
  }
}

.table-card {
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.03);
}

.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px 20px;
  border-bottom: 1px solid #e5e6eb;

  .header-left {
    h3 {
      margin: 0;
      font-size: 16px;
      font-weight: 600;
      color: #1d2129;
    }
  }

  .header-right {
    display: flex;
    align-items: center;
  }
}

.card-body {
  padding: 0;
}

.card-pagination {
  padding: 16px 20px;
  display: flex;
  justify-content: flex-end;
  border-top: 1px solid #e5e6eb;
  background: #fafafa;
}
</style>
