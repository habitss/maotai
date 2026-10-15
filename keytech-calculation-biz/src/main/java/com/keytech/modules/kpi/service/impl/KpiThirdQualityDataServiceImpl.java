package com.keytech.modules.kpi.service.impl;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.keytech.modules.common.enums.TimeDimension;
import com.keytech.modules.kpi.entity.KpiInfo;
import com.keytech.modules.kpi.entity.KpiReportActualDayValue;
import com.keytech.modules.kpi.entity.KpiReportTargetValue;
import com.keytech.modules.kpi.entity.KpiThirdQualityData;
import com.keytech.modules.kpi.mapper.KpiThirdQualityDataMapper;
import com.keytech.modules.kpi.service.*;
import com.keytech.modules.kpi.util.MonthEnum;
import com.keytech.modules.mdm3.entity.MdmWeek;
import com.keytech.modules.mdm3.service.IMdmWeekService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.keytech.base.service.impl.BaseServiceImpl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * @Description: KPI第三方接口-质量KPI
 * @Author: jeecg-boot
 * @Date:   2025-08-18
 * @Version: V1.0
 */
@Service
public class KpiThirdQualityDataServiceImpl extends BaseServiceImpl<KpiThirdQualityDataMapper, KpiThirdQualityData> implements IKpiThirdQualityDataService {
    @Value("${mes-system.tenant-id}")
    private Integer tenantId;
    @Autowired
    private IKpiInfoService kpiInfoService;
    @Autowired
    private IKpiReportTargetValueService targetValueService;
    @Autowired
    private IKpiReportActualWeekValueService weekValueService;
    @Autowired
    private IMdmWeekService mdmWeekService;
    @Autowired
    private IKpiReportActualDayValueService kpiReportActualDayValueService;

    /**
     * 接收质量KPI数据
     * @param kpiThirdQualityDataList
     */
    @Override
    public void qualityData(List<KpiThirdQualityData> kpiThirdQualityDataList) {
        //设置租户ID
        kpiThirdQualityDataList.forEach(kpiThirdQualityData -> {
            kpiThirdQualityData.setTenantId(tenantId);
        });
        //存储数据
        saveOrUpdateBatch(kpiThirdQualityDataList);

        for (KpiThirdQualityData kpiThirdQualityData : kpiThirdQualityDataList) {
            String year = kpiThirdQualityData.getYear();
            if (StrUtil.isBlank(year)) {
                String month = kpiThirdQualityData.getMonth();
                if (StrUtil.isNotBlank(month)) {
                    year = month.substring(0, 4);
                } else if (kpiThirdQualityData.getWeek() != null) {
                    year = kpiThirdQualityData.getWeek().substring(0, 4);
                }
            }
            //判断unit是否包含% 且 indexnum<=1 就将indexnum*100
            String unit = kpiThirdQualityData.getUnit();
            Double indexNum = kpiThirdQualityData.getIndexNum();
            if (StrUtil.isNotBlank(unit) && unit.contains("%")
                    && indexNum != null && indexNum <= 1) {
                kpiThirdQualityData.setIndexNum(indexNum * 100);
            }

            //获取KPI数据(配置指数)
            LambdaQueryWrapper<KpiInfo> kpiLambdaQueryWrapper = new LambdaQueryWrapper<KpiInfo>();
            kpiLambdaQueryWrapper.isNotNull(KpiInfo::getThirdKey);
            kpiLambdaQueryWrapper.eq(KpiInfo::getThirdKey,kpiThirdQualityData.getIndexCode());
            kpiLambdaQueryWrapper.like(KpiInfo::getYear, year);
            kpiLambdaQueryWrapper.likeRight(KpiInfo::getThirdName,"质量-"); //第三方接口的指标名称为空
            List<KpiInfo> kpiInfoList = kpiInfoService.list(kpiLambdaQueryWrapper);
            if (!(null != kpiInfoList && kpiInfoList.size() > 0)) {
                log.error("KPI内没有第三方接口的指标");
                continue;
            }
            //数据合并
//            Map<String, KpiInfo> KpitMap = kpiInfoList.stream()
//                    .collect(Collectors.toMap(
//                            KpiInfo::getThirdKey, // Key的映射函数
//                            Function.identity()   // Value的映射函数（对象本身）
//                    ));
//            //设置KPI信息
//            kpiThirdQualityData.setKpiInfo(KpitMap.get(kpiThirdQualityData.getIndexCode()));
            //配置了相同指数
            for (KpiInfo kpiInfo : kpiInfoList) {
                kpiThirdQualityData.setKpiInfo(kpiInfo);
                //判断数据(年/月/周)
                TimeDimension dimension = TimeDimension.fromValue(kpiThirdQualityData.getTimeDimension());
                switch (dimension) {
                    case YEAR:
                        // 处理年
                        processYear(kpiThirdQualityData);
                        break;
                    case MONTH:
                        // 处理月
                        processMonth(kpiThirdQualityData);
                        break;
                    case WEEK:
                        // 处理周
                        processWeek(kpiThirdQualityData);
                        break;
                }
            }

        }
    }

