package com.keytech.modules.pi.service.impl;

import cn.hutool.core.io.unit.DataUnit;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.keytech.base.service.impl.BaseServiceImpl;
import com.keytech.modules.kpi.util.DateUtils;
import com.keytech.modules.mdm3.entity.MdmDevice;
import com.keytech.modules.mdm3.entity.MdmDict;
import com.keytech.modules.mdm3.service.IMdmDeviceService;
import com.keytech.modules.mdm3.service.IMdmDictService;
import com.keytech.modules.pi.common.MeasurementData;
import com.keytech.modules.pi.common.PiCommonConstants;
import com.keytech.modules.pi.dto.OrderPointDto;
import com.keytech.modules.pi.dto.PiCurveStepDto;
import com.keytech.modules.pi.dto.TableConditionQueryDto;
import com.keytech.modules.pi.entity.*;
import com.keytech.modules.pi.mapper.PiCurveConfigMapper;
import com.keytech.modules.pi.service.*;
import com.keytech.modules.pi.util.AdvancedDateTimeCalculator;
import com.keytech.modules.pi.util.MeasurementDataGenerator;
import com.keytech.modules.pi.vo.*;
import io.swagger.v3.oas.annotations.Operation;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.aspect.annotation.AutoLog;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.text.ParseException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @Description: PI曲线配置
 * @Author: jeecg-boot
 * @Date:   2025-12-09
 * @Version: V1.0
 */
@Service
public class PiCurveConfigServiceImpl extends BaseServiceImpl<PiCurveConfigMapper, PiCurveConfig> implements IPiCurveConfigService {

    @Autowired
    private IMdmDeviceService mdmDeviceService;
    @Autowired
    private IPiCurveConfigService piCurveConfigService;
    @Autowired
    private IPiCustomizeTableService iPiCustomizeTableService;
    @Autowired
    private IPiCustomizeTableFieldService iPiCustomizeTableFieldService;
    @Autowired
    private IPiBasePointService piBasePointService;
    @Autowired
    private IPiComputationModelService iPiComputationModelService;
    @Autowired
    private IMdmDictService mdmDictService;
    @Autowired
    private IPiCurveStepService iPiCurveStepService;
    /**
     * 修改数据
     * @param piCurveConfig
     */
    @Override
    public void editData(PiCurveConfig piCurveConfig) {
        //获取报表名称
        PiCustomizeTable piCustomizeTable = null;
        if(StrUtil.isBlank(piCurveConfig.getTableName())) {
            piCustomizeTable = iPiCustomizeTableService.getById(piCurveConfig.getTableId());
            if (piCustomizeTable != null) {
                piCurveConfig.setTableName(piCustomizeTable.getTableName());
            }
        }
        //获取PI对照
        if (piCustomizeTable != null){
            List<PiCustomizeTableField> fieldList = iPiCustomizeTableFieldService.getFieldByTableId(piCustomizeTable.getId());
            //字段对照
            Map<String, String> idToFieldNameMap = fieldList.stream()
                .collect(Collectors.toMap(
                    PiCustomizeTableField::getId,      // Key: id
                    PiCustomizeTableField::getFieldName, // Value: fieldName
                    (existing, replacement) -> existing  // 如果key冲突，保留已有的
                ));
            //字段PI对照
            piCurveConfig.setEventTimePi(idToFieldNameMap.get(piCurveConfig.getEventTime()));
            piCurveConfig.setStartTimePi(idToFieldNameMap.get(piCurveConfig.getStartTime()));
            piCurveConfig.setEndTimePi(idToFieldNameMap.get(piCurveConfig.getEndTime()));
            piCurveConfig.setUnitNoPi(idToFieldNameMap.get(piCurveConfig.getUnitNo()));
            piCurveConfig.setUnitNamePi(idToFieldNameMap.get(piCurveConfig.getUnitName()));
            piCurveConfig.setRecipePi(idToFieldNameMap.get(piCurveConfig.getRecipe()));
            piCurveConfig.setOrderNoPi(idToFieldNameMap.get(piCurveConfig.getOrderNo()));
            piCurveConfig.setBatchNoPi(idToFieldNameMap.get(piCurveConfig.getBatchNo()));
            piCurveConfig.setEopNamePi(idToFieldNameMap.get(piCurveConfig.getEopName()));
            piCurveConfig.setEopNoPi(idToFieldNameMap.get(piCurveConfig.getEopNo()));
            piCurveConfig.setRecipeTypePi(idToFieldNameMap.get(piCurveConfig.getRecipeType()));

            piCurveConfig.setRecipeNoPi(idToFieldNameMap.get(piCurveConfig.getRecipeNo()));
        }

        if(StrUtil.isNotBlank(piCurveConfig.getId())){
            baseMapper.updateById(piCurveConfig);
        } else {
            baseMapper.insert(piCurveConfig);
        }
    }


    /**
     * 获取设备
     * @param piCurveStepDto
     */
    @Override
    public List<MdmDevice> getDevice(PiCurveStepDto piCurveStepDto) {
        List<MdmDevice> devices = new ArrayList<>();
        //获取设备
        LambdaQueryWrapper<MdmDevice> wrapperDevice = new LambdaQueryWrapper<>();
        wrapperDevice.eq(MdmDevice::getLineCode, piCurveStepDto.getLineCode());
        wrapperDevice.like(MdmDevice::getDeviceCode, "_");
        List<MdmDevice> deviceList = mdmDeviceService.list(wrapperDevice);
        //获取Pi曲线配置数据
        PiCurveConfig piCurveConfig = piCurveConfigService.getOne(
                new LambdaQueryWrapper<PiCurveConfig>()
                        .eq(PiCurveConfig::getCurveType, piCurveStepDto.getCurveType()),
                false  // 为false表示查询不到数据时不抛出异常，返回null
        );
        if(piCurveConfig != null){
            //获取PI表设备
            TableConditionQueryDto queryDto = new TableConditionQueryDto();
            queryDto.setTable(piCurveConfig.getTableName());
//            String condition = piCurveConfig.getEventTimePi() + " >= '" + piCurveStepDto.getStartTime() +
//                    "' AND " + piCurveConfig.getEventTimePi() + " <= '" + piCurveStepDto.getEndTime() + "'";
            String condition = "";
            if(piCurveStepDto.getStartTime() != null && piCurveStepDto.getEndTime() != null){
                String startTimeStr = DateUtils.formatDateTime(piCurveStepDto.getStartTime());
                String endTimeStr = DateUtils.formatDateTime(piCurveStepDto.getEndTime());
                condition = piCurveConfig.getEventTimePi() + " >= '" + startTimeStr +
                        "' AND " + piCurveConfig.getEventTimePi() + " <= '" + endTimeStr + "'";
            }

            queryDto.setCondition(condition);
            List<Map<String,Object>> dataList = iPiCustomizeTableService.getTableByCondition(queryDto);
            Set<String> set = new HashSet<>();
            //遍历list 得到 区域-设备-pcuset集合
            for (Map<String, Object> data: dataList) {
                set.add(String.valueOf(data.get(piCurveConfig.getUnitNoPi())));
            }
            //deviceList遍历 只保留 deviceCode 与 set 中相同的
            devices = deviceList.stream()
                .filter(device -> set.contains(device.getDeviceCode()))
                .collect(Collectors.toList());
        }
        return devices;
    }
    /**
     * 获取配方
     * @param piCurveStepDto
     */
    @Override
    public List<PiCurveStepVo> getRecipe(PiCurveStepDto piCurveStepDto) {
        List<PiCurveStepVo> list = new ArrayList<>();
        //获取Pi曲线配置数据
        PiCurveConfig piCurveConfig = piCurveConfigService.getOne(
                new LambdaQueryWrapper<PiCurveConfig>()
                        .eq(PiCurveConfig::getDeviceNo, piCurveStepDto.getDeviceNo())
                        .eq(PiCurveConfig::getAreaNo, piCurveStepDto.getAreaNo()),
                        //.eq(PiCurveConfig::getCurveType, piCurveStepDto.getCurveType()),
                false  // 为false表示查询不到数据时不抛出异常，返回null
        );

        if(piCurveConfig != null){
            //获取PI表设备
            TableConditionQueryDto queryDto = new TableConditionQueryDto();
            queryDto.setTable(piCurveConfig.getTableName());
            String condition = "";
            if(piCurveStepDto.getStartTime() != null && piCurveStepDto.getEndTime() != null){
                String startTimeStr = DateUtils.formatDateTime(piCurveStepDto.getStartTime());
                String endTimeStr = DateUtils.formatDateTime(piCurveStepDto.getEndTime());
                condition = piCurveConfig.getEventTimePi() + " >= '" + startTimeStr +
                        "' AND " + piCurveConfig.getEventTimePi() + " <= '" + endTimeStr + "'";
            }

            //设备编码
            if(StrUtil.isNotBlank(piCurveStepDto.getDeviceNo())){
                condition += " AND " + piCurveConfig.getUnitNoPi() + " = '" + piCurveStepDto.getDeviceNo() + "'";
            }
            //设备名称
//            if(StrUtil.isNotBlank(piCurveStepDto.getUnitName())){
//                condition += " AND " + piCurveConfig.getUnitNamePi() + " = '" + piCurveStepDto.getUnitName() + "'";
//            }

            queryDto.setCondition(condition);
            List<Map<String,Object>> dataList = iPiCustomizeTableService.getTableByCondition(queryDto);
            for (Map<String, Object> map : dataList) {
                PiCurveStepVo piCurveStepVo = new PiCurveStepVo();
                piCurveStepVo.setRecipe(String.valueOf(map.get(piCurveConfig.getRecipePi())));
                list.add(piCurveStepVo);
            }
        }

        //去除重复订单号
        List<PiCurveStepVo> piCurveStepVoList = list.stream()
                .collect(Collectors.toMap(
                        PiCurveStepVo::getRecipe,
                        Function.identity(),
                        (existing, replacement) -> existing
                ))
                .values()
                .stream()
                .sorted(Comparator.comparing(PiCurveStepVo::getRecipe))
                .collect(Collectors.toList());
        return piCurveStepVoList;
    }


