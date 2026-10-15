package com.keytech.modules.maotai.equipment.controller;

import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.keytech.base.controller.BaseController;
import com.keytech.modules.maotai.equipment.entity.EquipmentLedger;
import com.keytech.modules.maotai.equipment.service.IEquipmentLedgerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.aspect.annotation.AutoDict;
import org.jeecg.common.aspect.annotation.AutoLog;
import org.jeecg.common.system.query.QueryGenerator;
import org.jeecg.common.system.vo.LoginUser;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.util.Arrays;
import java.util.List;

/**
 * 设备台账
 * @author maotai
 * @since 2026-06-15
 */
@Tag(name = "设备台账")
@RestController
@RequestMapping("/maotai/equipmentLedger")
@Slf4j
@RequiredArgsConstructor
public class EquipmentLedgerController extends BaseController<IEquipmentLedgerService, EquipmentLedger> {

    /**
     * 分页列表查询(避开 BaseController 自带的 /queryPageList,使用 /list/page)
     * 按当前登录用户的工厂 + 租户过滤
     */
    @Operation(summary = "设备台账-分页列表查询", description = "设备台账-分页列表查询")
    @AutoDict
    @GetMapping({"/list/page"})
    public Result<IPage<EquipmentLedger>> getPageList(EquipmentLedger entity, HttpServletRequest req,
                                                      @RequestParam(name = "pageNo", defaultValue = "1") Integer pageNo,
                                                      @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize,
                                                      @RequestParam(name = "orderField", required = false) String orderField,
                                                      @RequestParam(name = "orderType", required = false) String orderType,
                                                      @RequestParam(name = "selectFields", required = false) String selectFields) {
        QueryWrapper<EquipmentLedger> queryWrapper = QueryGenerator.initQueryWrapper(entity, req.getParameterMap());
        // 根据当前登录用户工厂过滤
        LoginUser loginUser = (LoginUser) SecurityUtils.getSubject().getPrincipal();
        if (loginUser != null && StringUtils.isNotEmpty(loginUser.getOrgCode())) {
            queryWrapper.likeRight("factory_code", loginUser.getOrgCode());
        }
        // 指定字段查询
        if (StringUtils.isNotEmpty(selectFields)) {
            List<String> selectFieldList = Arrays.stream(selectFields.split(",")).map(CharSequenceUtil::toUnderlineCase).toList();
            queryWrapper.select(selectFieldList);
        }
        // 排序
        if (StringUtils.isNotEmpty(orderField) && StringUtils.isNotEmpty(orderType)) {
            if ("desc".equals(orderType)) {
                queryWrapper.orderByDesc(StrUtil.toUnderlineCase(orderField));
            }
            if ("asc".equals(orderType)) {
                queryWrapper.orderByAsc(StrUtil.toUnderlineCase(orderField));
            }
        }
        // 租户过滤
        int tenantId = req.getIntHeader("X-Tenant-Id");
        if (tenantId > 0) {
            queryWrapper.eq("tenant_id", tenantId);
        }
        Page<EquipmentLedger> page = new Page<>(pageNo, pageSize);
        return Result.OK(this.baseService.page(page, queryWrapper));
    }

    /**
     * 添加(校验功能位置编码租户内唯一,回填租户/工厂)
     */
    @AutoLog(value = "设备台账-添加")
    @Operation(summary = "设备台账-添加")
    //@RequiresPermissions("maotai:mes3_maotai_equipment_ledger:add")
    @PostMapping(value = "/add")
    public Result<String> add(@RequestBody EquipmentLedger entity, HttpServletRequest req) {
        // 回填租户ID(前端 X-Tenant-Id)
        int tenantId = req.getIntHeader("X-Tenant-Id");
        if (tenantId > 0) {
            entity.setTenantId(tenantId);
        }
        // 回填工厂(当前登录用户部门)
        LoginUser loginUser = (LoginUser) SecurityUtils.getSubject().getPrincipal();
        if (loginUser != null && StringUtils.isNotEmpty(loginUser.getOrgCode()) && StringUtils.isEmpty(entity.getFactoryCode())) {
            entity.setFactoryCode(loginUser.getOrgCode());
        }
        // 功能位置编码租户内唯一校验
        QueryWrapper<EquipmentLedger> dup = new QueryWrapper<EquipmentLedger>()
                .eq("funloc_code", entity.getFunlocCode());
        if (entity.getTenantId() != null) {
            dup.eq("tenant_id", entity.getTenantId());
        }
        long count = this.baseService.count(dup);
        if (count > 0) {
            return Result.error("功能位置编码已存在:" + entity.getFunlocCode());
        }
        this.baseService.save(entity);
        return Result.OK("添加成功！");
    }

    /**
     * 编辑
     */
    @AutoLog(value = "设备台账-编辑")
    @Operation(summary = "设备台账-编辑")
    //@RequiresPermissions("maotai:mes3_maotai_equipment_ledger:edit")
    @RequestMapping(value = "/edit", method = {RequestMethod.PUT, RequestMethod.POST})
    public Result<String> edit(@RequestBody EquipmentLedger entity) {
        this.baseService.updateById(entity);
        return Result.OK("编辑成功!");
    }

    /**
     * 通过id删除
     */
    @AutoLog(value = "设备台账-通过id删除")
    @Operation(summary = "设备台账-通过id删除")
    //@RequiresPermissions("maotai:mes3_maotai_equipment_ledger:delete")
    @DeleteMapping(value = "/delete")
    public Result<String> delete(@RequestParam(name = "id") String id) {
        this.baseService.removeById(id);
        return Result.OK("删除成功!");
    }

    /**
     * 批量删除(避开 BaseController 自带的 /deleteBatch[Integer],使用 /deletionBatch[String])
     */
    @AutoLog(value = "设备台账-批量删除")
    @Operation(summary = "设备台账-批量删除")
    //@RequiresPermissions("maotai:mes3_maotai_equipment_ledger:deletionBatch")
    @DeleteMapping(value = "/deletionBatch")
    public Result<String> deletionBatch(@RequestParam(name = "ids") String ids) {
        this.baseService.removeByIds(Arrays.asList(ids.split(",")));
        return Result.OK("批量删除成功!");
    }

    /**
     * 通过id查询
     */
    @Operation(summary = "设备台账-通过id查询")
    @GetMapping(value = "/queryById")
    public Result<EquipmentLedger> queryById(@RequestParam(name = "id") String id) {
        return Result.OK(this.baseService.getById(id));
    }

    /**
     * 导出excel
     */
    //@RequiresPermissions("maotai:mes3_maotai_equipment_ledger:exportXls")
    @GetMapping(value = "/exportXls")
    public ModelAndView exportXls(HttpServletRequest request, EquipmentLedger entity) {
        return super.exportXls(request, entity, EquipmentLedger.class, "设备台账");
    }

    /**
     * 通过excel导入数据
     */
    //@RequiresPermissions("maotai:mes3_maotai_equipment_ledger:importExcel")
    @PostMapping(value = "/importExcel")
    public Result<?> importExcel(HttpServletRequest request, HttpServletResponse response) {
        return super.importExcel(request, response, EquipmentLedger.class);
    }
}
