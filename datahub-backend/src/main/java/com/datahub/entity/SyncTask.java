package com.datahub.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 数据库同步任务实体
 *
 * @author DataHub Team
 */
@Data
@TableName("sync_task")
public class SyncTask implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 任务ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 任务名称
     */
    private String taskName;

    /**
     * 源数据源ID
     */
    private Long sourceDatasourceId;

    /**
     * 目标数据源ID
     */
    private Long targetDatasourceId;

    /**
     * 源表名
     */
    private String sourceTable;

    /**
     * 目标表名
     */
    private String targetTable;

    /**
     * 任务描述
     */
    private String description;

    /**
     * 源Catalog（针对Trino）
     */
    private String sourceCatalog;

    /**
     * 源Schema/Database
     */
    private String sourceSchema;

    /**
     * 目标Catalog（针对Trino）
     */
    private String targetCatalog;

    /**
     * 目标Schema/Database
     */
    private String targetSchema;

    /**
     * 字段映射关系（JSON格式）
     */
    private String fieldMappings;

    /**
     * WHERE条件
     */
    private String whereClause;

    /**
     * 切分键
     */
    private String splitKey;

    /**
     * 写入模式：append-追加，overwrite-覆盖
     */
    private String writeMode;

    /**
     * 批量大小
     */
    private Integer batchSize;

    /**
     * 并行度
     */
    private Integer parallelism;

    /**
     * NULL值处理策略
     */
    private String nullPolicy;

    /**
     * 前置SQL
     */
    private String preSql;

    /**
     * 后置SQL
     */
    private String postSql;

    /**
     * 调度类型：manual-手动，cron-定时
     */
    private String scheduleType;

    /**
     * Cron表达式
     */
    private String cronExpression;

    /**
     * 同步模式：full-全量，incremental-增量
     */
    private String syncMode;

    /**
     * 状态：pending-待执行，running-执行中，success-成功，failed-失败
     */
    private String status;

    /**
     * 进度（0-100）
     */
    private Integer progress;

    /**
     * 总行数
     */
    private Long totalRows;

    /**
     * 已同步行数
     */
    private Long syncedRows;

    /**
     * 错误信息
     */
    private String errorMessage;

    /**
     * 最后同步时间
     */
    private LocalDateTime lastSyncTime;

    /**
     * 删除标志：0-未删除，1-已删除
     */
    @TableLogic
    private Integer deleted;

    /**
     * 创建人
     */
    private Long createBy;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}

