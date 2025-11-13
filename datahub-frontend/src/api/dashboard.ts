import { request } from '@/utils/request'

/**
 * 统计数据
 */
export interface DashboardStats {
  datasourceCount: number
  syncTaskCount: number
  queryCount: number
  datasetCount: number
  runningTaskCount: number
  successTaskCount: number
}

/**
 * 首页看板API
 */
export const dashboardApi = {
  /**
   * 获取统计数据
   */
  getStats() {
    return request.get<DashboardStats>('/dashboard/stats')
  },
}

