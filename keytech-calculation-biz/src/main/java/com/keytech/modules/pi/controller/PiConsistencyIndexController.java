package com.keytech.modules.pi.controller;


import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.keytech.base.controller.BaseController;
import com.keytech.modules.pi.dto.AlgorithmDataDto;
import com.keytech.modules.pi.dto.PiGreenDataDto;
import com.keytech.modules.pi.dto.PiMonWeekSummaryDto;
import com.keytech.modules.pi.entity.*;
import com.keytech.modules.pi.service.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.aspect.annotation.AutoDict;
import org.jeecg.common.aspect.annotation.AutoLog;
import org.jeecg.common.config.GlobalErrorEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * @Description: 绿区率统计/一致性指数
 * @Author: aolei
 * @Date:   2025-01-12
 * @Version: V1.0
 */
@Tag(name="一致性指数")
@RestController
@RequestMapping("/pi/consistencyIndex")
@Slf4j
public class PiConsistencyIndexController extends BaseController<IPiStandardDataService, PiStandardData> {

    @Resource
    private IPiStandardDataService piStandardDataService;

    /**
     * 一致性指数配置主表
     */
    @Resource
    private IConsistencyConfigZbService consistencyConfigZbService;

    /**
     * 一致性指数配置明细表
     */
    @Resource
    private IConsistencyConfigMxService consistencyConfigMxService;

    /**
     * PI绿区率查询
     * @param tableName 表中文名称
     * @param tableId 表id（PI报表ID）
     * @param queryField 查询字段名称
     * @param timeBegin 开始时间
     * @param timeEnd 结束时间
     * @param req 请求参数
     * @return 结果集合
     */
    @AutoLog(value = "PI绿区率查询")
    @Operation(summary = "PI绿区率查询", description = "PI绿区率查询")
    @GetMapping(value = "/piGreenData")
    public Result<List<PiGreenDataDto>> piGreenData(
                                                        @RequestParam(name = "tableName",required = false) String tableName,
                                                        @RequestParam(name = "tableId",required = false) String tableId,
                                                        @RequestParam(name = "tableCode",required = false) String tableCode,
                                                        @RequestParam(name = "queryField",required = false) String queryField,
                                                        @RequestParam(name = "timeBegin",required = false) String timeBegin,
                                                        @RequestParam(name = "timeEnd",required = false) String timeEnd,
                                                        HttpServletRequest req
    ) {
        List<PiGreenDataDto> retDataList = new ArrayList<>();
        if(StrUtil.isNotBlank(tableId) && StrUtil.isNotBlank(queryField) && StrUtil.isNotBlank(timeBegin) && StrUtil.isNotBlank(timeEnd)){
            retDataList = piStandardDataService.piGreenData(tableName,tableId,tableCode,queryField,timeBegin,timeEnd);
        }
        return Result.OK(retDataList);
    }



    /**
     * 首页重点关注pi查询
     * @param factoryCode 工厂编码
     * @param deptCode 部门编码
     * @return 结果集合
     */
    @AutoLog(value = "首页重点关注pi查询")
    @Operation(summary = "首页重点关注pi查询", description = "首页重点关注pi查询")
    @GetMapping(value = "/getPayCloseAttentionPi")
    @AutoDict
    public Result<List<PiGreenDataDto>> getPayCloseAttentionPi(
            @RequestParam(name = "factoryCode",required = false) String factoryCode,
            @RequestParam(name = "deptCode",required = false) String deptCode
    ) {
        Date now = new Date();
        // 获取本周第一天（默认以周一为开始）,true 表示将周一作为一周的开始
        Date firstDayOfWeek = DateUtil.beginOfWeek(now, true);
        // 获取本周最后一天（周日）,true 表示将周一作为一周的开始
        Date lastDayOfWeek = DateUtil.endOfWeek(now, true);
        // 格式化输出
        String startTime = DateUtil.format(firstDayOfWeek, "yyyy-MM-dd HH:mm:ss");
        String endTime = DateUtil.format(lastDayOfWeek, "yyyy-MM-dd HH:mm:ss");
        return Result.OK(piStandardDataService.getPayCloseAttentionPi(factoryCode, deptCode, "1", startTime, endTime));
    }


