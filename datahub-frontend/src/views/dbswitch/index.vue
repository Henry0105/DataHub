<template>
  <div class="wedata-sync-page" v-loading="pageLoading" element-loading-text="加载中...">
    <!-- WeData 风格顶部工具栏 -->
    <div class="wedata-toolbar">
      <div class="toolbar-left">
        <div class="wedata-tabs">
          <div 
            v-for="(step, index) in steps" 
            :key="index"
            :class="['wedata-tab', { active: currentStep === index }]"
            @click="currentStep = index"
          >
            <span class="step-number">{{ index + 1 }}</span>
            <span class="step-name">{{ step.title }}</span>
          </div>
        </div>
      </div>
      <div class="toolbar-right">
        <el-button @click="handleReset">
          <el-icon><RefreshLeft /></el-icon>
          重置表单
        </el-button>
        <el-button type="primary" @click="handleSubmit" :loading="submitLoading">
          <el-icon><Check /></el-icon>
          保存任务
        </el-button>
        <el-button type="success" @click="handleRun">
          <el-icon><VideoPlay /></el-icon>
          保存并运行
        </el-button>
      </div>
    </div>

    <!-- WeData 风格主内容区：步骤切换显示 -->
    <div class="wedata-content">
      <!-- 步骤1：配置数据源 -->
      <div v-show="currentStep === 0" class="step-panel">
        <div class="step-panel-header">
          <div class="step-title">
            <el-icon :size="20" color="#0052d9"><Coin /></el-icon>
            配置数据源
          </div>
          <div class="step-desc">选择源数据库和目标数据库</div>
        </div>

        <!-- 任务基本信息 -->
        <div class="basic-info-section">
          <el-form :model="formData" :rules="formRules" ref="formRef" label-width="100px">
            <el-form-item label="任务名称" prop="name">
              <el-input v-model="formData.name" placeholder="请输入任务名称" clearable style="width: 400px" />
            </el-form-item>
          </el-form>
        </div>

        <!-- 左右分栏：源数据库 | 目标数据库 -->
        <div class="datasource-layout">
          <div class="datasource-column source-column">
            <div class="column-header">
              <div class="header-icon">
                <el-icon :size="24" color="#0052d9"><Coin /></el-icon>
              </div>
              <div class="header-content">
                <div class="header-title">源数据库</div>
                <div class="header-subtitle">Source Database</div>
              </div>
            </div>
            <div class="column-body">
              <el-form label-width="80px">
                <el-form-item label="类型">
                  <el-select v-model="formData.sourceType" placeholder="选择数据库类型" style="width: 100%">
                    <el-option label="MySQL" value="MySQL" />
                    <el-option label="PostgreSQL" value="PostgreSQL" />
                    <el-option label="Oracle" value="Oracle" />
                  </el-select>
                </el-form-item>
                <el-form-item label="数据源">
                  <el-select v-model="formData.source" placeholder="选择数据源" filterable style="width: 100%" @change="handleSourceChange">
                    <el-option v-for="ds in datasourceList" :key="ds.id" :label="`${ds.name} (${ds.type})`" :value="ds.id" />
                  </el-select>
                  <el-link type="primary" :underline="false" style="margin-top: 8px">新建数据源</el-link>
                </el-form-item>
                <el-form-item label="库">
                  <el-select v-model="formData.sourceDb" placeholder="选择库" style="width: 100%">
                    <el-option label="示例库" value="example_db" />
                  </el-select>
                </el-form-item>
                <el-form-item label="表">
                  <el-select v-model="formData.selectedTable" placeholder="选择表" filterable :loading="tableLoading" style="width: 100%" @change="handleTableSelect">
                    <el-option v-for="t in sourceTables" :key="t" :label="t" :value="t" />
                  </el-select>
                  <el-button size="small" :icon="Refresh" style="margin-top: 8px" @click="loadSourceTables">刷新</el-button>
                </el-form-item>
                <el-form-item label="切割键">
                  <el-input v-model="formData.splitKey" placeholder="用于大表并行" />
                </el-form-item>
                <el-form-item label="筛选条件">
                  <el-input v-model="formData.where" type="textarea" :rows="3" placeholder="请输入WHERE条件" />
                </el-form-item>
              </el-form>
            </div>
          </div>

          <div class="datasource-divider">
            <el-icon :size="24" color="#0052d9"><Right /></el-icon>
          </div>

          <div class="datasource-column target-column">
            <div class="column-header">
              <div class="header-icon">
                <el-icon :size="24" color="#00b96b"><Coin /></el-icon>
              </div>
              <div class="header-content">
                <div class="header-title">目标数据库</div>
                <div class="header-subtitle">Target Database</div>
              </div>
            </div>
            <div class="column-body">
              <el-form label-width="80px">
                <el-form-item label="类型">
                  <el-select v-model="formData.targetType" placeholder="选择数据库类型" style="width: 100%">
                    <el-option label="MySQL" value="MySQL" />
                    <el-option label="PostgreSQL" value="PostgreSQL" />
                    <el-option label="Oracle" value="Oracle" />
                    <el-option label="Trino" value="Trino" />
                  </el-select>
                </el-form-item>
                <el-form-item label="数据源">
                  <el-select v-model="formData.target" placeholder="选择数据源" filterable style="width: 100%" @change="handleTargetChange">
                    <el-option v-for="ds in datasourceList" :key="ds.id" :label="`${ds.name} (${ds.type})`" :value="ds.id" />
                  </el-select>
                  <el-link type="primary" :underline="false" style="margin-top: 8px">新建数据源</el-link>
                </el-form-item>
                <!-- Trino 特殊处理：增加 Catalog 选择 -->
                <el-form-item v-if="isTargetTrino" label="Catalog">
                  <el-select v-model="formData.targetCatalog" placeholder="选择Catalog" filterable :loading="targetCatalogLoading" style="width: 100%" @change="handleTargetCatalogChange">
                    <el-option v-for="catalog in targetCatalogs" :key="catalog" :label="catalog" :value="catalog" />
                  </el-select>
                  <el-button size="small" :icon="Refresh" style="margin-top: 8px" @click="loadTargetCatalogs">刷新</el-button>
                </el-form-item>
                <el-form-item :label="isTargetTrino ? 'Schema' : '库'">
                  <el-select v-model="formData.targetDb" :placeholder="isTargetTrino ? '选择Schema' : '选择库'" filterable :loading="targetDbLoading" style="width: 100%" @change="handleTargetDbChange">
                    <el-option v-for="db in targetDatabases" :key="db" :label="db" :value="db" />
                  </el-select>
                  <el-button size="small" :icon="Refresh" style="margin-top: 8px" @click="loadTargetDatabases">刷新</el-button>
                </el-form-item>
                <el-form-item label="表">
                  <el-select v-model="formData.targetTableName" placeholder="选择表" filterable :loading="targetTableLoading" style="width: 100%" @change="handleTargetTableSelect">
                    <el-option v-for="t in targetTables" :key="t" :label="t" :value="t" />
                  </el-select>
                  <el-button size="small" :icon="Refresh" style="margin-top: 8px" @click="loadTargetTables">刷新</el-button>
                  <el-button size="small" type="primary" style="margin-top: 8px; margin-left: 8px" @click="handleCreateTargetTable">一键建立目标表</el-button>
                </el-form-item>
                <el-form-item label="写入模式">
                  <el-radio-group v-model="formData.writeMode">
                    <el-radio label="append">追加</el-radio>
                    <el-radio label="overwrite">覆盖</el-radio>
                    <el-radio label="nonConflict">非冲突</el-radio>
                  </el-radio-group>
                </el-form-item>
                <el-form-item label="批量大小">
                  <el-input-number v-model="formData.batchSize" :min="100" :max="50000" :step="100" />
                </el-form-item>
                <el-form-item label="前置SQL">
                  <el-input v-model="formData.preSql" type="textarea" :rows="2" placeholder="同步前执行的SQL" />
                </el-form-item>
                <el-form-item label="后置SQL">
                  <el-input v-model="formData.postSql" type="textarea" :rows="2" placeholder="同步后执行的SQL" />
                </el-form-item>
              </el-form>
            </div>
          </div>
        </div>

        <!-- 字段映射区域 - 当源表和目标表都配置后显示 -->
        <div v-if="showFieldMapping" class="field-mapping-section">
          <div class="mapping-section-header">
            <div class="section-title">
              <el-icon :size="18" color="#165dff"><Grid /></el-icon>
              <span>字段映射</span>
            </div>
            <div class="section-desc">已加载 {{ sourceColumns.length }} 个源字段，配置映射关系</div>
            <el-radio-group v-model="mappingMode" style="margin-left:auto;" @change="handleMappingModeChange">
              <el-radio label="same">同名映射</el-radio>
              <el-radio label="type">类型兼容映射</el-radio>
              <el-radio label="custom">自定义</el-radio>
            </el-radio-group>
          </div>
          <FieldMappingTable :field-mappings="fieldMappings" :has-target-table="!!formData.targetTableName" :mapping-mode="mappingMode" :mapping-locked="mappingLocked" @update="handleFieldUpdate" />
        </div>
      </div>

      <!-- 步骤2：任务属性（扩展版） -->
      <div v-show="currentStep === 1" class="step-panel">
        <div class="step-panel-header">
          <div class="step-title">
            <el-icon :size="20" color="#0052d9"><Clock /></el-icon>
            任务属性
          </div>
          <div class="step-desc">配置方式、调度周期及写入模式</div>
        </div>
        <div class="task-property-container">
          <el-form label-width="120px">
            <!-- 配置方式 -->
            <el-divider content-position="left">基础配置</el-divider>
            <el-row :gutter="20">
              <el-col :span="12">
                <el-form-item label="配置方式">
                  <el-radio-group v-model="formData.configMode">
                    <el-radio label="normal">常规</el-radio>
                    <el-radio label="crontab">Crontab</el-radio>
                  </el-radio-group>
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="调度周期">
                  <el-radio-group v-model="formData.schedulePeriod">
                    <el-radio label="period">周期</el-radio>
                    <el-radio label="once">一次性</el-radio>
                  </el-radio-group>
                </el-form-item>
              </el-col>
            </el-row>
            
            <!-- 周期配置 -->
            <el-row :gutter="20" v-if="formData.schedulePeriod === 'period'">
              <el-col :span="12">
                <el-form-item label="周期类型">
                  <el-select v-model="formData.periodType" placeholder="选择周期" style="width: 100%">
                    <el-option label="天" value="day" />
                    <el-option label="周" value="week" />
                    <el-option label="月" value="month" />
                  </el-select>
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="执行时间">
                  <el-time-picker v-model="formData.executeTime" placeholder="选择时间" style="width: 100%" format="HH:mm" value-format="HH:mm" />
                </el-form-item>
              </el-col>
            </el-row>
            
            <!-- 写入配置 -->
            <el-divider content-position="left">写入配置</el-divider>
            <el-row :gutter="20">
              <el-col :span="12">
                <el-form-item label="写入模式">
                  <el-radio-group v-model="formData.writeMode">
                    <el-radio label="append">追加</el-radio>
                    <el-radio label="overwrite">覆盖</el-radio>
                    <el-radio label="nonConflict">非冲突</el-radio>
                  </el-radio-group>
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="批量大小">
                  <el-input-number v-model="formData.batchSize" :min="100" :max="50000" :step="100" />
                </el-form-item>
              </el-col>
            </el-row>
            
            <!-- 高级配置 -->
            <el-divider content-position="left">高级配置</el-divider>
            <el-row :gutter="20">
              <el-col :span="12">
                <el-form-item label="并发数">
                  <el-input-number v-model="formData.parallelism" :min="1" :max="64" />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="调度类型">
                  <el-radio-group v-model="formData.scheduleType">
                    <el-radio label="normal">正常调度</el-radio>
                    <el-radio label="skip">空跑调度</el-radio>
                  </el-radio-group>
                </el-form-item>
              </el-col>
            </el-row>
            
            <!-- Cron表达式（如果是crontab模式） -->
            <el-row :gutter="20" v-if="formData.configMode === 'crontab'">
              <el-col :span="24">
                <el-form-item label="Cron表达式">
                  <el-input v-model="formData.cronExpression" placeholder="* * * * *" />
                  <el-link type="primary" :underline="false" style="margin-top: 4px;">周期说明</el-link>
                </el-form-item>
              </el-col>
            </el-row>
          </el-form>
        </div>
      </div>

      <!-- 步骤3：日志 -->
      <div v-show="currentStep === 2" class="step-panel">
        <div class="step-panel-header">
          <div class="step-title">
            <el-icon :size="20" color="#00b96b"><Document /></el-icon>
            日志
          </div>
          <div class="step-desc">查看任务执行日志</div>
        </div>
        <div class="log-container">
          <div class="log-item" v-for="(log, i) in syncLogs" :key="i">
            <span class="log-text">{{ log }}</span>
          </div>
          <div v-if="syncLogs.length === 0" class="log-empty">
            <el-empty description="暂无日志" />
          </div>
        </div>
      </div>
    </div>

    <!-- 历史任务抽屉 -->
    <el-drawer
      v-model="showHistoryDrawer"
      title="历史任务"
      :size="'80%'"
      direction="rtl"
    >
      <div class="history-drawer-content">
        <!-- 搜索和筛选 -->
        <div class="search-bar" style="margin-bottom: 16px;">
          <el-input
            v-model="searchText"
            placeholder="搜索任务名称或数据库名称"
            :prefix-icon="Search"
            clearable
            style="width: 300px"
          />
          <div class="filter-group">
            <el-select v-model="filterType" placeholder="同步类型" clearable style="width: 150px">
              <el-option label="全量同步" value="全量同步" />
              <el-option label="增量同步" value="增量同步" />
            </el-select>
            <el-select v-model="filterStatus" placeholder="任务状态" clearable style="width: 150px">
              <el-option label="运行中" value="运行中" />
              <el-option label="已完成" value="已完成" />
            </el-select>
            <el-button @click="handleRefresh" :icon="Refresh">刷新</el-button>
          </div>
        </div>
    <div class="table-card">
      <el-table 
        :data="filteredData" 
        style="width: 100%" 
        v-loading="loading"
        max-height="600"
        :stripe="true"
        :border="false"
      >
        <el-table-column prop="name" label="任务名称" min-width="220">
          <template #default="{ row }">
            <div class="name-cell">
              <el-icon :size="16" color="#0052d9"><Operation /></el-icon>
              <span class="task-name">{{ row.name }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="源数据库" min-width="180">
          <template #default="{ row }">
            <div class="db-info">
              <el-tag size="small" :type="getDbTagType(row.sourceType)">{{ row.sourceType }}</el-tag>
              <span class="db-name">{{ row.sourceName || row.source }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="目标数据库" min-width="180">
          <template #default="{ row }">
            <div class="db-info">
              <el-tag size="small" :type="getDbTagType(row.targetType)">{{ row.targetType }}</el-tag>
              <span class="db-name">{{ row.targetName || row.target }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="type" label="同步类型" width="110">
          <template #default="{ row }">
            <el-tag size="small" :type="getSyncTypeTag(row.syncType || row.type)">{{ row.syncType || row.type }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createBy" label="任务负责人" width="120">
          <template #default="{ row }">
            <div style="display: flex; align-items: center; gap: 6px;">
              <el-icon :size="16" color="#0052d9"><User /></el-icon>
              <span>{{ row.createByName || row.createBy || 'admin' }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="进度" width="150">
          <template #default="{ row }">
            <div class="progress-cell">
              <el-progress 
                :percentage="row.progress || 0" 
                :status="getProgressStatus(row.status)"
                :stroke-width="8"
              />
            </div>
          </template>
        </el-table-column>
            <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="(statusMap.type as any)[row.status] as any || 'info'" size="small">
              {{ (statusMap.text as any)[row.status] || '未知' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="260" fixed="right">
          <template #default="{ row }">
            <div class="action-buttons">
              <el-button 
                v-if="row.status === 0 || row.status === 3" 
                type="success" 
                size="small" 
                link 
                @click="handleStart(row)"
              >
                <el-icon><VideoPlay /></el-icon>
                启动
              </el-button>
              <el-button 
                v-if="row.status === 1" 
                type="warning" 
                size="small" 
                link 
                @click="handlePause(row)"
              >
                <el-icon><VideoPause /></el-icon>
                暂停
              </el-button>
              <el-button type="primary" size="small" link @click="handleEdit(row)">
                <el-icon><Edit /></el-icon>
                编辑
              </el-button>
              <el-button type="info" size="small" link @click="handleView(row)">
                <el-icon><View /></el-icon>
                详情
              </el-button>
              <el-button class="btn-text-danger" link size="small" @click="handleDelete(row)" :disabled="row.status === 1">
                <el-icon><Delete /></el-icon>
                删除
              </el-button>
            </div>
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
      </div>
    </el-drawer>

    <!-- 创建/编辑任务对话框 - WeData风格全屏编辑器 -->
    <!-- 对话框已禁用，直接使用页面主体 -->
    <el-dialog
      v-if="false"
      v-model="dialogVisible"
      :title="dialogTitle"
      fullscreen
      :close-on-click-modal="false"
      class="wedata-style-dialog"
      destroy-on-close
    >
      <!-- WeData 风格顶部工具栏 -->
      <template #header>
        <div class="wedata-header">
          <div class="wedata-tabs">
            <div 
              v-for="(step, index) in steps" 
              :key="index"
              :class="['wedata-tab', { active: currentStep === index }]"
              @click="currentStep = index"
            >
              <span class="step-number">{{ index + 1 }}</span>
              <span class="step-name">{{ step.title }}</span>
            </div>
          </div>
          <div class="wedata-actions">
            <el-button @click="dialogVisible = false">取消</el-button>
            <el-button type="primary" @click="handleSubmit" :loading="submitLoading">
              <el-icon><Check /></el-icon>
              保存
            </el-button>
            <el-button type="success" @click="handleRun">
              <el-icon><VideoPlay /></el-icon>
              运行
            </el-button>
          </div>
        </div>
      </template>
      <div class="steps-wrapper-enhanced">
        <!-- 编辑模式：3个步骤 -->
        <el-steps v-if="isEditMode" :active="currentStep" align-center class="custom-steps-enhanced">
          <el-step title="配置数据源" description="选择源库与目标库及字段映射" />
          <el-step title="任务属性" description="配置方式、调度周期及写入模式" />
          <el-step title="日志" description="查看任务执行日志" />
        </el-steps>
        <!-- 新建模式：5个步骤 -->
        <el-steps v-else :active="currentStep" align-center class="custom-steps-enhanced">
          <el-step title="配置数据源" description="选择源库与目标库" />
          <el-step title="字段映射" description="表结构与类型映射" />
          <el-step title="任务属性" description="写入模式与并发" />
          <el-step title="调度设置" description="周期与触发" />
          <el-step title="保存与运行" description="提交并执行" />
        </el-steps>
      </div>

      <div class="step-content-enhanced">
        <!-- 步骤0：配置数据源 -->
        <div v-show="currentStep === 0" class="step-card">
          <div class="step-header">
            <div class="step-title">配置数据源</div>
          </div>
          <div class="step-body">
            <el-form :model="formData" :rules="formRules" ref="formRef" label-width="100px" class="enhanced-form">
              <el-form-item label="任务名称" prop="name" class="form-item-enhanced">
                <el-input v-model="formData.name" placeholder="请输入任务名称" clearable />
              </el-form-item>
              <el-row :gutter="16" class="datasource-config-wrapper">
                <el-col :span="12">
                  <div class="datasource-panel-enhanced source-panel">
                    <div class="panel-header-enhanced">
                      <div class="panel-icon-wrapper source-icon">
                        <el-icon><Coin /></el-icon>
                      </div>
                      <div class="panel-title-wrapper">
                        <div class="panel-title">源数据库</div>
                        <div class="panel-subtitle">选择源数据源与表</div>
                      </div>
                    </div>
                    <div class="panel-body">
                      <el-form-item label="数据源" prop="source">
                        <el-select v-model="formData.source" class="select-enhanced" placeholder="选择源数据源" filterable @change="handleSourceChange">
                          <el-option v-for="ds in datasourceList" :key="ds.id" :label="`${ds.name} (${ds.type})`" :value="ds.id" />
                        </el-select>
                      </el-form-item>
                      <div class="table-select-wrapper">
                        <el-select v-model="formData.selectedTable" class="table-select" placeholder="选择源表" filterable :loading="tableLoading" @change="handleTableSelect">
                          <el-option v-for="t in sourceTables" :key="t" :label="t" :value="t" />
                        </el-select>
                        <el-button class="refresh-btn" :icon="Refresh" @click="loadSourceTables">刷新</el-button>
                      </div>
                      <el-input v-model="formData.where" placeholder="筛选条件（WHERE）" class="input-enhanced" style="margin-top: 12px;" />
                      <el-input v-model="formData.splitKey" placeholder="切割键（大表并行）" class="input-enhanced" style="margin-top: 12px;" />
                    </div>
                  </div>
                </el-col>
                <el-col :span="12">
                  <div class="datasource-panel-enhanced target-panel">
                    <div class="panel-header-enhanced">
                      <div class="panel-icon-wrapper target-icon">
                        <el-icon><Coin /></el-icon>
                      </div>
                      <div class="panel-title-wrapper">
                        <div class="panel-title">目标数据库</div>
                        <div class="panel-subtitle">选择目标数据源与目标表</div>
                      </div>
                    </div>
                    <div class="panel-body">
                      <el-form-item label="数据源" prop="target">
                        <el-select v-model="formData.target" class="select-enhanced" placeholder="选择目标数据源" filterable>
                          <el-option v-for="ds in datasourceList" :key="ds.id" :label="`${ds.name} (${ds.type})`" :value="ds.id" />
                        </el-select>
                      </el-form-item>
                      <el-input v-model="formData.targetTableName" placeholder="目标表名" class="input-enhanced" />
                      <div style="margin-top: 12px; display: flex; gap: 12px;">
                        <el-button type="primary" @click="handleCreateTargetTable">一键建立目标表</el-button>
                        <el-button @click="handlePreviewDDL">预览建表语句</el-button>
                      </div>
                    </div>
                  </div>
                </el-col>
              </el-row>
            </el-form>
            
            <!-- 编辑模式下，在步骤0中显示字段映射 -->
            <div v-if="isEditMode && showFieldMapping" style="margin-top: 24px;">
              <el-divider content-position="left">
                <span style="font-size: 16px; font-weight: 600;">字段映射</span>
              </el-divider>
              <FieldMappingTable :field-mappings="fieldMappings" @update="handleFieldUpdate" />
            </div>
          </div>
        </div>

        <!-- 步骤1：新建模式=字段映射，编辑模式=任务属性 -->
        <div v-show="currentStep === 1" class="step-card">
          <!-- 新建模式：字段映射 -->
          <template v-if="!isEditMode">
            <div class="step-header">
              <div class="step-title">字段映射</div>
            </div>
            <div class="step-body">
              <FieldMappingTable :field-mappings="fieldMappings" @update="handleFieldUpdate" />
            </div>
          </template>
          
          <!-- 编辑模式：任务属性（包含调度配置） -->
          <template v-else>
            <div class="step-header">
              <div class="step-title">任务属性</div>
            </div>
            <div class="step-body">
              <el-row :gutter="16">
                <el-col :span="12">
                  <el-radio-group v-model="formData.writeMode" class="radio-group-enhanced">
                    <el-radio label="append" class="radio-enhanced">
                      <div class="radio-content">
                        <div class="radio-title">追加写入</div>
                        <div class="radio-desc">在目标表后追加数据</div>
                      </div>
                    </el-radio>
                    <el-radio label="overwrite" class="radio-enhanced">
                      <div class="radio-content">
                        <div class="radio-title">覆盖写入</div>
                        <div class="radio-desc">覆盖目标表已有数据</div>
                      </div>
                    </el-radio>
                    <el-radio label="nonConflict" class="radio-enhanced">
                      <div class="radio-content">
                        <div class="radio-title">非冲突写入</div>
                        <div class="radio-desc">按主键去重避免冲突</div>
                      </div>
                    </el-radio>
                  </el-radio-group>
                </el-col>
                <el-col :span="12">
                  <el-form label-width="100px">
                    <el-form-item label="批量大小">
                      <el-input-number v-model="formData.batchSize" :min="100" :max="50000" :step="100" />
                    </el-form-item>
                    <el-form-item label="并发数">
                      <el-input-number v-model="formData.parallelism" :min="1" :max="64" />
                    </el-form-item>
                    <el-form-item label="空值策略">
                      <el-select v-model="formData.nullPolicy" class="select-enhanced" placeholder="请选择">
                        <el-option label="不处理" value="ignore" />
                        <el-option label="置为 null" value="setNull" />
                      </el-select>
                    </el-form-item>
                  </el-form>
                </el-col>
              </el-row>
              <el-divider />
              <el-row :gutter="16">
                <el-col :span="12">
                  <el-input v-model="formData.preSql" type="textarea" :rows="4" placeholder="前置SQL" class="textarea-enhanced" />
                </el-col>
                <el-col :span="12">
                  <el-input v-model="formData.postSql" type="textarea" :rows="4" placeholder="后置SQL" class="textarea-enhanced" />
                </el-col>
              </el-row>
              
              <!-- 调度配置 -->
              <el-divider content-position="left">
                <span style="font-size: 16px; font-weight: 600;">调度配置</span>
              </el-divider>
              <el-radio-group v-model="formData.configMode" class="radio-group-enhanced">
                <el-radio label="normal" class="radio-enhanced">
                  <div class="radio-content">
                    <div class="radio-title">手动</div>
                    <div class="radio-desc">手动触发执行</div>
                  </div>
                </el-radio>
                <el-radio label="crontab" class="radio-enhanced">
                  <div class="radio-content">
                    <div class="radio-title">Cron</div>
                    <div class="radio-desc">按 Cron 表达式执行</div>
                  </div>
                </el-radio>
              </el-radio-group>
              <div v-if="formData.configMode === 'crontab'" class="cron-input-wrapper">
                <el-input v-model="formData.cronExpression" placeholder="* * * * *" class="cron-input" />
              </div>
            </div>
          </template>
        </div>

        <!-- 步骤2：新建模式=任务属性，编辑模式=日志 -->
        <div v-show="currentStep === 2" class="step-card">
          <!-- 新建模式：任务属性 -->
          <template v-if="!isEditMode">
            <div class="step-header">
              <div class="step-title">任务属性</div>
            </div>
            <div class="step-body">
              <el-row :gutter="16">
                <el-col :span="12">
                  <el-radio-group v-model="formData.writeMode" class="radio-group-enhanced">
                    <el-radio label="append" class="radio-enhanced">
                      <div class="radio-content">
                        <div class="radio-title">追加写入</div>
                        <div class="radio-desc">在目标表后追加数据</div>
                      </div>
                    </el-radio>
                    <el-radio label="overwrite" class="radio-enhanced">
                      <div class="radio-content">
                        <div class="radio-title">覆盖写入</div>
                        <div class="radio-desc">覆盖目标表已有数据</div>
                      </div>
                    </el-radio>
                    <el-radio label="nonConflict" class="radio-enhanced">
                      <div class="radio-content">
                        <div class="radio-title">非冲突写入</div>
                        <div class="radio-desc">按主键去重避免冲突</div>
                      </div>
                    </el-radio>
                  </el-radio-group>
                </el-col>
                <el-col :span="12">
                  <el-form label-width="100px">
                    <el-form-item label="批量大小">
                      <el-input-number v-model="formData.batchSize" :min="100" :max="50000" :step="100" />
                    </el-form-item>
                    <el-form-item label="并发数">
                      <el-input-number v-model="formData.parallelism" :min="1" :max="64" />
                    </el-form-item>
                    <el-form-item label="空值策略">
                      <el-select v-model="formData.nullPolicy" class="select-enhanced" placeholder="请选择">
                        <el-option label="不处理" value="ignore" />
                        <el-option label="置为 null" value="setNull" />
                      </el-select>
                    </el-form-item>
                  </el-form>
                </el-col>
              </el-row>
              <el-divider />
              <el-row :gutter="16">
                <el-col :span="12">
                  <el-input v-model="formData.preSql" type="textarea" :rows="4" placeholder="前置SQL" class="textarea-enhanced" />
                </el-col>
                <el-col :span="12">
                  <el-input v-model="formData.postSql" type="textarea" :rows="4" placeholder="后置SQL" class="textarea-enhanced" />
                </el-col>
              </el-row>
            </div>
          </template>
          
          <!-- 编辑模式：日志 -->
          <template v-else>
            <div class="step-header">
              <div class="step-title">日志</div>
            </div>
            <div class="step-body">
              <div class="log-container">
                <div class="log-item" v-for="(log, i) in syncLogs" :key="i">
                  <span class="log-text">{{ log }}</span>
                </div>
                <div v-if="syncLogs.length === 0" class="log-empty">
                  <el-empty description="暂无日志" />
                </div>
              </div>
            </div>
          </template>
        </div>

        <div v-show="currentStep === 3" class="step-card">
          <div class="step-header">
            <div class="step-title">调度设置</div>
          </div>
          <div class="step-body">
            <el-radio-group v-model="formData.configMode" class="radio-group-enhanced">
              <el-radio label="normal" class="radio-enhanced">
                <div class="radio-content">
                  <div class="radio-title">手动</div>
                  <div class="radio-desc">手动触发执行</div>
                </div>
              </el-radio>
              <el-radio label="crontab" class="radio-enhanced">
                <div class="radio-content">
                  <div class="radio-title">Cron</div>
                  <div class="radio-desc">按 Cron 表达式执行</div>
                </div>
              </el-radio>
            </el-radio-group>
            <div v-if="formData.configMode === 'crontab'" class="cron-input-wrapper">
              <el-input v-model="formData.cronExpression" placeholder="* * * * *" class="cron-input" />
            </div>
          </div>
        </div>

        <div v-show="currentStep === 4" class="step-card">
          <div class="step-header">
            <div class="step-title">保存与运行</div>
          </div>
          <div class="step-body">
            <div class="dialog-footer-enhanced">
              <el-button class="footer-btn cancel-btn" @click="dialogVisible = false">取消</el-button>
              <el-button class="footer-btn primary-btn" type="primary" @click="handleSubmit" :loading="submitLoading">保存</el-button>
              <el-button type="success" @click="handleRun">运行</el-button>
            </div>
            <div class="log-container" style="margin-top: 16px;">
              <div class="log-item" v-for="(log, i) in syncLogs" :key="i">
                <span class="log-text">{{ log }}</span>
              </div>
              <div v-if="syncLogs.length === 0" class="log-empty">
                <el-empty description="暂无日志" />
              </div>
            </div>
          </div>
        </div>
      </div>

      <template #footer>
        <div class="dialog-footer-enhanced">
          <el-button class="footer-btn cancel-btn" @click="dialogVisible = false">关闭</el-button>
          <div style="flex:1"></div>
          <el-button @click="prevStep" :disabled="currentStep===0">上一步</el-button>
          <el-button type="primary" @click="nextStep" :disabled="currentStep===4">下一步</el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 查看任务详情对话框（子组件） -->
    <TaskDetailDialog 
      v-model:visible="viewDialogVisible" 
      :currentTask="currentTask" 
      v-model:syncLogs="syncLogs"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted, defineAsyncComponent, watch } from 'vue'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import { useRoute, useRouter } from 'vue-router'
import { Search, Refresh, Upload, Download, Document, Right, User, EditPen, InfoFilled, Grid, Connection, Clock, ArrowLeft, ArrowRight, Check, Timer, Coin, Operation, VideoPlay, VideoPause, Edit, View, Delete, Plus, SuccessFilled, CircleCloseFilled, Fold, Expand, RefreshLeft } from '@element-plus/icons-vue'
import { syncApi } from '@/api/sync'
import { datasourceApi } from '@/api/datasource'

const FieldMappingTable = defineAsyncComponent(() => import('./components/FieldMappingTable.vue'))
const TaskDetailDialog = defineAsyncComponent(() => import('./components/TaskDetailDialog.vue'))
const route = useRoute()
const router = useRouter()

// 搜索和筛选
const searchText = ref('')
const filterType = ref('')
const filterStatus = ref('')
const filterOwner = ref('')
const loading = ref(false)

// 页面加载状态
const pageLoading = ref(true)

// WeData 风格步骤定义（优化后3步）
const steps = [
  { title: '配置数据源', description: '选择源库与目标库及字段映射' },
  { title: '任务属性', description: '配置方式、调度周期及写入模式' },
  { title: '日志', description: '查看任务执行日志' },
]

// 历史任务抽屉
const showHistoryDrawer = ref(false)

// 表格数据 - 使用简单数据结构提升性能
const tableData = ref<any[]>([])

// 数据源列表（用于表单选择）
const datasourceList = ref<any[]>([])

// 加载同步任务列表（后端分页）- 优化版：使用骨架屏和懒加载
const loadSyncTasks = async () => {
  loading.value = true
  try {
    // 快速显示骨架屏，延迟加载真实数据
    const res: any = await syncApi.page({ 
      current: currentPage.value, 
      size: pageSize.value, 
      keyword: searchText.value || '' 
    })
    const records = res.records || []
    tableData.value = records.map((r: any) => ({
      id: r.id,
      name: r.taskName,
      sourceName: r.sourceTable,
      sourceType: 'MySQL',
      targetName: r.targetTable,
      targetType: 'PostgreSQL',
      syncType: r.syncMode === 'incremental' ? '增量同步' : '全量同步',
      status: r.status === 'running' ? 1 : r.status === 'success' ? 2 : r.status === 'failed' ? 3 : 0,
      progress: r.progress || 0,
      createByName: 'admin',
      createTime: r.createTime,
    }))
    total.value = res.total || records.length
  } catch (error: any) {
    console.error('加载同步任务列表失败:', error)
    // 失败时使用空数据，不阻塞页面
    tableData.value = []
    total.value = 0
  } finally {
    loading.value = false
  }
}

// 加载数据源列表（后端）- 优化版：异步加载，不阻塞主流程
const loadDatasources = async () => {
  try {
    const list: any = await datasourceApi.list()
    datasourceList.value = Array.isArray(list) ? list : []
  } catch (error: any) {
    datasourceList.value = []
    console.error('加载数据源列表失败:', error)
  }
}

// 组件挂载时加载数据 - 优化版：先显示页面，再加载数据
onMounted(() => {
  // 立即显示页面框架
  pageLoading.value = false
  
  // 延迟50ms加载数据，让页面先渲染
  setTimeout(() => {
    loadSyncTasks()
  }, 50)
  
  // 延迟加载数据源列表（非关键路径）
  setTimeout(() => {
    loadDatasources()
  }, 300)
  
  // 路由参数处理（如果需要）
  const id = route.params.id as any
  const editId = route.query.edit as any
  
  // 如果有 edit 参数，直接在页面主体加载数据（不打开对话框）
  if (editId) {
    (async () => {
      try {
        const task = await syncApi.getById(Number(editId))
        
        // 加载完整的任务数据到页面主体
        Object.assign(formData, {
          id: task.id,
          name: task.taskName,
          description: task.description || '',
          source: task.sourceDatasourceId,
          target: task.targetDatasourceId,
          type: task.syncMode === 'incremental' ? '增量同步' : '全量同步',
          selectedTable: task.sourceTable,
          targetTableName: task.targetTable,
          
          // 恢复 Trino 相关配置
          sourceCatalog: task.sourceCatalog || '',
          sourceDb: task.sourceSchema || '',
          targetCatalog: task.targetCatalog || '',
          targetDb: task.targetSchema || '',
          
          // 恢复任务属性
          where: task.whereClause || '',
          splitKey: task.splitKey || '',
          writeMode: task.writeMode || 'append',
          batchSize: task.batchSize || 1024,
          parallelism: task.parallelism || 1,
          nullPolicy: task.nullPolicy || 'ignore',
          preSql: task.preSql || '',
          postSql: task.postSql || '',
          
          // 恢复调度配置
          scheduleType: task.scheduleType === 'cron' ? 'normal' : 'skip',
          cronExpression: task.cronExpression || '',
        })
        
        // 恢复字段映射
        if (task.fieldMappings) {
          try {
            fieldMappings.value = JSON.parse(task.fieldMappings)
          } catch (e) {
            console.error('解析字段映射失败:', e)
          }
        }
        
        // 加载数据源信息和表列表
        try {
          // 获取数据源信息
          const sourceDatasource = await datasourceApi.getById(task.sourceDatasourceId)
          const targetDatasource = await datasourceApi.getById(task.targetDatasourceId)
          
          formData.sourceType = sourceDatasource.type
          formData.targetType = targetDatasource.type
          
          // 加载源表列表
          const tables = await datasourceApi.getTables(task.sourceDatasourceId, '')
          sourceTables.value = tables
          
          // 加载目标数据源的 Catalog/Database 列表
          if (targetDatasource.type === 'Trino') {
            const catalogs = await datasourceApi.getDatabases(task.targetDatasourceId)
            targetCatalogs.value = catalogs
          }
          
          // 加载源表字段
          if (task.sourceTable) {
            const sourceFields = await datasourceApi.getTableColumns(
              task.sourceDatasourceId,
              task.sourceTable
            )
            sourceColumns.value = sourceFields
            
            // 如果没有保存的字段映射，初始化默认映射
            if (!task.fieldMappings || fieldMappings.value.length === 0) {
              fieldMappings.value = sourceFields.map((col: any) => ({
                sourceColumn: col.columnName,
                sourceType: col.dataType,
                targetColumn: col.columnName,
                targetType: col.dataType,
                transform: '',
              }))
            }
          }
          
          // 加载目标表字段
          if (task.targetTable && formData.targetDb) {
            // 对于 Trino，需要传递 catalog.schema
            let databaseParam = formData.targetDb
            if (targetDatasource.type === 'Trino' && formData.targetCatalog) {
              databaseParam = `${formData.targetCatalog}.${formData.targetDb}`
            }
                    
            const targetFields = await datasourceApi.getTableColumns(
              task.targetDatasourceId,
              task.targetTable,
              databaseParam
            )
            targetColumns.value = targetFields
          }
          
        } catch (error) {
          console.error('加载数据失败:', error)
        }
        
        // 编辑时从第0步开始，让用户能看到所有配置
        currentStep.value = 0
        
        // 清除 query 参数
        router.replace({ path: '/dataintegration/sync' })
      } catch (error) {
        console.error('加载任务失败:', error)
        ElMessage.error('加载任务失败')
      }
    })()
  }
  
  if (id) {
    if (route.name === 'DataSyncView') {
      syncApi.getById(Number(id)).then((task: any) => {
        currentTask.value = {
          id: task.id,
          name: task.taskName,
          sourceName: task.sourceTable,
          sourceType: 'MySQL',
          targetName: task.targetTable,
          targetType: 'PostgreSQL',
          syncType: task.syncMode === 'incremental' ? '增量同步' : '全量同步',
          status: task.status,
          progress: task.progress || 0,
          createByName: 'admin',
          createTime: task.createTime,
        }
        viewDialogVisible.value = true
      })
    }
    if (route.name === 'DataSyncEdit') {
      syncApi.getById(Number(id)).then((task: any) => {
        dialogTitle.value = '编辑同步任务'
        Object.assign(formData, {
          id: task.id,
          name: task.taskName,
          source: task.sourceDatasourceId,
          target: task.targetDatasourceId,
          type: task.syncMode === 'incremental' ? '增量同步' : '全量同步',
          selectedTable: task.sourceTable,
          targetTableName: task.targetTable,
        })
        dialogVisible.value = true
      })
    }
  }
})

// 旧的测试数据（已废弃）
const oldTestData = [
  {
    id: 1,
    name: 'MySQL到PostgreSQL全量同步',
    source: 'MySQL主库',
    sourceType: 'MySQL',
    target: 'PostgreSQL分析库',
    targetType: 'PostgreSQL',
    type: '全量同步',
    status: '运行中',
    progress: 65,
    createTime: '2025-01-15 10:30:00',
    updateTime: '2025-01-15 14:20:00',
    tableCount: 25,
    recordCount: 1250000,
  },
  {
    id: 2,
    name: 'Oracle增量同步任务',
    source: 'Oracle数据仓库',
    sourceType: 'Oracle',
    target: 'MySQL主库',
    targetType: 'MySQL',
    type: '增量同步',
    status: '已完成',
    progress: 100,
    createTime: '2025-01-14 15:20:00',
    updateTime: '2025-01-14 18:30:00',
    tableCount: 15,
    recordCount: 850000,
  },
  {
    id: 3,
    name: 'PostgreSQL结构同步',
    source: 'PostgreSQL分析库',
    sourceType: 'PostgreSQL',
    target: 'MySQL主库',
    targetType: 'MySQL',
    type: '结构同步',
    status: '已完成',
    progress: 100,
    createTime: '2025-01-13 09:15:00',
    updateTime: '2025-01-13 09:20:00',
    tableCount: 30,
    recordCount: 0,
  },
  {
    id: 4,
    name: '达梦到MySQL迁移',
    source: '达梦数据库',
    sourceType: 'DM',
    target: 'MySQL主库',
    targetType: 'MySQL',
    type: '全量同步',
    status: '暂停',
    progress: 45,
    createTime: '2025-01-12 14:45:00',
    updateTime: '2025-01-12 16:30:00',
    tableCount: 20,
    recordCount: 500000,
  },
  {
    id: 5,
    name: '人大金仓同步任务',
    source: '人大金仓数据库',
    sourceType: 'KingBase',
    target: 'PostgreSQL分析库',
    targetType: 'PostgreSQL',
    type: '增量同步',
    status: '失败',
    progress: 30,
    createTime: '2025-01-11 11:30:00',
    updateTime: '2025-01-11 12:15:00',
    tableCount: 10,
    recordCount: 200000,
  },
]

// 优化任务统计计算 - 缓存结果避免重复计算
const taskStats = computed(() => {
  const data = tableData.value
  if (!data.length) return { total: 0, running: 0, completed: 0, failed: 0 }
  
  let total = 0, running = 0, completed = 0, failed = 0
  for (const task of data) {
    total++
    const status = task.status
    if (status === 1) running++
    else if (status === 2) completed++
    else if (status === 3) failed++
  }
  return { total, running, completed, failed }
})

// 优化负责人列表计算 - 缓存结果
const ownerList = computed(() => {
  const data = tableData.value
  if (!data.length) return ['admin']
  
  const owners = new Set<string>()
  for (const task of data) {
    owners.add(task.createByName || 'admin')
  }
  return Array.from(owners)
})

// 分页
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

// 进一步优化过滤逻辑 - 使用简单的条件判断
const filteredData = computed(() => {
  const searchLower = searchText.value.toLowerCase()
  const typeFilter = filterType.value
  const statusFilter = filterStatus.value
  const ownerFilter = filterOwner.value
  
  // 无过滤条件时直接返回原数据
  if (!searchLower && !typeFilter && !statusFilter && !ownerFilter) {
    return tableData.value
  }
  
  return tableData.value.filter(item => {
    // 搜索过滤 - 简化判断
    if (searchLower) {
      if (!item.name?.toLowerCase().includes(searchLower)) {
        const source = (item.sourceName || item.source || '').toLowerCase()
        const target = (item.targetName || item.target || '').toLowerCase()
        if (!source.includes(searchLower) && !target.includes(searchLower)) {
          return false
        }
      }
    }
    
    // 类型过滤
    if (typeFilter && item.syncType !== typeFilter) return false
    
    // 状态过滤 - 使用数字状态提升性能
    if (statusFilter) {
      const statusFilterMap: Record<string, number> = { '运行中': 1, '已完成': 2, '失败': 3, '暂停': 0 }
      if (statusFilterMap[statusFilter] !== item.status) return false
    }
    
    // 负责人过滤
    if (ownerFilter && (item.createByName || 'admin') !== ownerFilter) return false
    
    return true
  })
})

// 对话框
// 对话框相关变量（已废弃，直接使用页面主体）
const dialogVisible = ref(false)
const dialogTitle = ref('创建同步任务')
const submitLoading = ref(false)
const formRef = ref<FormInstance>()
const currentStep = ref(0)

// 查看详情对话框
const viewDialogVisible = ref(false)
const currentTask = ref<any>(null)

// 同步日志
const syncLogs = ref<string[]>([])
const logPollingTimer = ref<NodeJS.Timeout | null>(null)
const currentTaskId = ref<number | null>(null)

// 编辑模式标识（根据formData.id判断）
const isEditMode = computed(() => !!formData.id)

// 表单数据
const formData = reactive({
  id: null as number | null,
  name: '',
  description: '', // 任务描述
  source: '' as string | number,
  sourceType: '',
  sourceCatalog: '', // 源Catalog（针对Trino）
  sourceDb: '', // 源数据库名
  target: '' as string | number,
  targetType: '',
  targetCatalog: '', // Trino Catalog
  targetDb: '', // 目标数据库名/Schema
  type: '全量同步',
  tables: [] as string[],
  selectedTable: '',
  targetTableName: '',
  where: '',
  splitKey: '',
  writeMode: 'append',
  batchSize: 1024,
  parallelism: 1,
  nullPolicy: 'ignore',
  preSql: '',
  postSql: '',
  scheduleType: 'normal' as 'normal' | 'skip',
  cronExpression: '',
  // 新增字段：任务属性配置
  configMode: 'normal' as 'normal' | 'crontab',
  schedulePeriod: 'period' as 'period' | 'once',
  periodType: 'day' as 'day' | 'week' | 'month',
  executeTime: '00:00',
})

// 表单验证规则
const formRules: FormRules = {
  name: [{ required: true, message: '请输入任务名称', trigger: 'blur' }],
  source: [{ required: true, message: '请选择源数据源', trigger: 'change' }],
  target: [{ required: true, message: '请选择目标数据源', trigger: 'change' }],
  type: [{ required: true, message: '请选择同步类型', trigger: 'change' }],
}

const sourceTables = ref<string[]>([])
const sourceColumns = ref<any[]>([])
const targetColumns = ref<any[]>([])
const targetCatalogs = ref<string[]>([])
const targetDatabases = ref<string[]>([])
const targetTables = ref<string[]>([])
const fieldMappings = ref<any[]>([])
const tableLoading = ref(false)
const targetCatalogLoading = ref(false)
const targetDbLoading = ref(false)
const targetTableLoading = ref(false)
const mappingMode = ref<'same'|'type'|'custom'>('same')
const mappingLocked = ref(false) // 映射关系锁定状态

// 计算属性：判断目标数据源是否为 Trino
const isTargetTrino = computed(() => {
  const targetDs = datasourceList.value.find(ds => ds.id === formData.target)
  return targetDs?.type === 'Trino'
})

// 计算属性：是否显示字段映射（只要源表选择并加载了字段就显示）
const showFieldMapping = computed(() => {
  return !!formData.selectedTable && sourceColumns.value.length > 0
})

// 获取数据库标签类型
const getDbTagType = (type: string) => {
  const map: Record<string, any> = {
    'MySQL': 'primary',
    'Oracle': 'danger',
    'PostgreSQL': 'success',
    'SQLServer': 'warning',
    'DM': 'info',
    'KingBase': '',
  }
  return map[type] || ''
}

// 获取同步类型标签
const getSyncTypeTag = (type: string) => {
  const map: Record<string, any> = {
    '全量同步': 'primary',
    '增量同步': 'success',
    '结构同步': 'warning',
  }
  return map[type] || ''
}

// 优化状态相关函数 - 使用简单的数字映射
const statusMap = {
  class: { 0: 'warning', 1: 'running', 2: 'success', 3: 'error' } as Record<number, string>,
  text: { 0: '暂停', 1: '运行中', 2: '已完成', 3: '失败' } as Record<number, string>,
  type: { 0: 'warning', 1: 'primary', 2: 'success', 3: 'danger' } as Record<number, string>
}

const getStatusClass = (status: string | number) => {
  if (typeof status === 'number') {
    return (statusMap.class as any)[status] || 'info'
  }
  return (statusMap.class as any)[0] || 'info' // 默认返回暂停状态
}

const getStatusText = (status: string | number) => {
  if (typeof status === 'number') {
    return (statusMap.text as any)[status] || '未知'
  }
  return status
}

const getStatusType = (status: string) => {
  const map: Record<string, any> = {
    '运行中': 'primary',
    '已完成': 'success',
    '暂停': 'warning',
    '失败': 'danger',
  }
  return map[status] || 'info'
}

// 获取进度状态
const getProgressStatus = (status: string) => {
  if (status === '已完成') return 'success'
  if (status === '失败') return 'exception'
  return undefined
}

// 步骤控制在文件底部统一定义（支持 0-4 五步）

// 数据源变化处理
const handleSourceChange = async (datasourceId: string | number) => {
  sourceTables.value = []
  sourceColumns.value = []
  fieldMappings.value = []
  formData.selectedTable = ''
  
  if (datasourceId) {
    await loadSourceTables()
  }
}

const handleTargetChange = async (datasourceId: string | number) => {
  // 目标数据源变化时的处理
  targetCatalogs.value = []
  targetDatabases.value = []
  targetTables.value = []
  formData.targetCatalog = ''
  formData.targetDb = ''
  formData.targetTableName = ''
  targetColumns.value = []
  
  if (datasourceId) {
    // 判断是否为 Trino，如果是则加载 Catalog，否则加载数据库
    const targetDs = datasourceList.value.find(ds => ds.id === datasourceId)
    if (targetDs?.type === 'Trino') {
      await loadTargetCatalogs()
    } else {
      await loadTargetDatabases()
    }
  }
}

// 加载 Trino Catalog 列表
const loadTargetCatalogs = async () => {
  if (!formData.target) {
    ElMessage.warning('请先选择目标数据源')
    return
  }
  
  targetCatalogLoading.value = true
  try {
    const catalogs = await datasourceApi.getDatabases(Number(formData.target))
    targetCatalogs.value = catalogs
    ElMessage.success(`加载成功，共${catalogs.length}个Catalog`)
  } catch (error: any) {
    ElMessage.error('加载Catalog列表失败: ' + (error.message || '未知错误'))
  } finally {
    targetCatalogLoading.value = false
  }
}

// Catalog 变化处理
const handleTargetCatalogChange = async (catalog: string) => {
  targetDatabases.value = []
  targetTables.value = []
  formData.targetDb = ''
  formData.targetTableName = ''
  targetColumns.value = []
  
  if (catalog) {
    await loadTargetDatabases()
  }
}

// 加载目标数据源的库列表（或 Trino 的 Schema 列表）
const loadTargetDatabases = async () => {
  if (!formData.target) {
    ElMessage.warning('请先选择目标数据源')
    return
  }
  
  targetDbLoading.value = true
  try {
    // 对于 Trino，需要传入 catalog 参数
    const params = isTargetTrino.value && formData.targetCatalog 
      ? { catalog: formData.targetCatalog } 
      : {}
    const databases = await datasourceApi.getSchemas(Number(formData.target), params)
    targetDatabases.value = databases
    const label = isTargetTrino.value ? 'Schema' : '库'
    ElMessage.success(`加载成功，共${databases.length}个${label}`)
  } catch (error: any) {
    const label = isTargetTrino.value ? 'Schema' : '库'
    ElMessage.error(`加载${label}列表失败: ` + (error.message || '未知错误'))
  } finally {
    targetDbLoading.value = false
  }
}

// 目标库变化处理
const handleTargetDbChange = async (dbName: string) => {
  targetTables.value = []
  formData.targetTableName = ''
  targetColumns.value = []
  
  if (dbName) {
    await loadTargetTables()
  }
}

// 加载目标库的表列表
const loadTargetTables = async () => {
  if (!formData.target) {
    ElMessage.warning('请先选择目标数据源')
    return
  }
  
  targetTableLoading.value = true
  try {
    const tables = await datasourceApi.getTables(Number(formData.target), formData.targetDb)
    targetTables.value = tables
    ElMessage.success(`加载成功，共${tables.length}张表`)
  } catch (error: any) {
    ElMessage.error('加载表列表失败: ' + (error.message || '未知错误'))
  } finally {
    targetTableLoading.value = false
  }
}

// 选择目标表后加载字段信息
const handleTargetTableSelect = async (tableName: string) => {
  if (!tableName || !formData.target) return
  
  targetTableLoading.value = true
  try {
    // 对于 Trino，需要传递 catalog.schema 的组合
    let databaseParam = formData.targetDb
    if (isTargetTrino.value && formData.targetCatalog) {
      databaseParam = `${formData.targetCatalog}.${formData.targetDb}`
    }
    
    const columns = await datasourceApi.getTableColumns(Number(formData.target), tableName, databaseParam)
    targetColumns.value = columns
    
    // 如果有源字段，自动匹配到目标字段
    if (fieldMappings.value.length > 0) {
      // 应用映射模式，将目标字段填充到映射表中
      applyMappingMode()
    }
    
    ElMessage.success(`加载成功，共${columns.length}个字段`)
  } catch (error: any) {
    ElMessage.error('加载字段信息失败: ' + (error.message || '未知错误'))
  } finally {
    targetTableLoading.value = false
  }
}

// 处理映射模式变化
const handleMappingModeChange = () => {
  mappingLocked.value = true // 锁定映射关系
  applyMappingMode()
}

// 应用映射模式
const applyMappingMode = () => {
  if (!fieldMappings.value.length || !targetColumns.value.length) return
  
  if (mappingMode.value === 'same') {
    // 同名映射：根据源字段名在目标字段中查找同名字段
    fieldMappings.value = fieldMappings.value.map((m: any) => {
      const targetField = targetColumns.value.find(
        (col: any) => col.columnName.toLowerCase() === m.sourceColumn.toLowerCase()
      )
      return {
        ...m,
        targetColumn: targetField ? targetField.columnName : '',
        targetType: targetField ? targetField.dataType : '',
      }
    })
  } else if (mappingMode.value === 'type') {
    // 类型兼容映射：根据类型兼容性自动匹配
    fieldMappings.value = fieldMappings.value.map((m: any, index: number) => {
      const targetField = targetColumns.value[index]
      return {
        ...m,
        targetColumn: targetField ? targetField.columnName : '',
        targetType: targetField ? targetField.dataType : '',
      }
    })
  }
  // custom 模式不自动填充，保留用户手动配置
}

// 监听目标表名变化，当填写目标表后自动应用映射规则
watch(() => formData.targetTableName, async (newVal, oldVal) => {
  if (newVal && newVal !== oldVal && sourceColumns.value.length > 0) {
    // 如果目标表名发生变化，尝试加载目标表字段
    if (formData.target && formData.targetDb) {
      await handleTargetTableSelect(newVal)
    }
    
    // 应用映射模式
    if (fieldMappings.value.length > 0) {
      applyMappingMode()
    }
  }
})

// 监听对话框关闭，停止日志轮询
watch(() => dialogVisible.value, (newVal) => {
  if (!newVal) {
    stopLogPolling()
    syncLogs.value = []
    currentTaskId.value = null
  }
})

// 刷新任务列表 - 添加防抖
let refreshTimeout: NodeJS.Timeout | null = null

const handleRefresh = () => {
  if (refreshTimeout) clearTimeout(refreshTimeout)
  refreshTimeout = setTimeout(() => {
    loadSyncTasks()
  }, 200)
}

// 分页处理 - 添加防抖避免重复请求
let pageChangeTimeout: NodeJS.Timeout | null = null

const handleSizeChange = (val: number) => {
  if (pageChangeTimeout) clearTimeout(pageChangeTimeout)
  pageChangeTimeout = setTimeout(() => {
    pageSize.value = val
    currentPage.value = 1
    loadSyncTasks()
  }, 150)
}

const handleCurrentChange = (val: number) => {
  if (pageChangeTimeout) clearTimeout(pageChangeTimeout)
  pageChangeTimeout = setTimeout(() => {
    currentPage.value = val
    loadSyncTasks()
  }, 150)
}

// 加载源数据源的表列表
const loadSourceTables = async () => {
  if (!formData.source) {
    ElMessage.warning('请先选择源数据源')
    return
  }
  
  tableLoading.value = true
  try {
    const tables = await datasourceApi.getTables(Number(formData.source))
    sourceTables.value = tables
    ElMessage.success(`加载成功，共${tables.length}张表`)
  } catch (error: any) {
    ElMessage.error('加载表列表失败: ' + (error.message || '未知错误'))
  } finally {
    tableLoading.value = false
  }
}

// 选择表后加载字段信息
const handleTableSelect = async (tableName: string) => {
  if (!tableName || !formData.source) return
  
  tableLoading.value = true
  try {
    const columns = await datasourceApi.getTableColumns(Number(formData.source), tableName)
    sourceColumns.value = columns
    
    // 初始化字段映射 - 只设置源字段，目标字段留空
    fieldMappings.value = columns.map((col: any) => ({
      sourceColumn: col.columnName,
      sourceType: col.dataType,
      targetColumn: '', // 目标字段初始为空
      targetType: '', // 目标类型初始为空
      remarks: col.remarks,
    }))
    
    ElMessage.success(`加载成功，共${columns.length}个字段`)
    
    // 如果目标表已填写，立即应用映射规则
    if (formData.targetTableName) {
      applyMappingMode()
    }
  } catch (error: any) {
    ElMessage.error('加载字段信息失败: ' + (error.message || '未知错误'))
  } finally {
    tableLoading.value = false
  }
}

// 更新字段映射（优化性能）
const handleFieldUpdate = (index: number, field: string, value: any) => {
  if (fieldMappings.value[index]) {
    fieldMappings.value[index][field] = value
  }
}

// 创建任务
const handleCreateTask = () => {
  dialogTitle.value = '创建同步任务'
  currentStep.value = 0
  sourceTables.value = []
  sourceColumns.value = []
  fieldMappings.value = []
  
  Object.assign(formData, {
    id: null,
    name: '',
    description: '',
    source: '',
    sourceType: '',
    target: '',
    targetType: '',
    type: '全量同步',
    selectedTable: '',
    targetTableName: '',
    tables: [],
    where: '',
    splitKey: '',
    writeMode: 'append',
    batchSize: 1024,
    parallelism: 1,
    nullPolicy: 'ignore',
    preSql: '',
    postSql: '',
    scheduleType: 'manual',
    cronExpression: '',
  })
  dialogVisible.value = true
}

// 启动任务
const handleStart = (row: any) => {
  ElMessageBox.confirm(`确定要启动任务"${row.name}"吗？`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning',
  }).then(async () => {
    try {
      await syncApi.execute(row.id)
      await loadSyncTasks()
      ElMessage.success('任务已启动')
    } catch (error: any) {
      // 错误已在request拦截器中处理
    }
  })
}

const handleCreateTargetTable = () => {
  ElMessage.success('已根据源表结构生成目标建表语句（演示）')
}

const handlePreviewDDL = () => {
  ElMessage.info('预览建表语句（演示）')
}

const handleRun = async () => {
  await handleSubmit()
  if (formData.id) {
    currentTaskId.value = formData.id
    await syncApi.execute(formData.id)
    ElMessage.success('已触发运行')
    // 开始轮询日志
    startLogPolling(formData.id)
  }
}

// 开始轮询日志
const startLogPolling = (taskId: number) => {
  // 清除旧的轮询
  stopLogPolling()
  
  // 立即加载一次日志
  loadTaskLogs(taskId)
  
  // 每3秒轮询一次
  logPollingTimer.value = setInterval(() => {
    loadTaskLogs(taskId)
  }, 3000)
}

// 停止轮询日志
const stopLogPolling = () => {
  if (logPollingTimer.value) {
    clearInterval(logPollingTimer.value)
    logPollingTimer.value = null
  }
}

// 加载任务日志
const loadTaskLogs = async (taskId: number) => {
  try {
    const logs = await syncApi.getLogs(taskId)
    syncLogs.value = logs
    
    // 检查任务状态，如果已完成或失败，停止轮询
    const task = await syncApi.getById(taskId)
    if (task.status === 'success' || task.status === 'failed') {
      stopLogPolling()
      // 刷新任务列表
      await loadSyncTasks()
    }
  } catch (error: any) {
    console.error('加载日志失败:', error)
  }
}

// 暂停任务
const handlePause = (row: any) => {
  ElMessageBox.confirm(`确定要暂停任务"${row.name}"吗？`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning',
  }).then(async () => {
    try {
      await syncApi.stop(row.id)
      await loadSyncTasks()
      ElMessage.success('任务已暂停')
    } catch (error: any) {
      // 错误已在request拦截器中处理
    }
  })
}

// 编辑任务
const handleEdit = (row: any) => {
  dialogTitle.value = '编辑同步任务'
  Object.assign(formData, {
    id: row.id,
    name: row.name,
    source: row.sourceId,
    sourceType: row.sourceType,
    target: row.targetId,
    targetType: row.targetType,
    type: row.syncType || row.type,
    tables: row.tables ? row.tables.split(',') : [],
  })
  dialogVisible.value = true
}

// 查看详情
const handleView = (row: any) => {
  currentTask.value = row
  viewDialogVisible.value = true
}

// 删除
const handleDelete = (row: any) => {
  ElMessageBox.confirm(`确定要删除任务"${row.name}"吗？此操作不可恢复！`, '警告', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'error',
  }).then(async () => {
    try {
      await syncApi.delete(row.id)
      await loadSyncTasks()
      ElMessage.success('删除成功')
    } catch (error: any) {
      // 错误已在request拦截器中处理
    }
  })
}

// 重置表单
const handleReset = () => {
  ElMessageBox.confirm('确定要重置表单吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning',
  }).then(() => {
    // 重置表单数据
    Object.assign(formData, {
      id: null,
      name: '',
      source: '',
      sourceType: '',
      sourceDb: '',
      target: '',
      targetType: '',
      targetDb: '',
      type: '全量同步',
      tables: [],
      selectedTable: '',
      targetTableName: '',
      where: '',
      splitKey: '',
      writeMode: 'append',
      batchSize: 1024,
      parallelism: 1,
      nullPolicy: 'ignore',
      preSql: '',
      postSql: '',
      scheduleType: 'manual',
      cronExpression: '',
    })
    
    // 重置其他相关数据
    sourceTables.value = []
    sourceColumns.value = []
    fieldMappings.value = []
    currentStep.value = 0
    
    ElMessage.success('表单已重置')
  }).catch(() => {
    // 用户取消
  })
}

// 提交表单
const handleSubmit = async () => {
  if (!formRef.value) return
  
  await formRef.value.validate(async (valid) => {
    if (valid) {
      submitLoading.value = true
      try {
        const submitData: any = {
          taskName: formData.name,
          description: formData.description || '',
          sourceDatasourceId: formData.source,
          targetDatasourceId: formData.target,
          sourceTable: formData.selectedTable,
          targetTable: formData.targetTableName || formData.selectedTable,
          syncMode: formData.type === '增量同步' ? 'incremental' : 'full',
          
          // Trino 相关
          sourceCatalog: formData.sourceCatalog || null,
          sourceSchema: formData.sourceDb || null,
          targetCatalog: formData.targetCatalog || null,
          targetSchema: formData.targetDb || null,
          
          // 字段映射
          fieldMappings: JSON.stringify(fieldMappings.value),
          
          // 任务属性
          whereClause: formData.where || null,
          splitKey: formData.splitKey || null,
          writeMode: formData.writeMode || 'append',
          batchSize: formData.batchSize || 1024,
          parallelism: formData.parallelism || 1,
          nullPolicy: formData.nullPolicy || 'ignore',
          preSql: formData.preSql || null,
          postSql: formData.postSql || null,
          
          // 调度配置
          scheduleType: formData.scheduleType || 'manual',
          cronExpression: formData.cronExpression || null,
        }

        if (formData.id) {
          await syncApi.update(formData.id, submitData)
          ElMessage.success('更新成功')
        } else {
          const created = await syncApi.create(submitData)
          formData.id = (created as any)?.id || null
          ElMessage.success('创建成功')
        }
        
        await loadSyncTasks()
        currentStep.value = Math.min(currentStep.value + 1, 4)
      } catch (error: any) {
      
      } finally {
        submitLoading.value = false
      }
    }
  })
}

const nextStep = () => {
  if (currentStep.value < 4) currentStep.value++
}

const prevStep = () => {
  if (currentStep.value > 0) currentStep.value--
}
</script>

<style scoped lang="scss">
// Arco Design 风格 - 现代化页面容器
.wedata-sync-page {
  background: #f7f8fa;
  min-height: 100vh;
  padding: 16px;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'PingFang SC', 'Hiragino Sans GB', sans-serif;
}

// Arco Design 风格 - WeData 顶部工具栏
.wedata-toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 10px 0;
  background: transparent;
  border-radius: 0;
  box-shadow: none;
  margin-bottom: 16px;
  border: none;
  border-bottom: 1px solid #e5e6eb;

  .toolbar-left {
    flex: 1;
  }

  .toolbar-right {
    display: flex;
    gap: 12px;
  }

  .wedata-tabs {
    display: flex;
    gap: 8px;
  }

  .wedata-tab {
    display: flex;
    align-items: center;
    gap: 6px;
    padding: 6px 12px;
    background: #f7f8fa;
    border: 1px solid transparent;
    border-radius: 6px;
    cursor: pointer;
    transition: all 0.2s cubic-bezier(0.34, 0.69, 0.1, 1);
    color: #4e5969;
    font-weight: 500;

    &:hover {
      background: #e8f3ff;
      color: #165dff;
    }

    &.active {
      background: #e8f3ff;
      border-color: #165dff;
      color: #165dff;
      box-shadow: 0 2px 4px 0 rgba(22, 93, 255, 0.15);

      .step-number {
        background: #165dff;
        color: #fff;
        font-weight: 600;
      }
    }

    .step-number {
      display: flex;
      align-items: center;
      justify-content: center;
      width: 20px;
      height: 20px;
      background: #d1d5db;
      color: #fff;
      border-radius: 50%;
      font-size: 11px;
      font-weight: 600;
      transition: all 0.2s;
    }

    .step-name {
      font-size: 13px;
      white-space: nowrap;
    }
  }
}

// Arco Design 风格 - 主内容区
.wedata-content {
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 1px 4px 0 rgba(0, 0, 0, 0.06);
  border: 1px solid #e5e6eb;
  min-height: calc(100vh - 200px);
}

// 步骤面板
.step-panel {
  padding: 24px;
  animation: fadeIn 0.3s ease-in-out;

  .step-panel-header {
    margin-bottom: 24px;
    padding-bottom: 16px;
    border-bottom: 1px solid #e5e6eb;

    .step-title {
      display: flex;
      align-items: center;
      gap: 10px;
      font-size: 18px;
      font-weight: 600;
      color: #1d2129;
      margin-bottom: 8px;
    }

    .step-desc {
      font-size: 14px;
      color: #86909c;
      padding-left: 34px;
    }
  }
}

@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

// Arco Design 风格 - 数据源分栏布局
.datasource-layout {
  display: flex;
  gap: 20px;
  align-items: flex-start;

  .datasource-column {
    flex: 1;
    background: #f7f8fa;
    border-radius: 8px;
    border: 1px solid #e5e6eb;
    overflow: hidden;
    transition: all 0.3s cubic-bezier(0.34, 0.69, 0.1, 1);

    &:hover {
      border-color: #165dff;
      box-shadow: 0 4px 10px rgba(22, 93, 255, 0.1);
    }

    &.source-column .column-header {
      background: linear-gradient(135deg, #e8f3ff 0%, #d4e8ff 100%);
    }

    &.target-column .column-header {
      background: linear-gradient(135deg, #e8fff0 0%, #d4f5e0 100%);
    }
  }

  .column-header {
    display: flex;
    align-items: center;
    gap: 12px;
    padding: 16px 20px;
    border-bottom: 1px solid #e5e6eb;

    .header-icon {
      display: flex;
      align-items: center;
      justify-content: center;
      width: 40px;
      height: 40px;
      background: #fff;
      border-radius: 8px;
      box-shadow: 0 2px 4px rgba(0, 0, 0, 0.06);
    }

    .header-content {
      .header-title {
        font-size: 16px;
        font-weight: 600;
        color: #1d2129;
        margin-bottom: 4px;
      }

      .header-subtitle {
        font-size: 12px;
        color: #86909c;
        text-transform: uppercase;
        letter-spacing: 0.5px;
      }
    }
  }

  .column-body {
    padding: 20px;
    background: #fff;

    :deep(.el-form-item) {
      margin-bottom: 18px;

      .el-form-item__label {
        font-weight: 500;
        color: #4e5969;
      }
    }

    :deep(.el-input__wrapper) {
      border-radius: 6px;
      transition: all 0.2s;

      &:hover {
        border-color: #94b8ff;
      }

      &.is-focus {
        border-color: #165dff;
        box-shadow: 0 0 0 2px rgba(22, 93, 255, 0.1);
      }
    }

    :deep(.el-select) {
      .el-input__wrapper {
        border-radius: 6px;
      }
    }
  }

  .datasource-divider {
    display: flex;
    align-items: center;
    justify-content: center;
    width: 48px;
    height: 48px;
    background: #fff;
    border: 1px solid #e5e6eb;
    border-radius: 50%;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
    flex-shrink: 0;
    margin-top: 80px;
  }
}

// 基本信息区域
.basic-info-section {
  margin-bottom: 24px;
  padding: 20px;
  background: #f7f8fa;
  border-radius: 8px;
  border: 1px solid #e5e6eb;

  :deep(.el-form-item) {
    margin-bottom: 0;
  }
}

// 字段映射区域
.field-mapping-section {
  margin-top: 24px;
  border-radius: 8px;
  border: 1px solid #e5e6eb;
  overflow: hidden;
  animation: slideDown 0.3s ease-out;

  .mapping-section-header {
    padding: 16px 20px;
    background: linear-gradient(135deg, #e8f3ff 0%, #f0f7ff 100%);
    border-bottom: 1px solid #e5e6eb;

    .section-title {
      display: flex;
      align-items: center;
      gap: 10px;
      font-size: 16px;
      font-weight: 600;
      color: #1d2129;
      margin-bottom: 6px;
    }

    .section-desc {
      font-size: 13px;
      color: #86909c;
      padding-left: 28px;
    }
  }
}

@keyframes slideDown {
  from {
    opacity: 0;
    max-height: 0;
  }
  to {
    opacity: 1;
    max-height: 800px;
  }
}

// Arco Design 风格 - 搜索栏
.search-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;

  :deep(.el-input__wrapper) {
    border-radius: 6px;
    box-shadow: 0 1px 2px rgba(0, 0, 0, 0.05);
  }

  .filter-group {
    display: flex;
    gap: 12px;
    align-items: center;
  }
}

// Arco Design 风格 - 表格卡片
.table-card {
  background: #fff;
  border-radius: 8px;
  padding: 0;
  box-shadow: 0 1px 4px 0 rgba(0, 0, 0, 0.06);
  border: 1px solid #e5e6eb;
  overflow: hidden;

  :deep(.el-table) {
    border-radius: 8px;

    th.el-table__cell {
      background: #f7f8fa;
      color: #4e5969;
      font-weight: 600;
      font-size: 13px;
      border-bottom: 1px solid #e5e6eb;
    }

    tr {
      transition: all 0.2s;

      &:hover {
        background: #f7f8fa;
      }
    }

    td.el-table__cell {
      border-bottom: 1px solid #f2f3f5;
    }
  }

  .name-cell {
    display: flex;
    align-items: center;
    gap: 8px;

    .task-name {
      font-weight: 500;
      color: #1d2129;
    }
  }

  .db-info {
    display: flex;
    align-items: center;
    gap: 8px;

    .db-name {
      color: #4e5969;
      font-size: 13px;
    }

    :deep(.el-tag) {
      font-size: 12px;
      padding: 0 8px;
      height: 22px;
      line-height: 20px;
      border-radius: 4px;
    }
  }

  .progress-cell {
    padding: 4px 0;
  }

  .action-buttons {
    display: flex;
    gap: 8px;
    flex-wrap: wrap;
    justify-content: flex-end;

    :deep(.el-button) {
      padding: 5px 12px;
      font-size: 13px;
      border-radius: 4px;
      transition: all 0.2s;

      &.el-button--primary:hover {
        background: #3c71ff;
      }

      &.el-button--success:hover {
        background: #00c975;
      }
    }
  }
}

// 无边框胶囊删除按钮
.btn-text-danger {
  background: transparent !important;
  color: #ff4d4f !important;
  border: none !important;
  padding: 0 8px !important;
  height: auto !important;
  box-shadow: none !important;
}

.btn-text-danger:hover {
  color: #ff7875 !important;
  background: transparent !important;
}

.btn-text-danger.is-disabled,
.btn-text-danger.is-disabled:hover {
  color: #ffb3b3 !important;
  cursor: not-allowed;
}

:deep(.btn-text-danger .el-icon) {
  color: inherit !important;
  margin-right: 4px;
}

// 状态徽章
.status-badge {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 4px 12px;
  border-radius: 4px;
  font-size: 12px;

  .status-dot {
    width: 6px;
    height: 6px;
    border-radius: 50%;
  }

  &.status-running {
    background: #e6f0ff;
    color: #0052d9;

    .status-dot {
      background: #0052d9;
      animation: pulse 2s infinite;
    }
  }

  &.status-success {
    background: #e6fff9;
    color: #00b96b;

    .status-dot {
      background: #00b96b;
    }
  }

  &.status-warning {
    background: #fff7e6;
    color: #ff9800;

    .status-dot {
      background: #ff9800;
    }
  }

  &.status-error {
    background: #fff1f0;
    color: #ff4d4f;

    .status-dot {
      background: #ff4d4f;
    }
  }
}

.pagination-container {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}

.log-container {
  max-height: 300px;
  overflow-y: auto;
  background: #f5f5f5;
  border-radius: 4px;
  padding: 12px;

  .log-item {
    display: flex;
    gap: 12px;
    padding: 6px 0;
    font-size: 13px;
    font-family: 'Monaco', 'Menlo', 'Consolas', monospace;

    .log-time {
      color: #999;
      white-space: nowrap;
    }

    .log-level {
      font-weight: 600;
      white-space: nowrap;

      &.log-INFO {
        color: #0052d9;
      }

      &.log-SUCCESS {
        color: #00b96b;
      }

      &.log-ERROR {
        color: #ff4d4f;
      }

      &.log-WARN {
        color: #ff9800;
      }
    }

    .log-message {
      color: #333;
    }
  }
}

@keyframes pulse {
  0%, 100% {
    opacity: 1;
  }
  50% {
    opacity: 0.5;
  }
}

// 美化版弹窗样式
.sync-task-dialog-enhanced {
  :deep(.el-dialog__header) {
    padding: 24px 24px 16px;
    border-bottom: 1px solid #f0f0f0;
    
    .el-dialog__title {
      font-size: 18px;
      font-weight: 600;
      color: #1f2937;
    }
  }

  :deep(.el-dialog__body) {
    padding: 24px;
  }

  :deep(.el-dialog__footer) {
    padding: 16px 24px;
    border-top: 1px solid #f0f0f0;
    background: #fafbfc;
  }

  // 步骤指示器美化
  .steps-wrapper-enhanced {
    margin-bottom: 32px;
    padding: 24px 0;
    background: #f8f9ff;
    border-radius: 12px;
    border: 1px solid #e8ecf0;

    :deep(.custom-steps-enhanced) {
      .el-step__head {
        .el-step__icon {
          width: 40px;
          height: 40px;
          border: 2px solid #e4e7ed;
          background: #fff;

          .el-step__icon-inner {
            font-size: 18px;
          }
        }

        &.is-process .el-step__icon {
          border-color: #0052d9;
          background: #0052d9;
          color: #fff;
        }

        &.is-finish .el-step__icon {
          border-color: #00b96b;
          background: #00b96b;
          color: #fff;
        }
      }

      .el-step__title {
        font-size: 15px;
        font-weight: 600;
        color: #1f2937;

        &.is-process {
          color: #0052d9;
        }

        &.is-finish {
          color: #00b96b;
        }
      }

      .step-desc {
        font-size: 12px;
        color: #6b7280;
        margin-top: 4px;
      }

      .el-step__line {
        top: 20px;
        border-color: #e4e7ed;
      }

      .el-step__line-inner {
        border-color: #00b96b;
      }
    }
  }

  // 表单内容包装器
  .form-content-wrapper {
    min-height: 400px;
  }

  .enhanced-form {
    :deep(.el-form-item__label) {
      font-weight: 500;
      color: #374151;
      font-size: 14px;
    }
  }

  // 步骤内容卡片
  .step-content-enhanced {
    min-height: 400px;
  }

  .step-card {
    background: #fff;
    border-radius: 12px;
    border: 1px solid #e8ecf0;
    overflow: hidden;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
  }

  .step-header {
    display: flex;
    align-items: center;
    gap: 10px;
    padding: 20px 24px;
    background: #f8f9ff;
    border-bottom: 1px solid #e8ecf0;

    .step-title {
      font-size: 16px;
      font-weight: 600;
      color: #1f2937;
    }
  }

  .step-body {
    padding: 24px;
  }

  // 表单项增强
  .form-item-enhanced {
    margin-bottom: 24px;

    :deep(.el-form-item__label) {
      padding-bottom: 8px;
    }
  }

  .form-tip {
    display: flex;
    align-items: center;
    gap: 6px;
    margin-top: 8px;
    font-size: 12px;
    color: #6b7280;

    .el-icon {
      color: #0052d9;
    }
  }

  // 输入框增强
  .input-enhanced {
    :deep(.el-input__wrapper) {
      border-radius: 8px;
      border: 1px solid #e4e7ed;

      &:hover {
        border-color: #0052d9;
      }

      &.is-focus {
        border-color: #0052d9;
      }
    }

    :deep(.el-input__prefix) {
      color: #9ca3af;
    }
  }

  .textarea-enhanced {
    :deep(.el-textarea__inner) {
      border-radius: 8px;
      border-color: #e4e7ed;

      &:hover {
        border-color: #0052d9;
      }

      &:focus {
        border-color: #0052d9;
      }
    }
  }

  // 数据源配置面板
  .datasource-config-wrapper {
    margin-top: 8px;
  }

  .datasource-panel-enhanced {
    background: #fff;
    border-radius: 12px;
    border: 2px solid #e8ecf0;
    overflow: hidden;
    height: 100%;

    &:hover {
      border-color: #0052d9;
    }

    &.source-panel {
      .panel-icon-wrapper.source-icon {
        background: #e6f0ff;
      }
    }

    &.target-panel {
      .panel-icon-wrapper.target-icon {
        background: #e6fff9;
      }
    }
  }

  .panel-header-enhanced {
    display: flex;
    align-items: center;
    gap: 12px;
    padding: 20px;
    background: #fafbfc;
    border-bottom: 1px solid #e8ecf0;
  }

  .panel-icon-wrapper {
    display: flex;
    align-items: center;
    justify-content: center;
    width: 48px;
    height: 48px;
    border-radius: 10px;
    color: #0052d9;

    &.source-icon {
      background: #e6f0ff;
    }

    &.target-icon {
      background: #e6fff9;
      color: #00b96b;
    }
  }

  .panel-title-wrapper {
    flex: 1;
    display: flex;
    flex-direction: column;
    gap: 4px;
  }

  .panel-title {
    font-size: 16px;
    font-weight: 600;
    color: #1f2937;
  }

  .panel-subtitle {
    font-size: 12px;
    color: #6b7280;
  }

  .panel-body {
    padding: 20px;
  }

  // 选择框增强
  .select-enhanced {
    width: 100%;

    :deep(.el-input__wrapper) {
      border-radius: 8px;
      border: 1px solid #e4e7ed;

      &:hover {
        border-color: #0052d9;
      }

      &.is-focus {
        border-color: #0052d9;
      }
    }
  }

  .option-enhanced {
    .option-content {
      display: flex;
      align-items: center;
      justify-content: space-between;
      width: 100%;
    }

    .option-left {
      display: flex;
      align-items: center;
      gap: 8px;
    }

    .option-name {
      font-weight: 500;
    }
  }

  // 表选择器
  .table-select-wrapper {
    display: flex;
    gap: 12px;
    align-items: flex-start;
  }

  .table-select {
    flex: 1;
    max-width: 500px;
  }

  .refresh-btn {
    height: 40px;
    border-radius: 8px;
  }

  .table-option {
    display: flex;
    align-items: center;
    gap: 8px;
  }

  // 单选框组增强
  .radio-group-enhanced {
    display: flex;
    flex-direction: column;
    gap: 12px;
  }

  .radio-enhanced {
    margin: 0;
    padding: 16px;
    border: 2px solid #e8ecf0;
    border-radius: 10px;
    background: #fafbfc;

    &:hover {
      border-color: #0052d9;
      background: #f0f7ff;
    }

    :deep(.el-radio__input.is-checked) {
      .el-radio__inner {
        border-color: #0052d9;
        background: #0052d9;
      }

      + .el-radio__label {
        color: #0052d9;
      }
    }
  }

  .radio-content {
    display: flex;
    flex-direction: column;
    gap: 4px;
  }

  .radio-title {
    font-size: 14px;
    font-weight: 600;
    color: #1f2937;
  }

  .radio-desc {
    font-size: 12px;
    color: #6b7280;
  }

  // 字段映射容器
  .field-mapping-container-enhanced {
    border: 1px solid #e8ecf0;
    border-radius: 12px;
    overflow: hidden;
    background: #fafbfc;
  }

  .mapping-header {
    display: flex;
    align-items: center;
    gap: 8px;
    padding: 12px 16px;
    background: #f8f9ff;
    border-bottom: 1px solid #e8ecf0;
    font-size: 14px;
    font-weight: 600;
    color: #1f2937;
  }

  .mapping-table {
    background: #fff;

    :deep(.el-table__header) {
      th {
        background: #fafbfc;
        color: #374151;
        font-weight: 600;
        border-bottom: 2px solid #e8ecf0;
      }
    }

    :deep(.el-table__body) {
      tr:hover {
        background: #f8f9ff;
      }
    }
  }

  .field-cell {
    display: flex;
    align-items: center;
    gap: 8px;
  }

  .field-name {
    font-weight: 500;
    color: #1f2937;
  }

  .mapping-arrow {
    display: flex;
    justify-content: center;
    align-items: center;
  }

  .field-remark {
    color: #6b7280;
    font-size: 12px;
  }

  // Cron表达式输入
  .cron-input-wrapper {
    display: flex;
    flex-direction: column;
    gap: 12px;
  }

  .cron-input {
    max-width: 600px;
  }

  .cron-examples {
    display: flex;
    align-items: center;
    gap: 8px;
    flex-wrap: wrap;
  }

  .examples-label {
    font-size: 12px;
    color: #6b7280;
  }

  .cron-link {
    font-size: 12px;
  }

  // Switch增强
  .switch-wrapper {
    display: flex;
    align-items: center;
    gap: 12px;
  }

  .switch-label {
    font-size: 14px;
    color: #374151;
  }

  // 重试次数
  .retry-wrapper {
    display: flex;
    align-items: center;
    gap: 12px;
  }

  .retry-label {
    font-size: 14px;
    color: #6b7280;
  }

  // 底部按钮
  .dialog-footer-enhanced {
    display: flex;
    align-items: center;
    gap: 12px;
  }

  .footer-btn {
    padding: 10px 20px;
    border-radius: 8px;
    font-weight: 500;

    &.cancel-btn {
      &:hover {
        background: #f5f5f5;
        border-color: #d9d9d9;
      }
    }

    &.primary-btn {
      background: #0052d9;
      border: none;

      &:hover {
        background: #1a7fff;
      }
    }
  }
}
</style>
