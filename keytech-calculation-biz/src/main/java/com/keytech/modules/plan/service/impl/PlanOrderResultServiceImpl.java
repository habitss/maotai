package com.keytech.modules.plan.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.keytech.base.service.impl.BaseServiceImpl;
import com.keytech.modules.mdm3.entity.MdmItemSelection;
import com.keytech.modules.mdm3.service.IMdmItemSelectionService;
import com.keytech.modules.plan.dto.PlanOrderResultDto;
import com.keytech.modules.plan.dto.PlanOrderResultSaveDto;
import com.keytech.modules.plan.entity.PlanOrder;
import com.keytech.modules.plan.entity.PlanOrderResult;
import com.keytech.modules.plan.entity.PlanOrderResultConfig;
import com.keytech.modules.plan.enums.PlanOrderResultTypeEnum;
import com.keytech.modules.plan.mapper.PlanOrderResultMapper;
import com.keytech.modules.plan.service.IPlanOrderResultConfigService;
import com.keytech.modules.plan.service.IPlanOrderResultService;
import com.keytech.modules.plan.service.IPlanOrderService;
import com.keytech.modules.post.record.service.ITakeUpPostService;
import com.keytech.modules.post.take.entity.TakeUpPost;
import org.apache.shiro.SecurityUtils;
import org.jeecg.common.exception.JeecgBootException;
import org.jeecg.common.system.vo.LoginUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * &#064;Description:  计划工单-下发任务结果
 * &#064;Author:  jeecg-boot
 * &#064;Date:    2025-06-12
 * &#064;Version:  V1.0
 */
@Service
public class PlanOrderResultServiceImpl extends BaseServiceImpl<PlanOrderResultMapper, PlanOrderResult> implements IPlanOrderResultService {
    @Autowired
    private IPlanOrderResultConfigService planOrderResultConfigService;
    @Autowired
    private IMdmItemSelectionService iMdmItemSelectionService;
    @Autowired
    private ITakeUpPostService takeUpPostService;
    @Autowired
    private IPlanOrderService planOrderService;

    @Override
    public void removeTodoOrderResult(String planId, List<String> planDeviceIdList, Date now) {
        baseMapper.removeTodoOrderResult(planId, planDeviceIdList, now);
    }

    @Override
    public List<PlanOrderResultDto> queryByOrderId(String orderId, String resultConfigId) {
        //获取填报数据
        List<PlanOrderResult> planOrderResultList = this.lambdaQuery().eq(PlanOrderResult::getOrderId, orderId).list();
        //将数据转为map
        Map<Integer, PlanOrderResult> resultMap = planOrderResultList.stream()
                .collect(Collectors.toMap(
                        PlanOrderResult::getItemId,
                        item -> item
                ));
        //获取填报配置
        List<PlanOrderResultConfig> planOrderResultConfigList = planOrderResultConfigService.lambdaQuery()
                .eq(PlanOrderResultConfig::getResultConfigId, resultConfigId)
                .orderByAsc(PlanOrderResultConfig::getSort)
                .orderByAsc(PlanOrderResultConfig::getId)
                .list();
        //获取所有带下拉的条目id
        List<Integer> itemIdList = planOrderResultConfigList.stream()
                .filter(config ->
                        PlanOrderResultTypeEnum.TYPE_8.code.equals(config.getResultTypeCode())
                                || PlanOrderResultTypeEnum.TYPE_9.code.equals(config.getResultTypeCode())
                )
                .map(PlanOrderResultConfig::getItemId)
                .collect(Collectors.toList());

        Map<Integer, List<MdmItemSelection>> groupedByItemId = new HashMap<>();
        if(CollUtil.isNotEmpty(itemIdList)){
            //获取条目下拉配置数据
            List<MdmItemSelection> itemSelectionList = iMdmItemSelectionService.lambdaQuery()
                    .in(MdmItemSelection::getItemId, itemIdList)
                    .select(MdmItemSelection::getId, MdmItemSelection::getItemId, MdmItemSelection::getOptionName)
                    .list();
            if(CollUtil.isNotEmpty(itemSelectionList)){
                //根据条目id分组
                groupedByItemId = itemSelectionList.stream()
                        .collect(Collectors.groupingBy(MdmItemSelection::getItemId));
            }
        }
        //返回的数据
        List<PlanOrderResultDto> resultDtoList = new ArrayList<>();
        //拼接数据
        for(PlanOrderResultConfig config: planOrderResultConfigList){
            PlanOrderResultDto dto = new PlanOrderResultDto();
            //如果有填写过直接复制对象
            if(resultMap.containsKey(config.getItemId())){
                BeanUtil.copyProperties(resultMap.get(config.getItemId()), dto);
            }
            //开关类型默认选中
            if(PlanOrderResultTypeEnum.TYPE_1.code.equals(config.getResultTypeCode())){
                dto.setResult(StrUtil.isBlank(dto.getResult()) ? "true" : dto.getResult());
            }
            //如果需要判断是否合格、且没有填写过，则默认为合格
            if(1 == config.getIsQualified() && null == dto.getIsQualified()){
                dto.setIsQualified(1);
            }
            dto.setItemName(config.getItemName());
            dto.setItemStandard(config.getItemStandard());
            dto.setDetail(config.getDetail());
            dto.setItemId(config.getItemId());
            dto.setItemLocation(config.getItemLocation());
            dto.setIsCheckTime(config.getIsCheckTime());
            dto.setIsConfirmUser(config.getIsConfirmUser());
            dto.setIsRemark(config.getIsRemark());
            dto.setConfigIsQualified(config.getIsQualified());
            dto.setConfigRemark(config.getRemark());
            dto.setResultTypeCode(config.getResultTypeCode());
            dto.setSelectionList(groupedByItemId.getOrDefault(config.getItemId(), null));
            dto.setIsUploadImg(config.getIsUploadImg());
            dto.setIsRequire(config.getIsRequire());
            dto.setIsNa(config.getIsNa());
            resultDtoList.add(dto);
        }
        return resultDtoList;
    }