    /**
     * 一致性指数查询
     * @param consistencyType 类型 1一级指数，2二级指数
     * @param zbIds 主表id集合
     * @param timeBegin 开始时间
     * @param timeEnd 结束时间
     * @param req
     * @return
     */
    @AutoLog(value = "一致性指数查询")
    @Operation(summary = "一致性指数查询", description = "一致性指数查询")
    @GetMapping(value = "/getConsistencyData")
    public Result<List<ConsistencyConfigZb>> getConsistencyData(
            @RequestParam(name = "consistencyType",required = false) String consistencyType,
            @RequestParam(name = "zbIds",required = false) String zbIds,
            @RequestParam(name = "timeBegin",required = true) String timeBegin,
            @RequestParam(name = "timeEnd",required = true) String timeEnd,
            HttpServletRequest req
    ) {
        List<ConsistencyConfigZb> zbList = new ArrayList<>();
        if(StrUtil.isBlank(zbIds)){
            if(StrUtil.isBlank(consistencyType)){
                return Result.OK();
            }else{
                zbList = consistencyConfigZbService.lambdaQuery().eq(ConsistencyConfigZb::getConsistencyType,consistencyType).list();
            }
        }else{
            List<String> ids = Arrays.stream(zbIds.split(",")).toList();
            zbList = consistencyConfigZbService.lambdaQuery().in(ConsistencyConfigZb::getId,ids).list();
        }
        zbList = piStandardDataService.getConsistencyIndexBySum(zbList,timeBegin,timeEnd);
        return Result.OK(zbList);
    }

    /**
     * 一致性指数详情查询
     * @param zbId 主表id
     * @param timeBegin 开始时间
     * @param timeEnd 结束时间
     * @return
     */
    @AutoLog(value = "一致性指数详情查询")
    @Operation(summary = "一致性指数详情查询", description = "一致性指数详情查询")
    @GetMapping(value = "/getConsistencyDetailData")
    public Result<List<ConsistencyConfigMx>> getConsistencyData(
            @RequestParam(name = "zbId") String zbId,
            @RequestParam(name = "timeBegin") String timeBegin,
            @RequestParam(name = "timeEnd") String timeEnd
    ) {
        List<ConsistencyConfigMx> mxList = consistencyConfigMxService.lambdaQuery().eq(ConsistencyConfigMx::getZbId,zbId).list();
        return Result.OK(piStandardDataService.getConsistencyIndexByDetails(mxList, timeBegin, timeEnd));
    }

    /**
     * PI绿区率查询-根据部门查询
     * @param deptCode 部门编码
     * @param lineCode 产线编码
     * @param piName PI名称
     * @param tableIds 表id（PI报表ID）
     * @param timeBegin 开始时间
     * @param timeEnd 结束时间
     * @return 结果集合
     */
    @AutoLog(value = "PI绿区率查询-根据部门查询")
    @Operation(summary = "PI绿区率查询-根据部门查询", description = "PI绿区率查询-根据部门查询")
    @GetMapping(value = "/piGreenDataByDept")
    public Result<List<PiGreenDataDto>> piGreenDataByDept(
            @RequestParam(name = "deptCode") String deptCode,
            @RequestParam(name = "lineCode",required = false) String lineCode,
            @RequestParam(name = "piName",required = false) String piName,
            @RequestParam(name = "tableIds",required = false) String tableIds,
            @RequestParam(name = "queryField",required = false) String queryField,
            @RequestParam(name = "timeBegin",required = false) String timeBegin,
            @RequestParam(name = "timeEnd",required = false) String timeEnd
    ) {
        List<PiGreenDataDto> retDataList = piStandardDataService.piGreenDataByDept(deptCode, lineCode, piName, tableIds, queryField, timeBegin, timeEnd);
        return Result.OK(retDataList);
    }

    /**
     * PI周月累查询
     * @param deptCode 部门编码
     * @param lineCode 产线编码
     * @param year 年份
     * @param months 月份
     * @param piName PI名称
     * @return 统计结果
     */
    @AutoLog(value = "PI周月累查询")
    @Operation(summary = "PI周月累查询", description = "PI周月累查询")
    @GetMapping(value = "/getPiMonWeekSummary")
    public Result<Map<String, Object>> getPiMonWeekSummary(
            @RequestParam(name = "deptCode",required = false) String deptCode,
            @RequestParam(name = "lineCode",required = false) String lineCode,
            @RequestParam(name = "year") String year,
            @RequestParam(name = "months",required = false) String months,
            @RequestParam(name = "piName",required = false) String piName
    ) {
        return Result.OK(piStandardDataService.getPiMonWeekSummary(deptCode,lineCode,year,months,piName));
    }
}
