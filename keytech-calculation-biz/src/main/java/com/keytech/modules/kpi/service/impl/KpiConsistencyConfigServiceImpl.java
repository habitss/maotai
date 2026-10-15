package com.keytech.modules.kpi.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.keytech.modules.pi.service.IPiCustomizeTableService;
import com.keytech.modules.pi.dto.TableConditionQueryDto;
import org.jeecg.common.exception.JeecgBootException;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.keytech.base.service.impl.BaseServiceImpl;
import com.keytech.modules.kpi.dto.BatchConfigDto;
import com.keytech.modules.kpi.dto.DataItemDto;
import com.keytech.modules.kpi.dto.KpiYearMonthDataDto;
import com.keytech.modules.kpi.entity.KpiConsistencyConfig;
import com.keytech.modules.kpi.entity.KpiConsistencyReport;
import com.keytech.modules.kpi.entity.KpiReportActualDayValue;
import com.keytech.modules.kpi.mapper.KpiConsistencyConfigMapper;
import com.keytech.modules.kpi.service.IKpiConsistencyConfigService;
import com.keytech.modules.kpi.service.IKpiConsistencyReportService;
import com.keytech.modules.kpi.service.IKpiReportActualDayValueService;
import com.keytech.modules.kpi.util.DateUtils;
import com.keytech.modules.mdm3.service.IMdmWeekService;
import com.keytech.modules.mdm3.entity.MdmWeek;
import com.keytech.modules.mdm3.service.IMdmWeekService;
import com.keytech.modules.pi.entity.PiAbnormalAnalysis;
import com.keytech.modules.pi.entity.PiApprove;
import com.keytech.modules.pi.entity.PiStandardData;
import com.keytech.modules.pi.service.IPiAbnormalAnalysisService;
import com.keytech.modules.pi.service.IPiApproveService;
import com.keytech.modules.pi.service.IPiStandardDataService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @Description: 一致KPI数据配置
 * @Author: jeecg-boot
 * @Date:   2026-03-24
 * @Version: V1.0
 */
@Service
@Slf4j
public class KpiConsistencyConfigServiceImpl extends BaseServiceImpl<KpiConsistencyConfigMapper, KpiConsistencyConfig> implements IKpiConsistencyConfigService {

    @Autowired
    private IKpiConsistencyReportService kpiConsistencyReportService;
    @Autowired
    private IKpiReportActualDayValueService kpiReportActualDayValueService;
    @Autowired
    private IPiStandardDataService piStandardDataService;
    @Autowired
    private IPiAbnormalAnalysisService piAbnormalAnalysisService;
    @Autowired
    private IMdmWeekService mdmWeekService;
    @Autowired
    private IPiCustomizeTableService iPiCustomizeTableService;
    @Autowired
    private IPiApproveService piApproveService;

    // 在类中注入或创建 ObjectMapper（推荐作为 Bean）
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 一致KPI数据计算定时任务
     *  batchFlag =1 批处理 beforeDays 前多少天, 计算前beforeDays 天数据
     */
    @Override
    public void extractData(Map map) {
        log.info("开始执行一致性KPI数据抽取任务");
        // 获取传入的日期(yyyy-MM-dd) batchFlag
        String batchFlag = map.get("batchFlag") != null ? map.get("batchFlag").toString() : "";
        if(StrUtil.isNotBlank(batchFlag)){
            //n天前数据
            String beforeDaysStr = map.get("beforeDays") != null ? map.get("beforeDays").toString() : "";
            //获取n天前内的所有周日
            int beforeDays = 0;
            try {
                beforeDays = Integer.parseInt(beforeDaysStr);
            } catch (NumberFormatException e) {
                log.error("beforeDays参数格式错误: {}", beforeDaysStr, e);
                return;
            }

            // 计算起始日期（今天减去beforeDays天）和结束日期（本周日，确保包含当前周）
            Calendar startCal = Calendar.getInstance();
            startCal.add(Calendar.DAY_OF_MONTH, -beforeDays);
            Calendar endCal = Calendar.getInstance();
            // 将结束日期延后到本周日，以包含当前周
            while (endCal.get(Calendar.DAY_OF_WEEK) != Calendar.SUNDAY) {
                endCal.add(Calendar.DAY_OF_MONTH, 1);
            }

            // 从起始日期遍历到结束日期，筛选出所有周日
            List<String> sundayList = new ArrayList<>();
            Calendar loopCal = Calendar.getInstance();
            loopCal.setTime(startCal.getTime());
            while (!loopCal.after(endCal)) {
                if (loopCal.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY) {
                    sundayList.add(DateUtils.formatDate(loopCal.getTime()));
                }
                loopCal.add(Calendar.DAY_OF_MONTH, 1);
            }
            log.info("近{}天内共有{}个周日: {}", beforeDays, sundayList.size(), sundayList);

            //遍历周日集合，逐个调用 extractWeekData 方法抽取对应周的数据
            for (String sunday : sundayList) {
                Map<String, Object> weekMap = new HashMap<>();
                weekMap.put("date", sunday);
                try {
                    log.info("开始处理周日[{}]的数据抽取", sunday);
                    extractWeekData(weekMap);
                } catch (Exception e) {
                    log.error("处理周日[{}]数据抽取失败", sunday, e);
                }
            }

        }else{
            //同步周
            extractWeekData(map);
        }

    }

