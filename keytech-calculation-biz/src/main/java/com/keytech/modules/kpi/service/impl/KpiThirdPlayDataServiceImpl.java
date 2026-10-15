package com.keytech.modules.kpi.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.keytech.modules.action.plan.entity.ActionPlanTrack;
import com.keytech.modules.action.plan.service.IActionPlanTrackService;
import com.keytech.modules.fivewhy.entity.FiveWhyAnalyse;
import com.keytech.modules.fivewhy.service.IFiveWhyAnalyseService;
import com.keytech.modules.kpi.dto.KpiFwhyDto;
import com.keytech.modules.kpi.dto.KpiFwhyProblemDto;
import com.keytech.modules.kpi.dto.KpiFwhyPushDto;
import com.keytech.modules.kpi.dto.KpiFwhyRectifyActionsDto;
import com.keytech.modules.kpi.entity.*;
import com.keytech.modules.kpi.mapper.KpiThirdPlayDataMapper;
import com.keytech.modules.kpi.service.*;
import com.keytech.modules.kpi.util.*;
import com.keytech.modules.mdm3.entity.MdmWeek;
import com.keytech.modules.mdm3.service.IMdmWeekService;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.jeecg.common.config.TenantContext;
import org.jeecg.modules.system.entity.SysDepart;
import org.jeecg.modules.system.entity.SysUser;
import org.jeecg.modules.system.service.ISysDepartService;
import org.jeecg.modules.system.service.ISysUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.keytech.base.service.impl.BaseServiceImpl;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @Description: 第三方接口-维护KPI
 * @Author: jeecg-boot
 * @Date:   2025-03-13
 * @Version: V1.0
 */
@Service
@Slf4j
public class KpiThirdPlayDataServiceImpl extends BaseServiceImpl<KpiThirdPlayDataMapper, KpiThirdPlayData> implements IKpiThirdPlayDataService {

    //第三方 nonce
    private static String NONCE = "123456";
    // 不达标推送-已处理的数据id
    private final Map<String, String> handledFailures = new HashMap<>();

    //维护KPI请求地址
    @Value("${kpi.maintenanceUrl}")
    private String maintenanceUrl;
    @Value("${mes-system.tenant-id}")
    private Integer tenantId;
    @Autowired
    private ISysDepartService sysDepartService;
    @Autowired
    private IKpiThirdPlayDataService thirdPlayDataService;
    @Autowired
    private IKpiInfoService kpiInfoService;
    @Autowired
    private IKpiReportTargetValueService targetValueService;
    @Autowired
    private IKpiReportActualWeekValueService weekValueService;
    @Autowired
    private IKpiReportActualDayValueService kpiReportActualDayValueService;
    @Autowired
    private IMdmWeekService mdmWeekService;

    @Autowired
    private IFiveWhyAnalyseService iFiveWhyAnalyseService;

    @Autowired
    private IKpiInfoService iKpiInfoService;
    @Autowired
    private IActionPlanTrackService iActionPlanTrackService;
    @Autowired
    private ISysUserService iSysUserService;
    @Autowired
    private IKpiNotstandardLogService iKpiNotstandardLogService;

    /**
     * 维护KPI数据抽取
     */
    @Override
    public void synchronizeMonthWeekValueJob() {
        log.info("第三方接口指标数据同步开始");
        try {
            //获取当前工厂名称
            String departName = getDepartName();
            //设置当前住户(tenantId)
            TenantContext.setTenant(tenantId+"");

            //获取请求参数
            JSONObject jsonBody = createJsonBody(departName);
            RequestBody requestBody = RequestBody.create(MediaType.parse("application/json"), jsonBody.toString());
            Request request = new Request.Builder().url(maintenanceUrl + "/api/dapi/kpiNorm/noGetwaygetkpiNormData").post(requestBody).build();
            log.error("第三方接口指标数据同步--请求参数 {}", jsonBody.toString());
            // 设置连接超时/读取超时为10000秒
            OkHttpClient client = new OkHttpClient.Builder()
                    .connectTimeout(99999, TimeUnit.SECONDS)
                    .readTimeout(99999, TimeUnit.SECONDS)
                    .build();
            //Call call = okHttpClient.newCall(request);

            try (Response response = client.newCall(request).execute()) {
                handleResponse(response);
            }

        } catch (IOException e) {
            log.error("调用错误 {}", e.getMessage());
        }

        log.info("第三方接口指标数据同步结束");
    }

    /**
     * 处理HTTP响应
     *
     * @param response HTTP响应对象
     * @throws IOException 如果在读取响应体时发生错误
     */
    private void handleResponse(Response response) throws IOException {
        if (response.isSuccessful()) {
            ResponseBody body = response.body();
            if (body != null) {
                handleResponseBody(body);
            } else {
                log.error("结果为空");
            }
        } else {
            log.error("连接错误 {}", response.message());
            log.error("连接错误-状态码 {}", response.code());
        }
    }

    /**
     * 处理响应体中的数据
     *
     * @param body 响应体对象
     */
    private void handleResponseBody(ResponseBody body) {
        try {
            JSONArray dataArray = new cn.hutool.json.JSONObject(body.string()).getJSONObject("data").getJSONArray("data");
            if (dataArray != null) {
                List<KpiThirdPlayData> list = dataArray.toList(KpiThirdPlayData.class);
                log.info("第三方接口指标数据同步--结果 {}", list.size());
                processData(list);
            } else {
                log.error("结果为空");
            }
        } catch (Exception e) {
            log.error("结果转换异常 {}", e.getMessage());
            e.printStackTrace();
        }
    }

    public void processData(List<KpiThirdPlayData> OList) {
        //数据类型[状态] 周累5,月累6,年累7
        Set<String> validStates = new HashSet<>(Arrays.asList("5", "6", "7"));
        // 周/月/年累数据
        List<KpiThirdPlayData> filteredList = OList.stream().filter(m -> StrUtil.isNotEmpty(m.getState()) && validStates.contains(m.getState())).collect(Collectors.toList());
        //保存数据
        filteredList.parallelStream()
                .peek(data -> {
                    data.setTenantId(tenantId);
                    data.setId(data.getIndocno()+"");
                })
                .forEach(thirdPlayDataService::saveOrUpdate);
        if (filteredList.isEmpty()) {
            log.error("结果为空");
            return;
        }
        //获取年集合(数据存在跨年,获取多个年份)  PM比例 值固定为40-60
        Set<Integer> yearSet = getYearSet(filteredList);

        //获取数据周和所属年
        getyearAndWeek(filteredList, yearSet);

        // 按照第三方接口的指标名称分组
        Map<String, List<KpiThirdPlayData>> groupedData = filteredList.parallelStream().filter(mdmThirdPlayData -> StrUtil.isNotEmpty(mdmThirdPlayData.getStitle()))
                .collect(Collectors.groupingByConcurrent(mdmThirdPlayData -> mdmThirdPlayData.getStitle()));

        //获取KPI
        LambdaQueryWrapper<KpiInfo> kpiLambdaQueryWrapper = new LambdaQueryWrapper<KpiInfo>();
        kpiLambdaQueryWrapper.isNotNull(KpiInfo::getThirdName).ne(KpiInfo::getThirdName, "");
        // 使用 and + 括号包裹 OR 条件
        kpiLambdaQueryWrapper.and(wrapper -> {
            boolean isFirst = true;
            for (Integer year : yearSet) {
                if (isFirst) {
                    wrapper.like(KpiInfo::getYear, year);
                    isFirst = false;
                } else {
                    wrapper.or().like(KpiInfo::getYear, year);
                }
            }
        });
        List<KpiInfo> kpiInfoList = kpiInfoService.list(kpiLambdaQueryWrapper);

//        if (CollUtil.isEmpty(keys)) {
//            log.error("KPI内没有第三方接口的指标");
//            return;
//        }

        if (!(null != kpiInfoList && kpiInfoList.size() > 0)) {
            log.error("KPI内没有第三方接口的指标");
            return;
        }

        for (KpiInfo kpiInfo : kpiInfoList) {

            List<KpiThirdPlayData> dataList = groupedData.getOrDefault(kpiInfo.getThirdName(), Collections.emptyList());
            if (CollUtil.isEmpty(dataList)) {
                log.error("没有第三方接口的指标数据 key : {}", kpiInfo.getThirdName());
                continue;
            }
            //
            for (KpiThirdPlayData kpiThirdPlayData : dataList) {
                if(null != kpiInfo.getDecimalNum()){
                    kpiThirdPlayData.setDecimalNum(kpiInfo.getDecimalNum());
                    String iplanvalue = kpiThirdPlayData.getIplanvalue();
                    String iactualvalue = kpiThirdPlayData.getIactualvalue();
                    //目标值 (排除 40-60)
                    if(StrUtil.isNotBlank(iplanvalue) && !iplanvalue.contains("-")){
                        BigDecimal bd = new BigDecimal(iplanvalue);
                        kpiThirdPlayData.setIplanvalue(bd.setScale(kpiInfo.getDecimalNum(), RoundingMode.HALF_UP).toString());
                    }
                    //实际值
                    if(StrUtil.isNotBlank(iactualvalue)){
                        BigDecimal bd = new BigDecimal(iactualvalue);
                        kpiThirdPlayData.setIactualvalue(bd.setScale(kpiInfo.getDecimalNum(), RoundingMode.HALF_UP).toString());
                    }
                }
            }
            dataList.parallelStream().filter(m -> StrUtil.isNotEmpty(m.getStitle()) && m.getStitle().contains("-"))
                    .collect(Collectors.groupingByConcurrent(KpiThirdPlayData::getStitle)).forEach((k, v) -> processGroupedData(kpiInfo.getThirdName(), k, v,kpiInfo));

        }
    }



