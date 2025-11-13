<!--
  数据源管理页面 - 对接后端API版本
  使用方法：将此文件内容复制到 index.vue
-->
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
        <span>支持MySQL、Oracle、PostgreSQL等多种数据库</span>
      </div>
      <div class="info-item">
        <el-icon color="#0052d9"><Check /></el-icon>
        <span>统一管理企业数据源</span>
      </div>
      <div class="info-item">
        <el-icon color="#0052d9"><Check /></el-icon>
        <span>支持连接测试和状态监控</span>
      </div>
    </div>

    <!-- 搜索和筛选 -->
    <div class="search-bar">
      <el-input
        v-model="searchKeyword"
        placeholder="搜索数据源名称或主机地址"
        clearable
        style="width: 300px"
        @input="loadData"
      >
        <template #prefix>
          <el-icon><Search /></el-icon>
        </template>
      </el-input>
    </div>

    <!-- 数据表格 -->
    <div class="table-card">
      <el-table :data="tableData" style="width: 100%" v-loading="loading">
        <el-table-column prop="name" label="数据源名称" min-width="180">
          <template #default="{ row }">
            <div class="name-cell">
              <el-icon :size="16" :color="getDbColor(row.type)">
                <component :is="getDbIcon(row.type)" />
              </el-icon>
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
        <el-table-column prop="databaseName" label="数据库名" min-width="150" />
        <el-table-column prop="updateTime" label="更新时间" width="160" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <span class="status-badge" :class="row.status === 1 ? 'status-success' : 'status-error'">
              <span class="status-dot"></span>
              {{ row.status === 1 ? '正常' : '禁用' }}
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
          <el-select v-model="formData.type" placeholder="请选择数据库类型" style="width: 100%">
            <el-option label="MySQL" value="MySQL" />
            <el-option label="Oracle" value="Oracle" />
            <el-option label="PostgreSQL" value="PostgreSQL" />
            <el-option label="达梦数据库" value="DM" />
            <el-option label="人大金仓" value="KingBase" />
          </el-select>
        </el-form-item>
        <el-form-item label="主机地址" prop="host">
          <el-input v-model="formData.host" placeholder="请输入主机地址" />
        </el-form-item>
        <el-form-item label="端口" prop="port">
          <el-input-number v-model="formData.port" :min="1" :max="65535" style="width: 100%" />
        </el-form-item>
        <el-form-item label="数据库名" prop="databaseName">
          <el-input v-model="formData.databaseName" placeholder="请输入数据库名" />
        </el-form-item>
        <el-form-item label="用户名" prop="username">
          <el-input v-model="formData.username" placeholder="请输入用户名" />
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input v-model="formData.password" type="password" placeholder="请输入密码" show-password />
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="formData.description" type="textarea" :rows="3" placeholder="请输入描述" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="submitting">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox, FormInstance, FormRules } from 'element-plus'
import { datasourceApi, type Datasource } from '@/api'

// 表格数据
const loading = ref(false)
const tableData = ref<Datasource[]>([])
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const searchKeyword = ref('')

// 对话框
const dialogVisible = ref(false)
const dialogTitle = ref('新增数据源')
const formRef = ref<FormInstance>()
const submitting = ref(false)

// 表单数据
const formData = reactive<Datasource>({
  name: '',
  type: 'MySQL',
  host: '',
  port: 3306,
  databaseName: '',
  username: '',
  password: '',
  description: '',
})

// 表单验证规则
const formRules: FormRules = {
  name: [{ required: true, message: '请输入数据源名称', trigger: 'blur' }],
  type: [{ required: true, message: '请选择数据库类型', trigger: 'change' }],
  host: [{ required: true, message: '请输入主机地址', trigger: 'blur' }],
  port: [{ required: true, message: '请输入端口', trigger: 'blur' }],
  databaseName: [{ required: true, message: '请输入数据库名', trigger: 'blur' }],
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
}

// 加载数据
const loadData = async () => {
  loading.value = true
  try {
    const res = await datasourceApi.page({
      current: currentPage.value,
      size: pageSize.value,
      keyword: searchKeyword.value,
    })
    tableData.value = res.records
    total.value = res.total
  } catch (error) {
    ElMessage.error('加载数据失败')
  } finally {
    loading.value = false
  }
}

