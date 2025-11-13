package com.datahub.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * SQL查询历史实体
 *
 * @author DataHub Team
 */
@Data
@TableName("query_history")
public class QueryHistory implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 查询ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 数据源ID
     */
    private Long datasourceId;

    /**
     * SQL内容
     */
    private String sqlContent;

    /**
     * 结果行数
     */
    private Integer resultRows;

    /**
     * 执行时间（毫秒）
     */
    private Integer executionTime;

    /**
     * 状态：success-成功，failed-失败
     */
    private String status;

    /**
     * 错误信息
     */
    private String errorMessage;

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
}

