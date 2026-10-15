package com.keytech.modules.pi.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.lang.Pair;
import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.expression.engine.jexl.JexlEngine;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.keytech.base.service.impl.BaseServiceImpl;
import com.keytech.modules.mdm3.entity.MdmDict;
import com.keytech.modules.mdm3.service.IMdmDictService;
import com.keytech.modules.pi.common.*;
import com.keytech.modules.pi.dto.*;
import com.keytech.modules.pi.entity.*;
import com.keytech.modules.pi.mapper.PiComputationModelMapper;
import com.keytech.modules.pi.service.*;
import jakarta.annotation.PreDestroy;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.jexl3.JexlException;
import org.apache.http.client.utils.CloneUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;


/**
 * <p>
 * pi-计算模型
 * </p>
 *
 * @author 敖磊
 * @since 2024-11-14 22:34:22
 */
@Slf4j
@Service
public class PiComputationModelServiceImpl extends BaseServiceImpl<PiComputationModelMapper, PiComputationModel> implements IPiComputationModelService {

    private static final Logger logger = LoggerFactory.getLogger(PiComputationModelServiceImpl.class);

    @Resource
    private InfluxDBClientHelper influxDBClientHelper;

    @Resource
    private BusinessHandler businessHandler;

    /**
     * 报表配置
     */
    @Resource
    private IPiCustomizeTableService piCustomizeTableService;

    /**
     * pi计算信号服务
     */
    @Resource
    private IPiCalculateSignalService piCalculateSignalService;

    /**
     * 定时任务配置模块
     */
    @Resource
    private IPiTaskParameterService piTaskParameterService;

    /**
     * 字典表
     */
    @Resource
    private IMdmDictService mdmDictService;

    /**
     * 数据红黄区数据存储表
     */
    @Resource
    private IPiStandardDataService piStandardDataService;

    /**
     * PI红黄区pi明细表服务
     */
    @Resource
    private IPiStandardMaintenanceService piStandardMaintenanceService;

    /**
     * PI红黄区pi主表服务
     */
    @Resource
    private IPiStandardManageService piStandardManageService;

    /**
     * PI报表数据审批服务
     */
    @Resource
    private IPiApproveService piApproveService;

    /**
     * 基础点位信息配置
     */
    @Resource
    private IPiBasePointService piBasePointService;

    /**
     * 计算模型
     */
    @Resource
    private IPiComputationModelService piComputationModelService;


    /**
     * 基础数据表字段配置服务
     */
    @Resource
    private IPiCustomizeTableFieldService piCustomizeTableFieldService;

    /**
     * 结果转换模型
     */
    @Resource
    private IPiResultTransformationModelService iPiResultTransformationModelService;



    /**
     * 定义一个ThreadLocal变量，用于判断计算逻辑中重算逻辑
     */
    private static final ThreadLocal<List<Map>> threadLocalValue = new ThreadLocal<>();



    //判断数据是否是数值，然后做向上取整、向下取整、保留小数位数
    private static final String regex = "^[+-]?\\d*(\\.\\d+)?$";

    /**
     * JdbcTemplate注入
     */
    private final JdbcTemplate jdbcTemplate;

    /**
     * 构造函数注入JdbcTemplate
     * @param jdbcTemplate JdbcTemplate实例
     */
    @Autowired
    public PiComputationModelServiceImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    // 自定义线程工厂，用于设置线程名称
    private static class PiCalcThreadFactory implements ThreadFactory {
        private final AtomicInteger threadNumber = new AtomicInteger(1);
        private final String namePrefix;

        PiCalcThreadFactory(String namePrefix) {
            this.namePrefix = namePrefix;
        }

        @Override
        public Thread newThread(Runnable r) {
            Thread t = new Thread(r, namePrefix + "-" + threadNumber.getAndIncrement());
            t.setDaemon(false);
            return t;
        }
    }

    // 定义一个有界队列的线程池，避免OOM
    private final ExecutorService executorService = new ThreadPoolExecutor(
            10,  // 核心线程数
            50,  // 最大线程数
            60L, TimeUnit.SECONDS,  // 空闲线程存活时间
            new LinkedBlockingQueue<>(2000),  // 有界队列
            new PiCalcThreadFactory("pi-calc"),  // 自定义线程工厂
            new ThreadPoolExecutor.CallerRunsPolicy()  // 拒绝策略：调用者执行
    );

    // 调度线程池
    private final ScheduledExecutorService scheduledExecutorService = new ScheduledThreadPoolExecutor(
            20,
            new PiCalcThreadFactory("pi-schedule"),
            new ThreadPoolExecutor.CallerRunsPolicy()
    );


    // 在类销毁时关闭线程池
    @PreDestroy
    public void shutdownExecutorService() {
        if (!executorService.isShutdown()) {
            executorService.shutdown();
            try {
                if (!executorService.awaitTermination(600, TimeUnit.SECONDS)) {
                    executorService.shutdownNow();
                }
            } catch (InterruptedException e) {
                executorService.shutdownNow();
            }
        }
        // 关闭 ScheduledExecutorService
        if (!scheduledExecutorService.isShutdown()) {
            scheduledExecutorService.shutdown();
            try {
                if (!scheduledExecutorService.awaitTermination(600, TimeUnit.SECONDS)) {
                    scheduledExecutorService.shutdownNow();
                }
            } catch (InterruptedException e) {
                scheduledExecutorService.shutdownNow();
            }
        }
    }

    /**
     * 根据计算信号id集合查询所有的取值信号集合
     * @param calculateIds
     * @return
     */
    @Override
    public List<PiComputationModel> queryListBycalculateIds(List<String> calculateIds){
        List<PiComputationModel> piComputationModelList = new ArrayList<>();
        if(null != calculateIds && calculateIds.size() > 0){
            LambdaQueryWrapper<PiComputationModel> queryWrapper = Wrappers.lambdaQuery(PiComputationModel.class);
            queryWrapper.in(PiComputationModel::getCalculateId,calculateIds);
            queryWrapper.orderByAsc(PiComputationModel::getCreateTime);
            piComputationModelList = list(queryWrapper);
        }
        return piComputationModelList;
    }

    /**
     * 触发条件实现方法（实时计算）
     * @param parameterObject 定时任务传入的参数,定时任务ID
     */
    @Override
    public void piCalculateSignal(String parameterObject) {
        //根据定时任务ID查询对应的配置模块
        List<PiTaskParameter> taskParameterList = new ArrayList<>();
        LambdaQueryWrapper<PiTaskParameter> queryWrapper = Wrappers.lambdaQuery(PiTaskParameter.class);
        queryWrapper.eq(PiTaskParameter::getTaskId,parameterObject);
        taskParameterList = piTaskParameterService.list(queryWrapper);

        try {
            //根据parameter参数，确定查询多久以前的数据，确定开始时间，传入参数单位秒
            //获取当前时间
            LocalDateTime endTime = LocalDateTime.now();

            if(StrUtil.isBlank(parameterObject)){
                return;
            }
            //查询当前任务关联多少计算计划，多个计算计划，就需要执行多次
            for(PiTaskParameter piTaskParameter : taskParameterList){
                // 创建一个表示秒的时间间隔,默认6分钟
                long seconds = 360;

                //没有字段就直接返回
                if(StrUtil.isBlank(piTaskParameter.getFields())){
                    continue;
                }
                if(StrUtil.isNotBlank(piTaskParameter.getDifference())){
                    seconds = Long.parseLong(piTaskParameter.getDifference());
                }
                //从当前日期和时间中减去对应的秒数
                LocalDateTime startTime = LocalDateTime.now().minusSeconds(seconds);
                //获取需要计算的PI
                List<String> listPI = Arrays.asList(piTaskParameter.getFields().split(","));
                //获取计算pi数据表
                String tableName = piTaskParameter.getTableName();

                //查询所有PI中使用了influxDB的配置
                listPI = piCustomizeTableFieldService.lambdaQuery().in(PiCustomizeTableField::getFieldName,listPI)
                        .eq(PiCustomizeTableField::getCurrentAlgorithm,"InfluxDB")
                        .list()
                        .stream()
                        .map(PiCustomizeTableField::getFieldName)
                        .filter(Objects::nonNull)  // 过滤空值
                        .distinct()  // 去重
                        .collect(Collectors.toList());

                //根据PI清单，查询对应PI所有的计算信号、计算模型、配置等
                List<PiCalculateSignal> piCalculateSignalList = piCalculateSignalService.queryListByPiCodes(tableName,listPI);

                //过滤掉PI日报间计算的PI
                List<PiCalculateSignal> piCalculateSignalListNew = new ArrayList<>();

                for(PiCalculateSignal piCalculateSignal : piCalculateSignalList){
                    try {
                        if(
                                !(
                                        "0".equals(piCalculateSignal.getStepType())
                                                && !piCalculateSignal.getPiComputationModelList().isEmpty()
                                                && piCalculateSignal.getPiComputationModelList().get(0).getValueSource() == 1
                                )
                        )
                        {
                                piCalculateSignalListNew.add(piCalculateSignal);
                        }
                    } catch (Exception e) {
                        logger.error("过滤计算信号异常, piCode={}", piCalculateSignal.getPiCode(), e);
                    }
                }
                executorService.submit(() -> {
                    try {
                        // 异步执行的任务
                        //进入计算
                        startCalculateSignal(piCalculateSignalListNew, startTime, endTime, 1);
                    } catch (Exception e) {
                        logger.error("实时计算任务执行异常", e);
                    } finally {
                        // 清理ThreadLocal，防止内存泄漏
                        threadLocalValue.remove();
                    }
                });
            }
        }catch (Exception e) {
            e.printStackTrace();
        }
//        logger.info("离开实时计算模块");
//        System.out.println("主线程执行完成");
    }


