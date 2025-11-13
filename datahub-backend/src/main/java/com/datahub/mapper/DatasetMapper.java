package com.datahub.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.datahub.entity.Dataset;
import org.apache.ibatis.annotations.Mapper;

/**
 * 数据集Mapper
 *
 * @author DataHub Team
 */
@Mapper
public interface DatasetMapper extends BaseMapper<Dataset> {
}

