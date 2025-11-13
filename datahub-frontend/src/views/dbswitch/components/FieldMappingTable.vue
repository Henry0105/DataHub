<template>
  <div class="visual-field-mapping">
    <!-- 操作按钮栏 -->
    <div class="mapping-toolbar">
      <div class="toolbar-left">
        <el-icon :size="18" color="#165dff"><Connection /></el-icon>
        <span class="toolbar-title">字段映射配置</span>
        <el-tag size="small" type="info">共 {{ sourceFields.length }} 个字段</el-tag>
      </div>
      <div class="toolbar-right">
        <template v-if="hasTargetTable">
          <el-button size="small" @click="autoMapBySameName">
            <el-icon><Connection /></el-icon>
            同名映射
          </el-button>
          <el-button size="small" @click="autoMapByOrder">
            <el-icon><Sort /></el-icon>
            同行映射
          </el-button>
          <el-button size="small" @click="clearMapping">
            <el-icon><Delete /></el-icon>
            清除映射
          </el-button>
        </template>
        <el-tag v-else size="small" type="warning">请填写目标表后配置映射</el-tag>
      </div>
    </div>

    <!-- 左右对照区域 -->
    <div class="mapping-container" ref="containerRef" :class="{ 'source-only': !hasTargetTable }">
      <!-- 源字段列表 -->
      <div class="field-column source-column">
        <div class="column-header">
          <el-icon :size="16" color="#165dff"><Coin /></el-icon>
          <span>源表字段</span>
        </div>
        <div class="field-list">
          <div
            v-for="(field, index) in sourceFields"
            :key="field.columnName"
            :ref="el => setSourceRef(el, index)"
            class="field-item"
            :class="{ active: selectedSource === index }"
            @click="handleSourceClick(index)"
          >
            <div class="field-info">
              <el-icon :size="14" color="#4e5969"><Document /></el-icon>
              <span class="field-name">{{ field.columnName }}</span>
            </div>
            <el-tag size="small" type="info" effect="plain">{{ field.dataType }}</el-tag>
          </div>
        </div>
      </div>

      <!-- SVG连线画布 - 只在有目标表时显示 -->
      <svg v-if="hasTargetTable" class="connection-canvas" ref="canvasRef">
        <line
          v-for="(conn, index) in connections"
          :key="index"
          :x1="conn.x1"
          :y1="conn.y1"
          :x2="conn.x2"
          :y2="conn.y2"
          :stroke="conn.color"
          stroke-width="2"
          class="connection-line"
        />
        <circle
          v-for="(conn, index) in connections"
          :key="'start-' + index"
          :cx="conn.x1"
          :cy="conn.y1"
          r="4"
          :fill="conn.color"
        />
        <circle
          v-for="(conn, index) in connections"
          :key="'end-' + index"
          :cx="conn.x2"
          :cy="conn.y2"
          r="4"
          :fill="conn.color"
        />
      </svg>

      <!-- 目标字段列表 - 只在有目标表时显示 -->
      <div v-if="hasTargetTable" class="field-column target-column">
        <div class="column-header">
          <el-icon :size="16" color="#00b96b"><Coin /></el-icon>
          <span>目标表字段</span>
        </div>
        <div class="field-list">
          <div
            v-for="(field, index) in targetFields"
            :key="index"
            :ref="el => setTargetRef(el, index)"
            class="field-item"
            :class="{ active: selectedTarget === index, mapped: field.mapped }"
            @click="handleTargetClick(index)"
          >
            <el-tag size="small" type="success" effect="plain">{{ field.dataType }}</el-tag>
            <div class="field-info">
              <span class="field-name">{{ field.columnName }}</span>
              <el-icon :size="14" color="#4e5969"><Document /></el-icon>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, nextTick, watch } from 'vue'
import { Connection, Document, Coin, Sort, Delete } from '@element-plus/icons-vue'

interface FieldMapping {
  sourceColumn: string
  sourceType: string
  targetColumn: string
  targetType: string
}

interface Field {
  columnName: string
  dataType: string
  mapped?: boolean
}

interface Connection {
  sourceIndex: number
  targetIndex: number
  x1: number
  y1: number
  x2: number
  y2: number
  color: string
}

const props = defineProps<{ 
  fieldMappings: FieldMapping[]
  hasTargetTable?: boolean  // 是否有目标表
  mappingMode?: 'same' | 'type' | 'custom'  // 映射模式
  mappingLocked?: boolean  // 映射是否锁定
}>()

// 为 props 提供默认值
const hasTargetTable = computed(() => props.hasTargetTable ?? false)
const mappingMode = computed(() => props.mappingMode ?? 'same')

