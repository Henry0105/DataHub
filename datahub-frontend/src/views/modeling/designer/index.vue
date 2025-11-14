<template>
  <div class="model-designer">
    <div class="page-header">
      <h2>数据模型设计器</h2>
      <p>可视化的表结构设计工具</p>
    </div>

    <!-- 数据仓湖架构分层展示 -->
    <div class="architecture-section">
      <div class="section-title">
        <h3>数据仓湖架构分层</h3>
        <el-button type="primary" size="small" @click="showArchitectureDetail = true">
          <el-icon><InfoFilled /></el-icon>
          查看详情
        </el-button>
      </div>
      <div class="layer-flow">
        <div class="layer-card ods">
          <div class="layer-header">
            <div class="layer-icon">
              <el-icon :size="28"><Coin /></el-icon>
            </div>
            <div class="layer-info">
              <div class="layer-name">ODS层</div>
              <div class="layer-subtitle">原始数据层</div>
            </div>
          </div>
          <div class="layer-desc">Operation Data Store</div>
          <div class="layer-stats">
            <span>{{ layerStats.ODS }} 张表</span>
          </div>
        </div>

        <div class="flow-arrow">
          <el-icon :size="24"><Right /></el-icon>
        </div>

        <div class="layer-card dwd">
          <div class="layer-header">
            <div class="layer-icon">
              <el-icon :size="28"><Grid /></el-icon>
            </div>
            <div class="layer-info">
              <div class="layer-name">DWD层</div>
              <div class="layer-subtitle">明细数据层</div>
            </div>
          </div>
          <div class="layer-desc">Data Warehouse Detail</div>
          <div class="layer-stats">
            <span>{{ layerStats.DWD }} 张表</span>
          </div>
        </div>

        <div class="flow-arrow">
          <el-icon :size="24"><Right /></el-icon>
        </div>

        <div class="layer-card dws">
          <div class="layer-header">
            <div class="layer-icon">
              <el-icon :size="28"><Histogram /></el-icon>
            </div>
            <div class="layer-info">
              <div class="layer-name">DWS层</div>
              <div class="layer-subtitle">汇总数据层</div>
            </div>
          </div>
          <div class="layer-desc">Data Warehouse Summary</div>
          <div class="layer-stats">
            <span>{{ layerStats.DWS }} 张表</span>
          </div>
        </div>

        <div class="flow-arrow">
          <el-icon :size="24"><Right /></el-icon>
        </div>

        <div class="layer-card ads">
          <div class="layer-header">
            <div class="layer-icon">
              <el-icon :size="28"><TrendCharts /></el-icon>
            </div>
            <div class="layer-info">
              <div class="layer-name">ADS层</div>
              <div class="layer-subtitle">应用数据层</div>
            </div>
          </div>
          <div class="layer-desc">Application Data Store</div>
          <div class="layer-stats">
            <span>{{ layerStats.ADS }} 张表</span>
          </div>
        </div>
      </div>
    </div>

    <div class="designer-container">
      <!-- 左侧工具栏 -->
      <div class="left-panel">
        <div class="panel-header">
          <h3>模型列表</h3>
          <el-button type="primary" size="small" @click="showCreateDialog = true">
            <el-icon><Plus /></el-icon>
            新建模型
          </el-button>
        </div>
        <el-input
          v-model="searchText"
          placeholder="搜索模型"
          clearable
          style="margin-bottom: 16px"
        >
          <template #prefix>
            <el-icon><Search /></el-icon>
          </template>
        </el-input>
        <div class="model-list">
          <div
            v-for="model in filteredModels"
            :key="model.id"
            class="model-item"
            :class="{ active: selectedModel?.id === model.id }"
            @click="selectModel(model)"
          >
            <el-icon class="model-icon"><Grid /></el-icon>
            <div class="model-info">
              <div class="model-name">{{ model.name }}</div>
              <div class="model-desc">{{ model.description }}</div>
            </div>
          </div>
        </div>
      </div>

      <!-- 中间画布区域 -->
      <div class="canvas-panel">
        <div class="canvas-toolbar">
          <el-button-group>
            <el-button size="small" @click="addTable">
              <el-icon><Plus /></el-icon>
              添加表
            </el-button>
            <el-button size="small" @click="addRelation">
              <el-icon><Connection /></el-icon>
              添加关系
            </el-button>
            <el-button size="small" @click="autoLayout">
              <el-icon><MagicStick /></el-icon>
              自动布局
            </el-button>
          </el-button-group>
          <el-button-group style="margin-left: 12px">
            <el-button size="small" @click="zoomIn">
              <el-icon><ZoomIn /></el-icon>
            </el-button>
            <el-button size="small" @click="zoomOut">
              <el-icon><ZoomOut /></el-icon>
            </el-button>
          </el-button-group>
          <el-button type="primary" size="small" style="margin-left: auto" @click="saveModel">
            <el-icon><Select /></el-icon>
            保存模型
          </el-button>
        </div>
        <div class="canvas-area" ref="canvasRef">
          <div v-if="!selectedModel" class="empty-state">
            <el-icon :size="64" color="#c0c4cc"><Box /></el-icon>
            <p>请选择或创建一个数据模型</p>
          </div>
          <div v-else class="canvas-content" :style="{ transform: `scale(${zoomLevel})` }">
            <!-- 表结构卡片 -->
            <div
              v-for="table in selectedModel.tables"
              :key="table.id"
              class="table-card"
              :style="{ left: table.x + 'px', top: table.y + 'px' }"
              draggable="true"
              @dragstart="onDragStart($event, table)"
              @dragend="onDragEnd($event, table)"
            >
              <div class="table-header">
                <el-icon><Tickets /></el-icon>
                <span>{{ table.name }}</span>
                <el-button type="danger" size="small" text @click="removeTable(table)">
                  <el-icon><Close /></el-icon>
                </el-button>
              </div>
              <div class="table-fields">
                <div v-for="field in table.fields" :key="field.name" class="field-item">
                  <el-icon v-if="field.isPrimaryKey" color="#409eff"><Key /></el-icon>
                  <span class="field-name">{{ field.name }}</span>
                  <span class="field-type">{{ field.type }}</span>
                </div>
              </div>
              <div class="table-footer">
                <el-button type="primary" size="small" text @click="editTable(table)">
                  <el-icon><Edit /></el-icon>
                  编辑
                </el-button>
              </div>
            </div>

            <!-- 关系连线 (简化版,实际应使用SVG) -->
            <svg class="relations-layer">
              <line
                v-for="(rel, idx) in selectedModel.relations"
                :key="idx"
                :x1="rel.x1"
                :y1="rel.y1"
                :x2="rel.x2"
                :y2="rel.y2"
                stroke="#409eff"
                stroke-width="2"
              />
            </svg>
          </div>
        </div>
      </div>

      <!-- 右侧属性面板 -->
      <div class="right-panel">
        <div class="panel-header">
          <h3>属性</h3>
        </div>
        <div v-if="selectedModel" class="properties">
          <el-form label-position="top" size="small">
            <el-form-item label="模型名称">
              <el-input v-model="selectedModel.name" />
            </el-form-item>
            <el-form-item label="模型描述">
              <el-input v-model="selectedModel.description" type="textarea" :rows="3" />
            </el-form-item>
            <el-form-item label="分层">
              <el-select v-model="selectedModel.layer" placeholder="选择数据分层">
                <el-option label="ODS - 原始数据层" value="ODS" />
                <el-option label="DWD - 明细数据层" value="DWD" />
                <el-option label="DWS - 汇总数据层" value="DWS" />
                <el-option label="ADS - 应用数据层" value="ADS" />
              </el-select>
            </el-form-item>
            <el-form-item label="主题域">
              <el-select v-model="selectedModel.domain" placeholder="选择主题域">
                <el-option label="用户域" value="user" />
                <el-option label="订单域" value="order" />
                <el-option label="商品域" value="product" />
                <el-option label="营销域" value="marketing" />
              </el-select>
            </el-form-item>
          </el-form>
        </div>
      </div>
    </div>

    <!-- 新建模型对话框 -->
    <el-dialog v-model="showCreateDialog" title="新建数据模型" width="500px">
      <el-form :model="newModel" label-width="80px">
        <el-form-item label="模型名称">
          <el-input v-model="newModel.name" placeholder="请输入模型名称" />
        </el-form-item>
        <el-form-item label="模型描述">
          <el-input v-model="newModel.description" type="textarea" :rows="3" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showCreateDialog = false">取消</el-button>
        <el-button type="primary" @click="createModel">创建</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { ElMessage } from 'element-plus'
