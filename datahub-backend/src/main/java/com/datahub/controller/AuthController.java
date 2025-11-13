package com.datahub.controller;

import com.datahub.common.Result;
import com.datahub.dto.LoginRequest;
import com.datahub.dto.LoginResponse;
import com.datahub.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 认证控制器
 *
 * @author DataHub Team
 */
@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    /**
     * 用户登录
     */
    @PostMapping("/login")
    public Result<LoginResponse> login(@Validated @RequestBody LoginRequest request) {
        LoginResponse response = authService.login(request);
        return Result.success("登录成功", response);
    }

    /**
     * 用户登出
     */
    @PostMapping("/logout")
    public Result<String> logout() {
        // 实际应该清除Redis中的Token
        return Result.success("登出成功");
    }

    /**
     * 获取当前用户信息
     */
    @GetMapping("/info")
    public Result<LoginResponse> getUserInfo(@RequestHeader("Authorization") String token) {
        // 这里简化处理，实际应该从Token中解析用户信息
        LoginResponse response = new LoginResponse();
        response.setUserId(1L);
        response.setUsername("admin");
        response.setNickname("管理员");
        return Result.success(response);
    }
}

