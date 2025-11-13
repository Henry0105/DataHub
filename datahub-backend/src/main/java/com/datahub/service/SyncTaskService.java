package com.datahub.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.datahub.entity.SyncTask;

/**
 * 数据库同步任务服务接口
 *
 * @author DataHub Team
 */
public interface SyncTaskService {

    /**
     * 分页查询同步任务
     */
    Page<SyncTask> page(Integer current, Integer size, String keyword);

    /**
     * 根据ID查询同步任务
     */
    SyncTask getById(Long id);

    /**
     * 创建同步任务
     */
    SyncTask create(SyncTask syncTask);

    /**
     * 更新同步任务
     */
    SyncTask update(SyncTask syncTask);

    /**
     * 删除同步任务
     */
    void delete(Long id);

    /**
     * 执行同步任务
     */
    void execute(Long id);

    /**
     * 停止同步任务
     */
    void stop(Long id);

    /**
     * 获取任务执行日志
     */
    java.util.List<String> getLogs(Long id);
}

