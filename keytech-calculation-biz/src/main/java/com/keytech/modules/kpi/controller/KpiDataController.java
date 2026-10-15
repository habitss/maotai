package com.keytech.modules.kpi.controller;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.keytech.modules.kpi.entity.KpiReportActualDayValue;
import com.keytech.modules.kpi.entity.KpiReportActualWeekValue;
import com.keytech.modules.kpi.entity.KpiReportTargetValue;
import com.keytech.modules.kpi.service.IKpiReportActualDayValueService;
import com.keytech.modules.kpi.service.IKpiReportActualWeekValueService;
import com.keytech.modules.kpi.service.IKpiReportTargetValueService;
import com.keytech.modules.mdm3.entity.MdmWeek;
import com.keytech.modules.mdm3.service.IMdmWeekService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.time.DateUtils;
import org.jeecg.common.api.vo.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * KPI历史数据 [临时]
 * 周数转换
 * 2024-01-01|2024-01-07 --> w_2024_1 (w_年_第几周)
 */
@Tag(name="KPI历史数据")
@RestController
@RequestMapping("/kpi/kpiDataClean")
@Slf4j
public class KpiDataController {

    @Autowired
    private IKpiReportActualDayValueService iKpiReportActualDayValueService;

    @Autowired
    private IKpiReportTargetValueService iKpiReportTargetValueService;

    @Autowired
    private IKpiReportActualWeekValueService iKpiReportActualWeekValueService;
    @Autowired
    private IMdmWeekService iMdmWeekService;

    List<MdmWeek> weekList2024 = new ArrayList<>();
    List<MdmWeek> weekList2025 = new ArrayList<>();
    // 使用 SimpleDateFormat 进行转换
//    SimpleDateFormat yyyyMmDd = new SimpleDateFormat("yyyy-MM-dd");
    /**
     * KPI数据清洗
     *
     * @return
     */
    @Operation(summary="差距分析-分页列表查询")
    @GetMapping(value = "/dataClean")
    public Result<?> dataClean(String type,Integer year) {
        //获取周
        LambdaQueryWrapper<MdmWeek> queryWrapper = new LambdaQueryWrapper();
        queryWrapper.eq(MdmWeek::getYear, 2024);
        weekList2024 = iMdmWeekService.list(queryWrapper);

        LambdaQueryWrapper<MdmWeek> queryWrapper2 = new LambdaQueryWrapper();
        queryWrapper2.eq(MdmWeek::getYear, 2025);
        weekList2025 = iMdmWeekService.list(queryWrapper2);

        if("1".equals(type)){
            //实际值(mes3_kpi_report_actual_day_value)
            //****************
            actual();
        }else if("2".equals(type)){
            //目标值(mes3_kpi_report_target_value)
            //******************
            target();
        }else if("3".equals(type)){
            //月(mes3_kpi_report_actual_month_value)
        }else if("4".equals(type)){
            //周(mes3_kpi_report_actual_week_value)
            //******************
            week(year);
        }else if("5".equals(type)){
            //*******************
            weekNumber(year);
        }else if("6".equals(type)){
            //测试
            Map<Integer, String> map = getWeekOfYear(year);
            System.out.println(map);
            return Result.OK("成功", map);
        }
        return Result.OK("成功");
    }
    //实际值
    public void actual(){
        //获取实际值集合
        List<KpiReportActualDayValue> list = iKpiReportActualDayValueService.list();
        for (KpiReportActualDayValue bean : list) {
            String dataValueStr = bean.getValue();
            JSONObject parse = null;
            if (JSONUtil.isTypeJSON(dataValueStr)) {
                parse = JSONUtil.parseObj(dataValueStr);
            }
            if (parse == null){
                continue;
            }
//            System.out.println(parse.toString());
            Map<String, Object> valueMap = new HashMap<>();
            parse.forEach((keyStr, valueStr) -> {
                if(keyStr.indexOf("|") > 0){
                    System.out.println(keyStr);
                    //获取第几周
                    String newKeyStr = getWeek(keyStr,bean.getYear());
                    if(StrUtil.isNotBlank(newKeyStr)) {
                        valueMap.put(newKeyStr, valueStr);
                    }
                    //valueMap.put(keyStr, valueStr);
                }else{
                    valueMap.put(keyStr, valueStr);
                }
//                if (!keyStr.equals(key) && !"kpiName".equals(keyStr)) {
//                    valueMap.put(keyStr, valueStr);
//                }
            });
            bean.setValue(JSONUtil.toJsonStr(valueMap));
        }

        for (KpiReportActualDayValue kpiReportActualDayValue : list) {
            System.out.println(kpiReportActualDayValue.getValue());
        }
//        iKpiReportActualDayValueService.saveOrUpdateBatch(list,500);
        iKpiReportActualDayValueService.updateBatchById(list);
    }

