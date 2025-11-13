package com.datahub.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * SQL查询请求
 *
 * @author DataHub Team
 */
@Data
public class QueryRequest {

    /**
     * 数据源ID
     */
    @NotNull(message = "数据源ID不能为空")
    private Long datasourceId;

    /**
     * SQL内容
     */
    @NotBlank(message = "SQL不能为空")
    private String sqlContent;
}