    /**
     * 获取订单
     * @param piCurveStepDto
     */
    @Override
    public List<PiCurveStepVo> getOrder(PiCurveStepDto piCurveStepDto) {
        List<PiCurveStepVo> list = new ArrayList<>();
        //获取Pi曲线配置数据
        PiCurveConfig piCurveConfig = piCurveConfigService.getOne(
                new LambdaQueryWrapper<PiCurveConfig>()
                        .eq(PiCurveConfig::getCurveType, piCurveStepDto.getCurveType()),
                false  // 为false表示查询不到数据时不抛出异常，返回null
        );

        if(piCurveConfig != null){
            //获取PI表设备
            TableConditionQueryDto queryDto = new TableConditionQueryDto();
            queryDto.setTable(piCurveConfig.getTableName());
//            String condition = piCurveConfig.getEventTimePi() + " >= '" + piCurveStepDto.getStartTime() +
//                    "' AND " + piCurveConfig.getEventTimePi() + " <= '" + piCurveStepDto.getEndTime() + "'";
            String condition = "";
            if(piCurveStepDto.getStartTime() != null && piCurveStepDto.getEndTime() != null){
                String startTimeStr = DateUtils.formatDateTime(piCurveStepDto.getStartTime());
                String endTimeStr = DateUtils.formatDateTime(piCurveStepDto.getEndTime());
                condition = piCurveConfig.getEventTimePi() + " >= '" + startTimeStr +
                        "' AND " + piCurveConfig.getEventTimePi() + " <= '" + endTimeStr + "'";
            }

            //设备编码
            condition += " AND " + piCurveConfig.getUnitNoPi() + " = '" + piCurveStepDto.getUnitNo() + "'";
            //设备名称
            condition += " AND " + piCurveConfig.getUnitNamePi() + " = '" + piCurveStepDto.getUnitName() + "'";
            //配方
            condition += " AND " + piCurveConfig.getRecipePi() + " = '" + piCurveStepDto.getRecipe() + "'";

            queryDto.setCondition(condition);
            List<Map<String,Object>> dataList = iPiCustomizeTableService.getTableByCondition(queryDto);
            for (Map<String, Object> map : dataList) {
                PiCurveStepVo piCurveStepVo = new PiCurveStepVo();
                piCurveStepVo.setOrderNo(String.valueOf(map.get(piCurveConfig.getOrderNoPi())));
                list.add(piCurveStepVo);
            }
        }

        //去除重复订单号
        List<PiCurveStepVo> piCurveStepVoList = list.stream()
                .collect(Collectors.toMap(
                        PiCurveStepVo::getOrderNo,
                        Function.identity(),
                        (existing, replacement) -> existing
                ))
                .values()
                .stream()
                .sorted(Comparator.comparing(PiCurveStepVo::getOrderNo))
                .collect(Collectors.toList());
        return piCurveStepVoList;
    }

    /**
     * 获取批次
     * @param piCurveStepDto
     */
    @Override
    public List<PiCurveStepVo> getBatch(PiCurveStepDto piCurveStepDto) {

        List<PiCurveStepVo> list = new ArrayList<>();
        //获取Pi曲线配置数据
        PiCurveConfig piCurveConfig = piCurveConfigService.getOne(
                new LambdaQueryWrapper<PiCurveConfig>()
                        //.eq(PiCurveConfig::getCurveType, piCurveStepDto.getCurveType()),
                        .eq(PiCurveConfig::getDeviceNo, piCurveStepDto.getDeviceNo())
                        .eq(PiCurveConfig::getAreaNo, piCurveStepDto.getAreaNo()), //pcuNo区域
                false  // 为false表示查询不到数据时不抛出异常，返回null
        );

        if(piCurveConfig != null){
            String deviceNo = piCurveConfig.getDeviceNo();
//            String unitNo = "";
//            if(StrUtil.isNotBlank(deviceNo)){
//                unitNo = deviceNo.split("-")[1];
//            }

            //获取PI表设备
            TableConditionQueryDto queryDto = new TableConditionQueryDto();
            queryDto.setTable(piCurveConfig.getTableName());
//            String condition = piCurveConfig.getEventTimePi() + " >= '" + piCurveStepDto.getStartTime() +
//                    "' AND " + piCurveConfig.getEventTimePi() + " <= '" + piCurveStepDto.getEndTime() + "'";
            String condition = "";
            if(piCurveStepDto.getStartTime() != null && piCurveStepDto.getEndTime() != null){
                String startTimeStr = DateUtils.formatDateTime(piCurveStepDto.getStartTime());
                String endTimeStr = DateUtils.formatDateTime(piCurveStepDto.getEndTime());
                condition = piCurveConfig.getEventTimePi() + " >= '" + startTimeStr +
                        "' AND " + piCurveConfig.getEventTimePi() + " <= '" + endTimeStr + "'";
            }

            //设备编码
            condition += " AND " + piCurveConfig.getUnitNoPi() + " = '" + deviceNo + "'";
            //设备名称
            //condition += " AND " + piCurveConfig.getUnitNamePi() + " = '" + piCurveStepDto.getUnitName() + "'";
            //配方
            //condition += " AND " + piCurveConfig.getRecipePi() + " = '" + piCurveStepDto.getRecipe() + "'";
            //订单
            //condition += " AND " + piCurveConfig.getOrderNoPi() + " = '" + piCurveStepDto.getOrder() + "'";

            queryDto.setCondition(condition);
            List<Map<String,Object>> dataList = iPiCustomizeTableService.getTableByCondition(queryDto);
            for (Map<String, Object> map : dataList) {
                PiCurveStepVo piCurveStepVo = new PiCurveStepVo();
                piCurveStepVo.setBatchNo(String.valueOf(map.get(piCurveConfig.getBatchNoPi())));
                piCurveStepVo.setOrderNo(String.valueOf(map.get(piCurveConfig.getOrderNoPi())));
                list.add(piCurveStepVo);
            }
        }

        //去除重复订单号-批次号
//        List<PiCurveStepVo> piCurveStepVoList = list.stream()
//                .collect(Collectors.toMap(
//                        PiCurveStepVo::getBatchNo,
//                        Function.identity(),
//                        (existing, replacement) -> existing
//                ))
//                .values()
//                .stream()
//                .sorted(Comparator.comparing(PiCurveStepVo::getBatchNo))
//                .collect(Collectors.toList());

        List<PiCurveStepVo> piCurveStepVoList = list.stream()
                .collect(Collectors.toMap(
                        vo -> vo.getOrderNo() + "_" + vo.getBatchNo(),  // 组合键
                        Function.identity(),
                        (existing, replacement) -> existing
                ))
                .values()
                .stream()
                .sorted(Comparator.comparing(PiCurveStepVo::getBatchNo))
                .collect(Collectors.toList());

        return piCurveStepVoList;
    }

