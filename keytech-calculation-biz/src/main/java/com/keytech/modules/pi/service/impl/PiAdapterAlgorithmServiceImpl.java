package com.keytech.modules.pi.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.keytech.base.service.impl.BaseServiceImpl;
import com.keytech.modules.kpi.util.DateUtils;
import com.keytech.modules.pi.common.CommonConstants;
import com.keytech.modules.pi.common.PiCommonConstants;
import com.keytech.modules.pi.dto.*;
import com.keytech.modules.pi.entity.*;
import com.keytech.modules.pi.mapper.PiAdapterAlgorithmMapper;
import com.keytech.modules.pi.service.*;
import com.keytech.modules.pi.util.AdapterOnlyEnum;
import com.keytech.modules.pi.util.DateConvertUtil;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.SecurityUtils;
import org.jeecg.common.system.vo.LoginUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

/**
 * @Description: Adapter-算法配置
 * @Author: jeecg-boot
 * @Date:   2025-09-04
 * @Version: V1.0
 */
@Service
@Slf4j
public class PiAdapterAlgorithmServiceImpl extends BaseServiceImpl<PiAdapterAlgorithmMapper, PiAdapterAlgorithm> implements IPiAdapterAlgorithmService {
    private static final Logger logger = LoggerFactory.getLogger(PiAdapterAlgorithmServiceImpl.class);
    private static final Set<String> TIME_FIELDS = Set.of(
            "startTime", "startDate", "endTime", "endDate", "runtime", "formula", "batchStartTime", "deviceName","ordercatName"
    );
    // 定义一个固定大小的线程池
    private final ExecutorService executorService = Executors.newFixedThreadPool(50);
    @Autowired
    private IPiAdapterAlgorithmConditionService iPiAdapterAlgorithmConditionService;
    @Autowired
    private IPiSistarRtStepParamsService iPiSistarRtStepParamsService;
    @Autowired
    private IPiCustomizeTableService iPiCustomizeTableService;
    @Autowired
    private IPiCustomizeTableFieldService iPiCustomizeTableFieldService;
    @Autowired
    private IPiAdapterUnitService iPiAdapterUnitService;
    @Autowired
    private IPiAdapterUnitEopService iPiAdapterUnitEopService;
    @Autowired
    private IPiSistarRtStepsService iPiSistarRtStepsService;
    @Autowired
    private IPiSistarRtBatchesService iPiSistarRtBatchesService;
    @Autowired
    private IPiAllocationTableFieldService iPiAllocationTableFieldService;
    @Autowired
    private IPiTaskParameterService piTaskParameterService;
    @Autowired
    private IPiRaStepParamService iPiRaStepParamService;
    @Autowired
    private IPiRaStepService iPiRaStepService;
    @Autowired
    private IPiRetrievalSignalService iPiRetrievalSignalService;

    /**
     * @Description: 添加算法信息
     * @param pageList
     * @return
     */
    @Override
    public IPage<PiAdapterAlgorithm> addInformation(IPage<PiAdapterAlgorithm> pageList) {
        List<PiAdapterAlgorithm> signalList = pageList.getRecords();
        //计算信号 id集合
        List<String> idList = signalList.stream()
                .map(PiAdapterAlgorithm::getId)
                .collect(Collectors.toList());
        if(idList != null && idList.isEmpty()){
            return pageList;
        }

        //匹配字典
        //设备
        List<PiAdapterUnit> unitList = iPiAdapterUnitService.list();
        //EOP号
        List<PiAdapterUnitEop> unitEopList = iPiAdapterUnitEopService.list();

        //获取设备map key为 区域+"_"+PCU+"_"+设备编号 value为设备对象
        Map<String, PiAdapterUnit> unitMap = unitList.stream()
                .collect(Collectors.toMap(
                        piAdapterUnit -> piAdapterUnit.getAreaNo() + "_" + piAdapterUnit.getPcuNo()+ "_" +  piAdapterUnit.getUnitNo(),
                        piAdapterUnit -> piAdapterUnit,
                        (existing, replacement) -> existing  // 冲突时保留已存在的
                ));
        //获取EOP号map key为 区域+"_"+PCU+"_"+设备编号+"_"+EOP号 value为EOP对象
        Map<String, PiAdapterUnitEop> unitEopMap = unitEopList.stream()
                .collect(Collectors.toMap(
                        piAdapterUnitEop -> piAdapterUnitEop.getAreaNo() + "_" + piAdapterUnitEop.getPcuNo() +"_" + piAdapterUnitEop.getUnitNo() + "_" + piAdapterUnitEop.getEopNo(),
                        piAdapterUnitEop -> piAdapterUnitEop,
                        (existing, replacement) -> existing  // 冲突时保留已存在的
                ));

        // 限制条件
        LambdaQueryWrapper<PiAdapterAlgorithmCondition> conditionWrapper = new LambdaQueryWrapper<>();
        conditionWrapper.in(PiAdapterAlgorithmCondition::getAlgorithmId, idList);
        List<PiAdapterAlgorithmCondition> conditionList = iPiAdapterAlgorithmConditionService.list(conditionWrapper);

        Map<String, List<PiAdapterAlgorithmCondition>> conditionMap = conditionList.stream()
                .collect(Collectors.groupingBy(PiAdapterAlgorithmCondition::getAlgorithmId));

        signalList.forEach(signal -> {
            String algorithmId = signal.getId();
            if (conditionMap.containsKey(algorithmId)) {
                // 获取所有匹配
                String combinedExplanation = conditionMap.get(algorithmId).stream()
                        .map(PiAdapterAlgorithmCondition::getExplanation)
                        .filter(explanation -> explanation != null && !explanation.isEmpty())
                        .collect(Collectors.joining("\n")); // 用两个换行符连接
                signal.setConditionExplanation(combinedExplanation);
            }
            //匹配设备名称
            if (unitMap.containsKey(signal.getAreaNo() + "_" +signal.getPcuNo()+"_"+signal.getUnitNo())) {
                signal.setUnitName(unitMap.get(signal.getAreaNo() + "_" +signal.getPcuNo()+ "_"+ signal.getUnitNo()).getUnitName());
            }
            //匹配EOP号名称
            if (unitEopMap.containsKey(signal.getAreaNo() + "_" + signal.getPcuNo() +"_" + signal.getUnitNo() + "_" + signal.getEopNo())) {
                signal.setEopName(unitEopMap.get(signal.getAreaNo() + "_" + signal.getPcuNo() +"_" + signal.getUnitNo() + "_" + signal.getEopNo()).getEopName());
            }

        });

        return pageList;
    }


    /**
     * @Description: 添加算法
     * @param adapterAlgorithmDataDto
     */
    @Override
    @Transactional
    public void addAlgorithm(AdapterAlgorithmDataDto adapterAlgorithmDataDto) {
        PiAdapterAlgorithm piAdapterAlgorithm = adapterAlgorithmDataDto.getPiAdapterAlgorithm();
        if(piAdapterAlgorithm != null){
            //存储算法配置
            saveOrUpdate(piAdapterAlgorithm);
            //存储约束
            //存储-取数信号
            String json = adapterAlgorithmDataDto.getAdapterAlgorithmCondition();
            List<PiAdapterAlgorithmCondition> algorithmConditionList = new ArrayList<>();
            if(StrUtil.isNotBlank(json)){
                JSONArray jsonArray = JSONUtil.parseArray(json);
                Double num = 1.0;
                for (Object object : jsonArray) {
                    String identifier = IdUtil.simpleUUID();
                    JSONArray arr = (JSONArray)object;
                    for (Object obj : arr) {
                        PiAdapterAlgorithmCondition adapterAlgorithmCondition = BeanUtil.copyProperties(obj, PiAdapterAlgorithmCondition.class);
                        adapterAlgorithmCondition.setAlgorithmId(piAdapterAlgorithm.getId());
                        adapterAlgorithmCondition.setIdentifier(identifier);
                        adapterAlgorithmCondition.setSort(num);
                        adapterAlgorithmCondition.setDelFlag(0);
                        algorithmConditionList.add(adapterAlgorithmCondition);
                    }
                    num++;
                }
            }
            if(null != algorithmConditionList){
                //获取之前存储的取数信号
                LambdaQueryWrapper<PiAdapterAlgorithmCondition> queryWrapper = new LambdaQueryWrapper<>();
                queryWrapper.eq(PiAdapterAlgorithmCondition::getAlgorithmId,piAdapterAlgorithm.getId());
                List<PiAdapterAlgorithmCondition> algorithmConditionOldList = iPiAdapterAlgorithmConditionService.list(queryWrapper);
                //获取删除
                List<PiAdapterAlgorithmCondition> listDel = new ArrayList<>();
                for (PiAdapterAlgorithmCondition algorithmConditionOld : algorithmConditionOldList) {
                    boolean isDel = true;
                    for (PiAdapterAlgorithmCondition algorithmConditionNew : algorithmConditionList) {
                        if(StrUtil.isNotBlank(algorithmConditionNew.getId())) {
                            if (algorithmConditionNew.getId().equals(algorithmConditionOld.getId())) {
                                isDel = false;
                                break;
                            }
                        }
                    }
                    if(isDel){
                        listDel.add(algorithmConditionOld);
                    }
                }
                //删除
                iPiAdapterAlgorithmConditionService.removeBatchByIds(listDel);
                //新增/修改
                iPiAdapterAlgorithmConditionService.saveOrUpdateBatch(algorithmConditionList);
            }
        }

    }

