package com.keytech.modules.home.service.impl;

import cn.hutool.core.collection.CollStreamUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUnit;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import com.alibaba.fastjson.JSONArray;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.google.common.collect.Lists;
import com.keytech.base.service.impl.BaseServiceImpl;
import com.keytech.modules.action.plan.service.IActionPlanTrackService;
import com.keytech.modules.cop.dto.CopPlanRank;
import com.keytech.modules.cop.service.ICopScheduleService;
import com.keytech.modules.home.dto.ReachTheStandardVo;
import com.keytech.modules.home.dto.StatisticalVo;
import com.keytech.modules.home.dto.WSActionStatisticsDto;
import com.keytech.modules.home.mapper.HomeMapper;
import com.keytech.modules.home.service.IHomeService;
import com.keytech.modules.kpi.entity.KpiInfo;
import com.keytech.modules.kpi.entity.KpiReportActualMonthValue;
import com.keytech.modules.kpi.entity.KpiReportActualWeekValue;
import com.keytech.modules.kpi.service.IKpiExceptionSummaryService;
import com.keytech.modules.kpi.service.IKpiInfoService;
import com.keytech.modules.kpi.service.IKpiReportActualMonthValueService;
import com.keytech.modules.kpi.service.IKpiReportActualWeekValueService;
import com.keytech.modules.kpi.util.DateUtils;
import com.keytech.modules.manage.check.service.IManageCheckListService;
import com.keytech.modules.mdm3.entity.MdmWeek;
import com.keytech.modules.mdm3.service.IMdmWeekService;
import com.keytech.modules.meeting.service.IMeetingService;
import com.keytech.modules.period.service.IPeriodVerificationScheduleService;
import com.keytech.modules.pi.dto.PiGreenDataDto;
import com.keytech.modules.pi.service.IPiAbnormalAnalysisEvaluationService;
import com.keytech.modules.pi.service.IPiAbnormalAnalysisService;
import com.keytech.modules.pi.service.IPiCustomizeTableService;
import com.keytech.modules.pi.service.IPiStandardDataService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.config.TenantContext;
import org.jeecg.modules.system.entity.SysDepart;
import org.jeecg.modules.system.entity.SysUser;
import org.jeecg.modules.system.service.ISysDepartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @Description: 首页
 * @Author: hq
 * @Date:   2025-02-07
 * @Version: V1.0
 */
@Service
@Slf4j
public class HomeServiceImpl extends BaseServiceImpl<HomeMapper, KpiInfo> implements IHomeService {

    @Autowired
    private IActionPlanTrackService actionPlanTrackService;

    @Autowired
    private IKpiInfoService kpiInfoService;
    @Autowired
    private IKpiReportActualMonthValueService monthValueService;
    @Autowired
    private IKpiReportActualWeekValueService weekValueService;
    @Autowired
    private IMdmWeekService mdmWeekService;
    @Autowired
    private ISysDepartService sysDepartService;
    @Autowired
    private IManageCheckListService manageCheckListService;
    @Autowired
    private ICopScheduleService copScheduleService;
    @Autowired
    private IPeriodVerificationScheduleService periodVerificationScheduleService;
    @Resource
    private IPiStandardDataService piStandardDataService;


    private final Map<String, Function<WSActionStatisticsDto, JSONObject>> functionMap = new ConcurrentHashMap<>();
    private final Map<String, String> deptMap;
    private final Map<String, String> idDeptMap;

    public HomeServiceImpl(ISysDepartService departService, IPiCustomizeTableService piCustomizeTableService) {
        //获取管理员租户信息(住户)
        SysUser sysUser = piCustomizeTableService.getAdminUser();
        Integer tenantId = sysUser.getLoginTenantId();
        TenantContext.setTenant(tenantId+"");

        deptMap = CollStreamUtil.toMap(departService.list(), SysDepart::getOrgCode, SysDepart::getDepartName);
        idDeptMap = CollStreamUtil.toMap(departService.list(), SysDepart::getId, SysDepart::getDepartName);
        //lineIdNameMap = CollStreamUtil.toMap(list, MdmLine::getId, MdmLine::getLineName);
        //lineGroup = CollStreamUtil.groupBy(list, MdmLine::getSysOrgCode, Collectors.mapping(MdmLine::getId, Collectors.toList()));
        initFunctionMap();
    }

