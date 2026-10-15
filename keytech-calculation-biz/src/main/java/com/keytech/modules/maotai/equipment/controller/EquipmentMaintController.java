package com.keytech.modules.maotai.equipment.controller;

import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.keytech.base.controller.BaseController;
import com.keytech.modules.maotai.equipment.entity.EquipmentLedger;
import com.keytech.modules.maotai.equipment.entity.EquipmentMaint;
import com.keytech.modules.maotai.equipment.mapper.EquipmentMaintMapper;
import com.keytech.modules.maotai.equipment.service.IEquipmentLedgerService;
import com.keytech.modules.maotai.equipment.service.IEquipmentMaintService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.apache.shiro.SecurityUtils;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.aspect.annotation.AutoDict;
import org.jeecg.common.aspect.annotation.AutoLog;
import org.jeecg.common.system.query.QueryGenerator;
import org.jeecg.common.system.vo.LoginUser;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.util.Arrays;

/**
 * 设备保养记录(保养工单)
 * @author maotai
 * @since 2026-06-15
 */
@Tag(name = "设备保养记录")
@RestController
@RequestMapping("/maotai/equipmentMaint")
@Slf4j
@RequiredArgsConstructor
public class EquipmentMaintController extends BaseController<IEquipmentMaintService, EquipmentMaint> {

    private final IEquipmentLedgerService equipmentLedgerService;
    private final EquipmentMaintMapper equipmentMaintMapper;

    /**
     * 分页列表查询(避开 BaseController 自带的 /queryPageList,使用 /list/page)
     */
    @Operation(summary = "设备保养记录-分页列表查询", description = "设备保养记录-分页列表查询")
    @AutoDict
    @GetMapping({"/list/page"})
    public Result<IPage<EquipmentMaint>> getPageList(EquipmentMaint entity, HttpServletRequest req,
                                                     @RequestParam(name = "pageNo", defaultValue = "1") Integer pageNo,
                                                     @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize,
                                                     @RequestParam(name = "orderField", required = false) String orderField,
                                                     @RequestParam(name = "orderType", required = false) String orderType,
                                                     @RequestParam(name = "selectFields", required = false) String selectFields) {
        QueryWrapper<EquipmentMaint> qw = QueryGenerator.initQueryWrapper(entity, req.getParameterMap());
        LoginUser loginUser = (LoginUser) SecurityUtils.getSubject().getPrincipal();
        if (loginUser != null && StringUtils.isNotEmpty(loginUser.getOrgCode())) {
            qw.likeRight("factory_code", loginUser.getOrgCode());
        }
        if (StringUtils.isNotEmpty(selectFields)) {
            qw.select(Arrays.stream(selectFields.split(",")).map(CharSequenceUtil::toUnderlineCase).toList());
        }
        if (StringUtils.isEmpty(orderField)) {
            // 默认按计划日期倒序
            qw.orderByDesc("plan_date");
        } else {
            if ("desc".equals(orderType)) {
                qw.orderByDesc(StrUtil.toUnderlineCase(orderField));
            }
            if ("asc".equals(orderType)) {
                qw.orderByAsc(StrUtil.toUnderlineCase(orderField));
            }
        }
        int tenantId = req.getIntHeader("X-Tenant-Id");
        if (tenantId > 0) {
            qw.eq("tenant_id", tenantId);
        }
        Page<EquipmentMaint> page = new Page<>(pageNo, pageSize);
        return Result.OK(this.baseService.page(page, qw));
    }

    /**
     * 添加(生成工单号 + 根据 equipmentId 回填设备名 + 回填租户/工厂)
     */
    @AutoLog(value = "设备保养记录-添加")
    @Operation(summary = "设备保养记录-添加")
    @PostMapping(value = "/add")
    public Result<String> add(@RequestBody EquipmentMaint entity, HttpServletRequest req) {
        // 生成工单号(规则在 service,后期可改)
        if (StringUtils.isEmpty(entity.getOrderNo())) {
            entity.setOrderNo(this.baseService.generateOrderNo());
        }
        // 根据 equipmentId 回填 equipmentName(列表/搜索直接用)
        if (StringUtils.isNotEmpty(entity.getEquipmentId()) && StringUtils.isEmpty(entity.getEquipmentName())) {
            EquipmentLedger eq = equipmentLedgerService.getById(entity.getEquipmentId());
            if (eq != null) {
                entity.setEquipmentName(eq.getEquipName());
            }
        }
        if (StringUtils.isEmpty(entity.getStatus())) {
            entity.setStatus("pending");
        }
        // 回填租户ID
        int tenantId = req.getIntHeader("X-Tenant-Id");
        if (tenantId > 0) {
            entity.setTenantId(tenantId);
        }
        // 回填工厂
        LoginUser loginUser = (LoginUser) SecurityUtils.getSubject().getPrincipal();
        if (loginUser != null && StringUtils.isNotEmpty(loginUser.getOrgCode()) && StringUtils.isEmpty(entity.getFactoryCode())) {
            entity.setFactoryCode(loginUser.getOrgCode());
        }
        this.baseService.save(entity);
        return Result.OK("添加成功！");
    }

