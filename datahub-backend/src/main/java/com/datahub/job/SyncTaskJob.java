package com.datahub.job;

import com.datahub.entity.SyncTask;
import com.datahub.service.SeaTunnelService;
import com.datahub.service.SyncTaskService;
import lombok.extern.slf4j.Slf4j;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 数据同步定时任务
 *
 * @author DataHub Team
 */
@Slf4j
@Component
public class SyncTaskJob implements Job {

    @Autowired
    private SyncTaskService syncTaskService;

    @Autowired
    private SeaTunnelService seaTunnelService;

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        Long taskId = context.getJobDetail().getJobDataMap().getLong("taskId");
        
        try {
            log.info("开始执行定时同步任务, TaskID: {}", taskId);
            
            // 执行同步任务
            syncTaskService.execute(taskId);
            
            log.info("定时同步任务执行完成, TaskID: {}", taskId);
            
        } catch (Exception e) {
            log.error("定时同步任务执行失败, TaskID: {}", taskId, e);
            throw new JobExecutionException(e);
        }
    }
}