import {
  Plus,
  Search,
  Grid,
  Connection,
  MagicStick,
  ZoomIn,
  ZoomOut,
  Select,
  Box,
  Tickets,
  Close,
  Key,
  Edit,
  InfoFilled,
  Right,
  Coin,
  Histogram,
  TrendCharts,
} from '@element-plus/icons-vue'

interface Field {
  name: string
  type: string
  isPrimaryKey?: boolean
  comment?: string
}

interface Table {
  id: string
  name: string
  fields: Field[]
  x: number
  y: number
}

interface Relation {
  fromTable: string
  toTable: string
  x1: number
  y1: number
  x2: number
  y2: number
}

interface Model {
  id: string
  name: string
  description: string
  layer?: string
  domain?: string
  tables: Table[]
  relations: Relation[]
}

const searchText = ref('')
const selectedModel = ref<Model | null>(null)
const showCreateDialog = ref(false)
const showArchitectureDetail = ref(false)
const zoomLevel = ref(1)
const canvasRef = ref<HTMLElement>()

const newModel = ref({
  name: '',
  description: '',
})

// 各层统计
const layerStats = ref({
  ODS: 12,
  DWD: 28,
  DWS: 15,
  ADS: 8,
})

// 模拟数据
const models = ref<Model[]>([
  {
    id: '1',
    name: '用户主题域模型',
    description: '用户相关的数据模型',
    layer: 'DWD',
    domain: 'user',
    tables: [
      {
        id: 't1',
        name: 'dim_user',
        x: 50,
        y: 50,
        fields: [
          { name: 'user_id', type: 'bigint', isPrimaryKey: true },
          { name: 'user_name', type: 'string' },
          { name: 'age', type: 'int' },
          { name: 'gender', type: 'string' },
        ],
      },
      {
        id: 't2',
        name: 'fact_user_behavior',
        x: 400,
        y: 50,
        fields: [
          { name: 'behavior_id', type: 'bigint', isPrimaryKey: true },
          { name: 'user_id', type: 'bigint' },
          { name: 'action_type', type: 'string' },
          { name: 'created_at', type: 'timestamp' },
        ],
      },
    ],
    relations: [
      { fromTable: 't1', toTable: 't2', x1: 250, y1: 100, x2: 400, y2: 100 },
    ],
  },
])