    private void processGroupedData(String key, String stitle, List<KpiThirdPlayData> dataList,KpiInfo kpiInfo) {
        dataList.stream().findFirst().ifPresent(mdmThirdPlayData -> processKpiData(mdmThirdPlayData, kpiInfo, dataList));
    }

    private void processKpiData(KpiThirdPlayData mdmThirdPlayData, KpiInfo kpiInfo, List<KpiThirdPlayData> orDefault) {
        Integer tenantId = kpiInfo.getTenantId();
        String kpiCode = kpiInfo.getKpiCode();
        String factory = kpiInfo.getFactoryCode();
        String dept = kpiInfo.getDeptCode();
//        Long perentId = kpiInfo.getPerentId();
        String id = kpiInfo.getId();

        LambdaUpdateWrapper<KpiInfo> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.set(KpiInfo::getUnit, mdmThirdPlayData.getSunit());
        updateWrapper.set(KpiInfo::getThirdName, mdmThirdPlayData.getStitle());
        updateWrapper.eq(KpiInfo::getId, id);
        kpiInfoService.update(updateWrapper);
//        Map<Long, String> longStringMap = CollStreamUtil.toMap(kpiObjectService.list(), KpiObject::getId, KpiObject::getHierarchy);
//        String reportId = longStringMap.get(Long.parseLong(String.valueOf(perentId).substring(0, 3)));
        String reportId = dept;

        Map<String, Object> map = new HashMap<>();
        map.put("factoryCode", factory);
        map.put("reportId", reportId);
        map.put("kpiCode", kpiCode);
        map.put("tenantId", tenantId);
//        map.put("actualType", isomerism);

        Map<String, List<KpiThirdPlayData>> stateType = orDefault.stream().filter(m -> StrUtil.isNotEmpty(m.getState()) && m.getDatekpi() != null).collect(Collectors.groupingBy(KpiThirdPlayData::getState, Collectors.collectingAndThen(Collectors.toCollection(LinkedList::new), list -> {
            list.sort(Comparator.comparing(KpiThirdPlayData::getDatekpi).reversed());
            return list;
        })));

        //获取年集合 orDefault
        Set<Integer> yearSet = orDefault.stream().map(KpiThirdPlayData::getYear).collect(Collectors.toSet());
        Map<String, Map<Integer, MdmWeek>> weekYearMap = new HashMap<>();
        //遍历年集合
        for (Integer year : yearSet) {
            weekYearMap.put(year+"", mdmWeekService.getWeekDataByWeekNum(year+""));
        }
        //获取周
        //Map<Integer, MdmWeek> weekMap = mdmWeekService.getWeekDataByWeekNum(mdmThirdPlayData.getYear()+"");


        //datekpi
        String yearTarget = MonthEnum.getValueByMonth("13");
        //线程 创建一个固定大小的线程池，线程数量为可用处理器核心数量
        ExecutorService executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
        for (Map.Entry<String, List<KpiThirdPlayData>> entry : stateType.entrySet()) {
            String k = entry.getKey();
            List<KpiThirdPlayData> v = entry.getValue();
            executorService.submit(() -> {
                switch (k) {
                    case "5": // 处理周数据
                        processWeekData(v, factory, dept, kpiCode, map, reportId, weekYearMap);
                        break;
                    case "6": // 处理月数据
                        processMonthData(v, factory, dept, kpiCode, map, reportId);
                        break;
                    default: // 处理年数据
                        processYearData(v, factory, dept, kpiCode, map, yearTarget, reportId);
                        break;
                }
                // 处理实际值数据
                processActualValueData(v,kpiCode, reportId,factory,map);
            });
        }
        // 关闭线程池
        executorService.shutdown();
    }

    /**
     * 处理周数据
     *
     * @param data        周数据列表
     * @param factory     工厂代码
     * @param dept        部门代码
     * @param kpiCode     KPI代码
     * @param map         周数据和月数据的映射
     * @param reportId    报表ID
     */
    private void processWeekData(List<KpiThirdPlayData> data, String factory, String dept, String kpiCode, Map<String, Object> map, String reportId,Map<String, Map<Integer, MdmWeek>> weekYearMap) {
        data.forEach(d -> d.setYyyyMMdd(DateUtils.YEAR_FORMAT.format(d.getDatekpi())));
//        Map<String, KpiThirdPlayData> result = data.stream().collect(Collectors.groupingBy(myObject -> getWeekKey(myObject.getDatekpi()),
          Map<String, KpiThirdPlayData> result = data.stream().collect(Collectors.groupingBy(myObject -> myObject.getWeekRange(),
                Collectors.collectingAndThen(Collectors.toList(), list -> list.stream().max(Comparator.comparing(KpiThirdPlayData::getDatekpi)).orElse(null))));

        result.forEach((weekKey, mdmThirdPlayData) -> {
            if (mdmThirdPlayData != null) {
                String yearKey = mdmThirdPlayData.getYyyyMMdd();
                Map<Integer, MdmWeek> weekMap = weekYearMap.get(yearKey);
                KpiReportTargetValue targetValue = targetValueInsert(factory, dept, kpiCode, yearKey, reportId, map);
                //目标值
                String iplanvalue = mdmThirdPlayData.getIplanvalue();
                String kpiTypeWeekValue = targetValue.getKpiTypeWeekValue();
                cn.hutool.json.JSONObject json = new cn.hutool.json.JSONObject(kpiTypeWeekValue);
                json.putOpt(weekKey, StrUtil.isEmpty(iplanvalue) ? "" : iplanvalue);
                targetValue.setKpiTypeWeekValue(json.toString());
                //目标值为空不做更新
                if(StrUtil.isNotBlank(iplanvalue)){
                    targetValueService.updateById(targetValue);
                }
                String iactualvalue = mdmThirdPlayData.getIactualvalue();
                if (!Objects.isNull(iactualvalue) || StrUtil.isNotEmpty(iactualvalue)) {
                    map.put("year", targetValue.getYear());

                    String[] keys = weekKey.split("_");
                    int weekNumber = Integer.parseInt(keys[2]);
                    map.put("weekNumber",weekNumber);
                    map.put("newKey",weekKey); //存储key w_2025_42
                    //更新key 格式 (2023-11-20|2023-11-26)
                    //newBean.put("monthStr",)
                    MdmWeek mdmWeek = weekMap.get(weekNumber);
                    String month = mdmWeek.getMonth();
                    if(StrUtil.isNotBlank(month)){
                        month = Integer.parseInt(month)<10?"0"+month:month;
                    }
                    map.put("monthStr",mdmWeek.getYear()+month);
                    weekKey = mdmWeek.getWeekRangeStr();

                    weekValueService.addWeekAndMonth(map, weekKey, iactualvalue);
                }
            }
        });
    }

    /**
     * 处理月数据
     *
     * @param data        月数据列表
     * @param factory     工厂代码
     * @param dept        部门代码
     * @param kpiCode     KPI代码
     * @param map         周数据和月数据的映射
     * @param reportId    报表ID
     */
    private void processMonthData(List<KpiThirdPlayData> data, String factory, String dept, String kpiCode, Map<String, Object> map, String reportId) {
//        data.forEach(d -> d.setYyyyMMdd(YEARMONTH_FORMAT.format(d.getDatekpi())));
        data.forEach(d -> d.setYyyyMMdd(DateUtils.formatDateByPattern(d.getDatekpi(), DateUtils.YEARMONTH_FORMAT)));
        List<KpiThirdPlayData> monthData = new ArrayList<>(data.stream().collect(Collectors.toMap(KpiThirdPlayData::getYyyyMMdd, obj -> obj, (a, b) -> a.getDatekpi().after(b.getDatekpi()) ? a : b)).values());

        monthData.forEach(m -> {
            String yyyyMMdd = m.getYyyyMMdd();
            String yearKey = yyyyMMdd.substring(0, 4);
            String month = yyyyMMdd.substring(yyyyMMdd.length() - 2);
            //写入目标
            KpiReportTargetValue targetValue = targetValueInsert(factory, dept, kpiCode, yearKey, reportId, map);
            String iplanvalue = m.getIplanvalue();
            String kpiTypeMonthValue = targetValue.getKpiTypeMonthValue();
            cn.hutool.json.JSONObject json = new cn.hutool.json.JSONObject(kpiTypeMonthValue);
            json.putOpt(MonthEnum.getValueByMonth(month), StrUtil.isEmpty(iplanvalue) ? "" : iplanvalue);
            updateActualAndTargetValue(map, m, yyyyMMdd, targetValue, json,iplanvalue);
        });
    }

    /**
     * 处理年数据
     *
     * @param data          年数据列表
     * @param factory       工厂代码
     * @param dept          部门代码
     * @param kpiCode       KPI代码
     * @param map           周数据和月数据的映射
     * @param yearTarget    年度目标
     * @param reportId      报表ID
     */
    private void processYearData(List<KpiThirdPlayData> data, String factory, String dept, String kpiCode, Map<String, Object> map, String yearTarget, String reportId) {
//        data.forEach(d -> d.setYyyyMMdd(YEAR_FORMAT.format(d.getDatekpi())));
        data.forEach(d -> d.setYyyyMMdd(DateUtils.formatDateByPattern(d.getDatekpi(), DateUtils.YEAR_FORMAT)));
        List<KpiThirdPlayData> yearData = new ArrayList<>(data.stream().collect(Collectors.toMap(KpiThirdPlayData::getYyyyMMdd, obj -> obj, (a, b) -> a.getDatekpi().after(b.getDatekpi()) ? a : b)).values());

        yearData.forEach(m -> {
            String yearKey = m.getYyyyMMdd();
            //写入目标
            KpiReportTargetValue targetValue = targetValueInsert(factory, dept, kpiCode, yearKey, reportId, map);
            String iplanvalue = m.getIplanvalue();
            String kpiTypeMonthValue = targetValue.getKpiTypeMonthValue();
            cn.hutool.json.JSONObject json = new cn.hutool.json.JSONObject(kpiTypeMonthValue);
            json.putOpt(yearTarget, StrUtil.isEmpty(iplanvalue) ? "" : iplanvalue);
            //更新目标值
            updateActualAndTargetValue(map, m, yearKey, targetValue, json, iplanvalue);
        });
    }

