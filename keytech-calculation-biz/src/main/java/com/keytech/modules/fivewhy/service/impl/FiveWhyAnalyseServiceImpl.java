package com.keytech.modules.fivewhy.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateUnit;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.keytech.base.service.impl.BaseServiceImpl;
import com.keytech.modules.action.plan.entity.ActionPlanTrack;
import com.keytech.modules.action.plan.service.IActionPlanTrackService;
import com.keytech.modules.fivewhy.dto.FiveWhyAnalyseDto;
import com.keytech.modules.fivewhy.dto.FiveWhyResonDto;
import com.keytech.modules.fivewhy.entity.FiveWhyAnalyse;
import com.keytech.modules.fivewhy.entity.FiveWhyDetail;
import com.keytech.modules.fivewhy.mapper.FiveWhyAnalyseMapper;
import com.keytech.modules.fivewhy.service.IFiveWhyAnalyseService;
import com.keytech.modules.fivewhy.service.IFiveWhyDetailService;
import com.keytech.modules.kpi.entity.KpiInfo;
import com.keytech.modules.kpi.entity.KpiReportActualMonthValue;
import com.keytech.modules.kpi.entity.KpiReportActualWeekValue;
import com.keytech.modules.kpi.entity.KpiReportTargetValue;
import com.keytech.modules.kpi.service.IKpiInfoService;
import com.keytech.modules.kpi.service.IKpiReportActualMonthValueService;
import com.keytech.modules.kpi.service.IKpiReportActualWeekValueService;
import com.keytech.modules.kpi.service.IKpiReportTargetValueService;
import com.keytech.modules.kpi.util.BizConstant;
import jakarta.servlet.http.HttpServletRequest;
import org.jeecg.common.system.query.QueryGenerator;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @Description: 5Why分析
 * @Author: jeecg-boot
 * @Date:   2025-01-20
 * @Version: V1.0
 */
@Service
public class FiveWhyAnalyseServiceImpl extends BaseServiceImpl<FiveWhyAnalyseMapper, FiveWhyAnalyse> implements IFiveWhyAnalyseService {
    @Autowired
    private IActionPlanTrackService actionPlanTrackService;

    @Autowired
    private IFiveWhyDetailService fiveWhyDetailService;

    @Autowired
    private IKpiInfoService kpiInfoService;

    /**周实际值**/
    @Autowired
    private IKpiReportActualWeekValueService actualWeekValueService;

    /**月实际值**/
    @Autowired
    private IKpiReportActualMonthValueService actualMonthValueService;
    @Autowired
    private FiveWhyAnalyseMapper fiveWhyAnalyseMapper;

    /**kpi目标值-原始表**/
    @Autowired
    private IKpiReportTargetValueService targetValueService;

