package com.datahub.service;

import com.datahub.entity.SyncTask;

/**
 * SeaTunnel数据同步服务接口
 *
 * @author DataHub Team
 */
public interface SeaTunnelService {

    /**
     * 提交同步任务到SeaTunnel集群
     *
     * @param syncTask 同步任务
     * @return 任务ID
     */
    String submitJob(SyncTask syncTask);

    /**
     * 取消同步任务
     *
     * @param jobId 任务ID
     */
    void cancelJob(String jobId);

    /**
     * 获取任务状态
     *
     * @param jobId 任务ID
     * @return 任务状态
     */
    String getJobStatus(String jobId);

    /**
     * 获取任务进度
     *
     * @param jobId 任务ID
     * @return 进度(0-100)
     */
    Integer getJobProgress(String jobId);

    /**
     * 检查SeaTunnel集群健康状态
     *
     * @return 是否健康
     */
    boolean isClusterHealthy();

    /**
     * 生成SeaTunnel配置文件
     *
     * @param syncTask 同步任务
     * @return 配置文件内容
     */
    String generateJobConfig(SyncTask syncTask);
}
