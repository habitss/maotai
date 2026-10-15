package com.keytech.modules.pi.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.keytech.modules.pi.common.PiCommonConstants;
import com.keytech.modules.pi.dto.TableConditionDto;
import com.keytech.modules.pi.dto.TableConditionQueryDto;
import com.keytech.modules.pi.dto.TableFieldValueDto;
import com.keytech.modules.pi.dto.TableSaveDto;
import com.keytech.modules.pi.entity.*;
import com.keytech.modules.pi.mapper.PiApproveMapper;
import com.keytech.modules.pi.service.*;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.SecurityUtils;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.config.GlobalErrorEnum;
import org.jeecg.common.exception.JeecgBootException;
import org.jeecg.common.system.vo.LoginUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.keytech.base.service.impl.BaseServiceImpl;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @Description: pi报表-报表审批
 * @Author: jeecg-boot
 * @Date:   2025-01-09
 * @Version: V1.0
 */
@Service
public class PiApproveServiceImpl extends BaseServiceImpl<PiApproveMapper, PiApprove> implements IPiApproveService {

    @Resource
    private IPiCustomizeTableService iPiCustomizeTableService;
    @Autowired
    private IPiAllocationTableFieldService iPiAllocationTableFieldService;
    @Resource
    private IPiCustomizeTableFieldService iPiCustomizeTableFieldService;

    @Resource
    private IPiStandardDataService iPiStandardDataService;
    @Autowired
    private IPiAllocationTableService iPiAllocationTableService;
    @Autowired
    private IPiCalculateSignalService iPiCalculateSignalService;
    @Resource
    private IPiComputationModelService piComputationModelService;

    /**
     * 修改/剔除申请
     * @param piApprove
     */
    @Override
    public Result<?> approveSave(PiApprove piApprove) {
        //是否有PI日报间计算(类型为修改)
//        if(PiCommonConstants.INSTANT.equals(piApprove.getType()) &&
//        iPiCalculateSignalService.isPiOtherCalculation(piApprove.getTableName(),piApprove.getFields())) {
//            //存在PI间计算
//            return Result.error(PiCommonConstants.CANNOT_EDIT);
//        }

        String type = piApprove.getType();
        LoginUser user = (LoginUser) SecurityUtils.getSubject().getPrincipal();
        //提交人信息
        piApprove.setModifyBy(user.getUsername());
        piApprove.setModifyTime(new Date());
        //判断算法管理员
        boolean isAdmin = false;
        if("admin".equals(user.getUsername()) || "fadmin".equals(user.getUsername())){
            isAdmin = true;
            //审批状态-审批通过
            piApprove.setState("1");
        }else{
            //审批状态-待审批
            piApprove.setState("0");
        }

        if("0".equals(type)){
            //修改(管理员)
            if(isAdmin){
                //更新值
                updateData(piApprove);
            }
        }else{
            //剔除,判断是否剔除整行
            String range = piApprove.getRemoveRange();
            if("0".equals(range)){
                //剔除整行
                //获取pi字段
                List<String> fields = new ArrayList<>();
                //pi报表id
                String allocationId = piApprove.getAllocationId();
                if(StrUtil.isNotBlank(allocationId)){
                    //pi报表
                    LambdaQueryWrapper<PiAllocationTableField> queryWrapper = new LambdaQueryWrapper<>();
                    queryWrapper.eq(PiAllocationTableField::getAllocationTableId, allocationId);
                    List<PiAllocationTableField> piFieldlist = iPiAllocationTableFieldService.list(queryWrapper);
                    fields = piFieldlist.stream().map(PiAllocationTableField::getFieldName).collect(Collectors.toList());
                }else{
                    //基本报表
                    LambdaQueryWrapper<PiCustomizeTableField> queryUniqueWrapper = new LambdaQueryWrapper<>();
                    queryUniqueWrapper.eq(PiCustomizeTableField::getTableId, piApprove.getTableId());
                    List<PiCustomizeTableField> baseFieldList = iPiCustomizeTableFieldService.list(queryUniqueWrapper);
                    fields = baseFieldList.stream().map(PiCustomizeTableField::getFieldName).collect(Collectors.toList());
                }
                if(null != fields && fields.size() > 0){
                    piApprove.setFields(String.join(",", fields));
                }
            }

            if(isAdmin){
                //管理员-直接剔除
                String fields = piApprove.getFields();
                String[] strArray = fields.split(",");
                List<String> piCodeList = Arrays.stream(strArray)
                        .collect(Collectors.toList());
                iPiStandardDataService.setPiColorToGrey(piApprove.getTableName(),piCodeList,piApprove.getDataId());
            }
        }
        if (piApprove.getAllocationId() != null && piApprove.getAllocationId().trim().isEmpty()) {
            piApprove.setAllocationId(null);
        }
        //写入审批表
        save(piApprove);
        return Result.OK(GlobalErrorEnum.SUCCESS.getCode());
    }




