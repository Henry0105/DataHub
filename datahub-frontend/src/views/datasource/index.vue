<template>
  <div class="page-container-tdesign">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="page-title">
        <el-icon :size="20" color="#0052d9"><Database /></el-icon>
        <span>数据源平台</span>
        <span class="page-subtitle">DataHub</span>
      </div>
      <el-button type="primary" @click="handleAdd">
        <el-icon><Plus /></el-icon>
        新增数据源
      </el-button>
    </div>

    <!-- 功能说明 -->
    <div class="info-banner">
      <div class="info-item">
        <el-icon color="#0052d9"><Check /></el-icon>
        <span>AI驱动的数据库管理工具</span>
      </div>
      <div class="info-item">
        <el-icon color="#0052d9"><Check /></el-icon>
        <span>支持MySQL、Oracle、PostgreSQL等多种数据库</span>
      </div>
      <div class="info-item">
        <el-icon color="#0052d9"><Check /></el-icon>
        <span>智能SQL生成与优化</span>
      </div>
      <div class="info-item">
        <el-icon color="#0052d9"><Check /></el-icon>
        <span>数据可视化与分析</span>
      </div>
    </div>

    <!-- 搜索和筛选 -->
    <div class="search-bar">
      <el-input
        v-model="searchText"
        placeholder="搜索数据源名称或主机地址"
        clearable
        style="width: 300px"
        @input="handleSearch"
      >
        <template #prefix>
          <el-icon><Search /></el-icon>
        </template>
      </el-input>
      <el-select v-model="filterType" placeholder="数据库类型" clearable style="width: 150px; margin-left: 12px" @change="handleSearch">
        <el-option label="全部" value="" />
        <el-option label="MySQL" value="MySQL" />
        <el-option label="Oracle" value="Oracle" />
        <el-option label="PostgreSQL" value="PostgreSQL" />
        <el-option label="达梦" value="DM" />
        <el-option label="人大金仓" value="KingBase" />
        <el-option label="OceanBase" value="OceanBase" />
      </el-select>
      <el-select v-model="filterStatus" placeholder="状态" clearable style="width: 120px; margin-left: 12px" @change="handleSearch">
        <el-option label="全部" value="" />
        <el-option label="正常" value="正常" />
        <el-option label="异常" value="异常" />
      </el-select>
    </div>

    <!-- 数据表格 -->
    <div class="table-card">
      <el-table 
        :data="filteredData" 
        style="width: 100%" 
        v-loading="loading"
        :default-sort="{ prop: 'updateTime', order: 'descending' }"
        lazy
      >
        <el-table-column prop="name" label="数据源名称" min-width="180">
          <template #default="{ row }">
            <div class="name-cell">
              <DatabaseIcon :type="row.type" :size="20" />
              <span>{{ row.name }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="type" label="数据库类型" width="140">
          <template #default="{ row }">
            <el-tag size="small" :type="getDbTagType(row.type)">{{ row.type }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="host" label="主机地址" min-width="200" />
        <el-table-column prop="port" label="端口" width="100" />
        <el-table-column prop="database" label="数据库名" min-width="150" />
        <el-table-column prop="updateTime" label="更新时间" width="180" sortable>
          <template #default="{ row }">
            <span style="white-space: nowrap;">{{ row.updateTime }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <span class="status-badge" :class="row.status === '正常' ? 'status-success' : 'status-error'">
              <span class="status-dot"></span>
              {{ row.status }}
            </span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="240" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" size="small" link @click="handleTest(row)">
              <el-icon><Connection /></el-icon>
              测试连接
            </el-button>
            <el-button type="primary" size="small" link @click="handleEdit(row)">
              <el-icon><Edit /></el-icon>
              编辑
            </el-button>
            <el-button type="danger" size="small" link @click="handleDelete(row)">
              <el-icon><Delete /></el-icon>
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination-container">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :page-sizes="[10, 20, 50, 100]"
          :total="total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </div>

    <!-- 新增/编辑对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="600px"
      :close-on-click-modal="false"
    >
      <el-form :model="formData" :rules="formRules" ref="formRef" label-width="100px">
        <el-form-item label="数据源名称" prop="name">
          <el-input v-model="formData.name" placeholder="请输入数据源名称" />
        </el-form-item>
        <el-form-item label="数据库类型" prop="type">
          <el-select v-model="formData.type" placeholder="请选择数据库类型" style="width: 100%" @change="handleTypeChange">
            <el-option-group label="关系型数据库 (RDBMS)">
              <el-option label="MySQL" value="MySQL">
                <div class="db-option"><DatabaseIcon type="MySQL" :size="16" /> <span>MySQL</span></div>
              </el-option>
              <el-option label="Oracle" value="Oracle">
                <div class="db-option"><DatabaseIcon type="Oracle" :size="16" /> <span>Oracle</span></div>
              </el-option>
              <el-option label="SQL Server" value="SQLServer">
                <div class="db-option"><DatabaseIcon type="SQLServer" :size="16" /> <span>SQL Server</span></div>
              </el-option>
              <el-option label="PostgreSQL" value="PostgreSQL">
                <div class="db-option"><DatabaseIcon type="PostgreSQL" :size="16" /> <span>PostgreSQL</span></div>
              </el-option>
              <el-option label="IBM DB2" value="DB2">
                <div class="db-option"><DatabaseIcon type="DB2" :size="16" /> <span>IBM DB2</span></div>
              </el-option>
              <el-option label="Sybase" value="Sybase">
                <div class="db-option"><DatabaseIcon type="Sybase" :size="16" /> <span>Sybase</span></div>
              </el-option>
              <el-option label="SQLite" value="SQLite">
                <div class="db-option"><DatabaseIcon type="SQLite" :size="16" /> <span>SQLite</span></div>
              </el-option>
              <el-option label="达梦数据库" value="DM">
                <div class="db-option"><DatabaseIcon type="DM" :size="16" /> <span>达梦数据库</span></div>
              </el-option>
              <el-option label="人大金仓" value="KingBase">
                <div class="db-option"><DatabaseIcon type="KingBase" :size="16" /> <span>人大金仓</span></div>
              </el-option>
            </el-option-group>
            <el-option-group label="分析型数据库 (OLAP)">
              <el-option label="Trino" value="Trino">
                <div class="db-option"><DatabaseIcon type="Trino" :size="16" /> <span>Trino</span></div>
              </el-option>
              <el-option label="Apache Hive" value="Hive">
                <div class="db-option"><DatabaseIcon type="Hive" :size="16" /> <span>Apache Hive</span></div>
              </el-option>
              <el-option label="ClickHouse" value="ClickHouse">
                <div class="db-option"><DatabaseIcon type="ClickHouse" :size="16" /> <span>ClickHouse</span></div>
              </el-option>
              <el-option label="StarRocks" value="StarRocks">
                <div class="db-option"><DatabaseIcon type="StarRocks" :size="16" /> <span>StarRocks</span></div>
              </el-option>
              <el-option label="Apache Doris" value="Doris">
                <div class="db-option"><DatabaseIcon type="Doris" :size="16" /> <span>Apache Doris</span></div>
              </el-option>
              <el-option label="GreenPlum" value="GreenPlum">
                <div class="db-option"><DatabaseIcon type="GreenPlum" :size="16" /> <span>GreenPlum</span></div>
              </el-option>
            </el-option-group>
            <el-option-group label="NoSQL数据库">
              <el-option label="MongoDB" value="MongoDB">
                <div class="db-option"><DatabaseIcon type="MongoDB" :size="16" /> <span>MongoDB</span></div>
              </el-option>
              <el-option label="Redis" value="Redis">
                <div class="db-option"><DatabaseIcon type="Redis" :size="16" /> <span>Redis</span></div>
              </el-option>
            </el-option-group>
            <el-option-group label="搜索引擎">
              <el-option label="ElasticSearch" value="ElasticSearch">
                <div class="db-option"><DatabaseIcon type="ElasticSearch" :size="16" /> <span>ElasticSearch</span></div>
              </el-option>
            </el-option-group>
            <el-option-group label="时序数据库">
              <el-option label="TDEngine" value="TDEngine">
                <div class="db-option"><DatabaseIcon type="TDEngine" :size="16" /> <span>TDEngine</span></div>
              </el-option>
            </el-option-group>
          </el-select>
        </el-form-item>
        <el-form-item label="主机地址" prop="host">
          <el-input v-model="formData.host" placeholder="请输入主机地址" />
        </el-form-item>
        <el-form-item label="端口" prop="port">
          <el-input v-model.number="formData.port" placeholder="请输入端口号" type="number" />
        </el-form-item>
        <el-form-item label="数据库名" prop="database">
          <el-input v-model="formData.database" placeholder="请输入数据库名" />
        </el-form-item>
        <el-form-item label="用户名" prop="username">
          <el-input v-model="formData.username" placeholder="请输入用户名" />
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input v-model="formData.password" placeholder="请输入密码" type="password" show-password />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button @click="handleTestConnection" :loading="testLoading">
          <el-icon><Connection /></el-icon>
          {{ testLoading ? '测试中...' : '测试连接' }}
        </el-button>
        <el-button type="primary" @click="handleSubmit" :loading="submitLoading">
          {{ submitLoading ? '保存中...' : '保存' }}
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import { Connection } from '@element-plus/icons-vue'
import { useDebounceFn } from '@vueuse/core'
import DatabaseIcon from '@/components/DatabaseIcon.vue'
import { getDatabaseConfig } from '@/config/database'
import { datasourceApi } from '@/api/datasource'

// 搜索和筛选
const searchText = ref('')
const filterType = ref('')
const filterStatus = ref('')
const loading = ref(false)

// 表格数据
const tableData = ref<any[]>([])

// 加载数据源列表
const loadDataSources = async () => {
  loading.value = true
  try {
    const result = await datasourceApi.page({ 
      current: currentPage.value, 
      size: pageSize.value, 
      keyword: searchText.value 
    })
    tableData.value = result.records.map((item: any) => ({
      id: item.id,
      name: item.name,
      type: item.type,
      host: item.host,
      port: item.port,
      database: item.databaseName,
      databaseName: item.databaseName,
      username: item.username,
      password: item.password,
      description: item.description,
      status: item.status === 1 ? '正常' : '异常',
      updateTime: item.updateTime,
      createTime: item.createTime
    }))
    total.value = result.total
    console.log('加载数据源列表成功，共', result.total, '条')
  } catch (error: any) {
    console.error('加载数据源列表失败:', error)
    ElMessage.error('加载数据源列表失败')
  } finally {
    loading.value = false
  }
}

// 组件挂载时加载数据
onMounted(() => {
  loadDataSources()
})

// 分页
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

// 显示的数据（不再需要前端过滤和分页）
const filteredData = computed(() => tableData.value)

// 对话框
const dialogVisible = ref(false)
const dialogTitle = ref('新增数据源')
const submitLoading = ref(false)
const testLoading = ref(false)
const formRef = ref<FormInstance>()

// 表单数据
const formData = reactive({
  id: null as number | null,
  name: '',
  type: '',
  host: '',
  port: 0,
  database: '',
  username: '',
  password: '',
})

// 表单验证规则
const formRules: FormRules = {
  name: [{ required: true, message: '请输入数据源名称', trigger: 'blur' }],
  type: [{ required: true, message: '请选择数据库类型', trigger: 'change' }],
  host: [{ required: true, message: '请输入主机地址', trigger: 'blur' }],
  port: [{ required: true, message: '请输入端口号', trigger: 'blur' }],
  database: [{ required: true, message: '请输入数据库名', trigger: 'blur' }],
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: false, message: '请输入密码', trigger: 'blur' }], // Trino等数据源可能无密码,
}

// 获取数据库标签类型
const getDbTagType = (type: string) => {
  const config = getDatabaseConfig(type)
  if (!config) return ''
  
  // 根据分类返回不同的标签类型
  const categoryTypeMap: Record<string, any> = {
    'RDBMS': 'primary',
    'OLAP': 'warning',
    'NoSQL': 'success',
    'Search': 'info',
    'TimeSeries': 'danger'
  }
  return categoryTypeMap[config.category] || ''
}

// 数据库类型改变时，自动填充默认端口
const handleTypeChange = (type: string) => {
  const config = getDatabaseConfig(type)
  if (config && config.defaultPort) {
    formData.port = config.defaultPort
  } else {
    formData.port = 0 // 默认值
  }
}

// 搜索处理（防抖300ms）
const handleSearch = useDebounceFn(() => {
  currentPage.value = 1
  loadDataSources()
}, 300)

// 分页处理
const handleSizeChange = (val: number) => {
  pageSize.value = val
  currentPage.value = 1
  loadDataSources()
}

const handleCurrentChange = (val: number) => {
  currentPage.value = val
  loadDataSources()
}

// 新增
const handleAdd = () => {
  dialogTitle.value = '新增数据源'
  Object.assign(formData, {
    id: null,
    name: '',
    type: '',
    host: '',
    port: 0,
    database: '',
    username: '',
    password: '',
  })
  dialogVisible.value = true
}

// 编辑
const handleEdit = (row: any) => {
  dialogTitle.value = '编辑数据源'
  Object.assign(formData, { 
    ...row,
    port: row.port || 0  // 确保端口不为null
  })
  dialogVisible.value = true
}

// 测试连接（列表中的数据源）
const handleTest = async (row: any) => {
  loading.value = true
  try {
    const result = await datasourceApi.testConnection(row.id)
    // 更新列表中的状态
    const index = tableData.value.findIndex(item => item.id === row.id)
    if (index > -1) {
      tableData.value[index].status = result.status === 1 ? '正常' : '异常'
      tableData.value[index].updateTime = result.updateTime
    }
    
    if (result.status === 1) {
      ElMessage.success(`数据源 ${row.name} 连接测试成功！`)
    } else {
      ElMessage.warning(`数据源 ${row.name} 连接测试失败，状态已更新为异常`)
    }
  } catch (error: any) {
    // 连接失败，更新状态为异常
    const index = tableData.value.findIndex(item => item.id === row.id)
    if (index > -1) {
      tableData.value[index].status = '异常'
    }
    // 不需要再次显示错误消息，request.ts的拦截器已经显示了
  } finally {
    loading.value = false
  }
}

// 测试连接（表单中）
const handleTestConnection = async () => {
  if (!formRef.value) return
  
  // 先验证表单
  await formRef.value.validate(async (valid) => {
    if (valid) {
      testLoading.value = true
      try {
        const testData = {
          name: formData.name,
          type: formData.type,
          host: formData.host,
          port: formData.port,
          databaseName: formData.database,
          username: formData.username,
          password: formData.password
        }
        await datasourceApi.testConnectionByConfig(testData)
        ElMessage.success('连接测试成功！')
      } catch (error: any) {
        // 不需要再次显示错误消息，request.ts的拦截器已经显示了
      } finally {
        testLoading.value = false
      }
    }
  })
}

// 删除
const handleDelete = (row: any) => {
  ElMessageBox.confirm(
    `确定要删除数据源 "${row.name}" 吗？删除后将无法恢复。`,
    '删除确认',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
    }
  ).then(async () => {
    try {
      await datasourceApi.delete(row.id)
      const index = tableData.value.findIndex(item => item.id === row.id)
      if (index > -1) {
        tableData.value.splice(index, 1)
      }
      ElMessage.success('删除成功')
    } catch (error: any) {
      // 不需要再次显示错误消息，request.ts的拦截器已经显示了
    }
  }).catch(() => {
    // 取消删除
  })
}

