package com.keytech.modules.meeting.service;

import com.keytech.base.service.BaseService;
import com.keytech.modules.meeting.entity.MeetingTemplate;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jeecg.common.api.vo.Result;

import java.io.IOException;
import java.util.List;

/**
 * @Description: 会议模版
 * @Author: qipengbo
 * @Date:   2025-02-28
 * @Version: V1.0
 */
public interface IMeetingTemplateService extends BaseService<MeetingTemplate> {

    /**
     * 根据会议类型获取会议模版列表
     * @param meetingType
     * @return
     */
    List<MeetingTemplate> getMeetingTemplates(String meetingType);

    /**
     * 自定义导入
     */
    Result<?> importExcelBy(HttpServletRequest request, HttpServletResponse response, Class<MeetingTemplate> clazz) throws IOException;
}
