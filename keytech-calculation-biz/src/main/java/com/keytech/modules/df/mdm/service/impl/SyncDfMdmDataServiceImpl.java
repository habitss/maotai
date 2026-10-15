package com.keytech.modules.df.mdm.service.impl;

import cn.hutool.core.date.DateUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.keytech.modules.alarm.entity.AlarmPoint;
import com.keytech.modules.alarm.mapper.AlarmPointMapper;
import com.keytech.modules.df.mdm.entity.*;
import com.keytech.modules.df.mdm.mapper.*;
import com.keytech.modules.df.mdm.service.ISyncDfMdmDataService;
import com.keytech.modules.ems.util.Java8DateUtil;
import com.keytech.modules.mdm3.entity.MdmDevice;
import com.keytech.modules.mdm3.entity.MdmDeviceType;
import com.keytech.modules.mdm3.service.IMdmDeviceService;
import com.keytech.modules.mdm3.service.IMdmDeviceTypeService;
import com.xxl.job.core.biz.model.ReturnT;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * MDM点位实例 实现类
 * @author Rocye
 * @since 2025.12.09
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class SyncDfMdmDataServiceImpl implements ISyncDfMdmDataService {
    @Value("${mdm.api.accessKey}")
    private String accessKey;
    @Value("${mdm.api.secretKey}")
    private String secretKey;
    @Value("${mdm.api.authUrl}")
    private String authUrl;
    @Value("${mdm.api.modelUrl}")
    private String modelUrl;

    //Http客户端
    private final OkHttpClient httpClient;
    //MDM指标Mapper
    private final DfMdmIndicatorMapper dfMdmIndicatorMapper;
    //MDM配方Mapper
    private final DfMdmRecipeMapper dfMdmRecipeMapper;
    //MDM报警点位Mapper
    private final DfMdmAlarmtagMapper dfMdmAlarmtagMapper;
    //MDM部门映射Mapper
    private final DfMdmDeptMappingMapper dfMdmDeptMappingMapper;
    //mes3主数据-设备
    private final IMdmDeviceService iMdmDeviceService;
    //mes3主数据-设备类型
    private final IMdmDeviceTypeService iMdmDeviceTypeService;
    //标准报警点位Mapper
    private final AlarmPointMapper alarmPointMapper;
    //指标标准Mapper
    private final DfMdmIndStandMapper dfMdmIndStandMapper;
    //功能位置Mapper
    private final DfMdmFunlocMapper dfMdmFunlocMapper;

    /**
     * 同步MDM点位实例数据
     * @param tenantId 租户ID
     * @param startTime 同步起始时间
     * @param endTime 同步结束时间
     * @param size 每页拉取多少条
     * @param modelCodes 同步模型编码 INST_IND
     */
    @Override
    public void syncMdmIndicatorData(Integer tenantId, String startTime, String endTime, int size, List<String> modelCodes) {
        if (StringUtils.isEmpty(startTime)) {
            //查询实例表中最新更新时间（注意: 如果是空表时SELECT MAX()会返回一条NULL记录）
            List<Object> objects = dfMdmIndicatorMapper.selectObjs(new QueryWrapper<DfMdmIndicator>().select("MAX(update_time)"));
            String maxUpdateTime = (objects == null || objects.isEmpty() || objects.get(0) == null) ? null : objects.get(0).toString();
            if (maxUpdateTime != null) {
                if (maxUpdateTime.contains(".")) {
                    maxUpdateTime = maxUpdateTime.substring(0, maxUpdateTime.lastIndexOf('.'));
                }
                startTime = Java8DateUtil.parse(maxUpdateTime).plusSeconds(1).format(Java8DateUtil.sdf1);
            }
        }else {
            //必须是yyyy-MM-dd HH:mm:ss格式
            startTime = Java8DateUtil.parse(startTime).format(Java8DateUtil.sdf1);
        }
        if (StringUtils.isEmpty(endTime)) {
            endTime = LocalDateTime.now().format(Java8DateUtil.sdf1);
        }else {
            //必须是yyyy-MM-dd HH:mm:ss格式
            endTime = Java8DateUtil.parse(endTime).format(Java8DateUtil.sdf1);
        }
        log.warn("======开始同步时间区间:{}-{} 模型编码:{} 的数据同步=======", startTime, endTime, String.join(",", modelCodes));
        //获取Token
        String token = getAuthToken(authUrl,accessKey, secretKey);
        if (StringUtils.isEmpty(token)){
            log.error("获取MDM服务接口授权Token失败");
            return;
        }

        //如果有多个模型编码，则循环拉取
        for (String modelCode : modelCodes) {
            int curPage = 1;
            // 初始化为1，后续从API获取实际值
            int totalPage = 1;
            while (curPage <= totalPage) {
                try {
                    long now = System.currentTimeMillis();
                    JSONObject data = queryModelData(token, modelCode, startTime, endTime, size, curPage);
                    if (data == null) {
                        log.warn("======= 同步模型编码 {} 失败 ========", modelCode);
                    }else {
                        int total = data.getInteger("total");
                        int current = data.getInteger("current");
                        totalPage = data.getInteger("pages");
                        int pageSize = data.getInteger("size");
                        log.warn("====== 本轮总计需要拉取：{} 当前页/总页数：{}/{} 数量：{}", total, current, totalPage, pageSize);
                        if(pageSize > 0) {
                            List<JSONObject> records = data.getJSONArray("records").toJavaList(JSONObject.class);
                            List<DfMdmIndicator> dataList = new ArrayList<>();
                            for (JSONObject item : records) {
                                DfMdmIndicator instance = mapToMdmIndicator(item, tenantId);
                                dataList.add(instance);
                            }
                            if (!dataList.isEmpty()) {
                                dfMdmIndicatorMapper.batchInsertOrUpdate(dataList);
                            }
                            log.warn("====== 本次成功插入或更新{}记录: {} 条, 耗时: {}", modelCode, dataList.size(), System.currentTimeMillis() - now);
                        }
                    }
                    curPage++;
                    // 添加适当延迟，避免对API造成过大压力
                    Thread.sleep(200);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    log.error("拉取模型数据时同步被中断: {}", e.getMessage());
                    break;
                } catch (Exception e) {
                    log.error("拉取模型数据 {} 第 {} 页时发生错误: {}", modelCode, curPage, e.getMessage(), e);
                    // 跳过当前页继续
                    curPage++;
                }
            }
        }

//        try { //暂时不从实例化指标中同步部门映射表
//            dfMdmDeptMappingMapper.syncMdmDeptMapping();
//        } catch (Exception e) {
//            log.error("同步模型数据-处理MDM部门映射表时发生错误: {}", e.getMessage(), e);
//        }
    }

    /**
     * 同步MDM指标标准数据
     * @param tenantId 租户ID
     * @param startTime 同步起始时间
     * @param endTime 同步结束时间
     * @param size 每页拉取多少条
     * @param modelCode 同步模型编码 IND_STAND
     */
    @Override
    public void syncMdmIndStandData(Integer tenantId, String startTime, String endTime, int size, String modelCode) {
        // 查询最新更新时间的方法
        Function<DfMdmIndStandMapper, String> queryMaxTimeMethod = mapper -> {
            List<Object> objects = mapper.selectObjs(new QueryWrapper<DfMdmIndStand>().select("MAX(update_time)"));
            return (objects == null || objects.isEmpty() || objects.get(0) == null) ? null : objects.get(0).toString();
        };

        // 将JSONObject映射为实体对象的方法
        BiFunction<JSONObject, Integer, DfMdmIndStand> mapFunction = this::mapToMdmIndStand;

        // 批量插入或更新的方法
        Consumer<List<DfMdmIndStand>> batchInsertOrUpdateMethod = dfMdmIndStandMapper::batchInsertOrUpdate;

        syncMdmDataGeneric(tenantId, startTime, endTime, size, modelCode,
                dfMdmIndStandMapper, queryMaxTimeMethod, mapFunction, batchInsertOrUpdateMethod);
    }

    /**
     * 同步MDM功能位置数据
     * @param tenantId 租户ID
     * @param startTime 同步起始时间
     * @param endTime 同步结束时间
     * @param size 每页拉取多少条
     * @param modelCode 同步模型编码 FUNLOC_BASIC_INFO2
     */
    @Override
    public void syncMdmFunlocData(Integer tenantId, String startTime, String endTime, int size, String modelCode) {
        // 查询最新更新时间的方法
        Function<DfMdmFunlocMapper, String> queryMaxTimeMethod = mapper -> {
            List<Object> objects = mapper.selectObjs(new QueryWrapper<DfMdmFunloc>().select("MAX(update_time)"));
            return (objects == null || objects.isEmpty() || objects.get(0) == null) ? null : objects.get(0).toString();
        };

        // 将JSONObject映射为实体对象的方法
        BiFunction<JSONObject, Integer, DfMdmFunloc> mapFunction = this::mapToMdmFunlocStand;

        // 批量插入或更新的方法
        Consumer<List<DfMdmFunloc>> batchInsertOrUpdateMethod = dfMdmFunlocMapper::batchInsertOrUpdateFunloc;

        syncMdmDataGeneric(tenantId, startTime, endTime, size, modelCode,
                dfMdmFunlocMapper, queryMaxTimeMethod, mapFunction, batchInsertOrUpdateMethod);
    }

    /**
     * 同步MDM配方数据
     * @param tenantId 租户ID
     * @param startTime 同步起始时间
     * @param endTime 同步结束时间
     * @param size 每页拉取多少条
     * @param modelCodes 同步模型编码 FAC_BASIC_INFO_RECIPE_NEW
     */
    @Override
    public void syncMdmRecipeData(Integer tenantId, String startTime, String endTime, int size, List<String> modelCodes) {
        if (StringUtils.isEmpty(startTime)) {
            //查询实例表中最新更新时间（注意: 如果是空表时SELECT MAX()会返回一条NULL记录）
            List<Object> objects = dfMdmRecipeMapper.selectObjs(new QueryWrapper<DfMdmRecipe>().select("MAX(update_time)"));
            String maxUpdateTime = (objects == null || objects.isEmpty() || objects.get(0) == null) ? null : objects.get(0).toString();
            if (maxUpdateTime != null) {
                if (maxUpdateTime.contains(".")) {
                    maxUpdateTime = maxUpdateTime.substring(0, maxUpdateTime.lastIndexOf('.'));
                }
                startTime = Java8DateUtil.parse(maxUpdateTime).plusSeconds(1).format(Java8DateUtil.sdf1);
            }
        }else {
            //必须是yyyy-MM-dd HH:mm:ss格式
            startTime = Java8DateUtil.parse(startTime).format(Java8DateUtil.sdf1);
        }
        if (StringUtils.isEmpty(endTime)) {
            endTime = LocalDateTime.now().format(Java8DateUtil.sdf1);
        }else {
            //必须是yyyy-MM-dd HH:mm:ss格式
            endTime = Java8DateUtil.parse(endTime).format(Java8DateUtil.sdf1);
        }
        log.warn("======开始同步时间区间:{}-{} 模型编码:{} 的数据同步=======", startTime, endTime, String.join(",", modelCodes));
        //获取Token
        String token = getAuthToken(authUrl,accessKey, secretKey);
        if (StringUtils.isEmpty(token)){
            log.error("获取MDM服务接口授权Token失败");
            return;
        }

        //如果有多个模型编码，则循环拉取
        for (String modelCode : modelCodes) {
            int curPage = 1;
            // 初始化为1，后续从API获取实际值
            int totalPage = 1;
            while (curPage <= totalPage) {
                try {
                    long now = System.currentTimeMillis();
                    JSONObject data = queryModelData(token, modelCode, startTime, endTime, size, curPage);
                    if (data == null) {
                        log.warn("======= 同步模型编码 {} 失败 ========", modelCode);
                    }else {
                        int total = data.getInteger("total");
                        int current = data.getInteger("current");
                        totalPage = data.getInteger("pages");
                        int pageSize = data.getInteger("size");
                        log.warn("====== 本轮总计需要拉取：{} 当前页/总页数：{}/{} 数量：{}", total, current, totalPage, pageSize);
                        if(pageSize > 0) {
                            List<JSONObject> records = data.getJSONArray("records").toJavaList(JSONObject.class);
                            List<DfMdmRecipe> dataList = new ArrayList<>();
                            for (JSONObject item : records) {
                                DfMdmRecipe recipe = mapToMdmRecipe(item, tenantId);
                                dataList.add(recipe);
                            }
                            if (!dataList.isEmpty()) {
                                dfMdmRecipeMapper.batchInsertOrUpdate(dataList);
                            }
                            log.warn("====== 本次成功插入或更新 {} 记录: {} 条, 耗时: {}", modelCode, dataList.size(), System.currentTimeMillis() - now);
                        }
                    }
                    curPage++;
                    // 添加适当延迟，避免对API造成过大压力
                    Thread.sleep(200);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    log.error("拉取模型数据时同步被中断: {}", e.getMessage());
                    break;
                } catch (Exception e) {
                    log.error("拉取模型数据 {} 第 {} 页时发生错误: {}", modelCode, curPage, e.getMessage(), e);
                    // 跳过当前页继续
                    curPage++;
                }
            }
        }
    }

    /**
     * 同步MDM报警主数据
     * @param tenantId 租户ID
     * @param startTime 同步起始时间
     * @param endTime 同步结束时间
     * @param size 每页拉取多少条
     * @param modelCodes 同步模型编码 TAG_DICTIONARY
     */
    @Override
    public void syncMdmAlarmtagData(Integer tenantId, String startTime, String endTime, int size, List<String> modelCodes) {
        //获取系统中的部门映射关系
        List<DfMdmDeptMapping> dfDeptMappingList = dfMdmDeptMappingMapper.selectList(
                new QueryWrapper<>(DfMdmDeptMapping.class)
                        .isNotNull("sys_dept_code")
        );
        Map<String, DfMdmDeptMapping> dfMdmDeptMappingMap = dfDeptMappingList.stream()
                .collect(Collectors.toMap(
                        //key为mdmFullCode
                        DfMdmDeptMapping::getMdmFullCode,
                        //value为对象本身
                        item -> item,
                        //如果有重复key，保留第一个值
                        (existing, replacement) -> existing
                ));
        
        //获取系统中的设备
        List<MdmDevice> mdmDeviceList = iMdmDeviceService.lambdaQuery()
                .eq(MdmDevice::getSource, "2")
                .eq(MdmDevice::getDelFlag, 0)
                .list();
        Map<String, MdmDevice> mdmDeviceMap = mdmDeviceList.stream()
                .collect(Collectors.toMap(
                        //key为deviceCode
                        MdmDevice::getDeviceCode,
                        //value为对象本身
                        item -> item,
                        //如果有重复key，保留第一个值
                        (existing, replacement) -> existing
                ));

        if (StringUtils.isEmpty(startTime)) {
            //查询实例表中最新更新时间（注意: 如果是空表时SELECT MAX()会返回一条NULL记录）
            List<Object> objects = dfMdmAlarmtagMapper.selectObjs(new QueryWrapper<DfMdmAlarmtag>().select("MAX(update_time)"));
            String maxUpdateTime = (objects == null || objects.isEmpty() || objects.get(0) == null) ? null : objects.get(0).toString();
            if (maxUpdateTime != null) {
                if (maxUpdateTime.contains(".")) {
                    maxUpdateTime = maxUpdateTime.substring(0, maxUpdateTime.lastIndexOf('.'));
                }
                startTime = Java8DateUtil.parse(maxUpdateTime).plusSeconds(1).format(Java8DateUtil.sdf1);
            }
        }else {
            //必须是yyyy-MM-dd HH:mm:ss格式
            startTime = Java8DateUtil.parse(startTime).format(Java8DateUtil.sdf1);
        }
        if (StringUtils.isEmpty(endTime)) {
            endTime = LocalDateTime.now().format(Java8DateUtil.sdf1);
        }else {
            //必须是yyyy-MM-dd HH:mm:ss格式
            endTime = Java8DateUtil.parse(endTime).format(Java8DateUtil.sdf1);
        }
        log.warn("======开始同步时间区间:{}-{} 模型编码:{} 的数据同步=======", startTime, endTime, String.join(",", modelCodes));
        //获取Token
        String token = getAuthToken(authUrl,accessKey, secretKey);
        if (StringUtils.isEmpty(token)){
            log.error("获取MDM服务接口授权Token失败");
            return;
        }

        //如果有多个模型编码，则循环拉取
        for (String modelCode : modelCodes) {
            int curPage = 1;
            // 初始化为1，后续从API获取实际值
            int totalPage = 1;
            while (curPage <= totalPage) {
                try {
                    long now = System.currentTimeMillis();
                    JSONObject data = queryModelData(token, modelCode, startTime, endTime, size, curPage);
                    if (data == null) {
                        log.warn("======= 同步模型编码 {} 失败 ========", modelCode);
                    }else {
                        int total = data.getInteger("total");
                        int current = data.getInteger("current");
                        totalPage = data.getInteger("pages");
                        int pageSize = data.getInteger("size");
                        log.warn("====== 本轮总计需要拉取：{} 当前页/总页数：{}/{} 数量：{}", total, current, totalPage, pageSize);
                        if(total > 0) {
                            List<JSONObject> records = data.getJSONArray("records").toJavaList(JSONObject.class);
                            List<DfMdmAlarmtag> dataList = new ArrayList<>();
                            for (JSONObject item : records) {
                                DfMdmAlarmtag alarmtag = mapToMdmAlarmTag(item, tenantId);
                                dataList.add(alarmtag);
                            }
                            if (!dataList.isEmpty()) {
                                dfMdmAlarmtagMapper.batchInsertOrUpdate(dataList);
                            }
                            log.warn("====== 本次成功插入或更新 {} 记录: {} 条, 耗时: {}", modelCode, dataList.size(), System.currentTimeMillis() - now);

                            //同步更新标准点位表
                            if (!dataList.isEmpty()) {
                                List<AlarmPoint> alarmPointList = new ArrayList<>();
                                for (DfMdmAlarmtag dfTag : dataList) {
                                    alarmPointList.add(mapToAlarmPoint(dfTag, dfMdmDeptMappingMap, mdmDeviceMap));
                                }
                                alarmPointMapper.batchInsertOrUpdate(alarmPointList);
                            }
                        }
                    }
                    curPage++;
                    // 添加适当延迟，避免对API造成过大压力
                    Thread.sleep(200);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    log.error("拉取模型数据时同步被中断: {}", e.getMessage());
                    break;
                } catch (Exception e) {
                    log.error("拉取模型数据 {} 第 {} 页时发生错误: {}", modelCode, curPage, e.getMessage(), e);
                    // 跳过当前页继续
                    curPage++;
                }
            }
        }
    }

    @Override
    public ReturnT<String> syncMdmDevice(Integer tenantId) {
        //获取系统中的部门映射关系
        List<DfMdmDeptMapping> dfDeptMappingList = dfMdmDeptMappingMapper.selectList(
                new QueryWrapper<>(DfMdmDeptMapping.class)
                        .isNotNull("sys_dept_code")
        );
        Map<String, DfMdmDeptMapping> dfMdmDeptMappingMap = dfDeptMappingList.stream()
                .collect(Collectors.toMap(
                        //key为DeviceTypeCode
                        DfMdmDeptMapping::getMdmFullCode,
                        //value为对象本身
                        item -> item,
                        //如果有重复key，保留第一个值
                        (existing, replacement) -> existing
                ));

        try {
            //获取df数据解析出来的设备类型
            List<DfMdmIndicator> dfDeviceTypeList = dfMdmIndicatorMapper.queryDeviceTypeGroup();

            //获取系统中的设备类型
            List<MdmDeviceType> mdmDeviceTypeList = iMdmDeviceTypeService.lambdaQuery()
                    .eq(MdmDeviceType::getSource, "2")
                    .eq(MdmDeviceType::getDelFlag, 0)
                    .list();
            Map<String, MdmDeviceType> mdmDeviceTypeMap = mdmDeviceTypeList.stream()
                    .collect(Collectors.toMap(
                            //key为DeviceTypeCode
                            MdmDeviceType::getDeviceTypeCode,
                            //value为对象本身
                            item -> item,
                            //如果有重复key，保留第一个值
                            (existing, replacement) -> existing
                    ));
            mdmDeviceTypeList.clear();

            //处理数据
            for(DfMdmIndicator dfDeviceType : dfDeviceTypeList){
                MdmDeviceType mdmDeviceType = mdmDeviceTypeMap.get(dfDeviceType.getEquipCode());
                //判断系统中是否有该设备类型
                if(null == mdmDeviceType){
                    mdmDeviceType = new MdmDeviceType();
                }

                //解析部门
                String deviceDeptCode = dfDeviceType.getFacCode()+"_"+dfDeviceType.getDeparCode()+"_"+dfDeviceType.getLineCode();
                DfMdmDeptMapping dfMdmDeptMapping = dfMdmDeptMappingMap.get(deviceDeptCode);
                if(null != dfMdmDeptMapping){
                    String lineCode = dfMdmDeptMapping.getSysDeptCode();
                    String deptCode = lineCode.substring(0, 12);
                    String factoryCode = lineCode.substring(0, 9);
                    mdmDeviceType.setTenantId(tenantId);
                    mdmDeviceType.setLineCode(lineCode);
                    mdmDeviceType.setDeptCode(deptCode);
                    mdmDeviceType.setFactoryCode(factoryCode);
                    mdmDeviceType.setDeviceTypeName(dfDeviceType.getEquipDesc());
                    mdmDeviceType.setDeviceTypeCode(dfDeviceType.getEquipCode());
                    mdmDeviceType.setSource("2");
                    mdmDeviceType.setPid("0");
                    mdmDeviceType.setHasChild("0");
                    mdmDeviceTypeList.add(mdmDeviceType);
                }
            }
            iMdmDeviceTypeService.saveOrUpdateBatch(mdmDeviceTypeList);
        } catch (Exception e) {
            log.error("同步模型数据-处理MDM设备类型发生错误: {}", e.getMessage(), e);
            return new ReturnT<>(ReturnT.FAIL_CODE, "同步模型数据-处理MDM设备类型发生错误：" + e.getMessage());
        }

        try {
            //获取df数据解析出来的设备
            List<DfMdmIndicator> dfDeviceList = dfMdmIndicatorMapper.queryDeviceGroup();

            //获取系统中的设备
            List<MdmDevice> mdmDeviceList = iMdmDeviceService.lambdaQuery()
                    .eq(MdmDevice::getSource, "2")
                    .eq(MdmDevice::getDelFlag, 0)
                    .list();
            Map<String, MdmDevice> mdmDeviceMap = mdmDeviceList.stream()
                    .collect(Collectors.toMap(
                            //key为DeviceTypeCode
                            MdmDevice::getDeviceCode,
                            //value为对象本身
                            item -> item,
                            //如果有重复key，保留第一个值
                            (existing, replacement) -> existing
                    ));
            mdmDeviceList.clear();

            //获取系统中的设备类型
            List<MdmDeviceType> mdmDeviceTypeList = iMdmDeviceTypeService.lambdaQuery()
                    .eq(MdmDeviceType::getSource, "2")
                    .eq(MdmDeviceType::getDelFlag, 0)
                    .list();
            Map<String, MdmDeviceType> mdmDeviceTypeMap = mdmDeviceTypeList.stream()
                    .collect(Collectors.toMap(
                            //key为DeviceTypeCode
                            MdmDeviceType::getDeviceTypeCode,
                            //value为对象本身
                            item -> item,
                            //如果有重复key，保留第一个值
                            (existing, replacement) -> existing
                    ));
            //存储处理数据循环中已存在的记录，避免重复
            Map<String, String> existMap = new HashMap<>();
            //处理数据
            for(DfMdmIndicator dfDevice : dfDeviceList){
                String key = dfDevice.getEquipCode()+"_"+dfDevice.getEquipNum();
                if(existMap.containsKey(key)){
                    continue;
                } else {
                    existMap.put(key, "1");
                }
                MdmDevice mdmDevice = mdmDeviceMap.get(key);
                //判断系统中是否有该设备
                if(null == mdmDevice){
                    mdmDevice = new MdmDevice();
                }

                //解析部门
                String deviceDeptCode = dfDevice.getFacCode()+"_"+dfDevice.getDeparCode()+"_"+dfDevice.getLineCode();
                DfMdmDeptMapping dfMdmDeptMapping = dfMdmDeptMappingMap.get(deviceDeptCode);
                if(null != dfMdmDeptMapping){
                    MdmDeviceType mdmDeviceType = mdmDeviceTypeMap.get(dfDevice.getEquipCode());
                    String lineCode = dfMdmDeptMapping.getSysDeptCode();
                    String factoryCode = lineCode.substring(0, 9);
                    String deptCode = lineCode.substring(0, 12);
                    mdmDevice.setTenantId(tenantId);
                    mdmDevice.setLineCode(lineCode);
                    mdmDevice.setDeptCode(deptCode);
                    mdmDevice.setFactoryCode(factoryCode);
                    mdmDevice.setDeviceName(dfDevice.getEquipDesc()+dfDevice.getEquipNumDesc());
                    mdmDevice.setDeviceCode(key);
                    mdmDevice.setSource("2");
                    mdmDevice.setDeviceTypeId(null != mdmDeviceType ? mdmDeviceType.getId() : null);
                    mdmDeviceList.add(mdmDevice);
                }
            }
            iMdmDeviceService.saveOrUpdateBatch(mdmDeviceList);
        } catch (Exception e) {
            log.error("同步模型数据-处理MDM设备类型发生错误: {}", e.getMessage(), e);
            return new ReturnT<>(ReturnT.FAIL_CODE, "同步模型数据-处理MDM设备类型发生错误：" + e.getMessage());
        }
        return ReturnT.SUCCESS;
    }

    /**
     * 重新转换报警点位的 factoryCode、deptCode、lineCode 和 deviceId
     * @param tenantId 租户ID
     * @since 2026.02.07
     */
    @Override
    public void refreshAlarmPointMapping() {
        //获取系统中的部门映射关系
        List<DfMdmDeptMapping> dfDeptMappingList = dfMdmDeptMappingMapper.selectList(
                new QueryWrapper<>(DfMdmDeptMapping.class).isNotNull("sys_dept_code"));
        Map<String, DfMdmDeptMapping> dfMdmDeptMappingMap = dfDeptMappingList.stream()
                .collect(Collectors.toMap(DfMdmDeptMapping::getMdmFullCode, Function.identity(), (k1, k2) -> k1));
        //获取系统中的设备
        List<MdmDevice> mdmDeviceList = iMdmDeviceService.list();
        Map<String, MdmDevice> mdmDeviceMap = mdmDeviceList.stream()
                .collect(Collectors.toMap(MdmDevice::getDeviceCode, Function.identity(), (k1, k2) -> k1));
        //直接从alarmPoint表中获取数据
        List<AlarmPoint> alarmPointList = alarmPointMapper.selectList(Wrappers.<AlarmPoint>lambdaQuery());
        //重新转换报警点位
        for (AlarmPoint alarmPoint : alarmPointList) {
            String funlocCode = alarmPoint.getFuncLocCode();
            if (StringUtils.isNotBlank(funlocCode) && funlocCode.startsWith("APAC-")) {
                String[] funlocParts = funlocCode.split("-");
                if (funlocParts.length >= 3) {
                    //获取产线编码 CN10223001
                    String lineCodePart = funlocParts[1];
                    //获取设备编码 0902
                    String deviceCodePart = funlocParts[2];

                    //处理产线编码，转换为MDM部门映射格式 CN54_22_01
                    String mdmFullCode = convertToMdmFullCode(lineCodePart);

                    //通过部门映射获取系统部门编码
                    if (dfMdmDeptMappingMap.containsKey(mdmFullCode)) {
                        DfMdmDeptMapping deptMapping = dfMdmDeptMappingMap.get(mdmFullCode);
                        String sysDeptCode = deptMapping.getSysDeptCode();
                        if (StringUtils.isNotBlank(sysDeptCode)) {
                            //根据sysDeptCode设置factoryCode, deptCode, lineCode
                            alarmPoint.setLineCode(sysDeptCode);
                            if (sysDeptCode.length() >= 12) {
                                alarmPoint.setDeptCode(sysDeptCode.substring(0, 12));
                            }
                            if (sysDeptCode.length() >= 9) {
                                alarmPoint.setFactoryCode(sysDeptCode.substring(0, 9));
                            }
                        }
                    }
                    //处理设备编码，转换为MdmDevice的device_code格式 CN54_22_80_01_04_01
                    String deviceCode = convertToDeviceCode(lineCodePart, deviceCodePart);
                    alarmPoint.setDeviceCode(deviceCode);

                    //通过设备编码获取deviceId
                    if (mdmDeviceMap.containsKey(deviceCode)) {
                        MdmDevice device = mdmDeviceMap.get(deviceCode);
                        alarmPoint.setDeviceId(device.getId());
                    }
                }
            }
        }
        //批量更新标准点位表
        if (!alarmPointList.isEmpty()) {
            alarmPointMapper.batchInsertOrUpdate(alarmPointList);
            log.warn("====== 成功更新报警点位映射: {} 条 ======", alarmPointList.size());
        }
    }

    /**
     * 请求MDM服务接口获取指定模型编码指定区间的数据
     * @param authToken 授权码
     * @param modelCode 模型编码
     * @param startTime 开始时间
     * @param endTime 绍束时间
     * @param size 每页拉取条数
     * @param current 当前第几页
     * @since 2025.07.16
     */
    private JSONObject queryModelData(String authToken, String modelCode, String startTime, String endTime, int size, int current) {
        try {
            // 使用 FastJSON 构建请求体
            JSONObject requestBody = new JSONObject();
            requestBody.put("modelCode", modelCode);
            //启用状态则新增或修改，停用状态则标记删除
            requestBody.put("enableFlag", "Y");
            requestBody.put("size", size);
            requestBody.put("current", current);
            requestBody.put("startDate", startTime);
            requestBody.put("endDate", endTime);

            String jsonBody = JSON.toJSONString(requestBody, SerializerFeature.WriteMapNullValue);

            // 构建请求
            MediaType mediaType = MediaType.get("application/json");
            Request request = new Request.Builder()
                    .url(modelUrl)
                    .post(RequestBody.create(jsonBody, mediaType))
                    .addHeader("Authorization", authToken)
                    .addHeader("Content-Type", "application/json")
                    .build();

            // 发送请求并处理响应
            try (Response response = httpClient.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    String errorMsg = String.format("模型数据查询失败. 状态码: %d, URL: %s", response.code(), request.url());
                    log.error(errorMsg);
                    return null;
                }

                ResponseBody body = response.body();
                if (body == null) {
                    log.error("模型数据查询失败: 响应体为空");
                    return null;
                }

                String jsonResponse = body.string();
                JSONObject result = JSON.parseObject(jsonResponse);
                if (result.getInteger("code") != 0) {
                    log.error("模型数据查询失败: {}", jsonResponse);
                    return null;
                }
                // 检查数据格式
                if (!result.containsKey("data")) {
                    log.error("模型数据查询返回格式错误: 缺少 'data' 字段. 响应: {}", jsonResponse);
                    return null;
                }
                return result.getJSONObject("data");
            }
        } catch (IOException e) {
            log.error("网络请求异常: 模型数据查询失败. 模型: {}, 开始时间: {}, 结束时间: {}. 错误信息: {}",
                    modelCode, startTime, endTime, e.getMessage(), e);
            return null;
        } catch (JSONException e) {
            log.error("JSON解析异常: 模型数据解析失败. 模型: {}, 开始时间: {}, 结束时间: {}. 错误信息: {}",
                    modelCode, startTime, endTime, e.getMessage(), e);
            return null;
        } catch (Exception e) {
            log.error("未知异常: 模型数据处理失败. 模型: {}, 开始时间: {}, 结束时间: {}. 错误信息: {}",
                    modelCode, startTime, endTime, e.getMessage(), e);
            return null;
        }
    }

    /**
     * 获取接口授权
     */
    private String getAuthToken(String authUrl, String accessKey, String secretKey) {
        try {
            // 构建认证请求体
            JSONObject authBodyJson = new JSONObject();
            authBodyJson.put("accessKey", accessKey);
            authBodyJson.put("secretKey", secretKey);
            String authBody = authBodyJson.toJSONString();

            // 构建请求
            Request authRequest = new Request.Builder()
                    .url(authUrl)
                    .post(RequestBody.create(authBody, MediaType.get("application/json")))
                    .addHeader("Content-Type", "application/json")
                    .build();

            // 发送请求并解析Token
            try (Response response = httpClient.newCall(authRequest).execute()) {
                if (!response.isSuccessful()) {
                    log.error("获取MDM服务接口授权失败. HTTP状态码: {}, URL: {}", response.code(), authUrl);
                    return null;
                }

                if (response.body() == null) {
                    log.error("获取MDM服务接口授权失败: 响应体为空");
                    return null;
                }

                String responseBody = response.body().string();
                JSONObject result = JSON.parseObject(responseBody);
                return result.getString("token");
            }
        } catch (Exception e) {
            log.error("获取MDM服务接口授权时发生错误. URL: {}, 错误信息: {}", authUrl, e.getMessage(), e);
            return null;
        }
    }

    /**
     * JSON对象映射指标字段
     */
    private DfMdmIndicator mapToMdmIndicator(JSONObject item, Integer tenantId) {
        DfMdmIndicator instance = new DfMdmIndicator();
        instance.setTenantId(tenantId);
        // 基本字段映射
        instance.setSysUniqueId(item.getString("sys_unique_id"));
        instance.setDataSourceSysId(item.getString("data_source_sys_id"));
        instance.setSysUniqueCode(item.getString("sys_unique_code"));
        instance.setEnableFlag(item.getString("enable_flag"));
        instance.setCreateBy(item.getString("create_by"));
        instance.setCreateTime(Java8DateUtil.parse(item.getString("create_time")));
        instance.setUpdateBy(item.getString("update_by"));
        instance.setUpdateTime(Java8DateUtil.parse(item.getString("update_time")));
        instance.setCheckErrMsg(item.getString("check_err_msg"));
        instance.setRtMdmDataVersion(item.getString("rt_mdm_data_version"));
        instance.setFunlocCode(item.getString("FUNLOC_CODE"));
        instance.setFunlocDesc(item.getString("FUNLOC_DESC"));
        instance.setIndInstCode(item.getString("IND_INST_CODE"));
        //有空值
        if (StringUtils.isEmpty(instance.getIndStandCode())) {
            instance.setIndStandCode(StringUtils.replace(UUID.randomUUID().toString(), "-", ""));
        }
        // 指标框架字段
        instance.setIndBusiArea(item.getString("IND_BUSI_AREA"));
        instance.setIndCenManDept(item.getString("IND_CEN_MAN_DEPT"));
        instance.setIndName(item.getString("IND_NAME"));
        instance.setIndFraT0(item.getString("IND_FRA_T0"));
        instance.setIndFraT1(item.getString("IND_FRA_T1"));
        instance.setIndFraT2(item.getString("IND_FRA_T2"));
        instance.setIndFraT3(item.getString("IND_FRA_T3"));
        instance.setIndFraT4(item.getString("IND_FRA_T4"));
        instance.setIndFraT5(item.getString("IND_FRA_T5"));
        // 其他描述字段
        instance.setIndStandCode(item.getString("IND_STAND_CODE"));
        instance.setIndStandDesc(item.getString("IND_STAND_DESC"));
        instance.setUnit(item.getString("UNIT") == null ? "" : item.getString("UNIT"));
        instance.setFacCode(item.getString("FAC_CODE"));
        instance.setFacDesc(item.getString("FAC_DESC"));
        // 位置相关字段
        instance.setDeparCode(item.getString("DEPAR_CODE"));
        instance.setDeparDesc(item.getString("DEPAR_DESC"));
        instance.setProcessCode(item.getString("PROCESS_CODE"));
        instance.setProcessDesc(item.getString("PROCESS_DESC"));
        instance.setLineCode(item.getString("LINE_CODE"));
        instance.setLineDesc(item.getString("LINE_DESC"));

        // 设备相关字段
        // EQUIP_CODE 处理
        String equipCode = item.getString("EQUIP_CODE");
        if (StringUtils.isEmpty(equipCode)) {
            equipCode = item.getString("V_EQUIP_CODE");
        }
        instance.setEquipCode(equipCode);
        // EQUIP_DESC 处理
        String equipDesc = item.getString("EQUIP_DESC");
        if (StringUtils.isEmpty(equipDesc)) {
            equipDesc = item.getString("V_EQUIP_DESC");
        }
        instance.setEquipDesc(equipDesc);
        // EQUIP_NUM 处理
        String equipNum = item.getString("EQUIP_NUM");
        if (StringUtils.isEmpty(equipNum)) {
            equipNum = item.getString("V_EQUIP_NUM");
        }
        instance.setEquipNum(equipNum);
        // EQUIP_NUM_DESC 处理
        String equipNumDesc = item.getString("EQUIP_NUM_DESC");
        if (StringUtils.isEmpty(equipNumDesc)) {
            equipNumDesc = item.getString("V_EQUIP_NUM_DESC");
        }
        instance.setEquipNumDesc(equipNumDesc);
        instance.setAcceEquipCode(item.getString("ACCE_EQUIP_CODE"));
        instance.setAcceEquipDesc(item.getString("ACCE_EQUIP_DESC"));
        // 部件相关字段
        instance.setPartNum(item.getString("PART_NUM"));
        instance.setPartNumDesc(item.getString("PART_NUM_DESC"));
        instance.setPart(item.getString("PART"));
        instance.setPartDesc(item.getString("PART_DESC"));
        // 数据源和计算相关字段
        instance.setDataSource(item.getString("DATA_SOURCE"));
        instance.setCalMeth(item.getString("CAL_METH"));
        instance.setCalFormula(item.getString("CAL_FORMULA"));
        // 公式字段太长截断
        if (StringUtils.isNotEmpty(instance.getCalFormula()) && instance.getCalFormula().length() > 1000) {
            instance.setCalFormula(instance.getCalFormula().substring(0, 1000));
        }
        // 能源和业务相关字段
        instance.setPiType(item.getString("PI_TYPE"));
        instance.setRawcamType(item.getString("RAWCAM_TYPE"));
        instance.setBusiType(item.getString("BUSI_TYPE"));
        instance.setProcurementSource(item.getString("PROCUREMENT_SOURCE"));
        instance.setEnergyType(item.getString("ENERGY_TYPE"));
        instance.setEnergySubtype(item.getString("ENERGY_SUBTYPE"));
        instance.setSynchronousStatus(item.getString("SYNCHRONOUS_STATUS"));
        // 备注字段
        instance.setRemarks(item.getString("REMARKS"));
        if (StringUtils.isNotEmpty(instance.getRemarks()) && instance.getRemarks().length() > 500) {
            instance.setRemarks(instance.getRemarks().substring(0, 500));
        }
        instance.setRemarks1(item.getString("REMARKS1"));
        if (StringUtils.isNotEmpty(instance.getRemarks1()) && instance.getRemarks1().length() > 500) {
            instance.setRemarks1(instance.getRemarks1().substring(0, 500));
        }
        instance.setRemarks2(item.getString("REMARKS2"));
        if (StringUtils.isNotEmpty(instance.getRemarks2()) && instance.getRemarks2().length() > 500) {
            instance.setRemarks2(instance.getRemarks2().substring(0, 500));
        }
        instance.setRemarks3(item.getString("REMARKS3"));
        if (StringUtils.isNotEmpty(instance.getRemarks3()) && instance.getRemarks3().length() > 500) {
            instance.setRemarks3(instance.getRemarks3().substring(0, 500));
        }
        // 实时数据相关字段
        instance.setRtDataOrigin(item.getString("rt_data_origin"));
        instance.setRtApplyNo(item.getString("rt_apply_no"));
        instance.setRtTicketNo(item.getString("rt_ticket_no"));
        // 同步和状态字段
        instance.setLockLineStatus(item.getString("LOCK_LINE_STATUS"));
        instance.setInstUseCode(item.getString("INST_USE_CODE"));
        instance.setUnitCn(item.getString("UNIT_CN") == null ? "" : item.getString("UNIT_CN"));
        instance.setBusinessStatus(item.getString("BUSINESS_STATUS"));
        instance.setInstBusinessStatus(item.getString("INST_BUSINESS_STATUS"));
        instance.setSyncTime(LocalDateTime.now());
        return instance;
    }

    /**
     * JSON对象映射配方字段
     */
    private DfMdmRecipe mapToMdmRecipe(JSONObject item, Integer tenantId) {
        DfMdmRecipe recipe = new DfMdmRecipe();
        recipe.setTenantId(tenantId);
        recipe.setFacCode(item.getString("FAC_CODE"));
        recipe.setFacDesc(item.getString("FAC_DESC"));
        recipe.setDeparCode(item.getString("DEPAR_CODE"));
        recipe.setDeparDesc(item.getString("DEPAR_DESC"));
        recipe.setProcessCod(item.getString("PROCESS_COD"));
        recipe.setProcessDesc(item.getString("PROCESS_DESC"));
        recipe.setLineCode(item.getString("LINE_CODE"));
        recipe.setLineSCode(item.getString("LINE_S_CODE"));
        recipe.setRecipeNum(item.getString("RECIPE_NUM"));
        recipe.setRecipeType(item.getString("RECIPE_TYPE"));
        recipe.setRecipeTypeDesc(item.getString("RECIPE_TYPE_DESC"));
        recipe.setFacRecipeName(item.getString("FAC_RECIPE_NAME"));
        recipe.setFacRecipeCode(item.getString("FAC_RECIPE_CODE"));
        recipe.setZoneRecipeCode(item.getString("ZONE_RECIPE_CODE"));
        recipe.setZoneRecipeCnName(item.getString("ZONE_RECIPE_CN_NAME"));
        recipe.setSubBrandOgCode(item.getString("SUB_BRAND_OG_CODE"));
        recipe.setSubBrandOgCnName(item.getString("SUB_BRAND_OG_CN_NAME"));
        recipe.setStandVol(item.getString("STAND_VOL"));
        recipe.setContProdStandCycleTime(item.getString("CONT_PROD_STAND_CYCLE_TIME"));
        recipe.setFirstProdProcessTime(item.getString("FIRST_PROD_PROCESS_TIME"));
        recipe.setVersionNumber(item.getString("VERSION_NUMBER"));
        recipe.setEquipCode(item.getString("EQUIP_CODE"));
        recipe.setEquipDesc(item.getString("EQUIP_DESC"));
        recipe.setEffeDate(Java8DateUtil.parse(item.getString("EFFE_DATE")));
        recipe.setExpiDate(Java8DateUtil.parse(item.getString("EXPI_DATE")));
        recipe.setBusineStatus(item.getString("BUSINE_STATUS"));
        recipe.setAttr1(item.getString("ATTR1"));
        recipe.setAttr2(item.getString("ATTR2"));
        // 系统字段
        recipe.setSysUniqueId(item.getString("sys_unique_id"));
        recipe.setSysUniqueCode(item.getString("sys_unique_code"));
        recipe.setDataSourceSysId(item.getString("data_source_sys_id"));
        recipe.setRtMdmDataVersion(item.getString("rt_mdm_data_version"));
        recipe.setRtDataOrigin(item.getString("rt_data_origin"));
        recipe.setRtApplyNo(item.getString("rt_apply_no"));
        recipe.setRtTicketNo(item.getString("rt_ticket_no"));
        recipe.setCheckErrMsg(item.getString("check_err_msg"));
        recipe.setEnableFlag(item.getString("enable_flag"));
        // 创建 & 更新字段
        recipe.setCreateBy(item.getString("create_by"));
        recipe.setUpdateBy(item.getString("update_by"));
        recipe.setCreateTime(Java8DateUtil.parse(item.getString("create_time")));
        recipe.setUpdateTime(Java8DateUtil.parse(item.getString("update_time")));
        // 同步时间
        recipe.setSyncTime(LocalDateTime.now());

        return recipe;
    }
    /**
     * JSON对象映射指标标准字段
     */
    private DfMdmIndStand mapToMdmIndStand(JSONObject item, Integer tenantId) {
        DfMdmIndStand instance = new DfMdmIndStand();
        instance.setTenantId(tenantId);
        instance.setBizCatalogPath(item.getString("BIZ_CATALOG_PATH"));
        instance.setIndFraT0(item.getString("IND_FRA_T0"));
        instance.setIndFraT1(item.getString("IND_FRA_T1"));
        instance.setIndFraT2(item.getString("IND_FRA_T2"));
        instance.setIndFraT3(item.getString("IND_FRA_T3"));
        instance.setIndFraT4(item.getString("IND_FRA_T4"));
        instance.setIndFraT5(item.getString("IND_FRA_T5"));
        instance.setManaObj(item.getString("MANA_OBJ"));
        instance.setActivity(item.getString("ACTIVITY"));
        instance.setFirTask(item.getString("FIR_TASK"));
        instance.setSecTask(item.getString("SEC_TASK"));
        instance.setIndStandCode(item.getString("IND_STAND_CODE"));
        instance.setCorOrsCode(item.getString("COR_ORS_CODE"));
        instance.setIndNameOld(item.getString("IND_NAME_OLD"));
        instance.setIndNameEn(item.getString("IND_NAME_EN"));
        instance.setIndName(item.getString("IND_NAME"));
        instance.setIndDef(item.getString("IND_DEF"));
        instance.setMonObjType(item.getString("MON_OBJ_TYPE"));
        instance.setMonObj(item.getString("MON_OBJ"));
        instance.setInspItem(item.getString("INSP_ITEM"));
        instance.setInspObjType(item.getString("INSP_OBJ_TYPE"));
        instance.setInspObj(item.getString("INSP_OBJ"));
        instance.setOpStepType(item.getString("OP_STEP_TYPE"));
        instance.setFirProcName(item.getString("FIR_PROC_NAME"));
        instance.setSecProcName(item.getString("SEC_PROC_NAME"));
        instance.setOp1StepName(item.getString("OP1_STEP_NAME"));
        instance.setMeaObj(item.getString("MEA_OBJ"));
        instance.setUnit(item.getString("UNIT"));
        instance.setFunlocFac(item.getString("FUNLOC_FAC"));
        instance.setFunlocDeparDesc(item.getString("FUNLOC_DEPAR_DESC"));
        instance.setFunlocProcessDesc(item.getString("FUNLOC_PROCESS_DESC"));
        instance.setFunlocEquipDesc(item.getString("FUNLOC_EQUIP_DESC"));
        instance.setFunlocAcceEquipDesc(item.getString("FUNLOC_ACCE_EQUIP_DESC"));
        instance.setFunlocPart(item.getString("FUNLOC_PART"));
        instance.setSamPoint(item.getString("SAM_POINT"));
        instance.setMonMethod(item.getString("MON_METHOD"));
        instance.setMonEquip(item.getString("MON_EQUIP"));
        instance.setDataSource(item.getString("DATA_SOURCE"));
        instance.setIndCal(item.getString("IND_CAL"));
        instance.setIndSetPurp(item.getString("IND_SET_PURP"));
        instance.setCalMeth(item.getString("CAL_METH"));
        instance.setCalFormula(item.getString("CAL_FORMULA"));
        instance.setIndStaCycle(item.getString("IND_STA_CYCLE"));
        instance.setIndStaDim(item.getString("IND_STA_DIM"));
        instance.setRefFreq(item.getString("REF_FREQ"));
        instance.setIndCenManDept(item.getString("IND_CEN_MAN_DEPT"));
        instance.setIndOwner(item.getString("IND_OWNER"));
        instance.setPiType(item.getString("PI_TYPE"));
        instance.setRawcamType(item.getString("RAWCAM_TYPE"));
        instance.setBusiType(item.getString("BUSI_TYPE"));
        instance.setProcurementSource(item.getString("PROCUREMENT_SOURCE"));
        instance.setEnergyType(item.getString("ENERGY_TYPE"));
        instance.setEnergySubtype(item.getString("ENERGY_SUBTYPE"));
        instance.setIndNameAlias(item.getString("IND_NAME_ALIAS"));
        instance.setApplyScenario(item.getString("APPLY_SCENARIO"));
        instance.setAccReport(item.getString("ACC_REPORT"));
        instance.setReportField(item.getString("REPORT_FIELD"));
        instance.setRemarks(item.getString("REMARKS"));
        instance.setSynchronousStatus(item.getString("SYNCHRONOUS_STATUS"));
        instance.setUnitCn(item.getString("UNIT_CN"));
        instance.setOp2StepName(item.getString("OP2_STEP_NAME"));
        instance.setSysUniqueId(item.getString("sys_unique_id"));
        instance.setDataSourceSysId(item.getString("data_source_sys_id"));
        instance.setSysUniqueCode(item.getString("sys_unique_code"));
        instance.setEnableFlag(item.getString("enable_flag"));
        instance.setCreateBy(item.getString("create_by"));
        instance.setCreateTime(DateUtil.parse(item.getString("create_time")));
        instance.setUpdateBy(item.getString("update_by"));
        instance.setCreateTime(DateUtil.parse(item.getString("update_time")));
        instance.setCheckErrMsg(item.getString("check_err_msg"));
        instance.setRtMdmDataVersion(item.getString("rt_mdm_data_version"));
        return instance;
    }
    /**
     * JSON对象映射功能位置字段
     */
    private DfMdmFunloc mapToMdmFunlocStand(JSONObject item, Integer tenantId) {
        DfMdmFunloc instance = new DfMdmFunloc();
        instance.setTenantId(tenantId);
        instance.setFunlocCode(item.getString("FUNLOC_CODE"));
        instance.setFunlocDesc(item.getString("FUNLOC_DESC"));
        instance.setPart(item.getString("PART"));
        instance.setAttr1(item.getString("ATTR1"));
        instance.setFacCode(item.getString("FAC_CODE"));
        instance.setDeparCode(item.getString("DEPAR_CODE"));
        instance.setProcessCode(item.getString("PROCESS_CODE"));
        instance.setLineCode(item.getString("LINE_CODE"));
        instance.setEquipCode(item.getString("EQUIP_CODE"));
        instance.setEquipNum(item.getString("EQUIP_NUM"));
        instance.setAcceEquipCode(item.getString("ACCE_EQUIP_CODE"));
        instance.setPartNum(item.getString("PART_NUM"));
        instance.setPartRepl(item.getString("PART_REPL"));
        instance.setPartType(item.getString("PART_TYPE"));
        instance.setPartShortEn(item.getString("PART_SHORT_EN"));
        instance.setFunlocCodeAssi(item.getString("FUNLOC_CODE_ASSI"));
        instance.setStrId(item.getString("STR_ID"));
        instance.setSizeDime(item.getString("SIZE_DIME"));
        instance.setManu(item.getString("MANU"));
        instance.setModel(item.getString("MODEL"));
        instance.setManuPartNum(item.getString("MANU_PART_NUM"));
        instance.setSeriNum(item.getString("SERI_NUM"));
        instance.setTechObjType(item.getString("TECH_OBJ_TYPE"));
        instance.setManuCountry(item.getString("MANU_COUNTRY"));
        instance.setConsYear(item.getString("CONS_YEAR"));
        instance.setConsMonth(item.getString("CONS_MONTH"));
        instance.setMainFac(item.getString("MAIN_FAC"));
        instance.setWorkCenter(item.getString("WORK_CENTER"));
        instance.setMainWorkCenter(item.getString("MAIN_WORK_CENTER"));
        instance.setAbcMark(item.getString("ABC_MARK"));
        instance.setCostCenter(item.getString("COST_CENTER"));
        instance.setPlanGroup(item.getString("PLAN_GROUP"));
        instance.setStartDate(DateUtil.parse(item.getString("START_DATE")));
        instance.setSysStatus(item.getString("SYS_STATUS"));
        instance.setFactDesc(item.getString("FACT_DESC"));
        instance.setDeparDesc(item.getString("DEPAR_DESC"));
        instance.setProcessDesc(item.getString("PROCESS_DESC"));
        instance.setEquipDesc(item.getString("EQUIP_DESC"));
        instance.setAcceEquipDesc(item.getString("ACCE_EQUIP_DESC"));
        instance.setPartDesc(item.getString("PART_DESC"));
        instance.setAttr2(item.getString("ATTR2"));
        instance.setAttr3(item.getString("ATTR3"));
        instance.setBusinessStatus(item.getString("BUSINESS_STATUS"));
        instance.setAttr4(item.getString("ATTR4"));
        instance.setAttr5(item.getString("ATTR5"));
        instance.setDistributeSapFlag(item.getString("DISTRIBUTE_SAP_FLAG"));
        instance.setAttr6(item.getString("ATTR6"));
        instance.setAttr7(item.getString("ATTR7"));
        instance.setAttr8(item.getString("ATTR8"));
        instance.setAttr9(item.getString("ATTR9"));
        instance.setAttr10(item.getString("ATTR10"));
        instance.setLineDesc(item.getString("LINE_DESC"));
        instance.setEquipNumDesc(item.getString("EQUIP_NUM_DESC"));
        instance.setPartBigType(item.getString("PART_BIG_TYPE"));
        instance.setSysUniqueId(item.getString("sys_unique_id"));
        instance.setDataSourceSysId(item.getString("data_source_sys_id"));
        instance.setSysUniqueCode(item.getString("sys_unique_code"));
        instance.setEnableFlag(item.getString("enable_flag"));
        instance.setCreateBy(item.getString("create_by"));
        instance.setCreateTime(DateUtil.parse(item.getString("create_time")));
        instance.setUpdateBy(item.getString("update_by"));
        instance.setCreateTime(DateUtil.parse(item.getString("update_time")));
        instance.setCheckErrMsg(item.getString("check_err_msg"));
        instance.setRtMdmDataVersion(item.getString("rt_mdm_data_version"));
        return instance;
    }

    /**
     * JSON对象映射 IOT 报警点主数据
     */
    private DfMdmAlarmtag mapToMdmAlarmTag(JSONObject item, Integer tenantId) {
        DfMdmAlarmtag tag = new DfMdmAlarmtag();
        tag.setTenantId(tenantId);
        tag.setSysUniqueId(item.getString("sys_unique_id"));
        tag.setSysUniqueCode(item.getString("sys_unique_code"));
        tag.setUniqId(item.getString("UNIQ_ID"));
        tag.setRtMdmDataVersion(item.getString("rt_mdm_data_version"));
        // ===== 功能位置相关 =====
        tag.setTagFunlocCode(item.getString("TAG_FUNLOC_CODE"));
        tag.setTagFunlocCode2(item.getString("TAG_FUNLOC_CODE2"));
        tag.setVfunlocDesc(item.getString("V_FUNLOC_DESC"));
        tag.setVfunlocDesc2(item.getString("V_FUNLOC_DESC2"));
        // ===== IOT 属性 =====
        tag.setIotAttrCode(item.getString("IOT_ATTR_CODE"));
        tag.setIotAttrDispName(item.getString("IOT_ATTR_DISP_NAME"));
        // ===== 报警位信息 =====
        tag.setTagBit(item.getString("TAG_BIT"));
        tag.setTagBitDesc(item.getString("TAG_BIT_DESC"));
        tag.setTagBitDesc2(item.getString("TAG_BIT_DESC2"));
        // ===== 系统与控制字段 =====
        tag.setDataSourceSysId(item.getString("data_source_sys_id"));
        tag.setEnableFlag(item.getString("enable_flag"));
        tag.setCheckErrMsg(item.getString("check_err_msg"));
        tag.setRtDataOrigin(item.getString("rt_data_origin"));
        tag.setRtApplyNo(item.getString("rt_apply_no"));
        tag.setRtTicketNo(item.getString("rt_ticket_no"));
        // ===== 创建 & 更新字段 =====
        tag.setCreateBy(item.getString("create_by"));
        tag.setUpdateBy(item.getString("update_by"));
        tag.setCreateTime(Java8DateUtil.parse(item.getString("create_time")));
        tag.setUpdateTime(Java8DateUtil.parse(item.getString("update_time")));
        // ===== 同步时间 =====
        tag.setSyncTime(LocalDateTime.now());
        return tag;
    }

    /**
     * DF报警点位数据到报警中心标准点位
     */
    private AlarmPoint mapToAlarmPoint(DfMdmAlarmtag alarmTag, Map<String, DfMdmDeptMapping> dfMdmDeptMappingMap, Map<String, MdmDevice> mdmDeviceMap) {
        AlarmPoint alarmPoint = new AlarmPoint();
        alarmPoint.setTenantId(alarmTag.getTenantId());
        //APAC-CN93223001-0902 温州-酿造-糖化-1-糊化锅-2 （下面两个都有可能是汉字或编码）
        String funlocCode = null;
        String funlocDesc = null;
        if(alarmTag.getTagFunlocCode2() != null && alarmTag.getTagFunlocCode2().startsWith("APAC-")){
            funlocCode = alarmTag.getTagFunlocCode2();
            funlocDesc = alarmTag.getVfunlocDesc2();
        }else{
            funlocCode = alarmTag.getVfunlocDesc2();
            funlocDesc = alarmTag.getTagFunlocCode2();
        }
        
        //解析funlocCode获取产线和设备编码
        if (StringUtils.isNotBlank(funlocCode) && funlocCode.startsWith("APAC-")) {
            String[] funlocParts = funlocCode.split("-");
            if (funlocParts.length >= 3) {
                //获取产线编码 CN10223001
                String lineCodePart = funlocParts[1];
                //获取设备编码 0902
                String deviceCodePart = funlocParts[2];
                
                //处理产线编码，转换为MDM部门映射格式 CN54_22_01
                String mdmFullCode = convertToMdmFullCode(lineCodePart);
                
                //通过部门映射获取系统部门编码
                if (dfMdmDeptMappingMap.containsKey(mdmFullCode)) {
                    DfMdmDeptMapping deptMapping = dfMdmDeptMappingMap.get(mdmFullCode);
                    String sysDeptCode = deptMapping.getSysDeptCode();
                    if (StringUtils.isNotBlank(sysDeptCode)) {
                        //根据sysDeptCode设置factoryCode, deptCode, lineCode
                        alarmPoint.setLineCode(sysDeptCode);
                        if (sysDeptCode.length() >= 12) {
                            alarmPoint.setDeptCode(sysDeptCode.substring(0, 12));
                        }
                        if (sysDeptCode.length() >= 9) {
                            alarmPoint.setFactoryCode(sysDeptCode.substring(0, 9));
                        }
                    }
                }
                //处理设备编码，转换为MdmDevice的device_code格式 CN54_22_80_01_04_01
                String deviceCode = convertToDeviceCode(lineCodePart, deviceCodePart);
                alarmPoint.setDeviceCode(deviceCode);
                
                //通过设备编码获取deviceId
                if (mdmDeviceMap.containsKey(deviceCode)) {
                    MdmDevice device = mdmDeviceMap.get(deviceCode);
                    alarmPoint.setDeviceId(device.getId());
                }
            }
        }
        //原有逻辑作为 fallback
        if (StringUtils.isAnyEmpty(alarmPoint.getFactoryCode(), alarmPoint.getDeptCode(), alarmPoint.getLineCode())) {
            if (StringUtils.isNotBlank(funlocDesc)) {
                String[] tagFunlocCodeArr = funlocDesc.split("-");
                if (StringUtils.isEmpty(alarmPoint.getFactoryCode())) {
                    alarmPoint.setFactoryCode(tagFunlocCodeArr[0]);
                }
                if (StringUtils.isEmpty(alarmPoint.getDeptCode())) {
                    alarmPoint.setDeptCode(tagFunlocCodeArr[1]);
                }
                if (StringUtils.isEmpty(alarmPoint.getLineCode()) && tagFunlocCodeArr.length > 3) {
                    alarmPoint.setLineCode(tagFunlocCodeArr[2] + "-" + tagFunlocCodeArr[3]);
                }
                if (StringUtils.isEmpty(alarmPoint.getDeviceCode()) && tagFunlocCodeArr.length > 5) {
                    alarmPoint.setDeviceCode(tagFunlocCodeArr[4] + "-" + tagFunlocCodeArr[5]);
                }
            } else {
                String vfunlocDesc = alarmTag.getVfunlocDesc();
                if (StringUtils.isNotBlank(vfunlocDesc)) {
                    String[] vfunlocDescArr = vfunlocDesc.split("-");
                    if (StringUtils.isEmpty(alarmPoint.getFactoryCode())) {
                        alarmPoint.setFactoryCode(vfunlocDescArr[0]);
                    }
                    if (StringUtils.isEmpty(alarmPoint.getDeptCode())) {
                        alarmPoint.setDeptCode(vfunlocDescArr[1]);
                    }
                }
            }
        }
        alarmPoint.setAlarmName(alarmTag.getIotAttrDispName() + (StringUtils.isNotBlank(alarmTag.getTagBitDesc())?("_" + alarmTag.getTagBitDesc()):""));
        alarmPoint.setAlarmPiCode(alarmTag.getUniqId());
        alarmPoint.setFuncLocCode(funlocCode);
        alarmPoint.setSourceSystem("MDM");
        alarmPoint.setEnableFlag("Y".equals(alarmTag.getEnableFlag())?1:0);
        alarmPoint.setCreateTime(LocalDateTime.now());
        alarmPoint.setCreateBy("SYSTEM");
        return alarmPoint;
    }
    
    /**
     * 将产线编码转换为MDM部门映射格式
     * 例如：CN93223001 -> CN93_22_30_01
     */
    private String convertToMdmFullCode(String lineCodePart) {
        if (StringUtils.isEmpty(lineCodePart) || lineCodePart.length() < 8) {
            return lineCodePart;
        }
        //提取前两位 CN
        String prefix = lineCodePart.substring(0, 2);
        //提取接下来的两位 93
        String part1 = lineCodePart.substring(2, 4);
        //提取接下来的两位 22
        String part2 = lineCodePart.substring(4, 6);
        //提取接下来的两位 30
        String part3 = lineCodePart.substring(6, 8);
        //提取接下来的两位 01
        String part4 = lineCodePart.substring(8, 10);
        return prefix + part1 + "_" + part2 + "_" + part3 + "_" + part4;
    }
    
    /**
     * 将产线编码和设备编码转换为MdmDevice的device_code格式
     * 例如：CN10223001 + 0902 -> CN10_22_30_01_09_02
     */
    private String convertToDeviceCode(String lineCodePart, String deviceCodePart) {
        if (StringUtils.isEmpty(lineCodePart) || lineCodePart.length() < 8 || StringUtils.isEmpty(deviceCodePart)) {
            return lineCodePart + "_" + deviceCodePart;
        }
        //提取前两位 CN
        String prefix = lineCodePart.substring(0, 2);
        //提取接下来的两位 10
        String part1 = lineCodePart.substring(2, 4);
        //提取接下来的两位 22
        String part2 = lineCodePart.substring(4, 6);
        //提取接下来的两位 30
        String part3 = lineCodePart.substring(6, 8);
        //提取接下来的两位 01
        String part4 = lineCodePart.substring(8, 10);
        //提取设备编码的前两位 09
        String devicePart1 = deviceCodePart.length() >= 2 ? deviceCodePart.substring(0, 2) : deviceCodePart;
        //提取设备编码的后两位 02
        String devicePart2 = deviceCodePart.length() >= 4 ? deviceCodePart.substring(2, 4) : "";
        
        StringBuilder deviceCode = new StringBuilder();
        deviceCode.append(prefix).append(part1).append("_").append(part2).append("_").append(part3).append("_").append(part4);
        if (StringUtils.isNotBlank(devicePart1)) {
            deviceCode.append("_").append(devicePart1);
        }
        if (StringUtils.isNotBlank(devicePart2)) {
            deviceCode.append("_").append(devicePart2);
        }
        return deviceCode.toString();
    }


    /**
     * 通用的MDM数据同步方法
     * @param tenantId 租户ID
     * @param startTime 同步起始时间
     * @param endTime 同步结束时间
     * @param size 每页拉取多少条
     * @param modelCode 同步模型编码
     * @param mapper 查询最新更新时间的Mapper
     * @param queryMaxTimeMethod 用于查询最大时间的方法引用
     * @param mapFunction 将JSONObject映射为实体对象的函数
     * @param batchInsertOrUpdateMethod 批量插入或更新的方法引用
     */
    private <T, M> void syncMdmDataGeneric(
            Integer tenantId,
            String startTime,
            String endTime,
            int size,
            String modelCode,
            M mapper,
            Function<M, String> queryMaxTimeMethod,
            BiFunction<JSONObject, Integer, T> mapFunction,
            Consumer<List<T>> batchInsertOrUpdateMethod) {

        String processedStartTime = processStartTime(queryMaxTimeMethod.apply(mapper), startTime);
        String processedEndTime = processEndTime(endTime);

        String token = getAuthToken(authUrl, accessKey, secretKey);
        if (StringUtils.isEmpty(token)) {
            log.error("模型编码:{}--获取MDM服务接口授权Token失败", modelCode);
            return;
        }

        int curPage = 1;
        // 初始化为1，后续从API获取实际值
        int totalPage = 1;
        while (curPage <= totalPage) {
            try {
                JSONObject data = queryModelData(token, modelCode, processedStartTime, processedEndTime, size, curPage);
                if (data == null) {
                    log.error("模型编码:{}--获取MDM数据为null", modelCode);
                } else {
                    totalPage = data.getInteger("pages");
                    int pageSize = data.getInteger("size");
                    if (pageSize > 0) {
                        List<JSONObject> records = data.getJSONArray("records").toJavaList(JSONObject.class);
                        List<T> dataList = new ArrayList<>();
                        for (JSONObject item : records) {
                            T instance = mapFunction.apply(item, tenantId);
                            dataList.add(instance);
                        }
                        if (!dataList.isEmpty()) {
                            batchInsertOrUpdateMethod.accept(dataList);
                        }
                    }
                }
                curPage++;
                // 添加适当延迟，避免对API造成过大压力
                Thread.sleep(200);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.error("模型编码:{}--同步报错：{}", modelCode, e.getMessage());
                break;
            } catch (Exception e) {
                log.error("拉取模型数据 {} 第 {} 页时发生错误: {}", modelCode, curPage, e.getMessage(), e);
                // 跳过当前页继续
                curPage++;
            }
        }
    }
    /**
     * 处理开始时间
     */
    private String processStartTime(String maxUpdateTime, String startTime) {
        if (StringUtils.isEmpty(startTime)) {
            // 如果没有提供开始时间，从数据库中获取最新的更新时间
            if (maxUpdateTime != null) {
                if (maxUpdateTime.contains(".")) {
                    maxUpdateTime = maxUpdateTime.substring(0, maxUpdateTime.lastIndexOf('.'));
                }
                return Java8DateUtil.parse(maxUpdateTime).plusSeconds(1).format(Java8DateUtil.sdf1);
            }
            return null;
        } else {
            // 必须是yyyy-MM-dd HH:mm:ss格式
            return Java8DateUtil.parse(startTime).format(Java8DateUtil.sdf1);
        }
    }

    /**
     * 处理结束时间
     */
    private String processEndTime(String endTime) {
        if (StringUtils.isEmpty(endTime)) {
            return LocalDateTime.now().format(Java8DateUtil.sdf1);
        } else {
            // 必须是yyyy-MM-dd HH:mm:ss格式
            return Java8DateUtil.parse(endTime).format(Java8DateUtil.sdf1);
        }
    }

}
