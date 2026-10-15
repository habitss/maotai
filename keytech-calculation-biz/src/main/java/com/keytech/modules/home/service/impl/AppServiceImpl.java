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
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.google.common.collect.Lists;
import com.keytech.base.service.impl.BaseServiceImpl;
import com.keytech.modules.action.plan.entity.ActionPlanProblemType;
import com.keytech.modules.action.plan.service.IActionPlanProblemTypeService;
import com.keytech.modules.action.plan.service.IActionPlanTrackService;
import com.keytech.modules.cop.dto.CopPlanRank;
import com.keytech.modules.cop.service.ICopScheduleService;
import com.keytech.modules.home.dto.EmpRaFromDto;
import com.keytech.modules.home.dto.ReachTheStandardVo;
import com.keytech.modules.home.dto.StatisticalVo;
import com.keytech.modules.home.dto.WSActionStatisticsDto;
import com.keytech.modules.home.mapper.HomeMapper;
import com.keytech.modules.home.service.IAppService;
import com.keytech.modules.home.service.IHomeService;
import com.keytech.modules.home.vo.EmpRaFromVo;
import com.keytech.modules.kpi.dto.KpiTypeDto;
import com.keytech.modules.kpi.entity.KpiExceptionSummary;
import com.keytech.modules.kpi.entity.KpiInfo;
import com.keytech.modules.kpi.entity.KpiReportActualMonthValue;
import com.keytech.modules.kpi.entity.KpiReportActualWeekValue;
import com.keytech.modules.kpi.service.IKpiExceptionSummaryService;
import com.keytech.modules.kpi.service.IKpiInfoService;
import com.keytech.modules.kpi.service.IKpiReportActualMonthValueService;
import com.keytech.modules.kpi.service.IKpiReportActualWeekValueService;
import com.keytech.modules.kpi.util.DateUtils;
import com.keytech.modules.kpi.vo.ExcPercentVo;
import com.keytech.modules.manage.check.service.IManageCheckListService;
import com.keytech.modules.mdm3.entity.MdmWeek;
import com.keytech.modules.mdm3.service.IMdmWeekService;
import com.keytech.modules.meeting.entity.Meeting;
import com.keytech.modules.meeting.service.IMeetingService;
import com.keytech.modules.period.service.IPeriodVerificationScheduleService;
import com.keytech.modules.pi.entity.PiAbnormalAnalysis;
import com.keytech.modules.pi.entity.PiAbnormalAnalysisEvaluation;
import com.keytech.modules.pi.entity.PiCustomizeTable;
import com.keytech.modules.pi.entity.PiCustomizeTableField;
import com.keytech.modules.pi.service.IPiAbnormalAnalysisEvaluationService;
import com.keytech.modules.pi.service.IPiAbnormalAnalysisService;
import com.keytech.modules.pi.service.IPiCustomizeTableFieldService;
import com.keytech.modules.pi.service.IPiCustomizeTableService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.SecurityUtils;
import org.jeecg.common.config.TenantContext;
import org.jeecg.common.exception.JeecgBootException;
import org.jeecg.common.system.query.QueryGenerator;
import org.jeecg.common.system.vo.LoginUser;
import org.jeecg.modules.system.entity.SysDepart;
import org.jeecg.modules.system.entity.SysUser;
import org.jeecg.modules.system.service.ISysDepartService;
import org.jeecg.modules.system.service.ISysUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @Description: APP查询
 * @Author: hq
 * @Date:   2025-02-07
 * @Version: V1.0
 */
@Service
@Slf4j
public class AppServiceImpl implements IAppService {

    @Autowired
    private IKpiExceptionSummaryService kpiExceptionSummaryService;
    @Autowired
    private IActionPlanTrackService actionPlanTrackService;

    @Autowired
    private IMeetingService meetingService;
    @Autowired
    private IPiCustomizeTableService piCustomizeTableService;
    @Autowired
    private IPiCustomizeTableFieldService piCustomizeTableFieldService;
    @Autowired
    private IPiAbnormalAnalysisEvaluationService piAbnormalAnalysisEvaluationService;
    @Autowired
    private IPiAbnormalAnalysisService piAbnormalAnalysisService;
    @Autowired
    private ISysUserService iSysUserService;
    @Autowired
    private IActionPlanProblemTypeService actionPlanProblemTypeService;

