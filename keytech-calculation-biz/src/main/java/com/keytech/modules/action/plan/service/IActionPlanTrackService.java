package com.keytech.modules.action.plan.service;

import com.keytech.modules.action.plan.dto.ActionPlanSummaryChartDto;
import com.keytech.modules.action.plan.dto.ActionPlanSummaryListDto;
import com.keytech.modules.action.plan.dto.ActionPlanSummaryQueryDto;
import com.keytech.modules.action.plan.entity.ActionPlanTrack;
import com.keytech.base.service.BaseService;
import com.keytech.modules.home.dto.EmpRaFromDto;
import com.keytech.modules.home.dto.WSActionStatisticsDto;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jeecg.common.api.vo.Result;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * @Description: 行动计划跟踪
 * @Author: jeecg-boot
 * @Date:   2025-01-08
 * @Version: V1.0
 */
public interface IActionPlanTrackService extends BaseService<ActionPlanTrack> {

    /**
     * 定时任务使用更新行动相关数据
     * @param parameterObject xxl-job所传json参数
     */
    void updateDataByJob(String parameterObject);

    /**
     * 共其他地方使用的行动计划保存方法
     */
    void saveOrUpdateAction(String dataId, List<ActionPlanTrack> actionPlanTrackList);

    /**
     * 自定义导入
     */
    Result<?> importExcelBy(HttpServletRequest request, HttpServletResponse response, Class<ActionPlanTrack> clazz) throws IOException;

    /**
     * 行动计划汇总统计数据
     */
    Map<String, Object> getSummaryData(ActionPlanSummaryQueryDto queryDto);

    /**
     * 行动计划首页汇总统计数据
     */
    Map<String, Object> queryIndexSummaryData(WSActionStatisticsDto dto);

    /**
     * RA单统计
     * @param empRaFromDto
     * @return
     */
    Map<String, Object> empRaFrom(EmpRaFromDto empRaFromDto);
}
