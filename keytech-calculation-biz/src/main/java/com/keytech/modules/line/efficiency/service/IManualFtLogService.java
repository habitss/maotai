package com.keytech.modules.line.efficiency.service;

import com.keytech.modules.line.efficiency.dto.ManualMessagesDto;
import com.keytech.modules.line.efficiency.entity.ManualFtLog;
import com.keytech.base.service.BaseService;

import java.sql.Timestamp;
import java.util.List;

/**
 * @Description: 上位机系统手动操作记录
 * @Author: jeecg-boot
 * @Date:   2025-11-13
 * @Version: V1.0
 */
public interface IManualFtLogService extends BaseService<ManualFtLog> {

    /**
     * 定时任务接口-上位机系统手动操作记录数据抽取(黑龙江)
     */
    void extractData();

    /**
     * 分页查询(RA)
     * @param lastLoadTime
     * @param currentTime
     * @param offset
     * @param pageSize
     * @return
     */
    List<ManualFtLog> selectManualDataWithPaging(Timestamp lastLoadTime, Timestamp currentTime, long offset, long pageSize);

    /**
     * 定时任务接口-上位机系统手动操作记录数据抽取
     */
    void extractManualData();

    /**
     * 分页查询
     * @param lastLoadTime
     * @param currentTime
     * @param offset
     * @param pageSize
     * @return
     */
    List<ManualMessagesDto> selectManualPmDataWithPaging(Timestamp lastLoadTime, Timestamp currentTime, long offset, long pageSize);
}