const emit = defineEmits<{ (e: 'update', index: number, field: string, value: any): void }>()

// 源字段和目标字段
const sourceFields = ref<Field[]>([])
const targetFields = ref<Field[]>([])

// 映射关系
const mappingRelations = ref<Map<number, number>>(new Map())

// 选中的字段
const selectedSource = ref<number | null>(null)
const selectedTarget = ref<number | null>(null)

// DOM引用
const containerRef = ref<HTMLElement>()
const canvasRef = ref<SVGSVGElement>()
const sourceRefs = ref<(HTMLElement | null)[]>([])
const targetRefs = ref<(HTMLElement | null)[]>([])

// 设置ref
const setSourceRef = (el: any, index: number) => {
  if (el) sourceRefs.value[index] = el
}

const setTargetRef = (el: any, index: number) => {
  if (el) targetRefs.value[index] = el
}

// 计算连线坐标
const connections = computed<Connection[]>(() => {
  const conns: Connection[] = []
  const colors = ['#165dff', '#00b96b', '#f7ba1e', '#f53f3f', '#722ed1']
  
  mappingRelations.value.forEach((targetIndex, sourceIndex) => {
    const sourceEl = sourceRefs.value[sourceIndex]
    const targetEl = targetRefs.value[targetIndex]
    
    if (sourceEl && targetEl && containerRef.value) {
      const containerRect = containerRef.value.getBoundingClientRect()
      const sourceRect = sourceEl.getBoundingClientRect()
      const targetRect = targetEl.getBoundingClientRect()
      
      conns.push({
        sourceIndex,
        targetIndex,
        x1: sourceRect.right - containerRect.left,
        y1: sourceRect.top - containerRect.top + sourceRect.height / 2,
        x2: targetRect.left - containerRect.left,
        y2: targetRect.top - containerRect.top + targetRect.height / 2,
        color: colors[conns.length % colors.length]
      })
    }
  })
  
  return conns
})

// 初始化字段列表
const initFields = () => {
  sourceFields.value = props.fieldMappings.map(m => ({
    columnName: m.sourceColumn,
    dataType: m.sourceType
  }))
  
  // 只在有目标表时初始化目标字段
  if (hasTargetTable.value) {
    targetFields.value = props.fieldMappings
      .filter(m => m.targetColumn) // 过滤掉没有目标字段的
      .map(m => ({
        columnName: m.targetColumn,
        dataType: m.targetType,
        mapped: false
      }))
    
    // 根据映射模式初始化映射关系（只在有目标字段时）
    if (targetFields.value.length > 0) {
      if (mappingMode.value === 'same') {
        // 同名映射：自动建立映射
        autoMapBySameName()
      } else if (mappingMode.value === 'type') {
        // 类型映射：同行映射
        autoMapByOrder()
      }
    }
  } else {
    targetFields.value = []
    mappingRelations.value.clear()
  }
}

// 同名映射
const autoMapBySameName = () => {
  mappingRelations.value.clear()
  
  sourceFields.value.forEach((sourceField, sourceIndex) => {
    const targetIndex = targetFields.value.findIndex(
      tf => tf.columnName.toLowerCase() === sourceField.columnName.toLowerCase()
    )
    if (targetIndex !== -1) {
      mappingRelations.value.set(sourceIndex, targetIndex)
      targetFields.value[targetIndex].mapped = true
    }
  })
  
  updateMappings()
  nextTick(() => {
    drawConnections()
  })
}

// 同行映射
const autoMapByOrder = () => {
  mappingRelations.value.clear()
  
  const minLength = Math.min(sourceFields.value.length, targetFields.value.length)
  for (let i = 0; i < minLength; i++) {
    mappingRelations.value.set(i, i)
    targetFields.value[i].mapped = true
  }
  
  updateMappings()
  nextTick(() => {
    drawConnections()
  })
}

// 清除映射
const clearMapping = () => {
  mappingRelations.value.clear()
  targetFields.value.forEach(f => f.mapped = false)
  selectedSource.value = null
  selectedTarget.value = null
  updateMappings()
}

// 处理源字段点击
const handleSourceClick = (index: number) => {
  if (selectedSource.value === index) {
    selectedSource.value = null
    return
  }
  
  selectedSource.value = index
  
  // 如果目标字段也选中了，建立映射
  if (selectedTarget.value !== null) {
    mappingRelations.value.set(index, selectedTarget.value)
    targetFields.value[selectedTarget.value].mapped = true
    selectedSource.value = null
    selectedTarget.value = null
    updateMappings()
    nextTick(() => {
      drawConnections()
    })
  }
}

