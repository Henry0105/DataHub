<template>
  <div class="page-container-tdesign">
    <div class="page-header">
      <div class="page-title">
        <el-icon :size="20" color="#0052d9"><Document /></el-icon>
        <span>数据标准</span>
        <span class="page-subtitle">Data Standard</span>
      </div>
      <el-button type="primary" @click="handleCreate">
        <el-icon><Plus /></el-icon>
        创建标准
      </el-button>
    </div>

    <div class="info-banner">
      <div class="info-item">
        <el-icon color="#0052d9"><Check /></el-icon>
        <span>数据标准定义与管理</span>
      </div>
      <div class="info-item">
        <el-icon color="#0052d9"><Check /></el-icon>
        <span>数据质量规则配置</span>
      </div>
      <div class="info-item">
        <el-icon color="#0052d9"><Check /></el-icon>
        <span>标准化数据治理</span>
      </div>
    </div>

    <div class="stats-container">
      <div class="stat-card">
        <div class="stat-icon" style="background: #e6f0ff;">
          <el-icon :size="24" color="#0052d9"><Document /></el-icon>
        </div>
        <div class="stat-content">
          <div class="stat-value">128</div>
          <div class="stat-label">标准数量</div>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon" style="background: #e6fffb;">
          <el-icon :size="24" color="#00b96b"><Check /></el-icon>
        </div>
        <div class="stat-content">
          <div class="stat-value">95</div>
          <div class="stat-label">已发布</div>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon" style="background: #fff7e6;">
          <el-icon :size="24" color="#faad14"><Edit /></el-icon>
        </div>
        <div class="stat-content">
          <div class="stat-value">23</div>
          <div class="stat-label">草稿</div>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon" style="background: #f0f5ff;">
          <el-icon :size="24" color="#597ef7"><FolderOpened /></el-icon>
        </div>
        <div class="stat-content">
          <div class="stat-value">12</div>
          <div class="stat-label">分类</div>
        </div>
      </div>
    </div>

    <div class="table-card">
      <el-tabs v-model="activeTab">
        <el-tab-pane label="字段标准" name="field">
          <el-table :data="fieldData" style="width: 100%">
            <el-table-column prop="name" label="标准名称" min-width="180" />
            <el-table-column prop="code" label="标准编码" width="150" />
            <el-table-column prop="type" label="数据类型" width="120" />
            <el-table-column prop="length" label="长度" width="100" />
            <el-table-column prop="category" label="分类" width="120">
              <template #default="{ row }">
                <el-tag size="small">{{ row.category }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="status" label="状态" width="100">
              <template #default="{ row }">
                <el-tag :type="row.status === '已发布' ? 'success' : 'warning'" size="small">
                  {{ row.status }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="180">
              <template #default="{ row }">
                <el-button type="primary" size="small" link>
                  <el-icon><Edit /></el-icon>
                  编辑
                </el-button>
                <el-button type="primary" size="small" link>
                  <el-icon><View /></el-icon>
                  详情
                </el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>

        <el-tab-pane label="业务术语" name="term">
          <el-table :data="termData" style="width: 100%">
            <el-table-column prop="name" label="术语名称" min-width="180" />
            <el-table-column prop="code" label="术语编码" width="150" />
            <el-table-column prop="definition" label="定义" min-width="250" />
            <el-table-column prop="status" label="状态" width="100">
              <template #default="{ row }">
                <el-tag :type="row.status === '已发布' ? 'success' : 'warning'" size="small">
                  {{ row.status }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="180">
              <template #default="{ row }">
                <el-button type="primary" size="small" link>
                  <el-icon><Edit /></el-icon>
                  编辑
                </el-button>
                <el-button type="primary" size="small" link>
                  <el-icon><View /></el-icon>
                  详情
                </el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>

        <el-tab-pane label="质量规则" name="rule">
          <el-table :data="ruleData" style="width: 100%">
            <el-table-column prop="name" label="规则名称" min-width="180" />
            <el-table-column prop="type" label="规则类型" width="120">
              <template #default="{ row }">
                <el-tag size="small">{{ row.type }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="expression" label="规则表达式" min-width="200" />
            <el-table-column prop="status" label="状态" width="100">
              <template #default="{ row }">
                <el-tag :type="row.status === '启用' ? 'success' : 'info'" size="small">
                  {{ row.status }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="180">
              <template #default="{ row }">
                <el-button type="primary" size="small" link>
                  <el-icon><Edit /></el-icon>
                  编辑
                </el-button>
                <el-button type="primary" size="small" link>
                  <el-icon><VideoPlay /></el-icon>
                  测试
                </el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>
      </el-tabs>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { ElMessage } from 'element-plus'

const activeTab = ref('field')

const fieldData = ref([
  { id: 1, name: '用户ID', code: 'USER_ID', type: 'VARCHAR', length: 32, category: '基础信息', status: '已发布' },
  { id: 2, name: '手机号码', code: 'MOBILE', type: 'VARCHAR', length: 11, category: '联系方式', status: '已发布' },
  { id: 3, name: '身份证号', code: 'ID_CARD', type: 'VARCHAR', length: 18, category: '身份信息', status: '草稿' },
])

const termData = ref([
  { id: 1, name: '活跃用户', code: 'ACTIVE_USER', definition: '最近30天内有登录行为的用户', status: '已发布' },
  { id: 2, name: '订单金额', code: 'ORDER_AMOUNT', definition: '订单中所有商品的总价格', status: '已发布' },
])

const ruleData = ref([
  { id: 1, name: '手机号格式校验', type: '格式校验', expression: '^1[3-9]\\d{9}$', status: '启用' },
  { id: 2, name: '金额范围校验', type: '范围校验', expression: 'amount > 0 AND amount < 1000000', status: '启用' },
])

const handleCreate = () => {
  ElMessage.info('创建标准功能开发中...')
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
  padding: 16px 24px;
  background: linear-gradient(135deg, #e6f0ff 0%, #f0f7ff 100%);
  border-radius: 6px;
  margin-bottom: 16px;

  .info-item {
    display: flex;
    align-items: center;
    gap: 8px;
    font-size: 14px;
    color: #374151;
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
}
</style>