    private void initFunctionMap() {
        //---------- 工厂首页统计 函数-----
//        //kpi达标率
        functionMap.put("kpiComplianceRate", this::kpiComplianceRate);
//        //kpi排名
        functionMap.put("kpiComplianceRank", this::kpiComplianceRank);
        //pi达标率
        functionMap.put("piComplianceRate", this::piComplianceRate);
        //pi排名
        functionMap.put("piComplianceRank", this::piComplianceRank);
        //行动跟踪
        functionMap.put("trackPlan", this::trackPlan);
        //分层检查
        functionMap.put("manageCheck", this::manageCheck);
        //cop计划
        functionMap.put("cop", this::copPlanRank);
        //期间核查
        functionMap.put("periodPlan", this::periodPlanRank);

        //---------- 车间首页统计 函数-----
        functionMap.put("workshopStatistics", this::workshopStatistics);
    }

    @Override
    public Map<String, Function<WSActionStatisticsDto, JSONObject>> getFunctionMap() {
        return functionMap;
    }


    /**
     * kpi 达标率
     *
     * @param dto
     * @return
     */
    public JSONObject kpiComplianceRate(WSActionStatisticsDto dto) {
        List<ReachTheStandardVo> list = getReachTheStandarByKpi(dto);
        JSONObject hashMap = new JSONObject();
        if (CollUtil.isNotEmpty(list)) {
            //总数
            int size = list.size();
            //绿区
            int lvSum = (int) list.stream().filter(standardVo -> standardVo.getColor() == 1).count();
            StatisticalVo vo = new StatisticalVo();
            vo.setName("达标率");
            vo.setSize(size);
            if (0 == lvSum) {
                vo.setLv(0);
            } else {
                vo.setLv(NumberUtil.div(lvSum, size));
            }
            hashMap.putOpt("工厂KPI达标率", vo);
        } else {
            hashMap.putOpt("工厂KPI达标率", new StatisticalVo("达标率", 0, 0, null));
        }

        return hashMap;

    }

    /**
     * KPI达标率
     * @param dto
     * @return
     */
    private List<ReachTheStandardVo> getReachTheStandarByKpi(WSActionStatisticsDto dto) {
        String opTime = dto.getOpTime();
        String edTime = dto.getEdTime();
        String factory = dto.getFactory();
        DateTime startTime = DateUtil.parse(opTime);
        DateTime endTime = DateUtil.parse(edTime);
        List<KpiReportActualMonthValue> monthList = new ArrayList<>();
        List<KpiReportActualWeekValue> weekList = new ArrayList<>();
        // 统计KPI周月报上的KPI && 总数只统计有颜色
        List<String> kpiCodeList = homeStatisticsRule();
        boolean isMoreThanAWeek = DateUtil.between(startTime, endTime, DateUnit.DAY) > 7;
        String startDate = opTime.substring(0, 10);
        String endDate = edTime.substring(0, 10);

        //获取KPI
        List<KpiInfo> kpiList = kpiInfoService.list(new LambdaQueryWrapper<KpiInfo>()
                .eq(KpiInfo::getVisible, 1)
                .eq(KpiInfo::getActiveLook, 1)
                .isNotNull(KpiInfo::getKpiCode));

        if (isMoreThanAWeek) {
            monthList = monthValueService.lambdaQuery()
                    .eq(StringUtils.hasLength(factory), KpiReportActualMonthValue::getReportId, factory)
                    .ge(KpiReportActualMonthValue::getStartTime, startDate)
                    .le(KpiReportActualMonthValue::getEndTime, endDate)
                    .ne(KpiReportActualMonthValue::getColour, 0)
                    .in(KpiReportActualMonthValue::getKpiCode, kpiCodeList)
                    .list();
            //替换部门 ReportId 替换成 kpi层级部门
            monthList.forEach(week -> {
                kpiList.forEach(kpi -> {
                    if (week.getKpiCode().equals(kpi.getKpiCode())) {
                        week.setReportId(kpi.getDeptCode());
                    }
                });
            });
        } else {
            weekList = weekValueService.lambdaQuery()
                    .eq(StringUtils.hasLength(factory), KpiReportActualWeekValue::getReportId, factory)
                    .ge(KpiReportActualWeekValue::getStartTime, startDate)
                    .le(KpiReportActualWeekValue::getEndTime, endDate)
                    .ne(KpiReportActualWeekValue::getColour, 0)
                    .in(KpiReportActualWeekValue::getKpiCode, kpiCodeList)
                    .list();
            //替换部门 ReportId 替换成 kpi层级部门
            weekList.forEach(week -> {
                kpiList.forEach(kpi -> {
                    if (week.getKpiCode().equals(kpi.getKpiCode())) {
                        week.setReportId(kpi.getDeptCode());
                    }
                });
            });
        }
        List<ReachTheStandardVo> list2 = Lists.newArrayList();
        list2.addAll(monthList.parallelStream().map(value -> new ReachTheStandardVo(value.getReportId(), value.getColour(), value.getKpiCode(), null, value.getValue())).collect(Collectors.toList()));
        list2.addAll(weekList.parallelStream().map(value -> new ReachTheStandardVo(value.getReportId(), value.getColour(), value.getKpiCode(), null, value.getValue())).collect(Collectors.toList()));

        return list2;
    }

