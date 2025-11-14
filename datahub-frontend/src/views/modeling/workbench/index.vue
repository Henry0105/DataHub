<template>
  <div class="sql-workbench">
    <div class="page-header">
      <h2>SQL工作台</h2>
      <p>在线查询和数据探索</p>
    </div>

    <div class="workbench-container">
      <!-- 左侧数据库结构树 -->
      <div class="left-panel">
        <div class="panel-header">
          <h3>数据库</h3>
          <el-button type="primary" size="small" text @click="refreshTree">
            <el-icon><Refresh /></el-icon>
          </el-button>
        </div>
        <el-tree
          :data="databaseTree"
          :props="treeProps"
          @node-click="handleNodeClick"
          default-expand-all
        >
          <template #default="{ node, data }">
            <span class="tree-node">
              <el-icon v-if="data.type === 'database'"><Coin /></el-icon>
              <el-icon v-else-if="data.type === 'table'"><Tickets /></el-icon>
              <el-icon v-else><Document /></el-icon>
              <span>{{ node.label }}</span>
            </span>
          </template>
        </el-tree>
      </div>

      <!-- 中间编辑器和结果区域 -->
      <div class="center-panel">
        <!-- SQL编辑器 -->
        <div class="editor-section">
          <div class="editor-toolbar">
            <el-select v-model="selectedDatabase" placeholder="选择数据库" style="width: 180px">
              <el-option label="default" value="default" />
              <el-option label="analytics" value="analytics" />
            </el-select>
            <el-button-group style="margin-left: 12px">
              <el-button type="primary" @click="executeQuery">
                <el-icon><VideoPlay /></el-icon>
                执行 (F5)
              </el-button>
              <el-button @click="formatSql">
                <el-icon><MagicStick /></el-icon>
                格式化
              </el-button>
              <el-button @click="clearEditor">
                <el-icon><Delete /></el-icon>
                清空
              </el-button>
            </el-button-group>
            <el-button style="margin-left: auto" @click="saveQuery">
              <el-icon><FolderAdd /></el-icon>
              保存查询
            </el-button>
          </div>
          <div class="editor-container">
            <el-input
              v-model="sqlText"
              type="textarea"
              :rows="12"
              placeholder="在此输入SQL语句..."
              class="sql-editor"
            />
          </div>
        </div>

        <!-- 查询结果 -->
        <div class="result-section">
          <el-tabs v-model="activeTab" type="border-card">
            <el-tab-pane label="查询结果" name="result">
              <div class="result-info">
                <span v-if="queryResult">
                  共 {{ queryResult.length }} 条记录，耗时 {{ executionTime }}ms
                </span>
              </div>
              <el-table
                :data="queryResult"
                style="width: 100%"
                size="small"
                max-height="300"
                stripe
              >
                <el-table-column
                  v-for="col in resultColumns"
                  :key="col"
                  :prop="col"
                  :label="col"
                  min-width="120"
                />
              </el-table>
              <div v-if="!queryResult" class="empty-result">
                <el-icon :size="48"><DocumentCopy /></el-icon>
                <p>执行SQL查询后,结果将显示在此处</p>
              </div>
            </el-tab-pane>
            <el-tab-pane label="执行历史" name="history">
              <el-table :data="queryHistory" style="width: 100%" size="small">
                <el-table-column prop="sql" label="SQL语句" :show-overflow-tooltip="true" />
                <el-table-column prop="time" label="执行时间" width="170" />
                <el-table-column prop="duration" label="耗时(ms)" width="100" />
                <el-table-column label="操作" width="120">
                  <template #default="{ row }">
                    <el-button type="primary" size="small" text @click="loadHistory(row)">
                      加载
                    </el-button>
                  </template>
                </el-table-column>
              </el-table>
            </el-tab-pane>
          </el-tabs>
        </div>
      </div>

      <!-- 右侧快捷操作 -->
      <div class="right-panel">
        <div class="panel-header">
          <h3>SQL模板</h3>
        </div>
        <div class="template-list">
          <div
            v-for="template in sqlTemplates"
            :key="template.name"
            class="template-item"
            @click="insertTemplate(template.sql)"
          >
            <div class="template-name">{{ template.name }}</div>
            <div class="template-desc">{{ template.description }}</div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { ElMessage } from 'element-plus'
import {
  Refresh,
  Coin,
  Tickets,
  Document,
  VideoPlay,
  MagicStick,
  Delete,
  FolderAdd,
  DocumentCopy,
} from '@element-plus/icons-vue'

const selectedDatabase = ref('default')
const sqlText = ref('')
const activeTab = ref('result')
const queryResult = ref<any[] | null>(null)
const resultColumns = ref<string[]>([])
const executionTime = ref(0)

const treeProps = {
  children: 'children',
  label: 'label',
}

const databaseTree = ref([
  {
    label: 'default',
    type: 'database',
    children: [
      { label: 'ods_user_info', type: 'table' },
      { label: 'dwd_user_info', type: 'table' },
      { label: 'dws_user_summary', type: 'table' },
    ],
  },
  {
    label: 'analytics',
    type: 'database',
    children: [
      { label: 'ads_user_report', type: 'table' },
      { label: 'ads_sales_report', type: 'table' },
    ],
  },
])

