package com.keytech.modules.lims.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.exceptions.StatefulException;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.keytech.modules.action.plan.entity.ActionPlanTrack;
import com.keytech.modules.action.plan.service.IActionPlanTrackService;
import com.keytech.modules.fivewhy.entity.FiveWhyAnalyse;
import com.keytech.modules.fivewhy.service.IFiveWhyAnalyseService;
import com.keytech.modules.kpi.util.DateUtils;
import com.keytech.modules.lims.dto.LimsAnalysisDto;
import com.keytech.modules.lims.dto.LimsMoveDto;
import com.keytech.modules.lims.dto.LimsResultDto;
import com.keytech.modules.lims.dto.LimsResultsAbnormalDto;
import com.keytech.modules.lims.entity.LimsResultsAbnormal;
import com.keytech.modules.lims.mapper.LimsResultsAbnormalMapper;
import com.keytech.modules.lims.service.ILimsResultsAbnormalService;
import com.keytech.modules.lims.util.LimsPush;
import com.keytech.modules.lims.util.LimsUrl;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.shiro.SecurityUtils;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.system.query.QueryGenerator;
import org.jeecg.common.system.vo.LoginUser;
import org.jeecg.modules.system.entity.SysDepart;
import org.jeecg.modules.system.entity.SysUser;
import org.jeecg.modules.system.service.ISysDepartService;
import org.jeecg.modules.system.service.ISysUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.keytech.base.service.impl.BaseServiceImpl;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * @Description: 超标不合格台账(LIMS)
 * @Author: jeecg-boot
 * @Date:   2025-03-17
 * @Version: V1.0
 */
@Service
public class LimsResultsAbnormalServiceImpl extends BaseServiceImpl<LimsResultsAbnormalMapper, LimsResultsAbnormal> implements ILimsResultsAbnormalService {

    @Autowired
    private IFiveWhyAnalyseService fiveWhyAnalyseService;
    @Autowired
    private ISysDepartService sysDepartService;
    @Autowired
    private ISysUserService sysUserService;
    @Autowired
    private IActionPlanTrackService actionPlanTrackService;

    private String limsUrl;
    private String limsPassword;

    /**
     * 更新5why
     * @param limsResultsAbnormal
     */
    @Override
    public void updateFwhy(LimsResultsAbnormal limsResultsAbnormal) {
        //分析完成时间
        if(limsResultsAbnormal.getAnalysisTime() == null){
            limsResultsAbnormal.setAnalysisTime(new Date());
        }
        //不通过改成通过/重新提交审批
        if(Objects.equals(2, limsResultsAbnormal.getExamineStatus())){
            limsResultsAbnormal.setExamineStatus(0);
        }
        updateById(limsResultsAbnormal);
    }


    /**
     * 审批
     * @param limsResultsAbnormal
     * @return
     */
    @Override
    public Result approval(LimsResultsAbnormal limsResultsAbnormal) {
        //Result result = new Result<>();
        //审批时间
        limsResultsAbnormal.setMgrapprovalTime(new Date());
        //获取当前用户
        LoginUser sysUser = SecurityUtils.getSubject().getPrincipal() != null ? (LoginUser) SecurityUtils.getSubject().getPrincipal() : null;
        if(sysUser == null){
            return Result.error("无法获取用户信息");
        }
        //审批人
//        limsResultsAbnormal.setApprovalCode(sysUser.getUsername());
        limsResultsAbnormal.setApprovalCode(sysUser.getId());
        if(1 == limsResultsAbnormal.getExamineStatus()){
            LimsResultsAbnormal limsResultsAbnormalNew = getById(limsResultsAbnormal.getId());
            //审批时间
            limsResultsAbnormalNew.setMgrapprovalTime(limsResultsAbnormal.getMgrapprovalTime());

            //推送车间分析排查结果 5w分析
            LimsResultDto resultDto = push5wToLims(limsResultsAbnormalNew);
            if("1".equals(resultDto.getCode())){
                //推送行动计划 行动分析
                resultDto = pushActionToLims(limsResultsAbnormalNew);
                if(!"1".equals(resultDto.getCode())){
                    //推送失败
                    return Result.error(resultDto.getMsg());
                }
            }else{
                //推送失败
                return Result.error(resultDto.getMsg());
            }
        }
        //变更审批状态
        updateById(limsResultsAbnormal);
        return Result.ok("审核成功");
    }

