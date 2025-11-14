import request from '@/utils/request'

// ==================== 服务器管理 ====================

/**
 * 获取服务器列表
 */
export function getServers() {
  return request({
    url: '/ops/servers',
    method: 'get'
  })
}

/**
 * 获取服务器详情
 */
export function getServerDetail(id: number) {
  return request({
    url: `/ops/servers/${id}`,
    method: 'get'
  })
}

// ==================== 组件管理 ====================

/**
 * 获取组件列表
 */
export function getComponents() {
  return request({
    url: '/ops/components',
    method: 'get'
  })
}

/**
 * 启动组件
 */
export function startComponent(id: number) {
  return request({
    url: `/ops/components/${id}/start`,
    method: 'post'
  })
}

/**
 * 停止组件
 */
export function stopComponent(id: number) {
  return request({
    url: `/ops/components/${id}/stop`,
    method: 'post'
  })
}

/**
 * 查看组件日志
 */
export function getComponentLogs(id: number) {
  return request({
    url: `/ops/components/${id}/logs`,
    method: 'get'
  })
}