// 处理目标字段点击
const handleTargetClick = (index: number) => {
  if (selectedTarget.value === index) {
    selectedTarget.value = null
    return
  }
  
  selectedTarget.value = index
  
  // 如果源字段也选中了，建立映射
  if (selectedSource.value !== null) {
    mappingRelations.value.set(selectedSource.value, index)
    targetFields.value[index].mapped = true
    selectedSource.value = null
    selectedTarget.value = null
    updateMappings()
    nextTick(() => {
      drawConnections()
    })
  }
}

// 更新映射关系到父组件
const updateMappings = () => {
  mappingRelations.value.forEach((targetIndex, sourceIndex) => {
    // 安全检查：确保目标字段存在
    if (targetFields.value[targetIndex]) {
      emit('update', sourceIndex, 'targetColumn', targetFields.value[targetIndex].columnName)
      emit('update', sourceIndex, 'targetType', targetFields.value[targetIndex].dataType)
    }
  })
}

// 绘制连线
const drawConnections = () => {
  // 连线由computed属性自动计算
}

// 监听尺寸变化
watch(() => props.fieldMappings, () => {
  initFields()
}, { deep: true })

// 监听目标表状态和映射模式变化
watch(() => [hasTargetTable.value, mappingMode.value], () => {
  initFields()
  nextTick(() => {
    drawConnections()
  })
})

onMounted(() => {
  initFields()
  nextTick(() => {
    drawConnections()
  })
})
</script>

<style scoped lang="scss">
.visual-field-mapping {
  background: #fff;
  border-radius: 8px;
  overflow: hidden;
}

// 工具栏
.mapping-toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 20px;
  background: #f7f8fa;
  border-bottom: 1px solid #e5e6eb;

  .toolbar-left {
    display: flex;
    align-items: center;
    gap: 12px;

    .toolbar-title {
      font-size: 16px;
      font-weight: 600;
      color: #1d2129;
    }
  }

  .toolbar-right {
    display: flex;
    gap: 8px;
  }
}

// 映射容器
.mapping-container {
  position: relative;
  display: flex;
  padding: 20px;
  gap: 40px;
  min-height: 400px;
  max-height: 600px;
  overflow: hidden;
  
  // 只有源字段时，占满宽度
  &.source-only {
    .field-column {
      max-width: 100%;
    }
  }
}

// 字段列
.field-column {
  flex: 1;
  display: flex;
  flex-direction: column;
  border: 1px solid #e5e6eb;
  border-radius: 8px;
  overflow: hidden;
  background: #fff;

  .column-header {
    display: flex;
    align-items: center;
    gap: 8px;
    padding: 12px 16px;
    background: linear-gradient(135deg, #e8f3ff 0%, #f0f7ff 100%);
    border-bottom: 1px solid #e5e6eb;
    font-size: 14px;
    font-weight: 600;
    color: #1d2129;
  }

  &.target-column .column-header {
    background: linear-gradient(135deg, #e8fff0 0%, #f0fff5 100%);
  }

  .field-list {
    flex: 1;
    overflow-y: auto;
    padding: 8px;

    &::-webkit-scrollbar {
      width: 6px;
    }

    &::-webkit-scrollbar-thumb {
      background: #d1d5db;
      border-radius: 3px;
    }
  }
}

// 字段项
.field-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 10px 12px;
  margin-bottom: 6px;
  border: 1px solid #e5e6eb;
  border-radius: 6px;
  background: #fff;
  cursor: pointer;
  transition: all 0.2s;

  &:hover {
    border-color: #165dff;
    background: #f5f8ff;
    transform: translateX(2px);
  }

  &.active {
    border-color: #165dff;
    background: #e8f3ff;
    box-shadow: 0 2px 8px rgba(22, 93, 255, 0.15);
  }

  &.mapped {
    background: #f0fff5;
    border-color: #00b96b;
  }

  .field-info {
    display: flex;
    align-items: center;
    gap: 8px;

    .field-name {
      font-size: 13px;
      font-weight: 500;
      color: #1d2129;
    }
  }
}

.target-column .field-item {
  flex-direction: row-reverse;

  &:hover {
    transform: translateX(-2px);
  }

  .field-info {
    flex-direction: row-reverse;
  }
}

// SVG连线画布
.connection-canvas {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  pointer-events: none;
  z-index: 1;

  .connection-line {
    transition: all 0.3s;
    stroke-dasharray: 0;
    animation: drawLine 0.5s ease-out;
  }
}

@keyframes drawLine {
  from {
    stroke-dasharray: 1000;
    stroke-dashoffset: 1000;
  }
  to {
    stroke-dasharray: 1000;
    stroke-dashoffset: 0;
  }
}
</style>