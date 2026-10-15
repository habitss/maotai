package com.keytech.modules.pi.service.impl;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.keytech.base.service.impl.BaseServiceImpl;
import com.keytech.modules.pi.dto.CopyStandardDto;
import com.keytech.modules.pi.dto.TableConditionDto;
import com.keytech.modules.pi.dto.TableFieldValueDto;
import com.keytech.modules.pi.dto.TableSaveDto;
import com.keytech.modules.pi.entity.PiCustomizeTableField;
import com.keytech.modules.pi.entity.PiStandardData;
import com.keytech.modules.pi.entity.PiStandardMaintenance;
import com.keytech.modules.pi.entity.PiStandardManage;
import com.keytech.modules.pi.mapper.PiCustomizeTableFieldMapper;
import com.keytech.modules.pi.service.IPiStandardDataService;
import com.keytech.modules.pi.service.IPiStandardMaintenanceService;
import com.keytech.modules.pi.mapper.PiStandardMaintenanceMapper;
import com.keytech.modules.pi.service.IPiStandardManageService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
* @Description: 针对表【mes3_pi_standard_maintenance(PI标准维护)】的数据库操作Service实现
* @Author: sjw
* @CreateDate: 2024/12/11 14:37:17
*/
@Service
public class PiStandardMaintenanceServiceImpl extends BaseServiceImpl<PiStandardMaintenanceMapper, PiStandardMaintenance>
    implements IPiStandardMaintenanceService {
    @Resource
    private PiCustomizeTableFieldMapper piCustomizeTableFieldMapper;

    @Resource
    private IPiStandardDataService iPiStandardDataService;
    @Resource
    private IPiStandardManageService iPiPiStandardManageService;

    @Override
    public List<PiStandardMaintenance> getPiStandardMaintenance(PiStandardMaintenance piStandardMaintenance) {
        //根据pi管理标准ID查询标准维护数据
        LambdaQueryWrapper<PiStandardMaintenance> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(PiStandardMaintenance::getManageId, piStandardMaintenance.getManageId());
        List<PiStandardMaintenance> maintenanceList = baseMapper.selectList(queryWrapper);

        //根据表名查询所有配置字段
        LambdaQueryWrapper<PiCustomizeTableField> fieldQueryWrapper = new LambdaQueryWrapper<>();
        fieldQueryWrapper.eq(PiCustomizeTableField::getTableName, piStandardMaintenance.getTableName());
        fieldQueryWrapper.orderByAsc(PiCustomizeTableField::getSort);
        List<PiCustomizeTableField> fieldList = piCustomizeTableFieldMapper.selectList(fieldQueryWrapper);

        //按配置字段排序拼接返回数据
        List<PiStandardMaintenance> resultList = new ArrayList<>();
        for(PiCustomizeTableField field : fieldList){
            boolean isNew = true;
            for(PiStandardMaintenance maintenance : maintenanceList){
                if(field.getFieldName().equals(maintenance.getFieldName())){
                    resultList.add(maintenance);
                    isNew = false;
                    break;
                }
            }
            if(isNew){
                PiStandardMaintenance maintenance = new PiStandardMaintenance();
                maintenance.setFieldText(field.getName());
                maintenance.setFieldName(field.getFieldName());
                maintenance.setTableName(field.getTableName());
                maintenance.setManageId(piStandardMaintenance.getManageId());
                resultList.add(maintenance);
            }
        }
        return resultList;
    }

    /**
     * 获取标准
     * @param piStandardMaintenance
     * @return
     */
    @Override
    public List<PiStandardMaintenance> queryStandard(PiStandardMaintenance piStandardMaintenance) {
        List<PiStandardMaintenance> list = new ArrayList<>();
        if(StrUtil.isNotBlank(piStandardMaintenance.getId())){
            //数据对应的标准
            //获取红黄区
            LambdaQueryWrapper<PiStandardData> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(PiStandardData::getDataId, piStandardMaintenance.getId());
            queryWrapper.eq(PiStandardData::getTableName, piStandardMaintenance.getTableName());
            List<PiStandardData> piStandardDataList = iPiStandardDataService.list(queryWrapper);
            if(null != piStandardDataList && piStandardDataList.size() > 0){
                //获取字段对应的标准id(maintenance_id)
                PiStandardData piStandardData = piStandardDataList.get(0);
                String standardJson = piStandardData.getStandardJson();
                if(StrUtil.isNotBlank(standardJson)){
                    JSONArray array = JSONUtil.parseArray(standardJson);
                    List<Map> mapList = JSONUtil.toList(array, Map.class);
                    String maintenanceId = "";
                    for (Map map : mapList) {
                        if(piStandardMaintenance.getFieldName().equals(map.get("field"))){
                            maintenanceId = map.get("maintenance_id")+"";
                            break;
                        }
                    }
                    if(StrUtil.isNotBlank(maintenanceId) && !"null".equals(maintenanceId)){
                        piStandardMaintenance.setId(maintenanceId);
                        list = piCustomizeTableFieldMapper.queryStandard(piStandardMaintenance);
                    }
                }
            }
        }else{
            //字段对应的所有标准
            list = piCustomizeTableFieldMapper.queryStandard(piStandardMaintenance);
        }
        return list;
    }

    /**
     * 批量编辑值
     *
     * @param maintenanceList 包含需要编辑的键值对的Map
     * @return 返回一个包含操作结果的Map，其中"code"字段表示操作结果，"0"表示成功
     */
    @Override
    public Map<String, String> batchEditVal(List<PiStandardMaintenance> maintenanceList) {
        Map<String,String> mapR = new HashMap<>();
        mapR.put("code","0");
        for (PiStandardMaintenance piStandardMaintenance : maintenanceList) {
            if(piStandardMaintenance.getId().startsWith("temp_")){
                piStandardMaintenance.setId(null);
            }
        }
        saveOrUpdateBatch(maintenanceList);
        return mapR;
    }

    /**
     * 批量删除数据
     *
     * @param data      要删除的数据，格式为JSON字符串
     * @return 删除成功返回true，否则返回false
     */
    @Override
    public boolean deleteDataBatch(String data) {
        boolean isOk = true;
        if(StrUtil.isNotBlank(data)){
            String[] ids = data.split(",");
            for (String id : ids) {
                removeById(id);
            }
        }
        return isOk;
    }

    /**
     * 复制标准
     *
     * @param copyStandardDto 复制的标准信息
     * @return 是否复制成功
     */
    @Override
    public boolean copyStandard(CopyStandardDto copyStandardDto) {
        try {
            //获取标准管理
            LambdaQueryWrapper<PiStandardManage> standardWrapper = new LambdaQueryWrapper<>();
            standardWrapper.eq(PiStandardManage::getCustomizeId, copyStandardDto.getTableId());

            List<PiStandardManage> standardList = iPiPiStandardManageService.list(standardWrapper);
            //标准主键id集合
            List<String> standardIdList = standardList.stream()
                    .map(PiStandardManage::getId)
                    .collect(Collectors.toList());
            if(standardIdList != null && standardIdList.size() > 0) {
                //获取目标字段配置的标准
                LambdaQueryWrapper<PiStandardMaintenance> targetWrapper = new LambdaQueryWrapper<>();
                targetWrapper.in(PiStandardMaintenance::getManageId, standardIdList);
                targetWrapper.eq(PiStandardMaintenance::getFieldName, copyStandardDto.getTargetField());
                List<PiStandardMaintenance> targetStandardList = list(targetWrapper);

                //排除
                List<String> filteredList = standardIdList.stream()
                        .filter(id -> targetStandardList.stream()
                                .noneMatch(target -> target.getManageId().equals(id)))
                        .collect(Collectors.toList());

                if(filteredList != null && filteredList.size() > 0){
                    //获取标准表(源字段配置标准集合, 排除目标表配置标准)
                    LambdaQueryWrapper<PiStandardMaintenance> sourceWrapper = new LambdaQueryWrapper<>();
                    sourceWrapper.in(PiStandardMaintenance::getManageId, filteredList);
                    sourceWrapper.eq(PiStandardMaintenance::getFieldName, copyStandardDto.getSourceField());
                    List<PiStandardMaintenance> standardMainList = list(sourceWrapper);

                    for (PiStandardMaintenance piStandardMaintenance : standardMainList) {
                        piStandardMaintenance.setId(null);
                        piStandardMaintenance.setFieldName(copyStandardDto.getTargetField());
                        piStandardMaintenance.setFieldText(copyStandardDto.getTargetFieldName());
                    }
                    //保存
                    saveOrUpdateBatch(standardMainList);
                }
            }
        } catch (Exception e) {
            return false;
        }
        return true;
    }
}




