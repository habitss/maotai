package com.keytech.modules.pi.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.keytech.base.service.impl.BaseServiceImpl;
import com.keytech.modules.kpi.util.DateUtils;
import com.keytech.modules.mdm3.entity.MdmDevice;
import com.keytech.modules.mdm3.service.IMdmDeviceService;
import com.keytech.modules.pi.dto.PiCurveStepDto;
import com.keytech.modules.pi.entity.*;
import com.keytech.modules.pi.mapper.PiSistarRtStepsMapper;
import com.keytech.modules.pi.mapper.SourceSistarRtStepsMapper;
import com.keytech.modules.pi.service.*;
import com.keytech.modules.pi.vo.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.session.SqlSession;
import org.jeecg.common.api.vo.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @Description: Adapter数据抽取-步骤
 * @Author: jeecg-boot
 * @Date:   2025-08-28
 * @Version: V1.0
 */
@Service
@Slf4j
public class PiSistarRtStepsServiceImpl extends BaseServiceImpl<PiSistarRtStepsMapper, PiSistarRtSteps> implements IPiSistarRtStepsService {

    public static final int BATCH_SIZE = 500; // 每批处理500条
    @Value("${mes-system.tenant-id}")
    private Integer tenantId;
    @Autowired
    private ISourceSistarRtStepsService iSourceSistarRtStepsService;
    @Autowired
    private IPiSistarRtStepLogService iPiSistarRtStepLogService;
    @Autowired
    private IPiSistarEngReccatsService iPiSistarEngReccatsService;
    @Autowired
    private IPiSistarEngPcusService iPiSistarEngPcusService;
    @Autowired
    private IPiSistarEngUnitsService iPiSistarEngUnitsService;
    @Autowired
    private IPiSistarEngEopsService iPiSistarEngEopsService;
    @Autowired
    private IPiSistarEngRecipesService iPiSistarEngRecipesService;
    @Autowired
    private IPiAdapterUnitService iPiAdapterUnitService;
    @Autowired
    private IPiAdapterUnitEopService iPiAdapterUnitEopService;
    @Autowired
    private IPiAdapterAlgorithmService iPiAdapterAlgorithmService;
    @Autowired
    private IMdmDeviceService mdmDeviceService;
    @Autowired
    private IPiBasePointService piBasePointService;
    @Autowired
    private IPiCurveStepService iPiCurveStepService;
    @Autowired
    private IPiCurveStandardService iPiCurveStandardService;

    @Autowired
    private IPiCurveConfigService iPiCurveConfigService;

    @Autowired
    private IPiCurvePartialStandardService iPiCurvePartialStandardService;


