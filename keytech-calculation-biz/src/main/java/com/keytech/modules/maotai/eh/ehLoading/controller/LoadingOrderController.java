package com.keytech.modules.maotai.eh.ehLoading.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.keytech.base.controller.BaseController;
import com.keytech.modules.maotai.eh.ehLoading.entity.LoadingOrder;
import com.keytech.modules.maotai.eh.ehLoading.service.LoadingOrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.apache.shiro.SecurityUtils;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.aspect.annotation.AutoDict;
import org.jeecg.common.aspect.annotation.AutoLog;
import org.jeecg.common.system.query.QueryGenerator;
import org.jeecg.common.system.vo.LoginUser;
import org.springframework.dao.DuplicateKeyException;
import org.jeecg.common.exception.JeecgBootBizTipException;
import org.springframework.web.bind.annotation.*;

/**
 * 装卸车工单(对接 E+H)。
 * 工单落库 → 推送 E+H → 状态回写。
 *
 * @author maotai
 * @since 2026-07-21
 */
@Tag(name = "装卸车工单")
@RestController
@RequestMapping("/maotai/loadingOrder")
@Slf4j
@RequiredArgsConstructor
public class LoadingOrderController extends BaseController<LoadingOrderService, LoadingOrder> {

    /**
     * 工单分页查询(按当前登录用户的工厂 + 租户过滤)。
     * @param loadingOrder 查询条件(实体字段值做等值/like 匹配)
     * @param req HTTP 请求(取 X-Tenant-Id 头)
     * @param pageNo 页码,默认 1
     * @param pageSize 每页条数,默认 10
     * @return 分页结果,按创建时间倒序
     */
    @Operation(summary = "工单-分页列表(按工厂/租户过滤)")
    @AutoDict
    @GetMapping("/list/page")
    public Result<IPage<LoadingOrder>> getPageList(LoadingOrder loadingOrder, HttpServletRequest req,
                                                   @RequestParam(name = "pageNo", defaultValue = "1") Integer pageNo,
                                                   @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize) {
        QueryWrapper<LoadingOrder> qw = QueryGenerator.initQueryWrapper(loadingOrder, req.getParameterMap());
        LoginUser u = (LoginUser) SecurityUtils.getSubject().getPrincipal();
        if (u != null && StringUtils.isNotEmpty(u.getOrgCode())) {
            qw.likeRight("factory_code", u.getOrgCode());
        }
        int tenantId = req.getIntHeader("X-Tenant-Id");
        if (tenantId > 0) {
            qw.eq("tenant_id", tenantId);
        }
        qw.orderByDesc("create_time");
        Page<LoadingOrder> page = new Page<>(pageNo, pageSize);
        return Result.OK(this.baseService.page(page, qw));
    }

    /**
     * 新增工单(默认 pushStatus=0 待推,不推送 E+H)。
     * <p>自动回填租户(X-Tenant-Id 头)与工厂(登录用户 orgCode),并跑 validateOrder 校验
     * (IC 卡 UInt32 / IC 卡活跃唯一 / 48h)。OrderNo 唯一不再预查,靠 uk_tenant_order
     * 唯一索引 + catch DuplicateKeyException 返回"工单号已存在"(幂等,并发安全)。
     * @param loadingOrder 工单(9 必填:orderNo/carNo/preWeight/productName/validate/driverName/idloadingOrder/carrierName/customerName)
     * @param req HTTP 请求(取租户头)
     * @return 操作结果
     */
    @AutoLog(value = "装卸车工单-新增")
    @Operation(summary = "工单-新增(默认推送状态=待推,自动校验)")
    @PostMapping("/add")
    public Result<String> add(@Valid @RequestBody LoadingOrder loadingOrder, HttpServletRequest req) {
        int tenantId = req.getIntHeader("X-Tenant-Id");
        if (tenantId > 0) {
            loadingOrder.setTenantId(tenantId);
        }
        LoginUser u = (LoginUser) SecurityUtils.getSubject().getPrincipal();
        if (u != null && StringUtils.isNotEmpty(u.getOrgCode()) && StringUtils.isEmpty(loadingOrder.getFactoryCode())) {
            loadingOrder.setFactoryCode(u.getOrgCode());
        }
        if (loadingOrder.getPushStatus() == null) {
            loadingOrder.setPushStatus(0);
        }
        // 状态由推送/回传驱动,建单阶段清空(防调用方误传)
        loadingOrder.setStatus(null);
        loadingOrder.setOrderStatus(null);
        this.baseService.validateOrder(loadingOrder, null);
        try {
            this.baseService.save(loadingOrder);
        } catch (DuplicateKeyException e) {
            throw new JeecgBootBizTipException("工单号已存在: " + loadingOrder.getOrderNo());
        }
        return Result.OK("添加成功!");
    }

