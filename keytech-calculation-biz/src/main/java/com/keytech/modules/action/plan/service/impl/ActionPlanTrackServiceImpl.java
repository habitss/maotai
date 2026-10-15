package com.keytech.modules.action.plan.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollStreamUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateUnit;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.keytech.base.service.impl.BaseServiceImpl;
import com.keytech.modules.action.plan.dto.ActionPlanSummaryChartDto;
import com.keytech.modules.action.plan.dto.ActionPlanSummaryListDto;
import com.keytech.modules.action.plan.dto.ActionPlanSummaryQueryDto;
import com.keytech.modules.action.plan.entity.ActionPlanProblemType;
import com.keytech.modules.action.plan.entity.ActionPlanTrack;
import com.keytech.modules.action.plan.mapper.ActionPlanTrackMapper;
import com.keytech.modules.action.plan.service.IActionPlanProblemTypeService;
import com.keytech.modules.action.plan.service.IActionPlanTrackService;
import com.keytech.modules.home.dto.EmpRaFromDto;
import com.keytech.modules.home.dto.WSActionStatisticsDto;
import com.keytech.modules.home.vo.EmpRaFromVo;
import com.keytech.modules.mdm3.entity.MdmDict;
import com.keytech.modules.mdm3.service.IMdmDictService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.shiro.SecurityUtils;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.exception.JeecgBootException;
import org.jeecg.common.system.vo.LoginUser;
import org.jeecg.common.util.ImportExcelUtil;
import org.jeecg.modules.system.entity.SysDepart;
import org.jeecg.modules.system.entity.SysUser;
import org.jeecg.modules.system.model.SysDepartTreeModel;
import org.jeecg.modules.system.service.ISysDepartService;
import org.jeecg.modules.system.service.ISysUserService;
import org.jeecgframework.poi.excel.ExcelImportUtil;
import org.jeecgframework.poi.excel.entity.ImportParams;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import java.io.IOException;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

import static java.util.Objects.nonNull;

/**
 * @Description: 行动计划跟踪
 * @Author: jeecg-boot
 * @Date:   2025-01-08
 * @Version: V1.0
 */
@Service
public class ActionPlanTrackServiceImpl extends BaseServiceImpl<ActionPlanTrackMapper, ActionPlanTrack> implements IActionPlanTrackService {
    @Resource
    private ActionPlanTrackMapper actionPlanTrackMapper;

    @Autowired
    private IMdmDictService mdmDictService;

    @Autowired
    private ISysUserService sysUserService;

    @Autowired
    private ISysDepartService sysDepartService;

    @Autowired
    private IActionPlanProblemTypeService actionPlanProblemTypeService;

    @Override
    @Transactional
    public void updateDataByJob(String parameterObject) {
        //获取进行中、延迟，且实际完成时间为空的数据
        LambdaQueryWrapper<ActionPlanTrack> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(ActionPlanTrack::getFinishState, 0, 2);
        queryWrapper.lt(ActionPlanTrack::getDelayCount, 3);
        queryWrapper.isNull(ActionPlanTrack::getPracticalFinishDate);
        List<ActionPlanTrack> list = actionPlanTrackMapper.selectList(queryWrapper);

        List<ActionPlanTrack> newList = new ArrayList<>();
        for (ActionPlanTrack actionPlanTrack : list) {
            processingData(actionPlanTrack);
            newList.add(actionPlanTrack);
            if(newList.size() > 2000){
                this.updateBatchById(newList);
                newList.clear();
            }
        }
        if(CollUtil.isNotEmpty(newList)){
            this.updateBatchById(newList);
        }
    }

    @Override
    public void saveOrUpdateAction(String dataId, List<ActionPlanTrack> actionPlanTrackList) {
        if(StrUtil.isBlank(dataId) || CollUtil.isEmpty(actionPlanTrackList)){
            throw new JeecgBootException("行动计划参数错误");
        }

        //查询已存在的行动计划,且有效的行动计划
        List<ActionPlanTrack> oldActionList = this.list(
                new LambdaQueryWrapper<ActionPlanTrack>()
                        .eq(ActionPlanTrack::getDataId, dataId)
        );
        //将数据转换成map集合,方便后续判断是否存在
        Map<Integer, ActionPlanTrack> actionMap = oldActionList.stream()
                .collect(Collectors.toMap(ActionPlanTrack::getId, actionPlanTrack -> actionPlanTrack));

        //需要进行操作的行动计划集合
        List<ActionPlanTrack> optList = new ArrayList<>();
        actionPlanTrackList.forEach(actionPlan -> {
            actionPlan.setDataId(dataId);
            if(null == actionPlan.getId()){
                //如果id为空直接添加
                optList.add(actionPlan);
            }else if(actionMap.containsKey(actionPlan.getId())){
                //如果已存在行动计划数据
                ActionPlanTrack oldAction = actionMap.get(actionPlan.getId());
                //无效状态可继续修改
                if("0".equals(oldAction.getStatus())){
                    optList.add(actionPlan);
                }
            }
        });
        if(CollUtil.isNotEmpty(optList)){
            this.saveOrUpdateBatch(optList);
        }
    }

    @Override
    public Result<?> importExcelBy(HttpServletRequest request, HttpServletResponse response, Class<ActionPlanTrack> clazz) throws IOException {
        //获取导入文件
        MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest) request;
        Map<String, MultipartFile> fileMap = multipartRequest.getFileMap();

