package com.keytech.modules.cop.service.impl;


import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.keytech.base.service.impl.BaseServiceImpl;
import com.keytech.modules.cop.entity.CopConfig;
import com.keytech.modules.cop.entity.CopDevice;
import com.keytech.modules.cop.entity.CopPoint;
import com.keytech.modules.cop.mapper.CopPointMapper;
import com.keytech.modules.cop.service.ICopConfigService;
import com.keytech.modules.cop.service.ICopDeviceService;
import com.keytech.modules.cop.service.ICopPointService;
import com.keytech.modules.mdm3.entity.MdmWeek;
import com.keytech.modules.mdm3.service.impl.MdmWeekServiceImpl;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.util.Strings;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.exception.JeecgBootException;
import org.jeecg.common.system.query.QueryGenerator;
import org.jeecg.common.util.ImportExcelUtil;
import org.jeecgframework.poi.excel.ExcelImportUtil;
import org.jeecgframework.poi.excel.entity.ImportParams;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * <p>
 * cop清洗点 服务实现类
 * </p>
 *
 * @author lijun
 * @since 2025-02-18 04:42:21
 */
@Service
public class CopPointServiceImpl extends BaseServiceImpl<CopPointMapper, CopPoint> implements ICopPointService {
    @Autowired
    private CopPointMapper copPointMapper;
    @Autowired
    private ICopConfigService iCopConfigService;
    @Autowired
    private ICopDeviceService iCopDeviceService;

