package com.keytech.modules.cop.service.impl;


import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.keytech.base.service.impl.BaseServiceImpl;
import com.keytech.modules.cop.entity.CopConfig;
import com.keytech.modules.cop.entity.CopDevice;
import com.keytech.modules.cop.entity.CopPoint;
import com.keytech.modules.cop.mapper.CopDeviceMapper;
import com.keytech.modules.cop.service.ICopConfigService;
import com.keytech.modules.cop.service.ICopDeviceService;
import com.keytech.modules.cop.service.ICopPointService;
import com.keytech.modules.period.entity.PeriodVerificationDevice;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.logging.log4j.util.Strings;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.system.query.QueryGenerator;
import org.jeecg.common.util.ImportExcelUtil;
import org.jeecgframework.poi.excel.ExcelImportUtil;
import org.jeecgframework.poi.excel.entity.ImportParams;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * <p>
 * cop设备管理 服务实现类
 * </p>
 *
 * @author lijun
 * @since 2025-02-18 02:08:08
 */
@Slf4j
@Service
public class CopDeviceServiceImpl extends BaseServiceImpl<CopDeviceMapper, CopDevice> implements ICopDeviceService {
    @Autowired
    private ICopPointService copPointService;
    @Autowired
    private CopDeviceMapper copDeviceMapper;
    @Autowired
    private ICopConfigService iCopConfigService;

    @Override
    public Page<CopDevice> listData(CopDevice copDevice, Integer pageNo, Integer pageSize, String orderField, String orderType, String selectFields, HttpServletRequest req) {
        QueryWrapper<CopDevice> queryWrapper = QueryGenerator.initQueryWrapper(copDevice, req.getParameterMap());
        if (Strings.isNotEmpty(selectFields)) {
            List<String> selectFieldList = (List) Arrays.stream(selectFields.split(",")).map(CharSequenceUtil::toUnderlineCase).collect(Collectors.toList());
            queryWrapper.select(selectFieldList);
        }

        if (Strings.isNotEmpty(orderType) && Strings.isNotEmpty(orderField)) {
            if ("desc".equals(orderType)) {
                queryWrapper.orderByDesc(StrUtil.toUnderlineCase(orderField));
            }

            if ("asc".equals(orderType)) {
                queryWrapper.orderByAsc(StrUtil.toUnderlineCase(orderField));
            }
        }

        queryWrapper.eq("tenant_id", req.getIntHeader("X-Tenant-Id"));
        Page<CopDevice> page = new Page((long)pageNo, (long)pageSize);
        page = this.copDeviceMapper.selectPage(page, queryWrapper);
        if (CollectionUtils.isNotEmpty(page.getRecords())) {
            page = this.buildCopPoint(page);
        }
        return page;
    }

    @Override
    public Result<?> importExcelBy(HttpServletRequest request, HttpServletResponse response, Class<CopDevice> clazz) throws IOException {
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
                List<CopDevice> list = ExcelImportUtil.importExcel(file.getInputStream(), clazz, params);

                //获取上传文件中有id的原数据
                LambdaQueryWrapper<CopDevice> queryWrapper = new LambdaQueryWrapper<>();
                queryWrapper.in(CopDevice::getId, list.stream().map(CopDevice::getId).collect(Collectors.toList()));
                //操作数据
                List<CopDevice> saveList = new ArrayList<>();
                int lineNumber = 2;
                for(CopDevice template : list){
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
                    if(StrUtil.isBlank(template.getCopDeviceName())){
                        errorMessage.add("第" + lineNumber + "行，设备名称为空或不存在");
                        errorLines++;
                        continue;
                    }
//                    if(null == template.getSort()){
//                        errorMessage.add("第" + lineNumber + "行，排序为空");
//                        errorLines++;
//                        continue;
//                    }

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

    /**
     * 填充copPoint
     * @param page
     * @return
     */
    private Page<CopDevice> buildCopPoint(Page<CopDevice> page) {
        List<String> deviceIds = page.getRecords().stream().map(CopDevice::getId).collect(Collectors.toList());
        List<CopPoint> list = copPointService.list(new LambdaQueryWrapper<CopPoint>().in(CopPoint::getCopDeviceId, deviceIds));
        if (CollectionUtils.isNotEmpty(list)) {
            //设备id分组排序
            LinkedHashMap<String, List<CopPoint>> copGroup = list.stream().sorted(Comparator.comparing(CopPoint::getSort))
                    .collect(Collectors.groupingBy(CopPoint::getCopDeviceId, LinkedHashMap::new, Collectors.toList()));
            for (CopDevice record : page.getRecords()) {
                if (CollectionUtils.isNotEmpty(copGroup.get(record.getId()))) {
                    record.setCopPointList(copGroup.get(record.getId()));
                }
            }
        }
        return page;
    }

    @Override
    public void deleteData(String id) {
        //检查是否被绑定
        List<String> pointIds = copPointService.list(new LambdaQueryWrapper<CopPoint>().eq(CopPoint::getCopDeviceId, id)).stream().map(CopPoint::getId).toList();
        if(CollectionUtils.isNotEmpty(pointIds)) {
            List<String> configIds = iCopConfigService.list(new LambdaQueryWrapper<CopConfig>().in(CopConfig::getCopPointId, pointIds)).stream().map(CopConfig::getId).toList();
            if(CollectionUtils.isNotEmpty(configIds)){
                iCopConfigService.removeBatchByIds(configIds);
            }
            copPointService.removeBatchByIds(pointIds);
        }
        copDeviceMapper.deleteById(id);
    }

    @Override
    public void removeByIds(List<String> ids) {
        List<String> pointIds = copPointService.list(new LambdaQueryWrapper<CopPoint>().in(CopPoint::getCopDeviceId, ids)).stream().map(CopPoint::getId).toList();
        if(CollectionUtils.isNotEmpty(pointIds)) {
            List<String> configIds = iCopConfigService.list(new LambdaQueryWrapper<CopConfig>().in(CopConfig::getCopPointId, pointIds)).stream().map(CopConfig::getId).toList();
            if (CollectionUtils.isNotEmpty(configIds)) {
                iCopConfigService.removeBatchByIds(configIds);
            }
            copPointService.removeBatchByIds(pointIds);
        }
        copDeviceMapper.deleteBatchIds(ids);
    }
}