    /**
     * 数据抽取
     */
    @Override
    public void extractData() {
        Timestamp currentTime = new Timestamp(System.currentTimeMillis());
        Timestamp lastLoadTime = iPiSistarRtStepLogService.getLastLoadTime("0");
        if(lastLoadTime == null){
            return;
        }
        log.info("开始参数分页增量同步，时间范围: {} - {}", lastLoadTime, currentTime);
        int page = 1;
        boolean hasMoreData = true;
        int totalProcessed = 0;

        // 获取设备
        List<PiAdapterUnit> unitList = iPiAdapterUnitService.list();
        // 获取设备与EOP对照
        List<PiAdapterUnitEop> unitEopList = iPiAdapterUnitEopService.list();

        //设备map key 为区域和设备编号下横线分隔 value 为对象
        Map<String, PiAdapterUnit> unitMap = unitList.stream()
            .collect(Collectors.toMap(
                    piAdapterUnit -> piAdapterUnit.getAreaNo() + "_" + piAdapterUnit.getPcuNo() + "_" + piAdapterUnit.getUnitNo(),
                    piAdapterUnit -> piAdapterUnit,
                    (existing, replacement) -> existing  // 保留已存在的值，忽略新值
            ));

        //设备EOPmap key 为区域、设备编号、EOP号下横线分隔 value 为对象
        Map<String, PiAdapterUnitEop> unitEopMap = unitEopList.stream()
                .collect(Collectors.toMap(
                        ue -> ue.getAreaNo() + "_" + ue.getPcuNo() +"_"+ ue.getUnitNo() + "_" + ue.getEopNo(),
                        ue -> ue,
                        (existing, replacement) -> existing  // 保留已存在的值，忽略新值
                ));

        Timestamp lastRecordTime = lastLoadTime; // 初始化最后记录时间
        while (hasMoreData) {
            long offset = (page - 1) * BATCH_SIZE;
            //long endRow = page * BATCH_SIZE;
            // 使用参数分页查询
            log.info("查询条件，开始时间: {} - 结束时间: {} &&&& 第: {} 页数据,每页{}条数据", lastLoadTime,currentTime,offset,BATCH_SIZE);
            List<SourceSistarRtSteps> dataList = iSourceSistarRtStepsService.selectIncrementalDataWithPaging(
                    lastLoadTime, currentTime, offset, BATCH_SIZE);
            log.info("查询返回数据，条数: {} 条数据", dataList.size());

            if (dataList == null || (dataList != null && dataList.isEmpty())) {
                log.info("已处理完成，总计: {} 条数据", totalProcessed);
                break;
            }
            // 处理本批数据
            int processed = processBatchData(dataList);
            totalProcessed += processed;

            // 写入记录 由于数据按 insert_timestamp 正序排列，直接取最后一条的时间
            SourceSistarRtSteps lastRecord = dataList.get(dataList.size() - 1);
            if (lastRecord.getInsertTimestamp() != null && lastRecord.getInsertTimestamp().after(lastRecordTime)) {
                lastRecordTime = new Timestamp(lastRecord.getInsertTimestamp().getTime());
                iPiSistarRtStepLogService.updateLastLoadTime("0", lastRecordTime, tenantId);
            }
            log.info("已处理第 {} 页数据，数量: {}, 总计: {}", page, processed, totalProcessed);
            page++;

            // 如果返回的数据少于批次大小，说明没有更多数据了
            if (dataList.size() < BATCH_SIZE) {
                hasMoreData = false;
            }
            // 定期清空缓存
            if (page % 10 == 0) {
                clearMyBatisCache();
            }


            // 新增 设备和设备EOP对照
            try {
                if(dataList != null && !dataList.isEmpty()){
                    //设备新增集合
                    List<PiAdapterUnit> unitAddList = new ArrayList<>();
                    //设备与EOP对照新增集合
                    List<PiAdapterUnitEop> unitEopAddList = new ArrayList<>();
                    for(SourceSistarRtSteps sourceSistarRtSteps : dataList){
                        //设备
                        String key = sourceSistarRtSteps.getAreaNo() + "_" + sourceSistarRtSteps.getPcuNo() + "_" + sourceSistarRtSteps.getUnitNo();
                        if(!unitMap.containsKey(key)){
                            PiAdapterUnit piAdapterUnit = new PiAdapterUnit();
                            piAdapterUnit.setTenantId(tenantId);
                            piAdapterUnit.setAreaNo(sourceSistarRtSteps.getAreaNo());
                            piAdapterUnit.setPcuNo(sourceSistarRtSteps.getPcuNo());
                            piAdapterUnit.setUnitNo(sourceSistarRtSteps.getUnitNo());
                            piAdapterUnit.setUnitName(sourceSistarRtSteps.getUnitName());
                            unitAddList.add(piAdapterUnit);
                            unitMap.put(key, piAdapterUnit);
                        }else{
                            PiAdapterUnit piAdapterUnit = unitMap.get(key);
                            //判断名称是否相同
                            if(!piAdapterUnit.getUnitName().equals(sourceSistarRtSteps.getUnitName())){
                                //更新名称
                                piAdapterUnit.setUnitName(sourceSistarRtSteps.getUnitName());
                                unitAddList.add(piAdapterUnit);
                                unitMap.put(key, piAdapterUnit);
                            }
                        }
                        String keyEop = sourceSistarRtSteps.getAreaNo() + "_" + sourceSistarRtSteps.getPcuNo() + "_" + sourceSistarRtSteps.getUnitNo() +
                                       "_" +sourceSistarRtSteps.getEopNo();
                        if(!unitEopMap.containsKey(keyEop)){
                            PiAdapterUnitEop unitEop = new PiAdapterUnitEop();
                            unitEop.setTenantId(tenantId);
                            unitEop.setAreaNo(sourceSistarRtSteps.getAreaNo());
                            unitEop.setPcuNo(sourceSistarRtSteps.getPcuNo());
                            unitEop.setUnitNo(sourceSistarRtSteps.getUnitNo());
                            unitEop.setEopNo(sourceSistarRtSteps.getEopNo());
                            unitEop.setEopName(sourceSistarRtSteps.getEopName());
                            unitEopAddList.add(unitEop);
                            unitEopMap.put(keyEop, unitEop);
                        }else{
                            PiAdapterUnitEop unitEop = unitEopMap.get(keyEop);
                            //判断名称是否相同
                            if(!unitEop.getEopName().equals(sourceSistarRtSteps.getEopName())){
                                //更新名称
                                unitEop.setEopName(sourceSistarRtSteps.getEopName());
                                unitEopAddList.add(unitEop);
                                unitEopMap.put(keyEop, unitEop);
                            }
                        }
                    }
                    // 更新设备
                    iPiAdapterUnitService.saveOrUpdateBatch(unitAddList);
                    // 更新设备与EOP对照
                    //iPiAdapterUnitEopService.saveOrUpdateBatch(unitEopAddList);
                    for (PiAdapterUnitEop unitEop : unitEopAddList) {
                        PiAdapterUnitEop exist = iPiAdapterUnitEopService.lambdaQuery()
                                .eq(PiAdapterUnitEop::getAreaNo, unitEop.getAreaNo())
                                .eq(PiAdapterUnitEop::getPcuNo, unitEop.getPcuNo())
                                .eq(PiAdapterUnitEop::getUnitNo, unitEop.getUnitNo())
                                .eq(PiAdapterUnitEop::getEopNo, unitEop.getEopNo())
                                .one();
                        if (exist != null) {
                            unitEop.setId(exist.getId());
                            iPiAdapterUnitEopService.updateById(unitEop);
                        } else {
                            iPiAdapterUnitEopService.save(unitEop);
                        }
                    }

                }
            } catch (Exception e) {
                e.printStackTrace();
            }

        }

        // 更新抽取记录
        if (totalProcessed > 0) {
            //iPiSistarRtStepLogService.updateLastLoadTime("0", lastRecordTime, tenantId);
            log.info("参数分页同步完成，共处理 {} 条数据", totalProcessed);
        }

    }


