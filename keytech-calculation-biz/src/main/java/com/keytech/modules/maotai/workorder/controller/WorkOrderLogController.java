package com.keytech.modules.maotai.workorder.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.keytech.modules.maotai.workorder.entity.WorkOrderLog;
import com.keytech.modules.maotai.workorder.mapper.WorkOrderLogMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.api.vo.Result;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 工单操作日志查询(仅记录工单的人工操作)
 * <p>
 * 查询条件:工单号(用户要求) + 工单状态 + 关键字(操作人/操作类型/操作内容模糊) + 操作时间范围。
 *
 * @author workorder
 * @since 2026-09-15
 */
@Tag(name = "工单操作日志")
@RestController
@RequestMapping("/maotai/workOrderLog")
@Slf4j
@RequiredArgsConstructor
public class WorkOrderLogController {

    private final WorkOrderLogMapper workOrderLogMapper;

    @Operation(summary = "工单操作日志分页查询", description = "筛选:工单号/工单状态/关键字(操作人+类型+内容)/时间范围")
    @GetMapping("/list/page")
    public Result<IPage<WorkOrderLog>> getPageList(@RequestParam(required = false) String orderNo,
                                                   @RequestParam(required = false) String status,
                                                   @RequestParam(required = false) String keyword,
                                                   @RequestParam(name = "beginTime", required = false) String beginTime,
                                                   @RequestParam(name = "endTime", required = false) String endTime,
                                                   @RequestParam(name = "pageNo", defaultValue = "1") Integer pageNo,
                                                   @RequestParam(name = "pageSize", defaultValue = "20") Integer pageSize) {
        QueryWrapper<WorkOrderLog> qw = new QueryWrapper<WorkOrderLog>().eq("del_flag", 0);
        if (orderNo != null && !orderNo.isEmpty()) {
            qw.like("order_no", orderNo);
        }
        if (status != null && !status.isEmpty()) {
            qw.eq("order_status", status);
        }
        if (keyword != null && !keyword.isEmpty()) {
            // 原型语义:搜索框覆盖 操作人/操作对象(工单号)/操作内容(+操作类型)
            qw.and(w -> w.like("operator", keyword).or().like("content", keyword).or().like("order_no", keyword).or().like("action", keyword));
        }
        if (beginTime != null && !beginTime.isEmpty()) {
            qw.ge("operate_time", beginTime + " 00:00:00");
        }
        if (endTime != null && !endTime.isEmpty()) {
            qw.le("operate_time", endTime + " 23:59:59");
        }
        qw.orderByDesc("operate_time");
        return Result.OK(workOrderLogMapper.selectPage(new Page<>(pageNo, pageSize), qw));
    }
}
