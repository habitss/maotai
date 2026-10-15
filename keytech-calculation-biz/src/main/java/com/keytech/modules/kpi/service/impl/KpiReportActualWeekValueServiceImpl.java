package com.keytech.modules.kpi.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import cn.hutool.core.collection.CollStreamUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONConfig;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.google.common.collect.Lists;
import com.keytech.base.service.impl.BaseServiceImpl;
import com.keytech.modules.fivewhy.entity.FiveWhyAnalyse;
import com.keytech.modules.fivewhy.service.IFiveWhyAnalyseService;
import com.keytech.modules.kpi.dto.*;
import com.keytech.modules.kpi.entity.*;
import com.keytech.modules.kpi.mapper.KpiReportActualWeekValueMapper;
import com.keytech.modules.kpi.service.*;
import com.keytech.modules.kpi.util.BuildCellUtil;
import com.keytech.modules.kpi.util.DateUtils;
import com.keytech.modules.kpi.util.PiColourEnum;
import com.keytech.modules.kpi.vo.KpiReportCellVo;
import com.keytech.modules.mdm3.entity.MdmWeek;
import com.keytech.modules.mdm3.service.IMdmWeekService;
import org.jeecg.modules.system.entity.SysDepart;
import org.jeecg.modules.system.service.ISysDepartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.Year;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * @Description: kpi报表实际值周表
 * @Author: jeecg-boot
 * @Date:   2025-02-24
 * @Version: V1.0
 */
@Service
public class KpiReportActualWeekValueServiceImpl extends BaseServiceImpl<KpiReportActualWeekValueMapper, KpiReportActualWeekValue> implements IKpiReportActualWeekValueService {

    @Autowired
    private IKpiInfoService infoService;
//    @Autowired
//    private ThreadPoolTaskExecutor taskExecutor;
    @Autowired
    private IKpiReportActualMonthValueService monthlyDayValueService;
    @Autowired
    private IKpiReportTargetValueService targetValueService;
    @Autowired
    private IMdmWeekService mdmWeekService;
    @Autowired
    private IKpiReportActualDayValueService dayValueService;
    @Autowired
    private IKpiTypeService kpiTypeService;
    @Autowired
    private ISysDepartService sysDepartService;
    //5why
    @Autowired
    private IFiveWhyAnalyseService fiveWhyAnalyseService;
    private final ExecutorService executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

    //KPI日报表头
    private static Map<String, String> headerMap;



    /**
     * 同步周月
     * @param bean
     * @param k
     * @param v
     */
    @Override
    public void addWeekAndMonth(Map<String, Object> bean, String k, Object v) {
        if (Objects.isNull(v)) {
            return;
        }
        //剔除首尾空字符串
        String result = StrUtil.trim(v + "");
        //处理比值带是中文符号的问题
        if (result.contains("：")) {
            result = result.replace("：", ":");
        }
        //年和月的数据
        if (k.matches("\\d{4}-\\d{2}") || k.matches("\\d{4}") || k.endsWith("-yearAccumulate")) {
            //月
            doYearAdnMonth(bean, k, result);
        } else if (k.contains("|")) {
//        } else if (k.startsWith("w_")) {
            //周
            doWeekData(bean, k, result);
        }
    }

    /**周*/
    private void doWeekData(Map<String, Object> bean, String k, Object v) {
        KpiReportActualWeekValue weekValue = BeanUtil.toBean(bean, KpiReportActualWeekValue.class);
        //kpi 比值
        List<KpiInfo> kpiInfoList = infoService.list(Wrappers.lambdaQuery(KpiInfo.class).eq(KpiInfo::getKpiCode, weekValue.getKpiCode()).like(KpiInfo::getYear, bean.get("year")));
        if (CollUtil.isEmpty(kpiInfoList) || CollUtil.isEmpty(kpiInfoList)) {
            return;
        }
        KpiInfo kpiInfo = kpiInfoList.get(0);
        weekValue.setIndicator(0);

        List<String> split = StrUtil.split(k, "|");
        String startStr = split.get(0);
        weekValue.setStartTime(DateUtil.parseDate(startStr));
        weekValue.setEndTime(DateUtil.parseDate(split.get(1)));
        weekValue.setValue(String.valueOf(v));
        //月份
        String monthStr = bean.get("monthStr")+"";
        if(StrUtil.isBlank(monthStr)){
            //月份
            monthStr = startStr.substring(0, 7).replace("-", "");
        }

        weekValue.setMonth(NumberUtil.parseInt(monthStr));
        //颜色有标准的 默认0
        KpiReportTargetValue targetValue = targetValueService.queryTargetValueBy(weekValue.getReportId(), weekValue.getYear(), weekValue.getKpiCode(), weekValue.getFactoryCode(), null);
        int colour = 0;
        if (Objects.nonNull(targetValue) && JSONUtil.isTypeJSON(targetValue.getKpiTypeWeekValue())) {
            String kpiTypeWeekValue = targetValue.getKpiTypeWeekValue();
            JSONObject object = JSONUtil.parseObj(kpiTypeWeekValue);
            //获取动态周 w_年_周数
            String newKey = bean.get("newKey")+"";
            String weekTargetValue = object.getStr(newKey, "");
//            String weekTargetValue = object.getStr(k, "");
            String value = weekValue.getValue();
            colour = targetValueService.getColour(object, value, weekTargetValue, kpiInfo.getCompareValue());
            weekValue.setExtJson(object.toString());
        }
        weekValue.setColour(colour);
        weekValue.setId(null);
        LambdaUpdateWrapper<KpiReportActualWeekValue> updateWrapper = Wrappers.lambdaUpdate(KpiReportActualWeekValue.class);
        updateWrapper.eq(KpiReportActualWeekValue::getYear, weekValue.getYear());
        updateWrapper.eq(KpiReportActualWeekValue::getMonth, weekValue.getMonth());
        updateWrapper.eq(KpiReportActualWeekValue::getStartTime, DateUtil.format(weekValue.getStartTime(), "yyyy-MM-dd"));
        updateWrapper.eq(KpiReportActualWeekValue::getEndTime, DateUtil.format(weekValue.getEndTime(), "yyyy-MM-dd"));
//        updateWrapper.eq(MdmKpiReportActualWeekValue::getActualType, actualType);
        updateWrapper.eq(KpiReportActualWeekValue::getKpiCode, weekValue.getKpiCode());
//        updateWrapper.eq(StrUtil.isNotBlank(weekValue.getReportId()), MdmKpiReportActualWeekValue::getReportId, weekValue.getReportId());
//        taskExecutor.execute(() -> saveOrUpdate(weekValue, updateWrapper));
//        saveOrUpdate(weekValue, updateWrapper);
        //更新或者新增
        executorService.submit(() -> {saveOrUpdateWithWrapper(weekValue, updateWrapper);});
    }


    /**
     * 根据条件保存或更新周实际值数据
     *
     * @param weekValue 周实际值数据对象
     * @param updateWrapper 更新条件包装器
     * @return 如果更新成功则返回true，如果插入成功也返回true，否则返回false
     */
    private boolean saveOrUpdateWithWrapper(KpiReportActualWeekValue weekValue, LambdaUpdateWrapper<KpiReportActualWeekValue> updateWrapper) {
        // 先尝试根据条件更新
        boolean updateResult = update(weekValue, updateWrapper);
        if (updateResult) {
            return true;
        }
        // 更新失败，尝试插入
        return this.save(weekValue);
    }

    /**月*/
    private void doYearAdnMonth(Map<String, Object> bean, String k, Object v) {
        KpiReportActualMonthValue monthValue = BeanUtil.toBean(bean, KpiReportActualMonthValue.class);
        //kpi 比值
        List<KpiInfo> kpiInfoList = infoService.list(Wrappers.lambdaQuery(KpiInfo.class).eq(KpiInfo::getKpiCode, monthValue.getKpiCode())
                .like(KpiInfo::getYear, bean.get("year")));
        if (CollUtil.isEmpty(kpiInfoList) || CollUtil.isEmpty(kpiInfoList)) {
            return;
        }
        KpiInfo kpiInfo = kpiInfoList.get(0);
        int indicator = 1;
        String monthStr="";
        int year = monthValue.getYear();
        if (k.matches("\\d{4}")) {
            //年的
            indicator = 2;
            monthStr = year + "";
            String start = monthStr + "-01-01";
            DateTime time = DateUtil.parseDate(start);
            monthValue.setStartTime(time);
            monthValue.setEndTime(DateUtil.parseDate(monthStr + "-12-31"));
        }else if(k.endsWith("-yearAccumulate")){
            //某月年累
            indicator = 3;
            String yearMonth = k.replace("-yearAccumulate", "");
            monthStr = yearMonth.replace("-", "");
            //monthStr = yearMonth.replace("-", "").replace(year+"","");
            DateTime parseDate = DateUtil.parseDate(yearMonth + "-01");
            monthValue.setStartTime(parseDate);
            monthValue.setEndTime(DateUtil.parseDate(DateUtil.endOfMonth(parseDate).toDateStr()));
        }else if(k.matches("\\d{4}-\\d{2}")) {
            //月
            monthStr = k.replace("-", "");
            DateTime parseDate = DateUtil.parseDate(k + "-01");
            monthValue.setStartTime(parseDate);
            monthValue.setEndTime(DateUtil.parseDate(DateUtil.endOfMonth(parseDate).toDateStr()));
        }
        monthValue.setValue(String.valueOf(v));
        monthValue.setIndicator(indicator);
        monthValue.setMonth(NumberUtil.parseInt(monthStr));
        monthValue.setId(null);
        //红区判断条件
        String compareValue = kpiInfo.getCompareValue();
//        taskExecutor.execute(() -> monthlyDayValueService.saveOrUpdateBy(monthValue, compareValue));
        executorService.submit(() -> {monthlyDayValueService.saveOrUpdateBy(monthValue, compareValue);});
    }