    /**
     * 计算每周(当前周)
     * @param map
     * beforeDays 前n天
     * date 2026-05-17
     */
    public void extractWeekData(Map map) {
        log.info("开始执行一致性KPI数据抽取任务");
        // 获取传入的前n天参数
        String beforeDaysStr = map.get("beforeDays") != null ? map.get("beforeDays").toString() : "";
        // 获取传入的日期(yyyy-MM-dd)
        String dateStr = map.get("date") != null ? map.get("date").toString() : "";

        // date (判断传入的 date 是否为空  为空就取当前日期)
        Date queryDate;
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd");
        if (StrUtil.isBlank(dateStr)) {
            queryDate = new Date();
        } else {
            try {
                queryDate = sdf.parse(dateStr);
            } catch (Exception e) {
                log.error("日期格式解析失败，使用当前日期: {}", dateStr, e);
                queryDate = new Date();
            }
        }

        // 如果有beforeDays参数，将queryDate减去n天
        if (StrUtil.isNotBlank(beforeDaysStr)) {
            try {
                int beforeDays = Integer.parseInt(beforeDaysStr);
                Calendar cal = Calendar.getInstance();
                cal.setTime(queryDate);
                cal.add(Calendar.DAY_OF_MONTH, -beforeDays);
                queryDate = cal.getTime();
            } catch (NumberFormatException e) {
                log.error("beforeDays参数格式错误: {}", beforeDaysStr, e);
            }
        }



        // year 通过date 获取年
        Calendar cal = Calendar.getInstance();
        cal.setTime(queryDate);
        String year = String.valueOf(cal.get(Calendar.YEAR));

        MdmWeek mdmWeek = getWeek(queryDate, year);
        Date weekStartDate = mdmWeek.getStartDate();
        Date weekEndDate = mdmWeek.getEndDate();
        String month = mdmWeek.getMonth();
        // 月需要添加0
        month = month.length() == 1 ? "0" + month : month;
        String yearMonth = year + "-" + month;
        Integer weekNumber = mdmWeek.getWeekNumber();
        //周 w_2026_11
        String weekKey = "w_" + year + "_" + weekNumber;
        //结束日期 2023-03-07
        String weekEndDateStr = DateUtils.formatDate(weekEndDate);


        // 获取所有有效配置
        List<KpiConsistencyConfig> configList = lambdaQuery()
                .eq(KpiConsistencyConfig::getDelFlag, 0)
                .orderByAsc(KpiConsistencyConfig::getSort)
                .list();
        if (CollUtil.isEmpty(configList)) {
            log.info("未找到有效的一致性KPI配置数据");
            return;
        }
        log.info("获取到{}条配置数据", configList.size());

        // 为每个配置对象设置周日期
        for (KpiConsistencyConfig config : configList) {
            config.setWeekDate(weekEndDateStr);
        }


        // config按层级分组
        Map<Integer, List<KpiConsistencyConfig>> configGroupByLevel = configList.stream()
                .collect(Collectors.groupingBy(KpiConsistencyConfig::getLevel));

        List<KpiConsistencyConfig> rootList = configGroupByLevel.get(0);
        List<KpiConsistencyConfig> kpiList = configGroupByLevel.get(1);
        List<KpiConsistencyConfig> piTwoList = configGroupByLevel.get(2);

        //最上级写入KPI集合
        kpiList.addAll(rootList);

        // pi (配置)
        List<KpiConsistencyConfig> piList = configGroupByLevel.get(3);
        if (piList == null) {
            piList = new ArrayList<>();
        }
        // 将piTwoList有配置(needConfig=1) 写入piList 没有配置(needConfig=0) 写入notPiList
        List<KpiConsistencyConfig> notPiList = new ArrayList<>();
        if (CollUtil.isNotEmpty(piTwoList)) {
            for (KpiConsistencyConfig config : piTwoList) {
                if (config.getNeedConfig() != null && config.getNeedConfig() == 1) {
                    // 有配置，加入 piList
                    piList.add(config);
                } else {
                    // 没有配置，加入 notPiList
                    notPiList.add(config);
                }
            }
        }


        // 构建年月和周Key（使用 MdmWeek 中的数据）
//        String currentMonth = String.format("%02d", Integer.parseInt(month));
//        String yearMonth = year + "-" + currentMonth;
//        String weekKey = "w_" + year + "_" + weekNumber;

        Set<String> kpiCodeSet = new HashSet<String>();
        //kpi
        for (KpiConsistencyConfig kpi : kpiList) {
            String passRateConfig = kpi.getPassRateConfig();
            if (StrUtil.isNotBlank(passRateConfig)) {
                JSONObject jsonObject = JSONUtil.parseObj(passRateConfig);
                String kpiCode = jsonObject.getStr("kpiCode");
                // 获取PI周   N月年类  月累
                kpiCodeSet.add(kpiCode);
                kpi.setKpiMesCode(kpiCode);
            }
        }
        //获取KPI填报数据(kpi实际值)
        LambdaQueryWrapper<KpiReportActualDayValue> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.in(KpiReportActualDayValue::getKpiCode,kpiCodeSet);
        lambdaQueryWrapper.like(KpiReportActualDayValue::getYear,year);
        List<KpiReportActualDayValue> kpiReportActualDayValues = kpiReportActualDayValueService.list(lambdaQueryWrapper);

        Map<String, KpiYearMonthDataDto> kpiYearMonthDataDtoMap = new HashMap<>();
        if (CollUtil.isNotEmpty(kpiReportActualDayValues)) {
            for (KpiReportActualDayValue kpiReportActualDayValue : kpiReportActualDayValues) {
                String dataValueStr = kpiReportActualDayValue.getValue();
                if (StrUtil.isBlank(dataValueStr)) {
                    continue;
                }

                JSONObject parse = null;
                if (JSONUtil.isTypeJSON(dataValueStr)) {
                    parse = JSONUtil.parseObj(dataValueStr);
                } else {
                    continue;
                }

                KpiYearMonthDataDto dto = new KpiYearMonthDataDto();
                dto.setKpiCode(kpiReportActualDayValue.getKpiCode());
                // 月累 2026-03
                dto.setMonthCumulative(parse.getByPath(yearMonth, String.class));
                // 月年累 2026-03-yearAccumulate
                dto.setYearMonthCumulative(parse.getByPath(yearMonth + "-yearAccumulate", String.class));
                // 周 w_2026_11
                dto.setWeekCumulative(parse.getByPath(weekKey, String.class));

                kpiYearMonthDataDtoMap.put(kpiReportActualDayValue.getKpiCode(), dto);
            }
        }

        // kpi数据关联配置
        for (KpiConsistencyConfig kpi : kpiList) {
            KpiYearMonthDataDto dto = kpiYearMonthDataDtoMap.get(kpi.getKpiMesCode());
            if (dto != null){
                kpi.setMonthCumulative(dto.getMonthCumulative());
                kpi.setYearMonthCumulative(dto.getYearMonthCumulative());
                kpi.setPassRate(dto.getWeekCumulative());
            }
        }

        //3个月
        Calendar threeMonthsAgo = Calendar.getInstance();
        threeMonthsAgo.add(Calendar.MONTH, -3);

        // pi配置
        for (KpiConsistencyConfig kpiConsistencyConfig : piList) {
            String batchConfig = kpiConsistencyConfig.getBatchConfig();
            if (StrUtil.isNotBlank(batchConfig)) {
                try {
                    // 直接解析为 BatchConfig 对象
                    BatchConfigDto config = objectMapper.readValue(batchConfig, BatchConfigDto.class);
                    Integer type = config.getType();
                    List<DataItemDto> datasList = config.getDatas();
                    if(CollUtil.isEmpty(datasList)){
                        continue;
                    }

                    //遍历 datasList 集合得到报表名称集合
                    List<String> tableNames = datasList.stream()
                            .map(DataItemDto::getTableId)
                            .distinct()
                            .collect(Collectors.toList());
                    // 遍历datasList得到 tableNames+"-"+fieldId集合 后面需要判断是否存在
                    Set<String> tableFieldKeys = datasList.stream()
                            .map(d -> d.getTableId() + "-" + d.getFieldId())
                            .collect(Collectors.toSet());

                    // 获取剔除记录: table_name匹配、type=1(剔除)、del_flag=0(未删除)、state=1(审批通过)、创建时间3个月内
                    List<PiApprove> removeList = piApproveService.lambdaQuery()
                            .in(PiApprove::getTableName, tableNames)
                            .eq(PiApprove::getType, "1")
                            .eq(PiApprove::getState, "1")

                            .ge(PiApprove::getCreateTime, threeMonthsAgo.getTime())
                            .list();

                    Set<String> removeRowKeys = new HashSet<>();
                    Set<String> removeFieldKeys = new HashSet<>();
                    if (CollUtil.isNotEmpty(removeList)) {

                        //记录整行 pi报表名称(tableName)+"-"+dataId 存入集合方便下面判断是否存在
                        removeRowKeys = removeList.stream()
                                .filter(a -> "0".equals(a.getRemoveRange()))
                                .map(a -> a.getTableName() + "-" + a.getDataId())
                                .collect(Collectors.toSet());
                        //当前pi key pi报表名称(tableName)+"-"+dataId+"-"+pi  存入集合方便下面判断是否存在
                        final Set<String> finalTableFieldKeys = tableFieldKeys;
                        removeFieldKeys = removeList.stream()
                                .filter(a -> "1".equals(a.getRemoveRange()) && StrUtil.isNotBlank(a.getFields()))
                                .map(a -> a.getTableName() + "-" + a.getDataId()+"-" + a.getFields())
                                .collect(Collectors.toSet());

                        //剔除集合(整行/当前PI)
//                        removeRowKeys = removeList.stream()
//                            .map(a -> a.getTableName() + "-" + a.getDataId())
//                            .collect(Collectors.toSet());
                    }


                    // 累加变量（多条DataItemDto求和）
                    int sumGreenCount = 0;
                    int sumTotalCount = 0;
                    int sumRepeatCount = 0;
                    
                    for (DataItemDto data : datasList) {
                        //报表名称
                        final String tableName = data.getTableId();
                        //字段名
                        final String fieldId = data.getFieldId();
                        String timeFieldId = data.getTimeFieldId();

                        //构建查询条件：表名 + 时间字段范围条件(周开始~周结束)
                        TableConditionQueryDto conditionQueryDto = new TableConditionQueryDto();
                        conditionQueryDto.setTable(tableName);
                        conditionQueryDto.setFieldList(new ArrayList<>());
                        if (StrUtil.isNotBlank(timeFieldId)) {
                            String weekStartStr = DateUtils.formatDateTime(weekStartDate);
                            String weekEndStr = DateUtils.formatDateTime(weekEndDate);
                            // 结束日期需要设置为当天最后一刻，避免遗漏当天数据
                            weekEndStr = weekEndStr.replace(" 00:00:00", " 23:59:59");
                            conditionQueryDto.setCondition(timeFieldId + " >= '" + weekStartStr + "' and " + timeFieldId + " <= '" + weekEndStr + "'");
                        }

                        //获取PI数据 表 + 时间条件(周开始和结束)
                        List<Map<String, Object>> piDataList = iPiCustomizeTableService.getTableByCondition(conditionQueryDto);

                        //过滤掉剔除 piDataList  过滤 removeRowKeys(整行)
                        if (CollUtil.isNotEmpty(piDataList)) {
                            //过滤整行
                            final Set<String> finalRemoveRowKeys = removeRowKeys;
                            final Set<String> finalRemoveFieldKeys = removeFieldKeys;
                            piDataList.removeIf(piData -> finalRemoveRowKeys.contains(tableName + "-" + piData.get("id")));
                            //过滤剔除当前pi
                            piDataList.removeIf(piData -> finalRemoveFieldKeys.contains(tableName + "-" + piData.get("id")+"-"+fieldId));
                        }
                        //判断字段绿区
                        int greenCount = 0;
                        int totalCount = 0;
                        if (CollUtil.isNotEmpty(piDataList)) {
                            //批次总数
                            //totalCount = piDataList.size();
                            List<String> dataIds = piDataList.stream()
                                    .map(piData -> piData.get("id").toString())
                                    .collect(Collectors.toList());

                            // 批量查询 standard_json (标准计算结果记录(红黄区))
                            List<PiStandardData> standardDataList = piStandardDataService.lambdaQuery()
                                    .eq(PiStandardData::getTableName, tableName)
                                    .in(PiStandardData::getDataId, dataIds)
                                    .list();

                            // standardDataList 去除重复dataId取最新一条
                            standardDataList = standardDataList.stream()
                                    .collect(Collectors.groupingBy(PiStandardData::getDataId,
                                            Collectors.collectingAndThen(
                                                    Collectors.maxBy(Comparator.comparing(PiStandardData::getCreateTime)),
                                                    opt -> opt.orElse(null))))
                                    .values().stream()
                                    .filter(Objects::nonNull)
                                    .collect(Collectors.toList());

                            //piDataList 过滤没有匹配红黄区的
                            //totalCount = standardDataList.size();

                            if (CollUtil.isNotEmpty(standardDataList)) {
                                for (PiStandardData standardData : standardDataList) {
                                    String standardJson = standardData.getStandardJson();
                                    if (StrUtil.isNotBlank(standardJson)) {
                                        List<Map> fieldColorList = JSONUtil.toList(standardJson, Map.class);
                                        for (Map fieldColor : fieldColorList) {
                                            String field = fieldColor.get("field") != null ? fieldColor.get("field").toString() : "";
                                            String color = fieldColor.get("color") != null ? fieldColor.get("color").toString() : "";
                                            if (fieldId.equals(field)) {
                                                //批次总数有颜色才计算
                                                if(StrUtil.isNotBlank(color)){
                                                    totalCount++;
                                                }
                                                if ("green".equals(color)) {
                                                    greenCount++;
                                                }
                                                break;
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        log.info("表[{}] 字段[{}] 绿区数量: {}, 总数量: {}", tableName, fieldId, greenCount, totalCount);

                        //判断字段重复数量
                        int repeatCount = 0;
                        if (CollUtil.isNotEmpty(piDataList)) {
                            List<String> dataIds = piDataList.stream()
                                    .map(piData -> piData.get("id").toString())
                                    .collect(Collectors.toList());

                            List<PiAbnormalAnalysis> abnormalAnalysisList = piAbnormalAnalysisService.lambdaQuery()
                                    .eq(PiAbnormalAnalysis::getTableCode, tableName)
                                    .eq(PiAbnormalAnalysis::getFieldCode, fieldId)
                                    .in(PiAbnormalAnalysis::getDataId, dataIds)
                                    .eq(PiAbnormalAnalysis::getState, 1)
                                    .list();

                            repeatCount = CollUtil.isEmpty(abnormalAnalysisList) ? 0 : abnormalAnalysisList.size();
                        }
                        log.info("表[{}] 字段[{}] 重复异常数量: {}", tableName, fieldId, repeatCount);

                        // 累加
                        sumGreenCount += greenCount;
                        sumTotalCount += totalCount;
                        sumRepeatCount += repeatCount;
                    }

                    // 多条数据汇总后统一设置
                    kpiConsistencyConfig.setGreenBatchCount(sumGreenCount);
                    kpiConsistencyConfig.setTotalBatchCount(sumTotalCount);
                    kpiConsistencyConfig.setRepeatCount(sumRepeatCount);
                    // 合格率 = 绿区批次 / 总批次（保留两位小数）
                    if (sumTotalCount > 0) {
                        double passRateVal = sumGreenCount / (double) sumTotalCount * 100;
                        kpiConsistencyConfig.setPassRate(String.format("%.2f", passRateVal));
                    } else {
                        kpiConsistencyConfig.setPassRate("0.00");
                    }
                    // 重复率 = 重复数 / (总批次 - 绿区批次)（保留两位小数）
                    int nonGreenCount = sumTotalCount - sumGreenCount;
                    if (nonGreenCount > 0) {
                        double repeatRateVal = sumRepeatCount / (double) nonGreenCount * 100;
                        kpiConsistencyConfig.setRepeatRate(String.format("%.2f", repeatRateVal));
                    } else {
                        kpiConsistencyConfig.setRepeatRate("0.00");
                    }
                    log.info("PI[{}] 汇总 | 绿区: {} 总批次: {} 合格率: {} 重复数: {} 重复率: {}",
                            kpiConsistencyConfig.getPiCode(), sumGreenCount, sumTotalCount,
                            kpiConsistencyConfig.getPassRate(), sumRepeatCount, kpiConsistencyConfig.getRepeatRate());
                } catch (Exception e) {
                    // 处理解析异常
                    log.error("解析 batchConfig 失败: {}", batchConfig, e);
                }
            }
        }

        /**
         * pi 计算上级
         * piList
         * 满足条件 piCode(P010101) 长度大于 5 上一级是(P0101)  长度小于等于5不需要处理
         * 计算上级pi的绿区批次、总批次、合格率、重复次数、重复率
         * 计算结果 更新到 notPiList 中
         */
        // 按上级piCode分组汇总
        Map<String, List<KpiConsistencyConfig>> parentPiCodeMap = new HashMap<>();

        if (CollUtil.isNotEmpty(piList)) {
            for (KpiConsistencyConfig piConfig : piList) {
                String piCode = piConfig.getPiCode();
                // 长度大于5的需要计算上级
                if (StrUtil.isNotBlank(piCode) && piCode.length() > 5) {
                    // 获取上级piCode（去掉最后2位）
                    String parentPiCode = piCode.substring(0, piCode.length() - 2);
                    parentPiCodeMap.computeIfAbsent(parentPiCode, k -> new ArrayList<>()).add(piConfig);
                }
            }
        }



        // 遍历上级piCode，汇总计算统计数据
        if (CollUtil.isNotEmpty(notPiList)) {
            for (KpiConsistencyConfig notPiConfig : notPiList) {
                String notPiCode = notPiConfig.getPiCode();
                // 查找该上级对应的所有子PI
                List<KpiConsistencyConfig> childPiList = parentPiCodeMap.get(notPiCode);
                if (CollUtil.isNotEmpty(childPiList)) {
                    // 汇总统计
                    int totalGreenBatchCount = 0;
                    int totalBatchCount = 0;
                    int totalRepeatCount = 0;

                    for (KpiConsistencyConfig childPi : childPiList) {
                        if (childPi.getGreenBatchCount() != null) {
                            totalGreenBatchCount += childPi.getGreenBatchCount();
                        }
                        if (childPi.getTotalBatchCount() != null) {
                            totalBatchCount += childPi.getTotalBatchCount();
                        }
                        if (childPi.getRepeatCount() != null) {
                            totalRepeatCount += childPi.getRepeatCount();
                        }
                    }

                    // 设置汇总数据到上级PI
                    notPiConfig.setGreenBatchCount(totalGreenBatchCount);
                    notPiConfig.setTotalBatchCount(totalBatchCount);
                    notPiConfig.setRepeatCount(totalRepeatCount);

                    // 计算合格率（防止除零，保留两位小数）
                    if (totalBatchCount > 0) {
                        double rate = totalGreenBatchCount / (double) totalBatchCount * 100;
                        notPiConfig.setPassRate(String.format("%.2f", rate));
                    } else {
                        notPiConfig.setPassRate("0.00");
                    }

                    // 计算重复率（防止除零）
                    int nonGreenCount = totalBatchCount - totalGreenBatchCount;
                    if (nonGreenCount > 0) {
                        notPiConfig.setRepeatRate(String.format("%.2f", totalRepeatCount / (double) nonGreenCount * 100));
                    } else {
                        notPiConfig.setRepeatRate("0.00");
                    }
                }
            }
        }




        // kpiList notPiList piList rootList 数据合并到 reportList
        List<KpiConsistencyReport> reportList = new ArrayList<>();
        Date createTime = new Date();

        // 合并所有层级的配置数据
        List<KpiConsistencyConfig> allConfigList = new ArrayList<>();
//        if (CollUtil.isNotEmpty(rootList)) {
//            allConfigList.addAll(rootList);
//        }
        if (CollUtil.isNotEmpty(kpiList)) {
            allConfigList.addAll(kpiList);
        }
        if (CollUtil.isNotEmpty(notPiList)) {
            allConfigList.addAll(notPiList);
        }
        if (CollUtil.isNotEmpty(piList)) {
            allConfigList.addAll(piList);
        }

        for (KpiConsistencyConfig config : allConfigList) {
            KpiConsistencyReport report = new KpiConsistencyReport();
            // 基础信息
            report.setTenantId(config.getTenantId());
            report.setFactoryCode(config.getFactoryCode());
            report.setDeptCode(config.getDeptCode());
            report.setLineCode(config.getLineCode());
            report.setFactory(config.getFactory());
            report.setFactoryName(config.getFactoryName());
            report.setKpiCode(config.getKpiCode());
            report.setKpiName(config.getKpiName());
            report.setPiCode(config.getPiCode());
            report.setPiName(config.getPiName());
            report.setUnit(config.getUnit());
            //report.setWeight(config.getWeight());
            report.setTargetValue(config.getTargetValue());
            report.setLevel(config.getLevel());
            report.setSort(config.getSort());
            // 统计数据（配置没有的值给0）
            report.setGreenBatchCount(config.getGreenBatchCount() != null ? config.getGreenBatchCount() : 0);
            report.setTotalBatchCount(config.getTotalBatchCount() != null ? config.getTotalBatchCount() : 0);
            report.setPassRate(config.getPassRate() != null ? config.getPassRate() : "0.00");
            report.setRepeatCount(config.getRepeatCount() != null ? config.getRepeatCount() : 0);
            report.setRepeatRate(config.getRepeatRate() != null ? config.getRepeatRate() : "0.00");
            // 月累、月年累（仅KPI层级有值）
            report.setMonthAccumulate(config.getMonthCumulative() != null ? config.getMonthCumulative() : "0");
            report.setMonthYearAccumulate(config.getYearMonthCumulative() != null ? config.getYearMonthCumulative() : "0");
            // 日期
            try {
                report.setWeekDate(DateUtils.parseDate(config.getWeekDate()));
            } catch (Exception e) {
                report.setWeekDate(weekEndDate);
            }
            report.setCreateTime(createTime);
            report.setUpdateTime(createTime);

            reportList.add(report);
            log.debug("合并报表数据: level={} kpiCode={} piCode={} passRate={}",
                    config.getLevel(), config.getKpiCode(), config.getPiCode(), config.getPassRate());
        }

        log.info("数据合并完成，共{}条报表数据", reportList.size());


        // 删除本周已有的数据，避免重复
        deleteExistingReportData(weekEndDate);

        // 批量保存报表数据
        if (CollUtil.isNotEmpty(reportList)) {
            kpiConsistencyReportService.saveBatch(reportList);
            log.info("成功保存{}条一致性KPI报表数据", reportList.size());
        }

        log.info("一致性KPI数据抽取任务执行完成");
    }

    /**
     * 删除已存在的报表数据（物理删除）
     *
     * @param weekDate 周日期
     */
    private void deleteExistingReportData(Date weekDate) {
        // 物理删除，报表数据可重新生成，绕过 @TableLogic 逻辑删除
        int deleted = kpiConsistencyReportService.physicalDeleteByWeekDate(weekDate);
        log.info("物理删除周日期[{}]的报表数据，共{}条", DateUtils.formatDate(weekDate), deleted);
    }


    /**
     * 获取周对象 MdmWeek
     * 查询条件
     *  date 判断时间在周范围内 (开始  结束)
     *  year
     *
     * 获取开始时间 结束时间  月 周数
     */
    private MdmWeek getWeek(Date queryDate,  String year) {
        // 根据日期查询所在的周
        String queryDateStr = DateUtils.formatDate(queryDate);
        MdmWeek mdmWeek = mdmWeekService.lambdaQuery()
                .le(MdmWeek::getStartDate, queryDateStr)
                .ge(MdmWeek::getEndDate, queryDateStr)
                .eq(MdmWeek::getYear, year)
                .last("limit 1")
                .one();

        if (mdmWeek == null) {
            throw new JeecgBootException("未找到日期[" + queryDateStr + "]对应的周数据");
        }
        return mdmWeek;
    }

}