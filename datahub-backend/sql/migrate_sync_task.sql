-- =============================================
-- sync_task 表结构迁移脚本
-- 说明：添加完整的任务配置字段
-- =============================================

USE `datahub`;

-- 删除旧表并重新创建（如果有重要数据，请先备份）
DROP TABLE IF EXISTS `sync_task`;

CREATE TABLE `sync_task` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '任务ID',
  `task_name` VARCHAR(200) NOT NULL COMMENT '任务名称',
  `description` VARCHAR(500) DEFAULT NULL COMMENT '任务描述',
  
  -- 数据源信息
  `source_datasource_id` BIGINT NOT NULL COMMENT '源数据源ID',
  `target_datasource_id` BIGINT NOT NULL COMMENT '目标数据源ID',
  `source_table` VARCHAR(200) NOT NULL COMMENT '源表名',
  `target_table` VARCHAR(200) NOT NULL COMMENT '目标表名',
  
  -- Trino相关
  `source_catalog` VARCHAR(100) DEFAULT NULL COMMENT '源Catalog（针对Trino）',
  `source_schema` VARCHAR(100) DEFAULT NULL COMMENT '源Schema/Database',
  `target_catalog` VARCHAR(100) DEFAULT NULL COMMENT '目标Catalog（针对Trino）',
  `target_schema` VARCHAR(100) DEFAULT NULL COMMENT '目标Schema/Database',
  
  -- 字段映射
  `field_mappings` TEXT DEFAULT NULL COMMENT '字段映射关系（JSON格式）',
  
  -- 任务属性
  `where_clause` VARCHAR(1000) DEFAULT NULL COMMENT 'WHERE条件',
  `split_key` VARCHAR(100) DEFAULT NULL COMMENT '切分键',
  `write_mode` VARCHAR(20) DEFAULT 'append' COMMENT '写入模式：append-追加，overwrite-覆盖',
  `batch_size` INT DEFAULT 1024 COMMENT '批量大小',
  `parallelism` INT DEFAULT 1 COMMENT '并行度',
  `null_policy` VARCHAR(20) DEFAULT 'ignore' COMMENT 'NULL值处理策略',
  `pre_sql` TEXT DEFAULT NULL COMMENT '前置SQL',
  `post_sql` TEXT DEFAULT NULL COMMENT '后置SQL',
  
  -- 调度配置
  `schedule_type` VARCHAR(20) DEFAULT 'manual' COMMENT '调度类型：manual-手动，cron-定时',
  `cron_expression` VARCHAR(100) DEFAULT NULL COMMENT 'Cron表达式',
  
  -- 同步模式和状态
  `sync_mode` VARCHAR(20) DEFAULT 'full' COMMENT '同步模式：full-全量，incremental-增量',
  `status` VARCHAR(20) DEFAULT 'pending' COMMENT '状态：pending-待执行，running-执行中，success-成功，failed-失败',
  `progress` INT DEFAULT 0 COMMENT '进度（0-100）',
  `total_rows` BIGINT DEFAULT 0 COMMENT '总行数',
  `synced_rows` BIGINT DEFAULT 0 COMMENT '已同步行数',
  `error_message` TEXT DEFAULT NULL COMMENT '错误信息',
  `last_sync_time` DATETIME DEFAULT NULL COMMENT '最后同步时间',
  
  -- 通用字段
  `deleted` TINYINT DEFAULT 0 COMMENT '删除标志：0-未删除，1-已删除',
  `create_by` BIGINT DEFAULT NULL COMMENT '创建人',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  
  PRIMARY KEY (`id`),
  KEY `idx_source_datasource` (`source_datasource_id`),
  KEY `idx_target_datasource` (`target_datasource_id`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='数据库同步任务表';

-- 插入测试数据（可选）
INSERT INTO `sync_task` (`task_name`, `source_datasource_id`, `target_datasource_id`, `source_table`, `target_table`, `sync_mode`, `status`, `progress`, `total_rows`, `synced_rows`, `last_sync_time`, `create_by`) VALUES
('用户数据同步', 1, 2, 'users', 'users_backup', 'full', 'success', 100, 15000, 15000, '2025-01-10 10:30:00', 1),
('订单数据同步', 1, 2, 'orders', 'orders_backup', 'incremental', 'running', 65, 50000, 32500, '2025-01-10 11:00:00', 1),
('产品数据同步', 1, 3, 'products', 'products_backup', 'full', 'pending', 0, 0, 0, NULL, 1);