    static {
        headerMap = new LinkedHashMap<>();
        headerMap.put("分类", "type");
        headerMap.put("项目", "project");
        headerMap.put("KPI", "kpiName");
        headerMap.put("单位", "unit");
        headerMap.put("年实际", "lastYear");
        headerMap.put("年目标", "yearTarget");
        headerMap.put("曲线", "curve");
        //列表尾部
        headerMap.put("R", "provideBy");
        headerMap.put("A", "possessBy");
        headerMap.put("C", "consultedBy");
        headerMap.put("I", "informedBy");

        headerMap.put("dutyR", "dutyProvideBy");
        headerMap.put("dutyA", "dutyPossessBy");
        headerMap.put("dutyC", "dutyConsultedBy");
        headerMap.put("dutyI", "dutyInformedBy");

        headerMap.put("截止时间", "deadline");
    }


    /**
     * 获取KPI日报表头
     * @param dto
     * @return
     */
    @Override
    public List<KpiReportCellVo> queryKpiReportHeader(KpiReportValueDto dto) {
        //弄成树结构
        int year = dto.getYear();
        int lastYear = year - 1;
        List<String> months = dto.getMonths();
        boolean showWeek = dto.getShowWeek();
        //生成表头
        List<KpiReportCellVo> headerList = new ArrayList<>();
        //排除项目
        ArrayList<String> strings = CollUtil.toList("R", "A", "C", "I","dutyR", "dutyA", "dutyC", "dutyI", "截止时间","");
        headerMap.forEach((k, v) -> {
            //k tile v key
            if (strings.contains(k)) {
                return;
            }
            if (k.equals("年实际")) {
                k = lastYear + k;
            }
            if (k.equals("年目标")) {
                k = year + k;
            }
//            if (k.equals("年累")) {
//                v = year + v;
//            }
            //构建表头二级结构
            BuildCellUtil.buildTableCell("", k, v, headerList);
        });
        //添加某月月年累
        for (String monthStr : months) {
            DateTime currentMonth = DateUtil.parse(monthStr, "yyyy-MM");
            String timeStr = DateUtil.format(currentMonth, DateTimeFormatter.ofPattern("M"));
            //添加年累
            BuildCellUtil.buildTableCell("年累", timeStr + "月", monthStr+"-yearAccumulate", headerList);
        }

        //获取一年内所有周
        //Map<Integer, String> weekOfYear = WeekUtils.getWeekOfYear(year);
        //Map<Integer, String>  weekOfYear = mdmWeekService.getWeekOfYear(year+"");
        MdmWeek mdmWeek = new MdmWeek().setYear(year+"");
        List<MdmWeek> weekList = mdmWeekService.getWeek(mdmWeek);
        //添加月累和周累
        for (String monthStr : months) {
            DateTime currentMonth = DateUtil.parse(monthStr, "yyyy-MM");
            //月份
            String timeStr = DateUtil.format(currentMonth, DateTimeFormatter.ofPattern("M"));
//            //添加年累
//            dayValueService.buildCell("年累", timeStr + "月", monthStr+"-yearAccumulate", headerList);
            //月累
            BuildCellUtil.buildTableCell("月累", timeStr + "月", monthStr, headerList);
            if (showWeek) {
                //计算周数据
//                buildWeek2(currentMonth, headerList, weekOfYear);
                //取得对应月份的周
                for (MdmWeek week : weekList) {
                    if(week.getMonth().equals(timeStr)){
                        String startDate = DateUtil.format(week.getStartDate(), "MM.dd");
                        String endDate = DateUtil.format(week.getEndDate(), "MM.dd");
                        BuildCellUtil.buildTableCell("第"+week.getWeekNumber()+"周", startDate+"-"+endDate, "w_"+week.getYear()+"_"+week.getWeekNumber(), headerList);
                    }
                }
            }
        }
        List<KpiReportCellVo> chList = new ArrayList<>();
        //后面新增RACI 截止时间
        KpiReportCellVo parentNode = KpiReportCellVo.builder().title("车间职责分工").key("").sortIndex("").build();
        headerMap.forEach((k, v) -> {
            if (!strings.contains(k)) {
                return;
            }
            if (!k.equals("截止时间") && !k.startsWith("duty")) {
                KpiReportCellVo chNode = KpiReportCellVo.builder().title(k).key(v).sortIndex(v).build();
                chList.add(chNode);
            }
        });

        parentNode.setChildren(chList);
        headerList.add(parentNode);

        //KPI职责分工
        if(dto != null && Boolean.TRUE.equals(dto.getShowKpiDuty())) {
            List<KpiReportCellVo> dutyChList = new ArrayList<>();
            KpiReportCellVo dutyParentNode = KpiReportCellVo.builder().title("KPI职责分工").key("").sortIndex("").build();
            headerMap.forEach((k, v) -> {
                if (!strings.contains(k)) {
                    return;
                }
                if (!k.equals("截止时间") && k.startsWith("duty")) {
                    KpiReportCellVo chNode = KpiReportCellVo.builder().title(k.replace("duty", "")).key(v).sortIndex(v).build();
                    dutyChList.add(chNode);
                }
            });
            dutyParentNode.setChildren(dutyChList);
            headerList.add(dutyParentNode);
        }

        BuildCellUtil.buildTableCell("", "截止时间", "deadline", headerList);
        // 填写部门
        BuildCellUtil.buildTableCell("", "填写部门", "deptName", headerList);
        return headerList;
    }

    /**
     * 获取周月报数据
     * @param dto
     * @return
     */
    @Override
    public List<JSONObject> listKpiReportData(KpiReportValueDto dto) {
        List<String> months = dto.getMonths();
        String year = dto.getYear() + "";
        List<KpiReportCellVo> listHeader = queryKpiReportHeader(dto);
        //找到对应的key
        Map<String, String> keyIndexMap = new HashMap<>();
        //转换MAP
        BuildCellUtil.getChildrenKeyRecursion(keyIndexMap, listHeader);
        //是否显示周数据 默认加载月表
        boolean showWeek = dto.getShowWeek();
        Map<String, List<KpiReportActualWeekValue>> kpiWeekGroup = null;
        //是否在报表中显示
        dto.setVisible(1);
        //排序标识(KPI日报)
        dto.setTypeSort("report");
        //获取kpi
        List<KPiConfigDto> kpiList = infoService.listKpi(dto);
        if(StrUtil.isNotBlank(dto.getCiteKpiCode())){
            kpiList = getCiteList(kpiList, dto.getCiteKpiCode());
        }
        List<String> kpiCodeIds = kpiList.stream().map(KPiConfigDto::getKpiCode).collect(Collectors.toList());
        //周集合(周数--> 年月日|年月日)
        Map<Integer, String> weekMap = new HashMap<>();
        //周数据
        Map<Integer, MdmWeek> weekData = new HashMap<>();
        if (showWeek) {
            weekData = mdmWeekService.getWeekDataByWeekNum(year);
            //周数据(实际值) kpiCode-->KpiReportActualWeekValue
            kpiWeekGroup = listWeekDataBy(dto, kpiCodeIds).stream().collect(Collectors.groupingBy(KpiReportActualWeekValue::getKpiCode));
            //周集合(日期)
            weekMap = mdmWeekService.getWeekOfYear(year);
        }
        //月/年累(颜色)
        List<KpiReportActualMonthValue> listMonthData = monthlyDayValueService.listMonthDataBy(dto, kpiCodeIds);
        //分组
        Map<String, List<KpiReportActualMonthValue>> kpiMonthGroup = listMonthData.stream().collect(Collectors.groupingBy(KpiReportActualMonthValue::getKpiCode));
        //查询kpi分类信息
        List<JSONObject> reportList = new ArrayList<>();
        //目标值
        Map<String, KpiReportTargetValue> targetValueMap = targetValueService.queryTargetValue(dto.getReportId(), dto.getYear(), dto.getFactory(), dto.getDept());
        Map<String, String> monthTargetMap = targetValueService.monthMap(false);
        CellDataVo defaultV = new CellDataVo(null, "", null);

        Map<String, List<KpiReportActualWeekValue>> finalKpiWeekGroup = kpiWeekGroup;
        //获取kpi分类
        List<KpiType> kpiTypes = kpiTypeService.listAll(dto.getFactory(), null);
        Map<String, KpiType> typeNameMap = CollStreamUtil.toMap(kpiTypes, KpiType::getId, bean -> bean);
        //kpi集合转map code->kpi
        Map<String, KPiConfigDto> kpiCodeMap = CollStreamUtil.toMap(kpiList, KPiConfigDto::getKpiCode, bean -> bean);

        //kpi 5why 分析
        Map<String, FiveWhyAnalyse> fiveWhyAnalyseMap = new HashMap<>(32);
        if (CollUtil.isNotEmpty(kpiCodeIds)) {
            List<FiveWhyAnalyse> list = fiveWhyAnalyseService.lambdaQuery().eq(FiveWhyAnalyse::getKpiYear, dto.getYear())
                    .in(FiveWhyAnalyse::getKpiCode, kpiCodeIds).list();
            fiveWhyAnalyseMap = CollStreamUtil.toMap(list, bean -> StrUtil.join("#", bean.getKpiCode(), bean.getKpiColKey()), bean -> bean);
        }

        //获取部门
        List<SysDepart> departList = sysDepartService.list();
        //code ==> 部门对象
        Map<String, SysDepart> departCodeMap = departList.stream()
                .collect(Collectors.toMap(SysDepart::getOrgCode, depart -> depart));

        for (KPiConfigDto kPiConfigDto : kpiList) {

            String kpiCode = kPiConfigDto.getKpiCode();
            //获取目标值
            JSONObject targetObject = getTargetObject(showWeek, targetValueMap, kpiCode);
            for (int i = 0; i < 2; i++) {
                //0 为目标值 1为实际值
                JSONObject jsonObject = new JSONObject();
                //put 固定列字段
                putFixedFiledData(year, keyIndexMap, kpiMonthGroup, defaultV, typeNameMap, kpiCode, targetObject, i, jsonObject, kpiCodeMap, departCodeMap,months);
                //put 年和月周数据
                putYearAndWeekData(keyIndexMap, showWeek, kpiMonthGroup, monthTargetMap, defaultV, finalKpiWeekGroup
                        , kpiCode, targetObject, jsonObject, i, fiveWhyAnalyseMap,weekMap, weekData, dto.getCiteKpiCode());
                reportList.add(jsonObject);
            }
        }

        //Map<String, List<JSONObject>> dataMap = reportList.stream().collect(Collectors.groupingBy(bean -> (String) bean.getByPath("kpiName.id")));
        Map<String, List<JSONObject>> dataMap = reportList.stream().collect(Collectors.groupingBy(
                bean -> {
                    Object value = bean.getByPath("perentId.value");
                    return value == null? "" : (String) value;
                }));
        //排序
        KpiTypeDto kpiTypeDto = new KpiTypeDto();
        kpiTypeDto.setOrgCode(dto.getReportId());
        kpiTypeDto.setEnableSort(true);
        //报表排序
        kpiTypeDto.setSortType("report");

        List<List<JSONObject>> lists = infoService.kpiSortList(kpiTypeDto, dataMap);

        List<JSONObject> dataList = lists.stream().flatMap(Collection::stream).collect(Collectors.toList());
        //如果是重复引用，不需要树结构，直接返回
        if(StrUtil.isNotBlank(dto.getCiteKpiCode())){
            return dataList;
        }
        //组装成树结构
        dataList = getTreeList(dataList,kpiCodeMap);
        return dataList;
    }

