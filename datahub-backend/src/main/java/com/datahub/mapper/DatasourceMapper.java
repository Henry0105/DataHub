package com.datahub.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.datahub.entity.Datasource;
import org.apache.ibatis.annotations.Mapper;

/**
 * 数据源Mapper
 *
 * @author DataHub Team
 */
@Mapper
public interface DatasourceMapper extends BaseMapper<Datasource> {
}