    /**
     * 处理年
     */
    public void processYear(KpiThirdQualityData kpiThirdQualityData){
        //年
        String year = kpiThirdQualityData.getYear();
        //KPI
        KpiInfo kpiInfo = kpiThirdQualityData.getKpiInfo();
        //年键值
        String yearTarget = MonthEnum.YEAR.getValue();
        // #################################目标值#################################
        targetData(kpiInfo, kpiThirdQualityData, yearTarget, true);


        // #################################实际值#################################
        //实际值
        Double indexNum = kpiThirdQualityData.getIndexNum();
        Map<String, Object> map = new HashMap<>();
        map.put("year", year);
        map.put("kpiCode", kpiInfo.getKpiCode());
        map.put("reportId", kpiInfo.getDeptCode());
        map.put("factoryCode", kpiInfo.getFactoryCode());
        map.put("tenantId", kpiInfo.getTenantId());
        //同步年周月(实际值)
        weekValueService.addWeekAndMonth(map, year, indexNum);
    }

    /**
     * 处理月
     */
    public void processMonth(KpiThirdQualityData kpiThirdQualityData){
        //年
        String year = kpiThirdQualityData.getYear();
        // 年为空
        if (StrUtil.isBlank(year)) {
            String month = kpiThirdQualityData.getMonth();
            if (StrUtil.isNotBlank(month)) {
                year = month.substring(0, 4);
            }
        }
        //KPI
        KpiInfo kpiInfo = kpiThirdQualityData.getKpiInfo();

        String yearMonth = kpiThirdQualityData.getMonth();
        String month = "";
        String[] parts = yearMonth.split("-");
        if (parts.length == 2) {
            //month = Integer.parseInt(parts[1]) >= 10? parts[1] : "0"+parts[1];
            month = parts[1];
        }else{
            log.error("Kpi质量推送--月数据-月份为空");
            return;
        }
        //月键key
        String monthKey = MonthEnum.getValueByMonth(month);
        // #################################目标值#################################
        targetData(kpiInfo, kpiThirdQualityData, monthKey, true);

        // #################################同步月#################################
        //实际值
        Double indexNum = kpiThirdQualityData.getIndexNum();
        Map<String, Object> map = new HashMap<>();
        map.put("year", year);
        map.put("kpiCode", kpiInfo.getKpiCode());
        map.put("reportId", kpiInfo.getDeptCode());
        map.put("factoryCode", kpiInfo.getFactoryCode());
        map.put("tenantId", kpiInfo.getTenantId());
        //同步年周月(实际值月)
        weekValueService.addWeekAndMonth(map, year+"-"+month, indexNum);

        // #################################实际值#################################
        String key = year+"-"+month;
        actualData(kpiInfo,kpiThirdQualityData, key);

    }

