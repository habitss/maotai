package com.keytech.modules.period.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.keytech.modules.period.entity.PeriodVerificationConfig;
import com.keytech.modules.period.entity.PeriodVerificationDevice;
import com.keytech.modules.period.mapper.PeriodVerificationConfigMapper;
import com.keytech.modules.period.service.IPeriodVerificationConfigService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.util.ImportExcelUtil;
import org.jeecgframework.poi.excel.ExcelImportUtil;
import org.jeecgframework.poi.excel.entity.ImportParams;
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
 * @Description: 期间核查填报项配置
 * @Author: jeecg-boot
 * @Date:   2025-03-10
 * @Version: V1.0
 */
@Service
public class PeriodVerificationConfigServiceImpl extends BaseServiceImpl<PeriodVerificationConfigMapper, PeriodVerificationConfig> implements IPeriodVerificationConfigService {

    @Override
    public Result<?> importExcelBy(HttpServletRequest request, HttpServletResponse response, Class<PeriodVerificationConfig> clazz) throws IOException {
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
                List<PeriodVerificationConfig> list = ExcelImportUtil.importExcel(file.getInputStream(), clazz, params);

                //获取上传文件中有id的原数据
                LambdaQueryWrapper<PeriodVerificationConfig> queryWrapper = new LambdaQueryWrapper<>();
                queryWrapper.in(PeriodVerificationConfig::getId, list.stream().map(PeriodVerificationConfig::getId).collect(Collectors.toList()));
                //操作数据
                List<PeriodVerificationConfig> saveList = new ArrayList<>();
                int lineNumber = 2;
                for(PeriodVerificationConfig template : list){
                    lineNumber++;

                    if(StrUtil.isBlank(template.getDeptCode())){
                        errorMessage.add("第" + lineNumber + "行，部门Code为空或不存在");
                        errorLines++;
                        continue;
                    }
                    if(StrUtil.isBlank(template.getLineCode())){
                        errorMessage.add("第" + lineNumber + "行，产线Code为空或不存在");
                        errorLines++;
                        continue;
                    }
                    if(StrUtil.isBlank(template.getFieldName())){
                        errorMessage.add("第" + lineNumber + "行，字段名称为空或不存在");
                        errorLines++;
                        continue;
                    }
                    if(StrUtil.isBlank(template.getFieldType())){
                        errorMessage.add("第" + lineNumber + "行，字段类型为空或不存在");
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
                    saveList.add(template);
                    successLines++;
                }
                this.saveOrUpdateBatch(saveList);
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
