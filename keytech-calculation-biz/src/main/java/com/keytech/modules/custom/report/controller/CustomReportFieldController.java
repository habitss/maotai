package com.keytech.modules.custom.report.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.keytech.base.controller.BaseController;
import com.keytech.modules.custom.report.entity.CustomReportField;
import com.keytech.modules.custom.report.service.ICustomReportFieldService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.aspect.annotation.AutoLog;
import org.jeecg.common.config.GlobalErrorEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
* @Description: 自定义报表字段
* @Author: jeecg-boot
* @Date:   2024-11-13
* @Version: V1.0
*/
@Tag(name="自定义报表字段")
@RestController
@RequestMapping("/customReport/customReportField")
@Slf4j
public class CustomReportFieldController extends BaseController<ICustomReportFieldService,CustomReportField> {

    @Autowired
    private ICustomReportFieldService customReportFieldService;

    /**
     * 分页列表查询
     *
     * @param customReportField
     * @param pageNo
     * @param pageSize
     * @param req
     * @return
     */
    @AutoLog(value = "自定义报表字段-分页列表查询")
    @Operation(summary = "自定义报表字段-分页列表查询", description = "自定义报表字段-分页列表查询")
    @GetMapping(value = "/listData")
    public Result<IPage<CustomReportField>> queryPageList(CustomReportField customReportField,
          @RequestParam(name = "pageNo",defaultValue = "1") Integer pageNo,
          @RequestParam(name = "pageSize",defaultValue = "10") Integer pageSize,
          @RequestParam(name = "orderField",required = false) String orderField,
          @RequestParam(name = "orderType",required = false) String orderType,
          @RequestParam(name = "selectFields",required = false) String selectFields,
          HttpServletRequest req) {
        Page<CustomReportField> list = baseService.list(customReportField, pageNo, pageSize, orderField, orderType, null, req);
        return Result.OK(list);
    }

    /**
     * 获取最大排序值
     * @param tableId
     * @return
     */
    @AutoLog(value = "自定义报表字段-获取最大排序")
    @Operation(summary = "自定义报表字段-获取最大排序", description = "自定义报表字段-获取最大排序")
    @GetMapping(value = "/getMaxSort")
    public Result<Double> getMaxSort(@RequestParam(name = "tableId") String tableId) {
        return Result.OK(baseService.getMaxSort(tableId));
    }


    /**
     *   添加
     *
     * @param customReportField
     * @return
     */
    @AutoLog(value = "自定义报表字段-添加")
    @Operation(summary = "自定义报表字段-添加", description = "自定义报表字段-添加")
    @PostMapping(value = "/add")
    public Result<String> add(@RequestBody CustomReportField customReportField) {
        Result<String> result = customReportFieldService.saveField(customReportField);
        return Result.OK("添加成功！");
    }

    /**
     *  编辑
     *
     * @param customReportField
     * @return
     */
    @AutoLog(value = "自定义报表字段-编辑")
    @Operation(summary = "自定义报表字段-编辑", description = "自定义报表字段-编辑")
    @RequestMapping(value = "/edit", method = {RequestMethod.PUT,RequestMethod.POST})
    public Result<String> edit(@RequestBody CustomReportField customReportField) {
        customReportFieldService.updateById(customReportField);
        return Result.OK("编辑成功!");
    }

    /**
     *   通过id删除
     *
     * @param id
     * @return
     */
    @AutoLog(value = "自定义报表字段-通过id删除")
    @Operation(summary = "自定义报表字段-通过id删除", description = "自定义报表字段-通过id删除")
    @DeleteMapping(value = "/deleteData")
    public Result<String> deleteData(@RequestParam(name="id",required=true) String id) {
        customReportFieldService.removeById(id);
        return Result.ok(GlobalErrorEnum.SUCCESS.getCode());
    }


