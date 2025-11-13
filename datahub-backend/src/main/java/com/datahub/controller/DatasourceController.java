package com.datahub.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.datahub.common.PageResult;
import com.datahub.common.Result;
import com.datahub.entity.Datasource;
import com.datahub.service.DatasourceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 数据源管理控制器
 *
 * @author DataHub Team
 */
@RestController
@RequestMapping("/datasource")
public class DatasourceController {

    @Autowired
    private DatasourceService datasourceService;

    /**
     * 分页查询数据源
     */
    @GetMapping("/page")
    public Result<PageResult<Datasource>> page(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String keyword) {
        Page<Datasource> page = datasourceService.page(current, size, keyword);
        PageResult<Datasource> result = new PageResult<>(
                page.getRecords(),
                page.getTotal(),
                page.getCurrent(),
                page.getSize()
        );
        return Result.success(result);
    }

    /**
     * 查询所有数据源
     */
    @GetMapping("/list")
    public Result<List<Datasource>> list() {
        List<Datasource> list = datasourceService.list();
        return Result.success(list);
    }

    /**
     * 根据ID查询数据源
     */
    @GetMapping("/{id}")
    public Result<Datasource> getById(@PathVariable Long id) {
        Datasource datasource = datasourceService.getById(id);
        return Result.success(datasource);
    }

    /**
     * 创建数据源
     */
    @PostMapping
    public Result<Datasource> create(@RequestBody Datasource datasource) {
        Datasource created = datasourceService.create(datasource);
        return Result.success("创建成功", created);
    }

    /**
     * 更新数据源
     */
    @PutMapping("/{id}")
    public Result<Datasource> update(@PathVariable Long id, @RequestBody Datasource datasource) {
        datasource.setId(id);
        Datasource updated = datasourceService.update(datasource);
        return Result.success("更新成功", updated);
    }

    /**
     * 删除数据源
     */
    @DeleteMapping("/{id}")
    public Result<String> delete(@PathVariable Long id) {
        datasourceService.delete(id);
        return Result.<String>success("删除成功");
    }

    /**
     * 测试数据源连接
     */
    @PostMapping("/{id}/test")
    public Result<Datasource> testConnection(@PathVariable Long id) {
        Datasource datasource = datasourceService.testConnectionAndUpdateStatus(id);
        return Result.success(datasource);
    }

    /**
     * 测试数据源连接（通过配置）
     */
    @PostMapping("/test")
    public Result<String> testConnectionByConfig(@RequestBody Datasource datasource) {
        datasourceService.testConnectionByConfig(datasource);
        return Result.<String>success("连接测试成功");
    }

    /**
     * 获取数据源的所有数据库/Catalog列表
     */
    @GetMapping("/{id}/databases")
    public Result<List<String>> getDatabases(@PathVariable Long id) {
        List<String> databases = datasourceService.getDatabases(id);
        return Result.success(databases);
    }

    /**
     * 获取数据源的Schema列表（对Trino需要传catalog参数）
     */
    @GetMapping("/{id}/schemas")
    public Result<List<String>> getSchemas(
            @PathVariable Long id,
            @RequestParam(required = false) String catalog) {
        List<String> schemas = datasourceService.getSchemas(id, catalog);
        return Result.success(schemas);
    }

    /**
     * 获取数据源的所有表
     */
    @GetMapping("/{id}/tables")
    public Result<List<String>> getTables(
            @PathVariable Long id,
            @RequestParam(required = false) String database) {
        List<String> tables = datasourceService.getTables(id, database);
        return Result.success(tables);
    }

    /**
     * 获取表的字段信息
     */
    @GetMapping("/{id}/tables/{tableName}/columns")
    public Result<List<Map<String, Object>>> getTableColumns(
            @PathVariable Long id,
            @PathVariable String tableName,
            @RequestParam(required = false) String database) {
        List<Map<String, Object>> columns = datasourceService.getTableColumns(id, tableName, database);
        return Result.success(columns);
    }
}
