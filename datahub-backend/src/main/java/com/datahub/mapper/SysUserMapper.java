package com.datahub.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.datahub.entity.SysUser;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户Mapper
 *
 * @author DataHub Team
 */
@Mapper
public interface SysUserMapper extends BaseMapper<SysUser> {
}