    /**
     * 处理周
     */
    public void processWeek(KpiThirdQualityData kpiThirdQualityData){
        //年
        String year = kpiThirdQualityData.getYear();
        if (StrUtil.isBlank(year)){
            String week = kpiThirdQualityData.getWeek();
            if (StrUtil.isNotBlank(week)) {
                year = week.substring(0, 4);
            }
        }
        //KPI
        KpiInfo kpiInfo = kpiThirdQualityData.getKpiInfo();
        //获取年-周数
        String weekEndDate = kpiThirdQualityData.getWeek();
        //获取周数
        String weekNum = weekEndDate.substring(weekEndDate.lastIndexOf("-") + 1);
        //获取对应的周开始时间和结束时间
        MdmWeek mdmWeekQuery = new MdmWeek();
        mdmWeekQuery.setWeekNumber(Integer.parseInt(weekNum));
        mdmWeekQuery.setYear(year);
        List<MdmWeek> list = mdmWeekService.getWeek(mdmWeekQuery);
        //获取周  判断list不为空 有内容
        if(list != null && list.isEmpty()){
            log.error("Kpi质量推送--周数据-周为空");
            return;
        }
        MdmWeek mdmWeek = list.get(0);
        String weekKey = "w_"+year+"_"+weekNum;

        // #################################目标值#################################
        targetData(kpiInfo,kpiThirdQualityData,weekKey,false);

        // #################################同步周#################################
        //实际值
        Double indexNum = kpiThirdQualityData.getIndexNum();
        Map<String, Object> map = new HashMap<>();
        map.put("year", year);
        map.put("kpiCode", kpiInfo.getKpiCode());
        map.put("reportId", kpiInfo.getDeptCode());
        map.put("factoryCode", kpiInfo.getFactoryCode());
        map.put("tenantId", kpiInfo.getTenantId());

        String[] keys = weekKey.split("_");
        int weekNumber = Integer.parseInt(keys[2]);
        map.put("weekNumber",weekNumber);
        map.put("newKey",weekKey); //存储key w_2025_42
        String month = mdmWeek.getMonth();
        if(StrUtil.isNotBlank(month)){
            month = Integer.parseInt(month)<10?"0"+month:month;
        }
        map.put("monthStr",mdmWeek.getYear()+month);
        // yyyy-mm-dd|yyyy-mm-dd
        String startDate = DateUtil.format(mdmWeek.getStartDate(), "yyyy-MM-dd");
        String endDate = DateUtil.format(mdmWeek.getEndDate(), "yyyy-MM-dd");
        weekKey = startDate + "|" + endDate;
        weekValueService.addWeekAndMonth(map, weekKey, indexNum);


        // #################################实际值#################################
        actualData(kpiInfo,kpiThirdQualityData, map.get("newKey")+"");
    }

