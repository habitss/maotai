package com.keytech.modules.kpi.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import cn.hutool.core.collection.CollStreamUtil;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.math.Calculator;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.ReUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.google.common.collect.Lists;
import com.keytech.base.service.impl.BaseServiceImpl;
import com.keytech.modules.kpi.dto.*;
import com.keytech.modules.kpi.entity.KpiReportActualMonthValue;
import com.keytech.modules.kpi.entity.KpiReportActualTargetLog;
import com.keytech.modules.kpi.entity.KpiReportTargetValue;
import com.keytech.modules.kpi.entity.KpiType;
import com.keytech.modules.kpi.mapper.KpiReportTargetValueMapper;
import com.keytech.modules.kpi.service.*;
import com.keytech.modules.kpi.util.BizConstant;
import com.keytech.modules.kpi.util.BuildCellUtil;
import com.keytech.modules.kpi.util.MonthEnum;
import com.keytech.modules.kpi.util.PiColourEnum;
import com.keytech.modules.kpi.vo.KpiReportCellVo;
import com.keytech.modules.kpi.vo.KpiReportTargetHeaderVo;
import com.keytech.modules.mdm3.entity.MdmWeek;
import com.keytech.modules.mdm3.service.IMdmWeekService;
import org.jeecg.common.aspect.DictAspect;
import org.jeecg.modules.system.entity.SysUser;
import org.jeecg.modules.system.service.ISysUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.beans.PropertyDescriptor;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * @Description: kpi报表目标值
 * @Author: jeecg-boot
 * @Date:   2025-02-24
 * @Version: V1.0
 */
@Service
public class KpiReportTargetValueServiceImpl extends BaseServiceImpl<KpiReportTargetValueMapper, KpiReportTargetValue> implements IKpiReportTargetValueService {

    private static final String[] ALIASES = BizConstant.MONTHS;

    @Autowired
    private IMdmWeekService mdmWeekService;
    @Autowired
    private IKpiTypeService kpiTypeService;
    @Autowired
    private KpiReportTargetValueMapper targetValueMapper;
    @Autowired
    private IKpiInfoService kpiInfoService;
    @Autowired
    private DictAspect dictAspect;
    @Autowired
    private ISysUserService iSysUserService;
    @Autowired
    private IKpiReportActualMonthValueService monthValueService;
    @Autowired
    private IKpiReportActualTargetLogService kpiReportActualTargetLogService;

