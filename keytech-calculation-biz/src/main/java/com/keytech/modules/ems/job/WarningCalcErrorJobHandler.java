package com.keytech.modules.ems.job;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.keytech.modules.ems.constants.*;
import com.keytech.modules.ems.entity.CalcWarningLog;
import com.keytech.modules.ems.entity.CollectErrorLog;
import com.keytech.modules.ems.service.ICalcWarningLogService;
import com.keytech.modules.ems.service.ICollectErrorLogService;
import com.keytech.modules.ems.util.Java8DateUtil;
import com.keytech.modules.ems.util.WebhookSendHelper;
import com.keytech.modules.ems.vo.WarningPushVo;
import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.modules.system.entity.SysDepart;
import org.jeecg.modules.system.service.ISysDepartService;
import org.springframework.stereotype.Component;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 采集和聚合计算异常预警任务
 * @author yepeng
 * @since 2025.05.23
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class WarningCalcErrorJobHandler {
    private final ICollectErrorLogService collectErrorLogService;
    private final ICalcWarningLogService calcWarningLogService;
    private final ISysDepartService sysDepartService;

    /**
     * 采集任务异常预警扫描（XxlJob中设置：每小时 0 分 0 秒执行）
     * 0 0 0/1 * * ?
     */
    @XxlJob("warningCalcErrorJob")
    public ReturnT<String> handleWarningCalcErrorJob(String params) {
        if(params == null) {
            params = XxlJobHelper.getJobParam();
            log.info("参数：{}", params);
        }
        log.info("[采集异常预警扫描任务] 启动...");
        try{
            // 1. 获取上次执行时间（CalcWarningLog表中最大的update_time）
            LocalDateTime lastExecuteTime = getLastExecuteTime();

            // 2. 查询异常记录
            List<CollectErrorLog> errorLogs = queryErrorLogs(lastExecuteTime);

            // 3. 按工厂、部门、时间和错误类型分组处理
            int size = 0;
            if (!errorLogs.isEmpty()) {
                size = processErrorLogs(errorLogs);
            } else {
                processNoError();
            }
            if (size > 0) {
                log.info("本次任务扫描并产生预警记录 {} 条", size);
            }else {
                log.info("本次任务未扫描到预警记录，所有点位采集正常");
            }
            return ReturnT.SUCCESS;
        } catch (Exception e) {
            log.error("warningCalcErrorJob 任务执行失败", e);
            return new ReturnT<>(ReturnT.FAIL_CODE, "warningCalcErrorJob 执行失败：" + e.getMessage());
        } finally {
            log.info("[采集异常预警扫描任务] 结束...");
        }
    }

    /**
     * 异常预警消息推送任务（XxlJob中设置：每 5 分 0 秒执行）
     * 0 0/5 * * * ?
     */
    @XxlJob("warningMsgPushJob")
    public ReturnT<String> handleWarningMsgPushJob(String params) {
        if(params == null) {
            params = XxlJobHelper.getJobParam();
            log.info("参数：{}", params);
        }
        log.info("[异常预警消息推送任务] 启动...");
        try{
            // 1.查找最近24小时内未推送的预警记录（太久远已经没有推送的必要了）
            List<CalcWarningLog> warningLogs = calcWarningLogService.list(new LambdaQueryWrapper<CalcWarningLog>()
                    .eq(CalcWarningLog::getPushFlag, YnEnum.NO).gt(CalcWarningLog::getWarnLevel, 0)
                    .gt(CalcWarningLog::getTimePoint, LocalDateTime.now().truncatedTo(ChronoUnit.DAYS).minusDays(10)));
            if (warningLogs.isEmpty()) {
                log.info("没有需要推送的预警记录");
                return ReturnT.SUCCESS;
            }

            // 取出所有组织机构转换为Map
            List<SysDepart> deptList = this.sysDepartService.list(new LambdaQueryWrapper<SysDepart>().select(Arrays.asList(SysDepart::getOrgCode, SysDepart::getDepartName))
                    .eq(SysDepart::getOrgType, SysOrgTypeEnum.DEPART.getVal()));
            Map<String, String> deptMap = deptList.stream().collect(Collectors.toMap(SysDepart::getOrgCode, SysDepart::getDepartName, (k1, k2) -> k1));

            // 2.基于 warningLogs 构建推送消息 WarningPushVo 列表
            List<WarningPushVo> pushVos = warningLogs.stream()
                .sorted(Comparator.comparingInt(CalcWarningLog::getWarnLevel).reversed().thenComparing(CalcWarningLog::getTimePoint))
                .collect(Collectors.groupingBy(log ->
                    String.join("_", log.getTimePoint().format(Java8DateUtil.sdf_ymdh_0), String.valueOf(log.getWarnLevel()))
                )).values().stream().map(warnLogs -> {
                    Map<String, List<CalcWarningLog>> typeGroups = warnLogs.stream().collect(Collectors.groupingBy(CalcWarningLog::getWarnMsg));

                    int warnLevel = warnLogs.get(0).getWarnLevel();
                    StringBuilder markdown = new StringBuilder(warnLevel == WarnLevelEnum.HIGH.getVal() ? "<font color='warning'>" : "");
                    markdown.append("【").append(WarnLevelEnum.getNameByVal(warnLevel)).append("】[")
                            .append(warnLogs.get(0).getTimePoint().format(DateTimeFormatter.ofPattern("MM/dd HH时"))).append("]预警信息")
                            .append(warnLevel == WarnLevelEnum.HIGH.getVal() ? "</font>\n" : "\n");
                    typeGroups.forEach((warnMsg, logs) -> {
                        markdown.append("### ").append(warnMsg).append("：\n");
                        logs.stream().collect(Collectors.groupingBy(CalcWarningLog::getDeptCode)).forEach((orgCode, deptLogs) -> {
                            List<String> tagIds = deptLogs.stream().map(CalcWarningLog::getTagId).distinct().toList();
                            markdown.append(Arrays.stream(orgCode.split(",")).map(code -> deptMap.getOrDefault(code, code)).collect(Collectors.joining(",")));
                            markdown.append(" (").append(tagIds.stream().flatMap(id -> Arrays.stream(id.split(","))).count()).append(") <font color='comment'>");
                            List<String> allTagIds = tagIds.stream().flatMap(id -> Arrays.stream(id.split(","))).toList();
                            markdown.append(allTagIds.stream().limit(5).collect(Collectors.joining(",")));
                            markdown.append(allTagIds.size() > 5 ? "..." : "").append("\n");
                            markdown.append("</font>");
                        });
                        markdown.append("\n").append("查看详情请进入EMS系统采集预警日志列表");
                    });

                    WarningPushVo vo = new WarningPushVo();
                    vo.setTimePoint(warnLogs.get(0).getTimePoint());
                    vo.setContent(markdown.toString());
                    vo.setLogIdList(warnLogs.stream().map(CalcWarningLog::getId).distinct().toList());
                    return vo;
                }).sorted(Comparator.comparing(WarningPushVo::getTimePoint)).limit(20).toList();

            // 3. 推送消息
            this.pushMsgToWebhook(pushVos);
            return ReturnT.SUCCESS;
        } catch (Exception e) {
            log.error("warningMsgPushJob 任务执行失败", e);
            return new ReturnT<>(ReturnT.FAIL_CODE, "warningMsgPushJob 执行失败：" + e.getMessage());
        } finally {
            log.info("[异常预警消息推送任务] 结束...");
        }
    }

    /**
     * 获取上次执行时间，如果上次时间为空，取最近 7天 的时间，太久远的数据不再计算预警日志
     */
    private LocalDateTime getLastExecuteTime() {
        CalcWarningLog lastWarning = calcWarningLogService.getOne(new LambdaQueryWrapper<CalcWarningLog>()
                        .orderByDesc(CalcWarningLog::getUpdateTime).last("limit 1"));
        return lastWarning != null ? lastWarning.getUpdateTime() : LocalDateTime.now().truncatedTo(ChronoUnit.DAYS).minusDays(7);
    }

    /**
     * 查询异常记录
     */
    private List<CollectErrorLog> queryErrorLogs(LocalDateTime startTime) {
        LambdaQueryWrapper<CollectErrorLog> wrapper = new LambdaQueryWrapper<>();
        if (startTime != null) {
            wrapper.ge(CollectErrorLog::getCreateTime, startTime);
        }
        return this.collectErrorLogService.list(wrapper);
    }

    /**
     * 处理异常记录
     */
    private int processErrorLogs(List<CollectErrorLog> errorLogs) {
        // 按工厂、采集时间（小时）和错误类型分组
        Map<String, List<CollectErrorLog>> groups = errorLogs.stream().collect(Collectors.groupingBy(log ->
                String.join("_", log.getFactoryCode(), log.getLineCode(), log.getCollectTime().format(Java8DateUtil.sdf_ymdhm_0), String.valueOf(log.getErrorType()))));

        List<CalcWarningLog> warningLogs = new ArrayList<>();

        // 处理每个分组
        for (Map.Entry<String, List<CollectErrorLog>> entry : groups.entrySet()) {
            List<CollectErrorLog> group = entry.getValue();
            CollectErrorLog first = group.get(0);

            // 创建预警记录
            CalcWarningLog warningLog = new CalcWarningLog();
            warningLog.setFactoryCode(first.getFactoryCode());
            warningLog.setDeptCode(group.stream().map(CollectErrorLog::getDeptCode).distinct().collect(Collectors.joining(",")));
            warningLog.setGrainType(GrainTypeEnum.HOUR.getVal());
            warningLog.setTagKind(TagKindEnum.COLLECT.getVal());
            warningLog.setTimePoint(first.getCollectTime());
            // 设置点位ID（多个用逗号分隔）
            String tagIds = group.stream().map(CollectErrorLog::getTagId).distinct().collect(Collectors.joining(","));
            // 表字段限制了1500字符
            warningLog.setTagId(tagIds.substring(0, Math.min(tagIds.length(), 1500)));
            // 设置预警信息
            warningLog.setWarnMsg(CollectErrorTypeEnum.getNameByVal(first.getErrorType()));
            // 设置预警等级
            int errorCode = first.getErrorType();
            if (errorCode == CollectErrorTypeEnum.NETWORK_ERROR.getVal() || errorCode == CollectErrorTypeEnum.NULL_DATA_ERROR.getVal()
                    || errorCode == CollectErrorTypeEnum.ALL_ZERO_ERROR.getVal() || errorCode == CollectErrorTypeEnum.NOT_SET_FLUX_CODE.getVal()
                    || errorCode == CollectErrorTypeEnum.ALL_NEGATE_ERROR.getVal()) {
                warningLog.setWarnLevel(WarnLevelEnum.HIGH.getVal());
            } else if (errorCode == CollectErrorTypeEnum.JUMP_MIN_ERROR.getVal() || errorCode == CollectErrorTypeEnum.JUMP_MAX_ERROR.getVal()) {
                warningLog.setWarnLevel(WarnLevelEnum.MID.getVal());
            } else if (errorCode == CollectErrorTypeEnum.MANUAL_ZERO_ERROR.getVal()) {
                warningLog.setWarnLevel(WarnLevelEnum.LOW.getVal());
            }
            warningLogs.add(warningLog);
        }
        // 批量保存预警记录
        this.calcWarningLogService.saveBatch(warningLogs);
        return warningLogs.size();
    }

    /**
     * 处理无异常情况
     */
    private void processNoError() {
        // 查找无异常记录(只有一条)
        LocalDateTime now = LocalDateTime.now();
        CalcWarningLog existingLog = calcWarningLogService.getOne(new LambdaQueryWrapper<CalcWarningLog>().eq(CalcWarningLog::getWarnLevel, 0).eq(CalcWarningLog::getTagKind, 0).last("LIMIT 1"));
        if (existingLog != null) {
            // 更新已存在的无异常记录
            calcWarningLogService.update(new LambdaUpdateWrapper<CalcWarningLog>().eq(CalcWarningLog::getId, existingLog.getId()).set(CalcWarningLog::getUpdateTime, now));
        } else {
            // 创建新的无异常记录
            CalcWarningLog warningLog = new CalcWarningLog();
            warningLog.setGrainType(0);
            warningLog.setTagKind(0);
            warningLog.setTimePoint(now);
            warningLog.setWarnMsg("正常");
            warningLog.setWarnLevel(0);
            this.calcWarningLogService.save(warningLog);
        }
    }

    /**
     * 推送消息到指定WebHook
     * @param pushVos 推送消息列表
     */
    private void pushMsgToWebhook(List<WarningPushVo> pushVos) {
        for (WarningPushVo vo : pushVos) {
            boolean pushResult = WebhookSendHelper.sendMarkdownMsg("d7789373-331a-4cec-8211-6697d039ce35", vo.getContent());
            if (pushResult) {
                log.info("预警消息推送成功");
                // 更新预警记录状态为已推送
                LambdaUpdateWrapper<CalcWarningLog> updateWrapper = new LambdaUpdateWrapper<>();
                updateWrapper.set(CalcWarningLog::getPushFlag, YnEnum.YES.getVal()).in(CalcWarningLog::getId, vo.getLogIdList());
                calcWarningLogService.update(updateWrapper);
            } else {
                log.error("预警消息推送失败");
            }
        }
    }

}
