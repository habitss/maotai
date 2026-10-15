package com.keytech.modules.meeting.service;

import com.keytech.modules.meeting.entity.MeetingRecordTemplate;
import com.keytech.base.service.BaseService;
import com.keytech.modules.period.entity.PeriodVerificationDevice;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jeecg.common.api.vo.Result;

import java.io.IOException;

/**
 * @Description: 会议评估模版
 * @Author: jeecg-boot
 * @Date:   2025-03-19
 * @Version: V1.0
 */
public interface IMeetingRecordTemplateService extends BaseService<MeetingRecordTemplate> {

    /**
     * 自定义导入
     */
    Result<?> importExcelBy(HttpServletRequest request, HttpServletResponse response, Class<MeetingRecordTemplate> clazz) throws IOException;
}