    /**
     * 5why分析
     * @param limsResultsAbnormal
     * @return
     */
    public LimsResultDto push5wToLims(LimsResultsAbnormal limsResultsAbnormal) {
        if(StrUtil.isBlank(limsResultsAbnormal.getLimsFlowId())){
            return  LimsResultDto.error("2001","LIMS唯一表示为空,LIMS数据推送异常");
        }else if(StrUtil.isBlank(limsResultsAbnormal.getReason())){
            return  LimsResultDto.error("2002","根本原因为空,请先进行否5why分析");
        }
        //推送地址
        String url = limsUrl+ LimsUrl.INVESTIGATIONURL;

        LimsAnalysisDto limsAnalysisDto = new LimsAnalysisDto();
        //limsId
        limsAnalysisDto.setLIMSFLOWID(limsResultsAbnormal.getLimsFlowId());
        //根本原因
        limsAnalysisDto.setREASON(limsResultsAbnormal.getReason());
        //时间格式 yyyy-dd-mm
        //[最新]时间格式 yyyy-dd-mm HH:mm:ss
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        limsAnalysisDto.setMGRAPPROVALTIME(sdf.format(limsResultsAbnormal.getMgrapprovalTime()));
        //分析时间
        limsAnalysisDto.setANALYSISTIME(sdf.format(limsResultsAbnormal.getAnalysisTime()));
        //推送
        LimsResultDto limsResult  = LimsPush.sendPost(url, JSONUtil.toJsonStr(limsAnalysisDto), Map.of(LimsPush.HEADER_PASSWORD, limsPassword));

        if("0".equals(limsResult.getCode())){
            limsResult.setMsg("LIMS接口返回:"+limsResult.getMsg());
        }
        return limsResult;
    }


