package com.keytech.modules.project.management.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollStreamUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.keytech.base.service.impl.BaseServiceImpl;
import com.keytech.modules.common.GetDeptChildUtil;
import com.keytech.modules.mdm3.entity.MdmDict;
import com.keytech.modules.mdm3.entity.MdmWeek;
import com.keytech.modules.mdm3.service.IMdmDictService;
import com.keytech.modules.mdm3.service.IMdmWeekService;
import com.keytech.modules.project.management.dto.ProjectManagementSummaryDto;
import com.keytech.modules.project.management.entity.ProjectManagement;
import com.keytech.modules.project.management.entity.ProjectManagementAction;
import com.keytech.modules.project.management.entity.ProjectManagementActionSummary;
import com.keytech.modules.project.management.entity.ProjectManagementNode;
import com.keytech.modules.project.management.mapper.ProjectManagementActionMapper;
import com.keytech.modules.project.management.mapper.ProjectManagementMapper;
import com.keytech.modules.project.management.service.IProjectManagementActionService;
import com.keytech.modules.project.management.service.IProjectManagementNodeService;
import com.keytech.modules.project.management.service.IProjectManagementService;
import jakarta.servlet.http.HttpServletRequest;
import org.jeecg.common.system.query.QueryGenerator;
import org.jeecg.modules.system.entity.SysDepart;
import org.jeecg.modules.system.entity.SysUser;
import org.jeecg.modules.system.service.ISysDepartService;
import org.jeecg.modules.system.service.ISysUserService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @Description: 项目管理
 * @Author: jeecg-boot
 * @Date:   2025-03-28
 * @Version: V1.0
 */
@Service
public class ProjectManagementServiceImpl extends BaseServiceImpl<ProjectManagementMapper, ProjectManagement> implements IProjectManagementService {
    final List<String> monthList = Arrays.asList( "1", "2", "3","4", "5", "6","7", "8", "9","10", "11", "12");

    @Autowired
    private IProjectManagementActionService projectManagementActionService;
    @Autowired
    private IProjectManagementNodeService projectManagementNodeService;
    @Autowired
    private ProjectManagementActionMapper actionMapper;
    @Autowired
    private IMdmWeekService mdmWeekService;
    @Autowired
    private IMdmDictService mdmDictService;
    @Autowired
    private ISysUserService sysUserService;
    @Autowired
    private ISysDepartService sysDepartService;
    @Autowired
    private GetDeptChildUtil getDeptChildUtil;
    @Override
    public List<ProjectManagement> queryAll(ProjectManagement projectManagement, HttpServletRequest req) {
        //返回数据
        List<ProjectManagement> resultList = new ArrayList<>();
        //周维护数据
        List<MdmWeek> weekList = getWeekList(projectManagement.getYear(), projectManagement.getMonths());

        QueryWrapper<ProjectManagement> queryWrapper = QueryGenerator.initQueryWrapper(projectManagement, req.getParameterMap());
        queryWrapper.likeRight(null != projectManagement.getYear(), "create_time", projectManagement.getYear());
        if(StrUtil.isNotBlank(projectManagement.getLeaderDeptIds())){
            queryWrapper.exists("select 1 from mes3_project_management_action action where action.project_id = mes3_project_management.id " +
                    " and action.del_flag = 0 " +
                    " and action.leader_dept_id in("+getDeptChildUtil.getConversionToSqlStr(projectManagement.getLeaderDeptIds())+")"
            );
        }
        //按条件获取所有数据
        List<ProjectManagement> list = this.list(queryWrapper);
        //获取节点、行动统计数据
        List<ProjectManagementActionSummary> actionSummary = actionMapper.querySummaryData(projectManagement);
        Map<String, ProjectManagementActionSummary> projectMap = actionSummary.stream()
                .collect(Collectors.toMap(
                        ProjectManagementActionSummary::getProjectId,
                        project -> project
                ));
        //月计划值
        Map<String, ProjectManagementActionSummary> planMonthMap = getMonthSummary(projectManagement.getYear(), "plan_completion_time");
        //月实际值
        Map<String, ProjectManagementActionSummary> realMonthMap = getMonthSummary(projectManagement.getYear(), "real_completion_time");
        //周计划值
        Map<String, ProjectManagementActionSummary> planMap = getWeekSummary(projectManagement.getYear(), projectManagement.getMonths(), "plan_completion_time");
        //周实际值
        Map<String, ProjectManagementActionSummary> realMap = getWeekSummary(projectManagement.getYear(), projectManagement.getMonths(), "real_completion_time");

        //将统计数据和项目数据集合
        for(ProjectManagement project : list){
            ProjectManagementActionSummary summary = new ProjectManagementActionSummary();
            if(projectMap.containsKey(project.getId())){
                summary = projectMap.get(project.getId());
            }
            //项目统计数据
            project.setSummary(summary);
            //项目月累、周累计划值数据
            project.setWeekSummary(calcWeekSummaryData(project, planMap, weekList, "目标值", planMonthMap));
            resultList.add(project);

            //复制一份实际值数据
            ProjectManagement realProject = new ProjectManagement();
            BeanUtils.copyProperties(project, realProject);
            //项目月累、周累实际值数据
            realProject.setWeekSummary(calcWeekSummaryData(project, realMap, weekList, "实际值", realMonthMap));
            resultList.add(realProject);
        }

        return resultList;
    }

