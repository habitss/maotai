package com.keytech.modules.pi.service.impl;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.keytech.base.service.impl.BaseServiceImpl;
import com.keytech.modules.kpi.util.DateUtils;
import com.keytech.modules.mdm3.entity.MdmDevice;
import com.keytech.modules.mdm3.service.IMdmDeviceService;
import com.keytech.modules.pi.common.PiCommonConstants;
import com.keytech.modules.pi.dto.PiCurveStepDto;
import com.keytech.modules.pi.dto.RaAreaDto;
import com.keytech.modules.pi.dto.TableConditionQueryDto;
import com.keytech.modules.pi.entity.*;
import com.keytech.modules.pi.mapper.PiRaStepMapper;
import com.keytech.modules.pi.service.*;
import com.keytech.modules.pi.util.DateConvertUtil;
import com.keytech.modules.pi.vo.*;
import groovy.util.logging.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.jeecg.common.api.vo.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @Description: RA-步骤
 * @Author: jeecg-boot
 * @Date:   2025-10-31
 * @Version: V1.0
 */
@Service
@Slf4j
public class PiRaStepServiceImpl extends BaseServiceImpl<PiRaStepMapper, PiRaStep> implements IPiRaStepService {
    private static final Logger log = LoggerFactory.getLogger(PiRaStepServiceImpl.class);
    public static final int BATCH_SIZE = 60; // 每批处理100条
    public static final int BATCH_ONE = 1;
    @Value("${mes-system.tenant-id}")
    private Integer tenantId;
    @Autowired
    private IPiRaStepService iPiRaStepService;
    @Autowired
    private IPiRaStepParamService iPiRaStepParamService;
    @Autowired
    private IPiRaUnitService iPiRaUnitService;
    @Autowired
    private IPiAdapterAlgorithmService iPiAdapterAlgorithmService;
    @Autowired
    private IMdmDeviceService mdmDeviceService;
    @Autowired
    private IPiBasePointService piBasePointService;
    @Autowired
    private IPiCurveStepService iPiCurveStepService;
    @Autowired
    private IPiCurveParamService iPiCurveParamService;
    @Autowired
    private IPiCustomizeTableService iPiCustomizeTableService;
    @Autowired
    private IPiCurveConfigService iPiCurveConfigService;
    @Autowired
    private IPiCurveStandardService iPiCurveStandardService;

    @Override
    public void extractData() {
        //开始时间
        Timestamp lastLoadTime = null;
        //当前时间做结束时间
        Timestamp currentTime = new Timestamp(System.currentTimeMillis());
        //获取开始数据(表中最新最新离开时间)
        PiRaStep latestRecord = lambdaQuery()
                .orderByDesc(PiRaStep::getInsertDt)
                .last("LIMIT 1")
                .one();
        if(latestRecord != null){
            //时间转换
            lastLoadTime = new Timestamp(latestRecord.getInsertDt().getTime());
        }

        if(lastLoadTime == null){
            //设置开始时间为3个月前
            long threeMonthsAgo = System.currentTimeMillis() - TimeUnit.DAYS.toMillis(90);
            lastLoadTime = new Timestamp(threeMonthsAgo);
        }
        //获取设备
        List<PiRaUnit> units = iPiRaUnitService.list();
        //遍历产线 得到map key为 areaId_1 value为RaLineVO
        Map<String, PiRaUnit> unitMap = units.stream()
            .collect(Collectors.toMap(
                    unit -> unit.getAreaId()+"_"+unit.getUnitId(),
                    unit -> unit,
                    (existing, replacement) -> existing  // 保留已存在的，忽略新的
            ));

        log.info("开始参数分页增量同步，时间范围: {} - {}", lastLoadTime, currentTime);
        int page = 1;
        boolean hasMoreData = true;
        int totalProcessed = 0;

        //Timestamp lastRecordTime = lastLoadTime; // 初始化最后记录时间
        while (hasMoreData) {
            long offset = (page - 1) * BATCH_SIZE;
            //long endRow = page * BATCH_SIZE;
            // 使用参数分页查询
            log.info("查询条件，开始时间: {} - 结束时间: {} &&&& 第: {} 页数据,每页{}条数据", lastLoadTime,currentTime,offset,BATCH_SIZE);
            List<PiRaStep> dataList = iPiRaStepService.stepDataWithPaging(
                    lastLoadTime, currentTime, offset, BATCH_SIZE);
            log.info("查询返回数据，条数: {} 条数据", dataList.size());

            if (dataList == null || (dataList != null && dataList.isEmpty())) {
                log.info("已处理完成，总计: {} 条数据", totalProcessed);
                break;
            }
            //匹配设备 通过设备获取产线
            for (PiRaStep step : dataList) {
                PiRaUnit unit = unitMap.get(step.getAreaId()+"_"+step.getUnitId());
                if(unit != null){
                    step.setLineId(unit.getLineId());
                    step.setLineName(unit.getNameCn());
                }
            }

            //获取参数(计算结束时间和时长){通过最新参数的EventTime获取最新时间}
            List<PiRaStepParam> stepParamList = iPiRaStepParamService.selectByConditions(dataList);
            Map<String, PiRaStepParam> stepParamMap = getLatestStepParams(stepParamList);
            //匹配结束时间
            for (PiRaStep step : dataList) {
                String key = buildKey(step);
                if (stepParamMap.containsKey(key)) {
                    step.setEndTime(stepParamMap.get(key).getEventTime());
                    //计算时差(秒)
                    long durationInMillis = step.getEndTime().getTime() - step.getEventTime().getTime();
                    long durationInSeconds = durationInMillis / 1000;
                    step.setDuration(durationInSeconds);
                }
            }

            //获取监控时间(分钟)[计算结束时间和时长] {新方法 通过"监控时间"获取运行时长} [参数会查询出多个]
//            List<PiRaStepParam> stepParamTimeList = iPiRaStepParamService.selectByTimeConditions(dataList);
//            Map<String, PiRaStepParam> stepParamMap = getEarliestStepParams(stepParamTimeList);
//            for (PiRaStep step : dataList) {
//                String key = buildKey(step);
//                if (stepParamMap.containsKey(key)) {
//                    //获取运行时长(分钟)
//                    PiRaStepParam stepParam = stepParamMap.get(key);
//                    String rptValue = stepParam.getRptValue();
//                    //运行时长(毫秒)
//                    long durationInMillis = DateConvertUtil.convertToMillis(rptValue);
//                    //计算结束时间
//                    step.setEndTime(DateConvertUtil.calculateEndTime(step.getEventTime(), durationInMillis));
//                    //时差(秒)
//                    long durationInSeconds = durationInMillis / 1000;
//                    step.setDuration(durationInSeconds);
//                }
//            }

            // 处理本批数据
            int processed = processBatchData(dataList);
            totalProcessed += processed;

            log.info("已处理第 {} 页数据，数量: {}, 总计: {}", page, processed, totalProcessed);
            page++;

            // 如果返回的数据少于批次大小，说明没有更多数据了
            if (dataList.size() < BATCH_SIZE) {
                hasMoreData = false;
            }

        }
        // 更新抽取记录
        if (totalProcessed > 0) {
            log.info("参数分页同步完成，共处理 {} 条数据", totalProcessed);
        }
    }

    @Override
    @DS("rastepsqlservice")
    public List<PiRaStep> stepDataWithPaging(Timestamp lastLoadTime, Timestamp currentTime, long offset, int batchSize) {
        return baseMapper.stepDataWithPaging(lastLoadTime, currentTime, offset, batchSize);
    }


    /**
     * 处理批量数据
     */
    private int processBatchData(List<PiRaStep> dataList) {
        log.info("RA步骤数据处理1--------------{}", dataList.size());
        if (dataList.isEmpty()) {
            return 0;
        }
        // 转换数据
        List<PiRaStep> targetList = dataList.stream()
                .map(this::convertToTarget)
                .collect(Collectors.toList());

        // 批量插入
        saveOrUpdateBatch(targetList);
        //计算
        try {
            //顾虑无效数据
            List<PiRaStep> filteredList = dataList.stream()
                    .filter(step -> "Running Status".equals(step.getStatus()))
                    .collect(Collectors.toList());
            iPiAdapterAlgorithmService.calculateRa(filteredList, null);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return targetList.size();
    }

    /**
     * 数据转换
     */
    private PiRaStep convertToTarget(PiRaStep piRaStep) {
        //对象复制
        piRaStep.setTenantId(tenantId);
        // 拼接ID
        String id = buildCompleteId(piRaStep);
        piRaStep.setId(id);
        return piRaStep;
    }

    /**
     * 步骤 生成ID
     * 区域ID 设备ID 订单ID 批次号 配方ID 步骤ID 步骤号
     * @param piRaStep
     * @return
     */
    private String buildCompleteId(PiRaStep piRaStep) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS");
        String formattedTime = piRaStep.getEventTime() != null ?
                piRaStep.getEventTime().toInstant()
                        .atZone(ZoneId.systemDefault())
                        .format(formatter) : "00000000000000000";

        return String.join("_",
                piRaStep.getAreaId() != null ? piRaStep.getAreaId().toString() : "NULL_AREAID",
                piRaStep.getUnitId() != null ? piRaStep.getUnitId().toString() : "NULL_UNIT",
                piRaStep.getOrderId() != null ? piRaStep.getOrderId().toString() : "NULL_ORDER",
                piRaStep.getBatchNr() != null ? piRaStep.getBatchNr().toString() : "NULL_BATCH",
                piRaStep.getRecipeId() != null ? piRaStep.getRecipeId().toString() : "NULL_RECIPE",
                piRaStep.getBopId() != null ? piRaStep.getBopId().toString() : "NULL_BOP",
                piRaStep.getStepNr() != null ? piRaStep.getStepNr().toString() : "NULL_STEPNR",
                formattedTime
        );
    }


