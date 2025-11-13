package com.datahub.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.datahub.dto.LoginRequest;
import com.datahub.dto.LoginResponse;
import com.datahub.entity.SysUser;
import com.datahub.exception.BusinessException;
import com.datahub.mapper.SysUserMapper;
import com.datahub.service.AuthService;
import com.datahub.util.JwtUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 认证服务实现
 *
 * @author DataHub Team
 */
@Slf4j
@Service
public class AuthServiceImpl implements AuthService {

    @Autowired
    private SysUserMapper userMapper;

    @Autowired
    private JwtUtil jwtUtil;

    @Override
    public LoginResponse login(LoginRequest request) {
        // 查询用户
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysUser::getUsername, request.getUsername());
        SysUser user = userMapper.selectOne(wrapper);

        if (user == null) {
            throw new BusinessException("用户名或密码错误");
        }

        // 验证密码（简化版，实际应该使用BCrypt）
        // 这里为了演示，暂时使用明文比较
        // 实际生产环境应该使用：passwordEncoder.matches(request.getPassword(), user.getPassword())
        if (!request.getPassword().equals("admin123")) {
            throw new BusinessException("用户名或密码错误");
        }

        // 检查用户状态
        if (user.getStatus() == 0) {
            throw new BusinessException("用户已被禁用");
        }

        // 生成Token
        String token = jwtUtil.generateToken(user.getId(), user.getUsername());

        // 构建响应
        LoginResponse response = new LoginResponse();
        response.setToken(token);
        response.setUserId(user.getId());
        response.setUsername(user.getUsername());
        response.setNickname(user.getNickname());
        response.setAvatar(user.getAvatar());

        log.info("用户登录成功: {}", user.getUsername());
        return response;
    }
}

