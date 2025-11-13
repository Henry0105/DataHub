package com.datahub.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 同步任务历史记录实体
 *
 * @author DataHub Team
 */
@Data
@TableName("sync_task_history")
public class SyncTaskHistory implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 历史记录ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 任务ID
     */
    private Long taskId;

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
     * 同步模式：full-全量，incremental-增量
     */
    private String syncMode;

    /**
     * 状态：success-成功，failed-失败
     */
    private String status;

    /**
     * 总行数
     */
    private Long totalRows;

    /**
     * 已同步行数
     */
    private Long syncedRows;

    /**
     * 耗时（毫秒）
     */
    private Long duration;

    /**
     * 错误信息
     */
    private String errorMessage;

    /**
     * 开始时间
     */
    private LocalDateTime startTime;

    /**
     * 结束时间
     */
    private LocalDateTime endTime;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