    /**
     * 获取最新的参数
     * @param stepParamList
     * @return
     */
    public Map<String, PiRaStepParam> getLatestStepParams(List<PiRaStepParam> stepParamList) {
        return stepParamList.stream()
            .collect(Collectors.groupingBy(
                param -> buildKey(param),
                Collectors.collectingAndThen(
                        Collectors.maxBy(Comparator.comparing(PiRaStepParam::getEventTime)),
                        Optional::get
                )
            ));
    }

    /**
     * 获取最早的参数
     * @param stepParamList
     * @return
     */
    public Map<String, PiRaStepParam> getEarliestStepParams(List<PiRaStepParam> stepParamList) {
        return stepParamList.stream()
                .collect(Collectors.groupingBy(
                        param -> buildKey(param),
                        Collectors.collectingAndThen(
                                Collectors.maxBy(Comparator.comparing(PiRaStepParam::getEventTime).reversed()), // 反转比较器
                                Optional::get
                        )
                ));
    }
    /**
     *  构建唯一key
     * @param entity
     * @return
     */
    // 泛型方法，支持 PiRaStepParam 和 PiRaStep
    private <T> String buildKey(T entity) {
        if (entity instanceof PiRaStepParam) {
            PiRaStepParam param = (PiRaStepParam) entity;
            return String.format("%s_%s_%s_%s_%s_%s",
                    param.getAreaId(),
                    param.getUnitId(),
                    param.getOrderId(),
                    param.getBatchNr(),
                    param.getBopId(),
                    param.getStepNr()
            );
        } else if (entity instanceof PiRaStep) {
            PiRaStep step = (PiRaStep) entity;
            return String.format("%s_%s_%s_%s_%s_%s",
                    step.getAreaId(),
                    step.getUnitId(),
                    step.getOrderId(),
                    step.getBatchNr(),
                    step.getBopId(),
                    step.getStepNr()
            );
        }
        throw new IllegalArgumentException("Unsupported entity type: " + entity.getClass());
    }

    @Override
    public List<RaLineVo> getLine(Integer areaId) {
        return baseMapper.getLine(areaId);
    }

    @Override
    public List<RaAreaDto> getArea() {
        return baseMapper.getArea();
    }

