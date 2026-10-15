package com.keytech.modules.line.efficiency.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.keytech.base.service.impl.BaseServiceImpl;
import com.keytech.modules.line.efficiency.entity.LineEfficiencyStateRecord;
import com.keytech.modules.line.efficiency.entity.LineEfficiencyStateRecordPieceSku;
import com.keytech.modules.line.efficiency.mapper.LineEfficiencyStateRecordPieceSkuMapper;
import com.keytech.modules.line.efficiency.service.ILineEfficiencyStateRecordPieceSkuService;
import com.keytech.modules.line.efficiency.service.ILineEfficiencyStateRecordService;
import com.keytech.modules.line.efficiency.utils.OpiCalculatorUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

/**
 * @Description: 线效清零分段-按照sku记录
 * @Author: jeecg-boot
 * @Date:   2025-08-25
 * @Version: V1.0
 */
@Service
public class LineEfficiencyStateRecordPieceSkuServiceImpl extends BaseServiceImpl<LineEfficiencyStateRecordPieceSkuMapper, LineEfficiencyStateRecordPieceSku> implements ILineEfficiencyStateRecordPieceSkuService {

    @Autowired
    private ILineEfficiencyStateRecordService lineEfficiencyStateRecordService;

    /**
     * 计算OPI
     * 公式 OPI = 产量/速度/运行时长*3600
     * @param pageList
     */
    @Override
    public void calculateOpi(IPage<LineEfficiencyStateRecordPieceSku> pageList) {
        //遍历数据
        for (LineEfficiencyStateRecordPieceSku recordPieceSku : pageList.getRecords()) {
            BigDecimal opi = OpiCalculatorUtil.calculateOpi(recordPieceSku.getOutputValue(), recordPieceSku.getSkuSpeed(), recordPieceSku.getRunningTime());
            recordPieceSku.setOpi(opi != null ? opi.doubleValue()+"" : "0%");
        }
    }

    /**
     * 修改速度
     * 1.存储SKU数据
     * 2.获取批次的sku数据集合
     * 4.计算批次数据OPI
     * 5.写入批次
     * @param stateRecordPieceSku
     */
    @Override
    public void speedEdit(LineEfficiencyStateRecordPieceSku stateRecordPieceSku) {
        //存储SKU数据
        updateById(stateRecordPieceSku);
        //获取批次的sku数据集合
        LambdaQueryWrapper<LineEfficiencyStateRecordPieceSku> queryWrapper = Wrappers.lambdaQuery(LineEfficiencyStateRecordPieceSku.class);
        queryWrapper.ge(LineEfficiencyStateRecordPieceSku::getStartTime, stateRecordPieceSku.getBatchStartTime());
        queryWrapper.le(LineEfficiencyStateRecordPieceSku::getEndTime, stateRecordPieceSku.getBatchEndTime());
        queryWrapper.eq(LineEfficiencyStateRecordPieceSku::getLineCode, stateRecordPieceSku.getLineCode());
        List<LineEfficiencyStateRecordPieceSku> skuList = list(queryWrapper);
        //计算批次数据OPI
        Double runningTime = stateRecordPieceSku.getBatchRunningTime();
        BigDecimal sum = BigDecimal.ZERO;
        for (LineEfficiencyStateRecordPieceSku recordPieceSku : skuList) {
            BigDecimal opiEval = OpiCalculatorUtil.divide(recordPieceSku.getOutputValue(), recordPieceSku.getSkuSpeed());
            // 相加
            sum = sum.add(opiEval);
        }
        // SKU比例 / 运行时长
        BigDecimal opi = OpiCalculatorUtil.calculateOpi(sum, runningTime);

        LineEfficiencyStateRecord lineEfficiencyStateRecord = new LineEfficiencyStateRecord();
        lineEfficiencyStateRecord.setId(stateRecordPieceSku.getBatchId());
        lineEfficiencyStateRecord.setActualEfficiencyValue(opi != null ? opi.doubleValue()+"" : "0");
        //写入批次
        lineEfficiencyStateRecordService.updateById(lineEfficiencyStateRecord);
    }

}
