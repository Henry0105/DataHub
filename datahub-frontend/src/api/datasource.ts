import { request } from '@/utils/request'

/**
 * 数据源类型
 */
export interface Datasource {
  id?: number
  name: string
  type: string
  host: string
  port: number
  databaseName: string
  username: string
  password: string
  description?: string
  status?: number
  createTime?: string
  updateTime?: string
}

/**
 * 分页结果
 */
export interface PageResult<T> {
  records: T[]
  total: number
  current: number
  size: number
  pages: number
}

/**
 * 分页查询参数
 */
export interface PageQuery {
  current?: number
  size?: number
  keyword?: string
}

/**
 * 数据源管理API
 */
export const datasourceApi = {
  /**
   * 分页查询数据源
   */
  page(params: PageQuery) {
    return request.get<PageResult<Datasource>>('/datasource/page', { params })
  },

  /**
   * 查询所有数据源
   */
  list() {
    return request.get<Datasource[]>('/datasource/list')
  },

  /**
   * 根据ID查询数据源
   */
  getById(id: number) {
    return request.get<Datasource>(`/datasource/${id}`)
  },

  /**
   * 创建数据源
   */
  create(data: Datasource) {
    return request.post<Datasource>('/datasource', data)
  },

  /**
   * 更新数据源
   */
  update(id: number, data: Datasource) {
    return request.put<Datasource>(`/datasource/${id}`, data)
  },

  /**
   * 删除数据源
   */
  delete(id: number) {
    return request.delete(`/datasource/${id}`)
  },

  /**
   * 测试数据源连接（已存在的数据源）
   */
  testConnection(id: number) {
    return request.post<Datasource>(`/datasource/${id}/test`)
  },

  /**
   * 测试数据源连接（新建时测试）
   */
  testConnectionByConfig(data: Datasource) {
    return request.post<string>('/datasource/test', data)
  },

  /**
   * 获取数据源的所有库（Catalog）
   */
  getDatabases(id: number) {
    return request.get<string[]>(`/datasource/${id}/databases`)
  },

  /**
   * 获取数据源的所有Schema（对Trino需要传catalog参数）
   */
  getSchemas(id: number, params?: { catalog?: string }) {
    return request.get<string[]>(`/datasource/${id}/schemas`, { params })
  },

  /**
   * 获取数据源的所有表
   */
  getTables(id: number, database?: string) {
    const params = database ? { database } : {}
    return request.get<string[]>(`/datasource/${id}/tables`, { params })
  },

  /**
   * 获取表的字段信息
   */
  getTableColumns(id: number, tableName: string, database?: string) {
    const params = database ? { database } : {}
    return request.get<any[]>(`/datasource/${id}/tables/${tableName}/columns`, { params })
  },
}