// 提交表单
const handleSubmit = async () => {
  if (!formRef.value) return
  
  await formRef.value.validate(async (valid) => {
    if (valid) {
      submitLoading.value = true
      try {
        const submitData = {
          name: formData.name,
          type: formData.type,
          host: formData.host,
          port: formData.port,
          databaseName: formData.database,
          username: formData.username,
          password: formData.password
        }

        if (formData.id) {
          // 编辑
          const result = await datasourceApi.update(formData.id, submitData)
          // 重新加载列表以获取最新数据
          await loadDataSources()
          ElMessage.success(result.status === 1 ? '数据源更新成功，连接正常！' : '数据源已保存，但连接异常，请检查配置')
        } else {
          // 新增
          const result = await datasourceApi.create(submitData)
          // 重新加载列表以获取最新数据
          await loadDataSources()
          ElMessage.success(result.status === 1 ? '数据源创建成功，连接正常！' : '数据源已保存，但连接异常，请检查配置')
        }
        
        dialogVisible.value = false
      } catch (error: any) {
        // 不需要再次显示错误消息，request.ts的拦截器已经显示了
      } finally {
        submitLoading.value = false
      }
    }
  })
}
</script>

<style scoped lang="scss">
// TDesign 风格的页面容器
.page-container-tdesign {
  padding: 0;
  background: transparent;
  min-height: 100%;
  
  .page-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 16px;
    
    .page-title {
      display: flex;
      align-items: center;
      gap: 8px;
      font-size: 20px;
      font-weight: 600;
      color: #000000e6;
      
      .page-subtitle {
        font-size: 14px;
        font-weight: 400;
        color: #00000073;
        margin-left: 4px;
      }
    }
  }
  
  .info-banner {
    background: #f3f9ff;
    border: 1px solid #d4e8ff;
    border-radius: 3px;
    padding: 16px 20px;
    margin-bottom: 16px;
    display: grid;
    grid-template-columns: repeat(2, 1fr);
    gap: 12px;
    
    .info-item {
      display: flex;
      align-items: center;
      gap: 8px;
      font-size: 14px;
      color: #000000d9;
    }
  }
  
  // 搜索栏
  .search-bar {
    display: flex;
    align-items: center;
    margin-bottom: 16px;
  }
  
  .table-card {
    background: #fff;
    border: 1px solid #dcdcdc;
    border-radius: 3px;
    padding: 16px;
    
    :deep(.el-table) {
      font-size: 14px;
      
      .el-table__header-wrapper {
        th {
          background: #f3f3f3;
          color: #000000d9;
          font-weight: 500;
          border-bottom: 1px solid #e7e7e7;
        }
      }
      
      .el-table__body-wrapper {
        td {
          border-bottom: 1px solid #f3f3f3;
        }
      }
    }
    
    .name-cell {
      display: flex;
      align-items: center;
      gap: 8px;
    }
    
    .status-badge {
      display: inline-flex;
      align-items: center;
      gap: 4px;
      padding: 2px 8px;
      border-radius: 3px;
      font-size: 12px;
      
      .status-dot {
        width: 6px;
        height: 6px;
        border-radius: 50%;
      }
      
      &.status-success {
        background: #e8f8f2;
        color: #00a870;
        
        .status-dot {
          background: #00a870;
        }
      }
      
      &.status-error {
        background: #fdecec;
        color: #d54941;
        
        .status-dot {
          background: #d54941;
        }
      }
    }
    
    .pagination-container {
      margin-top: 16px;
      display: flex;
      justify-content: flex-end;
    }
  }
}

// 对话框样式
:deep(.el-dialog) {
  border-radius: 3px;
  
  .el-dialog__header {
    border-bottom: 1px solid #e7e7e7;
    padding: 16px 24px;
    
    .el-dialog__title {
      font-size: 16px;
      font-weight: 600;
      color: #000000e6;
    }
  }
  
  .el-dialog__body {
    padding: 24px;
  }
  
  .el-dialog__footer {
    border-top: 1px solid #e7e7e7;
    padding: 12px 24px;
  }
}

// 数据库选项样式
.db-option {
  display: flex;
  align-items: center;
  gap: 8px;
  
  span {
    flex: 1;
  }
}
</style>
