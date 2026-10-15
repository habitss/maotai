package com.keytech.modules.meeting.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.keytech.modules.meeting.entity.MeetingConfig;
import com.keytech.modules.meeting.entity.MeetingTemplate;
import com.keytech.modules.meeting.mapper.MeetingTemplateMapper;
import com.keytech.modules.meeting.service.IMeetingConfigService;
import com.keytech.modules.meeting.service.IMeetingTemplateService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.lang3.StringUtils;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.util.ImportExcelUtil;
import org.jeecgframework.poi.excel.ExcelImportUtil;
import org.jeecgframework.poi.excel.entity.ImportParams;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.keytech.base.service.impl.BaseServiceImpl;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @Description: 会议模版
 * @Author: jeecg-boot
 * @Date:   2025-02-28
 * @Version: V1.0
 */
@Service
public class MeetingTemplateServiceImpl extends BaseServiceImpl<MeetingTemplateMapper, MeetingTemplate> implements IMeetingTemplateService {

    @Autowired
    private IMeetingConfigService iMeetingConfigService;

    @Override
    public List<MeetingTemplate> getMeetingTemplates(String meetingType) {
        return this.list(new LambdaQueryWrapper<MeetingTemplate>().eq(MeetingTemplate::getMeetingType, meetingType));
    }

    @Override
    public Result<?> importExcelBy(HttpServletRequest request, HttpServletResponse response, Class<MeetingTemplate> clazz) throws IOException {
        //获取导入文件
        MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest) request;
        Map<String, MultipartFile> fileMap = multipartRequest.getFileMap();

        //错误信息集合
        List<String> errorMessage = new ArrayList<>();
        //成功数，错误数
        int successLines = 0, errorLines = 0;
        for (Map.Entry<String, MultipartFile> entity : fileMap.entrySet()) {
            // 获取上传文件对象
            MultipartFile file = entity.getValue();
            ImportParams params = new ImportParams();
            params.setTitleRows(2);
            params.setHeadRows(2);
            params.setNeedSave(true);
            try {
                //获取上传文件中的数据
                List<MeetingTemplate> list = ExcelImportUtil.importExcel(file.getInputStream(), clazz, params);
                //操作数据
                int lineNumber = 3;
                for(MeetingTemplate template : list){
                    lineNumber++;
                    int deptNum = 0;
                    if(StrUtil.isNotBlank(template.getFactoryCode())){
                        deptNum++;
                    }
                    if(StrUtil.isNotBlank(template.getDeptCode())){
                        deptNum++;
                    }
                    if(StrUtil.isNotBlank(template.getWorkshop())){
                        deptNum++;
                    }
                    if(deptNum != 1){
                        errorMessage.add("第" + lineNumber + "行，工厂、部门、车间请填写其中一个");
                        errorLines++;
                        continue;
                    }
                    MeetingConfig meetingConfig;
                    if(StringUtils.isNotEmpty(template.getMeetingType()) && !template.getMeetingType().matches("^[-+]?((\\d+\\.?\\d*)|(\\.\\d+))([eE][-+]?\\d+)?$")){
                        //根据会议类型查询，如果不存在为新增
                         meetingConfig = iMeetingConfigService.getOne(new LambdaQueryWrapper<MeetingConfig>().eq(MeetingConfig::getMeetingType, template.getMeetingType()).eq(StringUtils.isNotBlank(template.getDeptCode()), MeetingConfig::getDeptCode, template.getDeptCode()).eq(StringUtils.isNotBlank(template.getLineCode()), MeetingConfig::getLineCode, template.getLineCode()).eq(StringUtils.isNotBlank(template.getFactoryCode()), MeetingConfig::getFactoryCode, template.getFactoryCode()).eq(StringUtils.isNotBlank(template.getDeptCode()), MeetingConfig::getDeptCode, template.getDeptCode()));
                        if (null == meetingConfig) {
                            meetingConfig = new MeetingConfig();
                            meetingConfig.setMeetingType(template.getMeetingType());
                            meetingConfig.setMeetingName(template.getMeetingName());
                            meetingConfig.setParticipant(template.getParticipant());
                            meetingConfig.setDeptCode(template.getDeptCode());
                            meetingConfig.setWorkshop(template.getWorkshop());
                            meetingConfig.setLineCode(template.getLineCode());
                            meetingConfig.setFactoryCode(template.getFactoryCode());
                            iMeetingConfigService.save(meetingConfig);
                        }
                        template.setMeetingType(meetingConfig.getId());
                    }
                    if(StrUtil.isBlank(template.getMeetingType())){
                        errorMessage.add("第" + lineNumber + "行，会议类型为空或不存在");
                        errorLines++;
                        continue;
                    }
                    if(StrUtil.isBlank(template.getMeetingName())){
                        errorMessage.add("第" + lineNumber + "行，会议名称为空或不存在");
                        errorLines++;
                        continue;
                    }

                    if(StrUtil.isBlank(template.getContentTitle()) ){
                        errorMessage.add("第" + lineNumber + "行，会议主题为空");
                        errorLines++;
                        continue;
                    }
                    if(StrUtil.isBlank(template.getContent()) ){
                        errorMessage.add("第" + lineNumber + "行，会议内容为空");
                        errorLines++;
                        continue;
                    }
                    if(null == template.getSort()){
                        errorMessage.add("第" + lineNumber + "行，排序为空");
                        errorLines++;
                        continue;
                    }

                    //情况创建、修改人不让导入。使用默认值
                    template.setCreateBy(null);
                    template.setUpdateBy(null);
                    template.setUpdateTime(null);
                    this.saveOrUpdate(template);
                    successLines++;
                }
            } catch (Exception e) {
                log.error(e.getMessage(), e);
                errorLines++;
                errorMessage.add("发生异常：" + e.getMessage());
            } finally {
                try {
                    file.getInputStream().close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return ImportExcelUtil.imporReturnRes(errorLines, successLines, errorMessage);
    }
}
