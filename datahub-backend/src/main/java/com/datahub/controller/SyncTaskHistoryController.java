package com.datahub.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.datahub.common.PageResult;
import com.datahub.common.Result;
import com.datahub.entity.SyncTaskHistory;
import com.datahub.service.SyncTaskHistoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 同步任务执行历史控制器
 *
 * @author DataHub Team
 */
@RestController
@RequestMapping("/sync/history")
public class SyncTaskHistoryController {

    @Autowired
    private SyncTaskHistoryService historyService;

    /**
     * 分页查询执行历史
     */
    @GetMapping("/page")
    public Result<PageResult<SyncTaskHistory>> page(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long taskId) {
        Page<SyncTaskHistory> page = historyService.page(current, size, keyword, taskId);
        PageResult<SyncTaskHistory> result = new PageResult<>(
                page.getRecords(),
                page.getTotal(),
                page.getCurrent(),
                page.getSize()
        );
        return Result.success(result);
    }

    /**
     * 根据ID查询执行历史
     */
    @GetMapping("/{id}")
    public Result<SyncTaskHistory> getById(@PathVariable Long id) {
        SyncTaskHistory history = historyService.getById(id);
        return Result.success(history);
    }
}
