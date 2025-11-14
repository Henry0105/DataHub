package com.datahub.controller;

import com.datahub.common.Result;
import com.datahub.service.OpsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 运维管理Controller
 */
@Slf4j
@RestController
@RequestMapping("/ops")
public class OpsController {
    
    @Autowired
    private OpsService opsService;
    
    /**
     * 获取服务器列表
     */
    @GetMapping("/servers")
    public Result<List<Map<String, Object>>> getServers() {
        log.info("获取服务器列表");
        try {
            List<Map<String, Object>> servers = opsService.getServers();
            return Result.success(servers);
        } catch (Exception e) {
            log.error("获取服务器列表失败", e);
            return Result.error("获取服务器列表失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取组件列表
     */
    @GetMapping("/components")
    public Result<List<Map<String, Object>>> getComponents() {
        log.info("获取组件列表");
        try {
            List<Map<String, Object>> components = opsService.getComponents();
            return Result.success(components);
        } catch (Exception e) {
            log.error("获取组件列表失败", e);
            return Result.error("获取组件列表失败: " + e.getMessage());
        }
    }
}