    /**
     * 获取表头(目标值)
     * @param kpiReportTargetValue
     * @return
     */
    @Override
    public List<KpiReportCellVo> queryHeader(KpiReportTargetValue kpiReportTargetValue) {
        //月集合
        List<Integer>  months = kpiReportTargetValue.getMonths();
        if(months == null || months.isEmpty()){
            kpiReportTargetValue.setMonths(Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12));
        }else{
            Collections.sort(months);
        }
        //获取表头数据
        return getReportHead(kpiReportTargetValue);
    }


    /**
     * 获取表头
     * @return
     */
    public List<KpiReportCellVo> getReportHead(KpiReportTargetValue reportTarget){
        Integer year = reportTarget.getYear();
        List<Integer> monthList = reportTarget.getMonths();
        //获取周
        List<MdmWeek> weekList = new ArrayList<>();
        if(reportTarget.getShowWeek()){
            MdmWeek mdmWeek = new MdmWeek();
            mdmWeek.setYear(year+"");
            //月
            if(monthList != null && !monthList.isEmpty()){mdmWeek.setMonths(monthList);}
            weekList = mdmWeekService.getWeek(mdmWeek);
        }

        List<KpiReportCellVo> cellVoList = new ArrayList<>();
        KpiReportCellVo build =KpiReportCellVo.builder().title("KPI").key("kpiName").sortIndex("").build();
        cellVoList.add(build);
        //固定字段
        BuildCellUtil.buildCell("",(year-1)+"年实际",  "lastYear", cellVoList);
        BuildCellUtil.buildCell("",year+"年目标",  "yearTarget", cellVoList);
        BuildCellUtil.buildCell("","同步年目标",  "copyYearTarget", cellVoList);
        BuildCellUtil.buildCell("","同步月目标",  "copyMonthTarget", cellVoList);
        for (Integer i : monthList) {
            String monthStr = i >= 10 ? i+"" : "0"+i;
            //N月年累目标 2025-03-yearAccumulate
//            BuildCellUtil.buildCell("",i+"月年累目标",  "currentYearTarget_"+i, cellVoList);
            BuildCellUtil.buildCell("",i+"月年累目标",  year+"-"+monthStr+"-"+"yearAccumulate", cellVoList);
        }
        for (Integer i : monthList) {
            //N月月累目标
            BuildCellUtil.buildCell("", i + "月月累目标", ALIASES[i - 1] + "Value", cellVoList);
        }
        //周数据
        for (MdmWeek week : weekList) {
            String startDate = DateUtil.format(week.getStartDate(), "MM.dd");
            String endDate = DateUtil.format(week.getEndDate(), "MM.dd");
            BuildCellUtil.buildCell("第"+week.getWeekNumber()+"周", startDate+"-"+endDate, "w_"+week.getYear()+"_"+week.getWeekNumber(), cellVoList);
        }
        //固定字段
//        BuildCellUtil.buildCell("数据填报职责分工","R",  "provideBy", cellVoList);
//        BuildCellUtil.buildCell("数据填报职责分工","A",  "possessBy", cellVoList);
//        BuildCellUtil.buildCell("数据填报职责分工","C",  "consultedBy", cellVoList);
//        BuildCellUtil.buildCell("数据填报职责分工","I",  "informedBy", cellVoList);
        List<KpiReportCellVo> childNodes = new ArrayList<>();
        childNodes.add(KpiReportCellVo.builder().title("R").key("provideBy").sortIndex(build.getKey()).build());
        childNodes.add(KpiReportCellVo.builder().title("A").key("possessBy").sortIndex(build.getKey()).build());
        childNodes.add(KpiReportCellVo.builder().title("C").key("consultedBy").sortIndex(build.getKey()).build());
        childNodes.add(KpiReportCellVo.builder().title("I").key("informedBy").sortIndex(build.getKey()).build());
        BuildCellUtil.buildCell("车间职责分工",childNodes, cellVoList);

        //是否显示KPI职责分工
        if(reportTarget != null && Boolean.TRUE.equals(reportTarget.getShowKpiDuty())){
            List<KpiReportCellVo> dutyChildNodes = new ArrayList<>();
            dutyChildNodes.add(KpiReportCellVo.builder().title("R").key("dutyProvideBy").sortIndex(build.getKey()).build());
            dutyChildNodes.add(KpiReportCellVo.builder().title("A").key("dutyPossessBy").sortIndex(build.getKey()).build());
            dutyChildNodes.add(KpiReportCellVo.builder().title("C").key("dutyConsultedBy").sortIndex(build.getKey()).build());
            dutyChildNodes.add(KpiReportCellVo.builder().title("I").key("dutyInformedBy").sortIndex(build.getKey()).build());
            BuildCellUtil.buildCell("KPI职责分工",dutyChildNodes, cellVoList);
        }


        BuildCellUtil.buildCell("","截止时间",  "deadline", cellVoList);
        BuildCellUtil.buildCell("","备注",  "remark", cellVoList);

        return cellVoList;
    }

    /**
     * 获取目标值数据
     * @param valueDto
     * @return
     */
    @Override
    public List<Map<String, Object>> queryData(KpiReportValueDto valueDto) {
        List<Map<String, Object>> resultList = new ArrayList<>();

        //查询kpi 数据回显
        String dept = valueDto.getDept();
        String factory = valueDto.getFactory();

        Integer year = valueDto.getYear();

        //**获取月份集合
        //List<String> months = valueDto.getMonths();
        List<Integer>  monthList = valueDto.getMonthList();
        if(monthList == null || monthList.isEmpty()){
            monthList = Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12);
        }
        //**获取周
        boolean showWeek = valueDto.getShowWeek();

        //**KPI分类
        String typeId = valueDto.getTypeId();
        valueDto.setYear(year);

        //获取KPI集合
        valueDto.setShowChildrenKpi(true);
        valueDto.setVisibleWrite(1);
        List<KPiConfigDto> configDtos = kpiInfoService.listKpi(valueDto);
        if(null != configDtos && configDtos.size() > 0){
            QueryWrapper<KpiReportTargetValue> queryWrapper = new QueryWrapper<>();
            if (StrUtil.isNotBlank(typeId)) {
                queryWrapper.and(q -> q.eq("y.id", typeId).or().eq("y.parent_id", typeId));
            }
            //年
            queryWrapper.eq("t.year", year);
            queryWrapper.like("c.year", year);
//            queryWrapper.eq("y.year", year);

            queryWrapper.likeRight(StrUtil.isNotBlank(factory), "c.factory_code", factory);
            //是否在录入中展示
            queryWrapper.eq(Objects.nonNull(valueDto.getVisibleWrite()), "c.visible_write", 1);
            //是否可用
            queryWrapper.eq(true, "c.active_look", 1);
            //录入权限(填写部门)
            if("1".equals(valueDto.getVisibleWrite()+"")){
                //like 工厂查询所有
                queryWrapper.likeRight(StrUtil.isNotBlank(dept), "c.input_permission", dept);
            }
            //查年数据left join 为空的情况
            if (year < 1) {
                year = DateUtil.year(new Date());
            }
            //获取分类
            //Map<Integer, KpiType> typeMap = kpiTypeService.listTypeMap(factory);
            int finalYear = year;

            //层级部门
            if(StrUtil.isNotBlank(valueDto.getDeptQuery())){
                String[] deptArr = valueDto.getDeptQuery().split(",");
                queryWrapper.in("c.dept_code", deptArr);
            }

            //获取KPI目标值
            List<KpiReportTargetValue>  KpiReportTarList = targetValueMapper.listByReportIdJoin(finalYear, queryWrapper);
            Map<String, KpiReportTargetValue> actualDayValueMap = CollStreamUtil.toMap(KpiReportTarList, KpiReportTargetValue::getKpiCode, bean -> bean);

//            Map<String, JSONObject> resultMap = KpiReportTarList.stream()
//                    .map(targetValue -> getJsonObject(typeMap, finalYear, targetValue, month + ""))
//                    .collect(Collectors.toMap(bean -> bean.getStr("kpiCode"), bean -> bean));
            //System.out.println(resultMap);
            //获取用户 dictText
            List<SysUser> userList = iSysUserService.list();
            Map<String, String> userMap = CollStreamUtil.toMap(userList, SysUser::getId, bean -> bean.getRealname());

            String yearStr = year+"";
//            String monthStr = month+"";
            String monthStr = ""; //*****
            resultList = configDtos.stream()
                    .map(bean -> getTargetValueMap(valueDto, yearStr,monthStr,actualDayValueMap, bean, userMap))
                    .sorted(Comparator.comparing(map -> (Comparable) map.get("inputSort")!=null?Double.parseDouble(map.get("inputSort")+""):null, Comparator.nullsLast(Comparator.naturalOrder())))
                    .collect(Collectors.toList());

        }

        return resultList;
    }


    /**
     * 数据整合
     * @param dto
     * @param year
     * @param month
     * @param targetValueMap
     * @param bean
     * @return
     */
    private Map<String, Object> getTargetValueMap(KpiReportValueDto dto, String year,String month, Map<String, KpiReportTargetValue> targetValueMap, KPiConfigDto bean, Map<String, String> userMap) {
        Map<String, Object> valueMap = new HashMap<>();
        String kpiCode = bean.getKpiCode();
        //目标值对象
        KpiReportTargetValue targetValue = null;
        if (targetValueMap.containsKey(kpiCode)) {
            targetValue = targetValueMap.get(kpiCode);
            if(StrUtil.isNotBlank(targetValue.getId())){
                BeanUtil.copyProperties(targetValue, bean);
            }
        }

        //名称加单位拼接
        String unit = bean.getUnit();
        if (StrUtil.isNotBlank(unit)) {
            unit = "(" + unit + ")";
        } else {
            unit = "";
        }

        //R
        if(StrUtil.isNotBlank(bean.getProvideBy())){
            valueMap.put("provideBy", bean.getProvideBy());
            valueMap.put("provideByText",userMap.get(bean.getProvideBy()));
        }

        //A
        if(StrUtil.isNotBlank(bean.getPossessBy())) {
            valueMap.put("possessBy", bean.getPossessBy());
            valueMap.put("possessByText", userMap.get(bean.getPossessBy()));
        }
        //C
        if(StrUtil.isNotBlank(bean.getConsultedBy())) {
            valueMap.put("consultedBy", bean.getConsultedBy());
            valueMap.put("consultedByText", userMap.get(bean.getConsultedBy()));
        }
        //I
        if(StrUtil.isNotBlank(bean.getInformedBy())) {
            valueMap.put("informedBy", bean.getInformedBy());
            valueMap.put("informedByText", userMap.get(bean.getInformedBy()));
        }

        //KPI职责分工R
        if(StrUtil.isNotBlank(bean.getDutyProvideBy())){
            valueMap.put("dutyProvideBy", bean.getDutyProvideBy());
            valueMap.put("dutyProvideByText",userMap.get(bean.getDutyProvideBy()));
        }

        //KPI职责分工A
        if(StrUtil.isNotBlank(bean.getDutyPossessBy())) {
            valueMap.put("dutyPossessBy", bean.getDutyPossessBy());
            valueMap.put("dutyPossessByText", userMap.get(bean.getDutyPossessBy()));
        }
        //KPI职责分工C
        if(StrUtil.isNotBlank(bean.getDutyConsultedBy())) {
            valueMap.put("dutyConsultedBy", bean.getDutyConsultedBy());
            valueMap.put("dutyConsultedByText", userMap.get(bean.getDutyConsultedBy()));
        }
        //KPI职责分工I
        if(StrUtil.isNotBlank(bean.getDutyInformedBy())) {
            valueMap.put("dutyInformedBy", bean.getDutyInformedBy());
            valueMap.put("dutyInformedByText", userMap.get(bean.getDutyInformedBy()));
        }



        //截止时间
        valueMap.put("deadline", StrUtil.isNotBlank(bean.getDeadline())?bean.getDeadline():"");
//        }
        String intPut = bean.getInputPermission();
        if (StrUtil.isNotEmpty(intPut) && StrUtil.isNotBlank(dto.getReportId())) {
            valueMap.put("inputPermission", intPut.contains(dto.getReportId()) ? 1 : 0);
        }
        valueMap.put("kpiName", bean.getKpiName() + unit);
        valueMap.put("reportId", dto.getReportId());
        valueMap.put("kpiCode", kpiCode);
        valueMap.put("id", bean.getId());
        valueMap.put("year", year);
        valueMap.put("month", month);
        if(null != targetValue){
            valueMap.put("remark", targetValue.getRemark());
        }

        // 月目标值配置
        String kpiTypeMonthValue = bean.getKpiTypeMonthValue();
        JSONObject monthAndWeekValue;
        if (!JSONUtil.isTypeJSONObject(kpiTypeMonthValue)) {
            monthAndWeekValue = JSONUtil.parseObj(new KpiReportTargetHeaderVo());
        } else {
            String monthKeyStr = "";
            if(StrUtil.isNotBlank(month) && StrUtil.isNotBlank(year)){
                monthKeyStr = year+"-"+StrUtil.padPre(month, 2, "0")+"-yearAccumulate";
            }
            monthAndWeekValue = JSONUtil.parseObj(kpiTypeMonthValue);
            if(null != monthAndWeekValue.get(monthKeyStr)){
                monthAndWeekValue.set("currentYearTarget",monthAndWeekValue.get(monthKeyStr));
            }
        }

        // 周目标
        String kpiTypeWeekValue = bean.getKpiTypeWeekValue();
        if (JSONUtil.isTypeJSONObject(kpiTypeWeekValue)) {
            JSONObject entries = JSONUtil.parseObj(kpiTypeWeekValue);
            //过滤本年的数据
            for (Map.Entry<String, Object> entry : entries) {
                String key = entry.getKey();
                if (!key.startsWith("w_"+year)) {
                    continue;
                }
                monthAndWeekValue.putOpt(key, entry.getValue());
            }

        }

        valueMap.putAll(monthAndWeekValue);
        valueMap.put("copyYearTarget", "");
        valueMap.put("copyMonthTarget", "");
        valueMap.put("inputSort", bean.getInputSort());
        valueMap.put("kpiCode",bean.getKpiCode());

        return valueMap;
    }


    /**
     * KPI目标保存
     */
    @Transactional(rollbackFor = Exception.class)