    @Override
    @Transactional
    public void saveOrUpdateDataBatch(PlanOrderResultSaveDto dto) {
        if(CollUtil.isEmpty(dto.getList())){
            return;
        }
        //判断是否更新为已完成，如果所有条目填写完毕则更新为已完成
        if(dto.getIsFinish()){
            //获取当前登录用户信息
            LoginUser sysUser = (LoginUser) SecurityUtils.getSubject().getPrincipal();
            //判断班组是否为空
            if(StrUtil.isBlank(dto.getList().get(0).getShiftGroupId())){
                //获取订单数据
                List<PlanOrder> planOrderList = planOrderService.lambdaQuery()
                        .eq(PlanOrder::getId, dto.getList().get(0).getOrderId())
                        .list();
                if(CollUtil.isEmpty(planOrderList)){
                    throw new JeecgBootException("任务不存在！");
                }
                PlanOrder planOrder = planOrderList.get(0);
                //获取上岗信息
                List<TakeUpPost> takeUpPostList = takeUpPostService.lambdaQuery()
                        .eq(TakeUpPost::getPostId, planOrder.getPostId())
                        .eq(TakeUpPost::getStatus, 0)
                        .eq(TakeUpPost::getUsername, sysUser.getUsername())
                        .list();
                if(CollUtil.isEmpty(takeUpPostList)){
                    throw new JeecgBootException("岗位信息不存在，请重新上岗！");
                }
                TakeUpPost takeUpPost = takeUpPostList.get(0);
                //设置班组班次信息
                dto.getList().forEach(item ->{
                    item.setShiftGroupId(takeUpPost.getTeam());
                    item.setShiftId(takeUpPost.getShiftId());
                });
            }
            planOrderService.lambdaUpdate()
                    .set(PlanOrder::getExecuteStatus, 4)
                    .set(PlanOrder::getExecutorId, sysUser.getId())
                    .set(PlanOrder::getRealEndTime, new Date())
                    .set(PlanOrder::getTeam, dto.getList().get(0).getShiftGroupId())
                    .set(PlanOrder::getShiftNum, dto.getList().get(0).getShiftId())
                    .eq(PlanOrder::getId, dto.getList().get(0).getOrderId())
                    .update();
        }
        this.saveOrUpdateBatch(dto.getList());
    }
}