    /**
     * 获取周数据
     * @param dto
     * @param kpiCodeIds
     * @return
     */
    private List<KpiReportActualWeekValue> listWeekDataBy(KpiReportValueDto dto, List<String> kpiCodeIds) {
        //String dept = dto.getDept();
        String factory = dto.getFactory();
        //String reportId = dto.getReportId();
        int year = dto.getYear();
        // kpi
        LambdaQueryWrapper<KpiReportActualWeekValue> lambdaQuery = Wrappers.lambdaQuery(KpiReportActualWeekValue.class);
        //lambdaQuery.eq(StrUtil.isNotBlank(reportId), MdmKpiReportActualWeekValue::getReportId, reportId);
        lambdaQuery.in(CollUtil.isNotEmpty(kpiCodeIds), KpiReportActualWeekValue::getKpiCode, kpiCodeIds);
        lambdaQuery.eq(KpiReportActualWeekValue::getYear, year);
        lambdaQuery.eq(KpiReportActualWeekValue::getIndicator, 0);
        lambdaQuery.likeRight(StrUtil.isNotBlank(factory), KpiReportActualWeekValue::getFactoryCode, factory);
//        lambdaQuery.likeRight(StrUtil.isNotBlank(dept), MdmKpiReportActualWeekValue::getDept, dept);
        //月份
        List<String> months = dto.getMonths();
        //生产周数据
        lambdaQuery.and(wrapper -> {
            for (String monthStr : months) {
                String dateStr = StrUtil.replace(monthStr, "-", "");
                int month = NumberUtil.parseInt(dateStr);
                //月
                wrapper.or(wq -> wq.eq(KpiReportActualWeekValue::getMonth, month));

            }
        });
        lambdaQuery.orderBy(true, false, Lists.newArrayList(KpiReportActualWeekValue::getKpiCode, KpiReportActualWeekValue::getCreateTime));
        return list(lambdaQuery);
    }

    /**
     * 将传入的kpi集合组装成树，去重
     * @param dataList
     * @return
     */
    public List<JSONObject> getTreeList(List<JSONObject> dataList, Map<String, KPiConfigDto> kpiCodeMap ){
        //所有注入到child中的元素都需要删除
        List<JSONObject> removeAllList = new ArrayList<>();
        //匹配id
        for(JSONObject kpiInfoF : dataList) {
            //当前循环中注入到child中的元素
            JSONObject jsonObject = (JSONObject) kpiInfoF.get("kpiName");
            if (null != jsonObject) {
                String kpiCode = jsonObject.get("id") + "";
                if (null != kpiCodeMap.get(kpiCode)) {

                    kpiInfoF.put("kpiId", kpiCodeMap.get(kpiCode).getKpiId());
                    kpiInfoF.put("kpiParentId",kpiCodeMap.get(kpiCode).getKpiParentId());
                    System.out.println(kpiInfoF);
                }
            }
        }

        int num = 0;
        //循环2次，进行组装
        for(JSONObject kpiInfoF : dataList){
            num++;
            //当前循环中注入到child中的元素
            List<JSONObject> removeList = new ArrayList<>();
            int rNum = 0;
            for(JSONObject kpiInfoS : dataList){
                //第一层KPICODE不为空
                if(StrUtil.isNotBlank(kpiInfoF.get("KpiCode")+"")){
                    //第二层KPI父节点id不为空
                    if(Objects.nonNull(kpiInfoS.get("parentId")) && !(kpiInfoS.get("kpiId")+"").equals(kpiInfoF.get("kpiId")+"")){
                        JSONObject jsonObject = (JSONObject) kpiInfoF.get("kpiName");
                        if(String.valueOf(jsonObject.get("id")).equals(kpiInfoS.get("kpiParentId")+"")){
                            rNum++;
                            //页面合并
                            if(rNum%2 == 0){
                                kpiInfoS.set("rowSpan_project",0);
                                kpiInfoS.set("rowSpan_kpiName",0);
                                kpiInfoS.set("rowSpan_kpiName",0);
                                kpiInfoS.set("rowSpan_unit",0);
                                kpiInfoS.set("rowSpan_yearTarget",0);
                                kpiInfoS.set("rowSpan_lastYear",0);
                            }else{
//                                kpiInfoS.set("type","");
                                kpiInfoS.set("rowSpan_project",2);
                                kpiInfoS.set("rowSpan_kpiName",2);
                                kpiInfoS.set("rowSpan_kpiName",2);
                                kpiInfoS.set("rowSpan_unit",2);
                                kpiInfoS.set("rowSpan_yearTarget",2);
                                kpiInfoS.set("rowSpan_lastYear",2);
                            }
                            removeList.add(kpiInfoS);
                        }
                    }
                }
            }
            //注入到子节点中
            if(CollUtil.isNotEmpty(removeList)){
                if(num%2 == 0){
//                if(num%2 != 0){
                    //排除奇数行
                    kpiInfoF.set("children",removeList);
                    kpiInfoF.set("key",kpiInfoF.get("kpiId")+"_2");
                }else{
                    kpiInfoF.set("key",kpiInfoF.get("kpiId")+"_1");
                }
            }else{
                if(num%2 == 0){
                    kpiInfoF.set("key",kpiInfoF.get("kpiId")+"_2");
                }else{
                    kpiInfoF.set("key",kpiInfoF.get("kpiId")+"_1");
                }
            }
            //存储到待删除的集合中
            removeAllList.addAll(removeList);
        }
        //将所有removeAllList，删除掉
        dataList.removeAll(removeAllList);
        return dataList;
    }


    /**
     * 获取目标值
     * @param showWeek
     * @param targetValueMap
     * @param kpiCode
     * @return
     */
    private JSONObject getTargetObject(boolean showWeek, Map<String, KpiReportTargetValue> targetValueMap, String kpiCode) {
        //目标值
        KpiReportTargetValue targetValue = targetValueMap.getOrDefault(kpiCode, new KpiReportTargetValue());
        //null 的key 也得复制
        JSONConfig jsonConfig = new JSONConfig();
        jsonConfig.setIgnoreNullValue(false);
        JSONObject targetObject = new JSONObject(jsonConfig);
        //转化map
        CopyOptions copyOptions = CopyOptions.create(KpiReportTargetValue.class, false, "kpiTypeMonthValue", "kpiTypeWeekValue", "kpiName", "reportId", "unit");
        //null 转空字符串
        copyOptions.setFieldValueEditor((key, value) -> value == null ? "" : value);
        BeanUtil.beanToMap(targetValue, targetObject, copyOptions);
        //年目标和年实际 当前年目标
        targetObject.putOpt("lastYear", "").putOpt("yearTarget", "");
        //月目标
        if (JSONUtil.isTypeJSON(targetValue.getKpiTypeMonthValue())) {
            targetObject.putAll(JSONUtil.parseObj(targetValue.getKpiTypeMonthValue()));
        }
        if (showWeek) {
            //周目标
            String weekValue = targetValue.getKpiTypeWeekValue();
            if (JSONUtil.isTypeJSON(weekValue)) {
                targetObject.putAll(JSONUtil.parseObj(weekValue));
            }
        }
        return targetObject;
    }