    /**
     * 推送行动计划 行动分析
     * @param limsResultsAbnormal
     * @return
     */
    public LimsResultDto pushActionToLims(LimsResultsAbnormal limsResultsAbnormal) {

        try {
            //时间格式 yyyy-dd-mm
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            //时间格式 yyyy-dd-mm HH:mm:ss
            SimpleDateFormat sdfTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            LimsMoveDto limsMoveDto = new LimsMoveDto();

            //获取行动计划
            LambdaQueryWrapper<FiveWhyAnalyse> queryWrapper = new LambdaQueryWrapper<FiveWhyAnalyse>();
            queryWrapper.eq(FiveWhyAnalyse::getId, limsResultsAbnormal.getAnalysisId());
            //FiveWhyAnalyse fiveWhyAnalyse = fiveWhyAnalyseService.getById(limsResultsAbnormal.getAnalysisId());
            //String downDataJson = fiveWhyAnalyse.getDownData();
            //json转对象
            //JSONArray array = JSONUtil.parseArray(downDataJson);

            //获取用户
            List<SysUser> userList = sysUserService.list();
            Map<String, SysUser> userMap = userList.stream()
                    .collect(Collectors.toMap(SysUser::getId, user -> user));
            //获取部门
            List<SysDepart> departList = sysDepartService.list();
            //id ==> 部门对象
            Map<String, SysDepart> departMap = departList.stream()
                    .collect(Collectors.toMap(SysDepart::getId, depart -> depart));
            //code ==> 部门对象
            Map<String, SysDepart> departCodeMap = departList.stream()
                    .collect(Collectors.toMap(SysDepart::getOrgCode, depart -> depart));


            //获取车间行动计划跟踪(行动计划跟踪)
            LambdaQueryWrapper<ActionPlanTrack> queryWrapperMove = new LambdaQueryWrapper<ActionPlanTrack>();
            //id+"-limsCode-问题类型(1纠正性/2预防性)-1"
            //5whyid
//            queryWrapperMove.like(ActionPlanTrack::getDataId, limsResultsAbnormal.getId()+"-limsCode");
            queryWrapperMove.like(ActionPlanTrack::getDataId, limsResultsAbnormal.getAnalysisId());
            List<ActionPlanTrack> listTrack = actionPlanTrackService.list(queryWrapperMove);
            if(null != listTrack && listTrack.size() > 0){
                for (ActionPlanTrack workshopActionTrack:listTrack) {
                    //LIMS行动编号
                    limsMoveDto.setACTIONNO(workshopActionTrack.getId()+"");
                    limsMoveDto.setSOURCETYPE(""); //任务来源类型
                    //任务来源编号(行动id+LIMSFLOWID)
                    limsMoveDto.setSOURCENO(workshopActionTrack.getId()+limsResultsAbnormal.getLimsFlowId());

                    //任务来源标题(SOURCETITLE)[传空]
                    //任务内容[行动]
                    limsMoveDto.setCONTENT(workshopActionTrack.getActionTitle());
                    //计划完成时间(yyyy-MM-dd)
                    if(null != workshopActionTrack.getPlanFinishDate()) {
                        limsMoveDto.setPLANFINISHTIME(sdf.format(workshopActionTrack.getPlanFinishDate()));
                    }

                    if(null != workshopActionTrack.getExecuteDeptId() && StrUtil.isNotBlank(workshopActionTrack.getExecuteDeptId())){
                        String executeDeptId = workshopActionTrack.getExecuteDeptId();
                        String orgCode = departMap.get(executeDeptId).getOrgCode();
                        orgCode = orgCode.substring(0,12);
                        executeDeptId = departCodeMap.get(orgCode).getId();
                        //执行部门 (需要转换) ***
                        limsMoveDto.setCHARGEORG(getDeptNameByDeptName(executeDeptId, departMap));
                        //执行部门id [必填] 执行部门
                        limsMoveDto.setCHARGEORGID(getDeptCodeByDeptName(executeDeptId, departMap));
                    }

                    //负责人 CHARGEPERSON ***
                    if(StrUtil.isNotBlank(workshopActionTrack.getDutyUserId())){
                        //执行人
                        limsMoveDto.setCHARGEPERSON(getUserRealNameByUserId(workshopActionTrack.getDutyUserId(),userMap));

                    }
                    //负责人ID CHARGEPERSONID
                    if(StrUtil.isNotBlank(workshopActionTrack.getDutyUserId())){
                        //执行人
//                        limsMoveDto.setCHARGEPERSONID(userMap.get(workshopActionTrack.getExecuteUserId()).getUsername());
                        //负责人
                        limsMoveDto.setCHARGEPERSONID(getUserNameByUserId(workshopActionTrack.getDutyUserId(),userMap));
                    }
                    //任务完成情况(PLANFINISHDETAIL)[传个/或者已完成]
                    limsMoveDto.setPLANFINISHDETAIL("/");

                    //实际完成时间
                    if(null != workshopActionTrack.getPracticalFinishDate()){
                        limsMoveDto.setFINISHTIME(sdf.format(workshopActionTrack.getPracticalFinishDate()));
                    }
                    //完成情况验证 verifyDescription
                    String verifyDescription = workshopActionTrack.getFinishVerifyState();
                    // 1待确认 2合格 3不合格
                    if("1".equals(verifyDescription)){
                        limsMoveDto.setDETAILCHECK("待确认");
                    }else if("2".equals(verifyDescription)){
                        limsMoveDto.setDETAILCHECK("合格");
                    }else{
                        limsMoveDto.setDETAILCHECK("不合格");
                    }

                    //确认人
                    limsMoveDto.setCHECKORG(getUserDept(workshopActionTrack.getVerifyUserId(),userMap,departCodeMap,"name"));
                    //
                    limsMoveDto.setCHECKORGID(getUserDept(workshopActionTrack.getVerifyUserId(),userMap,departCodeMap,"code"));


                    //确认人id
                    if(StrUtil.isNotBlank(workshopActionTrack.getVerifyUserId())){
                        limsMoveDto.setCHECKPERSONID(getUserNameByUserId(workshopActionTrack.getVerifyUserId(),userMap));
                    }
                    //确认人名称 ***
                    if(StrUtil.isNotBlank(workshopActionTrack.getVerifyUserId())){
                        limsMoveDto.setCHECKPERSON(getUserRealNameByUserId(workshopActionTrack.getVerifyUserId(),userMap));
                    }

                    //验证时间 CHECKTIME [传空]

                    //备注 REMARK
                    limsMoveDto.setREMARK(workshopActionTrack.getRemark());

                    //制单人编码 (提出人)
                    if(StrUtil.isNotBlank(workshopActionTrack.getPutUserId())){
                        limsMoveDto.setCREATEDBYID(getUserNameByUserId(workshopActionTrack.getPutUserId(),userMap));
                    }

                    //制单人名称 ***
                    if(StrUtil.isNotBlank(workshopActionTrack.getPutUserId())){
                        limsMoveDto.setCREATEDBYNAME(getUserRealNameByUserId(workshopActionTrack.getPutUserId(),userMap));
                    }
                    //制单时间 提出时间
                    if(workshopActionTrack.getRaiseDate() != null){
                        limsMoveDto.setCREATEDTIME(sdfTime.format(workshopActionTrack.getRaiseDate()));
                    }

                    //制单人单位编码 *** (传提出人归属工厂) #*****
                    limsMoveDto.setCREATEDBYORGID(limsResultsAbnormal.getFactoryCode());
                    //制单人单位名称 *** (传提出人归属工厂) #*****
                    limsMoveDto.setCREATEDBYORGNAME(limsResultsAbnormal.getLimsFactoryName());

                    //来源ID
                    limsMoveDto.setSOURCEID(limsResultsAbnormal.getLimsFlowId());
                    //来源 固定值 18 不合格管理
                    limsMoveDto.setACTIONSOURCE("18"); //问题分类
                    //问题分级 ***
                    String actionLevel = workshopActionTrack.getActionLevel();//行动级别
                    if("1".equals(actionLevel)){
                        limsMoveDto.setPROBLEMLEVEL("部门级");
                    }else if("2".equals(actionLevel)){
                        limsMoveDto.setPROBLEMLEVEL("工厂级");
                    }else{
                        limsMoveDto.setPROBLEMLEVEL("车间级");
                    }

                    //任务/问题描述
                    limsMoveDto.setPROBLEMDESC(workshopActionTrack.getProblemDescription());

                    if(StrUtil.isNotBlank(workshopActionTrack.getDutyDeptId())){
                        //责任部门 ***
                        limsMoveDto.setDIRECTORORGNAME(getDeptNameByDeptName(workshopActionTrack.getDutyDeptId(), departMap));
                        //责任部门ID
                        limsMoveDto.setDIRECTORORGID(getDeptCodeByDeptName(workshopActionTrack.getDutyDeptId(), departMap));
                    }
                    //责任人ID
                    if(StrUtil.isNotBlank(workshopActionTrack.getDutyUserId())){
                        limsMoveDto.setDIRECTORID(getUserNameByUserId(workshopActionTrack.getDutyUserId(),userMap));
                    }
                    //责任人 ***
                    if(StrUtil.isNotBlank(workshopActionTrack.getDutyUserId())){
                        limsMoveDto.setDIRECTORNAME(getUserRealNameByUserId(workshopActionTrack.getDutyUserId(),userMap));
                    }

                    if(StrUtil.isNotBlank(workshopActionTrack.getPutUserId())){
                        //提出人ID
                        limsMoveDto.setFOUNDERID(getUserNameByUserId(workshopActionTrack.getPutUserId(),userMap));
                        //提出人
                        limsMoveDto.setFOUNDERNAME(getUserRealNameByUserId(workshopActionTrack.getPutUserId(),userMap));
                    }
                    if(null != workshopActionTrack.getRaiseDate()){
                        //提出时间
                        limsMoveDto.setINITIALTIME(sdf.format(workshopActionTrack.getRaiseDate()));
                    }

                    //匹配延期
                    if(StrUtil.isNotBlank(workshopActionTrack.getOneText())){
                        //第一次延期备注
                        limsMoveDto.setFIRSTDELAYREMARK(workshopActionTrack.getOneText());
                    }
                    if(null != workshopActionTrack.getOneTime()){
                        //第一次延期期限
                        limsMoveDto.setFIRSTDELAYPERIOD(sdf.format(workshopActionTrack.getOneTime()));
                    }

                    if(StrUtil.isNotBlank(workshopActionTrack.getTwoText())){
                        //第二次延期备注
                        limsMoveDto.setSECONDDELAYREMARK(workshopActionTrack.getTwoText());
                    }
                    if(null != workshopActionTrack.getTwoTime()){
                        //第二次延期期限
                        limsMoveDto.setSECONDDELAYPERIOD(sdf.format(workshopActionTrack.getTwoTime()));
                    }

                    if(StrUtil.isNotBlank(workshopActionTrack.getThreeText())){
                        //第三次延期备注
                        limsMoveDto.setTHIRDDELAYREMARK(workshopActionTrack.getThreeText());
                    }
                    if(null != workshopActionTrack.getThreeTime()){
                        //第三次延期期限
                        limsMoveDto.setTHIRDDELAYPERIOD(sdf.format(workshopActionTrack.getThreeTime()));
                    }

                    //执行部门为空,使用责任部门
                    if(StrUtil.isBlank(limsMoveDto.getCHARGEORG())){
                        if(StrUtil.isNotBlank(workshopActionTrack.getDutyDeptId())){
                            String dutyDeptId= workshopActionTrack.getDutyDeptId();
                            String orgCode = departMap.get(dutyDeptId).getOrgCode();
                            orgCode = orgCode.substring(0,12);
                            dutyDeptId = departCodeMap.get(orgCode).getId();
                            limsMoveDto.setCHARGEORG(getDeptNameByDeptName(dutyDeptId, departMap));
                        }
                    }
                    //执行部门id为空,使用责任部门
                    if(StrUtil.isBlank(limsMoveDto.getCHARGEORGID())){
                        if(StrUtil.isNotBlank(workshopActionTrack.getDutyDeptId())) {
                            String dutyDeptId= workshopActionTrack.getDutyDeptId();
                            String orgCode = departMap.get(dutyDeptId).getOrgCode();
                            orgCode = orgCode.substring(0,12);
                            dutyDeptId = departCodeMap.get(orgCode).getId();

                            limsMoveDto.setDIRECTORORGID(getDeptCodeByDeptName(dutyDeptId, departMap));
//                            limsMoveDto.setCHARGEORGID(limsMoveDto.getDIRECTORORGID());
                        }
                    }

                    //任务状态 0：待下发，1：待执行，2：待验证，3：已完成，4：已取消 (lims)
                    //完成情况 0：正在进行 1：已完成 2：延迟  4：延迟完成(系统)
                    // 1：已完成-->3：已完成 / 3：取消-->4：已取消 / 0：正在进行--> 0：待下发 / 2：延迟--> 0：待下发 / 4：延迟完成 -->3：已完成
                    String status = completionStatus(workshopActionTrack.getFinishState());
                    limsMoveDto.setSTATUS(status);

                    //判断行动类型 [1：预防，2：纠正]PLANTYPE  (行动措施 1预防/2纠正)
                    if(StrUtil.isNotBlank(workshopActionTrack.getActionMeasures())){
                        //String[] dataId = workshopActionTrack.getDataId().split("-");
//                        if(null != dataId && dataId.length == 4){
                        if("2".equals(workshopActionTrack.getActionMeasures())){
                            limsMoveDto.setPLANTYPE("纠正"); //1：预防，2：纠正
                        }else{
                            limsMoveDto.setPLANTYPE("预防"); //1：预防，2：纠正
                        }
//                        }
                    }
                    String url = limsUrl + LimsUrl.MOVEURL;
                    LimsResultDto limsResultDto = LimsPush.sendPost(url, JSONUtil.toJsonStr(limsMoveDto), Map.of(LimsPush.HEADER_PASSWORD, limsPassword));
                    if(!"1".equals(limsResultDto.getCode())){
                        //推送失败
                        if("CHARGEORG为空".equals(limsResultDto.getMsg())){
                            limsResultDto.setMsg("执行部门为空，请在记录行动中进行维护");
                        }
                        if("CHARGEORGID为空".equals(limsResultDto.getMsg())){
                            limsResultDto.setMsg("执行部门为空，请在记录行动中进行维护");
                        }
                        if("CHARGEPERSON为空".equals(limsResultDto.getMsg())){
                            limsResultDto.setMsg("负责人为空，请在记录行动中进行维护");
                        }
                        if("FINISHTIME为空".equals(limsResultDto.getMsg())){
                            limsResultDto.setMsg("实际完成时间为空，请在记录行动中进行维护");
                        }
                        if("PLANFINISHTIME为空".equals(limsResultDto.getMsg())){
                            limsResultDto.setMsg("计划完成时间为空，请在记录行动中进行维护");
                        }
                        if("PLANFINISHDETAIL为空".equals(limsResultDto.getMsg())){
                            limsResultDto.setMsg("任务完成情况为空，请在记录行动中进行维护");
                        }
                        if("PLANFINISHDETAIL为空".equals(limsResultDto.getMsg())){
                            limsResultDto.setMsg("任务完成情况为空，请在记录行动中进行维护");
                        }
                        if("PROBLEMDESC为空".equals(limsResultDto.getMsg())){
                            limsResultDto.setMsg("任务/问题描述为空");
                        }
                        if("CHECKPERSONID为空".equals(limsResultDto.getMsg())){
                            limsResultDto.setMsg("确认人为空，请在记录行动中进行维护");
                        }
                        if("CHECKPERSON为空".equals(limsResultDto.getMsg())){
                            limsResultDto.setMsg("确认人为空，请在记录行动中进行维护");
                        }
                        return LimsResultDto.error("3001",limsResultDto.getMsg());
                    }
                }
            }
        } catch (StatefulException e) {
            return LimsResultDto.error("3001","操作失败");
        }
        return LimsResultDto.success("1");
    }



