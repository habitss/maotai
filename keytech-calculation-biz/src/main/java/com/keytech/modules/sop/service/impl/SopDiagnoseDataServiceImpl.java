package com.keytech.modules.sop.service.impl;

import cn.hutool.core.collection.CollStreamUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.keytech.modules.action.plan.entity.ActionPlanTrack;
import com.keytech.modules.action.plan.service.IActionPlanTrackService;
import com.keytech.modules.sop.entity.SopDiagnoseData;
import com.keytech.modules.sop.entity.SopHomePage;
import com.keytech.modules.sop.entity.SopStatisticData;
import com.keytech.modules.sop.mapper.SopDiagnoseDataMapper;
import com.keytech.modules.sop.service.ISopDiagnoseDataService;
import com.keytech.modules.sop.service.ISopHomePageService;
import com.keytech.modules.sop.service.ISopStatisticDataService;
import org.apache.commons.lang3.StringUtils;
import org.jeecg.common.api.vo.Result;
import org.jeecg.modules.system.entity.SysDepart;
import org.jeecg.modules.system.entity.SysUser;
import org.jeecg.modules.system.service.ISysDepartService;
import org.jeecg.modules.system.service.ISysUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.keytech.base.service.impl.BaseServiceImpl;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @Description: SOP诊断计划-工作诊断
 * @Author: jeecg-boot
 * @Date:   2025-04-26
 * @Version: V1.0
 */
@Service
public class SopDiagnoseDataServiceImpl extends BaseServiceImpl<SopDiagnoseDataMapper, SopDiagnoseData> implements ISopDiagnoseDataService {

    @Autowired
    private ISysDepartService departService;
    @Autowired
    private ISysUserService userService;
    @Autowired
    private IActionPlanTrackService actionPlanTrackService;
    @Autowired
    private ISopDiagnoseDataService sopDiagnoseDataService;
    @Autowired
    private ISopStatisticDataService sopStatisticDataService;
    @Autowired
    private ISopHomePageService sopHomePageService;



    @Override
    public Result<?> diagnosticPostSave(SopDiagnoseData sopDiagnoseData) {
        //行动计划
        List<ActionPlanTrack> track = sopDiagnoseData.getCDataList();
        //工作诊断保存
        saveOrUpdate(sopDiagnoseData);
        if (!CollUtil.isEmpty(track)) {
            //诊断表ID
            String id = sopDiagnoseData.getId();
            //行动保存
            actionPlanTrackService.saveOrUpdateAction(id, track);
        }
        //诊断计划
        LambdaQueryWrapper<SopStatisticData> queryWrapper = Wrappers.lambdaQuery(SopStatisticData.class);
        queryWrapper.eq(SopStatisticData::getId,sopDiagnoseData.getSopStatisticId());
        List<SopStatisticData> list = sopStatisticDataService.list(queryWrapper);
        if(list != null && !list.isEmpty()){
            SopStatisticData sopStatisticData = list.get(0);
            String resultData = sopStatisticData.getResultData();
            JSONObject s = new JSONObject();
            if(StringUtils.isBlank(resultData) || "{}".equals(resultData)){
                //完成
                s.put(sopDiagnoseData.getWeek(), 3);
            }else{
                s = JSON.parseObject(resultData);
                s.put(sopDiagnoseData.getWeek(), 3);

            }
            //更新诊断计划状态(完成)
            sopStatisticData.setResultData(s.toJSONString());
            sopStatisticDataService.updateById(sopStatisticData);
        }


        return Result.OK("添加成功！");
    }

    /**
     * 计算剩余天数
     *
     * @param date 当前日期
     * @param planFinish 计划完成日期
     * @return 剩余天数，计划完成日期为空时返回0，计划完成日期在当前日期之前时返回负数
     */
    private int calculateSurplusDay(DateTime date, Date planFinish) {
        if (Objects.isNull(planFinish)) {
            return 0;
        }
        int surplusDay = (int) DateUtil.betweenDay(date, planFinish, true);
        if (planFinish.before(date)) {
            surplusDay = -surplusDay;
        }
        return surplusDay;
    }

