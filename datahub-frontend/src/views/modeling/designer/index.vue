<template>
  <div class="lakehouse-config">
    <div class="page-header">
      <h2>湖仓建模配置</h2>
      <p>配置数据源、分层架构和模型规范</p>
    </div>

    <div class="config-wrapper">
      <!-- 步骤1: 数据源配置 -->
      <el-card class="step-card">
        <template #header>
          <div class="card-title">
            <span class="step-num">1</span>
            <span class="step-name">数据源配置</span>
          </div>
        </template>
        
        <el-form label-width="120px">
          <el-form-item label="数据湖引擎">
            <el-select v-model="config.engine" placeholder="选择引擎" style="width: 300px">
              <el-option label="Apache Doris" value="doris" />
              <el-option label="StarRocks" value="starrocks" />
              <el-option label="Apache Hive" value="hive" />
              <el-option label="Trino" value="trino" />
              <el-option label="Apache Iceberg" value="iceberg" />
            </el-select>
          </el-form-item>
          
          <el-form-item label="存储位置">
            <el-select v-model="config.storage" placeholder="选择存储" style="width: 300px">
              <el-option label="UCloud US3" value="us3" />
              <el-option label="AWS S3" value="s3" />
              <el-option label="阿里云 OSS" value="oss" />
              <el-option label="腾讯云 COS" value="cos" />
              <el-option label="HDFS" value="hdfs" />
            </el-select>
          </el-form-item>
          
          <el-form-item label="连接地址">
            <el-input v-model="config.endpoint" placeholder="jdbc:mysql://localhost:9030" style="width: 500px" />
          </el-form-item>
          
          <el-form-item label="存储路径">
            <el-input v-model="config.storagePath" placeholder="s3://bucket/warehouse/" style="width: 500px" />
          </el-form-item>
        </el-form>
      </el-card>

      <!-- 步骤2: 仓湖分层 -->
      <el-card class="step-card">
        <template #header>
          <div class="card-title">
            <span class="step-num">2</span>
            <span class="step-name">仓湖分层配置</span>
            <el-button type="primary" size="small" style="margin-left: auto" @click="createDatabases">
              创建数据库
            </el-button>
          </div>
        </template>
        
        <div class="layers-grid">
          <div v-for="layer in layers" :key="layer.code" class="layer-box" :class="{ active: layer.enabled }">
            <el-checkbox v-model="layer.enabled">
              <div class="layer-info">
                <div class="layer-header">
                  <span class="layer-name">{{ layer.name }}</span>
                  <el-tag size="small">{{ layer.code }}</el-tag>
                </div>
                <div class="layer-desc">{{ layer.description }}</div>
              </div>
            </el-checkbox>
            
            <div v-if="layer.enabled" class="layer-form">
              <el-input v-model="layer.database" size="small" placeholder="数据库名" />
              <el-input v-model="layer.prefix" size="small" placeholder="表前缀" style="margin-top: 8px">
                <template #append>table_name</template>
              </el-input>
            </div>
          </div>
        </div>
      </el-card>

      <!-- 步骤3: 模型规范 -->
      <el-card class="step-card">
        <template #header>
          <div class="card-title">
            <span class="step-num">3</span>
            <span class="step-name">数据模型规范</span>
          </div>
        </template>
        
        <el-tabs v-model="activeTab">
          <el-tab-pane label="命名规范" name="naming">
            <el-form label-width="140px">
              <el-form-item label="表名命名规则">
                <el-radio-group v-model="standards.tableNaming">
                  <el-radio value="snake">下划线(user_info)</el-radio>
                  <el-radio value="camel">驼峰(userInfo)</el-radio>
                </el-radio-group>
              </el-form-item>
              <el-form-item label="字段名命名规则">
                <el-radio-group v-model="standards.columnNaming">
                  <el-radio value="snake">下划线</el-radio>
                  <el-radio value="camel">驼峰</el-radio>
                </el-radio-group>
              </el-form-item>
            </el-form>
          </el-tab-pane>
          
          <el-tab-pane label="数据类型" name="datatype">
            <el-form label-width="140px">
              <el-form-item label="时间类型">
                <el-select v-model="standards.timestampType" style="width: 200px">
                  <el-option label="TIMESTAMP" value="timestamp" />
                  <el-option label="DATETIME" value="datetime" />
                  <el-option label="BIGINT" value="bigint" />
                </el-select>
              </el-form-item>
              <el-form-item label="金额类型">
                <el-select v-model="standards.decimalType" style="width: 200px">
                  <el-option label="DECIMAL(18,2)" value="decimal18" />
                  <el-option label="DECIMAL(10,2)" value="decimal10" />
                  <el-option label="BIGINT(分)" value="bigint" />
                </el-select>
              </el-form-item>
            </el-form>
          </el-tab-pane>
          
          <el-tab-pane label="分区策略" name="partition">
            <el-form label-width="140px">
              <el-form-item label="分区字段">
                <el-input v-model="standards.partitionColumn" placeholder="dt" style="width: 200px" />
              </el-form-item>
              <el-form-item label="分区粒度">
                <el-radio-group v-model="standards.partitionGranularity">
                  <el-radio value="day">按天</el-radio>
                  <el-radio value="month">按月</el-radio>
                </el-radio-group>
              </el-form-item>
              <el-form-item label="保留天数">
                <el-input-number v-model="standards.retentionDays" :min="7" :max="3650" />
                <span style="margin-left: 8px">天</span>
              </el-form-item>
            </el-form>
          </el-tab-pane>
        </el-tabs>
      </el-card>

      <div class="action-bar">
        <el-button size="large">重置</el-button>
        <el-button type="primary" size="large" @click="applyConfig">
          <el-icon><Check /></el-icon>
          应用配置
        </el-button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { ElMessage } from 'element-plus'