    //目标值写入
    public void targetData(KpiInfo kpiInfo, KpiThirdQualityData kpiThirdQualityData, String key, boolean isMonth){
        String year = kpiThirdQualityData.getYear();
        //年为空,从月/周中取
        if (StrUtil.isBlank(year)){
            if (isMonth) {
                //月
                String month = kpiThirdQualityData.getMonth();
                if (StrUtil.isNotBlank(month)) {
                    year = month.substring(0, 4);
                }
            }else{
                //周
                String week = kpiThirdQualityData.getWeek();
                if (StrUtil.isNotBlank(week)) {
                    year = week.substring(0, 4);
                }
            }
        }
        //查询目标值
        KpiReportTargetValue targetObj = targetValueService.getOne(new LambdaQueryWrapper<KpiReportTargetValue>()
                .eq(KpiReportTargetValue::getKpiCode, kpiInfo.getKpiCode())
                .eq(KpiReportTargetValue::getYear, year)
                .last("LIMIT 1") );
        //目标值
        Double targetValue = kpiThirdQualityData.getTargetValue();
        if(targetObj == null){
            //新增
            JSONObject jsonObject = new JSONObject();
            jsonObject.put(key, targetValue == null ? "" : targetValue);
            KpiReportTargetValue target = new KpiReportTargetValue();
            target.setTenantId(kpiInfo.getTenantId());
            target.setFactoryCode(kpiInfo.getFactoryCode());
            target.setDeptCode(kpiInfo.getDeptCode());
            target.setReportId(kpiInfo.getDeptCode());
            target.setKpiCode(kpiInfo.getKpiCode());
            target.setYear(Integer.valueOf(year));
            if (isMonth) {
                //月目标值配置
                target.setKpiTypeMonthValue(jsonObject.toJSONString());
                //周目标值配置
                target.setKpiTypeWeekValue(new JSONObject().toString());
            }else{
                target.setKpiTypeMonthValue(new JSONObject().toString());
                target.setKpiTypeWeekValue(jsonObject.toJSONString());
            }
            targetValueService.save(target);
        }else{
            //更新
            if (isMonth){
                String kpiTypeMonthValue = targetObj.getKpiTypeMonthValue();
                cn.hutool.json.JSONObject json = new cn.hutool.json.JSONObject(kpiTypeMonthValue);
                json.putOpt(key, targetValue == null ? "" : targetValue);
                targetObj.setKpiTypeMonthValue(json.toString());
            }else{
                String kpiTypeWeekValue = targetObj.getKpiTypeWeekValue();
                cn.hutool.json.JSONObject json = new cn.hutool.json.JSONObject(kpiTypeWeekValue);
                json.putOpt(key, targetValue == null ? "" : targetValue);
                targetObj.setKpiTypeWeekValue(json.toString());
            }
            targetValueService.updateById(targetObj);
        }
    }


    /**
     * 实际值写入
     */
    public void actualData(KpiInfo kpiInfo, KpiThirdQualityData kpiThirdQualityData, String actualKey) {
        String year = kpiThirdQualityData.getYear();
        //年为空,从月/周中取
        if (StrUtil.isBlank(year)){
            String month = kpiThirdQualityData.getMonth();
            if (StrUtil.isNotBlank(month)) {
                //月
                year = month.substring(0,4);
            }else {
                //周
                String week = kpiThirdQualityData.getWeek();
                if (StrUtil.isNotBlank(week)) {
                    year = week.substring(0,4);
                }

            }
        }
        Double indexNum = kpiThirdQualityData.getIndexNum();
        //获取表中实际值
        List<KpiReportActualDayValue> actualList = kpiReportActualDayValueService.list(new LambdaQueryWrapper<KpiReportActualDayValue>()
                .eq(KpiReportActualDayValue::getKpiCode, kpiInfo.getKpiCode())
                .eq(KpiReportActualDayValue::getYear, year));

        Optional<KpiReportActualDayValue> optionalFirst = actualList.stream().findFirst();
        if (optionalFirst.isPresent()) {
            KpiReportActualDayValue first = optionalFirst.get();
            String value = first.getValue();
            JSONObject jsonObject = JSONObject.parseObject(value);
            jsonObject.put(actualKey, indexNum);
            first.setValue(jsonObject.toJSONString());
            first.setIndicator(1);
            kpiReportActualDayValueService.updateById(first);
        } else {
            KpiReportActualDayValue dayValue = new KpiReportActualDayValue();
            JSONObject jsonObject = new JSONObject();
            jsonObject.put(actualKey, indexNum);
            dayValue.setValue(jsonObject.toJSONString());
            dayValue.setYear(Integer.parseInt(year));
            dayValue.setReportId(kpiInfo.getDeptCode());
            dayValue.setKpiCode(kpiInfo.getKpiCode());
            dayValue.setFactoryCode(kpiInfo.getFactoryCode());
            dayValue.setDeptCode(kpiInfo.getDeptCode());
            dayValue.setIndicator(1);
            dayValue.setTenantId(kpiInfo.getTenantId());
            kpiReportActualDayValueService.save(dayValue);
        }
    }



}
