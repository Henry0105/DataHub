<template>
  <div class="page-container-tdesign">
    <div class="page-header">
      <div class="page-title">
        <el-icon :size="20" color="#0052d9"><Platform /></el-icon>
        <span>大数据运维管理平台</span>
      </div>
      <div class="page-actions">
        <el-button type="primary" @click="showAddServerDialog = true">
          <el-icon><Plus /></el-icon>
          添加服务器
        </el-button>
        <el-button type="success" @click="showInstallDialog = true">
          <el-icon><Download /></el-icon>
          安装组件
        </el-button>
      </div>
    </div>

    <!-- Tab 导航 -->
    <el-tabs v-model="activeTab" class="main-tabs">
      <!-- 服务器管理 -->
      <el-tab-pane label="服务器管理" name="servers">
        <div class="stats-container">
          <div class="stat-card">
            <div class="stat-icon" style="background: #e6f0ff;">
              <el-icon :size="24" color="#0052d9"><Server /></el-icon>
            </div>
            <div class="stat-content">
              <div class="stat-value">{{ servers.length }}</div>
              <div class="stat-label">服务器总数</div>
            </div>
          </div>
          <div class="stat-card">
            <div class="stat-icon" style="background: #e6fffb;">
              <el-icon :size="24" color="#00b96b"><CircleCheck /></el-icon>
            </div>
            <div class="stat-content">
              <div class="stat-value">{{ servers.filter(s => s.status === '在线').length }}</div>
              <div class="stat-label">在线</div>
            </div>
          </div>
          <div class="stat-card">
            <div class="stat-icon" style="background: #fff7e6;">
              <el-icon :size="24" color="#faad14"><Cpu /></el-icon>
            </div>
            <div class="stat-content">
              <div class="stat-value">{{ totalCpu }}核</div>
              <div class="stat-label">总CPU</div>
            </div>
          </div>
          <div class="stat-card">
            <div class="stat-icon" style="background: #f0f5ff;">
              <el-icon :size="24" color="#597ef7"><Histogram /></el-icon>
            </div>
            <div class="stat-content">
              <div class="stat-value">{{ totalMemory }}GB</div>
              <div class="stat-label">总内存</div>
            </div>
          </div>
        </div>

        <div class="table-card">
          <div class="table-header">
            <h3>服务器列表</h3>
          </div>
          <el-table :data="servers" style="width: 100%">
            <el-table-column prop="name" label="服务器名称" width="150" />
            <el-table-column prop="ip" label="IP地址" width="140" />
            <el-table-column prop="type" label="类型" width="100">
              <template #default="{ row }">
                <el-tag :type="row.type === 'Docker' ? 'primary' : 'success'" size="small">
                  {{ row.type }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="cpu" label="CPU" width="100" />
            <el-table-column prop="memory" label="内存" width="100" />
            <el-table-column prop="disk" label="磁盘" width="100" />
            <el-table-column label="CPU使用率" width="120">
              <template #default="{ row }">
                <el-progress :percentage="row.cpuUsage" :color="getProgressColor(row.cpuUsage)" />
              </template>
            </el-table-column>
            <el-table-column label="内存使用率" width="120">
              <template #default="{ row }">
                <el-progress :percentage="row.memUsage" :color="getProgressColor(row.memUsage)" />
              </template>
            </el-table-column>
            <el-table-column prop="status" label="状态" width="100">
              <template #default="{ row }">
                <el-tag :type="row.status === '在线' ? 'success' : 'danger'" size="small">
                  {{ row.status }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="200" fixed="right">
              <template #default="{ row }">
                <el-button type="primary" size="small" link @click="viewServer(row)">
                  <el-icon><View /></el-icon>
                  详情
                </el-button>
                <el-button type="warning" size="small" link @click="monitorServer(row)">
                  <el-icon><Monitor /></el-icon>
                  监控
                </el-button>
              </template>
            </el-table-column>
          </el-table>
        </div>
      </el-tab-pane>

      <!-- 组件管理 -->
      <el-tab-pane label="组件管理" name="components">
        <div class="stats-container">
          <div class="stat-card">
            <div class="stat-icon" style="background: #e6f0ff;">
              <el-icon :size="24" color="#0052d9"><Service /></el-icon>
            </div>
            <div class="stat-content">
              <div class="stat-value">{{ components.length }}</div>
              <div class="stat-label">已安装组件</div>
            </div>
          </div>
          <div class="stat-card">
            <div class="stat-icon" style="background: #e6fffb;">
              <el-icon :size="24" color="#00b96b"><CircleCheck /></el-icon>
            </div>
            <div class="stat-content">
              <div class="stat-value">{{ components.filter(c => c.status === '运行中').length }}</div>
              <div class="stat-label">运行中</div>
            </div>
          </div>
          <div class="stat-card">
            <div class="stat-icon" style="background: #fff7e6;">
              <el-icon :size="24" color="#faad14"><Warning /></el-icon>
            </div>
            <div class="stat-content">
              <div class="stat-value">{{ components.filter(c => c.status === '已停止').length }}</div>
              <div class="stat-label">已停止</div>
            </div>
          </div>
          <div class="stat-card">
            <div class="stat-icon" style="background: #f0f5ff;">
              <el-icon :size="24" color="#597ef7"><TrendCharts /></el-icon>
            </div>
            <div class="stat-content">
              <div class="stat-value">{{ availableComponents.length }}</div>
              <div class="stat-label">可安装组件</div>
            </div>
          </div>
        </div>

        <div class="table-card">
          <div class="table-header">
            <h3>已安装组件</h3>
          </div>
          <el-table :data="components" style="width: 100%">
            <el-table-column prop="name" label="组件名称" width="150" />
            <el-table-column prop="version" label="版本" width="120" />
            <el-table-column prop="server" label="部署服务器" width="150" />
            <el-table-column prop="port" label="端口" width="100" />
            <el-table-column prop="status" label="状态" width="100">
              <template #default="{ row }">
                <el-tag :type="row.status === '运行中' ? 'success' : 'info'" size="small">
                  {{ row.status }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="installTime" label="安装时间" width="160" />
            <el-table-column label="操作" width="300" fixed="right">
              <template #default="{ row }">
                <el-button 
                  v-if="row.status === '运行中'" 
                  type="warning" 
                  size="small" 
                  link 
                  @click="stopComponent(row)"
                >
                  <el-icon><VideoPause /></el-icon>
                  停止
                </el-button>
                <el-button 
                  v-else 
                  type="success" 
                  size="small" 
                  link 
                  @click="startComponent(row)"
                >
                  <el-icon><VideoPlay /></el-icon>
                  启动
                </el-button>
                <el-button type="primary" size="small" link @click="viewLogs(row)">
                  <el-icon><Document /></el-icon>
                  日志
                </el-button>
                <el-button type="primary" size="small" link @click="configComponent(row)">
                  <el-icon><Setting /></el-icon>
                  配置
                </el-button>
              </template>
            </el-table-column>
          </el-table>
        </div>
      </el-tab-pane>

      <!-- 监控面板 -->
      <el-tab-pane label="监控面板" name="monitor">
        <div class="monitor-container">
          <el-alert
            title="监控功能"
            type="info"
            description="实时监控服务器和组件运行状态,查看资源使用情况"
            :closable="false"
            show-icon
          />
        </div>
      </el-tab-pane>
    </el-tabs>

    <!-- 添加服务器对话框 -->
    <el-dialog v-model="showAddServerDialog" title="添加服务器" width="600px">
      <el-form :model="serverForm" label-width="100px">
        <el-form-item label="服务器名称">
          <el-input v-model="serverForm.name" placeholder="请输入服务器名称" />
        </el-form-item>
        <el-form-item label="IP地址">
          <el-input v-model="serverForm.ip" placeholder="请输入IP地址" />
        </el-form-item>
        <el-form-item label="类型">
          <el-select v-model="serverForm.type" placeholder="请选择类型">
            <el-option label="Docker" value="Docker" />
            <el-option label="CentOS" value="CentOS" />
            <el-option label="Ubuntu" value="Ubuntu" />
          </el-select>
        </el-form-item>
        <el-form-item label="SSH端口">
          <el-input v-model="serverForm.port" placeholder="默认22" />
        </el-form-item>
        <el-form-item label="用户名">
          <el-input v-model="serverForm.username" placeholder="请输入SSH用户名" />
        </el-form-item>
        <el-form-item label="密码">
          <el-input v-model="serverForm.password" type="password" placeholder="请输入SSH密码" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showAddServerDialog = false">取消</el-button>
        <el-button type="primary" @click="addServer">确定</el-button>
      </template>
    </el-dialog>

    <!-- 安装组件对话框 -->
    <el-dialog v-model="showInstallDialog" title="安装组件" width="700px">
      <div class="component-grid">
        <div 
          v-for="item in availableComponents" 
          :key="item.name"
          class="component-card"
          :class="{ selected: selectedComponent === item.name }"
          @click="selectedComponent = item.name"
        >
          <div class="component-header">
            <span class="component-name">{{ item.name }}</span>
            <el-tag size="small">{{ item.category }}</el-tag>
          </div>
          <div class="component-desc">{{ item.description }}</div>
        </div>
      </div>
      <template #footer>
        <el-button @click="showInstallDialog = false">取消</el-button>
        <el-button type="primary" @click="installComponent" :disabled="!selectedComponent">安装</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { ElMessage } from 'element-plus'

// 当前Tab
const activeTab = ref('servers')

// 对话框控制
const showAddServerDialog = ref(false)
const showInstallDialog = ref(false)
const selectedComponent = ref('')

// 服务器表单
const serverForm = ref({
  name: '',
  ip: '',
  type: 'Docker',
  port: '22',
  username: '',
  password: ''
})

// 服务器数据（基于你的3台Docker）
const servers = ref([
  {
    id: 1,
    name: 'Docker-Server-1',
    ip: '192.168.1.101',
    type: 'Docker',
    cpu: '8核',
    memory: '16GB',
    disk: '500GB',
    cpuUsage: 45,
    memUsage: 62,
    status: '在线'
  },
  {
    id: 2,
    name: 'Docker-Server-2',
    ip: '192.168.1.102',
    type: 'Docker',
    cpu: '8核',
    memory: '16GB',
    disk: '500GB',
    cpuUsage: 38,
    memUsage: 55,
    status: '在线'
  },
  {
    id: 3,
    name: 'Docker-Server-3',
    ip: '192.168.1.103',
    type: 'Docker',
    cpu: '8核',
    memory: '16GB',
    disk: '500GB',
    cpuUsage: 52,
    memUsage: 68,
    status: '在线'
  }
])

// 已安装组件
const components = ref([
  {
    id: 1,
    name: 'Hadoop',
    version: '3.3.4',
    server: 'Docker-Server-1',
    port: '9000',
    status: '运行中',
    installTime: '2025-01-10 10:30:00'
  },
  {
    id: 2,
    name: 'Zookeeper',
    version: '3.8.0',
    server: 'Docker-Server-1',
    port: '2181',
    status: '运行中',
    installTime: '2025-01-10 10:35:00'
  },
  {
    id: 3,
    name: 'Kafka',
    version: '3.4.0',
    server: 'Docker-Server-2',
    port: '9092',
    status: '运行中',
    installTime: '2025-01-10 11:00:00'
  },
  {
    id: 4,
    name: 'Flink',
    version: '1.17.1',
    server: 'Docker-Server-2',
    port: '8081',
    status: '已停止',
    installTime: '2025-01-10 11:20:00'
  },
  {
    id: 5,
    name: 'Spark',
    version: '3.4.1',
    server: 'Docker-Server-3',
    port: '8080',
    status: '运行中',
    installTime: '2025-01-10 14:00:00'
  }
])

// 可安装组件
const availableComponents = ref([
  { name: 'Hive', category: '数仓', description: '分布式数据仓库系统' },
  { name: 'HBase', category: '存储', description: 'NoSQL分布式数据库' },
  { name: 'Elasticsearch', category: '搜索', description: '分布式搜索引擎' },
  { name: 'Presto', category: '查询', description: '分布式 SQL查询引擎' },
  { name: 'Airflow', category: '调度', description: '工作流调度平台' },
  { name: 'Redis', category: '缓存', description: '内存数据库' },
])

// 计算属性
const totalCpu = computed(() => {
  return servers.value.reduce((sum, s) => sum + parseInt(s.cpu), 0)
})

const totalMemory = computed(() => {
  return servers.value.reduce((sum, s) => sum + parseInt(s.memory), 0)
})

// 进度条颜色
const getProgressColor = (percentage: number) => {
  if (percentage < 50) return '#00b96b'
  if (percentage < 80) return '#faad14'
  return '#f5222d'
}

// 服务器操作
const addServer = () => {
  ElMessage.success('添加服务器成功')
  showAddServerDialog.value = false
}

const viewServer = (row: any) => {
  ElMessage.info(`查看服务器: ${row.name}`)
}

const monitorServer = (row: any) => {
  ElMessage.info(`监控服务器: ${row.name}`)
}

// 组件操作
const installComponent = () => {
  ElMessage.success(`安装组件: ${selectedComponent.value}`)
  showInstallDialog.value = false
  selectedComponent.value = ''
}

const startComponent = (row: any) => {
  ElMessage.success(`启动组件: ${row.name}`)
  row.status = '运行中'
}

const stopComponent = (row: any) => {
  ElMessage.warning(`停止组件: ${row.name}`)
  row.status = '已停止'
}

const viewLogs = (row: any) => {
  ElMessage.info(`查看日志: ${row.name}`)
}

const configComponent = (row: any) => {
  ElMessage.info(`配置组件: ${row.name}`)
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
  }

  .page-actions {
    display: flex;
    gap: 12px;
  }
}

.main-tabs {
  background: #fff;
  border-radius: 6px;
  padding: 16px 24px;
  box-shadow: 0 1px 2px 0 rgba(0, 0, 0, 0.03);
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

  .table-header {
    margin-bottom: 16px;

    h3 {
      margin: 0;
      font-size: 16px;
      font-weight: 600;
      color: #1f2937;
    }
  }
}

.monitor-container {
  padding: 20px;
}

.component-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 12px;
  margin-bottom: 20px;

  .component-card {
    padding: 16px;
    border: 1px solid #e5e6eb;
    border-radius: 6px;
    cursor: pointer;
    transition: all 0.3s;

    &:hover {
      border-color: #0052d9;
      box-shadow: 0 2px 8px rgba(0, 82, 217, 0.1);
    }

    &.selected {
      border-color: #0052d9;
      background: #f0f7ff;
    }

    .component-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 8px;

      .component-name {
        font-size: 16px;
        font-weight: 600;
        color: #1f2937;
      }
    }

    .component-desc {
      font-size: 13px;
      color: #6b7280;
      line-height: 1.5;
    }
  }
}
</style>
