package com.keytech.modules.period.service;

import com.keytech.modules.period.dto.PeriodScheduleSubmitVO;
import com.keytech.modules.period.entity.PeriodVerificationSubmit;
import com.keytech.base.service.BaseService;

/**
 * @Description: 期间排查排查内容提交
 * @Author: jeecg-boot
 * @Date:   2025-03-12
 * @Version: V1.0
 */
public interface IPeriodVerificationSubmitService extends BaseService<PeriodVerificationSubmit> {

    PeriodVerificationSubmit findSubmitByParam(PeriodScheduleSubmitVO periodScheduleSubmitVO);

    /**
     * 计划执行情况定时任务
     */
    void scheduleTask();
}