    /**
     * 获取数据(步骤时间)
     * @param piCurveStepDto
     */
    @Override
    public PiCurveStepVo getData(PiCurveStepDto piCurveStepDto) {
        PiCurveStepVo stepDataVo = new PiCurveStepVo();
//        List<PiCurveStepVo> list = new ArrayList<>();
        //获取Pi曲线配置数据
        PiCurveConfig piCurveConfig = piCurveConfigService.getOne(
                new LambdaQueryWrapper<PiCurveConfig>()
                        //.eq(PiCurveConfig::getCurveType, piCurveStepDto.getCurveType())
                        .eq(PiCurveConfig::getDeviceNo, piCurveStepDto.getDeviceNo())
                        .eq(PiCurveConfig::getAreaNo, piCurveStepDto.getAreaNo()),
                false  // 为false表示查询不到数据时不抛出异常，返回null
        );

        if(piCurveConfig != null){
            //获取PI表设备
            TableConditionQueryDto queryDto = new TableConditionQueryDto();
            queryDto.setTable(piCurveConfig.getTableName());
//            String condition = piCurveConfig.getEventTimePi() + " >= '" + piCurveStepDto.getStartTime() +
//                    "' AND " + piCurveConfig.getEventTimePi() + " <= '" + piCurveStepDto.getEndTime() + "'";

            //设备编码
            String condition = piCurveConfig.getUnitNoPi() + " = '" + piCurveStepDto.getDeviceNo() + "'";
            //设备名称
           // condition += " AND " + piCurveConfig.getUnitNamePi() + " = '" + piCurveStepDto.getUnitName() + "'";
            //配方
            //condition += " AND " + piCurveConfig.getRecipePi() + " = '" + piCurveStepDto.getRecipe() + "'";
            //订单
            if(StrUtil.isNotBlank(piCurveStepDto.getOrder())){
                condition += " AND " + piCurveConfig.getOrderNoPi() + " = '" + piCurveStepDto.getOrder() + "'";
            }
            //批次
            if(StrUtil.isNotBlank(piCurveStepDto.getBatch())) {
                condition += " AND " + piCurveConfig.getBatchNoPi() + " = '" + piCurveStepDto.getBatch() + "'";
            }

            queryDto.setCondition(condition);
            //排序
            queryDto.setSortField(piCurveConfig.getStartTimePi());
            //排序方式
            queryDto.setSortMode(PiCommonConstants.SORT_ASC);

            List<Map<String,Object>> dataList = iPiCustomizeTableService.getTableByCondition(queryDto);

            if(dataList != null && !dataList.isEmpty()){
                //获取第一条的开始时间
                String startTimeStr = "";
                if(dataList.get(0).get(piCurveConfig.getStartTimePi()) != null){
                    startTimeStr = String.valueOf(dataList.get(0).get(piCurveConfig.getStartTimePi())).replace("T", " ");
                    if (startTimeStr.matches("^\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}$")) {
                        startTimeStr = startTimeStr + ":00";
                    }
                }
                //String startTimeStr = String.valueOf(dataList.get(0).get(piCurveConfig.getStartTimePi()));
                //获取最后一条的结束时间
                String endTimeStr = "";
                if(dataList.get(dataList.size() - 1).get(piCurveConfig.getEndTimePi()) != null){
                    endTimeStr = String.valueOf(dataList.get(dataList.size() - 1).get(piCurveConfig.getEndTimePi())).replace("T", " ");
                    if (endTimeStr.matches("^\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}$")) {
                        endTimeStr = endTimeStr + ":00";
                    }
                }

                //步骤集合
                List<PiCurveStepDto> piCurveStepList = new ArrayList<>();

                // 定义输入格式（ISO格式）
                //DateTimeFormatter inputFormatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
                // 定义输出格式
                //DateTimeFormatter outputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

                for (Map<String, Object> map : dataList) {
                    PiCurveStepDto piCurveStep = new PiCurveStepDto();
                    //piCurveStep.setStartTime(String.valueOf(map.get(piCurveConfig.getStartTimePi())));
                    //piCurveStep.setEndTime(String.valueOf(map.get(piCurveConfig.getEndTimePi())));
                    //eopName
                    // 处理开始时间
                    String startTimeRaw = getSafeString(map.get(piCurveConfig.getStartTimePi()));
                    if (startTimeRaw != null && !startTimeRaw.isEmpty()) {
                        try {
                            //LocalDateTime startDateTime = LocalDateTime.parse(startTimeRaw, inputFormatter);
                            //piCurveStep.setStartTime(startDateTime.format(outputFormatter));
                            //判断是否包含T
                            if (startTimeRaw.contains("T")) {
                                startTimeRaw = startTimeRaw.replace("T", " ");
                            }
                            piCurveStep.setStartTime(DateUtils.parseDateTime(startTimeRaw));
                        } catch (Exception e) {
                            // 记录日志或设置默认值
                            piCurveStep.setStartTime(null); // 保持原值或设置为null
                        }
                    } else {
                        piCurveStep.setStartTime(null); // 或者设置为空字符串
                    }

                    // 处理结束时间
                    String endTimeRaw = getSafeString(map.get(piCurveConfig.getEndTimePi()));
                    if (endTimeRaw != null && !endTimeRaw.isEmpty()) {
                        try {
                            //LocalDateTime endDateTime = LocalDateTime.parse(endTimeRaw, inputFormatter);
                            //piCurveStep.setEndTime(endDateTime.format(outputFormatter));
                            //判断是否包含T
                            if(endTimeRaw.contains("T")){
                                endTimeRaw = endTimeRaw.replace("T", " ");
                            }
                            piCurveStep.setEndTime(DateUtils.parseDateTime(endTimeRaw));
                        } catch (Exception e) {
                            e.printStackTrace();
                            piCurveStep.setEndTime(null);
                        }
                    } else {
                        piCurveStep.setEndTime(null);
                    }

                    //
                    piCurveStep.setEopName(String.valueOf(map.get(piCurveConfig.getEopNamePi())));
                    piCurveStep.setId(String.valueOf(map.get("id")));
                    piCurveStep.setRecipeNo(String.valueOf(map.get(piCurveConfig.getRecipeNoPi())));
                    piCurveStep.setEopNo(String.valueOf(map.get(piCurveConfig.getEopNoPi())));
                    piCurveStep.setAreaNo(piCurveStepDto.getAreaNo());
                    piCurveStep.setUnitNo(String.valueOf(map.get(piCurveConfig.getUnitNoPi())));
                    piCurveStep.setRopId(piCurveStep.getEopNo());
                    piCurveStepList.add(piCurveStep);

                }

                stepDataVo.setStartTime(startTimeStr);
                stepDataVo.setEndTime(endTimeStr);
                stepDataVo.setStepList(piCurveStepList);
                stepDataVo.setRecipeName(String.valueOf(dataList.get(0).get(piCurveConfig.getRecipePi())));


                //获取步骤配置
                PiCurveStep curveStepList = iPiCurveStepService.getPiCurveStep(piCurveStepList.get(0), piCurveStepDto.getDataType());
                String eopNos = curveStepList != null ? curveStepList.getExcludeRopIds() : null;
                if(eopNos != null){
                    //得到map key为 stepConfig(,号分隔)每项 值为 true
                    Map<String, Boolean> stepMap = Arrays.stream(eopNos.split(","))
                            .collect(Collectors.toMap(
                                    Function.identity(),
                                    k -> true,
                                    (existingValue, newValue) -> existingValue // 合并函数，保留现有值
                            ));
                    stepDataVo.setCurveStepMap(stepMap);
                }
            }
        }
        return stepDataVo;
    }