    private void putFixedFiledData(String year, Map<String, String> keyIndexMap, Map<String, List<KpiReportActualMonthValue>> kpiMonthGroup,
                                   CellDataVo defaultV, Map<String, KpiType> typeNameMap, String kpiCode, JSONObject targetObject, int i, JSONObject jsonObject,
                                   Map<String, KPiConfigDto> kpiCodeMap, Map<String, SysDepart> departCodeMap,List<String> months) {
        KPiConfigDto kPiConfigDto = kpiCodeMap.get(kpiCode);
        String orgCode = kPiConfigDto.getOrgCode();
        String objName = kPiConfigDto.getOrgName();
        //这个就是kpI项目
        String kpiType = kPiConfigDto.getKpiType();
        String kpiName = kPiConfigDto.getKpiName();
        String unit = kPiConfigDto.getUnit();
        String typeId = kPiConfigDto.getTypeId();
        String perentId = kPiConfigDto.getKpiPerentId();
        //填写部门
        String inputPermission = kPiConfigDto.getInputPermission();
        String inputPermissionName = "";
        if(StrUtil.isNotBlank(inputPermission) && null != departCodeMap){
            SysDepart sysDepart = departCodeMap.get(inputPermission);
            if(null != sysDepart){
                inputPermissionName = sysDepart.getDepartName();
            }
        }

        // 分类 project
//        KpiType kpiTypeData = typeNameMap.get(NumberUtil.parseInt(kPiConfigDto.getKpiParentId()));
        KpiType kpiTypeData = typeNameMap.get(kPiConfigDto.getTypeParentId());
        String project = kpiType;
        if (kpiTypeData != null) {
            project = kpiTypeData.getKpiType();
        }
        jsonObject.putOpt("kpiObj", CellDataVo.builder().id(orgCode).value(objName).build())
                .putOpt("type", CellDataVo.builder().value(project).build())
                .putOpt("project", CellDataVo.builder().value(kpiType).build())
                .putOpt("kpiName", CellDataVo.builder().id(kpiCode).value(kpiName).build())
                .putOpt("unit", CellDataVo.builder().value(unit).build())
                .putOpt("perentId", CellDataVo.builder().value(perentId).build())
                .putOpt("parentId", CellDataVo.builder().value(kPiConfigDto.getKpiParentId()).build())
                .putOpt("typeId", CellDataVo.builder().value(String.valueOf(typeId)).build())
                .putOpt("decimalNum", CellDataVo.builder().value(StrUtil.toStringOrNull(kPiConfigDto.getDecimalNum())).build())
                .putOpt("deptName", CellDataVo.builder().value(StrUtil.toStringOrNull(inputPermissionName)).build());
        //处理目标相关的固定列数据
        headerMap.forEach((k, v) -> {
            if (!targetObject.containsKey(v)) {
                return;
            }
            jsonObject.putOpt(v, CellDataVo.builder().value(targetObject.getStr(v, "")).build());
        });
        //年累
        for (String month : months) {
            if (keyIndexMap.containsKey(month+"-yearAccumulate")) {
                String currentYearTarget = targetObject.getStr(month+"-yearAccumulate");
                if (i == 0 && StrUtil.isNotBlank(currentYearTarget)) {
                    jsonObject.putOpt(month+"-yearAccumulate", CellDataVo.builder().value(currentYearTarget).build());
                }
                if(i == 1){
                    //查询月和年数据
                    DateTime startTime = DateUtil.parseDate(month + "-01");
                    DateTime endTime = DateUtil.parseDate(DateUtil.endOfMonth(startTime).toDateStr());

                    KpiReportActualMonthValue mdmKpiReportActualMonthValue = null;
                    if (kpiMonthGroup.containsKey(kpiCode)) {
                        List<KpiReportActualMonthValue> mdmKpiReportActualMonthValues = kpiMonthGroup.get(kpiCode);
                        //mdmKpiReportActualMonthValue = mdmKpiReportActualMonthValues.stream().filter(bean -> this.findValue(bean, kpiCode, startTime, endTime, 2)).findFirst().orElse(null);
                        mdmKpiReportActualMonthValue = mdmKpiReportActualMonthValues.stream().filter(bean -> this.findValue(bean, kpiCode, startTime, endTime, 3)).findFirst().orElse(null);
                    }
                    if (Objects.isNull(mdmKpiReportActualMonthValue)) {
                        jsonObject.putOpt(month+"-yearAccumulate", defaultV);
                    } else {
                        jsonObject.putOpt(month+"-yearAccumulate", CellDataVo.builder().id(mdmKpiReportActualMonthValue.getId()).value(mdmKpiReportActualMonthValue.getValue()).colour(PiColourEnum.getValueByCode(mdmKpiReportActualMonthValue.getColour())).build());
                    }
                }
                //break;
            }
        }
        //曲线
        String curveName;
        if (i == 0) {
            curveName = "计划";
        } else {
            curveName = "实际";
        }
        jsonObject.putOpt("curve", CellDataVo.builder().value(curveName).build());
    }

    /**
     *
     * @param bean
     * @param kpiCode
     * @param startDate
     * @param endDate
     * @param indicator
     * @return
     */
    private boolean findValue(KpiReportActualMonthValue bean, String kpiCode, DateTime startDate, DateTime endDate, int indicator) {
        return equalsValue(bean.getKpiCode(), bean.getIndicator(), bean.getStartTime(), bean.getEndTime(), kpiCode, startDate, endDate, indicator);
    }

    /**
     *
     * @param bean
     * @param kpiCode
     * @param startDate
     * @param endDate
     * @param indicator
     * @return
     */
    private boolean findValue(KpiReportActualWeekValue bean, String kpiCode, DateTime startDate, DateTime endDate, int indicator) {
        return equalsValue(bean.getKpiCode(), bean.getIndicator(), bean.getStartTime(), bean.getEndTime(), kpiCode, startDate, endDate, indicator);
    }


    /**
     *
     * @param srcKpiCode
     * @param srcIndicator
     * @param startTime
     * @param endTime
     * @param kpiCode
     * @param startDate
     * @param endDate
     * @param indicator
     * @return
     */
    private boolean equalsValue(String srcKpiCode, int srcIndicator, Date startTime, Date endTime, String kpiCode, DateTime startDate, DateTime endDate, int indicator) {
        boolean kpiCodeEq = srcKpiCode.equals(kpiCode);
        boolean indicatorEq = srcIndicator == indicator;
        boolean timeEq = DateUtil.format(startTime, "yyyy-MM-dd").equals(startDate.toDateStr()) && DateUtil.format(endTime, "yyyy-MM-dd").equals(endDate.toDateStr());
        return kpiCodeEq && indicatorEq && timeEq;
    }


    /**
     * 年/月/周数据
     * @param keyIndexMap
     * @param showWeek
     * @param kpiMonthGroup
     * @param monthTargetMap
     * @param defaultV
     * @param finalKpiWeekGroup
     * @param kpiCode
     * @param targetObject
     * @param jsonObject
     * @param index
     * @param fiveWhyAnalyseMap
     */
    private void putYearAndWeekData(Map<String, String> keyIndexMap, boolean showWeek, Map<String, List<KpiReportActualMonthValue>> kpiMonthGroup
            , Map<String, String> monthTargetMap, CellDataVo defaultV, Map<String, List<KpiReportActualWeekValue>> finalKpiWeekGroup
            , String kpiCode, JSONObject targetObject, JSONObject jsonObject, int index, Map<String, FiveWhyAnalyse> fiveWhyAnalyseMap
            , Map<Integer, String> weekMap , Map<Integer, MdmWeek> weekData, String citeKpiCode) {

        keyIndexMap.forEach((key, value) -> {
            //排除了固定列
            if (jsonObject.containsKey(key)) {
                return;
            }

            if (showWeek && key.startsWith("w_")) {
                //根据周获取月份
                String[] keys = key.split("_");
                int weekNumber = Integer.parseInt(keys[2]);
                jsonObject.putOpt("month", weekData.get(weekNumber).getMonth());
            }

            if (index == 0) {
                //月和周目标值 \d{4}-\d{2}-\d{2} \d{2}:\d{2}:\d{2} //标准的日期加时间 2021-01
                String srcKey = key;
                if (key.matches("\\d{4}-\\d{2}")) {
                    //月的目标值 后期需要定义一样的
                    key = monthTargetMap.get(key.substring(key.length() - 2));
                }
                String str = targetObject.getStr(key, "");
                jsonObject.putOpt(srcKey, CellDataVo.builder().value(str).build());
            } else {
                //月和周实际值 2021-07-05|2021-07-11
                if (key.matches("\\d{4}-\\d{2}")) {
                    //月
                    DateTime startTime = DateUtil.parse(key, "yyyy-MM");
                    DateTime endTime = DateUtil.endOfMonth(startTime);
                    KpiReportActualMonthValue mdmKpiReportActualMonthValue = null;
                    if (kpiMonthGroup.containsKey(kpiCode)) {
                        mdmKpiReportActualMonthValue = kpiMonthGroup.get(kpiCode).stream().filter(bean -> this.findValue(bean, kpiCode, startTime, endTime, 1)).findFirst().orElse(null);
                    }
                    if (Objects.isNull(mdmKpiReportActualMonthValue)) {
                        jsonObject.putOpt(key, defaultV);
                    } else {
                        CellDataVo build = CellDataVo.builder().id(mdmKpiReportActualMonthValue.getId()).value(mdmKpiReportActualMonthValue.getValue())
                                .colour(PiColourEnum.getValueByCode(mdmKpiReportActualMonthValue.getColour()))
                                .build();
                        //是否存在why
                        FiveWhyAnalyse fiveWhyAnalyse = fiveWhyAnalyseMap.get(StrUtil.join("#", kpiCode, key));
                        if (Objects.nonNull(fiveWhyAnalyse)
                                && StrUtil.isNotEmpty(fiveWhyAnalyse.getReasonJson())) {
                            build.setFiveWhyMark(true);
                            if(StrUtil.isNotBlank(citeKpiCode)){
                                build.setFiveWhyData(fiveWhyAnalyse);
                            }
                        }
                        jsonObject.putOpt(key, build);
                    }

//                }else if (showWeek && key.contains("|")) {
                }else if (showWeek && key.startsWith("w_")) {
                    //周
                    //在判断一下周的key
                    //周 2021-07-05|2021-07-11
                    String[] keys = key.split("_");
                    int weekNumber = Integer.parseInt(keys[2]);
                    String dateKey = weekMap.get(weekNumber);
                    List<String> split = StrUtil.split(dateKey, "|");
                    DateTime startTime = DateUtil.parse(split.get(0));
                    DateTime endTime = DateUtil.parse(split.get(1));
                    List<KpiReportActualWeekValue> mdmKpiReportActualWeekValues = finalKpiWeekGroup.get(kpiCode);
                    if (CollUtil.isEmpty(mdmKpiReportActualWeekValues)) {
                        jsonObject.putOpt(key, defaultV);
                    } else {
                        KpiReportActualWeekValue mdmKpiReportActualWeekValue = mdmKpiReportActualWeekValues.stream().filter(bean -> this.findValue(bean, kpiCode, startTime, endTime, 0)).findFirst().orElse(null);
                        if (Objects.isNull(mdmKpiReportActualWeekValue)) {
                            jsonObject.putOpt(key, defaultV);
                        } else {
                            CellDataVo build = CellDataVo.builder().id(mdmKpiReportActualWeekValue.getId()).value(mdmKpiReportActualWeekValue.getValue())
                                    .colour(PiColourEnum.getValueByCode(mdmKpiReportActualWeekValue.getColour())).build();
                            //是否存在why
                            FiveWhyAnalyse fiveWhyAnalyse = fiveWhyAnalyseMap.get(StrUtil.join("#", kpiCode, key));
                            if (Objects.nonNull(fiveWhyAnalyse)) {
                                build.setFiveWhyMark(true);
                                if(StrUtil.isNotBlank(citeKpiCode)){
                                    build.setFiveWhyData(fiveWhyAnalyse);
                                }
                            }
                            jsonObject.putOpt(key, build);
                        }
                    }
                }

            }
        });
    }


