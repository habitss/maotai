package com.keytech.modules.line.efficiency.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.keytech.base.service.impl.BaseServiceImpl;
import com.keytech.modules.line.efficiency.entity.DbScaleweight;
import com.keytech.modules.line.efficiency.entity.DbSourceScaleweight;
import com.keytech.modules.line.efficiency.mapper.DbScaleweightMapper;
import com.keytech.modules.line.efficiency.service.IDbScaleweightService;
import com.keytech.modules.line.efficiency.service.IDbSourceScaleweightService;
import com.keytech.modules.pi.service.impl.PiSistarRtStepsServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @Description: 地磅数据
 * @Author: jeecg-boot
 * @Date:   2025-10-29
 * @Version: V1.0
 */
@Service
@Slf4j
public class DbScaleweightServiceImpl extends BaseServiceImpl<DbScaleweightMapper, DbScaleweight> implements IDbScaleweightService {
    @Value("${mes-system.tenant-id}")
    private Integer tenantId;
    @Autowired
    private IDbSourceScaleweightService iDbSourceScaleweightService;

    /**
     * 地磅数据抽取
     */
    @Override
    public void extractData() {
        //开始时间
        Timestamp lastLoadTime = null;
        //当前时间做结束时间
        Timestamp currentTime = new Timestamp(System.currentTimeMillis());
        //获取开始数据(表中最新最新离开时间)
        DbScaleweight latestRecord = lambdaQuery()
                .orderByDesc(DbScaleweight::getOutime)
                .last("LIMIT 1")
                .one();
        if(latestRecord != null){
            //时间转换
            lastLoadTime = new Timestamp(latestRecord.getOutime().getTime());
        }

        if(lastLoadTime == null){
            //设置开始时间为1年前
            long oneYearAgo = System.currentTimeMillis() - TimeUnit.DAYS.toMillis(365);
            lastLoadTime = new Timestamp(oneYearAgo);
        }
        log.info("开始参数分页增量同步，时间范围: {} - {}", lastLoadTime, currentTime);
        int page = 1;
        boolean hasMoreData = true;
        int totalProcessed = 0;

        while (hasMoreData) {
            long pageSize = PiSistarRtStepsServiceImpl.BATCH_SIZE;
            long offset = (page - 1) * pageSize;

            // 使用参数分页查询
            List<DbSourceScaleweight> dataList = iDbSourceScaleweightService.selectScaleweightDataWithPaging(
                    lastLoadTime, currentTime, offset, pageSize);

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
            log.info("参数分页同步完成，共处理 {} 条数据", totalProcessed);
        }
    }


    /**
     * 处理批量数据
     */
    private int processBatchData(List<DbSourceScaleweight> dataList) {
        if (dataList.isEmpty()) {
            return 0;
        }
        // 转换数据
        List<DbScaleweight> targetList = dataList.stream()
                .map(this::convertToTarget)
                .collect(Collectors.toList());

        // 批量插入
        saveOrUpdateBatch(targetList);
        return targetList.size();
    }

    /**
     * 数据转换
     */
    private DbScaleweight convertToTarget(DbSourceScaleweight batches) {
        //对象复制
        DbScaleweight target = BeanUtil.copyProperties(batches, DbScaleweight.class);
        target.setTenantId(tenantId);
        // 设置ID
        target.setId(batches.getTicketid());
        return target;
    }
}
