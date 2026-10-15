package com.keytech.modules.cop.service;

import com.keytech.modules.cop.dto.CopScheduleJson;
import com.keytech.modules.cop.dto.CopSubmitDto;
import com.keytech.modules.cop.dto.CopSubmitJson;
import com.keytech.modules.cop.entity.CopSchedule;
import com.keytech.modules.cop.entity.CopSubmit;
import com.keytech.base.service.BaseService;
import com.keytech.modules.period.dto.PeriodScheduleSubmitVO;
import com.keytech.modules.period.entity.PeriodVerificationSubmit;
import org.jeecg.common.api.vo.Result;

import java.util.List;

/**
 * <p>
 * cop填报 服务类
 * </p>
 *
 * @author lijun
 * @since 2025-02-20 02:19:51
 */
public interface ICopSubmitService extends BaseService<CopSubmit> {
    /**
     * Json转对象
     * @param copSubmit
     * @return
     * @throws Exception
     */
    List<CopSubmitJson> initSubmitJsonList(CopSubmit copSubmit) throws Exception;

    /**
     * list数据填充json数据成数组对象
     * @param list
     * @return
     */
    List<CopSubmitDto> initSubmitJson(List<CopSubmit> list);

    /**
     * 查询提交记录
     * @param copSubmitDto
     * @return
     */
    CopSubmit findSubmitByParam(CopSubmitDto copSubmitDto);

    void scheduleTask();
}