    /**
     * 审批-修改/剔除
     * @param piApprove
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void approve(PiApprove piApprove) {
        //人员信息
        LoginUser user = (LoginUser) SecurityUtils.getSubject().getPrincipal();
        piApprove.setApproveBy(user.getUsername());
        piApprove.setApproveTime(new Date());
        if("0".equals(piApprove.getType())){
            //修改
            if("1".equals(piApprove.getState())){
                //审批通过
                //更新审批表(更新数据)
                updateData(piApprove);
            }
            updateById(piApprove);
        }else{
            //剔除
            if("1".equals(piApprove.getState())){
                //审批通过
                PiApprove approveData = getById(piApprove.getId());
                String fields = approveData.getFields();
                String[] strArray = fields.split(",");
                List<String> piCodeList = Arrays.stream(strArray)
                        .collect(Collectors.toList());
                iPiStandardDataService.setPiColorToGrey(approveData.getTableName(),piCodeList,approveData.getDataId());
            }
            //更新审批表(更新数据)
            updateById(piApprove);
        }
    }


    /**
     * 匹配字段
     * @param pageList
     * @return
     */
    @Override
    public IPage<PiApprove> matchData(IPage<PiApprove> pageList) {
        //获取字段名称
        LambdaQueryWrapper<PiCustomizeTableField> queryWrapperMaintenance = Wrappers.lambdaQuery(PiCustomizeTableField.class);
        List<PiCustomizeTableField> customizeTableFieldList = iPiCustomizeTableFieldService.list(queryWrapperMaintenance);

        //将列表转换为 Map
        Map<String, String> resultMap = customizeTableFieldList.stream()
             .filter(field -> StrUtil.isNotBlank(field.getTableName()))
            .collect(Collectors.toMap(
                    field -> field.getTableName() + "_" + field.getFieldName(),
                    field -> field.getName()
            ));

        //获取当前数据集合
        List<PiApprove> approveList = pageList.getRecords();

        //获取唯一约束
        // 根据 tableName 分组，并筛选 isUnique 为 1 的记录
        Map<String, List<PiCustomizeTableField>> groupedByTableName = customizeTableFieldList.stream()
                // 筛选 isUnique 为 1 的记录
                .filter(field -> Integer.valueOf(1).equals(field.getIsUnique()))
                // 按 tableName 分组
                .collect(Collectors.groupingBy(
                        PiCustomizeTableField::getTableName
                ));

        //根据根据 tableName 分组获取dataId获取数据集合
        Map<String, String> tableDataIds = approveList.stream()
                .collect(Collectors.groupingBy(
                        PiApprove::getTableName,
                        Collectors.mapping(
                                PiApprove::getDataId,
                                Collectors.collectingAndThen(
                                        Collectors.toList(),
                                        list -> list.stream().filter(Objects::nonNull).distinct().collect(Collectors.joining(","))
                                )
                        )
                ));

        //按照tableName，DataId 获取当前的唯一约束集合
        Map<String, String> uniqueConstraints = new HashMap<>();

        //没有唯一约束的表
        List<String> noUniqueList = new ArrayList<>();

        for (Map.Entry<String, String> entry : tableDataIds.entrySet()) {
            String tableName = entry.getKey();
            String dataIds = entry.getValue();

            String query = " id in (" + dataIds + ") ";

            TableConditionQueryDto tableConditionQueryDto = new TableConditionQueryDto();
            if(!groupedByTableName.containsKey(tableName)){
                noUniqueList.add(tableName);
                continue;
            }
            //默认查询所有的
            List<String> evenIndexNumbers = groupedByTableName.get(tableName).stream().map(PiCustomizeTableField::getFieldName).collect(Collectors.toList());

            tableConditionQueryDto.setFieldList(evenIndexNumbers);

            tableConditionQueryDto.setTable(tableName);
            tableConditionQueryDto.setCondition(query);
            //当前表中所有的符合要求的数据
            List<Map<String,Object>> piData = iPiCustomizeTableService.getTableByCondition(tableConditionQueryDto);

            //循环数据
            for (Map<String, Object> dataMap : piData){
                String uniqueStr = "";
                //循环唯一约束，将唯一约束添加到字符串中
                for (PiCustomizeTableField field : groupedByTableName.get(tableName)) {
                    String data = Objects.isNull(dataMap.get(field.getFieldName()))? "" : dataMap.get(field.getFieldName()).toString();
                    uniqueStr += field.getName() + ":" + piComputationModelService.changeDataFormat(data) + "；";
                }
                if (uniqueStr.endsWith( "；")){
                    uniqueStr = uniqueStr.substring(0, uniqueStr.length() - 1);
                }
                uniqueConstraints.put(tableName+"⊙"+dataMap.get("id"), uniqueStr);
            }
        }

        if(CollUtil.isNotEmpty(noUniqueList)){
            throw new JeecgBootException("以下PI报表没有唯一约束"+noUniqueList);
        }

        //匹配
        for (PiApprove piApprove : approveList) {
            String fieldsStr = piApprove.getFields();
            String[] fields = fieldsStr.split(",");
            String tableName = piApprove.getTableName();
            List<String> list = new ArrayList<>();
            for (String field : fields) {
                list.add(resultMap.get(tableName+"_"+field));
            }
            piApprove.setFieldName(StringUtils.join(list, ","));
            piApprove.setUniqueStr(uniqueConstraints.get(piApprove.getTableName()+"⊙"+piApprove.getDataId()));
        }
        return pageList;
    }

