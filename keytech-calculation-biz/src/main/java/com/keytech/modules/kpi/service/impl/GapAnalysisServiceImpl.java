package com.keytech.modules.kpi.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUnit;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.google.common.collect.Lists;
import com.keytech.modules.action.plan.entity.ActionPlanTrack;
import com.keytech.modules.action.plan.service.IActionPlanTrackService;
import com.keytech.modules.fivewhy.entity.FiveWhyDetail;
import com.keytech.modules.fivewhy.service.IFiveWhyDetailService;
import com.keytech.modules.kpi.dto.CellDataVo;
import com.keytech.modules.kpi.dto.KpiDiffAnalysisDto;
import com.keytech.modules.kpi.dto.KpiReportValueDto;
import com.keytech.modules.kpi.entity.GapAnalysis;
import com.keytech.modules.kpi.entity.KpiReportActualMonthValue;
import com.keytech.modules.kpi.entity.KpiReportTargetValue;
import com.keytech.modules.kpi.mapper.GapAnalysisMapper;
import com.keytech.modules.kpi.service.IGapAnalysisService;
import com.keytech.modules.kpi.service.IKpiReportActualMonthValueService;
import com.keytech.modules.kpi.service.IKpiReportTargetValueService;
import com.keytech.modules.kpi.util.PiColourEnum;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.keytech.base.service.impl.BaseServiceImpl;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @Description: 差距分析
 * @Author: jeecg-boot
 * @Date:   2025-02-28
 * @Version: V1.0
 */
@Service
public class GapAnalysisServiceImpl extends BaseServiceImpl<GapAnalysisMapper, GapAnalysis> implements IGapAnalysisService {
    @Autowired
    private IFiveWhyDetailService fiveWhyDetailService;
    @Autowired
    private IActionPlanTrackService actionPlanTrackService;
    @Autowired
    private IKpiReportTargetValueService targetValueService;
    @Autowired
    private IKpiReportActualMonthValueService monthValueService;
    @Override
    public void add(GapAnalysis gapAnalysis) {
        String id = IdUtil.getSnowflakeNextIdStr();
        if(null == gapAnalysis.getId()){
            gapAnalysis.setId(id);
        }

        //行动计划集合数据
        List<ActionPlanTrack> actionPlanTrackList = new ArrayList<>();
        List<FiveWhyDetail> detailList = new ArrayList<>();
        for(FiveWhyDetail item : gapAnalysis.getDetailList()){
            if(item.getId().indexOf("temp_") >= 0){
                item.setId(IdUtil.getSnowflakeNextIdStr());
                item.setType("3");
                item.setRecordId(gapAnalysis.getId());
                //判断是否添加至行动计划
                if(item.getActionType()){
                    ActionPlanTrack actionPlanTrack = new ActionPlanTrack();
                    BeanUtils.copyProperties(item, actionPlanTrack);
                    actionPlanTrack.setFiveWhyMeasuresId(item.getId());
                    actionPlanTrack.setActionTitle(item.getSolveProblem());
                    actionPlanTrack.setEnterMeeting("0");
                    actionPlanTrack.setRaiseDate(new Date());
                    actionPlanTrack.setImportanceEvaluation("2");
                    actionPlanTrack.setProblemDescription(item.getReason());
                    actionPlanTrack.setPkiInfo(gapAnalysis.getKpiCode());
                    actionPlanTrack.setDataId(gapAnalysis.getId());

                    //计算剩余时间
                    int betweenDay = (int) DateUtil.between(actionPlanTrack.getPlanFinishDate(), new Date(), DateUnit.DAY);
                    if(actionPlanTrack.getPlanFinishDate().before(new Date())){
                        betweenDay = -betweenDay;
                    }
                    actionPlanTrack.setSurplusDay(betweenDay+"");
                    actionPlanTrackList.add(actionPlanTrack);
                    detailList.add(item);
                }
            }
        }
        fiveWhyDetailService.saveBatch(detailList);
        actionPlanTrackService.saveBatch(actionPlanTrackList);
        this.saveOrUpdate(gapAnalysis);
    }