    /**
     * 处理批量数据
     */
    private int processBatchData(List<SourceSistarRtSteps> dataList) {
        log.info("步骤数据处理1--------------{}", dataList.size());
        if (dataList.isEmpty()) {
            return 0;
        }
        // 转换数据
        List<PiSistarRtSteps> targetList = dataList.stream()
                .map(this::convertToTarget)
                .collect(Collectors.toList());

        // 批量插入
        //saveBatch(targetList);
        saveOrUpdateBatch(targetList);

        //计算
        try {
            iPiAdapterAlgorithmService.calculate(targetList, null);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return targetList.size();
    }

    /**
     * 数据转换
     */
    private PiSistarRtSteps convertToTarget(SourceSistarRtSteps source) {
        //对象复制
        PiSistarRtSteps target = BeanUtil.copyProperties(source, PiSistarRtSteps.class);
        target.setTenantId(tenantId);
        // 拼接ID
        String id = buildCompleteId(source);
        target.setId(id);

        return target;
    }

    /**
     * 生成ID
     * @param source
     * @return
     */
    private String buildCompleteId(SourceSistarRtSteps source) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
        String formattedTime = source.getStartTime() != null ?
                source.getStartTime().toInstant()
                        .atZone(ZoneId.systemDefault())
                        .format(formatter) : "00000000000000";

        return String.join("_",
                source.getSiteNo() != null ? source.getSiteNo().toString() : "NULL_SITE",
                source.getAreaNo() != null ? source.getAreaNo().toString() : "NULL_AREA",
                source.getYearNo() != null ? source.getYearNo().toString() : "NULL_YEAR",
                source.getReccatNo() != null ? source.getReccatNo().toString() : "NULL_RECCAT",
                source.getOrderNo() != null ? source.getOrderNo().toString() : "NULL_ORDER",
                source.getBatchNo() != null ? source.getBatchNo().toString() : "NULL_BATCH",
                source.getPcuNo() != null ? source.getPcuNo().toString() : "NULL_PCU",
                source.getUnitNo() != null ? source.getUnitNo().toString() : "NULL_UNIT",
                source.getEopNo() != null ? source.getEopNo().toString() : "NULL_EOP",
                formattedTime,
                source.getRopId() != null ? source.getRopId().toString() : "NULL_ROP",
                source.getUserProtId() != null ? source.getUserProtId().toString() : "NULL_USERPROT"
        );
    }


