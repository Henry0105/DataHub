-- =============================================
-- DataHub 数据中台数据库初始化脚本
-- =============================================

CREATE DATABASE IF NOT EXISTS `datahub` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE `datahub`;

-- =============================================
-- 用户表
-- =============================================
DROP TABLE IF EXISTS `sys_user`;
CREATE TABLE `sys_user` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '用户ID',
  `username` VARCHAR(50) NOT NULL COMMENT '用户名',
  `password` VARCHAR(100) NOT NULL COMMENT '密码',
  `nickname` VARCHAR(50) DEFAULT NULL COMMENT '昵称',
  `email` VARCHAR(100) DEFAULT NULL COMMENT '邮箱',
  `phone` VARCHAR(20) DEFAULT NULL COMMENT '手机号',
  `avatar` VARCHAR(255) DEFAULT NULL COMMENT '头像',
  `status` TINYINT DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
  `deleted` TINYINT DEFAULT 0 COMMENT '删除标志：0-未删除，1-已删除',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- 插入默认管理员账号（密码：admin123）
INSERT INTO `sys_user` (`username`, `password`, `nickname`, `status`) 
VALUES ('admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '管理员', 1);

-- =============================================
-- 数据源表
-- =============================================
DROP TABLE IF EXISTS `datasource`;
CREATE TABLE `datasource` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '数据源ID',
  `name` VARCHAR(100) NOT NULL COMMENT '数据源名称',
  `type` VARCHAR(50) NOT NULL COMMENT '数据源类型：MySQL、PostgreSQL、Oracle、DM、KingBase',
  `host` VARCHAR(100) NOT NULL COMMENT '主机地址',
  `port` INT NOT NULL COMMENT '端口',
  `database_name` VARCHAR(100) NOT NULL COMMENT '数据库名',
  `username` VARCHAR(100) NOT NULL COMMENT '用户名',
  `password` VARCHAR(255) NOT NULL COMMENT '密码（加密）',
  `description` VARCHAR(500) DEFAULT NULL COMMENT '描述',
  `status` TINYINT DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
  `deleted` TINYINT DEFAULT 0 COMMENT '删除标志：0-未删除，1-已删除',
  `create_by` BIGINT DEFAULT NULL COMMENT '创建人',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_type` (`type`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='数据源表';

-- 插入测试数据源
INSERT INTO `datasource` (`name`, `type`, `host`, `port`, `database_name`, `username`, `password`, `description`, `create_by`) VALUES
('本地MySQL', 'MySQL', 'localhost', 3306, 'test', 'root', 'root', '本地测试MySQL数据库', 1),
('生产MySQL', 'MySQL', '10.2.0.16', 3306, 'datahub', 'root', 'DataHub@2025', '生产环境MySQL数据库', 1),
('生产PostgreSQL', 'PostgreSQL', '10.2.0.16', 5432, 'datahub', 'postgres', 'DataHub@2025', '生产环境PostgreSQL数据库', 1);

-- =============================================
-- 数据库同步任务表
-- =============================================
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

-- 插入测试同步任务
INSERT INTO `sync_task` (`task_name`, `source_datasource_id`, `target_datasource_id`, `source_table`, `target_table`, `sync_mode`, `status`, `progress`, `total_rows`, `synced_rows`, `last_sync_time`, `create_by`) VALUES
('用户数据同步', 1, 2, 'users', 'users_backup', 'full', 'success', 100, 15000, 15000, '2025-01-10 10:30:00', 1),
('订单数据同步', 1, 2, 'orders', 'orders_backup', 'incremental', 'running', 65, 50000, 32500, '2025-01-10 11:00:00', 1),
('产品数据同步', 1, 3, 'products', 'products_backup', 'full', 'pending', 0, 0, 0, NULL, 1);

-- =============================================
-- SQL查询历史表
-- =============================================
DROP TABLE IF EXISTS `query_history`;
CREATE TABLE `query_history` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '查询ID',
  `datasource_id` BIGINT NOT NULL COMMENT '数据源ID',
  `sql_content` TEXT NOT NULL COMMENT 'SQL内容',
  `result_rows` INT DEFAULT 0 COMMENT '结果行数',
  `execution_time` INT DEFAULT 0 COMMENT '执行时间（毫秒）',
  `status` VARCHAR(20) DEFAULT 'success' COMMENT '状态：success-成功，failed-失败',
  `error_message` TEXT DEFAULT NULL COMMENT '错误信息',
  `deleted` TINYINT DEFAULT 0 COMMENT '删除标志：0-未删除，1-已删除',
  `create_by` BIGINT DEFAULT NULL COMMENT '创建人',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_datasource` (`datasource_id`),
  KEY `idx_create_by` (`create_by`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='SQL查询历史表';

-- 插入测试查询历史
INSERT INTO `query_history` (`datasource_id`, `sql_content`, `result_rows`, `execution_time`, `status`, `create_by`) VALUES
(1, 'SELECT * FROM users WHERE status = 1 LIMIT 100', 100, 45, 'success', 1),
(1, 'SELECT COUNT(*) FROM orders WHERE create_time > "2025-01-01"', 1, 120, 'success', 1),
(2, 'SELECT * FROM products WHERE price > 100', 50, 89, 'success', 1);

-- =============================================
-- 数据集表
-- =============================================
DROP TABLE IF EXISTS `dataset`;
CREATE TABLE `dataset` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '数据集ID',
  `name` VARCHAR(100) NOT NULL COMMENT '数据集名称',
  `datasource_id` BIGINT NOT NULL COMMENT '数据源ID',
  `sql_content` TEXT NOT NULL COMMENT 'SQL内容',
  `description` VARCHAR(500) DEFAULT NULL COMMENT '描述',
  `row_count` BIGINT DEFAULT 0 COMMENT '数据行数',
  `column_count` INT DEFAULT 0 COMMENT '列数',
  `last_refresh_time` DATETIME DEFAULT NULL COMMENT '最后刷新时间',
  `deleted` TINYINT DEFAULT 0 COMMENT '删除标志：0-未删除，1-已删除',
  `create_by` BIGINT DEFAULT NULL COMMENT '创建人',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_datasource` (`datasource_id`),
  KEY `idx_create_by` (`create_by`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='数据集表';

-- 插入测试数据集
INSERT INTO `dataset` (`name`, `datasource_id`, `sql_content`, `description`, `row_count`, `column_count`, `last_refresh_time`, `create_by`) VALUES
('活跃用户数据集', 1, 'SELECT * FROM users WHERE status = 1 AND last_login_time > DATE_SUB(NOW(), INTERVAL 30 DAY)', '最近30天活跃的用户数据', 1250, 8, '2025-01-10 09:00:00', 1),
('订单统计数据集', 1, 'SELECT DATE(create_time) as date, COUNT(*) as count, SUM(amount) as total FROM orders GROUP BY DATE(create_time)', '按日期统计的订单数据', 365, 3, '2025-01-10 10:00:00', 1),
('热销产品数据集', 1, 'SELECT p.*, COUNT(oi.id) as sales FROM products p LEFT JOIN order_items oi ON p.id = oi.product_id GROUP BY p.id ORDER BY sales DESC LIMIT 100', '销量前100的产品数据', 100, 10, '2025-01-10 08:00:00', 1);

-- =============================================
-- 可视化报表表
-- =============================================
DROP TABLE IF EXISTS `report`;
CREATE TABLE `report` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '报表ID',
  `name` VARCHAR(100) NOT NULL COMMENT '报表名称',
  `dataset_id` BIGINT NOT NULL COMMENT '数据集ID',
  `chart_type` VARCHAR(50) DEFAULT 'line' COMMENT '图表类型：line-折线图，bar-柱状图，pie-饼图',
  `config` TEXT DEFAULT NULL COMMENT '图表配置（JSON）',
  `description` VARCHAR(500) DEFAULT NULL COMMENT '描述',
  `deleted` TINYINT DEFAULT 0 COMMENT '删除标志：0-未删除，1-已删除',
  `create_by` BIGINT DEFAULT NULL COMMENT '创建人',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_dataset` (`dataset_id`),
  KEY `idx_create_by` (`create_by`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='可视化报表表';

-- 插入测试报表
INSERT INTO `report` (`name`, `dataset_id`, `chart_type`, `description`, `create_by`) VALUES
('用户增长趋势', 1, 'line', '展示用户数量随时间的增长趋势', 1),
('订单金额统计', 2, 'bar', '按日期统计订单金额', 1),
('产品销量分布', 3, 'pie', '热销产品的销量占比', 1);

-- =============================================
-- 统计数据（用于首页看板）
-- =============================================
DROP TABLE IF EXISTS `dashboard_stats`;
CREATE TABLE `dashboard_stats` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `stat_date` DATE NOT NULL COMMENT '统计日期',
  `datasource_count` INT DEFAULT 0 COMMENT '数据源数量',
  `sync_task_count` INT DEFAULT 0 COMMENT '同步任务数量',
  `query_count` INT DEFAULT 0 COMMENT '查询次数',
  `dataset_count` INT DEFAULT 0 COMMENT '数据集数量',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_stat_date` (`stat_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='统计数据表';

-- 插入测试统计数据
INSERT INTO `dashboard_stats` (`stat_date`, `datasource_count`, `sync_task_count`, `query_count`, `dataset_count`) VALUES
('2025-01-10', 3, 3, 156, 3);