    /**
     * 处理实际值数据
     *
     * @param data      实际值数据列表
     * @param kpiCode   KPI代码
     * @param reportId  报表ID
     * @param factory   工厂代码
     */
    public void processActualValueData(List<KpiThirdPlayData> data,String kpiCode,String reportId,String factory,Map<String, Object> mapData){
        for (Map.Entry<Integer, List<KpiThirdPlayData>> entry : groupDataByYear(data).entrySet()) {
            Integer year = entry.getKey();
            List<KpiThirdPlayData> yearData = entry.getValue();
            Map<String, Object> map = new HashMap<>();
            map.put("year", year);
            map.put("reportId", reportId);
            map.put("kpiCode", kpiCode);
            Map<String, List<KpiThirdPlayData>> groupedByState = yearData.stream().filter(item -> Arrays.asList("5", "6", "7").contains(item.getState())).collect(Collectors.groupingBy(KpiThirdPlayData::getState));
            // 根据状态处理分组的数据
            groupedByState.forEach((state, stateData) -> processStateData(state, stateData, map));
            //List<Map<String, Object>> listOfMaps = Collections.singletonList(map);
            //保存实际值
            //kpiReportActualDayValueService.updateBy(listOfMaps);
            //LambdaUpdateWrapper<MdmKpiReportActualDayValue> updaterWrapper = Wrappers.lambdaUpdate(MdmKpiReportActualDayValue.class);
            List<KpiReportActualDayValue> list = kpiReportActualDayValueService.list(new LambdaQueryWrapper<KpiReportActualDayValue>()
                            .eq(KpiReportActualDayValue::getKpiCode, kpiCode)
                            //.eq(MdmKpiReportActualDayValue::getYear, year).eq(MdmKpiReportActualDayValue::getActualType, isomerism).eq(MdmKpiReportActualDayValue::getReportId, reportId));
                            .eq(KpiReportActualDayValue::getYear, year)
                    //.eq(MdmKpiReportActualDayValue::getReportId, reportId)
            );
            Optional<KpiReportActualDayValue> optionalFirst = list.stream().findFirst();
            if (optionalFirst.isPresent()) {
                KpiReportActualDayValue first = optionalFirst.get();
                String value = first.getValue();
                JSONObject jsonObject = JSONObject.parseObject(value);
                jsonObject.putAll(map);
                first.setValue(jsonObject.toJSONString());
                first.setIndicator(1);
                kpiReportActualDayValueService.updateById(first);
            } else {
                KpiReportActualDayValue dayValue = new KpiReportActualDayValue();
                dayValue.setValue(new JSONObject(map).toJSONString());
                dayValue.setYear(year);
                dayValue.setReportId(reportId);
                dayValue.setKpiCode(kpiCode);
                dayValue.setFactoryCode(factory);
                dayValue.setIndicator(1);
                dayValue.setTenantId(Integer.parseInt(mapData.get("tenantId")+""));
                kpiReportActualDayValueService.save(dayValue);
            }
        }
    }

    /**
     * 目标值模块插入
     * @param factory
     * @param dept
     * @param kpiCode
     * @param yearKey
     * @return
     */
    public KpiReportTargetValue targetValueInsert(String factory, String dept, String kpiCode, String yearKey, String reportId, Map<String, Object> map) {
        KpiReportTargetValue targetValue = new KpiReportTargetValue();
        KpiReportTargetValue value = targetValueService.getOne(new LambdaQueryWrapper<KpiReportTargetValue>()
                //.eq(MdmKpiReportTargetValue::getReportId, reportId)
                .eq(KpiReportTargetValue::getKpiCode, kpiCode)
                .eq(KpiReportTargetValue::getYear, yearKey));
        if (value == null) {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("lastYear", "");
            KpiReportTargetValue target = new KpiReportTargetValue();
            target.setTenantId(Integer.parseInt(map.get("tenantId")+""));
            target.setFactoryCode(factory);
            target.setDeptCode(dept);
            target.setReportId(reportId);
            target.setKpiCode(kpiCode);
            target.setYear(Integer.valueOf(yearKey));
            target.setKpiTypeMonthValue(jsonObject.toJSONString());
            target.setKpiTypeWeekValue(jsonObject.toJSONString());
            targetValueService.save(target);
            BeanUtil.copyProperties(target, targetValue);
        } else {
            BeanUtil.copyProperties(value, targetValue);
        }
        return targetValue;
    }

    /**
     * 更新目标值
     *
     * @param map 存储周数据的映射
     * @param m KpiThirdPlayData 对象
     * @param yyyyMMdd 日期字符串
     * @param targetValue KpiReportTargetValue 对象
     * @param json 存储目标值的 JSON 对象
     */
    private void updateActualAndTargetValue(Map<String, Object> map, KpiThirdPlayData m, String yyyyMMdd, KpiReportTargetValue targetValue, cn.hutool.json.JSONObject json,String iplanvalue) {
        targetValue.setKpiTypeMonthValue(json.toString());
        //目标值为空不更新
        if(StrUtil.isNotBlank(iplanvalue)){
            targetValueService.updateById(targetValue);
            String iactualvalue = m.getIactualvalue();
            if (!Objects.isNull(iactualvalue) || StrUtil.isNotEmpty(iactualvalue)) {
                map.put("year", targetValue.getYear());
                //同步周月(实际值)
                weekValueService.addWeekAndMonth(map, yyyyMMdd.length() > 4 ? yyyyMMdd.substring(0, 4) + "-" + yyyyMMdd.substring(4) : yyyyMMdd, iactualvalue);
            }
        }

    }


    /**
     * 根据状态处理分组的数据
     *
     * @param state       状态码
     * @param stateData   对应状态的数据列表
     * @param map         存储周数据和月数据的映射
     */
    private static void processStateData(String state, List<KpiThirdPlayData> stateData, Map<String, Object> map) {
        switch (state) {
            case "5":
//                stateData.forEach(week -> map.put(getWeekKey(week.getDatekpi()), week.getIactualvalue()));
                stateData.forEach(week -> map.put(week.getWeekRange(), week.getIactualvalue()));
                break;
            case "6":
                processMonthlyData(stateData, map);
                break;
            case "7":
                processYearlyData(stateData, map);
                break;
            default:
                break;
        }
    }

    private static void processMonthlyData(List<KpiThirdPlayData> stateData, Map<String, Object> map) {
//        stateData.forEach(d -> d.setYyyyMMdd(YEARMONTH_FORMAT.format(d.getDatekpi())));
        stateData.forEach(d -> d.setYyyyMMdd(DateUtils.formatDateByPattern(d.getDatekpi(), DateUtils.YEARMONTH_FORMAT)));

        List<KpiThirdPlayData> monthData = new ArrayList<>(stateData.stream()
                .collect(Collectors.toMap(KpiThirdPlayData::getYyyyMMdd, obj -> obj, (a, b) -> a.getDatekpi().after(b.getDatekpi()) ? a : b))
                .values());

        monthData.forEach(month -> map.put(month.getYyyyMMdd().substring(0, 4) + "-" + month.getYyyyMMdd().substring(4), month.getIactualvalue()));
    }

    private static void processYearlyData(List<KpiThirdPlayData> stateData, Map<String, Object> map) {
//        stateData.forEach(d -> d.setYyyyMMdd(YEAR_FORMAT.format(d.getDatekpi())));
        stateData.forEach(d -> d.setYyyyMMdd(DateUtils.formatDateByPattern(d.getDatekpi(), DateUtils.YEAR_FORMAT)));

        List<KpiThirdPlayData> yearData = new ArrayList<>(stateData.stream()
                .collect(Collectors.toMap(KpiThirdPlayData::getYyyyMMdd, obj -> obj, (a, b) -> a.getDatekpi().after(b.getDatekpi()) ? a : b))
                .values());

        yearData.forEach(year -> map.put(year.getYyyyMMdd(), year.getIactualvalue()));
    }


    /**
     * 获取部门名称
     *
     * @return 部门名称，如果找不到部门则返回 "SysDepart must not be null"
     */
    private String getDepartName() {
        LambdaQueryWrapper  wrapper = new LambdaQueryWrapper<SysDepart>().eq(SysDepart::getOrgType, "3");
        Optional<SysDepart> sysDepartOptional = sysDepartService.list(wrapper).stream().findFirst();
        if(sysDepartOptional.isPresent()){
            return sysDepartOptional.get().getDepartName();
        }
        return "SysDepart must not be null";
    }

    /**
     * 创建JSON请求体
     *
     * @param departName 部门名称
     * @return JSON请求体
     */
    private JSONObject createJsonBody(String departName) {
        JSONObject jsonBody = new JSONObject();
        jsonBody.put("_nonce", 123456);
        long dateTime = System.currentTimeMillis();
        jsonBody.put("_timestamp", dateTime);
        String sign = SignatureUtil.getSignature(jsonBody);
        jsonBody.put("_signature", sign);
        //获取15天前
        //LocalDateTime halfMonthAgo = LocalDateTime.now().minusDays(15);
        //北京工厂8天(天数过大接口无返回)  其他工厂15天
        LocalDateTime halfMonthAgo = LocalDateTime.now().minusDays(departName.equals("北京工厂") ? 8 : 15);
        LocalDateTime now = LocalDateTime.now();

        jsonBody.put("starttime", halfMonthAgo.format(DateUtils.DATE_TIME_FORMATTER));
        jsonBody.put("endtime", now.format(DateUtils.DATE_TIME_FORMATTER));
        jsonBody.put("timetype", "dmodt");
        //jsonBody.put("timetype", "dregt");
        jsonBody.put("icompanyid", FactoryEnum.getPrimaryByCode(departName));
        Set<String> thirdKeys = Stream.of(
                "2", "3", "4", "5", "6",
                "19", "20","23", "26", "29", "32", "33","191").collect(Collectors.toSet());
        if("赤峰工厂".equals(departName)){
            // 赤峰添加了类别
            thirdKeys = Stream.of(
                    "2", "3", "4", "5", "6",
                    "19", "20","23", "26", "29", "32", "33","191","35", "38","41").collect(Collectors.toSet());
        }

        jsonBody.put("libraryids", thirdKeys);
        return jsonBody;
    }

