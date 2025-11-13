package com.datahub.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.datahub.common.PageResult;
import com.datahub.common.Result;
import com.datahub.dto.QueryRequest;
import com.datahub.dto.QueryResult;
import com.datahub.entity.Dataset;
import com.datahub.entity.QueryHistory;
import com.datahub.service.DatacapService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 数据中枢控制器
 *
 * @author DataHub Team
 */
@RestController
@RequestMapping("/datacap")
public class DatacapController {

    @Autowired
    private DatacapService datacapService;

    /**
     * 执行SQL查询
     */
    @PostMapping("/query")
    public Result<QueryResult> executeQuery(@Validated @RequestBody QueryRequest request) {
        QueryResult result = datacapService.executeQuery(request);
        return Result.success(result);
    }

    /**
     * 分页查询查询历史
     */
    @GetMapping("/history/page")
    public Result<PageResult<QueryHistory>> queryHistoryPage(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String keyword) {
        Page<QueryHistory> page = datacapService.queryHistoryPage(current, size, keyword);
        PageResult<QueryHistory> result = new PageResult<>(
                page.getRecords(),
                page.getTotal(),
                page.getCurrent(),
                page.getSize()
        );
        return Result.success(result);
    }

    /**
     * 删除查询历史
     */
    @DeleteMapping("/history/{id}")
    public Result<String> deleteQueryHistory(@PathVariable Long id) {
        datacapService.deleteQueryHistory(id);
        return Result.success("删除成功");
    }

    /**
     * 分页查询数据集
     */
    @GetMapping("/dataset/page")
    public Result<PageResult<Dataset>> datasetPage(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String keyword) {
        Page<Dataset> page = datacapService.datasetPage(current, size, keyword);
        PageResult<Dataset> result = new PageResult<>(
                page.getRecords(),
                page.getTotal(),
                page.getCurrent(),
                page.getSize()
        );
        return Result.success(result);
    }

    /**
     * 根据ID查询数据集
     */
    @GetMapping("/dataset/{id}")
    public Result<Dataset> getDatasetById(@PathVariable Long id) {
        Dataset dataset = datacapService.getDatasetById(id);
        return Result.success(dataset);
    }

    /**
     * 创建数据集
     */
    @PostMapping("/dataset")
    public Result<Dataset> createDataset(@RequestBody Dataset dataset) {
        Dataset created = datacapService.createDataset(dataset);
        return Result.success("创建成功", created);
    }

    /**
     * 更新数据集
     */
    @PutMapping("/dataset/{id}")
    public Result<Dataset> updateDataset(@PathVariable Long id, @RequestBody Dataset dataset) {
        dataset.setId(id);
        Dataset updated = datacapService.updateDataset(dataset);
        return Result.success("更新成功", updated);
    }

    /**
     * 删除数据集
     */
    @DeleteMapping("/dataset/{id}")
    public Result<String> deleteDataset(@PathVariable Long id) {
        datacapService.deleteDataset(id);
        return Result.success("删除成功");
    }

    /**
     * 刷新数据集
     */
    @PostMapping("/dataset/{id}/refresh")
    public Result<QueryResult> refreshDataset(@PathVariable Long id) {
        QueryResult result = datacapService.refreshDataset(id);
        return Result.success("刷新成功", result);
    }
}