    /**
     * 步骤数据补抽
     * @param map
     */
    @Override
    public void retrieveData(Map map) {
        int currentPage = BATCH_ONE;
        int pageSize = BATCH_SIZE;
        boolean hasMoreData = true;

        while (hasMoreData) {
            // 创建分页对象
            Page<PiRaStep> page = new Page<>(currentPage, pageSize);
            // 构建查询条件
            LambdaQueryWrapper<PiRaStep> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.isNull(PiRaStep::getEndTime);
            LocalDateTime threeMonthsAgo = LocalDateTime.now().minusMonths(3);
            queryWrapper.ge(PiRaStep::getEventTime, threeMonthsAgo);
            //1天前
            queryWrapper.le(PiRaStep::getEventTime, LocalDateTime.now().minusHours(24));
            queryWrapper.orderByAsc(PiRaStep::getEventTime, PiRaStep::getId);
            // 执行分页查询
            Page<PiRaStep> pageResult = page(page, queryWrapper);
            List<PiRaStep> dataList = pageResult.getRecords();

            // 如果没有数据或数据量小于页大小，说明是最后一页
            if (dataList == null || dataList.isEmpty() || dataList.size() < pageSize) {
                hasMoreData = false;
            }
            if (!dataList.isEmpty()) {
                // 处理当前页数据
                processData(dataList);
            }
            currentPage++;

            // 添加延迟，避免对数据库造成太大压力
            try { Thread.sleep(100); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
        }
    }

    /**
     * 处理数据(补抽)-计算结束时间
     */
    private void processData(List<PiRaStep> dataList) {
        // 获取参数
        try {
            // 存储没有匹配上结束时间的步骤
            List<PiRaStep> noEndTimeList = new ArrayList<>();
            //通过参数获取结束时间
            List<PiRaStepParam> stepParamList = iPiRaStepParamService.selectByConditions(dataList);
            Map<String, PiRaStepParam> stepParamMap = getLatestStepParams(stepParamList);
            // 匹配结束时间
            List<PiRaStep> updateList = new ArrayList<>();
            for (PiRaStep step : dataList) {
                String key = buildKey(step);
                if (stepParamMap.containsKey(key)) {
                    step.setEndTime(stepParamMap.get(key).getEventTime());
                    // 计算时差(秒)
                    long durationInMillis = step.getEndTime().getTime() - step.getEventTime().getTime();
                    long durationInSeconds = durationInMillis / 1000;
                    step.setDuration(durationInSeconds);
                    updateList.add(step);
                }else{
                    //没有匹配到结束时间 将结束时间设置成开始时间
//                    step.setEndTime(step.getEventTime());
//                    step.setDuration(0L);
//                    updateList.add(step);
                    //开始时间 大于2个月 结束时间设置成开始时间
//                    if (LocalDateTime.now().minusMonths(2).isAfter(
//                            step.getEventTime().toInstant()
//                                    .atZone(ZoneId.systemDefault())
//                                    .toLocalDateTime())) {
//                        step.setEndTime(step.getEventTime());
//                    }
                    noEndTimeList.add(step);
                }
            }

            //获取监控时间(分钟)[计算结束时间和时长] {新方法 通过"监控时间"获取运行时长} [参数会查询出多个]
            if (noEndTimeList != null && !noEndTimeList.isEmpty()) {
                List<PiRaStepParam> stepParamTimeList = iPiRaStepParamService.selectByTimeConditions(noEndTimeList);
                stepParamMap = getEarliestStepParams(stepParamTimeList);
                for (PiRaStep step : noEndTimeList) {
                    String key = buildKey(step);
                    if (stepParamMap.containsKey(key)) {
                        //获取运行时长(分钟)
                        PiRaStepParam stepParam = stepParamMap.get(key);
                        String rptValue = stepParam.getRptValue();
                        //运行时长(毫秒)
                        long durationInMillis = DateConvertUtil.convertToMillis(rptValue);
                        //计算结束时间
                        step.setEndTime(DateConvertUtil.calculateEndTime(step.getEventTime(), durationInMillis));
                        //时差(秒)
                        long durationInSeconds = durationInMillis / 1000;
                        step.setDuration(durationInSeconds);
                        updateList.add(step);
                    } else {
                        //没有匹配到结束时间 将结束时间设置成开始时间
                        //                    step.setEndTime(step.getEventTime());
                        //                    step.setDuration(0L);
                        //                    updateList.add(step);
                        //开始时间 大于2个月 结束时间设置成开始时间
//                        if (LocalDateTime.now().minusMonths(2).isAfter(
//                                step.getEventTime().toInstant()
//                                        .atZone(ZoneId.systemDefault())
//                                        .toLocalDateTime())) {
//                            step.setEndTime(step.getEventTime());
//                            updateList.add(step);
//                        }
                    }
                }
            }
            // 更新数据
            if (!updateList.isEmpty()) {
                updateBatchById(updateList);
                log.info("成功更新 {} 条数据", updateList.size());
            }
        } catch (Exception e) {
            log.error("RA补抽时发生错误：{}", e.getMessage());
        }
    }

    /**
     * 获取上一批/下一批
     * @param piRaStep
     * @return
     */
    @Override
    public List<PiRaStep> getBatchByDirection(PiRaStep piRaStep) {
        String pageType = piRaStep.getPageType();
        LambdaQueryWrapper<PiRaStep> queryWrapper = new LambdaQueryWrapper<>();
        //queryWrapper.ge(PiRaStep::getEventTime, threeMonthsAgo);
        if(PiCommonConstants.CLOSE.equals(pageType)){
            //下一批 时间大于开始时间
            queryWrapper.gt(PiRaStep::getEventTime, piRaStep.getStartTime());
            queryWrapper.orderByAsc(PiRaStep::getEventTime);
        }else{
            //上一批 时间小于开始时间
            queryWrapper.lt(PiRaStep::getEventTime, piRaStep.getStartTime());
            queryWrapper.orderByDesc(PiRaStep::getEventTime);
        }
        queryWrapper.eq(PiRaStep::getUnitId, piRaStep.getUnitId());
        queryWrapper.eq(PiRaStep::getAreaId, piRaStep.getAreaId());
        queryWrapper.ne(PiRaStep::getBatchNr, piRaStep.getBatchNr());

        queryWrapper.last("limit 1");
        return list(queryWrapper);
    }

    /**
     * 获取设备点位
     * @param piRaStep
     * @return
     */
    @Override
    public DevicePointVo unitAndPoint(PiRaStep piRaStep) {
        String unitNames = piRaStep.getUnitNames();
        String orderAndBatch = piRaStep.getOrderAndBatch();
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

        String [] unitName = unitNames.split(",");
        // 获取设备(步骤中获取设备)
        LambdaQueryWrapper<PiRaStep> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PiRaStep::getAreaId, piRaStep.getAreaId());
        wrapper.eq(PiRaStep::getRecipeId, piRaStep.getRecipeId());

        if(StrUtil.isNotBlank(piRaStep.getUnitId())){
//            wrapper.eq(PiRaStep::getUnitId, piRaStep.getUnitId());
            //判断是否包含,
            if(piRaStep.getUnitId().contains(",")){
                wrapper.in(PiRaStep::getUnitId, piRaStep.getUnitId().split(","));
            }else {
                wrapper.eq(PiRaStep::getUnitId, piRaStep.getUnitId());
            }
        }else{
            //设备-查询条件
            if (unitName!=null && unitName.length > 0) {
                wrapper.and(w -> {
                    for (String name : unitName) {
                        w.or().like(PiRaStep::getUnitName, name);
                    }
                });
            }
        }

        //订单-批次查询条件
        wrapper.and(wr -> {
            for (String orderBatch : orderBatchNos) {
                String[] parts = orderBatch.split("-");
                wr.or(w -> w.eq(PiRaStep::getOrderId, parts[0])
                        .eq(PiRaStep::getBatchNr, parts[1]));
            }
        });
        wrapper.orderByAsc(PiRaStep::getEventTime);
        //.orderByAsc(PiRaStep::getEventTime);
        //步骤列表
        List<PiRaStep> stepList = list(wrapper);

        //先按区域 areaId 和设备 unitId 分类，再在每个分类下按 订单号 orderId 和批次号 batchNr 分类
        Map<String, Map<String, List<PiRaStep>>> stepGrouped = stepList.stream()
                // 先按区域-设备分类
                .collect(Collectors.groupingBy(
                        step -> String.format("%s-%s", step.getAreaId(), step.getUnitId()),
                        // 在每个区域-设备下再按订单-批次分类
                        Collectors.groupingBy(
                                step -> String.format("%d-%d", step.getOrderId(), step.getBatchNr())
                        )
                ));

        //获取时间跨度最大的步骤
        Map<String, PiRaStep> maxStep = maxDuration(stepList);
        //步骤通过设备分组（合并相同步骤，参考getData方法）
        Map<String, List<PiRaStep>> stepsByDevice = stepList.stream()
            .collect(Collectors.groupingBy(
                step -> String.format("%s-%s", step.getAreaId(), step.getUnitId()),
                LinkedHashMap::new,
                Collectors.toList()
            ));
        Map<String, List<StepVo>> stepUnitGroupMap = new LinkedHashMap<>();
        for (Map.Entry<String, List<PiRaStep>> entry : stepsByDevice.entrySet()) {
            List<PiRaStep> deviceSteps = entry.getValue();
            List<StepVo> stepVos = new ArrayList<>();
            String lastId = null;
            StepVo lastStepVo = null;
            for (PiRaStep step : deviceSteps) {
                String id = step.getId();
                //截取最后一个下划线之前的部分（去掉时间戳），用于判断相同步骤
                int lastUnderscoreIndex = id.lastIndexOf('_');
                String extracted = id.substring(0, lastUnderscoreIndex);
                if (extracted.equals(lastId)) {
                    //相同步骤，合并endTime
                    if (step.getEndTime() != null) {
                        lastStepVo.setEndTime(step.getEndTime());
                    }
                    continue;
                }
                StepVo stepVo = new StepVo();
                stepVo.setId(step.getId());
                stepVo.setStartTime(step.getEventTime());
                stepVo.setEndTime(step.getEndTime());
                stepVo.setOrderNo(step.getOrderId());
                stepVo.setBatchNo(step.getBatchNr());
                stepVo.setStepNr(step.getStepNr());
                String bopName = step.getBopName();
                //包含/取前面
                if (bopName != null && bopName.contains("/")) {
                    bopName = bopName.substring(0, bopName.indexOf("/"));
                }
                stepVo.setEopName(bopName);
                stepVo.setEopNo(String.valueOf(step.getBopId()));
                lastStepVo = stepVo;
                lastId = extracted;
                stepVos.add(stepVo);
            }
            stepUnitGroupMap.put(entry.getKey(), stepVos);
        }

        //过滤重复步骤
        stepUnitGroupMap =
                stepUnitGroupMap.entrySet()
                        .stream()
                        .collect(Collectors.toMap(
                                Map.Entry::getKey,
                                entry -> {
                                    PiRaStep max = maxStep.get(entry.getKey());
                                    if (max == null) {
                                        return Collections.emptyList();
                                    }

                                    Integer orderId = max.getOrderId();
                                    Integer batchNr = max.getBatchNr();

                                    return entry.getValue()
                                            .stream()
                                            .filter(vo ->
                                                    Objects.equals(orderId, vo.getOrderNo())
                                                            && Objects.equals(batchNr, vo.getBatchNo())
                                            )
                                            .collect(Collectors.toList());
                                }
                        ));


        //获取曲线步骤配置
        List<PiCurveStep> curveStepList = iPiCurveStepService.batchRaQueryByStepsSql(stepList);
        //遍历 curveStepList 得到map  key为  areaNo,yearNo,reccatNo,pcuNo,unitNo,recipeNo 值为 excludeRopIds
        Map<String, String> excludeRopIdsMap = curveStepList.stream()
                .collect(Collectors.toMap(
                        item -> String.format("%s-%s-%s-%s",
                                item.getAreaNo(),
                                item.getReccatNo(),
                                item.getUnitNo(),
                                item.getRecipeNo()),  // 添加了 recipeNo
                        PiCurveStep::getExcludeRopIds
                ));
        //excludeRopIds 存储了ropId集合 逗号拼接(排除步骤)
        stepList = filterExcludedStepsOptimized(stepList, excludeRopIdsMap);


        //分组 (区域_设备_订单_批次)
        Map<String, PiRaStep> stepGroupMap = stepGroup(stepList);

        //步骤 设备去重 unitNo和pointName 不能相同
        List<PiRaStep> distinctList = stepList.stream()
                .collect(Collectors.collectingAndThen(
                        Collectors.toCollection(() ->
                                new TreeSet<>(Comparator.comparing((PiRaStep p) -> p.getUnitId())
                                        .thenComparing(p -> p.getUnitName()))
                        ),
                        ArrayList::new
                ));

        //获取设备Code集合
        List<String> unitNos = distinctList.stream()
                .map(step -> step.getAreaId() + "-" + step.getUnitId())
                .distinct()
                .collect(Collectors.toList());
        if(unitNos != null && unitNos.isEmpty()){
            return new DevicePointVo();
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
//        pointWrapper.orderByAsc(PiBasePoint::getDataAttribute);
        pointWrapper.orderByAsc(PiBasePoint::getSort);
        List<PiBasePoint> pointList = piBasePointService.list(pointWrapper);

        //
        Map<String,Boolean> deviceMap = new HashMap<String,Boolean>();
        //设备id 和订单批次
        Map<String, ArrayList<Map<String, String>>> deviceIdToOrderBatchMap = new HashMap<>();
        for (PiRaStep sistarRtSteps : stepList) {
            //设备code (区域_设备id)
            String deviceCode = sistarRtSteps.getAreaId()+"-"+sistarRtSteps.getUnitId();
            String deviceId = deviceIdToCodeMap.get(deviceCode)+"";
            String key = sistarRtSteps.getOrderId()+"-"+sistarRtSteps.getBatchNr();
            String orderBatchDate = orderBatchMpa.get(key);
            //获取分组值
            PiRaStep stepGroupTime = stepGroupMap.get(deviceCode+"-"+key);
            if(StrUtil.isNotBlank(deviceId) && StrUtil.isNotBlank(orderBatchDate) && !deviceMap.containsKey(deviceId+"_"+key) && stepGroupTime != null){
                deviceMap.put(deviceId+"_"+key, true);
                Map<String, String> map = new HashMap<>();
                //String[] dates= orderBatchDate.split("_");
                //if(dates != null && dates.length == 2 && StrUtil.isNotBlank(dates[0]) && StrUtil.isNotBlank(dates[1])){
                map.put("orderBatch", key);
//                    map.put("startDate",dates[0]);
//                    map.put("endDate", dates[1]);
                if (stepGroupTime.getEventTime() != null && stepGroupTime.getEndTime() != null){
                    map.put("startDate", DateUtils.formatDateTime(stepGroupTime.getEventTime()));
                    map.put("endDate", DateUtils.formatDateTime(stepGroupTime.getEndTime()));
                }
                //排除时间为空
                if(deviceIdToOrderBatchMap.containsKey(deviceId)){
                    ArrayList<Map<String, String>> list = deviceIdToOrderBatchMap.get(deviceId);
                    list.add(map);
                }else{
                    ArrayList<Map<String, String>> list = new ArrayList<>();
                    list.add(map);
                    deviceIdToOrderBatchMap.put(deviceId, list);
                }
                    //deviceIdToOrderBatchMap.put(deviceId, map);
                //}
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

        //匹配标准曲线配置
        //步骤分组-stepList (区域_设备_配方_步骤号)
        Map<String, List<PiRaStep>> stepEopGroup = stepEopGroup(stepList);
        //获取标准配置 (步骤 和 点位code[pointList])
        List<PiCurveStandard> piCurveStandardList = iPiCurveStandardService.getCurveStandardByRa(stepEopGroup, pointList);
        //标准分组 (设备 --> id_参数_点位集合)
//        Map<String, List<String>> standardGroupMap = standardGroup(piCurveStandardList);
        Map<String, List<PiCurveStandard>> standardGroupMap = standardGroup(piCurveStandardList);
        //写入返回集合 点位code


        List<PiSistarEngUnits> unitList = new ArrayList<>();
        //拼接数据 设备 设备点位
        for (MdmDevice mdmDevice : deviceList) {
            String deviceCode = "";
            //判断设备编号是否包含- 包含进行拆分
            if (mdmDevice.getDeviceCode().contains("-")){
                String[] deviceCodeArr = mdmDevice.getDeviceCode().split("-");
                deviceCode = deviceCodeArr[1];
            }else{
                deviceCode = mdmDevice.getDeviceCode();
            }
            PiSistarEngUnits piSistarEngUnits = new PiSistarEngUnits();
            piSistarEngUnits.setUnitNo(Integer.parseInt(deviceCode));
            piSistarEngUnits.setUnitName(mdmDevice.getDeviceName());
            List<PiBasePoint> points = new ArrayList<>();
            for (PiBasePoint point : pointList) {
                if (Objects.equals(mdmDevice.getId(), point.getDeviceId())) {
                    points.add(point);
                }
            }

            //标准曲线
            if(standardGroupMap.containsKey(deviceCode)){
                List<PiCurveStandard> standard = standardGroupMap.get(deviceCode);
                if(standard != null && !standard.isEmpty()){
                    PiBasePoint point = new PiBasePoint();
                    String pointCode = "";
                    String pointName = "";
                    String unit = "";
                    List< String> ids = new ArrayList<>();
                    for (PiCurveStandard curvestandard : standard) {
                        //if(StrUtil.isBlank(str)){continue;}
                        //String[] split = str.split("_");
                        if(StrUtil.isBlank(pointCode)){
                            pointCode = "point_"+curvestandard.getPointCode();
                        }
                        if(StrUtil.isBlank(pointName)){
                            pointName = curvestandard.getParam();
                        }
                        if (StrUtil.isBlank(unit)){
                            unit = curvestandard.getUnit();
                        }
                        ids.add(curvestandard.getId());
                    }
                    point.setPointName(pointName);
                    point.setStandardIds(String.join(",", ids));
                    point.setPointCode(pointCode);
                    point.setUnit(unit);
                    points.add(point);
                }

            }

            if(points != null && !points.isEmpty()){
                piSistarEngUnits.setPoints(points);
                piSistarEngUnits.setDeviceCode(mdmDevice.getDeviceCode());
                unitList.add(piSistarEngUnits);
            }
        }

        // 设备分组-步骤配置 id 集合
        Map<String, List<String>> configStepGroupMap = stepList.stream()
            .collect(Collectors.groupingBy(
                step -> String.format("%s-%s", step.getAreaId(), step.getUnitId()),
                Collectors.mapping(
                    PiRaStep::getId,  // 提取 id
                    Collectors.toList()  // 收集为 List
                )
            ));

        // 构建标准步骤列表(按设备分组) - 参考getData方法
        Map<String, List<PiRaStep>> standardStepListMap = new HashMap<>();
        for (Map.Entry<String, List<StepVo>> deviceEntry : stepUnitGroupMap.entrySet()) {
            String deviceKey = deviceEntry.getKey(); // areaId-unitId
            List<StepVo> deviceSteps = deviceEntry.getValue();
            if (deviceSteps == null || deviceSteps.isEmpty()) continue;

            // 从deviceKey中提取unitId
            String unitId = deviceKey.contains("-") ? deviceKey.split("-")[1] : deviceKey;
            // 获取该设备的标准配置
            List<PiCurveStandard> standards = standardGroupMap.get(unitId);
            if (standards == null || standards.isEmpty()) continue;

            // 匹配第一个标准步骤的起始时间
            PiCurveStandard firstStandard = standards.get(0);
            String firstEopNo = firstStandard.getEopNo();
            Date standardStartTime = null;
            List<PiRaStep> unmatchedList = new ArrayList<>();

            for (StepVo step : deviceSteps) {
                if (step.getEopNo() != null && step.getEopNo().equals(firstEopNo)) {
                    standardStartTime = step.getStartTime();
                    break;
                } else {
                    PiRaStep unmatched = new PiRaStep();
                    unmatched.setEventTime(step.getStartTime());
                    unmatched.setEopName("");
                    unmatched.setBopName("");
                    unmatched.setDuration(0L);
                    unmatched.setStartTime(step.getStartTime());
                    unmatched.setEndTime(step.getStartTime());
                    unmatched.setId(step.getId());
                    unmatchedList.add(unmatched);
                }
            }

            if (standardStartTime == null) continue;

            List<PiRaStep> standardStepList = new ArrayList<>(unmatchedList);
            Date currentTime = standardStartTime;
            Set<String> matchedIds = new HashSet<>();

            for (PiCurveStandard standard : standards) {
                PiRaStep rtStep = new PiRaStep();
                rtStep.setEopName(standard.getEopName());
                rtStep.setBopName(standard.getEopName());
                rtStep.setGroupName(standard.getGroupName());
                rtStep.setStartTime(currentTime);
                rtStep.setEventTime(currentTime);

                long durationMillis = 0;
                if (StrUtil.isNotBlank(standard.getDuration())) {
                    double duration = Double.parseDouble(standard.getDuration().trim());
                    Integer durationType = standard.getDurationType();
                    if (durationType != null && durationType == 1) {
                        durationMillis = (long) (duration * 60 * 1000);
                    } else {
                        durationMillis = (long) (duration * 60 * 60 * 1000);
                    }
                }
                Date stepEndTime = new Date(currentTime.getTime() + durationMillis);
                rtStep.setEndTime(stepEndTime);
                rtStep.setDuration(durationMillis / 1000);

                for (StepVo step : deviceSteps) {
                    if (step.getEopNo() != null && step.getEopNo().equals(standard.getEopNo())
                            && !matchedIds.contains(step.getId())) {
                        rtStep.setId(step.getId());
                        matchedIds.add(step.getId());
                        break;
                    }
                }
                standardStepList.add(rtStep);
                currentTime = stepEndTime;
            }

            // 检查最后实际步骤是否比最后标准步骤结束时间晚
            if (!deviceSteps.isEmpty() && !standardStepList.isEmpty()) {
                StepVo lastStep = deviceSteps.get(deviceSteps.size() - 1);
                PiRaStep lastStandardStep = standardStepList.get(standardStepList.size() - 1);
                if (lastStep.getEndTime() != null && lastStandardStep.getEndTime() != null
                        && lastStep.getEndTime().after(lastStandardStep.getEndTime())) {
                    PiRaStep tailStep = new PiRaStep();
                    tailStep.setStartTime(lastStandardStep.getEndTime());
                    tailStep.setEndTime(lastStep.getEndTime());
                    tailStep.setEventTime(lastStandardStep.getEndTime());
                    tailStep.setEopName("");
                    tailStep.setBopName("");
                    tailStep.setDuration(0L);
                    tailStep.setId(lastStep.getId());
                    standardStepList.add(tailStep);
                }
            }
            //合并连续且groupName相同的步骤
            List<PiRaStep> mergedList = new ArrayList<>();
            for (PiRaStep step : standardStepList) {
                if (!mergedList.isEmpty()) {
                    PiRaStep last = mergedList.get(mergedList.size() - 1);
                    if (StrUtil.isNotBlank(step.getGroupName())
                            && step.getGroupName().equals(last.getGroupName())) {
                        last.setEndTime(step.getEndTime());
                        if (last.getDuration() != null && step.getDuration() != null) {
                            last.setDuration(last.getDuration() + step.getDuration());
                        }
                        continue;
                    }
                }
                mergedList.add(step);
            }
            standardStepListMap.put(deviceKey, mergedList);
        }

        DevicePointVo devicePointVo = new DevicePointVo();
        devicePointVo.setUnitList(unitList);
        //设备步骤分组 设备-步骤集合
        devicePointVo.setStepGroupMap(stepUnitGroupMap);
        //设备步骤分组 设备-选中步骤集合
        devicePointVo.setConfigStepsMap(configStepGroupMap);
        //步骤分组
        devicePointVo.setStepGrouped(stepGrouped);
        //标准生成的步骤集合(按设备分组)
        devicePointVo.setStandardStepListMap(standardStepListMap);
        return devicePointVo;
    }

    /**
     * 步骤分组-stepList (区域_设备_配方_步骤号)
     */
    public Map<String, List<PiRaStep>> stepEopGroup(List<PiRaStep> stepList) {
        Map<String, List<PiRaStep>> stepEopGroup = stepList.stream()
                .collect(Collectors.groupingBy(
                        step -> step.getAreaId() + "-" +
                                step.getUnitId() + "-" +
                                step.getRecipeId() + "-" +
                                step.getBopId()
                ));
        return stepEopGroup;
    }

    public Map<String, List<PiCurveStandard>> standardGroup(List<PiCurveStandard> piCurveStandardList) {
        //设备分组
        return piCurveStandardList.stream().collect(Collectors.groupingBy(PiCurveStandard::getUnitNo));
    }

    /**
     * 步骤分组
     * 区域_设备_订单_批次
     * 开始时间 结束时间
     * @param stepList
     * @return
     */
    public Map<String, PiRaStep> stepGroup(List<PiRaStep> stepList) {
        Map<String, PiRaStep> resultMap =
                stepList.stream()
                        .collect(Collectors.toMap(
                                s -> s.getAreaId() + "-" +
                                        s.getUnitId() + "-" +
                                        s.getOrderId() + "-" +
                                        s.getBatchNr(),
                                Function.identity(),
                                (a, b) -> {
                                    // 开始时间取最小
                                    if (a.getStartTime() == null ||
                                            (b.getStartTime() != null && a.getEventTime().after(b.getEventTime()))) {
                                        a.setStartTime(b.getEventTime());
                                    }

                                    // 结束时间取最大
                                    if (a.getEndTime() == null ||
                                            (b.getEndTime() != null && a.getEndTime().before(b.getEndTime()))) {
                                        a.setEndTime(b.getEndTime());
                                    }

                                    return a;
                                }
                        ));
        return resultMap;
    }


    /**
     * 获取设备
     * @param piRaStep
     * @return
     */
    @Override
    public List<DeviceCurveVo> getDevice(PiRaStep piRaStep) {
        //获取设备
        LambdaQueryWrapper<MdmDevice> wrapperDevice = new LambdaQueryWrapper<>();
        wrapperDevice.like(MdmDevice::getDeviceCode, "-");
        // 排序
        wrapperDevice.orderByAsc(MdmDevice::getSort);
        List<MdmDevice> deviceList = mdmDeviceService.list(wrapperDevice);

        LambdaQueryWrapper<PiRaStep> wrapper = new LambdaQueryWrapper<>();
        wrapper.ge(PiRaStep::getEventTime, piRaStep.getEventTime());
        wrapper.le(PiRaStep::getEventTime, piRaStep.getEndTime());
        wrapper.eq(PiRaStep::getAreaId, piRaStep.getAreaId());
        //配方
        wrapper.eq(piRaStep.getRecipeId() != null ,PiRaStep::getRecipeId, piRaStep.getRecipeId());

        wrapper.orderByAsc(PiRaStep::getUnitId);
        List<PiRaStep> list = list(wrapper);
        Set<String> set = new HashSet<>();

        int curveDataType = 1;
        if(list != null && !list.isEmpty()) {
            //遍历list 得到 区域-设备set集合
            for (PiRaStep raStep : list) {
                set.add(raStep.getAreaId() + "-" + raStep.getUnitId());
            }
        }else{
            //没有设备查找PI配置
            List<PiCurveConfig> piCurveConfigList = iPiCurveConfigService.getCurveConfigByArea(String.valueOf(piRaStep.getAreaId()));
            for (PiCurveConfig piCurveConfig : piCurveConfigList) {
                set.add(piCurveConfig.getDeviceNo());
            }
            curveDataType = 3;
        }
        int dataType = curveDataType;

        //deviceList遍历 只保留 deviceCode 与 set 中相同的
//        List<MdmDevice> filteredDevices = deviceList.stream()
//                .filter(device -> set.contains(device.getDeviceCode()))
//                .collect(Collectors.toList());
        List<DeviceCurveVo> filteredDevices = deviceList.stream()
                .filter(device -> set.contains(device.getDeviceCode()))
                .map(device -> {
                    DeviceCurveVo vo = new DeviceCurveVo();
                    vo.setId(device.getId());
                    vo.setDeviceName(device.getDeviceName());
                    vo.setDeviceCode(device.getDeviceCode());
                    vo.setCurveDataType(dataType);
                    return vo;
                })
                .collect(Collectors.toList());

        return filteredDevices;
    }

    /**
     * 步骤保存
     * @param piCurveStep
     */
    @Override
    public void stepsRaSave(PiCurveStep piCurveStep) {
        //参数校验前置
//        if(StringUtils.isBlank(piCurveStep.getExcludeIds())){
//            throw new IllegalArgumentException("请先配置步骤");
//        }
        if (StringUtils.isBlank(piCurveStep.getStepIds())) {
            throw new IllegalArgumentException("步骤ID不能为空");
        }

        //
        String[] stepIdArray = StringUtils.split(piCurveStep.getStepIds(), ",");
        if (stepIdArray == null || stepIdArray.length == 0) {
            return;
        }
        //排除得 集合
        String excludeRopIds = "";
        String[] existiongArray = StringUtils.split(piCurveStep.getExcludeIds(), ",");

        List<PiRaStep> stepList = new ArrayList<>();
        if(piCurveStep.getCurveDataType().equals("1")) {
            //直接使用数组转换
            stepList = listByIds(Arrays.asList(stepIdArray));
            if (CollectionUtils.isEmpty(stepList)) {
                return;
            }
        }else{
            //pi 步骤
            if(StrUtil.isNotBlank(piCurveStep.getUnitNo()) && piCurveStep.getUnitNo().contains("-")){
                String areaNo = piCurveStep.getUnitNo().split("-")[0];
                piCurveStep.setAreaNo(Integer.parseInt(areaNo));
            }
            PiCurveStepDto piCurveStepDto = new PiCurveStepDto();
            piCurveStepDto.setDeviceNo(piCurveStep.getUnitNo());
            piCurveStepDto.setAreaNo(String.valueOf(piCurveStep.getPcuNo())); //区域
            piCurveStepDto.setBatch(piCurveStep.getBatchNo());
            //判断批次是否包含"-"
            String batchNo = piCurveStep.getBatchNo();
            if (batchNo != null && batchNo.contains("-")) {
                // 如果包含"-"，格式为"订单号 - 批次号"，拆分后分别设置
                String[] parts = batchNo.split("-");
                piCurveStepDto.setOrder(parts[0]);
                piCurveStepDto.setBatch(parts[1]);
            }
            //piCurveStepDto.setBatch(batchNo);
            //判断设备编码是否是否为空,且包含"-" 去第一个值左区域
            if (piCurveStep.getUnitNo() != null && piCurveStep.getUnitNo().contains("-")) {
                piCurveStepDto.setAreaNo(piCurveStep.getUnitNo().split("-")[0]);
            }

            PiCurveStepVo piCurveStepVo =iPiCurveConfigService.getData(piCurveStepDto);
            List<PiCurveStepDto> piStepList = piCurveStepVo.getStepList();

            List<String> ids = Arrays.asList(stepIdArray);

            for (PiCurveStepDto step : piStepList) {
                if(step.getId() != null && ids.contains(step.getId().toString())){
                    PiRaStep sistarRtStep = new PiRaStep();
                    sistarRtStep.setAreaId(piCurveStep.getAreaNo());
                    //sistarRtStep.setPcuNoStr(piCurveStep.getPcuNo());
                    String unitNo = step.getUnitNo();
//                    if(unitNo.contains("/")){
//                        unitNo = unitNo.split("/")[1];
//                    }
                    sistarRtStep.setUnitId(unitNo);
                    if(StrUtil.isNotBlank(step.getRecipeNo()) && !"null".equals(step.getRecipeNo())) {
                        sistarRtStep.setRecipeId(Integer.parseInt(step.getRecipeNo()));
                    }
                    //排除null
                    if(StrUtil.isNotBlank(step.getEopNo()) && !"null".equals(step.getEopNo())){
                        sistarRtStep.setStepNr(Integer.parseInt(step.getEopNo()));
                    }
                    stepList.add(sistarRtStep);
                }
            }

            //排除
            StringJoiner excludeStepConfig = new StringJoiner(",");
            List<String> existingList = Arrays.asList(existiongArray);
            for (PiCurveStepDto step : piStepList) {
                if(step.getId() != null && existingList.contains(step.getId().toString())){
                    excludeStepConfig.add(String.valueOf(step.getEopNo()));
                }
            }
            excludeRopIds = excludeStepConfig.toString();
        }

        // 使用 StringJoiner 自动处理分隔符
        StringJoiner stepConfig = new StringJoiner(",");

        //优化属性赋值逻辑
        Integer areaNo = piCurveStep.getAreaNo();
        //Integer yearNo = piCurveStep.getYearNo();
        Integer reccatNo = piCurveStep.getReccatNo();
        String pcuNo = piCurveStep.getPcuNo();
        String unitNo = piCurveStep.getUnitNo();
        Integer recipeNo = piCurveStep.getRecipeNo();
        for (PiRaStep step : stepList) {
            if (areaNo == null) {
                areaNo = step.getAreaId();
            }
            if (reccatNo == null) {
                //产线
                reccatNo = step.getLineId();
            }
//            if (pcuNo == null) {
//                pcuNo = step.getPcuNo();
//            }
            if (unitNo == null) {
                unitNo = step.getUnitId();
            }
            if (recipeNo == null) {
                recipeNo = step.getRecipeId();
            }
            stepConfig.add(String.valueOf(step.getStepNr()));
        }
        if(piCurveStep.getCurveDataType().equals("1")) {
            //排除得 roop_id集合
            //String[] existiongArray = StringUtils.split(piCurveStep.getExcludeIds(), ",");
//            if (existiongArray == null || existiongArray.length == 0) {
//                return;
//            }
            if (existiongArray == null || existiongArray.length == 0) {
                excludeRopIds = "";
            } else {
                //直接使用数组转换
                List<PiRaStep> excludeStepList = listByIds(Arrays.asList(existiongArray));
                if (CollectionUtils.isEmpty(excludeStepList)) {
                    return;
                }

                StringJoiner excludeStepConfig = new StringJoiner(",");
                for (PiRaStep step : excludeStepList) {
                    excludeStepConfig.add(String.valueOf(step.getStepNr()));
                }
                excludeRopIds = excludeStepConfig.toString();
            }
        }


        // 更新/新增
        iPiCurveStepService.saveOrUpdateByQuery(stepConfig.toString(),excludeRopIds, areaNo, reccatNo,
                pcuNo, unitNo, recipeNo, piCurveStep.getCurveDataType());
    }

    /**
     * 获取步骤数据
     * @param piRaStep
     * @return
     */
    @Override
    public StepDataVo getData(PiRaStep piRaStep) {
        MdmDevice mdmDevice = mdmDeviceService.getById(piRaStep.getUnitId());
        StepDataVo stepDataVO = new StepDataVo();
        if(mdmDevice== null){
            return stepDataVO;
        }
        String deviceCode = mdmDevice.getDeviceCode();
        //判断是否包含-
        if (StrUtil.isNotBlank(deviceCode) && deviceCode.contains("-")){
            //拆分 1-2
            String[] split = deviceCode.split("-");
            deviceCode = split[1];
        }
        LambdaQueryWrapper<PiRaStep> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PiRaStep::getAreaId, piRaStep.getAreaId());
//		wrapper.ge(PiRaStep::getEventTime, piRaStep.getEventTime());
//		wrapper.le(PiRaStep::getEventTime, piRaStep.getEndTime());
        wrapper.eq(piRaStep.getRecipeId() != null ,PiRaStep::getRecipeId, piRaStep.getRecipeId());
        wrapper.eq(PiRaStep::getOrderId, piRaStep.getOrderId());
        wrapper.eq(PiRaStep::getBatchNr, piRaStep.getBatchNr());
        wrapper.eq(PiRaStep::getUnitId, deviceCode);
        wrapper.orderByAsc(PiRaStep::getStepNr).orderByAsc(PiRaStep::getEventTime);
        List<PiRaStep> list = list(wrapper);

        //上一次id
        String lastId = null;
        PiRaStep lastRaStep = null;
        List<PiRaStep> filterList = new ArrayList<>();
        for (PiRaStep raStep : list) {
            //提取id 用于处理相同步骤 id 合并成一条
            String id= raStep.getId();
            // 找到最后一个下划线的位置
            int lastUnderscoreIndex = id.lastIndexOf('_');
            // 截取最后一个下划线之前的部分
            String extracted = id.substring(0, lastUnderscoreIndex);
            if(extracted.equals(lastId)){
                lastRaStep.setEndTime(raStep.getEndTime());
                continue;
            }else{
                lastRaStep = raStep;
                lastId = extracted;
            }

            raStep.setStartTime(raStep.getEventTime());
            String bopName = raStep.getBopName();
            //取/前
            if (StrUtil.isNotBlank(bopName) && bopName.contains("/")) {
                bopName = bopName.substring(0, bopName.indexOf("/"));
            }
            raStep.setEopName(bopName);
            //步骤id
            raStep.setRopId(raStep.getStepNr());

            filterList.add(raStep);
        }

        if(filterList != null && !filterList.isEmpty()){
            PiRaStep raStep = filterList.get(0);
            //获取第一条的开始时间
            Date startTime = raStep.getEventTime();
            //获取最后一条的结束时间
            Date endTime = filterList.get(filterList.size() - 1).getEndTime();
            stepDataVO.setStartTime(startTime);
            //为空则设置为当前时间
            //stepDataVO.setEndTime((endTime != null && endTime.getTime() > 0) ? endTime : new Date());
            stepDataVO.setEndTime(endTime);
            stepDataVO.setRaStepList(filterList);
            stepDataVO.setRecipeName(raStep.getRecipeName());

            //获取步骤配置
            PiCurveStep curveStepList = iPiCurveStepService.getRaCurveStep(raStep);
            String ropIds = curveStepList != null ? curveStepList.getExcludeRopIds() : null;
            if(ropIds != null){
                //得到map key为 stepConfig(,号分隔)每项 值为 true
                Map<String, Boolean> stepMap = Arrays.stream(ropIds.split(","))
                        .collect(Collectors.toMap(
                                Function.identity(),
                                k -> true,
                                (existingValue, newValue) -> existingValue // 合并函数，保留现有值
                        ));
                stepDataVO.setCurveStepMap(stepMap);
            }
            //获取标准曲线配置
            LambdaQueryWrapper<PiCurveStandard> standardwrapper = new LambdaQueryWrapper<>();
            standardwrapper.eq(PiCurveStandard::getAreaNo, raStep.getAreaId());
            standardwrapper.eq(PiCurveStandard::getUnitNo, raStep.getUnitId());
            standardwrapper.eq(PiCurveStandard::getRecipeNo, raStep.getRecipeId());
            //步骤条件
            standardwrapper.and(w -> {
                for (PiRaStep step : filterList) {
                    w.or(or -> or.eq(PiCurveStandard::getEopNo, step.getBopId()));
                }
            });
            standardwrapper.orderByAsc(PiCurveStandard::getSort);
            List<PiCurveStandard> standardList = iPiCurveStandardService.list(standardwrapper);



            //匹配步骤，得到匹配的第一个步骤开始时间
            if (standardList != null && !standardList.isEmpty()) {
                //
                PiCurveStandard firstStandard = standardList.stream()
                        .filter(s -> s.getInitialValue() != null && !s.getInitialValue().isEmpty()
                                && s.getEndValue() != null && !s.getEndValue().isEmpty()
                                && s.getDuration() != null && !s.getDuration().isEmpty())
                        .findFirst()
                        .orElse(null);
                if (firstStandard == null) {
                    return stepDataVO;
                }
                // 获取firstStandard 之前的标准id集合set
                Set<String> beforeFirstIds = new HashSet<>();
                for (PiCurveStandard s : standardList) {
                    if (s == firstStandard) {
                        break;
                    }
                    beforeFirstIds.add(s.getId());
                }

                String firstEopNo = firstStandard.getEopNo();
                //Date firstTime = null;
                Date standardStartTime = null;
                // 没匹配前集合
                List<PiRaStep> unmatchedList = new ArrayList<>();

                for (PiRaStep step : filterList) {
                    if (step.getBopId() != null && String.valueOf(step.getBopId()).equals(firstEopNo)) {
                        standardStartTime = step.getEventTime();
                        break;
                    }else{
                        unmatchedList.add(step);
                    }
                }
                //开始时间为空不做计算
                if (standardStartTime != null) {
                    //开始时间不为空 基于这个时间计算
                    List<PiRaStep> standardStepList = new ArrayList<>();

                    //遍历-没匹配前集合
                    for (PiRaStep step : unmatchedList) {
                        PiRaStep piRaStepTmp = new PiRaStep();
                        piRaStepTmp.setEventTime(step.getEventTime());
                        piRaStepTmp.setEopName("");
                        piRaStepTmp.setBopName("");
                        piRaStepTmp.setDuration(0L);
                        piRaStepTmp.setStartTime(step.getEventTime());
                        piRaStepTmp.setEndTime(step.getEventTime());
                        piRaStepTmp.setId(step.getId());
                        standardStepList.add(piRaStepTmp);
                    }

                    Date currentTime = standardStartTime;
                    Set<String> matchedIds = new HashSet<>();
                    for (PiCurveStandard standard : standardList) {
                        //排除未匹配unmatchedList - 已通过matchedIds初始化排除
                        //排除firstStandard之前的标准id
                        if (beforeFirstIds.contains(standard.getId())) {
                            continue;
                        }

                        PiRaStep rtStep = new PiRaStep();
//                        if (StrUtil.isNotBlank(standard.getEopNo())) {
//                            rtStep.setEopNo(standard.getEopNo());
//                        }
                        rtStep.setEopName(standard.getEopName());
                        rtStep.setBopName(standard.getEopName());
                        rtStep.setGroupName(standard.getGroupName());
                        rtStep.setStartTime(currentTime);
                        rtStep.setEventTime(currentTime);
                        //计算持续时间
                        long durationMillis = 0;
                        if (StrUtil.isNotBlank(standard.getDuration())) {
                            double duration = Double.parseDouble(standard.getDuration().trim());
                            Integer durationType = standard.getDurationType();
                            if (durationType != null && durationType == 1) {
                                //分钟
                                durationMillis = (long) (duration * 60 * 1000);
                            } else {
                                //小时
                                durationMillis = (long) (duration * 60 * 60 * 1000);
                            }
                        }
                        Date stepEndTime = new Date(currentTime.getTime() + durationMillis);
                        rtStep.setEndTime(stepEndTime);
                        rtStep.setDuration((Long) (durationMillis / 1000));
                        //从filterList中匹配对应步骤的id，已匹配过的不再匹配
                        for (PiRaStep step : filterList) {
                            if (step.getBopId() != null && String.valueOf(step.getBopId()).equals(standard.getEopNo())
                                    && !matchedIds.contains(step.getId())) {
                                rtStep.setId(step.getId());
                                matchedIds.add(step.getId());
                                break;
                            }
                        }
                        //没匹配上的按PiRaStep抽取方式生成id
                        if (rtStep.getId() == null) {
                            rtStep.setAreaId(raStep.getAreaId());
                            rtStep.setUnitId(raStep.getUnitId());
                            rtStep.setOrderId(raStep.getOrderId());
                            rtStep.setBatchNr(raStep.getBatchNr());
                            rtStep.setRecipeId(raStep.getRecipeId());
                            if (StrUtil.isNotBlank(standard.getEopNo())) {
                                rtStep.setBopId(Integer.parseInt(standard.getEopNo().trim()));
                            }
                            rtStep.setStepNr(standard.getSort() != null ? standard.getSort().intValue() : null);
                            rtStep.setId(buildCompleteId(rtStep));
                        }
                        standardStepList.add(rtStep);
                        currentTime = stepEndTime;
                    }
                    //判断filterList最后一条数据的EndTime 是否大于 standardStepList最后一条的EndTime 大于写入一条到standardStepList
                    if (!filterList.isEmpty() && !standardStepList.isEmpty()) {
                        PiRaStep lastFilterStep = filterList.get(filterList.size() - 1);
                        PiRaStep lastStandardStep = standardStepList.get(standardStepList.size() - 1);
                        if (lastFilterStep.getEndTime() != null && lastStandardStep.getEndTime() != null
                                && lastFilterStep.getEndTime().after(lastStandardStep.getEndTime())) {
                            PiRaStep tailStep = new PiRaStep();
                            tailStep.setStartTime(lastStandardStep.getEndTime());
                            tailStep.setEndTime(lastFilterStep.getEndTime());
                            tailStep.setEventTime(lastStandardStep.getEndTime());
                            tailStep.setEopName("");
                            tailStep.setBopName("");
                            tailStep.setDuration(0L);
                            //从filterList中匹配最后一条的id
                            tailStep.setId(lastFilterStep.getId());
                            standardStepList.add(tailStep);
                        }
                    }

                    //合并连续且groupName相同的步骤
                    List<PiRaStep> mergedList = new ArrayList<>();
                    for (PiRaStep step : standardStepList) {
                        if (!mergedList.isEmpty()) {
                            PiRaStep last = mergedList.get(mergedList.size() - 1);
                            if (StrUtil.isNotBlank(step.getGroupName())
                                    && step.getGroupName().equals(last.getGroupName())) {
                                last.setEndTime(step.getEndTime());
                                if (last.getDuration() != null && step.getDuration() != null) {
                                    last.setDuration(last.getDuration() + step.getDuration());
                                }
                                continue;
                            }
                        }
                        mergedList.add(step);
                    }
                    standardStepList = mergedList;

                    //结果写入stepDataVO.standardStepList
                    stepDataVO.setStandardStepList(standardStepList);

                    //匹配不上的追加到raStepList中 按EventTime顺序插入
                    Set<String> existingIds = filterList.stream()
                            .map(PiRaStep::getId)
                            .collect(Collectors.toSet());
                    List<PiRaStep> mergedRaStepList = new ArrayList<>(filterList);
                    for (PiRaStep standardStep : standardStepList) {
                        if (standardStep.getId() != null && !existingIds.contains(standardStep.getId())) {
                            mergedRaStepList.add(standardStep);
                        }
                    }
                    mergedRaStepList.sort(Comparator.comparing(PiRaStep::getEventTime));
                    stepDataVO.setRaStepList(mergedRaStepList);
                }
            }
        }
        return stepDataVO;
    }

    /**
     * 获取其他参数
     *   1工艺浓度
     *   2冷麦汁产量
     * @param piRaStep
     * @return
     */
    @Override
    public Object getOtherParam(PiRaStep piRaStep) {
        Map<Integer, Object> result = new HashMap<>();
        //获取PI曲线参数配置
        LambdaQueryWrapper<PiCurveParam> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PiCurveParam::getAreaNo, piRaStep.getAreaId());
        List<PiCurveParam> paramList = iPiCurveParamService.list(wrapper);
        if(paramList != null && paramList.isEmpty()){
            return result;
        }
        //获取数据
        for (PiCurveParam piCurveParam : paramList) {
            int param = piCurveParam.getParam();
            String tableName = piCurveParam.getTableName();
            //值为需要获取的值
            String value = piCurveParam.getParamVal();

            //查询条件
            //时间
            String dateTime = piCurveParam.getDateTime();
            // 得到年
            String year = "";
            if(piRaStep.getStartTime() != null){
                year = DateUtils.formatDateByPattern(piRaStep.getStartTime(), DateUtils.YEAR_FORMAT);
            }

            //订单号
            String orderNo = piCurveParam.getOrderNo();
            //批次号
            String batchNo = piCurveParam.getBatchNo();

            TableConditionQueryDto queryDto = new TableConditionQueryDto();
            queryDto.setTable(tableName);


            //
            String condition = dateTime + " = '" + year + "'";
            //订单
            condition += " AND " + orderNo + " = '" + piRaStep.getOrderId() + "'";
            //批次
            condition += " AND " + batchNo + " = '" + piRaStep.getBatchNr() + "'";
            queryDto.setCondition(condition);

            List<Map<String,Object>> dataList = iPiCustomizeTableService.getTableByCondition(queryDto);
            Object val = "";
            if(dataList != null && !dataList.isEmpty()) {
                if (dataList.get(0).get(value) != null) {
                    val = dataList.get(0).get(value);
                }
            }
            //查询数据
            result.put(param, val);
        }
        return result;
    }

    @Override
    public Object getRecipeByAreaId(String areaId, String unitId) {

        List<String> unitIdList = new ArrayList<>();
        if(StrUtil.isNotBlank(unitId)){
            unitIdList = Arrays.asList(unitId.split(","));
        }
        List<PiRaStep> stepList = baseMapper.getRecipeByAreaId(areaId, unitIdList);
        List<PiCurveStandard> list = new ArrayList<>();
        for (PiRaStep piRaStep : stepList) {
            PiCurveStandard piCurveStandard = new PiCurveStandard();
            piCurveStandard.setRecipeNo(String.valueOf(piRaStep.getRecipeId()));
            String recipeName = piRaStep.getRecipeName();
            //名称包含/的取 前
            if (StrUtil.isNotBlank(recipeName) && recipeName.contains("/")) {
                recipeName = recipeName.substring(0, recipeName.indexOf("/"));
            }
            piCurveStandard.setRecipeName(recipeName);
            list.add(piCurveStandard);
        }
        return list;
    }

    @Override
    public Object getStepByAreaId(String areaId, String unitId, String recipeId) {

        List<String> unitIdList = new ArrayList<>();
        if(StrUtil.isNotBlank(unitId)){
            unitIdList = Arrays.asList(unitId.split(","));
        }
        List<PiRaStep> stepList = baseMapper.getStepByAreaId(areaId, unitIdList, recipeId);
        List<PiCurveStandard> list = new ArrayList<>();
        for (PiRaStep piRaStep : stepList) {
            PiCurveStandard piCurveStandard = new PiCurveStandard();
            piCurveStandard.setEopNo(String.valueOf(piRaStep.getBopId()));
            String bopName = piRaStep.getBopName();
            //名称包含/的取 前
            if (StrUtil.isNotBlank(bopName) && bopName.contains("/")) {
                bopName = bopName.substring(0, bopName.indexOf("/"));
            }
            piCurveStandard.setEopName(bopName);
            list.add(piCurveStandard);
        }
        return list;
    }

    /**
     * 获取 最新上一批次
     * @param piRaStep
     * @return
     */
    @Override
    public PiRaStep getMashingData(PiRaStep piRaStep) {
        // 最新批次
        Optional<PiRaStep> optional = iPiRaStepService.lambdaQuery()
                .like(PiRaStep::getUnitName, piRaStep.getUnitName())
//                .notLike(PiRaStep::getRecipeName,"CIP")
                .orderByDesc(PiRaStep::getEventTime)
                .last("LIMIT 1")
                .oneOpt();
        PiRaStep piSistarRtStep = optional.orElse(null);

        //获取上一批次
        PiRaStep lastPiRaStep = iPiRaStepService.lambdaQuery()
                .le(PiRaStep::getEventTime, piSistarRtStep.getEventTime())
//                .like(PiRaStep::getUnitName, piRaStep.getUnitName())
                .eq(PiRaStep::getUnitName, piSistarRtStep.getUnitName())
                //.notLike(PiRaStep::getRecipeName,"CIP")
                .ne(PiRaStep::getBatchNr, piSistarRtStep.getBatchNr())
                .orderByDesc(PiRaStep::getEventTime)
                .last("LIMIT 1")
                .oneOpt()
                .orElse(null);

//        QueryWrapper<PiRaStep> queryWrapper = new QueryWrapper<>();
//        queryWrapper.le("start_time", piSistarRtStep.getStartTime())
//                .like("unit_name", piRaStep.getUnitName())
//                .ne("batch_nr", piSistarRtStep.getBatchNr())
//                .orderByDesc("event_time")
//                .last("LIMIT 1");
        //PiRaStep lastPiRaStep = iPiRaStepService.getOne(queryWrapper);

        // 获取设备id
        LambdaQueryWrapper<MdmDevice> wrapperDevice = new LambdaQueryWrapper<>();
        wrapperDevice.like(MdmDevice::getDeviceCode, "-");
        List<MdmDevice> deviceList = mdmDeviceService.list(wrapperDevice);
        //获取设备code 和 设备对象 对照map
        Map<String, MdmDevice> deviceMap = deviceList.stream()
                .collect(Collectors.toMap(
                        MdmDevice::getDeviceCode,  // key mapper
                        device -> device,          // 使用设备对象本身作为value
                        (existing, replacement) -> existing  // 保留已有值，忽略新值
                ));
        String key = lastPiRaStep.getAreaId() + "-" + lastPiRaStep.getUnitId();
        if(lastPiRaStep != null && deviceMap.containsKey(key)){
            MdmDevice mdmDevice = (deviceMap.get(key));
            lastPiRaStep.setUnitNames(mdmDevice.getId().toString());
            lastPiRaStep.setLineCode(mdmDevice.getLineCode());
        }
        return lastPiRaStep;
    }

    @Override
    public void retrieveEndTime(Map map) {
        int currentPage = BATCH_ONE;
        int pageSize = BATCH_SIZE;
        boolean hasMoreData = true;

        while (hasMoreData) {
            // 创建分页对象
            Page<PiRaStep> page = new Page<>(currentPage, pageSize);
            // 构建查询条件
            LambdaQueryWrapper<PiRaStep> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.isNull(PiRaStep::getEndTime);
            LocalDateTime threeMonthsAgo = LocalDateTime.now().minusMonths(3);
            queryWrapper.ge(PiRaStep::getEventTime, threeMonthsAgo);
            //queryWrapper.ge(PiRaStep::getEventTime, LocalDateTime.of(2026, 1, 20, 19, 1, 43));
            //12 小时前
            queryWrapper.le(PiRaStep::getEventTime, LocalDateTime.now().minusHours(12));
            queryWrapper.orderByAsc(PiRaStep::getEventTime, PiRaStep::getId);
            // 执行分页查询
            Page<PiRaStep> pageResult = page(page, queryWrapper);
            List<PiRaStep> dataList = pageResult.getRecords();

            // 如果没有数据或数据量小于页大小，说明是最后一页
            if (dataList == null || dataList.isEmpty() || dataList.size() < pageSize) {
                hasMoreData = false;
            }
            if (!dataList.isEmpty()) {
                // 处理当前页数据(计算下一步开始时间)
                processEndTime(dataList);
            }
            currentPage++;

            // 添加延迟，避免对数据库造成太大压力
            try { Thread.sleep(100); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
        }
    }

    /**
     * 处理数据(补抽)-计算结束时间
     */
    private void processEndTime(List<PiRaStep> dataList) {
        // 获取参数
        try {
            List<PiRaStep> updateList = new ArrayList<>();


            for (PiRaStep piRaStep : dataList) {
                //获取下一步结束时间
                List<PiRaStep> stepList = iPiRaStepService.selectNextData(piRaStep);
                if(stepList != null && !stepList.isEmpty()){
                    //下一步的开始时间是上一步的结束时间
                    PiRaStep nextRaStep = stepList.get(0);
                    piRaStep.setEndTime(nextRaStep.getEventTime());
                    // 计算时差(秒)
                    long durationInMillis = piRaStep.getEndTime().getTime() - piRaStep.getEventTime().getTime();
                    long durationInSeconds = durationInMillis / 1000;
                    piRaStep.setDuration(durationInSeconds);
                    updateList.add(piRaStep);
                }
            }

            // 更新数据
            if (!updateList.isEmpty()) {
                updateBatchById(updateList);
                log.info("成功更新 {} 条数据", updateList.size());
            }
        } catch (Exception e) {
            log.error("RA补抽时发生错误：{}", e.getMessage());
        }
    }


    /**
     * 获取下一步数据
     */
    @DS("rastepsqlservice")
    @Override
    public List<PiRaStep> selectNextData(PiRaStep piRaStep) {
        return baseMapper.selectNextData(piRaStep);
    }

    //过滤步骤
    public List<PiRaStep> filterExcludedStepsOptimized(
            List<PiRaStep> stepList,
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

    private boolean isExcludedOptimized(PiRaStep step, Map<String, Set<String>> excludeMap) {
        String key = buildCompositeKey(step);
        Set<String> excludeSet = excludeMap.get(key);

        if (excludeSet == null || excludeSet.isEmpty() || step.getStepNr() == null) {
            return false;
        }

        return excludeSet.contains(String.valueOf(step.getStepNr()));
    }

    //指定key
    private String buildCompositeKey(PiRaStep step) {
        return String.format("%s-%s-%s-%s",
                step.getAreaId(),
                step.getLineId(),
                step.getAreaId() + "-" + step.getUnitId(),
                step.getRecipeId()
        );
    }

    /**
     * 获取最大持续时间的步骤
     */
    private Map<String, PiRaStep> maxDuration(List<PiRaStep> stepList) {

        return stepList.stream()
                .collect(Collectors.groupingBy(
                        step -> step.getAreaId() + "-" + step.getUnitId()
                ))
                .entrySet()
                .stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> entry.getValue().stream()
                                .collect(Collectors.groupingBy(
                                        s -> s.getOrderId() + "-" + s.getBatchNr()
                                ))
                                .values()
                                .stream()
                                .map(batchSteps -> {

                                    Date start = batchSteps.stream()
                                            .map(PiRaStep::getEventTime)
                                            .filter(Objects::nonNull)
                                            .min(Date::compareTo)
                                            .orElse(null);

                                    Date end = batchSteps.stream()
                                            .map(PiRaStep::getEndTime)
                                            .filter(Objects::nonNull)  // 过滤掉 null 值
                                            .max(Date::compareTo)
                                            .orElse(null);

                                    if (start == null || end == null) {
                                        return null;
                                    }

                                    long durationMillis = Duration.between(
                                            start.toInstant(),
                                            end.toInstant()
                                    ).toMillis();

                                    PiRaStep endStep = batchSteps.stream()
                                            .filter(s -> s.getEndTime() != null)
                                            .max(Comparator.comparing(PiRaStep::getEndTime))
                                            .orElse(null);

                                    return endStep == null
                                            ? null
                                            : Map.entry(endStep, durationMillis);
                                })
                                .filter(Objects::nonNull)
                                // 按 duration 比较
                                .max(Comparator.comparing(Map.Entry::getValue))
                                // 只返回 step
                                .map(Map.Entry::getKey)
                                .orElse(null)
                ));
    }
}
