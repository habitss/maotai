package com.keytech.modules.pi.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.keytech.modules.pi.entity.*;
import com.keytech.modules.pi.mapper.PiSistarRtBatchesMapper;
import com.keytech.modules.pi.service.IPiSistarRtBatchesService;
import com.keytech.modules.pi.service.IPiSistarRtStepLogService;
import com.keytech.modules.pi.service.IPiSistarRtStepsService;
import com.keytech.modules.pi.service.ISourceSistarRtBatchesService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.keytech.base.service.impl.BaseServiceImpl;

import java.sql.Timestamp;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @Description: BM-实时订单记录表
 * @Author: jeecg-boot
 * @Date:   2025-09-23
 * @Version: V1.0
 */
@Service
@Slf4j
public class PiSistarRtBatchesServiceImpl extends BaseServiceImpl<PiSistarRtBatchesMapper, PiSistarRtBatches> implements IPiSistarRtBatchesService {

    @Value("${mes-system.tenant-id}")
    private Integer tenantId;
    @Autowired
    private IPiSistarRtStepLogService iPiSistarRtStepLogService;
    @Autowired
    private IPiSistarRtStepsService iPiSistarRtStepsService;
    @Autowired
    private ISourceSistarRtBatchesService iSourceSistarRtStepParamsService;

    /**
     * 批次分页同步
     */
    @Override
    public void extractData() {
        Timestamp currentTime = new Timestamp(System.currentTimeMillis());
        Timestamp lastLoadTime = iPiSistarRtStepLogService.getLastLoadTime("2");
        if(lastLoadTime == null){
            return;
        }
        log.info("开始参数分页增量同步，时间范围: {} - {}", lastLoadTime, currentTime);
        int page = 1;
        boolean hasMoreData = true;
        int totalProcessed = 0;

        Timestamp lastRecordTime = lastLoadTime; // 初始化最后记录时间
        while (hasMoreData) {
            long pageSize = PiSistarRtStepsServiceImpl.BATCH_SIZE;
            long offset = (page - 1) * pageSize;

            // 使用参数分页查询
            List<SourceSistarRtBatches> dataList = iSourceSistarRtStepParamsService.selectIncrementalDataWithPaging(
                    lastLoadTime, currentTime, offset, pageSize);

            if (dataList.isEmpty()) {
                hasMoreData = false;
                break;
            }

            // 处理本批数据
            int processed = processBatchData(dataList);
            totalProcessed += processed;

            // 由于数据按 insert_timestamp 正序排列，直接取最后一条的时间
            SourceSistarRtBatches lastRecord = dataList.get(dataList.size() - 1);
            if (lastRecord.getInsertTimestamp() != null && lastRecord.getInsertTimestamp().after(lastRecordTime)) {
                if(lastRecord.getInsertTimestamp() != null){
                    lastRecordTime = new Timestamp(lastRecord.getInsertTimestamp().getTime());
                    iPiSistarRtStepLogService.updateLastLoadTime("2", lastRecordTime, tenantId);
                }

            }

            log.info("已处理第 {} 页数据，数量: {}, 总计: {}", page, processed, totalProcessed);

            page++;

            // 如果返回的数据少于批次大小，说明没有更多数据了
            if (dataList.size() < pageSize) {
                hasMoreData = false;
            }
            // 定期清空缓存
            if (page % 10 == 0) {
                iPiSistarRtStepsService.clearMyBatisCache();
            }

        }

        // 更新抽取记录
        if (totalProcessed > 0) {
            //iPiSistarRtStepLogService.updateLastLoadTime("0", lastRecordTime, tenantId);
            log.info("参数分页同步完成，共处理 {} 条数据", totalProcessed);
        }
    }


    /**
     * 处理批量数据
     */
    private int processBatchData(List<SourceSistarRtBatches> dataList) {
        if (dataList.isEmpty()) {
            return 0;
        }
        // 转换数据
        List<PiSistarRtBatches> targetList = dataList.stream()
                .map(this::convertToTarget)
                .collect(Collectors.toList());

        // 批量插入
//        saveBatch(targetList);
        saveOrUpdateBatch(targetList);
        return targetList.size();
    }


    /**
     * 数据转换
     */
    private PiSistarRtBatches convertToTarget(SourceSistarRtBatches batches) {
        //对象复制
        PiSistarRtBatches target = BeanUtil.copyProperties(batches, PiSistarRtBatches.class);
        target.setTenantId(tenantId);
        // 拼接ID
        String id = buildCompleteId(batches);
        target.setId(id);
        return target;
    }

    /**
     * 生成ID
     * @param batches
     * @return
     */
    private String buildCompleteId(SourceSistarRtBatches batches) {
        return String.join("_",
                batches.getSiteNo() != null ? batches.getSiteNo().toString() : "NULL_SITE",
                batches.getAreaNo() != null ? batches.getAreaNo().toString() : "NULL_AREA",
                batches.getYearNo() != null ? batches.getYearNo().toString() : "NULL_YEAR",
                batches.getReccatNo() != null ? batches.getReccatNo().toString() : "NULL_RECCAT",
                batches.getOrderNo() != null ? batches.getOrderNo().toString() : "NULL_ORDER",
                batches.getBatchNo() != null ? batches.getBatchNo().toString() : "NULL_BATCH"
        );
    }
}
