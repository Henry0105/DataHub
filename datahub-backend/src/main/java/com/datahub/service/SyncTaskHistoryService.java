package com.datahub.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.datahub.entity.SyncTaskHistory;

/**
 * 同步任务历史服务接口
 *
 * @author DataHub Team
 */
public interface SyncTaskHistoryService {

    /**
     * 分页查询历史记录
     */
    Page<SyncTaskHistory> page(Integer current, Integer size, String keyword, Long taskId);

    /**
     * 根据ID查询
     */
    SyncTaskHistory getById(Long id);

    /**
     * 创建历史记录
     */
    SyncTaskHistory create(SyncTaskHistory history);

    /**
     * 删除历史记录
     */
    void delete(Long id);
}