    /**
     * KPI颜色重算
     * @param dto
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void retryReportData(KpiReportValueDto dto) {
        int year = dto.getYear();
        //是否录入中显示
        Integer VisibleWrite = 1;
        String reportId = dto.getReportId();
        List<String> months = dto.getMonths();
        String typeId = dto.getTypeId();
        //目标值
        KpiReportValueDto targetValue = new KpiReportValueDto();
        targetValue.setYear(year);
        targetValue.setShowWeek(true);
        targetValue.setTypeId(typeId);
        targetValue.setVisibleWrite(VisibleWrite);
        targetValue.setMonth(dto.getMonth());
        //目标值
        List<Map<String, Object>> targetValues = targetValueService.queryData(targetValue);
        //targetType
        if (CollUtil.isEmpty(targetValues)) {
            return;
        }
        //实际值
        Map<String, KpiReportValueDto> dayValueDtoMap = new HashMap<>(16);
        for (String month : months) {
            for (Map<String, Object> value : targetValues) {
                String id = value.get("id")+"";
                //Integer targetType = Integer.parseInt(value.get("targetType")+"");
                //String key = StrUtil.join("#", year, month, reportId, targetType, typeId);
                String key = StrUtil.join("#", year, month, reportId, typeId);
                if (StrUtil.isEmpty(id) || dayValueDtoMap.containsKey(key)) {
                    continue;
                }
                KpiReportValueDto dayValue = new KpiReportValueDto();
                dayValue.setYear(year);
                dayValue.setReportId(reportId);
                dayValue.setMonth(month);
                dayValue.setTypeId(typeId);
                dayValueDtoMap.put(key, dayValue);
            }
        }
        //实际值遍历
        dayValueDtoMap.values().forEach(k -> dayValueService.updateByImport(dayValueService.actualDataList(k), k.getYear() + ""));
    }

    /**
     * 颜色计算
     * @param weekMonthDataList
     * @param year
     */
    @Override
    public void updateWeekAndMonth(List<JSONObject> weekMonthDataList, String year) {
        // 获取kpiCode集合
        Set<String> kpiCodeList = weekMonthDataList.stream().map(bena -> (String) bena.get("kpiCode")).collect(Collectors.toSet());
        //获取月报/周报 数据 获取目标值数据
        List<KpiReportTargetValue> targetValues = targetValueService.lambdaQuery().in(KpiReportTargetValue::getKpiCode, kpiCodeList)
                .eq(KpiReportTargetValue::getYear, year).list();
        Map<String, KpiReportTargetValue> targetValueMap = CollStreamUtil.toMap(targetValues, KpiReportTargetValue::getKpiCode, bean -> bean);
        //月
        List<KpiReportActualMonthValue> listMonthData = monthlyDayValueService.lambdaQuery().eq(KpiReportActualMonthValue::getYear, year).in(KpiReportActualMonthValue::getKpiCode, kpiCodeList)
                .list();
        //Map<String, MdmKpiReportActualMonthValue> monthValueMap = CollStreamUtil.toMap(listMonthData, this::getMonthKey, bean -> bean);
        Map<String, KpiReportActualMonthValue> monthValueMap = CollStreamUtil.toMap(listMonthData, this::getMonthYearKey, bean -> bean);
        //周
        List<KpiReportActualWeekValue> listWeekData = this.lambdaQuery().eq(KpiReportActualWeekValue::getYear, year).in(KpiReportActualWeekValue::getKpiCode, kpiCodeList)
                .list();
        Map<String, KpiReportActualWeekValue> weekValueMap = CollStreamUtil.toMap(listWeekData, this::getWeekKey, bean -> bean);
        //获取年对应的周
        //Map<Integer, String> weekMap = mdmWeekService.getWeekOfYear(year);

        //获取年对应的周
        Map<Integer, MdmWeek> weekMap = mdmWeekService.getWeekDataByWeekNum(year);

        //线程
        ExecutorService executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
        //处理逻辑数据
        for (JSONObject jsonObject : weekMonthDataList) {
            //处理周数据 处理月数据 计算颜色
            String kpiCode = jsonObject.getStr("kpiCode");
            KpiInfo kpiInfo = jsonObject.getBean("kpiInfo", KpiInfo.class);
            //目标值
            KpiReportTargetValue targetValue = targetValueMap.getOrDefault(kpiCode, new KpiReportTargetValue());
            //目标值-周
            String kpiTypeWeekValue = targetValue.getKpiTypeWeekValue();
            //目标值-年月
            String kpiTypeMonthValue = targetValue.getKpiTypeMonthValue();

            jsonObject.forEach((key, value) -> {
                if (Objects.nonNull(value) && StrUtil.isNotBlank(value + "")) {
                    value = StrUtil.trim(value + "");
                }
                //年和月和某月年累的数据
                if (key.matches("\\d{4}-\\d{2}") || key.matches("\\d{4}") || key.endsWith("-yearAccumulate")) {
                    //月和年累
                    KpiReportActualMonthValue mdmKpiReportActualMonthValue = getYearMonthValue(key, String.valueOf(value), kpiTypeMonthValue, kpiInfo, monthValueMap, year);
//                            getMonthValue(key, String.valueOf(value), kpiTypeMonthValue
//                                    , kpiInfo, monthValueMap, year);
//                  getYearMonthValue(key, String.valueOf(value), kpiTypeMonthValue, kpiInfo, monthValueMap, year);
//                    taskExecutor.execute(() -> monthlyDayValueService.saveOrUpdate(mdmKpiReportActualMonthValue));
                    executorService.submit(() -> monthlyDayValueService.saveOrUpdate(mdmKpiReportActualMonthValue));
//                } else if (key.contains("|")) {
                } else if (key.startsWith("w_")) {
                    //周累
                    String keyNew = key; //存储周key(w_2025_n周)
                    String[] keys = key.split("_");
                    int weekNumber = Integer.parseInt(keys[2]);
                    //设置周数
//                    newBean.put("weekNumber",weekNumber);
//                    newBean.put("newKey",key); //存储key w_2025_42
                    //更新key 格式 (2023-11-20|2023-11-26)
                    MdmWeek mdmWeek = weekMap.get(weekNumber);
                    key = mdmWeek.getWeekRangeStr();

                    //获取周所属年月
                    String month = mdmWeek.getMonth();
                    if(StrUtil.isNotBlank(month)){
                        month = Integer.parseInt(month)<10?"0"+month:month;
                    }
                    String monthStr = mdmWeek.getYear()+month;

                    KpiReportActualWeekValue weekValue =getWeekValue(key, String.valueOf(value), kpiTypeWeekValue
                                    , kpiInfo, weekValueMap, year, keyNew, monthStr);
//                    taskExecutor.execute(() -> this.saveOrUpdate(weekValue));
                    executorService.submit(() -> saveOrUpdate(weekValue));
                }
            });
        }
    }


    /**
     * 获取月份和年份的关键值
     *
     * @param mdmKpiReportActualMonthValue KpiReportActualMonthValue对象，包含KPI代码、指标和月份信息
     * @return 返回一个字符串，作为月份和年份的关键值。若指标值为3，则在关键值后追加"yearAccumulate"
     */
    private String getMonthYearKey(KpiReportActualMonthValue mdmKpiReportActualMonthValue) {
        String key ="";
        //统计指标
        String indicator = "3";
        if(indicator.equals(mdmKpiReportActualMonthValue.getIndicator()+"")){
            key = StrUtil.join("#", mdmKpiReportActualMonthValue.getKpiCode(), mdmKpiReportActualMonthValue.getMonth())+"yearAccumulate";
        }else{
            key = StrUtil.join("#", mdmKpiReportActualMonthValue.getKpiCode(), mdmKpiReportActualMonthValue.getMonth());
        }
        return key;
    }