    @Override
    @Transactional
    public void removeProjectByIds(String projectIds) {
        List<String> ids = Arrays.asList(projectIds.split(","));
        this.removeByIds(ids);
        projectManagementNodeService.remove(
                new LambdaUpdateWrapper<ProjectManagementNode>().in(ProjectManagementNode::getProjectId, ids)
        );
        projectManagementActionService.remove(
                new LambdaUpdateWrapper<ProjectManagementAction>().in(ProjectManagementAction::getProjectId, ids)
        );
    }

    @Override
    public List<ProjectManagementSummaryDto> getDepartmentalProjectSummary(ProjectManagement projectManagement) {
        //项目等级
        if(StrUtil.isNotBlank(projectManagement.getProjectLevel())){
            List<String> projectLevelList = Arrays.stream(projectManagement.getProjectLevel().split(","))
                    .collect(Collectors.toList());
            projectManagement.setProjectLevelList(projectLevelList);
        }
        //项目类型
        if(StrUtil.isNotBlank(projectManagement.getProjectType())){
            List<String> projectTypeList = Arrays.stream(projectManagement.getProjectType().split(","))
                    .collect(Collectors.toList());
            projectManagement.setProjectTypelList(projectTypeList);
        }

        //项目来源
        if(StrUtil.isNotBlank(projectManagement.getProjectSource())){
            List<String> projectSourceList = Arrays.stream(projectManagement.getProjectSource().split(","))
                    .collect(Collectors.toList());
            projectManagement.setProjectSourceList(projectSourceList);
        }
        projectManagement.setMonthList(monthList);
        List<ProjectManagementSummaryDto> summaryDtoList = baseMapper.getDepartmentalProjectSummary(projectManagement);
        getSummaryObj(summaryDtoList);
        //获取翻译字典
        getDict(summaryDtoList);
        return summaryDtoList;
    }

    @Override
    public List<ProjectManagementSummaryDto> getPersonalProjectSummary(ProjectManagement projectManagement) {
        //项目数据
        if(StrUtil.isNotBlank(projectManagement.getProjectLevel())){
            List<String> projectLevelList = Arrays.stream(projectManagement.getProjectLevel().split(","))
                    .collect(Collectors.toList());
            projectManagement.setProjectLevelList(projectLevelList);
        }

        //项目类型
        if(StrUtil.isNotBlank(projectManagement.getProjectType())){
            List<String> projectTypeList = Arrays.stream(projectManagement.getProjectType().split(","))
                    .collect(Collectors.toList());
            projectManagement.setProjectTypelList(projectTypeList);
        }

        //项目来源
        if(StrUtil.isNotBlank(projectManagement.getProjectSource())){
            List<String> projectSourceList = Arrays.stream(projectManagement.getProjectSource().split(","))
                    .collect(Collectors.toList());
            projectManagement.setProjectSourceList(projectSourceList);
        }
        List<ProjectManagement> projectManagementList = baseMapper.queryAll(projectManagement);
        // 抽取id
        String projectIds = projectManagementList.stream()
                .map(ProjectManagement::getId)
                .collect(Collectors.joining(","));
        if(StrUtil.isBlank(projectIds)){
            return new ArrayList<>();
        }
        //抽取项目
        Map<String, ProjectManagement> projectMap = CollStreamUtil.toMap(projectManagementList, ProjectManagement::getId, bean -> bean);
        //获取统计数据
        List<ProjectManagementSummaryDto> dtoList = baseMapper.getPersonalProjectSummary(projectIds, projectManagement.getUserId(), projectManagement.getYear(), monthList, projectManagement.getIsFinishNode());

        getSummaryObj(dtoList);
        //组合项目数据与统计数据
        for(ProjectManagementSummaryDto dto : dtoList){
            ProjectManagement p = projectMap.get(dto.getId());
            BeanUtil.copyProperties(p, dto);
        }
        //获取翻译字典
        getDict(dtoList);
        return dtoList;
    }

