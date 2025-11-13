import { request } from '@/utils/request'
import type { PageResult, PageQuery } from './datasource'

/**
 * SQL查询请求
 */
export interface QueryRequest {
  datasourceId: number
  sqlContent: string
}

/**
 * SQL查询结果
 */
export interface QueryResult {
  columns: string[]
  data: Record<string, any>[]
  rows: number
  duration: number
}

/**
 * 查询历史
 */
export interface QueryHistory {
  id: number
  datasourceId: number
  sqlContent: string
  resultRows: number
  executionTime: number
  status: string
  errorMessage?: string
  createTime: string
}

/**
 * 数据集
 */
export interface Dataset {
  id?: number
  name: string
  datasourceId: number
  sqlContent: string
  description?: string
  rowCount?: number
  columnCount?: number
  lastRefreshTime?: string
  createTime?: string
  updateTime?: string
}

/**
 * 数据中枢API
 */
export const datacapApi = {
  /**
   * 执行SQL查询
   */
  executeQuery(data: QueryRequest) {
    return request.post<QueryResult>('/datacap/query', data)
  },

  /**
   * 分页查询查询历史
   */
  queryHistoryPage(params: PageQuery) {
    return request.get<PageResult<QueryHistory>>('/datacap/history/page', { params })
  },

  /**
   * 删除查询历史
   */
  deleteQueryHistory(id: number) {
    return request.delete(`/datacap/history/${id}`)
  },

  /**
   * 分页查询数据集
   */
  datasetPage(params: PageQuery) {
    return request.get<PageResult<Dataset>>('/datacap/dataset/page', { params })
  },

  /**
   * 根据ID查询数据集
   */
  getDatasetById(id: number) {
    return request.get<Dataset>(`/datacap/dataset/${id}`)
  },

  /**
   * 创建数据集
   */
  createDataset(data: Dataset) {
    return request.post<Dataset>('/datacap/dataset', data)
  },

  /**
   * 更新数据集
   */
  updateDataset(id: number, data: Dataset) {
    return request.put<Dataset>(`/datacap/dataset/${id}`, data)
  },

  /**
   * 删除数据集
   */
  deleteDataset(id: number) {
    return request.delete(`/datacap/dataset/${id}`)
  },

  /**
   * 刷新数据集
   */
  refreshDataset(id: number) {
    return request.post<QueryResult>(`/datacap/dataset/${id}/refresh`)
  },
}

