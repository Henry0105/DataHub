package com.datahub.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.datahub.common.Result;
import com.datahub.dto.DashboardStats;
import com.datahub.entity.Dataset;
import com.datahub.entity.Datasource;
import com.datahub.entity.QueryHistory;
import com.datahub.entity.SyncTask;
import com.datahub.mapper.DatasetMapper;
import com.datahub.mapper.DatasourceMapper;
import com.datahub.mapper.QueryHistoryMapper;
import com.datahub.mapper.SyncTaskMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * 首页看板控制器
 *
 * @author DataHub Team
 */
@RestController
@RequestMapping("/dashboard")
public class DashboardController {

    @Autowired
    private DatasourceMapper datasourceMapper;

    @Autowired
    private SyncTaskMapper syncTaskMapper;

    @Autowired
    private QueryHistoryMapper queryHistoryMapper;

    @Autowired
    private DatasetMapper datasetMapper;

    /**
     * 获取统计数据
     */
    @GetMapping("/stats")
    public Result<DashboardStats> getStats() {
        DashboardStats stats = new DashboardStats();

        // 数据源总数
        stats.setDatasourceCount(datasourceMapper.selectCount(null).intValue());

        // 同步任务总数
        stats.setSyncTaskCount(syncTaskMapper.selectCount(null).intValue());

        // 今日查询次数
        LocalDateTime todayStart = LocalDateTime.of(LocalDate.now(), LocalTime.MIN);
        LambdaQueryWrapper<QueryHistory> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.ge(QueryHistory::getCreateTime, todayStart);
        stats.setQueryCount(queryHistoryMapper.selectCount(queryWrapper).intValue());

        // 数据集总数
        stats.setDatasetCount(datasetMapper.selectCount(null).intValue());

        // 运行中的任务数
        LambdaQueryWrapper<SyncTask> runningWrapper = new LambdaQueryWrapper<>();
        runningWrapper.eq(SyncTask::getStatus, "running");
        stats.setRunningTaskCount(syncTaskMapper.selectCount(runningWrapper).intValue());

        // 今日成功任务数
        LambdaQueryWrapper<SyncTask> successWrapper = new LambdaQueryWrapper<>();
        successWrapper.eq(SyncTask::getStatus, "success");
        successWrapper.ge(SyncTask::getLastSyncTime, todayStart);
        stats.setSuccessTaskCount(syncTaskMapper.selectCount(successWrapper).intValue());

        return Result.success(stats);
    }
}