    /**
     * 获取部门名称(LIMS部门名称)
     * @param deptName
     * @param departMap
     * @return
     */
    public String getDeptNameByDeptName(String deptName,Map<String, SysDepart> departMap){
        String name = "";
        if(StrUtil.isNotBlank(deptName)){
            SysDepart sysDepart = departMap.get(deptName);
            if(sysDepart != null){
                name = limsDeptName(sysDepart.getDepartName());
            }
        }
        return name;
    }

    /**
     * 获取部门(LIMS部门编码)
     * @param dept
     * @param departMap
     * @return
     */
    public String getDeptCodeByDeptName(String dept,Map<String, SysDepart> departMap){
        String code = "";
        if(StrUtil.isNotBlank(dept)){
            SysDepart sysDepart = departMap.get(dept);
            if(sysDepart != null){
                code = limsDeptCode(sysDepart.getDepartName());
            }
        }
        return code;
    }

    /**
     * 获取用户机构
     * @return
     */
    public String getUserDept(String userId, Map<String, SysUser> userMap,Map<String, SysDepart> departCodeMap,String type){
        String dept = "";
        if(StrUtil.isNotBlank(userId)){
            SysUser sysUser = userMap.get(userId);
            if (sysUser != null && StrUtil.isNotBlank(sysUser.getOrgCode())) {
                String departName = departCodeMap.get(sysUser.getOrgCode()).getDepartName();
                if("name".equals(type)){
                    dept = limsDeptName(departName);
                }else{
                    dept = limsDeptCode(departName);
                }

            }
        }
        return dept;
    }


