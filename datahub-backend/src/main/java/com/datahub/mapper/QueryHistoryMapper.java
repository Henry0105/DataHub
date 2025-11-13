package com.datahub.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.datahub.entity.QueryHistory;
import org.apache.ibatis.annotations.Mapper;

/**
 * SQL查询历史Mapper
 *
 * @author DataHub Team
 */
@Mapper
public interface QueryHistoryMapper extends BaseMapper<QueryHistory> {
}