const filteredModels = computed(() => {
  if (!searchText.value) return models.value
  return models.value.filter((m) =>
    m.name.toLowerCase().includes(searchText.value.toLowerCase())
  )
})

const selectModel = (model: Model) => {
  selectedModel.value = model
}

const createModel = () => {
  if (!newModel.value.name) {
    ElMessage.warning('请输入模型名称')
    return
  }

  const model: Model = {
    id: Date.now().toString(),
    name: newModel.value.name,
    description: newModel.value.description,
    tables: [],
    relations: [],
  }

  models.value.push(model)
  selectedModel.value = model
  showCreateDialog.value = false
  newModel.value = { name: '', description: '' }
  ElMessage.success('创建成功')
}

const addTable = () => {
  if (!selectedModel.value) return
  const table: Table = {
    id: 't' + Date.now(),
    name: 'new_table',
    x: 100,
    y: 100,
    fields: [{ name: 'id', type: 'bigint', isPrimaryKey: true }],
  }
  selectedModel.value.tables.push(table)
}

const removeTable = (table: Table) => {
  if (!selectedModel.value) return
  const index = selectedModel.value.tables.findIndex((t) => t.id === table.id)
  if (index > -1) {
    selectedModel.value.tables.splice(index, 1)
  }
}

const editTable = (table: Table) => {
  ElMessage.info('编辑表功能开发中')
}

const addRelation = () => {
  ElMessage.info('添加关系功能开发中')
}

const autoLayout = () => {
  ElMessage.info('自动布局功能开发中')
}

const zoomIn = () => {
  zoomLevel.value = Math.min(zoomLevel.value + 0.1, 2)
}

const zoomOut = () => {
  zoomLevel.value = Math.max(zoomLevel.value - 0.1, 0.5)
}

const saveModel = () => {
  ElMessage.success('模型已保存')
}

// 拖拽相关
let draggedTable: Table | null = null

const onDragStart = (e: DragEvent, table: Table) => {
  draggedTable = table
}

const onDragEnd = (e: DragEvent, table: Table) => {
  if (draggedTable && canvasRef.value) {
    const rect = canvasRef.value.getBoundingClientRect()
    table.x = (e.clientX - rect.left) / zoomLevel.value
    table.y = (e.clientY - rect.top) / zoomLevel.value
  }
  draggedTable = null
}
</script>

<style scoped lang="scss">
.model-designer {
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

.architecture-section {
  padding: 24px;
  background: #fff;
  margin: 16px;
  border-radius: 8px;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.03);
}

.section-title {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;

  h3 {
    margin: 0;
    font-size: 18px;
    font-weight: 600;
    color: #1d2129;
  }
}

.layer-flow {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
}