    /**
     * 统计KPI周月报上的KPI && 总数只统计有颜色
     *
     * @return
     */
    public List<String> homeStatisticsRule() {
        List<KpiInfo> infos = kpiInfoService.list(new LambdaQueryWrapper<KpiInfo>()
                .eq(KpiInfo::getVisible, 1)
                .eq(KpiInfo::getActiveLook, 1)
                .isNotNull(KpiInfo::getKpiCode));
        List<String> collect = infos.stream()
                .map(KpiInfo::getKpiCode)
                .collect(Collectors.toList());
        if (CollUtil.isEmpty(collect)) {
            collect.add("none");
        }
        return collect;
    }

    /**
     * KPI排行
     * @param dto
     * @return
     */
    public JSONObject kpiComplianceRank(WSActionStatisticsDto dto) {
        dto.setFactory(null);
        List<ReachTheStandardVo> list = getReachTheStandarByKpi(dto);
        Map<String, String> kpiMap = CollStreamUtil.toMap(kpiInfoService.list(), KpiInfo::getKpiCode, KpiInfo::getKpiName);
        Map<String, String> filteredBuMap = deptMap.entrySet().stream().filter(entry -> entry.getValue().contains("部") && !entry.getValue().contains("部门") && !entry.getValue().contains("财务部") && !entry.getValue().contains("市场部")).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        List<StatisticalVo> listData = new ArrayList<>();
        JSONObject hashMap = new JSONObject();
        if (!list.isEmpty()) {
            // 过滤掉工厂kpi 不参加排名
            Map<String, List<ReachTheStandardVo>> groupedByPrefix = list.stream()
                    .filter(vo -> StrUtil.isNotEmpty(vo.getReportId()) && vo.getReportId().length() > 6)
                    .collect(Collectors.groupingBy(ReachTheStandardVo::getReportId));
            // 遍历 groupedByPrefix 并处理
            groupedByPrefix.forEach((k, v) -> {
                if (filteredBuMap.containsKey(k)) {
                    // 总数
                    int size = v.size();
                    // 达标
                    int count = (int) v.stream().filter(R -> 1 == R.getColor()).count();
                    StatisticalVo vo = new StatisticalVo(filteredBuMap.get(k), size, (0 == count) ? 0 : (double) count / size, new JSONArray(Collections.singletonList(v.stream().filter(R -> 1 != R.getColor()).sorted(Comparator.comparing(ReachTheStandardVo::getKpiCode)).collect(Collectors.toList()))));
                    v.forEach(r -> r.setKpiName(kpiMap.getOrDefault(r.getKpiCode(), null)));
                    listData.add(vo);
                }
            });
            // 对于 filteredBuMap 中没有在 groupedByPrefix 中出现的键
            filteredBuMap.keySet().stream().filter(k -> !groupedByPrefix.containsKey(k)).forEach(k -> listData.add(new StatisticalVo(filteredBuMap.get(k), 0, 0, null)));
        } else {
            // 如果 list2 为空
            filteredBuMap.forEach((k, v) -> listData.add(new StatisticalVo(v, 0, 0, null)));
        }
        List<StatisticalVo> collect = listData.stream().sorted(Comparator.comparing(StatisticalVo::getLv).thenComparing(StatisticalVo::getName).reversed()).collect(Collectors.toList());
        hashMap.putOpt("KPI达标率排名", collect);
        return hashMap;
    }


