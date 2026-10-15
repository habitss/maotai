package com.keytech.modules.pi.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.keytech.modules.pi.entity.*;
import com.keytech.modules.pi.mapper.PiSistarRtStepParamsMapper;
import com.keytech.modules.pi.service.*;
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
 * @Description: Adapter数据-步骤明细
 * @Author: jeecg-boot
 * @Date:   2025-08-31
 * @Version: V1.0
 */
@Service
@Slf4j
public class PiSistarRtStepParamsServiceImpl extends BaseServiceImpl<PiSistarRtStepParamsMapper, PiSistarRtStepParams> implements IPiSistarRtStepParamsService {

    @Autowired
    private IPiSistarRtStepLogService iPiSistarRtStepLogService;
    @Autowired
    private ISourceSistarRtStepParamsService iSourceSistarRtStepParamsService;
    @Autowired
    private IPiSistarRtStepsService iPiSistarRtStepsService;
    @Autowired
    private IPiAdapterEopParamService iPiAdapterEopParamService;
    @Value("${mes-system.tenant-id}")
    private Integer tenantId;

    /**
     * 定时任务接口-Adapter数据抽取
     */
    @Override
    public void extractData() {
        Timestamp currentTime = new Timestamp(System.currentTimeMillis());
        Timestamp lastLoadTime = iPiSistarRtStepLogService.getLastLoadTime("1");
        if(lastLoadTime == null){
            return;
        }
        log.info("开始参数分页增量同步，时间范围: {} - {}", lastLoadTime, currentTime);
        int page = 1;
        boolean hasMoreData = true;
        int totalProcessed = 0;

        //获取EOP与参数对照
        List<PiAdapterEopParam> eopParamList = iPiAdapterEopParamService.list();
        //设备map key (区域_EOP_参数)为区域、EOP、参数下横线分隔 value 为对象
        Map<String, PiAdapterEopParam> eopParamMap = eopParamList.stream()
                .collect(Collectors.toMap(
                        piAdapterEopParam -> piAdapterEopParam.getAreaNo() + "_" + piAdapterEopParam.getPcuNo() +"_" + piAdapterEopParam.getEopNo() + "_" + piAdapterEopParam.getParamName(),
                        piAdapterEopParam -> piAdapterEopParam,
                        (existing, replacement) -> existing
                ));


        Timestamp lastRecordTime = lastLoadTime; // 初始化最后记录时间
        while (hasMoreData) {
            long pageSize = PiSistarRtStepsServiceImpl.BATCH_SIZE;
            long offset = (page - 1) * pageSize;

            // 使用参数分页查询
            List<SourceSistarRtStepParams> dataList = iSourceSistarRtStepParamsService.selectIncrementalDataWithPaging(
                    lastLoadTime, currentTime, offset, pageSize);

            if (dataList.isEmpty()) {
                hasMoreData = false;
                break;
            }

            // 处理本批数据
            int processed = processBatchData(dataList);
            totalProcessed += processed;

            // 由于数据按 insert_timestamp 正序排列，直接取最后一条的时间
            SourceSistarRtStepParams lastRecord = dataList.get(dataList.size() - 1);
            if (lastRecord.getInsertTimestamp() != null && lastRecord.getInsertTimestamp().after(lastRecordTime)) {
                if(lastRecord.getInsertTimestamp() != null){
                    lastRecordTime = new Timestamp(lastRecord.getInsertTimestamp().getTime());
                    //记录最后更新时间
                    iPiSistarRtStepLogService.updateLastLoadTime("1", lastRecordTime, tenantId);
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

            // 新增 EOP参数对照
            try {
                if(dataList != null && !dataList.isEmpty()){
                    //EOP参数对照新增
                    List<PiAdapterEopParam> eopParamAddList = new ArrayList<>();
                    for(SourceSistarRtStepParams sourceSistarRtStepParams : dataList){
                        String key = sourceSistarRtStepParams.getAreaNo() + "_" + sourceSistarRtStepParams.getPcuNo() + "_" + sourceSistarRtStepParams.getEopNo() +
                                "_" + sourceSistarRtStepParams.getParamName();
                        if(!eopParamMap.containsKey(key)){
                            PiAdapterEopParam piAdapterEopParam = new PiAdapterEopParam();
                            piAdapterEopParam.setTenantId(tenantId);
                            piAdapterEopParam.setAreaNo(sourceSistarRtStepParams.getAreaNo());
                            piAdapterEopParam.setEopNo(sourceSistarRtStepParams.getEopNo());
                            piAdapterEopParam.setParamPos(sourceSistarRtStepParams.getParamPos());
                            piAdapterEopParam.setParamName(sourceSistarRtStepParams.getParamName());
                            eopParamAddList.add(piAdapterEopParam);
                            eopParamMap.put(key, piAdapterEopParam);
                        }else{
                            PiAdapterEopParam piAdapterEopParam = eopParamMap.get(key);
                            //判断名称是否相同
                            if(!piAdapterEopParam.getParamName().equals(sourceSistarRtStepParams.getParamName())){
                                //更新名称
                                piAdapterEopParam.setParamName(sourceSistarRtStepParams.getParamName());
                                piAdapterEopParam.setParamPos(sourceSistarRtStepParams.getParamPos());
                                eopParamAddList.add(piAdapterEopParam);
                                eopParamMap.put(key, piAdapterEopParam);
                            }
                        }
                    }
                    //iPiAdapterEopParamService.saveOrUpdateBatch(eopParamAddList);
                    // 避免重复写入
                    for (PiAdapterEopParam eopParam : eopParamAddList) {
                        PiAdapterEopParam exist = iPiAdapterEopParamService.lambdaQuery()
                                .eq(PiAdapterEopParam::getAreaNo, eopParam.getAreaNo())
                                .eq(PiAdapterEopParam::getPcuNo, eopParam.getPcuNo())
                                .eq(PiAdapterEopParam::getEopNo, eopParam.getEopNo())
                                .eq(PiAdapterEopParam::getParamName, eopParam.getParamName())
                                .one();
                        if (exist != null) {
                            eopParam.setId(exist.getId());
                            iPiAdapterEopParamService.updateById(eopParam);
                        } else {
                            iPiAdapterEopParamService.save(eopParam);
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
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
    private int processBatchData(List<SourceSistarRtStepParams> dataList) {
        if (dataList.isEmpty()) {
            return 0;
        }
        // 转换数据
        List<PiSistarRtStepParams> targetList = dataList.stream()
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
    private PiSistarRtStepParams convertToTarget(SourceSistarRtStepParams params) {
        //对象复制
        PiSistarRtStepParams target = BeanUtil.copyProperties(params, PiSistarRtStepParams.class);
        target.setTenantId(tenantId);
        // 拼接ID
        String id = buildCompleteId(params);
        target.setId(id);
        return target;
    }

    /**
     * 生成ID
     * @param params
     * @return
     */
    private String buildCompleteId(SourceSistarRtStepParams params) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
        String formattedTime = params.getStartTime() != null ?
                params.getStartTime().toInstant()
                        .atZone(ZoneId.systemDefault())
                        .format(formatter) : "00000000000000";

        return String.join("_",
                params.getSiteNo() != null ? params.getSiteNo().toString() : "NULL_SITE",
                params.getAreaNo() != null ? params.getAreaNo().toString() : "NULL_AREA",
                params.getYearNo() != null ? params.getYearNo().toString() : "NULL_YEAR",
                params.getReccatNo() != null ? params.getReccatNo().toString() : "NULL_RECCAT",
                params.getOrderNo() != null ? params.getOrderNo().toString() : "NULL_ORDER",
                params.getBatchNo() != null ? params.getBatchNo().toString() : "NULL_BATCH",
                params.getPcuNo() != null ? params.getPcuNo().toString() : "NULL_PCU",
                params.getEopNo() != null ? params.getEopNo().toString() : "NULL_EOP",
                formattedTime,
                params.getRopId() != null ? params.getRopId().toString() : "NULL_ROP",
                params.getUserProtId() != null ? params.getUserProtId().toString() : "NULL_USERPROT",
                params.getParamPos() != null ? params.getParamPos().toString() : "NULL_PARAM"
        );
    }


}
