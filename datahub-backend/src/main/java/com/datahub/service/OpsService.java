package com.datahub.service;

import com.datahub.entity.OpsServer;
import com.datahub.entity.OpsComponent;
import com.datahub.mapper.OpsServerMapper;
import com.datahub.mapper.OpsComponentMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 运维管理服务
 */
@Slf4j
@Service
public class OpsService {
    
    @Autowired
    private OpsServerMapper serverMapper;
    
    @Autowired
    private OpsComponentMapper componentMapper;
    
    /**
     * 获取所有服务器
     */
    public List<Map<String, Object>> getServers() {
        List<OpsServer> servers = serverMapper.selectList(null);
        
        return servers.stream().map(server -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", server.getId());
            map.put("name", server.getName());
            map.put("ip", server.getIp());
            map.put("type", server.getType());
            map.put("vendor", server.getVendor());
            map.put("cpu", server.getCpu());
            map.put("memory", server.getMemory());
            map.put("disk", server.getDisk());
            map.put("status", server.getStatus());
            map.put("rack", server.getRack());
            map.put("cpuArch", server.getCpuArch());
            map.put("sshHost", server.getSshHost());
            map.put("cpuUsage", 0); // 后续实现真实监控
            map.put("memUsage", 0);
            map.put("diskUsage", 0);
            return map;
        }).collect(Collectors.toList());
    }
    
    /**
     * 获取所有组件
     */
    public List<Map<String, Object>> getComponents() {
        List<OpsComponent> components = componentMapper.selectList(null);
        
        return components.stream().map(comp -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", comp.getId());
            map.put("name", comp.getName());
            map.put("version", comp.getVersion());
            map.put("server", comp.getServerName() != null ? comp.getServerName() : "-");
            map.put("port", comp.getPort() != null ? comp.getPort() : "-");
            map.put("status", comp.getStatus());
            map.put("installTime", comp.getInstallTime() != null ? comp.getInstallTime().toString() : "-");
            return map;
        }).collect(Collectors.toList());
    }
}
