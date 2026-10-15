package com.keytech.modules.period.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.keytech.modules.cop.entity.CopPoint;
import com.keytech.modules.meeting.entity.MeetingTemplate;
import com.keytech.modules.period.entity.PeriodVerificationConfig;
import com.keytech.modules.period.entity.PeriodVerificationDevice;
import com.keytech.modules.period.mapper.PeriodVerificationDeviceMapper;
import com.keytech.modules.period.service.IPeriodVerificationConfigService;
import com.keytech.modules.period.service.IPeriodVerificationDeviceService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.logging.log4j.util.Strings;
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
 * @Description: 期间核查设备信息
 * @Author: jeecg-boot
 * @Date:   2025-03-09
 * @Version: V1.0
 */
@Service
public class PeriodVerificationDeviceServiceImpl extends BaseServiceImpl<PeriodVerificationDeviceMapper, PeriodVerificationDevice> implements IPeriodVerificationDeviceService {

    @Autowired
    private PeriodVerificationDeviceMapper periodVerificationDeviceMapper;

    @Autowired
    private IPeriodVerificationConfigService periodVerificationConfigService;

    @Override
    public void addPeriodVerificationConfig(List<PeriodVerificationConfig> list, PeriodVerificationDevice periodVerificationDevice, String fieldType, String fieldName, double sort) {
        PeriodVerificationConfig config = new PeriodVerificationConfig();
        config.setApparatusId(periodVerificationDevice.getId());
        config.setDeptCode(periodVerificationDevice.getDeptCode());
        config.setLineCode(periodVerificationDevice.getLineCode());
        config.setFieldType(fieldType);
        config.setFieldName(fieldName);
        config.setSort(sort);
        list.add(config);
    }

    @Override
    public Result<?> importExcelBy(HttpServletRequest request, HttpServletResponse response, Class<PeriodVerificationDevice> clazz) throws IOException {
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
                List<PeriodVerificationDevice> list = ExcelImportUtil.importExcel(file.getInputStream(), clazz, params);

                //获取上传文件中有id的原数据
                LambdaQueryWrapper<PeriodVerificationDevice> queryWrapper = new LambdaQueryWrapper<>();
                queryWrapper.in(PeriodVerificationDevice::getId, list.stream().map(PeriodVerificationDevice::getId).collect(Collectors.toList()));
                //操作数据
                int lineNumber = 2;
                for(PeriodVerificationDevice template : list){
                    lineNumber++;

                    if(StrUtil.isBlank(template.getDeptCode()) || template.getDeptCode().matches("^[\\u4e00-\\u9fa5]+$")){
                        errorMessage.add("第" + lineNumber + "行，部门Code为空或不存在");
                        errorLines++;
                        continue;
                    }
                    if(StrUtil.isBlank(template.getLineCode()) || template.getLineCode().matches("^[\\u4e00-\\u9fa5]+$")){
                        errorMessage.add("第" + lineNumber + "行，产线Code为空或不存在");
                        errorLines++;
                        continue;
                    }

                    if(StrUtil.isBlank(template.getApparatusName())){
                        errorMessage.add("第" + lineNumber + "行，仪器名称为空或不存在");
                        errorLines++;
                        continue;
                    }
                    if(StrUtil.isBlank(template.getFrequencyOfCheck()) || !template.getFrequencyOfCheck().matches("^[-+]?((\\d+\\.?\\d*)|(\\.\\d+))([eE][-+]?\\d+)?$")){
                        errorMessage.add("第" + lineNumber + "行，频次为空或不存在");
                        errorLines++;
                        continue;
                    }


                    //情况创建、修改人不让导入。使用默认值
                    template.setCreateBy(null);
                    template.setUpdateBy(null);
                    template.setUpdateTime(null);
                    boolean flag = Strings.isEmpty(template.getId());
                    if(flag) {
                        long count =this.count(new LambdaQueryWrapper<PeriodVerificationDevice>().eq(PeriodVerificationDevice::getDeptCode, template.getDeptCode()).eq(PeriodVerificationDevice::getLineCode, template.getLineCode()).eq(PeriodVerificationDevice::getApparatusName, template.getApparatusName()));
                        if(count < 1){
                            this.save(template);
                            List<PeriodVerificationConfig> configList = new ArrayList<>();
                            this.addPeriodVerificationConfig(configList, template, "3", "仪器显示值", 1.0);

                            this.addPeriodVerificationConfig(configList, template, "4", "仪器显示值图片", 2.0);

                            this.addPeriodVerificationConfig(configList, template, "3", "电脑显示值", 3.0);

                            this.addPeriodVerificationConfig(configList, template, "4", "电脑显示值图片", 4.0);

                            this.addPeriodVerificationConfig(configList, template, "3", "实际偏差", 5.0);
                            periodVerificationConfigService.saveBatch(configList);
                        }
                    }else{
                        updateById(template);
                    }
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

    @Override
    public void removeByIds(List<String> ids) {
        List<String> configList = periodVerificationConfigService.list(new LambdaQueryWrapper<PeriodVerificationConfig>().in(PeriodVerificationConfig::getApparatusId, ids)).stream().map(PeriodVerificationConfig::getId).toList();
        periodVerificationConfigService.removeByIds(configList);
        periodVerificationDeviceMapper.deleteBatchIds(ids);
    }
}
