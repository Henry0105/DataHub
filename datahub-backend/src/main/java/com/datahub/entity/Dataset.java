package com.datahub.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 数据集实体
 *
 * @author DataHub Team
 */
@Data
@TableName("dataset")
public class Dataset implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 数据集ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 数据集名称
     */
    private String name;

    /**
     * 数据源ID
     */
    private Long datasourceId;

    /**
     * SQL内容
     */
    private String sqlContent;

    /**
     * 描述
     */
    private String description;

    /**
     * 数据行数
     */
    private Long rowCount;

    /**
     * 列数
     */
    private Integer columnCount;

    /**
     * 最后刷新时间
     */
    private LocalDateTime lastRefreshTime;

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

