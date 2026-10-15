package com.keytech.modules.plan.service.impl;

import cn.hutool.core.collection.CollStreamUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.keytech.base.service.impl.BaseServiceImpl;
import com.keytech.modules.mdm3.entity.*;
import com.keytech.modules.mdm3.service.*;
import com.keytech.modules.plan.entity.Plan;
import com.keytech.modules.plan.entity.PlanDevice;
import com.keytech.modules.plan.entity.PlanPause;
import com.keytech.modules.plan.enums.PlanStatusEnum;
import com.keytech.modules.plan.mapper.PlanMapper;
import com.keytech.modules.plan.service.IPlanDeviceService;
import com.keytech.modules.plan.service.IPlanPauseService;
import com.keytech.modules.plan.service.IPlanService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.config.GlobalErrorEnum;
import org.jeecg.common.exception.JeecgBootException;
import org.jeecg.common.system.query.QueryGenerator;
import org.jeecg.common.util.ImportExcelUtil;
import org.jeecg.modules.system.entity.SysDepart;
import org.jeecg.modules.system.service.ISysDepartService;
import org.jeecgframework.poi.excel.ExcelImportUtil;
import org.jeecgframework.poi.excel.entity.ImportParams;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * &#064;Description:  工单管理
 * &#064;Author:  jeecg-boot
 * &#064;Date:    2025-06-06
 * &#064;Version:  V1.0
 */
@Service
@Slf4j
public class PlanServiceImpl extends BaseServiceImpl<PlanMapper, Plan> implements IPlanService {
    @Autowired
    private IPlanDeviceService planDeviceService;
    @Autowired
    private IMdmPostDeviceService mdmPostDeviceService;
    @Autowired
    private ISysDepartService sysDepartService;
    @Autowired
    private IMdmDictService mdmDictService;
    @Autowired
    private IMdmDeviceService deviceService;
    @Autowired
    private IPlanPauseService planPauseService;
    @Autowired
    private IMdmPostService mdmPostService;
    @Autowired
    private IMdmTaskService mdmTaskService;

    @Override
    public Result<IPage<Plan>> queryPageList(Plan plan, Integer pageNo, Integer pageSize, String orderField, String orderType, String selectFields, HttpServletRequest req) {
        //先通过岗位获取机台计划、再通过机台计划获取工单
        if(StrUtil.isNotBlank(plan.getPostIds())){
            List<PlanDevice> planDeviceList = planDeviceService.lambdaQuery()
                    .in(PlanDevice::getPostId, Collections.singletonList(plan.getPostIds()))
                    .eq(PlanDevice::getLineCode, plan.getLineCode())
                    .list();
            String planIds = planDeviceList.stream()
                    .map(PlanDevice::getPlanId) // 取出每个 planId 转为字符串
                    .collect(Collectors.joining(",")); // 用逗号拼接
            plan.setId(planIds);
        }
        //如果需要查询结束状态工单，清空掉值，避免initQueryWrapper重复查询
        boolean isFinished = false;
        if(null != plan.getPlanStatus() && PlanStatusEnum.FINISHED.code == plan.getPlanStatus()){
            plan.setPlanStatus(null);
            isFinished = true;
        }
        QueryWrapper<Plan> queryWrapper = QueryGenerator.initQueryWrapper(plan, req.getParameterMap());
        queryWrapper.in(isFinished, "plan_status", Arrays.asList(PlanStatusEnum.FINISHED.code, PlanStatusEnum.DELAY.code));
        if(StrUtil.isNotBlank(plan.getTaskItemName())){
            //根据任务项名称查询任务
            List<MdmTask> mdmTaskList =mdmTaskService.lambdaQuery()
                    .exists("select 1 from mes3_mdm_task_item a where a.del_flag = 0 " +
                            " and a.task_id = mes3_mdm_task.id " +
                            " and a.item_name like '%"+plan.getTaskItemName()+"%' ")
                    .list();
            if(CollUtil.isEmpty(mdmTaskList)){
                return null;
            }
            //获取任务中的所有id
            Set<Integer> uniqueDeviceIds = mdmTaskList.stream()
                    .map(MdmTask::getDeviceId)
                    .collect(Collectors.toSet());
            List<PlanDevice> planDeviceList = planDeviceService.lambdaQuery()
                    .in(PlanDevice::getDeviceId, uniqueDeviceIds)
                    .list();
            if(CollUtil.isEmpty(planDeviceList)){
                return null;
            }
            //获取任务中的所有id
            Set<String> uniquePlanIds = planDeviceList.stream()
                    .map(PlanDevice::getPlanId)
                    .collect(Collectors.toSet());
            queryWrapper.in("id", uniquePlanIds);
        }

        Page<Plan> page = new Page<>(pageNo, pageSize);
        IPage<Plan> pageList = this.page(page, queryWrapper);
        return Result.OK(pageList);
    }

