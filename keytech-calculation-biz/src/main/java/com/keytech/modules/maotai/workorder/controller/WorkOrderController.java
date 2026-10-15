package com.keytech.modules.maotai.workorder.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.keytech.modules.maotai.workorder.entity.WorkOrder;
import com.keytech.modules.maotai.workorder.service.WorkOrderService;
import com.keytech.modules.maotai.workorder.vo.TransferPathVO;
import com.keytech.modules.maotai.workorder.vo.WorkOrderStatisticsVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.system.vo.LoginUser;
import org.apache.shiro.SecurityUtils;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * 工单任务管理
 * <p>
 * 页面人工创建的工艺工单(卸车/装车/倒罐/灌装),状态机流转+操作日志。
 * 区别于装卸车工单(E+H 对接)与 API05/06 罐转罐工单。
 *
 * @author workorder
 * @since 2026-09-15
 */
@Tag(name = "工单任务管理")
@RestController
@RequestMapping("/maotai/workOrder")
@Slf4j
@RequiredArgsConstructor
public class WorkOrderController {

    private final WorkOrderService workOrderService;

    /** batchAdd 请求体转 WorkOrder 用;裸 ObjectMapper 必须注册 JSR310(planDate 等),并关掉未知字段报错(前端 rows 原样上送 _key/pathOptions)——与 Spring Boot 全局转换器默认行为对齐 */
    private static final ObjectMapper BODY_MAPPER = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
            .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);

    /** BasicTable 列排序白名单:dataIndex(驼峰) → 物理列名,未知列回退 plan_date */
    private static final Map<String, String> SORT_COLUMNS = Map.of(
            "orderNo", "order_no", "planDate", "plan_date", "createTime", "create_time");

    @Operation(summary = "工单分页查询", description = "支持车间/状态/工单号筛选;排序参数 column/order 对齐 BasicTable 约定;status 支持逗号多值(执行中=running,paused)")
    @GetMapping("/list/page")
    public Result<IPage<WorkOrder>> getPageList(@RequestParam(required = false) String workshopCode,
                                                @RequestParam(required = false) String status,
                                                @RequestParam(required = false) String orderNo,
                                                @RequestParam(required = false) String column,
                                                @RequestParam(required = false) String order,
                                                @RequestParam(name = "pageNo", defaultValue = "1") Integer pageNo,
                                                @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize) {
        QueryWrapper<WorkOrder> qw = new QueryWrapper<WorkOrder>().eq("del_flag", 0);
        if (workshopCode != null && !workshopCode.isEmpty()) {
            qw.eq("workshop_code", workshopCode);
        }
        if (status != null && !status.isEmpty()) {
            qw.in("status", Arrays.asList(status.split(",")));
        }
        if (orderNo != null && !orderNo.isEmpty()) {
            qw.like("order_no", orderNo);
        }
        String sortCol = SORT_COLUMNS.getOrDefault(column, "plan_date");
        qw.orderBy(true, !"asc".equalsIgnoreCase(order), sortCol).orderByDesc("create_time");
        return Result.OK(workOrderService.page(new Page<>(pageNo, pageSize), qw));
    }

    @Operation(summary = "车间罐清单(下拉用)", description = "设备台账 tank_config_id 非空=酒罐,按车间过滤;工单与路径配置共用")
    @GetMapping("/tanks")
    public Result<List<Map<String, String>>> tanks(@RequestParam String workshopCode) {
        return Result.OK(workOrderService.listTanks(workshopCode));
    }

    @Operation(summary = "批次追溯数据", description = "全量有效工单(不含已删除),前端据源/目标罐+时间构建酒液谱系图")
    @GetMapping("/trace/orders")
    public Result<List<WorkOrder>> traceOrders() {
        return Result.OK(workOrderService.listValid());
    }

    @Operation(summary = "工单统计", description = "双向联动:状态计数按车间过滤,车间计数按状态过滤(逗号多值)")
    @GetMapping("/statistics")
    public Result<WorkOrderStatisticsVO> statistics(@RequestParam(required = false) String workshopCode,
                                                    @RequestParam(required = false) String status) {
        return Result.OK(workOrderService.statistics(workshopCode, status));
    }

    @Operation(summary = "创建工单(单个)", description = "工单号自动生成 WO-yyyy-nnnn;操作记日志")
    @PostMapping("/add")
    public Result<WorkOrder> add(@RequestBody WorkOrder order) {
        return Result.OK(workOrderService.createOrder(order, currentUserName()));
    }

    @Operation(summary = "批量创建工单", description = "公共字段+多行明细{源罐,目标罐,数量};逐行生成工单号+记日志")
    @PostMapping("/batchAdd")
    public Result<List<WorkOrder>> batchAdd(@RequestBody Map<String, Object> body) {
        WorkOrder common = BODY_MAPPER.convertValue(body.get("common"), WorkOrder.class);
        List<WorkOrder> rows = BODY_MAPPER.convertValue(body.get("rows"),
                BODY_MAPPER.getTypeFactory().constructCollectionType(List.class, WorkOrder.class));
        return Result.OK(workOrderService.createBatch(common, rows, currentUserName()));
    }

    @Operation(summary = "编辑工单", description = "仅待执行(pending)可编辑;操作记日志")
    @PutMapping("/edit")
    public Result<WorkOrder> edit(@RequestParam String id, @RequestBody WorkOrder patch) {
        return Result.OK(workOrderService.editOrder(id, patch, currentUserName()));
    }

    @Operation(summary = "状态流转", description = "action=start/pause/resume/complete/stop/delete;操作记日志")
    @PutMapping("/transition")
    public Result<WorkOrder> transition(@RequestParam String id, @RequestParam String action) {
        return Result.OK(workOrderService.transition(id, action, currentUserName()));
    }

    @Operation(summary = "删除工单", description = "移入已废弃(delete 流转)")
    @DeleteMapping("/delete")
    public Result<WorkOrder> delete(@RequestParam String id) {
        return Result.OK(workOrderService.transition(id, "delete", currentUserName()));
    }

    @Operation(summary = "转酒路径候选", description = "源罐+目标罐+车间+工艺 → 静态线组映射,1~N 条")
    @GetMapping("/paths")
    public Result<List<TransferPathVO>> paths(@RequestParam String workshopCode,
                                              @RequestParam String processName,
                                              @RequestParam(required = false) String sourceTank,
                                              @RequestParam(required = false) String targetTank) {
        return Result.OK(workOrderService.transferPaths(workshopCode, processName, sourceTank, targetTank));
    }

    /** 当前登录用户 realname(无登录上下文回退 system) */
    private String currentUserName() {
        try {
            LoginUser user = (LoginUser) SecurityUtils.getSubject().getPrincipal();
            return user != null && user.getRealname() != null ? user.getRealname() : "system";
        } catch (Exception e) {
            return "system";
        }
    }
}