    @Override
    public Page<CopPoint> listData(CopPoint copPoint, Integer pageNo, Integer pageSize, String orderField, String orderType, String selectFields, HttpServletRequest req) {
        QueryWrapper<CopPoint> queryWrapper = QueryGenerator.initQueryWrapper(copPoint, req.getParameterMap());
        if (Strings.isNotEmpty(selectFields)) {
            List<String> selectFieldList = Arrays.stream(selectFields.split(",")).map(CharSequenceUtil::toUnderlineCase).collect(Collectors.toList());
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
        Page<CopPoint> page = new Page((long) pageNo, (long) pageSize);
        page = this.copPointMapper.selectPage(page, queryWrapper);
//        List<MdmWeek> weekList = null;
//        String[] monthRequest = req.getParameterMap().get("monthField");
//        if (monthRequest != null && monthRequest.length > 0 && Strings.isNotEmpty(monthRequest[0])){
//            String month = req.getParameterMap().get("monthField")[0];
//            if (Strings.isNotEmpty(month)) {
//                String[] monthArr = month.split("-");
//                weekList = mdmWeekService.getWeek(new MdmWeek().setYear(monthArr[0]).setMonth(monthArr[1].replaceFirst("^0+(?!$)", "")));
//            }
//        }
        if (CollectionUtils.isNotEmpty(page.getRecords())) {
            page = this.buildCopConfig(page);
        }
        return page;
    }

    @Override
    public List<CopPoint> listData(CopPoint copPoint, HttpServletRequest req) {
        QueryWrapper<CopPoint> queryWrapper = QueryGenerator.initQueryWrapper(copPoint, request.getParameterMap());
        if(CollectionUtils.isEmpty(copPoint.getDeivceIdList())){
            queryWrapper.in("cop_device_id", copPoint.getDeivceIdList());
        }
        return this.list(queryWrapper);
    }

    @Override
    @Transactional
    public CopPoint savePoint(CopPoint copPoint) {
        // 判断cop清洗点名称是否重复
        long count = this.count(new LambdaQueryWrapper<CopPoint>().eq(CopPoint::getDeptCode, copPoint.getDeptCode()).eq(CopPoint::getLineCode, copPoint.getLineCode()).eq(CopPoint::getCopDeviceId, copPoint.getCopDeviceId()).eq(CopPoint::getPointName, copPoint.getPointName()));
        if(count < 1) {
            //根据cop设备是否可用设置清洗点是否可用
            CopDevice copDevice = iCopDeviceService.getById(copPoint.getCopDeviceId());
            if(null != copDevice) {
                copPoint.setActive(copDevice.getActive());
            }
            //保存清洗点
            this.save(copPoint);
            //==同步更新设备清洗点==
            getPointConfig(copPoint);
        } else {
            throw new JeecgBootException("新增失败，请检查该设备清洗点是否已经存在 " + copPoint.getPointName());
        }
        return copPoint;
    }

    @Override
    public Result<?> importExcelBy(HttpServletRequest request, HttpServletResponse response, Class<CopPoint> clazz) throws IOException {
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
                List<CopPoint> list = ExcelImportUtil.importExcel(file.getInputStream(), clazz, params);

                //获取上传文件中有id的原数据

                //操作数据
               // List<CopPoint> saveList = new ArrayList<>();
                int lineNumber = 2;
                for(CopPoint template : list){
                    lineNumber++;
                    if(StringUtils.isNotEmpty(template.getCopDeviceId()) && template.getCopDeviceId().matches("^[-+]?((\\d+\\.?\\d*)|(\\.\\d+))([eE][-+]?\\d+)?$")){
                        CopDevice device =iCopDeviceService.getById(template.getCopDeviceId());
                        if(null == device){
                            //根据设备查询，如果不存在为新增
                            CopDevice copDevice = iCopDeviceService.getOne(new LambdaQueryWrapper<CopDevice>().eq(CopDevice::getDeptCode, template.getDeptCode()).eq(CopDevice::getLineCode, template.getLineCode()).eq(CopDevice::getCopDeviceName, template.getCopDeviceName()));
                            if(null == copDevice){
                                copDevice = new CopDevice();
                                copDevice.setCopDeviceName(template.getCopDeviceName());
                                copDevice.setDeptCode(template.getDeptCode());
                                copDevice.setLineCode(template.getLineCode());
                                iCopDeviceService.save(copDevice);
                            }
                        } else {
                            template.setCopDeviceName(device.getCopDeviceName());
                        }
                    }
                    //根据设备查询，如果不存在为新增
                    CopDevice copDevice = iCopDeviceService.getOne(new LambdaQueryWrapper<CopDevice>().eq(CopDevice::getDeptCode, template.getDeptCode()).eq(CopDevice::getLineCode, template.getLineCode()).eq(CopDevice::getCopDeviceName, template.getCopDeviceName()));
                    //.or().eq(CopDevice::getId, template.getCopDeviceId()));
                    if(null == copDevice){
                        copDevice = new CopDevice();
                        copDevice.setCopDeviceName(template.getCopDeviceName());
                        copDevice.setDeptCode(template.getDeptCode());
                        copDevice.setLineCode(template.getLineCode());
                        iCopDeviceService.save(copDevice);
                    }
                    template.setCopDeviceId(copDevice.getId());
                    template.setCopDeviceName(copDevice.getCopDeviceName());
                    if(StrUtil.isBlank(template.getDeptCode())|| template.getDeptCode().matches("^[\\u4e00-\\u9fa5]+$")){
                        errorMessage.add("第" + lineNumber + "行，部门Code为空或不存在");
                        errorLines++;
                        continue;
                    }
                    if(StrUtil.isBlank(template.getLineCode())|| template.getLineCode().matches("^[\\u4e00-\\u9fa5]+$")){
                        errorMessage.add("第" + lineNumber + "行，产线Code为空或不存在");
                        errorLines++;
                        continue;
                    }
                    if(StrUtil.isBlank(template.getCopDeviceId())){
                        errorMessage.add("第" + lineNumber + "行，设备名称为空或不存在");
                        errorLines++;
                        continue;
                    }
                    if(StrUtil.isBlank(template.getPointName())){
                        errorMessage.add("第" + lineNumber + "行，清洗点名称为空或不存在");
                        errorLines++;
                        continue;
                    }
                    if(StrUtil.isBlank(template.getFrequency()) || !template.getFrequency().matches("^[-+]?((\\d+\\.?\\d*)|(\\.\\d+))([eE][-+]?\\d+)?$")){
                        errorMessage.add("第" + lineNumber + "行，频次为空或不存在");
                        errorLines++;
                        continue;
                    }
                    if(StrUtil.isBlank(template.getCleaningStep())){
                        errorMessage.add("第" + lineNumber + "行，清洗步骤为空或不存在");
                        errorLines++;
                        continue;
                    }
                    if(StrUtil.isBlank(template.getCleaningStandard())){
                        errorMessage.add("第" + lineNumber + "行，清洗标准为空或不存在");
                        errorLines++;
                        continue;
                    }
                    if(StrUtil.isEmpty(template.getFormula())){
                        template.setFormula(null);
                    }


                    //情况创建、修改人不让导入。使用默认值
                    template.setCreateBy(null);
                    template.setUpdateBy(null);
                    template.setUpdateTime(null);
                    boolean flag = Strings.isEmpty(template.getId());
                    //saveList.add(template);
                    if(flag){
                        long count =this.count(new LambdaQueryWrapper<CopPoint>().eq(CopPoint::getCopDeviceId, template.getCopDeviceId()).eq(CopPoint::getPointName, template.getPointName()));
                        if(count < 1) {
                            this.save(template);
                            // 同步更新设备清洗点
                            getPointConfig(template);
                        }
                    } else{
                        updateById(template);
                    }
                    successLines++;
                }
              //  this.saveOrUpdateBatch(saveList);
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
     * 填充清洗点配置项
     * @param page
     * @return
     */
    private Page<CopPoint> buildCopConfig(Page<CopPoint> page) {
        List<String> pointIds = page.getRecords().stream().map(CopPoint::getId).collect(Collectors.toList());
        List<CopConfig> list = iCopConfigService.list(new LambdaQueryWrapper<CopConfig>().in(CopConfig::getCopPointId, pointIds));
        if (CollectionUtils.isNotEmpty(list)) {

            //设备id分组排序
            LinkedHashMap<String, List<CopConfig>> copConfigGroup = list.stream().sorted(Comparator.comparing(CopConfig::getSort))
                    .collect(Collectors.groupingBy(CopConfig::getCopPointId, LinkedHashMap::new, Collectors.toList()));
            for (CopPoint record : page.getRecords()) {
                if (CollectionUtils.isNotEmpty(copConfigGroup.get(record.getId()))) {
                    record.setCopConfigList(copConfigGroup.get(record.getId()));
                }
            }
        }
//            page.getRecords().forEach(record -> {
//                record.setWeekList(weekList);
//            });

        return page;
    }

    @Override
    public void deleteData(String id) {
        //检查是否被绑定
        List<String> configIds = iCopConfigService.list(new LambdaQueryWrapper<CopConfig>().eq(CopConfig::getCopPointId, id)).stream().map(CopConfig::getId).toList();
        if(CollectionUtils.isNotEmpty(configIds)){
            iCopConfigService.removeBatchByIds(configIds);
        }
        copPointMapper.deleteById(id);
    }

    private void getPointConfig(CopPoint copPoint){
        List<CopConfig> newConfigList = new ArrayList<>();
        //查询当前设备，非该条新增清晰点的清洗点数据
        List<CopPoint> copPointList = this.lambdaQuery()
                .eq(CopPoint::getCopDeviceId, copPoint.getCopDeviceId())
                .ne(CopPoint::getId, copPoint.getId())
                .orderByAsc(CopPoint::getCreateTime)
                .list();
        if(CollectionUtils.isNotEmpty(copPointList)) {
            //根据清洗点获取清洗信息配置
            List<CopConfig> configList = iCopConfigService.lambdaQuery()
                    .eq(CopConfig::getCopPointId, copPointList.get(0)
                            .getId()).list();
            for(CopConfig config : configList){
                config.setCopPointId(copPoint.getId());
                config.setId(null);
                config.setCreateBy(null);
                config.setCreateTime(null);
                config.setUpdateBy(null);
                config.setUpdateTime(null);
                newConfigList.add(config);
            }
        } else {
            //如果没有清洗点，则新增默认配置
            for(int i = 0; i < 3; i++){
                CopConfig copConfig = new CopConfig();
                BeanUtils.copyProperties(copPoint, copConfig);
                copConfig.setId(null);
                copConfig.setCopPointId(copPoint.getId());
                copConfig.setFieldName(i == 0 ? "清理前照片" : "清理后照片"+i);
                copConfig.setFieldType("4");
                copConfig.setSort(1.0);
                newConfigList.add(copConfig);
            }
        }
        iCopConfigService.saveBatch(newConfigList);
    }
}
