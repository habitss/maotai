package com.keytech.modules.line.efficiency.service.impl;

import com.keytech.modules.line.efficiency.entity.LineEfficiencySkuDay;
import com.keytech.modules.line.efficiency.mapper.LineEfficiencySkuDayMapper;
import com.keytech.modules.line.efficiency.service.ILineEfficiencySkuDayService;
import com.keytech.modules.line.efficiency.service.ILineEfficiencySkuService;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.config.GlobalErrorEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.keytech.base.service.impl.BaseServiceImpl;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * @Description: 产线-SKU天记录表/拆分
 * @Author: jeecg-boot
 * @Date:   2025-08-20
 * @Version: V1.0
 */
@Service
public class LineEfficiencySkuDayServiceImpl extends BaseServiceImpl<LineEfficiencySkuDayMapper, LineEfficiencySkuDay> implements ILineEfficiencySkuDayService {
    @Autowired
    private ILineEfficiencySkuService lineEfficiencySkuService;
    /**
     * 拆分
     * 1.写入拆数据
     * 2.计算产量
     * 3.删除历史数据
     * @param lineEfficiencySkuDayList
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<String> dataSplit(List<LineEfficiencySkuDay> lineEfficiencySkuDayList) {

        if(lineEfficiencySkuDayList != null && !lineEfficiencySkuDayList.isEmpty()){
            //判断是否被拆分
            //原数据id
            String mainId = lineEfficiencySkuDayList.get(0).getMainId();
            LineEfficiencySkuDay skuDay = getById(mainId);
            if(skuDay == null){
                //数据已经被拆分
                return Result.error("数据已完成拆分，无需重复操作");
            }
            //获取产量数据
            for (LineEfficiencySkuDay lineEfficiencySkuDay : lineEfficiencySkuDayList) {
                // 时间段(秒)
                Long startTime = lineEfficiencySkuDay.getStartTime().getTime() / 1000;
                Long endTime = lineEfficiencySkuDay.getEndTime().getTime() / 1000;
                Double outputValue = lineEfficiencySkuService.queryDataProduction(lineEfficiencySkuDay.getOutputCode(), startTime, endTime);
                lineEfficiencySkuDay.setOutputValue(outputValue);
            }
            //写入拆数据
            saveBatch(lineEfficiencySkuDayList);
            //删除历史数据
            removeById(mainId);
            return Result.OK(GlobalErrorEnum.SUCCESS.getCode());
        }
        return Result.error("操作失败");
    }
}