    // 辅助方法：安全获取字符串（处理null值）
    private String getSafeString(Object value) {
        if (value == null) {
            return null;
        }
        String str = String.valueOf(value);
        return "null".equals(str) ? null : str; // 处理"null"字符串的情况
    }

    /**
     * 获取数据(点位时间)
     * @param piSistarRtSteps
     */
    @Override
    public PointDataVo pointData(PiSistarRtSteps piSistarRtSteps) {
        PointDataVo pointDataVO = new PointDataVo();
        //点位对应数据
        Map<String, List<Object[]>> pointMap = new HashMap<>();
        Map<String, String> pointNameMap = new HashMap<>();
        String points = piSistarRtSteps.getPoints();
        List<String> pointList = Arrays.asList(points.split(","));
        //Date转 LocalDateTime
        LocalDateTime startTime = DateUtils.timestampToLocalDate(piSistarRtSteps.getStartTime());
        LocalDateTime endTime = DateUtils.timestampToLocalDate(piSistarRtSteps.getEndTime());

        //获取标准值
        LambdaQueryWrapper<PiBasePoint> basePointwrapper = new LambdaQueryWrapper<>();
        basePointwrapper.in(PiBasePoint::getPointCode, pointList);
        List<PiBasePoint> basePointList = piBasePointService.list(basePointwrapper);

        //获取目标bindStandard 值集合(可能有多标准 需要拆分a,b 得到 a和b)
//		List<String> bindStandardList = basePointList.stream().map(PiBasePoint::getBindStandard).collect(Collectors.toList());
        List<String> bindStandardList = basePointList.stream()
                .map(PiBasePoint::getBindStandard)
                .filter(Objects::nonNull)
                .flatMap(bindStandard -> Arrays.stream(bindStandard.split(",")))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .distinct()  // 去重
                .collect(Collectors.toList());
        basePointList.forEach(point -> pointNameMap.put(point.getPointCode(), point.getPointName()));

        if (bindStandardList != null && !bindStandardList.isEmpty()){
            basePointwrapper = new LambdaQueryWrapper<>();
            basePointwrapper.in(PiBasePoint::getId, bindStandardList);
            basePointList = piBasePointService.list(basePointwrapper);
        }else{
            basePointList = new ArrayList<>();
        }

        pointList = new ArrayList<>(pointList);
        for (PiBasePoint piBasePoint : basePointList) {
            pointList.add(piBasePoint.getPointCode());
            pointNameMap.put(piBasePoint.getPointCode(), piBasePoint.getPointName());
        }

        List<MeasurementData> measurementDataList = iPiComputationModelService.getMeasurementDataList(startTime, endTime ,pointList ,60);

        //测试数据
//        List<MeasurementData> measurementDataList  = new ArrayList<>();
//        //遍历pointList
//        for (String point : pointList) {
//            List<MeasurementData> pointDataList = MeasurementDataGenerator.generateMeasurementData(piSistarRtSteps.getStartTime(),
//                    piSistarRtSteps.getEndTime(), point);
//            measurementDataList.addAll(pointDataList);
//        }

        //循环点位
        for (String point : pointList) {
            List<Object[]> pointDataList = new ArrayList<>();
            for (MeasurementData measurementData : measurementDataList) {
                if(point.equals(measurementData.getCnlNum())){
                    Object[] dataPoint = new Object[2];
                    String timeStr = DateUtils.formatInstantToString(measurementData.getTime());
                    dataPoint[0] = timeStr;
                    dataPoint[1] = measurementData.getValue();
                    pointDataList.add(dataPoint);
                }
            }

            pointMap.put(point, pointDataList);
        }
        //获取时间范围
        pointDataVO.setPointTimeData(pointMap);
        pointDataVO.setPointName(pointNameMap);
        return pointDataVO;
    }


    /**
     * 获取批次
     * @param piCurveStepDto
     * @return
     */
    @Override
    public List<PiCurveStepDto> getRtBatches(PiCurveStepDto piCurveStepDto) {

        List<PiCurveStepDto> curveStepDtoList = new ArrayList<>();
        //获取Pi曲线配置数据
        PiCurveConfig piCurveConfig = piCurveConfigService.getOne(
                new LambdaQueryWrapper<PiCurveConfig>()
                        //.eq(PiCurveConfig::getCurveType, piCurveStepDto.getCurveType()),
                        .eq(PiCurveConfig::getDeviceNo, piCurveStepDto.getDeviceNo())
                        .eq(PiCurveConfig::getAreaNo, piCurveStepDto.getAreaNo()),
                false  // 为false表示查询不到数据时不抛出异常，返回null
        );

        if(piCurveConfig != null) {
            //获取PI表设备
            TableConditionQueryDto queryDto = new TableConditionQueryDto();
            queryDto.setTable(piCurveConfig.getTableName());

//            String condition = piCurveConfig.getEventTimePi() + " >= '" + piCurveStepDto.getStartTime() +
//                    "' AND " + piCurveConfig.getEventTimePi() + " <= '" + piCurveStepDto.getEndTime() + "'";
            String condition = "";
            if(piCurveStepDto.getStartTime() != null && piCurveStepDto.getEndTime() != null){
                String startTimeStr = DateUtils.formatDateTime(piCurveStepDto.getStartTime());
                String endTimeStr = DateUtils.formatDateTime(piCurveStepDto.getEndTime());
                condition = piCurveConfig.getEventTimePi() + " >= '" + startTimeStr +
                        "' AND " + piCurveConfig.getEventTimePi() + " <= '" + endTimeStr + "'";
            }
            //配方
            condition += " AND " + piCurveConfig.getRecipePi() + " = '" + piCurveStepDto.getRecipe() + "'";
            //设备编码
            condition += " AND " + piCurveConfig.getUnitNoPi() + " = '" + piCurveStepDto.getDeviceNo() + "'";
            queryDto.setCondition(condition);
            //排序
            queryDto.setSortField(piCurveConfig.getStartTimePi());
            //排序方式
            queryDto.setSortMode(PiCommonConstants.SORT_ASC);

            List<Map<String, Object>> dataList = iPiCustomizeTableService.getTableByCondition(queryDto);

            for (Map<String, Object> map : dataList) {
                PiCurveStepDto piCurveStep = new PiCurveStepDto();
                //获取开始时间
                String startTimeStr = "";
                if(map.get(piCurveConfig.getStartTimePi()) != null){
                    startTimeStr = String.valueOf(map.get(piCurveConfig.getStartTimePi())).replace("T", " ");
                }
                //String startTimeStr = String.valueOf(dataList.get(0).get(piCurveConfig.getStartTimePi()));
                //获取结束时间
                String endTimeStr = "";
                if(map.get(piCurveConfig.getEndTimePi()) != null){
                    endTimeStr = String.valueOf(map.get(piCurveConfig.getEndTimePi())).replace("T", " ");
                }


//                piCurveStep.setStartTime(startTimeStr);
//                piCurveStep.setEndTime(endTimeStr);
                try {
                    if (startTimeStr != null) {
                        piCurveStep.setStartTime(DateUtils.parseDateTime(startTimeStr));
                    }
                    if (endTimeStr != null) {
                        piCurveStep.setEndTime(DateUtils.parseDateTime(endTimeStr));
                    }
                } catch (ParseException e) {
                    e.printStackTrace();
                }

                //订单
                piCurveStep.setOrderNo(String.valueOf(map.get(piCurveConfig.getOrderNoPi())));
                //批次
                piCurveStep.setBatchNo(String.valueOf(map.get(piCurveConfig.getBatchNoPi())));
                curveStepDtoList.add(piCurveStep);
            }
            
        }

        return deduplicate(curveStepDtoList);
    }