    @Override
    @Transactional
    public void saveOrUpdateFiveWhy(FiveWhyAnalyseDto fiveWhyAnalyseDto) {
        if (null != fiveWhyAnalyseDto) {
            if(StrUtil.isBlank(fiveWhyAnalyseDto.getId()) || fiveWhyAnalyseDto.getId().contains("temp")){
                fiveWhyAnalyseDto.setId(IdUtil.getSnowflakeNextIdStr());
            }
            FiveWhyAnalyse fiveWhyAnalyse = new FiveWhyAnalyse();
            BeanUtils.copyProperties(fiveWhyAnalyseDto, fiveWhyAnalyse);
            //当前日期
            Date now = DateUtil.beginOfDay(new Date());
            //行动计划集合数据
            List<ActionPlanTrack> actionPlanTrackList = new ArrayList<>();
            //措施集合数据
            List<FiveWhyDetail> fiveWhyDetailList = fiveWhyAnalyseDto.getFiveWhyDetailList();
            //5Why集合数据
            List<FiveWhyResonDto> resonDtoList = fiveWhyAnalyseDto.getResonDtoList();
            //遍历数据，生成ID
            resonDtoList.forEach(item -> {
                if (StrUtil.isNotBlank(item.getId()) || item.getId().contains("temp_")) {
                    String id = IdUtil.getSnowflakeNextIdStr();
                    for(FiveWhyDetail fiveWhyDetail : fiveWhyDetailList){
                        if(item.getId().equals(fiveWhyDetail.getFiveWhyId())){
                            fiveWhyDetail.setFiveWhyId(id);
                            break;
                        }
                    }
                    item.setId(id);
                }
            });

            //处理措施数据
            fiveWhyDetailList.forEach(item -> {
                if (StrUtil.isNotBlank(item.getId()) && item.getId().contains("temp_")) {
                    item.setId(IdUtil.getSnowflakeNextIdStr());
                }

                //如果包含temp说明措施没有与5w的原因框关联，清除临时绑定的id
                if (StrUtil.isNotBlank(item.getFiveWhyId()) && item.getFiveWhyId().contains("temp_")) {
                    item.setFiveWhyId(null);
                }
                //设置5w记录id
                item.setRecordId(fiveWhyAnalyse.getId());
                item.setType(fiveWhyAnalyse.getType());
                //判断是否添加至行动计划
                if(item.getActionType() && item.getIsEnable()){
                    ActionPlanTrack actionPlanTrack = new ActionPlanTrack();
                    BeanUtils.copyProperties(item, actionPlanTrack);
                    actionPlanTrack.setFiveWhyMeasuresId(item.getId());
                    actionPlanTrack.setActionTitle(item.getSolveProblem());
                    actionPlanTrack.setEnterMeeting("0");
                    actionPlanTrack.setRaiseDate(now);
                    actionPlanTrack.setImportanceEvaluation("2");
                    actionPlanTrack.setProblemDescription(item.getReason());
                    actionPlanTrack.setPkiInfo(fiveWhyAnalyseDto.getFieldName());
                    actionPlanTrack.setDataId(fiveWhyAnalyseDto.getId());
                    actionPlanTrack.setStatus(item.getIsEnable() ? "1" : "0");

                    //计算剩余时间
                    int betweenDay = (int) DateUtil.between(actionPlanTrack.getPlanFinishDate(), now, DateUnit.DAY);
                    if(actionPlanTrack.getPlanFinishDate().before(now)){
                        betweenDay = -betweenDay;
                    }
                    actionPlanTrack.setSurplusDay(betweenDay+"");
                    actionPlanTrackList.add(actionPlanTrack);
                }
            });

            //添加行动计划数据
            if(CollUtil.isNotEmpty(actionPlanTrackList)){
                //先查询行动计划
                List<String> ids = actionPlanTrackList.stream()
                        .map(ActionPlanTrack::getFiveWhyMeasuresId)
                        .collect(Collectors.toList());
                List<ActionPlanTrack> queryList = actionPlanTrackService.list(
                        new LambdaQueryWrapper<ActionPlanTrack>().in(ActionPlanTrack::getFiveWhyMeasuresId, ids)
                );
                Map<String, ActionPlanTrack> actionPlanTrackMap = queryList.stream()
                        .collect(Collectors.toMap(ActionPlanTrack::getFiveWhyMeasuresId, a -> a));
                //如果存在则调整数据，否则直接新增
                if(CollUtil.isNotEmpty(queryList)){
                    //删除已存在的计划，避免重复添加
                    actionPlanTrackList.removeIf(item -> actionPlanTrackMap.containsKey(item.getFiveWhyMeasuresId()));
                }
                if(CollUtil.isNotEmpty(actionPlanTrackList)){
                    actionPlanTrackService.saveBatch(actionPlanTrackList);
                }
            }
            fiveWhyAnalyse.setReasonJson(JSONUtil.toJsonStr(resonDtoList));
            fiveWhyDetailService.saveOrUpdateBatch(fiveWhyDetailList);
            this.saveOrUpdate(fiveWhyAnalyse);
        }
    }

    @Override
    @Transactional
    public void deleteFiveWhy(String id) {
        fiveWhyDetailService.remove(new LambdaQueryWrapper<FiveWhyDetail>().eq(FiveWhyDetail::getRecordId, id));
        this.removeById(id);
    }

    @Override
    public FiveWhyAnalyseDto queryData(FiveWhyAnalyse fiveWhyAnalyse, HttpServletRequest req) {
        QueryWrapper<FiveWhyAnalyse> queryWrapper = QueryGenerator.initQueryWrapper(fiveWhyAnalyse, null != req ? req.getParameterMap() : null);
        queryWrapper.orderByDesc("create_time");
        List<FiveWhyAnalyse> list = this.list(queryWrapper);
        FiveWhyAnalyseDto dto = new FiveWhyAnalyseDto();
        if(CollUtil.isNotEmpty(list)){
            FiveWhyAnalyse queryData = list.get(0);

            QueryWrapper<FiveWhyDetail> detailQueryWrapper = new QueryWrapper<>();
            detailQueryWrapper.eq("record_id", queryData.getId());
            List<FiveWhyDetail> detailList = fiveWhyDetailService.list(detailQueryWrapper);

            fiveWhyDetailService.list();
            BeanUtils.copyProperties(queryData, dto);
            dto.setFiveWhyDetailList(detailList);
            dto.setResonDtoList(JSONUtil.toList(queryData.getReasonJson(), FiveWhyResonDto.class));
        } else {
            //类型为kpi，且数据为空时，需要查询kpi数据组合成原因
            if("2".equals(fiveWhyAnalyse.getType())){
                dto.setProblemDesc(problemDesByKpiDefault(fiveWhyAnalyse.getKpiCode(), fiveWhyAnalyse.getKpiYear(), fiveWhyAnalyse.getKpiColKey()));
            }
        }
        return dto;
    }