    /**
     * 配置PI定时重算（每次重算一周内的数据），每小时执行一次
     * @param parameterObject 定时任务传入的参数,定时任务ID
     */
    @Override
    public void automaticRePiCalculateSignal(String parameterObject) {
//        logger.info("开始进入定时重算模块，parameterObject："+parameterObject);

        //根据定时任务ID查询对应的配置模块
        List<PiTaskParameter> taskParameterList = new ArrayList<>();
        LambdaQueryWrapper<PiTaskParameter> queryWrapper = Wrappers.lambdaQuery(PiTaskParameter.class);
        queryWrapper.eq(PiTaskParameter::getTaskId,parameterObject);
        taskParameterList = piTaskParameterService.list(queryWrapper);
        try {
            //根据parameter参数，确定查询多久以前的数据，确定开始时间，传入参数单位秒
            //获取当前时间
            LocalDateTime endTime = LocalDateTime.now();

            if(StrUtil.isBlank(parameterObject)){
                return;
            }

            //查询当前任务关联多少计算计划，多个计算计划，就需要执行多次
//            logger.info("定时重算模块，taskParameterList："+taskParameterList);

            for(PiTaskParameter piTaskParameter : taskParameterList){
                // 创建一个表示秒的时间间隔,默认重算一周
                long seconds = 604800;

                //没有字段就直接返回
                if(StrUtil.isBlank(piTaskParameter.getFields())){
                    continue;
                }
                if(StrUtil.isNotBlank(piTaskParameter.getDifference())){
                    seconds = Long.parseLong(piTaskParameter.getDifference());
                }
                //从当前日期和时间中减去对应的秒数
                LocalDateTime startTime = LocalDateTime.now().minusSeconds(seconds);
                //获取需要计算的PI
                List<String> listPI = Arrays.asList(piTaskParameter.getFields().split(","));
                //获取计算pi数据表
                String tableName = piTaskParameter.getTableName();

                listPI = piCustomizeTableFieldService.lambdaQuery().in(PiCustomizeTableField::getFieldName,listPI)
                        .eq(PiCustomizeTableField::getCurrentAlgorithm,"InfluxDB")
                        .list()
                        .stream()
                        .map(PiCustomizeTableField::getFieldName)
                        .filter(Objects::nonNull)  // 过滤空值
                        .distinct()  // 去重
                        .collect(Collectors.toList());

                //根据PI清单，查询对应PI所有的计算信号
                List<PiCalculateSignal> piCalculateSignalList = piCalculateSignalService.queryListByPiCodes(tableName,listPI);

                //过滤掉PI日报间计算的PI
                List<PiCalculateSignal> piCalculateSignalListNew = new ArrayList<>();

                for(PiCalculateSignal piCalculateSignal : piCalculateSignalList){
                    try {
                        if(!("0".equals(piCalculateSignal.getStepType())
                            && piCalculateSignal.getPiComputationModelList() != null
                            && !piCalculateSignal.getPiComputationModelList().isEmpty()
                            && piCalculateSignal.getPiComputationModelList().get(0).getValueSource() == 1)){
                            piCalculateSignalListNew.add(piCalculateSignal);
                        }
                    } catch (Exception e) {
                        logger.error("过滤计算信号异常, piCode={}", piCalculateSignal.getPiCode(), e);
                    }
                }

                executorService.submit(() -> {
                    try {
                        // 异步执行的任务
                        //进入重算程序，重算是将开始时间和结束时间按照5分钟进行分割，然后查询前6分钟的
                        LocalDateTime endTimeTemp = startTime;
                        //防止接口异常暴露，无限执行，给出最大限制100天
                        for (int i = 0; i < 28800; i++) {
                            try {
                                //查询6分钟前
                                LocalDateTime startTimeTemp = endTimeTemp.minusSeconds(360);
                                Duration duration = Duration.between(endTimeTemp, endTime);
                                long secondsDuration = duration.getSeconds();
                                //给一个360秒的误差
                                if (secondsDuration >= -360) {
                                    startCalculateSignal(piCalculateSignalListNew, startTimeTemp, endTimeTemp, 3);
                                    endTimeTemp = endTimeTemp.plusSeconds(300);
                                } else {
                                    break;
                                }
                            } catch (Exception e) {
                                logger.error("定时重算任务执行异常, i={}", i, e);
                            }
                        }
                    } catch (Exception e) {
                        logger.error("定时重算任务执行异常", e);
                    } finally {
                        // 清理ThreadLocal，防止内存泄漏
                        threadLocalValue.remove();
                    }
                });
            }
        }catch (Exception e) {
            e.printStackTrace();
        }finally {
//            logger.info("离开定时重算模块");
//            System.out.println("主线程继续执行...");
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
    public void rePiCalculateSignal(String tableName,List<String> listPI, LocalDateTime startTime, LocalDateTime endTime){

        //查询算法来源于influxDB的PI
        listPI = piCustomizeTableFieldService.lambdaQuery().in(PiCustomizeTableField::getFieldName,listPI)
                .eq(PiCustomizeTableField::getCurrentAlgorithm,"InfluxDB")
                .list()
                .stream()
                .map(PiCustomizeTableField::getFieldName)
                .filter(Objects::nonNull)  // 过滤空值
                .distinct()  // 去重
                .collect(Collectors.toList());

        //根据PI清单，查询对应PI所有的计算信号
        List<PiCalculateSignal> piCalculateSignalList = piCalculateSignalService.queryListByPiCodes(tableName,listPI);
        //过滤掉PI日报间计算的PI
        List<PiCalculateSignal> piCalculateSignalListNew = new ArrayList<>();

        //存储PI间计算的触发信号
        List<PiCalculateSignal> piCalculateSignalListPi = new ArrayList<>();

        for(PiCalculateSignal piCalculateSignal : piCalculateSignalList){
            if(!("0".equals(piCalculateSignal.getStepType()) && piCalculateSignal.getPiComputationModelList().get(0).getValueSource() == 1)){
                piCalculateSignalListNew.add(piCalculateSignal);
            }else{
                piCalculateSignalListPi.add(piCalculateSignal);
            }
        }
        try {
            executorService.submit(() -> {
                try {
                    // 异步执行的任务
                    if (null != piCalculateSignalListNew && !piCalculateSignalListNew.isEmpty()) {
                        //进入重算程序，重算是将开始时间和结束时间按照6分钟进行分割，然后查询前15分钟的
                        LocalDateTime endTimeTemp = startTime;
                        //防止接口异常暴露，无限执行，给出最大限制100天
                        for (int i = 0; i < 28800; i++) {
                            try {
                                //查询6分钟前
                                LocalDateTime startTimeTemp = endTimeTemp.minusSeconds(360);
                                //计算当前结束时间跟实际结束时间之间相差多少秒
                                Duration duration = Duration.between(endTimeTemp, endTime);
                                long seconds = duration.getSeconds();
                                //给一个360秒的误差
                                if (seconds >= -360) {
                                    startCalculateSignal(piCalculateSignalListNew, startTimeTemp, endTimeTemp, 2);
                                    endTimeTemp = endTimeTemp.plusSeconds(300);
                                } else {
                                    break;
                                }
                            } catch (Exception e) {
                                logger.error("手动重算任务执行异常, i={}", i, e);
                            }
                        }
                    }

                    if (null != piCalculateSignalListPi && !piCalculateSignalListPi.isEmpty()) {
                        //开始进行PI日报间的PI计算
                        // 添加空值检查
                        List<String> piCodes = Optional.ofNullable(piCalculateSignalListPi)
                                .orElse(Collections.emptyList())
                                .stream()
                                .map(PiCalculateSignal::getPiCode)
                                .filter(Objects::nonNull)  // 过滤空值
                                .distinct()
                                .collect(Collectors.toList());

                        //时间查询字段
                        String fieldString = "";

                        //查询重算时间字段
                        List<PiCustomizeTable> listTable = piCustomizeTableService.lambdaQuery().eq(PiCustomizeTable::getTableName, tableName)
                                .isNotNull(PiCustomizeTable::getRecalculateTimePi).list();
                        if (null != listTable && !listTable.isEmpty()) {
                            fieldString = listTable.get(0).getRecalculateTimePi();
                        }
                        if (StringUtils.isBlank(fieldString)) {
                            return;
                        }
                        //查询dataID
                        // 设置查询条件
                        String startTimeString = startTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                        String endTimeString = endTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

                        String query = fieldString + " >= '" + startTimeString + "' and " + fieldString + " <= '" + endTimeString + "'";

                        //设置查询字段，默认查询全部
                        TableConditionQueryDto tableConditionQueryDto = new TableConditionQueryDto();
                        tableConditionQueryDto.setFieldList(Arrays.asList("id"));
                        tableConditionQueryDto.setTable(tableName);
                        tableConditionQueryDto.setCondition(query);
                        //当前表中所有的符合要求的数据
                        List<Map<String, Object>> tableData = piCustomizeTableService.getTableByCondition(tableConditionQueryDto);

                        if (null == tableData || tableData.isEmpty()) {
                            return;
                        }
                        for (Map<String, Object> map : tableData) {
                            String dataId = map.get("id").toString();
                            //循环PI
                            piCodes.forEach(piCode -> {
                                List<PiCalculateSignal> piSignals = piCalculateSignalListPi.stream().filter(e -> e.getPiCode().equals(piCode)).collect(Collectors.toList());
                                for (PiCalculateSignal piSignal : piSignals) {
                                    String retId = piModelCalculate(dataId, piSignal.getPiComputationModelList().get(0), piSignal, 2, LocalDateTime.now());
                                    if (StringUtils.isNotBlank(retId) && !retId.equals("-1")) {
                                        break;
                                    }
                                }
                            });
                        }
                    }
                } catch (Exception e) {
                    logger.error("手动重算任务执行异常", e);
                } finally {
                    // 清理ThreadLocal，防止内存泄漏
                    threadLocalValue.remove();
                }
            });
        } catch (Exception e) {
            logger.error("提交手动重算任务失败", e);
        }
    }

    /**
     * 开始计算（触发信号）
     * @param piCalculateSignalList 计算信号集合
     * @param startTime 计算开始时间
     * @param endTime 计算结束时间
     * @param reCalculateBj 重算标记 1定时计算，2手动重算，3定时重算
     */
    @Override
    public void startCalculateSignal(List<PiCalculateSignal> piCalculateSignalList,LocalDateTime startTime,LocalDateTime endTime,int reCalculateBj){
//        System.out.println("开始计算,piCalculateSignalList："+piCalculateSignalList + "，startTime：" + startTime + "，endTime：" + endTime + "，reCalculateBj：" + reCalculateBj);
        //将计算信号中的唯一约束json转换成listMap
        //json字符串格式：[{colName:a,colCode:aCode,tagCode:b,tagValue:c,monitorModelId:123},{colName:a,colCode:aCode,tagCode:b,tagValue:c,monitorModelId:123}]
        //唯一约束字符串解析:map格式：colName唯一约束字段名、tagCode字段对应的点位code、tagValue字段对应的点位value
        for(PiCalculateSignal entity : piCalculateSignalList){
            try{
                String extJson = entity.getUniqueJson();
                JSONArray jsonArray = new JSONArray(extJson);

                List<Map<String, Object>> listMap = new ArrayList<>();

                List<String> listUniqueTagCode = new ArrayList<>();
                for (int i = 0; i < jsonArray.size(); i++) {
                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                    Map<String, Object> map = new HashMap<>();
                    Set<String> keys = jsonObject.keySet();
                    for(String key : keys){
                        map.put(key, jsonObject.get(key));
                        //将唯一约束的tag集合进行整合
                        if(key.equals("tagCode") && null != jsonObject.get(key) && !"".equals(jsonObject.get(key))){
                            listUniqueTagCode.add(jsonObject.get(key).toString());
                        }
                    }
                    listMap.add(map);
                }
                //自动化取值
                entity.setListUniqueTagCode(listUniqueTagCode);
                //唯一约束
                entity.setListUniqueMap(listMap);
                //重算标记注入到对象中
                entity.setReCalculateBj(reCalculateBj);
            }catch (Exception e){
                e.printStackTrace();
            }
        }

        //最后所有符合条件的计算信号集合
        List<PiCalculateSignal> calculateSignalListFinal = new ArrayList<>();

        //触发时间，暂定当前时间
        LocalDateTime localDateTime = LocalDateTime.now();

        //20260202新增点位判断，判断点位是从自动化获取还是直接按照当前时间来进行判断

        //自动化点位的信号
        List<PiCalculateSignal> piCalculateSignalListByPoint = piCalculateSignalList.stream().filter(e-> Objects.nonNull(e.getPointType())  && e.getPointType().intValue() == 0).collect(Collectors.toList());

        //时间点位的信号
        List<PiCalculateSignal> piCalculateSignalListByPointTime = piCalculateSignalList.stream().filter(e-> Objects.nonNull(e.getPointType())  && e.getPointType().intValue() == 1).collect(Collectors.toList());

        //自动化点位的逻辑判断
        if(null != piCalculateSignalListByPoint && piCalculateSignalListByPoint.size() > 0){
            //一次性获取所有的取数信号的值
            List<String> calculateSignalTagCodeList = piCalculateSignalList.stream().map(PiCalculateSignal::getPointCode).distinct().collect(Collectors.toList());

            //将集合和时间点传入influxDB，返回对应的自动化值
            QueryCondition queryCondition = new QueryCondition();
            List<QueryTagCondition> queryTagConditionList = new ArrayList<>();
            for(String tagCode : calculateSignalTagCodeList){
                if(StrUtil.isNotBlank(tagCode)){
                    QueryTagCondition queryTagCondition = new QueryTagCondition();
                    queryTagCondition.setTagId(tagCode);
                    queryTagConditionList.add(queryTagCondition);
                }
            }
            queryCondition.setFrequency(1);
            queryCondition.setQueryType(QueryType.MEAN);
            queryCondition.setStart(startTime);
            queryCondition.setEnd(endTime);
            queryCondition.setQueryTagConditionList(queryTagConditionList);

            //获取所有的触发步骤点位集合
            List<MeasurementData> listPointData = new ArrayList<>();

            try{
                if(queryTagConditionList.size() > 0){
                    listPointData = influxDBClientHelper.query(queryCondition);
                }
            }catch (Exception e){
                e.printStackTrace();
            }


            //按照触发条件进行去重归类
            // 使用 Stream API 进行去重归类
            Map<CompositeKey, List<PiCalculateSignal>> uniqueMap = piCalculateSignalList.stream()
                    .collect(Collectors.groupingBy(
                            signal -> new CompositeKey(signal.getPointCode(), signal.getCalculateType(), signal.getCalculateValue())
                    ));

            //逐步执行触发条件
            for (Map.Entry<CompositeKey, List<PiCalculateSignal>> entry : uniqueMap.entrySet()) {
                //触发条件
                CompositeKey compositeKey = entry.getKey();
                //当前触发条件下符合条件的计算信号集合
                List<PiCalculateSignal> calculateSignalList = entry.getValue();

                String pointCode = compositeKey.pointCode;
                String type = compositeKey.calculateType;
                String calculateValue = compositeKey.calculateValue;
                // 指定时区（例如：系统默认时区）
                ZoneId zoneId = ZoneId.systemDefault();
                // 使用 pointName 进行后续操作
                try{
                    /*****开始进入触发逻辑******/
                    //如果没有配置触发条件，自动触发
                    if(StrUtil.isNotBlank(pointCode)){
                        //计算方式 进入7、结束8
                        //初始化一个自动化不可能存在的值，防止第一次异常匹配上
                        MeasurementData measurementDataPre = null;
                        for(MeasurementData measurementData : listPointData){
                            if(measurementData.getCnlNum().equals(pointCode)){
                                //进入触发,赋值第一个值
                                if(measurementDataPre == null){
                                    measurementDataPre = measurementData;
                                }
                                boolean isComming = false;
                                Double value = Double.parseDouble(calculateValue);
                                //进入
                                if(type.equals("7")){
                                    if(value.equals(measurementData.getValue()) && !measurementDataPre.getValue().equals(value) ){
                                        isComming = true;
                                        localDateTime = measurementData.getTime().atZone(zoneId).toLocalDateTime();
                                    }
                                }
                                //结束
                                if(type.equals("8")){
                                    if(!value.equals(measurementData.getValue()) && measurementDataPre.getValue().equals(value)){
                                        isComming = true;
                                        //结束的时候时间取上一个时间点
                                        localDateTime = measurementDataPre.getTime().atZone(zoneId).toLocalDateTime();
                                    }
                                }
                                /*****已经触发，进入计算逻辑******/
                                if(isComming){
                                    //将触发了信号的计算信号集合赋值给全局集合
                                    for(PiCalculateSignal beforeEntity : calculateSignalList){
                                        PiCalculateSignal beforeEntityCopy = BeanUtil.copyProperties(beforeEntity, PiCalculateSignal.class);
                                        beforeEntityCopy.setCalculateTime(localDateTime);
                                        calculateSignalListFinal.add(beforeEntityCopy);
                                    }
                                }
                                //将本次的值给全局，为下次循环比较做准备
                                measurementDataPre = measurementData;
                            }
                        }
                    }else{
                        localDateTime = endTime;
                        //将触发了信号的计算信号集合赋值给全局集合
                        for(PiCalculateSignal beforeEntity : calculateSignalList){
                            PiCalculateSignal beforeEntityCopy = BeanUtil.copyProperties(beforeEntity, PiCalculateSignal.class);
                            beforeEntityCopy.setCalculateTime(localDateTime);
                            calculateSignalListFinal.add(beforeEntityCopy);
                        }
                    }
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        }

        //时间点位的逻辑判断
        if(null != piCalculateSignalListByPointTime && piCalculateSignalListByPointTime.size() > 0){
            //如果计算信号根据时间来判断，则取time_type字段来判断
            //1、当前小时 2、当前偶数小时 3、指定时间，时分
            for(PiCalculateSignal signal : piCalculateSignalListByPointTime){
                int timeType = signal.getTimeType();
                if(Objects.isNull(timeType)){
                    continue;
                }
                //当前小时触发，只能在小时整数触发，考虑到时间误差，允许误差在1分钟内
                if(timeType == 0){
                    //判断时间范围内是否存在整数小时
                    List<LocalDateTime>  validHours = getValidHoursInRange(startTime,endTime,false);
                    if(null == validHours || validHours.size() == 0){
                        continue;
                    }
                    localDateTime = validHours.get(0);
                }
                //当前偶数小时触发，只能在偶数小时整数触发，考虑到时间误差，允许误差在1分钟内
                if(timeType == 1){
                    List<LocalDateTime>  validHours = getValidHoursInRange(startTime,endTime,true);
                    if(null == validHours || validHours.size() == 0){
                        continue;
                    }
                    localDateTime = validHours.get(0);
                }
                //自定义时间触发，只能在时间内进行触发，考虑到时间误差，允许误差在当前分钟内
                if(timeType == 2){
                    //目标执行时间
                    String targetTime = signal.getTimeComment();
                    if(StrUtil.isBlank(targetTime)){
                        continue;
                    }
                    List<LocalDateTime> validFullTimePoints = getValidFullTimePoints(targetTime,startTime,endTime);
                    if(null == validFullTimePoints || validFullTimePoints.size() == 0){
                        continue;
                    }
                    localDateTime = validFullTimePoints.get(0);
                }
                signal.setCalculateTime(localDateTime);
                calculateSignalListFinal.add(signal);
            }
        }

        //将calculateSignalListFinal按照触发时间从远到近排序
        // 排序方法
        Collections.sort(calculateSignalListFinal, Comparator.comparing(PiCalculateSignal::getCalculateTime));

        //将calculateSignalListFinal按照计算类型进行排序
        //定义一个新的集合，按照顺序进行排序
        List<PiCalculateSignal> calculateSignalListSort = new ArrayList<>();

        //瞬时取值，且是基础点位间的计算
        calculateSignalListSort.addAll(
                calculateSignalListFinal.stream().filter(
                        e-> e.getStepType().equals("0")
                                && null != e.getPiComputationModelList()
                                && null != e.getPiComputationModelList().get(0)
                                && e.getPiComputationModelList().get(0).getValueSource() == 0
                ).collect(Collectors.toList())
        );

        //监控型算法
        calculateSignalListSort.addAll(
                calculateSignalListFinal.stream().filter(e->e.getStepType().equals("2")).collect(Collectors.toList())
        );

        //范围取值
        calculateSignalListSort.addAll(
                calculateSignalListFinal.stream().filter(e->e.getStepType().equals("1")).collect(Collectors.toList())
        );

        //瞬时取值，且是PI日报间的计算
        calculateSignalListSort.addAll(
                calculateSignalListFinal.stream().filter(
                        e-> e.getStepType().equals("0")
                                && null != e.getPiComputationModelList()
                                && null != e.getPiComputationModelList().get(0)
                                && e.getPiComputationModelList().get(0).getValueSource() == 1
                )
                        .sorted(Comparator.comparingDouble(pi ->
                                pi.getCustomizeTableFieldList() != null
                                        && !pi.getCustomizeTableFieldList().isEmpty()
                                        && pi.getCustomizeTableFieldList().get(0).getSort() != null
                                        ? pi.getCustomizeTableFieldList().get(0).getSort()
                                        : Double.MAX_VALUE))
                        .collect(Collectors.toList())
        );

        //循环所有符合条件的计算信号，进入计算逻辑
        for(PiCalculateSignal beforeEntity : calculateSignalListSort){
            try {
                if(null != beforeEntity.getPiComputationModelList() && beforeEntity.getPiComputationModelList().size() == 1){
                    PiComputationModel computationModel = beforeEntity.getPiComputationModelList().get(0);
                    //如果是延迟的话，直接延迟执行
                    if("1".equals(computationModel.getAdvanceDelay()) && StrUtil.isNotBlank(computationModel.getAdvanceTime()) && !"0".equals(computationModel.getAdvanceTime())){
                        //如果是延迟计算，判断当前是否是重算，如果是重算的话，延迟时间早于当前时间，则不进行延迟，如果晚于当前时间，则按照当前时间和触发时间进行计算，计算出最新的延迟时间
                        long delayTime = Long.parseLong(computationModel.getAdvanceTime());
                        // 获取当前时间
                        LocalDateTime currentTime = LocalDateTime.now();
                        // 计算时间差
                        Duration duration = Duration.between(beforeEntity.getCalculateTime(), currentTime);

                        // 以秒为单位输出时间差
                        long secondsDifference = duration.getSeconds();
                        //如果触发时间延迟后，仍然在当前时间前，则直接触发
                        if(secondsDifference >= delayTime){
                            //进去PI的计算模型
                            calculate(beforeEntity);
                        }else{
                            delayTime = delayTime - secondsDifference;
                            final long finalDelayTime = delayTime;
                            Runnable task = () -> {
                                try {
                                    //进去PI的计算模型
                                    calculate(beforeEntity);
                                } catch (Exception e) {
                                    logger.error("延迟计算任务执行异常, delayTime={}s", finalDelayTime, e);
                                }
                            };
                            // 延迟执行任务
                            scheduledExecutorService.schedule(task, delayTime, TimeUnit.SECONDS);
                            logger.debug("延迟计算任务已调度, delayTime={}s", delayTime);
                        }
                    }else{
                        //进去PI的计算模型
                        calculate(beforeEntity);
                    }
                }
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }

    /**
     * 进入计算核心逻辑，触发后
     * @param calculateSignal 触发条件对象
     */
    @Override
    public void calculate(PiCalculateSignal calculateSignal)
    {
        //计算出对应的结果
        String retData = "";
        //20250325新增需求，唯一约束新增自定义分钟频率和指定时间节点计算，在不符合要求的时间点触发了计算，此处做一个拦截
        //根据触发时间来判定是否符合要求，符合要求的话，继续往下执行
        //查看唯一约束集合
        List<Map<String, Object>> listUniqueMap = calculateSignal.getListUniqueMap();
        //触发时间
        LocalDateTime timeUnique = calculateSignal.getCalculateTime();

        //对tagValue内容是自定义时间点timeFreFun和自定义分钟频率minFreFun进行判断
        List<String> listFun = Arrays.asList("timeFreFun","minFreFun");

        List<Map> freMap = listUniqueMap.stream().filter(
                map -> null != map.get("tagValue") && listFun.contains(map.get("tagValue").toString())
                ).collect(Collectors.toList());

        if(null != freMap && freMap.size() > 0){
            for(Map<String, Object> map : freMap){
                //频率值为空，则直接返回
                if(Objects.isNull(map.get("frequency"))){
                    return;
                }
                //自定义时间
                if(map.get("tagValue").equals("timeFreFun")){
                    // 定义输出的时间格式
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
                    String timeData = map.get("frequency").toString();
                    //将frequency格式化成时间格式，用于后续计算
                    LocalTime timeDataLocalTime = LocalTime.parse(timeData, formatter);
                    Duration duration = Duration.between(timeUnique.toLocalTime(), timeDataLocalTime);
                    long secondsDifference = duration.getSeconds();
                    //时间误差在1分钟内，都符合要求,否则直接返回
                    if(secondsDifference < -60 || secondsDifference > 60){
                        return;
                    }
                }
                //自定义分钟频率
                if(map.get("tagValue").equals("minFreFun")){
                    //2, 3, 4, 5, 6, 10, 12, 15, 20, 30
                    int timeData = Integer.parseInt(map.get("frequency").toString());
                    int minute = timeUnique.getMinute();
                    if(minute % timeData != 0){
                        return;
                    }
                }
            }
        }

        //逻辑做一个判断，如果唯一约束条件是当前小时或者当前小时偶数的时候，重算区间内，只在整数小时或者偶数小时进行触发计算
        if(calculateSignal.getReCalculateBj() == 2 || calculateSignal.getReCalculateBj() == 3){

            //为了做选择时间段不规范兼容,将触发时间往前推5分钟,如果中间存在相关整数,将触发时间定到整数时间进行计算
            LocalDateTime startTimeTemp = timeUnique.minusSeconds(360);
            // 创建一个日期时间格式化器，用于自定义输出格式
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH");
            // 格式化当前的日期和时间
            String formattedDateTime = timeUnique.format(formatter) + ":00:00";

            //转换回LocalDateTime格式
            formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            LocalDateTime localDateTime = LocalDateTime.parse(formattedDateTime, formatter);

            boolean isBetweenInclusive = !localDateTime.isBefore(startTimeTemp) && !localDateTime.isAfter(timeUnique);

            //查看唯一约束条件是否符合
            for(Map<String, Object> map : listUniqueMap){
                if(map.get("tagValue").equals("hourFun")){
                    if(isBetweenInclusive){
                        timeUnique = localDateTime;
                    }
                    if(timeUnique.getMinute() != 0 || timeUnique.getSecond() != 0){
                        return;
                    }
                    calculateSignal.setCalculateTime(localDateTime);
                }
                if(map.get("tagValue").equals("hour2Fun")){
                    if(isBetweenInclusive){
                        timeUnique = localDateTime;
                    }
                    //判断当前时间是否符合要求
                    if(timeUnique.getMinute() != 0 || timeUnique.getSecond() != 0 || timeUnique.getHour() % 2 != 0) {
                        return;
                    }
                    calculateSignal.setCalculateTime(localDateTime);
                }
            }
        }
        //计算唯一约束
        List<TableConditionDto> conditionList = getConditionList(calculateSignal);

        //如果唯一约束不为空，需要判断唯一约束对应的数据，对应的PI是否修改或者剔除，如果修改、剔除，不做计算
        if(null != conditionList && conditionList.size() > 0){
            TableQueryDto tableQueryDto = new TableQueryDto();
            tableQueryDto.setTable(calculateSignal.getPiTable());
            tableQueryDto.setConditionList(conditionList);
            List<String> fieldStrList = Arrays.asList("id");
            tableQueryDto.setFieldList(fieldStrList);
            List<Map<String,Object>> tableData = piCustomizeTableService.getTableData(tableQueryDto);
            if(null != tableData && tableData.size() == 1 && null != tableData.get(0)){
                //查询修改、剔除的数据
                LambdaQueryWrapper<PiApprove> queryWrapper = Wrappers.lambdaQuery(PiApprove.class);
                queryWrapper.eq(PiApprove::getTableName,calculateSignal.getPiTable());
                queryWrapper.eq(PiApprove::getState,"1");
                queryWrapper.like(PiApprove::getFields,calculateSignal.getPiCode());
                queryWrapper.eq(PiApprove::getDataId,tableData.get(0).get("id").toString());
                //已经剔除和修改的数据
                List<PiApprove> piApproveDataList = piApproveService.list(queryWrapper);
                if(null != piApproveDataList && piApproveDataList.size() > 0){
                    return;
                }
            }
        }

        try{
            //计算信号触发之后，判断相关取数信号是否通过
            //判断取值信号是否通过
            boolean isCommingByRetrievalSignal = retrievalSignalByMoment(calculateSignal);
            if(!isCommingByRetrievalSignal){
                return;
            }
            //判断取数步骤，状态  0瞬时取数 1范围取数 2监控取数
            //瞬时取值的时候，取数信号跟计算信号是且的关系，
            if("0".equals(calculateSignal.getStepType())){
                //基础点位间的计算
                if(calculateSignal.getPiComputationModelList().get(0).getValueSource() == 0){
                    retData = arithmeticByBasic(calculateSignal);
                }
            }
            //范围取数
            //范围取值的时候，取数信号属于取数步骤的筛选条件
            if("1".equals(calculateSignal.getStepType())){
                //范围取数，只支持基础数据间的计算
                if(calculateSignal.getPiComputationModelList().get(0).getValueSource() == 0){
                    retData = arithmeticByBasic(calculateSignal);
                }
            }
            //监控型取数
            if("2".equals(calculateSignal.getStepType())){
                //获取取数范围的时间段
                List<Pair<LocalDateTime,LocalDateTime>> listTime = getTimeListByRetrievalStep(calculateSignal);

                //取数范围为空，无需计算
                if(null == listTime || listTime.size() == 0){
                    return;
                }

                //定义取数频率 默认1s
                Integer frequency = 1;
                if(StrUtil.isNotBlank(calculateSignal.getPiRetrievalStepList().get(0).getFrequency())){
                    frequency = Integer.parseInt(calculateSignal.getPiRetrievalStepList().get(0).getFrequency());
                }
                retData = influxDbCommFunByMonitoring(calculateSignal,frequency,listTime);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        //保存数据
        String retId = "-1";
        if(StrUtil.isNotBlank(retData)){
            retId = saveData(retData,calculateSignal,conditionList);
        }
        //正常更新数据后，执行PI间的计算
        if(StringUtils.isNotBlank(retId) && !"-1".equals(retId)){
            arithmeticByPi(retId,calculateSignal.getPiTable(),calculateSignal.getPiCode(),calculateSignal.getCalculateTime(),calculateSignal.getReCalculateBj());
        }
    }

    /**
     * 将计算数据保存到对应的表中
     * @param retData 需要保存的值
     * @param calculateSignal 计算信号
     * @param conditionList 唯一约束条件
     * @return 保存数据主键ID
     */
    public String saveData(
                         String retData,
                         PiCalculateSignal calculateSignal,
                         List<TableConditionDto> conditionList
    ){
        String retId = "-1";
        try{
            //20250123屏蔽保留小数点几位、向上取整、向下取整判断，李进要求的
//            //查询字段的配置
//            LambdaQueryWrapper<PiCustomizeTableField> queryWrapper = Wrappers.lambdaQuery(PiCustomizeTableField.class);
//            queryWrapper.eq(PiCustomizeTableField::getTableName,calculateSignal.getPiTable());
//            queryWrapper.eq(PiCustomizeTableField::getFieldName,calculateSignal.getPiCode());
//            List<PiCustomizeTableField> listField = piCustomizeTableFieldService.list(queryWrapper);
//            if(null != listField && listField.size() == 1){
//                PiCustomizeTableField tableField = listField.get(0);
//                //String类型做保留数据计算
//                if("1".equals(tableField.getType()) || "5".equals(tableField.getType())){
//                    boolean isNumberOrDecimal = retData.matches(regex);
//                    if(isNumberOrDecimal){
//                        BigDecimal bd = new BigDecimal(Double.parseDouble(retData));
//                        if(null != tableField.getKeepDecimals()){
//                            int keepDecimals = 2;
//                            if(!Objects.isNull(tableField.getKeepDecimals())){
//                                keepDecimals = tableField.getKeepDecimals();
//                            }
//                            //向上取整
//                            if("1".equals(tableField.getTakingValue())){
//                                // 如果需要确保是真正的向上取整，使用自定义的向上取整逻辑
//                                BigDecimal trueRoundedUp = bd.setScale(keepDecimals, RoundingMode.CEILING);
//                                retData = trueRoundedUp.toString();
//                                //向下取整
//                            }else if("2".equals(tableField.getTakingValue())){
//                                BigDecimal truncated = bd.setScale(keepDecimals, RoundingMode.DOWN).setScale(tableField.getKeepDecimals(), RoundingMode.HALF_UP);
//                                retData = truncated.toString();
//                            }else{
//                                BigDecimal roundedUp = bd.setScale(keepDecimals, RoundingMode.HALF_UP);
//                                retData = roundedUp.toString();
//                            }
//                        }
//                    }
//                }
            PiComputationModel computationModel = calculateSignal.getPiComputationModelList().get(0);
            List<PiComputationMonitorModel> piComputationMonitorModelList = calculateSignal.getPiComputationMonitorModelList();

            retData = changeDataFormat(retData);
//                //判断结果是否是科学计数，如果是的，需要做double转换,原始数据默认保留4位
//                if(isScientificNotation(retData) || retData.matches(regex)){
//                    BigDecimal bd = new BigDecimal(Double.parseDouble(retData));
//                    //直接截取，不做4舍5入处理
//                    BigDecimal roundedUp = bd.setScale(4, RoundingMode.DOWN);
//                    // 检查是否是整数（没有小数部分）
//                    if (roundedUp.stripTrailingZeros().scale() <= 0) {
//                        // 如果是整数，则去掉小数部分
//                        retData = roundedUp.toBigInteger().toString();
//                    } else {
//                        // 否则保留原格式
//                        retData = roundedUp.toString();
//                    }
//                }

                //转换计算结果数据，在汉化模型中做汉化处理，只需要在瞬时取值、范围取数的方式进行数据转换
                //判断取值方式,瞬时取值、范围取值
            if("0".equals(calculateSignal.getStepType()) || "1".equals(calculateSignal.getStepType())){
                String dicCode = computationModel.getSinicizedDic();
                //判断是否需要汉化
                if(StrUtil.isNotBlank(dicCode)){
                    List<MdmDict> listDic = mdmDictService.lambdaQuery().eq(MdmDict::getGroupCode, dicCode).list();
                    String finalRetData = retData;
                    String finalRetDataTemp = finalRetData;
                    listDic = listDic.stream().filter(dic -> dic.getItemVal().equals(finalRetDataTemp)).collect(Collectors.toList());
                    if(null != listDic && listDic.size() == 1){
                        retData = listDic.get(0).getItemKey();
                    }
                }
            }

            //已经在数据库中存储的PI数据
            String piData = "";

            //根据唯一约束条件，查询是否存在数据
            TableQueryDto tableQueryDto = new TableQueryDto();
            tableQueryDto.setTable(calculateSignal.getPiTable());
            tableQueryDto.setConditionList(conditionList);
            //查询所有的字段
            List<String> fieldStrList = Arrays.asList("trigger_json",calculateSignal.getPiCode());
            tableQueryDto.setFieldList(fieldStrList);
            List<Map<String,Object>> tableData = piCustomizeTableService.getTableData(tableQueryDto);

            //记录每个字段的触发时间
            // 创建一个日期时间格式化器
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            // 使用格式化器格式化当前时间
            String triggerTime = calculateSignal.getCalculateTime().format(formatter);

            String triggerJson = "";
            //查询是否存在数据
            if(null != tableData && tableData.size() != 0 && null != tableData.get(0)){
                triggerJson = null != tableData.get(0).get("trigger_json") ? tableData.get(0).get("trigger_json").toString() : "";
                piData = null != tableData.get(0).get(calculateSignal.getPiCode()) ? tableData.get(0).get(calculateSignal.getPiCode()).toString() : "";
            }

            //如果是自动重算，则不写入有值的数据（需求取消20260605）
//            if(calculateSignal.getReCalculateBj() == 3){
//                if(StrUtil.isNotBlank(piData)){
//                    return retId;
//                }
//            }

            //用于判断更新时间集合中是否存在当前PI的更新时间
            boolean ifHave = false;
            List<Map> listMap = new ArrayList<>();

            if(StrUtil.isNotBlank(triggerJson)){
                listMap = JSONUtil.toList(triggerJson, Map.class);
                for(Map trigger : listMap){
                    if(trigger.get("field").equals(calculateSignal.getPiCode())){
                        trigger.put("time",triggerTime);
                        trigger.put("calculateSignalId",calculateSignal.getId());
                        ifHave = true;
                        break;
                    }
                }
            }
            //如果不存在，则新增到集合中
            if(!ifHave){
                Map triggerMap = new HashMap<>();
                //字段
                triggerMap.put("field",calculateSignal.getPiCode());
                //触发时间
                triggerMap.put("time",triggerTime);
                triggerMap.put("calculateSignalId",calculateSignal.getId());
                listMap.add(triggerMap);
            }
            triggerJson = JSONUtil.toJsonStr(listMap);

            //将数据写入到pi日报中
            TableSaveDto tableSaveDto = new TableSaveDto();

            //存储字段集合
            List<TableFieldValueDto> fieldList = new ArrayList<>();
            TableFieldValueDto tableFieldValueDto = new TableFieldValueDto();
            tableFieldValueDto.setField(calculateSignal.getPiCode());

            //将数据写入之前，需要判断数据是否要进行结果转换（20260206新增需求,在最后一步执行）
            retData = resultConvertModel(retData,computationModel.getResultTransformationModel(), calculateSignal.getCalculateTime());

            tableFieldValueDto.setFieldVal(retData);
            fieldList.add(tableFieldValueDto);

            //添加触发时间
            tableFieldValueDto = new TableFieldValueDto();
            tableFieldValueDto.setField("trigger_json");
            tableFieldValueDto.setFieldVal(triggerJson);
            fieldList.add(tableFieldValueDto);

            tableSaveDto.setFieldList(fieldList);
            tableSaveDto.setTable(calculateSignal.getPiTable());

            //是否覆盖,默认不覆盖
            boolean ifCover = false;
            if(conditionList.size() > 0){
                //判断取值方式,瞬时取值、范围取值
                if("0".equals(calculateSignal.getStepType()) || "1".equals(calculateSignal.getStepType())){
                    if("1".equals(computationModel.getIfCover())){
                        ifCover = true;
                    }
                }
                //监控取值
                if("2".equals(calculateSignal.getStepType())){
                    if(piComputationMonitorModelList.size() > 0 && "1".equals(piComputationMonitorModelList.get(0).getIfCover())){
                        ifCover = true;
                    }
                }
                tableSaveDto.setConditionList(conditionList);
                //如果是重算，无需判断是否覆盖，直接写入更新
                //-----20241219新问题，重算的时候需要判断是否覆盖，如果覆盖，直接写入，如果不覆盖，只写入第一次
                //是否重算
                if(ifCover){
                    //覆盖
                    retId = piCustomizeTableService.tableSave(tableSaveDto);
                }else{
                    //判断是否手动重算
                    if(calculateSignal.getReCalculateBj() == 2){
                        List<Map> tableSaveMapList = threadLocalValue.get();
                        if(null == tableSaveMapList){
                            tableSaveMapList = new ArrayList<>();
                        }

                        List<String> fields = tableSaveDto.getFieldList().stream().map(TableFieldValueDto::getField).collect(Collectors.toList());
                        Map map = new HashMap<>();
                        map.put("fields",fields);
                        map.put("table",tableSaveDto.getTable());
                        map.put("conditionList",tableSaveDto.getConditionList());

                        if(tableSaveMapList.size() == 0 || !tableSaveMapList.contains(map)){

                            retId = piCustomizeTableService.tableSave(tableSaveDto);

                            tableSaveMapList.add(map);
                            threadLocalValue.set(tableSaveMapList);
                        }
                    }else{
                        //不覆盖，判断是否在数据库中存在
                        if(StrUtil.isBlank(piData)){
                            //写入
                            tableSaveDto.setConditionList(conditionList);
                            retId = piCustomizeTableService.tableSave(tableSaveDto);
                        }
                    }
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return retId;
    }


    /**
     * 瞬时取数的取数信号判断
     * @param calculateSignal 触发条件对象
     * @return
     */
    @Override
    public boolean retrievalSignalByMoment(PiCalculateSignal calculateSignal) {
        //取值信号集合
        List<PiRetrievalSignal> retrievalSignalListBefor = calculateSignal.getPiRetrievalSignalList();

        /*****判断取数信号****/
        //取数标记
        boolean isCommingByRetrievalSignal = false;

        //当取数信号为空，直接返回true
        if(null == retrievalSignalListBefor || retrievalSignalListBefor.size() == 0){
            return true;
        }
        //过滤当前适用范围的取数信号,找到适用于触发条件的取数信号
        List<PiRetrievalSignal> retrievalSignalList = retrievalSignalListBefor.stream().filter(e->e.getApplyTo().equals("0")).collect(Collectors.toList());

        //查看取数信号
        if(null != retrievalSignalList && retrievalSignalList.size() > 0){

            //按照标识符进行归类，集合内是并关系，集合外是或关系
            Map<String, List<PiRetrievalSignal>> listMap = new HashMap<>();

            //将取数信号，按照标识符组装成map
            listMap = retrievalSignalList.stream()
                    .collect(Collectors.groupingBy(PiRetrievalSignal::getIdentifier));

            //将集合和时间点传入influxDB，返回对应的自动化值
            //判断提前或者延后
            for (PiRetrievalSignal retrievalSignal : retrievalSignalList){
                LocalDateTime time = calculateSignal.getCalculateTime();
                List<String> codes = new ArrayList<>();
                codes.add(retrievalSignal.getPointCode());
                if(StringUtils.isNotBlank(retrievalSignal.getValueType()) && retrievalSignal.getValueType().equals("1")){
                    codes.add(retrievalSignal.getCalculateValue());
                }
                //剔除重复
                codes = codes.stream().distinct().collect(Collectors.toList());
                //判断提前或者延后
                if(StringUtils.isNotBlank(retrievalSignal.getAdvanceDelay()) && StringUtils.isNotBlank(retrievalSignal.getAdvanceTime())){
                    //提前
                    if(retrievalSignal.getAdvanceDelay().equals("0")){
                        time = time.minusSeconds(Long.parseLong(retrievalSignal.getAdvanceTime()));
                    }
                    //延后
                    if(retrievalSignal.getAdvanceDelay().equals("1")){
                        time = time.plusSeconds(Long.parseLong(retrievalSignal.getAdvanceTime()));
                    }
                }
                List<MeasurementData> measurementDataListTemp = influxDbMoment(codes,time,"now");
                //判断当前条件是否成立
                retrievalSignal.setConditionIsTrue(isCommingByRetrievalSignal(retrievalSignal,measurementDataListTemp));
            }
            //循环所有的的取数信号
            Set<String> keys = listMap.keySet();

            for(String key : keys){
                boolean isAnd = true;
                List<PiRetrievalSignal> signals = listMap.get(key);
                //单个取数信号，并关系,任何一个条件不满足，就不通过
                for (PiRetrievalSignal signal : signals){
                    if(!signal.getConditionIsTrue()){
                        isAnd = false;
                        break;
                    }
                }
                if(isAnd){
                    isCommingByRetrievalSignal = true;
                    break;
                }
            }
        }else{
            isCommingByRetrievalSignal = true;
        }
        return isCommingByRetrievalSignal;
    }

    /**
     * 判断单个条件是否成立
     * @param signal
     * @param measurementDataList
     * @return
     */
    public boolean isCommingByRetrievalSignal(PiRetrievalSignal signal, List<MeasurementData> measurementDataList){
        if(StrUtil.isBlank(signal.getCalculateValue())){
            return false;
        }

        boolean signalComing = false;

        //判断是否是数据字典
        List<Double> dicCodeList = new ArrayList<>();
        if(StringUtils.isNotBlank(signal.getValueType()) && "2".equals(signal.getValueType())){
            List<MdmDict> listDic = mdmDictService.lambdaQuery().eq(MdmDict::getGroupCode, signal.getCalculateValue()).list();
            if(null != listDic && listDic.size() > 0){
                dicCodeList =
                        listDic.stream()
                                .map(MdmDict::getItemVal)
                                .filter(Objects::nonNull) // 过滤掉 null 值
                                .map(itemVal -> {
                                    try {
                                        return Double.parseDouble(itemVal);
                                    } catch (NumberFormatException e) {
                                        // 处理无法转换的情况，例如记录日志或返回默认值
                                        return 0.0; // 或者其他默认值
                                    }
                                })
                                .collect(Collectors.toList());
            }
        }
        for(MeasurementData measurementData : measurementDataList){
            if(measurementData.getCnlNum().equals(signal.getPointCode())){
                if(!Objects.isNull(measurementData.getValue())){
                    //判断是否符合条件
                    Double tagValue = measurementData.getValue();

                    //临时给一个不常用的变量
                    Double signalValue = -1000.0;
                    //常量
                    if(StringUtils.isBlank(signal.getValueType()) || "0".equals(signal.getValueType())){
                        signalValue = Double.parseDouble(signal.getCalculateValue());
                    }
                    //点位
                    if(StringUtils.isNotBlank(signal.getValueType()) && "1".equals(signal.getValueType())){
                        Optional<MeasurementData> pointOptional = measurementDataList.stream().filter(pointCodeVo->
                                pointCodeVo.getCnlNum().equals(signal.getCalculateValue()) &&
                                        pointCodeVo.getTime().equals(measurementData.getTime())
                        ).findAny();
                        if(pointOptional.isPresent()){
                            signalValue = pointOptional.get().getValue();
                        }
                    }

                    //计算类型，1> 2>= 3= 4!= 5< 6<= 7进入 8结束
                    String type = signal.getCalculateType();
                    if("1".equals(type)){
                        if(tagValue > signalValue){
                            signalComing = true;
                        }
                    }
                    if("2".equals(type)){
                        if(tagValue >= signalValue){
                            signalComing = true;
                        }
                    }
                    if("3".equals(type)){
                        if(tagValue.equals(signalValue)){
                            signalComing = true;
                        }
                    }
                    if("4".equals(type)){
                        if(!tagValue.equals(signalValue)){
                            signalComing = true;
                        }
                    }
                    if("5".equals(type)){
                        if(tagValue < signalValue){
                            signalComing = true;
                        }
                    }
                    if("6".equals(type)){
                        if(tagValue <= signalValue){
                            signalComing = true;
                        }
                    }
                    if("7".equals(type)){
                        //数据字典
                        if("2".equals(signal.getValueType())){
                            if(dicCodeList.contains(tagValue)){
                                signalComing = true;
                            }
                        }
                    }
                }
            }
        }
        return signalComing;
    }


    /**
     * 模型计算（基础点位间的计算）
     * @param calculateSignal 触发信号模型
     * @return
     */
    @Override
    public String arithmeticByBasic(PiCalculateSignal calculateSignal){
        String retData = "";

        PiComputationModel computationModel = calculateSignal.getPiComputationModelList().get(0);
        List<PiRetrievalStep> retrievalStepList = calculateSignal.getPiRetrievalStepList();

        String formulaCode = computationModel.getFormulaCode();

        //用于时间模型
        LocalDateTime time = calculateSignal.getCalculateTime();

        // 创建一个日期时间格式化器
        DateTimeFormatter formatterTime = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        // 创建一个日期时间格式化器
        DateTimeFormatter formatterDate = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        if(StrUtil.isNotBlank(computationModel.getAdvanceTime()) && !"0".equals(computationModel.getAdvanceTime())){
            if("0".equals(computationModel.getAdvanceDelay())){
                time = time.minusSeconds(Long.parseLong(computationModel.getAdvanceTime()));
            }else{
                time = time.plusSeconds(Long.parseLong(computationModel.getAdvanceTime()));
            }
        }

        //时间模型，不参与四则运算
        if(formulaCode.contains("timeFun()")){
            LocalDateTime resultTime = time;
            // 支持 timeFun() + 30 或 timeFun() - 30 格式
            java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("timeFun\\(\\s*\\)\\s*([+-])\\s*(\\d+)");
            java.util.regex.Matcher matcher = pattern.matcher(formulaCode);
            while (matcher.find()) {
                String operator = matcher.group(1);
                long seconds = Long.parseLong(matcher.group(2));
                if ("+".equals(operator)) {
                    resultTime = resultTime.plusSeconds(seconds);
                } else if ("-".equals(operator)) {
                    resultTime = resultTime.minusSeconds(seconds);
                }
            }
            // 使用格式化器格式化时间
            retData = resultTime.format(formatterTime);
            return retData;
        }

        //日期模型，不参与四则运算
        if(formulaCode.contains("dateFun()")){
            // 使用格式化器格式化当前时间
            retData = time.format(formatterDate);
            return retData;
        }

        //取数信号符合条件次数（直接返回，不参与四则运算）,第一个值时间和最后一个值时间也可以直接在此处返回
        //20260103新增算法，计算符合条件的时间总和timeSecondFun()
        //以下特殊函数依赖 PiRetrievalStep 取数步骤，stepType=0（瞬时取数）不应执行
        if(!"0".equals(calculateSignal.getStepType())
                && (formulaCode.contains("timesFun()")
                    || formulaCode.contains("firstTimeFun()")
                    || formulaCode.contains("lastTimeFun()")
                    || formulaCode.contains("timeSecondFun()"))
        ){
            //获取取数范围的时间段
            List<Pair<LocalDateTime,LocalDateTime>> listTime = getTimeListByRetrievalStep(calculateSignal);

            //第一个值时间
            if(formulaCode.contains("firstTimeFun()")){
                if(null != listTime && listTime.size() > 0 ){
                    LocalDateTime firstTime = listTime.get(0).getKey();
                    // 使用格式化器格式化当前时间
                    retData = firstTime.format(formatterTime);
                }
                return retData;
            }

            //最后一个值时间
            if(formulaCode.contains("lastTimeFun()")){
                if(null != listTime && listTime.size() > 0 ){
                    LocalDateTime lastTime = listTime.get(listTime.size() -1).getValue();
                    // 使用格式化器格式化当前时间
                    retData = lastTime.format(formatterTime);
                }
                return retData;
            }

            //次数
            if(formulaCode.contains("timesFun()")){
                String result = "0";
                if(null != listTime){
                    result = String.valueOf(listTime.size());
                }
                formulaCode = formulaCode.replace("timesFun()",result);
            }

            //时间段求和
            if(formulaCode.contains("timeSecondFun()")){
                String result = "0";
                if(null != listTime && listTime.size() > 0){
                    for (Pair<LocalDateTime,LocalDateTime> pair : listTime) {
                        // 计算两个时间之间的秒数差
                        long secondsDifference = Duration.between(pair.getKey(), pair.getValue()).getSeconds();
                        result = String.valueOf(Long.parseLong(result) + secondsDifference);
                    }
                }
                formulaCode = formulaCode.replace("timeSecondFun()",result);
            }
        }

        //跳出计算标识（当任意一个值为null，则跳出所有计算）
        boolean bj = false;

        //瞬时取数不支持聚合算法（前端已经控制）
        if("0".equals(calculateSignal.getStepType())){
            if(formulaCode.contains("maxFun(") || formulaCode.contains("minFun(")
                    || formulaCode.contains("averageFun(") || formulaCode.contains("sumFun(")
                    || formulaCode.contains("firstFun(") || formulaCode.contains("lastFun(") )
            {
                return retData;
            }
        }

        //定义取数频率 默认1s
        Integer frequency = 1;
        if( !retrievalStepList.isEmpty() && StrUtil.isNotBlank(retrievalStepList.get(0).getFrequency())){
            frequency = Integer.parseInt(retrievalStepList.get(0).getFrequency());
        }

        //判断是否存在最大值、最小值、平均值、求和、第一个值、最后一个值-----聚合判断
        List<String> funNames = Arrays.asList("max","min","average","sum","first","last");
        //循环方法名称，统一处理
        for(String funName : funNames){
            //截取方法名称中的值
            List<String> listPiCode = getListPiCodeByFunString(formulaCode,funName + "Fun");
            for(String piCodes : listPiCode){
                List<String> indexPi = getListPiCodeByFormulaCode(piCodes);
                for(String piCode : indexPi){
                    //获取取数范围的时间段
                    List<Pair<LocalDateTime,LocalDateTime>> listTime = getTimeListByRetrievalStep(calculateSignal);
                    //获取结果
                    String result = influxDbCommFun(piCode,funName,frequency,listTime);
                    if(StrUtil.isNotBlank(result)){
                        String funString = funName  + "Fun(" + piCodes + ")";
                        formulaCode = formulaCode.replace(funString,result);
                    }else{
                        bj = true;
                        break;
                    }
                }
            }
        }
        if(!bj){
            //拆分计算公式中所有的的基础点位tagCode
            List<String> listNew = getListPiCodeByFormulaCode(formulaCode);
            if(listNew.size() > 0){
                List<MeasurementData> listData = influxDbMoment(listNew,time,"now");
                //绝对值 absFun(⊙sdfasdfasd⊙)
                if(formulaCode.contains("absFun(")){
                    List<String> listPiCode = getListPiCodeByFunString(formulaCode,"absFun");
                    for(String piCodes : listPiCode){
                        List<String> indexPi = getListPiCodeByFormulaCode(piCodes);
                        for(String piCode : indexPi){
                            Optional<MeasurementData> dataOptional =listData.stream().filter(data -> data.getCnlNum().equals(piCode)).findFirst();
                            if (dataOptional.isPresent()){
                                Double result = dataOptional.get().getValue();
                                result = Math.abs(result);

                                String tagFun = "absFun(⊙" +piCode+"⊙)";
                                formulaCode = formulaCode.replace(tagFun,changeDataFormat(result.toString()));
                            }else{
                                bj = true;
                                break;
                            }
                        }
                    }
                }

                //为空给0算法 ifNullFun(⊙sdfasdfasd⊙)
                if(formulaCode.contains("ifNullFun(")){
                    List<String> listPiCode = getListPiCodeByFunString(formulaCode,"ifNullFun");
                    for(String piCodes : listPiCode){
                        List<String> indexPi = getListPiCodeByFormulaCode(piCodes);
                        for(String piCode : indexPi){
                            Optional<MeasurementData> dataOptional =listData.stream().filter(data -> data.getCnlNum().equals(piCode)).findFirst();
                            String result = "0";
                            if (dataOptional.isPresent()){
                                result = dataOptional.get().getValue().toString();
                            }
                            String tagFun = "ifNullFun(⊙" +piCode+"⊙)";
                            formulaCode = formulaCode.replace(tagFun,changeDataFormat(result));
                        }
                    }
                }
                //formulaCode进行替换后，获取新的listNew
                listNew = getListPiCodeByFormulaCode(formulaCode);
                for(String piCode : listNew){
                    Optional<MeasurementData> dataOptional =listData.stream().filter(data -> data.getCnlNum().equals(piCode)).findFirst();
                    if (dataOptional.isPresent()){
                        String result = dataOptional.get().getValue().toString();
                        String tagFun = "⊙" +piCode+"⊙";
                        formulaCode = formulaCode.replace(tagFun,changeDataFormat(result));
                    }else{
                        bj = true;
                        break;
                    }
                }
            }
        }
        if(!bj){
            retData = formulaCode;
            //计算公式是否包含 + - * / （ ）
            if(formulaCode.contains("+") || formulaCode.contains("-") || formulaCode.contains("/")
                    || formulaCode.contains("*") || formulaCode.contains("(")
                    || formulaCode.contains(")"))
            {
                Object result = null;
                formulaCode = formulaCode.replaceAll("(?<!\\.)\\b(\\d+)\\b(?![\\.\\d])", "$1.0");
                try{
                    JexlEngine jexlEngine = new JexlEngine();
                    result = jexlEngine.eval(formulaCode,null);
                }catch (JexlException e){
                    String msg = e.getMessage();
                    if (msg != null && (msg.contains("divide") || msg.contains("division"))) {
                        logger.warn("JEXL除零异常, formulaCode={}", formulaCode);
                        result = "0";
                    } else {
                        logger.warn("JEXL解析异常, formulaCode={}", formulaCode, e);
                        if(hasChineseOrEnglish(formulaCode)){
                            result = formulaCode;
                        }else{
                            result = "0";
                        }
                    }
                }catch (Exception e){
                    logger.warn("JEXL计算异常, formulaCode={}", formulaCode, e);
                    if(hasChineseOrEnglish(formulaCode)){
                        result = formulaCode;
                    }else{
                        result = "0";
                    }
                }
                if(!Objects.isNull(result)){
                    retData = result.toString();
                }
            }
        }
        //防止异常情况，出现没有取到PI数据，直接给空
        if(retData.contains("⊙")){
            retData = "";
        }
        return retData;
    }


    /**
     * 获取唯一约束的值
     * @param calculateSignal 触发模型
     * @return
     */
    @Override
    public List<TableConditionDto> getConditionList(PiCalculateSignal calculateSignal){
        //查询条件
        List<TableConditionDto> conditionList = new ArrayList<>();
        //查找对应的唯一约束
        List<Map<String, Object>> listUniqueMap = calculateSignal.getListUniqueMap();

        if(null == listUniqueMap || listUniqueMap.size() == 0){

            String extJson = calculateSignal.getUniqueJson();

            if(StringUtils.isEmpty(extJson)){
                return conditionList;
            }

            JSONArray jsonArray = new JSONArray(extJson);

            List<Map<String, Object>> listMap = new ArrayList<>();

            List<String> listUniqueTagCode = new ArrayList<>();
            for (int i = 0; i < jsonArray.size(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                Map<String, Object> map = new HashMap<>();
                Set<String> keys = jsonObject.keySet();
                for(String key : keys){
                    map.put(key, jsonObject.get(key));
                    //将唯一约束的tag集合进行整合
                    if(key.equals("tagCode") && null != jsonObject.get(key) && !"".equals(jsonObject.get(key))){
                        listUniqueTagCode.add(jsonObject.get(key).toString());
                    }
                }
                listMap.add(map);
            }
            //自动化取值
            calculateSignal.setListUniqueTagCode(listUniqueTagCode);
            //唯一约束
            calculateSignal.setListUniqueMap(listMap);

            listUniqueMap = calculateSignal.getListUniqueMap();
        }

        //获取存储对应唯一约束对应的tag
        List<String> tagList = calculateSignal.getListUniqueTagCode();

        //唯一约束的时间计算
        LocalDateTime timeUnique = calculateSignal.getCalculateTime();
        if(StrUtil.isNotBlank(calculateSignal.getAdvanceTime()) && !"0".equals(calculateSignal.getAdvanceTime())){
            if("0".equals(calculateSignal.getAdvanceDelay())){
                timeUnique = timeUnique.minusSeconds(Long.parseLong(calculateSignal.getAdvanceTime()));
            }else{
                timeUnique = timeUnique.plusSeconds(Long.parseLong(calculateSignal.getAdvanceTime()));
            }
        }
        //获取唯一约束的值
        List<MeasurementData> listData = new ArrayList<>();
        if(tagList.size() > 0){
            listData = influxDbMoment(tagList,timeUnique,"now");
        }
        //获取唯一约束中所有的monitorModelId（模型ID）
        List<String> listMonitorModelId = listUniqueMap.stream().map(map -> map.get("monitorModelId"))
                .filter(Objects::nonNull)
                .map(Object::toString)
                .distinct()
                .collect(Collectors.toList());
        //获取唯一约束中模型ID所对应的模型转换模型
        List<PiResultTransformationModel> piResultTransformationModelList = new ArrayList<>();
        if (listMonitorModelId.size() > 0){
            piResultTransformationModelList = iPiResultTransformationModelService.queryListByResultModelIds(listMonitorModelId);
        }

        //获取自动化系统中的数据之后，将数据补全到listUniqueMap中
        A:for(Map<String, Object> map : listUniqueMap){
            TableConditionDto tableConditionDto = new TableConditionDto();
            tableConditionDto.setKey(map.get("colCode").toString());

            //判断是否取自动化定位,tagCode为空就取tagValue
            if(Objects.isNull(map.get("tagCode")) || "".equals(map.get("tagCode"))){
                //定义tagValue的实际值
                String tagValue = map.get("tagValue").toString();

                //4位年
                if(map.get("tagValue").equals("yearFun")){
                    tagValue = timeUnique.getYear()+"";
                }
                //2位年
                if(map.get("tagValue").equals("year2Fun")){
                    String year = String.valueOf(timeUnique.getYear());
                    year = year.substring(year.length()-2);
                    tagValue = year;
                }
                //获取当前小时
                if(map.get("tagValue").equals("hourFun")){
                    // 创建一个日期时间格式化器，用于自定义输出格式
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH");
                    // 格式化当前的日期和时间
                    String formattedDateTime = timeUnique.format(formatter) + ":00:00";
                    tagValue = formattedDateTime;
                }
                //获取当前小时（偶数）
                if(map.get("tagValue").equals("hour2Fun")){
                    int hour = timeUnique.getHour();
                    //判断小数是否是偶数
                    if(hour % 2 != 0){
                        timeUnique = timeUnique.minusHours(1);
                    }
                    // 创建一个日期时间格式化器，用于自定义输出格式
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH");
                    // 格式化当前的日期和时间
                    String formattedDateTime = timeUnique.format(formatter) + ":00:00";
                    tagValue = formattedDateTime;
                }
                //获取当前分钟
                if(map.get("tagValue").equals("minuteFun")){
                    // 创建一个日期时间格式化器，用于自定义输出格式
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
                    // 格式化当前的日期和时间
                    String formattedDateTime = timeUnique.format(formatter) + ":00";
                    tagValue = formattedDateTime;
                }
                //获取当前分钟
                if(map.get("tagValue").equals("timeFreFun") || map.get("tagValue").equals("minFreFun")){
                    // 创建一个日期时间格式化器，用于自定义输出格式
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                    // 格式化当前的日期和时间
                    String formattedDateTime = timeUnique.format(formatter);
                    tagValue = formattedDateTime;
                }
                //获取当前日期currentDateFun
                if(map.get("tagValue").equals("currentDateFun")){
                    // 创建一个日期时间格式化器，用于自定义输出格式
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                    // 格式化当前的日期和时间
                    String formattedDateTime = timeUnique.format(formatter);
                    tagValue = formattedDateTime;
                }
                //获取昨天日期yesterdayDateFun
                if(map.get("tagValue").equals("yesterdayDateFun")){
                    LocalDateTime timeUniqueTemp = timeUnique.minusDays(1);
                    // 创建一个日期时间格式化器，用于自定义输出格式
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                    // 格式化当前的日期和时间
                    String formattedDateTime = timeUniqueTemp.format(formatter);
                    tagValue = formattedDateTime;
                }
                map.put("tagValue",tagValue);
            }else{
                //自动化取数
                //唯一约束值，任意一个为空，将不进行计算
                B:for(MeasurementData data : listData){
                    if(map.get("tagCode").equals(data.getCnlNum())){
                        Double val = data.getValue();
                        map.put("tagValue",changeDataFormat(String.valueOf(val)));
                        break;
                    }
                }
            }
            //对最后的格式进行处理，存储到对象中
            if(!Objects.isNull(map.get("tagValue")) && !"".equals(map.get("tagValue"))){
                String tagValue = map.get("tagValue").toString();
                //自动化点位需要判断做数字转换
                if(!Objects.isNull(map.get("tagCode")) && !"".equals(map.get("tagCode"))){
                    tagValue = retStrByInfluxDouble(map.get("tagCode").toString(),Double.parseDouble(tagValue));
                }
                //对数据进行格式处理
                tagValue = changeDataFormat(tagValue);
                //判断是否需要对结果进行模型转换
                String monitorModelId = Objects.isNull(map.get("monitorModelId")) ? "" : map.get("monitorModelId").toString();
                if (StrUtil.isNotBlank(monitorModelId)){
                    Optional<PiResultTransformationModel > optionalPiResultTransformationModel = piResultTransformationModelList.stream().filter(entity ->entity.getId().equals(monitorModelId)).findFirst();
                    if (optionalPiResultTransformationModel.isPresent()){
                        tagValue = resultConvertModel(tagValue,optionalPiResultTransformationModel.get(), calculateSignal.getCalculateTime());
                    }
                }
                tableConditionDto.setVal(tagValue);
            }
            conditionList.add(tableConditionDto);
        }
        return conditionList;
    }

    /**
     * 根据范围取数信号获取符合要求的时间范围
     * @param calculateSignal 触发信号
     * @return
     */
    @Override
    public List<Pair<LocalDateTime,LocalDateTime>> getTimeListByRetrievalStep(PiCalculateSignal calculateSignal){
        List<Pair<LocalDateTime,LocalDateTime>> retTimePairs = new ArrayList<>();
        //取数信号
        List<PiRetrievalSignal> retrievalSignalListBefor = calculateSignal.getPiRetrievalSignalList();
        //获取步骤范围的开始时间和结束时间
        Pair<LocalDateTime,LocalDateTime> timePair = getStepDate(calculateSignal);
        //开始时间
        LocalDateTime startTime = timePair.getKey();
        //结束时间
        LocalDateTime endTime = timePair.getValue();

        if(null == startTime || null == endTime){
            return retTimePairs;
        }

        // 指定时区，例如系统默认时区
        ZoneId zoneId = ZoneId.systemDefault();

        //判断是否有取数信号，没有取数信号，直接返回步骤范围的开始时间和结束时间
        if(null == retrievalSignalListBefor || retrievalSignalListBefor.size() == 0){
            retTimePairs.add(Pair.of(startTime,endTime));
            return retTimePairs;
        }
        //判断是否存在跟取数范围有关的取数信号
        List<PiRetrievalSignal> retrievalSignalList = retrievalSignalListBefor.stream().filter(e->e.getApplyTo().equals("1")).collect(Collectors.toList());
        if(null == retrievalSignalList || retrievalSignalList.size() == 0){
            retTimePairs.add(Pair.of(startTime,endTime));
            return retTimePairs;
        }
        //框定范围取数的最大范围，根据取数信号判断分段取数
        //有过滤条件，需要添加过滤条件，然后手动循环做对应的计算
        //一次性获取所有的取数信号的值
        List<String> retrievalSignalTagCodeList = retrievalSignalList.stream().map(PiRetrievalSignal::getPointCode).distinct().collect(Collectors.toList());
        //存在点位和点位之间的比较，需要做处理
        List<String> codes = retrievalSignalList.stream().filter(e -> e.getValueType().equals("1")).map(e -> e.getCalculateValue()).distinct().collect(Collectors.toList());
        if(null != codes && codes.size() > 0){
            retrievalSignalTagCodeList.addAll(codes);
            //去重复
            retrievalSignalTagCodeList = retrievalSignalTagCodeList.stream().distinct().collect(Collectors.toList());
        }

        //将集合和时间点传入influxDB，返回对应的自动化值
        QueryCondition queryCondition = new QueryCondition();
        //点位tag
        List<QueryTagCondition> queryTagConditionList = new ArrayList<>();
        for(String retrievalSignalTagCode : retrievalSignalTagCodeList){
            QueryTagCondition queryTagCondition = new QueryTagCondition();
            queryTagCondition.setTagId(retrievalSignalTagCode);
            queryTagConditionList.add(queryTagCondition);
        }
        queryCondition.setFrequency(1);
        queryCondition.setQueryType(QueryType.MEAN);
        queryCondition.setStart(startTime);
        queryCondition.setEnd(endTime);
        queryCondition.setQueryTagConditionList(queryTagConditionList);
        //获取所有的条件点位集合
        List<MeasurementData> listPointData = new ArrayList<>();
        if(queryTagConditionList.size() > 0){
            listPointData = influxDBClientHelper.query(queryCondition);
        }

        if(null != listPointData && listPointData.size() > 0 && null != listPointData.get(0)){
            //将取数信号，按照标识符组装成map
            Map<String, List<PiRetrievalSignal>> listMap = retrievalSignalList.stream()
                    .collect(Collectors.groupingBy(PiRetrievalSignal::getIdentifier));

            //循环所有的的取数信号,按照且跟或的关系进行循环，第一层循环是或关系，第二层循环是且关系
            Set<String> keys = listMap.keySet();
            for(String key : keys){
                //临时存储当前并关系中，符合要求的时间段
                List<Pair<LocalDateTime,LocalDateTime>> timePairsNow = new ArrayList<>();
                //获取取数信号点位，关系为且的
                List<PiRetrievalSignal> signals = listMap.get(key);

                for(int j = 0; j < signals.size(); j++){
                    PiRetrievalSignal signal = signals.get(j);
                    //单个条件符合的时间段
                    List<Pair<LocalDateTime,LocalDateTime>> timePairsOneTag = new ArrayList<>();

                    List<MeasurementData> measurementDataByTagCode =
                            listPointData.stream().filter(pointCodeVo->pointCodeVo.getCnlNum().equals(signal.getPointCode())).collect(Collectors.toList());

                    //第二个点位的点位集合
                    List<MeasurementData> measurementDataByValue = new ArrayList<>();
                    if(StringUtils.isNotBlank(signal.getValueType()) && "1".equals(signal.getValueType())){
                        measurementDataByValue = listPointData.stream().filter(pointCodeVo->pointCodeVo.getCnlNum().equals(signal.getCalculateValue())).collect(Collectors.toList());
                    }

                    //判断是否是数据字典
                    List<Double> dicCodeList = new ArrayList<>();
                    if(StringUtils.isNotBlank(signal.getValueType()) && "2".equals(signal.getValueType())){
                        List<MdmDict> listDic = mdmDictService.lambdaQuery().eq(MdmDict::getGroupCode, signal.getCalculateValue()).list();
                        if(null != listDic && listDic.size() > 0){
                            dicCodeList =
                                    listDic.stream()
                                            .map(MdmDict::getItemVal)
                                            .filter(Objects::nonNull) // 过滤掉 null 值
                                            .map(itemVal -> {
                                                try {
                                                    return Double.parseDouble(itemVal);
                                                } catch (NumberFormatException e) {
                                                    // 处理无法转换的情况，例如记录日志或返回默认值
                                                    return 0.0; // 或者其他默认值
                                                }
                                            })
                                            .collect(Collectors.toList());
                        }
                    }

                    //记录所有时间
                    LocalDateTime startPairTime = null;
                    LocalDateTime endPairTime = null;

                    //记录上一步，用于判断是否连续
                    int i_pre = 0;
                    for(int i = 0; i < measurementDataByTagCode.size(); i++){
                        MeasurementData measurementData = measurementDataByTagCode.get(i);
                        //过滤类型 1> 2>= 3= 4!= 5< 6<= 7包含
                        String calculateType = signal.getCalculateType();

                        //设定值
                        double calculateValue = -1000.0 ;

                        //点位
                        if(StringUtils.isNotBlank(signal.getValueType()) && "1".equals(signal.getValueType())){
                            Optional<MeasurementData> calculateValueOptional = measurementDataByValue.stream().filter(pointCodeVo->pointCodeVo.getTime().equals(measurementData.getTime())).findAny();
                            if(calculateValueOptional.isPresent()){
                                calculateValue = calculateValueOptional.get().getValue();
                            }
                        }
                        //值
                        if(StringUtils.isNotBlank(signal.getValueType()) && "0".equals(signal.getValueType())){
                            calculateValue = Double.parseDouble(signal.getCalculateValue());
                        }

                        //实际值
                        double measurementDataValue = measurementData.getValue();

                        int result = Double.compare(measurementDataValue, calculateValue);
                        boolean isTrue = false;
                        switch (calculateType){
                            case "1" :
                                if(result > 0){
                                    isTrue = true;
                                }
                                break;
                            case "2" :
                                if(result >= 0 ){
                                    isTrue = true;
                                }
                                break;
                            case "3" :
                                if(result == 0 ){
                                    isTrue = true;
                                }
                                break;
                            case "4" :
                                if(result != 0 ){
                                    isTrue = true;
                                }
                                break;
                            case "5" :
                                if(result < 0 ){
                                    isTrue = true;
                                }
                                break;
                            case "6" :
                                if(result <= 0 ){
                                    isTrue = true;
                                }
                                break;
                            case "7" :
                                if(dicCodeList.contains(measurementDataValue)){
                                    isTrue = true;
                                }
                                break;
                        }
                        if(isTrue){
                            if(i_pre == 0){
                                // 将 Instant 转换为 LocalDateTime
                                startPairTime = measurementData.getTime().atZone(zoneId).toLocalDateTime();
                            }
                            endPairTime = measurementData.getTime().atZone(zoneId).toLocalDateTime();
                            i_pre ++;
                            if( i == measurementDataByTagCode.size()-1 && i_pre != 0){
                                timePairsOneTag.add(Pair.of(startPairTime,endPairTime));
                            }
                        }else{
                            if(i_pre != 0){
                                timePairsOneTag.add(Pair.of(startPairTime,endPairTime));
                                i_pre = 0;
                            }
                        }
                    }
                    //任意一次
                    if(timePairsOneTag.size() == 0){
                        timePairsNow = new ArrayList<>();
                        break;
                    }else{
                        //判断提前或者延后,对当前所有时间集合的第一个点做提前或者延后处理
                        for(int k = 0; k< timePairsOneTag.size(); k++){
                            if(StringUtils.isNotBlank(signal.getAdvanceDelay()) && StringUtils.isNotBlank(signal.getAdvanceTime())){
                                //提前
                                if("0".equals(signal.getAdvanceDelay())){
                                    Pair<LocalDateTime,LocalDateTime> pairFirst = Pair.of(timePairsOneTag.get(k).getKey().minusSeconds(Long.parseLong(signal.getAdvanceTime())),timePairsOneTag.get(k).getValue());
                                    timePairsOneTag.set(k,pairFirst);
                                }
                                //延后
                                if("1".equals(signal.getAdvanceDelay())){
                                    Pair<LocalDateTime,LocalDateTime> pairFirst = Pair.of(timePairsOneTag.get(k).getKey().plusSeconds(Long.parseLong(signal.getAdvanceTime())),timePairsOneTag.get(k).getValue());
                                    timePairsOneTag.set(k,pairFirst);
                                }
                            }
                        }

                        //涉及多次循环，就需要取时间段的交集，单次循环不需要取
                        if(j == 0){
                            timePairsNow = timePairsOneTag;
                        }else{
                            timePairsNow = getIntersectionFromTimeList(timePairsNow,timePairsOneTag);
                        }
                    }
                }
                retTimePairs.addAll(timePairsNow);
            }
        }
        retTimePairs = mergeOverlappingTimePairs(retTimePairs);
        //将retTimePairs中的时间差小于取数频率的部分剔除掉
        String frequency = calculateSignal.getPiRetrievalStepList().get(0).getFrequency();
        frequency = StrUtil.isBlank(frequency)? "0" : frequency;
        long frequencyLong = Long.parseLong( frequency);
        retTimePairs = retTimePairs.stream().filter(pair -> Duration.between(pair.getKey(), pair.getValue()).getSeconds() >= frequencyLong).collect(Collectors.toList());

        //新增特殊需求，前端选择是否按照条件切割，如果retTimePairs有值，则需要判断是否切割，如果没值，则无需判断20250310
        if(retTimePairs.size() > 0){
            if(StrUtil.isNotBlank(calculateSignal.getPiRetrievalStepList().get(0).getIsSlicing())
                    && "0".equals(calculateSignal.getPiRetrievalStepList().get(0).getIsSlicing())){
                retTimePairs = new ArrayList<>();
                retTimePairs.add(Pair.of(startTime,endTime));
            }
        }
        return  retTimePairs;
    }

    /**
     * 计算（最大值、最小值、平均值、累计值、第一个值、最后一个值）-范围计算
     * @param tagCode 基础点位code
     * @param type 计算类型
     * @param frequency 取数频率
     * @param listTimePair 取数时间集合
     * @return
     */
    public String influxDbCommFun(String tagCode, String type,Integer frequency, List<Pair<LocalDateTime,LocalDateTime>> listTimePair){
        String retData = "";
        //获取到所有符合条件的时间段集合，然后获取指定基础点位在对应时间段内的明细数据
        List<MeasurementData> listPointDataAll = new ArrayList<>();

        for(Pair<LocalDateTime,LocalDateTime> dateTimePair : listTimePair){
            //将集合和时间点传入influxDB，返回对应的自动化值
            QueryCondition queryCondition = new QueryCondition();
            List<QueryTagCondition> queryTagConditionList = new ArrayList<>();

            QueryTagCondition queryTagCondition = new QueryTagCondition();
            queryTagCondition.setTagId(tagCode);
            queryTagConditionList.add(queryTagCondition);

            queryCondition.setStart(dateTimePair.getKey());
            queryCondition.setEnd(dateTimePair.getValue());
            queryCondition.setQueryTagConditionList(queryTagConditionList);

            queryCondition.setFrequency(frequency);
            queryCondition.setQueryType(QueryType.LAST);

            //获取所有的触发步骤点位集合
            List<MeasurementData> listPointDataPI = new ArrayList<>();
            if(queryTagConditionList.size() > 0){
                listPointDataPI = influxDBClientHelper.query(queryCondition);
            }
            if(listPointDataPI.size() > 0 && null != listPointDataPI.get(0)){
                //如果最后一个值不符合要求，则剔除
                if(listPointDataPI.size() >= 2){
                    // 计算两个 Instant 对象之间的时间差
                    Duration duration = Duration.between(listPointDataPI.get(listPointDataPI.size()-2).getTime(), listPointDataPI.get(listPointDataPI.size()-1).getTime());
                    long seconds = duration.getSeconds();
                    if(seconds != frequency){
                        listPointDataPI.remove(listPointDataPI.size()-1);
                    }
                }
                listPointDataAll.addAll(listPointDataPI);
            }
        }
        //数据为空，直接返回空字符串
        if (listPointDataAll.isEmpty()) {
            return "";
        }
        //参与计算的值（double类型）
        Double resultNum = null;
        //取第一个值
        if("first".equals(type)){
            if(listPointDataAll.get(0) != null){
                resultNum = listPointDataAll.get(0).getValue();
            }
        }else if("last".equals(type)){
            if(listPointDataAll.get(listPointDataAll.size() - 1) != null){
                resultNum = listPointDataAll.get(listPointDataAll.size()-1).getValue();
            }
        }else{
            //临时定义一个变量，为了增量做准备，增量主要是本次-上次的量，变量存储上一次的结果
            Double preDataValue = null;
            //获取所有的数据之后，对结果进行分析
            for(int i = 0; i < listPointDataAll.size() ; i++){
                MeasurementData measurementData = listPointDataAll.get(i);
                if(i == 0){
                    if("sum".equals(type)){
                        preDataValue = measurementData.getValue();
                        resultNum = 0.0;
                    }else{
                        resultNum = measurementData.getValue();
                    }
                }else {
                    //取最大值
                    if("max".equals(type)){
                        if(measurementData.getValue() > resultNum){
                            resultNum = measurementData.getValue();
                        }
                    }
                    //最小值
                    if("min".equals(type)){
                        if(measurementData.getValue() < resultNum){
                            resultNum = measurementData.getValue();
                        }
                    }
                    //平均值，平均值用所有的和，然后除以总数
                    if("average".equals(type)){
                        resultNum = resultNum + measurementData.getValue();
                    }
                    //增量
                    if("sum".equals(type)){
                        Double sum = measurementData.getValue() - preDataValue;
                        preDataValue = measurementData.getValue();
                        //防止清零或者跳变
                        if(sum > 0){
                            resultNum = resultNum + sum;
                        }
                    }
                }
            }
        }
        //平均值需要做处理
        if("average".equals(type)){
            resultNum = resultNum/listPointDataAll.size();
        }
        if(Objects.nonNull(resultNum)){
            retData = String.valueOf(resultNum);
        }

        return changeDataFormat(retData);
    }


    /**
     * 计算（最大值、最小值、平均值、累计值、第一个值、最后一个值等）-监控型算法
     * @param calculateSignal 计算信号
     * @param frequency 频率
     * @param listTimePair 时间集合
     * @return
     */
    @Override
    public String influxDbCommFunByMonitoring(
            PiCalculateSignal calculateSignal,
            Integer frequency, List<Pair<LocalDateTime,LocalDateTime>> listTimePair
    ){
        String retData = "";
        List<PiComputationMonitorModel> listMonitorModel = calculateSignal.getPiComputationMonitorModelList();
        //获取到所有符合条件的时间段集合，然后获取指定基础点位在对应时间段内的明细数据
        List<MeasurementData> listPointDataAll = new ArrayList<>();

        for(Pair<LocalDateTime,LocalDateTime> dateTimePair : listTimePair){
            //将集合和时间点传入influxDB，返回对应的自动化值
            QueryCondition queryCondition = new QueryCondition();
            List<QueryTagCondition> queryTagConditionList = new ArrayList<>();

            //将实际点位加入计算列表
            QueryTagCondition queryTagCondition = new QueryTagCondition();
            queryTagCondition.setTagId(listMonitorModel.get(0).getActualValue());
            queryTagConditionList.add(queryTagCondition);
            //判断设定点位是否是基础点位还是手输入的值，设定的基础点位加入计算列表
            if(StrUtil.isNotBlank(listMonitorModel.get(0).getPresumePoint())){
                queryTagCondition = new QueryTagCondition();
                queryTagCondition.setTagId(listMonitorModel.get(0).getPresumePoint());
                queryTagConditionList.add(queryTagCondition);
            }

            queryCondition.setStart(dateTimePair.getKey());
            queryCondition.setEnd(dateTimePair.getValue());
            queryCondition.setQueryTagConditionList(queryTagConditionList);

            queryCondition.setFrequency(frequency);
            queryCondition.setQueryType(QueryType.LAST);

            //获取所有的触发步骤点位集合
            List<MeasurementData> listPointDataPI = new ArrayList<>();
            if(queryTagConditionList.size() > 0){
                listPointDataPI = influxDBClientHelper.query(queryCondition);
            }

            if(listPointDataPI.size() > 0 && null != listPointDataPI.get(0)){
                //将listPointDataPI集合按照MeasurementData中的cnlNum进行分组
                Map<String, List<MeasurementData>> groupedByCnlNum = listPointDataPI.stream()
                        .collect(Collectors.groupingBy(MeasurementData::getCnlNum));
                //直接判断当前集合的最后一个点位跟倒数第二个点位之间的差是否跟频率匹配，如果不匹配，直接删除最后一个点
                groupedByCnlNum.keySet().forEach(cnlNum -> {
                    List<MeasurementData> list = groupedByCnlNum.get(cnlNum);
                    if(list.size() >=2){
                        // 计算两个 Instant 对象之间的时间差
                        Duration duration = Duration.between(list.get(list.size()-2).getTime(), list.get(list.size()-1).getTime());
                        long seconds = duration.getSeconds();
                        if(seconds != frequency){
                            list.remove(list.size()-1);
                        }
                    }
                    listPointDataAll.addAll(list);
                });
            }
        }
//        LocalDate currentDate = LocalDate.now();
//        DateTimeFormatter formatterA = DateTimeFormatter.ofPattern("yyyy-MM-dd");
//        String formattedDate = currentDate.format(formatterA);
//        //文件路径
//        String path = "/home/mes3/calculateLog/" + formattedDate + "/" + calculateSignal.getPiTable() + "/" + calculateSignal.getPiCode() + ".log";
        //获取实际点位的数据列表
        List<MeasurementData> actualList = listPointDataAll.stream().filter(e->e.getCnlNum().equals(listMonitorModel.get(0).getActualValue()))
                .collect(Collectors.toList());
//
//        //写入标题
//        String content = "报表名称：" + calculateSignal.getPiTable() + "，PI编码：" + calculateSignal.getPiCode() + "，PI名称："
//                + calculateSignal.getPiName() + "，当前时间：" + LocalDateTime.now() + "，触发时间：" + calculateSignal.getCalculateTime() + "\n";
//
//        writeListPointDataToFile("实际值：" + content,actualList,path);
//
        //设定点位的数据列表
        List<MeasurementData> presumeList = new ArrayList<>();
        if(StrUtil.isNotBlank(listMonitorModel.get(0).getPresumePoint())){
            presumeList = listPointDataAll.stream().filter(e->e.getCnlNum().equals(listMonitorModel.get(0).getPresumePoint()))
                    .collect(Collectors.toList());
//            writeListPointDataToFile("设定值：" + content,presumeList,path);
        }

        //20250107新增需求，设定值取红黄区的设定值，如果设定值设置成红黄区的设定值的话，presume_value存储的内容是piStandardData
        PiStandardMaintenance resultPiStandardMaintenance = null;

        Double presumeValueStandar = null;
        if(StrUtil.isNotBlank(listMonitorModel.get(0).getPresumeValue()) && listMonitorModel.get(0).getPresumeValue().equals("piStandardData")){
            //查找标准
            resultPiStandardMaintenance = getStandarValueBySignal(calculateSignal,calculateSignal.getPiCode(),null);
            if(null != resultPiStandardMaintenance && null != resultPiStandardMaintenance.getStandardValue()){
                presumeValueStandar = resultPiStandardMaintenance.getStandardValue();
            }
            if(null == presumeValueStandar){
                return retData;
            }
        }

        //获得所有的数据后，进行监控计算
//        当 左偏差值【输入框（正数、负数、小数）】 左偏差条件【大于/小于/大于等于…】计算值类型【实际值/差/比值/偏差值】 右偏差条件 【大于/小于/大于等于…】 右偏差值【输入框（正数、负数、小数）】
//        的 计算方式【连续次数/次数】   计算条件【大于/小于/大于等于…】 计算次数【输入框】 次，
//        取结果值类型 【实际值/差/比值/偏差值】  结果值模型【最大值、最小值、平均值、第一个值、最后一个值、偏差最大的值】
        for(PiComputationMonitorModel monitorModel : listMonitorModel){
            //最大连续次数
            int continuousTimesMax = 0;
            // 次数
            int count = 0;
            //连续次数
            int continuousTimes = 0;


            //当前条件是否成立
            boolean successBj = false;
            //上个点的生成时间
            Instant previousTime = null;

            //符合条件的集合（次数）
            List<MeasurementData> countDataList = new ArrayList<>();
            //符合条件的集合（连续次数）
            List<MeasurementData> consecutiveDataList = new ArrayList<>();


            //循环实际值的集合
            for(int i = 0; i < actualList.size(); i++){
                MeasurementData measurementDataActual = actualList.get(i);
                //条件计算值
                Double calculationValue = null;
                //实际值
                if("1".equals(monitorModel.getValueType())){
                    calculationValue = measurementDataActual.getValue();
                }else{
                    //设定值
                    Double presumeValue = null;
                    //判断是否引用红黄区的配置
                    if(StrUtil.isNotBlank(listMonitorModel.get(0).getPresumeValue()) && listMonitorModel.get(0).getPresumeValue().equals("piStandardData")){
                        presumeValue = presumeValueStandar;
                    }else{
                        if(StrUtil.isNotBlank(monitorModel.getPresumePoint())){
                            Optional<MeasurementData> measurementDataPresumeOptional = presumeList.stream().filter(e->e.getTime().equals(measurementDataActual.getTime())).findFirst();
                            if(measurementDataPresumeOptional.isPresent()){
                                presumeValue = measurementDataPresumeOptional.get().getValue();
                            }
                        }else{
                            if(StrUtil.isNotBlank(monitorModel.getPresumeValue())){
                                presumeValue = Double.parseDouble(monitorModel.getPresumeValue());
                            }
                        }
                    }
                    if(!Objects.isNull(presumeValue)){
                        //差
                        if("2".equals(monitorModel.getValueType())){
                            calculationValue = measurementDataActual.getValue() - presumeValue;
                        }
                        //比值
                        if("3".equals(monitorModel.getValueType())){
                            calculationValue = (measurementDataActual.getValue() - presumeValue ) / presumeValue;
                        }
                        //绝对值
                        if("4".equals(monitorModel.getValueType())){
                            calculationValue = Math.abs(measurementDataActual.getValue() -presumeValue);
                        }
                    }else{
                        //将最大连续次数赋值给全局变量
                        if(continuousTimesMax < continuousTimes){
                            continuousTimesMax = continuousTimes;
                        }
                        continuousTimes = 0;
                        continue;
                    }
                }
                if(!Objects.isNull(calculationValue)){
                    boolean isContinue = false;
                    //进入红区 7
                    //进去黄区 8
                    //进入绿区 9
                    //进入红黄区 10
                    List<String> typeList = Arrays.asList("7","8","9","10");
                    //当需要引入红黄区上下限的时候
                    if(StrUtil.isNotBlank(monitorModel.getRightCondition()) && typeList.contains(monitorModel.getRightCondition())){
                        //获取PI颜色
                        String color = getColorByPiData(calculateSignal,calculationValue,resultPiStandardMaintenance);
                        if(StrUtil.isNotBlank(color)){
                            switch (monitorModel.getRightCondition()){
                                case "7" :
                                    if(color.equals("red")){
                                        isContinue = true;
                                    }
                                    break;
                                case "8" :
                                    if(color.equals("yellow")){
                                        isContinue = true;
                                    }
                                    break;
                                case "9" :
                                    if(color.equals("green")){
                                        isContinue = true;
                                    }
                                    break;
                                case "10" :
                                    if(color.equals("red") || color.equals("yellow")){
                                        isContinue = true;
                                    }
                                    break;
                            }
                        }
                    }else{
                        //左偏差值状态
                        boolean leftContinue = true;
                        //右偏差值状态
                        boolean rightContinue = true;
                        //isContinue
                        //左偏差值
                        if(StrUtil.isNotBlank(monitorModel.getLeftValue()) && StrUtil.isNotBlank(monitorModel.getLeftCondition())){
                            leftContinue = isCorrectCalculate(Double.parseDouble(monitorModel.getLeftValue()),calculationValue,monitorModel.getLeftCondition());
                        }
                        //右偏差值
                        if(StrUtil.isNotBlank(monitorModel.getRightValue()) && StrUtil.isNotBlank(monitorModel.getRightCondition())){
                            rightContinue = isCorrectCalculate(calculationValue,Double.parseDouble(monitorModel.getRightValue()),monitorModel.getRightCondition());
                        }
                        if(leftContinue && rightContinue){
                            isContinue = true;
                        }else{
                            isContinue = false;
                        }
                    }
                    //符合要求
                    if(isContinue){
                        count ++;
                        countDataList.add(measurementDataActual);

                        //判断是否连续，判断当前点位和上个点位是否中断，时间差大于频率则中断，连续取消
                        //previousTime为空，则说明是第一个点
                        if(null == previousTime){
                            continuousTimes ++;
                        }else{
                            // 计算时间差
                            Duration duration = Duration.between(previousTime, measurementDataActual.getTime());
                            // 输出时间差的各个部分
                            long seconds = duration.getSeconds();
                            if(seconds == frequency){
                                continuousTimes ++;
                            }else{
                                //将最大连续次数赋值给全局变量
                                if(continuousTimesMax < continuousTimes){
                                    continuousTimesMax = continuousTimes;
                                }
                                //判断连续是否达标，达标的话，将数据进行存档
                                //标准比较计数
                                int standardCount = 0;
                                if(Integer.parseInt(monitorModel.getCalculationNumber()) == -1){
                                    standardCount = actualList.size();
                                }else{
                                    if("1".equals(monitorModel.getCalculationMethod())){
                                        standardCount = Integer.parseInt(monitorModel.getCalculationNumber());
                                    }
                                }
                                successBj = isCorrectCalculate(continuousTimes,standardCount,monitorModel.getCalculationCondition());
                                if(successBj){
                                    consecutiveDataList.addAll(actualList.subList(i-continuousTimes,i));
                                }
                                continuousTimes = 0;
                            }
                        }
                    }else{
                        //将最大连续次数赋值给全局变量
                        if(continuousTimesMax < continuousTimes){
                            continuousTimesMax = continuousTimes;
                        }

                        //判断连续是否达标，达标的话，将数据进行存档
                        //标准比较计数
                        int standardCount = 0;
                        if(Integer.parseInt(monitorModel.getCalculationNumber()) == -1){
                            standardCount = actualList.size();
                        }else{
                            if("1".equals(monitorModel.getCalculationMethod())){
                                standardCount = Integer.parseInt(monitorModel.getCalculationNumber());
                            }
                        }
                        successBj = isCorrectCalculate(continuousTimes,standardCount,monitorModel.getCalculationCondition());
                        if(successBj){
                            consecutiveDataList.addAll(actualList.subList(i-continuousTimes,i));
                        }

                        continuousTimes = 0;
                    }
                }else {
                    //将最大连续次数赋值给全局变量
                    if(continuousTimesMax < continuousTimes){
                        continuousTimesMax = continuousTimes;
                    }
                    //判断连续是否达标，达标的话，将数据进行存档
                    //标准比较计数
                    int standardCount = 0;
                    if(Integer.parseInt(monitorModel.getCalculationNumber()) == -1){
                        standardCount = actualList.size();
                    }else{
                        if("1".equals(monitorModel.getCalculationMethod())){
                            standardCount = Integer.parseInt(monitorModel.getCalculationNumber());
                        }
                    }
                    successBj = isCorrectCalculate(continuousTimes,standardCount,monitorModel.getCalculationCondition());
                    if(successBj){
                        consecutiveDataList.addAll(actualList.subList(i-continuousTimes,i));
                    }
                    continuousTimes = 0;
                }
                //将当前点位信息传递给上一个点
                previousTime = measurementDataActual.getTime();
            }

            //将最大连续次数赋值给全局变量
            if(continuousTimesMax < continuousTimes){
                continuousTimesMax = continuousTimes;
            }
            //判断连续是否达标，达标的话，将数据进行存档
            //标准比较计数
            int standardCount = 0;
            if(Integer.parseInt(monitorModel.getCalculationNumber()) == -1){
                standardCount = actualList.size();
            }else{
                if("1".equals(monitorModel.getCalculationMethod())){
                    standardCount = Integer.parseInt(monitorModel.getCalculationNumber());
                }
            }
            successBj = isCorrectCalculate(continuousTimes,standardCount,monitorModel.getCalculationCondition());
            if(successBj){
                consecutiveDataList.addAll(actualList.subList(actualList.size()-continuousTimes,actualList.size()));
            }

            //判断是否需要全部满足要求，如果需要全部符合，只按照次数来判断，不按照连续次数判断
            if(Integer.parseInt(monitorModel.getCalculationNumber()) == -1){
                successBj = isCorrectCalculate(count,actualList.size(),monitorModel.getCalculationCondition());
            }else{
                //先判断是否连续次数
                if("1".equals(monitorModel.getCalculationMethod())){
                    //判断是否连续，判断当前点位和上个点位是否中断，时间差大于频率则中断，连续取消
                    successBj = isCorrectCalculate(continuousTimesMax,Integer.parseInt(monitorModel.getCalculationNumber()),monitorModel.getCalculationCondition());
                }
                //连续次数和全部等于在上面已经校验，此处校验次数非连续
                if("2".equals(monitorModel.getCalculationMethod())){
                    successBj = isCorrectCalculate(count,Integer.parseInt(monitorModel.getCalculationNumber()),monitorModel.getCalculationCondition());
                }
            }

            if(successBj){
                //当前条件成立,计算结果
                String retDataDou = null;

                //临时存储，特殊算法使用
                Double retDataDouTemp = null;

                // 定义一个时区，例如系统默认时区
                ZoneId zoneId = ZoneId.systemDefault();
                // 定义一个日期时间格式化器
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

                //绿区计数
                int greenNum = 0;
                //黄区计数
                int yellowNum = 0;
                //红区计数
                int redNum = 0;

                //常规算法 17取第一个符合条件的值，18取第一个符合条件的值的时间，19取最后一个符合条件的值，20取最后一个符合条件的值时间
                if(!Arrays.asList("17","18","19","20").contains(monitorModel.getResultModel())){
                    for(int i = 0; i < actualList.size(); i++){
                        MeasurementData measurementDataActual = actualList.get(i);
                        //实际值
                        Double actualValue = measurementDataActual.getValue();
                        //设定值
                        Double presumeValue = null;
                        if(StrUtil.isNotBlank(listMonitorModel.get(0).getPresumeValue()) && listMonitorModel.get(0).getPresumeValue().equals("piStandardData")){
                            presumeValue = presumeValueStandar;
                        }else{
                            if(StrUtil.isNotBlank(monitorModel.getPresumePoint())){
                                Optional<MeasurementData> measurementDataPresumeOptional = presumeList.stream().filter(e->e.getTime().equals(measurementDataActual.getTime())).findFirst();
                                if(measurementDataPresumeOptional.isPresent()){
                                    presumeValue = measurementDataPresumeOptional.get().getValue();
                                }
                            }else{
                                if(StrUtil.isNotBlank(monitorModel.getPresumeValue())){
                                    presumeValue = Double.parseDouble(monitorModel.getPresumeValue());
                                }
                            }
                        }
                        //条件计算值
                        Double calculationValue = null;
                        switch (monitorModel.getResultType()){
                            case "1" :
                                //实际值
                                calculationValue = actualValue;
                                break;
                            case "2" :
                                //差
                                calculationValue = actualValue - presumeValue;
                                break;
                            case "3" :
                                //比值
                                calculationValue = (actualValue - presumeValue)/presumeValue;
                                break;
                            case "4" :
                                //偏差值
                                calculationValue = Math.abs(actualValue - presumeValue);
                                break;
                        }

                        if(Objects.isNull(retDataDouTemp)){
                            retDataDouTemp = calculationValue;
                        }

//                  1最大值，2最小值，3平均值，4第一个值，5最后一个值，6最大偏差实际值，7最小偏差实际值，8最大偏差时间，9最小偏差时间，10第一个值时间，11最后一个值时间
//                  12 第一个绿区值 13最后一个绿区值 14 绿区平均值
                        String color = getColorByPiData(calculateSignal,calculationValue,resultPiStandardMaintenance);
                        switch (color){
                            case "red" :
                                redNum ++;
                                break;
                            case "yellow" :
                                yellowNum ++;
                                break;
                            case "green" :
                                greenNum ++;
                                break;
                        }

                        switch (monitorModel.getResultModel()){
                            case "1" :
                                if(calculationValue > retDataDouTemp){
                                    retDataDouTemp = calculationValue;
                                }
                                retDataDou = String.valueOf(retDataDouTemp);
                                break;
                            case "2" :
                                if(calculationValue < retDataDouTemp){
                                    retDataDouTemp = calculationValue;
                                }
                                retDataDou = String.valueOf(retDataDouTemp);
                                break;
                            case "3" :
                                if(i != 0){
                                    retDataDouTemp = retDataDouTemp + calculationValue;
                                }
                                retDataDou = String.valueOf(retDataDouTemp);
                                break;
                            case "4" :
                                retDataDou = String.valueOf(retDataDouTemp);
                                break;
                            case "5" :
                                retDataDouTemp = calculationValue;
                                retDataDou = String.valueOf(retDataDouTemp);
                                break;
                            case "6" :
                                if (i == 0){
                                    retDataDouTemp = Math.abs(actualValue - presumeValue);
                                    retDataDou = String.valueOf(actualValue);
                                }else{
                                    if(retDataDouTemp < Math.abs(actualValue - presumeValue)){
                                        retDataDouTemp = Math.abs(actualValue - presumeValue);
                                        retDataDou = String.valueOf(actualValue);
                                    }
                                }
                                break;
                            case "7" :
                                if (i == 0){
                                    retDataDouTemp = Math.abs(actualValue - presumeValue);
                                    retDataDou = String.valueOf(actualValue);;
                                }else{
                                    if(retDataDouTemp > Math.abs(actualValue - presumeValue)){
                                        retDataDouTemp = Math.abs(actualValue - presumeValue);
                                        retDataDou = String.valueOf(actualValue);;
                                    }
                                }
                                break;
                            case "8" :
                                if (i == 0){
                                    retDataDouTemp = Math.abs(actualValue - presumeValue);
                                    // 将Instant转换为ZonedDateTime
                                    ZonedDateTime zonedDateTime = measurementDataActual.getTime().atZone(zoneId);
                                    retDataDou = zonedDateTime.format(formatter);
                                }else{
                                    if(retDataDouTemp < Math.abs(actualValue - presumeValue)){
                                        retDataDouTemp = Math.abs(actualValue - presumeValue);
                                        // 将Instant转换为ZonedDateTime
                                        ZonedDateTime zonedDateTime = measurementDataActual.getTime().atZone(zoneId);
                                        retDataDou = zonedDateTime.format(formatter);
                                    }
                                }
                                break;
                            case "9" :
                                if (i == 0){
                                    retDataDouTemp = Math.abs(actualValue - presumeValue);
                                    // 将Instant转换为ZonedDateTime
                                    ZonedDateTime zonedDateTime = measurementDataActual.getTime().atZone(zoneId);
                                    retDataDou = zonedDateTime.format(formatter);
                                }else{
                                    if(retDataDouTemp > Math.abs(actualValue - presumeValue)){
                                        retDataDouTemp = Math.abs(actualValue - presumeValue);
                                        // 将Instant转换为ZonedDateTime
                                        ZonedDateTime zonedDateTime = measurementDataActual.getTime().atZone(zoneId);
                                        retDataDou = zonedDateTime.format(formatter);
                                    }
                                }
                                break;
                            case "10" :
                                if(StrUtil.isBlank(retDataDou)){
                                    // 将Instant转换为ZonedDateTime
                                    ZonedDateTime zonedDateTime = measurementDataActual.getTime().atZone(zoneId);
                                    retDataDou = zonedDateTime.format(formatter);
                                }
                                break;
                            case "11" :
                                // 将Instant转换为ZonedDateTime
                                ZonedDateTime zonedDateTime = measurementDataActual.getTime().atZone(zoneId);
                                retDataDou = zonedDateTime.format(formatter);
                                break;
                            case "12" :
                                //取第一个绿区值
                                //获取PI颜色
                                if(StrUtil.isNotBlank(color) && color.equals("green") && greenNum == 1){
                                    retDataDouTemp = calculationValue;
                                    retDataDou = String.valueOf(retDataDouTemp);
                                }
                                break;
                            case "13" :
                                //取最后一个绿区值
                                if(StrUtil.isNotBlank(color) && color.equals("green")){
                                    retDataDouTemp = calculationValue;
                                    retDataDou = String.valueOf(retDataDouTemp);
                                }
                                break;
                            case "14" :
                                //取绿区平均值
                                if(StrUtil.isNotBlank(color) && color.equals("green")){
                                    if(greenNum == 1){
                                        retDataDouTemp = calculationValue;
                                    }else{
                                        retDataDouTemp = retDataDouTemp + calculationValue;
                                    }
                                    retDataDou = String.valueOf(retDataDouTemp);
                                }
                                break;
                            //黄区偏离最大实际值
                            case "15" :
                                if(StrUtil.isNotBlank(color) && color.equals("yellow")){
                                    if (yellowNum == 1){
                                        retDataDouTemp = Math.abs(actualValue - presumeValue);
                                        retDataDou = String.valueOf(actualValue);
                                    }else{
                                        if(retDataDouTemp < Math.abs(actualValue - presumeValue)){
                                            retDataDouTemp = Math.abs(actualValue - presumeValue);
                                            retDataDou = String.valueOf(actualValue);
                                        }
                                    }
                                }
                                break;
                            //红区偏离最大实际值
                            case "16" :
                                if(StrUtil.isNotBlank(color) && color.equals("red")){
                                    if (redNum == 0){
                                        retDataDouTemp = Math.abs(actualValue - presumeValue);
                                        retDataDou = String.valueOf(actualValue);
                                    }else{
                                        if(retDataDouTemp < Math.abs(actualValue - presumeValue)){
                                            retDataDouTemp = Math.abs(actualValue - presumeValue);
                                            retDataDou = String.valueOf(actualValue);
                                        }
                                    }
                                }
                                break;
                        }
                    }
                    //平均值需要单独做个处理(平均值或者绿区平均值)
                    if("3".equals(monitorModel.getResultModel())){
                        if(actualList.size() > 1){
                            retDataDou = String.valueOf(retDataDouTemp / actualList.size());
                        }
                    }
                    if("14".equals(monitorModel.getResultModel()) ){
                        if(greenNum > 1){
                            retDataDou = String.valueOf(retDataDouTemp / greenNum);
                        }
                    }
                }else{
                    //单独新增的4个算法，17取第一个符合条件的值，18取第一个符合条件的值的时间，19取最后一个符合条件的值，20取最后一个符合条件的值时间
                    List<MeasurementData> dataList = new ArrayList<>();
                    if("1".equals(monitorModel.getCalculationMethod())){
                        dataList = consecutiveDataList;
                    }else{
                        dataList = countDataList;
                    }

                    switch (monitorModel.getResultModel()) {
                        case "17":
                            retDataDou = String.valueOf(dataList.get(0).getValue());
                            break;
                        case "18":
                            // 将Instant转换为ZonedDateTime
                            ZonedDateTime zonedDateTime = dataList.get(0).getTime().atZone(zoneId);
                            retDataDou = zonedDateTime.format(formatter);
                            break;
                        case "19":
                            retDataDou = String.valueOf(dataList.get(dataList.size()-1).getValue());
                            break;
                        case "20":
                            // 将Instant转换为ZonedDateTime
                            zonedDateTime = dataList.get(dataList.size()-1).getTime().atZone(zoneId);
                            retDataDou = zonedDateTime.format(formatter);
                            break;
                    }
                }
                retData = retDataDou;
            }
        }
        return changeDataFormat(retData);
    }

    /**
     * 计算（获取瞬时数据）
     * @param tagCodes 基础点位集合
     * @param time 时间
     * @param type 类型，up向上、down向下、now现在
     * @return
     */
    @Override
    public List<MeasurementData> influxDbMoment(List<String> tagCodes, LocalDateTime time, String type){
        //传入对应的参数，从influxDB中进行计算
        QueryCondition queryCondition = new QueryCondition();
        List<QueryTagCondition> list = new ArrayList<>();
        tagCodes = tagCodes.stream().distinct().collect(Collectors.toList());
        for(String tagCode : tagCodes){
            QueryTagCondition queryTagCondition = new QueryTagCondition();
            queryTagCondition.setTagId(tagCode);
            list.add(queryTagCondition);
        }
        queryCondition.setQueryTagConditionList(list);
        queryCondition.setEnd(time);
        queryCondition.setQueryType(QueryType.LAST);
//        switch (type){
//            case "up":
//            case "now":
//            default:
//                queryCondition.setStart(time);
//                queryCondition.setQueryType(QueryType.FIRST);
//                break;
//            case "down":
//                queryCondition.setEnd(time);
//                queryCondition.setQueryType(QueryType.LAST);
//                break;
//        }
        List<MeasurementData> listData = businessHandler.upOrDownNearestValue(queryCondition);

        return listData;
    }

    /**
     * 判断字符串是否是时间格式
     * @param timeStr
     * @param pattern
     * @return
     */
    public boolean isValidTimeFormat(String timeStr, String pattern) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);
        try {
            LocalTime.parse(timeStr, formatter);
            return true;
        } catch (DateTimeParseException e) {
            return false;
        }
    }

    /**
     * 判断条件是否正确
     * @param calculationValue 实际值
     * @param deviationValue 标准值
     * @param type 比较方法 1>,2>=,3=,4!=,5<,6<=
     * @return
     */
    public static boolean isCorrectCalculate(double calculationValue, double deviationValue, String type){
        boolean isCorrect = false;
        switch (type){
            case "1" :
                if(calculationValue > deviationValue){
                    isCorrect = true;
                }
                break;
            case "2" :
                if(calculationValue >= deviationValue){
                    isCorrect = true;
                }
                break;
            case "3" :
                if(calculationValue == deviationValue){
                    isCorrect = true;
                }
                break;
            case "4" :
                if(calculationValue != deviationValue){
                    isCorrect = true;
                }
                break;
            case "5" :
                if(calculationValue < deviationValue){
                    isCorrect = true;
                }
                break;
            case "6" :
                if(calculationValue <= deviationValue){
                    isCorrect = true;
                }
                break;
        }
        return isCorrect;
    }


    /**
     * 取两个时间集合中所有的交集
     * @param startIntervals 开始集合
     * @param endIntervals 需要比对的集合
     * @return
     */
    @Override
    public List<Pair<LocalDateTime,LocalDateTime>> getIntersectionFromTimeList(List<Pair<LocalDateTime, LocalDateTime>> startIntervals, List<Pair<LocalDateTime, LocalDateTime>> endIntervals){
        // 按起始时间排序时间段列表
        Collections.sort(startIntervals, Comparator.comparing(interval -> interval.getKey()));
        Collections.sort(endIntervals, Comparator.comparing(interval -> interval.getKey()));
        //用于返回
        List<Pair<LocalDateTime,LocalDateTime>> listTime = new ArrayList<>();

        for(Pair<LocalDateTime, LocalDateTime> startPair : startIntervals){
            for(Pair<LocalDateTime, LocalDateTime> endPair : endIntervals){
                // 计算当前时间段与交集的交集
                LocalDateTime newStart = startPair.getKey().isAfter(endPair.getKey()) ? startPair.getKey() : endPair.getKey();
                LocalDateTime newEnd = startPair.getValue().isBefore(endPair.getValue()) ? startPair.getValue() : endPair.getValue();
                // 检查交集是否为空
                if (newStart.isBefore(newEnd)) {
                    listTime.add(Pair.of(newStart,newEnd));
                }
            }
        }
        return listTime;
    }

    /**
     * 合并重叠的时间区间
     * @param timePairs 时间区间列表
     * @return 合并后的时间区间列表
     */
    @Override
    public List<Pair<LocalDateTime, LocalDateTime>> mergeOverlappingTimePairs(List<Pair<LocalDateTime, LocalDateTime>> timePairs) {
        if (timePairs == null || timePairs.size() <= 1) {
            return timePairs;
        }

        // 先按开始时间排序
        List<Pair<LocalDateTime, LocalDateTime>> sortedPairs = new ArrayList<>(timePairs);
        sortedPairs.sort(Comparator.comparing(Pair::getKey));

        List<Pair<LocalDateTime, LocalDateTime>> mergedPairs = new ArrayList<>();
        // 初始化第一个区间
        Pair<LocalDateTime, LocalDateTime> currentPair = sortedPairs.get(0);
        LocalDateTime currentStart = currentPair.getKey();
        LocalDateTime currentEnd = currentPair.getValue();

        // 遍历其余区间
        for (int i = 1; i < sortedPairs.size(); i++) {
            Pair<LocalDateTime, LocalDateTime> pair = sortedPairs.get(i);
            LocalDateTime nextStart = pair.getKey();
            LocalDateTime nextEnd = pair.getValue();

            // 如果当前区间的结束时间大于等于下一个区间的开始时间，则合并
            // 这里使用 isAfter 或 isEqual 来比较时间
            if (currentEnd.isAfter(nextStart) || currentEnd.isEqual(nextStart)) {
                // 更新当前区间的结束时间为两个区间结束时间的最大值
                if (nextEnd.isAfter(currentEnd)) {
                    currentEnd = nextEnd;
                }
            } else {
                // 否则，将当前区间加入结果集，并更新当前区间为下一个区间
                mergedPairs.add(Pair.of(currentStart, currentEnd));
                currentStart = nextStart;
                currentEnd = nextEnd;
            }
        }

        // 将最后一个区间加入结果集
        mergedPairs.add(Pair.of(currentStart, currentEnd));

        return mergedPairs;
    }


    /**
     * 根据计算信号获取对应PI配置的红黄区标准
     * @param calculateSignal 计算信号，主要获取唯一约束信息
     * @param piCode 需要查找那个PI的红黄区标准
     * @param dataId PI报表中数据的主键
     * @return
     */
    private PiStandardMaintenance getStandarValueBySignal(PiCalculateSignal calculateSignal,String piCode,String dataId){
        PiStandardMaintenance piStandardMaintenance = null;
        try {
            if(StrUtil.isBlank(dataId)){
                //获取唯一约束的值
                List<TableConditionDto> conditionList = new ArrayList<>();
                conditionList = getConditionList(calculateSignal);

                //不覆盖，判断是否在数据库中存在
                TableQueryDto tableQueryDto = new TableQueryDto();
                tableQueryDto.setTable(calculateSignal.getPiTable());
                tableQueryDto.setConditionList(conditionList);
                List<String> fieldStrList = Arrays.asList("id",piCode);
                tableQueryDto.setFieldList(fieldStrList);
                List<Map<String,Object>> tableData = piCustomizeTableService.getTableData(tableQueryDto);
                if(null == tableData || tableData.size() != 1){
                    return piStandardMaintenance;
                }
                dataId = tableData.get(0).get("id").toString();
            }

            //数据查询出来后，查找对应的红黄区标准
            LambdaQueryWrapper<PiStandardData> queryWrapper = Wrappers.lambdaQuery(PiStandardData.class);
            queryWrapper.eq(PiStandardData::getTableName,calculateSignal.getPiTable());
            queryWrapper.eq(PiStandardData::getDataId,dataId);
            List<PiStandardData> piStandardDataList = piStandardDataService.list(queryWrapper);
            if(null == piStandardDataList || piStandardDataList.size() != 1){
                return piStandardMaintenance;
            }
            //标准ID
            String standardJson = piStandardDataList.get(0).getStandardJson();
            List<Map> listMapHave = JSONUtil.toList(standardJson, Map.class);

            //获取当前PI的红黄区标准
            Optional<Map> fieldMapOptional = listMapHave.stream().filter(e-> null != e.get("field") && e.get("field").equals(piCode)).findFirst();
            String maintenanceId = "";
            if(fieldMapOptional.isPresent() && null != fieldMapOptional.get().get("maintenance_id") && !"".equals(fieldMapOptional.get().get("maintenance_id"))){
                maintenanceId = fieldMapOptional.get().get("maintenance_id").toString();
            }

            List<PiStandardMaintenance> piStandardMaintenanceList = new ArrayList<>();
            if(StrUtil.isNotBlank(maintenanceId)){
                //数据查询出来后，查找对应的红黄区标准
                LambdaQueryWrapper<PiStandardMaintenance> queryWrapperMaintenance = Wrappers.lambdaQuery(PiStandardMaintenance.class);
                queryWrapperMaintenance.eq(PiStandardMaintenance::getId,maintenanceId);
                piStandardMaintenanceList = piStandardMaintenanceService.list(queryWrapperMaintenance);
            }

            if(null == piStandardMaintenanceList || piStandardMaintenanceList.size() != 1){
                return piStandardMaintenance;
            }
            piStandardMaintenance = piStandardMaintenanceList.get(0);
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            return piStandardMaintenance;
        }
    }


    /**
     * 获取最近一条数据的PI值（跨行算法）
     * @param computationModel 计算模型
     * @param calculateSignal 计算信号（）
     * @param nearFields 邻近数据字段数
     * @param piDatas 当前PI行数据
     * @return
     */
    private Map<String,Object> getNearValueByComputationModel(PiComputationModel computationModel,PiCalculateSignal calculateSignal,List<String> nearFields,Map<String,Object> piDatas){
        Map<String,Object> nearValue = new HashMap<>();

        if(Objects.isNull(piDatas.get(computationModel.getSortField()))){
            return nearValue;
        }
        //排序字段的值
        String sortFieldValue = piDatas.get(computationModel.getSortField()).toString();
        try{
            //定位当前行，查找当前行的数据
            //判断值是否是时间格式
            //判断字符串是否是时间格式，如果是时间格式，转换成long时间戳
            //用于时间字符串判断
            if(
                    isValidTimeFormat(sortFieldValue, "yyyy-MM-dd HH:mm:ss")  ||
                    isValidTimeFormat(sortFieldValue, "yyyy-MM-dd'T'HH:mm:ss") ||
                    isValidTimeFormat(sortFieldValue, "yyyy-MM-dd HH:mm") ||
                    isValidTimeFormat(sortFieldValue, "yyyy-MM-dd'T'HH:mm") ||
                    isValidTimeFormat(sortFieldValue, "yyyy-MM-dd")

            ){
                if(sortFieldValue.contains("T")){
                    sortFieldValue = sortFieldValue.replace("T"," ");
                }
                if(sortFieldValue.length() == 16){
                    sortFieldValue = sortFieldValue + ":00";
                }
            }
            // 设置查询条件
            String query = computationModel.getSortField() + " < '"+sortFieldValue+" '";

            //过滤字段
            if(StrUtil.isNotBlank(computationModel.getFilterField())){
                String[] filterFields = computationModel.getFilterField().split(",");
                for(String filterField : filterFields){
                    if(null != piDatas.get(filterField)){
                        query = query + " and " + filterField + " = " + piDatas.get(filterField);
                    }
                }
            }

            //设置查询字段，默认查询全部
            TableConditionQueryDto tableConditionQueryDto = new TableConditionQueryDto();
            tableConditionQueryDto.setFieldList(nearFields);
            tableConditionQueryDto.setTable(calculateSignal.getPiTable());
            tableConditionQueryDto.setCondition(query);
            //需要补充排序字段和限制条数
            tableConditionQueryDto.setSortField(computationModel.getSortField());
            tableConditionQueryDto.setSortMode(computationModel.getSortMode());
            tableConditionQueryDto.setRowsNumber(1);

            //当前表中所有的符合要求的数据
            List<Map<String,Object>> tableData = piCustomizeTableService.getTableByCondition(tableConditionQueryDto);

            if(null != tableData && tableData.size() == 1){
                nearValue = tableData.get(0);
            }

        }catch (Exception e){
            e.printStackTrace();
        }finally {
            return nearValue;
        }
    }


    /**
     * PI日报间的计算（自动/手动算法触发计算）
     * @param dataId 数据ID
     * @param piCode 触发PI编码
     * @param time 触发时间
     */
    @Override
    public void arithmeticByPi(String dataId,String piTable,String piCode,LocalDateTime time,int calculateBj){

        //执行保存操作，返回保存数据的ID
        String retId = "-1";
        //查询当前piTable涉及到的所有触发信号
        List<PiCalculateSignal> piCalculateSignalListAll = piCalculateSignalService.lambdaQuery()
                .eq(PiCalculateSignal::getPiTable,piTable)
                .eq(PiCalculateSignal::getIsEnabled,1)
                .list();
        if (null == piCalculateSignalListAll || piCalculateSignalListAll.size() == 0){
            return;
        }
        List<String> calculateIds = piCalculateSignalListAll.stream().map(PiCalculateSignal::getId).collect(Collectors.toList());

        List<PiComputationModel> piComputationModelListAll = lambdaQuery().in(PiComputationModel::getCalculateId,calculateIds).list();

        if(null == piComputationModelListAll || piComputationModelListAll.size() == 0){
            return;
        }

        //查询涉及到piCode的计算模型
        List<PiComputationModel> piComputationModelList = piComputationModelListAll.stream().filter(entity -> StrUtil.isNotBlank(entity.getFormulaCode()) && entity.getFormulaCode().contains(piCode)).collect(Collectors.toList());

        if(null == piComputationModelList || piComputationModelList.size() == 0){
            return;
        }
        //查询所有的触发信号，用于查找当前需要计算PI的信息
        calculateIds = piComputationModelList.stream().map(PiComputationModel::getCalculateId).distinct().collect(Collectors.toList());

        //查找对应模型的PI信息
        List<String> finalCalculateIds = calculateIds;
        List<PiCalculateSignal> piCalculateSignalList = piCalculateSignalListAll.stream().filter(entity -> finalCalculateIds.contains(entity.getId())).collect(Collectors.toList());

        //收集PI的集合
        List<String> piCodes = Optional.ofNullable(piCalculateSignalList)
                .orElse(Collections.emptyList())
                .stream()
                .map(PiCalculateSignal::getPiCode)
                .filter(Objects::nonNull)  // 过滤空值
                .distinct()
                .collect(Collectors.toList());

        //循环需要计算的PI
        for(String signalPiCode : piCodes){
            //获取触发信号
            List<PiCalculateSignal>  signalList = piCalculateSignalList.stream().filter(entity ->entity.getPiCode().equals(signalPiCode)).collect(Collectors.toList());
            for (PiCalculateSignal signal : signalList){
                //获取计算模型
                List<PiComputationModel> modelList = piComputationModelList.stream().filter(entity ->entity.getCalculateId().equals(signal.getId())).collect(Collectors.toList());
                if(null != modelList && modelList.size() > 0){
                    //开始计算
                    retId = piModelCalculate(dataId,modelList.get(0),signal,calculateBj,time);

                    //正常更新数据
                    if(StrUtil.isNotBlank(retId) && !"-1".equals(retId)){
                        //判断当前PI是否参与了其他PI的计算，如果参与，则需要继续执行当前方法
                        //查询涉及到piCode的计算模型
                        List<PiComputationModel> modelListLast = piComputationModelListAll.stream().filter(modelEntity -> StrUtil.isNotBlank(modelEntity.getFormulaCode()) && modelEntity.getFormulaCode().contains(signal.getPiCode())).collect(Collectors.toList());
                        if(null != modelListLast && modelListLast.size() > 0){
                            //如果当前计算的PI涉及到另外的PI，则继续执行当前方法
                            arithmeticByPi(dataId,signal.getPiTable(),signal.getPiCode(),time,signal.getReCalculateBj());
                        }
                        break;
                    }
                }
            }
        }
    }

    /**
     * PI日报间计算的计算模型
     * @param model 需要计算的PI的计算模型
     * @param signal 需要计算的PI的计算信号
     * @param calculateBj 计算标识
     * @param time 计算时间
     * @return
     */
    public String piModelCalculate(String dataId,PiComputationModel model, PiCalculateSignal signal,int calculateBj,LocalDateTime time){
        String retId = "-1";
        try {
            signal.setPiComputationModelList(Arrays.asList(model));
            signal.setReCalculateBj(calculateBj);
            signal.setCalculateTime(time);

            //计算的结果数据
            String retData = "";

            String formulaCode = model.getFormulaCode();

            if(StrUtil.isNotBlank(model.getAdvanceTime()) && !"0".equals(model.getAdvanceTime())){
                if("0".equals(model.getAdvanceDelay())){
                    time = time.minusSeconds(Long.parseLong(model.getAdvanceTime()));
                }else{
                    time = time.plusSeconds(Long.parseLong(model.getAdvanceTime()));
                }
            }

            //时间模型，不参与四则运算
            if(formulaCode.contains("timeFun()")){
                LocalDateTime resultTime = time;
                // 支持 timeFun() + 30 或 timeFun() - 30 格式
                java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("timeFun\\(\\s*\\)\\s*([+-])\\s*(\\d+)");
                java.util.regex.Matcher matcher = pattern.matcher(formulaCode);
                while (matcher.find()) {
                    String operator = matcher.group(1);
                    long seconds = Long.parseLong(matcher.group(2));
                    if ("+".equals(operator)) {
                        resultTime = resultTime.plusSeconds(seconds);
                    } else if ("-".equals(operator)) {
                        resultTime = resultTime.minusSeconds(seconds);
                    }
                }
                // 创建一个日期时间格式化器
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                // 使用格式化器格式化时间
                retData = resultTime.format(formatter);
                List<TableConditionDto> conditionList = new ArrayList<>();
                TableConditionDto tableConditionDto = new TableConditionDto();
                tableConditionDto.setKey("id");
                tableConditionDto.setVal(dataId);
                conditionList.add(tableConditionDto);
                retId = saveData(retData, signal, conditionList);
                return retId;
            }

            //日期模型，不参与四则运算
            if(formulaCode.contains("dateFun()")){
                // 创建一个日期时间格式化器
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                // 使用格式化器格式化当前时间
                retData = time.format(formatter);
                List<TableConditionDto> conditionList = new ArrayList<>();
                TableConditionDto tableConditionDto = new TableConditionDto();
                tableConditionDto.setKey("id");
                tableConditionDto.setVal(dataId);
                conditionList.add(tableConditionDto);
                retId = saveData(retData, signal, conditionList);
                return retId;
            }

            //获取所有的计算模型中的PI
            List<String> evenIndexNumbers = getListPiCodeByFormulaCode(formulaCode);

            //将主键查出来
            evenIndexNumbers.add("id");
            //将排序字段也查询出来
            if(StrUtil.isNotBlank(model.getSortField())){
                evenIndexNumbers.add(model.getSortField());
            }
            //将限制字段也查询出来
            if(StrUtil.isNotBlank(model.getFilterField())){
                evenIndexNumbers.addAll(Arrays.stream(model.getFilterField().split(",")).toList());
            }
            //去重
            evenIndexNumbers = evenIndexNumbers.stream().distinct().collect(Collectors.toList());

            //PI日报间不存在函数计算
            if(formulaCode.contains("maxFun") || formulaCode.contains("minFun") || formulaCode.contains("averageFun")
                    || formulaCode.contains("sumFun") || formulaCode.contains("firstFun") || formulaCode.contains("lastFun")
                    || formulaCode.contains("firstTimeFun") || formulaCode.contains("lastTimeFun")
                    || formulaCode.contains("timesFun") || formulaCode.contains("timeSecondFun")){
                return retId;
            }

            String query = " id = '" + dataId + "' ";

            TableConditionQueryDto tableConditionQueryDto = new TableConditionQueryDto();
            //默认查询所有的
            tableConditionQueryDto.setFieldList(evenIndexNumbers);

            tableConditionQueryDto.setTable(signal.getPiTable());
            tableConditionQueryDto.setCondition(query);
            //当前表中所有的符合要求的数据
            List<Map<String,Object>> piData = piCustomizeTableService.getTableByCondition(tableConditionQueryDto);

            //如果存在修改或者剔除的数据，直接跳过，不做计算
            if(null != piData && piData.size() == 1 ){
                //所有数据的主键集合
                List<String> piDataIds = piData.stream().map(map -> map.get("id").toString()).collect(Collectors.toList());
                //查询修改、剔除的数据
                LambdaQueryWrapper<PiApprove> queryWrapper = Wrappers.lambdaQuery(PiApprove.class);
                queryWrapper.eq(PiApprove::getTableName,signal.getPiTable());
                queryWrapper.eq(PiApprove::getState,"1");
                queryWrapper.like(PiApprove::getFields,signal.getPiCode());
                queryWrapper.in(PiApprove::getDataId,piDataIds);
                //已经剔除和修改的数据
                List<PiApprove> piApproveDataList = piApproveService.list(queryWrapper);
                if(null != piApproveDataList && piApproveDataList.size() > 0){
                    //将当前PI已经剔除或者修改的数据ID过滤出来
                    List<String> delIds = piApproveDataList.stream()
                            .map(PiApprove::getDataId)
                            .distinct()
                            .collect(Collectors.toList());
                    piData = piData.stream().filter(e -> !delIds.contains(e.get("id").toString())).collect(Collectors.toList());
                }
                //如果计算公式中包含了跨行计算算法，先计算出临近一行的数据
                Map<String,Object> nearData = new HashMap<>();
                if(formulaCode.contains("nearFun(")){
                    Object sortFieldValue = piData.get(0).get(model.getSortField());
                    if(null != sortFieldValue && !"".equals(sortFieldValue)){
                        nearData = getNearValueByComputationModel(model,signal,evenIndexNumbers,piData.get(0));
                    }
                }
                //获取当前行数据
                Map<String,Object> map = piData.get(0);

                //单独处理函数，多个PI，取第一个不为空的PI的值，函数格式：firstNotNullFun(⊙sdfasdfasd⊙⊙sdfasdfasd⊙⊙sdfasdfasd⊙...)
                if(formulaCode.contains("firstNotNullFun(")){
                    //截取firstNotNullFun中的值
                    List<String> listPiCode = getListPiCodeByFunString(formulaCode,"firstNotNullFun");
                    for(String piCodes : listPiCode){
                        //定义第一个不为空的值
                        String valueString = "";
                        List<String> indexPi = getListPiCodeByFormulaCode(piCodes);
                        for(String piCode : indexPi){
                            if(null != map.get(piCode) && !"".equals(map.get(piCode))){
                                valueString = dateTimeToLongByResult(map.get(piCode).toString());
                                break;
                            }
                        }
                        String funString = "firstNotNullFun(" + piCodes + ")";
                        //计算公式中存在为空的，直接返回全部为空
                        if(StrUtil.isBlank(valueString)){
                            formulaCode = "";
                        }else{
                            formulaCode = formulaCode.replace(funString,valueString);
                        }
                    }
                }

                //绝对值 absFun(⊙sdfasdfasd⊙)
                if(formulaCode.contains("absFun(")){
                    List<String> listPiCode = getListPiCodeByFunString(formulaCode,"absFun");
                    for(String piCodes : listPiCode){
                        String valueString = "";
                        List<String> indexPi = getListPiCodeByFormulaCode(piCodes);
                        for(String piCode : indexPi){
                            if(null != map.get(piCode) && !"".equals(map.get(piCode))){
                                valueString = dateTimeToLongByResult(map.get(piCode).toString());
                                break;
                            }
                        }
                        String funString = "absFun(" + piCodes + ")";
                        if(StrUtil.isNotBlank(valueString)){
                            valueString = Math.abs(Double.parseDouble(valueString))+"";
                            formulaCode = formulaCode.replace(funString,valueString);
                        }else{
                            //计算公式中存在为空的，直接跳过计算，返回
                            formulaCode = "";
                        }
                    }
                }

                //标准值 standardFun(⊙sdfasdfasd⊙)
                if(formulaCode.contains("standardFun(")){
                    List<String> listPiCode = getListPiCodeByFunString(formulaCode,"standardFun");
                    for(String piCodes : listPiCode){
                        List<String> indexPi = getListPiCodeByFormulaCode(piCodes);
                        for(String piCode : indexPi){
                            String valueString = "";
                            if(StrUtil.isNotBlank(dataId)){
                                //查询标准值
                                PiStandardMaintenance resultPiStandardMaintenance = getStandarValueBySignal(signal,piCode,dataId);
                                if(null != resultPiStandardMaintenance && null != resultPiStandardMaintenance.getStandardValue()){
                                    valueString = null != resultPiStandardMaintenance.getStandardValue() ? resultPiStandardMaintenance.getStandardValue().toString() : "";
                                }
                            }
                            String funString = "standardFun(" + piCodes + ")";
                            valueString = dateTimeToLongByResult(valueString);
                            //计算公式中存在为空的，直接跳过计算，返回
                            if(StrUtil.isBlank(valueString)){
                                formulaCode = "";
                            }else{
                                formulaCode = formulaCode.replace(funString,valueString);
                            }
                        }
                    }
                }

                //临近算法
                if(formulaCode.contains("nearFun(")){
                    List<String> listPiCode = getListPiCodeByFunString(formulaCode,"nearFun");
                    for(String piCodes : listPiCode){
                        List<String> indexPi = getListPiCodeByFormulaCode(piCodes);
                        for(String piCode : indexPi){
                            String keyValue = "";
                            if(null != nearData.get(piCode) && !"".equals(nearData.get(piCode))){
                                keyValue = dateTimeToLongByResult(nearData.get(piCode).toString());
                            }
                            String funString = "nearFun(" + piCodes + ")";
                            //计算公式中存在为空的，直接跳过计算，返回
                            if(StrUtil.isBlank(keyValue)){
                                formulaCode = "";
                            }else{
                                formulaCode = formulaCode.replace(funString,keyValue);
                            }
                        }
                    }
                }

                //为空给0 ifNullFun(⊙sdfasdfasd⊙)
                if(formulaCode.contains("ifNullFun(")){
                    List<String> listPiCode = getListPiCodeByFunString(formulaCode,"ifNullFun");
                    for(String piCodes : listPiCode){
                        List<String> indexPi = getListPiCodeByFormulaCode(piCodes);
                        for(String piCode : indexPi){
                            String result = dateTimeToLongByResult(null != map.get(piCode) && !"".equals(map.get(piCode)) ? map.get(piCode).toString() : "");
                            if(StringUtils.isBlank(result)){
                                result = "0";
                            }
                            String funString = "ifNullFun(" + piCodes + ")";
                            formulaCode = formulaCode.replace(funString,result);
                        }
                    }
                }

                //所有函数均替换完成后，直接查看是否存在单独的PI
                List<String> indexPi = getListPiCodeByFormulaCode(formulaCode);
                for(String piCode : indexPi){
                    String resultStr = "";
                    if(!Objects.isNull( map.get(piCode))){
                        //判断值是否是时间格式
                        //判断字符串是否是时间格式，如果是时间格式，转换成long时间戳
                        //用于时间字符串判断
                        resultStr = dateTimeToLongByResult(map.get(piCode).toString());
                    }
                    String funString = "⊙" + piCode + "⊙";
                    //计算公式中存在为空的，直接跳过计算，返回
                    if(StrUtil.isBlank(resultStr)){
                        formulaCode = "";
                    }else{
                        formulaCode = formulaCode.replace(funString,resultStr);
                    }
                }

                //替换完成后，开始进行四则运算
                //计算公式是否包含 + - * / （ ）
                if(formulaCode.contains("+") || formulaCode.contains("-") || formulaCode.contains("/")
                        || formulaCode.contains("*") || formulaCode.contains("(")
                        || formulaCode.contains(")"))
                {
                    String [] forMulacodeList = formulaCode.split("-");
                    Object result = null;
                    //特殊判断，将单独的减法进行单独处理，防止时间戳之间的相减溢出
                    if(forMulacodeList.length == 2 && forMulacodeList[0].length() == 13 && forMulacodeList[1].length() == 13){
                        result = Long.parseLong(forMulacodeList[0]) - Long.parseLong(forMulacodeList[1]);
                    }else{
                        //避免计算结果是整数，将整数部分默认加上.0
                        formulaCode = formulaCode.replaceAll("(?<!\\.)\\b(\\d+)\\b(?![\\.\\d])", "$1.0");
                        try {
                            JexlEngine jexlEngine = new JexlEngine();
                            result = jexlEngine.eval(formulaCode, null);
                        }catch (JexlException e) {
                            String msg = e.getMessage();
                            if (msg != null && (msg.contains("divide") || msg.contains("division"))) {
                                logger.warn("JEXL除零异常, formulaCode={}", formulaCode);
                                result = "0";
                            } else {
                                logger.warn("JEXL解析异常, formulaCode={}", formulaCode, e);
                                if (hasChineseOrEnglish(formulaCode)) {
                                    result = formulaCode;
                                } else {
                                    result = "0";
                                }
                            }
                        } catch (Exception e) {
                            logger.warn("JEXL计算异常, formulaCode={}", formulaCode, e);
                            if(hasChineseOrEnglish(formulaCode)){
                                result = formulaCode;
                            }else{
                                result = "";
                            }
                        }
                    }
                    if(!Objects.isNull(result)){
                        retData = changeDataFormat(result.toString());
                    }
                }else{
                    retData = changeDataFormat(formulaCode);
                }
//                //结果为空，不执行保存
//                if(StrUtil.isBlank(retData)){
//                    return  retId;
//                }
                //判断时间类型计算，需要选择结果类型，结果类型存在，就直接对最终结果进行转换
                if(StrUtil.isNotBlank(model.getResultType()) && StringUtils.isNotBlank(retData)){
                    String resultType = model.getResultType();
                    switch (resultType){
                        //天
                        case "1" :
                            retData = Long.parseLong(retData) / 86400000.00 + "";
                            break;
                        //小时
                        case "2" :
                            retData = Long.parseLong(retData) / 3600000.00 + "";
                            break;
                        //分钟
                        case "3" :
                            retData = Long.parseLong(retData) / 60000.00 + "";
                            break;
                        //秒
                        case "4" :
                            retData = Long.parseLong(retData) / 1000.00 + "";
                            break;
                        default:
                    }
                }
                //计算出数据后，将数据更新到数据表中
                //适应接口传参，new一个空
                //唯一约束是主键
                List<TableConditionDto> conditionList = new ArrayList<>();
                TableConditionDto tableConditionDto = new TableConditionDto();
                tableConditionDto.setKey("id");
                tableConditionDto.setVal(map.get("id").toString());
                conditionList.add(tableConditionDto);
                retId = saveData(retData,signal,conditionList);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return retId;
    }



    /**
     * 根据PI编码的实际值，获取当前PI的颜色
     * @param calculateSignal 计算信号
     * @param calculationValue
     * @param resultPiStandardMaintenance
     * @return
     */
    public String getColorByPiData(PiCalculateSignal calculateSignal,Double calculationValue,PiStandardMaintenance resultPiStandardMaintenance){
        String color = "";
        String piCode = calculateSignal.getPiCode();
        Map<String,Object> data = new HashMap<>();
        data.put(piCode,calculationValue);
        if(null != resultPiStandardMaintenance) {
            List<PiStandardMaintenance> list = new ArrayList<>();
            list.add(resultPiStandardMaintenance);
            //查询字段的红黄区
            String jsonColor = piStandardDataService.getRedYellowJson(data, list,calculateSignal.getCustomizeTableFieldList());
            if (StrUtil.isNotBlank(jsonColor)) {
                List<Map> listMapColor = JSONUtil.toList(jsonColor, Map.class);
                //判断红黄区
                Optional<Map> mapOptional = listMapColor.stream().filter(e -> e.get("field").equals(piCode)).findFirst();
                if (mapOptional.isPresent()) {
                    if (!Objects.isNull(mapOptional.get().get("color")) && !"".equals(mapOptional.get().get("color").toString())) {
                        color = mapOptional.get().get("color").toString();
                    }
                }
            }
        }
        return color;
    }

    /**
     * 自动化系统点位中存在字符串，底层将字符串按照编码格式转换成double存储，平台需要将double数据转换回string
     * @param cnlVal
     * @return
     */
    public String retStrByInfluxDouble(String cnlNum,Double cnlVal){
        // 将 double 转换为 BigDecimal，注意这里可能会丢失精度，因为 double 是浮点数
        // 为了避免精度丢失，最好从 String 构造 BigDecimal
        BigDecimal bd = new BigDecimal(Double.toString(cnlVal));
        // 使用 toPlainString() 方法获取不带科学计数法的纯数字字符串
        String result = bd.toPlainString();

        //查询是否需要做数字转换，在基础点位配置中，数据类型配置成String或者String12等，均需要进行转换
        List<PiBasePoint> listPoints =  piBasePointService.lambdaQuery()
                .eq(PiBasePoint::getPointCode,cnlNum)
                .list();

        //由于考虑到基础点位中点位可能存在重复，只要任意一个点位的数据类型配置了string，都进行转换
        long count = listPoints.stream().filter(e->
                StrUtil.isNotBlank(e.getDataType())
                        && e.getDataType().toLowerCase().contains("string")
        ).count();
        if(count > 0){
            //长度大于20位，按照汉字转换成ByteBuffer的方式转换
            if(result.length() > 20){
                // 将 int 类型的值转换为字节数组
                ByteBuffer buffer = ByteBuffer.allocate(8).order(ByteOrder.LITTLE_ENDIAN).putDouble(cnlVal);
                byte[] bytes = buffer.array();
                // 使用 UTF-16 编码将字节数组转换为字符串
                result = new String(bytes, Charset.forName("UTF-16LE")).trim();
            }else{
                int hexInt = (int) cnlVal.doubleValue();
                result = hexToAscii(hexInt);
            }
        }
        return result;
    }

    /**
     * 判断是否是科学计数法或者数字
     * @param input
     * @return
     */
    public static boolean isScientificNotation(String input) {
        if (input == null || input.isEmpty()) {
            return false;
        }
        String SCIENTIFIC_NOTATION_PATTERN =
                "^[-+]?(\\d+(\\.\\d*)?|\\.\\d+)([eE][-+]?\\d+)?$";
        // 利用正则表达式匹配科学计数法表示的数值
        return input.matches(SCIENTIFIC_NOTATION_PATTERN);
    }

    /**
     * 判断字符串中是否含有汉字或者字符串
     * @param input
     * @return
     */
    public static boolean hasChineseOrEnglish(String input) {
        if (input == null || input.isEmpty()) {
            return false;
        }
        // 匹配汉字、字母、或 + - * /
        Pattern pattern = Pattern.compile("[\\u4e00-\\u9fff]|[a-zA-Z]");
        return pattern.matcher(input).find();
    }

    /**
     * 将10进制字符串转换为ASCII字符串
     *
     * @param hexInt 10进制数值（不包含"0x"前缀）
     * @return ASCII字符串
     */
    public static String hexToAscii(int hexInt) {
        // 将十进制数字转换为十六进制字符串（不带"0x"前缀）
        String hexString = Integer.toHexString(hexInt).toUpperCase();

        StringBuilder output = new StringBuilder("");
        for (int i = 0; i < hexString.length(); i += 2) {
            // 每两个字符表示一个字节
            String str = hexString.substring(i, i + 2);
            if(!str.equals("00")){
                // 将16进制字符串转换为整数
                int decimalValue = Integer.parseInt(str, 16);
                // 将整数转换为对应的字符并追加到结果字符串中
                output.append((char) decimalValue);
            }
        }
        return output.toString();
    }

    /**
     * 定时按照PI数据表配置的天数，进行数据清理，将多余的PI数据清理掉
     */
    @Override
    public void dataCleansing(){
        //查询所有清理时间不为空的数据的数据表配置
        LambdaQueryWrapper<PiCustomizeTable> queryWrapper = Wrappers.lambdaQuery(PiCustomizeTable.class);
        queryWrapper.isNotNull(PiCustomizeTable::getDays)  // 字段不为 null
                .ne(PiCustomizeTable::getDays, "");    // 字段不等于空字符串 ""
        List<PiCustomizeTable> listTable = piCustomizeTableService.list(queryWrapper);

        for (PiCustomizeTable piCustomizeTable : listTable) {
            //配置保留天数数据
            Integer days = piCustomizeTable.getDays();
            if(null == days || days == 0){
                continue;
            }
            LocalDateTime localDateTime = LocalDateTime.now();
            // 提前days天
            localDateTime = localDateTime.minusDays(days);
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            String formattedDateTime = localDateTime.format(formatter);


            List<String> tableList = new ArrayList<>();
            //判断是否存在子表
            String subtable = piCustomizeTable.getSubtable();
            if(StrUtil.isNotBlank(subtable)){
                tableList = Arrays.asList(subtable.split(","));
            }else{
                tableList.add(piCustomizeTable.getTableName());
            }
            // 删除指定时间数据, 同时清除子表数据
            for (String tn : tableList) {
                dataClean(tn, formattedDateTime, piCustomizeTable.getTableName());
            }
        }
    }

    /**
     * 清理表数据
     */
    public void dataClean(String tableName, String formattedDateTime, String mainTable){
        int batchSize = 1000;  // 每批删除1000条记录
        int offset = 0;
        int rowsDeleted = 0;
        while (true) {
            String delSql = "DELETE FROM " + tableName + " WHERE create_time < ? LIMIT ?";
            //判断是否子表
            boolean isMatch = tableName.matches("mes3_basedata_\\d+");
            if(isMatch){
                rowsDeleted = jdbcTemplate.update(delSql, formattedDateTime, batchSize);
            }else{
                Pattern pattern = Pattern.compile(".*_(\\d+)$");  // 匹配最后一个 _ 后的数字
                Matcher matcher = pattern.matcher(tableName);
                if (matcher.find()) {
                    String table = tableName+"_";
                    //lastNumber
                    delSql = " DELETE FROM  " + tableName +
                            " WHERE NOT EXISTS ( " +
                            "    SELECT 1 FROM " + mainTable +
                            "    WHERE a.id = b.main_id " +
                            " ) LIMIT ? ";
                    //delSql = "DELETE FROM " + tableName + " WHERE create_time < ? LIMIT ?";
                }
                rowsDeleted = jdbcTemplate.update(delSql, batchSize);
            }
            if (rowsDeleted < batchSize) {
                break;  // 如果删除的行数少于批次大小，说明已经删除完毕
            }
            offset += batchSize;
        }
    }


    /**
     * 根据模型配置的计算公式，提取出里面对应的pi或者基础点位
     * @param formulaCode 计算公式
     * @return
     */
    @Override
    public List<String> getListPiCodeByFormulaCode(String formulaCode){
        List<String> listPiCode = new ArrayList<>();
        if(StrUtil.isNotBlank(formulaCode)){
            // 正则表达式：匹配 ⊙ 和 ⊙ 之间的内容
            String regex = "⊙(.*?)⊙";
            // 创建 Pattern 对象
            Pattern pattern = Pattern.compile(regex);
            // 创建 Matcher 对象
            Matcher matcher = pattern.matcher(formulaCode);
            // 遍历所有匹配项
            while (matcher.find()) {
                // 提取捕获组中的内容（即 ⊙ 和 ⊙ 之间的部分）
                if(!listPiCode.contains(matcher.group(1))){
                    listPiCode.add(matcher.group(1));
                }
            }
        }
        return listPiCode;
    }

    /**
     * 提取指定函数中的计算公式
     * @param formulaCode 计算公式
     * @param funName 需要提取的方法名称
     * @return
     */
    public static List<String> getListPiCodeByFunString(String formulaCode,String funName){
        List<String> listPiCode = new ArrayList<>();
        if(StrUtil.isNotBlank(formulaCode)){
            // 正则表达式：匹配 ⊙ 和 ⊙ 之间的内容
            String regex = funName +"\\((.*?)\\)";
            // 创建 Pattern 对象
            Pattern pattern = Pattern.compile(regex);
            // 创建 Matcher 对象
            Matcher matcher = pattern.matcher(formulaCode);
            // 遍历所有匹配项
            while (matcher.find()) {
                // 提取捕获组中的内容（即 ⊙ 和 ⊙ 之间的部分）
                if(!listPiCode.contains(matcher.group(1))){
                    listPiCode.add(matcher.group(1));
                }
            }
        }
        return listPiCode;
    }

    /**
     * 将listPointDataAll写入文件
     * @param content 写入内容
     * @param listPointDataAll 写入点位集合
     * @param filePath 写入文件路径
     */
    public static void writeListPointDataToFile(String content,List<MeasurementData> listPointDataAll, String filePath) {
        Path path = Paths.get(filePath);
        Path parentDir = path.getParent();
        try {
            // 检查目录是否存在，如果不存在则创建
            if (parentDir != null && !Files.exists(parentDir)) {
                Files.createDirectories(parentDir);
            }
            try (FileWriter fileWriter = new FileWriter(filePath,true)) {
                fileWriter.write(content + "\n");
                for (MeasurementData data : listPointDataAll) {
                    // 假设 MeasurementData 类有一个 toString 方法，可以将其内容转换为字符串
                    fileWriter.write(data.toString() + "\n");
                }
                fileWriter.write("结束" + "\n");
                fileWriter.write("结束" + "\n");
                fileWriter.write("结束" + "\n");
            }
        }catch (IOException e) {
            logger.error("Error writing listPointDataAll to file: " + filePath, e);
        }
    }

    /**
     * 传入开始时间，结束时间和点位列表，查询列表时间范围内，influxDB中的数据，返回一个MeasurementData集合
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @param calculateSignalTagCodeList 点位列表
     * @param frequency 频率
     * @return
     */
    @Override
    public List<MeasurementData> getMeasurementDataList(LocalDateTime startTime,LocalDateTime endTime,List<String> calculateSignalTagCodeList,Integer frequency){
        //将集合和时间点传入influxDB，返回对应的自动化值
        QueryCondition queryCondition = new QueryCondition();
        List<QueryTagCondition> queryTagConditionList = new ArrayList<>();
        for(String tagCode : calculateSignalTagCodeList){
            if(StrUtil.isNotBlank(tagCode)){
                QueryTagCondition queryTagCondition = new QueryTagCondition();
                queryTagCondition.setTagId(tagCode);
                queryTagConditionList.add(queryTagCondition);
            }
        }
        //时间范围为空或无效时，直接返回空列表，避免 InfluxDB 报 "cannot query an empty range"
        if (startTime == null || endTime == null || !startTime.isBefore(endTime)) {
            return new ArrayList<>();
        }

        queryCondition.setStart(startTime);
        queryCondition.setEnd(endTime);

        if(Objects.nonNull(frequency) && frequency >= 1){
            queryCondition.setFrequency(frequency);
            queryCondition.setQueryType(QueryType.LAST);
        }

        queryCondition.setQueryTagConditionList(queryTagConditionList);

        //获取所有的触发步骤点位集合
        List<MeasurementData> listPointData = new ArrayList<>();

        try{
            if(queryTagConditionList.size() > 0){
                listPointData = influxDBClientHelper.query(queryCondition);
            }
        }catch (Exception e){
            logger.warn("获取测量数据异常, startTime={}, endTime={}", startTime, endTime, e);
        }
        return listPointData;
    }


    /**
     * 获取取数步骤的时间
     * @param calculateSignal 触发信号
     * @return
     */
    public Pair<LocalDateTime,LocalDateTime> getStepDate(PiCalculateSignal calculateSignal){
        Pair<LocalDateTime,LocalDateTime> timePair = new Pair<>(null,null);
        LocalDateTime startDateTime = null;
        LocalDateTime endDateTime = null;

        //获取取数步骤集合
        List<PiRetrievalStep> retrievalStepList = calculateSignal.getPiRetrievalStepList();

        //步骤只能有2步
        if(null == retrievalStepList  || retrievalStepList.size() != 2){
            return timePair;
        }

        // 指定时区，例如系统默认时区
        ZoneId zoneId = ZoneId.systemDefault();

        for(PiRetrievalStep step : retrievalStepList){
            //开始时间
            LocalDateTime calculateTime = calculateSignal.getCalculateTime();
            //框定时间范围，取数步骤有2个类型，触发时间和步骤号
            String stepValueType = step.getStepValueType();
            //步骤号
            if("2".equals(stepValueType)){
                //防止出现一秒钟的差异,将时间往后延迟2秒
                calculateTime = calculateTime.plusSeconds(2);

                //开始步骤,获取最近一个步骤的开始和结束时间
                QueryCondition queryCondition = new QueryCondition();
                //点位tag
                List<QueryTagCondition> queryTagConditionList = new ArrayList<>();
                QueryTagCondition tagCondition = new QueryTagCondition();
                tagCondition.setTagId(step.getPointCode());
                tagCondition.setValue(Float.parseFloat(step.getCalculateValue()));
                queryTagConditionList.add(tagCondition);
                //触发时间
                queryCondition.setEnd(calculateTime);
                queryCondition.setQueryTagConditionList(queryTagConditionList);

                //获取点位tag最近的开始时间和结束时间
                Pair<Instant, Instant> pairPoint = businessHandler.getStartAndEndTimeForContinuousTag(queryCondition);
                if(null == pairPoint){
                    return timePair;
                }
                //开始时间
                Instant startInstantTime = pairPoint.getKey();
                //结束时间
                Instant endInstantTime = pairPoint.getValue();

                // 将Instant转换为ZonedDateTime
                ZonedDateTime startZonedDateTime = startInstantTime.atZone(zoneId);
                // 从ZonedDateTime中提取LocalDateTime
                LocalDateTime startStepTime = startZonedDateTime.toLocalDateTime();

                ZonedDateTime endZonedDateTime = endInstantTime.atZone(zoneId);
                // 从ZonedDateTime中提取LocalDateTime
                LocalDateTime endStepTime = endZonedDateTime.toLocalDateTime();

                //根据算法类型（进入或者结束）来判定使用开始时间还是结束时间
                //进入
                if("7".equals(step.getCalculateType())){
                    calculateTime = startStepTime;
                }else{
                    calculateTime = endStepTime;
                }
            }
            //取PI的值
            if("3".equals(stepValueType)){
                //根据唯一约束条件，查询是否存在数据
                List<TableConditionDto> conditionList = getConditionList(calculateSignal);
                //当没有唯一约束的时候，整个计算跳出
                if(conditionList.size() == 0){
                    return timePair;
                }
                //查询PI对应的字段值
                TableQueryDto tableQueryDto = new TableQueryDto();
                tableQueryDto.setConditionList(conditionList);
                tableQueryDto.setTable(calculateSignal.getPiTable());
                tableQueryDto.setFieldList(Arrays.asList(step.getPointCode()));
                List<Map<String,Object>> piData = piCustomizeTableService.getTableData(tableQueryDto);
                if (null == piData || piData.size() != 1){
                    return timePair;
                }
                try {
                    //字段只能是时间类型
                    calculateTime = (LocalDateTime) piData.get(0).get(step.getPointCode());
                } catch (Exception e) {
                    return timePair;
                }
            }
            //提前或者延后0
            if(StrUtil.isNotBlank(step.getAdvanceTime()) && !"0".equals(step.getAdvanceTime())){
                //提前
                if("0".equals(step.getAdvanceDelay())){
                    calculateTime = calculateTime.minusSeconds(Long.parseLong(step.getAdvanceTime()));
                }else{
                    //延后
                    calculateTime = calculateTime.plusSeconds(Long.parseLong(step.getAdvanceTime()));
                }
            }

            if(step.getStepType() == 0){
                startDateTime = calculateTime;
            }
            if(step.getStepType() == 1){
                endDateTime = calculateTime;
            }
        }
        timePair = Pair.of(startDateTime,endDateTime);
        return timePair;
    }

    /**
     * 手动修改PI，触发PI间算法，进行自动计算
     * @param dataIds 修改的数据行id
     * @param piTable 修改的数据表
     * @param piCodes pi集合（修改数据的pi）
     */
    @Override
    public void savePICalculateSignal(List<String> dataIds, String piTable, List<String> piCodes){
        for (String dataId : dataIds){
            for (String piCode : piCodes){
                arithmeticByPi(dataId,piTable,piCode,LocalDateTime.now(),2);
            }
        }

    }


    /**
     * 将时间格式字符串转换成long
     * @param resultStr
     * @return
     */
    public String dateTimeToLongByResult(String resultStr){
        if(
                isValidTimeFormat(resultStr, "yyyy-MM-dd HH:mm:ss")  ||
                        isValidTimeFormat(resultStr, "yyyy-MM-dd'T'HH:mm:ss") ||
                        isValidTimeFormat(resultStr, "yyyy-MM-dd HH:mm") ||
                        isValidTimeFormat(resultStr, "yyyy-MM-dd'T'HH:mm")

        ){
            if(resultStr.contains("T")){
                resultStr = resultStr.replace("T"," ");
            }
            if(resultStr.length() == 16){
                resultStr = resultStr + ":00";
            }
            // 使用formatter将LocalDateTime格式化为字符串
            SimpleDateFormat formatterDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            try {
                resultStr = formatterDate.parse(resultStr).getTime() + "";
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        return changeDataFormat(resultStr);
    }

    /**
     * 对数据进行格式化转换
     * @param retData
     * @return
     */
    @Override
    public String changeDataFormat(String retData){
        if(StringUtils.isNotBlank(retData)){
            if(isScientificNotation(retData) || retData.matches(regex)){
                BigDecimal bd = new BigDecimal(retData);
                //直接截取，不做4舍5入处理
                BigDecimal roundedUp = bd.setScale(4, RoundingMode.DOWN).stripTrailingZeros();
                retData = roundedUp.toPlainString();
            }
        }
        return retData;
    }

    /**
     * 判断时间字符串是否在给定时间范围内，并返回符合条件的完整时间点（LocalDateTime格式）
     *
     * @param timeStr   时间字符串（格式如 "09:43"）
     * @param startTime 范围开始时间（LocalDateTime）
     * @param endTime   范围结束时间（LocalDateTime）
     * @return 符合条件的完整时间点列表（LocalDateTime格式）
     */
    public static List<LocalDateTime> getValidFullTimePoints(String timeStr, LocalDateTime startTime, LocalDateTime endTime) {
        List<LocalDateTime> validTimePoints = new ArrayList<>();

        try {
            // 解析输入的时间字符串为 LocalTime
            DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
            LocalTime targetTime = LocalTime.parse(timeStr, timeFormatter);

            // 遍历时间范围内的每一天
            LocalDateTime currentDate = startTime.toLocalDate().atStartOfDay();
            while (!currentDate.isAfter(endTime)) {
                // 构造当天的目标时间点
                LocalDateTime candidateTime = currentDate.with(targetTime);

                // 判断是否在范围内
                if ((candidateTime.isAfter(startTime) || candidateTime.isEqual(startTime)) &&
                        (candidateTime.isBefore(endTime) || candidateTime.isEqual(endTime))) {
                    validTimePoints.add(candidateTime); // 直接添加 LocalDateTime
                }

                // 移动到下一天
                currentDate = currentDate.plusDays(1);
            }
        } catch (DateTimeParseException e) {
            // 输入时间格式错误，抛出异常或返回空列表
            System.err.println("时间格式错误: " + timeStr);
        }

        return validTimePoints;
    }

    /**
     * 判断在给定时间范围内是否存在满足条件的整数小时，并返回符合条件的小时时间列表（LocalDateTime格式）
     *
     * @param startTime   开始时间
     * @param endTime     结束时间
     * @param isEvenHour  是否判断偶数小时（true：偶数小时，false：普通整数小时）
     * @return 符合条件的小时时间列表（LocalDateTime格式）
     */
    public static List<LocalDateTime> getValidHoursInRange(LocalDateTime startTime, LocalDateTime endTime, boolean isEvenHour) {
        List<LocalDateTime> validHours = new ArrayList<>();

        // 获取 startTime 所在的整数小时时间点
        LocalDateTime currentHourStart = startTime.truncatedTo(ChronoUnit.HOURS);

        // 循环检查每个整数小时是否满足条件
        while (!currentHourStart.isAfter(endTime)) {
            // 判断是否满足条件：
            // 1. 如果是偶数小时，则检查 hour % 2 == 0；
            // 2. 如果是普通整数小时，则直接通过。
            boolean isValidHour = !isEvenHour || (currentHourStart.getHour() % 2 == 0);

            // 判断当前小时是否在范围内且满足条件
            if (isValidHour && !currentHourStart.isBefore(startTime)) {
                validHours.add(currentHourStart); // 将符合条件的小时加入列表
            }

            // 移动到下一个整数小时
            currentHourStart = currentHourStart.plusHours(1);
        }
        return validHours;
    }

    /**
     * 结果转换模型
     * @param result 输入结果
     * @param piResultTransformationModel 模型配置信息
     * @param triggerTime 触发时间
     * @return
     */
    public static String resultConvertModel(String result,PiResultTransformationModel piResultTransformationModel,LocalDateTime triggerTime) {
        if (StrUtil.isBlank( result)){
            return result;
        }
        if(null == piResultTransformationModel || null == piResultTransformationModel.getPiResultTransformationModelDetailList() || piResultTransformationModel.getPiResultTransformationModelDetailList().size() == 0 ){
            return result;
        }
        //结果转换方式分3类，结果输入类型0时间类型、1字符类型、2数字类型
        int resultType = piResultTransformationModel.getResultType();
        if (Objects.isNull(resultType)){
            return result;
        }
        PiResultTransformationModelDetail detail = null;
        switch (resultType) {
            case 0:
                //时间类型
                detail = TimeResultConvertModel(result,piResultTransformationModel.getPiResultTransformationModelDetailList());
                break;
            case 1:
                //字符类型
                detail = StringResultConvertModel(result,piResultTransformationModel.getPiResultTransformationModelDetailList());
                break;
            case 2:
                //数字类型
                detail = IntegerResultConvertModel(result,piResultTransformationModel.getPiResultTransformationModelDetailList());
                break;
        }
        //对结果进行转换
        if (null != detail){
            //输出方式0自定义输出 1函数输出
            int outputType = detail.getOutputType();
            switch (outputType) {
                case 0:
                    result = detail.getOutputValue();
                    break;
                case 1:
                    String outputFunction = detail.getOutputValue();
                    //获取当前日期currentDateFun
                    if(outputFunction.equals("currentDateFun")){
                        // 创建一个日期时间格式化器，用于自定义输出格式
                        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                        // 格式化当前的日期和时间
                        String formattedDateTime = triggerTime.format(formatter);
                        result = formattedDateTime;
                    }
                    //获取昨天日期yesterdayDateFun
                    if(outputFunction.equals("yesterdayDateFun")){
                        LocalDateTime timeUniqueTemp = triggerTime.minusDays(1);
                        // 创建一个日期时间格式化器，用于自定义输出格式
                        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                        // 格式化当前的日期和时间
                        String formattedDateTime = timeUniqueTemp.format(formatter);
                        result = formattedDateTime;
                    }
                    break;
            }
        }
        return result;
    }

    /**
     * 结果转换模型-时间类型模型
     * @param result
     * @param piResultTransformationModelDetailList
     * @return
     */
    public static PiResultTransformationModelDetail TimeResultConvertModel(String result, List<PiResultTransformationModelDetail> piResultTransformationModelDetailList) {
        PiResultTransformationModelDetail retDetail = null ;
        if(null == piResultTransformationModelDetailList || piResultTransformationModelDetailList.size() == 0 ){
            return null;
        }
        //循环配置信息
        for (PiResultTransformationModelDetail detail : piResultTransformationModelDetailList) {
            if(StrUtil.isBlank(detail.getLeftValue()) && StrUtil.isBlank(detail.getRightValue())){
                continue;
            }
            if (StrUtil.isBlank(detail.getLeftValue())){
                detail.setLeftValue("00:00");
            }
            if (StrUtil.isBlank(detail.getRightValue())){
                detail.setRightValue("23:59");
            }
            //将result结果转换为LocalDateTime，并且格式化成HH:MM字符串
            String resultTime = convertToHHmmFlexible(result);
            if(StrUtil.isBlank(resultTime)){
                return null;
            }
            boolean pass = isBetween(detail.getLeftValue(), resultTime, detail.getRightValue());
            if (pass){
                retDetail = detail;
                break;
            }
        }
        return retDetail;
    }

    /**
     * 结果转换模型-字符串类型模型
     * @param result
     * @param piResultTransformationModelDetailList
     * @return
     */
    public static PiResultTransformationModelDetail StringResultConvertModel(String result, List<PiResultTransformationModelDetail> piResultTransformationModelDetailList) {
        PiResultTransformationModelDetail retDetail = null ;
        if(null == piResultTransformationModelDetailList || piResultTransformationModelDetailList.size() == 0 ){
            return null;
        }
        //循环配置信息
        for (PiResultTransformationModelDetail detail : piResultTransformationModelDetailList) {
            if(StrUtil.isBlank(detail.getRightValue())){
                continue;
            }
            //字符串类型默认使用右条件，条件方式默认等于
            if(result.equals(detail.getRightValue())){
                retDetail = detail;
                break;
            }
        }
        return retDetail;
    }


    /**
     * 结果转换模型-数字类型模型
     * @param result
     * @param piResultTransformationModelDetailList
     * @return
     */
    public static PiResultTransformationModelDetail IntegerResultConvertModel(String result, List<PiResultTransformationModelDetail> piResultTransformationModelDetailList) {
        PiResultTransformationModelDetail retDetail = null ;
        if(null == piResultTransformationModelDetailList || piResultTransformationModelDetailList.size() == 0 ){
            return null;
        }
        Double calculationValue = Double.parseDouble(result);
        //循环配置信息
        for (PiResultTransformationModelDetail detail : piResultTransformationModelDetailList) {
            boolean pass = false;
            //左偏差值状态
            boolean leftContinue = true;
            //右偏差值状态
            boolean rightContinue = true;
            //isContinue
            //左偏差值
            if(StrUtil.isNotBlank(detail.getLeftValue()) && StrUtil.isNotBlank(detail.getLeftCondition())){
                leftContinue = isCorrectCalculate(Double.parseDouble(detail.getLeftValue()),calculationValue,detail.getLeftCondition());
            }
            //右偏差值
            if(StrUtil.isNotBlank(detail.getRightValue()) && StrUtil.isNotBlank(detail.getRightCondition())){
                rightContinue = isCorrectCalculate(calculationValue,Double.parseDouble(detail.getRightValue()),detail.getRightCondition());
            }
            if(leftContinue && rightContinue){
                pass = true;
            }else{
                pass = false;
            }
            if (pass){
                retDetail = detail;
                break;
            }
        }
        return retDetail;
    }

    /**
     * 将时间字符串转换为HH:mm格式
     * @param dateTimeStr
     * @return
     */
    public static String convertToHHmmFlexible(String dateTimeStr) {
        List<String> patterns = Arrays.asList(
                "yyyy-MM-dd HH:mm:ss",
                "yyyy-MM-dd'T'HH:mm:ss",
                "yyyy-MM-dd HH:mm"
        );

        for (String pattern : patterns) {
            try {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);
                LocalDateTime dateTime = LocalDateTime.parse(dateTimeStr, formatter);
                return dateTime.format(DateTimeFormatter.ofPattern("HH:mm"));
            } catch (Exception ignored) {
                // 忽略当前格式，尝试下一个
                return null;
            }
        }
        return null;
    }

    /**
     * 将时间字符串转换为分钟数
     * @param time
     * @return
     */
    public static int timeToMinutes(String time) {
        String[] parts = time.split(":");
        int hours = Integer.parseInt(parts[0]);
        int minutes = Integer.parseInt(parts[1]);
        return hours * 60 + minutes;
    }

    /**
     * 判断 b 是否在 a 和 c 之间（考虑跨天）
     * @param a
     * @param b
     * @param c
     * @return
     */
    public static boolean isBetween(String a, String b, String c) {
        int timeA = timeToMinutes(a);
        int timeB = timeToMinutes(b);
        int timeC = timeToMinutes(c);

        // 处理跨天情况
        if (timeC < timeA) {
            // 跨天：c 加上一天的分钟数
            timeC += 24 * 60;
            // 如果 b 在当天范围内，也加上一天的分钟数
            if (timeB < timeA) {
                timeB += 24 * 60;
            }
        }
        // 判断 b 是否在 [a, c] 范围内
        return timeA <= timeB && timeB <= timeC;
    }
}
