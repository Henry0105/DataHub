package com.datahub.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.datahub.entity.SyncTaskHistory;
import com.datahub.exception.BusinessException;
import com.datahub.mapper.SyncTaskHistoryMapper;
import com.datahub.service.SyncTaskHistoryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 同步任务历史服务实现
 *
 * @author DataHub Team
 */
@Slf4j
@Service
public class SyncTaskHistoryServiceImpl implements SyncTaskHistoryService {

    @Autowired
    private SyncTaskHistoryMapper syncTaskHistoryMapper;

    @Override
    public Page<SyncTaskHistory> page(Integer current, Integer size, String keyword, Long taskId) {
        Page<SyncTaskHistory> page = new Page<>(current, size);
        LambdaQueryWrapper<SyncTaskHistory> wrapper = new LambdaQueryWrapper<>();
        
        if (taskId != null) {
            wrapper.eq(SyncTaskHistory::getTaskId, taskId);
        }
        
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.and(w -> w.like(SyncTaskHistory::getTaskName, keyword)
                    .or().like(SyncTaskHistory::getSourceTable, keyword)
                    .or().like(SyncTaskHistory::getTargetTable, keyword));
        }
        
        wrapper.orderByDesc(SyncTaskHistory::getCreateTime);
        return syncTaskHistoryMapper.selectPage(page, wrapper);
    }

    @Override
    public SyncTaskHistory getById(Long id) {
        SyncTaskHistory history = syncTaskHistoryMapper.selectById(id);
        if (history == null) {
            throw new BusinessException("历史记录不存在");
        }
        return history;
    }

    @Override
    public SyncTaskHistory create(SyncTaskHistory history) {
        syncTaskHistoryMapper.insert(history);
        log.info("创建同步任务历史记录: {}", history.getTaskName());
        return history;
    }

    @Override
    public void delete(Long id) {
        syncTaskHistoryMapper.deleteById(id);
        log.info("删除同步任务历史记录: {}", id);
    }
}