    /**
     * 获取周关键值
     *
     * @param mdmKpiReportActualWeekValue KpiReportActualWeekValue对象，包含KPI代码和开始/结束时间
     * @return 返回一个字符串，格式为"KPI代码#开始时间|结束时间"
     */
    private String getWeekKey(KpiReportActualWeekValue mdmKpiReportActualWeekValue) {
        return StrUtil.join("#", mdmKpiReportActualWeekValue.getKpiCode()
                , StrUtil.join("|", DateUtil.formatDate(mdmKpiReportActualWeekValue.getStartTime()), DateUtil.formatDate(mdmKpiReportActualWeekValue.getEndTime())));
    }


    /**
     * 年月计算
     */
    private KpiReportActualMonthValue getYearMonthValue(String key, String value, String kpiTypeMonthValue
            , KpiInfo kpiInfo, Map<String, KpiReportActualMonthValue> monthValueMap, String year) {
        //month 的key 是就是kpiCode
        String kpiCode = kpiInfo.getKpiCode();
        //红区判断条件标准
        String compareValue = kpiInfo.getCompareValue();
        String join = StrUtil.join("#", kpiCode, key.replaceAll("-", ""));
        KpiReportActualMonthValue monthValue = monthValueMap.getOrDefault(join, new KpiReportActualMonthValue());
        //不管有没有id 都重新计算一下值
        int indicator = 1;
        String monthStr="";
        //某月年累(月)
        String monthStrYearAccumulate = "";
        if (key.matches("\\d{4}")) {
            //年的
            indicator = 2;
            monthStr = year + "";
            String start = monthStr + "-01-01";
            DateTime time = DateUtil.parseDate(start);
            monthValue.setStartTime(time);
            monthValue.setEndTime(DateUtil.parseDate(monthStr + "-12-31"));
        } else if(key.endsWith("-yearAccumulate")){
            //某月年累
            indicator = 3;
            String yearMonth = key.replace("-yearAccumulate", "");
            monthStrYearAccumulate = yearMonth.replace("-", "").replace(year+"","");
            monthStr = yearMonth.replace("-", "");
            DateTime parseDate = DateUtil.parseDate(yearMonth + "-01");
            monthValue.setStartTime(parseDate);
            monthValue.setEndTime(DateUtil.parseDate(DateUtil.endOfMonth(parseDate).toDateStr()));
        }else if(key.matches("\\d{4}-\\d{2}")) {
            monthStr = key.replace("-", "");
            DateTime parseDate = DateUtil.parseDate(key + "-01");
            monthValue.setStartTime(parseDate);
            monthValue.setEndTime(DateUtil.parseDate(DateUtil.endOfMonth(parseDate).toDateStr()));
        }

        monthValue.setKpiCode(kpiCode);
        monthValue.setReportId(kpiInfo.getOrgCode());
        monthValue.setFactoryCode(kpiInfo.getFactoryCode());
        monthValue.setYear(NumberUtil.parseInt(year));
        monthValue.setValue(value);
        monthValue.setIndicator(indicator);
        monthValue.setMonth(NumberUtil.parseInt(monthStr));
        monthValue.setTenantId(kpiInfo.getTenantId());
        //计算颜色
        int colour = 0;
        if (JSONUtil.isTypeJSONObject(kpiTypeMonthValue)) {
            JSONObject targetObject = JSONUtil.parseObj(kpiTypeMonthValue);
            if (monthStr.length() > 4 && "1".equals(monthValue.getIndicator() + "")) {
                Map<String, String> monthMap = targetValueService.monthMap(false);
                String month = monthStr.substring(4);
                String str = targetObject.getStr(monthMap.get(month));
                colour = targetValueService.getColour(targetObject, value, str, compareValue);
            } else if("3".equals(monthValue.getIndicator()+"")){
                // 年 先按照当前某月年累
                String month = monthValue.getYear()+"-"+(monthStrYearAccumulate.length() < 2?"0"+monthStrYearAccumulate:monthStrYearAccumulate);
                String yearTarget = targetObject.getStr(month+"-yearAccumulate");
                if (StrUtil.isEmpty(yearTarget)) {
                    yearTarget = targetObject.getStr("yearTarget");
                }
                colour = targetValueService.getColour(targetObject, value, yearTarget, compareValue);
                //monthValue.setMonth(NumberUtil.parseInt(month.replace("-","")));
            }else{
                // 年的
                String yearTarget = targetObject.getStr("yearTarget");
                colour = targetValueService.getColour(targetObject, value, yearTarget, compareValue);
            }
            monthValue.setExtJson(targetObject.toString());
        }
        monthValue.setColour(colour);

        return monthValue;
    }

    /**
     * 周计算
     * @param key
     * @param valueOf
     * @param kpiTypeWeekValue
     * @param kpiInfo
     * @param weekValueMap
     * @param year
     * @return
     */
    private KpiReportActualWeekValue getWeekValue(String key, String valueOf, String kpiTypeWeekValue
            , KpiInfo kpiInfo, Map<String, KpiReportActualWeekValue> weekValueMap, String year, String keyNew, String monthStr) {

        String kpiCode = kpiInfo.getKpiCode();
        //红区计算标准
        String compareValue = kpiInfo.getCompareValue();
        String join = StrUtil.join("#", kpiCode, key);
        KpiReportActualWeekValue weekValue = weekValueMap.getOrDefault(join, new KpiReportActualWeekValue());

        weekValue.setKpiCode(kpiCode);
        weekValue.setYear(NumberUtil.parseInt(year));
        //weekValue.setReportId(kpiInfo.getOrgCode());
        weekValue.setTenantId(kpiInfo.getTenantId());
        weekValue.setFactoryCode(kpiInfo.getFactoryCode());
        weekValue.setIndicator(0);
        List<String> split = StrUtil.split(key, "|");
        String startStr = split.get(0);
        weekValue.setStartTime(DateUtil.parseDate(startStr));
        weekValue.setEndTime(DateUtil.parseDate(split.get(1)));
        weekValue.setValue(valueOf);
        //月份
        //String monthStr = startStr.substring(0, 7).replace("-", "");
        if(StrUtil.isBlank(monthStr)){
            //月份
            monthStr = startStr.substring(0, 7).replace("-", "");
        }
        weekValue.setMonth(NumberUtil.parseInt(monthStr));
        //颜色有标准的 默认0
        int colour = 0;
        if (JSONUtil.isTypeJSON(kpiTypeWeekValue)) {
            JSONObject object = JSONUtil.parseObj(kpiTypeWeekValue);
            String weekTargetValue = object.getStr(keyNew, "");
            String value = weekValue.getValue();
            //获取颜色
            colour = targetValueService.getColour(object, value, weekTargetValue, compareValue);
            weekValue.setExtJson(object.toString());
        }
        weekValue.setColour(colour);
        return weekValue;
    }