    /**
     * 设置部门ID。
     *
     * @param w            ActionPlanTrack对象
     * @param orgCodeDeptIdMap 组织代码与部门ID的映射关系
     * @param userMap        用户ID与用户信息的映射关系
     */
    private void setDepartmentIds(ActionPlanTrack w, Map<String, String> orgCodeDeptIdMap, Map<String, String> userMap) {
        //提出部门
        if (StrUtil.isNotBlank(w.getPutUserId()) && StrUtil.isEmpty(w.getPutDeptId())) {
            w.setPutDeptId(getDeptId(w.getPutUserId(), orgCodeDeptIdMap, userMap));
        }
        if (StrUtil.isNotBlank(w.getDutyUserId()) && StrUtil.isEmpty(w.getDutyDeptId())) {
            w.setDutyDeptId(getDeptId(w.getDutyUserId(), orgCodeDeptIdMap, userMap));
        }
        if (StrUtil.isNotBlank(w.getExecuteUserId()) && StrUtil.isEmpty(w.getExecuteDeptId())) {
            w.setExecuteDeptId(getDeptId(w.getExecuteUserId(), orgCodeDeptIdMap, userMap));
        }
        if (StrUtil.isNotEmpty(w.getPutUserId()) && StrUtil.isEmpty(w.getVerifyUserId())) {
            w.setVerifyUserId(w.getPutUserId());
            w.setFinishVerifyState("1");
        }
    }


    /**
     * 设置行动级别
     *
     * @param w 行动计划跟踪对象
     */
    private void setActionLevel(ActionPlanTrack w) {
        String actionLevel = w.getActionLevel();
        String putDept = w.getPutDeptId();
        String dutDept = w.getDutyDeptId();
        if (actionLevel == null) {
            if (StrUtil.isEmpty(putDept) || StrUtil.isEmpty(dutDept) || !putDept.equals(dutDept)) {
                w.setActionLevel("2");
            } else {
                w.setActionLevel("1");
            }
        }
    }


    /**
     * 根据用户ID获取部门ID
     *
     * @param userId 用户ID
     * @param orgCodeDeptIdMap 组织代码与部门ID的映射关系
     * @param userMap 用户ID与组织代码的映射关系
     * @return 部门ID，若未找到则返回null
     */
    private String getDeptId(String userId, Map<String, String> orgCodeDeptIdMap, Map<String, String> userMap) {
        String orgCode = userMap.getOrDefault(userId, null);
        if (StrUtil.isNotEmpty(orgCode)) {
            return orgCodeDeptIdMap.getOrDefault(orgCode, null);
        }
        return null;
    }

    /**
     * 获取诊断
     * @param sopStatisticId
     * @param week
     * @param year
     * @return
     */
    @Override
    public Result<?> getDiagnosticPost(String sopStatisticId, String week, String year) {
        SopDiagnoseData sopDiagnoseData = sopDiagnoseDataService.getOne(new LambdaQueryWrapper<SopDiagnoseData>()
                .eq(SopDiagnoseData::getSopStatisticId,sopStatisticId).eq(SopDiagnoseData::getWeek,week).eq(SopDiagnoseData::getYear,year));
        if(sopDiagnoseData==null) {
            return Result.OK(new SopDiagnoseData());
        }
        String cData = sopDiagnoseData.getCData();
        if (StrUtil.isNotEmpty(cData)){
            List<ActionPlanTrack> list = actionPlanTrackService.list(new LambdaQueryWrapper<ActionPlanTrack>()
                    .in(ActionPlanTrack::getId,Arrays.stream(cData.split(","))
                            .collect(Collectors.toList())));
            sopDiagnoseData.setCDataList(list);
        }
        return Result.OK(sopDiagnoseData);
    }

}
