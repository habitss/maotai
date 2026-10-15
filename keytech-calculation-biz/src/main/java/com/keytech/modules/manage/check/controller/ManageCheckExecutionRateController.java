package com.keytech.modules.manage.check.controller;


import com.keytech.modules.manage.check.service.IManageCheckExecutionRateService;
import com.keytech.modules.statistics.vo.TableChildrenVo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.api.vo.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@Tag(name="管理者检查清单执行率统计")
@RestController
@RequestMapping("/manage/check/executionRate")
@Slf4j
public class ManageCheckExecutionRateController {
    @Autowired
    private IManageCheckExecutionRateService manageCheckExecutionRateService;

    //执行率统计查询
    @Operation(summary="执行率统计查询")
    @GetMapping(value = "/list")
    public Result<?> list(String checkDateBegin, String checkDateEnd, String dept, String postId, String checkerName) {
        List<Map> list = manageCheckExecutionRateService.getStatistics(checkDateBegin, checkDateEnd, dept, postId, checkerName);
        return Result.OK(list);
    }

}
