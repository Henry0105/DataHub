# 前端API对接指南

## ✅ 已完成的工作

### 1. API接口文件创建

已在 `src/api/` 目录下创建了所有API接口文件：

- ✅ `auth.ts` - 用户认证API
- ✅ `datasource.ts` - 数据源管理API
- ✅ `datacap.ts` - 数据中枢API
- ✅ `sync.ts` - 数据库同步API
- ✅ `dashboard.ts` - 首页看板API
- ✅ `index.ts` - 统一导出

### 2. 已对接的页面

- ✅ **登录页面** (`src/views/login/index.vue`)
  - 已对接 `authApi.login()`
  - 用户信息保存到 store

- ✅ **用户 Store** (`src/store/user.ts`)
  - 已对接 `authApi.login()`
  - 已对接 `authApi.logout()`
  - 已对接 `authApi.getUserInfo()`

- ✅ **首页看板** (`src/views/dashboard/index.vue`)
  - 已对接 `dashboardApi.getStats()`
  - 实时显示统计数据

- ✅ **数据源管理** (`src/views/datasource/index-api.vue`)
  - 已创建完整的API对接版本
  - 需要将内容复制到 `index.vue`

## 📋 待对接的页面

### 1. 数据中枢 (`src/views/datacap/index.vue`)

需要对接的API：

```typescript
import { datacapApi, datasourceApi } from '@/api'

// 获取数据源列表
const datasources = ref([])
const loadDatasources = async () => {
  datasources.value = await datasourceApi.list()
}

// 执行SQL查询
const handleExecuteQuery = async () => {
  const result = await datacapApi.executeQuery({
    datasourceId: selectedDatasource.value,
    sqlContent: sqlQuery.value
  })
  queryResult.value = result
}

// 查询历史
const loadQueryHistory = async () => {
  const res = await datacapApi.queryHistoryPage({
    current: currentPage.value,
    size: pageSize.value,
    keyword: searchKeyword.value
  })
  queries.value = res.records
  total.value = res.total
}

// 数据集管理
const loadDatasets = async () => {
  const res = await datacapApi.datasetPage({
    current: currentPage.value,
    size: pageSize.value,
    keyword: searchKeyword.value
  })
  datasets.value = res.records
  total.value = res.total
}

// 创建数据集
const handleCreateDataset = async () => {
  await datacapApi.createDataset(formData)
  ElMessage.success('创建成功')
  loadDatasets()
}

// 刷新数据集
const handleRefreshDataset = async (id: number) => {
  await datacapApi.refreshDataset(id)
  ElMessage.success('刷新成功')
  loadDatasets()
}
```

### 2. 数据库同步 (`src/views/dbswitch/index.vue`)

需要对接的API：

```typescript
import { syncApi, datasourceApi } from '@/api'

// 获取数据源列表
const datasources = ref([])
const loadDatasources = async () => {
  datasources.value = await datasourceApi.list()
}

// 加载同步任务列表
const loadTasks = async () => {
  const res = await syncApi.page({
    current: currentPage.value,
    size: pageSize.value,
    keyword: searchKeyword.value
  })
  tasks.value = res.records
  total.value = res.total
}

// 创建同步任务
const handleCreate = async () => {
  await syncApi.create(formData)
  ElMessage.success('创建成功')
  loadTasks()
}

// 执行同步任务
const handleExecute = async (id: number) => {
  await syncApi.execute(id)
  ElMessage.success('任务已开始执行')
  // 定时刷新任务状态
  setTimeout(() => loadTasks(), 1000)
}

// 停止同步任务
const handleStop = async (id: number) => {
  await syncApi.stop(id)
  ElMessage.success('任务已停止')
  loadTasks()
}

// 删除同步任务
const handleDelete = async (id: number) => {
  await ElMessageBox.confirm('确定要删除该任务吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning',
  })
  await syncApi.delete(id)
  ElMessage.success('删除成功')
  loadTasks()
}
```

## 🔧 通用对接模式

### 1. 导入API

```typescript
import { datasourceApi, datacapApi, syncApi, dashboardApi } from '@/api'
import type { Datasource, QueryResult, SyncTask } from '@/api'
```

### 2. 分页查询模式

```typescript
const loading = ref(false)
const tableData = ref([])
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const searchKeyword = ref('')

const loadData = async () => {
  loading.value = true
  try {
    const res = await xxxApi.page({
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

// 分页事件
const handleSizeChange = () => {
  loadData()
}

const handleCurrentChange = () => {
  loadData()
}

// 页面加载
onMounted(() => {
  loadData()
})
```

### 3. 表单提交模式

