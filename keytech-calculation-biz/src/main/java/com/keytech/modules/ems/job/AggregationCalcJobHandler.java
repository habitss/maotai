package com.keytech.modules.ems.job;

import com.keytech.modules.ems.constants.AggregationTypeEnum;
import com.keytech.modules.ems.util.Java8DateUtil;
import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import java.time.LocalDateTime;

/**
 * 聚合计算器 定时任务
 * @author yepeng
 * @since 2025.05.13
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AggregationCalcJobHandler {
    private final AggregationBatchProcessor aggregationBatchProcessor;
    private final CollectClassCalcExecutor collectClassCalcExecutor;
    private final ThirdpartyClassCalcExecutor thirdpartyClassCalcExecutor;
    private final CollectDayCalcExecutor collectDayCalcExecutor;
    private final ThirdpartyDayCalcExecutor thirdpartyDayCalcExecutor;
    private final OrsDataHourCalcExecutor orsDataHourCalcExecutor;
    private final OrsDataClassCalcExecutor orsDataClassCalcExecutor;
    private final OrsDataDayCalcExecutor orsDataDayCalcExecutor;
    private final OrsDataWeekCalcExecutor orSdataWeekCalcExecutor;
    private final OrsDataMtdCalcExecutor orsDataMtdCalcExecutor;
    private final OrsDataYtdCalcExecutor orsDataYtdCalcExecutor;

    /**
     * 中间点位小时聚合任务（XxlJob中设置：每隔 10 分执行）
     * 0 1/10 * * * ?
     */
    @XxlJob("orsHourAggregationCalcJob")
    public ReturnT<String> handleOrsHourAggregationCalcJob(String params) {
        if(params == null) {
            params = XxlJobHelper.getJobParam();
            log.info("参数：{}", params);
        }
        long now = System.currentTimeMillis();
        log.info("开始中间点小时聚合任务[{}]", LocalDateTime.now().format(Java8DateUtil.sdf1));
        aggregationBatchProcessor.execute(orsDataHourCalcExecutor, AggregationTypeEnum.ORS_DATA_HOUR);
        long now4 = System.currentTimeMillis();
        log.info("结束中间点小时聚合任务[{}], 耗时：{} ms", LocalDateTime.now().format(Java8DateUtil.sdf1), now4 - now);

        return ReturnT.SUCCESS;
    }

    /**
     * 采集和第三方点位班聚合->中间点班聚合 任务（XxlJob中设置：每隔 10 分执行）
     * 0 3/10 * * * ?
     */
    @XxlJob("classAggregationCalcJob")
    public ReturnT<String> handleClassAggregationCalcJob(String params) {
        if(params == null) {
            params = XxlJobHelper.getJobParam();
            log.info("参数：{}", params);
        }
        long now = System.currentTimeMillis();

        log.info("开始采集班聚合任务[{}]", LocalDateTime.now().format(Java8DateUtil.sdf1));
        aggregationBatchProcessor.execute(collectClassCalcExecutor, AggregationTypeEnum.COLLECT_SHIFT);
        long now2 = System.currentTimeMillis();
        log.info("结束采集班聚合任务[{}], 耗时：{} ms", LocalDateTime.now().format(Java8DateUtil.sdf1), now2 - now);

        log.info("开始第三方班聚合任务[{}]", LocalDateTime.now().format(Java8DateUtil.sdf1));
        aggregationBatchProcessor.execute(thirdpartyClassCalcExecutor, AggregationTypeEnum.THIRD_SHIFT);
        long now3 = System.currentTimeMillis();
        log.info("结束第三方班聚合任务[{}], 耗时：{} ms", LocalDateTime.now().format(Java8DateUtil.sdf1), now3 - now2);

        log.info("开始中间点班次聚合任务[{}]", LocalDateTime.now().format(Java8DateUtil.sdf1));
        aggregationBatchProcessor.execute(orsDataClassCalcExecutor, AggregationTypeEnum.ORS_DATA_SHIFT);
        long now4 = System.currentTimeMillis();
        log.info("结束中间点班次聚合任务[{}], 耗时：{} ms", LocalDateTime.now().format(Java8DateUtil.sdf1), now4 - now3);

        return ReturnT.SUCCESS;
    }

    /**
     * 采集点位天聚合->中间点天聚合->中间点周聚合->中间点月/年聚合（XxlJob中设置：每隔 10 分执行）
     * 0 7/10 * * * ?
     */
    @XxlJob("dayWeekMtdYtdAggregationCalcJob")
    public ReturnT<String> handleDayWeekMtdYtdAggregationCalcJob(String params) {
        if(params == null) {
            params = XxlJobHelper.getJobParam();
            log.info("参数：{}", params);
        }
        long now = System.currentTimeMillis();

        log.info("开始采集天聚合任务[{}]", LocalDateTime.now().format(Java8DateUtil.sdf1));
        aggregationBatchProcessor.execute(collectDayCalcExecutor, AggregationTypeEnum.COLLECT_DAY);
        long now2= System.currentTimeMillis();
        log.info("结束采集天聚合任务[{}], 耗时：{} ms", LocalDateTime.now().format(Java8DateUtil.sdf1), now2 - now);

        log.info("开始第三方天聚合任务[{}]", LocalDateTime.now().format(Java8DateUtil.sdf1));
        aggregationBatchProcessor.execute(thirdpartyDayCalcExecutor, AggregationTypeEnum.THIRD_DAY);
        long now3= System.currentTimeMillis();
        log.info("结束第三方天聚合任务[{}], 耗时：{} ms", LocalDateTime.now().format(Java8DateUtil.sdf1), now3 - now2);

        log.info("开始中间点天聚合任务[{}]", LocalDateTime.now().format(Java8DateUtil.sdf1));
        aggregationBatchProcessor.execute(orsDataDayCalcExecutor, AggregationTypeEnum.ORS_DATA_DAY);
        long now4 = System.currentTimeMillis();
        log.info("结束中间点天聚合任务[{}], 耗时：{} ms", LocalDateTime.now().format(Java8DateUtil.sdf1), now4 - now);

        log.info("开始中间点周聚合任务[{}]", LocalDateTime.now().format(Java8DateUtil.sdf1));
        aggregationBatchProcessor.execute(orSdataWeekCalcExecutor, AggregationTypeEnum.ORS_DATA_WEEK);
        long now5 = System.currentTimeMillis();
        log.info("结束中间点周聚合任务[{}], 耗时：{} ms", LocalDateTime.now().format(Java8DateUtil.sdf1), now5 - now4);

        log.info("开始中间点MTD聚合任务[{}]", LocalDateTime.now().format(Java8DateUtil.sdf1));
        aggregationBatchProcessor.execute(orsDataMtdCalcExecutor, AggregationTypeEnum.ORS_DATA_MTD);
        long now6 = System.currentTimeMillis();
        log.info("结束中间点MTD聚合任务[{}], 耗时：{} ms", LocalDateTime.now().format(Java8DateUtil.sdf1), now6 - now5);

        log.info("开始中间点YTD聚合任务[{}]", LocalDateTime.now().format(Java8DateUtil.sdf1));
        aggregationBatchProcessor.execute(orsDataYtdCalcExecutor, AggregationTypeEnum.ORS_DATA_YTD);
        long now7 = System.currentTimeMillis();
        log.info("结束中间点YTD聚合任务[{}], 耗时：{} ms", LocalDateTime.now().format(Java8DateUtil.sdf1), now7 - now6);

        return ReturnT.SUCCESS;
    }

}