    /**
     * 匹配lims名称(重新拼装"部",解决不带"部",导致与LIMS不匹配)
     * @param sysDeptName
     * @return
     */
    public String limsDeptName(String sysDeptName) {
        Pattern pattern = Pattern.compile("部" + "$");
        Matcher matcher = pattern.matcher(sysDeptName);
        if (matcher.find()) {
            sysDeptName = matcher.replaceFirst("");
        }
        return sysDeptName+"部";
    }

    /**
     * 匹配lims部门code
     * @param sysDeptName
     * @return
     */
    public String limsDeptCode(String sysDeptName) {
        sysDeptName = limsDeptName(sysDeptName);
        //BZ 包装部  ZLGL质量管理部  NZ 酿造部  QG企管部  DORS 党群人事部  AH 安环部  WL物流部  CW 财务部  JSFW 技术服务部
        switch (sysDeptName){
            case "包装部":
                return "BZ";
            case "质量管理部":
                return "ZLGL";
            case "酿造部":
                return "NZ";
            case "企管部":
                return "QG";
            case "党群人事部":
                return "DORS";
            case "安环部":
                return "AH";
            case "物流部":
                return "WL";
            case "运营部":
                return "WL";
            case "财务部":
                return "CWCW";
            case "技术服务部":
                return "JSFW";
            case "精酿部":
                return "JN";
            default:
                return "";
        }
//        return sysDeptName+"部";
    }

    /**
     * 完成情况对照
     */
    public String completionStatus(String State){
        String strCode = "";
        switch (State) {
            case "0":
                strCode = "0";
                break;
            case "1":
                strCode = "3";
                break;
            case "2":
                strCode = "0";
                break;
            case "3":
                strCode = "4";
                break;
            case "4":
                strCode = "3";
                break;
        }
        return strCode;
    }

