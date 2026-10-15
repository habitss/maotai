package com.keytech.modules.pi.service.impl;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.keytech.modules.pi.entity.PiRaBop;
import com.keytech.modules.pi.entity.PiRaUnit;
import com.keytech.modules.pi.mapper.PiRaBopMapper;
import com.keytech.modules.pi.service.IPiRaBopService;
import com.keytech.modules.pi.service.IPiRaUnitService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.keytech.base.service.impl.BaseServiceImpl;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @Description: RA-步骤字典
 * @Author: jeecg-boot
 * @Date:   2025-10-31
 * @Version: V1.0
 */
@Service
public class PiRaBopServiceImpl extends BaseServiceImpl<PiRaBopMapper, PiRaBop> implements IPiRaBopService {
    private static final Logger log = LoggerFactory.getLogger(PiRaBopServiceImpl.class);
    @Value("${mes-system.tenant-id}")
    private Integer tenantId;
    @Autowired
    private IPiRaBopService iPiRaBopService;
    @Autowired
    private IPiRaUnitService iPiRaUnitService;

    /**
     * 步骤数据抽取
     */
    @Override
    public void stepExtract() {
        // 抽取设备字典
        log.info("开始RA步骤分页增量同步");
        int page = 1;
        boolean hasMoreData = true;
        int totalProcessed = 0;

        //获取设备
        List<PiRaUnit> units = iPiRaUnitService.list();
        //遍历产线 得到map key为 areaId_1 value为RaLineVO
        Map<Integer, PiRaUnit> unitMap = units.stream()
            .collect(Collectors.toMap(
                    unit -> unit.getUid(),
                    unit -> unit
            ));

        while (hasMoreData) {
            long pageSize = PiSistarRtStepsServiceImpl.BATCH_SIZE;
            long offset = (page - 1) * pageSize;

            // 使用参数分页查询
            List<PiRaBop> dataList = iPiRaBopService.stepDataExtract(offset, pageSize);
            if (dataList.isEmpty()) {
                //hasMoreData = false;
                break;
            }
            //匹配区域id 设备id
            for (PiRaBop piRaBop : dataList) {
                PiRaUnit piRaUnit = unitMap.get(piRaBop.getUnitUid());
                if (piRaUnit != null) {
                    piRaBop.setAreaId(piRaUnit.getAreaId());
                    piRaBop.setUnitId(piRaUnit.getUnitId());
                }
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
            log.info("RA步骤分页同步完成，共处理 {} 条数据", totalProcessed);
        }
    }

    @Override
    @DS("rabasesqlservice")
    public List<PiRaBop> stepDataExtract(long offset, long pageSize) {
        return baseMapper.stepDataExtract(offset, pageSize);
    }

    /**
     * 处理数据
     */
    private int processBatchData(List<PiRaBop> dataList) {
        log.info("RA步骤数据处理1--------------{}", dataList.size());
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
