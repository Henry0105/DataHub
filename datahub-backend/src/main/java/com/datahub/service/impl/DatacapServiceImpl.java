package com.datahub.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.datahub.dto.QueryRequest;
import com.datahub.dto.QueryResult;
import com.datahub.entity.Datasource;
import com.datahub.entity.Dataset;
import com.datahub.entity.QueryHistory;
import com.datahub.exception.BusinessException;
import com.datahub.mapper.DatasetMapper;
import com.datahub.mapper.DatasourceMapper;
import com.datahub.mapper.QueryHistoryMapper;
import com.datahub.service.DatacapService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 数据中枢服务实现
 *
 * @author DataHub Team
 */
@Slf4j
@Service
public class DatacapServiceImpl implements DatacapService {

    @Autowired
    private DatasourceMapper datasourceMapper;

    @Autowired
    private QueryHistoryMapper queryHistoryMapper;

    @Autowired
    private DatasetMapper datasetMapper;

    @Override
    public QueryResult executeQuery(QueryRequest request) {
        long startTime = System.currentTimeMillis();
        
        // 查询数据源
        Datasource datasource = datasourceMapper.selectById(request.getDatasourceId());
        if (datasource == null) {
            throw new BusinessException("数据源不存在");
        }

        QueryResult result = new QueryResult();
        QueryHistory history = new QueryHistory();
        history.setDatasourceId(request.getDatasourceId());
        history.setSqlContent(request.getSqlContent());

        try {
            // 执行SQL
            String jdbcUrl = buildJdbcUrl(datasource);
            try (Connection connection = DriverManager.getConnection(
                    jdbcUrl, datasource.getUsername(), datasource.getPassword());
                 Statement statement = connection.createStatement();
                 ResultSet rs = statement.executeQuery(request.getSqlContent())) {

                // 获取列信息
                ResultSetMetaData metaData = rs.getMetaData();
                int columnCount = metaData.getColumnCount();
                List<String> columns = new ArrayList<>();
                for (int i = 1; i <= columnCount; i++) {
                    columns.add(metaData.getColumnLabel(i));
                }

                // 获取数据
                List<Map<String, Object>> data = new ArrayList<>();
                while (rs.next()) {
                    Map<String, Object> row = new HashMap<>();
                    for (int i = 1; i <= columnCount; i++) {
                        row.put(columns.get(i - 1), rs.getObject(i));
                    }
                    data.add(row);
                }

                // 构建结果
                result.setColumns(columns);
                result.setData(data);
                result.setRows(data.size());
                result.setDuration((int) (System.currentTimeMillis() - startTime));

                // 保存查询历史
                history.setResultRows(data.size());
                history.setExecutionTime(result.getDuration());
                history.setStatus("success");
                queryHistoryMapper.insert(history);

                log.info("SQL查询成功: 数据源={}, 行数={}, 耗时={}ms", 
                        datasource.getName(), data.size(), result.getDuration());
            }
        } catch (Exception e) {
            log.error("SQL查询失败: {}", e.getMessage(), e);
            
            // 保存失败记录
            history.setResultRows(0);
            history.setExecutionTime((int) (System.currentTimeMillis() - startTime));
            history.setStatus("failed");
            history.setErrorMessage(e.getMessage());
            queryHistoryMapper.insert(history);
            
            throw new BusinessException("查询失败: " + e.getMessage());
        }

        return result;
    }

    @Override
    public Page<QueryHistory> queryHistoryPage(Integer current, Integer size, String keyword) {
        Page<QueryHistory> page = new Page<>(current, size);
        LambdaQueryWrapper<QueryHistory> wrapper = new LambdaQueryWrapper<>();
        
        if (StringUtils.hasText(keyword)) {
            wrapper.like(QueryHistory::getSqlContent, keyword);
        }
        
        wrapper.orderByDesc(QueryHistory::getCreateTime);
        return queryHistoryMapper.selectPage(page, wrapper);
    }

    @Override
    public void deleteQueryHistory(Long id) {
        queryHistoryMapper.deleteById(id);
        log.info("删除查询历史成功: {}", id);
    }

    @Override
    public Page<Dataset> datasetPage(Integer current, Integer size, String keyword) {
        Page<Dataset> page = new Page<>(current, size);
        LambdaQueryWrapper<Dataset> wrapper = new LambdaQueryWrapper<>();
        
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w.like(Dataset::getName, keyword)
                    .or().like(Dataset::getDescription, keyword));
        }
        
        wrapper.orderByDesc(Dataset::getCreateTime);
        return datasetMapper.selectPage(page, wrapper);
    }

    @Override
    public Dataset getDatasetById(Long id) {
        Dataset dataset = datasetMapper.selectById(id);
        if (dataset == null) {
            throw new BusinessException("数据集不存在");
        }
        return dataset;
    }

    @Override
    public Dataset createDataset(Dataset dataset) {
        // 检查名称是否重复
        LambdaQueryWrapper<Dataset> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Dataset::getName, dataset.getName());
        if (datasetMapper.selectCount(wrapper) > 0) {
            throw new BusinessException("数据集名称已存在");
        }

        datasetMapper.insert(dataset);
        log.info("创建数据集成功: {}", dataset.getName());
        return dataset;
    }

    @Override
    public Dataset updateDataset(Dataset dataset) {
        Dataset existing = getDatasetById(dataset.getId());
        
        // 检查名称是否重复
        if (!existing.getName().equals(dataset.getName())) {
            LambdaQueryWrapper<Dataset> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(Dataset::getName, dataset.getName());
            wrapper.ne(Dataset::getId, dataset.getId());
            if (datasetMapper.selectCount(wrapper) > 0) {
                throw new BusinessException("数据集名称已存在");
            }
        }

        datasetMapper.updateById(dataset);
        log.info("更新数据集成功: {}", dataset.getName());
        return dataset;
    }

    @Override
    public void deleteDataset(Long id) {
        Dataset dataset = getDatasetById(id);
        datasetMapper.deleteById(id);
        log.info("删除数据集成功: {}", dataset.getName());
    }

    @Override
    public QueryResult refreshDataset(Long id) {
        Dataset dataset = getDatasetById(id);
        
        // 执行SQL查询
        QueryRequest request = new QueryRequest();
        request.setDatasourceId(dataset.getDatasourceId());
        request.setSqlContent(dataset.getSqlContent());
        QueryResult result = executeQuery(request);
        
        // 更新数据集信息
        dataset.setRowCount((long) result.getRows());
        dataset.setColumnCount(result.getColumns().size());
        dataset.setLastRefreshTime(LocalDateTime.now());
        datasetMapper.updateById(dataset);
        
        log.info("刷新数据集成功: {}", dataset.getName());
        return result;
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
                return String.format("jdbc:mysql://%s:%d/%s?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=Asia/Shanghai", 
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

