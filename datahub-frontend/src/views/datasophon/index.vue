<template>
  <div class="page-container-tdesign">
    <!-- 四个模块卡片 -->
    <div class="modules-container">
      <!-- 模块1: 主机管理 -->
      <div class="module-card">
        <div class="card-header">
          <div class="header-left">
            <h3>主机管理</h3>
            <el-tag size="small">{{ servers.length }}台</el-tag>
          </div>
          <div class="header-right">
            <el-input
              v-model="serverSearchForm.ip"
              placeholder="请输入IP"
              clearable
              style="width: 160px"
            />
            <el-input
              v-model="serverSearchForm.name"
              placeholder="请输入主机名"
              clearable
              style="width: 160px"
            />
            <el-select
              v-model="serverSearchForm.vendor"
              placeholder="请选择厂商"
              clearable
              style="width: 130px"
            >
              <el-option label="阿里云" value="阿里云" />
              <el-option label="腾讯云" value="腾讯云" />
              <el-option label="华为云" value="华为云" />
              <el-option label="UCloud" value="UCloud" />
              <el-option label="AWS" value="AWS" />
            </el-select>
            <el-button type="primary" @click="searchServers">
              搜索
            </el-button>
            <el-button type="primary" @click="showAddServerDialog = true">
              添加主机
            </el-button>
            <el-button @click="resetServerSearch">
              重置
            </el-button>
          </div>
        </div>
        <div class="card-body">

          <el-table 
            :data="paginatedServers" 
            style="width: 100%" 
            size="small"
            :scrollbar-always-on="true"
            :max-height="600"
          >
            <el-table-column type="selection" width="55" fixed="left" />
            <el-table-column prop="id" label="序号" width="70" fixed="left" />
            <el-table-column prop="name" label="主机名" min-width="160" />
            <el-table-column prop="ip" label="IP地址" min-width="145" />
            <el-table-column prop="vendor" label="厂商" min-width="90" />
            <el-table-column label="核数" min-width="70">
              <template #default="{ row }">
                {{ row.cpu }}
              </template>
            </el-table-column>
            <el-table-column label="内存使用" min-width="160">
              <template #default="{ row }">
                <div style="display: flex; align-items: center; gap: 8px">
                  <span style="font-size: 12px; color: #86909c">{{ row.memory }}</span>
                  <el-progress
                    :percentage="row.memUsage || 0"
                    :color="getProgressColor(row.memUsage || 0)"
                    :show-text="false"
                    style="flex: 1"
                  />
                </div>
              </template>
            </el-table-column>
            <el-table-column label="磁盘使用" min-width="160">
              <template #default="{ row }">
                <div style="display: flex; align-items: center; gap: 8px">
                  <span style="font-size: 12px; color: #86909c">{{ row.disk }}</span>
                  <el-progress
                    :percentage="row.diskUsage || 0"
                    :color="getProgressColor(row.diskUsage || 0)"
                    :show-text="false"
                    style="flex: 1"
                  />
                </div>
              </template>
            </el-table-column>
            <el-table-column prop="rack" label="机架" min-width="120" :show-overflow-tooltip="true" />
            <el-table-column label="状态" min-width="80">
              <template #default="{ row }">
                <span class="status-badge" :class="row.status === '在线' ? 'status-online' : 'status-offline'">
                  <span class="status-dot"></span>
                  {{ row.status }}
                </span>
              </template>
            </el-table-column>
            <el-table-column prop="cpuArch" label="Cpu架构" min-width="105" />
            <el-table-column label="角色" min-width="90">
              <template #default="{ row }">
                <el-tag size="small" type="info">{{ row.type }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="updateTime" label="更新时间" min-width="170" :show-overflow-tooltip="true" />
            <el-table-column label="操作" width="240" fixed="right" align="left" header-align="left">
              <template #default="{ row }">
                <div style="white-space: nowrap; padding: 0 12px 0 0">
                  <el-button type="primary" size="small" text @click="viewServer(row)">
                    <el-icon><View /></el-icon>
                    查看
                  </el-button>
                  <el-button type="primary" size="small" text @click="monitorServer(row)" style="margin-left: 8px">
                    <el-icon><Monitor /></el-icon>
                    监控
                  </el-button>
                  <el-button type="danger" size="small" text @click="deleteServer(row)" style="margin-left: 8px">
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
            v-model:current-page="serverPage"
            v-model:page-size="serverPageSize"
            :page-sizes="[10, 20, 30, 50]"
            layout="total, sizes, prev, pager, next, jumper"
            :total="servers.length"
          />
        </div>
      </div>

      <!-- 模块2: 服务管理 -->
      <div class="module-card">
        <div class="card-header">
          <div class="header-left">
            <h3>服务管理</h3>
            <el-tag size="small">{{ components.length }}个组件</el-tag>
          </div>
          <div class="header-right">
            <!-- 搜索框 -->
            <el-input
              v-model="componentSearchText"
              placeholder="搜索组件名称"
              style="width: 200px; margin-right: 8px"
              clearable
            >
              <template #prefix>
                <el-icon><Search /></el-icon>
              </template>
            </el-input>
            
            <!-- 状态筛选 -->
            <el-select
              v-model="componentStatusFilter"
              placeholder="筛选状态"
              style="width: 120px; margin-right: 8px"
              clearable
            >
              <el-option label="已安装" value="installed" />
              <el-option label="未安装" value="uninstalled" />
              <el-option label="运行中" value="running" />
              <el-option label="已停止" value="stopped" />
            </el-select>
            
            <!-- 批量操作按钮 -->
            <el-button 
              type="success" 
              @click="batchStartComponents"
              :disabled="selectedComponents.length === 0"
              style="margin-right: 8px"
            >
              <el-icon><VideoPlay /></el-icon>
              批量启动
            </el-button>
            <el-button 
              type="warning" 
              @click="batchStopComponents"
              :disabled="selectedComponents.length === 0"
              style="margin-right: 8px"
            >
              <el-icon><VideoPause /></el-icon>
              批量停止
            </el-button>
            
            <!-- 安装组件按钮 -->
            <el-button type="primary" @click="showInstallDialog = true">
              <el-icon><Download /></el-icon>
              安装组件
            </el-button>
          </div>
        </div>
        <div class="card-body">
          <el-table 
            :data="paginatedComponents" 
            style="width: 100%" 
            size="small"
            :max-height="600"
            @selection-change="handleComponentSelectionChange"
          >
            <el-table-column type="selection" width="55" fixed="left" />
            <el-table-column label="组件" min-width="150">
              <template #default="{ row }">
                <div style="display: flex; align-items: center; gap: 12px">
                  <img :src="row.logo" alt="" style="width: 32px; height: 32px; border-radius: 4px" />
                  <span style="font-weight: 500; color: #1d2129">{{ row.name }}</span>
                </div>
              </template>
            </el-table-column>
            <el-table-column prop="description" label="备注" min-width="200" :show-overflow-tooltip="true" />
            <el-table-column prop="version" label="版本" min-width="100" />
            <el-table-column prop="server" label="部署服务器" min-width="140" />
            <el-table-column prop="port" label="端口" min-width="80" />
            <el-table-column prop="path" label="安装路径" min-width="140" :show-overflow-tooltip="true" />
            <el-table-column label="安装状态" min-width="100">
              <template #default="{ row }">
                <el-tag :type="row.installed ? 'success' : 'info'" size="small">
                  {{ row.installed ? '已安装' : '未安装' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="运行状态" min-width="100">
              <template #default="{ row }">
                <el-tag v-if="row.installed" :type="row.status === '运行中' ? 'success' : 'warning'" size="small">
                  {{ row.status }}
                </el-tag>
                <span v-else style="color: #86909c; font-size: 12px">-</span>
              </template>
            </el-table-column>
            <el-table-column prop="updateTime" label="更新时间" min-width="165" :show-overflow-tooltip="true" />
            <el-table-column label="操作" width="400" fixed="right" align="left" header-align="left">
              <template #default="{ row }">
                <div style="white-space: nowrap; padding: 0 12px 0 0">
                  <!-- 未安装状态 -->
                  <template v-if="!row.installed">
                    <el-button type="success" size="small" text @click="installComponent(row)">
                      <el-icon><Download /></el-icon>
                      安装
                    </el-button>
                  </template>
                  
                  <!-- 已安装但已停止 -->
                  <template v-else-if="row.status === '已停止'">
                    <el-button type="success" size="small" text @click="startComponent(row)">
                      <el-icon><VideoPlay /></el-icon>
                      启动
                    </el-button>
                    <el-button type="primary" size="small" text @click="restartComponent(row)" style="margin-left: 8px">
                      <el-icon><RefreshRight /></el-icon>
                      重启
                    </el-button>
                    <el-button type="primary" size="small" text @click="viewLogs(row)" style="margin-left: 8px">
                      <el-icon><Document /></el-icon>
                      日志
                    </el-button>
                    <el-button type="primary" size="small" text @click="configComponent(row)" style="margin-left: 8px">
                      <el-icon><Setting /></el-icon>
                      配置
                    </el-button>
                    <el-button type="danger" size="small" text @click="uninstallComponent(row)" style="margin-left: 8px">
                      <el-icon><Delete /></el-icon>
                      卸载
                    </el-button>
                  </template>
                  
                  <!-- 运行中状态 -->
                  <template v-else>
                    <el-button type="warning" size="small" text @click="stopComponent(row)">
                      <el-icon><VideoPause /></el-icon>
                      停止
                    </el-button>
                    <el-button type="primary" size="small" text @click="restartComponent(row)" style="margin-left: 8px">
                      <el-icon><RefreshRight /></el-icon>
                      重启
                    </el-button>
                    <el-button type="primary" size="small" text @click="viewLogs(row)" style="margin-left: 8px">
                      <el-icon><Document /></el-icon>
                      日志
                    </el-button>
                    <el-button type="primary" size="small" text @click="configComponent(row)" style="margin-left: 8px">
                      <el-icon><Setting /></el-icon>
                      配置
                    </el-button>
                    <el-button type="danger" size="small" text @click="uninstallComponent(row)" style="margin-left: 8px">
                      <el-icon><Delete /></el-icon>
                      卸载
                    </el-button>
                  </template>
                </div>
              </template>
            </el-table-column>
          </el-table>
        </div>
        <div class="card-pagination">
          <el-pagination
            v-model:current-page="componentPage"
            v-model:page-size="componentPageSize"
            :page-sizes="[10, 20, 30, 50]"
            layout="total, sizes, prev, pager, next, jumper"
            :total="components.length"
          />
        </div>
      </div>

      <!-- 模块3: 告警管理 -->
      <div class="module-card">
        <div class="card-header">
          <div class="header-left">
            <h3>告警管理</h3>
          </div>
          <div class="header-right">
            <el-button type="primary" @click="showAddAlertDialog = true">
              <el-icon><Plus /></el-icon>
              添加规则
            </el-button>
          </div>
        </div>
        <div class="card-body">
          <el-table 
            :data="paginatedAlerts" 
            style="width: 100%" 
            size="small"
            :max-height="600"
          >
            <el-table-column prop="name" label="告警名称" min-width="150" />
            <el-table-column label="级别" min-width="80">
              <template #default="{ row }">
                <el-tag :type="row.level === '严重' ? 'danger' : row.level === '警告' ? 'warning' : 'info'" size="small">
                  {{ row.level }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="source" label="来源" min-width="120" />
            <el-table-column prop="message" label="告警信息" min-width="200" :show-overflow-tooltip="true" />
            <el-table-column prop="time" label="时间" min-width="170" />
            <el-table-column label="操作" width="180" fixed="right" align="left" header-align="left">
              <template #default="{ row }">
                <div style="white-space: nowrap; padding: 0 12px 0 0">
                  <el-button type="primary" size="small" text @click="handleAlert(row)">
                    处理
                  </el-button>
                  <el-button type="danger" size="small" text @click="deleteAlert(row)" style="margin-left: 8px">
                    删除
                  </el-button>
                </div>
              </template>
            </el-table-column>
          </el-table>
        </div>
        <div class="card-pagination">
          <el-pagination
            v-model:current-page="alertPage"
            v-model:page-size="alertPageSize"
            :page-sizes="[10, 20, 30, 50]"
            layout="total, sizes, prev, pager, next, jumper"
            :total="alerts.length"
          />
        </div>
      </div>

      <!-- 模块4: 系统设置 -->
      <div class="module-card">
        <div class="card-header">
          <div class="header-left">
            <h3>系统设置</h3>
          </div>
        </div>
        <div class="card-body">
          <el-tabs v-model="settingsTab" class="settings-tabs">
            <el-tab-pane label="租户管理" name="tenants">
              <el-table 
                :data="paginatedTenants" 
                style="width: 100%" 
                size="small"
                :max-height="600"
              >
                <el-table-column prop="name" label="租户名称" min-width="150" />
                <el-table-column prop="quota" label="资源配额" min-width="120" />
                <el-table-column prop="used" label="已使用" min-width="120" />
                <el-table-column label="操作" width="180" fixed="right" align="left" header-align="left">
                  <template #default="{ row }">
                    <div style="white-space: nowrap; padding: 0 12px 0 0">
                      <el-button type="primary" size="small" text @click="editTenant(row)">
                        编辑
                      </el-button>
                      <el-button type="danger" size="small" text @click="deleteTenant(row)" style="margin-left: 8px">
                        删除
                      </el-button>
                    </div>
                  </template>
                </el-table-column>
              </el-table>
            </el-tab-pane>
          </el-tabs>
        </div>
        <div class="card-pagination">
          <el-pagination
            v-model:current-page="tenantPage"
            v-model:page-size="tenantPageSize"
            :page-sizes="[10, 20, 30, 50]"
            layout="total, sizes, prev, pager, next, jumper"
            :total="tenants.length"
          />
        </div>
      </div>
    </div>

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
import { ref, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { 
  View, 
  Monitor, 
  Delete, 
  Download, 
  VideoPlay, 
  VideoPause, 
  RefreshRight, 
  Document, 
  Setting, 
  Plus,
  Search
} from '@element-plus/icons-vue'
import { 
  getServers as apiGetServers,
  getComponents as apiGetComponents,
  startComponent as apiStartComponent,
  stopComponent as apiStopComponent,
  getComponentLogs as apiGetComponentLogs
} from '@/api/ops'

// 分页控制
const serverPage = ref(1)
const serverPageSize = ref(10)
const componentPage = ref(1)
const componentPageSize = ref(10)
const alertPage = ref(1)
const alertPageSize = ref(10)
const tenantPage = ref(1)
const tenantPageSize = ref(10)

// 对话框控制
const showAddServerDialog = ref(false)
const showInstallDialog = ref(false)
const showAddAlertDialog = ref(false)
const selectedComponent = ref('')
const settingsTab = ref('tenants')

// 组件管理相关状态
const componentSearchText = ref('')
const componentStatusFilter = ref('')
const selectedComponents = ref<any[]>([])

// 搜索表单
const serverSearchForm = ref({
  ip: '',
  name: '',
  vendor: ''
})

// 服务器表单
const serverForm = ref({
  name: '',
  ip: '',
  type: 'Docker',
  port: '22',
  username: '',
  password: ''
})

// 服务器数据
const servers = ref([
  {
    id: 1,
    name: '腾讯云服务器-1',
    ip: '82.156.36.157',
    vendor: '腾讯云',
    type: 'Docker',
    cpu: '8',
    memory: '8GB/16GB',
    disk: '100GB/500GB',
    memUsage: 50,
    diskUsage: 20,
    status: '在线',
    rack: 'default-rack',
    cpuArch: 'x86_64',
    updateTime: '2025-11-13 17:36:22'
  },
  {
    id: 2,
    name: '腾讯云服务器-2',
    ip: '58.87.103.99',
    vendor: '腾讯云',
    type: 'Docker',
    cpu: '8',
    memory: '10GB/16GB',
    disk: '200GB/500GB',
    memUsage: 63,
    diskUsage: 40,
    status: '在线',
    rack: 'default-rack',
    cpuArch: 'x86_64',
    updateTime: '2025-11-13 17:36:22'
  },
  {
    id: 3,
    name: '腾讯云服务器-3',
    ip: '192.144.173.136',
    vendor: '腾讯云',
    type: 'Docker',
    cpu: '4',
    memory: '8GB/8GB',
    disk: '120GB/120GB',
    memUsage: 50,
    diskUsage: 50,
    status: '在线',
    rack: 'default-rack',
    cpuArch: 'x86_64',
    updateTime: '2025-11-13 17:36:22'
  }
])

// 已安装组件(包含所有可用组件)
const components = ref([
  {
    id: 1,
    name: 'Flink',
    logo: 'https://flink.apache.org/img/logo/png/1000/flink_squirrel_1000.png',
    description: '实时计算引擎',
    version: '1.15.4',
    server: '腾讯云服务器-1',
    installed: true,
    status: '运行中',
    port: '8081',
    path: '/opt/flink',
    updateTime: '2025-11-13 16:30:00'
  },
  {
    id: 2,
    name: 'Spark',
    logo: 'https://spark.apache.org/images/spark-logo-trademark.png',
    description: '快速、高效的大数据处理引擎',
    version: '3.2.3',
    installed: false,
    server: '-',
    status: '-',
    port: '-',
    path: '-',
    updateTime: '-'
  },
  {
    id: 3,
    name: 'SeaTunnel',
    logo: 'https://avatars.githubusercontent.com/u/79972009',
    description: '海量数据集成引擎',
    version: '2.3.7',
    installed: false,
    server: '-',
    status: '-',
    port: '-',
    path: '-',
    updateTime: '-'
  },
  {
    id: 4,
    name: 'ZooKeeper',
    logo: 'https://zookeeper.apache.org/images/zookeeper_small.gif',
    description: '分布式协调系统',
    version: '3.7.1',
    server: '腾讯云服务器-1',
    installed: true,
    status: '运行中',
    port: '2181',
    path: '/opt/zookeeper',
    updateTime: '2025-11-13 16:25:00'
  },
  {
    id: 5,
    name: 'Kafka',
    logo: 'https://kafka.apache.org/images/logo.png',
    description: 'Kafka分布式消息系统',
    version: '2.8.2',
    server: '腾讯云服务器-2',
    installed: true,
    status: '已停止',
    port: '9092',
    path: '/opt/kafka',
    updateTime: '2025-11-13 15:40:00'
  },
  {
    id: 6,
    name: 'Hadoop',
    logo: 'https://hadoop.apache.org/images/hadoop-logo.jpg',
    description: '分布式大数据存储',
    version: '3.3.4',
    installed: false,
    server: '-',
    status: '-',
    port: '-',
    path: '-',
    updateTime: '-'
  },
  {
    id: 7,
    name: 'Hive',
    logo: 'https://hive.apache.org/images/hive_logo_medium.jpg',
    description: 'Hadoop离线数据仓库',
    version: '3.1.3',
    installed: false,
    server: '-',
    status: '-',
    port: '-',
    path: '-',
    updateTime: '-'
  },
  {
    id: 8,
    name: 'Kyuubi',
    logo: 'https://kyuubi.apache.org/assets/images/kyuubi_logo.png',
    description: '分布式和多租户SQL网关',
    version: '1.7.0',
    installed: false,
    server: '-',
    status: '-',
    port: '-',
    path: '-',
    updateTime: '-'
  },
  {
    id: 9,
    name: 'DolphinScheduler',
    logo: 'https://dolphinscheduler.apache.org/img/logo.svg',
    description: '可视化任务调度平台',
    version: '3.2.2',
    installed: false,
    server: '-',
    status: '-',
    port: '-',
    path: '-',
    updateTime: '-'
  },
  {
    id: 10,
    name: 'MinIO',
    logo: 'https://min.io/resources/img/logo.svg',
    description: '支持主流的云原生对象存储系统',
    version: '2021-04-22T15-44-28Z',
    installed: false,
    server: '-',
    status: '-',
    port: '-',
    path: '-',
    updateTime: '-'
  },
  {
    id: 11,
    name: 'Filebeat',
    logo: 'https://www.elastic.co/static-res/images/elastic-logo-200.png',
    description: '日志采集工具',
    version: '7.16.3',
    installed: false,
    server: '-',
    status: '-',
    port: '-',
    path: '-',
    updateTime: '-'
  },
  {
    id: 12,
    name: 'Trino',
    logo: 'https://trino.io/assets/logo.png',
    description: 'Trino是一个分布式SQL查询引擎',
    version: '424',
    installed: false,
    server: '-',
    status: '-',
    port: '-',
    path: '-',
    updateTime: '-'
  },
  {
    id: 13,
    name: 'Amoro',
    logo: 'https://amoro.apache.org/img/logo.svg',
    description: '数据湖管理平台',
    version: '0.6.1',
    installed: false,
    server: '-',
    status: '-',
    port: '-',
    path: '-',
    updateTime: '-'
  },
  {
    id: 14,
    name: 'Dinky',
    logo: 'https://www.dinky.org.cn/img/dinky-logo.svg',
    description: '实时计算平台',
    version: '1.1.0',
    installed: false,
    server: '-',
    status: '-',
    port: '-',
    path: '-',
    updateTime: '-'
  }
])

// 模拟告警数据
const alerts = ref([
  { id: 1, name: 'CPU使用率过高', level: '严重', source: 'Docker-Server-1', message: 'CPU使用率达到95%', time: '2025-01-13 14:30:00' },
  { id: 2, name: '内存不足', level: '警告', source: 'Docker-Server-2', message: '可用内存低于10%', time: '2025-01-13 14:25:00' },
  { id: 3, name: '磁盘空间不足', level: '提示', source: 'Docker-Server-3', message: '磁盘使用率达到80%', time: '2025-01-13 14:20:00' }
])

// 模拟租户数据
const tenants = ref([
  { id: 1, name: '数据团队', quota: '100GB', used: '75GB' },
  { id: 2, name: '分析团队', quota: '200GB', used: '120GB' }
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

// 页面加载时获取数据
onMounted(() => {
  loadData()
})

const loadData = async () => {
  try {
    // 获取服务器列表
    const serversRes = await apiGetServers()
    if (serversRes.data) {
      servers.value = serversRes.data
    }
    
    // 获取组件列表
    const componentsRes = await apiGetComponents()
    if (componentsRes.data) {
      components.value = componentsRes.data
    }
  } catch (error) {
    console.error('加载数据失败:', error)
    ElMessage.error('加载数据失败,请刷新页面重试')
  }
}

// 计算分页数据
const paginatedServers = computed(() => {
  const start = (serverPage.value - 1) * serverPageSize.value
  return servers.value.slice(start, start + serverPageSize.value)
})

const paginatedComponents = computed(() => {
  // 先过滤
  let filtered = components.value
  
  // 按名称搜索
  if (componentSearchText.value) {
    filtered = filtered.filter(c => 
      c.name.toLowerCase().includes(componentSearchText.value.toLowerCase())
    )
  }
  
  // 按状态筛选
  if (componentStatusFilter.value) {
    if (componentStatusFilter.value === 'installed') {
      filtered = filtered.filter(c => c.installed)
    } else if (componentStatusFilter.value === 'uninstalled') {
      filtered = filtered.filter(c => !c.installed)
    } else if (componentStatusFilter.value === 'running') {
      filtered = filtered.filter(c => c.installed && c.status === '运行中')
    } else if (componentStatusFilter.value === 'stopped') {
      filtered = filtered.filter(c => c.installed && c.status === '已停止')
    }
  }
  
  // 分页
  const start = (componentPage.value - 1) * componentPageSize.value
  return filtered.slice(start, start + componentPageSize.value)
})

const paginatedAlerts = computed(() => {
  const start = (alertPage.value - 1) * alertPageSize.value
  return alerts.value.slice(start, start + alertPageSize.value)
})

const paginatedTenants = computed(() => {
  const start = (tenantPage.value - 1) * tenantPageSize.value
  return tenants.value.slice(start, start + tenantPageSize.value)
})

// 刷新所有数据
const refreshAllData = async () => {
  await loadData()
  ElMessage.success('数据已刷新')
}

// 进度条颜色
const getProgressColor = (percentage: number) => {
  if (percentage < 50) return '#00b96b'
  if (percentage < 80) return '#faad14'
  return '#f5222d'
}

// 搜索服务器
const searchServers = () => {
  ElMessage.info('搜索功能待实现')
}

// 重置搜索
const resetServerSearch = () => {
  serverSearchForm.value = {
    ip: '',
    name: '',
    vendor: ''
  }
}

// 告警操作
const handleAlert = (row: any) => {
  ElMessage.success(`处理告警: ${row.name}`)
}

const deleteAlert = (row: any) => {
  ElMessage.warning(`删除告警: ${row.name}`)
}

// 租户操作
const editTenant = (row: any) => {
  ElMessage.info(`编辑租户: ${row.name}`)
}

const deleteTenant = (row: any) => {
  ElMessage.warning(`删除租户: ${row.name}`)
}

// 服务器操作
const addServer = () => {
  ElMessage.success('添加主机成功')
  showAddServerDialog.value = false
}

const viewServer = (row: any) => {
  ElMessage.info(`查看服务器: ${row.name}`)
}

const monitorServer = (row: any) => {
  ElMessage.info(`监控服务器: ${row.name}`)
}

const deleteServer = (row: any) => {
  ElMessage.warning(`删除服务器: ${row.name}`)
}

// 组件操作
const installComponent = (row?: any) => {
  const componentName = row ? row.name : selectedComponent.value
  ElMessage.success(`安装组件: ${componentName}`)
  if (!row) {
    showInstallDialog.value = false
    selectedComponent.value = ''
  }
}

const startComponent = async (row: any) => {
  try {
    await apiStartComponent(row.id)
    ElMessage.success(`启动组件: ${row.name}`)
    await loadData() // 刷新数据
  } catch (error) {
    ElMessage.error(`启动失败: ${error}`)
  }
}

const stopComponent = async (row: any) => {
  try {
    await apiStopComponent(row.id)
    ElMessage.warning(`停止组件: ${row.name}`)
    await loadData() // 刷新数据
  } catch (error) {
    ElMessage.error(`停止失败: ${error}`)
  }
}

const viewLogs = async (row: any) => {
  try {
    const res = await apiGetComponentLogs(row.id)
    if (res.data) {
      ElMessage.info(`日志: ${res.data.substring(0, 100)}...`)
    }
  } catch (error) {
    ElMessage.error(`获取日志失败: ${error}`)
  }
}

const configComponent = (row: any) => {
  ElMessage.info(`配置组件: ${row.name}`)
}

const restartComponent = async (row: any) => {
  try {
    await apiStopComponent(row.id)
    await new Promise(resolve => setTimeout(resolve, 1000))
    await apiStartComponent(row.id)
    ElMessage.success(`重启组件: ${row.name}`)
    await loadData()
  } catch (error) {
    ElMessage.error(`重启失败: ${error}`)
  }
}

const uninstallComponent = (row: any) => {
  ElMessage.warning(`卸载组件: ${row.name}`)
}

// 批量操作
const handleComponentSelectionChange = (selection: any[]) => {
  selectedComponents.value = selection
}

const batchStartComponents = async () => {
  if (selectedComponents.value.length === 0) {
    ElMessage.warning('请先选择组件')
    return
  }
  
  const stoppedComponents = selectedComponents.value.filter(
    c => c.installed && c.status === '已停止'
  )
  
  if (stoppedComponents.length === 0) {
    ElMessage.warning('没有可以启动的组件（只有已停止的组件可以启动）')
    return
  }
  
  try {
    for (const component of stoppedComponents) {
      await apiStartComponent(component.id)
    }
    ElMessage.success(`批量启动成功: ${stoppedComponents.length}个组件`)
    await loadData()
  } catch (error) {
    ElMessage.error(`批量启动失败: ${error}`)
  }
}

const batchStopComponents = async () => {
  if (selectedComponents.value.length === 0) {
    ElMessage.warning('请先选择组件')
    return
  }
  
  const runningComponents = selectedComponents.value.filter(
    c => c.installed && c.status === '运行中'
  )
  
  if (runningComponents.length === 0) {
    ElMessage.warning('没有可以停止的组件（只有运行中的组件可以停止）')
    return
  }
  
  try {
    for (const component of runningComponents) {
      await apiStopComponent(component.id)
    }
    ElMessage.success(`批量停止成功: ${runningComponents.length}个组件`)
    await loadData()
  } catch (error) {
    ElMessage.error(`批量停止失败: ${error}`)
  }
}
</script>

<style scoped lang="scss">
.page-container-tdesign {
  padding: 0;
  background: #f0f2f5;
  min-height: 100vh;
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

.modules-container {
  display: flex;
  flex-direction: column;
  gap: 20px;
  padding: 0;
  margin: 0;
}

.module-card {
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 1px 4px 0 rgba(0, 0, 0, 0.08);
  overflow: hidden;
  margin: 0;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 20px;
  background: #fff;
  border-bottom: 1px solid #f2f3f5;

  .header-left {
    display: flex;
    align-items: center;
    gap: 10px;

    h3 {
      margin: 0;
      font-size: 15px;
      font-weight: 600;
      color: #1d2129;
    }
  }

  .header-right {
    display: flex;
    gap: 8px;
  }
}

.card-body {
  padding: 0;
  background: #fff;
  
  :deep(.el-table) {
    // Arco Design 风格优化
    border: none;
    font-size: 14px;
    background-color: transparent;
    
    // 关键修复: 表格必须有明确的宽度才能启用固定列
    width: 100% !important;
    
    // 表头跟随body一起横向滚动
    .el-table__header-wrapper {
      overflow-x: hidden;
    }
    
    // 表体横向滚动 - 关键！
    .el-table__body-wrapper {
      overflow-x: auto !important;
    }
    
    th {
      background: #fafafa;
      border-bottom: 1px solid #e5e6eb;
      font-weight: 500;
      font-size: 14px;
      color: #1d2129;
      padding: 14px 16px !important;
      white-space: nowrap !important;
    }
    
    // 表体样式
    td {
      border-bottom: 1px solid #f2f3f5;
      padding: 14px 16px !important;
      font-size: 14px;
      color: #4e5969;
      white-space: nowrap !important;
    }
    
    // 确保所有单元格内容不换行
    .el-table__cell {
      white-space: nowrap !important;
    }
    
    // 单元格内部元素
    .cell {
      white-space: nowrap !important;
      text-overflow: clip !important;
    }
    
    // 悬停效果
    .el-table__body tr:hover > td {
      background: #f7f8fa !important;
    }
    
    // 移除垂直边框
    &::before {
      display: none;
    }
    
    th.el-table__cell,
    td.el-table__cell {
      border-right: none;
    }
    
    // 复选框样式优化
    .el-checkbox__inner {
      border-radius: 2px;
      width: 16px;
      height: 16px;
    }
    
    // 固定列样式优化 - 使用Element Plus默认的absolute定位
    .el-table__fixed,
    .el-table__fixed-right {
      z-index: 3 !important;
      box-shadow: -2px 0 8px rgba(0, 0, 0, 0.08);
      
      &::before {
        background-color: #fff;
      }
    }
    
    .el-table__fixed-left {
      z-index: 3 !important;
      box-shadow: 2px 0 8px rgba(0, 0, 0, 0.08);
      
      &::before {
        background-color: #fff;
      }
    }
    
    // 固定列右侧阴影
    .el-table__fixed-right::before {
      content: '';
      position: absolute;
      left: -10px;
      top: 0;
      bottom: 0;
      width: 10px;
      background: linear-gradient(to right, rgba(0, 0, 0, 0), rgba(0, 0, 0, 0.04));
      pointer-events: none;
    }
    
    // 确保固定列单元格背景色
    .el-table__fixed-right .el-table__cell {
      background-color: #fff;
    }
    
    .el-table__fixed-left .el-table__cell {
      background-color: #fff;
    }
    
    // 悬停时固定列背景
    .el-table__fixed-right .el-table__row:hover .el-table__cell {
      background-color: #f7f8fa !important;
    }
    
    .el-table__fixed-left .el-table__row:hover .el-table__cell {
      background-color: #f7f8fa !important;
    }
    
    // 操作列表头左对齐，去除左侧padding
    .el-table__fixed-right th.is-leaf:last-child .cell {
      padding-left: 0 !important;
      padding-right: 12px !important;
    }
    
    // 操作列内容左对齐
    .el-table__fixed-right td.is-left .cell {
      padding-left: 0 !important;
    }
  }
}

.search-bar {
  display: flex;
  align-items: center;
  justify-content: flex-end;
  gap: 12px;
  margin-bottom: 0;
  padding: 16px 20px;
  background: #fff;
  border-bottom: 1px solid #f2f3f5;
}

// 状态徽章样式
.status-badge {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  font-size: 14px;
  
  .status-dot {
    width: 6px;
    height: 6px;
    border-radius: 50%;
  }
  
  &.status-online {
    color: #00b42a;
    
    .status-dot {
      background: #00b42a;
    }
  }
  
  &.status-offline {
    color: #f53f3f;
    
    .status-dot {
      background: #f53f3f;
    }
  }
}

.card-pagination {
  padding: 16px 20px;
  display: flex;
  justify-content: flex-end;
  border-top: 1px solid #e5e6eb;
  background: #fafafa;
}

// 操作按钮样式优化
:deep(.el-button) {
  .el-icon {
    margin-right: 4px;
    font-size: 14px;
  }
}

.settings-tabs {
  :deep(.el-tabs__nav-wrap) {
    padding-left: 20px;
  }
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