    /**
     * 获取周维护数据
     * @param year 年份
     * @param months 月份
     * @return 周数据
     */
    private List<MdmWeek> getWeekList(String year, String months){
        //封装查询对象
        MdmWeek mdmWeek = new MdmWeek();
        mdmWeek.setYear(year);
        mdmWeek.setMonths(Arrays.stream(months.split(","))
                .map(String::trim)
                .map(Integer::parseInt)
                .collect(Collectors.toList()));
        return mdmWeekService.getWeek(mdmWeek);
    }

    /**
     * 获取月统计数据
     * @param year 年份
     * @param column 查询字段
     * @return 统计数据
     */
    private Map<String, ProjectManagementActionSummary> getMonthSummary(String year, String column){
        //获取月统计数据
        List<ProjectManagementActionSummary> monthSummaryList = actionMapper.queryMonthSummaryData(year, column);
        return monthSummaryList.stream()
                .collect(Collectors.toMap(
                        data -> data.getProjectId()+"-"+data.getMonth(),
                        data -> data
                ));
    }

    /**
     * 获取周统计数据
     * @param year 月份
     * @param months 年份
     * @param column 查询字段
     * @return 统计数据
     */
    private Map<String, ProjectManagementActionSummary> getWeekSummary(String year, String months, String column){
        //将月份拆分成list
        List<String> monthList = Arrays.stream(months.split(","))
                .collect(Collectors.toList());
        //获取周统计数据
        List<ProjectManagementActionSummary> weekSummaryList = actionMapper.queryWeekSummaryData(year, monthList, column);
        return weekSummaryList.stream()
                .collect(Collectors.toMap(
                        data -> data.getProjectId()+"-"+data.getWeekNumber(),
                        data -> data
                ));
    }

    /**
     * 处理周数据
     * @param project 项目对象
     * @param summaryMap 周统计对象
     * @param weekList 周维护数据
     * @return 处理后数据
     */
    private JSONObject calcWeekSummaryData(ProjectManagement project, Map<String, ProjectManagementActionSummary> summaryMap, List<MdmWeek> weekList, String weekType, Map<String, ProjectManagementActionSummary> monthMap){
        JSONObject jsonObject = new JSONObject();
        jsonObject.set("weekType", weekType);
        //循环给项目对象添加周统计数据
        for(MdmWeek week : weekList){
            //组装从map取数据的key
            String key = project.getId()+"-"+week.getWeekNumber();
            //周数据
            int weekTotal = 0;
            if(summaryMap.containsKey(key)){
                weekTotal = summaryMap.get(key).getWeekTotal();
            }

            //json的月份key
            String monKey = week.getYear()+"-"+week.getMonth();
            if(null == jsonObject.getInt(monKey) && monthMap.containsKey(project.getId()+"-"+monKey)){
                jsonObject.set(monKey, monthMap.get(project.getId()+"-"+monKey).getMonthTotal());
            }
            jsonObject.set(String.valueOf(week.getWeekNumber()), weekTotal);
        }
        return jsonObject;
    }