    /**
     *  批量删除
     *
     * @param ids
     * @return
     */
    @AutoLog(value = "自定义报表字段-批量删除")
    @Operation(summary = "自定义报表字段-批量删除", description = "自定义报表字段-批量删除")
    @DeleteMapping(value = "/deleteBatchData")
    public Result<String> deleteBatchData(@RequestParam(name="ids",required=true) String ids) {
        this.customReportFieldService.removeByIds(Arrays.asList(ids.split(",")));
        return Result.OK("批量删除成功!");
    }

    /**
     * 通过id查询
     *
     * @param id
     * @return
     */
    @AutoLog(value = "自定义报表字段-通过id查询")
    @Operation(summary = "自定义报表字段-通过id查询", description = "自定义报表字段-通过id查询")
    @GetMapping(value = "/queryById")
    public Result<CustomReportField> queryById(@RequestParam(name="id",required=true) String id) {
        CustomReportField customReportField = customReportFieldService.getById(id);
        if(customReportField==null) {
            return Result.error("未找到对应数据");
        }
        return Result.OK(customReportField);
    }


    /**
     * 获取字段(唯一约束)
     */
    @Operation(summary = "获取字段(唯一约束)", description = "获取字段(唯一约束)")
    @GetMapping(value = "/getFieldByTableId")
    public Result<?> getFieldByTableId(CustomReportField customReportField, HttpServletRequest req) {
        LambdaQueryWrapper<CustomReportField> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(CustomReportField::getTableId,customReportField.getTableId());
        queryWrapper.eq(null !=customReportField.getIsUnique(),CustomReportField::getIsUnique,customReportField.getIsUnique());
        List<CustomReportField> list = customReportFieldService.list(queryWrapper);
//		 List<CustomReportField> list = null;
        return Result.OK(list);
    }


    /**
     * 获取表头
     * @param templateId
     * @return
     */
    @AutoLog(value = "自定义报表字段-通过模板id查询表头")
    @Operation(summary = "自定义报表字段-通过模板id查询表头", description = "自定义报表字段-通过模板id查询表头")
    @GetMapping(value = "/queryHeaderByTemplateId")
    public Result<?> queryHeaderByTemplateId(@RequestParam(name="templateId",required=true) String templateId) {
        return Result.OK(customReportFieldService.queryHeaderByTemplateId(templateId));
    }

    /**
     * 获取模板统计查询数据
     */
    @AutoLog(value = "获取报表数据-分页列表查询")
    @Operation(summary = "获取报表数据-分页列表查询", description = "获取报表数据-分页列表查询")
    @GetMapping(value = "/queryData")
    public Result<?> queryData(
           @RequestParam(name = "pageNo",defaultValue = "1") Integer pageNo,
           @RequestParam(name = "pageSize",defaultValue = "10") Integer pageSize,
           @RequestParam(name = "orderField",required = false) String orderField,
           @RequestParam(name = "orderType",required = false) String orderType,
           @RequestParam(name = "templateId") String templateId,
           HttpServletRequest req) {

        Map<String,Object> result = customReportFieldService.queryData(pageNo, pageSize, orderField, orderType,
                templateId, req);
        return Result.OK(result);
    }

    /**
     * 导出excel
     *
     * @param request  HTTP请求对象
     * @param customReportField 自定义报表字段配置实体对象，用于传递查询条件
     * @return ModelAndView 返回Excel文件视图
     */
    @AutoLog(value = "自定义报表字段配置-导出excel")
    @Operation(summary = "自定义报表字段配置-导出excel", description = "自定义报表字段配置-导出excel")
    @GetMapping(value = "/exportXls")
    public ModelAndView exportXls(HttpServletRequest request, CustomReportField customReportField) {
        return exportXls(request, customReportField, CustomReportField.class, "自定义报表字段配置");
    }


    /**
     * 通过excel导入数据
     *
     * @param request  HTTP请求对象
     * @param response HTTP响应对象
     * @return 导入结果
     */
    @Operation(summary = "通过excel导入数据")
    @PostMapping(value = "/importExcel")
    @Transactional
    public Result<?> importExcel(HttpServletRequest request, HttpServletResponse response) throws IOException {
        return baseService.importExcel(request, response, CustomReportField.class);
    }
}