    /**
     * //#*****
     * 获取每周的开始日期和结束日期拼接起来作为键
     */
    private String getWeekKey(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setFirstDayOfWeek(Calendar.MONDAY); // 显式设置第一天为星期一
        calendar.setTime(date);
        // 找到本周的开始日期
        calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
        String startOfWeek = DateUtils.formatDate(calendar.getTime());
        // 找到本周的结束日期
        calendar.add(Calendar.DAY_OF_WEEK, 6);
        String endOfWeek = DateUtils.formatDate(calendar.getTime());
        return startOfWeek + "|" + endOfWeek;
    }

    /**
     * 按照年份分组数据
     *
     * @param data 需要分组的数据列表
     * @return 按年份分组的数据Map，键为年份，值为对应年份的数据列表
     */
    private Map<Integer, List<KpiThirdPlayData>> groupDataByYear(List<KpiThirdPlayData> data) {
        Map<Integer, List<KpiThirdPlayData>> yearDataMap = new HashMap<>();
        for (KpiThirdPlayData playData : data) {
            String year = DateUtils.formatDateByPattern(playData.getDatekpi(),DateUtils.YEAR_FORMAT);
            // 如果Map中已经包含该年份的数据，将数据添加到对应的List中；否则，创建一个新的List
            yearDataMap.computeIfAbsent(Integer.valueOf(year), k -> new ArrayList<>()).add(playData);
        }
        return yearDataMap;
    }


    /**
     * 获取周和年
     */
    public void getyearAndWeek(List<KpiThirdPlayData> filteredList, Set<Integer> yearSet){

        //获取周集合
        LambdaQueryWrapper<MdmWeek> queryWrapper = new LambdaQueryWrapper<MdmWeek>();
        queryWrapper.in(MdmWeek::getYear,yearSet);
        queryWrapper.orderByAsc(MdmWeek::getYear).orderByAsc(MdmWeek::getWeekNumber);
        List<MdmWeek> weekList = mdmWeekService.list(queryWrapper);
        //判断周
        for (KpiThirdPlayData kpiThirdPlayData : filteredList) {
            Date datekpi = kpiThirdPlayData.getDatekpi();
            for (MdmWeek mdmWeek : weekList) {
                Date startDate = mdmWeek.getStartDate();
                Date endDate = mdmWeek.getEndDate();
                if (datekpi != null && startDate != null && endDate != null &&
                        !datekpi.before(startDate) && !datekpi.after(endDate)) {
                    //周 w_年_周数
                    kpiThirdPlayData.setWeekRange("w_"+mdmWeek.getYear()+"_"+mdmWeek.getWeekNumber());
                    break; // 只要找到一个符合的范围就不再继续检查
                }
            }
        }
    }

    /**
     * 获取年集合/同时设置年份
     *
     * @param filteredList 包含 KpiThirdPlayData 对象的列表，这些对象包含日期信息。
     * @return 包含从 filteredList 中提取的年份的集合。
     */
    public Set<Integer> getYearSet(List<KpiThirdPlayData> filteredList) {
        Set<Integer> yearSet = new HashSet<>();
        Calendar calendar = Calendar.getInstance();

        for (KpiThirdPlayData thirdPlayDatadata : filteredList) {
            Date datekpi = thirdPlayDatadata.getDatekpi();
            if (datekpi != null) {
                calendar.setTime(datekpi);
                int year = calendar.get(Calendar.YEAR);
                yearSet.add(year);
                thirdPlayDatadata.setYear(year);
            }
            //PM比例 值固定为40-60
            if(thirdPlayDatadata.getStitle().startsWith("PM比例")){
                thirdPlayDatadata.setIplanvalue("40-60");
            }
        }
        return yearSet;
    }




    /*********************************************************不达标推送******************************************************/

    /**
     * 不达标KPI推送
     * @param list
     */
    @Override
    public void kpiNotStandard(List<Long> list) {
        //获取5why
        LambdaQueryWrapper fiveWhyWrapper = new LambdaQueryWrapper<FiveWhyAnalyse>()
                .in(FiveWhyAnalyse::getId, list);
        List<FiveWhyAnalyse> fiveWhyList = iFiveWhyAnalyseService.list(fiveWhyWrapper);
        kpiNotStandardData(fiveWhyList, null, "1");
    }


    /**
     * 不达标KPI推送
     * analyseId 5whyId
     * @param
     * fiveWhyList 5wyh
     * notstandardList 推送历史记录
     */
    public void kpiNotStandardData(List<FiveWhyAnalyse> fiveWhyList ,Map<String, String> analyseIdToIdMap, String type) {

        if(CollUtil.isEmpty(fiveWhyList)){
            return;
        }

        //签名
        JSONObject jsonBody = new JSONObject();
        jsonBody.put("_nonce", NONCE);
        jsonBody.put("_timestamp", System.currentTimeMillis());
        String sign = SignatureUtil.getSignature(jsonBody);
        //时间戳
        long timestamp = Instant.now().toEpochMilli();
        KpiFwhyPushDto kpiFwhyPushDto = new KpiFwhyPushDto();
        //签名
        kpiFwhyPushDto.setSignature(sign);
        //时间戳
        kpiFwhyPushDto.setTimestamp(timestamp);
        //nonce
        kpiFwhyPushDto.setNonce(Long.parseLong(NONCE));

        //获取人员
        Map<String, SysUser> userMap = iSysUserService.list().stream()
                .collect(Collectors.toConcurrentMap(
                        SysUser::getId,
                        Function.identity()));
        List<KpiFwhyDto> kpiFwhyDtoList = new ArrayList<>();
        for (FiveWhyAnalyse fiveWhyAnalyse : fiveWhyList) {
            KpiFwhyDto kpiFwhyDto = getKpiDataList(fiveWhyAnalyse,userMap,type);
            if(kpiFwhyDto != null){
                kpiFwhyDtoList.add(kpiFwhyDto);
            }
        }
        if((kpiFwhyDtoList != null && kpiFwhyDtoList.isEmpty()) || kpiFwhyDtoList == null){
            return;
        }

        //kpiFwhyDtoList.add(kpiFwhyDto);
        kpiFwhyPushDto.setDataList(kpiFwhyDtoList);

        List<KpiNotstandardLog> kpiNotstandardList = new ArrayList<>();
        for (KpiFwhyDto kpiFwhyDto : kpiFwhyDtoList) {
            KpiNotstandardLog kpiNotstandardLog = new KpiNotstandardLog();
            kpiNotstandardLog.setAnalyseId(kpiFwhyDto.getAnalyseId());
            kpiNotstandardLog.setThirdPlayId(kpiFwhyDto.getThirdPlayId());
            kpiNotstandardLog.setKpiCode(kpiFwhyDto.getKpiCode());
            if(analyseIdToIdMap != null){
                kpiNotstandardLog.setId(analyseIdToIdMap.getOrDefault(kpiFwhyDto.getAnalyseId(),""));
            }

            kpiNotstandardList.add(kpiNotstandardLog);
        }
        //推送
        push(kpiFwhyPushDto, kpiNotstandardList,type);

    }

    /**
     * 数据推送
     */
    private void push(KpiFwhyPushDto kpiFwhyPushDto, List<KpiNotstandardLog> kpiNotstandardList, String type){
        //推送数据(测试)
//        String url = "http://yjeam.lucksoft.com.cn/api/dapi/kpi5why/noGetwayAdd";
        //推送数据(正式)
        String url = maintenanceUrl+"/api/dapi/kpi5why/noGetwayAdd";
        try {
            ObjectMapper mapper = new ObjectMapper();
            String json = mapper.writeValueAsString(kpiFwhyPushDto);
            RequestBody requestBody = RequestBody.create(MediaType.parse("application/json"), json);
            Request request = new Request.Builder().url(url).post(requestBody).build();
            log.error("不达标kpi的5why分析数据同步--请求参数 {}", json);
            // 设置连接超时/读取超时为10000秒
            OkHttpClient client = new OkHttpClient.Builder()
                    .connectTimeout(99999, TimeUnit.SECONDS)
                    .readTimeout(99999, TimeUnit.SECONDS)
                    .build();

            try (Response response = client.newCall(request).execute()) {
                if (response.code() == 200) {
                    log.error("{code:"+response.code()+", message:"+response.message()+"}");
                    log.error("推送成功");
                    //写入记录表
                    for (KpiNotstandardLog kpiNotstandardLog : kpiNotstandardList) {
                        kpiNotstandardLog.setState(type);
                        if(StrUtil.isNotBlank(kpiNotstandardLog.getId()) && "1".equals(type)){
                            kpiNotstandardLog.setState("3");
                        }else{
                            kpiNotstandardLog.setState(type);
                        }
                        kpiNotstandardLog.setResult("{code:"+response.code()+", message:"+response.message()+"}");
                    }
                    //更新/新增
                    iKpiNotstandardLogService.saveOrUpdateBatch(kpiNotstandardList);
                } else {
                    log.error("连接错误 {}", response.message());
                    log.error("连接错误-状态码 {}", response.code());
                }
            }
        } catch (IOException e) {
            log.error("调用错误 {}", e.getMessage());
        }
    }


