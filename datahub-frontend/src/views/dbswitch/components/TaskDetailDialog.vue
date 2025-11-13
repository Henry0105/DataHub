<template>
  <el-dialog
    v-model="visibleProxy"
    title="任务详情"
    width="900px"
  >
    <el-descriptions :column="2" border v-if="currentTask">
      <el-descriptions-item label="任务名称">{{ currentTask.name }}</el-descriptions-item>
      <el-descriptions-item label="任务状态">
        <el-tag :type="getStatusType(currentTask.status)">{{ currentTask.status }}</el-tag>
      </el-descriptions-item>
      <el-descriptions-item label="源数据库">
        <el-tag size="small">{{ currentTask.sourceType }}</el-tag> {{ currentTask.source }}
      </el-descriptions-item>
      <el-descriptions-item label="目标数据库">
        <el-tag size="small">{{ currentTask.targetType }}</el-tag> {{ currentTask.target }}
      </el-descriptions-item>
      <el-descriptions-item label="同步类型">{{ currentTask.type }}</el-descriptions-item>
      <el-descriptions-item label="同步进度">
        <el-progress :percentage="currentTask.progress || 0" />
      </el-descriptions-item>
      <el-descriptions-item label="创建时间">{{ currentTask.createTime }}</el-descriptions-item>
      <el-descriptions-item label="更新时间">{{ currentTask.updateTime }}</el-descriptions-item>
      <el-descriptions-item label="同步表数量">{{ currentTask.tableCount }}</el-descriptions-item>
      <el-descriptions-item label="同步记录数">{{ currentTask.recordCount }}</el-descriptions-item>
    </el-descriptions>

    <el-divider content-position="left">同步日志</el-divider>
    <div class="log-container">
      <div v-for="(log, index) in syncLogs" :key="index" class="log-item">
        <span class="log-text">{{ log }}</span>
      </div>
      <div v-if="syncLogs.length === 0" class="log-empty">
        <el-empty description="暂无日志" />
      </div>
    </div>
  </el-dialog>
</template>

<script setup lang="ts">
import { computed, watch } from 'vue'
import { syncApi } from '@/api/sync'

const props = defineProps({
  visible: { type: Boolean, required: true },
  currentTask: { type: Object, default: null },
  syncLogs: { type: Array as () => string[], default: () => [] },
})

const emit = defineEmits(['update:visible', 'update:syncLogs'])

const visibleProxy = computed({
  get: () => props.visible,
  set: (val: boolean) => emit('update:visible', val),
})

// 当对话框打开时，加载任务日志
watch(() => props.visible, async (newVal) => {
  if (newVal && props.currentTask?.id) {
    try {
      const logs = await syncApi.getLogs(props.currentTask.id)
      emit('update:syncLogs', logs)
    } catch (error) {
      console.error('加载日志失败:', error)
    }
  }
})

function getStatusType(status: any) {
  switch (status) {
    case '运行中':
    case 1:
      return 'warning'
    case '已完成':
    case 2:
      return 'success'
    case '暂停':
    case 0:
      return 'info'
    case '失败':
    case 3:
      return 'danger'
    default:
      return 'info'
  }
}
</script>

<style scoped>
.log-container {
  display: flex;
  flex-direction: column;
  gap: 8px;
  max-height: 300px;
  overflow: auto;
  padding: 8px;
  background: #fafafa;
  border: 1px solid #eee;
  border-radius: 8px;
}

.log-item {
  display: flex;
  gap: 12px;
  font-size: 12px;
}

.log-time {
  color: #888;
}

.log-level {
  font-weight: 600;
}

.log-message {
  color: #333;
}
</style>