    /**
     * 重点关注KPI
     * @param dto
     * @return
     */
    @Override
    public List<PlaceEmphasisVo> factoryActionStatisticsC(KpiReportValueDto dto) {
        String yMd = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        String yearM = yMd.substring(0, 7);
        String lastMonth = null;
        try {
            List<String> months = dto.getMonths();
            lastMonth = getLastMonth(yearM);
            months.add(lastMonth);
            months.add(yearM);
            dto.setMonths(months);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        List<PlaceEmphasisVo> list = new ArrayList<>();
//        String dept = String.join(",", SpringContextUtils.getHttpServletRequest().getParameterMap().get("dept"));
        String dept = String.join(",", dto.getDept());
        dto.setReportId(dept);
        List<JSONObject> jsonObjects = listKpiReportData(dto);
        // 获取当前日期
        LocalDate today = LocalDate.now();
        //// 计算本周的开始时间（周一）

//        //计算上周的开始时间（上周一）
//        LocalDate startOfLastWeek = today.minusWeeks(1).with(DayOfWeek.MONDAY);
//        LocalDateTime startOfLastWeekMidnight = startOfLastWeek.atStartOfDay();
//        // 计算上周的结束时间（上周日）
//        LocalDate endOfLastWeek = startOfLastWeek.plusDays(6);
//        LocalDateTime endOfLastWeekMidnight = endOfLastWeek.atTime(23, 59, 59);
//        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
//        String weekStart = startOfLastWeekMidnight.format(formatter).substring(0, 10);
//        String weekEnd = endOfLastWeekMidnight.format(formatter).substring(0, 10);
        //获取上周
        String lastWeek = "";
        int year = Year.now().getValue();
        List<MdmWeek> weeks =  mdmWeekService.lambdaQuery().eq(MdmWeek::getYear,year).list();
        for (MdmWeek week : weeks) {
            if(isDateInRange(today,week.getStartDate().toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDate(),
                    week.getEndDate().toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDate())){
                lastWeek = "w_"+year+"_"+(week.getWeekNumber()-1);
                break;
            }
        }

//        System.out.println("上周开始时间: " + weekStart);
//        System.out.println("上周结束时间: " + weekEnd);
        String finalLastMonth = lastMonth;
        String lastWeekStr = lastWeek;
        jsonObjects.forEach(J -> {
            //SVo sysCode = J.get("kpiObj", SVo.class);
            SVo kpi = J.get("kpiName", SVo.class) == null ? new SVo() : J.get("kpiName", SVo.class);
            SVo month = J.get(finalLastMonth, SVo.class) == null ? new SVo() : J.get(finalLastMonth, SVo.class) ;//月实际
            SVo curve = J.get("curve", SVo.class) == null ? new SVo() : J.get("curve", SVo.class);//实际/目标

            //SVo week = J.get(weekStart + "|" + weekEnd, SVo.class) == null ? new SVo() : J.get(weekStart + "|" + weekEnd, SVo.class);
            SVo week = J.get(lastWeekStr, SVo.class) == null ? new SVo() : J.get(lastWeekStr, SVo.class);

            SVo standard = J.get(finalLastMonth, SVo.class) == null ? new SVo() : J.get(finalLastMonth, SVo.class);//月计划
            list.add(new PlaceEmphasisVo(kpi.getId(), kpi.getValue(), standard.getValue(), month.getValue(), week.getValue(), curve.getValue(), month.getColour(), week.getColour()));
        });

        return getPlaceEmphasisVos(list,dept);
    }

    /**
     * 判断指定日期是否在指定日期范围内
     *
     * @param currentDate 当前日期
     * @param startDate     开始日期（包含）
     * @param endDate       结束日期（包含）
     * @return 如果当前日期在开始日期和结束日期之间（包含开始日期和结束日期），则返回true；否则返回false
     */
    public boolean isDateInRange(LocalDate currentDate, LocalDate startDate, LocalDate endDate) {
        return currentDate.isAfter(startDate.minusDays(1)) && currentDate.isBefore(endDate.plusDays(1));
    }


    public static String getLastMonth(String yearMonthStr) throws ParseException {
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM");
        Date date = dateFormat.parse(yearMonthStr);
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        // 获取上个月的日期
        calendar.add(Calendar.MONTH, -1);
        // 格式化上个月的年月字符串
        String lastMonthStr = dateFormat.format(calendar.getTime());
        return lastMonthStr;
    }

    private List<PlaceEmphasisVo> getPlaceEmphasisVos(List<PlaceEmphasisVo> list,String dept) {

        return list.stream().collect(Collectors.groupingBy(PlaceEmphasisVo::getId)).values().stream().map(emplist -> {
            PlaceEmphasisVo vo = new PlaceEmphasisVo();
            vo.setId(emplist.get(0).getId());
            vo.setName(emplist.get(0).getName());
            // 展示 周/月实际值
            emplist.stream().filter(p -> "实际".equals(p.getCurve())).findFirst().ifPresent(p -> {
                vo.setYue(p.getYue());
                vo.setZhou(p.getZhou());
                vo.setYueColor(p.getYueColor());
                vo.setZhouColor(p.getZhouColor());
            });
            // 展示 月计划值
            emplist.stream().filter(p -> "计划".equals(p.getCurve())).findFirst().ifPresent(p -> vo.setBiaoZhun(p.getBiaoZhun()));

            return vo;
        })
        // 过滤出重点关注的指标
        .filter(p -> infoService.list(new LambdaQueryWrapper<KpiInfo>().eq(KpiInfo::getEmphasisType, 1).in(KpiInfo::getKpiCode, p.getId()).eq(StrUtil.isNotBlank(dept),KpiInfo::getDeptCode,dept)).stream().anyMatch(k -> Objects.equals(k.getKpiCode(), p.getId())))
        // 按kpi清单中sort升序
        .sorted(Comparator.comparingDouble(p -> infoService.list(new LambdaQueryWrapper<KpiInfo>().eq(KpiInfo::getKpiCode, p.getId())).stream().min(Comparator.comparingDouble(KpiInfo::getSort)).map(KpiInfo::getSort).orElse(0.0)))
        .limit(8) // 取前八条
        .collect(Collectors.toList());

    }


    @Override
    public List<KpiReportCellVo> trendCurveHeader(KpiReportValueDto dto) {
        //kpi code 年份
        List<Integer> years = dto.getYears();
        boolean showWeek = dto.getShowWeek();

        List<MdmWeek> weekList = getWeek(years);

        //表头
        List<KpiReportHeadDto> headerList = new ArrayList<KpiReportHeadDto>();
        //设置固定表头
        setReportHead(headerList,dto);

        AtomicInteger sortOrder = new AtomicInteger(3);
        //年月集合
        List<String> yearMonths = DateUtils.generateYearMonthListStream(years);

        for (String monthStr : yearMonths) {
            buildMonthTrendCurve(monthStr, monthStr, "2", headerList, sortOrder);
            if (showWeek) {
//                DateTime currentMonth = DateUtil.parse(month, "yyyy-MM");
                YearMonth yearMonth = YearMonth.parse(monthStr);
                // 获取年份和月份
                String year = String.valueOf(yearMonth.getYear());
                String month = String.valueOf(yearMonth.getMonthValue());
                buildWeekSort(year, month, headerList, weekList, "2", sortOrder);
            }
        }

        //生成表头
        JSONObject properties = new JSONObject();
        properties.putOpt("kpiName", dto.getKpiName());
        List<KpiReportCellVo> tm = convertToTree(headerList, properties);
        return tm;
    }


    public void setReportHead(List<KpiReportHeadDto> headerList, KpiReportValueDto dto) {
        //名称
        KpiReportHeadDto kpiReportHeadName = new KpiReportHeadDto("1","名称","name","0",1);
        //KPI名称
        KpiReportHeadDto kpiReportHeadKpiName = new KpiReportHeadDto("2","","kpiName","0",2);
        headerList.add(kpiReportHeadName);
        headerList.add(kpiReportHeadKpiName);
    }


    /**
     * 构建月度趋势曲线
     *
     * @param fTitle 标题
     * @param cKey   关键字
     * @param parentId 父级ID
     * @param headerList 头部列表
     * @param sortOrder 排序序号
     */
    public void buildMonthTrendCurve(String fTitle, String cKey, String parentId, List<KpiReportHeadDto> headerList, AtomicInteger sortOrder) {
        KpiReportHeadDto mdmKpiReportHead = new KpiReportHeadDto();
        mdmKpiReportHead.setId(IdUtil.getSnowflakeNextIdStr());
        mdmKpiReportHead.setKey(cKey);
        mdmKpiReportHead.setTitle(fTitle);
        mdmKpiReportHead.setParentId(parentId);
        mdmKpiReportHead.setSort(sortOrder.addAndGet(1));
        headerList.add(mdmKpiReportHead);
    }


    /**
     * 根据当前月份构建周排序的KPI报告头部信息
     *
     * @param year 年
     * @param headerList     报告头部列表
     * @param weekList     年份中每周的映射关系
     * @param parenId        父ID
     * @param sortOrder      排序顺序
     */
    public void buildWeekSort(String year,String month, List<KpiReportHeadDto> headerList, List<MdmWeek> weekList
            , String parenId, AtomicInteger sortOrder) {
        for (MdmWeek mdmWeek : weekList) {
            if(mdmWeek.getYear().equals(year) && mdmWeek.getMonth().equals(month)){
                String startDate = DateUtil.format(mdmWeek.getStartDate(), "yyyy-MM-dd");
                String endDate = DateUtil.format(mdmWeek.getEndDate(), "yyyy-MM-dd");
                String weekScope = startDate + "|" + endDate;
                List<String> split = StrUtil.split(weekScope, "|");
                String strT = split.get(0);
                String endT = split.get(1);
                //定义key
                String id = IdUtil.getSnowflakeNextIdStr();
                int sort = sortOrder.get();
                for (int j = 0; j < 2; j++) {
                    String key = "", title = "第" + mdmWeek.getWeekNumber() + "周", parentId = parenId;
                    KpiReportHeadDto mdmKpiReportHead = new KpiReportHeadDto();
                    if (j == 1) {
                        String strTimeStr = (strT.substring(strT.indexOf("-") + 1)).replace("-", ".");
                        String endTimeStr = (endT.substring(endT.indexOf("-") + 1)).replace("-", ".");
                        title = strTimeStr + "-" + endTimeStr;
                        //w_2025_27
                        key = "w_"+mdmWeek.getYear()+"_"+mdmWeek.getMonth();
                        parentId = id;
                        mdmKpiReportHead.setId(IdUtil.getSnowflakeNextIdStr());
                        mdmKpiReportHead.setSort(sortOrder.get());
                    } else {
                        mdmKpiReportHead.setId(id);
                        mdmKpiReportHead.setSort(sort);
                    }
                    mdmKpiReportHead.setParentId(parentId);
                    mdmKpiReportHead.setTitle(title);
                    mdmKpiReportHead.setKey(key);
                    headerList.add(mdmKpiReportHead);
                    sortOrder.addAndGet(1);
                }

            }
        }

    }

    /**
     * 将KpiReportHeadDto列表转换为树形结构
     *
     * @param list KpiReportHeadDto列表
     * @param params 参数对象
     * @return 转换后的树形结构列表
     */
    public List<KpiReportCellVo> convertToTree(List<KpiReportHeadDto> list, JSONObject params) {
        // 用于记录id到结点的映射关系
        Map<String, KpiReportCellVo> nodeMap = new HashMap<>(list.size());

        list.forEach(node -> addMap(nodeMap, node, params));

        List<KpiReportCellVo> rootNodeList = Lists.newArrayList();
        nodeMap.forEach((k, v) -> addList(v, rootNodeList));
        return rootNodeList.stream().sorted((o1, o2) -> {
            String dataIndex = o1.getSortIndex();
            String dataIndex1 = o2.getSortIndex();
            return NumberUtil.parseInt(dataIndex) - NumberUtil.parseInt(dataIndex1);
        }).collect(Collectors.toList());

    }


    /**
     * 将节点添加到Map中
     *
     * @param nodeMap 存储节点的Map
     * @param node 节点对象
     * @param params 参数对象
     */
    private void addMap(Map<String, KpiReportCellVo> nodeMap, KpiReportHeadDto node, JSONObject params) {

        if (nodeMap.containsKey(node.getId())) {
            //如果在nodeMap中已存在
            //说明：出现这种情况的原因是其下的子结点优先被遍历到并在nodeMap构造了一个临时结点用于占位
            KpiReportCellVo tempNode = nodeMap.get(node.getId());
            if (tempNode.getChildren() == null) {
                tempNode.initChildren();
            }
            tempNode.setChildren(tempNode.getChildren());
        }
        String title = node.getTitle();
        String key = node.getKey();
        //参数替换
        if (Objects.nonNull(params)) {
            if (params.containsKey(key) && node.getKeyType() != 1) {
                //动态title
                title = params.getStr(key) + title;
            } else if (node.getKeyType() == 1) {
                //动态key 和title
                title = params.getStr(key) + title;
                key = params.getStr(key);
            }
            if (params.containsKey(title)) {
                key = params.getStr(title);
            }
        }

        KpiReportCellVo cellVo = KpiReportCellVo.builder().parentId(node.getParentId())
                .key(key).sortIndex(String.valueOf(node.getSort()))
                .title(title).build();
        //扩展配置
        if (StrUtil.isNotBlank(node.getExt())) {
            cellVo.setExtraConfig(JSONUtil.parseObj(node.getExt()));
        }
        nodeMap.put(node.getId(), cellVo);

        if (nodeMap.containsKey(node.getParentId())) {
            //父结点已存在
            KpiReportCellVo parentNode = nodeMap.get(node.getParentId());
            if (parentNode.getChildren() == null) {
                parentNode.initChildren();
            }
            parentNode.getChildren().add(cellVo);
        }
    }

    /**
     * 将给定的 KpiReportCellVo 对象添加到列表中。
     *
     * @param v            要添加的 KpiReportCellVo 对象
     * @param rootNodeList 根节点列表，用于存储 KpiReportCellVo 对象
     */
    private void addList(KpiReportCellVo v, List<KpiReportCellVo> rootNodeList) {
        if ("0".equals(v.getParentId())) {
            rootNodeList.add(v);
        }
    }

    /**
     * KPI趋势数据
     * @param dto
     * @return
     */
    @Override
    public List<JSONObject> trendCurveData(KpiReportValueDto dto) {
        List<KpiReportCellVo> listHeader = trendCurveHeader(dto);
        Map<String, String> keyIndexMap = new HashMap<>();
        dayValueService.getChildrenRecursion(keyIndexMap, listHeader);
        String kpiCode = dto.getKpiCode();
        String reportId = dto.getReportId();
        List<Integer> years = dto.getYears();
        boolean showWeek = dto.getShowWeek();
        List<JSONObject> result = Lists.newArrayList();
        //查询当年的kpiCode 目标值和实际值
        KpiReportTargetValue targetValue = targetValueService.queryTargetValueByYear(reportId, years, kpiCode, dto.getFactory(), null);
        if (Objects.isNull(targetValue)) {
            return result;
        }
        JSONObject targetObject = JSONUtil.parseObj(targetValue.getKpiTypeMonthValue());
        List<String> kpiCodeList = Lists.newArrayList(kpiCode);
        //月数据
        List<KpiReportActualMonthValue> listMonthData = monthlyDayValueService.listMonthDataByYear(dto, kpiCodeList);
        List<KpiReportActualWeekValue> listWeekData = null;
        //获取周集合
        Map<Integer, String> weekMap = new HashMap<>();
        if (showWeek) {
            listWeekData = listWeekDataByYear(dto, kpiCodeList);
            String weekValue = targetValue.getKpiTypeWeekValue();
            if (StrUtil.isNotBlank(weekValue)) {
                targetObject.putAll(JSONUtil.parseObj(weekValue));
            }
            //周集合(日期)
            weekMap = getWeekOfYears(dto.getYears());
        }
        Map<Integer, String> weekMapNew = weekMap;
        Map<String, String> monthMap = targetValueService.monthMap(false);
        for (int i = 0; i < 2; i++) {
            JSONObject jsonObject = new JSONObject();
            String name = "目标值";
            if (i == 1) {
                name = "实际值";
            }
            jsonObject.putOpt("name", name);
            int finalI = i;
            List<KpiReportActualWeekValue> finalListWeekData = listWeekData;
            keyIndexMap.forEach((key, v) -> {
                if (jsonObject.containsKey(key)) {
                    return;
                }
                if (finalI == 0) {
                    //月和周目标值 \d{4}-\d{2}-\d{2} \d{2}:\d{2}:\d{2} //标准的日期加时间 2021-01
                    String srcKey = key;
                    if (key.matches("\\d{4}-\\d{2}")) {
                        //月的目标值 后期需要定义一样的
                        key = monthMap.get(key.substring(key.length() - 2));
                    }
                    String str = targetObject.getStr(key, "");
                    jsonObject.putOpt(srcKey, str);
                } else {
                    //月和周实际值 2021-07-05|2021-07-11
                    if (key.matches("\\d{4}-\\d{2}")) {
                        DateTime startTime = DateUtil.parse(key, "yyyy-MM");
                        DateTime endTime = DateUtil.endOfMonth(startTime);
                        KpiReportActualMonthValue mdmKpiReportActualMonthValue = listMonthData.stream()
                                .filter(bean -> this.findValue(bean, kpiCode, startTime, endTime, 1)).findFirst().orElse(null);
                        if (Objects.isNull(mdmKpiReportActualMonthValue)) {
                            jsonObject.putOpt(key, "");
                        } else {
                            jsonObject.putOpt(key, mdmKpiReportActualMonthValue.getValue());

                        }
                    } else if (showWeek) {
                        //周 2021-07-05|2021-07-11
                        String keyNew = key; //存储周key(w_2025_n周)
                        String[] keys = key.split("_");
                        int weekNumber = Integer.parseInt(keys[2]);
                        //weekMap.get(weekNumber);
                        key = weekMapNew.get(weekNumber);

                        List<String> split = StrUtil.split(key, "|");
                        DateTime startTime = DateUtil.parse(split.get(0));
                        DateTime endTime = DateUtil.parse(split.get(1));
                        KpiReportActualWeekValue mdmKpiReportActualMonthValue = finalListWeekData.stream()
                                .filter(bean -> this.findValue(bean, kpiCode, startTime, endTime, 0)).findFirst().orElse(null);
                        if (Objects.isNull(mdmKpiReportActualMonthValue)) {
                            jsonObject.putOpt(keyNew, "");
                        } else {
                            jsonObject.putOpt(keyNew, mdmKpiReportActualMonthValue.getValue());

                        }
                    }
                }

            });
            result.add(jsonObject);
        }
        return result;
    }

    public List<MdmWeek> getWeek(List<Integer> years){
        LambdaQueryWrapper<MdmWeek> queryWrapper = new LambdaQueryWrapper();
        queryWrapper.in(MdmWeek::getYear, years);
        queryWrapper.orderByAsc(MdmWeek::getYear).orderByAsc(MdmWeek::getWeekNumber);
        return mdmWeekService.list(queryWrapper);
    }

    /**
     * 获取周数据(多年份)
     * @param dto
     * @param kpiCodeIds
     * @return
     */
    private List<KpiReportActualWeekValue> listWeekDataByYear(KpiReportValueDto dto, List<String> kpiCodeIds) {
        String factory = dto.getFactory();
        List<Integer> years = dto.getYears();
        // kpi
        LambdaQueryWrapper<KpiReportActualWeekValue> lambdaQuery = Wrappers.lambdaQuery(KpiReportActualWeekValue.class);
        lambdaQuery.in(CollUtil.isNotEmpty(kpiCodeIds), KpiReportActualWeekValue::getKpiCode, kpiCodeIds);
        lambdaQuery.in(KpiReportActualWeekValue::getYear, years);
        lambdaQuery.eq(KpiReportActualWeekValue::getIndicator, 0);
        lambdaQuery.likeRight(StrUtil.isNotBlank(factory), KpiReportActualWeekValue::getFactoryCode, factory);
        //月份
        List<String> yearMonths = DateUtils.generateYearMonthListStream(years);
        //生产周数据
        lambdaQuery.and(wrapper -> {
            for (String monthStr : yearMonths) {
                String dateStr = StrUtil.replace(monthStr, "-", "");
                int month = NumberUtil.parseInt(dateStr);
                //月
                wrapper.or(wq -> wq.eq(KpiReportActualWeekValue::getMonth, month));
            }
        });
        lambdaQuery.orderBy(true, false, Lists.newArrayList(KpiReportActualWeekValue::getKpiCode, KpiReportActualWeekValue::getCreateTime));
        return list(lambdaQuery);
    }


    /**
     * 获取多个年份对应周
     * @param yearList
     * @return
     */
    public Map<Integer, String> getWeekOfYears(List yearList) {
        LambdaQueryWrapper<MdmWeek> queryWrapper = new LambdaQueryWrapper();
        queryWrapper.in(MdmWeek::getYear, yearList);
        queryWrapper.orderByAsc(MdmWeek::getWeekNumber);
        List<MdmWeek> weekList = mdmWeekService.list(queryWrapper);
        Map<Integer, String> weekOfYear = (Map)weekList.stream().collect(Collectors.toMap(MdmWeek::getWeekNumber, (weekDate) -> {
            String startDate = DateUtil.format(weekDate.getStartDate(), "yyyy-MM-dd");
            String endDate = DateUtil.format(weekDate.getEndDate(), "yyyy-MM-dd");
            return startDate + "|" + endDate;
        }, (existing, replacement) -> {
            return existing;
        }, HashMap::new));
        return weekOfYear;
    }

    //获取指定的重复引用数据
    private List<KPiConfigDto> getCiteList(List<KPiConfigDto> kpiList, String kpiCode) {
        List<KPiConfigDto> resultList = new ArrayList<>();
        kpiList.forEach(kpi -> {
            //如果kpiCode相等，添加进返回列表
            if(kpi.getKpiCode().equals(kpiCode)){
                resultList.add(kpi);
            }
            //如果有子级递归查询
            if(StrUtil.isNotBlank(kpi.getKpiParentId()) && kpi.getKpiParentId().equals(kpiCode)){
                resultList.addAll(getCiteList(kpiList, kpi.getKpiCode()));
            }
        });
        return resultList;
    }
}
