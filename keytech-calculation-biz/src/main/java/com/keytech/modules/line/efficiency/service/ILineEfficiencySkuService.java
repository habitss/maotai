package com.keytech.modules.line.efficiency.service;

import com.keytech.modules.line.efficiency.dto.PointDateDto;
import com.keytech.modules.line.efficiency.entity.LineEfficiencySku;
import com.keytech.base.service.BaseService;
import com.keytech.modules.line.efficiency.entity.LineEfficiencyStateRecord;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @Description: sku配置
 * @Author: jeecg-boot
 * @Date:   2025-08-13
 * @Version: V1.0
 */
public interface ILineEfficiencySkuService extends BaseService<LineEfficiencySku> {

    /**
     * 根据小时来获取对应的产量及SKU的信息（小时定时任务）
     */
    void getLineEfficiencySkuListByHour();

    /**
     * 根据点位获取时间段内的数据
     * @param pointCode 点位编码
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return
     */
    List<PointDateDto> getTimeRangeByPointCode(String pointCode, LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 根据时间段内获取产量（单独定制功能）
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return
     */
    Double queryDataProduction(String outPutCode, Long startTime, Long endTime);

    /**
     * 批量拆分
     * @param listStateRecord
     */
    void batchSplit(List<LineEfficiencyStateRecord> listStateRecord);
}