    /**
     * 获取用户登陆名
     * @param userId
     * @param userMap
     * @return
     */
    public String getUserNameByUserId(String userId,  Map<String, SysUser> userMap){
        String name = "";
        if(StrUtil.isNotBlank(userId)){
            SysUser sysUser = userMap.get(userId);
            if(sysUser != null){
                name = sysUser.getUsername();
            }
        }
        return name;
    }
    /**
     * 获取用户真实名称
     */
    public String getUserRealNameByUserId(String userId, Map<String, SysUser> userMap){
        String name = "";
        if(StrUtil.isNotBlank(userId)){
            SysUser sysUser = userMap.get(userId);
            if(sysUser != null){
                name = sysUser.getRealname();
            }
        }
        return name;
    }


    /**
     * 接收LIMS推送的数据
     * @param limsResultsAbnormalDto
     */
    @Override
    public void abnormalSave(LimsResultsAbnormalDto limsResultsAbnormalDto) {
        //拷贝
        LimsResultsAbnormal limsResultsAbnormal = BeanUtil.copyProperties(limsResultsAbnormalDto, LimsResultsAbnormal.class);
        //limsResultsAbnormal.setLimsDept(limsResultsAbnormal.getLimsDept());//部门
        //判断LimsFlowId是否存在
        LambdaQueryWrapper<LimsResultsAbnormal> queryWrapper = new LambdaQueryWrapper<LimsResultsAbnormal>();
        queryWrapper.eq(LimsResultsAbnormal::getLimsFlowId, limsResultsAbnormalDto.getLimsFlowId());
        LimsResultsAbnormal one = getOne(queryWrapper, false);
        if (one != null) {
            //更新
//            limsResultsAbnormal.setDeptCode(one.getDeptCode());
            limsResultsAbnormal.setId(one.getId());
            updateById(limsResultsAbnormal);
        }else {
            //新增
            //获取前用用户(租户id/工厂Code)
            LoginUser loginUser = (LoginUser) SecurityUtils.getSubject().getPrincipal();
            //住户
            if(StrUtil.isNotBlank(loginUser.getRelTenantIds())){
                limsResultsAbnormal.setTenantId(Integer.parseInt(loginUser.getRelTenantIds()));
            }

            //匹配部门
            LambdaQueryWrapper<SysDepart> queryWrapperDept = new LambdaQueryWrapper<SysDepart>();
            queryWrapperDept.eq(SysDepart::getOrgType, "4"); //4部门
            List<SysDepart> listDept = sysDepartService.list(queryWrapperDept);
            //匹配部门
            for (SysDepart sysDept : listDept) {
                String sysDeptName = sysDept.getDepartName().replaceAll("部$", "");
                String limsDeptName = limsResultsAbnormal.getLimsDept().replaceAll("部$", "");
                if (sysDeptName.equals(limsDeptName)){
                    limsResultsAbnormal.setDeptCode(sysDept.getOrgCode());
                }
            }
            //工厂Code
            if(StrUtil.isBlank(loginUser.getFactoryCode()) && StrUtil.isNotBlank(limsResultsAbnormal.getDeptCode())){
                String deptCode = limsResultsAbnormal.getDeptCode();
                limsResultsAbnormal.setFactoryCode(deptCode.substring(0, 9));
            }
            save(limsResultsAbnormal);
        }
    }