    /**
     * 清空 MyBatis 缓存
     */
    public void clearMyBatisCache() {
        // 实现清空缓存的逻辑
        try {
            // 创建新的 SqlSession 并清空缓存
            try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
                sqlSession.clearCache();
            }
            // 可选：执行垃圾回收
            System.gc();
            log.debug("MyBatis缓存已清理");
        } catch (Exception e) {
            log.warn("清理缓存失败: {}", e.getMessage());
        }
    }

    /**
     * 字典数据同步
     */
    @Override
    public void dictionaryData() {
        try {
            //同步产线(sistar_eng_reccats)
            List<PiSistarEngReccats> sistarEngReccatsList = iSourceSistarRtStepsService.getReccats();
            //清空旧数据
            iSourceSistarRtStepsService.deleteData("mes3_pi_sistar_eng_reccats");
            sistarEngReccatsList.forEach(item -> item.setTenantId(tenantId));
            iPiSistarEngReccatsService.saveBatch(sistarEngReccatsList);
        } catch (Exception e) {
            e.printStackTrace();
        }


        try {
            //同步sistar_eng_pcus
            List<PiSistarEngPcus> sistarEngPcusList = iSourceSistarRtStepsService.getPcus();
            //清空旧数据
            iSourceSistarRtStepsService.deleteData("mes3_pi_sistar_eng_pcus");
            sistarEngPcusList.forEach(item -> item.setTenantId(tenantId));
            iPiSistarEngPcusService.saveBatch(sistarEngPcusList);
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            //同步sistar_eng_units
            List<PiSistarEngUnits> sistarEngUnitsList = iSourceSistarRtStepsService.getUnits();
            //清空旧数据
            iSourceSistarRtStepsService.deleteData("mes3_pi_sistar_eng_units");
            sistarEngUnitsList.forEach(item -> item.setTenantId(tenantId));
            iPiSistarEngUnitsService.saveBatch(sistarEngUnitsList);
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            //同步sistar_eng_eops
            List<PiSistarEngEops> sistarEngEopsList = iSourceSistarRtStepsService.getEops();
            //清空旧数据
            iSourceSistarRtStepsService.deleteData("mes3_pi_sistar_eng_eops");
            sistarEngEopsList.forEach(item -> item.setTenantId(tenantId));
            iPiSistarEngEopsService.saveBatch(sistarEngEopsList);
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            //同步sistar_eng_recipes
            List<PiSistarEngRecipes> sistarEngRecipesList = iSourceSistarRtStepsService.getRecipes();
            //清空旧数据
            iSourceSistarRtStepsService.deleteData("mes3_pi_sistar_eng_recipes");
            sistarEngRecipesList.forEach(item -> item.setTenantId(tenantId));
            iPiSistarEngRecipesService.saveBatch(sistarEngRecipesList);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 获取设备点位
     * @param piSistarRtSteps
     * @return
     */
    @Override
    public DevicePointVo unitAndPoint(PiSistarRtSteps piSistarRtSteps) {
        String unitNames = piSistarRtSteps.getUnitNames();
        String orderAndBatch = piSistarRtSteps.getOrderAndBatch();
        String[] orderBatchTime = orderAndBatch.split(",");
        //订单批次
        List<String> orderBatchNos = new ArrayList<>();
        //订单批次-时间段Map
        Map<String, String> orderBatchMpa = new HashMap<>();
        //订单批次
        for (String orderBatchTimeStr : orderBatchTime) {
            String[] orderBatchTimeArr = orderBatchTimeStr.split("&");
            orderBatchNos.add(orderBatchTimeArr[0]);
            orderBatchMpa.put(orderBatchTimeArr[0], orderBatchTimeArr[1]);
        }
        //pcu集合
        String pcuNos = piSistarRtSteps.getPcuNos();
        String[] pcuNoArr = null;
        if(pcuNos.contains(",")){
            pcuNoArr = pcuNos.split(",");
        }
        //设备名称集合
        String [] unitName = unitNames.split(",");
        // 获取设备(步骤中获取设备)
        LambdaQueryWrapper<PiSistarRtSteps> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PiSistarRtSteps::getAreaNo, piSistarRtSteps.getAreaNo());
        if(pcuNoArr != null && pcuNoArr.length > 0){
            wrapper.in( PiSistarRtSteps::getPcuNo, pcuNoArr);
        }else{
            wrapper.eq(PiSistarRtSteps::getPcuNo, pcuNos);
        }
        wrapper.eq(PiSistarRtSteps::getRecipeNo, piSistarRtSteps.getRecipeNo());
        wrapper.eq(PiSistarRtSteps::getRecipeName, piSistarRtSteps.getRecipeName());
        wrapper.eq(PiSistarRtSteps::getReccatNo, piSistarRtSteps.getReccatNo());
        wrapper.notLike(PiSistarRtSteps::getRecipeName,"CIP");

        //设备-查询条件
        if(StrUtil.isNotBlank(piSistarRtSteps.getUnitId())){
            //判断是否包含,
            if(piSistarRtSteps.getUnitId().contains(",")){
                String [] unitIdArr = piSistarRtSteps.getUnitId().split(",");
                wrapper.and(wr -> {
                    for (String unitIdStr : unitIdArr) {
                        String[] parts = unitIdStr.split("-");
                        wr.or(w -> w.eq(PiSistarRtSteps::getUnitNo, parts[1])
                                .eq(PiSistarRtSteps::getPcuNo, parts[2]));
                    }
                });
                //wrapper.in(PiSistarRtSteps::getUnitNo, piSistarRtSteps.getUnitId().split(","));
            }else {
                String[] parts = piSistarRtSteps.getUnitId().split("-");
                wrapper.eq(PiSistarRtSteps::getUnitNo, parts[1]);
                wrapper.eq(PiSistarRtSteps::getPcuNo, parts[2]);
            }
        }else{
            if (unitName!=null && unitName.length > 0) {
                wrapper.and(w -> {
                    for (String name : unitName) {
                        w.or().like(PiSistarRtSteps::getUnitName, name);
                    }
                });
            }
        }

        //订单-批次查询条件
        wrapper.and(wr -> {
            for (String orderBatch : orderBatchNos) {
                String[] parts = orderBatch.split("-");
                wr.or(w -> w.eq(PiSistarRtSteps::getOrderNo, parts[0])
                        .eq(PiSistarRtSteps::getBatchNo, parts[1]));
            }
        });
        wrapper.orderByAsc(PiSistarRtSteps::getEventTime);
        //步骤列表
        List<PiSistarRtSteps> stepList = list(wrapper);


        //先按区域 areaId 和设备 unitId 分类，再在每个分类下按 订单号 orderId 和批次号 batchNr 分类
        Map<String, Map<String, List<PiSistarRtSteps>>> stepGrouped = stepList.stream()
                // 先按区域-设备分类
                .collect(Collectors.groupingBy(
                        step -> String.format("%s-%s-%s", step.getAreaNo(), step.getUnitNo(), step.getPcuNo()),
                        // 在每个区域-设备下再按订单-批次分类
                        Collectors.groupingBy(
                                step -> String.format("%s-%s", step.getOrderNo(), step.getBatchNo())
                        )
                ));

        //获取时间跨度最大的步骤
        Map<String, PiSistarRtSteps> maxStep = maxDuration(stepList);
        //步骤通过设备分组
        Map<String, List<StepVo>> stepUnitGroupMap = stepList.stream()
                .collect(Collectors.groupingBy(
                        step -> String.format("%s-%s-%s", step.getAreaNo(), step.getUnitNo(), step.getPcuNo()),
                        Collectors.mapping(
                                step -> {
                                    // 这里实现 PiSistarRtSteps 到 StepVo 的转换
                                    StepVo stepVo = new StepVo();
                                    stepVo.setId(step.getId());
                                    stepVo.setStartTime(step.getStartTime());
                                    stepVo.setEndTime(step.getEndTime());
                                    stepVo.setOrderNo(Integer.parseInt(step.getOrderNo()));
                                    stepVo.setBatchNo(Integer.parseInt(step.getBatchNo()));
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
                                    PiSistarRtSteps max = maxStep.get(entry.getKey());
                                    if (max == null) {
                                        return Collections.emptyList();
                                    }

                                    Integer orderId = Integer.parseInt(max.getOrderNo());
                                    Integer batchNr = Integer.parseInt(max.getBatchNo());

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
        List<PiSistarRtSteps> stepDedupList = distinctByAllFieldsStream(stepList);
        //获取曲线步骤配置
        List<PiCurveStep> curveStepList = iPiCurveStepService.batchQueryByStepsSql(stepDedupList);
        //遍历 curveStepList 得到map  key为  areaNo,yearNo,reccatNo,pcuNo,unitNo,recipeNo 值为 excludeRopIds
        Map<String, String> excludeRopIdsMap = curveStepList.stream()
                .collect(Collectors.toMap(
                        item -> String.format("%s-%s-%s-%s-%s",
                                item.getAreaNo(),
                                //item.getYearNo(),
                                item.getReccatNo(),
                                item.getPcuNo(),
                                item.getUnitNo(),
                                item.getRecipeNo()),  // 添加了 recipeNo
                        PiCurveStep::getExcludeRopIds,
                        (existing, replacement) -> existing
                ));
        //excludeRopIds 存储了ropId集合 逗号拼接(排除步骤)
        stepList = filterExcludedStepsOptimized(stepList, excludeRopIdsMap);

        //分组 (区域_设备_PCU_订单_批次)
        Map<String, PiSistarRtSteps> stepGroupMap = stepGroup(stepList);

        //步骤 设备去重 unitNo和pointName 不能相同
        List<PiSistarRtSteps> distinctList = stepList.stream()
            .collect(Collectors.collectingAndThen(
                Collectors.toCollection(() ->
                    new TreeSet<>(Comparator.comparing((PiSistarRtSteps p) -> p.getUnitNo())
                        .thenComparing(p -> p.getUnitName()))
                ),
                ArrayList::new
            ));

        //获取设备Code集合
        List<String> unitNos = distinctList.stream()
            .map(item -> String.format("%s-%s-%s", item.getAreaNo(), item.getUnitNo(), item.getPcuNo()))
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
        pointWrapper.orderByAsc(PiBasePoint::getSort);
        List<PiBasePoint> pointList = piBasePointService.list(pointWrapper);


        //
        Map<String,Boolean> deviceMap = new HashMap<String,Boolean>();
        //设备id 和订单批次
        Map<String, ArrayList<Map<String, String>>> deviceIdToOrderBatchMap = new HashMap<>();
        for (PiSistarRtSteps sistarRtSteps : stepList) {
            //设备code (区域_设备id)
            String deviceCode = sistarRtSteps.getAreaNo()+"-"+sistarRtSteps.getUnitNo()+"-"+sistarRtSteps.getPcuNo();
            String deviceId = deviceIdToCodeMap.get(deviceCode)+"";
            String key = sistarRtSteps.getOrderNo()+"-"+sistarRtSteps.getBatchNo();
            String orderBatchDate = orderBatchMpa.get(key);
            //获取分组值
            PiSistarRtSteps stepGroupTime = stepGroupMap.get(deviceCode+"-"+key);
            if(StrUtil.isNotBlank(deviceId) && StrUtil.isNotBlank(orderBatchDate) && !deviceMap.containsKey(deviceId+"_"+key) && stepGroupTime != null){
                deviceMap.put(deviceId+"_"+key, true);
                Map<String, String> map = new HashMap<>();
                //String[] dates= orderBatchDate.split("_");
                map.put("orderBatch", key);
//                map.put("startDate",dates[0]);
//                map.put("endDate", dates[1]);
                if (stepGroupTime.getStartTime() != null && stepGroupTime.getEndTime() != null){
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

        //匹配标准曲线配置
        //步骤分组-stepList (PCU_设备_配方_步骤号_ropId)
        Map<String, List<PiSistarRtSteps>> stepEopGroup = stepEopGroup(stepList);
        //获取标准配置 (步骤 和 点位code[pointList])
        List<PiCurveStandard> piCurveStandardList = iPiCurveStandardService.getCurveStandard(stepEopGroup, pointList);
        //标准分组 (设备 --> id_参数_点位集合)
//        Map<String, List<String>> standardGroupMap = standardGroup(piCurveStandardList);
        Map<String, List<PiCurveStandard>> standardGroupMap = standardGroup(piCurveStandardList);
        //写入返回集合 点位code


        List<PiSistarEngUnits> unitList = new ArrayList<>();
        //区域
        String areaNo = "";
        //拼接数据 设备 设备点位
        for (MdmDevice mdmDevice : deviceList) {
            String deviceCode = "";
            //判断设备编号是否包含- 包含进行拆分
            if (mdmDevice.getDeviceCode().contains("-")){
                String[] deviceCodeArr = mdmDevice.getDeviceCode().split("-");
                deviceCode = deviceCodeArr[1];
                areaNo = deviceCodeArr[0];
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

        //匹配局部标准配置 (pcuNo + reccatNo + recipeNo + unitNo + eopNo + ropId)
        final List<PiSistarRtSteps> finalStepList = stepList;
        LambdaQueryWrapper<PiCurvePartialStandard> partialWrapper = new LambdaQueryWrapper<>();
        partialWrapper.and(main -> {
            for (PiSistarRtSteps step : finalStepList) {
                main.or(w -> w
                    .eq(PiCurvePartialStandard::getPcuNo, String.valueOf(step.getPcuNo()))
                    .eq(PiCurvePartialStandard::getReccatNo, String.valueOf(step.getReccatNo()))
                    .eq(PiCurvePartialStandard::getRecipeNo, String.valueOf(step.getRecipeNo()))
                    .eq(PiCurvePartialStandard::getUnitNo, step.getUnitNo())
                    .eq(PiCurvePartialStandard::getEopNo, String.valueOf(step.getEopNo()))
                    .eq(PiCurvePartialStandard::getRopId, String.valueOf(step.getRopId()))
                );
            }
        });
        List<PiCurvePartialStandard> partialStandardList = iPiCurvePartialStandardService.list(partialWrapper);
        //局部标准按区域-设备-pcu分组
        String areaNoTmp = areaNo;
        Map<String, List<PiCurvePartialStandard>> partialStandardMap = partialStandardList.stream()
                .collect(Collectors.groupingBy(p -> String.format("%s-%s-%s",areaNoTmp, p.getUnitNo(), p.getPcuNo())));

        // 设备分组-步骤配置 id 集合
        Map<String, List<String>> configStepGroupMap = stepList.stream()
            .collect(Collectors.groupingBy(
                step -> String.format("%s-%s-%s", step.getAreaNo(), step.getUnitNo(), step.getPcuNo()),
                Collectors.mapping(
                        PiSistarRtSteps::getId,  // 提取 id
                        Collectors.toList()  // 收集为 List
                )
            ));

        DevicePointVo devicePointVo = new DevicePointVo();
        devicePointVo.setUnitList(unitList);

        //设备步骤分组 设备-步骤集合
        devicePointVo.setStepGroupMap(stepUnitGroupMap);
        //设备步骤分组 设备-选中步骤集合
        devicePointVo.setConfigStepsMap(configStepGroupMap);
        //步骤分组
        devicePointVo.setAdapterStepGrouped(stepGrouped);
        //局部标准配置(按设备分组)
        devicePointVo.setPartialStandardMap(partialStandardMap);
        return devicePointVo;
    }

    //步骤去重
    public List<PiSistarRtSteps> distinctByAllFieldsStream(List<PiSistarRtSteps> stepList) {
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
    private String buildCompositeKey(PiSistarRtSteps step) {
        return String.format("%s-%s-%s-%s-%s",
                step.getAreaNo(),
                //step.getYearNo(),
                step.getReccatNo(),
                step.getPcuNo(),
                step.getUnitNo(),
                step.getRecipeNo()
        );
    }

    /**
     * 步骤分组
     * 区域_设备_PCU_订单_批次
     * 开始时间 结束时间
     * @param stepList
     * @return
     */
    public Map<String, PiSistarRtSteps> stepGroup(List<PiSistarRtSteps> stepList) {
        Map<String, PiSistarRtSteps> resultMap =
            stepList.stream()
                .collect(Collectors.toMap(
                    s -> s.getAreaNo() + "-" +
                            s.getUnitNo() + "-" +
                            s.getPcuNo() + "-" +
                            s.getOrderNo() + "-" +
                            s.getBatchNo(),
                    Function.identity(),
                    (a, b) -> {
                        // 开始时间取最小
                        if (a.getStartTime() == null ||
                                (b.getStartTime() != null && a.getStartTime().after(b.getStartTime()))) {
                            a.setStartTime(b.getStartTime());
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
     * 步骤分组-stepList (PCU_设备_配方_步骤号_ropId)
     */
    public Map<String, List<PiSistarRtSteps>> stepEopGroup(List<PiSistarRtSteps> stepList) {
        Map<String, List<PiSistarRtSteps>> stepEopGroup = stepList.stream()
            .collect(Collectors.groupingBy(
                step -> step.getPcuNo() + "-" +
                        step.getUnitNo() + "-" +
                        step.getRecipeNo() + "-" +
                        step.getEopNo() + "-" +
                        step.getRopId()
            ));
        return stepEopGroup;
    }
    /**
     * 标准分组 设备 --> id_参数_点位集合
     * @param piCurveStandardList
     * @return
     */
//    public Map<String, List<String>> standardGroup(List<PiCurveStandard> piCurveStandardList) {
//        //设备分组  值为 (id_参数_点位)
//        Map<String, List<String>> standardGroup = piCurveStandardList.stream()
//            .collect(Collectors.groupingBy(
//                standard -> standard.getUnitNo(),
//                Collectors.mapping(
//                    standard -> standard.getId() + "_" + standard.getParam() + "_" + standard.getPointCode(),
//                    Collectors.toList()
//                )
//            ));
//        return standardGroup;
//    }

    public Map<String, List<PiCurveStandard>> standardGroup(List<PiCurveStandard> piCurveStandardList) {
        //设备分组
        return piCurveStandardList.stream().collect(Collectors.groupingBy(PiCurveStandard::getUnitNo));
    }

    /**
     * 保存步骤配置
     * @param piCurveStep
     */
    @Override
    public void stepsSave(PiCurveStep piCurveStep) {
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
        //排除得 roop_id集合
        String excludeRopIds = "";
        String[] existiongArray = StringUtils.split(piCurveStep.getExcludeIds(), ",");

        List<PiSistarRtSteps> stepList = new ArrayList<>();
        if(piCurveStep.getCurveDataType().equals("1")){
            // adapter 步骤
            stepList = listByIds(Arrays.asList(stepIdArray));
            if (CollectionUtils.isEmpty(stepList)) {
                return;
            }
        }else{
            //pi 步骤
            //stepList = iPiCurveConfigService.getDataById(Arrays.asList(stepIdArray), piCurveStep);
            //包含"-"
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
            PiCurveStepVo piCurveStepVo =iPiCurveConfigService.getData(piCurveStepDto);
            List<PiCurveStepDto> piStepList = piCurveStepVo.getStepList();

            List<String> ids = Arrays.asList(stepIdArray);

            for (PiCurveStepDto step : piStepList) {
                if(step.getId() != null && ids.contains(step.getId().toString())){
                    PiSistarRtSteps sistarRtStep = new PiSistarRtSteps();
                    sistarRtStep.setAreaNo(piCurveStep.getAreaNo());
                    sistarRtStep.setPcuNoStr(piCurveStep.getPcuNo());
                    String unitNo = step.getUnitNo();
                    if(unitNo.contains("/")){
                        unitNo = unitNo.split("/")[1];
                    }
                    sistarRtStep.setUnitNo(unitNo);
                    sistarRtStep.setRecipeNo(Integer.parseInt(step.getRecipeNo()));
                    sistarRtStep.setRopId(Integer.parseInt(step.getEopNo()));
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
        if (StrUtil.isNotBlank(unitNo) && unitNo.contains("-")) {
            unitNo = StrUtil.subBetween(unitNo, "-", "-");
        }
        Integer recipeNo = piCurveStep.getRecipeNo();
        for (PiSistarRtSteps step : stepList) {
            if (areaNo == null) {
                areaNo = step.getAreaNo();
            }
//            if (yearNo == null) {
//                yearNo = step.getYearNo();
//            }
            if (reccatNo == null) {
                reccatNo = step.getReccatNo();
            }
            if (StringUtils.isBlank(pcuNo)) {
                pcuNo = step.getPcuNos();
            }
            if (StrUtil.isBlank(unitNo)) {
                unitNo = step.getUnitNo();
            }
            if (recipeNo == null) {
                recipeNo = step.getRecipeNo();
            }
            //stepConfig.add(String.valueOf(step.getEopNo()));
            stepConfig.add(String.valueOf(step.getRopId()));
        }

        if(piCurveStep.getCurveDataType().equals("1")) {
            //adapter
            //获取到设备编号 unitNo "2-7-12" 得到 7
            String tankNo = null;
            if (StrUtil.isNotBlank(unitNo) && unitNo.contains("-")) {
                String[] parts = unitNo.split("-");
                if (parts.length >= 2) {
                    tankNo = parts[1];
                }
            }

            if (existiongArray == null || existiongArray.length == 0) {
                excludeRopIds = "";
            } else {
                //直接使用数组转换
                List<PiSistarRtSteps> excludeStepList = listByIds(Arrays.asList(existiongArray));
                if (CollectionUtils.isEmpty(excludeStepList)) {
                    return;
                }
                StringJoiner excludeStepConfig = new StringJoiner(",");
                for (PiSistarRtSteps step : excludeStepList) {
                    excludeStepConfig.add(String.valueOf(step.getRopId()));
                }
                excludeRopIds = excludeStepConfig.toString();
            }
        }
        // 更新/新增
        iPiCurveStepService.saveOrUpdateByQuery(stepConfig.toString(),excludeRopIds , areaNo, reccatNo, pcuNo, unitNo,
                recipeNo,
                piCurveStep.getCurveDataType());
    }

    /**
     * 获取步骤数据
     * @param piSistarRtSteps
     * @return
     */
    @Override
    public StepDataVo getData(PiSistarRtSteps piSistarRtSteps) {
        String unitNo = piSistarRtSteps.getUnitNo();
        StepDataVo stepDataVO = new StepDataVo();
        if(unitNo == null){
            return stepDataVO;
        }

        //步骤集合
        LambdaQueryWrapper<PiSistarRtSteps> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PiSistarRtSteps::getAreaNo, piSistarRtSteps.getAreaNo());
        wrapper.ge(PiSistarRtSteps::getStartTime, piSistarRtSteps.getStartTime());
        wrapper.le(PiSistarRtSteps::getStartTime, piSistarRtSteps.getEndTime());

        wrapper.eq(piSistarRtSteps.getReccatNo() != null,PiSistarRtSteps::getReccatNo, piSistarRtSteps.getReccatNo());
        wrapper.eq(piSistarRtSteps.getRecipeNo() != null,PiSistarRtSteps::getRecipeNo, piSistarRtSteps.getRecipeNo());
        wrapper.eq(piSistarRtSteps.getOrderNo() != null,PiSistarRtSteps::getOrderNo, piSistarRtSteps.getOrderNo());

        wrapper.eq(PiSistarRtSteps::getBatchNo, piSistarRtSteps.getBatchNo());
        wrapper.eq(PiSistarRtSteps::getPcuNo, piSistarRtSteps.getPcuNo());
        wrapper.notLike(PiSistarRtSteps::getRecipeName,"CIP");
        wrapper.eq(PiSistarRtSteps::getUnitNo, unitNo);
        wrapper.orderByAsc(PiSistarRtSteps::getStartTime);
        List<PiSistarRtSteps> list = list(wrapper);

        if(list != null && !list.isEmpty()){
            PiSistarRtSteps piSistarRtStep = list.get(0);
            //获取第一条的开始时间
            Date startTime = piSistarRtStep.getStartTime();
            //获取最后一条的结束时间
            Date endTime = list.get(list.size() - 1).getEndTime();
            stepDataVO.setStartTime(startTime);
            stepDataVO.setEndTime(endTime);
            stepDataVO.setStepList(list);
            stepDataVO.setRecipeName(piSistarRtStep.getRecipeName());

            //获取步骤配置
            PiCurveStep curveStepList = iPiCurveStepService.getCurveStep(piSistarRtStep);
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

            //匹配曲线局部配置 (pcuNo + reccatNo + recipeNo + unitNo + eopNo + ropId)
            LambdaQueryWrapper<PiCurvePartialStandard> partialWrapper = new LambdaQueryWrapper<>();
            partialWrapper.and(main -> {
                for (PiSistarRtSteps step : list) {
                    main.or(w -> w
                        .eq(PiCurvePartialStandard::getPcuNo, String.valueOf(step.getPcuNo()))
                        .eq(PiCurvePartialStandard::getReccatNo, String.valueOf(step.getReccatNo()))
                        .eq(PiCurvePartialStandard::getRecipeNo, String.valueOf(step.getRecipeNo()))
                        .eq(PiCurvePartialStandard::getUnitNo, step.getUnitNo())
                        .eq(PiCurvePartialStandard::getEopNo, String.valueOf(step.getEopNo()))
                        .eq(PiCurvePartialStandard::getRopId, String.valueOf(step.getRopId()))
                    );
                }
            });
            List<PiCurvePartialStandard> partialStandardList = iPiCurvePartialStandardService.list(partialWrapper);
            stepDataVO.setPartialStandardList(partialStandardList);

        }
        return stepDataVO;
    }

    /**
     * 获取设备
     * @param piSistarRtSteps
     * @return
     */
    @Override
    public List<DeviceCurveVo> getDevice(PiSistarRtSteps piSistarRtSteps) {
        String unitNames = piSistarRtSteps.getUnitNames();
        //获取设备
        LambdaQueryWrapper<MdmDevice> wrapperDevice = new LambdaQueryWrapper<>();
        wrapperDevice.like(MdmDevice::getDeviceCode, "-");
        // 排序
        wrapperDevice.orderByAsc(MdmDevice::getSort);
        List<MdmDevice> deviceList = mdmDeviceService.list(wrapperDevice);

        //区域PCU
        String pcuNos = piSistarRtSteps.getPcuNos();
        String[] pcuNoArr = null;
        if(pcuNos != null && pcuNos.contains(",")){
            pcuNoArr = pcuNos.split(",");
        }
        LambdaQueryWrapper<PiSistarRtSteps> wrapper = new LambdaQueryWrapper<>();
        wrapper.ge(PiSistarRtSteps::getEventTime, piSistarRtSteps.getEventTime());
        wrapper.le(PiSistarRtSteps::getEventTime, piSistarRtSteps.getEndTime());
        wrapper.notLike(PiSistarRtSteps::getRecipeName,"CIP");
        if(pcuNoArr != null && pcuNoArr.length > 0){
            wrapper.in(PiSistarRtSteps::getPcuNo, pcuNoArr);
        }else{
            wrapper.eq(StrUtil.isNotBlank(pcuNos), PiSistarRtSteps::getPcuNo, pcuNos);
        }
        //设备-查询条件
        if(StrUtil.isNotBlank(unitNames)) {
            String[] unitName = unitNames.split(",");
            if (unitName != null && unitName.length > 0) {
                wrapper.and(w -> {
                    for (String name : unitName) {
                        w.or().like(PiSistarRtSteps::getUnitName, name);
                    }
                });
            }
        }
        wrapper.orderByAsc(PiSistarRtSteps::getUnitNo);

        List<PiSistarRtSteps> list = list(wrapper);
        //List<DeviceCurveVo> filteredDevices = new ArrayList<>();
        Set<String> set = new HashSet<>();
        int curveDataType = 1;
        //
        List<PiCurveConfig> piCurveConfigList = iPiCurveConfigService.getCurveConfigByArea(piSistarRtSteps.getPcuNos());

        if(list != null && !list.isEmpty() && (piCurveConfigList == null || piCurveConfigList.isEmpty())){
            //遍历list 得到 区域-设备-pcu set集合
            for (PiSistarRtSteps rtStep : list) {
                set.add(rtStep.getAreaNo()+"-"+rtStep.getUnitNo()+"-"+rtStep.getPcuNo());
            }
        } else {
            //没有设备查找PI配置
            //List<PiCurveConfig> piCurveConfigList = iPiCurveConfigService.getCurveConfigByArea(piSistarRtSteps.getPcuNos());
            for (PiCurveConfig piCurveConfig : piCurveConfigList) {
                set.add(piCurveConfig.getDeviceNo());
            }
            curveDataType = 3;
        }
        int dataType = curveDataType;
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



    //过滤步骤
    public List<PiSistarRtSteps> filterExcludedStepsOptimized(
            List<PiSistarRtSteps> stepList,
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

    private boolean isExcludedOptimized(PiSistarRtSteps step, Map<String, Set<String>> excludeMap) {
        String key = buildCompositeKey(step);
        Set<String> excludeSet = excludeMap.get(key);

        if (excludeSet == null || excludeSet.isEmpty() || step.getRopId() == null) {
            return false;
        }

        return excludeSet.contains(String.valueOf(step.getRopId()));
    }

//    private String buildStepKey(PiSistarRtSteps step) {
//        return String.format("%d-%d-%d-%d-%d-%d",
//                step.getAreaNo(), step.getYearNo(), step.getReccatNo(),
//                step.getPcuNo(), step.getUnitNo(), step.getRecipeNo());
//    }

    @Override
    public List<PiSistarRtSteps> getRecipeByPcuNo(PiCurveStandard piCurveStandard) {
        List<String> list = new ArrayList();
        if(StrUtil.isNotBlank(piCurveStandard.getUnitNo())){
            list = Arrays.asList(piCurveStandard.getUnitNo().split(","));
        }
        List<String> areaNoList = new ArrayList<>();
        if (StrUtil.isNotBlank(piCurveStandard.getAreaNo())) {
            areaNoList = Arrays.asList(piCurveStandard.getAreaNo().split(","));
        }
        return baseMapper.getRecipeByPcuNo(areaNoList, list, piCurveStandard.getRecipeNo(), piCurveStandard.getRecipeName());
    }

    @Override
    public Object getStepByPcuNo(String pcuNo, String unitNo, String recipeNo, String reccatNo) {
        List<String> unitNoList = new ArrayList<>();
        if(StrUtil.isNotBlank(unitNo)){
            unitNoList = Arrays.asList(unitNo.split(","));
        }
        return baseMapper.getStepByPcuNo(pcuNo, unitNoList, recipeNo, reccatNo);
    }

    /**
     * 获取最大持续时间的步骤
     */
    private Map<String, PiSistarRtSteps> maxDuration(List<PiSistarRtSteps> stepList) {

        return stepList.stream()
                .collect(Collectors.groupingBy(
                        step -> step.getAreaNo() + "-" + step.getUnitNo()+ "-" + step.getPcuNo()
                ))
                .entrySet()
                .stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> entry.getValue().stream()
                                .collect(Collectors.groupingBy(
                                        s -> s.getOrderNo() + "-" + s.getBatchNo()
                                ))
                                .values()
                                .stream()
                                .map(batchSteps -> {

                                    Date start = batchSteps.stream()
                                            .map(PiSistarRtSteps::getStartTime)
                                            .filter(Objects::nonNull)
                                            .min(Date::compareTo)
                                            .orElse(null);

                                    Date end = batchSteps.stream()
                                            .map(PiSistarRtSteps::getEndTime)
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

                                    PiSistarRtSteps endStep = batchSteps.stream()
                                            .filter(s -> s.getEndTime() != null)
                                            .max(Comparator.comparing(PiSistarRtSteps::getEndTime))
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

    /**
     * 获取区域下的所有设备
     */
    @Override
    public List<PiSistarRtSteps> getStepsUnitByAreaNo(String areaNo, String recipeNo, String recipeName, String reccatNo) {
        return baseMapper.getStepsUnitByAreaNo(areaNo, recipeNo, recipeName, reccatNo);
    }
}
