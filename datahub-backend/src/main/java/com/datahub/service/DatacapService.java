package com.datahub.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.datahub.dto.QueryRequest;
import com.datahub.dto.QueryResult;
import com.datahub.entity.Dataset;
import com.datahub.entity.QueryHistory;

/**
 * 数据中枢服务接口
 *
 * @author DataHub Team
 */
public interface DatacapService {

    /**
     * 执行SQL查询
     */
    QueryResult executeQuery(QueryRequest request);

    /**
     * 分页查询查询历史
     */
    Page<QueryHistory> queryHistoryPage(Integer current, Integer size, String keyword);

    /**
     * 删除查询历史
     */
    void deleteQueryHistory(Long id);

    /**
     * 分页查询数据集
     */
    Page<Dataset> datasetPage(Integer current, Integer size, String keyword);

    /**
     * 根据ID查询数据集
     */
    Dataset getDatasetById(Long id);

    /**
     * 创建数据集
     */
    Dataset createDataset(Dataset dataset);

    /**
     * 更新数据集
     */
    Dataset updateDataset(Dataset dataset);

    /**
     * 删除数据集
     */
    void deleteDataset(Long id);

    /**
     * 刷新数据集
     */
    QueryResult refreshDataset(Long id);
}