    /**
     * 获取超标不合格数据
     * @param page
     * @param limsResults
     * @param req
     * @return
     */
    @Override
    public IPage<LimsResultsAbnormal> queryPageList(Page<LimsResultsAbnormal> page, LimsResultsAbnormal limsResults, HttpServletRequest req) {

        LambdaQueryWrapper<LimsResultsAbnormal> queryWrapper = new LambdaQueryWrapper<LimsResultsAbnormal>();
        queryWrapper.orderByDesc(LimsResultsAbnormal::getReportDate);
        queryWrapper.isNotNull(LimsResultsAbnormal::getLimsFlowId);
        //流水号
        queryWrapper.like(StrUtil.isNotBlank(limsResults.getLimsFlowId()),LimsResultsAbnormal::getLimsFlowId,limsResults.getLimsFlowId());
        //LIMS部门
        queryWrapper.like(StrUtil.isNotBlank(limsResults.getLimsDept()),LimsResultsAbnormal::getLimsDept,limsResults.getLimsDept());
        //LIMS产线
        queryWrapper.like(StrUtil.isNotBlank(limsResults.getDeviceName()),LimsResultsAbnormal::getDeviceName,limsResults.getDeviceName());
        //批号
        queryWrapper.like(StrUtil.isNotBlank(limsResults.getBatchNo()),LimsResultsAbnormal::getBatchNo,limsResults.getBatchNo());
        //检验类别
        queryWrapper.like(StrUtil.isNotBlank(limsResults.getCheckCategory()),LimsResultsAbnormal::getCheckCategory,limsResults.getCheckCategory());
        //是否作废
        queryWrapper.eq(StrUtil.isNotBlank(limsResults.getCancelFlag()),LimsResultsAbnormal::getCancelFlag,limsResults.getCancelFlag());
        //记录行动
        queryWrapper.eq(StrUtil.isNotBlank(limsResults.getActionStatus()),LimsResultsAbnormal::getActionStatus,limsResults.getActionStatus());
        //5why分析
        queryWrapper.eq(StrUtil.isNotBlank(limsResults.getFiveWhyStatus()),LimsResultsAbnormal::getFiveWhyStatus,limsResults.getFiveWhyStatus());
        //完成状态
        queryWrapper.eq(StrUtil.isNotBlank(limsResults.getCompleteStatus()),LimsResultsAbnormal::getCompleteStatus,limsResults.getCompleteStatus());
        //审核状态
        queryWrapper.eq(null != limsResults.getExamineStatus(),LimsResultsAbnormal::getExamineStatus,limsResults.getExamineStatus());

        //推送时间
        queryWrapper.ge(null != limsResults.getCheckDateBegin(),LimsResultsAbnormal::getReportDate,limsResults.getCheckDateBegin());
        queryWrapper.le(null != limsResults.getCheckDateEnd(),LimsResultsAbnormal::getReportDate,limsResults.getCheckDateEnd());

        //计算行动计划数量(n/n)
        countProgress();

        //查询权限
        if(!(null != limsResults.getDeptCode() && limsResults.getDeptCode().length() == 12)){
            //获取部门名称(用于权限判断 质量管理部和下级可以看所有)
            String dept = limsResults.getDeptCode();
            if(StrUtil.isNotBlank(dept)){
                //获取部门名称
                LambdaQueryWrapper<SysDepart> queryDeptWrapper = new LambdaQueryWrapper<SysDepart>();
                queryDeptWrapper.eq(SysDepart::getOrgCode, dept.substring(0,9));
                List<SysDepart> listDepart = sysDepartService.list(queryDeptWrapper);
                boolean isDept = true;
                if(null != listDepart && listDepart.size()>0){
                    for (SysDepart sysDepart : listDepart) {
                        if(sysDepart.getDepartName().startsWith("质量")){
                            isDept = false;
                        }
                    }
                }
                if(isDept){
                    queryWrapper.eq(LimsResultsAbnormal::getDeptCode ,dept);
                }
            }
        }
        IPage<LimsResultsAbnormal> pageList = page(page, queryWrapper);
        List<LimsResultsAbnormal> limsResultsAbnormalList = pageList.getRecords();
        //获取用户
        Map<String, String> userMap = sysUserService.list().stream().peek(Objects::requireNonNull) // 严格检查元素非空
                .collect(Collectors.toUnmodifiableMap(SysUser::getId, SysUser::getRealname));

        for (LimsResultsAbnormal lr : limsResultsAbnormalList) {
            //分析时效符合度(24h) 分析时间-推送时间
            lr.setAnalyzeTimeliness(DateUtils.calculateTimeDifferenceInHours(lr.getReportDate(),lr.getAnalysisTime()));
            //审核时效符合度(72h) 分析时间-审批时间
            lr.setAuditTimeliness(DateUtils.calculateTimeDifferenceInHours(lr.getReportDate(),lr.getMgrapprovalTime()));
            //审批人名称
            if(StrUtil.isNotBlank(lr.getApprovalCode())){
                lr.setApprovalName(userMap.get(lr.getApprovalCode()));
            }

        }
        return pageList;
    }

    /**
     * 计算行动计划数量
     */
    public void countProgress() {
        //获取未审批数据
        LambdaQueryWrapper<LimsResultsAbnormal> queryDeptWrapper = new LambdaQueryWrapper<LimsResultsAbnormal>();
        //完成状态(不等于完成)
        queryDeptWrapper.ne(LimsResultsAbnormal::getCompleteStatus,"1");
        //2个月内
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MONTH, -2);
        queryDeptWrapper.gt(LimsResultsAbnormal::getCreateTime, calendar.getTime());
        List<LimsResultsAbnormal> limsList = list(queryDeptWrapper);