    /**
     * App首页-统计看板 统计
     * pi 异常 KPI 异常 今日会议 Ra 单
     * @param dto
     * @return
     */
    @Override
    public JSONObject workbenchesSt(WSActionStatisticsDto dto) {
        LoginUser sysUser = (LoginUser) SecurityUtils.getSubject().getPrincipal();
        if (sysUser == null) {
            throw new JeecgBootException("未登陆");
        }

        String endTime = dto.getOpTime();
        String startTime = dto.getEdTime();
        if (StrUtil.isEmpty(startTime) && StrUtil.isEmpty(endTime)) {
            DateTime date = DateUtil.date();
            startTime = date.toDateStr();
            endTime = startTime;
        }
        //pi
        String finalStartTime = startTime;
        String finalEndTime = endTime;
        CompletableFuture<String> piExcCount = CompletableFuture.supplyAsync(() -> getPiExcCount(new ArrayList<>(), finalStartTime, finalEndTime)).exceptionally(ex -> "0/0");

        //kpi
        String orgCode = sysUser.getOrgCode();
        CompletableFuture<String> kpiExcCount = CompletableFuture.supplyAsync(() -> getKpiExcCount(orgCode, finalStartTime, finalEndTime)).exceptionally(ex -> "0/0");

        JSONObject data = new JSONObject();
        //会议
        List<Meeting> list = meetingService.lambdaQuery().eq(Meeting::getDeptCode, orgCode).
                ge(Meeting::getMeetingDate, startTime).le(Meeting::getMeetingDate, endTime)
                .list();
        long meetCount = list.stream()
                .filter(bean -> Objects.equals(1, bean.getActive()))
                .filter(bean -> bean.getMeetingDate() != null && bean.getMeetingDate().before(new Date()))
                .count();
        data.putOpt("meetCount", meetCount + "/" + list.size());
        //ra 单
        CompletableFuture<String> raCount = CompletableFuture.supplyAsync(() -> getRaCount(sysUser,finalStartTime,finalEndTime)).exceptionally(ex -> "0/0");

        try {
            data.putOpt("piExcCount", piExcCount.get());
            data.putOpt("kpiExcCount", kpiExcCount.get());
            data.putOpt("raCount", raCount.get());
        } catch (Exception e) {
            data.putOpt("piExcCount", 0);
            data.putOpt("kpiExcCount", 0);
            data.putOpt("raCount", 0);
            log.error("统计查询错误{}", e.getMessage());
        }
        return data;
    }

    /**
     * APP-异常pi
     */
    private String getPiExcCount(List<String> lineList, String finalStartTime, String finalEndTime) {
        long count = 0, size = 0;
//        for (MdmLine line : lineList) {
//            Integer reportType = line.getReportType();
//            //pi
//            IPiBaseExecuteService choose = abstractStrategyChoose.choose(reportType + "", IPiBaseExecuteService.class);
//            PiDataSearchFilter filter = new PiDataSearchFilter();
//            filter.setLineId(line.getId());
//            filter.setStartDate(finalStartTime);
//            filter.setEndDate(finalEndTime);
//            List<com.alibaba.fastjson.JSONObject> excTaskList = choose.findExcTaskList(filter);
//            count += excTaskList.stream().filter(this::filterData).count();
//            size += excTaskList.size();
//        }
        return size - count + "/" + size;
    }
    /**
     * 异常KPI
     */
    private String getKpiExcCount(String orgCode, String finalStartTime, String finalEndTime) {

        KpiExceptionSummary kpiExceptionSummary = new KpiExceptionSummary();
        kpiExceptionSummary.setHierarchy(orgCode);
        kpiExceptionSummary.setStartTime(finalStartTime);
        kpiExceptionSummary.setEndTime(finalEndTime);
        kpiExceptionSummary.setIsAllType(true);
        ExcPercentVo percent = kpiExceptionSummaryService.percent(kpiExceptionSummary);
        return percent.getExcCount() + "/" + percent.getExcSum();
    }

    /**
     * RA单统计
     */
    private String getRaCount(LoginUser user,String finalStartTime, String finalEndTime) {
        EmpRaFromDto empRaFromDto = new EmpRaFromDto();
        empRaFromDto.setUserId(user.getId());
        empRaFromDto.setFactory(user.getFactoryCode());
        empRaFromDto.setEddTime(finalEndTime);
        empRaFromDto.setStateTime(finalStartTime);
        Map<String, Object> map = actionPlanTrackService.empRaFrom(empRaFromDto);
        EmpRaFromVo dut = (EmpRaFromVo) map.get("dut");
        int ok = dut.getActionOk() + dut.getDeferOkCount();
        return ok + "/" + (dut.getTracks() == null ? 0 : dut.getTracks().size());
    }

