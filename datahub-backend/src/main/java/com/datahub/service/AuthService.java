package com.datahub.service;

import com.datahub.dto.LoginRequest;
import com.datahub.dto.LoginResponse;

/**
 * 认证服务接口
 *
 * @author DataHub Team
 */
public interface AuthService {

    /**
     * 用户登录
     */
    LoginResponse login(LoginRequest request);
}

