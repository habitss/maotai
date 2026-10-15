package com.keytech.modules.ems.service.impl;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.keytech.base.service.impl.BaseServiceImpl;
import com.keytech.modules.ems.constants.*;
import com.keytech.modules.ems.entity.CalcWarningLog;
import com.keytech.modules.ems.entity.CollectPointStatus;
import com.keytech.modules.ems.entity.CollectTag;
import com.keytech.modules.ems.mapper.CalcWarningLogMapper;
import com.keytech.modules.ems.mapper.CollectPointStatusMapper;
import com.keytech.modules.ems.mapper.CollectTagMapper;
import com.keytech.modules.ems.service.ICollectPointStatusService;
import com.keytech.modules.ems.util.Java8DateUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 点位采集状态表
 * @author yepeng
 * @since 2025.04.29
 * */
@Slf4j
@Service
@RequiredArgsConstructor
public class CollectPointStatusServiceImpl extends BaseServiceImpl<CollectPointStatusMapper, CollectPointStatus> implements ICollectPointStatusService {
    private final CollectTagMapper collectTagMapper;
    private final CalcWarningLogMapper calcWarningLogMapper;


    /**
     * 查询指定点位最近的一条有参考底数的状态记录
     * @param tagIds 点位编码列表
     * @param collectTime 指定采集时间
     */
    @Override
    public List<CollectPointStatus> selectLastHadBaseValRecordsByPoints(List<String> tagIds, LocalDateTime collectTime) {
        return baseMapper.selectLastHadBaseValRecordsByPoints(tagIds, collectTime);
    }

    /**
     * 查询指定条件的点位采集状态同时查出FluxCode
     * @param queryWrapper 查询条件
     */
    @Override
    public List<CollectPointStatus> selectFluxList(Wrapper<CollectPointStatus> queryWrapper) {
        List<CollectPointStatus> list = baseMapper.selectList(queryWrapper);
        if (!list.isEmpty()) {
            List<String> tagIds = list.stream().map(CollectPointStatus::getTagId).toList();
            List<CollectTag> tags = this.collectTagMapper.selectList(new LambdaQueryWrapper<CollectTag>().select(Arrays.asList(CollectTag::getTagId, CollectTag::getPointCode)).in(CollectTag::getTagId, tagIds).eq(CollectTag::getCollectionType,1));
            Map<String, String> fluxCodeMap = tags.stream().collect(Collectors.toMap(CollectTag::getTagId, CollectTag::getPointCode, (k1, k2) -> k1));
            list.forEach(item -> {
                if (fluxCodeMap.containsKey(item.getTagId())) {
                    item.setFluxCode(fluxCodeMap.get(item.getTagId()));
                }
            });
        }
        return list;
    }

    /**
     * 查询当前时间周期性任务(半小时/小时)
     * @param grainType 0-半小时 1-小时
     */
    @Override
    public List<CollectPointStatus> queryPendingForPeriodic(Integer grainType) {
        LocalDateTime now = LocalDateTime.now();
        // 计算出最近的 整点 或 30分 并往前移一个周期得到时间段起点
        LocalDateTime lastHalfHourOrFullHour;
        if (grainType == GrainTypeEnum.HOUR.getVal() || now.getMinute() < 30) {
            // 整点
            lastHalfHourOrFullHour = now.truncatedTo(ChronoUnit.HOURS);
        } else {
            // 30 分
            lastHalfHourOrFullHour = now.truncatedTo(ChronoUnit.HOURS).plusMinutes(30);
        }
        LocalDateTime stime = grainType == GrainTypeEnum.HOUR.getVal() ? lastHalfHourOrFullHour.minusHours(1) : lastHalfHourOrFullHour.minusMinutes(30);
        LambdaQueryWrapper<CollectPointStatus> query = Wrappers.lambdaQuery();
        query.eq(CollectPointStatus::getFinishFlag, StatusFinishFlagEnum.UNDONE.getVal())
                .eq(CollectPointStatus::getErrorFlag, CollectErrorFlagEnum.NORMAL)
                .eq(CollectPointStatus::getGrainType, grainType)
                .eq(CollectPointStatus::getCollectTime, stime);
        return this.selectFluxList(query);
    }

    /**
     * 查询自动重试未完成任务(半小时/小时)
     * @param maxRetry 最大重试次数
     * @param grainType 0-半小时 1-小时
     * @param minusHours 指定起始时间为当前时间前几小时
     */
    @Override
    public List<CollectPointStatus> queryPendingForRetry(int maxRetry, Integer grainType, Integer minusHours) {
        LocalDateTime now = LocalDateTime.now().withMinute(0).withSecond(0).withNano(0);
        LocalDateTime startTime = null;
        if(minusHours != null) {
            startTime = now.minusHours(minusHours);
        }
        LambdaQueryWrapper<CollectPointStatus> query = Wrappers.lambdaQuery();
        query.eq(CollectPointStatus::getFinishFlag, StatusFinishFlagEnum.UNDONE.getVal())
                .eq(CollectPointStatus::getErrorFlag, CollectErrorFlagEnum.UNUSUAL.getVal())
                .lt(CollectPointStatus::getRetryCount, maxRetry)
                .le(CollectPointStatus::getCollectTime, now).orderByAsc(CollectPointStatus::getCollectTime);
        if (grainType != null) {
            query.eq(CollectPointStatus::getGrainType, grainType);
        }
        if (startTime != null) {
            query.ge(CollectPointStatus::getCollectTime, startTime);
        }
        query.last("LIMIT 200" );
        return this.selectFluxList(query);
    }