    /**
     * 编辑(若改了 equipmentId 同步回填 equipmentName)
     */
    @AutoLog(value = "设备保养记录-编辑")
    @Operation(summary = "设备保养记录-编辑")
    @RequestMapping(value = "/edit", method = {RequestMethod.PUT, RequestMethod.POST})
    public Result<String> edit(@RequestBody EquipmentMaint entity) {
        if (StringUtils.isNotEmpty(entity.getEquipmentId())) {
            EquipmentLedger eq = equipmentLedgerService.getById(entity.getEquipmentId());
            if (eq != null) {
                entity.setEquipmentName(eq.getEquipName());
            }
        }
        this.baseService.updateById(entity);
        return Result.OK("编辑成功!");
    }

    /**
     * 通过id删除
     */
    @AutoLog(value = "设备保养记录-通过id删除")
    @Operation(summary = "设备保养记录-通过id删除")
    @DeleteMapping(value = "/delete")
    public Result<String> delete(@RequestParam(name = "id") String id) {
        this.baseService.removeById(id);
        return Result.OK("删除成功!");
    }

    /**
     * 批量删除
     */
    @AutoLog(value = "设备保养记录-批量删除")
    @Operation(summary = "设备保养记录-批量删除")
    @DeleteMapping(value = "/deletionBatch")
    public Result<String> deletionBatch(@RequestParam(name = "ids") String ids) {
        this.baseService.removeByIds(Arrays.asList(ids.split(",")));
        return Result.OK("批量删除成功!");
    }

    /**
     * 通过id查询
     */
    @Operation(summary = "设备保养记录-通过id查询")
    @GetMapping(value = "/queryById")
    public Result<EquipmentMaint> queryById(@RequestParam(name = "id") String id) {
        return Result.OK(this.baseService.getById(id));
    }

    /**
     * 顶部统计:本月已完成 / 待执行 / 逾期 / 保养覆盖率
     */
    @Operation(summary = "设备保养记录-统计", description = "设备保养记录-统计")
    @GetMapping(value = "/statistics")
    public Result<java.util.Map<String, Object>> statistics() {
        java.util.Map<String, Object> stat = new java.util.HashMap<>();
        LoginUser loginUser = (LoginUser) SecurityUtils.getSubject().getPrincipal();
        String factoryCode = loginUser != null ? loginUser.getOrgCode() : null;

        // 本月已完成(status=completed 且 complete_date 在本月)
        QueryWrapper<EquipmentMaint> monthQw = new QueryWrapper<>();
        monthQw.eq("status", "completed");
        monthQw.apply("DATE_FORMAT(complete_date,'%Y-%m') = DATE_FORMAT(NOW(),'%Y-%m')");
        if (StringUtils.isNotEmpty(factoryCode)) {
            monthQw.likeRight("factory_code", factoryCode);
        }
        stat.put("monthCompleted", this.baseService.count(monthQw));

        // 待执行(status=pending)
        QueryWrapper<EquipmentMaint> pendingQw = new QueryWrapper<>();
        pendingQw.eq("status", "pending");
        if (StringUtils.isNotEmpty(factoryCode)) {
            pendingQw.likeRight("factory_code", factoryCode);
        }
        stat.put("pending", this.baseService.count(pendingQw));

        // 逾期(status=overdue)
        QueryWrapper<EquipmentMaint> overdueQw = new QueryWrapper<>();
        overdueQw.eq("status", "overdue");
        if (StringUtils.isNotEmpty(factoryCode)) {
            overdueQw.likeRight("factory_code", factoryCode);
        }
        stat.put("overdue", this.baseService.count(overdueQw));

        // 保养覆盖率(历史口径)= 已被保养过的设备数(distinct equipment_id, status=completed)/ 当前工厂设备总数
        long maintainedEq;
        long totalEq;
        try {
            maintainedEq = equipmentMaintMapper.countMaintainedEquipment(factoryCode);
            QueryWrapper<EquipmentLedger> eqQw = new QueryWrapper<>();
            if (StringUtils.isNotEmpty(factoryCode)) {
                eqQw.likeRight("factory_code", factoryCode);
            }
            totalEq = equipmentLedgerService.count(eqQw);
        } catch (Exception e) {
            maintainedEq = 0;
            totalEq = 0;
        }
        double coverage = totalEq > 0 ? (maintainedEq * 100.0 / totalEq) : 0.0;
        stat.put("coverageRate", Math.round(coverage * 10) / 10.0);

        return Result.OK(stat);
    }

    /**
     * 导出excel
     */
    @GetMapping(value = "/exportXls")
    public ModelAndView exportXls(HttpServletRequest request, EquipmentMaint entity) {
        return super.exportXls(request, entity, EquipmentMaint.class, "设备保养记录");
    }

    /**
     * 通过excel导入数据
     */
    @PostMapping(value = "/importExcel")
    public Result<?> importExcel(HttpServletRequest request, HttpServletResponse response) {
        return super.importExcel(request, response, EquipmentMaint.class);
    }
}
