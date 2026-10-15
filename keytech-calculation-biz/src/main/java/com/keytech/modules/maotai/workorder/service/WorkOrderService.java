package com.keytech.modules.maotai.workorder.service;

import com.keytech.base.service.BaseService;
import com.keytech.modules.maotai.workorder.entity.WorkOrder;
import com.keytech.modules.maotai.workorder.vo.TransferPathVO;
import com.keytech.modules.maotai.workorder.vo.WorkOrderStatisticsVO;

import java.util.List;

/**
 * 工单任务服务
 *
 * @author workorder
 * @since 2026-09-15
 */
public interface WorkOrderService extends BaseService<WorkOrder> {

    /** 分页查询后的业务包装(子类暂无额外加工,留扩展位) */
    List<WorkOrder> listValid();

    /**
     * 各状态计数 + 各车间计数(双向联动,所见即所得):
     * statusCount 按车间过滤(该车间下各状态数),workshopCount 按状态过滤(该状态下各车间数)。
     * @param workshopCode 当前车间 tab,null=全车间
     * @param status       当前状态 tab,逗号多值(running,paused),null=全状态
     */
    WorkOrderStatisticsVO statistics(String workshopCode, String status);

    /** 创建工单(单个):工单号生成+校验+记日志 */
    WorkOrder createOrder(WorkOrder order, String operator);

    /** 批量创建:公共字段+多行明细,逐行生成工单号+记日志 */
    List<WorkOrder> createBatch(WorkOrder common, List<WorkOrder> rows, String operator);

    /** 编辑工单(仅 pending):可编辑字段回填+记日志 */
    WorkOrder editOrder(String id, WorkOrder patch, String operator);

    /**
     * 状态流转(统一入口)
     *
     * @param action  start/pause/resume/stop/delete
     * @param operator 操作人(登录 realname)
     */
    WorkOrder transition(String id, String action, String operator);

    /** 转酒路径候选:车间+工艺+源/目标罐 → transfer_route 配置表(1~N 条,无代码兜底) */
    List<TransferPathVO> transferPaths(String workshopCode, String processName, String sourceTank, String targetTank);

    /** 车间罐清单(设备台账 tank_config_id 非空=酒罐,label=value=设备名);工单与路径配置下拉共用 */
    List<java.util.Map<String, String>> listTanks(String workshopCode);
}