    /**
     * 获取设备-点位
     * @param piCurveStepDto
     * @return
     */
    @Override
    public DevicePointVo unitAndPoint(PiCurveStepDto piCurveStepDto) {
        DevicePointVo devicePointVo = new DevicePointVo();
        List<PiCurveStepDto> curveStepDtoList = new ArrayList<>();

        //String unitNames = piCurveStepDto.getUnitNames();
        //String [] unitName = unitNames.split(",");
        String orderAndBatch = piCurveStepDto.getOrderAndBatch();
        String[] orderBatchTime = orderAndBatch.split(",");
        List<String> orderBatchNos = new ArrayList<>();
        //订单批次Map
        Map<String, String> orderBatchMpa = new HashMap<>();
        //订单批次
        for (String orderBatchTimeStr : orderBatchTime) {
            String[] orderBatchTimeArr = orderBatchTimeStr.split("&");
            orderBatchNos.add(orderBatchTimeArr[0]);
            orderBatchMpa.put(orderBatchTimeArr[0], orderBatchTimeArr[1]);
        }

        //获取Pi曲线配置数据
        PiCurveConfig piCurveConfig = piCurveConfigService.getOne(
                new LambdaQueryWrapper<PiCurveConfig>()
                        //.eq(PiCurveConfig::getCurveType, piCurveStepDto.getCurveType()),
                        .eq(PiCurveConfig::getDeviceNo, piCurveStepDto.getDeviceNo())
                        .eq(PiCurveConfig::getAreaNo, piCurveStepDto.getAreaNo()),
                false  // 为false表示查询不到数据时不抛出异常，返回null
        );
        if(piCurveConfig != null) {
            //获取PI表设备
            TableConditionQueryDto queryDto = new TableConditionQueryDto();
            queryDto.setTable(piCurveConfig.getTableName());

            StringBuffer sb = new StringBuffer();
            //时间
//            sb.append(piCurveConfig.getEventTimePi()).append(" >= '").append(piCurveStepDto.getStartTime()).
//                    append("' AND ").append(piCurveConfig.getEventTimePi()).append(" <= '").append(piCurveStepDto.getEndTime()).
//                    append("'");
            //配方
            sb.append(piCurveConfig.getRecipePi()).append(" = '").append(piCurveStepDto.getRecipe()).append("'");

            //设备-查询条件
            boolean first = true;
            //配方
            sb.append(" AND ").append(piCurveConfig.getRecipePi()).append(" = '")
                    .append(piCurveStepDto.getRecipe()).append("'");
            //设备编码
            sb.append(" AND ").append(piCurveConfig.getUnitNoPi()).append(" = '")
                    .append(piCurveStepDto.getDeviceNo()).append("'");


            //订单批次-查询条件
            sb.append(" AND (");
            first = true;
            for (String orderBatch : orderBatchNos) {
                String[] parts = orderBatch.split("-");
                if (parts.length == 2) {
                    if (!first) {
                        sb.append(" OR ");
                    }
                    sb.append("(")
                            .append(piCurveConfig.getOrderNoPi()).append(" = '").append(parts[0]).append("'")
                            .append(" AND ")
                            .append(piCurveConfig.getBatchNoPi()).append(" = '").append(parts[1]).append("')");
                    first = false;
                }
            }
            sb.append(" ) ");

            queryDto.setCondition(sb.toString());
            //排序
            queryDto.setSortField(piCurveConfig.getStartTimePi());
            //排序方式
            queryDto.setSortMode(PiCommonConstants.SORT_ASC);
            // 获取PI数据
            List<Map<String, Object>> dataList = iPiCustomizeTableService.getTableByCondition(queryDto);
            for (Map<String, Object> map : dataList) {

                PiCurveStepDto piCurveStep = new PiCurveStepDto();

                //获取开始时间
                String startTimeStr = "";
                if(map.get(piCurveConfig.getStartTimePi()) != null){
                    startTimeStr = String.valueOf(map.get(piCurveConfig.getStartTimePi())).replace("T", " ");
                }
                //String startTimeStr = String.valueOf(dataList.get(0).get(piCurveConfig.getStartTimePi()));
                //获取结束时间
                String endTimeStr = "";
                if(map.get(piCurveConfig.getEndTimePi()) != null){
                    endTimeStr = String.valueOf(map.get(piCurveConfig.getEndTimePi())).replace("T", " ");
                }
                try {
                    if (startTimeStr != null) {
                        // 检查字符串长度，如果只有日期和时分，添加默认秒数
                        if (startTimeStr.matches("\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}")) {
                            startTimeStr = startTimeStr + ":00";  // 添加默认秒数
                        }
                        piCurveStep.setStartTime(DateUtils.parseDateTime(startTimeStr));
                    }
                    if (endTimeStr != null && !endTimeStr.isEmpty()) {
                        // 检查字符串长度，如果只有日期和时分，添加默认秒数
                        if (endTimeStr.matches("\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}")) {
                            endTimeStr = endTimeStr + ":00";  // 添加默认秒数
                        }
                        piCurveStep.setEndTime(DateUtils.parseDateTime(endTimeStr));
                    }
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                //piCurveStep.setStartTime(startTimeStr);
                //piCurveStep.setEndTime(endTimeStr);

                //订单
                piCurveStep.setOrder(String.valueOf(map.get(piCurveConfig.getOrderNoPi())));
                //批次
                piCurveStep.setBatch(String.valueOf(map.get(piCurveConfig.getBatchNoPi())));
                //设备
                piCurveStep.setUnitNo(String.valueOf(map.get(piCurveConfig.getUnitNoPi())));
                //设备名称
                piCurveStep.setUnitName(String.valueOf(map.get(piCurveConfig.getUnitNamePi())));
                //步骤名称
                piCurveStep.setEopName(String.valueOf(map.get(piCurveConfig.getEopNamePi())));
                //步骤号
                piCurveStep.setEopNo(String.valueOf(map.get(piCurveConfig.getEopNoPi())));
                //配方号
                piCurveStep.setRecipeNo(String.valueOf(map.get(piCurveConfig.getRecipeNoPi())));
                piCurveStep.setId(String.valueOf(map.get(PiCommonConstants.ID)));
                curveStepDtoList.add(piCurveStep);
            }

            //先按区域 设备 unitId 分类，再在每个分类下按 订单号 orderId 和批次号 batchNr 分类
            Map<String, Map<String, List<PiSistarRtSteps>>> stepGrouped = curveStepDtoList.stream()
                    .filter(Objects::nonNull)  // 过滤掉 null 的 step
                    // 先按区域-设备分类
                    .collect(Collectors.groupingBy(
                            step -> step.getUnitNo(),  // 直接使用 unitNo
                            // 在每个区域-设备下再按订单-批次分类
                            Collectors.groupingBy(
                                    step -> {
                                        String order = step.getOrder();
                                        String batch = step.getBatch();  // 注意字段名
                                        return String.format("%s-%s",
                                                order != null ? order : "",
                                                batch != null ? batch : "");
                                    },
                                    // 在分组的同时将 PiCurveStepDto 转换为 PiSistarRtSteps
                                    Collectors.mapping(
                                            step -> {
                                                PiSistarRtSteps rtSteps = new PiSistarRtSteps();
                                                // 复制属性
                                                rtSteps.setUnitNo(step.getUnitNo());
                                                rtSteps.setOrderNo(step.getOrder());
                                                rtSteps.setBatchNo(step.getBatch());
                                                rtSteps.setStartTime(step.getStartTime());
                                                rtSteps.setEndTime(step.getEndTime());
                                                rtSteps.setEopName(step.getEopName());
                                                return rtSteps;
                                            },
                                            Collectors.toList()
                                    )
                            )
                    ));

            //获取时间跨度最大的步骤
            Map<String, PiCurveStepDto> maxStep = maxDuration(curveStepDtoList);


            //步骤通过设备分组
            Map<String, List<StepVo>> stepUnitGroupMap = curveStepDtoList.stream()
                    .collect(Collectors.groupingBy(
                            step -> String.format("%s", step.getUnitNo()),
                            Collectors.mapping(
                                    step -> {
                                        // 这里实现 PiSistarRtSteps 到 StepVo 的转换
                                        StepVo stepVo = new StepVo();
                                        stepVo.setId(step.getId());

                                        stepVo.setStartTime(step.getStartTime());
                                        stepVo.setEndTime(step.getEndTime());

                                        stepVo.setOrderNo(Integer.parseInt(step.getOrder()));
                                        stepVo.setBatchNo(Integer.parseInt(step.getBatch()));
                                        stepVo.setRopId(String.valueOf(step.getRopId()));
                                        //String eopName = step.getEopName();
                                        stepVo.setEopName(step.getEopName());
                                        stepVo.setEopNo(String.valueOf(step.getEopNo()));
                                        // 设置其他属性...
                                        return stepVo;
                                    },
                                    Collectors.toList()
                            )
                    ));

            //过滤重复步骤
            stepUnitGroupMap =
                stepUnitGroupMap.entrySet()
                    .stream()
                    .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> {
                            PiCurveStepDto max = maxStep.get(entry.getKey());
                            if (max == null) {
                                return Collections.emptyList();
                            }

                            Integer orderId = Integer.parseInt(max.getOrder());
                            Integer batchNr = Integer.parseInt(max.getBatch());

                            return entry.getValue()
                                .stream()
                                .filter(vo ->
                                        Objects.equals(orderId, vo.getOrderNo())
                                                && Objects.equals(batchNr, vo.getBatchNo())
                                )
                                .collect(Collectors.toList());
                        }
                    ));

            //步骤先去重
            List<PiCurveStepDto> stepDedupList = distinctByAllFieldsStream(curveStepDtoList);
            //获取曲线步骤配置
            List<PiCurveStep> curveStepList = iPiCurveStepService.batchPiQueryByStepsSql(stepDedupList);
            //遍历 curveStepList 得到map  key为  areaNo,yearNo,reccatNo,pcuNo,unitNo,recipeNo 值为 excludeRopIds
            Map<String, String> excludeRopIdsMap = curveStepList.stream()
                    .collect(Collectors.toMap(
                            item -> String.format("%s-%s",
                                    //item.getAreaNo(),
                                    //item.getYearNo(),
                                    //item.getReccatNo(),
                                    //item.getPcuNo(),
                                    item.getUnitNo(),
                                    item.getRecipeNo()),
                            PiCurveStep::getExcludeRopIds
                    ));
            //excludeRopIds 存储了ropId集合 逗号拼接(排除步骤)
            curveStepDtoList = filterExcludedStepsOptimized(curveStepDtoList, excludeRopIdsMap);



            //设备订单号批次分组 (设备_订单_批次)
            Map<String, PiCurveStepDto> stepGroupMap = stepGroup(curveStepDtoList);

            //步骤 设备去重 unitNo和pointName 不能相同
            List<PiCurveStepDto> distinctList = curveStepDtoList.stream()
                .collect(Collectors.collectingAndThen(
                    Collectors.toCollection(() ->
                        new TreeSet<>(Comparator.comparing((PiCurveStepDto p) -> p.getUnitNo())
                            .thenComparing(p -> p.getUnitName()))
                    ),
                    ArrayList::new
                ));


            List<String> unitNos = distinctList.stream()
                    .map(item -> item.getUnitNo())
                    .distinct()
                    .collect(Collectors.toList());
            if(unitNos != null && unitNos.isEmpty()){
                return devicePointVo;
            }
            //根据设备Code 获取设备
            LambdaQueryWrapper<MdmDevice> deviceWrapper = new LambdaQueryWrapper<>();
            deviceWrapper.in(MdmDevice::getDeviceCode, unitNos);
            List<MdmDevice> deviceList = mdmDeviceService.list(deviceWrapper);

            // 获取设备id和设备code 对照
            Map<String, Integer> deviceIdToCodeMap = deviceList.stream()
                    .collect(Collectors.toMap(
                            MdmDevice::getDeviceCode,
                            MdmDevice::getId
                    ));

            // 提取设备id
            List<Integer> deviceIds = deviceList.stream()
                    .map(MdmDevice::getId)
                    .toList();

            // 获取设备对应的点位
            LambdaQueryWrapper<PiBasePoint> pointWrapper = new LambdaQueryWrapper<>();
            pointWrapper.in(PiBasePoint::getDeviceId, deviceIds);
            pointWrapper.in(PiBasePoint::getDataAttribute, Arrays.asList("0", "1"));
            List<PiBasePoint> pointList = piBasePointService.list(pointWrapper);

            //
            Map<String,Boolean> deviceMap = new HashMap<String,Boolean>();
            //设备id 和订单批次
            Map<String, ArrayList<Map<String, String>>> deviceIdToOrderBatchMap = new HashMap<>();
            for (PiCurveStepDto piCurveSte : curveStepDtoList) {
                //设备code (区域_设备id)
                String deviceCode = piCurveSte.getUnitNo();
                String deviceId = deviceIdToCodeMap.get(deviceCode)+"";
                String key = piCurveSte.getOrder()+"-"+piCurveSte.getBatch();
                String orderBatchDate = orderBatchMpa.get(key);
                //获取分组值
                PiCurveStepDto stepGroupTime = stepGroupMap.get(deviceCode+"-"+key);
                if(StrUtil.isNotBlank(deviceId) && StrUtil.isNotBlank(orderBatchDate) && !deviceMap.containsKey(deviceId+"_"+key) && stepGroupTime != null){
                    deviceMap.put(deviceId+"_"+key, true);
                    Map<String, String> map = new HashMap<>();
                    //String[] dates= orderBatchDate.split("_");
                    map.put("orderBatch", key);
//                    map.put("startDate",dates[0]);
//                    map.put("endDate", dates[1]);
                    if (stepGroupTime.getStartTime() != null && stepGroupTime.getEndTime() != null){
                        //map.put("startDate", stepGroupTime.getStartTime());
                        //map.put("endDate", stepGroupTime.getEndTime());
                        map.put("startDate", DateUtils.formatDateTime(stepGroupTime.getStartTime()));
                        map.put("endDate", DateUtils.formatDateTime(stepGroupTime.getEndTime()));
                    }
                    if(deviceIdToOrderBatchMap.containsKey(deviceId)){
                        ArrayList<Map<String, String>> list = deviceIdToOrderBatchMap.get(deviceId);
                        list.add(map);
                    }else{
                        ArrayList<Map<String, String>> list = new ArrayList<>();
                        list.add(map);
                        deviceIdToOrderBatchMap.put(deviceId, list);
                    }
                    //deviceIdToOrderBatchMap.put(deviceId, map);
                }
            }
            // 点位关联订单批次
            for (PiBasePoint piBasePoint : pointList) {
                Object obj = deviceIdToOrderBatchMap.get(piBasePoint.getDeviceId()+"");
                if(obj != null && ("0").equals(piBasePoint.getDataAttribute())){
                    ArrayList<Map<String, String>> list = (ArrayList<Map<String, String>>) obj;
                    piBasePoint.setOrderBatchJson(JSONUtil.toJsonStr(list));
                }
            }

            List<PiCurveUnits> unitList = new ArrayList<>();
            //拼接数据 设备 设备点位
            for (MdmDevice mdmDevice : deviceList) {
                //String deviceCode = mdmDevice.getDeviceCode();
                //判断设备编号是否包含- 包含进行拆分
//                if (mdmDevice.getDeviceCode().contains("-")){
//                    String[] deviceCodeArr = mdmDevice.getDeviceCode().split("-");
//                    deviceCode = deviceCodeArr[1];
//                }else{
//                    deviceCode = mdmDevice.getDeviceCode();
//                }
                PiCurveUnits piSistarEngUnits = new PiCurveUnits();
                piSistarEngUnits.setUnitNo(mdmDevice.getDeviceCode());
                piSistarEngUnits.setUnitName(mdmDevice.getDeviceName());
                List<PiBasePoint> points = new ArrayList<>();
                for (PiBasePoint point : pointList) {
                    if (Objects.equals(mdmDevice.getId(), point.getDeviceId())) {
                        points.add(point);
                    }
                }
                if(points != null && !points.isEmpty()){
                    piSistarEngUnits.setPoints(points);
                    unitList.add(piSistarEngUnits);
                }
            }

            // 设备分组-步骤配置 id 集合
            Map<String, List<String>> configStepGroupMap = curveStepDtoList.stream()
                .collect(Collectors.groupingBy(
                    step -> String.format("%s", step.getUnitNo()),
                    Collectors.mapping(
//                            PiCurveStepDto::getEopNo,  // 提取 id
                            PiCurveStepDto::getId,
                            Collectors.toList()  // 收集为 List
                    )
                ));

            devicePointVo.setUnitList(unitList);

            //设备步骤分组 设备-步骤集合
            devicePointVo.setStepGroupMap(stepUnitGroupMap);
            //设备步骤分组 设备-选中步骤集合
            devicePointVo.setConfigStepsMap(configStepGroupMap);
            //步骤分组
            devicePointVo.setAdapterStepGrouped(stepGrouped);
        }

