import { request } from '@/utils/request'
import type { PageResult, PageQuery } from './datasource'

/**
 * 同步任务
 */
export interface SyncTask {
  id?: number
  taskName: string
  description?: string
  sourceDatasourceId: number
  targetDatasourceId: number
  sourceTable: string
  targetTable: string
  
  // Trino 相关
  sourceCatalog?: string
  sourceSchema?: string
  targetCatalog?: string
  targetSchema?: string
  
  // 字段映射
  fieldMappings?: string
  
  // 任务属性
  whereClause?: string
  splitKey?: string
  writeMode?: string
  batchSize?: number
  parallelism?: number
  nullPolicy?: string
  preSql?: string
  postSql?: string
  
  // 调度配置
  scheduleType?: string
  cronExpression?: string
  
  // 任务状态
  syncMode?: string
  status?: string
  progress?: number
  totalRows?: number
  syncedRows?: number
  errorMessage?: string
  lastSyncTime?: string
  createTime?: string
  updateTime?: string
}

/**
 * 数据库同步API
 */
export const syncApi = {
  /**
   * 分页查询同步任务
   */
  page(params: PageQuery) {
    return request.get<PageResult<SyncTask>>('/sync/page', { params })
  },

  /**
   * 根据ID查询同步任务
   */
  getById(id: number) {
    return request.get<SyncTask>(`/sync/${id}`)
  },

  /**
   * 创建同步任务
   */
  create(data: SyncTask) {
    return request.post<SyncTask>('/sync', data)
  },

  /**
   * 更新同步任务
   */
  update(id: number, data: SyncTask) {
    return request.put<SyncTask>(`/sync/${id}`, data)
  },

  /**
   * 删除同步任务
   */
  delete(id: number) {
    return request.delete(`/sync/${id}`)
  },

  /**
   * 执行同步任务
   */
  execute(id: number) {
    return request.post(`/sync/${id}/execute`)
  },

  /**
   * 停止同步任务
   */
  stop(id: number) {
    return request.post(`/sync/${id}/stop`)
  },

  /**
   * 获取任务执行日志
   */
  getLogs(id: number) {
    return request.get<string[]>(`/sync/${id}/logs`)
  },
}