    /**
     * 查询人工补采未设置参考值的记录(防止CPU卡死，只取所有点位最早24小时内的未计算的状态记录)
     * @param grainType 0-半小时 1-小时
     * @param stime 开始时间
     * @param etime 结束时间
     * @param tagIds 点位编码列表
     */
    @Override
    public List<CollectPointStatus> queryNotLastBaseValBackfill(Integer grainType, LocalDateTime stime, LocalDateTime etime, List<String> tagIds) {
        //先查出最早的还未计算的点位采集时间，然后根据这个时间再查出所有点位最早24小时内的未计算的状态记录
        final int _limitHours = 24;
        LocalDateTime startTime = null;
        List<Object> objects = baseMapper.selectObjs(new QueryWrapper<CollectPointStatus>().select("MIN(collect_time)").eq("finish_flag", StatusFinishFlagEnum.UNDONE.getVal())
                .eq("error_flag", CollectErrorFlagEnum.NORMAL.getVal()).in(CollectionUtils.isNotEmpty(tagIds), "tag_id", tagIds).eq(grainType != null, "grain_type", grainType));
        String minCollectTime = (objects == null || objects.isEmpty() || objects.get(0) == null) ? null : objects.get(0).toString();
        if (minCollectTime == null) {
            return Collections.emptyList();
        }else {
            startTime = Java8DateUtil.parse(StringUtils.replace(minCollectTime, "T", " "));
        }
        LambdaQueryWrapper<CollectPointStatus> query = Wrappers.lambdaQuery();
        query.select(Arrays.asList(CollectPointStatus::getId,CollectPointStatus::getTagId,CollectPointStatus::getGrainType,CollectPointStatus::getCollectTime))
                .eq(CollectPointStatus::getFinishFlag, StatusFinishFlagEnum.UNDONE.getVal()).eq(CollectPointStatus::getErrorFlag, CollectErrorFlagEnum.NORMAL.getVal())
                .eq(CollectPointStatus::getLastBaseVal, 0).eq(CollectPointStatus::getLastUsage, 0).orderByAsc(CollectPointStatus::getCollectTime);
        if (tagIds != null && !tagIds.isEmpty()) {
            query.in(CollectPointStatus::getTagId, tagIds);
        }
        if (grainType != null) {
            query.eq(CollectPointStatus::getGrainType, grainType);
        }
        if (stime != null) {
            query.ge(CollectPointStatus::getCollectTime, stime);
        }else {
            query.ge(CollectPointStatus::getCollectTime, startTime);
        }
        if (etime != null) {
            query.lt(CollectPointStatus::getCollectTime, etime);
        }else {
            query.lt(CollectPointStatus::getCollectTime, startTime.plusHours(_limitHours));
        }
        List<CollectPointStatus> pointStatusList = this.selectFluxList(query);
        log.warn("本轮需要获取人工补采未设置参考值的记录数量:{}, 时间段：{}-{}", pointStatusList.size(), startTime, startTime.plusHours(_limitHours));
        return pointStatusList;
    }

    /**
     * 查询人工补采逻辑任务(半小时/小时)
     * @param grainType 0-半小时 1-小时
     * @param stime 开始时间
     * @param etime 结束时间
     * @param tagIds 点位编码列表
     */
    @Override
    public List<CollectPointStatus> queryPendingForBackfill(Integer grainType, LocalDateTime stime, LocalDateTime etime, List<String> tagIds) {
        //人工补采只能针对当前周期前一小时的点位，如果是手动设置历史时间，需要等参考值计算完成才能进行补采
        if(etime.isAfter(LocalDateTime.now().minusHours(1))) {
            etime = LocalDateTime.now().minusHours(1);
        }
        LambdaQueryWrapper<CollectPointStatus> query = Wrappers.lambdaQuery();
        query.eq(CollectPointStatus::getFinishFlag, StatusFinishFlagEnum.UNDONE.getVal())
                .eq(CollectPointStatus::getErrorFlag, CollectErrorFlagEnum.NORMAL.getVal()).gt(CollectPointStatus::getLastBaseVal, 0)
                .le(CollectPointStatus::getCollectTime, etime).orderByAsc(CollectPointStatus::getCollectTime);
        if (tagIds != null && !tagIds.isEmpty()) {
            query.in(CollectPointStatus::getTagId, tagIds);
        }
        if (grainType != null) {
            query.eq(CollectPointStatus::getGrainType, grainType);
        }
        if (stime != null) {
            query.ge(CollectPointStatus::getCollectTime, stime);
        }
        //如果数量太多不分批一次全部补采容易卡死
        return this.selectFluxList(query);
    }