    /**
     * 编辑工单(跑 validateOrder 校验,排除自身 id)。改单号撞 uk_tenant_order 同样幂等提示。
     * @param loadingOrder 工单(含 id 和要改的字段)
     * @return 操作结果
     */
    @AutoLog(value = "装卸车工单-编辑")
    @Operation(summary = "工单-编辑(自动校验)")
    @RequestMapping(value = "/edit", method = {RequestMethod.PUT, RequestMethod.POST})
    public Result<String> edit(@Valid @RequestBody LoadingOrder loadingOrder) {
        this.baseService.validateOrder(loadingOrder, loadingOrder.getId());
        try {
            this.baseService.updateById(loadingOrder);
        } catch (DuplicateKeyException e) {
            throw new JeecgBootBizTipException("工单号已存在: " + loadingOrder.getOrderNo());
        }
        return Result.OK("编辑成功!");
    }

    /**
     * 通过 id 逻辑删除工单。
     * @param id 工单主键
     * @return 操作结果
     */
    @AutoLog(value = "装卸车工单-删除")
    @Operation(summary = "工单-通过id删除")
    @DeleteMapping("/delete")
    public Result<String> delete(@RequestParam(name = "id") String id) {
        this.baseService.removeById(id);
        return Result.OK("删除成功!");
    }

    /**
     * 通过 id 查询工单详情。
     * @param id 工单主键
     * @return 工单(含 pushStatus/pushMsg/businessStatus 等)
     */
    @Operation(summary = "工单-通过id查询")
    @GetMapping("/queryById")
    public Result<LoadingOrder> queryById(@RequestParam(name = "id") String id) {
        return Result.OK(this.baseService.getById(id));
    }

    /**
     * 推送单条工单到 E+H(/api/Bussiness/Create),按结果回写 pushStatus。
     * <p>推送前再跑一遍 validateOrder(防工单建好后 IC 卡被别的活跃单占用仍强推)。
     * @param id 工单主键
     * @return 回写后的工单(pushStatus 1=已推/2=失败、pushTime、pushMsg)
     */
    @AutoLog(value = "装卸车工单-推送E+H")
    @Operation(summary = "工单-推送单条到 E+H(推送前再校验),回写推送状态")
    @PostMapping("/push/{id}")
    public Result<LoadingOrder> push(@PathVariable(name = "id") String id) {
        return Result.OK(this.baseService.pushOrder(id));
    }

    /**
     * 手动结束工单(调 E+H /api/Bussiness/Close,API19,仅已推工单可结束)。
     * @param id 工单主键
     * @return 结束处理后的工单
     */
    @AutoLog(value = "装卸车工单-结束工单")
    @Operation(summary = "工单-手动结束工单(API19 调 E+H Close,仅已推工单可结束)")
    @PostMapping("/close/{id}")
    public Result<LoadingOrder> close(@PathVariable(name = "id") String id) {
        return Result.OK(this.baseService.closeOrder(id));
    }

    /**
     * 交换两个工单的车牌(只换 carNo)。
     * <p>按各自 pushStatus 分别处理:已推→Close 作废 E+H 旧单 + 新 OrderNo 重推;
     * 待推/失败→只在 MES 改 carNo,不碰 E+H。混合态自动分别处理。
     * @param id1 工单1 主键
     * @param id2 工单2 主键
     * @return 是否成功
     */
    // 车牌交换已停用:待确认"一车绑定一单"业务规则,确定后连同 service.swapCarNo / handleSwapped 整体删除
    /*
    @AutoLog(value = "装卸车工单-交换车牌")
    @Operation(summary = "工单-交换两个工单的车牌(按推送状态分别处理:已推→Close+新号重推;待推/失败→只改MES)")
    @PostMapping("/swapCarNo")
    public Result<Boolean> swapCarNo(@RequestParam(name = "id1") String id1,
                                     @RequestParam(name = "id2") String id2) {
        return Result.OK(this.baseService.swapCarNo(id1, id2));
    }
    */
}
