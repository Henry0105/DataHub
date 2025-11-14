package com.datahub.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 运维组件实体
 */
@Data
@TableName("ops_component")
public class OpsComponent {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private String name;
    
    private String description;
    
    private String remark;
    
    private String version;
    
    private Long serverId;
    
    private String serverName;
    
    private String port;
    
    private String status;
    
    private String dockerContainerId;
    
    private String dockerImage;
    
    private String configPath;
    
    private String logPath;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime installTime;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
