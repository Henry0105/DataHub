<template>
  <div class="page-container">
    <div class="page-header">
      <div class="page-title">
        <el-icon :size="20"><Lock /></el-icon>
        <span>数据权限管理</span>
      </div>
    </div>
    
    <el-alert
      title="功能开发中"
      type="info"
      description="数据权限管理功能正在开发中，敬请期待..."
      :closable="false"
      show-icon
    />
  </div>
</template>

<script setup lang="ts">
import { Lock } from '@element-plus/icons-vue'
</script>

<style scoped lang="scss">
.page-container {
  padding: 24px;
}

.page-header {
  margin-bottom: 24px;
  
  .page-title {
    display: flex;
    align-items: center;
    gap: 8px;
    font-size: 18px;
    font-weight: 600;
  }
}
</style>
