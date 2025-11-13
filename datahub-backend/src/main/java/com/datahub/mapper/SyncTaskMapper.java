package com.datahub.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.datahub.entity.SyncTask;
import org.apache.ibatis.annotations.Mapper;

/**
 * 数据库同步任务Mapper
 *
 * @author DataHub Team
 */
@Mapper
public interface SyncTaskMapper extends BaseMapper<SyncTask> {
}