//    public List<KpiReportTargetValue> saveData(List<Map<String, Object>> mapList) {
    public List<KpiReportTargetValue> saveData(KpiReportValueDto kpiReportValueDto) {
        //目标值数据集合
        List<Map<String, Object>> mapList = kpiReportValueDto.getTargetDatas();

        Map<String, PropertyDescriptor> propertyDescriptorMap = BeanUtil.getPropertyDescriptorMap(KpiReportTargetValue.class, false);
        Map<String, PropertyDescriptor> monthMap = BeanUtil.getPropertyDescriptorMap(KpiTypeMonthValueDto.class, false);
        //月集合
        List<Integer> monthListTemp = kpiReportValueDto.getMonthList();
        if(monthListTemp == null || monthListTemp.isEmpty()){
            monthListTemp = Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12);
        }
        List<Integer> monthList = monthListTemp;
        //获取当前年所有周
        Map<Integer, String> weekMap = new HashMap<>();
//        if(mapList != null && !mapList.isEmpty()){
        Integer year = kpiReportValueDto.getYear();
        if(year != null){
            weekMap = getWeekOfYear(year+"");
        }
        Map<Integer, String> weekOfYear = weekMap;
        List<KpiReportTargetValue> collect = mapList.stream().map(bean -> {
            Map<String, Object> fixedMap = new HashMap<>(16);
            //月
            Map<String, Object> month = new HashMap<>(32);
            //周
            Map<String, Object> week = new HashMap<>(32);
            //获取年
//            String yearStr = bean.get("year")+"";
//            String monthKeyStr = "";
//            if(StrUtil.isNotBlank(bean.get("month")+"")){
//                //年 年累目标 key
//                monthKeyStr = yearStr+"-"+StrUtil.padPre(bean.get("month")+"", 2, "0")+"-yearAccumulate";
//            }
            //String monthKey = monthKeyStr;

            bean.forEach((k, v) -> {
                //剔除首尾空字符串
                if (Objects.nonNull(v) && StrUtil.isNotBlank(v + "")) {
                    v = StrUtil.trim(v + "");
                }
                String value = v + "";
                if (value.contains("：")) {
                    v = value.replace("：", ":");
                }
                if (propertyDescriptorMap.containsKey(k)) {
                    //固定字段
                    fixedMap.put(k, v);
//                }else if(k.endsWith("-yearAccumulate") && !k.equals(monthKey)){
                }else if(k.endsWith("-yearAccumulate")){
                    //历史年累(排除修改)
                    month.put(k, v);
                }else if (monthMap.containsKey(k)) {
                    //动态字段
                    month.put(k, v);
//                    if("currentYearTarget".equals(k) && StrUtil.isNotBlank(monthKey)){
//                        month.put(monthKey, v);
//                    }
                } else if (k.startsWith("w_")) {
                    week.put(k, v);
                }
            });
            KpiReportTargetValue targetValue = BeanUtil.mapToBean(fixedMap, KpiReportTargetValue.class, false, CopyOptions.create());

            //选中的月转化成代码
            List<String> monthCodeList = monthList.stream()
                .map(m -> {
                    // 确保月份在有效范围内 (1-12)
                    if (m >= 1 && m <= 12) {
                        return ALIASES[m - 1];
                    } else {
                        return "INVALID";
                    }
                }).collect(Collectors.toList());

            //年同步
            if (Objects.nonNull(targetValue.getCopyYearTarget()) && 1 == targetValue.getCopyYearTarget()) {
                //[N]年目标值
                Object yearTarget = month.get("yearTarget");
                //获取选择的月
//                int monthNum = 0;
//                if(StrUtil.isNotBlank(targetValue.getMonth())){
//                    monthNum = Integer.parseInt(targetValue.getMonth());
//                }
                //[N]月月累计目标
                for (Map.Entry<String, Object> entry : month.entrySet()) {
                    String key = entry.getKey();
                    if (key.contains("Value")) {
                        if (StrUtil.isNotBlank(MonthEnum.getMonthByValue(key)) && monthCodeList.contains(key.replace("Value",""))) {
//                            if (Integer.parseInt(MonthEnum.getMonthByValue(key)) >= monthNum) {
                            //同步全年目标
                            month.put(key, String.valueOf(yearTarget));
//                            }
                        }
                    }
                }
                //[N]月年累计目标
                for (int i = 0; i < 16; i++) {
                    if(monthList.contains(i)){
                        String monthStr = i < 10?"0"+i:i+"";
                        month.put(targetValue.getYear()+"-"+monthStr+"-yearAccumulate", String.valueOf(yearTarget));
                    }

                }
                //周-同步当前月到月底多所有周(改成同步当前年所有周)
                //String monthStr = monthNum < 10?"0"+monthNum:monthNum+"";
                weekOfYear.values().forEach(weekStr -> {
                    //判断是否大于等于当前月的周
                    //String data = targetValue.getYear()+"-"+monthStr+"-01";
                    String[] dates = weekStr.split("\\|");
                    //判断选中月份, 默认全年
                    if(monthCodeList.contains(dates[3])){
                        week.put(dates[0], yearTarget);
                    }
                });
            }

            //月同步 传对应月的key例如 augValue
            String copyMonthTarget = targetValue.getCopyMonthTarget();
            if (StrUtil.isNotEmpty(copyMonthTarget) && "1".equals(copyMonthTarget)) {

                //同步选中的月份下的周,没有选中月份同步所有(全年)周[月累数据同步到对应周]
                for (String monthStr : ALIASES) {
                    for (Map.Entry<Integer, String> entry : weekOfYear.entrySet()) {
//                        Integer key = entry.getKey();
                        String value = entry.getValue();
                        String[] weekData = value.split("\\|");
                        //过滤月份
                        if(weekData[3].equals(monthStr) && monthCodeList.contains(monthStr)){
                            week.put(weekData[0], month.get(monthStr+"Value"));
                        }
                    }
                }
            }
            //月
            targetValue.setKpiTypeMonthValue(JSONUtil.toJsonStr(month));
            //周
            targetValue.setKpiTypeWeekValue(JSONUtil.toJsonStr(week));
            return targetValue;
        }).collect(Collectors.toList());
        //保存或新增
        saveOrUpdateBatch(collect, 100);
        return collect;
    }

    /**
     * 根据年份获取该年的周次及其对应的日期范围。
     *
     * @param year 年份，字符串类型
     * @return 返回一个包含周次和对应日期范围的Map，键为周次（Integer类型），值为日期范围（String类型），格式为"开始日期-结束日期"
     *
     */
    public Map<Integer, String> getWeekOfYear(String year){
        MdmWeek mdmWeek = new MdmWeek();
        mdmWeek.setYear(year);
        //获取当前年周集合
        List<MdmWeek> weekList = mdmWeekService.getWeek(mdmWeek);
        Map<Integer, String> weekOfYear = weekList.stream().collect(Collectors.toMap(
                MdmWeek::getWeekNumber,
                weekDate -> {
//                    String startDate = DateUtil.format(weekDate.getStartDate(), "MM.dd");
//                    String endDate = DateUtil.format(weekDate.getEndDate(), "MM.dd");
                    String week = "w_"+year+"_"+weekDate.getWeekNumber();
                    String startDate = DateUtil.format(weekDate.getStartDate(), "YYYY-MM-dd");
                    String endDate = DateUtil.format(weekDate.getEndDate(), "YYYY-MM-dd");
                    //w_年_周数|日期起|日期止|月份代码(janValue)
                    return week+"|"+startDate + "|" + endDate+ "|" +ALIASES[Integer.parseInt(weekDate.getMonth())-1];
                },
                (existing, replacement) -> existing, // 处理键冲突的策略
                HashMap::new // 指定返回的 Map 实现类
        ));
        return weekOfYear;
    }



    /**
     * 判断给定日期是否在指定的日期范围内。
     *
     * @param data 需要判断的日期，格式为"yyyy-MM-dd"
     * @param weekStr 日期范围字符串，格式为"开始日期|结束日期"，日期格式为"yyyy-MM-dd"
     * @return 如果给定的日期在指定的日期范围内，则返回true；否则返回false
     */
    public Boolean isWeek(String data, String weekStr){
        if(StrUtil.isNotBlank(weekStr) && StrUtil.isNotBlank(data)){
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            String[] dates = weekStr.split("\\|");
            LocalDate startDate = LocalDate.parse(dates[1], formatter);
            LocalDate checkDate = LocalDate.parse(data, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            if (startDate.isAfter(checkDate)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 获取目标值
     * @param reportId
     * @param year
     * @param kpiCode
     * @param factoryCode
     * @param deptCode
     * @return
     */
    public KpiReportTargetValue queryTargetValueBy(String reportId, Integer year, String kpiCode, String factoryCode, String deptCode) {
        LambdaQueryWrapper<KpiReportTargetValue> queryWrapper = Wrappers.lambdaQuery(KpiReportTargetValue.class);
        queryWrapper.select(KpiReportTargetValue::getReportId, KpiReportTargetValue::getKpiCode
                , KpiReportTargetValue::getKpiTypeMonthValue, KpiReportTargetValue::getKpiTypeWeekValue);
//        queryWrapper.eq(StrUtil.isNotBlank(reportId), MdmKpiReportTargetValue::getReportId, reportId);
        queryWrapper.eq(KpiReportTargetValue::getYear, year);
        queryWrapper.eq(KpiReportTargetValue::getKpiCode, kpiCode);
        queryWrapper.eq(StrUtil.isNotBlank(factoryCode), KpiReportTargetValue::getFactoryCode, factoryCode);
        queryWrapper.eq(StrUtil.isNotBlank(deptCode), KpiReportTargetValue::getDeptCode, deptCode);
        queryWrapper.orderByDesc(KpiReportTargetValue::getCreateTime);
        queryWrapper.last("limit 1");
        return getOne(queryWrapper);
    }

    /**
     * 根据是否使用ISO标准生成月份映射表。
     * @param iso
     * @return
     */
    public Map<String, String> monthMap(boolean iso) {
        HashMap<String, String> map = new HashMap<>();
        for (int i = 0; i < ALIASES.length; i++) {
            String key = StrUtil.padPre((i + 1 + ""), 2, "0");
            map.put(key, ALIASES[i] + "Value");
        }
        if (iso) {
            map.put("13", "yearTarget");
        }
        return map;
    }

    /**
     * 颜色比较
     * 大于
     * 目标值：1:2:3:8
     * 实际值：0:3:6:12
     * <p>
     * 遇0就跳过
     * 只要存在不合格则为红
     * 合格：
     * 1:目标（第二级/第一级）< （实际）第二级/第一级
     * 2:目标（第三级/第二级）< （实际）第三级/第二级
     * 3:目标（第四级/第三级）< （实际）第四级/第三级
     * compareValue 0 比小 1比大 2 范围 -1 不比较 3比值
     * colour 0 没颜色 1:green ,2:red
     */
    public int getColour(JSONObject targetObject,
                         String actualValue, String targetValue, String compareValue) {
        int colour = 0;
        //如果是-1 就是不计算颜色
        if (Objects.equals(-1, compareValue)) {
            return colour;
        }
        if (StrUtil.isNotBlank(actualValue) || StrUtil.isNotBlank(targetValue)) {
            actualValue = StrUtil.trim(actualValue);
            targetValue = StrUtil.trim(targetValue);
        }
        //红区判断条件 -1不计算 1大于 0小于 2范围 3比例
//        if (Objects.equals(0, actualType) && NumberUtil.isNumber(actualValue)) {
        if (NumberUtil.isNumber(actualValue)) {
            //默认超过目标值为红
            BigDecimal diff = BigDecimal.ZERO;
            if (NumberUtil.isNumber(targetValue)) {
                diff = NumberUtil.sub(actualValue, targetValue);
                if (Objects.isNull(compareValue)) {
                    if (diff.compareTo(BigDecimal.ZERO) > 0) {
                        //默认超过目标值为红
                        colour = 2;
                    } else {
                        colour = 1;
                    }
                } else if (diff.compareTo(BigDecimal.ZERO) > 0 && "1".equals(compareValue)) {
                    colour = 2;
                } else if (diff.compareTo(BigDecimal.ZERO) < 0 && "0".equals(compareValue)) {
                    colour = 2;
                } else {
                    colour = 1;
                }
            } else if ("2".equals(compareValue)) {
                //范围比较 取最小的范围值来计算
                if (targetValue == null || !targetValue.contains("-")) {
                    return colour;
                }
                String[] values = targetValue.split("-");
                if (values.length != 2) {
                    return colour;
                }
                double min = Double.parseDouble(values[0]);
                double max = Double.parseDouble(values[1]);
                if (min > max) {
                    double temp = min;
                    min = max;
                    max = temp;
                }
                double actual = NumberUtil.parseDouble(actualValue);
                //范围内是绿
                if (actual >= min && actual <= max) {
                    colour = 1; //差距为0
                } else {
                    colour = 2;
                    if (actual > max) {
                        diff = NumberUtil.sub(actualValue, max + "");
                    } else {
                        diff = NumberUtil.sub(actualValue, min + "");
                    }
                }
            }
//            if(!Objects.equals(compareValue, 3)){
            if(!"3".equals(compareValue)){
                targetObject.putOpt("diffValue", diff);
            }
        }else if ("3".equals(compareValue)){
            //比值
            if (!(actualValue.contains(":") && targetValue.contains(":"))) {
                return colour;
            }
            //处理比值的问题
            colour = doColour(targetValue, actualValue);
        }
        return colour;
    }

    /**
     * 计算颜色(比率)
     * 1:2:3:4-> 4/3 3/2 2/1
     * 目标比实际值大的数据就不合格
     * @param targetValue
     * @param actualValue
     * @return
     */
    private int doColour(String targetValue, String actualValue) {
        Map<Integer, String> targetMap = getRatioMap(targetValue);
        Map<Integer, String> nowMap = getRatioMap(actualValue);
        //颜色
        int colour = 0; //无色
        //实际值为 1. 0:0:0:0(不达标)  2.分子非0 分母为0 无效(不达标)
        if(!notStandard(actualValue)){
            //红色
            return 2;
        }

        //如果实际的都是0/0 那么就是合格的
        boolean match = nowMap.values().stream().allMatch(v -> Objects.equals("0/0", v));
        if (match) {
            //比率都为零(达标)
            colour = 1; //绿色
            //colour = 2; //红色
        }
        for (Map.Entry<Integer, String> integerStringEntry : targetMap.entrySet()) {

            Integer key = integerStringEntry.getKey();
            String value = integerStringEntry.getValue();
            //实际
            String nowValue = nowMap.get(key);
            if (Objects.equals(nowValue, "0/0") || Objects.equals(value, "0/0") ||
                    StrUtil.isEmpty(value) || StrUtil.isEmpty(nowValue)) {
                continue;
            }
//            if (Objects.equals(value, "0/0") ||
//                    StrUtil.isEmpty(value) || StrUtil.isEmpty(nowValue)) {
//                continue;
//            }
            //实际值判断 (为0不合格)
//            if (Objects.equals(nowValue, "0/0")) {
//                //不合格 红色
//                colour = 2;
//                break;
//            }

            //计算
            double conversionT = Calculator.conversion(value);
            double conversionN = Calculator.conversion(nowValue);
            //存在目标比实际值大的数据就不合格 1:green ,2:red
            if (NumberUtil.compare(conversionT, conversionN) > 0) {
                //不合格 红色
                colour = 2;
                break;
            } else {
                //合格绿色
                colour = 1;
            }

        }
        return colour;
    }

    /**
     * 比例值的替换
     *
     * @param value
     * @return
     */
    private Map<Integer, String> getRatioMap(String value) {
        int count = StrUtil.count(value, ":");
        List<String> split = StrUtil.split(value, ":");
        int num = 0;
        Map<Integer, String> idxMap = new HashMap<>();
        for (int i = split.size() - 1; i >= 0; i--) {
            if (num == count) {
                break;
            }
            int preIdx = i - 1;
            if (preIdx < 0) {
                break;
            }
            //当前值
            String cur = split.get(i);
            //上个值
            String pre = split.get(preIdx);
            //处理0
            if (NumberUtil.isNumber(cur) && NumberUtil.parseDouble(cur) == 0 ||
                    NumberUtil.isNumber(pre) && NumberUtil.parseDouble(pre) == 0) {
                cur = "0";
                pre = "0";
            }
            idxMap.put(num++, cur + "/" + pre);
        }
        return idxMap;
    }
    /**
     * 不达标判断
     *  actualValue 实际值
     *  实际值为 1. 0:0:0:0(不达标)  2.分子非0 分母为0 无效(不达标)
     */
    public boolean notStandard(String actualValue){
       if("0:0:0:0".equals(actualValue)){
           return false;
       }
       String[] parts = actualValue.split(":");
        if (parts.length < 2) {
            return false; // 至少需要两个数字才能形成比率
        }
        for (int i = 0; i < parts.length - 1; i++) {
            try {
                int numerator = Integer.parseInt(parts[i].trim());
                int denominator = Integer.parseInt(parts[i + 1].trim());
                // 检查比率有效性：分子非0且分母为0时无效
                if (numerator != 0 && denominator == 0) {
                    return false;
                }
            } catch (NumberFormatException e) {
                return false; // 数字格式错误
            }
        }
        return true;
    }


    /**
     * 获取目标值
     * @param reportId
     * @param year
     * @param factory
     * @param dept
     * @return
     */
    @Override
    public Map<String, KpiReportTargetValue> queryTargetValue(String reportId, Integer year, String factory, String dept) {
        LambdaQueryWrapper<KpiReportTargetValue> queryWrapper = Wrappers.lambdaQuery(KpiReportTargetValue.class);
//        queryWrapper.eq(MdmKpiReportTargetValue::getReportId, orgCode);
        queryWrapper.eq(KpiReportTargetValue::getYear, year);
        queryWrapper.eq(StrUtil.isNotBlank(factory), KpiReportTargetValue::getFactoryCode, factory);
//        queryWrapper.eq(StrUtil.isNotBlank(dept), MdmKpiReportTargetValue::getDept, dept);
        List<KpiReportTargetValue> list = list(queryWrapper);
        Map<String, String> userMap = CollStreamUtil.toMap(iSysUserService.list(), SysUser::getId, SysUser::getRealname);
        list.forEach(bean -> {
            updateFieldIfNotEmpty(bean::getPossessBy, bean::setPossessBy, userMap);
            updateFieldIfNotEmpty(bean::getProvideBy, bean::setProvideBy, userMap);
            updateFieldIfNotEmpty(bean::getConsultedBy, bean::setConsultedBy, userMap);

            updateFieldIfNotEmpty(bean::getDutyPossessBy, bean::setDutyPossessBy, userMap);
            updateFieldIfNotEmpty(bean::getDutyProvideBy, bean::setDutyProvideBy, userMap);
            updateFieldIfNotEmpty(bean::getDutyConsultedBy, bean::setDutyConsultedBy, userMap);
        });
        return CollStreamUtil.toMap(list, KpiReportTargetValue::getKpiCode, bean -> bean);

    }

    /**
     * 人员转换
     * @param getter
     * @param setter
     * @param userMap
     */
    private void updateFieldIfNotEmpty(Supplier<String> getter, Consumer<String> setter, Map<String, String> userMap) {
        String value = getter.get();
        if (StrUtil.isNotEmpty(value)) {
            String userName = userMap.get(value);
            if (StrUtil.isEmpty(userName) && ReUtil.contains(ReUtil.RE_CHINESE, value)) {
                userName = value;
            }
            setter.accept(userName);
        }
    }

    /**
     * 更新目标 年累  周
     * kpiCode KPICode
     * year 年
     * key 需要更新的key 如月累(decValue) 年累(2024-09-yearAccumulate) 周(2024-08-19|2024-08-25)
     * value 值
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean updateTargetValue(String kpiCode, String year, String key, String value) {
        Boolean isOk = false;
        if(StrUtil.isNotBlank(kpiCode) && StrUtil.isNotBlank(key) && StrUtil.isNotBlank(value)){
            //查询实际值
            KpiReportTargetValue targetValue = lambdaQuery()
                    .eq(KpiReportTargetValue::getYear,year)
                    .eq(KpiReportTargetValue::getKpiCode,kpiCode).one();
            if(null != targetValue && StrUtil.isNotBlank(targetValue.getId())) {
                String month = targetValue.getKpiTypeMonthValue();
                String week = targetValue.getKpiTypeWeekValue();
                if(StrUtil.isNotBlank(month) || StrUtil.isNotBlank(week)){
                    //存储
                    // 判断 key 字符包含"|"
//                    if(key.contains("|")){
//                    判断 key 字符包含"w_"
                    if(key.startsWith("w_")){
                        JSONObject object = JSONUtil.parseObj(week);
                        object.set(key,value);
                        targetValue.setKpiTypeWeekValue(JSONUtil.toJsonStr(object));
                    }else{
                        JSONObject object = JSONUtil.parseObj(month);
                        object.set(key,value);
                        targetValue.setKpiTypeMonthValue(JSONUtil.toJsonStr(object));
                    }
                    updateById(targetValue);
                    isOk = true;
                }
            }
        }
        return isOk;
    }

    /**
     * 目标值调整查询
     * @param dto
     * @return
     */
    @Override
    public Object monthTracking(KpiDiffAnalysisDto dto) {
        //查询月表数据
        String monthStr = dto.getMonth();
        DateTime parse = DateUtil.parse(monthStr, "yyyy-MM");
        KpiReportValueDto kpiReportActualDayValueDto = BeanUtil.copyProperties(dto, KpiReportValueDto.class);
        int year = DateUtil.year(parse);
        kpiReportActualDayValueDto.setYear(year);
        //月数据和年数据
        List<KpiReportActualMonthValue> listMonthDataBy = monthValueService.listMonthDataBy(kpiReportActualDayValueDto, Lists.newArrayList(dto.getKpiCode()));
        //数据转换 key yyyyMM+数据类型, value 对象
        Map<String, KpiReportActualMonthValue> monthValueMap = listMonthDataBy.stream().collect(Collectors.toMap(bean -> String.valueOf(bean.getMonth())+String.valueOf(bean.getIndicator()), bean -> bean,(k1,k2)->k2));
        //获取目标值
        List<KpiReportTargetValue> list = list(new LambdaQueryWrapper<KpiReportTargetValue>().eq(KpiReportTargetValue::getYear, year).eq(KpiReportTargetValue::getKpiCode, dto.getKpiCode()));
        //目标值转换 key
        Map<Integer, KpiReportTargetValue> collect = list.stream().collect(Collectors.toMap(KpiReportTargetValue::getYear, bean -> bean, (k1, k2) -> k2));
        List<JSONObject> result = Lists.newArrayList();
        Map<String, String> monthMap = monthMap(false);
        CellDataVo defaultV = new CellDataVo(null, "", null);
        for (int i = 0; i < 3; i++) {
            JSONObject jsonObject = new JSONObject();
            jsonObject.putOpt("monthTracking", "月度跟踪");
            String indicator = "";
            if (i == 0) {
                indicator = "目标值";
            }
            if (i == 1) {
                indicator = "实际值";
            }
            if (i == 2) {
                indicator = "调整目标值";
            }
            jsonObject.putOpt("indicator", indicator);
            for (int j = 1; j <= 13; j++) {
                //月度
                String monthKey = StrUtil.padPre((j + ""), 2, "0");
                String monthValueKey = monthMap.getOrDefault(monthKey, "");
                //年累返回的KEY
                String yearValueKey = "yearTarget";
                String valueKey = year + monthKey+"1";
                if (j == 13) {
                    //年度
//                    monthValueKey = "yearTarget";
                    //某月年累
                    monthValueKey = monthStr+"-yearAccumulate";
//                    monthKey = "";
                    valueKey = monthStr.replace("-","")+"3";
                }
                KpiReportActualMonthValue monthValue = monthValueMap.get(valueKey);
                CellDataVo cellDataVo = defaultV;
                if (Objects.nonNull(monthValue)) {
                    if (i == 0) {
                        KpiReportTargetValue orDefault = collect.getOrDefault(year, null);
                        if (null == orDefault) {
                            cellDataVo = new CellDataVo(monthValue.getId(), "", null);
                        }else{
                            String extJson = orDefault.getKpiTypeMonthValue();
                            JSONObject extJsonObject = JSONUtil.parseObj(extJson);
                            cellDataVo = new CellDataVo(orDefault.getId(), extJsonObject.getStr(monthValueKey, ""), null);
                        }
                    }
                    if (i == 1) {
                        String value = monthValue.getValue();
                        int colourCode = monthValue.getColour();
                        String colour = PiColourEnum.getValueByCode(colourCode);
                        cellDataVo = new CellDataVo(monthValue.getId(), value, colour);
                    }
                    if (i == 2) {
                        String adjustJson = monthValue.getAdjustJson();
                        JSONObject extJsonObject = JSONUtil.parseObj(adjustJson);
                        cellDataVo = new CellDataVo(monthValue.getId(), extJsonObject.getStr(monthValueKey, ""), null);
                    }
                }
                //年累
                //jsonObject.putOpt(yearValueKey, cellDataVo);
                jsonObject.putOpt(monthValueKey, cellDataVo);
            }
            result.add(jsonObject);
        }
        return result;
    }

    /**
     * 目标值调整查询
     * @param json
     * @return
     */
    @Override
    public void monthTrackingUpdate(JSONObject json) {
        Map<String, String> monthMap = monthMap(false);
        String yearAccumulateKey = json.getStr("yearAccumulateKey");
        monthMap.put("13",yearAccumulateKey);

        List<KpiReportActualTargetLog> list = monthMap
            .values()
            .stream()
            .filter(key -> !StrUtil.isEmpty(json.getJSONObject(key).getStr("id")))
            .map(key -> {
                JSONObject jsonObject = json.getJSONObject(key);
                String id = jsonObject.getStr("id");
                KpiReportActualMonthValue byId = monthValueService.getById(id);
                String adjustJson = byId.getAdjustJson();

                //返回log数据
                KpiReportActualTargetLog log = new KpiReportActualTargetLog();
                log.setTargetId(id);
                log.setUpdateMonth(byId.getMonth().toString());
                log.setValue(jsonObject.getStr("value"));

                //调整json字段
                adjustJson = StrUtil.isEmpty(adjustJson) ? "{}" : adjustJson;

                //目标值调整
                JSONObject j = new JSONObject(adjustJson);
                j.putOpt(key, jsonObject.getStr("value"));
                byId.setAdjustJson(j.toString());
                monthValueService.updateById(byId);

                return log;
            })
            .collect(Collectors.toList());
        //添加log记录
        kpiReportActualTargetLogService.saveBatch(list);
    }

    @Override
    public KpiReportTargetValue queryTargetValueByYear(String reportId, List<Integer> years, String kpiCode, String factoryCode, String deptCode) {
        LambdaQueryWrapper<KpiReportTargetValue> queryWrapper = Wrappers.lambdaQuery(KpiReportTargetValue.class);
        queryWrapper.select(KpiReportTargetValue::getReportId, KpiReportTargetValue::getKpiCode
                , KpiReportTargetValue::getKpiTypeMonthValue, KpiReportTargetValue::getKpiTypeWeekValue);
        queryWrapper.in(KpiReportTargetValue::getYear, years);
        queryWrapper.eq(KpiReportTargetValue::getKpiCode, kpiCode);
        queryWrapper.eq(StrUtil.isNotBlank(factoryCode), KpiReportTargetValue::getFactoryCode, factoryCode);
        queryWrapper.eq(StrUtil.isNotBlank(deptCode), KpiReportTargetValue::getDeptCode, deptCode);
        queryWrapper.orderByDesc(KpiReportTargetValue::getCreateTime);
        queryWrapper.last("limit 1");
        return getOne(queryWrapper);
    }
}