// 新增
const handleAdd = () => {
  dialogTitle.value = '新增数据源'
  Object.assign(formData, {
    id: undefined,
    name: '',
    type: 'MySQL',
    host: '',
    port: 3306,
    databaseName: '',
    username: '',
    password: '',
    description: '',
  })
  dialogVisible.value = true
}

// 编辑
const handleEdit = (row: Datasource) => {
  dialogTitle.value = '编辑数据源'
  Object.assign(formData, row)
  dialogVisible.value = true
}

// 提交
const handleSubmit = async () => {
  if (!formRef.value) return

  await formRef.value.validate(async (valid) => {
    if (valid) {
      submitting.value = true
      try {
        if (formData.id) {
          await datasourceApi.update(formData.id, formData)
          ElMessage.success('更新成功')
        } else {
          await datasourceApi.create(formData)
          ElMessage.success('创建成功')
        }
        dialogVisible.value = false
        loadData()
      } catch (error) {
        // 错误已在request拦截器中处理
      } finally {
        submitting.value = false
      }
    }
  })
}

// 测试连接
const handleTest = async (row: Datasource) => {
  if (!row.id) return
  
  const loading = ElMessage.loading('正在测试连接...')
  try {
    await datasourceApi.testConnection(row.id)
    ElMessage.success('连接测试成功')
  } catch (error) {
    // 错误已在request拦截器中处理
  } finally {
    loading.close()
  }
}

// 删除
const handleDelete = async (row: Datasource) => {
  if (!row.id) return

  try {
    await ElMessageBox.confirm('确定要删除该数据源吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
    })

    await datasourceApi.delete(row.id)
    ElMessage.success('删除成功')
    loadData()
  } catch (error) {
    if (error !== 'cancel') {
      // 错误已在request拦截器中处理
    }
  }
}

// 分页
const handleSizeChange = () => {
  loadData()
}

const handleCurrentChange = () => {
  loadData()
}

// 获取数据库图标
const getDbIcon = (type: string) => {
  const iconMap: Record<string, string> = {
    MySQL: 'Coin',
    Oracle: 'Orange',
    PostgreSQL: 'Document',
    DM: 'Files',
    KingBase: 'Folder',
  }
  return iconMap[type] || 'Database'
}

// 获取数据库颜色
const getDbColor = (type: string) => {
  const colorMap: Record<string, string> = {
    MySQL: '#0052d9',
    Oracle: '#e37318',
    PostgreSQL: '#029cd4',
    DM: '#00a870',
    KingBase: '#0594fa',
  }
  return colorMap[type] || '#666'
}

// 获取数据库标签类型
const getDbTagType = (type: string) => {
  const typeMap: Record<string, any> = {
    MySQL: 'primary',
    Oracle: 'warning',
    PostgreSQL: 'info',
    DM: 'success',
    KingBase: '',
  }
  return typeMap[type] || ''
}

// 页面加载
onMounted(() => {
  loadData()
})
</script>

<style scoped lang="scss">
// 保持原有样式...
.page-container-tdesign {
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
      color: #1f2937;
      
      .page-subtitle {
        font-size: 14px;
        font-weight: 400;
        color: #9ca3af;
      }
    }
  }
  
  .info-banner {
    display: flex;
    gap: 24px;
    padding: 16px;
    background: #f3f6ff;
    border: 1px solid #d4e3ff;
    border-radius: 3px;
    margin-bottom: 16px;
    
    .info-item {
      display: flex;
      align-items: center;
      gap: 6px;
      font-size: 13px;
      color: #4b5563;
    }
  }
  
  .search-bar {
    margin-bottom: 16px;
  }
  
  .table-card {
    background: #fff;
    border: 1px solid #dcdcdc;
    border-radius: 3px;
    padding: 16px;
    
    .name-cell {
      display: flex;
      align-items: center;
      gap: 8px;
    }
    
    .status-badge {
      display: inline-flex;
      align-items: center;
      gap: 4px;
      font-size: 13px;
      
      .status-dot {
        width: 6px;
        height: 6px;
        border-radius: 50%;
      }
      
      &.status-success {
        color: #00a870;
        
        .status-dot {
          background: #00a870;
        }
      }
      
      &.status-error {
        color: #e34d59;
        
        .status-dot {
          background: #e34d59;
        }
      }
    }
    
    .pagination-container {
      display: flex;
      justify-content: flex-end;
      margin-top: 16px;
    }
  }
}
</style>