    /**
     * 获取KPI 默认的5WHY分析原因
     * @param kpiCode kpi编码
     * @param year kpi传递过来的年份
     * @param kpiColKey kpi传递过来的colKey
     * @return 结果
     */
    private String problemDesByKpiDefault(String kpiCode,String year, String kpiColKey){
        String reson = "";
        String cycle = kpiColKey;
        //查询KPI基本信息
        List<KpiInfo> kpiInfoList = kpiInfoService.lambdaQuery().eq(KpiInfo::getKpiCode,kpiCode).eq(KpiInfo::getYear,year).list();
        KpiInfo kpiInfo = null;
        if(CollUtil.isNotEmpty(kpiInfoList)){
            kpiInfo = kpiInfoList.get(0);
        }
        //根据title的格式来判断是周累还是月累
        if(null != kpiInfo && StrUtil.isNotBlank(kpiColKey)){
            //实际值
            String actualValue = "";
            //计划值
            String planValue = "";
            //颜色名称（红区、绿区）
            String colorName = "";

            //取目标值
            List<KpiReportTargetValue> targetValueList = targetValueService.lambdaQuery().eq(KpiReportTargetValue::getKpiCode,kpiCode)
                    .eq(KpiReportTargetValue::getYear,year)
                    .list();
            KpiReportTargetValue targetValue = null;
            if(CollUtil.isNotEmpty(targetValueList)){
                targetValue = targetValueList.get(0);
            }
            //周累
            if(kpiColKey.contains("w_")){
                String[] dateObj = kpiColKey.split("_");
                //查询当前的目标值和实际值
                List<KpiReportActualWeekValue> actualWeekValueList = actualWeekValueService.lambdaQuery()
                        .eq(KpiReportActualWeekValue::getIndicator,0)
                        .eq(KpiReportActualWeekValue::getKpiCode, kpiCode)
                        .eq(KpiReportActualWeekValue::getYear, year)
                        .eq(KpiReportActualWeekValue::getWeekNumber, dateObj[2]).list();

                KpiReportActualWeekValue actualWeekValue = null;

                if(CollUtil.isNotEmpty(actualWeekValueList)){
                    actualWeekValue = actualWeekValueList.get(0);
                }
                if (actualWeekValue != null) {
                    cycle = DateUtil.format(actualWeekValue.getStartTime(), "yyyy-MM-dd")+ "|" + DateUtil.format(actualWeekValue.getEndTime(), "yyyy-MM-dd");
                }
                if(null != actualWeekValue){
                    actualValue = actualWeekValue.getValue();
                    //显示颜色 1:green ,2:red ,3:yellow
                    int color = actualWeekValue.getColour();
                    colorName = color == 1 ? "绿区" : color == 2 ? "红区" : color == 3 ? "黄区" : "";
                }

                String weekJson = null;
                if (targetValue != null) {
                    weekJson = targetValue.getKpiTypeWeekValue();
                }
                JSONObject extJsonObject = JSONObject.parseObject(weekJson);
                if(Objects.nonNull(extJsonObject)){
                    planValue = null != extJsonObject.get(kpiColKey) ? extJsonObject.get(kpiColKey).toString() : "";
                }
            }
            //月累
            if(kpiColKey.length() == 7){
                //title的格式是2024-09，需要转换成202409
                //获取月份
                int month = Integer.parseInt(kpiColKey.split("-")[1]);
                String newTitle = kpiColKey.replace("-","");

                List<KpiReportActualMonthValue> actualMonthValueList = actualMonthValueService.lambdaQuery().eq(KpiReportActualMonthValue::getIndicator,1)
                        .eq(KpiReportActualMonthValue::getKpiCode,kpiCode)
                        .eq(KpiReportActualMonthValue::getMonth,newTitle)
                        .list();
                KpiReportActualMonthValue actualMonthValue = null;
                if(CollUtil.isNotEmpty(actualMonthValueList)){
                    actualMonthValue = actualMonthValueList.get(0);
                }

                if(null != actualMonthValue){
                    actualValue = actualMonthValue.getValue();
                    //显示颜色 1:green ,2:red ,3:yellow
                    int color = actualMonthValue.getColour();
                    colorName = color == 1 ? "绿区" : color == 2 ? "红区" : color == 3 ? "黄区" : "";
                }
                String monthJson = null;
                if (targetValue != null) {
                    monthJson = targetValue.getKpiTypeMonthValue();
                }
                JSONObject extJsonObject = JSONObject.parseObject(monthJson);
                if(Objects.nonNull(extJsonObject)){
                    //查询kpi对应的目标值
                    String[] ALIASES = BizConstant.MONTHS;
                    String key = ALIASES[month - 1] + "Value";
                    planValue = null != extJsonObject.get(key) ? extJsonObject.get(key).toString() : "";
                }
            }

            reson = "KPI名称："+ kpiInfo.getKpiName() + ",周期为：" + cycle  + "，目标值："+ planValue + ",实际值：" + actualValue +
                    ",颜色为：" +  colorName;
        }
        return reson;
    }

    @Override
    public List<FiveWhyAnalyse> listDel(String startDateStr, String endDateStr, List<String> kpiCodeList) {
        return fiveWhyAnalyseMapper.listDel(startDateStr, endDateStr, kpiCodeList);
    }
}