```typescript
const dialogVisible = ref(false)
const formRef = ref<FormInstance>()
const submitting = ref(false)
const formData = reactive({
  // 表单字段
})

const handleSubmit = async () => {
  if (!formRef.value) return

  await formRef.value.validate(async (valid) => {
    if (valid) {
      submitting.value = true
      try {
        if (formData.id) {
          await xxxApi.update(formData.id, formData)
          ElMessage.success('更新成功')
        } else {
          await xxxApi.create(formData)
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
```

### 4. 删除确认模式

```typescript
const handleDelete = async (row: any) => {
  if (!row.id) return

  try {
    await ElMessageBox.confirm('确定要删除吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
    })

    await xxxApi.delete(row.id)
    ElMessage.success('删除成功')
    loadData()
  } catch (error) {
    if (error !== 'cancel') {
      // 错误已在request拦截器中处理
    }
  }
}
```

## 🚀 快速对接步骤

### 步骤1：修改数据源管理页面

```bash
# 将 index-api.vue 的内容复制到 index.vue
cp src/views/datasource/index-api.vue src/views/datasource/index.vue
```

### 步骤2：修改数据中枢页面

在 `src/views/datacap/index.vue` 中：

1. 导入API：
```typescript
import { datacapApi, datasourceApi } from '@/api'
```

2. 替换测试数据为API调用：
```typescript
// 删除所有测试数据定义
// const datasources = ref([...])
// const queries = ref([...])
// const datasets = ref([...])

// 改为从API加载
const datasources = ref([])
const loadDatasources = async () => {
  datasources.value = await datasourceApi.list()
}

onMounted(() => {
  loadDatasources()
})
```

3. 替换所有操作函数为API调用（参考上面的示例）

### 步骤3：修改数据库同步页面

在 `src/views/dbswitch/index.vue` 中：

1. 导入API：
```typescript
import { syncApi, datasourceApi } from '@/api'
```

2. 替换测试数据为API调用（参考上面的示例）

## 📝 注意事项

### 1. 响应数据结构

后端返回的数据结构：

```typescript
{
  code: 200,
  message: "操作成功",
  data: {
    // 实际数据
  },
  timestamp: 1234567890
}
```

`request.ts` 已经处理了响应拦截，直接返回 `data` 部分，所以使用时：

```typescript
const data = await xxxApi.xxx()  // 直接得到 data 部分
```

### 2. 错误处理

错误已在 `request.ts` 的响应拦截器中统一处理，会自动弹出错误提示，所以业务代码中可以简化：

```typescript
try {
  await xxxApi.xxx()
  ElMessage.success('操作成功')
} catch (error) {
  // 错误已经被拦截器处理，这里可以不用再处理
  // 如果需要特殊处理，可以在这里添加
}
```

### 3. Loading状态

建议在所有API调用时添加loading状态：

```typescript
const loading = ref(false)

const loadData = async () => {
  loading.value = true
  try {
    const res = await xxxApi.xxx()
    // 处理数据
  } finally {
    loading.value = false
  }
}
```

### 4. 环境变量

API基础地址配置在 `.env` 文件中：

```bash
# 开发环境
VITE_API_BASE_URL=http://localhost:8080/api

# 生产环境
VITE_API_BASE_URL=http://82.156.36.157:8080/api
```

## 🧪 测试步骤

### 1. 启动后端服务

```bash
cd datahub-backend
mvn spring-boot:run
```

### 2. 启动前端服务

```bash
cd datahub-frontend
npm run dev
```

### 3. 测试功能

1. **登录测试**
   - 访问 http://localhost:5173
   - 使用 admin/admin123 登录
   - 检查是否成功跳转到首页

2. **首页看板测试**
   - 查看统计数据是否正确显示
   - 数据应该来自后端API

3. **数据源管理测试**
   - 查看数据源列表
   - 创建新数据源
   - 测试连接
   - 编辑数据源
   - 删除数据源

4. **数据中枢测试**
   - 选择数据源
   - 执行SQL查询
   - 查看查询历史
   - 管理数据集

5. **数据库同步测试**
   - 创建同步任务
   - 执行同步任务
   - 查看同步进度
   - 停止同步任务

## 📚 相关文档

- [后端开发完成说明.md](后端开发完成说明.md) - 后端API文档
- [datahub-backend/README.md](datahub-backend/README.md) - 后端完整文档
- [datahub-backend/QUICKSTART.md](datahub-backend/QUICKSTART.md) - 后端快速开始

## 💡 提示

1. 先完成数据源管理页面的对接，因为其他模块都依赖数据源
2. 对接时可以先注释掉测试数据，逐步替换为API调用
3. 使用浏览器开发者工具的Network面板查看API请求和响应
4. 如果遇到跨域问题，检查后端的CORS配置
5. 如果遇到401错误，检查Token是否正确传递

## 🎯 下一步

完成前端API对接后：

1. 本地联调测试所有功能
2. 修复发现的问题
3. 打包前后端
4. 部署到服务器
5. 生产环境测试