    /**
     * PI达标率
     * @param dto
     * @return
     */
    public JSONObject piComplianceRate(WSActionStatisticsDto dto) {
        List<PiGreenDataDto> list = piStandardDataService.getPayCloseAttentionPi(dto.getFactory(), dto.getWorkShopCode(), null, dto.getOpTime(), dto.getEdTime());
        Map<String, Integer> map = getPiData(list);
        int green = map.get("green");
        int all = map.get("all");
        JSONObject hashMap = new JSONObject();
        StatisticalVo vo = new StatisticalVo();
        vo.setName("达标率");
        vo.setSize(green);
        vo.setTotalSize(all);
        if (0 == green || 0 == all) {
            vo.setLv(0);
        } else {
            vo.setLv(NumberUtil.div(green, all));
        }
        hashMap.putOpt("工厂PI达标率", vo);
        return hashMap;
    }

    /**
     * PI排名
     * @param dto 查询参数
     * @return 数据集合
     */
    public JSONObject piComplianceRank(WSActionStatisticsDto dto) {
        //返回数据集合
        List<StatisticalVo> resultList = new ArrayList<>();
        //查询数据
        List<PiGreenDataDto> piGreenDataDtoList = piStandardDataService.getPayCloseAttentionPi(dto.getFactory(), dto.getWorkShopCode(), null, dto.getOpTime(), dto.getEdTime());
        //按产线分组
        Map<String, List<PiGreenDataDto>> groupedByLineCode = piGreenDataDtoList.stream()
                .collect(Collectors.groupingBy(PiGreenDataDto::getLineCode));
        //获取产线
        List<SysDepart> lineList = sysDepartService.lambdaQuery()
                .ge(SysDepart::getOrgType, "6")
                .list();
        for (SysDepart sysDepart : lineList) {
            StatisticalVo statisticalVo = new StatisticalVo();
            statisticalVo.setName(sysDepart.getDepartName());
            int size = 0;
            int totalSize = 0;
            double lv = 0.00;
            if(groupedByLineCode.containsKey(sysDepart.getOrgCode())){
                Map<String, Integer> map = getPiData(groupedByLineCode.get(sysDepart.getOrgCode()));
                size = map.get("green");
                totalSize = map.get("all");
                if (0 == size || 0 == totalSize) {
                    lv = 0;
                } else {
                    lv = NumberUtil.div(size, totalSize);
                }
            }
            resultList.add(new StatisticalVo(sysDepart.getDepartName(), size, lv, totalSize));
        }
        JSONObject hashMap = new JSONObject();
        hashMap.set("PI达标率排名", resultList.stream().sorted(Comparator.comparing(StatisticalVo::getLv).thenComparing(StatisticalVo::getName).reversed()).toList());
        return hashMap;
    }

    /**
     * 行动跟踪
     * @param wsActionStatisticsDto
     * @return
     */
    public JSONObject trackPlan(WSActionStatisticsDto wsActionStatisticsDto) {
        Map<String, Object> map = actionPlanTrackService.queryIndexSummaryData(wsActionStatisticsDto);
        JSONObject hashMap = new JSONObject(map);
        if(hashMap == null || (hashMap != null && hashMap.size() == 0)){
            hashMap = new JSONObject();
            Map<String, Object> mapR = new HashMap<>(32);
            mapR.put("工厂行动计划完成率", 0);
            mapR.put("行动计划完成率排名", Lists.newArrayList());
            hashMap.putOpt("行动计划", mapR);
        }
        return hashMap;
    }

