package com.keytech.modules.pi.service.impl;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.keytech.modules.pi.controller.PiRaStepController;
import com.keytech.modules.pi.entity.*;
import com.keytech.modules.pi.mapper.PiRaStepParamMapper;
import com.keytech.modules.pi.service.IPiRaStepParamService;
import groovy.util.logging.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @Description: RA-步骤参数
 * @Author: jeecg-boot
 * @Date:   2025-10-31
 * @Version: V1.0
 */
@Service
@Slf4j
public class PiRaStepParamServiceImpl extends BaseServiceImpl<PiRaStepParamMapper, PiRaStepParam> implements IPiRaStepParamService {
    private static final Logger log = LoggerFactory.getLogger(PiRaStepParamServiceImpl.class);
    public static final int BATCH_SIZE = 500; // 每批处理500条
    @Value("${mes-system.tenant-id}")
    private Integer tenantId;
    @Autowired
    private IPiRaStepParamService iPiRaStepParamService;


    @Override
    public void extractData() {
        //开始时间
        Timestamp lastLoadTime = null;
        //当前时间做结束时间
        Timestamp currentTime = new Timestamp(System.currentTimeMillis());
        //获取开始数据(表中最新最新离开时间)
        PiRaStepParam latestRecord = lambdaQuery()
                .orderByDesc(PiRaStepParam::getInsertDt)
                .last("LIMIT 1")
                .one();
        if(latestRecord != null){
            //时间转换
            lastLoadTime = new Timestamp(latestRecord.getInsertDt().getTime());
        }

        if(lastLoadTime == null){
            //设置开始时间为4个月前
            long oneYearAgo = System.currentTimeMillis() - TimeUnit.DAYS.toMillis(120);
            lastLoadTime = new Timestamp(oneYearAgo);
        }
        log.info("开始参数分页增量同步，时间范围: {} - {}", lastLoadTime, currentTime);
        int page = 1;
        boolean hasMoreData = true;
        int totalProcessed = 0;

        Timestamp lastRecordTime = lastLoadTime; // 初始化最后记录时间
        while (hasMoreData) {
            long offset = (page - 1) * BATCH_SIZE;
            //long endRow = page * BATCH_SIZE;
            // 使用参数分页查询
            log.info("查询条件，开始时间: {} - 结束时间: {} &&&& 第: {} 页数据,每页{}条数据", lastLoadTime,currentTime,offset,BATCH_SIZE);
            List<PiRaStepParam> dataList = iPiRaStepParamService.stepParamDataWithPaging(
                    lastLoadTime, currentTime, offset, BATCH_SIZE);
            log.info("查询返回数据，条数: {} 条数据", dataList.size());

            if (dataList == null || (dataList != null && dataList.isEmpty())) {
                log.info("已处理完成，总计: {} 条数据", totalProcessed);
                break;
            }
            // 处理本批数据
            int processed = processBatchData(dataList);
            totalProcessed += processed;

            log.info("已处理第 {} 页数据，数量: {}, 总计: {}", page, processed, totalProcessed);
            page++;

            // 如果返回的数据少于批次大小，说明没有更多数据了
            if (dataList.size() < BATCH_SIZE) {
                hasMoreData = false;
            }
        }
        // 更新抽取记录
        if (totalProcessed > 0) {
            log.info("参数分页同步完成，共处理 {} 条数据", totalProcessed);
        }
    }

    @Override
    @DS("rastepsqlservice")
    public List<PiRaStepParam> stepParamDataWithPaging(Timestamp lastLoadTime, Timestamp currentTime, long offset, int batchSize) {
        return baseMapper.stepParamDataWithPaging(lastLoadTime, currentTime, offset, batchSize);
    }

    @Override
    @DS("rastepsqlservice")
    public List<PiRaStepParam> selectByConditions(List<PiRaStep> dataList) {
        return baseMapper.selectByConditions(dataList);
    }


    /**
     * 处理批量数据
     */
    private int processBatchData(List<PiRaStepParam> dataList) {
        log.info("RA步骤数据处理1--------------{}", dataList.size());
        if (dataList.isEmpty()) {
            return 0;
        }
        // 转换数据
        List<PiRaStepParam> targetList = dataList.stream()
                .map(this::convertToTarget)
                .collect(Collectors.toList());

        // 批量插入
        saveOrUpdateBatch(targetList);
        return targetList.size();
    }

    /**
     * 数据转换
     */
    private PiRaStepParam convertToTarget(PiRaStepParam piRaStepParam) {
        //对象复制
        piRaStepParam.setTenantId(tenantId);
        // 拼接ID
        String id = buildCompleteId(piRaStepParam);
        piRaStepParam.setId(id);
        piRaStepParam.setCorrelation(correlation(piRaStepParam));
        return piRaStepParam;
    }

    /**
     * 步骤参数 生成ID
     * 区域ID 设备ID 订单ID 批次号 配方ID 步骤ID 步骤号setpNr  paraId
     * --数据插入时间
     * @param piRaStep
     * @return
     */
    private String buildCompleteId(PiRaStepParam piRaStep) {
//        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
//        String formattedTime = piRaStep.getInsertDt() != null ?
//                piRaStep.getInsertDt().toInstant()
//                        .atZone(ZoneId.systemDefault())
//                        .format(formatter) : "00000000000000";

        return String.join("_",
                piRaStep.getAreaId() != null ? piRaStep.getAreaId().toString() : "NULL_AREAID",
                piRaStep.getUnitId() != null ? piRaStep.getUnitId().toString() : "NULL_UNIT",
                piRaStep.getOrderId() != null ? piRaStep.getOrderId().toString() : "NULL_ORDER",
                piRaStep.getBatchNr() != null ? piRaStep.getBatchNr().toString() : "NULL_BATCH",
                piRaStep.getRecipeId() != null ? piRaStep.getRecipeId().toString() : "NULL_RECIPE",
                piRaStep.getBopId() != null ? piRaStep.getBopId().toString() : "NULL_BOP",
                piRaStep.getStepNr() != null ? piRaStep.getStepNr().toString() : "NULL_STEPNR",
                piRaStep.getParaId() != null ? piRaStep.getParaId().toString() : "NULL_PARAID"
        );
    }

    /**
     * 生成关联
     * 区域ID_设备ID_订单ID_批次号_步骤ID_步骤号_配方 (recipeId)
     * @param piRaStepParam
     * @return
     */
    private String correlation(PiRaStepParam piRaStepParam) {

        return String.join("_",
                piRaStepParam.getAreaId() != null ? piRaStepParam.getAreaId().toString() : "NULL_AREAID",
                piRaStepParam.getUnitId() != null ? piRaStepParam.getUnitId().toString() : "NULL_UNIT",
                piRaStepParam.getOrderId() != null ? piRaStepParam.getOrderId().toString() : "NULL_ORDER",
                piRaStepParam.getBatchNr() != null ? piRaStepParam.getBatchNr().toString() : "NULL_BATCH",
                piRaStepParam.getBopId() != null ? piRaStepParam.getBopId().toString() : "NULL_BOP",
                piRaStepParam.getStepNr() != null ? piRaStepParam.getStepNr().toString() : "NULL_STEPNR",
                piRaStepParam.getRecipeId() != null ? piRaStepParam.getRecipeId().toString() : "NULL_RECIPEID"
        );
    }


    /**
     * 根据时间(监控)条件查询
     * @param dataList
     * @return
     */
    @Override
    @DS("rastepsqlservice")
    public List<PiRaStepParam> selectByTimeConditions(List<PiRaStep> dataList) {
        return baseMapper.selectByTimeConditions(dataList);
    }
}