    private void getDict(List<ProjectManagementSummaryDto> dtoList){
        //获取项目等级字典、项目类型字典
        List<MdmDict> mdmDictList = mdmDictService.getDictAllOrByGroupCode(
            List.of(
                    "MES3_PROJECT_LEVEL"
                    , "MES3_PROJECT_TYPE"
                    , "MES3_PROJECT_SOURCE"
            )
        );
        //字典数据map集合，key为groupCode-itemVal,value为对象信息
        Map<String, String> dictMap = mdmDictList.stream()
            .collect(Collectors.toMap(
                    item -> item.getGroupCode() + "-" + item.getItemVal(),
                    MdmDict::getItemKey
            ));
        //所有用户信息
        List<SysUser> allUserList = sysUserService.list();
        Map<String, String> allUserMap = CollStreamUtil.toMap(allUserList, SysUser::getId, SysUser::getRealname);
        //所有单位信息
        List<SysDepart> allDeptList = sysDepartService.list();
        Map<String, String> allDeptMap = CollStreamUtil.toMap(allDeptList, SysDepart::getId, SysDepart::getDepartName);
        for(ProjectManagementSummaryDto dto : dtoList){
            dto.setProjectLevel_dictText(getDictItem("MES3_PROJECT_LEVEL-"+dto.getProjectLevel(), dictMap));
            dto.setProjectType_dictText(getDictItem("MES3_PROJECT_TYPE-"+dto.getProjectType(), dictMap));
            dto.setProjectSource_dictText(getDictItem("MES3_PROJECT_SOURCE-"+dto.getProjectSource(), dictMap));
            dto.setLeaderUserId_dictText(getDictItem(dto.getLeaderUserId(), allUserMap));
            dto.setSysOrgId_dictText(getDictItem(dto.getSysOrgId(), allDeptMap));
        }
    }

    private String getDictItem(String key, Map<?, ?> dictMap){
        if(StrUtil.isBlank(key) || !dictMap.containsKey(key)){
            return "";
        }
        return MapUtil.getStr(dictMap, key);
    }

    /**
     * 获取汇总对象
     * @param summaryDtoList 数据集合
     */
    private void getSummaryObj(List<ProjectManagementSummaryDto> summaryDtoList) {
        ProjectManagementSummaryDto summary = new ProjectManagementSummaryDto();
        summary.setProjectCode("汇总");
        summary.setProjectName("汇总");

        double sum1 = 0, sum2 = 0, sum3 = 0, sum4 = 0, sum5 = 0, sum6 = 0,
                sum7 = 0, sum8 = 0, sum9 = 0, sum10 = 0, sum11 = 0, sum12 = 0;

        for (ProjectManagementSummaryDto dto : summaryDtoList) {
            if (dto.getMonth1() != null) sum1 += dto.getMonth1();
            if (dto.getMonth2() != null) sum2 += dto.getMonth2();
            if (dto.getMonth3() != null) sum3 += dto.getMonth3();
            if (dto.getMonth4() != null) sum4 += dto.getMonth4();
            if (dto.getMonth5() != null) sum5 += dto.getMonth5();
            if (dto.getMonth6() != null) sum6 += dto.getMonth6();
            if (dto.getMonth7() != null) sum7 += dto.getMonth7();
            if (dto.getMonth8() != null) sum8 += dto.getMonth8();
            if (dto.getMonth9() != null) sum9 += dto.getMonth9();
            if (dto.getMonth10() != null) sum10 += dto.getMonth10();
            if (dto.getMonth11() != null) sum11 += dto.getMonth11();
            if (dto.getMonth12() != null) sum12 += dto.getMonth12();
        }

        summary.setMonth1(roundNumber(sum1));
        summary.setMonth2(roundNumber(sum2));
        summary.setMonth3(roundNumber(sum3));
        summary.setMonth4(roundNumber(sum4));
        summary.setMonth5(roundNumber(sum5));
        summary.setMonth6(roundNumber(sum6));
        summary.setMonth7(roundNumber(sum7));
        summary.setMonth8(roundNumber(sum8));
        summary.setMonth9(roundNumber(sum9));
        summary.setMonth10(roundNumber(sum10));
        summary.setMonth11(roundNumber(sum11));
        summary.setMonth12(roundNumber(sum12));

        summaryDtoList.add(summary);
    }

    private double roundNumber(double value) {
        BigDecimal result = new BigDecimal(value).setScale(2, RoundingMode.HALF_UP);
        return result.doubleValue();
    }
}
