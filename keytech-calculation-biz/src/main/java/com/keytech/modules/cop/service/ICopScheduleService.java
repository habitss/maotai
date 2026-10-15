package com.keytech.modules.cop.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.keytech.base.service.BaseService;
import com.keytech.modules.cop.dto.*;
import com.keytech.modules.cop.entity.CopSchedule;
import com.keytech.modules.home.dto.WSActionStatisticsDto;

import java.util.List;

/**
 * <p>
 * cop计划排期 服务类
 * </p>
 *
 * @author lijun
 * @since 2025-02-20 02:19:51
 */
public interface ICopScheduleService extends BaseService<CopSchedule> {

    /**
     * weekJson转对象
     * @param copSchedule
     * @return
     * @throws Exception
     */
    List<CopScheduleJson> initScheduleJsonList(CopSchedule copSchedule) throws Exception;

    /**
     * 加工wekkJson数据
     * @param copScheduleJson
     * @return
     */
    CopScheduleJson processJson(CopScheduleJson copScheduleJson) throws Exception;

    /**
     * 构建清洗表
     * @param copScheduleRequestVO
     * @param months
     * @return
     */
    Page<CopSchedule> initCopScheduleList(CopSchedule copScheduleRequestVO, List<String> months, Integer pageNo, Integer pageSize);

    /**
     * 排期
     * @return
     */
    CopSchedule saveOrUpdateCopScheduler(CopSchedulerVo copSchedule);

    /**
     * 排期
     * @return
     */
    CopSchedule cancelCopScheduler(CopSchedulerVo copScheduleVo);

    /**
     * 更新排期配置
     * @param list
     */
    void updateCop(List<CopSchedulerVo> list) throws Exception;

    /**
     * 处理排期
     * @param id
     * @param num
     * @param status
     * @return
     */
    CopSchedule processCopSchedule(String id, int num,String status);


    /**
     * cop统计
     * @param copStatisticsDto
     * @return
     */
    List<CopStatisticsDto> copStatistics(CopStatisticsDto copStatisticsDto) throws Exception;

    /**
     * 完成率排名
     * @param copStatisticsDto
     * @return
     * @throws Exception
     */
    List<CopPlanRank> copPlanRank(WSActionStatisticsDto copStatisticsDto) throws Exception;

    /**
     * 车间统计
     * @param copStatisticsDto
     * @return
     * @throws Exception
     */
    List<CopPlanRank> copLineStatistics(WSActionStatisticsDto copStatisticsDto) throws Exception;
}
