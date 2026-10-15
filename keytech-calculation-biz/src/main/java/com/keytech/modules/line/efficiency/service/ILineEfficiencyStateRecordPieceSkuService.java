package com.keytech.modules.line.efficiency.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.keytech.modules.line.efficiency.entity.LineEfficiencyStateRecordPieceSku;
import com.keytech.base.service.BaseService;

/**
 * @Description: 线效清零分段-按照sku记录
 * @Author: jeecg-boot
 * @Date:   2025-08-25
 * @Version: V1.0
 */
public interface ILineEfficiencyStateRecordPieceSkuService extends BaseService<LineEfficiencyStateRecordPieceSku> {

    /**
     * 计算Opi
     * @param pageList
     */
    void calculateOpi(IPage<LineEfficiencyStateRecordPieceSku> pageList);

    /**
     * 速度编辑
     * @param lineEfficiencyStateRecordPieceSku
     */
    void speedEdit(LineEfficiencyStateRecordPieceSku lineEfficiencyStateRecordPieceSku);
}