    /**
     * App-异常清单(PI)
     * @param dto
     * @return
     */
    @Override
    public IPage<PiAbnormalAnalysis> findExcTaskList(Page<PiAbnormalAnalysis> page, PiAbnormalAnalysis dto, HttpServletRequest req) {
        // 获取对应产线的PI报表
        List<PiCustomizeTable> tableList = piCustomizeTableService.list(new LambdaQueryWrapper<PiCustomizeTable>()
                .eq(PiCustomizeTable::getLineCode, dto.getLineCode()));
        List<String> tables = tableList.stream()
                .map(PiCustomizeTable::getTableName)  // 等同于 .map(table -> table.getId())
                .collect(Collectors.toList());

        if(tables != null && tables.size() == 0){
            tables.add("-1");
        }
        //异常分析
        dto.setLineCode("");
        QueryWrapper<PiAbnormalAnalysis> wrapper = QueryGenerator.initQueryWrapper(dto, req.getParameterMap());
        wrapper.in("table_code", tables);
        wrapper.ge("create_time", dto.getOpTime());
        wrapper.le("create_time", dto.getEdTime());
        wrapper.orderByDesc("create_time");
        IPage<PiAbnormalAnalysis> pageList = piAbnormalAnalysisService.page(page, wrapper);
        List<PiAbnormalAnalysis> analysisList = pageList.getRecords();

        //获取人员
        List<SysUser> userList = iSysUserService.list();
        Map<String, String> userMap = CollStreamUtil.toMap(userList, SysUser::getUsername, bean -> bean.getRealname());
        //问题类别
        List<ActionPlanProblemType> problemType = actionPlanProblemTypeService.list();
        Map<String, String> problemTypeMap = CollStreamUtil.toMap(problemType, ActionPlanProblemType::getId, bean -> bean.getProblemName());


//        List<ExceptionVo> list = new ArrayList<>();
        if(analysisList != null && !analysisList.isEmpty()){
            //获取id集合
            List<String> idList = analysisList.stream()
                    .map(PiAbnormalAnalysis::getId)
                    .collect(Collectors.toList());
            //获取评价
            LambdaQueryWrapper<PiAbnormalAnalysisEvaluation> queryWrapper = Wrappers.lambdaQuery(PiAbnormalAnalysisEvaluation.class);
            queryWrapper.in(PiAbnormalAnalysisEvaluation::getRecordId,idList);
            List<PiAbnormalAnalysisEvaluation> evaluationList = piAbnormalAnalysisEvaluationService.list(queryWrapper);

            //获取 评价 异常id_评价类型
            Map<String, PiAbnormalAnalysisEvaluation> evaluationMap = evaluationList.stream()
                    .collect(Collectors.toMap(
                            evaluation -> evaluation.getRecordId() + "_" + evaluation.getEvaluationType(), // Key 生成规则
                            evaluation -> evaluation,  // Value 直接取对象本身
                            (oldValue, newValue) -> newValue  // 如果 key 冲突，保留新值（可选）
                    ));
            for (PiAbnormalAnalysis piAbnormalAnalysis : analysisList) {
                List<PiAbnormalAnalysisEvaluation> evaluations = new ArrayList<>();
                PiAbnormalAnalysisEvaluation analysisEvaluation = evaluationMap.get(piAbnormalAnalysis.getId()+"_1");
                if(analysisEvaluation != null){
                    //班组
                    evaluations.add(analysisEvaluation);
                }else{
                    evaluations.add(null);
                }

                PiAbnormalAnalysisEvaluation workshopAnalysisEvaluation = evaluationMap.get(piAbnormalAnalysis.getId()+"_2");
                if(workshopAnalysisEvaluation != null){
                    //车间
                    evaluations.add(workshopAnalysisEvaluation);
                }else{
                    evaluations.add(null);
                }
                if(evaluations != null && !evaluations.isEmpty()){
                    piAbnormalAnalysis.setEvaluations(evaluations);
                }

                //匹配人员
                piAbnormalAnalysis.setCreateBy(userMap.getOrDefault(piAbnormalAnalysis.getCreateBy(),""));
                //匹配问题类别
                piAbnormalAnalysis.setProblemType(problemTypeMap.getOrDefault(piAbnormalAnalysis.getProblemType(),""));
            }
        }
        return pageList;
    }


    /**
     * 获取PI报表
     * @param dto
     * @return
     */
    @Override
    public List<PiCustomizeTable> piTable(PiCustomizeTable dto) {
        List<PiCustomizeTable> list = piCustomizeTableService.lambdaQuery().eq(PiCustomizeTable::getLineCode, dto.getLineCode()).
                orderByAsc(PiCustomizeTable::getSort).list();
        return list;
    }

    /**
     * 获取PI
     * @param dto
     * @return
     */
    @Override
    public List<PiCustomizeTableField> piList(PiCustomizeTableField dto) {
        List<PiCustomizeTableField> list = piCustomizeTableFieldService.lambdaQuery().eq(PiCustomizeTableField::getTableId, dto.getTableId()).
                orderByAsc(PiCustomizeTableField::getSort).list();
        return list;
    }

}
