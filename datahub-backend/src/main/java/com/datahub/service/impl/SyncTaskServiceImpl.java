package com.datahub.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.datahub.entity.Datasource;
import com.datahub.entity.SyncTask;
import com.datahub.exception.BusinessException;
import com.datahub.mapper.DatasourceMapper;
import com.datahub.mapper.SyncTaskMapper;
import com.datahub.service.SyncTaskService;
import com.datahub.service.SeaTunnelService;
import lombok.extern.slf4j.Slf4j;
import org.quartz.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 数据库同步任务服务实现
 *
 * @author DataHub Team
 */
@Slf4j
@Service
public class SyncTaskServiceImpl implements SyncTaskService {

    @Autowired
    private SyncTaskMapper syncTaskMapper;

    @Autowired
    private DatasourceMapper datasourceMapper;

    @Value("${seatunnel.enabled:true}")
    private Boolean seaTunnelEnabled;

    @Value("${seatunnel.master.host:82.156.36.157}")
    private String seaTunnelHost;

    // 任务日志存储(内存缓存，每个任务最多存傡000条)
    private static final Map<Long, List<String>> TASK_LOGS = new ConcurrentHashMap<>();
    private static final int MAX_LOG_SIZE = 1000;
    private static final DateTimeFormatter LOG_TIME_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    public Page<SyncTask> page(Integer current, Integer size, String keyword) {
        Page<SyncTask> page = new Page<>(current, size);
        LambdaQueryWrapper<SyncTask> wrapper = new LambdaQueryWrapper<>();
        
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w.like(SyncTask::getTaskName, keyword)
                    .or().like(SyncTask::getSourceTable, keyword)
                    .or().like(SyncTask::getTargetTable, keyword));
        }
        
        wrapper.orderByDesc(SyncTask::getCreateTime);
        return syncTaskMapper.selectPage(page, wrapper);
    }

    @Override
    public SyncTask getById(Long id) {
        SyncTask syncTask = syncTaskMapper.selectById(id);
        if (syncTask == null) {
            throw new BusinessException("同步任务不存在");
        }
        return syncTask;
    }

    @Override
    public SyncTask create(SyncTask syncTask) {
        // 检查名称是否重复
        LambdaQueryWrapper<SyncTask> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SyncTask::getTaskName, syncTask.getTaskName());
        if (syncTaskMapper.selectCount(wrapper) > 0) {
            throw new BusinessException("任务名称已存在");
        }

        // 设置默认值
        if (syncTask.getStatus() == null) {
            syncTask.setStatus("pending");
        }
        if (syncTask.getProgress() == null) {
            syncTask.setProgress(0);
        }
        if (syncTask.getTotalRows() == null) {
            syncTask.setTotalRows(0L);
        }
        if (syncTask.getSyncedRows() == null) {
            syncTask.setSyncedRows(0L);
        }

        syncTaskMapper.insert(syncTask);
        log.info("创建同步任务成功: {}", syncTask.getTaskName());
        return syncTask;
    }

    @Override
    public SyncTask update(SyncTask syncTask) {
        SyncTask existing = getById(syncTask.getId());
        
        // 检查名称是否重复
        if (!existing.getTaskName().equals(syncTask.getTaskName())) {
            LambdaQueryWrapper<SyncTask> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(SyncTask::getTaskName, syncTask.getTaskName());
            wrapper.ne(SyncTask::getId, syncTask.getId());
            if (syncTaskMapper.selectCount(wrapper) > 0) {
                throw new BusinessException("任务名称已存在");
            }
        }

        syncTaskMapper.updateById(syncTask);
        log.info("更新同步任务成功: {}", syncTask.getTaskName());
        return syncTask;
    }

    @Override
    public void delete(Long id) {
        SyncTask syncTask = getById(id);
        
        // 检查任务状态
        if ("running".equals(syncTask.getStatus())) {
            throw new BusinessException("任务正在执行中，无法删除");
        }

        syncTaskMapper.deleteById(id);
        log.info("删除同步任务成功: {}", syncTask.getTaskName());
    }

    @Override
    public void execute(Long id) {
        SyncTask syncTask = getById(id);
        
        // 检查任务状态
        if ("running".equals(syncTask.getStatus())) {
            throw new BusinessException("任务正在执行中");
        }

        // 清空旧日志
        TASK_LOGS.put(id, new ArrayList<>());
        addLog(id, "INFO", "开始执行同步任务: " + syncTask.getTaskName());

        // 查询数据源
        Datasource sourceDatasource = datasourceMapper.selectById(syncTask.getSourceDatasourceId());
        Datasource targetDatasource = datasourceMapper.selectById(syncTask.getTargetDatasourceId());
        
        if (sourceDatasource == null || targetDatasource == null) {
            addLog(id, "ERROR", "数据源不存在");
            throw new BusinessException("数据源不存在");
        }

        addLog(id, "INFO", "源数据源: " + sourceDatasource.getName() + " (" + sourceDatasource.getType() + ")");
        addLog(id, "INFO", "目标数据源: " + targetDatasource.getName() + " (" + targetDatasource.getType() + ")");

        // 更新任务状态
        syncTask.setStatus("running");
        syncTask.setProgress(0);
        syncTask.setSyncedRows(0L);
        syncTask.setErrorMessage(null);
        syncTaskMapper.updateById(syncTask);

        // 异步执行同步任务
        new Thread(() -> {
            try {
                if (Boolean.TRUE.equals(seaTunnelEnabled)) {
                    // 使用SeaTunnel执行同步
                    addLog(id, "INFO", "使用 SeaTunnel 引擎执行同步");
                    doSyncWithSeaTunnel(syncTask, sourceDatasource, targetDatasource);
                } else {
                    // 使用JDBC直接同步(兜底方案)
                    addLog(id, "INFO", "使用 JDBC 直接同步");
                    doSync(syncTask, sourceDatasource, targetDatasource);
                }
            } catch (Exception e) {
                log.error("同步任务执行失败: {} - {}", syncTask.getTaskName(), e.getMessage(), e);
                addLog(id, "ERROR", "执行失败: " + e.getMessage());
                syncTask.setStatus("failed");
                syncTask.setErrorMessage(e.getMessage());
                syncTaskMapper.updateById(syncTask);
            }
        }).start();

        log.info("开始执行同步任务: {}", syncTask.getTaskName());
    }

    @Override
    public void stop(Long id) {
        SyncTask syncTask = getById(id);
        
        if (!"running".equals(syncTask.getStatus())) {
            throw new BusinessException("任务未在执行中");
        }

        // 更新任务状态
        syncTask.setStatus("pending");
        syncTaskMapper.updateById(syncTask);
        
        addLog(id, "INFO", "任务已停止");
        log.info("停止同步任务: {}", syncTask.getTaskName());
    }

    @Override
    public List<String> getLogs(Long id) {
        return TASK_LOGS.getOrDefault(id, new ArrayList<>());
    }

    /**
     * 添加任务日志
     */
    private void addLog(Long taskId, String level, String message) {
        List<String> logs = TASK_LOGS.computeIfAbsent(taskId, k -> new ArrayList<>());
        String logEntry = String.format("[%s] [%s] %s", 
            LocalDateTime.now().format(LOG_TIME_FORMAT), level, message);
        
        logs.add(logEntry);
        
        // 限制日志数量
        if (logs.size() > MAX_LOG_SIZE) {
            logs.remove(0);
        }
    }

    /**
     * 使用SeaTunnel执行数据同步
     */
    private void doSyncWithSeaTunnel(SyncTask syncTask, Datasource sourceDatasource, Datasource targetDatasource) throws Exception {
        log.info("使用SeaTunnel执行同步任务: {}", syncTask.getTaskName());
        
        // 1. 生成SeaTunnel配置
        String jobConfig = generateSeaTunnelConfig(syncTask, sourceDatasource, targetDatasource);
        
        // 2. 保存配置到服务器
        String jobId = "job_" + syncTask.getId() + "_" + System.currentTimeMillis();
        String configPath = "/opt/seatunnel/jobs/" + jobId + ".conf";
        saveSeaTunnelConfig(configPath, jobConfig);
        
        // 3. 执行SeaTunnel任务
        executeSeaTunnelJob(syncTask, jobId, configPath);
    }

    /**
     * 生成SeaTunnel配置
     */
    private String generateSeaTunnelConfig(SyncTask syncTask, Datasource sourceDatasource, Datasource targetDatasource) {
        StringBuilder config = new StringBuilder();
        
        // Env配置
        config.append("env {\n");
        config.append("  execution.parallelism = 2\n");
        config.append("  job.mode = \"").append("full".equals(syncTask.getSyncMode()) ? "BATCH" : "STREAMING").append("\"\n");
        config.append("  checkpoint.interval = 10000\n");
        config.append("}\n\n");
        
        // Source配置
        config.append("source {\n");
        String sourceType = sourceDatasource.getType().toUpperCase();
        
        if ("MYSQL".equals(sourceType)) {
            if ("incremental".equals(syncTask.getSyncMode())) {
                // MySQL CDC
                config.append("  MySQL-CDC {\n");
                config.append("    hostname = \"").append(sourceDatasource.getHost()).append("\"\n");
                config.append("    port = ").append(sourceDatasource.getPort()).append("\n");
                config.append("    username = \"").append(sourceDatasource.getUsername()).append("\"\n");
                config.append("    password = \"").append(sourceDatasource.getPassword()).append("\"\n");
                config.append("    database-names = [\"").append(sourceDatasource.getDatabaseName()).append("\"]\n");
                config.append("    table-names = [\"").append(sourceDatasource.getDatabaseName()).append(".").append(syncTask.getSourceTable()).append("\"]\n");
                config.append("    startup.mode = \"initial\"\n");
                config.append("  }\n");
            } else {
                // JDBC Batch
                config.append("  Jdbc {\n");
                config.append("    url = \"").append(buildJdbcUrl(sourceDatasource)).append("\"\n");
                config.append("    driver = \"com.mysql.cj.jdbc.Driver\"\n");
                config.append("    user = \"").append(sourceDatasource.getUsername()).append("\"\n");
                config.append("    password = \"").append(sourceDatasource.getPassword()).append("\"\n");
                // 如果有WHERE条件，添加到查询中
                String query = "SELECT * FROM " + syncTask.getSourceTable();
                if (syncTask.getWhereClause() != null && !syncTask.getWhereClause().isEmpty()) {
                    query += " WHERE " + syncTask.getWhereClause();
                }
                config.append("    query = \"").append(query).append("\"\n");
                config.append("  }\n");
            }
        }
        config.append("}\n\n");
        
        // Sink配置
        config.append("sink {\n");
        String targetType = targetDatasource.getType().toUpperCase();
        
        switch (targetType) {
            case "TRINO":
                // Trino Sink - 使用JDBC连接器
                config.append("  Jdbc {\n");
                config.append("    url = \"").append(buildJdbcUrl(targetDatasource)).append("\"\n");
                config.append("    driver = \"io.trino.jdbc.TrinoDriver\"\n");
                config.append("    user = \"").append(targetDatasource.getUsername()).append("\"\n");
                if (targetDatasource.getPassword() != null && !targetDatasource.getPassword().isEmpty()) {
                    config.append("    password = \"").append(targetDatasource.getPassword()).append("\"\n");
                }
                // 对于Trino，database应该是Schema名称，而不是Catalog
                String targetSchema = syncTask.getTargetSchema();
                if (targetSchema == null || targetSchema.isEmpty()) {
                    // 如果没有指定Schema，使用默认的"default"
                    targetSchema = "default";
                }
                config.append("    database = \"").append(targetSchema).append("\"\n");
                config.append("    table = \"").append(syncTask.getTargetTable()).append("\"\n");
                config.append("    batch_size = 1000\n");
                config.append("  }\n");
                break;
            case "DORIS":
                config.append("  Doris {\n");
                config.append("    fenodes = \"").append(targetDatasource.getHost()).append(":").append(targetDatasource.getPort()).append("\"\n");
                config.append("    username = \"").append(targetDatasource.getUsername()).append("\"\n");
                config.append("    password = \"").append(targetDatasource.getPassword()).append("\"\n");
                config.append("    database = \"").append(targetDatasource.getDatabaseName()).append("\"\n");
                config.append("    table = \"").append(syncTask.getTargetTable()).append("\"\n");
                config.append("    batch_size = 1000\n");
                config.append("  }\n");
                break;
            case "HIVE":
                config.append("  Hive {\n");
                config.append("    table_name = \"").append(syncTask.getTargetTable()).append("\"\n");
                config.append("    metastore_uri = \"thrift://").append(targetDatasource.getHost()).append(":").append(targetDatasource.getPort()).append("\"\n");
                config.append("  }\n");
                break;
            case "ICEBERG":
                config.append("  Iceberg {\n");
                config.append("    warehouse = \"hdfs://").append(targetDatasource.getHost()).append(":").append(targetDatasource.getPort()).append("/warehouse/iceberg\"\n");
                config.append("    catalog_name = \"hadoop\"\n");
                config.append("    namespace = \"").append(targetDatasource.getDatabaseName()).append("\"\n");
                config.append("    table = \"").append(syncTask.getTargetTable()).append("\"\n");
                config.append("  }\n");
                break;
            default:
                // 默认使用JDBC
                config.append("  Jdbc {\n");
                config.append("    url = \"").append(buildJdbcUrl(targetDatasource)).append("\"\n");
                config.append("    driver = \"com.mysql.cj.jdbc.Driver\"\n");
                config.append("    user = \"").append(targetDatasource.getUsername()).append("\"\n");
                config.append("    password = \"").append(targetDatasource.getPassword()).append("\"\n");
                config.append("    table = \"").append(syncTask.getTargetTable()).append("\"\n");
                config.append("  }\n");
        }
        config.append("}\n");
        
        return config.toString();
    }

    /**
     * 保存SeaTunnel配置到服务器
     */
    private void saveSeaTunnelConfig(String remotePath, String content) throws Exception {
        // 通过SSH在Docker容器中保存文件
        String command = String.format(
            "ssh datahub-app \"docker exec seatunnel-server bash -c 'echo \\\"%s\\\" > %s'\"",
            content.replace("\"", "\\\\\\\"").replace("$", "\\$"), remotePath
        );
        
        Process process = Runtime.getRuntime().exec(new String[]{"/bin/sh", "-c", command});
        int exitCode = process.waitFor();
        if (exitCode != 0) {
            throw new Exception("保存SeaTunnel配置失败");
        }
    }

    /**
     * 执行SeaTunnel任务
     */
    private void executeSeaTunnelJob(SyncTask syncTask, String jobId, String configPath) throws Exception {
        addLog(syncTask.getId(), "INFO", "SeaTunnel 任务配置已生成: " + configPath);
        addLog(syncTask.getId(), "INFO", "开始执行 SeaTunnel 任务...");
        
        String command = String.format(
            "ssh datahub-app \"docker exec seatunnel-server bash -c 'cd /opt/seatunnel && ./bin/seatunnel.sh -e local --config %s'\"",
            configPath
        );
        
        Process process = Runtime.getRuntime().exec(new String[]{"/bin/sh", "-c", command});
        
        // 实时读取输出并记录到日志
        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
        
        // 启动线程读取标准输出
        new Thread(() -> {
            try {
                String line;
                while ((line = reader.readLine()) != null) {
                    addLog(syncTask.getId(), "INFO", line);
                    log.info("[SeaTunnel] {}", line);
                }
            } catch (IOException e) {
                log.error("读取SeaTunnel输出失败", e);
            }
        }).start();
        
        // 启动线程读取错误输出
        new Thread(() -> {
            try {
                String line;
                while ((line = errorReader.readLine()) != null) {
                    addLog(syncTask.getId(), "ERROR", line);
                    log.error("[SeaTunnel Error] {}", line);
                }
            } catch (IOException e) {
                log.error("读取SeaTunnel错误输出失败", e);
            }
        }).start();
        
        // 等待任务完成
        int exitCode = process.waitFor();
        
        if (exitCode == 0) {
            syncTask.setStatus("success");
            syncTask.setProgress(100);
            syncTask.setLastSyncTime(LocalDateTime.now());
            addLog(syncTask.getId(), "SUCCESS", "SeaTunnel 任务执行成功: " + jobId);
            log.info("SeaTunnel任务执行成功: {}", jobId);
        } else {
            syncTask.setStatus("failed");
            syncTask.setErrorMessage("SeaTunnel任务执行失败, exitCode: " + exitCode);
            addLog(syncTask.getId(), "ERROR", "SeaTunnel 任务执行失败, exitCode: " + exitCode);
            log.error("SeaTunnel任务执行失败: {}, exitCode: {}", jobId, exitCode);
        }
        
        syncTaskMapper.updateById(syncTask);
    }

    /**
     * 执行数据同步
     */
    private void doSync(SyncTask syncTask, Datasource sourceDatasource, Datasource targetDatasource) throws SQLException {
        String sourceJdbcUrl = buildJdbcUrl(sourceDatasource);
        String targetJdbcUrl = buildJdbcUrl(targetDatasource);

        addLog(syncTask.getId(), "INFO", "建立数据库连接...");

        try (Connection sourceConn = DriverManager.getConnection(
                sourceJdbcUrl, sourceDatasource.getUsername(), sourceDatasource.getPassword());
             Connection targetConn = DriverManager.getConnection(
                targetJdbcUrl, targetDatasource.getUsername(), targetDatasource.getPassword())) {

            // 1. 查询源表数据总数
            String countSql = "SELECT COUNT(*) FROM " + syncTask.getSourceTable();
            addLog(syncTask.getId(), "INFO", "查询源表数据总数...");
            try (Statement stmt = sourceConn.createStatement();
                 ResultSet rs = stmt.executeQuery(countSql)) {
                if (rs.next()) {
                    long totalRows = rs.getLong(1);
                    syncTask.setTotalRows(totalRows);
                    syncTaskMapper.updateById(syncTask);
                    addLog(syncTask.getId(), "INFO", "源表总记录数: " + totalRows);
                }
            }

            // 2. 查询源表结构
            String querySql = "SELECT * FROM " + syncTask.getSourceTable();
            addLog(syncTask.getId(), "INFO", "开始读取源表数据...");
            try (Statement sourceStmt = sourceConn.createStatement();
                 ResultSet rs = sourceStmt.executeQuery(querySql)) {

                ResultSetMetaData metaData = rs.getMetaData();
                int columnCount = metaData.getColumnCount();

                // 3. 构建插入SQL
                StringBuilder insertSql = new StringBuilder("INSERT INTO ");
                insertSql.append(syncTask.getTargetTable()).append(" VALUES (");
                for (int i = 0; i < columnCount; i++) {
                    if (i > 0) insertSql.append(", ");
                    insertSql.append("?");
                }
                insertSql.append(")");

                addLog(syncTask.getId(), "INFO", "开始写入目标表数据...");
                // 4. 批量插入数据
                try (PreparedStatement targetStmt = targetConn.prepareStatement(insertSql.toString())) {
                    int batchSize = 1000;
                    int count = 0;
                    long syncedRows = 0;

                    while (rs.next()) {
                        for (int i = 1; i <= columnCount; i++) {
                            targetStmt.setObject(i, rs.getObject(i));
                        }
                        targetStmt.addBatch();
                        count++;

                        if (count % batchSize == 0) {
                            targetStmt.executeBatch();
                            syncedRows += count;
                            count = 0;

                            // 更新进度
                            syncTask.setSyncedRows(syncedRows);
                            syncTask.setProgress((int) (syncedRows * 100 / syncTask.getTotalRows()));
                            syncTaskMapper.updateById(syncTask);
                            addLog(syncTask.getId(), "INFO", "已同步 " + syncedRows + " 条记录");
                        }
                    }

                    // 执行剩余的批次
                    if (count > 0) {
                        targetStmt.executeBatch();
                        syncedRows += count;
                    }

                    // 更新任务状态
                    syncTask.setStatus("success");
                    syncTask.setProgress(100);
                    syncTask.setSyncedRows(syncedRows);
                    syncTask.setLastSyncTime(LocalDateTime.now());
                    syncTaskMapper.updateById(syncTask);

                    addLog(syncTask.getId(), "SUCCESS", "同步完成，共同步 " + syncedRows + " 条记录");
                    log.info("同步任务执行成功: {} - 同步行数: {}", syncTask.getTaskName(), syncedRows);
                }
            }
        }
    }

    /**
     * 构建JDBC URL
     */
    private String buildJdbcUrl(Datasource datasource) {
        String type = datasource.getType();
        String host = datasource.getHost();
        Integer port = datasource.getPort();
        String database = datasource.getDatabaseName();

        switch (type) {
            case "MySQL":
                return String.format("jdbc:mysql://%s:%d/%s?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=Asia/Shanghai&rewriteBatchedStatements=true", 
                        host, port, database);
            case "PostgreSQL":
                return String.format("jdbc:postgresql://%s:%d/%s", host, port, database);
            case "Oracle":
                return String.format("jdbc:oracle:thin:@%s:%d:%s", host, port, database);
            case "DM":
                return String.format("jdbc:dm://%s:%d/%s", host, port, database);
            case "KingBase":
                return String.format("jdbc:kingbase8://%s:%d/%s", host, port, database);
            case "Trino":
                return String.format("jdbc:trino://%s:%d/%s", host, port, database);
            default:
                throw new BusinessException("不支持的数据库类型: " + type);
        }
    }
}

