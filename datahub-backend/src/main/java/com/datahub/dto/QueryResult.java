package com.datahub.dto;

import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * SQL查询结果
 *
 * @author DataHub Team
 */
@Data
public class QueryResult {

    /**
     * 列名列表
     */
    private List<String> columns;

    /**
     * 数据列表
     */
    private List<Map<String, Object>> data;

    /**
     * 结果行数
     */
    private Integer rows;

    /**
     * 执行时间（毫秒）
     */
    private Integer duration;
}