        //错误信息集合
        List<String> errorMessage = new ArrayList<>();
        //成功数，错误数
        int successLines = 0, errorLines = 0;
        for (Map.Entry<String, MultipartFile> entity : fileMap.entrySet()) {
            // 获取上传文件对象
            MultipartFile file = entity.getValue();
            ImportParams params = new ImportParams();
            params.setTitleRows(2);
            params.setHeadRows(2);
            params.setNeedSave(true);
            try {
                //获取当前用户信息
                LoginUser loginUser = (LoginUser) SecurityUtils.getSubject().getPrincipal();
                //所有用户信息
                Map<String, SysUser> allUserMap = (Map<String, SysUser>) getDict(null).get("allUserMap");
                //所有单位信息
                Map<String, SysDepart> allDeptMap = (Map<String, SysDepart>) getDict(null).get("allDeptMap");
                //获取问题类型数据
                List<ActionPlanProblemType> problemTypeList = actionPlanProblemTypeService.list();
                Map<String, ActionPlanProblemType> problemMap = CollStreamUtil.toMap(problemTypeList, ActionPlanProblemType::getId, bean -> bean);
                //获取检查数据记录格式字典
                List<MdmDict> mdmDictList = mdmDictService.getDictAllOrByGroupCode(
                        List.of(
                                "MES3_ACTION_PLAN_FINISH"
                                , "MES3_ACTION_PLAN_MEETING"
                                , "MES3_ACTION_PLAN_IMPORTANCE_EVALUATION"
                                , "MES3_ACTION_PLAN_LEVEL"
                        )
                );
                Map<String, MdmDict> dictMap = mdmDictList.stream()
                        .collect(Collectors.toMap(
                                item -> item.getGroupCode() + "-" + item.getItemVal(), // 键是由groupCode和val组成的字符串
                                item -> item // 值是对象本身
                        ));

                //获取上传文件中的数据
                List<ActionPlanTrack> list = ExcelImportUtil.importExcel(file.getInputStream(), clazz, params);

                //获取上传文件中有id的原数据
                LambdaQueryWrapper<ActionPlanTrack> queryWrapper = new LambdaQueryWrapper<>();
                queryWrapper.in(ActionPlanTrack::getId, list.stream().map(ActionPlanTrack::getId).collect(Collectors.toList()));
                List<ActionPlanTrack> oldList = this.list(queryWrapper);
                Map<Integer, ActionPlanTrack> oldMap = CollStreamUtil.toMap(oldList, ActionPlanTrack::getId, bean -> bean);

                //操作数据
                List<ActionPlanTrack> saveList = new ArrayList<>();
                int lineNumber = 3;
                for(ActionPlanTrack actionPlanTrack : list){
                    lineNumber++;
                    //提出日期为空，默认为当前日期
                    if(null == actionPlanTrack.getRaiseDate()){
                        actionPlanTrack.setRaiseDate(new Date());
                    }
                    if(StrUtil.isNotBlank(actionPlanTrack.getImportanceEvaluation()) && !dictMap.containsKey("MES3_ACTION_PLAN_IMPORTANCE_EVALUATION-" + actionPlanTrack.getImportanceEvaluation())){
                        errorMessage.add("第" + lineNumber + "行，重要性评价不存在");
                        errorLines++;
                        continue;
                    }
                    if(StrUtil.isBlank(actionPlanTrack.getProblemTypeId()) || !problemMap.containsKey(actionPlanTrack.getProblemTypeId())){
                        errorMessage.add("第" + lineNumber + "行，问题类型为空或不存在");
                        errorLines++;
                        continue;
                    }
                    if(StrUtil.isBlank(actionPlanTrack.getProblemDescription())){
                        errorMessage.add("第" + lineNumber + "行，问题描述为空");
                        errorLines++;
                        continue;
                    }
                    if(StrUtil.isBlank(actionPlanTrack.getActionTitle())){
                        errorMessage.add("第" + lineNumber + "行，行动为空");
                        errorLines++;
                        continue;
                    }
                    if(StrUtil.isBlank(actionPlanTrack.getPutUserId()) || !allUserMap.containsKey(actionPlanTrack.getPutUserId())){
                        errorMessage.add("第" + lineNumber + "行，提出人为空或不存在");
                        errorLines++;
                        continue;
                    }else{
                        actionPlanTrack.setPutUserName(allUserMap.get(actionPlanTrack.getPutUserId()).getRealname());
                    }
                    if(StrUtil.isBlank(actionPlanTrack.getDutyUserId()) || !allUserMap.containsKey(actionPlanTrack.getDutyUserId())){
                        errorMessage.add("第" + lineNumber + "行，责任人为空或不存在");
                        errorLines++;
                        continue;
                    }else{
                        actionPlanTrack.setDutyUserName(allUserMap.get(actionPlanTrack.getDutyUserId()).getRealname());
                    }
                    if(StrUtil.isNotBlank(actionPlanTrack.getExecuteUserId()) && !allUserMap.containsKey(actionPlanTrack.getExecuteUserId())){
                        errorMessage.add("第" + lineNumber + "行，执行人不存在");
                        errorLines++;
                        continue;
                    } else if (StrUtil.isNotBlank(actionPlanTrack.getExecuteUserId())){
                        actionPlanTrack.setExecuteUserName(allUserMap.get(actionPlanTrack.getExecuteUserId()).getRealname());
                    }
                    if(StrUtil.isBlank(actionPlanTrack.getVerifyUserId()) || !allUserMap.containsKey(actionPlanTrack.getVerifyUserId())){
                        errorMessage.add("第" + lineNumber + "行，确认人不存在");
                        errorLines++;
                        continue;
                    }else{
                        actionPlanTrack.setVerifyUserName(allUserMap.get(actionPlanTrack.getVerifyUserId()).getRealname());
                    }
                    if(StrUtil.isBlank(actionPlanTrack.getPutDeptId()) || !allDeptMap.containsKey(actionPlanTrack.getPutDeptId())){
                        errorMessage.add("第" + lineNumber + "行，提出部门为空或不存在");
                        errorLines++;
                        continue;
                    }
                    if(StrUtil.isBlank(actionPlanTrack.getDutyDeptId()) || !allDeptMap.containsKey(actionPlanTrack.getDutyDeptId())){
                        errorMessage.add("第" + lineNumber + "行，责任部门为空或不存在");
                        errorLines++;
                        continue;
                    }
                    if(StrUtil.isNotBlank(actionPlanTrack.getExecuteDeptId()) && !allDeptMap.containsKey(actionPlanTrack.getExecuteDeptId())){
                        errorMessage.add("第" + lineNumber + "行，执行部门不存在");
                        errorLines++;
                        continue;
                    }
                    if(StrUtil.isBlank(actionPlanTrack.getVerifyDeptId()) || !allDeptMap.containsKey(actionPlanTrack.getVerifyDeptId())){
                        errorMessage.add("第" + lineNumber + "行，确认部门不存在");
                        errorLines++;
                        continue;
                    }
                    if(StrUtil.isBlank(actionPlanTrack.getActionLevel()) || !dictMap.containsKey("MES3_ACTION_PLAN_LEVEL-" + actionPlanTrack.getActionLevel())){
                        errorMessage.add("第" + lineNumber + "行，行动级别为空或不存在");
                        errorLines++;
                        continue;
                    }
                    if(StrUtil.isBlank(actionPlanTrack.getEnterMeeting()) || !dictMap.containsKey("MES3_ACTION_PLAN_MEETING-" + actionPlanTrack.getEnterMeeting())){
                        errorMessage.add("第" + lineNumber + "行，是否进入会议为空或不存在");
                        errorLines++;
                        continue;
                    }
                    if(null == actionPlanTrack.getPlanFinishDate()){
                        errorMessage.add("第" + lineNumber + "行，计划完成时间为空");
                        errorLines++;
                        continue;
                    }
                    processingData(actionPlanTrack);

                    //情况创建、修改人不让导入。使用默认值
                    actionPlanTrack.setCreateBy(null);
                    actionPlanTrack.setUpdateBy(null);
                    actionPlanTrack.setUpdateTime(null);

                    if(null != actionPlanTrack.getId()){
                        //根据id获取原数据，判断当前用户是否有权限修改
                        ActionPlanTrack oldData = oldMap.get(actionPlanTrack.getId());
                        //根据不同角色，调整可修改数据
                        if(loginUser.getId().equals(oldData.getPutUserId())){
                            //提出人可以修改任何数据，既保存新上传数据
                            saveList.add(actionPlanTrack);
                        }else{
                            //三次延期及备注都可修改
                            oldData.setOneText(actionPlanTrack.getOneText());
                            oldData.setOneTime(actionPlanTrack.getOneTime());
                            oldData.setTwoTime(actionPlanTrack.getTwoTime());
                            oldData.setTwoText(actionPlanTrack.getTwoText());
                            oldData.setThreeText(actionPlanTrack.getThreeText());
                            oldData.setThreeTime(actionPlanTrack.getThreeTime());
                            oldData.setRemark(actionPlanTrack.getRemark());

                            if(loginUser.getId().equals(oldData.getDutyUserId())){
                                //当前用户为责任人
                                oldData.setDutyDeptId(actionPlanTrack.getDutyDeptId());
                                oldData.setExecuteUserId(actionPlanTrack.getExecuteUserId());
                                oldData.setExecuteUserName(actionPlanTrack.getExecuteUserName());
                                oldData.setExecuteDeptId(actionPlanTrack.getExecuteDeptId());
                                oldData.setPracticalFinishDate(actionPlanTrack.getPracticalFinishDate());
                            }else if(loginUser.getId().equals(oldData.getExecuteUserId())){
                                //当前用户为执行人
                                oldData.setExecuteDeptId(actionPlanTrack.getExecuteDeptId());
                                oldData.setPracticalFinishDate(actionPlanTrack.getPracticalFinishDate());
                            }else if(loginUser.getId().equals(oldData.getVerifyUserId())){
                                //当前用户为确认人
                                oldData.setVerifyDeptId(actionPlanTrack.getVerifyDeptId());
                                oldData.setFinishState(actionPlanTrack.getFinishState());
                                oldData.setFinishVerifyState(actionPlanTrack.getFinishVerifyState());
                            }
                            saveList.add(oldData);
                        }
                    }else{
                        saveList.add(actionPlanTrack);
                    }
                    successLines++;
                }
                this.saveOrUpdateBatch(saveList);
            } catch (Exception e) {
                log.error(e.getMessage(), e);
                errorLines++;
                errorMessage.add("发生异常：" + e.getMessage());
            } finally {
                try {
                    file.getInputStream().close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return ImportExcelUtil.imporReturnRes(errorLines, successLines, errorMessage);
    }


    @Override
    public Map<String, Object> getSummaryData(ActionPlanSummaryQueryDto queryDto) {
        List<ActionPlanTrack> actionPlanTrackList = processingSummaryQuery(queryDto);
        //获取所有用户信息，并按id做key转换成map
        Map<String, SysUser> allUserMap = (Map<String, SysUser>) getDict(queryDto).get("allUserMap");

        //获取所有单位信息，并按id做key转换成map
        Map<String, SysDepart> allDeptMap = (Map<String, SysDepart>) getDict(queryDto).get("allDeptMap");


        ActionPlanSummaryChartDto chartDto = getSummaryChartData(actionPlanTrackList, queryDto, allUserMap, allDeptMap);
        ActionPlanSummaryListDto listDto = getSummaryListData(1, actionPlanTrackList, queryDto, allUserMap, allDeptMap);
        Map<String, Object> result = new HashMap<>();
        result.put("chartData", chartDto);
        result.put("summaryListData", listDto);
        return result;
    }

    @Override
    public Map<String, Object> queryIndexSummaryData(WSActionStatisticsDto dto) {
        //所有用户信息
        Map<String, SysUser> allUserMap = (Map<String, SysUser>) getDict(null).get("allUserMap");
        //所有单位信息
        Map<String, SysDepart> allDeptMap = null;
        Object obj = getDict(null).get("allDeptMap");
        if (obj instanceof Map<?, ?> tempMap) {
            allDeptMap = new HashMap<>();
            for (Map.Entry<?, ?> entry : tempMap.entrySet()) {
                if(entry.getKey() instanceof String && entry.getValue() instanceof SysDepart) {
                    allDeptMap.put((String) entry.getKey(), (SysDepart) entry.getValue());
                }
            }
        }

        ActionPlanSummaryQueryDto queryDto = new ActionPlanSummaryQueryDto();
        queryDto.setObjectType("2");
        queryDto.setDateType("3");
        queryDto.setDutyOrPut("1");
        queryDto.setDateRangeField_begin(dto.getOpTime());
        queryDto.setDateRangeField_end(dto.getEdTime());
        queryDto.setUserOrDept("2");
        List<ActionPlanTrack> actionPlanTrackList = processingSummaryQuery(queryDto);

        ActionPlanSummaryListDto listDto = getSummaryListData(1, actionPlanTrackList, queryDto, allUserMap, allDeptMap);

        queryDto.setUserId(dto.getDutUserId());
        queryDto.setUserOrDept("1");
        queryDto.setDeptIds(dto.getDeptId());
        List<ActionPlanTrack> userActionPlanTrackList = processingSummaryQuery(queryDto);
        ActionPlanSummaryChartDto userChartDto = getSummaryChartData(userActionPlanTrackList, queryDto, allUserMap, allDeptMap);
        Map<String, Object> result = new HashMap<>();

        //封装工厂首页-行动计划数据
        List<Map<String, Object>> listMap = new ArrayList<>(0);
        for(ActionPlanSummaryListDto summaryDto : listDto.getChildren()){
            Map<String, Object> map = new HashMap<>();
            map.put("name", summaryDto.getTitle());
            map.put("size", summaryDto.getActionTotal());
            map.put("lv", summaryDto.getFinishedStateRatio());
            map.put("totalSize", 0);
            listMap.add(map);

            if(summaryDto.getId().equals(dto.getDeptId())){
                //封装车间首页-行动计划完成率
                JSONObject curMonthJson = new JSONObject();
                curMonthJson.set("本月计划完成任务", summaryDto.getActionTotal());
                curMonthJson.set("总数", summaryDto.getActionTotal());
                curMonthJson.set("已完成", summaryDto.getFinishedStateTotal());
                curMonthJson.set("延期", summaryDto.getDelayStateTotal());
                curMonthJson.set("进行中", summaryDto.getProgressStateTotal());

                JSONArray jsonArray = new JSONArray();
                JSONObject completedJson = new JSONObject();
                completedJson.set("name", "completed");
                completedJson.set("size", summaryDto.getFinishedStateTotal());
                completedJson.set("lv", summaryDto.getFinishedStateRatio());
                completedJson.set("totalSize", 0);
                jsonArray.add(completedJson);
                JSONObject postpone = new JSONObject();
                postpone.set("name", "postpone");
                postpone.set("size", summaryDto.getDelayStateTotal());
                postpone.set("lv", summaryDto.getDelayStateRatio());
                postpone.set("totalSize", 0);
                jsonArray.add(postpone);
                JSONObject underWay = new JSONObject();
                underWay.set("name", "underWay");
                underWay.set("size", summaryDto.getProgressStateTotal());
                underWay.set("lv", calcRatio(summaryDto.getProgressStateTotal(), summaryDto.getActionTotal()));
                underWay.set("totalSize", 0);
                jsonArray.add(underWay);
                curMonthJson.set("lv", jsonArray);
                //车间首页-行动计划完成率
                result.put("planCompletion", curMonthJson);
            }
        }
        JSONObject finishTopJson = new JSONObject();
        listMap.sort((m1, m2) -> {
            Number lv1 = (Double) m1.get("lv");
            Number lv2 = (Double) m2.get("lv");
            return Double.compare(lv2.doubleValue(), lv1.doubleValue()); // 降序
        });
        finishTopJson.set("行动计划完成率排名", listMap);
        finishTopJson.set("工厂行动计划完成率", listDto.getFinishedStateRatio());
        result.put("行动计划", finishTopJson);

        //封装车间首页-个人计划完成率
        JSONObject userActionJson = new JSONObject();
        int userTotal = userChartDto.getDelayTotal()+userChartDto.getFinishedTotal()+userChartDto.getProgressTotal();
        userActionJson.set("本月计划完成任务", userTotal);
        userActionJson.set("已完成", userChartDto.getFinishedTotal());
        userActionJson.set("延迟", userChartDto.getDelayTotal());
        userActionJson.set("正在进行", userChartDto.getProgressTotal());
        //个人行动计划完成率
        result.put("individualPlan", userActionJson);
        return result;
    }

    //数据处理逻辑
    private void processingData(ActionPlanTrack actionPlanTrack){
        //当前日期
        Date now = DateUtil.beginOfDay(new Date());
        //计划完成日期不为空
        if(null != actionPlanTrack.getPlanFinishDate()){
            //实际完成日期为空
            if(null == actionPlanTrack.getPracticalFinishDate()){
                //系统自动调整规则为第一次延期按计划完成时间推迟两天、第二次延期按第一次延期时间推迟两天、第三次延期按第二次延期时间推迟两天
                if(null == actionPlanTrack.getOneTime() && actionPlanTrack.getPlanFinishDate().compareTo(now) < 0){
                    actionPlanTrack.setOneTime(DateUtil.offsetDay(actionPlanTrack.getPlanFinishDate(), 2));
                    actionPlanTrack.setOneText("系统自动延期");
                    actionPlanTrack.setDelayCount(1);
                }
                if(null == actionPlanTrack.getTwoTime() && null != actionPlanTrack.getOneTime() && actionPlanTrack.getOneTime().compareTo(now) < 0){
                    actionPlanTrack.setTwoTime(DateUtil.offsetDay(actionPlanTrack.getOneTime(), 2));
                    actionPlanTrack.setTwoText("系统自动延期");
                    actionPlanTrack.setDelayCount(2);
                }
                if(null == actionPlanTrack.getThreeTime() && null != actionPlanTrack.getTwoTime() && actionPlanTrack.getTwoTime().compareTo(now) < 0){
                    actionPlanTrack.setThreeTime(DateUtil.offsetDay(actionPlanTrack.getTwoTime(), 2));
                    actionPlanTrack.setThreeText("系统自动延期");
                    actionPlanTrack.setDelayCount(3);
                }

                //计算剩余天数
                int betweenDay = (int) DateUtil.between(actionPlanTrack.getPlanFinishDate(), now, DateUnit.DAY);
                if(actionPlanTrack.getPlanFinishDate().before(now)){
                    betweenDay = -betweenDay;
                }
                actionPlanTrack.setSurplusDay(betweenDay+"");
            }

            //实际完成日期不为空
            if(null != actionPlanTrack.getPracticalFinishDate()){
                actionPlanTrack.setFinishVerifyState("1");
            } else {
                actionPlanTrack.setFinishVerifyState("");
            }
            //实际完成日期为空，计划完成日期小于当前日期,完成情况为延迟，否则为进行中
            if(null == actionPlanTrack.getPracticalFinishDate()
                    && DateUtil.endOfDay(actionPlanTrack.getPlanFinishDate()).compareTo(new Date()) < 0){
                actionPlanTrack.setFinishState("2");
            } else if(null != actionPlanTrack.getPracticalFinishDate()
                    && DateUtil.endOfDay(actionPlanTrack.getPlanFinishDate()).compareTo(DateUtil.endOfDay(actionPlanTrack.getPracticalFinishDate())) < 0){
                //实际完成日期不为空，计划完成日期小于实际完成日期,完成情况为延迟
                actionPlanTrack.setFinishState("2");
            }else{
                actionPlanTrack.setFinishState("0");
            }
        }
    }

    /**
     * 行动计划汇总查询数据处理逻辑
     * @param dto
     * @return
     */
    public List<ActionPlanTrack> processingSummaryQuery(ActionPlanSummaryQueryDto dto){
        QueryWrapper<ActionPlanTrack> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("status", 1);
        //检索对象和日期检索类型都不为空，拼接日期查询逻辑
        if(StrUtil.isNotBlank(dto.getObjectType()) && StrUtil.isNotBlank(dto.getDateType())){
            String searchField = "";
            //检索对象为1，2，3分别对应提出日期、计划完成日期、实际完成日期字段
            if("1".equals(dto.getObjectType())){
                searchField = "raise_date";
            } else if("2".equals(dto.getObjectType())){
                searchField = "plan_finish_date";
            } else if("3".equals(dto.getObjectType())){
                searchField = "practical_finish_date";
            }

            //检索日期类型为1，2，3分别对应当月、当日、自定义时间段
            if("1".equals(dto.getDateType()) && StrUtil.isNotBlank(dto.getMonthField())){
                Date date = DateUtil.parse(dto.getMonthField());
                queryWrapper.between(
                        searchField,
                        DateUtil.beginOfMonth(date),
                        DateUtil.endOfMonth(date)
                );
            } else if("2".equals(dto.getDateType()) && StrUtil.isNotBlank(dto.getDayField())){
                queryWrapper.eq(searchField, dto.getDayField());
            } else if("3".equals(dto.getDateType())
                    && StrUtil.isNotBlank(dto.getDateRangeField_begin())
                    & StrUtil.isNotBlank(dto.getDateRangeField_end())){
                queryWrapper.between(
                        searchField,
                        dto.getDateRangeField_begin(),
                        dto.getDateRangeField_end()
                );
            } else if("4".equals(dto.getDateType()) && StrUtil.isNotBlank(dto.getYearField())){
                Date date = DateUtil.parse(dto.getYearField());
                queryWrapper.between(
                        searchField,
                        DateUtil.beginOfYear(date),
                        DateUtil.endOfYear(date)
                );
            }
        }

        //处理部门检索逻辑
        if(StrUtil.isNotBlank(dto.getDeptIds())){
            List<String> list = Arrays.asList(dto.getDeptIds().split(","));
            if(dto.getDutyOrPut().equals("1")){
                queryWrapper.in("duty_dept_id", list);
                queryWrapper.in(null != dto.getUserId(), "duty_user_id", dto.getUserId());
            }else{
                queryWrapper.in("put_dept_id", list);
                queryWrapper.in(null != dto.getUserId(), "put_user_id", dto.getUserId());
            }
        }

        //处理问题类型检索逻辑
        if(StrUtil.isNotBlank(dto.getProblemTypeIds())){
            List<String> list = Arrays.asList(dto.getProblemTypeIds().split(","));
            queryWrapper.in("problem_type_id", list);
        }
        return this.list(queryWrapper);
    }

    /**
     * 行动计划汇总饼图数据
     */
    public ActionPlanSummaryChartDto getSummaryChartData(List<ActionPlanTrack> actionPlanTrackList, ActionPlanSummaryQueryDto queryDto, Map<String, SysUser> allUserMap, Map<String, SysDepart> allDeptMap) {

        //获取饼图数据
        ActionPlanSummaryChartDto summaryDto = processingSummaryData(actionPlanTrackList);

        //获取柱状图数据
        Map<String, List<ActionPlanTrack>> groupMap;
        List<Object> objectList = new ArrayList<>();
        if("1".equals(queryDto.getUserOrDept())){
            groupMap = getStringListMap(actionPlanTrackList, queryDto);
            groupMap.forEach((k, v) -> {
                Map<String, ActionPlanSummaryChartDto> map = new HashMap<>();
                String name = k;
                if (allUserMap.containsKey(k)) {
                    name = allUserMap.get(k).getRealname();
                }
                map.put(name, processingSummaryData(v));
                objectList.add(map);
            });
        } else {
            if("1".equals(queryDto.getDutyOrPut())){
                groupMap = actionPlanTrackList.stream().filter(item -> null != item.getDutyDeptId()).collect(Collectors.groupingBy(ActionPlanTrack::getDutyDeptId));
            } else {
                groupMap = actionPlanTrackList.stream().filter(item -> null != item.getPutDeptId()).collect(Collectors.groupingBy(ActionPlanTrack::getPutDeptId));
            }
            groupMap.forEach((k, v) -> {
                Map<String, ActionPlanSummaryChartDto> map = new HashMap<>();
                String name = k;
                if (allDeptMap.containsKey(k)) {
                    name = allDeptMap.get(k).getDepartName();
                }
                map.put(name, processingSummaryData(v));
                objectList.add(map);
            });
        }
        summaryDto.setObjectList(objectList);
        return summaryDto;
    }

    /**
     * 处理汇总数据逻辑
     * @param list
     * @return
     */
    private ActionPlanSummaryChartDto processingSummaryData(List<ActionPlanTrack> list){
        ActionPlanSummaryChartDto summaryDto = new ActionPlanSummaryChartDto();
        //处理饼图数据，0：正在进行；1：已完成；2/4延迟+延迟完成
        summaryDto.setProgressTotal((int) list.stream().filter(item -> "0".equals(item.getFinishState())).count());
        summaryDto.setFinishedTotal((int) list.stream().filter(item -> "1".equals(item.getFinishState())).count());
        summaryDto.setDelayTotal((int) list.stream().filter(item -> "2".equals(item.getFinishState()) || "4".equals(item.getFinishState())).count());
        return summaryDto;
    }

    /**
     * 行动计划汇总列表数据
     */
    public ActionPlanSummaryListDto getSummaryListData(int level, List<ActionPlanTrack> actionPlanTrackList, ActionPlanSummaryQueryDto queryDto, Map<String, SysUser> allUserMap, Map<String, SysDepart> allDeptMap) {
        int actionTotal = actionPlanTrackList.size();
        int finishedStateTotal = 0, progressStateTotal = 0, closeStateTotal = 0, delayStateTotal = 0, delayFinishStateTotal = 0, oneDelayTotal = 0, twoDelayTotal = 0, threeDelayTotal = 0, delayTotal = 0;
        double finishedStateRatio = 0.0, delayStateRatio = 0.0, delayFinishStateRatio = 0.0,  delayRatio = 0.0, putKindSum = 0.0, dutKindSum = 0.0;
        for(ActionPlanTrack actionPlanTrack : actionPlanTrackList){
            if("0".equals(actionPlanTrack.getFinishState())){
                //进行中状态统计
                progressStateTotal++;
            }else if("1".equals(actionPlanTrack.getFinishState())){
                //已完成状态统计
                finishedStateTotal++;
            }else if("3".equals(actionPlanTrack.getFinishState())){
                //取消状态统计
                closeStateTotal++;
            }else if("2".equals(actionPlanTrack.getFinishState())){
                //延迟状态统计
                delayStateTotal++;
            }else if("4".equals(actionPlanTrack.getFinishState())){
                //延迟完成状态统计
                delayFinishStateTotal++;
            }

            //第一次延期期限统计
            if(null != actionPlanTrack.getOneTime()){
                oneDelayTotal++;
            }
            //第二次延期期限统计
            if(null != actionPlanTrack.getTwoTime()){
                twoDelayTotal++;
            }
            //第三次延期期限统计
            if(null != actionPlanTrack.getThreeTime()){
                threeDelayTotal++;
            }
            //当前延期期限总次数
            delayTotal += actionPlanTrack.getDelayCount();

            // 提出人激励
            String putKind = actionPlanTrack.getPutKind();
            if (StrUtil.isNotEmpty(putKind)) {
                putKind = putKind.trim();
                // 验证是否是合法的数字（正负整数和浮点数）
                if (putKind.matches("-?\\d+(\\.\\d+)?")) {
                    // 将字符串转换为双精度数并累加
                    putKindSum += Double.parseDouble(putKind);
                }
            }
            // 责任人激励
            String dutKind = actionPlanTrack.getDutyKind();
            if (StrUtil.isNotEmpty(dutKind)) {
                dutKind = dutKind.trim();
                // 验证是否是合法的数字（正负整数和浮点数）
                if (dutKind.matches("-?\\d+(\\.\\d+)?")) {
                    // 将字符串转换为双精度数并累加
                    dutKindSum += Double.parseDouble(dutKind);
                }
            }
        }
        //完成状态率
        finishedStateRatio = calcRatio(finishedStateTotal, actionTotal);
        //延迟状态率
        delayStateRatio = calcRatio(delayStateTotal, actionTotal);
        //延迟状态率
        delayFinishStateRatio = calcRatio(delayFinishStateTotal, actionTotal);

        //当前延期期限率
        delayRatio = calcRatio(delayTotal, actionTotal);
        //封装返回数据
        ActionPlanSummaryListDto dto = new ActionPlanSummaryListDto("","", actionTotal, finishedStateTotal, finishedStateRatio, progressStateTotal, closeStateTotal,
                delayStateTotal, delayStateRatio, delayFinishStateTotal, delayFinishStateRatio, oneDelayTotal, twoDelayTotal, threeDelayTotal, delayTotal, delayRatio,
                String.valueOf(putKindSum), String.valueOf(dutKindSum), null);
        //获取子级数据
        List<ActionPlanSummaryListDto> children = new ArrayList<>();
        if(level == 1){
            //获取所有单位树
            List<SysDepartTreeModel> deptAllTreeList = sysDepartService.queryTreeList();
            //获取树中第三层部门数据
            List<SysDepartTreeModel> deptTreeList = deptAllTreeList.get(0).getChildren().get(0).getChildren().get(0).getChildren();
            for(SysDepartTreeModel dept : deptTreeList){
                if(StrUtil.isNotBlank(queryDto.getDeptIds()) && queryDto.getDeptIds().indexOf(dept.getId()) == -1){
                    continue;
                }
                List<ActionPlanTrack> newList;
                //判断是根据责任还是提出来筛选数据
                if(queryDto.getDutyOrPut().equals("1")){
                    newList = actionPlanTrackList.stream().filter(item -> getSubDept(item.getDutyDeptId(), allDeptMap).equals(dept.getOrgCode())).collect(Collectors.toList());
                } else {
                    newList = actionPlanTrackList.stream().filter(item -> getSubDept(item.getPutDeptId(), allDeptMap).equals(dept.getOrgCode())).collect(Collectors.toList());
                }
                //递归获取子级数据列表
                ActionPlanSummaryListDto newDto = getSummaryListData(2, newList, queryDto, allUserMap, allDeptMap);
                newDto.setTitle(dept.getTitle());
                newDto.setId(dept.getId());
                children.add(newDto);
            }
            dto.setTitle("工厂");
            dto.setId("1");
        } else if(level == 2){
            //判断是根据责任还是提出来按用户分组
            Map<String, List<ActionPlanTrack>> groupMap = getStringListMap(actionPlanTrackList, queryDto);
            groupMap.forEach((k, v) -> {
                String name = k;
                if (allUserMap.containsKey(k)) {
                    name = allUserMap.get(k).getRealname();
                }
                //递归获取子级数据列表
                ActionPlanSummaryListDto newDto = getSummaryListData(3, v, queryDto, allUserMap, allDeptMap);
                newDto.setTitle(name);
                newDto.setId(k);
                children.add(newDto);
            });
        }
        dto.setChildren(CollUtil.isEmpty(children) ? null : children);
        return dto;
    }

    @NotNull
    private Map<String, List<ActionPlanTrack>> getStringListMap(List<ActionPlanTrack> actionPlanTrackList, ActionPlanSummaryQueryDto queryDto) {
        Map<String, List<ActionPlanTrack>> groupMap;
        if("1".equals(queryDto.getDutyOrPut())){
            groupMap = actionPlanTrackList.stream().filter(item -> null != item.getDutyUserId()).collect(Collectors.groupingBy(ActionPlanTrack::getDutyUserId));
        } else {
            groupMap = actionPlanTrackList.stream().filter(item -> null != item.getPutUserId()).collect(Collectors.groupingBy(ActionPlanTrack::getPutUserId));
        }
        return groupMap;
    }

    private Map<String, ?> getDict(ActionPlanSummaryQueryDto queryDto){
        String userId = "";
        if(null != queryDto){
            userId = queryDto.getUserId();
        }
        //获取所有用户信息，并按id做key转换成map
        List<SysUser> allUserList = sysUserService.list(
                new LambdaQueryWrapper<SysUser>().eq(StrUtil.isNotBlank(userId), SysUser::getId,userId)
        );
        //获取所有单位信息，并按id做key转换成map
        List<SysDepart> allDeptList = sysDepartService.list();

        Map<String, Object> result = new HashMap<>();
        result.put("allUserMap", CollStreamUtil.toMap(allUserList, SysUser::getId, bean -> bean));
        result.put("allDeptMap", CollStreamUtil.toMap(allDeptList, SysDepart::getId, bean -> bean));
        return result;
    }

    /**
     * 计算百分比
     */
    private double calcRatio(double numA, double numB){
        double result = 0.0;
        if(numA != 0 && numB != 0){
            result = numA / numB * 100;
        }
        return result;
    }
    //截取部门编码
    private String getSubDept(String deptId, Map<String, SysDepart> allDeptMap){
        if(StrUtil.isBlank(deptId)){
            return "";
        }
        String deptCode = allDeptMap.get(deptId).getOrgCode();
        if(deptCode.length() <= 12){
            return deptCode;
        }
        return deptCode.substring(0, 12);
    }

    @Override
    public Map<String, Object> empRaFrom(EmpRaFromDto dto) {
        Map<String, Object> map = new HashMap<>(16);
        List<String> showType = dto.getShowType();
        // 提出
        if (showType.contains("put")) {
            List<ActionPlanTrack> putTracks = lambdaQueryTrackPut(dto);
            map.put("put", CollUtil.isNotEmpty(putTracks) ? findEmpRaDataP(putTracks) : new EmpRaFromVo());
        }
        // 责任
        if (showType.contains("dut")) {
            List<ActionPlanTrack> dutTracks = lambdaQueryTrackDut(dto);
            map.put("dut", CollUtil.isNotEmpty(dutTracks) ? findEmpRaDataP(dutTracks) : new EmpRaFromVo());
        }
        // 确认人
        if (showType.contains("verify")) {
            List<ActionPlanTrack> verifyTracks = lambdaQueryTrackVerify(dto);
            map.put("verify", CollUtil.isNotEmpty(verifyTracks) ? findEmpRaDataP(verifyTracks) : new EmpRaFromVo());
        }
        // 执行人
        if (showType.contains("execute")) {
            List<ActionPlanTrack> executeTracks = lambdaQueryTrackExecute(dto);
            map.put("execute", CollUtil.isNotEmpty(executeTracks) ? findEmpRaDataP(executeTracks) : new EmpRaFromVo());
        }
        return map;
    }

    /**
     * 根据条件查询行动计划轨迹信息
     *
     * @param dto 包含查询条件的Dto对象
     * @return 包含符合条件的行动计划轨迹信息的列表
     */
    public List<ActionPlanTrack> lambdaQueryTrackPut(EmpRaFromDto dto) {
        return lambdaQueryTrack(dto, (q, userId) -> q.eq(ActionPlanTrack::getPutUserId, userId));
    }
    public List<ActionPlanTrack> lambdaQueryTrackDut(EmpRaFromDto dto) {
        return lambdaQueryTrack(dto, (q, userId) -> q.eq(ActionPlanTrack::getDutyUserId, userId));
    }
    public List<ActionPlanTrack> lambdaQueryTrackVerify(EmpRaFromDto dto) {
        //默认查询待确认的
        EmpRaFromDto empRaFromDto = BeanUtil.copyProperties(dto, EmpRaFromDto.class);
        empRaFromDto.setState(CollUtil.toList(0, 2));
        return lambdaQueryTrack(empRaFromDto, (q, userId) -> q.eq(ActionPlanTrack::getVerifyUserId, userId)
                .eq(ActionPlanTrack::getFinishVerifyState, "1"));
    }
    public List<ActionPlanTrack> lambdaQueryTrackExecute(EmpRaFromDto dto) {
        return lambdaQueryTrack(dto, (q, userId) -> q.eq(ActionPlanTrack::getExecuteUserId, userId));
    }

    /**
     * 根据条件查询行动计划跟踪记录列表
     *
     * @param dto 查询条件封装对象
     * @param additionalConditions 额外的查询条件，需要调用者传入一个BiConsumer对象，用于在查询条件中添加自定义条件
     * @return 满足条件的行动计划跟踪记录列表
     */
    public List<ActionPlanTrack> lambdaQueryTrack(EmpRaFromDto dto, BiConsumer<LambdaQueryWrapper<ActionPlanTrack>, String> additionalConditions) {
        LambdaQueryWrapper<ActionPlanTrack> query = new LambdaQueryWrapper<>();
        query.likeRight(ActionPlanTrack::getFactoryCode, dto.getFactory());
        additionalConditions.accept(query, dto.getUserId());
        query.eq(ActionPlanTrack::getStatus, 1);
        if (StringUtils.hasLength(dto.getStateTime()) && StringUtils.hasLength(dto.getEddTime())) {
            query.between(ActionPlanTrack::getPlanFinishDate, dto.getStateTime(), dto.getEddTime());
        }
        if (CollUtil.isNotEmpty(dto.getState())) {
            query.in(ActionPlanTrack::getFinishState, dto.getState());
        }
        if (nonNull(dto.getSurplusDay())) {
            query.le(ActionPlanTrack::getSurplusDay, dto.getSurplusDay());
        }
        return list(query);
    }

    /**
     * 根据行动计划跟踪列表，查找员工绩效评估相关数据
     *
     * @param tracks 行动计划跟踪列表
     * @return 包含员工绩效评估相关数据的 EmpRaFromVo 对象
     */
    public EmpRaFromVo findEmpRaDataP(List<ActionPlanTrack> tracks) {
        EmpRaFromVo vo = new EmpRaFromVo();
        Map<String, Long> stateCountMap = tracks.stream().collect(Collectors.groupingBy(ActionPlanTrack::getFinishState, Collectors.counting()));
        //正在进行
        vo.setActionIng(stateCountMap.getOrDefault("0", 0L).intValue());
        //已完成
        vo.setActionOk(stateCountMap.getOrDefault("1", 0L).intValue());
        //延迟
        vo.setDeferCount(stateCountMap.getOrDefault("2", 0L).intValue());
        //取消
        vo.setCloseCount(stateCountMap.getOrDefault("3", 0L).intValue());
        //延迟完成
        vo.setDeferOkCount(stateCountMap.getOrDefault("4", 0L).intValue());
        //内容
        vo.setTracks(tracks);
        return vo;
    }
}