.layer-card {
  flex: 1;
  padding: 24px;
  border-radius: 12px;
  transition: all 0.3s;
  cursor: pointer;

  &:hover {
    transform: translateY(-4px);
    box-shadow: 0 8px 16px rgba(0, 0, 0, 0.1);
  }

  &.ods {
    background: linear-gradient(135deg, #e8f4ff 0%, #d0e8ff 100%);
    border: 2px solid #409eff;
  }

  &.dwd {
    background: linear-gradient(135deg, #e8fff4 0%, #d0ffe8 100%);
    border: 2px solid #00b42a;
  }

  &.dws {
    background: linear-gradient(135deg, #fff7e8 0%, #ffefd0 100%);
    border: 2px solid #ff7d00;
  }

  &.ads {
    background: linear-gradient(135deg, #ffece8 0%, #ffddd0 100%);
    border: 2px solid #f53f3f;
  }
}

.layer-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 12px;
}

.layer-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 56px;
  height: 56px;
  background: rgba(255, 255, 255, 0.8);
  border-radius: 12px;
  color: #409eff;

  .dwd & {
    color: #00b42a;
  }

  .dws & {
    color: #ff7d00;
  }

  .ads & {
    color: #f53f3f;
  }
}

.layer-info {
  flex: 1;
}

.layer-name {
  font-size: 18px;
  font-weight: 600;
  color: #1d2129;
  margin-bottom: 4px;
}

.layer-subtitle {
  font-size: 13px;
  color: #4e5969;
}

.layer-desc {
  font-size: 12px;
  color: #86909c;
  margin-bottom: 12px;
}

.layer-stats {
  font-size: 20px;
  font-weight: 600;
  color: #1d2129;
}

.flow-arrow {
  color: #86909c;
  font-size: 24px;
}

.designer-container {
  flex: 1;
  display: flex;
  gap: 16px;
  padding: 16px;
  overflow: hidden;
}

.left-panel {
  width: 280px;
  background: #fff;
  border-radius: 8px;
  padding: 16px;
  display: flex;
  flex-direction: column;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.03);
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
    color: #1d2129;
  }
}

.model-list {
  flex: 1;
  overflow-y: auto;
}

.model-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px;
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.3s;
  margin-bottom: 8px;

  &:hover {
    background: #f7f8fa;
  }

  &.active {
    background: #e8f3ff;
    border: 1px solid #409eff;
  }

  .model-icon {
    font-size: 24px;
    color: #409eff;
  }

  .model-info {
    flex: 1;
    min-width: 0;

    .model-name {
      font-size: 14px;
      font-weight: 500;
      color: #1d2129;
      margin-bottom: 4px;
    }

    .model-desc {
      font-size: 12px;
      color: #86909c;
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
    }
  }
}

.canvas-panel {
  flex: 1;
  background: #fff;
  border-radius: 8px;
  display: flex;
  flex-direction: column;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.03);
}

.canvas-toolbar {
  display: flex;
  align-items: center;
  padding: 12px 16px;
  border-bottom: 1px solid #e5e6eb;
}

.canvas-area {
  flex: 1;
  position: relative;
  overflow: auto;
  background: #fafafa;
  background-image: radial-gradient(circle, #e0e0e0 1px, transparent 1px);
  background-size: 20px 20px;
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 100%;
  color: #86909c;

  p {
    margin-top: 16px;
    font-size: 14px;
  }
}

.canvas-content {
  position: relative;
  width: 100%;
  height: 100%;
  min-width: 1200px;
  min-height: 800px;
  transform-origin: 0 0;
}

.table-card {
  position: absolute;
  width: 280px;
  background: #fff;
  border: 1px solid #e5e6eb;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  cursor: move;

  &:hover {
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  }
}

.table-header {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px;
  background: #409eff;
  color: #fff;
  border-radius: 8px 8px 0 0;
  font-weight: 500;

  span {
    flex: 1;
  }
}

.table-fields {
  padding: 8px 0;
}

.field-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 12px;
  font-size: 13px;

  &:hover {
    background: #f7f8fa;
  }

  .field-name {
    flex: 1;
    color: #1d2129;
    font-weight: 500;
  }

  .field-type {
    color: #86909c;
    font-size: 12px;
  }
}

.table-footer {
  padding: 8px 12px;
  border-top: 1px solid #e5e6eb;
  text-align: right;
}

.relations-layer {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  pointer-events: none;
}

.right-panel {
  width: 300px;
  background: #fff;
  border-radius: 8px;
  padding: 16px;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.03);
}

.properties {
  margin-top: 16px;
}
</style>