    @Override
    @Transactional
    public Result<String> savePlan(Plan plan) {
        //根据条件查询计划数据
        List<Plan> list = this.lambdaQuery()
                .eq(Plan::getOrderName, plan.getOrderName())
                .or(StrUtil.isNotBlank(plan.getId()))
                .eq(StrUtil.isNotBlank(plan.getId()), Plan::getId, plan.getId())
                .list();
        for(Plan p : list){
            //如果是修改工单需要判断是否是未开始状态
            if(StrUtil.isNotBlank(plan.getId()) && p.getPlanStatus() != PlanStatusEnum.TODO.code){
                throw new JeecgBootException("工单不是未开始状态无法修改");
            }
            //传参数据与数据库查询数据的id不同，但是订单号相同，则不能保存
            if(!p.getId().equals(plan.getId()) && p.getOrderName().equals(plan.getOrderName())){
                throw new JeecgBootException("订单号("+plan.getOrderName()+")已存在");
            }
        }

        //拆分工单中的设备
        Integer[] deviceIds = Arrays.stream(plan.getDeviceIds().split(","))
                .map(Integer::parseInt)
                .toArray(Integer[]::new);

        //获取设备
        List<MdmDevice> mdmDeviceList = deviceService.lambdaQuery()
                .in(MdmDevice::getId, (Object[]) deviceIds)
                .list();
        Map<Integer, String> deviceMap = mdmDeviceList.stream()
                .collect(Collectors.toMap(
                        MdmDevice::getId,
                        MdmDevice::getDeviceName,
                        (existing, replacement) -> replacement
                ));

        //查找是否有时间重叠的工单
        String existsSql = "select 1 from mes3_plan plan where plan.del_flag = 0 and plan.plan_status not in(3, 4, 7) and plan_id = plan.id and plan.plan_type = '"+plan.getPlanType()+"'";
        if(null != plan.getPlanTypeSub()){
            existsSql = existsSql + " and plan.plan_type_sub = '"+plan.getPlanTypeSub()+"'";
        }
        List<PlanDevice> existsList = planDeviceService.lambdaQuery()
                .ne(StrUtil.isNotBlank(plan.getId()), PlanDevice::getPlanId, plan.getId())  //如果有id排除自身
                .in(PlanDevice::getDeviceId, (Object[]) deviceIds) //设备id
                .ge(PlanDevice::getPlanEndTime, plan.getPlanStartTime())    //重叠时间判断查询
                .le(PlanDevice::getPlanStartTime, plan.getPlanEndTime())
                .notIn(PlanDevice::getPlanStatus, Arrays.asList(PlanStatusEnum.FINISHED.code, PlanStatusEnum.CANCEL.code, PlanStatusEnum.DELAY.code))
                .exists(existsSql)  //工单类型查询
                .list();
        if(CollUtil.isNotEmpty(existsList)){
            //如果查出来数据，则拼接重叠时间得设备
            StringBuilder deviceName = new StringBuilder();
            for(MdmDevice m : mdmDeviceList){
                for(PlanDevice p : existsList){
                    if(Objects.equals(m.getId(), p.getDeviceId())){
                        deviceName.append(m.getDeviceName()).append("、");
                        break;
                    }
                }
            }
            throw new JeecgBootException("以下设备在该计划时间范围内已有其他相同类型的计划【"+deviceName+"】。");
        }

        //如果有id，则进行特殊处理
        if(StrUtil.isNotBlank(plan.getId())){
            //删除已存在的机台计划
            planDeviceService.remove(new LambdaQueryWrapper<PlanDevice>().eq(PlanDevice::getPlanId, plan.getId()));
        } else {
            plan.setId(IdUtil.getSnowflakeNextIdStr());
            plan.setYmd(plan.getPlanStartTime());
        }

        //根据设备id获取岗位设备关联数据
        List<MdmPostDevice> postDeviceList = mdmPostDeviceService.lambdaQuery()
                .in(MdmPostDevice::getDeviceId, (Object[]) deviceIds)
                .list();
        //如果没有绑定了岗位的设备就不添加机台计划
        if(CollUtil.isNotEmpty(postDeviceList)){
            //将关联数据转换成map形式
            Map<Integer, MdmPostDevice> postDeviceMap = postDeviceList.stream()
                    .collect(Collectors.toMap(
                            MdmPostDevice::getDeviceId,
                            device -> device
                    ));

            //组装机台计划数据
            List<PlanDevice> planDeviceList = new ArrayList<>();
            StringBuilder errMsg = new StringBuilder();
            for (Integer deviceId : deviceIds) {
                if (!postDeviceMap.containsKey(deviceId)) {
                    errMsg.append(deviceMap.get(deviceId)).append("、");
                    continue;
                }

                PlanDevice planDevice = new PlanDevice();
                planDevice.setFactoryCode(plan.getFactoryCode());
                planDevice.setDeptCode(plan.getDeptCode());
                planDevice.setLineCode(plan.getLineCode());
                planDevice.setPlanId(plan.getId());
                planDevice.setDeviceId(deviceId);
                planDevice.setPostId(postDeviceMap.get(deviceId).getPostId());
                planDevice.setMainDeviceId(postDeviceMap.get(deviceId).getMainDeviceId());
                planDevice.setPlanStartTime(plan.getPlanStartTime());
                planDevice.setPlanEndTime(plan.getPlanEndTime());
                planDeviceList.add(planDevice);
            }
            if(StrUtil.isNotBlank(errMsg.toString())){
                throw new JeecgBootException("【"+errMsg.substring(0, errMsg.length() - 1)+"】未绑定岗位");
            }else{
                //添加工单
                this.saveOrUpdate(plan);
                planDeviceService.saveBatch(planDeviceList);
            }
        } else {
            throw new JeecgBootException("所选设备未绑定岗位");
        }
        return Result.OK(GlobalErrorEnum.SUCCESS.getCode());
    }

