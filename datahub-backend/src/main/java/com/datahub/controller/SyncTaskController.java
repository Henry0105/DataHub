package com.datahub.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.datahub.common.PageResult;
import com.datahub.common.Result;
import com.datahub.entity.SyncTask;
import com.datahub.service.SyncTaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 数据库同步任务控制器
 *
 * @author DataHub Team
 */
@RestController
@RequestMapping("/sync")
public class SyncTaskController {

    @Autowired
    private SyncTaskService syncTaskService;

    /**
     * 分页查询同步任务
     */
    @GetMapping("/page")
    public Result<PageResult<SyncTask>> page(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String keyword) {
        Page<SyncTask> page = syncTaskService.page(current, size, keyword);
        PageResult<SyncTask> result = new PageResult<>(
                page.getRecords(),
                page.getTotal(),
                page.getCurrent(),
                page.getSize()
        );
        return Result.success(result);
    }

    /**
     * 根据ID查询同步任务
     */
    @GetMapping("/{id}")
    public Result<SyncTask> getById(@PathVariable Long id) {
        SyncTask syncTask = syncTaskService.getById(id);
        return Result.success(syncTask);
    }

    /**
     * 创建同步任务
     */
    @PostMapping
    public Result<SyncTask> create(@RequestBody SyncTask syncTask) {
        SyncTask created = syncTaskService.create(syncTask);
        return Result.success("创建成功", created);
    }

    /**
     * 更新同步任务
     */
    @PutMapping("/{id}")
    public Result<SyncTask> update(@PathVariable Long id, @RequestBody SyncTask syncTask) {
        syncTask.setId(id);
        SyncTask updated = syncTaskService.update(syncTask);
        return Result.success("更新成功", updated);
    }

    /**
     * 删除同步任务
     */
    @DeleteMapping("/{id}")
    public Result<String> delete(@PathVariable Long id) {
        syncTaskService.delete(id);
        return Result.success("删除成功");
    }

    /**
     * 执行同步任务
     */
    @PostMapping("/{id}/execute")
    public Result<String> execute(@PathVariable Long id) {
        syncTaskService.execute(id);
        return Result.success("任务已开始执行");
    }

    /**
     * 停止同步任务
     */
    @PostMapping("/{id}/stop")
    public Result<String> stop(@PathVariable Long id) {
        syncTaskService.stop(id);
        return Result.success("任务已停止");
    }

    /**
     * 获取任务执行日志
     */
    @GetMapping("/{id}/logs")
    public Result<java.util.List<String>> getLogs(@PathVariable Long id) {
        java.util.List<String> logs = syncTaskService.getLogs(id);
        return Result.success(logs);
    }
}

