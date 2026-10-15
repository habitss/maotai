package com.keytech.modules.maotai.equipment.controller;

import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.keytech.base.controller.BaseController;
import com.keytech.modules.maotai.equipment.entity.EquipmentDoc;
import com.keytech.modules.maotai.equipment.service.IEquipmentDocService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
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

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * 设备操作手册(SOP/SWI 等)
 * @author maotai
 * @since 2026-06-15
 */
@Tag(name = "设备操作手册")
@RestController
@RequestMapping("/maotai/equipmentDoc")
@Slf4j
@RequiredArgsConstructor
public class EquipmentDocController extends BaseController<IEquipmentDocService, EquipmentDoc> {

    /** 允许的文件后缀白名单 */
    private static final Set<String> ALLOW_SUFFIX = Set.of("pdf", "doc", "docx", "xls", "xlsx", "jpg", "jpeg", "png");

    /**
     * 分页列表查询(按 equipmentId 过滤,避开 BaseController 自带 /queryPageList)
     */
    @Operation(summary = "设备操作手册-分页列表查询", description = "设备操作手册-分页列表查询")
    @AutoDict
    @GetMapping({"/list/page"})
    public Result<IPage<EquipmentDoc>> getPageList(EquipmentDoc entity, HttpServletRequest req,
                                                   @RequestParam(name = "pageNo", defaultValue = "1") Integer pageNo,
                                                   @RequestParam(name = "pageSize", defaultValue = "100") Integer pageSize,
                                                   @RequestParam(name = "orderField", required = false) String orderField,
                                                   @RequestParam(name = "orderType", required = false) String orderType,
                                                   @RequestParam(name = "selectFields", required = false) String selectFields) {
        QueryWrapper<EquipmentDoc> qw = QueryGenerator.initQueryWrapper(entity, req.getParameterMap());
        LoginUser u = (LoginUser) SecurityUtils.getSubject().getPrincipal();
        if (u != null && StringUtils.isNotEmpty(u.getOrgCode())) {
            qw.likeRight("factory_code", u.getOrgCode());
        }
        if (StringUtils.isNotEmpty(selectFields)) {
            qw.select(Arrays.stream(selectFields.split(",")).map(CharSequenceUtil::toUnderlineCase).toList());
        }
        if (StringUtils.isNotEmpty(orderField) && StringUtils.isNotEmpty(orderType)) {
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
        // 默认按发布日期倒序
        if (StringUtils.isEmpty(orderField)) {
            qw.orderByDesc("publish_date");
        }
        Page<EquipmentDoc> page = new Page<>(pageNo, pageSize);
        return Result.OK(this.baseService.page(page, qw));
    }

    /**
     * 添加(上传文件后调用:校验文件后缀 + 回填租户/工厂)
     */
    @AutoLog(value = "设备操作手册-添加")
    @Operation(summary = "设备操作手册-添加")
    @PostMapping(value = "/add")
    public Result<String> add(@RequestBody EquipmentDoc entity, HttpServletRequest req) {
        // 文件后缀白名单校验
        if (StringUtils.isEmpty(entity.getFileName())) {
            return Result.error("请先上传文件");
        }
        String suffix = entity.getFileName().substring(entity.getFileName().lastIndexOf('.') + 1).toLowerCase(Locale.ROOT);
        if (!ALLOW_SUFFIX.contains(suffix)) {
            return Result.error("不支持的文件类型,仅允许:pdf/doc/docx/xls/xlsx/jpg/png");
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
     * 编辑(可只改元信息;若带新 fileUrl/fileName 则替换文件)
     */
    @AutoLog(value = "设备操作手册-编辑")
    @Operation(summary = "设备操作手册-编辑")
    @RequestMapping(value = "/edit", method = {RequestMethod.PUT, RequestMethod.POST})
    public Result<String> edit(@RequestBody EquipmentDoc entity) {
        this.baseService.updateById(entity);
        return Result.OK("编辑成功!");
    }

    /**
     * 通过id删除
     */
    @AutoLog(value = "设备操作手册-通过id删除")
    @Operation(summary = "设备操作手册-通过id删除")
    @DeleteMapping(value = "/delete")
    public Result<String> delete(@RequestParam(name = "id") String id) {
        this.baseService.removeById(id);
        return Result.OK("删除成功!");
    }

    /**
     * 通过id查询
     */
    @Operation(summary = "设备操作手册-通过id查询")
    @GetMapping(value = "/queryById")
    public Result<EquipmentDoc> queryById(@RequestParam(name = "id") String id) {
        return Result.OK(this.baseService.getById(id));
    }
}