    /**
     * 行动完成情况类型转换
     * 0进行中   -->  1进行中
     * 1已完成   -->  2已完成
     * 2延迟     --> 3延迟
     * 3取消     --> 4取消
     * 4延迟完成  --> 5延迟完成
     *
     * @param finishState
     * @return
     */
    private Map<String, Object> getTrackType(String finishState) {
        Map<String, Object> map = new HashMap<>();
        if("0".equals(finishState)){
            map.put("code", 1);
            map.put("name", "进行中");
        }else if("1".equals(finishState)){
            map.put("code", 2);
            map.put("name", "已完成");
        }else if("3".equals(finishState)){
            map.put("code", 3);
            map.put("name", "延迟");
        }else if("4".equals(finishState)){
            map.put("code", 4);
            map.put("name", "取消");
        }else if("5".equals(finishState)){
            map.put("code", 5);
            map.put("name", "延迟完成");
        }else{
            map = null;
        }
        return map;
    }

    /**
     * 将编号格式化为8位字符串，不足部分前面补零
     * @param putUserId 用户id
     * userMap  用户id-用户对象
     * @return 8位格式化后的字符串（如"00002210"、"00022200"）
     */
    public String formatTo8DigitCode(String putUserId, Map<String, SysUser> userMap) {
        if(putUserId.length() == 8){
            return putUserId;
        }
        SysUser sysUser = userMap.get(putUserId);
        if(sysUser != null){
            String code = sysUser.getUsername();
            // 参数校验
            if (code == null) {
                return "";
            }
            // 移除可能存在的空格
            String trimmedCode = code.trim();
            // 检查是否只包含数字
            if (!trimmedCode.matches("\\d+")) {
                return trimmedCode;
            }
            // 大于8位
            if (trimmedCode.length() > 8) {
                //返回用户名
                return sysUser.getRealname();
            }
            // 使用String.format格式化，前面补零到8位
            return String.format("%08d", Integer.parseInt(trimmedCode));
        }else{
            return "";
        }

    }

    /**
     * 将编号格式化为8位字符串，不足部分前面补零
     * @param code 用户code
     * userMap  用户id-用户对象
     * @return 8位格式化后的字符串（如"00002210"、"00022200"）
     */
    public String formatTo8DigitUserCode(String code, Map<String, SysUser> userMap) {
        if(StrUtil.isNotBlank(code)){
            // 参数校验
            if (code == null) {
                return "";
            }
            // 移除可能存在的空格
            String trimmedCode = code.trim();
            // 检查是否只包含数字
            if (!trimmedCode.matches("\\d+")) {
                return trimmedCode;
            }
            // 大于8位
            if (trimmedCode.length() > 8) {
                //
                return trimmedCode;
            }
            // 使用String.format格式化，前面补零到8位
            return String.format("%08d", Integer.parseInt(trimmedCode));
        }else{
            return "";
        }

    }

    /**
     * 获取KPI5w推送数据集合
     */
    public KpiFwhyDto getKpiDataList(FiveWhyAnalyse fiveWhyAnalyse, Map<String, SysUser> userMap,String type){

        //数据集合
        KpiFwhyDto kpiFwhyDto = new KpiFwhyDto();
        //获取KPI
        LambdaQueryWrapper wrapper = new LambdaQueryWrapper<KpiInfo>()
                .eq(KpiInfo::getKpiCode, fiveWhyAnalyse.getKpiCode())
                .isNotNull(KpiInfo::getThirdName);

        List<KpiInfo> kpiInfoList = iKpiInfoService.list(wrapper);
        if(kpiInfoList != null && !kpiInfoList.isEmpty()) {
            KpiInfo kpiInfo = kpiInfoList.get(0);
            //第三方指标名称
            String thirdName = kpiInfo.getThirdName();
            //第三方状态
            String state = "";
            //第三方时间
            String dateKpi = "";
            //String kpiYear = fiveWhyAnalyse.getKpiYear();
            String colKey = fiveWhyAnalyse.getKpiColKey();
            //判断周月数据
            if (colKey.startsWith("w_")) {
                //周数据
                state = "5";
                String year = WeekUtils.extractYear(colKey);
                Integer weekNum = WeekUtils.extractWeekNum(colKey);
                Map<Integer, String> weekMap = mdmWeekService.getWeekOfYear(year);
                String datas = weekMap.get(weekNum);
                if (datas != null && !datas.isEmpty()) {
                    String[] parts = datas.split("\\|");
                    if (parts.length > 1) {
                        dateKpi = parts[1] + " 00:00:00";
                    }
                }

            } else {
                //月数据
                state = "6";
                dateKpi = DateUtils.getLastDayOfMonth(colKey) + " 00:00:00";
            }
            //获取维护KPI
            LambdaQueryWrapper kpiWrapper = new LambdaQueryWrapper<KpiThirdPlayData>()
                    .eq(KpiThirdPlayData::getStitle, thirdName) //指标名称
                    .eq(KpiThirdPlayData::getState, state) //指标状态(类型)
                    .eq(KpiThirdPlayData::getDatekpi, dateKpi); //时间
            List<KpiThirdPlayData> kpiThirdPlayList = thirdPlayDataService.list(kpiWrapper);

            if (kpiThirdPlayList != null && !kpiThirdPlayList.isEmpty()) {
                KpiThirdPlayData kpiThirdPlayData = kpiThirdPlayList.get(0);
                //5why数据生成 问答分析数据
                //生成推送Json
//                KpiFwhyDto kpiFwhyDto = new KpiFwhyDto();
                //kpi结果id 原对接表的indocno字段
                kpiFwhyDto.setKpiResultId(Long.valueOf(kpiThirdPlayData.getIndocno()));
                //伪删除  0 (删除是1)
                kpiFwhyDto.setIdel("3".equals(type)? 1: 0);
                //日期
                kpiFwhyDto.setDateKpi(kpiThirdPlayData.getDatekpi());
                // 状态 (1夜 2白 3中 4日累 5周累 6月累 7年累)
                kpiFwhyDto.setState(Integer.parseInt(kpiThirdPlayData.getState()));
                // 工厂名称 null
                //kpiFwhyDto.setSfactory(null);
                // 车间名称 null
                //kpiFwhyDto.setSplant(null);
                // 生产线名称 null
                //kpiFwhyDto.setSprdline(null);
                // 指标名称 原对接表的ilibraryid 字段
                kpiFwhyDto.setIlibraryId(kpiThirdPlayData.getIlibraryid());
                // 标题    mes3_kpi_third_play_data stitle
                kpiFwhyDto.setStitle(kpiThirdPlayData.getStitle());

                // 异常描述
                String description = "";
                if (StrUtil.isNotBlank(fiveWhyAnalyse.getProblemDesc())) {
                    description = fiveWhyAnalyse.getProblemDesc().replace("|","--");
                }
                kpiFwhyDto.setDescription(description);
                // 创建时间
                kpiFwhyDto.setDregtStr(DateUtils.formatDateTime(fiveWhyAnalyse.getCreateTime()));
                // 指标名称
                kpiFwhyDto.setHeadline(fiveWhyAnalyse.getProblem());
                // 人员工号
                String workno = formatTo8DigitUserCode(fiveWhyAnalyse.getCreateBy(), userMap);
                kpiFwhyDto.setWorkno(workno);
                //数据来源 默认1
                kpiFwhyDto.setItype("1");

                kpiFwhyDto.setAnalyseId(fiveWhyAnalyse.getId());
                kpiFwhyDto.setKpiCode(fiveWhyAnalyse.getKpiCode());
                kpiFwhyDto.setThirdPlayId(kpiThirdPlayData.getId());
                //问答分析数据 **************

                //FiveWhyResonDto fiveWhyResonDto = new FiveWhyResonDto();
                List<KpiFwhyProblemDto> kpiFwhyProblemDto = fiveWhyAnalysis(fiveWhyAnalyse.getReasonJson(), fiveWhyAnalyse.getMergeRule()
                        , workno);
                kpiFwhyDto.setKpiFwhyProblemDto(kpiFwhyProblemDto);


                //整改行动数据
                LambdaQueryWrapper trackWrapper = new LambdaQueryWrapper<ActionPlanTrack>()
                        .eq(ActionPlanTrack::getDataId, fiveWhyAnalyse.getId());
                List<ActionPlanTrack> trackList = iActionPlanTrackService.list(trackWrapper);
                List<KpiFwhyRectifyActionsDto> kpiFwhyRectifyActionsList = new ArrayList<KpiFwhyRectifyActionsDto>();
                for (ActionPlanTrack actionPlanTrack : trackList) {
                    KpiFwhyRectifyActionsDto kpiFwhyRectifyActionsDto = new KpiFwhyRectifyActionsDto();
                    // 工厂名称   null
                    kpiFwhyRectifyActionsDto.setSfactory(null);
                    // 车间名称   null
                    kpiFwhyRectifyActionsDto.setSplant(null);
                    // 生成线名称 null
                    kpiFwhyRectifyActionsDto.setSprdline(null);
                    // 类型id
                    kpiFwhyRectifyActionsDto.setItype(1);
                    // 类型
                    kpiFwhyRectifyActionsDto.setStype("KPI分析");
                    // 标题
                    kpiFwhyRectifyActionsDto.setStitle(kpiThirdPlayData.getStitle());
                    // 原因
                    kpiFwhyRectifyActionsDto.setSreason(actionPlanTrack.getProblemDescription());
                    // 行动
                    kpiFwhyRectifyActionsDto.setAction(actionPlanTrack.getActionTitle());

                    // 提出人工号
                    kpiFwhyRectifyActionsDto.setProposerworkno(formatTo8DigitCode(actionPlanTrack.getPutUserId(), userMap));
                    // 执行人工号
                    kpiFwhyRectifyActionsDto.setExecutorworkno(formatTo8DigitCode(actionPlanTrack.getExecuteUserId(), userMap));
                    // 计划完成日期
                    kpiFwhyRectifyActionsDto.setDplan(actionPlanTrack.getPlanFinishDate());
                    // 实际完成日期
                    kpiFwhyRectifyActionsDto.setDrend(actionPlanTrack.getPracticalFinishDate());
                    // 备注
                    kpiFwhyRectifyActionsDto.setSnote(actionPlanTrack.getRemark());
                    //生产信息化行动主键
                    kpiFwhyRectifyActionsDto.setIsourceid(actionPlanTrack.getId()+"");

                    // 完成状态 "istate": 1  -- 1进行中 2已完成 3延迟 4取消 5延迟完成,
                    Map<String, Object> map = getTrackType(actionPlanTrack.getFinishState());
                    if (map != null) {
                        kpiFwhyRectifyActionsDto.setIstate(Integer.parseInt(map.get("code") + ""));
                        // 完成状态描述 "sstate": "进行中",
                        kpiFwhyRectifyActionsDto.setSstate(map.get("name") + "");
                    }

                    // 作业班组
                    //kpiFwhyRectifyActionsDto.setSwbz(null);
                    // 取消原因(传空)
                    kpiFwhyRectifyActionsDto.setScancelreason(null);
                    // 伪删除   0
                    kpiFwhyRectifyActionsDto.setIdel(0);

                    kpiFwhyRectifyActionsList.add(kpiFwhyRectifyActionsDto);
                }

                kpiFwhyDto.setKpiFwhyRectifyActionsDto(kpiFwhyRectifyActionsList);
                //kpiFwhyDtoList.add(kpiFwhyDto);
            }else{
                return null;
            }
        }else{
            return null;
        }
        return kpiFwhyDto;
    }