const queryHistory = ref([
  {
    sql: 'SELECT * FROM ods_user_info LIMIT 10',
    time: '2025-01-13 16:30:25',
    duration: 125,
  },
  {
    sql: 'SELECT count(*) FROM dwd_user_info',
    time: '2025-01-13 15:20:10',
    duration: 89,
  },
])

const sqlTemplates = ref([
  {
    name: '查询全表',
    description: '查询表的所有数据',
    sql: 'SELECT * FROM table_name LIMIT 100;',
  },
  {
    name: '统计记录数',
    description: '统计表的记录数',
    sql: 'SELECT COUNT(*) as total FROM table_name;',
  },
  {
    name: '查看表结构',
    description: '查看表的字段结构',
    sql: 'DESC table_name;',
  },
  {
    name: '分组统计',
    description: '按字段分组统计',
    sql: 'SELECT column_name, COUNT(*) as cnt\nFROM table_name\nGROUP BY column_name\nORDER BY cnt DESC;',
  },
])

const handleNodeClick = (data: any) => {
  if (data.type === 'table') {
    insertTemplate(`SELECT * FROM ${data.label} LIMIT 100;`)
  }
}

const refreshTree = () => {
  ElMessage.success('数据库结构已刷新')
}

const executeQuery = () => {
  if (!sqlText.value.trim()) {
    ElMessage.warning('请输入SQL语句')
    return
  }

  const startTime = Date.now()
  
  // 模拟查询
  setTimeout(() => {
    executionTime.value = Date.now() - startTime
    
    // 模拟结果
    resultColumns.value = ['id', 'name', 'age', 'created_at']
    queryResult.value = [
      { id: 1, name: '张三', age: 25, created_at: '2025-01-01' },
      { id: 2, name: '李四', age: 30, created_at: '2025-01-02' },
      { id: 3, name: '王五', age: 28, created_at: '2025-01-03' },
    ]
    
    // 添加到历史
    queryHistory.value.unshift({
      sql: sqlText.value,
      time: new Date().toISOString().replace('T', ' ').substring(0, 19),
      duration: executionTime.value,
    })
    
    activeTab.value = 'result'
    ElMessage.success('查询执行成功')
  }, 300)
}

const formatSql = () => {
  if (!sqlText.value) return
  // 简单格式化
  sqlText.value = sqlText.value
    .replace(/\s+/g, ' ')
    .replace(/SELECT/gi, 'SELECT\n  ')
    .replace(/FROM/gi, '\nFROM')
    .replace(/WHERE/gi, '\nWHERE')
    .replace(/GROUP BY/gi, '\nGROUP BY')
    .replace(/ORDER BY/gi, '\nORDER BY')
  ElMessage.success('SQL已格式化')
}

const clearEditor = () => {
  sqlText.value = ''
}

const saveQuery = () => {
  ElMessage.success('查询已保存')
}

const insertTemplate = (sql: string) => {
  sqlText.value = sql
}

const loadHistory = (row: any) => {
  sqlText.value = row.sql
}
</script>

<style scoped lang="scss">
.sql-workbench {
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

.workbench-container {
  flex: 1;
  display: flex;
  gap: 16px;
  padding: 16px;
  overflow: hidden;
}

.left-panel,
.right-panel {
  width: 250px;
  background: #fff;
  border-radius: 8px;
  padding: 16px;
  overflow-y: auto;
}

.panel-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;

  h3 {
    margin: 0;
    font-size: 16px;
    font-weight: 600;
  }
}

.tree-node {
  display: flex;
  align-items: center;
  gap: 8px;
}

.center-panel {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.editor-section {
  background: #fff;
  border-radius: 8px;
  overflow: hidden;
}

.editor-toolbar {
  display: flex;
  align-items: center;
  padding: 12px 16px;
  border-bottom: 1px solid #e5e6eb;
}

.editor-container {
  padding: 16px;
}

.sql-editor {
  :deep(textarea) {
    font-family: 'Consolas', 'Monaco', 'Courier New', monospace;
    font-size: 14px;
    line-height: 1.6;
  }
}

.result-section {
  flex: 1;
  background: #fff;
  border-radius: 8px;
  overflow: hidden;

  :deep(.el-tabs__content) {
    padding: 16px;
  }
}

.result-info {
  margin-bottom: 12px;
  font-size: 13px;
  color: #606266;
}

.empty-result {
  text-align: center;
  padding: 60px 0;
  color: #909399;

  p {
    margin-top: 16px;
  }
}

.template-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.template-item {
  padding: 12px;
  background: #f7f8fa;
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.3s;

  &:hover {
    background: #e8f3ff;
    transform: translateX(4px);
  }

  .template-name {
    font-size: 14px;
    font-weight: 500;
    color: #1d2129;
    margin-bottom: 4px;
  }

  .template-desc {
    font-size: 12px;
    color: #86909c;
  }
}
</style>