    /**
     * 查询指定采集粒度指定时间段指定点位需要补采的状态集合(兼容新的周期时段)
     * 自动过滤掉数据库中存在的记录
     * @param grainType 0-半小时 1-小时
     * @param stime 开始时间
     * @param etime 结束时间
     * @param emsPointList 点位列表
     */
    @Override
    public List<CollectPointStatus> getBackfillPointStatusList(Integer grainType, LocalDateTime stime, LocalDateTime etime, List<CollectTag> emsPointList) {
        if (emsPointList == null || emsPointList.isEmpty()) {
            log.warn("点位列表为空");
            return Collections.emptyList();
        }
        String factoryCode = emsPointList.get(0).getFactoryCode();
        // 获取当前时间段（从 stime 到 etime）内所有小时/半小时的点位状态
        QueryWrapper<CollectPointStatus> queryWrapper = new QueryWrapper<CollectPointStatus>().select("factory_code,tag_id,collect_time")
                .between(EmsConstantsX.COLLECT_TIME, stime, etime).eq("grain_type", grainType).eq("factory_code", factoryCode)
                .in("tag_id", emsPointList.stream().map(CollectTag::getTagId).toList()).orderByAsc(EmsConstantsX.COLLECT_TIME);
        List<CollectPointStatus> pointStatusList = baseMapper.selectList(queryWrapper);
        Set<String> existingKeys = pointStatusList.stream().map(e -> e.getFactoryCode() + "_" + e.getTagId() + "_" + e.getCollectTime().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))).collect(Collectors.toSet());
        // 构造批量插入记录
        List<CollectPointStatus> toInsertList = new ArrayList<>();
        while (stime.isBefore(etime)) {
            // 针对当前整点小时生成点位状态
            LocalDateTime collectTime = stime;
            // 对于每一个点位，检查是否存在记录
            emsPointList.stream().filter(f -> !existingKeys.contains(f.getFactoryCode() + "_" + f.getTagId() + "_" + collectTime.format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))))
                    .forEach(f -> {
                        CollectPointStatus status = new CollectPointStatus();
                        status.setTenantId(f.getTenantId());
                        status.setFactoryCode(f.getFactoryCode());
                        status.setDeptCode(f.getDeptCode());
                        status.setLineCode(f.getLineCode());
                        status.setTagId(f.getTagId());
                        status.setGrainType(grainType);
                        status.setCollectTime(collectTime);
                        status.setLastBaseVal(BigDecimal.ZERO);
                        status.setLastUsage(BigDecimal.ZERO);
                        status.setFinishFlag(0);
                        status.setErrorFlag(0);
                        status.setRetryCount(0);
                        status.setFluxCode(f.getPointCode());
                        status.setCreateBy("system");
                        toInsertList.add(status);
                    });
            // 移动到下一个整点或半小时
            if (grainType == GrainTypeEnum.HOUR.getVal()) {
                stime = stime.plusHours(1);
            }else {
                stime = stime.plusMinutes(30);
            }
        }
        return toInsertList;
    }

    /**
     * 获取需要采集的点位配置
     * 同时处理未配置FluxCode的点位，生成预警记录
     * @param grainType 时间粒度
     * @return 有效点位列表
     */
    public List<CollectTag> getCollectPoints(int grainType) {
        List<CollectTag> collectTagList = collectTagMapper.selectList(new QueryWrapper<CollectTag>()
                .select("tenant_id,factory_code,dept_code,line_code,device_id,point_code,tag_id")
                .ne("energy_type", "").eq("collect_period", grainType)
        );
        //未配置 查询编码的 发送Webhook
        List<CollectTag> noFluxCodeList = collectTagList.stream().filter(s -> StringUtils.isBlank(s.getPointCode())).toList();
        if (!noFluxCodeList.isEmpty()) {
            log.warn("未配置FluxCode编码的点位：{} 个", noFluxCodeList.size());
            // 创建预警记录
            CollectTag first = noFluxCodeList.get(0);
            CalcWarningLog warningLog = new CalcWarningLog();
            warningLog.setFactoryCode(first.getFactoryCode());
            warningLog.setDeptCode(noFluxCodeList.stream().map(CollectTag::getDeptCode).distinct().collect(Collectors.joining(",")));
            warningLog.setGrainType(grainType);
            warningLog.setTagKind(TagKindEnum.COLLECT.getVal());
            warningLog.setTimePoint(LocalDateTime.now());
            // 设置点位ID（多个用逗号分隔）
            String tagIds = noFluxCodeList.stream().map(CollectTag::getTagId).distinct().collect(Collectors.joining(","));
            // 表字段限制了1500字符
            warningLog.setTagId(tagIds.substring(0, Math.min(tagIds.length(), 1500)));
            // 设置预警信息
            warningLog.setWarnMsg(CollectErrorTypeEnum.NOT_SET_FLUX_CODE.getName());
            warningLog.setWarnLevel(WarnLevelEnum.HIGH.getVal());
            this.calcWarningLogMapper.insert(warningLog);
        }
        return collectTagList.stream().filter(s -> StringUtils.isNotBlank(s.getPointCode())).toList();
    }

}
