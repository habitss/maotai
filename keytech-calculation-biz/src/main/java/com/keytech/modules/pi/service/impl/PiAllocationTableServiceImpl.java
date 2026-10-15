package com.keytech.modules.pi.service.impl;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import com.keytech.base.service.impl.BaseServiceImpl;
import com.keytech.modules.calculation.CalculationBaseApi;
import com.keytech.modules.pi.dto.RerunDto;
import com.keytech.modules.pi.dto.TableConditionDto;
import com.keytech.modules.pi.dto.TableFieldValueDto;
import com.keytech.modules.pi.dto.TableSaveDto;
import com.keytech.modules.pi.entity.*;
import com.keytech.modules.pi.mapper.PiAllocationTableMapper;
import com.keytech.modules.pi.service.*;
import com.keytech.modules.pi.vo.PiAllocationTableVisitsVo;
import jakarta.annotation.Resource;
import org.jeecg.common.api.vo.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @Description: pi报表分配
 * @Author: jeecg-boot
 * @Date:   2024-11-26
 * @Version: V1.0
 */
@Service
public class PiAllocationTableServiceImpl extends BaseServiceImpl<PiAllocationTableMapper, PiAllocationTable> implements IPiAllocationTableService {

    @Autowired
    private IPiComputationModelService computationModelService;
    @Resource
    private IPiCustomizeTableService iPiCustomizeTableService;
    @Resource
    IPiAdapterAlgorithmService iPiAdapterAlgorithmService;

    /**
     * 算法重算
     * fieldList 字段集合
     * tableName 表名
     * timeStart 时间段起
     * timeEnd 时间段止
     * @param rerunDto
     */
    @Override
    public Result rerun(RerunDto rerunDto) {
        //判断参数算法为空
        if(null == rerunDto.getFieldList() || rerunDto.getFieldList().size() == 0 ||
           null == rerunDto.getTimeStart() || null == rerunDto.getTimeEnd() || StrUtil.isBlank(rerunDto.getTableName())){
            return Result.error("参数错误！");
        }

        try {
            //时间转换
            LocalDateTime startDateTime = LocalDateTime.ofInstant(rerunDto.getTimeStart().toInstant(), ZoneId.systemDefault());
            LocalDateTime endDateTime = LocalDateTime.ofInstant(rerunDto.getTimeEnd().toInstant(), ZoneId.systemDefault());

            computationModelService.rePiCalculateSignal(rerunDto.getTableName(),rerunDto.getFieldList(),startDateTime,endDateTime);

            //Adapter算法重算
            iPiAdapterAlgorithmService.againPiCalculate(rerunDto.getTableName(),rerunDto.getFieldList(),startDateTime,endDateTime);

            //RA算法重算
            iPiAdapterAlgorithmService.againPiCalculateRa(rerunDto.getTableName(),rerunDto.getFieldList(),startDateTime,endDateTime);
        } catch (DateTimeParseException e) {
            return Result.error("重算失败！");
        }
        return Result.OK("重算完成！");
    }

    /**
     * 批量删除
     * @return
     */
    @Override
    public boolean deleteDataBatch(String data,String tableName) {
        boolean isOk = true;
        if(StrUtil.isNotBlank(data)){
            String[] ids = data.split(",");
            for (String id : ids) {

                //条件
                List<TableConditionDto> conditionList = new ArrayList<>();
                TableConditionDto tableConditionDto = new TableConditionDto();
                tableConditionDto.setKey("id");
                tableConditionDto.setVal(id);
                conditionList.add(tableConditionDto);

                //字段
                List<TableFieldValueDto> fieldList = new ArrayList<>();
                TableFieldValueDto tableFieldValueDto = new TableFieldValueDto();
                tableFieldValueDto.setFieldVal("1");
                tableFieldValueDto.setField("del_flag");
                fieldList.add(tableFieldValueDto);

                //更新值
                TableSaveDto tableSaveDto = new TableSaveDto();
                tableSaveDto.setTable(tableName);
                tableSaveDto.setConditionList(conditionList);
                tableSaveDto.setFieldList(fieldList);
                //更新
                String ok = iPiCustomizeTableService.tableSave(tableSaveDto);
                if("1".equals(ok)){
                    isOk=false;
                }
            }
            return isOk;
        }
        return isOk;
    }

    @Override
    public List<PiAllocationTableVisitsVo> queryAllocationTableVisits(PiAllocationTableVisitsVo piAllocationTableVisitsVo) {
        // 1. 获取指定年份第一天的开始（00:00:00）
        Date startOfYear = DateUtil.beginOfYear(DateUtil.parse(String.valueOf(piAllocationTableVisitsVo.getYear()), "yyyy"));
        // 2. 获取下一年第一天的开始（即本年的结束边界）
        Date startOfNextYear = DateUtil.beginOfYear(DateUtil.parse(String.valueOf(piAllocationTableVisitsVo.getYear()+1), "yyyy"));
        piAllocationTableVisitsVo.setStartTime(startOfYear);
        piAllocationTableVisitsVo.setEndTime(startOfNextYear);
        return baseMapper.queryAllocationTableVisits(piAllocationTableVisitsVo);
    }


}
