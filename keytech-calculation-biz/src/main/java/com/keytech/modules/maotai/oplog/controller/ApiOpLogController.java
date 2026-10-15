package com.keytech.modules.maotai.oplog.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.keytech.base.controller.BaseController;
import com.keytech.modules.maotai.oplog.entity.ApiOpLog;
import com.keytech.modules.maotai.oplog.service.ApiOpLogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.aspect.annotation.AutoDict;
import org.jeecg.common.aspect.annotation.AutoLog;
import org.jeecg.common.system.query.QueryGenerator;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 接口操作日志(审计追溯入口,全系统出入站接口总表)。
 * 覆盖 E+H(API04/08/11/12/15/16/19)/中集自控(API05/06/13/14/17)/报警等全部出入站接口,支持
 * direction/apiCode/orderNo/checkNo/operator/result 过滤 + 时间范围。
 * 走 /maotai/**(Shiro JWT)。表内容由 ApiOpLogCleanupJob 每天物理清理,默认保留 7 天。
 *
 * @author maotai
 * @since 2026-08-14
 */
@Tag(name = "接口操作日志")
@RestController
@RequestMapping("/maotai/apiOpLog")
@Slf4j
@RequiredArgsConstructor
public class ApiOpLogController extends BaseController<ApiOpLogService, ApiOpLog> {

    /**
     * 操作日志分页查询(审计追溯)。
     * @param apiOpLog 查询条件(direction/apiCode/orderNo/checkNo/operator/result 等值匹配)
     * @param req HTTP 请求(QueryGenerator 支持 operateTime_begin/operateTime_end 范围)
     * @param pageNo 页码,默认 1
     * @param pageSize 每页条数,默认 10
     * @return 分页结果,按操作时间倒序
     */
    @AutoDict
    @Operation(summary = "接口操作日志-分页查询(全系统审计追溯,支持方向/接口/工单/操作人/结果过滤)")
    @GetMapping("/list/page")
    public Result<IPage<ApiOpLog>> getPageList(ApiOpLog ehOpLog, HttpServletRequest req,
                                              @RequestParam(name = "pageNo", defaultValue = "1") Integer pageNo,
                                              @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize) {
        QueryWrapper<ApiOpLog> qw = QueryGenerator.initQueryWrapper(ehOpLog, req.getParameterMap());
        qw.orderByDesc("operate_time");
        Page<ApiOpLog> page = new Page<>(pageNo, pageSize);
        return Result.OK(this.baseService.page(page, qw));
    }
}
