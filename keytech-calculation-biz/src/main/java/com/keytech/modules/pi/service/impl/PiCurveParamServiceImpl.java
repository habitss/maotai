package com.keytech.modules.pi.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.keytech.modules.mdm3.entity.MdmDict;
import com.keytech.modules.mdm3.service.IMdmDictService;
import com.keytech.modules.pi.entity.PiCurveParam;
import com.keytech.modules.pi.entity.PiCustomizeTable;
import com.keytech.modules.pi.entity.PiCustomizeTableField;
import com.keytech.modules.pi.entity.PiRetrievalSignal;
import com.keytech.modules.pi.mapper.PiCurveParamMapper;
import com.keytech.modules.pi.service.IPiCurveParamService;
import com.keytech.modules.pi.service.IPiCustomizeTableFieldService;
import com.keytech.modules.pi.service.IPiCustomizeTableService;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.config.GlobalErrorEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.keytech.base.service.impl.BaseServiceImpl;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @Description: PI曲线参数配置
 * @Author: jeecg-boot
 * @Date:   2026-01-19
 * @Version: V1.0
 */
@Service
public class PiCurveParamServiceImpl extends BaseServiceImpl<PiCurveParamMapper, PiCurveParam> implements IPiCurveParamService {

    @Autowired
    private IPiCustomizeTableService iPiCustomizeTableService;
    @Autowired
    private IPiCustomizeTableFieldService iPiCustomizeTableFieldService;
    @Autowired
    private IMdmDictService mdmDictService;
    /**
     * 保存数据
     * @param piCurveParam
     */
    @Override
    public Result<?> saveDate(PiCurveParam piCurveParam) {
        Result result = new Result<>();
        boolean isSave = true;
        if(StrUtil.isNotBlank(piCurveParam.getId())){
            isSave = false;
        }else{
            //判断参数是否存在
            List<PiCurveParam> piCurveParamList = list(new LambdaQueryWrapper<PiCurveParam>()
                    .eq(PiCurveParam::getParam, piCurveParam.getParam())
                    .last("LIMIT 1")); // 限制只查一条
            if(piCurveParamList != null && !piCurveParamList.isEmpty()){
                result.error500("参数已存在");
                return result;
            }
        }

        //获取报表名称
        PiCustomizeTable piCustomizeTable = null;
        if (StrUtil.isBlank(piCurveParam.getTableName())) {
            piCustomizeTable = iPiCustomizeTableService.getById(piCurveParam.getTableId());
            if (piCustomizeTable != null) {
                piCurveParam.setTableName(piCustomizeTable.getTableName());
            }
        }
        //保存/修改
        if(isSave){
            save(piCurveParam);
        }else{
            updateById(piCurveParam);
        }
        result.setMessage(GlobalErrorEnum.SUCCESS.getCode());
        return result;
    }

    // 数据转换
    @Override
    public void convert(IPage<PiCurveParam> pageList) {
        //获取 tableId集合
        List<String> tableIdList = pageList.getRecords().stream().map(PiCurveParam::getTableId).distinct().toList();

        //获取指定报表PI集合
        LambdaQueryWrapper<PiCustomizeTableField> queryWrapper = new LambdaQueryWrapper<PiCustomizeTableField>();
        queryWrapper.in(PiCustomizeTableField::getTableId, tableIdList);
        List<PiCustomizeTableField> piCustomizeTableFieldList = iPiCustomizeTableFieldService.list(queryWrapper);
        //报表id 和对象集合
        Map<String, List<PiCustomizeTableField>> tableIdToFieldsMap = piCustomizeTableFieldList.stream()
                .filter(field -> field.getTableId() != null) // 过滤掉tableId为null的记录
                .collect(Collectors.groupingBy(
                        PiCustomizeTableField::getTableId,
                        Collectors.toList()
                ));
        //获取区域字典
        List<MdmDict> mdmDictList = mdmDictService.getDictAllOrByGroupCode(
                List.of("MES3_PI_CURVE_AREA")
        );
        Map<String, String> dictMap = mdmDictList.stream()
                .collect(Collectors.toMap(
                        item -> item.getItemVal(),
                        item -> item.getItemKey()
                ));

        for (PiCurveParam piCurveParam : pageList.getRecords()) {
            List<PiCustomizeTableField> fields = tableIdToFieldsMap.get(piCurveParam.getTableId());
            if (fields != null) {
                for (PiCustomizeTableField field : fields) {
                    if (field.getFieldName().equals(piCurveParam.getDateTime())) {
                        piCurveParam.setDateTimeName(field.getName());
                    }
                    if (field.getFieldName().equals(piCurveParam.getOrderNo())) {
                        piCurveParam.setOrderName(field.getName());
                    }
                    if (field.getFieldName().equals(piCurveParam.getBatchNo())) {
                        piCurveParam.setBatchName(field.getName());
                    }
                    if (field.getFieldName().equals(piCurveParam.getParamVal())) {
                        piCurveParam.setParamValName(field.getName());
                    }
                }
            }
            if(dictMap != null){
                piCurveParam.setAreaName(dictMap.get(piCurveParam.getAreaNo()));
            }

        }

    }
}
