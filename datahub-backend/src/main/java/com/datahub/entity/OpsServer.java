package com.datahub.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 运维服务器实体
 */
@Data
@TableName("ops_server")
public class OpsServer {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private String name;
    
    private String ip;
    
    private String type;
    
    private String vendor;
    
    private String cpu;
    
    private String memory;
    
    private String disk;
    
    private String status;
    
    private String sshHost;
    
    private Integer sshPort;
    
    private String sshUser;
    
    private String description;
    
    private String rack;
    
    private String cpuArch;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
