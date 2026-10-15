package com.keytech.modules.period.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.keytech.modules.cop.dto.CopPlanRank;
import com.keytech.modules.home.dto.WSActionStatisticsDto;
import com.keytech.modules.period.dto.PeriodScheduleVO;
import com.keytech.modules.period.dto.PeriodStatisticsDto;
import com.keytech.modules.period.entity.PeriodVerificationSchedule;
import com.keytech.base.service.BaseService;

import java.util.List;

/**
 * @Description: 期间核查排期
 * @Author: jeecg-boot
 * @Date:   2025-03-11
 * @Version: V1.0
 */
public interface IPeriodVerificationScheduleService extends BaseService<PeriodVerificationSchedule> {

    /**
     * 查询排期列表
     * @param periodScheduleVO
     * @param months
     * @param pageNo
     * @param pageSize
     * @return
     */
    Page<PeriodVerificationSchedule> findPage(PeriodVerificationSchedule periodScheduleVO, List<String> months, Integer pageNo, Integer pageSize);

    /**
     * 处理排期
     * @param periodScheduleVO
     * @return
     */
    PeriodVerificationSchedule handlePeriodSchedule(PeriodScheduleVO periodScheduleVO);

    /**
     * 取消排期
     * @param periodScheduleVO
     * @return
     */
    PeriodVerificationSchedule handleCancelPeriodSchedule(PeriodScheduleVO periodScheduleVO);

    /**
     * 根据填报修改排期状态
     *
     * @param num
     * @param status
     * @return
     */
    PeriodVerificationSchedule processPeriodSchedule(String id, int num,String status);

    /**
     * 排期页面配置更新
     * @param periodScheduleVO
     * @throws Exception
     */
    void updatePeriod(List<PeriodScheduleVO> periodScheduleVO)throws Exception;

    List<PeriodStatisticsDto> periodStatistics(PeriodStatisticsDto periodStatisticsDto);

    /**
     * 完成率排名
     * @param periodStatisticsDto
     * @return
     * @throws Exception
     */
    List<CopPlanRank> periodPlanRank(WSActionStatisticsDto periodStatisticsDto) throws Exception;

    /**
     * 车间统计
     * @param periodStatisticsDto
     * @return
     * @throws Exception
     */
    List<CopPlanRank> periodLineStatistics(WSActionStatisticsDto periodStatisticsDto) throws Exception;

}
