package com.datahub.dto;

import lombok.Data;

/**
 * 首页统计数据
 *
 * @author DataHub Team
 */
@Data
public class DashboardStats {

    /**
     * 数据源总数
     */
    private Integer datasourceCount;

    /**
     * 同步任务总数
     */
    private Integer syncTaskCount;

    /**
     * 今日查询次数
     */
    private Integer queryCount;

    /**
     * 数据集总数
     */
    private Integer datasetCount;

    /**
     * 运行中的任务数
     */
    private Integer runningTaskCount;

    /**
     * 今日成功任务数
     */
    private Integer successTaskCount;
}

