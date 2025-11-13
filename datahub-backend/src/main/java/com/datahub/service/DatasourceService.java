package com.datahub.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.datahub.entity.Datasource;

import java.util.List;
import java.util.Map;

/**
 * 数据源服务接口
 *
 * @author DataHub Team
 */
public interface DatasourceService {

    /**
     * 分页查询数据源
     */
    Page<Datasource> page(Integer current, Integer size, String keyword);

    /**
     * 查询所有数据源
     */
    List<Datasource> list();

    /**
     * 根据ID查询数据源
     */
    Datasource getById(Long id);

    /**
     * 创建数据源
     */
    Datasource create(Datasource datasource);

    /**
     * 更新数据源
     */
    Datasource update(Datasource datasource);

    /**
     * 删除数据源
     */
    void delete(Long id);

    /**
     * 测试数据源连接（已存在的数据源）
     */
    boolean testConnection(Long id);

    /**
     * 测试数据源连接并更新状态
     */
    Datasource testConnectionAndUpdateStatus(Long id);

    /**
     * 测试数据源连接（通过配置）
     */
    boolean testConnectionByConfig(Datasource datasource);

    /**
     * 获取数据源的所有数据库/Catalog列表
     */
    List<String> getDatabases(Long id);

    /**
     * 获取数据源的Schema列表（对Trino需要传catalog参数）
     */
    List<String> getSchemas(Long id, String catalog);

    /**
     * 获取数据源的所有表
     */
    List<String> getTables(Long id, String database);

    /**
     * 获取表的字段信息
     */
    List<Map<String, Object>> getTableColumns(Long id, String tableName, String database);
}

