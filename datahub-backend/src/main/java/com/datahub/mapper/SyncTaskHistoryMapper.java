package com.datahub.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.datahub.entity.SyncTaskHistory;
import org.apache.ibatis.annotations.Mapper;

/**
 * 同步任务执行历史Mapper
 *
 * @author DataHub Team
 */
@Mapper
public interface SyncTaskHistoryMapper extends BaseMapper<SyncTaskHistory> {
}