    //目标值
    public void target(){
        List<KpiReportTargetValue> list = iKpiReportTargetValueService.list();
        for (KpiReportTargetValue bean : list) {
            String dataValueStr = bean.getKpiTypeWeekValue();
            JSONObject parse = null;
            if (JSONUtil.isTypeJSON(dataValueStr)) {
                parse = JSONUtil.parseObj(dataValueStr);
            }
            if (parse == null){
                continue;
            }
//            System.out.println(parse.toString());
            Map<String, Object> valueMap = new HashMap<>();
            parse.forEach((keyStr, valueStr) -> {
                if(keyStr.indexOf("|") > 0){
                    System.out.println(keyStr);
                    //获取第几周
                    String newKeyStr = getWeek(keyStr,bean.getYear());
                    if(StrUtil.isNotBlank(newKeyStr)){
                        valueMap.put(newKeyStr, valueStr);
                    }
                    //valueMap.put(keyStr, valueStr);
                }else{
                    valueMap.put(keyStr, valueStr);
                }
            });
            bean.setKpiTypeWeekValue(JSONUtil.toJsonStr(valueMap));
        }
        for (KpiReportTargetValue bean : list) {
            System.out.println(bean.getKpiTypeWeekValue());
        }

//        iKpiReportTargetValueService.saveOrUpdateBatch(list,500);
        iKpiReportTargetValueService.updateBatchById(list,500);
    }

    // 月
    public void month(){

    }

    // 周
    public void week(int year){

        LambdaQueryWrapper<KpiReportActualWeekValue> queryWrapper = new LambdaQueryWrapper();
        queryWrapper.eq(KpiReportActualWeekValue::getYear, year);
        List<KpiReportActualWeekValue> list = iKpiReportActualWeekValueService.list(queryWrapper);
        for (KpiReportActualWeekValue bean : list) {
            String dataValueStr = bean.getExtJson();
            JSONObject parse = null;
            if (JSONUtil.isTypeJSON(dataValueStr)) {
                parse = JSONUtil.parseObj(dataValueStr);
            }
            if (parse == null){
                continue;
            }
//            System.out.println(parse.toString());
            Map<String, Object> valueMap = new HashMap<>();
            parse.forEach((keyStr, valueStr) -> {
                if(keyStr.indexOf("|") > 0){
//                    System.out.println(keyStr);
                    //获取第几周
                    String newKeyStr = getWeek(keyStr,bean.getYear());
                    if(StrUtil.isNotBlank(newKeyStr)){
                        valueMap.put(newKeyStr, valueStr);
                    }
                    //valueMap.put(keyStr, valueStr);
                }else{
                    if(StrUtil.isNotBlank(keyStr)) {
                        valueMap.put(keyStr, valueStr);
                    }
                }
            });
            bean.setExtJson(JSONUtil.toJsonStr(valueMap));
        }
        for (KpiReportActualWeekValue bean : list) {
            System.out.println(bean.getExtJson());
        }
//        iKpiReportActualWeekValueService.saveOrUpdateBatch(list,50);
        iKpiReportActualWeekValueService.updateBatchById(list);
    }

    //更新周表周数(mes3_kpi_report_actual_week_value)
    public void weekNumber(int year){
        LambdaQueryWrapper<KpiReportActualWeekValue> queryWrapper = new LambdaQueryWrapper();
        queryWrapper.eq(KpiReportActualWeekValue::getYear, year);

        List<KpiReportActualWeekValue> list = iKpiReportActualWeekValueService.list(queryWrapper);
        for (KpiReportActualWeekValue bean : list) {
            String keyStr = DateUtil.format(bean.getStartTime(),"yyyy-MM-dd")+"|"+DateUtil.format(bean.getEndTime(),"yyyy-MM-dd");
            String newKeyStr = getWeek(keyStr,bean.getYear());
            if(StrUtil.isNotBlank(newKeyStr)){
                String[] arr = newKeyStr.split("_");
                bean.setWeekNumber(Integer.parseInt(arr[2]));
            }
        }
        for (KpiReportActualWeekValue bean : list) {
            System.out.println(bean.getWeekNumber());
        }
//        iKpiReportActualWeekValueService.saveOrUpdateBatch(list,50);
        iKpiReportActualWeekValueService.updateBatchById(list);
    }