    /**
     * 5why 解析
     * @param measuresJson
     * @return
     */
    public List<KpiFwhyProblemDto> fiveWhyAnalysis(String measuresJson,String mergeRule,String workno) {
        if(StrUtil.isBlank(measuresJson) || StrUtil.isBlank(mergeRule)){
            return null;
        }
        List<KpiFwhyProblemDto> fwhyProblemList = new ArrayList<>();
        //5w项集合
        String[] reasonsArray = FwhyParameter.reasonsArray;
        //JSON解析(5why)
        List<Map> list = JSONUtil.toList(measuresJson,Map.class);
        //单元格合并信息
        List<Map> mergeList = JSONUtil.toList(mergeRule,Map.class);

        //存储返回集合
        List<KpiFwhyProblemDto> fwhyProblemOne = new ArrayList<>();
        List<KpiFwhyProblemDto> fwhyProblemTree = new ArrayList<>();

        //存储第一层原因
        Set<String> oneSet = new HashSet<>();
        //上节点问题
        Map<String,String>  upSwhyMap = new HashMap<>();
        //层级
        int num = 0;
        for (Map<String,String> map : list) {
            String id = map.get("id");
            //
            Map<String,Integer> mergeMap = mergeList.get(num);
            int rNum = 0;

            for (String key : reasonsArray) {
                KpiFwhyProblemDto kpiFwhyProblemDto = new KpiFwhyProblemDto();
//                Integer whyMerge = mergeMap.get(rNum * 2 + 3 + "");
                //获取上节点
                Integer upReasonMerge = 0;
                if(num > 0){
                    Map<String,Integer> upMergeMap = mergeList.get(num-1);
                    upReasonMerge = upMergeMap.get(rNum * 2 + 2 + "");
                }
                //当前节点(问题)
                Integer reasonMerge = mergeMap.get(rNum * 2 + 2 + "");
//                Integer reasonMerge = mergeMap.get(rNum * 2 + 3 + "");

//                if("因为班长对员工工单的执行检查不到位".equals(map.get(key+"Reason"))){
//                    System.out.println(1213);
//                }

                //问题
                String whyKey = key+"Why";
                String swhy = map.getOrDefault(whyKey, "");
                //清除回车
                kpiFwhyProblemDto.setSwhy(StringHandleUtils.removeAllLineBreaks(swhy.strip()));

                //问题id
                kpiFwhyProblemDto.setIwhy(id+whyKey);
                //原因
                String reasonKey = key+"Reason";
                String sanswer = map.getOrDefault(reasonKey,"");

                kpiFwhyProblemDto.setSanswer(sanswer.strip());
                //原因id
                kpiFwhyProblemDto.setIanswer(id+reasonKey);
                //删除标记
                kpiFwhyProblemDto.setIdel(0);
                //创建人工号
                kpiFwhyProblemDto.setSregworkno(workno);


                if("one".equals(key)){
                    //第一层
                    //排除相同
                    if(!oneSet.contains(kpiFwhyProblemDto.getSanswer())){
                        kpiFwhyProblemDto.setIlevel(1);
                        kpiFwhyProblemDto.setIparentid("0_0");
                        kpiFwhyProblemDto.setTmpId(id);
                        upSwhyMap.put(key, kpiFwhyProblemDto.getSwhy()); //存储上节点问题名称
                        fwhyProblemOne.add(kpiFwhyProblemDto);
                        oneSet.add(kpiFwhyProblemDto.getSanswer());
                    }
                }else{
                    if(reasonMerge != null){
//                    if(reasonMerge != null && reasonMerge != 0){
                        kpiFwhyProblemDto.setTmpId(id);
                        kpiFwhyProblemDto.setSeveral(key);
                        if(upReasonMerge > 1 && reasonMerge != 1){
//                        if(upReasonMerge > 1 && num != 1){
                            //当上节点合并行大于1,这行被合并,问题名称取上一单元格
                            String upSwhy = upSwhyMap.getOrDefault(key,"");
//                            if(StrUtil.isBlank(kpiFwhyProblemDto.getSwhy())){
                            kpiFwhyProblemDto.setSwhy(upSwhy);
//                            }
                        }
                        upSwhyMap.put(key, kpiFwhyProblemDto.getSwhy()); //存储上节点问题名称
                        fwhyProblemList.add(kpiFwhyProblemDto);
                    }

                }
                rNum++;
            }
            num++;
        }
        // 按 问题_当前是几why 分组
        Map<String, List<KpiFwhyProblemDto>> groupedBySwhy = fwhyProblemList.stream()
                .filter(dto -> dto.getSwhy() != null && !dto.getSwhy().trim().isEmpty())
                .collect(Collectors.groupingBy(
                        dto -> dto.getSwhy() + "_" + dto.getSeveral()  // 例如 "101_A"
                ));

        // 按 tmpId_当前是几why 分组
        Map<String, List<KpiFwhyProblemDto>> groupedByTmpId = fwhyProblemList.stream()
                .collect(Collectors.groupingBy(
                        dto -> dto.getTmpId() + "_" + dto.getSeveral()  // 例如 "101_A"
                ));

        fwhyProblemTree.addAll(fwhyProblemOne);


        //查找下层
        generateTree(fwhyProblemOne,groupedByTmpId,groupedBySwhy,fwhyProblemTree,2);

        try {
            ObjectMapper mapper = new ObjectMapper();
            String json = mapper.writeValueAsString(fwhyProblemTree);
            System.out.println("结果 "+json);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        return fwhyProblemTree;
    }

    /**
     * 获取第一层之后的层级
     * 通过id查到第下层(第一条) -->通过为什么找名称找下层(多条)
     *
     * @param list
     * @param groupedByTmpId
     * @param groupedBySwhy
     * @param fwhyProblemTree
     */
    public void generateTree(List<KpiFwhyProblemDto> list,Map<String, List<KpiFwhyProblemDto>> groupedByTmpId,Map<String,
            List<KpiFwhyProblemDto>> groupedBySwhy,List<KpiFwhyProblemDto> fwhyProblemTree,int level){
        String levelKey = FwhyParameter.reasonsArray[level-1];
        HashSet set = new HashSet<>();
        //当前层级集合
        List<KpiFwhyProblemDto> currentList = new ArrayList<>();
        for (KpiFwhyProblemDto kpiFwhyProblemDto : list) {
            List<KpiFwhyProblemDto> twoList = groupedByTmpId.get(kpiFwhyProblemDto.getTmpId()+"_"+levelKey);
            KpiFwhyProblemDto kpiFwhyProblemTwo = null;
            if(twoList != null && !twoList.isEmpty()){
                kpiFwhyProblemTwo = twoList.get(0);
            }
            //为什么(找问题相同的数据)
            String swhy = null;
            if (kpiFwhyProblemTwo != null) {
                swhy = kpiFwhyProblemTwo.getSwhy();
            }
            List<KpiFwhyProblemDto> twoAllList = groupedBySwhy.get(swhy+"_"+levelKey);
            if(twoAllList == null){
                twoAllList = new ArrayList<>();
            }else{
                for (KpiFwhyProblemDto fwhyProblemDto : twoAllList) {
                    if(!handledFailures.containsKey(fwhyProblemDto.getIwhy())){
                        //排除相同同时排除为空的 !set.contains(fwhyProblemDto.getSanswer()) &&
                        if( StrUtil.isNotBlank(fwhyProblemDto.getSwhy())) {
//                if( StrUtil.isNotBlank(fwhyProblemDto.getSwhy())
//                        && StrUtil.isNotBlank(fwhyProblemDto.getSanswer())) {
                            fwhyProblemDto.setIlevel(level);
                            fwhyProblemDto.setIparentid(kpiFwhyProblemDto.getIwhy() + "_" + kpiFwhyProblemDto.getIanswer());
                            fwhyProblemTree.add(fwhyProblemDto);
                            set.add(fwhyProblemDto.getSanswer());
                            currentList.add(fwhyProblemDto);
                            //记录已经处理的
                            handledFailures.put(fwhyProblemDto.getIwhy(),"");
                        }
                    }
                }
            }

        }
        if(CollUtil.isNotEmpty(currentList) && level < 10){
            level++;
            generateTree(currentList,groupedByTmpId,groupedBySwhy,fwhyProblemTree,level);
        }
    }


    /**
     * 不达标KPI5why分析推送
     */
    @Override
    public void notStandardKpiFwhyJob() {

        //获取维护KPI(第三方)
        LambdaQueryWrapper kpiInfoWrapper = new LambdaQueryWrapper<KpiInfo>()
                .isNotNull(KpiInfo::getThirdName)
                .ne(KpiInfo::getThirdName, "");

        List<KpiInfo> kpiList = iKpiInfoService.list(kpiInfoWrapper);
        List<String> kpiCodeList = kpiList.stream().map(KpiInfo::getKpiCode).collect(Collectors.toList());

        //推送-新增
        Map<String,Object> map = getPushData(kpiCodeList,"1");
        if(map != null){
            kpiNotStandardData((List<FiveWhyAnalyse>) map.get("filteredList"), (Map<String, String>) map.get("analyseIdToIdMap"),"1");
        }

        //推送-修改
        map = getPushData(kpiCodeList,"3");
        if(map != null) {
            kpiNotStandardData((List<FiveWhyAnalyse>) map.get("filteredList"), (Map<String, String>) map.get("analyseIdToIdMap"), "3");
        }
        //推送-删除
        map = getPushData(kpiCodeList,"4");
        if(map != null) {
            kpiNotStandardData((List<FiveWhyAnalyse>) map.get("filteredList"), (Map<String, String>) map.get("analyseIdToIdMap"),"4");
        }

    }

    /**
     * 获取需要推送数据
     * kpiCodeList kpi集合
     * type 数据类型 1新增, 2修改, 3删除
     * 新增, 推送新增数据 和 记录表未成功数据
     * 修改/删除, 推送修改 且 记录修改时间小于5why修改时间
     *
     */
    public Map<String,Object> getPushData(List<String> kpiCodeList, String type){
        // 获取当前日期时间作为结束时间
        LocalDateTime endDateTime = LocalDateTime.now();
        // 获取30天前的日期时间作为开始时间
        LocalDateTime startDateTime = endDateTime.minusDays(30);
        // 定义日期时间格式
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        // 格式化输出
        String startDateStr = startDateTime.format(formatter);
        String endDateStr = endDateTime.format(formatter);
        LambdaQueryWrapper fiveWhyWrapper = null;
//        Map<String,Object> mapDel = new HashMap<>();
        if("1".equals(type)){
            //新增
            fiveWhyWrapper = new LambdaQueryWrapper<FiveWhyAnalyse>()
                .between(FiveWhyAnalyse::getCreateTime, startDateStr, endDateStr)
                .isNotNull(FiveWhyAnalyse::getKpiCode)
                .in(FiveWhyAnalyse::getKpiCode, kpiCodeList)
                .ne(FiveWhyAnalyse::getKpiCode, "");
        }else if("3".equals(type)){
            //修改
            fiveWhyWrapper = new LambdaQueryWrapper<FiveWhyAnalyse>()
                .between(FiveWhyAnalyse::getUpdateTime, startDateStr, endDateStr)
                .isNotNull(FiveWhyAnalyse::getKpiCode)
                .in(FiveWhyAnalyse::getKpiCode, kpiCodeList)
                .ne(FiveWhyAnalyse::getKpiCode, "");
        }
        List<FiveWhyAnalyse> fiveWhyList = new ArrayList<>();
        if("1".equals(type) || "3".equals(type)){
            fiveWhyList = iFiveWhyAnalyseService.list(fiveWhyWrapper);
        }else{
            //删除
            fiveWhyList = iFiveWhyAnalyseService.listDel(startDateStr, endDateStr, kpiCodeList);
        }

        List<String> idList = fiveWhyList.stream()
                .map(FiveWhyAnalyse::getId)
                .collect(Collectors.toList());

        if(idList != null && idList.isEmpty()){
            return null;
        }

        //获取推送记录
        LambdaQueryWrapper notstandardWrapper = new LambdaQueryWrapper<KpiNotstandardLog>()
                .in(KpiNotstandardLog::getAnalyseId, idList);
        List<KpiNotstandardLog> notstandardList = iKpiNotstandardLogService.list(notstandardWrapper);

        //推送成功数据
        // 首先从 notstandardList 中 新增提取所有推送成功的 analyseId 集合 修改/删除提取全部结果
        Set<String> excludeIds = notstandardList.stream()
                .filter(log -> (!"-1".equals(log.getState()) && "1".equals(type)))
                .map(KpiNotstandardLog::getAnalyseId)
                .collect(Collectors.toSet());

        List<FiveWhyAnalyse> filteredList = new ArrayList<>();
        //
        if("1".equals(type)){
            //新增
            // 然后过滤 fiveWhyList，只保留不在 excludeIds 中的元素(获取推送失败/未推送)
            filteredList = fiveWhyList.stream()
                    .filter(f -> !excludeIds.contains(f.getId()))
                    .collect(Collectors.toList());
        }else{
            //修改/删除 记录修改时间小于5why修改时间(数据被更新,还未推送)
            Map<String, Date> excludeMap = notstandardList.stream()
                    .collect(Collectors.toMap(
                            KpiNotstandardLog::getAnalyseId,  // Key mapper
                            KpiNotstandardLog::getUpdateTime,        // Value mapper
                            (existing, replacement) -> existing  // 处理重复key的合并函数
                    ));

            filteredList = fiveWhyList.stream()
                    .filter(f -> {
                        Date excludeDate = excludeMap.get(f.getId());  // 先根据ID获取对应的Date
                        return excludeDate != null && excludeDate.before(f.getUpdateTime());
                    })
                    .collect(Collectors.toList());
        }

        //筛选出需要推送且上次推送失败的()
        List<KpiNotstandardLog> notStandardList = notstandardList.stream()
                .filter(f -> !excludeIds.contains(f.getAnalyseId()))
                .collect(Collectors.toList());

        //生成map
        Map<String, String> analyseIdToIdMap = notStandardList.stream()
                .collect(Collectors.toMap(
                        KpiNotstandardLog::getAnalyseId,  // Key mapper
                        KpiNotstandardLog::getId,        // Value mapper
                        (existing, replacement) -> existing  // 处理重复key的合并函数
                ));

        Map<String,Object> map = new HashMap<>();
        //需要推送得数据
        map.put("filteredList",filteredList);
        //记录
        map.put("analyseIdToIdMap",analyseIdToIdMap);
        return map;
    }


    public static void main(String[] args) {
//        String str = "[{\"id\":\"1879864567038320640\",\"whyId\":\"1879864567038320640\",\"fwId\":\"1\",\"mIndex\":1,\"numMap\":{\"1\":1,\"2\":1,\"3\":1,\"4\":1,\"5\":1,\"6\":1,\"7\":1,\"8\":1,\"9\":1,\"10\":1,\"11\":1,\"12\":1,\"13\":1,\"14\":1,\"15\":1,\"16\":1,\"17\":1,\"18\":1,\"19\":1,\"20\":1,\"21\":1,\"22\":1,\"23\":1,\"24\":1,\"25\":1,\"26\":1,\"27\":1,\"28\":1,\"29\":1},\"oneWhy\":\"KPI名称：GLY-燕京集团北京工厂,周期为：2025-01-06|2025-01-12，目标值：79.4,实际值：78.1,差值为1.3\",\"oneReason\":\"因为瓶装4线LEF-周累低于计划值84.5%，实际值67.8%未达标，差值16.7\",\"twoWhy\":\"为什么瓶装4线LEF-周累低于计划值84.5%，实际值67.8%未达标，差值16.7？\",\"twoReason\":\"因为瓶装4线本周生产2*3手提,纸包机降速运行由55降速到43；\",\"threeWhy\":\"为什么纸包机降速运行？\",\"threeReason\":\"因为纸包机出纸板时经常两个纸板钩在一起或斜着出箱，从而影响了纸包机的生产效率。\",\"fourWhy\":\"为什么纸包机出纸板时经常两个纸板钩在一起或斜着出箱？\",\"fourReason\":\"因为纸包机频繁更换不同厂家的纸箱，导致每次换厂家时都需要重新调整\",\"problem\":\"GLY-燕京集团北京工厂\",\"problemDes\":\"KPI名称：GLY-燕京集团北京工厂,周期为：2025-01-06|2025-01-12，目标值：79.4,实际值：78.1,颜色为：\",\"problemDesc\":\"KPI名称：GLY-燕京集团北京工厂,周期为：2025-01-06|2025-01-12，目标值：79.4,实际值：78.1,颜色为：\"},{\"id\":\"1879864567038320641\",\"whyId\":\"1879864567038320641\",\"fwId\":\"1\",\"mIndex\":2,\"numMap\":{\"1\":1,\"2\":1,\"3\":1,\"4\":1,\"5\":1,\"6\":1,\"7\":3,\"8\":3,\"9\":3,\"10\":3,\"11\":3,\"12\":3,\"13\":3,\"14\":3,\"15\":3,\"16\":3,\"17\":3,\"18\":3,\"19\":3,\"20\":3,\"21\":3,\"22\":3,\"23\":3,\"24\":3,\"25\":3,\"26\":3,\"27\":3,\"28\":3,\"29\":3},\"oneWhy\":\"KPI名称：GLY-燕京集团北京工厂,周期为：2025-01-06|2025-01-12，目标值：79.4,实际值：78.1,差值为1.3\",\"oneReason\":\"因为瓶装4线LEF-周累低于计划值84.5%，实际值67.8%未达标，差值16.7\",\"twoWhy\":\"为什么瓶装4线LEF-周累低于计划值84.5%，实际值67.8%未达标，差值16.7？\",\"twoReason\":\"因为瓶装4线本周生产2*3手提,纸包机降速运行由55降速到43；\",\"threeWhy\":\"为什么纸包机降速运行？\",\"threeReason\":\"因为车间纸包机机手对新品种2*3换线调整方法不熟\",\"problem\":\"GLY-燕京集团北京工厂\",\"problemDes\":\"KPI名称：GLY-燕京集团北京工厂,周期为：2025-01-06|2025-01-12，目标值：79.4,实际值：78.1,颜色为：\",\"problemDesc\":\"KPI名称：GLY-燕京集团北京工厂,周期为：2025-01-06|2025-01-12，目标值：79.4,实际值：78.1,颜色为：\"},{\"id\":\"1879864567038320642\",\"whyId\":\"1879864567038320642\",\"fwId\":\"1\",\"mIndex\":3,\"numMap\":{\"1\":1,\"2\":1,\"3\":2,\"4\":2,\"5\":2,\"6\":2,\"7\":2,\"8\":2,\"9\":2,\"10\":2,\"11\":2,\"12\":2,\"13\":2,\"14\":2,\"15\":2,\"16\":2,\"17\":2,\"18\":2,\"19\":2,\"20\":2,\"21\":2,\"22\":2,\"23\":2,\"24\":2,\"25\":2,\"26\":2,\"27\":2,\"28\":2,\"29\":2},\"oneWhy\":\"KPI名称：GLY-燕京集团北京工厂,周期为：2025-01-06|2025-01-12，目标值：79.4,实际值：78.1,差值为1.3\",\"oneReason\":\"因为瓶装5线LEF-周累低于计划值76.8%，实际值55.3%，未达标，差值21.5\\n\",\"twoWhy\":\"为什么瓶装5线LEF-周累低于计划值76.8%，实际值55.3%，未达标，差值21.5？\",\"twoReason\":\"因为灌装压盖机改造后进出口链道运转不平稳\",\"threeWhy\":\"为什么灌装压盖机改造后进口链道运转不平稳\",\"threeReason\":\"因为酒机进口无压力单排链道验瓶机出口拐弯处经常出现倒、卡瓶导致酒机进口供瓶不畅停机。\",\"fourWhy\":\"为什么酒机进口无压力单排链道验瓶机出口拐弯处经常出现倒、卡瓶\",\"fourReason\":\"因为酒机搬迁后进口无压力单排链道为瓶9线与瓶5链道拼接而成，由于链道尺寸不一，多为焊接且链道不平，电机控制也不平稳。\",\"problem\":\"GLY-燕京集团北京工厂\",\"problemDes\":\"KPI名称：GLY-燕京集团北京工厂,周期为：2025-01-06|2025-01-12，目标值：79.4,实际值：78.1,颜色为：\",\"problemDesc\":\"KPI名称：GLY-燕京集团北京工厂,周期为：2025-01-06|2025-01-12，目标值：79.4,实际值：78.1,颜色为：\"},{\"id\":\"1879864567038320643\",\"whyId\":\"1879864567038320643\",\"fwId\":\"1\",\"mIndex\":4,\"numMap\":{\"1\":1,\"2\":1,\"3\":2,\"4\":2,\"5\":2,\"6\":2,\"7\":6,\"8\":6,\"9\":6,\"10\":6,\"11\":6,\"12\":6,\"13\":6,\"14\":6,\"15\":6,\"16\":6,\"17\":6,\"18\":6,\"19\":6,\"20\":6,\"21\":6,\"22\":6,\"23\":6,\"24\":6,\"25\":6,\"26\":6,\"27\":6,\"28\":6,\"29\":6},\"oneWhy\":\"KPI名称：GLY-燕京集团北京工厂,周期为：2025-01-06|2025-01-12，目标值：79.4,实际值：78.1,差值为1.3\",\"oneReason\":\"因为瓶装5线LEF-周累低于计划值76.8%，实际值55.3%，未达标，差值21.5\\n\",\"twoWhy\":\"为什么瓶装5线LEF-周累低于计划值76.8%，实际值55.3%，未达标，差值21.5？\",\"twoReason\":\"因为灌装压盖机改造后进出口链道运转不平稳\",\"threeWhy\":\"为什么灌装压盖机改造后进口链道运转不平稳\",\"threeReason\":\"因为灌装压盖机改造后出口增加前液位检测，击打出不合格酒时，酒会弹回输送链道，导致停车\",\"fourWhy\":\"为什么击打出后，会弹回输送链道？\",\"fourReason\":\"因为场地有限且出口链道也是拼接而成，击打链道窄，又与定阀取样击打器挨着，使这部分链道没有链道护帮\",\"problem\":\"GLY-燕京集团北京工厂\",\"problemDes\":\"KPI名称：GLY-燕京集团北京工厂,周期为：2025-01-06|2025-01-12，目标值：79.4,实际值：78.1,颜色为：\",\"problemDesc\":\"KPI名称：GLY-燕京集团北京工厂,周期为：2025-01-06|2025-01-12，目标值：79.4,实际值：78.1,颜色为：\"},{\"id\":\"1879864567038320644\",\"whyId\":\"1879864567038320644\",\"fwId\":\"1\",\"mIndex\":5,\"numMap\":{\"1\":1,\"2\":1,\"3\":2,\"4\":2,\"5\":5,\"6\":5,\"7\":5,\"8\":5,\"9\":5,\"10\":5,\"11\":5,\"12\":5,\"13\":5,\"14\":5,\"15\":5,\"16\":5,\"17\":5,\"18\":5,\"19\":5,\"20\":5,\"21\":5,\"22\":5,\"23\":5,\"24\":5,\"25\":5,\"26\":5,\"27\":5,\"28\":5,\"29\":5},\"oneWhy\":\"KPI名称：GLY-燕京集团北京工厂,周期为：2025-01-06|2025-01-12，目标值：79.4,实际值：78.1,差值为1.3\",\"oneReason\":\"因为瓶装5线LEF-周累低于计划值76.8%，实际值55.3%，未达标，差值21.5\\n\",\"twoWhy\":\"为什么瓶装5线LEF-周累低于计划值76.8%，实际值55.3%，未达标，差值21.5？\",\"twoReason\":\"因为灌装压盖机改造后进出口链道运转不平稳\",\"threeWhy\":\"为什么灌装压盖机改造后出口链道运转不平稳\",\"threeReason\":\"因为酒机出口单排变多排时酒瓶撞击有倒瓶现象\",\"fourWhy\":\"为什么酒机出口单排变多排时酒瓶撞击有倒瓶现象\",\"fourReason\":\"因为酒机出口单排变多排链道没有变频控制，无法与酒机同速\",\"problem\":\"GLY-燕京集团北京工厂\",\"problemDes\":\"KPI名称：GLY-燕京集团北京工厂,周期为：2025-01-06|2025-01-12，目标值：79.4,实际值：78.1,颜色为：\",\"problemDesc\":\"KPI名称：GLY-燕京集团北京工厂,周期为：2025-01-06|2025-01-12，目标值：79.4,实际值：78.1,颜色为：\"}]";
//        String mergeRule ="[{\"0\":5,\"1\":5,\"2\":5,\"3\":2,\"4\":2,\"5\":2,\"6\":2,\"7\":1,\"8\":1,\"9\":1,\"10\":1,\"11\":1,\"12\":1,\"13\":1,\"14\":1,\"15\":1,\"16\":1,\"17\":1,\"18\":1,\"19\":1,\"20\":1,\"21\":1,\"22\":1,\"23\":1,\"24\":1,\"25\":1,\"26\":1,\"27\":1,\"28\":1,\"29\":1,\"30\":5,\"31\":5},{\"0\":0,\"1\":0,\"2\":0,\"3\":0,\"4\":0,\"5\":0,\"6\":0,\"7\":1,\"8\":1,\"9\":1,\"10\":1,\"11\":1,\"12\":1,\"13\":1,\"14\":1,\"15\":1,\"16\":1,\"17\":1,\"18\":1,\"19\":1,\"20\":1,\"21\":1,\"22\":1,\"23\":1,\"24\":1,\"25\":1,\"26\":1,\"27\":1,\"28\":1,\"29\":1,\"30\":0,\"31\":0},{\"0\":0,\"1\":0,\"2\":0,\"3\":3,\"4\":3,\"5\":2,\"6\":2,\"7\":1,\"8\":1,\"9\":1,\"10\":1,\"11\":1,\"12\":1,\"13\":1,\"14\":1,\"15\":1,\"16\":1,\"17\":1,\"18\":1,\"19\":1,\"20\":1,\"21\":1,\"22\":1,\"23\":1,\"24\":1,\"25\":1,\"26\":1,\"27\":1,\"28\":1,\"29\":1,\"30\":0,\"31\":0},{\"0\":0,\"1\":0,\"2\":0,\"3\":0,\"4\":0,\"5\":0,\"6\":0,\"7\":1,\"8\":1,\"9\":1,\"10\":1,\"11\":1,\"12\":1,\"13\":1,\"14\":1,\"15\":1,\"16\":1,\"17\":1,\"18\":1,\"19\":1,\"20\":1,\"21\":1,\"22\":1,\"23\":1,\"24\":1,\"25\":1,\"26\":1,\"27\":1,\"28\":1,\"29\":1,\"30\":0,\"31\":0},{\"0\":0,\"1\":0,\"2\":0,\"3\":0,\"4\":0,\"5\":1,\"6\":1,\"7\":1,\"8\":1,\"9\":1,\"10\":1,\"11\":1,\"12\":1,\"13\":1,\"14\":1,\"15\":1,\"16\":1,\"17\":1,\"18\":1,\"19\":1,\"20\":1,\"21\":1,\"22\":1,\"23\":1,\"24\":1,\"25\":1,\"26\":1,\"27\":1,\"28\":1,\"29\":1,\"30\":0,\"31\":0}]";
//        fiveWhyAnalysis(str,mergeRule,"00012343");
    }



}
