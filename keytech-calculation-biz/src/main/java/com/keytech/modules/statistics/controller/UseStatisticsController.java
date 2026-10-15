package com.keytech.modules.statistics.controller;


import com.baomidou.mybatisplus.core.metadata.IPage;
import com.keytech.base.controller.BaseController;
import com.keytech.modules.kpi.entity.KpiInfo;
import com.keytech.modules.kpi.service.IKpiInfoService;
import com.keytech.modules.statistics.service.IUseStatisticsService;
import com.keytech.modules.statistics.vo.TableChildrenVo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.api.vo.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@Tag(name="系统使用率")
@RestController
@RequestMapping("/statistics/useStatistics")
@Slf4j
public class UseStatisticsController{
    @Autowired
    private IUseStatisticsService useStatisticsService;

    //系统使用统计查询
    @Operation(summary="系统使用率查询")
    @GetMapping(value = "/list")
    public Result<?> list(String checkDateBegin, String checkDateEnd, Boolean week) {
        List<Map> list = useStatisticsService.getStatistics(checkDateBegin,checkDateEnd,week);
        return Result.OK(list);
    }

    //获取表头
    @Operation(summary="获取表头")
    @GetMapping(value = "/getHead")
    public Result<?> getHead() {
        List<TableChildrenVo> list = useStatisticsService.getHead();
        return Result.OK(list);
    }
}