        //lims与5why对照
        Map<String,String> limsAndFwhymap = new HashMap<>();
        //lims Id集合
        List<String> limsIdList = limsList.stream().map(LimsResultsAbnormal::getId).collect(Collectors.toList());
        //获取行动
        if(null != limsList && limsIdList.size() > 0){
            //获取5why
            LambdaQueryWrapper<FiveWhyAnalyse> fwhyQuery = new LambdaQueryWrapper<>();
            limsIdList.forEach(limsId -> fwhyQuery.or().like(FiveWhyAnalyse::getDataId, limsId));
            List<FiveWhyAnalyse> fiveWhyAnalyseList = fiveWhyAnalyseService.list(fwhyQuery);
            limsAndFwhymap = fiveWhyAnalyseList.stream().collect(Collectors.toMap(
                                    FiveWhyAnalyse::getDataId,
                                    FiveWhyAnalyse::getId,
                                    (existing, replacement) -> existing
                            ));

            List<ActionPlanTrack> actionPlanTrackList = new ArrayList<>();
            if(null != fiveWhyAnalyseList && fiveWhyAnalyseList.size() > 0){
                //获取行动(行动计划跟踪)
                LambdaQueryWrapper<ActionPlanTrack> query = new LambdaQueryWrapper<>();
                //5whyID
                fiveWhyAnalyseList.forEach(fiveWhyAnalyse -> query.or().like(ActionPlanTrack::getDataId, fiveWhyAnalyse.getId()));
                actionPlanTrackList = actionPlanTrackService.list(query);
            }

            //需要更新的lims
            List<LimsResultsAbnormal> limsUpdateList = new ArrayList<>();
            if(null != actionPlanTrackList && actionPlanTrackList.size() > 0){
                for (LimsResultsAbnormal lims : limsList) {
                    String fiveWhyStatus = lims.getFiveWhyStatus();
                    //回填5whyid
                    if(StrUtil.isBlank(lims.getAnalysisId())){
                        if(limsAndFwhymap != null && StrUtil.isNotBlank(limsAndFwhymap.get(lims.getId()))){
                            lims.setAnalysisId(limsAndFwhymap.get(lims.getId()));
                            //是否5why分析(是)
                            lims.setFiveWhyStatus("1");
                        }else{
                            //是否5why分析(否)
                            lims.setFiveWhyStatus("0");
                        }
                    }
                    //判断5why是否更新
                    if(StrUtil.isNotBlank(lims.getAnalysisId()) && !lims.getAnalysisId().equals(limsAndFwhymap.get(lims.getId()))){
                        lims.setAnalysisId(limsAndFwhymap.get(lims.getId()));
                    }

                    int total = 0; //总数
                    int complete = 0; //完成数
                    String limsId = lims.getAnalysisId();
                    for (ActionPlanTrack actionTrack : actionPlanTrackList) {
                        if(actionTrack.getDataId().equals(limsId)){
                            total++; //finish_verify_state
                            if("2".equals(actionTrack.getFinishVerifyState())){
                                complete++;
                            }
                        }
                    }
                    //进度
                    String completeProgress = complete+"/"+total;
                    String CompleteProgressOld = lims.getCompleteProgress();
                    lims.setCompleteProgress(completeProgress);
                    //完成状态(更新完成状态)
                    if(complete == total && total != 0){
                        lims.setCompleteStatus("1");
                    }else{
                        lims.setCompleteStatus("0");
                    }
                    String actionStatus = lims.getActionStatus();
                    if(total>0){
                        //是否进入行动(是)
                        lims.setActionStatus("1");
                    }else{
                        //是否进入行动(否)
                        lims.setActionStatus("0");
                    }
                    //判断是否更新
                    if(!completeProgress.equals(CompleteProgressOld) || !actionStatus.equals(lims.getActionStatus())
                            || !fiveWhyStatus.equals(lims.getFiveWhyStatus())){
                        limsUpdateList.add(lims);
                    }
                }
            }else{
                //没有匹配到行动计划
                for (LimsResultsAbnormal lims : limsList) {
                    lims.setCompleteProgress("0/0");
                    //是否进入行动
                    lims.setActionStatus("0");
                    limsUpdateList.add(lims);
                    //更新5why状态
                    if(StrUtil.isBlank(lims.getAnalysisId())){
                        if(limsAndFwhymap != null && StrUtil.isNotBlank(limsAndFwhymap.get(lims.getId()))){
                            lims.setAnalysisId(limsAndFwhymap.get(lims.getId()));
                            //是否5why分析(是)
                            lims.setFiveWhyStatus("1");
                        }else{
                            //是否5why分析(否)
                            lims.setFiveWhyStatus("0");
                        }
                    }

                }
            }
            //批量更新
            if(null != limsUpdateList && limsUpdateList.size() > 0){
                updateBatchById(limsUpdateList);
            }
        }
    }

    /**
     * 超标不合格定时推送
     * 推送到LIMS平台
     */
    @Override
    public void limsMessageSendJob() {

        //获取未审批数据
        LambdaQueryWrapper<LimsResultsAbnormal> queryDeptWrapper = new LambdaQueryWrapper<LimsResultsAbnormal>();
        //完成状态(完成)
        queryDeptWrapper.eq(LimsResultsAbnormal::getCompleteStatus,"1");
        //审批状态(通过)
        queryDeptWrapper.eq(LimsResultsAbnormal::getExamineStatus,"1");
        //最后推送完成状态(完成后不会再推送)
        queryDeptWrapper.eq(LimsResultsAbnormal::getLastStatus,"0");
        //获取1年前
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.YEAR, -1);
        queryDeptWrapper.gt(LimsResultsAbnormal::getCreateTime, calendar.getTime());

        List<LimsResultsAbnormal> limsList = list(queryDeptWrapper);
        for (LimsResultsAbnormal limsResultsAbnormalNew : limsList) {
            //推送车间分析排查结果 5w分析
            LimsResultDto resultDto = push5wToLims(limsResultsAbnormalNew);
            if("1".equals(resultDto.getCode())){
                //推送行动计划 行动分析
                resultDto = pushActionToLims(limsResultsAbnormalNew);
                if(!"1".equals(resultDto.getCode())){
                    //推送失败
                    limsResultsAbnormalNew.setSendLog(JSONUtil.toJsonStr(resultDto));
                }else{
                    //成功
                    limsResultsAbnormalNew.setSendLog("");
                    limsResultsAbnormalNew.setLastStatus("1");
                }
            }else{
                //推送失败
                limsResultsAbnormalNew.setSendLog(JSONUtil.toJsonStr(resultDto));
            }
        }
        if(null != limsList && limsList.size() > 0){
            updateBatchById(limsList);
        }
    }
}
