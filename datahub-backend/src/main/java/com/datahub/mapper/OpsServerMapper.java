package com.datahub.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.datahub.entity.OpsServer;
import org.apache.ibatis.annotations.Mapper;

/**
 * 运维服务器Mapper
 */
@Mapper
public interface OpsServerMapper extends BaseMapper<OpsServer> {
}