    //获取历史数据周数
    public String getWeek(String key, int year){

        //int beginIndex = DateUtil.weekOfYear(currentMonth);
        //第一周以外
//        if (beginIndex != 1 && !currentMonth.dayOfWeekEnum().equals(Week.MONDAY)) {
//            beginIndex += 1;
//        }

        Map<Integer, String> weekOfDate = getWeekOfYear(year);
//        DateTime endOfMonth = DateUtil.endOfMonth(currentMonth);
//        int endIndex = DateUtil.weekOfYear(endOfMonth);
//
//        int weekOfMonth = DateUtil.weekOfMonth(endOfMonth);
        //最后一个月的最后一周算出来都是1
//        if (endIndex == 1 && weekOfMonth != 1) {
//            endIndex = 53;
//        }
        String weekNumStr = "";
        int endIndex = 53;
        //周数据
        for (int i = 0; i <= endIndex; i++) {
            String weekScope = weekOfDate.get(i);
            if(year == 2025){
                if("2025-01-01|2025-01-05".equals(key)){
                    key = "2024-12-30|2025-01-05";
                }
//                List<String> split = StrUtil.split(weekScope, "|");
//                String strT = split.get(0);
//                String endT = split.get(1);
//                //yyyyMmDd
//                try {
//                    Date stDate = yyyyMmDd.parse(strT);
//                    Date enDate = yyyyMmDd.parse(endT);
//
//                    key = DateUtil.format(stDate,"yyyy-MM-dd")+"|"+DateUtil.format(enDate,"yyyy-MM-dd");
//                } catch (ParseException e) {
//                    System.out.println("解析日期时出现错误: " + e.getMessage());
//                }

            }
//            String strTimeStr = (strT.substring(strT.indexOf("-") + 1)).replace("-", ".");
//            String endTimeStr = (endT.substring(endT.indexOf("-") + 1)).replace("-", ".");
//            //定义key
//            String title = strTimeStr + "-" + endTimeStr;


            if(key.equals(weekScope)){
                //System.out.println(key+"===>"+weekScope+"---"+i+"周"+"  w_"+year+"_"+i);
                weekNumStr = "w_"+year+"_"+i;
            }
        }
        return weekNumStr;
    }


    public Map<Integer, String> getWeekOfYear(int year) {
        String begin = year + "-01-01";
        String end = year + "-12-31";
        if(year == 2025){
            return getWeekOfDate(year);
        }else {
            return getWeekOfDate(DateUtil.parseDate(begin), DateUtil.parseDate(end));
        }
    }


    public static Map<Integer, String> getWeekOfDate(Date start, Date end) {
        Map<Integer, String> result = new HashMap<>(55);
        StringBuilder sb;
        int weekNum = getWeekIndex(end);
        //获取周数
//        int weekNum = weeksInPeriod(start,end);

        Calendar cal = Calendar.getInstance();
        // 设置一个星期的第一天，按中国的习惯一个星期的第一天是星期一
        cal.setFirstDayOfWeek(Calendar.MONDAY);
        cal.setTime(start);
        // 获得当前日期是一个星期的第几天
        int dayWeek = cal.get(Calendar.DAY_OF_WEEK);
        if (1 == dayWeek) {
            cal.add(Calendar.DAY_OF_MONTH, -1);
        }
        for (int i = 1; i <= weekNum; i++) {
            sb = new StringBuilder();
            if (i == 1) {//第一周以 start 开始
                sb.append(DateUtil.format(start, "yyyy-MM-dd"));
            } else {
                sb.append(DateUtil.format(cal.getTime(), "yyyy-MM-dd"));
            }
            sb.append("|");
            if (i == weekNum) {//最后一周以 end 结束
                sb.append(DateUtil.format(end, "yyyy-MM-dd"));
            } else {
                //设置这周的周日日期，1代表周日，取值范围1~7，设置1~7之外会从周日开始往前后推算，负前正后，DAY_OF_WEEK的日期变更范围只会是在当前日期的周
                cal.set(Calendar.DAY_OF_WEEK, 1);
                sb.append(DateUtil.format(cal.getTime(), "yyyy-MM-dd"));
            }
            result.put(i, sb.toString());
            //调用 org.apache.commons.lang.time.DateUtils 包下的方法
            //新增一天到下一周的开始日期
            cal.setTime(DateUtils.addDays(cal.getTime(), 1));
        }
        return result;
    }

    public Map<Integer, String> getWeekOfDate(int year) {
        Map<Integer, String> result = new HashMap<>(55);
//        LambdaQueryWrapper<MdmWeek> queryWrapper = new LambdaQueryWrapper();
//        queryWrapper.eq(MdmWeek::getYear, year);
        List<MdmWeek> weekList = new ArrayList<>();
        if(2024 == year){
            weekList = weekList2024;
        }else{
            weekList = weekList2025;
        }
        StringBuilder sb = new StringBuilder();
        int i = 0;
        for (MdmWeek week : weekList) {
            i++;
            sb = new StringBuilder();
            sb.append(DateUtil.format(week.getStartDate(), "yyyy-MM-dd"));
            sb.append("|");
            sb.append(DateUtil.format(week.getEndDate(), "yyyy-MM-dd"));
            result.put(i, sb.toString());
        }
        return result;
    }


    public static int getWeekIndex(Date day) {
        //获取统计区间所在在周范围
        int endIndex = DateUtil.weekOfYear(day);
        int weekOfMonth = DateUtil.weekOfMonth(day);
        //最后一个月的最后一周算出来都是1
        if (endIndex == 1 && weekOfMonth != 1) {
            endIndex = 53;
        }
        return endIndex;
    }

    public static void main(String[] args) {
//        Map<Integer, String> map = getWeekOfYear(2025);
//        System.out.println(map);
    }

}