    /**
     * 分层检查
     */
    public JSONObject manageCheck(WSActionStatisticsDto wsActionStatisticsDto) {

        JSONObject jsonObject = manageCheckListService.queryIndexSummary(wsActionStatisticsDto);
        if(jsonObject != null && jsonObject.isEmpty()){
            jsonObject = new JSONObject();
            Map<String, Object> map = new HashMap<>(32);
            map.put("工厂整改行动计划完成率", 0);
            map.put("分层检查完成率排名", Lists.newArrayList());
            jsonObject.putOpt("分层检查", map);
        }
        return jsonObject;
    }

    /**
     * cop计划
     * @param wsActionStatisticsDto
     * @return
     */
    private JSONObject copPlanRank(WSActionStatisticsDto wsActionStatisticsDto) {
        JSONObject jsonObject = new JSONObject();
        String opTime = wsActionStatisticsDto.getOpTime();
        String edTime = wsActionStatisticsDto.getEdTime();
        List<CopPlanRank> list = null;
        try {
            list = copScheduleService.copPlanRank(wsActionStatisticsDto);
        } catch (Exception e) {
            list = null;
        }
        if(list == null || list.isEmpty()){
            List<StatisticalVo> collect = new ArrayList<>();
            jsonObject.putOpt("COP完成率排名", collect);
        }else{
            jsonObject.putOpt("COP完成率排名", list);
        }
        return jsonObject;
    }

