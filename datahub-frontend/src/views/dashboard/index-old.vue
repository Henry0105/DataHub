<template>
  <div class="dashboard-container">
    <el-row :gutter="20">
      <!-- 统计卡片 -->
      <el-col :span="6" v-for="item in statsData" :key="item.title">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-info">
              <p class="stat-title">{{ item.title }}</p>
              <h2 class="stat-value">{{ item.value }}</h2>
              <p class="stat-desc">{{ item.desc }}</p>
            </div>
            <div class="stat-icon" :style="{ background: item.color }">
              <el-icon :size="32">
                <component :is="item.icon" />
              </el-icon>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 快捷入口 -->
    <el-card class="quick-entry" style="margin-top: 20px">
      <template #header>
        <div class="card-header">
          <span>快捷入口</span>
        </div>
      </template>
      <el-row :gutter="20">
        <el-col :span="4" v-for="module in modules" :key="module.name">
          <div class="module-card" @click="goToModule(module.path)">
            <el-icon :size="40" :color="module.color">
              <component :is="module.icon" />
            </el-icon>
            <p class="module-name">{{ module.name }}</p>
            <p class="module-desc">{{ module.desc }}</p>
          </div>
        </el-col>
      </el-row>
    </el-card>

    <!-- 系统信息 -->
    <el-row :gutter="20" style="margin-top: 20px">
      <el-col :span="12">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>系统信息</span>
            </div>
          </template>
          <el-descriptions :column="1" border>
            <el-descriptions-item label="系统版本">v1.0.0</el-descriptions-item>
            <el-descriptions-item label="运行环境">生产环境</el-descriptions-item>
            <el-descriptions-item label="数据库">MySQL 8.0</el-descriptions-item>
            <el-descriptions-item label="缓存">Redis 6.0</el-descriptions-item>
          </el-descriptions>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>最近更新</span>
            </div>
          </template>
          <el-timeline>
            <el-timeline-item timestamp="2025/01/15" placement="top">
              <p>新增数据比对功能</p>
            </el-timeline-item>
            <el-timeline-item timestamp="2025/01/10" placement="top">
              <p>优化数据同步性能</p>
            </el-timeline-item>
            <el-timeline-item timestamp="2025/01/05" placement="top">
              <p>支持国产数据库</p>
            </el-timeline-item>
          </el-timeline>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router'

const router = useRouter()

const statsData = [
  {
    title: '数据源总数',
    value: '28',
    desc: '较昨日 +3',
    icon: 'Database',
    color: '#0052d9',
  },
  {
    title: '同步任务',
    value: '156',
    desc: '运行中 120',
    icon: 'Connection',
    color: '#029cd4',
  },
  {
    title: '数据表',
    value: '1,234',
    desc: '活跃表 980',
    icon: 'Document',
    color: '#00a870',
  },
  {
    title: '数据量',
    value: '8.5TB',
    desc: '今日新增 120GB',
    icon: 'Coin',
    color: '#0594fa',
  },
]

const modules = [
  { name: '数据源平台', desc: 'Chat2DB', icon: 'Database', path: '/datasource', color: '#0052d9' },
  { name: '数据库同步', desc: 'DBSwitch', icon: 'Connection', path: '/dbswitch', color: '#029cd4' },
  { name: '数据中枢', desc: 'DataCap', icon: 'Coin', path: '/datacap', color: '#0594fa' },
  { name: '数据集成', desc: 'Transport', icon: 'Share', path: '/transport', color: '#00a870' },
  { name: '数据集成平台', desc: 'Tis', icon: 'Share', path: '/tis', color: '#2ba471' },
  { name: '数据同步平台', desc: 'SeaTunnel', icon: 'Refresh', path: '/seatunnel', color: '#0052d9' },
]

const goToModule = (path: string) => {
  router.push(path)
}
</script>

<style scoped lang="scss">
// TDesign 风格的首页看板
.dashboard-container {
  :deep(.el-card) {
    border-radius: 8px;
    border: 1px solid #e5e7eb;
    box-shadow: 0 1px 2px 0 rgba(0, 0, 0, 0.03);
    transition: all 0.3s;
    
    &:hover {
      box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.1);
    }
    
    .el-card__header {
      border-bottom: 1px solid #e5e7eb;
      padding: 16px 20px;
      font-weight: 500;
      color: #1f2937;
    }
  }
  
  .stat-card {
    :deep(.el-card__body) {
      padding: 24px;
    }
    
    .stat-content {
      display: flex;
      justify-content: space-between;
      align-items: center;
      
      .stat-info {
        .stat-title {
          font-size: 14px;
          color: #6b7280;
          margin-bottom: 12px;
          font-weight: 400;
        }
        
        .stat-value {
          font-size: 32px;
          font-weight: 600;
          color: #1f2937;
          margin-bottom: 8px;
          letter-spacing: -0.5px;
        }
        
        .stat-desc {
          font-size: 13px;
          color: #9ca3af;
        }
      }
      
      .stat-icon {
        width: 48px;
        height: 48px;
        border-radius: 8px;
        display: flex;
        align-items: center;
        justify-content: center;
        color: #fff;
        background: var(--icon-bg);
      }
    }
  }
  
  .quick-entry {
    :deep(.el-card__body) {
      padding: 20px;
    }
  }
  
  .module-card {
    text-align: center;
    padding: 24px 16px;
    cursor: pointer;
    transition: all 0.3s;
    border-radius: 8px;
    border: 1px solid transparent;
    
    &:hover {
      background: #f9fafb;
      border-color: #e5e7eb;
      transform: translateY(-4px);
      box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.1);
    }
    
    .module-name {
      font-size: 15px;
      font-weight: 500;
      margin: 12px 0 6px;
      color: #1f2937;
    }
    
    .module-desc {
      font-size: 13px;
      color: #9ca3af;
    }
  }
  
  :deep(.el-descriptions) {
    .el-descriptions__label {
      color: #6b7280;
      font-weight: 400;
    }
    
    .el-descriptions__content {
      color: #1f2937;
      font-weight: 500;
    }
  }
  
  :deep(.el-timeline) {
    padding-left: 0;
    
    .el-timeline-item__timestamp {
      color: #9ca3af;
      font-size: 13px;
    }
    
    .el-timeline-item__content {
      color: #4b5563;
      font-size: 14px;
    }
  }
}
</style>