    @Override
    @Transactional
    public Result<String> deleteData(String ids) {
        //拆分id
        List<String> idList = Arrays.asList(ids.split(","));
        //查询非未开始的计划
        List<Plan> list = this.lambdaQuery().in(Plan::getId, idList)
                .ne(Plan::getPlanStatus, PlanStatusEnum.TODO.code)
                .list();
        if(CollUtil.isNotEmpty(list)){
            String msg = list.stream()
                    .map(Plan::getOrderName)
                    .filter(Objects::nonNull) // 如果需要处理可能存在的null值
                    .collect(Collectors.joining("、"));
            throw new JeecgBootException("以下订单号【"+msg+"】状态不是未开始，无法删除！");
        }
        //删除计划
        this.removeBatchByIds(idList);
        //删除机台计划
        planDeviceService.lambdaUpdate()
                .set(PlanDevice::getDelFlag, 1)
                .in(PlanDevice::getPlanId, idList)
                .update();
        return Result.OK(GlobalErrorEnum.SUCCESS.getCode());
    }

    @Override
    @Transactional
    public Result<?> importData(HttpServletRequest request, HttpServletResponse response) throws IOException {
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
            params.setHeadRows(1);
            try {
                //获取上传文件中的数据
                List<Plan> list = ExcelImportUtil.importExcel(file.getInputStream(), Plan.class, params);
                //所有单位信息
                List<SysDepart> allDeptList = sysDepartService.list();
                Map<String, SysDepart> allDeptMap = CollStreamUtil.toMap(allDeptList, SysDepart::getOrgCode, bean -> bean);
                //获取计划管理字典
                List<MdmDict> mdmDictList = mdmDictService.getDictAllOrByGroupCode(
                        List.of(
                                "MES3_PLAN_TYPE_SUB"
                                , "MES3_PLAN_TYPE"
                        )
                );
                //获取管理岗位
                List<MdmPost> mdmPostList = mdmPostService.lambdaQuery().eq(MdmPost::getPostType, 1).list();
                Map<Integer, MdmPost> postMap = CollStreamUtil.toMap(mdmPostList, MdmPost::getId, bean -> bean);

                //字典数据map集合，key为groupCode-itemVal,value为对象信息
                Map<String, MdmDict> dictMap = mdmDictList.stream()
                        .collect(Collectors.toMap(
                                item -> item.getGroupCode() + "-" + item.getItemVal(),
                                item -> item
                        ));
                int lineNumber = 2;
                for(Plan plan : list){
                    lineNumber++;
                    if(StrUtil.isBlank(plan.getFactoryCode()) || !allDeptMap.containsKey(plan.getFactoryCode())){
                        errorMessage.add("第" + lineNumber + "行，工厂不存在");
                        errorLines++;
                        continue;
                    }
                    if(StrUtil.isBlank(plan.getDeptCode()) || !allDeptMap.containsKey(plan.getDeptCode())){
                        errorMessage.add("第" + lineNumber + "行，部门不存在");
                        errorLines++;
                        continue;
                    }
                    if(StrUtil.isBlank(plan.getLineCode()) || !allDeptMap.containsKey(plan.getLineCode())){
                        errorMessage.add("第" + lineNumber + "行，产线不存在");
                        errorLines++;
                        continue;
                    }
                    if(StrUtil.isBlank(plan.getDeviceIds())){
                        errorMessage.add("第" + lineNumber + "行，设备不存在");
                        errorLines++;
                        continue;
                    }
                    if(StrUtil.isBlank(plan.getTriggerPostId()) || !postMap.containsKey(Integer.valueOf(plan.getTriggerPostId()))){
                        errorMessage.add("第" + lineNumber + "行，计划操作岗位不存在");
                        errorLines++;
                        continue;
                    }
                    String[] deviceIds = plan.getDeviceIds().split(",");
                    List<MdmDevice> deviceList = deviceService.lambdaQuery()
                            .eq(MdmDevice::getLineCode, plan.getLineCode())
                            .in(MdmDevice::getId, (Object[]) deviceIds).list();
                    if(CollUtil.isEmpty(deviceList) || deviceList.size() < deviceIds.length){
                        errorMessage.add("第" + lineNumber + "行，设备不存在");
                        errorLines++;
                        continue;
                    }
                    plan.setDeviceIds(deviceList.stream()
                            .map(MdmDevice::getId) // 提取每个MdmDevice对象的id
                            .map(String::valueOf)  // 将id转换为String类型
                            .collect(Collectors.joining(",")));
                    if(StrUtil.isBlank(plan.getOrderName())){
                        errorMessage.add("第" + lineNumber + "行，订单号不能为空");
                        errorLines++;
                        continue;
                    }
                    if(StrUtil.isBlank(plan.getPlanName())){
                        errorMessage.add("第" + lineNumber + "行，计划名不能为空");
                        errorLines++;
                        continue;
                    }
                    if(null == plan.getPlanStartTime()){
                        errorMessage.add("第" + lineNumber + "行，预计开始时间不能为空");
                        errorLines++;
                        continue;
                    }
                    plan.setYmd(plan.getPlanStartTime());
                    if(null == plan.getPlanEndTime()){
                        errorMessage.add("第" + lineNumber + "行，预计结束时间不能为空");
                        errorLines++;
                        continue;
                    }
                    if(null == plan.getPlanType() || !dictMap.containsKey("MES3_PLAN_TYPE-"+plan.getPlanType())){
                        errorMessage.add("第" + lineNumber + "行，计划大类不存在");
                        errorLines++;
                        continue;
                    }
                    if(2 == plan.getPlanType()){
                        if(!dictMap.containsKey("MES3_PLAN_TYPE_SUB-"+plan.getPlanTypeSub())){
                            errorMessage.add("第" + lineNumber + "行，计划细类不存在");
                            errorLines++;
                            continue;
                        }
                    }
                    successLines++;
                    savePlan(plan);
                }
            } catch (Exception e) {
                log.error(e.getMessage(), e);
                errorLines++;
                errorMessage.add("发生异常：" + e.getMessage());
            } finally {
                try {
                    file.getInputStream().close();
                } catch (IOException e) {
                    log.error("IOException", e);
                }
            }
        }
        return ImportExcelUtil.imporReturnRes(errorLines, successLines, errorMessage);
    }

    @Override
    @Transactional
    public void startPlan(Plan plan) {
        Date now = new Date();
        //根据id查询未开始的计划
        List<Plan> planList = this.lambdaQuery()
                .eq(Plan::getPlanStatus, PlanStatusEnum.TODO.code)
                .eq(Plan::getId, plan.getId())
                .list();
        //如果有未开始的主计划，则开启主计划
        if(CollUtil.isNotEmpty(planList)){
            plan.setRealStartTime(now);
            plan.setRecoveryTime(now);
            plan.setPlanStatus(PlanStatusEnum.DOING.code);
            //设置计划状态为进行中
            this.updateById(plan);

            //开启机台计划
            planDeviceService.startPlanDevice(plan, now);
        }
    }

    @Override
    public void endPLan(String planId, Date now, Boolean isDelayClose) {
        //查找进行中的机台计划
        List<PlanDevice> planDeviceList = planDeviceService.lambdaQuery()
                .eq(PlanDevice::getPlanId, planId)
                .in(PlanDevice::getPlanStatus, Arrays.asList(PlanStatusEnum.DOING.code, PlanStatusEnum.DELAY.code)                                                        )
                .list();
        //如果没有进行中的机台计划，则关闭主计划
        if(CollUtil.isEmpty(planDeviceList)){
            this.lambdaUpdate()
                    .set(!isDelayClose, Plan::getRealEndTime, now)
                    .set(Plan::getPlanStatus, PlanStatusEnum.FINISHED.code)
                    .eq(Plan::getId, planId)
                    .update();
        }
    }

    @Override
    @Transactional
    public void pauseOrRenew(Plan plan) {
        Date now = new Date();
        PlanPause planPause = new PlanPause();
        planPause.setFactoryCode(plan.getFactoryCode());
        planPause.setDeptCode(plan.getDeptCode());
        planPause.setLineCode(plan.getLineCode());
        planPause.setPlanId(plan.getId());
        //如果是进行中则暂停计划
        if(plan.getPlanStatus() == PlanStatusEnum.DOING.code){
            plan.setPlanStatus(PlanStatusEnum.PAUSE.code);
            planPause.setType(PlanStatusEnum.PAUSE.code);
        }
        //如果是暂停中则开始计划
        else if(plan.getPlanStatus() == PlanStatusEnum.PAUSE.code){
            plan.setPlanStatus(PlanStatusEnum.DOING.code);
            planPause.setType(PlanStatusEnum.DOING.code);
            plan.setRecoveryTime(now);
        }
        //创建机台计划工单
        planDeviceService.pauseOrRenewPlanDevice(plan, planPause.getType(), now);
        //修改计划
        this.updateById(plan);
        //修改机台计划
        planDeviceService.lambdaUpdate()
                .set(PlanDevice::getPlanStatus, plan.getPlanStatus())
                .set(PlanDevice::getRecoveryTime, now)
                .eq(PlanDevice::getPlanId, plan.getId())
                .update();
        //保存暂停或恢复记录
        planPauseService.save(planPause);
    }
}