    //期间核查
    private JSONObject periodPlanRank(WSActionStatisticsDto wsActionStatisticsDto) {
        JSONObject hashMap = new JSONObject();

        List<CopPlanRank> listR = null;
        try {
            listR  = periodVerificationScheduleService.periodPlanRank(wsActionStatisticsDto);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if(listR == null || listR.isEmpty()){
            List<StatisticalVo> collect = new ArrayList<>();
            hashMap.putOpt("期间核查完成率排名", collect);
        }else{
            hashMap.putOpt("期间核查完成率排名", listR);
        }

        return hashMap;
    }

    /**
     * 车间PI合格率
     * @param wsActionStatisticsDto
     * @return
     */
    public JSONObject workShopComplianceRate(WSActionStatisticsDto wsActionStatisticsDto) {
        JSONObject jsonObject = new JSONObject();
        List<PiGreenDataDto> list = piStandardDataService.getPayCloseAttentionPi(wsActionStatisticsDto.getFactory(), wsActionStatisticsDto.getWorkShopCode(), null, wsActionStatisticsDto.getOpTime(), wsActionStatisticsDto.getEdTime());
        Map<String, Integer> map = getPiData(list);
        jsonObject.putOpt("车间PI合格率", 0);
        jsonObject.putOpt("总数", map.get("all"));
        jsonObject.putOpt("green", map.get("green"));
        jsonObject.putOpt("red", map.get("red"));
        jsonObject.putOpt("yellow", map.get("yellow"));
        return jsonObject;
    }

    //个人行动计划完成率
    public JSONObject dutyTrackPlan(WSActionStatisticsDto wsActionStatisticsDto) {
        //个人行动计划完成率
//        JSONObject map = new JSONObject();
//        map.putOpt("本月计划完成任务", 0);
//        map.putOpt("延迟", 0);
//        map.putOpt("正在进行", 0);
//        map.putOpt("已完成", 0);
        Map<String, Object> map = actionPlanTrackService.queryIndexSummaryData(wsActionStatisticsDto);
        JSONObject jsonObject = (JSONObject)map.get("individualPlan");
        if(jsonObject == null || (jsonObject!=null && jsonObject.size()==0)){
            jsonObject= new JSONObject();
            jsonObject.putOpt("本月计划完成任务", 0);
            jsonObject.putOpt("延迟", 0);
            jsonObject.putOpt("正在进行", 0);
            jsonObject.putOpt("已完成", 0);
        }
        return jsonObject;
    }

    /**
     * 关键KPI累计达标率
     * @param wsActionStatisticsDto
     * @return
     */
    private JSONObject cruxKpiComplianceRate(WSActionStatisticsDto wsActionStatisticsDto) {

        String workShopCode = wsActionStatisticsDto.getWorkShopCode();
        //当前时间
        DateTime date = DateUtil.date();
        int year = date.year();
        //获取当前是哪周 存在跨年的情况
//        int weekIndex = WeekUtils.getWeekIndex(date);
//        if (weekIndex == 1) {
//            weekIndex = 53;
//            year = year - 1;
//        }
        //生成上周数据
//        Map<Integer, String> weekOfYear = WeekUtils.getWeekOfYear(year);
        //跨年
        List yearList = new ArrayList<>();
        yearList.add(year);
        yearList.add(year+1);
        //获取第几周
        LambdaQueryWrapper<MdmWeek> queryWrapper = new LambdaQueryWrapper();
        queryWrapper.in(MdmWeek::getYear, yearList);
        queryWrapper.le(MdmWeek::getStartDate, DateUtils.formatDate(date))
                .ge(MdmWeek::getEndDate, DateUtils.formatDate(date));
        List<MdmWeek> weekList = mdmWeekService.list(queryWrapper);
        String yearStr = ""; //当前周所属年份
        String prevYearStr = ""; //当前周上一年份
        int weekIndex = 0;
        if(weekList != null && !weekList.isEmpty()){
            MdmWeek mdmWeek = weekList.get(0);
            yearStr = mdmWeek.getYear();
            weekIndex = mdmWeek.getWeekNumber();
        }
        Map<Integer, String> weekOfYear = new HashMap<>();
        String weekStr = "";
        int upWeekIndex = 1;
        if(weekIndex == 1){
            //取上一年最后一周
            //得到上一年
            prevYearStr = String.valueOf(Integer.parseInt(yearStr) - 1);
            weekOfYear = mdmWeekService.getWeekOfYear(prevYearStr);
            upWeekIndex = weekOfYear.size();
            //取最后一条周
            weekStr = weekOfYear.get(upWeekIndex);
        }else{
            //取当前年
            weekOfYear = mdmWeekService.getWeekOfYear(yearStr);
            upWeekIndex = weekIndex - 1;
            weekStr = weekOfYear.get(upWeekIndex);
        }

        List<String> weekKeys = StrUtil.split(weekStr, "|");
        //上月
        DateTime dateTime = DateUtil.offsetMonth(date, -1);
        int upMonth = dateTime.month() + 1;
        String formattedMonth = String.format("%02d", upMonth);
        String currentMonth = year + formattedMonth;
        //开始月
        String yearMonth = year + "01";
        //关键kpi累计达标率
        JSONObject mapR = new JSONObject();
        // 统计KPI周月报上的KPI && 总数只统计有颜色
        List<String> kpiCodeList = homeStatisticsRule();
        //年
//        List<MdmKpiReportActualMonthValue> list1 = monthValueService.list(new LambdaQueryWrapper<MdmKpiReportActualMonthValue>()
//                .eq(MdmKpiReportActualMonthValue::getReportId, workShopCode)
//                .eq(MdmKpiReportActualMonthValue::getYear, year)
//                .ne(MdmKpiReportActualMonthValue::getColour, 0)
//                .in(MdmKpiReportActualMonthValue::getKpiCode, kpiCodeList)
//                .apply("CHAR_LENGTH(kpi_code) <={0}", 12));
//        //年达标数
//       月累 当年开始到上月的月累
        List<KpiReportActualMonthValue> monthList = monthValueService.list(new LambdaQueryWrapper<KpiReportActualMonthValue>()
                .eq(KpiReportActualMonthValue::getReportId, workShopCode)
                .le(KpiReportActualMonthValue::getMonth, currentMonth)
                .ge(KpiReportActualMonthValue::getMonth, yearMonth)
                .ne(KpiReportActualMonthValue::getColour, 0)
                .in(KpiReportActualMonthValue::getKpiCode, kpiCodeList)
                .apply("CHAR_LENGTH(kpi_code) <={0}", 12));
        //月达标数
        int upYueDaBiao = 0, upYueSum = 0, yueDaBiao = 0;
        for (KpiReportActualMonthValue mdmKpiReportActualMonthValue : monthList) {
            String month = mdmKpiReportActualMonthValue.getMonth() + "";
            int colour = mdmKpiReportActualMonthValue.getColour();
            if (colour == 1) {
                yueDaBiao++;
            }
            if (Objects.equals(month, currentMonth)) {
                if (colour == 1) {
                    upYueDaBiao++;
                }
                upYueSum++;
            }
        }
        double yueDaBiaoLv = (0 == yueDaBiao) ? 0 : NumberUtil.div(yueDaBiao, monthList.size());
        double upYueDaBiaoLv = (0 == upYueDaBiao) ? 0 : NumberUtil.div(upYueDaBiao, upYueSum);
        //上周达标
        List<KpiReportActualWeekValue> list3 = weekValueService.list(new LambdaQueryWrapper<KpiReportActualWeekValue>()
                .eq(KpiReportActualWeekValue::getReportId, workShopCode)
                .ge(KpiReportActualWeekValue::getStartTime, weekKeys.get(0))
                .le(KpiReportActualWeekValue::getEndTime, weekKeys.get(1))
                .ne(KpiReportActualWeekValue::getColour, 0)
                .in(KpiReportActualWeekValue::getKpiCode, kpiCodeList)
                .apply("CHAR_LENGTH(kpi_code) <={0}", 12));

        //上周达标数
        int zhouDaBiao = (int) list3.stream().filter(value -> 1 == value.getColour()).count();
        double zhouDaBiaoLv = (0 == zhouDaBiao) ? 0 : NumberUtil.div(zhouDaBiao, list3.size());
        //异常数量
        int yiChangSum = (monthList.size()) - (yueDaBiao);
        mapR.putOpt("上月异常数量", yiChangSum);
        mapR.putOpt("月累达标率", yueDaBiaoLv);
        mapR.putOpt("上月达标率", upYueDaBiaoLv);
        mapR.putOpt("周达标率", zhouDaBiaoLv);
        mapR.putOpt("上周", upWeekIndex);
        return mapR;


    }

    /**
     * 行动计划完成率
     * @param wsActionStatisticsDto
     * @return
     */
    public JSONObject workShopTrackPlan(WSActionStatisticsDto wsActionStatisticsDto) {
//        JSONObject map = new JSONObject();
        JSONObject mapLv = new JSONObject("{\"lv\":[{\"name\":\"completed\",\"size\":0,\"lv\":1.0,\"totalSize\":0},{\"name\":\"postpone\",\"size\":0,\"lv\":0.0,\"totalSize\":0},{\"name\":\"underWay\",\"size\":0,\"lv\":0.0,\"totalSize\":0}]}");
//
//        map.putOpt("本月计划完成任务", 0);
//        map.putOpt("总数", 0);
//        map.putOpt("已完成", 0);
//        map.putOpt("延期", 0);
//        map.putOpt("进行中", 0);
//        map.putOpt("lv", mapLv.get("lv"));
        Map<String, Object> map = actionPlanTrackService.queryIndexSummaryData(wsActionStatisticsDto);
        JSONObject jsonObject = new JSONObject((JSONObject)map.get("planCompletion"));
        if(jsonObject == null || (jsonObject!=null && jsonObject.size() == 0)){
            jsonObject.putOpt("本月计划完成任务", 0);
            jsonObject.putOpt("总数", 0);
            jsonObject.putOpt("已完成", 0);
            jsonObject.putOpt("延期", 0);
            jsonObject.putOpt("进行中", 0);
            jsonObject.putOpt("lv", mapLv.get("lv"));
        }
        return jsonObject;
    }

    /**
     * COP记录
     * @param wsActionStatisticsDto
     * @return
     */
    public JSONObject workShopCop(WSActionStatisticsDto wsActionStatisticsDto) {
        JSONObject hashMap = new JSONObject();

        List<CopPlanRank> listR = null;
        try {
            if(0 == wsActionStatisticsDto.getDateType()){
                wsActionStatisticsDto.setDateType(3);
            }
            listR = copScheduleService.copLineStatistics(wsActionStatisticsDto);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if(listR == null || listR.isEmpty()){
            List<StatisticalVo> list = Lists.newArrayList();
            list.add(new StatisticalVo("completed", 0, 0, 0));
            list.add(new StatisticalVo("postpone", 0, 0, 0));
            list.add(new StatisticalVo("underWay", 0, 0, 0));
            hashMap.putOpt("COP记录", list);
        }else{
            hashMap.putOpt("COP记录", listR);
        }
        return hashMap;
    }

    /**
     * 分层检查
     * @param wsActionStatisticsDto
     * @return
     */
    public JSONObject workShopManageCheck(WSActionStatisticsDto wsActionStatisticsDto) {
        JSONObject jsonObject = new JSONObject();
        List<StatisticalVo> statisticalList = manageCheckListService.queryIndexSummaryList(wsActionStatisticsDto);
        if(statisticalList != null && statisticalList.isEmpty()){
            List<StatisticalVo> list = Lists.newArrayList();
            list.add(new StatisticalVo("completed", 0, 0, null));
            list.add(new StatisticalVo("postpone", 0, 0, null));
            list.add(new StatisticalVo("underWay", 0, 0, null));
            jsonObject.putOpt("分层检查", list);
        }else{
            jsonObject.putOpt("分层检查", statisticalList);
        }
        return jsonObject;
    }

    /**
     * 期间核查--车间
     * @param wsActionStatisticsDto
     * @return
     */
    public JSONObject workShopPeriodPlan(WSActionStatisticsDto wsActionStatisticsDto) {
        JSONObject hashMap = new JSONObject();
        List<CopPlanRank> listR = null;
        try {
            listR = periodVerificationScheduleService.periodLineStatistics(wsActionStatisticsDto);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if(listR == null || listR.isEmpty()){
            List<StatisticalVo> list = Lists.newArrayList();
            list.add(new StatisticalVo("completed", 0, 0, 0));
            list.add(new StatisticalVo("postpone", 0, 0, 0));
            list.add(new StatisticalVo("underWay", 0, 0, 0));
            hashMap.putOpt("期间核查", list);
        }else{
            hashMap.putOpt("期间核查", listR);
        }
        return hashMap;
    }

    /**
     * 异常分析
     * @param wsActionStatisticsDto
     * @return
     */
    public JSONObject exceptionRecord(WSActionStatisticsDto wsActionStatisticsDto) {
        JSONObject map4 = new JSONObject();

        List<StatisticalVo> list = new ArrayList<>();
        list.add(new StatisticalVo("completed", 0, 0, null));
        list.add(new StatisticalVo("postpone", 0, 0, null));
        list.add(new StatisticalVo("underWay", 0, 0, null));
        return map4.putOpt("异常分析", list);
    }

    /**
     * 车间统计
     * @param dto
     * @return
     */
    public JSONObject workshopStatistics(WSActionStatisticsDto dto) {

        JSONObject jsonObject = new JSONObject();

        jsonObject.putOpt("pi合格率", workShopComplianceRate(dto));
        jsonObject.putOpt("个人行动计划完成率", dutyTrackPlan(dto));
        jsonObject.putOpt("关键KPI累计达标率", cruxKpiComplianceRate(dto));
        jsonObject.putOpt("行动计划完成率", workShopTrackPlan(dto));

        //待办事项
        JSONObject todo = new JSONObject();
        todo.putAll(workShopCop(dto));
        todo.putAll(workShopManageCheck(dto));
        todo.putAll(workShopPeriodPlan(dto));
        todo.putAll(exceptionRecord(dto));

        jsonObject.putOpt("待办事项", todo);

        return jsonObject;
    }


    //Pi统计
    @Override
    public Object factoryActionStatisticsPi(WSActionStatisticsDto dto) {


        return new ArrayList<>();
    }

    private Map<String, Integer> getPiData(List<PiGreenDataDto> list) {
        Map<String, Integer> map = new HashMap<>();
        int green = 0;
        int red = 0;
        int yellow = 0;
        int all = 0;
        for(PiGreenDataDto dto : list){
            if(StrUtil.isNotBlank(dto.getGreen())){
                green += Integer.parseInt(dto.getGreen());
            }
            if(StrUtil.isNotBlank(dto.getRed())){
                red += Integer.parseInt(dto.getRed());
            }
            if(StrUtil.isNotBlank(dto.getYellow())){
                yellow += Integer.parseInt(dto.getYellow());
            }
            if(StrUtil.isNotBlank(dto.getAll())){
                all += Integer.parseInt(dto.getAll());
            }
        }
        map.put("all", all);
        map.put("green", green);
        map.put("red", red);
        map.put("yellow", yellow);
        return map;
    }
}