    /**
     * 更新数据
     * @param piApprove
     */
    public void updateData(PiApprove piApprove) {

        //条件
        List<TableConditionDto> conditionList = new ArrayList<>();
        TableConditionDto tableConditionDto = new TableConditionDto();
        tableConditionDto.setKey("id");
        tableConditionDto.setVal(piApprove.getDataId());
        conditionList.add(tableConditionDto);

        //字段
        List<TableFieldValueDto> fieldList = new ArrayList<>();
        TableFieldValueDto tableFieldValueDto = new TableFieldValueDto();
        tableFieldValueDto.setFieldVal(piApprove.getModifyValue());
        tableFieldValueDto.setField(piApprove.getFields());
        fieldList.add(tableFieldValueDto);

        //更新值
        TableSaveDto tableSaveDto = new TableSaveDto();
        tableSaveDto.setTable(piApprove.getTableName());
        tableSaveDto.setConditionList(conditionList);
        tableSaveDto.setFieldList(fieldList);
        //更新
        iPiCustomizeTableService.tableSave(tableSaveDto);


        //保存成功-更新PI间数据
        List<String> dataIds = Arrays.asList(piApprove.getDataId());
        List<String> piCodes = Arrays.asList(piApprove.getFields());
        iPiAllocationTableFieldService.savePICalculateSignal(dataIds, piApprove.getTableName(),piCodes);
    }

}
