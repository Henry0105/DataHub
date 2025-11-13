package com.datahub.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.datahub.entity.Datasource;
import com.datahub.exception.BusinessException;
import com.datahub.mapper.DatasourceMapper;
import com.datahub.service.DatasourceService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.sql.*;
import java.util.*;

/**
 * 数据源服务实现
 *
 * @author DataHub Team
 */
@Slf4j
@Service
public class DatasourceServiceImpl implements DatasourceService {

    @Autowired
    private DatasourceMapper datasourceMapper;

    @Override
    public Page<Datasource> page(Integer current, Integer size, String keyword) {
        Page<Datasource> page = new Page<>(current, size);
        LambdaQueryWrapper<Datasource> wrapper = new LambdaQueryWrapper<>();
        
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w.like(Datasource::getName, keyword)
                    .or().like(Datasource::getType, keyword)
                    .or().like(Datasource::getHost, keyword));
        }
        
        wrapper.orderByDesc(Datasource::getCreateTime);
        return datasourceMapper.selectPage(page, wrapper);
    }

    @Override
    public List<Datasource> list() {
        LambdaQueryWrapper<Datasource> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Datasource::getStatus, 1);
        wrapper.orderByDesc(Datasource::getCreateTime);
        return datasourceMapper.selectList(wrapper);
    }

    @Override
    public Datasource getById(Long id) {
        Datasource datasource = datasourceMapper.selectById(id);
        if (datasource == null) {
            throw new BusinessException("数据源不存在");
        }
        return datasource;
    }

    @Override
    public Datasource create(Datasource datasource) {
        // 检查名称是否重复
        LambdaQueryWrapper<Datasource> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Datasource::getName, datasource.getName());
        if (datasourceMapper.selectCount(wrapper) > 0) {
            throw new BusinessException("数据源名称已存在");
        }

        // 测试连接并设置状态
        try {
            testConnectionByConfig(datasource);
            datasource.setStatus(1); // 连接成功，状态设为正常
        } catch (Exception e) {
            datasource.setStatus(0); // 连接失败，状态设为异常
            // 不需要再次记录日志，testConnectionByConfig已经记录了
        }

        datasourceMapper.insert(datasource);
        log.info("创建数据源成功: {}", datasource.getName());
        return datasource;
    }

    @Override
    public Datasource update(Datasource datasource) {
        Datasource existing = getById(datasource.getId());
        
        // 检查名称是否重复
        if (!existing.getName().equals(datasource.getName())) {
            LambdaQueryWrapper<Datasource> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(Datasource::getName, datasource.getName());
            wrapper.ne(Datasource::getId, datasource.getId());
            if (datasourceMapper.selectCount(wrapper) > 0) {
                throw new BusinessException("数据源名称已存在");
            }
        }

        // 测试连接并更新状态
        try {
            testConnectionByConfig(datasource);
            datasource.setStatus(1); // 连接成功，状态设为正常
        } catch (Exception e) {
            datasource.setStatus(0); // 连接失败，状态设为异常
            // 不需要再次记录日志，testConnectionByConfig已经记录了
        }

        datasourceMapper.updateById(datasource);
        log.info("更新数据源成功: {}", datasource.getName());
        return datasource;
    }

    @Override
    public void delete(Long id) {
        Datasource datasource = getById(id);
        datasourceMapper.deleteById(id);
        log.info("删除数据源成功: {}", datasource.getName());
    }

    @Override
    public boolean testConnection(Long id) {
        Datasource datasource = getById(id);
        return testConnectionByConfig(datasource);
    }

    @Override
    public Datasource testConnectionAndUpdateStatus(Long id) {
        Datasource datasource = getById(id);
        
        try {
            testConnectionByConfig(datasource);
            datasource.setStatus(1); // 连接成功
        } catch (Exception e) {
            datasource.setStatus(0); // 连接失败
            // 不需要再次记录日志，testConnectionByConfig已经记录了
        }
        
        // 更新数据库中的状态
        datasourceMapper.updateById(datasource);
        return datasource;
    }

    @Override
    public boolean testConnectionByConfig(Datasource datasource) {
        try {
            String jdbcUrl = buildJdbcUrl(datasource);
            log.info("测试连接: {} - {}", datasource.getType(), jdbcUrl);
            
            // 设置连接超时时间为5秒
            DriverManager.setLoginTimeout(5);
            
            Connection connection = DriverManager.getConnection(
                    jdbcUrl, 
                    datasource.getUsername(), 
                    datasource.getPassword()
            );
            
            // 测试连接是否有效（超时时间3秒）
            if (!connection.isValid(3)) {
                connection.close();
                throw new BusinessException("连接无效");
            }
            
            connection.close();
            log.info("数据源连接测试成功: {}", datasource.getName());
            return true;
        } catch (Exception e) {
            log.error("数据源连接测试失败: {} - {}", datasource.getName(), e.getMessage());
            throw new BusinessException("连接失败: " + e.getMessage());
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
                return String.format("jdbc:mysql://%s:%d/%s?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true", 
                        host, port, database);
            case "PostgreSQL":
            case "GreenPlum":
                return String.format("jdbc:postgresql://%s:%d/%s", host, port, database);
            case "Oracle":
                return String.format("jdbc:oracle:thin:@%s:%d:%s", host, port, database);
            case "SQLServer":
                return String.format("jdbc:sqlserver://%s:%d;databaseName=%s", host, port, database);
            case "DB2":
                return String.format("jdbc:db2://%s:%d/%s", host, port, database);
            case "Sybase":
                return String.format("jdbc:jtds:sybase://%s:%d/%s", host, port, database);
            case "SQLite":
                return String.format("jdbc:sqlite:%s", database);
            case "DM":
                return String.format("jdbc:dm://%s:%d/%s", host, port, database);
            case "KingBase":
                return String.format("jdbc:kingbase8://%s:%d/%s", host, port, database);
            case "ClickHouse":
                return String.format("jdbc:clickhouse://%s:%d/%s", host, port, database);
            case "Hive":
                return String.format("jdbc:hive2://%s:%d/%s", host, port, database);
            case "Trino":
                return String.format("jdbc:trino://%s:%d/%s", host, port, database);
            case "StarRocks":
            case "Doris":
                return String.format("jdbc:mysql://%s:%d/%s?useSSL=false&allowPublicKeyRetrieval=true", host, port, database);
            case "MongoDB":
                return String.format("mongodb://%s:%s@%s:%d/%s", 
                        datasource.getUsername(), datasource.getPassword(), host, port, database);
            case "Redis":
                return String.format("redis://%s:%d", host, port);
            case "ElasticSearch":
                return String.format("http://%s:%d", host, port);
            case "TDEngine":
                return String.format("jdbc:TAOS://%s:%d/%s", host, port, database);
            default:
                throw new BusinessException("不支持的数据库类型: " + type);
        }
    }

    @Override
    public List<String> getDatabases(Long id) {
        Datasource datasource = getById(id);
        List<String> databases = new ArrayList<>();
        
        try {
            String jdbcUrl = buildJdbcUrl(datasource);
            DriverManager.setLoginTimeout(5);
            
            Connection connection = DriverManager.getConnection(
                    jdbcUrl,
                    datasource.getUsername(),
                    datasource.getPassword()
            );
            
            if ("Trino".equals(datasource.getType())) {
                // Trino 特殊处理：获取 Catalog 列表
                Statement stmt = connection.createStatement();
                ResultSet rs = stmt.executeQuery("SHOW CATALOGS");
                while (rs.next()) {
                    databases.add(rs.getString(1));
                }
                rs.close();
                stmt.close();
            } else {
                // 其他数据库：获取数据库列表
                DatabaseMetaData metaData = connection.getMetaData();
                ResultSet rs = metaData.getCatalogs();
                while (rs.next()) {
                    databases.add(rs.getString("TABLE_CAT"));
                }
                rs.close();
            }
            
            connection.close();
            log.info("获取数据库列表成功: {} - 共{}个库", datasource.getName(), databases.size());
        } catch (Exception e) {
            log.error("获取数据库列表失败: {} - {}", datasource.getName(), e.getMessage());
            throw new BusinessException("获取数据库列表失败: " + e.getMessage());
        }
        
        return databases;
    }

    @Override
    public List<String> getSchemas(Long id, String catalog) {
        Datasource datasource = getById(id);
        List<String> schemas = new ArrayList<>();
        
        try {
            String jdbcUrl = buildJdbcUrl(datasource);
            DriverManager.setLoginTimeout(5);
            
            Connection connection = DriverManager.getConnection(
                    jdbcUrl,
                    datasource.getUsername(),
                    datasource.getPassword()
            );
            
            if ("Trino".equals(datasource.getType()) && catalog != null) {
                // Trino 特殊处理：获取指定 Catalog 下的 Schema 列表
                Statement stmt = connection.createStatement();
                ResultSet rs = stmt.executeQuery("SHOW SCHEMAS FROM " + catalog);
                while (rs.next()) {
                    schemas.add(rs.getString(1));
                }
                rs.close();
                stmt.close();
            } else {
                // 其他数据库：获取 Schema 列表
                DatabaseMetaData metaData = connection.getMetaData();
                ResultSet rs = metaData.getSchemas();
                while (rs.next()) {
                    schemas.add(rs.getString("TABLE_SCHEM"));
                }
                rs.close();
            }
            
            connection.close();
            log.info("获取Schema列表成功: {} - 共{}个Schema", datasource.getName(), schemas.size());
        } catch (Exception e) {
            log.error("获取Schema列表失败: {} - {}", datasource.getName(), e.getMessage());
            throw new BusinessException("获取Schema列表失败: " + e.getMessage());
        }
        
        return schemas;
    }

    @Override
    public List<String> getTables(Long id, String database) {
        Datasource datasource = getById(id);
        List<String> tables = new ArrayList<>();
        
        try {
            String jdbcUrl = buildJdbcUrl(datasource);
            DriverManager.setLoginTimeout(5);
            
            Connection connection = DriverManager.getConnection(
                    jdbcUrl,
                    datasource.getUsername(),
                    datasource.getPassword()
            );
            
            if ("Trino".equals(datasource.getType())) {
                // Trino 特殊处理：需要指定 catalog.schema
                String schema = database != null ? database : "default";
                String catalog = datasource.getDatabaseName(); // catalog 存储在 databaseName 字段
                Statement stmt = connection.createStatement();
                ResultSet rs = stmt.executeQuery("SHOW TABLES FROM " + catalog + "." + schema);
                while (rs.next()) {
                    tables.add(rs.getString(1));
                }
                rs.close();
                stmt.close();
            } else {
                // 其他数据库：使用标准 JDBC MetaData
                DatabaseMetaData metaData = connection.getMetaData();
                String dbName = database != null ? database : datasource.getDatabaseName();
                ResultSet rs = metaData.getTables(dbName, null, "%", new String[]{"TABLE"});
                
                while (rs.next()) {
                    String tableName = rs.getString("TABLE_NAME");
                    tables.add(tableName);
                }
                rs.close();
            }
            
            connection.close();
            log.info("获取数据源表列表成功: {} - 共{}张表", datasource.getName(), tables.size());
        } catch (Exception e) {
            log.error("获取数据源表列表失败: {} - {}", datasource.getName(), e.getMessage());
            throw new BusinessException("获取表列表失败: " + e.getMessage());
        }
        
        return tables;
    }

    @Override
    public List<Map<String, Object>> getTableColumns(Long id, String tableName, String database) {
        Datasource datasource = getById(id);
        List<Map<String, Object>> columns = new ArrayList<>();
        
        try {
            String jdbcUrl = buildJdbcUrl(datasource);
            DriverManager.setLoginTimeout(5);
            
            Connection connection = DriverManager.getConnection(
                    jdbcUrl,
                    datasource.getUsername(),
                    datasource.getPassword()
            );
            
            if ("Trino".equals(datasource.getType())) {
                // Trino 特殊处理：使用 DESCRIBE 查询
                // database 参数可能是 "catalog.schema" 格式
                String catalog;
                String schema;
                
                if (database != null && database.contains(".")) {
                    // 格式为 catalog.schema
                    String[] parts = database.split("\\.", 2);
                    catalog = parts[0];
                    schema = parts[1];
                } else {
                    // 只有 schema，使用默认 catalog
                    catalog = datasource.getDatabaseName();
                    schema = database != null ? database : "default";
                }
                
                Statement stmt = connection.createStatement();
                String sql = "DESCRIBE " + catalog + "." + schema + "." + tableName;
                log.info("Trino DESCRIBE SQL: {}", sql);
                ResultSet rs = stmt.executeQuery(sql);
                
                while (rs.next()) {
                    Map<String, Object> column = new HashMap<>();
                    column.put("columnName", rs.getString("Column"));
                    column.put("dataType", rs.getString("Type"));
                    column.put("columnSize", 0); // Trino 不返回长度
                    column.put("nullable", true); // 默认可为空
                    column.put("remarks", rs.getString("Comment") != null ? rs.getString("Comment") : "");
                    columns.add(column);
                }
                rs.close();
                stmt.close();
            } else {
                // 其他数据库：使用标准 JDBC MetaData
                DatabaseMetaData metaData = connection.getMetaData();
                String dbName = database != null ? database : datasource.getDatabaseName();
                ResultSet rs = metaData.getColumns(dbName, null, tableName, "%");
                
                while (rs.next()) {
                    Map<String, Object> column = new HashMap<>();
                    column.put("columnName", rs.getString("COLUMN_NAME"));
                    column.put("dataType", rs.getString("TYPE_NAME"));
                    column.put("columnSize", rs.getInt("COLUMN_SIZE"));
                    column.put("nullable", rs.getInt("NULLABLE") == DatabaseMetaData.columnNullable);
                    column.put("remarks", rs.getString("REMARKS"));
                    columns.add(column);
                }
                rs.close();
            }
            
            connection.close();
            log.info("获取表字段信息成功: {}.{} - 共{}个字段", datasource.getName(), tableName, columns.size());
        } catch (Exception e) {
            log.error("获取表字段信息失败: {}.{} - {}", datasource.getName(), tableName, e.getMessage());
            throw new BusinessException("获取字段信息失败: " + e.getMessage());
        }
        
        return columns;
    }
}

