package com.keytech.modules.df.data.service.impl;

import com.google.common.collect.Lists;
import com.keytech.modules.df.data.entity.DfDataLog;
import com.keytech.modules.df.data.mapper.DfDataLogMapper;
import com.keytech.modules.df.data.mapper.DfPiValueViewMapper;
import com.keytech.modules.df.data.service.ISyncDfDataService;
import com.keytech.modules.df.data.vo.DfPiValueView;
import com.keytech.modules.ems.util.Java8DateUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;
import java.util.*;

/**
 * 同步DF视图数据 业务实现类
 * @author yepeng
 * @since 2025.12.12
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class SyncDfDataServiceImpl implements ISyncDfDataService {
    private final DfPiValueViewMapper dfPiValueViewMapper;
    private final DfDataLogMapper dfDataLogMapper;

    /**
     * 同步DF视图数据到本地日志表
     * @param tenantId 租户ID
     * @param startDate 开始时间
     * @param endDate   结束时间
     * @since 2025.12.12
     */
    @Override
    public void syncDfDataToLog(Integer tenantId, String startDate, String endDate) {
        List<DfPiValueView> piValueList = dfPiValueViewMapper.selectList(Java8DateUtil.parse(startDate), Java8DateUtil.parse(endDate));
        if (CollectionUtils.isEmpty(piValueList)) {
            log.info("-------本次获取 {} - {} DF数据为空", startDate, endDate);
            return;
        }
        List<DfDataLog> dataList = new ArrayList<>();
        for (DfPiValueView piValue : piValueList) {
            DfDataLog dataLog = new DfDataLog();
            dataLog.setEventId(piValue.getEventID());
            dataLog.setStartTime(piValue.getStartTime());
            dataLog.setEndTime(piValue.getEndTime());
            dataLog.setPiValue(piValue.getPI());
            dataLog.setDataJson(piValue.getData());
            dataLog.setEopId(piValue.getEopID());
            dataLog.setPiCode(piValue.getPICode());
            dataLog.setPiName(piValue.getPIName());
            dataLog.setPiType(piValue.getPIType());
            dataLog.setRuleId(piValue.getRuleID());
            dataLog.setStatus(piValue.getStatus());
            dataLog.setStepId(piValue.getStepID());
            dataLog.setBatchId(piValue.getBatchID());
            dataLog.setOrderId(piValue.getOrderID());
            dataLog.setRunning(piValue.getRunning());
            dataLog.setThingId(piValue.getThingID());
            dataLog.setRecipeId(piValue.getRecipeID());
            dataList.add(dataLog);
        }
        if (dataList.size() > 2000) {
            List<List<DfDataLog>> dataListList = Lists.partition(dataList, 2000);
            for (List<DfDataLog> list : dataListList) {
                dfDataLogMapper.batchInsertOrUpdate(list);
            }
        }else {
            dfDataLogMapper.batchInsertOrUpdate(dataList);
        }
    }

}