    /**
     * @Description: 获取数据
     * @param piAdapterAlgorithm
     * @return
     */
    @Override
    public AdapterAlgorithmDataDto adapterData(PiAdapterAlgorithm piAdapterAlgorithm) {
        AdapterAlgorithmDataDto adapterAlgorithmDataDto = new AdapterAlgorithmDataDto();
        String algorithmId = piAdapterAlgorithm.getId();
        if(StrUtil.isNotBlank(algorithmId)){
            //条件 [[{id:1,identifier:2},{id:2,identifier:2}],[{id:3,identifier:3}]]
            LambdaQueryWrapper<PiAdapterAlgorithmCondition> wrapper = Wrappers.lambdaQuery(PiAdapterAlgorithmCondition.class);
            wrapper.eq(PiAdapterAlgorithmCondition::getAlgorithmId,algorithmId);
            wrapper.orderByAsc(PiAdapterAlgorithmCondition::getSort)
                                  .orderByAsc(PiAdapterAlgorithmCondition::getId);
            List<PiAdapterAlgorithmCondition> list = iPiAdapterAlgorithmConditionService.list(wrapper);
            // 将相同分组的对象归为一组
            Map<String, List<PiAdapterAlgorithmCondition>> groupedMap = new HashMap<>();
            for (PiAdapterAlgorithmCondition object : list) {
                String identifier = object.getIdentifier();
                if (!groupedMap.containsKey(identifier)) {
                    groupedMap.put(identifier, new ArrayList<>());
                }
                groupedMap.get(identifier).add(object);
            }
            // 将分组后的结果转换为符合要求的格式（列表的列表）
            List<List<PiAdapterAlgorithmCondition>> finalList = new ArrayList<>(groupedMap.values());
            // 使用ObjectMapper将结果转换为JSON字符串
            ObjectMapper objectMapper = new ObjectMapper();
            String json = null;
            try {
                json = objectMapper.writeValueAsString(finalList);
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
            adapterAlgorithmDataDto.setAdapterAlgorithmCondition(json);
        }
        return adapterAlgorithmDataDto;
    }

    /**
     * @Description: Adapter-计算
     * @param list 步骤数据
     *  获取值
     * 表名获取和需要写入的字段 满足取数信号(区域/设备/EOP)的算法配置
     * 唯一约束 - 通过表获取唯一约束字段,将值匹配唯一约束字段  如 field_1(area_no) 将area_no 值写入field_1
     * [字段=值]-[表]-[约束条件(字段=值)]
     */
    @Override
    public void calculate(List<PiSistarRtSteps> list,List<PiAdapterAlgorithm> listAlgorithm) {
        if(list != null && list.isEmpty()){
            log.info("Adapter-计算-没有数据");
            return;
        }
        //获取算法配置
        if (listAlgorithm == null){
            LambdaQueryWrapper<PiAdapterAlgorithm> queryWrapper = Wrappers.lambdaQuery(PiAdapterAlgorithm.class);
            queryWrapper.eq(PiAdapterAlgorithm::getType, PiCommonConstants.OPEN); //
            queryWrapper.eq(PiAdapterAlgorithm::getDelFlag,CommonConstants.NOT_DELETED);
            listAlgorithm = list(queryWrapper);
        }

        //获取map key为 区域、PCU、设备、EOP下横线分隔 value为对象
        Map<String, List<PiAdapterAlgorithm>> algorithmMap = listAlgorithm.stream()
            .collect(Collectors.groupingBy(
                piAdapterAlgorithm -> piAdapterAlgorithm.getAreaNo() + "_" +
                    piAdapterAlgorithm.getPcuNo() + "_" +
                    piAdapterAlgorithm.getUnitNo() + "_" + piAdapterAlgorithm.getEopNo()
            ));

        //获取条件配置
        List<PiAdapterAlgorithmCondition> listCondition = iPiAdapterAlgorithmConditionService.list();
        //将条件分组 按算法id
        Map<String, List<PiAdapterAlgorithmCondition>> conditionMap = listCondition.stream()
                .collect(Collectors.groupingBy(PiAdapterAlgorithmCondition::getAlgorithmId));


        //获取Adapter数据抽取-步骤参数
        //[问题不能查询全量数据]-抽取改成 关联子表
        //list开始时间集合
        List<Date> startTimeList = list.stream()
                .map(PiSistarRtSteps::getStartTime)
                .collect(Collectors.toList());
        //获取步骤参数
        Map<String, List<PiSistarRtStepParams>> stepParamsMap = getStepParam(startTimeList);

        //获取配置Adapter唯一约束字段
        LambdaQueryWrapper<PiCustomizeTableField> tableFieldqueryWrapper = Wrappers.lambdaQuery(PiCustomizeTableField.class);
        tableFieldqueryWrapper.eq(PiCustomizeTableField::getDelFlag,0);

        //表字段 adapterOnly 不为空
        tableFieldqueryWrapper.isNotNull(PiCustomizeTableField::getAdapterOnly);
        List<PiCustomizeTableField> listTableField = iPiCustomizeTableFieldService.list(tableFieldqueryWrapper);
        //获取map key为报表名称tableName value为对象集合
        Map<String, List<PiCustomizeTableField>> tableFieldMap = listTableField.stream()
                .collect(Collectors.groupingBy(
                        PiCustomizeTableField::getTableName
                ));

        for (PiSistarRtSteps piSistarRtSteps : list) {
            PiSistarRtSteps piSistarRtStepsNew = BeanUtil.copyProperties(piSistarRtSteps, PiSistarRtSteps.class);
            //区域
            String areaNo = piSistarRtSteps.getAreaNo()+"";
            //年
            String yearNo = piSistarRtSteps.getYearNo()+"";
            //EOP
            String eopNo = piSistarRtSteps.getEopNo()+"";
            //订单
            String orderNo = piSistarRtSteps.getOrderNo();
            //批次
            String batchNo = piSistarRtSteps.getBatchNo();
            //PCU
            String pcuNo = piSistarRtSteps.getPcuNo()+"";
            //reccatNo
            String reccatNo = piSistarRtSteps.getReccatNo()+"";
            //开始时间(步骤)
            Date stepStartTime = piSistarRtSteps.getStartTime();
            //产线
            String lineNo = piSistarRtSteps.getOrdercatName()+"";
            //配方
            String recipeNameOnly = piSistarRtSteps.getRecipeName();
            //开始时间
            String startTimeOnly = "";
            Date startDateTime = piSistarRtSteps.getStartTime();
            if(startDateTime != null){
                startTimeOnly = DateUtils.formatDateTime(startDateTime);
            }


            //判断是否有算法
            String key = areaNo + "_" + piSistarRtSteps.getPcuNo() +"_" + piSistarRtSteps.getUnitNo() + "_" + eopNo;
            if(algorithmMap.containsKey(key)) {
                //算法配置
                List<PiAdapterAlgorithm> piAdapterAlgorithmList = algorithmMap.get(key);
                for (PiAdapterAlgorithm piAdapterAlgorithm : piAdapterAlgorithmList) {
                    //步骤条件
                    String stepCondition = piAdapterAlgorithm.getStepCondition();
                    if(StrUtil.isNotBlank(stepCondition)){
                        if(!stepConditionPass(piSistarRtSteps , stepCondition)){
                            continue;
                        }
                    }

                    //表名
                    String tableName = piAdapterAlgorithm.getPiTable();
                    //字段名
                    String piCode = piAdapterAlgorithm.getPiCode();
                    String algorithmId = piAdapterAlgorithm.getId();
                    if (StrUtil.isNotBlank(algorithmId)) {
                        //约束条件
                        List<PiAdapterAlgorithmCondition> conditionList = conditionMap.get(algorithmId);
                        //取第几次(步骤)
                        Integer several = piAdapterAlgorithm.getSeveral();
                        //取值(时间/时长/参数)
                        String val = piAdapterAlgorithm.getVal();
                        //取值类型0设定值/1实际值
                        String valType = piAdapterAlgorithm.getValType();
                        //ropId
                        String ropId = piAdapterAlgorithm.getRopId();
                        //时间格式 1分  2时  3天
                        Integer timeFormat = piAdapterAlgorithm.getTimeFormat();

                        // 取第几条 或者 判断ropId several为 -1 取最后一条
                        if ((several != null && (several > 0 || several == -1)) || StrUtil.isNotBlank(ropId)) {
                            LambdaQueryWrapper<PiSistarRtSteps> stepWrapper = Wrappers.lambdaQuery(PiSistarRtSteps.class);
                            stepWrapper.eq(PiSistarRtSteps::getAreaNo, areaNo);
                            stepWrapper.eq(PiSistarRtSteps::getYearNo, yearNo);
                            stepWrapper.eq(PiSistarRtSteps::getOrderNo, orderNo);
                            stepWrapper.eq(PiSistarRtSteps::getBatchNo, batchNo);
                            stepWrapper.eq(PiSistarRtSteps::getPcuNo, pcuNo);
                            stepWrapper.eq(PiSistarRtSteps::getEopNo, eopNo);
                            stepWrapper.orderByAsc(PiSistarRtSteps::getEventTime);
                            List<PiSistarRtSteps> listStep = iPiSistarRtStepsService.list(stepWrapper);
                            //先判断第几步
                            if (listStep != null && several != null && listStep.size() > 0 && several <= listStep.size()) {
                                //several等于-1取最后一条
                                if (several == -1) {
                                    piSistarRtStepsNew = listStep.get(listStep.size() - 1);
                                }else{
                                    piSistarRtStepsNew = listStep.get(several - 1);
                                }
                                //判断步骤条件
                                if(!stepConditionPass(piSistarRtStepsNew , piAdapterAlgorithm.getStepCondition())){
                                    piSistarRtStepsNew = null;
                                }
                                //判断ropId是否相等
                                if(piSistarRtStepsNew == null || (StrUtil.isNotBlank(ropId) &&
                                        !ropId.equals(piSistarRtStepsNew.getRopId()))){
                                    piSistarRtStepsNew = null;
                                }
                                //判断开始时间是否与之前的相同,相同不做更新
                                if (piSistarRtStepsNew != null && stepStartTime.getTime() != piSistarRtStepsNew.getStartTime().getTime()) {
                                    stepStartTime = piSistarRtStepsNew.getStartTime();
                                    startTimeList.add(stepStartTime);
                                    //stepParamsMap = getStepParam(startTimeList);

                                    List<Date> timeList = new ArrayList<>();
                                    timeList.add(stepStartTime);
                                    Map<String, List<PiSistarRtStepParams>> stepParamsNewMap = getStepParam(timeList);
                                    stepParamsMap.putAll(stepParamsNewMap);
                                }
                            }else{
                                piSistarRtStepsNew = null;
                            }
                            //判断ropId
                            if (StrUtil.isNotBlank(ropId)) {
                                //遍历listStep判断ropId一致的步骤 lamd方法
                                listStep = listStep.stream().filter(step -> String.valueOf(step.getRopId()).equals(ropId)).collect(Collectors.toList());
                                if (listStep != null && listStep.size() > 0) {
                                    piSistarRtStepsNew = listStep.get(0);
                                    //判断步骤条件
                                    if(!stepConditionPass(piSistarRtStepsNew , piAdapterAlgorithm.getStepCondition())){
                                        piSistarRtStepsNew = null;
                                    }

                                    if (piSistarRtStepsNew != null && stepStartTime.getTime() != piSistarRtStepsNew.getStartTime().getTime()) {
                                        stepStartTime = piSistarRtStepsNew.getStartTime();
                                        startTimeList.add(stepStartTime);
                                        //stepParamsMap = getStepParam(startTimeList);

                                        List<Date> timeList = new ArrayList<>();
                                        timeList.add(stepStartTime);
                                        Map<String, List<PiSistarRtStepParams>> stepParamsNewMap = getStepParam(timeList);
                                        stepParamsMap.putAll(stepParamsNewMap);
                                    }
                                }else{
                                    piSistarRtStepsNew = null;
                                }
                            }
                            //判断是否匹配上
                            if (piSistarRtStepsNew == null){
                                continue;
                            }else{
                                piSistarRtSteps = piSistarRtStepsNew;
                            }
                        }

                        //获取步骤参数
                        String paramKey = piSistarRtSteps.getAreaNo() + "_" + piSistarRtSteps.getYearNo() + "_" +
                                piSistarRtSteps.getOrderNo() + "_" + piSistarRtSteps.getBatchNo() + "_" +
                                piSistarRtSteps.getEopNo() + "_" + piSistarRtSteps.getStartTime();
                        //步骤参数
                        List<PiSistarRtStepParams> stepParamsList = stepParamsMap.get(paramKey);

                        // 写表对象
                        TableSaveDto tableSaveDto = new TableSaveDto();
                        //表名
                        tableSaveDto.setTable(tableName);
                        //开始/结束时间/时长
                        if (StrUtil.isNotBlank(val) && TIME_FIELDS.contains(val)) {
                            String fieldVal = "";
                            // 获取(开始/结束时间/时长)
                            if ("startTime".equals(val)) {
                                //开始时间
                                Date startTime = piSistarRtSteps.getStartTime();
                                fieldVal = DateUtils.formatDateTime(startTime);
                            } else if ("startDate".equals(val)) {
                                //开始时间(日期)
                                Date startTime = piSistarRtSteps.getStartTime();
                                fieldVal = DateUtils.formatDate(startTime);
                            } else if ("endTime".equals(val)) {
                                //结束时间
                                Date startTime = piSistarRtSteps.getEndTime();
                                fieldVal = DateUtils.formatDateTime(startTime);
                            } else if ("endDate".equals(val)) {
                                //结束时间(日期)
                                Date startTime = piSistarRtSteps.getEndTime();
                                fieldVal = DateUtils.formatDate(startTime);
                            } else if ("runtime".equals(val)) {
                                //时长
                                Integer duration = piSistarRtSteps.getDuration();
                                fieldVal = duration + "";
                            } else if ("formula".equals(val)) {
                                //配方
                                String recipeName = piSistarRtSteps.getRecipeName();
                                fieldVal = recipeName;
                            } else if ("batchStartTime".equals(val)) {
                                //批次开始时间
                                //获取批次数据 (区域 步骤  年 订单 批此 reccat)
                                LambdaQueryWrapper<PiSistarRtBatches> batchWrapper = Wrappers.lambdaQuery(PiSistarRtBatches.class);
                                batchWrapper.eq(PiSistarRtBatches::getAreaNo, areaNo);
                                batchWrapper.eq(PiSistarRtBatches::getYearNo, yearNo);
                                batchWrapper.eq(PiSistarRtBatches::getOrderNo, orderNo);
                                batchWrapper.eq(PiSistarRtBatches::getBatchNo, batchNo);
                                batchWrapper.eq(PiSistarRtBatches::getReccatNo, reccatNo);
//                                batchWrapper.eq(PiSistarRtBatches::getEopNo, eopNo);
                                batchWrapper.orderByDesc(PiSistarRtBatches::getActualStartTime);
                                List<PiSistarRtBatches> batcheList = iPiSistarRtBatchesService.list(batchWrapper);
                                if (batcheList != null && batcheList.size() > 0) {
                                    PiSistarRtBatches piSistarRtBatches = batcheList.get(0);
                                    Date startTime = piSistarRtBatches.getActualStartTime();
                                    if (startTime != null) {
                                        fieldVal = DateUtils.formatDateTime(startTime);
                                    }else{
                                        fieldVal = "";
                                    }
                                }
                            } else if("deviceName".equals(val)){
                                //设备名称
                                fieldVal = piSistarRtSteps.getUnitName();
                            } else if("ordercatName".equals(val)){
                                //生产配方
                                fieldVal = piSistarRtSteps.getOrdercatName();
                            }
                            List<TableFieldValueDto> fieldList = new ArrayList<>();
                            TableFieldValueDto tableFieldValueDto = new TableFieldValueDto();
                            // 字段名
                            tableFieldValueDto.setField(piCode);
                            if(timeFormat != null && timeFormat != null){
                                fieldVal = DateConvertUtil.convertSeconds(fieldVal,timeFormat);
                            }
                            // 字段值
                            tableFieldValueDto.setFieldVal(fieldVal);
                            fieldList.add(tableFieldValueDto);
                            tableSaveDto.setFieldList(fieldList);
                        } else {
                            // 获取参数

                            List<PiSistarRtStepParams> piSistarRtStepParamsList = new ArrayList<>();
                            //匹配上的参数对象
                            PiSistarRtStepParams piSistarRtStepParam = null;

                            //约束条件
                            Map<String, List<PiAdapterAlgorithmCondition>> identifierMap = new HashMap<>();
                            if (conditionList != null && !conditionList.isEmpty()) {
                                //同一标识符分为一类
                                identifierMap = conditionList.stream()
                                        .filter(Objects::nonNull) // 过滤null值
                                        .filter(condition -> condition.getIdentifier() != null) // 过滤identifier为null的项
                                        .collect(Collectors.groupingBy(
                                                PiAdapterAlgorithmCondition::getIdentifier,
                                                Collectors.toList()
                                        ));
                            }

                            if(stepParamsList == null || (stepParamsList!= null && stepParamsList.isEmpty())){
                                log.error("adapter-步骤参数为空");
                                continue;
                            }
                            //遍历步骤参数
                            for (PiSistarRtStepParams piSistarRtStepParams : stepParamsList) {
                                //设定值
                                String setValue = piSistarRtStepParams.getSetpointValue();
                                //实际值
                                String actualValue = piSistarRtStepParams.getActualValue();
                                //参数名称
                                String paramName = piSistarRtStepParams.getParamName();
                                boolean isParam = true;
                                //判断配置取值(参数名称)和步骤参数的参数名称是否一致
                                if (!paramName.equals(val)) {
                                    //遍历参数表判断是否有相同
                                    for (PiSistarRtStepParams stepParam : stepParamsList) {
                                        if (stepParam.getParamName().equals(val)) {
                                            piSistarRtStepParam = stepParam;
                                            break;
                                        }
                                    }
                                    if (piSistarRtStepParam == null) {
                                        //参数名称不一致
                                        isParam = false;
                                        //结束循环
                                        break;
                                    }
                                }
                                // 且 为一组的约束条件
                                for (String identifier : identifierMap.keySet()) {

                                    List<PiAdapterAlgorithmCondition> conditions = identifierMap.get(identifier);
                                    for (PiAdapterAlgorithmCondition condition : conditions) {
                                        //条件
                                        String calculateType = condition.getCalculateType();
                                        //比较值
                                        String calculateValue = condition.getCalculateValue();
                                        //判断参数名称是否相同
                                        if (!piSistarRtStepParams.getParamName().equals(condition.getParamName())) {
                                            isParam = false;
                                            break;
                                        } else {
                                            isParam = true;
                                        }

                                        //值类型
                                        String vType = condition.getValType();
                                        //参数值
                                        String fieldVal = "";
                                        if ("0".equals(vType)) {
                                            //设定值
                                            fieldVal = setValue;
                                        } else {
                                            //实际值
                                            fieldVal = actualValue;
                                        }
                                        //过滤为空
                                        if (StrUtil.isBlank(fieldVal) || StrUtil.isBlank(calculateValue)) {
                                            isParam = false;
                                            break;
                                        }
                                        //比较大小
                                        if ("1".equals(calculateType) && !(Double.parseDouble(fieldVal) > Double.parseDouble(calculateValue))) {
                                            //>
                                            isParam = false;
                                            break;
                                        } else if ("2".equals(calculateType) && !(Double.parseDouble(fieldVal) >= Double.parseDouble(calculateValue))) {
                                            //≥
                                            isParam = false;
                                            break;
                                        } else if ("3".equals(calculateType)) {
                                            //=
                                            //判断是否数字
                                            if (calculateValue.trim().matches("-?\\d+(\\.\\d+)?") && !(Double.parseDouble(fieldVal) == Double.parseDouble(calculateValue))) {
                                                isParam = false;
                                                break;
                                            } else if (!fieldVal.equals(calculateValue)) {
                                                isParam = false;
                                                break;
                                            }
                                        } else if ("4".equals(calculateType)) {
                                            //≠
                                            if (calculateValue.trim().matches("-?\\d+(\\.\\d+)?") && !(Double.parseDouble(fieldVal) != Double.parseDouble(calculateValue))) {
                                                isParam = false;
                                                break;
                                            } else if (fieldVal.equals(calculateValue)) {
                                                isParam = false;
                                                break;
                                            }
                                        } else if ("5".equals(calculateType) && Double.parseDouble(fieldVal) < Double.parseDouble(calculateValue)) {
                                            //<
                                            isParam = false;
                                            break;
                                        } else if ("6".equals(calculateType) && Double.parseDouble(fieldVal) <= Double.parseDouble(calculateValue)) {
                                            //≤
                                            isParam = false;
                                            break;
                                        }
                                    }
                                    
                                    if (!isParam) {
                                        break;
                                    }
                                }
                                //判断参数是否匹配
                                if (isParam) {
                                    piSistarRtStepParamsList.add(piSistarRtStepParams);
                                }
                            }

                            //匹配到参数
                            if (piSistarRtStepParamsList != null && !piSistarRtStepParamsList.isEmpty()) {
                                if(piSistarRtStepParam == null){
                                    piSistarRtStepParam = piSistarRtStepParamsList.get(0);
                                }
                                //设定值
                                String setValue = piSistarRtStepParam.getSetpointValue();
                                //实际值
                                String actualValue = piSistarRtStepParam.getActualValue();

                                //写入字段
                                List<TableFieldValueDto> fieldList = new ArrayList<>();
                                TableFieldValueDto tableFieldValueDto = new TableFieldValueDto();
                                tableFieldValueDto.setField(piCode);

                                //值
                                String fieldVal = "";
                                if ("0".equals(valType)) {
                                    //设定值
                                    fieldVal = setValue;
                                } else {
                                    //实际值
                                    fieldVal = actualValue;
                                }
                                if(fieldVal != null && timeFormat != null){
                                    fieldVal = DateConvertUtil.convertSeconds(fieldVal, timeFormat);
                                }
                                tableFieldValueDto.setFieldVal(fieldVal);
                                fieldList.add(tableFieldValueDto);
                                tableSaveDto.setFieldList(fieldList);
                            }

                        }

                        if(tableSaveDto != null && tableSaveDto.getFieldList() == null ){
                            log.error("adapter-写入的值为空 areaNo: [{}], pcuNo: [{}], unitNo: [{}], eopNo: [{}]",
                                    areaNo, piSistarRtSteps.getPcuNo(), piSistarRtSteps.getUnitNo(), eopNo);
                            continue;
                        }
                        // 数据写入
                        //#######################条件#######################
                        List<TableConditionDto> conditions = new ArrayList<>();
                        //字段维护-唯一约束
                        //获取配置的唯一约束
                        List<PiCustomizeTableField> onlyFieldList = tableFieldMap.get(tableName);
                        if (onlyFieldList != null && !onlyFieldList.isEmpty()) {
                            Map<String, String> valueMapping = Map.of(
                                    AdapterOnlyEnum.YEAR_NO.getValue(), yearNo,
                                    AdapterOnlyEnum.ORDER_NO.getValue(), orderNo,
                                    AdapterOnlyEnum.BATCH_NO.getValue(), batchNo,
                                    AdapterOnlyEnum.EOP_NO.getValue(), eopNo,
                                    AdapterOnlyEnum.LINE_NO.getValue(), lineNo,
                                    AdapterOnlyEnum.RECIPE_NAME.getValue(), recipeNameOnly,
                                    AdapterOnlyEnum.START_TIME.getValue(), startTimeOnly
                            );
                            //唯一约束
                            for (PiCustomizeTableField customizeTableField : onlyFieldList) {
                                String adapterOnly = customizeTableField.getAdapterOnly();
                                String value = valueMapping.get(adapterOnly);

                                TableConditionDto dto = new TableConditionDto();
                                dto.setKey(customizeTableField.getFieldName());
                                dto.setVal(value);
                                conditions.add(dto);
                            }
                            tableSaveDto.setConditionList(conditions);

                            try {
                                String id = iPiCustomizeTableService.tableSave(tableSaveDto);
                                //保存成功-更新PI间数据
                                List<String> dataIds = Arrays.asList(id);
                                List<String> piCodes = Arrays.asList(piCode);
                                iPiAllocationTableFieldService.savePICalculateSignal(dataIds, tableSaveDto.getTable(),piCodes);

                                //更新PI过程记录
                                updatePiProcessRecord(tableName, id, piCode, piAdapterAlgorithm.getId());
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }

                }
            }
        }

    }

    /**
     * 更新PI过程记录
     * @param tableName 表名
     * @param id 数据id
     * @param piCode PI
     * @param algorithmId 算法id
     */
    public void updatePiProcessRecord(String tableName, String id,String piCode, String algorithmId){
        try {
            //获取PI数据
            List<TableConditionDto> conditionList = new ArrayList<>();
            conditionList.add(new TableConditionDto(PiCommonConstants.ID, id));

            //查询对象 查询字段(trigger_json), 表, 条件(id)
            TableQueryDto tableQueryDto = new TableQueryDto(Arrays.asList(PiCommonConstants.TRIGGER_JSON), tableName, conditionList);
            List<Map<String,Object>> tableData = iPiCustomizeTableService.getTableData(tableQueryDto);
            //获取过程记录json
            String triggerJson = "";
            //判断过程记录中的PI是否存在当前PI(存在更新, 不存在添加))
            if(null != tableData && tableData.size() != 0 && null != tableData.get(0)){
                triggerJson = null != tableData.get(0).get(PiCommonConstants.TRIGGER_JSON) ?
                        tableData.get(0).get(PiCommonConstants.TRIGGER_JSON).toString() : "";
            }

            List<ProcessRecordDto> listMap = new ArrayList<>();
            boolean ifHave = false;
            if(StrUtil.isNotBlank(triggerJson)){
                listMap = JSONUtil.toList(triggerJson, ProcessRecordDto.class);
                for(ProcessRecordDto processRecordDto : listMap){
                    if(processRecordDto.getField().equals(piCode)){
                        processRecordDto.setTime(new Date());
                        processRecordDto.setCalculateSignalId(algorithmId);
                        processRecordDto.setType(PiCommonConstants.ONE_STR);
                        ifHave = true;
                        break;
                    }
                }
            }
            //如果不存在，则新增到集合中
            if(!ifHave){
                ProcessRecordDto processRecordDto = new ProcessRecordDto(piCode, new Date(), algorithmId, PiCommonConstants.ONE_STR);
                listMap.add(processRecordDto);
            }

            //更新PI
            ObjectMapper mapper = new ObjectMapper();
            TableFieldValueDto tableFieldValueDto = new TableFieldValueDto(PiCommonConstants.TRIGGER_JSON, mapper.writeValueAsString(listMap));
            List<TableFieldValueDto> fieldList = new ArrayList<>();
            fieldList.add(tableFieldValueDto);
            TableSaveDto tableSaveDto = new TableSaveDto(fieldList, tableName, conditionList);
            iPiCustomizeTableService.tableSave(tableSaveDto);
        } catch (Exception e) {
            log.info("更新PI过程记录异常", e);
        }
    }


    /**
     * 重算（PI手动重算）
     * @param tableName 计算表
     * @param listPI 计算的PI
     * @param startTime 计算开始时间
     * @param endTime 计算结束时间
     */
    @Override
    public void againPiCalculate(String tableName, List<String> listPI, LocalDateTime startTime, LocalDateTime endTime) {
        try {
            executorService.submit(() -> {
                // 异步执行的任务
                System.out.println("Adapter异步任务执行中...");

                //获取算法
                List<PiAdapterAlgorithm> listAlgorithm = queryListByPiCodes(tableName,listPI, PiCommonConstants.ONE);
                //获取设备编码集合 去重
                List<Integer> unitNoList = listAlgorithm.stream().map(PiAdapterAlgorithm::getUnitNo).distinct().collect(Collectors.toList());
                //获取步骤号集合 去重
                List<Integer> eopNoList = listAlgorithm.stream().map(PiAdapterAlgorithm::getEopNo).distinct().collect(Collectors.toList());

                //获取步骤
                LambdaQueryWrapper<PiSistarRtSteps> wrapper = Wrappers.lambdaQuery(PiSistarRtSteps.class);
                // 查询 条件时间段
                wrapper.ge(PiSistarRtSteps::getEventTime, startTime);
                wrapper.le(PiSistarRtSteps::getEventTime, endTime);
                wrapper.in(PiSistarRtSteps::getUnitNo, unitNoList);
                wrapper.in(PiSistarRtSteps::getEopNo, eopNoList);
                wrapper.orderByAsc(PiSistarRtSteps::getEventTime);
                List<PiSistarRtSteps> stepList = iPiSistarRtStepsService.list(wrapper);

                calculate(stepList,listAlgorithm);
                System.out.println("Adapter异步任务完成！");
            });
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    /**
     * Ra重算（PI手动重算）
     * @param tableName 计算表
     * @param listPI 计算的PI
     * @param startTime 计算开始时间
     * @param endTime 计算结束时间
     */
    @Override
    public void againPiCalculateRa(String tableName, List<String> listPI, LocalDateTime startTime, LocalDateTime endTime) {
        try {
            executorService.submit(() -> {
                // 异步执行的任务
                System.out.println("异步任务执行中...");
                //
                LambdaQueryWrapper<PiRaStep> wrapper = Wrappers.lambdaQuery(PiRaStep.class);
                // 查询 条件时间段
                wrapper.ge(PiRaStep::getEventTime, startTime);
                wrapper.le(PiRaStep::getEventTime, endTime);
                List<PiRaStep> stepList = iPiRaStepService.list(wrapper);

                //获取算法
                List<PiAdapterAlgorithm> listAlgorithm = queryListByPiCodes(tableName,listPI, PiCommonConstants.TWO);

                calculateRa(stepList,listAlgorithm);
                System.out.println("异步任务完成！");
            });
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    /**
     * 获取步骤参数
     * @return
     */
    public Map<String, List<PiSistarRtStepParams>> getStepParam(List<Date> startTimeList){
        LambdaQueryWrapper<PiSistarRtStepParams> piSistarRtStepParamsWrapper= Wrappers.lambdaQuery(PiSistarRtStepParams.class);
        //piSistarRtStepParamsWrapper.eq(PiSistarRtStepParams::getDelFlag,0);
        piSistarRtStepParamsWrapper.in(PiSistarRtStepParams::getStartTime,startTimeList);
        List<PiSistarRtStepParams> listStepParams = iPiSistarRtStepParamsService.list(piSistarRtStepParamsWrapper);
        //步骤参数分组 区域、年份、订单号、批次号、EOP、开始时间下横线分隔 value为对象集合
        Map<String, List<PiSistarRtStepParams>> stepParamsMap = listStepParams.stream()
                .collect(Collectors.groupingBy(
                        piSistarRtStepParams -> piSistarRtStepParams.getAreaNo() + "_" +
                                piSistarRtStepParams.getYearNo() + "_" +
                                piSistarRtStepParams.getOrderNo() + "_" +
                                piSistarRtStepParams.getBatchNo() + "_" +
                                piSistarRtStepParams.getEopNo() + "_" +
                                piSistarRtStepParams.getStartTime()
                ));
        return stepParamsMap;
    }

    /**
     * 定时任务重算
     * @param parameterObject
     */
    @Override
    public void taskRecalculate(String parameterObject) {
        logger.info("开始进入定时重算模块，parameterObject："+parameterObject);

        //根据定时任务ID查询对应的配置模块
        List<PiTaskParameter> taskParameterList = piTaskParameterService.list(Wrappers.lambdaQuery(PiTaskParameter.class)
                            .eq(PiTaskParameter::getTaskId,parameterObject));
        try {
            //获取当前时间
            LocalDateTime endTime = LocalDateTime.now();
            if(StrUtil.isBlank(parameterObject)){return;}

            //查询当前任务关联多少计算计划，多个计算计划，就需要执行多次
            logger.info("定时重算模块，taskParameterList："+taskParameterList);

            for(PiTaskParameter piTaskParameter : taskParameterList){
                // 创建一个表示秒的时间间隔,默认重算2天
                long seconds = 172800;

                //没有字段就直接返回
                if(StrUtil.isBlank(piTaskParameter.getFields())){continue;}
                //获取间隔(秒)
                if(StrUtil.isNotBlank(piTaskParameter.getDifference())){
                    seconds = Long.parseLong(piTaskParameter.getDifference());
                }
                //从当前日期和时间中减去对应的秒数
                LocalDateTime startTime = LocalDateTime.now().minusSeconds(seconds);
                //获取需要计算的PI
                List<String> listPI = Arrays.asList(piTaskParameter.getFields().split(","));
                //获取计算pi数据表
                String tableName = piTaskParameter.getTableName();

                listPI = iPiCustomizeTableFieldService.lambdaQuery().in(PiCustomizeTableField::getFieldName,listPI)
                        .eq(PiCustomizeTableField::getTableName,tableName)
                        .eq(PiCustomizeTableField::getCurrentAlgorithm,"Adapter")
                        .list()
                        .stream()
                        .map(PiCustomizeTableField::getFieldName)
                        .filter(Objects::nonNull)  // 过滤空值
                        .distinct()  // 去重
                        .collect(Collectors.toList());
                if (listPI == null || listPI.isEmpty()) {
                    continue;
                }
                //根据PI清单，查询Aadapter算法
                List<PiAdapterAlgorithm> listAlgorithm = queryListByPiCodes(tableName,listPI, PiCommonConstants.ONE);
                //未查询到算法配置则跳过
                if (listAlgorithm == null || listAlgorithm.isEmpty()) {
                    logger.warn("未查询到算法配置，tableName: {}, listPI: {}", tableName, listPI);
                    continue;
                }

                //获取设备编码集合 去重
                List<Integer> unitNoList = listAlgorithm.stream().map(PiAdapterAlgorithm::getUnitNo).distinct().collect(Collectors.toList());
                //获取步骤号集合 去重
                List<Integer> eopNoList = listAlgorithm.stream().map(PiAdapterAlgorithm::getEopNo).distinct().collect(Collectors.toList());

                //获取数据
                LambdaQueryWrapper<PiSistarRtSteps> wrapper = Wrappers.lambdaQuery(PiSistarRtSteps.class);
                // 查询 条件时间段
                wrapper.ge(PiSistarRtSteps::getEventTime, startTime);
                wrapper.le(PiSistarRtSteps::getEventTime, endTime);
                wrapper.in(PiSistarRtSteps::getUnitNo, unitNoList);
                wrapper.in(PiSistarRtSteps::getEopNo, eopNoList);
                wrapper.orderByAsc(PiSistarRtSteps::getEventTime);
                List<PiSistarRtSteps> stepList = iPiSistarRtStepsService.list(wrapper);

                executorService.submit(() -> {
                    // 异步执行的任务
                    try {
                        logger.info("Adapter异步任务执行中...");
                        //进入计算
                        //logger.info("定时重算模块，调用rePiCalculateSignal方法，参数tableName："+tableName+"+，参数listPI："+listPI+"，参数startTime："+startTime+"，参数endTime："+endTime);
                        calculate(stepList, listAlgorithm);
                    } catch (Exception e) {
                        e.printStackTrace();
                        logger.info("Adapter异步任务完成！");
                    }
                });
            }
        }catch (Exception e) {
            e.printStackTrace();
        }finally {
            logger.info("离开定时重算模块");
            logger.info("主线程继续执行...");
        }

    }

    /**
     * 根据PI清单，查询Aadapter算法
     * @param tableName
     * @param listPI
     * @return
     */
    private List<PiAdapterAlgorithm> queryListByPiCodes(String tableName, List<String> listPI, int  type){
        LambdaQueryWrapper<PiAdapterAlgorithm> queryWrapper = Wrappers.lambdaQuery(PiAdapterAlgorithm.class);
        queryWrapper.eq(PiAdapterAlgorithm::getType, type);
        queryWrapper.eq(PiAdapterAlgorithm::getDelFlag, CommonConstants.NOT_DELETED);
        queryWrapper.eq(PiAdapterAlgorithm::getPiTable,tableName);
        queryWrapper.in(PiAdapterAlgorithm::getPiCode,listPI);
        return list(queryWrapper);
    }

    /**
     * 扩展版本：支持更多参数和条件类型
     * @param piSistarRtSteps 步骤对象
     * @param conditionJson 条件JSON
     * @return 是否通过条件
     */
    private boolean stepConditionPass(PiSistarRtSteps piSistarRtSteps, String conditionJson){

        if (conditionJson == null || conditionJson.trim().isEmpty()) {
            return true;
        }
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            List<StepConditionDto> conditions = objectMapper.readValue(
                    conditionJson,
                    new TypeReference<List<StepConditionDto>>() {}
            );
            if (conditions == null || conditions.isEmpty()) {
                return true;
            }
            // 创建步骤参数映射
            Map<String, String> stepParams = extractStepParams(piSistarRtSteps);
            // 检查所有条件
            for (StepConditionDto condition : conditions) {
                String paramName = condition.getParamName();
                String paramValue = stepParams.get(paramName);
                // 如果步骤中没有这个参数，条件不成立
                if (paramValue == null) {
                    return false;
                }
                boolean conditionResult = checkConditionWithParam(paramValue, condition);
                if (!conditionResult) {
                    return false;
                }
            }
            return true;
        } catch (Exception e) {
            System.err.println("解析条件JSON失败: " + e.getMessage());
            return false;
        }
    }

    /**
     * 从步骤对象中提取所有参数
     */
    private Map<String, String> extractStepParams(PiSistarRtSteps piSistarRtSteps) {
        Map<String, String> params = new HashMap<>();
        // 添加配方名
        params.put("recipeName", piSistarRtSteps.getRecipeName());
        return params;
    }

    /**
     * 检查带参数的条件
     */
    private boolean checkConditionWithParam(String paramValue, StepConditionDto condition) {
        String calculateType = condition.getCalculateType();
        String calculateValue = condition.getCalculateValue();
        if (calculateValue == null || calculateValue.trim().isEmpty()) {
            return false;
        }
        String[] values = calculateValue.split(",");
        switch (calculateType) {
            case "1": // 等于（单值）
                return paramValue.equals(values[0].trim());
            case "2": // 不等于（单值）
                return !paramValue.equals(values[0].trim());
            case "3": // 等于（多值或关系）
                for (String value : values) {
                    if (paramValue.equals(value.trim())) {
                        return true;
                    }
                }
                return false;
            case "4": // 不等于（多值或关系的反义）
                for (String value : values) {
                    if (paramValue.equals(value.trim())) {
                        return false;
                    }
                }
                return true;
            case "5": // 包含
                for (String value : values) {
                    if (paramValue.contains(value.trim())) {
                        return true;
                    }
                }
                return false;
            case "6": // 不包含
                for (String value : values) {
                    if (paramValue.contains(value.trim())) {
                        return false;
                    }
                }
                return true;
            default:
                System.err.println("未知的条件类型: " + calculateType);
                return false;
        }
    }





    /**
     * 计算RA
     * @param list
     * @param listAlgorithm
     */
    @Override
    public void calculateRa(List<PiRaStep> list, List<PiAdapterAlgorithm> listAlgorithm) {
        if(list != null && list.isEmpty()){
            log.info("没有数据");
            return;
        }
        //获取算法配置
        if (listAlgorithm == null){
            LambdaQueryWrapper<PiAdapterAlgorithm> queryWrapper = Wrappers.lambdaQuery(PiAdapterAlgorithm.class);
            queryWrapper.eq(PiAdapterAlgorithm::getType, PiCommonConstants.TWO);
            queryWrapper.eq(PiAdapterAlgorithm::getDelFlag,CommonConstants.NOT_DELETED);
            listAlgorithm = list(queryWrapper);
        }

        //获取map key为 区域、产线、设备、步骤(EOP) value为对象
        Map<String, List<PiAdapterAlgorithm>> algorithmMap = listAlgorithm.stream()
                .collect(Collectors.groupingBy(
                        piAdapterAlgorithm -> piAdapterAlgorithm.getAreaNo() + "_" +
                                piAdapterAlgorithm.getLineId() + "_" +
                                piAdapterAlgorithm.getUnitNo() + "_" + piAdapterAlgorithm.getEopNo()
                ));

        //获取条件配置
        List<PiAdapterAlgorithmCondition> listCondition = iPiAdapterAlgorithmConditionService.list();
        //将条件分组 按算法id
        Map<String, List<PiAdapterAlgorithmCondition>> conditionMap = listCondition.stream()
                .collect(Collectors.groupingBy(PiAdapterAlgorithmCondition::getAlgorithmId));


        //获取RA数据抽取-步骤参数-对照  区域ID_设备ID_订单ID_批次号_步骤ID_步骤号
        List<String> correlationList = list.stream()
                .map(step ->buildCorrelationKey(step))
                .collect(Collectors.toList());

        //获取步骤参数
        Map<String, List<PiRaStepParam>> stepParamsMap = getRaStepParam(correlationList);


        //获取配置Adapter唯一约束字段
        LambdaQueryWrapper<PiCustomizeTableField> tableFieldqueryWrapper = Wrappers.lambdaQuery(PiCustomizeTableField.class);
        tableFieldqueryWrapper.eq(PiCustomizeTableField::getDelFlag,0);

        //表字段 adapterOnly 不为空
        tableFieldqueryWrapper.isNotNull(PiCustomizeTableField::getAdapterOnly);
        List<PiCustomizeTableField> listTableField = iPiCustomizeTableFieldService.list(tableFieldqueryWrapper);
        //获取map key为报表名称tableName value为对象集合
        Map<String, List<PiCustomizeTableField>> tableFieldMap = listTableField.stream()
                .collect(Collectors.groupingBy(
                        PiCustomizeTableField::getTableName
                ));

        for (PiRaStep piRaStep : list) {
            //区域
            String areaNo = piRaStep.getAreaId()+"";
            //年
            //String yearNo = piRaStep.getYearId()+"";
            //步骤(EOP)
            String eopNo = piRaStep.getBopId()+"";
            //订单
            String orderNo = piRaStep.getOrderId()+"";
            //批次
            String batchNo = piRaStep.getBatchId()+"";
            //产线
            String lineId = piRaStep.getLineId()+"";
            //步骤号
            //String reccatNo = piRaStep.getReccatNo()+"";
            //开始时间(步骤)
            Date stepStartTime = piRaStep.getEventTime();


            //判断是否有算法 (区域、产线、设备、步骤(EOP))
            String key = areaNo + "_" + lineId +"_" + piRaStep.getUnitId() + "_" + eopNo;
            if(algorithmMap.containsKey(key)) {
                //算法配置
                List<PiAdapterAlgorithm> piAdapterAlgorithmList = algorithmMap.get(key);
                for (PiAdapterAlgorithm piAdapterAlgorithm : piAdapterAlgorithmList) {
                    //表名
                    String tableName = piAdapterAlgorithm.getPiTable();
                    //字段名
                    String piCode = piAdapterAlgorithm.getPiCode();
                    String algorithmId = piAdapterAlgorithm.getId();
                    if (StrUtil.isNotBlank(algorithmId)) {
                        //约束条件
                        List<PiAdapterAlgorithmCondition> conditionList = conditionMap.get(algorithmId);
                        //取第几次(步骤)
                        Integer several = piAdapterAlgorithm.getSeveral();
                        //取值(时间/时长/参数)
                        String val = piAdapterAlgorithm.getVal();
                        //取值类型0设定值/1实际值
                        String valType = piAdapterAlgorithm.getValType();
                        //ropId
                        String ropId = piAdapterAlgorithm.getRopId();

                        // 取第几条 或者 判断ropId
                        if ((several != null && several > 0) || StrUtil.isNotBlank(ropId)) {
                            LambdaQueryWrapper<PiRaStep> stepWrapper = Wrappers.lambdaQuery(PiRaStep.class);
                            stepWrapper.eq(PiRaStep::getAreaId, areaNo);
                            //stepWrapper.eq(PiSistarRtSteps::getYearNo, yearNo);
                            stepWrapper.eq(PiRaStep::getOrderId, orderNo);
                            stepWrapper.eq(PiRaStep::getBatchId, batchNo);
                            stepWrapper.eq(PiRaStep::getBopId, eopNo);
                            stepWrapper.orderByAsc(PiRaStep::getEventTime);
                            List<PiRaStep> listStep = iPiRaStepService.list(stepWrapper);
                            //先判断第几步
                            if (listStep != null && several != null && listStep.size() > 0 && several <= listStep.size()) {
                                piRaStep = listStep.get(several - 1);
                                //判断ropId是否相等
                                if(piRaStep == null || (StrUtil.isNotBlank(ropId) && piRaStep != null &&
                                    !ropId.equals(piRaStep.getStepNr().toString()))){
                                    piRaStep = null;
                                }
                                //判断开始时间是否与之前的相同,相同不做更新
                                if (stepStartTime.getTime() != piRaStep.getEventTime().getTime()) {
                                    correlationList.add(buildCorrelationKey(piRaStep));
                                    stepParamsMap = getRaStepParam(correlationList);
                                }
                            }else{
                                piRaStep = null;
                            }
                            //判断步骤号ropId
                            if (StrUtil.isNotBlank(ropId)) {
                                //遍历listStep判断步骤号(ropId)一致的步骤 lamd方法
                                listStep = listStep.stream()
                                        .filter(step -> step.getStepNr() != null && step.getStepNr().toString().equals(ropId))
                                        .collect(Collectors.toList());

                                if (listStep != null && listStep.size() > 0) {
                                    piRaStep = listStep.get(0);
                                    if (stepStartTime.getTime() != piRaStep.getEventTime().getTime()) {
                                        correlationList.add(buildCorrelationKey(piRaStep));
                                        stepParamsMap = getRaStepParam(correlationList);
                                    }
                                }else{
                                    piRaStep = null;
                                }
                            }
                            //判断是否匹配上
                            if (piRaStep == null){
                                continue;
                            }
                        }

                        //获取步骤参数
//                        String paramKey = piRaStep.getAreaId() + "_" +
//                                piRaStep.getOrderId() + "_" + piRaStep.getBatchId() + "_" +
//                                piRaStep.getEopNo() + "_" + piRaStep.getStartTime();
                        String paramKey = buildCorrelationKey(piRaStep);
                        //步骤参数
                        List<PiRaStepParam> stepParamsList = stepParamsMap.get(paramKey);

                        // 写表对象
                        TableSaveDto tableSaveDto = new TableSaveDto();
                        //表名
                        tableSaveDto.setTable(tableName);
                        //开始/结束时间/时长
                        if (StrUtil.isNotBlank(val) && TIME_FIELDS.contains(val)) {
                            String fieldVal = "";
                            // 获取(开始/结束时间/时长)
                            if ("startTime".equals(val)) {
                                //开始时间
                                Date startTime = piRaStep.getEventTime();
                                fieldVal = DateUtils.formatDateTime(startTime);
                            } else if ("startDate".equals(val)) {
                                //开始时间(日期)
                                Date startTime = piRaStep.getEventTime();
                                fieldVal = DateUtils.formatDate(startTime);
                            } else if ("endTime".equals(val)) {
                                //结束时间
                                Date startTime = piRaStep.getEndTime();
                                fieldVal = DateUtils.formatDateTime(startTime);
                            } else if ("endDate".equals(val)) {
                                //结束时间(日期)
                                Date startTime = piRaStep.getEndTime();
                                fieldVal = DateUtils.formatDate(startTime);
                            } else if ("runtime".equals(val)) {
                                //时长
                                Long duration = piRaStep.getDuration();
                                fieldVal = duration + "";
                            }

                            List<TableFieldValueDto> fieldList = new ArrayList<>();
                            TableFieldValueDto tableFieldValueDto = new TableFieldValueDto();
                            // 字段名
                            tableFieldValueDto.setField(piCode);
                            // 字段值
                            tableFieldValueDto.setFieldVal(fieldVal);
                            fieldList.add(tableFieldValueDto);
                            tableSaveDto.setFieldList(fieldList);
                        } else {
                            // 获取参数

                            List<PiRaStepParam> piRaStepParamList = new ArrayList<>();
                            //匹配上的参数对象
                            PiRaStepParam piRaStepParamMatch = null;

                            //约束条件
                            Map<String, List<PiAdapterAlgorithmCondition>> identifierMap = new HashMap<>();
                            if (conditionList != null && !conditionList.isEmpty()) {
                                //同一标识符分为一类
                                identifierMap = conditionList.stream()
                                        .filter(Objects::nonNull) // 过滤null值
                                        .filter(condition -> condition.getIdentifier() != null) // 过滤identifier为null的项
                                        .collect(Collectors.groupingBy(
                                                PiAdapterAlgorithmCondition::getIdentifier,
                                                Collectors.toList()
                                        ));
                            }

                            if(stepParamsList == null || (stepParamsList!= null && stepParamsList.isEmpty())){
                                log.error("adapter-步骤参数为空");
                                continue;
                            }
                            //遍历步骤参数
                            for (PiRaStepParam piRaStepParam : stepParamsList) {
                                //设定值
                                String setValue = piRaStepParam.getRcpValue();
                                //实际值
                                String actualValue = piRaStepParam.getRptValue();
                                //参数名称
                                String paramName = piRaStepParam.getParaName();
                                boolean isParam = true;
                                //判断配置取值(参数名称)和步骤参数的参数名称是否一致
                                if (!paramName.equals(val)) {
                                    isParam = false;
                                    continue;
                                }
                                // 且 为一组的约束条件
                                for (String identifier : identifierMap.keySet()) {

                                    List<PiAdapterAlgorithmCondition> conditions = identifierMap.get(identifier);
                                    for (PiAdapterAlgorithmCondition condition : conditions) {
                                        //条件
                                        String calculateType = condition.getCalculateType();
                                        //比较值
                                        String calculateValue = condition.getCalculateValue();
                                        //判断参数名称是否相同
                                        if (!paramName.equals(condition.getParamName())) {
                                            isParam = false;
                                            break;
                                        } else {
                                            isParam = true;
                                        }

                                        //值类型
                                        String vType = condition.getValType();
                                        //参数值
                                        String fieldVal = "";
                                        if ("0".equals(vType)) {
                                            //设定值
                                            fieldVal = setValue;
                                        } else {
                                            //实际值
                                            fieldVal = actualValue;
                                        }
                                        //过滤为空
                                        if (StrUtil.isBlank(fieldVal) || StrUtil.isBlank(calculateValue)) {
                                            isParam = false;
                                            break;
                                        }
                                        //比较大小
                                        if ("1".equals(calculateType) && !(Double.parseDouble(fieldVal) > Double.parseDouble(calculateValue))) {
                                            //>
                                            isParam = false;
                                            break;
                                        } else if ("2".equals(calculateType) && !(Double.parseDouble(fieldVal) >= Double.parseDouble(calculateValue))) {
                                            //≥
                                            isParam = false;
                                            break;
                                        } else if ("3".equals(calculateType) && Double.parseDouble(fieldVal) == Double.parseDouble(calculateValue)) {
                                            //=
                                            //判断是否数字
                                            if (calculateValue.trim().matches("-?\\d+(\\.\\d+)?") && !(Double.parseDouble(fieldVal) == Double.parseDouble(calculateValue))) {
                                                isParam = false;
                                                break;
                                            } else if (!fieldVal.equals(calculateValue)) {
                                                isParam = false;
                                                break;
                                            }
                                        } else if ("4".equals(calculateType) && Double.parseDouble(fieldVal) != Double.parseDouble(calculateValue)) {
                                            //≠
                                            if (calculateValue.trim().matches("-?\\d+(\\.\\d+)?") && !(Double.parseDouble(fieldVal) != Double.parseDouble(calculateValue))) {
                                                isParam = false;
                                                break;
                                            } else if (fieldVal.equals(calculateValue)) {
                                                isParam = false;
                                                break;
                                            }
                                        } else if ("5".equals(calculateType) && Double.parseDouble(fieldVal) < Double.parseDouble(calculateValue)) {
                                            //<
                                            isParam = false;
                                            break;
                                        } else if ("6".equals(calculateType) && Double.parseDouble(fieldVal) <= Double.parseDouble(calculateValue)) {
                                            //≤
                                            isParam = false;
                                            break;
                                        }
                                    }

                                    if (!isParam) {
                                        break;
                                    }
                                }
                                //判断参数是否匹配
                                if (isParam) {
                                    piRaStepParamList.add(piRaStepParam);
                                }
                            }

                            //匹配到参数
                            if (piRaStepParamList != null && !piRaStepParamList.isEmpty()) {
                                piRaStepParamMatch = piRaStepParamList.get(0);
                                //设定值
                                String setValue = piRaStepParamMatch.getRcpValue();
                                //实际值
                                String actualValue = piRaStepParamMatch.getRptValue();

                                //写入字段
                                List<TableFieldValueDto> fieldList = new ArrayList<>();
                                TableFieldValueDto tableFieldValueDto = new TableFieldValueDto();
                                tableFieldValueDto.setField(piCode);

                                //值
                                String fieldVal = "";
                                if ("0".equals(valType)) {
                                    //设定值
                                    fieldVal = setValue;
                                } else {
                                    //实际值
                                    fieldVal = actualValue;
                                }
                                tableFieldValueDto.setFieldVal(fieldVal);
                                fieldList.add(tableFieldValueDto);
                                tableSaveDto.setFieldList(fieldList);
                            }

                        }

                        if(tableSaveDto != null && tableSaveDto.getFieldList() == null ){
                            log.error("adapter-写入的值为空 areaNo: [{}], line: [{}], unitNo: [{}], bopNo: [{}]",
                                    areaNo, piRaStep.getLineId(), piRaStep.getUnitId(), eopNo);
                            continue;
                        }
                        // 数据写入
                        //#######################条件#######################
                        List<TableConditionDto> conditions = new ArrayList<>();
                        //字段维护-唯一约束
                        //获取配置的唯一约束
                        List<PiCustomizeTableField> onlyFieldList = tableFieldMap.get(tableName);
                        if (onlyFieldList != null && !onlyFieldList.isEmpty()) {
                            Map<String, String> valueMapping = Map.of(
//                                    AdapterOnlyEnum.YEAR_NO.getValue(), yearNo,
                                    AdapterOnlyEnum.ORDER_NO.getValue(), orderNo,
                                    AdapterOnlyEnum.BATCH_NO.getValue(), batchNo,
                                    AdapterOnlyEnum.LINE_NO.getValue(), lineId
                            );
                            //唯一约束
                            for (PiCustomizeTableField customizeTableField : onlyFieldList) {
                                String adapterOnly = customizeTableField.getAdapterOnly();
                                String value = valueMapping.get(adapterOnly);

                                TableConditionDto dto = new TableConditionDto();
                                dto.setKey(customizeTableField.getFieldName());
                                dto.setVal(value);
                                conditions.add(dto);
                            }
                            tableSaveDto.setConditionList(conditions);

                            try {
                                String id = iPiCustomizeTableService.tableSave(tableSaveDto);
                                //保存成功-更新PI间数据
                                List<String> dataIds = Arrays.asList(id);
                                List<String> piCodes = Arrays.asList(piCode);
                                iPiAllocationTableFieldService.savePICalculateSignal(dataIds, tableSaveDto.getTable(),piCodes);

                                //更新PI过程记录
                                updatePiProcessRecord(tableName, id, piCode, piAdapterAlgorithm.getId());
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }

                }
            }
        }
    }



    /**
     * 获取RA步骤参数
     * @return
     */
    public Map<String, List<PiRaStepParam>> getRaStepParam(List<String> correlationList){
        LambdaQueryWrapper<PiRaStepParam> piSistarRtStepParamsWrapper = Wrappers.lambdaQuery(PiRaStepParam.class);
        piSistarRtStepParamsWrapper.eq(PiRaStepParam::getDelFlag,CommonConstants.NOT_DELETED);
        piSistarRtStepParamsWrapper.in(PiRaStepParam::getCorrelation,correlationList);
        List<PiRaStepParam> listStepParams = iPiRaStepParamService.list(piSistarRtStepParamsWrapper);
        //遍历 listStepParams 得到关联对照 map
        Map<String, List<PiRaStepParam>> stepParamMap = listStepParams.stream()
                .collect(Collectors.groupingBy(
                        stepParam -> stepParam.getCorrelation()
                ));
        return stepParamMap;
    }

    /**
     * 构建关联对照
     * 区域ID_设备ID_订单ID_批次号_步骤ID_步骤号
     * @param step
     * @return
     */
    public String buildCorrelationKey(PiRaStep step) {
        return String.format("%s_%s_%s_%s_%s_%s",
                step.getAreaId(),
                step.getUnitId(),
                step.getOrderId(),
                step.getBatchId(),
                step.getBopId(),
                step.getStepNr());
    }


    /**
     * Ra定时任务重算
     * @param parameterObject
     */
    @Override
    public void taskRecalculateRa(String parameterObject) {
        logger.info("开始进入定时重算模块，parameterObject："+parameterObject);

        //根据定时任务ID查询对应的配置模块
        List<PiTaskParameter> taskParameterList = piTaskParameterService.list(Wrappers.lambdaQuery(PiTaskParameter.class)
                .eq(PiTaskParameter::getTaskId,parameterObject));
        try {
            //获取当前时间
            LocalDateTime endTime = LocalDateTime.now();
            if(StrUtil.isBlank(parameterObject)){return;}

            //查询当前任务关联多少计算计划，多个计算计划，就需要执行多次
            logger.info("定时重算模块，taskParameterList："+taskParameterList);

            for(PiTaskParameter piTaskParameter : taskParameterList){
                // 创建一个表示秒的时间间隔,默认重算2天
                long seconds = 172800;

                //没有字段就直接返回
                if(StrUtil.isBlank(piTaskParameter.getFields())){continue;}
                //获取间隔(秒)
                if(StrUtil.isNotBlank(piTaskParameter.getDifference())){
                    seconds = Long.parseLong(piTaskParameter.getDifference());
                }
                //从当前日期和时间中减去对应的秒数
                LocalDateTime startTime = LocalDateTime.now().minusSeconds(seconds);
                //获取需要计算的PI
                List<String> listPI = Arrays.asList(piTaskParameter.getFields().split(","));
                //获取计算pi数据表
                String tableName = piTaskParameter.getTableName();
                //根据PI清单，查询Aadapter算法
                List<PiAdapterAlgorithm> listAlgorithm = queryListByPiCodes(tableName,listPI, PiCommonConstants.TWO);
                //获取数据
                LambdaQueryWrapper<PiRaStep> wrapper = Wrappers.lambdaQuery(PiRaStep.class);
                // 查询 条件时间段
                wrapper.ge(PiRaStep::getEventTime, startTime);
                wrapper.le(PiRaStep::getEventTime, endTime);
                List<PiRaStep> stepList = iPiRaStepService.list(wrapper);

                executorService.submit(() -> {
                    // 异步执行的任务
                    System.out.println("异步任务执行中...");
                    //进入计算
                    logger.info("定时重算模块，调用rePiCalculateSignal方法，参数tableName："+tableName+"+，参数listPI："+listPI+"，参数startTime："+startTime+"，参数endTime："+endTime);
                    calculateRa(stepList, listAlgorithm);
                    logger.info("异步任务完成！");
                });
            }
        }catch (Exception e) {
            e.printStackTrace();
        }finally {
            logger.info("离开定时重算模块");
            logger.info("主线程继续执行...");
        }

    }

    /**
     * 删除算法,同时删除条件
     * @param asList
     */
    @Override
    public void deleteDataBatch(List<String> asList) {
        //当前用户
        LoginUser user = (LoginUser) SecurityUtils.getSubject().getPrincipal();

        for (String id : asList) {
            //算法-删除
            LambdaUpdateWrapper<PiAdapterAlgorithm> algorithmWrapper = new UpdateWrapper().lambda();
            algorithmWrapper.eq(PiAdapterAlgorithm::getId, id);
            algorithmWrapper.eq(PiAdapterAlgorithm::getDelFlag,CommonConstants.NOT_DELETED);

            algorithmWrapper.set(PiAdapterAlgorithm::getDelFlag, CommonConstants.DELETED);
            algorithmWrapper.set(PiAdapterAlgorithm::getUpdateTime, new Date());
            algorithmWrapper.set(PiAdapterAlgorithm::getUpdateBy, user.getUsername());
            update(algorithmWrapper);

            //删除条件
            LambdaUpdateWrapper<PiAdapterAlgorithmCondition> conditionWrapper = new UpdateWrapper().lambda();
            conditionWrapper.eq(PiAdapterAlgorithmCondition::getAlgorithmId,id);
            iPiAdapterAlgorithmConditionService.remove(conditionWrapper);
        }




    }

    /**
     * 复制算法
     * @param piCustomizeTableField
     */
    @Override
    @Transactional
    public void copyAdapterRaDb(PiCustomizeTableField piCustomizeTableField) {
        //算法
        LambdaQueryWrapper<PiAdapterAlgorithm> queryWrapper = new LambdaQueryWrapper<PiAdapterAlgorithm>();
        queryWrapper.eq(PiAdapterAlgorithm::getPiTable,piCustomizeTableField.getTableName());
        queryWrapper.eq(PiAdapterAlgorithm::getPiCode,piCustomizeTableField.getFieldName());
        queryWrapper.eq(PiAdapterAlgorithm::getDelFlag,0);

        queryWrapper.eq(PiAdapterAlgorithm::getType, "Adapter".equals(piCustomizeTableField.getCurrentAlgorithm())? "1" : "2");

        List<PiAdapterAlgorithm> list = list(queryWrapper);

        if(null != list && list.size() > 0){

            for (PiAdapterAlgorithm piAdapterAlgorithm : list) {
                //历史计算信号id
                String oldCalculateId = piAdapterAlgorithm.getId();

                //复制算法-存储
                PiAdapterAlgorithm newAlgorithm = BeanUtil.copyProperties(piAdapterAlgorithm, PiAdapterAlgorithm.class);
                newAlgorithm.setId(null);
                newAlgorithm.setPiCode(piCustomizeTableField.getTargetField());
                newAlgorithm.setCreateTime(new Date());
                newAlgorithm.setUpdateTime(new Date());
                save(newAlgorithm);

                //新算法id
                String newCalculateId = newAlgorithm.getId();

                //复制约束条件
                copyAlgorithmCondition(oldCalculateId, newCalculateId);
            }
            //更新自定义报表字段目标表的当前使用类型
            LambdaUpdateWrapper<PiCustomizeTableField> updateWrapper = Wrappers.lambdaUpdate(PiCustomizeTableField.class);
            updateWrapper.eq(PiCustomizeTableField::getTableName, piCustomizeTableField.getTableName());
            updateWrapper.eq(PiCustomizeTableField::getFieldName, piCustomizeTableField.getTargetField());
            updateWrapper.eq(PiCustomizeTableField::getDelFlag, 0);
            updateWrapper.set(PiCustomizeTableField::getCurrentAlgorithm, piCustomizeTableField.getCurrentAlgorithm());
            updateWrapper.set(PiCustomizeTableField::getUpdateTime, new Date());
            iPiCustomizeTableFieldService.update(updateWrapper);

        }

    }


    /**
     * 复制约束条件
     * @param oldAlgorithmId 原算法id
     * @param newAlgorithmId 新算法id
     */
    private void copyAlgorithmCondition(String oldAlgorithmId, String newAlgorithmId) {
        LambdaQueryWrapper<PiAdapterAlgorithmCondition> conditionWrapper = Wrappers.lambdaQuery(PiAdapterAlgorithmCondition.class);
        conditionWrapper.eq(PiAdapterAlgorithmCondition::getAlgorithmId, oldAlgorithmId);
        conditionWrapper.orderByAsc(PiAdapterAlgorithmCondition::getSort).orderByAsc(PiAdapterAlgorithmCondition::getId);
        List<PiAdapterAlgorithmCondition> conditionList = iPiAdapterAlgorithmConditionService.list(conditionWrapper);

        if (conditionList != null && !conditionList.isEmpty()) {
            List<PiAdapterAlgorithmCondition> newConditionList = new ArrayList<>();
            for (PiAdapterAlgorithmCondition condition : conditionList) {
                PiAdapterAlgorithmCondition newCondition = BeanUtil.copyProperties(condition, PiAdapterAlgorithmCondition.class);
                newCondition.setId(null);
                newCondition.setAlgorithmId(newAlgorithmId);
                newCondition.setCreateTime(new Date());
                newCondition.setUpdateTime(new Date());
                newConditionList.add(newCondition);
            }
            iPiAdapterAlgorithmConditionService.saveBatch(newConditionList);
        }
    }
}


