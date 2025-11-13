<template>
  <div class="dashboard-container">
    <!-- 数据统计卡片 - TDesign 极简风格 -->
    <el-row :gutter="16" class="stats-row">
      <el-col :span="6" v-for="item in statsData" :key="item.title">
        <div class="stat-card-simple">
          <div class="stat-header">
            <span class="stat-title">{{ item.title }}</span>
            <el-icon :size="20" :color="item.color">
              <component :is="item.icon" />
            </el-icon>
          </div>
          <div class="stat-value">{{ item.value }}</div>
          <div class="stat-desc">{{ item.desc }}</div>
        </div>
      </el-col>
    </el-row>

    <!-- 快捷入口 - TDesign 卡片风格 -->
    <div class="section-card">
      <div class="section-header">
        <h3>快捷入口</h3>
      </div>
      <div class="modules-grid">
        <div 
          v-for="module in modules" 
          :key="module.name"
          class="module-item"
          @click="goToModule(module.path)"
        >
          <div class="module-icon" :style="{ background: module.color }">
            <el-icon :size="24">
              <component :is="module.icon" />
            </el-icon>
          </div>
          <div class="module-info">
            <div class="module-name">{{ module.name }}</div>
            <div class="module-desc">{{ module.desc }}</div>
          </div>
        </div>
      </div>
    </div>

    <!-- 系统信息 -->
    <el-row :gutter="16">
      <el-col :span="12">
        <div class="section-card">
          <div class="section-header">
            <h3>系统信息</h3>
          </div>
          <div class="info-list">
            <div class="info-item">
              <span class="info-label">系统版本</span>
              <span class="info-value">v1.0.0</span>
            </div>
            <div class="info-item">
              <span class="info-label">运行环境</span>
              <span class="info-value">生产环境</span>
            </div>
            <div class="info-item">
              <span class="info-label">数据库</span>
              <span class="info-value">MySQL 8.0</span>
            </div>
            <div class="info-item">
              <span class="info-label">缓存</span>
              <span class="info-value">Redis 6.0</span>
            </div>
          </div>
        </div>
      </el-col>
      <el-col :span="12">
        <div class="section-card">
          <div class="section-header">
            <h3>最近更新</h3>
          </div>
          <div class="timeline-list">
            <div class="timeline-item">
              <div class="timeline-dot"></div>
              <div class="timeline-content">
                <div class="timeline-title">新增数据比对功能</div>
                <div class="timeline-time">2025/01/15</div>
              </div>
            </div>
            <div class="timeline-item">
              <div class="timeline-dot"></div>
              <div class="timeline-content">
                <div class="timeline-title">优化数据同步性能</div>
                <div class="timeline-time">2025/01/10</div>
              </div>
            </div>
            <div class="timeline-item">
              <div class="timeline-dot"></div>
              <div class="timeline-content">
                <div class="timeline-title">支持国产数据库</div>
                <div class="timeline-time">2025/01/05</div>
              </div>
            </div>
          </div>
        </div>
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
// 完全基于 TDesign 的设计规范
.dashboard-container {
  .stats-row {
    margin-bottom: 16px;
  }
  
  // 统计卡片 - 极简风格
  .stat-card-simple {
    background: #fff;
    border: 1px solid #dcdcdc;
    border-radius: 3px;
    padding: 20px;
    transition: all 0.2s;
    
    &:hover {
      border-color: #0052d9;
      box-shadow: 0 1px 10px 0 rgba(0, 82, 217, 0.1);
    }
    
    .stat-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 16px;
      
      .stat-title {
        font-size: 14px;
        color: #000000d9;
        font-weight: 400;
      }
    }
    
    .stat-value {
      font-size: 28px;
      font-weight: 600;
      color: #000000e6;
      line-height: 1.2;
      margin-bottom: 8px;
    }
    
    .stat-desc {
      font-size: 12px;
      color: #00000073;
    }
  }
  
  // 区块卡片
  .section-card {
    background: #fff;
    border: 1px solid #dcdcdc;
    border-radius: 3px;
    margin-bottom: 16px;
    
    .section-header {
      padding: 16px 24px;
      border-bottom: 1px solid #e7e7e7;
      
      h3 {
        margin: 0;
        font-size: 16px;
        font-weight: 600;
        color: #000000e6;
      }
    }
  }
  
  // 模块网格
  .modules-grid {
    display: grid;
    grid-template-columns: repeat(3, 1fr);
    gap: 1px;
    background: #e7e7e7;
    
    .module-item {
      background: #fff;
      padding: 20px;
      display: flex;
      align-items: center;
      cursor: pointer;
      transition: all 0.2s;
      
      &:hover {
        background: #f3f3f3;
      }
      
      .module-icon {
        width: 40px;
        height: 40px;
        border-radius: 3px;
        display: flex;
        align-items: center;
        justify-content: center;
        color: #fff;
        margin-right: 12px;
        flex-shrink: 0;
      }
      
      .module-info {
        flex: 1;
        
        .module-name {
          font-size: 14px;
          font-weight: 500;
          color: #000000e6;
          margin-bottom: 4px;
        }
        
        .module-desc {
          font-size: 12px;
          color: #00000073;
        }
      }
    }
  }
  
  // 信息列表
  .info-list {
    padding: 16px 24px;
    
    .info-item {
      display: flex;
      justify-content: space-between;
      padding: 12px 0;
      border-bottom: 1px solid #f3f3f3;
      
      &:last-child {
        border-bottom: none;
      }
      
      .info-label {
        font-size: 14px;
        color: #00000073;
      }
      
      .info-value {
        font-size: 14px;
        color: #000000e6;
        font-weight: 500;
      }
    }
  }
  
  // 时间线
  .timeline-list {
    padding: 16px 24px;
    
    .timeline-item {
      display: flex;
      padding: 12px 0;
      
      &:last-child {
        .timeline-dot::after {
          display: none;
        }
      }
      
      .timeline-dot {
        width: 8px;
        height: 8px;
        border-radius: 50%;
        background: #0052d9;
        margin-right: 12px;
        margin-top: 6px;
        flex-shrink: 0;
        position: relative;
        
        &::after {
          content: '';
          position: absolute;
          left: 3px;
          top: 12px;
          width: 2px;
          height: 32px;
          background: #e7e7e7;
        }
      }
      
      .timeline-content {
        flex: 1;
        
        .timeline-title {
          font-size: 14px;
          color: #000000e6;
          margin-bottom: 4px;
        }
        
        .timeline-time {
          font-size: 12px;
          color: #00000073;
        }
      }
    }
  }
}
</style>