        return devicePointVo;
    }

    /**
     * 步骤分组
     * 设备
     * 开始时间 结束时间
     * @param stepList
     * @return
     */
    public Map<String, PiCurveStepDto> stepGroup(List<PiCurveStepDto> stepList) {
        Map<String, PiCurveStepDto> resultMap =
            stepList.stream()
                .collect(Collectors.toMap(
                    s -> s.getUnitNo()+ "-" +
                         s.getOrder() + "-" +
                         s.getBatch(),
                    Function.identity(),
                    (a, b) -> {
                        // 开始时间取最小
                        Date aTime = null;
                        Date bTime = null;
//                        try {
//                            aTime = a.getStartTime() != null ? DateUtils.parseDateTime(a.getStartTime()) : null;
//                            bTime = b.getStartTime() != null ? DateUtils.parseDateTime(b.getStartTime()) : null;
                            aTime = a.getStartTime() != null ? a.getStartTime() : null;
                            bTime = b.getStartTime() != null ? b.getStartTime() : null;
//                        } catch (ParseException e) {}
                        if (aTime == null ||
                                (bTime != null && aTime.after(bTime))) {
                            a.setStartTime(b.getStartTime());
                        }
                        // 结束时间取最大
                        Date aEndTime = null;
                        Date bEndTime = null;
//                        try {
//                            aEndTime = a.getEndTime() != null ? DateUtils.parseDateTime(a.getEndTime()) : null;
//                            bEndTime = b.getEndTime() != null ? DateUtils.parseDateTime(b.getEndTime()) : null;
                            aEndTime = a.getEndTime() != null ? a.getEndTime() : null;
                            bEndTime = b.getEndTime() != null ? b.getEndTime() : null;
//                        } catch (ParseException e) {}
                        if (aEndTime == null ||
                                (bEndTime != null && aEndTime.before(bEndTime))) {
                            a.setEndTime(b.getEndTime());
                        }
                        return a;
                    }
                ));
        return resultMap;
    }


    /**
     * 获取设备点位
     * @param piSistarRtSteps
     * @return
     */
    @Override
    public PointDataVo getPointData(PiSistarRtSteps piSistarRtSteps) {
        //点位对应数据
        Map<String, List<MeasurementData>> pointMap = new HashMap<>();
        String pointJson = piSistarRtSteps.getPoints();

        List<OrderPointDto> jsonArray = JSONUtil.toList(pointJson, OrderPointDto.class);

        Date minStartTime = null;
        Date maxEndTime = null;
        OrderPointDto maxDurationRecord = null;
        //获取时间跨度最大点位
        long maxDuration = -1;
        for (OrderPointDto points : jsonArray) {
            Date startTime = points.getStartDate();
            Date endTime = points.getEndDate();
            if(startTime != null && endTime != null) {
                // 计算当前记录的时间跨度（毫秒）
                long duration = endTime.getTime() - startTime.getTime();
                // 更新最大时间跨度记录
                if (maxDurationRecord == null || duration > maxDuration) {
                    maxDuration = duration;
                    maxDurationRecord = points;
                }
            }
        }

        List<MeasurementData> measurementDataList = new ArrayList<>();
        for (OrderPointDto points : jsonArray) {
            LocalDateTime startLocalTime = null;
            LocalDateTime endLocalTime = null;
            if(points.getStartDate() != null && points.getEndDate() != null){
                startLocalTime = DateUtils.timestampToLocalDate(points.getStartDate());
                endLocalTime = DateUtils.timestampToLocalDate(points.getEndDate());
            }else{
                if (maxDurationRecord != null) {
                    startLocalTime = DateUtils.timestampToLocalDate(maxDurationRecord.getStartDate());
                    endLocalTime = DateUtils.timestampToLocalDate(maxDurationRecord.getEndDate());
                    points.setStartDate(maxDurationRecord.getStartDate());
                    points.setEndDate(maxDurationRecord.getEndDate());
                }
            }


            List<String> pointList = new ArrayList<>();
            pointList.add(points.getPointCode());
            List<MeasurementData> pointDataList = iPiComputationModelService.getMeasurementDataList(startLocalTime, endLocalTime ,pointList ,60);

            //测试
//			Date startTime = points.getStartDate();
//			Date endTime = points.getEndDate();
//			List<MeasurementData> pointDataList = new ArrayList<>();
//			for (String point : pointList) {
//				pointDataList = MeasurementDataGenerator.generateMeasurementData(startTime,
//						endTime, point);
//				measurementDataList.addAll(pointDataList);
//			}

            //measurementDataList.addAll(pointDataList);
            if (StrUtil.isNotBlank(points.getOrderBatch())){
                pointMap.put(points.getOrderBatch()+"-"+points.getPointCode(), pointDataList);
            }else{
                pointMap.put(points.getPointCode(), pointDataList);
            }
        }

        //获取时间范围
        List<Integer> intervals = new ArrayList<>();
        if(maxDurationRecord != null){
            minStartTime = maxDurationRecord.getStartDate();
            maxEndTime = maxDurationRecord.getEndDate();
            intervals = AdvancedDateTimeCalculator.calculate6MinuteIntervals(minStartTime, maxEndTime);
        }

        PointDataVo pointDataVo = new PointDataVo();
        pointDataVo.setPointData(pointMap);
        pointDataVo.setIntervals(intervals);
        return pointDataVo;
    }


    /**
     * 根据 orderNo 和 batchNo 分组，然后在每个组内取 startTime 的最小值和 endTime 的最大值
     * @param list
     * @return
     */
    public List<PiCurveStepDto> deduplicate(List<PiCurveStepDto> list) {
        return list.stream()
            .collect(Collectors.groupingBy(
                item -> item.getOrderNo() + "_" + item.getBatchNo(),
                Collectors.collectingAndThen(
                    Collectors.toList(),
                    group -> {
                        // 按 startTime 排序
                        group.sort(Comparator.comparing(PiCurveStepDto::getStartTime));
                        // 取第一个元素的 startTime 和最后一个元素的 endTime
                        PiCurveStepDto first = group.get(0);
                        PiCurveStepDto last = group.get(group.size() - 1);
                        // 创建新的对象或修改现有对象
                        PiCurveStepDto result = new PiCurveStepDto();
                        result.setOrderNo(first.getOrderNo());
                        result.setBatchNo(first.getBatchNo());
                        result.setStartTime(first.getStartTime());
                        result.setEndTime(last.getEndTime());
                        return result;
                    }
                )
            ))
            .values()
            .stream()
            .collect(Collectors.toList());
    }



    @Override
    public void convert(IPage<PiCurveConfig> pageList) {
        //获取区域字典
        List<MdmDict> mdmDictList = mdmDictService.getDictAllOrByGroupCode(
                List.of("MES3_PI_CURVE_AREA")
        );
        Map<String, String> dictMap = mdmDictList.stream()
                .collect(Collectors.toMap(
                        item -> item.getItemVal(),
                        item -> item.getItemKey()
                ));

        for (PiCurveConfig piCurveConfig : pageList.getRecords()) {
            if(dictMap != null){
                piCurveConfig.setAreaName(dictMap.get(piCurveConfig.getAreaNo()));
            }

        }

    }

    @Override
    public List<PiCurveConfig> getCurveConfigByArea(String areaNo) {
        LambdaQueryWrapper<PiCurveConfig> deviceWrapper = new LambdaQueryWrapper<>();
        deviceWrapper.in(PiCurveConfig::getAreaNo, areaNo);
        return list(deviceWrapper);
    }

    /**
     * 获取最大持续时间的步骤
     */
    private Map<String, PiCurveStepDto> maxDuration(List<PiCurveStepDto> stepList) {
        return stepList.stream()
                .collect(Collectors.groupingBy(PiCurveStepDto::getUnitNo))
                .entrySet()
                .stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> {
                            PiCurveStepDto result = entry.getValue().stream()
                                    .collect(Collectors.groupingBy(
                                            s -> s.getOrder() + "-" + s.getBatch()
                                    ))
                                    .values()
                                    .stream()
                                    .map(batchSteps -> {
                                        Date start = batchSteps.stream()
                                                .map(PiCurveStepDto::getStartTime)
                                                .filter(Objects::nonNull)
                                                .min(Date::compareTo)
                                                .orElse(null);

                                        Date end = batchSteps.stream()
                                                .map(PiCurveStepDto::getEndTime)
                                                .filter(Objects::nonNull)
                                                .max(Date::compareTo)
                                                .orElse(null);

                                        if (start == null || end == null) {
                                            return null;
                                        }

                                        long durationMillis = Duration.between(
                                                start.toInstant(),
                                                end.toInstant()
                                        ).toMillis();

//                                        return batchSteps.stream()
//                                                .max(Comparator.comparing(PiCurveStepDto::getEndTime))
//                                                .map(step -> Map.entry(step, durationMillis))
//                                                .orElse(null);
                                        return batchSteps.stream()
                                                .max(Comparator.nullsLast(
                                                        Comparator.comparing(PiCurveStepDto::getEndTime,
                                                                Comparator.nullsLast(Comparator.naturalOrder()))
                                                ))
                                                .map(step -> Map.entry(step, durationMillis))
                                                .orElse(null);
                                    })
                                    .filter(Objects::nonNull)
                                    .max(Comparator.comparing(Map.Entry::getValue))
                                    .map(Map.Entry::getKey)
                                    .orElse(null);

                            // 如果结果为null，返回一个默认值或者跳过这个entry
                            return result != null ? result : new PiCurveStepDto(); // 或者返回null并后续过滤
                        }
                ));
    }


    //步骤去重
    public List<PiCurveStepDto> distinctByAllFieldsStream(List<PiCurveStepDto> stepList) {
        if (CollectionUtils.isEmpty(stepList)) {
            return Collections.emptyList();
        }

        return stepList.stream()
            .collect(Collectors.collectingAndThen(
                Collectors.toMap(
                    step -> buildCompositeKey(step),
                    Function.identity(),
                    (existing, replacement) -> existing,  // 去重策略：保留第一个
                    LinkedHashMap::new
                ),
                map -> new ArrayList<>(map.values())
            ));
    }
    //指定key
    private String buildCompositeKey(PiCurveStepDto step) {
        return String.format("%s-%s",
                //step.getAreaNo(),
                //step.getYearNo(),
                //step.getReccatNo(),
                //step.getPcuNo(),
                step.getUnitNo(),
                step.getRecipeNo()
        );
    }

    //过滤步骤
    public List<PiCurveStepDto> filterExcludedStepsOptimized(
            List<PiCurveStepDto> stepList,
            Map<String, String> excludeRopIdsMap) {

        if (CollectionUtils.isEmpty(stepList) || excludeRopIdsMap == null) {
            return stepList;
        }

        // 预先处理排除Map，将字符串转换为Set
        Map<String, Set<String>> excludeMap = preprocessExcludeMap(excludeRopIdsMap);

        return stepList.stream()
                .filter(step -> !isExcludedOptimized(step, excludeMap))
                .collect(Collectors.toList());
    }

    private Map<String, Set<String>> preprocessExcludeMap(Map<String, String> excludeRopIdsMap) {
        Map<String, Set<String>> result = new HashMap<>();

        for (Map.Entry<String, String> entry : excludeRopIdsMap.entrySet()) {
            if (StringUtils.isNotBlank(entry.getValue())) {
                Set<String> excludeSet = Arrays.stream(entry.getValue().split(","))
                        .map(String::trim)
                        .filter(StringUtils::isNotBlank)
                        .collect(Collectors.toSet());
                result.put(entry.getKey(), excludeSet);
            }
        }

        return result;
    }

    private boolean isExcludedOptimized(PiCurveStepDto step, Map<String, Set<String>> excludeMap) {
        String key = buildCompositeKey(step);
        Set<String> excludeSet = excludeMap.get(key);

        if (excludeSet == null || excludeSet.isEmpty() || step.getEopNo() == null) {
            return false;
        }

        return excludeSet.contains(String.valueOf(step.getEopNo()));
    }






    /**
     * 获取数据步骤
     * @param ids
     */