    @Override
    public List<JSONObject> monthGapValue(KpiDiffAnalysisDto dto) {
        String month = dto.getMonth();
        DateTime parse = DateUtil.parse(month, "yyyy-MM");
        //查询月表数据
        KpiReportValueDto kpiReportActualDayValueDto = BeanUtil.copyProperties(dto, KpiReportValueDto.class);
        kpiReportActualDayValueDto.setMonths(Lists.newArrayList(month));
        kpiReportActualDayValueDto.setYear(DateUtil.year(parse));
        //月数据和年数据
        List<KpiReportActualMonthValue> listMonthDataBy = monthValueService.listMonthDataBy(kpiReportActualDayValueDto, Lists.newArrayList(dto.getKpiCode()));
        JSONObject jsonObject = new JSONObject();
        jsonObject.putOpt("title", "月份").putOpt("month", month);
        List<JSONObject> result = Lists.newArrayList();
        Map<String, String> monthMap = targetValueService.monthMap(false);
        String monthStr = month.substring(month.length() - 2);
        String monthTargetKey = monthMap.get(monthStr);
        for (KpiReportActualMonthValue monthValue : listMonthDataBy) {
            //月和年的
            int indicator = monthValue.getIndicator();
            String value = monthValue.getValue();
            //差据和当年目标值 {"diffValue":"22","janValue":"1"}
            String extJson = monthValue.getExtJson();
            String valueKey = "{}Value", diffKey = "{}Diff", sunDiffKey = "{}SumDiff", targetValue = "{}TargetValue";
            JSONObject extJsonObject = JSONUtil.parseObj(extJson);
            if(indicator == 1 || indicator == 3){
                String fmt="";
                String target="";
                if (indicator == 1) {
                    //月数据
                    fmt = "month";
                    target = monthTargetKey;
                } else if(indicator == 3) {
//                fmt = "year";
                    fmt = "year";
                    target = month+"-yearAccumulate";
                    String monthCompare =  month.replace("-","");
                    if(!monthCompare.equals(monthValue.getMonth()+"")){
                        continue;
                    }
                }
                jsonObject.putOpt(StrUtil.format(valueKey, fmt), value).putOpt(StrUtil.format(diffKey, fmt), extJsonObject.getStr("diffValue", ""))
                        .putOpt(StrUtil.format(targetValue, fmt), extJsonObject.getStr(target, ""))
                        .putOpt(StrUtil.format(sunDiffKey, fmt), extJsonObject.getStr("sumDiff", ""));
            }
        }
        result.add(jsonObject);
        return result;
    }
    //月度跟踪
    @Override
    public List<JSONObject> monthTracking(KpiDiffAnalysisDto dto) {
        //查询月表数据
        String monthStr = dto.getMonth();
        DateTime parse = DateUtil.parse(monthStr, "yyyy-MM");
        KpiReportValueDto kpiReportActualDayValueDto = BeanUtil.copyProperties(dto, KpiReportValueDto.class);
        int year = DateUtil.year(parse);
        kpiReportActualDayValueDto.setYear(year);
        //月数据和年数据
        List<KpiReportActualMonthValue> listMonthDataBy = monthValueService.listMonthDataBy(kpiReportActualDayValueDto, Lists.newArrayList(dto.getKpiCode()));
//        Map<String, KpiReportActualMonthValue> monthValueMap = listMonthDataBy.stream().collect(Collectors.toMap(bean -> String.valueOf(bean.getMonth()), bean -> bean,(k1,k2)->k2));
        //数据转换 key yyyyMM+数据类型, value 对象
        Map<String, KpiReportActualMonthValue> monthValueMap = listMonthDataBy.stream().collect(Collectors.toMap(bean -> String.valueOf(bean.getMonth())+String.valueOf(bean.getIndicator()), bean -> bean,(k1, k2)->k2));
        //获取目标值
//        List<KpiReportTargetValue> list = targetValueService.list(new LambdaQueryWrapper<KpiReportTargetValue>().eq(KpiReportTargetValue::getReportId, dto.getReportId()).eq(KpiReportTargetValue::getYear, year).eq(KpiReportTargetValue::getKpiCode, dto.getKpiCode()));
        List<KpiReportTargetValue> list = targetValueService.list(new LambdaQueryWrapper<KpiReportTargetValue>().eq(KpiReportTargetValue::getYear, year).eq(KpiReportTargetValue::getKpiCode, dto.getKpiCode()));
        //目标值转换 key
        Map<Integer, KpiReportTargetValue> collect = list.stream().collect(Collectors.toMap(KpiReportTargetValue::getYear, bean -> bean, (k1, k2) -> k2));
        List<JSONObject> result = Lists.newArrayList();
        Map<String, String> monthMap = targetValueService.monthMap(false);
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
                if(monthValueKey.contains("-yearAccumulate")){
                    //年累
                    jsonObject.putOpt(yearValueKey, cellDataVo);
                }else{
                    jsonObject.putOpt(monthValueKey, cellDataVo);
                }

            }
            result.add(jsonObject);
        }
        return result;
    }
}
