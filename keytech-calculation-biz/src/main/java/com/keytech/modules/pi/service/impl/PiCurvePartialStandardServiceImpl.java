package com.keytech.modules.pi.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.keytech.modules.pi.entity.PiAdapterUnit;
import com.keytech.modules.pi.entity.PiCurvePartialStandard;
import com.keytech.modules.pi.entity.PiCurveStandardRecipe;
import com.keytech.modules.pi.mapper.PiCurvePartialStandardMapper;
import com.keytech.modules.pi.service.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.util.ImportExcelUtil;
import org.jeecgframework.poi.excel.ExcelImportUtil;
import org.jeecgframework.poi.excel.entity.ImportParams;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import com.keytech.base.service.impl.BaseServiceImpl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @Description: 曲线局部标准配置
 * @Author: jeecg-boot
 * @Date:   2026-04-16
 * @Version: V1.0
 */
@Service
@Slf4j
public class PiCurvePartialStandardServiceImpl extends BaseServiceImpl<PiCurvePartialStandardMapper, PiCurvePartialStandard> implements IPiCurvePartialStandardService {

    @Autowired
    private IPiCurveStandardRecipeService piCurveStandardRecipeService;
    @Autowired
    private IPiAdapterUnitService piAdapterUnitService;

    @Override
    public Result<?> importExcel(HttpServletRequest request, HttpServletResponse response, Integer dataSource) throws IOException {
        MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest) request;
        Map<String, MultipartFile> fileMap = multipartRequest.getFileMap();
        for (Map.Entry<String, MultipartFile> entity : fileMap.entrySet()) {
            MultipartFile file = entity.getValue();
            ImportParams params = new ImportParams();
            params.setTitleRows(2);
            params.setHeadRows(1);
            params.setNeedSave(true);
            List<PiCurvePartialStandard> list;
            try {
                list = ExcelImportUtil.importExcel(file.getInputStream(), PiCurvePartialStandard.class, params);
            } catch (Exception e) {
                log.error("Excel解析失败", e);
                return Result.error("Excel解析失败：" + e.getMessage());
            }
            // 填充前台传入的dataSource
            if (dataSource != null) {
                list.forEach(item -> item.setDataSource(dataSource));
            }
            // 按 id 是否为空分流：有id更新，无id新增
            List<PiCurvePartialStandard> updateList = list.stream().filter(item -> StrUtil.isNotBlank(item.getId())).toList();
            List<PiCurvePartialStandard> saveList = list.stream().filter(item -> StrUtil.isBlank(item.getId())).toList();
            if (CollUtil.isNotEmpty(updateList)) {
                this.updateBatchById(updateList);
            }
            if (CollUtil.isNotEmpty(saveList)) {
                this.saveBatch(saveList);
            }
        }
        return Result.OK("导入成功！");
    }

}