import { Check } from '@element-plus/icons-vue'

const activeTab = ref('naming')

const config = ref({
  engine: 'doris',
  storage: 'us3',
  endpoint: '',
  storagePath: '',
})

const layers = ref([
  {
    code: 'ODS',
    name: 'ODS层',
    description: 'Operation Data Store - 原始数据层',
    enabled: true,
    database: 'ods',
    prefix: 'ods_',
  },
  {
    code: 'DWD',
    name: 'DWD层',
    description: 'Data Warehouse Detail - 明细数据层',
    enabled: true,
    database: 'dwd',
    prefix: 'dwd_',
  },
  {
    code: 'DWS',
    name: 'DWS层',
    description: 'Data Warehouse Summary - 汇总数据层',
    enabled: true,
    database: 'dws',
    prefix: 'dws_',
  },
  {
    code: 'ADS',
    name: 'ADS层',
    description: 'Application Data Store - 应用数据层',
    enabled: true,
    database: 'ads',
    prefix: 'ads_',
  },
])

const standards = ref({
  tableNaming: 'snake',
  columnNaming: 'snake',
  timestampType: 'timestamp',
  decimalType: 'decimal18',
  partitionColumn: 'dt',
  partitionGranularity: 'day',
  retentionDays: 365,
})

const createDatabases = () => {
  const enabledLayers = layers.value.filter((l) => l.enabled)
  if (enabledLayers.length === 0) {
    ElMessage.warning('请至少启用一个分层')
    return
  }
  
  const dbNames = enabledLayers.map((l) => l.database).join(', ')
  ElMessage.success(`将创建数据库: ${dbNames}`)
}

const applyConfig = () => {
  ElMessage.success('配置已应用')
}
</script>

<style scoped lang="scss">
.lakehouse-config {
  height: 100%;
  display: flex;
  flex-direction: column;
  background: #f5f7fa;
}

.page-header {
  padding: 24px;
  background: #fff;
  border-bottom: 1px solid #e5e6eb;

  h2 {
    margin: 0 0 8px 0;
    font-size: 20px;
    font-weight: 600;
  }

  p {
    margin: 0;
    font-size: 14px;
    color: #86909c;
  }
}

.config-wrapper {
  flex: 1;
  padding: 16px;
  overflow: auto;
}

.step-card {
  margin-bottom: 16px;
}

.card-title {
  display: flex;
  align-items: center;
  font-size: 16px;
  font-weight: 600;

  .step-num {
    display: flex;
    align-items: center;
    justify-content: center;
    width: 28px;
    height: 28px;
    background: #409eff;
    color: #fff;
    border-radius: 50%;
    margin-right: 12px;
    font-size: 14px;
  }
}

.layers-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 16px;
}

.layer-box {
  padding: 16px;
  border: 2px solid #e5e6eb;
  border-radius: 8px;
  transition: all 0.3s;

  &.active {
    border-color: #409eff;
    background: #f0f7ff;
  }
}

.layer-info {
  .layer-header {
    display: flex;
    align-items: center;
    gap: 8px;
    margin-bottom: 4px;

    .layer-name {
      font-weight: 500;
      font-size: 15px;
    }
  }

  .layer-desc {
    font-size: 12px;
    color: #86909c;
  }
}

.layer-form {
  margin-top: 12px;
  padding-top: 12px;
  border-top: 1px solid #e5e6eb;
}

.action-bar {
  display: flex;
  justify-content: center;
  gap: 16px;
  padding: 24px;
  background: #fff;
  border-radius: 8px;
  margin-top: 16px;
}
</style>
