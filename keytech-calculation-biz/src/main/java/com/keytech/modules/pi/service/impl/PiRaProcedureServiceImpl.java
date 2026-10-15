package com.keytech.modules.pi.service.impl;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.keytech.modules.pi.entity.PiRaProcedure;
import com.keytech.modules.pi.entity.PiRaUnit;
import com.keytech.modules.pi.mapper.PiRaProcedureMapper;
import com.keytech.modules.pi.service.IPiRaProcedureService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.keytech.base.service.impl.BaseServiceImpl;

import java.util.List;

/**
 * @Description: RA-配方
 * @Author: jeecg-boot
 * @Date:   2025-10-31
 * @Version: V1.0
 */
@Service
public class PiRaProcedureServiceImpl extends BaseServiceImpl<PiRaProcedureMapper, PiRaProcedure> implements IPiRaProcedureService {
    private static final Logger log = LoggerFactory.getLogger(PiRaProcedureServiceImpl.class);
    @Value("${mes-system.tenant-id}")
    private Integer tenantId;
    @Autowired
    private IPiRaProcedureService iPiRaProcedureService;

    /**
     * 配方数据抽取
     */
    @Override
    public void recipeExtract() {
        // 抽取配方字典
        log.info("开始RA配方分页增量同步");
        int page = 1;
        boolean hasMoreData = true;
        int totalProcessed = 0;

        while (hasMoreData) {
            long pageSize = PiSistarRtStepsServiceImpl.BATCH_SIZE;
            long offset = (page - 1) * pageSize;

            // 使用参数分页查询
            List<PiRaProcedure> dataList = iPiRaProcedureService.recipeDataExtract(offset, pageSize);
            if (dataList.isEmpty()) {
                hasMoreData = false;
                break;
            }
            // 处理本批数据
            int processed = processBatchData(dataList);
            totalProcessed += processed;

            log.info("已处理第 {} 页数据，数量: {}, 总计: {}", page, processed, totalProcessed);
            page++;
            // 如果返回的数据少于批次大小，说明没有更多数据了
            if (dataList.size() < pageSize) {
                hasMoreData = false;
            }
        }
        // 更新抽取记录
        if (totalProcessed > 0) {
            log.info("RA配方分页同步完成，共处理 {} 条数据", totalProcessed);
        }
    }

    @Override
    @DS("rabasesqlservice")
    public List<PiRaProcedure> recipeDataExtract(long offset, long pageSize) {
        return baseMapper.recipeDataExtract(offset, pageSize);
    }


    /**
     * 处理批量数据
     */
    private int processBatchData(List<PiRaProcedure> dataList) {
        log.info("RA配方数据处理1--------------{}", dataList.size());
        if (dataList.isEmpty()) {
            return 0;
        }
        // 设置租户
        dataList.forEach(data -> data.setTenantId(tenantId));

        // 批量插入
        saveOrUpdateBatch(dataList);

        return dataList.size();
    }
}