//    @Override
//    public List<PiSistarRtSteps> getDataById(List ids, PiCurveStep piCurveStep) {
//        List<PiSistarRtSteps> stepList = new ArrayList<>();
//        //获取Pi曲线配置数据
//        PiCurveConfig piCurveConfig = piCurveConfigService.getOne(
//                new LambdaQueryWrapper<PiCurveConfig>()
//                        //.eq(PiCurveConfig::getCurveType, piCurveStepDto.getCurveType())
//                        .eq(PiCurveConfig::getDeviceNo, piCurveStep.getUnitNo())
//                        .eq(PiCurveConfig::getAreaNo, piCurveStep.getAreaNo()),
//                false  // 为false表示查询不到数据时不抛出异常，返回null
//        );
//
//        if(piCurveConfig != null){
//            //获取PI表设备
//            TableConditionQueryDto queryDto = new TableConditionQueryDto();
//            queryDto.setTable(piCurveConfig.getTableName());
//
//            //设备编码
//            String condition = buildCondition(ids);
//            queryDto.setCondition(condition);
//            //排序
//            queryDto.setSortField(piCurveConfig.getStartTimePi());
//            //排序方式
//            queryDto.setSortMode(PiCommonConstants.SORT_ASC);
//
//            List<Map<String,Object>> dataList = iPiCustomizeTableService.getTableByCondition(queryDto);
//
//            if(dataList != null && !dataList.isEmpty()){
//                //步骤集合
//                List<PiCurveStepDto> piCurveStepList = new ArrayList<>();
//                for (Map<String, Object> map : dataList) {
//                    PiSistarRtSteps SistarRtStep = new PiSistarRtSteps();
//                    SistarRtStep.setAreaNo(piCurveStep.getAreaNo());
//                    SistarRtStep.setPcuNo(piCurveStep.getAreaNo());
//                    SistarRtStep.setUnitNo(piCurveStep.getUnitNo());
//
//                    SistarRtStep.setEopName(String.valueOf(map.get(piCurveConfig.getEopNamePi())));
//                    SistarRtStep.setId(String.valueOf(map.get("id")));
//                    stepList.add(SistarRtStep);
//                }
//
//            }
//        }
//        return stepList;
//    }
//
//    public String buildCondition(List<String> ids) {
//        if (ids == null || ids.isEmpty()) {
//            return "1=1"; // 或者返回空字符串
//        }
//        StringBuilder sb = new StringBuilder("id IN (");
//        for (int i = 0; i < ids.size(); i++) {
//            if (i > 0) {
//                sb.append(",");
//            }
//            sb.append(ids.get(i));
//        }
//        sb.append(")");
//
//        return sb.toString();
//    }
}
